/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl.search.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jivesoftware.base.User;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.base.wiki.WikiContentHelper;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.BinaryBody;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.JiveObjectType;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.TagManager;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.acclaim.impl.LikeAcclaimType;
import com.jivesoftware.community.acclaim.impl.RateAcclaimType;
import com.jivesoftware.community.action.util.RenderUtils;
import com.jivesoftware.community.impl.dao.DbDocumentDAO;
import com.jivesoftware.community.impl.search.SearchIndexSettingsManager;
import com.jivesoftware.community.impl.search.provider.filter.BinaryBodyIndexFilter;
import com.jivesoftware.community.impl.search.provider.util.DefaultAcclaimRowMapper;
import com.jivesoftware.community.impl.search.provider.util.DefaultAttachmentInfoRowMapper;
import com.jivesoftware.community.impl.search.provider.util.DefaultOutcomeInfoRowMapper;
import com.jivesoftware.community.impl.search.provider.util.DefaultTagInfoRowMapper;
import com.jivesoftware.community.impl.search.provider.util.IndexInfoProviderHelper;
import com.jivesoftware.community.impl.search.provider.util.SearchAttachmentGuide;
import com.jivesoftware.community.impl.search.provider.util.SearchContentAcclaimInfo;
import com.jivesoftware.community.impl.search.provider.util.SearchContentAttachmentInfo;
import com.jivesoftware.community.impl.search.provider.util.SearchContentOutcomeInfo;
import com.jivesoftware.community.impl.search.provider.util.SearchContentTagInfo;
import com.jivesoftware.community.impl.search.provider.util.SearchDataObject;
import com.jivesoftware.community.impl.search.provider.util.SearchTagUtil;
import com.jivesoftware.community.impl.search.signal.BookmarkInfoHelper;
import com.jivesoftware.community.outcome.dao.OutcomeResultFilter;
import com.jivesoftware.community.renderer.impl.v2.JAXPUtils;
import com.jivesoftware.community.search.ExtendedIndexInfoProvider;
import com.jivesoftware.community.search.IndexField;
import com.jivesoftware.community.search.IndexInfo;
import com.jivesoftware.community.util.DocumentPermHelper;
import com.jivesoftware.service.client.JiveSearchTextExtractionService;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jivesoftware.community.impl.DbDocument;

import static java.lang.String.valueOf;

public class DocumentIndexInfoProvider extends JiveJdbcDaoSupport implements ExtendedIndexInfoProvider {

    private static final Logger log = LogManager.getLogger(DocumentIndexInfoProvider.class);

    private static final String COUNT = "SELECT count(*) FROM jiveDocument, jiveDocVersion "
            + " WHERE jiveDocument.internalDocID = jiveDocVersion.internalDocID "
            + " AND jiveDocVersion.state = 'published' AND jiveDocVersion.modificationDate <= ?";

    private static final String MAX_ID = "SELECT MAX(jiveDocument.internalDocID) FROM jiveDocument, jiveDocVersion "
            + " WHERE jiveDocument.internalDocID = jiveDocVersion.internalDocID "
            + " AND jiveDocVersion.state = 'published' AND jiveDocVersion.modificationDate <= ?";

    private static final String MIN_ID = "SELECT MIN(jiveDocument.internalDocID) FROM jiveDocument, jiveDocVersion "
            + " WHERE jiveDocument.internalDocID = jiveDocVersion.internalDocID "
            + " AND jiveDocVersion.state = 'published' AND jiveDocVersion.modificationDate >= ?";
    private static final String MIN_ID_NO_DATE = "SELECT MIN(jiveDocument.internalDocID) FROM jiveDocument, jiveDocVersion "
            + " WHERE jiveDocument.internalDocID = jiveDocVersion.internalDocID "
            + " AND jiveDocVersion.state = 'published'";

