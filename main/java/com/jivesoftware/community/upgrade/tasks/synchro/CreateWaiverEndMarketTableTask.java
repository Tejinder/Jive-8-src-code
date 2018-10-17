package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateWaiverEndMarketTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create WaiverEndMarket Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro related to Waiver EndMarkets mappings.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateWaiverEndMarketTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailWaiverEndMarket")) {        	
        	UpgradeUtils.dropTable("grailWaiverEndMarket");        	
        }
        UpgradeUtils.executeSQLGenFile("CreateWaiverEndMarketTable");
    }
}