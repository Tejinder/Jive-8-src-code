package com.grail.synchro.action;

import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class AgencyWaiverPendingActivityActionNew extends JiveActionSupport {

    private PermissionManager permissionManager;
    
    private boolean pendingAgencyWaiverCatalogue = true;
    private boolean initiateWaiver = false;
    
    public boolean isPendingAgencyWaiverCatalogue() {
		return pendingAgencyWaiverCatalogue;
	}


	private SynchroUtils synchroUtils;

	public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }
	
    @Override
    public String execute() {
        if(getUser() != null) {
            if(!getPermissionManager().isSynchroUser(getUser()))
            {
                return UNAUTHORIZED;
            }
            
           /* if(!getSynchroUtils().canAccessProjectWaiver(getUser()))
        	{
        		return UNAUTHORIZED;
        	}
            */
            initiateWaiver = getSynchroUtils().canIniateProjectWaiver(getUser());
        }
        return SUCCESS;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }
}
