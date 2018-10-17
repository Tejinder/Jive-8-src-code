package com.grail.synchro.action;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectWaiverManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author: Kanwar Grewal
 * @version 4.0
 */
public class WaiverViewAction extends JiveActionSupport{

    private static final Logger LOGGER = Logger.getLogger(WaiverViewAction.class);
    private Long projectWaiverID;
    private String waiverName;
    private PermissionManager permissionManager;
    private ProjectWaiverManager projectWaiverManager;

	@Override
    public String input() {    
		//Authentication layer
    	if(!SynchroPermHelper.canInitiateWaiver(getUser()))
    	{
    		return UNAUTHORIZED;
    	}
    	return INPUT;
    }
    

    public String execute() {
    	//Authentication layer
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            // This will check whether the user has accepted the Disclaimer or not.
//            if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//            {
//                return "disclaimererror";
//            }

            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
                return UNAUTHORIZED;
            }
        }
        if(projectWaiverID != null && projectWaiverID>0)
        {
        	ProjectWaiver projectWaiver = getProjectWaiverManager().get(projectWaiverID);
        	if(projectWaiver != null) {
        		waiverName = projectWaiver.getName();
            }
        	
        }
        
    	if(!SynchroPermHelper.canInitiateProject(jiveUser))
    	{
    		return UNAUTHORIZED;
    	}
    	
    	       
    	return SUCCESS;
    }
    
    public PermissionManager getPermissionManager() {
        if(permissionManager == null){
            permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }
    
    public ProjectWaiverManager getProjectWaiverManager() {
        if(projectWaiverManager == null){
        	projectWaiverManager = JiveApplication.getContext().getSpringBean("projectWaiverManager");
        }
        return projectWaiverManager;
    }
    
    public Long getProjectWaiverID() {
		return projectWaiverID;
	}

	public void setProjectWaiverID(Long projectWaiverID) {
		this.projectWaiverID = projectWaiverID;
	}


	public String getWaiverName() {
		return waiverName;
	}
    
}
