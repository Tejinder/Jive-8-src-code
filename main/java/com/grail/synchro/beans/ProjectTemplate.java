package com.grail.synchro.beans;

import java.util.List;

/**
 * @author Kanwar Grewal
 * @version 4.0, Date: 11/27/13
 */
public class ProjectTemplate extends BeanObject {
    private Long templateID;
    private String templateName;
    private String name;
    private String description;
    private List<Long> categoryType; 
    private Long brand;
    private Long methodology;
    private Long methodologyGroup;
    private Long proposedMethodology;
    private List<Long> endMarkets;
    private String startDate;
    private String endDate;
    private Long initialCost;
    private Long totalCostType;
    private Long ownerID;
    private Long spi;
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getOwnerID() {
		return ownerID;
	}
	public void setOwnerID(Long ownerID) {
		this.ownerID = ownerID;
	}
	public List<Long> getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(List<Long> categoryType) {
		this.categoryType = categoryType;
	}
	public Long getBrand() {
		return brand;
	}
	public void setBrand(Long brand) {
		this.brand = brand;
	}
	public Long getMethodology() {
		return methodology;
	}
	public void setMethodology(Long methodology) {
		this.methodology = methodology;
	}
	public Long getMethodologyGroup() {
		return methodologyGroup;
	}
	public void setMethodologyGroup(Long methodologyGroup) {
		this.methodologyGroup = methodologyGroup;
	}
	public Long getProposedMethodology() {
		return proposedMethodology;
	}
	public void setProposedMethodology(Long proposedMethodology) {
		this.proposedMethodology = proposedMethodology;
	}
	public List<Long> getEndMarkets() {
		return endMarkets;
	}
	public void setEndMarkets(List<Long> endMarkets) {
		this.endMarkets = endMarkets;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public Long getInitialCost() {
		return initialCost;
	}
	public void setInitialCost(Long initialCost) {
		this.initialCost = initialCost;
	}
	public Long getTotalCostType() {
		return totalCostType;
	}
	public void setTotalCostType(Long totalCostType) {
		this.totalCostType = totalCostType;
	}
	public Long getSpi() {
		return spi;
	}
	public void setSpi(Long spi) {
		this.spi = spi;
	}
	public Long getTemplateID() {
		return templateID;
	}
	public void setTemplateID(Long templateID) {
		this.templateID = templateID;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

}
