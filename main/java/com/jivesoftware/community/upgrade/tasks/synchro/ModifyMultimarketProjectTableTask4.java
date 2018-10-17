package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyMultimarketProjectTableTask4 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyMultimarketProjectTableTask4.class);
	
	private static String ALTER_COL = "ALTER TABLE grailproject ALTER COLUMN proposedmethodology TYPE VARCHAR(5000);";
	
	
    @Override
    public String getName() {
        return "Modify Project Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Project Table for modifying proposed methodology field to multiselect comma spearated string";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProject")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL);
        	}catch(Exception e){LOGGER.error("Error while updating project table - modifying proposed methodology field to multiselect comma spearated string " + e.getMessage());}
        }
    }
}
