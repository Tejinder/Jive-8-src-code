package com.grail.synchro.beans;

import java.util.Date;
import java.util.List;

public class EvaluationAgencyReportFiltersNew {
    
    private Date aeStartDateBegin;
    private Date aeStartDateComplete;
    private Date aeEndDateBegin;
    private Date aeEndDateComplete;
    
    private String evaluationDoneBy;
    private List<Long> aeResearchAgencies;

    private List<Long> aeCostComponents;
    
    private List<Long> aeResearchEndMarkets;
    
    private List<Long> aeMethDetails;
    private List<Long> aeMethodologyTypes;
    private List<Long> aeBudgetLocations;
    private List<Long> aeBudgetYears;
    
	public Date getAeStartDateBegin() {
		return aeStartDateBegin;
	}
	public void setAeStartDateBegin(Date aeStartDateBegin) {
		this.aeStartDateBegin = aeStartDateBegin;
	}
	public Date getAeStartDateComplete() {
		return aeStartDateComplete;
	}
	public void setAeStartDateComplete(Date aeStartDateComplete) {
		this.aeStartDateComplete = aeStartDateComplete;
	}
	public Date getAeEndDateBegin() {
		return aeEndDateBegin;
	}
	public void setAeEndDateBegin(Date aeEndDateBegin) {
		this.aeEndDateBegin = aeEndDateBegin;
	}
	public Date getAeEndDateComplete() {
		return aeEndDateComplete;
	}
	public void setAeEndDateComplete(Date aeEndDateComplete) {
		this.aeEndDateComplete = aeEndDateComplete;
	}
	public String getEvaluationDoneBy() {
		return evaluationDoneBy;
	}
	public void setEvaluationDoneBy(String evaluationDoneBy) {
		this.evaluationDoneBy = evaluationDoneBy;
	}
	public List<Long> getAeResearchAgencies() {
		return aeResearchAgencies;
	}
	public void setAeResearchAgencies(List<Long> aeResearchAgencies) {
		this.aeResearchAgencies = aeResearchAgencies;
	}
	public List<Long> getAeCostComponents() {
		return aeCostComponents;
	}
	public void setAeCostComponents(List<Long> aeCostComponents) {
		this.aeCostComponents = aeCostComponents;
	}
	public List<Long> getAeResearchEndMarkets() {
		return aeResearchEndMarkets;
	}
	public void setAeResearchEndMarkets(List<Long> aeResearchEndMarkets) {
		this.aeResearchEndMarkets = aeResearchEndMarkets;
	}
	public List<Long> getAeMethDetails() {
		return aeMethDetails;
	}
	public void setAeMethDetails(List<Long> aeMethDetails) {
		this.aeMethDetails = aeMethDetails;
	}
	public List<Long> getAeMethodologyTypes() {
		return aeMethodologyTypes;
	}
	public void setAeMethodologyTypes(List<Long> aeMethodologyTypes) {
		this.aeMethodologyTypes = aeMethodologyTypes;
	}
	public List<Long> getAeBudgetLocations() {
		return aeBudgetLocations;
	}
	public void setAeBudgetLocations(List<Long> aeBudgetLocations) {
		this.aeBudgetLocations = aeBudgetLocations;
	}
	public List<Long> getAeBudgetYears() {
		return aeBudgetYears;
	}
	public void setAeBudgetYears(List<Long> aeBudgetYears) {
		this.aeBudgetYears = aeBudgetYears;
	}
    
	

	
}
