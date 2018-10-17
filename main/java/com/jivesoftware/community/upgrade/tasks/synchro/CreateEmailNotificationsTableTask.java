package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author Tejinder
 */
public class CreateEmailNotificationsTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Create Email Notification Table";
    }

    @Override
    public String getDescription() {
        return "This task will create a new 'grailEmailNotification' table to save the Email notification timestamp ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateEmailNotificationsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailEmailNotification")) {
            UpgradeUtils.dropTable("grailEmailNotification");
        }
        UpgradeUtils.executeSQLGenFile("CreateEmailNotificationsTable");
    }
}
