package com.grail.synchro.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectDashboardViewBeanNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroRawExtractUtil;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.StringUtils;

@Decorate(false)
public class RegionalOpenDashboardPaginationAction extends JiveActionSupport{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RegionalOpenDashboardPaginationAction.class);
    private List<ProjectDashboardViewBeanNew> projects = null;
    private Integer page = 1;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, 10);
    private Integer results;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;
    private ProjectManagerNew synchroProjectManagerNew;
    private ProjectResultFilter projectResultFilter;
    private SynchroUtils synchroUtils;
    
    private Integer plimit;
    
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    private String downloadReportType;
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    
    public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }


    public String execute()
    {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }

        setPagination(synchroProjectManagerNew.getProjectDashboardCount(getSearchFilter()).intValue());
        updatePage();
        return SUCCESS;

    }
    
    public String downloadReport()
    {
    	downloadFilename = "RegionalOpenProjectsDashboard.xls";
    	downloadStreamType = "application/vnd.ms-excel";
    	
    	String downloadReportPage = getRequest().getParameter("downloadReportPage");
		String downloadReportKeyword = getRequest().getParameter("downloadReportKeyword");
		
		String downloadReportLimit = getRequest().getParameter("downloadReportLimit");
		
		page = Integer.parseInt(downloadReportPage);
		keyword = downloadReportKeyword;
		limit = Integer.parseInt(downloadReportLimit);
    
    	 start = (page-1) * limit;
         end = start + limit;
       
        try
        {
        	if(downloadReportType!=null && downloadReportType.equals("ProjectSummary"))
        	{
        		processRawExtactReport();
        		//Audit Logs
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
        				0, "Regional Open Projects Summary - Download Report", "", -1L, getUser().getID());
        	}
        	else
        	{
        		processReport();
        		//Audit Logs
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
        				0, "Regional Open Projects Status - Download Report", "", -1L, getUser().getID());
        	}
        }
        
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        return DOWNLOAD_REPORT;

    }
    
    public void setPagination(final Integer count) {
    	if(plimit!=null && plimit > 0)
    	{
    		limit=plimit;
    	}
    	
    	if(count > limit) {
            double temp = count / (limit * 1.0);
            if(count%limit == 0) {
                pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }
    }

    public void updatePage() {
        start = (page-1) * limit;
        end = start + limit;
         
        //List<Project> projectList  = synchroProjectManagerNew.getProjects(getSearchFilter());
   
        projects = this.toProjectPaginationBeans(synchroProjectManagerNew.getProjects(getSearchFilter()));
        
    }
    
    
    private void processReport() throws IOException {
        HSSFWorkbook workbook = null;
        workbook = new HSSFWorkbook();
      
       
        workbook = generateReport(workbook);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
              
    }
    
    private void processRawExtactReport() throws IOException {
        HSSFWorkbook workbook = null;
        workbook = new HSSFWorkbook();
        downloadFilename = "RegionalOpenProjectsSummary.xls";
        projectResultFilter = getSearchFilter();
   	 	Calendar calendar = Calendar.getInstance();
        String timeStamp = calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
       
       StringBuilder generatedBy = new StringBuilder();
       generatedBy.append("Regional Open Projects Report ").append("\n");
       String userName = getUser().getName();
       generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
       generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
       
       
       projectResultFilter.setLimit(null);
 	 	 projectResultFilter.setStart(null);
 	 	 
       projects = this.toProjectPaginationBeans(synchroProjectManagerNew.getProjects(projectResultFilter));
       List<Long> projectIds = new ArrayList<Long>();
       
       if(projects!=null && projects.size()>0)
       {
    	   for(ProjectDashboardViewBeanNew bean : projects)
    	   {
    		   projectIds.add(bean.getProjectID());
    	   }
       }
       
        workbook = SynchroRawExtractUtil.generateRawExtract(workbook,generatedBy.toString(),"RegionalOpenProjects", projectIds);
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
        generatedBy.append("Regional Open Projects Report ").append("\n");
       
        String userName = getUser().getName();
        generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
       
       

        generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
 	 	 projectResultFilter.setLimit(null);
 	 	 projectResultFilter.setStart(null);
 	 	 
    	 projects = this.toProjectPaginationBeans(synchroProjectManagerNew.getProjects(projectResultFilter));
         
         
         Integer startRow = 4;
         HSSFSheet sheet = workbook.createSheet("RegionalOpenProjectsDashboard");
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
         
         sheet.addMergedRegion(new CellRangeAddress(0,3,0,20));
         sheet.addMergedRegion(new CellRangeAddress(4,7,0,20));
         
         startRow = startRow + 4;
         
         reportTypeHeader = sheet.createRow(startRow);
         
         int columnNo = 0;
	      reportTypeHeaderColumn = reportTypeHeader.createCell(columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Project Code");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Project Name");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Research End Market(s)");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Budget Year");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Project Status");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Project Start Date");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Project End Date");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Project Stage");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Methodology Defined");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Brief Uploaded");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Legal Approval Received - Brief");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Proposal Uploaded");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Legal Approval Received - Proposal");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Agency and Cost Break-Up Provided");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Report Provided");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Legal Approval Received");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("IRIS Summary Provided");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Legal Approval Received");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("TPD Summary Provided");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Legal Approval Received");
       
       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
       reportTypeHeaderColumn.setCellValue("Agency Review Done");
         
       	CellStyle greenStyle = workbook.createCellStyle();
         greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
         greenStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
         
         greenStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         greenStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
         greenStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
         greenStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         greenStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
         greenStyle.setFont(callibiriFont);
	 
         CellStyle redStyle = workbook.createCellStyle();
         redStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
         redStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
         redStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         redStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
         redStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
         redStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         redStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
         redStyle.setFont(callibiriFont);
         
         CellStyle noStyle = workbook.createCellStyle();
         
         noStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         sheetHeaderFont.setFontName("Calibri");
         noStyle.setFont(callibiriFont);
         noStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        if(projects!=null && projects.size()>0)
         {
	         for(ProjectDashboardViewBeanNew projectBean : projects)
	         {
	        	 int cellNo = 0;
	        	 HSSFRow dataRow = sheet.createRow(startRow + 1);
		         HSSFCell dataCell = dataRow.createCell(cellNo);
		         dataCell.setCellValue(projectBean.getProjectID());
			 dataCell.setCellStyle(noStyle);
		        
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellValue(projectBean.getProjectName());
			 dataCell.setCellStyle(noStyle);
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellValue(projectBean.getEndMarketName());
			 dataCell.setCellStyle(noStyle);
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellValue(projectBean.getBudgetYear());
			 dataCell.setCellStyle(noStyle);
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellValue(projectBean.getProjectTrackStatus());
			 dataCell.setCellStyle(noStyle);
		         
			 dataCell = dataRow.createCell(++cellNo);
	         dataCell.setCellStyle(noStyle);
	         if(projectBean.getStartDate()!=null)
	         {
	        	 String dateStr = df.format(projectBean.getStartDate());
	             dataCell.setCellValue(dateStr);
	         }
	         else
	         {
	        	 dataCell.setCellValue("-");
	         }
	         
	         
	         dataCell = dataRow.createCell(++cellNo);
	         dataCell.setCellStyle(noStyle);
	         if(projectBean.getEndDate()!=null)
	         {
	        	 String dateStr = df.format(projectBean.getEndDate());
	             dataCell.setCellValue(dateStr);
	         }
	         else
	         {
	        	 dataCell.setCellValue("-");
	         }
	         
	         dataCell = dataRow.createCell(++cellNo);
	         dataCell.setCellStyle(noStyle);
	         try
	         {
	        	// dataCell.setCellValue(SynchroGlobal.ProjectStatusNew.getName(projectBean.getStatus()));
	        	// dataCell.setCellValue(projectBean.getStatus());
	        	 dataCell.setCellValue(projectBean.getProjectStage());
	         }
	         catch(Exception e)
	         {
    			dataCell.setCellValue("-");
	         }
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(greenStyle);
		         dataCell.setCellValue("Yes");
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		        
		         if(projectBean.getBriefUploadColor()!=null && projectBean.getBriefUploadColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getBriefUploadColor()!=null && projectBean.getBriefUploadColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
			 else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		        
		                
		         dataCell.setCellValue(projectBean.getBriefUploaded());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         if(projectBean.getBriefLegalApprovalReceivedColor()!=null && projectBean.getBriefLegalApprovalReceivedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getBriefLegalApprovalReceivedColor()!=null && projectBean.getBriefLegalApprovalReceivedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
			  else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		         dataCell.setCellValue(projectBean.getBriefLegalApprovalReceived());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getProposalUploadedColor()!=null && projectBean.getProposalUploadedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getProposalUploadedColor()!=null && projectBean.getProposalUploadedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
			 else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		         
		         dataCell.setCellValue(projectBean.getProposalUploaded());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getProposalLegalApprovalReceivedColor()!=null && projectBean.getProposalLegalApprovalReceivedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getProposalLegalApprovalReceivedColor()!=null && projectBean.getProposalLegalApprovalReceivedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
			  else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		         dataCell.setCellValue(projectBean.getProposalLegalApprovalReceived());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(greenStyle);
		         dataCell.setCellValue("Yes");
		         
		         dataCell = dataRow.createCell(++cellNo);
		        
		         if(projectBean.getReportUploadedColor()!=null && projectBean.getReportUploadedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getReportUploadedColor()!=null && projectBean.getReportUploadedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
			 else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		         
		         dataCell.setCellValue(projectBean.getReportUploaded());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getReportLegalApprovalReceivedColor()!=null && projectBean.getReportLegalApprovalReceivedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getReportLegalApprovalReceivedColor()!=null && projectBean.getReportLegalApprovalReceivedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
		          else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		         dataCell.setCellValue(projectBean.getReportLegalApprovalReceived());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getIrisSummaryUploadedColor()!=null && projectBean.getIrisSummaryUploadedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getIrisSummaryUploadedColor()!=null && projectBean.getIrisSummaryUploadedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
		         else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         } 
		         dataCell.setCellValue(projectBean.getIrisSummaryUploaded());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getIrisLegalApprovalReceivedColor()!=null && projectBean.getIrisLegalApprovalReceivedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getIrisLegalApprovalReceivedColor()!=null && projectBean.getIrisLegalApprovalReceivedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
		         else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         } 
		         dataCell.setCellValue(projectBean.getIrisLegalApprovalReceived());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getTpdSummaryUploadedColor()!=null && projectBean.getTpdSummaryUploadedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getTpdSummaryUploadedColor()!=null && projectBean.getTpdSummaryUploadedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
		         else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         } 
		         dataCell.setCellValue(projectBean.getTpdSummaryUploaded());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getTpdSummaryLegalApprovalReceivedColor()!=null && projectBean.getTpdSummaryLegalApprovalReceivedColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getTpdSummaryLegalApprovalReceivedColor()!=null && projectBean.getTpdSummaryLegalApprovalReceivedColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
		          else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		         dataCell.setCellValue(projectBean.getTpdSummaryLegalApprovalReceived());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         
		         if(projectBean.getReviewDoneColor()!=null && projectBean.getReviewDoneColor().equals("Green"))
		         {
		        	 dataCell.setCellStyle(greenStyle);
		         }
		         else if(projectBean.getReviewDoneColor()!=null && projectBean.getReviewDoneColor().equals("Red"))
		         {
		        	 dataCell.setCellStyle(redStyle);
		         }
			  else
		         {
		        	 dataCell.setCellStyle(noStyle);
		         }
		         
		         dataCell.setCellValue(projectBean.getReviewDone());
		         
		         startRow++;
		         
	         }
	         
	         //	sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+15,0,20));
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
	            
		    	
		    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,20);
	        	
	        	sheet.addMergedRegion(mergedRegion);
	        	
	        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	        
	            
	         	StringBuilder notes = new StringBuilder();
	         	notes.append("Notes:").append("\n");
	         	notes.append("- This report doesn’t give a full view on compliance. The compliance report captures additional information that is not technically linked within Synchro, e.g., quality check of the IRIS summary. ").append("\n");
	         	notes.append("- The actual project status may vary if inputs are incomplete or incorrect.").append("\n");
	         	notes.append("- Entries corresponding to 'Yes' indicate completion of the mentioned group of tasks and 'No' indicate non-completion of the mentioned group of tasks.").append("\n");
	         	notes.append("- Project Status 'On Track' / 'Not On Track' denotes that project's progress is as per the timelines or not as per the timelines defined in the system, respectively.").append("\n");
	         	notes.append("\t").append("* On Track - If project start date has passed and all mandatory inputs, till the proposal stage, are provided then the project will be 'On Track'. Likewise, if the project completion date has passed and all mandatory inputs, till project evaluation stage, are provided then also the project will be 'On Track'. ").append("\n");
	         	notes.append("\t").append("* Not On Track - If project start date has passed and some of the mandatory inputs, till the proposal stage, are not provided then the project will be 'Not On Track'. Similarly, if the project completion date has passed and some of the mandatory inputs, till project evaluation stage, are not provided then the project will be 'Not on Track'. ").append("\n");
	         	notes.append("- Green color of cell indicates that the input for this field was mandatory and has been provided on time.").append("\n");
	         	notes.append("- Red color of cell indicates that the input for this field was mandatory; however, the input hasn't been provided.").append("\n");
	         	notes.append("- Colorless cells indicate that inputting a data or attachment was non-mandatory.").append("\n");
	         	
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

        if(projectResultFilter.getKeyword() == null || projectResultFilter.getKeyword().equals("")) {
            projectResultFilter.setKeyword(keyword);
        }
        projectResultFilter.setStart(start);
        if(plimit!=null && plimit > 0)
        {
        	projectResultFilter.setLimit(plimit);
        }
        else
        {
        	projectResultFilter.setLimit(limit);
        }

