package com.grail.synchro.objecttype;

import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.impl.BaseEntitlementProvider;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.objecttype.EntitlementCheckProvider;

/**
 *
 */
public class SynchroAttachmentEntitlementCheckProvider extends BaseEntitlementProvider<SynchroAttachment> implements EntitlementCheckProvider<SynchroAttachment> {

    public boolean isUserEntitled(SynchroAttachment object, User user) {
//        //TODO: Need to update once permission handling is been implemented
//        if(isUserDisabled(user) || !SynchroPermHelper.isSynchroUser(user)) {
//            return false;
//        }
        return true;
    }

    public boolean isUserEntitled(SynchroAttachment object) {
        //TODO: Need to update once permission handling is been implemented
//        if(isUserDisabled(getEffectiveUser()) || !SynchroPermHelper.isSynchroUser(getEffectiveUser())) {
//            return false;
//        }
        return true;
    }

    @Override
    public boolean isUserEntitled(SynchroAttachment object, EntitlementTypeProvider.Type type) {
        //TODO: Need to update once permission handling is been implemented
//        if(isUserDisabled(getEffectiveUser()) || !SynchroPermHelper.isSynchroUser(getEffectiveUser())) {
//            return false;
//        }
        return true;
    }

    @Override
    public boolean isUserEntitled(User user, JiveContainer container, EntitlementTypeProvider.Type type) {
//        if(isAnyNull(user, container, type)) {
//            return false;
//        }
//        if(isUserDisabled(user) || isBannedFromPosting(user)) {
//            return false;
//        }
        return true;
    }

    @Override
    public boolean isUserEntitled(User user, SynchroAttachment object, EntitlementTypeProvider.Type type) {
//        if(isAnyNull(user, object, type)) {
//            return false;
//        }
//
//        if(isUserDisabled(user) || isBannedFromPosting(user)) {
//            return false;
//        }
        return true;
    }

//    private GroupManager groupManager;
//
//    public GroupManager getProjectEvaluationManager() {
//        if(groupManager == null) {
//            return JiveApplication.getContext().getSpringBean("groupManagerImpl");
//        }
//        return groupManager;
//    }

}
