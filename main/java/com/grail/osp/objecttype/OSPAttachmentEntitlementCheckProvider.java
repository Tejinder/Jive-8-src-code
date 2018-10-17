package com.grail.osp.objecttype;

import com.grail.osp.object.OSPAttachment;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.impl.BaseEntitlementProvider;
import com.jivesoftware.community.objecttype.EntitlementCheckProvider;

/**
 *
 */
public class OSPAttachmentEntitlementCheckProvider extends BaseEntitlementProvider<OSPAttachment> implements EntitlementCheckProvider<OSPAttachment> {

    public boolean isUserEntitled(OSPAttachment object, User user) {
//        //TODO: Need to update once permission handling is been implemented
//        if(isUserDisabled(user) || !SynchroPermHelper.isSynchroUser(user)) {
//            return false;
//        }
        return true;
    }

    public boolean isUserEntitled(OSPAttachment object) {
        //TODO: Need to update once permission handling is been implemented
//        if(isUserDisabled(getEffectiveUser()) || !SynchroPermHelper.isSynchroUser(getEffectiveUser())) {
//            return false;
//        }
        return true;
    }

    @Override
    public boolean isUserEntitled(OSPAttachment object, EntitlementTypeProvider.Type type) {
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
    public boolean isUserEntitled(User user, OSPAttachment object, EntitlementTypeProvider.Type type) {
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
