package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/13/15
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyGrailProjectReminderTableTask  implements UpgradeTask {

    private static final Logger LOG = Logger.getLogger(ModifyGrailProjectReminderTableTask.class);

    private static String ADD_COL1 = "ALTER TABLE grailprojectreminders ADD rangeEndType bigint;";
    private static String ADD_COL2 = "ALTER TABLE grailprojectreminders ADD remindToType bigint;";

    @Override
    public String getName() {
        return "Modify Grail Project Reminder Table Task";
    }

    @Override
    public String getDescription() {
        return "This task performs adding 'rangeEndType' and 'remindToType' columns to 'grailprojectreminders' table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailprojectreminders tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectreminders")) {
            try {
                UpgradeUtils.executeStatement(ADD_COL1);
            } catch(Exception e) {
                LOG.error("Error while updating grailprojectreminders Table " + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_COL2);
            } catch(Exception e) {
                LOG.error("Error while updating grailprojectreminders Table " + e.getMessage());
            }
        }
    }
}
