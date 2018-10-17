package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateKantarReportTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Kantar Report Table";
    }

    @Override
    public String getDescription() {
        return "This task performs creation of new table called 'grailkantarreport'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateKantarReportTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarreport")) {
            UpgradeUtils.dropTable("grailkantarreport");
        }
        UpgradeUtils.executeSQLGenFile("CreateKantarReportTable");
    }
}
