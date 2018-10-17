package com.grail.synchro.manager.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.InvitedUser;
import com.grail.synchro.beans.InvitedUserResultFilter;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectPendingActivityViewBean;
import com.grail.synchro.beans.ProjectTemplate;
import com.grail.synchro.dao.EndMarketDetailDAO;
import com.grail.synchro.dao.ProjectDAONew;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectSpecsManagerNew;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
public class ProjectManagerImplNew implements ProjectManagerNew {

    private static final Logger LOG = Logger.getLogger(ProjectManagerImpl.class);
    private ProjectDAONew projectDAONew;
    private EndMarketDetailDAO endMarketDetailDAO;
    private StageManager stageManager;

    private static ProposalManagerNew proposalManagerNew;
    private static PIBManagerNew pibManagerNew;
    private static ProjectSpecsManagerNew projectSpecsManagerNew;
    private static ReportSummaryManagerNew reportSummaryManagerNew;
    private static ProjectEvaluationManagerNew projectEvaluationManagerNew;

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Project save(Project project) {
    	 LOG.info("Checking Region in Manager Impl initial -->"+project.getRegions());
        if (project.getProjectID() == null) {
            LOG.info("Creating synchro project.");
            LOG.info("Checking Region in Manager Impl-->"+project.getRegions());
            project = projectDAONew.create(project);
        } else {
            LOG.info("Updating synchro project.");
            project = projectDAONew.update(project);
        }
        return project;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Project updateProjectMigrate(Project project) {
    	 LOG.info("Checking Region in Manager Impl initial -->"+project.getRegions());
        if (project.getProjectID() == null) {
            LOG.info("Creating synchro project.");
            LOG.info("Checking Region in Manager Impl-->"+project.getRegions());
            project = projectDAONew.create(project);
        } else {
            LOG.info("Updating synchro project.");
            project = projectDAONew.updateProjectMigrate(project);
        }
        return project;
    }
   

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void delete(final Long projectID) {
        projectDAONew.delete(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateOwner(final Long projectID, final Long ownerID) {
        projectDAONew.updateOwner(projectID, ownerID);
    }

    @Override
    public Project get(final Long projectID) {
        final Project project = projectDAONew.get(projectID);
        return project;
    }


    @Override
    public List<Project> getAll() {
        return projectDAONew.getAll();
    }
    
    @Override
    public List<Project> getAllNew()
    {
    	return projectDAONew.getAllNew();
    }
    
    /**
     * Get All projects based on the result filter
     */
    @Override
    public List<Project> getProjects(ProjectResultFilter projectResultFilter) {
        return projectDAONew.getProjects(projectResultFilter);
    }



    /**
     * This will fetch all the projects for Reporting section
     * Projects with 1)	DRAFT, 2)DELETED 3)	CANCEL
     * Status will not be fetched.
     */
    @Override
    public List<Project> getReportProjects()
    {
        return projectDAONew.getReportProjects();
    }

    @Override
    public List<Project> getReportProjects(ProjectResultFilter projectResultFilter)
    {
        return projectDAONew.getReportProjects(projectResultFilter);
    }

    @Override
    public List<Project> getProjectsByUser(final Long userID) {
        return projectDAONew.getProjectsByUser(userID);
    }

    @Override
    public List<Project> getProjectsByUserAndResultFilter(final Long userID, final ProjectResultFilter projectResultFilter) {
        return projectDAONew.getProjectsByUserAndResultFilter(userID, projectResultFilter);
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void saveExchangeRate(final Integer currencyID) {
        projectDAONew.saveExchangeRate(currencyID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateExchangeRate(final Integer currencyID) {
        projectDAONew.updateExchangeRate(currencyID);
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
            template = projectDAONew.createTemplate(template);
        } else {
            LOG.info("Updating synchro project.");
            template = projectDAONew.update(template);
        }
        return template;
    }

    @Override
    public ProjectTemplate getTemplate(final Long templateID) {
        final ProjectTemplate template = projectDAONew.getTemplate(templateID);
        //TODO Get End Markets
        return template;
    }

    @Override
    public List<ProjectTemplate> getAllTemplates() {
        return projectDAONew.getAllTemplates();
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
    public void updateReferenceEndMarkets(final EndMarketInvestmentDetail endMarketInvestmentDetail) {
        endMarketDetailDAO.updateReferenceEndMarkets(endMarketInvestmentDetail);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateFundingEndMarkets(final EndMarketInvestmentDetail endMarketInvestmentDetail)
    {
    	endMarketDetailDAO.updateFundingEndMarkets(endMarketInvestmentDetail);
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
        return projectDAONew.getTotalCount(filter);
    }
    
    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getProjectDashboardCount(final ProjectResultFilter filter)
    {
    	return projectDAONew.getProjectDashboardCount(filter);
    }
    /**
     * This method will update the PIT Changes from the PIB Window
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updatePIT(Project project, EndMarketInvestmentDetail endMarketDetail)
    {
        projectDAONew.updatePIT(project);
       /* if(endMarketDetail!=null)
        {
            endMarketDetailDAO.updateInitialCost(endMarketDetail);
        }*/
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
        return projectDAONew.addInvite(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long addInvite(final String email, final Long invitedBy, final Date invitedDate) {
        return  projectDAONew.addInvite(email, invitedBy, invitedDate);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long addInvite(final String email, final User invitedBy, final Date invitedDate) {
        return projectDAONew.addInvite(email, invitedBy.getID(), invitedDate);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInvitedUser(final String email) {
        return projectDAONew.getInvitedUser(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInvitedUser(String email, User user) {
        return projectDAONew.getInvitedUser(email, user);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<InvitedUser> getInvitedUsers(User user) {
        return projectDAONew.getInvitedUsers(user);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<InvitedUser> getInvitedUsers() {
        return projectDAONew.getInvitedUsers();
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<InvitedUser> getInvitedUsers(final InvitedUserResultFilter filter) {
        return projectDAONew.getInvitedUsers(filter);
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInvitedUsersTotalCount(final InvitedUserResultFilter filter) {
        return projectDAONew.getInvitedUsersTotalCount(filter);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void  removeInvite(final String email)
    {
        projectDAONew.removeInvite(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getInviteIdByEmail(final String email)
    {
        return projectDAONew.getInviteIdByEmail(email);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void replaceInviteReferences(final Long referenceID, final Long id)
    {
        projectDAONew.replaceInviteReferences(referenceID, id);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Integer setStatusOnDelete(final Long projectID, final Integer status)
    {
        return projectDAONew.setStatusOnDelete(projectID, status);
    }

    @Override
    public Integer getStatusOnDelete(final Long projectID)
    {
        return projectDAONew.getStatusOnDelete(projectID);
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
        projectDAONew.updateProjectStatus(projectId, status);
    }

    /**
     * Method to update the project New Synchro Flag
     * @param projectId
     * @param status
     */
    @Override
    public void updateProjectNewSynchroFlag(final Long projectId, final Integer newSynchro)
    {
    	projectDAONew.updateProjectNewSynchroFlag(projectId, newSynchro);
    }
    
    /**
     * Method to Cancel the project
     * @param projectId
     */
    @Override
    public void updateCancelProject(final Long projectId, final Integer status)
    {
        projectDAONew.updateCancelProject(projectId, status);
    }
    
    public void setProjectDAONew(final ProjectDAONew projectDAONew) {
        this.projectDAONew = projectDAONew;
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
        return projectDAONew.setStatusTrack(projectID, status);
    }

    @Override
    public Integer getStatusTrack(final Long projectID)
    {
        return projectDAONew.getStatusTrack(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteStatusTrack(final Long projectID)
    {
        projectDAONew.deleteStatusTrack(projectID);
    }

    /**
     * Investment and Funding APIs
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public FundingInvestment setInvestment(final FundingInvestment investment)
    {
        return projectDAONew.setInvestment(investment);
    }

    @Override
    public FundingInvestment getInvestment(final Long investmentID)
    {
        return projectDAONew.getInvestment(investmentID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteInvestment(final Long investmentID)
    {
        projectDAONew.deleteInvestment(investmentID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteInvestment(final Long projectID,final Long endMarketId)
    {
    	projectDAONew.deleteInvestment(projectID,endMarketId);
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
                updatedInvestment = projectDAONew.updateInvestment(investment);
            } else {
                updatedInvestment = projectDAONew.setInvestment(investment);
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
    				projectDAONew.deleteInvestment(investment.getInvestmentID());
    			}
    			else
    			{
    				projectDAONew.deleteInvestment(projectID,investment.getFieldworkMarketID());
    			}
    			
    			FundingInvestment updatedInvestment = null;
	
	            updatedInvestment = projectDAONew.setInvestment(investment);
	
	            if(investment != null) {
	                updatedInvestments.add(updatedInvestment);
	            }
	        }
    	}
    	else
    	{
	    	projectDAONew.deleteProjectInvestments(projectID);
	        for(FundingInvestment investment: investments)
	        {
	            FundingInvestment updatedInvestment = null;
	//            if(investment.getInvestmentID() != null && investment.getInvestmentID() > 0) {
	//                updatedInvestment = projectDAO.updateInvestment(investment);
	//            } else {
	            updatedInvestment = projectDAONew.setInvestment(investment);
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
        List<ProjectPendingActivityViewBean> beans = projectDAONew.getPendingActivities(filter, userID);

    	/* List<ProjectPendingActivityViewBean> beans = new ArrayList<ProjectPendingActivityViewBean>();
    	 
        List<ProjectInitiation> piList =  getPibManagerNew().getProjectInitiationPendingActivities(filter);
        if(piList!=null && piList.size()>0)
        {
        	for(ProjectInitiation pi : piList)
        	{
        		ProjectPendingActivityViewBean bean = new ProjectPendingActivityViewBean();
        		bean.setProjectID(pi.getProjectID());
        		Project project = projectDAONew.get(pi.getProjectID());
        		bean.setBrand(project.getBrand()+"");
        		bean.setCountry("India");
        		bean.setOwner(project.getProjectOwner()+"");
        		bean.setActivityLink("/synchro//pib-details!input.jspa?projectID="+pi.getProjectID());
        		bean.setPendingActivity("Brief Review");
        		bean.setProjectName(project.getName());
        		bean.setStartYear("2010");
        		
        		beans.add(bean);
        	}
        }
        List<ProposalInitiation> proposalList =  getProposalManagerNew().getProposalPendingActivities(filter);
        if(proposalList!=null && proposalList.size()>0)
        {
        	for(ProposalInitiation pi : proposalList)
        	{
        		ProjectPendingActivityViewBean bean = new ProjectPendingActivityViewBean();
        		bean.setProjectID(pi.getProjectID());
        		Project project = projectDAONew.get(pi.getProjectID());
        		bean.setBrand(project.getBrand()+"");
        		bean.setCountry("India");
        		bean.setOwner(project.getProjectOwner()+"");
        		bean.setActivityLink("/synchro/propsoal-details!input.jspa?projectID="+pi.getProjectID());
        		bean.setProjectName(project.getName());
        		bean.setStartYear("2010");
        		bean.setPendingActivity("Proposal Review");
        		
        		beans.add(bean);
        	}
        }*/

        return beans;
    }

    @Override
    public Long getPendingActivitiesTotalCount(final ProjectResultFilter filter, final Long userID) {
        return projectDAONew.getPendingActivitiesTotalCount(filter, userID);
    }

    @Override
    public List<FundingInvestment> getProjectInvestments(final Long projectID)
    {
        return projectDAONew.getProjectInvestments(projectID);
    }

    @Override
    public List<FundingInvestment> getProjectInvestments(final Long projectID, final Long endMarketId)
    {
        return projectDAONew.getProjectInvestments(projectID, endMarketId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteProjectInvestments(final Long projectID)
    {
        projectDAONew.deleteProjectInvestments(projectID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateProjectContact(final Long projectContact, final Long projectId, final Long endMarketId)
    {
    	projectDAONew.updateProjectContact(projectContact, projectId, endMarketId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateFundingInvSPIContact(final Long projectContact, final Long projectId, final Long endMarketId)
    {
    	projectDAONew.updateFundingInvSPIContact(projectContact, projectId, endMarketId);
    }

    @Override
    @Transactional
    public Long getPendingActivityViewCount(final ProjectResultFilter filter, final Long userId) {
        return projectDAONew.getPendingActivityViewCount(filter, userId);
    }

    @Override
    public void updatePendingActivityViews(final ProjectResultFilter filter, final Long userId) {
       projectDAONew.updatePendingActivityViews(filter, userId);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void saveProjectCoseDetails(List<ProjectCostDetailsBean> projectCostDetails, BigDecimal totalCost) 
    {
    	 
        totalCost = new BigDecimal("0");
        Long projectId = new Long("0");
    	if (projectCostDetails != null && projectCostDetails.size()>0) 
        {
        	for(ProjectCostDetailsBean pcb: projectCostDetails)
        	{
        		projectDAONew.saveProjectCostDetails(pcb);
        		projectId = pcb.getProjectId();
        		// Commenting this as this will be calculated from the Front End
        		if(pcb.getCostCurrency()!=null && pcb.getEstimatedCost()!=null)
        		{
	        		//BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(pcb.getCostCurrency())) * (pcb.getEstimatedCost().doubleValue()));
        			BigDecimal gbpEstimatedCost = SynchroUtils.getCurrencyExchangeRateBD(pcb.getCostCurrency()).multiply(pcb.getEstimatedCost());
	        		totalCost = totalCost.add(gbpEstimatedCost);
	        		projectId = pcb.getProjectId();
        		}
        	}
        }
        //Calculate Total Cost
    	int defaultCurrency = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
    	
    	// This is done for rounding off to 3 decimal places as per http://redmine.nvish.com/redmine/issues/95
    	// This is done for rounding off to 2 decimal places as per http://redmine.nvish.com/redmine/issues/188
    	// This is commented as Total cost will be stored as it is and on UI it will be shown as rounded figure
    	//totalCost = totalCost.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    	
    	projectDAONew.updateProjectTotalCost(projectId, totalCost, defaultCurrency);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void saveProjectCostDetails(List<ProjectCostDetailsBean> projectCostDetails) 
    {
    	if (projectCostDetails != null && projectCostDetails.size()>0) 
        {
        	for(ProjectCostDetailsBean pcb: projectCostDetails)
        	{
        		projectDAONew.saveProjectCostDetails(pcb);
        	}
        }
     
    }
    
    @Override
    public Map<Integer, BigDecimal> getTotalCosts(List<ProjectCostDetailsBean> projectCostDetails, BigDecimal totalCostGBP)
    {
    	Map<Integer, BigDecimal> totalCosts = new HashMap<Integer, BigDecimal>();
    	for(ProjectCostDetailsBean pcb: projectCostDetails)
     	{
    		Double exchangeRate = 1/SynchroUtils.getCurrencyExchangeRate(pcb.getCostCurrency());
    		totalCosts.put(pcb.getCostCurrency(), (BigDecimal.valueOf(totalCostGBP.doubleValue() * exchangeRate)));
     	}
    	
    	return totalCosts;
    }
    
    @Override
    public List<ProjectCostDetailsBean> getProjectCostDetails(final Long projectId){
    	return projectDAONew.getProjectCostDetails(projectId);
    }
    
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void deleteProjectCostDetails(final Long projectID) {
        projectDAONew.deleteProjectCostDetails(projectID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateProjectMW(Project project)
    {
        projectDAONew.updateProjectMW(project);
    
    }
    
    /**
     * This method will update the PIT Changes from the PIB Window
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateProjectNew(Project project)
    {
        projectDAONew.updateProjectNew(project);
    
    }
    
    @Override
    public List<Long> getAllProjects(final Long budgetYear, Long budgetLocation)
    {
    	return projectDAONew.getAllProjects(budgetYear, budgetLocation);
    }
    
    public static PIBManagerNew getPibManagerNew() {
        if(pibManagerNew == null) {
            return JiveApplication.getContext().getSpringBean("pibManagerNew");
        }
        return pibManagerNew;

    }

    public ProposalManagerNew getProposalManagerNew() {
        if(proposalManagerNew == null) {
            return JiveApplication.getContext().getSpringBean("proposalManagerNew");
        }
        return proposalManagerNew;
    }

    public static ProjectSpecsManagerNew getProjectSpecsManager() {
        if(projectSpecsManagerNew == null) {
            return JiveApplication.getContext().getSpringBean("projectSpecsManagerNew");
        }
        return projectSpecsManagerNew;

    }
    public static ReportSummaryManagerNew getReportSummaryManagerNew() {
        if(reportSummaryManagerNew == null) {
            return JiveApplication.getContext().getSpringBean("reportSummaryManagerNew");
        }
        return reportSummaryManagerNew;

    }
    public static ProjectEvaluationManagerNew getProjectEvaluationManagerNew() {
        if(projectEvaluationManagerNew == null) {
            return JiveApplication.getContext().getSpringBean("projectEvaluationManagerNew");
        }
        return projectEvaluationManagerNew;
    }
    
    @Override
    public List<String> getSynchroUserNames()
    {
    	  return projectDAONew.getSynchroUserNames();
    }

}
