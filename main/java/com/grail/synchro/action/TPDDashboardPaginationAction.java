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
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.TPDSKUDetails;
import com.grail.synchro.beans.TPDSummary;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.manager.TPDSummaryManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroRawExtractUtil;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.StringUtils;

@Decorate(false)
public class TPDDashboardPaginationAction extends JiveActionSupport{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TPDDashboardPaginationAction.class);
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
    private TPDSummaryManager tpdSummaryManager;
    private ProjectResultFilter projectResultFilter;
    private SynchroUtils synchroUtils;
    
    private Integer plimit;
    
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    
    private ProjectManagerNew synchroProjectManagerNew;
    private ProposalManagerNew proposalManagerNew;
    private ReportSummaryManagerNew reportSummaryManagerNew;
    private String downloadReportType;
    private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }


    public String execute()
    {
 
        setPagination(tpdSummaryManager.getTPDDashboardCount(getSearchFilter()).intValue());
        updatePage();
        return SUCCESS;

    }
    
    
    public String downloadReport()
    {
    	downloadFilename = "TPDSummaryDashboard.xls";
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
        	}
        	else
        	{
        		processReport();
        	}

        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
      //Audit Logs
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
				0, "TPD Submission - Download Report", "", -1L, getUser().getID());
        
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
   
        projects = this.toProjectPaginationBeans(tpdSummaryManager.getTPDProjects(getSearchFilter()), false);
        
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
        downloadFilename = "TPDSubmissionProjectsSummary.xls";
        projectResultFilter = getSearchFilter();
   	 	Calendar calendar = Calendar.getInstance();
        String timeStamp = calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
       
       StringBuilder generatedBy = new StringBuilder();
       generatedBy.append("TPD Submission Projects Report ").append("\n");
       String userName = getUser().getName();
       generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
       generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
       
       
        projectResultFilter.setLimit(null);
 	 	 projectResultFilter.setStart(null);
 	 	 
 	  projects = this.toProjectPaginationBeans(tpdSummaryManager.getTPDProjects(projectResultFilter), false);
       List<Long> projectIds = new ArrayList<Long>();
       
       if(projects!=null && projects.size()>0)
       {
    	   for(ProjectDashboardViewBeanNew bean : projects)
    	   {
    		   projectIds.add(bean.getProjectID());
    	   }
       }
       
        workbook = SynchroRawExtractUtil.generateRawExtract(workbook,generatedBy.toString(),"TPDSubmissionProjects", projectIds);
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
       generatedBy.append("TPD Submission Report ").append("\n");
      
       String userName = getUser().getName();
       generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
      
      

       generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
       
       
   	 	projectResultFilter.setLimit(null);
   	 	projectResultFilter.setStart(null);
   	 
    	projects = this.toProjectPaginationBeans(tpdSummaryManager.getTPDProjects(projectResultFilter), true);
        
        Integer startRow = 4;
        HSSFSheet sheet = workbook.createSheet("TPDSummaryDashboard");
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
        
        CellStyle dashStyle = workbook.createCellStyle();
        
        dashStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        dashStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        dashStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        dashStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        dashStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        
        
        HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(0);
        reportTypeHeaderColumn.setCellStyle(generatedByCellStyle);
        reportTypeHeaderColumn.setCellValue(generatedBy.toString());
        
        int noOfDynamicColums = JiveGlobals.getJiveIntProperty("tpdSummary.dynamic.columns",5);
        sheet.addMergedRegion(new CellRangeAddress(0,3,0,(18+(4*noOfDynamicColums))));
        sheet.addMergedRegion(new CellRangeAddress(4,7,0,(18+(4*noOfDynamicColums))));
        
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
        reportTypeHeaderColumn.setCellValue("Methodology");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Project Stage");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Project Start Date");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Project End Date");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Budget Year");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Research End Market(s)");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Budget Location");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("TPD Status");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Legal Approval Provided By(On TPD Status)");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Date of Legal Approval(On TPD Status)");
        
      /*  reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("TPD Summary Uploaded?");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Date of Legal Approval (TPD Summary)");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Legal Approval Provided By (On TPD Summary)");
        */
        
       for(int k=0;k<noOfDynamicColums;k++)
       {
	        
    	   reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("TPD Summary Uploaded? - "+ (k+1));
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Date of Legal Approval (TPD Summary) - "+ (k+1));
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Legal Approval Provided By (On TPD Summary) - "+ (k+1));
       }
        
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Research Done on");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Product Description");
        
        for(int k=0;k<noOfDynamicColums;k++)
        {
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Submission Date " + (k+1));

	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("TAO Code " + (k+1));
        }
       /* reportTypeHeaderColumn = reportTypeHeader.createCell(14);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Has the Product Launch/Modification Happened Yet");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(15);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Date of Launch/Modification");*/
        
        

        /*
        reportTypeHeaderColumn = reportTypeHeader.createCell(17);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("TAO Code of the modified SKU (To be added only right before the submission)");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(18);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("TPD Summary Provided");
        
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(19);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("TPD Summary Approved By");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(20);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Date of Approval(For TPD Summary)");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(21);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("Submission Date - 1");
        
        reportTypeHeaderColumn = reportTypeHeader.createCell(22);
        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
        reportTypeHeaderColumn.setCellValue("SKY TAO Code - 1 ");*/
        
        
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
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getProjectID());
		        
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         
		         if(StringUtils.isNotBlank(projectBean.getProjectName()))
		         {
		        	 dataCell.setCellValue(projectBean.getProjectName());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        // dataCell.setCellValue(projectBean.getMethodology());
		         
		         if(StringUtils.isNotBlank(projectBean.getMethodology()))
		         {
		        	 dataCell.setCellValue(projectBean.getMethodology());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         //dataCell.setCellValue(projectBean.getProjectStage());
		         
		         if(StringUtils.isNotBlank(projectBean.getProjectStage()))
		         {
		        	 dataCell.setCellValue(projectBean.getProjectStage());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(projectBean.getStartDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getStartDate()));
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(projectBean.getEndDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getEndDate()));
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getBudgetYear());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         //dataCell.setCellValue(projectBean.getEndMarketName());
		         
		         if(StringUtils.isNotBlank(projectBean.getEndMarketName()))
		         {
		        	 dataCell.setCellValue(projectBean.getEndMarketName());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		       //  dataCell.setCellValue(projectBean.getBudgetLocation());
		         if(StringUtils.isNotBlank(projectBean.getBudgetLocation()))
		         {
		        	 dataCell.setCellValue(projectBean.getBudgetLocation());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        // dataCell.setCellValue(projectBean.getStatus());
		         if(StringUtils.isNotBlank(projectBean.getStatus()))
		         {
		        	 dataCell.setCellValue(projectBean.getStatus());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        // dataCell.setCellValue(projectBean.getProposalLegalApproval());
		         if(StringUtils.isNotBlank(projectBean.getProposalLegalApproval()))
		         {
		        	 dataCell.setCellValue(projectBean.getProposalLegalApproval());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(projectBean.getProposalLegalApprovalDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getProposalLegalApprovalDate()));
		         }
		         else
		         {
		        	 dataCell.setCellValue("-");
		        	 dataCell.setCellStyle(dashStyle);
		         }
		         
		         
		      
		         
		         List<ReportSummaryDetails> reportSummaryDetailsList = getReportSummaryManagerNew().getReportSummaryDetails(projectBean.getProjectID());
      	       Map<Integer, Map<Integer, List<Long>>> reportSummaryAttachments = getReportSummaryManagerNew().getReportSummaryAttachmentDetails(projectBean.getProjectID());
      	       List<ReportSummaryInitiation> rsInitiationList = getReportSummaryManagerNew().getReportSummaryInitiation(projectBean.getProjectID());
      	       
      	       
		         int tpdLegalApproverColRef = 0;
      	       for(int i=0;i<reportSummaryDetailsList.size();i++)
      	       {
      	    	   if(reportSummaryDetailsList.get(i).getReportType()==SynchroGlobal.ReportType.TPD_SUMMARY.getId())
      	    	   {
      	    		   tpdLegalApproverColRef = i;
      	    		   break;
      	    	   }
      	       }
      	       
		         for(int k=0;k<noOfDynamicColums;k++)
      	       {
      	    	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(reportSummaryDetailsList!=null && reportSummaryAttachments!=null )
				         {
				        	if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(k+3)!=null) 
				        	{
				        		 dataCell.setCellValue("Yes");
				        	}
				        	else
				        	{
				        		 dataCell.setCellValue("No");
				        	}
				         }
				         else
				         {
				        	 dataCell.setCellValue("-");
				        	 dataCell.setCellStyle(dashStyle);
				         }
	        	       
	        	       
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       
	        	       if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size() > tpdLegalApproverColRef && reportSummaryDetailsList.get(tpdLegalApproverColRef).getLegalApprovalDate()!=null)
	        	       {
	        	    	   
	        	    	   String dateStr = df.format(new Date(reportSummaryDetailsList.get(tpdLegalApproverColRef).getLegalApprovalDate().getTime()));
	        	    	   dataCell.setCellValue(dateStr);
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellValue("-");
	        	    	   dataCell.setCellStyle(dashStyle);
	        	       }
	        	       
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       
	        	       if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size() > tpdLegalApproverColRef && StringUtils.isNotBlank(reportSummaryDetailsList.get(tpdLegalApproverColRef).getLegalApprover()))
	        	       {
	        	    	  
	        	    	   if(reportSummaryDetailsList.get(tpdLegalApproverColRef).getReportType()==SynchroGlobal.ReportType.TPD_SUMMARY.getId())
	        	    	   {
	        	    		   dataCell.setCellValue(reportSummaryDetailsList.get(tpdLegalApproverColRef).getLegalApprover());
	        	    	   }
	        	    	   else
	        	    	   {
	        	    		   dataCell.setCellValue("-");
		        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellValue("-");
	        	    	   dataCell.setCellStyle(dashStyle);
	        	       }
	        	       tpdLegalApproverColRef++;
      	       }
		         
		   
		         
		         
		      /*   dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        
		    	// dataCell.setCellValue(projectBean.getTpdSummaryAttUploaded());
		    	 if(StringUtils.isNotBlank(projectBean.getTpdSummaryAttUploaded()))
		         {
		        	 dataCell.setCellValue(projectBean.getTpdSummaryAttUploaded());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         
		    	 dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        
		    	 if(projectBean.getTpdSummaryLegalApprovalDate()!=null)
		    	 {
		    		 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getTpdSummaryLegalApprovalDate()));
		    	 }
		    	 else
		    	 {
		    		 dataCell.setCellStyle(dashStyle);
		    		 dataCell.setCellValue("-");
		    	 }
		    	 
		    	 dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        
		    	 //dataCell.setCellValue(projectBean.getTpdSummaryLegalApprover());
		    	 if(StringUtils.isNotBlank(projectBean.getTpdSummaryLegalApprover()))
		         {
		        	 dataCell.setCellValue(projectBean.getTpdSummaryLegalApprover());
		         }
		         else
		         {
		        	 dataCell.setCellStyle(dashStyle);
		        	 dataCell.setCellValue("-");
		         }
		         */
		    	 
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		        /* if(projectBean.getTpdResearchDoneOn()!=null && projectBean.getTpdResearchDoneOn().equals("Existing"))
		         {
		        	 dataCell.setCellValue("Yes");
		         }
		         else
		         {
		        	 dataCell.setCellValue("No");
		         }*/
		         if(projectBean.getTpdResearchDoneOn()!=null)
		         {
		        	 dataCell.setCellValue(projectBean.getTpdResearchDoneOn());
		         }
		         else
		         {
		        	 dataCell.setCellValue("-");
		        	 dataCell.setCellStyle(dashStyle);
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         
		         if(projectBean.getTpdProductDescription()!=null)
		         {
		        	 dataCell.setCellValue(projectBean.getTpdProductDescription().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		         }
		         else
		         {
		        	 dataCell.setCellValue("-");
		        	 dataCell.setCellStyle(dashStyle);
		         }
		         
		         List<TPDSKUDetails> tpdSkuDetails = projectBean.getTpdSkuDetails();
		         for(int k=0;k<noOfDynamicColums;k++)
		         {
		        	 
		        	 dataCell = dataRow.createCell(++cellNo);
			         dataCell.setCellStyle(noStyle);
			         if(tpdSkuDetails!=null && tpdSkuDetails.size() > k && tpdSkuDetails.get(k).getSubmissionDate()!=null)
			         {
			        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(tpdSkuDetails.get(k).getSubmissionDate()));
			         }
			         else
			         {
			        	 dataCell.setCellValue("-");
			        	 dataCell.setCellStyle(dashStyle);
			         }
			         
			         dataCell = dataRow.createCell(++cellNo);
			         dataCell.setCellStyle(noStyle);
			         if(tpdSkuDetails!=null && tpdSkuDetails.size() > k && tpdSkuDetails.get(k).getTaoCode()!=null)
			         {
				        
				         dataCell.setCellValue(tpdSkuDetails.get(k).getTaoCode());
			         }
			         else
			         {
			        	 dataCell.setCellValue("-");
			        	 dataCell.setCellStyle(dashStyle);
			         }
		         }    	
		         
		        /* dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(projectBean.getTpdProductionModification());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(projectBean.getTpdProductModificationDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getTpdProductModificationDate()));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         */
		         
		         
		         /*dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellValue("Yes");
		         
		         dataCell = dataRow.createCell(++cellNo);
		         if(projectBean.getTpdSummaryLegalApprover()!=null)
		         {
		        	 dataCell.setCellValue(projectBean.getTpdSummaryLegalApprover());
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         if(projectBean.getTpdSummaryLegalApprovalDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getTpdSummaryLegalApprovalDate()));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         if(projectBean.getSkuSubmissionDate1()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(projectBean.getSkuSubmissionDate1()));
		         }
		         else
		         {
		        	 dataCell.setCellValue("");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellValue(projectBean.getSkuDetails1());
		         */
		         startRow++;
		         
	         }
	         
	         //sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+8,0,(18+(4*noOfDynamicColums))));
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
		    	
	        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+8,0,(18+(4*noOfDynamicColums)));
	        	
	        	sheet.addMergedRegion(mergedRegion);
	        	
	        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            
	            
	            notesStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	            notesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	            
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
        projectResultFilter.setFetchGlobalProjects(true);
        
        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
         
            //statuses.add(new Long(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal()));
            
            projectResultFilter.setProjectStatusFields(statuses);
        }
        
     // Cancel Projects should be accessible to Admin users only
        if(SynchroPermHelper.isSystemAdmin())
        {
        	 projectResultFilter.setFetchCancelProjects(true);
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
        
        if(StringUtils.isNotBlank(getRequest().getParameter("tpdSubmitDateBegin"))) {
            this.projectResultFilter.setTpdSubmitDateBegin(DateUtils.parse(getRequest().getParameter("tpdSubmitDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("tpdSubmitDateComplete"))) {
            this.projectResultFilter.setTpdSubmitDateComplete(DateUtils.parse(getRequest().getParameter("tpdSubmitDateComplete")));
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
        
        if(StringUtils.isNotBlank(getRequest().getParameter("tpdSubmitDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("tpdSubmitDateComplete")) ) 
        {
        	//selectedFilters.add("Project Started Between");
        	if(StringUtils.isNotBlank(getRequest().getParameter("tpdSubmitDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("tpdSubmitDateComplete")))
        		{
        			selectedFilters.add("Last TPD Submitted Between - " + getRequest().getParameter("tpdSubmitDateBegin") + " and "+getRequest().getParameter("tpdSubmitDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Last TPD Submitted Between - " +getRequest().getParameter("tpdSubmitDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Last TPD Submitted Between - " +getRequest().getParameter("tpdSubmitDateComplete"));
        	}
        }
        
        if(projectResultFilter.getProjectStages()!=null && projectResultFilter.getProjectStages().size()>0)
        {
        	selectedFilters.add("Project Stage - "+ SynchroUtils.getProjectStageName(projectResultFilter.getProjectStages()));
        }
        
        if(StringUtils.isNotBlank(projectResultFilter.getProjManager())) 
        {
        	selectedFilters.add("SP&I Contact - "+ projectResultFilter.getProjManager());
        }
        
        if(StringUtils.isNotBlank(projectResultFilter.getProjectInitiator())) 
        {
        	selectedFilters.add("Project Initiator - "+ projectResultFilter.getProjectInitiator());
        }
        
        if(projectResultFilter.getCategoryTypes()!=null && projectResultFilter.getCategoryTypes().size()>0)
        {
        	selectedFilters.add("Category Type - " + SynchroUtils.getCategoryNames(projectResultFilter.getCategoryTypes()));
        }
        
        if(projectResultFilter.getTpdStatus()!=null && projectResultFilter.getTpdStatus().size()>0)
        {
        	selectedFilters.add("TPD Status - " + SynchroUtils.getTPDStatusName(projectResultFilter.getTpdStatus()));
        }
        
        if(projectResultFilter.getResearchEndMarkets()!=null && projectResultFilter.getResearchEndMarkets().size()>0)
        {
        	selectedFilters.add("Research End-Market(s) - " + SynchroUtils.getEndMarketNames(projectResultFilter.getResearchEndMarkets()));
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


    public List<ProjectDashboardViewBeanNew> toProjectPaginationBeans(final List<Project> projects, boolean downloadExcel) {
        List<ProjectDashboardViewBeanNew> beans = new ArrayList<ProjectDashboardViewBeanNew>();
        for(Project project: projects) {
        	
        	ProjectDashboardViewBeanNew tpdBean = 	ProjectDashboardViewBeanNew.toTPDDashboardViewBean(project);
        	List<TPDSummary> tpdSummaryDetails = tpdSummaryManager.getTPDSummaryDetails(project.getProjectID());
        	if(tpdSummaryDetails!=null && tpdSummaryDetails.size()>0)
        	{
        		if(tpdSummaryDetails.get(0).getLastSubmissionDate()!=null)
        		{
        			tpdBean.setTpdLastSubmittedDate(tpdSummaryDetails.get(0).getLastSubmissionDate());
        			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        			String dateStr = df.format(tpdSummaryDetails.get(0).getLastSubmissionDate());
        			tpdBean.setTpdLastSubmittedDateString(dateStr);
        			
        		}
        		if(tpdSummaryDetails.get(0).getPreviouslySubmitted())
        		{
        			tpdBean.setTpdPreviouslySubmitted("Yes");
        		}
        		else
        		{
        			tpdBean.setTpdPreviouslySubmitted("No");
        		}
        		
        		if(tpdSummaryDetails.get(0).getResearchDoneOn()!=null && tpdSummaryDetails.get(0).getResearchDoneOn().intValue()==1 )
        		{
        			tpdBean.setTpdResearchDoneOn("Existing Product in the Market");
        		}
        		else if(tpdSummaryDetails.get(0).getResearchDoneOn()!=null && tpdSummaryDetails.get(0).getResearchDoneOn().intValue()==2 )
        		{
        			tpdBean.setTpdResearchDoneOn("Non-existing Product in the Market");
        		}
        		else if(tpdSummaryDetails.get(0).getResearchDoneOn()!=null && tpdSummaryDetails.get(0).getResearchDoneOn().intValue()==3 )
        		{
        			tpdBean.setTpdResearchDoneOn("Both - Existing & Non-existing Product in the Market");
        		}
        		else
        		{
        			tpdBean.setTpdResearchDoneOn(" ");
        		}
        		
        		if(tpdSummaryDetails.get(0).getProductDescription()!=null )
        		{
        			tpdBean.setTpdProductDescription(tpdSummaryDetails.get(0).getProductDescription());
        		}
        		else
        		{
        			tpdBean.setTpdResearchDoneOn("");
        		}
        		
        	/*	if(tpdSummaryDetails.get(0).getHasProductModification()!=null && tpdSummaryDetails.get(0).getHasProductModification().intValue()==1)
        		{
        			tpdBean.setTpdProductionModification("Yes");
        		}
        		else
        		{
        			tpdBean.setTpdProductionModification("No");
        		}
        		
        		if(tpdSummaryDetails.get(0).getTpdModificationDate()!=null)
        		{
        			tpdBean.setTpdProductModificationDate(tpdSummaryDetails.get(0).getTpdModificationDate());
        		}
        		
        		if(tpdSummaryDetails.get(0).getTaoCode()!=null)
        		{
        			tpdBean.setTpdTAOCode(tpdSummaryDetails.get(0).getTaoCode());
        		}
        		else
        		{
        			tpdBean.setTpdTAOCode("");
        		}
        		
        		List<TPDSKUDetails> tpdSkuDetails = tpdSummaryManager.getTPDSKUDetails(project.getProjectID());
        		if(tpdSkuDetails!=null && tpdSkuDetails.size()>0)
        		{
        			if(tpdSkuDetails.get(0).getSubmissionDate()!=null)
        			{
        				tpdBean.setSkuSubmissionDate1(tpdSkuDetails.get(0).getSubmissionDate());
        			}
        			if(tpdSkuDetails.get(0).getSkuDetails()!=null)
        			{
        				tpdBean.setSkuDetails1(tpdSkuDetails.get(0).getSkuDetails());
        			}
        		}*/
        		
        	}
        	else
        	{
        		tpdBean.setTpdPreviouslySubmitted("No");
        	}
        	
        	Project synchroProject = synchroProjectManagerNew.get(project.getProjectID());
        	tpdBean.setMethodology(SynchroDAOUtil.getMethodologyNames(StringUtils.join(synchroProject.getMethodologyDetails(),",")));
        	try
    		{
        		//tpdBean.setProjectStage(SynchroGlobal.ProjectStatusNew.getName(synchroProject.getStatus()));
        		
        		//http://redmine.nvish.com/redmine/issues/459 - Cancel Status to be displayed on Dashboard
                if(synchroProject.getIsCancel())
                {
                	tpdBean.setProjectStage(SynchroGlobal.ProjectStatusNew.CANCEL.getValue());
                }
                else
                {
                	tpdBean.setProjectStage(SynchroGlobal.ProjectStatusNew.getName(synchroProject.getStatus()));
                }
    		}
    		catch(Exception e)
    		{
    			tpdBean.setProjectStage("");
    		}
        	tpdBean.setEndDate(synchroProject.getEndDate());
        	tpdBean.setBudgetYear(synchroProject.getBudgetYear());
        	tpdBean.setBudgetLocation(SynchroUtils.getBudgetLocationName(synchroProject.getBudgetLocation()));
        	
        	Integer tpdStatus = SynchroPermHelper.getLatestTPDStatus(project.getProjectID());
        	if(tpdStatus.intValue() == -1)
        	{
        		tpdBean.setStatus("Pending");
        	}
        	else
        	{
        		if(tpdStatus.intValue() == 1)
        		{
        			tpdBean.setStatus("May have to be TPD submitted");
        		}
        		else if(tpdStatus.intValue() == 2)
        		{
        			tpdBean.setStatus("Doesn't have to be TPD submitted");
        		}
        	}
        	
        	/*List<ProposalInitiation> proposalInitiationList = this.proposalManagerNew.getProposalInitiationNew(project.getProjectID());
        	if(proposalInitiationList!=null && proposalInitiationList.size()>0)
        	{
        		try
        		{
        			tpdBean.setProposalLegalApproval(userManager.getUser(proposalInitiationList.get(0).getProposalLegalApprover()).getName());
        			tpdBean.setProposalLegalApprovalDate(proposalInitiationList.get(0).getLegalApprovalDate());
        		}
        		catch(UserNotFoundException e)
        		{
        			tpdBean.setProposalLegalApproval("");
        		}
        	}*/
        	
        	tpdBean.setProposalLegalApproval(SynchroPermHelper.getLatestTPDLegalApprover(project.getProjectID()));
			if(SynchroPermHelper.getLatestTPDLegalApprovalDate(project.getProjectID())!=null)
			{
				tpdBean.setProposalLegalApprovalDate(SynchroPermHelper.getLatestTPDLegalApprovalDate(project.getProjectID()));
			}
        	
        	List<ReportSummaryDetails> reportSummaryDetailsList = this.reportSummaryManagerNew.getReportSummaryDetails(project.getProjectID());
        	boolean flag = true;
        	for(ReportSummaryDetails  rsd :  reportSummaryDetailsList)
        	{
        		if(rsd.getReportType()==SynchroGlobal.ReportType.TPD_SUMMARY.getId() && flag)
        		{
        			flag=false;
        			if(rsd.getLegalApprover()!=null)
        			{
        				tpdBean.setTpdSummaryLegalApprover(rsd.getLegalApprover());
        			}
        			if(rsd.getLegalApprovalDate()!=null)
        			{
        				tpdBean.setTpdSummaryLegalApprovalDate(rsd.getLegalApprovalDate());
        			}
        		}
        	}
        	
        	 Map<Integer, Map<Integer, List<Long>>> reportSummaryAttachments = this.reportSummaryManagerNew.getReportSummaryAttachmentDetails(project.getProjectID());
	    	 if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size() > 0)
      	     {
	               if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null)
	               {
	            	   tpdBean.setTpdSummaryAttUploaded("Yes");
	               }
	               else 
	               {
	            	   tpdBean.setTpdSummaryAttUploaded("No");
	               }
      	      }
      	      else
      	      {
      	    	tpdBean.setTpdSummaryAttUploaded("");
      	      }
        	
        	if(downloadExcel)
        	{
        		List<TPDSKUDetails> tpdSkuDetails = tpdSummaryManager.getTPDSKUDetails(project.getProjectID());
            	if(tpdSkuDetails!=null && tpdSkuDetails.size() >0)
            	{
            		
            		tpdBean.setTpdSkuDetails(tpdSkuDetails);
            		beans.add(tpdBean);
            		/*
            		for(TPDSKUDetails skuDetail:tpdSkuDetails)
            		{
            			
            			ProjectDashboardViewBeanNew newTpdBean = ProjectDashboardViewBeanNew.toTPDDashboardViewBean(project);
            			newTpdBean.setTpdLastSubmittedDate(tpdBean.getTpdLastSubmittedDate());
            			newTpdBean.setTpdLastSubmittedDateString(tpdBean.getTpdLastSubmittedDateString());
            			
            			
            			newTpdBean.setTpdPreviouslySubmitted(tpdBean.getTpdPreviouslySubmitted());
            			newTpdBean.setTpdResearchDoneOn(tpdBean.getTpdResearchDoneOn());
            			newTpdBean.setTpdProductDescription(tpdBean.getTpdProductDescription());
            			newTpdBean.setTpdProductionModification(tpdBean.getTpdProductionModification());
            			newTpdBean.setTpdProductModificationDate(tpdBean.getTpdProductModificationDate());
            			newTpdBean.setTpdTAOCode(tpdBean.getTpdTAOCode());
            			//newTpdBean.setSkuSubmissionDate1(tpdBean.getSkuSubmissionDate1());
            			//newTpdBean.setSkuDetails1(tpdBean.getSkuDetails1());
            			newTpdBean.setProjectStage(tpdBean.getProjectStage());
            			newTpdBean.setEndDate(tpdBean.getEndDate());
            			newTpdBean.setBudgetYear(tpdBean.getBudgetYear());
    		        	newTpdBean.setBudgetLocation(tpdBean.getBudgetLocation());
    		        	newTpdBean.setStatus(tpdBean.getStatus());
    		        	newTpdBean.setProposalLegalApproval(tpdBean.getProposalLegalApproval());
    		        	newTpdBean.setProposalLegalApprovalDate(tpdBean.getProposalLegalApprovalDate());
    		        	newTpdBean.setTpdSummaryLegalApprover(tpdBean.getTpdSummaryLegalApprover());
    		        	newTpdBean.setTpdSummaryLegalApprovalDate(tpdBean.getTpdSummaryLegalApprovalDate());
    		        	
    		        	
    		        	
    		        	
    		        	if(skuDetail.getSubmissionDate()!=null)
    	        		{
    		        		newTpdBean.setSkuSubmissionDate1(skuDetail.getSubmissionDate());
    	        		}
    		        	
    		        	if(skuDetail.getHasProductModification()!=null && skuDetail.getHasProductModification().intValue()==1)
    	        		{
    		        		newTpdBean.setTpdProductionModification("Launch");
    	        		}
    		        	else if(skuDetail.getHasProductModification()!=null && skuDetail.getHasProductModification().intValue()==2)
    	        		{
    		        		newTpdBean.setTpdProductionModification("Modification");
    	        		}
    		        	else if(skuDetail.getHasProductModification()!=null && skuDetail.getHasProductModification().intValue()==3)
    	        		{
    		        		newTpdBean.setTpdProductionModification("None");
    	        		}
    	        		else
    	        		{
    	        			newTpdBean.setTpdProductionModification(" ");
    	        		}
    	        		
    	        		if(skuDetail.getTpdModificationDate()!=null)
    	        		{
    	        			newTpdBean.setTpdProductModificationDate(skuDetail.getTpdModificationDate());
    	        		}
    	        		
    	        		if(skuDetail.getTaoCode()!=null)
    	        		{
    	        			newTpdBean.setTpdTAOCode(skuDetail.getTaoCode());
    	        		}
    	        		else
    	        		{
    	        			newTpdBean.setTpdTAOCode("");
    	        		}
    	        		
    		        	
    		        	beans.add(newTpdBean);
            		}*/
            	}
            	else
            	{
            		beans.add(tpdBean);
            	}
        	}
        	else
        	{
        		beans.add(tpdBean);
        	}
        	
        	
        	
        	//beans.add(ProjectDashboardViewBeanNew.toTPDDashboardViewBean(project));

        }
        return beans;
    }


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


	public TPDSummaryManager getTpdSummaryManager() {
		return tpdSummaryManager;
	}


	public void setTpdSummaryManager(TPDSummaryManager tpdSummaryManager) {
		this.tpdSummaryManager = tpdSummaryManager;
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


	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}


	public void setSynchroProjectManagerNew(
			ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}


	public ProposalManagerNew getProposalManagerNew() {
		return proposalManagerNew;
	}


	public void setProposalManagerNew(ProposalManagerNew proposalManagerNew) {
		this.proposalManagerNew = proposalManagerNew;
	}


	public ReportSummaryManagerNew getReportSummaryManagerNew() {
		return reportSummaryManagerNew;
	}


	public void setReportSummaryManagerNew(
			ReportSummaryManagerNew reportSummaryManagerNew) {
		this.reportSummaryManagerNew = reportSummaryManagerNew;
	}
	
		public String getDownloadReportType() {
		return downloadReportType;
	}


	public void setDownloadReportType(String downloadReportType) {
		this.downloadReportType = downloadReportType;
	}

}
