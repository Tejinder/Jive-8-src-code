package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar
 * @since: 1.0
 */
public class CreateTAgencyFieldTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Tendering Agency Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a table used by synchro for storing Tendering Agency fields ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateTenderingAgencyFieldTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailTAgencyFields")) {
            UpgradeUtils.executeSQLGenFile("CreateTenderingAgencyFieldTable");
        }
    }
}
