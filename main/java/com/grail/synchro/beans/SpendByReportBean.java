package com.grail.synchro.beans;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 22/12/16
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpendByReportBean {

    private String spendByReportKey;
    
	private Long projectId;
    private String projectName;
    private Integer budgetLocation;
    private String budgetLocationName;
    
    /*private BigDecimal coplaTotalCost = new BigDecimal(0);
    private BigDecimal qpr1TotalCost = new BigDecimal(0);
    private BigDecimal qpr2TotalCost = new BigDecimal(0);
    private BigDecimal qpr3TotalCost = new BigDecimal(0);
    private BigDecimal fullYearTotalCost = new BigDecimal(0);
    private BigDecimal latestTotalCost = new BigDecimal(0);*/
    
    private BigDecimal coplaTotalCost;
    private BigDecimal qpr1TotalCost;
    private BigDecimal qpr2TotalCost;
    private BigDecimal qpr3TotalCost;
    private BigDecimal fullYearTotalCost;
    private BigDecimal latestTotalCost;
    
    private String methodologies;
    private String methodologyNames;
    
    private String brandNonBrandName;
    private Integer brand;
    private Integer studyType;
    
    private String researchAgecny;
    private String researchAgecnyType;
    
    private String region;
    private String area;
    private String t20_40;
    
    private String methGroup;
    private String brandType;
    
    private Integer budgetYear;
    
    private String categoryTypes;
    private String categoryTypesNames;
    
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
	public Integer getBudgetLocation() {
		return budgetLocation;
	}
	public void setBudgetLocation(Integer budgetLocation) {
		this.budgetLocation = budgetLocation;
	}
	public BigDecimal getCoplaTotalCost() {
		return coplaTotalCost;
	}
	public void setCoplaTotalCost(BigDecimal coplaTotalCost) {
		this.coplaTotalCost = coplaTotalCost;
	}
	public BigDecimal getQpr1TotalCost() {
		return qpr1TotalCost;
	}
	public void setQpr1TotalCost(BigDecimal qpr1TotalCost) {
		this.qpr1TotalCost = qpr1TotalCost;
	}
	public BigDecimal getQpr2TotalCost() {
		return qpr2TotalCost;
	}
	public void setQpr2TotalCost(BigDecimal qpr2TotalCost) {
		this.qpr2TotalCost = qpr2TotalCost;
	}
	public BigDecimal getQpr3TotalCost() {
		return qpr3TotalCost;
	}
	public void setQpr3TotalCost(BigDecimal qpr3TotalCost) {
		this.qpr3TotalCost = qpr3TotalCost;
	}
	public BigDecimal getFullYearTotalCost() {
		return fullYearTotalCost;
	}
	public void setFullYearTotalCost(BigDecimal fullYearTotalCost) {
		this.fullYearTotalCost = fullYearTotalCost;
	}
	public BigDecimal getLatestTotalCost() {
		return latestTotalCost;
	}
	public void setLatestTotalCost(BigDecimal latestTotalCost) {
		this.latestTotalCost = latestTotalCost;
	}
	public String getMethodologies() {
		return methodologies;
	}
	public void setMethodologies(String methodologies) {
		this.methodologies = methodologies;
	}
	public String getBrandNonBrandName() {
		return brandNonBrandName;
	}
	public void setBrandNonBrandName(String brandNonBrandName) {
		this.brandNonBrandName = brandNonBrandName;
	}
	public String getResearchAgecny() {
		return researchAgecny;
	}
	public void setResearchAgecny(String researchAgecny) {
		this.researchAgecny = researchAgecny;
	}
	public String getResearchAgecnyType() {
		return researchAgecnyType;
	}
	public void setResearchAgecnyType(String researchAgecnyType) {
		this.researchAgecnyType = researchAgecnyType;
	}
	public String getBudgetLocationName() {
		return budgetLocationName;
	}
	public void setBudgetLocationName(String budgetLocationName) {
		this.budgetLocationName = budgetLocationName;
	}
	public String getMethodologyNames() {
		return methodologyNames;
	}
	public void setMethodologyNames(String methodologyNames) {
		this.methodologyNames = methodologyNames;
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
	public String getT20_40() {
		return t20_40;
	}
	public void setT20_40(String t20_40) {
		this.t20_40 = t20_40;
	}
	public Integer getBudgetYear() {
		return budgetYear;
	}
	public void setBudgetYear(Integer budgetYear) {
		this.budgetYear = budgetYear;
	}
	public String getCategoryTypes() {
		return categoryTypes;
	}
	public void setCategoryTypes(String categoryTypes) {
		this.categoryTypes = categoryTypes;
	}
	public String getCategoryTypesNames() {
		return categoryTypesNames;
	}
	public void setCategoryTypesNames(String categoryTypesNames) {
		this.categoryTypesNames = categoryTypesNames;
	}
	public Integer getBrand() {
		return brand;
	}
	public void setBrand(Integer brand) {
		this.brand = brand;
	}
	public String getSpendByReportKey() {
		return spendByReportKey;
	}
	public void setSpendByReportKey(String spendByReportKey) {
		this.spendByReportKey = spendByReportKey;
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
	public Integer getStudyType() {
		return studyType;
	}
	public void setStudyType(Integer studyType) {
		this.studyType = studyType;
	}


   
}
