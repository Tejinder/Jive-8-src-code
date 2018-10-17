package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User: Tejinder
 * 
 */
public class ProjectCostDetailsBean {


    private Long projectId;
    private Long agencyId;
    private Integer costComponent;
    private Integer costCurrency;
    private BigDecimal estimatedCost = new BigDecimal(0);
    
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
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
	
}
