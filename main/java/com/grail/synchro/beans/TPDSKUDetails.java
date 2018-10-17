package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Tejinder
 * @version 4.0, Date: 11/27/13
 */
public class TPDSKUDetails extends BeanObject {
    private Long projectID;
    private String skuDetails;
    private Date submissionDate;
    
    private Integer hasProductModification;
    private Date tpdModificationDate;
    
    private String taoCode;
    
    private Boolean sameAsPrevSubmitted;

    private Integer rowId;
    private Boolean isRowSaved;
    
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public String getSkuDetails() {
		return skuDetails;
	}
	public void setSkuDetails(String skuDetails) {
		this.skuDetails = skuDetails;
	}
	public Date getSubmissionDate() {
		return submissionDate;
	}
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
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
	public Boolean getSameAsPrevSubmitted() {
		return sameAsPrevSubmitted;
	}
	public void setSameAsPrevSubmitted(Boolean sameAsPrevSubmitted) {
		this.sameAsPrevSubmitted = sameAsPrevSubmitted;
	}
	public Integer getRowId() {
		return rowId;
	}
	public void setRowId(Integer rowId) {
		this.rowId = rowId;
	}
	public Boolean getIsRowSaved() {
		return isRowSaved;
	}
	public void setIsRowSaved(Boolean isRowSaved) {
		this.isRowSaved = isRowSaved;
	}
    
        
}
