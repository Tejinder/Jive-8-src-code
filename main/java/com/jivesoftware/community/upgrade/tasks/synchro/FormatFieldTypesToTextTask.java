package com.jivesoftware.community.upgrade.tasks.synchro;

import org.apache.log4j.Logger;

import com.jivesoftware.community.upgrade.UpgradeTask;
import com.jivesoftware.community.upgrade.UpgradeUtils;

/**
 * @author: kanwar
 * @since: 1.0
 */
public class FormatFieldTypesToTextTask implements UpgradeTask {
	
	private static final Logger LOGGER = Logger.getLogger(FormatFieldTypesToTextTask.class);
	
	private static String ALTER_COL_PROJECT1 = "ALTER TABLE grailproject ALTER COLUMN description TYPE TEXT;";	
	private static String ALTER_COL_PROJECT2 = "ALTER TABLE grailproject ALTER COLUMN descriptiontext TYPE TEXT;";
	
	private static String ALTER_COL_PIB1 = "ALTER TABLE grailpib ALTER COLUMN bizquestion TYPE TEXT;";
	private static String ALTER_COL_PIB2 = "ALTER TABLE grailpib ALTER COLUMN researchobjective TYPE TEXT;";
	private static String ALTER_COL_PIB3 = "ALTER TABLE grailpib ALTER COLUMN actionstandard TYPE TEXT;";
	private static String ALTER_COL_PIB4 = "ALTER TABLE grailpib ALTER COLUMN researchdesign TYPE TEXT;";
	private static String ALTER_COL_PIB5 = "ALTER TABLE grailpib ALTER COLUMN sampleprofile TYPE TEXT;";
	private static String ALTER_COL_PIB6 = "ALTER TABLE grailpib ALTER COLUMN stimulusmaterial TYPE TEXT;";
	private static String ALTER_COL_PIB7 = "ALTER TABLE grailpib ALTER COLUMN others TYPE TEXT;";
	private static String ALTER_COL_PIB8 = "ALTER TABLE grailpib ALTER COLUMN bizquestiontext TYPE TEXT;";
	private static String ALTER_COL_PIB9 = "ALTER TABLE grailpib ALTER COLUMN researchobjectivetext TYPE TEXT;";
	private static String ALTER_COL_PIB10 = "ALTER TABLE grailpib ALTER COLUMN actionstandardtext TYPE TEXT;";
	private static String ALTER_COL_PIB11 = "ALTER TABLE grailpib ALTER COLUMN researchdesigntext TYPE TEXT;";
	private static String ALTER_COL_PIB12 = "ALTER TABLE grailpib ALTER COLUMN sampleprofiletext TYPE TEXT;";
	private static String ALTER_COL_PIB13 = "ALTER TABLE grailpib ALTER COLUMN stimulusmaterialtext TYPE TEXT;";
	private static String ALTER_COL_PIB14 = "ALTER TABLE grailpib ALTER COLUMN otherstext TYPE TEXT;";
	
	private static String ALTER_COL_PIB_MW1 = "ALTER TABLE grailpibmethodologywaiver ALTER COLUMN methodologydeviationrationale TYPE TEXT;";
	private static String ALTER_COL_PIB_MW2 = "ALTER TABLE grailpibmethodologywaiver ALTER COLUMN methodologyapprovercomment TYPE TEXT;";
	
	private static String ALTER_COL_PIB_REP1 = "ALTER TABLE grailpibreporting ALTER COLUMN otherreportingrequirements TYPE TEXT;";
	private static String ALTER_COL_PIB_REP2 = "ALTER TABLE grailpibreporting ALTER COLUMN otherreportingrequirementstext TYPE TEXT;";
	
	
	private static String ALTER_COL_PROPOSAL1 = "ALTER TABLE grailproposal ALTER COLUMN bizquestion TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL2 = "ALTER TABLE grailproposal ALTER COLUMN researchobjective TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL3 = "ALTER TABLE grailproposal ALTER COLUMN actionstandard TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL4 = "ALTER TABLE grailproposal ALTER COLUMN researchdesign TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL5 = "ALTER TABLE grailproposal ALTER COLUMN sampleprofile TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL6 = "ALTER TABLE grailproposal ALTER COLUMN stimulusmaterial TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL7 = "ALTER TABLE grailproposal ALTER COLUMN stimulusmaterialshipped TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL8 = "ALTER TABLE grailproposal ALTER COLUMN others TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL9 = "ALTER TABLE grailproposal ALTER COLUMN proposalcosttemplate TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL10 = "ALTER TABLE grailproposal ALTER COLUMN bizquestiontext TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL11 = "ALTER TABLE grailproposal ALTER COLUMN researchobjectivetext TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL12 = "ALTER TABLE grailproposal ALTER COLUMN actionstandardtext TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL13 = "ALTER TABLE grailproposal ALTER COLUMN researchdesigntext TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL14 = "ALTER TABLE grailproposal ALTER COLUMN sampleprofiletext TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL15 = "ALTER TABLE grailproposal ALTER COLUMN stimulusmaterialtext TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL16 = "ALTER TABLE grailproposal ALTER COLUMN otherstext TYPE TEXT;";
	
