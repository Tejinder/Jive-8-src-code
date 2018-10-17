package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar
 * @since: 1.0
 */
public class CreateDataCollectionFieldTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Data Collection Field Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a table used by synchro for storing Data Collection fields ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateDataCollectionFieldTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailDataCollectionFields")) {        	
        	UpgradeUtils.dropTable("grailDataCollectionFields");
        }
        UpgradeUtils.executeSQLGenFile("CreateDataCollectionFieldTable");
    }
}
