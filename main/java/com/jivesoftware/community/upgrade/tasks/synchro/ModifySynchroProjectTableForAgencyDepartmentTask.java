package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/3/15
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifySynchroProjectTableForAgencyDepartmentTask implements UpgradeTask {
    private static final Logger LOGGER = Logger.getLogger(ModifySynchroProjectTableForAgencyDepartmentTask.class);

    private static String ADD_COL1 = "ALTER TABLE grailproject ADD agencyDept bigint DEFAULT -1;";

    @Override
    public String getName() {
        return "Modify Project Table Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies Grail Project Table for adding a new column 'agencyDept' ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject tables...\n";
        ///       + UpgradeUtils.processSQLGenFile("CreateProjectTable");
        // UpgradeUtils.executeStatement(sql)
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailproject")) {

            try{
                UpgradeUtils.executeStatement(ADD_COL1);

            }catch(Exception e){LOGGER.error("Error while updating grail project table " + e.getMessage());}
        }
    }
}
