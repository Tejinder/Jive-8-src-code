package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateProjectReportSummaryDetailsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create New Project Report Summary Table task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by new synchro for Report Summary.";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectReportSummaryDetailsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectRepSummaryDetails")) {        	
        	UpgradeUtils.dropTable("grailProjectRepSummaryDetails");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectReportSummaryDetailsTable");
    }
}
