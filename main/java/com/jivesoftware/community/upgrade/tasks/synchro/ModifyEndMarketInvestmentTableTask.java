package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyEndMarketInvestmentTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyEndMarketInvestmentTableTask.class);
	
	private static String REMOVE_COL1 = "ALTER TABLE grailendmarketinvestment DROP investmenttype;";
	private static String REMOVE_COL2 = "ALTER TABLE grailendmarketinvestment DROP marketname;";
	private static String REMOVE_COL3 = "ALTER TABLE grailendmarketinvestment DROP approved;";
	private static String REMOVE_COL4 = "ALTER TABLE grailendmarketinvestment DROP latestestimatecost;";
	private static String REMOVE_COL5 = "ALTER TABLE grailendmarketinvestment DROP latestestimatecostcurrency;";
	
	
    @Override
    public String getName() {
        return "Modify EndMarket Investment Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail EndMarket Investment Table for removing unused columns";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailendmarketinvestment tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailendmarketinvestment")) {
        	
        	try{
        		
	        	UpgradeUtils.executeStatement(REMOVE_COL1);
	        	UpgradeUtils.executeStatement(REMOVE_COL2);
	        	UpgradeUtils.executeStatement(REMOVE_COL3);
	        	UpgradeUtils.executeStatement(REMOVE_COL4);
	        	UpgradeUtils.executeStatement(REMOVE_COL5);
	        	
        	}catch(Exception e){LOGGER.error("Error while updating project table " + e.getMessage());}
        }
    }
}
