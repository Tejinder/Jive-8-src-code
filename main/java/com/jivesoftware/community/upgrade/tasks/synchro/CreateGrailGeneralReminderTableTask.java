package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/18/15
 * Time: 12:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailGeneralReminderTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Grail General Reminder Table Task";
    }

    @Override
    public String getDescription() {
        return "This task performs creating a new table called 'grailgeneralreminders'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateGrailGeneralReminderTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailgeneralreminders")) {
            UpgradeUtils.dropTable("grailgeneralreminders");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailGeneralReminderTable");
    }
}
