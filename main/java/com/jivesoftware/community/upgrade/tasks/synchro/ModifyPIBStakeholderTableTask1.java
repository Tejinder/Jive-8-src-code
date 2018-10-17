package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyPIBStakeholderTableTask1 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBStakeholderTableTask1.class);
	
	private static String ADD_OTHER_SPI_CONTACT = "ALTER TABLE grailpibstakeholderlist ADD otherspicontact VARCHAR(1000)";
	private static String ADD_OTHER_LEGAL_CONTACT = "ALTER TABLE grailpibstakeholderlist ADD otherlegalcontact VARCHAR(1000)";
	private static String ADD_OTHER_PRODUCT_CONTACT = "ALTER TABLE grailpibstakeholderlist ADD otherproductcontact VARCHAR(1000)";
	
    @Override
    public String getName() {
        return "Modify PIB Stakeholder List Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB Stakeholders Table for adding new column Other SPI Contact, Other Legal Contact, Other Product Contact";
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
	        	UpgradeUtils.executeStatement(ADD_OTHER_SPI_CONTACT);
	        	UpgradeUtils.executeStatement(ADD_OTHER_LEGAL_CONTACT);
	        	UpgradeUtils.executeStatement(ADD_OTHER_PRODUCT_CONTACT);
        	}catch(Exception e){LOGGER.error("Error while updating pib stakeholder list table " + e.getMessage());}
        }
    }
}
