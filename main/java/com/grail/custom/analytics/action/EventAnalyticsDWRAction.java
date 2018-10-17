package com.grail.custom.analytics.action;

import com.grail.custom.analytics.EventAnalyticsUtil;
import com.grail.custom.analytics.dao.EventAnalyticsDAO;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.action.util.Pageable;
import com.jivesoftware.community.action.util.Paginator;
import com.jivesoftware.community.favorites.Favorite;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.community.web.struts.SetReferer;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Decorate(false)
@SetReferer(false)
public class EventAnalyticsDWRAction extends JiveActionSupport implements Pageable{

    private Map<String, Object> drillDownMapReport = new HashMap<String, Object>();

    // Pagination
    private int start;
    private int numResults;
    private int totalItemCount;

    // Date params will be of string type
    private String dateStart;
    private String dateEnd;



    // Spring injection
    private EventAnalyticsDAO eventAnalyticsDAO;

    // User drill down params
    private long userID;
    private String activityType;


    public Map<String, Object> getDrillDownMapReport() {
        return drillDownMapReport;
    }
    @TypeConversion(converter = "com.jivesoftware.community.util.DateFormatConverter")
    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }
    @TypeConversion(converter = "com.jivesoftware.community.util.DateFormatConverter")
    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

//    public String getDateEnd() {
//        return dateEnd;
//    }
//
//    public String getDateStart() {
//        return dateStart;
//    }

    public void setEventAnalyticsDAO(EventAnalyticsDAO eventAnalyticsDAO) {
        this.eventAnalyticsDAO = eventAnalyticsDAO;
    }
    
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }


    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String userDrillDownReport(){
        drillDownMapReport.clear();
        //((numResults == 0)? JiveGlobals.getJiveIntProperty("grail.analytics.pagination.results", 10):numResults)
        drillDownMapReport = getUserDrillDownMap(dateStart, dateEnd, userID, activityType, start, getNumResults());
        drillDownMapReport.put("obj_id", userID);
        drillDownMapReport.put("act_type", activityType);
        totalItemCount = eventAnalyticsDAO.getUserReportDrillDownCount(userID, activityType, EventAnalyticsUtil.convertStringToDate(dateStart),
                                            EventAnalyticsUtil.convertStringToDate(dateEnd));
        return SUCCESS;
    }

    public String documentDrillDownReport(){
        drillDownMapReport.clear();
        drillDownMapReport = getDocDrillDownMap(dateStart, dateEnd, userID, activityType, start, getNumResults());
        drillDownMapReport.put("obj_id", userID);
        drillDownMapReport.put("act_type", activityType);
        totalItemCount = eventAnalyticsDAO.getDocumentReportDrillDownCount(userID, activityType,parseDate(dateStart),parseDate(dateEnd));
        return SUCCESS;
    }


    /* Helper methods */
    /**
     * Method which return MAP data for User Drill
     * @param strDateStart
     * @param strDateEnd
     * @param userID
     * @param activityType
     * @param startIndex
     * @param numResults
     * @return - MAP containing USER DRILL DOWN the drill down report
     */
    public Map<String, Object> getUserDrillDownMap(String strDateStart, String strDateEnd, long userID, String activityType, int startIndex, int numResults) {
        List<Map<String, String>> paginatedUserReportMap = new ArrayList<Map<String, String>>();
        paginatedUserReportMap = eventAnalyticsDAO.getDWRUserReport(strDateStart, strDateEnd, userID, activityType, startIndex, numResults);
        return (EventAnalyticsUtil.getDrillDownPaginatedReport(userID, JiveConstants.USER, activityType, paginatedUserReportMap));
    }

    /**
     * Method which return MAP data for Document Drill
     * @param strDateStart
     * @param strDateEnd
     * @param userID
     * @param activityType
     * @param startIndex
     * @param numResults
     * @return  - MAP which contains DOCUMENT DRILL DOWN drill down report
     */
    public Map<String, Object> getDocDrillDownMap(String strDateStart, String strDateEnd, long userID, String activityType, int startIndex, int numResults) {
        List<Map<String, String>> paginatedUserReportMap = new ArrayList<Map<String, String>>();
        paginatedUserReportMap = eventAnalyticsDAO.getDWRDocumentReport(strDateStart, strDateEnd, userID, activityType, startIndex, numResults);
        return (EventAnalyticsUtil.getDrillDownPaginatedReport(userID, JiveConstants.DOCUMENT, activityType, paginatedUserReportMap));
    }


    /* PAGINATION */
    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getTotalItemCount() {
        return totalItemCount;
    }

    @Override
    public int getNumResults() {
        numResults = ((numResults == 0)? JiveGlobals.getJiveIntProperty("grail.analytics.pagination.results", 10):numResults);
        return numResults;
    }
    
    public Paginator getNewPaginator() {
           return new Paginator(this);
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }
     private Date parseDate(String date){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {

            return df.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
