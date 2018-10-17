package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 11/5/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyProjectSpecsTableTask implements UpgradeTask {

    private static Logger LOG = Logger.getLogger(ModifyProjectSpecsTableTask.class);

    private static String ADD_PO1_COLUMN = "ALTER TABLE grailprojectspecs ADD COLUMN ponumber1 VARCHAR(1000) DEFAULT NULL";

    @Override
    public String getName() {
        return "Add new ponumber1 column to Grail Project Specs Table task";
    }

    @Override
    public String getDescription() {
        return "This task performs adding a new column called 'ponumber1' to 'grailprojectspecs' table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailprojectspecs";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojectspecs")) {

            try {
                UpgradeUtils.executeStatement(ADD_PO1_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding 'ponumber1' column to grailprojectspecs table " + e.getMessage());
            }
        }
    }
}
