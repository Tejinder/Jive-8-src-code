package com.grail.synchro.beans;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class PSMethodologyWaiver extends BeanObject{

    private Long projectID;
    private Long endMarketID;

    private String methodologyDeviationRationale;
    private Long methodologyApprover;
    private String methodologyApproverComment;
    private Integer isApproved;
    private Integer status;
    
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

   

  
}
