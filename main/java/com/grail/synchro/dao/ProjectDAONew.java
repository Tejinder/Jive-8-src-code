package com.grail.synchro.dao;

import java.util.Date;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;
import java.math.BigDecimal;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
public interface ProjectDAONew {

    /**
     * Method to fetch the project object from DB
     * @param projectID - ID of project
     * @return - Project object
     */
    Project get(final Long projectID);

    Project get(final Long projectID, final String projectName, final Integer budgetLocation);
    
    /**
     * Method to fetch all Synchro projects
     * @return - List of projects
     */
    List<Project> getAll();
    List<Project> getAllNew();
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

    Project updateProjectMigrate(final Project project);
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
    
    void updateProjectNewSynchroFlag(final Long projectId, final Integer newSynchro);
    
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
    
    void saveProjectCostDetails(final ProjectCostDetailsBean projectCostBean);
    List<ProjectCostDetailsBean> getProjectCostDetails(final Long projectId);
    
    List<ProjectCostDetailsBean> getProjectCostDetails(final Long projectId, final Long agencyId);
    
    void deleteProjectCostDetails(final Long projectID);
    void updateProjectTotalCost(final Long projectID, final BigDecimal totalCost, final int totalCostCurrency);
    void updateProjectMW(final Project project);
    void updateProjectNew(final Project project);
    
    void updateCancelProject(final Long projectId, final Integer status);
    List<String> getSynchroUserNames();
    Long getProjectDashboardCount(final ProjectResultFilter filter);
    
    List<Project> getAllProjectsForQPRSnapshot();
    List<Project> getBudgetYearProjectsForQPRSnapshot(final Integer budgetYear);
    
    List<Integer> getBudgetLocationsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    List<Project> getSpendByBudgetLocationLatestCost(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter);
    
    List<String> getMethodologiesForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    
    List<String> getCategoryTypesForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    
    List<Project> getSpendByMethodologyLatestCost(final Integer budgetYear, final String methodology, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    List<Project> getSpendByCategoryTypeLatestCost(final Integer budgetYear, final String categoryType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    List<Integer> getBrandsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    List<Project> getSpendByBrandLatestCost(final Integer budgetYear, final Integer brand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Integer> getNonBrandsForQPRSnapshot(final Integer budgetYear , SpendReportExtractFilter spendReportFilter);
    List<Project> getSpendByNonBrandLatestCost(final Integer budgetYear, final Integer nonBrand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    List<ProjectCostDetailsBean> getSpendByAgencyLatestCost(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<ProjectCostDetailsBean> getSpendByAgencyTypeLatestCost(final Integer budgetYear, final Integer agencyType,SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    List<Project> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    List<Project> getSpendByBudgetYearProjectsForQPRSnapshot(List<Long> projectIds);
    
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBrand(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation, String methodology, Integer brand);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostNonBrand(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation, String methodology, Integer nonbrand);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBLMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation, String methodology);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBrandMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, String methodology, Integer brand);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostNonBrandMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, String methodology, Integer nonbrand);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBL(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, String methodology);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBrandOnly(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer brand);
    List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostNonBrandOnly(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds,Integer nonbrand);
    
    List<Project> getSpendByLatestCostBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer brand, final List<Long> projectIds);
    List<Project> getSpendByLatestCostNonBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer nonbrand, final List<Long> projectIds);
    List<Project> getSpendByCrossTabBLMethodologyLatestCost(final Integer budgetYear ,final Integer budgetLocation, final String methodology, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Project> getSpendByCrossTabMethodologyBrandLatestCost(final Integer budgetYear , final String methodology, final Integer brand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Project> getSpendByCrossTabMethodologyNonBrandLatestCost(final Integer budgetYear , final String methodology, final Integer nonbrand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    List<Project> getSpendByLatestCostBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer brand, String categoryType, final List<Long> projectIds);
    List<Project> getSpendByLatestCostNonBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer nonbrand, final String categoryType, final List<Long> projectIds);
    
    List<Long> getAllProjects(final Long budgetYear, Long budgetLocation);
    
    List<Project> getSpendByBudgetLocationLatestCost(final Integer budgetYear, final Integer budgetLocation, String region, String area, String t20_t40, SpendReportExtractFilter spendReportFilter);
    
    List<Project> getSpendByMethodologyLatestCost(final Integer budgetYear, final String methodology, final String methodologyGroup, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    List<Project> getSpendByBrandLatestCost(final Integer budgetYear, final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Project> getSpendByNonBrandLatestCost(final Integer budgetYear, final Integer nonBrand, String brandtype, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    List<Project> getSpendByCrossTabBLMethodologyLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_40, final String methodology, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Project> getSpendByCrossTabBLMethodologyBrandLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Project> getSpendByCrossTabBLMethodologyNonBrandLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Project> getSpendByCrossTabBLMethodologyBrandCTLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer brand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    List<Project> getSpendByCrossTabBLMethodologyNonBrandCTLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer nonBrand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds);
    
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer brand, String brandType, String categoryType, Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    
    Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType, Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer brand, String brandType);
    Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer nonBrand, String brandType);
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, String categoryType);
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer brand, String brandType, String categoryType);
    Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer nonBrand, String brandType, String categoryType);
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup);
    
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer brand, String brandType);
    Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer nonBrand, String brandType);
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40,  String categoryType);
    Project getProjectsBrand(final Long projectID, final String projectName, String methodology, String methGroup, Integer brand, String brandType);
    Project getProjectsNonBrand(final Long projectID, final String projectName, String methodology, String methGroup, Integer nonBrand, String brandType);
    Project getProjectsBrand(final Long projectID, final String projectName, String methodology, String methGroup, String categoryType);
    
    Project getProjectsBrand(final Long projectID, final String projectName, Integer brand, String brandType, String categoryType);
    Project getProjectsNonBrand(final Long projectID, final String projectName, Integer nonBrand, String brandType, String categoryType);
    Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40);
    Project getProjectsBrand(final Long projectID, final String projectName, Integer brand, String brandType);
    Project getProjectsNonBrand(final Long projectID, final String projectName,  Integer nonBrand, String brandType);
    Project getProjectsBrand(final Long projectID, final String projectName, String categoryType);
    
    ProjectCostDetailsBean getProjectsBrandAgency(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer brand, String brandType, String categoryType, Long agency, String agencyType, Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    ProjectCostDetailsBean getProjectsNonBrandAgency(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType, Long agency, String agencyType, Integer budgetYear, SpendReportExtractFilter spendReportFilter);
    
}
