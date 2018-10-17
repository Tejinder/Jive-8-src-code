package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/1/14
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateDefaultTemplatesTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Synchro Default Templates table creation task";
    }

    @Override
    public String getDescription() {
        return  "Synchro Default Templates table creation task";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateDefaultTemplatesTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("graildefaulttemplates")) {
            UpgradeUtils.dropTable("graildefaulttemplates");
        }
        UpgradeUtils.executeSQLGenFile("CreateDefaultTemplatesTable");
    }
}
