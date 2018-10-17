package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar
 * @since: 1.0
 */
public class CreateMethCollectionMappingTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Methodology and Data Collection Field Mapping Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a table used by synchro for storing Methodology and Data Collection Field Mappings ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateMethCollectionMappingTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailMethCollectionMapping")) {        	
        	UpgradeUtils.dropTable("grailMethCollectionMapping");
        }
        UpgradeUtils.executeSQLGenFile("CreateMethCollectionMappingTable");
    }
}
