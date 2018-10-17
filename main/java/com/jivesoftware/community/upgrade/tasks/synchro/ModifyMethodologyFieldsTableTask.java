package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * 
 * User: Tejinder
 * 
 */
public class ModifyMethodologyFieldsTableTask implements UpgradeTask {
    private static final Logger LOGGER = Logger.getLogger(ModifyMethodologyFieldsTableTask.class);

    private static String ADD_COL1 = "ALTER TABLE grailmethodologyfields ADD islessfrequent int DEFAULT 0;";
    private static String ADD_COL2 = "ALTER TABLE grailmethodologyfields ADD briefexception int DEFAULT 0;";
    private static String ADD_COL3 = "ALTER TABLE grailmethodologyfields ADD proposalexception int DEFAULT 0;";
    private static String ADD_COL4 = "ALTER TABLE grailmethodologyfields ADD agencywaiverexception int DEFAULT 0;";
    private static String ADD_COL5 = "ALTER TABLE grailmethodologyfields ADD repsummaryexception int DEFAULT 0;";

    @Override
    public String getName() {
        return "Modify Methodology Fields Table Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies Grail Methodology Fields Table for adding a methodology exception columns ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailmethodologyfields tables...\n";
        ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
        // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailmethodologyfields")) {

            try{
                UpgradeUtils.executeStatement(ADD_COL1);
                UpgradeUtils.executeStatement(ADD_COL2);
                UpgradeUtils.executeStatement(ADD_COL3);
                UpgradeUtils.executeStatement(ADD_COL4);
                UpgradeUtils.executeStatement(ADD_COL5);

            }catch(Exception e){LOGGER.error("Error while updating grail methodology fields table " + e.getMessage());}
        }
    }
}
