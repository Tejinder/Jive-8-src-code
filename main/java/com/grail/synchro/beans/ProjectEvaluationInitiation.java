package com.grail.synchro.beans;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectEvaluationInitiation extends BeanObject {

    private long projectID;
    private long endMarketId;
  
    //Generic Project Evaluation Stage fields
 
    private Integer agencyPerfIM;
    private String batCommentsIM;
    private String agencyCommentsIM;

    private Integer agencyPerfLM;
    private String batCommentsLM;
    private String agencyCommentsLM;
    
    private Integer agencyPerfFA;
    private String batCommentsFA;
    private String agencyCommentsFA;
    
    private Integer status;
    
    private Long agencyId;
    private Integer agencyRating;
    private String agencyComments;
    
    
  
    
	public long getProjectID() {
		return projectID;
	}
	public void setProjectID(long projectID) {
		this.projectID = projectID;
	}
	
	public Integer getAgencyPerfIM() {
		return agencyPerfIM;
	}
	public void setAgencyPerfIM(Integer agencyPerfIM) {
		this.agencyPerfIM = agencyPerfIM;
	}
	public String getBatCommentsIM() {
		return batCommentsIM;
	}
	public void setBatCommentsIM(String batCommentsIM) {
		this.batCommentsIM = batCommentsIM;
	}
	public String getAgencyCommentsIM() {
		return agencyCommentsIM;
	}
	public void setAgencyCommentsIM(String agencyCommentsIM) {
		this.agencyCommentsIM = agencyCommentsIM;
	}
	public Integer getAgencyPerfLM() {
		return agencyPerfLM;
	}
	public void setAgencyPerfLM(Integer agencyPerfLM) {
		this.agencyPerfLM = agencyPerfLM;
	}
	public String getBatCommentsLM() {
		return batCommentsLM;
	}
	public void setBatCommentsLM(String batCommentsLM) {
		this.batCommentsLM = batCommentsLM;
	}
	public String getAgencyCommentsLM() {
		return agencyCommentsLM;
	}
	public void setAgencyCommentsLM(String agencyCommentsLM) {
		this.agencyCommentsLM = agencyCommentsLM;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public long getEndMarketId() {
		return endMarketId;
	}
	public void setEndMarketId(long endMarketId) {
		this.endMarketId = endMarketId;
	}
	public Integer getAgencyPerfFA() {
		return agencyPerfFA;
	}
	public void setAgencyPerfFA(Integer agencyPerfFA) {
		this.agencyPerfFA = agencyPerfFA;
	}
	public String getBatCommentsFA() {
		return batCommentsFA;
	}
	public void setBatCommentsFA(String batCommentsFA) {
		this.batCommentsFA = batCommentsFA;
	}
	public String getAgencyCommentsFA() {
		return agencyCommentsFA;
	}
	public void setAgencyCommentsFA(String agencyCommentsFA) {
		this.agencyCommentsFA = agencyCommentsFA;
	}
	public Long getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}
	public Integer getAgencyRating() {
		return agencyRating;
	}
	public void setAgencyRating(Integer agencyRating) {
		this.agencyRating = agencyRating;
	}
	public String getAgencyComments() {
		return agencyComments;
	}
	public void setAgencyComments(String agencyComments) {
		this.agencyComments = agencyComments;
	}

	
	
}
