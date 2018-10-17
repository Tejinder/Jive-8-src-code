package com.grail.osp.action;


import java.util.ArrayList;
import java.util.List;

import com.grail.osp.beans.OSPTile;
import com.grail.osp.manager.OSPManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * User: Tejinder
 * Date: 12/18/17
 */
public class ShareLinksAction extends JiveActionSupport {

	
	
    @Override
    public String execute() {
        if(!SynchroPermHelper.canAccessOSPSharePortal(getUser())) {
            return UNAUTHORIZED;
        }
       
        return "externalLinks";
    }
    
  
    public String externalLinks() {
        if(!SynchroPermHelper.canAccessOSPSharePortal(getUser())) {
            return UNAUTHORIZED;
        }
       
        return "externalLinks";
    }
    
    
    public String internalLinks() {
        if(!SynchroPermHelper.canAccessOSPSharePortal(getUser())) {
            return UNAUTHORIZED;
        }
       
        return "internalLinks";
    }
	
	

}
