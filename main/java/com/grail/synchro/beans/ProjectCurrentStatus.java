package com.grail.synchro.beans;

import java.util.Date;



/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectCurrentStatus extends BeanObject {

	  private long projectID;
	  private long endMarketID;
	  private String activityDesc;
	  private String status;
	  private String personResponsible;
	  private String nextStep;
	  private String nextStepLink;
	  private String personRespSubject;
	  private String personRespMessage;
	  private String personRespEmail;
	  
	  private String nextStepSubject;
	  private String nextStepMessage;
	  private String nextStepPersonEmail;
	  
	  private Boolean nextStepEnable;
	  private Boolean mandatory;
	  
	  private Date completionDate;
	  
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
	public String getActivityDesc() {
		return activityDesc;
	}
	public void setActivityDesc(String activityDesc) {
		this.activityDesc = activityDesc;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPersonResponsible() {
		return personResponsible;
	}
	public void setPersonResponsible(String personResponsible) {
		this.personResponsible = personResponsible;
	}
	public String getNextStep() {
		return nextStep;
	}
	public void setNextStep(String nextStep) {
		this.nextStep = nextStep;
	}
	public String getNextStepLink() {
		return nextStepLink;
	}
	public void setNextStepLink(String nextStepLink) {
		this.nextStepLink = nextStepLink;
	}
	public String getPersonRespSubject() {
		return personRespSubject;
	}
	public void setPersonRespSubject(String personRespSubject) {
		this.personRespSubject = personRespSubject;
	}
	public String getPersonRespMessage() {
		return personRespMessage;
	}
	public void setPersonRespMessage(String personRespMessage) {
		this.personRespMessage = personRespMessage;
	}
	public String getPersonRespEmail() {
		return personRespEmail;
	}
	public void setPersonRespEmail(String personRespEmail) {
		this.personRespEmail = personRespEmail;
	}
	public String getNextStepSubject() {
		return nextStepSubject;
	}
	public void setNextStepSubject(String nextStepSubject) {
		this.nextStepSubject = nextStepSubject;
	}
	public String getNextStepMessage() {
		return nextStepMessage;
	}
	public void setNextStepMessage(String nextStepMessage) {
		this.nextStepMessage = nextStepMessage;
	}
	public String getNextStepPersonEmail() {
		return nextStepPersonEmail;
	}
	public void setNextStepPersonEmail(String nextStepPersonEmail) {
		this.nextStepPersonEmail = nextStepPersonEmail;
	}
	public Boolean getNextStepEnable() {
		return nextStepEnable;
	}
	public void setNextStepEnable(Boolean nextStepEnable) {
		this.nextStepEnable = nextStepEnable;
	}
	public Boolean getMandatory() {
		return mandatory;
	}
	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}
	public Date getCompletionDate() {
		return completionDate;
	}
	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}
	
	
}
