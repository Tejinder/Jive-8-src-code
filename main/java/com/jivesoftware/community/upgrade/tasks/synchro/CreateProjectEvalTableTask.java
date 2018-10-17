package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class CreateProjectEvalTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Project Eval Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Project Eval details.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateProjectEvalTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProjectEval")) {        	
        	UpgradeUtils.dropTable("grailProjectEval");
        }
        UpgradeUtils.executeSQLGenFile("CreateProjectEvalTable");
    }
}
