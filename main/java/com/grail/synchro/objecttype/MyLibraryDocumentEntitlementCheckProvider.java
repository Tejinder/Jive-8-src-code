package com.grail.synchro.objecttype;

import com.grail.synchro.object.MyLibraryDocument;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.impl.BaseEntitlementProvider;
import com.jivesoftware.community.objecttype.EntitlementCheckProvider;

/**
 *
 */
public class MyLibraryDocumentEntitlementCheckProvider extends BaseEntitlementProvider<MyLibraryDocument> implements EntitlementCheckProvider<MyLibraryDocument> {
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
    public boolean isUserEntitled(User user, MyLibraryDocument object, EntitlementTypeProvider.Type type) {
        if(isAnyNull(user, object, type)) {
            return false;
        }

        if(isUserDisabled(user) || isBannedFromPosting(user)) {
            return false;
        }
        return true;
    }
}
