package com.grail.synchro.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ProjectStage.StageType;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectCurrentStatusManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.manager.StageManager;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class CurrentStatusViewBean implements Comparable<CurrentStatusViewBean> {
	private Long projectID;
	private String projectName;
    private String owner; 
    private String startYear;
    private String country;
    private String brand;   
    private String status;
    private String pendingActivity;
    private String pendingActionsLink;
    private String personResponsible;
    private static UserManager userManager;
    private static StageManager stageManager;
    
    private static ProjectSpecsManager projectSpecsManager;
    private static PIBManager pibManager;
    private static ProposalManager proposalManager;
    private static ProjectManager synchroProjectManager;
    private static ProjectCurrentStatusManager projectCurrentStatusManger;
    private static ReportSummaryManager reportSummaryManager;
    
    private Boolean multimarket = false;
    private String endMarketName;
    private List<EndMarketDashboardViewBean> endMarkets;

	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getPendingActivity() {
		return pendingActivity;
	}
	public void setPendingActivity(String pendingActivity) {
		this.pendingActivity = pendingActivity;
	}
	
	public String getStatus() {
		return status;
	}

	public String getStartYear() {
		return startYear;
	}
	public void setStartYear(String startYear) {
		this.startYear = startYear;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public void setStatus(String status) {
		this.status = status;
	}

    

    public static UserManager getUserManager() {
        if(userManager == null){
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }
    public static StageManager getStageManager() {
        if(stageManager == null){
        	stageManager = JiveApplication.getContext().getSpringBean("stageManager");
        }
        return stageManager;
    }
    

    public static ProjectSpecsManager getProjectSpecsManager() {
        if(projectSpecsManager == null){
            projectSpecsManager = JiveApplication.getContext().getSpringBean("projectSpecsManager");
        }
        return projectSpecsManager;
    }

    public static PIBManager getPIBManager() {
        if(pibManager == null){
        	pibManager = JiveApplication.getContext().getSpringBean("pibManager");
        }
        return pibManager;
    }
    
    public static ProposalManager getProposalManager() {
        if(proposalManager == null){
        	proposalManager = JiveApplication.getContext().getSpringBean("proposalManager");
        }
        return proposalManager;
    }

    public static ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null){
            synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;
    }
    
    public static ProjectCurrentStatusManager getProjectCurrentStatusManager() {
        if(projectCurrentStatusManger == null){
        	projectCurrentStatusManger = JiveApplication.getContext().getSpringBean("projectCurrentStatusManger");
        }
        return projectCurrentStatusManger;
    }
    
    public static ReportSummaryManager getReportSummaryManager() {
        if(reportSummaryManager == null){
        	reportSummaryManager = JiveApplication.getContext().getSpringBean("reportSummaryManager");
        }
        return reportSummaryManager;
    }
	/*
     * Comparator implementation to Sort Order object based on Project ID
     */
    public static class OrderByID implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o1.projectID > o2.projectID ? 1 : (o1.projectID < o2.projectID ? -1 : 0);
        }
    }
    
    public static class OrderByIDDesc implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o2.projectID > o1.projectID ? 1 : (o2.projectID < o1.projectID ? -1 : 0);
        }
    }

    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project name.
     */
    public static class OrderByName implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o1.projectName.toLowerCase().compareTo(o2.projectName.toLowerCase());
        }
    }
    
    public static class OrderByNameDesc implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o2.projectName.toLowerCase().compareTo(o1.projectName.toLowerCase());
        }
    }
    
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project owner.
     */
    public static class OrderByOwner implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o1.owner.toLowerCase().compareTo(o2.owner.toLowerCase());
        }
    }
    
    public static class OrderByOwnerDesc implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o2.owner.toLowerCase().compareTo(o1.owner.toLowerCase());
        }
    }
    
    /*
    * Comparator implementation to Sort Order object based on Project Year
    */
    
    public static class OrderByYear implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o1.startYear.compareTo(o2.startYear);
        }
    }
    
    public static class OrderByYearDesc implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o2.startYear.compareTo(o1.startYear);
        }
    }


    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project owner.
     */
    public static class OrderByBrand implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o1.brand.toLowerCase().compareTo(o2.brand.toLowerCase());
        }
    }
    
    public static class OrderByBrandDesc implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o2.brand.toLowerCase().compareTo(o1.brand.toLowerCase());
        }
    }
    
    
   
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project status.
     */
    public static class OrderByActivity implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o1.pendingActivity.toLowerCase().compareTo(o2.pendingActivity.toLowerCase());
        }
    }
    
    public static class OrderByActivityDesc implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o2.pendingActivity.toLowerCase().compareTo(o1.pendingActivity.toLowerCase());
        }
    }
    
    public static class OrderByPersonResponsible implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o1.personResponsible.toLowerCase().compareTo(o2.personResponsible.toLowerCase());
        }
    }
    
    public static class OrderByPersonResponsibleDesc implements Comparator<CurrentStatusViewBean> {

        @Override
        public int compare(CurrentStatusViewBean o1, CurrentStatusViewBean o2) {
            return o2.personResponsible.toLowerCase().compareTo(o1.personResponsible.toLowerCase());
        }
    }
    
    
	@Override
	public int compareTo(CurrentStatusViewBean o) {
		return this.projectID > o.projectID ? 1 : (this.projectID < o.projectID ? -1 : 0);
	}
	
	
	public static CurrentStatusViewBean toCurrentStatusViewBean(final Project project){

		CurrentStatusViewBean bean = new CurrentStatusViewBean();
        bean.setProjectID(project.getProjectID());
        bean.setProjectName(project.getName());
        bean.setMultimarket(project.getMultiMarket());
       
        
      

        try {
            bean.setOwner(getUserManager().getUser(project.getProjectOwner()).getName());
        } catch (UserNotFoundException e) {
            bean.setOwner("");
        }


        List<EndMarketDashboardViewBean> endMarkets = new ArrayList<EndMarketDashboardViewBean>();
        List<Long> endMarketIDs = getSynchroProjectManager().getEndMarketIDs(project.getProjectID());
        if(endMarketIDs != null && endMarketIDs.size() > 0) {
            for(Long endMarketID : endMarketIDs) {
                EndMarketDashboardViewBean endMarketDashboardViewBean = new EndMarketDashboardViewBean();
                endMarketDashboardViewBean.setId(endMarketID);
                endMarketDashboardViewBean.setName(SynchroGlobal.getEndMarkets().get(endMarketID.intValue()));
                endMarketDashboardViewBean.setProjectId(project.getProjectID());
                ProjectSpecsEndMarketDetails projectSpecsEndMarketDetails = getProjectSpecsManager().getProjectSpecsEMDetails(project.getProjectID(), endMarketID);
                if(projectSpecsEndMarketDetails != null) {
                    if(projectSpecsEndMarketDetails.getFwStartDate() != null) {
                        Calendar emStartDate = Calendar.getInstance();
                        emStartDate.setTime(projectSpecsEndMarketDetails.getFwStartDate());
                        endMarketDashboardViewBean.setStartDate(emStartDate.getTime());
                        endMarketDashboardViewBean.setStartYear(emStartDate.get(Calendar.YEAR));
                        endMarketDashboardViewBean.setStartYear(emStartDate.get(Calendar.MONTH));
                    }
                    if(projectSpecsEndMarketDetails.getFwEndDate() != null) {
                        Calendar emEndDate = Calendar.getInstance();
                        emEndDate.setTime(projectSpecsEndMarketDetails.getFwEndDate());
                        endMarketDashboardViewBean.setEndDate(emEndDate.getTime());
                        endMarketDashboardViewBean.setEndYear(emEndDate.get(Calendar.YEAR));
                        endMarketDashboardViewBean.setEndMonth(emEndDate.get(Calendar.MONTH));
                    }
                }
                endMarkets.add(endMarketDashboardViewBean);
            }
        }
        bean.setEndMarkets(endMarkets);
      
        if(!project.getMultiMarket())
        {
	        bean.setPendingActionsLink("/synchro/current-status!input.jspa?projectID="+project.getProjectID());
        	Integer stageNumber = ProjectStage.getCurrentStageNumber(project);
        	
        
        	//https://svn.sourcen.com/issues/19869
        	if(stageNumber.equals(StageType.PIT.getStageNumber()) || project.getStatus()==SynchroGlobal.Status.DRAFT.ordinal())
        	{
        		String pitStageUrl = "/synchro/create-project!input.jspa?projectID=" + project.getProjectID();
        		List<ProjectCurrentStatus> pitCurrentStatusList = getProjectCurrentStatusManager().getPITStatusList(project.getProjectID(), pitStageUrl);
        		
        		
        		if(pitCurrentStatusList!=null && pitCurrentStatusList.size()>0)
        		{
        			for(ProjectCurrentStatus pcs : pitCurrentStatusList)
        			{
        				bean.setPendingActivity("Confirm PIT");
        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				try
        				{
        					bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(pcs.getEndMarketID()+"")));
        				}
        				catch(Exception e)
        				{
        					bean.setEndMarketName("");
        				}
        			}
        		}
        		//19925
        		bean.setPendingActionsLink(pitStageUrl);
        	}
        	
        	if(stageNumber.equals(StageType.PIB.getStageNumber()))
	        {
	        	List<ProjectInitiation> pibInitiation = getPIBManager().getPIBDetails(project.getProjectID());
	        	if(pibInitiation!=null && pibInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(pibInitiation.get(0).getEndMarketID()+"")));
	        		String pibStageUrl = "/synchro/pib-details!input.jspa?projectID=" + project.getProjectID();
	        		PIBMethodologyWaiver pibMethodologyWaiver = getPIBManager().getPIBMethodologyWaiver(project.getProjectID(), endMarketIDs.get(0));
	        		PIBMethodologyWaiver pibKantarMethodologyWaiver = getPIBManager().getPIBKantarMethodologyWaiver(project.getProjectID(), endMarketIDs.get(0));
	        		List<ProjectCurrentStatus> pibCurrentStatusList = getProjectCurrentStatusManager().getPIBStatusList(project.getProjectID(), pibStageUrl,endMarketIDs,pibMethodologyWaiver, pibKantarMethodologyWaiver, pibInitiation );
	        		if(pibCurrentStatusList!=null && pibCurrentStatusList.size()>0)
	        		{
	        			//https://svn.sourcen.com/issues/19873
	        			for(ProjectCurrentStatus pcs : pibCurrentStatusList)
	        			{
	        				if(pcs.getActivityDesc().equalsIgnoreCase("Project Brief") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
		        				bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Legal Approval") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Brief Approval and Agency intimation") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}	        				
	        			}
	        			
	        		}
	        		
	        	}
	        	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
        		else
        		{
        			if(StringUtils.isEmpty(bean.getPendingActivity()))
        			{
	        			//bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(pibInitiation.get(0).getEndMarketID()+"")));
		        		String pibStageUrl = "/synchro/pib-details!input.jspa?projectID=" + project.getProjectID();
		        		PIBMethodologyWaiver pibMethodologyWaiver = getPIBManager().getPIBMethodologyWaiver(project.getProjectID(), endMarketIDs.get(0));
		        		PIBMethodologyWaiver pibKantarMethodologyWaiver = getPIBManager().getPIBKantarMethodologyWaiver(project.getProjectID(), endMarketIDs.get(0));
		        		List<ProjectCurrentStatus> pibCurrentStatusList = getProjectCurrentStatusManager().getPIBStatusList(project.getProjectID(), pibStageUrl,endMarketIDs,pibMethodologyWaiver, pibKantarMethodologyWaiver, pibInitiation );
		        		if(pibCurrentStatusList!=null && pibCurrentStatusList.size()>0)
		        		{
		        			//https://svn.sourcen.com/issues/19873
		        			for(ProjectCurrentStatus pcs : pibCurrentStatusList)
		        			{
		        				if(pcs.getActivityDesc().equalsIgnoreCase("Project Brief") && pcs.getStatus().equalsIgnoreCase("Pending"))
		        				{
			        				bean.setPendingActivity(pcs.getActivityDesc());
			        				bean.setPersonResponsible(pcs.getPersonResponsible());
		        				}
		        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Legal Approval") && pcs.getStatus().equalsIgnoreCase("Pending"))
		        				{
		        					bean.setPendingActivity(pcs.getActivityDesc());
			        				bean.setPersonResponsible(pcs.getPersonResponsible());
		        				}
		        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Brief Approval and Agency intimation") && pcs.getStatus().equalsIgnoreCase("Pending"))
		        				{
		        					bean.setPendingActivity(pcs.getActivityDesc());
			        				bean.setPersonResponsible(pcs.getPersonResponsible());
		        				}	        				
		        			}
		        			
		        		}
        			}
        		}
	        }
	        if(stageNumber.equals(StageType.PROPOSAL.getStageNumber()))
	        {
	        	List<ProposalInitiation> proposalInitiation = getProposalManager().getProposalDetails(project.getProjectID());
	        	if(proposalInitiation!=null && proposalInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(proposalInitiation.get(0).getEndMarketID()+"")));
	        		
	        		String proposalStageUrl = "/synchro/proposal-details!input.jspa?projectID=" + project.getProjectID();
	        		List<ProjectCurrentStatus> proposalCurrentStatusList = getProjectCurrentStatusManager().getProposalStatusList(proposalInitiation,project.getProjectID(), proposalStageUrl);
	                if(proposalCurrentStatusList!=null && proposalCurrentStatusList.size()>0)
	        		{
	        			for(ProjectCurrentStatus pcs : proposalCurrentStatusList)
	        			{
	        				/*bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());*/
	        				
	        				if(pcs.getActivityDesc().equalsIgnoreCase("Project Proposal") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
		        				bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Proposal Submission") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Proposal Revision and Update to BAT") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Proposal Award") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				
	        			}
	        			
	        			
	        		}
	        	}
	        }
	        if(stageNumber.equals(StageType.PROJECT_SPECS.getStageNumber()))
	        {
	        	List<ProjectSpecsInitiation> projectSpecsInitiation = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
	        	if(projectSpecsInitiation!=null && projectSpecsInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(projectSpecsInitiation.get(0).getEndMarketID()+"")));
	        		
	        		String projectSpecsStageUrl = "/synchro/project-specs!input.jspa?projectID=" + project.getProjectID();
	        		List<ProjectCurrentStatus> projectSpecsCurrentStatusList = getProjectCurrentStatusManager().getProjectSpecsStatusList(projectSpecsInitiation,project.getProjectID(), projectSpecsStageUrl);
	                if(projectSpecsCurrentStatusList!=null && projectSpecsCurrentStatusList.size()>0)
	        		{
	        			for(ProjectCurrentStatus pcs : projectSpecsCurrentStatusList)
	        			{
	        				/*bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());*/
	        				if(pcs.getActivityDesc().equalsIgnoreCase("Project Specs") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
		        				bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Legal Approval") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Project Specs Revision and Update to BAT") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Project Specs Approval") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        			}
	        		}
	        	}
	        }
	        if(stageNumber.equals(StageType.REPORT.getStageNumber()))
	        {
	        	List<ProjectSpecsInitiation> projectSpecsInitiation = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
	        	if(projectSpecsInitiation!=null && projectSpecsInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(projectSpecsInitiation.get(0).getEndMarketID()+"")));
	        		
	        		String reportSummaryStageUrl = "/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
	        		List<ReportSummaryInitiation> reportSummaryInitiationList = getReportSummaryManager().getReportSummaryInitiation(project.getProjectID());
	        		List<ProjectCurrentStatus> reportCurrentStatusList = getProjectCurrentStatusManager().getReportSummaryStatusList(reportSummaryInitiationList,project.getProjectID(), reportSummaryStageUrl);
	                if(reportCurrentStatusList!=null && reportCurrentStatusList.size()>0)
	        		{
	        			for(ProjectCurrentStatus pcs : reportCurrentStatusList)
	        			{
	        			/*	bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());*/
	        				if(pcs.getActivityDesc().equalsIgnoreCase("Reports") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
		        				bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Reports Revision and Update to BAT") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Legal Approval") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity())  && pcs.getActivityDesc().equalsIgnoreCase("Reports Approval") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Reports for IRIS Upload") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Reports for C-PSI Upload") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				
	        			}
	        		}
	        	}
	        }
        }
        else
        {
        	bean.setPendingActionsLink("/synchro/current-status-multi!input.jspa?projectID="+project.getProjectID());
        	bean.setEndMarketName("Multi Market");
        	Integer stageNumber = ProjectStage.getCurrentStageNumber(project);
        	
        	//https://svn.sourcen.com/issues/19869
        	if(stageNumber.equals(StageType.PIT.getStageNumber()) || project.getStatus()==SynchroGlobal.Status.DRAFT.ordinal())
        	{
        		String pitStageUrl = "/synchro/create-multimarket-project!input.jspa?projectID=" + project.getProjectID();
        		List<ProjectCurrentStatus> pitCurrentStatusList = getProjectCurrentStatusManager().getPITStatusList(project.getProjectID(), pitStageUrl);
        		if(pitCurrentStatusList!=null && pitCurrentStatusList.size()>0)
        		{
        			for(ProjectCurrentStatus pcs : pitCurrentStatusList)
        			{
        				bean.setPendingActivity("Confirm PIT");
        				bean.setPersonResponsible(pcs.getPersonResponsible());
           			}
        		}
        		//19925
        		bean.setPendingActionsLink(pitStageUrl);
        	}
        	
        	
        	if(stageNumber.equals(StageType.PIB.getStageNumber()))
	        {
        		String pibStageUrl = "/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
        		PIBMethodologyWaiver pibMethodologyWaiver = getPIBManager().getPIBMethodologyWaiver(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        		PIBMethodologyWaiver pibKantarMethodologyWaiver = getPIBManager().getPIBKantarMethodologyWaiver(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        		List<FundingInvestment> fundingInvestments = getSynchroProjectManager().getProjectInvestments(project.getProjectID());
        		List<ProjectCurrentStatus> pibCurrentStatusList = getProjectCurrentStatusManager().getPIBMultiStatusList(project.getProjectID(), pibStageUrl,endMarketIDs,pibMethodologyWaiver, pibKantarMethodologyWaiver, fundingInvestments );
        		if(pibCurrentStatusList!=null && pibCurrentStatusList.size()>0)
        		{
        			for(ProjectCurrentStatus pcs : pibCurrentStatusList)
        			{
        			/*	bean.setPendingActivity(pcs.getActivityDesc());
        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				*/
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Project Brief -") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
	        				bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Legal Approval") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Brief Approval and Agency intimation") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}	
        			}
        			
        		}
	        	
	        }
	        if(stageNumber.equals(StageType.PROPOSAL.getStageNumber()))
	        {
	        	List<ProposalInitiation> proposalInitiation = getProposalManager().getProposalDetails(project.getProjectID());
	        	if(proposalInitiation!=null && proposalInitiation.size()>0)
	        	{
	        		String proposalStageUrl = "/synchro/proposal-multi-details!input.jspa?projectID=" + project.getProjectID();
	        		List<FundingInvestment> fundingInvestments = getSynchroProjectManager().getProjectInvestments(project.getProjectID());
	        		List<ProjectCurrentStatus> proposalCurrentStatusList = getProjectCurrentStatusManager().getProposalMultiStatusList(proposalInitiation,project.getProjectID(), proposalStageUrl, fundingInvestments);
	                if(proposalCurrentStatusList!=null && proposalCurrentStatusList.size()>0)
	        		{
	        			for(ProjectCurrentStatus pcs : proposalCurrentStatusList)
	        			{
	        				/*bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());*/
	        				if(pcs.getActivityDesc().equalsIgnoreCase("Project Proposal") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
		        				bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Proposal Submission") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Proposal Revision and Update to BAT") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Proposal Award") && pcs.getStatus().equalsIgnoreCase("Pending"))
	        				{
	        					bean.setPendingActivity(pcs.getActivityDesc());
		        				bean.setPersonResponsible(pcs.getPersonResponsible());
	        				}
	        			}
	        		}
	        	}
	        }
	        if(stageNumber.equals(StageType.PROJECT_SPECS.getStageNumber()))
	        {	        	
        		String projectSpecsStageUrl = "/synchro/project-multi-specs!input.jspa?projectID=" + project.getProjectID();
        		List<FundingInvestment> fundingInvestments = getSynchroProjectManager().getProjectInvestments(project.getProjectID());
        		List<ProjectCurrentStatus> projectSpecsCurrentStatusList = getProjectCurrentStatusManager().getProjectSpecsMultiStatusList(project.getProjectID(), projectSpecsStageUrl, fundingInvestments);
                if(projectSpecsCurrentStatusList!=null && projectSpecsCurrentStatusList.size()>0)
        		{
        			for(ProjectCurrentStatus pcs : projectSpecsCurrentStatusList)
        			{
        			/*	bean.setPendingActivity(pcs.getActivityDesc());
        				bean.setPersonResponsible(pcs.getPersonResponsible());*/
        				
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Project Specs -") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
	        				bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Legal Approval -") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Project Specs Revision and Update to BAT") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Project Specs Approval -") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        			}
        		}
	        	
	        }
	        if(stageNumber.equals(StageType.REPORT.getStageNumber()))
	        {
	        	String reportSummaryStageUrl = "/synchro/report-multi-summary!input.jspa?projectID=" + project.getProjectID();
        		
        		List<FundingInvestment> fundingInvestments = getSynchroProjectManager().getProjectInvestments(project.getProjectID());
        		List<ProjectCurrentStatus> reportCurrentStatusList = getProjectCurrentStatusManager().getReportSummaryMultiStatusList(project.getProjectID(), reportSummaryStageUrl, fundingInvestments);
                if(reportCurrentStatusList!=null && reportCurrentStatusList.size()>0)
        		{
        			for(ProjectCurrentStatus pcs : reportCurrentStatusList)
        			{
        				/*bean.setPendingActivity(pcs.getActivityDesc());
        				bean.setPersonResponsible(pcs.getPersonResponsible());*/
        				
        				//https://svn.sourcen.com/issues/19987
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Reports - Above Market") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
	        				bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Reports Revision and Update to BAT - Above Market") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Legal Approval - Above Market") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().contains("Reports Approval - Above Market") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        				if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Reports for IRIS Upload") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}
        			/*	if(StringUtils.isEmpty(bean.getPendingActivity()) && pcs.getActivityDesc().equalsIgnoreCase("Reports for C-PSI Upload") && pcs.getStatus().equalsIgnoreCase("Pending"))
        				{
        					bean.setPendingActivity(pcs.getActivityDesc());
	        				bean.setPersonResponsible(pcs.getPersonResponsible());
        				}*/
        			}
        		}
	        	
	        }
        }
        
       // bean.setUrl(ProjectStage.generateURL(project, ProjectStage.getCurrentStageNumber(project)));
        return bean;
    }
	public Boolean getMultimarket() {
		return multimarket;
	}
	public void setMultimarket(Boolean multimarket) {
		this.multimarket = multimarket;
	}
	public String getEndMarketName() {
		return endMarketName;
	}
	public void setEndMarketName(String endMarketName) {
		this.endMarketName = endMarketName;
	}
	public List<EndMarketDashboardViewBean> getEndMarkets() {
		return endMarkets;
	}
	public void setEndMarkets(List<EndMarketDashboardViewBean> endMarkets) {
		this.endMarkets = endMarkets;
	}
	public String getPendingActionsLink() {
		return pendingActionsLink;
	}
	public void setPendingActionsLink(String pendingActionsLink) {
		this.pendingActionsLink = pendingActionsLink;
	}
	public String getPersonResponsible() {
		return personResponsible;
	}
	public void setPersonResponsible(String personResponsible) {
		this.personResponsible = personResponsible;
	}
}