	private static String ALTER_COL_PROPOSAL_REP1 = "ALTER TABLE grailproposalreporting ALTER COLUMN otherreportingrequirements TYPE TEXT;";
	private static String ALTER_COL_PROPOSAL_REP2 = "ALTER TABLE grailproposalreporting ALTER COLUMN otherreportingrequirementstext TYPE TEXT;";
	
	private static String ALTER_COL_SPECS1 = "ALTER TABLE grailprojectspecs ALTER COLUMN bizquestion TYPE TEXT;";
	private static String ALTER_COL_SPECS2 = "ALTER TABLE grailprojectspecs ALTER COLUMN researchobjective TYPE TEXT;";
	private static String ALTER_COL_SPECS3 = "ALTER TABLE grailprojectspecs ALTER COLUMN actionstandard TYPE TEXT;";
	private static String ALTER_COL_SPECS4 = "ALTER TABLE grailprojectspecs ALTER COLUMN researchdesign TYPE TEXT;";
	private static String ALTER_COL_SPECS5 = "ALTER TABLE grailprojectspecs ALTER COLUMN sampleprofile TYPE TEXT;";
	private static String ALTER_COL_SPECS6 = "ALTER TABLE grailprojectspecs ALTER COLUMN stimulusmaterial TYPE TEXT;";
	private static String ALTER_COL_SPECS7 = "ALTER TABLE grailprojectspecs ALTER COLUMN stimulusmaterialshipped TYPE TEXT;";
	private static String ALTER_COL_SPECS8 = "ALTER TABLE grailprojectspecs ALTER COLUMN screener TYPE TEXT;";
	private static String ALTER_COL_SPECS9 = "ALTER TABLE grailprojectspecs ALTER COLUMN consumerccagreement TYPE TEXT;";
	private static String ALTER_COL_SPECS10 = "ALTER TABLE grailprojectspecs ALTER COLUMN questionnaire TYPE TEXT;";
	private static String ALTER_COL_SPECS11 = "ALTER TABLE grailprojectspecs ALTER COLUMN discussionguide TYPE TEXT;";
	private static String ALTER_COL_SPECS12 = "ALTER TABLE grailprojectspecs ALTER COLUMN others TYPE TEXT;";
	private static String ALTER_COL_SPECS13 = "ALTER TABLE grailprojectspecs ALTER COLUMN projectdesc TYPE TEXT;";
	private static String ALTER_COL_SPECS14 = "ALTER TABLE grailprojectspecs ALTER COLUMN proposalcosttemplate TYPE TEXT;";
	private static String ALTER_COL_SPECS15 = "ALTER TABLE grailprojectspecs ALTER COLUMN bizquestiontext TYPE TEXT;";
	private static String ALTER_COL_SPECS16 = "ALTER TABLE grailprojectspecs ALTER COLUMN researchobjectivetext TYPE TEXT;";
	private static String ALTER_COL_SPECS17 = "ALTER TABLE grailprojectspecs ALTER COLUMN actionstandardtext TYPE TEXT;";
	private static String ALTER_COL_SPECS18 = "ALTER TABLE grailprojectspecs ALTER COLUMN researchdesigntext TYPE TEXT;";
	private static String ALTER_COL_SPECS19 = "ALTER TABLE grailprojectspecs ALTER COLUMN sampleprofiletext TYPE TEXT;";
	private static String ALTER_COL_SPECS20 = "ALTER TABLE grailprojectspecs ALTER COLUMN stimulusmaterialtext TYPE TEXT;";
	private static String ALTER_COL_SPECS21 = "ALTER TABLE grailprojectspecs ALTER COLUMN otherstext TYPE TEXT;";
	
	private static String ALTER_COL_SPECS_EM1 = "ALTER TABLE grailprojectspecsemdetails ALTER COLUMN latestfwcomments TYPE TEXT;";
	private static String ALTER_COL_SPECS_EM2 = "ALTER TABLE grailprojectspecsemdetails ALTER COLUMN finalcostcomments TYPE TEXT;";
	
	private static String ALTER_COL_SPECS_REP1 = "ALTER TABLE grailprojectspecsreporting ALTER COLUMN otherreportingrequirements TYPE TEXT;";
	
	private static String ALTER_COL_SPECS_MW1 = "ALTER TABLE grailpsmethodologywaiver ALTER COLUMN methodologydeviationrationale TYPE TEXT;";
	private static String ALTER_COL_SPECS_MW2 = "ALTER TABLE grailpsmethodologywaiver ALTER COLUMN methodologyapprovercomment TYPE TEXT;";

