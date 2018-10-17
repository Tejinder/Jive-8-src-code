package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateFMPrePlanTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create FinancialPrePlan Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to store Financial pre-plan details of the project.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateFMPrePlanTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailFMPrePlan")) {
            UpgradeUtils.executeSQLGenFile("CreateFMPrePlanTable");
        }
    }
}
