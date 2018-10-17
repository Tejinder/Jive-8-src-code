package com.grail.synchro.action.reports;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.CurrencyExchangeRate;
import com.grail.synchro.beans.SpendReportExtractBean;
import com.grail.synchro.beans.SpendReportExtractFilter;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.QuarterRangeUtil;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.format.number.CurrencyFormatter;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/28/14
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpendReportsAction extends JiveActionSupport implements Preparable {

    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename = "Spend Reports.xls";

    private String downloadStreamType = "application/vnd.ms-excel";

    private final String REPORT_TYPE_SELECTION_ERROR = "reportTypeSelectionError";

    private SynchroReportManager synchroReportManager;

    private String reportTypes;
    private String years;
    private String regions;
    private String countries;
    private String marketTypes;
    private Integer currencyId;

    private Integer defaultCurrency;


    @Override
    public void prepare() throws Exception {
        if(getUser() != null) {
            defaultCurrency = findDefaultCurrency(getUser());
        }
    }

    @Override
    public String execute() {
        if(getUser() != null) {

//            if(!SynchroPermHelper.hasGenerateReportAccess(getUser())) {
//                return UNAUTHORIZED;
//            }
            if(SynchroPermHelper.isExternalAgencyUser(getUser()) || SynchroPermHelper.isCommunicationAgencyUser(getUser())) {
                return UNAUTHORIZED;
            }

            if(!(SynchroPermHelper.isSynchroMiniAdmin(getUser())
                    || SynchroPermHelper.isSynchroAdmin(getUser())
                    || SynchroPermHelper.isSynchroGlobalSuperUser(getUser())
                    || SynchroPermHelper.isSynchroRegionalSuperUser(getUser())
                    || SynchroPermHelper.isSynchroEndmarketSuperUser(getUser())
            )) {
                return UNAUTHORIZED;
            }
        } else {
            return UNAUTHENTICATED;
        }
        return SUCCESS;
    }



    public String downloadReport() throws UnsupportedEncodingException, IOException {
        if(getUser() != null) {
//            if(!SynchroPermHelper.hasGenerateReportAccess(getUser())) {
//                return UNAUTHORIZED;
//            }
            if(SynchroPermHelper.isExternalAgencyUser(getUser()) || SynchroPermHelper.isCommunicationAgencyUser(getUser())) {
                return UNAUTHORIZED;
            }

            if(!(SynchroPermHelper.isSynchroMiniAdmin(getUser())
                    || SynchroPermHelper.isSynchroAdmin(getUser())
                    || SynchroPermHelper.isSynchroGlobalSuperUser(getUser())
                    || SynchroPermHelper.isSynchroRegionalSuperUser(getUser())
                    || SynchroPermHelper.isSynchroEndmarketSuperUser(getUser())
            )) {
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
            processReport();
            return DOWNLOAD_REPORT;
        }
    }

    private void processReport() throws IOException {
        HSSFWorkbook workbook = null;
        SpendReportExtractFilter filter = getSpendReportFilter();
        if(years != null && !years.equals("")) {
            String [] yearsArr = years.split(",");
            List<Integer> yearsFilter = null;
            if(yearsArr.length == 1) {
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
            }
        }  else {
            generateDownloadFileName(null, false);
            workbook = new HSSFWorkbook();
            workbook = generateReport(workbook, filter);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            downloadStream = new ByteArrayInputStream(baos.toByteArray());

        }
    }

    private void generateDownloadFileName(final String year, final boolean multipleFiles) {
        String fileName = "Spend Reports.xls";
        if(reportTypes != null && !reportTypes.equals("")) {
            String [] reportTypesStrArr = reportTypes.split(",");
            if(reportTypesStrArr.length == 1) {
                fileName = SynchroGlobal.SpendReportType.getById(Integer.parseInt(reportTypesStrArr[0])).getName();
            } else {

                fileName = "Spend Reports";
            }

            Calendar calendar = Calendar.getInstance();
            fileName = fileName + "_" + (year  != null?year+"_":"") +
                    calendar.get(Calendar.YEAR) +
                    "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                    "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                    "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                    "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                    "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) +
                    (multipleFiles?".zip":".xls");
        } else {

        }

        if(multipleFiles) {
            downloadStreamType = "application/vnd.ms-excel";
        } else {
            downloadStreamType = "application/vnd.ms-excel";
        }

        downloadFilename = fileName;
    }

    private HSSFWorkbook generateReport(final HSSFWorkbook workbook, final SpendReportExtractFilter filter) throws UnsupportedEncodingException, IOException{
        if(reportTypes != null && !reportTypes.equals("")) {


            String [] reportTypesStrArr = reportTypes.split(",");
            CurrencyExchangeRate currencyExchangeRate = getUserCurrencyExchangeRate(getUser());
            Integer startColumn = 0;
            Integer startRow = 0;
            boolean showProjectCodeColumn = false;


            if(reportTypesStrArr != null && reportTypesStrArr.length > 0) {
//                String fileName = null;
//                if(reportTypesStrArr.length == 1) {
//                    fileName = SynchroGlobal.SpendReportType.getById(Integer.parseInt(reportTypesStrArr[0])).getName();
//                } else {
//
//                    fileName = "Spend Reports";
//                }
//
//                Calendar calendar = Calendar.getInstance();
//                fileName = fileName + "_" +
//                        calendar.get(Calendar.YEAR) +
//                        "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
//                        "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
//                        "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
//                        "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
//                        "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) +
//                        ".xls";
//
//                downloadFilename = fileName;

                // Currency format
//                NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
//                numberFormat.setRoundingMode(RoundingMode.HALF_EVEN);

                HSSFDataFormat df = workbook.createDataFormat();
                short currencyDataFormatIndex = df.getFormat("#,###0.0000");

                //Format decimalFormat = new DecimalFormat("#,##0.#");
//                HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
//                dataFormatter.addFormat(decimalFormat);



                // Header Styles start
                HSSFFont sheetHeaderFont = workbook.createFont();
                sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

                HSSFFont notesFont = workbook.createFont();
                notesFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
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

                HSSFCellStyle tableHeaderProjectNameColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                tableHeaderProjectNameColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column1 style
                HSSFCellStyle tableHeaderColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                tableHeaderColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column2 style
                HSSFCellStyle tableHeaderColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn2Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column3 style
                HSSFCellStyle tableHeaderColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn3Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

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

                HSSFCellStyle dataRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THICK);
                dataRowColumn1Style.setWrapText(true);

                HSSFCellStyle dataRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);


                HSSFCellStyle dataRowColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle dataRowColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle dataRowColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);
                // Data Row cell styles end

                // Total cost row styles start
                HSSFCellStyle totalCostRowProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                totalCostRowProjectCodeColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle totalCostRowTotalColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                totalCostRowTotalColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle totalCostRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                totalCostRowColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle totalCostRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle notesStyle = workbook.createCellStyle();
                notesStyle.setWrapText(true);
                notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);

                // Total cost row styles end

                for(String reportTypeStr : reportTypesStrArr) {
                    Integer reportType = Integer.parseInt(reportTypeStr);


                    filter.setReportType(reportType);

                    showProjectCodeColumn = isSpendByProjectsReport(reportType);

                    // Create sheet for each report type
                    HSSFSheet sheet = workbook.createSheet(toSheetName(SynchroGlobal.SpendReportType.getById(reportType).getName()).replaceAll("/","or"));

                    // Create sheet row1(Report Type Header)
                    HSSFRow reportTypeHeader = sheet.createRow(startRow);
                    HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(startColumn);
                    reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
                    reportTypeHeaderColumn.setCellValue(SynchroGlobal.SpendReportType.getById(reportType).getDescription() + " (" + currencyExchangeRate.getCurrencyCode() + ")");

                    // Create sheet row2
                    HSSFRow userNameHeader = sheet.createRow(startRow + 1);
                    HSSFCell userNameHeaderColumn = userNameHeader.createCell(startColumn);
                    userNameHeaderColumn.setCellStyle(sheetHeaderCellStyle);
                    userNameHeaderColumn.setCellValue("User: " + getUser().getFirstName() + " " + getUser().getLastName());

                    Integer filtersRowCount = 0;

                    if(filter != null && filter.getYears() != null && filter.getYears().size() > 0) {
                        HSSFRow yearsFilterRow = sheet.createRow(startRow + 2);
                        HSSFCell yearsFilterRowCell = yearsFilterRow.createCell(startColumn);
                        yearsFilterRowCell.setCellStyle(sheetHeaderCellStyle);
                        yearsFilterRowCell.setCellValue("Year(s): "+StringUtils.join(filter.getYears(),", "));
                        filtersRowCount++;
                    }

                    if(regions != null && !regions.equals("")) {
                        List<String> regionsList = new LinkedList<String>();
                        String [] regionsStrIds = regions.split(",");
                        for(String ridStr : regionsStrIds) {
                            if(ridStr != null && !ridStr.equals("")) {
                                regionsList.add(SynchroUtils.getRegionFields().get(Integer.parseInt(ridStr)));
                            }
                        }
                        if(regionsList.size() > 0) {
                            HSSFRow regionsFilterRow = sheet.createRow(startRow + 2 + filtersRowCount);
                            HSSFCell regionsFilterRowCell = regionsFilterRow.createCell(startColumn);
                            regionsFilterRowCell.setCellStyle(sheetHeaderCellStyle);
                            regionsFilterRowCell.setCellValue("Region(s): " + StringUtils.join(regionsList,", "));
                            filtersRowCount++;
                        }
                    }

                    if(countries != null && !countries.equals("")) {
                        List<String> countriesList = new LinkedList<String>();
                        String [] countriesStrIds = countries.split(",");
                        for(String cidStr : countriesStrIds) {
                            if(cidStr != null && !cidStr.equals("")) {
                                countriesList.add(SynchroUtils.getEndMarketFields().get(Integer.parseInt(cidStr)));
                            }
                        }
                        if(countriesList.size() > 0) {
                            HSSFRow countriesFilterRow = sheet.createRow(startRow + 2 + filtersRowCount);
                            HSSFCell countriesFilterRowCell = countriesFilterRow.createCell(startColumn);

                            countriesFilterRowCell.setCellValue("Countries: " + StringUtils.join(countriesList, ", "));
                            countriesFilterRowCell.setCellStyle(sheetHeaderCellStyle);
                            filtersRowCount++;
                        }

                    }

                    // Table Header row
                    HSSFRow tableHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);

                    if(showProjectCodeColumn) {
                        // Table Header column0(Project Code)
                        HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
                        tableHeaderColumn0.setCellValue("Project Code");
                        tableHeaderColumn0.setCellStyle(tableHeaderProjectCodeColumnStyle);
                    }

                    // Table Header column1(Currency Code)
                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(showProjectCodeColumn?(startColumn + 1):startColumn);
//                    if(showProjectCodeColumn) {
//                        tableHeaderColumn1Style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//                    } else {
//                        if(tableHeaderColumn1Style.getBorderLeft() != HSSFCellStyle.BORDER_THICK) {
//                            tableHeaderColumn1Style.setBorderLeft(HSSFCellStyle.BORDER_THICK);
//                        }
//                    }
                    tableHeaderColumn1.setCellValue(currencyExchangeRate.getCurrencyCode());
                    tableHeaderColumn1.setCellStyle(showProjectCodeColumn?tableHeaderProjectNameColumnStyle:tableHeaderColumn1Style);

                    // Table Header column2(QPR1)
                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(showProjectCodeColumn?(startColumn + 2):(startColumn + 1));
                    tableHeaderColumn2.setCellValue("QPR1");
                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);

                    // Table Header column3(QPR2)
                    HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(showProjectCodeColumn?(startColumn + 3):(startColumn + 2));
                    tableHeaderColumn3.setCellValue("QPR2");
                    tableHeaderColumn3.setCellStyle(tableHeaderColumn3Style);

                    // Table Header column4(QPR3)
                    HSSFCell tableHeaderColumn4 = tableHeaderRow.createCell(showProjectCodeColumn?(startColumn + 4):(startColumn + 3));
                    tableHeaderColumn4.setCellValue("QPR3");
                    tableHeaderColumn4.setCellStyle(tableHeaderColumn4Style);

                    // Table Header column5(QPR4)
                    HSSFCell tableHeaderColumn5 = tableHeaderRow.createCell(showProjectCodeColumn?(startColumn + 5):(startColumn + 4));
                    tableHeaderColumn5.setCellValue("QPR4");
                    tableHeaderColumn5.setCellStyle(tableHeaderColumn5Style);



                    List<SpendReportExtractBean> spendReportExtractBeans = synchroReportManager.getSpendReport(filter, getUser());

                    Integer rowCount = tableHeaderRow.getRowNum() + 1;


                    if(spendReportExtractBeans != null && spendReportExtractBeans.size() > 0) {
                        BigDecimal quarter1TotalSpend = new BigDecimal(0);
                        BigDecimal quarter2TotalSpend = new BigDecimal(0);
                        BigDecimal quarter3TotalSpend = new BigDecimal(0);
                        BigDecimal quarter4TotalSpend = new BigDecimal(0);

                        BigDecimal quarter1TotalMethodologySpend = new BigDecimal(0);
                        BigDecimal quarter2TotalMethodologySpend = new BigDecimal(0);
                        BigDecimal quarter3TotalMethodologySpend = new BigDecimal(0);
                        BigDecimal quarter4TotalMethodologySpend = new BigDecimal(0);

                        Long projectId = -1L;
                        Long endmarketId = -1L;
                        Integer investmentType = -1;

                        boolean addedRegionsHeader = false;
                        boolean addedEndmarketsHeader = false;
                        boolean addedBrandsHeader = false;


                        for(SpendReportExtractBean spendReportExtractBean : spendReportExtractBeans) {
                            boolean sumTotal = true;

                            if(reportType.equals(SynchroGlobal.SpendReportType.TOTAL_SPEND.getId())) {
                                if(spendReportExtractBean.getOrder().intValue() != 3) {
                                    sumTotal = false;
                                }
                                if(spendReportExtractBean.getOrder().intValue() == 2 && !addedRegionsHeader) {
                                    // Empty row
                                    createEmptyDataRow(sheet, rowCount, showProjectCodeColumn?(startColumn + 1):startColumn,
                                            showProjectCodeColumn?projectNameDataRowColumnStyle:dataRowColumn1Style, dataRowColumn2Style, dataRowColumn3Style,
                                            dataRowColumn4Style, dataRowColumn5Style, null, null);
                                    rowCount++;

                                    // Regions label
                                    createEmptyDataRow(sheet, rowCount,showProjectCodeColumn?(startColumn + 1):startColumn,
                                            showProjectCodeColumn?projectNameDataRowColumnStyle:dataRowColumn1Style, dataRowColumn2Style, dataRowColumn3Style,
                                            dataRowColumn4Style, dataRowColumn5Style, "Regions", notesFont);
                                    rowCount++;
                                    addedRegionsHeader = true;
                                }
                                if(spendReportExtractBean.getOrder().intValue() == 3 && !addedEndmarketsHeader) {
                                    // Empty row
                                    createEmptyDataRow(sheet, rowCount,showProjectCodeColumn?(startColumn + 1):startColumn,
                                            showProjectCodeColumn?projectNameDataRowColumnStyle:dataRowColumn1Style, dataRowColumn2Style, dataRowColumn3Style,
                                            dataRowColumn4Style, dataRowColumn5Style, null, null);
                                    rowCount++;

                                    // Endmarkets label
                                    createEmptyDataRow(sheet, rowCount,showProjectCodeColumn?(startColumn + 1):startColumn,
                                            showProjectCodeColumn?projectNameDataRowColumnStyle:dataRowColumn1Style, dataRowColumn2Style, dataRowColumn3Style,
                                            dataRowColumn4Style, dataRowColumn5Style, "End-Markets", notesFont);
                                    rowCount++;
                                    addedEndmarketsHeader = true;
                                }
                            } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_BRANDED_NON_BRANDED.getId())) {
                                if(spendReportExtractBean.getOrder().intValue() == 3 && !addedBrandsHeader) {
                                    createEmptyDataRow(sheet, rowCount,showProjectCodeColumn?(startColumn + 1):startColumn,
                                            showProjectCodeColumn?projectNameDataRowColumnStyle:dataRowColumn1Style, dataRowColumn2Style, dataRowColumn3Style,
                                            dataRowColumn4Style, dataRowColumn5Style, null, null);
                                    rowCount++;
                                    addedBrandsHeader = true;
                                }
                                if(spendReportExtractBean.getOrder().intValue() == 2) {
                                    sumTotal = false;
                                }
                            } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_METHODOLOGY.getId())) {
                                sumTotal = false;
                            }

                            HSSFRow dataRow = sheet.createRow(rowCount);

                            if(showProjectCodeColumn) {
                                // Column1
                                HSSFCell projectCodeDataRowColumn = dataRow.createCell(startColumn);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellValue(spendReportExtractBean.getProjectId());
                                projectCodeDataRowColumn.setCellStyle(projectCodeDataRowColumnStyle);
                            }

                            // Column1
                            HSSFCell dataRowColumn1 = dataRow.createCell(showProjectCodeColumn?(startColumn + 1):startColumn);
                            dataRowColumn1.setCellValue(spendReportExtractBean.getReportLabel());
                            dataRowColumn1.setCellStyle(showProjectCodeColumn?projectNameDataRowColumnStyle:dataRowColumn1Style);

                            // Column2
                            HSSFCell dataRowColumn2 = dataRow.createCell(showProjectCodeColumn?(startColumn + 2):(startColumn + 1));
                            dataRowColumn2.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                            if(spendReportExtractBean.getQuarter1() != null
                                    && spendReportExtractBean.getQuarter1().doubleValue() > 0) {
                                BigDecimal quarter1Val = spendReportExtractBean.getQuarter1();
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter1Val = quarter1Val.divide(currencyExchangeRate.getExchangeRate(),MathContext.DECIMAL128);
                                }
                                dataRowColumn2Style.setDataFormat(currencyDataFormatIndex);
                                dataRowColumn2.setCellValue(quarter1Val.doubleValue());
                                if(sumTotal) {
                                    quarter1TotalSpend = quarter1TotalSpend.add(quarter1Val);
                                }
                            } else {
                                dataRowColumn2.setCellValue(0);
                            }
                            dataRowColumn2.setCellStyle(dataRowColumn2Style);
                            //dataFormatter.formatCellValue(dataRowColumn2);

                            // Column3
                            HSSFCell dataRowColumn3 = dataRow.createCell(showProjectCodeColumn?(startColumn + 3):(startColumn + 2));
                            dataRowColumn3.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                            if(spendReportExtractBean.getQuarter2() != null
                                    && spendReportExtractBean.getQuarter2().doubleValue() > 0) {
                                BigDecimal quarter2Val = spendReportExtractBean.getQuarter2();
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter2Val = quarter2Val.divide(currencyExchangeRate.getExchangeRate(),MathContext.DECIMAL128);
                                }
                                dataRowColumn3Style.setDataFormat(currencyDataFormatIndex);
                                dataRowColumn3.setCellValue(quarter2Val.doubleValue());
                                if(sumTotal) {
                                    quarter2TotalSpend = quarter2TotalSpend.add(quarter2Val);
                                }
                            } else {
                                dataRowColumn3.setCellValue(0);
                            }
                            dataRowColumn3.setCellStyle(dataRowColumn3Style);
                            // dataFormatter.formatCellValue(dataRowColumn3);


                            // Column4
                            HSSFCell dataRowColumn4 = dataRow.createCell(showProjectCodeColumn?(startColumn + 4):(startColumn + 3));
                            dataRowColumn4.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                            if(spendReportExtractBean.getQuarter3() != null
                                    && spendReportExtractBean.getQuarter3().doubleValue() > 0) {
                                BigDecimal quarter3Val = spendReportExtractBean.getQuarter3();
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter3Val = quarter3Val.divide(currencyExchangeRate.getExchangeRate(), MathContext.DECIMAL128);
                                }
                                dataRowColumn4Style.setDataFormat(currencyDataFormatIndex);
                                dataRowColumn4.setCellValue(quarter3Val.doubleValue());
                                if(sumTotal) {
                                    quarter3TotalSpend = quarter3TotalSpend.add(quarter3Val);
                                }
                            } else {
                                dataRowColumn4.setCellValue(0);
                            }
                            dataRowColumn4.setCellStyle(dataRowColumn4Style);
                            //dataFormatter.formatCellValue(dataRowColumn4);

                            // Column5
                            HSSFCell dataRowColumn5 = dataRow.createCell(showProjectCodeColumn?(startColumn + 5):(startColumn + 4));
                            dataRowColumn5.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                            if(spendReportExtractBean.getQuarter4() != null
                                    && spendReportExtractBean.getQuarter4().doubleValue() > 0) {
                                BigDecimal quarter4Val = spendReportExtractBean.getQuarter4();
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter4Val = quarter4Val.divide(currencyExchangeRate.getExchangeRate(), MathContext.DECIMAL128);
                                }
                                dataRowColumn5Style.setDataFormat(currencyDataFormatIndex);
                                dataRowColumn5.setCellValue(quarter4Val.doubleValue());
                                if(sumTotal) {
                                    quarter4TotalSpend = quarter4TotalSpend.add(quarter4Val);
                                }
                            } else {
                                dataRowColumn5.setCellValue(0);
                            }
                            dataRowColumn5.setCellStyle(dataRowColumn5Style);
                            //dataFormatter.formatCellValue(dataRowColumn5);



                            rowCount++;
                        }

                        if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_METHODOLOGY.getId())) {

                            quarter1TotalMethodologySpend = synchroReportManager.getMethodologyTotalSpend(filter,getUser(),QuarterRangeUtil.getQuarter(1));
                            quarter2TotalMethodologySpend = synchroReportManager.getMethodologyTotalSpend(filter,getUser(),QuarterRangeUtil.getQuarter(2));
                            quarter3TotalMethodologySpend = synchroReportManager.getMethodologyTotalSpend(filter,getUser(),QuarterRangeUtil.getQuarter(3));
                            quarter4TotalMethodologySpend = synchroReportManager.getMethodologyTotalSpend(filter,getUser(),QuarterRangeUtil.getQuarter(4));

                            if(quarter1TotalMethodologySpend != null && quarter1TotalMethodologySpend.doubleValue() > 0) {
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter1TotalSpend = quarter1TotalMethodologySpend.divide(currencyExchangeRate.getExchangeRate(), MathContext.DECIMAL128);;
                                } else {
                                    quarter1TotalSpend = quarter1TotalMethodologySpend;
                                }
                            } else {
                                quarter1TotalSpend = new BigDecimal(0);
                            }

                            if(quarter2TotalMethodologySpend != null && quarter2TotalMethodologySpend.doubleValue() > 0) {
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter2TotalSpend = quarter2TotalMethodologySpend.divide(currencyExchangeRate.getExchangeRate(), MathContext.DECIMAL128);;
                                } else {
                                    quarter2TotalSpend = quarter2TotalMethodologySpend;
                                }
                            } else {
                                quarter2TotalSpend = new BigDecimal(0);
                            }

                            if(quarter3TotalMethodologySpend != null && quarter3TotalMethodologySpend.doubleValue() > 0) {
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter3TotalSpend = quarter3TotalMethodologySpend.divide(currencyExchangeRate.getExchangeRate(), MathContext.DECIMAL128);;
                                } else {
                                    quarter3TotalSpend = quarter3TotalMethodologySpend;
                                }
                            } else {
                                quarter3TotalSpend = new BigDecimal(0);
                            }

                            if(quarter4TotalMethodologySpend != null && quarter4TotalMethodologySpend.doubleValue() > 0) {
                                if(currencyExchangeRate.getExchangeRate().doubleValue() > 0) {
                                    quarter4TotalSpend = quarter4TotalMethodologySpend.divide(currencyExchangeRate.getExchangeRate(), MathContext.DECIMAL128);;
                                } else {
                                    quarter4TotalSpend = quarter4TotalMethodologySpend;
                                }
                            } else {
                                quarter4TotalSpend = new BigDecimal(0);
                            }
                        }


                        // Total cost row column1
                        HSSFRow lastRow = sheet.createRow(rowCount);
                        // Total cost  row column0
                        if(showProjectCodeColumn) {
                            HSSFCell lastRowColumn0 = lastRow.createCell(startColumn);
                            lastRowColumn0.setCellValue("Total");
                            lastRowColumn0.setCellStyle(totalCostRowProjectCodeColumnStyle);
                        }
                        // Total cost  row column1
                        HSSFCell lastRowColumn1 = lastRow.createCell(showProjectCodeColumn?(startColumn + 1):startColumn);
                        lastRowColumn1.setCellValue(showProjectCodeColumn?"":"Total");
                        lastRowColumn1.setCellStyle(showProjectCodeColumn?totalCostRowTotalColumnStyle:totalCostRowColumn1Style);

                        // Total cost  row column2
                        HSSFCell lastRowColumn2 = lastRow.createCell(showProjectCodeColumn?(startColumn + 2):(startColumn + 1));
                        lastRowColumn2.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        if(quarter1TotalSpend.doubleValue() > 0) {
                            totalCostRowColumn2Style.setDataFormat(currencyDataFormatIndex);
                            lastRowColumn2.setCellValue(quarter1TotalSpend.doubleValue());
                        } else {
                            lastRowColumn2.setCellValue(0);
                        }
                        lastRowColumn2.setCellStyle(totalCostRowColumn2Style);
                        // dataFormatter.formatCellValue(lastRowColumn2);

                        // Total cost  row column3
                        HSSFCell lastRowColumn3 = lastRow.createCell(showProjectCodeColumn?(startColumn + 3):(startColumn + 2));
                        lastRowColumn3.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        if(quarter2TotalSpend.doubleValue() > 0) {
                            totalCostColumn3Style.setDataFormat(currencyDataFormatIndex);
                            lastRowColumn3.setCellValue(quarter2TotalSpend.doubleValue());
                        } else {
                            lastRowColumn3.setCellValue(0);
                        }
                        lastRowColumn3.setCellStyle(totalCostColumn3Style);
                        //dataFormatter.formatCellValue(lastRowColumn3);

                        // Total cost  row column4
                        HSSFCell lastRowColumn4 = lastRow.createCell(showProjectCodeColumn?(startColumn + 4):(startColumn + 3));
                        lastRowColumn4.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        if(quarter3TotalSpend.doubleValue() > 0) {
                            totalCostColumn4Style.setDataFormat(currencyDataFormatIndex);
                            lastRowColumn4.setCellValue(quarter3TotalSpend.doubleValue());
                        } else {
                            lastRowColumn4.setCellValue(0);
                        }
                        lastRowColumn4.setCellStyle(totalCostColumn4Style);
                        //dataFormatter.formatCellValue(lastRowColumn4);

                        // Total cost row column5
                        HSSFCell lastRowColumn5 = lastRow.createCell(showProjectCodeColumn?(startColumn + 5):(startColumn + 4));
                        lastRowColumn5.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        if(quarter4TotalSpend.doubleValue() > 0) {
                            totalCostColumn5Style.setDataFormat(currencyDataFormatIndex);
                            lastRowColumn5.setCellValue(quarter4TotalSpend.doubleValue());
                        } else {
                            lastRowColumn5.setCellValue(0);
                        }
                        lastRowColumn5.setCellStyle(totalCostColumn5Style);
                        //dataFormatter.formatCellValue(lastRowColumn4);
                    }
                    if(showProjectCodeColumn) {
                        //sheet.autoSizeColumn(startColumn, false);
                        sheet.setColumnWidth(startColumn ,sheet.getColumnWidth(startColumn) + 1000);
                    }
                    sheet.autoSizeColumn(showProjectCodeColumn?(startColumn + 1):startColumn, false);
                    sheet.autoSizeColumn(showProjectCodeColumn?(startColumn + 2):(startColumn + 1), false);
                    sheet.autoSizeColumn(showProjectCodeColumn?(startColumn + 3):(startColumn + 2), false);
                    sheet.autoSizeColumn(showProjectCodeColumn?(startColumn + 4):(startColumn + 3), false);
                    sheet.autoSizeColumn(showProjectCodeColumn?(startColumn + 5):(startColumn + 4), false);

                    sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startColumn, showProjectCodeColumn?(startColumn + 5):(startColumn + 4)));
                    sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 1, startColumn, showProjectCodeColumn?(startColumn + 5):(startColumn + 4)));
                    if(filtersRowCount > 0) {
                        for(int i = 1 ;i <= filtersRowCount; i++) {
                            sheet.addMergedRegion(new CellRangeAddress(startRow + 1 + i, startRow + 1 + i, startColumn, showProjectCodeColumn?(startColumn + 5):(startColumn + 4)));
                        }
                    }

                    rowCount = rowCount + 2;

                    if(reportType.equals(SynchroGlobal.SpendReportType.TOTAL_SPEND.getId())) {
                        HSSFRow totalSpendReportNotesRow = sheet.createRow(rowCount);
                        totalSpendReportNotesRow.setHeightInPoints(120);
                        HSSFCell totalSpendReportNotesRowCell = totalSpendReportNotesRow.createCell(startColumn);
                        totalSpendReportNotesRowCell.setCellStyle(notesStyle);
                        HSSFRichTextString totalSpendRichTextNotes = new HSSFRichTextString(SynchroGlobal.SpendReportNotes.TOTAL_SPEND_NOTES.toString());
                        totalSpendRichTextNotes.applyFont(0,6, notesFont);
                        totalSpendRichTextNotes.applyFont(7, SynchroGlobal.SpendReportNotes.TOTAL_SPEND_NOTES.toString().length() - 1, italicFont);
                        totalSpendReportNotesRowCell.setCellValue(totalSpendRichTextNotes);
                        sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount, startColumn, showProjectCodeColumn?(startColumn + 5):(startColumn + 4)));
                    } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_METHODOLOGY.getId())) {
                        HSSFRow spendReportNotesRow = sheet.createRow(rowCount);
                        spendReportNotesRow.setHeightInPoints(100);
                        HSSFCell spendReportNotesRowCell = spendReportNotesRow.createCell(startColumn);
                        spendReportNotesRowCell.setCellStyle(notesStyle);
                        HSSFRichTextString spendReportRichTextNotes = new HSSFRichTextString(SynchroGlobal.SpendReportNotes.METHODOLOGY_NOTES.toString());
                        spendReportRichTextNotes.applyFont(0,6, notesFont);
                        spendReportRichTextNotes.applyFont(7, SynchroGlobal.SpendReportNotes.METHODOLOGY_NOTES.toString().length() - 1, italicFont);
                        spendReportNotesRowCell.setCellValue(spendReportRichTextNotes);
                        sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount, startColumn, showProjectCodeColumn?(startColumn + 5):(startColumn + 4)));
                    } else {
                        HSSFRow spendReportNotesRow = sheet.createRow(rowCount);
                        spendReportNotesRow.setHeightInPoints(90);
                        HSSFCell spendReportNotesRowCell = spendReportNotesRow.createCell(startColumn);
                        spendReportNotesRowCell.setCellStyle(notesStyle);
                        HSSFRichTextString spendReportRichTextNotes = new HSSFRichTextString(SynchroGlobal.SpendReportNotes.REPORT_NOTES.toString());
                        spendReportRichTextNotes.applyFont(0,6, notesFont);
                        spendReportRichTextNotes.applyFont(7, SynchroGlobal.SpendReportNotes.REPORT_NOTES.toString().length() - 1, italicFont);
                        spendReportNotesRowCell.setCellValue(spendReportRichTextNotes);
                        sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount, startColumn, showProjectCodeColumn?(startColumn + 5):(startColumn + 4)));
                    }

                }
            }
        }
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        workbook.write(baos);
//        downloadStream = new ByteArrayInputStream(baos.toByteArray());
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
}
