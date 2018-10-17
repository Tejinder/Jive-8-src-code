package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 5/12/14
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateProjectCostFieldsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Project Cost Fields table task";
    }

    @Override
    public String getDescription() {
        return "This table to used capture all cost fields available in each project";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectCostFieldsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectcostfields")) {
            UpgradeUtils.dropTable("grailprojectcostfields");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectCostFieldsTable");
    }
}
