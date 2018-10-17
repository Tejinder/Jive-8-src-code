package com.grail.synchro.beans;


public class DataExtractReport {
	 private Long projectID;
	 private String name;
	 private Long ownerID;
	 private Integer status;
	 private Integer startMonth;
	 private Integer startYear;
	 private Integer endMonth;
	 private Integer endYear;
	 private Long brand;
	 private Long methodology;
	 private Long methodologyGroup;
	 private Long researchType;
	 private Long projectType;
	 private Boolean insights;
	 private Boolean fwEnabled;
	 private Long npi;
	 private Boolean partialMethodologyWaiverRequired;
	
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getOwnerID() {
		return ownerID;
	}
	public void setOwnerID(Long ownerID) {
		this.ownerID = ownerID;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getStartMonth() {
		return startMonth;
	}
	public void setStartMonth(Integer startMonth) {
		this.startMonth = startMonth;
	}
	public Integer getStartYear() {
		return startYear;
	}
	public void setStartYear(Integer startYear) {
		this.startYear = startYear;
	}
	public Integer getEndMonth() {
		return endMonth;
	}
	public void setEndMonth(Integer endMonth) {
		this.endMonth = endMonth;
	}
	public Integer getEndYear() {
		return endYear;
	}
	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
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
	public Long getResearchType() {
		return researchType;
	}
	public void setResearchType(Long researchType) {
		this.researchType = researchType;
	}
	public Long getProjectType() {
		return projectType;
	}
	public void setProjectType(Long projectType) {
		this.projectType = projectType;
	}
	public Boolean getInsights() {
		return insights;
	}
	public void setInsights(Boolean insights) {
		this.insights = insights;
	}
	public Boolean getFwEnabled() {
		return fwEnabled;
	}
	public void setFwEnabled(Boolean fwEnabled) {
		this.fwEnabled = fwEnabled;
	}
	public Long getNpi() {
		return npi;
	}
	public void setNpi(Long npi) {
		this.npi = npi;
	}
	public Boolean getPartialMethodologyWaiverRequired() {
		return partialMethodologyWaiverRequired;
	}
	public void setPartialMethodologyWaiverRequired(
			Boolean partialMethodologyWaiverRequired) {
		this.partialMethodologyWaiverRequired = partialMethodologyWaiverRequired;
	}

}
