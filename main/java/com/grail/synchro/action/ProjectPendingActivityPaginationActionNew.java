package com.grail.synchro.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroReminderUtils;
import com.grail.synchro.util.SynchroUtils;

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

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectPendingActivityViewBean;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.util.DateUtils;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.util.StringUtils;

@Decorate(false)
public class ProjectPendingActivityPaginationActionNew extends JiveActionSupport {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProjectPendingActivityPaginationActionNew.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private List<ProjectPendingActivityViewBean> pendingActivities;
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private ProjectManagerNew synchroProjectManagerNew;
    private DocumentManager documentManager;
    private CommunityManager communityManager;
    private StageManager stageManager;
    private String sortField;
    private Integer ascendingOrder;
    
    private Integer plimit;

    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }


    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }
/*
    public void setCommunityManager(CommunityManager communityManager) {
        this.communityManager = communityManager;
    }*/

    public void setSynchroProjectManagerNew(ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
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

        ProjectResultFilter filter = getSearchFilter();
        setPagination(synchroProjectManagerNew.getPendingActivitiesTotalCount(filter, getUser().getID()).intValue());
        updatePage();

      //  synchroProjectManagerNew.updatePendingActivityViews(SynchroReminderUtils.getPendingActivitySearchFilter(), getUser().getID());
        return SUCCESS;

    }
    
    public String downloadReport()
    {
    	downloadFilename = "PendingActionProjects.xls";
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
				0, "Projects Pending Activities - Download Report", "", -1L, getUser().getID());
        
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
        pendingActivities = synchroProjectManagerNew.getPendingActivities(getSearchFilter(), getUser().getID());
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
    	 
    	 projectResultFilter = getSearchFilter();
    	 
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
        
   	 	 projectResultFilter.setLimit(null);
   	 	 projectResultFilter.setStart(null);
   	 	 
    	 pendingActivities = synchroProjectManagerNew.getPendingActivities(projectResultFilter, getUser().getID());
         
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
         
         sheet.addMergedRegion(new CellRangeAddress(0,3,0,16));
         sheet.addMergedRegion(new CellRangeAddress(4,7,0,16));
         
         startRow = startRow + 4;
         
         reportTypeHeader = sheet.createRow(startRow);
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(0);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Code");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(1);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Name");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(2);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Description");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(3);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Creation Date");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(4);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Last Update Date");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(5);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Stage");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(6);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Category");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(7);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Research End Market(s)");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(8);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Start Date");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(9);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project End Date");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(10);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("SP&I Contact");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(11);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Initiator");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(12);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Methodology");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(13);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Methodology Type");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(14);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Budget Location");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(15);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Budget Year");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(16);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Action Pending");
         
         
         CellStyle noStyle = workbook.createCellStyle();
         
         noStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         
         sheetHeaderFont.setFontName("Calibri");
         noStyle.setFont(callibiriFont);
         noStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
         
         
         
         if(pendingActivities!=null && pendingActivities.size()>0)
         {
	         for(ProjectPendingActivityViewBean projectBean : pendingActivities)
	         {
	        	 int cellNo = 0;
	        	 HSSFRow dataRow = sheet.createRow(startRow + 1);
	        	 
		         HSSFCell dataCell = dataRow.createCell(cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getProjectID());
		        
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getProjectName());
		         
		         Project project = synchroProjectManagerNew.get(projectBean.getProjectID());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(project.getDescriptionText()!=null)
		         {
		        	 dataCell.setCellValue(project.getDescriptionText().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		       
		         
		       
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(project.getCreationDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new Date(project.getCreationDate())));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(project.getModifiedDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new Date(project.getModifiedDate())));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         try
        		 {
		        	 dataCell.setCellValue(SynchroGlobal.ProjectStatusNew.getName(project.getStatus()));
        		 }
        		 catch(Exception e)
        		 {	
        			 dataCell.setCellValue("");
        		 }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         List<String> categoryNames = new ArrayList<String>();
		         for(Long categId: project.getCategoryType())
		         {
		        	 if(SynchroGlobal.getProductTypes().get(categId.intValue())!=null)
		        	 {
		        		 categoryNames.add(SynchroGlobal.getProductTypes().get(categId.intValue()));
		        	 }
		         }
		         if(categoryNames!=null && categoryNames.size()>0)
		         {
		        	 dataCell.setCellValue(StringUtils.join(categoryNames, ","));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         List<Long> emIds = synchroProjectManagerNew.getEndMarketIDs(project.getProjectID());
        		 if(emIds!=null && emIds.size()>0)
        		 {
        			List<String> endMarketNames=new ArrayList<String>();
        			for(Long endMarketId : emIds )
        			{
        				endMarketNames.add(SynchroGlobal.getEndMarkets().get(endMarketId.intValue()));
        			}
        			dataCell.setCellValue(StringUtils.join(endMarketNames, ","));
        			
        		 }
        		 else
        		 {
        			 dataCell.setCellValue("");
        		 }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(projectBean.getStartDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getStartDate()));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(project.getEndDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(project.getEndDate()));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getProjectManager());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         try
		         {
		        	 dataCell.setCellValue(userManager.getUser(project.getCreationBy()).getName());
		         }
		         catch(UserNotFoundException e )
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getMethodology());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(SynchroGlobal.getProjectIsMapping().get(project.getMethodologyType().intValue())!=null)
		         {
		        	 dataCell.setCellValue(SynchroGlobal.getProjectIsMapping().get(project.getMethodologyType().intValue()));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(SynchroUtils.getBudgetLocationName(project.getBudgetLocation()));
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(project.getBudgetYear()+"");
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getPendingActivity());
		         
		         
		         startRow++;
		         
	         }
	         
	         	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+8,0,16));
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
		    	
	        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+8,0,16);
	        	
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
        projectResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());

        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
            // Fetch only those projects which are in PIT OPEN, PIB OPEN or Other Stage Open stage (Proposal, Project Specs, Report Summary),
            // and Completed projects as well (for showing Project Evaluation pending projects)
