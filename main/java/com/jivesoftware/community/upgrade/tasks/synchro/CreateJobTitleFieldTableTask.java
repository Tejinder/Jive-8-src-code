package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateJobTitleFieldTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Project Job Title Fields Table Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Job Title Meta Fields.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateJobTitleFieldTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailJobTitleFields")) {        	
        	UpgradeUtils.dropTable("grailJobTitleFields");
        }
        UpgradeUtils.executeSQLGenFile("CreateJobTitleFieldTable");
    }
}
