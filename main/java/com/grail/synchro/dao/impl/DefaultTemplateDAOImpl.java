package com.grail.synchro.dao.impl;

import com.grail.synchro.dao.DefaultTemplateDAO;
import com.grail.synchro.object.DefaultTemplate;
import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.objecttype.DefaultTemplateObjectType;
import com.grail.util.FileSizeUtils;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/1/14
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplateDAOImpl extends JiveJdbcDaoSupport implements DefaultTemplateDAO {

    private static Logger LOG = Logger.getLogger(DefaultTemplateDAOImpl.class);

    private static final String SELECT_FIELDS = "attachmentid,filename, filesize,objectid, contenttype, creationdate";
    private static final String GET_TEMPLATES = "SELECT " + SELECT_FIELDS + " FROM jiveattachment where objecttype = ?";
    private static final String GET_TEMPLATES_BY_ID = "SELECT " + SELECT_FIELDS + " FROM jiveattachment where objecttype = ? AND objectid = ?";

    private static final String GET_TEMPLATE = "SELECT " + SELECT_FIELDS + " FROM jiveattachment where objecttype = ? AND objectid = ?";

    private static final String GET_TEMPLATE_BY_ATTACHMENT_ID = "SELECT " + SELECT_FIELDS + " FROM jiveattachment where attachmentid = ?";


    @Override
    public DefaultTemplate get(final Long id) {
        List<DefaultTemplate> templates = null;
        try {
            templates = getSimpleJdbcTemplate().getJdbcOperations().query(GET_TEMPLATE, myTemplatesRowMapper, DefaultTemplateObjectType.DEFAULT_TEMPLATE_OBJECT_TYPE_ID, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        if(templates != null && templates.size() > 0) {
            return templates.get(0);
        } else {
           return null;
        }
    }

    @Override
    public DefaultTemplate getByAttachmentId(Long attachmentId) {
        DefaultTemplate template = null;
        try {
            template = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_TEMPLATE_BY_ATTACHMENT_ID, myTemplatesRowMapper, attachmentId);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return template;
    }

    @Override
    public List<DefaultTemplate> getAll() {
        List<DefaultTemplate> defaultTemplates = null;
        try {
            defaultTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(GET_TEMPLATES, myTemplatesRowMapper, DefaultTemplateObjectType.DEFAULT_TEMPLATE_OBJECT_TYPE_ID);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return defaultTemplates;
    }


    @Override
    public List<DefaultTemplate> getAllById(final Long id) {
        List<DefaultTemplate> defaultTemplates = null;
        try {
            defaultTemplates = getSimpleJdbcTemplate().getJdbcOperations().query(GET_TEMPLATES_BY_ID, myTemplatesRowMapper,
                    DefaultTemplateObjectType.DEFAULT_TEMPLATE_OBJECT_TYPE_ID, id);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage());
        }
        return defaultTemplates;
    }

    private final ParameterizedRowMapper<DefaultTemplate> myTemplatesRowMapper
            = new ParameterizedRowMapper<DefaultTemplate>() {
        public DefaultTemplate mapRow(ResultSet rs, int row) throws SQLException {
            DefaultTemplate document = new DefaultTemplate();
            document.getBean().setId(rs.getLong("objectid"));
            document.getBean().setAttachmentId(rs.getLong("attachmentId"));
            document.getBean().setContentType(rs.getString("contentType"));
            document.getBean().setFileName(rs.getString("filename"));
            document.getBean().setFileSize(FileSizeUtils.format(rs.getLong("filesize")));
            return document;
        }
    };



    @Override
    public boolean save(final DefaultTemplate defaultTemplate) {
        return false;
    }

    @Override
    public boolean update(final DefaultTemplate defaultTemplate) {
        return false;
    }

    @Override
    public boolean delete(final Long id) {
        return false;
    }
}
