package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.List;

import com.grail.synchro.beans.ProjectDashboardViewBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectGraphBar;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author Tejinder
 * @version 1.0, Date: 6/5/13
 */
public class RegionalDashboardAction extends JiveActionSupport {

    private static final Logger LOGGER = Logger.getLogger(RegionalDashboardAction.class);
    private ProjectManagerNew synchroProjectManagerNew;
    private List<Project> projects;
    private static Integer LIMIT = 10;
    private Integer pageLimit;
    private SynchroUtils synchroUtils;
   
    private PermissionManager permissionManager;
    private boolean changeStatus;
    private boolean adminUser = false;
    private boolean createProject = false;
    private boolean accessWaiverCatalogue = false;
    private boolean createWaiver = false;
    
    
	public boolean isCreateWaiver() {
		return createWaiver;
	}
	public boolean isAccessWaiverCatalogue() {
		return accessWaiverCatalogue;
	}
	public boolean isAdminUser() {
		return adminUser;
	}
	public boolean isChangeStatus() {
		return changeStatus;
	}

	public boolean isCreateProject() {
		return createProject;
	}
	
	public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    public String execute(){
		// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}

       // createProject = getSynchroUtils().canCreateProject(getUser());
    	 createProject = getSynchroUtils().canCreateProject(getUser());

        changeStatus = getSynchroUtils().canChangeProjectStatus(getUser());

        /**
         * Check if Logged-In user can Create new Waiver
         */
        createWaiver = getSynchroUtils().canIniateProjectWaiver(getUser());

        /**
         * Check if Logged-In user can access Waiver Catalogue
         */
        accessWaiverCatalogue = getSynchroUtils().canAccessProjectWaiver(getUser());

        return SUCCESS;
    }
	
    public List<Project> getProjects() {
        return projects;
    }

    public void setSynchroProjectManagerNew(final ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
    }

    @Required
    public void setPermissionManager(final PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }
}
