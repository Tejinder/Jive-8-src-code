package com.grail.synchro.manager;

import com.jivesoftware.base.User;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/28/14
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SynchroUserManager {
    User getUserByEmail(final String email);
    boolean isUserPropExists(final Long userId, final String userProp);
}
