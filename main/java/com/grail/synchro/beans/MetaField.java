package com.grail.synchro.beans;

public class MetaField {
	
	public MetaField()
	{
	
	}
	public MetaField(Long id, String name)
	{
		this.id = id;
		this.name = name;
	}
	private Long id;
	private String name;
	private boolean isLessFrequent;
	private boolean briefException;
	private boolean proposalException;
	private boolean agencyWaiverException;
	private boolean repSummaryException;
	
	private Integer brandSpecific;
	private Integer brandType;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isLessFrequent() {
		return isLessFrequent;
	}
	public void setLessFrequent(boolean isLessFrequent) {
		this.isLessFrequent = isLessFrequent;
	}
	public boolean isBriefException() {
		return briefException;
	}
	public void setBriefException(boolean briefException) {
		this.briefException = briefException;
	}
	public boolean isProposalException() {
		return proposalException;
	}
	public void setProposalException(boolean proposalException) {
		this.proposalException = proposalException;
	}
	public boolean isAgencyWaiverException() {
		return agencyWaiverException;
	}
	public void setAgencyWaiverException(boolean agencyWaiverException) {
		this.agencyWaiverException = agencyWaiverException;
	}
	public boolean isRepSummaryException() {
		return repSummaryException;
	}
	public void setRepSummaryException(boolean repSummaryException) {
		this.repSummaryException = repSummaryException;
	}
	public Integer getBrandSpecific() {
		return brandSpecific;
	}
	public void setBrandSpecific(Integer brandSpecific) {
		this.brandSpecific = brandSpecific;
	}
	public Integer getBrandType() {
		return brandType;
	}
	public void setBrandType(Integer brandType) {
		this.brandType = brandType;
	}
	
}
