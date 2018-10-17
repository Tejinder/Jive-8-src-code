package com.jivesoftware.community.upgrade.tasks.synchro;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/6/15
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyGrailBriefTemplateTableTask1 implements UpgradeTask {

    private static final Logger LOG = Logger.getLogger(ModifyGrailBriefTemplateTableTask1.class);

    private static String DROP_MARKETS_COLUMN = "ALTER TABLE grailbrieftemplate RENAME COLUMN markets TO marketsText;";
    private static String ADD_MARKETS_COLUMN = "ALTER TABLE grailbrieftemplate ADD markets bigint default -1;";
    private static String ADD_BAT_CONTACT_COLUMN = "ALTER TABLE grailbrieftemplate ADD batContact bigint;";
    private static String ADD_METHODOLOGY_TYPE_COLUMN = "ALTER TABLE grailbrieftemplate ADD methodologyType bigint;";
    private static String ADD_FINAL_COST_COLUMN = "ALTER TABLE grailbrieftemplate ADD finalCost decimal;";
    private static String ADD_FINAL_COST_CURRENCY_COLUMN = "ALTER TABLE grailbrieftemplate ADD finalCostCurrency int;";
    private static String ADD_DATA_SOURCE_COLUMN = "ALTER TABLE grailbrieftemplate ADD datasource text;";
    private static String ADD_COMMENTS_COLUMN = "ALTER TABLE grailbrieftemplate ADD comments text;";
    private static String ADD_STATUS_COLUMN = "ALTER TABLE grailbrieftemplate ADD status int default 1;";
    private static String ADD_CREATED_BY_COLUMN = "ALTER TABLE grailbrieftemplate ADD createdby bigint;";
    private static String ADD_CREATION_TIME_COLUMN = "ALTER TABLE grailbrieftemplate ADD creationdate bigint;";
    private static String ADD_MODIFIED_BY_COLUMN = "ALTER TABLE grailbrieftemplate ADD modifiedby bigint;";
    private static String ADD_MODIFICATION_TIME_COLUMN = "ALTER TABLE grailbrieftemplate ADD modificationdate bigint;";

    private static String COPY_DATE_COLUMN_VALUES = "UPDATE grailbrieftemplate set creationdate=captureddatetime,modificationdate=captureddatetime,createdby=sender,modifiedby=sender;";


    @Override
    public String getName() {
        return "Grail Brief Template Table Modify Task";
    }

    @Override
    public String getDescription() {
        return "This task will perform altering grailbrieftemplate table";
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
                UpgradeUtils.executeStatement(DROP_MARKETS_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while renaming market column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_MARKETS_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding market column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_BAT_CONTACT_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding batcontact column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_METHODOLOGY_TYPE_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding methodologytype column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_FINAL_COST_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding finalcost column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_FINAL_COST_CURRENCY_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding finalcostcurrency column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_DATA_SOURCE_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding datasource column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_COMMENTS_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding comments column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_STATUS_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding status column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_CREATED_BY_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding createdby column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_CREATION_TIME_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding creationdate column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_MODIFIED_BY_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding modifiedby column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(ADD_MODIFICATION_TIME_COLUMN);
            } catch(Exception e) {
                LOG.error("Error while adding modificationdate column in grailbrieftemplate table" + e.getMessage());
            }

            try {
                UpgradeUtils.executeStatement(COPY_DATE_COLUMN_VALUES);
            } catch(Exception e) {
                LOG.error("Error while copying date values in grailbrieftemplate table" + e.getMessage());
            }

        }
    }

}
