package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyPIBKantarMWTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBKantarMWTableTask.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailPIBKantarMW ADD waiverid bigint DEFAULT 0;";
	
    @Override
    public String getName() {
        return "Modify PIB Kantar Waiver Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting PIB Kantar Waiver Table for adding new waiverID Type column";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailPIBKantarMW tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailPIBKantarMW")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_COL1);
	        		        	
        	}catch(Exception e){LOGGER.error("Error while updating PIB Kantar Waiver  table " + e.getMessage());}
        }
    }
}
