package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/11/15
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailGeneralReminderViewsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Grail General Reminder Views Table Task";
    }

    @Override
    public String getDescription() {
        return "This task performs creating a new table called 'grailgeneralreminderviews'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("grailgeneralreminderviews");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailgeneralreminderviews")) {
            UpgradeUtils.dropTable("grailgeneralreminderviews");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailGeneralReminderViewsTable");
    }
}
