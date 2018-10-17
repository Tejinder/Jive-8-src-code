package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/30/15
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyKantarReportTypeTableTask implements UpgradeTask {
    private static final Logger LOGGER = Logger.getLogger(ModifyKantarReportTypeTableTask.class);
    private static String ADD_COLUMN = "ALTER TABLE grailkantarreporttype ADD sortorder int default 0;";



    @Override
    public String getName() {
        return "Modify Kantar Report Type Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Kantar Report Type Table for adding new column 'sortorder'";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailkantarreporttype tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarreporttype")) {
            try{
                UpgradeUtils.executeStatement(ADD_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarreporttype table - add of sortorder column " + e.getMessage());
            }


        }
    }
}
