package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Tejinder
 * @since: 1.0
 */
public class ModifyProjectReportSummaryTask2 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProjectReportSummaryTask2.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailprojectrepsummary ADD needrevisionclicked int DEFAULT 0;";
	
	
    @Override
    public String getName() {
        return "Modify grailprojectrepsummary Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail grailprojectrepsummary Table for adding new columns";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailprojectrepsummary tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectrepsummary")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_COL1);
	        	
	        	}catch(Exception e){LOGGER.error("Error while updating grailprojectrepsummary table - adding new columns - needrevisionclicked" + e.getMessage());}
        }
    }
}
