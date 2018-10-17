/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl;


import com.grail.GrailGlobals;
import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.util.KantarGlobals;
import com.grail.object.GrailAttachment;
import com.grail.object.GrailEmailQueryAttachment;
import com.grail.osp.object.OSPAttachment;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.object.SynchroAttachment;

import com.jivesoftware.base.database.JivePropertyMap;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.sequence.SequenceManager;
import com.jivesoftware.cache.Cache;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentVersion;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.ImageContainerResource;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveContext;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.VersionManager;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.attachments.AttachmentManagerFactory;
import com.jivesoftware.community.event.AttachmentEvent;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.impl.dao.AttachmentDAO;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.util.ImageUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Database implementation of the Attachment interface. Attachment data can be stored in
 * one of two ways:<ol>
 * <li>On the file system.
 * <li>In the database using BLOBS. In this mode, attachments are cached on
 * the file system for improved performance (default).
 * </ol>
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class DbAttachment implements Attachment, JivePropertyMap.PropertyMapListener {

    private static final Logger log = LogManager.getLogger(DbAttachment.class);

    private final JiveContext jiveContext = JiveApplication.getContext();

    protected AttachmentBean bean;
    private JivePropertyMap properties;
    private AttachmentContentResource associatedResource;

    public DbAttachment() {
    }

    public DbAttachment(long id) throws DAOException {
        bean = getByID(id);
        properties = new JivePropertyMap(bean.getProperties(), true, this);
    }

    public DbAttachment(AttachmentBean bean) {
        this.bean = bean;
        properties = new JivePropertyMap(this.bean.getProperties(), true, this);
    }

    DbAttachment(AttachmentBean bean, AttachmentContentResource parent) {
        this.bean = bean;
        setAssociatedAttachmentContentResource(parent);
        properties = new JivePropertyMap(this.bean.getProperties(), true, this);
    }

    public DbAttachment(JiveObject jiveObject, String name, String contentType, InputStream data)
            throws AttachmentException
    {
        this(jiveObject, name, contentType, data, new HashMap<>(), JiveContentObject.Status.PUBLISHED);
    }

    public DbAttachment(JiveObject jiveObject, String name, String contentType, InputStream data,
                        Map<String, String> props, JiveContentObject.Status status)
            throws AttachmentException
    {
        bean = new AttachmentBean(); // set early, to prevent NPE in getAttachmentManager()

        // Keep track of what this attachment is associated with
        if (jiveObject instanceof AttachmentContentResource) {
            setAssociatedAttachmentContentResource((AttachmentContentResource) jiveObject);
        }

        // An uploaded container image is always converted into a png
        boolean validContainerImage = (jiveObject instanceof ImageContainerResource) &&
                ((ImageContainerResource)jiveObject).isImageEnabled() &&
                "image/png".equals(contentType);
        boolean validContentType = getAttachmentManager().isValidType(contentType);

        String filenameMimeType = jiveContext.getMimeTypeManager().getExtensionMimeType(name.toLowerCase());
        boolean validFileType = getAttachmentManager().isValidType(filenameMimeType);


        // See if the contentType is valid.
        if (!validContainerImage && !(validContentType && validFileType)) {
            // If the content type is not valid, ensure it wasn't auto-zipped
            if (props != null && !props.containsKey(AttachmentHelper.ZIPPED_ATTACH_PROP)) {
                throw new AttachmentException(AttachmentException.BAD_CONTENT_TYPE);
            }
        }

        bean.setID(SequenceManager.nextID(JiveConstants.ATTACHMENT));
        bean.setObjectID(jiveObject.getID());
        bean.setObjectType(jiveObject.getObjectType());
        bean.setName(name);
        bean.setContentType(contentType);
        bean.setCreationDate(System.currentTimeMillis());
        bean.setModificationDate(bean.getCreationDate());
        bean.setStatus(status);
        if (props == null) {
            props = new HashMap<>();
        }
        bean.setProperties(props);
        this.properties = new JivePropertyMap(bean.getProperties(), true, this);

        if (jiveObject.getObjectType() == JiveConstants.DOCUMENT && jiveObject.getID() < 0) {
            bean.setDocumentID(((Document) jiveObject).getDocumentID());
        }

        // If the content type is image/jpeg, then read the EXIF metadata and rotate the image if Orientation is present.
        if (ImageUtils.doesContentTypeSupportExif (contentType)) {
            try {
                InputStream updatedData = ImageUtils.rotateFromOrientationMetadata(data, jiveContext.getAttachmentManager().getMaxAttachmentSize());
                insertBean(updatedData);
            } catch (IOException e) {
                throw new AttachmentException("Error reading in image data to perform exif processing.", e, name);
            }
        } else {
            insertBean(data);
        }
    }

    protected AttachmentBean getByID(long id) {
        AttachmentDAO attachmentDAO = getAttachmentDAO();
        return attachmentDAO.getByAttachmentID(id);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {AttachmentException.class})
    protected void insertBean(InputStream data) throws AttachmentException {
        AttachmentDAO attachmentDAO = getAttachmentDAO();

        try {
            attachmentDAO.create(bean);
            insert(data);
            // we need to update since the size will not be set until the data is inserted
            attachmentDAO.update(bean);

            if (bean.getObjectID() > 0) {
                getAttachmentCache().put(bean.getID(), bean);
            }
            clearCachedAttachmentList();
        }
        catch (IOException ioe) {
            //if a custom inputstream has wrapped an attachment exception out of necessity, throw that instead
            if (ioe.getCause() instanceof AttachmentException) {
                AttachmentException cause = (AttachmentException) ioe.getCause();
                throw new AttachmentException(cause.getErrorType(), bean.getName());
            }

            throw new AttachmentException(AttachmentException.GENERAL_ERROR, ioe);
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);

            // special case lame ass mysql error - their protocol tries to stuff everything into
            // a single 'packet' which is a really dumb solution overall for handling blobs and clobs
            if (e.getCause() instanceof SQLException &&
                    e.getMessage().indexOf("com.mysql.jdbc.PacketTooBigException") != -1)
            {
                throw new AttachmentException(AttachmentException.TOO_LARGE, e.getMessage());
            }
            else {
                throw new DAOException(e);
            }
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {AttachmentException.class})
    public void updateData(InputStream data) throws AttachmentException {
        AttachmentDAO attachmentDAO = getAttachmentDAO();

        try {
            insert(data);
            // we need to update since the size will not be set until the data is inserted
            attachmentDAO.update(bean);

            if (bean.getObjectID() > 0) {
                getAttachmentCache().put(bean.getID(), bean);
            }
            clearCachedAttachmentList();
        }
        catch (IOException ioe) {
            throw new AttachmentException(AttachmentException.GENERAL_ERROR, ioe);
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);

            // special case lame ass mysql error - their protocol tries to stuff everything into
            // a single 'packet' which is a really dumb solution overall for handling blobs and clobs
            if (e.getCause() instanceof SQLException &&
                    e.getMessage().indexOf("com.mysql.jdbc.PacketTooBigException") != -1)
            {
                throw new AttachmentException(AttachmentException.TOO_LARGE, e.getMessage());
            }
            else {
                throw new DAOException(e);
            }
        }
    }

    // Attachment Interface

    public long getID() {
        return bean.getID();
    }

    public long getObjectID() {
        return bean.getObjectID();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void setObjectID(long objectID) {
        bean.setObjectID(objectID);

        save();
    }

    public int getObjectType() {
        return JiveConstants.ATTACHMENT;
    }

    public AttachmentContentResource getAttachmentContentResource() {
        AttachmentContentResource result = associatedResource;
        if (result == null) {
            result = loadAttachmentContentResource();
            setAssociatedAttachmentContentResource(result);
        }
        return result;
    }

    void setAssociatedAttachmentContentResource(@Nullable AttachmentContentResource parent) {
        this.associatedResource = parent;
    }

    private AttachmentContentResource loadAttachmentContentResource() {
        AttachmentContentResource resource = null;
        try {
            int type = bean.getObjectType();
            long objectID = bean.getObjectID();

            if (type == JiveConstants.DOCUMENT && objectID == -1 && bean.getDocumentID() != null) {
                // temporary document - no way to load so no way to return it
                return null;
            }
            else if (objectID <= 0 || type == JiveConstants.NULL) {
                // temporary object - no way to load so no way to return it
                return null;
            }
            else if(SynchroGlobal.isSynchroAttachmentObjectType(bean.getObjectType())) {
                SynchroAttachment synchroAttachment = new SynchroAttachment();
                synchroAttachment.getBean().setObjectId(bean.getObjectID());
                synchroAttachment.getBean().setObjectType(bean.getObjectType());
                resource = synchroAttachment;
            } else if(KantarGlobals.isKantarAttachmentType(bean.getObjectType())
                    || KantarGlobals.isKantarReportAttachmentType(bean.getObjectType())) {
                KantarAttachment kantarAttachment = new KantarAttachment();
                kantarAttachment.getBean().setObjectId(bean.getObjectID());
                kantarAttachment.getBean().setObjectType(bean.getObjectType());
                resource = kantarAttachment;
            } else if(GrailGlobals.isGrailEmailQueriesAttachmentType(bean.getObjectType())) {
                GrailEmailQueryAttachment grailAttachment = new GrailEmailQueryAttachment();
                grailAttachment.getBean().setObjectId(bean.getObjectID());
                grailAttachment.getBean().setObjectType(bean.getObjectType());
                resource = grailAttachment;
            } else if(GrailGlobals.isGrailAttachmentType(bean.getObjectType())) {
                GrailAttachment grailAttachment = new GrailAttachment();
                grailAttachment.getBean().setObjectId(bean.getObjectID());
                grailAttachment.getBean().setObjectType(bean.getObjectType());
                resource = grailAttachment;
            } else if(SynchroGlobal.isOSPAttachmentType(bean.getObjectType())) {
           	 OSPAttachment ospAttachment = new OSPAttachment();
           	 ospAttachment.getOspFile().setAttachmentId(bean.getObjectID());
           	 //ospAttachment.getBean().setObjectType(bean.getObjectType());
                resource = ospAttachment;
           } 
            else {
                DatabaseObjectLoader objectLoader = DatabaseObjectLoader.getInstance();
                resource = (AttachmentContentResource) objectLoader.getJiveObject(type, objectID);
            }

            if (resource.getObjectType() == JiveConstants.DOCUMENT) {
                Document doc = (Document) resource;
                VersionManager versionManager = doc.getVersionManager();

                for (int i = versionManager.getNewestDocumentVersion().getVersionNumber(); i >= 1; i--) {

                    try {

                        if (versionManager
                                .isVersionViewable(i, jiveContext.getAuthenticationProvider().getJiveUser()))
                        {

                            DocumentVersion version = versionManager.getDocumentVersion(i);
                            if (getAttachmentManager().getAttachmentCount(version.getDocument()) > 0) {

                                Iterable<Attachment> attachments = getAttachmentManager()
                                        .getAttachments(version.getDocument());
                                for (Attachment attachment : attachments) {
                                    if (attachment.getID() == bean.getID()) {
                                        return version.getDocument();
                                    }
                                }
                            }
                        }

                    }
                    catch (Exception e) {
                        log.error(String.format("Can't load version %d of document with id %d", i, doc.getID()), e);

                    }
                }

            }
        }
        catch (NotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return resource;
    }

    public int getContentObjectType() {
        return bean.getObjectType();
    }

    public String getContentType() {
        return bean.getContentType();
    }

    public String getName() {
        return bean.getName();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void setName(String name) {
        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("Cannot set name with empty or null value.");
        }

        bean.setName(name);
        bean.setModificationDate(System.currentTimeMillis());


        save();
    }

    public int getSize() {
        return bean.getSize();
    }

    @Override
    public void setSize(int size) {
        bean.setSize(size);
        save();
    }

    @Override
    public void setExStorageFileID(Long exStorageFileID) {
        bean.setExStorageFileID(exStorageFileID);
        save();
    }

    @Override
    public Long getExStorageFileID() {
        return bean.getExStorageFileID();
    }

    public Date getCreationDate() {
        return new Date(bean.getCreationDate());
    }

    public Date getModificationDate() {
        return new Date(bean.getModificationDate());
    }

    public int getDownloadCount() {
        return bean.getDownloadCount();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    protected void incrementDownloadCount() {
        bean.setDownloadCount(bean.getDownloadCount() + 1);
    }

    public AttachmentBean getBean() {
        return bean;
    }

    @Override
    public JiveContentObject.Status getStatus() {
        return bean.getStatus();
    }

    public void setStatus(JiveContentObject.Status status) {
        bean.setStatus(status);
        save();
    }

    public InputStream getData() throws IOException {
        return getStatus() == JiveContentObject.Status.PUBLISHED ? getUnfilteredData() : null;
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public InputStream getUnfilteredData() throws IOException {
        return getAttachmentManager().getAttachmentData(this);
    }



    public String propertyMapRetrieveFilteredValue(String key, String unfilteredValue) {
        return unfilteredValue;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void propertyMapEntryAdded(String addedKey) {
        save();

        if (bean.getObjectID() != -1 && bean.getObjectType() == JiveConstants.DOCUMENT) {
            // fire off an event
            Map<String, String> params = new HashMap<>();
            params.put("Type", "propertyAdd");
            params.put("PropertyKey", addedKey);

            try {
                DocumentManager documentManager = jiveContext.getDocumentManager();
                Document parentDoc = documentManager.getDocument(bean.getObjectID());
                dispatchModifiedEvent(params, parentDoc);
            }
            catch (DocumentObjectNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void dispatchModifiedEvent(Map<String, String> params, Document parentDoc) {
        jiveContext.getEventDispatcher()
                .fire(new AttachmentEvent(AttachmentEvent.Type.MODIFIED, parentDoc, this, params));
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void propertyMapEntryModified(String modifiedKey, String originalValue) {
        save();
        DocumentManager documentManager = jiveContext.getDocumentManager();

        if (bean.getObjectID() != -1 && bean.getObjectType() == JiveConstants.DOCUMENT) {
            // fire off an event
            Map<String, String> params = new HashMap<>();
            params.put("Type", "propertyModify");
            params.put("PropertyKey", modifiedKey);
            params.put("originalValue", originalValue);

            try {
                Document parentDoc = documentManager.getDocument(bean.getObjectID());
                dispatchModifiedEvent(params, parentDoc);
            }
            catch (DocumentObjectNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void propertyMapEntryDeleted(String deletedKey) {
        DocumentManager documentManager = jiveContext.getDocumentManager();

        if (bean.getObjectID() != -1 && bean.getObjectType() == JiveConstants.DOCUMENT) {
            // fire off an event
            Map<String, String> params = new HashMap<>();
            params.put("Type", "propertyDelete");
            params.put("PropertyKey", deletedKey);

            try {
                Document parentDoc = documentManager.getDocument(bean.getObjectID());
                dispatchModifiedEvent(params, parentDoc);
            }
            catch (DocumentObjectNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }

        save();
    }

    // Other Methods

    /**
     * Deletes the attachment.
     *
     * @throws DAOException If there is trouble deleting the attachment.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void delete() throws DAOException {
        try {
            getAttachmentManager().deleteAttachment(this);
        }
        catch (AttachmentException e) {
            log.warn("Could not delete attachment binary", e);
        }
        getAttachmentCache().remove(bean.getID());
        clearCachedAttachmentList();
    }

    protected AttachmentDAO getAttachmentDAO() {
        return (AttachmentDAO) jiveContext.getSpringBean("attachmentDAO");
    }

    @SuppressWarnings({"unchecked"})
    protected Cache<Long, AttachmentBean> getAttachmentCache() {
        return (Cache<Long, AttachmentBean>) jiveContext.getSpringBean("attachmentCache");
    }

    @SuppressWarnings({"unchecked"})
    protected void clearCachedAttachmentList() {

        Cache<EntityDescriptor, long[]> cache =
                (Cache<EntityDescriptor, long[]>) jiveContext.getSpringBean("attachmentListCache");
        cache.remove(new EntityDescriptor(getContentObjectType(), getObjectID()));

    }

    /**
     * Saves the image to storage and updates the db
     *
     * @param data the inputstream containing the attachment data.
     * @throws AttachmentException if the attachment is missing information
     * @throws IOException         if an error occurs writing the attachment to storage
     * @throws DAOException        If there is trouble inserting the attachment data
     */
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY,
            rollbackFor = {IOException.class, AttachmentException.class})
    private void insert(InputStream data) throws DAOException, IOException, AttachmentException {
        getAttachmentManager(true).insertAttachmentData(this, data);
    }

    private AttachmentManager getAttachmentManager() {
        return getAttachmentManager(false);
    }

    /**
     * get the attachment-manager that should be used for this attachment.
     * @param useParentResource true if should decide on type of attachment-manager by associatedResources.
     *                          default is false. should be true only during upload. (insert(InputStream data) method)
     * @return the attachment-manager to use
     */
    private AttachmentManager getAttachmentManager(boolean useParentResource) {
        AttachmentManagerFactory attachmentManagerFactory = jiveContext.getSpringBean("attachmentManagerFactory");
        if (useParentResource) {
            return attachmentManagerFactory.getAttachmentManager(this, this.associatedResource);
        }
        else {
            return attachmentManagerFactory.getAttachmentManager(this);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    protected void save() {
        try {
            getAttachmentDAO().update(bean);
            getAttachmentCache().put(bean.getID(), bean);
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DbAttachment");
        sb.append("{properties=").append(properties);
        sb.append(", bean=").append(bean);
        sb.append('}');
        return sb.toString();
    }
}
