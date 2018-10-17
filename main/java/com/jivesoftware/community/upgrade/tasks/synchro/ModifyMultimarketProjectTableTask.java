package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyMultimarketProjectTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyMultimarketProjectTableTask.class);
	
	private static String REMOVE_COL = "ALTER TABLE grailproject DROP communityID;";
	
	
    @Override
    public String getName() {
        return "Modify Project Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Project Table for removing column communityID";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject tables...\n";
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
        	}catch(Exception e){LOGGER.error("Error while updating project table - removal of communityID column " + e.getMessage());}
        }
    }
}
