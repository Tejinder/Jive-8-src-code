package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 11/5/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyProjectEvalTableTask implements UpgradeTask {

    private static Logger LOG = Logger.getLogger(ModifyProjectEvalTableTask.class);

     
    private static String ADD_COL1 = "ALTER TABLE grailprojecteval ADD agencyid bigint";
	private static String ADD_COL2 = "ALTER TABLE grailprojecteval ADD agencyrating integer ";
	private static String ADD_COL3 = "ALTER TABLE grailprojecteval ADD agencycomment text ";
	
	
    @Override
    public String getName() {
        return "Add new columns to Grail Project Evaluation Table task for Synchro New Requirements";
    }

    @Override
    public String getDescription() {
        return "This task Add new  columns to  Grail Project Evaluation Table task for Synchro New Requirements";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailprojecteval";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailprojecteval")) {

            try {
                UpgradeUtils.executeStatement(ADD_COL1);
                UpgradeUtils.executeStatement(ADD_COL2);
                UpgradeUtils.executeStatement(ADD_COL3);
               
            } catch(Exception e) {
                LOG.error("Error while Adding new  columns to  Grail Project Evaluation Table task for Synchro New Requirements " + e.getMessage());
            }
        }
    }
}
