package com.grail.action;

import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/7/15
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class GrailDashboardAction extends JiveActionSupport {
    private boolean grailDashboardCatalogue = true;

    public boolean isGrailDashboardCatalogue() {
        return grailDashboardCatalogue;
    }

    @Override
    public String execute() {
        if(!SynchroPermHelper.canAccessKantarPortal(getUser())) {
            return UNAUTHORIZED;
        }
        return SUCCESS;
    }

}
