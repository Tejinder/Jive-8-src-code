package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/9/15
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyInviteUserTrackTableTask implements UpgradeTask {

    private static Logger LOG = Logger.getLogger(ModifyInviteUserTrackTableTask.class);

    private static String ADD_INVITED_BY = "ALTER TABLE grailInviteUserTrackTable ADD invitedBy bigint;";
    private static String ADD_INVITED_DATE = "ALTER TABLE grailInviteUserTrackTable ADD invitedDate bigint;";

    @Override
    public String getName() {
        return "Modify invite user track table";
    }

    @Override
    public String getDescription() {
        return "This task performs altering 'grailInviteUserTrackTable' to track invited user, invited by and invited date";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailInviteUserTrackTable tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailInviteUserTrackTable")) {

            try {
                UpgradeUtils.executeStatement(ADD_INVITED_BY);
            } catch (Exception e) {
                LOG.error("Error on adding 'invitedBy' column to 'grailInviteUserTrackTable'", e);
            }

            try {
                UpgradeUtils.executeStatement(ADD_INVITED_DATE);
            } catch (Exception e) {
                LOG.error("Error on adding 'invitedDate' column to 'grailInviteUserTrackTable'", e);
            }
        }
    }
}
