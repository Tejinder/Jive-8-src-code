package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyPIBTableTask2 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyPIBTableTask2.class);
	
	private static String ALTER_COL_PIB = "ALTER TABLE grailpib ALTER COLUMN stimulusmaterial TYPE VARCHAR(3000);";
	
	private static String ADD_COL1 = "ALTER TABLE grailpib ADD bizquestiontext VARCHAR(3000)";
	private static String ADD_COL2 = "ALTER TABLE grailpib ADD researchobjectivetext VARCHAR(3000)";
	private static String ADD_COL3 = "ALTER TABLE grailpib ADD actionstandardtext VARCHAR(3000)";
	private static String ADD_COL4 = "ALTER TABLE grailpib ADD researchdesigntext VARCHAR(8000)";
	private static String ADD_COL5 = "ALTER TABLE grailpib ADD sampleprofiletext VARCHAR(3000)";
	private static String ADD_COL6 = "ALTER TABLE grailpib ADD stimulusmaterialtext VARCHAR(3000)";//Change to 3000 from 1000
	private static String ADD_COL7 = "ALTER TABLE grailpib ADD otherstext VARCHAR(3000)";
	private static String ADD_COL8 = "ALTER TABLE grailpibreporting ADD otherreportingrequirementstext VARCHAR(3000)";
	
	private static String COPY_COL1_VALUES = "update grailpib set bizquestiontext=bizquestion;";
	private static String COPY_COL2_VALUES = "update grailpib set researchobjectivetext=researchobjective;";
	private static String COPY_COL3_VALUES = "update grailpib set actionstandardtext=actionstandard;";
	private static String COPY_COL4_VALUES = "update grailpib set researchdesigntext=researchdesign;";
	private static String COPY_COL5_VALUES = "update grailpib set sampleprofiletext=sampleprofile;";
	private static String COPY_COL6_VALUES = "update grailpib set stimulusmaterialtext=stimulusmaterial;";
	private static String COPY_COL7_VALUES = "update grailpib set otherstext=others;";
	private static String COPY_COL8_VALUES = "update grailpibreporting set otherreportingrequirementstext=otherreportingrequirements;";
	
	
    @Override
    public String getName() {
        return "Modify PIB and PIB Reporting Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail PIB and PIB Reporting Table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailpib n grailpibreporting tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        if (UpgradeUtils.doesTableExist("grailProject")) {
        	
        	if (UpgradeUtils.doesTableExist("grailpib")) {
            	try{
    	        	UpgradeUtils.executeStatement(ALTER_COL_PIB);
    	        	UpgradeUtils.executeStatement(ADD_COL1);
    	        	UpgradeUtils.executeStatement(ADD_COL2);
    	        	UpgradeUtils.executeStatement(ADD_COL3);
    	        	UpgradeUtils.executeStatement(ADD_COL4);
    	        	UpgradeUtils.executeStatement(ADD_COL5);
    	        	UpgradeUtils.executeStatement(ADD_COL6);
    	        	UpgradeUtils.executeStatement(ADD_COL7);
    	        	
    	        	UpgradeUtils.executeStatement(COPY_COL1_VALUES);
    	        	UpgradeUtils.executeStatement(COPY_COL2_VALUES);
    	        	UpgradeUtils.executeStatement(COPY_COL3_VALUES);
    	        	UpgradeUtils.executeStatement(COPY_COL4_VALUES);
    	        	UpgradeUtils.executeStatement(COPY_COL5_VALUES);
    	        	UpgradeUtils.executeStatement(COPY_COL6_VALUES);
    	        	UpgradeUtils.executeStatement(COPY_COL7_VALUES);
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail PIB Table " + e.getMessage());}
            }
        	
        	if (UpgradeUtils.doesTableExist("grailpibreporting")) {
            	try{
    	        	UpgradeUtils.executeStatement(ADD_COL8);
    	        	UpgradeUtils.executeStatement(COPY_COL8_VALUES);
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail PIB Reporting Table " + e.getMessage());}
            }
        }
    }
}