//        if(projectResultFilter.getSortField() == null || projectResultFilter.getSortField().equals("")) {
//            projectResultFilter.setSortField("status");
//        }
//
//        if(projectResultFilter.getAscendingOrder() == null) {
//            projectResultFilter.setAscendingOrder(0);
//        }


        projectResultFilter.setFetchOnlyUserSpecificProjects(true);
        projectResultFilter.setFetchRegionalProjects(true);
        
        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
         
            
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal()));
            
           
            
            projectResultFilter.setProjectStatusFields(statuses);
        }
   
        
        // Cancel Projects should be accessible to Admin users only  .
        // Cancelled projects should be accessible only in All projects sub tab  // http://redmine.nvish.com/redmine/issues/459
       /* if(SynchroPermHelper.isSystemAdmin())
        {
        	 projectResultFilter.setFetchCancelProjects(true);
        }
        */
        
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
        
        
        if(projectResultFilter.getProjectStatus()!=null && projectResultFilter.getProjectStatus().size()>0)
        {
        	selectedFilters.add("Project Status - " + SynchroUtils.getProjectStatusName(projectResultFilter.getProjectStatus()));
        }
        
        if(projectResultFilter.getProjectStages()!=null && projectResultFilter.getProjectStages().size()>0)
        {
        	selectedFilters.add("Project Stage - "+ SynchroUtils.getProjectStageName(projectResultFilter.getProjectStages()));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("startDateComplete")) ) 
        {
        	//selectedFilters.add("Project Started Between - ");
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
        	//selectedFilters.add("Project Completed Between - ");
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
        
        if(StringUtils.isNotBlank(projectResultFilter.getProjManager())) 
        {
        	selectedFilters.add("SP&I Contact - " + projectResultFilter.getProjManager());
        }
        
        if(projectResultFilter.getCategoryTypes()!=null && projectResultFilter.getCategoryTypes().size()>0)
        {
        	selectedFilters.add("Category Type - " + SynchroUtils.getCategoryNames(projectResultFilter.getCategoryTypes()));
        }
        
        if(projectResultFilter.getResearchEndMarkets()!=null && projectResultFilter.getResearchEndMarkets().size()>0)
        {
        	selectedFilters.add("Research End-Market(s) - " + SynchroUtils.getEndMarketNames(projectResultFilter.getResearchEndMarkets()));
        }
        
        if(projectResultFilter.getResearchAgencies()!=null && projectResultFilter.getResearchAgencies().size()>0)
        {
        	selectedFilters.add("Agency - "+ SynchroUtils.getAgencyNames(projectResultFilter.getResearchAgencies()));
        }
        if(projectResultFilter.getMethDetails()!=null && projectResultFilter.getMethDetails().size()>0)
        {
        	selectedFilters.add("Methodology - "+ SynchroUtils.getMethodologyNames(projectResultFilter.getMethDetails()));
        }
        if(projectResultFilter.getMethodologyTypes()!=null && projectResultFilter.getMethodologyTypes().size()>0)
        {
        	selectedFilters.add("Methodology Type - "+ SynchroUtils.getMethodologyTypeNames(projectResultFilter.getMethodologyTypes()));
        }
        if(projectResultFilter.getBrands()!=null && projectResultFilter.getBrands().size()>0)
        {
        	selectedFilters.add("Branded/Non-Branded - "+ SynchroUtils.getBrandNames(projectResultFilter.getBrands()));
        }
        if(projectResultFilter.getBudgetLocations()!=null && projectResultFilter.getBudgetLocations().size()>0)
        {
        	selectedFilters.add("Budget Location - "+ SynchroUtils.getBudgetLocationNames(projectResultFilter.getBudgetLocations()));
        }
        if(projectResultFilter.getBudgetYears()!=null && projectResultFilter.getBudgetYears().size()>0)
        {
        	selectedFilters.add("Budget Year - " + StringUtils.join(projectResultFilter.getBudgetYears(), ","));
        }
        if(projectResultFilter.getTotalCostStart()!=null || projectResultFilter.getTotalCostEnd()!=null ) 
        {
        	//selectedFilters.add("Total Cost(GBP) - ");
        	if(projectResultFilter.getTotalCostStart()!=null && projectResultFilter.getTotalCostEnd()!=null)
        	{
        		selectedFilters.add("Total Cost(GBP) -  Between " + projectResultFilter.getTotalCostStart() +" and "+ projectResultFilter.getTotalCostEnd());
        	}
        	else if(projectResultFilter.getTotalCostStart()!=null)
        	{
        		selectedFilters.add("Total Cost(GBP) - " + projectResultFilter.getTotalCostStart()+"");
        	}
        	else
        	{
        		selectedFilters.add("Total Cost(GBP) - " + projectResultFilter.getTotalCostEnd()+"");
        	}
        }
        
        return projectResultFilter;
    }

    private void setPaginationFilter(int page,int results) {
        start = (this.page-1)*limit;
        end = start + limit;
//        end = end >= this.results?this.results:end;
    }


    public List<ProjectDashboardViewBeanNew> toProjectPaginationBeans(final List<Project> projects) {
        List<ProjectDashboardViewBeanNew> beans = new ArrayList<ProjectDashboardViewBeanNew>();
        for(Project project: projects) {
            //https://www.svn.sourcen.com/issues/17926
        	
        	//https://svn.sourcen.com/issues/19574
//        	if(project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())
//        	{
//        		if(project.getBriefCreator()==getUser().getID())
//        		{
//        			beans.add(ProjectDashboardViewBean.toProjectDashboardViewBean(project));
//        		}
//        	}
//        	else
//        	{
        		beans.add(ProjectDashboardViewBeanNew.toProjectDashboardViewBean(project));
//        	}
        }
        return beans;
    }


    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

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



    public List<ProjectDashboardViewBeanNew> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDashboardViewBeanNew> projects) {
        this.projects = projects;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
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
	public String getDownloadReportType() {
		return downloadReportType;
	}


	public void setDownloadReportType(String downloadReportType) {
		this.downloadReportType = downloadReportType;
	}
}
