package com.grail.synchro.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.AgencyEvaluationReport;
import com.grail.synchro.beans.DataExtractReport;
import com.grail.synchro.beans.DataExtractReportFilters;
import com.grail.synchro.beans.ExchangeRateReport;
import com.grail.synchro.beans.ExchangeRateReportList;
import com.grail.synchro.beans.PIBBrand;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStimulus;
import com.grail.synchro.beans.PIBTargetSegment;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ResearchCycleReport;
import com.grail.synchro.beans.ResearchCycleReportFilters;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.StageManager;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class SynchroReportUtil {
	
	private static ProjectManager synchroProjectManager;
	private static UserManager userManager;
	private static StageManager stageManager;
	private static SynchroUtils synchroUtils;
	private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");


	public static UserManager getUserManager() {
		if(userManager==null)
		{
			return JiveApplication.getContext().getSpringBean("userManager");
		}
		return userManager;
	}


	public static ProjectManager getSynchroProjectManager() {
		if(synchroProjectManager==null)
		{
			return JiveApplication.getContext().getSpringBean("synchroProjectManager");
		}
		return synchroProjectManager;
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

	public static HSSFWorkbook generateResearchCycleReport(ResearchCycleReportFilters researchCycleReportFilters, List<ResearchCycleReport> researchCycleReportList, int startMonth, int startYear, int endMonth, int endYear)
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
    	HSSFSheet sheet = workbook.createSheet("Research Cycle Plan Report");
    	int row_idx = 4;
    	
    	//sheet.setDisplayGridlines(false);
    	HSSFCellStyle headerStyle = SynchroSheetStylingUtil.getOrientedHeadingStyle(workbook, "1111");
    	HSSFCellStyle cellStyle = SynchroSheetStylingUtil.getCellStyle(workbook, "1111");
    	Row row = sheet.createRow(row_idx);
    	 Cell cell = row.createCell(1);
    	 cell.setCellValue("Project ID");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(2);
    	 cell.setCellValue("Project Name");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(3);
    	 cell.setCellValue("Project Owner");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(4);
    	 cell.setCellValue("Status");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(5);
    	 cell.setCellValue("Start Date");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(6);
    	 cell.setCellValue("End Date");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(7);
    	 cell.setCellValue("End Market");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(8);
    	 cell.setCellValue("Fieldwork Start Date");
    	 cell.setCellStyle(headerStyle);
    	 cell = row.createCell(9);
    	 cell.setCellValue("Report Completion Date");
    	 cell.setCellStyle(headerStyle);
    	 row_idx = row_idx + 1;
    	for(ResearchCycleReport researchCycleReport : researchCycleReportList)
    	{/*
    		//Start
    		if(validateDateFilters(researchCycleReport.getStartMonth(), researchCycleReport.getStartYear(), researchCycleReport.getEndMonth(), researchCycleReport.getEndYear(), startMonth, startYear, endMonth, endYear) && canAccessProjectReport(researchCycleReport.getProjectID()))
    		{
    		row = sheet.createRow(row_idx++);
    		//Project End-Market Details
    		List<EndMarketDetail> endMarketDetails = getSynchroProjectManager().getEndMarketDetails(researchCycleReport.getProjectID());
    		boolean firstrow = true;
    		for(EndMarketDetail endMarketDetail : endMarketDetails)
    		{
    			if(validateEndMarketFilters(researchCycleReportFilters.getEndMarkets(),endMarketDetail.getEndMarketID()))
		    	{
    				if(!firstrow)
    				{
    					row= sheet.createRow(row_idx++);
    					cell = row.createCell(1);
        	    		cell.setCellValue("");
        	    		cell.setCellStyle(cellStyle);
        	    		cell = row.createCell(2);
        	    		cell.setCellValue("");
        	    		cell.setCellStyle(cellStyle);
        	    		cell = row.createCell(3);
        	    		cell.setCellValue("");
        				cell.setCellStyle(cellStyle);
        	    		cell = row.createCell(4);
        	    		cell.setCellValue("");
        	    		cell.setCellStyle(cellStyle);
        	    		cell = row.createCell(5);
        	    		cell.setCellValue("");
        	    		cell.setCellStyle(cellStyle);
        	    		cell = row.createCell(6);
        	    		cell.setCellValue("");
        	    		cell.setCellStyle(cellStyle);
    				}
    				else
    				{
    					cell = row.createCell(1);
    		    		cell.setCellValue(researchCycleReport.getProjectID());
    		    		cell.setCellStyle(cellStyle);
    		    		cell = row.createCell(2);
    		    		cell.setCellValue(researchCycleReport.getName());
    		    		cell.setCellStyle(cellStyle);
    		    		cell = row.createCell(3);
    		    			if(researchCycleReport.getOwnerID() > 0)
    		    			{
    		    				try{
    		    				cell.setCellValue(getUserManager().getUser(researchCycleReport.getOwnerID()).getName());
    		    				cell.setCellStyle(cellStyle);
    		    				}catch(UserNotFoundException e){cell.setCellValue("Anonmyous");}
    		    			}
    		    			else
    		    			{
    		    				cell.setCellValue("Anonmyous");
    		    				cell.setCellStyle(cellStyle);
    		    			}

    		    		cell = row.createCell(4);
    		    		cell.setCellValue(SynchroGlobal.getProjectStatusNames().get(SynchroGlobal.Status.getName(researchCycleReport.getStatus())));
    		    		cell.setCellStyle(cellStyle);
    		    		cell = row.createCell(5);
    		    		cell.setCellValue((researchCycleReport.getStartMonth()+1)+"/"+researchCycleReport.getStartYear());
    		    		cell.setCellStyle(cellStyle);
    		    		cell = row.createCell(6);
    		    		cell.setCellValue((researchCycleReport.getEndMonth()+1)+"/"+researchCycleReport.getEndYear());
    		    		cell.setCellStyle(cellStyle);
    		    		firstrow = false;
    				}
    			
    				cell = row.createCell(7);
		    		cell.setCellValue(endMarketDetail.getName());
		    		cell.setCellStyle(cellStyle);
		    		cell = row.createCell(8);
		    		if(endMarketDetail.getStartYear()>0)
		    		{
		    			cell.setCellValue((endMarketDetail.getStartMonth()+1)+"/"+endMarketDetail.getStartYear());
		    		}
		    		else
		    		{
		    			cell.setCellValue("");
		    		}
		    		cell.setCellStyle(cellStyle);		    		
		    		cell = row.createCell(9);
		    		if(endMarketDetail.getEndYear()>0)
		    		{
		    			cell.setCellValue((endMarketDetail.getEndMonth()+1)+"/"+endMarketDetail.getEndYear());
		    		}
		    		else
		    		{
		    			cell.setCellValue("");
		    		}
		    		cell.setCellStyle(cellStyle);
		    	}
    		}
    	}
	*/}
    	

    	/**
    	 * Autosize columns after data has been entered
    	 */
    	sheet.setColumnWidth(0, 1000);
    	for(int i=1; i<=9 ; i++)
    	{
    		sheet.autoSizeColumn(i);
    		int width = sheet.getColumnWidth(i);
    		
    		if(width > 10000)
    		{
    			sheet.setColumnWidth(i, 10000);
    		}
    	}
    	return workbook;
		
	}
	
	/**
	 * This method will generate the excel for Project Data Extract and Financial Data Extract Reports
	 * @param dataExtractFilters
	 * @param dataExtractList
	 * @return
	 */
	public static HSSFWorkbook generateDataExtractReport(DataExtractReportFilters dataExtractFilters,List<DataExtractReport> dataExtractList)
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
		/*
		
    	HSSFSheet sheet = workbook.createSheet("Project Finance Extract");
    	//sheet.setDisplayGridlines(false);
    	HSSFCellStyle headerStyle = SynchroSheetStylingUtil.getOrientedHeadingStyle(workbook, "1111");
    	HSSFCellStyle cellStyle = SynchroSheetStylingUtil.getCellStyle(workbook, "1111");
    	int row_idx = 1;
    	int cellNum = 0;
    	Row row = sheet.createRow(row_idx);
    	if(dataExtractFilters.getProjectDetailFields()!=null)
    	{
	    	if(dataExtractFilters.getProjectDetailFields().contains("projectID"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project ID");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("name"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project Name");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("ownerID"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project Owner");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("startYear"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project Start Year");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("startMonth"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project Start Month");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("endYear"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project End Year");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("endMonth"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project End Month");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("projectType"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project Type");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("brand"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Brand");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("methodologyGroup"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Methodology Group");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("methodology"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Methodology");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("researchType"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Research Type");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("insights"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Insights Priorities Project");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("npi"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("NPI Reference Number");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("fwEnabled"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project Has Fieldwork");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getProjectDetailFields().contains("partialMethodologyWaiverRequired"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Partial Methodology Waiver Required");
	        	cell.setCellStyle(headerStyle);
	    	}
    	}
    	int endMarketCellNo = new Integer(cellNum);
    	if(dataExtractFilters.getEndMarketDetailFields()!=null)
    	{
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("startYear"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Fieldwork Start Year");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("startMonth"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Fieldwork Start Month");
	        	cell.setCellStyle(headerStyle);
	    	}if(dataExtractFilters.getEndMarketDetailFields().contains("endYear"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Report Completion Year");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("endMonth"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Report Completion Month");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("interviews"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Number of Interviews");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("focusGroups"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Number of Focus Groups");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("waves"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Number of Waves");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("cells"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Number of Cells");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("dataCollectionMethod"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Data Collection Method");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("partialMethodologyWaiverRequired"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Partial Methodology Waiver Required");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("methodologyRationale"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Methodology Rationale");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("oracleApprover"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Oracle Approver");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("approved"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Approved");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getEndMarketDetailFields().contains("oracleApproverComment"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Oracle Approver Comments");
	        	cell.setCellStyle(headerStyle);
	    	}
	   	}
    	int coordinationDetailsOriginalCellNo = new Integer(cellNum);
    	if(dataExtractFilters.getCoordinatingDetailFields()!=null)
    	{
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("supplier"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Supplier");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("name"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Supplier Group");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("createDate"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Coordination Created Date");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("lastUpdated"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Coordination Last Updated Date");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwSupplier"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Fieldwork Supplier");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwSupplierGroup"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Fieldwork Supplier Group");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwEndMarket"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Fieldwork End-Market");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("tenderingAgencyID"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Tendering Agency");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("bidValue"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Bid Value");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("marketType"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Market Type");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("collectionMethod"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Data Collection Method");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwCancelled"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Fieldwork Cancelled");
	        	cell.setCellStyle(headerStyle);
	    	}
    	}
    	int financialDetailsOriginalCellNo = new Integer(cellNum);
    	if(dataExtractFilters.getFinancialDetailFields()!=null)
    	{
	    	if(dataExtractFilters.getFinancialDetailFields().contains("budgetApprover"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Budget Approver");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getFinancialDetailFields().contains("holderLocation"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Budget Holder Location");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getFinancialDetailFields().contains("holderFunction"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Budget Holder Function");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getFinancialDetailFields().contains("budgetYear"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Budget Year");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getFinancialDetailFields().contains("prePlanDetails"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Total pre-plan cost");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getFinancialDetailFields().contains("budgetDetails"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Total Budget");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	if(dataExtractFilters.getFinancialDetailFields().contains("forecastDetails"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Total QPR 1 Forecast");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	        	
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Total QPR 2 Forecast");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	        	
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Total QPR 3 Forecast");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	        	
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Total QPR 4 Forecast");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	        	
	    	}
	    	if(dataExtractFilters.getFinancialDetailFields().contains("actuals"))
	    	{
	    		Cell cell = row.createCell(++cellNum);
	        	cell.setCellValue("Project Management");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	        	
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("FieldWork 1");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	        	
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("FieldWork 2");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	        	
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("FieldWork 3");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Currency");
	        	cell.setCellStyle(headerStyle);
	        	cell = row.createCell(++cellNum);
	        	cell.setCellValue("Exchange Rate");
	        	cell.setCellStyle(headerStyle);
	    	}
	    	
    	}
    	for(int i=0;i<dataExtractList.size();i++)
    	{
    		//This check is for Project Start Date and Project End Date filters.
    		if(validateDateFilters(dataExtractList.get(i).getStartMonth(), dataExtractList.get(i).getStartYear(), dataExtractList.get(i).getEndMonth(), dataExtractList.get(i).getEndYear(), dataExtractFilters.getStartMonth(), dataExtractFilters.getStartYear(), dataExtractFilters.getEndMonth(), dataExtractFilters.getEndYear()))
    		{
	    		row = sheet.createRow(++row_idx);
	    		cellNum = 0;
	    		if(dataExtractFilters.getProjectDetailFields()!=null)
	    		{
	    			if(dataExtractFilters.getProjectDetailFields().contains("projectID"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	        	cell.setCellValue(dataExtractList.get(i).getProjectID());
	    	        	cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("name"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		cell.setCellValue(dataExtractList.get(i).getName());
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("ownerID"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	        	cell.setCellValue(dataExtractList.get(i).getOwnerID());
	    	        	cell.setCellStyle(cellStyle);
	    	        	if(dataExtractList.get(i).getOwnerID() > 0)
	        			{
	        				try{
	        				cell.setCellValue(getUserManager().getUser(dataExtractList.get(i).getOwnerID()).getName());
	        				cell.setCellStyle(cellStyle);
	        				}catch(UserNotFoundException e){cell.setCellValue("Anonmyous");}
	        			}
	        			else
	        			{
	        				cell.setCellValue("Anonmyous");
	        				cell.setCellStyle(cellStyle);
	        			}
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("startYear"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	        	cell.setCellValue(dataExtractList.get(i).getStartYear());
	    	        	cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("startMonth"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	        	cell.setCellValue(dataExtractList.get(i).getStartMonth()+1);
	    	        	cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("endYear"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	        	cell.setCellValue(dataExtractList.get(i).getEndYear());
	    	        	cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("endMonth"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	        	cell.setCellValue(dataExtractList.get(i).getEndMonth()+1);
	    	        	cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("projectType"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	        	if(dataExtractList.get(i).getProjectType()!=null)
	    	        	{
	    	        		cell.setCellValue(SynchroGlobal.getProjectTypes().get(dataExtractList.get(i).getProjectType().intValue()));
	    	        	}
	    	        	cell.setCellStyle(cellStyle);
	    	        	
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("brand"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		if(dataExtractList.get(i).getBrand()!=null)
	    	    		{
	    	    			cell.setCellValue(SynchroGlobal.getBrands(false,dataExtractList.get(i).getBrand()).get(dataExtractList.get(i).getBrand().intValue()));
	    	    		}
	    	    		cell.setCellStyle(cellStyle);
	    	    		
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("methodologyGroup"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		if(dataExtractList.get(i).getMethodologyGroup()!=null)
	    	    		{
	    	    			cell.setCellValue(SynchroGlobal.getMethodologyGroups(true, dataExtractList.get(i).getMethodology()).get(dataExtractList.get(i).getMethodologyGroup().intValue()));
	    	    		}
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("methodology"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		if(dataExtractList.get(i).getMethodology()!=null)
	    	    		{
	    	    			cell.setCellValue(SynchroGlobal.getMethodologies().get(dataExtractList.get(i).getMethodology().intValue()));
	    	    		}
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("researchType"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		cell.setCellValue(dataExtractList.get(i).getResearchType());
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("insights"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		if(dataExtractList.get(i).getInsights()!=null)
	    	    		{
	    	    			cell.setCellValue(dataExtractList.get(i).getInsights()?"yes":"no");
	    	    		}
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("npi"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		cell.setCellValue(dataExtractList.get(i).getNpi());
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("fwEnabled"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		if(dataExtractList.get(i).getFwEnabled()!=null)
	    	    		{
	    	    			cell.setCellValue(dataExtractList.get(i).getFwEnabled()?"yes":"no");
	    	    		}
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    	    	if(dataExtractFilters.getProjectDetailFields().contains("partialMethodologyWaiverRequired"))
	    	    	{
	    	    		Cell cell = row.createCell(++cellNum);
	    	    		if(dataExtractList.get(i).getPartialMethodologyWaiverRequired()!=null)
	    	    		{
	    	    			cell.setCellValue(dataExtractList.get(i).getPartialMethodologyWaiverRequired()?"yes":"no");
	    	    		}
	    	    		cell.setCellStyle(cellStyle);
	    	    	}
	    		}
	    		int coordinationDetailRowNo = new Integer(row_idx);
	    		int financialDetailRowNo = new Integer(row_idx);
	    		//int endMarketCellNo = new Integer(cellNum);
	    		if(dataExtractFilters.getEndMarketDetailFields()!=null)
	    		{
	    			List<EndMarketDetail> endMarketDetails = getSynchroProjectManager().getEndMarketDetails(dataExtractList.get(i).getProjectID());
	    			for(int j=0;j<endMarketDetails.size();j++)
	    			{
	    				if(j>0)
	    				{
	    					row = sheet.createRow(++row_idx);
	    				}
	    				endMarketCellNo = new Integer(cellNum);
	    				if(dataExtractFilters.getEndMarketDetailFields().contains("startYear"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getStartYear());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("startMonth"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getStartMonth()+1);
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("endYear"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getEndYear());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("endMonth"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getEndMonth()+1);
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("interviews"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getInterviews());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("focusGroups"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getFocusGroups());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("waves"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getWaves());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("cells"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getCells());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("dataCollectionMethod"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	
	    		    		if(endMarketDetails.get(j).getDataCollectionMethod()!=null && endMarketDetails.get(j).getDataCollectionMethod().size()>0)
	    		    		{
	    		    			StringBuilder dataCollection = new StringBuilder();
	    		    			int count = 1;
	    		    			for(Long dc:endMarketDetails.get(j).getDataCollectionMethod())
	    		    			{
	    		    				dataCollection.append(SynchroGlobal.getDataCollections().get(dc.intValue()));
	    		    				if(count<endMarketDetails.get(j).getDataCollectionMethod().size())
	    		    				{
	    		    					dataCollection.append(",");
	    		    				}
	    		    				count++;
	    		    			}
	    		    			
	    		    			//cell.setCellValue(endMarketDetails.get(j).getDataCollectionMethod().toString());
	    		    			cell.setCellValue(dataCollection.toString());
	    		    			cell.setCellStyle(cellStyle);
	    		    		}
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("partialMethodologyWaiverRequired"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	if(endMarketDetails.get(j).getPartialMethodologyWaiverRequired()!=null)
	    		        	{
	    		        		cell.setCellValue(endMarketDetails.get(j).getPartialMethodologyWaiverRequired()?"yes":"no");
	    		        	}
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("methodologyRationale"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getMethodologyRationale());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("oracleApprover"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	//cell.setCellValue(endMarketDetails.get(j).getOracleApprover());
	    		        	if(endMarketDetails.get(j).getOracleApprover() > 0)
	    	    			{
	    	    				try{
	    	    				cell.setCellValue(getUserManager().getUser(endMarketDetails.get(j).getOracleApprover()).getName());
	    	    				}catch(UserNotFoundException e){cell.setCellValue("Anonmyous");}
	    	    			}
	    	    			else
	    	    			{
	    	    				cell.setCellValue("Anonmyous");
	    	    			}
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("approved"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	if(endMarketDetails.get(j).getApproved()!=null)
	    		        	{
	    		        		cell.setCellValue(endMarketDetails.get(j).getApproved()?"yes":"no");
	    		        	}
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getEndMarketDetailFields().contains("oracleApproverComment"))
	    		    	{
	    		    		Cell cell = row.createCell(++endMarketCellNo);
	    		        	cell.setCellValue(endMarketDetails.get(j).getOracleApproverComment());
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    			}
	    		}
	    		if(dataExtractFilters.getCoordinatingDetailFields()!=null)
	    		{
	    			List<CoordinationDetail> coordinationDetails = getSynchroProjectManager().getCoordinationDetails(dataExtractList.get(i).getProjectID());
	    			
	    			for(int j=0;j<coordinationDetails.size();j++)
	    			{
	    				if(j>0 && coordinationDetailRowNo>row_idx)
	    				{
	    					row = sheet.createRow(++row_idx);
	    				}
	    				else
	    				{
	    					row = sheet.getRow(coordinationDetailRowNo);
	    				}
	    				int coordinationDetailsCellNo = new Integer(coordinationDetailsOriginalCellNo);
	    			//	int coordinationDetailsCellNo = new Integer(endMarketCellNo);
	    				if(dataExtractFilters.getCoordinatingDetailFields().contains("supplier"))
	    		    	{
	    		    		Cell cell = row.createCell(++coordinationDetailsCellNo);
	    		        	cell.setCellValue(SynchroGlobal.getSuppliers().get(coordinationDetails.get(j).getSupplier().intValue()));
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("name"))
	    		    	{
	    		    		Cell cell = row.createCell(++coordinationDetailsCellNo);
	    		        	cell.setCellValue(SynchroGlobal.getSupplierGroup().get(coordinationDetails.get(j).getSupplierGroup().intValue()));
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("createDate"))
	    		    	{
	    		    		Cell cell = row.createCell(++coordinationDetailsCellNo);
	    		        	cell.setCellValue(df.format(new Date(coordinationDetails.get(j).getCreationDate())));
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("lastUpdated"))
	    		    	{
	    		    		Cell cell = row.createCell(++coordinationDetailsCellNo);
	    		        	cell.setCellValue(df.format(new Date(coordinationDetails.get(j).getModifiedDate())));
	    		        	cell.setCellStyle(cellStyle);
	    		    	}
	    		    	for(int k=0;k<coordinationDetails.get(j).getFieldWorkAgencies().size();k++)
	    		    	{
		    		    	if(k>0 && coordinationDetailRowNo>row_idx)
		    				{
		    					row = sheet.createRow(++row_idx);
		    				}
		    				else
		    				{
		    					row = sheet.getRow(coordinationDetailRowNo);
		    				}
		    		    	int fwAgencyCellNo = new Integer(coordinationDetailsCellNo);
		    		    	
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwSupplier"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		        	cell.setCellValue(SynchroGlobal.getSuppliers().get(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getFwSupplier().intValue()));
		    		        	cell.setCellStyle(cellStyle);
		    		    	}
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwSupplierGroup"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		        	cell.setCellValue(SynchroGlobal.getSupplierGroup().get(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getFwSupplierGroup().intValue()));
		    		        	cell.setCellStyle(cellStyle);
		    		    	}
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwEndMarket"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		        	cell.setCellValue(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getFwEndMarket());
		    		        	cell.setCellStyle(cellStyle);
		    		    	}
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("tenderingAgencyID"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		        	if(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getTenderingAgencyID()!=null)
		    		        	{
		    		        		cell.setCellValue(SynchroGlobal.getTenderingAgency().get(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getTenderingAgencyID().intValue()));
		    		        	}
		    		        	cell.setCellStyle(cellStyle);
		    		    	}
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("bidValue"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		        	if(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getBidValue()!=null)
		    		        	{
		    		        		cell.setCellValue(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getBidValue().intValue());
		    		        	}
		    		        	cell.setCellStyle(cellStyle);
		    		    	}
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("marketType"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		        	//cell.setCellValue(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getMarketType());
		    		        	if(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getMarketType()!=null)
		    		        	{
		    		        		if(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getMarketType()==15)
		    		        		{
		    		        			cell.setCellValue(SynchroGlobal.T15_T40.T15.displayName());
		    		        		}
		    		        		if(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getMarketType()==40)
		    		        		{
		    		        			cell.setCellValue(SynchroGlobal.T15_T40.T40.displayName());
		    		        		}
		    		        	}
		    		        	cell.setCellStyle(cellStyle);
		    		    	}
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("collectionMethod"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		        	//cell.setCellValue(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getCollectionMethod().toString());
		    		        	if(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getDataCollectionMethod()!=null && coordinationDetails.get(j).getFieldWorkAgencies().get(k).getDataCollectionMethod().size()>0)
		    		        	{
			    		        	StringBuilder dataCollection = new StringBuilder();
		    		    			int count = 1;
		    		    			for(Long dc:coordinationDetails.get(j).getFieldWorkAgencies().get(k).getDataCollectionMethod())
		    		    			{
		    		    				dataCollection.append(SynchroGlobal.getDataCollections().get(dc.intValue()));
		    		    				if(count<coordinationDetails.get(j).getFieldWorkAgencies().get(k).getDataCollectionMethod().size())
		    		    				{
		    		    					dataCollection.append(",");
		    		    				}
		    		    				count++;
		    		    			}
		    		    			
		    		    			//cell.setCellValue(endMarketDetails.get(j).getDataCollectionMethod().toString());
		    		    			cell.setCellValue(dataCollection.toString());
		    		    			cell.setCellStyle(cellStyle);
		    		        	}
		    		    	}
		    		    	if(dataExtractFilters.getCoordinatingDetailFields().contains("fwCancelled"))
		    		    	{
		    		    		Cell cell = row.createCell(++fwAgencyCellNo);
		    		    		if(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getFwCancelled()!=null)
		    		    		{
		    		    			cell.setCellValue(coordinationDetails.get(j).getFieldWorkAgencies().get(k).getFwCancelled()?"yes":"no");
		    		    		}
		    		    		cell.setCellStyle(cellStyle);
		    		    	}
		    		    	coordinationDetailRowNo++;
	    		    	}
	    		    	
	    			}
	    		}
	    		//Financial Details Sections
	    		if(dataExtractFilters.getFinancialDetailFields()!=null)
	    		{
	    			List<? extends User> budgetApprovers = getSynchroProjectManager().getBudgetApprovers(dataExtractList.get(i).getProjectID());
	    			List<Integer> budgetYears = getSynchroProjectManager().getProjectBudgetYears(getSynchroProjectManager().get(dataExtractList.get(i).getProjectID()));
	    			for(int j=0;j<budgetApprovers.size();j++)
	    	    	{
	    	    		if(j>0 && financialDetailRowNo>row_idx)
	    				{
	    					row = sheet.createRow(++row_idx);
	    				}
	    				else
	    				{
	    					row = sheet.getRow(financialDetailRowNo);
	    				}
	    	    		int financialDetailsCellNo = new Integer(financialDetailsOriginalCellNo);
	    	    		if(dataExtractFilters.getFinancialDetailFields().contains("budgetApprover"))
	    		    	{
	    	    			Cell cell = row.createCell(++financialDetailsCellNo);
	    		    		cell.setCellValue(getUserManager().getUser(budgetApprovers.get(j)).getName());
	    		    		cell.setCellStyle(cellStyle);
	    		    	}
	    	    		if(dataExtractFilters.getFinancialDetailFields().contains("holderLocation"))
	    		    	{
	    	    			Cell cell = row.createCell(++financialDetailsCellNo);
	    		    		cell.setCellValue(getSynchroUtils().getUserCountry(budgetApprovers.get(j)));
	    		    		cell.setCellStyle(cellStyle);
	    		    	}
	    	    		for(int k=0;k<budgetYears.size();k++ )
	    				{
	    	    			if(k>0 && financialDetailRowNo>row_idx)
	        				{
	        					row = sheet.createRow(++row_idx);
	        				}
	        				else
	        				{
	        					row = sheet.getRow(financialDetailRowNo);
	        				}
	    	    			int financialBudYearCellNo = new Integer(financialDetailsCellNo);
	    	    			FinancialDetailPrePlan financialPrePlan = getFinancialDetailsManager().getPrePlanDetails(dataExtractList.get(i).getProjectID(), budgetYears.get(k), budgetApprovers.get(j).getID());
	    					
	    					if(dataExtractFilters.getFinancialDetailFields().contains("holderFunction"))
	        		    	{
	        	    			Cell cell = row.createCell(++financialBudYearCellNo);
	        		    		if(financialPrePlan.getHolderFunction()!=null)
	        		    		{
	        		    			cell.setCellValue(financialPrePlan.getHolderFunction());
	        		    		}
	        		    		cell.setCellStyle(cellStyle);
	        		    	}
	    					if(dataExtractFilters.getFinancialDetailFields().contains("budgetYear"))
	        		    	{
	        	    			Cell cell = row.createCell(++financialBudYearCellNo);
	        		    		cell.setCellValue(budgetYears.get(k));
	        		    		cell.setCellStyle(cellStyle);
	        		    	}
	    					if(dataExtractFilters.getFinancialDetailFields().contains("prePlanDetails"))
	        		    	{
	        	    			Cell cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialPrePlan.getTotalCosts()!=null)
	        	    			{
	        	    				cell.setCellValue(financialPrePlan.getTotalCosts().intValue());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialPrePlan.getTotalCosts()!=null&&financialPrePlan.getTotalCostsType()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialPrePlan.getTotalCostsType()));
	        	    				
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialPrePlan.getTotalCosts()!=null && financialPrePlan.getTotalCostsExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialPrePlan.getTotalCostsExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        		    	}
	    					if(dataExtractFilters.getFinancialDetailFields().contains("budgetDetails"))
	        		    	{
	    						FinancialDetailCoPlan financialCoPlan = getFinancialDetailsManager().getCoPlanDetails(dataExtractList.get(i).getProjectID(), budgetYears.get(k), budgetApprovers.get(j).getID());
	    						Cell cell = row.createCell(++financialBudYearCellNo);
	        		    		if(financialCoPlan.getPmBudget()!=null)
	        		    		{
	        		    			cell.setCellValue(financialCoPlan.getPmBudget().intValue());
	        		    		}
	        		    		cell.setCellStyle(cellStyle);
	        		    		cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialCoPlan.getPmBudget()!=null && financialCoPlan.getPmBudgetType()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialCoPlan.getPmBudgetType()));
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialCoPlan.getPmBudget()!=null && financialCoPlan.getPmBudgetExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialCoPlan.getPmBudgetExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        		    	}
	    					if(dataExtractFilters.getFinancialDetailFields().contains("forecastDetails"))
	        		    	{
	    						FinancialDetailForecast financialForecast = getFinancialDetailsManager().getForecastDetails(dataExtractList.get(i).getProjectID(), budgetYears.get(k), budgetApprovers.get(j).getID());
	    						Cell cell = row.createCell(++financialBudYearCellNo);
	        		    		// Quarter 1
	    						if(financialForecast.getPmQPR1()!=null)
	        		    		{
	        		    			cell.setCellValue(financialForecast.getFw1QPR1().intValue());
	        		    		}
	    						cell.setCellStyle(cellStyle);
	        		    		cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR1()!=null && financialForecast.getPmQPR1Type()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR1Type()));
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR1()!=null && financialForecast.getPmQPR1ExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialForecast.getPmQPR1ExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			
	        	    			// Quarter 2 
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR2()!=null)
	        		    		{
	        		    			cell.setCellValue(financialForecast.getPmQPR2().intValue());
	        		    		}
	        	    			cell.setCellStyle(cellStyle);
	        		    		cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR2()!=null && financialForecast.getPmQPR2Type()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR2Type()));
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR2()!=null  && financialForecast.getPmQPR2ExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialForecast.getPmQPR2ExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			
	        	    			// Quarter 3
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR3()!=null)
	        		    		{
	        		    			cell.setCellValue(financialForecast.getPmQPR3().intValue());
	        		    		}
	        	    			cell.setCellStyle(cellStyle);
	        		    		cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR3()!=null && financialForecast.getPmQPR3Type()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR3Type()));
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR3()!=null && financialForecast.getPmQPR3ExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialForecast.getPmQPR2ExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			
	        	    			// Quarter 4
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR4()!=null)
	        		    		{
	        		    			cell.setCellValue(financialForecast.getPmQPR4().intValue());
	        		    		}
	        	    			cell.setCellStyle(cellStyle);
	        		    		cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR4()!=null && financialForecast.getPmQPR4Type()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR4Type()));
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialForecast.getPmQPR4()!=null && financialForecast.getPmQPR4ExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialForecast.getPmQPR4ExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        		    		
	        		    	}
	    					if(dataExtractFilters.getFinancialDetailFields().contains("actuals"))
	        		    	{
	    						FinancialDetailActuals financialActuals = getFinancialDetailsManager().getActualsDetails(dataExtractList.get(i).getProjectID(), budgetYears.get(k), budgetApprovers.get(j).getID());
	    						Cell cell = row.createCell(++financialBudYearCellNo);
	        		    		//Project Management
	    						if(financialActuals.getPmActualCost()!=null)
	        		    		{
	        		    			cell.setCellValue(financialActuals.getPmActualCost().intValue());
	        		    		}
	    						cell.setCellStyle(cellStyle);
	    						cell = row.createCell(++financialBudYearCellNo);
	        		    		if(financialActuals.getPmActualCost()!=null && financialActuals.getPmActualCostType()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialActuals.getPmActualCostType()));
	        	    			}
	        		    		cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialActuals.getPmActualCost()!=null && financialActuals.getPmActualExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialActuals.getPmActualExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			
	        	    			//FieldWork1
	        	    			cell = row.createCell(++financialBudYearCellNo);
	    						if(financialActuals.getFwOne()!=null)
	        		    		{
	        		    			cell.setCellValue(financialActuals.getFwOne().intValue());
	        		    		}
	    						cell.setCellStyle(cellStyle);
	    						
	    						cell = row.createCell(++financialBudYearCellNo);
	        		    		if(financialActuals.getFwOne()!=null && financialActuals.getFwOneType()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialActuals.getFwOneType()));
	        	    			}
	        		    		cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialActuals.getFwOne()!=null && financialActuals.getFwOne()!=null)
	        	    			{
	        	    				cell.setCellValue(financialActuals.getFwOne().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			
	        	    			//FieldWork2
	        	    			cell = row.createCell(++financialBudYearCellNo);
	    						if(financialActuals.getFwTwo()!=null)
	        		    		{
	        		    			cell.setCellValue(financialActuals.getFwTwo().intValue());
	        		    		}
	    						cell.setCellStyle(cellStyle);
	    						cell = row.createCell(++financialBudYearCellNo);
	        		    		if(financialActuals.getFwTwo()!=null && financialActuals.getFwTwoType()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialActuals.getFwTwoType()));
	        	    			}
	        		    		cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialActuals.getFwTwo()!=null && financialActuals.getFwTwo()!=null)
	        	    			{
	        	    				cell.setCellValue(financialActuals.getFwTwo().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        	    			
	        	    			//FieldWork3
	        	    			cell = row.createCell(++financialBudYearCellNo);
	    						if(financialActuals.getFwThree()!=null)
	        		    		{
	        		    			cell.setCellValue(financialActuals.getFwThree().intValue());
	        		    		}
	    						cell.setCellStyle(cellStyle);
	    						cell = row.createCell(++financialBudYearCellNo);
	        		    		if(financialActuals.getFwThree()!=null && financialActuals.getFwThreeType()!=null)
	        	    			{
	        	    				cell.setCellValue(SynchroGlobal.getCurrencies().get(financialActuals.getFwThreeType()));
	        	    			}
	        		    		cell.setCellStyle(cellStyle);
	        	    			cell = row.createCell(++financialBudYearCellNo);
	        	    			if(financialActuals.getFwThree()!=null && financialActuals.getFwThreeExchangeRate()!=null)
	        	    			{
	        	    				cell.setCellValue(financialActuals.getFwThreeExchangeRate().toString());
	        	    			}
	        	    			cell.setCellStyle(cellStyle);
	        		    	}
	    					financialDetailRowNo++;
	    				}
	    	    		
	    	    	}
		    		
	    		}
    		}
    	}
    	*//**
		 * Auto re-sizing of column width based on content size 
		 *//*
		 sheet.setColumnWidth(0, 1000);
		int col_count =  sheet.getRow(1).getPhysicalNumberOfCells();
	  	for(int i=1; i<col_count ; i++)
	  	{
	  		sheet.autoSizeColumn(i);
	  		int width = sheet.getColumnWidth(i);
	  		if(width > 6000)
	  		{
	  			sheet.setColumnWidth(i, 6000);
	  		}
	  	}
       	
	*/
		return workbook;	
	}
	
	public static HSSFWorkbook generateAgencyEvaluationReport(List<AgencyEvaluationReport> agencyEvaluationReportList, int startMonth, int startYear, int endMonth, int endYear)
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
		/*
		
    	HSSFSheet sheet = workbook.createSheet("Agency Evaluation Report");
    	//sheet.setDisplayGridlines(false);
    	HSSFCellStyle headerStyle = SynchroSheetStylingUtil.getOrientedHeadingStyle(workbook, "1111");
    	HSSFCellStyle cellStyle = SynchroSheetStylingUtil.getCellStyle(workbook, "1111");
    	int row_idx = 4;
    	Row row = sheet.createRow(row_idx);
    		 Cell cell = row.createCell(1);
    	     cell.setCellValue("Agency Name");
    	     cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(2);
	    	 cell.setCellValue("Supplier Group");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(3);
	    	 cell.setCellValue("Project ID");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(4);
	    	 cell.setCellValue("Project Owner");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(5);
	    	 cell.setCellValue("Project Description");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(6);
	    	 cell.setCellValue("Budgeted Agency Spend");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(7);
	    	 cell.setCellValue("Currency");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(8);
	    	 cell.setCellValue("Exchange Rate");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(9);
	    	 cell.setCellValue("Current ForecastAgency Spend");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(10);
	    	 cell.setCellValue("Currency");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(11);
	    	 cell.setCellValue("Exchange Rate");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(12);
	    	 cell.setCellValue("Actual Agency Spend");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(13);
	    	 cell.setCellValue("Currency");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(14);
	    	 cell.setCellValue("Exchange Rate");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(15);
	    	 cell.setCellValue("Evaluation Score");
	    	 cell.setCellStyle(headerStyle);
	    	 cell = row.createCell(16);
	    	 cell.setCellValue("Evaluation Comment");
	    	 cell.setCellStyle(headerStyle);
	    	 row_idx = row_idx + 1;
	    	 for(AgencyEvaluationReport agencyEvaluationReport : agencyEvaluationReportList)
	    	 {
	    		if(validateDateFilters(agencyEvaluationReport.getStartMonth(), agencyEvaluationReport.getStartYear(), agencyEvaluationReport.getEndMonth(), agencyEvaluationReport.getEndYear(), startMonth, startYear, endMonth, endYear) )
	    		 {
	    		 List<CoordinationDetail> coordinationDetails = agencyEvaluationReport.getCoordinationDetails();
	    		 for(CoordinationDetail coordinationDetail : coordinationDetails)
	    		 {
	    			if(hasValidProjectStatus(agencyEvaluationReport.getProjectID()))
		    		{
		    			row = sheet.createRow(row_idx);
			    		cell = row.createCell(1);
			    		if(coordinationDetail.getAgencyID() > 0)
		    			{
		    				try{
		    				cell.setCellValue(getUserManager().getUser(coordinationDetail.getAgencyID()).getName());
		    				}catch(UserNotFoundException e){cell.setCellValue("Anonmyous");}
		    			}
		    			else
		    			{
		    				cell.setCellValue("Anonmyous");
		    			}
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(2);
			    		
			    		cell.setCellValue(SynchroGlobal.getSupplierGroup().get(coordinationDetail.getSupplierGroup().intValue()));
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(3);
			    		cell.setCellValue(coordinationDetail.getProjectID());
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(4);
			    		if(agencyEvaluationReport.getOwner() > 0)
		    			{
		    				try{
		    				cell.setCellValue(getUserManager().getUser(agencyEvaluationReport.getOwner()).getName());
		    				}catch(UserNotFoundException e){cell.setCellValue("Anonmyous");}
		    			}
		    			else
		    			{
		    				cell.setCellValue("Anonmyous");
		    			}
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(5);
			    		cell.setCellValue(agencyEvaluationReport.getProjectDescription());
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(6);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(7);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(8);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(9);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(10);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(11);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(12);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(13);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		cell = row.createCell(14);
			    		cell.setCellValue("");
			    		cell.setCellStyle(cellStyle);
			    		
			    		AgencyEvaluation agencyEvaluation = getSynchroProjectManager().getAgencyEvaluationDetails(coordinationDetail.getProjectID(), coordinationDetail.getAgencyID());
			    		if(agencyEvaluation!=null)
			    		{
			    			cell = row.createCell(15);
			    			cell.setCellValue(agencyEvaluation.getRating());
			    			cell.setCellStyle(cellStyle);
			    			cell = row.createCell(16);
			    			cell.setCellValue(agencyEvaluation.getComment());
			    			cell.setCellStyle(cellStyle);
			    		}
			    		else
			    		{
			    			cell = row.createCell(15);
			    			cell.setCellValue("");
			    			cell.setCellStyle(cellStyle);
			    			cell = row.createCell(16);
			    			cell.setCellValue("");
			    			cell.setCellStyle(cellStyle);
			    		}
			    		
			    		
		    			List<FieldWorkAgencyDetail> fieldWorkAgencies = coordinationDetail.getFieldWorkAgencies();
		    			for(FieldWorkAgencyDetail fieldWorkAgencyDetail : fieldWorkAgencies)
		    			{
		    				row_idx++;
		    				row = sheet.createRow(row_idx);
				    		cell = row.createCell(1);
				    		
				    		if(fieldWorkAgencyDetail.getTenderingAgencyID()!=null && fieldWorkAgencyDetail.getTenderingAgencyID()>0)
				    		{
				    			cell.setCellValue(SynchroGlobal.getTenderingAgency().get(Integer.parseInt(fieldWorkAgencyDetail.getTenderingAgencyID().toString())));
				    		}
				    		else
				    		{
				    			cell.setCellValue("");
				    		}
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(2);
				    		
				    		if(fieldWorkAgencyDetail.getFwSupplierGroup()!=null && fieldWorkAgencyDetail.getFwSupplierGroup()>0)
				    		{
				    			cell.setCellValue(SynchroGlobal.getFieldWorkSupplierGroup().get(Integer.parseInt(fieldWorkAgencyDetail.getFwSupplierGroup().toString())));
				    		}
				    		else
				    		{
				    			cell.setCellValue("");
				    		}
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(3);
				    		cell.setCellValue(coordinationDetail.getProjectID());
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(4);
				    		if(agencyEvaluationReport.getOwner() > 0)
			    			{
			    				try{
			    				cell.setCellValue(getUserManager().getUser(agencyEvaluationReport.getOwner()).getName());
			    				}catch(UserNotFoundException e){
			    					cell.setCellValue("Anonmyous");
			    				}
			    			}
			    			else
			    			{
			    				cell.setCellValue("Anonmyous");
			    			}				    		
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(5);
				    		cell.setCellValue(agencyEvaluationReport.getProjectDescription());
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(6);
				    		cell.setCellValue(fieldWorkAgencyDetail.getFwBudPlan()==null?"":fieldWorkAgencyDetail.getFwBudPlan().toString());
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(7);
				    		cell.setCellValue(fieldWorkAgencyDetail.getFwBudPlanType()==null?"":SynchroGlobal.getCurrencies().get(fieldWorkAgencyDetail.getFwBudPlanType().intValue()));
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(8);
				    		cell.setCellValue(fieldWorkAgencyDetail.getFwBudPlanExchangeRate()==null?"":fieldWorkAgencyDetail.getFwBudPlanExchangeRate().toString());
				    		cell.setCellStyle(cellStyle);
				    		if(SynchroUtils.getCurrentQuarter()==1)
				    		{
				    			cell = row.createCell(9);
				    			cell.setCellValue(fieldWorkAgencyDetail.getFwQPR1()==null?"":fieldWorkAgencyDetail.getFwQPR1().toString());
				    			cell.setCellStyle(cellStyle);
				    			cell = row.createCell(10);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR1Type()==null?"":SynchroGlobal.getCurrencies().get(fieldWorkAgencyDetail.getFwQPR1Type().intValue()));
					    		cell.setCellStyle(cellStyle);
					    		cell = row.createCell(11);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR1ExchangeRate()==null?"":fieldWorkAgencyDetail.getFwQPR1ExchangeRate().toString());
					    		cell.setCellStyle(cellStyle);
				    		}
				    		else if(SynchroUtils.getCurrentQuarter()==2)
				    		{
				    			cell = row.createCell(9);
				    			cell.setCellValue(fieldWorkAgencyDetail.getFwQPR2()==null?"":fieldWorkAgencyDetail.getFwQPR2().toString());
				    			cell.setCellStyle(cellStyle);
				    			cell = row.createCell(10);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR2Type()==null?"":SynchroGlobal.getCurrencies().get(fieldWorkAgencyDetail.getFwQPR2Type().intValue()));
					    		cell.setCellStyle(cellStyle);
					    		cell = row.createCell(11);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR2ExchangeRate()==null?"":fieldWorkAgencyDetail.getFwQPR2ExchangeRate().toString());
					    		cell.setCellStyle(cellStyle);
				    		}
				    		else if(SynchroUtils.getCurrentQuarter()==3)
				    		{
				    			cell = row.createCell(9);
				    			cell.setCellValue(fieldWorkAgencyDetail.getFwQPR3()==null?"":fieldWorkAgencyDetail.getFwQPR3().toString());
				    			cell.setCellStyle(cellStyle);
				    			cell = row.createCell(10);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR3Type()==null?"":SynchroGlobal.getCurrencies().get(fieldWorkAgencyDetail.getFwQPR3Type().intValue()));
					    		cell.setCellStyle(cellStyle);
					    		cell = row.createCell(11);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR3ExchangeRate()==null?"":fieldWorkAgencyDetail.getFwQPR3ExchangeRate().toString());
					    		cell.setCellStyle(cellStyle);
				    		}
				    		else if(SynchroUtils.getCurrentQuarter()==4)
				    		{
				    			cell = row.createCell(9);
				    			cell.setCellValue(fieldWorkAgencyDetail.getFwQPR4()==null?"":fieldWorkAgencyDetail.getFwQPR4().toString());
				    			cell.setCellStyle(cellStyle);
				    			cell = row.createCell(10);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR4Type()==null?"":SynchroGlobal.getCurrencies().get(fieldWorkAgencyDetail.getFwQPR4Type().intValue()));
					    		cell.setCellStyle(cellStyle);
					    		cell = row.createCell(11);
					    		cell.setCellValue(fieldWorkAgencyDetail.getFwQPR4ExchangeRate()==null?"":fieldWorkAgencyDetail.getFwQPR4ExchangeRate().toString());
					    		cell.setCellStyle(cellStyle);
				    		}
				    		
				    		cell = row.createCell(12);
				    		cell.setCellValue(fieldWorkAgencyDetail.getFwActualCost()==null?"":fieldWorkAgencyDetail.getFwActualCost().toString());
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(13);
				    		cell.setCellValue(fieldWorkAgencyDetail.getFwActualCostType()==null?"":SynchroGlobal.getCurrencies().get(fieldWorkAgencyDetail.getFwActualCostType().intValue()));
				    		cell.setCellStyle(cellStyle);
				    		cell = row.createCell(14);
				    		cell.setCellValue(fieldWorkAgencyDetail.getFwActualCostExchangeRate()==null?"":fieldWorkAgencyDetail.getFwActualCostExchangeRate().toString());
				    		cell.setCellStyle(cellStyle);
				    		agencyEvaluation = getSynchroProjectManager().getAgencyEvaluationDetails(coordinationDetail.getProjectID(), fieldWorkAgencyDetail.getFwAgencyID());			    		
				    		if(agencyEvaluation!=null)
				    		{
				    			cell = row.createCell(15);
				    			cell.setCellValue(agencyEvaluation.getRating());
				    			cell.setCellStyle(cellStyle);
				    			cell = row.createCell(16);
				    			cell.setCellValue(agencyEvaluation.getComment());
				    			cell.setCellStyle(cellStyle);
				    		}
				    		else
				    		{
				    			cell = row.createCell(15);
				    			cell.setCellValue("");
				    			cell.setCellStyle(cellStyle);
				    			cell = row.createCell(16);
				    			cell.setCellValue("");
				    			cell.setCellStyle(cellStyle);
				    		}
		    			}
		    			row_idx++;
		    		 } 
	    		 }
	    	 }
	}
	    	 *//**
	     	 * Autosize columns after data has been entered
	     	 *//*
	     sheet.setColumnWidth(0, 1000);
	     	for(int i=1; i<=16 ; i++)
	     	{
	     		sheet.autoSizeColumn(i);
	     		int width = sheet.getColumnWidth(i);
	     		
	     		if(width > 6000)
	     		{
	     			sheet.setColumnWidth(i, 6000);
	     		}
	     	}
	     	
	     	*//**
	     	 * Small Columns
	     	 *//*
	     	sheet.setColumnWidth(6, 2500);
	     	sheet.setColumnWidth(7, 2000);
	     	sheet.setColumnWidth(8, 2000);
	     	sheet.setColumnWidth(9, 2500);
	     	sheet.setColumnWidth(10, 2000);
	     	sheet.setColumnWidth(11, 2000);
	     	sheet.setColumnWidth(12, 2500);
	     	sheet.setColumnWidth(13, 2000);
	     	sheet.setColumnWidth(14, 2000);
	     	
	*/     	
	   return workbook;
	}
	/**
	 * Utililty method for formatting the excel for Project Summary Report.
	 * @param projectID
	 * @return
	 */
	public static HSSFWorkbook getProjectSummaryReport(Long projectID)
	{
		HSSFWorkbook projectSummary = new HSSFWorkbook();
		/*
    	
    	Project project = getSynchroProjectManager().get(projectID);
    	HSSFSheet sheet = projectSummary.createSheet("Project Details Summary");
    	//sheet.setDisplayGridlines(false);
    	int rownum = populateMainProjectDetails(project, sheet, projectSummary);
    	rownum++;
    	// End Market Details
    	Row row = sheet.createRow(rownum);
    	Cell cell = row.createCell(1);
    	cell.setCellValue("End Market Details");
    	Cell cell2 = row.createCell(2);
    	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
    	sheet = SynchroSheetStylingUtil.mergeCells(sheet, rownum, rownum, 1, 2);
        cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
    	
    	rownum++;
    	
    	List<EndMarketDetail> endMarketDetails = getSynchroProjectManager().getEndMarketDetails(projectID);
    	HSSFCellStyle cellStyleBL = SynchroSheetStylingUtil.getCellStyle(projectSummary, "0001");
    	HSSFCellStyle cellStyleBR = SynchroSheetStylingUtil.getCellStyle(projectSummary, "0100");
    	
    	for(int i=0;i<endMarketDetails.size();i++)
    	{
    		row = sheet.createRow(rownum++);
         	cell = row.createCell(1);
         	cell.setCellStyle(cellStyleBL);
         	cell = row.createCell(2);
         	cell.setCellStyle(cellStyleBR);
         	
    	   	//data.put("End-Market", endMarket.getName());
    	   	row = sheet.createRow(rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("End-Market "+(i+1) );
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue(endMarketDetails.get(i).getName());
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Fieldwork Start", endMarket.getStartMonth()+"/"+endMarket.getStartYear());
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Fieldwork Start");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue((endMarketDetails.get(i).getStartMonth()+1)+"/"+endMarketDetails.get(i).getStartYear());
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Report Completion", endMarket.getEndMonth()+"/"+endMarket.getEndYear());
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Report Completion");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue((endMarketDetails.get(i).getEndMonth()+1)+"/"+endMarketDetails.get(i).getEndYear());
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Has 'Number of Waves'", endMarket.getWaves()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Has 'Number of Waves'");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue((endMarketDetails.get(i).getWaves()!=null && endMarketDetails.get(i).getWaves().longValue()>0)?"yes":"no");
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Number of Waves", endMarket.getWaves()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Number of Waves");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue(endMarketDetails.get(i).getWaves()+"");
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Has 'Number of Interviews'", endMarket.getInterviews()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Has 'Number of Interviews'");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue((endMarketDetails.get(i).getInterviews()!=null && endMarketDetails.get(i).getInterviews().longValue()>0)?"yes":"no");
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Number of Interviews",  endMarket.getInterviews()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Number of Interviews");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue(endMarketDetails.get(i).getInterviews()+"");
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Has 'Number of Focus Groups'", endMarket.getFocusGroups()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Has 'Number of Focus Groups'");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue((endMarketDetails.get(i).getFocusGroups()!=null && endMarketDetails.get(i).getFocusGroups().longValue()>0)?"yes":"no");
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Number of Focus Groups", endMarket.getFocusGroups()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Number of Focus Groups");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue(endMarketDetails.get(i).getFocusGroups()+"");
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Has 'Number of Cells'", endMarket.getCells()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Has 'Number of Cells'");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue((endMarketDetails.get(i).getCells()!=null && endMarketDetails.get(i).getCells().longValue()>0)?"yes":"no");
    	    cell.setCellStyle(cellStyleBR);
    	    
        	//data.put("Number of Cells", endMarket.getCells()+"");
        	row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Number of Cells");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue(endMarketDetails.get(i).getCells()+"");
    	    cell.setCellStyle(cellStyleBR);
        	++rownum;
        	
       	}
    	//Closing row
    	row = sheet.getRow(rownum-1);	    
    	row.getCell(1).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0011"));
    	if(row.getCell(2)!=null)
    	{
    		row.getCell(2).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0110"));
    	}
    	else
    	{
    		row.createCell(2).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0110"));
    	}
    	
	     
	    
    	// Coordination Details
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(1);
    	cell.setCellValue("Coordination Details");
    	cell2 = row.createCell(2);
    	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
    	sheet = SynchroSheetStylingUtil.mergeCells(sheet, rownum, rownum, 1, 2);
        cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
        
    	++rownum;
    	
    	
    	List<CoordinationDetail> coordinationDetails = getSynchroProjectManager().getCoordinationDetails(projectID);
    	for(int i=0;i<coordinationDetails.size();i++)
    	{
    		row = sheet.createRow(rownum++);
         	cell = row.createCell(1);
         	cell.setCellStyle(cellStyleBL);
         	cell = row.createCell(2);
         	cell.setCellStyle(cellStyleBR);
         	
    		row = sheet.createRow(rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Coordinating Agency "+ (i+1));
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue(SynchroGlobal.getSuppliers().get(coordinationDetails.get(i).getSupplier().intValue()));
    	    cell.setCellStyle(cellStyleBR);
    	    
    	    row = sheet.createRow(++rownum);
    	    cell = row.createCell(1);
    	    cell.setCellValue("Supplier Group");
    	    cell.setCellStyle(cellStyleBL);
    	    cell = row.createCell(2);
    	    cell.setCellValue(SynchroGlobal.getSupplierGroup().get(coordinationDetails.get(i).getSupplierGroup().intValue()));
    	    cell.setCellStyle(cellStyleBR);
    	    
    	    List<FieldWorkAgencyDetail> fieldWorkAgency = coordinationDetails.get(i).getFieldWorkAgencies();
    	    for(int j=0;j<fieldWorkAgency.size();j++)
    	    {
    	    	row = sheet.createRow(++rownum);
        	    cell = row.createCell(1);
        	    cell.setCellValue("FieldWork Agency "+ (j+1));
        	    cell.setCellStyle(cellStyleBL);
        	    cell = row.createCell(2);
        	    cell.setCellValue(SynchroGlobal.getSuppliers().get(fieldWorkAgency.get(j).getFwSupplier().intValue()));
        	    cell.setCellStyle(cellStyleBR);
        	    
        	    row = sheet.createRow(++rownum);
        	    cell = row.createCell(1);
        	    cell.setCellValue("FieldWork Supplier Group");
        	    cell.setCellStyle(cellStyleBL);
        	    cell = row.createCell(2);
        	    cell.setCellValue(SynchroGlobal.getSupplierGroup().get(fieldWorkAgency.get(j).getFwSupplierGroup().intValue()));
        	    cell.setCellStyle(cellStyleBR);
        	     	    
    	    }
    	    rownum++;
    	    
    	}
    	
    	//for empty coordination details 
    	if(coordinationDetails.size()<1)
    	{
    		 row = sheet.createRow(rownum);
        	 cell = row.createCell(1);
        	 cell.setCellStyle(cellStyleBL);
        	 cell = row.createCell(2);
        	 cell.setCellStyle(cellStyleBR);
        	 rownum++;
        	 row = sheet.createRow(rownum);
        	 cell = row.createCell(1);
        	 cell.setCellStyle(cellStyleBL);
        	 cell = row.createCell(2);
        	 cell.setCellStyle(cellStyleBR);
        	 rownum++;
    		
    	}
    	
    	
    	//Closing row
    	row = sheet.getRow(rownum-1);	    
    	row.getCell(1).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0011"));
    	row.getCell(2).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0110"));
	    
    	// Project Status
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(1);
    	cell.setCellValue("Project Status");
    	cell2 = row.createCell(2);
    	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
    	sheet = SynchroSheetStylingUtil.mergeCells(sheet, rownum, rownum, 1, 2);
        cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
    	++rownum;
    	
    	row = sheet.createRow(rownum);
 	    cell = row.createCell(1);
 	    cell.setCellStyle(cellStyleBL);
 	    cell = row.createCell(2);
 	    cell.setCellStyle(cellStyleBR);
    	
    	row = sheet.createRow(++rownum);
 	    cell = row.createCell(1);
 	    cell.setCellValue("Overall Project Status");
 	    cell.setCellStyle(cellStyleBL);
 	    cell = row.createCell(2);
 	    cell.setCellValue(SynchroGlobal.Status.getName(project.getStatus()));
 	    cell.setCellStyle(cellStyleBR);
 	    
 	    List<Activities> projectActivities = getStageManager().getActivities(projectID);
 	    for(int i=0;i<6;i++)
 	    {
	 	    row = sheet.createRow(++rownum);
		    cell = row.createCell(1);
		    cell.setCellValue(projectActivities.get(i).getActivityName());
		    cell.setCellStyle(cellStyleBL);
		    cell = row.createCell(2);
		    cell.setCellValue(projectActivities.get(i).getActivityStatus());
		    cell.setCellStyle(cellStyleBR);
		  
	 	}
 	    //Closing row
    	row = sheet.getRow(rownum);	    
    	row.getCell(1).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0011"));
    	if(row.getCell(2)!=null)
    	{
    		row.getCell(2).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0110"));
    	}
    	else
    	{
    		row.createCell(2).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0110"));
    	}
 	    
 	    ++rownum;
 	   	    
    	// Stakeholders
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(1);
    	cell.setCellValue("Stakeholders");
    	cell2 = row.createCell(2);
    	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
    	sheet = SynchroSheetStylingUtil.mergeCells(sheet, rownum, rownum, 1, 2);
        cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectSummary, "1101"));
    	++rownum;
    	List<User> projectStakeholders = getSynchroUtils().getProjectStakeHolders(projectID);
    	row = sheet.createRow(rownum);
    	cell = row.createCell(1);
    	cell.setCellStyle(cellStyleBL);
    	cell = row.createCell(2);
    	cell.setCellStyle(cellStyleBR);
    	
		for (int i = 0; i < projectStakeholders.size(); i++) {
			row = sheet.createRow(++rownum);
			cell = row.createCell(1);
			cell.setCellValue("Stakeholder " + (i + 1));
			  cell.setCellStyle(cellStyleBL);
			cell = row.createCell(2);
			cell.setCellValue(projectStakeholders.get(i).getName());
			  cell.setCellStyle(cellStyleBR);

			row = sheet.createRow(++rownum);
			cell = row.createCell(1);
			cell.setCellValue("Stakeholder Role Type");
			  cell.setCellStyle(cellStyleBL);
			cell = row.createCell(2);
			if(getSynchroUtils().getUserRole(projectStakeholders.get(i)).size() > 0)
			{
				String role = getSynchroUtils().getUserRole(projectStakeholders.get(i)).toString();
				cell.setCellValue(role.substring(role.indexOf("[")+1, role.indexOf("]")-1));
			}
			else
			{
				cell.setCellValue("");
			}
			
			  cell.setCellStyle(cellStyleBR);
		}
		//Closing row
    	row = sheet.getRow(rownum);	    
    	row.getCell(1).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0011"));
    	if(row.getCell(2) !=null)
    	{
    		row.getCell(2).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0110"));	
    	}
    	else
    	{
    		row.createCell(2).setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectSummary, "0110"));
    	}
    	  
		
    	*//**
    	 * Autosize columns after data has been entered
    	 *//*
    	  sheet.setColumnWidth(0, 1000);
	     	for(int i=1; i<=2 ; i++)
	     	{
	     		sheet.autoSizeColumn(i);
	     		int width = sheet.getColumnWidth(i);
	     		if(width > 10000)
	     		{
	     			sheet.setColumnWidth(i, 10000);
	     		}
	     	}
		
    	return projectSummary;
    
	
	*/
		
		return projectSummary;
		}
	
	/*
	 * Common method for populating the Main Project Details
	 * 
	 */
	public static int populateMainProjectDetails(Project project,HSSFSheet sheet, HSSFWorkbook hwb)
	{/*
		//HSSFSheet sheet = projectSummary.createSheet("Project Details Summary");
		
    	Row row = sheet.createRow(1);
    	Cell cell = row.createCell(1);
    	cell.setCellValue("Main Project details");
    	cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(hwb, "1101"));
    	Cell cell2 = row.createCell(2);
    	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(hwb, "1101"));
    	sheet = SynchroSheetStylingUtil.mergeCells(sheet, 1, 1, 1, 2);
    	
       
        
        row = sheet.createRow(2);
	    cell = row.createCell(1);
	    cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(hwb, "0001"));
	    cell = row.createCell(2);
	    cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(hwb, "0100"));
	    
    	int rownum = 3;
    	
    	HSSFCellStyle cellStyle = SynchroSheetStylingUtil.getCellStyle(hwb, "0001");
    	HSSFCellStyle cellStyleV = SynchroSheetStylingUtil.getCellStyle(hwb, "0100");
    	row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Project ID");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue(project.getProjectID()+"");
	    cell.setCellStyle(cellStyleV);
	    
	    *//**
	     * Empty Row
	     *//*
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue("");
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Project Name");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue(project.getName());
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Project Description");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue(project.getDescription());
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Project Owner");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    if(project.getOwnerID() > 0)
		{
	    	try
	    	{
	    		cell.setCellValue(getUserManager().getUser(project.getOwnerID()).getName());
	    	}
	    	catch(UserNotFoundException e)
	    	{
	    		cell.setCellValue("Anonymous");
	    	}
		}
		else
		{
			cell.setCellValue("Anonymous");
		}
	    cell.setCellStyle(cellStyleV);
	   
	    *//**
	     * Empty Row
	     *//*
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue("");
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Brand");
	    cell.setCellStyle(cellStyle);	    
	    
	    cell = row.createCell(2);
	    cell.setCellValue(SynchroGlobal.getBrands(false,project.getBrand()).get(project.getBrand().intValue()));
	    cell.setCellStyle(cellStyleV);
	    
	    StringBuilder endMarkets = new StringBuilder();
    	for(Long endMarket:project.getEndMarkets())
    	{
    		if(endMarkets.length()>0)
    		{
    			endMarkets.append(","+getSynchroProjectManager().getEndMarketDetail(project.getProjectID(), endMarket).getName());
    		}
    		else
    		{
    			endMarkets.append(getSynchroProjectManager().getEndMarketDetail(project.getProjectID(), endMarket).getName());
    		}
    	}
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("End-Markets");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue(endMarkets.toString());
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Project Start");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue((project.getStartMonth()+1)+"/"+project.getStartYear());
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Project End");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue((project.getEndMonth()+1)+"/"+project.getEndYear());
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Workflow Type");
	    cell.setCellStyle(cellStyle);
	    cell = row.createCell(2);
	    cell.setCellValue(project.getWorkflowType());
	    cell.setCellStyle(cellStyleV);
	    
	    row = sheet.createRow(rownum++);
	    cell = row.createCell(1);
	    cell.setCellValue("Project has fieldwork");
	    cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(hwb, "0011"));
	    cell = row.createCell(2);
	    cell.setCellValue(project.getFwEnabled()?"yes":"no");
	    cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(hwb, "0110"));
	    return rownum;
	*/
	return -1;	
	}
	
	public static HSSFWorkbook getProjectFinancialReport(Long projectID)
	{
		HSSFWorkbook projectFinancialReport = new HSSFWorkbook();
		/*
    	
    	Project project = getSynchroProjectManager().get(projectID);
    	HSSFSheet sheet = projectFinancialReport.createSheet("Project Finance Summary");
    	//sheet.setDisplayGridlines(false);
    	int rownum = populateMainProjectDetails(project, sheet, projectFinancialReport);
    	rownum++;
    	rownum++;
    	
    	// Budget Approvers and Budget Section
    	List<? extends User> budgetApprovers = getSynchroProjectManager().getBudgetApprovers(projectID);
    	Row row = sheet.createRow(rownum);
    	Cell cell = row.createCell(1);
    	cell.setCellValue("Budget Approvers & Budgets");
    	int size = budgetApprovers.size();
    	
    	*//**
    	 * Merging columns for "Budget Approvers & Budgets as header for all Budget holders
    	 *//*
    	
    	Cell cell2;
    	for(int col_idx=2; col_idx<=(size+1); col_idx++)
    	{
    		cell2 = row.createCell(col_idx);
        	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectFinancialReport, "1101"));
    	}
    	sheet = SynchroSheetStylingUtil.mergeCells(sheet, rownum, rownum, 1, size+1);
        cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectFinancialReport, "1101"));
    	
    	//int cellNo = 2;
    	//row = sheet.createRow(++rownum);
        HSSFCellStyle cellIStyleBL = SynchroSheetStylingUtil.getCellStyleItalic(projectFinancialReport, "0001");
    	HSSFCellStyle cellIStyleBR = SynchroSheetStylingUtil.getCellStyleItalic(projectFinancialReport, "0100");
    	HSSFCellStyle cellIStyleBN = SynchroSheetStylingUtil.getCellStyleItalic(projectFinancialReport, "0000");
    	
    	HSSFCellStyle cellStyleBL = SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0001");
    	HSSFCellStyle cellStyleBR = SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0100");
    	HSSFCellStyle cellStyleBN = SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0000");
    	
    	Integer startBugAppRowNum = new Integer(rownum);
    	List<Integer> budgetYears = getSynchroProjectManager().getProjectBudgetYears(project);
    	Integer endBugAppRowNum = new Integer(rownum);
    	boolean lastcol = false;
    	int budgetCols = budgetApprovers.size() + 2;
    	for(int i=0;i<budgetApprovers.size();i++)
    	{
    		if((i+1)==budgetApprovers.size())
    		{
    			lastcol = true;
    		}
    		if(i==0)
			{
				row = sheet.createRow(++rownum);
				cell = row.createCell(i + 1);
				cell.setCellStyle(cellStyleBL);
				cell = row.createCell(i + 2);
				cell.setCellValue("Budget Approver " + (i + 1));
				if(lastcol)
					cell.setCellStyle(cellStyleBR);
				else
					cell.setCellStyle(cellStyleBN);
			}
			else
			{
				cell = sheet.getRow(++startBugAppRowNum).createCell(i + 2);
				cell.setCellValue("Budget Approver " + (i + 1));
				if(lastcol)
					cell.setCellStyle(cellStyleBR);
				else
					cell.setCellStyle(cellStyleBN);
			}
    		
    		//cell = row.createCell(cellNo);
			//cell.setCellValue("Budget Approver " + (i + 1));
			//cellNo++;
			// This check is for first column
			if (i == 0) {
				row = sheet.createRow(++rownum);
				cell = row.createCell(1);
				cell.setCellValue("Budget Approver");
				cell.setCellStyle(cellStyleBL);
				
				cell = row.createCell(i+2);
				cell.setCellValue(budgetApprovers.get(i).getName());
				if(lastcol)
					cell.setCellStyle(cellIStyleBR);
				else
					cell.setCellStyle(cellIStyleBN);
			} else {
				cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
				cell.setCellValue(budgetApprovers.get(i).getName());
				if(lastcol)
					cell.setCellStyle(cellIStyleBR);
				else
					cell.setCellStyle(cellIStyleBN);
			}
			if(i==0)
			{
				row = sheet.createRow(++rownum);
				cell = row.createCell(1);
				cell.setCellValue("Budget Approver Role Type");
				cell.setCellStyle(cellStyleBL);
				cell = row.createCell(i+2);
				cell.setCellValue(getSynchroUtils().getUserRole(budgetApprovers.get(i)).toString());
				if(lastcol)
					cell.setCellStyle(cellIStyleBR);
				else
					cell.setCellStyle(cellIStyleBN);
			}
			else
			{
				cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
				cell.setCellValue(getSynchroUtils().getUserRole(budgetApprovers.get(i)).toString());
				if(lastcol)
					cell.setCellStyle(cellStyleBR);
				else
					cell.setCellStyle(cellStyleBN);
			}
			if(i==0)
			{
				row = sheet.createRow(++rownum);
				cell = row.createCell(1);
				cell.setCellValue("Market");
				cell.setCellStyle(cellStyleBL);
				cell = row.createCell(i+2);
				cell.setCellValue(getSynchroUtils().getUserCountry(budgetApprovers.get(i)));
				if(lastcol)
					cell.setCellStyle(cellIStyleBR);
				else
					cell.setCellStyle(cellIStyleBN);
			}
			else
			{
				cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
				cell.setCellValue(getSynchroUtils().getUserCountry(budgetApprovers.get(i)));
				if(lastcol)
					cell.setCellStyle(cellIStyleBR);
				else
					cell.setCellStyle(cellIStyleBN);
			}
			for(Integer budgetYear:budgetYears )
			{
				FinancialDetailPrePlan financialPrePlan = getFinancialDetailsManager().getPrePlanDetails(projectID, budgetYear, budgetApprovers.get(i).getID());
				FinancialDetailCoPlan financialCoPlan = getFinancialDetailsManager().getCoPlanDetails(projectID, budgetYear, budgetApprovers.get(i).getID());
				FinancialDetailForecast financialForecast = getFinancialDetailsManager().getForecastDetails(projectID, budgetYear, budgetApprovers.get(i).getID());
				FinancialDetailActuals financialActuals = getFinancialDetailsManager().getActualsDetails(projectID, budgetYear, budgetApprovers.get(i).getID());
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Budget Holder Function");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialPrePlan.getHolderFunction()!=null)
					{
						cell.setCellValue(SynchroGlobal.getBudgetHolderFunctions().get(financialPrePlan.getHolderFunction().intValue()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialPrePlan.getHolderFunction()!=null)
					{
						cell.setCellValue(SynchroGlobal.getBudgetHolderFunctions().get(financialPrePlan.getHolderFunction().intValue()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Budget Year");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					cell.setCellValue(budgetYear);
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++rownum;
				}
				else
				{
					
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					cell.setCellValue(budgetYear);
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++startBugAppRowNum;
				}
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(budgetCols-1);
				cell.setCellStyle(cellIStyleBR);						
				
				// Financial Details Pre Plan section
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Total Preplan Costs");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					//cell.setCellValue(financialPrePlan.getTotalCosts().intValue());
					if(financialPrePlan.getTotalCosts()!=null)
					{
						cell.setCellValue(financialPrePlan.getTotalCosts().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialPrePlan.getTotalCosts()!=null)
					{
						cell.setCellValue(financialPrePlan.getTotalCosts().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					// Currency will be fetched only when there is cost associated with it
					if(financialPrePlan.getTotalCosts()!=null && financialPrePlan.getTotalCostsType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialPrePlan.getTotalCostsType()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialPrePlan.getTotalCosts()!=null && financialPrePlan.getTotalCostsType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialPrePlan.getTotalCostsType()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialPrePlan.getTotalCosts()!=null && financialPrePlan.getTotalCostsExchangeRate()!=null)
					{
						cell.setCellValue(financialPrePlan.getTotalCostsExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++rownum;
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialPrePlan.getTotalCosts()!=null && financialPrePlan.getTotalCostsExchangeRate()!=null)
					{
						cell.setCellValue(financialPrePlan.getTotalCostsExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++startBugAppRowNum;
				}
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(budgetCols-1);
				cell.setCellStyle(cellIStyleBR);		
				
				
				// Financial Details Co Plan section
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Project Management Budget (CoPlan)");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					//cell.setCellValue(financialCoPlan.getPmBudget().intValue());
					if(financialCoPlan.getPmBudget()!=null)
					{
						cell.setCellValue(financialCoPlan.getPmBudget().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialCoPlan.getPmBudget()!=null)
					{
						cell.setCellValue(financialCoPlan.getPmBudget().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					//cell.setCellValue(financialCoPlan.getPmBudgetType());
					if(financialCoPlan.getPmBudget()!=null && financialCoPlan.getPmBudgetType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialCoPlan.getPmBudgetType()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialCoPlan.getPmBudget()!=null && financialCoPlan.getPmBudgetType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialCoPlan.getPmBudgetType()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialCoPlan.getPmBudget()!=null && financialCoPlan.getPmBudgetExchangeRate()!=null)
					{
						cell.setCellValue(financialCoPlan.getPmBudgetExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++rownum;
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialCoPlan.getPmBudget()!=null && financialCoPlan.getPmBudgetExchangeRate()!=null)
					{
						cell.setCellValue(financialCoPlan.getPmBudgetExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++startBugAppRowNum;
				}
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(budgetCols-1);
				cell.setCellStyle(cellIStyleBR);	
				
				// Financial Details Forecast section
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Project Management QPR1 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR1()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR1().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR1()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR1().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR1()!=null && financialForecast.getPmQPR1Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR1Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR1()!=null && financialForecast.getPmQPR1Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR1Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR1()!=null && financialForecast.getPmQPR1ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR1ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR1()!=null && financialForecast.getPmQPR1ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR1ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Project Management QPR2 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR2()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR2().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					//cell.setCellValue(financialForecast.getPmQPR2().intValue());
					if(financialForecast.getPmQPR2()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR2().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					
					if(financialForecast.getPmQPR2()!=null && financialForecast.getPmQPR2Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR2Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR2()!=null && financialForecast.getPmQPR2Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR2Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR2()!=null && financialForecast.getPmQPR2ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR2ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR2()!=null && financialForecast.getPmQPR2ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR2ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Project Management QPR3 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					//cell.setCellValue(financialForecast.getPmQPR3().intValue());
					if(financialForecast.getPmQPR3()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR3().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					//cell.setCellValue(financialForecast.getPmQPR3().intValue());
					if(financialForecast.getPmQPR3()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR3().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR3()!=null && financialForecast.getPmQPR3Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR3Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR3()!=null && financialForecast.getPmQPR3Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR3Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR3()!=null && financialForecast.getPmQPR3ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR3ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR3()!=null && financialForecast.getPmQPR3ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR3ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Project Management QPR4 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					//cell.setCellValue(financialForecast.getPmQPR4().intValue());
					if(financialForecast.getPmQPR4()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR4().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR4()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR4().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR4()!=null && financialForecast.getPmQPR4Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR4Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR4()!=null && financialForecast.getPmQPR4Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialForecast.getPmQPR4Type()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialForecast.getPmQPR4()!=null && financialForecast.getPmQPR4ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR4ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++rownum;
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialForecast.getPmQPR4()!=null && financialForecast.getPmQPR4ExchangeRate()!=null)
					{
						cell.setCellValue(financialForecast.getPmQPR4ExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++startBugAppRowNum;
				}
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(budgetCols-1);
				cell.setCellStyle(cellIStyleBR);		
				
				
				// Financial Details Actuals section
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Project Management Actual Cost");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					//cell.setCellValue(financialActuals.getPmActualCost().intValue());
					if(financialActuals.getPmActualCost()!=null)
					{
						cell.setCellValue(financialActuals.getPmActualCost().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialActuals.getPmActualCost()!=null)
					{
						cell.setCellValue(financialActuals.getPmActualCost().intValue());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					
					if(financialActuals.getPmActualCost()!=null && financialActuals.getPmActualCostType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialActuals.getPmActualCostType()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialActuals.getPmActualCost()!=null && financialActuals.getPmActualCostType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(financialActuals.getPmActualCostType()));
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
				}
				if(i==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(i+2);
					if(financialActuals.getPmActualCost()!=null && financialActuals.getPmActualExchangeRate()!=null)
					{
						cell.setCellValue(financialActuals.getPmActualExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++rownum;
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(i+2);
					if(financialActuals.getPmActualCost()!=null && financialActuals.getPmActualExchangeRate()!=null)
					{
						cell.setCellValue(financialActuals.getPmActualExchangeRate().toString());
					}
					if(lastcol)
						cell.setCellStyle(cellIStyleBR);
					else
						cell.setCellStyle(cellIStyleBN);
					++startBugAppRowNum;
				}
				
			}
			
			startBugAppRowNum=endBugAppRowNum;
			//endBugAppRowNum=rownum;

		}
    	
    	//Closing row
    	row = sheet.createRow(rownum);
    	cell = row.createCell(1);
    	cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0011"));
    	for(int k=1;k<size;k++)
    	{
    		cell = row.createCell(k+1);
    		cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0010"));
    	}
    	cell = row.createCell(size+1);
		cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0110"));
    	
		rownum=rownum+2;
    	//Coordination Details
		List<CoordinationDetail> coordinationDetails = getSynchroProjectManager().getCoordinationDetails(projectID);
		int csize = coordinationDetails.size();
    	
    	row = sheet.createRow(++rownum);
		cell = row.createCell(1);
		cell.setCellValue("Coordination Details");
		int maxCoodSize = 2;
		
		for(int i=0;i<coordinationDetails.size();i++)
		{
			List<FieldWorkAgencyDetail> fwAgencyDetail = coordinationDetails.get(i).getFieldWorkAgencies();
			if((fwAgencyDetail.size()+2)>maxCoodSize)
			{
				maxCoodSize = fwAgencyDetail.size()+2;
			}
			
		}
    	for(int col_idx=2; col_idx<maxCoodSize; col_idx++)
    	{
    		cell2 = row.createCell(col_idx);
        	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectFinancialReport, "1101"));
    	}
		sheet = SynchroSheetStylingUtil.mergeCells(sheet, rownum, rownum, 1, maxCoodSize-1);
        cell.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(projectFinancialReport, "1101"));
		++rownum;
		
		//Empty formatted row
		row = sheet.createRow(rownum);
		cell = row.createCell(1);
		cell.setCellStyle(cellStyleBL);
		cell = row.createCell(maxCoodSize-1);
		cell.setCellStyle(cellStyleBR);		
		
		lastcol= false;
		int coordCols = 2;
		for(int i=0;i<coordinationDetails.size();i++)
		{
			row = sheet.createRow(++rownum);
			cell = row.createCell(1);
			cell.setCellValue("Coordinating Agency "+ (i+1));
			cell.setCellStyle(cellStyleBL);
			cell = row.createCell(2);
			cell.setCellValue(SynchroGlobal.getSuppliers().get(coordinationDetails.get(i).getSupplier().intValue()));
			if((maxCoodSize-1)==2)
			{
				cell.setCellStyle(cellIStyleBR);		
			}
			else
			{
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);		
			}
			
			//TODO Border bottom
			row = sheet.createRow(++rownum);
			cell = row.createCell(1);
			cell.setCellValue("Supplier Group");
			cell.setCellStyle(cellStyleBL);
			cell = row.createCell(2);
			cell.setCellValue(SynchroGlobal.getSupplierGroup().get(coordinationDetails.get(i).getSupplierGroup().intValue()));
			if((maxCoodSize-1)==2)
			{
				cell.setCellStyle(cellIStyleBR);		
			}
			else
			{
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);		
			}
			++rownum;
			

			//Empty formatted row
			row = sheet.createRow(rownum);
			cell = row.createCell(1);
			cell.setCellStyle(cellIStyleBL);
			cell = row.createCell(maxCoodSize-1);
			cell.setCellStyle(cellIStyleBR);		
			
			startBugAppRowNum = new Integer(rownum);
			endBugAppRowNum = new Integer(rownum);
			
			List<FieldWorkAgencyDetail> fwAgencyDetail = coordinationDetails.get(i).getFieldWorkAgencies();
			int fwCols = fwAgencyDetail.size() + 2;
			if((fwAgencyDetail.size()+2)>=coordCols)
			{
				coordCols = fwAgencyDetail.size()+2;
			}
			lastcol = false;
			for(int j=0;j<fwAgencyDetail.size();j++)
			{
				if((j+1)==fwAgencyDetail.size())
				{
					lastcol = true;
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(1);
					cell.setCellStyle(cellIStyleBL);
					cell = row.createCell(j+2);
					cell.setCellValue("Fieldwork Agency "+ (j+1));
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					cell.setCellValue("Fieldwork Agency "+ (j+1));
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork Agency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					cell.setCellValue(SynchroGlobal.getSuppliers().get(fwAgencyDetail.get(j).getFwSupplier().intValue()));
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					cell.setCellValue(SynchroGlobal.getSuppliers().get(fwAgencyDetail.get(j).getFwSupplier().intValue()));
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork Supplier Group ");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					cell.setCellValue(SynchroGlobal.getSupplierGroup().get(fwAgencyDetail.get(j).getFwSupplierGroup().intValue()));
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++rownum;
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					cell.setCellValue(SynchroGlobal.getSupplierGroup().get(fwAgencyDetail.get(j).getFwSupplierGroup().intValue()));
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++startBugAppRowNum;
				}
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);	
				
				
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork Budget (CoPlan)");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwBudPlan()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwBudPlan().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwBudPlan()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwBudPlan().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwBudPlan()!=null && fwAgencyDetail.get(j).getFwBudPlanType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwBudPlanType()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwBudPlan()!=null && fwAgencyDetail.get(j).getFwBudPlanType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwBudPlanType()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwBudPlan()!=null && fwAgencyDetail.get(j).getFwBudPlanExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwBudPlanExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++rownum;
					
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwBudPlan()!=null && fwAgencyDetail.get(j).getFwBudPlanExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwBudPlanExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++startBugAppRowNum;
				}
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);		
				
				
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork QPR1 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR1()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR1().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR1()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR1().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR1()!=null && fwAgencyDetail.get(j).getFwQPR1Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR1Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR1()!=null && fwAgencyDetail.get(j).getFwQPR1Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR1Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR1()!=null && fwAgencyDetail.get(j).getFwQPR1ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR1ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++rownum;
					
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR1()!=null && fwAgencyDetail.get(j).getFwQPR1ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR1ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++startBugAppRowNum;
				}
				
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);		
				
				
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork QPR2 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR2()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR2().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR2()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR2().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR2()!=null && fwAgencyDetail.get(j).getFwQPR2Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR2Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR2()!=null && fwAgencyDetail.get(j).getFwQPR2Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR2Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR2()!=null && fwAgencyDetail.get(j).getFwQPR2ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR2ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++rownum;
					
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR2()!=null && fwAgencyDetail.get(j).getFwQPR2ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR2ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++startBugAppRowNum;
				}
				
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);		
				
				
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork QPR3 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR3()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR3().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR3()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR3().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR3()!=null && fwAgencyDetail.get(j).getFwQPR3Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR3Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR3()!=null && fwAgencyDetail.get(j).getFwQPR3Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR3Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR3()!=null && fwAgencyDetail.get(j).getFwQPR3ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR3ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++rownum;
					
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR3()!=null && fwAgencyDetail.get(j).getFwQPR3ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR3ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++startBugAppRowNum;
				}
				
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellIStyleBL);
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);	
				
				
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork QPR4 Forecast");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR4()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR4().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR4()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR4().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR4()!=null && fwAgencyDetail.get(j).getFwQPR4Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR4Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR4()!=null && fwAgencyDetail.get(j).getFwQPR4Type()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwQPR4Type()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR4()!=null && fwAgencyDetail.get(j).getFwQPR4ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR4ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++rownum;
					
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwQPR4()!=null && fwAgencyDetail.get(j).getFwQPR4ExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwQPR4ExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++startBugAppRowNum;
				}
				
				
				//Empty row with formating
				row = sheet.createRow(rownum);
				cell = row.createCell(1);
				cell.setCellStyle(cellStyleBL);
				cell = row.createCell(maxCoodSize-1);
				cell.setCellStyle(cellIStyleBR);			
				
				
				
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Fieldwork Actual Cost");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwActualCost()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwActualCost().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwActualCost()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwActualCost().intValue());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Currency");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwActualCost()!=null && fwAgencyDetail.get(j).getFwActualCostType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwActualCostType()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwActualCost()!=null && fwAgencyDetail.get(j).getFwActualCostType()!=null)
					{
						cell.setCellValue(SynchroGlobal.getCurrencies().get(fwAgencyDetail.get(j).getFwActualCostType()));
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
				}
				if(j==0)
				{
					row = sheet.createRow(++rownum);
					cell = row.createCell(j+1);
					cell.setCellValue("Exchange Rate");
					cell.setCellStyle(cellStyleBL);
					cell = row.createCell(j+2);
					if(fwAgencyDetail.get(j).getFwActualCost()!=null && fwAgencyDetail.get(j).getFwActualCostExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwActualCostExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++rownum;
					
				}
				else
				{
					cell = sheet.getRow(++startBugAppRowNum).createCell(j+2);
					if(fwAgencyDetail.get(j).getFwActualCost()!=null && fwAgencyDetail.get(j).getFwActualCostExchangeRate()!=null)
					{
						cell.setCellValue(fwAgencyDetail.get(j).getFwActualCostExchangeRate().toString());
					}
					if(lastcol)
					{
						if((j+2)==(maxCoodSize-1))
						{
							cell.setCellStyle(cellIStyleBR);
						}
						else
						{
							cell = row.createCell(maxCoodSize-1);
							cell.setCellStyle(cellIStyleBR);
						}
					}
					else
					{
						cell.setCellStyle(cellIStyleBN);
					}
					++startBugAppRowNum;
				}
				
				
				
				startBugAppRowNum = endBugAppRowNum;
			}
	    
		}
		
		//Closing row
    	row = sheet.createRow(rownum);
    	cell = row.createCell(1);
    	cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0011"));
    	for(int k=1;k<csize;k++)
    	{
    		cell = row.createCell(k+1);
    		cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0010"));
    	}
    	cell = row.createCell(csize+1);
		cell.setCellStyle(SynchroSheetStylingUtil.getCellStyle(projectFinancialReport, "0110"));
		
		*//**
		 * Auto resizing of columns
		 *//*
		sheet.autoSizeColumn(1);
		int colsize = budgetCols;
		if(coordCols>budgetCols)
		{
			colsize = coordCols;
		}
		for(int colno=1;colno<=colsize;colno++)
    	{
			sheet.autoSizeColumn(colno);
			int width = sheet.getColumnWidth(colno);
	  		if(width > 10000)
	  		{
	  			sheet.setColumnWidth(colno, 10000);
	  		}
    	}
		
		
	*/
		return projectFinancialReport;	
	}

	/**
	 * This method will tell whether the project start date and end date lies with the date filters 
	 */
