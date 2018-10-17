package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifyProposalTableTask1 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifyProposalTableTask1.class);
	
	//Update Proposal Table
	private static String ALTER_COL1_PIB = "ALTER TABLE grailproposal ALTER COLUMN bizquestion TYPE VARCHAR(3000);";
	private static String ALTER_COL2_PIB = "ALTER TABLE grailproposal ALTER COLUMN researchobjective TYPE VARCHAR(3000);";
	private static String ALTER_COL3_PIB = "ALTER TABLE grailproposal ALTER COLUMN actionstandard TYPE VARCHAR(3000);";
	private static String ALTER_COL4_PIB = "ALTER TABLE grailproposal ALTER COLUMN researchdesign TYPE VARCHAR(8000);";
	private static String ALTER_COL5_PIB = "ALTER TABLE grailproposal ALTER COLUMN sampleprofile TYPE VARCHAR(3000);";
	private static String ALTER_COL6_PIB = "ALTER TABLE grailproposal ALTER COLUMN stimulusmaterial TYPE VARCHAR(3000);";
	private static String ALTER_COL7_PIB = "ALTER TABLE grailproposal ALTER COLUMN others TYPE VARCHAR(3000);";
	
	//Update PIB Reporting Table
	private static String ALTER_COL1_PROPOSALREPORTING = "ALTER TABLE grailproposalreporting ALTER COLUMN otherreportingrequirements TYPE VARCHAR(3000);";
	
    @Override
    public String getName() {
        return "Grail Proposal & Reporting Modify Task for Textarea editors";
    }

    @Override
    public String getDescription() {
        return "This task modifies existing Proposal & Reporting Tables by altering the textarea columns for increasing their size approx. 2.5 folds";
    }

    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, modifying table...  \b grailproposal \b grailproposalreporting";
               
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }
    @Override
    public void doTask() throws Exception {
        
        if (UpgradeUtils.doesTableExist("grailproposal")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL2_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL3_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL4_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL5_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL6_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL7_PIB);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of Proposal table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailproposalreporting")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PROPOSALREPORTING);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailproposalreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
    }
}
