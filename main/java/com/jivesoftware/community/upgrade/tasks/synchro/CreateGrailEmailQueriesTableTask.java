package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/4/14
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGrailEmailQueriesTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Kantar Email Queries Table";
    }

    @Override
    public String getDescription() {
        return "This task performs creating a new table called 'grailemailqueries' to store all email queries from grail button, kantar button, kantar report";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateGrailEmailQueriesTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailemailqueries")) {
            UpgradeUtils.dropTable("grailemailqueries");
        }
        UpgradeUtils.executeSQLGenFile("CreateGrailEmailQueriesTable");
    }
}
