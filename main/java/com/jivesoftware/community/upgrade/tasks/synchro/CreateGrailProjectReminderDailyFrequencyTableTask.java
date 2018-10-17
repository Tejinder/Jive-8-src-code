package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/18/15
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailProjectReminderDailyFrequencyTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Create Grail Project Remainder Daily Frequency Table Task";
    }

    @Override
    public String getDescription() {
        return "This task performs creating a new table called 'grailProjReminderDailyFreq'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateGrailProjectReminderDailyFrequencyTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjReminderDailyFreq")) {
            UpgradeUtils.dropTable("grailProjReminderDailyFreq");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailProjectReminderDailyFrequencyTable");
    }
}
