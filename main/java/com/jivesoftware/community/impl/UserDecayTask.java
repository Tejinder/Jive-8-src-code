/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.impl.dao.UserDAO;
import com.jivesoftware.community.util.BasePermHelper;
import com.jivesoftware.util.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;
import java.util.List;

/**
 * Task that will disable users that have not logged in for a year or more.
 */
public class UserDecayTask implements Runnable {

    public static final String USER_DECAY_PERIOD_PROPERTY = "user.decay.period";

    public static final int USER_DECAY_PERIOD_DEFAULT = 12;

    private static final Logger log = Logger.getLogger(UserDecayTask.class);

    protected UserManager userManager;
    protected UserDAO userDAO;
    protected int decayPeriodMonths = USER_DECAY_PERIOD_DEFAULT;

    @Required
    public final void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Required
    public final void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * This value can be overriden by jivePropertyOverrideMap. The number of months of inactivity, determined by the
     * last time a user logged in, after which they will be disabled. Specifying 0 for this value means that users will
     * never be disabled for inactivity.
     *
     * @param decayPeriodMonths the number of months of inactivity, determined by the last time a user logged in, after
     * which they will be disabled.
     */
    @Required
    public final void setDecayPeriodMonths(int decayPeriodMonths) {
        this.decayPeriodMonths = decayPeriodMonths;
    }

    public void run() {
        Date cutoffDate = getUserDecayPeriod();
        if (cutoffDate == null) {
            return;
        }

        List<Long> list = userDAO.getUserIDsOfLastLoggedInBefore(cutoffDate);

        int disabledCount = 0;
        for (Long userID : list) {
            try {
                disabledCount = decayUser(userID) ? disabledCount += 1 : disabledCount;
            }
            catch (Exception e) {
                // should never happen
                log.error(String.format(
                        "An error occurred determining if a user, with ID %d, should be decayed due to inactivity.",
                        userID), e);
            }
        }
        log.info(String.format("%d users have been inactive since %s; They will be disabled.", disabledCount,
                cutoffDate));
    }

    protected Date getUserDecayPeriod() {
        int monthsAgo = decayPeriodMonths;
        if (monthsAgo == 0) {
            log.info(USER_DECAY_PERIOD_PROPERTY + " is set to 0; No users will be disabled due to inactivity.");
            return null;
        }
        return DateUtils.monthsBefore(monthsAgo < 0 ? 12 : monthsAgo, DateUtils.now().getTime());
    }

    protected boolean decayUser(Long userID) throws UserNotFoundException {
        User user = userManager.getUser(userID);

        // only disable users who are NOT system administrators
        if (BasePermHelper.isSystemAdmin(user)) {
            return false;
        }

        userManager.disableUser(user);
        if (log.isDebugEnabled()) {
            log.debug(String.format("User with id %d has been disabled due to inactivity.", userID));
        }
        return true;
    }
}
