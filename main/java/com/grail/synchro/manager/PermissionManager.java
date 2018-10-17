package com.grail.synchro.manager;

import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.SynchroPermissionLevelBean;
import com.jivesoftware.base.User;

/**
 * Centralized management of Permission related to Synchro System including creating, retrieving, and deleting
 * of Permission objects.
 *
 * @see SynchroPermissionLevelBean
 * @javadoc api
 *
 * @author: Vivek Kondur
 */
public interface PermissionManager {
    
    boolean isSynchroUser(final User user);
    boolean isRKPUser(final User user);

}
