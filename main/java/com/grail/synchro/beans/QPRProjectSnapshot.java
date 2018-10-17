package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Tejinder
 * @version 1.0, Date: 12/8/16
 */
public class QPRProjectSnapshot extends BeanObject {
    private Long snapShotID;
    private Long projectID;
    private String projectName;
    private Integer budgetLocation;
    private List<Long> methodologyDetails;
   
    
    private BigDecimal totalCost;
    private Long totalCostCurrency;
    private Boolean isFreeze;
    
    private Long brand;
    private Integer brandSpecificStudy;
    private Integer brandSpecificStudyType;
    
    // Meta Data Fields
    private String region;
    private String area;
    private String t20_t40;
    private String methGroup;
    private String brandType;
    
    private List<Long> categoryType;
    
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
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Integer getBudgetLocation() {
		return budgetLocation;
	}
	public void setBudgetLocation(Integer budgetLocation) {
		this.budgetLocation = budgetLocation;
	}
	public List<Long> getMethodologyDetails() {
		return methodologyDetails;
	}
	public void setMethodologyDetails(List<Long> methodologyDetails) {
		this.methodologyDetails = methodologyDetails;
	}
	
	public BigDecimal getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}
	public Long getTotalCostCurrency() {
		return totalCostCurrency;
	}
	public void setTotalCostCurrency(Long totalCostCurrency) {
		this.totalCostCurrency = totalCostCurrency;
	}
	public Boolean getIsFreeze() {
		return isFreeze;
	}
	public void setIsFreeze(Boolean isFreeze) {
		this.isFreeze = isFreeze;
	}
	public Long getBrand() {
		return brand;
	}
	public void setBrand(Long brand) {
		this.brand = brand;
	}
	public Integer getBrandSpecificStudy() {
		return brandSpecificStudy;
	}
	public void setBrandSpecificStudy(Integer brandSpecificStudy) {
		this.brandSpecificStudy = brandSpecificStudy;
	}
	public Integer getBrandSpecificStudyType() {
		return brandSpecificStudyType;
	}
	public void setBrandSpecificStudyType(Integer brandSpecificStudyType) {
		this.brandSpecificStudyType = brandSpecificStudyType;
	}
	public List<Long> getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(List<Long> categoryType) {
		this.categoryType = categoryType;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getT20_t40() {
		return t20_t40;
	}
	public void setT20_t40(String t20_t40) {
		this.t20_t40 = t20_t40;
	}
	public String getMethGroup() {
		return methGroup;
	}
	public void setMethGroup(String methGroup) {
		this.methGroup = methGroup;
	}
	public String getBrandType() {
		return brandType;
	}
	public void setBrandType(String brandType) {
		this.brandType = brandType;
	}
	
    
	
    
}
