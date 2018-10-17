package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyProposalTableTask5 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProposalTableTask5.class);
	

	
	
	private static String ADD_PROPOSAL_COLUMN = "ALTER TABLE grailproposal ADD proposal TEXT ";
	private static String ADD_PROPOSAL_TEXT_COLUMN = "ALTER TABLE grailproposal ADD proposaltext TEXT ";
	private static String ADD_PROPOSAL_LEGAL_APPROVER = "ALTER TABLE grailproposal ADD proposallegalapprover bigint ";
	private static String ADD_IS_APPROVED_COLUMN = "ALTER TABLE grailproposal ADD isproposalapproved integer ";
	private static String ADD_APPROVAL_DATE_COLUMN = "ALTER TABLE grailproposal ADD proposalapprovaldate bigint ";
	private static String ADD_LEGAL_SIGNOFF_REQ_COLUMN = "ALTER TABLE grailproposal ADD islegalsignoffreq integer ";
	private static String ADD_SEND_FOR_APPROVAL_COLUMN = "ALTER TABLE grailproposal ADD proposalsendforapproval integer ";
	private static String ADD_NEED_DISCUSSION_COLUMN = "ALTER TABLE grailproposal ADD proposalneedsdiscussion integer ";
	private static String ADD_PROPOSAL_LEGAL_APPROVAL_STATUS_COLUMN = "ALTER TABLE grailproposal ADD proposallegalapprovalstatus integer ";
	
	
	
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
            		UpgradeUtils.executeStatement(ADD_PROPOSAL_COLUMN);
                    UpgradeUtils.executeStatement(ADD_PROPOSAL_TEXT_COLUMN);
                    UpgradeUtils.executeStatement(ADD_PROPOSAL_LEGAL_APPROVER);
                    UpgradeUtils.executeStatement(ADD_IS_APPROVED_COLUMN);                
                    UpgradeUtils.executeStatement(ADD_APPROVAL_DATE_COLUMN);
                    UpgradeUtils.executeStatement(ADD_LEGAL_SIGNOFF_REQ_COLUMN);
                    UpgradeUtils.executeStatement(ADD_SEND_FOR_APPROVAL_COLUMN);
                    UpgradeUtils.executeStatement(ADD_NEED_DISCUSSION_COLUMN);
                    UpgradeUtils.executeStatement(ADD_PROPOSAL_LEGAL_APPROVAL_STATUS_COLUMN);
                    
    	        	
    	        	
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Proposal Table " + e.getMessage());}
            }
           
    }
}
