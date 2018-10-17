package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyPIBTableTask5 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBTableTask4.class);
	
	private static String ADD_SAVE_CREATION_TIME_COLUMN = "ALTER TABLE grailpib ADD pibsavedate bigint ";
	private static String ADD_LEGAL_APPROVAL_TIME_COLUMN = "ALTER TABLE grailpib ADD piblegalapprovaldate bigint ";
	private static String ADD_PIB_COMPLETE_TIME_COLUMN = "ALTER TABLE grailpib ADD pibcompletiondate bigint ";
	private static String ADD_NOTIFY_AM_CONTACTS_TIME_COLUMN = "ALTER TABLE grailpib ADD pibnotifyamcontactsdate bigint ";

	
	
	
	
    @Override
    public String getName() {
        return "Modify PIB Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB Table for adding new columns regarding Pending Action fields";
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
        		
	        	UpgradeUtils.executeStatement(ADD_SAVE_CREATION_TIME_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_LEGAL_APPROVAL_TIME_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_PIB_COMPLETE_TIME_COLUMN);
	        	UpgradeUtils.executeStatement(ADD_NOTIFY_AM_CONTACTS_TIME_COLUMN);
	        	
	        	
        	}catch(Exception e){LOGGER.error("Error while updating pib table " + e.getMessage());}
        }
    }
}
