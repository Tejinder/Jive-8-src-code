package com.grail.synchro.beans;

import java.util.Date;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ReportSummaryInitiation extends BeanObject {

    private long projectID;
    private long endMarketID;
  
    //Generic Report Summary Stage fields
 
    private String comments;
    private Integer status;
    
    private Boolean isSPIApproved;
  
        
    private Date spiApprovalDate;
    
        
    private long spiApprover;
    
    
    private Boolean sendForApproval;
    private Boolean needRevision;
    
    private Boolean legalApproval;
    private String legalApprover;
    
    private Boolean uploadedSummary;
    private Boolean irisOracleSummary;
    
    private Boolean fullReport;
    private Boolean summaryReport;
    private Boolean summaryForIRIS;
    
    private Boolean uploadToIRIS;
    private Boolean uploadToCPSIDdatabase;
    
    private Boolean needRevisionClicked;
    
    private Date repSummarySaveDate;
    private Date needRevisionClickDate;
    private Date repSummaryLegalApprovalDate;
    private Date irisUploadDate;
    private Date cpsiUploadDate;
    private Integer legalSignOffRequired;
        

	public long getProjectID() {
		return projectID;
	}

	public void setProjectID(long projectID) {
		this.projectID = projectID;
	}

	public long getEndMarketID() {
		return endMarketID;
	}

	public void setEndMarketID(long endMarketID) {
		this.endMarketID = endMarketID;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Boolean getIsSPIApproved() {
		return isSPIApproved;
	}

	public void setIsSPIApproved(Boolean isSPIApproved) {
		this.isSPIApproved = isSPIApproved;
	}



	public Date getSpiApprovalDate() {
		return spiApprovalDate;
	}

	public void setSpiApprovalDate(Date spiApprovalDate) {
		this.spiApprovalDate = spiApprovalDate;
	}

	

	public long getSpiApprover() {
		return spiApprover;
	}

	public void setSpiApprover(long spiApprover) {
		this.spiApprover = spiApprover;
	}

	

	public Boolean getSendForApproval() {
		return sendForApproval;
	}

	public void setSendForApproval(Boolean sendForApproval) {
		this.sendForApproval = sendForApproval;
	}

	public Boolean getNeedRevision() {
		return needRevision;
	}

	public void setNeedRevision(Boolean needRevision) {
		this.needRevision = needRevision;
	}

	public Boolean getLegalApproval() {
		return legalApproval;
	}

	public void setLegalApproval(Boolean legalApproval) {
		this.legalApproval = legalApproval;
	}

	public String getLegalApprover() {
		return legalApprover;
	}

	public void setLegalApprover(String legalApprover) {
		this.legalApprover = legalApprover;
	}

	public Boolean getUploadedSummary() {
		return uploadedSummary;
	}

	public void setUploadedSummary(Boolean uploadedSummary) {
		this.uploadedSummary = uploadedSummary;
	}

	public Boolean getIrisOracleSummary() {
		return irisOracleSummary;
	}

	public void setIrisOracleSummary(Boolean irisOracleSummary) {
		this.irisOracleSummary = irisOracleSummary;
	}

	public Boolean getFullReport() {
		return fullReport;
	}

	public void setFullReport(Boolean fullReport) {
		this.fullReport = fullReport;
	}

	public Boolean getSummaryReport() {
		return summaryReport;
	}

	public void setSummaryReport(Boolean summaryReport) {
		this.summaryReport = summaryReport;
	}

	public Boolean getSummaryForIRIS() {
		return summaryForIRIS;
	}

	public void setSummaryForIRIS(Boolean summaryForIRIS) {
		this.summaryForIRIS = summaryForIRIS;
	}

	public Boolean getUploadToIRIS() {
		return uploadToIRIS;
	}

	public void setUploadToIRIS(Boolean uploadToIRIS) {
		this.uploadToIRIS = uploadToIRIS;
	}

	public Boolean getUploadToCPSIDdatabase() {
		return uploadToCPSIDdatabase;
	}

	public void setUploadToCPSIDdatabase(Boolean uploadToCPSIDdatabase) {
		this.uploadToCPSIDdatabase = uploadToCPSIDdatabase;
	}

	public Boolean getNeedRevisionClicked() {
		return needRevisionClicked;
	}

	public void setNeedRevisionClicked(Boolean needRevisionClicked) {
		this.needRevisionClicked = needRevisionClicked;
	}

	public Date getRepSummarySaveDate() {
		return repSummarySaveDate;
	}

	public void setRepSummarySaveDate(Date repSummarySaveDate) {
		this.repSummarySaveDate = repSummarySaveDate;
	}

	public Date getNeedRevisionClickDate() {
		return needRevisionClickDate;
	}

	public void setNeedRevisionClickDate(Date needRevisionClickDate) {
		this.needRevisionClickDate = needRevisionClickDate;
	}

	public Date getRepSummaryLegalApprovalDate() {
		return repSummaryLegalApprovalDate;
	}

	public void setRepSummaryLegalApprovalDate(Date repSummaryLegalApprovalDate) {
		this.repSummaryLegalApprovalDate = repSummaryLegalApprovalDate;
	}

	public Date getIrisUploadDate() {
		return irisUploadDate;
	}

	public void setIrisUploadDate(Date irisUploadDate) {
		this.irisUploadDate = irisUploadDate;
	}

	public Date getCpsiUploadDate() {
		return cpsiUploadDate;
	}

	public void setCpsiUploadDate(Date cpsiUploadDate) {
		this.cpsiUploadDate = cpsiUploadDate;
	}

	public Integer getLegalSignOffRequired() {
		return legalSignOffRequired;
	}

	public void setLegalSignOffRequired(Integer legalSignOffRequired) {
		this.legalSignOffRequired = legalSignOffRequired;
	}
	
}
