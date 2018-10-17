package com.grail.synchro.beans;

public class EvaluationAgencyReportFilters {
	private Integer startMonth;
	private Integer startYear;
	private Integer endMonth;
	private Integer endYear;
	private Long supplierGroup;
    private Long supplier;

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
}
