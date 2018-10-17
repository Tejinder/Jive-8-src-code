package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class ModifyPIBTableTask1 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBTableTask1.class);
	
	private static String ALTER_LATEST_ESTIMATE = "ALTER TABLE grailpib ALTER latestestimate SET DEFAULT NULL;";
	
    @Override
    public String getName() {
        return "Modify PIB Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB Table for altering latest estimate to remove default value as 0";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailpib tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailpib")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_LATEST_ESTIMATE);
        	}catch(Exception e){LOGGER.error("Error while updating pib table " + e.getMessage());}
        }
    }
}
