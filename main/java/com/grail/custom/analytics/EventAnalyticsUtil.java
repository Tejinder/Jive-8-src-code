package com.grail.custom.analytics;

import com.grail.custom.analytics.util.ReportingMap;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.*;
import com.jivesoftware.community.acclaim.Vote;
import com.jivesoftware.community.favorites.Favorite;
import com.jivesoftware.community.favorites.FavoriteManager;
import com.jivesoftware.community.favorites.ObjectFavorite;
import com.jivesoftware.community.favorites.authz.FavoriteEntitlementProvider;
import com.jivesoftware.community.impl.DbAttachment;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.rating.Rating;
import com.jivesoftware.community.rating.RatingDelegator;
import com.jivesoftware.community.rating.RatingManager;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author samee
 * @Date Apr 20, 2010
 * @Time 3:21:55 PM
 * @Version 1.0
 */
public class EventAnalyticsUtil {

    private static final Logger log = LogManager.getLogger(EventAnalyticsUtil.class);
    // For generating sequence
    public static int USER_SESSION_TYPE = 2103;
    public static int USER_SESSION_EVENT_TYPE = 2183;


    // Type of formats that is supported for export option

    public enum EXPORT_FORMATS {
        EXCEL_2000, EXCEL_2007, CSV, EXPORT_ALL
    }

    ;

    /**
     * Utility method to fetch the Attachment name by passing the attachmentID
     *
     * @param attachmentID
     * @return
     */
    public static String getAttachmentBean(long attachmentID) {
        DbAttachment dbAttBean = new DbAttachment(attachmentID);
        String attachementFileName = "";
        if (null != dbAttBean) {
            attachementFileName = dbAttBean.getName();
        }
        return attachementFileName;
    }

