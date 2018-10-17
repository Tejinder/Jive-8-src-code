package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.List;

import com.jivesoftware.base.User;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
public class FieldWorkAgencyDetail extends BeanObject {
    private Long projectID;
    private Long coAgencyID;
    private Long fwAgencyID;
    private Long tenderingAgencyID;
    private BigDecimal bidValue;
    private Integer bidValueType;
    private BigDecimal bidValueExchangeRate;
    private Long fwSupplier;
    private Long fwSupplierGroup;
    private Long fwEndMarket;
    private Integer marketType;
    private List<Long> dataCollectionMethod;
    private BigDecimal fwBudPlan;
    private Integer fwBudPlanType;
    private BigDecimal fwBudPlanExchangeRate;
    private BigDecimal fwActualCost;
    private Integer fwActualCostType;
    private BigDecimal fwActualCostExchangeRate;
    private BigDecimal fwQPR1;
    private Integer fwQPR1Type;
    private BigDecimal fwQPR1ExchangeRate;
    private BigDecimal fwQPR2;
    private Integer fwQPR2Type;
    private BigDecimal fwQPR2ExchangeRate;
    private BigDecimal fwQPR3;
    private Integer fwQPR3Type;
    private BigDecimal fwQPR3ExchangeRate;
    private BigDecimal fwQPR4;
    private Integer fwQPR4Type;
    private BigDecimal fwQPR4ExchangeRate;
    public BigDecimal getBidValueExchangeRate() {
		return bidValueExchangeRate;
	}

	public void setBidValueExchangeRate(BigDecimal bidValueExchangeRate) {
		this.bidValueExchangeRate = bidValueExchangeRate;
	}

	public BigDecimal getFwBudPlanExchangeRate() {
		return fwBudPlanExchangeRate;
	}

	public void setFwBudPlanExchangeRate(BigDecimal fwBudPlanExchangeRate) {
		this.fwBudPlanExchangeRate = fwBudPlanExchangeRate;
	}

	public BigDecimal getFwActualCostExchangeRate() {
		return fwActualCostExchangeRate;
	}

	public void setFwActualCostExchangeRate(BigDecimal fwActualCostExchangeRate) {
		this.fwActualCostExchangeRate = fwActualCostExchangeRate;
	}

	public BigDecimal getFwQPR1ExchangeRate() {
		return fwQPR1ExchangeRate;
	}

	public void setFwQPR1ExchangeRate(BigDecimal fwQPR1ExchangeRate) {
		this.fwQPR1ExchangeRate = fwQPR1ExchangeRate;
	}

	public BigDecimal getFwQPR2ExchangeRate() {
		return fwQPR2ExchangeRate;
	}

	public void setFwQPR2ExchangeRate(BigDecimal fwQPR2ExchangeRate) {
		this.fwQPR2ExchangeRate = fwQPR2ExchangeRate;
	}

	public BigDecimal getFwQPR3ExchangeRate() {
		return fwQPR3ExchangeRate;
	}

	public void setFwQPR3ExchangeRate(BigDecimal fwQPR3ExchangeRate) {
		this.fwQPR3ExchangeRate = fwQPR3ExchangeRate;
	}

	public BigDecimal getFwQPR4ExchangeRate() {
		return fwQPR4ExchangeRate;
	}

	public void setFwQPR4ExchangeRate(BigDecimal fwQPR4ExchangeRate) {
		this.fwQPR4ExchangeRate = fwQPR4ExchangeRate;
	}

	private Boolean fwCancelled;
    // The status will be used to perform Soft Delete on the FW Agency Remove feature
    private Integer status;

    private User fwAgency;

    public FieldWorkAgencyDetail() {
    }

    public FieldWorkAgencyDetail(final Long projectID,final Long coAgencyID, final Long fwAgencyID) {
        this.projectID = projectID;
        this.coAgencyID = coAgencyID;
        this.fwAgencyID = fwAgencyID;
    }

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

    public Long getCoAgencyID() {
        return coAgencyID;
    }

    public void setCoAgencyID(final Long coAgencyID) {
        this.coAgencyID = coAgencyID;
    }

    public Long getFwAgencyID() {
        return fwAgencyID;
    }

    public void setFwAgencyID(final Long fwAgencyID) {
        this.fwAgencyID = fwAgencyID;
    }

