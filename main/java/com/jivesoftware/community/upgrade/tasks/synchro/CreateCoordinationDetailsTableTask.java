package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateCoordinationDetailsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create CoordinationDetails Table ";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table which is used by synchro for saving Co-ordination details related to the project.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateCoordinationDetailsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailCoordinationDetails")) {
            UpgradeUtils.executeSQLGenFile("CreateCoordinationDetailsTable");
        }
    }
}
