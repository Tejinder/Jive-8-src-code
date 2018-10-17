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
public class ModifyQPRSnapshotProjectTableTask2 implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyQPRSnapshotProjectTableTask2.class);

    private static String ADD_COLUMN1 = "ALTER TABLE grailqprsnapshotproject ADD region VARCHAR(1000);";
    private static String ADD_COLUMN2 = "ALTER TABLE grailqprsnapshotproject ADD area VARCHAR(1000);";
    private static String ADD_COLUMN3 = "ALTER TABLE grailqprsnapshotproject ADD t2040 VARCHAR(1000);";
    private static String ADD_COLUMN4 = "ALTER TABLE grailqprsnapshotproject ADD methgroup VARCHAR(1000);";
    private static String ADD_COLUMN5 = "ALTER TABLE grailqprsnapshotproject ADD brandtype VARCHAR(1000);";
   // private static String ADD_COLUMN6 = "ALTER TABLE grailqprsnapshotproject ADD agencytype integer;";
    
    
    @Override
    public String getName() {
        return "Modify QPR SNAPShot Project Table task for New Synchro Requirements for Meta Data Fields";
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
                UpgradeUtils.executeStatement(ADD_COLUMN2);
                UpgradeUtils.executeStatement(ADD_COLUMN3);
                UpgradeUtils.executeStatement(ADD_COLUMN4);
                UpgradeUtils.executeStatement(ADD_COLUMN5);
               // UpgradeUtils.executeStatement(ADD_COLUMN6);
               } catch(Exception e) {
                LOG.error("Error while updating project table " + e.getMessage());
            }
        }
    }
}

