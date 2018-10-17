package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Tejinder
 */
public class MigrateProjectCostBean extends BeanObject {
    
	private Long projectID;
	private String researchAgency;
    private String costComponent;
    private String costCurrency;
    private BigDecimal estimatedCost = new BigDecimal(0);
   
    
    
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public String getResearchAgency() {
		return researchAgency;
	}
	public void setResearchAgency(String researchAgency) {
		this.researchAgency = researchAgency;
	}
	public String getCostComponent() {
		return costComponent;
	}
	public void setCostComponent(String costComponent) {
		this.costComponent = costComponent;
	}
	public String getCostCurrency() {
		return costCurrency;
	}
	public void setCostCurrency(String costCurrency) {
		this.costCurrency = costCurrency;
	}
	public BigDecimal getEstimatedCost() {
		return estimatedCost;
	}
	public void setEstimatedCost(BigDecimal estimatedCost) {
		this.estimatedCost = estimatedCost;
	}

	
	
}