    private static final String ID = "SELECT jiveDocument.internalDocID FROM jiveDocument, jiveDocVersion "
            + " WHERE jiveDocument.internalDocID = jiveDocVersion.internalDocID "
            + " AND jiveDocVersion.internalDocID >= ? AND jiveDocVersion.internalDocID <= ? "
            + " AND jiveDocVersion.state = 'published' AND jiveDocVersion.modificationDate >= ? "
            + " AND jiveDocVersion.modificationDate <= ?";

    private static RowMapper<EntityDescriptor> ENTITY_MAPPER = new RowMapper<EntityDescriptor>() {
        public EntityDescriptor mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EntityDescriptor(JiveConstants.DOCUMENT, rs.getLong("internalDocID"));
        }
    };

    private static final String CONDITIONS =
            "jdv.state = 'published' " +
                    "AND jd.internalDocID >= ? " +
                    "AND jd.internalDocID <= ? " +
                    "AND jdv.modificationDate >= ? " +
                    "AND jdv.modificationDate <= ?";

    private static final String CONTENT =
            "SELECT jd.internalDocID, jd.documentID, jdv.versionID, jdv.title, jdv.summary, jdb.bodyID, jdb.bodyText, jd.creationDate, " +
                   "jdv.modificationDate, jd.containerType, jd.containerID, jd.userID, ju.username " +
              "FROM jiveDocument jd " +
              "JOIN jiveDocVersion jdv " +
                "ON jd.internalDocID = jdv.internalDocID " +
              "JOIN jiveDocBodyVersion jdbv " +
                "ON jdbv.internalDocID = jdv.internalDocID " +
               "AND jdbv.versionID = jdv.versionID " +
              "JOIN jiveDocumentBody jdb " +
                "ON jdb.bodyID = jdbv.bodyID " +
              "LEFT OUTER JOIN jiveUser ju " +
                "ON ju.userID = jd.userID " +
             "WHERE " + CONDITIONS;

    private static final String ACCLAIM =
            "SELECT jd.internalDocID, jaLikes.score AS likes, jaRatings.score AS ratings " +
              "FROM jiveDocument jd " +
              "JOIN jiveDocVersion jdv " +
                "ON jdv.internalDocID = jd.internalDocID " +
              "LEFT OUTER JOIN jiveAcclaim jaLikes " +
                "ON jaLikes.objectType = " + JiveObjectType.Document.getTypeID() + " " +
               "AND jaLikes.objectID = jd.internalDocID " +
               "AND jaLikes.acclaimType = '" + LikeAcclaimType.NAME + "' " +
              "LEFT OUTER JOIN jiveAcclaim jaRatings " +
                "ON jaRatings.objectType = " + JiveObjectType.Document.getTypeID() + " " +
               "AND jaRatings.objectID = jd.internalDocID " +
               "AND jaRatings.acclaimType = '" + RateAcclaimType.NAME + "' " +
             "WHERE " + CONDITIONS;

    private static final String TAGS =
            "SELECT jd.internalDocID, jt.tagname " +
              "FROM jiveDocument jd " +
              "JOIN jiveDocVersion jdv " +
                "ON jdv.internalDocID = jd.internalDocID " +
              "JOIN jiveObjectTag jot " +
                "ON jot.objectType = " + JiveObjectType.Document.getTypeID() + " " +
               "AND jot.objectID = jd.internalDocID " +
              "JOIN jiveTag jt ON jt.tagID = jot.tagID " +
             "WHERE jot.childObjectType = -1 AND jot.childObjectID = -1 AND " + CONDITIONS;

    private static final String OUTCOMES =
            "SELECT jd.internalDocID, jo.outcomeID, jo.outcomeTypeID, jo.modificationDate " +
              "FROM jiveDocument jd " +
              "JOIN jiveDocVersion jdv " +
                "ON jdv.internalDocID = jd.internalDocID " +
              "JOIN jiveOutcome jo " +
                "ON jo.parentObjectType = " + JiveObjectType.Document.getTypeID() + " " +
               "AND jo.parentObjectID = jd.internalDocID " +
             "WHERE jo.status in (" + StringUtils.join(OutcomeResultFilter.getOutcomeStatusIDs(false), ",") +
               ") AND " + CONDITIONS;

    private static final String ATTACH =
            "SELECT jd.internalDocID, ja.attachmentID " +
              "FROM jiveDocument jd " +
              "JOIN jiveDocVersion jdv " +
                "ON jdv.internalDocID = jd.internalDocID " +
              "JOIN jiveAttachVersion ja " +
                "ON ja.internalDocID = jd.internalDocID " +
               "AND ja.versionID = jdv.versionID " +
             "WHERE " + CONDITIONS;

    private static final String COLLAB =
            "SELECT jd.internalDocID, jdc.userID, ju.username " +
              "FROM jiveDocument jd " +
              "JOIN jiveDocVersion jdv " +
                "ON jdv.internalDocID = jd.internalDocID " +
              "JOIN jiveDocCollab jdc " +
                "ON jdc.internalDocID = jd.internalDocID " +
               "AND jdc.collaboratorType = " + DbDocumentDAO.COLLABORATOR_AUTHOR + " " +
              "JOIN jiveUser ju " +
                "ON ju.userID = jdc.userID " +
             "WHERE " + CONDITIONS;

    private static final RowMapper<DocCollaborator> COLLAB_MAPPER = new RowMapper<DocCollaborator>() {
        public DocCollaborator mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DocCollaborator(rs.getLong("internalDocID"), rs.getLong("userID"), rs.getString("username"));
        }
    };

    private DocumentManager documentManager;
    private SearchIndexSettingsManager searchIndexSettingsManager;
    private IndexInfoProviderHelper helper;
    private TagManager tagManager;
    private SearchTypeProviderUtil util;
    private BookmarkInfoHelper bookmarkInfoHelper;
    private JiveSearchTextExtractionService jiveSearchTextExtractionService;
    private Collection<BinaryBodyIndexFilter> binaryBodyIndexFilters;
    
    private static String GRAIL_BRAND_PROP = "grail.brand";
    private static String GRAIL_COUNTRY_PROP = "grail.country";
    private static String GRAIL_METHODOLOGY_PROP = "grail.methodology";
    private static String GRAIL_MONTH_PROP = "grail.month";
    private static String GRAIL_YEAR_PROP = "grail.year";

    private static String GRAIL_PERIOD_PROP = "grail.period.long";


    public void setBinaryBodyIndexFilters(Collection<BinaryBodyIndexFilter> binaryBodyIndexFilters) {
        this.binaryBodyIndexFilters = binaryBodyIndexFilters;
    }

    @Required
    public void setSearchIndexSettingsManager(SearchIndexSettingsManager searchIndexSettingsManager) {
        this.searchIndexSettingsManager = searchIndexSettingsManager;
    }

    @Required
    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    @Required
    public void setIndexInfoProviderHelper(IndexInfoProviderHelper helper) {
        this.helper = helper;
    }

    @Required
    public void setTagManager(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    @Required
    public void setSearchTypeProviderUtil(SearchTypeProviderUtil util) {
        this.util = util;
    }

    @Required
    public void setBookmarkInfoHelper(BookmarkInfoHelper bookmarkInfoHelper) {
        this.bookmarkInfoHelper = bookmarkInfoHelper;
    }

    @Required
    public void setJiveSearchTextExtractionService(JiveSearchTextExtractionService jiveSearchTextExtractionService) {
        this.jiveSearchTextExtractionService = jiveSearchTextExtractionService;
    }

    public long getMaxID(Date max) {
        return getSimpleJdbcTemplate().queryForLong(MAX_ID, max.getTime());
    }

    public long getMinID(Date min) {
        if (min.getTime() == 0) {
            return getSimpleJdbcTemplate().queryForLong(MIN_ID_NO_DATE);
        } else {
            return getSimpleJdbcTemplate().queryForLong(MIN_ID, min.getTime());
        }
    }

    public long getCount(Date max) {
        return getSimpleJdbcTemplate().queryForLong(COUNT, max.getTime());
    }

    public List<EntityDescriptor> getIDs(long minID, long maxID, Date minDate, Date maxDate) {
        return getSimpleJdbcTemplate().query(ID, ENTITY_MAPPER, minID, maxID, minDate.getTime(), maxDate.getTime());
    }

    public List<IndexInfo> getContent(long minID, long maxID, Date minDate, Date maxDate) {
        long minTime = minDate.getTime();
        long maxTime = maxDate.getTime();

        RowMapper<DocInfo> contentMapper = new RowMapper<DocInfo>() {
            public DocInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new DocInfo(rs, helper);
            }
        };

        Map<Long, DocInfo> collectionMap = helper.collectBulkDataMap(getSimpleJdbcTemplate(), CONTENT, contentMapper,
                minID, maxID, minTime, maxTime);

        List<SearchContentAcclaimInfo> acclaimInfo = getSimpleJdbcTemplate().query(ACCLAIM,
                DefaultAcclaimRowMapper.getInstance(), minID, maxID, minTime, maxTime);
        helper.mergeData(collectionMap, acclaimInfo, new IndexInfoProviderHelper.DataMerger<DocInfo, SearchContentAcclaimInfo>() {
            public void merge(DocInfo docInfo, SearchContentAcclaimInfo acclaimData) {
                docInfo.likes = acclaimData.getLikesScore();
                docInfo.rating = acclaimData.getRatingScore();
            }
        });

        List<SearchContentTagInfo> tagInfo = getSimpleJdbcTemplate().query(TAGS, DefaultTagInfoRowMapper.getInstance(),
                minID, maxID, minTime, maxTime);
        helper.mergeData(collectionMap, tagInfo, new IndexInfoProviderHelper.DataMerger<DocInfo, SearchContentTagInfo>() {
            public void merge(DocInfo docInfo, SearchContentTagInfo tagData) {
                if (docInfo != null && tagData != null) {
                    docInfo.tags.add(tagData);
                }
            }
        });

        List<SearchContentOutcomeInfo> outcomeInfo = getSimpleJdbcTemplate().query(OUTCOMES, DefaultOutcomeInfoRowMapper.getInstance(),
                minID, maxID, minTime, maxTime);
        helper.mergeData(collectionMap, outcomeInfo, new IndexInfoProviderHelper.DataMerger<DocInfo, SearchContentOutcomeInfo>() {
            public void merge(DocInfo docInfo, SearchContentOutcomeInfo outcomeData) {
                docInfo.outcomes.add(outcomeData);
            }
        });

        List<DocCollaborator> collabInfo =
                getSimpleJdbcTemplate().query(COLLAB, COLLAB_MAPPER, minID, maxID, minTime, maxTime);
        helper.mergeData(collectionMap, collabInfo, new IndexInfoProviderHelper.DataMerger<DocInfo, DocCollaborator>() {
            public void merge(DocInfo docInfo, DocCollaborator collab) {
                if (collab.userID != AnonymousUser.ANONYMOUS_ID) {
                    docInfo.users.add(new DocUser(collab.userID, collab.username));
                }
            }
        });

        if (searchIndexSettingsManager.isAttachmentSearchEnabled()) {
            List<SearchContentAttachmentInfo> attachInfo = getSimpleJdbcTemplate().query(ATTACH,
                    DefaultAttachmentInfoRowMapper.getInstance(), minID, maxID, minTime, maxTime);
            helper.mergeData(collectionMap, attachInfo, new IndexInfoProviderHelper.DataMerger<DocInfo, SearchContentAttachmentInfo>() {
                public void merge(DocInfo docInfo, SearchContentAttachmentInfo attachData) {
                    docInfo.attachments.add(attachData.getAttachmentID());
                }
            });
        }

        List<IndexInfo> infos = helper.buildIndexInfoObjects(collectionMap.values(),
            new IndexInfoProviderHelper.IndexInfoObjectDataProvider<DocInfo>() {
                public Map<IndexField, String> getIndexFields(DocInfo object) {
                    return DocumentIndexInfoProvider.this.getIndexFields(object, false);
                }

                public String getLanguage(DocInfo object) {
                    return helper.getContainerLanguage(object.containerType, object.containerID);
                }
            });

        return bookmarkInfoHelper.applyBookmarkInfo(infos, JiveObjectType.Document.getTypeID(), minID, maxID);
    }

    private Map<IndexField, String> getIndexFields(DocInfo data, boolean isHighlight) {
        Map<IndexField, String> map = Maps.newHashMap();
        String body = getBodyText(data, isHighlight);
        map.put(IndexField.subject, data.subject);
        map.put(IndexField.description, data.summary);
        map.put(IndexField.body, body);
        map.put(IndexField.tags, SearchTagUtil.joinTags(data.tags));
        map.put(IndexField.objectType, valueOf(JiveObjectType.Document.getTypeID()));
        map.put(IndexField.objectID, valueOf(data.internalDocID));
        map.put(IndexField.creationDate, valueOf(data.creationDate));
        map.put(IndexField.modificationDate, valueOf(helper.extractModificationDate(data.modificationDate, data.outcomes)));
        map.put(IndexField.alias, data.documentID + " " + data.documentID.toLowerCase());
        map.put(IndexField.containerType, valueOf(data.containerType));
        map.put(IndexField.containerID, valueOf(data.containerID));
        map.put(IndexField.containerIDs,
                util.buildIDString(helper.getContainers(data.containerType, data.containerID).iterator()));
        if (data.likes > 0d) {
            map.put(IndexField.likes, valueOf(data.likes));
        }
        if (data.rating > 0d) {
            map.put(IndexField.rating, valueOf(data.rating));
        }
        
     // Allow document properties to be indexed
        Document document = getDocument(data.internalDocID);
        if(document != null) {
            StringBuilder propertiesText = new StringBuilder();
            Map<String, String> properties = document.getProperties();
            if(properties != null && properties.size() > 0) {
                if(properties.get(GRAIL_BRAND_PROP) != null) {
                    propertiesText.append(properties.get(GRAIL_BRAND_PROP)).append(" ");
                }

                if(properties.get(GRAIL_METHODOLOGY_PROP) != null) {
                    propertiesText.append(properties.get(GRAIL_METHODOLOGY_PROP)).append(" ");
                }

                if(properties.get(GRAIL_MONTH_PROP) != null) {
                    propertiesText.append(properties.get(GRAIL_MONTH_PROP)).append(" ");
                }

                if(properties.get(GRAIL_YEAR_PROP) != null) {
                    propertiesText.append(properties.get(GRAIL_YEAR_PROP)).append(" ");
                }

                if(properties.get(GRAIL_COUNTRY_PROP) != null) {
                    propertiesText.append(properties.get(GRAIL_COUNTRY_PROP));
                }
            }

            if(!propertiesText.toString().equals("")) {
                map.put(IndexField.text, propertiesText.toString());
            }
        }

        StringBuilder authorUsernames = new StringBuilder();
        StringBuilder authorIDs = new StringBuilder();
        for (DocUser u : data.users) {
            if (u.userID != AnonymousUser.ANONYMOUS_ID) {
                if (StringUtils.isNotEmpty(u.username)) {
                    authorUsernames.append(u.username).append(" ");
                }
                authorIDs.append(u.userID).append(" ");
            }
        }

        if (StringUtils.isNotBlank(authorUsernames.toString())) {
            map.put(IndexField.author, authorUsernames.toString().trim());
        }

        if (StringUtils.isNotBlank(authorIDs.toString())) {
            map.put(IndexField.userID, authorIDs.toString().trim());
        }

        StringBuilder outcomeIDs = new StringBuilder();
        StringBuilder outcomeTypes = new StringBuilder();
        for (SearchContentOutcomeInfo outcome : data.outcomes) {
            outcomeIDs.append(outcome.getOutcomeID()).append(" ");
            outcomeTypes.append(outcome.getOutcomeTypeID()).append(" ");
        }

        if (StringUtils.isNotBlank(outcomeIDs.toString())) {
            map.put(IndexField.outcomeID, outcomeIDs.toString().trim());
        }

        if (StringUtils.isNotBlank(outcomeTypes.toString())) {
            map.put(IndexField.outcomeType, outcomeTypes.toString().trim());
        }

        return map;
    }

    public IndexInfo getContent(long id) {
        final Document doc = getDocument(id);
        if (doc != null) {
            final Collection<SearchContentTagInfo> tags = SearchTagUtil.loadSearchContentTagInfo(tagManager, doc);
            final Iterator<SearchContentOutcomeInfo> outcomeInfo = helper.getOutcomeInfo(doc);
            final SearchContentAcclaimInfo acclaimInfo = helper.getObjectAcclaimInfo(doc);
            final DocInfo data = new DocInfo(doc, tags, Lists.newArrayList(outcomeInfo), acclaimInfo, helper);
            final IndexInfo info = new IndexInfo() {
                private Map<IndexField, String> content = new HashMap<>();

                public Map<IndexField, String> getIndexContent(boolean isHighlight) {
                    content.putAll(getIndexFields(data, isHighlight));
                    return content;
                }

                public String getLanguage() {
                    return helper.getContainerLanguage(data.containerType, data.containerID);
                }
            };

            return bookmarkInfoHelper.applyBookmarkInfo(info, JiveObjectType.Document.getTypeID());
        }

        return null;
    }

    @Override
    public Map<HighlightDataField, String> getHighlightsData(long objectID, Set<HighlightDataField> retrieveDataFields) {
        if (retrieveDataFields.isEmpty()) {
            return Collections.emptyMap();
        }

        Document doc = getDocument(objectID);
        if (doc == null) {
            return Collections.emptyMap();
        }

        Map<HighlightDataField, String> highlightData = Maps.newHashMap();
        for (HighlightDataField field : retrieveDataFields) {
            switch (field) {
                case SUBJECT:
                    highlightData.put(field, StringUtils.trimToEmpty(doc.getSubject()));
                    break;
                case BODY:
                    DocInfo docInfo = new DocInfo(doc, Collections.<SearchContentTagInfo>emptyList(), Collections.<SearchContentOutcomeInfo>emptyList(),
                            new SearchContentAcclaimInfo(objectID, 0d, 0d), helper);
                    highlightData.put(field, docInfo.summary + " " + docInfo.dbBody);
                    break;
            }
        }

        return highlightData;
    }

    private Document getDocument(long docID) {
        Document doc = null;
        try {
            doc = documentManager.getDocument(docID);
        }
        catch (DocumentObjectNotFoundException e) {
            log.debug("Couldn't get document with id " + docID);
        }

        return doc;
    }

    public boolean getCanViewType() {
        return true;
    }

    public boolean getCanViewObject(JiveObject jiveObject) {
        return DocumentPermHelper.getCanViewDocument((Document) jiveObject);
    }



    private String getBodyText(DocInfo docInfo, boolean isHighlight) {
        String body = docInfo.dbBody;
        if (!isHighlight && docInfo.isBinaryDoc) {
            body = loadBinaryBody(docInfo);
        }

        body = StringUtils.isBlank(body) ? "" : body;

        if (!isHighlight && searchIndexSettingsManager.isAttachmentSearchEnabled()) {
            Iterator<Attachment> attachIter = helper.getAttachments(docInfo);
            if (attachIter.hasNext()) {
                StringBuilder bodyBuilder = new StringBuilder(body);
                helper.loadAttachmentText(bodyBuilder, attachIter, docInfo.getAttachmentIDs().size());
                body = bodyBuilder.toString();
            }
        }

        return body == null ? "" : body;
    }

    private String loadBinaryBody(DocInfo data) {
        BinaryBody binaryBody = null;
        Document document = null;
        try {
            document = documentManager.getDocument(data.internalDocID);
            binaryBody = document.getBinaryBody();
        }
        catch (DocumentObjectNotFoundException e) {
            log.debug("Couldn't find binary body for document " + data.documentID);
        }

        if (binaryBody == null) {
            return "";
        }

        if (!searchIndexSettingsManager.isAttachmentSearchEnabled() || shouldFilterBinaryIndexing(document, binaryBody)) {
            return binaryBody.getName();
        }

        EntityDescriptor entity = new EntityDescriptor(binaryBody);
        try {
            InputStream extractedBodyText = jiveSearchTextExtractionService.getEntityExtractedText(entity,
                    data.modificationDate);

            return helper.readExtractedTextStream(entity, extractedBodyText);
        }
        catch (Throwable e) {
            log.warn("Error extracting text from binary body for entity " + entity, e);
            return "";
        }
    }

    private boolean shouldFilterBinaryIndexing(Document document, BinaryBody binaryBody) {
        try {
            if (binaryBodyIndexFilters != null) {
                for (BinaryBodyIndexFilter binaryBodyIndexFilter : binaryBodyIndexFilters) {
                    if (binaryBodyIndexFilter.shouldFilterBinaryBody(document, binaryBody)) {
                        return true;
                    }
                }
            }
        }
        catch (Exception e) {
            log.warn("failed to check if binary-body should be filtered. ignoring.", e);
        }

        return false;
    }


    public boolean isIndexable(JiveObject jiveObject) {
        if (jiveObject instanceof EntityDescriptor) {
            EntityDescriptor ed = (EntityDescriptor) jiveObject;
            if (ed.getObjectType() == JiveConstants.DOCUMENT ) {
                try {
                    Document document = documentManager.getDocument(ed.getID());
                    return isVisible(document);
                }
                catch (NotFoundException e) {
                    // it's possible we're being called from a delete event
                    // where we might not have a handle on the object
                    return false;
                }
            }
        }
        else if (jiveObject instanceof Document) {
            return isVisible((Document) jiveObject);
        }

        return false;
    }

    public EntityDescriptor getParentEntity(long objectID) {
        return null;
    }

    private boolean isVisible(Document document) {
        return document.getStatus().isVisible() && document.getDocumentState() == DocumentState.PUBLISHED;
    }

    static class DocInfo implements SearchDataObject, SearchAttachmentGuide {
        private final Document documentObject;
        private final long internalDocID;
        private final String documentID;
        private final String subject;
        private final String summary;
        private final long bodyID;
        final boolean isBinaryDoc;
        private final String dbBody;
        private final long creationDate;
        private final long modificationDate;
        private final int containerType;
        private final long containerID;

        private Set<DocUser> users = Sets.newHashSet();

        private double likes;
        private double rating;

        private Collection<SearchContentTagInfo> tags;

        private Collection<SearchContentOutcomeInfo> outcomes;

        private Set<Long> attachments;

        DocInfo(Document doc, Collection<SearchContentTagInfo> tags, Collection<SearchContentOutcomeInfo> outcomes,
                SearchContentAcclaimInfo acclaimInfo, IndexInfoProviderHelper helper)
        {
            this.documentObject = doc;
            this.internalDocID = doc.getID();
            this.documentID = doc.getDocumentID();
            this.subject = StringUtils.trimToEmpty(doc.getSubject());
            this.summary = RenderUtils.convertTextForSearch((doc.getSummary()));
            BinaryBody bb = doc.getBinaryBody();
            this.isBinaryDoc = bb != null;
            this.bodyID = bb == null ? -1L : bb.getID();
            this.dbBody = RenderUtils.convertTextForSearch(doc.getBody()) + helper.getHRefsFromAnchorsWithDifferentTitles(doc.getBody());
            this.creationDate = doc.getCreationDate().getTime();
            this.modificationDate = doc.getModificationDate().getTime();
            this.containerType = doc.getContainerType();
            this.containerID = doc.getContainerID();

            for (User u : doc.getAuthors()) {
                this.users.add(new DocUser(u.getID(), u.getUsername()));
            }

            User u = doc.getUser();
            this.users.add(new DocUser(u.getID(), u.getUsername()));

            this.likes = acclaimInfo.getLikesScore();
            this.rating = acclaimInfo.getRatingScore();

            if (tags != null) {
                this.tags = Lists.newArrayList(tags);
            }
            else {
                this.tags = Lists.newArrayList();
            }

            this.outcomes = outcomes;
        }

        DocInfo(ResultSet rs, IndexInfoProviderHelper helper) throws SQLException {
            this.documentObject = null;

            String body = rs.getString("bodyText");

            this.isBinaryDoc = body == null;

            if (StringUtils.isEmpty(body)) {
                body = null;
            }
            else if (!WikiContentHelper.isKnownContent(body)) {
                // get the 'cleaned' version of the body
                body = JAXPUtils.toXmlString(WikiContentHelper.unknownContentToJiveDoc(body));
            }

            this.internalDocID = rs.getLong("internalDocID");
            this.subject = StringUtils.trimToEmpty(rs.getString("title"));
            this.summary = RenderUtils.convertTextForSearch(rs.getString("summary"));
            this.bodyID = rs.getLong("bodyID");
            this.dbBody = body != null ? RenderUtils.convertTextForSearch(body) + helper.getHRefsFromAnchorsWithDifferentTitles(JAXPUtils.toXmlDocument(body)) : null;
            this.creationDate = rs.getLong("creationDate");
            this.modificationDate = rs.getLong("modificationDate");
            this.containerType = rs.getInt("containerType");
            this.containerID = rs.getLong("containerID");
            this.documentID = rs.getString("documentID");

            long userID = rs.getLong("userID");
            String username = rs.getString("username");
            users.add(new DocUser(userID, username));

            this.tags = Lists.newArrayList();
            this.attachments = Sets.newHashSet();

            this.outcomes = Lists.newArrayList();
        }

        @Override
        public long getObjectID() {
            return internalDocID;
        }

        @Override
        public boolean isAttachmentIDsAvailable() {
            return documentObject == null;
        }

        @Override
        public Set<Long> getAttachmentIDs() {
            if (documentObject == null) {
                return attachments;
            }
            return Collections.emptySet();
        }

        @Override
        public AttachmentContentResource getAttachmentContentResource() {
            return documentObject;
        }
    }

    private static class DocUser {
        private final long userID;
        private final String username;

        private DocUser(long userID, String username) {
            this.userID = userID;
            this.username = username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DocUser that = (DocUser) o;

            if (userID != that.userID) {
                return false;
            }
            if (username != null ? !username.equals(that.username) : that.username != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (userID ^ (userID >>> 32));
            result = 31 * result + (username != null ? username.hashCode() : 0);
            return result;
        }
    }

    private static class DocCollaborator implements SearchDataObject {
        private final long internalDocID;
        private final long userID;
        private final String username;

        private DocCollaborator(long internalDocID, long userID, String username) {
            this.internalDocID = internalDocID;
            this.userID = userID;
            this.username = username;
        }

        @Override
        public long getObjectID() {
            return internalDocID;
        }
    }
}
