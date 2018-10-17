package com.grail.synchro.action;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author Kanwar Grewal
 * @version 1.0, Date: 7/22/13
 */
public class ProjectStatusDashboardAction extends JiveActionSupport {

    private static final Logger LOGGER = Logger.getLogger(ProjectStatusDashboardAction.class);
 	private SynchroUtils synchroUtils;
 	private boolean statusCatalogue = true;
    private boolean adminUser = false;
    
	public boolean isAdminUser() {
		return adminUser;
	}
 	
 	public boolean isStatusCatalogue() {
		return statusCatalogue;
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
		if(!getSynchroUtils().canChangeProjectStatus(getUser()))
			return UNAUTHORIZED;
		adminUser = SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin();
        return SUCCESS;
    }

}
