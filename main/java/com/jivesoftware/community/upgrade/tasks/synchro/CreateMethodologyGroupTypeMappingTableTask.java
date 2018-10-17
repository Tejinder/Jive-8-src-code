package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/28/15
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateMethodologyGroupTypeMappingTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Synchro methodology type and methodology group mapping table creation Task";
    }

    @Override
    public String getDescription() {
        return  "Synchro methodology type and methodology group mapping table creation Task";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateMethodologyGroupTypeMappingTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailMethGroupTypeMapping")) {
            UpgradeUtils.dropTable("grailMethGroupTypeMapping");
        }
        UpgradeUtils.executeSQLGenFile("CreateMethodologyGroupTypeMappingTable");
    }


}
