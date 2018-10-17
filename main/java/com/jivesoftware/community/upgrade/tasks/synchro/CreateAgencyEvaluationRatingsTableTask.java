package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateAgencyEvaluationRatingsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create AgencyEvaluationRatings Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro for saving the Agency evaluation ratings for the project.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateAgencyEvaluationRatingsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailAgencyEvaluationRatings")) {
            UpgradeUtils.executeSQLGenFile("CreateAgencyEvaluationRatingsTable");
        }
    }
}