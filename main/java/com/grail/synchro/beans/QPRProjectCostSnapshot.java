package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Tejinder
 * @version 1.0, Date: 12/8/16
 */
public class QPRProjectCostSnapshot extends BeanObject {
    private Long snapShotID;
    private Long projectID;
    private Long agencyId;
    private Integer costComponent;
    private Integer costCurrency;
    private BigDecimal estimatedCost = new BigDecimal(0);
    
    private String agencyType;
    
	public Long getSnapShotID() {
		return snapShotID;
	}
	public void setSnapShotID(Long snapShotID) {
		this.snapShotID = snapShotID;
	}
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}
	public Integer getCostComponent() {
		return costComponent;
	}
	public void setCostComponent(Integer costComponent) {
		this.costComponent = costComponent;
	}
	public Integer getCostCurrency() {
		return costCurrency;
	}
	public void setCostCurrency(Integer costCurrency) {
		this.costCurrency = costCurrency;
	}
	public BigDecimal getEstimatedCost() {
		return estimatedCost;
	}
	public void setEstimatedCost(BigDecimal estimatedCost) {
		this.estimatedCost = estimatedCost;
	}
	public String getAgencyType() {
		return agencyType;
	}
	public void setAgencyType(String agencyType) {
		this.agencyType = agencyType;
	}
    
   
	
    
}
