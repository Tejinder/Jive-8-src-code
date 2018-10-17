package com.grail.synchro.action;

import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/6/15
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralReminderDashboardAction extends JiveActionSupport {

    @Override
    public String input() {
        return INPUT;
    }

    @Override
    public String execute() {
        return SUCCESS;
    }
}
