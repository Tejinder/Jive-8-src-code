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
public class ModifyQPRSnapshotTableTask implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyQPRSnapshotTableTask.class);

    private static String ADD_COLUMN1 = "ALTER TABLE grailqprsnapshot ADD openbudgetlocation VARCHAR(1000);";
    
    @Override
    public String getName() {
        return "Modify QPR SNAPShot Table task for New Synchro Requirements for Open Budget Location";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailqprsnapshot' table to add new column to capture new Synchro Requirements for QPR Snapshot";
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
        if (UpgradeUtils.doesTableExist("grailqprsnapshot")) {
            try {
                UpgradeUtils.executeStatement(ADD_COLUMN1);
                
              
            } catch(Exception e) {
                LOG.error("Error while updating project table " + e.getMessage());
            }
        }
    }
}

