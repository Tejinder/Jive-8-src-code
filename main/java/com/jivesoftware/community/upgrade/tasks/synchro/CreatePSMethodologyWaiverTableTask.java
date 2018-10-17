package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreatePSMethodologyWaiverTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Project Specs Methodology Waiver Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Project Specs Methodology Waiver details.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreatePSMethodologyWaiverTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

   /* @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailPIB")) {
            UpgradeUtils.executeSQLGenFile("CreatePIBTable");
        }
    }*/
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailPSMethodologyWaiver")) {        	
        	UpgradeUtils.dropTable("grailPSMethodologyWaiver");
        }
        UpgradeUtils.executeSQLGenFile("CreatePSMethodologyWaiverTable");
    }
}
