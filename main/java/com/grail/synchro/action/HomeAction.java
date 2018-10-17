package com.grail.synchro.action;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author: vivek
 * @since: 1.0
 */
public class HomeAction extends JiveActionSupport {

    private static final Logger logger = LogManager.getLogger(HomeAction.class);
    private boolean showHome = false;
    private boolean createAccess = false;
    private boolean changeStatus= false;
    private PermissionManager permissionManager;
    private boolean canAccessProjectWaiver = false;

    public boolean isChangeStatus() {
        return changeStatus;
    }

    public PermissionManager getPermissionManager() {
        if(permissionManager == null){
            permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }

    public boolean isCreateAccess() {
        return createAccess;
    }

   

    public boolean getCanAccessProjectWaiver() {
        return canAccessProjectWaiver;
    }

    public void setCanAccessProjectWaiver(boolean canAccessProjectWaiver) {
        this.canAccessProjectWaiver = canAccessProjectWaiver;
    }

    public boolean isShowHome() {
        return showHome;
    }

    public String execute() {

    	System.out.println("Inside Home Action ---");
    	final User jiveUser = getUser();
        if(jiveUser != null) {

            // This will check whether the user has accepted *the Disclaimer or not.
//            if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//            {
//                return "disclaimererror";
//            }

            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
                return UNAUTHORIZED;
            }
        }
      
        
      
        //createAccess = SynchroPermHelper.canInitiateProject(getUser());
        createAccess = SynchroPermHelper.canInitiateProject_NEW();
        canAccessProjectWaiver = SynchroPermHelper.canInitiateWaiver(getUser());
                
        return SUCCESS;
    }
}
