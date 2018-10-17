package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 6/19/14
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyProjectTableTask9 implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyProjectTableTask9.class);
    private static String ADD_IS_CANCEL_COLUMN = "ALTER TABLE grailproject ADD iscancel integer DEFAULT 0;";
     
    
    @Override
    public String getName() {
        return "Modify Project Table task for New Synchro Requirements";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailproject' table to add new column to capture new Synchro Requirements for cancel Projects";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProject")) {
            try {
                UpgradeUtils.executeStatement(ADD_IS_CANCEL_COLUMN);
              
            } catch(Exception e) {
                LOG.error("Error while updating project table " + e.getMessage());
            }
        }
    }
}

