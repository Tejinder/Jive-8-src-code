package com.grail.kantar.action;

import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/29/14
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarDashboardAction extends JiveActionSupport {

    private boolean kantarDashboardCatalogue = true;

    public boolean isKantarDashboardCatalogue() {
        return kantarDashboardCatalogue;
    }

    @Override
    public String execute() {
        if(!SynchroPermHelper.canAccessKantarPortal(getUser())) {
            return UNAUTHORIZED;
        }
        return SUCCESS;
    }

}
