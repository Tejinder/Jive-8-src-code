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
public class ModifyProjectTPDSummaryTableTask2 implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyProjectTPDSummaryTableTask2.class);
    private static String REMOVE_PREVIOUSLY_SUBMITTED = "ALTER TABLE grailprojecttpdsummary DROP COLUMN previouslysubmitted;";
    private static String REMOVE_LAST_SUBMISSION_DATE = "ALTER TABLE grailprojecttpdsummary DROP COLUMN lastsubmissiondate;";
    
    private static String ADD_PREVIOUSLY_SUBMITTED = "ALTER TABLE grailproject ADD tpdpreviouslysubmitted integer DEFAULT 0;";
    private static String ADD_LAST_SUBMISSION_DATE = "ALTER TABLE grailproject ADD tpdlastsubmissiondate bigint DEFAULT 0;";
    
    
    @Override
    public String getName() {
        return "Modify Project TPD Summary task for New Synchro Requirements";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailprojecttpdsummary' table to add new column to capture new Synchro Requirements ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailprojecttpdsummary tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojecttpdsummary")) {
            try {
                UpgradeUtils.executeStatement(REMOVE_PREVIOUSLY_SUBMITTED);
                UpgradeUtils.executeStatement(REMOVE_LAST_SUBMISSION_DATE);
                UpgradeUtils.executeStatement(ADD_PREVIOUSLY_SUBMITTED);
                UpgradeUtils.executeStatement(ADD_LAST_SUBMISSION_DATE);
              
            } catch(Exception e) {
                LOG.error("Error while updating grailprojecttpdsummary table " + e.getMessage());
            }
        }
    }
}

