package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/21/14
 * Time: 6:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyKantarBriefTemplateTableTask implements UpgradeTask {
    private static final Logger LOGGER = Logger.getLogger(ModifyKantarBriefTemplateTableTask.class);

    private static String ADD_COL = "ALTER TABLE grailkantarbrieftemplate ADD batContact VARCHAR(255);";

    private static String DROP_BAT_CONTACT_COLUMN = "ALTER TABLE grailkantarbrieftemplate RENAME COLUMN batContact TO batContactText;";
    private static String ADD_BAT_CONTACT_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD batContact bigint;";
    private static String ADD_METHODOLOGY_TYPE_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD methodologyType bigint;";
    private static String ADD_FINAL_COST_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD finalCost decimal;";
    private static String ADD_FINAL_COST_CURRENCY_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD finalCostCurrency int;";
    private static String ADD_DATA_SOURCE_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD datasource text;";
    private static String ADD_COMMENTS_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD comments text;";
    private static String ADD_STATUS_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD status int default 1;";
    private static String ADD_CREATED_BY_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD createdby bigint;";
    private static String ADD_CREATION_TIME_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD creationdate bigint;";
    private static String ADD_MODIFIED_BY_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD modifiedby bigint;";
    private static String ADD_MODIFICATION_TIME_COLUMN = "ALTER TABLE grailkantarbrieftemplate ADD modificationdate bigint;";

    private static String COPY_DATE_COLUMN_VALUES = "UPDATE grailkantarbrieftemplate set creationdate=captureddatetime,modificationdate=captureddatetime,createdby=sender,modifiedby=sender;";





    @Override
    public String getName() {
        return "Modify Kantar Brief Template Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Kantar Brief Template Table for adding column batContact";
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

//            try{
//                UpgradeUtils.executeStatement(ADD_COL);
//            } catch(Exception e){
//                LOGGER.error("Error while updating grailkantarbrieftemplate table - drop of batContact column " + e.getMessage());
//            }

            try{
                UpgradeUtils.executeStatement(DROP_BAT_CONTACT_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - drop of batContact column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_BAT_CONTACT_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of batContact column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_METHODOLOGY_TYPE_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of methodologyType column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_FINAL_COST_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of finalCost column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_FINAL_COST_CURRENCY_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of finalCostCurrency column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_DATA_SOURCE_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of dataSource column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_COMMENTS_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of comments column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_STATUS_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of status column " + e.getMessage());
            }




            try{
                UpgradeUtils.executeStatement(ADD_CREATED_BY_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of createdby column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_CREATION_TIME_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of creationdate column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_MODIFIED_BY_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of modifiedby column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(ADD_MODIFICATION_TIME_COLUMN);
            } catch(Exception e){
                LOGGER.error("Error while updating grailkantarbrieftemplate table - add of modificationdate column " + e.getMessage());
            }

            try{
                UpgradeUtils.executeStatement(COPY_DATE_COLUMN_VALUES);
            } catch(Exception e){
                LOGGER.error("Error while copying grailkantarbrieftemplate table values" + e.getMessage());
            }
        }
    }
}
