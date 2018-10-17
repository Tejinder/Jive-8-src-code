package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateMethodologyGroupFieldTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Methodology Group Field Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Methodology Group field info";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateMethodologyGroupFieldTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
   	public void doTask() throws Exception {
   		if (UpgradeUtils.doesTableExist("grailMethodologyGroupFields")) {
   			UpgradeUtils.dropTable("grailMethodologyGroupFields");
   		}
   		UpgradeUtils.executeSQLGenFile("CreateMethodologyGroupFieldTable");
   	}
    
}
