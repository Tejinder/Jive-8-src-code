package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateUserReadTrackerTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create UserReadTracker Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to store read track information of the user against all the projects.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateUserReadTrackerTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
    	if (UpgradeUtils.doesTableExist("grailUserReadTracker")) {        	
        	UpgradeUtils.dropTable("grailUserReadTracker");
        }
        UpgradeUtils.executeSQLGenFile("CreateUserReadTrackerTable");
    }
}
