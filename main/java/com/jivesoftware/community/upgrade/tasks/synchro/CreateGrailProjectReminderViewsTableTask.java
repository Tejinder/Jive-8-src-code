package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/11/15
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailProjectReminderViewsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Grail Project Reminder Views Table Task";
    }

    @Override
    public String getDescription() {
        return "This task performs creating a new table called 'grailprojectreminderviews'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("grailprojectreminderviews");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectreminderviews")) {
            UpgradeUtils.dropTable("grailprojectreminderviews");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailProjectReminderViewsTable");
    }
}
