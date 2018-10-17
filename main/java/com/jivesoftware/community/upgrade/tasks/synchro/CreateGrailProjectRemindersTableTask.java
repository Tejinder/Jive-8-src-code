package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/18/15
 * Time: 12:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailProjectRemindersTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create Grail Project Remainders Table Task";
    }

    @Override
    public String getDescription() {
        return "This task performs creating a new table called 'grailprojectreminders'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateGrailProjectRemindersTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectreminders")) {
            UpgradeUtils.dropTable("grailprojectreminders");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailProjectRemindersTable");
    }
}
