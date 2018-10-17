package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyMultimarketProjectTableTask3 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyMultimarketProjectTableTask3.class);
	
	private static String ADD_COL = "ALTER TABLE grailfundinginvestment ADD spicontact bigint;";
	
	
    @Override
    public String getName() {
        return "Modify grailfundinginvestment Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail grailfundinginvestment Table for adding column spicontact";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailfundinginvestment tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailfundinginvestment")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_COL);
        	}catch(Exception e){LOGGER.error("Error while updating grailfundinginvestment table - adding new column - spicontact for multimarket project " + e.getMessage());}
        }
    }
}
