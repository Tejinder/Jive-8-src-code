package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyPIBAndProjectTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBAndProjectTableTask.class);
	
	//Update Project Table
	private static String ALTER_COL1_PROJECT = "ALTER TABLE grailproject ALTER COLUMN description TYPE VARCHAR(6000);";
	
	//Update PIB Table
	private static String ALTER_COL1_PIB = "ALTER TABLE grailpib ALTER COLUMN bizquestion TYPE VARCHAR(3000);";
	private static String ALTER_COL2_PIB = "ALTER TABLE grailpib ALTER COLUMN researchobjective TYPE VARCHAR(3000);";
	private static String ALTER_COL3_PIB = "ALTER TABLE grailpib ALTER COLUMN actionstandard TYPE VARCHAR(3000);";
	private static String ALTER_COL4_PIB = "ALTER TABLE grailpib ALTER COLUMN researchdesign TYPE VARCHAR(8000);";
	private static String ALTER_COL5_PIB = "ALTER TABLE grailpib ALTER COLUMN sampleprofile TYPE VARCHAR(3000);";
	private static String ALTER_COL6_PIB = "ALTER TABLE grailpib ALTER COLUMN stimulusmaterial TYPE VARCHAR(3000);";
	private static String ALTER_COL7_PIB = "ALTER TABLE grailpib ALTER COLUMN others TYPE VARCHAR(3000);";
	
	//Update PIB Reporting Table
	private static String ALTER_COL1_PIBREPORTING = "ALTER TABLE grailpibreporting ALTER COLUMN otherreportingrequirements TYPE VARCHAR(3000);";
	
    @Override
    public String getName() {
        return "Grail Project, PIB & Reporting Modify Task for Textarea editors";
    }

    @Override
    public String getDescription() {
        return "This task modifies existing Project, PIB & Reporting Tables by altering the textarea columns for increasing their size approx. 2.5 folds";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailproject \b grailpib \b grailpibreporting";
               
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailproject")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PROJECT);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of Project table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailpib")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL2_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL3_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL4_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL5_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL6_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL7_PIB);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of PIB table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailpibreporting")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PIBREPORTING);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
    }
}
