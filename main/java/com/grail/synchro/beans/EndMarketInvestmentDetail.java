package com.grail.synchro.beans;

import java.math.BigDecimal;

/**
 * @author Kanwar Grewal
 * @version 1.0,
 */
public class EndMarketInvestmentDetail extends BeanObject {

    private Long projectID;
    private Long endMarketID;
    private BigDecimal initialCost;
    private Long initialCostCurrency;
    private Long spiContact;
    private Long investmentType;
    private String marketName;
    private Boolean approved;
    private BigDecimal latestEstimateCost;
    private Long latestEstimateCostCurrency;
    private Integer status; 
    private Long referenceSynchroCode;
    private boolean isFundingMarket;

    public EndMarketInvestmentDetail() {
    }

    public EndMarketInvestmentDetail(final Long projectID, final Long endMarketID) {
        final Long currentTime = System.currentTimeMillis();
        this.projectID = projectID;
        this.endMarketID = endMarketID;
        setCreationDate(currentTime);
        setModifiedDate(currentTime);
    }

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

	public Long getEndMarketID() {
		return endMarketID;
	}

	public void setEndMarketID(Long endMarketID) {
		this.endMarketID = endMarketID;
	}

	public BigDecimal getInitialCost() {
		return initialCost;
	}

	public void setInitialCost(BigDecimal initialCost) {
		this.initialCost = initialCost;
	}

	public Long getInitialCostCurrency() {
		return initialCostCurrency;
	}

	public void setInitialCostCurrency(Long initialCostCurrency) {
		this.initialCostCurrency = initialCostCurrency;
	}

	public Long getSpiContact() {
		return spiContact;
	}

	public void setSpiContact(Long spiContact) {
		this.spiContact = spiContact;
	}

	public Long getInvestmentType() {
		return investmentType;
	}

	public void setInvestmentType(Long investmentType) {
		this.investmentType = investmentType;
	}

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	public BigDecimal getLatestEstimateCost() {
		return latestEstimateCost;
	}

	public void setLatestEstimateCost(BigDecimal latestEstimateCost) {
		this.latestEstimateCost = latestEstimateCost;
	}

	public Long getLatestEstimateCostCurrency() {
		return latestEstimateCostCurrency;
	}

	public void setLatestEstimateCostCurrency(Long latestEstimateCostCurrency) {
		this.latestEstimateCostCurrency = latestEstimateCostCurrency;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getReferenceSynchroCode() {
		return referenceSynchroCode;
	}

	public void setReferenceSynchroCode(Long referenceSynchroCode) {
		this.referenceSynchroCode = referenceSynchroCode;
	}

	public boolean getIsFundingMarket() {
		return isFundingMarket;
	}

	public void setIsFundingMarket(boolean isFundingMarket) {
		this.isFundingMarket = isFundingMarket;
	}

	
}
