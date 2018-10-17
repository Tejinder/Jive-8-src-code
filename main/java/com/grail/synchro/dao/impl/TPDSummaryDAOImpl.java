package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.TPDSKUDetails;
import com.grail.synchro.beans.TPDSummary;
import com.grail.synchro.dao.TPDSummaryDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;

/**
 * @author: tejinder
 * @since: 1.0
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class TPDSummaryDAOImpl extends JiveJdbcDaoSupport implements TPDSummaryDAO {

    private static final Logger LOG = Logger.getLogger(TPDSummaryDAOImpl.class.getName());
    
    private static final String INSERT_TPD_SUMMARY = "INSERT INTO grailprojecttpdsummary( " +
            " projectid, researchdoneon, productdescription, productdescriptiontext, hasproductmodification, tpdmodificationdate, taocode, " +
            " creationby, modificationby, creationdate, modificationdate) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    
    private static final String INSERT_TPD_SKU_DETAILS = "INSERT INTO grailprojecttpdskudetails( " +
            " projectid, submissiondate, hasproductmodification, tpdmodificationdate, taocode, sameasprevsubmitted, rowid, isrowsave) " +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?) ";
            
    private static final String DELETE_TPD_SKU_DETAILS = "DELETE FROM grailprojecttpdskudetails where projectid = ?";

    private static final String LOAD_TPD_SUMMARY = " SELECT tpd.projectid, tpd.researchdoneon, tpd.productdescription, tpd.productdescriptiontext, tpd.hasproductmodification," +
    		" tpd.tpdmodificationdate, tpd.taocode, tpd.creationby,   tpd.modificationby, tpd.creationdate, tpd.modificationdate, project.tpdpreviouslysubmitted, project.tpdlastsubmissiondate, " +
    		" tpd.legalapprovalstatus, tpd.legalapprover, tpd.legalapprovaldate, tpd.legalapproveroffline " +
            "  FROM grailprojecttpdsummary tpd, grailproject project ";

    private static final String GET_TPD_SUMMARY_PROJECT_ID = LOAD_TPD_SUMMARY + " where tpd.projectid = project.projectid and tpd.projectid = ? ";
    
    private static final String GET_TPD_SKU_DETAILS_PROJECT_ID = "SELECT projectid, submissiondate, hasproductmodification, tpdmodificationdate, taocode, sameasprevsubmitted, rowid, isrowsave  " +
    		" from grailprojecttpdskudetails where projectid = ? order by rowid desc ";
    
    private static final String GET_TPD_SKU_DETAILS_PROJECT_ID_ROW_ID = "SELECT projectid, submissiondate, hasproductmodification, tpdmodificationdate, taocode, sameasprevsubmitted, rowid,  isrowsave " +
    		" from grailprojecttpdskudetails where projectid = ? and rowid=? ";
   

    private static final String UPDATE_TPD_SUMMARY_PROJECT_ID = "UPDATE grailprojecttpdsummary " +
            "   SET  researchdoneon=?, productdescription=?,productdescriptiontext=?, hasproductmodification=?, tpdmodificationdate=? , taocode=?,  " +
            "   modificationby=?, modificationdate=? " +
            "   WHERE projectid = ?  ";
    
    private static final String UPDATE_TPD_SUMMARY_LEGAL_DETAILS = "UPDATE grailprojecttpdsummary " +
            "   SET  legalapprovalstatus=?, legalapprover=?,legalapprovaldate=?, " +
            "   modificationby=?, modificationdate=?, legalapproveroffline=? " +
            "   WHERE projectid = ?  ";
    
    private static final String UPDATE_PROPOSAL_LEGAL_DETAILS = "UPDATE grailproposal " +
            "   SET  proposallegalapprovalstatus=?, proposallegalapprover=?,proposalapprovaldate=?, proposallegalapproveroffline=?, " +
            "   modificationby=?, modificationdate=? " +
            "   WHERE projectid = ?  ";
    
    private static final String UPDATE_PIB_LEGAL_DETAILS = "UPDATE grailpib " +
            "   SET  brieflegalapprovalstatus=?, brieflegalapprover=?,briefapprovaldate=?, brieflegalapproveroffline=?,  " +
            "   modificationby=?, modificationdate=? " +
            "   WHERE projectid = ?  ";
  
    private static final String DELETE_PROJ_EVAL_BY_PROJECT_ID = "delete from  grailprojecteval  WHERE projectid = ?";
    
    
    private static final String UPDATE_TPD_SUMMARY_PREV_SUBMITED = "UPDATE grailproject " +
            "   SET  tpdpreviouslysubmitted=?, tpdlastsubmissiondate=? WHERE projectid = ?  ";
     
    
    private SynchroDAOUtil synchroDAOUtil;
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public TPDSummary save(final TPDSummary tpdSummary) {
        try {
            getSimpleJdbcTemplate().update(INSERT_TPD_SUMMARY, tpdSummary.getProjectID(),tpdSummary.getResearchDoneOn(), tpdSummary.getProductDescription(),
            		tpdSummary.getProductDescriptionText(), tpdSummary.getHasProductModification(),
            		tpdSummary.getTpdModificationDate()!=null?tpdSummary.getTpdModificationDate().getTime():-1, tpdSummary.getTaoCode(), tpdSummary.getCreationBy(),
            		tpdSummary.getModifiedBy(), tpdSummary.getCreationDate(), tpdSummary.getModifiedDate());
            return tpdSummary;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE TPD Summary Details for projectID" + tpdSummary.getProjectID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    	
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public TPDSKUDetails saveSKU(final TPDSKUDetails tpdSKUDetails) {
        try {
            getSimpleJdbcTemplate().update(INSERT_TPD_SKU_DETAILS, 
            		tpdSKUDetails.getProjectID(),
            		tpdSKUDetails.getSubmissionDate()!=null?tpdSKUDetails.getSubmissionDate().getTime():-1,
            		tpdSKUDetails.getHasProductModification(),
            		tpdSKUDetails.getTpdModificationDate()!=null?tpdSKUDetails.getTpdModificationDate().getTime():-1,
            		tpdSKUDetails.getTaoCode(),
            		tpdSKUDetails.getSameAsPrevSubmitted()!=null && tpdSKUDetails.getSameAsPrevSubmitted()?1:0,
            		tpdSKUDetails.getRowId(),
            		tpdSKUDetails.getIsRowSaved()!=null && tpdSKUDetails.getIsRowSaved()?1:0);
            return tpdSKUDetails;
        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE TPD SKU Details for projectID" + tpdSKUDetails.getProjectID();
            LOG.log(Level.SEVERE, message, daEx);
            throw new DAOException(message, daEx);
        }
    	
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteTPDSKUDetails(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_TPD_SKU_DETAILS, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the TPD SKU DETAILS Project --  " + projectID;
           
            throw new DAOException(message, e);
        }
    }
   
   
    @Override
    public List<TPDSummary> getTPDSummaryDetails(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_TPD_SUMMARY_PROJECT_ID, tpdSummaryRowMapper, projectID);
    }  
    
    @Override
    public List<TPDSKUDetails> getTPDSKUDetails(final Long projectID) {
        return getSimpleJdbcTemplate().query(GET_TPD_SKU_DETAILS_PROJECT_ID, tpdSKUDetailsRowMapper, projectID);
    }  
    

    @Override
    public List<TPDSKUDetails> getTPDSKUDetailsRowId(final Long projectID, final Integer rowId) {
        return getSimpleJdbcTemplate().query(GET_TPD_SKU_DETAILS_PROJECT_ID_ROW_ID, tpdSKUDetailsRowMapper, projectID, rowId);
    } 
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public TPDSummary update(final TPDSummary tpdSummary) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_TPD_SUMMARY_PROJECT_ID,
        		   tpdSummary.getResearchDoneOn(),
        		   tpdSummary.getProductDescription(),
        		   tpdSummary.getProductDescriptionText(),
        		   tpdSummary.getHasProductModification(),
        		   tpdSummary.getTpdModificationDate()!=null?tpdSummary.getTpdModificationDate().getTime():-1,
        		   tpdSummary.getTaoCode(),
        		   tpdSummary.getModifiedBy(),
        		   tpdSummary.getModifiedDate(),
        		   tpdSummary.getProjectID());
           return tpdSummary;
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update TPD Summary Details for projectID" + tpdSummary.getProjectID() ;
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public TPDSummary updateLegalApprovalDetails(final TPDSummary tpdSummary) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_TPD_SUMMARY_LEGAL_DETAILS,
        		   tpdSummary.getLegalApprovalStatus(),
        		   tpdSummary.getLegalApprover(),
        		   tpdSummary.getLegalApprovalDate()!=null?tpdSummary.getLegalApprovalDate().getTime():-1,
        		   tpdSummary.getModifiedBy(),
        		   tpdSummary.getModifiedDate(),
        		   tpdSummary.getLegalApproverOffline(),
        		   tpdSummary.getProjectID());
           return tpdSummary;
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update TPD Summary Legal Details for projectID" + tpdSummary.getProjectID() ;
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProposalLegalApprovalDetails(final ProposalInitiation proposalInitiation) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_PROPOSAL_LEGAL_DETAILS,
        		   proposalInitiation.getLegalApprovalStatus(),
        		   proposalInitiation.getProposalLegalApprover(),
        		   proposalInitiation.getLegalApprovalDate()!=null?proposalInitiation.getLegalApprovalDate().getTime():-1,
        		   proposalInitiation.getProposalLegalApproverOffline(), 	   
        		   proposalInitiation.getModifiedBy(),
        		   proposalInitiation.getModifiedDate(),
        		   proposalInitiation.getProjectID());
           
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update Proposal Legal Details for projectID" + proposalInitiation.getProjectID() ;
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIBLegalApprovalDetails(final ProjectInitiation projectInitiation) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_PIB_LEGAL_DETAILS,
        		   projectInitiation.getLegalApprovalStatus(),
        		   projectInitiation.getBriefLegalApprover(),
        		   projectInitiation.getLegalApprovalDate()!=null?projectInitiation.getLegalApprovalDate().getTime():-1,
        		   projectInitiation.getBriefLegalApproverOffline(),
        		   projectInitiation.getModifiedBy(),
        		   projectInitiation.getModifiedDate(),
        		   projectInitiation.getProjectID());
           
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update Brief Summary Legal Details for projectID" + projectInitiation.getProjectID() ;
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public TPDSummary updateTPDPrevSubmission(final TPDSummary tpdSummary) {
       try{
           getSimpleJdbcTemplate().update( UPDATE_TPD_SUMMARY_PREV_SUBMITED,
        		   1,
        		   tpdSummary.getLastSubmissionDate()!=null?tpdSummary.getLastSubmissionDate().getTime():-1,
        		   tpdSummary.getProjectID());
           return tpdSummary;
       } catch (DataAccessException daEx) {
    	   final String message = "Failed to Update TPD Summary Previous Submitted Details for projectID" + tpdSummary.getProjectID() ;
           LOG.log(Level.SEVERE, message, daEx);
           throw new DAOException(message, daEx);
       }
    }
    
    
    @Override
    public List<Project> getTPDProjects(final ProjectResultFilter projectResultFilter){
        List<Project> projects = Collections.emptyList();
        
        StringBuilder sql = new StringBuilder();
        sql.append(getTPDDashboardQuery(projectResultFilter,false));
        	
	        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
	            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
	        } else {
	            //sql.append(" order by ").append(getOrderByField("status")).append(" ").append(SynchroDAOUtil.getSortType(0)).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
	        	sql.append(" order by ").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
	        }
	        try {
	            if(projectResultFilter.getStart() != null) {
	                sql.append(" OFFSET ").append(projectResultFilter.getStart());
	            }
	            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
	                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
	            }
	            projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectDashboardRowMapper);
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to load projects by filter";
	            //LOG.error(message, e);
	            throw new DAOException(message, e);
	        }
        
        
	        //sql.append(getOrderByClause(projectResultFilter.getSortField(), projectResultFilter.getAscendingOrder()));
        
        return projects;
    }

    private String getTPDDashboardQueryOLD(final ProjectResultFilter projectResultFilter, boolean fetchCount)
    {
    	StringBuilder sql = new StringBuilder();
        
        	String projectFields="";
        	
        	String todayDate = SynchroUtils.getDateString(new Date().getTime());
        	if(fetchCount)
        	{
        		projectFields = "select count(*)  from grailproject p, grailproposal proposal where p.projectid = proposal.projectid and proposal.proposallegalapprovalstatus = 1 ";
        	}
        	else 
        	{
        		projectFields = "select p.projectid, p.name , p.projecttype, p.processtype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel,  (SELECT em.name FROM grailendmarketinvestment ei, grailendmarketfields em where p.projectid = ei.projectid and ei.endmarketid = em.id) as EndMarket  from grailproject p,  grailproposal proposal where p.projectid = proposal.projectid and proposal.proposallegalapprovalstatus = 1 ";
        	}
	        sql = new StringBuilder(projectFields);
	        sql.append(" and p.status IN ("+ StringUtils.join(projectResultFilter.getProjectStatusFields(), ",")+") ");
        	sql.append(" and p.processtype in (1,2,5,6) ");
        	
        	// This has been added as part of http://redmine.nvish.com/redmine/issues/389
        	sql.append(" and p.projectid not in (select projectid from grailprojecttpdsummary where legalapprovalstatus = 2)");
	       
	        if(projectResultFilter.isFetchCancelProjects())
	        {
	        	sql.append(" and p.iscancel IN (0,1) ");
	        }
	        else
	        {
	        	sql.append(" and p.iscancel IN (0) ");
	        }
	        
	        if(SynchroPermHelper.isEndMarketUserType())
	        {
	        	List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	        	sql.append(" and (case when p.projecttype in (1,2) THEN p.projectid in (SELECT ei.projectid FROM grailendmarketinvestment ei, grailendmarketfields em where p.projectid = ei.projectid and ei.endmarketid = em.id and ei.endmarketid in (");
	        	sql.append(StringUtils.join(emBudgetLocations, ","));
	        	sql.append(")) ELSE p.budgetlocation in (");
	        	sql.append(StringUtils.join(emBudgetLocations, ","));
	        	sql.append(") END) ");
	        }
	        
	        // Check for Legal Users as they can access on EM/GLOBAL/REGIONAL projects with EU markets
	        /*if(SynchroPermHelper.isLegaUserType())
	        {
	        	sql.append(" AND p.processtype in (1,5)");
	        }*/
	        sql.append(applyProjectFilterNew(projectResultFilter));
	        
	        
       
	        return sql.toString();
    }
    
    private String getTPDDashboardQuery(final ProjectResultFilter projectResultFilter, boolean fetchCount)
    {
    	StringBuilder sql = new StringBuilder();
        
        	String projectFields="";
        	
        	String todayDate = SynchroUtils.getDateString(new Date().getTime());
        	if(fetchCount)
        	{
        		//projectFields = "select count(*)  from grailproject p, grailproposal proposal where p.projectid = proposal.projectid and proposal.proposallegalapprovalstatus = 1 ";
        		projectFields = "select count(*)  from grailproject p where p.processtype in (1,2,5,6) AND ((case when p.projecttype in (1,2) THEN p.globaloutcomeeushare = 1  END) OR (case when p.projecttype in (3) and p.processtype in (1,2,3) THEN p.globaloutcomeeushare isNull  END)) ";
        	}
        	else 
        	{
        		//projectFields = "select p.projectid, p.name , p.projecttype, p.processtype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel,  (SELECT em.name FROM grailendmarketinvestment ei, grailendmarketfields em where p.projectid = ei.projectid and ei.endmarketid = em.id) as EndMarket  from grailproject p,  grailproposal proposal where p.projectid = proposal.projectid and proposal.proposallegalapprovalstatus = 1 ";
        		projectFields = "select p.projectid, p.name , p.projecttype, p.processtype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel, p.budgetlocation, p.enddate,  (SELECT em.name FROM grailendmarketinvestment ei, grailendmarketfields em where p.projectid = ei.projectid and ei.endmarketid = em.id) as EndMarket  from grailproject p where p.processtype in (1,2,5,6) AND ((case when p.projecttype in (1,2) THEN p.globaloutcomeeushare = 1  END) OR (case when p.projecttype in (3) and p.processtype in (1,2,3) THEN p.globaloutcomeeushare isNull  END))";
        	}
	        sql = new StringBuilder(projectFields);
	        
	       // Adding the Exception logic for showing the projects on TPD Summary Dashboard
	       // sql.append(" AND ((case when p.status = 1 THEN p.projectid in (select projectid from grailpib where islegalsignoffreq isNull OR islegalsignoffreq = 0)  END) OR (case when p.status > 1 THEN p.projectid in (select projectid from grailproposal where islegalsignoffreq isNull OR islegalsignoffreq = 0)  END))");
	        
	     //   sql.append(" AND ((case when (p.status = 1 AND (SELECT count(*) FROM grailpib pib where p.projectid = pib.projectid ) > 0) THEN p.projectid in (select projectid from grailpib where islegalsignoffreq isNull OR islegalsignoffreq = 0) ELSE p.projectid in (select projectid from grailproject) END) OR (case when p.status > 1 THEN p.projectid in (select projectid from grailproposal where islegalsignoffreq isNull OR islegalsignoffreq = 0)  END))");
	        
	        sql.append(" AND ((case when (p.status = 1 AND (SELECT count(*) FROM grailpib pib where p.projectid = pib.projectid ) > 0) THEN p.projectid in (select projectid from grailpib where islegalsignoffreq isNull OR islegalsignoffreq = 0) ELSE p.projectid in (select projectid from grailproject where status = 1) END) OR (case when p.status=1 THEN p.projectid in (select projectid from grailpib where islegalsignoffreq = 1) END) OR (case when p.status > 1 THEN p.projectid in (select projectid from grailproposal where islegalsignoffreq isNull OR islegalsignoffreq = 0)  END) OR (case when p.status = 2 THEN p.projectid in (select projectid from grailproposal where islegalsignoffreq = 1)  END) OR (case when (p.status = 2 AND (SELECT count(*) FROM grailproposal proposal where p.projectid = proposal.projectid ) = 0) THEN p.projectid in (select projectid from grailpib where islegalsignoffreq isNull OR islegalsignoffreq = 0)  END))");
	        
	       
	        
	        sql.append(" and p.status IN ("+ StringUtils.join(projectResultFilter.getProjectStatusFields(), ",")+") ");
       // 	sql.append(" and p.processtype in (1,2,5,6) ");
        	
        	// This has been added as part of http://redmine.nvish.com/redmine/issues/389
        //	sql.append(" and p.projectid not in (select projectid from grailprojecttpdsummary where legalapprovalstatus = 2)");
	       
	        if(projectResultFilter.isFetchCancelProjects())
	        {
	        	sql.append(" and p.iscancel IN (0,1) ");
	        }
	        else
	        {
	        	sql.append(" and p.iscancel IN (0) ");
	        }
	        
	        if(SynchroPermHelper.isEndMarketUserType())
	        {
	        	List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	        	sql.append(" and (case when p.projecttype in (1,2) THEN p.projectid in (SELECT ei.projectid FROM grailendmarketinvestment ei, grailendmarketfields em where p.projectid = ei.projectid and ei.endmarketid = em.id and ei.endmarketid in (");
	        	sql.append(StringUtils.join(emBudgetLocations, ","));
	        	sql.append(")) ELSE p.budgetlocation in (");
	        	sql.append(StringUtils.join(emBudgetLocations, ","));
	        	sql.append(") END) ");
	        }
	        
	        // Check for Legal Users as they can access on EM/GLOBAL/REGIONAL projects with EU markets
	        /*if(SynchroPermHelper.isLegaUserType())
	        {
	        	sql.append(" AND p.processtype in (1,5)");
	        }*/
	        sql.append(applyProjectFilterNew(projectResultFilter));
	        
	        
       
	        return sql.toString();
    }
    
    @Override
    public Long getTPDDashboardCount(final ProjectResultFilter filter) {
        Long count = 0L;
        StringBuilder sql = new StringBuilder();
        sql.append(getTPDDashboardQuery(filter,true));
       
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        }
        catch (DataAccessException e) {
            final String message = "Failed to load dashboard tpd count by filter";
            
            throw new DAOException(message, e);
        }
        return count;
    }
    
    private String applyProjectFilterNew(final ProjectResultFilter projectResultFilter) {
    	
        StringBuilder sql = new StringBuilder();
        // Keyword filter
           if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
               StringBuilder keywordCondition = new StringBuilder();
               
               List<Integer> status = new ArrayList<Integer>();
               if(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(1);
               }
               if(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(2);
               }
               if(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(3);
               }
               if(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(4);
               }
               if(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(5);
               }
               if(SynchroGlobal.ProjectStatusNew.CLOSE.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(6);
               }
               
               
               keywordCondition.append(" AND (lower(p.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                       .append(" OR ").append("(''|| p.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(projectResultFilter.getKeyword())).append("%'")
                      // .append(" OR ").append("lower(methodologyapproverName) like ").append("'%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                     //  .append(" OR ").append("project.budgetyear = ").append(projectResultFilter.getKeyword().toLowerCase()).append(")");
                       .append(" OR to_char(to_timestamp(p.startdate/1000),'DD/MM/YYYY') like '%").append(projectResultFilter.getKeyword()).append("%'")
                       .append(" OR lower(p.projectmanager) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                       .append(" OR p.projectid in (select e.projectid from grailendmarketinvestment e, grailendmarketfields ef where lower(ef.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%' and e.endmarketid = ef.id )")
                       .append(" OR (''|| p.totalcost ||'') like '%").append(projectResultFilter.getKeyword()).append("%'")
                       .append(" OR to_char(to_timestamp(p.tpdlastsubmissiondate/1000),'DD/MM/YYYY') like '%").append(projectResultFilter.getKeyword()).append("%'");
               
              // Previously Submitted Keyword
               if("yes".contains(projectResultFilter.getKeyword().toLowerCase()))
               {
               		//keywordCondition.append(" OR ").append("p.projectid in (select projectid from grailprojecttpdsummary where previouslysubmitted=1) ");
               		keywordCondition.append(" OR tpdpreviouslysubmitted=1");
               }
               if("no".contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   //keywordCondition.append(" OR ").append("p.projectid not in (select projectid from grailprojecttpdsummary where previouslysubmitted=1) ");
            	   keywordCondition.append(" OR tpdpreviouslysubmitted=0");
               }
               
               
               
               // Status Keyword
               if(status.size()>0)
               {
            	   keywordCondition.append(" OR p.status in (").append(StringUtils.join(status,",")).append(")");
               }
               
               // Project Stage Keyword Track/Not On Track
               if(SynchroConstants.SYNCHRO_PROJECT_ON_TRACK.toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   String todayDate = SynchroUtils.getDateString(new Date().getTime());
            	   keywordCondition.append(" OR p.projectid not in (select projectid from grailproject p1 where ((p1.status = 1 AND to_char(to_timestamp(p1.startdate/1000),'YYYY-MM-DD') > '"+todayDate+"' ) OR (p1.status IN (1,2,3,4,5) AND to_char(to_timestamp(p1.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')))");
               }
               if(SynchroConstants.SYNCHRO_PROJECT_NOT_ON_TRACK.toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   String todayDate = SynchroUtils.getDateString(new Date().getTime());
            	   keywordCondition.append(" OR ((p.status = 1 AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') > '"+todayDate+"' ) OR (p.status IN (1,2,3,4,5) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"'))");
               }

               
               // Methodologies Keyword
               Map<Integer, String> allMethodologies = SynchroGlobal.getMethodologies();
               List<Integer> methodologies = new ArrayList<Integer>();
               if(allMethodologies!=null)
               {
            	   for(Integer methId : allMethodologies.keySet())
            	   {
            		   if(allMethodologies.get(methId).toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
            		   {
            			   methodologies.add(methId);
            		   }
            	   }
            	   if(methodologies!=null && methodologies.size()>0)
            	   {
            		   keywordCondition.append(" OR (p.methodologydetails in ('").append(methodologies.get(0)+"").append("')");
		               if(methodologies.size()>0)
		               {
		            	   keywordCondition.append(" or p.methodologydetails like ('").append(methodologies.get(0)+"").append(",%')");
		            	   keywordCondition.append(" or p.methodologydetails like ('%,").append(methodologies.get(0)+"").append("')");
		            	   keywordCondition.append(" or p.methodologydetails like ('%,") .append(methodologies.get(0)+"").append(",%')");
			               	for(int i=1;i<methodologies.size();i++)
			               	{
			               		keywordCondition.append(" or p.methodologydetails in ('").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or p.methodologydetails like ('").append(methodologies.get(i)+"").append(",%')");
			               		keywordCondition.append(" or p.methodologydetails like ('%,").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or p.methodologydetails like ('%,").append(methodologies.get(i)+"").append(",%')");
			               	}
		               }
		               keywordCondition.append(")");
            	   }
            	   
               }
               
               
             //TPD Status Keyword
               if("May have to be TPD Submitted".toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
                   StringBuilder tpdStatusCondition = new StringBuilder();
                   keywordCondition.append(" OR ((case when (p.status = 1 AND (SELECT count(*) FROM grailpib pib where p.projectid = pib.projectid ) > 0) THEN p.projectid in (select projectid from grailpib where brieflegalapprovalstatus in (1))  END) OR (case when p.status > 1 THEN p.projectid in (select projectid from grailproposal where proposallegalapprovalstatus in (1))  END))");
               }
               if("Doesn't have to be TPD Submitted".toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
                   StringBuilder tpdStatusCondition = new StringBuilder();
                   keywordCondition.append(" OR ((case when (p.status = 1 AND (SELECT count(*) FROM grailpib pib where p.projectid = pib.projectid ) > 0) THEN p.projectid in (select projectid from grailpib where brieflegalapprovalstatus in (2))  END) OR (case when p.status > 1 THEN p.projectid in (select projectid from grailproposal where proposallegalapprovalstatus in (2))  END))");
               }
               if("Pending".toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
                   StringBuilder tpdStatusCondition = new StringBuilder();
                   keywordCondition.append(" OR ((case when (p.status=1 AND (SELECT count(*) FROM grailpib pib where p.projectid = pib.projectid) = 0)  THEN p.projectid in  (select projectid from grailproject)  END) OR (case when (p.status=1 AND (SELECT count(*)  FROM grailpib pib where p.projectid = pib.projectid and (pib.brieflegalapprovalstatus isNull OR pib.brieflegalapprovalstatus = 0)) > 0)  THEN p.projectid in  (select projectid from grailproject)  END))");
               }
               
               keywordCondition.append(")");
               
               
               sql.append(keywordCondition.toString());
           }
          
           //Project Type Filter
           if(projectResultFilter.getProjectTypes() != null && projectResultFilter.getProjectTypes().size()>0 && !isListNull(projectResultFilter.getProjectTypes())) {
               StringBuilder projectTypesCondition = new StringBuilder();
               
               List<Integer> projectStatus = getProjectStatusFromTypes(projectResultFilter.getProjectTypes());
               boolean selectCancel = false;
               
               // This check for filtering the Cancel projects as well
               for(Integer ps: projectStatus)
               {
            	   if(ps==SynchroGlobal.ProjectStatusNew.CANCEL.ordinal())
            	   {
            		   selectCancel = true;
            	   }
               }
               
               if(selectCancel)
               {
            	   projectTypesCondition.append(" AND (p.status in (");
	               
	               
	               projectTypesCondition.append(StringUtils.join(projectStatus,","));
	               projectTypesCondition.append(") OR p.iscancel IN (1))");
	               sql.append(projectTypesCondition.toString());
               }
               else
               {
	               projectTypesCondition.append(" AND p.status in (");
	               
	               
	               projectTypesCondition.append(StringUtils.join(projectStatus,","));
	               projectTypesCondition.append(")");
	               sql.append(projectTypesCondition.toString());
               }
               
           }
           
         //Project Status Filter
           if(projectResultFilter.getProjectStatus() != null && projectResultFilter.getProjectStatus().size()>0 && !isListNull(projectResultFilter.getProjectStatus())) {
               StringBuilder projectStatusCondition = new StringBuilder();
               
               String todayDate = SynchroUtils.getDateString(new Date().getTime());
               
               // If only one of the Project Statys On-Track/Not-On-Track is selected, then only filter the projects.
               if(projectResultFilter.getProjectStatus().size()==1)
               {
	               if(projectResultFilter.getProjectStatus().get(0)==1)
	               {
	            	   //On-Track
	            	   //projectStatusCondition.append(" AND ((p.status > 1 AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+todayDate+"' ) OR (p.status > 3 AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+todayDate+"'))");
	            	   projectStatusCondition.append(" AND p.projectid not in (select projectid from grailproject p1 where ((p1.status = 1 AND to_char(to_timestamp(p1.startdate/1000),'YYYY-MM-DD') > '"+todayDate+"' ) OR (p1.status IN (1,2,3) AND to_char(to_timestamp(p1.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')))");
	            	   
	               }
	               else
	               {
	            	 //Not On-Track
	            	   projectStatusCondition.append(" AND ((p.status = 1 AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') > '"+todayDate+"' ) OR (p.status IN (1,2,3) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"'))");
	               }
	               sql.append(projectStatusCondition.toString());
               }
               
           }
           
         //Project Stage Filter
           if(projectResultFilter.getProjectStages() != null && projectResultFilter.getProjectStages().size()>0 && !isListNull(projectResultFilter.getProjectStages())) {
               StringBuilder projectStagesCondition = new StringBuilder();
               
               List<Integer> projectStatus = getProjectStatusFromStages(projectResultFilter.getProjectStages());
               boolean selectCancel = false;
               
               // This check for filtering the Cancel projects as well
               for(Integer ps: projectStatus)
               {
            	   if(ps==SynchroGlobal.ProjectStatusNew.CANCEL.ordinal())
            	   {
            		   selectCancel = true;
            	   }
               }
               
               if(selectCancel)
               {
            	  
	               
	               projectStagesCondition.append(" AND (p.status in (");
	               projectStagesCondition.append(StringUtils.join(projectStatus,","));
	               projectStagesCondition.append(") OR p.iscancel IN (1))");
	               sql.append(projectStagesCondition.toString());
               }
               else
               {
            	   projectStagesCondition.append(" AND p.status in (");
                   projectStagesCondition.append(StringUtils.join(projectStatus,","));
                   projectStagesCondition.append(")");
                   sql.append(projectStagesCondition.toString());
               }
               
               
              
               
           }
           
           // Start Date Filter
           
           if(projectResultFilter.getStartDateBegin() != null && projectResultFilter.getStartDateComplete()!=null) {
               StringBuilder startDateCondition = new StringBuilder();
               String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
               String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
               
               sql.append(startDateCondition.toString());
               
           }
           else if(projectResultFilter.getStartDateBegin() != null)
           {
        	   StringBuilder startDateCondition = new StringBuilder();
               String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'");
               
               sql.append(startDateCondition.toString());
           }
           else if(projectResultFilter.getStartDateComplete()!=null)
           {
        	   StringBuilder startDateCondition = new StringBuilder();
               String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
               
               sql.append(startDateCondition.toString());
           }
          
           // End Date Filter
           if(projectResultFilter.getEndDateBegin() != null && projectResultFilter.getEndDateComplete()!=null) {
               StringBuilder endDateCondition = new StringBuilder();
               String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
               String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
               endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
               
               sql.append(endDateCondition.toString());
               
           }
           else if (projectResultFilter.getEndDateBegin() != null)
           {
        	   StringBuilder endDateCondition = new StringBuilder();
               String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
               endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'");
               sql.append(endDateCondition.toString());
           }
           else if (projectResultFilter.getEndDateComplete()!=null)
           {
        	   StringBuilder endDateCondition = new StringBuilder();
               String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
               endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
               
               sql.append(endDateCondition.toString());
           }
        
           // Project Manager filter
           if(projectResultFilter.getProjManager() != null && !projectResultFilter.getProjManager().equals("")) {
               StringBuilder projectManagerCondition = new StringBuilder();
               projectManagerCondition.append(" AND p.projectmanager like '%"+projectResultFilter.getProjManager()+"%'");
               
               sql.append(projectManagerCondition.toString());
           }
           
        // Project Initiator filter
           if(projectResultFilter.getProjectInitiator() != null && !projectResultFilter.getProjectInitiator().equals("")) {
               StringBuilder projectInitiatorCondition = new StringBuilder();
               projectInitiatorCondition.append(" AND p.briefcreator in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+projectResultFilter.getProjectInitiator()+"%'");
               projectInitiatorCondition.append(")");
               sql.append(projectInitiatorCondition.toString());
           }
           
         //Category Types Filter
           if(projectResultFilter.getCategoryTypes() != null && projectResultFilter.getCategoryTypes().size()>0 && !isListNull(projectResultFilter.getCategoryTypes())) {
               StringBuilder categoryTypesCondition = new StringBuilder();
               /*categoryTypesCondition.append(" AND p.categorytype in (");
               categoryTypesCondition.append(StringUtils.join(projectResultFilter.getCategoryTypes(),","));
               categoryTypesCondition.append(")");
               sql.append(categoryTypesCondition.toString());
               */
               
               

             
               categoryTypesCondition.append(" AND (p.categorytype in ('")
                       .append(projectResultFilter.getCategoryTypes().get(0)+"").append("')");
               if(projectResultFilter.getCategoryTypes().size()>0)
               {
	            	categoryTypesCondition.append(" or p.categorytype like ('").append(projectResultFilter.getCategoryTypes().get(0)+"").append(",%')");
	            	categoryTypesCondition.append(" or p.categorytype like ('%,").append(projectResultFilter.getCategoryTypes().get(0)+"").append("')");
	            	categoryTypesCondition.append(" or p.categorytype like ('%,") .append(projectResultFilter.getCategoryTypes().get(0)+"").append(",%')");
	               	for(int i=1;i<projectResultFilter.getCategoryTypes().size();i++)
	               	{
	               		categoryTypesCondition.append(" or p.categorytype in ('").append(projectResultFilter.getCategoryTypes().get(i)+"").append("')");
	               		categoryTypesCondition.append(" or p.categorytype like ('").append(projectResultFilter.getCategoryTypes().get(i)+"").append(",%')");
	               		categoryTypesCondition.append(" or p.categorytype like ('%,").append(projectResultFilter.getCategoryTypes().get(i)+"").append("')");
	               		categoryTypesCondition.append(" or p.categorytype like ('%,").append(projectResultFilter.getCategoryTypes().get(i)+"").append(",%')");
	               	}
               }
               categoryTypesCondition.append(")");
               
               sql.append(categoryTypesCondition.toString());
           
               
               
           } 
           //Methodology Details Filter
           if(projectResultFilter.getMethDetails() != null && projectResultFilter.getMethDetails().size()>0 && !isListNull(projectResultFilter.getMethDetails())) {
               StringBuilder methodologyDetailsCondition = new StringBuilder();
              
               
               methodologyDetailsCondition.append(" AND (p.methodologydetails in ('")
                       .append(projectResultFilter.getMethDetails().get(0)+"").append("')");
               if(projectResultFilter.getMethDetails().size()>0)
               {
            	   methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(projectResultFilter.getMethDetails().get(0)+"").append(",%')");
            	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(0)+"").append("')");
            	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,") .append(projectResultFilter.getMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<projectResultFilter.getMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or p.methodologydetails in ('").append(projectResultFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(projectResultFilter.getMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(i)+"").append(",%')");
	               	}
               }
               methodologyDetailsCondition.append(")");
               
               sql.append(methodologyDetailsCondition.toString());
               
           }
         //Methodology Type Filter
           if(projectResultFilter.getMethodologyTypes() != null && projectResultFilter.getMethodologyTypes().size()>0 && !isListNull(projectResultFilter.getMethodologyTypes())) {
               StringBuilder methodologyTypesCondition = new StringBuilder();
               methodologyTypesCondition.append(" AND p.methodologytype in (");
               methodologyTypesCondition.append(StringUtils.join(projectResultFilter.getMethodologyTypes(),","));
               methodologyTypesCondition.append(")");
               sql.append(methodologyTypesCondition.toString());
               
           }
           
         //Brand Filter
           if(projectResultFilter.getBrands() != null && projectResultFilter.getBrands().size()>0 && !isListNull(projectResultFilter.getBrands())) {
               StringBuilder brandsCondition = new StringBuilder();
               brandsCondition.append(" AND (p.brand in (");
               brandsCondition.append(StringUtils.join(projectResultFilter.getBrands(),","));
               brandsCondition.append(")");
               brandsCondition.append(" OR p.studytype in (");
               brandsCondition.append(StringUtils.join(projectResultFilter.getBrands(),","));
               brandsCondition.append("))");
               sql.append(brandsCondition.toString());
               
           }
           
           //Budget Location Filter
           if(projectResultFilter.getBudgetLocations() != null && projectResultFilter.getBudgetLocations().size()>0 && !isListNull(projectResultFilter.getBudgetLocations())) {
               StringBuilder budgetLocationsCondition = new StringBuilder();
               budgetLocationsCondition.append(" AND p.budgetlocation in (");
               budgetLocationsCondition.append(StringUtils.join(projectResultFilter.getBudgetLocations(),","));
               budgetLocationsCondition.append(")");
               sql.append(budgetLocationsCondition.toString());
               
           }
           
           //BudgetYear Filter
           if(projectResultFilter.getBudgetYears() != null && projectResultFilter.getBudgetYears().size()>0 && !isListNull(projectResultFilter.getBudgetYears())) {
               StringBuilder budgetYearsCondition = new StringBuilder();
               budgetYearsCondition.append(" AND p.budgetyear in (");
               budgetYearsCondition.append(StringUtils.join(projectResultFilter.getBudgetYears(),","));
               budgetYearsCondition.append(")");
               sql.append(budgetYearsCondition.toString());
               
           }
           
         //Research End Market Filter
           if(projectResultFilter.getResearchEndMarkets() != null && projectResultFilter.getResearchEndMarkets().size()>0 && !isListNull(projectResultFilter.getResearchEndMarkets())) {
               StringBuilder researchEndMarketsCondition = new StringBuilder();
               researchEndMarketsCondition.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
               researchEndMarketsCondition.append(StringUtils.join(projectResultFilter.getResearchEndMarkets(),","));
               researchEndMarketsCondition.append("))");
               sql.append(researchEndMarketsCondition.toString());
               
           }
           
         //Research Agency Filter
           if(projectResultFilter.getResearchAgencies() != null && projectResultFilter.getResearchAgencies().size()>0 && !isListNull(projectResultFilter.getResearchAgencies())) {
               StringBuilder researchAgenciesCondition = new StringBuilder();
               researchAgenciesCondition.append(" AND p.projectid in (select pcd.projectid from grailprojectcostdetails pcd where pcd.agencyid in (");
               researchAgenciesCondition.append(StringUtils.join(projectResultFilter.getResearchAgencies(),","));
               researchAgenciesCondition.append("))");
               sql.append(researchAgenciesCondition.toString());
               
           }
           
         //Total Cost Filter
           if(projectResultFilter.getTotalCostStart() != null && projectResultFilter.getTotalCostEnd() != null) {
               StringBuilder totalCostsCondition = new StringBuilder();
               totalCostsCondition.append(" AND p.totalcost >="+ projectResultFilter.getTotalCostStart() + " AND p.totalcost <="+ projectResultFilter.getTotalCostEnd());
                            
               sql.append(totalCostsCondition.toString());
               
           }
           else if(projectResultFilter.getTotalCostStart() != null)
           {
        	   StringBuilder totalCostsCondition = new StringBuilder();
               totalCostsCondition.append(" AND p.totalcost >="+ projectResultFilter.getTotalCostStart());
                            
               sql.append(totalCostsCondition.toString());
           }
           else if(projectResultFilter.getTotalCostEnd() != null)
           {
        	   StringBuilder totalCostsCondition = new StringBuilder();
               totalCostsCondition.append(" AND p.totalcost <="+ projectResultFilter.getTotalCostEnd());
                            
               sql.append(totalCostsCondition.toString());
           }
           
           
           //TPD Status Filter
           if(projectResultFilter.getTpdStatus() != null && projectResultFilter.getTpdStatus().size()>0 && !isListNull(projectResultFilter.getTpdStatus())) {
               StringBuilder tpdStatusCondition = new StringBuilder();
             /*  tpdStatusCondition.append(" AND (p.projectid in ( select projectid from grailprojecttpdsummary where legalapprovalstatus in (");
               tpdStatusCondition.append(StringUtils.join(projectResultFilter.getTpdStatus(),","));
               tpdStatusCondition.append("))");
               
               tpdStatusCondition.append(" OR p.projectid in ( select projectid from grailpib where brieflegalapprovalstatus in (");
               tpdStatusCondition.append(StringUtils.join(projectResultFilter.getTpdStatus(),","));
               tpdStatusCondition.append("))");
               
               tpdStatusCondition.append(" OR p.projectid in ( select projectid from grailproposal where proposallegalapprovalstatus in (");
               tpdStatusCondition.append(StringUtils.join(projectResultFilter.getTpdStatus(),","));
               tpdStatusCondition.append(")))");
               sql.append(tpdStatusCondition.toString());
               */
               
               tpdStatusCondition.append(" AND ((case when (p.status = 1 AND (SELECT count(*) FROM grailpib pib where p.projectid = pib.projectid ) > 0) THEN p.projectid in (select projectid from grailpib where brieflegalapprovalstatus in ("+StringUtils.join(projectResultFilter.getTpdStatus(),",")+"))  END) OR (case when p.status > 1 THEN p.projectid in (select projectid from grailproposal where proposallegalapprovalstatus in ("+StringUtils.join(projectResultFilter.getTpdStatus(),",")+"))  END)");
               
               // This is for Pending Status
               if(projectResultFilter.getTpdStatus().contains(new Long("3")))
               {
            	
            	   tpdStatusCondition.append("OR (case when (p.status=1 AND (SELECT count(*) FROM grailpib pib where p.projectid = pib.projectid) = 0)  THEN p.projectid in  (select projectid from grailproject)  END) OR (case when (p.status=1 AND (SELECT count(*)  FROM grailpib pib where p.projectid = pib.projectid and (pib.brieflegalapprovalstatus isNull OR pib.brieflegalapprovalstatus = 0)) > 0)  THEN p.projectid in  (select projectid from grailproject)  END)");
               }
               tpdStatusCondition.append(")");
               sql.append(tpdStatusCondition.toString());
               
               
           }
           
           
	        

	        
           // TPD Submission Date Filter
           if(projectResultFilter.getTpdSubmitDateBegin() != null && projectResultFilter.getTpdSubmitDateComplete()!=null) {
               StringBuilder startDateCondition = new StringBuilder();
               String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getTpdSubmitDateBegin().getTime());
               String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getTpdSubmitDateComplete().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.tpdlastsubmissiondate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(p.tpdlastsubmissiondate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
               
               sql.append(startDateCondition.toString());
               
           }
           else if(projectResultFilter.getTpdSubmitDateBegin() != null)
           {
        	   StringBuilder startDateCondition = new StringBuilder();
               String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getTpdSubmitDateBegin().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.tpdlastsubmissiondate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'");
               
               sql.append(startDateCondition.toString());
           }
           else if(projectResultFilter.getTpdSubmitDateComplete()!=null)
           {
        	   StringBuilder startDateCondition = new StringBuilder();
               String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getTpdSubmitDateComplete().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.tpdlastsubmissiondate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
               
               sql.append(startDateCondition.toString());
           }
           return sql.toString();
           
           
    }

	private String getOrderByField(final String sortField) {
    if(StringUtils.isNotBlank(sortField)) {
        String field = null;
        if(sortField.equals("id")) {
            field = "p.projectID";
        } else if(sortField.equals("name")) {
            field = "p.name";
        } else if(sortField.equals("owner")) {
            field = "ownerName";
        } else if(sortField.equals("brand")) {
            field = "brandName";
        } else if(sortField.equals("year")) {
            field = "to_char(to_timestamp(startDate/1000),'YYYY')::int";
        } else if(sortField.equals("status")) {
            field = "p.status";
        } else if(sortField.equals("projectStartDate")) {
            field = "p.startDate";
        } 
        // This is done for Sorting on the basis of  columns 'Previously submitted' and 'last Submission Date' as these columns are stored separately in grailprojecttpdsummary table.
        //http://redmine.nvish.com/redmine/issues/378
        else if(sortField.equals("previousSubmit")) {
            field = " p.tpdpreviouslysubmitted ";
        }
        else if(sortField.equals("lastSubmitDate")) {
        	field = " p.tpdlastsubmissiondate ";
        }
        
        else {
            field = sortField;
        }
        return field;
    }
    return null;
}
    private boolean isListNull(List<Long> projectFilterList)
    {
    	if(projectFilterList!=null && projectFilterList.size()==1)
    	{
    		if(projectFilterList.get(0)==null)
    		{
    			return true;
    		}
    	}
    	return false;
    }
    private List<Integer> getProjectStatusFromTypes(List<Long> projectTypes)
    {
    	List<Integer> projectStatus = new ArrayList<Integer>();
    	for(Long projectType:projectTypes)
    	{
    		if(projectType==1)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal());
    			
    		}
    		if(projectType==2)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    		}
    		if(projectType==3)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal());
    		}
    		if(projectType==4)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal());
    		}
    	}
    	return projectStatus;
    }
    
    private List<Integer> getProjectStatusFromStages(List<Long> projectStages)
    {
    	List<Integer> projectStatus = new ArrayList<Integer>();
    	for(Long projectStage:projectStages)
    	{
    		if(projectStage==1)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    			
    			
    		}
    		if(projectStage==2)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
    		}
    		if(projectStage==3)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
       		}
    		if(projectStage==4)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal());
       		}
    		if(projectStage==5)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal());
    		}
    	}
    	return projectStatus;
    }
    private final RowMapper<TPDSummary> tpdSummaryRowMapper = new RowMapper<TPDSummary>() {
        public TPDSummary mapRow(ResultSet rs, int row) throws SQLException {
        	TPDSummary initiationBean = new TPDSummary();
            initiationBean.setProjectID(rs.getLong("projectid"));
            initiationBean.setResearchDoneOn(rs.getInt("researchdoneon"));
            initiationBean.setProductDescription(rs.getString("productdescription"));
            initiationBean.setProductDescriptionText(rs.getString("productdescriptiontext"));
            initiationBean.setHasProductModification(rs.getInt("hasproductmodification"));
            if(rs.getLong("tpdmodificationdate")>0)
            {
            	initiationBean.setTpdModificationDate(new Date(rs.getLong("tpdmodificationdate")));
            }
                        
            initiationBean.setTaoCode(rs.getString("taocode"));
            
            initiationBean.setPreviouslySubmitted(rs.getBoolean("tpdpreviouslysubmitted"));
            if(rs.getLong("tpdlastsubmissiondate")>0)
            {
            	initiationBean.setLastSubmissionDate(new Date(rs.getLong("tpdlastsubmissiondate")));
            }
            
            initiationBean.setLegalApprovalStatus(rs.getInt("legalapprovalstatus"));
            initiationBean.setLegalApprover(rs.getLong("legalapprover"));
            
            initiationBean.setLegalApproverOffline(rs.getString("legalapproveroffline"));
            if(rs.getLong("legalapprovaldate")>0)
            {
            	initiationBean.setLegalApprovalDate(new Date(rs.getLong("legalapprovaldate")));
            }
            
            
            return initiationBean;
        }
    };
   
    private final RowMapper<TPDSKUDetails> tpdSKUDetailsRowMapper = new RowMapper<TPDSKUDetails>() {
        public TPDSKUDetails mapRow(ResultSet rs, int row) throws SQLException {
        	TPDSKUDetails initiationBean = new TPDSKUDetails();
            initiationBean.setProjectID(rs.getLong("projectid"));
           // initiationBean.setSkuDetails(rs.getString("skudetails"));
            if(rs.getLong("submissiondate")>0)
            {
            	initiationBean.setSubmissionDate(new Date(rs.getLong("submissiondate")));
            }
            initiationBean.setHasProductModification(rs.getInt("hasproductmodification"));
            if(rs.getLong("tpdmodificationdate")>0)
            {
            	initiationBean.setTpdModificationDate(new Date(rs.getLong("tpdmodificationdate")));
            }
            initiationBean.setTaoCode(rs.getString("taocode"));
            
            initiationBean.setSameAsPrevSubmitted(rs.getBoolean("sameasprevsubmitted"));
            
            initiationBean.setRowId(rs.getInt("rowid"));
            
            initiationBean.setIsRowSaved(rs.getBoolean("isrowsave"));
            
            return initiationBean;
        }
    };
    
    private final ParameterizedRowMapper<Project> projectDashboardRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
            Project project = new Project();
            project.setProjectID(rs.getLong("projectID"));
            project.setName(rs.getString("name"));
            project.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
            
            
            if(rs.getLong("startDate")>0)
            {
            	project.setStartDate(new Date(rs.getLong("startDate")));
            }
           
            project.setTotalCost(rs.getBigDecimal("totalCost"));
           
            project.setStatus(rs.getLong("status"));
           // project.setCapRating(rs.getLong("caprating"));
          
          
            project.setProjectManagerName(rs.getString("projectmanager"));
            project.setProjectType(rs.getInt("projecttype"));
            project.setProcessType(rs.getInt("processtype"));
            project.setEndMarketName(rs.getString("endmarket"));
            project.setNewSynchro(rs.getBoolean("newsynchro"));
            project.setFieldWorkStudy(rs.getInt("fieldworkstudy"));
            project.setBudgetLocation(rs.getInt("budgetlocation"));
            
            if(rs.getLong("endDate")>0)
            {
            	project.setEndDate(new Date(rs.getLong("endDate")));
            }
          //  project.setProjectTrackStatus(rs.getString("projecttrackstatus"));
            return project;
        }
    };

	public SynchroDAOUtil getSynchroDAOUtil() {
		return synchroDAOUtil;
	}

	public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
		this.synchroDAOUtil = synchroDAOUtil;
	}
   
}
