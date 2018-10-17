package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Tejinder
 * @since: 1.0
 */
public class CreateResearchAgencyTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Research Agency Table";
    }

    @Override
    public String getDescription() {
        return "This task creates a table used by synchro for storing Research Agency fields ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateResearchAgencyTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {

        if (UpgradeUtils.doesTableExist("grailResearchAgency")) {        	
        	UpgradeUtils.dropTable("grailResearchAgency");
        }
        UpgradeUtils.executeSQLGenFile("CreateResearchAgencyTable");
    }
}
