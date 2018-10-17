package com.grail.synchro.manager.impl;

import com.grail.synchro.dao.SynchroUserDAO;
import com.grail.synchro.manager.SynchroUserManager;
import com.jivesoftware.base.User;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/28/14
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroUserManagerImpl implements SynchroUserManager {

    private SynchroUserDAO synchroUserDAO;

    @Override
    public User getUserByEmail(final String email) {
        return synchroUserDAO.getUserByEmail(email);
    }

    @Override
    public boolean isUserPropExists(final Long userId, final String userProp) {
        return synchroUserDAO.isUserPropExists(userId, userProp);
    }

    public SynchroUserDAO getSynchroUserDAO() {
        return synchroUserDAO;
    }

    public void setSynchroUserDAO(SynchroUserDAO synchroUserDAO) {
        this.synchroUserDAO = synchroUserDAO;
    }
}
