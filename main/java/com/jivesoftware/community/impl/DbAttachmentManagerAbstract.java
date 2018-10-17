/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.grail.GrailGlobals;
import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.util.KantarGlobals;
import com.grail.object.GrailAttachment;
import com.grail.object.GrailEmailQueryAttachment;
import com.grail.osp.object.OSPAttachment;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.object.DefaultTemplate;
import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.database.DatabaseException;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.base.event.v2.EventSource;
import com.jivesoftware.base.proxy.JiveProxy;
import com.jivesoftware.cache.Cache;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentLimitInfoProvider;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveHome;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.JiveRuntimeException;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.VersionableAttachmentContentResource;
import com.jivesoftware.community.audit.aop.Audit;
import com.jivesoftware.community.event.AttachmentEvent;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.impl.dao.AttachmentDAO;
import com.jivesoftware.community.impl.dao.AttachmentManagerBean;
import com.jivesoftware.community.impl.dao.AttachmentManagerDAO;
import com.jivesoftware.community.objecttype.AttachmentLimitableType;
import com.jivesoftware.community.objecttype.JiveObjectType;
import com.jivesoftware.community.objecttype.ObjectTypeManager;
import com.jivesoftware.community.objecttype.ObjectTypeUtils;
import com.jivesoftware.community.objecttype.impl.ForumMessageObjectType;
import com.jivesoftware.community.util.MimeTypeFile;
import com.jivesoftware.util.ClassUtilsStatic;

