package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyActivityTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyActivityTableTask.class);
	
	private static String ADD_WAIVER_ID_COLUMN = "ALTER TABLE grailactivitylog ADD waiverid bigint ";
	
	
    @Override
    public String getName() {
        return "Modify Grail Activity Log Table task for Adding Audit Logs for Process Waiver";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Activity Log Table for adding new columns regarding New Synchro Requirements";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailactivitylog tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailactivitylog")) {
        	
        	try{
        		
	        	UpgradeUtils.executeStatement(ADD_WAIVER_ID_COLUMN);
	        	
	        	
        	}catch(Exception e){LOGGER.error("Error while updating grailactivitylog table " + e.getMessage());}
        }
    }
}
