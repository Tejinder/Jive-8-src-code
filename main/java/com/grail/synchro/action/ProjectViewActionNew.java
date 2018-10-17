package com.grail.synchro.action;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author: Kanwar Grewal
 * @version 4.0
 */
public class ProjectViewActionNew extends JiveActionSupport{

    private static final Logger LOGGER = Logger.getLogger(ProjectViewAction.class);
    private Long projectID;
    private String projectName;
    private PermissionManager permissionManager;
    private ProjectManagerNew synchroProjectManagerNew;
    private Boolean isMultiMarketProject;
    private Boolean isFieldWorkProject;

	@Override
    public String input() {    
		//Authentication layer
    	if(!SynchroPermHelper.canInitiateProject_NEW())
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
        		if(project.getFieldWorkStudy()!=null && project.getFieldWorkStudy()==1)
        		{
        			isFieldWorkProject=true;
        		}
        		else
        		{
        			isFieldWorkProject=false;
        		}
                //isMultiMarketProject = project.getMultiMarket();
            }
        	
        }
        
    	if(!SynchroPermHelper.canInitiateProject_NEW())
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
    
    public ProjectManagerNew getProjectManager() {
        if(synchroProjectManagerNew == null){
        	synchroProjectManagerNew = JiveApplication.getContext().getSpringBean("synchroProjectManagerNew");
        }
        return synchroProjectManagerNew;
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


	public Boolean getIsFieldWorkProject() {
		return isFieldWorkProject;
	}


	public void setIsFieldWorkProject(Boolean isFieldWorkProject) {
		this.isFieldWorkProject = isFieldWorkProject;
	}
}
