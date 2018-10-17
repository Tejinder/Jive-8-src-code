package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifySpecsTableTask3 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifySpecsTableTask3.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailprojectspecs ADD categoryType VARCHAR(5000);";
	
    @Override
    public String getName() {
        return "Modify Project Specs Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Project Specs Table for adding new Category Type column";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailprojectspecs tables...\n";
         ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
       // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectspecs")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ADD_COL1);
	        		        	
        	}catch(Exception e){LOGGER.error("Error while updating Specs details table " + e.getMessage());}
        }
    }
}
