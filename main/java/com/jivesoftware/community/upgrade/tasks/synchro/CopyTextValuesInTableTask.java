package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class CopyTextValuesInTableTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(CopyTextValuesInTableTask.class);
	
	private static String COPY_COL_VALUES = "update grailproject set descriptiontext=description;";
	
	private static String COPY_COL1_VALUES = "update grailpib set bizquestiontext=bizquestion;";
	private static String COPY_COL2_VALUES = "update grailpib set researchobjectivetext=researchobjective;";
	private static String COPY_COL3_VALUES = "update grailpib set actionstandardtext=actionstandard;";
	private static String COPY_COL4_VALUES = "update grailpib set researchdesigntext=researchdesign;";
	private static String COPY_COL5_VALUES = "update grailpib set sampleprofiletext=sampleprofile;";
	private static String COPY_COL6_VALUES = "update grailpib set stimulusmaterialtext=stimulusmaterial;";
	private static String COPY_COL7_VALUES = "update grailpib set otherstext=others;";
	private static String COPY_COL8_VALUES = "update grailpibreporting set otherreportingrequirementstext=otherreportingrequirements;";
	
	
	private static String COPY2_COL1_VALUES = "update grailproposal set bizquestiontext=bizquestion;";
	private static String COPY2_COL2_VALUES = "update grailproposal set researchobjectivetext=researchobjective;";
	private static String COPY2_COL3_VALUES = "update grailproposal set actionstandardtext=actionstandard;";
	private static String COPY2_COL4_VALUES = "update grailproposal set researchdesigntext=researchdesign;";
	private static String COPY2_COL5_VALUES = "update grailproposal set sampleprofiletext=sampleprofile;";
	private static String COPY2_COL6_VALUES = "update grailproposal set stimulusmaterialtext=stimulusmaterial;";
	private static String COPY2_COL7_VALUES = "update grailproposal set otherstext=others;";
	private static String COPY2_COL8_VALUES = "update grailproposalreporting set otherreportingrequirementstext=otherreportingrequirements;";
	
    @Override
    public String getName() {
        return "Copy Text Values Table task for new Editors and compare feauture";
    }

    @Override
    public String getDescription() {
        return "This task Copy Text Values Table task for new Editors and compare feauture";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproject, grailpib, grailproposal, grailpibreporting, grailproposalreporting tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        	
    	if (UpgradeUtils.doesTableExist("grailproject")) {
        	try{
	        	UpgradeUtils.executeStatement(COPY_COL_VALUES);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	
    	if (UpgradeUtils.doesTableExist("grailpib")) {
            	try{
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
    	        	UpgradeUtils.executeStatement(COPY_COL8_VALUES);
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail PIB Reporting Table " + e.getMessage());}
            }
        

    	if (UpgradeUtils.doesTableExist("grailproposal")) {
        	try{
	        	UpgradeUtils.executeStatement(COPY2_COL1_VALUES);
	        	UpgradeUtils.executeStatement(COPY2_COL2_VALUES);
	        	UpgradeUtils.executeStatement(COPY2_COL3_VALUES);
	        	UpgradeUtils.executeStatement(COPY2_COL4_VALUES);
	        	UpgradeUtils.executeStatement(COPY2_COL5_VALUES);
	        	UpgradeUtils.executeStatement(COPY2_COL6_VALUES);
	        	UpgradeUtils.executeStatement(COPY2_COL7_VALUES);
	        	
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Proposal Table " + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailproposalreporting")) {
        	try{
	        	UpgradeUtils.executeStatement(COPY2_COL8_VALUES);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail PIB Reporting Table " + e.getMessage());}
        }
    }
}
