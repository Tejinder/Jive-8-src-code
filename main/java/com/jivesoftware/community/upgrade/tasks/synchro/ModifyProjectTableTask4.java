package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Tejinder
 * @since: 1.0
 */
public class ModifyProjectTableTask4 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProjectTableTask4.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailproject ADD region VARCHAR(3000)";
	private static String ADD_COL2 = "ALTER TABLE grailproject ADD area VARCHAR(3000)";
	
	
    @Override
    public String getName() {
        return "Modify Project Table task for adding Region and Area columns";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Project Table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject ...\n";
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
    	        	UpgradeUtils.executeStatement(ADD_COL2);
    	        	
    	        	
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table " + e.getMessage());}
            }
    }
}
