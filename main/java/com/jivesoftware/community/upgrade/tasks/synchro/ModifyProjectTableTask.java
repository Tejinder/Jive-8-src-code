package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class ModifyProjectTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProjectTableTask.class);
	
	private static String REMOVE_COL = "ALTER TABLE grailproject DROP estimatedcost;";
	private static String ADD_COL1 = "ALTER TABLE grailproject ADD totalCost numeric DEFAULT 0;";
	private static String ADD_COL2 = "ALTER TABLE grailproject ADD totalCostCurrency bigint DEFAULT -1;";
	
    @Override
    public String getName() {
        return "Modify Project Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Project Table for removing and adding new columns";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProject")) {
        	
        	try{
        		
	        	UpgradeUtils.executeStatement(REMOVE_COL);
	        	UpgradeUtils.executeStatement(ADD_COL1);
	        	UpgradeUtils.executeStatement(ADD_COL2);
	        	
        	}catch(Exception e){LOGGER.error("Error while updating project table " + e.getMessage());}
        }
    }
}
