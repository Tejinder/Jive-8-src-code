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
public class ModifyQPRSnapshotProjectTableTask1 implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyQPRSnapshotProjectTableTask1.class);

    private static String ADD_COLUMN1 = "ALTER TABLE grailqprsnapshotproject ADD categorytype VARCHAR(1000);";
    
    @Override
    public String getName() {
        return "Modify QPR SNAPShot Project Table task for New Synchro Requirements for CategoryType";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailqprsnapshotproject' table to add new column to capture new Synchro Requirements for QPR Snapshot";
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
        if (UpgradeUtils.doesTableExist("grailqprsnapshotproject")) {
            try {
                UpgradeUtils.executeStatement(ADD_COLUMN1);
                
              
            } catch(Exception e) {
                LOG.error("Error while updating project table " + e.getMessage());
            }
        }
    }
}

