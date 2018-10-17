package com.grail.synchro.events;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.event.UserEvent;
import com.jivesoftware.base.event.v2.EventListener;
import com.jivesoftware.community.impl.search.user.ProfileSearchIndexManager;

/**
 * A custom EventListener which listens to any user who has been updated/modified.
 * This event listener is necessary for keeping the Cached Groups - SynchroUtils.synchroUserGroupsMap - update at all times.
 * Following are the Groups which are supported by the Listener:
 * SYNCHRO
 * MARKETING_APPROVERS
 * SPI_APPROVERS
 * LEGAL_APPROVERS
 * PROCUREMENT_APPROVERS
 * COORDINATION_AGENCY
 * CO_AGENCY_SUPPORT
 * FIELDWORK_AGENCY
 * COMMUNICATION_AGENCY
 *
 * @author: Tejinder
 */
public class SynchroUserEventListener implements EventListener<UserEvent> {

    private static final Logger log = Logger.getLogger(SynchroUserEventListener.class);

    private SynchroUtils synchroUtils;
    private UserManager userManager;
    private ProfileSearchIndexManager profileSearchIndexManager;

    @Required
    public void setProfileSearchIndexManager(
			ProfileSearchIndexManager profileSearchIndexManager) {
		this.profileSearchIndexManager = profileSearchIndexManager;
	}

	@Required
    public void setSynchroUtils(final SynchroUtils synchroUtils) {
        this.synchroUtils = synchroUtils;
    }

    @Required
    public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

    @Override
    public void handle(final UserEvent e) {


        log.debug("\n\n\n Synchro UserEventHandler : " + e);

        switch (e.getType()) {
            case MODIFIED:
                handleMemberModified(e);
                break;
            
        }

    }

    /**
     * This method will update the modified user in the SynchroUtils.synchroUserGroupsMap 
     * @param event
     */
   
    protected void handleMemberModified(final UserEvent event) {
        log.debug("Handling user event:   -- " + event);
        User changedUser = getUser(event.getPayload().getID());
        if(changedUser!=null)
        {
        	Map<String, List<User>> userGroupCacheMap = getSynchroUtils().getSynchroUserGroupsMap();
        	for(String key:userGroupCacheMap.keySet())
        	{
        		if(userGroupCacheMap.get(key).contains(changedUser))
        		{
        			List<User> members = userGroupCacheMap.get(key);
                    if( members != null ) {
                        members.remove(changedUser);
                        members.add(changedUser);
                    }
        		}
        	}
        }
        profileSearchIndexManager.updateIndex(changedUser);
    }

    /**
     * Get the User 
     * @param userID
     * @return
     */
    private User getUser(final Long userID)
    {
    	User user = null;
    	try
    	{
    		user = this.userManager.getUser(userID);
    	}
    	catch(UserNotFoundException e)
    	{
    		log.error("User Not Found while updating User Properties. Changed user properties will not reflect properly");
    	}
    	return user;
    }

    private SynchroUtils getSynchroUtils(){
        if( synchroUtils == null)
            synchroUtils =  new SynchroUtils();

        return synchroUtils;
    }

	


}
