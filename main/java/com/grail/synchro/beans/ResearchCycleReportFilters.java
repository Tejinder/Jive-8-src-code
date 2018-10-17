package com.grail.synchro.beans;

import java.util.List;

public class ResearchCycleReportFilters {
	private String workflowType;
	private Integer startMonth;
	private Integer startYear;
	private Integer endMonth;
	private Integer endYear;
    private Long supplier;
    private Long supplierGroup;
    private List<Long> regionFields;
    private List<Long> endMarkets;
    private List<Long> methodologyFields;
    private List<Long> brandFields;
    
	public String getWorkflowType() {
		return workflowType;
	}
	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
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
	public Long getSupplier() {
		return supplier;
	}
	public void setSupplier(Long supplier) {
		this.supplier = supplier;
	}
	public Long getSupplierGroup() {
		return supplierGroup;
	}
	public void setSupplierGroup(Long supplierGroup) {
		this.supplierGroup = supplierGroup;
	}
	public List<Long> getRegionFields() {
		return regionFields;
	}
	public void setRegionFields(List<Long> regionFields) {
		this.regionFields = regionFields;
	}
	public List<Long> getEndMarkets() {
		return endMarkets;
	}
	public void setEndMarkets(List<Long> endMarkets) {
		this.endMarkets = endMarkets;
	}
	public List<Long> getMethodologyFields() {
		return methodologyFields;
	}
	public void setMethodologyFields(List<Long> methodologyFields) {
		this.methodologyFields = methodologyFields;
	}
	public List<Long> getBrandFields() {
		return brandFields;
	}
	public void setBrandFields(List<Long> brandFields) {
		this.brandFields = brandFields;
	}
    
}
