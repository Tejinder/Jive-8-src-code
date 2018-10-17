package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/23/15
 * Time: 7:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePendingActivityViewsTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Pending Activity Views Table";
    }

    @Override
    public String getDescription() {
        return "Pending Activity Views Table used to store all the pending activities views by user.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreatePendingActivityViewsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailPendingActivityViews")) {
            UpgradeUtils.dropTable("grailPendingActivityViews");
        }
        UpgradeUtils.executeSQLGenFile("CreatePendingActivityViewsTable");
    }
}
