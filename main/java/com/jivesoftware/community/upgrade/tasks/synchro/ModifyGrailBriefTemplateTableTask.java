package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/6/14
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyGrailBriefTemplateTableTask implements UpgradeTask {

    private static final Logger LOG = Logger.getLogger(ModifyGrailBriefTemplateTableTask.class);

    private static final String ADD_ISDRAFT_COLUMN = "ALTER TABLE grailbrieftemplate ADD isdraft int DEFAULT 0";

    @Override
    public String getName() {
        return "Grail Brief Template Table Modify Task";
    }

    @Override
    public String getDescription() {
        return "This task will add new column 'isDraft' to grailbrieftemplate table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_ONE_MINUTE;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table 'grailbrieftemplate' table";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailbrieftemplate")) {
            try {
                UpgradeUtils.executeStatement(ADD_ISDRAFT_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding 'isDraft' column to grailbrieftemplate table" + e.getMessage());
            }
        }
    }
}
