package com.grail.synchro.dao;

import java.util.Date;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
public interface ProjectDAO {

    /**
     * Method to fetch the project object from DB
     * @param projectID - ID of project
     * @return - Project object
     */
    Project get(final Long projectID);

    
    /**
     * Method to fetch all Synchro projects
     * @return - List of projects
     */
    List<Project> getAll();
    List<Project> getProjects(ProjectResultFilter projectResultFilter);
    List<Project> getReportProjects();
    List<Project> getReportProjects(final ProjectResultFilter projectResultFilter);
    List<Project> getProjectsByUser(final Long projectID);
    
    List<Project> getProjectsByUserAndResultFilter(final Long userID, final ProjectResultFilter projectResultFilter);

    /**
     * Method to save new Synchro project details
     * @param project -
     * @return - Returns a newly created synchro project object
     */
    Project create(final Project project);

    /**
     * Method to Update the Synchro project details
     * @param project -
     * @return - Returns a newly created synchro project object
     */
    Project update(final Project project);

    /**
     * Method to delete synchro project
     * @param projectID - Synchro project id
     */
    void delete(final Long projectID);

    /**
     * Method used to update the ownerID column for project
     * @param projectID - ID of project for which the owner field needs an update
     * @param ownerID - User ID who is the owner of this project
     */
    void updateOwner(final Long projectID, final Long ownerID);

    /**
     * Method to persist the budget approvers for this project
     * @param userID - ID of user who is making the changes to this project
     * @param projectID - ID of project for which the budget owners should be associated with
     * @param approvers - List of approver user ID's
     */
    
    
    void saveExchangeRate(final Integer currencyID);
    void updateExchangeRate(final Integer currencyID);


	/**
	 * Method to update and retrieve  Project Templates
	 */
	ProjectTemplate createTemplate(final ProjectTemplate template);
	ProjectTemplate update(final ProjectTemplate template);
	ProjectTemplate getTemplate(final Long templateID);
	List<ProjectTemplate> getAllTemplates();
	
	/**
	 * Method to update and retrieve Project Status
	 */
//	 public void setProjectStatus(final ProjectStatus projectStatus);
	 //public ProjectStatus getProjectStatus(final Long projectID, final Long endMarketID);

    public Long getTotalCount(final ProjectResultFilter filter);
    void updateCategory(final Project project);
    
    void updatePIT(final Project project);
    
    void updateProjectStatus(final Long projectId, final Integer status);
    
    /**
     * Invite User
     */
    Long addInvite(final String email);
    Long addInvite(final String email, final Long invitedBy, final Date invitedDate);
    Long getInvitedUser(final String email);
    Long getInvitedUser(final String email, final User user);

    List<InvitedUser> getInvitedUsers(final User user);
    List<InvitedUser> getInvitedUsers();
    List<InvitedUser> getInvitedUsers(final InvitedUserResultFilter filter);
    Long getInvitedUsersTotalCount(final InvitedUserResultFilter filter);

    void removeInvite(final String email);
    
    Long getInviteIdByEmail(final String email);
    
    void replaceInviteReferences(final Long referenceID, final Long id);
    
    
    Integer setStatusOnDelete(final Long projectID, final Integer status);
    
    Integer getStatusOnDelete(final Long projectID);
    
    
    Integer setStatusTrack(final Long projectID, final Integer status);
    
    Integer getStatusTrack(final Long projectID);
    
    void deleteStatusTrack(final Long projectID);
    
    
    /**
     * Investment and Funding
     */
    FundingInvestment setInvestment(FundingInvestment investment);
    
    FundingInvestment getInvestment(Long investmentID);

    FundingInvestment updateInvestment(FundingInvestment investment);
    
    void deleteInvestment(Long investmentID);
    
    List<FundingInvestment> getProjectInvestments(Long projectID);
    
    List<FundingInvestment> getProjectInvestments(Long projectID, Long endMarketId);
    
    void deleteProjectInvestments(Long projectID);

    List<ProjectPendingActivityViewBean> getPendingActivities(final ProjectResultFilter filter, final Long userID);

    Long getPendingActivitiesTotalCount(final ProjectResultFilter filter, final Long userID);
    void deleteInvestment(final Long projectID,final Long endMarketId);
    void updateProjectContact(final Long projectContact, final Long projectId, final Long endMarketId);
    void updateFundingInvSPIContact(final Long spiContact, final Long projectId, final Long endMarketId);

    Long getPendingActivityViewCount(final ProjectResultFilter filter, final Long userId);

    void updatePendingActivityViews(final ProjectResultFilter filter, final Long userId);
}
