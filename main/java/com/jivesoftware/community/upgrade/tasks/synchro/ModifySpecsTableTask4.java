package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class ModifySpecsTableTask4 implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(ModifySpecsTableTask4.class);
	
	//Update Project Specs Table
	private static String ALTER_COL1_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN projectdesc TYPE VARCHAR(6000);";
	private static String ALTER_COL2_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN bizquestion TYPE VARCHAR(3000);";
	private static String ALTER_COL3_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN researchobjective TYPE VARCHAR(3000);";
	private static String ALTER_COL4_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN actionstandard TYPE VARCHAR(3000);";
	private static String ALTER_COL5_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN researchdesign TYPE VARCHAR(8000);";
	private static String ALTER_COL6_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN sampleprofile TYPE VARCHAR(3000);";
	private static String ALTER_COL7_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN stimulusmaterial TYPE VARCHAR(3000);";
	private static String ALTER_COL8_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN stimulusmaterialshipped TYPE VARCHAR(3000);";
	private static String ALTER_COL9_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN screener TYPE VARCHAR(2000);";
	private static String ALTER_COL10_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN consumerccagreement TYPE VARCHAR(2000);";
	private static String ALTER_COL11_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN questionnaire TYPE VARCHAR(2000);";
	private static String ALTER_COL12_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN discussionguide TYPE VARCHAR(2000);";
	private static String ALTER_COL13_PIB = "ALTER TABLE grailprojectspecs ALTER COLUMN others TYPE VARCHAR(2000);";
	
	//Update Specs Reporting Table
	private static String ALTER_COL1_SPECSREPORTING = "ALTER TABLE grailprojectspecsreporting ALTER COLUMN otherreportingrequirements TYPE VARCHAR(3000);";
	
	//Update Specs Reporting Table
	private static String ALTER_COL1_PROPOSALEM = "ALTER TABLE grailproposalendmarketdetails ALTER COLUMN latestfwcomments TYPE VARCHAR(3000);";
	private static String ALTER_COL2_PROPOSALEM = "ALTER TABLE grailproposalendmarketdetails ALTER COLUMN finalcostcomments TYPE VARCHAR(3000);";
		
		
	//Update Specs Reporting Table
	private static String ALTER_COL1_SPECSEM = "ALTER TABLE grailprojectspecsemdetails ALTER COLUMN latestfwcomments TYPE VARCHAR(3000);";
	private static String ALTER_COL2_SPECSEM = "ALTER TABLE grailprojectspecsemdetails ALTER COLUMN finalcostcomments TYPE VARCHAR(3000);";
			
	//Update Methodology Approver Table
	private static String ALTER_COL1_PIBMETH = "ALTER TABLE grailpibmethodologywaiver ALTER COLUMN methodologydeviationrationale TYPE VARCHAR(10000);";
	private static String ALTER_COL2_PIBMETH = "ALTER TABLE grailpibmethodologywaiver ALTER COLUMN methodologyapprovercomment TYPE VARCHAR(10000);";
	
	//Update Methodology Approver Table
	private static String ALTER_COL1_PROPOSALMETH = "ALTER TABLE grailpsmethodologywaiver ALTER COLUMN methodologydeviationrationale TYPE VARCHAR(10000);";
	private static String ALTER_COL2_PROPOSALMETH = "ALTER TABLE grailpsmethodologywaiver ALTER COLUMN methodologyapprovercomment TYPE VARCHAR(10000);";
	
	//Update Report Summary Table
	private static String ALTER_COL1_REPSUMMARY = "ALTER TABLE grailprojectrepsummary ALTER COLUMN comments TYPE VARCHAR(5000);";
	
	//Update Waiver Tables
		private static String ALTER_COL1_WAIVER = "ALTER TABLE grailwaiver ALTER COLUMN summary TYPE VARCHAR(8000);";
		private static String ALTER_COL2_WAIVER = "ALTER TABLE grailwaiverapprovers ALTER approvercomments summary TYPE VARCHAR(3000);";
	
	
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
        if (UpgradeUtils.doesTableExist("grailprojectspecs")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL2_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL3_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL4_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL5_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL6_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL7_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL8_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL9_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL10_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL11_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL12_PIB);
	        	UpgradeUtils.executeStatement(ALTER_COL13_PIB);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of Project table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailprojectspecsreporting")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_SPECSREPORTING);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of PIB table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailproposalendmarketdetails")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PROPOSALEM);
	        	UpgradeUtils.executeStatement(ALTER_COL2_PROPOSALEM);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailprojectspecsemdetails")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_SPECSEM);
	        	UpgradeUtils.executeStatement(ALTER_COL2_SPECSEM);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailpibmethodologywaiver")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PIBMETH);
	        	UpgradeUtils.executeStatement(ALTER_COL2_PIBMETH);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailpsmethodologywaiver")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_PROPOSALMETH);
	        	UpgradeUtils.executeStatement(ALTER_COL2_PROPOSALMETH);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailprojectrepsummary")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_REPSUMMARY);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailwaiver")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL1_WAIVER);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
        if (UpgradeUtils.doesTableExist("grailwaiverapprovers")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL2_WAIVER);
        	}catch(Exception e){LOGGER.error("Error while altering the columns of grailpibreporting table for textarea columns for increasing their size approx. 2.5 folds " + e.getMessage());}
        }
        
    }
}

