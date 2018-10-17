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

import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.dao.ReportSummaryDAO;
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
public class ReportSummaryDAOImpl extends JiveJdbcDaoSupport implements ReportSummaryDAO {
    private static final Logger LOG = Logger.getLogger(ReportSummaryDAOImpl.class.getName());
    
    private static final String INSERT_REPORT_SUMMARY_SQL = "INSERT INTO grailprojectrepsummary( " +
            " projectid, endmarketid, comments,status, " +
            " creationby, modificationby, creationdate, modificationdate,legalapproval,legalapprover, uploadedsummary, irisOracleSummary, fullReport, summaryReport, summaryForIRIS, repsummarylegalapprovaldate) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?) ";
            

    private static final String LOAD_REPORT_SUMMARY = " SELECT projectid, endmarketid, comments, " +
            "     creationby,   modificationby, creationdate, modificationdate, status," +
            " isspiapproved, spiapprover, spiapprovaldate, sendforapproval, needrevision, legalapproval,legalapprover, uploadedsummary, irisOracleSummary, fullReport, summaryReport, summaryForIRIS, uploadtoiris, uploadtocpsidatabase, needrevisionclicked, " +
            " repsummarysavedate, needrevisionclickdate, repsummarylegalapprovaldate, irisuploaddate, cpsiuploaddate " +
            "  FROM grailprojectrepsummary ";

    private static final String GET_REPORT_SUMMARY_BY_PROJECT_EM_ID = LOAD_REPORT_SUMMARY + " where projectid = ? and endmarketid=? ";
    private static final String GET_REPORT_SUMMARY_BY_PROJECT_ID = LOAD_REPORT_SUMMARY + " where projectid = ? order by endmarketid asc";

    private static final String UPDATE_REPORT_SUMMARY_BY_PROJECT_ID = "UPDATE grailprojectrepsummary " +
            "   SET  comments=?, modificationby=?, modificationdate=?, sendforapproval=?, needrevision=?, status=?, legalapproval=?, legalapprover=?, uploadedsummary=?, irisOracleSummary=?, fullReport=?, summaryReport=?, summaryForIRIS=? , repsummarylegalapprovaldate=? " +
            "  WHERE projectid = ? and endmarketid=? ";
  
    private static final String GET_ATTACHMENT_BY_OBJECT = "SELECT * from jiveattachment where objecttype=? AND objectid=?";
    private static final String APPROVE_REPORT_SUMMARY_SPI = "UPDATE grailprojectrepsummary " +
            "   SET  isspiapproved=?, spiapprovaldate=?, spiapprover=? WHERE projectid = ? and endmarketid=? ";
    
    private static final String APPROVE_REPORT_SUMMARY_SPI_ALL = "UPDATE grailprojectrepsummary " +
            "   SET  isspiapproved=?, spiapprovaldate=?, spiapprover=? WHERE projectid = ?";
    
    private static final String APPROVE_REPORT_SUMMARY_LEGAL = "UPDATE grailprojectrepsummary " +
            "   SET  islegalapproved=?, legalapprovaldate=?, legalapprover=? WHERE projectid = ? and endmarketid=? ";

    private static final String UPDATE_REPORT_SUMMARY_STATUS = "UPDATE grailprojectrepsummary SET status=? WHERE projectid = ?";
    
    private static final String UPDATE_SEND_FOR_APPROVAL = "UPDATE grailprojectrepsummary " +
            "   SET  sendforapproval=?, needrevision = ?, isspiapproved=?, spiapprovaldate=?, spiapprover=?, repsummarysavedate=?  WHERE projectid = ? and endmarketid=? ";
    private static final String UPDATE_SEND_FOR_APPROVAL_ALL = "UPDATE grailprojectrepsummary " +
            "   SET  sendforapproval=?, needrevision = ?, isspiapproved=?, spiapprovaldate=?, spiapprover=?, repsummarysavedate=?  WHERE projectid = ? ";
    
    private static final String UPDATE_NEED_REVISION = "UPDATE grailprojectrepsummary " +
            "   SET  needrevision=?, isspiapproved=?, spiapprovaldate=?, spiapprover=?, needrevisionclicked=?, needrevisionclickdate=?  WHERE projectid = ? and endmarketid=? ";
   
    private static final String UPDATE_FULL_REPORT = "UPDATE grailprojectrepsummary " +
            "   SET  fullreport=? WHERE projectid = ? and endmarketid=? ";
    private static final String UPDATE_FULL_REPORT_ALL = "UPDATE grailprojectrepsummary " +
            "   SET  fullreport=? WHERE projectid = ? ";
    
