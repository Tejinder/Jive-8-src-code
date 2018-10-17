package com.grail.custom.analytics.dao;

import com.grail.custom.analytics.EventAnalyticsUtil;
import com.grail.custom.analytics.SessionEventBean;
import com.grail.custom.analytics.util.ReportingMap;
import com.jivesoftware.base.database.ConnectionManager;
import com.jivesoftware.base.database.dao.JiveJdbcOperationsTemplate;
import com.jivesoftware.base.database.dao.SequenceDAO;
import com.jivesoftware.base.event.v2.BaseJiveEvent;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @Author Samee
 * @Date May 11, 2010
 * @Time 2:24:23 PM
 * @Version 1.0
 */
public class EventAnalyticsDAO extends SimpleJdbcDaoSupport {

    private static final Logger log = LogManager.getLogger(EventAnalyticsDAO.class);

    /* Spring Injection */
    private SequenceDAO sequenceDAO;

    // Add new activity event row details to DB
    protected final static String INSERT_USER_SESSION_SQL = "INSERT INTO USERSESSIONS(userSessionID, userID, loginTime, " +
            "logoutTime, duration) VALUES(?, ?, ?, ?, ?)";

    // Update activity event row details to DB
    protected final static String UPDATE_USER_SESSION_SQL = "UPDATE USERSESSIONS " +
            "SET logoutTime = ? , duration = ? WHERE userID = ?";

    // Get loginTime of user
    protected final static String GET_USER_ACTIVE_SESSION_DETAILS_SQL = "SELECT * FROM USERSESSIONS WHERE logoutTime = -1 AND duration = -1 AND userID = ?";
    protected final static String GET_USER_ACTIVE_SESSION_DETAILS_DESC_LOGINTIME_SQL = "SELECT * FROM USERSESSIONS WHERE logoutTime = -1 AND duration = -1 AND userID = ? ORDER BY loginTime DESC";

    // Fetch all session details from userSession table
    protected final static String INSERT_USER_SESSION_EVENT_SQL = "INSERT INTO userSessionEvents(userEventID, userSessionID, " +
            "objectID, objectType, containerID, containerType, eventType, eventName, eventUrl, ratingUser, ratingScore, startTime, endTime) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // Update activity event row details to DB
    protected final static String UPDATE_USER_SESSION_EVENT_ENDTIME_SQL = "UPDATE userSessionEvents " +
            "SET endTime = ? WHERE  objectID = ? AND objectType = ? " +
            "AND containerID = ? AND containerType = ? " +
            "AND userSessionID = ?";

    protected final static String UPDATE_MULTIPLE_USER_SESSION_EVENT_ENDTIME_SQL = "UPDATE userSessionEvents " +
            "SET endTime = ? WHERE  objectID = ? AND objectType = ? " +
            "AND containerID = ? AND containerType = ? " +
            "AND userSessionID IN ( %s )";

    protected final static String GET_USER_SESSION_ID = "SELECT loginTime FROM USERSESSIONS WHERE logoutTime = -1 ";

    // user - event - object
    protected final static String GET_USER_OBJ_EVENT_REPORT_SQL = "SELECT * FROM userSessionEvents WHERE userID = ? " +
            "AND objectType = ? AND eventType = ?";

    // REPORTS SQL
    //page-1 User Report
   // protected final String CREATE_VIEW_STATEMENT = "CREATE  VIEW AggEventDetails AS SELECT * FROM userSessionEvents where startTime >= ? AND endTime <= ?";
    //protected final String USER_REPORT_CREATE_VIEW_STATEMENT = "CREATE OR REPLACE  VIEW AggEventDetails AS SELECT * FROM userSessionEvents where startTime >= __ AND endTime <= ___  AND NOT endTime = -1";
    //protected final String USER_REPORT_CREATE_VIEW_STATEMENT = "CREATE OR REPLACE  VIEW AggEventDetails AS SELECT * FROM userSessionEvents where startTime >= __ AND endTime <= ___  AND eventName IN ('COMMENT_ADDED', 'FAVORITE_added', 'DOCUMENT_RATED') OR endTime != -1";
    // protected final String USER_REPORT_CREATE_VIEW_STATEMENT = "CREATE OR REPLACE  VIEW AggEventDetails AS SELECT * FROM userSessionEvents where startTime >= __ AND endTime <= ___  AND eventName IN ('COMMENT_ADDED', 'FAVORITE_added', 'DOCUMENT_RATED') OR endTime != -1";



    // Changes done as per the call with Grail on 24th May - from vikas/abhishek
    protected final String USER_REPORT_CREATE_VIEW_STATEMENT = "CREATE OR REPLACE  VIEW AggEventDetails AS SELECT * FROM userSessionEvents where startTime >= __ AND startTime <= ___";
    protected final String USER_REPORT_GET_READ_COUNT = "SELECT u.userID, count(u.userSessionID) as READ_COUNT from AggEventDetails aed, userSessions u  where aed.eventName = 'DOCUMENT_VIEWED' AND aed.userSessionID = u.userSessionID group by u.userID";
    protected final String USER_REPORT_GET_COMMENT_COUNT = "SELECT u.userID, count(u.userSessionID) as COMMENT_COUNT from AggEventDetails aed, userSessions u  where aed.eventName = 'COMMENT_ADDED' AND aed.userSessionID = u.userSessionID group by u.userID";
    //protected final String USER_REPORT_GET_RATING_COUNT = "SELECT u.userID, count(u.userSessionID) as RATING_COUNT from AggEventDetails aed, userSessions u  " +
    //       "where aed.eventName = 'DOCUMENT_RATED' AND aed.userSessionID = u.userSessionID group by u.userID";
    protected final String USER_REPORT_GET_RATING_COUNT = "SELECT u.userID,  count(distinct(objectid)) as RATING_COUNT FROM AggEventDetails use, usersessions u WHERE use.userSessionID = u.userSessionID AND use.eventName = 'DOCUMENT_RATED' GROUP BY u.userID";
    protected final String USER_REPORT_GET_BOOKMARK_COUNT = "SELECT u.userID, count(u.userSessionID) as BOOKMARK_COUNT from AggEventDetails aed, userSessions u  where aed.eventName = 'FAVORITE_added' AND aed.userSessionID = u.userSessionID group by u.userID";
  
//  Page-1 Drill Down report
//  private final String GET_USER_DOCUMENT_EVENTS_COUNT = "SELECT objectID as DOCUMENT, count(*) AS ACTIVITY_COUNT FROM AggEventDetails " +
//            "WHERE startTime >= ? AND endTime <= ?" +
//            "AND userSessionID IN (SELECT userSessionID from userSessions )" +
//            "AND eventName = ? GROUP BY objectID";
    private final String GET_USER_DOCUMENT_EVENTS_COUNT = "SELECT objectID as DOCUMENT, count(*) AS ACTIVITY_COUNT FROM AggEventDetails " +
            "WHERE startTime >= ? AND startTime <= ? " +
            "AND userSessionID IN ( SELECT userSessionID from userSessions WHERE userID = ?) " +
            "AND eventName = ? " +
            "GROUP BY objectID";


