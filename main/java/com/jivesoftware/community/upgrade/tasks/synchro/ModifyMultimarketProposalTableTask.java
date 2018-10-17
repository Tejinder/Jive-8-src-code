package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyMultimarketProposalTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyMultimarketProposalTableTask.class);
	
	private static String ADD_COL = "ALTER TABLE grailproposal ADD caprating bigint DEFAULT -1;";
	
	
    @Override
    public String getName() {
        return "Modify Proposal Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Proposal Table for adding caprating";
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
        if (UpgradeUtils.doesTableExist("grailProposal")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_COL);
        	}catch(Exception e){LOGGER.error("Error while updating proposal table - adding new column - caprating for multimarket project " + e.getMessage());}
        }
    }
}
