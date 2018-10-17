package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCostsBean {

    private Long id;
    private Long projectId;
    private Long endmarketId;
    private Boolean isMultiMarket = false;
    private Integer stage = -1;
    private Date projectStartDate;
    private Date projectEndDate;
    private BigDecimal estimatedCost = new BigDecimal(0);
    private Long estimatedCostCurrency = -1L;
    private BigDecimal latestEstimatedCost = new BigDecimal(0);
    private Long latestEstimatedCostCurrency = -1L;
    private Boolean isAboveMarket = false;
    private Long investmentType;
    private Long spiContact;
    private Long projectOwner;
    private Long agency1;
    private Long agency1optional;
    private Long agency2;
    private Long agency2optional;
    private Long agency3;
    private Long agency3optional;
    private String agency1Department;
    private String agency2Department;
    private String agency3Department;
    private Long awardedAgency;
    private BigDecimal proposalTotalCost = new BigDecimal(0);
    private Long proposalTotalCostCurrency = -1L;
    private BigDecimal proposalInitialMgmtCost = new BigDecimal(0);
    private Long proposalInitialMgmtCostCurrency = -1L;
    private BigDecimal proposalLocalMgmtCost = new BigDecimal(0);
    private Long proposalLocalMgmtCostCurrency = -1L;
    private BigDecimal proposalFieldworkCost = new BigDecimal(0);
    private Long proposalFieldworkCostCurrency = -1L;
    private BigDecimal proposalOperationHubCost = new BigDecimal(0);
    private Long proposalOperationHubCostCurrency = -1L;
    private BigDecimal proposalOtherCost = new BigDecimal(0);
    private Long proposalOtherCostCurrency = -1L;
    private BigDecimal psOriginalCost = new BigDecimal(0);
    private Long psOriginalCostCurrency = -1L;
    private BigDecimal psFinalCost = new BigDecimal(0);
    private Long psFinalCostCurrency = -1L;
    private BigDecimal totalProjectCost = new BigDecimal(0);
    private Long totalProjectCostCurrency = -1L;
    private BigDecimal latestProjectCost = new BigDecimal(0);
    private Long latestProjectCostCurrency = -1L;
    private BigDecimal tenderingCost = new BigDecimal(0);
    private Long tenderingCostCurrency = -1L;
    private String datacollection;
    private Date capturedDate;
    private Date updatedDate;
    private Long projectStatus;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEndmarketId() {
        return endmarketId;
    }

    public void setEndmarketId(Long endmarketId) {
        this.endmarketId = endmarketId;
    }

    public Boolean getMultiMarket() {
        return isMultiMarket;
    }

    public void setMultiMarket(Boolean multiMarket) {
        isMultiMarket = multiMarket;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
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

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Long getEstimatedCostCurrency() {
        return estimatedCostCurrency;
    }

    public void setEstimatedCostCurrency(Long estimatedCostCurrency) {
        this.estimatedCostCurrency = estimatedCostCurrency;
    }


    public BigDecimal getLatestEstimatedCost() {
        return latestEstimatedCost;
    }

    public void setLatestEstimatedCost(BigDecimal latestEstimatedCost) {
        this.latestEstimatedCost = latestEstimatedCost;
    }

    public Long getLatestEstimatedCostCurrency() {
        return latestEstimatedCostCurrency;
    }

    public void setLatestEstimatedCostCurrency(Long latestEstimatedCostCurrency) {
        this.latestEstimatedCostCurrency = latestEstimatedCostCurrency;
    }

    public Boolean getAboveMarket() {
        return isAboveMarket;
    }

    public void setAboveMarket(Boolean aboveMarket) {
        isAboveMarket = aboveMarket;
    }

    public Long getInvestmentType() {
        return investmentType;
    }

    public void setInvestmentType(Long investmentType) {
        this.investmentType = investmentType;
    }

    public Long getSpiContact() {
        return spiContact;
    }

    public void setSpiContact(Long spiContact) {
        this.spiContact = spiContact;
    }

    public Long getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(Long projectOwner) {
        this.projectOwner = projectOwner;
    }

    public Long getAgency1() {
        return agency1;
    }

    public void setAgency1(Long agency1) {
        this.agency1 = agency1;
    }

    public Long getAgency1optional() {
        return agency1optional;
    }

    public void setAgency1optional(Long agency1optional) {
        this.agency1optional = agency1optional;
    }

    public Long getAgency2() {
        return agency2;
    }

    public void setAgency2(Long agency2) {
        this.agency2 = agency2;
    }

    public Long getAgency2optional() {
        return agency2optional;
    }

    public void setAgency2optional(Long agency2optional) {
        this.agency2optional = agency2optional;
    }

    public Long getAgency3() {
        return agency3;
    }

    public void setAgency3(Long agency3) {
        this.agency3 = agency3;
    }

    public Long getAgency3optional() {
        return agency3optional;
    }

    public void setAgency3optional(Long agency3optional) {
        this.agency3optional = agency3optional;
    }

    public String getAgency1Department() {
        return agency1Department;
    }

    public void setAgency1Department(String agency1Department) {
        this.agency1Department = agency1Department;
    }

    public String getAgency2Department() {
        return agency2Department;
    }

    public void setAgency2Department(String agency2Department) {
        this.agency2Department = agency2Department;
    }

    public String getAgency3Department() {
        return agency3Department;
    }

    public void setAgency3Department(String agency3Department) {
        this.agency3Department = agency3Department;
    }

    public Long getAwardedAgency() {
        return awardedAgency;
    }

    public void setAwardedAgency(Long awardedAgency) {
        this.awardedAgency = awardedAgency;
    }

    public BigDecimal getProposalTotalCost() {
        return proposalTotalCost;
    }

    public void setProposalTotalCost(BigDecimal proposalTotalCost) {
        this.proposalTotalCost = proposalTotalCost;
    }

    public Long getProposalTotalCostCurrency() {
        return proposalTotalCostCurrency;
    }

    public void setProposalTotalCostCurrency(Long proposalTotalCostCurrency) {
        this.proposalTotalCostCurrency = proposalTotalCostCurrency;
    }

    public BigDecimal getProposalInitialMgmtCost() {
        return proposalInitialMgmtCost;
    }

    public void setProposalInitialMgmtCost(BigDecimal proposalInitialMgmtCost) {
        this.proposalInitialMgmtCost = proposalInitialMgmtCost;
    }

    public Long getProposalInitialMgmtCostCurrency() {
        return proposalInitialMgmtCostCurrency;
    }

    public void setProposalInitialMgmtCostCurrency(Long proposalInitialMgmtCostCurrency) {
        this.proposalInitialMgmtCostCurrency = proposalInitialMgmtCostCurrency;
    }

    public BigDecimal getProposalLocalMgmtCost() {
        return proposalLocalMgmtCost;
    }

    public void setProposalLocalMgmtCost(BigDecimal proposalLocalMgmtCost) {
        this.proposalLocalMgmtCost = proposalLocalMgmtCost;
    }

    public Long getProposalLocalMgmtCostCurrency() {
        return proposalLocalMgmtCostCurrency;
    }

    public void setProposalLocalMgmtCostCurrency(Long proposalLocalMgmtCostCurrency) {
        this.proposalLocalMgmtCostCurrency = proposalLocalMgmtCostCurrency;
    }

    public BigDecimal getProposalFieldworkCost() {
        return proposalFieldworkCost;
    }

    public void setProposalFieldworkCost(BigDecimal proposalFieldworkCost) {
        this.proposalFieldworkCost = proposalFieldworkCost;
    }

    public Long getProposalFieldworkCostCurrency() {
        return proposalFieldworkCostCurrency;
    }

    public void setProposalFieldworkCostCurrency(Long proposalFieldworkCostCurrency) {
        this.proposalFieldworkCostCurrency = proposalFieldworkCostCurrency;
    }

    public BigDecimal getProposalOperationHubCost() {
        return proposalOperationHubCost;
    }

    public void setProposalOperationHubCost(BigDecimal proposalOperationHubCost) {
        this.proposalOperationHubCost = proposalOperationHubCost;
    }

    public Long getProposalOperationHubCostCurrency() {
        return proposalOperationHubCostCurrency;
    }

    public void setProposalOperationHubCostCurrency(Long proposalOperationHubCostCurrency) {
        this.proposalOperationHubCostCurrency = proposalOperationHubCostCurrency;
    }

    public BigDecimal getProposalOtherCost() {
        return proposalOtherCost;
    }

    public void setProposalOtherCost(BigDecimal proposalOtherCost) {
        this.proposalOtherCost = proposalOtherCost;
    }

    public Long getProposalOtherCostCurrency() {
        return proposalOtherCostCurrency;
    }

    public void setProposalOtherCostCurrency(Long proposalOtherCostCurrency) {
        this.proposalOtherCostCurrency = proposalOtherCostCurrency;
    }

    public BigDecimal getPsOriginalCost() {
        return psOriginalCost;
    }

    public void setPsOriginalCost(BigDecimal psOriginalCost) {
        this.psOriginalCost = psOriginalCost;
    }

    public Long getPsOriginalCostCurrency() {
        return psOriginalCostCurrency;
    }

    public void setPsOriginalCostCurrency(Long psOriginalCostCurrency) {
        this.psOriginalCostCurrency = psOriginalCostCurrency;
    }

    public BigDecimal getPsFinalCost() {
        return psFinalCost;
    }

    public void setPsFinalCost(BigDecimal psFinalCost) {
        this.psFinalCost = psFinalCost;
    }

    public Long getPsFinalCostCurrency() {
        return psFinalCostCurrency;
    }

    public void setPsFinalCostCurrency(Long psFinalCostCurrency) {
        this.psFinalCostCurrency = psFinalCostCurrency;
    }

    public BigDecimal getTotalProjectCost() {
        return totalProjectCost;
    }

    public void setTotalProjectCost(BigDecimal totalProjectCost) {
        this.totalProjectCost = totalProjectCost;
    }

    public Long getTotalProjectCostCurrency() {
        return totalProjectCostCurrency;
    }

    public void setTotalProjectCostCurrency(Long totalProjectCostCurrency) {
        this.totalProjectCostCurrency = totalProjectCostCurrency;
    }

    public BigDecimal getLatestProjectCost() {
        return latestProjectCost;
    }

    public void setLatestProjectCost(BigDecimal latestProjectCost) {
        this.latestProjectCost = latestProjectCost;
    }

    public Long getLatestProjectCostCurrency() {
        return latestProjectCostCurrency;
    }

    public void setLatestProjectCostCurrency(Long latestProjectCostCurrency) {
        this.latestProjectCostCurrency = latestProjectCostCurrency;
    }

    public BigDecimal getTenderingCost() {
        return tenderingCost;
    }

    public void setTenderingCost(BigDecimal tenderingCost) {
        this.tenderingCost = tenderingCost;
    }

    public Long getTenderingCostCurrency() {
        return tenderingCostCurrency;
    }

    public void setTenderingCostCurrency(Long tenderingCostCurrency) {
        this.tenderingCostCurrency = tenderingCostCurrency;
    }

    public String getDatacollection() {
        return datacollection;
    }

    public void setDatacollection(String datacollection) {
        this.datacollection = datacollection;
    }

    public Date getCapturedDate() {
        return capturedDate;
    }

    public void setCapturedDate(Date capturedDate) {
        this.capturedDate = capturedDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(Long projectStatus) {
        this.projectStatus = projectStatus;
    }
}
