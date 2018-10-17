package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifySpecsTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifySpecsTableTask.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailprojectspecsemdetails ADD operationalHubCost numeric;";
	private static String ADD_COL2 = "ALTER TABLE grailprojectspecsemdetails ADD operationalHubCostType bigint;";
	
    @Override
    public String getName() {
        return "Modify EM Proposal details Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Project Specs Table for adding new operationalHubCost and operationalHubCostType";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailprojectspecsemdetails tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectspecsemdetails")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_COL1);
	        	UpgradeUtils.executeStatement(ADD_COL2);
	        	
        	}catch(Exception e){LOGGER.error("Error while updating Specs EM details table " + e.getMessage());}
        }
    }
}
