package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 6/19/14
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyQPRSnapshotProjectCostTableTask implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyQPRSnapshotProjectCostTableTask.class);

    private static String ADD_COLUMN1 = "ALTER TABLE grailqprsnapshotprojcost ADD agencytype VARCHAR(1000);";
    
    
    @Override
    public String getName() {
        return "Modify QPR SNAPShot Project Cost  task for New Synchro Requirements Fields";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailqprsnapshotprojcost' table to add new column to capture new Synchro Requirements for QPR Snapshot";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
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
        if (UpgradeUtils.doesTableExist("grailqprsnapshotprojcost")) {
            try {
                UpgradeUtils.executeStatement(ADD_COLUMN1);
                
               // UpgradeUtils.executeStatement(ADD_COLUMN6);
               } catch(Exception e) {
                LOG.error("Error while updating project table " + e.getMessage());
            }
        }
    }
}

