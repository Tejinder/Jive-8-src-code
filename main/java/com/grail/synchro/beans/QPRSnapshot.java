package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Tejinder
 * @version 1.0, Date: 12/8/16
 */
public class QPRSnapshot extends BeanObject {
    private Long snapShotID;
    private Integer spendFor;
    private Integer budgetYear;
    private Boolean isFreeze;
    private Date freezeDate;
    private List<Long> openProjectIds;
    private String projectIds;
    
    private List<Long> openBudgetLocations;
    private String budgetLocations;
    private String openBudgetLocationIds;
    
    private String openIndividualProjectIds;
    private String openBLProjectIds;
    
	public Long getSnapShotID() {
		return snapShotID;
	}
	public void setSnapShotID(Long snapShotID) {
		this.snapShotID = snapShotID;
	}
	public Integer getSpendFor() {
		return spendFor;
	}
	public void setSpendFor(Integer spendFor) {
		this.spendFor = spendFor;
	}
	public Integer getBudgetYear() {
		return budgetYear;
	}
	public void setBudgetYear(Integer budgetYear) {
		this.budgetYear = budgetYear;
	}
	public Boolean getIsFreeze() {
		return isFreeze;
	}
	public void setIsFreeze(Boolean isFreeze) {
		this.isFreeze = isFreeze;
	}
	public Date getFreezeDate() {
		return freezeDate;
	}
	public void setFreezeDate(Date freezeDate) {
		this.freezeDate = freezeDate;
	}
	public List<Long> getOpenProjectIds() {
		return openProjectIds;
	}
	public void setOpenProjectIds(List<Long> openProjectIds) {
		this.openProjectIds = openProjectIds;
	}
	public String getProjectIds() {
		return projectIds;
	}
	public void setProjectIds(String projectIds) {
		this.projectIds = projectIds;
	}
	public List<Long> getOpenBudgetLocations() {
		return openBudgetLocations;
	}
	public void setOpenBudgetLocations(List<Long> openBudgetLocations) {
		this.openBudgetLocations = openBudgetLocations;
	}
	public String getBudgetLocations() {
		return budgetLocations;
	}
	public void setBudgetLocations(String budgetLocations) {
		this.budgetLocations = budgetLocations;
	}
	public String getOpenBudgetLocationIds() {
		return openBudgetLocationIds;
	}
	public void setOpenBudgetLocationIds(String openBudgetLocationIds) {
		this.openBudgetLocationIds = openBudgetLocationIds;
	}
	public String getOpenIndividualProjectIds() {
		return openIndividualProjectIds;
	}
	public void setOpenIndividualProjectIds(String openIndividualProjectIds) {
		this.openIndividualProjectIds = openIndividualProjectIds;
	}
	public String getOpenBLProjectIds() {
		return openBLProjectIds;
	}
	public void setOpenBLProjectIds(String openBLProjectIds) {
		this.openBLProjectIds = openBLProjectIds;
	}
    
}
