package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 11/19/13
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateMyLibraryTableTask implements UpgradeTask {
    @Override
    public String getName() {
        return "Synchro My Library table creation task";
    }

    @Override
    public String getDescription() {
        return  "Synchro My Library table creation task";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateMyLibraryTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailMyLibrary")) {
            UpgradeUtils.dropTable("grailMyLibrary");
        }
        UpgradeUtils.executeSQLGenFile("CreateMyLibraryTable");
    }
}
