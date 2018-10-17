package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/19/14
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateUserDepartmentsTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "User Departments Table";
    }

    @Override
    public String getDescription() {
        return "This table stores all the departments for synchro users";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateUserDepartmentsTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailuserdepartments")) {
            UpgradeUtils.dropTable("grailuserdepartments");
        }
        UpgradeUtils.executeSQLGenFile("CreateUserDepartmentsTable");
    }
}
