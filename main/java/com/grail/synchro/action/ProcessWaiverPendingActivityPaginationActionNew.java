package com.grail.synchro.action;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverCatalogueBean;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.ProcessWaiverManagerNew;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectWaiverManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroLogUtilsNew;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.grail.util.URLUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/7/14
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class ProcessWaiverPendingActivityPaginationActionNew extends JiveActionSupport {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProcessWaiverPendingActivityPaginationActionNew.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private List<ProjectWaiverCatalogueBean> waiverPendingActivities;
    ProjectResultFilter waiverResultFilter = new ProjectResultFilter();
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;
    private ProcessWaiverManagerNew processWaiverManagerNew;
    
    private StageManager stageManager;
    private EmailNotificationManager emailNotificationManager;
    
    private ProjectWaiver projectWaiver;
    
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    
    private Integer plimit;
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    private String redirectUrl;
    
    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

    public Integer getPages() {
        return pages;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public void setResults(Integer results) {
        this.results = results;
    }

    public Integer getResults() {
        return results;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String execute()
    {
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        setPagination(processWaiverManagerNew.getPendingActivityTotalCount(getUser(), getSearchFilter()).intValue());
        updatePage();
        return SUCCESS;

    }

    public String downloadReport()
    {
    	downloadFilename = "PendingProcessWaivers.xls";
    	downloadStreamType = "application/vnd.ms-excel";
    	
    	String downloadReportPage = getRequest().getParameter("downloadReportPage");
		String downloadReportKeyword = getRequest().getParameter("downloadReportKeyword");
		
		page = Integer.parseInt(downloadReportPage);
		keyword = downloadReportKeyword;
    
    	 start = (page-1) * LIMIT;
         end = start + LIMIT;
       
        try
        {
        	processReport();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
      //Audit Logs
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
				0, "Pending Process Waivers - Download Report", "", -1L, getUser().getID());
        
        return DOWNLOAD_REPORT;

    }
    
    public void setPagination(final Integer count) {
    	if(plimit!=null && plimit > 0)
    	{
    		LIMIT=plimit;
    	}
    	
    	if(count > LIMIT) {
            double temp = count / (LIMIT * 1.0);
            if(count%LIMIT == 0) {
                pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }
    }

    public void updatePage() {
        start = (page-1) * LIMIT;
        end = start + LIMIT;
        List<ProjectWaiver> projectWaivers = processWaiverManagerNew.getPendingApprovalWaivers(getUser(), getSearchFilter());
        if(projectWaivers != null && projectWaivers.size() > 0) {
            waiverPendingActivities =  toProjectWaiverCatalogueBeans(projectWaivers);
        }
    }

    private void processReport() throws IOException {
        HSSFWorkbook workbook = null;
        workbook = new HSSFWorkbook();
      
       
        workbook = generateReport(workbook);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
              
    }
    private HSSFWorkbook generateReport(HSSFWorkbook workbook) throws UnsupportedEncodingException, IOException{
    	 
    	 waiverResultFilter = getSearchFilter();
    	 
    	 Calendar calendar = Calendar.getInstance();
         String timeStamp = calendar.get(Calendar.YEAR) +
                 "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                 "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                 "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                 "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                 "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
        
        
        
        StringBuilder generatedBy = new StringBuilder();
        generatedBy.append("Pending Approval Report ").append("\n");
       
        String userName = getUser().getName();
        generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
       
       

        generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
        
        
    	 waiverResultFilter.setStart(null);
    	 waiverResultFilter.setLimit(null);
    	 
    	List<ProjectWaiver> projectWaivers = processWaiverManagerNew.getPendingApprovalWaivers(getUser(), waiverResultFilter);
         if(projectWaivers != null && projectWaivers.size() > 0) {
             waiverPendingActivities =  toProjectWaiverCatalogueBeans(projectWaivers);
         }
         
         
         Integer startRow = 4;
         HSSFSheet sheet = workbook.createSheet("PendingApprovalReport");
         
         HSSFRow reportTypeHeader = sheet.createRow(startRow);
         
         HSSFFont sheetHeaderFont = workbook.createFont();
         sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	  sheetHeaderFont.setFontName("Calibri");
 	      
 	     HSSFFont callibiriFont = workbook.createFont();
         callibiriFont.setFontName("Calibri");
         
         HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
         sheetHeaderCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
         sheetHeaderCellStyle.setFont(sheetHeaderFont);
         sheetHeaderCellStyle.setWrapText(true);
         sheetHeaderCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
         
         sheetHeaderCellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
	      sheetHeaderCellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
	      sheetHeaderCellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
	      sheetHeaderCellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
	      
	      sheetHeaderCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	      sheetHeaderCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	      
	      
	      HSSFCellStyle generatedByCellStyle = workbook.createCellStyle();
	      
	      generatedByCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	      generatedByCellStyle.setFont(sheetHeaderFont);
	      generatedByCellStyle.setWrapText(true);
	      generatedByCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
         
         
         HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(0);
         reportTypeHeaderColumn.setCellStyle(generatedByCellStyle);
         reportTypeHeaderColumn.setCellValue(generatedBy.toString());
         
         sheet.addMergedRegion(new CellRangeAddress(0,3,0,8));
         sheet.addMergedRegion(new CellRangeAddress(4,7,0,8));
         
         startRow = startRow + 4;
         
         reportTypeHeader = sheet.createRow(startRow);
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(0);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Waiver ID");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(1);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Waiver Name");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(2);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Research End Market(s)");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(3);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Brand");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(4);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Rationale For Waiver");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(5);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Waiver Initiator");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(6);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Date Of Request");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(7);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Approver");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(8);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Status");
         
         CellStyle noStyle = workbook.createCellStyle();
         
         noStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         sheetHeaderFont.setFontName("Calibri");
         noStyle.setFont(callibiriFont);
         noStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        
         
         if(waiverPendingActivities!=null && waiverPendingActivities.size()>0)
         {
	         for(ProjectWaiverCatalogueBean waiver : waiverPendingActivities)
	         {
	        	 int cellNo = 0;
	        	 HSSFRow dataRow = sheet.createRow(startRow + 1);
		        
	        	 HSSFCell dataCell = dataRow.createCell(cellNo);
	        	 dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getWaiverID());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getWaiverName());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getCountry());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getBrand());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        
		         if(waiver.getSummary()!=null)
		         {
		        	 dataCell.setCellValue(waiver.getSummary().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		        
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getInitiator());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(waiver.getCreationDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new Date(waiver.getCreationDate())));
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		         
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getApprover());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue("Pending");
		         
		         
		         startRow++;
		         
	         }
	         
	       // sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+8,0,8));
	         HSSFRow notesRow  = sheet.createRow(startRow+3);
         	
         	HSSFCellStyle notesStyle = workbook.createCellStyle();
			 notesStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
             notesStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
             notesStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
             notesStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            notesStyle.setWrapText(true);
            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
            notesStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            notesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    	
            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	        
            notesStyle.setAlignment(CellStyle.ALIGN_LEFT);
	    	notesStyle.setWrapText(true);
	    	notesStyle.setFont(callibiriFont);
	    	
        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+8,0,8);
        	
        	sheet.addMergedRegion(mergedRegion);
        	
        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            
         	StringBuilder notes = new StringBuilder();
         	notes.append("Notes:").append("\n");
         	notes.append("- The cells with value '–' indiacate that the information is not available ").append("\n");
         	notes.append("- Cancelled projects are not included in the above report");
         	HSSFCell notesColumn = notesRow.createCell(0);
         	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
         	notesColumn.setCellValue(notes.toString());
         	notesColumn.setCellStyle(notesStyle);
	        
         }
         workbook = SynchroUtils.createExcelImage(workbook, sheet);
         return workbook;
         
         
    }
    private ProjectResultFilter getSearchFilter() {
        waiverResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(waiverResultFilter);
        binder.bind(getRequest());

        if(waiverResultFilter.getKeyword() == null || waiverResultFilter.getKeyword().equals("")) {
            waiverResultFilter.setKeyword(keyword);
        }
        if(waiverResultFilter.getStart() == null) {
            waiverResultFilter.setStart(start);
        }
       /* if(waiverResultFilter.getLimit() == null) {
            waiverResultFilter.setLimit(LIMIT);
        }*/
        
        if(plimit!=null && plimit > 0)
        {
        	waiverResultFilter.setLimit(plimit);
        }
        else
        {
        	waiverResultFilter.setLimit(LIMIT);
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin"))) {
            this.waiverResultFilter.setStartDateBegin(DateUtils.parse(getRequest().getParameter("startDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateComplete"))) {
            this.waiverResultFilter.setStartDateComplete(DateUtils.parse(getRequest().getParameter("startDateComplete")));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin"))) {
            this.waiverResultFilter.setEndDateBegin(DateUtils.parse(getRequest().getParameter("endDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateComplete"))) {
            this.waiverResultFilter.setEndDateComplete(DateUtils.parse(getRequest().getParameter("endDateComplete")));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("startDateComplete")) ) 
        {
        	if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("startDateComplete")))
        		{
        			selectedFilters.add("Date Of Request Between - " + getRequest().getParameter("startDateBegin") + " and "+getRequest().getParameter("startDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Date Of Request - " +getRequest().getParameter("startDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Date Of Request - " +getRequest().getParameter("startDateComplete"));
        	}
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("endDateComplete")) ) 
        {
        	if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("endDateComplete")))
        		{
        			selectedFilters.add("Date Of Approval Between - " + getRequest().getParameter("endDateBegin") + " and "+getRequest().getParameter("endDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Date Of Approval - " + getRequest().getParameter("endDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Date Of Approval - " + getRequest().getParameter("endDateComplete"));
        	}
        }
        if(waiverResultFilter.getResearchEndMarkets()!=null && waiverResultFilter.getResearchEndMarkets().size()>0)
        {
        	selectedFilters.add("Research End-Market(s) - "+ SynchroUtils.getEndMarketNames(waiverResultFilter.getResearchEndMarkets()));
        }
        if(waiverResultFilter.getMethDetails()!=null && waiverResultFilter.getMethDetails().size()>0)
        {
        	selectedFilters.add("Methodology - "+ SynchroUtils.getMethodologyNames(waiverResultFilter.getMethDetails()));
        }
        if(waiverResultFilter.getBudgetYears()!=null && waiverResultFilter.getBudgetYears().size()>0)
        {
        	selectedFilters.add("Budget Year - " + StringUtils.join(waiverResultFilter.getBudgetYears(), ","));
        }
        return waiverResultFilter;
    }

    private List<ProjectWaiverCatalogueBean> toProjectWaiverCatalogueBeans(final List<ProjectWaiver> projectWaivers) {
        List<ProjectWaiverCatalogueBean> projectCataloguesAll = new ArrayList<ProjectWaiverCatalogueBean>();
        if(projectWaivers != null && !projectWaivers.isEmpty()) {
            for(ProjectWaiver projectWaiver : projectWaivers) {
                try {
                    Calendar calender = Calendar.getInstance();
                    Date creationDate=new Date(projectWaiver.getCreationDate());
                    calender.setTime(creationDate);
                    Integer year = calender.get(Calendar.YEAR);
                    ProjectWaiverCatalogueBean bean = new ProjectWaiverCatalogueBean();
                    bean.setWaiverID(projectWaiver.getWaiverID());
                    bean.setWaiverName(projectWaiver.getName());
                    bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
                    bean.setYear(year);
                    processCountryAndRegion(bean, projectWaiver);
                    bean.setEndMarkets(projectWaiver.getEndMarkets());
                    bean.setSummary(projectWaiver.getSummary());
                    bean.setApproverComments(projectWaiver.getApproverComments());
                    bean.setBrand(SynchroGlobal.getBrands(true, null).get(projectWaiver.getBrand().intValue()));
                    bean.setBrandPopUp(projectWaiver.getBrand());
                    if(projectWaiver.getApproverID() != null) {
                        User approver = userManager.getUser(projectWaiver.getApproverID());
                        if(approver != null) {
                            bean.setApprover(approver.getName());
                        }
                    }

                    bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
                    
                    // This is done to show the Attachments for Process Waiver Pending Dashboard 
                    // http://redmine.nvish.com/redmine/issues/406
                    Map<Integer, List<AttachmentBean>> attachmentMap = processWaiverManagerNew.getDocumentAttachment(projectWaiver.getWaiverID());
                    if(attachmentMap!=null && attachmentMap.containsKey(SynchroGlobal.SynchroAttachmentObject.WAIVER_ATTACHMENT.getId().intValue()))
            		{
            			bean.setAttachments(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.WAIVER_ATTACHMENT.getId().intValue()));
            			
            		}
                    
                    bean.setCreationDate(projectWaiver.getCreationDate());
                    projectCataloguesAll.add(bean);
                } catch (UserNotFoundException e) {
                    LOG.info(e.getMessage(), e);
                }
            }
        }
        return projectCataloguesAll;
    }

    private void processCountryAndRegion(final ProjectWaiverCatalogueBean bean, final ProjectWaiver projectWaiver) {
        Set<String> countryList = new HashSet<String>();
        Set<String> regionList = new HashSet<String>();
        if(projectWaiver.getEndMarkets() != null && !projectWaiver.getEndMarkets().isEmpty()) {
            int idx = 0;
            for(Long mrktId: projectWaiver.getEndMarkets()) {
                String country = SynchroGlobal.getEndMarkets().get(mrktId.intValue());
                if(country != null) {
                    countryList.add(country);
                }
                //String region = SynchroGlobal.getRegionEndMarket().get(mrktId.intValue());
                String region ="Global";
                if(region != null) {
                    regionList.add(region);
                }
            }
        }
        bean.setCountry(StringUtils.join(countryList.toArray(), ", "));
        bean.setRegion(StringUtils.join(regionList.toArray(), ", "));
    }


    
    public String approveProcessWaiver()
    {
    	 
    	Long waiverID=null;
		final String id = getRequest().getParameter("projectWaiverID");
		
	    if(id  != null ) 
	    {
	         try{
	             if(waiverID==null)
	            	 waiverID = Long.parseLong(id);
	         } catch (NumberFormatException nfEx) {
	        	 LOGGER.error("Invalid waiverID ");
	             throw nfEx;
	         }
	         
	    }
	    
	    approveWaiver(waiverID, null);
	    
	     redirectUrl = "process-waiver-pending-activities.jspa?approve=true";
         return "process-waiver-pending-activities";
    }
    public void approveWaiver(Long waiverID, String comment)
    {
    	projectWaiver = processWaiverManagerNew.get(waiverID);
    	projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.APPROVED.value());
		
    	if(comment!=null)
    	{
    		projectWaiver.setApproverComments(comment);
    	}
		
		// Email Notifications when Process Waiver is Approved
		try
		{
    		String initiatorEmail = userManager.getUser(projectWaiver.getCreationBy()).getEmail();
            EmailMessage email = stageManager.populateNotificationEmail(initiatorEmail, null, null,"approve.process.waiver.htmlBody","approve.process.waiver.subject");
            String baseUrl = URLUtils.getBaseURL(request);
            //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/proposal-details!input.jspa?projectID=" + projectID;
            String waiverUrl = baseUrl+"/new-synchro/project-waiver!input.jspa?projectWaiverID=" + waiverID;
           
            email.getContext().put("waiverId", SynchroUtils.generateProjectCode(waiverID));
            email.getContext().put("waiverName",projectWaiver.getName());
            email.getContext().put("waiverUrl",waiverUrl);
            //This is the Approver Name
            email.getContext().put ("waiverApprover",getUser().getName());
            
            String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
            String adminName = SynchroPermHelper.getSystemAdminName();
            
            stageManager.sendNotificationNew(adminName, adminEmail,email);

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(waiverID);
            
            emailNotBean.setStageID(SynchroConstants.PROCESS_WAIVER_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("Notification | Process Waiver Approved");
	    	emailNotBean.setEmailSubject("Notification | Process Waiver Approved");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(initiatorEmail);

            emailNotificationManager.saveDetails(emailNotBean);
            this.projectWaiver = processWaiverManagerNew.update(projectWaiver);
            
            String i18Text = getText("logger.process.waiver.approve"); 
    		SynchroLogUtilsNew.addProcessWaiverLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROCESS_WAIVER.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
   					SynchroGlobal.LogProjectStage.PROJECT_WAIVER.getId(), i18Text , "" , new Long(-1L), getUser().getID(), waiverID);
		}
		catch(UserNotFoundException ue)
        {
            LOGGER.error("User not found while Process Waiver is approved" + projectWaiver.getCreationBy());
        }
    }
    
    public String approveMultipleProcessWaiver()
    {
    	
    	String multipleWaiverIds = getRequest().getParameter("multipleWaiverIds");
    	if(multipleWaiverIds!=null)
    	{
    	//	multipleProjectIds = multipleProjectIds.replaceAll("[", "").replaceAll("]", "");
    	
    		if(multipleWaiverIds.contains(","))
    		{
    			String[] splitWaiverId = multipleWaiverIds.split(",");
    			for(int i=0;i<splitWaiverId.length;i++)
    			{
    				Long projectID = new Long(splitWaiverId[i]);
    				approveWaiver(projectID, null);
    			}
    		}
    		else
    		{
    			Long waiverID = new Long(multipleWaiverIds);
				approveWaiver(waiverID, null);
    		}
    	
    	}
    	redirectUrl = "process-waiver-pending-activities.jspa?approve=true";
         return "process-waiver-pending-activities";
    }
    
    public String rejectMultipleProcessWaiver()
    {
    	
    	String multipleWaiverIds = getRequest().getParameter("multipleRejectWaiverIds");
    	if(multipleWaiverIds!=null)
    	{
    	//	multipleProjectIds = multipleProjectIds.replaceAll("[", "").replaceAll("]", "");
    	
    		if(multipleWaiverIds.contains(","))
    		{
    			String[] splitWaiverId = multipleWaiverIds.split(",");
    			for(int i=0;i<splitWaiverId.length;i++)
    			{
    				Long projectID = new Long(splitWaiverId[i]);
    				rejectWaiver(projectID, null);
    			}
    		}
    		else
    		{
    			Long waiverID = new Long(multipleWaiverIds);
    			rejectWaiver(waiverID, null);
    		}
    	
    	}
    	redirectUrl = "process-waiver-pending-activities.jspa?reject=true";
         return "process-waiver-pending-activities";
    }
    
    public void rejectWaiver(Long waiverID, String comment)
    {
    	projectWaiver = processWaiverManagerNew.get(waiverID);
    	projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.REJECTED.value());
    	if(comment!=null)
    	{
    		projectWaiver.setApproverComments(comment);
    	}
		
		// Email Notifications when Process Waiver is Approved
		try
		{
			String initiatorEmail = userManager.getUser(projectWaiver.getCreationBy()).getEmail();
            EmailMessage email = stageManager.populateNotificationEmail(initiatorEmail, null, null,"reject.process.waiver.htmlBody","reject.process.waiver.subject");
            String baseUrl = URLUtils.getBaseURL(request);
            //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/proposal-details!input.jspa?projectID=" + projectID;
            String waiverUrl = baseUrl+"/synchro/project-waiver!input.jspa?projectWaiverID=" + waiverID;
           
            email.getContext().put("waiverId", SynchroUtils.generateProjectCode(waiverID));
            email.getContext().put("waiverName",projectWaiver.getName());
            email.getContext().put("waiverUrl",waiverUrl);
               	
            //This is the Rejector Name
            email.getContext().put ("waiverRejector",getUser().getName());
            
            String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
            String adminName = SynchroPermHelper.getSystemAdminName();
            
            stageManager.sendNotificationNew(adminName, adminEmail,email);

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(waiverID);
            
            emailNotBean.setStageID(SynchroConstants.PROCESS_WAIVER_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("Notification | Process Waiver Rejected");
	    	emailNotBean.setEmailSubject("Notification | Process Waiver Rejected");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(initiatorEmail);

            emailNotificationManager.saveDetails(emailNotBean);
            this.projectWaiver = processWaiverManagerNew.update(projectWaiver);
            
            String i18Text = getText("logger.process.waiver.reject"); 
    		SynchroLogUtilsNew.addProcessWaiverLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROCESS_WAIVER.getId(), SynchroGlobal.Activity.REJECT.getId(), 
   					SynchroGlobal.LogProjectStage.PROJECT_WAIVER.getId(), i18Text , "" , new Long(-1L), getUser().getID(), waiverID);
		}
		catch(UserNotFoundException ue)
        {
            LOGGER.error("User not found while Process Waiver is Rejected" + projectWaiver.getCreationBy());
        }
    }
    
    
    public String updateWaiver()
    {
    	Long waiverID=null;
		String id = getRequest().getParameter("updateWaiverID");
		if(id  != null ) 
	    {
	         try{
	             if(waiverID==null)
	            	 waiverID = Long.parseLong(id);
	         } catch (NumberFormatException nfEx) {
	        	 LOGGER.error("Invalid waiverID ");
	             throw nfEx;
	         }
	         
	    }
		
		String approverCommentParam = "approverComment";
		String waiverActionParam = "processWaiverAction";
		
		String approverComment = getRequest().getParameter(approverCommentParam);
		
		String waiverAction =  getRequest().getParameter(waiverActionParam);
		
		
		if(waiverAction!=null && waiverAction.equalsIgnoreCase("Approve"))
		{
			approveWaiver(waiverID,approverComment );
		}
		
		if(waiverAction!=null && waiverAction.equalsIgnoreCase("Reject"))
		{
			rejectWaiver(waiverID,approverComment );
		}
		
    	
        redirectUrl = "process-waiver-pending-activities.jspa";
        return "process-waiver-pending-activities";
    }
    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public List<ProjectWaiverCatalogueBean> getWaiverPendingActivities() {
        return waiverPendingActivities;
    }


    public ProjectResultFilter getWaiverResultFilter() {
        return waiverResultFilter;
    }

    public void setWaiverResultFilter(ProjectResultFilter waiverResultFilter) {
        this.waiverResultFilter = waiverResultFilter;
    }

    public Integer getPageLimit() {
        return pageLimit;
    }

    public void setPageLimit(Integer pageLimit) {
        this.pageLimit = pageLimit;
    }

	public ProcessWaiverManagerNew getProcessWaiverManagerNew() {
		return processWaiverManagerNew;
	}

	public void setProcessWaiverManagerNew(
			ProcessWaiverManagerNew processWaiverManagerNew) {
		this.processWaiverManagerNew = processWaiverManagerNew;
	}

	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}

	public ProjectWaiver getProjectWaiver() {
		return projectWaiver;
	}

	public void setProjectWaiver(ProjectWaiver projectWaiver) {
		this.projectWaiver = projectWaiver;
	}

	public InputStream getDownloadStream() {
		return downloadStream;
	}

	public void setDownloadStream(InputStream downloadStream) {
		this.downloadStream = downloadStream;
	}

	public String getDownloadFilename() {
		return downloadFilename;
	}

	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

	public String getDownloadStreamType() {
		return downloadStreamType;
	}

	public void setDownloadStreamType(String downloadStreamType) {
		this.downloadStreamType = downloadStreamType;
	}

	public Integer getPlimit() {
		return plimit;
	}

	public void setPlimit(Integer plimit) {
		this.plimit = plimit;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

  
}