//            statuses.add(new Long(SynchroGlobal.Status.PIT_OPEN.ordinal()));
           
            
            
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal()));
            
           
            
            projectResultFilter.setProjectStatusFields(statuses);
        }

        if(projectResultFilter.getKeyword() == null || projectResultFilter.getKeyword().equals("")) {
            projectResultFilter.setKeyword(keyword);
        }
        if(projectResultFilter.getStart() == null) {
            projectResultFilter.setStart(start);
        }
       
        
        if(plimit!=null && plimit > 0)
        {
        	projectResultFilter.setLimit(plimit);
        }
        else
        {
        	projectResultFilter.setLimit(LIMIT);
        }

        
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
        
        if(StringUtils.isNotBlank(getRequest().getParameter("creationDateBegin"))) {
            this.projectResultFilter.setCreationDateBegin(DateUtils.parse(getRequest().getParameter("creationDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("creationDateComplete"))) {
            this.projectResultFilter.setCreationDateComplete(DateUtils.parse(getRequest().getParameter("creationDateComplete")));
        }
        
        if(projectResultFilter.getProjectStages()!=null && projectResultFilter.getProjectStages().size()>0)
        {
        	selectedFilters.add("Project Stage - "+ SynchroUtils.getProjectStageName(projectResultFilter.getProjectStages()));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("creationDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("creationDateComplete")) ) 
        {
        	//selectedFilters.add("Creation Date");
        	if(StringUtils.isNotBlank(getRequest().getParameter("creationDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("creationDateComplete")))
        		{
        			selectedFilters.add("Creation Date Between - " + getRequest().getParameter("creationDateBegin") + " and "+getRequest().getParameter("creationDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Creation Date Between - " +getRequest().getParameter("creationDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Creation Date Between - " +getRequest().getParameter("creationDateComplete"));
        	}
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("startDateComplete")) ) 
        {
        	//selectedFilters.add("Project Started Between");
        	if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("startDateComplete")))
        		{
        			selectedFilters.add("Project Started Between - " + getRequest().getParameter("startDateBegin") + " and "+getRequest().getParameter("startDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Project Started Between - " +getRequest().getParameter("startDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Project Started Between - " +getRequest().getParameter("startDateComplete"));
        	}
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("endDateComplete")) ) 
        {
        	//selectedFilters.add("Project Completed Between");
        	if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("endDateComplete")))
        		{
        			selectedFilters.add("Project Completed Between - " + getRequest().getParameter("endDateBegin") + " and "+getRequest().getParameter("endDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Project Completed Between - " + getRequest().getParameter("endDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Project Completed Between - " + getRequest().getParameter("endDateComplete"));
        	}
        }
        
        if(projectResultFilter.getActionPendings()!=null && projectResultFilter.getActionPendings().size()>0)
        {
        	selectedFilters.add("Action Pending - " + SynchroUtils.getPendingActionName(projectResultFilter.getActionPendings()));
        }
        
        if(StringUtils.isNotBlank(projectResultFilter.getProjManager())) 
        {
        	selectedFilters.add("SP&I Contact - " + projectResultFilter.getProjManager());
        }
        
        if(projectResultFilter.getCategoryTypes()!=null && projectResultFilter.getCategoryTypes().size()>0)
        {
        	selectedFilters.add("Category Type - "+  SynchroUtils.getCategoryNames(projectResultFilter.getCategoryTypes()));
        }
        
        if(projectResultFilter.getResearchEndMarkets()!=null && projectResultFilter.getResearchEndMarkets().size()>0)
        {
        	selectedFilters.add("Research End-Market(s) - "+ SynchroUtils.getEndMarketNames(projectResultFilter.getResearchEndMarkets()));
        }
        if(projectResultFilter.getMethDetails()!=null && projectResultFilter.getMethDetails().size()>0)
        {
        	selectedFilters.add("Methodology - "+ SynchroUtils.getMethodologyNames(projectResultFilter.getMethDetails()));
        }
        if(projectResultFilter.getMethodologyTypes()!=null && projectResultFilter.getMethodologyTypes().size()>0)
        {
        	selectedFilters.add("Methodology Type - "+SynchroUtils.getMethodologyTypeNames(projectResultFilter.getMethodologyTypes()));
        }
        if(projectResultFilter.getBudgetLocations()!=null && projectResultFilter.getBudgetLocations().size()>0)
        {
        	selectedFilters.add("Budget Location - "+SynchroUtils.getBudgetLocationNames(projectResultFilter.getBudgetLocations()));
        }
        
        return projectResultFilter;
    }


    public StageManager getStageManager() {
        return stageManager;
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public List<ProjectPendingActivityViewBean> getPendingActivities() {
        return pendingActivities;
    }

    public void setPendingActivities(List<ProjectPendingActivityViewBean> pendingActivities) {
        this.pendingActivities = pendingActivities;
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
