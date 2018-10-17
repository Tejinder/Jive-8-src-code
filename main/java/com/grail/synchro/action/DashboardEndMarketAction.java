package com.grail.synchro.action;

import java.util.List;

import com.grail.synchro.beans.ProjectDashboardViewBean;
import com.jivesoftware.base.UserNotFoundException;
import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectGraphBar;
import com.grail.synchro.manager.ProjectManager;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * @author Kanwar
 * @version 1.0, Date: 26/6/13
 */
public class DashboardEndMarketAction extends JiveActionSupport {

    private static final Logger LOG = Logger.getLogger(DashboardEndMarketAction.class);

    private ProjectManager synchroProjectManager;
    private ProjectDashboardViewBean project;
    private Long projectID;

    public String execute(){
        // TODO: Return the list based on permission for project
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        project = ProjectDashboardViewBean.toProjectDashboardViewBean(synchroProjectManager.get(projectID));
        return SUCCESS;
    }

    public ProjectManager getSynchroProjectManager() {
        return synchroProjectManager;
    }

    public void setSynchroProjectManager(final ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

    public ProjectDashboardViewBean getProject() {
        return project;
    }


    public void setProjectID(Long projectID) {
        this.projectID = projectID;
    }
}
