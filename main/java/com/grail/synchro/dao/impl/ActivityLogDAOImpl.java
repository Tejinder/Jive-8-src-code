package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.grail.synchro.util.SynchroLogUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ActivityLog;
import com.grail.synchro.dao.ActivityLogDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.search.filter.LogResultFilter;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Kanwar Grewal
 * @version 1.0
 */
public class ActivityLogDAOImpl extends JiveJdbcDaoSupport implements ActivityLogDAO {
    private static final Logger LOG = Logger.getLogger(ActivityLogDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static final String FIELDS = "timestamp, userID, userName, " +
            " portal, page, type, stage, jsonValue, projectID, endmarketID, projectName, waiverid ";

    private static final String FETCH_ACTIVITY_LOGS_ALL = "SELECT " + FIELDS +
            " FROM grailactivitylog order by timestamp DESC ";

    private static final String FETCH_ACTIVITY_LOGS_BY_USER = "SELECT " + FIELDS +
            " FROM grailactivitylog WHERE userID = ? order by timestamp DESC";

    private static final String INSERT_LOG_ACTIVITY = "INSERT INTO grailactivitylog( " +FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String GET_TOTAL_COUNT = "SELECT count(*) FROM grailactivitylog al";

    private static final String GET_LOGS_BY_FILTER = "SELECT " + FIELDS +
            " FROM grailactivitylog al";

    private static final String GET_PROJECT_IDS = "SELECT p.projectid FROM grailproject p";

    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }

    @Override
    public List<ActivityLog> getActivityLogs() {
        return getSimpleJdbcTemplate().query(FETCH_ACTIVITY_LOGS_ALL, activityLogRowMapper);
    }


    @Override
    public List<ActivityLog> getActivityLogs(final LogResultFilter filter, final Long userID) {
        List<ActivityLog> activityLogs = Collections.EMPTY_LIST;
        StringBuilder sql = new StringBuilder(GET_LOGS_BY_FILTER);
        sql.append(applyLogFilter(filter, userID, false));

        try {
            activityLogs = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), activityLogRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load logs by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return activityLogs;
    }


