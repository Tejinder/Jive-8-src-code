package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar
 * @since: 1.0
 */
public class CreateCurrencyFieldTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create CurrencyFields Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a table used by synchro for storing currency fields ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateCurrencyFieldTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {

        if (UpgradeUtils.doesTableExist("grailCurrencyFields")) {        	
        	UpgradeUtils.dropTable("grailCurrencyFields");
        }
        UpgradeUtils.executeSQLGenFile("CreateCurrencyFieldTable");
    }
}
