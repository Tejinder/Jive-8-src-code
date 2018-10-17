package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * 
 * User: Tejinder
 * Date: 4/1/17
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyProjectTPDSummaryTableTask3 implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyProjectTPDSummaryTableTask3.class);
    
    private static String ADD_LEGAL_APPROVAL_STATUS = "ALTER TABLE grailprojecttpdsummary ADD legalApprovalStatus integer;";
    private static String ADD_LEGAL_APPROVER_NAME = "ALTER TABLE grailprojecttpdsummary ADD legalApprover bigint;";
    private static String ADD_LEGAL_APPROVAL_DATE = "ALTER TABLE grailprojecttpdsummary ADD legalApprovalDate bigint;";
    
    
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
                UpgradeUtils.executeStatement(ADD_LEGAL_APPROVAL_STATUS);
                UpgradeUtils.executeStatement(ADD_LEGAL_APPROVER_NAME);
                UpgradeUtils.executeStatement(ADD_LEGAL_APPROVAL_DATE);
             
              
            } catch(Exception e) {
                LOG.error("Error while updating grailprojecttpdsummary table " + e.getMessage());
            }
        }
    }
}

