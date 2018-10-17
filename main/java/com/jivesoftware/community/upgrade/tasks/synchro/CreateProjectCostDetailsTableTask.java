package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateProjectCostDetailsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create New Project Cost Detail Table task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by new synchro to save project cost details table.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectCostDetailsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectCostDetails")) {        	
        	UpgradeUtils.dropTable("grailProjectCostDetails");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectCostDetailsTable");
    }
}