    private static final String UPDATE_SUMMARY_FOR_IRIS = "UPDATE grailprojectrepsummary " +
            "   SET  summaryforiris=? WHERE projectid = ? and endmarketid=? ";
    private static final String UPDATE_SUMMARY_FOR_IRIS_ALL = "UPDATE grailprojectrepsummary " +
            "   SET  summaryforiris=? WHERE projectid = ? ";
    
    private static final String DELETE_RS_EM_ID = "DELETE from grailprojectrepsummary WHERE projectid =? and endmarketid=?";
    
    private static final String UPDATE_UPLOAD_TO_IRIS = "UPDATE grailprojectrepsummary " +
            "   SET  uploadtoiris=? , irisuploaddate=? WHERE projectid = ? and endmarketid=? ";
    
    private static final String UPDATE_UPLOAD_TO_CPSI_DATABASE = "UPDATE grailprojectrepsummary " +
            "   SET  uploadtocpsidatabase=? , cpsiuploaddate=? WHERE projectid = ? and endmarketid=? ";
    
    private static final String LOAD_SYNCHRO_TO_IRIS = " SELECT projectid, endmarketid, name, " +
            "     brand,   description, bizquestion, researchobjective, actionstandard, researchdesign, conclusions, keyfindings, " +
            " methodologytype, methodologygroup, respondenttype, samplesize, fieldworkstartdate, fieldworkenddate,reportdate, researchagency," +
            "  summarywrittenby, batprimarycontact, relatedstudies, tags, alldocsenglish, disclaimer, irissummaryrequired, irisoptionrationale, " +
            " creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailrssynchrotoiris ";

    private static final String GET_SYNCHRO_TO_IRIS_PROJECT_EM_ID = LOAD_SYNCHRO_TO_IRIS + " where projectid = ? and endmarketid=? ";
    
