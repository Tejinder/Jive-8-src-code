package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyProposalTableTask4 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProposalTableTask3.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailproposal ADD proposalsavedate bigint";
	private static String ADD_COL2 = "ALTER TABLE grailproposal ADD proposalsubmitdate bigint ";
	private static String ADD_COL3 = "ALTER TABLE grailproposal ADD reqclarificationreqdate bigint ";
	private static String ADD_COL4 = "ALTER TABLE grailproposal ADD propsendtoownerdate bigint ";
	private static String ADD_COL5 = "ALTER TABLE grailproposal ADD proposalawarddate bigint ";
	
	
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
    	        	UpgradeUtils.executeStatement(ADD_COL1);
    	        	UpgradeUtils.executeStatement(ADD_COL2);
    	        	UpgradeUtils.executeStatement(ADD_COL3);
    	        	UpgradeUtils.executeStatement(ADD_COL4);
    	        	UpgradeUtils.executeStatement(ADD_COL5);
    	        	
    	        	
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Proposal Table " + e.getMessage());}
            }
           
    }
}
