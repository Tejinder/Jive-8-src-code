package com.grail.synchro.dao.impl;

import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.dao.MyLibraryDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.objecttype.MyLibraryDocumentObjectType;
import com.grail.synchro.search.filter.MyLibrarySearchFilter;
import com.grail.util.FileSizeUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class MyLibraryDAOImpl extends JiveJdbcDaoSupport implements MyLibraryDAO {

    private final static Logger LOG = Logger.getLogger(MyLibraryDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static final String INSERT_DOCUMENT_FIELDS =  "id, title, description, userid";

    private static final String INSERT_DOCUMENT = "INSERT INTO grailmylibrary( " + INSERT_DOCUMENT_FIELDS + ")" +
            " VALUES (?, ?, ?, ?);";
    private static final String SELECT_FIELDS = "ml.id as id, ml.title as title, ml.description " +
            "as description, ml.userid as userId, a.attachmentid as attachmentId,a.filename as fileName, " +
            "a.filesize as fileSize, a.contenttype as contentType, a.creationdate as creationDate";
    private static final String GET_DOCUMENTS = "SELECT " + SELECT_FIELDS + " FROM jiveattachment a " +
            "INNER JOIN  grailmylibrary ml ON (ml.id = a.objectid and ml.userid = ?)";

    private static final String SELECT_WHERE_CLAUSE = " where a.objecttype = ?";

    private static final String GET_DOCUMENT_BY_ID = "SELECT " + SELECT_FIELDS + " FROM jiveattachment a " +
            "INNER JOIN  grailmylibrary ml ON (ml.id = a.objectid and ml.id = ?) where a.objecttype = ?";


    private static final String GET_TOTAL_COUNT = "SELECT count(*) FROM jiveattachment a " +
            "INNER JOIN  grailmylibrary ml ON (ml.id = a.objectid and a.objecttype = ? and ml.userid = ?)";

    private static final String REMOVE_DOCUMENT = "DELETE FROM grailmylibrary where id=?";

    /**
     *
     * @param id
     * @return
     */
    @Override
    public MyLibraryDocument get(final Long id) {
        MyLibraryDocument document = null;
        try {
            document = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_DOCUMENT_BY_ID,
                    myLibraryDocumentsRowMapper, id, MyLibraryDocumentObjectType.MY_LIBRARY_DOCUMENT_OBJECT_TYPE_ID);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            return null;
        }
        return document;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public MyLibraryDocument addDocument(final MyLibraryDocument obj) {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailmylibrary");
        try {
            obj.getBean().setId(id);
            getSimpleJdbcTemplate().getJdbcOperations().update(INSERT_DOCUMENT,
                    id,
                    obj.getBean().getTitle(),
                    obj.getBean().getDescription(),
                    obj.getBean().getUserId()
            );
            return obj;
        } catch (DataAccessException e) {
            final String message = "Failed to create new my library document - "+obj.getBean().getTitle();
            LOG.error(message, e);
        }
        return null;
    }

    /**
     *
     * @param filter
     * @return
     */
    @Override
    public List<MyLibraryDocument> getDocuments(final MyLibrarySearchFilter filter) {
        List<MyLibraryDocument> myLibraryDocuments = new ArrayList<MyLibraryDocument>();
        StringBuilder sql = new StringBuilder(GET_DOCUMENTS);
        StringBuilder where = new StringBuilder();
        where.append(SELECT_WHERE_CLAUSE);
        if(filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            where.append(" and (");
            where.append("ml.title like '%").append(filter.getKeyword()).append("%'");
            where.append(" OR ml.description like '%").append(filter.getKeyword()).append("%'");
            where.append(" OR a.filename like '%").append(filter.getKeyword()).append("%'");
            where.append(")");
        }
        sql.append(where);

        sql.append(getOrderByClause(filter.getSortField(), filter.getAscendingOrder()));

        try {
            if(filter.getStart() != null) {
                sql.append(" OFFSET ").append(filter.getStart());
            }
            if(filter.getLimit() != null && filter.getLimit() > 0) {
                sql.append(" LIMIT ").append(filter.getLimit());
            }
            myLibraryDocuments = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(),
                    myLibraryDocumentsRowMapper, filter.getUserId() , MyLibraryDocumentObjectType.MY_LIBRARY_DOCUMENT_OBJECT_TYPE_ID);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            return null;
        }
        return myLibraryDocuments;
    }

    /**
     *
     * @param sortField
     * @param order
     * @return
     */
    private String getOrderByClause(final String sortField, final Integer order) {
        StringBuilder orderBy = new StringBuilder();
        orderBy.append(" order by ");
        if(null != sortField && !sortField.equals("")) {
            String field = null;

            if(sortField.equals("title")) {
                field = "ml.title ";
            } else if(sortField.equals("description")) {
                field = "ml.description ";
            }  else if(sortField.equals("fileName")) {
                field = "a.filename ";
            } else if(sortField.equals("addedDate")) {
                field = "a.creationdate ";
            }  else if(sortField.equals("fileSize")) {
                field = "a.filesize ";
            }
            if(field != null) {
                orderBy.append(field).append(SynchroDAOUtil.getSortType(order));
            }

        } else {
            orderBy.append("a.creationdate ").append(SynchroDAOUtil.getSortType(1));
        }
        return orderBy.toString();
    }



    /**
     *
     * @param id
     * @return
     */
    @Override
    public boolean removeDocument(final Long id) {
        boolean success = false;
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_DOCUMENT, id);
            success = true;
        } catch (DataAccessException e) {
            final String message = "Failed to remove document";
            LOG.error(message, e);
            success = false;
        }
        return success;
    }

    @Override
    public List<MyLibraryDocument> getDocuments() {
        return getDocuments(null, null);
    }

    @Override
    public List<MyLibraryDocument> getDocuments(final Long userID) {
        return getDocuments(userID, null, null);
    }

    @Override
    public List<MyLibraryDocument> getDocuments(final Integer start, final Integer limit) {
        return getDocuments(null, start, limit);
    }

    @Override
    public List<MyLibraryDocument> getDocuments(final Long userID, final Integer start, final Integer limit) {
        return getDocuments(null, userID, start, limit);
    }

    @Override
    public List<MyLibraryDocument> getDocuments(final String keyword, final Long userID, final Integer start, final Integer limit) {
        List<MyLibraryDocument> myLibraryDocuments = new ArrayList<MyLibraryDocument>();
        StringBuilder sql = new StringBuilder(GET_DOCUMENTS);
        StringBuilder where = new StringBuilder();
        where.append(SELECT_WHERE_CLAUSE);

        if(keyword != null && !keyword.equals("")) {
            where.append(" and (ml.title like '%").append(keyword).append("%'");
            where.append(" OR ml.description like '%").append(keyword).append("%'");
            where.append(" OR a.filename like '%").append(keyword).append("%'");
            where.append(")");
        }
        sql.append(where);
        sql.append(" order by a.creationdate desc");

        try {
            if(start != null) {
                sql.append(" OFFSET ").append(start);
            }
            if(limit != null && limit > 0) {
                sql.append(" LIMIT ").append(limit);
            }
            if(keyword != null && !keyword.equals("")) {

            }
            myLibraryDocuments = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(),
                    myLibraryDocumentsRowMapper, userID, MyLibraryDocumentObjectType.MY_LIBRARY_DOCUMENT_OBJECT_TYPE_ID);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            return null;
        }
        return myLibraryDocuments;
    }

    private final ParameterizedRowMapper<MyLibraryDocument> myLibraryDocumentsRowMapper
            = new ParameterizedRowMapper<MyLibraryDocument>() {
        public MyLibraryDocument mapRow(ResultSet rs, int row) throws SQLException {
            MyLibraryDocument document = new MyLibraryDocument();
            document.getBean().setId(rs.getLong("id"));
            document.getBean().setTitle(rs.getString("title"));
            document.getBean().setDescription(rs.getString("description"));
            document.getBean().setUserId(rs.getLong("userId"));
            document.getBean().setCreationBy(rs.getLong("userId"));
            document.getBean().setCreationDate(rs.getLong("creationDate"));
            document.getBean().setAddedDate(new Date(rs.getLong("creationDate")));
            document.getBean().setAttachmentId(rs.getLong("attachmentId"));
            document.getBean().setContentType(rs.getString("contentType"));
            document.getBean().setFileName(rs.getString("fileName"));
            document.getBean().setFileSize(FileSizeUtils.format(rs.getLong("fileSize")));
            return document;
        }
    };


    @Override
    public Long getTotalCount(final Long userId) {
        return getTotalCount(null, userId);
    }

    @Override
    public Long getTotalCount(final String keyword, final Long userId) {
        Long count = 0L;
        StringBuilder sql = new StringBuilder(GET_TOTAL_COUNT);

        if(keyword != null && !keyword.equals("")) {
            StringBuilder where = new StringBuilder(" WHERE ");
            where.append("(");
            where.append("ml.title like '%").append(keyword).append("%'");
            where.append(" OR ml.description like '%").append(keyword).append("%'");
            where.append(" OR a.filename like '%").append(keyword).append("%'");
            where.append(")");
            sql.append(where);
        }
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString(),
                    MyLibraryDocumentObjectType.MY_LIBRARY_DOCUMENT_OBJECT_TYPE_ID, userId);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return count;
    }



    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
