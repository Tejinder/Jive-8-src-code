package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ModifyRSSynchroToIRISTableTask1 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyRSSynchroToIRISTableTask1.class);
	
	private static String ALTER_COL1 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN description TYPE TEXT;";
	private static String ALTER_COL2 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN bizquestion TYPE TEXT;";
	private static String ALTER_COL3 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN researchobjective TYPE TEXT;";
	private static String ALTER_COL4 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN actionstandard TYPE TEXT;";
	private static String ALTER_COL5 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN researchdesign TYPE TEXT;";
	private static String ALTER_COL6 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN conclusions TYPE TEXT;";
	private static String ALTER_COL7 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN keyfindings TYPE TEXT;";
	private static String ALTER_COL8 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN respondenttype TYPE TEXT;";
	private static String ALTER_COL9 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN samplesize TYPE TEXT;";
	private static String ALTER_COL10 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN researchagency TYPE TEXT;";
	private static String ALTER_COL11 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN relatedstudies TYPE TEXT;";
	private static String ALTER_COL12 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN tags TYPE TEXT;";
	private static String ALTER_COL13 = "ALTER TABLE grailRSSynchroToIRIS ALTER COLUMN irisoptionrationale TYPE TEXT;";
	
	
		

	
	
    @Override
    public String getName() {
        return "Grail RSSynchro to IRIS Table Modify Task for text fields";
    }

    @Override
    public String getDescription() {
        return "This task modifies existing table grailRSSynchroToIRIS by altering the columns from varchar to text fields.";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailRSSynchroToIRIS";
               
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailRSSynchroToIRIS")) {
        	
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1);
	        	UpgradeUtils.executeStatement(ALTER_COL2);
	        	UpgradeUtils.executeStatement(ALTER_COL3);
	        	UpgradeUtils.executeStatement(ALTER_COL4);
	        	UpgradeUtils.executeStatement(ALTER_COL5);
	        	UpgradeUtils.executeStatement(ALTER_COL6);
	        	UpgradeUtils.executeStatement(ALTER_COL7);
	        	UpgradeUtils.executeStatement(ALTER_COL8);
	        	UpgradeUtils.executeStatement(ALTER_COL9);
	        	UpgradeUtils.executeStatement(ALTER_COL10);
	        	UpgradeUtils.executeStatement(ALTER_COL11);
	        	UpgradeUtils.executeStatement(ALTER_COL12);
	        	UpgradeUtils.executeStatement(ALTER_COL13);
	        	
	        	
        	}catch(Exception e){LOGGER.error("Error while altering the columns from varchar to text fields of grailRSSynchroToIRIS table " + e.getMessage());}
        }
    }
}
