package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Tejinder
 * @since: 1.0
 */
public class CreateNewProposalTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail New Proposal Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save new Proposal details.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateNewProposalTable");
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
        if (UpgradeUtils.doesTableExist("grailProposalNew")) {        	
        	UpgradeUtils.dropTable("grailProposalNew");
        }
        UpgradeUtils.executeSQLGenFile("CreateNewProposalTable");
    }
}
