package com.grail.custom.analytics.action;

import com.grail.custom.analytics.EventAnalyticsUtil;
import com.grail.custom.analytics.dao.EventAnalyticsDAO;
import com.grail.custom.analytics.util.ReportingMap;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.database.dao.JiveJdbcOperationsTemplate;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Pageable;
import com.jivesoftware.community.action.util.Paginator;
import com.jivesoftware.community.favorites.Favorite;

import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
//import com.opensymphony.xwork2.util.TypeConversionException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts2.util.StrutsTypeConverter;
import org.springframework.beans.factory.annotation.Required;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author samee
 * @Date Apr 20, 2010
 * @Time 1:23:13 PM
 * @Version 1.0
 */
public class EventAnalyticsAction extends JiveActionSupport implements Pageable {

    public enum SortType {
        NAME(0), READ_COUNT(1), COMMENT_COUNT(2), BOOKMARK_COUNT(3), RATING_COUNT(4);

        private final int id;

        private SortType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static SortType getById(int id) {
            for (SortType status : values()) {
                if (status.id == id) {
                    return status;
                }
            }

            throw new IllegalArgumentException("Specified id does not relate to a valid status");
        }
    }

    private static final Logger log = LogManager.getLogger(EventAnalyticsAction.class);

    // place holder for all reports generated
    private Map<Long, Map<String, Integer>> analyticsReportMap = new HashMap<Long, Map<String, Integer>>();

    // Report query data
    private Date dateStart;  // Start date for report querying
    private Date dateEnd;  // End date for report querying

    // Pagination
    private int start;
    private int numResults;
    private int totalItemCount;

    // Spring injection
    private EventAnalyticsDAO eventAnalyticsDAO;


    private int exportType = 0;   // 0- XLS, 1 - XLSX, 2 - CSV

    private int sortField;
    // 0 - Name/Document (default)
    // 1 - Read Count
    // 2 - Comment Count
    // 3 - Bookmark Count
    // 4 - Rating Count
    private int sortOrder;
    // 0 - Ascending (default)
    // 1 - Descending


    /* Download report */
    protected InputStream exportStream;
    private String exportFilename;

    // User drill down params
    private long userID;
    private String activityType;



