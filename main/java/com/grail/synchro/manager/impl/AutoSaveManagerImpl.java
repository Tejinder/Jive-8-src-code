package com.grail.synchro.manager.impl;

import com.grail.synchro.beans.AutoSaveDetailsBean;
import com.grail.synchro.dao.AutoSaveDAO;
import com.grail.synchro.exceptions.AutoSaveDetailsNotFoundException;
import com.grail.synchro.exceptions.AutoSavePersistException;
import com.grail.synchro.manager.AutoSaveManager;
import com.jivesoftware.base.User;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class AutoSaveManagerImpl implements AutoSaveManager {
    private static final Logger LOG = Logger.getLogger(AutoSaveManagerImpl.class);

    private AutoSaveDAO autoSaveDAO;

    @Override
    @Transactional(readOnly = false, rollbackFor = AutoSavePersistException.class)
    public void saveDetails(final String objectType, final Long objectID, final String data)
            throws AutoSavePersistException,AutoSaveDetailsNotFoundException {
        if(objectType != null && StringUtils.isNotBlank(data)) {
            AutoSaveDetailsBean bean = new AutoSaveDetailsBean();
            bean.setObjectType(new Long(objectType.hashCode()));
            bean.setDetails(data);
            User user = getUser();
            bean.setUserID(user.getID());
            if(objectID == null) {
                bean.setDraft(true);
                bean.setObjectID(getObjectID(objectType, user.getID()));
            } else {
                bean.setDraft(false);
                bean.setObjectID(objectID);
            }
            try {
                this.saveDetails(bean);
            } catch (AutoSavePersistException e) {
                throw new AutoSavePersistException("Unable to save auto-save details. Please try again", e.getCause());
            } catch (AutoSaveDetailsNotFoundException e) {
                LOG.error(e.getCause());
            }
        } else {
            throw new AutoSavePersistException("Incorrect details to save auto-save details. Please try again");
        }
    }

    private Long getObjectID(final String objectType, final Long userID) {
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(objectType).append("-").append(userID);
        return new Long(idBuilder.toString().hashCode());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = AutoSavePersistException.class)
    public void saveDetails(final AutoSaveDetailsBean bean) throws AutoSavePersistException,AutoSaveDetailsNotFoundException {

        if(bean != null) {
            AutoSaveDetailsBean existing = null;
            try {
                existing = autoSaveDAO.getDetails(bean.getObjectType(), bean.getObjectID(), bean.getUserID());
            } catch (AutoSaveDetailsNotFoundException e) {
                LOG.error(e.getCause());
            }
            try {

                if(existing != null) {
                    bean.setId(existing.getId());
                    autoSaveDAO.updateDetails(bean);
                } else {
                    autoSaveDAO.saveDetails(bean);
                }
            } catch (AutoSavePersistException e) {
                throw new AutoSavePersistException("Unable to save auto-save details. Please try again", e.getCause());
            }
        } else {
            throw new AutoSavePersistException("Incorrect details.");
        }

    }


    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public AutoSaveDetailsBean getDetails(final String objectType, final Long objectID)
            throws AutoSaveDetailsNotFoundException {
        Long id = null;
        if(objectID == null) {
            id = getObjectID(objectType, getUser().getID());
        } else {
            id = objectID;
        }
        return getDetails(new Long(objectType.hashCode()), id, getUser().getID());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public AutoSaveDetailsBean getDetails(final Long objectType, final Long objectID, final Long userID)
            throws AutoSaveDetailsNotFoundException {
        return autoSaveDAO.getDetails(objectType, objectID, userID);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public AutoSaveDetailsBean getDraftDetails(final Long userID) throws AutoSaveDetailsNotFoundException {
        return autoSaveDAO.getDraftDetails(userID, true);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public boolean deleteDetails(final String objectType, final Long objectID) {
        Long id = null;
        if(objectID == null) {
            id = getObjectID(objectType, getUser().getID());
        } else {
            id = objectID;
        }
        return deleteDetails(new Long(objectType.hashCode()), id, getUser().getID());
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public boolean deleteDetails(final Long objectType, final Long objectID, final Long userID) {
        return autoSaveDAO.deleteDetails(objectType, objectID, userID);
    }

    private User getUser() {
        return JiveApplication.getContext().getAuthenticationProvider().getJiveUser();
    }

    public AutoSaveDAO getAutoSaveDAO() {
        return autoSaveDAO;
    }

    public void setAutoSaveDAO(AutoSaveDAO autoSaveDAO) {
        this.autoSaveDAO = autoSaveDAO;
    }


}
