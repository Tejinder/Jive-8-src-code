package com.grail.synchro.action.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.RegionUtil;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.CurrencyExchangeRate;
import com.grail.synchro.beans.SpendByReportBean;
import com.grail.synchro.beans.SpendReportExtractFilter;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.QPRSnapshotManager;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.opensymphony.xwork2.Preparable;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 12/6/16
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpendReportsActionNew extends JiveActionSupport implements Preparable {

    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename = "Spend Reports.xls";

    private String downloadStreamType = "application/vnd.ms-excel";

    private final String REPORT_TYPE_SELECTION_ERROR = "reportTypeSelectionError";

    private SynchroReportManager synchroReportManager;
    private QPRSnapshotManager qprSnapshotManager;
    private ProjectManagerNew synchroProjectManagerNew;

    private String reportTypes;
    private String years;
    private String regions;
    private String countries;
    private String marketTypes;
    private Integer currencyId;

    private Integer defaultCurrency;
    
    private String spendForSnapshot;
    private String timeStamp;
    
    private String budgetLocationsFilter;
    private String methDetailsFilter;
    private String brandsFilter;
    
    private String generateCrossTab;
    
    private List<BigDecimal> totalCosts=new ArrayList<BigDecimal>();
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    private List<String> selectedCrossTabSpends = new ArrayList<String>(); 
    
    private int columnWidth = 8000;
   
    private int bannerColumnWidth = 12000;

    @Override
    public void prepare() throws Exception {
        if(getUser() != null) {
            defaultCurrency = findDefaultCurrency(getUser());
        }
    }

    @Override
    public String execute() {
        
        return SUCCESS;
    }



    public String downloadReport() throws UnsupportedEncodingException, IOException {
        if(getUser() != null) {


            if(!(SynchroPermHelper.isSynchroUser(getUser())))
            {
                return UNAUTHORIZED;
            }
        } else {
            return UNAUTHENTICATED;
        }
        if(reportTypes == null || reportTypes.equals("")) {
            return REPORT_TYPE_SELECTION_ERROR;
        } else {
            //HSSFWorkbook workbook = new HSSFWorkbook();

            //downloadFilename = "Spend_Reports.xls";
            //generateReport(workbook);
        	System.out.println("Checking budgetLocationsFilter==>"+ budgetLocationsFilter);
        	System.out.println("Checking methDetailsFilter==>"+ methDetailsFilter);
        	System.out.println("Checking brandsFilter==>"+ brandsFilter);
        	
        	System.out.println("Checking generateCrossTab==>"+ generateCrossTab);
            processReport();
            
          //Audit Logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
    				0, "Spend Reports - Download Report", "", -1L, getUser().getID());
            
            return DOWNLOAD_REPORT;
        }
    }

    private void processReport() throws IOException {
        HSSFWorkbook workbook = null;
        SpendReportExtractFilter filter = getSpendReportFilter();
        if(years != null && !years.equals("")) {
            String [] yearsArr = years.split(",");
            List<Integer> yearsFilter = null;
            
            yearsFilter = new ArrayList<Integer>();
            yearsFilter.add(Integer.parseInt(yearsArr[0]));
            filter.setYears(yearsFilter);
            workbook = new HSSFWorkbook();
            generateDownloadFileName(yearsArr[0], false);
            if(generateCrossTab!=null && generateCrossTab.equals("Yes"))
            {
            	workbook = generateCrossTabReport(workbook, filter);
            }
            else
            {
            	workbook = generateReport(workbook, filter);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            downloadStream = new ByteArrayInputStream(baos.toByteArray());
            /*if(yearsArr.length == 1) {
                yearsFilter = new ArrayList<Integer>();
                yearsFilter.add(Integer.parseInt(yearsArr[0]));
                filter.setYears(yearsFilter);
                workbook = new HSSFWorkbook();
                generateDownloadFileName(yearsArr[0], false);
                workbook = generateReport(workbook, filter);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                downloadStream = new ByteArrayInputStream(baos.toByteArray());
            } else {
                generateDownloadFileName(null, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos);

                for(String yrStr :yearsArr) {
                    yearsFilter = new ArrayList<Integer>();
                    yearsFilter.add(Integer.parseInt(yrStr));
                    filter.setYears(yearsFilter);
                    workbook = new HSSFWorkbook();

                    workbook = generateReport(workbook, filter);
                    ZipEntry zipEntry = new ZipEntry("Year_"+yrStr+".xls");
                    zos.putNextEntry(zipEntry);
                    workbook.write(zos);
                    zos.closeEntry();
                }
                zos.close();
                downloadStream = new ByteArrayInputStream(baos.toByteArray());
            }*/
        }  else {
            generateDownloadFileName(null, false);
            workbook = new HSSFWorkbook();
           // workbook = generateReport(workbook, filter);
            if(generateCrossTab!=null && generateCrossTab.equals("Yes"))
            {
            	workbook = generateCrossTabReport(workbook, filter);
            }
            else
            {
            	workbook = generateReport(workbook, filter);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            downloadStream = new ByteArrayInputStream(baos.toByteArray());

        }
    }

    private void generateDownloadFileName(final String year, final boolean multipleFiles) {
        String fileName = "Spend Reports.xls";
        
        
        Calendar calendar = Calendar.getInstance();
        timeStamp = calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
        
       /* if(reportTypes != null && !reportTypes.equals("")) {
            String [] reportTypesStrArr = reportTypes.split(",");
            if(reportTypesStrArr.length == 1) {
                fileName = SynchroGlobal.SpendReportTypeNew.getById(Integer.parseInt(reportTypesStrArr[0])).getName();
            } else {

                fileName = "Spend Reports";
            }

            Calendar calendar = Calendar.getInstance();
            timeStamp = calendar.get(Calendar.YEAR) +
                    "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                    "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                    "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                    "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                    "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
            
            fileName = fileName + "_" + (year  != null?year+"_":"") +
                     timeStamp +
                    (multipleFiles?".zip":".xls");
           
            
            
        } else {

        }*/

        if(multipleFiles) {
            downloadStreamType = "application/vnd.ms-excel";
        } else {
            downloadStreamType = "application/vnd.ms-excel";
        }

        downloadFilename = fileName;
    }

    private HSSFWorkbook generateReport( HSSFWorkbook workbook, final SpendReportExtractFilter filter) throws UnsupportedEncodingException, IOException{
        
    	List<Long> snapShotIds = new ArrayList<Long>();
    	
    	Map<Integer, Map<Integer, Long>> snapShotMap = new HashMap<Integer, Map<Integer, Long>>();
    	if(StringUtils.isNotBlank(spendForSnapshot))
    	{
    		if(StringUtils.isNotBlank(years))
    		{
    			String[] spendForSnapshotArr = spendForSnapshot.split(",");
    			String[] budgetYearsArr = years.split(",");
    			for(int i=0;i<budgetYearsArr.length;i++)
    			{
    				HashMap<Integer, Long> sMap = new HashMap<Integer, Long>();
    				for(int j=0;j<spendForSnapshotArr.length;j++)
    				{
    					Long snapShotId = qprSnapshotManager.getSnapShotId(new Integer(spendForSnapshotArr[j]), new Integer(budgetYearsArr[i]));
    					if(snapShotId!=null && snapShotId.intValue() > 0)
    					{
    						snapShotIds.add(snapShotId);
    						
    						sMap.put(new Integer(spendForSnapshotArr[j]), snapShotId);
    					}
    					
    				}
    				snapShotMap.put(new Integer(budgetYearsArr[i]),sMap );
    			}
    		}
    	}
    	
    	
    	if(reportTypes != null && !reportTypes.equals("")) {


            String [] reportTypesStrArr = reportTypes.split(",");
            CurrencyExchangeRate currencyExchangeRate = getUserCurrencyExchangeRate(getUser());
            Integer startColumn = 0;
            Integer startRow = 0;
            boolean showProjectCodeColumn = false;


            if(reportTypesStrArr != null && reportTypesStrArr.length > 0) {

                HSSFDataFormat df = workbook.createDataFormat();
                short currencyDataFormatIndex = df.getFormat("#,###0.0000");

           

                // Header Styles start
                HSSFFont sheetHeaderFont = workbook.createFont();
                sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                sheetHeaderFont.setFontName("Calibri");

                HSSFFont notesFont = workbook.createFont();
                notesFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                notesFont.setFontName("Calibri");
                notesFont.setItalic(true);
                
                HSSFFont callibiriFont = workbook.createFont();
                callibiriFont.setFontName("Calibri");

                
                
                HSSFFont italicFont = workbook.createFont();
                italicFont.setItalic(true);

                HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
                sheetHeaderCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                sheetHeaderCellStyle.setFont(sheetHeaderFont);
                sheetHeaderCellStyle.setWrapText(true);
                sheetHeaderCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                // Header Styles end


                // Table Header Column Styles start

                // Table header column1 style
                HSSFCellStyle tableHeaderProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                tableHeaderProjectCodeColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle tableHeaderProjectNameColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                tableHeaderProjectNameColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column1 style
                HSSFCellStyle tableHeaderColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn1Style.setWrapText(true);
                tableHeaderColumn1Style.setFont(sheetHeaderFont);
                tableHeaderColumn1Style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderColumn1Style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            	
                
                
                // Table header column2 style
                HSSFCellStyle tableHeaderColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn2Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn2Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn2Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn2Style.setWrapText(true);
                tableHeaderColumn2Style.setFont(sheetHeaderFont);
                tableHeaderColumn2Style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderColumn2Style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                

                // Table header column3 style
                HSSFCellStyle tableHeaderColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn3Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn3Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn3Style.setWrapText(true);
                tableHeaderColumn3Style.setFont(sheetHeaderFont);
                tableHeaderColumn3Style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderColumn3Style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn4Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn5Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                // Table Header Column Styles end

                // Data Row cell styles start
                HSSFCellStyle projectCodeDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THICK);
                projectCodeDataRowColumnStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);

                HSSFCellStyle projectNameDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THIN);
                projectNameDataRowColumnStyle.setWrapText(true);
                projectNameDataRowColumnStyle.setFont(callibiriFont);

                HSSFCellStyle dataRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THICK);
                dataRowColumn1Style.setWrapText(true);
                dataRowColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                dataRowColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);
                dataRowColumn1Style.setFont(callibiriFont);

                HSSFCellStyle dataRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                
                dataRowColumn2Style.setWrapText(true);
                dataRowColumn2Style.setFont(callibiriFont);
                dataRowColumn2Style.setAlignment(CellStyle.ALIGN_CENTER);


                HSSFCellStyle dataRowColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN);
                dataRowColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);
                dataRowColumn3Style.setFont(callibiriFont);

                HSSFCellStyle dataRowColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle dataRowColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);
                // Data Row cell styles end

                // Total cost row styles start
                HSSFCellStyle totalCostRowProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                totalCostRowProjectCodeColumnStyle.setWrapText(true);
                totalCostRowProjectCodeColumnStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                totalCostRowProjectCodeColumnStyle.setAlignment(CellStyle.ALIGN_CENTER);
                totalCostRowProjectCodeColumnStyle.setFont(sheetHeaderFont);
                
                HSSFDataFormat cf = workbook.createDataFormat();
                //short currencyDataFormatIndex = cf.getFormat("#,###0.0000");
     	       //short currencyDataFormatIndexString = cf.getFormat("@");
                short currencyDataFormatIndexString = cf.getFormat("#,###0");
                
                HSSFCellStyle costFormatStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                costFormatStyle.setDataFormat(currencyDataFormatIndexString);
                
                costFormatStyle.setWrapText(true);
                costFormatStyle.setFont(callibiriFont);

                short currencyDecimalDataFormatIndexString = cf.getFormat("#,###0.00");
                
                HSSFCellStyle costDecimalFormatStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                costDecimalFormatStyle.setDataFormat(currencyDecimalDataFormatIndexString);
                
                costDecimalFormatStyle.setWrapText(true);
                costDecimalFormatStyle.setFont(callibiriFont);
               

                HSSFCellStyle totalCostRowTotalColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                totalCostRowTotalColumnStyle.setWrapText(true);
                totalCostRowTotalColumnStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                totalCostRowTotalColumnStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                totalCostRowTotalColumnStyle.setFont(sheetHeaderFont);
                totalCostRowTotalColumnStyle.setDataFormat(currencyDataFormatIndexString);
                
                
                HSSFCellStyle totalCostRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                totalCostRowColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                totalCostRowColumn1Style.setFont(callibiriFont);

                HSSFCellStyle totalCostRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                
                HSSFCellStyle notesStyle =  getCellStyle(workbook, HSSFCellStyle.BORDER_THIN,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN);
                notesStyle.setWrapText(true);
                notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
                notesStyle.setFont(callibiriFont);

                notesStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	            notesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    	
	            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		        
	            notesStyle.setAlignment(CellStyle.ALIGN_LEFT);
		    	notesStyle.setWrapText(true);
                
               

           
                
                
                if(filter.getBudgetLocations()!=null && filter.getBudgetLocations().size()>0)
                {
                	selectedFilters.add("Budget Location - "+ SynchroUtils.getBudgetLocationNames(filter.getBudgetLocations()));
                }
                if(filter.getMethDetails()!=null && filter.getMethDetails().size()>0)
                {
                	selectedFilters.add("Methodology - "+ SynchroDAOUtil.getMethodologyNames(StringUtils.join(filter.getMethDetails(),",")));
                }

                if(filter.getBrands()!=null && filter.getBrands().size()>0)
                {
                	selectedFilters.add("Branded/Non-Branded - "+ SynchroUtils.getBrandNames(filter.getBrands()));
                }

                
                
                // Total cost row styles end
      
                for(String reportTypeStr : reportTypesStrArr) {
                    Integer reportType = Integer.parseInt(reportTypeStr);
                    startRow = 4;
                    startColumn = 0;
                    
                    // Create sheet for each report type
                   // HSSFSheet sheet = workbook.createSheet(toSheetName(SynchroGlobal.SpendReportTypeNew.getById(reportType).getName()).replaceAll("/","or"));
                    HSSFSheet sheet = workbook.createSheet(SynchroGlobal.SpendReportTypeNew.getById(reportType).getName().replaceAll("/","or"));

                    
                    StringBuilder generatedBy = new StringBuilder();
                    generatedBy.append("\n").append("Spend Reports By ").append(SynchroGlobal.SpendReportTypeNew.getById(reportType).getDescription()).
                    append(" ( ").append(currencyExchangeRate.getCurrencyCode()).append(" )").append("\n");
                    String userName = getUser().getName();
                    generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
                   // generatedBy.append("\"").append("Filters:").append("\"").append("\n");
                    
                    

                    generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
                    
                    // Create sheet row1(Report Type Header)
                    HSSFRow reportTypeHeader = sheet.createRow(startRow);
                    HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(startColumn);
                    reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
                    reportTypeHeaderColumn.setCellValue(generatedBy.toString());

                    ++startRow;
                    
                    // Create sheet row2
                    HSSFRow userNameHeader = sheet.createRow(startRow + 1);
                    HSSFCell userNameHeaderColumn = userNameHeader.createCell(startColumn);
                    userNameHeaderColumn.setCellStyle(sheetHeaderCellStyle);
                  //  userNameHeaderColumn.setCellValue("User: " + getUser().getFirstName() + " " + getUser().getLastName());

                    Integer filtersRowCount = 0;

                    if(filter != null && filter.getYears() != null && filter.getYears().size() > 0) {
                        HSSFRow yearsFilterRow = sheet.createRow(startRow + 2);
                        HSSFCell yearsFilterRowCell = yearsFilterRow.createCell(startColumn);
                        yearsFilterRowCell.setCellStyle(sheetHeaderCellStyle);
                    //    yearsFilterRowCell.setCellValue("Year(s): "+StringUtils.join(filter.getYears(),", "));
                        filtersRowCount++;
                    }

             
                    // Spend By Projects
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getId())) 
                    {
                  
                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                      
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                       // String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        
                        
                        int cellIndex = 3;
                        
                       // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),3,5));
                       // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),6,9));
                        
                        int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 3 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 3 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                    	
                    	
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    
                    	totalCosts=new ArrayList<BigDecimal>();
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                     /*   for(int j=0;j<(spendForSnapshotArr.length);j++)
	                        {
	                        	int emptyColum = cellIndex;
	                        	budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(++emptyColum);
		                        budgetYearColumn0.setCellValue(" ");
		                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                        }*/
	                        
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                       /* if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
	                       */
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
		                    
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	                    HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Project Code");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Project Name");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn2.setCellValue("Budget Location");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                    sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost = false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        isQPR1SpendFor=false;
	                        isQPR2SpendFor=false;
	                        isQPR3SpendFor=false;
	                        isFullYearSpendFor=false;
	                        isCOPLASpendFor=false;
	                        isLatestCost=false;
	                        
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
	                    	
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
		                        costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        	/*
                        	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByProjects(new Integer(budgetYearsArr[i]), currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                      	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor, isLatestCost);
                      	    		*/	
                        	
                        }
                     
	                    
	                    	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByProjects(new Integer(budgetYearsArr[i]), currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    	Map<String,List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByProjects(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	if(spendByReportMap!=null)
                        	{
                        		System.out.println("SIZE PROJECT SPEND BY LIST==>"+ spendByReportMap.size());
                        	}
                        	
                        	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
	                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
                        	// This is done in case there are no records for the budget location with which the user is associated with 
                        	else
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
                        			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
                        
                        	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		
                        		HSSFRow dataRow = null;
                      			HSSFCell projectCodeDataRowColumn = null;
                      			
                      			for(String  spendByReportKey :spendByReportMap.keySet() )
                          		{
                      				Long projectId = new Long(spendByReportKey.split("~")[0]);
                      				String projectName = spendByReportKey.split("~")[1];
                      				String budgetLocation = spendByReportKey.split("~")[2];
                      				
                      				dataStartColumn = 0;
                        			dataRow = sheet.createRow(++startRow);
                        			
                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                                if(projectId!=null)
	                                {
	                                	projectCodeDataRowColumn.setCellValue(projectId);
	                                }
	                                else
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(" ");
	                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                             projectCodeDataRowColumn.setCellValue("-");
	    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                }
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                
	                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                                if(projectName!=null)
	                                {
	                                	projectCodeDataRowColumn.setCellValue(projectName);
	                                }
	                                else
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(" ");
	                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                             projectCodeDataRowColumn.setCellValue("-");
	    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                }
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                
	                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                                if(budgetLocation!=null)
	                                {
	                                	projectCodeDataRowColumn.setCellValue(SynchroUtils.getBudgetLocationName(new Integer(budgetLocation)));
	                                }
	                                else
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(" ");
	                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                             projectCodeDataRowColumn.setCellValue("-");
	    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                }
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                
	                                int noOfColumnsToDecorate = 0;
	                                if(isLatestCost)
	                                {
	                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
	                                }
	                                else
	                                {
	                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
	                                }
	                                
	                                int decCol = dataStartColumn;
                                	for(int k=0;k<noOfColumnsToDecorate;k++)
                                	{
                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
                                		sheet.setColumnWidth(decCol, columnWidth);
    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
    	                                projectCodeDataRowColumn.setCellValue("-");
    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
    	                                decCol++;
                                	}
                                	
                                	
                                	int snapShotRef = 0;
                                	
                      				for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
                      				{
                        			// This is done as for handing "-" and "NA" scenarions. In case there is no data for a Snapshot then there will be no entry in the excel.
                        			if(projectId!=null && projectId > 0)
                        			{
                        			
	                        		
	                                	
		                                if(isCOPLASpendFor)
		                                {
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(spendByBean.getCoplaTotalCost()!=null)
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
			                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
			                                }
			                                else
			                                {
			                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                	
			                                	/*if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
			                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                		
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}*/
			                                	
			                                	
			                                }
			                                
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                dataStartColumn++;
			                                snapShotRef++;
		                                }
		                                
		                                if(isQPR1SpendFor)
		                                {
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                               // projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost()+"");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(spendByBean.getQpr1TotalCost()!=null)
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
			                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue("-");
			                                	/*if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
			                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                		
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}*/
			                                	
			                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
			                                	{
			                                		/*Long qpr1SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("2"), spendByBean.getBudgetYear());
			                                		
			                                		// This logic is added for displaying "NA" only when the QPR is not frozen and otherwise -
			                                		if(qpr1SnapShotId!=null && qpr1SnapShotId.intValue() > 0)
			                                		{
			                                			projectCodeDataRowColumn.setCellValue("-");
				                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                		}
			                                		else
			                                		{
			                                			projectCodeDataRowColumn.setCellValue("NA");
			                                		}*/
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                }
			                              
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                dataStartColumn++;
			                                snapShotRef++;
		                                }
		                                if(isQPR2SpendFor)
		                                {
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                //projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost()+"");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(spendByBean.getQpr2TotalCost()!=null)
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
			                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue("-");
			                                /*	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
			                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	} */
			                                	
			                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                }
			                              
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                dataStartColumn++;
			                                snapShotRef++;
		                                }
		                                
		                                if(isQPR3SpendFor)
		                                {
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                               // projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost()+"");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(spendByBean.getQpr3TotalCost()!=null)
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
			                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue("-");
			                                /*	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
			                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	} */
			                                	
			                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                }
			                             
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                dataStartColumn++;
			                                snapShotRef++;
		                                }
		                                if(isFullYearSpendFor)
		                                {
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                               // projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost()+"");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(spendByBean.getFullYearTotalCost()!=null)
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
			                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue("-");
			                                /*	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
			                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}*/
			                                	

			                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                }
			                               
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                dataStartColumn++;
			                                snapShotRef++;
		                                }
		                                
		                                if(isLatestCost)
		                                {
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                //projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost()+"");
			                                if(spendByBean.getLatestTotalCost()!=null)
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
			                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue("-");
			                                	//projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("NA");
			                                	}
			                                	else
			                                	{
			                                		projectCodeDataRowColumn.setCellValue("-");
			                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	}
			                                }
			                               
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                dataStartColumn++;
			                                snapShotRef++;
		                                }
		                              
                        			}
                        			// This else is for calculating the proper dataStartColumn value
                        			else
                        			{
	                        			dataStartColumn = 0;
	                        		    dataStartColumn++;
		                                
		                                dataStartColumn++;
		                                
		                                dataStartColumn++;
		                                
		                                /*if(i>0)
		                                {
		                                	int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
		                                        dataStartColumn++;
		                                	}
		                                	dataStartColumn=budgetYearColumn;
		                                }*/
		                               
		                                if(isCOPLASpendFor)
		                                {
			                               
			                                dataStartColumn++;
		                                }
		                                
		                                if(isQPR1SpendFor)
		                                {
			                                  dataStartColumn++;
		                                }
		                                if(isQPR2SpendFor)
		                                {
			                                 dataStartColumn++;
		                                }
		                                
		                                if(isQPR3SpendFor)
		                                {
			                                  dataStartColumn++;
		                                }
		                                if(isFullYearSpendFor)
		                                {
			                                  dataStartColumn++;
		                                }
		                                
		                                
		                                dataStartColumn++;
		                                
		                            }
	                                
                        		}
                        	//	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                        	  //  		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);	
                        	}
                        	budgetYearColumn = dataStartColumn;
                        	
                        }
	                    
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(dataStartColumn -1)));
	                    }
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(3+totalCosts.size() - 1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(3+totalCosts.size() - 1)));
	                    }
	                    
	                   
	                 
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                   
	                    
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
                        
                        sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,2));
                        int totalCostColumnIndex = 3;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	
                        	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                        
                        //Notes Region
                        
                        if(dataStartColumn > 0)
                        {
	                    	
                        	
                        	
                        	dataRow = sheet.createRow(startRow+3);
                        	
                        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,dataStartColumn -1);
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	
                        	
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            
	                    	
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	
                        	
                        	
                        	
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,(3+totalCosts.size() - 1)));
                        	
                        	
                        	dataRow = sheet.createRow(startRow+3);
                        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,(3+totalCosts.size() - 1));
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            
	                    	
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    }
                
                    
                    // Spend By Budget Location
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getId())) 
                    {

                        
                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                      //  String[] spendForSnapshotArr = spendForSnapshot.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                      
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                        //String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        
                        int cellIndex = 4;
                        
                       // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),3,5));
                       // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),6,9));
                        
                        
                        
                    	//int firstRowColumns = 4 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                    	int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 4 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 4 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                    
	                       
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                     
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
		                    
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	                    HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Region");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Area");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    //sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn2.setCellValue("T20, T40, or non-T40");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                 //   sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	
	                    HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn3.setCellValue("Budget Location");
	                    tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost=false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
	                    	isQPR1SpendFor=false;
	                        isQPR2SpendFor=false;
	                        isQPR3SpendFor=false;
	                        isFullYearSpendFor=false;
	                        isCOPLASpendFor=false;
	                        isLatestCost=false;
	                        
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
	                    	
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
                        	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        	
                       /* 	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByBudgetLocation(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor, isLatestCost);*/
                        	
                        }
                        	
	                    	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByBudgetLocation(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    	Map<String,List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByBudgetLocation(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
	                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	// This is done in case there are no records for the budget location with which the user is associated with 
                        	else
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
                        			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		
                        		//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                        	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                        		
                        		HSSFRow dataRow = null;
                    			HSSFCell projectCodeDataRowColumn = null;
                        		for(String bugLocKey:spendByReportMap.keySet() )
                        		{
	                        		
                        			Integer budgetLocation = null;
                        			
                        			try
                        			{
                        				budgetLocation = new Integer(bugLocKey.split("~")[0]);
                        			}
                        			catch(Exception e)
                        			{
                        				
                        			}

                        			String regionName = "";
                      				String areaName = "";
                      				String t20_40_Name = "";
                        			
                        			try
                      				{
                        				 regionName = bugLocKey.split("~")[1];
                      				}
                      				catch(Exception e)
                      				{
                      					
                      				}
                      				try
                      				{
                      					areaName = bugLocKey.split("~")[2];
                      				}
                      				catch(Exception e)
                      				{
                      					
                      				}
                      				try
                      				{
                      					t20_40_Name = bugLocKey.split("~")[3];
                      				}
                      				catch(Exception e)
                      				{
                      					
                      				}
                      				
	                        			
	                        			
		                        			dataStartColumn = 0;
		                        			dataRow = sheet.createRow(++startRow);
		                        		//	previousYearExist = true;
		                        			
			                    			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                    			sheet.setColumnWidth(dataStartColumn, columnWidth);
			                    			
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
			                                if(StringUtils.isNotBlank(regionName))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(regionName);
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(StringUtils.isNotBlank(areaName))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(areaName);
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                               
			                                dataStartColumn++;
			                                
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(StringUtils.isNotBlank(t20_40_Name))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(t20_40_Name);
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                
		                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                        			sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(budgetLocation!=null)
			                                {
			                                	projectCodeDataRowColumn.setCellValue(SynchroUtils.getBudgetLocationName(budgetLocation));
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                //int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                
			                                int noOfColumnsToDecorate = 0;
			                                if(isLatestCost)
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                }
			                                else
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
			                                }
			                                
			                                int decCol = dataStartColumn;
		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
		                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
		                                		sheet.setColumnWidth(decCol, columnWidth);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue("-");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		    	                                decCol++;
		                                	}
		                                	
		                                	int snapShotRef = 0;
		                                	for(SpendByReportBean spendByBean:spendByReportMap.get(bugLocKey))
					                      	{
					                        		
				                        			boolean isLatesCostNA = true;
				                        			
					                                if(isCOPLASpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getCoplaTotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                	
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                	
						                                	/*if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	
						                                	
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                }
						                                
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                                if(isQPR1SpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getQpr1TotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                /*
						                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                }
						                               
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                if(isQPR2SpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getQpr2TotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                
						                                	
						                                /*	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                              
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                                if(isQPR3SpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getQpr3TotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                	/*
						                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                             //   projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                if(isFullYearSpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getFullYearTotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                	/*
						                                	
						                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	//projectCodeDataRowColumn.setCellValue("-");*/
						                                	
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                               // projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                                if(isLatestCost)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getLatestTotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	
						                                }
						                                else
						                                {
						                                	/*if(isLatesCostNA)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{		                                	
							                                	projectCodeDataRowColumn.setCellValue("-");
							                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}*/
						                                	
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                               // projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                              /*  if(i==0)
					                                {
					                                	int noOfColumnsToDecorate = (budgetYearsArr.length-1) * (spendForSnapshotArr.length+1);
					                                	if(noOfColumnsToDecorate>0)
					                                	{
					                                		int columnLocation = dataStartColumn;
					                                		for(int k=0;k<noOfColumnsToDecorate;k++)
						                                	{
					                                			projectCodeDataRowColumn = dataRow.createCell(columnLocation);
						    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
						    	                                projectCodeDataRowColumn.setCellValue(" ");
						    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						    	                                columnLocation++;
						                                	}
					                                	}
					                                }*/
					                                
				                        		
				                        		/*calculateTotaCost(spendByReportMap.get(bugLoc), isQPR1SpendFor, 
				                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);*/	
				                        	}
	                        	budgetYearColumn = dataStartColumn;
                        	}
                        	}
                    
                        
                        
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	//sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	
	                    	if(isLatestCost)
	                    	{
	                    		sheet.addMergedRegion(new CellRangeAddress(0,3,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length+1)))));
	                    		sheet.addMergedRegion(new CellRangeAddress(4,8,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length+1)))));
	                    	}
	                    	else
	                    	{
	                    		sheet.addMergedRegion(new CellRangeAddress(0,3,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length)))));
	                    		sheet.addMergedRegion(new CellRangeAddress(4,8,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length)))));
	                    	}
	                    }
	                   
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(3+totalCosts.size() )));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(3+totalCosts.size() )));
	                    }
	                 
	                    
	                   
	                 
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                   
	                    
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    totalCostColumn = dataRow.createCell(3);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
                        
                        
	                    sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,3));
	                    
                        int totalCostColumnIndex = 4;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                        
                        //Notes Region
                        
                       
	                 
                        
                        if(dataStartColumn > 0)
                        {
	                    	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,dataStartColumn -1));
                        	dataRow = sheet.createRow(startRow+3);
                        	
	                    	if(isLatestCost)
	                    	{
	                    		//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+12,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length+1)))));
	                    		
	                    		CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+10,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length+1))));
	                        	
	                        	sheet.addMergedRegion(mergedRegion);
	                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	}
	                    	else
	                    	{
	                    		//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length)))));
	                    		CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length))));
	                        	
	                        	sheet.addMergedRegion(mergedRegion);
	                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	}
	                    	
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,(3+totalCosts.size() - 1)));
                        	
                        	dataRow = sheet.createRow(startRow+3);
                        	
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,(3+totalCosts.size())));
                        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,(3+totalCosts.size()));
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        	
	                    	
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    
                    }
                    
                    // Spend By Methodology
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getId()))
                    {

                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                        //String[] spendForSnapshotArr = spendForSnapshot.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        
                        int cellIndex = 2;
                        
                        
                    	//int firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        
                        int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                    
	                        /*if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
	                       
	                        cellIndex = cellIndex+spendForSnapshotArr.length+1;
		                    */
	                        
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	                  /*  HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Project Code");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Project Name");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    sheet.autoSizeColumn(startColumn);
	*/
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn2.setCellValue("Methodology Group");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    
	                    tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn2.setCellValue("Methodology");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost = false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
	                    	isQPR1SpendFor=false;
	                        isQPR2SpendFor=false;
	                        isQPR3SpendFor=false;
	                        isFullYearSpendFor=false;
	                        isCOPLASpendFor=false;
	                        isLatestCost = false;
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
                        
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                        
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        	/*
                        	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByMethodology(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor, isLatestCost);
                    	    */		
                        	
                        }
                        	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByMethodology(new Integer(budgetYearsArr[i]),currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	
                        	//Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByMethodologyMap(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    	Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByMethodology(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                          	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
	                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	// This is done in case there are no records for the budget location with which the user is associated with 
                        	else
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
                        			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                          	{
                          		//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                        	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                          		
                          		HSSFRow dataRow = null;
                      			HSSFCell projectCodeDataRowColumn = null;
                          		for(String  methKey:spendByReportMap.keySet() )
                          		{
  	                        		
                          			String meth = ""; 
                  					try
                  					{
                  						meth =	methKey.split("~")[0];
                  					}
                  					catch(Exception e)
                  					{
                  						
                  					}
                          			String methGroup = ""; 
                          			try
                  					{
                          				methGroup =	methKey.split("~")[1];
                  					}
                  					catch(Exception e)
                  					{
                  						
                  					}	
                          					
                        	
  	                        				 dataStartColumn = 0;
		                        			 dataRow = sheet.createRow(++startRow);
		                        			// previousYearExist = true;
			                    		
		                        		/*	HSSFCell projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                projectCodeDataRowColumn.setCellValue(" ");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
			                                dataStartColumn++;
			                                
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellValue(" ");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                dataStartColumn++;
			                             */
		                        			
		                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                        			sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                
			                               /* String met = spendByBean.getMethodologies();
			                                String methGroup = "";
			                                if(met!=null)
			                                {
			                                	if(met.contains(","))
			                                	{
			                                		
			                                		methGroup = SynchroGlobal.getMethodologyGroupName(new Long (met.split(",")[0]));
			                                	}
			                                	else
			                                	{
			                                	
			                                		
			                                		methGroup = SynchroGlobal.getMethodologyGroupName(new Long (met));
			                                	}
			                                }
			                                */
			                                projectCodeDataRowColumn.setCellValue(methGroup);
			                                
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                               
			                                if(StringUtils.isBlank(methGroup))
			                                {
				                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                            projectCodeDataRowColumn.setCellValue("-");
			    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                                dataStartColumn++;
		                        			 
		                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                        			sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(StringUtils.isNotBlank(meth))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(SynchroDAOUtil.getMethodologyNames(meth));
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                
			                               // int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                
			                                int noOfColumnsToDecorate = 0;
			                                if(isLatestCost)
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                }
			                                else
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
			                                }
			                                
			                                int decCol = dataStartColumn;
		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
		                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
		                                		sheet.setColumnWidth(decCol, bannerColumnWidth);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue("-");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		    	                                decCol++;
		                                	}
  	                        		
		                                	int snapShotRef = 0;
		                                	for(SpendByReportBean spendByBean:spendByReportMap.get(methKey))
					                      	{
  	                        		boolean isLatesCostNA = true;
  	                        		
  	                        		if(isCOPLASpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getCoplaTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	//projectCodeDataRowColumn.setCellValue("-");*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR1SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr1TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isQPR2SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr2TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
//		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR3SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr3TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isFullYearSpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getFullYearTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isLatestCost)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getLatestTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                }
		                                else
		                                {
		                                	/*if(isLatesCostNA)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
			                                	projectCodeDataRowColumn.setCellValue("-");
			                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                           
	                                
                        		
                        	/*	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);*/	
                        	}
                        	budgetYearColumn = dataStartColumn;
                          		}
                        	}
	                    
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(dataStartColumn -1)));
	                    }
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCosts.size() + 1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(totalCosts.size() + 1)));
	                    }
	                    
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    
                        
                        
	                    sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,1));
	                    
	                    
	                  /*  totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
                        
                        sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,2));
                        */
                        int totalCostColumnIndex = 2;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                    
                                              
                        if(dataStartColumn > 0)
                        {
	                    	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,dataStartColumn -1));
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,dataStartColumn -1));
	                    	
	                    	
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,dataStartColumn -1);
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,(totalCosts.size() + 1)));
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,(totalCosts.size() + 1));
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    }

                    // Spend By Branded/Non Branded
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BRANDED_NON_BRANDED.getId()))
                    {


                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                       // String[] spendForSnapshotArr = spendForSnapshot.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                      
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                        //String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        int cellIndex = 2;
                        
                        
                    	//int firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        
                        int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                    
	                       /* if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
	                       
	                        cellIndex = cellIndex+spendForSnapshotArr.length+1; */
	                        
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
		                    
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	                   /* HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Project Code");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Project Name");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    sheet.autoSizeColumn(startColumn);
	*/
                        HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn2.setCellValue("Brand Type");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                    //sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
                        
                        tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn2.setCellValue("Brand");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost = false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                       
	                        
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
	                    	
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                        
	                    	
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
	                    		costColumns = tableHeaderRow.createCell(++startColumn);
	                    	
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
                        	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        /*	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByBrand(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                        	*/
                        }
                        	
	                    //List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByBrand(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    
	                   // Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByBrandMap(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByBrand(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	
	                    
	                    if(spendByReportMap!=null && spendByReportMap.size()>0)
                    	{
                    		for(int i=0;i<budgetYearsArr.length;i++)
                            {
                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                            }
                    	}
	                 // This is done in case there are no records for the budget location with which the user is associated with 
                    	else
                    	{
                    		for(int i=0;i<budgetYearsArr.length;i++)
                            {
                    			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                            }
                    	}
	                    
	                    if(spendByReportMap!=null && spendByReportMap.size()>0)
                      	{
	                    	//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                    	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
	                    	
	                    	HSSFRow dataRow = null;
                  			HSSFCell projectCodeDataRowColumn = null;
                  			for(String brandKey:spendByReportMap.keySet() )
                    		{
                      			
                  				//Integer brand = new Integer(brandKey.split("~")[0]);
                  				String brandName = brandKey.split("~")[0];
                    			String brandType  = brandKey.split("~")[1];
                    			
                      			
	                    				dataStartColumn = 0;
	                        			dataRow = sheet.createRow(++startRow);
	                        		
	                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                        			sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		                               // projectCodeDataRowColumn.setCellValue(SynchroUtils.getBrandBrandTypeFields().get(spendByBean.getBrand()));
		                                projectCodeDataRowColumn.setCellValue(brandType);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(StringUtils.isBlank(brandType))
		                                {
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                            projectCodeDataRowColumn.setCellValue("-");
		    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                }
	    	                            
		                                dataStartColumn++;
	                        			
	                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                        			sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(StringUtils.isNotBlank(brandName))
		                                {
		                                	projectCodeDataRowColumn.setCellValue(brandName);
		                                }
		                                
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                            projectCodeDataRowColumn.setCellValue("-");
		    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                }
		                                
		                                
		                               
		                                dataStartColumn++;
		                                
		                             
		                                
		                               // int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
		                                
		                                int noOfColumnsToDecorate = 0;
		                                if(isLatestCost)
		                                {
		                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
		                                }
		                                else
		                                {
		                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
		                                }
		                                
		                                int decCol = dataStartColumn;
	                                	for(int k=0;k<noOfColumnsToDecorate;k++)
	                                	{
	                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
	                                		sheet.setColumnWidth(decCol, bannerColumnWidth);
	    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                                projectCodeDataRowColumn.setCellValue("-");
	    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	    	                                decCol++;
	                                	}
		                                
	                                	int snapShotRef = 0;
	                                	for(SpendByReportBean spendByBean:spendByReportMap.get(brandKey))
				                      	{
                        			
                        			
	                        		
                        			boolean isLatesCostNA = true;
                        			
	                                if(isCOPLASpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getCoplaTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR1SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr1TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                /*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isQPR2SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr2TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                
		                                /*	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR3SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr3TotalCost()!=null)
		                                {
		                                //	projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isFullYearSpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getFullYearTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	
		                                	/*
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	} */
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isLatestCost)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getLatestTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                }
		                                else
		                                {
		                                	/*if(isLatesCostNA)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
			                                	projectCodeDataRowColumn.setCellValue("-");
			                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                             /*   if(i==0)
	                                {
	                                	int noOfColumnsToDecorate = (budgetYearsArr.length-1) * (spendForSnapshotArr.length+1);
	                                	if(noOfColumnsToDecorate>0)
	                                	{
	                                		int columnLocation = dataStartColumn;
	                                		for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
	                                			projectCodeDataRowColumn = dataRow.createCell(columnLocation);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue(" ");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		    	                                columnLocation++;
		                                	}
	                                	}
	                                }*/
	                                
                        		}
                        		/*calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);	*/
                        	
                  			
                        	budgetYearColumn = dataStartColumn;
                        }
                      	}
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(dataStartColumn -1)));
	                    }
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCosts.size() + 1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(totalCosts.size() + 1)));
	                    }
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    
                        
                        
	                    sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,1));
	                   
	                   /* 
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
                        
                        sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,2));*/
                        int totalCostColumnIndex = 2;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle , costDecimalFormatStyle);
                        	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                        
                        //Notes Region
                        
                        
                        if(dataStartColumn > 0)
                        {
	                    	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,dataStartColumn -1));
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,dataStartColumn -1);
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,(totalCosts.size() + 1)));
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,(totalCosts.size() + 1));
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    
                    }
                    
                    // Spend By Agency
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_AGENCY.getId()))
                    {

                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                       // String[] spendForSnapshotArr = spendForSnapshot.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                      
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                        //String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        
                        int cellIndex = 1;
                        

                        
                        
                    	//int firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        
                        int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                    
	                       /* if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
	                       
	                        cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        */
	                        
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
		                    
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	                   /* HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Project Code");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Project Name");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    sheet.autoSizeColumn(startColumn);
						*/
                        
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn2.setCellValue("Research Agency");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                    //sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost=false;
	                    
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
	                    	 isQPR1SpendFor=false;
	                         isQPR2SpendFor=false;
	                         isQPR3SpendFor=false;
	                         isFullYearSpendFor=false;
	                         isCOPLASpendFor=false;
	                         isLatestCost=false;
	                        
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}        		
		                    }
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                        

	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
	                    		costColumns = tableHeaderRow.createCell(++startColumn);
		                    	
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
                        	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                       /*
                        	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByAgency(new Integer(budgetYearsArr[i]) , currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor, isLatestCost);
                    	    */
                        	
                        }	
                        	
                        	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByAgency(new Integer(budgetYearsArr[i]) , currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByAgency(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    
	                    if(spendByReportMap!=null && spendByReportMap.size()>0)
                    	{
                    		for(int i=0;i<budgetYearsArr.length;i++)
                            {
                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                            }
                    	}
	                 // This is done in case there are no records for the budget location with which the user is associated with 
                    	else
                    	{
                    		for(int i=0;i<budgetYearsArr.length;i++)
                            {
                    			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                            }
                    	}
	                    
	                    if(spendByReportMap!=null && spendByReportMap.size()>0)
                      	{
	                    	//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                    	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
	                    	
	                    	HSSFRow dataRow = null;
                  			HSSFCell projectCodeDataRowColumn = null;
                      		for(String  agency:spendByReportMap.keySet() )
                      		{
                      			
                      			
                        			
                        			
                        			dataStartColumn = 0;
                        			 dataRow = sheet.createRow(++startRow);
                        			
                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                        			sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                try
	                                {
		                                Integer agId = new Integer(agency);
		                                //String agencyName =  SynchroGlobal.getResearchAgency().get(agId.intValue());
		                                String agencyName =  SynchroGlobal.getAllResearchAgency().get(agId.intValue());
		                                if(StringUtils.isNotBlank(agencyName))
		                                {
		                                	projectCodeDataRowColumn.setCellValue(agencyName);
		                                }
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                            projectCodeDataRowColumn.setCellValue("-");
		    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                }
	                                }
	                                catch(Exception e)
	                                {
	                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                            projectCodeDataRowColumn.setCellValue("-");
	    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                }
	                                
	                               
	                                dataStartColumn++;
	                                
	                                
	                               // int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
	                                
	                                int noOfColumnsToDecorate = 0;
	                                if(isLatestCost)
	                                {
	                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
	                                }
	                                else
	                                {
	                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
	                                }
	                                
	                                int decCol = dataStartColumn;
                                	for(int k=0;k<noOfColumnsToDecorate;k++)
                                	{
                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
                                		sheet.setColumnWidth(decCol, columnWidth);
    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
    	                                projectCodeDataRowColumn.setCellValue("-");
    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
    	                                decCol++;
                                	}
                                	
	                               
                                	int snapShotRef = 0;
                                	for(SpendByReportBean spendByBean:spendByReportMap.get(agency))
                            		{
                        			boolean isLatesCostNA = true;
                        			
	                                if(isCOPLASpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getCoplaTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR1SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr1TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	
		                                	/*
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isQPR2SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr2TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR3SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr3TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isFullYearSpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getFullYearTotalCost()!=null)
		                                {
		                                //	projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                
		                                /*	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	} */
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isLatestCost)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getLatestTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                }
		                                else
		                                {
		                                	/*if(isLatesCostNA)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
			                                	projectCodeDataRowColumn.setCellValue("-");
			                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                              
	                                
                        		}
                        	/*	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor); */	
                        	
                        	budgetYearColumn = dataStartColumn;
                        }
                      	}
                    
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(dataStartColumn -1)));
	                    }
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCosts.size() )));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(totalCosts.size() )));
	                    }
	                    
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                   
	                    
	                    /*totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
                        
                        sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,2));*/
                        int totalCostColumnIndex = 1;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                        
                        //Notes Region
                        
                      
                        if(dataStartColumn > 0)
                        {
	                    	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+20,0,dataStartColumn -1));
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+18,0,dataStartColumn -1);
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Costs extracted from this report may slightly differ from the actual project cost due to currency conversion.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+20,0,(totalCosts.size())));
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+18,0,(totalCosts.size()));
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Costs extracted from this report may slightly differ from the actual project cost due to currency conversion.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    
                    }
                    
                    // Spend By Kantar/Non-Kantar
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_KANTAR_NONKANTAR.getId()))
                    {


                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                      //  String[] spendForSnapshotArr = spendForSnapshot.split(",");
                         
                         String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                         
                         List<String> spendForSnapShotList = new ArrayList<String>();
                       
                         boolean showLatestCost = false;
                         
                         
                         for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                         {
                         	if(!spendForSnapshotArrOriginal[z].equals("6"))
                         	{
                         		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                         	}
                         	else
                        	{
                        		showLatestCost = true;
                        	}
                         }
                         //String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                         String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                         
                         
                        int cellIndex = 1;
                        
                        
                    	//int firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        
                        int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                    
	                       /* if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
	                       
	                        cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        */
	                        
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
		                    
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	               /*     HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Project Code");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Project Name");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    sheet.autoSizeColumn(startColumn);
					*/
		
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn2.setCellValue("Research Agency Type");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost=false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                      
	                        
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
	                    	
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
		                        costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
                        	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        /*	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByAgencyType(new Integer(budgetYearsArr[i]), currencyId,filter,isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor, isLatestCost);*/
                        }	
                        	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByAgencyType(new Integer(budgetYearsArr[i]), currencyId,filter,isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    
	                    Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByAgencyTypeMap(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    
	                    if(spendByReportMap!=null && spendByReportMap.size()>0)
                    	{
                    		for(int i=0;i<budgetYearsArr.length;i++)
                            {
                        		//calculateTotalCostAgencyTypeBY(spendByReportMap, isQPR1SpendFor, 
                        	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                    			
                    			calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
                            	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                            }
                    	}
	                 // This is done in case there are no records for the budget location with which the user is associated with 
                    	else
                    	{
                    		for(int i=0;i<budgetYearsArr.length;i++)
                            {
                    			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                            }
                    	}
	                    
	                    if(spendByReportMap!=null && spendByReportMap.size()>0)
                      	{
	                    	//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                    	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
	                    	
	                    	HSSFRow dataRow = null;
                  			HSSFCell projectCodeDataRowColumn = null;
                      		for(String  agency:spendByReportMap.keySet() )
                      		{
                      			boolean previousYearExist = false;
                      			for(SpendByReportBean spendByBean:spendByReportMap.get(agency))
                        		{
                        			if(spendByBean.getBudgetYear().toString().equalsIgnoreCase(budgetYearsArr[0]))
		                        	{
                        	
                        			
	                        			dataStartColumn = 0;
	                        			 dataRow = sheet.createRow(++startRow);
	                        			 previousYearExist = true;
		                    			/*HSSFCell projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellValue(" ");
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
		                                dataStartColumn++;
		                                
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		                                projectCodeDataRowColumn.setCellValue(" ");
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                */
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(spendByBean.getResearchAgecnyType()!=null)
		                                {
		                                	projectCodeDataRowColumn.setCellValue(spendByBean.getResearchAgecnyType());
		                                }
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                            projectCodeDataRowColumn.setCellValue("-");
		    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                }
		                               
		                                dataStartColumn++;
		                                
		                              /*  if(i>0)
		                                {
		                                	int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
		                                		projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue(" ");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		    	                                dataStartColumn++;
		                                	}
		                                	dataStartColumn=budgetYearColumn;
		                                } */
		                                
		                                //int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
		                                
		                                int noOfColumnsToDecorate = 0;
		                                if(isLatestCost)
		                                {
		                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
		                                }
		                                else
		                                {
		                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
		                                }
		                                
		                                int decCol = dataStartColumn;
	                                	for(int k=0;k<noOfColumnsToDecorate;k++)
	                                	{
	                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
	                                		sheet.setColumnWidth(decCol, columnWidth);
	    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                                projectCodeDataRowColumn.setCellValue("-");
	    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	    	                                decCol++;
	                                	}
		                        	}
                        			else
                        			{
                        				for(int i=1;i<budgetYearsArr.length;i++)
                        				{
                        					if(spendByBean.getBudgetYear().toString().equalsIgnoreCase(budgetYearsArr[i]))
    	                        			{
                        						if(i>1)
                        						{
                        							// This has been done for having the data in case there is no data for previous years 
                        							if(!previousYearExist)
                        							{
                        								dataRow = sheet.createRow(++startRow);
                        								previousYearExist=true;
                        								
                        								projectCodeDataRowColumn = dataRow.createCell(0);
                        								sheet.setColumnWidth(0, columnWidth);
             			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
             			                               projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
             			                                if(spendByBean.getResearchAgecnyType()!=null)
             			                                {
             			                                	projectCodeDataRowColumn.setCellValue(spendByBean.getResearchAgecnyType());
             			                                }
             			                                else
             			                                {
             			                                	//projectCodeDataRowColumn.setCellValue(" ");
             			                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
             			    	                            projectCodeDataRowColumn.setCellValue("-");
             			    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
             			                                }
             			                                
             			                               
             			                                dataStartColumn = (i*(spendForSnapshotArr.length+1)) + 1;
             			                               
             			                             //  	int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
             			                               	
             			                               int noOfColumnsToDecorate = 0;
             			                                if(isLatestCost)
             			                                {
             			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
             			                                }
             			                                else
             			                                {
             			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
             			                                }
             			                                
	          			                                int decCol = 1;
	          		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
	          		                                	{
	          		                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
	          		                                		sheet.setColumnWidth(decCol, columnWidth);
	          		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	          		    	                                projectCodeDataRowColumn.setCellValue("-");
	          		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	          		    	                                decCol++;
	          		                                	}
                        							}
                        							/*else
                        							{
                        								dataStartColumn = dataStartColumn+spendForSnapshotArr.length+1;
                        							}*/
                        							else
                        							{
                        								
                        								//dataStartColumn = (i*(spendForSnapshotArr.length+1)) + 1;
                        								
                        								if(isLatestCost)
              			                                {
                        									 dataStartColumn = (i*(spendForSnapshotArr.length+1)) + 1;
              			                                }
              			                                else
              			                                {
              			                                	dataStartColumn = (i*(spendForSnapshotArr.length)) + 1;
              			                                }
              			                                
                        							}
                        							break;
                        						}
    	                        			}
                        					
                        				}
                        			}
	                               
                        			boolean	isLatesCostNA = true;
                        			
	                                if(isCOPLASpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(spendByBean.getCoplaTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                	/*if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
		                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("1"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	//projectCodeDataRowColumn.setCellValue("-");
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                }
		                                
		                                dataStartColumn++;
	                                }
	                                
	                                if(isQPR1SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(spendByBean.getQpr1TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                /*	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
		                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("2"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                //	projectCodeDataRowColumn.setCellValue("-");
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
	                                }
	                                if(isQPR2SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr2TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                /*	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
		                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("3"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	} */
		                                	
		                                //	projectCodeDataRowColumn.setCellValue("-");
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
	                                }
	                                
	                                if(isQPR3SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr3TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                	/*if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
		                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("4"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	
		                                	//projectCodeDataRowColumn.setCellValue("-");
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
	                                }
	                                if(isFullYearSpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getFullYearTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(" ");
		                                /*	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
		                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("5"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	} */
		                                	
		                                	//projectCodeDataRowColumn.setCellValue("-");
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
	                                }
	                                
	                                if(isLatestCost)
	                                {
		                                
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getLatestTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                }
		                                else
		                                {
		                                	/*if(isLatesCostNA)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
			                                	projectCodeDataRowColumn.setCellValue("-");
			                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}*/
		                                	projectCodeDataRowColumn.setCellValue("-");
		                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
	                                }
		                                
	                             /*   if(i==0)
	                                {
	                                	int noOfColumnsToDecorate = (budgetYearsArr.length-1) * (spendForSnapshotArr.length+1);
	                                	if(noOfColumnsToDecorate>0)
	                                	{
	                                		int columnLocation = dataStartColumn;
	                                		for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
	                                			projectCodeDataRowColumn = dataRow.createCell(columnLocation);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue(" ");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		    	                                columnLocation++;
		                                	}
	                                	}
	                                }*/
	                                
                        		}
                        	/*	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);	*/
                        	}
                        	budgetYearColumn = dataStartColumn;
                        }
	                    
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(dataStartColumn -1)));
	                    }
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCosts.size() )));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(totalCosts.size() )));
	                    }
	                 
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                   
	                 /*   
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
                        
                        sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,2));
                       */
                        int totalCostColumnIndex = 1;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                    
                        //Notes Region
                        
                        if(dataStartColumn > 0)
                        {
	                    	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+20,0,dataStartColumn -1));
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+18,0,dataStartColumn -1);
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Costs extracted from this report may slightly differ from the actual project cost due to currency conversion.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+20,0,(3+totalCosts.size() - 1)));
	                    	dataRow = sheet.createRow(startRow+5);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+18,0,(3+totalCosts.size() - 1));
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Costs extracted from this report may slightly differ from the actual project cost due to currency conversion.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    }
                    
                    
                 // Spend By Category
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_CATEGORY.getId()))
                    {

                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                        //String[] spendForSnapshotArr = spendForSnapshot.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                      
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        
                        int cellIndex = 1;
                        
                        
                    	//int firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        
                        int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 1 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                    
	                       /* if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
	                       
	                        cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        */
	                        
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
		                    
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn2.setCellValue("Category");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                    //sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost = false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
	                    	isQPR1SpendFor=false;
	                        isQPR2SpendFor=false;
	                        isQPR3SpendFor=false;
	                        isFullYearSpendFor=false;
	                        isCOPLASpendFor=false;
	                        isLatestCost = false;
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
                        
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                        
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
                        	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        /*	
                        	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByCategoryType(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                    	    */		
                        	
                        }
                        	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByMethodology(new Integer(budgetYearsArr[i]),currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	
                        	//Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByCategoryTypeMap(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    	Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByCategoryType(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                          	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
	                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	// This is done in case there are no records for the budget location with which the user is associated with 
                        	else
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
                        			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                          	{
                          	
                          		//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                        	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                          		
                          		HSSFRow dataRow = null;
                      			HSSFCell projectCodeDataRowColumn = null;
                      			for(String  spendByReportKey :spendByReportMap.keySet() )
                          		{
  	                        		
                          			String categoryName = SynchroDAOUtil.getCategoryNames(spendByReportKey);
                        	
  	                        				 dataStartColumn = 0;
		                        			 dataRow = sheet.createRow(++startRow);
		                        			 
			                    		
		                        		/*	HSSFCell projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                projectCodeDataRowColumn.setCellValue(" ");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
			                                dataStartColumn++;
			                                
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellValue(" ");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                dataStartColumn++;
			                             */
		                        			
		                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                        			sheet.setColumnWidth(dataStartColumn, columnWidth);
		                        			
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(categoryName!=null && !categoryName.equalsIgnoreCase(""))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(categoryName);
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
 			    	                            projectCodeDataRowColumn.setCellValue("-");
 			    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                	
			                                }
			                               
			                                dataStartColumn++;
			                                
			                                
			                                //int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                
			                                int noOfColumnsToDecorate = 0;
			                                if(isLatestCost)
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                }
			                                else
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
			                                }
			                                
			                                int decCol = dataStartColumn;
		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
		                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
		                                		sheet.setColumnWidth(decCol, columnWidth);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue("-");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		    	                                decCol++;
		                                	}
		                                	
		                                	int snapShotRef = 0;
  	                        			
		                                	for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
		                      				{
	                        			
	                             
  	                        		/*if(i>0)
	                                {
	                                	int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
	                                	for(int k=0;k<noOfColumnsToDecorate;k++)
	                                	{
	                                		projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                                projectCodeDataRowColumn.setCellValue(" ");
	    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	    	                                dataStartColumn++;
	                                	}
	                                	dataStartColumn=budgetYearColumn;
	                                }
	                               */
  	                        			
  	                        		boolean	isLatesCostNA = true;
  	                        		
	                                if(isCOPLASpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getCoplaTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	//projectCodeDataRowColumn.setCellValue("-");*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR1SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(spendByBean.getQpr1TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isQPR2SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr2TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR3SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr3TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	
		                                	/*
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isFullYearSpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(spendByBean.getFullYearTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isLatestCost)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
		                                if(spendByBean.getLatestTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                }
		                                else
		                                {
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                           
	                                
                        		}
                        	/*	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);*/	
                        	}
                        	budgetYearColumn = dataStartColumn;
                        }
	                    
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(dataStartColumn -1)));
	                    }
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCosts.size() )));
	                    	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(totalCosts.size() )));
	                    }
	                    
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                   
	                    
	                  /*  totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
                        
                        sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,2));
                        */
                        int totalCostColumnIndex = 1;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                    
                        //Notes Region
                        
                        if(dataStartColumn > 0)
                        {
	                    	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,dataStartColumn -1));
	                    	dataRow = sheet.createRow(startRow+3);
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,dataStartColumn -1);
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,(totalCosts.size() )));
	                    	
	                    	dataRow = sheet.createRow(startRow+3);
	                    	
	                    	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,(totalCosts.size()));
                        	
                        	sheet.addMergedRegion(mergedRegion);
                        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    
                    }
                    
                    // Commenting the logo for Spend Reports
                    workbook = SynchroUtils.createExcelImage(workbook, sheet);
                }
            }
        }

        return workbook;
    }

    private HSSFWorkbook generateCrossTabReport(HSSFWorkbook workbook, final SpendReportExtractFilter filter) throws UnsupportedEncodingException, IOException
    {

        
    	List<Long> snapShotIds = new ArrayList<Long>();
    	
    	Map<Integer, Map<Integer, Long>> snapShotMap = new HashMap<Integer, Map<Integer, Long>>();
    	if(StringUtils.isNotBlank(spendForSnapshot))
    	{
    		if(StringUtils.isNotBlank(years))
    		{
    			String[] spendForSnapshotArr = spendForSnapshot.split(",");
    			String[] budgetYearsArr = years.split(",");
    			for(int i=0;i<budgetYearsArr.length;i++)
    			{
    				HashMap<Integer, Long> sMap = new HashMap<Integer, Long>();
    				for(int j=0;j<spendForSnapshotArr.length;j++)
    				{
    					Long snapShotId = qprSnapshotManager.getSnapShotId(new Integer(spendForSnapshotArr[j]), new Integer(budgetYearsArr[i]));
    					if(snapShotId!=null && snapShotId.intValue() > 0)
    					{
    						snapShotIds.add(snapShotId);
    						
    						sMap.put(new Integer(spendForSnapshotArr[j]), snapShotId);
    					}
    					
    				}
    				snapShotMap.put(new Integer(budgetYearsArr[i]),sMap );
    			}
    		}
    	}
    	
    	
    	if(reportTypes != null && !reportTypes.equals("")) {


            String [] reportTypesStrArr = reportTypes.split(",");
            CurrencyExchangeRate currencyExchangeRate = getUserCurrencyExchangeRate(getUser());
            Integer startColumn = 0;
            Integer startRow = 4;
            boolean showProjectCodeColumn = false;


            if(reportTypesStrArr != null && reportTypesStrArr.length > 0) {

                HSSFDataFormat df = workbook.createDataFormat();
                short currencyDataFormatIndex = df.getFormat("#,###0.0000");

           

                // Header Styles start
                HSSFFont sheetHeaderFont = workbook.createFont();
                sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                sheetHeaderFont.setFontName("Calibri");
                
                HSSFFont callibiriFont = workbook.createFont();
                callibiriFont.setFontName("Calibri");

                HSSFFont notesFont = workbook.createFont();
                notesFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                notesFont.setFontName("Calibri");
                notesFont.setItalic(true);

                HSSFFont italicFont = workbook.createFont();
                italicFont.setItalic(true);

                HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
                sheetHeaderCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                sheetHeaderCellStyle.setFont(sheetHeaderFont);
                sheetHeaderCellStyle.setWrapText(true);
                sheetHeaderCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                // Header Styles end


                // Table Header Column Styles start

                // Table header column1 style
                HSSFCellStyle tableHeaderProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                tableHeaderProjectCodeColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                tableHeaderProjectCodeColumnStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderProjectCodeColumnStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

                HSSFCellStyle tableHeaderProjectNameColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                tableHeaderProjectNameColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                tableHeaderProjectNameColumnStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderProjectNameColumnStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

                // Table header column1 style
                HSSFCellStyle tableHeaderColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn1Style.setWrapText(true);
                tableHeaderColumn1Style.setFont(sheetHeaderFont);
                tableHeaderColumn1Style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderColumn1Style.setFillPattern(CellStyle.SOLID_FOREGROUND);

                
                // Table header column2 style
                HSSFCellStyle tableHeaderColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn2Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn2Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn2Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn2Style.setWrapText(true);
                
                tableHeaderColumn2Style.setFont(sheetHeaderFont);
                tableHeaderColumn2Style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderColumn2Style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                

                // Table header column3 style
                HSSFCellStyle tableHeaderColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn3Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn3Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn3Style.setWrapText(true);
                
                tableHeaderColumn3Style.setFont(sheetHeaderFont);
                tableHeaderColumn3Style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                tableHeaderColumn3Style.setFillPattern(CellStyle.SOLID_FOREGROUND);

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn4Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn5Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                // Table Header Column Styles end

                // Data Row cell styles start
                HSSFCellStyle projectCodeDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THICK);
                projectCodeDataRowColumnStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                projectCodeDataRowColumnStyle.setFont(callibiriFont);

                HSSFCellStyle projectNameDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THIN);
                projectNameDataRowColumnStyle.setWrapText(true);
                projectNameDataRowColumnStyle.setFont(callibiriFont);

                HSSFCellStyle dataRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THICK);
                dataRowColumn1Style.setWrapText(true);
                dataRowColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                dataRowColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);
                dataRowColumn1Style.setFont(callibiriFont);
                
             // Total cost row styles start
                HSSFCellStyle totalCostRowProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                totalCostRowProjectCodeColumnStyle.setWrapText(true);
                totalCostRowProjectCodeColumnStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                totalCostRowProjectCodeColumnStyle.setAlignment(CellStyle.ALIGN_CENTER);
                totalCostRowProjectCodeColumnStyle.setFont(sheetHeaderFont);

                HSSFCellStyle dataRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                
                dataRowColumn2Style.setWrapText(true);
                dataRowColumn2Style.setFont(callibiriFont);
                dataRowColumn2Style.setAlignment(CellStyle.ALIGN_CENTER);


                
                HSSFCellStyle dataRowColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN);
                dataRowColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);
                dataRowColumn3Style.setFont(callibiriFont);
                

                HSSFCellStyle dataRowColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle dataRowColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);
                // Data Row cell styles end

                

                HSSFDataFormat cf = workbook.createDataFormat();
                //short currencyDataFormatIndex = cf.getFormat("#,###0.0000");
     	       //short currencyDataFormatIndexString = cf.getFormat("@");
                short currencyDataFormatIndexString = cf.getFormat("#,###0");
                
                HSSFCellStyle costFormatStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                costFormatStyle.setDataFormat(currencyDataFormatIndexString);
                
                costFormatStyle.setWrapText(true);
                costFormatStyle.setFont(callibiriFont);

                short currencyDecimalDataFormatIndexString = cf.getFormat("#,###0.00");
                
                HSSFCellStyle costDecimalFormatStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                costDecimalFormatStyle.setDataFormat(currencyDecimalDataFormatIndexString);
                
                costDecimalFormatStyle.setWrapText(true);
                costDecimalFormatStyle.setFont(callibiriFont);
                
              
                
                HSSFCellStyle totalCostRowTotalColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                totalCostRowTotalColumnStyle.setWrapText(true);
                totalCostRowTotalColumnStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                totalCostRowTotalColumnStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                totalCostRowTotalColumnStyle.setFont(sheetHeaderFont);
                totalCostRowTotalColumnStyle.setDataFormat(currencyDataFormatIndexString);

                HSSFCellStyle totalCostRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                totalCostRowColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                totalCostRowColumn1Style.setFont(callibiriFont);

                HSSFCellStyle totalCostRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                
                HSSFCellStyle totalCostColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle notesStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_THIN,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN);
                
                
                notesStyle.setWrapText(true);
                notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
                notesStyle.setFont(callibiriFont);
                
                notesStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	            notesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    	
	            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		        
	            notesStyle.setAlignment(CellStyle.ALIGN_LEFT);
		    	notesStyle.setWrapText(true);
                
		    	
                if(filter.getBudgetLocations()!=null && filter.getBudgetLocations().size()>0)
                {
                	selectedFilters.add("Budget Location - "+ SynchroUtils.getBudgetLocationNames(filter.getBudgetLocations()));
                }
                if(filter.getMethDetails()!=null && filter.getMethDetails().size()>0)
                {
                	selectedFilters.add("Methodology - "+ SynchroDAOUtil.getMethodologyNames(StringUtils.join(filter.getMethDetails(),",")));
                }

                if(filter.getBrands()!=null && filter.getBrands().size()>0)
                {
                	selectedFilters.add("Branded/Non-Branded - "+ SynchroUtils.getBrandNames(filter.getBrands()));
                }

                boolean isSpendByProject = false;
                boolean isSpendByBudgetLocation=false;
                boolean isSpendByBrand=false;
                boolean isSpendByMethodology=false;
                boolean isSpendByAgency=false;
                
                boolean isSpendByKantarNonKantar=false;
                
                boolean isSpendByCategoryType=false;
                
                for(String reportTypeStr : reportTypesStrArr)
                {
                	Integer reportType = Integer.parseInt(reportTypeStr);
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getName());
                		isSpendByBudgetLocation=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getId()))
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getName());
                		isSpendByMethodology=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BRANDED_NON_BRANDED.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BRANDED_NON_BRANDED.getName());
                		isSpendByBrand=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_AGENCY.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_AGENCY.getName());
                		isSpendByAgency=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_KANTAR_NONKANTAR.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_KANTAR_NONKANTAR.getName());
                		isSpendByKantarNonKantar=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getName());
                		isSpendByProject=true;
                	}
                	
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_CATEGORY.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_CATEGORY.getName());
                		isSpendByCategoryType=true;
                	}
                }
                
                HSSFSheet sheet = workbook.createSheet("CrossTab");
                StringBuilder generatedBy = new StringBuilder();
                generatedBy.append("\n").append("\"").append(Joiner.on(",").join(selectedCrossTabSpends)).append("\"").append("\n");
                generatedBy.append(" ( ").append(currencyExchangeRate.getCurrencyCode()).append(" )").append("\n");
                String userName = getUser().getName();
                generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
                generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
                
                HSSFRow reportTypeHeader = sheet.createRow(startRow);
                HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(startColumn);
                reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
                reportTypeHeaderColumn.setCellValue(generatedBy.toString());
                
                // Create sheet row2
                HSSFRow userNameHeader = sheet.createRow(startRow + 2);
                HSSFCell userNameHeaderColumn = userNameHeader.createCell(startColumn);
                userNameHeaderColumn.setCellStyle(sheetHeaderCellStyle);
              //  userNameHeaderColumn.setCellValue("User: " + getUser().getFirstName() + " " + getUser().getLastName());

                Integer filtersRowCount = 0;

                if(filter != null && filter.getYears() != null && filter.getYears().size() > 0) {
                    HSSFRow yearsFilterRow = sheet.createRow(startRow + 3);
                    HSSFCell yearsFilterRowCell = yearsFilterRow.createCell(startColumn);
                    yearsFilterRowCell.setCellStyle(sheetHeaderCellStyle);
                //    yearsFilterRowCell.setCellValue("Year(s): "+StringUtils.join(filter.getYears(),", "));
                    filtersRowCount++;
                }


                
            	// Table Header row
                HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                String[] budgetYearsArr = years.split(",");
              //  String[] spendForSnapshotArr = spendForSnapshot.split(",");
                
                String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                
                List<String> spendForSnapShotList = new ArrayList<String>();
                
                boolean showLatestCost = false;
              
                
                for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                {
                	if(!spendForSnapshotArrOriginal[z].equals("6"))
                	{
                		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                	}
                   	else
                	{
                		showLatestCost = true;
                	}
                }
                //String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                
                int cellIndex = 0;
                
               // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),3,5));
               // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),6,9));
                
                int spendByColumns = 0;
                int totalCostColumns = 0;
                
                
                if(isSpendByBudgetLocation)
                {
                	spendByColumns=4;
                }
                if(isSpendByProject)
                {
                	spendByColumns=spendByColumns+2;
                }
                if(isSpendByMethodology)
                {
                	spendByColumns=spendByColumns+2;
                }
                if(isSpendByBrand)
                {
                	spendByColumns=spendByColumns+2;
                }
                if(isSpendByAgency || isSpendByKantarNonKantar)
                {
                	spendByColumns=spendByColumns+2;
                }
                else if(isSpendByKantarNonKantar)
                {
                	if(isSpendByBudgetLocation || isSpendByMethodology || isSpendByBrand)
                	{
                		
                	}
                	else
                	{
                		spendByColumns=spendByColumns+1;
                	}
                }
                if(isSpendByCategoryType)
                {
                	spendByColumns=spendByColumns+1;
                }
                
                cellIndex = spendByColumns;
                totalCostColumns = spendByColumns;
            	
                //int firstRowColumns = spendByColumns + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                
                int firstRowColumns = 0;
                if(showLatestCost)
                {
                	firstRowColumns = spendByColumns + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                }
                else
                {
                	firstRowColumns = spendByColumns + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                }
                
            	for(int m=0;m<firstRowColumns;m++)
            	{
            		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
            		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
            		budgetYearColumn0.setCellValue(" ");
            		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
            	}
        	
            	 totalCosts=new ArrayList<BigDecimal>();
                
                for(int i=0;i<budgetYearsArr.length;i++)
                {
                    
                	
                	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
                    budgetYearColumn0.setCellValue(budgetYearsArr[i]);
                    budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                 
                    /*
                    if(i==0)
                	{
                		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                	}
                	else
                	{
                		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                	}
                   
                    cellIndex = cellIndex+spendForSnapshotArr.length+1;
                    */
                    
                    if(showLatestCost)
                    {
                    	if(spendForSnapshotArr.length > 1)
                    	{
                    		sheet.addMergedRegion(new CellRangeAddress((startRow + 4 + filtersRowCount),(startRow + 4 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                    	}
                    }
                    else
                    {
                    	if(spendForSnapshotArr.length > 1)
                    	{
                    		sheet.addMergedRegion(new CellRangeAddress((startRow + 4 + filtersRowCount),(startRow + 4 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
                    	}
                    }
                    
                    if(showLatestCost)
                    {
                    	cellIndex = cellIndex+spendForSnapshotArr.length+1;
                    }
                    else
                    {
                    	cellIndex = cellIndex+spendForSnapshotArr.length;
                    }
                    
                }
                
                
                HSSFRow tableHeaderRow = sheet.createRow(startRow + 5 + filtersRowCount);
                
                tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                int initialColumnIndex = 0;
            	
                if(isSpendByProject)
                {
                	HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
 	                tableHeaderColumn0.setCellValue("Project Code");
 	                tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
 	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
 	                initialColumnIndex++;
 	                
 	               HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
                   tableHeaderColumn1.setCellValue("Project Name");
                   tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
                  // sheet.autoSizeColumn(startColumn);
                   sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                if(isSpendByMethodology)
                {
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
                	
	                tableHeaderColumn3.setCellValue("Methodology Group");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn2.setCellValue("Methodology");
	                tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                if(isSpendByCategoryType)
                {
	               
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
	                tableHeaderColumn3.setCellValue("Category");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	               
                }
                
                if(isSpendByBrand)
                {
	               // HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
	                tableHeaderColumn3.setCellValue("Brand Type");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn2.setCellValue("Brand");
	                tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                if(isSpendByAgency || isSpendByKantarNonKantar)
                {
	                //HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
	                tableHeaderColumn3.setCellValue("Agency");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn3.setCellValue("Kantar/Non-Kantar");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                else if (isSpendByKantarNonKantar)
                {
                	// If Spend By Budget Location, Spend By Methodology, Spend By Brand is selected for Crosstab then wont add the Spend By Kantar Non Kantar fields 
                	if(isSpendByBudgetLocation || isSpendByMethodology || isSpendByBrand)
                	{
                		
                	}
                	else
                	{
	                	HSSFCell tableHeaderColumn3 = null;
	                	if(startColumn==0 && initialColumnIndex==0)
		                {
	                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
	                		initialColumnIndex++;
		                }
		                else
		                {
		                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
		                }
	                	tableHeaderColumn3.setCellValue("Kantar/Non-Kantar");
	 	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	 	             //   sheet.autoSizeColumn(startColumn);
	 	               sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                }
                
                if(isSpendByBudgetLocation)
                {
	                
                	HSSFCell tableHeaderColumn0 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
                		tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn0 = tableHeaderRow.createCell(++startColumn);
	                	tableHeaderColumn0.setCellStyle(tableHeaderColumn2Style);
	                }
                	
                	//HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                tableHeaderColumn0.setCellValue("Region");
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	              
	                HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn1.setCellValue("Area");
	                tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	               // sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn2.setCellValue("T20, T40, or non-T40");
	                tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	
	                HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn3.setCellValue("Budget Location");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                
                
                
                
                
                
               
                
                HSSFCell costColumns;
                
                int budgetYearColumn=0;
                int dataStartColumn = 0;
                
                boolean isQPR1SpendFor=false;
                boolean isQPR2SpendFor=false;
                boolean isQPR3SpendFor=false;
                boolean isFullYearSpendFor=false;
                boolean isCOPLASpendFor=false;
                boolean isLatestCost=false;
                
                for(int i=0;i<budgetYearsArr.length;i++)
                {
                   
                	isQPR1SpendFor=false;
                    isQPR2SpendFor=false;
                    isQPR3SpendFor=false;
                    isFullYearSpendFor=false;
                    isCOPLASpendFor=false;
                    isLatestCost = false;
                    
                	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
                    {
                    	if(spendForSnapshotArrOriginal[j].equals("1"))
                    	{
                    		isCOPLASpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("2"))
                    	{
                    		isQPR1SpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("3"))
                    	{
                    		isQPR2SpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("4"))
                    	{
                    		isQPR3SpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("5"))
                    	{
                    		isFullYearSpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("6"))
                    	{
                    		isLatestCost=true;
                    	}
                    	
                		
                    }
                	
                	
                	
                	if(isCOPLASpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("COPLA");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isQPR1SpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("QPR1");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isQPR2SpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("QPR2");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isQPR3SpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("QPR3");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isFullYearSpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("FULL YEAR");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	
                	// Latest Cost will be shown only if it selected in UI
                	if(isLatestCost)
                	{
	                    costColumns = tableHeaderRow.createCell(++startColumn);
	                	costColumns.setCellValue("LATEST COST");
	                	costColumns.setCellStyle(tableHeaderColumn3Style);
	                	//sheet.autoSizeColumn(startColumn);
	                	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	
                	
                	
                	if(i==0)
                	{
                		startRow = startRow + 5 + filtersRowCount;
                	}
                	
                	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByCrossTab(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost,isSpendByProject, isSpendByBudgetLocation,isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                	//calculateTotaCost(spendByReportList, isQPR1SpendFor, 
            	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                	
                }
                    
                	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByCrossTab(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost,isSpendByProject, isSpendByBudgetLocation,isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                	Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByCrossTab(budgetYearsArr, currencyId,filter, isLatestCost,isSpendByProject, isSpendByBudgetLocation,isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                	
                	
                	if(spendByReportMap!=null && spendByReportMap.size()>0)
                	{
                		for(int i=0;i<budgetYearsArr.length;i++)
                        {
                    		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                        }
                	}
                	// This is done in case there are no records for the budget location with which the user is associated with 
                	else
                	{
                		for(int i=0;i<budgetYearsArr.length;i++)
                        {
                			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                        }
                	}
                	
                	if(spendByReportMap!=null && spendByReportMap.size()>0)
                  	{
                		//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                    	  //  		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                		
                		HSSFRow dataRow = null;
              			HSSFCell projectCodeDataRowColumn = null;
              			
              			for(String  spendByReportKey :spendByReportMap.keySet() )
                  		{
                			
                			dataStartColumn = 0;
                			dataRow = sheet.createRow(++startRow);
                			
                			
                			
                			Long projectId= null;
                      		String projectName = "";
                			Integer budgetLocation = null;
                			
                			String regionName = "";
              				String areaName = "";
              				String t20_40_Name = "";
              				String brandName = "";
                			String brandType  = "";
                			String categoryName = "";
                			String agencyName="";
                			String agencyType="";
                			
              				
                			
              				try
                			{
              					
              					projectId = new Long(getFieldName(spendByReportKey, "projectId", null, null, null, null, null,  null, null, null,  null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
                			}
                			catch(Exception e)
                			{
                				
                			}
                			try
                			{
                				projectName = getFieldName(spendByReportKey, null, "projectName", null, null, null, null, null,null, null , null,  null, null, null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
                			}
                			catch(Exception e)
                			{
                				
                			}
                			
              				try
                			{
                				budgetLocation = new Integer(getFieldName(spendByReportKey, null, null, "budgetLocation", null, null, null, null,null, null , null,  null, null, null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
                			}
                			catch(Exception e)
                			{
                				
                			}
              				try
              				{
                				 regionName = getFieldName(spendByReportKey, null, null, null, "region", null, null,null, null , null,  null, null, null,null,  isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
              				}
              				catch(Exception e)
              				{
              					
              				}
              				try
              				{
              					areaName = getFieldName(spendByReportKey, null, null, null, null, "area",null, null, null , null,  null, null, null,null,  isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
              				}
              				catch(Exception e)
              				{
              					
              				}
              				try
              				{
              					t20_40_Name = getFieldName(spendByReportKey, null, null, null, null, null ,"t20_40", null, null , null,  null, null, null,null,  isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
              				}
              				catch(Exception e)
              				{
              					
              				}
                			
              				String meth = ""; 
          					try
          					{
          						meth =	getFieldName(spendByReportKey, null, null, null, null, null , null, "methodology", null , null,  null, null, null,null,  isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}
                  			String methGroup = ""; 
                  			try
          					{
                  				methGroup = getFieldName(spendByReportKey, null, null, null, null, null , null,  null, "methGroup" , null,  null, null, null,null,  isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}	
                  			try
          					{
                  				 brandName = getFieldName(spendByReportKey, null, null, null, null, null , null,  null,  null, "brand" , null, null, null,null,  isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}	
                  			try
          					{
                  				brandType  = getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  "brandType", null, null, null,  isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}	
                  			
                  			try
              				{
                				 categoryName = SynchroDAOUtil.getCategoryNames(getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  null, "categoryType", null, null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
              				}
              				catch(Exception e)
              				{
              					
              				}
                  			
                  			try
              				{
                				//Integer aid = new Integer(spendByReportKey.split("~")[11]);
                  				Integer aid = new Integer(getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  null, null, "agency", null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
                  				//agencyName = SynchroGlobal.getResearchAgency().get(aid);
                  				agencyName = SynchroGlobal.getAllResearchAgency().get(aid);
                  				
                  				agencyType = getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  null, null, null, "agencyType", isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
                  				/*if(SynchroGlobal.getResearchAgencyGroupFromAgency(aid)!=null && SynchroGlobal.getResearchAgencyGroupFromAgency(aid)==1)
                				{
                  					agencyType = "Kantar";
                				}
                				else if(SynchroGlobal.getResearchAgencyGroupFromAgency(aid)!=null && SynchroGlobal.getResearchAgencyGroupFromAgency(aid)==2)
                				{
                					agencyType = "Non-Kantar";
                				}
                				else if(SynchroGlobal.getResearchAgencyGroupFromAgency(aid)!=null && SynchroGlobal.getResearchAgencyGroupFromAgency(aid)==3)
                				{
                					agencyType = "Non Classified";
                				}*/
                				
              				}
              				catch(Exception e)
              				{
              					
              				}
                  			
                			if(isSpendByProject)
                			{
                				projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
	                            
	                            if(projectId!=null)
	                            {
	                            	projectCodeDataRowColumn.setCellValue(projectId);
	                            }
	                            else
	                            {
	                            	//projectCodeDataRowColumn.setCellValue(" ");
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
    	                            projectCodeDataRowColumn.setCellValue("-");
    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                          
	                         
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                dataStartColumn++;
                                
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                                
                                
                                if(StringUtils.isNotBlank(projectName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(projectName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                               
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                dataStartColumn++;
                			}
                			
                			if(isSpendByMethodology)
                            {
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                           	
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            if(StringUtils.isNotBlank(methGroup))
	                            {
	                        	   projectCodeDataRowColumn.setCellValue(methGroup);
	                            }
	                            else
	                            {
	                        	   	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                
	                           
	                            dataStartColumn++;
                            	
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            if(StringUtils.isNotBlank(meth))
                                {
                                	projectCodeDataRowColumn.setCellValue(SynchroDAOUtil.getMethodologyNames(meth));
                                }
                                else
                                {
                                	//projectCodeDataRowColumn.setCellValue(" ");
                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
    	                            projectCodeDataRowColumn.setCellValue("-");
    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
                                }
	                           
	                            dataStartColumn++;
                            }
                			
                			if(isSpendByCategoryType)
                            {
	                            
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            
	                           // projectCodeDataRowColumn.setCellValue(categoryName);
	                            
	                            if(StringUtils.isNotBlank(categoryName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(categoryName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                           
	                            dataStartColumn++;
	                        	
                            }
                			
                			
                			if(isSpendByBrand)
                            {
	                            
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                           
	                            if(StringUtils.isNotBlank(brandType))
	                           	{
	                           		projectCodeDataRowColumn.setCellValue(brandType);
	                           	}
	                           	else
	                           	{
	                           		projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                           	}
	                          
	                          
	                            dataStartColumn++;
	                            
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            if(StringUtils.isNotBlank(brandName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(brandName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                          /*  if(spendByBean.getBrandNonBrandName()!=null)
	                            {
	                            	projectCodeDataRowColumn.setCellValue(spendByBean.getBrandNonBrandName());
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellValue("");
	                            }*/
	                            
	                            dataStartColumn++;
                            }
                            
                            if(isSpendByAgency || isSpendByKantarNonKantar)
                            {
	                            projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                          
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            
	                            if(StringUtils.isNotBlank(agencyName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(agencyName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                            dataStartColumn++;
	                            
	                            projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                           
	                          //  projectCodeDataRowColumn.setCellValue(agencyType);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            
	                            if(StringUtils.isNotBlank(agencyType))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(agencyType);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                           
	                            dataStartColumn++;
                            }
                            else if(isSpendByKantarNonKantar)
                            {
                            	if(isSpendByBudgetLocation || isSpendByMethodology || isSpendByBrand)
                            	{
                            		
                            	}
                            	else
                            	{
	                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	 	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	 	                           
	 	                          //  projectCodeDataRowColumn.setCellValue(spendByBean.getResearchAgecnyType());
	 	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	 	                            dataStartColumn++;
                            	}
                            }
                            
                            
                			if(isSpendByBudgetLocation)
                			{
	                			
	                			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                			sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                           
	                            
	                            if(StringUtils.isNotBlank(regionName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(regionName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                              
                                dataStartColumn++;
                                
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                               
                                if(StringUtils.isNotBlank(areaName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(areaName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                
                                dataStartColumn++;
	                            
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                               
                               
                                if(StringUtils.isNotBlank(t20_40_Name))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(t20_40_Name);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                dataStartColumn++;
	                            
	                            
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                    			sheet.setColumnWidth(dataStartColumn, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                                                               
                                if(budgetLocation!=null)
	                            {
	                            	//projectCodeDataRowColumn.setCellValue(budgetLocation);
	                            	projectCodeDataRowColumn.setCellValue(SynchroUtils.getBudgetLocationName(new Integer(budgetLocation)));
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                dataStartColumn++;
                			}
                            
                            
                            
                            
                            
                            
                           /* if(i>0)
                            {
                            	int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                            	for(int k=0;k<noOfColumnsToDecorate;k++)
                            	{
                            		projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                                projectCodeDataRowColumn.setCellValue(" ");
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                dataStartColumn++;
                            	}
                            	dataStartColumn=budgetYearColumn;
                            }
                           */
                            
                            int noOfColumnsToDecorate = 0;
                            if(isLatestCost)
                            {
                            	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                            }
                            else
                            {
                            	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
                            }
                            
                            int decCol = dataStartColumn;
                        	for(int k=0;k<noOfColumnsToDecorate;k++)
                        	{
                        		projectCodeDataRowColumn = dataRow.createCell(decCol);
                        		sheet.setColumnWidth(decCol, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellValue("-");
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
                                decCol++;
                        	}
                        	int snapShotRef = 0;
                        	
                        	for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
		                     {
	                        		
	                            if(isCOPLASpendFor)
	                            {
	                               
	                                
	                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                
	                                if(spendByBean.getCoplaTotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("1"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	
	                                	
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                               
	                                
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            
	                            if(isQPR1SpendFor)
	                            {
	                            	 projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
	                                if(spendByBean.getQpr1TotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("2"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            if(isQPR2SpendFor)
	                            {
	                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                
	                                if(spendByBean.getQpr2TotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("3"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            
	                            if(isQPR3SpendFor)
	                            {
	                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                
	                                if(spendByBean.getQpr3TotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("4"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	/*if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                	*/
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            if(isFullYearSpendFor)
	                            {
	                            	 projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                             sheet.setColumnWidth(dataStartColumn, columnWidth);
		                             projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                
		                             projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
	                                if(spendByBean.getFullYearTotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("5"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                	/*
	                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}*/
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            
	                            if(isLatestCost)
                                {
	                            	
                                
	                            	 projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                             sheet.setColumnWidth(dataStartColumn, columnWidth);
		                             projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                             projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                            if(spendByBean.getLatestTotalCost()!=null)
		                            {
		                            	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                            	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                            }
		                            else
		                            {
		                            	projectCodeDataRowColumn.setCellValue("-");
                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                            	/*if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}*/
		                            }
		                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
                                }
	                            
	                            /*if(i==0)
	                            {
	                            	int noOfColumnsToDecorate = (budgetYearsArr.length-1) * (spendForSnapshotArr.length+1);
	                            	if(noOfColumnsToDecorate>0)
	                            	{
	                            		int columnLocation = dataStartColumn;
	                            		for(int k=0;k<noOfColumnsToDecorate;k++)
	                                	{
	                            			projectCodeDataRowColumn = dataRow.createCell(columnLocation);
	    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                                projectCodeDataRowColumn.setCellValue(" ");
	    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	    	                                columnLocation++;
	                                	}
	                            	}
	                            }*/
	                            
	                		}
                	/*	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor); */	
                	}
                	budgetYearColumn = dataStartColumn;
                }
                
                // This is for merging the top row
                if(dataStartColumn > 0)
                {
                	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
                	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(dataStartColumn -1)));
                }
                else
                {
                	
                	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(startColumn)));
                	sheet.addMergedRegion(new CellRangeAddress(4,8,0,(startColumn)));
                }
                
                if(spendByReportMap!=null && spendByReportMap.size()>0)
            	{
            		
            	}
            	// This is done in case there are no records for the budget location with which the user is associated with 
            	else
            	{
            		sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCostColumns)));
            	}
             
                HSSFRow dataRow = sheet.createRow(++startRow);
                HSSFCell totalCostColumn = dataRow.createCell(0);
                totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                totalCostColumn.setCellValue("Total Cost");
                totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
               
                for(int l=1;l<totalCostColumns;l++)
                {
                	 totalCostColumn = dataRow.createCell(l);
                     totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                     totalCostColumn.setCellValue(" ");
                     totalCostColumn.setCellStyle(dataRowColumn2Style);
                   /*  totalCostColumn = dataRow.createCell(2);
                     totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                     totalCostColumn.setCellValue(" ");
                     totalCostColumn.setCellStyle(dataRowColumn2Style);
                     totalCostColumn = dataRow.createCell(3);
                     totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                     totalCostColumn.setCellValue(" ");
                     totalCostColumn.setCellStyle(dataRowColumn2Style);*/
                }
                
               
                
                if(totalCostColumns>0)
                {
                	sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,totalCostColumns-1));
                }
                
               // int totalCostColumnIndex = 4;
                int totalCostColumnIndex = totalCostColumns;
                for(int i=0;i<totalCosts.size();i++)
                {
                	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                	
                	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                	totalCostColumn.setCellStyle(totalCostRowTotalColumnStyle);
                	totalCostColumnIndex++;
                	sheet.autoSizeColumn(totalCostColumnIndex);
                	
                }
            
                //Notes Region
                
                if(dataStartColumn > 0)
                {
                	
                	dataRow = sheet.createRow(startRow+3);
                	if(isSpendByAgency || isSpendByKantarNonKantar)
                	{
                		//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+18,0,dataStartColumn -1));
                		CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+16,0,dataStartColumn -1);
                		sheet.addMergedRegion(mergedRegion);
                    	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                		
                	}
                	else
                	{
                		//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,dataStartColumn -1));
                		CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,dataStartColumn -1);
                		sheet.addMergedRegion(mergedRegion);
                    	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                	}
                	
                	
                	StringBuilder notes = new StringBuilder();
                	notes.append("Notes:").append("\n");
                	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
                	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
                	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
                	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
                	notes.append("- Cancelled projects are not included in the above report.").append("\n");
                	if(isSpendByAgency || isSpendByKantarNonKantar)
                	{
                		notes.append("- Costs extracted from this report may slightly differ from the actual project cost due to currency conversion.").append("\n");
                	}
                	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
                	HSSFCell notesColumn = dataRow.createCell(0);
                	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                	notesColumn.setCellValue(notes.toString());
                	notesColumn.setCellStyle(notesStyle);
                }
                else
                {
                	dataRow = sheet.createRow(startRow+3);
                	if(isSpendByAgency || isSpendByKantarNonKantar)
                	{
                		//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+18,0,dataStartColumn -1));
                		CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+16,0,startColumn);
                		sheet.addMergedRegion(mergedRegion);
                    	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                		
                	}
                	else
                	{
                		//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+17,0,dataStartColumn -1));
                		CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+15,0,startColumn);
                		sheet.addMergedRegion(mergedRegion);
                    	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                        RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
                	}
                	
                	
                	StringBuilder notes = new StringBuilder();
                	notes.append("Notes:").append("\n");
                	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
                	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
                	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence, it is not available.").append("\n");
                	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
                	notes.append("- Cancelled projects are not included in the above report.").append("\n");
                	if(isSpendByAgency || isSpendByKantarNonKantar)
                	{
                		notes.append("- Costs extracted from this report may slightly differ from the actual project cost due to currency conversion.").append("\n");
                	}
                	notes.append("- Some projects may have been categorized differently and have different Synchro codes now. Please contact admin team if you have any questions on this.").append("\n");
                	HSSFCell notesColumn = dataRow.createCell(0);
                	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                	notesColumn.setCellValue(notes.toString());
                	notesColumn.setCellStyle(notesStyle);
                }
                
             // Commenting the logo for Spend Reports
                workbook = SynchroUtils.createExcelImage(workbook, sheet);
            }
        }

    	
        return workbook;
    
    }
    private void createEmptyDataRow(final HSSFSheet sheet,final Integer rowCount,final Integer startColumn, final HSSFCellStyle dataRowColumn1Style,
                                    final HSSFCellStyle dataRowColumn2Style, final HSSFCellStyle dataRowColumn3Style,
                                    final HSSFCellStyle dataRowColumn4Style, final HSSFCellStyle dataRowColumn5Style,
                                    final String label, final HSSFFont font) {
        HSSFRow emptyDataRow = sheet.createRow(rowCount);
        HSSFCell emptyDataRowCell1 = emptyDataRow.createCell(startColumn);
        if(label != null) {
            HSSFRichTextString textString = new HSSFRichTextString(label);
            if(font != null) {
                textString.applyFont(0, label.length(), font);
            }
            emptyDataRowCell1.setCellValue(textString);
        }
        emptyDataRowCell1.setCellStyle(dataRowColumn1Style);
        HSSFCell emptyDataRowCell2 = emptyDataRow.createCell(startColumn + 1);
        emptyDataRowCell2.setCellStyle(dataRowColumn2Style);
        HSSFCell emptyDataRowCell3 = emptyDataRow.createCell(startColumn + 2);
        emptyDataRowCell3.setCellStyle(dataRowColumn3Style);
        HSSFCell emptyDataRowCell4 = emptyDataRow.createCell(startColumn + 3);
        emptyDataRowCell4.setCellStyle(dataRowColumn4Style);
        HSSFCell emptyDataRowCell5 = emptyDataRow.createCell(startColumn + 4);
        emptyDataRowCell5.setCellStyle(dataRowColumn5Style);
    }

    private void calculateTotaCost(List<SpendByReportBean> spendByReportList, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor)
    {
    	
    	
    	BigDecimal coplaTotalCost = new BigDecimal("0");
    	BigDecimal qpr1TotalCost = new BigDecimal("0");
    	BigDecimal qpr2TotalCost = new BigDecimal("0");
    	BigDecimal qpr3TotalCost = new BigDecimal("0");
    	BigDecimal fullYearTotalCost = new BigDecimal("0");
    	BigDecimal latestTotalCost = new BigDecimal("0");
    	
    	for(SpendByReportBean spendByBean: spendByReportList)
    	{
    		if(spendByBean.getCoplaTotalCost()!=null)
    		{
    			coplaTotalCost=coplaTotalCost.add(spendByBean.getCoplaTotalCost());
    		}
    		if(spendByBean.getQpr1TotalCost()!=null)
    		{
    			qpr1TotalCost=qpr1TotalCost.add(spendByBean.getQpr1TotalCost());
    		}
    		if(spendByBean.getQpr2TotalCost()!=null)
    		{
    			qpr2TotalCost=qpr2TotalCost.add(spendByBean.getQpr2TotalCost());
    		}
    		if(spendByBean.getQpr3TotalCost()!=null)
    		{
    			qpr3TotalCost=qpr3TotalCost.add(spendByBean.getQpr3TotalCost());
    		}
    		if(spendByBean.getFullYearTotalCost()!=null)
    		{
    			fullYearTotalCost=fullYearTotalCost.add(spendByBean.getFullYearTotalCost());
    		}
    		if(spendByBean.getLatestTotalCost()!=null)
    		{
    			latestTotalCost=latestTotalCost.add(spendByBean.getLatestTotalCost());
    		}
    		
    	}
    	if(isCOPLASpendFor)
		{
			totalCosts.add(coplaTotalCost);
		}
    	if(isQPR1SpendFor)
		{
			totalCosts.add(qpr1TotalCost);
		}
    	if(isQPR2SpendFor)
		{
			totalCosts.add(qpr2TotalCost);
		}
    	if(isQPR3SpendFor)
		{
			totalCosts.add(qpr3TotalCost);
		}
    	if(isFullYearSpendFor)
		{
			totalCosts.add(fullYearTotalCost);
		}
    	totalCosts.add(latestTotalCost);
    }
    
    private void calculateTotaCost(List<SpendByReportBean> spendByReportList, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor, boolean isLatestCost)
    {
    	
    	
    	BigDecimal coplaTotalCost = new BigDecimal("0");
    	BigDecimal qpr1TotalCost = new BigDecimal("0");
    	BigDecimal qpr2TotalCost = new BigDecimal("0");
    	BigDecimal qpr3TotalCost = new BigDecimal("0");
    	BigDecimal fullYearTotalCost = new BigDecimal("0");
    	BigDecimal latestTotalCost = new BigDecimal("0");
    	
    	for(SpendByReportBean spendByBean: spendByReportList)
    	{
    		if(spendByBean.getCoplaTotalCost()!=null)
    		{
    			coplaTotalCost=coplaTotalCost.add(spendByBean.getCoplaTotalCost());
    		}
    		if(spendByBean.getQpr1TotalCost()!=null)
    		{
    			qpr1TotalCost=qpr1TotalCost.add(spendByBean.getQpr1TotalCost());
    		}
    		if(spendByBean.getQpr2TotalCost()!=null)
    		{
    			qpr2TotalCost=qpr2TotalCost.add(spendByBean.getQpr2TotalCost());
    		}
    		if(spendByBean.getQpr3TotalCost()!=null)
    		{
    			qpr3TotalCost=qpr3TotalCost.add(spendByBean.getQpr3TotalCost());
    		}
    		if(spendByBean.getFullYearTotalCost()!=null)
    		{
    			fullYearTotalCost=fullYearTotalCost.add(spendByBean.getFullYearTotalCost());
    		}
    		if(spendByBean.getLatestTotalCost()!=null)
    		{
    			latestTotalCost=latestTotalCost.add(spendByBean.getLatestTotalCost());
    		}
    		
    	}
    	if(isCOPLASpendFor)
		{
			totalCosts.add(coplaTotalCost);
		}
    	if(isQPR1SpendFor)
		{
			totalCosts.add(qpr1TotalCost);
		}
    	if(isQPR2SpendFor)
		{
			totalCosts.add(qpr2TotalCost);
		}
    	if(isQPR3SpendFor)
		{
			totalCosts.add(qpr3TotalCost);
		}
    	if(isFullYearSpendFor)
		{
			totalCosts.add(fullYearTotalCost);
		}
    	if(isLatestCost)
    	{
    		totalCosts.add(latestTotalCost);
    	}
    }
    
    private void calculateTotalCost(Map<String, List<SpendByReportBean>> spendByReportMap, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor, boolean isLatestCost)
    {
    	
    	
    	BigDecimal coplaTotalCost = new BigDecimal("0");
    	BigDecimal qpr1TotalCost = new BigDecimal("0");
    	BigDecimal qpr2TotalCost = new BigDecimal("0");
    	BigDecimal qpr3TotalCost = new BigDecimal("0");
    	BigDecimal fullYearTotalCost = new BigDecimal("0");
    	BigDecimal latestTotalCost = new BigDecimal("0");
    	
    	for(String  spendByReportKey :spendByReportMap.keySet() )
    	{
    		for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
	    	{
	    		if(spendByBean.getCoplaTotalCost()!=null)
	    		{
	    			coplaTotalCost=coplaTotalCost.add(spendByBean.getCoplaTotalCost());
	    		}
	    		if(spendByBean.getQpr1TotalCost()!=null)
	    		{
	    			qpr1TotalCost=qpr1TotalCost.add(spendByBean.getQpr1TotalCost());
	    		}
	    		if(spendByBean.getQpr2TotalCost()!=null)
	    		{
	    			qpr2TotalCost=qpr2TotalCost.add(spendByBean.getQpr2TotalCost());
	    		}
	    		if(spendByBean.getQpr3TotalCost()!=null)
	    		{
	    			qpr3TotalCost=qpr3TotalCost.add(spendByBean.getQpr3TotalCost());
	    		}
	    		if(spendByBean.getFullYearTotalCost()!=null)
	    		{
	    			fullYearTotalCost=fullYearTotalCost.add(spendByBean.getFullYearTotalCost());
	    		}
	    		if(spendByBean.getLatestTotalCost()!=null)
	    		{
	    			latestTotalCost=latestTotalCost.add(spendByBean.getLatestTotalCost());
	    		}
	    		
	    	}
    	}
    	if(isCOPLASpendFor)
		{
			totalCosts.add(coplaTotalCost);
		}
    	if(isQPR1SpendFor)
		{
			totalCosts.add(qpr1TotalCost);
		}
    	if(isQPR2SpendFor)
		{
			totalCosts.add(qpr2TotalCost);
		}
    	if(isQPR3SpendFor)
		{
			totalCosts.add(qpr3TotalCost);
		}
    	if(isFullYearSpendFor)
		{
			totalCosts.add(fullYearTotalCost);
		}
    	if(isLatestCost)
    	{
    		totalCosts.add(latestTotalCost);
    	}
    }
    
    private void calculateTotalCostBY(Map<String, List<SpendByReportBean>> spendByReportMap, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor, boolean isLatestCost, Integer budgetYear)
    {
    	
    	
    	Long coplaSnapShotId = qprSnapshotManager.getSnapShotId(new Integer("1"), budgetYear);
		Long qpr1SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("2"), budgetYear);
		Long qpr2SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("3"), budgetYear);
		Long qpr3SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("4"), budgetYear);
		Long fullYearSnapShotId = qprSnapshotManager.getSnapShotId(new Integer("5"), budgetYear);
    	
    	
    	BigDecimal coplaTotalCost = null;
    	BigDecimal qpr1TotalCost = null;
    	BigDecimal qpr2TotalCost = null;
    	BigDecimal qpr3TotalCost = null;
    	BigDecimal fullYearTotalCost = null;
    	BigDecimal latestTotalCost = null;
    	
    	if(coplaSnapShotId!=null && coplaSnapShotId.intValue() > 0 )
    	{
    		
    	}
    	else
    	{
    		coplaTotalCost = new BigDecimal("0");
    	}
    	
    	if(qpr1SnapShotId!=null && qpr1SnapShotId.intValue() > 0 )
    	{
    		
    	}
    	else
    	{
    		qpr1TotalCost = new BigDecimal("0");
    	}
    	
    	if(qpr2SnapShotId!=null && qpr2SnapShotId.intValue() > 0 )
    	{
    		
    	}
    	else
    	{
    		qpr2TotalCost = new BigDecimal("0");
    	}
    	
    	if(qpr3SnapShotId!=null && qpr3SnapShotId.intValue() > 0 )
    	{
    		
    	}
    	else
    	{
    		qpr3TotalCost = new BigDecimal("0");
    	}
    	
    	if(fullYearSnapShotId!=null && fullYearSnapShotId.intValue() > 0 )
    	{
    		
    	}
    	else
    	{
    		fullYearTotalCost = new BigDecimal("0");
    	}
    	
    	for(String  spendByReportKey :spendByReportMap.keySet() )
    	{
    		for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
	    	{
	    		if(spendByBean!=null && spendByBean.getBudgetYear()!=null && budgetYear.intValue() == spendByBean.getBudgetYear().intValue())
	    		{
	    			if(spendByBean.getCoplaTotalCost()!=null && coplaSnapShotId!=null && coplaSnapShotId.intValue() > 0)
		    		{
		    			if(coplaTotalCost==null)
		    			{
		    				coplaTotalCost = new BigDecimal("0");
		    			}
	    				coplaTotalCost=coplaTotalCost.add(spendByBean.getCoplaTotalCost());
		    		}
		    		if(spendByBean.getQpr1TotalCost()!=null && qpr1SnapShotId!=null && qpr1SnapShotId.intValue() > 0)
		    		{
		    			if(qpr1TotalCost==null)
		    			{
		    				qpr1TotalCost = new BigDecimal("0");
		    			}
		    			qpr1TotalCost=qpr1TotalCost.add(spendByBean.getQpr1TotalCost());
		    		}
		    		if(spendByBean.getQpr2TotalCost()!=null && qpr2SnapShotId!=null && qpr2SnapShotId.intValue() > 0 )
		    		{
		    			if(qpr2TotalCost==null)
		    			{
		    				qpr2TotalCost = new BigDecimal("0");
		    			}
		    			qpr2TotalCost=qpr2TotalCost.add(spendByBean.getQpr2TotalCost());
		    		}
		    		if(spendByBean.getQpr3TotalCost()!=null && qpr3SnapShotId!=null && qpr3SnapShotId.intValue() > 0)
		    		{
		    			if(qpr3TotalCost==null)
		    			{
		    				qpr3TotalCost = new BigDecimal("0");
		    			}
		    			qpr3TotalCost=qpr3TotalCost.add(spendByBean.getQpr3TotalCost());
		    		}
		    		if(spendByBean.getFullYearTotalCost()!=null && fullYearSnapShotId!=null && fullYearSnapShotId.intValue()  > 0)
		    		{
		    			if(fullYearTotalCost==null)
		    			{
		    				fullYearTotalCost = new BigDecimal("0");
		    			}
		    			fullYearTotalCost=fullYearTotalCost.add(spendByBean.getFullYearTotalCost());
		    		}
		    		if(spendByBean.getLatestTotalCost()!=null)
		    		{
		    			if(latestTotalCost==null)
		    			{
		    				latestTotalCost = new BigDecimal("0");
		    			}
		    			latestTotalCost=latestTotalCost.add(spendByBean.getLatestTotalCost());
		    		}
	    		}
		    		
	    	}
    	}
    	if(isCOPLASpendFor)
		{
			//totalCosts.add(coplaTotalCost);
			
			if(coplaSnapShotId!=null && coplaSnapShotId.intValue() > 0 )
			{
				totalCosts.add(coplaTotalCost);
				
				
			}
			else
			{
				totalCosts.add(coplaTotalCost);
			}
			
		}
    	if(isQPR1SpendFor)
		{
			//totalCosts.add(qpr1TotalCost);
			if(qpr1SnapShotId!=null && qpr1SnapShotId.intValue() > 0 )
			{
				totalCosts.add(qpr1TotalCost);
				
			}
			else
			{
				totalCosts.add(qpr1TotalCost);
				
			}
		}
    	if(isQPR2SpendFor)
		{
			//totalCosts.add(qpr2TotalCost);
			
			if(qpr2TotalCost!=null && qpr2TotalCost.intValue() > 0 )
			{
				
				totalCosts.add(qpr2TotalCost);
				
			}
			else
			{
				totalCosts.add(qpr2TotalCost);
			}
		}
    	if(isQPR3SpendFor)
		{
			//totalCosts.add(qpr3TotalCost);
    		
			if(qpr3TotalCost!=null && qpr3TotalCost.intValue() > 0 )
			{
				totalCosts.add(qpr3TotalCost);
				
			}
			else
			{
				
				totalCosts.add(qpr3TotalCost);
			}
		}
    	if(isFullYearSpendFor)
		{
			//totalCosts.add(fullYearTotalCost);
    		
			if(fullYearTotalCost!=null && fullYearTotalCost.intValue() > 0 )
			{
				totalCosts.add(fullYearTotalCost);
				
				
			}
			else
			{
				totalCosts.add(fullYearTotalCost);
			}
		}
    	if(isLatestCost)
    	{
    		totalCosts.add(latestTotalCost);
    	}
    }
    
    
    private void calculateTotalCostAgencyTypeBY(Map<String, List<SpendByReportBean>> spendByReportMap, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor, boolean isLatestCost, Integer budgetYear)
    {
    	
    	
    	BigDecimal coplaTotalCost = null;
    	BigDecimal qpr1TotalCost = null;
    	BigDecimal qpr2TotalCost = null;
    	BigDecimal qpr3TotalCost = null;
    	BigDecimal fullYearTotalCost = null;
    	BigDecimal latestTotalCost = null;
    	
    	for(String  spendByReportKey :spendByReportMap.keySet() )
    	{
    		for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
	    	{
	    		if(spendByBean!=null && spendByBean.getBudgetYear()!=null && budgetYear.intValue() == spendByBean.getBudgetYear().intValue())
	    		{
	    			if(spendByBean.getCoplaTotalCost()!=null)
		    		{
	    				if(coplaTotalCost==null)
	    				{
	    					coplaTotalCost = new BigDecimal("0");
	    				}
	    				coplaTotalCost=coplaTotalCost.add(spendByBean.getCoplaTotalCost());
		    		}
		    		if(spendByBean.getQpr1TotalCost()!=null)
		    		{
		    			if(qpr1TotalCost==null)
	    				{
		    				qpr1TotalCost = new BigDecimal("0");
	    				}
		    			qpr1TotalCost=qpr1TotalCost.add(spendByBean.getQpr1TotalCost());
		    		}
		    		if(spendByBean.getQpr2TotalCost()!=null)
		    		{
		    			if(qpr2TotalCost==null)
	    				{
		    				qpr2TotalCost = new BigDecimal("0");
	    				}
		    			qpr2TotalCost=qpr2TotalCost.add(spendByBean.getQpr2TotalCost());
		    		}
		    		if(spendByBean.getQpr3TotalCost()!=null)
		    		{
		    			if(qpr3TotalCost==null)
	    				{
		    				qpr3TotalCost = new BigDecimal("0");
	    				}
		    			qpr3TotalCost=qpr3TotalCost.add(spendByBean.getQpr3TotalCost());
		    		}
		    		if(spendByBean.getFullYearTotalCost()!=null)
		    		{
		    			if(fullYearTotalCost==null)
	    				{
		    				fullYearTotalCost = new BigDecimal("0");
	    				}
		    			fullYearTotalCost=fullYearTotalCost.add(spendByBean.getFullYearTotalCost());
		    		}
		    		if(spendByBean.getLatestTotalCost()!=null)
		    		{
		    			
		    			if(latestTotalCost==null)
	    				{
		    				latestTotalCost = new BigDecimal("0");
	    				}
		    			latestTotalCost=latestTotalCost.add(spendByBean.getLatestTotalCost());
		    		}
	    		}
		    		
	    	}
    	}
    	if(isCOPLASpendFor)
		{
			totalCosts.add(coplaTotalCost);
		}
    	if(isQPR1SpendFor)
		{
			totalCosts.add(qpr1TotalCost);
		}
    	if(isQPR2SpendFor)
		{
			totalCosts.add(qpr2TotalCost);
		}
    	if(isQPR3SpendFor)
		{
			totalCosts.add(qpr3TotalCost);
		}
    	if(isFullYearSpendFor)
		{
			totalCosts.add(fullYearTotalCost);
		}
    	if(isLatestCost)
    	{
    		totalCosts.add(latestTotalCost);
    	}
    }
    
    private void calculateTotalCostNoBY(Map<String, List<SpendByReportBean>> spendByReportMap, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor, boolean isLatestCost, Integer budgetYear)
    {
    	
    	Long coplaSnapShotId = qprSnapshotManager.getSnapShotId(new Integer("1"), budgetYear);
		Long qpr1SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("2"), budgetYear);
		Long qpr2SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("3"), budgetYear);
		Long qpr3SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("4"), budgetYear);
		Long fullYearSnapShotId = qprSnapshotManager.getSnapShotId(new Integer("5"), budgetYear);
    	
    	BigDecimal coplaTotalCost = new BigDecimal("0");
    	BigDecimal qpr1TotalCost = new BigDecimal("0");
    	BigDecimal qpr2TotalCost = new BigDecimal("0");
    	BigDecimal qpr3TotalCost = new BigDecimal("0");
    	BigDecimal fullYearTotalCost = new BigDecimal("0");
    	BigDecimal latestTotalCost = new BigDecimal("0");
    	
    	
    	if(isCOPLASpendFor)
		{
			if(coplaSnapShotId!=null && coplaSnapShotId.intValue() > 0 )
			{
				totalCosts.add(null);
				
			}
			else
			{
				totalCosts.add(coplaTotalCost);
			}
		}
    	if(isQPR1SpendFor)
		{
			
			if(qpr1SnapShotId!=null && qpr1SnapShotId.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(qpr1TotalCost);
			}
		}
    	if(isQPR2SpendFor)
		{
			if(qpr2TotalCost!=null && qpr2TotalCost.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(qpr2TotalCost);
			}
		}
    	if(isQPR3SpendFor)
		{
			if(qpr3TotalCost!=null && qpr3TotalCost.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(qpr3TotalCost);
			}
		}
    	if(isFullYearSpendFor)
		{
			if(fullYearTotalCost!=null && fullYearTotalCost.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(fullYearTotalCost);
			}
		}
    	if(isLatestCost)
    	{
    		totalCosts.add(null);
    	}
    }
    
   
    private SpendReportExtractFilter getSpendReportFilter() {
        SpendReportExtractFilter filter = new SpendReportExtractFilter();
//        if(years != null && !years.equals("")) {
//            String [] yearsArr = years.split(",");
//            List<Integer> yearsFilter = new ArrayList<Integer>();
//            for(String yrStr : yearsArr) {
//                yearsFilter.add(Integer.parseInt(yrStr));
//            }
//            filter.setYears(yearsFilter);
//        }

        if(regions != null && !regions.equals("")) {
            String [] regionsArr = regions.split(",");
            List<Long> regionsFilter = new ArrayList<Long>();
            for(String rgStr : regionsArr) {
                regionsFilter.add(Long.parseLong(rgStr));
            }
            filter.setRegions(regionsFilter);
        }

        if(countries != null && !countries.equals("")) {
            String [] countriesArr = countries.split(",");
            List<Long> countriesFilter = new ArrayList<Long>();
            for(String cnStr : countriesArr) {
                countriesFilter.add(Long.parseLong(cnStr));
            }
            filter.setCountries(countriesFilter);
        }

        if(marketTypes != null && !marketTypes.equals("")) {
            String [] marketTypesArr = marketTypes.split(",");
            List<Long> marketTypesFilter = new ArrayList<Long>();
            for(String mtStr : marketTypesArr) {
                marketTypesFilter.add(Long.parseLong(mtStr));
            }
            filter.setMarketTypes(marketTypesFilter);
        }
        if(currencyId != null && currencyId > 0) {
            filter.setCurrencyId(currencyId);
        }
        
        if(budgetLocationsFilter != null && !budgetLocationsFilter.equals("")) {
            String [] budgetLocationsArr = budgetLocationsFilter.split(",");
            List<Long> blocFilter = new ArrayList<Long>();
            for(String bl : budgetLocationsArr) {
            	blocFilter.add(Long.parseLong(bl));
            }
            filter.setBudgetLocations(blocFilter);
        }
        
        if(methDetailsFilter != null && !methDetailsFilter.equals("")) {
            String [] methDetailsArr = methDetailsFilter.split(",");
            List<Long> methDetails = new ArrayList<Long>();
            for(String md : methDetailsArr) {
            	methDetails.add(Long.parseLong(md));
            }
            filter.setMethDetails(methDetails);
        }
        
        if(brandsFilter != null && !brandsFilter.equals("")) {
            String [] brandsArr = brandsFilter.split(",");
            List<Long> brandsList = new ArrayList<Long>();
            for(String brand : brandsArr) {
            	brandsList.add(Long.parseLong(brand));
            }
            filter.setBrands(brandsList);
        }
        
        return filter;
    }

    private boolean isSpendByProjectsReport(final Integer reportType) {
        boolean spendByProjects = false;
        if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_NON_BRANDED_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB1_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB2_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB3_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB4_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB5_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB6_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB7_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_UPT_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_BPT_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_CAP1_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_CAP2_PROJECTS.getId()) ||
                reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_CAP3_PROJECTS.getId())) {
            spendByProjects = true;
        }
        return spendByProjects;
    }


    private HSSFCellStyle getCellStyle(final HSSFWorkbook workbook, final short borderTop,
                                       final short borderRight, final short borderBottom, final short borderLeft) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(borderTop);
        cellStyle.setBorderRight(borderRight);
        cellStyle.setBorderBottom(borderBottom);
        cellStyle.setBorderLeft(borderLeft);
        return cellStyle;
    }

    public Integer getDefaultCurrency() {
        return defaultCurrency;
    }

    public Integer findDefaultCurrency(final User user) {
        Integer defaultCountry = SynchroUtils.getCountryByUser(user);
        Map<String, String> countryCurrencyMap = SynchroGlobal.getCountryCurrencyMap();
        Integer currencyId = -1;
        if(defaultCountry != null && defaultCountry > 0 && countryCurrencyMap.containsKey(defaultCountry.toString())) {
            try{
                currencyId = Integer.parseInt(countryCurrencyMap.get(defaultCountry.toString()));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return currencyId;
    }


    private CurrencyExchangeRate getUserCurrencyExchangeRate(final User user) {
        CurrencyExchangeRate currencyExchangeRate = new CurrencyExchangeRate();
        Integer currency = (currencyId != null && currencyId > 0)?currencyId:defaultCurrency;
        if(currency != null) {
            String currencyCode = SynchroGlobal.getCurrencies().get(currency);
            String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencyCode);
            String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
            if(currencyCode != null) {
                currencyExchangeRate.setCurrencyCode(currencyCode);
            } else {
                currencyExchangeRate.setCurrencyCode("");
            }
            currencyExchangeRate.setCurrencyId(new Long(currency));
            currencyExchangeRate.setExchangeRate(new BigDecimal(currencyValue));
        } else {
            currencyExchangeRate.setExchangeRate(new BigDecimal(1.0));
            currencyExchangeRate.setCurrencyCode("GBP");
        }
        return currencyExchangeRate;
    }


    private String getDecodedString(String str) {
        return str.replaceAll("%20A", " ");
    }





    private String toSheetName(final String input) throws UnsupportedEncodingException {
//        Pattern p = Pattern.compile("Spend by", Pattern.CASE_INSENSITIVE);
//        Matcher m = p.matcher(input);
//        if(m != null && m.matches()) {
        String result = input.replaceAll("(?i)Spend by","");
        return result;
//        } else {
//            return input;
//        }
    }

    //This method will fetch the ProjectId, Project Name, Meth, MethGroup etc for Cross Tab
    public static String getFieldName(String key, String projectId, String projectName, String bugetLocation, String region, String area, String t20_t40,  String methodology, String methGroup, String brand, String brandType, String categoryType, String agency, String agencyType, boolean isSpendByProject, boolean isSpendByBudgetLocation, boolean isSpendByBrand, 
    		boolean isSpendByMethodology, boolean isSpendByAgency, boolean isSpendByKantarNonKantar, boolean isSpendByCategoryType)
    {
    	String fieldValue="";
    	
    		
    	if(projectId!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				fieldValue = key.split("~")[0];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		if(isSpendByBudgetLocation && !isSpendByProject)
    		{
    			
    		}
    		if(isSpendByMethodology && !isSpendByBudgetLocation && !isSpendByProject)
    		{
    			
    		}
    		if(isSpendByBrand && !isSpendByMethodology && !isSpendByBudgetLocation && !isSpendByProject)
    		{
    			
    		}
    		if(isSpendByBrand && !isSpendByMethodology && !isSpendByBudgetLocation && !isSpendByProject)
    		{
    			
    		}
    		if(isSpendByCategoryType && !isSpendByBrand && !isSpendByMethodology && !isSpendByBudgetLocation && !isSpendByProject)
    		{
    			
    		}
    	}
    	
    	if(projectName!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				fieldValue = key.split("~")[1];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	if(bugetLocation!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				fieldValue = key.split("~")[2];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				fieldValue = key.split("~")[0];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	if(region!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				fieldValue = key.split("~")[3];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				fieldValue = key.split("~")[1];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	if(area!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				fieldValue = key.split("~")[4];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				fieldValue = key.split("~")[2];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	if(t20_t40!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				fieldValue = key.split("~")[5];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				fieldValue = key.split("~")[3];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	
    	if(methodology!=null)
    	{
    		if(isSpendByProject)
    		{
    			if (isSpendByBudgetLocation)
				{
					fieldValue = key.split("~")[6];
				}
    			else
    			{
    				//fieldValue = key.split("~")[3];
    				fieldValue = key.split("~")[2];
    			}
    			
    		}
    		
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				fieldValue = key.split("~")[4];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByMethodology)
    		{
    			try
    			{
    				fieldValue = key.split("~")[0];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	
    	if(methGroup!=null)
    	{
    		if(isSpendByProject)
    		{
    			if (isSpendByBudgetLocation)
				{
					fieldValue = key.split("~")[7];
				}
    			else
    			{
    				//fieldValue = key.split("~")[4];
    				fieldValue = key.split("~")[3];
    			}
    			
    		}
    		
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				fieldValue = key.split("~")[5];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByMethodology)
    		{
    			try
    			{
    				fieldValue = key.split("~")[1];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	
    	if(brand!=null)
    	{
    		if(isSpendByProject)
    		{
    			if(isSpendByBudgetLocation && isSpendByMethodology)
				{
					fieldValue = key.split("~")[8];
				}
				else if (isSpendByMethodology)
				{
					//fieldValue = key.split("~")[5];
					fieldValue = key.split("~")[4];
				}
				else if (isSpendByBudgetLocation)
				{
					fieldValue = key.split("~")[6];
				}
				else 
				{
					//fieldValue = key.split("~")[3];
					fieldValue = key.split("~")[2];
				}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				if (isSpendByMethodology)
    				{
    					fieldValue = key.split("~")[6];
    				}
    				else
    				{
    					fieldValue = key.split("~")[4];
    				}
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByMethodology)
    		{
    			try
    			{
    				fieldValue = key.split("~")[2];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBrand)
    		{
    			try
    			{
    				fieldValue = key.split("~")[0];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	
    	if(brandType!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				
    				if(isSpendByBudgetLocation && isSpendByMethodology)
    				{
    					fieldValue = key.split("~")[9];
    				}
    				else if (isSpendByMethodology)
    				{
    					//fieldValue = key.split("~")[6];
    					fieldValue = key.split("~")[5];
    				}
    				else if (isSpendByBudgetLocation)
    				{
    					fieldValue = key.split("~")[7];
    				}
    				else 
    				{
    					//fieldValue = key.split("~")[4];
    					fieldValue = key.split("~")[3];
    				}
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				//fieldValue = key.split("~")[7];
    				if (isSpendByMethodology)
    				{
    					fieldValue = key.split("~")[7];
    				}
    				else
    				{
    					fieldValue = key.split("~")[5];
    				}
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByMethodology)
    		{
    			try
    			{
    				fieldValue = key.split("~")[3];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBrand)
    		{
    			try
    			{
    				fieldValue = key.split("~")[1];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	
    	if(categoryType!=null)
    	{
    		if(isSpendByProject)
    		{
    			try
    			{
    				if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[10];
    				}
    				else if (isSpendByBudgetLocation && isSpendByMethodology)
    				{
    					fieldValue = key.split("~")[8];
    				}
    				else if (isSpendByMethodology && isSpendByBrand)
    				{
    					//fieldValue = key.split("~")[7];
    					fieldValue = key.split("~")[6];
    				}
    				else if (isSpendByBudgetLocation && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[8];
    				}
    				else if (isSpendByMethodology && isSpendByBrand)
    				{
    					//fieldValue = key.split("~")[7];
    					fieldValue = key.split("~")[6];
    				}
    				else if(isSpendByMethodology)
    				{
    					//fieldValue = key.split("~")[5];
    					fieldValue = key.split("~")[4];
    				}
    				else if(isSpendByBudgetLocation)
    				{
    					fieldValue = key.split("~")[6];
    				}
    				else if(isSpendByBrand)
    				{
    					//fieldValue = key.split("~")[5];
    					fieldValue = key.split("~")[4];
    				}
    				else 
    				{
    					//fieldValue = key.split("~")[4];
    					fieldValue = key.split("~")[3];
    				}
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				//fieldValue = key.split("~")[8];
    				if(isSpendByMethodology && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[8];
    				}
    				else if(isSpendByMethodology)
    				{
    					fieldValue = key.split("~")[6];
    				}
    				else if(isSpendByBrand)
    				{
    					fieldValue = key.split("~")[6];
    				}
    				else
    				{
    					fieldValue = key.split("~")[4];
    				}
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByMethodology)
    		{
    			try
    			{
    				//fieldValue = key.split("~")[4];
    				if(isSpendByBrand)
    				{
    					fieldValue = key.split("~")[4];
    				}
    				else
    				{
    					fieldValue = key.split("~")[2];
    				}
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBrand)
    		{
    			try
    			{
    				fieldValue = key.split("~")[2];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByCategoryType)
    		{
    			try
    			{
    				fieldValue = key.split("~")[0];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		
    	}
    	
    	if(agency!=null)
    	{
    		
    		try
			{
				String[] splitFieldValue = key.split("~");
				
				for(int i=0;i<(splitFieldValue.length-1);i++)
				{
					fieldValue=splitFieldValue[i];
				}
				/*
				if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByBrand && isSpendByCategoryType)
				{
					fieldValue = key.split("~")[11];
				}
				else if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByBrand)
				{
					fieldValue = key.split("~")[10];
				}
				else if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByCategoryType)
				{
					fieldValue = key.split("~")[10];
				}
				else if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByBrand)
				{
					fieldValue = key.split("~")[10];
				}
				else if (isSpendByBudgetLocation && isSpendByMethodology)
				{
					fieldValue = key.split("~")[8];
				}
				else if (isSpendByMethodology && isSpendByBrand)
				{
					fieldValue = key.split("~")[7];
				}
				else if (isSpendByBudgetLocation && isSpendByBrand)
				{
					fieldValue = key.split("~")[8];
				}
				else if (isSpendByMethodology && isSpendByBrand)
				{
					fieldValue = key.split("~")[7];
				}
				else if(isSpendByMethodology)
				{
					fieldValue = key.split("~")[5];
				}
				else if(isSpendByBudgetLocation)
				{
					fieldValue = key.split("~")[6];
				}
				else if(isSpendByBrand)
				{
					fieldValue = key.split("~")[5];
				}*/
			}
			catch(Exception e)
			{
				
			}
    		
    		/*if(isSpendByProject)
    		{
    			try
    			{
    				String[] splitFieldValue = key.split("~");
    				
    				for(int i=0;i<splitFieldValue.length;i++)
    				{
    					fieldValue=splitFieldValue[i];
    				}
    				
    				if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByBrand && isSpendByCategoryType)
    				{
    					fieldValue = key.split("~")[11];
    				}
    				else if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[10];
    				}
    				else if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByCategoryType)
    				{
    					fieldValue = key.split("~")[10];
    				}
    				else if(isSpendByBudgetLocation && isSpendByMethodology && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[10];
    				}
    				else if (isSpendByBudgetLocation && isSpendByMethodology)
    				{
    					fieldValue = key.split("~")[8];
    				}
    				else if (isSpendByMethodology && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[7];
    				}
    				else if (isSpendByBudgetLocation && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[8];
    				}
    				else if (isSpendByMethodology && isSpendByBrand)
    				{
    					fieldValue = key.split("~")[7];
    				}
    				else if(isSpendByMethodology)
    				{
    					fieldValue = key.split("~")[5];
    				}
    				else if(isSpendByBudgetLocation)
    				{
    					fieldValue = key.split("~")[6];
    				}
    				else if(isSpendByBrand)
    				{
    					fieldValue = key.split("~")[5];
    				}
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBudgetLocation)
    		{
    			try
    			{
    				fieldValue = key.split("~")[8];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByMethodology)
    		{
    			try
    			{
    				fieldValue = key.split("~")[4];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByBrand)
    		{
    			try
    			{
    				fieldValue = key.split("~")[2];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}
    		else if(isSpendByCategoryType)
    		{
    			try
    			{
    				fieldValue = key.split("~")[0];
    			}
    			catch(Exception e)
    			{
    				
    			}
    		}*/
    		
    	}
    	
    	if(agencyType!=null)
    	{
    		
    		try
			{
				String[] splitFieldValue = key.split("~");
				
				for(int i=0;i<(splitFieldValue.length);i++)
				{
					fieldValue=splitFieldValue[i];
				}
				
			}
			catch(Exception e)
			{
				
			}
    	}
    		
    	return fieldValue;
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

    public String getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(String reportTypes) {
        this.reportTypes = reportTypes;
    }

    public String getYears() {
        return years;
    }

    public void setYears(String years) {
        this.years = years;
    }

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public String getCountries() {
        return countries;
    }

    public void setCountries(String countries) {
        this.countries = countries;
    }

    public String getMarketTypes() {
        return marketTypes;
    }

    public void setMarketTypes(String marketTypes) {
        this.marketTypes = marketTypes;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public SynchroReportManager getSynchroReportManager() {
        return synchroReportManager;
    }

    public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
        this.synchroReportManager = synchroReportManager;
    }

	public String getSpendForSnapshot() {
		return spendForSnapshot;
	}

	public void setSpendForSnapshot(String spendForSnapshot) {
		this.spendForSnapshot = spendForSnapshot;
	}

	public QPRSnapshotManager getQprSnapshotManager() {
		return qprSnapshotManager;
	}

	public void setQprSnapshotManager(QPRSnapshotManager qprSnapshotManager) {
		this.qprSnapshotManager = qprSnapshotManager;
	}

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public void setSynchroProjectManagerNew(
			ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}

	public String getBudgetLocationsFilter() {
		return budgetLocationsFilter;
	}

	public void setBudgetLocationsFilter(String budgetLocationsFilter) {
		this.budgetLocationsFilter = budgetLocationsFilter;
	}

	public String getMethDetailsFilter() {
		return methDetailsFilter;
	}

	public void setMethDetailsFilter(String methDetailsFilter) {
		this.methDetailsFilter = methDetailsFilter;
	}

	public String getBrandsFilter() {
		return brandsFilter;
	}

	public void setBrandsFilter(String brandsFilter) {
		this.brandsFilter = brandsFilter;
	}

	public String getGenerateCrossTab() {
		return generateCrossTab;
	}

	public void setGenerateCrossTab(String generateCrossTab) {
		this.generateCrossTab = generateCrossTab;
	}
}
