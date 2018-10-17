package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyPIBTableTask4 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBTableTask4.class);
	
	private static String ADD_NON_KANTAR = "ALTER TABLE grailpib ADD nonKantar int DEFAULT 0;";
	
	
	
    @Override
    public String getName() {
        return "Modify PIB Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB Table for adding new columns regarding non Kantar fields";
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
        		
	        	UpgradeUtils.executeStatement(ADD_NON_KANTAR);
	        	
	        	
        	}catch(Exception e){LOGGER.error("Error while updating pib table " + e.getMessage());}
        }
    }
}
