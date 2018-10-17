package com.grail.synchro.action.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EvaluationAgencyReportFiltersNew;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.SynchroReportManagerNew;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.util.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 12/5/16
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgencyEvaluationReportActionNew extends JiveActionSupport {
    private final Logger LOG = Logger.getLogger(AgencyEvaluationReportActionNew.class);
    private PermissionManager permissionManager;

    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    
    private SynchroReportManagerNew synchroReportManagerNew;
    private ProjectEvaluationManagerNew projectEvaluationManagerNew;
    private ProjectManagerNew synchroProjectManagerNew;
    
    private EvaluationAgencyReportFiltersNew evaluationAgencyReportFilter;
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    private int columnWidth = 8000;
    
    @Override
    public String execute() {
        if(getUser() != null) {
//            if(!SynchroPermHelper.hasGenerateReportAccess(getUser())) {
//                 return UNAUTHORIZED;
//            }
            if(SynchroPermHelper.isExternalAgencyUser(getUser()) || SynchroPermHelper.isCommunicationAgencyUser(getUser())) {
                return UNAUTHORIZED;
            }

            if(!(SynchroPermHelper.isSynchroMiniAdmin(getUser())
                    || SynchroPermHelper.isSynchroAdmin(getUser())
                    || SynchroPermHelper.isSynchroGlobalSuperUser(getUser())
            )) {
                return UNAUTHORIZED;
            }


        } else {
            return UNAUTHENTICATED;
        }
        return SUCCESS;
    }

    public String downloadReport() throws IOException {
      

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        LOG.info("Downloading Raw extract by " +getUser().getFirstName() + " " + getUser().getLastName() + " on " + new Date());
        Calendar calendar = Calendar.getInstance();
        
        String timeStamp = calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND);
        downloadFilename = "Agency Evaluation Extract_" +
        		timeStamp +
                ".xls";
        
        downloadStreamType = "application/vnd.ms-excel";
        
        List<Long> projectIds = synchroReportManagerNew.getAgencyEvaluationReport(getAgencyEvaluationFilter());
        
        StringBuilder report = new StringBuilder();

       
        
        
        
        
        StringBuilder generatedBy = new StringBuilder();
        generatedBy.append("Agency Evaluation Report").append("\n");
        String userName = getUser().getName();
        generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
        generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
        
        report.append(generatedBy.toString()).append("\n");
        
        StringBuilder header = new StringBuilder();
        header.append("KANTAR/NON-KANTAR").append(",");
        header.append("AGENCY").append(",");
        header.append("PROJECT CODE").append(",");
        header.append("PROJECT NAME").append(",");
        header.append("PROJECT START DATE").append(",");
        header.append("PROJECT END DATE").append(",");
        header.append("RESEARCH END MARKET(s)").append(",");
        header.append("CATEGORY").append(",");
        header.append("METHODOLOGY").append(",");
        header.append("BUDGET YEAR").append(",");
        header.append("EVALUATION DONE BY").append(",");
        header.append("DATE OF EVALUATION").append(",");
        header.append("COORDINATION/FIELDWORK").append(",");
        header.append("RATING").append(",");
        header.append("COMMENTS").append(",");
        

        report.append(header.toString()).append("\n");

        DecimalFormat currencyFormatter = new DecimalFormat("#,###0.00000000000000000000000000000000000");
        
        
      /*  if(projectIds != null && projectIds.size() > 0)
        {
        	for(Long projectID: projectIds)
        	{
        		Project project = synchroProjectManagerNew.get(projectID);
        		List<ProjectCostDetailsBean> projectCostDetailsList =  synchroProjectManagerNew.getProjectCostDetails(projectID);
        		for(ProjectCostDetailsBean bean: projectCostDetailsList)
        		{
	        		
	        		StringBuilder data = new StringBuilder();
		        	
		        	String researchAgencyGroup = " ";
		        	if(bean.getAgencyId()!=null && bean.getAgencyId().intValue() > 0)
		        	{
		        		Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(bean.getAgencyId().intValue());
		        		if(rGroupId!=null && rGroupId>0)
		        		{
		        			researchAgencyGroup = SynchroGlobal.getResearchAgencyGroup().get(rGroupId);
		        		}
		        	}
		        	data.append(researchAgencyGroup).append(",");	
		        	
		        	data.append(SynchroGlobal.getResearchAgency().get(bean.getAgencyId().intValue())).append(",");
		            data.append(SynchroUtils.generateProjectCode(projectID)).append(",");
		           
		            data.append(project.getName()).append(",");
		            if(project.getStartDate()!=null)
		            {	
		            	data.append(dateFormat.format(project.getStartDate())).append(",");
		            }
		            else
		            {
		            	data.append(" ").append(",");
		            }
		            if(project.getEndDate()!=null)
		            {	
		            	data.append(dateFormat.format(project.getEndDate())).append(",");
		            }
		            else
		            {
		            	data.append(" ").append(",");
		            }
		            List<Long> emIds = synchroProjectManagerNew.getEndMarketIDs(projectID);
		            List<String> endMktNames = new ArrayList<String>();
		            StringBuilder endMktName = new StringBuilder();
		            for(Long endMarket: emIds)
		            {
		            	//endMktName.append(SynchroGlobal.getEndMarkets().get(endMarket.intValue())).append("\\,");
		            	endMktNames.add(SynchroGlobal.getEndMarkets().get(endMarket.intValue()));
		            }
		            data.append("\"").append(Joiner.on(",").join(endMktNames)).append("\"").append(",");
		            
		            //StringBuilder categoryTypes = new StringBuilder();
		            List<String> categoryTypes = new ArrayList<String>();
		            List<Long> cTypes = project.getCategoryType();
		            for(Long cType: cTypes)
		            {
		            	//categoryTypes.append(SynchroGlobal.getProductTypes().get(cType.intValue())).append("\\,");
		            	categoryTypes.add(SynchroGlobal.getProductTypes().get(cType.intValue()));
		            }
		            
		            data.append("\"").append(Joiner.on(",").join(categoryTypes)).append("\"").append(",");
		            
		            
		         
		            List<String> methodologies = new ArrayList<String>();
		            List<Long> methDetails = project.getMethodologyDetails();
		            for(Long meth: methDetails)
		            {
		            	methodologies.add(SynchroGlobal.getMethodologies().get(meth.intValue()));
		            }
		            data.append("\"").append(Joiner.on(",").join(methodologies)).append("\"").append(",");
		            
		            data.append(project.getBudgetYear()).append(",");
		            
		            List<ProjectEvaluationInitiation> pEvalList = projectEvaluationManagerNew.getProjectEvaluationInitiationAgency(projectID,bean.getAgencyId());
		            if(pEvalList!=null && pEvalList.size()>0)
		            {
			            try
			            {
			            	String evaluationDoneBy = userManager.getUser(pEvalList.get(0).getModifiedBy()).getName();
			            	data.append(evaluationDoneBy).append(",");
				            Date evaluationDate = new Date(pEvalList.get(0).getModifiedDate());
			            	
			            	data.append(dateFormat.format(evaluationDate)).append(",");
			            }
			            catch(Exception e)
			            {
			            	data.append(" ").append(",");
			            	data.append(" ").append(",");
			            }
		            }
		            else
		            {
		            	data.append(" ").append(",");
		            	data.append(" ").append(",");
		            }
		            
		            if(bean.getCostComponent().intValue()==1)
		            {
		            	data.append("Coordination").append(",");
		            }
		            else
		            {
		            	data.append("Fieldwork").append(",");
		            }
		            
		            if(pEvalList!=null && pEvalList.size()>0)
		            {
		            	if(pEvalList.get(0).getAgencyRating()!=null)
		            	{
		            		data.append(SynchroGlobal.getAgencyRatings().get((pEvalList.get(0).getAgencyRating()))).append(",");
		            	}
		            	else
		            	{
		            		data.append(" ").append(",");
		            	}
		            	if(pEvalList.get(0).getAgencyRating()!=null)
		            	{
		            		data.append(pEvalList.get(0).getAgencyComments()).append(",");
		            	}
		            	else
		            	{
		            		data.append(" ").append(",");
		            	} 
		            }
		            else
		            {
		            	data.append(" ").append(",");
		            	data.append(" ").append(",");
		            }
		            report.append(data.toString()).append("\n");
        		}
        	}
        }
       */
        

       // downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Agency Evaluation Report");
        //sheet.addMergedRegion(new CellRangeAddress(4,0,0,14));
        sheet.addMergedRegion(new CellRangeAddress(0,3,0,14));
        sheet.addMergedRegion(new CellRangeAddress(4,7,0,14));
        
        int row_idx = 4;
    	int cellNum = 0;
    	Row row = sheet.createRow(row_idx);
    	
    	
    	Font font2 = workbook.createFont();
	    //((XSSFFont) font2).setBold(true);
    	((HSSFFont) font2).setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	    font2.setFontName("Calibri");
	    font2.setFontHeightInPoints((short)9);
	    
	    HSSFCellStyle generatedByCellStyle = workbook.createCellStyle();
	      
	      generatedByCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	      generatedByCellStyle.setFont(font2);
	      generatedByCellStyle.setWrapText(true);
	      generatedByCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	      
	      
    	Cell cell = row.createCell(cellNum);
    	cell.setCellValue(generatedBy.toString());
    	cell.setCellStyle(generatedByCellStyle);
    	
    	CellStyle headerStyle = workbook.createCellStyle();
    	
    	headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        
    	headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
    	headerStyle.setWrapText(true);
    	
    	
	      
    	headerStyle.setFont(font2);
    	headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    	headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	
    	//cell.setCellStyle(headerStyle);
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	row = sheet.createRow(8);
    	row.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
    	
    	
    	CellStyle headerCellStyle = getHeaderCellStyle(workbook);
    	CellStyle bodyCellStyle = getBodyCellStyle(workbook);
    	
    	cell = getHeaderCell(row, cellNum, "KANTAR/NON-KANTAR", workbook, sheet,headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "AGENCY", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "COORDINATION/FIELDWORK", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "RATING", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "EVALUATION DONE BY", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "DATE OF EVALUATION", workbook, sheet, headerCellStyle);
    	
    	cell = getHeaderCell(row, ++cellNum, "PROJECT CODE", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "PROJECT NAME", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "PROJECT START DATE", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "PROJECT END DATE", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "RESEARCH END MARKET(s)", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "CATEGORY", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "METHODOLOGY", workbook, sheet, headerCellStyle);
    	cell = getHeaderCell(row, ++cellNum, "BUDGET YEAR", workbook, sheet, headerCellStyle);
    	
    	cell = getHeaderCell(row, ++cellNum, "COMMENTS", workbook, sheet, headerCellStyle);
    	
    	
    	row_idx=9;
    	
    	
    	
    	
    	if(projectIds != null && projectIds.size() > 0)
        {
        	for(Long projectID: projectIds)
        	{
        		Project project = synchroProjectManagerNew.get(projectID);
        		List<ProjectCostDetailsBean> projectCostDetailsList =  synchroProjectManagerNew.getProjectCostDetails(projectID);
        		for(ProjectCostDetailsBean bean: projectCostDetailsList)
        		{
	        		
        			List<ProjectEvaluationInitiation> pEvalList = projectEvaluationManagerNew.getProjectEvaluationInitiationAgency(projectID,bean.getAgencyId());
        			
        			// This is done as only those Agency entries should appear for which the Agency rating is there.
        			if(pEvalList!=null && pEvalList.size() > 0 && pEvalList.get(0).getAgencyRating()!=null)
        			{
        			
	        			//http://redmine.nvish.com/redmine/issues/366 and http://redmine.nvish.com/redmine/issues/365
	        			if(evaluationAgencyReportFilter.getAeCostComponents()!=null && evaluationAgencyReportFilter.getAeCostComponents().size()>0 && 
	        					evaluationAgencyReportFilter.getAeResearchAgencies()!=null && evaluationAgencyReportFilter.getAeResearchAgencies().size()>0 )
	        			{
	        				boolean flag = false;
	        				for(int i =0; i<evaluationAgencyReportFilter.getAeResearchAgencies().size();i++)
	        				{
	        					if(evaluationAgencyReportFilter.getAeResearchAgencies().get(i)==bean.getAgencyId())
	        					{
	        						for(int j =0; j<evaluationAgencyReportFilter.getAeCostComponents().size();j++)
	                				{
	                					if(evaluationAgencyReportFilter.getAeCostComponents().get(j)==Long.valueOf(bean.getCostComponent()))
	                					{
	                						flag=true;
	                					}
	                				}
	        						//flag=true;
	        					}
	        				}
	            				
	        			
	        				if(flag)
	        				{
	        					
	        				}
	        				else
	        				{
	        					continue;
	        				}
	        			}
	        			else if(evaluationAgencyReportFilter.getAeResearchAgencies()!=null && evaluationAgencyReportFilter.getAeResearchAgencies().size()>0 )
	        			{
	        				boolean flag = false;
	        				for(int i =0; i<evaluationAgencyReportFilter.getAeResearchAgencies().size();i++)
	        				{
	        					if(evaluationAgencyReportFilter.getAeResearchAgencies().get(i)==bean.getAgencyId())
	        					{
	        						flag=true;
	        					}
	        				}
	        				
	        				if(flag)
	        				{
	        					
	        				}
	        				else
	        				{
	        					continue;
	        				}
	        			}
	        			else if(evaluationAgencyReportFilter.getAeCostComponents()!=null && evaluationAgencyReportFilter.getAeCostComponents().size()>0)
	        			{
	        				boolean flag = false;
	        				for(int i =0; i<evaluationAgencyReportFilter.getAeCostComponents().size();i++)
	        				{
	        					if(evaluationAgencyReportFilter.getAeCostComponents().get(i)==Long.valueOf(bean.getCostComponent()))
	        					{
	        						flag=true;
	        					}
	        				}
	        				
	        				if(flag)
	        				{
	        					
	        				}
	        				else
	        				{
	        					continue;
	        				}
	        			}
	        			row = sheet.createRow(row_idx);
	        			cellNum = 0;
	        			
		        		//StringBuilder data = new StringBuilder();
			        	
			        	String researchAgencyGroup = " ";
			        	if(bean.getAgencyId()!=null && bean.getAgencyId().intValue() > 0)
			        	{
			        		Integer rGroupId = SynchroGlobal.getResearchAgencyGroupFromAgency(bean.getAgencyId().intValue());
			        		if(rGroupId!=null && rGroupId>0)
			        		{
			        			researchAgencyGroup = SynchroGlobal.getResearchAgencyGroup().get(rGroupId);
			        		}
			        	}
			        	//data.append(researchAgencyGroup).append(",");	
			        	cell = getBodyCell(row, cellNum, researchAgencyGroup, workbook, bodyCellStyle);
			        	
			        	//data.append(SynchroGlobal.getResearchAgency().get(bean.getAgencyId().intValue())).append(",");
			        	//cell = getBodyCell(row, ++cellNum, SynchroGlobal.getResearchAgency().get(bean.getAgencyId().intValue()), workbook, bodyCellStyle);
			        	cell = getBodyCell(row, ++cellNum, SynchroGlobal.getAllResearchAgency().get(bean.getAgencyId().intValue()), workbook, bodyCellStyle);
			            
			        	//Coordination or Fieldwork 
			        	if(bean.getCostComponent().intValue()==1)
			            {
			            	//data.append("Coordination").append(",");
			            	cell = getBodyCell(row, ++cellNum, "Coordination", workbook, bodyCellStyle);
			            }
			            else
			            {
			            	//data.append("Fieldwork").append(",");
			            	cell = getBodyCell(row, ++cellNum, "Fieldwork", workbook, bodyCellStyle);
			            }
				        
			        	
			        	
			        	// Agency Rating
			        	if(pEvalList!=null && pEvalList.size()>0)
			            {
			            	if(pEvalList.get(0).getAgencyRating()!=null)
			            	{
			            		//data.append(SynchroGlobal.getAgencyRatings().get((pEvalList.get(0).getAgencyRating()))).append(",");
			            		cell = getBodyCell(row, ++cellNum, SynchroGlobal.getAgencyRatings().get((pEvalList.get(0).getAgencyRating())), workbook, bodyCellStyle);
			            	}
			            	else
			            	{
			            		//data.append(" ").append(",");
			            		cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            	}
			            	
			            }
			            else
			            {
			            	//data.append(" ").append(",");
			            	//data.append(" ").append(",");
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            	
			            }
			        	
			        	// Evaluation Done By and Date of Evaluation
			        	
			            if(pEvalList!=null && pEvalList.size()>0)
			            {
				            try
				            {
				            	String evaluationDoneBy = userManager.getUser(pEvalList.get(0).getModifiedBy()).getName();
				          //  	data.append(evaluationDoneBy).append(",");
				            	
				            	cell = getBodyCell(row, ++cellNum, evaluationDoneBy, workbook, bodyCellStyle);
				            	
					            Date evaluationDate = new Date(pEvalList.get(0).getModifiedDate());
				            	
				            //	data.append(dateFormat.format(evaluationDate)).append(",");
				            	
				            	cell = getBodyCell(row, ++cellNum, dateFormat.format(evaluationDate), workbook, bodyCellStyle);
				            	
				            }
				            catch(Exception e)
				            {
				            	//data.append(" ").append(",");
				            	//data.append(" ").append(",");
				            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
				            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
				            	
				            }
			            }
			            else
			            {
			            	//data.append(" ").append(",");
			            	//data.append(" ").append(",");
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            }
			        	 
			        	//data.append(SynchroUtils.generateProjectCode(projectID)).append(",");
			            cell = getBodyCellLong(row, ++cellNum, projectID, workbook, bodyCellStyle);
			           
			            //data.append(project.getName()).append(",");
			            cell = getBodyCell(row, ++cellNum, project.getName(), workbook, bodyCellStyle);
			            if(project.getStartDate()!=null)
			            {	
			            	//data.append(dateFormat.format(project.getStartDate())).append(",");
			            	cell = getBodyCell(row, ++cellNum, dateFormat.format(project.getStartDate()), workbook, bodyCellStyle);
			            }
			            else
			            {
			            	//data.append(" ").append(",");
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            }
			            if(project.getEndDate()!=null)
			            {	
			            	//data.append(dateFormat.format(project.getEndDate())).append(",");
			            	cell = getBodyCell(row, ++cellNum, dateFormat.format(project.getEndDate()), workbook, bodyCellStyle);
			            }
			            else
			            {
			            	//data.append(" ").append(",");
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            }
			            List<Long> emIds = synchroProjectManagerNew.getEndMarketIDs(projectID);
			            List<String> endMktNames = new ArrayList<String>();
			            StringBuilder endMktName = new StringBuilder();
			            try
			            {
				            for(Long endMarket: emIds)
				            {
				            	//endMktName.append(SynchroGlobal.getEndMarkets().get(endMarket.intValue())).append("\\,");
				            	/*String endMarketName = SynchroGlobal.getEndMarkets().get(endMarket.intValue());
				            	if(endMarketName!=null)
				            	{
				            		endMktNames.add(endMarketName);
				            	}
				            	*/
				            	String endMarketName = SynchroGlobal.getEndMarkets().get(endMarket.intValue());
		        				if(StringUtils.isNotBlank(endMarketName))
		        				{
		        					endMktNames.add(endMarketName);
		        				}
		        				else
		        				{
		        					// This is done for Regions
		        					endMarketName = SynchroGlobal.getRegions().get(endMarket.intValue());
		        					if(StringUtils.isNotBlank(endMarketName))
			        				{
		        						endMktNames.add(endMarketName);
			        				}
		        				}
				            }
				            //data.append("\"").append(Joiner.on(",").join(endMktNames)).append("\"").append(",");
				            
				            if(endMktNames!=null && endMktNames.size()>0)
				            {
				            	cell = getBodyCell(row, ++cellNum, Joiner.on(",").join(endMktNames), workbook, bodyCellStyle);
				            }
				            else
				            {
				            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
				            }
			            }
			            catch(Exception e)
			            {
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            }
			            
			            //StringBuilder categoryTypes = new StringBuilder();
			            List<String> categoryTypes = new ArrayList<String>();
			            List<Long> cTypes = project.getCategoryType();
			            
			            try
			            {
				            for(Long cType: cTypes)
				            {
				            	//categoryTypes.append(SynchroGlobal.getProductTypes().get(cType.intValue())).append("\\,");
				            	categoryTypes.add(SynchroGlobal.getProductTypes().get(cType.intValue()));
				            }
				            
				           // data.append("\"").append(Joiner.on(",").join(categoryTypes)).append("\"").append(",");
				            
				            if(categoryTypes!=null && categoryTypes.size()>0)
				            {
				            	cell = getBodyCell(row, ++cellNum, Joiner.on(",").join(categoryTypes), workbook, bodyCellStyle);
				            }
				            else
				            {
				            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
				            }
			            }
			            catch(Exception e)
			            {
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            }
			         
			            List<String> methodologies = new ArrayList<String>();
			            List<Long> methDetails = project.getMethodologyDetails();
			            
			            try
			            {
				            for(Long meth: methDetails)
				            {
				            	if(SynchroGlobal.getAllMethodologies().get(meth.intValue())!=null)
				            	{
				            		methodologies.add(SynchroGlobal.getAllMethodologies().get(meth.intValue()));
				            	}
				            }
				            //data.append("\"").append(Joiner.on(",").join(methodologies)).append("\"").append(",");
				            
				            if(methodologies!=null && methodologies.size()>0)
				            {
				            	
				            	cell = getBodyCell(row, ++cellNum, Joiner.on(",").join(methodologies), workbook, bodyCellStyle);
				            }
				            else
				            {
				            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
				            }
			            }
			            catch(Exception e)
			            {
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            }
			            
			            //data.append(project.getBudgetYear()).append(",");
			            cell = getBodyCellInt(row, ++cellNum, project.getBudgetYear(), workbook, bodyCellStyle);
			            
			            
			            
			           
			            // Agency Comment
			            if(pEvalList!=null && pEvalList.size()>0)
			            {
			            	
			            	if(pEvalList.get(0).getAgencyComments()!=null && !pEvalList.get(0).getAgencyComments().equalsIgnoreCase(""))
			            	{
			            		//data.append(pEvalList.get(0).getAgencyComments()).append(",");
			            		cell = getBodyCell(row, ++cellNum, pEvalList.get(0).getAgencyComments(), workbook, bodyCellStyle);
			            	}
			            	else
			            	{
			            		//data.append(" ").append(",");
			            		cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            	} 
			            }
			            else
			            {
			            	cell = getBodyCell(row, ++cellNum, "-", workbook, bodyCellStyle);
			            }
			           // report.append(data.toString()).append("\n");
			            row_idx++;
        			}    
        		}
        	}
        }
	   	
    	
    	//sheet.addMergedRegion(new CellRangeAddress(3,0,0,14));
    	//sheet.addMergedRegion(new CellRangeAddress(row_idx+3,row_idx+9,0,14));
    	row = sheet.createRow(row_idx+3);
    	
    	
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
    	Font callibiriFont = workbook.createFont();
		 
    	callibiriFont.setFontName("Calibri");
    	notesStyle.setFont(callibiriFont);
        
    	
    	CellRangeAddress mergedRegion = new CellRangeAddress(row_idx+3,row_idx+9,0,14);
    	
    	sheet.addMergedRegion(mergedRegion);
    	
    	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
        RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
        RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
        RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
    	
    	StringBuilder notes = new StringBuilder();
    	notes.append("Notes:").append("\n");
    	notes.append("- Cancelled projects are not included in the above report").append("\n");
    	notes.append("- Value of '–' in a field indicates that information is not available");
    	//cell = getSpecialCell(row, 0, notes.toString(), workbook);
    	Cell notesColumn = row.createCell(0);
     	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
     	notesColumn.setCellValue(notes.toString());
     	notesColumn.setCellStyle(notesStyle);
     	
    	workbook = SynchroUtils.createExcelImage(workbook, sheet);
    	
	   	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
        
        
      //Audit Logs
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
				0, "Agency Evaluation - Download Report", "", -1L, getUser().getID());
        
        return DOWNLOAD_REPORT;
    }

    private Cell getHeaderCell(Row row, int cellNum, String value, HSSFWorkbook workbook, HSSFSheet sheet, CellStyle cellStyle)
    {
    	Cell cell = row.createCell(cellNum);
    	
    	cell.setCellValue(value);
    /*	CellStyle cellStyle = workbook.createCellStyle();
    	
    	cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        
    	cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
    	//cellStyle.setWrapText(true);
    	Font font2 = workbook.createFont();
	    //((XSSFFont) font2).setBold(true);
    	((HSSFFont) font2).setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	    font2.setFontName("Calibri");
	    font2.setFontHeightInPoints((short)9);
    	cellStyle.setFont(font2);
    	cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
    	cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
    	cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
    	cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
    	cell.setCellStyle(cellStyle);*/
    	cell.setCellStyle(cellStyle);
    	sheet.autoSizeColumn(cellNum);
    	sheet.setColumnWidth(cellNum, columnWidth);
    	return cell;
    }
    
    private Cell getBodyCell(Row row, int cellNum, String value, HSSFWorkbook workbook, CellStyle cellStyle)
    {
    	Cell cell = row.createCell(cellNum);
    	
    	cell.setCellValue(value);
    	try
    	{
	    	/*CellStyle cellStyle = workbook.createCellStyle();
	    	
	    	cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	        
	    	cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    	cellStyle.setWrapText(true);
	    	Font font2 = workbook.createFont();
		   // ((XSSFFont) font2).setBold(true);
		    font2.setFontName("Calibri");
		    font2.setFontHeightInPoints((short)9);
	    	cellStyle.setFont(font2);
	    	cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);*/
	    	cell.setCellStyle(cellStyle);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return cell;
    }
    
    private Cell getBodyCellInt(Row row, int cellNum, int value, HSSFWorkbook workbook, CellStyle cellStyle)
    {
    	Cell cell = row.createCell(cellNum);
    	
    	cell.setCellValue(value);
    	try
    	{
	    	/*CellStyle cellStyle = workbook.createCellStyle();
	    	
	    	cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	        
	    	cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    	cellStyle.setWrapText(true);
	    	Font font2 = workbook.createFont();
		   // ((XSSFFont) font2).setBold(true);
		    font2.setFontName("Calibri");
		    font2.setFontHeightInPoints((short)9);
	    	cellStyle.setFont(font2);
	    	cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);*/
	    	cell.setCellStyle(cellStyle);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return cell;
    }
    
    private Cell getBodyCellLong(Row row, int cellNum, long value, HSSFWorkbook workbook, CellStyle cellStyle)
    {
    	Cell cell = row.createCell(cellNum);
    	
    	cell.setCellValue(value);
    	try
    	{
	    	/*CellStyle cellStyle = workbook.createCellStyle();
	    	
	    	cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	        
	    	cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	    	cellStyle.setWrapText(true);
	    	Font font2 = workbook.createFont();
		   // ((XSSFFont) font2).setBold(true);
		    font2.setFontName("Calibri");
		    font2.setFontHeightInPoints((short)9);
	    	cellStyle.setFont(font2);
	    	cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);*/
	    	cell.setCellStyle(cellStyle);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return cell;
    }
    
    private CellStyle getBodyCellStyle(HSSFWorkbook workbook)
    {
    	CellStyle cellStyle = workbook.createCellStyle();
    	
    	cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        
    	cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
    	cellStyle.setWrapText(true);
    	Font font2 = workbook.createFont();
	   // ((XSSFFont) font2).setBold(true);
	    font2.setFontName("Calibri");
	    font2.setFontHeightInPoints((short)9);
    	cellStyle.setFont(font2);
    	cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    	cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
    	cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
    	cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    	return cellStyle;
    }
    
    private CellStyle getHeaderCellStyle(HSSFWorkbook workbook)
    {
    	CellStyle cellStyle = workbook.createCellStyle();
    	cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        
    	cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
    	//cellStyle.setWrapText(true);
    	Font font2 = workbook.createFont();
	    //((XSSFFont) font2).setBold(true);
    	((HSSFFont) font2).setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	    font2.setFontName("Calibri");
	    font2.setFontHeightInPoints((short)9);
    	cellStyle.setFont(font2);
    	cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
    	cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
    	cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
    	cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
    	
    	cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    	cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    	
    	return cellStyle;
    }
    
    
    private Cell getSpecialCell(Row row, int cellNum, String value, HSSFWorkbook workbook)
    {
    	Cell cell = row.createCell(cellNum);
    	
    	cell.setCellValue(value);
    	try
    	{
	    	CellStyle cellStyle = workbook.createCellStyle();
	    	
	    	cellStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	    	cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    	
	    	cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	        
	    	cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
	    	cellStyle.setWrapText(true);
	    	Font font2 = workbook.createFont();
		   // ((XSSFFont) font2).setBold(true);
		    font2.setFontName("Calibri");
		    font2.setFontHeightInPoints((short)9);
	    	cellStyle.setFont(font2);
	    	cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
	    	cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	    	cell.setCellStyle(cellStyle);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return cell;
    }
    private EvaluationAgencyReportFiltersNew getAgencyEvaluationFilter() {
    	evaluationAgencyReportFilter = new EvaluationAgencyReportFiltersNew();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(evaluationAgencyReportFilter);
        binder.bind(getRequest());

       
        if(StringUtils.isNotBlank(getRequest().getParameter("aeStartDateBegin"))) {
            this.evaluationAgencyReportFilter.setAeStartDateBegin(DateUtils.parse(getRequest().getParameter("aeStartDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("aeStartDateComplete"))) {
            this.evaluationAgencyReportFilter.setAeStartDateComplete(DateUtils.parse(getRequest().getParameter("aeStartDateComplete")));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("aeEndDateBegin"))) {
            this.evaluationAgencyReportFilter.setAeEndDateBegin(DateUtils.parse(getRequest().getParameter("aeEndDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("aeEndDateComplete"))) {
            this.evaluationAgencyReportFilter.setAeEndDateComplete(DateUtils.parse(getRequest().getParameter("aeEndDateComplete")));
        }
      
     /*   if(evaluationAgencyReportFilter.getAeStartDateBegin()!=null || evaluationAgencyReportFilter.getAeStartDateComplete()!=null 
        		|| evaluationAgencyReportFilter.getAeEndDateBegin()!=null || evaluationAgencyReportFilter.getAeEndDateComplete()!=null)
        {
        	selectedFilters.add("Project Duration");
        }
       */ 
        if(StringUtils.isNotBlank(getRequest().getParameter("aeStartDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("aeStartDateComplete")) ) 
        {
        	//selectedFilters.add("Project Started Between - ");
        	if(StringUtils.isNotBlank(getRequest().getParameter("aeStartDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("aeStartDateComplete")))
        		{
        			selectedFilters.add("Project Started Between - " + getRequest().getParameter("aeStartDateBegin") + " and "+getRequest().getParameter("aeStartDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Project Started Between - " +getRequest().getParameter("aeStartDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Project Started Between - " +getRequest().getParameter("aeStartDateComplete"));
        	}
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("aeEndDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("aeEndDateComplete")) ) 
        {
        	//selectedFilters.add("Project Completed Between - ");
        	if(StringUtils.isNotBlank(getRequest().getParameter("aeEndDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("aeEndDateComplete")))
        		{
        			selectedFilters.add("Project Completed Between - " + getRequest().getParameter("aeEndDateBegin") + " and "+getRequest().getParameter("aeEndDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Project Completed Between - " + getRequest().getParameter("aeEndDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Project Completed Between - " + getRequest().getParameter("aeEndDateComplete"));
        	}
        }
        
        if(StringUtils.isNotBlank(evaluationAgencyReportFilter.getEvaluationDoneBy()))
        {
        	selectedFilters.add("Evaluation Done By - "+ evaluationAgencyReportFilter.getEvaluationDoneBy());
        }
        
        if(evaluationAgencyReportFilter.getAeResearchAgencies()!=null && evaluationAgencyReportFilter.getAeResearchAgencies().size()>0)
        {
        	selectedFilters.add("Agency - " + SynchroUtils.getAgencyNames(evaluationAgencyReportFilter.getAeResearchAgencies()));
        }
       
        if(evaluationAgencyReportFilter.getAeCostComponents()!=null && evaluationAgencyReportFilter.getAeCostComponents().size()>0)
        {
        	selectedFilters.add("Cost Component - "+ SynchroUtils.getCostComponentName(evaluationAgencyReportFilter.getAeCostComponents()));
        }
        if(evaluationAgencyReportFilter.getAeResearchEndMarkets()!=null && evaluationAgencyReportFilter.getAeResearchEndMarkets().size()>0)
        {
        	selectedFilters.add("Research End Market(s) - " + SynchroUtils.getEndMarketNames(evaluationAgencyReportFilter.getAeResearchEndMarkets()));
        }
        
        if(evaluationAgencyReportFilter.getAeMethDetails()!=null && evaluationAgencyReportFilter.getAeMethDetails().size()>0)
        {
        	selectedFilters.add("Methodology - "+ SynchroUtils.getMethodologyNames(evaluationAgencyReportFilter.getAeMethDetails()));
        }
        
        if(evaluationAgencyReportFilter.getAeMethodologyTypes()!=null && evaluationAgencyReportFilter.getAeMethodologyTypes().size()>0)
        {
        	selectedFilters.add("Methodology Type - "+ SynchroUtils.getMethodologyTypeNames(evaluationAgencyReportFilter.getAeMethodologyTypes()));
        }
        
        if(evaluationAgencyReportFilter.getAeBudgetLocations()!=null && evaluationAgencyReportFilter.getAeBudgetLocations().size()>0)
        {
        	selectedFilters.add("Budget Location - " + SynchroUtils.getBudgetLocationNames(evaluationAgencyReportFilter.getAeBudgetLocations()));
        }
        if(evaluationAgencyReportFilter.getAeBudgetYears()!=null && evaluationAgencyReportFilter.getAeBudgetYears().size()>0)
        {
        	selectedFilters.add("Budget Year - " + StringUtils.join(evaluationAgencyReportFilter.getAeBudgetYears(), ","));
        }
        return evaluationAgencyReportFilter;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
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

	public SynchroReportManagerNew getSynchroReportManagerNew() {
		return synchroReportManagerNew;
	}

	public void setSynchroReportManagerNew(
			SynchroReportManagerNew synchroReportManagerNew) {
		this.synchroReportManagerNew = synchroReportManagerNew;
	}

	public ProjectEvaluationManagerNew getProjectEvaluationManagerNew() {
		return projectEvaluationManagerNew;
	}

	public void setProjectEvaluationManagerNew(
			ProjectEvaluationManagerNew projectEvaluationManagerNew) {
		this.projectEvaluationManagerNew = projectEvaluationManagerNew;
	}

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public void setSynchroProjectManagerNew(
			ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}

	public String getDownloadStreamType() {
		return downloadStreamType;
	}

	public void setDownloadStreamType(String downloadStreamType) {
		this.downloadStreamType = downloadStreamType;
	}


}
