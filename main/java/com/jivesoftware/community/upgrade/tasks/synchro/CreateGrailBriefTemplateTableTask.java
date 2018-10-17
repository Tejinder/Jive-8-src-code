package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/1/14
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailBriefTemplateTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Brief Template table create task";
    }

    @Override
    public String getDescription() {
        return "This task will create a table called 'grailbrieftemplate' to save brief template details in 'Grail' portal";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateGrailBriefTemplateTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailbrieftemplate")) {
            UpgradeUtils.dropTable("grailbrieftemplate");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailBriefTemplateTable");
    }
}
