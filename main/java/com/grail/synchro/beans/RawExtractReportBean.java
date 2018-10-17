package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/28/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class RawExtractReportBean {
    private Long projectId;
    private String projectName;
    private Boolean isMultiMarket;
    private String market;
    private Long marketId;
    private Date projectStartDate;
    private Date projectEndDate;
    private Integer budgetYear;
    private String methodology;
    private String methodologyType;
    private String brand;
    private String supplier;
    private String capRating;

    private Boolean isAboveMarket;
    private String spiContact;
    private String projectOwner;

    private BigDecimal proposalTotalCost = new BigDecimal(0);
    private String proposalTotalCostCurrency;
    private BigDecimal proposalTotalCostDBPRate;
    private BigDecimal proposalInternationalMgmtCost = new BigDecimal(0);
    private String proposalInternationalMgmtCostCurrency;
    private BigDecimal proposalInternationalMgmtCostDBPRate;
    private BigDecimal proposalLocalMgmtCost = new BigDecimal(0);
    private String proposalLocalMgmtCostCurrency;
    private BigDecimal proposalLocalMgmtCostDBPRate;
    private BigDecimal proposalFieldworkCost = new BigDecimal(0);
    private String proposalFieldworkCostCurrency;
    private BigDecimal proposalFieldworkCostDBPRate;
    private BigDecimal proposalOperationHubCost = new BigDecimal(0);
    private String proposalOperationHubCostCurrency;
    private BigDecimal proposalOperationHubCostDBPRate;
    private BigDecimal proposalOtherCost = new BigDecimal(0);
    private String proposalOtherCostCurrency;
    private BigDecimal proposalOtherCostDBPRate;
    private BigDecimal totalProjectCost = new BigDecimal(0);
    private String totalProjectCostCurrency;
    private BigDecimal totalProjectCostDBPRate;
    private BigDecimal latestProjectCost;
    private String latestProjectCostCurrency;
    private BigDecimal latestProjectCostDBPRate;

    private BigDecimal tenderingCost;
    private String tenderingCostCurrency;
    private BigDecimal tenderingCostDBPRate;


    private String dataCollectionMethod;
    private String projectStatus;


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Boolean getMultiMarket() {
        return isMultiMarket;
    }

    public void setMultiMarket(Boolean multiMarket) {
        isMultiMarket = multiMarket;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Date getProjectStartDate() {
        return projectStartDate;
    }

    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    public Date getProjectEndDate() {
        return projectEndDate;
    }

    public void setProjectEndDate(Date projectEndDate) {
        this.projectEndDate = projectEndDate;
    }

    public Integer getBudgetYear() {
        return budgetYear;
    }

    public void setBudgetYear(Integer budgetYear) {
        this.budgetYear = budgetYear;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public String getMethodologyType() {
        return methodologyType;
    }

    public void setMethodologyType(String methodologyType) {
        this.methodologyType = methodologyType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCapRating() {
        return capRating;
    }

    public void setCapRating(String capRating) {
        this.capRating = capRating;
    }

    public Boolean getAboveMarket() {
        return isAboveMarket;
    }

    public void setAboveMarket(Boolean aboveMarket) {
        isAboveMarket = aboveMarket;
    }

    public String getSpiContact() {
        return spiContact;
    }

    public void setSpiContact(String spiContact) {
        this.spiContact = spiContact;
    }

    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }

    public BigDecimal getProposalTotalCost() {
        return proposalTotalCost;
    }

    public void setProposalTotalCost(BigDecimal proposalTotalCost) {
        this.proposalTotalCost = proposalTotalCost;
    }

    public String getProposalTotalCostCurrency() {
        return proposalTotalCostCurrency;
    }

    public void setProposalTotalCostCurrency(String proposalTotalCostCurrency) {
        this.proposalTotalCostCurrency = proposalTotalCostCurrency;
    }

    public BigDecimal getProposalTotalCostDBPRate() {
        return proposalTotalCostDBPRate;
    }

    public void setProposalTotalCostDBPRate(BigDecimal proposalTotalCostDBPRate) {
        this.proposalTotalCostDBPRate = proposalTotalCostDBPRate;
    }

    public BigDecimal getProposalInternationalMgmtCost() {
        return proposalInternationalMgmtCost;
    }

    public void setProposalInternationalMgmtCost(BigDecimal proposalInternationalMgmtCost) {
        this.proposalInternationalMgmtCost = proposalInternationalMgmtCost;
    }

    public String getProposalInternationalMgmtCostCurrency() {
        return proposalInternationalMgmtCostCurrency;
    }

    public void setProposalInternationalMgmtCostCurrency(String proposalInternationalMgmtCostCurrency) {
        this.proposalInternationalMgmtCostCurrency = proposalInternationalMgmtCostCurrency;
    }

    public BigDecimal getProposalInternationalMgmtCostDBPRate() {
        return proposalInternationalMgmtCostDBPRate;
    }

    public void setProposalInternationalMgmtCostDBPRate(BigDecimal proposalInternationalMgmtCostDBPRate) {
        this.proposalInternationalMgmtCostDBPRate = proposalInternationalMgmtCostDBPRate;
    }

    public BigDecimal getProposalLocalMgmtCost() {
        return proposalLocalMgmtCost;
    }

    public void setProposalLocalMgmtCost(BigDecimal proposalLocalMgmtCost) {
        this.proposalLocalMgmtCost = proposalLocalMgmtCost;
    }

    public String getProposalLocalMgmtCostCurrency() {
        return proposalLocalMgmtCostCurrency;
    }

    public void setProposalLocalMgmtCostCurrency(String proposalLocalMgmtCostCurrency) {
        this.proposalLocalMgmtCostCurrency = proposalLocalMgmtCostCurrency;
    }

    public BigDecimal getProposalLocalMgmtCostDBPRate() {
        return proposalLocalMgmtCostDBPRate;
    }

    public void setProposalLocalMgmtCostDBPRate(BigDecimal proposalLocalMgmtCostDBPRate) {
        this.proposalLocalMgmtCostDBPRate = proposalLocalMgmtCostDBPRate;
    }

    public BigDecimal getProposalFieldworkCost() {
        return proposalFieldworkCost;
    }

    public void setProposalFieldworkCost(BigDecimal proposalFieldworkCost) {
        this.proposalFieldworkCost = proposalFieldworkCost;
    }

    public String getProposalFieldworkCostCurrency() {
        return proposalFieldworkCostCurrency;
    }

    public void setProposalFieldworkCostCurrency(String proposalFieldworkCostCurrency) {
        this.proposalFieldworkCostCurrency = proposalFieldworkCostCurrency;
    }

    public BigDecimal getProposalFieldworkCostDBPRate() {
        return proposalFieldworkCostDBPRate;
    }

    public void setProposalFieldworkCostDBPRate(BigDecimal proposalFieldworkCostDBPRate) {
        this.proposalFieldworkCostDBPRate = proposalFieldworkCostDBPRate;
    }

    public BigDecimal getProposalOperationHubCost() {
        return proposalOperationHubCost;
    }

    public void setProposalOperationHubCost(BigDecimal proposalOperationHubCost) {
        this.proposalOperationHubCost = proposalOperationHubCost;
    }

    public String getProposalOperationHubCostCurrency() {
        return proposalOperationHubCostCurrency;
    }

    public void setProposalOperationHubCostCurrency(String proposalOperationHubCostCurrency) {
        this.proposalOperationHubCostCurrency = proposalOperationHubCostCurrency;
    }

    public BigDecimal getProposalOperationHubCostDBPRate() {
        return proposalOperationHubCostDBPRate;
    }

    public void setProposalOperationHubCostDBPRate(BigDecimal proposalOperationHubCostDBPRate) {
        this.proposalOperationHubCostDBPRate = proposalOperationHubCostDBPRate;
    }

    public BigDecimal getProposalOtherCost() {
        return proposalOtherCost;
    }

    public void setProposalOtherCost(BigDecimal proposalOtherCost) {
        this.proposalOtherCost = proposalOtherCost;
    }

    public String getProposalOtherCostCurrency() {
        return proposalOtherCostCurrency;
    }

    public void setProposalOtherCostCurrency(String proposalOtherCostCurrency) {
        this.proposalOtherCostCurrency = proposalOtherCostCurrency;
    }

    public BigDecimal getProposalOtherCostDBPRate() {
        return proposalOtherCostDBPRate;
    }

    public void setProposalOtherCostDBPRate(BigDecimal proposalOtherCostDBPRate) {
        this.proposalOtherCostDBPRate = proposalOtherCostDBPRate;
    }

    public BigDecimal getTotalProjectCost() {
        return totalProjectCost;
    }

    public void setTotalProjectCost(BigDecimal totalProjectCost) {
        this.totalProjectCost = totalProjectCost;
    }

    public String getTotalProjectCostCurrency() {
        return totalProjectCostCurrency;
    }

    public void setTotalProjectCostCurrency(String totalProjectCostCurrency) {
        this.totalProjectCostCurrency = totalProjectCostCurrency;
    }

    public BigDecimal getTotalProjectCostDBPRate() {
        return totalProjectCostDBPRate;
    }

    public void setTotalProjectCostDBPRate(BigDecimal totalProjectCostDBPRate) {
        this.totalProjectCostDBPRate = totalProjectCostDBPRate;
    }

    public BigDecimal getLatestProjectCost() {
        return latestProjectCost;
    }

    public void setLatestProjectCost(BigDecimal latestProjectCost) {
        this.latestProjectCost = latestProjectCost;
    }

    public String getLatestProjectCostCurrency() {
        return latestProjectCostCurrency;
    }

    public void setLatestProjectCostCurrency(String latestProjectCostCurrency) {
        this.latestProjectCostCurrency = latestProjectCostCurrency;
    }

    public BigDecimal getLatestProjectCostDBPRate() {
        return latestProjectCostDBPRate;
    }

    public void setLatestProjectCostDBPRate(BigDecimal latestProjectCostDBPRate) {
        this.latestProjectCostDBPRate = latestProjectCostDBPRate;
    }

    public BigDecimal getTenderingCost() {
        return tenderingCost;
    }

    public void setTenderingCost(BigDecimal tenderingCost) {
        this.tenderingCost = tenderingCost;
    }

    public String getTenderingCostCurrency() {
        return tenderingCostCurrency;
    }

    public void setTenderingCostCurrency(String tenderingCostCurrency) {
        this.tenderingCostCurrency = tenderingCostCurrency;
    }

    public BigDecimal getTenderingCostDBPRate() {
        return tenderingCostDBPRate;
    }

    public void setTenderingCostDBPRate(BigDecimal tenderingCostDBPRate) {
        this.tenderingCostDBPRate = tenderingCostDBPRate;
    }

    public String getDataCollectionMethod() {
        return dataCollectionMethod;
    }

    public void setDataCollectionMethod(String dataCollectionMethod) {
        this.dataCollectionMethod = dataCollectionMethod;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }
}
