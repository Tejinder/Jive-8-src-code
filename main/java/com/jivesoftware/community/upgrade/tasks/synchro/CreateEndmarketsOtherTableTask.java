package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar Grewal
 * @since: 5.0
 */
public class CreateEndmarketsOtherTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Phase 5 Table Task : Creating Table for endmarket other options";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro get config details about type of endmarket as approval type";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateEndmarketOthersTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailEMOthersMapping")) {        	
        	UpgradeUtils.dropTable("grailEMOthersMapping");
        }
        UpgradeUtils.executeSQLGenFile("CreateEndmarketOthersTable");
    }
}
