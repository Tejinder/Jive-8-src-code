package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateStageToDoListTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Stage To Do List Table Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to manage the To Do List for Each Stage.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateStageToDoListTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailstagetodolist")) {
            UpgradeUtils.executeSQLGenFile("CreateStageToDoListTable");
        }
    }
}
