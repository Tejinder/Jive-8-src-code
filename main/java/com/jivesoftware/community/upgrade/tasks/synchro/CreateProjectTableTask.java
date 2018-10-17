package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateProjectTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Project Table task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save project details table which are not capture as Community Extended properties.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProject")) {        	
        	UpgradeUtils.dropTable("grailProject");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectTable");
    }
}
