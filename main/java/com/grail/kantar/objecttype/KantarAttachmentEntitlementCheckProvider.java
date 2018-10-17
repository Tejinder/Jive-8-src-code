package com.grail.kantar.objecttype;

import com.grail.kantar.object.KantarAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.impl.BaseEntitlementProvider;
import com.jivesoftware.community.objecttype.EntitlementCheckProvider;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 11/6/14
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarAttachmentEntitlementCheckProvider extends BaseEntitlementProvider<KantarAttachment> implements EntitlementCheckProvider<KantarAttachment> {

    @Override
    public boolean isUserEntitled(User user, JiveContainer container, EntitlementTypeProvider.Type type) {
        if(isAnyNull(user, container, type)) {
            return false;
        }
        if(isUserDisabled(user) || isBannedFromPosting(user)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isUserEntitled(User user, KantarAttachment object, EntitlementTypeProvider.Type type) {
        if(isAnyNull(user, object, type)) {
            return false;
        }

        if(isUserDisabled(user) || isBannedFromPosting(user)) {
            return false;
        }
        return true;
    }

    public boolean isUserEntitled(KantarAttachment object, User user) {
//        //TODO: Need to update once permission handling is been implemented
//        if(isUserDisabled(user) || !SynchroPermHelper.isSynchroUser(user)) {
//            return false;
//        }
        return true;
    }
}