    /* Sorting */
    public void setSortField(int sortField) {
        this.sortField = sortField;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSortField() {
        return sortField;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    /* Download drill down reports */

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

    public void setEventAnalyticsDAO(EventAnalyticsDAO eventAnalyticsDAO) {
        this.eventAnalyticsDAO = eventAnalyticsDAO;
    }

    public Map<Long, Map<String, Integer>> getAnalyticsReportMap() {
        return analyticsReportMap;
    }

    public InputStream getExportStream() {
        return exportStream;
    }

    public String getExportFilename() {
        return exportFilename;
    }

    public Date getDateStart() {
        return dateStart;
    }

    @TypeConversion(converter = "com.jivesoftware.community.util.DateFormatConverter")
    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public int getExportType() {
        return exportType;
    }

    public void setExportType(int exportType) {
        this.exportType = exportType;
    }

    public Date getDateEnd() {
        return dateEnd;
    }
    @TypeConversion(converter = "com.jivesoftware.community.util.DateFormatConverter")
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

//    public void prepare() throws Exception {
//        if(dateStart != null) {
//            DateFormat startDateFormat = new SimpleDateFormat("dd/MM/yyyy");
//            dateStart = startDateFormat.parse(startDateFormat.format(dateStart));
//        }
//
//        if(dateEnd != null) {
//            DateFormat endDateFormat = new SimpleDateFormat("dd/MM/yyyy");
//            dateEnd = endDateFormat.parse(endDateFormat.format(dateEnd));
//        }
//    }

    public String usersReportInput() {
        return "user-report-input";
    }

    public String docReportInput() {
        return "document-report-input";
    }




    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
     }
    /**
    * Method to render the Users Main Report
    * @return
    */
    public String userReport() {
        String sortTypeStr, sortOrderStr;
        Map<Long, Map<String, Integer>> tmpUserReportMap = new HashMap<Long, Map<String, Integer>>();
        
      //  dateStart = parseDate("2014-02-14");
      //  dateEnd = parseDate("2017-02-14");
        
        if (dateStart == null && dateEnd == null) {
            addActionError("Please choose Start and End dates to create report");
            return "user-report-input";
        } else if (dateStart == null) {
            addActionError("Please choose the Start Date");
            return "user-report-input";
        } else if (dateEnd == null) {
            addActionError("Please choose the End Date");
            return "user-report-input";
        } else {
            if (dateEnd.before(dateStart)) {
                log.info("End date can't be less than the start date.");
                addActionError("End date can't be less than the start date.");
                return "user-report-input";
            }
            if (dateEnd.after(new Date()) || dateStart.after(new Date())) {
                log.info("Start/End date can't be greater than Today's date.");
                addActionError("Start/End date can't be greater than Today's date.");
                return "user-report-input";
            }
            
            tmpUserReportMap = eventAnalyticsDAO.getUserEventsReport(dateStart, dateEnd, start, numResults);
            totalItemCount = tmpUserReportMap.size();
            if (tmpUserReportMap != null && tmpUserReportMap.size() > 0) {
                tmpUserReportMap = EventAnalyticsUtil.getMapSubset(tmpUserReportMap, start, numResults);
                boolean isAscending = (sortOrder ==0)?true:false; 
                if(sortField == 0){
                    analyticsReportMap = EventAnalyticsUtil.sortMapReportByName(tmpUserReportMap, isAscending);
                }else{
                    analyticsReportMap = EventAnalyticsUtil.sortMapReportByActivity(tmpUserReportMap,SortType.getById(sortField).toString(), isAscending);
                }
            }
        }
        return SUCCESS;
    }

    /**
     * Method to render the Documents Main Report
     * @return
     */
    public String documentReport() {
        Map<Long, Map<String, Integer>> tmpDocReportMap = new HashMap<Long, Map<String, Integer>>();
        analyticsReportMap.clear();

     //  dateStart = parseDate("2014-02-14");
      //  dateEnd = parseDate("2017-02-14");
        
        if (dateStart == null && dateEnd == null) {
            addActionError("Please choose Start and End dates to create report");
            return "document-report-input";
        } else if (dateStart == null) {
            addActionError("Please choose the Start Date");
            return "document-report-input";
        } else if (dateEnd == null) {
            addActionError("Please choose the End Date");
            return "document-report-input";
        } else {

            if (dateEnd.before(dateStart)) {
                addActionError("End date can't be less than the start date.");
                return "document-report-input";
            }
            if (dateEnd.after(new Date()) || dateStart.after(new Date())) {
                addActionError("Start/End date can't be greater than Today's date.");
                return "document-report-input";
            }

            tmpDocReportMap = eventAnalyticsDAO.getDocumentEventsReport(dateStart, dateEnd, start, numResults);
            totalItemCount = tmpDocReportMap.size();
            if (tmpDocReportMap != null && tmpDocReportMap.size() > 0) {
                tmpDocReportMap = EventAnalyticsUtil.getMapSubset(tmpDocReportMap, start, numResults);
                boolean isAscending = (sortOrder ==0)?true:false; 
                if(sortField == 0){
                   analyticsReportMap = EventAnalyticsUtil.sortMapReportByName(tmpDocReportMap,isAscending);                    
                }else{
                   analyticsReportMap = EventAnalyticsUtil.sortMapReportByActivity(tmpDocReportMap,SortType.getById(sortField).toString(), isAscending);
                }
            }
        }
        return SUCCESS;
    }

    /**
     * Main User Report download 
     * @return
     */
    public String downloadUserReport() {
        if (dateStart != null || dateEnd != null) {
            exportFilename = dateStart.toString() + "_" + dateEnd.toString().concat(".xls");
            // Prepare MAP to publish the Excel report
            analyticsReportMap = eventAnalyticsDAO.getUserEventsReport(dateStart, dateEnd, start, numResults);
        }
        if (null == analyticsReportMap || analyticsReportMap.size() == 0) {
            return "user-report-input";
        }
        exportStream = EventAnalyticsUtil.generateExcelReport(analyticsReportMap, exportFilename, JiveConstants.USER, false);
        return SUCCESS;
    }

   /**
     * User Drill down report download
     * @return
    */
    public String downloadUserDrillDownReport() {
        Map<String, Object> drillDownData = new HashMap<String, Object>();
        if (dateStart != null || dateEnd != null) {
            exportFilename = dateStart.toString() + "_" + dateEnd.toString().concat(".xls");
            // Prepare MAP to publish the Excel report
            List<Map<String, String>> drillDownReports = eventAnalyticsDAO.fetchUserDrillDownReports(start, numResults, dateStart, dateEnd, userID, activityType, false);
            drillDownData = EventAnalyticsUtil.getDrillDownPaginatedReport(userID, JiveConstants.USER, activityType, drillDownReports);
        }
        if (drillDownData.size() > 0) {
            exportStream = EventAnalyticsUtil.generateDrillDownExcelReport(drillDownData, exportFilename, JiveConstants.USER);
        }
        return SUCCESS;
    }

    public int getUserReportDrillDownCount(long userID, String activityType, String dateStartStr, String dateEndStr){
        int total_items = eventAnalyticsDAO.getUserReportDrillDownCount(userID, activityType, EventAnalyticsUtil.convertStringToDate(dateStartStr),
                EventAnalyticsUtil.convertStringToDate(dateEndStr));  
        return(total_items);
    }

    /**
     *  Main Document Report download 
     * @return  
     */
    public String downloadDocumentReport() {
        if (dateStart != null || dateEnd != null) {
            exportFilename = dateStart.toString() + "_" + dateEnd.toString().concat(".xls");
            // Prepare MAP to publish the Excel report
            analyticsReportMap = eventAnalyticsDAO.getDocumentEventsReport(dateStart, dateEnd, start, numResults);
        }
        if (null == analyticsReportMap || analyticsReportMap.size() == 0) {
            return "document-report-input";
        }
        exportStream = EventAnalyticsUtil.generateExcelReport(analyticsReportMap, exportFilename, JiveConstants.DOCUMENT, false);
        return SUCCESS;
    }

    public String downloadAllDocumentsReport(){
        List<ReportingMap> analyticsReportMapList = new ArrayList<ReportingMap>();
        if (dateStart != null || dateEnd != null) {
            exportFilename = dateStart.toString() + "_" + dateEnd.toString().concat(".xls");
            // Prepare MAP to publish the Excel report
            analyticsReportMapList = eventAnalyticsDAO.getAllUserActivitiesReport(dateStart, dateEnd);
        }
        if (null == analyticsReportMapList || analyticsReportMapList.size() == 0) {
            return "document-report-input";
        }
        exportStream = EventAnalyticsUtil.generateAllUserActivityExcelReport(analyticsReportMapList, exportFilename, JiveConstants.DOCUMENT);
        return SUCCESS;
    }

    /**
     *  Document Drill down report download
     * @return
    */
    public String downloadDrillDocumentReport() {
        Map<String, Object> drillDownData = new HashMap<String, Object>();
        List<Map<String, String>> paginatedUserReportMap = new ArrayList<Map<String, String>>();
        if (dateStart != null || dateEnd != null) {
            exportFilename = dateStart.toString() + "_" + dateEnd.toString().concat(".xls");
            // Prepare MAP to publish the Excel report
            List<Map<String, String>> drillDownReports = eventAnalyticsDAO.fetchDocumentDrillDownReports(userID, activityType, start, numResults, dateStart, dateEnd, false);
            drillDownData = EventAnalyticsUtil.getDrillDownPaginatedReport(userID, JiveConstants.DOCUMENT, activityType, drillDownReports);
        }
        if (null == drillDownData || drillDownData.size() == 0) {
            return "document-report-input";
        }
        exportStream = EventAnalyticsUtil.generateDrillDownExcelReport(drillDownData, exportFilename, JiveConstants.DOCUMENT);
        return SUCCESS;
    }

//    public int getDocumentReportDrillDownCount(long userID, String activityType){
//        int total_items = eventAnalyticsDAO.getDocumentReportDrillDownCount(userID, activityType);
//        return(total_items);
//    }

    /**
     * DWR method which return the List of Documents for a particular User selected for an activity
     *
     * @param userID       - ID of user whose activities which needs to be listed
     * @param activityType - Type of activity to be listed
     * @param startIndex   - starting index for pagination
     * @param numResults   - Total no of records that needs to be displayed
     * @return - Map of <document_name> and <count_of_activity>
     */
    public String paginatedUserReport(String strDateStart, String strDateEnd, long userID, String activityType, int startIndex, int numResults) {
        List<Map<String, String>> paginatedUserReportMap = new ArrayList<Map<String, String>>();
        if(startIndex > 0){
            startIndex = ((numResults * startIndex) - numResults) + 1;
        }
        paginatedUserReportMap = eventAnalyticsDAO.getDWRUserReport(strDateStart, strDateEnd, userID, activityType, startIndex, numResults);
        return (generatePaginatedReportHTML(userID, JiveConstants.USER, activityType, paginatedUserReportMap));
    }

    /**
     * DWR method which return the List of Users for a particular Document selected for an activity
     * @param strDateStart
     * @param strDateEnd
     * @param userID
     * @param activityType
     * @param startIndex
     * @param numResults
     * @return
     */
    public String paginatedDocumentReport(String strDateStart, String strDateEnd, long userID, String activityType, int startIndex, int numResults) {
        List<Map<String, String>> paginatedUserReportMap = new ArrayList<Map<String, String>>();
        if(startIndex > 0){
            startIndex = ((numResults * startIndex) - numResults) + 1;
        }
        paginatedUserReportMap = eventAnalyticsDAO.getDWRDocumentReport(strDateStart, strDateEnd, userID, activityType, startIndex, numResults);
        return (generatePaginatedReportHTML(userID, JiveConstants.DOCUMENT, activityType, paginatedUserReportMap));
    }

    /**
     * Helper method whic will generate a DRILL DOWN HTML BLOCK to render on UI
     * @param objectID
     * @param objType
     * @param activity
     * @param paginatedUserReportMapList
     * @return
     */
    public String generatePaginatedReportHTML(long objectID, int objType, String activity, List<Map<String, String>> paginatedUserReportMapList) {
        /*
           User drill down -
                    document - count
           Document drill down -
                    ratinguser - count
        */
        Map<String, Object> drillDownData = new HashMap<String, Object>();
        Favorite fav = null;
        Document docObj = null;
        String name = "";
        String repType = "";
        JiveObject tmpObj = null;
        int totalCount = 0;
        boolean isPrivate = false;
        double meanRating = 0;

        // MAP with <type>, <title - username/docment_subject>, <activity>, <total_act_count>, <activities> - [ list<String, Integer> (activity details) ] 


        if (objType == JiveConstants.USER) {
            repType = "Name";
            name = SkinUtils.getUserDisplayName(objectID);
            drillDownData.put("type", "USER");
            drillDownData.put("title", name);
        }
        if (objType == JiveConstants.DOCUMENT) {
            repType = "Document";
            docObj = (Document) EventAnalyticsUtil.loadJiveObject(JiveConstants.DOCUMENT, objectID);
            name = docObj.getSubject();
            drillDownData.put("type", "DOCUMENT");
            drillDownData.put("title", name);
        }

        String htmlReportTypeInfo = "<div class='drilled_report_data'><table>" +
                "<tbody>" +
                "<tr><td>%s :</td><td id='td_user_id'> %s </td></tr>" +
                "<tr><td>Activity :</td><td id='td_act_type'> %s </td></tr>" +
                "</tbody>" +
                "</table>";

        activity = (activity.equals("DOCUMENT_VIEWED")) ? "Read" :
                ((activity.equals("COMMENT_ADDED")) ? "Comment" :
                        ((activity.equals("FAVORITE_added")) ? "Bookmark" :
                                ((activity.equals("DOCUMENT_RATED")) ? "Rating" : "")));

        drillDownData.put("activity", activity);
        // Build the header block for Ajax click
        htmlReportTypeInfo = String.format(htmlReportTypeInfo, repType, name, activity);


        // Build the main report content block for Ajax click
        StringBuilder htmlReportHeaderData = new StringBuilder();
        htmlReportHeaderData.append("<table width='100%' border='1'>");
        htmlReportHeaderData.append("<tbody>");
        htmlReportHeaderData.append("<tr>");
        htmlReportHeaderData.append("<th class='jive-table-head-title'>" + ((repType.equals("Document")) ? "Users" : "Documents"));
        htmlReportHeaderData.append("</th>");
        htmlReportHeaderData.append("<th class='jive-table-head-title td_values'>COUNT</th>");
        if (activity.equals("Bookmark")) {
            htmlReportHeaderData.append("<th class='jive-table-head-title td_values'>Type</th>");
        }

        if (activity.equals("Rating")) {
            htmlReportHeaderData.append("<th class='jive-table-head-title td_values'>Score</th>");
        }

        htmlReportHeaderData.append("</tr> %%");  // data from DB records
        htmlReportHeaderData.append("</tbody>");
        htmlReportHeaderData.append("</table>");


        // Generate the HTML block for report to render 
        StringBuilder sb = new StringBuilder();
        List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
        Map<String, String> dataMap = null;
        // Iterate through all List of Maps (records from DB)
        for (Map<String, String> paginatedUserReportMap : paginatedUserReportMapList) {
            dataMap = new HashMap<String, String>();
            // Fetch each record details - create a <tr> for each record
            long objID = -1;
            String act_count = "", subject = "";
            // If document load the document and fetch the Subject
            if (objType == JiveConstants.DOCUMENT) {
                if (paginatedUserReportMap.get("ratinguser") != null) {
                    objID = Long.valueOf(paginatedUserReportMap.get("ratinguser")).longValue();
                } else {
                    objID = Long.valueOf(paginatedUserReportMap.get("RATINGUSER")).longValue();
                }
                subject = SkinUtils.getUserDisplayName(objID);

                // Fetch users list with type of bookmark for the selected document
                if (activity.equals("Bookmark")) {
                    isPrivate = EventAnalyticsUtil.isPrivateBookmark(JiveConstants.DOCUMENT, docObj.getID(),objID);
                    if (isPrivate) {
                        dataMap.put("bookmark_type", "Private");
                    } else {
                        dataMap.put("bookmark_type", "Public");
                    }
                }

                //Rating Drill Report
                if (activity.equals("Rating")) {
                    dataMap.put("rating_score", String.valueOf(EventAnalyticsUtil.getUserRating(objID, docObj)));
                }

            }
            // If user load the user and fetch the User display name
            if (objType == JiveConstants.USER) {
                if (paginatedUserReportMap.get("document") != null) {
                    objID = Long.valueOf(paginatedUserReportMap.get("document")).longValue();
                } else {
                    objID = Long.valueOf(paginatedUserReportMap.get("DOCUMENT")).longValue();
                }
                docObj = (Document) EventAnalyticsUtil.loadJiveObject(JiveConstants.DOCUMENT, objID);
                subject = docObj.getSubject();
                // Fetch users list with type of bookmark for the selected document
                if (activity.equals("Bookmark")) {
                    isPrivate = EventAnalyticsUtil.isPrivateBookmark(JiveConstants.DOCUMENT, docObj.getID(),objectID);
                    if (isPrivate) {
                        dataMap.put("bookmark_type", "Private");

                    } else {
                        dataMap.put("bookmark_type", "Public");
                    }
                }

                if (activity.equals("Rating")) {
                    // objectID - userID
                    //dataMap.put("rating_score", String.valueOf(EventAnalyticsUtil.getMeanRating(docObj)));
                    dataMap.put("rating_score", String.valueOf(EventAnalyticsUtil.getUserRating (objectID, docObj)));
                }

            }
            // Fetch the total activity count 
            if (paginatedUserReportMap.get("activity_count") != null) {
                act_count = paginatedUserReportMap.get("activity_count");
            } else {
                act_count = paginatedUserReportMap.get("ACTIVITY_COUNT");
            }

            dataMap.put("target_obj_title", subject);
            dataMap.put("target_obj_event_cnt", act_count);
            dataList.add(dataMap);

            // Update the total count
            totalCount += Integer.valueOf(act_count).intValue();

            //log.info("DD_RECORD : " + subject + " - " + act_count);
            sb.append("<tr>");
            sb.append("<td>")
                    .append(subject)
                    .append("</td><td>")
                    .append(act_count)
                    .append("</td>");
            if (activity.equals("Bookmark")) {
                sb.append("<td>");
                sb.append(dataMap.get("bookmark_type"));
                sb.append("</td>");
            }

            if (activity.equals("Rating")) {
                sb.append("<td>");
                sb.append(dataMap.get("rating_score"));
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        // add total count tr
        sb.append("<tr>");
        sb.append("<td align='center' style='font-weight:bold;'>Total</td><td align='center' style='font-weight:bold;'>")
                .append(totalCount)
                .append("</td>");
        sb.append("</tr>");
        // Update the data list
        drillDownData.put("dataList", dataList);
        // Update the total activity count 
        drillDownData.put("total_act_count", totalCount);

        String repData = sb.toString();
        //log.info("Map data - " + repData);
        String repHeader = htmlReportHeaderData.toString();
        repHeader = StringUtils.replaceFirst(repHeader, "%%", repData);
        log.info("Ajax report data - " + htmlReportHeaderData.toString());
        return htmlReportTypeInfo.concat(repHeader);
    }


    
    /* DWR Methods */

    /**
     * DWR method to update the event end time
     *
     * @param objectType    - ObjectType of current page
     * @param objectID      - ObjectID of current page
     * @param containerType - ContainerType
     * @param containerID   - ContainerID
     * @param url           - Current url of page
     */
    public void updateEventEndTime(long userID, int objectType, long objectID, int containerType, long containerID, String url) {
        eventAnalyticsDAO.updateEventEndTime(userID, objectType, objectID, containerType, containerID, url);
    }

    public Paginator getNewPaginator() {
        return new Paginator(this);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getNumResults() {
        if (numResults == 0) {
            numResults = JiveGlobals.getJiveIntProperty("grail.analytics.pagination.results", 10);
        }
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

}
