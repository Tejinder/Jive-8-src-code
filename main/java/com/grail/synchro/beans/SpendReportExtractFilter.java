package com.grail.synchro.beans;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/5/14
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpendReportExtractFilter {
    private Integer reportType;
    private List<Integer> years;
    private List<Long> regions;
    private List<Long> countries;
    private List<Long> marketTypes;
    private Integer currencyId;
    
    private List<Long> methDetails;
    private List<Long> brands;
    private List<Long> budgetLocations;

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public List<Integer> getYears() {
        return years;
    }

    public void setYears(List<Integer> years) {
        this.years = years;
    }

    public List<Long> getCountries() {
        return countries;
    }

    public void setCountries(List<Long> countries) {
        this.countries = countries;
    }

    public List<Long> getRegions() {
        return regions;
    }

    public void setRegions(List<Long> regions) {
        this.regions = regions;
    }

    public List<Long> getMarketTypes() {
        return marketTypes;
    }

    public void setMarketTypes(List<Long> marketTypes) {
        this.marketTypes = marketTypes;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

	public List<Long> getMethDetails() {
		return methDetails;
	}

	public void setMethDetails(List<Long> methDetails) {
		this.methDetails = methDetails;
	}

	public List<Long> getBrands() {
		return brands;
	}

	public void setBrands(List<Long> brands) {
		this.brands = brands;
	}

	public List<Long> getBudgetLocations() {
		return budgetLocations;
	}

	public void setBudgetLocations(List<Long> budgetLocations) {
		this.budgetLocations = budgetLocations;
	}
}