    public Long getTenderingAgencyID() {
        return tenderingAgencyID;
    }

    public void setTenderingAgencyID(final Long tenderingAgencyID) {
        this.tenderingAgencyID = tenderingAgencyID;
    }

    public BigDecimal getBidValue() {
        return bidValue;
    }

    public void setBidValue(final BigDecimal bidValue) {
        this.bidValue = bidValue;
    }

    public Integer getBidValueType() {
        return bidValueType;
    }

    public void setBidValueType(final Integer bidValueType) {
        this.bidValueType = bidValueType;
    }

    public Long getFwSupplier() {
        return fwSupplier;
    }

    public void setFwSupplier(final Long fwSupplier) {
        this.fwSupplier = fwSupplier;
    }

    public Long getFwSupplierGroup() {
        return fwSupplierGroup;
    }

    public void setFwSupplierGroup(final Long fwSupplierGroup) {
        this.fwSupplierGroup = fwSupplierGroup;
    }

    public Long getFwEndMarket() {
        return fwEndMarket;
    }

    public void setFwEndMarket(final Long fwEndMarket) {
        this.fwEndMarket = fwEndMarket;
    }

    public Integer getMarketType() {
        return marketType;
    }

    public void setMarketType(final Integer marketType) {
        this.marketType = marketType;
    }

   

    public BigDecimal getFwBudPlan() {
        return fwBudPlan;
    }

    public void setFwBudPlan(final BigDecimal fwBudPlan) {
        this.fwBudPlan = fwBudPlan;
    }

    public Integer getFwBudPlanType() {
        return fwBudPlanType;
    }

    public void setFwBudPlanType(final Integer fwBudPlanType) {
        this.fwBudPlanType = fwBudPlanType;
    }

    public BigDecimal getFwActualCost() {
        return fwActualCost;
    }

    public void setFwActualCost(final BigDecimal fwActualCost) {
        this.fwActualCost = fwActualCost;
    }

    public Integer getFwActualCostType() {
        return fwActualCostType;
    }

    public void setFwActualCostType(final Integer fwActualCostType) {
        this.fwActualCostType = fwActualCostType;
    }

    public BigDecimal getFwQPR1() {
        return fwQPR1;
    }

    public void setFwQPR1(final BigDecimal fwQPR1) {
        this.fwQPR1 = fwQPR1;
    }

    public Integer getFwQPR1Type() {
        return fwQPR1Type;
    }

    public void setFwQPR1Type(final Integer fwQPR1Type) {
        this.fwQPR1Type = fwQPR1Type;
    }

    public BigDecimal getFwQPR2() {
        return fwQPR2;
    }

    public void setFwQPR2(final BigDecimal fwQPR2) {
        this.fwQPR2 = fwQPR2;
    }

    public Integer getFwQPR2Type() {
        return fwQPR2Type;
    }

    public void setFwQPR2Type(final Integer fwQPR2Type) {
        this.fwQPR2Type = fwQPR2Type;
    }

    public BigDecimal getFwQPR3() {
        return fwQPR3;
    }

    public void setFwQPR3(final BigDecimal fwQPR3) {
        this.fwQPR3 = fwQPR3;
    }

    public Integer getFwQPR3Type() {
        return fwQPR3Type;
    }

    public void setFwQPR3Type(final Integer fwQPR3Type) {
        this.fwQPR3Type = fwQPR3Type;
    }

    public BigDecimal getFwQPR4() {
        return fwQPR4;
    }

    public void setFwQPR4(final BigDecimal fwQPR4) {
        this.fwQPR4 = fwQPR4;
    }

    public Integer getFwQPR4Type() {
        return fwQPR4Type;
    }

    public void setFwQPR4Type(final Integer fwQPR4Type) {
        this.fwQPR4Type = fwQPR4Type;
    }

    public Boolean getFwCancelled() {
        return fwCancelled;
    }

    public void setFwCancelled(final Boolean fwCancelled) {
        this.fwCancelled = fwCancelled;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }

    public User getFwAgency() {
        return fwAgency;
    }

    public void setFwAgency(final User fwAgency) {
        this.fwAgency = fwAgency;
    }

	

	public List<Long> getDataCollectionMethod() {
		return dataCollectionMethod;
	}

	public void setDataCollectionMethod(List<Long> dataCollectionMethod) {
		this.dataCollectionMethod = dataCollectionMethod;
	}
}
