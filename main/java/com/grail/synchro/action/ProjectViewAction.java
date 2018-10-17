package com.grail.synchro.action;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author: Kanwar Grewal
 * @version 4.0
 */
public class ProjectViewAction extends JiveActionSupport{

    private static final Logger LOGGER = Logger.getLogger(ProjectViewAction.class);
    private Long projectID;
    private String projectName;
    private PermissionManager permissionManager;
    private ProjectManager projectManager;
    private Boolean isMultiMarketProject;

	@Override
    public String input() {    
		//Authentication layer
    	if(!SynchroPermHelper.canInitiateProject(getUser()))
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
        if(projectID != null && projectID>0)
        {
        	Project project = getProjectManager().get(projectID);
        	if(project != null) {
        		projectName = project.getName();
                isMultiMarketProject = project.getMultiMarket();
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
    
    public ProjectManager getProjectManager() {
        if(projectManager == null){
        	projectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return projectManager;
    }
    
    public Long getProjectID() {
		return projectID;
	}

	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}


	public String getProjectName() {
		return projectName;
	}

    public Boolean getIsMultiMarketProject() {
        return isMultiMarketProject;
    }

    public void setIsMultiMarketProject(Boolean multiMarketProject) {
        isMultiMarketProject = multiMarketProject;
    }
}