    @Override
    public List<ActivityLog> getActivityLogs(final Long userID) {
        return getSimpleJdbcTemplate().query(FETCH_ACTIVITY_LOGS_BY_USER, activityLogRowMapper, userID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveActivityLog(final ActivityLog activityLog) {

//            String sql = "INSERT INTO "+ SynchroLogUtils.getCurrentTableName()+"( " +FIELDS + ")" +
//                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        String sql = INSERT_LOG_ACTIVITY;
            try {
                getSimpleJdbcTemplate().update(sql,
                        activityLog.getTimestamp(),
                        activityLog.getUserID(),
                        activityLog.getUserName(),
                        activityLog.getPortal(),
                        activityLog.getPage(),
                        activityLog.getType(),
                        activityLog.getStage(),
                        activityLog.getJsonValue(),
                        activityLog.getProjectID(),
                        activityLog.getEndmarketID(),
                        activityLog.getProjectName(),
                        activityLog.getWaiverID()
                );
            }
            catch (DataAccessException e) {
                final String message = "Failed to insert audit into database";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void createTableIfNotExists() {
        String table = SynchroLogUtils.getCurrentTableName();
        boolean isTableExits = false;
        try {
            getSimpleJdbcTemplate().getJdbcOperations().queryForInt("SELECT COUNT(*) FROM " + table);
            isTableExits = true;
        } catch (Exception e) {
            isTableExits = false;
        }
        if(!isTableExits) {
            try {
                String tableCreateSql = "CREATE TABLE "+ table + " () INHERITS (grailactivitylog)";
                getSimpleJdbcTemplate().update(tableCreateSql);
            } catch (Exception e) {
            }
        }
    }

    private final RowMapper<ActivityLog> activityLogRowMapper = new RowMapper<ActivityLog>() {
        public ActivityLog mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            ActivityLog activityLog = new ActivityLog();
            activityLog.setTimestamp(rs.getLong("timestamp"));
            activityLog.setUserName(rs.getString("userName"));
            activityLog.setUserID(rs.getLong("userID"));
            activityLog.setPortal(rs.getString("portal"));
            activityLog.setPage(rs.getInt("page"));
            activityLog.setType(rs.getInt("type"));
            activityLog.setStage(rs.getInt("stage"));
            activityLog.setJsonValue(rs.getString("jsonValue"));
            activityLog.setProjectID(rs.getLong("projectID"));
            activityLog.setEndmarketID((rs.getLong("endmarketID")));
            activityLog.setProjectName(rs.getString("projectName"));
            activityLog.setWaiverID(rs.getLong("waiverid"));
            
            return activityLog;
        }
    };

    @Override
    public Long getTotalCount(final LogResultFilter filter, final Long userID) {
        Long count = 0L;
        StringBuilder sql = new StringBuilder(GET_TOTAL_COUNT);
        sql.append(applyLogFilter(filter, userID, true));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        }
        catch (DataAccessException e) {
            final String message = "Failed to load logs by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return count;
    }


    public String applyLogFilter(final LogResultFilter filter, final Long userID, final Boolean countQuery)
    {
        StringBuilder sql = new StringBuilder();
        List<String> conditions = new ArrayList<String>();
        Long timestamp_start = null;
        Long timestamp_end = null;

        if(filter.getStartDateLog()!=null)
        {
            timestamp_start = filter.getStartDateLog().getTime();
        }
        if(filter.getEndDateLog()!=null)
        {
            //timestamp_end = filter.getEndDateLog().getTime();
            
           // String untildate="2011-10-08";//can take any date in current format    
           try
           {
	        	/*SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy" );   
	            Calendar cal = Calendar.getInstance();    
	            cal.setTime(filter.getEndDateLog());    
	            cal.add( Calendar.DATE, 1 );    
	            timestamp_end = new Long(cal.getTime().getTime());    
	            //System.out.println("Date increase by one.."+convertedDate);
	            */
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(filter.getEndDateLog());
	            cal.add(Calendar.DATE, 1); //minus number would decrement the days
	            timestamp_end =  new Long(cal.getTime().getTime());    
           }
           catch(Exception e)
           {
        	   
           }
        }

        if(timestamp_start!= null)
        {
            StringBuilder dateCondition = new StringBuilder();
            dateCondition.append(" al.timestamp >= " + timestamp_start);
            conditions.add(dateCondition.toString());
        }
        if(timestamp_end!= null)
        {
            StringBuilder dateCondition = new StringBuilder();
            dateCondition.append(" al.timestamp <= " + timestamp_end);
            conditions.add(dateCondition.toString());
        }

        if(filter.getProjectID() == null)
        {
            if(filter.getPortalFields() != null && filter.getPortalFields().size() > 0) {
                StringBuilder portalCondition = new StringBuilder();
                portalCondition.append(" al.portal in (");
                portalCondition.append(StringUtils.join(SynchroDAOUtil.getPortalNames(filter.getPortalFields()), ",")).append(") ");
                conditions.add(portalCondition.toString());
            }


            if(filter.getPageFields() != null && filter.getPageFields().size() > 0) {
                StringBuilder pageCondition = new StringBuilder();
                pageCondition.append(" al.page in (");
                pageCondition.append(StringUtils.join(filter.getPageFields(), ",")).append(") ");
                conditions.add(pageCondition.toString());
            }

        }

        if(filter.getActivityFields() != null && filter.getActivityFields().size() > 0) {
            StringBuilder activityCondition = new StringBuilder();
            activityCondition.append(" al.type in (");
            activityCondition.append(StringUtils.join(filter.getActivityFields(), ",")).append(") ");
            conditions.add(activityCondition.toString());
        }
        /*
                //Fetch by user
                if(userID != null && userID > 0) {
                    StringBuilder userCondition = new StringBuilder();
                    userCondition.append(" al.userID = ");
                    userCondition.append(userID);
                    conditions.add(userCondition.toString());
                }
                */
        //keyword Search for Admin Dashboard or All Project Logs
        if(filter.getProjectID() == null)
        {
            if(filter.getKeyword()!=null && com.jivesoftware.util.StringUtils.isNotBlank(filter.getKeyword()))
            {
                List<Integer> keylist = new ArrayList<Integer>();
                StringBuilder keywordCondition = new StringBuilder();
                keywordCondition.append("(lower(al.projectName) like '%").append(filter.getKeyword().toLowerCase()).append("%'")
                        .append(" OR ").append("(''|| al.projectID ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(filter.getKeyword())).append("%'")
                        .append(" OR ").append("lower(al.userName) like '%").append(filter.getKeyword().toLowerCase()).append("%'")
                        .append(" OR ").append("lower(al.portal) like '%").append(filter.getKeyword().toLowerCase()).append("%'")
                        .append(" OR ").append("(''|| al.waiverid ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(filter.getKeyword())).append("%'");

                //Page Type
                for(Integer key : SynchroGlobal.getPageTypes().keySet())
                {
                    if(SynchroGlobal.getPageTypes().get(key).toLowerCase().contains(filter.getKeyword().toLowerCase()))
                    {
                        keylist.add(key);
                    }
                }
                if(keylist.size() > 0)
                {
                    keywordCondition.append(" OR ").append("al.page in (").append(StringUtils.join(keylist, ",")).append(")");
                }

                //Activity Type
                keylist.clear();
                for(Integer key : SynchroGlobal.getActivityTypes().keySet())
                {
                    if(SynchroGlobal.getActivityTypes().get(key).toLowerCase().contains(filter.getKeyword().toLowerCase()))
                    {
                        keylist.add(key);
                    }
                }
                if(keylist.size() > 0)
                {
                    keywordCondition.append(" OR ").append("al.type in (").append(StringUtils.join(keylist, ",")).append(")");
                }


                keywordCondition.append(")");

                conditions.add(keywordCondition.toString());
            }
        }
        else
        {
            //keyword Search for Project Log Dashboard or Limited field search
            if(filter.getKeyword()!=null && com.jivesoftware.util.StringUtils.isNotBlank(filter.getKeyword()))
            {
                List<Integer> keylist = new ArrayList<Integer>();
                StringBuilder keywordCondition = new StringBuilder();
                keywordCondition.append("(lower(al.userName) like '%").append(filter.getKeyword().toLowerCase()).append("%'");
                keywordCondition.append(" OR ").append("lower(al.jsonValue) like '%").append(filter.getKeyword().toLowerCase()).append("%'");
                keywordCondition.append(")");
                conditions.add(keywordCondition.toString());
            }
        }

        if(filter.getProjectID() == null)
        {
            //name
            if(filter.getName()!=null && com.jivesoftware.util.StringUtils.isNotBlank(filter.getName()))
            {
                StringBuilder nameCondition = new StringBuilder();
                nameCondition.append("(lower(al.projectName) like '%").append(filter.getName().toLowerCase()).append("%'")
                        .append(")");
                conditions.add(nameCondition.toString());
            }
        }
        else
        {
            //name
            if(filter.getName()!=null && com.jivesoftware.util.StringUtils.isNotBlank(filter.getName()))
            {
                StringBuilder nameCondition = new StringBuilder();
                nameCondition.append("(lower(al.jsonValue) like '%").append(filter.getName().toLowerCase()).append("%'")
                        .append(")");
                conditions.add(nameCondition.toString());
            }
        }

        //Project ID
        if(filter.getProjectID() != null && filter.getProjectID() > 0) {
            StringBuilder projectCondition = new StringBuilder();
            projectCondition.append(" al.projectID = ");
            projectCondition.append(filter.getProjectID());
            conditions.add(projectCondition.toString());
        }
        
        //Waiver ID
        if(filter.getWaiverID() != null && filter.getWaiverID() > 0) {
            StringBuilder projectCondition = new StringBuilder();
            projectCondition.append(" al.waiverId = ");
            projectCondition.append(filter.getWaiverID());
            conditions.add(projectCondition.toString());
        }

        if(conditions.size() > 0) {
            sql.append(" WHERE ").append(org.apache.commons.lang.StringUtils.join(conditions, " AND "));
        }

        if(!countQuery)
        {
            //Apply Sortfield filter
            if(filter.getSortField() != null && !filter.getSortField().equals("")) {
                sql.append(" order by ").append(getOrderByField(filter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(filter.getAscendingOrder())).append(",").append(getOrderByField("timestamp")).append(" ").append(SynchroDAOUtil.getSortType(1));
            } else {
                sql.append(" order by ").append(getOrderByField("timestamp")).append(" ").append(SynchroDAOUtil.getSortType(1)).append(",").append(getOrderByField("type")).append(" ").append(SynchroDAOUtil.getSortType(0));;
            }

            if(filter.getLimit()!=null && filter.getLimit().intValue()!=-1)
            {
                //Apply OFFSET & LIMIT, required for pagination
                if(filter.getStart() != null) {
                    sql.append(" OFFSET ").append(filter.getStart());
                }
                if(filter.getLimit() != null && filter.getLimit() > 0) {
                    sql.append(" LIMIT ").append(filter.getLimit());
                }
            }
        }
        return sql.toString();
    }

    private String getOrderByField(final String sortField) {
        if(StringUtils.isNotBlank(sortField)) {
            String field = null;
            if(sortField.equals("id")) {
                field = "al.projectID";
            } else if(sortField.equals("name")) {
                field = "al.projectName";
            } else if(sortField.equals("userName")) {
                field = "al.userName";
            } else if(sortField.equals("portal")) {
                field = "al.portal";
            } else if(sortField.equals("page")) {
                field = "al.page";
            } else if(sortField.equals("stage")) {
                field = "al.stage";
            } else if(sortField.equals("timestamp")) {
                field = "al.timestamp";
            } else if(sortField.equals("waiverId")) {
                field = "al.waiverId";
            }
            else {
                field = sortField;
            }
            return field;
        }
        return null;
    }

}
