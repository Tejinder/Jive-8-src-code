package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyPIBTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBTableTask.class);
	
	private static String ADD_HASTENDERING_PROCESS = "ALTER TABLE grailpib ADD hasTenderingProcess bigint;";
	private static String ADD_FWCOST = "ALTER TABLE grailpib ADD fieldworkCost numeric;";
	private static String ADD_FWCOST_CURRENCY = "ALTER TABLE grailpib ADD fieldworkCostCurrency bigint;";
	
	
    @Override
    public String getName() {
        return "Modify PIB Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB Table for adding new columns regarding tendering process and fieldwork cost fields";
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
        		
	        	UpgradeUtils.executeStatement(ADD_HASTENDERING_PROCESS);
	        	UpgradeUtils.executeStatement(ADD_FWCOST);
	        	UpgradeUtils.executeStatement(ADD_FWCOST_CURRENCY);
	        	
        	}catch(Exception e){LOGGER.error("Error while updating pib table " + e.getMessage());}
        }
    }
}
