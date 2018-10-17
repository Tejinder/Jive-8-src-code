package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/16/14
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateKantarBriefTemplateTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Kantar Brief Template table create task";
    }

    @Override
    public String getDescription() {
        return "This task will create a table called 'grailkantarbrieftemplate' to save brief template details in 'Kantar' portal";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateKantarBriefTemplateTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarbrieftemplate")) {
            UpgradeUtils.dropTable("grailkantarbrieftemplate");
        }
        UpgradeUtils.executeSQLGenFile("CreateKantarBriefTemplateTable");
    }
}
