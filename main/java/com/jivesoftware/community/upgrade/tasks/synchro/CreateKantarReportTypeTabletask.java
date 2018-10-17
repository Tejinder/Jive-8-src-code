package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/8/15
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateKantarReportTypeTabletask implements UpgradeTask {

    @Override
    public String getName() {
        return "Kantar Report Type Table";
    }

    @Override
    public String getDescription() {
        return "This task performs creation of new table called 'grailkantarreporttype'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateKantarReportTypeTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarreporttype")) {
            UpgradeUtils.dropTable("grailkantarreporttype");
        }
        UpgradeUtils.executeSQLGenFile("CreateKantarReportTypeTable");
    }
}
