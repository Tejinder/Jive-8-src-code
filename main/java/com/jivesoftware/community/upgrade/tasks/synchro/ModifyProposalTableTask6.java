package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyProposalTableTask6 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProposalTableTask6.class);
	

	
	private static String ADD_SEND_REMINDER_DATE_COLUMN = "ALTER TABLE grailproposal ADD sendreminderdate bigint ";
	
	
	
    @Override
    public String getName() {
        return "Modify Proposal Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Proposal Table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproposal tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        	
        	if (UpgradeUtils.doesTableExist("grailproposal")) {
            	try{
            		UpgradeUtils.executeStatement(ADD_SEND_REMINDER_DATE_COLUMN);
                    
    	        	
    	        	
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Proposal Table " + e.getMessage());}
            }
           
    }
}
