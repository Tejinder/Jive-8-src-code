/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.database.ConnectionManager;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.event.ContentEvent;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.base.event.v2.EventSource;
import com.jivesoftware.base.proxy.ProxyUtils;
import com.jivesoftware.base.wiki.WikiContentHelper;
import com.jivesoftware.cache.Cache;
import com.jivesoftware.community.ApprovalManager;
import com.jivesoftware.community.BinaryBody;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.ContainerAwareEntityDescriptor;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentResultFilter;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.DocumentType;
import com.jivesoftware.community.DocumentVersion;
import com.jivesoftware.community.DuplicateIDException;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.InvalidLanguageException;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveInterceptor;
import com.jivesoftware.community.JiveObjectLoader;
import com.jivesoftware.community.JiveObjectType;
import com.jivesoftware.community.LanguageConfiguration;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.RejectedException;
import com.jivesoftware.community.ResultFilter;
import com.jivesoftware.community.ThreadResultFilter;
import com.jivesoftware.community.UserContainerManager;
import com.jivesoftware.community.audit.aop.Audit;
import com.jivesoftware.community.browse.BrowseIterator;
import com.jivesoftware.community.browse.BrowseManager;
import com.jivesoftware.community.browse.QueryFilterDef;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.filter.DocumentFilter;
import com.jivesoftware.community.browse.filter.ObjectTypeFilter;
import com.jivesoftware.community.browse.filter.ParentFilter;
import com.jivesoftware.community.browse.filter.ParentFilterFactory;
import com.jivesoftware.community.browse.provider.DocumentBrowseFilterProvider;
import com.jivesoftware.community.browse.sort.CreationDateSort;
import com.jivesoftware.community.browse.util.CastingIterator;
import com.jivesoftware.community.document.BinaryDocumentHelper;
import com.jivesoftware.community.event.DocumentEvent;
import com.jivesoftware.community.event.LanguageConfigurationEventListener;
import com.jivesoftware.community.impl.binarybody.BinaryBodyProvider;
import com.jivesoftware.community.impl.dao.ApprovalWorkflowBean;
import com.jivesoftware.community.impl.dao.BinaryBodyDAO;
import com.jivesoftware.community.impl.dao.DocumentBean;
import com.jivesoftware.community.impl.dao.DocumentDAO;
import com.jivesoftware.community.impl.dao.DocumentVersionBean;
import com.jivesoftware.community.impl.dao.DocumentVersionDAO;
import com.jivesoftware.community.impl.dao.VersionCommentBean;
import com.jivesoftware.community.impl.dao.VersionCommentDAO;
import com.jivesoftware.community.integration.storage.exceptions.StorageException;
import com.jivesoftware.community.internal.ExtendedDocumentManager;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.proxy.DocumentProxy;
import com.jivesoftware.community.proxy.GenericCollectionProxy;
import com.jivesoftware.community.proxy.GenericProxyFactory;
import com.jivesoftware.community.quest.QuestManager;
import com.jivesoftware.community.tagset.TagSet;
import com.jivesoftware.community.tagset.impl.DbTagSetManager;
import com.jivesoftware.community.util.DocumentCollaborationHelper;
import com.jivesoftware.community.util.DocumentPermHelper;
import com.jivesoftware.community.util.JiveContainerPermHelper;
import com.jivesoftware.community.util.collect.JiveIterators;
import com.jivesoftware.community.util.concurrent.LockUtil;
import com.jivesoftware.community.workflow.WorkflowUtils;
import com.jivesoftware.util.DateUtils;
import com.jivesoftware.util.Errors;
import com.jivesoftware.util.LongList;
import com.jivesoftware.visibility.ContentVisibilityHelper;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;

import static com.jivesoftware.community.action.ChooseContainerAction.ORIGINAL_DOCUMENT_SUBJECT_KEY;
import static com.jivesoftware.community.impl.ProxyBypassHelper.setModificationDateForContainer;
import static com.jivesoftware.community.moderation.JiveObjectModerator.Type;

public class DbDocumentManager implements ExtendedDocumentManager, LanguageConfigurationEventListener, EventSource
{

    private static final Logger log = Logger.getLogger(DbDocumentManager.class);

    private static final DocumentResultFilter DEFAULT_DOCUMENT_FILTER = DocumentResultFilter.createDefaultFilter();

    private boolean commentsEnabled = false;

    private static final String COMMENTS_ENABLED = "document.comments.enabled";

    /**
     * A cache for Document objects.
     */
    private Cache<Long, DocumentBean> documentCache;

    /**
     * A cache to map document id strings to internal document ids
     */
    private Cache<String, Long> documentIDCache;

    /**
     * A cache for document version objects
     */
    private Cache<String, DocumentVersionBean> versionCache;

    /**
     * A cache for mapping document ID -> versionID
     */
    private Cache<String, int[]> documentVersionsCache;

    /**
     * A cache of user document counts. This greatly helps for skins that show the number of entries
     * a user has posted next to each of their entries.
     */
    private Cache userDocumentCountCache;

    /**
     * A cache for document approval workflow beans
     */
    private Cache<Long, long[]> workflowBeanCache;

    /**
     * A cache for container level document approvers
     */
    private Cache<EntityDescriptor, List<Long>> containerApproverCache;

    /**
     * A cache for container aware entity descriptors
     */
    private Cache<Long, ContainerAwareEntityDescriptor> docContainerAwareEntityDescriptorCache;

    private QueryCacheManager queryCacheManager;
    private CommunityManager communityManager;
    private BrowseManager browseManager;
    private ParentFilterFactory parentFilterFactory;
    private DocumentEditManager documentEditManager;
    private UserManager userManager;
    private DocumentDAO documentDAO;
    private DocumentProvider documentProvider;
    private VersionManagerProvider versionManagerProvider;
    private JiveObjectLoader jiveObjectLoader;
    private ApprovalManager approvalManager;
    private BinaryBodyDAO binaryBodyDAO;
    private BinaryBodyProvider binaryBodyProvider;
    private DocumentVersionDAO documentVersionDAO;
    private VersionCommentDAO versionCommentDAO;
    private EventDispatcher eventDispatcher;
    private ContentVisibilityHelper contentVisibilityHelper;
    private BinaryDocumentHelper binaryDocumentHelper;
    private WorkflowUtils workflowUtils;
    private UserContainerManager userContainerManager;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void init() {
        LanguageConfiguration.addEventListeners(this);
        this.commentsEnabled = JiveGlobals.getJiveBooleanProperty(COMMENTS_ENABLED, true);
    }

    @Override
    public void destroy() {
        LanguageConfiguration.deleteEventListeners(this);
    }

    public Document createDocument(User user, DocumentType documentType, String documentID,
                                       String title, String body, JiveContainer container)
            throws DuplicateIDException
    {
        if (documentType == null) {
            throw new IllegalArgumentException("A document type is required to create a document");
        }

        return documentProvider.getDocument(user,
                documentType,
                documentID,
                title,
                WikiContentHelper.unknownContentToJiveDoc(body),
                getContainerImpl(container)); // CS-7755 -- set container to a concrete implementation - we may have been passed in a proxy
    }

