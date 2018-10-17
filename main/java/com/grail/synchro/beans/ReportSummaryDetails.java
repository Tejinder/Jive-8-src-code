package com.grail.synchro.beans;

import java.util.Date;
import java.util.List;


/**
 * @author: tejinder
 * @since: 1.0
 */
public class ReportSummaryDetails extends BeanObject {

    private long projectID;
    private int reportType;
    private int reportOrderId;
    private String legalApprover;
    private Date legalApprovalDate;
    private List<Long> attachmentId;
    
	public long getProjectID() {
		return projectID;
	}
	public void setProjectID(long projectID) {
		this.projectID = projectID;
	}
	public int getReportType() {
		return reportType;
	}
	public void setReportType(int reportType) {
		this.reportType = reportType;
	}
	public int getReportOrderId() {
		return reportOrderId;
	}
	public void setReportOrderId(int reportOrderId) {
		this.reportOrderId = reportOrderId;
	}
	public String getLegalApprover() {
		return legalApprover;
	}
	public void setLegalApprover(String legalApprover) {
		this.legalApprover = legalApprover;
	}
	public Date getLegalApprovalDate() {
		return legalApprovalDate;
	}
	public void setLegalApprovalDate(Date legalApprovalDate) {
		this.legalApprovalDate = legalApprovalDate;
	}
	public List<Long> getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(List<Long> attachmentId) {
		this.attachmentId = attachmentId;
	}
    
    
    
	
}
