package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/23/15
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifySynchroProjectTableTask implements UpgradeTask {
    private static final Logger LOGGER = Logger.getLogger(ModifySynchroProjectTableTask.class);

    private static String ADD_COL1 = "ALTER TABLE grailproject ADD budgetyear bigint;";
    @Override
    public String getName() {
        return "Adding a new column 'budgetyear' in synchro project table task";
    }

    @Override
    public String getDescription() {
        return "Adding a new column 'budgetyear' in synchro project table task";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject table to add 'budgetyear' column...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailproject")) {
            try {
                UpgradeUtils.executeStatement(ADD_COL1);

            } catch(Exception e) {
                LOGGER.error("Error while adding 'budgetyear' column in to 'grailproject' table " + e.getMessage());
            }
        }
    }
}
