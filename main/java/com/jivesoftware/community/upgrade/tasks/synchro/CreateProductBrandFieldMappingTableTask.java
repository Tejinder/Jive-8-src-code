package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateProductBrandFieldMappingTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Product Brand Field mapping Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Product Brand Field mapping info";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProductBrandFieldMappingTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailPBFieldMappingFields")) {        	
        	UpgradeUtils.dropTable("grailPBFieldMappingFields");
        }
        UpgradeUtils.executeSQLGenFile("CreateProductBrandFieldMappingTable");
    }
}
