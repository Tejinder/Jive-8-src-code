package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyInvestmentFundingTableTask1 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyInvestmentFundingTableTask1.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailfundinginvestment ADD approvaldate bigint;";
	
    @Override
    public String getName() {
        return "Modify grailfundinginvestment Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting grailfundinginvestmentTable for adding new column - Approval Date";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailfundinginvestmentTable tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailfundinginvestment")) {
        	
        	try{
        		
	        	UpgradeUtils.executeStatement(ADD_COL1);
	        	
        	}catch(Exception e){LOGGER.error("Error while updating grailfundinginvestment Table " + e.getMessage());}
        }
    }
}
