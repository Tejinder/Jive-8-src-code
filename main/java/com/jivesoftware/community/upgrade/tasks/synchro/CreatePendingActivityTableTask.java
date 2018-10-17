package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 12:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePendingActivityTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Pending Activity Table";
    }

    @Override
    public String getDescription() {
        return "Pending Activity Table used to store all the pending activities on each stage.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreatePendingActivityTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailpendingactivities")) {
            UpgradeUtils.dropTable("grailpendingactivities");
        }
        UpgradeUtils.executeSQLGenFile("CreatePendingActivityTable");
    }
}
