/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl.dao;

import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.DocumentResultFilter;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.DocumentType;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObjectType;
import com.jivesoftware.community.ResultFilter;
import com.jivesoftware.community.impl.CachedPreparedStatement;
import com.jivesoftware.community.impl.dao.sql.SQLHelper;
import com.jivesoftware.community.internal.ExtendedCommunityManager;
import com.jivesoftware.community.util.NumberUtils;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.jivesoftware.community.JiveConstants.CREATION_DATE;
import static com.jivesoftware.community.JiveConstants.DOCUMENT_FIELD;
import static com.jivesoftware.community.JiveConstants.DOCUMENT_STATE;
import static com.jivesoftware.community.JiveConstants.DOCUMENT_TITLE;
import static com.jivesoftware.community.JiveConstants.DOCUMENT_TYPE;
import static com.jivesoftware.community.JiveConstants.EXPIRATION_DATE;
import static com.jivesoftware.community.JiveConstants.EXTENDED_PROPERTY;
import static com.jivesoftware.community.JiveConstants.HOUR;
import static com.jivesoftware.community.JiveConstants.MODIFICATION_DATE;
import static com.jivesoftware.community.JiveConstants.RATING;

/**
 * Document version data access object.
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class DbDocumentVersionDAO extends JiveJdbcDaoSupport implements DocumentVersionDAO {

    private static final Logger log = Logger.getLogger(DbDocumentVersionDAO.class);

    private static final String LOAD_DOCUMENT_VERSION_BY_COMPOSITE_KEY =
            "SELECT versionID, state, userID, creationDate, modificationDate, recordID, internalDocID, exStorageFileVersionID FROM jiveDocVersion " +
            "WHERE internalDocID = ? AND versionID = ?";

    private static final String UPDATE_DOCUMENT_VERSION =
            "UPDATE jiveDocVersion SET userID = ?, creationDate = ?, modificationDate = ?, state = ? " +
            "WHERE internalDocID = ? AND versionID = ?";
    private static final String UPDATE_DOCUMENT_VERSION_STATE =
            "UPDATE jiveDocVersion SET state = 'archived' WHERE internalDocID = ? AND state = 'published' " +
            "AND versionID <> ?";
    private static final String DELETE_DOCUMENT_VERSION =
            "DELETE FROM jiveDocVersion WHERE internalDocID = ? AND versionID = ?";

     private static final String LOAD_VERSIONS =
            "SELECT versionID, state, userID, creationDate, modificationDate, recordID, internalDocID, exStorageFileVersionID FROM jiveDocVersion "
                    + "WHERE internalDocID = ? ORDER BY versionID DESC";

    private static final String LOAD_VERSION_BY_ID =
            "SELECT versionID, state, userID, creationDate, modificationDate, recordID, internalDocID, exStorageFileVersionID FROM jiveDocVersion "
                    + "WHERE recordID = ?";

    private static final String LOAD_DOC_VERSION_COMMENTS =
            "SELECT versionID, vCommentID, userID, creationDate, comments, internalDocID FROM jiveDocVersionCmmt "
                    + "WHERE internalDocID = ?";

    private ExtendedCommunityManager communityManager;

    @Required
    public final void setCommunityManager(ExtendedCommunityManager communityManager) {
        this.communityManager = communityManager;
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void update(DocumentVersionBean bean) throws DAOException {
        try {
            if ("published".equals(bean.getState())) {
                getSimpleJdbcTemplate().update(UPDATE_DOCUMENT_VERSION_STATE, bean.getDocID(), bean.getVersionID());
            }

            getSimpleJdbcTemplate().update(UPDATE_DOCUMENT_VERSION, bean.getAuthorID(), bean.getCreationDate(),
                    bean.getModificationDate(), bean.getState(), bean.getDocID(), bean.getVersionID());
        }
        catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new DAOException(e);
        }
    }

    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void delete(DocumentVersionBean bean) throws DAOException {
        try {
            getSimpleJdbcTemplate().update(DELETE_DOCUMENT_VERSION, bean.getDocID(), bean.getVersionID());
        }
        catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new DAOException(e);
        }
    }

    public DocumentVersionBean getByID(long docID, int versionID) throws DAOException {
        List<DocumentVersionBean> versions = template().query(LOAD_DOCUMENT_VERSION_BY_COMPOSITE_KEY, new VersionBeanMapper(), docID, versionID);

         if (versions.size() > 1) {
            throw new IllegalStateException("More than one DocumentVersion with the doc ID: " + docID + " and versionID " + versionID);
        } else if (versions.isEmpty()) {
            return null;
        }

        DocumentVersionBean bean = versions.get(0);

        template().query(LOAD_DOC_VERSION_COMMENTS, new VersionCommentBeanMapper(versions), bean.getDocID());

        return bean;
    }

    public DocumentVersionBean getByID(long ID) throws DAOException {
        List<DocumentVersionBean> versions = template().query(LOAD_VERSION_BY_ID, new VersionBeanMapper(), ID);

         if (versions.size() > 1) {
            throw new IllegalStateException("More than one DocumentVersion with the same ID: " + ID);
        } else if (versions.isEmpty()) {
            return null;
        }

        DocumentVersionBean bean = versions.get(0);

        template().query(LOAD_DOC_VERSION_COMMENTS, new VersionCommentBeanMapper(versions), bean.getDocID());

        return bean;
    }

   public List<DocumentVersionBean> getVersionsByDocID(long docID) throws DAOException {

        List<DocumentVersionBean> versions = template().query(LOAD_VERSIONS, new VersionBeanMapper(), docID);

        if (versions.size() == 0) {
            return null;
        }

        template().query(LOAD_DOC_VERSION_COMMENTS, new VersionCommentBeanMapper(versions), docID);

        return versions;
    }

    /**
     * Returns the SQL statement corresponding to a ResultFilter for documents.
     *
     * @param filter the result filter to build the SQL from.
     * @param countQuery true if this should be a count query.
     * @return the SQL statement corresponding to a ResultFilter for documents.
     */
    @SuppressWarnings("unchecked")
    public CachedPreparedStatement getDocumentVersionListSQL(DocumentResultFilter filter, boolean countQuery) {
        return getDocumentVersionListSQL(null, filter, countQuery);
    }

    /**
     * Returns the SQL statement corresponding to a ResultFilter for documents.
     *
     * @param container the container to get documents from
     * @param filter the result filter to build the SQL from.
     * @param countQuery true if this should be a count query.
     * @return the SQL statement corresponding to a ResultFilter for documents.
     */
    @SuppressWarnings("unchecked")
    public CachedPreparedStatement getDocumentVersionListSQL(JiveContainer container, DocumentResultFilter filter,
            boolean countQuery)
    {
        int sortField = filter.getSortField();

        boolean isRoot = container == null || (container.getObjectType() == JiveConstants.COMMUNITY
                && container.getID() == communityManager.getRootCommunity().getID());

        // Make sure the sort field is valid.
        if (!countQuery && !SQLHelper.validateSortField(filter, CREATION_DATE, MODIFICATION_DATE, EXPIRATION_DATE, DOCUMENT_TITLE,
                DOCUMENT_TYPE, DOCUMENT_STATE, DOCUMENT_FIELD, RATING, EXTENDED_PROPERTY))
        {
            throw new IllegalArgumentException("The specified sort field is not valid.");
        }

        CachedPreparedStatement pstmt = new CachedPreparedStatement();
        StringBuilder query = new StringBuilder(160);
        if (!countQuery) {
            if (filter.isRetrieveContainerInfo()) {
                query.append("SELECT d.internalDocID AS objectID,").append(JiveConstants.DOCUMENT)
                        .append(" AS objectType, d.containerType, d.containerID");
            }
            else {
                query.append("SELECT d.internalDocID AS internalDocID");
            }
        }
        else {
            query.append("SELECT count(d.internalDocID) AS internalDocID");
        }

        boolean filterUser = filter.getUserID() != ResultFilter.NULL_INT;
        boolean filterDocumentType = filter.getDocumentTypes() != null;
        boolean filterCreationDate = filter.getCreationDateRangeMin() != null
                || filter.getCreationDateRangeMax() != null;
        boolean filterModificationDate = filter.getModificationDateRangeMin() != null
                || filter.getModificationDateRangeMax() != null;
        boolean filterExpirationDate = filter.getExpirationDateRangeMin() != null
                || filter.getExpirationDateRangeMax() != null;
        boolean expirationEnabled = JiveGlobals.getJiveBooleanProperty("documentExpiration.enabled", true);
        boolean onlyIncludeCollaborativeDocuments = filter.isOnlyIncludeCollaborativeDocuments();
        boolean filterContentTypes = filter.getBinaryBodyContentTypeCount() > 0;
        boolean searchBody = filter.isSearchBody();

        int documentFieldCount = filter.getDocumentFieldCount();

        // SELECT -- need to add value that we sort on
        if (!countQuery) {
            switch (sortField) {
                case DOCUMENT_TITLE:
                    query.append(", v.title AS vTitle");
                    break;
                case MODIFICATION_DATE:
                    query.append(", v.modificationDate AS vModDate");
                    if(filter.isFromMostRatedWidget() || filter.isFromMostViewedWidget()){
                        query.append(", d.meanRating AS dMeanRating");
                        query.append(", c.viewCount AS viewCount");
                        query.append(",   s.score AS score");
                    }
                    break;
                case EXPIRATION_DATE:
                    if (expirationEnabled) {
                        query.append(", d.expirationDate AS dExprDate");
                    }
                    break;
                case CREATION_DATE:
                    query.append(", d.creationDate AS dCreateDate");
                    break;
                case EXTENDED_PROPERTY:
                    query.append(", propTable.propValue AS propTablePropValue");
                    break;
                case RATING:
                    query.append(", d.meanRating AS dMeanRating");
                    // This is required so that the generated sql
                    // is properly formatted for db's such as SQLServer 2000
                    query.append(", v.modificationDate AS vModDate");
                    if(filter.isFromMostRatedWidget()){

                        query.append(", c.viewCount AS viewCount");
                        query.append(",   s.score as score");

                    }

                    break;
                case DOCUMENT_TYPE:
                    query.append(", jiveDocType.sortOrder AS docTypeSortOrder");
                    break;
                case DOCUMENT_FIELD:
                    query.append(", fo.optionValue AS fieldOptionValue");
                    break;
            }
        }

        // FROM -- values
        query.append(" FROM ");
        if (filter.isRecursive() && !isRoot && container.getObjectType() == JiveConstants.COMMUNITY) {
            query.append("jiveCommunity c, ");
        }
        query.append("jiveDocument d, jiveDocVersion v");
        
        if(filter.isFromMostRatedWidget() || filter.isFromMostViewedWidget()){
            query.append(",jiveViewCount c");

            query.append(",(select distinct j.internalDocID As docObjectId,  COALESCE((sum(cast(r.score as float)) / count(r.objectID)) ,0) AS score ");
            //query.append(",(select distinct j.internalDocID As docObjectId,  COALESCE((sum(r.score) / count(r.objectID)) ,0) AS score ");
            query.append(" From jiveDocument j left join jiveacclaim r on j.internalDocID = r.objectID ");
            query.append(" group by j.internalDocID) s ");
        }
        
        if (!countQuery) {
            if (sortField == DOCUMENT_FIELD) {
                query.append(", jiveFieldOption fo");
                query.append(", jiveFieldValue fv");
            }
            if (sortField == EXTENDED_PROPERTY) {
                query.append(", jiveDocumentProp propTable");
            }
            if (sortField == DOCUMENT_TYPE) {
                query.append(", jiveDocType");
            }
        }

        if (onlyIncludeCollaborativeDocuments || filterContentTypes || searchBody) {
            query.append(", jiveDocumentBody jdb, jiveDocBodyVersion jdbv");
        }

        // WHERE BLOCK
        if (isRoot) {
            query.append(" WHERE 1=1"); // 1=1 will get thrown out and makes later logic simpler.
        }
        else {
            if (!filter.isRecursive() || !(container instanceof Community)) {
                query.append(" WHERE d.containerID = ?");
                pstmt.addLong(container.getID());
                query.append(" AND d.containerType = ?");
                pstmt.addInt(container.getObjectType());
            }
            else {
                query.append(" WHERE d.containerID = c.communityID");
                query.append(" AND d.containerType = ").append(JiveConstants.COMMUNITY);
                int[] lftRgtValues = communityManager.getLftRgtValues(container.getID());
                query.append(" AND c.lft >= ?");
                pstmt.addInt(lftRgtValues[0]);
              /*  query.append(" AND c.lft < ?");
                pstmt.addInt(lftRgtValues[1]);
                query.append(" AND c.rgt > ?");
                pstmt.addInt(lftRgtValues[0]);
                */
                query.append(" AND c.rgt <= ?");
                pstmt.addInt(lftRgtValues[1]);
            }
        }
        if(filter.isFromMostRatedWidget() || filter.isFromMostViewedWidget()){
            query.append(" AND d.internalDocID = c.objectID AND c.objectType = "+JiveConstants.DOCUMENT);
            query.append(" AND d.internalDocID = s.docObjectID ");
        }

        // join doc and version tables
        query.append(" AND d.internalDocID = v.internalDocID");
        if (filter.isRestrictToLatestVersion()) {
            query.append(" AND d.versionID = v.versionID");
        }

        // state
        if (filter.getDocumentStateCount() > 0) {
            Iterator<DocumentState> states = filter.getDocumentStates();
            query.append(" AND (");
            String delim = "";
            while (states.hasNext()) {
                DocumentState state = states.next();
                query.append(delim);
                query.append("v.state = ?");
                pstmt.addString(state.getState());
                delim = " OR ";
            }
            query.append(")");
        }

        // language
        if (!filter.getLanguages().isEmpty()) {
            List<String> languages = filter.getLanguages();
            query.append(" AND (");
            String delim = "";
            for (Object language : languages) {
                query.append(delim).append("v.language = ?");
                pstmt.addString(language.toString());
                delim = " OR ";
            }
            query.append(")");
        }

        // recommended documents
        if (filter.isIncludeRecommendedDocuments() && filter.isIncludeNonRecommendedDocuments()) {
            // do nothing - will include all docs
        }
        else if (filter.isIncludeNonRecommendedDocuments()) {
            query.append(" AND d.recommended = 0");
        }
        else if (filter.isIncludeRecommendedDocuments()) {
            query.append(" AND d.recommended = 1");
        }

        // User
        if (filterUser) {
            if (filter.isIncludeAuthorsInUserFilter() || filter.isIncludeReviewersInUserFilter()) {
                query.append(" AND (");
                if (!filter.isExcludeOwnerInUserFilter()) {
                    query.append(" v.userID = ?");
                    pstmt.addLong(filter.getUserID());
                    query.append(" OR d.userID = ?");
                    pstmt.addLong(filter.getUserID());
                    query.append(" OR");
                }
                query.append(" v.internalDocID IN");
                query.append(" (SELECT jiveDocCollab.internalDocID FROM jiveDocCollab, jiveDocument");
                query.append(
                        " WHERE jiveDocCollab.internalDocID = jiveDocument.internalDocID AND (jiveDocCollab.collaboratorType = ");
                if (filter.isIncludeAuthorsInUserFilter()) {
                    query.append(String.valueOf(DbDocumentDAO.COLLABORATOR_AUTHOR));
                    if (filter.isIncludeReviewersInUserFilter()) {
                        query.append(" OR jiveDocCollab.collaboratorType = ");
                        query.append(String.valueOf(DbDocumentDAO.COLLABORATOR_REVIEWER));
                    }
                }
                else {
                    query.append(String.valueOf(DbDocumentDAO.COLLABORATOR_REVIEWER));
                }
                query.append(") AND jiveDocCollab.userID = ?");
                pstmt.addLong(filter.getUserID());
                query.append("))");
            }
            else {
                query.append(" AND v.userID = ?");
                pstmt.addLong(filter.getUserID());
            }
        }

        // doc type
        if (filterDocumentType) {
            Iterator iter = filter.getDocumentTypes();
            if (iter.hasNext()) {
                query.append(" AND (");
                String delim = "";
                while (iter.hasNext()) {
                    DocumentType type = (DocumentType) iter.next();
                    query.append(delim).append(" d.typeID = ?");
                    pstmt.addLong(type.getID());
                    delim = "OR ";
                }
                query.append(")");
            }
        }

        // doc fields
        String logicalOperator = "";
        if (documentFieldCount > 0) {
            query.append(" AND (");
        }
        for (int i = 0; i < documentFieldCount; i++) {
            query.append(logicalOperator);
            query.append("EXISTS (SELECT 1 FROM jiveFieldValue f WHERE d.internalDocID=f.internalDocID AND f.versionID=v.versionID");

            if (filter.getDocumentFieldValue(i) != null) {
                query.append(" AND f").append(i).append(".fieldID = ?");
                pstmt.addLong(filter.getDocumentField(i).getID());
                Object value = filter.getDocumentFieldValue(i);
                if (value instanceof List) {
                    List values = (List) value;
                    for (int j = 0; j < values.size(); j++) {
                        Long v = (Long) values.get(j);
                        query.append((j == 0) ? " AND (f" : " OR f");
                        query.append(i).append(".numValue = ?");
                        pstmt.addLong(v);
                    }
                    query.append(")");
                }
                else {
                    query.append(" AND f").append(i).append(".numValue = ?");
                    Long v = (Long) value;
                    pstmt.addLong(v);
                }
            }
            query.append(")"); // for EXISTS
            logicalOperator = filter.getDocumentFieldMode() == ResultFilter.OR_MODE ? " OR " : " AND ";
        }

        if (documentFieldCount > 0) {
            query.append(")");
        }

        // properties
        SQLHelper.addPropertyFilters(query, pstmt, filter, "d", "jiveDocumentProp", "internalDocID", "v.versionID = jiveDocumentProp.versionID");

        // Creation date range
        if (filterCreationDate) {
            if (filter.getCreationDateRangeMin() != null) {
                query.append(" AND v.creationDate >= ?");
                pstmt.addLong(filter.getCreationDateRangeMin().getTime());
            }
            if (filter.getCreationDateRangeMax() != null) {
                query.append(" AND v.creationDate <= ?");
                pstmt.addLong(filter.getCreationDateRangeMax().getTime());
            }
        }

        // Modification date range
        if (filterModificationDate) {
            if (filter.getModificationDateRangeMin() != null) {
                query.append(" AND v.modificationDate >= ?");
                pstmt.addLong(filter.getModificationDateRangeMin().getTime());
            }
            if (filter.getModificationDateRangeMax() != null) {
                query.append(" AND v.modificationDate <= ?");
                pstmt.addLong(filter.getModificationDateRangeMax().getTime());
            }
        }

        long now = System.currentTimeMillis();
        long expirationTimeStamp = now - (now % (HOUR * 6));

        // expiration date range
        if (filterExpirationDate && expirationEnabled) {
            if (filter.getExpirationDateRangeMin() != null) {
                query.append(" AND d.expirationDate >= ?");
                pstmt.addLong(filter.getExpirationDateRangeMin().getTime());
            }
            else {
                // only show documents which haven't expired
                query.append(" AND d.expirationDate >= ?");
                pstmt.addLong(new Date(expirationTimeStamp).getTime());
            }

            if (filter.getExpirationDateRangeMax() != null) {
                query.append(" AND d.expirationDate <= ?");
                pstmt.addLong(filter.getExpirationDateRangeMax().getTime());
            }
        }
        else if (expirationEnabled) {
            // only show documents which haven't expired
            query.append(" AND d.expirationDate >= ?");
            pstmt.addLong(new Date(expirationTimeStamp).getTime());
        }

        SQLHelper.updateWithTagFilterIfNeeded(filter, query, "d.internalDocID", JiveObjectType.Document);
        SQLHelper.updateWithTagSetFilterIfNeeded(filter, query, "d.internalDocID", JiveObjectType.Document.getKey());

        if (onlyIncludeCollaborativeDocuments) {
            query.append(" AND jdb.fileName IS NULL");
            // join doc body and version tables
            query.append(" AND jdb.bodyID = jdbv.bodyID");
            query.append(" AND jdbv.internalDocID = d.internalDocID");
            if (filter.getDocumentStateCount() > 0) {
                query.append(" AND v.versionID = jdbv.versionID");
            }
        }
        else if (filterContentTypes) {
            // join doc body and version tables
            query.append(" AND jdb.bodyID = jdbv.bodyID");
            query.append(" AND jdbv.internalDocID = d.internalDocID");
            if (filter.getDocumentStateCount() > 0) {
                query.append(" AND v.versionID = jdbv.versionID");
            }

            // add in the contentType filtering
            boolean notMode = filter.getBinaryBodyMode() == DocumentResultFilter.NOT_MODE;
            query.append(" AND (");
            String sep = " ";
            for (int i = 0; i < filter.getBinaryBodyContentTypeCount(); i++) {
                String contentType = filter.getBinaryBodyContentType(i);
                if (contentType.indexOf('*') >= 0) {
                    contentType = contentType.replace('*', '%');
                    query.append(sep).append("jdb.contentType ");
                    if (notMode) {
                        query.append("NOT ");
                    }
                    query.append("LIKE '");
                    query.append(StringUtils.escapeForSQL(contentType)).append("'");
                }
                else {
                    query.append(sep).append("jdb.contentType ");
                    if (notMode) {
                        // OpenSQL -- ensuring the empty string contentType queries use NOT NULL
                        // instead of ''.
                        if (contentType != null && "".equals(contentType.trim())) {
                            query.append("is not null");
                        }
                        else {
                            query.append("<> '");
                            query.append(StringUtils.escapeForSQL(contentType)).append("'");
                        }
                    }
                    else {
                        query.append("= '");
                        query.append(StringUtils.escapeForSQL(contentType)).append("'");
                    }
                }
                if (notMode) {
                    sep = " AND ";
                }
                else {
                    sep = " OR ";
                }
            }
            query.append(")");
        }
        
        if(searchBody)
        {
        	query.append(" AND jdb.bodytext like '%"+filter.getSearchBodyParam()+"%' ");
            // join doc body and version tables
            query.append(" AND jdb.bodyID = jdbv.bodyID");
            query.append(" AND jdbv.internalDocID = d.internalDocID");
            if (filter.getDocumentStateCount() > 0) {
                query.append(" AND v.versionID = jdbv.versionID");
            }
        }
        if (filter.getExStorageFileVersionID() != null) {
            query.append(" AND v.exStorageFileVersionID = ?");
            pstmt.addLong(filter.getExStorageFileVersionID());
        }

        // SORTING
        if (!countQuery) {
            if (sortField == EXTENDED_PROPERTY) {
                query.append(" AND d.internalDocID=propTable.internalDocID");
                query.append(" AND propTable.name = ?");
                pstmt.addString(filter.getSortPropertyName());
            }
            else if (sortField == DOCUMENT_FIELD) {
                query.append(" AND d.internalDocID = fv.internalDocID");
                query.append(" AND fv.fieldID = fo.fieldID AND ");
                query.append(" AND fo.fieldID = ?");
                pstmt.addLong(filter.getSortDocumentField().getID());
            }
            else if (sortField == DOCUMENT_TYPE) {
                query.append(" AND d.typeID=jiveDocType.typeID");
            }
        }

        // GROUP BY - we add a group by if we are getting a recursive list of documents.
        // If we do not do this we can get the same document multiple times in the result
        // of the query which isn't a desirable outcome.
        if (!countQuery && filter.isRecursive()) {
            query.append(" GROUP BY d.internalDocID");
            if (filter.isRetrieveContainerInfo()) {
                query.append(" , d.containerType, d.containerID");
            }
            switch (sortField) {
                case DOCUMENT_TITLE:
                    query.append(", v.title");
                    break;
                case MODIFICATION_DATE:
                    query.append(", v.modificationDate");
                    break;
                case EXPIRATION_DATE:
                    if (expirationEnabled) {
                        query.append(", d.expirationDate");
                    }
                    break;
                case CREATION_DATE:
                    query.append(", d.creationDate");
                    break;
                case EXTENDED_PROPERTY:
                    query.append(", propTable.propValue");
                    break;
                case RATING:
                    //query.append(", d.meanRating");
                	query.append(",score");
                    // This is required so that the generated sql
                    // is properly formatted for db's such as SQLServer 2000
                    query.append(", v.modificationDate");
                    break;
                case DOCUMENT_TYPE:
                    query.append(", jiveDocType.sortOrder");
                    break;
                case DOCUMENT_FIELD:
                    query.append(", fo.optionValue");
                    break;
            }
        }

        // ORDER BY
        if (!countQuery) {
            switch (sortField) {
                case DOCUMENT_TITLE:
                    query.append(" ORDER BY vTitle");
                    break;
                case MODIFICATION_DATE:
                	if(filter.isFromMostViewedWidget()) {
                        query.append(" ORDER BY viewCount");
                         break;
                    }  else {
                        query.append(" ORDER BY vModDate");
                        break;
                    }
                case EXPIRATION_DATE:
                    if (expirationEnabled) {
                        query.append(" ORDER BY dExprDate");
                    }
                    break;
                case CREATION_DATE:
                    query.append(" ORDER BY dCreateDate");
                    break;
                case EXTENDED_PROPERTY:
                    query.append(" ORDER BY propTablePropValue");
                    break;
                case RATING:
                	if(filter.isFromMostRatedWidget() && filter.isView()){
                        break;
                    } else {
                        query.append(" ORDER BY score");
                        break;
                    }
                case DOCUMENT_TYPE:
                    query.append(" ORDER BY docTypeSortOrder");
                    break;
                case DOCUMENT_FIELD:
                    query.append(" ORDER BY fieldOptionValue");
                    break;
            }

            if(!filter.isView()){
            	
	            if (filter.getSortOrder() == ResultFilter.DESCENDING) {
	                query.append(" DESC");
	            }
	            else {
	                query.append(" ASC");
	            }
            }

            // if we're sorting by rating, add a sort on the modificationDate as well
            if (sortField == RATING && !filter.isView()) {
                if(filter.isFromMostRatedWidget()){
                    query.append(" , viewCount");
                    if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                        query.append(" DESC");
                    }
                    else {
                        query.append(" ASC");
                    }
                }
                query.append(" , v.modificationDate");
                if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                    query.append(" DESC");
                }
                else {
                    query.append(" ASC");
                }
            }else   if (sortField == RATING && filter.isView()) {
                if(filter.isFromMostRatedWidget()){
                    query.append("  ORDER BY viewCount");
                    if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                        query.append(" DESC");
                    }
                    else {
                        query.append(" ASC");
                    }
                }
                query.append(" , score");
                if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                    query.append(" DESC");
                }
                else {
                    query.append(" ASC");
                }
                query.append(" , v.modificationDate");
                if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                    query.append(" DESC");
                }
                else {
                    query.append(" ASC");
                }
            } else if (sortField == MODIFICATION_DATE) {
                if(filter.isFromMostViewedWidget()) {
                    query.append(" , vModDate");
                    if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                        query.append(" DESC");
                    }
                    else {
                        query.append(" ASC");
                    }
                } else if(filter.isFromMostRatedWidget()){
                    query.append(" , score ");

                    if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                        query.append(" DESC");
                    }
                    else {
                        query.append(" ASC");
                    }

                    query.append(", viewCount ");
                    if (filter.getSortOrder() == ResultFilter.DESCENDING) {
                        query.append(" DESC");
                    }
                    else {
                        query.append(" ASC");
                    }
                }
            }
        }

        pstmt.setSQL(query.toString());
        return pstmt;
    }

    private class VersionBeanMapper implements RowMapper<DocumentVersionBean> {

        public DocumentVersionBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            DocumentVersionBean bean = new DocumentVersionBean();
            bean.setVersionID(rs.getInt(1));
            bean.setState(rs.getString(2));
            bean.setAuthorID(rs.getLong(3));
            bean.setCreationDate(rs.getLong(4));
            bean.setModificationDate(rs.getLong(5));
            bean.setID(rs.getLong(6));
            bean.setDocID(rs.getLong(7));
            bean.setExStorageFileVersionID(NumberUtils.convertToLong((Number) rs.getObject(8)));
            return bean;
        }
    }

    private class VersionCommentBeanMapper implements RowMapper<VersionCommentBean> {

        private List<DocumentVersionBean> versions;

        private VersionCommentBeanMapper(List<DocumentVersionBean> versions) {
            this.versions = versions;
        }

        public VersionCommentBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            VersionCommentBean bean = new VersionCommentBean();
            bean.setVersionID(rs.getInt(1));
            bean.setID(rs.getLong(2));
            bean.setAuthorID(rs.getLong(3));
            bean.setCreationDate(rs.getLong(4));
            bean.setComment(rs.getString(5));
            bean.setDocID(rs.getLong(6));

            // find the version for this bean and add it to the version bean's comment list
            for (DocumentVersionBean versionBean : versions) {
                if (versionBean.getVersionID() == bean.getVersionID()) {
                    List<VersionCommentBean> comments = versionBean.getComments();
                    if (comments == null) {
                        comments = new ArrayList<>();
                    }
                    comments.add(bean);
                    versionBean.setComments(comments);
                    break;
                }
            }

            return bean;
        }
    }
}
