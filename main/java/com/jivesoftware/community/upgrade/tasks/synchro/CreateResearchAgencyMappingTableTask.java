package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: Tejinder
 * @since: 1.0
 */
public class CreateResearchAgencyMappingTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail ResearchAgencyMappingTableTask";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Research Agency and Research Agency Mapping details.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateResearchAgencyMappingTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
   	public void doTask() throws Exception {
   		if (UpgradeUtils.doesTableExist("grailResearchAgencyMapping")) {
   			UpgradeUtils.dropTable("grailResearchAgencyMapping");
   		}
   		UpgradeUtils.executeSQLGenFile("CreateResearchAgencyMappingTable");
   	}
}
