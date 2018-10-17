package com.grail.synchro.beans;

import java.util.List;

/**
 * @author Kanwar Grewal
 * @version 4.1, Date: 3/6/2014
 */
public class MultiMarketProject extends Project {
	private List<Long> investmentType;
	private List<Long> investmentTypeID;
    private List<Long> fieldworkMarketID;
    private List<Long> fundingMarketID; 
    private List<Long> projectContact;
    private List<Long> approved;
    private List<Long> approvalStatus;
    private List<Long> investmentID;
    
    
	public List<Long> getInvestmentType() {
		return investmentType;
	}
	public void setInvestmentType(List<Long> investmentType) {
		this.investmentType = investmentType;
	}
	public List<Long> getInvestmentTypeID() {
		return investmentTypeID;
	}
	public void setInvestmentTypeID(List<Long> investmentTypeID) {
		this.investmentTypeID = investmentTypeID;
	}
	public List<Long> getFieldworkMarketID() {
		return fieldworkMarketID;
	}
	public void setFieldworkMarketID(List<Long> fieldworkMarketID) {
		this.fieldworkMarketID = fieldworkMarketID;
	}
	public List<Long> getFundingMarketID() {
		return fundingMarketID;
	}
	public void setFundingMarketID(List<Long> fundingMarketID) {
		this.fundingMarketID = fundingMarketID;
	}
	public List<Long> getProjectContact() {
		return projectContact;
	}
	public void setProjectContact(List<Long> projectContact) {
		this.projectContact = projectContact;
	}

	public List<Long> getApproved() {
		return approved;
	}
	public void setApproved(List<Long> approved) {
		this.approved = approved;
	}

    public List<Long> getInvestmentID() {
        return investmentID;
    }

    public void setInvestmentID(List<Long> investmentID) {
        this.investmentID = investmentID;
    }
    
    

    public List<Long> getApprovalStatus() {
		return approvalStatus;
	}
	public void setApprovalStatus(List<Long> approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
	public Project toProjectbean(MultiMarketProject multiMarketProject)
	{
		Project project = new Project();
		project.setProjectID(multiMarketProject.getProjectID());
		project.setName(multiMarketProject.getName());
		project.setDescription(multiMarketProject.getDescription());
		project.setDescriptionText(multiMarketProject.getDescriptionText());
		project.setCategoryType(multiMarketProject.getCategoryType());
		project.setBrand(multiMarketProject.getBrand());
		project.setMethodologyType(multiMarketProject.getMethodologyType());
		project.setMethodologyGroup(multiMarketProject.getMethodologyGroup());
		project.setProposedMethodology(multiMarketProject.getProposedMethodology());
		project.setStartDate(multiMarketProject.getStartDate());
		project.setEndDate(multiMarketProject.getEndDate());
		project.setProjectOwner(multiMarketProject.getProjectOwner());
		project.setBriefCreator(multiMarketProject.getBriefCreator());
		project.setMultiMarket(multiMarketProject.getMultiMarket());
		project.setTotalCost(multiMarketProject.getTotalCost());
		project.setTotalCostCurrency(multiMarketProject.getTotalCostCurrency());
		project.setEndMarkets(multiMarketProject.getEndMarkets());
		project.setInitialCost(multiMarketProject.getInitialCost());
		project.setInitialCostCurrency(multiMarketProject.getInitialCostCurrency());
		project.setStatus(multiMarketProject.getStatus());
		project.setCapRating(multiMarketProject.getCapRating());
        project.setConfidential(multiMarketProject.getConfidential());
        project.setRegions(multiMarketProject.getRegions());
        project.setAreas(multiMarketProject.getAreas());
        project.setBudgetYear(multiMarketProject.getBudgetYear());
        
		return project;
	}
	

}

