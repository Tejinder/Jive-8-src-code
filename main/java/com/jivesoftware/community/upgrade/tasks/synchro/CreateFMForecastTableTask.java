package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateFMForecastTableTask  implements UpgradeTask {

    @Override
    public String getName() {
        return "Create FMForecast Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro for saving the Financial forecast details related to the project.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateFMForecastTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailFMForecast")) {
            UpgradeUtils.executeSQLGenFile("CreateFMForecastTable");
        }
    }
}
