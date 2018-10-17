package com.grail.synchro.beans;

import java.util.Date;


/**
 * @author: tejinder
 * @since: 1.0
 */
public class SynchroToIRIS extends BeanObject {

    private long projectID;
    private long endMarketId;
    private String projectName;
    private String projectDesc;
    private Long brand;
    
    private String bizQuestion;
    private String researchObjective;
    private String actionStandard;
    private String researchDesign;
    private String conclusions;
    private String keyFindings;
    
    private Long methodologyType;
    private Long methodologyGroup;
  	
    private String respondentType;
    private String sampleSize;
    
    private Date fieldWorkStartDate;
    private Date fieldWorkEndDate;
    private Date reportDate;
    
    private String researchAgency;
    private Long summaryWrittenBy;
    private Long batPrimaryContact;
    
    private String relatedStudy;
    private String tags;
    
    private Boolean allDocsEnglish;
    private Boolean disclaimer;
    private Integer irisSummaryRequired;
    
    private String irisOptionRationale;

	public long getProjectID() {
		return projectID;
	}

	public void setProjectID(long projectID) {
		this.projectID = projectID;
	}

	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDesc() {
		return projectDesc;
	}

	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
	}

	public Long getBrand() {
		return brand;
	}

	public void setBrand(Long brand) {
		this.brand = brand;
	}

	public String getBizQuestion() {
		return bizQuestion;
	}

	public void setBizQuestion(String bizQuestion) {
		this.bizQuestion = bizQuestion;
	}

	public String getResearchObjective() {
		return researchObjective;
	}

	public void setResearchObjective(String researchObjective) {
		this.researchObjective = researchObjective;
	}

	public String getActionStandard() {
		return actionStandard;
	}

	public void setActionStandard(String actionStandard) {
		this.actionStandard = actionStandard;
	}

	public String getResearchDesign() {
		return researchDesign;
	}

	public void setResearchDesign(String researchDesign) {
		this.researchDesign = researchDesign;
	}

	public String getConclusions() {
		return conclusions;
	}

	public void setConclusions(String conclusions) {
		this.conclusions = conclusions;
	}

	public String getKeyFindings() {
		return keyFindings;
	}

	public void setKeyFindings(String keyFindings) {
		this.keyFindings = keyFindings;
	}

	public Long getMethodologyType() {
		return methodologyType;
	}

	public void setMethodologyType(Long methodologyType) {
		this.methodologyType = methodologyType;
	}

	public Long getMethodologyGroup() {
		return methodologyGroup;
	}

	public void setMethodologyGroup(Long methodologyGroup) {
		this.methodologyGroup = methodologyGroup;
	}

	public String getRespondentType() {
		return respondentType;
	}

	public void setRespondentType(String respondentType) {
		this.respondentType = respondentType;
	}

	public String getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(String sampleSize) {
		this.sampleSize = sampleSize;
	}

	public Date getFieldWorkStartDate() {
		return fieldWorkStartDate;
	}

	public void setFieldWorkStartDate(Date fieldWorkStartDate) {
		this.fieldWorkStartDate = fieldWorkStartDate;
	}

	public Date getFieldWorkEndDate() {
		return fieldWorkEndDate;
	}

	public void setFieldWorkEndDate(Date fieldWorkEndDate) {
		this.fieldWorkEndDate = fieldWorkEndDate;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public String getResearchAgency() {
		return researchAgency;
	}

	public void setResearchAgency(String researchAgency) {
		this.researchAgency = researchAgency;
	}

	public Long getSummaryWrittenBy() {
		return summaryWrittenBy;
	}

	public void setSummaryWrittenBy(Long summaryWrittenBy) {
		this.summaryWrittenBy = summaryWrittenBy;
	}

	public Long getBatPrimaryContact() {
		return batPrimaryContact;
	}

	public void setBatPrimaryContact(Long batPrimaryContact) {
		this.batPrimaryContact = batPrimaryContact;
	}

	public String getRelatedStudy() {
		return relatedStudy;
	}

	public void setRelatedStudy(String relatedStudy) {
		this.relatedStudy = relatedStudy;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Boolean getAllDocsEnglish() {
		return allDocsEnglish;
	}

	public void setAllDocsEnglish(Boolean allDocsEnglish) {
		this.allDocsEnglish = allDocsEnglish;
	}

	public Boolean getDisclaimer() {
		return disclaimer;
	}

	public void setDisclaimer(Boolean disclaimer) {
		this.disclaimer = disclaimer;
	}

	public Integer getIrisSummaryRequired() {
		return irisSummaryRequired;
	}

	public void setIrisSummaryRequired(Integer irisSummaryRequired) {
		this.irisSummaryRequired = irisSummaryRequired;
	}

	public String getIrisOptionRationale() {
		return irisOptionRationale;
	}

	public void setIrisOptionRationale(String irisOptionRationale) {
		this.irisOptionRationale = irisOptionRationale;
	}

	public long getEndMarketId() {
		return endMarketId;
	}

	public void setEndMarketId(long endMarketId) {
		this.endMarketId = endMarketId;
	}
    
	
	
}
