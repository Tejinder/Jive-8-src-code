package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyRSSynchroToIRISTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyRSSynchroToIRISTableTask.class);
	
	private static String ALTER_COL1 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN researchAgency TYPE VARCHAR(1000);";
	
	
    @Override
    public String getName() {
        return "Grail RSSynchro to IRIS Table Modify Task";
    }

    @Override
    public String getDescription() {
        return "This task modifies existing table grailRSSynchroToIRIS by altering the columns researchAgency.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailRSSynchroToIRIS";
               
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailRSSynchroToIRIS")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1);
	        	
	        	
        	}catch(Exception e){LOGGER.error("Error while altering the columns researchAgency of grailRSSynchroToIRIS table " + e.getMessage());}
        }
    }
}
