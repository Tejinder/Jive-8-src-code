package com.grail.synchro.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.RegionUtil;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectSpecsManagerNew;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.manager.StageManager;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.StringUtils;


public class SynchroRawExtractUtil {
	
	private static ProjectManagerNew synchroProjectManagerNew;
	private static UserManager userManager;
	private static StageManager stageManager;
	private static SynchroUtils synchroUtils;
	private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private static PIBManagerNew pibManagerNew;
	
	private static ProposalManagerNew proposalManagerNew;
	
	private static ProjectSpecsManagerNew projectSpecsManagerNew;
	
	private static ReportSummaryManagerNew reportSummaryManagerNew;
	
	private static ProjectEvaluationManagerNew projectEvaluationManagerNew;
	 
	 


	public static UserManager getUserManager() {
		if(userManager==null)
		{
			return JiveApplication.getContext().getSpringBean("userManager");
		}
		return userManager;
	}


	public static ProjectManagerNew getSynchroProjectManagerNew() {
		if(synchroProjectManagerNew==null)
		{
			return JiveApplication.getContext().getSpringBean("synchroProjectManagerNew");
		}
		return synchroProjectManagerNew;
	}
	
	public static StageManager getStageManager() {
		if(stageManager == null)
		{
			return JiveApplication.getContext().getSpringBean("stageManager");
		}
		return stageManager;
	}

	public static SynchroUtils getSynchroUtils() {
		if(synchroUtils == null)
		{
			return JiveApplication.getContext().getSpringBean("synchroUtils");
		}
		return synchroUtils;
	}
	
	public static PIBManagerNew getPIBManagerNew() 
	{
		if(pibManagerNew==null)
		{
			return JiveApplication.getContext().getSpringBean("pibManagerNew");
		}
		return pibManagerNew;
	}
	
	public static ProposalManagerNew getProposalManagerNew() 
	{
		if(proposalManagerNew==null)
		{
			return JiveApplication.getContext().getSpringBean("proposalManagerNew");
		}
		return proposalManagerNew;
	}
	
	public static ProjectSpecsManagerNew getProjectSpecsManagerNew() 
	{
		if(projectSpecsManagerNew==null)
		{
			return JiveApplication.getContext().getSpringBean("projectSpecsManagerNew");
		}
		return projectSpecsManagerNew;
	}
	
	public static ReportSummaryManagerNew getReportSummaryManagerNew() 
	{
		if(reportSummaryManagerNew==null)
		{
			return JiveApplication.getContext().getSpringBean("reportSummaryManagerNew");
		}
		return reportSummaryManagerNew;
	}
	
	public static ProjectEvaluationManagerNew getProjectEvaluationManagerNew() 
	{
		if(projectEvaluationManagerNew==null)
		{
			return JiveApplication.getContext().getSpringBean("projectEvaluationManagerNew");
		}
		return projectEvaluationManagerNew;
	}

