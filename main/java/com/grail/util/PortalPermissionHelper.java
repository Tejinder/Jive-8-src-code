package com.grail.util;

import com.grail.synchro.SynchroConstants;
import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.aaa.AuthenticationProvider;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.util.BasePermHelper;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class PortalPermissionHelper extends BasePermHelper {
    private static Logger LOG = Logger.getLogger(PortalPermissionHelper.class);

    private static GroupManager groupManager;

    /**
     *
     * @return
     */
 /*   public static GroupManager getGroupManager() {
        if(groupManager == null){
            groupManager = JiveApplication.getContext().getSpringBean("groupManager");
        }
        return groupManager;
    }
   */ 
    static GroupManager getGroupManager() {
        if (groupManager != null) {
            return groupManager;
        }
        groupManager = JiveApplication.getContext().getGroupManager();
        return groupManager;
    }

    /**
     *
     * @param user
     * @return
     */
    private static Iterable<Group> getGroups(final User user) {
        if(user != null) {
            return getGroupManager().getUserGroups(user);
        }
        return null;
    }

    /**
     *
     * @param user
     * @return
     */
    public static boolean hasRKPAccess(final User user) {
        Group rkpGroup = null;
        try {
            rkpGroup = getGroupManager().getGroup(BATConstants.JIVE_RKP_GROUP_NAME);
        } catch (GroupNotFoundException e) {
            LOG.error(e.getMessage());
        }
        return hasAccess(rkpGroup, user);
    }

    /**
     *
     * @param user
     * @return
     */
    public static boolean hasSynchroAccess(final User user) {
        Group synchroGroup = null;
        try {
            synchroGroup = getGroupManager().getGroup(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME);
        } catch (GroupNotFoundException e) {
            LOG.error(e.getMessage());
        }
        return hasAccess(synchroGroup, user);
    }

    /**
     *
     * @param group
     * @param user
     * @return
     */
    private static boolean hasAccess(final Group group, final User user) {
        boolean hasAccess = false;
        if(isSystemAdmin(user) || isUserAdmin(user)) {
           hasAccess = true;
        } else if(group != null) {
            Iterable<Group> groups = getGroups(user);
            while(groups.iterator().hasNext()) {
                Group grp = groups.iterator().next();
                if(grp != null && grp.equals(group)) {
                    hasAccess = true;
                    break;
                }
            }
        } else {
            hasAccess = false;
        }
        return hasAccess;
    }


}
