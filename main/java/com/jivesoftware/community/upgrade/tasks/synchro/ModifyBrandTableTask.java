package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 4/27/17
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyBrandTableTask implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyBrandTableTask.class);
    private static String ADD_COLUMN1 = "ALTER TABLE grailbrandfields ADD brandtype integer;";
    
    
    
    @Override
    public String getName() {
        return "Modify GRAIL Brand Table task for New Synchro Requirements for adding Brand Type";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailbrandfields' table to add new column to capture Brand Type";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailbrandfields tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailbrandfields")) {
            try {
                UpgradeUtils.executeStatement(ADD_COLUMN1);
              
            } catch(Exception e) {
                LOG.error("Error while updating grailbrandfields table " + e.getMessage());
            }
        }
    }
}

