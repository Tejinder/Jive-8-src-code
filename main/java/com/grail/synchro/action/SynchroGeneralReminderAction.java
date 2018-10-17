package com.grail.synchro.action;

import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/18/15
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroGeneralReminderAction extends JiveActionSupport {

    @Override
    public String input() {
        return INPUT;
    }


    @Override
    public String execute() {
        return SUCCESS;
    }
}
