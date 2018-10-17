package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateProjectDeleteStatusTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Project Delete Status Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save project current status while deleting";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectDeleteStatusTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectDeleteStatus")) {        	
        	UpgradeUtils.dropTable("grailProjectDeleteStatus");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectDeleteStatusTable");
    }
}
