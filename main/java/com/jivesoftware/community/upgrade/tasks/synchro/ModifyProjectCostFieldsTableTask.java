package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 9/6/14
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyProjectCostFieldsTableTask  implements UpgradeTask {
    private static final Logger LOG = Logger.getLogger(ModifyProjectCostFieldsTableTask.class);
    private static String ADD_UPDATED_DATE_TIME_COLUMN = "ALTER TABLE grailprojectcostfields ADD updateddatetime bigint;";

    @Override
    public String getName() {
        return "Modify Project Cost Fields Table Task";
    }

    @Override
    public String getDescription() {
        return "This task performs adding a new column called 'updateddatetime' to the 'grailprojectcostfields' table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailprojectcostfields table...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectcostfields")) {
           UpgradeUtils.executeStatement(ADD_UPDATED_DATE_TIME_COLUMN);
        }
    }
}
