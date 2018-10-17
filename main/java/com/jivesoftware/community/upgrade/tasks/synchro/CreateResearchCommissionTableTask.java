package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: vivek
 * @since: 1.0
 */
public class CreateResearchCommissionTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create ResearchCommission Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save the Research Commission details for the project.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateResearchCommissionTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailResearchCommission")) {
            UpgradeUtils.executeSQLGenFile("CreateResearchCommissionTable");
        }
    }
}