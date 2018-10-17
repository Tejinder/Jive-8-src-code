package com.grail.synchro.beans;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class PIBStakeholderList {

    private Long projectID;
    private Long endMarketID;

    private Long agencyContact1;
    private Long agencyContact2;
    private Long agencyContact3;
    private Long globalLegalContact;
    private Long globalProcurementContact;
    private Long globalCommunicationAgency;
    
    private Long agencyContact1Optional;
    private Long agencyContact2Optional;
    private Long agencyContact3Optional;
	private Long productContact;
	
    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

	public Long getEndMarketID() {
		return endMarketID;
	}

	public void setEndMarketID(Long endMarketID) {
		this.endMarketID = endMarketID;
	}

	public Long getAgencyContact1() {
		return agencyContact1;
	}

	public void setAgencyContact1(Long agencyContact1) {
		this.agencyContact1 = agencyContact1;
	}

	public Long getAgencyContact2() {
		return agencyContact2;
	}

	public void setAgencyContact2(Long agencyContact2) {
		this.agencyContact2 = agencyContact2;
	}

	public Long getAgencyContact3() {
		return agencyContact3;
	}

	public void setAgencyContact3(Long agencyContact3) {
		this.agencyContact3 = agencyContact3;
	}

	public Long getGlobalLegalContact() {
		return globalLegalContact;
	}

	public void setGlobalLegalContact(Long globalLegalContact) {
		this.globalLegalContact = globalLegalContact;
	}

	public Long getGlobalProcurementContact() {
		return globalProcurementContact;
	}

	public void setGlobalProcurementContact(Long globalProcurementContact) {
		this.globalProcurementContact = globalProcurementContact;
	}

	public Long getGlobalCommunicationAgency() {
		return globalCommunicationAgency;
	}

	public void setGlobalCommunicationAgency(Long globalCommunicationAgency) {
		this.globalCommunicationAgency = globalCommunicationAgency;
	}

	public Long getAgencyContact1Optional() {
		return agencyContact1Optional;
	}

	public void setAgencyContact1Optional(Long agencyContact1Optional) {
		this.agencyContact1Optional = agencyContact1Optional;
	}

	public Long getAgencyContact2Optional() {
		return agencyContact2Optional;
	}

	public void setAgencyContact2Optional(Long agencyContact2Optional) {
		this.agencyContact2Optional = agencyContact2Optional;
	}

	public Long getAgencyContact3Optional() {
		return agencyContact3Optional;
	}

	public void setAgencyContact3Optional(Long agencyContact3Optional) {
		this.agencyContact3Optional = agencyContact3Optional;
	}
	
	public Long getProductContact() {
		return productContact;
	}

	public void setProductContact(Long productContact) {
		this.productContact = productContact;
	}
	

  
}
