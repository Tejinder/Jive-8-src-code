package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectSpecsEndMarketDetails {

    private Long projectID;
    private Long endMarketID;
  
    private BigDecimal totalCost;
    private Integer totalCostType;
    private BigDecimal intMgmtCost;
    private Integer intMgmtCostType;
    private BigDecimal localMgmtCost;
    private Integer localMgmtCostType;
    private BigDecimal operationalHubCost;
    private Integer operationalHubCostType;
    private BigDecimal fieldworkCost;
    private Integer fieldworkCostType;
    private String proposedFWAgencyNames;
    private Date fwStartDate;
    private Date fwEndDate;
    
    private Integer totalNoInterviews;
    private Integer totalNoOfVisits;
    private Integer avIntDuration;
    private Integer totalNoOfGroups;
    private Integer interviewDuration;
    
    private Integer noOfRespPerGroup;
    private String cities;
    private Boolean geoSpreadNational;
    private Boolean geoSpreadUrban;
    private List<Long> dataCollectionMethod;
    
    private BigDecimal finalCost;
    private Integer finalCostType;
    private Date fwStartDateLatest;
    private Date fwEndDateLatest;
    private String latestFWComments;
    private String finalCostComments;
    
    private BigDecimal otherCost;
    private Integer otherCostType;
    
    private BigDecimal originalFinalCost;
    private Integer originalFinalCostType;
    
    private Date projectEndDate;
    private Date projectEndDateLatest;
    
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getEndMarketID() {
		return endMarketID;
	}
	public void setEndMarketID(Long endMarketID) {
		this.endMarketID = endMarketID;
	}
	
	public BigDecimal getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}
	public Integer getTotalCostType() {
		return totalCostType;
	}
	public void setTotalCostType(Integer totalCostType) {
		this.totalCostType = totalCostType;
	}
	public BigDecimal getIntMgmtCost() {
		return intMgmtCost;
	}
	public void setIntMgmtCost(BigDecimal intMgmtCost) {
		this.intMgmtCost = intMgmtCost;
	}
	public Integer getIntMgmtCostType() {
		return intMgmtCostType;
	}
	public void setIntMgmtCostType(Integer intMgmtCostType) {
		this.intMgmtCostType = intMgmtCostType;
	}
	public BigDecimal getLocalMgmtCost() {
		return localMgmtCost;
	}
	public void setLocalMgmtCost(BigDecimal localMgmtCost) {
		this.localMgmtCost = localMgmtCost;
	}
	public Integer getLocalMgmtCostType() {
		return localMgmtCostType;
	}
	public void setLocalMgmtCostType(Integer localMgmtCostType) {
		this.localMgmtCostType = localMgmtCostType;
	}
	public BigDecimal getFieldworkCost() {
		return fieldworkCost;
	}
	public void setFieldworkCost(BigDecimal fieldworkCost) {
		this.fieldworkCost = fieldworkCost;
	}
	public Integer getFieldworkCostType() {
		return fieldworkCostType;
	}
	public void setFieldworkCostType(Integer fieldworkCostType) {
		this.fieldworkCostType = fieldworkCostType;
	}
	public String getProposedFWAgencyNames() {
		return proposedFWAgencyNames;
	}
	public void setProposedFWAgencyNames(String proposedFWAgencyNames) {
		this.proposedFWAgencyNames = proposedFWAgencyNames;
	}

    public Date getFwStartDate() {
        return fwStartDate;
    }

    public void setFwStartDate(Date fwStartDate) {
        this.fwStartDate = fwStartDate;
    }

    public Date getFwEndDate() {
        return fwEndDate;
    }

    public void setFwEndDate(Date fwEndDate) {
        this.fwEndDate = fwEndDate;
    }

    public Integer getTotalNoInterviews() {
		return totalNoInterviews;
	}
	public void setTotalNoInterviews(Integer totalNoInterviews) {
		this.totalNoInterviews = totalNoInterviews;
	}
	public Integer getTotalNoOfVisits() {
		return totalNoOfVisits;
	}
	public void setTotalNoOfVisits(Integer totalNoOfVisits) {
		this.totalNoOfVisits = totalNoOfVisits;
	}
	public Integer getAvIntDuration() {
		return avIntDuration;
	}
	public void setAvIntDuration(Integer avIntDuration) {
		this.avIntDuration = avIntDuration;
	}
	public Integer getTotalNoOfGroups() {
		return totalNoOfGroups;
	}
	public void setTotalNoOfGroups(Integer totalNoOfGroups) {
		this.totalNoOfGroups = totalNoOfGroups;
	}
	public Integer getInterviewDuration() {
		return interviewDuration;
	}
	public void setInterviewDuration(Integer interviewDuration) {
		this.interviewDuration = interviewDuration;
	}
	public Integer getNoOfRespPerGroup() {
		return noOfRespPerGroup;
	}
	public void setNoOfRespPerGroup(Integer noOfRespPerGroup) {
		this.noOfRespPerGroup = noOfRespPerGroup;
	}
	public String getCities() {
		return cities;
	}
	public void setCities(String cities) {
		this.cities = cities;
	}
	public Boolean getGeoSpreadNational() {
		return geoSpreadNational;
	}
	public void setGeoSpreadNational(Boolean geoSpreadNational) {
		this.geoSpreadNational = geoSpreadNational;
	}
	public Boolean getGeoSpreadUrban() {
		return geoSpreadUrban;
	}
	public void setGeoSpreadUrban(Boolean geoSpreadUrban) {
		this.geoSpreadUrban = geoSpreadUrban;
	}
	public List<Long> getDataCollectionMethod() {
		return dataCollectionMethod;
	}
	public void setDataCollectionMethod(List<Long> dataCollectionMethod) {
		this.dataCollectionMethod = dataCollectionMethod;
	}
	public BigDecimal getFinalCost() {
		return finalCost;
	}
	public void setFinalCost(BigDecimal finalCost) {
		this.finalCost = finalCost;
	}
	public Integer getFinalCostType() {
		return finalCostType;
	}
	public void setFinalCostType(Integer finalCostType) {
		this.finalCostType = finalCostType;
	}
	public Date getFwStartDateLatest() {
		return fwStartDateLatest;
	}
	public void setFwStartDateLatest(Date fwStartDateLatest) {
		this.fwStartDateLatest = fwStartDateLatest;
	}
	public Date getFwEndDateLatest() {
		return fwEndDateLatest;
	}
	public void setFwEndDateLatest(Date fwEndDateLatest) {
		this.fwEndDateLatest = fwEndDateLatest;
	}
	public String getLatestFWComments() {
		return latestFWComments;
	}
	public void setLatestFWComments(String latestFWComments) {
		this.latestFWComments = latestFWComments;
	}
	public String getFinalCostComments() {
		return finalCostComments;
	}
	public void setFinalCostComments(String finalCostComments) {
		this.finalCostComments = finalCostComments;
	}
	public BigDecimal getOtherCost() {
		return otherCost;
	}
	public void setOtherCost(BigDecimal otherCost) {
		this.otherCost = otherCost;
	}
	public Integer getOtherCostType() {
		return otherCostType;
	}
	public void setOtherCostType(Integer otherCostType) {
		this.otherCostType = otherCostType;
	}
	public BigDecimal getOriginalFinalCost() {
		return originalFinalCost;
	}
	public void setOriginalFinalCost(BigDecimal originalFinalCost) {
		this.originalFinalCost = originalFinalCost;
	}
	public Integer getOriginalFinalCostType() {
		return originalFinalCostType;
	}
	public void setOriginalFinalCostType(Integer originalFinalCostType) {
		this.originalFinalCostType = originalFinalCostType;
	}
	public Date getProjectEndDate() {
		return projectEndDate;
	}
	public void setProjectEndDate(Date projectEndDate) {
		this.projectEndDate = projectEndDate;
	}
	public Date getProjectEndDateLatest() {
		return projectEndDateLatest;
	}
	public void setProjectEndDateLatest(Date projectEndDateLatest) {
		this.projectEndDateLatest = projectEndDateLatest;
	}
	public BigDecimal getOperationalHubCost() {
		return operationalHubCost;
	}
	public void setOperationalHubCost(BigDecimal operationalHubCost) {
		this.operationalHubCost = operationalHubCost;
	}
	public Integer getOperationalHubCostType() {
		return operationalHubCostType;
	}
	public void setOperationalHubCostType(Integer operationalHubCostType) {
		this.operationalHubCostType = operationalHubCostType;
	}
  
}
