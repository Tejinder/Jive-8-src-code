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
public class HelpAction extends JiveActionSupport{

    private static final Logger LOGGER = Logger.getLogger(HelpAction.class);
    private PermissionManager permissionManager;

	@Override
    public String input() {    
    	return execute();
    }
    
	@Override
    public String execute() {
//    	//Authentication layer
//    	final User jiveUser = getUser();
//        if(jiveUser != null) {
//            // This will check whether the user has accepted the Disclaimer or not.
//            if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//            {
//                return "disclaimererror";
//            }
//
//            if(!getPermissionManager().isSynchroUser(jiveUser))
//            {
//                return UNAUTHORIZED;
//            }
//        }
    	return SUCCESS;
    }
    
    public PermissionManager getPermissionManager() {
        if(permissionManager == null){
            permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }
}