    //    Commented below to support the Export All Users and All Activities line 85 - 95

    /* DOCUMENT REPORT QUERIES */
    //protected final String DOC_REPORT_CREATE_VIEW = "CREATE OR REPLACE VIEW documentDetails AS SELECT * FROM userSessionEvents where startTime >= __ AND endTime <= ___ AND eventName IN ('COMMENT_ADDED', 'FAVORITE_added', 'DOCUMENT_RATED') OR endTime != -1";
    // Changes done as per the call with Grail on 24th May - from vikas/abhishek
    protected final String DOC_REPORT_CREATE_VIEW = "CREATE OR REPLACE VIEW documentDetails AS SELECT * FROM userSessionEvents where startTime >= __ AND startTime <= ___";

//    protected final String DOC_REPORT_GET_READ_COUNT = "SELECT aed.objectid, count(u.userSessionID) as READ_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'DOCUMENT_VIEWED' AND aed.userSessionID = u.userSessionID group by aed.objectid";
//    protected final String DOC_REPORT_GET_COMMENT_COUNT = "SELECT aed.objectid, count(u.userSessionID) as COMMENT_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'COMMENT_ADDED' AND aed.userSessionID = u.userSessionID group by aed.objectid";
//    protected final String DOC_REPORT_GET_BOOKMARK_COUNT = "SELECT aed.objectid, count(u.userSessionID) as BOOKMARK_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'FAVORITE_added' AND aed.userSessionID = u.userSessionID group by aed.objectid";
//    protected final String DOC_REPORT_GET_RATING_COUNT = "SELECT aed.objectid, count(u.userSessionID) as RATING_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'DOCUMENT_RATED' AND aed.userSessionID = u.userSessionID group by aed.objectid";

    // Changes by leo
    // New Queries
//      protected final String DOC_REPORT_GET_READ_COUNT = "SELECT u.userid, aed.objectid, count(u.userSessionID) as READ_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'DOCUMENT_VIEWED' AND aed.userSessionID = u.userSessionID group by aed.objectid,u.userid";
//      protected final String DOC_REPORT_GET_COMMENT_COUNT = "SELECT u.userid, aed.objectid, count(u.userSessionID) as COMMENT_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'COMMENT_ADDED' AND aed.userSessionID = u.userSessionID group by aed.objectid,u.userid";
//      protected final String DOC_REPORT_GET_BOOKMARK_COUNT = "SELECT u.userid, aed.objectid, count(u.userSessionID) as BOOKMARK_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'FAVORITE_added' AND aed.userSessionID = u.userSessionID group by aed.objectid,u.userid";
//      protected final String DOC_REPORT_GET_RATING_COUNT = "SELECT u.userid, aed.objectid, count(u.userSessionID) as RATING_COUNT from documentDetails aed, userSessions u  where aed.eventName = 'DOCUMENT_RATED' AND aed.userSessionID = u.userSessionID group by aed.objectid,u.userid";
//
         private final static String DOC_REPORT_GET_READ_COUNT = new String("SELECT objectid, count(usersessionid) as READ_COUNT FROM usersessionevents where eventName = 'DOCUMENT_VIEWED' and startTime >= @ AND startTime <= @ group by objectid");
         private final static String DOC_REPORT_GET_COMMENT_COUNT = new String("SELECT objectid, count(usersessionid) as COMMENT_COUNT FROM usersessionevents where eventName = 'COMMENT_ADDED' and startTime >= @ AND startTime <= @ group by objectid");
         private final static String DOC_REPORT_GET_BOOKMARK_COUNT = new String("SELECT objectid, count(usersessionid) as BOOKMARK_COUNT FROM usersessionevents where eventName = 'FAVORITE_added' and startTime >= @ AND startTime <= @ group by objectid");
         private final static String DOC_REPORT_GET_RATING_COUNT = new String("SELECT objectid, count(usersessionid) as RATING_COUNT FROM usersessionevents where eventName = 'DOCUMENT_RATED' and startTime >= @ AND startTime <= @ group by objectid");

         private final static String ALL_DOC_REPORT_GET_READ_COUNT = new String("SELECT distinct(ue.objectid), us.userid, count(ue.usersessionid) as READ_COUNT from usersessions us right join usersessionevents ue on us.usersessionid = ue.usersessionid where ue.startTime >= @ AND ue.startTime <= @ and ue.eventname = 'DOCUMENT_VIEWED' group by us.userid, ue.objectid");
         private final static String ALL_DOC_REPORT_GET_COMMENT_COUNT = new String("SELECT distinct(ue.objectid), us.userid, count(ue.usersessionid) as COMMENT_COUNT from usersessions us right join usersessionevents ue on us.usersessionid = ue.usersessionid where ue.startTime >= @ AND ue.startTime <= @ and ue.eventname = 'COMMENT_ADDED' group by us.userid, ue.objectid");
         private final static String ALL_DOC_REPORT_GET_BOOKMARK_COUNT = new String("SELECT distinct(ue.objectid), us.userid, count(ue.usersessionid) as BOOKMARK_COUNT from usersessions us right join usersessionevents ue on us.usersessionid = ue.usersessionid where ue.startTime >= @ AND ue.startTime <= @ and ue.eventname = 'FAVORITE_added' group by us.userid, ue.objectid");
         private final static String ALL_DOC_REPORT_GET_RATING_COUNT = new String("SELECT distinct(ue.objectid), us.userid, count(ue.usersessionid) as RATING_COUNT from usersessions us right join usersessionevents ue on us.usersessionid = ue.usersessionid where ue.startTime >= @ AND ue.startTime <= @ and ue.eventname = 'DOCUMENT_RATED' group by us.userid, ue.objectid");


