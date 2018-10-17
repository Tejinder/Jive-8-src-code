package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyProjectTableTask3 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProjectTableTask3.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailproject ADD descriptiontext VARCHAR(3000)";
	private static String COPY_COL1_VALUES = "update grailproject set descriptiontext=description;";
	
    @Override
    public String getName() {
        return "Modify Proposal and Proposal Reporting Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Proposal and Proposal Reporting Table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproposal n grailproposalreporting tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        	
        	if (UpgradeUtils.doesTableExist("grailproject")) {
            	try{
    	        	UpgradeUtils.executeStatement(ADD_COL1);
    	        	UpgradeUtils.executeStatement(COPY_COL1_VALUES);
    	        	
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Proposal Table " + e.getMessage());}
            }
    }
}
