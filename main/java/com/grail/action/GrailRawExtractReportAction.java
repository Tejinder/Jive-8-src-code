package com.grail.action;

import com.grail.GrailGlobals;
import com.grail.beans.GrailBriefTemplate;
import com.grail.beans.GrailBriefTemplateFilter;
import com.grail.manager.GrailBriefTemplateManager;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.GrailUtils;
import com.jivesoftware.community.action.JiveActionSupport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/7/15
 * Time: 3:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailRawExtractReportAction extends JiveActionSupport {
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private GrailBriefTemplateManager grailBriefTemplateManager;


    @Override
    public String execute() {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }

        if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin(getUser())
                || SynchroPermHelper.isSynchroGlobalSuperUser(getUser()))) {
            return UNAUTHORIZED;
        }

        if(!(SynchroPermHelper.canAccessGrailPortal(getUser()) || SynchroPermHelper.isSynchroGlobalSuperUser(getUser()))) {
            return UNAUTHORIZED;
        }
        return SUCCESS;
    }

    public String downloadReport() throws IOException {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }

        if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin(getUser())
                || SynchroPermHelper.isSynchroGlobalSuperUser(getUser()))) {
            return UNAUTHORIZED;
        }

        if(!(SynchroPermHelper.canAccessGrailPortal(getUser()) || SynchroPermHelper.isSynchroGlobalSuperUser(getUser()))) {
            return UNAUTHORIZED;
        }

        LOG.info("Downloading Grail Raw extract by " +getUser().getFirstName() + " " + getUser().getLastName() + " on " + new Date());
        Calendar calendar = Calendar.getInstance();
        downloadFilename = "Grail_Raw_Extract_" +
                calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) +
                ".csv";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder report = new StringBuilder();

        StringBuilder header = new StringBuilder();

        header.append("Project Code").append(",");
        header.append("Owner").append(",");
        header.append("Request Raised").append(",");
        header.append("Status").append(",");
        header.append("Business Question").append(",");
        header.append("Hypotheses and Business Needs").append(",");
        header.append("Data Source").append(",");
        header.append("Markets").append(",");
        header.append("Products").append(",");
        header.append("Brands").append(",");
        header.append("Categories").append(",");
        header.append("Methodology Type").append(",");
        header.append("Delivery Date").append(",");
        header.append("Output Format").append(",");
        header.append("BAT Contact").append(",");
        header.append("Final Cost").append(",");
        header.append("Final Cost - Currency").append(",");
        header.append("Final Cost - Currency (GBP)");

        report.append(header.toString()).append("\n");


        DecimalFormat currencyFormatter = new DecimalFormat("#,###0.00000000000000000000000000000000000");

        GrailBriefTemplateFilter filter = new GrailBriefTemplateFilter();

        List<GrailBriefTemplate> grailBriefTemplates = grailBriefTemplateManager.getAll(filter);
        if(grailBriefTemplates != null && grailBriefTemplates.size() > 0) {
            for(GrailBriefTemplate grailBriefTemplate : grailBriefTemplates) {
                if(grailBriefTemplate != null) {
                    StringBuilder data = new StringBuilder();

                    if(grailBriefTemplate.getId() != null && grailBriefTemplate.getId() > 0) {
                        data.append("\"" + GrailGlobals.generateProjectCode(grailBriefTemplate.getId()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getCreatedBy() != null) {
                        data.append("\"" + SynchroUtils.getUserDisplayName(grailBriefTemplate.getCreatedBy()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getCreationDate() != null) {
                        data.append("\"" + dateFormat.format(grailBriefTemplate.getCreationDate()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getStatus() != null && grailBriefTemplate.getStatus() > 0) {
                        data.append("\"" + GrailGlobals.GrailBriefTemplateStatusType.getById(grailBriefTemplate.getStatus()).getName()+ "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getResearchNeedsPriorities() != null) {
                        data.append("\"" + grailBriefTemplate.getResearchNeedsPriorities() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getHypothesisBusinessNeed() != null) {
                        data.append("\"" + grailBriefTemplate.getHypothesisBusinessNeed() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getDataSource() != null) {
                        data.append("\"" + grailBriefTemplate.getDataSource() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getMarkets() != null && grailBriefTemplate.getMarkets() > 0) {
                        if(SynchroGlobal.getEndMarkets().containsKey(grailBriefTemplate.getMarkets().intValue())) {
                            data.append("\"" + SynchroGlobal.getEndMarkets().get(grailBriefTemplate.getMarkets().intValue()) + "\"").append(",");
                        } else {
                            data.append("").append(",");
                        }
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getProducts() != null) {
                        data.append("\"" + grailBriefTemplate.getProducts() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getBrands() != null) {
                        data.append("\"" + grailBriefTemplate.getBrands() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getCategories() != null) {
                        data.append("\"" + grailBriefTemplate.getCategories() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getMethodologyType() != null) {
                        Map<Long, String> methTypeMap = GrailUtils.getAllGrailButtomMethodologyTypes();
                        if(methTypeMap.containsKey(grailBriefTemplate.getMethodologyType())) {
                            data.append("\"" + methTypeMap.get(grailBriefTemplate.getMethodologyType()) + "\"").append(",");
                        } else {
                            data.append("").append(",");
                        }
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getDeliveryDate() != null) {
                        data.append("\"" + dateFormat.format(grailBriefTemplate.getDeliveryDate()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getOutputFormat() != null && grailBriefTemplate.getOutputFormat() > 0) {
                        data.append("\"" + GrailGlobals.BriefTemplateOutputType.getById(grailBriefTemplate.getOutputFormat().intValue()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getBatContact() != null && grailBriefTemplate.getBatContact() > 0) {
                        data.append("\"" + SynchroUtils.getUserDisplayName(grailBriefTemplate.getBatContact()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(grailBriefTemplate.getFinalCost() != null) {
                        data.append("\"" + currencyFormatter.format(grailBriefTemplate.getFinalCost()) + "\"").append(",");
                    } else {
                        data.append("0").append(",");
                    }

                    if(grailBriefTemplate.getFinalCostCurrency() != null && grailBriefTemplate.getFinalCostCurrency() > 0) {
                        if(SynchroGlobal.getCurrencies().containsKey(grailBriefTemplate.getFinalCostCurrency().intValue())) {
                            data.append("\"" + SynchroGlobal.getCurrencies().get(grailBriefTemplate.getFinalCostCurrency().intValue()) + "\"").append(",");
                            Double rate = SynchroUtils.getCurrencyExchangeRate(grailBriefTemplate.getFinalCostCurrency());
                            if(rate != null && rate > 0) {
                                data.append("\"" + currencyFormatter.format(BigDecimal.valueOf(rate * (grailBriefTemplate.getFinalCost() != null ? grailBriefTemplate.getFinalCost().doubleValue() : 0))) + "\"");
                            } else {
                                data.append("\"" + (grailBriefTemplate.getFinalCost() != null?currencyFormatter.format(grailBriefTemplate.getFinalCost()):0) + "\"");
                            }
                        } else {
                            data.append("NONE").append(",");
                            data.append("0");
                        }

                    } else {
                        data.append("NONE").append(",");
                        data.append("0");
                    }

//                    if(tenderingCostCurrency != null && tenderingCostCurrency > 0) {
//                        Double rate = SynchroUtils.getCurrencyExchangeRate(tenderingCostCurrency);
//                        if(rate != null && rate > 0) {
//                            rawExtractReportBean.setTenderingCostDBPRate(BigDecimal.valueOf(rate * tenderingCost.doubleValue()));
//                        } else {
//                            rawExtractReportBean.setTenderingCostDBPRate(tenderingCost);
//                        }
//                    } else {
//                        rawExtractReportBean.setTenderingCostDBPRate(tenderingCost);
//                        rawExtractReportBean.setTenderingCostCurrency("GBP");
//                    }

                    report.append(data.toString()).append("\n");
                }
            }
        }

        downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
        
      //Audit Logs
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.GRAIL.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
				0, "Generate Report", "", -1L, getUser().getID());
        
        return DOWNLOAD_REPORT;

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

    public GrailBriefTemplateManager getGrailBriefTemplateManager() {
        return grailBriefTemplateManager;
    }

    public void setGrailBriefTemplateManager(GrailBriefTemplateManager grailBriefTemplateManager) {
        this.grailBriefTemplateManager = grailBriefTemplateManager;
    }
}
