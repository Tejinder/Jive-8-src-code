package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateProjectReportSummaryAttachmentTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Project Report Summary Attachment Details Table Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Project Report Summary Attachment details.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectReportSummaryAttachmentTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectRepSummaryAttach")) {        	
        	UpgradeUtils.dropTable("grailProjectRepSummaryAttach");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectReportSummaryAttachmentTable");
    }
}
