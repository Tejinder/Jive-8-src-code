package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.beans.ReadTrackerObject;
import com.grail.synchro.dao.ReadTrackerDAO;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.util.StringUtils;

/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class ReadTrackerDAOImpl extends SynchroAbstractDAO implements ReadTrackerDAO {
		
	 	private static final Logger LOG = Logger.getLogger(ReadTrackerDAOImpl.class);
		
	 	private static final String READTRACKER_FIELDS = "projectID, userID, readDate, stageID";
	 
	    private static final String LOAD_READTRACKER_INFO = "SELECT " +  READTRACKER_FIELDS + " FROM grailUserReadTracker as tracktable " +
	            " WHERE userID = ? order by tracktable.readdate DESC";
	    
	    private static  final String INSERT_READTRACKER_INFO = "INSERT INTO grailUserReadTracker( "+ READTRACKER_FIELDS + ")" +
	            " VALUES (?, ?, ?, ?);";
	    
	    private static final String UPDATE_READTRACKER_INFO = "UPDATE grailUserReadTracker" +
	            " SET readDate=?" +
	            " WHERE projectID=? AND userID=? AND stageID=?";
	    
	 	@Override
	    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
	    public void saveUserReadInfo(final ReadTrackerObject readTrackerObject) 
	 	{
	 		Long projectID = readTrackerObject.getProjectID();
	 		Long userID = readTrackerObject.getUserID();
	 		Long stageID = readTrackerObject.getStageID();
	        int count = getSimpleJdbcTemplate().queryForInt("SELECT COUNT(*) FROM grailUserReadTracker " +
	                " WHERE projectID =? AND userID = ?",projectID, userID);
	        if(count == 0){
	            try {
	                getSimpleJdbcTemplate().update(INSERT_READTRACKER_INFO,
	                		readTrackerObject.getProjectID(),
	                		readTrackerObject.getUserID(),
	                		readTrackerObject.getReadDate(),
	                		readTrackerObject.getStageID()
	                );
	            }
	            catch (DataAccessException e) {
	                final String message = "Failed to insert Read track info for User " + userID+" and Project "+projectID;
	                LOG.error(message, e);
	                throw new DAOException(message, e);
	            }
	        }else{
	            update(readTrackerObject);
	        }	        
	    }	
	 
	 	
	 /**
	  * Update Project History for current Project, User and Stage 	  
	  * @param readTrackerObject
	  */
	    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
	    public void update(final ReadTrackerObject readTrackerObject) {
	        try {
	            getSimpleJdbcTemplate().update(UPDATE_READTRACKER_INFO,
	            		readTrackerObject.getReadDate(),
	            		readTrackerObject.getProjectID(),
	            		readTrackerObject.getUserID(),
	            		readTrackerObject.getStageID()
	            );
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to update Read track info  " + readTrackerObject.toString();
	            LOG.error(message, e);
	            throw new DAOException(message, e);
	        }
	    }

	 	
	 	
	 	@Override
	 	public List<ReadTrackerObject> getUserReadInfo(final Long userID)
	 	{
	 		List<ReadTrackerObject> readTrackerObjects = Collections.emptyList();
	        try{
	        	readTrackerObjects = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_READTRACKER_INFO,
	            		readTrackerObjectRowMapper, userID);
	            return readTrackerObjects;
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to get User Read Information for User " + userID;
	            LOG.error(message, e);
	            throw new DAOException(message, e);
	        }
	    }
	 	
	 	
	 	@Override
	 	public List<ReadTrackerObject> getUserReadInfo(final Long userID, final ProjectResultFilter projectResultFilter)
	 	{
	 		boolean flag = true;
	 		List<ReadTrackerObject> readTrackerObjects = Collections.emptyList();
	    	String LOAD_READTRACKER_BY_FILTER = "SELECT rt.projectid, rt.userid, rt.readdate, rt.stageid" +
		            "  FROM grailuserreadtracker as rt JOIN grailproject gp ON rt.projectid = gp.projectid WHERE rt.userid="+userID;
	    	 
	 		flag = false;
	        //project name keyword
	        String name = projectResultFilter.getName();
	        if(!StringUtils.isNullOrEmpty(name))
			{
	        	if(flag)
				{
	        		LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " where LOWER(gp.name) like '%" + name.toLowerCase() +"%'";
					flag = false;
				}
				else
				{
					LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " and LOWER(gp.name) like '%" + name.toLowerCase() +"%'";
				}
	        	
	        	
				
			}
	        
	      //Project Owner
	        List<Long> owner= projectResultFilter.getOwnerfield();
	        if(owner!=null && owner.size()>0)
			{
				if(flag)
					{
					LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " where gp.ownerid in ("+StringUtils.join(owner, ',')+")";
						flag = false;
					}
					else
					{
						LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " and gp.ownerid in ("+StringUtils.join(owner, ',')+")";
					}
			}
	        
	        //Project Brand Fields
	        List<Long> brandFields = projectResultFilter.getBrandFields();
	        if(brandFields!=null && brandFields.size()>0)
	        {
	        	if(flag)
				{
	        		LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " where gp.brand in ("+StringUtils.join(brandFields, ',')+")";
					flag = false;
				}
				else
				{
					LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " and gp.brand in ("+StringUtils.join(brandFields, ',')+")";
				}
	        }
	        
	        //Fetch Region specific filters
	    	List<Long> regions = projectResultFilter.getRegionFields();
	    	List<Long> endMarkets = projectResultFilter.getEndMarkets();
			
	    	List<Long> filterEndMarketIDs = new ArrayList<Long>();
			//List<Long> regions = researchCycleReportFilters.getRegionFields();
			/*
	    	List<Long> regionEndMarketIDs = new ArrayList<Long>();
			if(regions != null && regions.size() > 0)
			{
				Map<String, Integer> regionMapping = SynchroGlobal.getRegionEndMarketEnum();
				for(String emID : regionMapping.keySet())
				{
					Integer regionID = regionMapping.get(emID)+1;
					if((regions.contains(Long.parseLong(regionID.toString())) || regions.contains(Long.parseLong((SynchroGlobal.Region.GLOBAL.ordinal()+1)+""))) && !regionEndMarketIDs.contains(Long.parseLong(regionID.toString())))
					{
						regionEndMarketIDs.add(Long.parseLong(emID+""));
					}
				}
			}
		
			//End Market details
			//List<Long> endMarkets = researchCycleReportFilters.getEndMarkets();
			boolean includeMarketClause = false;
			if(endMarkets!=null && endMarkets.size() > 0)
			{
				filterEndMarketIDs.addAll(endMarkets);
			}
			if(regionEndMarketIDs!=null && regionEndMarketIDs.size() > 0)
			{
				filterEndMarketIDs.addAll(regionEndMarketIDs);
			}
	    */
	    	boolean includeMarketClause = false;
			List<Long> e_projectIDs = Collections.emptyList();
				if(filterEndMarketIDs != null && filterEndMarketIDs.size() > 0)
				{
				//Filter out the unique values
				Set<Long> set  = new HashSet<Long>(filterEndMarketIDs);
				ArrayList<Long> filterEndMarketIDsUnique = new ArrayList<Long>();
				filterEndMarketIDsUnique.addAll(set);
				
				String ENDMARKET_DETAILS_SQL = "SELECT projectid FROM grailendmarketdetails where endmarketid in ("+StringUtils.join(filterEndMarketIDsUnique, ',')+")";
					
			        try {
			        	e_projectIDs = getSimpleJdbcTemplate().getJdbcOperations().queryForList(ENDMARKET_DETAILS_SQL,
			                    Long.class);
			        }
			        catch (DataAccessException e) {
			            final String message = "Failed to load end markets and regions mentioned in Advannced Filter ";
			            LOG.error(message, e);
			            throw new DAOException(message, e);
			        }
				}
			
				 //Fetch projects based on end markets/Region selected in filter
		        if(includeMarketClause)
		        {
		        	if(flag)
					{
		        		LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " where gp.projectid in ("+StringUtils.join(e_projectIDs, ',')+")";
						flag = false;
					}
					else
					{
						LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " and gp.projectid in ("+StringUtils.join(e_projectIDs, ',')+")";
					}
		        }
		        
	        
	        LOAD_READTRACKER_BY_FILTER = LOAD_READTRACKER_BY_FILTER + " order by rt.readdate DESC";

	        try{
	        	readTrackerObjects = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_READTRACKER_BY_FILTER, readTrackerObjectRowMapper);
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to User History information by Filter";
	            LOG.error(message, e);
	            throw new DAOException(message, e);
	        }
	        return readTrackerObjects;
	    }
	 	
	 	
	 	
	    private final ParameterizedRowMapper<ReadTrackerObject> readTrackerObjectRowMapper = new ParameterizedRowMapper<ReadTrackerObject>() {
	        public ReadTrackerObject mapRow(ResultSet rs, int row) throws SQLException {
	            final ReadTrackerObject readTrackerObject = new ReadTrackerObject();
	            readTrackerObject.setProjectID(rs.getLong("projectID"));
	            readTrackerObject.setUserID(rs.getLong("userID"));
	            readTrackerObject.setStageID(rs.getLong("stageID"));
	            readTrackerObject.setReadDate(rs.getLong("readDate"));
	            return readTrackerObject;
	        }
	    };
}