    /**
     * Utility method to load JiveObject using objectID and objectType 
     * @param objectType - type ID of jiveObject
     * @param objectID   - ID of jiveObject
     * @return - jiveObject
     */
    public static JiveObject loadJiveObject(int objectType, long objectID) {
        JiveObject jObj = null;
        try {
            jObj = JiveApplication.getEffectiveContext().getObjectLoader().getJiveObject(objectType, objectID);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return jObj;
    }

    /**
     * Generate MS Excel user report
     *
     * @param userReportData - Data that needs has to exported to an Excel Spreadsheet
     * @return - InputStream of generated XLS/XLSX file
     */
    public static InputStream generateExcelReport(Map<Long, Map<String, Integer>> userReportData, String fileName, int objType, boolean exportAll) {
        // TODO : support both xls and xlsx formats. Now its always xls 
        // fileName = isXLSX?(fileName+".xlsx"):(fileName+".xls");
        return ReportExportUtil.buildReport(userReportData, fileName, objType);
    }

     /**
     * Generate MS Excel user report
     *
     * @param userReportData - Data that needs has to exported to an Excel Spreadsheet
     * @return - InputStream of generated XLS/XLSX file
     */
    public static InputStream generateAllUserActivityExcelReport(List<ReportingMap> userReportData, String fileName, int objType) {
        // TODO : support both xls and xlsx formats. Now its always xls
        // fileName = isXLSX?(fileName+".xlsx"):(fileName+".xls");
        return ReportExportUtil.buildAllUserActivityReport(userReportData, fileName, objType);
    }

    public static InputStream generateDrillDownExcelReport(Map<String, Object> drillDownData, String exportFilename, int objType) {
        return ReportExportUtil.buildDrillDownReport(drillDownData, exportFilename, objType);
    }


    /**
     * Utility method to fetch a subset of Map
     *
     * @param srcMap     - Source map
     * @param startIndex - Start index for the subset map
     * @param numResults - End index for the subset map
     * @return - Subset map
     */
    public static Map<Long, Map<String, Integer>> getMapSubset(Map<Long, Map<String, Integer>> srcMap, int startIndex, int numResults) {
        Map<Long, Map<String, Integer>> subSetMap = new HashMap<Long, Map<String, Integer>>();
        Object[] userIDArr = srcMap.keySet().toArray();
        for (int i = startIndex; i < srcMap.size(); i++) {
            Long currentIndex = (Long) userIDArr[i];
            subSetMap.put(currentIndex, srcMap.get(currentIndex));
            if (subSetMap.size() == numResults) {
                break;
            }
        }
        return subSetMap;
    }

    /**
     * Utility method to strip off the time details from the Date object passed
     *
     * @param date
     * @return
     */
    public static long getCurrentLongTime(Date date) {
        return (new Date(date.getYear(), date.getMonth(), date.getDate(), 0, 0)).getTime();
    }


    public static Date convertStringToDate(String dateStr){
        Date dateObj = null;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            dateObj = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj;
    }

    public static String convertDateToString(Date dteObj){
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return(df.format(dteObj).toString());        
    }

    public static boolean isPrivateBookmark(int objType, long objID, long userId) {
        boolean isPrivate = false;
        try {
            FavoriteEntitlementProvider favoriteEntitlementProvider = (FavoriteEntitlementProvider) JiveApplication.getEffectiveContext().getSpringBean("favoriteEntitlementProvider");
            FavoriteManager favUnProxyManager = (FavoriteManager) JiveApplication.getEffectiveContext().getSpringBean("favoriteManagerImpl");
            ObjectFavorite of = favUnProxyManager.getObjectFavorite(JiveApplication.getEffectiveContext().getObjectLoader().getJiveObject(objType, objID));
            //ObjectFavorite of = favUnProxyManager.getObjectFavorite(EventAnalyticsUtil.loadJiveObject(objType, objID));
            System.out.print(" Object Favorite found ::::" + of.getFavoriteCount());
           /* JiveIterator<Favorite> favs = of.getFavorites();
            System.err.print("Is Private ::::" + favs);
            while (favs.hasNext()) {
                Favorite fav = favs.next();
                if(fav.getUser().getID() == userId )
                {
                isPrivate = favoriteEntitlementProvider.isPrivate(fav);
                System.err.print("Is Private ::::" + isPrivate);
                }
            }*/
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return isPrivate;
    }

    public static double getMeanRating(JiveObject contentObject) {
        return new RatingDelegator(contentObject).getMeanRating();
        // RatingManager ratingManager = (RatingManager) JiveApplication.getEffectiveContext().getSpringBean("ratingManagerImpl");
        //return ratingManager.getMeanRating(contentObject);

    }

    public static int getUserRating(long userID, JiveObject contentObject) {
        int score = 0;
        try {
            UserManager userManager = (UserManager) JiveApplication.getEffectiveContext().getSpringBean("userManagerImpl");
            User user = userManager.getUser(userID);
            RatingManager ratingManager = (RatingManager) JiveApplication.getEffectiveContext().getSpringBean("ratingManagerImpl");

            // Added by Bhaskar Avulapati
            try
            {
            	score = new RatingDelegator(contentObject).getRating(user).getVoteValue();
            }
            catch(Exception e)
            {
            	log.error("Error while calculation Score ==>> " + e.getMessage());
            }
            //score = ratingManager.getRating(user, contentObject).getScore();
        } catch (UserNotFoundException unfe) {
            log.error("Unable to load user, while getting User Rating for Document " + unfe.getMessage());
        }
        return score;

    }

        /**
        * Sorts a given Map by ascending or descending order based on the 'Keys'
        *
        * @param results   - UserReports or DocumentReports Map object
        * @param sortOrder - true: Ascending; false: Descending
        * @return - Sorted Map object
        */
    public static Map sortMapReportByName(Map results, boolean sortOrder) {
        System.out.println("Sort by Names " + results);
        SortedMap nameSort = new TreeMap(results);
        if (sortOrder) {
            System.out.println("Ascending Sort by Names " + nameSort);
        } else {
            NavigableMap descending = ((TreeMap) nameSort).descendingMap();
            nameSort = descending;
            System.out.println("Descending Sort of Names " + descending);
        }
        return nameSort;

    }

    /**
     * Utility method which returns a BubbleSorted Map for a particular sortType.
     *
     * @param reportMap - the map which contains the resultset for User or Document Report
     * @param sortType  - the compartor, for which the sort will be applied to in the ActivityMap.
     *                  Can be any of the following: READ_COUNT,COMMENT_COUNT, RATING_COUNT,BOOKMARK_COUNT.
     * @param sortOrder - true: Ascending; false: Descending.
     * @return - Sorted Map object
     */
    public static Map sortMapReportByActivity(Map reportMap, String sortType, boolean sortOrder) {
        System.out.println("Sort  Type  :: "+sortType + "  sortOrder   ::"+sortOrder);
        Map sortedMap = new LinkedHashMap();
        Object[] keys = reportMap.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            System.out.println("Keys before sort  " + keys[i]);
        }
        Object temp = null;
        int n = keys.length;
        int i, j = 0;
        for (i = 0; i < n; i++) {
            for (j = 1; j < (n - i); j++) {
                ((ReportingMap) reportMap.get(keys[j - 1])).setAscending(sortOrder);
                ((ReportingMap) reportMap.get(keys[j - 1])).setSortType(sortType);

                
                if (((ReportingMap) reportMap.get(keys[j - 1])).compareTo((ReportingMap) reportMap.get(keys[j])) == 1) {
                    //swap contents
                    temp = keys[j - 1];
                    keys[j - 1] = keys[j];
                    keys[j] = temp;
                }
            }
        }
//        System.out.println(" \n\n\n ");
        for (int m = 0; m < keys.length; m++) {
            log.info("Keys after sort  " + keys[m]);
            sortedMap.put(keys[m], reportMap.get(keys[m]));
        }
        log.info("\n\n\n   FINAL Sorted RESULT MAP ::::::::::::::: "+sortedMap);
        return sortedMap;
    }


    static class ReportExportUtil {        
        public static InputStream buildReport(Map<Long, Map<String, Integer>> userReportData, String fileName, int objType) {
            // Check for excel file type
            if (fileName.endsWith(".xlsx")) {
                System.out.println("Currently MS-2007 format export is not supported. So will be generating XLS - 2000 file format excel.");
                fileName = StringUtils.replaceFirst(fileName, ".xlsx", ".xls");
            }
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet reportFirstSheet = null;
            // Define the Titles
            String  headerArray[][] =  new String[][]{
            };

            String  userReportHeaderArray[][] = new String[][]{
                    {"NAME", "NAME"},
                    {"READ_COUNT", "READ"},
                    {"COMMENT_COUNT", "COMMENT"},
                    {"BOOKMARK_COUNT", "BOOKMARK"},
                    {"RATING_COUNT", "DOCUMENT RATED"}
            };

            String  documentReportHeaderArray[][] = new String[][]{
                    {"NAME", "NAME"},
                    {"READ_COUNT", "READ"},
                    {"COMMENT_COUNT", "COMMENT"},
                    {"BOOKMARK_COUNT", "BOOKMARK"},
                    {"RATING_COUNT", "AVERAGE RATING"}
            };
            if (objType == JiveConstants.USER) {
                reportFirstSheet = workbook.createSheet("User Report");
                headerArray = userReportHeaderArray;
            }

            if (objType == JiveConstants.DOCUMENT) {
                reportFirstSheet = workbook.createSheet("Document Report");
                  headerArray = documentReportHeaderArray;
            }

            HSSFRow headerRow = reportFirstSheet.createRow(0);
                       


                  for (int j = 0; j < headerArray.length; j++) {
                        HSSFCell headerCell = headerRow.createCell(j);
                        headerCell.setCellValue(headerArray[j][1]);
                  }


            // Iterate through the data
            int rowIndex = 3;
            int firstCellIndex = 0;

            for (Long uid : userReportData.keySet()) {
                Map<String, Integer> eventCntDetails = userReportData.get(uid);
                HSSFRow rowA = reportFirstSheet.createRow(rowIndex);
     
                // Set Name field
                HSSFCell titleValueCell = rowA.createCell(firstCellIndex);
                Document docObj = null;
                if (objType == JiveConstants.USER) {
                    titleValueCell.setCellValue(SkinUtils.getUserDisplayName(uid));
                } else {
                    if (objType == JiveConstants.DOCUMENT) {
                        docObj = ((Document) EventAnalyticsUtil.loadJiveObject(JiveConstants.DOCUMENT, uid)); 
                        titleValueCell.setCellValue(docObj.getSubject());
                    }
                }

                   // Iterate through the event array to populate the event count data
                   for (int i = 1; i <= 4; i++) {
                        if (objType == JiveConstants.DOCUMENT && i== 4) {
                             // Add one more item - Avg Rating for a document
                            HSSFCell avgRatingCell = rowA.createCell(4);
                            double avgDocRating = EventAnalyticsUtil.getMeanRating(docObj);
                            // Round off to 2 decimal places
                            DecimalFormat twoDForm = new DecimalFormat("#.##");
                            avgDocRating = Double.valueOf(twoDForm.format(avgDocRating));
                            avgRatingCell.setCellValue(avgDocRating);
                        }
                       else
                        {
                            HSSFCell cellA = rowA.createCell(i);
                            int cnt = (eventCntDetails.containsKey(headerArray[i][0])) ? Integer.valueOf(eventCntDetails.get(headerArray[i][0])).intValue() : 0;
                            cellA.setCellValue(cnt);
                        }
                    } 

             rowIndex++;
            }
            InputStream inputStream = null;
            FileOutputStream fout = null;
            File newFile = new File("test.xls");
            Writer writer = null;
            try {
                // TODO: return the inputStream from workbook
                fout = new FileOutputStream(newFile);
                workbook.write(fout);
                inputStream = new FileInputStream(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        log.error("Exception occurred while closing the 'Normal' Report FileOS " + e.getMessage());
                    }
                }
            }
            return inputStream;
        }

         public static InputStream buildAllUserActivityReport(List<ReportingMap> userReportData, String fileName, int objType) {
            // Check for excel file type
            if (fileName.endsWith(".xlsx")) {
                System.out.println("Currently MS-2007 format export is not supported. So will be generating XLS - 2000 file format excel.");
                fileName = StringUtils.replaceFirst(fileName, ".xlsx", ".xls");
            }
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet reportFirstSheet = null;
            if (objType == JiveConstants.USER) {
                reportFirstSheet = workbook.createSheet("User Report");
            }
            if (objType == JiveConstants.DOCUMENT) {
                reportFirstSheet = workbook.createSheet("Document Report");
            }

            // Define the Titles
            String expAllHeaderArray[][] = new String[][]{
                    // User	NAME	READ	COMMENT	BOOKMARK	RATING
                    {"USER", "User"},
                    {"NAME", "Name"},
                    {"READ_COUNT", "Read"},
                    {"COMMENT_COUNT", "Comment"},
                    {"BOOKMARK_COUNT", "Bookmark"},
                    {"BOOKMARK_TYPE", "Bookmark Type"},
                    {"RATING_COUNT", "Rating"},
                    {"AVG_RATING", "Average rating"}
            };
            HSSFRow headerRow = reportFirstSheet.createRow(0);

             // Render -  'All Users and All User Activities' Report Headers
            HSSFCell expAllCell = headerRow.createCell(0);
            for (int j = 0; j <expAllHeaderArray.length; j++) {
                   HSSFCell expAllHeaderCell = headerRow.createCell(j);
                   expAllHeaderCell.setCellValue(expAllHeaderArray[j][1]);
              }

            // Iterate through the data
            int rowIndex = 3;
            int firstCellIndex = 1; // Since it will include an additional column

            for (ReportingMap allUserMap : userReportData) {

                HSSFRow rowA = reportFirstSheet.createRow(rowIndex);

                long uObj = (Long)allUserMap.get("userid");

                HSSFCell expAllFirstCell = rowA.createCell(0);
                // Currently it only supports Export All Users and Activities for User Report
                if (objType == JiveConstants.DOCUMENT) {
                        // value will be userName
                        expAllFirstCell.setCellValue(SkinUtils.getUserDisplayName(uObj));
                    }

                // Set Name field
                HSSFCell titleValueCell = rowA.createCell(firstCellIndex);
                long objectId =  (Long)allUserMap.get("objectid");
                Document docObj = ((Document) EventAnalyticsUtil.loadJiveObject(JiveConstants.DOCUMENT, objectId));
                titleValueCell.setCellValue(docObj.getSubject());

                int readCount = 0;
                int commentCount = 0;
                int bookmarkCount = 0;
                int ratingCount = 0;

                if(allUserMap.get("READ_COUNT") != null)
                {
                    readCount = (Integer)allUserMap.get("READ_COUNT");
                }
                if(allUserMap.get("COMMENT_COUNT") != null)
                {
                    commentCount = (Integer)allUserMap.get("COMMENT_COUNT");
                }
                if(allUserMap.get("BOOKMARK_COUNT") != null)
                {
                    bookmarkCount = (Integer)allUserMap.get("BOOKMARK_COUNT");
                }
                if(allUserMap.get("RATING_COUNT") != null)
                {
                    ratingCount = (Integer)allUserMap.get("RATING_COUNT");
                }

                 HSSFCell cellA = rowA.createCell(2);
                 cellA.setCellValue(readCount);
                 cellA = rowA.createCell(3);
                 cellA.setCellValue(commentCount);
                 cellA = rowA.createCell(4);
                 cellA.setCellValue(bookmarkCount);
                 String bookmarkType = "NA";
                 if(bookmarkCount > 0){
                   bookmarkType = (EventAnalyticsUtil.isPrivateBookmark(JiveConstants.DOCUMENT, docObj.getID(),uObj)?"Private":"Public");
                 }
                 cellA = rowA.createCell(5);
                 cellA.setCellValue(bookmarkType);
                 cellA = rowA.createCell(6);
                 cellA.setCellValue(ratingCount);
                  // Add one more item - Avg Rating for a document
                    HSSFCell avgRatingCell = rowA.createCell(7);
                    double avgDocRating = EventAnalyticsUtil.getMeanRating(docObj);
                    // Round off to 2 decimal places
                    DecimalFormat twoDForm = new DecimalFormat("#.##");
                    avgDocRating = Double.valueOf(twoDForm.format(avgDocRating));
                    avgRatingCell.setCellValue(avgDocRating);

                rowIndex++;
            }
            InputStream inputStream = null;
            FileOutputStream fout = null;
            File newFile = new File("test.xls");
            Writer writer = null;
            try {
                // TODO: return the inputStream from workbook
                fout = new FileOutputStream(newFile);
                workbook.write(fout);
                inputStream = new FileInputStream(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        log.error("Exception occurred while closing the 'Normal' Report FileOS " + e.getMessage());
                    }
                }
            }
            return inputStream;
        }
        public static InputStream buildDrillDownReport(Map<String, Object> drillDownData, String exportFilename, int objType) {
            // Check for excel file type
            if (exportFilename.endsWith(".xlsx")) {
                System.out.println("Currently MS-2007 format export is not supported. So will be generating XLS - 2000 file format excel.");
                exportFilename = StringUtils.replaceFirst(exportFilename, ".xlsx", ".xls");
            }
            // MAP with <type>, <title - username/docment_subject>, <activity>, <total_act_count>
            //  <dataList> - [ list<String, Integer> (activity details) ]

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet firstSheet = null;

            HSSFCell cellA = null;
            HSSFCell cellB = null;
            if (objType == JiveConstants.USER) {
                firstSheet = workbook.createSheet("User Drill Down Report");
                HSSFRow headerRow1 = firstSheet.createRow(0);
                cellA = headerRow1.createCell(0);
                cellA.setCellValue("Name");
                cellB = headerRow1.createCell(1);
            }
            if (objType == JiveConstants.DOCUMENT) {
                firstSheet = workbook.createSheet("Document  Drill Down Report");
                HSSFRow headerRow1 = firstSheet.createRow(0);
                cellA = headerRow1.createCell(0);
                cellA.setCellValue("Document");
                cellB = headerRow1.createCell(1);
            }
            cellB.setCellValue((String) drillDownData.get("title"));

            // Activity Type
            HSSFRow headerRow2 = firstSheet.createRow(1);
            HSSFCell actvityTitle = headerRow2.createCell(0);
            actvityTitle.setCellValue("Activity");
            HSSFCell actvityTitleValue = headerRow2.createCell(1);
            String activity = (String) drillDownData.get("activity");
            actvityTitleValue.setCellValue(activity);

            // Titles


            // Activity Type
            HSSFRow titleRow = firstSheet.createRow(2);
            HSSFCell typeTitle = titleRow.createCell(0);
            if (objType == JiveConstants.USER) {
                typeTitle.setCellValue("Document");
            } else {
                typeTitle.setCellValue("User");
            }

            HSSFCell countTitle = titleRow.createCell(1);
            countTitle.setCellValue("Count");

            if (activity.equals("Bookmark")) {
                HSSFCell bookmarkTypeTitle = titleRow.createCell(2);
                bookmarkTypeTitle.setCellValue("Type");
            }

            if (activity.equals("Rating")) {
                HSSFCell bookmarkTypeTitle = titleRow.createCell(2);
                bookmarkTypeTitle.setCellValue("Score");
            }


            // <dataList> - [ list<Map<String, Integer>> (activity details) ]
            // Iterate through the header array to build the report header
            int rowIndex = 3;
            List<Map<String, String>> dataList = (List<Map<String, String>>) drillDownData.get("dataList");
            for (Map<String, String> data : dataList) {
                HSSFRow rowA = firstSheet.createRow(rowIndex);
                // Set Name field
                HSSFCell titleCell = rowA.createCell(0);
                titleCell.setCellValue((String) data.get("target_obj_title"));
                HSSFCell countCell = rowA.createCell(1);
                countCell.setCellValue(Integer.parseInt(data.get("target_obj_event_cnt")));
                if (activity.equals("Bookmark")) {
                    HSSFCell bookmarkCell = rowA.createCell(2);
                    bookmarkCell.setCellValue((String) data.get("bookmark_type"));
                }
                if (activity.equals("Rating")) {
                    HSSFCell ratingCell = rowA.createCell(2);
                    ratingCell.setCellValue((String) data.get("rating_score"));
                }
                rowIndex++;
            }


            // Update the total count
            HSSFRow totalCountRow = firstSheet.createRow(rowIndex);
            HSSFCell totalCountCell = totalCountRow.createCell(0);
            totalCountCell.setCellValue("Total");
            HSSFCell totalCountCellValue = totalCountRow.createCell(1);
            totalCountCellValue.setCellValue(Integer.parseInt(drillDownData.get("total_act_count").toString()));

            InputStream inputStream = null;
            FileOutputStream fout = null;
            File newFile = new File("test_drill.xls");
            Writer writer = null;
            try {
                // TODO: return the inputStream from workbook
                fout = new FileOutputStream(newFile);
                workbook.write(fout);
                inputStream = new FileInputStream(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        log.error("Exception occurred while closing the 'Drill-Down Report' FileOS " + e.getMessage());
                    }
                }
            }
            return inputStream;
        }
    }


    /* DRILL DOWN REPORT HELPER METHOD */
    /**
    * Helper method to build User/Document reports MAP
    * @param objectID
    * @param objType
    * @param activity
    * @param paginatedUserReportMapList
    * @return - MAP which contains data related to User/Document depending on objType parameter
    */
    public static Map<String, Object> getDrillDownPaginatedReport(long objectID, int objType, String activity, List<Map<String, String>> paginatedUserReportMapList) {
           Map<String, Object> drillDownData = new HashMap<String, Object>();
           Favorite fav = null;
           Document docObj = null;
           String name = "";
           String repType = "";
           JiveObject tmpObj = null;
           int totalCount = 0;
           boolean isPrivate = false;
           // MAP with
           //      <type> - USER/DOCUMENT,
           //      <title> - username/docment_subject>,
           //      <activity> - Read/Bookmark/Rating/Comment,
           //      <total_act_count> - events total count,
           //      <dataList> - [ List(Map<String, String>) (jiveObjID, count) ]

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

           activity = (activity.equals("DOCUMENT_VIEWED")) ? "Read" :
                   ((activity.equals("COMMENT_ADDED")) ? "Comment" :
                           ((activity.equals("FAVORITE_added")) ? "Bookmark" :
                                   ((activity.equals("DOCUMENT_RATED")) ? "Rating" : "")));

           drillDownData.put("activity", activity);

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
                   // objID - userID
                   // Fetch the userID as Object
                   Object userIDObj = paginatedUserReportMap.get("ratinguser");
                   if (userIDObj instanceof String)
                       objID = Long.parseLong((String) userIDObj);
                   else if (userIDObj instanceof Long) {
                       objID = ((Long) userIDObj).longValue();
                   }
                   subject = SkinUtils.getUserDisplayName(objID);
                   // Fetch users list with type of bookmark for the selected document
                   if (activity.equals("Bookmark")) {
                      
                       isPrivate = EventAnalyticsUtil.isPrivateBookmark(JiveConstants.DOCUMENT, objectID, objID);
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

               } else {
                   // If user load the user and fetch the User display name
                   if (objType == JiveConstants.USER) {
                       // objID - documentID
                       if (paginatedUserReportMap.get("document") != null) {
                           // Fetch the documentID  as Object
                           Object obj = paginatedUserReportMap.get("document");
                           if (obj instanceof String)
                               objID = Long.parseLong((String) obj);
                           else if (obj instanceof Long) {
                               objID = ((Long) obj).longValue();
                           }
                       } else {
                           objID = Long.parseLong(paginatedUserReportMap.get("DOCUMENT"));
                       }
                       docObj = (Document) EventAnalyticsUtil.loadJiveObject(JiveConstants.DOCUMENT, objID);
                       subject = docObj.getSubject();
                   }
                   // Fetch documents list with type of bookmark for the selected user
                   if (activity.equals("Bookmark")) {
                       isPrivate = EventAnalyticsUtil.isPrivateBookmark(JiveConstants.DOCUMENT, objID,objectID);
                       if (isPrivate) {
                           dataMap.put("bookmark_type", "Private");

                       } else {
                           dataMap.put("bookmark_type", "Public");
                       }
                   }
                   if (activity.equals("Rating")) {
                       dataMap.put("rating_score", String.valueOf(EventAnalyticsUtil.getUserRating(objectID, docObj)));
                   }
               }

               // Fetch the total activity count
               if (paginatedUserReportMap.get("activity_count") != null) {
                   Object obj = paginatedUserReportMap.get("activity_count");
                   if (obj instanceof String)
                       act_count = (String) obj;
                   else if (obj instanceof Long) {
                       act_count = String.valueOf(obj);
                       //log.info("paginatedUserReportMap --- " + objID);
                   }

               } else {
                   act_count = paginatedUserReportMap.get("ACTIVITY_COUNT");
               }

               dataMap.put("target_obj_title", subject);
               dataMap.put("target_obj_event_cnt", act_count);
               dataList.add(dataMap);
               // Update the total count
               totalCount += Integer.valueOf(act_count).intValue();
           }
           // Update the data list
           drillDownData.put("dataList", dataList);
           // Update the total activity count
           drillDownData.put("total_act_count", totalCount);
           log.info("Ajax drill down report data - " + drillDownData);
           return drillDownData;
       }
      


   
}
 