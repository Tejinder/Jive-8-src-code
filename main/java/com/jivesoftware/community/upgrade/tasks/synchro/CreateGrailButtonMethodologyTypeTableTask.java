package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/19/15
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailButtonMethodologyTypeTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Button Methodology Type table create task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table called 'grailbuttonmethodologytype' to store all grail button related methodology types";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateGrailButtonMethodologyTypeTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailbuttonmethodologytype")) {
            UpgradeUtils.dropTable("grailbuttonmethodologytype");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailButtonMethodologyTypeTable");
    }
}