	public static HSSFWorkbook generateRawExtract(HSSFWorkbook workbook, String generatedBy, String sheetName, List<Long> projectIds)
	{
		  Integer startRow = 4;
	      HSSFSheet sheet = workbook.createSheet(sheetName);
	      HSSFRow reportTypeHeader = sheet.createRow(startRow);
	      
	      HSSFFont sheetHeaderFont = workbook.createFont();
	      sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	      sheetHeaderFont.setFontName("Calibri");
	      
	      
	     
	      
	      
	      HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
	      
	      
	      HSSFFont callibiriFont = workbook.createFont();
          callibiriFont.setFontName("Calibri");
          
          
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
	      
	      int noOfDynamicColumsReportType = JiveGlobals.getJiveIntProperty("reportType.dynamic.columns",5);
	      int noOfDynamicColumsIRISSummary = JiveGlobals.getJiveIntProperty("irisSummary.dynamic.columns",5);
	      int noOfDynamicColumsTPDSummary = JiveGlobals.getJiveIntProperty("tpdSummary.dynamic.columns",5);
	      
	      sheet.addMergedRegion(new CellRangeAddress(0,3,0,(70+(noOfDynamicColumsReportType*3)+(noOfDynamicColumsIRISSummary*2)+(noOfDynamicColumsTPDSummary*3))));
	      sheet.addMergedRegion(new CellRangeAddress(4,7,0,(70+(noOfDynamicColumsReportType*3)+(noOfDynamicColumsIRISSummary*2)+(noOfDynamicColumsTPDSummary*3))));
	      
	      startRow = startRow + 4;
	      
	      reportTypeHeader = sheet.createRow(startRow);
	      
	     
	       
	       
	      int columnIndex = 0;
	      
	       reportTypeHeaderColumn = reportTypeHeader.createCell(columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Budget Year");
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Budget Location");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Code");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Name");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Stage");
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Methodology Group");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Methodology");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Methodology Type");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Category");
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Brand Specific Details");
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Total Cost in GBP");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Research End Market(s)");
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Type");
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("SP&I Contact");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Initiator");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("1 - Research Agency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("1 - Kantar/Non-Kantar");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("1 - Cost Component");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("1 - Currency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("1 - Cost");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("1 - Cost (GBP)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("2 - Research Agency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("2 - Kantar/Non-Kantar");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("2 - Cost Component");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("2 - Currency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("2 - Cost");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("2 - Cost (GBP)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("3 - Research Agency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("3 - Kantar/Non-Kantar");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("3 - Cost Component");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("3 - Currency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("3 - Cost");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("3 - Cost (GBP)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("4 - Research Agency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("4 - Kantar/Non-Kantar");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("4 - Cost Component");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("4 - Currency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("4 - Cost");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("4 - Cost (GBP)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("5 - Research Agency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("5 - Kantar/Non-Kantar");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("5 - Cost Component");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("5 - Currency");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("5 - Cost");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("5 - Cost (GBP)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Start");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Creation Date");
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Completion");
	      
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Methodology Waiver Required?");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Status of Methodology Waiver");
	       
	      
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Agency Waiver Required?");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Status of Agency Waiver");
	       
	      
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("End market Process (EU/Without EU)");
	       
	      
	       
	      
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Reference Synchro Code of the Above Market Project(If Fieldwork Project)");
	       
	     
	       
	       /*reportTypeHeaderColumn = reportTypeHeader.createCell(9);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Reference Synchro Code for EU Projects");
	       */
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Will the outcome be shared with the respective EU market?");
	       
	      
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       //reportTypeHeaderColumn.setCellValue("Would this project need end market funding?");
	       reportTypeHeaderColumn.setCellValue("Is an end market involved in this project?");
	       
	       
	      
	       
	      
	       
	       
	       
	     
	      
	       
	     /*  reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Agency Exceptions");
	       */
	       
	      
	       
	       
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Brief Uploaded?");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Status of Legal Approval (Brief)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Date of Request for Legal Approval (Brief)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Date of last Update/ Approval (Brief)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Legal Approver Name (Brief)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Legal Sign-Off Required(Brief)?");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Proposal Uploaded?");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Status of Legal Approval (Proposal)");

	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Date of Request for Legal Approval (Proposal)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Date of last Update/ Approval (Proposal)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Legal Approver Name (Proposal)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Legal Sign-Off Required(Proposal)?");

	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Documentation Uploaded?");

	      
	       for(int k=0;k<noOfDynamicColumsReportType;k++)
	       {
		        
	    	   reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("Reports Uploaded? - "  + (k+1));

		       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("Type Of Report - " + (k+1));
		       
		       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("Legal Approval Provided By (On Reports) - " + (k+1));
	       }
	       
	      
	       for(int k=0;k<noOfDynamicColumsIRISSummary;k++)
	       {
		        
	    	   reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("IRIS Summary Uploaded? - "+ (k+1));
		       
		       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("Legal Approval Provided By (On IRIS Summary) - "+ (k+1));
	       }
	       
	       for(int k=0;k<noOfDynamicColumsTPDSummary;k++)
	       {
		        
	    	   reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("TPD Summary Uploaded? - "+ (k+1));
		       
		       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("Date of Legal Approval (TPD Summary) - "+ (k+1));
		       
		       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
		       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
		       reportTypeHeaderColumn.setCellValue("Legal Approval Provided By (On TPD Summary) - "+ (k+1));
	       }
	       
	      
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Legal Sign Off Required ? (Reports)");
	       
	       reportTypeHeaderColumn = reportTypeHeader.createCell(++columnIndex);
	       reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	       reportTypeHeaderColumn.setCellValue("Project Evaluation Done");
	       
	       HSSFDataFormat cf = workbook.createDataFormat();
           short currencyDataFormatIndex = cf.getFormat("#,###0");
	       //short currencyDataFormatIndex = cf.getFormat("@");
           
           HSSFCellStyle costFormatStyle = workbook.createCellStyle();
           costFormatStyle.setDataFormat(currencyDataFormatIndex);
           
           costFormatStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
           costFormatStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
           costFormatStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
           costFormatStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
           costFormatStyle.setFont(callibiriFont);
           
           
           
           short currencyDecimalDataFormatIndexString = cf.getFormat("#,###0.00");
         
           
           HSSFCellStyle costDecimalFormatStyle = workbook.createCellStyle();
           costDecimalFormatStyle.setDataFormat(currencyDecimalDataFormatIndexString);
           
           costDecimalFormatStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
           costDecimalFormatStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
           costDecimalFormatStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
           costDecimalFormatStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
 	      
           CellStyle noStyle = workbook.createCellStyle();
           
           noStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
           noStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
           noStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
           noStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
           noStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
           
           noStyle.setFont(callibiriFont);
           
           
           CellStyle dashStyle = workbook.createCellStyle();
           
           dashStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
           dashStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
           dashStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
           dashStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
           dashStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
           dashStyle.setFont(callibiriFont);
           
        
           
	       
	       try
	       {
		       for(Long projectId: projectIds)
		       {
		    	   	 Project project = getSynchroProjectManagerNew().get(projectId); 
		    	     int cellNo = 0;
		        	 HSSFRow dataRow = sheet.createRow(++startRow);
		        	 
		        	 
		        	 HSSFCell dataCell = dataRow.createCell(cellNo);
			         dataCell.setCellStyle(noStyle);
			         if(project.getBudgetYear()!=null)
		        	 {
			        	dataCell.setCellValue(project.getBudgetYear());
		        	 }
			         else if(project.getBudgetYear()!=null && project.getBudgetYear().intValue() == -1)
		        	 {
			        	dataCell.setCellValue("NONE");
		        	 }
			         else
		        	 {
		        		dataCell.setCellStyle(dashStyle);
		        		dataCell.setCellValue("-");
		        	 }
			         
			         
			         dataCell = dataRow.createCell(++cellNo);
			        dataCell.setCellStyle(noStyle);
			        if(project.getBudgetLocation()!=null)
		        	{
			        	//dataCell.setCellValue(SynchroUtils.getBudgetLocationName(project.getBudgetLocation()));
			        	dataCell.setCellValue(SynchroUtils.getAllBudgetLocationName(project.getBudgetLocation()));
		        	}
		        	else
		        	{
		        		dataCell.setCellStyle(dashStyle);
		        		dataCell.setCellValue("-");
		        	}
				        
			         dataCell = dataRow.createCell(++cellNo);
			         dataCell.setCellValue(project.getProjectID());
			         dataCell.setCellStyle(noStyle);
			         
			         dataCell = dataRow.createCell(++cellNo);
			         dataCell.setCellValue(project.getName());
			         dataCell.setCellStyle(noStyle);
			         
			         
			         dataCell = dataRow.createCell(++cellNo);
			         dataCell.setCellStyle(noStyle);
			        if(project.getStatus()!=null)
		        	{
		        		try
		        		{
		        			//dataCell.setCellValue(SynchroGlobal.ProjectStatusNew.getName(project.getStatus()));
		        			//http://redmine.nvish.com/redmine/issues/459 - Cancel Status to be displayed on Dashboard
		        	        if(project.getIsCancel())
		        	        {
		        	        	dataCell.setCellValue(SynchroGlobal.ProjectStatusNew.CANCEL.getValue());
		        	        }
		        	        else
		        	        {
		        	        	dataCell.setCellValue(SynchroGlobal.ProjectStatusNew.getName(project.getStatus()));
		        	        }
		        		}
		        		catch(Exception e)
		        		{
		        			dataCell.setCellStyle(dashStyle);
		        			dataCell.setCellValue("-");
		        		}
		        	}
		        	else 
		        	{
		        		dataCell.setCellStyle(dashStyle);
		        		dataCell.setCellValue("-");
		        		
		            }
			        
			        //Fetch Methodology Group
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   String methodologyGroup="";
	        		   if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size()>0)
	        		   {
	        			   methodologyGroup = SynchroGlobal.getMethodologyGroupName(project.getMethodologyDetails().get(0));
	        			 /*  Map<Integer, String> methGroups = SynchroGlobal.getMethodologyGroups(false,project.getMethodologyDetails().get(0));
	        			   if(methGroups!=null)
	        			   {
		        			   Set<Integer>  methGroupsKeys = methGroups.keySet();
		        			   for(Integer methGroup : methGroupsKeys)
		        			   {
		        				   methodologyGroup=methGroups.get(methGroup);
		        			   }
	        			   }*/
	        			   if(StringUtils.isNotBlank(methodologyGroup))
	        			   {
	        				   dataCell.setCellValue(methodologyGroup);
	        			   }
	        			   else
	        			   {
	        				   dataCell.setCellStyle(dashStyle);
	        				   dataCell.setCellValue("-");
	        			   }
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellStyle(dashStyle);
	        			   dataCell.setCellValue("-");
	        		   }
	        		   
	        		  
	        		   
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(project.getMethodologyDetails()!=null)
	        		   {
	        			   dataCell.setCellValue(SynchroDAOUtil.getMethodologyNames(StringUtils.join(project.getMethodologyDetails(),",")));
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellStyle(dashStyle);
	        			   dataCell.setCellValue("-");
	        		   }
	        		   
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(project.getMethodologyType()!=null)
	        		   {
	        			 if(SynchroGlobal.getProjectIsMapping().get(project.getMethodologyType().intValue())!=null)
	      		         {
	      		        	 dataCell.setCellValue(SynchroGlobal.getProjectIsMapping().get(project.getMethodologyType().intValue()));
	      		         }
	      		         else
	      		         {
	      		        	dataCell.setCellStyle(dashStyle);
	      		        	 dataCell.setCellValue("-");
	      		         }
	      		         
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellStyle(dashStyle);
	        			   dataCell.setCellValue("-");
	        		   }
	        		   
	        		   List<String> categoryNames = new ArrayList<String>();
				         for(Long categId: project.getCategoryType())
				         {
				        	 if(SynchroGlobal.getProductTypes().get(categId.intValue())!=null)
				        	 {
				        		 categoryNames.add(SynchroGlobal.getProductTypes().get(categId.intValue()));
				        	 }
				         }
				         dataCell = dataRow.createCell(++cellNo);
				         dataCell.setCellStyle(noStyle);
				         if(categoryNames!=null && categoryNames.size()>0)
				         {
				        	 dataCell.setCellValue(StringUtils.join(categoryNames, ","));
				         }
				         else
				         {
				        	 dataCell.setCellStyle(dashStyle);
				        	 dataCell.setCellValue("-");
				         }  
			         
			         //Brand Specific Details
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(project.getBrandSpecificStudy()!=null && project.getBrandSpecificStudy().intValue()==2)
		    			{
		    				if(project.getBrandSpecificStudyType()!=null && project.getBrandSpecificStudyType().intValue()==1)
		    				{
		    					dataCell.setCellValue("Multi-Brand Study");
		    				}
		    				else if(project.getBrandSpecificStudyType()!=null && project.getBrandSpecificStudyType().intValue()==2)
		    				{
		    					dataCell.setCellValue("Non-brand related");
		    				}
		    				else
		    				{
		    					dataCell.setCellStyle(dashStyle);
		    					dataCell.setCellValue("-");
		    				}
		    			}
		    			else
		    			{
		    				if(project.getBrand()!=null)
		    				{
		    					if(SynchroGlobal.getBrands().get(project.getBrand().intValue())!=null)
		    					{
		    						dataCell.setCellValue(SynchroGlobal.getBrands().get(project.getBrand().intValue()));
		    					}
		    					else
		    					{
		    						dataCell.setCellStyle(dashStyle);
		    						dataCell.setCellValue("-");
		    					}
		    				}
		    				else
		    				{
		    					dataCell.setCellStyle(dashStyle);
		    					dataCell.setCellValue("-");
		    				}
		    			}
		        		   
	        		   //Total Cost
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   
	        		   
	        		   if(project.getTotalCost()!=null)
	        		   {
	        			   dataCell = SynchroUtils.populateCost(project.getTotalCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
	        			   
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellStyle(dashStyle);
	        			   dataCell.setCellValue("-");
	        		   }
	        		   
	        		   dataCell = dataRow.createCell(++cellNo);
				         dataCell.setCellStyle(noStyle);
				         
				         List<Long> emIds = getSynchroProjectManagerNew().getEndMarketIDs(project.getProjectID());
		        		 if(emIds!=null && emIds.size()>0)
		        		 {
		        			List<String> endMarketNames=new ArrayList<String>();
		        			for(Long endMarketId : emIds )
		        			{
		        				//String endMarketName = SynchroGlobal.getEndMarkets().get(endMarketId.intValue());
		        				String endMarketName = SynchroGlobal.getAllEndMarkets().get(endMarketId.intValue());
		        				if(StringUtils.isNotBlank(endMarketName))
		        				{
		        					endMarketNames.add(endMarketName);
		        				}
		        				else
		        				{
		        					// This is done for Regions
		        					//endMarketName = SynchroGlobal.getRegions().get(endMarketId.intValue());
		        					endMarketName = SynchroGlobal.getAllRegions().get(endMarketId.intValue());
		        					if(StringUtils.isNotBlank(endMarketName))
			        				{
			        					endMarketNames.add(endMarketName);
			        				}
		        				}
		        				
		        			}
		        			dataCell.setCellValue(StringUtils.join(endMarketNames, ","));
		        			
		        		 }
		        		 else
		        		 {
		        			 dataCell.setCellStyle(dashStyle);
		        			 dataCell.setCellValue("-");
		        		 }
		        		 
		        		 dataCell = dataRow.createCell(++cellNo);
		        		   dataCell.setCellStyle(noStyle);
		        		   if(project.getProjectType()!=null && project.getProcessType()!=null)
		        		   {
		        			   if(project.getProcessType().intValue()==5 || project.getProcessType().intValue()==6)
		        			   {
		        				   //dataCell.setCellValue("Above market with EU market");
		        				   dataCell.setCellValue("Above market with EU end market");
		        			   }
		        			   if(project.getProcessType().intValue()==7)
		        			   {
		        				   // Only Global Type selected
		        				   if(project.getOnlyGlobalType()!=null && project.getOnlyGlobalType().intValue()==1)
		        				   {
		        					   if(project.getProjectType()!=null && project.getProjectType().intValue()==1)
		        					   {
		        						  // dataCell.setCellValue("Above market with Global market");
		        						   dataCell.setCellValue("Above market with no end market focus");
		        					   }
		        					   if(project.getProjectType()!=null && project.getProjectType().intValue()==2)
		        					   {
		        						   //dataCell.setCellValue("Above market with Regional market");
		        						   dataCell.setCellValue("Above market with no end market focus");
		        					   }
		        				   }
		        				   else
		        				   {
		        					  // dataCell.setCellValue("Above market without EU market");
		        					   // Latest fix for post migration
		        					   if(project.getGlobalOutcomeEUShare()!=null && project.getGlobalOutcomeEUShare().intValue() > 0 && project.getGlobalOutcomeEUShare().intValue()==2)
		        	        		   {
		        						   dataCell.setCellValue("Above market with EU end market");
		        	        		   }
		        					   else
		        					   {
		        						   dataCell.setCellValue("Above market with no EU end market");
		        					   }
		        				   }
		        			   }
		        			   if(project.getProcessType().intValue()==1 || project.getProcessType().intValue()==2 || project.getProcessType().intValue()==3)
		        			   {
		        				   //dataCell.setCellValue("End-market");
		        				   dataCell.setCellValue("End market");
		        			   }
		        			   if(project.getProcessType().intValue()==4)
		        			   {
		        				   dataCell.setCellValue("Fieldwork");
		        			   }
		        			   
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }
		        		   
		        		   dataCell = dataRow.createCell(++cellNo);
		        		   dataCell.setCellStyle(noStyle);
		        		   if(project.getProjectManagerName()!=null)
		        		   {
		        			   dataCell.setCellValue(project.getProjectManagerName());
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }
		        		   
		        		   dataCell = dataRow.createCell(++cellNo);
		        		   dataCell.setCellStyle(noStyle);
		        		   if(project.getBriefCreator()!=null)
		        		   {
		        			   try
		        			   {
		        				   dataCell.setCellValue(getUserManager().getUser(project.getBriefCreator()).getName());
		        			   }
		        			   catch(Exception e)
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }
		        		   
		        		   List<ProjectCostDetailsBean> projectCostDetailsList =  getSynchroProjectManagerNew().getProjectCostDetails(project.getProjectID());
		        		   //Research Agencies Details
		        		   if(projectCostDetailsList!=null && projectCostDetailsList.size()>0)
		        		   {
		        			   //Agency 1
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getAgencyId()!=null && SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(0).getAgencyId().intValue())!=null)
		        			   {
		        				   dataCell.setCellValue(SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(0).getAgencyId().intValue()));
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getAgencyId()!=null)
		        			   {   
			        			   Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(projectCostDetailsList.get(0).getAgencyId().intValue());
					        	   if(rGroupId!=null && rGroupId>0)
					        	   {
					        		   dataCell.setCellValue(SynchroGlobal.getResearchAgencyGroup().get(rGroupId));
					        	   }
					        	   else
					        	   {
					        		   dataCell.setCellStyle(dashStyle);
					        		   dataCell.setCellValue("-");
					        	   }
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
				        		
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getCostComponent()!=null && projectCostDetailsList.get(0).getCostComponent().intValue()==1)
				    		   {
		        				   dataCell.setCellValue("Coordination");
				    		   }
		        			   else if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getCostComponent()!=null && projectCostDetailsList.get(0).getCostComponent().intValue()==2)
				    		   {
		        				   dataCell.setCellValue("FieldWork");
				    		   }
		        			   else if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getCostComponent()!=null && projectCostDetailsList.get(0).getCostComponent().intValue()==3)
				    		   {
		        				   dataCell.setCellValue("Unclassified");
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getCostCurrency()!=null && SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(0).getCostCurrency())!=null)
				    		   {
		        				   dataCell.setCellValue(SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(0).getCostCurrency()));
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle); 
				    			   dataCell.setCellValue("-");
				    		   }
				    			
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getEstimatedCost()!=null)
				    		   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(0).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    			   dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(0).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
				    			
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getCostCurrency()!=null && (projectCostDetailsList.get(0).getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(0).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				 dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(0).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				        	   }
				        	   else if(projectCostDetailsList.get(0)!=null && projectCostDetailsList.get(0).getCostCurrency()!=null && (projectCostDetailsList.get(0).getCostCurrency()!=JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				        			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(projectCostDetailsList.get(0).getCostCurrency())) * (Double.valueOf(projectCostDetailsList.get(0).getEstimatedCost().doubleValue())));
				        			//dataCell.setCellValue(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				        			 dataCell = SynchroUtils.populateCost(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				        	   }
				        	   else
				        	   {
				        		   dataCell.setCellStyle(dashStyle);
				        		   dataCell.setCellValue("-");
				        	   }
				    		   
				    		   //Agnecy 2
				    		   
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getAgencyId()!=null && SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(1).getAgencyId().intValue())!=null)
		        			   {
		        				   dataCell.setCellValue(SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(1).getAgencyId().intValue()));
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getAgencyId()!=null)
		        			   {   
			        			   Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(projectCostDetailsList.get(1).getAgencyId().intValue());
					        	   if(rGroupId!=null && rGroupId>0)
					        	   {
					        		   dataCell.setCellValue(SynchroGlobal.getResearchAgencyGroup().get(rGroupId));
					        	   }
					        	   else
					        	   {
					        		   dataCell.setCellStyle(dashStyle);
					        		   dataCell.setCellValue("-");
					        	   }
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getCostComponent()!=null && projectCostDetailsList.get(1).getCostComponent().intValue()==1)
				    		   {
		        				   dataCell.setCellValue("Coordination");
				    		   }
		        			   else if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getCostComponent()!=null && projectCostDetailsList.get(1).getCostComponent().intValue()==2)
				    		   {
		        				   dataCell.setCellValue("FieldWork");
				    		   }
		        			   else if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getCostComponent()!=null && projectCostDetailsList.get(1).getCostComponent().intValue()==3)
				    		   {
		        				   dataCell.setCellValue("Unclassified");
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getCostCurrency()!=null && SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(1).getCostCurrency())!=null)
				    		   {
		        				   dataCell.setCellValue(SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(1).getCostCurrency()));
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle); 
				    			   dataCell.setCellValue("-");
				    		   }
				    			
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getEstimatedCost()!=null)
				    		   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(1).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				 dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(1).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle , costDecimalFormatStyle);
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    				dataCell.setCellValue("-");
				    		   }
				    			
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getCostCurrency()!=null && (projectCostDetailsList.get(1).getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(1).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				 dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(1).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				        	   }
				        	   else if(projectCostDetailsList.size()>1 && projectCostDetailsList.get(1)!=null && projectCostDetailsList.get(1).getCostCurrency()!=null && (projectCostDetailsList.get(1).getCostCurrency()!=JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				        			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(projectCostDetailsList.get(1).getCostCurrency())) * (Double.valueOf(projectCostDetailsList.get(1).getEstimatedCost().doubleValue())));
				        			//dataCell.setCellValue(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				        			 dataCell = SynchroUtils.populateCost(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				        	   }
				        	   else
				        	   {
				        		   dataCell.setCellStyle(dashStyle);
				        		   dataCell.setCellValue("-");
				        	   }
				    		   
				    		   	//Agnecy 3
				    		   
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getAgencyId()!=null && SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(2).getAgencyId().intValue())!=null)
		        			   {
		        				   dataCell.setCellValue(SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(2).getAgencyId().intValue()));
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getAgencyId()!=null)
		        			   {   
			        			   Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(projectCostDetailsList.get(2).getAgencyId().intValue());
					        	   if(rGroupId!=null && rGroupId>0)
					        	   {
					        		   dataCell.setCellValue(SynchroGlobal.getResearchAgencyGroup().get(rGroupId));
					        	   }
					        	   else
					        	   {
					        		   dataCell.setCellStyle(dashStyle);
					        		   dataCell.setCellValue("-");
					        	   }
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getCostComponent()!=null && projectCostDetailsList.get(2).getCostComponent().intValue()==1)
				    		   {
		        				   dataCell.setCellValue("Coordination");
				    		   }
		        			   else if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getCostComponent()!=null && projectCostDetailsList.get(2).getCostComponent().intValue()==2)
				    		   {
		        				   dataCell.setCellValue("FieldWork");
				    		   }
		        			   else if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getCostComponent()!=null && projectCostDetailsList.get(2).getCostComponent().intValue()==3)
				    		   {
		        				   dataCell.setCellValue("Unclassified");
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getCostCurrency()!=null && SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(2).getCostCurrency())!=null)
				    		   {
		        				   dataCell.setCellValue(SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(2).getCostCurrency()));
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle); 
				    			   dataCell.setCellValue("-");
				    		   }
				    			
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getEstimatedCost()!=null)
				    		   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(2).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				 dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(2).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
				    			
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getCostCurrency()!=null && (projectCostDetailsList.get(2).getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(1).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(2).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				        	   }
				        	   else if(projectCostDetailsList.size()>2 && projectCostDetailsList.get(2)!=null && projectCostDetailsList.get(2).getCostCurrency()!=null && (projectCostDetailsList.get(2).getCostCurrency()!=JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				        			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(projectCostDetailsList.get(2).getCostCurrency())) * (Double.valueOf(projectCostDetailsList.get(2).getEstimatedCost().doubleValue())));
				        			//dataCell.setCellValue(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				        			dataCell = SynchroUtils.populateCost(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				        	   }
				        	   else
				        	   {
				        		   dataCell.setCellStyle(dashStyle);
				        		   dataCell.setCellValue("-");
				        	   }
				    		   
				    		   //Agency 4
				    		   
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getAgencyId()!=null && SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(3).getAgencyId().intValue())!=null)
		        			   {
		        				   dataCell.setCellValue(SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(3).getAgencyId().intValue()));
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getAgencyId()!=null)
		        			   {   
			        			   Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(projectCostDetailsList.get(3).getAgencyId().intValue());
					        	   if(rGroupId!=null && rGroupId>0)
					        	   {
					        		   dataCell.setCellValue(SynchroGlobal.getResearchAgencyGroup().get(rGroupId));
					        	   }
					        	   else
					        	   {
					        		   dataCell.setCellStyle(dashStyle);
					        		   dataCell.setCellValue("-");
					        	   }
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getCostComponent()!=null && projectCostDetailsList.get(3).getCostComponent().intValue()==1)
				    		   {
		        				   dataCell.setCellValue("Coordination");
				    		   }
		        			   else if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getCostComponent()!=null && projectCostDetailsList.get(3).getCostComponent().intValue()==2)
				    		   {
		        				   dataCell.setCellValue("FieldWork");
				    		   }
		        			   else if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getCostComponent()!=null && projectCostDetailsList.get(3).getCostComponent().intValue()==3)
				    		   {
		        				   dataCell.setCellValue("Unclassified");
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getCostCurrency()!=null && SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(3).getCostCurrency())!=null)
				    		   {
		        				   dataCell.setCellValue(SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(3).getCostCurrency()));
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle); 
				    			   dataCell.setCellValue("-");
				    		   }
				    			
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getEstimatedCost()!=null)
				    		   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(3).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(3).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle , costDecimalFormatStyle);
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
				    			
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getCostCurrency()!=null && (projectCostDetailsList.get(3).getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(3).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(3).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle , costDecimalFormatStyle);
				        	   }
				        	   else if(projectCostDetailsList.size()>3 && projectCostDetailsList.get(3)!=null && projectCostDetailsList.get(3).getCostCurrency()!=null && (projectCostDetailsList.get(3).getCostCurrency()!=JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				        			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(projectCostDetailsList.get(3).getCostCurrency())) * (Double.valueOf(projectCostDetailsList.get(3).getEstimatedCost().doubleValue())));
				        			//dataCell.setCellValue(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				        			dataCell = SynchroUtils.populateCost(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle , costDecimalFormatStyle);
				        	   }
				        	   else
				        	   {
				        		   dataCell.setCellStyle(dashStyle);
				        		   dataCell.setCellValue("-");
				        	   }
				    		   
				    		   	//Agency 5
				    		   
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getAgencyId()!=null && SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(4).getAgencyId().intValue())!=null)
		        			   {
		        				   dataCell.setCellValue(SynchroGlobal.getAllResearchAgency().get(projectCostDetailsList.get(4).getAgencyId().intValue()));
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getAgencyId()!=null)
		        			   {   
			        			   Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(projectCostDetailsList.get(4).getAgencyId().intValue());
					        	   if(rGroupId!=null && rGroupId>0)
					        	   {
					        		   dataCell.setCellValue(SynchroGlobal.getResearchAgencyGroup().get(rGroupId));
					        	   }
					        	   else
					        	   {
					        		   dataCell.setCellStyle(dashStyle);
					        		   dataCell.setCellValue("-");
					        	   }
		        			   }
		        			   else
		        			   {
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getCostComponent()!=null && projectCostDetailsList.get(4).getCostComponent().intValue()==1)
				    		   {
		        				   dataCell.setCellValue("Coordination");
				    		   }
		        			   else if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getCostComponent()!=null && projectCostDetailsList.get(4).getCostComponent().intValue()==2)
				    		   {
		        				   dataCell.setCellValue("FieldWork");
				    		   }
		        			   else if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getCostComponent()!=null && projectCostDetailsList.get(4).getCostComponent().intValue()==3)
				    		   {
		        				   dataCell.setCellValue("Unclassified");
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
		        			   
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
		        			   if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getCostCurrency()!=null && SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(4).getCostCurrency())!=null)
				    		   {
		        				   dataCell.setCellValue(SynchroGlobal.getCurrencies().get(projectCostDetailsList.get(4).getCostCurrency()));
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    				 dataCell.setCellValue("-");
				    		   }
				    			
		        			   dataCell = dataRow.createCell(++cellNo);
		        			   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getEstimatedCost()!=null)
				    		   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(4).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    				dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(4).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				    		   }
				    		   else
				    		   {
				    			   dataCell.setCellStyle(dashStyle);
				    			   dataCell.setCellValue("-");
				    		   }
				    			
				    		   dataCell = dataRow.createCell(++cellNo);
				    		   dataCell.setCellStyle(noStyle);
				    		   if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getCostCurrency()!=null && (projectCostDetailsList.get(4).getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(4).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				    			   dataCell = SynchroUtils.populateCost(projectCostDetailsList.get(4).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle, costDecimalFormatStyle );
				        	   }
				        	   else if(projectCostDetailsList.size()>4 && projectCostDetailsList.get(4)!=null && projectCostDetailsList.get(4).getCostCurrency()!=null && (projectCostDetailsList.get(4).getCostCurrency()!=JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				        			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(projectCostDetailsList.get(4).getCostCurrency())) * (Double.valueOf(projectCostDetailsList.get(4).getEstimatedCost().doubleValue())));
				        			//dataCell.setCellValue(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				        			dataCell = SynchroUtils.populateCost(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN), dataCell,costFormatStyle , costDecimalFormatStyle);
				        	   }
				        	   else
				        	   {
				        		   dataCell.setCellStyle(dashStyle);
				        		   dataCell.setCellValue("-");
				        	   }
		        		   }
		        		   else
		        		   {
		        			   // This is for adding Blank values for Agency Fields
		        			   for(int j=0; j<30; j++)
		        			   {
		        				   dataCell = dataRow.createCell(++cellNo);
		        				   //dataCell.setCellStyle(noStyle);
		        				   dataCell.setCellStyle(dashStyle);
		        				   dataCell.setCellValue("-");
		        			   }
		        		   }
		        		
		        		   dataCell = dataRow.createCell(++cellNo);
		        		   dataCell.setCellStyle(noStyle);
		        		   if(project.getStartDate()!=null)
		        		   {
		        			   String dateStr = df.format(new Date(project.getStartDate().getTime()));
				               dataCell.setCellValue(dateStr);
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }	   
	        		   
					         dataCell = dataRow.createCell(++cellNo);
					         dataCell.setCellStyle(noStyle);
					         if(project.getCreationDate() != null) 
					         {
					                String creationDateStr = df.format(new Date(project.getCreationDate()));
					                dataCell.setCellValue(creationDateStr);
					         }
					         else 
					         {
					        	 dataCell.setCellValue(" ");
					         }
			         
					         dataCell = dataRow.createCell(++cellNo);
			        		   dataCell.setCellStyle(noStyle);
			        		   if(project.getEndDate()!=null)
			        		   {
			        			   String dateStr = df.format(new Date(project.getEndDate().getTime()));
					               dataCell.setCellValue(dateStr);
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
			        		   
			        		   
			        		   dataCell = dataRow.createCell(++cellNo);
			        		   dataCell.setCellStyle(noStyle);
			        		   if(project.getMethWaiverReq()!=null && project.getMethWaiverReq().intValue()==1)
			        		   {
			        			   dataCell.setCellValue("Yes");
			        		   }
			        		   else if(project.getMethWaiverReq()!=null && project.getMethWaiverReq().intValue()==2)
			        		   {
			        			   dataCell.setCellValue("No");
			        		   }
			        		   else if(project.getMethWaiverReq()!=null && project.getMethWaiverReq().intValue()==3)
			        		   {
			        			   dataCell.setCellValue("I don’t know yet");
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
			        		   
			        		   dataCell = dataRow.createCell(++cellNo);
			        		   dataCell.setCellStyle(noStyle);
			        		   if(emIds!=null && emIds.size()>0)
			        		   {
			        			   PIBMethodologyWaiver methWaiver =  getPIBManagerNew().getPIBMethodologyWaiver(project.getProjectID(), emIds.get(0));
			        			   if(methWaiver!=null && methWaiver.getIsApproved()!=null &&  methWaiver.getIsApproved().intValue()==1)
			        			   {
			        				   dataCell.setCellValue("Approved");
			        			   }
			        			   else if(methWaiver!=null && methWaiver.getIsApproved()!=null &&  methWaiver.getIsApproved().intValue()==2)
			        			   {
			        				   dataCell.setCellValue("Rejected");
			        			   }
			        			   else if(methWaiver!=null)
			        			   {
			        				   dataCell.setCellValue("Pending");
			        			   }
			        			   else
			        			   { 
			        				   dataCell.setCellStyle(dashStyle);
			        				   dataCell.setCellValue("-");
			        				   
			        			   }
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
			        		   
			        		  
			        		   
			        		   //Agency Waiver Required
			        		   
			        		   dataCell = dataRow.createCell(++cellNo);
			        		   dataCell.setCellStyle(noStyle);
			        		   if(projectCostDetailsList!=null && projectCostDetailsList.size()>0)
			        		   {
				        		   String researchAgencyGroup = " ";
						           for(ProjectCostDetailsBean bean : projectCostDetailsList)
						           {
					        		   if(bean.getAgencyId()!=null && bean.getAgencyId().intValue() > 0)
							           {
							        		Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(bean.getAgencyId().intValue());
							        		if(rGroupId!=null && rGroupId.intValue()==2)
							        		{
							        			researchAgencyGroup = "Non-Kantar";
							        		}
							           }
						           }
						           if(researchAgencyGroup.equals("Non-Kantar"))
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
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
			        		   
			        		   //Status of Agency Waiver
			        		   dataCell = dataRow.createCell(++cellNo);
			        		   dataCell.setCellStyle(noStyle);
			        		   if(emIds!=null && emIds.size()>0)
			        		   {
			        			   PIBMethodologyWaiver agencyWaiver =  getPIBManagerNew().getPIBKantarMethodologyWaiver(project.getProjectID(), emIds.get(0));
			        			   if(agencyWaiver!=null && agencyWaiver.getIsApproved()!=null &&  agencyWaiver.getIsApproved().intValue()==1)
			        			   {
			        				   dataCell.setCellValue("Approved");
			        			   }
			        			   else if(agencyWaiver!=null && agencyWaiver.getIsApproved()!=null &&  agencyWaiver.getIsApproved().intValue()==2)
			        			   {
			        				   dataCell.setCellValue("Rejected");
			        			   }
			        			   else if(agencyWaiver!=null)
			        			   {
			        				   dataCell.setCellValue("Pending");
			        			   }
			        			   else
			        			   { 
			        				   dataCell.setCellStyle(dashStyle);
			        				   dataCell.setCellValue("-");        				   
			        			   }
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
			        		   
			        		  
			        		   
			        
			        dataCell = dataRow.createCell(++cellNo);
			        dataCell.setCellStyle(noStyle);
			        
			        if(project.getProjectType()!=null && project.getProcessType()!=null)
	        		{
	        			// For Global and Regional Projects, this column should be -   
			        	if(project.getProjectType().intValue()==1 || project.getProjectType().intValue()==2 )
			        	{
			        		 dataCell.setCellStyle(dashStyle);
		        			 dataCell.setCellValue("-");
			        	}
			        	else 
			        	{
			        		if(project.getProcessType().intValue()==1 || project.getProcessType().intValue()==2 || project.getProcessType().intValue()==5 || project.getProcessType().intValue()==6)
 	        			    {
		        				   dataCell.setCellValue("With EU");
		        			}
		        			if(project.getProcessType().intValue()==3 || project.getProcessType().intValue()==7 )
		        			{
		        			   dataCell.setCellValue("Without EU");
		        			}
		        			if(project.getProcessType().intValue()==4)
		        			{
		        			   dataCell.setCellStyle(dashStyle);
			        		   dataCell.setCellValue("-");
		        			}
			        	 }
	        			  
	        			   
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellStyle(dashStyle);
	        			   dataCell.setCellValue("-");
	        		   }
			        
			        
			        
			        
			        
			         
			         dataCell = dataRow.createCell(++cellNo);
			         dataCell.setCellStyle(noStyle);
			         if(project.getRefSynchroCode()!=null && project.getRefSynchroCode().intValue()>0)
			         {
			        	 dataCell.setCellValue(project.getRefSynchroCode()+"");
			         }
			         else
			         {
			        	 dataCell.setCellStyle(dashStyle);
			        	 dataCell.setCellValue("-");
			         }
			         
			         
	        		  //REFERENCE SYNCHRO CODE FOR EU PROJECTS
	        		/*   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   List<EndMarketInvestmentDetail> emDetails = getSynchroProjectManagerNew().getEndMarketDetails(project.getProjectID());
	        		   if(emDetails!=null && emDetails.size()>0)
	        		   {
	        			   List<Long> refSynchroCodes = new ArrayList<Long>();
	        			   for(EndMarketInvestmentDetail emd:emDetails)
	        			   {
	        				   if(emd.getReferenceSynchroCode()!=null && emd.getReferenceSynchroCode().intValue()>0)
	        				   {
	        					   refSynchroCodes.add(emd.getReferenceSynchroCode());
	        				   }
	        			   }
	        			   if(refSynchroCodes!=null && refSynchroCodes.size()>0)
	        			   {
	        				   dataCell.setCellValue(StringUtils.join(refSynchroCodes,","));
	        			   }
	        			   else
	        			   {
	        				   dataCell.setCellValue(" ");
	        			   }
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellValue(" ");
	        		   }
	        		   
	        		  */ 
	        		 
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(project.getGlobalOutcomeEUShare()!=null && project.getGlobalOutcomeEUShare().intValue() > 0)
	        		   {
	        			   if(project.getGlobalOutcomeEUShare().intValue()==1)
	        			   {
	        				   dataCell.setCellValue("Yes");
	        			   }
	        			   if(project.getGlobalOutcomeEUShare().intValue()==2)
	        			   {
	        				   dataCell.setCellValue("No");
	        			   }
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellStyle(dashStyle);
	        			   dataCell.setCellValue("-");
	        		   }
	        		   
	        		  
	        		   
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(project.getEndMarketFunding()!=null && project.getEndMarketFunding().intValue()==1)
	        		   {
	        			   dataCell.setCellValue("Yes");
	        		   }
	        		   else if(project.getEndMarketFunding()!=null && project.getEndMarketFunding().intValue()==2)
	        		   {
	        			   dataCell.setCellValue("No");
	        		   }
	        		   else
	        		   {
	        			   dataCell.setCellValue("-");
	        		   }
	        		   
	        		   
	        		   
	        		  
	        		   
	        		   
	        		  
	        		  
	        		   
	        		   //Agency Exception
	        		/*   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size()>0)
	        		   {
	        			  Integer meth = project.getMethodologyDetails().get(0).intValue();
	        			  if(SynchroGlobal.getMethodologyProperties().get(meth)!=null && SynchroGlobal.getMethodologyProperties().get(meth).isAgencyWaiverException())
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
	        			   dataCell.setCellValue("No");
	        		   }
	        		  */
	        		  
	        		  
	        		   
	        		   // Brief Uploaded Field
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(emIds!=null && emIds.size()>0)
	        	       {
	        	        	//Map<Integer, List<AttachmentBean>> attachmentMap = getPIBManagerNew().getDocumentAttachment(project.getProjectID(), emIds.get(0));
	        			   Map<Integer, List<AttachmentBean>> attachmentMap = getPIBManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
	        	        	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId())!=null)
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
	        	    	   //dataCell.setCellValue("No");
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        		   
	        		   // Status of Brief Legal Approval
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   List<ProjectInitiation> projectInitiationList = getPIBManagerNew().getPIBDetailsNew(project.getProjectID());
	        	       if(projectInitiationList!=null && projectInitiationList.size()>0)
	        	       {
	        	        	if(projectInitiationList.get(0).getLegalApprovalStatus()!=null && projectInitiationList.get(0).getLegalApprovalStatus().intValue() == 1)
	        	        	{
	        	        		dataCell.setCellValue("May have to be TPD Submitted");
	        	        	} 
	        	        	else if(projectInitiationList.get(0).getLegalApprovalStatus()!=null && projectInitiationList.get(0).getLegalApprovalStatus().intValue() == 2)
	        	        	{
	        	        		dataCell.setCellValue("Does not have to be TPD Submitted");
	        	        	}
	        	        	else if(projectInitiationList.get(0).getNeedsDiscussion()!=null && projectInitiationList.get(0).getNeedsDiscussion().intValue()==1  )
	        	        	{
	        	        		dataCell.setCellValue("Needs Discussion");
	        	        	}
	        	        	else if(projectInitiationList.get(0).getSendForApproval()!=null && projectInitiationList.get(0).getSendForApproval().intValue()==1)
	        	        	{
	        	        		dataCell.setCellValue("Pending");
	        	        	}
	        	        	else
	 	        	        {
	        	        		dataCell.setCellStyle(dashStyle);
	        	        		dataCell.setCellValue("-");
	 	        	        }
	        	        	
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //Date of Request for Brief Legal Approval
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(projectInitiationList!=null && projectInitiationList.size()>0)
	        	       {
	        	    	   if(projectInitiationList.get(0).getSendForApprovalDate()!=null)
		        		   {
		        			   String dateStr = df.format(new Date(projectInitiationList.get(0).getSendForApprovalDate().getTime()));
				               dataCell.setCellValue(dateStr);
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       
	        	     //Date of Brief Legal Approval
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(projectInitiationList!=null && projectInitiationList.size()>0)
	        	       {
	        	    	   if(projectInitiationList.get(0).getLegalApprovalDate()!=null)
		        		   {
		        			   String dateStr = df.format(new Date(projectInitiationList.get(0).getLegalApprovalDate().getTime()));
				               dataCell.setCellValue(dateStr);
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	      //Brief Legal Approver Name
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(projectInitiationList!=null && projectInitiationList.size()>0)
	        	       {
	        	    	   if(project.getProcessType()!=null && (project.getProcessType().intValue()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() || project.getProcessType().intValue()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
	        	    	   {
	        	    		   if(StringUtils.isNotBlank(projectInitiationList.get(0).getBriefLegalApproverOffline()))
			        		   {
					              
					               {
					            	   dataCell.setCellValue(projectInitiationList.get(0).getBriefLegalApproverOffline());
					               }
					              
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
	        	    	   }
	        	    	   else
	        	    	   {
		        	    	   if(projectInitiationList.get(0).getBriefLegalApprover()!=null)
			        		   {
					               try
					               {
					            	   dataCell.setCellValue(getUserManager().getUser(projectInitiationList.get(0).getBriefLegalApprover()).getName());
					               }
					               catch(UserNotFoundException e)
					               {
					            	   dataCell.setCellStyle(dashStyle);
					            	   dataCell.setCellValue("-");
					               }
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
	        	    	   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //Legal Sign Off Required (Brief)
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() 
	        	    		   || project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() 
	        	    		   || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId() ||
	        	    		   project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
	        	       {
	        	    	   if(projectInitiationList!=null && projectInitiationList.size()>0)
	        	    	   {
	        	    		   if(projectInitiationList.get(0).getLegalSignOffRequired()!=null && projectInitiationList.get(0).getLegalSignOffRequired().intValue()==1)
	        	    		   {
	        	    			  // dataCell.setCellValue("Yes");
	        	    			   dataCell.setCellValue("No");
	        	    		   }
	        	    		   else
	        	    		   {
	        	    			   //dataCell.setCellValue("No");
	        	    			   dataCell.setCellValue("Yes");
	        	    		   }
	        	    	   }
	        	    	   else
	        	    	   {
	        	    		   dataCell.setCellStyle(dashStyle);
	        	    		   dataCell.setCellValue("-");
	        	    	   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       	
	        	      //Proposal Uploaded
	        	       
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   if(emIds!=null && emIds.size()>0)
	        	       {
	        	        	//Map<Integer, List<AttachmentBean>> attachmentMap = getProposalManagerNew().getDocumentAttachment(project.getProjectID(), emIds.get(0), new Long("-1"));
	        			   Map<Integer, List<AttachmentBean>> attachmentMap = getProposalManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"), new Long("-1"));
	        	        	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
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
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	    	   //dataCell.setCellValue("No");
	        	       }
	        		   
	        		   // Status of Proposal Legal Approval
	        		   dataCell = dataRow.createCell(++cellNo);
	        		   dataCell.setCellStyle(noStyle);
	        		   List<ProposalInitiation> proposalInitiationList = getProposalManagerNew().getProposalInitiationNew(project.getProjectID());
	        	       if(proposalInitiationList!=null && proposalInitiationList.size()>0)
	        	       {
	        	        	if(proposalInitiationList.get(0).getLegalApprovalStatus()!=null && proposalInitiationList.get(0).getLegalApprovalStatus().intValue() == 1)
	        	        	{
	        	        		dataCell.setCellValue("May have to be TPD Submitted");
	        	        	} 
	        	        	else if(proposalInitiationList.get(0).getLegalApprovalStatus()!=null && proposalInitiationList.get(0).getLegalApprovalStatus().intValue() == 2)
	        	        	{
	        	        		dataCell.setCellValue("Does not have to be TPD Submitted");
	        	        	}
	        	        	else if(proposalInitiationList.get(0).getNeedsDiscussion()!=null && proposalInitiationList.get(0).getNeedsDiscussion().intValue()==1  )
	        	        	{
	        	        		dataCell.setCellValue("Needs Discussion");
	        	        	}
	        	        	else if(proposalInitiationList.get(0).getSendForApproval()!=null && proposalInitiationList.get(0).getSendForApproval().intValue()==1)
	        	        	{
	        	        		dataCell.setCellValue("Pending");
	        	        	}
	        	        	else
	 	        	        {
	        	        		dataCell.setCellStyle(dashStyle);
	        	        		dataCell.setCellValue("-");
	 	        	        }
	        	        	
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //Date of Request for Proposal Legal Approval
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       	       
	        	       if(proposalInitiationList!=null && proposalInitiationList.size()>0)
	        	       {
	        	    	   if(proposalInitiationList.get(0).getSendForApprovalDate()!=null)
		        		   {
		        			   String dateStr = df.format(new Date(proposalInitiationList.get(0).getSendForApprovalDate().getTime()));
				               dataCell.setCellValue(dateStr);
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       
	        	     //Date of Proposal Legal Approval
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(proposalInitiationList!=null && proposalInitiationList.size()>0)
	        	       {
	        	    	   if(proposalInitiationList.get(0).getLegalApprovalDate()!=null)
		        		   {
		        			   String dateStr = df.format(new Date(proposalInitiationList.get(0).getLegalApprovalDate().getTime()));
				               dataCell.setCellValue(dateStr);
		        		   }
		        		   else
		        		   {
		        			   dataCell.setCellStyle(dashStyle);
		        			   dataCell.setCellValue("-");
		        		   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	      //Proposal Legal Approver Name
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(proposalInitiationList!=null && proposalInitiationList.size()>0)
	        	       {
	        	    	   if(project.getProcessType()!=null && (project.getProcessType().intValue()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() || project.getProcessType().intValue()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
	        	    	   {
	        	    		   if(StringUtils.isNotBlank(proposalInitiationList.get(0).getProposalLegalApproverOffline()))
			        		   {
					              
					               {
					            	   dataCell.setCellValue(proposalInitiationList.get(0).getProposalLegalApproverOffline());
					               }
					              
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
	        	    	   }
	        	    	   else
	        	    	   {
		        	    	   if(proposalInitiationList.get(0).getProposalLegalApprover()!=null)
			        		   {
					               try
					               {
					            	   dataCell.setCellValue(getUserManager().getUser(proposalInitiationList.get(0).getProposalLegalApprover()).getName());
					               }
					               catch(UserNotFoundException e)
					               {
					            	   dataCell.setCellStyle(dashStyle);
					            	   dataCell.setCellValue("-");
					               }
			        		   }
			        		   else
			        		   {
			        			   dataCell.setCellStyle(dashStyle);
			        			   dataCell.setCellValue("-");
			        		   }
	        	    	   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //Legal Sign Off Required (Proposal)
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() 
	        	    		   || project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() 
	        	    		   || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId() ||
	        	    		   project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
	        	       {
	        	    	   if(proposalInitiationList!=null && proposalInitiationList.size()>0)
	        	    	   {
	        	    		   if(proposalInitiationList.get(0).getLegalSignOffRequired()!=null && proposalInitiationList.get(0).getLegalSignOffRequired().intValue()==1)
	        	    		   {
	        	    			   //dataCell.setCellValue("Yes");
	        	    			   dataCell.setCellValue("No");
	        	    		   }
	        	    		   else
	        	    		   {
	        	    			   //dataCell.setCellValue("No");
	        	    			   dataCell.setCellValue("Yes");
	        	    		   }
	        	    	   }
	        	    	   else
	        	    	   {
	        	    		   dataCell.setCellStyle(dashStyle);
	        	    		   dataCell.setCellValue("-");
	        	    	   }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //Documentation Uploaded
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(emIds!=null && emIds.size()>0)
	        	       {
	        	        	//Map<Integer, List<AttachmentBean>> attachmentMap = getProjectSpecsManagerNew().getDocumentAttachment(project.getProjectID(), emIds.get(0));
	        	    	   Map<Integer, List<AttachmentBean>> attachmentMap = getProjectSpecsManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
	        	        	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId())!=null)
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
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //Reports Uploaded
	        	       
	        	       List<ReportSummaryDetails> reportSummaryDetailsList = getReportSummaryManagerNew().getReportSummaryDetails(project.getProjectID());
	        	       Map<Integer, Map<Integer, List<Long>>> reportSummaryAttachments = getReportSummaryManagerNew().getReportSummaryAttachmentDetails(project.getProjectID());
	        	       
	        	       Map<Integer, Map<Integer, List<Long>>> irisReportSummaryAttachments = getReportSummaryManagerNew().getReportSummaryAttachmentDetails(project.getProjectID(),SynchroGlobal.ReportType.IRIS_SUMMARY.getId());
	        	       
	        	       List<ReportSummaryInitiation> rsInitiationList = getReportSummaryManagerNew().getReportSummaryInitiation(project.getProjectID());
	        	       
	        	       
	        	       
	        	       for(int k=0;k<noOfDynamicColumsReportType;k++)
	        	       {
	        	    	   dataCell = dataRow.createCell(++cellNo);
		        	       dataCell.setCellStyle(noStyle);
		        	       if(reportSummaryDetailsList!=null && reportSummaryAttachments!=null )
					         {
					        	if(reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId()).get(k+1)!=null) 
					        	{
					        		 dataCell.setCellValue("Yes");
					        	}
					        	else if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()).get(k+1)!=null) 
					        	{
					        		 dataCell.setCellValue("Yes");
					        	}
					        	else if(reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId()).get(k+1)!=null) 
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
		        	       if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId()).get(k+1)!=null)
		        	       {
		        	    	   dataCell.setCellValue("Full Report");
		        	       }
		        	       else if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()).get(k+1)!=null) 
		        	       {
		        	    	   dataCell.setCellValue("Top Line Report");
		        	       }
		        	       else if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId()).get(k+1)!=null)
		        	       {
		        	    	   dataCell.setCellValue("Executive Presentation Report");
		        	       }
		        	       else
		        	       {
		        	    	   dataCell.setCellStyle(dashStyle);
		        	    	   dataCell.setCellValue("-");
		        	       }
		        	       
		        	       dataCell = dataRow.createCell(++cellNo);
		        	       dataCell.setCellStyle(noStyle);
		        	       
		        	       if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size() > k && StringUtils.isNotBlank(reportSummaryDetailsList.get(k).getLegalApprover()))
		        	       {
		        	    	   //dataCell.setCellValue(reportSummaryDetailsList.get(k).getLegalApprover());
		        	    	   if(reportSummaryDetailsList.get(k).getReportType()==SynchroGlobal.ReportType.FULL_REPORT.getId() || reportSummaryDetailsList.get(k).getReportType()==SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()
		        	    			   || reportSummaryDetailsList.get(k).getReportType()==SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())
		        	    	   {
		        	    		   dataCell.setCellValue(reportSummaryDetailsList.get(k).getLegalApprover());
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
	        	       }
	        	   
	        	       //Type Of Report
	        	   /*    dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null)
	        	       {
	        	    	   dataCell.setCellValue("Full Report");
	        	       }
	        	       else if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null) 
	        	       {
	        	    	   dataCell.setCellValue("Top Line Report");
	        	       }
	        	       else if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null)
	        	       {
	        	    	   dataCell.setCellValue("Executive Presentation Report");
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       
	        	       String reportTypeLegalApprover="";
	        	       String irisLegalApprover="";
	        	       String tpdSummaryLegalApprover="";
	        	       String tpdSummaryDate="";
	        	       
	        	       for(ReportSummaryDetails  rsd :  reportSummaryDetailsList)
	        	        {
	        	        	if(rsd.getReportType()==SynchroGlobal.ReportType.FULL_REPORT.getId() && StringUtils.isNotBlank(rsd.getLegalApprover()))
	        	    		{
	        	        		if(reportTypeLegalApprover.equals(""))
	        	        		{
	        	        			reportTypeLegalApprover= rsd.getLegalApprover();
	        	        		}
	        	    		}
	        	        	if(rsd.getReportType()==SynchroGlobal.ReportType.TOP_LINE_REPORT.getId() && StringUtils.isNotBlank(rsd.getLegalApprover()))
	        	    		{
	        	        		if(reportTypeLegalApprover.equals(""))
	        	        		{
	        	        			reportTypeLegalApprover= rsd.getLegalApprover();
	        	        		}
	        	    		}
	        	        	if(rsd.getReportType()==SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId() && StringUtils.isNotBlank(rsd.getLegalApprover()))
	        	    		{
	        	        		if(reportTypeLegalApprover.equals(""))
	        	        		{
	        	        			reportTypeLegalApprover= rsd.getLegalApprover();
	        	        		}
	        	    		}
	        	        	if(rsd.getReportType()==SynchroGlobal.ReportType.IRIS_SUMMARY.getId() && StringUtils.isNotBlank(rsd.getLegalApprover()))
	        	    		{
	        	        		
	        	        		if(irisLegalApprover.equals(""))
	        	        		{
	        	        			irisLegalApprover= rsd.getLegalApprover();
	        	        		}
	        	    		}
	        	        	if(rsd.getReportType()==SynchroGlobal.ReportType.TPD_SUMMARY.getId() && StringUtils.isNotBlank(rsd.getLegalApprover()))
	        	    		{
	        	        		if(tpdSummaryLegalApprover.equals(""))
	        	        		{
	        	        			tpdSummaryLegalApprover= rsd.getLegalApprover();
	        	        		}
	        	    		}
	        	        	if(rsd.getReportType()==SynchroGlobal.ReportType.TPD_SUMMARY.getId() && rsd.getLegalApprovalDate()!=null)
	        	    		{
	        	        		if(tpdSummaryDate.equals(""))
	        	        		{
	        	        			
	        	        			String dateStr = df.format(new Date(rsd.getLegalApprovalDate().getTime()));
	        	        			tpdSummaryDate = dateStr;
	        	        		}
	        	    		}
	        	        }
	        	       
	        	       //Report Type Legal Approver
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	      
	        	       
	        	       if(StringUtils.isNotBlank(reportTypeLegalApprover))
	        	       {
	        	    	   dataCell.setCellValue(reportTypeLegalApprover);
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	     //IRIS Summary Uploaded
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       
	        	       if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size() > 0)
	        	       {
		        	       if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null)
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
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	     //IRIS Summary Legal Approver
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	      
	        	       if(StringUtils.isNotBlank(irisLegalApprover))
	        	       {
	        	    	   dataCell.setCellValue(irisLegalApprover);
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       */
	        	       
	        	       int irisLegalApproverColRef = 0;
	        	       for(int i=0;i<reportSummaryDetailsList.size();i++)
	        	       {
	        	    	   if(reportSummaryDetailsList.get(i).getReportType()==SynchroGlobal.ReportType.IRIS_SUMMARY.getId())
	        	    	   {
	        	    		   irisLegalApproverColRef = i;
	        	    		   break;
	        	    	   }
	        	       }
	        	      
	        	       int minIRISReportOrderId = getReportSummaryManagerNew().getMinReportOrderId(project.getProjectID(),SynchroGlobal.ReportType.IRIS_SUMMARY.getId());
	        	       
	        	       for(int k=0;k<noOfDynamicColumsIRISSummary;k++)
	        	       {
	        	    	   dataCell = dataRow.createCell(++cellNo);
		        	       dataCell.setCellStyle(noStyle);
		        	       if(reportSummaryDetailsList!=null && irisReportSummaryAttachments!=null )
					         {
					        	//if(reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(k+3)!=null) 
		        	    	   if(irisReportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null && irisReportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(minIRISReportOrderId)!=null)
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
		        	      
		        	       
		        	       
		        	       if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size() > irisLegalApproverColRef && StringUtils.isNotBlank(reportSummaryDetailsList.get(irisLegalApproverColRef).getLegalApprover()))
		        	       {
		        	    	   if(reportSummaryDetailsList.get(irisLegalApproverColRef).getReportType()==SynchroGlobal.ReportType.IRIS_SUMMARY.getId())
		        	    	   {
		        	    		   dataCell.setCellValue(reportSummaryDetailsList.get(irisLegalApproverColRef).getLegalApprover());
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
		        	       irisLegalApproverColRef++;
		        	       minIRISReportOrderId++;
	        	       }
	        	      
	        	       //TPD Summary Uploaded
	        	     /*  dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size() > 0)
	        	       {
		        	       if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null)
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
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	      //TPD Summary Date Of Legal Approval
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(StringUtils.isNotBlank(tpdSummaryDate))
	        	       {
	        	    	   dataCell.setCellValue(tpdSummaryDate);
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //TPD Summary Legal Approver
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       
	        	       if(StringUtils.isNotBlank(tpdSummaryLegalApprover))
	        	       {
	        	    	   dataCell.setCellValue(tpdSummaryLegalApprover);
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       */
	        	       
	        	       
	        	       int tpdLegalApproverColRef = 0;
	        	       for(int i=0;i<reportSummaryDetailsList.size();i++)
	        	       {
	        	    	   if(reportSummaryDetailsList.get(i).getReportType()==SynchroGlobal.ReportType.TPD_SUMMARY.getId())
	        	    	   {
	        	    		   tpdLegalApproverColRef = i;
	        	    		   break;
	        	    	   }
	        	       }
	        	      
	        	       for(int k=0;k<noOfDynamicColumsTPDSummary;k++)
	        	       {
	        	    	   dataCell = dataRow.createCell(++cellNo);
		        	       dataCell.setCellStyle(noStyle);
		        	       if(reportSummaryDetailsList!=null && reportSummaryAttachments!=null )
					         {
					        	if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(k+1)!=null) 
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
	        	       
	        	       //Reports Legal Sign Off Required
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() 
	        	    		   || project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() 
	        	    		   || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId() ||
	        	    		   project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
	        	       {
		        	       if(rsInitiationList!=null && rsInitiationList.size()>0)
		        	       {
		        	    	   if(rsInitiationList.get(0).getLegalSignOffRequired()!=null && rsInitiationList.get(0).getLegalSignOffRequired().intValue()==1)
		        	    	   {
		        	    		   dataCell.setCellValue("yes");
		        	    	   }
		        	    	   else 
			        	       {
			        	           dataCell.setCellValue("No");
			        	       }
		        	       }
		        	       else 
		        	       {
		        	    	   dataCell.setCellStyle(dashStyle);
		        	    	   dataCell.setCellValue("-");
		        	       }
	        	       }
	        	       else
	        	       {
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
	        	       
	        	       //Project Evaluation Done
	        	       dataCell = dataRow.createCell(++cellNo);
	        	       dataCell.setCellStyle(noStyle);
	        	       List<ProjectEvaluationInitiation> peInitiationList = getProjectEvaluationManagerNew().getProjectEvaluationInitiation(project.getProjectID());
	        	       if(peInitiationList!=null && peInitiationList.size()>0)
	        	       {
	        	    	   if(peInitiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal())
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
	        	    	   dataCell.setCellStyle(dashStyle);
	        	    	   dataCell.setCellValue("-");
	        	       }
		       }
	       }
	       catch(Exception e)
	       {
	    	   e.printStackTrace();
	       }
	       
	     //  sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,(71+(noOfDynamicColumsReportType*3)+(noOfDynamicColumsIRISSummary*2)+(noOfDynamicColumsTPDSummary*3))));
	       
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
	    	
	    	
	    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+11,0,(70+(noOfDynamicColumsReportType*3)+(noOfDynamicColumsIRISSummary*2)+(noOfDynamicColumsTPDSummary*3)));
        	
        	sheet.addMergedRegion(mergedRegion);
        	
        	
        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
            
           
            StringBuilder notes = new StringBuilder();
        	notes.append("Notes:").append("\n");
        	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
        	notes.append("- Entry of 'Yes' in a field indicates that the project has reached to that stage and also that the user has made the input. Likewise, entry of 'No' in a field indicates that the project has reached to that stage but the user has not made the input. ").append("\n");
        	notes.append("- Value of '–' in a field indicates that the project has not reached to that stage.").append("\n");
        	notes.append("- Cancelled projects are not included in the above report.").append("\n");
        	
        	HSSFCell notesColumn = notesRow.createCell(0);
        	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
        	notesColumn.setCellValue(notes.toString());
        	notesColumn.setCellStyle(notesStyle);
        	
        	workbook = SynchroUtils.createExcelImage(workbook, sheet);
            return workbook;
	}
	
	
}
