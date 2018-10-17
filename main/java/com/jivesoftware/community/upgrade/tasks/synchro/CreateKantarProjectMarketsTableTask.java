package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/30/14
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateKantarProjectMarketsTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Kantar Project Market Table";
    }

    @Override
    public String getDescription() {
        return "This task perform creating a table called 'grailkantarprojectmarket' to persist kantar project market list";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateKantarProjectMarketsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarprojectmarket")) {
            UpgradeUtils.dropTable("grailkantarprojectmarket");
        }
        UpgradeUtils.executeSQLGenFile("CreateKantarProjectMarketsTable");
    }
}
