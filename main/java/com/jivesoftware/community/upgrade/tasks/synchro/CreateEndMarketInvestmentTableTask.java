package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Kanwar
 * @since: 1.0
 */
public class CreateEndMarketInvestmentTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create End Market Investment Table task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save end market and investment details";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateEndMarketInvestmentTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailEndMarketInvestment")) {        	
        	UpgradeUtils.dropTable("grailEndMarketInvestment");
        }
        UpgradeUtils.executeSQLGenFile("CreateEndMarketInvestmentTable");
    }
}
