package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Tejinder
 * @version 4.0, Date: 11/27/13
 */
public class TPDSummary extends BeanObject {
    private Long projectID;
    private Integer researchDoneOn;
    
    private String productDescription;
    private String productDescriptionText;
    private Integer hasProductModification;
    private Date tpdModificationDate;
    
    private String taoCode;
    
    private Boolean previouslySubmitted;
    private Date lastSubmissionDate;
    
    private Date legalApprovalDate;
    private Integer legalApprovalStatus;
    private Long legalApprover;
    
    private String legalApproverOffline;

	public Long getProjectID() {
		return projectID;
	}

	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}

	public Integer getResearchDoneOn() {
		return researchDoneOn;
	}

	public void setResearchDoneOn(Integer researchDoneOn) {
		this.researchDoneOn = researchDoneOn;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public String getProductDescriptionText() {
		return productDescriptionText;
	}

	public void setProductDescriptionText(String productDescriptionText) {
		this.productDescriptionText = productDescriptionText;
	}

	public Integer getHasProductModification() {
		return hasProductModification;
	}

	public void setHasProductModification(Integer hasProductModification) {
		this.hasProductModification = hasProductModification;
	}

	public Date getTpdModificationDate() {
		return tpdModificationDate;
	}

	public void setTpdModificationDate(Date tpdModificationDate) {
		this.tpdModificationDate = tpdModificationDate;
	}

	public String getTaoCode() {
		return taoCode;
	}

	public void setTaoCode(String taoCode) {
		this.taoCode = taoCode;
	}

	public Boolean getPreviouslySubmitted() {
		return previouslySubmitted;
	}

	public void setPreviouslySubmitted(Boolean previouslySubmitted) {
		this.previouslySubmitted = previouslySubmitted;
	}

	public Date getLastSubmissionDate() {
		return lastSubmissionDate;
	}

	public void setLastSubmissionDate(Date lastSubmissionDate) {
		this.lastSubmissionDate = lastSubmissionDate;
	}

	public Date getLegalApprovalDate() {
		return legalApprovalDate;
	}

	public void setLegalApprovalDate(Date legalApprovalDate) {
		this.legalApprovalDate = legalApprovalDate;
	}

	public Integer getLegalApprovalStatus() {
		return legalApprovalStatus;
	}

	public void setLegalApprovalStatus(Integer legalApprovalStatus) {
		this.legalApprovalStatus = legalApprovalStatus;
	}

	public Long getLegalApprover() {
		return legalApprover;
	}

	public void setLegalApprover(Long legalApprover) {
		this.legalApprover = legalApprover;
	}

	public String getLegalApproverOffline() {
		return legalApproverOffline;
	}

	public void setLegalApproverOffline(String legalApproverOffline) {
		this.legalApproverOffline = legalApproverOffline;
	}
    
    
}
