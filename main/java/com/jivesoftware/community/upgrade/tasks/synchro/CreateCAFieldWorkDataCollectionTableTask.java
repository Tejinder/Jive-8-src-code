package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateCAFieldWorkDataCollectionTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create CAFieldWorkDataCollectionTable Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro for saving the Data Collection Methods relation with FieldWorkAgency for a given project and Coordination Agency";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateCAFieldWorkDataCollectionTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailCAFieldWorkDataCollection")) {
            UpgradeUtils.executeSQLGenFile("CreateCAFieldWorkDataCollectionTable");
        }
    }
}
