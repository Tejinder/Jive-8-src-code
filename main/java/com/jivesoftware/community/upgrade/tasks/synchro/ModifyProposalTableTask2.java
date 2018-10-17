package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.grail.synchro.action.ProjectCreateAction;
import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyProposalTableTask2 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProposalTableTask2.class);
	
	private static String ADD_COL1 = "ALTER TABLE grailproposal ADD bizquestiontext VARCHAR(3000)";
	private static String ADD_COL2 = "ALTER TABLE grailproposal ADD researchobjectivetext VARCHAR(3000)";
	private static String ADD_COL3 = "ALTER TABLE grailproposal ADD actionstandardtext VARCHAR(3000)";
	private static String ADD_COL4 = "ALTER TABLE grailproposal ADD researchdesigntext VARCHAR(8000)";
	private static String ADD_COL5 = "ALTER TABLE grailproposal ADD sampleprofiletext VARCHAR(3000)";
	private static String ADD_COL6 = "ALTER TABLE grailproposal ADD stimulusmaterialtext VARCHAR(3000)";//Change to 3000 from 1000
	private static String ADD_COL7 = "ALTER TABLE grailproposal ADD otherstext VARCHAR(3000)";
	private static String ADD_COL8 = "ALTER TABLE grailproposalreporting ADD otherreportingrequirementstext VARCHAR(3000)";
	
	private static String COPY_COL1_VALUES = "update grailproposal set bizquestiontext=bizquestion;";
	private static String COPY_COL2_VALUES = "update grailproposal set researchobjectivetext=researchobjective;";
	private static String COPY_COL3_VALUES = "update grailproposal set actionstandardtext=actionstandard;";
	private static String COPY_COL4_VALUES = "update grailproposal set researchdesigntext=researchdesign;";
	private static String COPY_COL5_VALUES = "update grailproposal set sampleprofiletext=sampleprofile;";
	private static String COPY_COL6_VALUES = "update grailproposal set stimulusmaterialtext=stimulusmaterial;";
	private static String COPY_COL7_VALUES = "update grailproposal set otherstext=others;";
	private static String COPY_COL8_VALUES = "update grailproposalreporting set otherreportingrequirementstext=otherreportingrequirements;";
	
	
    @Override
    public String getName() {
        return "Modify Proposal and Proposal Reporting Table task";
    }

    @Override
    public String getDescription() {
        return "This task modifies exisiting Grail Proposal and Proposal Reporting Table";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the grailproposal n grailproposalreporting tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        	
        	if (UpgradeUtils.doesTableExist("grailproposal")) {
            	try{
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
    	        	
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Proposal Table " + e.getMessage());}
            }
        	
        	if (UpgradeUtils.doesTableExist("grailproposalreporting")) {

        		try{
    	        	UpgradeUtils.executeStatement(ADD_COL8);
    	        	UpgradeUtils.executeStatement(COPY_COL8_VALUES);
            	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Proposal Reporting Table " + e.getMessage());}
        		
            }
    }
}
