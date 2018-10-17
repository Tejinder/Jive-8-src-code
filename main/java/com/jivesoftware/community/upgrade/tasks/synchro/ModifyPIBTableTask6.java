package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyPIBTableTask6 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBTableTask6.class);
	
	private static String ADD_BRIEF_COLUMN = "ALTER TABLE grailpib ADD brief TEXT ";
	private static String ADD_BRIEF_TEXT_COLUMN = "ALTER TABLE grailpib ADD brieftext TEXT ";
	private static String ADD_BRIEF_LEGAL_APPROVER = "ALTER TABLE grailpib ADD brieflegalapprover bigint ";
	private static String ADD_IS_APPROVED_COLUMN = "ALTER TABLE grailpib ADD isbriefapproved integer ";
	private static String ADD_APPROVAL_DATE_COLUMN = "ALTER TABLE grailpib ADD briefapprovaldate bigint ";
	private static String ADD_LEGAL_SIGNOFF_REQ_COLUMN = "ALTER TABLE grailpib ADD islegalsignoffreq integer ";
	private static String ADD_SEND_FOR_APPROVAL_COLUMN = "ALTER TABLE grailpib ADD briefsendforapproval integer ";
	private static String ADD_NEED_DISCUSSION_COLUMN = "ALTER TABLE grailpib ADD briefneedsdiscussion integer ";
	private static String ADD_BRIEF_LEGAL_APPROVAL_STATUS_COLUMN = "ALTER TABLE grailpib ADD brieflegalapprovalstatus integer ";
	
	
    @Override
    public String getName() {
        return "Modify PIB Table task for New Synchro Requirements";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB Table for adding new columns regarding New Synchro Requirements";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailpib tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailpib")) {
        	
        	try{
        		
	        	UpgradeUtils.executeStatement(ADD_BRIEF_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_BRIEF_TEXT_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_BRIEF_LEGAL_APPROVER);
	        	UpgradeUtils.executeStatement(ADD_IS_APPROVED_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_APPROVAL_DATE_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_LEGAL_SIGNOFF_REQ_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_SEND_FOR_APPROVAL_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_NEED_DISCUSSION_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_BRIEF_LEGAL_APPROVAL_STATUS_COLUMN);
	        	
	        	
        	}catch(Exception e){LOGGER.error("Error while updating pib table " + e.getMessage());}
        }
    }
}
