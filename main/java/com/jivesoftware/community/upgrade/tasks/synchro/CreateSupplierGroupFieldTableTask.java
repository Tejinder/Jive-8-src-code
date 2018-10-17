package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CreateSupplierGroupFieldTableTask implements UpgradeTask {

    @Override
    public String getName() {
        return "Grail Supplier Group Field Task";
    }

    @Override
    public String getDescription() {
        return "This task creates a new table used by synchro to save Supplier Group fields info";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, create the following tables...\n" + "\n"
                + UpgradeUtils.processSQLGenFile("CreateSupplierGroupFieldTable");
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
	public void doTask() throws Exception {
		if (UpgradeUtils.doesTableExist("grailSupplierGroupFields")) {
			UpgradeUtils.dropTable("grailSupplierGroupFields");
		}
		UpgradeUtils.executeSQLGenFile("CreateSupplierGroupFieldTable");
	}
}
