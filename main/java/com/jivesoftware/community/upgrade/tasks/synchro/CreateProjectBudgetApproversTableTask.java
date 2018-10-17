package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateProjectBudgetApproversTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create ProjectBudgetApprovers Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to map Project and their BudgetApprovers";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectBudgetApproversTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailProjectBudgetApprovers")) {
            UpgradeUtils.executeSQLGenFile("CreateProjectBudgetApproversTable");
        }
    }
}
