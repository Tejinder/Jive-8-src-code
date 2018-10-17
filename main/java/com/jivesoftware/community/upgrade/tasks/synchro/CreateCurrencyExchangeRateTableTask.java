package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateCurrencyExchangeRateTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create CurrencyExchangeRateTable Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro for saving any changes related to currency exchange rate";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateCurrencyExchangeRateTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailCurrencyExchangeRate")) {
            UpgradeUtils.executeSQLGenFile("CreateCurrencyExchangeRateTable");
        }
    }
}
