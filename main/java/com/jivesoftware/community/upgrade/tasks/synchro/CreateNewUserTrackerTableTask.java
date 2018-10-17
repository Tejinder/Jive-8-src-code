package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar
 * @since: 1.0
 */
public class CreateNewUserTrackerTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create New Stakeholders Track Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro for capturing New Stakeholders Track to the project.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateNewUserTrackerTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailNewUserTracker")) {
            UpgradeUtils.executeSQLGenFile("CreateNewUserTrackerTable");
        }
    }
}
