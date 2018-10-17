package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.QPRProjectCostSnapshot;
import com.grail.synchro.beans.QPRProjectSnapshot;
import com.grail.synchro.beans.QPRSnapshot;
import com.grail.synchro.beans.SpendByReportBean;
import com.grail.synchro.beans.SpendReportExtractFilter;
import com.grail.synchro.dao.QPRSnapshotDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.database.dao.DAOException;

/**
 * @author Tejinder
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class QPRSnapshotDAOImpl extends SynchroAbstractDAO implements QPRSnapshotDAO {
    private static final Logger LOG = Logger.getLogger(QPRSnapshotDAOImpl.class);
    
    private SynchroDAOUtil synchroDAOUtil;
  
    private static final String QPR_SNAPSHOT_FIELDS = "snapshotid, spendfor, budgetyear, isfreeze, freezedate, creationby, modificationby, creationdate, modificationdate, openbudgetlocation ";
    
    private static final String QPR_SNAPSHOT_PROJECT_FIELDS = "snapshotid, projectid, projectname, budgetlocation, methodologydetails, brand, brandspecificstudy, studytype, isfreeze, totalcost, totalcostcurrency,creationby, modificationby, creationdate, modificationdate, categorytype, region, area, t2040, methgroup, brandtype ";
    
    private static final String QPR_SNAPSHOT_PROJECT_COST_FIELDS = "snapshotid, projectid, agencyid, costcomponent, costcurrency, estimatedcost, agencytype ";
    
    private static final String INSERT_QPR_SNAPSHOT = "INSERT INTO grailqprsnapshot( " +QPR_SNAPSHOT_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String INSERT_QPR_SNAPSHOT_PROJECT = "INSERT INTO grailqprsnapshotproject( " +QPR_SNAPSHOT_PROJECT_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String INSERT_QPR_SNAPSHOT_PROJECT_COST = "INSERT INTO grailqprsnapshotprojcost( " +QPR_SNAPSHOT_PROJECT_COST_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String GET_ALL_QPR_SNAPSHOTS = "select "+ QPR_SNAPSHOT_FIELDS +" from grailqprsnapshot order by snapshotid";
    
    private static final String GET_QPR_SNAPSHOT_SNAPSHOT_ID = "select "+ QPR_SNAPSHOT_FIELDS +" from grailqprsnapshot where snapshotid=?";
    
    private static final String GET_QPR_SNAPSHOT_PROJECT_SNAPSHOT_ID = "select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=?";
    
    private static final String GET_QPR_SNAPSHOT_PROJECT_SNAPSHOT_ID_PROJECT_ID = "select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=? and projectid = ?";
    
    private static final String GET_QPR_SNAPSHOT_PROJECT_SNAPSHOT_ID_PROJECT_ID_NAME_BL = "select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=? and projectid = ? and projectname=? and budgetlocation=?";
    
    private static final String GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID_PROJECT_ID_NAME_BL = "select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS +" from grailqprsnapshotprojcost where snapshotid=? and projectid in (select projectid from grailqprsnapshotproject where projectid = ? and projectname=? and budgetlocation=? and snapshotid=?)";
    
    private static final String GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID = "select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS  +" from grailqprsnapshotprojcost where snapshotid=?";
    
    private static final String GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID_PROJECT_ID = "select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS  +" from grailqprsnapshotprojcost where snapshotid=? and projectid = ?";
    
    private static final String GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID_PROJECT_ID_AGENCY_ID = "select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS  +" from grailqprsnapshotprojcost where snapshotid=? and projectid = ? and agencyid = ?";
    
    
    private static final String UPDATE_QPR_SNAPSHOT_PROJECT_FREEZE = "UPDATE grailqprsnapshotproject set isfreeze=?, modificationby=?, modificationdate=? " +
    		" where snapshotid=? and projectid=?";
    
    private static final String UPDATE_QPR_SNAPSHOT_PROJECT = "UPDATE grailqprsnapshotproject set projectname=?, budgetlocation=?, methodologydetails=?," +
    		"isfreeze=?, totalcost=?, totalcostcurrency=?, brand=?, brandspecificstudy=?, studytype=?,  modificationby=?, modificationdate=?, categorytype=? " +
    		" where snapshotid=? and projectid=?";
    
    private static final String DELETE_SNAPSHOT = "delete from grailqprsnapshot where snapshotid=?";
    private static final String DELETE_SNAPSHOT_PROJECT = "delete from grailqprsnapshotproject where snapshotid=?";
    private static final String DELETE_SNAPSHOT_PROJECT_COST = "delete from grailqprsnapshotprojcost where snapshotid=?";
    private static final String DELETE_SNAPSHOT_PROJECT_COST_PROJECT_ID = "delete from grailqprsnapshotprojcost where snapshotid=? and projectid=?";
    
    private static final String GET_SNAPSHOT_ID = "select snapshotid from grailqprsnapshot where spendfor=? and budgetyear=?";
    
    private static final String GET_SPEND_BY_ALL_BUDGET_LOCATION = "select budgetlocation, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ? group by budgetlocation ";
    
    private static final String GET_SPEND_BY_BUDGET_LOCATION = "select budgetlocation, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ? and budgetlocation=? group by budgetlocation ";
    
    private static final String GET_SPEND_BY_METHODOLOGY = "select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ? and methodologydetails=? group by methodologydetails ";
    
    private static final String GET_SPEND_BY_BRAND = "select brand, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ? and brand=? and brandspecificstudy=1 group by brand ";
    
    private static final String GET_SPEND_BY_NON_BRAND = "select studytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ? and studytype=? and brandspecificstudy=2 group by studytype ";
    
    private static final String GET_SPEND_BY_AGENCY = "select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS  +" from grailqprsnapshotprojcost where snapshotid=? and agencyid = ?";
    
    private static final String GET_SPEND_BY_AGENCY_TYPE = "select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS  +" from grailqprsnapshotprojcost where snapshotid = ? and agencyid in (select agency.id from grailresearchagency agency, grailresearchagencymapping mapping where agency.id = mapping.researchagencyid and mapping.researchagencygroupid = ? )";
    
    private static final String GET_ALL_BUDGET_LOCATIONS = "SELECT distinct(budgetlocation) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?) ";
    
    private static final String GET_ALL_METHODOLOGIES = "SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?) ";
    
    private static final String GET_ALL_CATEGORIES = "SELECT distinct(categorytype) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?) ";
    
    private static final String GET_ALL_BRANDS = "SELECT distinct(brand) FROM grailqprsnapshotproject where brandspecificstudy = 1 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?)";
    private static final String GET_ALL_NON_BRANDS = "SELECT distinct(studytype) FROM grailqprsnapshotproject where brandspecificstudy = 2 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? )";
    
    private static final String GET_ALL_AGENCIES = "SELECT distinct(agencyid) FROM grailqprsnapshotprojcost where projectid in (select distinct(projectid) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?))";
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveSnapshot(final QPRSnapshot qprSnapshot)
    {
    	 try {
            
            
             if(qprSnapshot.getSnapShotID()!=null && qprSnapshot.getSnapShotID().intValue() >0)
             {
            	 
             }
             else
             {
            	 Long id = synchroDAOUtil.nextSequenceID("snapshotid", "grailqprsnapshot");
                 LOG.info("SNAPShot next sequence - " + id);
                 qprSnapshot.setSnapShotID(id);
             }
            
           
             getSimpleJdbcTemplate().update(INSERT_QPR_SNAPSHOT,
            		 qprSnapshot.getSnapShotID(),
                     qprSnapshot.getSpendFor(),
                     qprSnapshot.getBudgetYear(),
                     qprSnapshot.getIsFreeze()?1:0,
                     qprSnapshot.getFreezeDate().getTime(),
                     qprSnapshot.getCreationBy(),
                     qprSnapshot.getModifiedBy(),
                     qprSnapshot.getCreationDate(),
                     qprSnapshot.getModifiedDate(),
                     null
             );
           

         }
         catch (DataAccessException e) {
             final String message = "Failed to create new snapshot for Spend For ==>"+ qprSnapshot.getSpendFor() +" and Budget Year ==>"+ qprSnapshot.getBudgetYear();
             LOG.error(message, e);
             //throw new DAOException(message, e);
         }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveProjectSnapshot(final QPRProjectSnapshot qprProjectSnapshot)
    {
    	 try {
            
             getSimpleJdbcTemplate().update(INSERT_QPR_SNAPSHOT_PROJECT,
            		 qprProjectSnapshot.getSnapShotID(),
            		 qprProjectSnapshot.getProjectID(),
            		 qprProjectSnapshot.getProjectName(),
            		 qprProjectSnapshot.getBudgetLocation(),
            		 
            		 (qprProjectSnapshot.getMethodologyDetails()!=null&&qprProjectSnapshot.getMethodologyDetails().size()>0 && qprProjectSnapshot.getMethodologyDetails().get(0)!=null)?Joiner.on(",").join(qprProjectSnapshot.getMethodologyDetails()):null,
            		 qprProjectSnapshot.getBrand(),
            		 qprProjectSnapshot.getBrandSpecificStudy(),
            		 qprProjectSnapshot.getBrandSpecificStudyType(),
            		 
            		 qprProjectSnapshot.getIsFreeze()?1:0,
            		 qprProjectSnapshot.getTotalCost(),
            		 qprProjectSnapshot.getTotalCostCurrency(),
            		 qprProjectSnapshot.getCreationBy(),
            		 qprProjectSnapshot.getModifiedBy(),
            		 qprProjectSnapshot.getCreationDate(),
            		 qprProjectSnapshot.getModifiedDate(),
            		 (qprProjectSnapshot.getCategoryType()!=null&&qprProjectSnapshot.getCategoryType().size()>0 && qprProjectSnapshot.getCategoryType().get(0)!=null)?Joiner.on(",").join(qprProjectSnapshot.getCategoryType()):null,
            				 qprProjectSnapshot.getRegion(), 
            				 qprProjectSnapshot.getArea(), 
            				 qprProjectSnapshot.getT20_t40(),
            				 qprProjectSnapshot.getMethGroup(), 
            				 qprProjectSnapshot.getBrandType()
             );
           

         }
         catch (DataAccessException e) {
             final String message = "Failed to create new Project snapshot for Snapshot Id ==>"+ qprProjectSnapshot.getSnapShotID();
             LOG.error(message, e);
             //throw new DAOException(message, e);
         }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveProjectCostSnapshot(final QPRProjectCostSnapshot qprProjectCostSnapshot)
    {
    	 try {
            
             getSimpleJdbcTemplate().update(INSERT_QPR_SNAPSHOT_PROJECT_COST,
            		 qprProjectCostSnapshot.getSnapShotID(),
            		 qprProjectCostSnapshot.getProjectID(),
            		 qprProjectCostSnapshot.getAgencyId(),
            		 qprProjectCostSnapshot.getCostComponent(),
            		 qprProjectCostSnapshot.getCostCurrency(),
            		 qprProjectCostSnapshot.getEstimatedCost(),
            		 qprProjectCostSnapshot.getAgencyType()
             );
           

         }
         catch (DataAccessException e) {
             final String message = "Failed to create new Project Cost snapshot for Snapshot Id ==>"+ qprProjectCostSnapshot.getSnapShotID();
             LOG.error(message, e);
             //throw new DAOException(message, e);
         }
    }
    
    @Override
    public Long getSnapShotId(final Integer freezeSpendFor, final Integer budgetYear) {
    	
    	Long snapshotId = -1L;
        try {
        	
        	snapshotId = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_SNAPSHOT_ID, freezeSpendFor, budgetYear);
        	
        }
        catch (DataAccessException e) {
            final String message = "Failed to load SnapShot Id for freezeSpendFor: " + freezeSpendFor + ", budgetYear: " + budgetYear;
            //LOG.error(message, e);
            LOG.debug(message);
           // //throw new DAOException(message, e);
        }
        return snapshotId;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteSnapshot(final Long snapshotId) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_SNAPSHOT, snapshotId);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the Snanpshot " + snapshotId;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteSnapshotProject(final Long snapshotId) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_SNAPSHOT_PROJECT, snapshotId);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the Project Snanpshot " + snapshotId;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteSnapshotProjectCost(final Long snapshotId) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_SNAPSHOT_PROJECT_COST, snapshotId);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the Project Cost Snanpshot " + snapshotId;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSnapshotFreeze(final QPRProjectSnapshot qprProjectSnapshot)
    {
    	 try {
            
             getSimpleJdbcTemplate().update(UPDATE_QPR_SNAPSHOT_PROJECT_FREEZE,
            		 qprProjectSnapshot.getIsFreeze()?1:0,
            		 qprProjectSnapshot.getModifiedBy(),
            		 qprProjectSnapshot.getModifiedDate(),
            		 qprProjectSnapshot.getSnapShotID(),
            		 qprProjectSnapshot.getProjectID()
             );
         }
         catch (DataAccessException e) {
             final String message = "Failed to update Freeze details for Snapshot Id ==>"+ qprProjectSnapshot.getSnapShotID() +" and Project Id ==>"+  qprProjectSnapshot.getProjectID();
             LOG.error(message, e);
             //throw new DAOException(message, e);
         }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectSnapshot(final QPRProjectSnapshot qprProjectSnapshot)
    {
    	 try {
            
             getSimpleJdbcTemplate().update(UPDATE_QPR_SNAPSHOT_PROJECT,
            		 qprProjectSnapshot.getProjectName(),
            		 qprProjectSnapshot.getBudgetLocation(),
            		 (qprProjectSnapshot.getMethodologyDetails()!=null&&qprProjectSnapshot.getMethodologyDetails().size()>0 && qprProjectSnapshot.getMethodologyDetails().get(0)!=null)?Joiner.on(",").join(qprProjectSnapshot.getMethodologyDetails()):null,
            		 qprProjectSnapshot.getIsFreeze()?1:0,
            		 qprProjectSnapshot.getTotalCost(),
            		 qprProjectSnapshot.getTotalCostCurrency(),
            		 qprProjectSnapshot.getBrand(),
            		 qprProjectSnapshot.getBrandSpecificStudy(),
            		 qprProjectSnapshot.getBrandSpecificStudyType(),
            		 qprProjectSnapshot.getModifiedBy(),
            		 qprProjectSnapshot.getModifiedDate(),
            		 (qprProjectSnapshot.getCategoryType()!=null&&qprProjectSnapshot.getCategoryType().size()>0 && qprProjectSnapshot.getCategoryType().get(0)!=null)?Joiner.on(",").join(qprProjectSnapshot.getCategoryType()):null,
            		 qprProjectSnapshot.getSnapShotID(),
            		 qprProjectSnapshot.getProjectID()
             );
         }
         catch (DataAccessException e) {
             final String message = "Failed to update Freeze details for Snapshot Id ==>"+ qprProjectSnapshot.getSnapShotID() +" and Project Id ==>"+  qprProjectSnapshot.getProjectID();
             LOG.error(message, e);
             //throw new DAOException(message, e);
         }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteProjectCostDetailsSnapshot(final Long snapshotId, final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_SNAPSHOT_PROJECT_COST_PROJECT_ID, snapshotId, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the project Cost Details Snapshot for Snapshot==>"+ snapshotId +" Project --  " + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    }
    @Override
    public List<QPRSnapshot> getAllSnapshots()
    {
    	List<QPRSnapshot> qprSnapshots = Collections.emptyList();
        
    	 try {
	           
    		 	qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL_QPR_SNAPSHOTS, qprSnapshotRowMapper);
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to load All QPR SNAPSHOTS";
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public QPRSnapshot getSnapshot(final Long snapShotId)
    {
    	QPRSnapshot  qprSnapshot =null; 
    	try {
	           
    		qprSnapshot = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_QPR_SNAPSHOT_SNAPSHOT_ID, qprSnapshotRowMapper, snapShotId);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshot;
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectSnapshot(final Long snapShotId)
    {
    	List<QPRProjectSnapshot>  qprSnapshots= null; 
    	try {
	           
    		   qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_QPR_SNAPSHOT_PROJECT_SNAPSHOT_ID, qprProjectSnapshotRowMapper, snapShotId);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectSnapshot(final Long snapShotId, final Long projectId)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_QPR_SNAPSHOT_PROJECT_SNAPSHOT_ID_PROJECT_ID, qprProjectSnapshotRowMapper, snapShotId, projectId);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectSnapshot(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	           
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_QPR_SNAPSHOT_PROJECT_SNAPSHOT_ID_PROJECT_ID_NAME_BL, qprProjectSnapshotRowMapper, snapShotId, projectId, projectName, budgetLocation);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    
    @Override
    public List<QPRProjectCostSnapshot> getProjectAgencySnapshot(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
    	try {
	           
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID_PROJECT_ID_NAME_BL, qprProjectCostSnapshotRowMapper, snapShotId, projectId, projectName, budgetLocation, snapShotId);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getProjectBrandAgencySnapshotCrossTab(final Long snapShotId,final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer brand, String brandType, String categoryType,final Long agencyId, String agencyType)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	if(agencyId!=null)
    	{
	    	sql.append(" and agencyid=");
	    	sql.append(agencyId.toString());
    	}
    	
    	if(agencyType!=null)
    	{
	    	sql.append(" and agencytype='");
	    	sql.append(agencyType);
	    	sql.append("' ");
    	}
    	
    	
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject where snapshotid = ");
	    	sql.append(snapShotId.toString());
			
	    	if(projectId!=null && projectId.intValue() > 0)
	    	{
		    	sql.append(" and projectid = ");
				sql.append(projectId.toString());
	    	}	
			if(projectName!=null)
			{
				sql.append(" and projectname='");
				sql.append(SynchroUtils.removeQuotes(projectName)+"'");
			}
			if(budgetLocation!=null)
			{
				sql.append(" and budgetlocation=");
				sql.append(budgetLocation.toString());
			}
			 
			if(region!=null)
			{
				sql.append(" and region='");
		    	sql.append(region+"'");
			}
			
			if(area!=null)
			{	
		    	sql.append(" and area='");
		    	sql.append(area+"'");
			}
			
			if(t20_40!=null)
			{
		    	sql.append(" and t2040='");
		    	sql.append(t20_40+"'");
			}
	    	
			if(methodology!=null)
			{
		    	sql.append(" and methodologydetails='");
		    	sql.append(methodology.toString()+"'");
			}
			
			if(brand!=null)
			{
		    	sql.append(" and brand=");
		    	sql.append(brand.toString());
		    	sql.append(" and brandspecificstudy=1 ");
		    	
		    	sql.append(" and brandtype='");
		    	sql.append(brandType+"' ");
			}
			
			if(categoryType!=null)
			{
		    	sql.append(" and categoryType='");
		    	sql.append(categoryType.toString()+"'");
			}
			sql.append(")");
    	
    	
    	List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectCostSnapshot>  qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY, qprProjectCostSnapshotRowMapper, snapShotId, agencyId);
    		qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		 return qprCostSnapshots;
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	         //   //throw new DAOException(message, e);
	        }
    	return qprCostSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getProjectNonBrandAgencySnapshotCrossTab(final Long snapShotId,final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType,final Long agencyId, String agencyType)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	if(agencyId!=null)
    	{
	    	sql.append(" and agencyid=");
	    	sql.append(agencyId.toString());
    	}
    	
    	if(agencyType!=null)
    	{
	    	sql.append(" and agencytype='");
	    	sql.append(agencyType);
	    	sql.append("' ");
    	}
    	
    	
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject where snapshotid = ");
	    	sql.append(snapShotId.toString());
	    	
	    	if(projectId!=null && projectId.intValue() > 0)
	    	{
		    	sql.append(" and projectid = ");
				sql.append(projectId.toString());
	    	}
			
			if(projectName!=null)
			{
				sql.append(" and projectname='");
				sql.append(SynchroUtils.removeQuotes(projectName)+"'");
			}
			if(budgetLocation!=null)
			{
				sql.append(" and budgetlocation=");
				sql.append(budgetLocation.toString());
			}
			 
			if(region!=null)
			{
				sql.append(" and region='");
		    	sql.append(region+"'");
			}
			
			if(area!=null)
			{	
		    	sql.append(" and area='");
		    	sql.append(area+"'");
			}
			
			if(t20_40!=null)
			{
		    	sql.append(" and t2040='");
		    	sql.append(t20_40+"'");
			}
	    	
			if(methodology!=null)
			{
		    	sql.append(" and methodologydetails='");
		    	sql.append(methodology.toString()+"'");
			}
			
			if(nonBrand!=null)
			{
		    	sql.append(" and studytype=");
		    	sql.append(nonBrand.toString());
		    	sql.append(" and brandspecificstudy=2 ");
		    	
		    	sql.append(" and brandtype='");
		    	sql.append(brandType+"' ");
			}
			
			if(categoryType!=null)
			{
		    	sql.append(" and categoryType='");
		    	sql.append(categoryType.toString()+"'");
			}
			sql.append(")");
    	
    	List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	try {
	           
    		 //List<QPRProjectCostSnapshot>  qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY, qprProjectCostSnapshotRowMapper, snapShotId, agencyId);
    		 qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	          //  //throw new DAOException(message, e);
	        }
    	 return qprCostSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer brand, String brandType, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder selectFields = new StringBuilder();
    	 	 StringBuilder groupByFields = new StringBuilder();
    	 	 boolean flag = false;
    	 	 
    	 	 if(projectId!=null)
         	 {
         		selectFields.append(" projectID ");
         		groupByFields.append(" projectID");
         		flag = true;
         	 }
    	 	 if(projectName!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",projectname ");
    	     		groupByFields.append(",projectname");
         		}
         		else
         		{
         			selectFields.append(" projectname ");
    	     		groupByFields.append(" projectname");
         		}
         		flag = true;
         	 }
         	 if(budgetLocation!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",budgetlocation ");
    	     		groupByFields.append(",budgetlocation ");
         		}
         		else
         		{
         			selectFields.append(" budgetlocation ");
    	     		groupByFields.append(" budgetlocation ");
         		}
         		flag = true;
         	 }
         	 if(methodology!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",methodologyDetails ");
    	     		groupByFields.append(",methodologyDetails ");
         		}
         		else
         		{
         			selectFields.append(" methodologyDetails ");
    	     		groupByFields.append(" methodologyDetails ");
         		}
         		flag = true;
         	 }
         	
         	 if(brand!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",brand ");
    	     		groupByFields.append(",brand ");
         		}
         		else
         		{
         			selectFields.append(" brand ");
    	     		groupByFields.append(" brand ");
         		}
         		flag = true;
         	 }
         	
         	 if(categoryType!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",categoryType ");
    	     		groupByFields.append(",categoryType ");
         		}
         		else
         		{
         			selectFields.append(" categoryType ");
    	     		groupByFields.append(" categoryType ");
         		}
         		flag = true;
         	 }
          
         	selectFields.append(", sum(totalcost) as totalcost ");
    	 	
    		 StringBuilder sql = new StringBuilder();
    		 //sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append("select "+ selectFields.toString() +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 
    		 if(projectId!=null && projectId.intValue() > 0)
    		 {
	    		 sql.append(" and projectid = ");
	    		 sql.append(projectId.toString());
    		 }
    		 
    		 if(projectName!=null)
    		 {
	    		 sql.append(" and projectname='");
	    		 sql.append(SynchroUtils.removeQuotes(projectName)+"'");
    		 }
    		 
    		 if(budgetLocation!=null)
    		 {
	    		 sql.append(" and budgetlocation=");
	    		 sql.append(budgetLocation.toString());
    		 }
    		 
    		 if(region!=null)
    		 {
	    		 sql.append(" and region='");
		    	 sql.append(region+"'");
    		 }
    		 
    		 if(area!=null)
    		 {
		    	 sql.append(" and area='");
		    	 sql.append(area+"'");
    		 }
    		 
    		 if(t20_40!=null)
    		 {
		    	 sql.append(" and t2040='");
		    	 sql.append(t20_40+"'");
    		 }
	    	
    		 if(methodology!=null)
    		 {
		    	 sql.append(" and methodologydetails='");
		    	 sql.append(methodology.toString()+"'");
    		 }
    		 
    		 if(brand!=null)
    		 {
		    	 sql.append(" and brand=");
		    	 sql.append(brand.toString());
		    	 sql.append(" and brandspecificstudy=1 ");
		    	
		    	 sql.append(" and brandtype='");
		    	 sql.append(brandType+"' ");
    		 }
    		 
    		 if(categoryType!=null)
    		 {
		    	 sql.append(" and categoryType='");
		    	 sql.append(categoryType.toString()+"'");
    		 }
    		 
    		 sql.append(" group by "+ groupByFields.toString());
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByCrossTabSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder selectFields = new StringBuilder();
    	 	 StringBuilder groupByFields = new StringBuilder();
    	 	 boolean flag = false;
    	 	 
    	 	 if(projectId!=null)
         	 {
         		selectFields.append(" projectID ");
         		groupByFields.append(" projectID");
         		flag = true;
         	 }
    	 	 if(projectName!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",projectname ");
    	     		groupByFields.append(",projectname");
         		}
         		else
         		{
         			selectFields.append(" projectname ");
    	     		groupByFields.append(" projectname");
         		}
         		flag = true;
         	 }
         	 if(budgetLocation!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",budgetlocation ");
    	     		groupByFields.append(",budgetlocation ");
         		}
         		else
         		{
         			selectFields.append(" budgetlocation ");
    	     		groupByFields.append(" budgetlocation ");
         		}
         		flag = true;
         	 }
         	 if(methodology!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",methodologyDetails ");
    	     		groupByFields.append(",methodologyDetails ");
         		}
         		else
         		{
         			selectFields.append(" methodologyDetails ");
    	     		groupByFields.append(" methodologyDetails ");
         		}
         		flag = true;
         	 }
         	
         	 if(nonBrand!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",studytype ");
    	     		groupByFields.append(",studytype ");
         		}
         		else
         		{
         			selectFields.append(" studytype ");
    	     		groupByFields.append(" studytype ");
         		}
         		flag = true;
         	 }
         	
         	 if(categoryType!=null)
         	 {
         		if(flag)
         		{
    	     		selectFields.append(",categoryType ");
    	     		groupByFields.append(",categoryType ");
         		}
         		else
         		{
         			selectFields.append(" categoryType ");
    	     		groupByFields.append(" categoryType ");
         		}
         		flag = true;
         	 }
          
         	selectFields.append(", sum(totalcost) as totalcost ");
    		 StringBuilder sql = new StringBuilder();
    		// sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append("select "+ selectFields.toString() +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 
    		 if(projectId!=null && projectId.intValue() >0)
    		 {
	    		 sql.append(" and projectid = ");
	    		 sql.append(projectId.toString());
    		 }
    		 
    		 if(projectName!=null)
    		 {
	    		 sql.append(" and projectname='");
	    		 sql.append(SynchroUtils.removeQuotes(projectName)+"'");
    		 }
    		 
    		 if(budgetLocation!=null)
    		 {
	    		 sql.append(" and budgetlocation=");
	    		 sql.append(budgetLocation.toString());
    		 }
    		 
    		 if(region!=null)
    		 {
	    		 sql.append(" and region='");
		    	 sql.append(region+"'");
    		 }
    		 
    		 if(area!=null)
    		 {
		    	 sql.append(" and area='");
		    	 sql.append(area+"'");
    		 }
    		 
    		 if(t20_40!=null)
    		 {
		    	 sql.append(" and t2040='");
		    	 sql.append(t20_40+"'");
    		 }
	    	
    		 if(methodology!=null)
    		 {
		    	 sql.append(" and methodologydetails='");
		    	 sql.append(methodology.toString()+"'");
    		 }
    		 
    		 if(nonBrand!=null)
    		 {
		    	 sql.append(" and studytype=");
		    	 sql.append(nonBrand.toString());
		    	 sql.append(" and brandspecificstudy=2 ");
		    	
		    	 sql.append(" and brandtype='");
		    	 sql.append(brandType+"' ");
    		 }
    		 
    		 if(categoryType!=null)
    		 {
		    	 sql.append(" and categoryType='");
		    	 sql.append(categoryType.toString()+"'");
    		 }
    		 
    		 sql.append(" group by "+ groupByFields.toString());
    		 
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByCrossTabSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer brand, String brandType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	 sql.append(" and methodologydetails='");
	    	 sql.append(methodology.toString()+"'");
	    	 sql.append(" and brand=");
	    	 sql.append(brand.toString());
	    	 sql.append(" and brandspecificstudy=1 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer nonBrand, String brandType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	 sql.append(" and methodologydetails='");
	    	 sql.append(methodology.toString()+"'");
	    	 sql.append(" and studytype=");
	    	 sql.append(nonBrand.toString());
	    	 sql.append(" and brandspecificstudy=2 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	 sql.append(" and methodologydetails='");
	    	 sql.append(methodology.toString()+"'");
	    	 
	    	
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, Integer brand, String brandType, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	
	    	 sql.append(" and brand=");
	    	 sql.append(brand.toString());
	    	 sql.append(" and brandspecificstudy=1 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, Integer nonBrand, String brandType, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	
	    	 sql.append(" and studytype=");
	    	 sql.append(nonBrand.toString());
	    	 sql.append(" and brandspecificstudy=2 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	 sql.append(" and methodologydetails='");
	    	 sql.append(methodology.toString()+"'");
	    	
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String categoryType)
    {
    	 List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40,  Integer brand, String brandType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	
	    	 sql.append(" and brand=");
	    	 sql.append(brand.toString());
	    	 sql.append(" and brandspecificstudy=1 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40,Integer nonBrand, String brandType)
    {
    	 List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	
	    	 sql.append(" and studytype=");
	    	 sql.append(nonBrand.toString());
	    	 sql.append(" and brandspecificstudy=2 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String methodology, String methGroup, Integer brand, String brandType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		
	    	
	    	 sql.append(" and methodologydetails='");
	    	 sql.append(methodology.toString()+"'");
	    	 sql.append(" and brand=");
	    	 sql.append(brand.toString());
	    	 sql.append(" and brandspecificstudy=1 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String methodology, String methGroup, Integer nonBrand, String brandType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 
	    	
	    	 sql.append(" and methodologydetails='");
	    	 sql.append(methodology.toString()+"'");
	    	 sql.append(" and studytype=");
	    	 sql.append(nonBrand.toString());
	    	 sql.append(" and brandspecificstudy=2 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String methodology, String methGroup, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 
	    	 sql.append(" and methodologydetails='");
	    	 sql.append(methodology.toString()+"'");
	    	 
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName,  Integer brand, String brandType, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		
	    	 sql.append(" and brand=");
	    	 sql.append(brand.toString());
	    	 sql.append(" and brandspecificstudy=1 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer nonBrand, String brandType, String categoryType)
    {
    	 List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 
	    	 sql.append(" and studytype=");
	    	 sql.append(nonBrand.toString());
	    	 sql.append(" and brandspecificstudy=2 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		 sql.append(" and budgetlocation=");
    		 sql.append(budgetLocation.toString());
    		 
    		 sql.append(" and region='");
	    	 sql.append(region+"'");
	    	 sql.append(" and area='");
	    	 sql.append(area+"'");
	    	 sql.append(" and t2040='");
	    	 sql.append(t20_40+"'");
	    	
	    	 
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer brand, String brandType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		
	    	 sql.append(" and brand=");
	    	 sql.append(brand.toString());
	    	 sql.append(" and brandspecificstudy=1 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer nonBrand, String brandType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		
	    	 sql.append(" and studytype=");
	    	 sql.append(nonBrand.toString());
	    	 sql.append(" and brandspecificstudy=2 ");
	    	
	    	 sql.append(" and brandtype='");
	    	 sql.append(brandType+"' ");
	    	
	    	
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String categoryType)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	         
    		 StringBuilder sql = new StringBuilder();
    		 sql.append("select "+ QPR_SNAPSHOT_PROJECT_FIELDS +" from grailqprsnapshotproject where snapshotid=");
    		 sql.append(snapShotId.toString());
    		 sql.append(" and projectid = ");
    		 sql.append(projectId.toString());
    		 sql.append(" and projectname='");
    		 sql.append(projectName+"'");
    		
	    	 sql.append(" and categoryType='");
	    	 sql.append(categoryType.toString()+"'");
    		 
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    @Override
    public List<QPRProjectSnapshot> getSpendByBudgetLocationSnapshot(final Long snapShotId)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null; 
    	try {
	           
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_ALL_BUDGET_LOCATION, spendByBudgetLocationSnapshotRowMapper, snapShotId);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByBudgetLocationSnapshot(final Long snapShotId,final Integer budgetLocation, SpendReportExtractFilter spendReportFilter)
    {
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation ");
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BUDGET_LOCATION, spendByBudgetLocationSnapshotRowMapper, snapShotId, budgetLocation);
    		
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBudgetLocationSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getBudgetLocationAgencySnapshot(final Long snapShotId,final Integer budgetLocation, String region, String area, String t20_40, SpendReportExtractFilter spendReportFilter)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS +" from grailqprsnapshotprojcost where snapshotid=? and projectid in (select projectid from grailqprsnapshotproject where budgetlocation=? and snapshotid=? and region = ? and area =? and t2040 =? ");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(")");
    	
    	try {
	           
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper, snapShotId,  budgetLocation, snapShotId, region, area, t20_40);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    
    @Override
    public List<QPRProjectSnapshot> getSpendByBudgetLocationSnapshot(final Long snapShotId,final Integer budgetLocation, String region, String area, String t20_40, SpendReportExtractFilter spendReportFilter)
    {
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and region='");
    	sql.append(region+"'");
    	sql.append(" and area='");
    	sql.append(area+"'");
    	sql.append(" and t2040='");
    	sql.append(t20_40+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	
    	sql.append(" group by budgetlocation ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BUDGET_LOCATION, spendByBudgetLocationSnapshotRowMapper, snapShotId, budgetLocation);
    		
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBudgetLocationSnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    private String applySpendReportFilter(SpendReportExtractFilter spendReportFilter)
    {
    	StringBuilder sql = new StringBuilder();
    	
    	// Access Mechanism for Spend Reports Filter
    	if(SynchroPermHelper.isGlobalUserType())
    	{
    		
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		sql.append(" AND budgetlocation in (");
    		
    		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
    		regionBudgetLocations.addAll(regionEndMarketLocations);
    		
    		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
    		if(SynchroPermHelper.isEndMarketUserType())
    		{
    			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    			regionBudgetLocations.addAll(emBudgetLocations);
    		}
    		
    		sql.append(StringUtils.join(regionBudgetLocations, ","));
    		sql.append(")");
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		sql.append(" AND budgetlocation in (");
    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    		sql.append(StringUtils.join(emBudgetLocations, ","));
    		sql.append(")");
    	} 
    	
    	
    	//Methodology Details Filter
        if(spendReportFilter.getMethDetails() != null && spendReportFilter.getMethDetails().size()>0 && !isListNull(spendReportFilter.getMethDetails())) {
            StringBuilder methodologyDetailsCondition = new StringBuilder();
            methodologyDetailsCondition.append(" AND (methodologydetails in ('")
                    .append(spendReportFilter.getMethDetails().get(0)+"").append("')");
            if(spendReportFilter.getMethDetails().size()>0)
            {
         	   methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(0)+"").append("')");
         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,") .append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<spendReportFilter.getMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or methodologydetails in ('").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
	               	}
            }
            methodologyDetailsCondition.append(")");
            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
            sql.append(methodologyDetailsCondition.toString());
            
        }
      
        
      //Brand Filter
        if(spendReportFilter.getBrands() != null && spendReportFilter.getBrands().size()>0 && !isListNull(spendReportFilter.getBrands())) {
            StringBuilder brandsCondition = new StringBuilder();
            
            Set<Integer> allBrands = new HashSet<Integer>();
        	Set<Integer> allNonBrands = new HashSet<Integer>();
            for(Long brand : spendReportFilter.getBrands())
        	{
        		/*allBrands.add(brand.intValue());
        		allNonBrands.add(brand.intValue());*/
        		if(brand.intValue()==SynchroConstants.MULTI_BRAND_UI_VALUE)
        		{
        			allNonBrands.add(SynchroConstants.MULTI_BRAND_DB_VALUE);
        		}
        		else if(brand.intValue()==SynchroConstants.NON_BRAND_UI_VALUE)
        		{
        			allNonBrands.add(SynchroConstants.NON_BRAND_DB_VALUE);
        		}
        		else
        		{
        			allBrands.add(brand.intValue());
        		}
        	}
            
            if(allBrands.size()>0 && allNonBrands.size()>0)
            {
	            brandsCondition.append(" AND (brand in (");
	            brandsCondition.append(StringUtils.join(allBrands,","));
	            brandsCondition.append(")");
	            brandsCondition.append(" OR studytype in (");
	            brandsCondition.append(StringUtils.join(allNonBrands,","));
	            brandsCondition.append("))");
            }
            else if(allBrands.size()>0)
            {
            	brandsCondition.append(" AND (brand in (");
	            brandsCondition.append(StringUtils.join(allBrands,","));
	            brandsCondition.append("))");
            }
            else if(allNonBrands.size()>0)
            {
            	brandsCondition.append(" AND (studytype in (");
	            brandsCondition.append(StringUtils.join(allNonBrands,","));
	            brandsCondition.append("))");
            }
            sql.append(brandsCondition.toString());
            
        }
        
        //Budget Location Filter
        if(spendReportFilter.getBudgetLocations() != null && spendReportFilter.getBudgetLocations().size()>0 && !isListNull(spendReportFilter.getBudgetLocations())) {
            StringBuilder budgetLocationsCondition = new StringBuilder();
            budgetLocationsCondition.append(" AND budgetlocation in (");
            budgetLocationsCondition.append(StringUtils.join(spendReportFilter.getBudgetLocations(),","));
            budgetLocationsCondition.append(")");
            sql.append(budgetLocationsCondition.toString());
            
        }
        return sql.toString();
    }
    
    private String applySpendReportAgencyFilter(SpendReportExtractFilter spendReportFilter)
    {
    	StringBuilder sql = new StringBuilder();
    	boolean flag = false;
    	if((spendReportFilter.getMethDetails() != null && spendReportFilter.getMethDetails().size()>0 && !isListNull(spendReportFilter.getMethDetails())) 
        		|| (spendReportFilter.getBrands() != null && spendReportFilter.getBrands().size()>0 && !isListNull(spendReportFilter.getBrands())) 
        		|| (spendReportFilter.getBudgetLocations() != null && spendReportFilter.getBudgetLocations().size()>0 && !isListNull(spendReportFilter.getBudgetLocations())))
    	{
    	
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject where ");
	    	
    		//Methodology Details Filter
	        if(spendReportFilter.getMethDetails() != null && spendReportFilter.getMethDetails().size()>0 && !isListNull(spendReportFilter.getMethDetails())) {
	            flag = true;
	        	StringBuilder methodologyDetailsCondition = new StringBuilder();
	            methodologyDetailsCondition.append("  (methodologydetails in ('")
	                    .append(spendReportFilter.getMethDetails().get(0)+"").append("')");
	            if(spendReportFilter.getMethDetails().size()>0)
	            
	         	   methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
	         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(0)+"").append("')");
	         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,") .append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
		               	for(int i=1;i<spendReportFilter.getMethDetails().size();i++)
		               	{
		               		methodologyDetailsCondition.append(" or methodologydetails in ('").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
		               		methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
		               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
		               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
		               	}
	           
	            methodologyDetailsCondition.append(")");
	            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
	            sql.append(methodologyDetailsCondition.toString());
	            
	        }
	      
	        
	      //Brand Filter
	        if(spendReportFilter.getBrands() != null && spendReportFilter.getBrands().size()>0 && !isListNull(spendReportFilter.getBrands())) {
	            StringBuilder brandsCondition = new StringBuilder();
	            if(flag)
	            {
	            	//brandsCondition.append(" AND (brand in (");
	            }
	            else
	            {
	            	//brandsCondition.append(" (brand in (");
	            	flag = true;	
	            }
	            
	            
	            Set<Integer> allBrands = new HashSet<Integer>();
	        	Set<Integer> allNonBrands = new HashSet<Integer>();
	            for(Long brand : spendReportFilter.getBrands())
	        	{
	        		/*allBrands.add(brand.intValue());
	        		allNonBrands.add(brand.intValue());*/
	        		if(brand.intValue()==SynchroConstants.MULTI_BRAND_UI_VALUE)
	        		{
	        			allNonBrands.add(SynchroConstants.MULTI_BRAND_DB_VALUE);
	        		}
	        		else if(brand.intValue()==SynchroConstants.NON_BRAND_UI_VALUE)
	        		{
	        			allNonBrands.add(SynchroConstants.NON_BRAND_DB_VALUE);
	        		}
	        		else
	        		{
	        			allBrands.add(brand.intValue());
	        		}
	        	}
	            
	            
	            if(allBrands.size()>0 && allNonBrands.size()>0)
	            {
		           
		            if(flag)
		            {
		            	brandsCondition.append(" AND (brand in (");
		            }
		            else
		            {
		            	brandsCondition.append(" (brand in (");
		            	flag = true;	
		            }
		            brandsCondition.append(StringUtils.join(allBrands,","));
		            brandsCondition.append(")");
		            brandsCondition.append(" OR studytype in (");
		            brandsCondition.append(StringUtils.join(allNonBrands,","));
		            brandsCondition.append("))");
	            }
	            else if(allBrands.size()>0)
	            {

		            if(flag)
		            {
		            	brandsCondition.append(" AND (brand in (");
		            }
		            else
		            {
		            	brandsCondition.append(" (brand in (");
		            	flag = true;	
		            }
		            brandsCondition.append(StringUtils.join(allBrands,","));
		            brandsCondition.append("))");
	            }
	            else if(allNonBrands.size()>0)
	            {
	            	

		            if(flag)
		            {
		            	brandsCondition.append(" AND (studytype in (");
		            }
		            else
		            {
		            	brandsCondition.append(" (studytype in (");
		            	flag = true;	
		            }
		            brandsCondition.append(StringUtils.join(allNonBrands,","));
		            brandsCondition.append("))");
	            }
	            
	           /* brandsCondition.append(StringUtils.join(allBrands,","));
	            brandsCondition.append(")");
	            brandsCondition.append(" OR studytype in (");
	            brandsCondition.append(StringUtils.join(allNonBrands,","));
	            brandsCondition.append("))");*/
	            sql.append(brandsCondition.toString());
	            
	        }
	        
	        //Budget Location Filter
	        if(spendReportFilter.getBudgetLocations() != null && spendReportFilter.getBudgetLocations().size()>0 && !isListNull(spendReportFilter.getBudgetLocations())) {
	            StringBuilder budgetLocationsCondition = new StringBuilder();
	            if(flag)
	            {
	            	 budgetLocationsCondition.append(" AND budgetlocation in (");
	            }
	            else
	            {
	            	 budgetLocationsCondition.append(" budgetlocation in (");
	            	 flag=true;
	            }
	            
	           // budgetLocationsCondition.append(" AND budgetlocation in (");
	            budgetLocationsCondition.append(StringUtils.join(spendReportFilter.getBudgetLocations(),","));
	            budgetLocationsCondition.append(")");
	            sql.append(budgetLocationsCondition.toString());
	            
	        }
	        sql.append(")");
    	}
    
		// Access Mechanism for Spend Reports Filter
    	if(SynchroPermHelper.isGlobalUserType())
    	{
    		
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		sql.append(" and projectid in (select projectid from grailqprsnapshotproject where ");
    		sql.append(" budgetlocation in (");
    		
    		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
    		regionBudgetLocations.addAll(regionEndMarketLocations);
    		
    		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
    		if(SynchroPermHelper.isEndMarketUserType())
    		{
    			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    			regionBudgetLocations.addAll(emBudgetLocations);
    		}
    		
    		sql.append(StringUtils.join(regionBudgetLocations, ","));
    		sql.append("))");
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		sql.append(" and projectid in (select projectid from grailqprsnapshotproject where ");
    		sql.append(" budgetlocation in (");
    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    		sql.append(StringUtils.join(emBudgetLocations, ","));
    		sql.append("))");
    	} 
	
        return sql.toString();
    }
    
    private String applySpendReportAgencyFilter(SpendReportExtractFilter spendReportFilter, List<Long> selectedQPRs)
    {
    	StringBuilder sql = new StringBuilder();
    	boolean flag = false;
    	if((spendReportFilter.getMethDetails() != null && spendReportFilter.getMethDetails().size()>0 && !isListNull(spendReportFilter.getMethDetails())) 
        		|| (spendReportFilter.getBrands() != null && spendReportFilter.getBrands().size()>0 && !isListNull(spendReportFilter.getBrands())) 
        		|| (spendReportFilter.getBudgetLocations() != null && spendReportFilter.getBudgetLocations().size()>0 && !isListNull(spendReportFilter.getBudgetLocations())))
    	{
    	
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject where snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")  ");
	    	flag = true;
    		//Methodology Details Filter
	        if(spendReportFilter.getMethDetails() != null && spendReportFilter.getMethDetails().size()>0 && !isListNull(spendReportFilter.getMethDetails())) {
	            flag = true;
	        	StringBuilder methodologyDetailsCondition = new StringBuilder();
	            methodologyDetailsCondition.append(" and (methodologydetails in ('")
	                    .append(spendReportFilter.getMethDetails().get(0)+"").append("')");
	            if(spendReportFilter.getMethDetails().size()>0)
	            
	         	   methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
	         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(0)+"").append("')");
	         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,") .append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
		               	for(int i=1;i<spendReportFilter.getMethDetails().size();i++)
		               	{
		               		methodologyDetailsCondition.append(" or methodologydetails in ('").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
		               		methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
		               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
		               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
		               	}
	           
	            methodologyDetailsCondition.append(")");
	            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
	            sql.append(methodologyDetailsCondition.toString());
	            
	        }
	      
	        
	      //Brand Filter
	        if(spendReportFilter.getBrands() != null && spendReportFilter.getBrands().size()>0 && !isListNull(spendReportFilter.getBrands())) {
	            StringBuilder brandsCondition = new StringBuilder();
	            if(flag)
	            {
	            	//brandsCondition.append(" AND (brand in (");
	            }
	            else
	            {
	            	//brandsCondition.append(" AND (brand in (");
	            	flag = true;	
	            }
	            
	            Set<Integer> allBrands = new HashSet<Integer>();
	        	Set<Integer> allNonBrands = new HashSet<Integer>();
	            for(Long brand : spendReportFilter.getBrands())
	        	{
	        		/*allBrands.add(brand.intValue());
	        		allNonBrands.add(brand.intValue());*/
	        		if(brand.intValue()==SynchroConstants.MULTI_BRAND_UI_VALUE)
	        		{
	        			allNonBrands.add(SynchroConstants.MULTI_BRAND_DB_VALUE);
	        		}
	        		else if(brand.intValue()==SynchroConstants.NON_BRAND_UI_VALUE)
	        		{
	        			allNonBrands.add(SynchroConstants.NON_BRAND_DB_VALUE);
	        		}
	        		else
	        		{
	        			allBrands.add(brand.intValue());
	        		}
	        	}
	            
	          /*  brandsCondition.append(StringUtils.join(allBrands,","));
	            brandsCondition.append(")");
	            brandsCondition.append(" OR studytype in (");
	            brandsCondition.append(StringUtils.join(allNonBrands,","));
	            brandsCondition.append("))");
	            */
	            if(allBrands.size()>0 && allNonBrands.size()>0)
	            {
		           
		            if(flag)
		            {
		            	brandsCondition.append(" AND (brand in (");
		            }
		            else
		            {
		            	brandsCondition.append(" (brand in (");
		            	flag = true;	
		            }
		            brandsCondition.append(StringUtils.join(allBrands,","));
		            brandsCondition.append(")");
		            brandsCondition.append(" OR studytype in (");
		            brandsCondition.append(StringUtils.join(allNonBrands,","));
		            brandsCondition.append("))");
	            }
	            else if(allBrands.size()>0)
	            {

		            if(flag)
		            {
		            	brandsCondition.append(" AND (brand in (");
		            }
		            else
		            {
		            	brandsCondition.append(" (brand in (");
		            	flag = true;	
		            }
		            brandsCondition.append(StringUtils.join(allBrands,","));
		            brandsCondition.append("))");
	            }
	            else if(allNonBrands.size()>0)
	            {
	            	

		            if(flag)
		            {
		            	brandsCondition.append(" AND (studytype in (");
		            }
		            else
		            {
		            	brandsCondition.append(" (studytype in (");
		            	flag = true;	
		            }
		            brandsCondition.append(StringUtils.join(allNonBrands,","));
		            brandsCondition.append("))");
	            }
	            sql.append(brandsCondition.toString());
	            
	        }
	        
	        //Budget Location Filter
	        if(spendReportFilter.getBudgetLocations() != null && spendReportFilter.getBudgetLocations().size()>0 && !isListNull(spendReportFilter.getBudgetLocations())) {
	            StringBuilder budgetLocationsCondition = new StringBuilder();
	            if(flag)
	            {
	            	 budgetLocationsCondition.append(" AND budgetlocation in (");
	            }
	            else
	            {
	            	 budgetLocationsCondition.append(" AND budgetlocation in (");
	            	 flag=true;
	            }
	            
	           // budgetLocationsCondition.append(" AND budgetlocation in (");
	            budgetLocationsCondition.append(StringUtils.join(spendReportFilter.getBudgetLocations(),","));
	            budgetLocationsCondition.append(")");
	            sql.append(budgetLocationsCondition.toString());
	            
	        }
	        sql.append(")");
    	}
    
		// Access Mechanism for Spend Reports Filter
    	if(SynchroPermHelper.isGlobalUserType())
    	{
    		
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		sql.append(" and projectid in (select projectid from grailqprsnapshotproject where snapshotid in ("+StringUtils.join(selectedQPRs, ",")+") and ");
    		sql.append(" budgetlocation in (");
    		
    		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
    		regionBudgetLocations.addAll(regionEndMarketLocations);
    		
    		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
    		if(SynchroPermHelper.isEndMarketUserType())
    		{
    			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    			regionBudgetLocations.addAll(emBudgetLocations);
    		}
    		
    		sql.append(StringUtils.join(regionBudgetLocations, ","));
    		sql.append("))");
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		sql.append(" and projectid in (select projectid from grailqprsnapshotproject where snapshotid in ("+StringUtils.join(selectedQPRs, ",")+") and ");
    		sql.append(" budgetlocation in (");
    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    		sql.append(StringUtils.join(emBudgetLocations, ","));
    		sql.append("))");
    	} 
	
        return sql.toString();
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
    
    @Override
    public List<QPRProjectSnapshot> getSpendByMethodolgies(final Long snapShotId,final String methodology, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots  = null;
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get Methodolgies for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByMethodolgies(final Long snapShotId,final String methodology, String methodologyGroup, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and methgroup='");
    	sql.append(methodologyGroup+"'");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get Methodolgies for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	
    	 return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getMethodologyAgencySnapshot(final Long snapShotId,final String methodology, String methodologyGroup, SpendReportExtractFilter spendReportFilter)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS +" from grailqprsnapshotprojcost where snapshotid=? and projectid in (select projectid from grailqprsnapshotproject where methodologydetails=? and snapshotid=? and methgroup = ?  ");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(")");
    	
    	try {
	           
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper, snapShotId,  methodology, snapShotId, methodologyGroup);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCategoryTypes(final Long snapShotId,final String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select categoryType, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by categoryType ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get Category Types for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	
    	 return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getCategoryTypeAgencySnapshot(final Long snapShotId,final String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS +" from grailqprsnapshotprojcost where snapshotid=? and projectid in (select projectid from grailqprsnapshotproject where categoryType=? and snapshotid=?  ");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(")");
    	
    	try {
	           
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper, snapShotId,  categoryType, snapShotId);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByBrandSnapshot(final Long snapShotId,final Integer brand, SpendReportExtractFilter spendReportFilter)
    {
    	StringBuilder sql = new StringBuilder();
    	sql.append("select brand, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by brand ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		// List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BRAND, spendByBrandSnapshotRowMapper, snapShotId, brand);
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBrandSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByBrandSnapshot(final Long snapShotId,final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter)
    {
    	StringBuilder sql = new StringBuilder();
    	sql.append("select brand, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	sql.append(" and brandtype='");
    	sql.append(brandType+"' ");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by brand ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		// List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BRAND, spendByBrandSnapshotRowMapper, snapShotId, brand);
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBrandSnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    	
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getBrandAgencySnapshot(final Long snapShotId,final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS +" from grailqprsnapshotprojcost where snapshotid=? and projectid in (select projectid from grailqprsnapshotproject where brand=? and snapshotid=? and brandspecificstudy=1 and brandtype = ?  ");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(")");
    	
    	try {
	           
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper, snapShotId,  brand, snapShotId, brandType);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    @Override
    public List<QPRProjectSnapshot> getSpendByNonBrandSnapshot(final Long snapShotId,final Integer nonBrand, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select studytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by studytype ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		// List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_NON_BRAND, spendByNonBrandSnapshotRowMapper, snapShotId, nonBrand);
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByNonBrandSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
  
    
    @Override
    public List<QPRProjectSnapshot> getSpendByNonBrandSnapshot(final Long snapShotId,final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select studytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	
    	sql.append(" and brandtype='");
    	sql.append(brandType+"' ");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by studytype ");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		// List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_NON_BRAND, spendByNonBrandSnapshotRowMapper, snapShotId, nonBrand);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByNonBrandSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getNonBrandAgencySnapshot(final Long snapShotId,final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select "+ QPR_SNAPSHOT_PROJECT_COST_FIELDS +" from grailqprsnapshotprojcost where snapshotid=? and projectid in (select projectid from grailqprsnapshotproject where studytype=? and snapshotid=? and brandspecificstudy=2 and brandtype = ?  ");
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(")");
    	
    	try {
	           
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper, snapShotId,  nonBrand, snapShotId, brandType);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getSpendByAgencySnapshot(final Long snapShotId,final Long agencyId, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and agencyid=");
    	sql.append(agencyId.toString());
    	sql.append(applySpendReportAgencyFilter(spendReportFilter));
    	
    	List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectCostSnapshot>  qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY, qprProjectCostSnapshotRowMapper, snapShotId, agencyId);
    		 qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprCostSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getSpendByAgencyTypeSnapshot(final Long snapShotId,final Integer agencyType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and agencyid in (select agency.id from grailresearchagency agency, grailresearchagencymapping mapping where agency.id = mapping.researchagencyid and mapping.researchagencygroupid = ");
    	sql.append(agencyType.toString()+")");
    	//sql.append(applySpendReportAgencyFilter(spendReportFilter));
    	List<Long> snapShotIdList = new ArrayList<Long>();
    	snapShotIdList.add(snapShotId);
    	
    	sql.append(applySpendReportAgencyFilter(spendReportFilter,snapShotIdList));
    	
    	 List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	try {
	           
    		// List<QPRProjectCostSnapshot>  qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_TYPE, qprProjectCostSnapshotRowMapper, snapShotId, agencyType);
    		 qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprCostSnapshots;
    	
    }
    
    
    
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, brand, categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,brand, categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBrandCategoryTypeCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabNonBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonbrand, String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, studytype, categoryType, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonbrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,studytype,categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByNonBrandCategoryTypeCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get Cross Tab Non Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
   
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, brand, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,brand");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBrandCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
   
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabNonBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonbrand, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, studytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonbrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,studytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByNonBrandCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get Cross Tab Non Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    
 
   
    
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLMeth(final Long snapShotId,final Integer budgetLocation, final String methodology, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	 
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLMeth(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and region='");
    	sql.append(region+"'");
    	sql.append(" and area='");
    	sql.append(area+"'");
    	sql.append(" and t2040='");
    	sql.append(t20_40+"'");
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLMethBrand(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, brand, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and region='");
    	sql.append(region+"'");
    	sql.append(" and area='");
    	sql.append(area+"'");
    	sql.append(" and t2040='");
    	sql.append(t20_40+"'");
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	
    	sql.append(" and brandtype='");
    	sql.append(brandType+"' ");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,brand");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBrandCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLMethBrandCT(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer brand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, brand, categoryType, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and region='");
    	sql.append(region+"'");
    	sql.append(" and area='");
    	sql.append(area+"'");
    	sql.append(" and t2040='");
    	sql.append(t20_40+"'");
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	
    	sql.append(" and brandtype='");
    	sql.append(brandType+"' ");
    	
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,brand, categoryType");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByBrandCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLMethNonBrand(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, studytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and region='");
    	sql.append(region+"'");
    	sql.append(" and area='");
    	sql.append(area+"'");
    	sql.append(" and t2040='");
    	sql.append(t20_40+"'");
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	
    	sql.append(" and brandtype='");
    	sql.append(brandType+"' ");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,studytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots  = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByNonBrandCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Non Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLMethNonBrandCT(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer nonBrand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,methodologydetails, studytype, categoryType, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and region='");
    	sql.append(region+"'");
    	sql.append(" and area='");
    	sql.append(area+"'");
    	sql.append(" and t2040='");
    	sql.append(t20_40+"'");
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	
    	sql.append(" and brandtype='");
    	sql.append(brandType+"' ");
    	
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails,studytype, categoryType");
    	
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByNonBrandCrossTabRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Non Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLCT(final Long snapShotId,final Integer budgetLocation, final String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select budgetlocation,categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBLMethCT(final Long snapShotId,final Integer budgetLocation, final String methodology, final String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation,methodologydetails, categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation,methodologydetails, categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabMethCT(final Long snapShotId, final String categoryType, final String methodology,  SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails, categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabMethBrand(final Long snapShotId, final String methodology, final Integer brand, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select methodologydetails, brand, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails,brand");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    

    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabMethNonBrand(final Long snapShotId, final String methodology, final Integer nonBrand, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select methodologydetails, studytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails,studytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabMethBrandCT(final Long snapShotId, final String methodology, final Integer brand, String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select methodologydetails, brand, categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails,brand, categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    

    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabMethNonBrandCT(final Long snapShotId, final String methodology, final Integer nonBrand,String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select methodologydetails, studytype, categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails,studytype, categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabBrandCT(final Long snapShotId, final Integer brand, String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select brand, categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy=1 ");
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by brand, categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    

    @Override
    public List<QPRProjectSnapshot> getSpendByCrossTabNonBrandCT(final Long snapShotId, final Integer nonBrand,String categoryType, SpendReportExtractFilter spendReportFilter)
    {
    	
    	StringBuilder sql = new StringBuilder();
    	//sql.append("select methodologydetails, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append("select studytype, categorytype, sum(totalcost) as totalcost from grailqprsnapshotproject where snapshotid = ");
    	sql.append(snapShotId.toString());
    	
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ");
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by studytype, categorytype");
    	
    	List<QPRProjectSnapshot>  qprSnapshots = null;
    	
    	try {
	           
    		 //List<QPRProjectSnapshot>  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY, spendByMethodologySnapshotRowMapper, snapShotId, methodology);
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), spendByMethodologySnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    
    @Override
    public List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, final Long agencyId, SpendReportExtractFilter spendReportFilter)
    {
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and agencyid=");
    	sql.append(agencyId.toString());
    	
    	boolean flag = false;
    	if(budgetLocation!=null || methodology!=null || brand!=null)
    	{
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject ");
	    	
	    	
	    	if(budgetLocation!=null)
	    	{
		    	sql.append(" where budgetlocation= ");
		    	sql.append(budgetLocation.toString());
		    	flag=true;
	    	}
	    	
	    	if(methodology!=null)
	    	{
		    	if(flag)
		    	{
		    		sql.append(" and methodologydetails='");
		    	}
		    	else
		    	{
		    		sql.append(" where methodologydetails='");
		    		flag=true;
		    	}
		    	sql.append(methodology.toString()+"'");
	    	}
	    	
	    	if(brand!=null)
	    	{
		    	
	    		if(flag)
		    	{
		    		sql.append("  and brand=");
		    	}
		    	else
		    	{
		    		sql.append(" where brand=");
		    	}
	    		
		    	sql.append(brand.toString());
		    	sql.append(" and brandspecificstudy=1 ");
	    	}
	    	sql.append(")");
    	}
    	sql.append(applySpendReportAgencyFilter(spendReportFilter));
    	
    	List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	try {
	           
    		
    		 qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprCostSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyNonBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonBrand, final Long agencyId, SpendReportExtractFilter spendReportFilter)
    {
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and agencyid=");
    	sql.append(agencyId.toString());
    	
    	
    	boolean flag = false;
    	if(budgetLocation!=null || methodology!=null || nonBrand!=null)
    	{
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject ");
	    	
	    	
	    	if(budgetLocation!=null)
	    	{
		    	sql.append(" where budgetlocation= ");
		    	sql.append(budgetLocation.toString());
		    	flag=true;
	    	}
	    	
	    	if(methodology!=null)
	    	{
		    	if(flag)
		    	{
		    		sql.append(" and methodologydetails='");
		    	}
		    	else
		    	{
		    		sql.append(" where methodologydetails='");
		    		flag=true;
		    	}
		    	sql.append(methodology.toString()+"'");
	    	}
	    	
	    	if(nonBrand!=null)
	    	{
		    	
	    		if(flag)
		    	{
		    		sql.append("  and studytype=");
		    	}
		    	else
		    	{
		    		sql.append(" where studytype=");
		    	}
	    		
		    	sql.append(nonBrand.toString());
		    	sql.append(" and brandspecificstudy=2 ");
	    	}
	    	sql.append(")");
    	}
    	
    	
    	/*sql.append(" and projectid in (select projectid from grailqprsnapshotproject where budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ) ");*/
    	sql.append(applySpendReportAgencyFilter(spendReportFilter));
    	
    	List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	
    	try {
	           
    		  qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprCostSnapshots;
    }
    
    
    @Override
    public List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyBrandCT(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, String categoryType, final Long agencyId, SpendReportExtractFilter spendReportFilter)
    {
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and agencyid=");
    	sql.append(agencyId.toString());
    	
    	boolean flag = false;
    	if(budgetLocation!=null || methodology!=null || brand!=null || categoryType!=null)
    	{
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject ");
	    	
	    	
	    	if(budgetLocation!=null)
	    	{
		    	sql.append(" where budgetlocation= ");
		    	sql.append(budgetLocation.toString());
		    	flag=true;
	    	}
	    	
	    	if(methodology!=null)
	    	{
		    	if(flag)
		    	{
		    		sql.append(" and methodologydetails='");
		    	}
		    	else
		    	{
		    		sql.append(" where methodologydetails='");
		    		flag=true;
		    	}
		    	sql.append(methodology.toString()+"'");
	    	}
	    	
	    	if(brand!=null)
	    	{
		    	
	    		if(flag)
		    	{
		    		sql.append("  and brand=");
		    	}
		    	else
		    	{
		    		sql.append(" where brand=");
		    		flag=true;
		    	}
	    		
		    	sql.append(brand.toString());
		    	sql.append(" and brandspecificstudy=1 ");
	    	}
	    	
	    	if(categoryType!=null)
	    	{
		    	if(flag)
		    	{
		    		sql.append(" and categorytype='");
		    	}
		    	else
		    	{
		    		sql.append(" where categorytype='");
		    		flag=true;
		    	}
		    	sql.append(categoryType.toString()+"'");
	    	}
	    	
	    	sql.append(")");
    	}
    	sql.append(applySpendReportAgencyFilter(spendReportFilter));
    	
    	List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	
    	try {
	           
    		
    		 qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprCostSnapshots;
    }
    
    
    @Override
    public List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyNonBrandCT(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonBrand, final String categoryType, final Long agencyId, SpendReportExtractFilter spendReportFilter)
    {
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select ").append(QPR_SNAPSHOT_PROJECT_COST_FIELDS).append(" from grailqprsnapshotprojcost where snapshotid = ");
    	sql.append(snapShotId.toString());
    	sql.append(" and agencyid=");
    	sql.append(agencyId.toString());
    	
    	
    	boolean flag = false;
    	if(budgetLocation!=null || methodology!=null || nonBrand!=null)
    	{
	    	sql.append(" and projectid in (select projectid from grailqprsnapshotproject ");
	    	
	    	
	    	if(budgetLocation!=null)
	    	{
		    	sql.append(" where budgetlocation= ");
		    	sql.append(budgetLocation.toString());
		    	flag=true;
	    	}
	    	
	    	if(methodology!=null)
	    	{
		    	if(flag)
		    	{
		    		sql.append(" and methodologydetails='");
		    	}
		    	else
		    	{
		    		sql.append(" where methodologydetails='");
		    		flag=true;
		    	}
		    	sql.append(methodology.toString()+"'");
	    	}
	    	
	    	if(nonBrand!=null)
	    	{
		    	
	    		if(flag)
		    	{
		    		sql.append("  and studytype=");
		    	}
		    	else
		    	{
		    		sql.append(" where studytype=");
		    		flag=true;
		    	}
	    		
		    	sql.append(nonBrand.toString());
		    	sql.append(" and brandspecificstudy=2 ");
	    	}
	    	
	    	if(categoryType!=null)
	    	{
		    	if(flag)
		    	{
		    		sql.append(" and categoryType='");
		    	}
		    	else
		    	{
		    		sql.append(" where categoryType='");
		    		flag=true;
		    	}
		    	sql.append(categoryType.toString()+"'");
	    	}
	    	
	    	sql.append(")");
    	}
    	
    	
    	/*sql.append(" and projectid in (select projectid from grailqprsnapshotproject where budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy=2 ) ");*/
    	sql.append(applySpendReportAgencyFilter(spendReportFilter));
    	
    	List<QPRProjectCostSnapshot>  qprCostSnapshots = null;
    	
    	try {
	           
    		 qprCostSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectCostSnapshotRowMapper);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  get CrossTab Brand for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprCostSnapshots;
    	
    }
    
    public List<Long> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter){
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Long> projects = Collections.emptyList();
        
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct(projectid) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(")");
        sql.append(applySpendReportFilter(spendReportFilter));
          
        try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(BUDGET_YEAR_PROJECTS_QPR, projectRowMapper, budgetYear);
            projects = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(),  Long.class);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
          //  //throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Long> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter, List<Long> selectedQPRs){
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Long> projects = Collections.emptyList();
        
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct(projectid) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))");
        sql.append(applySpendReportFilter(spendReportFilter));
          
        try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(BUDGET_YEAR_PROJECTS_QPR, projectRowMapper, budgetYear);
            projects = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(),  Long.class);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, List<Long> projectIds, List<Long> selectedQPRs)
    {
    	 List<Project> projects = Collections.emptyList();
        
         StringBuilder sql = new StringBuilder();
         sql.append("SELECT projectid, projectname, budgetlocation, methodologydetails, brand, brandspecificstudy, studytype, categorytype, region, area, t2040, methgroup, brandtype,  totalcost, totalcostcurrency FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))").append(" and projectid in (").append(StringUtils.join(projectIds, ",")).append(")");
         
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(BUDGET_YEAR_PROJECTS_QPR, projectRowMapper, budgetYear);
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprProjectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects ";
            LOG.error(message, e);
          //  //throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Integer> getBudgetLocationsForQPRSnapshot(final Integer budgetYear){
        List<Integer> budgetLocations = Collections.emptyList();
        try {
        	
        	StringBuilder sql = new StringBuilder(GET_ALL_BUDGET_LOCATIONS);
        	
        	
        	// This is done as part of http://redmine.nvish.com/redmine/issues/515. 
        	//Only the budget location the user has access can have spend by result. Rest budget locations should not even appear in the spend by report.
        	// Access Mechanism for Spend Reports Filter
        	if(SynchroPermHelper.isGlobalUserType())
        	{
        		
        	}
        	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
        	else if(SynchroPermHelper.isRegionalUserType())
        	{
        		sql.append(" AND budgetlocation in (");
        		
        		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
        		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
        		regionBudgetLocations.addAll(regionEndMarketLocations);
        		
        		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
        		if(SynchroPermHelper.isEndMarketUserType())
        		{
        			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
        			regionBudgetLocations.addAll(emBudgetLocations);
        		}
        		
        		sql.append(StringUtils.join(regionBudgetLocations, ","));
        		sql.append(")");
        		
        	}
        	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
        	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
        	else if(SynchroPermHelper.isEndMarketUserType())
        	{
        		sql.append(" AND budgetlocation in (");
        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
        		sql.append(StringUtils.join(emBudgetLocations, ","));
        		sql.append(")");
        	} 
        	
        	budgetLocations = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), Integer.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot budget Locations for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return budgetLocations;
    }
    
    public List<Integer> getBudgetLocationsForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter){
        List<Integer> budgetLocations = Collections.emptyList();
        try {
        	
        	StringBuilder sql = new StringBuilder("SELECT distinct(budgetlocation) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
        	
        	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) ");
        	sql.append(applySpendReportFilter(spendReportFilter));
        	
        	// This is done as part of http://redmine.nvish.com/redmine/issues/515. 
        	//Only the budget location the user has access can have spend by result. Rest budget locations should not even appear in the spend by report.
        	// Access Mechanism for Spend Reports Filter
        	if(SynchroPermHelper.isGlobalUserType())
        	{
        		
        	}
        	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
        	else if(SynchroPermHelper.isRegionalUserType())
        	{
        		sql.append(" AND budgetlocation in (");
        		
        		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
        		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
        		regionBudgetLocations.addAll(regionEndMarketLocations);
        		
        		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
        		if(SynchroPermHelper.isEndMarketUserType())
        		{
        			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
        			regionBudgetLocations.addAll(emBudgetLocations);
        		}
        		
        		sql.append(StringUtils.join(regionBudgetLocations, ","));
        		sql.append(")");
        		
        	}
        	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
        	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
        	else if(SynchroPermHelper.isEndMarketUserType())
        	{
        		sql.append(" AND budgetlocation in (");
        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
        		sql.append(StringUtils.join(emBudgetLocations, ","));
        		sql.append(")");
        	} 
        	
        	budgetLocations = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), Integer.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot budget Locations for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return budgetLocations;
    }
    
    
    public List<SpendByReportBean> getBudgetLocationsForQPRSnapshot(final Integer budgetYear, Set<Integer> budgetLocations, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter){
    	List<SpendByReportBean> budgetLocationsBean = Collections.emptyList();
        try {
        	
        	  StringBuilder sql = new StringBuilder();
              sql.append("SELECT  budgetlocation, region, area, t2040 FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))").append(" and budgetlocation in (").append(StringUtils.join(budgetLocations, ",")).append(") ");
            
              sql.append(applySpendReportFilter(spendReportFilter));
              budgetLocationsBean = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprBudgetLocationRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot budget Locations for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return budgetLocationsBean;
    }
    
    public List<String> getMethodologiesForQPRSnapshot(final Integer budgetYear){
        List<String> methodologies = Collections.emptyList();
        List<Integer> budgetLocations = getBudgetLocationsForQPRSnapshot(budgetYear);
        
        try {
        	if(budgetLocations!=null && budgetLocations.size()>0)
        	{
        		String sql = GET_ALL_METHODOLOGIES +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        	}
        	else
        	{
        		methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_ALL_METHODOLOGIES, String.class, budgetYear);
        	}
        	
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Methodologies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
        return methodologies;
    }
    
    public List<String> getMethodologiesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter){
        List<String> methodologies = Collections.emptyList();
        List<Integer> budgetLocations = getBudgetLocationsForQPRSnapshot(budgetYear);
        
        try {
        	if(budgetLocations!=null && budgetLocations.size()>0)
        	{
        		//String sql = GET_ALL_METHODOLOGIES +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		
        		StringBuilder sql = new StringBuilder("SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
        		//SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?)
            	
            	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))");
            	sql.append(" AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ") ");
            	sql.append(applySpendReportFilter(spendReportFilter));
            	
        		methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), String.class, budgetYear);
        	}
        	else
        	{
        		StringBuilder sql = new StringBuilder("SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
        		//SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?)
            	
            	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) ");
            	sql.append(applySpendReportFilter(spendReportFilter));
        		
        		methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), String.class, budgetYear);
        	}
        	
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Methodologies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return methodologies;
    }
    
    public List<SpendByReportBean> getMethodologiesForQPRSnapshot(final Integer budgetYear, List<String> methodologies, List<Long> selectedQPRs){
    	List<SpendByReportBean> methodologiesBean = Collections.emptyList();
        try {
        	
        	  StringBuilder sql = new StringBuilder();
              //sql.append("SELECT  methodologydetails, methgroup FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))").append(" and methodologydetails in ('").append(StringUtils.join(methodologies, ",")).append("')");
        	  
        	  sql.append("SELECT  methodologydetails, methgroup FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))");
            
              StringBuilder methodologyDetailsCondition = new StringBuilder(); 
              methodologyDetailsCondition.append(" AND (methodologydetails in ('")
              .append(methodologies.get(0)+"").append("')");
		      if(methodologies.size()>0)
		      {
		   	   methodologyDetailsCondition.append(" or methodologydetails like ('").append(methodologies.get(0)+"").append(",%')");
		   	   methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(methodologies.get(0)+"").append("')");
		   	   methodologyDetailsCondition.append(" or methodologydetails like ('%,") .append(methodologies.get(0)+"").append(",%')");
		             	for(int i=1;i<methodologies.size();i++)
		             	{
		             		methodologyDetailsCondition.append(" or methodologydetails in ('").append(methodologies.get(i)+"").append("')");
		             		methodologyDetailsCondition.append(" or methodologydetails like ('").append(methodologies.get(i)+"").append(",%')");
		             		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(methodologies.get(i)+"").append("')");
		             		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(methodologies.get(i)+"").append(",%')");
		             	}
		      }
		      methodologyDetailsCondition.append(")");
		      sql.append(methodologyDetailsCondition.toString());
		      
              methodologiesBean = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprMethodologyRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all  QPR SnapShot Methodologies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
          //  //throw new DAOException(message, e);
        }
        return methodologiesBean;
    }
    
    public List<String> getCategoryTypesForQPRSnapshot(final Integer budgetYear){
       
    	List<String> categories = Collections.emptyList();
        List<Integer> budgetLocations = getBudgetLocationsForQPRSnapshot(budgetYear);
        
        try {
        	if(budgetLocations!=null && budgetLocations.size()>0)
        	{
        		String sql = GET_ALL_CATEGORIES +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		categories = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        	}
        	else
        	{
        		categories = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_ALL_CATEGORIES, String.class, budgetYear);
        	}
        	
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Category Types for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return categories;
    }
    
    public List<String> getCategoryTypesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter){
        
    	List<String> categories = Collections.emptyList();
        List<Integer> budgetLocations = getBudgetLocationsForQPRSnapshot(budgetYear);
        
        try {
        	if(budgetLocations!=null && budgetLocations.size()>0)
        	{
        		String sql = "SELECT distinct(categorytype) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) ";
        		
        		sql = sql +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ") ";
        		sql = sql + applySpendReportFilter(spendReportFilter);
        		categories = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        	}
        	else
        	{
        		String sql = "SELECT distinct(categorytype) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) ";
        		sql = sql + applySpendReportFilter(spendReportFilter);
        		categories = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        	}
        	
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Category Types for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            ////throw new DAOException(message, e);
        }
        return categories;
    }
    
    public List<Integer> getBrandsForQPRSnapshot(final Integer budgetYear){
        List<Integer> brands = Collections.emptyList();
        List<Integer> budgetLocations = getBudgetLocationsForQPRSnapshot(budgetYear);
        try {
        	
        	if(budgetLocations!=null && budgetLocations.size()>0)
        	{
        		String sql = GET_ALL_BRANDS +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		
        		brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        	}
        	else
        	{
        		brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_ALL_BRANDS, Integer.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return brands;
    }
    
    public List<Integer> getBrandsForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter){
        List<Integer> brands = Collections.emptyList();
        List<Integer> budgetLocations = getBudgetLocationsForQPRSnapshot(budgetYear);
        try {
        	
        	if(budgetLocations!=null && budgetLocations.size()>0)
        	{
        		//String sql = GET_ALL_BRANDS +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		
        		
        		StringBuilder sql = new StringBuilder("SELECT distinct(brand) FROM grailqprsnapshotproject where brandspecificstudy = 1 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
        		//SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?)
            	
            	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))");
            	sql.append(" AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ") and brand not in (-1) ");
            	sql.append(applySpendReportFilter(spendReportFilter));
        		
        	
        		brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), Integer.class, budgetYear);
        	}
        	else
        	{
        		//brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_ALL_BRANDS, Integer.class, budgetYear);
        		
        		StringBuilder sql = new StringBuilder("SELECT distinct(brand) FROM grailqprsnapshotproject where brandspecificstudy = 1 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
        		//SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?)
            	
            	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and brand not in (-1) ");
            	sql.append(applySpendReportFilter(spendReportFilter));
            	
            	brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), Integer.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            ////throw new DAOException(message, e);
        }
        return brands;
    }
    
    public List<SpendByReportBean> getBrandsForQPRSnapshot(final Integer budgetYear, Set<Integer> brands, List<Long> selectedQPRs){
    	List<SpendByReportBean> brandsBean = Collections.emptyList();
        try {
        	
        	  StringBuilder sql = new StringBuilder();
              sql.append("SELECT  brand, brandspecificstudy, studytype, brandtype FROM grailqprsnapshotproject where brandspecificstudy = 1 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))").append(" and brand in (").append(StringUtils.join(brands, ",")).append(")");
            
             
              brandsBean = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprBrandRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
           // //throw new DAOException(message, e);
        }
        return brandsBean;
    }
    
    public List<Integer> getNonBrandsForQPRSnapshot(final Integer budgetYear){
        List<Integer> nonBrands = Collections.emptyList();
        try {
        	nonBrands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_ALL_NON_BRANDS, Integer.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Non brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            ////throw new DAOException(message, e);
        }
        return nonBrands;
    }
    
    public List<Integer> getNonBrandsForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter){
        List<Integer> nonBrands = Collections.emptyList();
        List<Integer> budgetLocations = getBudgetLocationsForQPRSnapshot(budgetYear);
        
        try {
        	
        	
        /*	StringBuilder sql = new StringBuilder("SELECT distinct(studytype) FROM grailqprsnapshotproject where brandspecificstudy = 2 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
    		
        	
        	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and studytype not in (-1) ");
        	nonBrands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), Integer.class, budgetYear);
        	*/
        	
        	if(budgetLocations!=null && budgetLocations.size()>0)
        	{
        		//String sql = GET_ALL_BRANDS +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		
        		
        		StringBuilder sql = new StringBuilder("SELECT distinct(studytype) FROM grailqprsnapshotproject where brandspecificstudy = 2 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
        		//SELECT distinct(methodologydetails) FROM grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ?)
            	
            	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and studytype not in (-1) ");
            	sql.append(" AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ") and studytype not in (-1) ");
            	sql.append(applySpendReportFilter(spendReportFilter));
        	
            	nonBrands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), Integer.class, budgetYear);
        	}
        	else
        	{
        		StringBuilder sql = new StringBuilder("SELECT distinct(studytype) FROM grailqprsnapshotproject where brandspecificstudy = 2 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? ");
        		
            	
            	sql.append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and studytype not in (-1) ");
            	sql.append(applySpendReportFilter(spendReportFilter));
            	
            	nonBrands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(), Integer.class, budgetYear);
        	}
        	
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Non brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            ////throw new DAOException(message, e);
        }
        return nonBrands;
    }
    
    public List<SpendByReportBean> getNonBrandsForQPRSnapshot(final Integer budgetYear, Set<Integer> nonBrands, List<Long> selectedQPRs){
    	List<SpendByReportBean> brandsBean = Collections.emptyList();
        try {
        	
        	  StringBuilder sql = new StringBuilder();
              sql.append("SELECT  brand, brandspecificstudy, studytype, brandtype FROM grailqprsnapshotproject where brandspecificstudy = 2 and snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ").append(budgetYear.toString()).append(" and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+"))").append(" and studytype in (").append(StringUtils.join(nonBrands, ",")).append(")");
            
             
              brandsBean = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), qprBrandRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            ////throw new DAOException(message, e);
        }
        return brandsBean;
    }
    
    public List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear){
        List<Long> agencies = Collections.emptyList();
        try {
        	agencies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_ALL_AGENCIES, Long.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Agencies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
        return agencies;
    }
    
    public List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs){
        List<Long> agencies = Collections.emptyList();
        
        String sql = "SELECT distinct(agencyid) FROM grailqprsnapshotprojcost where projectid in (select distinct(projectid) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")";
        try {
        	agencies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Agencies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
        return agencies;
    }
    
    public List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter){
        List<Long> agencies = Collections.emptyList();
        
       // String filterQuery = applySpendReportAgencyFilter(spendReportFilter, selectedQPRs);
        String sql ="";
        
        sql = "SELECT distinct(agencyid) FROM grailqprsnapshotprojcost where projectid in (select distinct(projectid) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? and  snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) "+ applySpendReportAgencyFilter(spendReportFilter, selectedQPRs) +") and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+") ";
        
        LOG.info("QUERY ==>"+ sql);
        
        try {
        	agencies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Agencies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
        return agencies;
    }
    // This method is used for Spend By Report for CrossTab
    public List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, Long projectId){
        List<Long> agencies = Collections.emptyList();
        
        String sql = "SELECT distinct(agencyid) FROM grailqprsnapshotprojcost where projectid in (select distinct(projectid) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+") and projectid = "+projectId;
        try {
        	agencies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Agencies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
        return agencies;
    }
    
 // This method is used for Spend By Report for CrossTab
    public List<QPRProjectCostSnapshot> getAgenciesAgencyTypeForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, Long projectId){
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
        
        String sql = "SELECT agencyid, agencytype FROM grailqprsnapshotprojcost where projectid in (select distinct(projectid) from grailqprsnapshotproject where snapshotid in (select snapshotid from grailqprsnapshot where budgetyear = ? and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+")) and snapshotid in ("+StringUtils.join(selectedQPRs, ",")+") and projectid = "+projectId;
        try {
        	qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(sql, crossTabQprProjectCostSnapshotRowMapper, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Distinct QPR SnapShot Agencies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
        return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null; 
    	try {
	           
    		  qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID, qprProjectCostSnapshotRowMapper, snapShotId);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId, final Long projectId)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots  = null; 
    	try {
	           
    		   qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID_PROJECT_ID, qprProjectCostSnapshotRowMapper, snapShotId, projectId);
    		
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId +" and ProjectId ==>"+ projectId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return qprSnapshots;
    }
    
    @Override
    public List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId, final Long projectId, final Long agencyId)
    {
    	List<QPRProjectCostSnapshot>  qprSnapshots = null;  
    	try {
	           
    		 qprSnapshots = getSimpleJdbcTemplate().getJdbcOperations().query(GET_QPR_SNAPSHOT_PROJECT_COST_SNAPSHOT_ID_PROJECT_ID_AGENCY_ID, qprProjectCostSnapshotRowMapper, snapShotId, projectId, agencyId);
    		 
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to  QPR Project Cost SNAPSHOT for SnapShotId ==>"+ snapShotId +" and ProjectId ==>"+ projectId  +" and agencyId ==>"+ agencyId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	return qprSnapshots;
    	
    }
    
    @Override
    public List<Long> getBudgetYear(final Long snapShotId){
        List<Long> budgetYear = Collections.emptyList();
        try {
        	String sql = "select budgetyear from grailqprsnapshot where snapshotid =?";
        	budgetYear = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, snapShotId);
        }
        catch (DataAccessException e) {
            final String message = "Failed to get Budget Year for Snapshot Id==> "+ snapShotId;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
        return budgetYear;
    }
    
    @Override
    public List<Long> getAllOpenProjectIds(final Long snapShotId)
    {
    	List<Long> projectIds = Collections.emptyList();
        
    	 try {
	           
    		 	String sql = "select projectid from grailqprsnapshotproject where isfreeze=0 and snapshotid=?";
    		 	projectIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql,
	                    Long.class, snapShotId);
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to load All Open Project Ids for SNAPSHOT ==> "+ snapShotId;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return projectIds;
    }
    
    @Override
    public List<Long> getAllOpenProjectIds(final Long snapShotId, final Long budgetLocation)
    {
    	List<Long> projectIds = Collections.emptyList();
        
    	 try {
	           
    		 	String sql = "select projectid from grailqprsnapshotproject where snapshotid=? and budgetlocation=? ";
    		 	projectIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql,
	                    Long.class, snapShotId, budgetLocation);
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to load All Open Project Ids for SNAPSHOT ==> "+ snapShotId +" and BudgetLocation ==>"+ budgetLocation;
	            LOG.error(message, e);
	            //throw new DAOException(message, e);
	        }
    	 return projectIds;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateOpenBudgetLocation(final Long snapShotId, final Long budgetLocation)
    {
    	 
    	String existingBudgetLocation = new String("");
    	
        
   	 	try
   	 	{
	           
   		 	String sql = "select openbudgetlocation from grailqprsnapshot where snapshotid=?";
   		 	existingBudgetLocation = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql,String.class, snapShotId);
	    }
	    catch (DataAccessException e) 
	    {
            final String message = "Failed to load  Open Budget Location for SNAPSHOT ==> "+ snapShotId;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
   	 
		 if(StringUtils.isNotBlank(existingBudgetLocation))
		 {
			existingBudgetLocation = existingBudgetLocation + "," + budgetLocation.toString();
		 }
		 else
		 {
			existingBudgetLocation = budgetLocation.toString();
		 }
			
		try 
		{
			String sql = "UPDATE grailqprsnapshot set openbudgetlocation=?  where snapshotid=?  ";
			getSimpleJdbcTemplate().update(sql,	existingBudgetLocation, snapShotId);
		}
	    catch (DataAccessException e) 
	    {
	         final String message = "Failed to update openbudgetlocation for Snapshot Id ==>"+ snapShotId ;
	         LOG.error(message, e);
	         //throw new DAOException(message, e);
	    }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateFreezeBudgetLocation(final Long snapShotId)
    {
    	try 
		{
			String sql = "UPDATE grailqprsnapshot set openbudgetlocation=?  where snapshotid=?  ";
			getSimpleJdbcTemplate().update(sql,	null, snapShotId);
		}
	    catch (DataAccessException e) 
	    {
	         final String message = "Failed to update openbudgetlocation for Snapshot Id ==>"+ snapShotId ;
	         LOG.error(message, e);
	         //throw new DAOException(message, e);
	    }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateFreezeBudgetLocation(final Long snapShotId, final String budgetLocation)
    {
    	 
    	String existingBudgetLocation = new String("");
    	
   	 	try
   	 	{
	           
   		 	String sql = "select openbudgetlocation from grailqprsnapshot where snapshotid=?";
   		 	existingBudgetLocation = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql,String.class, snapShotId);
	    }
	    catch (DataAccessException e) 
	    {
            final String message = "Failed to load  Open Budget Location for SNAPSHOT ==> "+ snapShotId;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
   	 
   	 	String updateBL = "";
   	 	
		 if(StringUtils.isNotBlank(existingBudgetLocation))
		 {
			if(existingBudgetLocation.contains(","))
			{
				String[] splitBL = existingBudgetLocation.split(",");
				List<String> updatedBL = new ArrayList<String>();
				
				for(int i=0;i<splitBL.length;i++)
				{
					if(splitBL[i].equalsIgnoreCase(budgetLocation))
					{
						
					}
					else
					{
						updatedBL.add(splitBL[i]);
					}
				}
				updateBL = Joiner.on(",").join(updatedBL);
			}
			else
			{
				updateBL=existingBudgetLocation.replace(budgetLocation, "");
			}
		 }
		 else
		 {
			//existingBudgetLocation = budgetLocation.toString();
		 }
			
		try 
		{
			String sql = "UPDATE grailqprsnapshot set openbudgetlocation=?  where snapshotid=?  ";
			getSimpleJdbcTemplate().update(sql,	updateBL, snapShotId);
		}
	    catch (DataAccessException e) 
	    {
	         final String message = "Failed to update openbudgetlocation for Snapshot Id ==>"+ snapShotId ;
	         LOG.error(message, e);
	         //throw new DAOException(message, e);
	    }
    }
    private final ParameterizedRowMapper<QPRSnapshot> qprSnapshotRowMapper = new ParameterizedRowMapper<QPRSnapshot>() {

        public QPRSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRSnapshot qprSnapshot = new QPRSnapshot();
            qprSnapshot.setSnapShotID(rs.getLong("snapshotid"));
            qprSnapshot.setSpendFor(rs.getInt("spendfor"));
            qprSnapshot.setBudgetYear(rs.getInt("budgetyear"));
            qprSnapshot.setIsFreeze(rs.getBoolean("isfreeze"));
            
            if(rs.getLong("freezedate")>0)
            {
            	qprSnapshot.setFreezeDate(new Date(rs.getLong("freezedate")));
            }
            
            qprSnapshot.setCreationBy(rs.getLong("creationby"));
            qprSnapshot.setCreationDate(rs.getLong("creationdate"));
            qprSnapshot.setModifiedBy(rs.getLong("modificationby"));
            qprSnapshot.setModifiedDate(rs.getLong("modificationdate"));
            
            String budgetLocations = rs.getString("openbudgetlocation");
            
            if(StringUtils.isNotEmpty(budgetLocations))
            {
            	if(budgetLocations.contains(","))
            	{
            		String[] splitBL = budgetLocations.split(",");
            		List<String> blNames = new ArrayList<String>();
            		List<Long> bLoc = new ArrayList<Long>();
            		
            		for(int i=0; i<splitBL.length;i++)
            		{
            			blNames.add(SynchroUtils.getBudgetLocationName(new Integer(splitBL[i])));
            			bLoc.add(new Long(splitBL[i]));
            		}
            		
            		qprSnapshot.setBudgetLocations(Joiner.on(",").join(blNames));
            		qprSnapshot.setOpenBudgetLocations(bLoc);
            		qprSnapshot.setOpenBudgetLocationIds(Joiner.on(",").join(bLoc));
            	}
            	else
            	{
            		List<Long> bLoc = new ArrayList<Long>();
            		bLoc.add(new Long(budgetLocations));
            		String bLocName = SynchroUtils.getBudgetLocationName(new Integer(budgetLocations));
            		qprSnapshot.setBudgetLocations(bLocName);
            		qprSnapshot.setOpenBudgetLocations(bLoc);
            		qprSnapshot.setOpenBudgetLocationIds(Joiner.on(",").join(bLoc));
            	}
            }
           
            return qprSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> qprProjectSnapshotRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	qprProjectSnapshot.setSnapShotID(rs.getLong("snapshotid"));
        	qprProjectSnapshot.setProjectID(rs.getLong("projectid"));
        	qprProjectSnapshot.setProjectName(rs.getString("projectname"));
        	qprProjectSnapshot.setBudgetLocation(rs.getInt("budgetlocation"));
        	qprProjectSnapshot.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
        	//qprProjectSnapshot.setBrandOrNonBrand(rs.getInt("isbrand"));
        	
        	qprProjectSnapshot.setBrand(rs.getLong("brand"));
        	
        	qprProjectSnapshot.setBrandSpecificStudy(rs.getInt("brandspecificstudy"));
        	qprProjectSnapshot.setBrandSpecificStudyType(rs.getInt("studytype"));
        	
        	qprProjectSnapshot.setIsFreeze(rs.getBoolean("isfreeze"));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalCost"));
        	qprProjectSnapshot.setTotalCostCurrency(rs.getLong("totalCostCurrency"));
        	qprProjectSnapshot.setCreationBy(rs.getLong("creationby"));
        	qprProjectSnapshot.setCreationDate(rs.getLong("creationdate"));
        	qprProjectSnapshot.setModifiedBy(rs.getLong("modificationby"));
        	qprProjectSnapshot.setModifiedDate(rs.getLong("modificationdate"));
        	
        	qprProjectSnapshot.setCategoryType(synchroDAOUtil.getIDs(rs.getString("categorytype")));
        	
        	qprProjectSnapshot.setRegion(rs.getString("region"));
        	qprProjectSnapshot.setArea(rs.getString("area"));
        	qprProjectSnapshot.setT20_t40(rs.getString("t2040"));
        	qprProjectSnapshot.setMethGroup(rs.getString("methgroup"));
        	qprProjectSnapshot.setBrandType(rs.getString("brandtype"));
        	
        	
            
            return qprProjectSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<Project> qprProjectRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
        	Project project = new Project();
        	
        	project.setProjectID(rs.getLong("projectid"));
        	project.setName(rs.getString("projectname"));
        	project.setBudgetLocation(rs.getInt("budgetlocation"));
        	project.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
        	//qprProjectSnapshot.setBrandOrNonBrand(rs.getInt("isbrand"));
        	
        	project.setBrandSpecificStudy(rs.getInt("brandspecificstudy"));
        	
        	if(project.getBrandSpecificStudy()!=null && project.getBrandSpecificStudy().intValue()==1)
        	{
        		project.setBrand(rs.getLong("brand"));
        		project.setBrandSpecificStudyType(-1);
        	}
        	if(project.getBrandSpecificStudy()!=null && project.getBrandSpecificStudy().intValue()==2)
        	{
        		project.setBrandSpecificStudyType(rs.getInt("studytype"));
        		project.setBrand(new Long("-1"));
        	}
        	
        	
        	
        	
        	
        	
        	project.setCategoryType(synchroDAOUtil.getIDs(rs.getString("categorytype")));
        	project.setTotalCost(rs.getBigDecimal("totalCost"));
            project.setTotalCostCurrency(rs.getLong("totalCostCurrency"));
            
            project.setRegion(rs.getString("region"));
            project.setArea(rs.getString("area"));
            project.setT20_40(rs.getString("t2040"));
            project.setMethGroup(rs.getString("methgroup"));
            project.setBrandType(rs.getString("brandtype"));
            project.setMethodologies(rs.getString("methodologyDetails"));
            project.setCategories(rs.getString("categorytype"));
            
            
            return project;
        }
    };
    
    private final ParameterizedRowMapper<SpendByReportBean> qprBudgetLocationRowMapper = new ParameterizedRowMapper<SpendByReportBean>() {

        public SpendByReportBean mapRow(ResultSet rs, int row) throws SQLException {
        	SpendByReportBean spendByBean = new SpendByReportBean();
        	
        	spendByBean.setBudgetLocation(rs.getInt("budgetlocation"));
        	spendByBean.setRegion(rs.getString("region"));
        	spendByBean.setArea(rs.getString("area"));
        	spendByBean.setT20_40(rs.getString("t2040"));
        	
        	
            return spendByBean;
        }
    };
    
    private final ParameterizedRowMapper<SpendByReportBean> qprBrandRowMapper = new ParameterizedRowMapper<SpendByReportBean>() {

        public SpendByReportBean mapRow(ResultSet rs, int row) throws SQLException {
        	SpendByReportBean spendByBean = new SpendByReportBean();
        	
        	spendByBean.setBrand(rs.getInt("brand"));
        	spendByBean.setBrandType(rs.getString("brandtype"));
        	spendByBean.setStudyType(rs.getInt("studytype"));
        	
        	
        	
            return spendByBean;
        }
    };
    
    private final ParameterizedRowMapper<SpendByReportBean> qprMethodologyRowMapper = new ParameterizedRowMapper<SpendByReportBean>() {

        public SpendByReportBean mapRow(ResultSet rs, int row) throws SQLException {
        	SpendByReportBean spendByBean = new SpendByReportBean();
        	
        	spendByBean.setMethodologies(rs.getString("methodologydetails"));
        	spendByBean.setMethGroup(rs.getString("methgroup"));
        	
        	
            return spendByBean;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByBudgetLocationSnapshotRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setBudgetLocation(rs.getInt("budgetlocation"));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByCrossTabSnapshotRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByBrandSnapshotRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setBrand(rs.getLong("brand"));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByNonBrandSnapshotRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setBrandSpecificStudyType(rs.getInt("studytype"));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByMethodologySnapshotRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	//qprProjectSnapshot.setBudgetLocation(rs.getInt("budgetlocation"));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };

    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByBrandCategoryTypeCrossTabRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setBudgetLocation(rs.getInt("budgetlocation"));
        	qprProjectSnapshot.setBrand(rs.getLong("brand"));
        	qprProjectSnapshot.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
        	//qprProjectSnapshot.setCategoryType(rs.getString("categorytype"));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByNonBrandCategoryTypeCrossTabRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setBudgetLocation(rs.getInt("budgetlocation"));
        	qprProjectSnapshot.setBrandSpecificStudyType(rs.getInt("studytype"));
        //	qprProjectSnapshot.setCategoryType(rs.getString("categorytype"));
        	qprProjectSnapshot.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByBrandCrossTabRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setBudgetLocation(rs.getInt("budgetlocation"));
        	qprProjectSnapshot.setBrand(rs.getLong("brand"));
        	qprProjectSnapshot.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
        
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    
    private final ParameterizedRowMapper<QPRProjectSnapshot> spendByNonBrandCrossTabRowMapper = new ParameterizedRowMapper<QPRProjectSnapshot>() {

        public QPRProjectSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectSnapshot qprProjectSnapshot = new QPRProjectSnapshot();
        	
        	qprProjectSnapshot.setBudgetLocation(rs.getInt("budgetlocation"));
        	qprProjectSnapshot.setBrandSpecificStudyType(rs.getInt("studytype"));
        
        	qprProjectSnapshot.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
        	qprProjectSnapshot.setTotalCost(rs.getBigDecimal("totalcost"));
        	
            
            return qprProjectSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectCostSnapshot> qprProjectCostSnapshotRowMapper = new ParameterizedRowMapper<QPRProjectCostSnapshot>() {

        public QPRProjectCostSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectCostSnapshot qprProjectCostSnapshot = new QPRProjectCostSnapshot();
        	qprProjectCostSnapshot.setSnapShotID(rs.getLong("snapshotid"));
        	qprProjectCostSnapshot.setProjectID(rs.getLong("projectid"));
        	qprProjectCostSnapshot.setAgencyId(rs.getLong("agencyid"));
        	qprProjectCostSnapshot.setCostComponent(rs.getInt("costcomponent"));
        	
        	qprProjectCostSnapshot.setCostCurrency(rs.getInt("costcurrency"));
        	
        	qprProjectCostSnapshot.setEstimatedCost(rs.getBigDecimal("estimatedcost"));
        	
        	qprProjectCostSnapshot.setAgencyType(rs.getString("agencytype"));
            
            return qprProjectCostSnapshot;
        }
    };
    
    private final ParameterizedRowMapper<QPRProjectCostSnapshot> crossTabQprProjectCostSnapshotRowMapper = new ParameterizedRowMapper<QPRProjectCostSnapshot>() {

        public QPRProjectCostSnapshot mapRow(ResultSet rs, int row) throws SQLException {
        	QPRProjectCostSnapshot qprProjectCostSnapshot = new QPRProjectCostSnapshot();
        	
        	qprProjectCostSnapshot.setAgencyId(rs.getLong("agencyid"));
        	
        	qprProjectCostSnapshot.setAgencyType(rs.getString("agencytype"));
            
            return qprProjectCostSnapshot;
        }
    };

     
	public SynchroDAOUtil getSynchroDAOUtil() {
		return synchroDAOUtil;
	}

	public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
		this.synchroDAOUtil = synchroDAOUtil;
	}
   
}
