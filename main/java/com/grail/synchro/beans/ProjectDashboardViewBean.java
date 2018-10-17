package com.grail.synchro.beans;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ProjectStage.StageType;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class ProjectDashboardViewBean extends BeanObject {
    private Long projectID;
    private String projectName;
    private String owner;
    private String status;
    private String graphColor;
    private Date startDate;
    private Integer startYear;
    private Integer startMonth;
    private Date endDate;
    private Integer endYear;
    private Integer endMonth;
    private Long projectOwner;
    private String url;
    private String spiContact;
    private List<EndMarketDashboardViewBean> endMarkets;
    private Boolean multimarket = false;
    private static UserManager userManager;
    private static ProjectManager synchroProjectManager;
    private static ProjectSpecsManager projectSpecsManager;
    private static PIBManager pibManager;
    private static ProposalManager proposalManager;
    private Boolean confidential = false;
    private String endMarketName;


    public ProjectDashboardViewBean() {
    }

    public ProjectDashboardViewBean(Long projectID, String projectName, String owner,
                                    String status, String graphColor, Integer startYear,
                                    Integer startMonth, Integer endYear, Integer endMonth, Long projectOwner,
                                    List<EndMarketDashboardViewBean> endMarkets, boolean multiMarket, String url) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.owner = owner;
        this.status = status;
        this.graphColor = graphColor;
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.endYear = endYear;
        this.endMonth = endMonth;
        this.projectOwner = projectOwner;
        this.endMarkets = endMarkets;
        this.multimarket = multiMarket;
        this.url = url;
    }

    public static ProjectDashboardViewBean toProjectDashboardViewBean(final Project project){

        ProjectDashboardViewBean bean = new ProjectDashboardViewBean();
        bean.setProjectID(project.getProjectID());
        bean.setProjectName(project.getName());
        bean.setMultimarket(project.getMultiMarket());
        bean.setProjectOwner(project.getProjectOwner());
        bean.setConfidential(project.getConfidential());

        try {
            bean.setOwner(getUserManager().getUser(project.getProjectOwner()).getName());
        } catch (UserNotFoundException e) {
            bean.setOwner("");
        }


        if(project.getSpiContact() != null && project.getSpiContact().size() > 0) {
            try {
                bean.setSpiContact(getUserManager().getUser(project.getSpiContact().get(0)).getName());
            } catch (UserNotFoundException e) {
                bean.setSpiContact("");
            }
        } else {
            bean.setSpiContact("");
        }

        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTime(project.getStartDate());
        bean.setStartDate(startDateCal.getTime());
        bean.setStartYear(startDateCal.get(Calendar.YEAR));
        bean.setStartMonth(startDateCal.get(Calendar.MONTH));

        Calendar endDateCal = Calendar.getInstance();
        endDateCal.setTime(project.getEndDate());
        bean.setEndDate(endDateCal.getTime());
        bean.setEndYear(endDateCal.get(Calendar.YEAR));
        bean.setEndMonth(endDateCal.get(Calendar.MONTH));



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
        bean.setStatus(SynchroGlobal.Status.getName(project.getStatus().intValue()).toString());
        if(StringUtils.isNotBlank(bean.getStatus())) {
            bean.setGraphColor(SynchroUtils.getGraphBarColor(bean.getStatus()));
        }
        
        // https://svn.sourcen.com/issues/19724
        if(!project.getMultiMarket())
        {
	        Integer stageNumber = ProjectStage.getCurrentStageNumber(project);
	        if(stageNumber.equals(StageType.PIB.getStageNumber()))
	        {
	        	List<ProjectInitiation> pibInitiation = getPIBManager().getPIBDetails(project.getProjectID());
	        	if(pibInitiation!=null && pibInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(pibInitiation.get(0).getEndMarketID()+"")));
	        	}
	        }
	        if(stageNumber.equals(StageType.PROPOSAL.getStageNumber()))
	        {
	        	List<ProposalInitiation> proposalInitiation = getProposalManager().getProposalDetails(project.getProjectID());
	        	if(proposalInitiation!=null && proposalInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(proposalInitiation.get(0).getEndMarketID()+"")));
	        	}
	        }
	        if(stageNumber.equals(StageType.PROJECT_SPECS.getStageNumber()))
	        {
	        	List<ProjectSpecsInitiation> projectSpecsInitiation = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
	        	if(projectSpecsInitiation!=null && projectSpecsInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(projectSpecsInitiation.get(0).getEndMarketID()+"")));
	        	}
	        }
	        if(stageNumber.equals(StageType.REPORT.getStageNumber()))
	        {
	        	List<ProjectSpecsInitiation> projectSpecsInitiation = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
	        	if(projectSpecsInitiation!=null && projectSpecsInitiation.size()>0)
	        	{
	        		bean.setEndMarketName(SynchroGlobal.getEndMarkets().get(Integer.valueOf(projectSpecsInitiation.get(0).getEndMarketID()+"")));
	        	}
	        }
        }
        else
        {
        	bean.setEndMarketName("Multi Market");
        }
        
        if(project.getNewSynchro())
        {
        	bean.setUrl(ProjectStage.generateURLNew(project));
        }
        else
        {
        	bean.setUrl(ProjectStage.generateURL(project, ProjectStage.getCurrentStageNumber(project)));
        }
        return bean;
    }

    public static String generateURL(final Project project) {
        StringBuilder urlBuilder = new StringBuilder();
        return urlBuilder.toString();
    }

    public static UserManager getUserManager() {
        if(userManager == null){
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }

    public static ProjectSpecsManager getProjectSpecsManager() {
        if(projectSpecsManager == null){
            projectSpecsManager = JiveApplication.getContext().getSpringBean("projectSpecsManager");
        }
        return projectSpecsManager;
    }


    public static ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null){
            synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getGraphColor() {
        return graphColor;
    }

    public void setGraphColor(String graphColor) {
        this.graphColor = graphColor;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public Integer getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(Integer endMonth) {
        this.endMonth = endMonth;
    }

    public List<EndMarketDashboardViewBean> getEndMarkets() {
        return endMarkets;
    }

    public void setEndMarkets(List<EndMarketDashboardViewBean> endMarkets) {
        this.endMarkets = endMarkets;
    }

    public Boolean getMultimarket() {
        return multimarket;
    }

    public void setMultimarket(Boolean multimarket) {
        this.multimarket = multimarket;
    }

    public Long getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(Long projectOwner) {
        this.projectOwner = projectOwner;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getConfidential() {
        return confidential;
    }

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
    }

    public String getSpiContact() {
        return spiContact;
    }

    public void setSpiContact(String spiContact) {
        this.spiContact = spiContact;
    }

	public String getEndMarketName() {
		return endMarketName;
	}

	public void setEndMarketName(String endMarketName) {
		this.endMarketName = endMarketName;
	}
}
