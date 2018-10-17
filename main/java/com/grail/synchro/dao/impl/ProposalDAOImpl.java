package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.FieldAttachmentBean;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalEndMarketDetails;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ProposalReporting;
import com.grail.synchro.dao.ProposalDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class ProposalDAOImpl extends JiveJdbcDaoSupport implements ProposalDAO {

    private static final Logger LOG = Logger.getLogger(ProposalDAOImpl.class.getName());
    private SynchroDAOUtil synchroDAOUtil;
   

    private static final String INSERT_PROPOSAL_SQL = "INSERT INTO grailproposal( " +
            " projectid, endmarketid, agencyid, bizquestion, " +
            " researchobjective, actionstandard, researchdesign, " +
            " sampleprofile, stimulusmaterial, stimulusmaterialshipped, others, proposalCostTemplate, stimulidate, " +
            " npiReferenceNo, " +
            " creationby, modificationby, creationdate, modificationdate, status," +
            " brand, projectOwner, spiContact, proposedMethodology, startDate, endDate, methodologyType, methodologyGroup, caprating, bizquestiontext, researchobjectivetext, actionstandardtext, researchdesigntext, sampleprofiletext, stimulusmaterialtext, otherstext, proposalsavedate) " +
            " VALUES (?, ?, ?, " +
            " ?, ?, ?, ?, " +
            " ?, ?, ?, ?, ?, ?, " +
            " ?, " + 
            " ?, ?, ?, ?, ?, " +
            " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
            

    private static final String LOAD_PROPOSAL = " SELECT projectid, endmarketid, agencyid, bizquestion, " +
            "       researchobjective, actionstandard, researchdesign, " +
            "       sampleprofile, " +
            "       stimulusmaterial, stimulusmaterialshipped, others, proposalCostTemplate, stimulidate,npireferenceno, issubmitted, isawarded, " +
            "       creationby, " +
            "       modificationby, creationdate, modificationdate,status, " +
            "       brand, projectOwner, spiContact, proposedMethodology, startDate, endDate, methodologyType, methodologyGroup, issendtoprojectowner,isreqclarimodification, caprating, " +
            "       bizquestiontext, researchobjectivetext, actionstandardtext, researchdesigntext, sampleprofiletext, stimulusmaterialtext, otherstext, reqclarificationreqclicked, proposalsavedate, proposalsubmitdate, reqclarificationreqdate, propsendtoownerdate, proposalawarddate "+
            "  FROM grailproposal ";

    private static final String GET_PROPOSAL_BY_PROJECT_ID_EM_ID_AG_ID = LOAD_PROPOSAL + " where projectid = ? and endmarketid=? and agencyid=? order by endmarketid ASC";
    private static final String GET_PROPOSAL_BY_PROJECT_ID = LOAD_PROPOSAL + " where projectid = ? order by endmarketid ASC";
    private static final String GET_PROPOSAL_BY_PROJECT_ID_AG_ID = LOAD_PROPOSAL + " where projectid = ? and agencyid=? order by endmarketid ASC";

    private static final String UPDATE_PROPOSAL_BY_PROJECT_ID = "UPDATE grailproposal " +
            "   SET  bizquestion=?, researchobjective=?, actionstandard=?, " +
            "   researchdesign=?, sampleprofile=?, stimulusmaterial=?, stimulusmaterialshipped=?, others=?, proposalCostTemplate=?, stimulidate=?, " +
            "   npireferenceno=?, " +
            "   modificationby=?, modificationdate=?, status=?," +
            "  brand=?, projectOwner=?, spiContact=?, proposedMethodology=?, startDate=?, endDate=?, methodologyGroup=?, methodologyType=?, caprating=?, " +
            " bizquestiontext=?, researchobjectivetext=?, actionstandardtext=?, researchdesigntext=?, sampleprofiletext=?, stimulusmaterialtext=?, otherstext=?, proposalsavedate=? "+
            "  WHERE projectid = ? and endmarketid=? and agencyid=? ";
  
    private static final String GET_PROPOSAL_REPORTING_BY_PROJECT_ID = "SELECT projectid, endmarketid,agencyid, " +
            "       toplinepresentation, presentation, fullreport, globalsummary, otherreportingrequirements, otherreportingrequirementstext " +
            "  FROM grailproposalreporting WHERE projectid = ? and endmarketid=? and agencyid=?";
    
    private static final String GET_PROPOSAL_EM_DETAILS_BY_PROJECT_ID = "SELECT projectid, endmarketid,agencyid, " +
            "       totalcost, totalcostcurrency, intmgmcost, intmgmtcostcurrency, localmanagementcost, localmanagementcostcurrency, " +
            " operationalHubCost, operationalHubCostType, fieldworkcost, fieldworkcostcurrency, proposedfwagencynames, fwstartestimated, fwcompleteestimated, totalnoofinterviews, " +
            " totalnoofvisits, averageinterviewduration, totalnoofgroups, interviewduration, noofresppergroup, geospreadnational,geospreadurban," +
            " cities, datacollection, othercost, othercostcurrency  " +
            "  FROM grailproposalendmarketdetails WHERE projectid = ? and endmarketid=? and agencyid=?"; 
    
    private static final String GET_PROPOSAL_EM_DETAILS_BY_PROJECT_ID_AGENCY_ID = "SELECT projectid, endmarketid,agencyid, " +
            "       totalcost, totalcostcurrency, intmgmcost, intmgmtcostcurrency, localmanagementcost, localmanagementcostcurrency, " +
            " operationalHubCost, operationalHubCostType, fieldworkcost, fieldworkcostcurrency, proposedfwagencynames, fwstartestimated, fwcompleteestimated, totalnoofinterviews, " +
            " totalnoofvisits, averageinterviewduration, totalnoofgroups, interviewduration, noofresppergroup, geospreadnational,geospreadurban," +
            " cities, datacollection, othercost, othercostcurrency  " +
            "  FROM grailproposalendmarketdetails WHERE projectid = ? and agencyid=?"; 
   

    private static final String INSERT_PROPOSAL_REPORTING = "INSERT INTO grailproposalreporting(" +
            " projectid, endmarketid,agencyid, " +
            " toplinepresentation, presentation, fullreport, globalsummary, otherReportingRequirements, otherReportingRequirementstext)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String INSERT_PROPOSAL_EM_DETAILS = "INSERT INTO grailproposalendmarketdetails(" +
            " projectid, endmarketid,agencyid, " +
            " totalcost, totalcostcurrency, intmgmcost, intmgmtcostcurrency, localmanagementcost, localmanagementcostcurrency, " +
            " operationalHubCost, operationalHubCostType, fieldworkcost, fieldworkcostcurrency, proposedfwagencynames, fwstartestimated, fwcompleteestimated, totalnoofinterviews," +
            " totalnoofvisits, averageinterviewduration, totalnoofgroups, interviewduration, noofresppergroup, geospreadnational,geospreadurban, cities, datacollection, othercost, othercostcurrency) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
   

    private static final String UPDATE_PROPOSAL_REPORTING_BY_PROJECT_ID = "UPDATE grailproposalreporting " +
            "   SET toplinepresentation=?, presentation=?, fullreport=?, globalsummary=?,  " +
            "   otherReportingRequirements=?, otherReportingRequirementstext=? " +
            " WHERE projectid = ? and endmarketid=? and agencyid=? ";
   
    private static final String UPDATE_PROPOSAL_EM_DETAILS_BY_PROJECT_ID = "UPDATE grailproposalendmarketdetails " +
            "   SET  totalcost=?, totalcostcurrency=?, intmgmcost=?, intmgmtcostcurrency=?, " +
            "   localmanagementcost=?, localmanagementcostcurrency=?, operationalHubCost=?, operationalHubCostType=?, fieldworkcost=?, fieldworkcostcurrency=?, " +
            "   proposedfwagencynames=?, fwstartestimated=?, fwcompleteestimated=?, totalnoofinterviews=?, " +
            "   totalnoofvisits=?, averageinterviewduration=?, totalnoofgroups=?, interviewduration=?," +
            "   noofresppergroup=?, geospreadnational=?, geospreadurban=?, cities=?, datacollection=?, othercost=?, othercostcurrency=?   " +
            "  WHERE projectid = ? and endmarketid=? and agencyid=? ";

    private static final String INSERT_ATTACHMENT = "INSERT INTO grailfieldattachment(projectid, endmarketid, " +
    		"	fieldcategoryid, attachmentid, userid)" +
            "  VALUES (?, ?, ?, ?, ?)";
    private static final String REMOVE_ATTACHMENT = "DELETE FROM grailfieldattachment where attachmentid=?";
    private static final String GET_ATTACHMENT = "SELECT projectid, endmarketid, fieldcategoryid, attachmentid, userid FROM grailfieldattachment where projectid=? and endmarketid=?";
   
    
    private static final String UPDATE_PIB_REPORTING_SINGLE_ENDMARKET_ID = "UPDATE grailpibreporting" +
            " SET endmarketid=? WHERE projectid = ?";

    private static final String UPDATE_PIB_STAKEHOLDER_SINGLE_ENDMARKET_ID = "UPDATE grailpibstakeholderlist" +
            " SET endmarketid=? WHERE projectid = ?";
    
    private static final String UPDATE_PIB_ATTACHMENT_SINGLE_ENDMARKET_ID = "UPDATE grailfieldattachment" +
            " SET endmarketid=? WHERE projectid = ?";
    
    private static final String SUBMIT_PROPOSAL = "UPDATE grailproposal" +
            " SET issubmitted=?, status=?, proposalsubmitdate=? WHERE projectid = ? and agencyid=? ";
    
    private static final String AWARD_AGENCY = "UPDATE grailproposal" +
            " SET isawarded=?, status=?, proposalawarddate=? WHERE projectid = ? and agencyid=? ";
    private static final String REJECT_AGENCY = "UPDATE grailproposal" +
            " SET issubmitted=?, isawarded=?, status=? WHERE projectid = ? and agencyid=? ";
    private static final String GET_ATTACHMENT_BY_OBJECT = "SELECT * from jiveattachment where objecttype=? AND objectid=?";
    private static final String UPDATE_PROPOSAL_STATUS = "UPDATE grailproposal SET status=? WHERE projectid = ?";
    private static final String UPDATE_PROPOSAL_END_MARKET_ID = "UPDATE grailproposal SET endmarketid=? WHERE projectid =? and agencyid=?";
    private static final String UPDATE_PROPOSAL_EMD_END_MARKET_ID = "UPDATE grailproposalendmarketdetails SET endmarketid=? WHERE projectid =? and agencyid=?";
    private static final String UPDATE_PROPOSAL_REPORTING_END_MARKET_ID = "UPDATE grailproposalreporting SET endmarketid=? WHERE projectid =? and agencyid=?";
    
    private static final String UPDATE_SEND_TO_PROJECT_OWNER = "UPDATE grailproposal" +
            " SET issendtoprojectowner=?, propsendtoownerdate=? WHERE projectid = ? and agencyid=? ";
    private static final String UPDATE_REQ_CLARI_MODIFICATION = "UPDATE grailproposal" +
            " SET isreqclarimodification=?, reqclarificationreqclicked=?, reqclarificationreqdate=? WHERE projectid = ? and agencyid=? ";
    
    private static final String REMOVE_PROPOSAL_AGENCY = "DELETE FROM grailproposal where projectid=? and endmarketid=? and agencyid=?";
    private static final String REMOVE_PROPOSAL_AGENCY_END_MARKET = "DELETE FROM grailproposalendmarketdetails where projectid=? and endmarketid=? and agencyid=?";
    private static final String REMOVE_PROPOSAL_AGENCY_REPORTING = "DELETE FROM grailproposalreporting where projectid=? and endmarketid=? and agencyid=?";
    private static final String UPDATE_PROPOSAL_ACTION_STANDARD = "UPDATE grailproposal SET actionstandard=?, actionstandardtext=? WHERE projectid = ? and endmarketid = ? and agencyid=?";
    private static final String UPDATE_PROPOSAL_RESEARCH_DESIGN = "UPDATE grailproposal SET researchdesign=?, researchdesigntext=? WHERE projectid = ? and endmarketid = ? and agencyid=?";
    private static final String UPDATE_PROPOSAL_SAMPLE_PROFILE = "UPDATE grailproposal SET sampleprofile=?, sampleprofiletext=? WHERE projectid = ? and endmarketid = ? and agencyid=?";
    private static final String UPDATE_PROPOSAL_STIMULUS_MATERIAL = "UPDATE grailproposal SET stimulusmaterial=?, stimulusmaterialtext=? WHERE projectid = ? and endmarketid = ? and agencyid=?";
    private static final String UPDATE_IS_SUBMITTED = "UPDATE grailproposal" +
            " SET issubmitted=?, status=? WHERE projectid = ? ";
    
    private static final String UPDATE_PROPOSAL_AGENCY_ID = "UPDATE grailproposal " +
            "   SET agencyid=? " +
            " WHERE projectid = ? and endmarketid=? and agencyid=? ";
    
    private static final String UPDATE_PROPOSAL_AGENCY_EM_ID = "UPDATE grailproposalendmarketdetails " +
            "   SET agencyid=? " +
            " WHERE projectid = ? and endmarketid=? and agencyid=? ";
    
    private static final String UPDATE_PROPOSAL_AGENCY__REPORTING_ID = "UPDATE grailproposalreporting " +
            "   SET agencyid=? " +
            " WHERE projectid = ? and endmarketid=? and agencyid=? ";
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProposalInitiation saveProposalReporting(final ProposalInitiation proposalInitiation) {
        try {
            getSimpleJdbcTemplate().update(INSERT_PROPOSAL_REPORTING,  proposalInitiation.getProjectID(),proposalInitiation.getEndMarketID(),
            		proposalInitiation.getAgencyID(),
                    BooleanUtils.toIntegerObject(proposalInitiation.getTopLinePresentation()),
                    BooleanUtils.toIntegerObject(proposalInitiation.getPresentation()),
                    BooleanUtils.toIntegerObject(proposalInitiation.getFullreport()),
                    BooleanUtils.toIntegerObject(proposalInitiation.getGlobalSummary()),
                  
                    proposalInitiation.getOtherReportingRequirements(),
                    proposalInitiation.getOtherReportingRequirementsText());
            return proposalInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Proposal Reporting Details for projectID" + proposalInitiation.getProjectID() +" and End Market Id -"+ proposalInitiation.getEndMarketID() + " and Agency ID"+ proposalInitiation.getAgencyID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails) {
        try {
            getSimpleJdbcTemplate().update(INSERT_PROPOSAL_EM_DETAILS,  proposalEMDetails.getProjectID(),proposalEMDetails.getEndMarketID(),
            		proposalEMDetails.getAgencyID(),proposalEMDetails.getTotalCost(),proposalEMDetails.getTotalCostType(),proposalEMDetails.getIntMgmtCost(),
            		proposalEMDetails.getIntMgmtCostType(), proposalEMDetails.getLocalMgmtCost(), proposalEMDetails.getLocalMgmtCostType(),
            		proposalEMDetails.getOperationalHubCost(), proposalEMDetails.getOperationalHubCostType(), 
            		proposalEMDetails.getFieldworkCost(), proposalEMDetails.getFieldworkCostType(), 
            		proposalEMDetails.getProposedFWAgencyNames(), 
            		proposalEMDetails.getFwStartDate()!=null?proposalEMDetails.getFwStartDate().getTime():0,
            		proposalEMDetails.getFwEndDate()!=null?proposalEMDetails.getFwEndDate().getTime():0,
            		proposalEMDetails.getTotalNoInterviews(),proposalEMDetails.getTotalNoOfVisits(),proposalEMDetails.getAvIntDuration(),proposalEMDetails.getTotalNoOfGroups(),
            		proposalEMDetails.getInterviewDuration(),proposalEMDetails.getNoOfRespPerGroup(),
            		BooleanUtils.toIntegerObject(proposalEMDetails.getGeoSpreadNational()),BooleanUtils.toIntegerObject(proposalEMDetails.getGeoSpreadUrban()),
            		proposalEMDetails.getCities(),   		
            		proposalEMDetails.getDataCollectionMethod()!=null?Joiner.on(",").join(proposalEMDetails.getDataCollectionMethod()):null,
            		proposalEMDetails.getOtherCost(),proposalEMDetails.getOtherCostType());
            
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Proposal EndMarket Details for projectID" + proposalEMDetails.getProjectID() +" and End Market Id -"+ proposalEMDetails.getEndMarketID() + " and Agency ID"+ proposalEMDetails.getAgencyID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProposalInitiation updateProposalReporting(final ProposalInitiation proposalInitiation) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_PROPOSAL_REPORTING_BY_PROJECT_ID,
            		BooleanUtils.toIntegerObject(proposalInitiation.getTopLinePresentation()),
            		BooleanUtils.toIntegerObject(proposalInitiation.getPresentation()),
                    BooleanUtils.toIntegerObject(proposalInitiation.getFullreport()),
                    BooleanUtils.toIntegerObject(proposalInitiation.getGlobalSummary()),
                    
                    proposalInitiation.getOtherReportingRequirements(),
                    proposalInitiation.getOtherReportingRequirementsText(),
                    proposalInitiation.getProjectID(), proposalInitiation.getEndMarketID(), proposalInitiation.getAgencyID());
            return proposalInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to Update Proposal Reporting Details for projectID" + proposalInitiation.getProjectID() +" and End Market Id -"+ proposalInitiation.getEndMarketID() + " and Agency ID"+ proposalInitiation.getAgencyID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails)
    {
    	try{
    		final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailproposalendmarketdetails " +
                    "WHERE projectid = ? and endmarketid=? and agencyid=? ", proposalEMDetails.getProjectID(),proposalEMDetails.getEndMarketID(),
                    proposalEMDetails.getAgencyID());
    		if(count>0)
    		{
	    		
	    		getSimpleJdbcTemplate().update( UPDATE_PROPOSAL_EM_DETAILS_BY_PROJECT_ID,
	            	proposalEMDetails.getTotalCost(),
	            	proposalEMDetails.getTotalCostType(),
	            	proposalEMDetails.getIntMgmtCost(),
	            	proposalEMDetails.getIntMgmtCostType(),
	            	proposalEMDetails.getLocalMgmtCost(),
	            	proposalEMDetails.getLocalMgmtCostType(),
	            	proposalEMDetails.getOperationalHubCost(),
	            	proposalEMDetails.getOperationalHubCostType(),
	            	proposalEMDetails.getFieldworkCost(),
	            	proposalEMDetails.getFieldworkCostType(),
	            	proposalEMDetails.getProposedFWAgencyNames(),
	            	proposalEMDetails.getFwStartDate()!=null?proposalEMDetails.getFwStartDate().getTime():0,
	    	        proposalEMDetails.getFwEndDate()!=null?proposalEMDetails.getFwEndDate().getTime():0,
	            	proposalEMDetails.getTotalNoInterviews(),
	            	proposalEMDetails.getTotalNoOfVisits(),
	            	proposalEMDetails.getAvIntDuration(),
	            	proposalEMDetails.getTotalNoOfGroups(),
	            	proposalEMDetails.getInterviewDuration(),
	            	proposalEMDetails.getNoOfRespPerGroup(),
	            	BooleanUtils.toIntegerObject(proposalEMDetails.getGeoSpreadNational()),
	            	BooleanUtils.toIntegerObject(proposalEMDetails.getGeoSpreadUrban()),// Geographical Spread
	            	proposalEMDetails.getCities(),
	            	proposalEMDetails.getDataCollectionMethod()!=null?Joiner.on(",").join(proposalEMDetails.getDataCollectionMethod()):null,
	            	proposalEMDetails.getOtherCost(),
	    	        proposalEMDetails.getOtherCostType(),
	            	proposalEMDetails.getProjectID(),
	            	proposalEMDetails.getEndMarketID(),
	            	proposalEMDetails.getAgencyID());
    		}
    		else
    		{
    			saveProposalEMDetails(proposalEMDetails);
    		}
           
        } catch (DataAccessException daEx) {
     	   final String message = "Failed to Update Proposal End Market Details for projectID" + proposalEMDetails.getProjectID() +" and End Market Id -"+ proposalEMDetails.getEndMarketID() + " and Agency ID"+ proposalEMDetails.getAgencyID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateAgency(final Long projectId,  final Long endMarketId, final Long updatedAgencyId, final Long sourceAgencyId){
    	 LOG.info("Inside Proposal DAO IMPL --- > "+ updatedAgencyId + "----"+ sourceAgencyId);
    	try {
            getSimpleJdbcTemplate().update(UPDATE_PROPOSAL_AGENCY_ID, updatedAgencyId, projectId, endMarketId,sourceAgencyId);
            getSimpleJdbcTemplate().update(UPDATE_PROPOSAL_AGENCY_EM_ID, updatedAgencyId, projectId, endMarketId,sourceAgencyId);
            getSimpleJdbcTemplate().update(UPDATE_PROPOSAL_AGENCY__REPORTING_ID, updatedAgencyId, projectId, endMarketId,sourceAgencyId);
            
        } catch (DataAccessException daEx) {
            final String message = "Failed to Update Agency Id for projectID" + projectId +" and End Market Id -"+ endMarketId + " and Agency ID"+ sourceAgencyId;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    public List<ProposalReporting> getProposalReporting(final Long projectID, final Long endMarketId, final Long agencyId) {
        return getSimpleJdbcTemplate().query(GET_PROPOSAL_REPORTING_BY_PROJECT_ID, proposalReportingRowMapper, projectID, endMarketId,agencyId);
    }
    @Override
    public List<ProposalEndMarketDetails> getProposalEMDetails(final Long projectID, final Long endMarketId, final Long agencyId)
    {
    	return getSimpleJdbcTemplate().query(GET_PROPOSAL_EM_DETAILS_BY_PROJECT_ID, proposalEMDetailsMapper, projectID, endMarketId,agencyId);
    }
    @Override
    public List<ProposalEndMarketDetails> getProposalEMDetails(final Long projectID, final Long agencyId)
    {
    	return getSimpleJdbcTemplate().query(GET_PROPOSAL_EM_DETAILS_BY_PROJECT_ID_AGENCY_ID, proposalEMDetailsMapper, projectID, agencyId);
    }
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProposalInitiation save(final ProposalInitiation proposalInitiation) {
        try {
            getSimpleJdbcTemplate().update(INSERT_PROPOSAL_SQL, proposalInitiation.getProjectID(), proposalInitiation.getEndMarketID(),
            		proposalInitiation.getAgencyID(), proposalInitiation.getBizQuestion(), 
            		proposalInitiation.getResearchObjective(), proposalInitiation.getActionStandard(),proposalInitiation.getResearchDesign(),
            		proposalInitiation.getSampleProfile(), proposalInitiation.getStimulusMaterial(),proposalInitiation.getStimulusMaterialShipped(),proposalInitiation.getOthers(),proposalInitiation.getProposalCostTemplate(),
            		proposalInitiation.getStimuliDate()!=null?proposalInitiation.getStimuliDate().getTime():0,
            		proposalInitiation.getNpiReferenceNo(),
            		proposalInitiation.getCreationBy(), proposalInitiation.getModifiedBy(), proposalInitiation.getCreationDate(), proposalInitiation.getModifiedDate(), proposalInitiation.getStatus(),
            		proposalInitiation.getBrand(), proposalInitiation.getProjectOwner(), proposalInitiation.getSpiContact(),
            		proposalInitiation.getProposedMethodology()!=null?Joiner.on(",").join(proposalInitiation.getProposedMethodology()):null,
            		proposalInitiation.getStartDate()!=null?proposalInitiation.getStartDate().getTime():0,proposalInitiation.getEndDate()!=null?proposalInitiation.getEndDate().getTime():0,
            		proposalInitiation.getMethodologyType(),proposalInitiation.getMethodologyGroup(),proposalInitiation.getCapRating(),
            		proposalInitiation.getBizQuestionText(),
            		proposalInitiation.getResearchObjectiveText(),
            		proposalInitiation.getActionStandardText(),
            		proposalInitiation.getResearchDesignText(),
            		proposalInitiation.getSampleProfileText(),
            		proposalInitiation.getStimulusMaterialText(),
            		proposalInitiation.getOthersText(),
            		proposalInitiation.getProposalSaveDate()!=null?proposalInitiation.getProposalSaveDate().getTime():0);
         
            return proposalInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Proposal Details for projectID" + proposalInitiation.getProjectID() +" and End Market Id -"+ proposalInitiation.getEndMarketID() + " and Agency ID"+ proposalInitiation.getAgencyID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    	
    }
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProposalInitiation update(final ProposalInitiation proposalInitiation) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_PROPOSAL_BY_PROJECT_ID,
                   //projectInitiation.getVersionNumber(),
        		  // proposalInitiation.getEndMarketID(),
        		   proposalInitiation.getBizQuestion(),
        		   proposalInitiation.getResearchObjective(),
        		   proposalInitiation.getActionStandard(),
        		   proposalInitiation.getResearchDesign(),
        		   proposalInitiation.getSampleProfile(),
        		   proposalInitiation.getStimulusMaterial(),
        		   proposalInitiation.getStimulusMaterialShipped(),
        		   proposalInitiation.getOthers(),
        		   proposalInitiation.getProposalCostTemplate(),
        		   proposalInitiation.getStimuliDate()!=null?proposalInitiation.getStimuliDate().getTime():0,
                   proposalInitiation.getNpiReferenceNo(),
                   proposalInitiation.getModifiedBy(),
                   proposalInitiation.getModifiedDate(),
                   proposalInitiation.getStatus(),
                   proposalInitiation.getBrand(),
                   proposalInitiation.getProjectOwner(),
                   proposalInitiation.getSpiContact(),
                   proposalInitiation.getProposedMethodology()!=null?Joiner.on(",").join(proposalInitiation.getProposedMethodology()):null,
                   proposalInitiation.getStartDate()!=null?proposalInitiation.getStartDate().getTime():0,
                   proposalInitiation.getEndDate()!=null?proposalInitiation.getEndDate().getTime():0,
                   proposalInitiation.getMethodologyGroup(),
                   proposalInitiation.getMethodologyType(),
                   proposalInitiation.getCapRating(),
                   
                   proposalInitiation.getBizQuestionText(),
	           		proposalInitiation.getResearchObjectiveText(),
	           		proposalInitiation.getActionStandardText(),
	           		proposalInitiation.getResearchDesignText(),
	           		proposalInitiation.getSampleProfileText(),
	           		proposalInitiation.getStimulusMaterialText(),
	           		proposalInitiation.getOthersText(),
	           		proposalInitiation.getProposalSaveDate()!=null?proposalInitiation.getProposalSaveDate().getTime():0,
                   //where clause
                   proposalInitiation.getProjectID(),
                   proposalInitiation.getEndMarketID(),
                   proposalInitiation.getAgencyID());
           return proposalInitiation;
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update Proposal Details for projectID" + proposalInitiation.getProjectID() +" and End Market Id -"+ proposalInitiation.getEndMarketID() + " and Agency ID"+ proposalInitiation.getAgencyID();
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalEndMarketId(final Long projectId, final Long agencyId, final Long endMarketId)
    {
    	getSimpleJdbcTemplate().update( UPDATE_PROPOSAL_END_MARKET_ID,endMarketId,projectId,agencyId);
    	getSimpleJdbcTemplate().update( UPDATE_PROPOSAL_EMD_END_MARKET_ID,endMarketId,projectId,agencyId);
    	getSimpleJdbcTemplate().update( UPDATE_PROPOSAL_REPORTING_END_MARKET_ID,endMarketId,projectId,agencyId);
    }
    @Override
    public List<ProposalInitiation> getProposalInitiation(final Long projectID, final Long endMarketId, final Long agencyId) {
        return getSimpleJdbcTemplate().query(GET_PROPOSAL_BY_PROJECT_ID_EM_ID_AG_ID, proposalInitiationRowMapper, projectID,endMarketId, agencyId);
    }
    @Override
    public List<ProposalInitiation> getProposalInitiation(final Long projectID, final Long agencyId) {
        return getSimpleJdbcTemplate().query(GET_PROPOSAL_BY_PROJECT_ID_AG_ID, proposalInitiationRowMapper, projectID, agencyId);
    }
    @Override
    public List<ProposalInitiation> getProposalInitiation(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_PROPOSAL_BY_PROJECT_ID, proposalInitiationRowMapper, projectID);
    }
    @Override
    public int addDocumentAttachment(final Long projectId, final Long endMarketId, final Long fieldCategoryId, 
			final Long attachmentId, final Long userId) {

		try {

			getSimpleJdbcTemplate().getJdbcOperations().update(
					INSERT_ATTACHMENT, projectId, endMarketId, fieldCategoryId,
					attachmentId, userId);
			return attachmentId.intValue();
		} catch (DataAccessException daEx) {
			final String message = "Failed to insert attachment details for project  - "
					+ projectId + " and attachmentId --" + attachmentId;
			LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
	}
    @Override
    public boolean removeDocumentAttachment(final Long attachmentId) {
        boolean success = false;
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_ATTACHMENT, attachmentId);
            success = true;
        } catch (DataAccessException daEx) {
            final String message = "Failed to remove document Attachment -- " + attachmentId;
            LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
            
        }
        return success;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void submitProposal(final Long projectID, final Long agencyId)
    {
    	try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					SUBMIT_PROPOSAL, 1,SynchroGlobal.StageStatus.PROPOSAL_SUBMITTED.ordinal(), System.currentTimeMillis(), projectID, agencyId);

		} catch (DataAccessException daEx) {
			final String message = "Failed to submit the Proposal for Project "
					+ projectID + " and AgencyId --"+ agencyId;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void awardAgency(final Long projectID, final Long agencyId)
    {
    	try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					AWARD_AGENCY, 1, SynchroGlobal.StageStatus.PROPOASL_AWARDED.ordinal(),System.currentTimeMillis(),projectID, agencyId);

		} catch (DataAccessException daEx) {
			final String message = "Failed to award the Agency for Project "
					+ projectID + " and AgencyId --"+ agencyId;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void rejectAgency(final Long projectID, final Long agencyId, final Integer status)
    {
    	try {
			/*getSimpleJdbcTemplate().getJdbcOperations().update(
					REJECT_AGENCY, null, null, SynchroGlobal.StageStatus.PROPOSAL_SAVED.ordinal(),projectID, agencyId);*/
    		getSimpleJdbcTemplate().getJdbcOperations().update(
					REJECT_AGENCY, null, null, status,projectID, agencyId);

		} catch (DataAccessException daEx) {
			final String message = "Failed to award the Agency for Project "
					+ projectID + " and AgencyId --"+ agencyId;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
	public void updatePIBReportingSingleEndMarketId(final Long projectID,
			final Long endMarketID) {
		try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_PIB_REPORTING_SINGLE_ENDMARKET_ID, endMarketID, projectID);

		} catch (DataAccessException daEx) {
			final String message = "Failed to update the Single endmarket Id for Project "
					+ projectID;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
	}
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
	public void updatePIBStakeholderListSingleEndMarketId(final Long projectID,
			final Long endMarketID) {
		try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_PIB_STAKEHOLDER_SINGLE_ENDMARKET_ID, endMarketID, projectID);

		} catch (DataAccessException daEx) {
			final String message = "Failed to update the Single endmarket Id for Project "
					+ projectID;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
	}
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
	public void updatePIBAttachmentSingleEndMarketId(final Long projectID,
			final Long endMarketID) {
		try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_PIB_ATTACHMENT_SINGLE_ENDMARKET_ID, endMarketID, projectID);

		} catch (DataAccessException daEx) {
			final String message = "Failed to update the Single endmarket Id for Project "
					+ projectID;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
	}
  
    @Override
    public List<AttachmentBean> getFieldAttachments(SynchroAttachment attachment) {
        List<AttachmentBean> bean = new ArrayList<AttachmentBean>();
        try{
            bean = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, attachment.getObjectType(), attachment.getID());
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return bean;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalStatus(final Long projectID,final List<ProposalInitiation> proposalInitiationList,final Integer status) {
        try {
        	final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(status) from grailproposal " +
                    "WHERE projectid = ? ", projectID);
        	if(count==0)
        	{
        		for(ProposalInitiation pi:proposalInitiationList)
        		{
        			save(pi);
        			saveProposalReporting(pi);
        		}
        	}
        	else
        	{
	        	getSimpleJdbcTemplate().getJdbcOperations().update(
	            		UPDATE_PROPOSAL_STATUS, status, projectID);
        	}

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the Proposal Status for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveAgency(final Long projectID,final List<ProposalInitiation> proposalInitiationList) {
        try {
        	for(ProposalInitiation pi:proposalInitiationList)
        		{
        			save(pi);
        			saveProposalReporting(pi);
        		}
        	
        
        } catch (DataAccessException daEx) {
            final String message = "Failed to update the Save Agency for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateSendToProjectOwner(final Long projectID, final Long agencyId, final Integer sendToProjectOwner)
    {
    	try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_SEND_TO_PROJECT_OWNER, sendToProjectOwner, System.currentTimeMillis(), projectID, agencyId);

		} catch (DataAccessException daEx) {
			final String message = "Failed to submit the Proposal for Project "
					+ projectID + " and AgencyId --"+ agencyId;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateRequestClarificationModification(final Long projectID, final Long agencyId, final Integer reqClarification)
    {
    	try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_REQ_CLARI_MODIFICATION, reqClarification,1,System.currentTimeMillis(), projectID, agencyId);

		} catch (DataAccessException daEx) {
			final String message = "Failed to update the Request Clarification the Proposal for Project "
					+ projectID + " and AgencyId --"+ agencyId;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void removeAgency(final Long projectId, final Long endMarketId, final Long agencyId) {
       
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_PROPOSAL_AGENCY, projectId,endMarketId,agencyId);
            getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_PROPOSAL_AGENCY_END_MARKET, projectId,endMarketId,agencyId);
            getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_PROPOSAL_AGENCY_REPORTING, projectId,endMarketId,agencyId);
           
        } catch (DataAccessException daEx) {
            final String message = "Failed to remove Agency for Project id --" + projectId +" and End Market Id --" + endMarketId;
            LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
            
        }
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalActionStandard(final List<ProposalInitiation> proposalInitiationList) {
        for(ProposalInitiation pi : proposalInitiationList)
        {
	    	try {
	               	getSimpleJdbcTemplate().getJdbcOperations().update(
	               			UPDATE_PROPOSAL_ACTION_STANDARD, pi.getActionStandard(),pi.getActionStandardText(), pi.getProjectID(), pi.getEndMarketID(), pi.getAgencyID());
	            } catch (DataAccessException daEx) {
	            final String message = "Failed to update the Proposal Action Standard for Project "
	                    + pi.getProjectID();
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
  
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalResearchDesign(final List<ProposalInitiation> proposalInitiationList) {
        for(ProposalInitiation pi : proposalInitiationList)
        {
	    	try {
	               	getSimpleJdbcTemplate().getJdbcOperations().update(
	               			UPDATE_PROPOSAL_RESEARCH_DESIGN, pi.getResearchDesign(), pi.getResearchDesignText(), pi.getProjectID(), pi.getEndMarketID(), pi.getAgencyID());
	            } catch (DataAccessException daEx) {
	            final String message = "Failed to update the Proposal Research Design for Project "
	                    + pi.getProjectID();
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalSampleProfile(final List<ProposalInitiation> proposalInitiationList) {
        for(ProposalInitiation pi : proposalInitiationList)
        {
	    	try {
	               	getSimpleJdbcTemplate().getJdbcOperations().update(
	               			UPDATE_PROPOSAL_SAMPLE_PROFILE, pi.getSampleProfile(), pi.getSampleProfileText(), pi.getProjectID(), pi.getEndMarketID(), pi.getAgencyID());
	            } catch (DataAccessException daEx) {
	            final String message = "Failed to update the Proposal Sample Profile for Project "
	                    + pi.getProjectID();
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalStimulusMaterial(final List<ProposalInitiation> proposalInitiationList) {
        for(ProposalInitiation pi : proposalInitiationList)
        {
	    	try {
	               	getSimpleJdbcTemplate().getJdbcOperations().update(
	               			UPDATE_PROPOSAL_STIMULUS_MATERIAL, pi.getStimulusMaterial(), pi.getStimulusMaterialText(), pi.getProjectID(), pi.getEndMarketID(), pi.getAgencyID());
	            } catch (DataAccessException daEx) {
	            final String message = "Failed to update the Proposal Stimulus Material for Project "
	                    + pi.getProjectID();
	            LOG.log(Level.SEVERE, message, daEx);
	            throw new DAOException(message, daEx);
	        }
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalSubimtted(final Long projectID, final Integer isSubmitted)
    {
    	try {
			/*getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_IS_SUBMITTED, isSubmitted, SynchroGlobal.StageStatus.PROPOSAL_SAVED.ordinal(),projectID);*/
    		getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_IS_SUBMITTED, isSubmitted, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),projectID);

		} catch (DataAccessException daEx) {
			final String message = "Failed to update is Submitted field for Proposal for Project "
					+ projectID;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
    }
    private final RowMapper<AttachmentBean> attachmentRowMapper =  new RowMapper<AttachmentBean>() {
        public AttachmentBean mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            AttachmentBean bean = new AttachmentBean();
            bean.setID(rs.getLong("attachmentid"));
            bean.setObjectID(rs.getLong("objectid"));
            bean.setObjectType(rs.getInt("objecttype"));
            bean.setName(rs.getString("filename"));
            bean.setSize(rs.getInt("filesize"));
            bean.setContentType(rs.getString("contenttype"));
            bean.setCreationDate(rs.getLong("creationdate"));
            bean.setModificationDate(rs.getLong("modificationdate"));
            return bean;
        }
    };
    /*public List<FieldAttachmentBean> getDocumentAttachment(final Long projectId, final Long endMakerketId) {
    	return getSimpleJdbcTemplate().query(GET_ATTACHMENT, fieldAttachmentRowMapper, projectId,endMakerketId);
       
    }*/
    
    private final RowMapper<ProposalReporting> proposalReportingRowMapper = new RowMapper<ProposalReporting>() {

        public ProposalReporting mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            ProposalReporting reporting = new ProposalReporting();
            reporting.setEndMarketID(rs.getLong("endMarketID"));
            reporting.setProjectID(rs.getLong("projectid"));
            reporting.setAgencyID(rs.getLong("agencyid"));
            reporting.setPresentation(rs.getBoolean("presentation"));
            reporting.setFullreport(rs.getBoolean("fullreport"));
            reporting.setGlobalSummary(rs.getBoolean("globalsummary"));
            reporting.setTopLinePresentation(rs.getBoolean("toplinepresentation"));
            
            reporting.setOtherReportingRequirements(rs.getString("otherreportingrequirements"));
            reporting.setOtherReportingRequirementsText(rs.getString("otherreportingrequirementstext"));
            return reporting;
        }
    };
    
    private final RowMapper<ProposalEndMarketDetails> proposalEMDetailsMapper = new RowMapper<ProposalEndMarketDetails>() {

        public ProposalEndMarketDetails mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	ProposalEndMarketDetails proposalEMDetails = new ProposalEndMarketDetails();
        	proposalEMDetails.setProjectID(rs.getLong("projectid"));
        	proposalEMDetails.setEndMarketID(rs.getLong("endmarketid"));
        	proposalEMDetails.setAgencyID(rs.getLong("agencyid"));
        	proposalEMDetails.setTotalCost(rs.getBigDecimal("totalcost"));
        	proposalEMDetails.setTotalCostType(rs.getInt("totalcostcurrency"));
        	proposalEMDetails.setIntMgmtCost(rs.getBigDecimal("intmgmcost"));
        	proposalEMDetails.setIntMgmtCostType(rs.getInt("intmgmtcostcurrency"));
        	proposalEMDetails.setLocalMgmtCost(rs.getBigDecimal("localmanagementcost"));
        	proposalEMDetails.setLocalMgmtCostType(rs.getInt("localmanagementcostcurrency"));
        	proposalEMDetails.setOperationalHubCost(rs.getBigDecimal("operationalHubCost"));
        	proposalEMDetails.setOperationalHubCostType(rs.getInt("operationalHubCostType"));
        	proposalEMDetails.setFieldworkCost(rs.getBigDecimal("fieldworkcost"));
        	proposalEMDetails.setFieldworkCostType(rs.getInt("fieldworkcostcurrency"));
        	proposalEMDetails.setProposedFWAgencyNames(rs.getString("proposedfwagencynames"));
        	if(rs.getLong("fwstartestimated") > 0)
            {
        		proposalEMDetails.setFwStartDate(new Date(rs.getLong("fwstartestimated")));
            }
        	if(rs.getLong("fwcompleteestimated") > 0)
            {
        		proposalEMDetails.setFwEndDate(new Date(rs.getLong("fwcompleteestimated")));
            }
        	
        	if (rs.getObject("totalnoofinterviews") != null && !rs.wasNull()) {
        		proposalEMDetails.setTotalNoInterviews(rs.getInt("totalnoofinterviews"));
        	}
        	else
        	{
        		proposalEMDetails.setTotalNoInterviews(null);
        	}
        	
        	if (rs.getObject("totalnoofvisits") != null && !rs.wasNull()) {
        		proposalEMDetails.setTotalNoOfVisits(rs.getInt("totalnoofvisits"));
        	}
        	else
        	{
        		proposalEMDetails.setTotalNoOfVisits(null);
        	}
        	
        	if (rs.getObject("averageinterviewduration") != null && !rs.wasNull()) {
        		proposalEMDetails.setAvIntDuration(rs.getInt("averageinterviewduration"));
        	}
        	else
        	{
        		proposalEMDetails.setAvIntDuration(null);
        	}
        	
        	if (rs.getObject("totalnoofgroups") != null && !rs.wasNull()) {
        		proposalEMDetails.setTotalNoOfGroups(rs.getInt("totalnoofgroups"));
        	}
        	else
        	{
        		proposalEMDetails.setTotalNoOfGroups(null);
        	}
        	
        	if (rs.getObject("interviewduration") != null && !rs.wasNull()) {
        		proposalEMDetails.setInterviewDuration(rs.getInt("interviewduration"));
        	}
        	else
        	{
        		proposalEMDetails.setInterviewDuration(null);
        	}
        	
        	if (rs.getObject("noofresppergroup") != null && !rs.wasNull()) {
        		proposalEMDetails.setNoOfRespPerGroup(rs.getInt("noofresppergroup"));
        	}
        	else
        	{
        		proposalEMDetails.setNoOfRespPerGroup(null);
        	}
        	
        	/*proposalEMDetails.setTotalNoInterviews(rs.getInt("totalnoofinterviews"));
        	proposalEMDetails.setTotalNoOfVisits(rs.getInt("totalnoofvisits"));
        	proposalEMDetails.setAvIntDuration(rs.getInt("averageinterviewduration"));
        	proposalEMDetails.setTotalNoOfGroups(rs.getInt("totalnoofgroups"));
        	proposalEMDetails.setInterviewDuration(rs.getInt("interviewduration"));
        	proposalEMDetails.setNoOfRespPerGroup(rs.getInt("noofresppergroup"));
        	*/
        	proposalEMDetails.setCities(rs.getString("cities"));
        	proposalEMDetails.setGeoSpreadNational(rs.getBoolean("geospreadnational"));
        	proposalEMDetails.setGeoSpreadUrban(rs.getBoolean("geospreadurban"));
        	proposalEMDetails.setDataCollectionMethod(synchroDAOUtil.getIDs(rs.getString("datacollection")));
        	proposalEMDetails.setOtherCost(rs.getBigDecimal("othercost"));
        	proposalEMDetails.setOtherCostType(rs.getInt("othercostcurrency"));
          
            return proposalEMDetails;
        }
    };
    
 
      /**
     * Reusable row mapper for mapping a result set to ProposalInitiation
     */
    private final RowMapper<ProposalInitiation> proposalInitiationRowMapper = new RowMapper<ProposalInitiation>() {
        public ProposalInitiation mapRow(ResultSet rs, int row) throws SQLException {
        	ProposalInitiation initiationBean = new ProposalInitiation();
            initiationBean.setProjectID(rs.getLong("projectid"));
            initiationBean.setEndMarketID(rs.getLong("endmarketid"));
            initiationBean.setAgencyID(rs.getLong("agencyid"));
            initiationBean.setBizQuestion(rs.getString("bizquestion"));
            initiationBean.setResearchObjective(rs.getString("researchobjective"));
            initiationBean.setActionStandard(rs.getString("actionstandard"));
            initiationBean.setResearchDesign(rs.getString("researchdesign"));
            initiationBean.setStimulusMaterial(rs.getString("stimulusmaterial"));
            initiationBean.setStimulusMaterialShipped(rs.getString("stimulusmaterialshipped"));
            initiationBean.setOthers(rs.getString("others"));
            initiationBean.setProposalCostTemplate(rs.getString("proposalCostTemplate"));
          
            if(rs.getLong("stimuliDate") > 0)
            {
            	initiationBean.setStimuliDate(new Date(rs.getLong("stimuliDate")));
            }
            initiationBean.setSampleProfile(rs.getString("sampleprofile"));
            initiationBean.setNpiReferenceNo(rs.getString("npireferenceno"));
            initiationBean.setIsPropSubmitted(rs.getBoolean("issubmitted"));
            initiationBean.setIsAwarded(rs.getBoolean("isawarded"));
            initiationBean.setCreationBy(rs.getLong("creationby"));
            initiationBean.setCreationDate(rs.getLong("creationdate"));
            initiationBean.setModifiedBy(rs.getLong("modificationby"));
            initiationBean.setModifiedDate(rs.getLong("modificationdate"));
            initiationBean.setStatus(rs.getInt("status"));
            initiationBean.setBrand(rs.getLong("brand"));
            initiationBean.setProjectOwner(rs.getLong("projectOwner"));
            initiationBean.setSpiContact(rs.getLong("spiContact"));
            initiationBean.setStartDate(new Date(rs.getLong("startDate")));
            initiationBean.setEndDate(new Date(rs.getLong("endDate")));
            initiationBean.setMethodologyGroup(rs.getLong("methodologyGroup"));
            initiationBean.setMethodologyType(rs.getLong("methodologyType"));
            initiationBean.setProposedMethodology(synchroDAOUtil.getIDs(rs.getString("proposedMethodology")));
            initiationBean.setIsSendToProjectOwner(rs.getBoolean("issendtoprojectowner"));
            initiationBean.setIsReqClariModification(rs.getBoolean("isreqclarimodification"));
            initiationBean.setCapRating(rs.getLong("caprating"));
            
            initiationBean.setBizQuestionText(rs.getString("bizquestiontext"));
            initiationBean.setResearchObjectiveText(rs.getString("researchobjectivetext"));
            initiationBean.setActionStandardText(rs.getString("actionstandardtext"));
            initiationBean.setResearchDesignText(rs.getString("researchdesigntext"));
            initiationBean.setSampleProfileText(rs.getString("sampleprofiletext"));
            initiationBean.setStimulusMaterialText(rs.getString("stimulusmaterialtext"));
            initiationBean.setOthersText(rs.getString("otherstext"));
            initiationBean.setReqClarificationReqClicked(rs.getBoolean("reqclarificationreqclicked"));
            
            if(rs.getLong("proposalsavedate") > 0) {
            	initiationBean.setProposalSaveDate(new Date(rs.getLong("proposalsavedate")));
            }
            
            if(rs.getLong("proposalsubmitdate") > 0) {
            	initiationBean.setProposalSubmitDate(new Date(rs.getLong("proposalsubmitdate")));
            }
            
            if(rs.getLong("reqclarificationreqdate") > 0) {
            	initiationBean.setReqClarificationReqDate(new Date(rs.getLong("reqclarificationreqdate")));
            }
            
            if(rs.getLong("propsendtoownerdate") > 0) {
            	initiationBean.setPropSendToOwnerDate(new Date(rs.getLong("propsendtoownerdate")));
            }
            
            if(rs.getLong("proposalawarddate") > 0) {
            	initiationBean.setProposalAwardDate(new Date(rs.getLong("proposalawarddate")));
            }
            
            return initiationBean;
        }
    };
    private final RowMapper<FieldAttachmentBean> fieldAttachmentRowMapper = new RowMapper<FieldAttachmentBean>() {

        public FieldAttachmentBean mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	FieldAttachmentBean fieldAttachment = new FieldAttachmentBean();
        	fieldAttachment.setProjectId(rs.getLong("projectid"));
        	fieldAttachment.setEndMarketId(rs.getLong("endmarketid"));
        	fieldAttachment.setFieldCategoryId(rs.getLong("fieldcategoryid"));
        	fieldAttachment.setAttachmentId(rs.getLong("attachmentid"));
        	fieldAttachment.setUserId(rs.getLong("userid"));
            
            return fieldAttachment;
        }
    };

	public SynchroDAOUtil getSynchroDAOUtil() {
		return synchroDAOUtil;
	}

	public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
		this.synchroDAOUtil = synchroDAOUtil;
	}
   
}
