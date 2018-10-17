package com.grail.synchro.manager.impl;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.dao.EmailNotificationDAO;
import com.grail.synchro.exceptions.AutoSavePersistException;
import com.grail.synchro.manager.EmailNotificationManager;

/**
 * @author Tejinder
 * @version 1.0
 */
public class EmailNotificationManagerImpl implements EmailNotificationManager {
    private static final Logger LOG = Logger.getLogger(EmailNotificationManagerImpl.class);

    private EmailNotificationDAO emailNotificationDAO;

    @Override
    @Transactional(readOnly = false, rollbackFor = AutoSavePersistException.class)
    public void saveDetails(EmailNotificationDetailsBean bean)
    {
    	emailNotificationDAO.saveDetails(bean);
    }

	public EmailNotificationDAO getEmailNotificationDAO() {
		return emailNotificationDAO;
	}

	public void setEmailNotificationDAO(EmailNotificationDAO emailNotificationDAO) {
		this.emailNotificationDAO = emailNotificationDAO;
	}
            


}
