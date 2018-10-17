package com.grail.synchro.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class PIBMethodologyWaiver extends BeanObject{

    private Long projectID;
    private Long endMarketID;

    private String methodologyDeviationRationale;
    private Long methodologyApprover;
    private String methodologyApproverComment;
    private Integer isApproved;
    private String otherReportingRequirements;
    private Integer status;
    
    //These fields are added for Methodology and Agency Waiver Dashboard
    
    private String projectName;
    private String endMarketName;
    private Integer budgetYear;
    private String approverName;
    private String stageURL;
    private List<Long> methodologyDetails;
    private String methodology;
    
    private String projectDescription;
    private String projectStage;
    private String budgetLocation;
    private String projectManager;
    private String waiverInitiator; 
    private String agencyNames;
    
  
    
    private Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();
    
    
    
    private Long waiverID;
    
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
	public String getMethodologyDeviationRationale() {
		return methodologyDeviationRationale;
	}
	public void setMethodologyDeviationRationale(
			String methodologyDeviationRationale) {
		this.methodologyDeviationRationale = methodologyDeviationRationale;
	}
	public Long getMethodologyApprover() {
		return methodologyApprover;
	}
	public void setMethodologyApprover(Long methodologyApprover) {
		this.methodologyApprover = methodologyApprover;
	}
	public String getMethodologyApproverComment() {
		return methodologyApproverComment;
	}
	public void setMethodologyApproverComment(String methodologyApproverComment) {
		this.methodologyApproverComment = methodologyApproverComment;
	}
	
	public String getOtherReportingRequirements() {
		return otherReportingRequirements;
	}
	public void setOtherReportingRequirements(String otherReportingRequirements) {
		this.otherReportingRequirements = otherReportingRequirements;
	}
	public Integer getIsApproved() {
		return isApproved;
	}
	public void setIsApproved(Integer isApproved) {
		this.isApproved = isApproved;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getWaiverID() {
		return waiverID;
	}
	public void setWaiverID(Long waiverID) {
		this.waiverID = waiverID;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getEndMarketName() {
		return endMarketName;
	}
	public void setEndMarketName(String endMarketName) {
		this.endMarketName = endMarketName;
	}
	public Integer getBudgetYear() {
		return budgetYear;
	}
	public void setBudgetYear(Integer budgetYear) {
		this.budgetYear = budgetYear;
	}
	public String getApproverName() {
		return approverName;
	}
	public void setApproverName(String approverName) {
		this.approverName = approverName;
	}
	public String getStageURL() {
		return stageURL;
	}
	public void setStageURL(String stageURL) {
		this.stageURL = stageURL;
	}
	public List<Long> getMethodologyDetails() {
		return methodologyDetails;
	}
	public void setMethodologyDetails(List<Long> methodologyDetails) {
		this.methodologyDetails = methodologyDetails;
	}
	public String getMethodology() {
		return methodology;
	}
	public void setMethodology(String methodology) {
		this.methodology = methodology;
	}
	public Map<Integer, List<AttachmentBean>> getAttachmentMap() {
		return attachmentMap;
	}
	public void setAttachmentMap(Map<Integer, List<AttachmentBean>> attachmentMap) {
		this.attachmentMap = attachmentMap;
	}
	public String getProjectDescription() {
		return projectDescription;
	}
	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}
	public String getProjectStage() {
		return projectStage;
	}
	public void setProjectStage(String projectStage) {
		this.projectStage = projectStage;
	}
	public String getBudgetLocation() {
		return budgetLocation;
	}
	public void setBudgetLocation(String budgetLocation) {
		this.budgetLocation = budgetLocation;
	}
	public String getProjectManager() {
		return projectManager;
	}
	public void setProjectManager(String projectManager) {
		this.projectManager = projectManager;
	}
	public String getWaiverInitiator() {
		return waiverInitiator;
	}
	public void setWaiverInitiator(String waiverInitiator) {
		this.waiverInitiator = waiverInitiator;
	}
	public String getAgencyNames() {
		return agencyNames;
	}
	public void setAgencyNames(String agencyNames) {
		this.agencyNames = agencyNames;
	}

   

  
}
