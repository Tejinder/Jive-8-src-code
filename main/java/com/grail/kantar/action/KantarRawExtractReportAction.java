package com.grail.kantar.action;

import com.grail.GrailGlobals;
import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplateFilter;
import com.grail.kantar.manager.KantarBriefTemplateManager;
import com.grail.kantar.util.KantarGlobals;
import com.grail.kantar.util.KantarUtils;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
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
 * Date: 12/11/14
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarRawExtractReportAction extends JiveActionSupport {
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private KantarBriefTemplateManager kantarBriefTemplateManager;


    @Override
    public String execute() {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }

        if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin(getUser())
                || SynchroPermHelper.isSynchroGlobalSuperUser(getUser()))) {
            return UNAUTHORIZED;
        }

        if(!(SynchroPermHelper.canAccessKantarPortal(getUser()) || SynchroPermHelper.isSynchroGlobalSuperUser(getUser()))) {
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

        if(!(SynchroPermHelper.canAccessKantarPortal(getUser()) || SynchroPermHelper.isSynchroGlobalSuperUser(getUser()))) {
            return UNAUTHORIZED;
        }

        LOG.info("Downloading Kantar Raw extract by " +getUser().getFirstName() + " " + getUser().getLastName() + " on " + new Date());
        Calendar calendar = Calendar.getInstance();
        downloadFilename = "Kantar_Raw_Extract_" +
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

        KantarBriefTemplateFilter filter = new KantarBriefTemplateFilter();

        List<KantarBriefTemplate> kantarBriefTemplates = kantarBriefTemplateManager.getAll(filter);
        if(kantarBriefTemplates != null && kantarBriefTemplates.size() > 0) {
            for(KantarBriefTemplate kantarBriefTemplate : kantarBriefTemplates) {
                if(kantarBriefTemplate != null) {
                    StringBuilder data = new StringBuilder();

                    if(kantarBriefTemplate.getId() != null && kantarBriefTemplate.getId() > 0) {
                        data.append("\"" + KantarUtils.generateKantarCode(kantarBriefTemplate.getId()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getCreatedBy() != null) {
                        data.append("\"" + SynchroUtils.getUserDisplayName(kantarBriefTemplate.getCreatedBy()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getCreationDate() != null) {
                        data.append("\"" + dateFormat.format(kantarBriefTemplate.getCreationDate()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getStatus() != null && kantarBriefTemplate.getStatus() > 0) {
                        data.append("\"" + KantarGlobals.KantarBriefTemplateStatusType.getById(kantarBriefTemplate.getStatus()).getName()+ "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getResearchNeedsPriorities() != null) {
                        data.append("\"" + kantarBriefTemplate.getResearchNeedsPriorities() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getHypothesisBusinessNeed() != null) {
                        data.append("\"" + kantarBriefTemplate.getHypothesisBusinessNeed() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getDataSource() != null) {
                        data.append("\"" + kantarBriefTemplate.getDataSource() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getMarkets() != null && kantarBriefTemplate.getMarkets() > 0) {
                        if(SynchroGlobal.getEndMarkets().containsKey(kantarBriefTemplate.getMarkets().intValue())) {
                            data.append("\"" + SynchroGlobal.getEndMarkets().get(kantarBriefTemplate.getMarkets().intValue()) + "\"").append(",");
                        } else {
                            data.append("").append(",");
                        }
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getProducts() != null) {
                        data.append("\"" + kantarBriefTemplate.getProducts() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getBrands() != null) {
                        data.append("\"" + kantarBriefTemplate.getBrands() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getCategories() != null) {
                        data.append("\"" + kantarBriefTemplate.getCategories() + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getMethodologyType() != null) {
                        Map<Long, String> methTypeMap = KantarUtils.getAllKantarButtomMethodologyTypes();
                        if(methTypeMap.containsKey(kantarBriefTemplate.getMethodologyType())) {
                            data.append("\"" + methTypeMap.get(kantarBriefTemplate.getMethodologyType()) + "\"").append(",");
                        } else {
                            data.append("").append(",");
                        }
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getDeliveryDate() != null) {
                        data.append("\"" + dateFormat.format(kantarBriefTemplate.getDeliveryDate()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getOutputFormat() != null && kantarBriefTemplate.getOutputFormat() > 0) {
                        data.append("\"" + KantarGlobals.BriefTemplateOutputType.getById(kantarBriefTemplate.getOutputFormat().intValue()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getBatContact() != null && kantarBriefTemplate.getBatContact() > 0) {
                        data.append("\"" + SynchroUtils.getUserDisplayName(kantarBriefTemplate.getBatContact()) + "\"").append(",");
                    } else {
                        data.append("").append(",");
                    }

                    if(kantarBriefTemplate.getFinalCost() != null) {
                        data.append("\"" + currencyFormatter.format(kantarBriefTemplate.getFinalCost()) + "\"").append(",");
                    } else {
                        data.append("0").append(",");
                    }

                    if(kantarBriefTemplate.getFinalCostCurrency() != null && kantarBriefTemplate.getFinalCostCurrency() > 0) {
                        if(SynchroGlobal.getCurrencies().containsKey(kantarBriefTemplate.getFinalCostCurrency().intValue())) {
                            data.append("\"" + SynchroGlobal.getCurrencies().get(kantarBriefTemplate.getFinalCostCurrency().intValue()) + "\"").append(",");
                            Double rate = SynchroUtils.getCurrencyExchangeRate(kantarBriefTemplate.getFinalCostCurrency());
                            if(rate != null && rate > 0) {
                                data.append("\"" + currencyFormatter.format(BigDecimal.valueOf(rate * (kantarBriefTemplate.getFinalCost() != null?kantarBriefTemplate.getFinalCost().doubleValue():0))) + "\"");
                            } else {
                                data.append("\"" + (kantarBriefTemplate.getFinalCost() != null?currencyFormatter.format(kantarBriefTemplate.getFinalCost()):0) + "\"");
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
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.KANTAR.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
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

    public KantarBriefTemplateManager getKantarBriefTemplateManager() {
        return kantarBriefTemplateManager;
    }

    public void setKantarBriefTemplateManager(KantarBriefTemplateManager kantarBriefTemplateManager) {
        this.kantarBriefTemplateManager = kantarBriefTemplateManager;
    }
}
