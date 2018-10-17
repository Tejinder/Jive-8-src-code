package com.grail.synchro.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.*;
import com.grail.synchro.manager.*;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.dao.EndMarketDetailDAO;
import com.grail.synchro.dao.ProjectDAO;
import com.grail.synchro.search.filter.ProjectResultFilter;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
public class ProjectManagerImpl implements ProjectManager {

    private static final Logger LOG = Logger.getLogger(ProjectManagerImpl.class);
    private ProjectDAO projectDAO;
    private EndMarketDetailDAO endMarketDetailDAO;
    private StageManager stageManager;

    private static ProposalManager proposalManager;
    private static PIBManager pibManager;
    private static ProjectSpecsManager projectSpecsManager;
    private static ReportSummaryManager reportSummaryManager;
    private static ProjectEvaluationManager projectEvaluationManager;

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Project save(Project project) {
    	 LOG.info("Checking Region in Manager Impl initial -->"+project.getRegions());
        if (project.getProjectID() == null) {
            LOG.info("Creating synchro project.");
            LOG.info("Checking Region in Manager Impl-->"+project.getRegions());
            project = projectDAO.create(project);
        } else {
            LOG.info("Updating synchro project.");
            project = projectDAO.update(project);
        }
        return project;
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void delete(final Long projectID) {
        projectDAO.delete(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateOwner(final Long projectID, final Long ownerID) {
        projectDAO.updateOwner(projectID, ownerID);
    }

    @Override
    public Project get(final Long projectID) {
        final Project project = projectDAO.get(projectID);
        return project;
    }


    @Override
    public List<Project> getAll() {
        return projectDAO.getAll();
    }
    /**
     * Get All projects based on the result filter
     */
    @Override
    public List<Project> getProjects(ProjectResultFilter projectResultFilter) {
        return projectDAO.getProjects(projectResultFilter);
    }



    /**
     * This will fetch all the projects for Reporting section
     * Projects with 1)	DRAFT, 2)DELETED 3)	CANCEL
     * Status will not be fetched.
     */
    @Override
    public List<Project> getReportProjects()
    {
        return projectDAO.getReportProjects();
    }

    @Override
    public List<Project> getReportProjects(ProjectResultFilter projectResultFilter)
    {
        return projectDAO.getReportProjects(projectResultFilter);
    }

    @Override
    public List<Project> getProjectsByUser(final Long userID) {
        return projectDAO.getProjectsByUser(userID);
    }

    @Override
    public List<Project> getProjectsByUserAndResultFilter(final Long userID, final ProjectResultFilter projectResultFilter) {
        return projectDAO.getProjectsByUserAndResultFilter(userID, projectResultFilter);
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void saveExchangeRate(final Integer currencyID) {
        projectDAO.saveExchangeRate(currencyID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateExchangeRate(final Integer currencyID) {
        projectDAO.updateExchangeRate(currencyID);
    }


    /**
     * Method to create and update Project Templates
     *
     * @param template
     * @return
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public ProjectTemplate saveTemplate(ProjectTemplate template) {
        if (template.getTemplateID() == null) {
            LOG.info("Creating new synchro project Template");
            template = projectDAO.createTemplate(template);
        } else {
            LOG.info("Updating synchro project.");
            template = projectDAO.update(template);
        }
        return template;
    }

    @Override
    public ProjectTemplate getTemplate(final Long templateID) {
        final ProjectTemplate template = projectDAO.getTemplate(templateID);
        //TODO Get End Markets
        return template;
    }

    @Override
    public List<ProjectTemplate> getAllTemplates() {
        return projectDAO.getAllTemplates();
    }

    /**
     * End Market Investment Methods
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void saveEndMarketDetail(final EndMarketInvestmentDetail endMarketInvestmentDetail) {
        endMarketDetailDAO.save(endMarketInvestmentDetail);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateEndMarketDetail(final EndMarketInvestmentDetail endMarketInvestmentDetail) {
        endMarketDetailDAO.update(endMarketInvestmentDetail);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateSingleEndMarketId(final Long projectID, final Long endMarketID) {
        endMarketDetailDAO.updateSingleEndMarketId(projectID,endMarketID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateSPIContact(final Long projectID,final Long endMarketID, final Long spiContact) {
        endMarketDetailDAO.updateSPIContact(projectID,endMarketID,spiContact);
    }

    @Override
    public EndMarketInvestmentDetail getEndMarketDetail(final Long projectID, final Long endMarketID) {
        return endMarketDetailDAO.get(projectID, endMarketID);
    }

    @Override
    public List<EndMarketInvestmentDetail> getEndMarketDetails(final Long projectID) {
        return endMarketDetailDAO.getProjectEndMarkets(projectID);
    }

    @Override
    public List<Long> getEndMarketIDs(final Long projectID) {
        return endMarketDetailDAO.getProjectEndMarketIds(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteEndMarketDetail(final Long projectID, final Long endMarketID) {
        endMarketDetailDAO.delete(projectID, endMarketID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteEndMarketDetail(final Long projectID) {
        endMarketDetailDAO.delete(projectID);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getTotalCount(final ProjectResultFilter filter) {
        return projectDAO.getTotalCount(filter);
    }
    /**
     * This method will update the PIT Changes from the PIB Window
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updatePIT(Project project, EndMarketInvestmentDetail endMarketDetail)
    {
        projectDAO.updatePIT(project);
        if(endMarketDetail!=null)
        {
            endMarketDetailDAO.updateInitialCost(endMarketDetail);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateInitialCostSingleEM(EndMarketInvestmentDetail endMarketDetail)
    {
    	if(endMarketDetail!=null)
        {
            endMarketDetailDAO.updateInitialCostSM(endMarketDetail);
        }
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long addInvite(final String email)
    {
        return projectDAO.addInvite(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long addInvite(final String email, final Long invitedBy, final Date invitedDate) {
        return  projectDAO.addInvite(email, invitedBy, invitedDate);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long addInvite(final String email, final User invitedBy, final Date invitedDate) {
        return projectDAO.addInvite(email, invitedBy.getID(), invitedDate);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInvitedUser(final String email) {
        return projectDAO.getInvitedUser(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInvitedUser(String email, User user) {
        return projectDAO.getInvitedUser(email, user);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<InvitedUser> getInvitedUsers(User user) {
        return projectDAO.getInvitedUsers(user);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<InvitedUser> getInvitedUsers() {
        return projectDAO.getInvitedUsers();
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<InvitedUser> getInvitedUsers(final InvitedUserResultFilter filter) {
        return projectDAO.getInvitedUsers(filter);
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInvitedUsersTotalCount(final InvitedUserResultFilter filter) {
        return projectDAO.getInvitedUsersTotalCount(filter);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void  removeInvite(final String email)
    {
        projectDAO.removeInvite(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInviteIdByEmail(final String email)
    {
        return projectDAO.getInviteIdByEmail(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void replaceInviteReferences(final Long referenceID, final Long id)
    {
        projectDAO.replaceInviteReferences(referenceID, id);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Integer setStatusOnDelete(final Long projectID, final Integer status)
    {
        return projectDAO.setStatusOnDelete(projectID, status);
    }

    @Override
    public Integer getStatusOnDelete(final Long projectID)
    {
        return projectDAO.getStatusOnDelete(projectID);
    }

    @Override
    public Integer getEndMarketStatus(final Long projectID, final Long endmarketID)
    {
        return endMarketDetailDAO.getEndMarketStatus(projectID, endmarketID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteEndMarketStatus(final Long projectID, final List<Long> endmarketIDs)
    {
        endMarketDetailDAO.deleteEndMarketStatus(projectID, endmarketIDs);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteEndMarketStatus(final Long projectID, final Long endmarketID)
    {
        endMarketDetailDAO.deleteEndMarketStatus(projectID, endmarketID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void setEndMarketStatus(final Long projectID, final List<Long> endmarketIDs, final Integer status)
    {
        endMarketDetailDAO.setEndMarketStatus(projectID, endmarketIDs, status);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void setEndMarketStatus(final Long projectID, final Long endmarketID, final Integer status)
    {
        endMarketDetailDAO.setEndMarketStatus(projectID, endmarketID, status);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void setAllEndMarketStatus(final Long projectID, final Integer status)
    {
        List<Long> eids = endMarketDetailDAO.getProjectEndMarketIds(projectID);
        for(Long eid : eids)
        {
            endMarketDetailDAO.setEndMarketStatus(projectID, eid, status);
        }
    }

    /**
     * Method to update the project status
     * @param projectId
     * @param status
     */
    @Override
    public void updateProjectStatus(final Long projectId, final Integer status)
    {
        projectDAO.updateProjectStatus(projectId, status);
    }

    public void setProjectDAO(final ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void setEndMarketDetailDAO(final EndMarketDetailDAO endMarketDetailDAO) {
        this.endMarketDetailDAO = endMarketDetailDAO;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Integer setStatusTrack(final Long projectID, final Integer status)
    {
        return projectDAO.setStatusTrack(projectID, status);
    }

    @Override
    public Integer getStatusTrack(final Long projectID)
    {
        return projectDAO.getStatusTrack(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteStatusTrack(final Long projectID)
    {
        projectDAO.deleteStatusTrack(projectID);
    }

    /**
     * Investment and Funding APIs
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public FundingInvestment setInvestment(final FundingInvestment investment)
    {
        return projectDAO.setInvestment(investment);
    }

    @Override
    public FundingInvestment getInvestment(final Long investmentID)
    {
        return projectDAO.getInvestment(investmentID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteInvestment(final Long investmentID)
    {
        projectDAO.deleteInvestment(investmentID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteInvestment(final Long projectID,final Long endMarketId)
    {
    	projectDAO.deleteInvestment(projectID,endMarketId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<FundingInvestment> setProjectInvestments(final List<FundingInvestment> investments)
    {
        List<FundingInvestment> updatedInvestments = new ArrayList<FundingInvestment>();
        for(FundingInvestment investment: investments)
        {
            FundingInvestment updatedInvestment = null;
            if(investment.getInvestmentID() != null && investment.getInvestmentID() > 0) {
                updatedInvestment = projectDAO.updateInvestment(investment);
            } else {
                updatedInvestment = projectDAO.setInvestment(investment);
            }
            if(investment != null) {
                updatedInvestments.add(updatedInvestment);
            }
        }
        return updatedInvestments;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<FundingInvestment> setProjectInvestments(List<FundingInvestment> investments, Long projectID) {
        
    	List<FundingInvestment> updatedInvestments = new ArrayList<FundingInvestment>();
    	// https://svn.sourcen.com/issues/19580 - Above market project contacts should not be copied to other End Markets
    	if(!(SynchroPermHelper.isAboveMarketProjectContact(projectID) || SynchroPermHelper.isRegionalProjectContact(projectID) || SynchroPermHelper.isAreaProjectContact(projectID)))
    	{
    		for(FundingInvestment investment: investments)
	        {
    			//https://svn.sourcen.com/issues/19604 - To avoid adding multiple entries while saving project as draft
    			if(investment.getInvestmentID()!=null && investment.getInvestmentID()>0)
    			{
    				projectDAO.deleteInvestment(investment.getInvestmentID());
    			}
    			else
    			{
    				projectDAO.deleteInvestment(projectID,investment.getFieldworkMarketID());
    			}
    			
    			FundingInvestment updatedInvestment = null;
	
	            updatedInvestment = projectDAO.setInvestment(investment);
	
	            if(investment != null) {
	                updatedInvestments.add(updatedInvestment);
	            }
	        }
    	}
    	else
    	{
	    	projectDAO.deleteProjectInvestments(projectID);
	        for(FundingInvestment investment: investments)
	        {
	            FundingInvestment updatedInvestment = null;
	//            if(investment.getInvestmentID() != null && investment.getInvestmentID() > 0) {
	//                updatedInvestment = projectDAO.updateInvestment(investment);
	//            } else {
	            updatedInvestment = projectDAO.setInvestment(investment);
	//            }
	            if(investment != null) {
	                updatedInvestments.add(updatedInvestment);
	            }
	        }
    	}
        return updatedInvestments;
    }

//    @Override
//    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
//    public List<ProjectPendingActivityViewBean> getPendingActivities(final Long userID) {
//        return projectDAO.getPendingActivities(userID);
//    }
//
//    @Override
//    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
//    public Long getPendingActivitiesTotalCount(final Long userID) {
//        return projectDAO.getPendingActivitiesTotalCount(userID);
//    }


    @Override
    public List<ProjectPendingActivityViewBean> getPendingActivities(final ProjectResultFilter filter, final Long userID) {
        List<ProjectPendingActivityViewBean> beans = projectDAO.getPendingActivities(filter, userID);
//        for(ProjectPendingActivityViewBean bean: beans) {
//            Project project = get(bean.getProjectID());
//            int currentStageNumber = -1;
//            Integer status;
//            List<EndMarketInvestmentDetail> endMarkets = getEndMarketDetails(project.getProjectID());
//            List<FundingInvestment> fundingInvestments = getProjectInvestments(project.getProjectID());
//
//            if(project != null) {
//                currentStageNumber = 2;
//            }
//
//            // Check if PIB started
//            List<ProjectInitiation> projectInitiations = getPibManager().getPIBDetails(project.getProjectID());
//            if(projectInitiations != null && projectInitiations.size() > 0) {
//                status = projectInitiations.get(0).getStatus();
//                if(status != null && status >= SynchroGlobal.StageStatus.PIB_STARTED.ordinal()
//                        && status <= SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal()) {
//                    currentStageNumber = 2;
//                }
//
//                for(ProjectInitiation projectInitiation:projectInitiations) {
//                    PIBMethodologyWaiver methodologyWaiver = getPibManager().getPIBMethodologyWaiver(projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
//                    if(methodologyWaiver != null
//                            && (methodologyWaiver.getIsApproved() == null || methodologyWaiver.getIsApproved() == 0)) {
//                        if(methodologyWaiver.getMethodologyApprover().equals(userID) && methodologyWaiver.getStatus() == SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal()) {
//                            bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.METHODOLOGY_WAIVER_APPROVAL_PENDING.toString());
//                        } else if(methodologyWaiver.getModifiedBy().equals(userID) && methodologyWaiver.getStatus() == SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal()) {
//                            bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.METHODOLOGY_WAIVER_MORE_INFORMATION_REQUESTED.toString());
//                        }
//                    } else if(projectInitiation.getStatus() == SynchroGlobal.StageStatus.PIB_SAVED.ordinal()) {
//                        if(project.getMultiMarket()) {
//                            if(projectInitiation.getLegalApprovalRcvd() == null
//                                    || projectInitiation.getLegalApprovalRcvd() == false) {
//                                for(FundingInvestment fundingInvestment : fundingInvestments) {
//                                    if((fundingInvestment.getAboveMarket() && project.getProjectOwner().equals(userID))
//                                            || (fundingInvestment.getProjectContact().equals(userID) || fundingInvestment.getSpiContact().equals(userID))) {
//                                        bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.PIB_LEGAL_APPROVAL_PENDING.toString());
//                                    }
//                                }
//                            } else if(project.getProjectOwner().equals(userID)) {
//                                bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.PIB_COMPLETE_NOTIFY_AGENCY_PENDING.toString());
//                            }
//                        } else {
//                            if(endMarkets != null && endMarkets.size() > 0
//                                    && endMarkets.get(0).getSpiContact().equals(userID)) {
//                                if(projectInitiation.getLegalApprovalRcvd() == null
//                                        || projectInitiation.getLegalApprovalRcvd() == false) {
//                                    bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.PIB_LEGAL_APPROVAL_PENDING.toString());
//                                } else {
//                                    bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.PIB_COMPLETE_NOTIFY_AGENCY_PENDING.toString());
//                                }
//                            }
//                        }
//                    } else {
//                        for(FundingInvestment fundingInvestment : fundingInvestments) {
//                            if(fundingInvestment.getEstimatedCost().intValue() > 0
//                                    && (fundingInvestment.getApproved() == null || fundingInvestment.getApproved() == false)
//                                    && (fundingInvestment.getProjectContact().equals(userID) || fundingInvestment.getSpiContact().equals(userID))) {
//                                bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.PIT_APPROVAL_ON_COST_PENDING.toString());
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Check If proposal started
//            List<ProposalInitiation> proposalInitiations = getProposalManager().getProposalDetails(project.getProjectID());
//            if(proposalInitiations != null && proposalInitiations.size() > 0) {
//                status = proposalInitiations.get(0).getStatus();
//                if(status != null && status >= SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal()
//                        && status <= SynchroGlobal.StageStatus.PROPOASL_AWARDED.ordinal()) {
//                    currentStageNumber = 3;
//                    boolean projectAwarded = false;
//                    for(ProposalInitiation proposalInitiation:proposalInitiations) {
//                        if(proposalInitiation.getIsAwarded() != null && proposalInitiation.getIsAwarded()) {
//                            projectAwarded = true;
//                            break;
//                        }
//                    }
//
//                    for(ProposalInitiation proposalInitiation:proposalInitiations) {
//                        if(proposalInitiation.getStatus() == SynchroGlobal.StageStatus.PROPOSAL_SUBMITTED.ordinal()) {
//                            if(proposalInitiation.getIsPropSubmitted() && !projectAwarded) {
//                                if(project.getMultiMarket()) {
//
//                                } else {
//                                    if(proposalInitiation.getIsReqClariModification()
//                                            && proposalInitiation.getIsSendToProjectOwner()
//                                            && proposalInitiation.getSpiContact().equals(userID)) {
//                                        bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.PROPOSAL_CLARIFICATION_REQUESTED.toString());
//                                    }  else {
//                                        bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.PROPOSAL_AWARD_AGENCY_PENDING.toString());
//                                    }
//                                }
//                            }
//                        } else if(proposalInitiation.getStatus() == SynchroGlobal.StageStatus.PROPOSAL_SAVED.ordinal()) {
//
//                        }
//                    }
//                }
//            }
//
//            List<ProjectSpecsInitiation> projectSpecsInitiations = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
//            if(projectSpecsInitiations != null && projectSpecsInitiations.size() > 0
//                    && projectSpecsInitiations.get(0).getStatus() != null
//                    && projectSpecsInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal()
//                    && projectSpecsInitiations.get(0).getStatus() <= SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal()) {
//                currentStageNumber = 4;
//            }
//
//            List<ReportSummaryInitiation> reportSummaryInitiations = getReportSummaryManager().getReportSummaryInitiation(project.getProjectID());
//            if(reportSummaryInitiations != null && reportSummaryInitiations.size() > 0
//                    && reportSummaryInitiations.get(0).getStatus() != null) {
//                if(reportSummaryInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal()
//                        && reportSummaryInitiations.get(0).getStatus() < SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal()) {
//                    currentStageNumber = 5;
//                } else if(reportSummaryInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal()) {
//                    currentStageNumber = 6;
//                }
//            }
//            bean.setActivityLink(ProjectStage.generateURL(project, currentStageNumber));
//        }
        return beans;
    }

    @Override
    public Long getPendingActivitiesTotalCount(final ProjectResultFilter filter, final Long userID) {
        return projectDAO.getPendingActivitiesTotalCount(filter, userID);
    }

    @Override
    public List<FundingInvestment> getProjectInvestments(final Long projectID)
    {
        return projectDAO.getProjectInvestments(projectID);
    }

    @Override
    public List<FundingInvestment> getProjectInvestments(final Long projectID, final Long endMarketId)
    {
        return projectDAO.getProjectInvestments(projectID, endMarketId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteProjectInvestments(final Long projectID)
    {
        projectDAO.deleteProjectInvestments(projectID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateProjectContact(final Long projectContact, final Long projectId, final Long endMarketId)
    {
    	projectDAO.updateProjectContact(projectContact, projectId, endMarketId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateFundingInvSPIContact(final Long projectContact, final Long projectId, final Long endMarketId)
    {
    	projectDAO.updateFundingInvSPIContact(projectContact, projectId, endMarketId);
    }

    @Override
    @Transactional
    public Long getPendingActivityViewCount(final ProjectResultFilter filter, final Long userId) {
        return projectDAO.getPendingActivityViewCount(filter, userId);
    }

    @Override
    public void updatePendingActivityViews(final ProjectResultFilter filter, final Long userId) {
       projectDAO.updatePendingActivityViews(filter, userId);
    }

    public static PIBManager getPibManager() {
        if(pibManager == null) {
            return JiveApplication.getContext().getSpringBean("pibManager");
        }
        return pibManager;

    }

    public ProposalManager getProposalManager() {
        if(proposalManager == null) {
            return JiveApplication.getContext().getSpringBean("proposalManager");
        }
        return proposalManager;
    }

    public static ProjectSpecsManager getProjectSpecsManager() {
        if(projectSpecsManager == null) {
            return JiveApplication.getContext().getSpringBean("projectSpecsManager");
        }
        return projectSpecsManager;

    }
    public static ReportSummaryManager getReportSummaryManager() {
        if(reportSummaryManager == null) {
            return JiveApplication.getContext().getSpringBean("reportSummaryManager");
        }
        return reportSummaryManager;

    }
    public static ProjectEvaluationManager getProjectEvaluationManager() {
        if(projectEvaluationManager == null) {
            return JiveApplication.getContext().getSpringBean("projectEvaluationManager");
        }
        return projectEvaluationManager;
    }

}
