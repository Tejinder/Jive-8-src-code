package com.grail.synchro.scheduling.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/2/15
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroReminderJob extends QuartzJobBean {
    private SynchroReminderTask synchroReminderTask;

    public SynchroReminderTask getSynchroReminderTask() {
        return synchroReminderTask;
    }

    public void setSynchroReminderTask(SynchroReminderTask synchroReminderTask) {
        this.synchroReminderTask = synchroReminderTask;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        synchroReminderTask.manageReminders();
    }
}
