package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/29/14
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateKantarPIBTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Kantar PIB Table";
    }

    @Override
    public String getDescription() {
        return "This task perform creating a table called 'grailkantarpib' to persist kantar related pib details";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateKantarPIBTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarpib")) {
            UpgradeUtils.dropTable("grailkantarpib");
        }
        UpgradeUtils.executeSQLGenFile("CreateKantarPIBTable");
    }
}
