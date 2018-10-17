package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateProjectTPDSKUTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create New Project TPD SKU Table task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by new synchro for TPD SKU Details.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectTPDSKUTableTask");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectTPDSKUDetails")) {        	
        	UpgradeUtils.dropTable("grailProjectTPDSKUDetails");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectTPDSKUTable");
    }
}
