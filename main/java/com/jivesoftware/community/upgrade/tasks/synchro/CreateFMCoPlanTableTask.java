package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateFMCoPlanTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Financial CoPlan Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro for capturing co-plan financial details related to the project.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateFMCoPlanTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailFMCoPlan")) {
            UpgradeUtils.executeSQLGenFile("CreateFMCoPlanTable");
        }
    }
}
