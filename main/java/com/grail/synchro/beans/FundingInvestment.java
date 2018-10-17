package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Kanwar Grewal
 * @version 4.1, Date: 3/6/2014
 */
public class FundingInvestment extends BeanObject {
    
	private Long investmentID;
	private Long projectID;
	private Boolean aboveMarket;
    private Long investmentType;
    private Long investmentTypeID;
    private Long fieldworkMarketID;
    private Long fundingMarketID;    
    private Long projectContact;
    private Long spiContact;
    private BigDecimal estimatedCost;
    private Long estimatedCostCurrency;
    private Boolean approved;
    private Boolean approvalStatus;
    private Integer endmarketStatus;
    private Date approvalDate;
    
	public Long getInvestmentID() {
		return investmentID;
	}
	public void setInvestmentID(Long investmentID) {
		this.investmentID = investmentID;
	}
	public Long getInvestmentType() {
		return investmentType;
	}
	public void setInvestmentType(Long investmentType) {
		this.investmentType = investmentType;
	}
	public Long getInvestmentTypeID() {
		return investmentTypeID;
	}
	public void setInvestmentTypeID(Long investmentTypeID) {
		this.investmentTypeID = investmentTypeID;
	}
	public Long getFieldworkMarketID() {
		return fieldworkMarketID;
	}
	public void setFieldworkMarketID(Long fieldworkMarketID) {
		this.fieldworkMarketID = fieldworkMarketID;
	}
	public Long getFundingMarketID() {
		return fundingMarketID;
	}
	public void setFundingMarketID(Long fundingMarketID) {
		this.fundingMarketID = fundingMarketID;
	}
	public Long getProjectContact() {
		return projectContact;
	}
	public void setProjectContact(Long projectContact) {
		this.projectContact = projectContact;
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
	public Boolean getAboveMarket() {
		return aboveMarket;
	}
	public void setAboveMarket(Boolean aboveMarket) {
		this.aboveMarket = aboveMarket;
	}
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Boolean getApproved() {
		return approved;
	}
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}
	public Long getSpiContact() {
		return spiContact;
	}
	public void setSpiContact(Long spiContact) {
		this.spiContact = spiContact;
	}
	public Boolean getApprovalStatus() {
		return approvalStatus;
	}
	
	public void setApprovalStatus(Boolean approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
	public Integer getEndmarketStatus() {
		return endmarketStatus;
	}
	public void setEndmarketStatus(Integer endmarketStatus) {
		this.endmarketStatus = endmarketStatus;
	}
	public Date getApprovalDate() {
		return approvalDate;
	}
	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}
	
}
