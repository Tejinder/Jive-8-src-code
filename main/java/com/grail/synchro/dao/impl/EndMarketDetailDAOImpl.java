package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.dao.EndMarketDetailDAO;
import com.jivesoftware.base.database.dao.DAOException;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/30/13
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class EndMarketDetailDAOImpl extends SynchroAbstractDAO implements EndMarketDetailDAO {
    private static final Logger LOG = Logger.getLogger(EndMarketDetailDAOImpl.class);

    private static final String ENDMARKET_FIELDS = "projectID, endMarketID, initialCost, initialCostCurrency, " +
    		" spiContact, creationBy, modificationBy, creationDate, modificationDate";

    private static final String INSERT_ENDMARKET = "INSERT INTO grailEndMarketInvestment( " +  ENDMARKET_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String UPDATE_ENDMARKET = "UPDATE grailEndMarketInvestment" +
            " SET initialCost=?, initialCostCurrency=?,  spiContact=?, " +
            " modificationby=?, modificationdate=? " +
            " WHERE projectid = ? AND endMarketID = ?";
    

    private static final String UPDATE_REFERENCE_ENDMARKET = "UPDATE grailEndMarketInvestment" +
            " SET synchrocode=? " +
            " WHERE projectid = ? AND endMarketID = ?";
    
    private static final String UPDATE_FUNDING_ENDMARKET = "UPDATE grailEndMarketInvestment" +
            " SET fundingMarket=? " +
            " WHERE projectid = ? AND endMarketID = ?";

    private static final String LOAD_ENDMARKET = "SELECT " +  ENDMARKET_FIELDS + ", synchrocode, fundingMarket FROM grailEndMarketInvestment " +
            " WHERE projectID = ? AND endMarketID = ?";

    private static final String LOAD_PROJECT_ENDMARKETS = "SELECT " +  ENDMARKET_FIELDS + ", synchrocode, fundingMarket FROM grailEndMarketInvestment " +
            " WHERE projectid = ? order by endMarketID";

    private static final String LOAD_PROJECT_ENDMARKET_IDS = "SELECT endMarketID FROM grailEndMarketInvestment WHERE projectid = ? order by endMarketID";

    private static final String DELETE_ENDMARKET = "DELETE FROM grailEndMarketInvestment WHERE projectid = ? AND endMarketID = ?";
    
    private static final String DELETE_ENDMARKET_ALL = "DELETE FROM grailEndMarketInvestment WHERE projectid = ?";
    private static final String UPDATE_SINGLE_ENDMARKET_ID = "UPDATE grailEndMarketInvestment" +
            " SET endMarketID=? WHERE projectid = ?";
    
    private static final String UPDATE_INITIAL_COST = "UPDATE grailEndMarketInvestment" +
            " SET initialcost=?, initialcostcurrency=? WHERE endMarketID=? AND projectid = ?";
    
    private static final String UPDATE_INITIAL_COST_SM = "UPDATE grailEndMarketInvestment" +
            " SET initialcost=?, initialcostcurrency=? WHERE projectid = ?";
    
    private static final String UPDATE_SPI_CONTACT = "UPDATE grailEndMarketInvestment" +
            " SET spiContact=? WHERE projectid = ? and endMarketID = ?";
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public EndMarketInvestmentDetail save(final EndMarketInvestmentDetail endMarketInvestmentDetail) {
        try {

            final int count = getSimpleJdbcTemplate().queryForInt("SELECT count(endMarketID) from grailEndMarketInvestment " +
                    "WHERE projectid = ? AND endMarketID = ?",
                    endMarketInvestmentDetail.getProjectID(), endMarketInvestmentDetail.getEndMarketID());

            if(count == 0){
                updateAuditFields(endMarketInvestmentDetail, false);
                getSimpleJdbcTemplate().update(INSERT_ENDMARKET,                
                		endMarketInvestmentDetail.getProjectID(),
                		endMarketInvestmentDetail.getEndMarketID(),
                		endMarketInvestmentDetail.getInitialCost(),
                		endMarketInvestmentDetail.getInitialCostCurrency(),
                		endMarketInvestmentDetail.getSpiContact(),
                		endMarketInvestmentDetail.getCreationBy(),
                		endMarketInvestmentDetail.getModifiedBy(),
                		endMarketInvestmentDetail.getCreationDate(),
                		endMarketInvestmentDetail.getModifiedDate()
                );
            }
            else
            {
            	 update(endMarketInvestmentDetail);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to create new endmarket Investment entry";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return endMarketInvestmentDetail;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateReferenceEndMarkets(final EndMarketInvestmentDetail endMarketInvestmentDetail) {
        try {

           
                getSimpleJdbcTemplate().update(UPDATE_REFERENCE_ENDMARKET,                
                		endMarketInvestmentDetail.getReferenceSynchroCode(),
                		endMarketInvestmentDetail.getProjectID(),
                		endMarketInvestmentDetail.getEndMarketID()
                		
                );
           
        }
        catch (DataAccessException e) {
            final String message = "Failed to create new endmarket Investment entry";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateFundingEndMarkets(final EndMarketInvestmentDetail endMarketInvestmentDetail) {
        try {

           
                getSimpleJdbcTemplate().update(UPDATE_FUNDING_ENDMARKET,                
                		endMarketInvestmentDetail.getIsFundingMarket()?1:0,
                		endMarketInvestmentDetail.getProjectID(),
                		endMarketInvestmentDetail.getEndMarketID()
                		
                );
           
        }
        catch (DataAccessException e) {
            final String message = "Failed to create new endmarket Investment entry";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public EndMarketInvestmentDetail update(final EndMarketInvestmentDetail endMarketInvestmentDetail) {
        // update audit fields
        updateAuditFields(endMarketInvestmentDetail, true);
        try {
                  getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_ENDMARKET,
            		endMarketInvestmentDetail.getInitialCost(),
            		endMarketInvestmentDetail.getInitialCostCurrency(),
            		endMarketInvestmentDetail.getSpiContact(),
            		endMarketInvestmentDetail.getModifiedBy(),
            		endMarketInvestmentDetail.getModifiedDate()
            );
            return endMarketInvestmentDetail;
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the endmarket Investment details.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
	public void updateSingleEndMarketId(final Long projectID,
			final Long endMarketID) {
		try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_SINGLE_ENDMARKET_ID, endMarketID, projectID);

		} catch (DataAccessException e) {
			final String message = "Failed to update the Single endmarket Id for Project "
					+ projectID;
			LOG.error(message, e);
			throw new DAOException(message, e);
		}
	}
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
	public void updateSPIContact(final Long projectID,
			final Long endMarketID, final Long spiContact) {
		try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_SPI_CONTACT, spiContact, projectID, endMarketID );

		} catch (DataAccessException e) {
			final String message = "Failed to update the SPI Contact for Project "
					+ projectID;
			LOG.error(message, e);
			throw new DAOException(message, e);
		}
	}

    @Override
    public EndMarketInvestmentDetail get(final Long projectID, final Long endMarketID) {
        LOG.debug("## Return the list of EndMarket object for project - " + projectID);
        EndMarketInvestmentDetail endMarketInvestmentDetail = null;
        try {
        	endMarketInvestmentDetail = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_ENDMARKET, endMarketDetailRowMapper, projectID, endMarketID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load endmarket projectID: " + projectID + ", endMarketID: " + endMarketID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return endMarketInvestmentDetail;
    }

    @Override
    public List<EndMarketInvestmentDetail> getProjectEndMarkets(final Long projectID) {
        LOG.debug("## Return the list of EndMarket object for project - " + projectID);
        List<EndMarketInvestmentDetail> endMarketInvestmentDetails = Collections.emptyList();
        try {
        	endMarketInvestmentDetails = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_PROJECT_ENDMARKETS,
                    endMarketDetailRowMapper, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load endmarkets for project " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return endMarketInvestmentDetails;
    }

    @Override
    public List<Long> getProjectEndMarketIds(final Long projectID) {
        List<Long> endMarketIDs = Collections.emptyList();
        try {
            endMarketIDs = getSimpleJdbcTemplate().getJdbcOperations().queryForList(LOAD_PROJECT_ENDMARKET_IDS,
                    Long.class, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load endmarkets for project " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return endMarketIDs;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void delete(final Long projectID, final Long endMarketID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET, projectID, endMarketID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load endmarkets for project " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void delete(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_ALL, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete endmarkets for project " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    private final ParameterizedRowMapper<EndMarketInvestmentDetail> endMarketDetailRowMapper = new ParameterizedRowMapper<EndMarketInvestmentDetail>() {
        public EndMarketInvestmentDetail mapRow(ResultSet rs, int row) throws SQLException {
        	EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail();
        	endMarketInvestmentDetail.setProjectID(rs.getLong("projectID"));
        	endMarketInvestmentDetail.setEndMarketID(rs.getLong("endMarketID"));
        	endMarketInvestmentDetail.setInitialCost(rs.getBigDecimal("initialCost"));
        	endMarketInvestmentDetail.setInitialCostCurrency(rs.getLong("initialCostCurrency"));
        	endMarketInvestmentDetail.setSpiContact(rs.getLong("spiContact"));
        	endMarketInvestmentDetail.setCreationBy(rs.getLong("creationBy"));
        	endMarketInvestmentDetail.setCreationDate(rs.getLong("creationDate"));
        	endMarketInvestmentDetail.setModifiedBy(rs.getLong("modificationBy"));
        	endMarketInvestmentDetail.setModifiedDate(rs.getLong("modificationDate"));
        	endMarketInvestmentDetail.setReferenceSynchroCode(rs.getLong("synchrocode"));
        	endMarketInvestmentDetail.setIsFundingMarket(rs.getBoolean("fundingMarket"));
            return endMarketInvestmentDetail;
        }
    };
    private void copy(String[] source, List<Long> target, int index) {
        if (index == source.length)
            return;
        target.add(Long.parseLong(source[index]));
        copy(source, target, index+1);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateInitialCost(final EndMarketInvestmentDetail endMarketInvestmentDetail)
    {
    	try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_INITIAL_COST, endMarketInvestmentDetail.getInitialCost(),endMarketInvestmentDetail.getInitialCostCurrency(),
					endMarketInvestmentDetail.getEndMarketID(),endMarketInvestmentDetail.getProjectID());

		} catch (DataAccessException e) {
			final String message = "Failed to update the Initial Cosr for Project - "+ endMarketInvestmentDetail.getProjectID() +" and EndMarket -" + endMarketInvestmentDetail.getEndMarketID();
			LOG.error(message, e);
			throw new DAOException(message, e);
		}
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateInitialCostSM(final EndMarketInvestmentDetail endMarketInvestmentDetail)
    {
    	try {
			getSimpleJdbcTemplate().getJdbcOperations().update(
					UPDATE_INITIAL_COST_SM, endMarketInvestmentDetail.getInitialCost(),endMarketInvestmentDetail.getInitialCostCurrency(),
					endMarketInvestmentDetail.getProjectID());

		} catch (DataAccessException e) {
			final String message = "Failed to update the Initial Cost SM  for Project - "+ endMarketInvestmentDetail.getProjectID() +" and EndMarket -" ;
			LOG.error(message, e);
			throw new DAOException(message, e);
		}
    }
    
    @Override
    public Integer getEndMarketStatus(final Long projectID, final Long endmarketID) {
    	String GET_ENDMARKET_STATUS = "SELECT status from grailProjectStatus where projectID = ? AND endMarketID = ?";        
    	int status = -1;
        try {
        	int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectStatus where projectID = ? AND endMarketID=?", projectID, endmarketID);
        	if(count>0)
        	{
        		status = getSimpleJdbcTemplate().getJdbcOperations().queryForInt(GET_ENDMARKET_STATUS, projectID, endmarketID);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load endmarket status for projectID: " + projectID + ", endMarketID: " + endmarketID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return status;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteEndMarketStatus(final Long projectID, final List<Long> endmarketIDs) {
    	
    	String DELETE_ENDMARKET_STATUS = "DELETE FROM grailProjectStatus WHERE projectid = ? AND endMarketID IN ("+StringUtils.join(endmarketIDs, ',')+")";
    	if(endmarketIDs!=null && endmarketIDs.size()>0)
    	{
    		try {
        		int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectStatus where projectID = ? AND endMarketID IN ("+StringUtils.join(endmarketIDs, ',')+")", projectID);
             	if(count>0)
             	{
             		getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_STATUS, projectID);
             	}         	
             	
         	} catch (DataAccessException e) {
             final String message = "Failed to delete Project endmarket status from grailProjectStatus table";
             LOG.error(message, e);
             throw new DAOException(message, e);
         }	
    	}
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteEndMarketStatus(final Long projectID, final Long endmarketID) {
    	
    	String DELETE_ENDMARKET_STATUS = "DELETE FROM grailProjectStatus WHERE projectid = ? AND endMarketID = ? ;";
    	if(endmarketID!=null && endmarketID>0)
    	{
    		try {
        		int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectStatus where projectID = ? AND endMarketID = ?", projectID, endmarketID);
             	if(count>0)
             	{
             		getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ENDMARKET_STATUS, projectID, endmarketID);
             	}         	
             	
         	} catch (DataAccessException e) {
             final String message = "Failed to delete Project endmarket status from grailProjectStatus table";
             LOG.error(message, e);
             throw new DAOException(message, e);
         }	
    	}
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void setEndMarketStatus(final Long projectID, final List<Long> endmarketIDs, final Integer status) {
    	
    	String INSERT_ENDMARKET_STATUS = "INSERT into grailProjectStatus(projectID, endMarketID, status) VALUES(?, ?, ?)";
    	try {
    		if(endmarketIDs!=null && endmarketIDs.size() > 0)
    		{
    			for(Long endmarketID : endmarketIDs)
    			{
    				int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectStatus where projectID = ? AND endMarketID=?", projectID, endmarketID);
                 	if(count==0)
                 	{
                 		getSimpleJdbcTemplate().update(INSERT_ENDMARKET_STATUS, projectID, endmarketID, status);
                 	}
                 	else
                 	{
                 		updateEndMarketStatus(projectID, endmarketID, status);
                 	}	
    			}
    				
    		}
     	} catch (DataAccessException e) {
         final String message = "Failed to insert project endmarket status into grailProjectStatus for projectID="+ projectID;
         LOG.error(message, e);
         throw new DAOException(message, e);
     }
   }
    
@Override
@Transactional(readOnly = false, propagation = Propagation.MANDATORY)
public void setEndMarketStatus(final Long projectID, final Long endmarketID, final Integer status) {
	
	String INSERT_ENDMARKET_STATUS = "INSERT into grailProjectStatus(projectID, endMarketID, status) VALUES(?, ?, ?)";
	try {
		int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectStatus where projectID = ? AND endMarketID=?", projectID, endmarketID);
     	if(count==0)
     	{
     		getSimpleJdbcTemplate().update(INSERT_ENDMARKET_STATUS, projectID, endmarketID, status);
     	}
     	else
     	{
     		updateEndMarketStatus(projectID, endmarketID, status);
     	}
     	
 	} catch (DataAccessException e) {
     final String message = "Failed to insert project status while deleting into grailProjectDeleteStatus";
     LOG.error(message, e);
     throw new DAOException(message, e);
 }
}

	public void updateEndMarketStatus(final Long projectID, final Long endmarketID, final Integer status) {

		String UPDATE_ENDMARKET_STATUS = "UPDATE grailProjectStatus SET status=? WHERE projectID = ? AND endMarketID = ? ";
		try {
	     		getSimpleJdbcTemplate().update(UPDATE_ENDMARKET_STATUS, status, projectID, endmarketID);
			} catch (DataAccessException e) {
	     final String message = "Failed to update project endmarket status for projectID "+ projectID + " endmarketID "+ endmarketID ;
	     LOG.error(message, e);
	     throw new DAOException(message, e);
	 }
		

	}
}


