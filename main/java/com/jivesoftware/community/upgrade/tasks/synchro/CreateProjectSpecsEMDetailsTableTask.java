package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateProjectSpecsEMDetailsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Project Specs End Market Details Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Project Specs Stage End Market details.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectSpecsEMDetailsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectSpecsEMDetails")) {        	
        	UpgradeUtils.dropTable("grailProjectSpecsEMDetails");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectSpecsEMDetailsTable");
    }
}