	private static String ALTER_COL_EVAL1 = "ALTER TABLE grailprojecteval ALTER COLUMN batcommentsim TYPE TEXT;";
	private static String ALTER_COL_EVAL2 = "ALTER TABLE grailprojecteval ALTER COLUMN agencycommentsim TYPE TEXT;";
	private static String ALTER_COL_EVAL3 = "ALTER TABLE grailprojecteval ALTER COLUMN batcommentslm TYPE TEXT;";
	private static String ALTER_COL_EVAL4 = "ALTER TABLE grailprojecteval ALTER COLUMN agencycommentslm TYPE TEXT;";
	private static String ALTER_COL_EVAL5 = "ALTER TABLE grailprojecteval ALTER COLUMN batcommentsfa TYPE TEXT;";
	private static String ALTER_COL_EVAL6 = "ALTER TABLE grailprojecteval ALTER COLUMN agencycommentsfa TYPE TEXT;";
	
	private static String ALTER_COL_REPSUMMARY = "ALTER TABLE grailprojectrepsummary ALTER COLUMN comments TYPE TEXT;";
	
	private static String ALTER_COL_WAIVER1 = "ALTER TABLE grailwaiver ALTER COLUMN summary TYPE TEXT;";
	private static String ALTER_COL_WAIVER2 = "ALTER TABLE grailwaiver ALTER COLUMN preapprovalcomment TYPE TEXT;";
	
	private static String ALTER_COL_WAIVER_APPROVER1 = "ALTER TABLE grailwaiverapprovers ALTER COLUMN approvercomments TYPE TEXT;";
	
    @Override
    public String getName() {
        return "Alter table";
    }

    @Override
    public String getDescription() {
        return "alter table for varchar to text types";
    }

    @Override
    public String getEstimatedRunTime() {
        return LESS_TEN_SECONDS;
    }

    @Override
    public String getInstructions() {
        return "To upgrade your installation, updating the: \ngrailpib, \ngrailpibmethodologywaiver, \ngrailpibreporting, \ngrailproposal, \ngrailproposalreporting, \ngrailprojectspecs, \ngrailprojectspecsemdetails, \ngrailprojectspecsreporting, \ngrailpsmethodologywaiver, \ngrailprojecteval, \ngrailprojectrepsummary, \ngrailwaiver, \ngrailwaiverapprovers tables...\n";
    }

    @Override
    public boolean isBackgroundTask() {
        return false;
    }

    @Override
    public void doTask() throws Exception {
        	
    	if (UpgradeUtils.doesTableExist("grailproject")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_PROJECT1);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROJECT2);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailpib")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB1);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB2);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB3);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB4);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB5);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB6);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB7);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB8);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB9);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB10);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB11);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB12);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB13);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB14);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailpibmethodologywaiver")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB_MW1);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB_MW2);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailpibreporting")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB_REP1);
	        	UpgradeUtils.executeStatement(ALTER_COL_PIB_REP2);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailproposal")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL1);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL2);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL3);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL4);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL5);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL6);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL7);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL8);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL9);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL10);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL11);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL12);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL13);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL14);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL15);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL16);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailproposalreporting")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL_REP1);
	        	UpgradeUtils.executeStatement(ALTER_COL_PROPOSAL_REP2);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailprojectspecs")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS1);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS2);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS3);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS4);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS5);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS6);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS7);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS8);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS9);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS10);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS11);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS12);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS13);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS14);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS15);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS16);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS17);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS18);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS19);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS20);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS21);
	        	
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailprojectspecsemdetails")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS_EM1);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS_EM2);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailprojectspecsreporting")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS_REP1);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailpsmethodologywaiver")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS_MW1);
	        	UpgradeUtils.executeStatement(ALTER_COL_SPECS_MW2);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailprojecteval")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_EVAL1);
	        	UpgradeUtils.executeStatement(ALTER_COL_EVAL2);
	        	UpgradeUtils.executeStatement(ALTER_COL_EVAL3);
	        	UpgradeUtils.executeStatement(ALTER_COL_EVAL4);
	        	UpgradeUtils.executeStatement(ALTER_COL_EVAL5);
	        	UpgradeUtils.executeStatement(ALTER_COL_EVAL6);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailprojectrepsummary")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_REPSUMMARY);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    
    	if (UpgradeUtils.doesTableExist("grailwaiver")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_WAIVER1);
	        	UpgradeUtils.executeStatement(ALTER_COL_WAIVER2);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    	
    	if (UpgradeUtils.doesTableExist("grailwaiverapprovers")) {
        	try{
	        	UpgradeUtils.executeStatement(ALTER_COL_WAIVER_APPROVER1);
        	}catch(Exception e){LOGGER.error("Error while modifies exisiting Grail Project Table" + e.getMessage());}
        }
    
    
    }
}
