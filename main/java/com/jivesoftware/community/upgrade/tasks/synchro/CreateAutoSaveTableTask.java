package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author Bhaskar Avulapati
 */
public class CreateAutoSaveTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Create Synchro Auto Save Table";
    }

    @Override
    public String getDescription() {
        return "This task will create a new 'grailautosave' table to persist auto-save form " +
                "details (ex: PIT, PIB, Project Waiver and etc., details)";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateAutoSaveTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailAutoSaveFormDetails")) {
            UpgradeUtils.dropTable("grailAutoSaveFormDetails");
        }
        UpgradeUtils.executeSQLGenFile("CreateAutoSaveTable");
    }
}
