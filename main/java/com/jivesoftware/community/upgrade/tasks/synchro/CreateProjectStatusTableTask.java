package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class CreateProjectStatusTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Project Status Table task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table user for maintaing project status";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectStatusTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectStatus")) {        	
        	UpgradeUtils.dropTable("grailProjectStatus");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectStatusTable");
    }
}
