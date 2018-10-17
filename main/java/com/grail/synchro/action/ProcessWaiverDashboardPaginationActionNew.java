package com.grail.synchro.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverCatalogueBean;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProcessWaiverManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.StringUtils;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
@Decorate(false)
public class ProcessWaiverDashboardPaginationActionNew extends JiveActionSupport {

    private static Logger LOG = Logger.getLogger(ProcessWaiverDashboardPaginationActionNew.class);

    private List<ProjectWaiverCatalogueBean> projectCatalogues = new ArrayList<ProjectWaiverCatalogueBean>();
    private Integer page = 1;
    private Integer results;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_WAIVER_CATALOGUE_PAGE_LIMIT, 10);
    private String keyword;
    private SynchroUtils synchroUtils;
    private ProcessWaiverManagerNew processWaiverManagerNew;
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private List<ProjectWaiver> projectWaivers = new ArrayList<ProjectWaiver>();
    private String sortField;
    private Integer ascendingOrder;
    private PIBManagerNew pibManagerNew;
    private ProjectManagerNew synchroProjectManagerNew;
    
    private Integer plimit;
    
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    private int columnWidth = 8000;
    
    public String getSortField() {
		return sortField;
	}

	public Integer getAscendingOrder() {
		return ascendingOrder;
	}
	
	public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    @Override
    public String execute() {
    	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    
    	/*if(!getSynchroUtils().canAccessProjectWaiver(getUser()))
    	{
    		return UNAUTHORIZED;
    	}
    	*/
    	
    	if(plimit!=null && plimit > 0)
    	{
    		limit=plimit;
    	}
    	
    	ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin"))) {
            this.projectResultFilter.setStartDateBegin(DateUtils.parse(getRequest().getParameter("startDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateComplete"))) {
            this.projectResultFilter.setStartDateComplete(DateUtils.parse(getRequest().getParameter("startDateComplete")));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin"))) {
            this.projectResultFilter.setEndDateBegin(DateUtils.parse(getRequest().getParameter("endDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateComplete"))) {
            this.projectResultFilter.setEndDateComplete(DateUtils.parse(getRequest().getParameter("endDateComplete")));
        }
        
        if(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroSystemOwner())
		{
        	projectWaivers = processWaiverManagerNew.getAllByResultFilter(projectResultFilter);	
		}
		else
		{
			projectWaivers = processWaiverManagerNew.getAllByResultFilter(getUser(), projectResultFilter);	
		}
		
		if(projectWaivers != null && projectWaivers.size() > 0)
		{
			results = projectWaivers.size();
			List<ProjectWaiverCatalogueBean> projectCataloguesAll =  toProjectWaiverCatalogueBeans(projectWaivers);
			results=0;
			pages=0;
			 if(projectCataloguesAll != null && keyword != null && !keyword.trim().equals(""))
			 {
				 projectCatalogues = searchProjectWaiverList(projectCataloguesAll);
				 projectCatalogues = sortObjectList(projectCatalogues);
				 if(projectCatalogues != null && projectCatalogues.size() > 0)
				 {
					 //results = projectCataloguesAll.size();
					 results = projectCatalogues.size();
				     pages = results%limit==0?results/limit:results/limit+1;
				     setPaginationFilter(page,results);
				    // projectCatalogues = projectCataloguesAll.subList(start, end);
				     
				     // This is done for setting correct pagination in case of Keyword search
				     projectCatalogues = projectCatalogues.subList(start, end);
				 }
			 }
			 
			 else if(projectCataloguesAll != null && (keyword == null || keyword.trim().equals("")))
			 {
				 projectCataloguesAll = sortObjectList(projectCataloguesAll);
				 results = projectCataloguesAll.size();
			     pages = results%limit==0?results/limit:results/limit+1;
			     setPaginationFilter(page,results);
			     projectCatalogues = projectCataloguesAll.subList(start, end);
			 }
		}
		return SUCCESS;
    }

    public String downloadReport()
    {
    	downloadFilename = "ProcessWaiverDashboard.xls";
    	downloadStreamType = "application/vnd.ms-excel";
    	
    	String downloadReportPage = getRequest().getParameter("downloadReportPage");
		String downloadReportKeyword = getRequest().getParameter("downloadReportKeyword");
		
		
		if(plimit!=null && plimit > 0)
    	{
    		limit=plimit;
    	}
		page = Integer.parseInt(downloadReportPage);
		keyword = downloadReportKeyword;
		
    	ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin"))) {
            this.projectResultFilter.setStartDateBegin(DateUtils.parse(getRequest().getParameter("startDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateComplete"))) {
            this.projectResultFilter.setStartDateComplete(DateUtils.parse(getRequest().getParameter("startDateComplete")));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin"))) {
            this.projectResultFilter.setEndDateBegin(DateUtils.parse(getRequest().getParameter("endDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateComplete"))) {
            this.projectResultFilter.setEndDateComplete(DateUtils.parse(getRequest().getParameter("endDateComplete")));
        }
         
        
  	 	projectResultFilter.setLimit(null);
  	 	projectResultFilter.setStart(null);
  	 	 
        if(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroSystemOwner())
		{
        	projectWaivers = processWaiverManagerNew.getAllByResultFilter(projectResultFilter);	
		}
		else
		{
			projectWaivers = processWaiverManagerNew.getAllByResultFilter(getUser(), projectResultFilter);	
		}
		
		if(projectWaivers != null && projectWaivers.size() > 0)
		{
			results = projectWaivers.size();
			List<ProjectWaiverCatalogueBean> projectCataloguesAll =  toProjectWaiverCatalogueBeans(projectWaivers);
			results=0;
			pages=0;
			 if(projectCataloguesAll != null && keyword != null && !keyword.trim().equals(""))
			 {
				 projectCatalogues = searchProjectWaiverList(projectCataloguesAll);
				 projectCatalogues = sortObjectList(projectCatalogues);
				 if(projectCatalogues != null && projectCatalogues.size() > 0)
				 {
					 //results = projectCataloguesAll.size();
					 results = projectCatalogues.size();
				     pages = results%limit==0?results/limit:results/limit+1;
				     setPaginationFilter(page,results);
				     //projectCatalogues = projectCataloguesAll.subList(start, end);
				     //projectCatalogues = projectCatalogues.subList(start, end);
				 }
			 }
			 
			 else if(projectCataloguesAll != null && (keyword == null || keyword.trim().equals("")))
			 {
				 projectCataloguesAll = sortObjectList(projectCataloguesAll);
				 results = projectCataloguesAll.size();
			     pages = results%limit==0?results/limit:results/limit+1;
			     setPaginationFilter(page,results);
			     //projectCatalogues = projectCataloguesAll.subList(start, end);
			     projectCatalogues = projectCataloguesAll;
			 }
		}
		
		
    
    	 
       
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
				0, "Process Waivers - Download Report", "", -1L, getUser().getID());
        
        return DOWNLOAD_REPORT;

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
   	 
    	 Calendar calendar = Calendar.getInstance();
         String timeStamp = calendar.get(Calendar.YEAR) +
                 "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                 "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                 "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                 "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                 "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
        
        Integer startRow = 4;
        HSSFSheet sheet = workbook.createSheet("ProcessWaiverDashboard");
       
        
        
        StringBuilder generatedBy = new StringBuilder();
        generatedBy.append("Process Waiver Report ").append("\n");
       
        String userName = getUser().getName();
        generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
       
        if(StringUtils.isNotBlank(projectResultFilter.getWaiverInitiator()) )
        {
        	selectedFilters.add("Initiator - "+ projectResultFilter.getWaiverInitiator());
        }
        if(projectResultFilter.getStartDateBegin()!=null || projectResultFilter.getStartDateComplete()!=null)
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
        if(projectResultFilter.getEndDateBegin()!=null || projectResultFilter.getEndDateComplete()!=null)
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
        if(projectResultFilter.getWaiverStatus()!=null && projectResultFilter.getWaiverStatus().size()>0)
        {
        	selectedFilters.add("Status - "+ SynchroUtils.getProcessWaiverStatusName(projectResultFilter.getWaiverStatus()));
        }
        
        if(projectResultFilter.getResearchEndMarkets()!=null && projectResultFilter.getResearchEndMarkets().size()>0)
        {
        	selectedFilters.add("Research End-Market(s) - "+ SynchroUtils.getEndMarketNames(projectResultFilter.getResearchEndMarkets()));
        } 
        if(projectResultFilter.getBrands()!=null && projectResultFilter.getBrands().size()>0)
        {
        	selectedFilters.add("Branded/Non-Branded - "+SynchroUtils.getBrandNames(projectResultFilter.getBrands()));
        }

        generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
        
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
        
        sheet.addMergedRegion(new CellRangeAddress(0,3,0,10));
        sheet.addMergedRegion(new CellRangeAddress(4,7,0,10));
        
        startRow = startRow + 4;
        reportTypeHeader = sheet.createRow(startRow);
        
        
        
        
        
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(0);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Waiver ID");
        sheet.setColumnWidth(0, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(1);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Waiver Name");
        sheet.setColumnWidth(1, columnWidth);
                
        reportTypeHeaderColumn = reportTypeHeader.createCell(2);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Research End Market(s)");
        sheet.setColumnWidth(2, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(3);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Brand");
        sheet.setColumnWidth(3, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(4);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Rationale For Waiver");
        sheet.setColumnWidth(4, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(5);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Waiver Initiator");
        sheet.setColumnWidth(5, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(6);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Date of Request");
        sheet.setColumnWidth(6, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(7);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Approver");
        sheet.setColumnWidth(7, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(8);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Status");
        sheet.setColumnWidth(8, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(9);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Date of Last Update");
        sheet.setColumnWidth(9, columnWidth);
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(10);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Approver's Comment");
        sheet.setColumnWidth(10, columnWidth);
        
        
        CellStyle noStyle = workbook.createCellStyle();
        
        noStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        noStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        noStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        noStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        sheetHeaderFont.setFontName("Calibri");
        noStyle.setFont(callibiriFont);
        noStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        
        
        if(projectCatalogues!=null && projectCatalogues.size()>0)
        {
	         for(ProjectWaiverCatalogueBean waiver : projectCatalogues)
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
		         if(waiver.getBrand()!=null)
		         {
		        	 dataCell.setCellValue(waiver.getBrand());
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
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
		         if(waiver.getStatus()!=null)
		         {
		        	 dataCell.setCellValue(waiver.getStatus());
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(waiver.getModifiedDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new Date(waiver.getModifiedDate())));
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		         
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(waiver.getApproverComments()!=null)
		         {
		        	 dataCell.setCellValue(waiver.getApproverComments().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		         
		         startRow++;
		         
	         }
	         
	        
         	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+8,0,10));
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
	    	
        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+8,0,10);
        	
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
			                    
			                    
			                    // Fetch only Process Waivers here and not the Agency Waivers, as we have separate dashoboard for Agency Waivers.
			                    
			                    
			                    bean.setWaiverUrl("/new-synchro/project-waiver!input.jspa?projectWaiverID="+projectWaiver.getWaiverID());
		                    	bean.setWaiverName(projectWaiver.getName());
			                    bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
			                    bean.setYear(year);
			                    processCountryAndRegion(bean, projectWaiver);
			                    bean.setBrand(SynchroGlobal.getBrands(true, null).get(projectWaiver.getBrand().intValue()));
			                    bean.setApprover(userManager.getUser(projectWaiver.getApproverID()).getName());
			                    bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
			                    
			                    bean.setSummary(projectWaiver.getSummary());
			                    bean.setCreationDate(projectWaiver.getCreationDate());
			                    bean.setModifiedDate(projectWaiver.getModifiedDate());
			                    bean.setApproverComments(projectWaiver.getApproverComments());
			                    
			                    projectCataloguesAll.add(bean);
			                    
			                    /*
			                     * 
			                     * if(projectResultFilter.getStartYear().intValue() == -1 || year.intValue() == projectResultFilter.getStartYear().intValue())
		                		{
		                			if(projectWaiver.getIsKantar()!=null && projectWaiver.getIsKantar())
			                    {
			                    	 // Set The URLs
			                    	 Long projectId = pibManagerNew.getProjectId(projectWaiver.getWaiverID());
			                    	 // This check has been added to handle the scenario in which the Process Waiver Id exists but there in no project id corresponding to that.
			                    	 if(projectId!=null && projectId.longValue() > 0)
			                    	 {
				                    	 Project project = synchroProjectManagerNew.get(projectId);
				                    	 if(project.getMultiMarket())
				                    	 {
				                    		 bean.setWaiverUrl("/new-synchro/pib-multi-details!input.jspa?projectID="+projectId);
				                    		 bean.setCountry("Multi-Market");
				                    	 }
				                    	 else
				                    	 {
				                    		 bean.setWaiverUrl("/new-synchro/pib-details!input.jspa?projectID="+projectId);
				                    		 List<Long> emIds = synchroProjectManagerNew.getEndMarketIDs(projectId);
				                    		// bean.setCountry(SynchroGlobal.getEndMarkets().get(emIds.get(0).intValue()));
				                    		 if(emIds!=null && emIds.size()>0)
				                    		 {
				                    			 bean.setCountry(SynchroGlobal.getEndMarkets().get(emIds.get(0).intValue()));
				                    		 }
				                    		 else
				                    		 {
				                    			 bean.setCountry("");
				                    		 }
				                    	 }
				                    	 bean.setWaiverName(project.getName());
						                 bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
						                 bean.setYear(year);
						               //  bean.setCountry("N/A");
						                 bean.setRegion("");
						                 bean.setBrand("");
						                 bean.setApprover(userManager.getUser(projectWaiver.getApproverID()).getName());
						                 bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
						                 projectCataloguesAll.add(bean);
			                    	 }
			                    }
			                    else
			                    {
				                    bean.setWaiverUrl("/new-synchro/project-waiver!input.jspa?projectWaiverID="+projectWaiver.getWaiverID());
			                    	bean.setWaiverName(projectWaiver.getName());
				                    bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
				                    bean.setYear(year);
				                    processCountryAndRegion(bean, projectWaiver);
				                    bean.setBrand(SynchroGlobal.getBrands(true, null).get(projectWaiver.getBrand().intValue()));
				                    bean.setApprover(userManager.getUser(projectWaiver.getApproverID()).getName());
				                    bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
				                    projectCataloguesAll.add(bean);
			                    }
			                    }*/
			                   // projectCataloguesAll.add(bean);
		                
	                } catch (UserNotFoundException e) {
	                    LOG.info(e.getMessage(), e);
	                }
            }
        }
      return projectCataloguesAll;
    }

	private List<ProjectWaiverCatalogueBean>  searchProjectWaiverList(final List<ProjectWaiverCatalogueBean> projectCataloguesAll)
	{
		List<ProjectWaiverCatalogueBean> projectCatalogues = new ArrayList<ProjectWaiverCatalogueBean> ();
		for(ProjectWaiverCatalogueBean projectWaiverBean : projectCataloguesAll)
		{
			//if((projectWaiverBean.getWaiverID().toString()).contains(keyword) || projectWaiverBean.getWaiverName().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getInitiator().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getRegion().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getCountry().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getBrand().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getStatus().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getApprover().toUpperCase().contains(keyword.toUpperCase()))
			try
			{
				if((projectWaiverBean.getWaiverID().toString()).contains(keyword) || (projectWaiverBean.getWaiverName()!=null && projectWaiverBean.getWaiverName().toUpperCase().contains(keyword.toUpperCase())) || (projectWaiverBean.getInitiator()!=null && projectWaiverBean.getInitiator().toUpperCase().contains(keyword.toUpperCase())) || (projectWaiverBean.getCountry()!=null && projectWaiverBean.getCountry().toUpperCase().contains(keyword.toUpperCase())) || (projectWaiverBean.getStatus()!=null && projectWaiverBean.getStatus().toUpperCase().contains(keyword.toUpperCase())) || (projectWaiverBean.getApprover()!=null && projectWaiverBean.getApprover().toUpperCase().contains(keyword.toUpperCase())))
				{
					projectCatalogues.add(projectWaiverBean);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return projectCatalogues;
		
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
    
	
	private void setPaginationFilter(int page,int results) {
		 
		if(plimit!=null && plimit > 0)
    	{
			limit=plimit;
    	}
		start = (this.page-1)*limit;
		 end = start + limit;
		 end = end>=this.results?this.results:end;
	}

    public List<ProjectWaiverCatalogueBean> getProjectCatalogues() {
        return projectCatalogues;
    }

    public void setProjectCatalogues(List<ProjectWaiverCatalogueBean> projectCatalogues) {
        this.projectCatalogues = projectCatalogues;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getResults() {
        return results;
    }

    public void setResults(Integer results) {
        this.results = results;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
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

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    
    
    private List<ProjectWaiverCatalogueBean> sortObjectList(List<ProjectWaiverCatalogueBean> list)
	{		
		if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().trim().equals(""))
		{
			if(projectResultFilter.getSortField().equals("id"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByID());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByIDDesc());
				}
				
			}
			else if(projectResultFilter.getSortField().equals("name"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByName());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByNameDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("owner"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByOwner());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByOwnerDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("year"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByYear());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByYearDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("brand"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByBrand());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByBrandDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("approver"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByApprover());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByApproverDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("status"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByStatus());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByStatusDesc());
				}
			}

			sortField = projectResultFilter.getSortField();
			ascendingOrder = projectResultFilter.getAscendingOrder();
		}
		
		return list;
	}

	public ProcessWaiverManagerNew getProcessWaiverManagerNew() {
		return processWaiverManagerNew;
	}

	public void setProcessWaiverManagerNew(
			ProcessWaiverManagerNew processWaiverManagerNew) {
		this.processWaiverManagerNew = processWaiverManagerNew;
	}
	public PIBManagerNew getPibManagerNew() {
		return pibManagerNew;
	}

	public void setPibManagerNew(PIBManagerNew pibManagerNew) {
		this.pibManagerNew = pibManagerNew;
	}

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public void setSynchroProjectManagerNew(ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}

	public Integer getPlimit() {
		return plimit;
	}

	public void setPlimit(Integer plimit) {
		this.plimit = plimit;
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
}
