package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyProposalTableTask8 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProposalTableTask8.class);

	private static String ADD_PROPOSAL_LEGAL_APPROVER_OFFLINE = "ALTER TABLE grailproposal ADD proposallegalapproveroffline VARCHAR(3000) ";
	
	
    @Override
    public String getName() {
        return "Modify PROPOSAL Table task for New Synchro Requirements";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PROPOSAL Table for adding new columns regarding New Synchro Requirements";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproposal tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailproposal")) {
        	
        	try{
        		
	        	UpgradeUtils.executeStatement(ADD_PROPOSAL_LEGAL_APPROVER_OFFLINE);
	        	
        	}catch(Exception e){LOGGER.error("Error while updating proposal table " + e.getMessage());}
        }
    }
}
