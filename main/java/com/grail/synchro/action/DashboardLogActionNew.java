package com.grail.synchro.action;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectWaiverManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * @author Kanwar Grewal/Tejinder
 * @version 1.0, Date: 17/2/15
 */
public class DashboardLogActionNew extends JiveActionSupport {

    private static final Logger LOGGER = Logger.getLogger(DashboardLogActionNew.class);
    private PermissionManager permissionManager;
    private boolean adminUser = false;
    private boolean logDashboardCatalogue = true;
    private Long projectID;
    private Long waiverID;
    private ProjectManagerNew synchroProjectManagerNew;
    private String projectName;
    private String waiverName;
    private ProjectWaiverManager projectWaiverManager;
    
	public boolean isAdminUser() {
		return adminUser;
	}

    public String execute(){
    	if(getUser() != null) {
    		
    		if(projectID!=null && projectID > 0)
    		{
    			Project project = synchroProjectManagerNew.get(projectID);    			
    			if(project!=null)
    				projectName = project.getName();    		
    		}
    		
    		if(waiverID!=null && waiverID > 0)
    		{
    			ProjectWaiver waiver = projectWaiverManager.get(waiverID);    			
    			if(waiver!=null)
    				waiverName = waiver.getName();    		
    		}
    		
    		if(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroSystemOwner())
    		{
    			return SUCCESS;
    		}
    		
    		if(projectID!=null && projectID > 0)
    		{
    			if(SynchroPermHelper.canAccessActivityLogs(projectID))
    				return SUCCESS;
    		}
    	}
    	
    	return UNAUTHORIZED;
    }
	

    @Required
    public void setPermissionManager(final PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public boolean isLogDashboardCatalogue() {
		return logDashboardCatalogue;
	}

	public void setLogDashboardCatalogue(boolean logDashboardCatalogue) {
		this.logDashboardCatalogue = logDashboardCatalogue;
	}

	public Long getProjectID() {
		return projectID;
	}

	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	
	public void setSynchroProjectManagerNew(ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
    }

	public String getProjectName() {
		return projectName;
	}

	public Long getWaiverID() {
		return waiverID;
	}

	public void setWaiverID(Long waiverID) {
		this.waiverID = waiverID;
	}

	public String getWaiverName() {
		return waiverName;
	}

	public void setWaiverName(String waiverName) {
		this.waiverName = waiverName;
	}

	public ProjectWaiverManager getProjectWaiverManager() {
		return projectWaiverManager;
	}

	public void setProjectWaiverManager(ProjectWaiverManager projectWaiverManager) {
		this.projectWaiverManager = projectWaiverManager;
	}    
    
}
