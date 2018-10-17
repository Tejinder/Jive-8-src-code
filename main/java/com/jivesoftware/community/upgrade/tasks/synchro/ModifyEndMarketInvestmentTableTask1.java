package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyEndMarketInvestmentTableTask1 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyEndMarketInvestmentTableTask1.class);
	
	
	private static String ADD_SYNCHRO_CODE_COLUMN = "ALTER TABLE grailendmarketinvestment ADD synchroCode bigint";
	
	
    @Override
    public String getName() {
        return "Modify EndMarket Investment Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail EndMarket Investment Table for adding columns for Synchro New Requirements";
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
        		
	        	UpgradeUtils.executeStatement(ADD_SYNCHRO_CODE_COLUMN);
	        
	        	
        	}catch(Exception e){LOGGER.error("Error while updating project table " + e.getMessage());}
        }
    }
}
