package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/23/15
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateMethodologyTypeMappingTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Synchro methodology type and proposed methodology mapping table creation Task";
    }

    @Override
    public String getDescription() {
        return  "Synchro methodology type and proposed methodology mapping table creation Task";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateMethodologyTypeMappingTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailMethTypeMappingFields")) {
            UpgradeUtils.dropTable("grailMethTypeMappingFields");
        }
        UpgradeUtils.executeSQLGenFile("CreateMethodologyTypeMappingTable");
    }
}
