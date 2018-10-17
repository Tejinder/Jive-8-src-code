package com.grail.synchro.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.dao.EmailNotificationDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;

/**
 * @author Tejinder
 * @version 1.0
 */
public class EmailNotificationDAOImpl extends JiveJdbcDaoSupport implements EmailNotificationDAO {
    private static final Logger LOG = Logger.getLogger(AutoSaveDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static final String FIELDS = "id, projectid, endmarketid, agencyid, stageid, emailtype, emaildesc, emailtime, emailsubject, emailsender, emailrecipients";
    private static final String INSERT_EMAIL_NOTIFICATION_DETAILS = "INSERT INTO grailEmailNotification (" + FIELDS + ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

   

    @Override
    public void saveDetails(final EmailNotificationDetailsBean bean){
        try{
            Long id = synchroDAOUtil.nextSequenceID("id", "grailEmailNotification");
            bean.setId(id);
            getSimpleJdbcTemplate().getJdbcOperations().update(INSERT_EMAIL_NOTIFICATION_DETAILS,
                    bean.getId(),
                    bean.getProjectID(),
                    bean.getEndmarketID(),
                    bean.getAgencyID(),
                    bean.getStageID(),
                    bean.getEmailType(),
                    bean.getEmailDesc(),
                    System.currentTimeMillis(),
                    bean.getEmailSubject(),
                    bean.getEmailSender(),
                    bean.getEmailRecipients()
            );
        } catch (DataAccessException e) {
            LOG.error(e.getCause());
            
        }
    }


    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
