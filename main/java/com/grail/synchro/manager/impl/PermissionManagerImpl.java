package com.grail.synchro.manager.impl;

import java.util.List;

import com.grail.synchro.SynchroConstants;
import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.aaa.AuthenticationProvider;
import org.apache.log4j.Logger;

import com.grail.synchro.beans.SynchroPermissionLevelBean;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;

/**
 * Implementation
 *
 * @author: Vivek Kondur
 * @see PermissionManager
 * @see SynchroPermissionLevelBean
 */
public class PermissionManagerImpl implements PermissionManager {

    private static final Logger LOG = Logger.getLogger(PermissionManagerImpl.class);

    private GroupManager groupManager;
    private AuthenticationProvider authenticationProvider;


    private SynchroUtils synchroUtils;

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public boolean isSynchroUser(final User user) {
        boolean synchroUser = false;
       if(SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroAdmin(user)) {
            synchroUser =  true;
        } else {
           try {
               Group group = groupManager.getGroup(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME.toString());
               synchroUser = group.isMember(user);
           } catch (GroupNotFoundException e) {
               e.printStackTrace();
           }
        }
        return synchroUser;
    }

    @Override
    public boolean isRKPUser(User user) {
        boolean rkpUser = false;
        if(this.authenticationProvider.isSystemAdmin()) {
            rkpUser =  true;
        } else {
            try {
                Group group = groupManager.getGroup(SynchroConstants.JIVE_RKP_GROUP_NAME.toString());
                rkpUser = group.isMember(user);
            } catch (GroupNotFoundException e) {
                e.printStackTrace();
            }
        }
        return rkpUser;
    }

    public void setSynchroUtils(final SynchroUtils synchroUtils) {
        this.synchroUtils = synchroUtils;
    }

}
