package com.grail.synchro.scheduling.quartz;

import com.grail.synchro.manager.SynchroReminderManager;
import com.grail.synchro.util.SynchroReminderUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/2/15
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroReminderTask {

    private SynchroReminderManager synchroReminderManager;

    public void manageReminders() {
        SynchroReminderUtils.processReminders();
    }

    public SynchroReminderManager getSynchroReminderManager() {
        return synchroReminderManager;
    }

    public void setSynchroReminderManager(SynchroReminderManager synchroReminderManager) {
        this.synchroReminderManager = synchroReminderManager;
    }
}
