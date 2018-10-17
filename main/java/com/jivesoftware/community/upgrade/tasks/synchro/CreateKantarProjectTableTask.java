package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/29/14
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateKantarProjectTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Kantar Project Table";
    }

    @Override
    public String getDescription() {
        return "This task perform creating a table called 'grailkantarproject' to persist kantar related projects";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateKantarProjectTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarproject")) {
            UpgradeUtils.dropTable("grailkantarproject");
        }
        UpgradeUtils.executeSQLGenFile("CreateKantarProjectTable");
    }
}