        // private final String DOC_REPORT_GET_DOCUMENT_USERS_SQL = "select distinct(docView.ratinguser),count(docView.ratinguser) AS ACTIVITY_COUNT from documentDetails docView where docView.objectid = ? and docView.eventname = ? group by docView.ratinguser";
        private final static String DOC_REPORT_GET_DOCUMENT_USERS_SQL = new String("select us.userid AS ratinguser, count(ue.usersessionid) AS ACTIVITY_COUNT from usersessions us right join usersessionevents ue on us.usersessionid = ue.usersessionid where ue.objectid = ? and ue.eventname = ? and ue.startTime >= @ AND ue.startTime <= @ group by us.userid");


    private Map<Long, Map<String, Integer>> userReportMap = new HashMap<Long, Map<String, Integer>>();

    private Map<Long, Map<String, Integer>> documentReportMap = new HashMap<Long, Map<String, Integer>>();

    private List<ReportingMap> allUserActivityList = new ArrayList<ReportingMap>();

    public void setSequenceDAO(SequenceDAO sequenceDAO) {
        this.sequenceDAO = sequenceDAO;
    }

    /**
     * Method to add a new entry to UserSessions table - userSessions
     *
     * @param userSessionEventBean
     */
    public long insertUserSession(final SessionEventBean userSessionEventBean) {
        final long[] sessionID = new long[1];
        try {
            getJdbcTemplate().update(INSERT_USER_SESSION_SQL, new PreparedStatementSetter() {
                long userSessionID;

                @Override
                public void setValues(PreparedStatement preparedStatement) throws SQLException {
                    try {
                        // Auto seqID = sequenceDAO.nextID(2103); - New type will be created for UserSession Sequence
                        // userSessionID
                        userSessionID = sequenceDAO.nextID(EventAnalyticsUtil.USER_SESSION_TYPE);
                        preparedStatement.setLong(1, userSessionID);
                        // userID
                        preparedStatement.setLong(2, userSessionEventBean.getActorID());
                        // loginTime
                        preparedStatement.setLong(3, userSessionEventBean.getEventDate());
                        // logoutTime
                        preparedStatement.setLong(4, -1); // Will be updated once user logs out of the system or LOGGED_OUT event is fired
                        // duration
                        preparedStatement.setLong(5, -1); // Will be updated once user logs out of the system or LOGGED_OUT event is fired
                    }
                    catch (Exception e) {
                        log.info("Couldn't serialize event " + userSessionEventBean.toString() + " to analytics DB", e);
                    }
                    sessionID[0] = userSessionID;
                    log.info("GENERATED NEXT SEQ ID - " + sessionID[0]);
                }
            });
        }
        catch (DataAccessException e) {
            log.warn("Couldn't serialize " + userSessionEventBean.toString() + " event to analytics DB", e);
        }
        return (sessionID[0]);
    }

    public void updateUserSession(final SessionEventBean userSessionEventBean) {
        JdbcTemplate template = getJdbcTemplate();
        try {
            Map<String, Object> activeSessionDetails = template.queryForMap(GET_USER_ACTIVE_SESSION_DETAILS_SQL, new Object[]{userSessionEventBean.getActorID()});
            long loginTime = Long.valueOf(activeSessionDetails.get("loginTime").toString()).longValue();
            long logoutTime = userSessionEventBean.getEventDate();
            // logoutTime, duration, userID
            getJdbcTemplate().update(UPDATE_USER_SESSION_SQL, new Object[]{logoutTime, (logoutTime - loginTime), userSessionEventBean.getActorID()});
        }
        catch (EmptyResultDataAccessException e) {
            log.warn("No entry found for  - " + userSessionEventBean.toString() + " event", e);
        }
        catch (DataAccessException e) {
            log.warn("Couldn't serialize " + userSessionEventBean.toString() + " event to analytics DB", e);
        }
    }

