package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/30/14
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateKantarProjectStakeholdersTableTask implements UpgradeTask {


    @Override
    public String getName() {
        return "Kantar Project Stakeholders Table";
    }

    @Override
    public String getDescription() {
        return "This task perform creating a table called 'grailkantarprojectstakeholder' to persist kantar project stakeholder list";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateKantarProjectStakeholdersTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarprojectstakeholder")) {
            UpgradeUtils.dropTable("grailkantarprojectstakeholder");
        }
        UpgradeUtils.executeSQLGenFile("CreateKantarProjectStakeholdersTable");
    }
}
