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
public class ModifyProjectTableTask7 implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyProjectTableTask6.class);
    private static String ADD_METH_DETAILS_COLUMN = "ALTER TABLE grailproject ADD methodologydetails VARCHAR(5000); ";
    private static String ADD_PROJECT_TYPE_COLUMN = "ALTER TABLE grailproject ADD projecttype integer ";
    private static String ADD_PROCESS_TYPE_COLUMN = "ALTER TABLE grailproject ADD processtype integer ";
    private static String ADD_FIELDWORK_STUDY_COLUMN = "ALTER TABLE grailproject ADD fieldworkstudy integer ";
    private static String ADD_METH_WAIVER_REQ_COLUMN = "ALTER TABLE grailproject ADD methwaiverreq integer ";
    private static String ADD_BRAND_SP_STUDY_COLUMN = "ALTER TABLE grailproject ADD brandspecificstudy integer ";
    private static String ADD_STUDY_TYPE_COLUMN = "ALTER TABLE grailproject ADD studytype integer ";
    private static String ADD_BUDGET_LOCATION_COLUMN = "ALTER TABLE grailproject ADD budgetlocation bigint ";
    private static String ADD_REF_SYNCHRO_CODE_COLUMN = "ALTER TABLE grailproject ADD refsynchrocode bigint ";
   
    private static String ADD_PROJECT_MANAGER_COLUMN = "ALTER TABLE grailproject ADD projectManager VARCHAR(5000); ";
    
    
    @Override
    public String getName() {
        return "Modify Project Table task for New Synchro Requirements";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailproject' table to add new column to capture new Synchro Requirements";
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
        if (UpgradeUtils.doesTableExist("grailProject")) {
            try {
                UpgradeUtils.executeStatement(ADD_METH_DETAILS_COLUMN);
                UpgradeUtils.executeStatement(ADD_PROJECT_TYPE_COLUMN);
                UpgradeUtils.executeStatement(ADD_PROCESS_TYPE_COLUMN);
                UpgradeUtils.executeStatement(ADD_FIELDWORK_STUDY_COLUMN);                
                UpgradeUtils.executeStatement(ADD_METH_WAIVER_REQ_COLUMN);
                UpgradeUtils.executeStatement(ADD_BRAND_SP_STUDY_COLUMN);
                UpgradeUtils.executeStatement(ADD_STUDY_TYPE_COLUMN);
                UpgradeUtils.executeStatement(ADD_BUDGET_LOCATION_COLUMN);
                UpgradeUtils.executeStatement(ADD_REF_SYNCHRO_CODE_COLUMN);
                UpgradeUtils.executeStatement(ADD_PROJECT_MANAGER_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while updating project table " + e.getMessage());
            }
        }
    }
}

