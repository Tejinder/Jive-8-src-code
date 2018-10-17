package com.grail.synchro.beans;

import java.util.List;

public class ProjectResultFilter{

	private String name;
	private List<Long> ownerfield;
	private Integer startMonth;
    private Integer startYear;
    private Integer endMonth;
    private Integer endYear;
    private List<Long> methodologyFields;
    private List<Long> brandFields;
    private List<Long> regionFields;
    private List<Long> endMarkets;
    private List<Long> projectSupplierFields;
    private String sortField;
    private Integer ascendingOrder;
    private List<Long> spiContacts;
    private List<Long> agencies;


    public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}
	public Integer getAscendingOrder() {
		return ascendingOrder;
	}
	public void setAscendingOrder(Integer ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}
	public List<Long> getProjectSupplierFields() {
		return projectSupplierFields;
	}
	public void setProjectSupplierFields(List<Long> projectSupplierFields) {
		this.projectSupplierFields = projectSupplierFields;
	}
	public List<Long> getEndMarkets() {
		return endMarkets;
	}
	public void setEndMarkets(List<Long> endMarkets) {
		this.endMarkets = endMarkets;
	}
	private List<Long> projectStatusFields;
    private List<Long> projectActivityFields;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Long> getOwnerfield() {
		return ownerfield;
	}
	public void setOwnerfield(List<Long> ownerfield) {
		this.ownerfield = ownerfield;
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
	public List<Long> getRegionFields() {
		return regionFields;
	}
	public void setRegionFields(List<Long> regionFields) {
		this.regionFields = regionFields;
	}

	public List<Long> getProjectStatusFields() {
		return projectStatusFields;
	}
	public void setProjectStatusFields(List<Long> projectStatusFields) {
		this.projectStatusFields = projectStatusFields;
	}
	public List<Long> getProjectActivityFields() {
		return projectActivityFields;
	}
	public void setProjectActivityFields(List<Long> projectActivityFields) {
		this.projectActivityFields = projectActivityFields;
	}

    public List<Long> getSpiContacts() {
        return spiContacts;
    }

    public void setSpiContacts(List<Long> spiContacts) {
        this.spiContacts = spiContacts;
    }

    public List<Long> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<Long> agencies) {
        this.agencies = agencies;
    }
}
