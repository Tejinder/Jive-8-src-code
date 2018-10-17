package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyWaiverTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyWaiverTableTask.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailwaiver ADD iskantar int DEFAULT 0;";
	
    @Override
    public String getName() {
        return "Modify Grail Waiver Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Waiver Table for adding new isKantar Type column";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailwaiver tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailwaiver")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_COL1);
	        		        	
        	}catch(Exception e){LOGGER.error("Error while updating Grail Waiver  table " + e.getMessage());}
        }
    }
}
