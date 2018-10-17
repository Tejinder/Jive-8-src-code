package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyPIBStakeholderTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBStakeholderTableTask.class);
	
	private static String ADD_PRODUCT_CONTACT = "ALTER TABLE grailpibstakeholderlist ADD productContact bigint;";
	
    @Override
    public String getName() {
        return "Modify PIB Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB Stakeholders Table for adding new column Product Contact";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailpibstakeholderlist table...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailpibstakeholderlist")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_PRODUCT_CONTACT);
        	}catch(Exception e){LOGGER.error("Error while updating pib stakeholder list table " + e.getMessage());}
        }
    }
}
