package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tejinder
 */
public class MigrateProjectBean extends BeanObject {
    
	private Long projectID;
	
	private Integer projectType;
	private Integer processType;
	
	private String budgetLocation;
	private String status;
	
	private Long refSynchroCode;
    
    private BigDecimal totalCost = new BigDecimal(0);
    private String agencyDevRationale;
    
    private List<MigrateProjectCostBean> migrateProjectCostBean = new ArrayList<MigrateProjectCostBean>();
    
    private String oldProjectStatus;
    private String newProjectStatus;
    
    private List<Long> oldCategoryType;
    private List<Long> newCategoryType;
    
    private Long oldMethodologyGroup;
    private Long newMethodologyGroup;
    
    private Date oldPibDateOfRequestForLA;
    private Date oldPibDateOfLA;
    private String oldLAName;
    
    private Date newPibDateOfRequestForLA;
    private Date newPibDateOfLA;
    private String newLAName;
    
    private String oldReportTypeLA;
    private String newReportTypeLA;
    
    private String oldIRISSummaryLA;
    private String newIRISSummaryLA;
    
    private String migrationException;
    
    private String projectName;
    
    private String endMarketIds;
    private String endMarketFundingIds;
    
    private String endMarketFundingReq;
    private String projectManagerName;
    
    
    
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	
	public BigDecimal getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}
	public List<MigrateProjectCostBean> getMigrateProjectCostBean() {
		return migrateProjectCostBean;
	}
	public void setMigrateProjectCostBean(
			List<MigrateProjectCostBean> migrateProjectCostBean) {
		this.migrateProjectCostBean = migrateProjectCostBean;
	}
	public Integer getProjectType() {
		return projectType;
	}
	public void setProjectType(Integer projectType) {
		this.projectType = projectType;
	}
	public Integer getProcessType() {
		return processType;
	}
	public void setProcessType(Integer processType) {
		this.processType = processType;
	}
	public String getOldProjectStatus() {
		return oldProjectStatus;
	}
	public void setOldProjectStatus(String oldProjectStatus) {
		this.oldProjectStatus = oldProjectStatus;
	}
	public String getNewProjectStatus() {
		return newProjectStatus;
	}
	public void setNewProjectStatus(String newProjectStatus) {
		this.newProjectStatus = newProjectStatus;
	}
	public List<Long> getOldCategoryType() {
		return oldCategoryType;
	}
	public void setOldCategoryType(List<Long> oldCategoryType) {
		this.oldCategoryType = oldCategoryType;
	}
	public List<Long> getNewCategoryType() {
		return newCategoryType;
	}
	public void setNewCategoryType(List<Long> newCategoryType) {
		this.newCategoryType = newCategoryType;
	}
	public Long getOldMethodologyGroup() {
		return oldMethodologyGroup;
	}
	public void setOldMethodologyGroup(Long oldMethodologyGroup) {
		this.oldMethodologyGroup = oldMethodologyGroup;
	}
	public Long getNewMethodologyGroup() {
		return newMethodologyGroup;
	}
	public void setNewMethodologyGroup(Long newMethodologyGroup) {
		this.newMethodologyGroup = newMethodologyGroup;
	}
	public Date getOldPibDateOfRequestForLA() {
		return oldPibDateOfRequestForLA;
	}
	public void setOldPibDateOfRequestForLA(Date oldPibDateOfRequestForLA) {
		this.oldPibDateOfRequestForLA = oldPibDateOfRequestForLA;
	}
	public Date getOldPibDateOfLA() {
		return oldPibDateOfLA;
	}
	public void setOldPibDateOfLA(Date oldPibDateOfLA) {
		this.oldPibDateOfLA = oldPibDateOfLA;
	}
	
	public Date getNewPibDateOfRequestForLA() {
		return newPibDateOfRequestForLA;
	}
	public void setNewPibDateOfRequestForLA(Date newPibDateOfRequestForLA) {
		this.newPibDateOfRequestForLA = newPibDateOfRequestForLA;
	}
	public Date getNewPibDateOfLA() {
		return newPibDateOfLA;
	}
	public void setNewPibDateOfLA(Date newPibDateOfLA) {
		this.newPibDateOfLA = newPibDateOfLA;
	}
	public String getOldLAName() {
		return oldLAName;
	}
	public void setOldLAName(String oldLAName) {
		this.oldLAName = oldLAName;
	}
	public String getNewLAName() {
		return newLAName;
	}
	public void setNewLAName(String newLAName) {
		this.newLAName = newLAName;
	}
	public String getOldReportTypeLA() {
		return oldReportTypeLA;
	}
	public void setOldReportTypeLA(String oldReportTypeLA) {
		this.oldReportTypeLA = oldReportTypeLA;
	}
	public String getNewReportTypeLA() {
		return newReportTypeLA;
	}
	public void setNewReportTypeLA(String newReportTypeLA) {
		this.newReportTypeLA = newReportTypeLA;
	}
	public String getOldIRISSummaryLA() {
		return oldIRISSummaryLA;
	}
	public void setOldIRISSummaryLA(String oldIRISSummaryLA) {
		this.oldIRISSummaryLA = oldIRISSummaryLA;
	}
	public String getNewIRISSummaryLA() {
		return newIRISSummaryLA;
	}
	public void setNewIRISSummaryLA(String newIRISSummaryLA) {
		this.newIRISSummaryLA = newIRISSummaryLA;
	}
	public String getBudgetLocation() {
		return budgetLocation;
	}
	public void setBudgetLocation(String budgetLocation) {
		this.budgetLocation = budgetLocation;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getRefSynchroCode() {
		return refSynchroCode;
	}
	public void setRefSynchroCode(Long refSynchroCode) {
		this.refSynchroCode = refSynchroCode;
	}
	public String getMigrationException() {
		return migrationException;
	}
	public void setMigrationException(String migrationException) {
		this.migrationException = migrationException;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getEndMarketIds() {
		return endMarketIds;
	}
	public void setEndMarketIds(String endMarketIds) {
		this.endMarketIds = endMarketIds;
	}
	public String getEndMarketFundingIds() {
		return endMarketFundingIds;
	}
	public void setEndMarketFundingIds(String endMarketFundingIds) {
		this.endMarketFundingIds = endMarketFundingIds;
	}
	public String getEndMarketFundingReq() {
		return endMarketFundingReq;
	}
	public void setEndMarketFundingReq(String endMarketFundingReq) {
		this.endMarketFundingReq = endMarketFundingReq;
	}
	public String getProjectManagerName() {
		return projectManagerName;
	}
	public void setProjectManagerName(String projectManagerName) {
		this.projectManagerName = projectManagerName;
	}
	public String getAgencyDevRationale() {
		return agencyDevRationale;
	}
	public void setAgencyDevRationale(String agencyDevRationale) {
		this.agencyDevRationale = agencyDevRationale;
	}
	
	
	
	
	
}
