package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/9/14
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyKantarBriefTemplateTableTask1 implements UpgradeTask {

    private static final Logger LOGGER = Logger.getLogger(ModifyKantarBriefTemplateTableTask1.class);
    private static String DROP_MARKETS_COLUMN = "ALTER TABLE grailkantarbrieftemplate RENAME COLUMN markets TO marketsText;";
    private static String ADD_MARKETS_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD markets bigint default -1;";



    @Override
    public String getName() {
        return "Modify Kantar Brief Template Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Kantar Brief Template Table for changing column type of markets";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the kantarbrieftemplate tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailkantarbrieftemplate")) {

            try{
                UpgradeUtils.executeStatement(DROP_MARKETS_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - drop of batContact column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_MARKETS_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of batContact column " + e.getMessage());
            }


        }
    }
}
