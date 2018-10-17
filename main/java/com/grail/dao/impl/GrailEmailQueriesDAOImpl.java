package com.grail.dao.impl;

import com.grail.beans.GrailEmailQuery;
import com.grail.dao.GrailEmailQueriesDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/4/14
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailEmailQueriesDAOImpl extends JiveJdbcDaoSupport implements GrailEmailQueriesDAO {
    private static final Logger LOG = Logger.getLogger(GrailEmailQueriesDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static final String TABLE_NAME = "grailemailqueries";

    private static final String FIELDS = "recipients, subject, body, attachmentid, sender, creationdate, emailsent, type";

    private static final String INSERT = "INSERT INTO " + TABLE_NAME + "(id, "+ FIELDS +") VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE = "UPDATE " + TABLE_NAME + " SET "+ FIELDS.replaceAll(",", "=?,") + "=?" + " WHERE id=?";
    private static final String UPDATE_ATTACHMENT_ID =  "UPDATE " + TABLE_NAME + " SET  attachmentid = ? WHERE id=?";
    private static final String UPDATE_EMAIL_STATUS =  "UPDATE " + TABLE_NAME + " SET emailsent = ? WHERE id=?";

    @Override
    public Long saveQuery(final GrailEmailQuery emailQuery) {
        try {
            Long id = synchroDAOUtil.nextSequenceID("id", TABLE_NAME);
            emailQuery.setId(id);
            getSimpleJdbcTemplate().getJdbcOperations().update(INSERT,
                    emailQuery.getId(),
                    emailQuery.getRecipients(),
                    emailQuery.getSubject(),
                    emailQuery.getBody(),
                    emailQuery.getAttachmentId(),
                    emailQuery.getSender(),
                    emailQuery.getCreationDate() != null?emailQuery.getCreationDate().getTime():new Date().getTime(),
                    emailQuery.getEmailSent()?1:0,
                    emailQuery.getType()
            );

            return id;
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Long updateQuery(final GrailEmailQuery emailQuery) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE,
                    emailQuery.getRecipients(),
                    emailQuery.getSubject(),
                    emailQuery.getBody(),
                    emailQuery.getAttachmentId(),
                    emailQuery.getSender(),
                    emailQuery.getCreationDate() != null?emailQuery.getCreationDate().getTime():new Date().getTime(),
                    emailQuery.getEmailSent()?1:0,
                    emailQuery.getType(),
                    emailQuery.getId()
            );
            return emailQuery.getId();
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void updateAttachment(final Long id, final Long attachmentId) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_ATTACHMENT_ID,attachmentId,id);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void updateEmailStatus(final Long id, final Boolean status) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_EMAIL_STATUS,status?1:0,id);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
    }

    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
