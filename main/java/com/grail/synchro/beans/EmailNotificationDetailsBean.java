package com.grail.synchro.beans;

/**
 * @author Tejinder
 * @version 1.0
 */
public class EmailNotificationDetailsBean {
    private Long id;
    private Long projectID;
    private Long endmarketID;
    private Long agencyID;
    private Integer stageID;
    private Integer emailType;
    private Long emailTime;
    private String emailDesc;
    private String emailSubject;
    private String emailSender;
    private String emailRecipients;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getEndmarketID() {
		return endmarketID;
	}
	public void setEndmarketID(Long endmarketID) {
		this.endmarketID = endmarketID;
	}
	public Long getAgencyID() {
		return agencyID;
	}
	public void setAgencyID(Long agencyID) {
		this.agencyID = agencyID;
	}
	public Integer getStageID() {
		return stageID;
	}
	public void setStageID(Integer stageID) {
		this.stageID = stageID;
	}
	public Integer getEmailType() {
		return emailType;
	}
	public void setEmailType(Integer emailType) {
		this.emailType = emailType;
	}
	public Long getEmailTime() {
		return emailTime;
	}
	public void setEmailTime(Long emailTime) {
		this.emailTime = emailTime;
	}
	public String getEmailDesc() {
		return emailDesc;
	}
	public void setEmailDesc(String emailDesc) {
		this.emailDesc = emailDesc;
	}
	public String getEmailSubject() {
		return emailSubject;
	}
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	public String getEmailSender() {
		return emailSender;
	}
	public void setEmailSender(String emailSender) {
		this.emailSender = emailSender;
	}
	public String getEmailRecipients() {
		return emailRecipients;
	}
	public void setEmailRecipients(String emailRecipients) {
		this.emailRecipients = emailRecipients;
	}
    
}
