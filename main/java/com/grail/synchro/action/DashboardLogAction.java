package com.grail.synchro.action;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * @author Kanwar Grewal
 * @version 1.0, Date: 17/2/15
 */
public class DashboardLogAction extends JiveActionSupport {

    private static final Logger LOGGER = Logger.getLogger(DashboardLogAction.class);
    private PermissionManager permissionManager;
    private boolean adminUser = false;
    private boolean logDashboardCatalogue = true;
    private Long projectID;
    private ProjectManager synchroProjectManager;
    private String projectName;
    
	public boolean isAdminUser() {
		return adminUser;
	}

    public String execute(){
    	if(getUser() != null) {
    		
    		if(projectID!=null && projectID > 0)
    		{
    			Project project = synchroProjectManager.get(projectID);    			
    			if(project!=null)
    				projectName = project.getName();    		
    		}
    		
    		if(SynchroPermHelper.isSynchroAdmin(getUser()))
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
	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

	public String getProjectName() {
		return projectName;
	}    
    
}
