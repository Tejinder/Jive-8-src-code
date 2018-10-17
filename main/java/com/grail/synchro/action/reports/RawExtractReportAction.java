package com.grail.synchro.action.reports;

import com.grail.synchro.beans.RawExtractReportBean;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/27/14
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class RawExtractReportAction extends JiveActionSupport {
    private final Logger LOG = Logger.getLogger(RawExtractReportAction.class);
    private PermissionManager permissionManager;

    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private SynchroReportManager synchroReportManager;

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


        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        LOG.info("Downloading Raw extract by " +getUser().getFirstName() + " " + getUser().getLastName() + " on " + new Date());
        Calendar calendar = Calendar.getInstance();
        downloadFilename = "Raw Extract_" +
                calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) +
                ".csv";

        StringBuilder report = new StringBuilder();

        StringBuilder header = new StringBuilder();
        header.append("Project Code").append(",");
        header.append("Project Name").append(",");
        header.append("Project Type").append(",");
        header.append("Project Status").append(",");
        header.append("Project Start").append(",");
        header.append("Project End").append(",");
        header.append("Budget Year").append(",");
        header.append("SPI Contact").append(",");
        header.append("Project Owner").append(",");
        header.append("Market").append(",");
        header.append("Methodology Type").append(",");
        header.append("Methodology").append(",");
        header.append("Data Collection Method").append(",");
        header.append("Brand").append(",");
        header.append("Supplier").append(",");
        header.append("CAP Rating").append(",");
        header.append("Latest Project Cost").append(",");
        header.append("Latest Project Cost - Currency").append(",");
        header.append("Latest Project Cost (GBP)").append(",");
        header.append("Tendering Cost").append(",");
        header.append("Tendering Cost - Currency").append(",");
        header.append("Tendering Cost (GBP)").append(",");
        header.append("International Management Cost - Research Hub Cost").append(",");
        header.append("International Management Cost - Research Hub Cost - Currency").append(",");
        header.append("International Management Cost - Research Hub Cost (GBP)").append(",");
        header.append("Local Management Cost").append(",");
        header.append("Local Management Cost - Currency").append(",");
        header.append("Local Management Cost (GBP)").append(",");
        header.append("Fieldwork Cost").append(",");
        header.append("Fieldwork Cost - Currency").append(",");
        header.append("Fieldwork Cost (GBP)").append(",");
        header.append("Operational Hub Cost").append(",");
        header.append("Operational Hub Cost - Currency").append(",");
        header.append("Operational Hub Cost - Currency (GBP)").append(",");
        header.append("Other Cost").append(",");
        header.append("Other Cost - Currency").append(",");
        header.append("Other Cost - Currency (GBP)");

        report.append(header.toString()).append("\n");

        DecimalFormat currencyFormatter = new DecimalFormat("#,###0.00000000000000000000000000000000000");

        String keyword = request.getParameter("keyword");
        StandardReportFilter filter = null;
        if(keyword != null && !keyword.equals("")) {
            filter = new StandardReportFilter();
            filter.setKeyword(keyword);
        }
        List<RawExtractReportBean> reportBeans = synchroReportManager.getRawExtractReport(filter);
        if(reportBeans != null && reportBeans.size() > 0) {
            for(RawExtractReportBean bean:reportBeans) {
                StringBuilder data = new StringBuilder();
                if(bean.getProjectId() != null) {
                    data.append(SynchroUtils.generateProjectCode(bean.getProjectId())).append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getProjectName() != null && !bean.getProjectName().equals("")) {
                    data.append("\"").append(bean.getProjectName().replaceAll("\"","\\\'")).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getMultiMarket() != null) {
                    if(bean.getMultiMarket()) {
                        data.append("\"").append("Multi Market").append("\"").append(",");
                    } else {
                        data.append("\"").append("Single Market").append("\"").append(",");
                    }
                } else {
                    data.append(" ").append(",");
                }

                if(bean.getProjectStatus() != null && !bean.getProjectStatus().equals("")) {
                    data.append("\"").append(bean.getProjectStatus()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }

                if(bean.getProjectStartDate() != null) {
                    String startDateStr = dateFormat.format(bean.getProjectStartDate());
                    data.append("\"").append(startDateStr).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getProjectEndDate() != null) {
                    String endDateStr = dateFormat.format(bean.getProjectEndDate());
                    data.append("\"").append(endDateStr).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }

                if(bean.getBudgetYear() != null && bean.getBudgetYear() > 0) {
                    data.append("\"").append(bean.getBudgetYear()).append("\"").append(",");
                } else {
                    data.append("NONE").append(",");
                }

                if(bean.getSpiContact() != null && !bean.getSpiContact().equals("")) {
                    data.append("\"").append(bean.getSpiContact()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getProjectOwner() != null && !bean.getProjectOwner().equals("")) {
                    data.append("\"").append(bean.getProjectOwner()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }

                if(bean.getMarket() != null && !bean.getMarket().equals("")) {
                    data.append("\"").append(bean.getMarket()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getMethodologyType() != null && !bean.getMethodologyType().equals("")) {
                    data.append("\"").append(bean.getMethodologyType()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getMethodology() != null && !bean.getMethodology().equals("")) {
                    data.append("\"").append(bean.getMethodology()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getDataCollectionMethod() != null && !bean.getDataCollectionMethod().equals("")) {
                    data.append("\"").append(bean.getDataCollectionMethod()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getBrand() != null && !bean.getBrand().equals("")) {
                    data.append("\"").append(bean.getBrand()).append("\"").append(",");
                } else {
                    data.append(" ").append(",");
                }
                if(bean.getSupplier() != null && !bean.getSupplier().equals("")) {
                    data.append("\"").append(bean.getSupplier()).append("\"").append(",");
                } else {
                    data.append("Not Defined").append(",");
                }
                if(bean.getCapRating() != null && !bean.getCapRating().equals("")) {
                    data.append("\"").append(bean.getCapRating()).append("\"").append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getLatestProjectCost() != null
                        && bean.getLatestProjectCost().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getLatestProjectCost())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }
                if(bean.getLatestProjectCostCurrency() != null
                        && !bean.getLatestProjectCostCurrency().equals("")) {
                    data.append(bean.getLatestProjectCostCurrency()).append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getLatestProjectCostDBPRate() != null && bean.getLatestProjectCostDBPRate().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getLatestProjectCostDBPRate())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }

                if(bean.getTenderingCost() != null
                        && bean.getTenderingCost().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getTenderingCost())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }
                if(bean.getTenderingCostCurrency() != null
                        && !bean.getTenderingCostCurrency().equals("")) {
                    data.append(bean.getTenderingCostCurrency()).append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getTenderingCostDBPRate() != null && bean.getTenderingCostDBPRate().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getTenderingCostDBPRate())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }

                if(bean.getProposalInternationalMgmtCost() != null
                        && bean.getProposalInternationalMgmtCost().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalInternationalMgmtCost())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }
                if(bean.getProposalInternationalMgmtCostCurrency() != null
                        && !bean.getProposalInternationalMgmtCostCurrency().equals("")) {
                    data.append(bean.getProposalInternationalMgmtCostCurrency()).append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getProposalInternationalMgmtCostDBPRate() != null && bean.getProposalInternationalMgmtCostDBPRate().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalInternationalMgmtCostDBPRate())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }

                if(bean.getProposalLocalMgmtCost() != null
                        && bean.getProposalLocalMgmtCost().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalLocalMgmtCost())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }
                if(bean.getProposalLocalMgmtCostCurrency() != null
                        && !bean.getProposalLocalMgmtCostCurrency().equals("")) {
                    data.append(bean.getProposalLocalMgmtCostCurrency()).append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getProposalLocalMgmtCostDBPRate() != null && bean.getProposalLocalMgmtCostDBPRate().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalLocalMgmtCostDBPRate())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }

                if(bean.getProposalFieldworkCost() != null
                        && bean.getProposalFieldworkCost().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalFieldworkCost())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }
                if(bean.getProposalFieldworkCostCurrency() != null
                        && !bean.getProposalFieldworkCostCurrency().equals("")) {
                    data.append(bean.getProposalFieldworkCostCurrency()).append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getProposalFieldworkCostDBPRate() != null && bean.getProposalFieldworkCostDBPRate().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalFieldworkCostDBPRate())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }

                if(bean.getProposalOperationHubCost() != null
                        && bean.getProposalOperationHubCost().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalOperationHubCost())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }
                if(bean.getProposalOperationHubCostCurrency() != null
                        && !bean.getProposalOperationHubCostCurrency().equals("")) {
                    data.append(bean.getProposalOperationHubCostCurrency()).append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getProposalOperationHubCostDBPRate() != null && bean.getProposalOperationHubCostDBPRate().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalOperationHubCostDBPRate())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }

                if(bean.getProposalOtherCost() != null
                        && bean.getProposalOtherCost().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalOtherCost())).append("\"").append(",");
                } else {
                    data.append("0").append(",");
                }
                if(bean.getProposalOtherCostCurrency() != null
                        && !bean.getProposalOtherCostCurrency().equals("")) {
                    data.append(bean.getProposalOtherCostCurrency()).append(",");
                } else {
                    data.append("NONE").append(",");
                }
                if(bean.getProposalOtherCostDBPRate() != null && bean.getProposalOtherCostDBPRate().doubleValue() > 0) {
                    data.append("\"").append(currencyFormatter.format(bean.getProposalOtherCostDBPRate())).append("\"");
                } else {
                    data.append("0");
                }

                report.append(data.toString()).append("\n");
            }
        }

        downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
        return DOWNLOAD_REPORT;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public SynchroReportManager getSynchroReportManager() {
        return synchroReportManager;
    }

    public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
        this.synchroReportManager = synchroReportManager;
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


}
