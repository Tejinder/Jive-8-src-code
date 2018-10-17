package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateMethodologyFieldTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Methodology Field Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Methodology field info";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateMethodologyFieldTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
   	public void doTask() throws Exception {
   		if (UpgradeUtils.doesTableExist("grailMethodologyFields")) {
   			UpgradeUtils.dropTable("grailMethodologyFields");
   		}
   		UpgradeUtils.executeSQLGenFile("CreateMethodologyFieldTable");
   	}
}
