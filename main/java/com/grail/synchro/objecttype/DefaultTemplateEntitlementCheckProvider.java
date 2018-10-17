package com.grail.synchro.objecttype;

import com.grail.synchro.object.DefaultTemplate;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.impl.BaseEntitlementProvider;
import com.jivesoftware.community.objecttype.EntitlementCheckProvider;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplateEntitlementCheckProvider extends BaseEntitlementProvider<DefaultTemplate> implements EntitlementCheckProvider<DefaultTemplate> {

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
    public boolean isUserEntitled(User user, DefaultTemplate object, EntitlementTypeProvider.Type type) {
        if(isAnyNull(user, object, type)) {
            return false;
        }

        if(isUserDisabled(user) || isBannedFromPosting(user)) {
            return false;
        }
        return true;
    }
}
