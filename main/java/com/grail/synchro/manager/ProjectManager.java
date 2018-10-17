package com.grail.synchro.manager;

import java.util.Date;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
public interface ProjectManager {

    Project save(final Project synchroProject);

    void delete(final Long projectID);

    void updateOwner(final Long projectID, final Long ownerID);

    Project get(final Long projectID);
    

    List<Project> getAll();
    
    List<Project> getProjects(final ProjectResultFilter projectResultFilter);
    
    List<Project> getReportProjects();
    
    List<Project> getReportProjects(ProjectResultFilter projectResultFilter);
    
    List<Project> getProjectsByUser(final Long userID);

    List<Project> getProjectsByUserAndResultFilter(final Long userID, final ProjectResultFilter projectResultFilter);
    
    // Saves exchange rate information for the currencies
    void saveExchangeRate(final Integer currencyID);
    void updateExchangeRate(final Integer currencyID);

	
	/**
	 * Manage Project Templates for Synchro 
	 * 
	 */
	ProjectTemplate saveTemplate(final ProjectTemplate template);
	ProjectTemplate getTemplate(final Long templateID);
	List<ProjectTemplate> getAllTemplates();
	
	
	/**
	 * Manage End Market Investment Details
	 */
	void saveEndMarketDetail(final EndMarketInvestmentDetail endMarketInvestmentDetail);

	void updateEndMarketDetail(final EndMarketInvestmentDetail endMarketInvestmentDetail);
	
	EndMarketInvestmentDetail getEndMarketDetail(final Long projectID, final Long endMarketID);
	
	List<EndMarketInvestmentDetail> getEndMarketDetails(final Long projectID);
	
	List<Long> getEndMarketIDs(final Long projectID);
	
	void deleteEndMarketDetail(final Long projectID, final Long endMarketID);
    
    void deleteEndMarketDetail(final Long projectID);
    
    void updateSingleEndMarketId(final Long projectID, final Long endMarketID); 
    
    void updateSPIContact(final Long projectID,final Long endMarketID, final Long spiContact);
	
    /**
     *
     */
    Long getTotalCount(final ProjectResultFilter filter);
    
    void updatePIT(Project project, EndMarketInvestmentDetail endMarketDetail);
    
    void updateProjectStatus(final Long projectId, final Integer status);
    
    /**
     * Invite User
     */
    Long addInvite(final String email);
    Long addInvite(final String email, final Long invitedBy, final Date invitedDate);
    Long addInvite(final String email, final User invitedBy, final Date invitedDate);
    Long getInvitedUser(final String email);
    Long getInvitedUser(final String email, final User user);

    List<InvitedUser> getInvitedUsers(final User user);
    List<InvitedUser> getInvitedUsers();
    List<InvitedUser> getInvitedUsers(final InvitedUserResultFilter filter);
    Long getInvitedUsersTotalCount(final InvitedUserResultFilter filter);
    
    void removeInvite(final String email);
    
    Long getInviteIdByEmail(final String email);
    
    void replaceInviteReferences(final Long referenceID, final Long id);
    
    /**
     * Project Status while deleting
     */
    
    Integer setStatusOnDelete(final Long projectID, final Integer status);
    
    Integer getStatusOnDelete(final Long projectID);
    
    
    /**
     * Project End Market Status
     */
    
    Integer getEndMarketStatus(final Long projectID, final Long endmarketID);
    
    void deleteEndMarketStatus(final Long projectID, final List<Long> endmarketIDs);
    
    void deleteEndMarketStatus(final Long projectID, final Long endmarketID);
    
    void setEndMarketStatus(final Long projectID, final List<Long> endmarketIDs, final Integer status);
    
    void setEndMarketStatus(final Long projectID, final Long endmarketID, final Integer status);
    
    void setAllEndMarketStatus(final Long projectID, final Integer status);
    
    /**
     * Project Status while deleting
     */
    
    Integer setStatusTrack(final Long projectID, final Integer status);
    
    Integer getStatusTrack(final Long projectID);
    
    void deleteStatusTrack(final Long projectID);
    
    
    /**
     * Investment and Funding
     */
    FundingInvestment setInvestment(FundingInvestment investment);
    
    FundingInvestment getInvestment(Long investmentID);
    
    void deleteInvestment(Long investmentID);
    
    List<FundingInvestment> setProjectInvestments(List<FundingInvestment> investments);

    List<FundingInvestment> setProjectInvestments(final List<FundingInvestment> investments, final Long projectID);
    
    List<FundingInvestment> getProjectInvestments(Long projectID);
    List<FundingInvestment> getProjectInvestments(Long projectID, Long endMarketId);
    
    void deleteProjectInvestments(Long projectID);

    List<ProjectPendingActivityViewBean> getPendingActivities(final ProjectResultFilter filter, final Long userID);

    Long getPendingActivitiesTotalCount(final ProjectResultFilter filter, final Long userID);
    void updateInitialCostSingleEM(EndMarketInvestmentDetail endMarketDetail);
    
    void deleteInvestment(final Long projectID,final Long endMarketId);
    void updateProjectContact(final Long projectContact, final Long projectId, final Long endMarketId);
    void updateFundingInvSPIContact(final Long spiContact, final Long projectId, final Long endMarketId);

    Long getPendingActivityViewCount(final ProjectResultFilter filter, final Long userId);
    void updatePendingActivityViews(final ProjectResultFilter filter, final Long userId);

}

