package com.grail.synchro.dao.impl;

import com.grail.synchro.beans.BeanObject;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
//import com.jivesoftware.util.AuthUtils;

/**
 * @author: Samee K.S
 * @since: 1.0
 */
abstract class SynchroAbstractDAO extends JiveJdbcDaoSupport {
    protected <T extends BeanObject> T updateAuditFields( final T object, final boolean update){
        final Long currentTime = System.currentTimeMillis();
        final Long userID = JiveApplication.getContext().getAuthenticationProvider().getJiveUser().getID();
        if(! update){
            object.setCreationDate(currentTime);
            object.setCreationBy(userID);
        }
        object.setModifiedBy(userID);
        object.setModifiedDate(currentTime);
        return object;
    }
}