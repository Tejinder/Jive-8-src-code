package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyProjectSpecsEMDetailsTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProjectSpecsEMDetailsTableTask.class);
	
	private static String ALTER_COL1 = "ALTER TABLE grailprojectspecsemdetails ALTER COLUMN latestfwcomments TYPE VARCHAR(2500);";
	private static String ALTER_COL2 = "ALTER TABLE grailprojectspecsemdetails ALTER COLUMN finalcostcomments TYPE VARCHAR(2500);";
	
    @Override
    public String getName() {
        return "Grail Project Specs End Market Details Modify Task";
    }

    @Override
    public String getDescription() {
        return "This task modifies existing table grailProjectSpecsEMDetails by altering the columns latestfwcomments, finalcostcomments used by synchro to save Project Specs Stage End Market details.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailProjectSpecsEMDetails";
               
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectSpecsEMDetails")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1);
	        	UpgradeUtils.executeStatement(ALTER_COL2);
	        	
        	}catch(Exception e){LOGGER.error("Error while altering the columns latestfwcomments, finalcostcomments of grailProjectSpecsEMDetails table " + e.getMessage());}
        }
    }
}
