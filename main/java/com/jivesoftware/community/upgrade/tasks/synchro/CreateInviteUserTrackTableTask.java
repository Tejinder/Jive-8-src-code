package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateInviteUserTrackTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Create CreateInviteUserTrackTable Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro for saving the Users invited and track";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateInviteUserTrackTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (!UpgradeUtils.doesTableExist("grailInviteUserTrackTable")) {
            UpgradeUtils.executeSQLGenFile("CreateInviteUserTrackTable");
        }
    }
}