    /**
     * Method to insert a batch of user events in a particular session - userSessionEvents
     *
     * @param events
     */
    public void insertUserSessionEvents(Collection<SessionEventBean> events) {
        final List<SessionEventBean> eventsToInsert = new ArrayList<SessionEventBean>(events);
        final JdbcTemplate jdbcTemplate = getJdbcTemplate();
        try {
            jdbcTemplate.batchUpdate(INSERT_USER_SESSION_EVENT_SQL, new BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    SessionEventBean eventBean = eventsToInsert.get(i);
                    long userSessionID = fetchLatestSessionID(eventBean.getActorID());
                    try {
                        ps.setLong(1, sequenceDAO.nextID(EventAnalyticsUtil.USER_SESSION_EVENT_TYPE));
                        ps.setLong(2, userSessionID);
                        ps.setLong(3, eventBean.getDirectObjectID());
                        ps.setInt(4, eventBean.getDirectObjectType());
                        ps.setLong(5, eventBean.getContainerObjectID());
                        ps.setLong(6, eventBean.getContainerObjectType());
                        ps.setInt(7, eventBean.getID());
                        ps.setString(8, eventBean.getName());
                        ps.setString(9, eventBean.getUrl());
                        ps.setLong(10, (Long) eventBean.getEventParams().get("ratingUserID"));
                        ps.setInt(11, (Integer) eventBean.getEventParams().get("ratingScore"));
                        ps.setLong(12, eventBean.getEventDate());
                        // if type is bookmark or comment update the event endTime
                        if (StringUtils.containsAnyOf(eventBean.getName(), "COMMENT_ADDED", "FAVORITE_added")) {
                            ps.setLong(13, eventBean.getEventDate());
                        } else {
                            ps.setLong(13, -1);
                        }
                    }
                    catch (Exception e) {
                        log.warn("Couldn't serialize event " + eventBean + " to analytics DB", e);
                    }
                }

                public int getBatchSize() {
                    return eventsToInsert.size();
                }
            });
            log.info("Updated session events  - " + eventsToInsert.size() + " to analytics db");
        }
        catch (DataAccessException e) {
            log.warn("Couldn't serialize " + eventsToInsert.size() + " events to analytics DB", e);
        }

    }

    public void updateEventEndTime(long userID, int objectType, long objectID, int containerType, long containerID, String url) {
        long currentTime = EventAnalyticsUtil.getCurrentLongTime(new Date());
        long userSessionID = -1;
        Map<String, Object> activeSessionDetails;
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        try {
            activeSessionDetails = jdbcTemplate.queryForMap(GET_USER_ACTIVE_SESSION_DETAILS_SQL, new Object[]{userID});
            userSessionID = (Long) activeSessionDetails.get("userSessionID");
            log.info("Previous user session log-out was successfull - " + userSessionID);
        } catch (IncorrectResultSizeDataAccessException incorrestEx) {
            userSessionID = fetchLatestSessionID(userID);
            log.info("Previous user session log-out was NOT successfull - " + userSessionID);
            // Update the previuous session logout time with current time
            int upCnt = jdbcTemplate.update("UPDATE USERSESSIONS SET logoutTime = ? WHERE logoutTime = -1 " +
                    "AND duration = -1 AND userID = ? AND NOT userSessionID = ?", new Object[]{currentTime, userID, userSessionID});
            if (upCnt > 0) {
                log.info("Sucessfully updated " + upCnt + " records which had invalid logout time");
            } else {
                log.info("Error while updating records which had invalid logout time");
            }
        } catch (DataAccessException dataAEx) {
            log.info("Error while updating session events endTime [ currentTime: " + currentTime + "\n objectType" +
                    objectType + "\nobjectID: " + objectID + "\ncontainerType: " + containerType + "\n containerID " + containerID +
                    "\nurl " + url + " ]");
        }
        jdbcTemplate.update(UPDATE_USER_SESSION_EVENT_ENDTIME_SQL, new Object[]{currentTime, objectID, objectType,
                containerID, containerType, userSessionID});
    }

    private long fetchLatestSessionID(long userID) {
        long userSessionID = -1;
        List<Map<String, Long>> recList = (List) getJdbcTemplate().queryForList(GET_USER_ACTIVE_SESSION_DETAILS_DESC_LOGINTIME_SQL, new Object[]{(Long) userID});
        userSessionID = Long.valueOf(recList.get(0).get("userSessionID")).longValue();
        return userSessionID;
    }

    public int getFilteredUserReportResultsCount(Date dateStart, Date dateEnd) {
        return getSimpleJdbcTemplate().queryForInt("SELECT count(*) from AggEventDetails where startTime >= ? AND endTime <= ?", dateStart, dateEnd);
    }

    /* User report
    *   UserReportMap<Long, ActivityMap<String, Integer>> -->  
    *                   <user_id, ActivityMap<activty_name, activty_count>>
    *   UserReportMap<1,ActivityMap>
    * 
    *       ActivityMap<String, Integer> -
    *               <'READ_COUNT', 20>
    *               <'COMMENT_COUNT', 20>
    *               <'RATING_COUNT', 20>
    *               <'BOOKMARK_COUNT', 20>
    *
    * */
    public Map<Long, Map<String, Integer>> getUserEventsReport(Date dateStart, Date dateEnd, int startIndex, int numResults) {
        JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
        List<Map<String, Object>> recordsList = new ArrayList<Map<String, Object>>();
        PreparedStatement stm = null;
        Connection conn = null;
        // 1) Create a view which will store all event detail records in a VIEW
        // 2) Query the view to fetch the individual event counts and update the map with details
        // 3) Prepare a final map
        // 4) Return the map to render on UI

        userReportMap.clear();
        String finalQuery = USER_REPORT_CREATE_VIEW_STATEMENT;

        finalQuery = StringUtils.replaceFirst(finalQuery, "__", String.valueOf(EventAnalyticsUtil.getCurrentLongTime(dateStart)).toString());
        finalQuery = StringUtils.replaceFirst(finalQuery, "___", String.valueOf(EventAnalyticsUtil.getCurrentLongTime(dateEnd)).toString());
        log.info("User Report VIEW statement : " + finalQuery);
        getSimpleJdbcTemplate().getJdbcOperations().execute(finalQuery);
        //update(finalQuery);
        try {
            // GET READ count
            recordsList = getSimpleJdbcTemplate().queryForList(USER_REPORT_GET_READ_COUNT);
            updateUserReportMap(recordsList, "READ_COUNT");

            // GET COMMENTS count
            recordsList.clear();
            recordsList = getSimpleJdbcTemplate().queryForList(USER_REPORT_GET_COMMENT_COUNT);
            updateUserReportMap(recordsList, "COMMENT_COUNT");

            // GET BOOKMARKS count
            recordsList.clear();
            recordsList = getSimpleJdbcTemplate().queryForList(USER_REPORT_GET_BOOKMARK_COUNT);
            updateUserReportMap(recordsList, "BOOKMARK_COUNT");

            // GET RATINGS COUNT
            recordsList.clear();
            recordsList = getSimpleJdbcTemplate().queryForList(USER_REPORT_GET_RATING_COUNT);
            updateUserReportMap(recordsList, "RATING_COUNT");
        } catch (DataAccessException daEx) {
            log.info("ERROR: While retrieving the data for user  report - " + daEx.getMessage());
            daEx.printStackTrace();
        }

        log.info("User report data > " + userReportMap);
        if ((userReportMap.size() > 0)) {
            return userReportMap;
        } else {
            // If the control comes here means there are no records updated in VIEW so return an empty map
            return Collections.emptyMap();
        }
    }

    /* User report - DWR  */
    public List<Map<String, String>> getDWRUserReport(String strDateStart, String strDateEnd, long userID, String activityType, int startIndex, int numResults) {
        Date dateStart = null, dateEnd = null;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            dateStart = df.parse(strDateStart);
            dateEnd = df.parse(strDateEnd);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        log.info("USER DRILL DOWN QUERY  : " + GET_USER_DOCUMENT_EVENTS_COUNT + " - " + EventAnalyticsUtil.getCurrentLongTime(dateStart) + " - " + dateEnd.getTime() + " - uID " + userID + " - " + activityType);
        List<Map<String, String>> paginatedUserDocumentsList = fetchUserDrillDownReports(startIndex, numResults, dateStart, dateEnd, userID, activityType, true);
        return paginatedUserDocumentsList;
    }

    
    public List<Map<String, String>> fetchUserDrillDownReports(int startIndex, int numResults, Date dateStart, Date dateEnd, long userID,
           String activityType, boolean pagination){
        JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
        List<Map<String, String>> userReportDrillDownList = null;
        if(pagination){
              userReportDrillDownList = template.queryScrollable(GET_USER_DOCUMENT_EVENTS_COUNT,
                        startIndex, numResults, genericRowMapper, EventAnalyticsUtil.getCurrentLongTime(dateStart), EventAnalyticsUtil.getCurrentLongTime(dateEnd),
                        userID, activityType);
        }else{
            userReportDrillDownList =  (List) getJdbcTemplate().queryForList(GET_USER_DOCUMENT_EVENTS_COUNT, new Object[]{EventAnalyticsUtil.getCurrentLongTime(dateStart), EventAnalyticsUtil.getCurrentLongTime(dateEnd), userID, activityType});
        }
        return userReportDrillDownList;
    }

    /**
     * Method returns the total records count for User Drill down report
     * @param userID
     * @param activityType
     * @param dateStart
     * @param dateEnd
     * @return
     */
    public int getUserReportDrillDownCount(long userID, String activityType, Date dateStart, Date dateEnd){
        log.info("User Drill down count - " + GET_USER_DOCUMENT_EVENTS_COUNT + " -- " + EventAnalyticsUtil.getCurrentLongTime(dateStart)
                        + " -- " + EventAnalyticsUtil.getCurrentLongTime(dateEnd) + " UID - " + userID + " ActType - " + activityType);
        List<Map<String, String>> userReportDrillDownList =  (List) getJdbcTemplate().queryForList(GET_USER_DOCUMENT_EVENTS_COUNT,
                new Object[]{EventAnalyticsUtil.getCurrentLongTime(dateStart),
                             EventAnalyticsUtil.getCurrentLongTime(dateEnd), userID, activityType});
        return(userReportDrillDownList.size());
    }


    /* Document report */
    public Map<Long, Map<String, Integer>> getDocumentEventsReport(Date dateStart, Date dateEnd, int startIndex, int numResults) {
        JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
        List<Map<String, Object>> recordsList = new ArrayList<Map<String, Object>>();
        PreparedStatement stm = null;
        Connection conn = null;

        documentReportMap.clear();
        //String finalQuery = DOC_REPORT_CREATE_VIEW;

       // finalQuery = StringUtils.replaceFirst(finalQuery, "__", String.valueOf(EventAnalyticsUtil.getCurrentLongTime(dateStart)).toString());
       // finalQuery = StringUtils.replaceFirst(finalQuery, "___", String.valueOf(EventAnalyticsUtil.getCurrentLongTime(dateEnd)).toString());
        //log.info("Document Report VIEW statement : " + finalQuery);
        //getSimpleJdbcTemplate().getJdbcOperations().execute(finalQuery);
        try {
            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(DOC_REPORT_GET_READ_COUNT),dateStart,dateEnd));
            updateDocumentReportMap(recordsList, "READ_COUNT");
            recordsList.clear();

            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(DOC_REPORT_GET_COMMENT_COUNT),dateStart,dateEnd));
            updateDocumentReportMap(recordsList, "COMMENT_COUNT");

            recordsList.clear();
            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(DOC_REPORT_GET_BOOKMARK_COUNT),dateStart,dateEnd));
            updateDocumentReportMap(recordsList, "BOOKMARK_COUNT");

            recordsList.clear();
            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(DOC_REPORT_GET_RATING_COUNT),dateStart,dateEnd));
            updateDocumentReportMap(recordsList, "RATING_COUNT");
            //log.info("AFTER RATING_COUNT UPDATE" + userReportMap.toString());
            //userReportMap = template.queryScrollable(CREATE_VIEW_STATEMENT, startIndex, numResults, genericRowMapper, dateStart, dateEnd);
        } catch (DataAccessException daEx) {
            log.info("ERROR: While retrieving the data - " + daEx.getMessage());
            daEx.printStackTrace();
        }

        log.info("Document report data > " + documentReportMap);
        if ((documentReportMap.size() > 0)) {
            return documentReportMap;
        } else {
            // If the control comes here means there are no records updated in VIEW so return an empty map
            return Collections.emptyMap();
        }
    }


    /* Document report */
    public List<ReportingMap> getAllUserActivitiesReport(Date dateStart, Date dateEnd) {
       // JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
        List<Map<String, Object>> recordsList = new ArrayList<Map<String, Object>>();
        PreparedStatement stm = null;
        Connection conn = null;

        allUserActivityList.clear();

        //String finalQuery = DOC_REPORT_CREATE_VIEW;

       // finalQuery = StringUtils.replaceFirst(finalQuery, "__", String.valueOf(EventAnalyticsUtil.getCurrentLongTime(dateStart)).toString());
       // finalQuery = StringUtils.replaceFirst(finalQuery, "___", String.valueOf(EventAnalyticsUtil.getCurrentLongTime(dateEnd)).toString());
        //log.info("Document Report VIEW statement : " + finalQuery);
        //getSimpleJdbcTemplate().getJdbcOperations().execute(finalQuery);
        try {
            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(ALL_DOC_REPORT_GET_READ_COUNT),dateStart,dateEnd));
            updateAllUserActivitiesReportMap(recordsList, "READ_COUNT");
            recordsList.clear();

            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(ALL_DOC_REPORT_GET_COMMENT_COUNT),dateStart,dateEnd));
            updateAllUserActivitiesReportMap(recordsList, "COMMENT_COUNT");

            recordsList.clear();
            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(ALL_DOC_REPORT_GET_BOOKMARK_COUNT),dateStart,dateEnd));
            updateAllUserActivitiesReportMap(recordsList, "BOOKMARK_COUNT");

            recordsList.clear();
            recordsList = getSimpleJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(ALL_DOC_REPORT_GET_RATING_COUNT),dateStart,dateEnd));
            updateAllUserActivitiesReportMap(recordsList, "RATING_COUNT");
            //log.info("AFTER RATING_COUNT UPDATE" + userReportMap.toString());
            //userReportMap = template.queryScrollable(CREATE_VIEW_STATEMENT, startIndex, numResults, genericRowMapper, dateStart, dateEnd);
        } catch (DataAccessException daEx) {
            log.info("ERROR: While retrieving the data - " + daEx.getMessage());
            daEx.printStackTrace();
        }

        log.info("Document report data > " + documentReportMap);
        if ((allUserActivityList.size() > 0)) {
            return allUserActivityList;
        } else {
            // If the control comes here means there are no records updated in VIEW so return an empty map
            return Collections.emptyList();
        }
    }

    /* Document report - DWR  */
    public List<Map<String, String>> getDWRDocumentReport(String strDateStart, String strDateEnd, long userID, String activityType, int startIndex, int numResults) {
        Date dateStart = null, dateEnd = null;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            dateStart = df.parse(strDateStart);
            dateEnd = df.parse(strDateEnd);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<Map<String, String>> paginatedUserDocumentsList = fetchDocumentDrillDownReports(userID, activityType, startIndex, numResults, dateStart, dateEnd, true);
        return paginatedUserDocumentsList;
    }

    public List<Map<String, String>> fetchDocumentDrillDownReports(long userID, String activityType,
                    int startIndex, int numResults, Date dateStart, Date dateEnd, boolean pagination) {
        JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
        List<Map<String, String>> documentReportDrillDownList = null;
        if(pagination){
            log.info("DOC DRILL DOWN QUERY : " + DOC_REPORT_GET_DOCUMENT_USERS_SQL + " - " + EventAnalyticsUtil.getCurrentLongTime(dateStart) + " - " + dateEnd.getTime() + " - uID " + userID + " - " + activityType);
            documentReportDrillDownList = template.queryScrollable(setStartAndEndDate(new StringBuffer(DOC_REPORT_GET_DOCUMENT_USERS_SQL),dateStart,dateEnd),
                startIndex, numResults, genericRowMapper, userID, activityType);
        }else{
            documentReportDrillDownList = (List) getJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(DOC_REPORT_GET_DOCUMENT_USERS_SQL),dateStart,dateEnd), new Object[] {
                userID, activityType});    
        }
        log.info("FINAL DOC DRILL DOWN LIST >>  " + documentReportDrillDownList);
        return documentReportDrillDownList;
    }

    /**
     * Method returns the total records count for Document Drill down report
     * 
     * @param userID
     * @param activityType
     * @return
     */
    public int getDocumentReportDrillDownCount(long userID, String activityType, Date startDate, Date endDate){
        log.info("Document Drill down count - " + GET_USER_DOCUMENT_EVENTS_COUNT + " -- UID -- " + userID + " ActType - " + activityType);
        List<Map<String, String>> documentReportDrillDownList = (List) getJdbcTemplate().queryForList(setStartAndEndDate(new StringBuffer(DOC_REPORT_GET_DOCUMENT_USERS_SQL),startDate,endDate), new Object[] {
                userID, activityType}); 
        return(documentReportDrillDownList.size());
    }

    /**
     * Method to update the final report map  with updated READ, COMMENT, BOOKMARK and RATING counts
     * //@param userReportMap - Map data ( <userID, Map<eventName, count>>) with the existing values
     *
     * @param recordsList - Records list(userID and event_count) for all users pertaining to an event - eventType
     * @param eventType   - Event name whose count should be updated
     * @return - MAP - Updated userReportMap
     */
    private Map<Long, Map<String, Integer>> updateUserReportMap(List<Map<String, Object>> recordsList, String eventType) {
        for (Map recData : recordsList) {
            //Map<String, Integer> eventCountDetailsMap = new HashMap<String, Integer>();
             ReportingMap eventCountDetailsMap = new ReportingMap();
            long userID = (Long) recData.get("userID");
            int eventCount = Integer.valueOf(recData.get(eventType).toString()).intValue();
           
            eventCountDetailsMap.put(eventType, eventCount);
            if (userReportMap.containsKey(userID) && userReportMap.get(userID) != null) {
                userReportMap.get(userID).putAll(eventCountDetailsMap);
            } else {
                userReportMap.put(userID, eventCountDetailsMap);
            }
        }
        return userReportMap;
    }

    private Map<Long, Map<String, Integer>> updateDocumentReportMap(List<Map<String, Object>> recordsList, String eventType) {
        for (Map recData : recordsList) {
            //Map<String, Integer> eventCountDetailsMap = new HashMap<String, Integer>();
            ReportingMap eventCountDetailsMap = new ReportingMap();
            long objectID = (Long) recData.get("objectid");
            int eventCount = Integer.valueOf(recData.get(eventType).toString()).intValue();
            //String eventCount = recData.get(eventType).toString(); 
            // override the eventcount if - eventType = 'RATING_COUNT'
            if(eventType.equals("RATING_COUNT")){
               double meanRating = EventAnalyticsUtil.getMeanRating(EventAnalyticsUtil.loadJiveObject(JiveConstants.DOCUMENT, objectID));
               log.info(objectID + " == Mean Rating - " + meanRating); 
               eventCount = Double.valueOf(meanRating).intValue();
               log.info("Integer Mean Rating - " + eventCount); 
            }
//            long userID = (Long) recData.get("userid");
//            eventCountDetailsMap.put("userid",userID);

            eventCountDetailsMap.put(eventType, eventCount);
            if (documentReportMap.containsKey(objectID) && documentReportMap.get(objectID) != null) {
                documentReportMap.get(objectID).putAll(eventCountDetailsMap);
            } else {
                documentReportMap.put(objectID, eventCountDetailsMap);
            }
        }
        return documentReportMap;
    }

      private List updateAllUserActivitiesReportMap(List<Map<String, Object>> recordsList, String eventType) {

        for (Map recData : recordsList) {
            //Map<String, Integer> eventCountDetailsMap = new HashMap<String, Integer>();
            long userID = (Long) recData.get("userid");
            long objectID = (Long) recData.get("objectid");
            int eventCount = Integer.valueOf(recData.get(eventType).toString()).intValue();
            //String eventCount = recData.get(eventType).toString();
            // override the eventcount if - eventType = 'RATING_COUNT'
            if(eventType.equals("RATING_COUNT")){
               Document docObj = (Document) EventAnalyticsUtil.loadJiveObject(JiveConstants.DOCUMENT, objectID);
               double meanRating = EventAnalyticsUtil.getUserRating(userID,docObj);
               log.info(objectID + " == Mean Rating - " + meanRating);
               eventCount = Double.valueOf(meanRating).intValue();
               log.info("Integer Mean Rating - " + eventCount);
            }

            boolean insertFlag = false;

            if(allUserActivityList != null && !allUserActivityList.isEmpty())
            {
                     for( ReportingMap allUserActivityMap : allUserActivityList)
                     {
                         long uid = (Long)allUserActivityMap.get("userid");
                         long objid = (Long)allUserActivityMap.get("objectid");
                         if(userID == uid && objectID == objid)
                         {
                             allUserActivityMap.put(eventType, eventCount);
                             insertFlag = true;
                             break;
                         }
                     }
            }
            if(!insertFlag)
            {
            ReportingMap eventCountDetailsMap = new ReportingMap();
            eventCountDetailsMap.put("userid",userID);
            eventCountDetailsMap.put("objectid",objectID);
            eventCountDetailsMap.put(eventType, eventCount);

            allUserActivityList.add(eventCountDetailsMap);
            }
        }
        return allUserActivityList;  

    }
    /**
     * Reusable row mapper for mapping a drupal DB result set to a Map
     */
    private final ParameterizedRowMapper<Map<String, String>> genericRowMapper = new ParameterizedRowMapper<Map<String, String>>() {
        public Map<String, String> mapRow(ResultSet rs, int row) throws SQLException {
            ResultSetMetaData metaData = rs.getMetaData();
            Map<String, String> rowData = new HashMap<String, String>();
            String columnValue = "";
            // loop through all columns and update the map with column name and its values
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                switch (metaData.getColumnType(i)) {
                    /* Char types */
                    case Types.CHAR:
                    case Types.NCHAR:
                        break;
                    /* String types */
                    case Types.LONGNVARCHAR:
                    case Types.NVARCHAR:
                    case Types.VARCHAR:
                        columnValue = rs.getString(i);
                        break;
                    /* Integer types */
                    case Types.NUMERIC:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.TINYINT:
                        columnValue = String.valueOf(rs.getInt(i)).toString();
                        break;
                    /* BIGINT types */
                    case Types.BIGINT:
                        columnValue = String.valueOf(rs.getLong(i)).toString();
                        break;
                    /* DOUBLE types */
                    case Types.DOUBLE:
                        columnValue = String.valueOf(rs.getDouble(i)).toString();
                        break;
                    /* FLOAT types */
                    case Types.FLOAT:
                        columnValue = String.valueOf(rs.getFloat(i)).toString();
                        break;
                    /* BOOLEAN types */
                    case Types.BOOLEAN:
                        columnValue = String.valueOf(rs.getBoolean(i)).toString();
                        break;
                    /* BLOB, CLOB types */
                    case Types.BLOB:
                    case Types.CLOB:
                        //columnValue = String.valueOf(rs.getBoolean(i)).toString();
                        // do nothing - There wont be any user data with this type
                        break;
                    /* TIMESTAMP types */
                    case Types.TIMESTAMP:
                        columnValue = String.valueOf(rs.getTimestamp(i)).toString();
                        break;
                    /* TIME types */
                    case Types.TIME:
                        columnValue = String.valueOf(rs.getTime(i)).toString();
                        break;
                    /* DATE types */
                    case Types.DATE:
                        columnValue = String.valueOf(rs.getDate(i)).toString();
                        break;
                }
                // Push the column value to MAP
                rowData.put(metaData.getColumnName(i), columnValue);
            }
            return rowData;
        }
    };

    private boolean doesViewExists(String viewTableName, String[] tableTypes) {
        ResultSet rs = null;
        Connection con = getConnection();
        try {

            // original case
            rs = con.getMetaData().getTables(null, null, viewTableName, tableTypes);

            if (rs.next()) {
                return true;
            }

            rs.close();

            // lower case
            rs = con.getMetaData().getTables(null, null, viewTableName.toLowerCase(), tableTypes);

            if (rs.next()) {
                return true;
            }

            rs.close();

            // upper case
            rs = con.getMetaData().getTables(null, null, viewTableName.toUpperCase(), tableTypes);

            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            log.error("Error getting metadata for table", e);
        }
        finally {
            ConnectionManager.close(rs);
            ConnectionManager.close(con);
        }

        return false;
    }

    /**
     * Method to update the favorite event record in userSessionEvents table when the user remove the 'Bookmarks' for a particular object
     * @param sessionBean
     * @param eventName
     */
    public void updateUserSessionEventForEvent(SessionEventBean sessionBean, String eventName) {
        /*
            containerType=14,
	        commentCreationDate=1274306626515,
	        userID=2026,
            commentResourceID=1491,  - objectid
            commentResourceType=102,  - objecttype
            containerID=2489
        */
//        // Update SQL for Bookmark event
//        String GET_FAVORITE_EVENT_RECORD_SESSION_ID = "SELECT usereventid FROM userSessionEvents WHERE eventname = 'FAVORITE_added' AND objectid = ? AND containerid = ? AND containertype = ?";
//        String UPDATE_FAVORITE_EVENT_RECORD = "UPDATE userSessionEvents SET eventname = 'FAVORITE_removed' WHERE usereventid = ?";
//
//        // Update SQL for Document event
//        String GET_DOCUMENT_EVENT_RECORD_SESSION_ID = "SELECT usereventid FROM userSessionEvents WHERE eventname = 'DOCUMENT_VIEWED' AND objectid = ? AND containerid = ? AND containertype = ?";
//        String UPDATE_DOCUMENT_EVENT_RECORD = "UPDATE userSessionEvents SET eventname = 'DOCUMENT_DELETED' WHERE usereventid = ?";
//
//        // Update SQL for Comment event
//        String GET_COMMENT_EVENT_RECORD_SESSION_ID = "SELECT usereventid FROM userSessionEvents WHERE eventname = 'COMMENT_ADDED' AND objectid = ? AND containerid = ? AND containertype = ?";
//        String UPDATE_COMMENT_EVENT_RECORD = "UPDATE userSessionEvents SET eventname ='COMMENT_DELETED' WHERE usereventid = ?";
        // Update SQL for Bookmark event
        String GET_FAVORITE_EVENT_RECORD_SESSION_ID = "SELECT ue.usereventid FROM usersessions us right join usersessionevents ue on us.usersessionid = ue.usersessionid WHERE ue.eventname = 'FAVORITE_added' AND ue.objectid = ? AND ue.containertype = ? AND ue.containerid = ? AND us.userid = ?";
        String UPDATE_FAVORITE_EVENT_RECORD = "UPDATE userSessionEvents SET eventname = 'FAVORITE_removed' WHERE usereventid = ?";

        // Update SQL for Document event
        String GET_DOCUMENT_EVENT_RECORD_SESSION_ID = "SELECT usereventid FROM usersessionevents WHERE eventname = 'DOCUMENT_VIEWED' AND objectid = ? AND containertype = ? AND containerid = ?";
        String UPDATE_DOCUMENT_EVENT_RECORD = "UPDATE userSessionEvents SET eventname = 'DOCUMENT_DELETED' WHERE usereventid = ?";

        // Update SQL for Comment event
        String GET_COMMENT_EVENT_RECORD_SESSION_ID = "SELECT ue.usereventid FROM usersessions us right join usersessionevents ue on us.usersessionid = ue.usersessionid WHERE ue.eventname = 'COMMENT_ADDED' AND ue.objectid = ? AND ue.containertype = ? AND ue.containerid = ? AND us.userid = ?";
        String UPDATE_COMMENT_EVENT_RECORD = "UPDATE userSessionEvents SET eventname ='COMMENT_DELETED' WHERE usereventid = ?";

        JiveJdbcOperationsTemplate template = new JiveJdbcOperationsTemplate(getSimpleJdbcTemplate());
        try{
            int sessionEventID = -1;
            int totalUpdateRecordsCnt = 0;
            List<Map<String, Object>> sessionIDList = null;
            long containerID = sessionBean.getContainerObjectID();
            int containerType = sessionBean.getContainerObjectType();
            long objectID = sessionBean.getDirectObjectID();
            long userId = sessionBean.getActorID();
            if(eventName.equals("FAVORITE_deleted")) {
                sessionIDList = getSimpleJdbcTemplate().queryForList(GET_FAVORITE_EVENT_RECORD_SESSION_ID, objectID , containerType, containerID,userId);
                log.info("QUERY : " + GET_FAVORITE_EVENT_RECORD_SESSION_ID + " -- " + objectID + " -- " + containerType + " -- " + containerID);
                log.info("Update these session events row with ID - " + sessionIDList.toString());
                for(int i=0;i< sessionIDList.size(); i++){
                    template.update(UPDATE_FAVORITE_EVENT_RECORD, (Long)sessionIDList.get(i).get("usereventid"));
                }
            }else if(eventName.equals("DOCUMENT_DELETED")){
                sessionIDList =  template.queryForList(GET_DOCUMENT_EVENT_RECORD_SESSION_ID, objectID , containerType, containerID);
                log.info("QUERY : " + GET_COMMENT_EVENT_RECORD_SESSION_ID + " -- " + objectID + " -- " + containerType + " -- " + containerID);
                log.info("Update these session events row with ID - " + sessionIDList.toString());
                for(int i=0;i< sessionIDList.size(); i++){
                    template.update(UPDATE_DOCUMENT_EVENT_RECORD, (Long)sessionIDList.get(i).get("usereventid"));
                }
            }else if(eventName.equals("COMMENT_DELETED")){
                // Comment Type event - Params [ containerID, containerType, commentResourceID, commentResourceType, userID ]
                sessionIDList = getSimpleJdbcTemplate().queryForList(GET_COMMENT_EVENT_RECORD_SESSION_ID, objectID , containerType, containerID,userId);
                log.info("QUERY : " + GET_COMMENT_EVENT_RECORD_SESSION_ID + " -- " + objectID + " -- " + containerType + " -- " + containerID );
                log.info("Update these session events row with ID - " + sessionIDList.toString());
                for(int i=0;i< sessionIDList.size(); i++){
                    template.update(UPDATE_COMMENT_EVENT_RECORD , (Long)sessionIDList.get(i).get("usereventid"));
                }
            }
//            if(upDateCount > 0){
//                log.info("'" + eventName + "' event record for user - "+ sessionBean.getActorID() +" Document - " + sessionBean.getDirectObjectID() +"  in 'userSessionEvents' table was updated sucessfully");
//            }else{
//                log.info("Error: No records were updated for the '" + eventName+ "' event record for user - "+ sessionBean.getActorID() +" in 'userSessionEvents'.");
//            }
        }catch (DataAccessException daEx){
            log.info("Error; While updating the '" + eventName + "' event record for user - "+ sessionBean.getActorID() +" in 'userSessionEvents'. " + daEx.getMessage());            
        }
    }
    
    private String setStartAndEndDate(StringBuffer queryStrBfr, Date startDate, Date endDate){

        int index = queryStrBfr.indexOf("@");
        queryStrBfr.replace(index,index+1,String.valueOf(EventAnalyticsUtil.getCurrentLongTime(startDate)));
        index = queryStrBfr.indexOf("@");
        queryStrBfr.replace(index,index+1,String.valueOf(EventAnalyticsUtil.getCurrentLongTime(endDate)));

        return queryStrBfr.toString();
    }
   
}