    public Document createDocument(User user, DocumentType documentType, String documentID,
                                       String title, org.w3c.dom.Document body, JiveContainer container)
            throws DuplicateIDException
    {
        if (documentType == null) {
            throw new IllegalArgumentException("A document type is required to create a document");
        }

        return documentProvider.getDocument(user,
                documentType,
                documentID,
                title,
                body,
                getContainerImpl(container)); // CS-7755 -- set container to a concrete implementation - we may have been passed in a proxy
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private long[] getUserApprovalDocumentIDs(long userID) {
        long[] docIDs;
        synchronized(getWorkflowBeanCacheLock(userID)) {
            docIDs = workflowBeanCache.get(userID);
            if (docIDs == null) {
                try {
                    List<ApprovalWorkflowBean> beans = approvalManager.getUnApprovedWorkflowBeans(userID, JiveObjectType.Document, Type.APPROVAL);
                    docIDs = new long[beans.size()];
                    for (int i = 0; i<beans.size(); i++) {
                        docIDs[i] = beans.get(i).getObjID();
                    }
                    workflowBeanCache.put(userID, docIDs);
                }
                catch (DAOException e) {
                    log.error(e.getMessage(), e);
                    return new long[0];
                }
            }
        }
        return docIDs;
    }

    public Iterable<Document> getUserApprovalDocuments(User user) {
        return new DatabaseObjectIterable<Document>(JiveConstants.DOCUMENT,
                getUserApprovalDocumentIDs(user.getID()));
    }

    public int getUserApprovalDocumentCount(User user) {
        return getUserApprovalDocumentIDs(user.getID()).length;
    }

    public Iterable<Document> getUserDocuments(User user, DocumentState[] states) {
        Set<BrowseFilter> browseFilterSet = new HashSet<BrowseFilter>();
        browseFilterSet.add(new ObjectTypeFilter(JiveConstants.DOCUMENT));
        DocumentFilter documentFilter = new DocumentFilter("all", DocumentBrowseFilterProvider.FILTER_ALL);
        documentFilter.setExpirationDateRangeMin(DateUtils.yesterday());
        for (DocumentState state : states) {
            documentFilter.addDocumentState(state);
        }
        documentFilter.setIncludeAuthorsInUserFilter(true);
        documentFilter.setUserID(user.getID());
        browseFilterSet.add(documentFilter);

        BrowseIterator<JiveContentObject> content = browseManager.getContent(browseFilterSet, new CreationDateSort(), 0, Integer.MAX_VALUE);
        Iterator<Document> documentsIter = new CastingIterator<Document>(content);
        List<Document> docs = new ArrayList<Document>();
        while (documentsIter.hasNext()) {
            Document document = documentsIter.next();
            docs.add(document);
        }
        return docs;

    }

    public int getUserDocumentCount(User user, DocumentState[] states) {
        Set<BrowseFilter> browseFilterSet = new HashSet<BrowseFilter>();
        browseFilterSet.add(new ObjectTypeFilter(JiveConstants.DOCUMENT));
        DocumentFilter documentFilter = new DocumentFilter("all", DocumentBrowseFilterProvider.FILTER_ALL);
        documentFilter.setExpirationDateRangeMin(DateUtils.yesterday());
        for (DocumentState state : states) {
            documentFilter.addDocumentState(state);
        }
        documentFilter.setIncludeAuthorsInUserFilter(true);
        documentFilter.setUserID(user.getID());
        browseFilterSet.add(documentFilter);

        ParentFilter parentFilter = parentFilterFactory.getFilterInstance();
        parentFilter.setRecursive(true);
        browseFilterSet.add(parentFilter.getBoundInstance(getCommunityManager().getRootCommunity()));
        int communityDocCount = browseManager.getObjectCount(browseFilterSet, QueryFilterDef.Archetype.Content);

        browseFilterSet.clear();
        browseFilterSet.add(parentFilter.getBoundInstance(userContainerManager.getUserContainer(user)));
        int userContainerDocCount = browseManager.getObjectCount(browseFilterSet, QueryFilterDef.Archetype.Content);
        return communityDocCount + userContainerDocCount;
    }

    public int getDocumentsUnderEditCount() {
        return documentEditManager.getDocumentsUnderEditCount();
    }

    public Iterable<Document> getDocumentsUnderEdit() {
        return documentEditManager.getDocumentsUnderEdit();
    }

    @Override
    public Document getDocumentByExStorageFileID(long exStorageFileID) throws DocumentObjectNotFoundException {
        if (exStorageFileID < 1) {
            throwDocumentNotFoundByExFileID(exStorageFileID);
        }

        DocumentResultFilter resultFilter = DocumentResultFilter.createDefaultFilter();
        CachedPreparedStatement statement = documentDAO.getByExStorageFileIDSQL(exStorageFileID);
        Iterable<Document> documents = queryCacheManager.getList(statement, resultFilter.getStartIndex(), resultFilter.getEndIndex(), JiveConstants.DOCUMENT, JiveConstants.SYSTEM, -1);

        Iterator<Document> documentsIterator = documents.iterator();

        if (!documentsIterator.hasNext()) {
            throwDocumentNotFoundByExFileID(exStorageFileID);
        }

        Document document = documentsIterator.next();

        if (document == null) {
            throwDocumentNotFoundByExFileID(exStorageFileID);
        }

        return document;
    }

    private void throwDocumentNotFoundByExFileID(long exStorageFileID) throws DocumentObjectNotFoundException {
        throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1,
                "A document with ExStorageFileID=" + exStorageFileID + " could not be loaded from the database.");
    }

    @Audit(auditor = DbDocumentManagerAuditor.class)
    public void moveDocument(final Document document, JiveContainer destination) {
        moveDocument(document, destination, (Date)null);
    }

    @Audit(auditor = DbDocumentManagerAuditor.class)
    public void moveDocument(final Document document, JiveContainer destination, Date updated) {
        moveDocumentInternal(document, destination, Maps.<String, Object>newHashMap(), updated);
    }

