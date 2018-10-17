package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/2/15
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyGrailEmailQueriesTableTask implements UpgradeTask {

    private static final Logger LOGGER = Logger.getLogger(ModifyGrailEmailQueriesTableTask.class);
    private static String DROP_KANTAR_EMAIL_QUERIES_TABLE = "DROP TABLE IF EXISTS grailkantaremailqueries;";
    private static String ADD_TYPE_COLUMN = "ALTER TABLE grailemailqueries ADD type bigint;";



    @Override
    public String getName() {
        return "Modify Grail Email Queries Table task";
    }

    @Override
    public String getDescription() {
        return "This task performs altering/adding 'grailemailqueries' table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailemailqueries tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {

        if (UpgradeUtils.doesTableExist("grailkantaremailqueries")) {
            try{
                UpgradeUtils.executeStatement(DROP_KANTAR_EMAIL_QUERIES_TABLE);
            } catch(Exception e){
                LOGGER.error("Error while droping table grailkantaremailqueries" + e.getMessage());
            }
        }

        if (!UpgradeUtils.doesTableExist("grailemailqueries")) {
            UpgradeUtils.executeSQLGenFile("CreateGrailEmailQueriesTable");
        }
    }
}
