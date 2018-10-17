package com.grail.synchro.beans;

import java.util.List;

public class ProjectWaiver extends BeanObject {
	
    private Long waiverID;
    private String name;
    private String summary;
    private Long brand = -1L;
    private Long methodology = -1L;
    private List<Long> preApprovals;
    private String preApprovalComment;
    private Long nexus;
    private Long status;
    private List<Long> endMarkets;
    private Long approverID;
    private String approverComments;
    private Boolean isKantar;

    public ProjectWaiver() {
    }

    public ProjectWaiver(Long waiverID, String name, String summary,
                         Long brand, Long methodology, List<Long> preApprovals, String preApprovalComment, Long nexus, Long status, List<Long> endMarkets, Long approverID, String approverComments) {
    	 this.waiverID =waiverID;
    	 this.name = name;
    	 this.summary= summary;
    	 this.brand = brand;
    	 this.methodology = methodology;
    	 this.preApprovals = preApprovals;
    	 this.preApprovalComment= preApprovalComment;
    	 this.nexus = nexus;
    	 this.status = status;
    	 this.endMarkets = endMarkets;
    	 this.approverID = approverID;
    	 this.approverComments = approverComments;
    }

	public Long getWaiverID() {
		return waiverID;
	}

	public void setWaiverID(Long waiverID) {
		this.waiverID = waiverID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Long getBrand() {
		return brand;
	}

	public void setBrand(Long brand) {
		this.brand = brand;
	}

	public Long getMethodology() {
		return methodology;
	}

	public void setMethodology(Long methodology) {
		this.methodology = methodology;
	}

	public List<Long> getPreApprovals() {
		return preApprovals;
	}

	public void setPreApprovals(List<Long> preApprovals) {
		this.preApprovals = preApprovals;
	}

	public String getPreApprovalComment() {
		return preApprovalComment;
	}

	public void setPreApprovalComment(String preApprovalComment) {
		this.preApprovalComment = preApprovalComment;
	}

	public Long getNexus() {
		return nexus;
	}

	public void setNexus(Long nexus) {
		this.nexus = nexus;
	}

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

	public List<Long> getEndMarkets() {
		return endMarkets;
	}

	public void setEndMarkets(List<Long> endMarkets) {
		this.endMarkets = endMarkets;
	}

	public Long getApproverID() {
		return approverID;
	}

	public void setApproverID(Long approverID) {
		this.approverID = approverID;
	}

	public String getApproverComments() {
		return approverComments;
	}

	public void setApproverComments(String approverComments) {
		this.approverComments = approverComments;
	}

	public Boolean getIsKantar() {
		return isKantar;
	}

	public void setIsKantar(Boolean isKantar) {
		this.isKantar = isKantar;
	}

    
}
