package com.grail.synchro.action;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.manager.PermissionManager;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author Tejinder
 * @version 1.0, 
 */
public class ProjectPendingActivityActionNew extends JiveActionSupport {

    private static final Logger LOGGER = Logger.getLogger(ProjectPendingActivityActionNew.class);
    private PermissionManager permissionManager;
    private boolean projectPendingActivityCatalogue = true;
    
	public boolean isProjectPendingActivityCatalogue() {
		return projectPendingActivityCatalogue;
	}

	public PermissionManager getPermissionManager() {
         if(permissionManager == null){
         	permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
         }
         return permissionManager;
     }
 	
	public String execute(){
        // TODO: Return the list based on permission for project
		// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
		final User jiveUser = getUser();
        if(jiveUser != null) {
            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
            	return UNAUTHORIZED;
            }
        }
        return SUCCESS;
    }

}