    @Audit(auditor = DbDocumentManagerAuditor.class)
    public void moveDocument(final Document document, JiveContainer destination, Map<String, Object> parameters) {
        moveDocument(document, destination, parameters, null);
    }
    @Audit(auditor = DbDocumentManagerAuditor.class)
    public void moveDocument(final Document document, JiveContainer destination, Map<String, Object> parameters, Date updated) {
        moveDocumentInternal(document, destination, parameters, updated);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void moveDocumentInternal(final Document document, JiveContainer destination,
            Map<String, Object> parameters, Date updated)
    {
        Errors.failIfNull(destination, "Destination container cannot be null");
        Errors.failIfNull(document, "Document cannot be null");

        JiveContainer from = getContainerImpl(document.getJiveContainer());
        JiveContainer to = getContainerImpl(destination);
        DbDocument dbDoc = getDbDocument(document);

        Errors.failIfNull(to, "Could not obtain an implementation for " + destination.toString());
        Errors.failIfNull(from, "Could not obtain an implementation for " + document.getJiveContainer().toString());
        Errors.failIfNull(dbDoc, "Could not obtain a DbDocument");
        Errors.failIf(!dbDoc.isTextBody() && binaryDocumentHelper.isBinaryDocumentMoveDisabled(destination), "Destination container doesn't allow moving binary-documents.");

        // When there are document versions, the first move causes the versioned document's container to be changed, resulting in subsequent document versions to already have the correct 'to' container...
        // By continuing, instead of failing, we allow for a document moved fire event sent for each document version...because the caller, however, is returning to us duplicate documents (each with the highest version)
        // those events will have the wrong version information...
        if (areContainersSame(to, from)) {
            log.debug(String.format("Current %s and destination %s containers for document %s / version %s are already the same. No need to move...", from.getID(), to.getID(), document.getID(), document.getVersionID()));
            return;
        }

        try {
            removeUnauthorizedCollaborators(dbDoc, to); // causes a save, so do this before trying to move the document
            assignUniqueDocumentTitle(dbDoc, to);
            updateUserContainerPerms(dbDoc, from, to);

            final long latestVersionAuthorID = document.getVersionManager().getNewestDocumentVersion().getAuthorID();

            //overriding save functionality - the document.save sets versioning information and in a
            // move document, we do not want to update the version information. Only the container should change
            DocSaveTemplate saveWithoutVersioning = new DocSaveTemplate() {
                public void saveDocument() throws DAOException{
                // unproxy to the target document allowing for custom/templated save to possibly operate on
                Document targetDocument = ProxyUtils.unProxy(document);

                // special case: only save DbDocuments this way when moving.. other Documents need to be saved using save method
                if (targetDocument instanceof DbDocument) {
                    DocumentBean documentBean = ((DbDocument)targetDocument).getBean();

                    //maintain latest version author id and set update as silent so that modification date of
                    //version is not changed
                    documentBean.setCurrentUserID(latestVersionAuthorID);
                    documentBean.setSilentUpdate(true);

                    //updates the database with the latest container
                    documentDAO.update(documentBean);

                    //sets the bean into an already saved state
                    documentBean.beanSaved();

                    // CS-22202: Document Count Not Incremented When Document Moved To Space
                    clearQueryCache(targetDocument.getJiveContainer());

                    return;
                }

                //defaults to normal save for document types that are not DbDocuments
                document.save();
                }
            };
            dbDoc.setJiveContainerObject(to);
            addDocument(document, null, Collections.emptyMap(), false, saveWithoutVersioning, updated);
            clearCaches(document);
            updateFromContainerAfterMovingDocument(from, dbDoc, updated);
            fireDocumentMovedEvent(from, to, dbDoc, parameters);
        }
        catch (UnauthorizedException | StorageException e) {
            throw e;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DAOException(e);
        }
    }

    public void clearCaches(Document document) {
        // remove doc from caches
        synchronized(getDocumentCacheLock(document.getID())) {
            documentCache.remove(document.getID());
            documentIDCache.remove(document.getDocumentID());
            docContainerAwareEntityDescriptorCache.remove(document.getID());
        }
        removeVersionsFromCache(document.getID());
        DocumentVersion newest = versionManagerProvider.getVersionManager(document.getID()).getNewestDocumentVersion();
        for (int i=1; i <=newest.getVersionNumber(); i++) {
            removeFromVersionCache(document.getID(), i);
        }

        //CS-655: Document create links were cached with old community id. Expiring cache will
        //force a new render and update the links with new community id.
        //Note: Caching activities seems way too intrusive.
        DbDocument.clearFilteredCacheValues(document);
    }

    private void fireDocumentMovedEvent(JiveContainer from, JiveContainer to, Document document,
            Map<String, Object> params)
    {
        log.debug("Moving document, firing DOCUMENT_MOVED event");

        if (params == null) {
            params = new HashMap<>();
        }
        params.put(DocumentEvent.PARAM_ORIGINAL_CONTAINER, new EntityDescriptor(from));
        params.put(DocumentEvent.PARAM_DESTINATION_CONTAINER, new EntityDescriptor(to));
        final DocumentEvent event = new DocumentEvent(DocumentEvent.Type.MOVED, document, to, params);
        event.setOldContainer(new EntityDescriptor(from));
        eventDispatcher.fire(event);
    }

    private JiveContainer getContainerImpl(JiveContainer container) {
        try {
            return jiveObjectLoader.getJiveContainer(container.getObjectType(), container.getID());
        }
        catch (NotFoundException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private DbDocument getDbDocument(Document d) {
        DbDocument dbDocument = null;
        if (d instanceof DbDocument) {
            dbDocument = (DbDocument) d;
        }
        else if (d instanceof DocumentProxy) {
            Document proxied = ((DocumentProxy)d).getProxiedDocument();
            return getDbDocument(proxied);
        }

        return dbDocument;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    private void assignUniqueDocumentTitle(Document document, JiveContainer container) {
        try {
            String subject = document.getUnfilteredSubject();
            DocumentBean existingDoc = documentDAO.getByDocumentTitle(container.getObjectType(), container.getID(), subject);
            if (existingDoc != null) {
                log.debug("document title is not unique in target container");
                int counter = 1;
                String newSubject = subject + '[' + counter + ']';
                while (existingDoc != null) {
                    newSubject = subject + '[' + counter + ']';
                    existingDoc = documentDAO.getByDocumentTitle(container.getObjectType(), container.getID(), newSubject);
                    counter++;
                }
                log.debug("setting document title to '" + newSubject + "'");
                document.setSubject(newSubject);
            }
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
        }
        catch (DocumentAlreadyExistsException e) {
            // very unlikely - would have to be a very fast race condition to occur
            log.error(e.getMessage(), e);
        }
    }

    private boolean areContainersSame(JiveContainer to, JiveContainer from) {
        return to.getObjectType() == from.getObjectType() && to.getID() == from.getID();
    }

    @Audit(auditor = DbDocumentManagerAuditor.class)
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public synchronized void deleteDocument(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        DbDocument dbDocument;
        JiveContainer container;

        // get the underlying DbDocument object
        try {
            if (document instanceof DbDocument) {
                dbDocument = (DbDocument) document;
            }
            else {
                dbDocument = getDocument(document.getID());
            }

            container = dbDocument.getJiveContainer();
        }
        catch (DocumentObjectNotFoundException e) {
            log.error(e.getMessage(), e);
            return;
        }

        fireDocumentDeletingEvent(document, container);

        try {
            // deletion of all comments, ratings, tags, read stats, search index entries/cached text
            // triggered by the above event firing

            // Expire cache for this container
            clearQueryCache(container);

            synchronized(getDocumentCacheLock(document.getID())) {
                documentCache.remove(document.getID());
            }
            removeVersionsFromCache(document.getID());
            List<DocumentVersion> v = versionManagerProvider.getVersionManager(document.getID()).getDocumentVersions();
            for (DocumentVersion version : v) {
                removeFromVersionCache(document.getID(), version.getVersionNumber());
            }
            DbDocument.clearFilteredCacheValues(dbDocument);
            queryCacheManager.removeQueriesForObject(JiveConstants.DOCUMENT, document.getID());

            DocumentBean bean = dbDocument.getBean();
            bean.setVersionID(-1);
            documentDAO.delete(bean);

        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new DAOException(e);
        }

        fireDocumentDeletedEvent(document, container);
    }

    private void fireDocumentDeletingEvent(Document document, JiveContainer container) {
        DocumentEvent event = new DocumentEvent(DocumentEvent.Type.DELETING, document, container,
                Collections.<String, Object>emptyMap());
        eventDispatcher.fireInline(event);
    }

    private void fireDocumentDeletedEvent(Document document, JiveContainer container) {
        Map<String, Object> params = new HashMap<>();
        params.put(DocumentEvent.PARAM_ORIGINAL_AUTHOR, new EntityDescriptor(document.getUser()));
        DocumentEvent event = new DocumentEvent(DocumentEvent.Type.DELETED, document, container, params);
        eventDispatcher.fire(event);
    }

    public void clearQueryCache(JiveContainer container) {
        queryCacheManager.removeContainerQueries(container);
    }

    public boolean isCommentsEnabled() {
        return commentsEnabled;
    }

    public void setCommentsEnabled(boolean commentsEnabled) throws UnauthorizedException {
        this.commentsEnabled = commentsEnabled;
        JiveGlobals.setJiveProperty(COMMENTS_ENABLED, String.valueOf(commentsEnabled));
    }

    @Override
    public void allowedLanguageDeleted(String language) {
        // update all the documents with this language specified and set their
        // language to the default system language
        DocumentResultFilter ignoreStateFilter = DocumentResultFilter.createDefaultFilter();
        ignoreStateFilter.addDocumentState(DocumentState.INCOMPLETE);
        ignoreStateFilter.addDocumentState(DocumentState.DELETED);
        ignoreStateFilter.addDocumentState(DocumentState.PENDING_APPROVAL);
        ignoreStateFilter.addDocumentState(DocumentState.EXPIRED);
        ignoreStateFilter.addLanguage(language);
        for (Document document : getDocumentVersions(getCommunityManager().getRootCommunity(), ignoreStateFilter)) {
            try {
                DbDocument dbDocument = getDocument(document.getID());
                dbDocument.setLanguage(LanguageConfiguration.getDefaultLanguage());
            }
            catch (DocumentObjectNotFoundException e) {
                log.error(e.getMessage(), e);
            }
            catch (InvalidLanguageException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public DbDocument getDocument(long docID) throws DocumentObjectNotFoundException {
        if (docID < 1) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, docID);
        }

        DocumentBean bean = getDocumentBean(docID);
        if (bean == null) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1,
                    "Document " + docID + " could not be loaded from the database.");
        }

        return documentProvider.getDocument(bean);
    }

    @Override
    public List<Document> getDocuments(List<Long> docIDs) {
        Set<Long> uniqueIDs = Sets.newHashSet(docIDs);
        Map<Long, DocumentBean> cachedDocumentBeans = documentCache.getAll(uniqueIDs);

        List<Document> docs = Lists.newLinkedList();

        // Anything we didn't find in cache, load up from the DAO
        Set<Long> needToLoad;

        if (cachedDocumentBeans == null) {
            log.info("documentCache.getAll unexpectedly returned null. Expected an empty map if there are no results. " + documentCache.getClass().getName());
            needToLoad = uniqueIDs;
        }
        else {
            needToLoad = Sets.difference(uniqueIDs, cachedDocumentBeans.keySet());
        }

        if (!needToLoad.isEmpty()) {
            List<DocumentBean> beans = documentDAO.getByIDs(needToLoad);
            for (DocumentBean bean : beans) {
                // cache the document because loading the bean by ID will give us the latest published version or,
                // if no published version exists, the latest version.
                documentCache.put(bean.getID(), bean);
                documentIDCache.put(bean.getDocumentID(), bean.getID());
                cachedDocumentBeans.put(bean.getID(), bean);
            }
        }

        for (Long id : docIDs) {
            DocumentBean bean = cachedDocumentBeans.get(id);
            if (bean != null) {
                docs.add(documentProvider.getDocument(bean.createDeepCopy()));
            }
        }
        return docs;
    }

    @Override
    public List<ContainerAwareEntityDescriptor> getDocumentsAsContainerAwareEntityDescriptors(List<Long> docIDs) {
        Set<Long> uniqueIDs = Sets.newHashSet(docIDs);
        Map<Long, ContainerAwareEntityDescriptor> cached = docContainerAwareEntityDescriptorCache.getAll(uniqueIDs);

        List<ContainerAwareEntityDescriptor> docCAEDs = Lists.newLinkedList();

        // Anything we didn't find in cache, load up from the DAO
        Set<Long> needToLoad = Sets.difference(uniqueIDs, cached.keySet());
        if (!needToLoad.isEmpty()) {
            List<ContainerAwareEntityDescriptor> beans = documentDAO.getContainerAwareEntityDescriptorsByIDs(needToLoad);
            for (ContainerAwareEntityDescriptor bean : beans) {
                docContainerAwareEntityDescriptorCache.put(bean.getID(), bean);
                cached.put(bean.getID(), bean);
            }
        }

        for (Long id : docIDs) {
            ContainerAwareEntityDescriptor entityDescriptor = cached.get(id);
            if (entityDescriptor != null) {
                docCAEDs.add(new ContainerAwareEntityDescriptor(entityDescriptor));
            }
        }

        return docCAEDs;

    }

    public DbDocument getDocument(long docID, int version) throws DocumentObjectNotFoundException {
        if (docID < 1) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, docID);
        }

        return documentProvider.getDocument(getDocumentBean(docID, version));
    }

    public DbDocument getDocument(String documentID) throws DocumentObjectNotFoundException {
        if (documentID == null || "".equals(documentID.trim())) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1);
        }