    private static final String INSERT_SYNCHRO_TO_IRIS_SQL = "INSERT INTO grailrssynchrotoiris( projectid, endmarketid, name, " +
            "     brand,   description, bizquestion, researchobjective, actionstandard, researchdesign, conclusions, keyfindings, " +
            " methodologytype, methodologygroup, respondenttype, samplesize, fieldworkstartdate, fieldworkenddate,reportdate," +
            " researchagency, summarywrittenby, batprimarycontact, relatedstudies, tags, alldocsenglish, disclaimer,irissummaryrequired, irisoptionrationale, " +
            " creationby, modificationby, creationdate, modificationdate )" +
    		" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    private static final String UPDATE_SYNCHRO_TO_IRIS_BY_PROJECT_ID = "UPDATE grailrssynchrotoiris " +
            "   SET  name=?, brand=?,   description=?, bizquestion=?, researchobjective=?, actionstandard=?, researchdesign=?, conclusions=?, keyfindings=?, " +
            " methodologytype=?, methodologygroup=?, respondenttype=?, samplesize=?, fieldworkstartdate=?, fieldworkenddate=?,reportdate=?," +
            " researchagency=?, summarywrittenby=?, batprimarycontact=?, relatedstudies=?, tags=?, alldocsenglish=?, disclaimer=?, irissummaryrequired=?,irisoptionrationale=?," +
            " modificationby=?, modificationdate=? " +
            "  WHERE projectid = ? and endmarketid=? ";
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ReportSummaryInitiation save(final ReportSummaryInitiation reportSummaryInitiation) {
        try {
            getSimpleJdbcTemplate().update(INSERT_REPORT_SUMMARY_SQL,
            		reportSummaryInitiation.getProjectID(),
            		reportSummaryInitiation.getEndMarketID(),
            		reportSummaryInitiation.getComments(),
            		reportSummaryInitiation.getStatus(),
            		reportSummaryInitiation.getCreationBy(),
            		reportSummaryInitiation.getModifiedBy(),
            		reportSummaryInitiation.getCreationDate(), 
            		reportSummaryInitiation.getModifiedDate(),
            		BooleanUtils.toIntegerObject(reportSummaryInitiation.getLegalApproval()),
            		reportSummaryInitiation.getLegalApprover(), 
            		BooleanUtils.toIntegerObject(reportSummaryInitiation.getUploadedSummary()),
            		BooleanUtils.toIntegerObject(reportSummaryInitiation.getIrisOracleSummary()),
            		(reportSummaryInitiation.getFullReport() != null && reportSummaryInitiation.getFullReport())?1:0, 
            		(reportSummaryInitiation.getSummaryReport() != null && reportSummaryInitiation.getSummaryReport())?1:0,
            		(reportSummaryInitiation.getSummaryForIRIS()) != null && reportSummaryInitiation.getSummaryForIRIS()?1:0,
            		reportSummaryInitiation.getRepSummaryLegalApprovalDate() != null ?reportSummaryInitiation.getRepSummaryLegalApprovalDate().getTime():0);
            return reportSummaryInitiation;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Project Report Summary Details for projectID" + reportSummaryInitiation.getProjectID() +" and End Market Id -"+ reportSummaryInitiation.getEndMarketID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
      
    }
 
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ReportSummaryInitiation update(final ReportSummaryInitiation reportSummaryInitiation) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_REPORT_SUMMARY_BY_PROJECT_ID,
        		   reportSummaryInitiation.getComments(),
        		   reportSummaryInitiation.getModifiedBy(),
        		   reportSummaryInitiation.getModifiedDate(),
        		   reportSummaryInitiation.getSendForApproval()?1:0,
        		   reportSummaryInitiation.getNeedRevision()?1:0,
        		   reportSummaryInitiation.getStatus(),
        		   BooleanUtils.toIntegerObject(reportSummaryInitiation.getLegalApproval()),
        		   reportSummaryInitiation.getLegalApprover(),
        		   BooleanUtils.toIntegerObject(reportSummaryInitiation.getUploadedSummary()),
        		   BooleanUtils.toIntegerObject(reportSummaryInitiation == null?false:reportSummaryInitiation.getIrisOracleSummary()),
	           	   (reportSummaryInitiation.getFullReport() != null && reportSummaryInitiation.getFullReport())?1:0, 
	           	   (reportSummaryInitiation.getSummaryReport() != null && reportSummaryInitiation.getSummaryReport())?1:0,
	           	   (reportSummaryInitiation.getSummaryForIRIS()) != null && reportSummaryInitiation.getSummaryForIRIS()?1:0,
	           		reportSummaryInitiation.getRepSummaryLegalApprovalDate() != null ?reportSummaryInitiation.getRepSummaryLegalApprovalDate().getTime():0,		   
                   //where clause
        		   reportSummaryInitiation.getProjectID(),
        		   reportSummaryInitiation.getEndMarketID());
           return reportSummaryInitiation;
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update Project Report Summary Details for projectID" + reportSummaryInitiation.getProjectID() +" and End Market Id -"+ reportSummaryInitiation.getEndMarketID();
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }
    @Override
    public void updateSendForApproval(final long projectId, final long endMarketId, final Integer sendForApproval) {
    	getSimpleJdbcTemplate().update( UPDATE_SEND_FOR_APPROVAL,
    			sendForApproval,0,null, null, null,System.currentTimeMillis(), projectId,endMarketId);
    }
    @Override
    public void updateSendForApproval(final long projectId, final Integer sendForApproval) {
    	getSimpleJdbcTemplate().update( UPDATE_SEND_FOR_APPROVAL_ALL,
    			sendForApproval,0,null, null, null,System.currentTimeMillis(), projectId);
    }
    
    @Override
    public void updateNeedRevision(final long projectId, final long endMarketId) {
    	getSimpleJdbcTemplate().update( UPDATE_NEED_REVISION,
                1, null, null, null, 1,System.currentTimeMillis(), projectId,endMarketId);
    }
    @Override
    public void updateFullReport(final long projectId, final long endMarketId, final Integer fullReport) {
    	getSimpleJdbcTemplate().update( UPDATE_FULL_REPORT,
    			fullReport, projectId,endMarketId);
    }
    @Override
    public void updateFullReport(final long projectId, final Integer fullReport) {
    	getSimpleJdbcTemplate().update( UPDATE_FULL_REPORT_ALL,
    			fullReport, projectId);
    }
    
    @Override
    public void updateSummaryForIris(final long projectId, final long endMarketId, final Integer summaryForIris) {
    	getSimpleJdbcTemplate().update( UPDATE_SUMMARY_FOR_IRIS,
    			summaryForIris, projectId,endMarketId);
    }
    @Override
    public void updateSummaryForIris(final long projectId, final Integer summaryForIris) {
    	getSimpleJdbcTemplate().update( UPDATE_SUMMARY_FOR_IRIS_ALL,
    			summaryForIris, projectId);
    }
    
    @Override
    public List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID, final Long endMarketId) {
        return getSimpleJdbcTemplate().query(GET_REPORT_SUMMARY_BY_PROJECT_EM_ID, reportSummaryInitiationRowMapper, projectID,endMarketId);
    }
    @Override
    public List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_REPORT_SUMMARY_BY_PROJECT_ID, reportSummaryInitiationRowMapper, projectID);
    }
   
    @Override
    public void approveSPI(final User user, final Long projectId,final  Long endMarketId)
    {
    	 getSimpleJdbcTemplate().update( APPROVE_REPORT_SUMMARY_SPI,
                 1, new Date().getTime(),user.getID(),
                 //where clause
                 projectId,endMarketId);
    }
    @Override
    public void approveSPI(final User user, final Long projectId)
    {
    	 getSimpleJdbcTemplate().update( APPROVE_REPORT_SUMMARY_SPI_ALL,
                 1, new Date().getTime(),user.getID(),
                 //where clause
                 projectId);
    }
    @Override
    public void approveLegal(final User user, final Long projectId,final  Long endMarketId)
    {
    	 getSimpleJdbcTemplate().update( APPROVE_REPORT_SUMMARY_LEGAL,
                 1, new Date().getTime(),user.getID(),
                 //where clause
                 projectId,endMarketId);
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
    public void updateReportSummaryStatus(final Long projectID,final  Long endMarketId,final Integer status)
    {
    	getSimpleJdbcTemplate().update( UPDATE_REPORT_SUMMARY_STATUS, status,projectID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteReportSummaryDetails(final Long projectID, final Long endMarketID)
    {
    	getSimpleJdbcTemplate().update(DELETE_RS_EM_ID,projectID,endMarketID);
    	
    }
    
    @Override
    public void updateUploadToIRIS(final long projectId, final long endMarketId, final Integer uploadToIRIS) {
    	getSimpleJdbcTemplate().update( UPDATE_UPLOAD_TO_IRIS,
    			uploadToIRIS, System.currentTimeMillis(), projectId,endMarketId);
    }
    
    @Override
    public void updateUploadToCPSIDatabase(final long projectId, final long endMarketId, final Integer uploadToCPSIDatabase) {
    	getSimpleJdbcTemplate().update( UPDATE_UPLOAD_TO_CPSI_DATABASE,
    			uploadToCPSIDatabase, System.currentTimeMillis(), projectId,endMarketId);
    }
    
    @Override
    public List<SynchroToIRIS> getSynchroToIRIS(final Long projectID, final Long endMarketId) {
        return getSimpleJdbcTemplate().query(GET_SYNCHRO_TO_IRIS_PROJECT_EM_ID, synchroToIRISRowMapper, projectID,endMarketId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateSynchroToIRIS(final SynchroToIRIS synchroToIRIS) {

    	    
    	
    	 try {

             int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailrssynchrotoiris where projectid = ? and endmarketid=?", synchroToIRIS.getProjectID(), synchroToIRIS.getEndMarketId());

             if(count>0)
             {
            	 getSimpleJdbcTemplate().update( UPDATE_SYNCHRO_TO_IRIS_BY_PROJECT_ID,
            			 synchroToIRIS.getProjectName(),synchroToIRIS.getBrand(), synchroToIRIS.getProjectDesc(),
            			 synchroToIRIS.getBizQuestion(), synchroToIRIS.getResearchObjective(), synchroToIRIS.getActionStandard(), 
            			 synchroToIRIS.getResearchDesign(), synchroToIRIS.getConclusions(), synchroToIRIS.getKeyFindings(), synchroToIRIS.getMethodologyType(),
            			 synchroToIRIS.getMethodologyGroup(), synchroToIRIS.getRespondentType(), synchroToIRIS.getSampleSize(),
            			(synchroToIRIS.getFieldWorkStartDate() != null)?synchroToIRIS.getFieldWorkStartDate().getTime():null,
            			(synchroToIRIS.getFieldWorkEndDate() != null)?synchroToIRIS.getFieldWorkEndDate().getTime():null,
            			(synchroToIRIS.getReportDate() != null)?synchroToIRIS.getReportDate().getTime():null,
            			 synchroToIRIS.getResearchAgency(), synchroToIRIS.getSummaryWrittenBy(),
            			 synchroToIRIS.getBatPrimaryContact(), synchroToIRIS.getRelatedStudy(), synchroToIRIS.getTags(), BooleanUtils.toIntegerObject(synchroToIRIS.getAllDocsEnglish()),
            			 BooleanUtils.toIntegerObject(synchroToIRIS.getDisclaimer()), synchroToIRIS.getIrisSummaryRequired(), synchroToIRIS.getIrisOptionRationale(), synchroToIRIS.getModifiedBy(), synchroToIRIS.getModifiedDate(), synchroToIRIS.getProjectID(), synchroToIRIS.getEndMarketId());
             }
             

             else
             {
            	 getSimpleJdbcTemplate().update(INSERT_SYNCHRO_TO_IRIS_SQL,
            			 synchroToIRIS.getProjectID(),
            			 synchroToIRIS.getEndMarketId(),
            			 synchroToIRIS.getProjectName(),
            			 synchroToIRIS.getBrand(),
            			 synchroToIRIS.getProjectDesc(),
            			 synchroToIRIS.getBizQuestion(),
            			 synchroToIRIS.getResearchObjective(), 
            			 synchroToIRIS.getActionStandard(),
            			 synchroToIRIS.getResearchDesign(),
            			 synchroToIRIS.getConclusions(),
            			 synchroToIRIS.getKeyFindings(),
            			 synchroToIRIS.getMethodologyType(), synchroToIRIS.getMethodologyGroup(),
            			 synchroToIRIS.getRespondentType(), synchroToIRIS.getSampleSize(), 
            			(synchroToIRIS.getFieldWorkStartDate() != null)?synchroToIRIS.getFieldWorkStartDate().getTime():null,
            			(synchroToIRIS.getFieldWorkEndDate() != null)?synchroToIRIS.getFieldWorkEndDate().getTime():null,
            			(synchroToIRIS.getReportDate() != null)?synchroToIRIS.getReportDate().getTime():null,				 
            			 synchroToIRIS.getResearchAgency(),
            			 synchroToIRIS.getSummaryWrittenBy(),
            			 synchroToIRIS.getBatPrimaryContact(), synchroToIRIS.getRelatedStudy(), synchroToIRIS.getTags(),
                 		BooleanUtils.toIntegerObject(synchroToIRIS.getAllDocsEnglish()),
                 		BooleanUtils.toIntegerObject(synchroToIRIS.getDisclaimer()),
                 		synchroToIRIS.getIrisSummaryRequired(), 
                 		synchroToIRIS.getIrisOptionRationale(),
                 		synchroToIRIS.getCreationBy(),synchroToIRIS.getModifiedBy(),
                 		synchroToIRIS.getCreationDate(), synchroToIRIS.getModifiedDate());
             }

         } catch (DataAccessException e) {
             final String message = "Failed to update Synchro To IRIS";
             LOG.info(message);
             throw new DAOException(message, e);
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
    
      /**
     * Reusable row mapper for mapping a result set to ProjectSpecsInitiation
     */
    private final RowMapper<ReportSummaryInitiation> reportSummaryInitiationRowMapper = new RowMapper<ReportSummaryInitiation>() {
        public ReportSummaryInitiation mapRow(ResultSet rs, int row) throws SQLException {
        	ReportSummaryInitiation initiationBean = new ReportSummaryInitiation();
            initiationBean.setProjectID(rs.getLong("projectid"));
            initiationBean.setEndMarketID(rs.getLong("endmarketid"));
            initiationBean.setComments(rs.getString("comments"));
            initiationBean.setStatus(rs.getInt("status"));
            
            initiationBean.setIsSPIApproved(rs.getBoolean("isspiapproved"));
            initiationBean.setSpiApprover(rs.getLong("spiapprover"));
            if(rs.getLong("spiapprovaldate")>0)
            {	
            	initiationBean.setSpiApprovalDate(new Date(rs.getLong("spiapprovaldate")));
            }
           /* initiationBean.setIsLegalApproved(rs.getBoolean("islegalapproved"));
            if(rs.getLong("legalapprovaldate")>0)
            {
            	initiationBean.setLegalApprovalDate(new Date(rs.getLong("legalapprovaldate")));
            }*/
            initiationBean.setLegalApprover(rs.getString("legalapprover"));
            initiationBean.setLegalApproval(rs.getBoolean("legalapproval"));
            initiationBean.setSendForApproval(rs.getBoolean("sendforapproval"));
            initiationBean.setNeedRevision(rs.getBoolean("needrevision"));
            initiationBean.setUploadedSummary(rs.getBoolean("uploadedsummary"));
            initiationBean.setIrisOracleSummary(rs.getBoolean("irisOracleSummary"));
            initiationBean.setFullReport(rs.getBoolean("fullreport"));
            initiationBean.setSummaryReport(rs.getBoolean("summaryreport"));
            initiationBean.setSummaryForIRIS(rs.getBoolean("summaryforiris"));
            
            initiationBean.setUploadToIRIS(rs.getBoolean("uploadtoiris"));
            initiationBean.setUploadToCPSIDdatabase(rs.getBoolean("uploadtocpsidatabase"));
            initiationBean.setNeedRevisionClicked(rs.getBoolean("needrevisionclicked"));
            
            if(rs.getLong("repsummarysavedate")>0)
            {	
            	initiationBean.setRepSummarySaveDate(new Date(rs.getLong("repsummarysavedate")));
            }
            if(rs.getLong("needrevisionclickdate")>0)
            {	
            	initiationBean.setNeedRevisionClickDate(new Date(rs.getLong("needrevisionclickdate")));
            }
            if(rs.getLong("repsummarylegalapprovaldate")>0)
            {	
            	initiationBean.setRepSummaryLegalApprovalDate(new Date(rs.getLong("repsummarylegalapprovaldate")));
            }
            if(rs.getLong("irisuploaddate")>0)
            {	
            	initiationBean.setIrisUploadDate(new Date(rs.getLong("irisuploaddate")));
            }
            if(rs.getLong("cpsiuploaddate")>0)
            {	
            	initiationBean.setCpsiUploadDate(new Date(rs.getLong("cpsiuploaddate")));
            }
            
            initiationBean.setModifiedBy(rs.getLong("modificationby"));
            initiationBean.setModifiedDate(rs.getLong("modificationdate"));
            
            return initiationBean;
        }
    };
    
    /**
     * Reusable row mapper for mapping a result set to SynchroToIRIS
     */
    private final RowMapper<SynchroToIRIS> synchroToIRISRowMapper = new RowMapper<SynchroToIRIS>() {
        public SynchroToIRIS mapRow(ResultSet rs, int row) throws SQLException {
        	SynchroToIRIS initiationBean = new SynchroToIRIS();
            initiationBean.setProjectID(rs.getLong("projectid"));
            initiationBean.setEndMarketId(rs.getLong("endmarketid"));
            initiationBean.setProjectName(rs.getString("name"));
            initiationBean.setBrand(rs.getLong("brand"));
            initiationBean.setProjectDesc(rs.getString("description"));
            initiationBean.setBizQuestion(rs.getString("bizquestion"));
            initiationBean.setActionStandard(rs.getString("actionstandard"));
            initiationBean.setResearchObjective(rs.getString("researchobjective"));
            initiationBean.setResearchDesign(rs.getString("researchdesign"));
            initiationBean.setConclusions(rs.getString("conclusions"));
            initiationBean.setKeyFindings(rs.getString("keyfindings"));
            initiationBean.setMethodologyType(rs.getLong("methodologyType"));
            initiationBean.setMethodologyGroup(rs.getLong("methodologyGroup"));
            
            initiationBean.setRespondentType(rs.getString("respondenttype"));
            initiationBean.setSampleSize(rs.getString("samplesize"));
            if(rs.getLong("fieldworkstartdate")>0)
            {
            	initiationBean.setFieldWorkStartDate(new Date(rs.getLong("fieldworkstartdate")));
            }
            if(rs.getLong("fieldworkenddate")>0)
            {
            	initiationBean.setFieldWorkEndDate(new Date(rs.getLong("fieldworkenddate")));
            }
            if(rs.getLong("reportdate")>0)
            {
            	initiationBean.setReportDate(new Date(rs.getLong("reportdate")));
            }
            initiationBean.setResearchAgency(rs.getString("researchagency"));
            initiationBean.setSummaryWrittenBy(rs.getLong("summarywrittenby"));
            initiationBean.setBatPrimaryContact(rs.getLong("batprimarycontact"));
            initiationBean.setRelatedStudy(rs.getString("relatedstudies"));
            initiationBean.setTags(rs.getString("tags"));
            initiationBean.setAllDocsEnglish(rs.getBoolean("alldocsenglish")); 
            initiationBean.setDisclaimer(rs.getBoolean("disclaimer")); 
            initiationBean.setIrisSummaryRequired(rs.getInt("irissummaryrequired"));
            initiationBean.setIrisOptionRationale(rs.getString("irisoptionrationale"));
                        
            return initiationBean;
        }
    };
   
    
}