public static Boolean validateDateFilters(int pStartMonth, int pStartYear, int pEndMonth, int pEndYear, int startMonth, int startYear, int endMonth, int endYear)
	{
	
		pStartMonth = pStartMonth+1;
		pEndMonth = pEndMonth+1;
		
		startMonth= startMonth + 1;
		endMonth= endMonth + 1;
		if(startYear>0 && startMonth<1)
		{
			startMonth = 1;
		}
		if(endYear>0 && endMonth<1)
		{
			endMonth = 1;
		}
	
		DateTime fStartDate;
		DateTime fEndDate;
		DateTime startDate;
		DateTime endDate;
		
		if(startYear>0 && endYear > 0)
		{
			fStartDate = new DateTime(startYear, startMonth, 1, 0, 0, 0, 0);
			fEndDate = new DateTime(endYear, endMonth, 1, 0, 0, 0, 0);
			startDate = new DateTime(pStartYear, pStartMonth, 1, 0, 0, 0, 0);
			endDate = new DateTime(pEndYear, pEndMonth, 1, 0, 0, 0, 0);
			if(startDate.compareTo(fStartDate)>=0 && endDate.compareTo(fEndDate)<=0)
			{
	    		return true;
	    	}
			else
			{
				return false;
			}
		}
		else if(startYear>0 && endYear < 0)
		{
			fStartDate = new DateTime(startYear, startMonth, 1, 0, 0, 0, 0);
			startDate = new DateTime(pStartYear, pStartMonth, 1, 0, 0, 0, 0);
			endDate = new DateTime(pEndYear, pEndMonth, 1, 0, 0, 0, 0);
			if(startDate.compareTo(fStartDate)>=0)
			{
	    		return true;
	    	}
			else
			{
				return false;
			}
		}
		else if(endYear > 0)
		{
			fEndDate = new DateTime(endYear, endMonth, 1, 0, 0, 0, 0);
			startDate = new DateTime(pStartYear, pStartMonth, 1, 0, 0, 0, 0);
			endDate = new DateTime(pEndYear, pEndMonth, 1, 0, 0, 0, 0);
			if(startDate.compareTo(fEndDate)<=0)
			{
	    		return true;
	    	}
			else
			{
				return false;
			}
		}
		else if(startYear < 0 && endYear < 0)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}

	public static boolean canAccessProjectReport(Long projectID) {
		if (SynchroPermHelper.isExternalAgencyUser()
				|| SynchroPermHelper.isCommunicationAgencyUser()) {
			if (SynchroPermHelper.hasProjectAccess(projectID)) {
				return true;
			} else
				return false;
		} else
			return true;
	}

	public static boolean hasValidProjectStatus(Long projectID) {/*
		if (projectID != null) {
			Project project = getSynchroProjectManager().get(projectID);
			if (project.getStatus() == SynchroGlobal.Status.DRAFT.ordinal()
					|| project.getStatus() == SynchroGlobal.Status.CONCEPT_CANCEL
							.ordinal()
					|| project.getStatus() == SynchroGlobal.Status.PLANNED_CANCEL
							.ordinal()
					|| project.getStatus() == SynchroGlobal.Status.INPROGRESS_CANCEL
							.ordinal()) {
				return false;
			} else
				return true;
		} else
			return false;
	*/
		/*Dummy Value*/
	return false;	
	}
	/**
	 * Utililty method for formatting the excel for PIB Screen.
	 * @param projectID
	 * @return
	 */
	public static HSSFWorkbook getPIBExcel(Project project,ProjectInitiation projectInitiation,List<PIBTargetSegment> targetSegmentList,List<PIBBrand> pibBrandList,List<PIBBrand> pibSmokerList,PIBStimulus pibStimulus,Document document,PIBReporting pibReporting)
	{
		HSSFWorkbook pibExcel = new HSSFWorkbook();
		/*


    	
    	//Project project = getSynchroProjectManager().get(projectID);
    	HSSFSheet sheet = pibExcel.createSheet("PIB Details");
    	
    	int rownum = 0;
    	Row row = sheet.createRow(rownum);
    	Cell cell = row.createCell(0);
    	cell.setCellValue("Project Name");
    	cell = row.createCell(1);
    	cell.setCellValue(project.getName());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Project Description");
    	cell = row.createCell(1);
    	cell.setCellValue(project.getDescription());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Project ID");
    	cell = row.createCell(1);
    	cell.setCellValue(project.getProjectID());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Version ID");
    	cell = row.createCell(1);
    	cell.setCellValue(document.getVersionID());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Version Time");
    	cell = row.createCell(1);
    	cell.setCellValue(df.format(document.getModificationDate()));
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Project Owner");
    	cell = row.createCell(1);
    	try
    	{
    		cell.setCellValue(getUserManager().getUser(project.getOwnerID()).getName());
    	}
    	catch(UserNotFoundException ue)
    	{
    		cell.setCellValue("");
    	}
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Project Has Fieldwork");
    	cell = row.createCell(1);
    	cell.setCellValue(project.getFwEnabled()?"yes":"no");
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Budget Holder");
    	cell = row.createCell(1);
    	List<? extends User> budgetApprovers = getSynchroUtils().getBudgetApprovers(project.getProjectID());
    	StringBuilder budgetApp = new StringBuilder();
    	if(budgetApprovers!=null && budgetApprovers.size()>0)
    	{
    		for(User ba:budgetApprovers)
    		{
    			if(budgetApp.length()>0)
    			{
    				budgetApp.append(",");
    			}
    			budgetApp.append(ba.getName());
    		}
    	}
    	cell.setCellValue(budgetApp.toString());
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Marketing Stakeholder");
    	cell = row.createCell(1);
    	//cell.setCellValue(getSynchroUtils().getMarketUsers(project.getProjectID()).toString());
    	
    	StringBuilder marketApp = new StringBuilder();
    	Collection<User> marketApprovers = getSynchroUtils().getMarketUsers(project.getProjectID());
    	if(marketApprovers!=null && marketApprovers.size()>0)
    	{
    		for(User ma:marketApprovers)
    		{
    			if(marketApp.length()>0)
    			{
    				marketApp.append(",");
    			}
    			marketApp.append(ma.getName());
    		}
    	}
    	cell.setCellValue(marketApp.toString());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Other BAT Stakeholder");
    	cell = row.createCell(1);
    	//cell.setCellValue(getSynchroUtils().getOtherBATUsers(project.getProjectID()).toString());
    	StringBuilder otherBATApp = new StringBuilder();
    	Collection<User> otherBATApprovers = getSynchroUtils().getOtherBATUsers(project.getProjectID());
    	if(otherBATApprovers!=null && otherBATApprovers.size()>0)
    	{
    		for(User obat:otherBATApprovers)
    		{
    			if(otherBATApp.length()>0)
    			{
    				otherBATApp.append(",");
    			}
    			otherBATApp.append(obat.getName());
    		}
    	}
    	cell.setCellValue(otherBATApp.toString());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("SP&I Stakeholder");
    	cell = row.createCell(1);
    	//cell.setCellValue(getSynchroUtils().getSPIUsers(project.getProjectID()).toString());
    	StringBuilder spiApp = new StringBuilder();
    	Collection<User> spiApprovers = getSynchroUtils().getSPIUsers(project.getProjectID());
    	if(spiApprovers!=null && spiApprovers.size()>0)
    	{
    		for(User spi:spiApprovers)
    		{
    			if(spiApp.length()>0)
    			{
    				spiApp.append(",");
    			}
    			spiApp.append(spi.getName());
    		}
    	}
    	cell.setCellValue(spiApp.toString());
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Coordinating Agency");
    	cell = row.createCell(1);
    	//cell.setCellValue(getSynchroUtils().getCoAgencyUsers(project.getProjectID()).toString());
    	StringBuilder coAg = new StringBuilder();
    	Collection<User> coAgUsers = getSynchroUtils().getCoAgencyUsers(project.getProjectID());
    	if(coAgUsers!=null && coAgUsers.size()>0)
    	{
    		for(User coAgU:coAgUsers)
    		{
    			if(coAg.length()>0)
    			{
    				coAg.append(",");
    			}
    			coAg.append(coAgU.getName());
    		}
    	}
    	cell.setCellValue(coAg.toString());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Coordinating Agency Support");
    	cell = row.createCell(1);
    	//cell.setCellValue(getSynchroUtils().getCoAgencySupportUsers(project.getProjectID()).toString());
    	StringBuilder coAgSupport = new StringBuilder();
    	Collection<User> coAgSupportUsers = getSynchroUtils().getCoAgencySupportUsers(project.getProjectID());
    	if(coAgSupportUsers!=null && coAgSupportUsers.size()>0)
    	{
    		for(User coAgSU:coAgSupportUsers)
    		{
    			if(coAgSupport.length()>0)
    			{
    				coAgSupport.append(",");
    			}
    			coAgSupport.append(coAgSU.getName());
    		}
    	}
    	cell.setCellValue(coAgSupport.toString());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Fieldwork Agency");
    	cell = row.createCell(1);
    	//cell.setCellValue(getSynchroUtils().getFieldWorkAgencyUsers(project.getProjectID()).toString());
    	StringBuilder fAgU= new StringBuilder();
    	Collection<User> fAgUsers = getSynchroUtils().getFieldWorkAgencyUsers(project.getProjectID());
    	if(fAgUsers!=null && fAgUsers.size()>0)
    	{
    		for(User fieldAgUser:fAgUsers)
    		{
    			if(fAgU.length()>0)
    			{
    				fAgU.append(",");
    			}
    			fAgU.append(fieldAgUser.getName());
    		}
    	}
    	cell.setCellValue(fAgU.toString());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Country");
    	cell = row.createCell(1);
    	//cell.setCellValue(project.getEndMarkets().toString());
    	if(project.getEndMarkets()!=null && project.getEndMarkets().size()>0)
    	{
    		StringBuilder endMarkets = new StringBuilder();
    		for(Long endMarket:project.getEndMarkets())
    		{
    			if(endMarkets.length()>0)
    			{
    				endMarkets.append(",");
    			}
    			endMarkets.append(SynchroGlobal.getEndMarkets().get(endMarket.intValue()));
    		}
    		cell.setCellValue(endMarkets.toString());
    	}
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Product Category");
    	cell = row.createCell(1);
    	if(project.getProductType()!=null)
    	{
    		cell.setCellValue(SynchroGlobal.getProductTypes().get(project.getProductType().intValue()));
    	}
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Reference Brand");
    	if(pibBrandList!=null && pibBrandList.size()>0)
    	{
    		for(int i=0;i<pibBrandList.size();i++)
    		{
    			int cellNo = 1;
    			if(i!=0)
	    		{
	    			row = sheet.createRow(++rownum);
	    		}
    			cell = row.createCell(cellNo);
	        	cell.setCellValue(SynchroGlobal.getEndMarkets().get(pibBrandList.get(i).getEndmarketID().intValue()));
	        	cell = row.createCell(++cellNo);
	        	if(pibBrandList.get(i).getBrandId()!=null && pibBrandList.get(i).getBrandId().size()>0)
	        	{
	        		StringBuilder pibBrand = new StringBuilder();
	        		for(Long brandId:pibBrandList.get(i).getBrandId())
	        		{
	        			if(pibBrand.length()>0)
	        			{
	        				pibBrand.append(",");
	        			}
	        			pibBrand.append(SynchroGlobal.getBrands(true, project.getProductType()).get(brandId.intValue()));
	        		}
	        		cell.setCellValue(pibBrand.toString());
	        	}
    		}
    	}
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Target Segment");
    	if(targetSegmentList!=null && targetSegmentList.size()>0)
    	{
    		for(int i=0;i<targetSegmentList.size();i++)
    		{
	    		int cellNo = 1;
    			if(i!=0)
	    		{
	    			row = sheet.createRow(++rownum);
	    		}
    			cell = row.createCell(cellNo);
	        	cell.setCellValue(targetSegmentList.get(i).getName());
	        	cell = row.createCell(++cellNo);
	        	cell.setCellValue(targetSegmentList.get(i).getSegementDetail());
    		}
    	}
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Smoker Groups Included in Research");
    	if(pibSmokerList!=null && pibSmokerList.size()>0)
    	{
    		for(int i=0;i<pibSmokerList.size();i++)
    		{
    			int cellNo = 1;
    			if(i!=0)
	    		{
	    			row = sheet.createRow(++rownum);
	    		}
    			cell = row.createCell(cellNo);
	        	cell.setCellValue(SynchroGlobal.getEndMarkets().get(pibSmokerList.get(i).getEndmarketID().intValue()));
	        	cell = row.createCell(++cellNo);
	        	
	        	if(pibSmokerList.get(i).getBrandId()!=null && pibSmokerList.get(i).getBrandId().size()>0)
	        	{
	        		StringBuilder smokerBrand = new StringBuilder();
	        		for(Long brandId:pibSmokerList.get(i).getBrandId())
	        		{
	        			if(smokerBrand.length()>0)
	        			{
	        				smokerBrand.append(",");
	        			}
	        			smokerBrand.append(SynchroGlobal.getBrands(true, project.getProductType()).get(brandId.intValue()));
	        		}
	        		cell.setCellValue(smokerBrand.toString());
	        	}
    		}
    	}
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Business Question");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getBizQuestion());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Critical Business Decisions");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getBizSteps());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Research Objective");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getResearchObjective());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Action Standard");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getActionStandard());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Methodology Type");
    	cell = row.createCell(1);
    	if(project.getMethodology()!=null)
    	{
    		cell.setCellValue(SynchroGlobal.getMethodologies().get(project.getMethodology().intValue()));
    	}
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Proposed Research Design");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getResearchDesign());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Other Comments");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getOtherComments());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Results of Research Expected");
    	cell = row.createCell(1);
    	cell.setCellValue((project.getEndMonth()+1)+"/"+project.getEndYear());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Stimulus Delivery (In-Market)");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getDeliveryMarketMonth()+"/"+projectInitiation.getDeliveryMarketYear());
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Stimulus Delivery (In-Agency)");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getDeliveryMarketMonth()+"/"+projectInitiation.getDeliveryMarketYear());
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Type of Stimulus");
    	cell = row.createCell(1);
    	StringBuilder stimulusType = new StringBuilder();
    	if(pibStimulus!=null)
    	{
    		if(pibStimulus.getActualStick())
    		{
    			stimulusType.append("Actual Stick");
    		}
    		if(pibStimulus.getActualPack())
    		{
    			stimulusType.append(stimulusType.length()>0?",Actual Pack":"Actual Pack");
    			
    		}
    		if(pibStimulus.getMockSticks())
    		{
    			stimulusType.append(stimulusType.length()>0?",Mock Up Sticks":"Mock Up Sticks");
    		}
    		if(pibStimulus.getMockPacks())
    		{
    			stimulusType.append(stimulusType.length()>0?",Mock Up Packs":"Mock Up Packs");
    		}
    		if(pibStimulus.getConceptBoards())
    		{
    			stimulusType.append(stimulusType.length()>0?",Concept Boards":"Concept Boards");
    		}
    		if(pibStimulus.getCommBoards())
    		{
    			stimulusType.append(stimulusType.length()>0?",Comm Boards":"Comm Boards");
    		}
    		if(pibStimulus.getPostMaterials())
    		{
    			stimulusType.append(stimulusType.length()>0?",POS Materials":"POS Materials");
    		}
    		if(pibStimulus.getDigitalVisuals())
    		{
    			stimulusType.append(stimulusType.length()>0?",Digital Visuals (Stills/Clips)":"Digital Visuals (Stills/Clips)");
    		}
    		cell.setCellValue(stimulusType.toString());	
    	}
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Reporting Requirement");
    	
    	if(pibReporting!=null)
    	{
    		cell = row.createCell(1);
    		cell.setCellValue("Topline Debrief");
    		cell = row.createCell(2);
    		cell.setCellValue(pibReporting.getTopDebrief()?"Yes":"No");
    		cell = row.createCell(3);
    		cell.setCellValue(pibReporting.getTopDebriefNumber());
    		cell = row.createCell(4);
    		cell.setCellValue(pibReporting.getTopDebriefLocation());
    		
    		row = sheet.createRow(++rownum);
    		cell = row.createCell(1);
    		cell.setCellValue("Full Debrief");
    		cell = row.createCell(2);
    		cell.setCellValue(pibReporting.getFullDebrief()?"Yes":"No");
    		cell = row.createCell(3);
    		cell.setCellValue(pibReporting.getFullDebriefNumber());
    		cell = row.createCell(4);
    		cell.setCellValue(pibReporting.getFullDebriefLocation());
    		
    		row = sheet.createRow(++rownum);
    		cell = row.createCell(1);
    		cell.setCellValue("Topline Report");
    		cell = row.createCell(2);
    		cell.setCellValue(pibReporting.getTopReport()?"Yes":"No");
    		cell = row.createCell(3);
    		cell.setCellValue(pibReporting.getTopReportNumber());
    		cell = row.createCell(4);
    		cell.setCellValue(pibReporting.getTopReportLocation());
    		
    		row = sheet.createRow(++rownum);
    		cell = row.createCell(1);
    		cell.setCellValue("Full Report");
    		cell = row.createCell(2);
    		cell.setCellValue(pibReporting.getFullreport()?"Yes":"No");
    		cell = row.createCell(3);
    		cell.setCellValue(pibReporting.getFullreportNumber());
    		cell = row.createCell(4);
    		cell.setCellValue(pibReporting.getFullreportLocation());
    		
    		row = sheet.createRow(++rownum);
    		cell = row.createCell(1);
    		cell.setCellValue("Data Tables");
    		cell = row.createCell(2);
    		cell.setCellValue(pibReporting.getDatatables()?"Yes":"No");
    		cell = row.createCell(3);
    		cell.setCellValue(pibReporting.getDatatablesNumber());
    		cell = row.createCell(4);
    		cell.setCellValue(pibReporting.getDatatablesLocation());
    		
    	}
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Budget Allocation");
    	cell = row.createCell(1);
    	cell.setCellValue("Budget Allocation");
    	
    	
    	row = sheet.createRow(++rownum);
    	cell = row.createCell(0);
    	cell.setCellValue("Payment Arrangements");
    	cell = row.createCell(1);
    	cell.setCellValue(projectInitiation.getPayArrangement());
    	
    	
    	
    	
    	
    	
    
	
	*/
		return pibExcel;	
	}
	public static HSSFWorkbook generateExchangeRateReport(List<ExchangeRateReportList> exchangeRateReportList)
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Exchange Rate Report");
		//sheet.setDisplayGridlines(false);
		HSSFCellStyle headerStyle = SynchroSheetStylingUtil.getHeadingStyle(workbook, "1111");
		HSSFCellStyle cellStyle = SynchroSheetStylingUtil.getCellStyle(workbook, "1111");
		int row_idx = 0;
		Row row = sheet.createRow(row_idx);
		Cell cell = row.createCell(1);
		cell.setCellValue("Figures shown below are conversions of 1 British Pound (GBP)");
		int size = exchangeRateReportList.size() + 2;
		Cell cell2;
		for(int col_idx=2; col_idx<=size; col_idx++)
    	{
    		cell2 = row.createCell(col_idx);
        	cell2.setCellStyle(SynchroSheetStylingUtil.getHeadingStyle(workbook, "1101"));
    	}
		
		sheet =  SynchroSheetStylingUtil.mergeCells(sheet, row_idx, row_idx, 1, size);
		cell.setCellStyle(headerStyle);
		row_idx++;
		row = sheet.createRow(row_idx);
		cell = row.createCell(1);
		cell.setCellValue("Currency Description");
		cell.setCellStyle(headerStyle);
		cell = row.createCell(2);
		cell.setCellValue("Currency Code");
		cell.setCellStyle(headerStyle);
		int col_idx = 3;
		
		//Dynamic loop for year columns
		for(ExchangeRateReportList exchangeRateReport: exchangeRateReportList)
		{
			cell = row.createCell(col_idx);
			cell.setCellValue(exchangeRateReport.getYear());
			cell.setCellStyle(headerStyle);
			col_idx++;
		}
		row_idx++;
		row = sheet.createRow(row_idx);
		
		//Data entry starts here
		for(Integer key :SynchroGlobal.getCurrencies().keySet())
		{
			row = sheet.createRow(row_idx);
			cell = row.createCell(1);
			cell.setCellValue(SynchroGlobal.getCurrencyDescriptions().get(key)==null?"":SynchroGlobal.getCurrencyDescriptions().get(key));
			cell.setCellStyle(cellStyle);
			cell = row.createCell(2);
			cell.setCellValue(SynchroGlobal.getCurrencies().get(key)==null?"":SynchroGlobal.getCurrencies().get(key));
			cell.setCellStyle(cellStyle);
			row_idx++;
		}
		
		// Yearly Data filling
		row_idx = 2;
		col_idx = 3;
		for(ExchangeRateReportList exchangeRateReport: exchangeRateReportList)
		{
			for(ExchangeRateReport exchangeRate : exchangeRateReport.getExchangeRateReportList())
			{
				cell = sheet.getRow(row_idx).createCell(col_idx);
				cell.setCellValue(exchangeRate.getExchangeRate()==null?"No Rate":exchangeRate.getExchangeRate().toString());
				cell.setCellStyle(cellStyle);
				row_idx++;
			}
			row_idx=2;
			col_idx++;
		}
		
		/**
		 * Auto re-sizing of column width based on content size 
		 */
		 sheet.setColumnWidth(0, 1000);
	  	for(int i=1; i<=col_idx ; i++)
	  	{
	  		sheet.autoSizeColumn(i);
	  		int width = sheet.getColumnWidth(i);
	  		if(width > 10000)
	  		{
	  			sheet.setColumnWidth(i, 10000);
	  		}
	  	}
	  	
		return workbook;
	}
	private static boolean validateEndMarketFilters(List<Long> endMarkets, Long eID)
	{
		if(endMarkets==null)
			return true;
		if(endMarkets.contains(eID))
			return true;
		else
			return false;
	}
}
