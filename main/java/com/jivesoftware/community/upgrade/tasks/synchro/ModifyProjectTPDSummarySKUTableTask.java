package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * 
 * User: Tejinder
 * Date: 4/1/17
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyProjectTPDSummarySKUTableTask implements UpgradeTask {
    private static Logger LOG = Logger.getLogger(ModifyProjectTPDSummarySKUTableTask.class);
    
    private static String ADD_PRODUCT_MODIFICATION = "ALTER TABLE grailprojecttpdskudetails ADD hasproductmodification integer;";
    private static String ADD_TPD_MODIFICATION_DATE = "ALTER TABLE grailprojecttpdskudetails ADD tpdmodificationdate bigint;";
    private static String ADD_TAO_CODE = "ALTER TABLE grailprojecttpdskudetails ADD taocode VARCHAR(1000);";
    
    
    @Override
    public String getName() {
        return "Modify Project TPD Summary SKU task for New Synchro Requirements";
    }

    @Override
    public String getDescription() {
        return "This task modifies 'grailprojecttpdskudetails' table to add new column to capture new Synchro Requirements ";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailprojecttpdskudetails tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojecttpdskudetails")) {
            try {
                UpgradeUtils.executeStatement(ADD_PRODUCT_MODIFICATION);
                UpgradeUtils.executeStatement(ADD_TPD_MODIFICATION_DATE);
                UpgradeUtils.executeStatement(ADD_TAO_CODE);
             
              
            } catch(Exception e) {
                LOG.error("Error while updating grailprojecttpdskudetails table " + e.getMessage());
            }
        }
    }
}

