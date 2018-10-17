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
public class ModifyProjectSpecsTableTask1 implements UpgradeTask {

    private static Logger LOG = Logger.getLogger(ModifyProjectSpecsTableTask1.class);

     
    private static String ADD_COL1 = "ALTER TABLE grailprojectspecs ADD projectspecssavedate bigint";
	private static String ADD_COL2 = "ALTER TABLE grailprojectspecs ADD pslegalapprovaldate bigint ";
	private static String ADD_COL3 = "ALTER TABLE grailprojectspecs ADD reqclarificationmoddate bigint ";
	
    @Override
    public String getName() {
        return "Add new Pending Activity Completion Dates column to Grail Project Specs Table task";
    }

    @Override
    public String getDescription() {
        return "This task Add new Pending Activity Completion Dates column to Grail Project Specs Table task";
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
                UpgradeUtils.executeStatement(ADD_COL1);
                UpgradeUtils.executeStatement(ADD_COL2);
                UpgradeUtils.executeStatement(ADD_COL3);
            } catch(Exception e) {
                LOG.error("Error while Pending Activity Completion Dates column to Grail Project Specs Table " + e.getMessage());
            }
        }
    }
}