        DocumentBean bean = getDocumentBean(documentID);
        if (bean == null) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1,
                    "Document " + documentID + " could not be loaded from the database.");
        }

        return documentProvider.getDocument(bean);
    }

    public DbDocument getDocument(String documentID, int version) throws DocumentObjectNotFoundException {
        if (documentID == null || "".equals(documentID.trim())) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1);
        }

        return documentProvider.getDocument(getDocumentBean(documentID, version));
    }

    public DbDocument getDocument(JiveContainer container, String subject)
            throws DocumentObjectNotFoundException, UnauthorizedException
    {
        if (container == null || subject == null || "".equals(subject.trim())) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1);
        }

        return documentProvider.getDocument(getDocumentBean(container, subject));
    }

    private static Object getDocumentCacheLock(long docID) {
        return LockUtil.intern("document-" + docID);
    }

    public DocumentBean getDocumentBean(long docID) throws DocumentObjectNotFoundException {
        DocumentBean bean = documentCache.get(docID);
        if (bean == null) {
            bean = loadBeanById(docID);
            // cache because loading the bean by ID will give us the latest published version or, if no published version
            // exists, the latest version.
            documentCache.put(docID, bean);
            documentIDCache.put(bean.getDocumentID(), docID);
        }

        return bean.createDeepCopy();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected DocumentBean loadBeanById(long docID) throws DocumentObjectNotFoundException {
        DocumentBean bean;
        try {
            bean = documentDAO.getByID(docID);
            if (bean == null) {
                throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1,
                        "Document " + docID + " could not be loaded from the database.");
            }
        }
        catch (DAOException e) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1, "", e);
        }
        return bean;
    }

    public DocumentBean getDocumentBean(long docID, int version) throws DocumentObjectNotFoundException {
        DocumentBean bean;
        try{
            // most calls are looking for the most recent version of the doc, which should be in the cache
            bean = findInLocalCache(docID, version);
            if (bean != null) {
                return bean.createDeepCopy();
            }
            bean = loadBeanByIdAndVersion(docID, version);
            // only put the document into the cache if the document is the latest published version
            // This mimics the semantics of calling the document DAO without a specific version
            DocumentVersion latestVersion = versionManagerProvider.getVersionManager(docID).getPublishedDocumentVersion();
            if (latestVersion != null && latestVersion.getVersionNumber() == version) {
                if (DocumentState.PUBLISHED.getState().equalsIgnoreCase(bean.getDocumentState())) {
                    documentCache.put(docID, bean);
                }
                documentIDCache.put(bean.getDocumentID(), docID);
            }
        }
        catch (DAOException e) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1, "", e);
        }

        return bean.createDeepCopy();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected DocumentBean loadBeanByIdAndVersion(long docID, int version) throws DocumentObjectNotFoundException {
        DocumentBean bean;
        bean = documentDAO.getByID(docID, version);
        if (bean == null) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1,
                    "Document " + docID + " could not be loaded from the database.");
        }
        return bean;
    }

    // the documentCache contains a DocumentBean with the version contents (e.g., subject, body) of either
    // (1) the latest published version of the doc OR
    // (2) if no published version exists, the most recent version in any state (only documents which have never had a
    //     version published because (examples) they're in draft (incomplete) or have their first awaiting moderation)
    private DocumentBean findInLocalCache(long docID, int version) {
        DocumentBean bean = documentCache.get(docID);
        if (bean != null && bean.getVersionID() == version) {
            return bean;
        }
        return null;
    }

    public DocumentBean getDocumentBean(String documentID) throws DocumentObjectNotFoundException {
        Long docID = documentIDCache.get(documentID);
        if (docID == null) {
            DocumentBean bean;
            bean = loadBeanByDocumentId(documentID);

            documentIDCache.put(bean.getDocumentID(), bean.getID());
            docID = bean.getID();
        }
        return getDocumentBean(docID);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected DocumentBean loadBeanByDocumentId(String documentID) throws DocumentObjectNotFoundException {
        DocumentBean bean;
        try {
            bean = documentDAO.getByDocumentID(documentID);
            if (bean == null) {
                throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1,
                        "Document with title \"" + documentID + "\" could not be loaded from the database.");
            }
        }
        catch (DAOException e) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1, "", e);
        }
        return bean;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public DocumentBean getDocumentBean(String documentID, int version) throws DocumentObjectNotFoundException {
        DocumentBean bean;
        try {
            bean = documentDAO.getByDocumentID(documentID, version);
        }
        catch (DAOException e) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1, "", e);
        }

        return bean.createDeepCopy();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public DocumentBean getDocumentBean(JiveContainer container, String subject)
            throws DocumentObjectNotFoundException
    {
        DocumentBean bean;
        try {
            bean = documentDAO.getByDocumentTitle(container.getObjectType(), container.getID(), subject);
        }
        catch (DAOException e) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1, "", e);
        }

        if (bean != null) {
            return bean.createDeepCopy();
        }

        throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT, -1,
                "Document with subject '" + subject + "' could not be found in container " +
                        container.getObjectType() + "," + container.getID());
    }

    public BinaryBody getBinaryBody(long bodyID) throws DocumentObjectNotFoundException {
        return binaryBodyProvider.getBinaryBody(bodyID);
    }

    @Override
    public DocumentVersion getDocumentVersion(long docID, int versionID) throws DocumentObjectNotFoundException {
        String key = getVersionCacheKey(docID, versionID); // already interned
        DocumentVersionBean bean = versionCache.get(key);
        if (bean == null) {
            synchronized (LockUtil.intern(key)) {
                bean = getDocumentVersionBean(docID, versionID);
                if (bean == null) {
                    throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT_VERSION, versionID,
                            "Document " + docID + " could not load version " + versionID + " from the database.");
                }
                versionCache.put(key, bean);
            }
        }

        return new DbDocumentVersion(bean);
    }

    @Override
    public DocumentVersion getDocumentVersion(long ID) throws DocumentObjectNotFoundException
    {
        String key = getVersionCacheKey(ID);
        DocumentVersionBean bean = versionCache.get(key);
        if (bean == null) {
            bean = loadDocumentVersionById(ID);
            versionCache.put(key, bean);
        }

        return new DbDocumentVersion(bean);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected DocumentVersionBean loadDocumentVersionById(long ID) throws DocumentObjectNotFoundException {
        DocumentVersionBean bean;
        try {
            bean = documentVersionDAO.getByID(ID);

            if (bean == null) {
                String msg = "Unable to find version " + ID;
                throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT_VERSION, ID, msg);
            }
        }
        catch (DAOException e) {
            String msg = "Unable to find version " + ID;
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT_VERSION, ID, msg, e);
        }
        return bean;
    }

    @Override
    public List<DocumentVersion> getDocumentVersions(long docID) throws DocumentObjectNotFoundException {
        String key = getVersionListCacheKey(docID);
        int[] versionIDs = documentVersionsCache.get(key);
        if (versionIDs == null) {
            try {
                List<DocumentVersionBean> versionBeans = loadDocumentVersionsById(docID);
                versionIDs = new int[versionBeans.size()];
                for (int i = 0; i < versionBeans.size(); i++) {
                    versionIDs[i] = versionBeans.get(i).getVersionID();
                }
                documentVersionsCache.put(key, versionIDs);

                // now update the versionCache if necessary and return the
                // doc versions. This allows us to not call getDocVersionBean(docID, versionID)
                // for every bean that we just retrieved above as we have to do otherwise
                // below, thus saving having to reexecute a bunch of sql to retrieve the
                // version beans we already have

                List<DocumentVersion> docVersions = new ArrayList<DocumentVersion>(versionIDs.length);
                for (DocumentVersionBean versionBean : versionBeans) {
                    String versionCacheKey = getVersionCacheKey(docID, versionBean.getVersionID());
                    if (versionCache.get(versionCacheKey) == null) {
                        versionCache.put(versionCacheKey, versionBean);
                    }
                    docVersions.add(new DbDocumentVersion(versionBean));
                }

                return docVersions;
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT_VERSION, -1,
                        "Document " + docID + " could not load versions from the database.");
            }
        }

        List<DocumentVersion> v = new ArrayList<DocumentVersion>(versionIDs.length);
        for (int versionID : versionIDs) {
            v.add(getDocumentVersion(docID, versionID));
        }

        return v;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected List<DocumentVersionBean> loadDocumentVersionsById(long docID) throws DocumentObjectNotFoundException {
        List<DocumentVersionBean> versionBeans = documentVersionDAO.getVersionsByDocID(docID);
        if (versionBeans == null) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT_VERSION, -1,
                    "Unable to load versions for document " + docID + " from the database.");
        }
        return versionBeans;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    protected DocumentVersionBean getDocumentVersionBean(long docID, int versionID)
            throws DocumentObjectNotFoundException
    {
        String key = getVersionCacheKey(docID, versionID);
        DocumentVersionBean bean = versionCache.get(key);
        if (bean == null) {
            try {
                bean = documentVersionDAO.getByID(docID, versionID);
            }
            catch (DAOException e) {
                String msg = "Unable to load version " + versionID + " for document" + docID;
                throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT_VERSION, versionID, msg, e);
            }
            if (bean != null) { // null if deleted
                versionCache.put(key, bean);
            }
        }

        return bean;
    }

    public Cache<Long, DocumentBean> getDocumentCache() {
        return documentCache;
    }

    public Cache<String, Long> getDocumentIDCache() {
        return documentIDCache;
    }

    public void removeFromVersionCache(long docID, int versionID) {
        versionCache.remove(getVersionCacheKey(docID, versionID));
    }

    public Cache<String, int[]> getDocumentVersionsCache() {
        return documentVersionsCache;
    }

    public void removeVersionsFromCache(long docID) {
        documentVersionsCache.remove(getVersionListCacheKey(docID));
    }

    public Cache getUserDocumentCountCache() {
        return userDocumentCountCache;
    }

    public Cache<Long, long[]> getWorkflowBeanCache() {
        return workflowBeanCache;
    }

    public static Object getWorkflowBeanCacheLock(long userID) {
        return LockUtil.intern("workflowCacheLock-" + userID);
    }

    private CommunityManager getCommunityManager() {
        return JiveApplication.getContext().getCommunityManager();
    }

    private static String getVersionCacheKey(long docID, int versionID) {
        return "version-" + docID + "-" + versionID;
    }

    private static String getVersionCacheKey(long ID) {
        return "version-" + ID;
    }

    private static String getVersionListCacheKey(long docID) {
        return "documentVersions-" + docID;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteUserDocuments(User user) {
        try {
            deleteUserReviewedDocuments(user);
            deleteUserAuthoredDocuments(user);
            deleteUserDocumentVersions(user);
            deleteUserVersionComments(user);
            documentDAO.removeCollaboration(user);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    private void deleteUserVersionComments(User user) {
        // doc version comments (jiveDocVersionCmmt)
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        LongList commentIDs = new LongList();
        try {
            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement("SELECT vCommentID FROM jiveDocVersionCmmt WHERE userID=?");
            pstmt.setLong(1, user.getID());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                commentIDs.add(rs.getLong(1));
            }
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        finally {
            ConnectionManager.close(rs, pstmt, con);
        }

        try {
            for (int i = 0; i < commentIDs.size(); i++) {
                long commentID = commentIDs.get(i);
                VersionCommentBean bean = versionCommentDAO.getByCommentID(commentID);
                // delete from db
                versionCommentDAO.delete(bean);
                // clear cache
                removeVersionsFromCache(bean.getDocID());
            }
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    private void deleteUserReviewedDocuments(User user) {
        DocumentResultFilter resultFilter;
        CachedPreparedStatement cachedPstmt;

        // doc versions reviewed by the user
        resultFilter = DocumentResultFilter.createDefaultFilter();
        resultFilter.setUserID(user.getID());
        resultFilter.setIncludeAuthorsInUserFilter(false);
        resultFilter.setIncludeReviewersInUserFilter(true);
        resultFilter.setRecursive(true);
        resultFilter.setRestrictToLatestVersion(false);
        resultFilter.deleteDocumentState(DocumentState.PUBLISHED);
        for (DocumentState state : DocumentState.values()) {
            resultFilter.addDocumentState(state);
        }
        cachedPstmt = documentVersionDAO.getDocumentVersionListSQL(resultFilter, false);
        Iterable<Document> documents = queryCacheManager.getList(cachedPstmt, resultFilter.getStartIndex(),
                resultFilter.getEndIndex(), JiveConstants.DOCUMENT, JiveConstants.SYSTEM, -1);

        for (Document document : documents) {
            document.removeReviewer(user);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    private void deleteUserAuthoredDocuments(User user) {
        DocumentResultFilter resultFilter;
        CachedPreparedStatement cachedPstmt;

        // doc versions authored by the user
        resultFilter = DocumentResultFilter.createDefaultFilter();
        resultFilter.setUserID(user.getID());
        resultFilter.setIncludeAuthorsInUserFilter(true);
        resultFilter.setIncludeReviewersInUserFilter(false);
        resultFilter.setRecursive(true);
        resultFilter.setRestrictToLatestVersion(false);
        resultFilter.deleteDocumentState(DocumentState.PUBLISHED);
        for (DocumentState state : DocumentState.values()) {
            resultFilter.addDocumentState(state);
        }
        cachedPstmt = documentVersionDAO.getDocumentVersionListSQL(resultFilter, false);
        Iterable<Document>  documents = queryCacheManager.getList(cachedPstmt, resultFilter.getStartIndex(),
                resultFilter.getEndIndex(), JiveConstants.DOCUMENT, JiveConstants.SYSTEM, -1);

        for (Document document : documents) {
            document.removeApprover(user);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    private void deleteUserDocumentVersions(User user) throws Exception {
        // A filter to find all documents (including versions) authored by the user
        DocumentResultFilter resultFilter = DocumentResultFilter.createAllStatesFilter();
        resultFilter.setUserID(user.getID());
        resultFilter.setRecursive(true);
        resultFilter.setRestrictToLatestVersion(false);
        resultFilter.setIncludeAuthorsInUserFilter(true);
        resultFilter.setExcludeOwnerInUserFilter(false);
        resultFilter.setIncludeUnSearchableDocuments(true);

        CachedPreparedStatement cachedPstmt = documentVersionDAO.getDocumentVersionListSQL(resultFilter, false);
        Iterable<Document> documents = queryCacheManager.getList(cachedPstmt, resultFilter.getStartIndex(),
                resultFilter.getEndIndex(), JiveConstants.DOCUMENT, JiveConstants.SYSTEM, -1);


        for (Document document : documents) {
            List<DocumentVersion> versions = document.getVersionManager().getDocumentVersions();
            DocumentVersion oldestVersion = versions.get(versions.size() - 1);
            if (document.getUserID() == user.getID() && oldestVersion.getAuthorID() != user.getID()) {
                // remove deleted user from document owner
                document.setUser(oldestVersion.getAuthor());
                ((DbDocument)document).setCurrentUserID(oldestVersion.getAuthorID());
                document.setMinorEdit(true);
                document.save();
            }

            // If document has only one version, and the user is the author of that version, just delete the document.
            if (1 == document.getVersionManager().getDocumentVersionCount() &&
                    document.getVersionManager().getNewestDocumentVersion().getAuthor() != null &&
                    document.getVersionManager().getNewestDocumentVersion().getAuthor().getID() == user.getID()) {
                deleteDocument(document);
                continue;
            }

            for (DocumentVersion version : versions) {

                // If the current version was authored by the given user...
                if (version.getAuthorID() == user.getID()) {
                    // If version is archived, delete the version
                    if (DocumentState.ARCHIVED.equals(version.getDocumentState())
                            || DocumentState.EXPIRED.equals(version.getDocumentState()))
                    {
                        // If user holds the oldest version, update the owner of the document
                        if (((oldestVersion.getVersionNumber()) == version.getVersionNumber())
                                && document.getUserID() == user.getID()) {
                            DocumentVersion nextOldestVersion = versions.get(versions.size() - 2);
                            // remove deleted user from document owner
                            document.setUser(nextOldestVersion.getAuthor());
                            ((DbDocument)document).setCurrentUserID(nextOldestVersion.getAuthorID());
                            document.setMinorEdit(true);
                            document.save();
                        }
                        document.getVersionManager().deleteDocumentVersion(version.getVersionNumber());
                        removeFromVersionCache(document.getID(), version.getVersionNumber());
                    }
                    // If version is incomplete, pending approval, rejected or published,
                    // roll back to most recent version authored by someone else.
                    if (DocumentState.PUBLISHED.equals(version.getDocumentState())
                            || DocumentState.INCOMPLETE.equals(version.getDocumentState())
                            || DocumentState.PENDING_APPROVAL.equals(version.getDocumentState())
                            || DocumentState.REJECTED.equals(version.getDocumentState()))
                    {
                        // Remove any pre-existing workflow
                        ((DbDocument) version.getDocument()).removeWorkflow();
                        // Find the most recent version not authored by the user being deleted
                        final DocumentVersion mostRecent = getMostRecentVersionNotAuthoredByUser(document, user);
                        // Just delete the document since there are no other non-guest contributors
                        if (mostRecent == null) {
                            deleteDocument(document);
                            break;
                        }
                        else {
                            // We need to put the current modification data on the restored version.
                            // Otherwise, the recent content list will be corrupted.
                            final Date currentModificationDate = document.getModificationDate();

                            // need to change doc owner
                            document.setUser(mostRecent.getAuthor());
                            document.setMinorEdit(true);
                            document.save();

                            // Rollback to previous version, and delete user's version
                            DocumentVersion current = document.getVersionManager()
                                    .restoreDocumentVersion(mostRecent.getVersionNumber(),
                                            mostRecent.getAuthor().getID());

                            // Reset the modfication date.
                            document.setMinorEdit(true);
                            document.setModificationDate(currentModificationDate);
                            current.setModificationDate(currentModificationDate);

                            // Delete the version
                            document.getVersionManager().deleteDocumentVersion(version.getVersionNumber());
                            removeFromVersionCache(document.getID(), version.getVersionNumber());
                        }
                    }
                }
            }

            // finally, remove current doc from caches
            synchronized(getDocumentCacheLock(document.getID())) {
                documentCache.remove(document.getID());
                documentIDCache.remove(document.getDocumentID());
            }

            removeVersionsFromCache(document.getID());
        }
    }

    private DocumentVersion getMostRecentVersionNotAuthoredByUser(Document document, User user) {
        // Fetch the doc's versions in version-ID descending order.  Return the 1st one not authored by the user, or null if there isn't one.
        DocumentVersion version = null;
        int count = document.getVersionManager().getNewestDocumentVersion().getVersionNumber();

        for (int i = count; i >= 1; i--) {
            try {
                DocumentVersion v = document.getVersionManager().getDocumentVersion(i);
                if (v.getAuthor() != null && v.getAuthor().getID() != user.getID()) {
                    version = v;
                    break;
                }
            }
            catch (Exception e) {
                log.error(String.format("Error reading version %d for document with id %d", i, document.getID()),e);
            }
        }
        return version;
    }

    @Required
    public void setDocumentCache(Cache<Long, DocumentBean> documentCache) {
        this.documentCache = documentCache;
    }

    @Required
    public void setDocumentIDCache(Cache<String, Long> documentIDCache) {
        this.documentIDCache = documentIDCache;
    }

    @Required
    public void setVersionCache(Cache<String, DocumentVersionBean> versionCache) {
        this.versionCache = versionCache;
    }

    @Required
    public void setDocumentVersionsCache(Cache<String, int[]> documentVersionsCache) {
        this.documentVersionsCache = documentVersionsCache;
    }

    @Required
    public void setUserDocumentCountCache(Cache userDocumentCountCache) {
        this.userDocumentCountCache = userDocumentCountCache;
    }

    @Required
    public void setWorkflowBeanCache(Cache<Long, long[]> workflowBeanCache) {
        this.workflowBeanCache = workflowBeanCache;
    }

    @Required
    public void setContainerApproverCache(Cache<EntityDescriptor, List<Long>> containerApproverCache) {
        this.containerApproverCache = containerApproverCache;
    }

    @Required
    public void setDocContainerAwareEntityDescriptorCache(
            Cache<Long, ContainerAwareEntityDescriptor> docContainerAwareEntityDescriptorCache)
    {
        this.docContainerAwareEntityDescriptorCache = docContainerAwareEntityDescriptorCache;
    }

    @Required
    public void setBinaryDocumentHelper(BinaryDocumentHelper binaryDocumentHelper) {
        this.binaryDocumentHelper = binaryDocumentHelper;
    }

    @Required
    public void setQueryCacheManager(QueryCacheManager queryCacheManager) {
        this.queryCacheManager = queryCacheManager;
    }

    @Required
    public void setCommunityManager(CommunityManager communityManager) {
        this.communityManager = communityManager;
    }

    @Required
    public void setBrowseManager(BrowseManager browseManager) {
        this.browseManager = browseManager;
    }

    @Required
    public void setParentFilterFactory(ParentFilterFactory parentFilterFactory) {
        this.parentFilterFactory = parentFilterFactory;
    }

    @Required
    public void setDocumentEditManager(DocumentEditManager documentEditManager) {
        this.documentEditManager = documentEditManager;
    }

    @Required
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Required
    public void setDocumentDAO(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    @Required
    public void setDocumentProvider(DocumentProvider documentProvider) {
        this.documentProvider = documentProvider;
    }

    @Required
    public void setVersionManagerProvider(VersionManagerProvider versionManagerProvider) {
        this.versionManagerProvider = versionManagerProvider;
    }

    @Required
    public void setJiveObjectLoader(JiveObjectLoader jiveObjectLoader) {
        this.jiveObjectLoader = jiveObjectLoader;
    }

    @Required
    public void setApprovalManager(ApprovalManager approvalManager) {
        this.approvalManager = approvalManager;
    }

    @Required
    public void setBinaryBodyDAO(BinaryBodyDAO binaryBodyDAO) {
        this.binaryBodyDAO = binaryBodyDAO;
    }

    @Required
    public void setBinaryBodyProvider(BinaryBodyProvider binaryBodyProvider) {
        this.binaryBodyProvider = binaryBodyProvider;
    }

    @Required
    public void setDocumentVersionDAO(DocumentVersionDAO documentVersionDAO) {
        this.documentVersionDAO = documentVersionDAO;
    }

    @Required
    public void setVersionCommentDAO(VersionCommentDAO versionCommentDAO) {
        this.versionCommentDAO = versionCommentDAO;
    }

    @Required
    public void setWorkflowUtils(WorkflowUtils workflowUtils) {
        this.workflowUtils = workflowUtils;
    }

    @Required
    public void setUserContainerManager(UserContainerManager userContainerManager) {
        this.userContainerManager = userContainerManager;
    }

    @Override
    public void setDispatcher(EventDispatcher dispatcher) {
        this.eventDispatcher = dispatcher;
    }

    public int getDocumentVersionCount(JiveContainer container) {
        return getDocumentVersionCount(container, DEFAULT_DOCUMENT_FILTER);
    }

    public int getDocumentVersionCount(TagSet tagSet) {
        DocumentResultFilter filter = DocumentResultFilter.createDefaultFilter();
        DbTagSetManager.setTagFilters(tagSet, filter);
        return getDocumentVersionCount(tagSet, filter);
    }

    public Iterable<Document> getDocumentVersions(JiveContainer container) {
        return getDocumentVersions(container, DEFAULT_DOCUMENT_FILTER);
    }

    @Override
    public Iterable<ContainerAwareEntityDescriptor> getDocumentVersionsAsContainerAwareEntityDescriptors(JiveContainer container) {
        return getDocumentsAsContainerAwareEntityDescriptors(container, DEFAULT_DOCUMENT_FILTER);
    }

    public Iterable<Document> getDocumentVersions(DocumentResultFilter filter) {
        return getDocumentVersions(communityManager.getRootCommunity(), filter);
    }

    public Iterable<ContainerAwareEntityDescriptor> getDocumentVersionsAsContainerAwareEntityDescriptors(
            DocumentResultFilter filter) {
        return getDocumentsAsContainerAwareEntityDescriptors(communityManager.getRootCommunity(), filter);
    }

    public Iterable<Document> getDocuments(TagSet tagSet) {
        DocumentResultFilter filter = DocumentResultFilter.createDefaultFilter();
        DbTagSetManager.setTagFilters(tagSet, filter);
        return getDocumentVersions(tagSet.getJiveContainer(), filter);
    }

    @Override
    public Iterable<ContainerAwareEntityDescriptor> getDocumentsAsContainerAwareEntityDescriptors(TagSet tagSet) {
        DocumentResultFilter filter = DocumentResultFilter.createDefaultFilter();
        DbTagSetManager.setTagFilters(tagSet, filter);
        return getDocumentsAsContainerAwareEntityDescriptors(tagSet.getJiveContainer(), filter);
    }

    public int getDocumentVersionCount(JiveContainer container, DocumentResultFilter resultFilter) {
        // Note, the filter dictates if the query is recursive or not. The default behavior is non-recursive.
        // {@see ResultFilter#isRecursive} for more info.
        CachedPreparedStatement cachedPstmt = documentVersionDAO.getDocumentVersionListSQL(container, resultFilter,
                true);
        return queryCacheManager.getCount(cachedPstmt, container.getObjectType(), container.getID());
    }

    @Override
    public int getDocumentVersionCount(DocumentResultFilter resultFilter) {
        return getDocumentVersionCount(communityManager.getRootCommunity(), resultFilter);
    }

    public int getDocumentVersionCount(TagSet tagSet, DocumentResultFilter resultFilter) {
        DbTagSetManager.setTagFilters(tagSet, resultFilter);
        return getDocumentVersionCount(tagSet.getJiveContainer(), resultFilter);
    }

    public Iterable<Document> getDocumentVersions(TagSet tagSet, DocumentResultFilter resultFilter) {
        DbTagSetManager.setTagFilters(tagSet, resultFilter);
        return getDocumentVersions(tagSet.getJiveContainer(), resultFilter);
    }

    @Override
    public Iterable<ContainerAwareEntityDescriptor> getDocumentsAsContainerAwareEntityDescriptors(TagSet tagSet,
            DocumentResultFilter resultFilter) {
        DbTagSetManager.setTagFilters(tagSet, resultFilter);
        return getDocumentsAsContainerAwareEntityDescriptors(tagSet.getJiveContainer(), resultFilter);
    }

    public Iterable<Document> getDocumentVersions(JiveContainer container, DocumentResultFilter resultFilter) {
        // Note, the filter dictates if the query is recursive or not. The default behavior is non-recursive.
        // {@see ResultFilter#isRecursive} for more info.
        CachedPreparedStatement cachedPstmt = documentVersionDAO.getDocumentVersionListSQL(container, resultFilter,
                false);

        return queryCacheManager.getList(cachedPstmt, resultFilter.getStartIndex(), resultFilter.getEndIndex(),
                JiveConstants.DOCUMENT, container.getObjectType(), container.getID());
    }

    @Override
    public Iterable<ContainerAwareEntityDescriptor> getDocumentsAsContainerAwareEntityDescriptors(
            JiveContainer container, DocumentResultFilter resultFilter) {

        boolean oldRetrieveValue = resultFilter.isRetrieveContainerInfo();
        CachedPreparedStatement cachedPstmt = null;
        try {
            resultFilter.setRetrieveContainerInfo(true);
            cachedPstmt = documentVersionDAO.getDocumentVersionListSQL(container, resultFilter, false);
            if(resultFilter.isFromQuickSearchWidget())
            {
            	/*if(resultFilter.getPropertyName(0).equalsIgnoreCase("grail.period.long"))
            	{
            		String startMonth = resultFilter.getPropertyValueString(0).split("~")[0];
            		String startYear = resultFilter.getPropertyValueString(0).split("~")[1];
            		String endMonth = resultFilter.getPropertyValueString(0).split("~")[2];
            		String endYear = resultFilter.getPropertyValueString(0).split("~")[3];
            		 
            		
            		Calendar cal = Calendar.getInstance();
            		cal.set(Calendar.YEAR, Integer.parseInt(startYear.trim()));
                    cal.set(Calendar.MONTH, Integer.parseInt(startMonth.trim()));
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    Long startTime = cal.getTimeInMillis();
                    
                    cal = Calendar.getInstance();
            		cal.set(Calendar.YEAR, Integer.parseInt(endYear.trim()));
                    cal.set(Calendar.MONTH, Integer.parseInt(endMonth.trim()));
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    Long endTime = cal.getTimeInMillis();
                   
                     
            		Long time = new Date().getTime();
            		//String sql = "SELECT d.internalDocID AS objectID,102 AS objectType, d.containerType, d.containerID, v.modificationDate AS vModDate FROM jiveDocument d, jiveDocVersion v WHERE 1=1 AND d.internalDocID = v.internalDocID AND d.versionID = v.versionID AND (EXISTS (SELECT 1 FROM jiveDocumentProp p WHERE d.internalDocID=p.internalDocID AND v.versionID = p.versionID AND p.name = 'grail.month' AND p.propvalue not in ('NA') and (CAST( p.propvalue as INT ) > "+startMonth+") and (CAST( p.propvalue as INT ) < "+endMonth+") and  p.internalDocID in (select pyear.internalDocID from jivedocumentprop pyear where pyear.name = 'grail.year' and pyear.propvalue not in ('NA') and (CAST( pyear.propvalue as INT ) > "+startYear+") and (CAST( pyear.propvalue as INT ) < "+endYear+")) UNION SELECT 1 FROM jiveDocumentProp p1 WHERE d.internalDocID=p1.internalDocID AND v.versionID = p1.versionID AND  p1.name = 'grail.year' and p1.propvalue not in ('NA') and (CAST( p1.propvalue as INT ) > "+startYear+") and (CAST( p1.propvalue as INT ) < "+endYear+"))) AND d.expirationDate >= 1473228000000 ORDER BY vModDate DESC";
            		String sql = "SELECT d.internalDocID AS objectID,102 AS objectType, d.containerType, d.containerID, v.modificationDate AS vModDate FROM jiveDocument d, jiveDocVersion v WHERE 1=1 AND d.internalDocID = v.internalDocID AND d.versionID = v.versionID AND (EXISTS (SELECT 1 FROM jiveDocumentProp p WHERE d.internalDocID=p.internalDocID AND v.versionID = p.versionID AND p.name = 'grail.period.long' and p.propvalue not in ('NA') and (CAST( p.propvalue as BIGINT ) > "+startTime+") and (CAST( p.propvalue as BIGINT ) < "+endTime+"))) AND d.expirationDate >= "+time+" ORDER BY vModDate DESC";
	            	cachedPstmt.setSQL(sql);
            	
            	}
            	else
            	{
	            	String sql = cachedPstmt.getSQL().replaceAll("jiveDocumentProp.versionID", "p.versionID");
	            	cachedPstmt.setSQL(sql);
            	}*/
            	String sql = cachedPstmt.getSQL().replaceAll("jiveDocumentProp.versionID", "p.versionID");
            	cachedPstmt.setSQL(sql);
            }
        }
        finally {
            resultFilter.setRetrieveContainerInfo(oldRetrieveValue);
        }

        return new EntityDescriptorBlockIterable<ContainerAwareEntityDescriptor>(cachedPstmt,
                new ContainerAwareQueryCacheStrategy(), resultFilter.getStartIndex(), resultFilter.getEndIndex(),
                container.getObjectType(), container.getID());

    }


    public Iterable<Document> getDocumentsUnderEdit(JiveContainer container) {
        Iterable<Document> docs = documentEditManager.getDocumentsUnderEdit();
        List<Document> comDocs = new ArrayList<Document>();
        for (Document doc : docs) {
            if (doc.getJiveContainer().equals(container)) {
                comDocs.add(doc);
            }
        }

        return comDocs;
    }

    public void addDocument(Document document, Map parameters)
            throws RejectedException, DocumentAlreadyExistsException
    {
        addDocument(document, null, parameters);
    }

    public void addDocument(Document document, PermissionsBundle permBundle, Map parameters)
            throws RejectedException, DocumentAlreadyExistsException
    {
        addDocument(document, permBundle, parameters, true);
    }

    public void addDocument(Document document, PermissionsBundle permBundle, Map parameters, boolean fireEvents)
            throws RejectedException, DocumentAlreadyExistsException
    {
       addDocument(document, permBundle, parameters, fireEvents, null, null);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {RejectedException.class, DocumentAlreadyExistsException.class})
    public void addDocument(Document document, PermissionsBundle permBundle, Map parameters, boolean fireEvents,
            DocSaveTemplate docSaveTemplate, Date updated)
            throws RejectedException, DocumentAlreadyExistsException
    {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        // Get the underlying DbDocument object.
        DbDocument dbDocument = null;
        try {
            if (document instanceof DocumentProxy) {
                try {
                    dbDocument = (DbDocument) ((DocumentProxy) document).getProxiedDocument();
                }
                catch (UnauthorizedException e) {
                    log.error(e.getMessage(), e);
                }
            }
            else if (document instanceof DbDocument) {
                dbDocument = (DbDocument) document;
            }
            else {
                dbDocument = getDocument(document.getID());
            }
        }
        catch (DocumentObjectNotFoundException e) {
            log.error(e.getMessage(), e);
            return;
        }

        final JiveContainer container = dbDocument.getJiveContainer();

        // validate subject of the document is unique in the community
        DocumentBean docBean = null;
        try {
            docBean = documentDAO.getByDocumentTitle(container.getObjectType(), container.getID(), document.getUnfilteredSubject());
        }
        catch (DAOException e) {
            log.error(e.getMessage(), e);
        }

        if (docBean != null && docBean.getDocID() != document.getID()) {
            throw new DocumentAlreadyExistsException();
        }

        if (JiveContentObject.Status.DRAFT.equals(document.getStatus()) && !document.isTextBody()) {
            if (binaryDocumentHelper.isBinaryDocumentDraftDisabled(container)) {
                log.warn(MessageFormat.format("addDocument called for binary-document and with Draft state but drafts aren''t allowed for give container ({0}/{1}). rejecting the request.", container.getObjectType(), container.getID()));
                throw new RejectedException("Binary document drafts are not allowed in the given container", document);
            }
        }

        DbInterceptorManager interceptorManager = initDbInterceptorManager(container);
        invokePreInterceptors(interceptorManager, dbDocument);

        boolean importedContent = false;
        if ( parameters != null ) {
            importedContent = parameters.get(ContentEvent.IMPORTED) != null;
        }

        // Check the state of the document, if published then update the modification date of the container.
        // Otherwise, we'll wait to update the modification date to when the document is published.
        if (dbDocument.getDocumentState() == DocumentState.PUBLISHED) {
            if (!importedContent) {
                Date now = updated != null ? updated : new Date();
                dbDocument.setModificationDate(now);
                setModificationDateForContainer(container, now, null);
            }
            else {
                Date modificationDate = updated != null ? updated : dbDocument.getModificationDate();
                if (container.getModificationDate().before(modificationDate)) {
                    setModificationDateForContainer(container, modificationDate, null);
                }
            }
        }

        String tempDocID = document.getDocumentID();

        if (docSaveTemplate == null) {
            docSaveTemplate = new DefaultDocSaveTemplate();
        }
        docSaveTemplate.setDocument(document);

        // add document to the database
        docSaveTemplate.save();
        // set permissions
        if (permBundle != null) {
            dbDocument.setPermissions(permBundle);
        }

        // Run the document through all "post" interceptors.
        invokePostInterceptors(interceptorManager, dbDocument);

        // CS-4222 - don't fire add events when merging spaces as it causes status level points to be wrongly created
        if (fireEvents) {
            Map<String, String> params = new HashMap<>();
            if (parameters != null && parameters.containsKey(ORIGINAL_DOCUMENT_SUBJECT_KEY)) {
                params.put(ORIGINAL_DOCUMENT_SUBJECT_KEY, (String)parameters.get(ORIGINAL_DOCUMENT_SUBJECT_KEY));
            }

            Map<String, String> documentProperties = document.getProperties();
            if (documentProperties != null && documentProperties.containsKey(QuestManager.FROM_QUEST_KEY)) {
                params.put(QuestManager.FROM_QUEST_KEY, documentProperties.get(QuestManager.FROM_QUEST_KEY));
            }

            if (importedContent) {
                params.put(ContentEvent.IMPORTED, String.valueOf(parameters.get(ContentEvent.IMPORTED)));
            }
            final DocumentEvent event = new DocumentEvent(DocumentEvent.Type.ADDED, dbDocument, container, params);
            event.setTempDocID(tempDocID);
            //FIXME[Alok]: Is this lock needed when the event delivery will happen on a separate thread, at a later time?
            // Need to talk to Bruce/Pete about it.
            //
            // just trying to avoid the cross-classloader deadlock - this call eventually gets a lock for 'watchTime-XX',
            // then 'document-XX', while the rest of the code related to this gets the locks in the reverse order
            synchronized(LockUtil.intern(("document-" + document.getDocumentID()))) {
                eventDispatcher.fire(event);
            }
        }
    }

    // should be called before before document.save()
    public void invokePreInterceptors(DbInterceptorManager interceptorManager, Document dbDocument) {
        interceptorManager.invokeInterceptors(dbDocument, JiveInterceptor.Type.TYPE_PRE);
    }

    // should be called after document.save()
    public void invokePostInterceptors(DbInterceptorManager interceptorManager, Document dbDocument) {
        interceptorManager.invokeInterceptors(dbDocument, JiveInterceptor.Type.TYPE_POST);
    }

    public DbInterceptorManager initDbInterceptorManager(JiveContainer container) {
        DbInterceptorManager interceptorManager = new DbInterceptorManager(container.getObjectType(), container.getID());
        interceptorManager.setBlogManager(JiveApplication.getContext().getBlogManager()); // todo refactor
        interceptorManager.setCommunityManager(communityManager);
        interceptorManager.setProjectManager(JiveApplication.getContext().getProjectManager());
        interceptorManager.init();
        return interceptorManager;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Collection<User> getDocumentApprovers(JiveContainer container) {
        EntityDescriptor key = new EntityDescriptor(container);
        List<Long> approverIds = containerApproverCache.get(key);

        if (approverIds == null) {
            try {
                approverIds = approvalManager.getContainerApprovers(container.getObjectType(), container.getID());
                containerApproverCache.put(key, approverIds);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
                approverIds = Collections.emptyList();
            }
        }

        return new GenericCollectionProxy<Long, User>(approverIds,

                new GenericProxyFactory<Long, User>() {
                    public User createProxy(Long object) {
                        try {
                            return userManager.getUser(object);
                        }
                        catch (Exception e) {
                            log.error(e.getMessage(), e);
                            return null;
                        }
                    }
                });

    }

    @Audit(auditor = DbDocumentManagerAuditor.class)
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void addDocumentApprover(JiveContainer container, User user) {
        try {
            DocumentResultFilter filter = DocumentResultFilter.createAllStatesFilter();
            filter.addDocumentState(DocumentState.PENDING_APPROVAL);
            filter.setRecursive(true);
            for (Document doc : getDocumentVersions(container, filter)) {
                //failing to do this check can bork workflow for existing doc-level approvers
                boolean currentApprover = false;
                for (User approver : doc.getApprovers()) {
                    if (approver.getID() == user.getID()) {
                        currentApprover = true;
                        break;
                    }
                }
                if (!currentApprover) {
                    doc.addApprover(user);

                    doc.setMinorEdit(true);
                    doc.save();
                }
            }
            approvalManager.addContainerApprover(container.getObjectType(), container.getID(), user.getID());
            containerApproverCache.remove(new EntityDescriptor(container));
        }
        catch (DAOException e) {
            log.error(e.getMessage(),e);
        }
    }

    @Audit(auditor = DbDocumentManagerAuditor.class)
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void removeDocumentApprover(JiveContainer container, User user) {
        if (getDocumentApprovers(container).contains(user)) {
            try {
                //get all documents which are in approval in this container; delete them all for this approver.
                DocumentResultFilter filter = DocumentResultFilter.createAllStatesFilter();
                filter.addDocumentState(DocumentState.PENDING_APPROVAL);
                filter.setRecursive(true);
                for (Document doc : getDocumentVersions(container, filter)) {
                    if (DocumentCollaborationHelper.getApprovalCommunities(this, doc, user).size() <= 1 &&
                            !DocumentCollaborationHelper.isDocumentLevelApprover(doc, user)) {
                        Workflow workflow = workflowUtils.getDocumentWorkflow((DbDocument)doc, user);
                        if (workflow != null) {
                            long workflowID = ((DbDocument)doc).getWorkflowID();

                            try {
                                HashMap<String, User> args = new HashMap<String, User>();
                                args.put("user", user);

                                // Mark as approved before deleting the workflow, so we don't end up with docs forever pending
                                workflow.doAction(workflowID, 1, args);
                            }
                            catch (WorkflowException e) {
                                log.error(e.getMessage(), e);
                            }

                            if (approvalManager.getWorkflowBean(workflowID, user.getID()) != null) {
                                approvalManager.deleteUserWorkflowBean(workflowID, user.getID());
                            }
                        }
                    }
                }
                approvalManager.removeContainerApprover(container.getObjectType(), container.getID(), user.getID());
                containerApproverCache.remove(new EntityDescriptor(container));
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Audit(auditor = DbDocumentManagerAuditor.class)
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void removeDocumentApprover(Document document, User user) {
        Workflow workflow = workflowUtils.getDocumentWorkflow((DbDocument) document, user);
        try {
            if (workflow != null) {
                try {
                    HashMap<String, User> args = new HashMap<String, User>();
                    args.put("user", user);
                    //If you see any exceptions here. Then a bunch of permission level changes got fired at once.
                    // The net result is fine. Don't worry :)
                    workflow.doAction(((DbDocument) document).getWorkflowID(), 6, args);
                    document.removeApprover(user);
                    document.setMinorEdit(true);
                    document.save();
                }
               catch (WorkflowException e) {
                    log.error(e.getMessage(), e);
                }
            }
            }
             catch (DAOException e) {
                    log.error(e.getMessage(), e);
                }

    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void updateFromContainerAfterMovingDocument(JiveContainer container, Document document, Date updated) {
        clearQueryCache(container);
        if (document.getDocumentState() == DocumentState.PUBLISHED) {
            setModificationDateForContainer(container, updated != null ? updated : new Date(), null);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void updateUserContainerPerms(Document document, JiveContainer sourceContainer, JiveContainer targetContainer) throws Exception{
        User currentOwner = document.getUser();

        // if moving from user's personal container then restore basic owner only visibility
        if (sourceContainer.getObjectType() == JiveConstants.USER_CONTAINER) {
            getContentVisibilityHelper().addOwnerVisibilityPolicy(currentOwner, document);
            // fall back to visibility settings to dictate access to edit, set to authorship to OPEN
            if (document.getAuthorshipPolicy() != Document.AUTHORSHIP_MULTIPLE) {
                document.setAuthorshipPolicy(Document.AUTHORSHIP_OPEN);
            }
        }
        if (targetContainer.getObjectType() != JiveConstants.USER_CONTAINER) {
            return;
        }

        Long containerUserID = userContainerManager.getUserContainer(targetContainer.getID()).getUserID();

        //Check ownership, only onwers of a doc can move it to their user container
        if (containerUserID != currentOwner.getID()) {
            throw new UnauthorizedException("User " + currentOwner.getID() +
                    " can't move the document " + document.getID() + " to its personal container since it isn't the owner");
        }

        // Only current authors will be able to edit the file
        document.setAuthorshipPolicy(Document.AUTHORSHIP_MULTIPLE);
        List<Long> authors = JiveIterators.mapToList(document.getAuthors(), new JiveIterators.Mapper<User, Long>() {
            public Long map(User user) {
                return user.getID();
            }
        });
        //Change Visibility.
        getContentVisibilityHelper().addRestrictedVisibilityPolicy(currentOwner, document, authors.toArray(new Long[authors.size()]));
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    private void removeUnauthorizedCollaborators(Document document, JiveContainer targetContainer) {
        if (document.getAuthorCount() > 0 || document.getApproverCount() > 0 || document.getReviewerCount() > 0) {
            boolean dirty = false;
            //authors
            Iterable<User> authors = document.getAuthors();
            for (User author : authors) {
                if (!DocumentPermHelper.getCanCreateDocument(author, targetContainer))
                {
                    document.removeAuthor(author);
                    dirty = true;
                }
            }
            //approvers
            Iterable<User> approvers = document.getApprovers();
            for (User approver : approvers) {
                if (!JiveContainerPermHelper.getCanViewContainerContent(targetContainer, approver)) {
                    document.removeApprover(approver);
                    dirty = true;
                }
            }
            //reviewers
            Iterable<User> reviewers = document.getReviewers();
            for (User reviewer : reviewers) {
                if (!JiveContainerPermHelper.getCanViewContainerContent(targetContainer, reviewer)) {
                    document.removeReviewer(reviewer);
                    dirty = true;
                }
            }
            if (dirty) {
                document.save();
            }
        }
    }

    public Document getLatestDocument(JiveContainer container) {
        // WARNING! The implementation of this method does not respect permissions and
        // may provide access to documents that the user doesn't have permission to view.
        // The proxy class bypasses this method call and implements its own
        // logic to find the latest document in the container that the user is allowed
        // to view. This implementation is only provided for the case
        // that a JiveContext instance is being used directly without proxy.
        if (getDocumentVersionCount(container) == 0) {
            return null;
        }
        DocumentResultFilter filter = new DocumentResultFilter();
        filter.setSortOrder(ResultFilter.DESCENDING);
        filter.setSortField(JiveConstants.MODIFICATION_DATE);
        // Round down the lower date boundary by 1 day.
        long modificationDate = container.getModificationDate().getTime();
        filter.setModificationDateRangeMin(ThreadResultFilter.roundDate(new Date(modificationDate), 24 * 60 * 60));
        filter.setNumResults(1);
        Iterable<Document> documents = getDocumentVersions(container, filter);
        if (documents.iterator().hasNext()) {
            return documents.iterator().next();
        }
        else {
            return null;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void expireTemporaryBinaryBodies() {
        // Delete any temporary bodies that were created more
        // than 24 hours ago and that haven't been added to a document.
        Date deleteDate = new Date(System.currentTimeMillis() - JiveConstants.DAY);
        List<Long> bodies = binaryBodyDAO.getTempBodyIDsWithDateAfter(deleteDate);

        for (long bodyID : bodies) {
            try {
                DbBinaryBody body = (DbBinaryBody) getBinaryBody(bodyID);
                body.delete();
            }
            catch (DocumentObjectNotFoundException e) {
                log.error(e.getMessage(), e);
            }
            catch (DAOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private ContentVisibilityHelper getContentVisibilityHelper() {
        if (contentVisibilityHelper == null) {
            contentVisibilityHelper = JiveApplication.getContext().getSpringBean("visibilityHelper");
        }
        return contentVisibilityHelper;
    }

    /**
     * DocSaveTemplate allows overriding the save call and implementing specific save functionality
     */
    abstract class DocSaveTemplate {

        protected Document document;
        //enforced anyone wanting to use DocSaveTemplate must specify their save method
        public abstract void saveDocument();

        /**
         * this give you the ability to override save process
         * @throws DAOException
         */
        public void save() throws DAOException{
            saveDocument();
        }

        protected void setDocument(Document document) {
            this.document = document;
        }
    }

    /**
     * DefaultDocSaveTemplate is used to call save on a document as the default save behavior
     */
    class DefaultDocSaveTemplate extends DocSaveTemplate{
        public void saveDocument() {
            document.save();
        }
    }
}
