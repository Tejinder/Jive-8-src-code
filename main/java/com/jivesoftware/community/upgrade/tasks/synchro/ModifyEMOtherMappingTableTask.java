package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 11/5/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyEMOtherMappingTableTask implements UpgradeTask {

    private static Logger LOG = Logger.getLogger(ModifyEMOtherMappingTableTask.class);

     
    private static String ADD_COL1 = "ALTER TABLE grailemothersmapping ADD t20_t40 bigint DEFAULT 1";
	
	
	
    @Override
    public String getName() {
        return "Add new columns to Grail End Market Mapping Meta Data Table task for Synchro New Requirements";
    }

    @Override
    public String getDescription() {
        return "Add new columns to Grail End Market Mapping Meta Data Table task for Synchro New Requirements";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailemothersmapping";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailemothersmapping")) {

            try {
                UpgradeUtils.executeStatement(ADD_COL1);
               
            } catch(Exception e) {
                LOG.error("Error while Adding new columns to Grail End Market Mapping Meta Data Table task for Synchro New Requirements " + e.getMessage());
            }
        }
    }
}
