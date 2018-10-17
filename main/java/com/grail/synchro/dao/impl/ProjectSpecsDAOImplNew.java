package com.grail.synchro.dao.impl;

import java.math.BigDecimal;
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
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.FieldAttachmentBean;
import com.grail.synchro.beans.PSMethodologyWaiver;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectSpecsReporting;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.dao.ProjectSpecsDAONew;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class ProjectSpecsDAOImplNew extends JiveJdbcDaoSupport implements ProjectSpecsDAONew {

    private static final Logger LOG = Logger.getLogger(ProjectSpecsDAOImpl.class.getName());
    private SynchroDAOUtil synchroDAOUtil;
   

    private static final String INSERT_PROJECT_SPECS_SQL = "INSERT INTO grailprojectspecs( " +
            " projectid, endmarketid, bizquestion, " +
            " researchobjective, actionstandard, researchdesign, " +
            " sampleprofile, stimulusmaterial, stimulusmaterialshipped, stimulidate, " +
            " screener, consumerccagreement, questionnaire, discussionguide, others, " +
            " npiReferenceNo,deviationfromsm, " +
            " creationby, modificationby, creationdate, modificationdate," +
            " brand, projectOwner, spiContact, proposedMethodology, startDate, endDate, methodologyType, methodologyGroup, status, " +
            " projectdesc, ponumber,ponumber1, abovemarketfinalcost, abovemarketfinalcosttype, categorytype, pslegalapprovaldate, documentation, " +
            " documentationtext) " +
            " VALUES (?, ?, ?, " +
            " ?, ?, ?, " +
            " ?, ?, ?, ?,  " +
            " ?, ?, ?, ?, ?, " +
            " ?, ?, " + 
            " ?, ?, ?, ?," +
            " ?, ?, ?, ?, ?, ?, ?, ?, ?," +
            " ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
    private static final String INSERT_NEW_PROJECT_SPECS_SQL = "INSERT INTO grailprojectspecsnew( " +
            " projectid, documentation, " +
            " documentationtext, status, " +
            " creationby, modificationby, creationdate, modificationdate) " +
            " VALUES (?, ?, ?, " +
            " ?, ?, ?, " +
            " ?, ?) ";
    
    private static final String LOAD_PROJECT_SPECS = " SELECT projectid, endmarketid, bizquestion, " +
            "       researchobjective, actionstandard, researchdesign, " +
            "       sampleprofile, stimulusmaterial, stimulusmaterialshipped, stimulidate, " +
            "       screener, consumerccagreement, questionnaire, discussionguide, others, " +
            "       npireferenceno, deviationfromsm, creationby, " +
            "       modificationby, creationdate, modificationdate, " +
            "		brand, projectOwner, spiContact, proposedMethodology, startDate, endDate, methodologyType, methodologyGroup, status," +
            "       projectdesc, ponumber,ponumber1, " +
            "		isscreenerccapproved, isqdgapproved, isapproved, issendforapproval, screenerccapproveddate, qdgapproveddate, approveddate, screenerccapprover," +
            " 		approver, qdgapprover, legalapprovalstimulus, legalapproverstimulus, legalapprovalscreener, legalapproverscreener," +
            "   legalapprovalquestionnaire, legalapproverquestionnaire, legalapprovaldg, legalapproverdg, isreqclarimodification, legalapprovalccca, legalapproverccca, " +
            "  abovemarketfinalcost, abovemarketfinalcosttype, categorytype, projectspecssavedate, pslegalapprovaldate, reqclarificationmoddate " +
            "  FROM grailprojectspecs ";

    private static final String LOAD_PROJECT_SPECS_NEW = " SELECT projectid, documentation, documentationtext, " +
            "       status," +
            "       creationby, " +
            "       modificationby, creationdate, modificationdate " +
            "  FROM grailprojectspecs ";
    
    private static final String GET_PROJECT_SPECS_BY_PROJECT_EM_ID = LOAD_PROJECT_SPECS + " where projectid = ? and endmarketid=? ";
    private static final String GET_PROJECT_SPECS_BY_PROJECT_ID = LOAD_PROJECT_SPECS + " where projectid = ? ";
    
    private static final String GET_PROJECT_SPECS_NEW_BY_PROJECT_ID = LOAD_PROJECT_SPECS_NEW + " where projectid = ? ";
    
    

    private static final String UPDATE_PROJECT_SPECS__BY_PROJECT_ID = "UPDATE grailprojectspecs " +
            "   SET  bizquestion=?, researchobjective=?, actionstandard=?, " +
            "   researchdesign=?, sampleprofile=?, stimulusmaterial=?, stimulusmaterialshipped=?, stimulidate=?, " +
            "   screener=?, consumerccagreement=?, questionnaire=?, discussionguide=?, others=?, " +
            "   npireferenceno=?, deviationfromsm=?, " +
            "   modificationby=?, modificationdate=?, " +
            "   brand=?, projectOwner=?, spiContact=?, proposedMethodology=?, startDate=?, endDate=?, methodologyGroup=?, methodologyType=?, " +
            "   projectdesc=?, ponumber=?,ponumber1=?, status=?, isscreenerccapproved=?, isqdgapproved=?, " +
            "   legalapprovalstimulus=?, legalapproverstimulus=?, legalapprovalscreener=?, legalapproverscreener=?," +
            "   legalapprovalquestionnaire=?, legalapproverquestionnaire=?, legalapprovaldg=?, legalapproverdg=?, legalapprovalccca=?, legalapproverccca=?, categorytype=?, pslegalapprovaldate=? " +
            "   WHERE projectid = ? and endmarketid=? ";
    
    private static final String UPDATE_PROJECT_SPECS_NEW__BY_PROJECT_ID = "UPDATE grailprojectspecs " +
            "   SET documentation=?, documentationtext=?, " +
            "   modificationby=?, modificationdate=?, status=? "+
            "  WHERE projectid = ? ";
    
    
    private static final String RESET_PROJECT_SPECS = "UPDATE grailprojectspecs " +
            "   SET documentation=?, documentationtext=?  WHERE projectid = ? ";
  
    private static final String GET_PROJECT_SPECS_REPORTING_BY_PROJECT_ID = "SELECT projectid, endmarketid, " +
            "       toplinepresentation, presentation, fullreport, globalsummary, otherreportingrequirements " +
            "  FROM grailprojectspecsreporting WHERE projectid = ? and endmarketid=?";
    
    private static final String GET_PROJECT_SPECS_EM_DETAILS_BY_PROJECT_ID_END_MARKET_ID = "SELECT projectid, endmarketid, " +
            " totalcost, totalcostcurrency, intmgmcost, intmgmtcostcurrency, localmanagementcost, localmanagementcostcurrency, " +
            " operationalHubCost, operationalHubCostType, fieldworkcost, fieldworkcostcurrency, proposedfwagencynames, fwstartestimated, fwcompleteestimated, totalnoofinterviews, " +
            " totalnoofvisits, averageinterviewduration, totalnoofgroups, interviewduration, noofresppergroup, geospreadnational,geospreadurban, cities," +
            " datacollection, fwstartdatelatest, fwenddatelatest, latestfwcomments, finalcost, finalcosttype, finalcostcomments, othercost, othercostcurrency," +
            " originalfinalcost, originalfinalcosttype, projectenddate, projectenddatelatest  " +
            "  FROM grailprojectspecsemdetails WHERE projectid = ? and endmarketid=? ";  
    
    private static final String GET_PROJECT_SPECS_EM_DETAILS_BY_PROJECT_ID = "SELECT projectid, endmarketid, " +
            " totalcost, totalcostcurrency, intmgmcost, intmgmtcostcurrency, localmanagementcost, localmanagementcostcurrency, " +
            " operationalHubCost, operationalHubCostType, fieldworkcost, fieldworkcostcurrency, proposedfwagencynames, fwstartestimated, fwcompleteestimated, totalnoofinterviews, " +
            " totalnoofvisits, averageinterviewduration, totalnoofgroups, interviewduration, noofresppergroup, geospreadnational,geospreadurban, cities," +
            " datacollection, fwstartdatelatest, fwenddatelatest, latestfwcomments, finalcost, finalcosttype, finalcostcomments, othercost, othercostcurrency," +
            " originalfinalcost, originalfinalcosttype, projectenddate, projectenddatelatest  " +
            "  FROM grailprojectspecsemdetails WHERE projectid = ? order by endmarketid asc";   
   

    private static final String INSERT_PROJECT_SPECS_REPORTING = "INSERT INTO grailprojectspecsreporting(" +
            " projectid, endmarketid, " +
            " toplinepresentation, presentation, fullreport, globalsummary, otherReportingRequirements)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String INSERT_PROJECT_SPECS_EM_DETAILS = "INSERT INTO grailprojectspecsemdetails(" +
            " projectid, endmarketid, " +
            " totalcost, totalcostcurrency, intmgmcost, intmgmtcostcurrency, localmanagementcost, localmanagementcostcurrency, " +
            " operationalHubCost, operationalHubCostType, fieldworkcost, fieldworkcostcurrency, proposedfwagencynames, fwstartestimated, fwcompleteestimated, totalnoofinterviews," +
            " totalnoofvisits, averageinterviewduration, totalnoofgroups, interviewduration, noofresppergroup, geospreadnational,geospreadurban, cities," +
            " datacollection, fwstartdatelatest, fwenddatelatest, latestfwcomments, finalcost, finalcosttype, finalcostcomments, othercost, othercostcurrency, originalfinalcost, originalfinalcosttype, projectenddate, projectenddatelatest) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?)";
    
   

    private static final String UPDATE_PROJECT_SPECS_REPORTING_BY_PROJECT_ID = "UPDATE grailprojectspecsreporting " +
            "   SET toplinepresentation=?, presentation=?, fullreport=?, globalsummary=?, " +
            "   otherReportingRequirements=? " +
            " WHERE projectid = ? and endmarketid=? ";
   
    private static final String UPDATE_PROJECT_SPECS_EM_DETAILS_BY_PROJECT_ID = "UPDATE grailprojectspecsemdetails " +
            "   SET  totalcost=?, totalcostcurrency=?, intmgmcost=?, intmgmtcostcurrency=?, " +
            "   localmanagementcost=?, localmanagementcostcurrency=?, operationalHubCost=?, operationalHubCostType=?, fieldworkcost=?, fieldworkcostcurrency=?, " +
            "   proposedfwagencynames=?, fwstartestimated=?, fwcompleteestimated=?, totalnoofinterviews=?, " +
            "   totalnoofvisits=?, averageinterviewduration=?, totalnoofgroups=?, interviewduration=?," +
            "   noofresppergroup=?, geospreadnational=? ,geospreadurban=?, cities=?, datacollection=?," +
            "   fwstartdatelatest=?, fwenddatelatest=?, latestfwcomments=?, finalcost=?, finalcosttype=?, finalcostcomments=?, othercost=?, othercostcurrency=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String UPDATE_PROJECT_SPECS_EM_FW_DETAILS_BY_PROJECT_ID = "UPDATE grailprojectspecsemdetails " +
            "   SET  totalcost=?, totalcostcurrency=?, intmgmcost=?, intmgmtcostcurrency=?, " +
            "   localmanagementcost=?, localmanagementcostcurrency=?, operationalHubCost=?, operationalHubCostType=?, fieldworkcost=?, fieldworkcostcurrency=?, " +
            "   proposedfwagencynames=?, fwstartestimated=?, fwcompleteestimated=?, totalnoofinterviews=?, " +
            "   totalnoofvisits=?, averageinterviewduration=?, totalnoofgroups=?, interviewduration=?," +
            "   noofresppergroup=?, geospreadnational=? ,geospreadurban=?, cities=?, " +
            "   fwstartdatelatest=?, fwenddatelatest=?, latestfwcomments=?, finalcost=?, finalcosttype=?, finalcostcomments=?, " +
            "   othercost=?, othercostcurrency=?, projectenddate=?, projectenddatelatest=?  " +
            "  WHERE projectid = ? and endmarketid=? ";

    private static final String INSERT_ATTACHMENT = "INSERT INTO grailfieldattachment(projectid, endmarketid, " +
    		"	fieldcategoryid, attachmentid, userid)" +
            "  VALUES (?, ?, ?, ?, ?)";
    private static final String REMOVE_ATTACHMENT = "DELETE FROM grailfieldattachment where attachmentid=?";
   
    
    private static final String UPDATE_PIB_REPORTING_SINGLE_ENDMARKET_ID = "UPDATE grailpibreporting" +
            " SET endmarketid=? WHERE projectid = ?";

    private static final String UPDATE_PIB_STAKEHOLDER_SINGLE_ENDMARKET_ID = "UPDATE grailpibstakeholderlist" +
            " SET endmarketid=? WHERE projectid = ?";
    
    private static final String UPDATE_PIB_ATTACHMENT_SINGLE_ENDMARKET_ID = "UPDATE grailfieldattachment" +
            " SET endmarketid=? WHERE projectid = ?";
    
    private static final String APPROVE_PROJECT_SPECS_SCREENER_CC = "UPDATE grailprojectspecs " +
            "   SET  isscreenerccapproved=?, screenerccapproveddate=?, screenerccapprover=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String REJECT_PROJECT_SPECS_SCREENER_CC = "UPDATE grailprojectspecs " +
            "   SET  isscreenerccapproved=?, screenerccapproveddate=?, screenerccapprover=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String APPROVE_PROJECT_SPECS_QDG = "UPDATE grailprojectspecs " +
            "   SET  isqdgapproved=?, qdgapproveddate=?, qdgapprover=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String REJECT_PROJECT_SPECS_QDG = "UPDATE grailprojectspecs " +
            "   SET  isqdgapproved=?, qdgapproveddate=?, qdgapprover=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    private static final String APPROVE_PROJECT_SPECS = "UPDATE grailprojectspecs " +
            "   SET  isapproved=?, approveddate=?, approver=? " +
            "  WHERE projectid = ?  and endmarketid=? ";
    
    private static final String UPDATE_PROJECT_SPECS_STATUS = "UPDATE grailprojectspecs SET status=? WHERE projectid = ?";
    
    private static final String UPDATE_PROJECT_SPECS_SEND_FOR_APPROVAL = "UPDATE grailprojectspecs SET issendforapproval=?, projectspecssavedate=? WHERE projectid = ? and endmarketid=? ";
  
    
    private static final String UPDATE_PROJECT_SPECS_SEND_FOR_APPROVAL_SM = "UPDATE grailprojectspecs SET issendforapproval=?, projectspecssavedate=? WHERE projectid = ? ";

    
    private static final String GET_ATTACHMENT_BY_OBJECT = "SELECT * from jiveattachment where objecttype=? AND objectid=?";
    
    private static final String INSERT_PS_METHODOLOGY_WAIVER = "INSERT INTO grailpsmethodologywaiver( " +
            " projectid, endmarketid, methodologydeviationrationale, methodologyapprover, creationby, " +
            "       modificationby, creationdate, modificationdate, status) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    private static final String INSERT_PS_PIB_METHODOLOGY_WAIVER = "INSERT INTO grailpsmethodologywaiver( " +
            " projectid, endmarketid, methodologydeviationrationale, methodologyapprover, methodologyapprovercomment, isapproved, creationby, " +
            "       modificationby, creationdate, modificationdate, status) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    private static final String UPDATE_PS_METHODOLOGY_WAIVER = "UPDATE grailpsmethodologywaiver " +
            "  SET methodologydeviationrationale=?, methodologyapprover=?, isapproved=?, modificationby=?, modificationdate=?, status=? " +
            "  WHERE projectid = ? and endmarketid=? ";

    private static final String APPROVE_PS_METHODOLOGY_WAIVER = "UPDATE grailpsmethodologywaiver " +
            "  SET methodologyapprovercomment=?, isapproved=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    private static final String REJECT_PS_METHODOLOGY_WAIVER = "UPDATE grailpsmethodologywaiver " +
            "  SET methodologyapprovercomment=?, isapproved=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    private static final String REQ_FOR_INFO_PS_METHODOLOGY_WAIVER = "UPDATE grailpsmethodologywaiver " +
            "  SET methodologyapprovercomment=? " +
            "  WHERE projectid = ? and endmarketid=? ";

    private static final String GET_PS_METHODOLOGY_WAIVER = "SELECT projectid, endmarketid, " +
            "       methodologydeviationrationale, methodologyapprover, methodologyapprovercomment, isapproved, status "+
            "  FROM grailpsmethodologywaiver WHERE projectid = ? and endmarketid=?";

    private static final String UPDATE_PS_METH_WAIVER_SINGLE_ENDMARKET_ID = "UPDATE grailpsmethodologywaiver" +
            " SET endmarketid=?  WHERE projectid = ?";
    
    private static final String UPDATE_PS_END_MARKET_ID = "UPDATE grailprojectspecs SET endmarketid=? WHERE projectid =?";
    private static final String UPDATE_PS_EMD_END_MARKET_ID = "UPDATE grailprojectspecsemdetails SET endmarketid=? WHERE projectid =? ";
    private static final String UPDATE_PS_REPORTING_END_MARKET_ID = "UPDATE grailprojectspecsreporting SET endmarketid=? WHERE projectid =? ";
    private static final String UPDATE_PS_DEVIATION_BY_PROJECT_ID = "UPDATE grailprojectspecs " +
            "   SET deviationfromsm=?, modificationby=?, modificationdate=? WHERE projectid = ? ";
    
    private static final String UPDATE_REQ_CLARI_MODIFICATION = "UPDATE grailprojectspecs" +
                    " SET isreqclarimodification=?, isapproved=?, approveddate=?, approver=?, reqclarificationmoddate=? WHERE projectid = ? and endmarketid=?";
    
    private static final String UPDATE_PS_PROJECT_END_DATE = "UPDATE grailprojectspecs SET enddate=? WHERE projectid =? and endmarketid=? ";
    private static final String UPDATE_PS_PROJECT_END_DATE_ALL = "UPDATE grailprojectspecs SET enddate=? WHERE projectid =? ";
    private static final String UPDATE_PS_PROJECT_AM_FINALCOST = "UPDATE grailprojectspecs SET abovemarketfinalcost=?, abovemarketfinalcosttype=? WHERE projectid =? and endmarketid=? ";
    
    private static final String DELETE_PS = "DELETE from grailprojectspecs WHERE projectid =?";
    private static final String DELETE_PS_EM = "DELETE from grailprojectspecsemdetails WHERE projectid =?";
    private static final String DELETE_PS_REPORTING = "DELETE from grailprojectspecsreporting WHERE projectid =?";
    private static final String DELETE_PS_MW = "DELETE from grailpsmethodologywaiver WHERE projectid =?";
    
    private static final String DELETE_PS_EM_ID = "DELETE from grailprojectspecs WHERE projectid =? and endmarketid=?";
    private static final String DELETE_PS_EM_EM_ID = "DELETE from grailprojectspecsemdetails WHERE projectid =? and endmarketid=?";
    private static final String DELETE_PS_REPORTING_EM_ID = "DELETE from grailprojectspecsreporting WHERE projectid =? and endmarketid=?";
    
    private static final String DELETE_PS_ALL = "DELETE from grailprojectspecs WHERE projectid =? and endmarketid NOT IN ("+SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID+")";
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectSpecsInitiation saveProjectSpecsReporting(final ProjectSpecsInitiation projectSpecsInitiation) {
        try {
            getSimpleJdbcTemplate().update(INSERT_PROJECT_SPECS_REPORTING,  projectSpecsInitiation.getProjectID(),projectSpecsInitiation.getEndMarketID(),
            		BooleanUtils.toIntegerObject(projectSpecsInitiation.getTopLinePresentation()),
                    BooleanUtils.toIntegerObject(projectSpecsInitiation.getPresentation()),
                    BooleanUtils.toIntegerObject(projectSpecsInitiation.getFullreport()),
                    BooleanUtils.toIntegerObject(projectSpecsInitiation.getGlobalSummary()),
                    projectSpecsInitiation.getOtherReportingRequirements());
            return projectSpecsInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Project Specs Reporting Details for projectID" + projectSpecsInitiation.getProjectID() +" and End Market Id -"+ projectSpecsInitiation.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails) {
        try {
            getSimpleJdbcTemplate().update(INSERT_PROJECT_SPECS_EM_DETAILS,  projectSpecsEMDetails.getProjectID(),projectSpecsEMDetails.getEndMarketID(),
            		projectSpecsEMDetails.getTotalCost(),projectSpecsEMDetails.getTotalCostType(),projectSpecsEMDetails.getIntMgmtCost(),
            		projectSpecsEMDetails.getIntMgmtCostType(),projectSpecsEMDetails.getLocalMgmtCost(),projectSpecsEMDetails.getLocalMgmtCostType(),
            		projectSpecsEMDetails.getOperationalHubCost(), projectSpecsEMDetails.getOperationalHubCostType(),
            		projectSpecsEMDetails.getFieldworkCost(), projectSpecsEMDetails.getFieldworkCostType(),
            		projectSpecsEMDetails.getProposedFWAgencyNames(), 
            		projectSpecsEMDetails.getFwStartDate()!=null?projectSpecsEMDetails.getFwStartDate().getTime():0,
            		projectSpecsEMDetails.getFwEndDate()!=null?projectSpecsEMDetails.getFwEndDate().getTime():0,
            		projectSpecsEMDetails.getTotalNoInterviews(),projectSpecsEMDetails.getTotalNoOfVisits(),projectSpecsEMDetails.getAvIntDuration(),projectSpecsEMDetails.getTotalNoOfGroups(),
            		projectSpecsEMDetails.getInterviewDuration(),projectSpecsEMDetails.getNoOfRespPerGroup(),
            		BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadNational()),BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadUrban()),
            		projectSpecsEMDetails.getCities(),   		
            		projectSpecsEMDetails.getDataCollectionMethod()!=null?Joiner.on(",").join(projectSpecsEMDetails.getDataCollectionMethod()):null,
            		projectSpecsEMDetails.getFwStartDateLatest()!=null?projectSpecsEMDetails.getFwStartDateLatest().getTime():0,
            		projectSpecsEMDetails.getFwEndDateLatest()!=null?projectSpecsEMDetails.getFwEndDateLatest().getTime():0,
            		projectSpecsEMDetails.getLatestFWComments(),projectSpecsEMDetails.getFinalCost(),projectSpecsEMDetails.getFinalCostType(),
            		projectSpecsEMDetails.getFinalCostComments(),
            		projectSpecsEMDetails.getOtherCost(),projectSpecsEMDetails.getOtherCostType(),
            		projectSpecsEMDetails.getOriginalFinalCost(),projectSpecsEMDetails.getOriginalFinalCostType(),
            		projectSpecsEMDetails.getProjectEndDate()!=null?projectSpecsEMDetails.getProjectEndDate().getTime():0,
            		projectSpecsEMDetails.getProjectEndDateLatest()!=null?projectSpecsEMDetails.getProjectEndDateLatest().getTime():0);
            
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Project Specs EndMarket Details for projectID" + projectSpecsEMDetails.getProjectID() +" and End Market Id -"+ projectSpecsEMDetails.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }

    }
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectSpecsInitiation updateProjectSpecsReporting(final ProjectSpecsInitiation projectSpecsInitiation) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_PROJECT_SPECS_REPORTING_BY_PROJECT_ID,
            		BooleanUtils.toIntegerObject(projectSpecsInitiation.getTopLinePresentation()),
            		BooleanUtils.toIntegerObject(projectSpecsInitiation.getPresentation()),
                    BooleanUtils.toIntegerObject(projectSpecsInitiation.getFullreport()),
                    BooleanUtils.toIntegerObject(projectSpecsInitiation.getGlobalSummary()),
                    projectSpecsInitiation.getOtherReportingRequirements(),
                    projectSpecsInitiation.getProjectID(), projectSpecsInitiation.getEndMarketID());
            return projectSpecsInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to Update Project Specs Reporting Details for projectID" + projectSpecsInitiation.getProjectID() +" and End Market Id -"+ projectSpecsInitiation.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails)
    {
    	try{
            getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS_EM_DETAILS_BY_PROJECT_ID,
            	projectSpecsEMDetails.getTotalCost(),
            	projectSpecsEMDetails.getTotalCostType(),
            	projectSpecsEMDetails.getIntMgmtCost(),
            	projectSpecsEMDetails.getIntMgmtCostType(),
            	projectSpecsEMDetails.getLocalMgmtCost(),
            	projectSpecsEMDetails.getLocalMgmtCostType(),
            	projectSpecsEMDetails.getOperationalHubCost(),
            	projectSpecsEMDetails.getOperationalHubCostType(),
            	projectSpecsEMDetails.getFieldworkCost(),
            	projectSpecsEMDetails.getFieldworkCostType(),
            	projectSpecsEMDetails.getProposedFWAgencyNames(),
            	projectSpecsEMDetails.getFwStartDate()!=null?projectSpecsEMDetails.getFwStartDate().getTime():0,
            	projectSpecsEMDetails.getFwEndDate()!=null?projectSpecsEMDetails.getFwEndDate().getTime():0,
            	projectSpecsEMDetails.getTotalNoInterviews(),
            	projectSpecsEMDetails.getTotalNoOfVisits(),
            	projectSpecsEMDetails.getAvIntDuration(),
            	projectSpecsEMDetails.getTotalNoOfGroups(),
            	projectSpecsEMDetails.getInterviewDuration(),
            	projectSpecsEMDetails.getNoOfRespPerGroup(),
            	BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadNational()),
            	BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadUrban()),// Geographical Spread
            	projectSpecsEMDetails.getCities(),
            	projectSpecsEMDetails.getDataCollectionMethod()!=null?Joiner.on(",").join(projectSpecsEMDetails.getDataCollectionMethod()):null,
            	projectSpecsEMDetails.getFwStartDateLatest()!=null?projectSpecsEMDetails.getFwStartDateLatest().getTime():0,
            	projectSpecsEMDetails.getFwEndDateLatest()!=null?projectSpecsEMDetails.getFwEndDateLatest().getTime():0,
            	projectSpecsEMDetails.getLatestFWComments(),
            	projectSpecsEMDetails.getFinalCost(),
            	projectSpecsEMDetails.getFinalCostType(),
            	projectSpecsEMDetails.getFinalCostComments(),
            	projectSpecsEMDetails.getOtherCost(),
            	projectSpecsEMDetails.getOtherCostType(),
            	projectSpecsEMDetails.getProjectID(),
            	projectSpecsEMDetails.getEndMarketID());
            
            
           
        } catch (DataAccessException daEx) {
     	   final String message = "Failed to Update Project Specs End Market Details for projectID" + projectSpecsEMDetails.getProjectID() +" and End Market Id -"+ projectSpecsEMDetails.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSpecsFieldWorkDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails)
    {
    	try{
            getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS_EM_FW_DETAILS_BY_PROJECT_ID,
            	projectSpecsEMDetails.getTotalCost(),
            	projectSpecsEMDetails.getTotalCostType(),
            	projectSpecsEMDetails.getIntMgmtCost(),
            	projectSpecsEMDetails.getIntMgmtCostType(),
            	projectSpecsEMDetails.getLocalMgmtCost(),
            	projectSpecsEMDetails.getLocalMgmtCostType(),
            	projectSpecsEMDetails.getOperationalHubCost(),
            	projectSpecsEMDetails.getOperationalHubCostType(),
            	projectSpecsEMDetails.getFieldworkCost(),
            	projectSpecsEMDetails.getFieldworkCostType(),
            	projectSpecsEMDetails.getProposedFWAgencyNames(),
            	projectSpecsEMDetails.getFwStartDate()!=null?projectSpecsEMDetails.getFwStartDate().getTime():0,
            	projectSpecsEMDetails.getFwEndDate()!=null?projectSpecsEMDetails.getFwEndDate().getTime():0,
            	projectSpecsEMDetails.getTotalNoInterviews(),
            	projectSpecsEMDetails.getTotalNoOfVisits(),
            	projectSpecsEMDetails.getAvIntDuration(),
            	projectSpecsEMDetails.getTotalNoOfGroups(),
            	projectSpecsEMDetails.getInterviewDuration(),
            	projectSpecsEMDetails.getNoOfRespPerGroup(),
            	BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadNational()),
            	BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadUrban()),// Geographical Spread
            	projectSpecsEMDetails.getCities(),
            	//projectSpecsEMDetails.getDataCollectionMethod()!=null?Joiner.on(",").join(projectSpecsEMDetails.getDataCollectionMethod()):null,
            	projectSpecsEMDetails.getFwStartDateLatest()!=null?projectSpecsEMDetails.getFwStartDateLatest().getTime():0,
            	projectSpecsEMDetails.getFwEndDateLatest()!=null?projectSpecsEMDetails.getFwEndDateLatest().getTime():0,
            	projectSpecsEMDetails.getLatestFWComments(),
            	projectSpecsEMDetails.getFinalCost(),
            	projectSpecsEMDetails.getFinalCostType(),
            	projectSpecsEMDetails.getFinalCostComments(),
            	projectSpecsEMDetails.getOtherCost(),
            	projectSpecsEMDetails.getOtherCostType(),
            	projectSpecsEMDetails.getProjectEndDate()!=null?projectSpecsEMDetails.getProjectEndDate().getTime():0,
                projectSpecsEMDetails.getProjectEndDateLatest()!=null?projectSpecsEMDetails.getProjectEndDateLatest().getTime():0,
            	projectSpecsEMDetails.getProjectID(),
            	projectSpecsEMDetails.getEndMarketID());
            
            
           
        } catch (DataAccessException daEx) {
     	   final String message = "Failed to Update Project Specs End Market Details for projectID" + projectSpecsEMDetails.getProjectID() +" and End Market Id -"+ projectSpecsEMDetails.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSpecsFieldWorkDetailsMM(final ProjectSpecsEndMarketDetails projectSpecsEMDetails)
    {
    	try{
            getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS_EM_FW_DETAILS_BY_PROJECT_ID,
            	projectSpecsEMDetails.getFinalCost(),
            	projectSpecsEMDetails.getFinalCostType(),
            	//projectSpecsEMDetails.getTotalCost(),
                //projectSpecsEMDetails.getTotalCostType(),
            	projectSpecsEMDetails.getIntMgmtCost(),
            	projectSpecsEMDetails.getIntMgmtCostType(),
            	projectSpecsEMDetails.getLocalMgmtCost(),
            	projectSpecsEMDetails.getLocalMgmtCostType(),
            	projectSpecsEMDetails.getOperationalHubCost(),
            	projectSpecsEMDetails.getOperationalHubCostType(),
            	projectSpecsEMDetails.getFieldworkCost(),
            	projectSpecsEMDetails.getFieldworkCostType(),
            	projectSpecsEMDetails.getProposedFWAgencyNames(),
            	projectSpecsEMDetails.getFwStartDate()!=null?projectSpecsEMDetails.getFwStartDate().getTime():0,
            	projectSpecsEMDetails.getFwEndDate()!=null?projectSpecsEMDetails.getFwEndDate().getTime():0,
            	projectSpecsEMDetails.getTotalNoInterviews(),
            	projectSpecsEMDetails.getTotalNoOfVisits(),
            	projectSpecsEMDetails.getAvIntDuration(),
            	projectSpecsEMDetails.getTotalNoOfGroups(),
            	projectSpecsEMDetails.getInterviewDuration(),
            	projectSpecsEMDetails.getNoOfRespPerGroup(),
            	BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadNational()),
            	BooleanUtils.toIntegerObject(projectSpecsEMDetails.getGeoSpreadUrban()),// Geographical Spread
            	projectSpecsEMDetails.getCities(),
            	//projectSpecsEMDetails.getDataCollectionMethod()!=null?Joiner.on(",").join(projectSpecsEMDetails.getDataCollectionMethod()):null,
            	projectSpecsEMDetails.getFwStartDateLatest()!=null?projectSpecsEMDetails.getFwStartDateLatest().getTime():0,
            	projectSpecsEMDetails.getFwEndDateLatest()!=null?projectSpecsEMDetails.getFwEndDateLatest().getTime():0,
            	projectSpecsEMDetails.getLatestFWComments(),
            	projectSpecsEMDetails.getFinalCost(),
            	projectSpecsEMDetails.getFinalCostType(),
            	projectSpecsEMDetails.getFinalCostComments(),
            	projectSpecsEMDetails.getOtherCost(),
            	projectSpecsEMDetails.getOtherCostType(),
            	projectSpecsEMDetails.getProjectEndDate()!=null?projectSpecsEMDetails.getProjectEndDate().getTime():0,
                projectSpecsEMDetails.getProjectEndDateLatest()!=null?projectSpecsEMDetails.getProjectEndDateLatest().getTime():0,
            	projectSpecsEMDetails.getProjectID(),
            	projectSpecsEMDetails.getEndMarketID());
            
            
           
        } catch (DataAccessException daEx) {
     	   final String message = "Failed to Update Project Specs End Market Details for projectID" + projectSpecsEMDetails.getProjectID() +" and End Market Id -"+ projectSpecsEMDetails.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    public List<ProjectSpecsReporting> getProjectSpecsReporting(final Long projectID, final Long endMarketId) {
        return getSimpleJdbcTemplate().query(GET_PROJECT_SPECS_REPORTING_BY_PROJECT_ID, projectSpecsReportingRowMapper, projectID, endMarketId);
    }
    @Override
    public List<ProjectSpecsEndMarketDetails> getProjectSpecsEMDetails(final Long projectID, final Long endMarketId)
    {
    	return getSimpleJdbcTemplate().query(GET_PROJECT_SPECS_EM_DETAILS_BY_PROJECT_ID_END_MARKET_ID, projectSpecsEMDetailsMapper, projectID, endMarketId);
    }
    @Override
    public List<ProjectSpecsEndMarketDetails> getProjectSpecsEMDetails(final Long projectID)
    {
    	return getSimpleJdbcTemplate().query(GET_PROJECT_SPECS_EM_DETAILS_BY_PROJECT_ID, projectSpecsEMDetailsMapper, projectID);
    }
   
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectSpecsInitiation save(final ProjectSpecsInitiation projectSpecsInitiation) {
    	
     	
    	 try {
         	/*getSimpleJdbcTemplate().update(INSERT_NEW_PROJECT_SPECS_SQL, projectSpecsInitiation.getProjectID(), projectSpecsInitiation.getDocumentation(),
         			projectSpecsInitiation.getDocumentationText(),
         			1,
         			projectSpecsInitiation.getCreationBy(), projectSpecsInitiation.getModifiedBy(), projectSpecsInitiation.getCreationDate(), projectSpecsInitiation.getModifiedDate());
          
             return projectSpecsInitiation;
             */
             
             getSimpleJdbcTemplate().update(INSERT_PROJECT_SPECS_SQL, projectSpecsInitiation.getProjectID(), projectSpecsInitiation.getEndMarketID(),
             		projectSpecsInitiation.getBizQuestion(), 
             		projectSpecsInitiation.getResearchObjective(), projectSpecsInitiation.getActionStandard(),projectSpecsInitiation.getResearchDesign(),
             		projectSpecsInitiation.getSampleProfile(), projectSpecsInitiation.getStimulusMaterial(),projectSpecsInitiation.getStimulusMaterialShipped(),
             		projectSpecsInitiation.getStimuliDate()!=null?projectSpecsInitiation.getStimuliDate().getTime():0,
             		projectSpecsInitiation.getScreener(), projectSpecsInitiation.getConsumerCCAgreement(),projectSpecsInitiation.getQuestionnaire(),
             		projectSpecsInitiation.getDiscussionguide(), projectSpecsInitiation.getOthers(),
             		projectSpecsInitiation.getNpiReferenceNo(), projectSpecsInitiation.getDeviationFromSM(),
             		projectSpecsInitiation.getCreationBy(), projectSpecsInitiation.getModifiedBy(), projectSpecsInitiation.getCreationDate(), projectSpecsInitiation.getModifiedDate(),
             		projectSpecsInitiation.getBrand()!=null?projectSpecsInitiation.getBrand():-1, projectSpecsInitiation.getProjectOwner()!=null?projectSpecsInitiation.getProjectOwner():-1, 
             		projectSpecsInitiation.getSpiContact()!=null?projectSpecsInitiation.getSpiContact():-1,
             		projectSpecsInitiation.getProposedMethodology()!=null?Joiner.on(",").join(projectSpecsInitiation.getProposedMethodology()):null, 
             		projectSpecsInitiation.getStartDate()!=null?projectSpecsInitiation.getStartDate().getTime():0,projectSpecsInitiation.getEndDate()!=null?projectSpecsInitiation.getEndDate().getTime():0,
             		projectSpecsInitiation.getMethodologyType()!=null?projectSpecsInitiation.getMethodologyType():-1,projectSpecsInitiation.getMethodologyGroup()!=null?projectSpecsInitiation.getMethodologyGroup():-1,
             		projectSpecsInitiation.getStatus(),
             		projectSpecsInitiation.getDescription(), projectSpecsInitiation.getPoNumber(), projectSpecsInitiation.getPoNumber1(),
             		projectSpecsInitiation.getAboveMarketFinalCost(), projectSpecsInitiation.getAboveMarketFinalCostType(),
             		projectSpecsInitiation.getCategoryType()!=null?Joiner.on(",").join(projectSpecsInitiation.getCategoryType()):null,
             		projectSpecsInitiation.getPsLegalApprovalDate()!=null?projectSpecsInitiation.getPsLegalApprovalDate().getTime():0,
             		projectSpecsInitiation.getDocumentation(),
                 	projectSpecsInitiation.getDocumentationText());
             return projectSpecsInitiation;
             
         } catch (DataAccessException daEx) {
             final String message = "Failed to SAVE Project Specs Details for projectID" + projectSpecsInitiation.getProjectID();
             LOG.log(Level.SEVERE, message, daEx);
             throw new DAOException(message, daEx);
         }
     	
    }
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectSpecsInitiation update(final ProjectSpecsInitiation projectSpecsInitiation) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS__BY_PROJECT_ID,
                   //projectInitiation.getVersionNumber(),
        		  // proposalInitiation.getEndMarketID(),
        		   projectSpecsInitiation.getBizQuestion(),
        		   projectSpecsInitiation.getResearchObjective(),
        		   projectSpecsInitiation.getActionStandard(),
        		   projectSpecsInitiation.getResearchDesign(),
        		   projectSpecsInitiation.getSampleProfile(),
        		   projectSpecsInitiation.getStimulusMaterial(),
        		   projectSpecsInitiation.getStimulusMaterialShipped(),
                   (projectSpecsInitiation.getStimuliDate() != null) ? projectSpecsInitiation.getStimuliDate().getTime():null,
                   projectSpecsInitiation.getScreener(),
                   projectSpecsInitiation.getConsumerCCAgreement(),
                   projectSpecsInitiation.getQuestionnaire(),
                   projectSpecsInitiation.getDiscussionguide(),
                   projectSpecsInitiation.getOthers(),                   
                   projectSpecsInitiation.getNpiReferenceNo(),
                   projectSpecsInitiation.getDeviationFromSM(),
                   projectSpecsInitiation.getModifiedBy(),
                   projectSpecsInitiation.getModifiedDate(),
                   
                   projectSpecsInitiation.getBrand(),
                   projectSpecsInitiation.getProjectOwner(),
                   projectSpecsInitiation.getSpiContact(),
                   projectSpecsInitiation.getProposedMethodology()!=null?Joiner.on(",").join(projectSpecsInitiation.getProposedMethodology()):null,
                   projectSpecsInitiation.getStartDate()!=null?projectSpecsInitiation.getStartDate().getTime():0,
                   projectSpecsInitiation.getEndDate()!=null?projectSpecsInitiation.getEndDate().getTime():0,
                   projectSpecsInitiation.getMethodologyGroup(),
                   projectSpecsInitiation.getMethodologyType(),
                   projectSpecsInitiation.getDescription(),
                   projectSpecsInitiation.getPoNumber(),
                   projectSpecsInitiation.getPoNumber1(),
                   projectSpecsInitiation.getStatus(),
                   projectSpecsInitiation.getIsScreenerCCApproved(),
                   projectSpecsInitiation.getIsQDGApproved(),
                   BooleanUtils.toIntegerObject(projectSpecsInitiation.getLegalApprovalStimulus()),
                   projectSpecsInitiation.getLegalApproverStimulus(),
                   BooleanUtils.toIntegerObject(projectSpecsInitiation.getLegalApprovalScreener()),
                   projectSpecsInitiation.getLegalApproverScreener(),
                   BooleanUtils.toIntegerObject(projectSpecsInitiation.getLegalApprovalQuestionnaire()),
                   projectSpecsInitiation.getLegalApproverQuestionnaire(),
                   BooleanUtils.toIntegerObject(projectSpecsInitiation.getLegalApprovalDG()),
                   projectSpecsInitiation.getLegalApproverDG(),
                   BooleanUtils.toIntegerObject(projectSpecsInitiation.getLegalApprovalCCCA()),
                   projectSpecsInitiation.getLegalApproverCCCA(),                   
                   projectSpecsInitiation.getCategoryType()!=null?Joiner.on(",").join(projectSpecsInitiation.getCategoryType()):null,
                   projectSpecsInitiation.getPsLegalApprovalDate()!=null?projectSpecsInitiation.getPsLegalApprovalDate().getTime():0,
                   //where clause
                   projectSpecsInitiation.getProjectID(),
                   projectSpecsInitiation.getEndMarketID());
           
           return projectSpecsInitiation;
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update Project Specs Details for projectID" + projectSpecsInitiation.getProjectID() +" and End Market Id -"+ projectSpecsInitiation.getEndMarketID();
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectSpecsInitiation updateNew(final ProjectSpecsInitiation projectSpecsInitiation) {
    	try{
            getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS_NEW__BY_PROJECT_ID,
            		projectSpecsInitiation.getDocumentation(),
            		projectSpecsInitiation.getDocumentationText(),
            		projectSpecsInitiation.getModifiedBy(),
            		projectSpecsInitiation.getModifiedDate(),
            		projectSpecsInitiation.getStatus(),
                    
                    //where clause
            		projectSpecsInitiation.getProjectID());
            return projectSpecsInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Project Specs for projectID  " + projectSpecsInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void resetProjectSpecs(final ProjectSpecsInitiation projectSpecsInitiation) {
        try{
            getSimpleJdbcTemplate().update( RESET_PROJECT_SPECS,
            		null,null,
                    
                    //where clause
            		projectSpecsInitiation.getProjectID());
            
            
            
            
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to RESET PROJECT SPECS For Project Id  " + projectSpecsInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    @Override
    public List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID, final Long endMarketId) {
        return getSimpleJdbcTemplate().query(GET_PROJECT_SPECS_BY_PROJECT_EM_ID, projectSpecsInitiationRowMapper, projectID,endMarketId);
    }
    @Override
    public List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_PROJECT_SPECS_BY_PROJECT_ID, projectSpecsInitiationRowMapper, projectID);
    }
    
    @Override
    public List<ProjectSpecsInitiation> getProjectSpecsInitiationNew(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_PROJECT_SPECS_NEW_BY_PROJECT_ID, newProjectSpecsInitiationRowMapper, projectID);
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
    public void approveScreener(final User user, final Long projectId,final Long endMarketId)
    {
    	 getSimpleJdbcTemplate().update( APPROVE_PROJECT_SPECS_SCREENER_CC,
               1, new Date().getTime(),user.getID(),
               //where clause
               projectId,endMarketId);
    }
    @Override
    public void rejectScreener(final Long projectId,final Long endMarketId)
    {
    	 getSimpleJdbcTemplate().update( REJECT_PROJECT_SPECS_SCREENER_CC,
               2, null,null,
               //where clause
               projectId,endMarketId);
    }
    @Override
    public void approveQDG(final User user, final Long projectId,final Long endMarketId)
    {
    	 getSimpleJdbcTemplate().update( APPROVE_PROJECT_SPECS_QDG,
               1, new Date().getTime(),user.getID(),
               //where clause
               projectId,endMarketId);
    }
    @Override
    public void rejectQDG(final Long projectId,final Long endMarketId)
    {
    	 getSimpleJdbcTemplate().update( REJECT_PROJECT_SPECS_QDG,
    			 2, null,null,
               //where clause
               projectId,endMarketId);
    }
    @Override
    public void approve(final User user, final Long projectId,final Long endMarketId)
    {
    	 getSimpleJdbcTemplate().update( APPROVE_PROJECT_SPECS,
               1, new Date().getTime(),user.getID(),
               //where clause
               projectId, endMarketId);
    }
    @Override
    public void updateProjectSpecsStatus(final Long projectID,final  Long endMarketId,final Integer status) 
    {
    	getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS_STATUS,
    			status,projectID);
    }
    @Override
    public void updateProjectSpecsSendForApproval(final Long projectID,final  Long endMarketId,final Integer sendForApproval) 
    {
    	// For SM
    	if(endMarketId==null)
    	{
    		getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS_SEND_FOR_APPROVAL_SM,
	    			sendForApproval,System.currentTimeMillis(),projectID);
    	}
    	else
    	{
	    	getSimpleJdbcTemplate().update( UPDATE_PROJECT_SPECS_SEND_FOR_APPROVAL,
	    			sendForApproval,System.currentTimeMillis(), projectID, endMarketId);
    	}
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void savePSMethodologyWaiver(final PSMethodologyWaiver psWaiver) {
        try {
            final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endmarketid) from grailpsmethodologywaiver " +
                    "WHERE projectid = ? AND endmarketid = ?",
                    psWaiver.getProjectID(), psWaiver.getEndMarketID());
            if(count==0)
            {
                getSimpleJdbcTemplate().update(INSERT_PS_METHODOLOGY_WAIVER, psWaiver.getProjectID(), psWaiver.getEndMarketID(),
                        psWaiver.getMethodologyDeviationRationale(), psWaiver.getMethodologyApprover(),
                        psWaiver.getCreationBy(), psWaiver.getModifiedBy(), psWaiver.getCreationDate(), psWaiver.getModifiedDate(), psWaiver.getStatus());
            }
            else
            {
                updatePSMethodologyWaiver(psWaiver);
            }

        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE PS Methodology Waiver for projectID" + psWaiver.getProjectID() + " and EndMarket --"+ psWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void savePS_PIBMethodologyWaiver(final PSMethodologyWaiver psWaiver) {
        try {
            
                getSimpleJdbcTemplate().update(INSERT_PS_PIB_METHODOLOGY_WAIVER, psWaiver.getProjectID(), psWaiver.getEndMarketID(),
                        psWaiver.getMethodologyDeviationRationale(), psWaiver.getMethodologyApprover(), psWaiver.getMethodologyApproverComment(),
                        psWaiver.getIsApproved(), psWaiver.getCreationBy(), psWaiver.getModifiedBy(), psWaiver.getCreationDate(), psWaiver.getModifiedDate(), psWaiver.getStatus());
            

        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE PS PIB Methodology Waiver for projectID" + psWaiver.getProjectID() + " and EndMarket --"+ psWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePSMethodologyWaiver(final PSMethodologyWaiver psWaiver) {
        try {

            getSimpleJdbcTemplate().update(UPDATE_PS_METHODOLOGY_WAIVER,
            		psWaiver.getMethodologyDeviationRationale(), psWaiver.getMethodologyApprover(),psWaiver.getIsApproved(),
            		psWaiver.getModifiedBy(), psWaiver.getModifiedDate(), psWaiver.getStatus(), psWaiver.getProjectID(), psWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Update PS Methodology Waiver for projectID" + psWaiver.getProjectID() + " and EndMarket --"+ psWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePSDeviation(final ProjectSpecsInitiation projectSpecsInitiation) {
        try{
            getSimpleJdbcTemplate().update( UPDATE_PS_DEVIATION_BY_PROJECT_ID,
            		projectSpecsInitiation.getDeviationFromSM(),
            		projectSpecsInitiation.getModifiedBy(),
            		projectSpecsInitiation.getModifiedDate(),
                    
                    //where clause
            		projectSpecsInitiation.getProjectID());
          
        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE PS Deviation for projectID  " + projectSpecsInitiation.getProjectID() ;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
        
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePSMethWaiverSingleEndMarketId(final Long projectID,
                                                     final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(
                    UPDATE_PS_METH_WAIVER_SINGLE_ENDMARKET_ID, endMarketID, projectID);

        } catch (DataAccessException daEx) {
            final String message = "Failed to update the Single endmarket Id for Project "
                    + projectID;
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void approvePSMethodologyWaiver(final PSMethodologyWaiver psWaiver) {
        try {

            getSimpleJdbcTemplate().update(APPROVE_PS_METHODOLOGY_WAIVER,
            		psWaiver.getMethodologyApproverComment(), 1,
            		psWaiver.getProjectID(), psWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Approve PS Methodology Waiver for projectID" + psWaiver.getProjectID() + " and EndMarket --"+ psWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void rejectPSMethodologyWaiver(final PSMethodologyWaiver psWaiver) {
        try {

            getSimpleJdbcTemplate().update(REJECT_PS_METHODOLOGY_WAIVER,
            		psWaiver.getMethodologyApproverComment(), 2,
            		psWaiver.getProjectID(), psWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Reject PS Methodology Waiver for projectID" + psWaiver.getProjectID() + " and EndMarket --"+ psWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void reqForInfoPSMethodologyWaiver(final PSMethodologyWaiver psWaiver) {
        try {

            getSimpleJdbcTemplate().update(REQ_FOR_INFO_PS_METHODOLOGY_WAIVER,
            		psWaiver.getMethodologyApproverComment(),
            		psWaiver.getProjectID(), psWaiver.getEndMarketID());


        } catch (DataAccessException daEx) {
            final String message = "Failed to Updtade Req For Methodology PS Methodology Waiver for projectID" + psWaiver.getProjectID() + " and EndMarket --"+ psWaiver.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSpecsEndMarketId(final Long projectId, final Long endMarketId)
    {
    	getSimpleJdbcTemplate().update( UPDATE_PS_END_MARKET_ID,endMarketId,projectId);
    	getSimpleJdbcTemplate().update( UPDATE_PS_EMD_END_MARKET_ID,endMarketId,projectId);
    	getSimpleJdbcTemplate().update( UPDATE_PS_REPORTING_END_MARKET_ID,endMarketId,projectId);
    	getSimpleJdbcTemplate().update( UPDATE_PS_METH_WAIVER_SINGLE_ENDMARKET_ID,endMarketId,projectId);
    	
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateRequestClarificationModification(final Long projectID, final  Long endMarketId, final Integer reqClarification)
    {
    	try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_REQ_CLARI_MODIFICATION,  reqClarification, null, null, null, System.currentTimeMillis(), projectID, endMarketId);

		} catch (DataAccessException daEx) {
			final String message = "Failed to update the Request Clarification the Project Specs for Project "
					+ projectID ;
			 LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSpecsEndDate(final Long projectId, final Long endMarketId, final Date projectEndDate)
    {
    	getSimpleJdbcTemplate().update( UPDATE_PS_PROJECT_END_DATE,projectEndDate!=null?projectEndDate.getTime():0,projectId,endMarketId);
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSpecsEndDate(final Long projectId, final Date projectEndDate)
    {
    	getSimpleJdbcTemplate().update( UPDATE_PS_PROJECT_END_DATE_ALL,projectEndDate!=null?projectEndDate.getTime():0,projectId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSpecsAMFinalCost(final Long projectId, final Long endMarketId, final BigDecimal aboveMarketFinalCost, final Integer aboveMarketFinalCostType)
    {
    	//getSimpleJdbcTemplate().update( UPDATE_PS_PROJECT_AM_FINALCOST,aboveMarketFinalCost, aboveMarketFinalCostType, projectId,endMarketId);
    	getSimpleJdbcTemplate().update( UPDATE_PS_PROJECT_AM_FINALCOST,aboveMarketFinalCost, aboveMarketFinalCostType, projectId, endMarketId);
    }
    
    @Override
    public List<PSMethodologyWaiver> getPSMethodologyWaiver(final Long projectID,final Long endMakerketId)
    {
        return getSimpleJdbcTemplate().query(GET_PS_METHODOLOGY_WAIVER, psMethodologyWaiverRowMapper, projectID, endMakerketId);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteProjectSpecsDetails(final Long projectID)
    {
    	getSimpleJdbcTemplate().update(DELETE_PS,projectID);
    	getSimpleJdbcTemplate().update(DELETE_PS_EM,projectID);
    	getSimpleJdbcTemplate().update(DELETE_PS_REPORTING,projectID);
    	getSimpleJdbcTemplate().update(DELETE_PS_MW,projectID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteProjectSpecsDetails(final Long projectID, final Long endMarketID)
    {
    	getSimpleJdbcTemplate().update(DELETE_PS_EM_ID,projectID,endMarketID);
    	getSimpleJdbcTemplate().update(DELETE_PS_EM_EM_ID,projectID,endMarketID);
    	getSimpleJdbcTemplate().update(DELETE_PS_REPORTING_EM_ID,projectID,endMarketID);
    	
    }
  
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteAllProjectSpecsDetails(final Long projectID)
    {
    	getSimpleJdbcTemplate().update(DELETE_PS_ALL,projectID);
    }
    
    private final RowMapper<PSMethodologyWaiver> psMethodologyWaiverRowMapper = new RowMapper<PSMethodologyWaiver>() {

        public PSMethodologyWaiver mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            PSMethodologyWaiver methodologyWaiver = new PSMethodologyWaiver();
            methodologyWaiver.setEndMarketID(rs.getLong("endmarketid"));
            methodologyWaiver.setProjectID(rs.getLong("projectid"));
            methodologyWaiver.setMethodologyDeviationRationale(rs.getString("methodologydeviationrationale"));
            methodologyWaiver.setMethodologyApprover(rs.getLong("methodologyapprover"));
            methodologyWaiver.setMethodologyApproverComment(rs.getString("methodologyapprovercomment"));
            methodologyWaiver.setIsApproved(rs.getInt("isapproved"));
            methodologyWaiver.setStatus(rs.getInt("status"));

            return methodologyWaiver;
        }
    };
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
    
    private final RowMapper<ProjectSpecsReporting> projectSpecsReportingRowMapper = new RowMapper<ProjectSpecsReporting>() {

        public ProjectSpecsReporting mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	ProjectSpecsReporting reporting = new ProjectSpecsReporting();
            reporting.setEndMarketID(rs.getLong("endMarketID"));
            reporting.setProjectID(rs.getLong("projectid"));
           
            reporting.setFullreport(rs.getBoolean("fullreport"));
            reporting.setPresentation(rs.getBoolean("presentation"));
            reporting.setTopLinePresentation(rs.getBoolean("toplinepresentation"));
            reporting.setGlobalSummary(rs.getBoolean("globalsummary"));
            
            reporting.setOtherReportingRequirements(rs.getString("otherreportingrequirements"));
            return reporting;
        }
    };
    
    private final RowMapper<ProjectSpecsEndMarketDetails> projectSpecsEMDetailsMapper = new RowMapper<ProjectSpecsEndMarketDetails>() {

        public ProjectSpecsEndMarketDetails mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	ProjectSpecsEndMarketDetails projectSpecsEMDetails = new ProjectSpecsEndMarketDetails();
        	projectSpecsEMDetails.setProjectID(rs.getLong("projectid"));
        	projectSpecsEMDetails.setEndMarketID(rs.getLong("endmarketid"));
        	projectSpecsEMDetails.setTotalCost(rs.getBigDecimal("totalcost"));
        	projectSpecsEMDetails.setTotalCostType(rs.getInt("totalcostcurrency"));
        	projectSpecsEMDetails.setIntMgmtCost(rs.getBigDecimal("intmgmcost"));
        	projectSpecsEMDetails.setIntMgmtCostType(rs.getInt("intmgmtcostcurrency"));
        	projectSpecsEMDetails.setLocalMgmtCost(rs.getBigDecimal("localmanagementcost"));
        	projectSpecsEMDetails.setLocalMgmtCostType(rs.getInt("localmanagementcostcurrency"));
        	projectSpecsEMDetails.setOperationalHubCost(rs.getBigDecimal("operationalHubCost"));
        	projectSpecsEMDetails.setOperationalHubCostType(rs.getInt("operationalHubCostType"));
        	projectSpecsEMDetails.setFieldworkCost(rs.getBigDecimal("fieldworkcost"));
        	projectSpecsEMDetails.setFieldworkCostType(rs.getInt("fieldworkcostcurrency"));
        	projectSpecsEMDetails.setProposedFWAgencyNames(rs.getString("proposedfwagencynames"));
        	if(rs.getLong("fwstartestimated") > 0)
            {
        		projectSpecsEMDetails.setFwStartDate(new Date(rs.getLong("fwstartestimated")));
            }
        	if(rs.getLong("fwcompleteestimated") > 0)
            {
        		projectSpecsEMDetails.setFwEndDate(new Date(rs.getLong("fwcompleteestimated")));
            }
        	/*projectSpecsEMDetails.setTotalNoInterviews(rs.getInt("totalnoofinterviews"));
        	projectSpecsEMDetails.setTotalNoOfVisits(rs.getInt("totalnoofvisits"));
        	projectSpecsEMDetails.setAvIntDuration(rs.getInt("averageinterviewduration"));
        	projectSpecsEMDetails.setTotalNoOfGroups(rs.getInt("totalnoofgroups"));
        	projectSpecsEMDetails.setInterviewDuration(rs.getInt("interviewduration"));
        	*/
        	if (rs.getObject("totalnoofinterviews") != null && !rs.wasNull()) {
        		projectSpecsEMDetails.setTotalNoInterviews(rs.getInt("totalnoofinterviews"));
        	}
        	else
        	{
        		projectSpecsEMDetails.setTotalNoInterviews(null);
        	}
        	
        	if (rs.getObject("totalnoofvisits") != null && !rs.wasNull()) {
        		projectSpecsEMDetails.setTotalNoOfVisits(rs.getInt("totalnoofvisits"));
        	}
        	else
        	{
        		projectSpecsEMDetails.setTotalNoOfVisits(null);
        	}
        	
        	if (rs.getObject("averageinterviewduration") != null && !rs.wasNull()) {
        		projectSpecsEMDetails.setAvIntDuration(rs.getInt("averageinterviewduration"));
        	}
        	else
        	{
        		projectSpecsEMDetails.setAvIntDuration(null);
        	}
        	
        	if (rs.getObject("totalnoofgroups") != null && !rs.wasNull()) {
        		projectSpecsEMDetails.setTotalNoOfGroups(rs.getInt("totalnoofgroups"));
        	}
        	else
        	{
        		projectSpecsEMDetails.setTotalNoOfGroups(null);
        	}
        	
        	if (rs.getObject("interviewduration") != null && !rs.wasNull()) {
        		projectSpecsEMDetails.setInterviewDuration(rs.getInt("interviewduration"));
        	}
        	else
        	{
        		projectSpecsEMDetails.setInterviewDuration(null);
        	}
        	
        	if (rs.getObject("noofresppergroup") != null && !rs.wasNull()) {
        		projectSpecsEMDetails.setNoOfRespPerGroup(rs.getInt("noofresppergroup"));
        	}
        	else
        	{
        		projectSpecsEMDetails.setNoOfRespPerGroup(null);
        	}
        	
           	projectSpecsEMDetails.setCities(rs.getString("cities"));
           	projectSpecsEMDetails.setGeoSpreadNational(rs.getBoolean("geospreadnational"));
           	projectSpecsEMDetails.setGeoSpreadUrban(rs.getBoolean("geospreadurban"));
           //	projectSpecsEMDetails.setNoOfRespPerGroup(rs.getInt("noofresppergroup"));
        	projectSpecsEMDetails.setDataCollectionMethod(synchroDAOUtil.getIDs(rs.getString("datacollection")));
        	
        	projectSpecsEMDetails.setFwStartDateLatest(new Date(rs.getLong("fwstartdatelatest")));
        	projectSpecsEMDetails.setFwEndDateLatest(new Date(rs.getLong("fwenddatelatest")));
        	projectSpecsEMDetails.setLatestFWComments(rs.getString("latestfwcomments"));
        	projectSpecsEMDetails.setFinalCost(rs.getBigDecimal("finalcost"));
        	projectSpecsEMDetails.setFinalCostType(rs.getInt("finalcosttype"));
        	projectSpecsEMDetails.setFinalCostComments(rs.getString("finalcostcomments"));
        	projectSpecsEMDetails.setOtherCost(rs.getBigDecimal("othercost"));
        	projectSpecsEMDetails.setOtherCostType(rs.getInt("othercostcurrency"));
        	
        	projectSpecsEMDetails.setOriginalFinalCost(rs.getBigDecimal("originalfinalcost"));
        	projectSpecsEMDetails.setOriginalFinalCostType(rs.getInt("originalfinalcosttype"));
        	
        	if(rs.getLong("projectenddate") > 0)
            {
        		projectSpecsEMDetails.setProjectEndDate(new Date(rs.getLong("projectenddate")));
            }
        	if(rs.getLong("projectenddatelatest") > 0)
            {
        		projectSpecsEMDetails.setProjectEndDateLatest(new Date(rs.getLong("projectenddatelatest")));
            }
        	
            return projectSpecsEMDetails;
        }
    };
    
 
      /**
     * Reusable row mapper for mapping a result set to ProjectSpecsInitiation
     */
    private final RowMapper<ProjectSpecsInitiation> projectSpecsInitiationRowMapper = new RowMapper<ProjectSpecsInitiation>() {
        public ProjectSpecsInitiation mapRow(ResultSet rs, int row) throws SQLException {
        	ProjectSpecsInitiation initiationBean = new ProjectSpecsInitiation();
            initiationBean.setProjectID(rs.getLong("projectid"));
            initiationBean.setEndMarketID(rs.getLong("endmarketid"));
            initiationBean.setBizQuestion(rs.getString("bizquestion"));
            initiationBean.setResearchObjective(rs.getString("researchobjective"));
            initiationBean.setActionStandard(rs.getString("actionstandard"));
            initiationBean.setResearchDesign(rs.getString("researchdesign"));
            initiationBean.setStimulusMaterial(rs.getString("stimulusmaterial"));
            initiationBean.setStimulusMaterialShipped(rs.getString("stimulusmaterialshipped"));
            if(rs.getLong("stimuliDate") > 0)
            {
            	initiationBean.setStimuliDate(new Date(rs.getLong("stimuliDate")));
            }
            initiationBean.setSampleProfile(rs.getString("sampleprofile"));
            initiationBean.setNpiReferenceNo(rs.getString("npireferenceno"));
            initiationBean.setDeviationFromSM(rs.getInt("deviationfromsm"));
            initiationBean.setScreener(rs.getString("screener"));
            initiationBean.setConsumerCCAgreement(rs.getString("consumerccagreement"));
            initiationBean.setQuestionnaire(rs.getString("questionnaire"));
            initiationBean.setDiscussionguide(rs.getString("discussionguide"));
            initiationBean.setOthers(rs.getString("others"));
            
            initiationBean.setBrand(rs.getLong("brand"));
            initiationBean.setProjectOwner(rs.getLong("projectOwner"));
            initiationBean.setSpiContact(rs.getLong("spiContact"));
            initiationBean.setStartDate(new Date(rs.getLong("startDate")));
            initiationBean.setEndDate(new Date(rs.getLong("endDate")));
            initiationBean.setMethodologyGroup(rs.getLong("methodologyGroup"));
            initiationBean.setMethodologyType(rs.getLong("methodologyType"));
            initiationBean.setProposedMethodology(synchroDAOUtil.getIDs(rs.getString("proposedMethodology")));
            initiationBean.setStatus(rs.getInt("status"));
            initiationBean.setDescription(rs.getString("projectdesc"));
            initiationBean.setPoNumber(rs.getString("ponumber"));
            initiationBean.setPoNumber1(rs.getString("ponumber1"));
            
            initiationBean.setIsScreenerCCApproved(rs.getInt("isscreenerccapproved"));
            initiationBean.setIsQDGApproved(rs.getInt("isqdgapproved"));
            initiationBean.setIsApproved(rs.getInt("isapproved"));
          
            initiationBean.setIsSendForApproval(rs.getInt("issendforapproval"));
            if(rs.getLong("screenerccapproveddate")>0)
            {
            	initiationBean.setScreenerCCApprovedDate(new Date(rs.getLong("screenerccapproveddate")));
            }
            if(rs.getLong("qdgapproveddate")>0)
            {
            	initiationBean.setQdgApprovedDate(new Date(rs.getLong("qdgapproveddate")));
            }
            if(rs.getLong("approveddate")>0)
            {
            	initiationBean.setApprovedDate(new Date(rs.getLong("approveddate")));
            }
            initiationBean.setScreenerCCApprover(rs.getLong("screenerccapprover"));
            initiationBean.setQdgApprover(rs.getLong("qdgapprover"));
            initiationBean.setApprover(rs.getLong("approver"));
           
            initiationBean.setLegalApprovalStimulus(rs.getBoolean("legalapprovalstimulus"));
            initiationBean.setLegalApproverStimulus(rs.getString("legalapproverstimulus"));
            initiationBean.setLegalApprovalScreener(rs.getBoolean("legalapprovalscreener"));
            initiationBean.setLegalApproverScreener(rs.getString("legalapproverscreener"));
            initiationBean.setLegalApprovalQuestionnaire(rs.getBoolean("legalapprovalquestionnaire"));
            initiationBean.setLegalApproverQuestionnaire(rs.getString("legalapproverquestionnaire"));
            initiationBean.setLegalApprovalDG(rs.getBoolean("legalapprovaldg"));
            initiationBean.setLegalApproverDG(rs.getString("legalapproverdg"));
            initiationBean.setIsReqClariModification(rs.getBoolean("isreqclarimodification"));
            
            initiationBean.setLegalApprovalCCCA(rs.getBoolean("legalapprovalccca"));
            initiationBean.setLegalApproverCCCA(rs.getString("legalapproverccca"));
            
            initiationBean.setAboveMarketFinalCost(rs.getBigDecimal("abovemarketfinalcost"));
            initiationBean.setAboveMarketFinalCostType(rs.getInt("abovemarketfinalcosttype"));
            initiationBean.setCategoryType(synchroDAOUtil.getIDs(rs.getString("categorytype")));
            
            if(rs.getLong("projectspecssavedate")>0)
            {
            	initiationBean.setProjectSpecsSaveDate(new Date(rs.getLong("projectspecssavedate")));
            }
            if(rs.getLong("pslegalapprovaldate")>0)
            {
            	initiationBean.setPsLegalApprovalDate(new Date(rs.getLong("pslegalapprovaldate")));
            }
            if(rs.getLong("reqclarificationmoddate")>0)
            {
            	initiationBean.setReqClarificationModDate(new Date(rs.getLong("reqclarificationmoddate")));
            }
            
            return initiationBean;
        }
    };
    
    /**
     * Reusable row mapper for mapping a result set to ProjectSpecsInitiation
     */
    private final RowMapper<ProjectSpecsInitiation> newProjectSpecsInitiationRowMapper = new RowMapper<ProjectSpecsInitiation>() {
        public ProjectSpecsInitiation mapRow(ResultSet rs, int row) throws SQLException {
        	ProjectSpecsInitiation initiationBean = new ProjectSpecsInitiation();
            initiationBean.setProjectID(rs.getLong("projectid"));
            
            initiationBean.setDocumentation(rs.getString("documentation"));
            initiationBean.setDocumentationText(rs.getString("documentationtext"));
           
            initiationBean.setStatus(rs.getInt("status"));
            
           
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