/**
 * Database implementation of the AttachmentManager interface.
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public abstract class DbAttachmentManagerAbstract implements AttachmentManager, EventSource {

    private static final Logger log = LogManager.getLogger(DbAttachmentManagerAbstract.class);

    private static File attachmentDir;
    private long docTypeID;
    private AttachmentDAO attachmentDAO;
    private AttachmentManagerDAO attachmentManagerDAO;
    private Cache<Long, AttachmentBean> attachmentCache;
    private Cache<EntityDescriptor, long[]> attachmentListCache;
    private Cache<String, Long> attachmentNameCache;
    private EventDispatcher eventDispatcher;
    private ObjectTypeManager objectTypeManager;

    private static final int DEFAULT_MAX_ATTACHMENTS_PER_OBJECT = 5;

    public DbAttachmentManagerAbstract(long docTypeID) {
        this.docTypeID = docTypeID;
    }

    public DbAttachmentManagerAbstract() {
        this(-1L);
    }

    public void setAttachmentDAO(AttachmentDAO attachmentDAO) {
        this.attachmentDAO = attachmentDAO;
    }

    public void setAttachmentCache(Cache<Long, AttachmentBean> attachmentCache) {
        this.attachmentCache = attachmentCache;
    }

    public void setAttachmentListCache(Cache<EntityDescriptor, long[]> attachmentListCache) {
        this.attachmentListCache = attachmentListCache;
    }

    public void setAttachmentNameCache(Cache<String, Long> attachmentNameCache) {
        this.attachmentNameCache = attachmentNameCache;
    }

    public void destroy() {
        attachmentDir = null;
    }

    public boolean isConfigured() {
        AttachmentManagerBean bean = getAttachmentManagerBean();

        return (bean != null);
    }

    private AttachmentBean getAttachmentBean(long attachmentId) {
        AttachmentBean bean = attachmentCache.get(attachmentId);
        if (bean == null) {
            bean = attachmentDAO.getByAttachmentID(attachmentId);
            if (bean != null) {
                attachmentCache.put(attachmentId, bean);
            }
        }
        return bean;
    }

    public Attachment getAttachment(JiveObject jo, String attachmentName)
            throws IllegalArgumentException, AttachmentNotFoundException
    {

        Attachment a = null;

        //check name cache first
        String name = createNameCacheKey(jo, attachmentName);
        Long id = attachmentNameCache.get(name);
        if (id != null) {
            try {
                a = getAttachment(id);
            }
            catch (NotFoundException nfe) {
                //no-op
            }
        }
        if (a == null) {
            try {
                AttachmentBean bean = attachmentDAO.getByAttachmentName(jo.getID(), jo.getObjectType(), attachmentName);
                if (bean != null) {
                    attachmentNameCache.put(name, bean.getID());
                    a = new DbAttachment(bean);
                }
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return a;
    }

    @Override
    public List<Attachment> getByExStorageFileID(Long exStorageFileID)
            throws IllegalArgumentException, AttachmentNotFoundException
    {
        final List<AttachmentBean> attachments = attachmentDAO.getByExStorageFileID(exStorageFileID);
        return Lists.transform(attachments, new Function<AttachmentBean, Attachment>() {
            @Nullable
            @Override
            public Attachment apply(@Nullable AttachmentBean input) {
                return new DbAttachment(input);
            }
        });
    }

    private String createNameCacheKey(JiveObject jo, String attachmentName) {
        return new StringBuilder().append(jo.getID()).append("|").append(jo.getObjectType()).append("|")
                .append(attachmentName).toString();
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteAttachment(Attachment attachment, Map<String, Object> eventParams) {
        try {
            AttachmentBean bean = getAttachmentBean(attachment.getID());

            // send attachment deleting event
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("attachmentBean", bean);
            
            AttachmentContentResource resource = null;
            if(SynchroGlobal.isSynchroAttachmentObjectType(bean.getObjectType())) {
                SynchroAttachment synchroAttachment = new SynchroAttachment();
                synchroAttachment.getBean().setObjectId(bean.getObjectID());
                synchroAttachment.getBean().setObjectType(bean.getObjectType());
                resource = synchroAttachment;
            } if(KantarGlobals.isKantarAttachmentType(bean.getObjectType())
                    || KantarGlobals.isKantarReportAttachmentType(bean.getObjectType())) {
                KantarAttachment kantarAttachment = new KantarAttachment();
                kantarAttachment.getBean().setObjectId(bean.getObjectID());
                kantarAttachment.getBean().setObjectType(bean.getObjectType());
                resource = kantarAttachment;
            } if(GrailGlobals.isGrailEmailQueriesAttachmentType(bean.getObjectType())) {
                GrailAttachment grailAttachment = new GrailAttachment();
                grailAttachment.getBean().setObjectId(bean.getObjectID());
                grailAttachment.getBean().setObjectType(bean.getObjectType());
                resource = grailAttachment;
            } if(GrailGlobals.isGrailAttachmentType(bean.getObjectType())) {
                GrailEmailQueryAttachment grailAttachment = new GrailEmailQueryAttachment();
                grailAttachment.getBean().setObjectId(bean.getObjectID());
                grailAttachment.getBean().setObjectType(bean.getObjectType());
                resource = grailAttachment;
            } 
            
            if(SynchroGlobal.isOSPAttachmentType(bean.getObjectType())) {
            	 OSPAttachment ospAttachment = new OSPAttachment();
            	 ospAttachment.getOspFile().setAttachmentId(bean.getObjectID());
            	 //ospAttachment.getBean().setObjectType(bean.getObjectType());
                 resource = ospAttachment;
            } 
            
            else {
                resource = attachment.getAttachmentContentResource();
            }
            
            if ( eventParams != null ) {
                paramMap.putAll(eventParams);
            }
           /* this.eventDispatcher.fire(new AttachmentEvent(AttachmentEvent.Type.DELETED,
                    attachment.getAttachmentContentResource(), attachment, paramMap));*/
            
            this.eventDispatcher.fire(new AttachmentEvent(AttachmentEvent.Type.DELETED,
            		resource, attachment, paramMap));

            // callback to the versionable resource - don't actually delete the attachment in this case,
            // since the previous version of a resource can be restored.
          //  AttachmentContentResource resource = attachment.getAttachmentContentResource();
            if (resource != null && resource instanceof VersionableAttachmentContentResource) {
                ((VersionableAttachmentContentResource) resource).deleteAttachment(attachment);
                return;
            }

            attachmentDAO.delete(bean);
            attachmentCache.remove(attachment.getID());
          //  if (attachment.getAttachmentContentResource() != null) {
            if (resource != null) {
                EntityDescriptor descriptor = new EntityDescriptor(resource);
                attachmentListCache.remove(descriptor);
                attachmentNameCache.remove(createNameCacheKey(descriptor, attachment.getName()));
            }

            deleteAttachmentBinary(attachment);
        }
        catch (DAOException e) {
            log.error("Unable to delete attachment", e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteAttachment(Attachment attachment) {
        deleteAttachment(attachment, null);
    }

    public void expireTemporaryAttachments() {
        List<AttachmentBean> beans = attachmentDAO.retrieveOldTemporaryAttachments();
        for (AttachmentBean bean : beans) {
            deleteAttachment(new DbAttachment(bean));
        }
    }

    public boolean isAttachmentsEnabled() {
        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }


        return bean.isAttachmentsEnabled();
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void setAttachmentsEnabled(boolean enabled) {
        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (docTypeID != -1) {
            try {
                if (enabled != bean.isAttachmentsEnabled()) {
                    bean.setAttachmentsEnabled(enabled);
                    attachmentManagerDAO.update(bean);
                }
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            JiveGlobals.setJiveProperty("attachments.enabled", String.valueOf(enabled));
        }
    }

    public int getMaxAttachmentSize() {
        return getAttachmentManagerBean().getMaxAttachmentSize();
    }

    @Override
    public int getOverallMaxAttachmentsPerAttachableContentType() {

        List<JiveObjectType> objectTypes = objectTypeManager.getObjectTypes();
        int maxAttachments = 0;
        for (JiveObjectType objectType : objectTypes) {

            if (objectType instanceof AttachmentLimitableType
                    && ((AttachmentLimitableType) objectType).getAttachmentLimitInfoProvider().getMaxAttachmentsPerObject() > maxAttachments) {
                maxAttachments = ((AttachmentLimitableType) objectType).getAttachmentLimitInfoProvider().getMaxAttachmentsPerObject();
            }
        }

        return maxAttachments;
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void setMaxAttachmentSize(int maxAttachmentSize) {
        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (docTypeID != -1) {
            try {
                if (maxAttachmentSize != bean.getMaxAttachmentSize()) {
                    bean.setMaxAttachmentSize(maxAttachmentSize);
                    attachmentManagerDAO.update(bean);
                }
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            JiveGlobals.setJiveProperty("attachments.maxAttachmentSize", String.valueOf(maxAttachmentSize));
        }
    }

    public int getMaxAttachmentsPerDocument() {
        DocumentContentType docContentType = (DocumentContentType) ObjectTypeUtils
                .getJiveObjectType(JiveConstants.DOCUMENT);
        return getMaxAttachmentsFromObjectType(docContentType,
                getAttachmentManagerBean().getMaxAttachmentsPerDocument());
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void setMaxAttachmentsPerDocument(int maxAttachmentsPerDocument) {
        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (docTypeID != -1) {
            try {
                if (maxAttachmentsPerDocument != bean.getMaxAttachmentsPerDocument()) {
                    bean.setMaxAttachmentsPerDocument(maxAttachmentsPerDocument);
                    attachmentManagerDAO.update(bean);
                }
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            JiveGlobals.setJiveProperty("attachments.maxAttachmentsPerDocument",
                    "" + maxAttachmentsPerDocument);
        }
    }

    public boolean isValidType(String contentType) {
        if (isAllowAllByDefault()) {
            return !getDisallowedTypes().contains(contentType);
        }
        else {
            return getAllowedTypes().contains(contentType);
        }
    }

    public List<String> getAllowedTypes() {
        return Collections.unmodifiableList(getAttachmentManagerBean().getAllowedTypes());
    }

    public List<String> getAllowedTypesAsFileExtensions() {
        List<String> types = getAttachmentManagerBean().getAllowedTypes();
        if (types == null || types.isEmpty()) {
            return Collections.emptyList();
        }

        return getMimeTypes().extensionsFor(types);
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void addAllowedType(String contentType) {
        if (contentType == null) {
            return;
        }

        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (getAllowedTypes().contains(contentType)) {
            return;
        }

        if (docTypeID != -1) {
            try {
                bean.getAllowedTypes().add(contentType);
                attachmentManagerDAO.update(bean);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            List<String> allowed = getAttachmentManagerBean().getAllowedTypes();
            allowed.add(contentType);
            JiveGlobals.setJiveProperty("attachments.allowedTypes", listToString(allowed));
        }
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void removeAllowedType(String contentType) {
        if (contentType == null) {
            return;
        }

        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (!getAllowedTypes().contains(contentType)) {
            return;
        }

        if (docTypeID != -1) {
            try {
                bean.getAllowedTypes().remove(contentType);
                attachmentManagerDAO.update(bean);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            List<String> allowed = getAttachmentManagerBean().getAllowedTypes();
            allowed.remove(contentType);
            JiveGlobals.setJiveProperty("attachments.allowedTypes", listToString(allowed));
        }
    }

    public List<String> getDisallowedTypes() {
        return Collections.unmodifiableList(getAttachmentManagerBean().getDisallowedTypes());
    }

    public List<String> getDisallowedTypesAsFileExtensions() {
        List<String> types = getAttachmentManagerBean().getDisallowedTypes();
        if (types == null || types.isEmpty()) {
            return Collections.emptyList();
        }

        return getMimeTypes().extensionsFor(types);
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void addDisallowedType(String contentType) {
        if (contentType == null) {
            return;
        }

        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (getDisallowedTypes().contains(contentType)) {
            return;
        }

        if (docTypeID != -1) {
            try {
                bean.getDisallowedTypes().add(contentType);
                attachmentManagerDAO.update(bean);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            List<String> disallowed = getAttachmentManagerBean().getDisallowedTypes();
            disallowed.add(contentType);
            JiveGlobals.setJiveProperty("attachments.disallowedTypes", listToString(disallowed));
        }
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void removeDisallowedType(String contentType) {
        if (contentType == null) {
            return;
        }

        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (!getDisallowedTypes().contains(contentType)) {
            return;
        }

        if (docTypeID != -1) {
            try {
                bean.getDisallowedTypes().remove(contentType);
                attachmentManagerDAO.update(bean);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            List<String> disallowed = getAttachmentManagerBean().getDisallowedTypes();
            disallowed.remove(contentType);
            JiveGlobals.setJiveProperty("attachments.disallowedTypes", listToString(disallowed));
        }
    }

    public boolean isAllowAllByDefault() {
        return getAttachmentManagerBean().isAllowAllByDefault();
    }

    @Audit
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void setAllowAllByDefault(boolean allowAllByDefault) {
        AttachmentManagerBean bean;

        if (!isConfigured()) {
            bean = createAttachmentManagerBean();
        }
        else {
            bean = getAttachmentManagerBean();
        }

        if (allowAllByDefault == bean.isAllowAllByDefault()) {
            return;
        }

        if (docTypeID != -1) {
            try {
                bean.setAllowAllByDefault(allowAllByDefault);
                attachmentManagerDAO.update(bean);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }
        else {
            JiveGlobals.setJiveProperty("attachments.allowAllByDefault", "" + allowAllByDefault);
        }
    }

    /**
     * Returns the directory that attachments are stored in.
     *
     * @return the directory that attachments are stored in.
     */
    public synchronized static File getAttachmentDir() {
        attachmentDir = JiveHome.getAttachments();
        if (!attachmentDir.exists()) {
            boolean result = attachmentDir.mkdir();
            if (!result) {
                log.error("Unable to create attachment directory: '" + attachmentDir + "'");
            }
        }
        return attachmentDir;
    }

    public boolean isImagePreviewEnabled() {
        return getGlobalAttachmentManagerBean().isImagePreviewEnabled();
    }

    @Audit
    public void setImagePreviewEnabled(boolean imagePreviewEnabled) throws UnauthorizedException {
        JiveGlobals.setJiveProperty("attachments.imagePreview.enabled", String.valueOf(imagePreviewEnabled));
    }

    public int getImagePreviewMaxSize() {
        return getGlobalAttachmentManagerBean().getImagePreviewMaxSize();
    }

    @Audit
    public void setImagePreviewMaxSize(int imagePreviewMaxSize) throws UnauthorizedException {
        JiveGlobals.setJiveProperty("attachments.imagePreview.maxSize", String.valueOf(imagePreviewMaxSize));
    }

    public boolean isImagePreviewRatioEnabled() {
        return getGlobalAttachmentManagerBean().isImagePreviewRatioEnabled();
    }

    @Audit
    public void setImagePreviewRatioEnabled(boolean imagePreviewRatioEnabled) throws UnauthorizedException {
        JiveGlobals.setJiveProperty("attachments.imagePreview.preserveAspectRatio",
                String.valueOf(imagePreviewRatioEnabled));
    }

    public Attachment getAttachment(long attachmentID) throws AttachmentNotFoundException {
        return getAttachment(attachmentID, null);
    }

    private Attachment getAttachment(long attachmentID, AttachmentContentResource parent)
            throws AttachmentNotFoundException {
        try {
            AttachmentBean attachmentBean = getAttachmentBean(attachmentID);
            if (attachmentBean == null) {
                throw new AttachmentNotFoundException();
            }
            return new DbAttachment(attachmentBean, parent);
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new JiveRuntimeException(e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED,
            rollbackFor = {AttachmentException.class, UnauthorizedException.class, IOException.class})
    public Attachment createAttachment(AttachmentContentResource resource, String name,
                                       String contentType, InputStream data, File file)
            throws IllegalStateException, AttachmentException, UnauthorizedException
    {
        return createAttachment(resource, name, contentType, data, file, new HashMap<>(), null);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED,
            rollbackFor = {AttachmentException.class, UnauthorizedException.class, IOException.class})
    public Attachment createAttachment(AttachmentContentResource resource, String name, String contentType,
            InputStream data,
            File file, Map<String, String> fileProperties, Map<String, Object> eventParams)
            throws IllegalStateException, AttachmentException, UnauthorizedException
    {
       // checkAttachmentLimit(resource, name);
    	 if(!((resource instanceof SynchroAttachment) || (resource instanceof MyLibraryDocument)
                 || (resource instanceof DefaultTemplate) || resource instanceof GrailAttachment || resource instanceof KantarAttachment || resource instanceof OSPAttachment)) {
             checkAttachmentLimit(resource, name);
         }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        paramMap.put("contentType", contentType);

        try {
            Attachment attachment = new DbAttachment(resource, name, contentType, data, fileProperties,
                    JiveContentObject.Status.PUBLISHED);

            // callback to the versionable resource
            if (resource instanceof VersionableAttachmentContentResource) {
                VersionableAttachmentContentResource versionable = (VersionableAttachmentContentResource) resource;
                attachment = versionable.createAttachment(attachment);
            }

            if ( eventParams != null ) {
                paramMap.putAll(eventParams);
            }

            paramMap.put("size", attachment.getSize());
            this.eventDispatcher.fire(new AttachmentEvent(AttachmentEvent.Type.ADDED, resource, attachment, paramMap));
            return attachment;
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new DatabaseException(e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED,
            rollbackFor = {AttachmentException.class, UnauthorizedException.class, IOException.class})
    public void setAttachmentParent(Attachment attachment, AttachmentContentResource resource)
            throws AttachmentException, AttachmentNotFoundException
    {
        AttachmentBean bean = getAttachmentBean(attachment.getID());
        if (bean.getObjectID() > 0) {
            throw new AttachmentException(String.format("Parent for attachment %d could not be updated as it already "
                    + "has a parent %d", bean.getID(), bean.getObjectID()));
        }
        checkAttachmentLimit(resource, attachment.getName());
        bean.setObjectType(resource.getObjectType());
        bean.setObjectID(resource.getID());
        attachmentDAO.update(bean);

        attachmentCache.remove(attachment.getID());
        EntityDescriptor descriptor = new EntityDescriptor(resource);
        attachmentListCache.remove(descriptor);
        attachmentNameCache.remove(createNameCacheKey(descriptor, attachment.getName()));

        this.eventDispatcher.fire(new AttachmentEvent(AttachmentEvent.Type.MODIFIED, resource, attachment,
                Collections.<String, Object>emptyMap()));
    }

    public int getMaxAttachmentsPerMessage() {
        ForumMessageObjectType forumMessageObjectType = (ForumMessageObjectType) ObjectTypeUtils
                .getJiveObjectType(JiveConstants.MESSAGE);
        return getMaxAttachmentsFromObjectType(forumMessageObjectType,
                getAttachmentManagerBean().getMaxAttachmentsPerMessage());
    }

    @Audit
    public void setMaxAttachmentsPerMessage(int maxAttachmentsPerMessage) throws UnauthorizedException {
        JiveGlobals.setJiveProperty("attachments.maxAttachmentsPerMessage", Integer.toString(maxAttachmentsPerMessage));
    }

    public int getMaxAttachmentsPerBlogPost() {
        BlogPostObjectType blogPostObjectType = (BlogPostObjectType) ObjectTypeUtils
                .getJiveObjectType(JiveConstants.BLOGPOST);
        return getMaxAttachmentsFromObjectType(blogPostObjectType,
                getAttachmentManagerBean().getMaxAttachmentsPerBlogPost());
    }

    /**
     * Given an AttachmentLimitableType returns the max number of attachments it specifies. This method will also check
     * for <tt>null</tt> for both the given type and the <tt>AttachmentLimitInfoProvider</tt> it returns to prevent a
     * <tt>NullPointerException</tt> when the type is disabled.
     *
     * @param attachmentLimitableType the type for which the max number of attachments are being returned.
     * @param defaultMaxAttachments   the default value, to be returned if the given type does not have an
     *                                <tt>AttachmentLimitInfoProvider</tt> available.
     * @return the max number of attachments specified by the given AttachmentLimitableType
     * @since Jive SBS 4.5.0
     */
    protected int getMaxAttachmentsFromObjectType(AttachmentLimitableType attachmentLimitableType,
                                                  int defaultMaxAttachments)
    {
        if (attachmentLimitableType == null) {
            return 0;
        }
        AttachmentLimitInfoProvider alip = attachmentLimitableType.getAttachmentLimitInfoProvider();
        if (alip != null) {
            return alip.getMaxAttachmentsPerObject();
        }
        return defaultMaxAttachments;
    }

    @Audit
    public void setMaxAttachmentsPerBlogPost(int maxAttachmentsPerBlogPost) throws UnauthorizedException {
        JiveGlobals
                .setJiveProperty("attachments.maxAttachmentsPerBlogPost", Integer.toString(maxAttachmentsPerBlogPost));
    }

    public int getAttachmentCount(AttachmentContentResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        // callback to the versionable resource
        if (resource instanceof VersionableAttachmentContentResource) {
            VersionableAttachmentContentResource versionable = (VersionableAttachmentContentResource) resource;
            return versionable.getAttachmentCount();
        }

        return getAttachmentIDsByParent(resource).length;
    }

    public Iterable<Attachment> getAttachments(AttachmentContentResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        if (resource instanceof VersionableAttachmentContentResource) {
            // new method to see if resource is instance of jiveproxy.
            // if it is, get unproxied object and cast it to the versionable
            final VersionableAttachmentContentResource versionable = getVersionable(resource);
            return Iterables.transform(versionable.getAttachments(), new Function<Attachment,Attachment>() {
                @Override
                public Attachment apply(@Nullable Attachment attachment) {
                    if (attachment instanceof DbAttachment) {
                        DbAttachment dbAttachment = (DbAttachment) attachment;
                        dbAttachment.setAssociatedAttachmentContentResource(versionable);
                    }
                    return attachment;
                }
            });
        }
        return getAttachmentsByParent(resource);
    }

    public List<Attachment> getAttachmentsByParent(final AttachmentContentResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        if (!isTempDocument(resource) && resource instanceof VersionableAttachmentContentResource) {
            // new method to see if resource is instance of jiveproxy.
            // if it is, get unproxied object and cast it to the versionable
            VersionableAttachmentContentResource versionable = getVersionable(resource);
            Iterable<Attachment> iterable = versionable.getAttachments();
            return Lists.newArrayList(iterable);
        }

        final long[] attachmentIDs = getAttachmentIDsByParent(resource);

        if (attachmentIDs == null || attachmentIDs.length == 0) {
            return Collections.emptyList();
        }

        return Lists.transform(Longs.asList(attachmentIDs), new Function<Long, Attachment>() {
            public Attachment apply(Long attachmentID) {
                try {
                    return getAttachment(attachmentID, resource);
                }
                catch (AttachmentNotFoundException e) {
                    log.error(e.getMessage(), e);
                    return null;
                }
            }
        });
    }

    protected long[] getAttachmentIDsByParent(AttachmentContentResource resource) {
        if (resource.getID() < 0) {
            return new long[0];
        }

        EntityDescriptor descriptor = new EntityDescriptor(resource);
        long[] ids = attachmentListCache.get(descriptor);

        if (ids == null) {
            try {
                List<AttachmentBean> list = new ArrayList<AttachmentBean>();

                if (!isTempDocument(resource)) {
                    if (resource instanceof VersionableAttachmentContentResource) {
                        // new method to see if resource is instance of jiveproxy.
                        // if it is, get unproxied object and cast it to the versionable
                        VersionableAttachmentContentResource versionable = getVersionable(resource);
                        Iterable<Attachment> iterable = versionable.getAttachments();

                        for (Attachment attachment : iterable) {
                            AttachmentBean bean = new AttachmentBean();
                            bean.setID(attachment.getID());
                            list.add(bean);
                        }
                    }
                    else {
                        list = attachmentDAO.getByObjectTypeAndObjectID(resource.getObjectType(), resource.getID());
                    }
                }
                else {
                    list = attachmentDAO.getByDocumentID(((Document) resource).getDocumentID());
                }

                ids = Longs.toArray(Lists.transform(list, new Function<AttachmentBean, Long>() {
                    public Long apply(AttachmentBean bean) {
                        return bean.getID();
                    }
                }));

                if (!isTempDocument(resource)) {
                    attachmentListCache.put(descriptor, ids);
                }
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new JiveRuntimeException(e);
            }
        }

        return ids;
    }

    private VersionableAttachmentContentResource getVersionable(AttachmentContentResource resource) {
        VersionableAttachmentContentResource versionable;
        if (resource instanceof JiveProxy) {
            JiveProxy proxy = (JiveProxy) resource;
            Object unproxiedResource = proxy.getUnproxiedObject();
            versionable = (VersionableAttachmentContentResource) unproxiedResource;
        }
        else {
            versionable = (VersionableAttachmentContentResource) resource;
        }
        return versionable;
    }

    private boolean isTempDocument(AttachmentContentResource resource) {
        if (resource.getObjectType() == JiveConstants.DOCUMENT) {
            Document document = (Document) resource;

            return document.getID() < 0 &&
                    document.getDocumentID()
                            .startsWith(JiveGlobals.getJiveProperty("jive.temporaryDocPrefix", "tempDOC-"));
        }

        return false;
    }

    /**
     * This is a method used by internal jive classes, and not exposed in the public AttachmentManager API
     *
     * @param tempDocID   document id for the temporary document
     * @param destination document to which the attachements are being moved to.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void moveTempDocumentAttachements(String tempDocID, Document destination) {
        List<AttachmentBean> beans = attachmentDAO.getByDocumentID(tempDocID);
        for (AttachmentBean bean : beans) {
            DbAttachment attachment = new DbAttachment(bean);
            attachment.getBean().setDocumentID(destination.getDocumentID());
            attachment.setObjectID(destination.getID());
            attachment.setAssociatedAttachmentContentResource(null);
        }
    }

    /**
     * Returns a Map for the list of content types to image names. The
     * JiveServlet class uses this method.
     *
     * @param contentType the content type
     * @return the thumbnail image name
     */
    public static String getThumbnailImage(String contentType) {
        // Protect against nulls
        if (contentType == null) {
            return "attachment.gif";
        }
        // Microsoft Office formats
        if (contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        {
            return "msword.gif";
        }
        else if (contentType.equals("application/vnd.ms-excel")
                || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        {
            return "msexcel.gif";
        }
        else if (contentType.equals("application/vnd.ms-powerpoint")
                || contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
        {
            return "mspowerpoint.gif";
        }
        else if (contentType.equals("application/pdf")) {
            return "pdf.gif";
        }
        // Compressed files
        else if (contentType.startsWith("application/") && contentType.indexOf("zip") > 0) {
            return "zip.gif";
        }
        // Images
        else if (contentType.startsWith("image/")) {
            return "image.gif";
        }
        // Audio Files
        else if (contentType.startsWith("audio/")) {
            return "audio.gif";
        }
        // Text content types
        else if (contentType.equals("text/richtext") || contentType.equals("text/rtf")) {
            return "rtf.gif";
        }
        else if (contentType.startsWith("text/")) {
            return "txt.gif";
        }
        // Default to generic attachment icon
        return "attachment.gif";
    }

    public void setAttachmentManagerDAO(AttachmentManagerDAO attachmentManagerDAO) {
        this.attachmentManagerDAO = attachmentManagerDAO;
    }

    public void setDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }


    public void setObjectTypeManager(ObjectTypeManager objectTypeManager) {
        this.objectTypeManager = objectTypeManager;
    }

    protected void checkAttachmentLimit(AttachmentContentResource resource, String name) throws AttachmentException {
        long[] attachIDs = getAttachmentIDsByParent(resource);

        // See if we're allowed to add another attachment.
        AttachmentLimitableType attachmentLimitableType = getAttachmentLimitableType(resource);
        if (attachmentLimitableType != null && attachIDs.length >= attachmentLimitableType
                .getAttachmentLimitInfoProvider().getMaxAttachmentsPerObject())
        {
            throw new AttachmentException(AttachmentException.TOO_MANY_ATTACHMENTS, name);
        }
        else if (attachmentLimitableType == null && attachIDs.length >= DEFAULT_MAX_ATTACHMENTS_PER_OBJECT) {
            throw new AttachmentException(AttachmentException.TOO_MANY_ATTACHMENTS, name);
        }
    }

    private AttachmentLimitableType getAttachmentLimitableType(JiveObject contentObject) {
        if (contentObject == null) {
            return null;
        }
        JiveObjectType type = objectTypeManager.getObjectType(contentObject.getObjectType());
        if (type instanceof AttachmentLimitableType) {
            return (AttachmentLimitableType) type;
        }
        return null;
    }

    private AttachmentManagerBean getAttachmentManagerBean() {
        AttachmentManagerBean bean = null;

        if (docTypeID != -1) {
            try {
                bean = attachmentManagerDAO.getByDocTypeID(docTypeID);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DAOException(e);
            }
        }

        if (bean == null) {
            bean = getGlobalAttachmentManagerBean();
        }

        return bean;
    }

    private AttachmentManagerBean getGlobalAttachmentManagerBean() {
        return new GlobalAttachmentManagerBean();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private AttachmentManagerBean createAttachmentManagerBean() {
        AttachmentManagerBean bean = new AttachmentManagerBean();
        AttachmentManagerBean defaultBean;

        try {
            bean.setTypeID(docTypeID);
            defaultBean = getGlobalAttachmentManagerBean();
            bean.setAttachmentsEnabled(defaultBean.isAttachmentsEnabled());
            bean.setAllowAllByDefault(defaultBean.isAllowAllByDefault());
            bean.setMaxAttachmentSize(defaultBean.getMaxAttachmentSize());
            bean.setMaxAttachmentsPerDocument(defaultBean.getMaxAttachmentsPerDocument());
            bean.setMaxAttachmentsPerMessage(defaultBean.getMaxAttachmentsPerMessage());
            bean.setMaxAttachmentsPerBlogPost(defaultBean.getMaxAttachmentsPerBlogPost());
            bean.setAllowedTypes(defaultBean.getAllowedTypes());
            bean.setDisallowedTypes(defaultBean.getDisallowedTypes());
            attachmentManagerDAO.create(bean);
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new DAOException(e);
        }

        return bean;
    }

    /**
     * Deletes all images in the image preview cache directory.
     */
    private synchronized void clearImagePreviewCache() {
        try {
            File dir = JiveHome.getAttachmentCache();
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (!file.delete()) {
                        log.warn("Unable to delete image preview file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private MimeTypeFile getMimeTypes() {
        // load the mime.types file
        try {
            InputStream is = ClassUtilsStatic.getResourceAsStream("META-INF/mime.types");
            return new MimeTypeFile(is);
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return MimeTypeFile.empty();
    }

    private static String listToString(List<String> list) {
        StringBuilder buf = new StringBuilder();
        for (String element : list) {
            buf.append(element).append(",");
        }
        return buf.toString();
    }

}
