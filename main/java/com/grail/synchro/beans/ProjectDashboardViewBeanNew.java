package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class ProjectDashboardViewBeanNew extends BeanObject {
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
    private static ProjectManagerNew synchroProjectManagerNew;
    private static ProjectSpecsManager projectSpecsManager;
    private static PIBManagerNew pibManagerNew;
    private static ProposalManagerNew proposalManagerNew;
    
    private static ReportSummaryManagerNew reportSummaryManagerNew;
    
    private Boolean confidential = false;
    private String endMarketName;
    private Boolean newSynchro = false;
    private String projectManagerName;
    private BigDecimal totalCost;
    private String methodology;
    private String projectStage;
    private String projectTrackStatus;
    
    private String tpdPreviouslySubmitted;
    private Date tpdLastSubmittedDate;
    
    private String tpdLastSubmittedDateString;
    
    private Integer budgetYear;
    private String budgetLocation;
    private String proposalLegalApproval;
    private Date proposalLegalApprovalDate;
    
    private String tpdResearchDoneOn;
    private String tpdProductDescription;
    private String tpdProductionModification;
    private Date tpdProductModificationDate;
    private String tpdTAOCode;
    
    private Date skuSubmissionDate1;
    private String skuDetails1;
    
    private Date tpdSummaryLegalApprovalDate;
    private String tpdSummaryLegalApprover;
    
    private String tpdSummaryAttUploaded;
    
    private String briefUploaded;
    private String briefUploadColor;
    
    private String briefLegalApprovalReceived;
    private String briefLegalApprovalReceivedColor;
    
    private String proposalUploaded;
    private String proposalUploadedColor;
    
    private String proposalLegalApprovalReceived;
    private String proposalLegalApprovalReceivedColor;
    
    private String reportUploaded;
    private String reportUploadedColor;
    
    private String reportLegalApprovalReceived;
    private String reportLegalApprovalReceivedColor;
    
    private String irisSummaryUploaded;
    private String irisSummaryUploadedColor;
    
    private String irisLegalApprovalReceived;
    private String irisLegalApprovalReceivedColor;
    
    private String tpdSummaryUploaded;
    private String tpdSummaryUploadedColor;
    
    private String tpdSummaryLegalApprovalReceived;
    private String tpdSummaryLegalApprovalReceivedColor;
    
    private String reviewDone;
    private String reviewDoneColor;
    
    private List<TPDSKUDetails> tpdSkuDetails;
    

    public ProjectDashboardViewBeanNew() {
    }

    public ProjectDashboardViewBeanNew(Long projectID, String projectName, String owner,
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

    public static ProjectDashboardViewBeanNew toProjectDashboardViewBean(final Project project){

    	ProjectDashboardViewBeanNew bean = new ProjectDashboardViewBeanNew();
        bean.setProjectID(project.getProjectID());
        bean.setProjectName(project.getName());
     //   bean.setMultimarket(project.getMultiMarket());
      //  bean.setProjectOwner(project.getProjectOwner());
      //  bean.setConfidential(project.getConfidential());

        Calendar startDateCal = Calendar.getInstance();
        if(project.getStartDate()!=null)
        {
	        startDateCal.setTime(project.getStartDate());
	        bean.setStartDate(startDateCal.getTime());
	        bean.setStartYear(startDateCal.get(Calendar.YEAR));
	        bean.setStartMonth(startDateCal.get(Calendar.MONTH));
        }

        if(project.getEndDate()!=null)
        {
	        bean.setEndDate(project.getEndDate());
        }
        

        
        bean.setEndMarketName(project.getEndMarketName());
        List<Long> emIds = getSynchroProjectManagerNew().getEndMarketIDs(project.getProjectID());
        if(project.getEndMarketName()!=null && project.getEndMarketName().equalsIgnoreCase("MultiMarket"))
        {
        	
    		if(emIds!=null && emIds.size()>0)
    		{
    			List<String> endMarketNames=new ArrayList<String>();
    			for(Long endMarketId : emIds )
    			{
    				//endMarketNames.add(SynchroGlobal.getEndMarkets().get(endMarketId.intValue()));
    				
    				String endMarketName = SynchroGlobal.getEndMarkets().get(endMarketId.intValue());
    				if(StringUtils.isNotBlank(endMarketName))
    				{
    					endMarketNames.add(endMarketName);
    				}
    				else
    				{
    					// This is done for Regions
    					endMarketName = SynchroGlobal.getRegions().get(endMarketId.intValue());
    					if(StringUtils.isNotBlank(endMarketName))
        				{
        					endMarketNames.add(endMarketName);
        				}
    				}
    			}
    			bean.setEndMarketName(StringUtils.join(endMarketNames, ","));
    			
    		}
    		else
    		{
    			bean.setEndMarketName("");
    		}
        }
        
        
        //bean.setStatus(SynchroGlobal.Status.getName(project.getStatus().intValue()).toString());
        
        if(project.getNewSynchro()!=null)
        {
	        if(project.getNewSynchro())
	        {
	        	 bean.setStatus(SynchroGlobal.ProjectStatusNew.getName(project.getStatus().intValue()).toString());
	        	 bean.setNewSynchro(project.getNewSynchro());
	        }
	        else
	        {
	        	 bean.setStatus(SynchroGlobal.Status.getName(project.getStatus().intValue()).toString());
	        	 bean.setNewSynchro(project.getNewSynchro());
	        }
        }
        else
        {
        	bean.setStatus(SynchroGlobal.Status.getName(project.getStatus().intValue()).toString());
       	 	bean.setNewSynchro(project.getNewSynchro());
        }
       
        bean.setMethodology(SynchroDAOUtil.getMethodologyNames(StringUtils.join(project.getMethodologyDetails(),",")));
        bean.setTotalCost(project.getTotalCost());
        
        // The Migrated projects should be open in Brief Stage Only
       /* if(project.getIsMigrated())
        {
        	bean.setUrl("/new-synchro/pib-details!input.jspa?projectID="+project.getProjectID());
        }
        else
        {
        	bean.setUrl(ProjectStage.generateURLNew(project));
        }*/
        
        bean.setUrl(ProjectStage.generateURLNew(project));
        
        //http://redmine.nvish.com/redmine/issues/459 - Cancel Status to be displayed on Dashboard
        if(project.getIsCancel())
        {
        	bean.setProjectStage(SynchroGlobal.ProjectStatusNew.CANCEL.getValue());
        }
        else
        {
        	bean.setProjectStage(SynchroGlobal.ProjectStatusNew.getName(project.getStatus()));
        }
        bean.setProjectManagerName(project.getProjectManagerName());
        bean.setProjectTrackStatus(project.getProjectTrackStatus());
        
        bean.setBudgetYear(project.getBudgetYear());
        
        if(emIds!=null && emIds.size()>0)
        {
        	
        	// This has been done as part of http://redmine.nvish.com/redmine/issues/458
        	//Map<Integer, List<AttachmentBean>> attachmentMap = getPIBManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
        	Map<Integer, List<AttachmentBean>> attachmentMap = getReportSummaryManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
        	
        	//Brief Non Mandatory Case
        	if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId()
        			|| project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId()))
        	{
        		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId())!=null)
	        	{
	        		bean.setBriefUploaded("Yes");
	        	}
	        	else
	        	{
	        		bean.setBriefUploaded("No");
	        	}
        	}
        	//Brief Mandatory Case
        	else
        	{
	        	if(SynchroUtils.hasDateMet(project.getStartDate()))
	        	{
		        	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId())!=null)
		        	{
		        		bean.setBriefUploaded("Yes");
		        		bean.setBriefUploadColor("Green");
		        	}
		        	else
		        	{
		        		bean.setBriefUploaded("No");
		        		bean.setBriefUploadColor("Red");
		        	}
	        	}
	        	else
	        	{
	        		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId())!=null)
		        	{
		        		bean.setBriefUploaded("Yes");
		        		bean.setBriefUploadColor("Green");
		        	}
		        	else
		        	{
		        		bean.setBriefUploaded("No");
		        		bean.setBriefUploadColor("");
		        	}
	        	}
        	}
        		
        		
        }
        else
        {
        	bean.setBriefUploaded("No");
        }
        
       
        List<ProjectInitiation> projectInitiationList = getPIBManagerNew().getPIBDetailsNew(project.getProjectID());
        
      //Brief Non Mandatory Case
    	if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId()
    			|| project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId()))
    	{
    		if(projectInitiationList!=null && projectInitiationList.size()>0)
	        {
	        	if(projectInitiationList.get(0).getLegalApprovalStatus()!=null && projectInitiationList.get(0).getLegalApprovalStatus() > 0)
	        	{
	        		bean.setBriefLegalApprovalReceived("Yes");
	        	}
	        	else if(bean.getBriefUploaded().equals("No"))
	        	{
	        		bean.setBriefLegalApprovalReceived("No");
	        	}
	        	else
	        	{
	        		//bean.setBriefLegalApprovalReceived("NA");
	        		bean.setBriefLegalApprovalReceived("No");
	        	}
	        }
	        else
	        {
	        	//bean.setBriefLegalApprovalReceived("NA");
	        	bean.setBriefLegalApprovalReceived("No");
	        }
    	}
    	// EU-OFFLINE CASE
    	else if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
    	{
    		if(SynchroUtils.hasDateMet(project.getStartDate()))
    		{
    			if(projectInitiationList!=null && projectInitiationList.size()>0)
		        {
    				//Map<Integer, List<AttachmentBean>> attachmentMap = getPIBManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
    				Map<Integer, List<AttachmentBean>> attachmentMap = getReportSummaryManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
    				if(projectInitiationList.get(0).getLegalApprovalStatus()!=null && projectInitiationList.get(0).getLegalApprovalStatus() > 0 
		        			&& projectInitiationList.get(0).getLegalApprovalDate()!=null && attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF_LEGAL_APPROVAL.getId())!=null)
		        	{
		        		bean.setBriefLegalApprovalReceived("Yes");
		        		bean.setBriefLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setBriefLegalApprovalReceived("No");
		        		bean.setBriefLegalApprovalReceivedColor("Red");
		        	}
		        }
		        else
		        {
		        	bean.setBriefLegalApprovalReceived("No");
	        		bean.setBriefLegalApprovalReceivedColor("Red");
		        }
    		}
    		else
    		{
	    		if(projectInitiationList!=null && projectInitiationList.size()>0)
		        {
	    			//Map<Integer, List<AttachmentBean>> attachmentMap = getPIBManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
	    			Map<Integer, List<AttachmentBean>> attachmentMap = getReportSummaryManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
    				if(projectInitiationList.get(0).getLegalApprovalStatus()!=null && projectInitiationList.get(0).getLegalApprovalStatus() > 0 
		        			&& projectInitiationList.get(0).getLegalApprovalDate()!=null && attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF_LEGAL_APPROVAL.getId())!=null)
		        	
		        	{
		        		bean.setBriefLegalApprovalReceived("Yes");
		        		bean.setBriefLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setBriefLegalApprovalReceived("No");
		        	
		        	}
		        }
		        else
		        {
		        	bean.setBriefLegalApprovalReceived("No");
		        }
    		}
    	}
    	//Mandatory Case
    	else
    	{
    		if(SynchroUtils.hasDateMet(project.getStartDate()))
    		{
    			if(projectInitiationList!=null && projectInitiationList.size()>0)
		        {
		        	if(projectInitiationList.get(0).getLegalApprovalStatus()!=null && projectInitiationList.get(0).getLegalApprovalStatus() > 0)
		        	{
		        		bean.setBriefLegalApprovalReceived("Yes");
		        		bean.setBriefLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setBriefLegalApprovalReceived("No");
		        		bean.setBriefLegalApprovalReceivedColor("Red");
		        	}
		        }
		        else
		        {
		        	bean.setBriefLegalApprovalReceived("No");
	        		bean.setBriefLegalApprovalReceivedColor("Red");
		        }
    		}
    		else
    		{
	    		if(projectInitiationList!=null && projectInitiationList.size()>0)
		        {
	    			if(projectInitiationList.get(0).getLegalApprovalStatus()!=null && projectInitiationList.get(0).getLegalApprovalStatus() > 0)
		        	{
		        		bean.setBriefLegalApprovalReceived("Yes");
		        		bean.setBriefLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setBriefLegalApprovalReceived("No");
		        	
		        	}
		        }
		        else
		        {
		        	bean.setBriefLegalApprovalReceived("No");
		        }
    		}
    	}
        
    	// This is for removing the colour in case of Legal Sign Off Check box is checked
    	if(projectInitiationList!=null && projectInitiationList.size()>0 && projectInitiationList.get(0).getLegalSignOffRequired()!=null && projectInitiationList.get(0).getLegalSignOffRequired().intValue() == 1)
    	{
    		// http://redmine.nvish.com/redmine/issues/458 . Brief is mandatory even if the Legal Sign Off Check box is checked.
    		//bean.setBriefUploadColor("");
    		bean.setBriefLegalApprovalReceivedColor("");
    	}
    	
        if(emIds!=null && emIds.size()>0)
        {
        	
        	//Map<Integer, List<AttachmentBean>> attachmentMap = getProposalManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"), new Long("-1"));
        	Map<Integer, List<AttachmentBean>> attachmentMap = getReportSummaryManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
        	
        	/*if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
        	{
        		bean.setProposalUploaded("Yes");
        	}
        	else if(bean.getBriefUploaded().equals("Yes") && bean.getBriefLegalApprovalReceived().equals("Yes"))
        	{
        		bean.setProposalUploaded("No");
        	}
        	else
        	{
        		bean.setProposalUploaded("NA");
        	}
        	*/

     
        	
        	//Proposal Non Mandatory Case
        	/*if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId()))
        	{
        		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
	        	{
	        		bean.setProposalUploaded("Yes");
	        	}
	        	else
	        	{
	        		bean.setProposalUploaded("No");
	        	}
        	}
        	//Proposal Mandatory Case
        	else
        	{
	        	if(SynchroUtils.hasDateMet(project.getStartDate()))
	        	{
		        	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
		        	{
		        		bean.setProposalUploaded("Yes");
		        		bean.setProposalUploadedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setProposalUploaded("No");
		        		bean.setProposalUploadedColor("Red");
		        	}
	        	}
	        	else
	        	{
	        		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
		        	{
		        		bean.setProposalUploaded("Yes");
		        		bean.setProposalUploadedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setProposalUploaded("No");
		        		bean.setProposalUploadedColor("");
		        	}
	        	}
        	}*/
        	
        	if(SynchroUtils.hasDateMet(project.getStartDate()))
        	{
	        	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
	        	{
	        		bean.setProposalUploaded("Yes");
	        		bean.setProposalUploadedColor("Green");
	        	}
	        	else
	        	{
	        		bean.setProposalUploaded("No");
	        		bean.setProposalUploadedColor("Red");
	        	}
        	}
        	else
        	{
        		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
	        	{
	        		bean.setProposalUploaded("Yes");
	        		bean.setProposalUploadedColor("Green");
	        	}
	        	else
	        	{
	        		bean.setProposalUploaded("No");
	        		bean.setProposalUploadedColor("");
	        	}
        	}
      		
        }
        else
        {
        	//bean.setProposalUploaded("NA");
        	bean.setProposalUploaded("No");
        }
        
        List<ProposalInitiation> proposalInitiationList = getProposalManagerNew().getProposalInitiationNew(project.getProjectID());
       
        
      //Proposal Non Mandatory Case
    	if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId()
    			|| project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId()))
    	{
    		if(proposalInitiationList!=null && proposalInitiationList.size()>0)
	        {
    			if(proposalInitiationList.get(0).getLegalApprovalStatus()!=null && proposalInitiationList.get(0).getLegalApprovalStatus() > 0)
	        	{
	        		bean.setProposalLegalApprovalReceived("Yes");
	        	}
	        	else if(bean.getProposalUploaded().equals("No"))
	        	{
	        		bean.setProposalLegalApprovalReceived("No");
	        	}
	        	else
	        	{
	        		//bean.setProposalLegalApprovalReceived("NA");
	        		bean.setProposalLegalApprovalReceived("No");
	        	}
	        }
	        else
	        {
	        	//bean.setProposalLegalApprovalReceived("NA");
	        	bean.setProposalLegalApprovalReceived("No");
	        }
    	}
    	// EU-OFFLINE CASE
    	else if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
    	{
    		if(SynchroUtils.hasDateMet(project.getStartDate()))
    		{
    			if(proposalInitiationList!=null && proposalInitiationList.size()>0)
    	        {
    				//Map<Integer, List<AttachmentBean>> attachmentMap = getProposalManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"), new Long("-1"));
    				Map<Integer, List<AttachmentBean>> attachmentMap = getReportSummaryManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
    				if(proposalInitiationList.get(0).getLegalApprovalStatus()!=null && proposalInitiationList.get(0).getLegalApprovalStatus() > 0
		        			&& proposalInitiationList.get(0).getLegalApprovalDate()!=null && attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL_LEGAL_APPROVAL.getId())!=null)
    				{
		        		bean.setProposalLegalApprovalReceived("Yes");
		        		bean.setProposalLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setProposalLegalApprovalReceived("No");
		        		bean.setProposalLegalApprovalReceivedColor("Red");
		        	}
		        }
		        else
		        {
		        	bean.setProposalLegalApprovalReceived("No");
	        		bean.setProposalLegalApprovalReceivedColor("Red");
		        }
    		}
    		else
    		{
    			if(proposalInitiationList!=null && proposalInitiationList.size()>0)
    	        {
    				//Map<Integer, List<AttachmentBean>> attachmentMap = getProposalManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"), new Long("-1"));
    				Map<Integer, List<AttachmentBean>> attachmentMap = getReportSummaryManagerNew().getDocumentAttachment(project.getProjectID(), new Long("-1"));
    				if(proposalInitiationList.get(0).getLegalApprovalStatus()!=null && proposalInitiationList.get(0).getLegalApprovalStatus() > 0
		        			&& proposalInitiationList.get(0).getLegalApprovalDate()!=null && attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL_LEGAL_APPROVAL.getId())!=null)
    				
		        	{
		        		bean.setProposalLegalApprovalReceived("Yes");
		        		bean.setProposalLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setProposalLegalApprovalReceived("No");
		        	
		        	}
		        }
		        else
		        {
		        	bean.setProposalLegalApprovalReceived("No");
		        }
    		}
    	}
    	//Mandatory Case
    	else
    	{
    		if(SynchroUtils.hasDateMet(project.getStartDate()))
    		{
    			if(proposalInitiationList!=null && proposalInitiationList.size()>0)
    	        {
        			if(proposalInitiationList.get(0).getLegalApprovalStatus()!=null && proposalInitiationList.get(0).getLegalApprovalStatus() > 0)
		        	{
		        		bean.setProposalLegalApprovalReceived("Yes");
		        		bean.setProposalLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setProposalLegalApprovalReceived("No");
		        		bean.setProposalLegalApprovalReceivedColor("Red");
		        	}
		        }
		        else
		        {
		        	bean.setProposalLegalApprovalReceived("No");
	        		bean.setProposalLegalApprovalReceivedColor("Red");
		        }
    		}
    		else
    		{
    			if(proposalInitiationList!=null && proposalInitiationList.size()>0)
    	        {
        			if(proposalInitiationList.get(0).getLegalApprovalStatus()!=null && proposalInitiationList.get(0).getLegalApprovalStatus() > 0)
		        	{
		        		bean.setProposalLegalApprovalReceived("Yes");
		        		bean.setProposalLegalApprovalReceivedColor("Green");
		        	}
		        	else
		        	{
		        		bean.setProposalLegalApprovalReceived("No");
		        	
		        	}
		        }
		        else
		        {
		        	bean.setProposalLegalApprovalReceived("No");
		        }
    		}
    	}
        
    	
    	// This is for removing the colour in case of Legal Sign Off Check box is checked
    	if(proposalInitiationList!=null && proposalInitiationList.size()>0 && proposalInitiationList.get(0).getLegalSignOffRequired()!=null && proposalInitiationList.get(0).getLegalSignOffRequired().intValue() == 1)
    	{
    		// http://redmine.nvish.com/redmine/issues/458 . Proposal is mandatory even if the Legal Sign Off Check box is checked.
    		//bean.setProposalUploadedColor("");
    		bean.setProposalLegalApprovalReceivedColor("");
    	}
    	
    	List<ReportSummaryInitiation> reportSummaryInitiationList = getReportSummaryManagerNew().getReportSummaryInitiation(project.getProjectID());
        List<ReportSummaryDetails> reportSummaryDetailsList = getReportSummaryManagerNew().getReportSummaryDetails(project.getProjectID());
        Map<Integer, Map<Integer, List<Long>>> reportSummaryAttachments = getReportSummaryManagerNew().getReportSummaryAttachmentDetails(project.getProjectID());
        
       
        
        int fullTypeAttachmentRowSize = 0;
        int topLineTypeAttachmentRowSize = 0;
        int executivePresentationTypeAttachmentRowSize = 0;
        int irisSummaryAttachmentRowSize = 0;
        int tpdSummaryAttachmentRowSize = 0;
        
        if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null))
        {
        	fullTypeAttachmentRowSize = reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId()).size();
        }
        if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null))
        {
        	topLineTypeAttachmentRowSize = reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()).size();
        }
        if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null))
        {
        	executivePresentationTypeAttachmentRowSize = reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId()).size();
        }
        if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null))
        {
        	irisSummaryAttachmentRowSize = reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).size();
        }
        if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null))
        {
        	tpdSummaryAttachmentRowSize = reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).size();
        }
        
        
        int fullTypeLegalApproverRowSize = 0;
        int topLineTypeLegalApproverRowSize = 0;
        int executivePresentationTypeLegalApproverRowSize = 0;
        int irisSummaryLegalApproverRowSize = 0;
        int tpdSummaryLegalApproverRowSize = 0;
        
        for(ReportSummaryDetails  rsd :  reportSummaryDetailsList)
        {
        	if(rsd.getReportType()==SynchroGlobal.ReportType.FULL_REPORT.getId())
    		{
        		if(StringUtils.isNotBlank(rsd.getLegalApprover()))
        		{
        			if(bean.getReportLegalApprovalReceived()!=null && bean.getReportLegalApprovalReceived().equals("No"))
        			{
        				
        			}
        			else
        			{
        				bean.setReportLegalApprovalReceived("Yes");
        			}
        		}
        		else
        		{
        			bean.setReportLegalApprovalReceived("No");
        		}
        		fullTypeLegalApproverRowSize++;
        		
    		}
        	if(rsd.getReportType()==SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())
    		{
        		//bean.setReportLegalApprovalReceived("Yes");
        		if(StringUtils.isNotBlank(rsd.getLegalApprover()))
        		{
        			if(bean.getReportLegalApprovalReceived()!=null && bean.getReportLegalApprovalReceived().equals("No"))
        			{
        				
        			}
        			else
        			{
        				bean.setReportLegalApprovalReceived("Yes");
        			}
        		}
        		else
        		{
        			bean.setReportLegalApprovalReceived("No");
        		}
        		topLineTypeLegalApproverRowSize++;
        		
    		}
        	if(rsd.getReportType()==SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())
    		{
        		//bean.setReportLegalApprovalReceived("Yes");
        		if(StringUtils.isNotBlank(rsd.getLegalApprover()))
        		{
        			if(bean.getReportLegalApprovalReceived()!=null && bean.getReportLegalApprovalReceived().equals("No"))
        			{
        				
        			}
        			else
        			{
        				bean.setReportLegalApprovalReceived("Yes");
        			}
        		}
        		else
        		{
        			bean.setReportLegalApprovalReceived("No");
        		}
        		executivePresentationTypeLegalApproverRowSize++;
        		
    		}
        	if(rsd.getReportType()==SynchroGlobal.ReportType.IRIS_SUMMARY.getId() )
    		{
        		//bean.setIrisLegalApprovalReceived("Yes");
        		if(StringUtils.isNotBlank(rsd.getLegalApprover()))
        		{
        			if(bean.getIrisLegalApprovalReceived()!=null && bean.getIrisLegalApprovalReceived().equals("No"))
        			{
        				
        			}
        			else
        			{
        				bean.setIrisLegalApprovalReceived("Yes");
        			}
        		}
        		else
        		{
        			bean.setIrisLegalApprovalReceived("No");
        		}
        		irisSummaryLegalApproverRowSize++;
        	
    		}
        	if(rsd.getReportType()==SynchroGlobal.ReportType.TPD_SUMMARY.getId())
    		{
        		//bean.setTpdSummaryLegalApprovalReceived("Yes");
        		if(StringUtils.isNotBlank(rsd.getLegalApprover()))
        		{
        			if(bean.getTpdSummaryLegalApprovalReceived()!=null && bean.getTpdSummaryLegalApprovalReceived().equals("No"))
        			{
        				
        			}
        			else
        			{
        				bean.setTpdSummaryLegalApprovalReceived("Yes");
        			}
        		}
        		else
        		{
        			bean.setTpdSummaryLegalApprovalReceived("No");
        		}
        		tpdSummaryLegalApproverRowSize++;
    		}
        }
        
       
        
        /*if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null 
        		|| reportSummaryAttachments.get(SynchroGlobal.SynchroAttachmentObject.TOP_LINE_REPORT.getId())!=null 
        		|| reportSummaryAttachments.get(SynchroGlobal.SynchroAttachmentObject.EXECUTIVE_PRESENTATION_REPORT.getId())!=null))*/
       
        //Proposal Non Mandatory Case
    /*	if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId()))
    	{
        
	        if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null 
	     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null 
	     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null))
	        	
	        {
	        	if(fullTypeAttachmentRowSize==fullTypeLegalApproverRowSize && topLineTypeAttachmentRowSize==topLineTypeLegalApproverRowSize && 
	        			executivePresentationTypeAttachmentRowSize==executivePresentationTypeLegalApproverRowSize)
	        	{
	        		bean.setReportUploaded("Yes");
	        	}
	        	else
	        	{
	        		bean.setReportUploaded("No");
	        	}
	        }
	     
	        else
	        {
	        	bean.setReportUploaded("No");
	        }
	                
	        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null && irisSummaryAttachmentRowSize==irisSummaryLegalApproverRowSize)
	        {
	        	bean.setIrisSummaryUploaded("Yes");
	        }
	     
	        else
	        {
	        	bean.setIrisSummaryUploaded("No");
	        }
	        
	        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null && tpdSummaryAttachmentRowSize==tpdSummaryLegalApproverRowSize)
	        {
	        	bean.setTpdSummaryUploaded("Yes");
	        }
	       
	        else
	        {
	        	bean.setTpdSummaryUploaded("No");
	        }
    	}
    	else
    	{
    		if(SynchroUtils.hasDateMet(project.getEndDate()))
    		{
    			 if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null 
    			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null 
    			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null))
    			        	
    			        {
    				 		if(fullTypeAttachmentRowSize==fullTypeLegalApproverRowSize && topLineTypeAttachmentRowSize==topLineTypeLegalApproverRowSize && 
    		        			executivePresentationTypeAttachmentRowSize==executivePresentationTypeLegalApproverRowSize)
    				 		{
	    				 		bean.setReportUploaded("Yes");
	    			        	bean.setReportUploadedColor("Green");
    				 		}
    				 		else
    				 		{
    				 			bean.setReportUploaded("No");
        			        	bean.setReportUploadedColor("Red");
    				 		}
    			        }
    			    
    			        else
    			        {
    			        	bean.setReportUploaded("No");
    			        	bean.setReportUploadedColor("Red");
    			        }
    			                
    			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null && irisSummaryAttachmentRowSize==irisSummaryLegalApproverRowSize)
    			        {
    			        	bean.setIrisSummaryUploaded("Yes");
    			        	bean.setIrisSummaryUploadedColor("Green");
    			        }
    			        else
    			        {
    			        	bean.setIrisSummaryUploaded("No");
    			        	bean.setIrisSummaryUploadedColor("Red");
    			        }
    			        
    			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null && tpdSummaryAttachmentRowSize==tpdSummaryLegalApproverRowSize)
    			        {
    			        	bean.setTpdSummaryUploaded("Yes");
    			        	bean.setTpdSummaryUploadedColor("Green");
    			        }
    			        else
    			        {
    			        	bean.setTpdSummaryUploaded("No");
    			        	bean.setTpdSummaryUploadedColor("Red");
    			        }
    		}
    		else
    		{
    			 if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null 
    			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null 
    			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null))
    			        	
    			        {
    				 		if(fullTypeAttachmentRowSize==fullTypeLegalApproverRowSize && topLineTypeAttachmentRowSize==topLineTypeLegalApproverRowSize && 
    				 				executivePresentationTypeAttachmentRowSize==executivePresentationTypeLegalApproverRowSize)
    				 		{
	    				 		bean.setReportUploaded("Yes");
	    			        	bean.setReportUploadedColor("Green");
    				 		}
    				 		else
    				 		{
    				 			bean.setReportUploaded("No");
    				 		}
    			        }
    			    
    			        else
    			        {
    			        	bean.setReportUploaded("No");
    			        }
    			                
    			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null && irisSummaryAttachmentRowSize==irisSummaryLegalApproverRowSize)
    			        {
    			        	bean.setIrisSummaryUploaded("Yes");
    			        	bean.setIrisSummaryUploadedColor("Green");
    			        }
    			    
    			        else
    			        {
    			        	bean.setIrisSummaryUploaded("No");
    			        }
    			        
    			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null && tpdSummaryAttachmentRowSize==tpdSummaryLegalApproverRowSize)
    			        {
    			        	bean.setTpdSummaryUploaded("Yes");
    			        	bean.setTpdSummaryUploadedColor("Green");
    			        }
    			        else
    			        {
    			        	bean.setTpdSummaryUploaded("No");
    			        }
    		}
    	}
        
*/
        if(project.getProcessType()!=null && project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId())
        {
        	bean.setReportUploaded("No");
        	bean.setIrisSummaryUploaded("No");
        	bean.setTpdSummaryUploaded("No");
        	bean.setReportLegalApprovalReceived("No");
        	bean.setIrisLegalApprovalReceived("No");
        	bean.setTpdSummaryLegalApprovalReceived("No");
        }
        else if(SynchroUtils.hasDateMet(project.getEndDate()))
		{
			 if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null 
			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null 
			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null))
			        	
			        {
				 		if(fullTypeAttachmentRowSize==fullTypeLegalApproverRowSize && topLineTypeAttachmentRowSize==topLineTypeLegalApproverRowSize && 
		        			executivePresentationTypeAttachmentRowSize==executivePresentationTypeLegalApproverRowSize)
				 		{
    				 		bean.setReportUploaded("Yes");
    			        	bean.setReportUploadedColor("Green");
				 		}
				 		else
				 		{
				 			bean.setReportUploaded("No");
    			        	bean.setReportUploadedColor("Red");
				 		}
			        }
			    
			        else
			        {
			        	bean.setReportUploaded("No");
			        	bean.setReportUploadedColor("Red");
			        }
			                
			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null && irisSummaryAttachmentRowSize==irisSummaryLegalApproverRowSize)
			        {
			        	bean.setIrisSummaryUploaded("Yes");
			        	bean.setIrisSummaryUploadedColor("Green");
			        }
			        else
			        {
			        	bean.setIrisSummaryUploaded("No");
			        	bean.setIrisSummaryUploadedColor("Red");
			        }
			        
			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null && tpdSummaryAttachmentRowSize==tpdSummaryLegalApproverRowSize)
			        {
			        	bean.setTpdSummaryUploaded("Yes");
			        	//bean.setTpdSummaryUploadedColor("Green");
			        	// Only if the Latest TPD Status is set as 1, then only show the TPD Summary colour. It means the colour should come only when the TPD Summary has to be displayed.
			    		if(SynchroPermHelper.getLatestTPDStatus(project.getProjectID())==1)
			    		{
			    			bean.setTpdSummaryUploadedColor("Green");
			    		}
			        }
			        else
			        {
			        	bean.setTpdSummaryUploaded("No");
			        	if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId()))
			        	{
			        		
			        	}
			        	else
			        	{
			        		// Only if the Latest TPD Status is set as 1, then only show the TPD Summary colour. It means the colour should come only when the TPD Summary has to be displayed.
			        		if(SynchroPermHelper.getLatestTPDStatus(project.getProjectID())==1)
			        		{
			        			bean.setTpdSummaryUploadedColor("Red");
			        		}
			        	}
			        }
		}
		else
		{
			 if(reportSummaryAttachments!=null && (reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null 
			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null 
			     		|| reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null))
			        	
			        {
				 		if(fullTypeAttachmentRowSize==fullTypeLegalApproverRowSize && topLineTypeAttachmentRowSize==topLineTypeLegalApproverRowSize && 
				 				executivePresentationTypeAttachmentRowSize==executivePresentationTypeLegalApproverRowSize)
				 		{
    				 		bean.setReportUploaded("Yes");
    			        	bean.setReportUploadedColor("Green");
				 		}
				 		else
				 		{
				 			bean.setReportUploaded("No");
				 		}
			        }
			    
			        else
			        {
			        	bean.setReportUploaded("No");
			        }
			                
			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null && irisSummaryAttachmentRowSize==irisSummaryLegalApproverRowSize)
			        {
			        	bean.setIrisSummaryUploaded("Yes");
			        	bean.setIrisSummaryUploadedColor("Green");
			        }
			    
			        else
			        {
			        	bean.setIrisSummaryUploaded("No");
			        }
			        
			        if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null && tpdSummaryAttachmentRowSize==tpdSummaryLegalApproverRowSize)
			        {
			        	bean.setTpdSummaryUploaded("Yes");
			        	//bean.setTpdSummaryUploadedColor("Green");
			        	// Only if the Latest TPD Status is set as 1, then only show the TPD Summary colour. It means the colour should come only when the TPD Summary has to be displayed.
			    		if(SynchroPermHelper.getLatestTPDStatus(project.getProjectID())==1)
			    		{
			    			bean.setTpdSummaryUploadedColor("Green");
			    		}
			        }
			        else
			        {
			        	bean.setTpdSummaryUploaded("No");
			        }
		}
	
        
        if(project.getProcessType()!=null && project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId())
        {
        	
        }
       /* else if(bean.getReportLegalApprovalReceived()!=null && bean.getReportLegalApprovalReceived().equals("Yes") && fullTypeAttachmentRowSize==fullTypeLegalApproverRowSize && topLineTypeAttachmentRowSize==topLineTypeLegalApproverRowSize && 
 				executivePresentationTypeAttachmentRowSize==executivePresentationTypeLegalApproverRowSize)
        {
        	bean.setReportLegalApprovalReceivedColor("Green");
        }*/
        // As per http://redmine.nvish.com/redmine/issues/458
        else if(bean.getReportLegalApprovalReceived()!=null && bean.getReportLegalApprovalReceived().equals("Yes"))
        {
        	bean.setReportLegalApprovalReceivedColor("Green");
        }
        else 
        {
        	bean.setReportLegalApprovalReceived("No");
        	if(SynchroUtils.hasDateMet(project.getEndDate()))
    		{
    			bean.setReportLegalApprovalReceivedColor("Red");
    		}
    		
        }
        
        
        if(project.getProcessType()!=null && project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId())
        {
        	
        }
        /*else if(bean.getIrisLegalApprovalReceived()!=null && bean.getIrisLegalApprovalReceived().equals("Yes") && irisSummaryAttachmentRowSize==irisSummaryLegalApproverRowSize)
        {
        	bean.setIrisLegalApprovalReceivedColor("Green");
        }*/
        else if(bean.getIrisLegalApprovalReceived()!=null && bean.getIrisLegalApprovalReceived().equals("Yes"))
        {
        	bean.setIrisLegalApprovalReceivedColor("Green");
        }
        else 
        {
        	bean.setIrisLegalApprovalReceived("No");
        	if(SynchroUtils.hasDateMet(project.getEndDate()))
    		{
    			bean.setIrisLegalApprovalReceivedColor("Red");
    		}
    		
        }
        
        if(project.getProcessType()!=null && project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId())
        {
        	
        }
       /* else if(bean.getTpdSummaryLegalApprovalReceived()!=null && bean.getTpdSummaryLegalApprovalReceived().equals("Yes") && tpdSummaryAttachmentRowSize==tpdSummaryLegalApproverRowSize)
        {
        	bean.setTpdSummaryLegalApprovalReceivedColor("Green");
        }
       */
        else if(bean.getTpdSummaryLegalApprovalReceived()!=null && bean.getTpdSummaryLegalApprovalReceived().equals("Yes"))
        {
        	//bean.setTpdSummaryLegalApprovalReceivedColor("Green");
        	// Only if the Latest TPD Status is set as 1, then only show the TPD Summary colour. It means the colour should come only when the TPD Summary has to be displayed.
    		if(SynchroPermHelper.getLatestTPDStatus(project.getProjectID())==1)
    		{
    			bean.setTpdSummaryLegalApprovalReceivedColor("Green");
    		}
        }
        else
        {
        	bean.setTpdSummaryLegalApprovalReceived("No");
        	if(project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId()))
        	{
        		
        	}
        	else
        	{
	        	if(SynchroUtils.hasDateMet(project.getEndDate()))
	    		{
	    			//bean.setTpdSummaryLegalApprovalReceivedColor("Red");
	    			
	    			// Only if the Latest TPD Status is set as 1, then only show the TPD Summary colour. It means the colour should come only when the TPD Summary has to be displayed.
	        		if(SynchroPermHelper.getLatestTPDStatus(project.getProjectID())==1)
	        		{
	        			bean.setTpdSummaryLegalApprovalReceivedColor("Red");
	        		}
	    		}
        	}
        }
      
    
     // This is for removing the colour in case of Legal Sign Off Check box is checked
    	if(reportSummaryInitiationList!=null && reportSummaryInitiationList.size()>0 && reportSummaryInitiationList.get(0).getLegalSignOffRequired()!=null && reportSummaryInitiationList.get(0).getLegalSignOffRequired().intValue() == 1)
    	{
    		bean.setReportUploadedColor("");
    		bean.setReportLegalApprovalReceivedColor("");
    		bean.setIrisSummaryUploadedColor("");
    		bean.setIrisLegalApprovalReceivedColor("");
    		// As TPD Summary is mandatory field now so the exception should not have any impact on this.
    		//bean.setTpdSummaryUploadedColor("");
    		//bean.setTpdSummaryLegalApprovalReceivedColor("");
    	}
    	
    	
    	 // This is for removing the colour in case the Budget Location is one of the Exception Budget Location i.e Australia, Canada or New Zealand
    	if(SynchroPermHelper.budgetYearException(project.getProjectID()))
    	{
    		bean.setReportUploadedColor("");
    		bean.setReportLegalApprovalReceivedColor("");
    		bean.setIrisSummaryUploadedColor("");
    		bean.setIrisLegalApprovalReceivedColor("");
    	}
        
        if(SynchroUtils.hasDateMet(project.getEndDate()))
        {
        	if(project.getStatus()==SynchroGlobal.ProjectStatusNew.CLOSE.ordinal())
            {
            	bean.setReviewDone("Yes");
            	bean.setReviewDoneColor("Green");
            }
        	else
        	{
        		bean.setReviewDone("No");
        		bean.setReviewDoneColor("Red");
        	}
        }
        else
        {
        	if(project.getStatus()==SynchroGlobal.ProjectStatusNew.CLOSE.ordinal())
            {
            	bean.setReviewDone("Yes");
            	bean.setReviewDoneColor("Green");
            
            }
        	else
        	{
        		bean.setReviewDone("No");
        	
        	}
        }
        return bean;
    }
    
    
    // This method is used for TPD Dashboard
    public static ProjectDashboardViewBeanNew toTPDDashboardViewBean(final Project project){

    	ProjectDashboardViewBeanNew bean = new ProjectDashboardViewBeanNew();
        bean.setProjectID(project.getProjectID());
        bean.setProjectName(project.getName());
        bean.setConfidential(project.getConfidential());

        Calendar startDateCal = Calendar.getInstance();
        if(project.getStartDate()!=null)
        {
	        startDateCal.setTime(project.getStartDate());
	        bean.setStartDate(startDateCal.getTime());
	        bean.setStartYear(startDateCal.get(Calendar.YEAR));
	        bean.setStartMonth(startDateCal.get(Calendar.MONTH));
        }

        

        
        bean.setEndMarketName(project.getEndMarketName());
        if(project.getEndMarketName()!=null && project.getEndMarketName().equalsIgnoreCase("MultiMarket"))
        {
        	List<Long> emIds = getSynchroProjectManagerNew().getEndMarketIDs(project.getProjectID());
    		if(emIds!=null && emIds.size()>0)
    		{
    			List<String> endMarketNames=new ArrayList<String>();
    			for(Long endMarketId : emIds )
    			{
    				endMarketNames.add(SynchroGlobal.getEndMarkets().get(endMarketId.intValue()));
    			}
    			bean.setEndMarketName(StringUtils.join(endMarketNames, ","));
    			
    		}
    		else
    		{
    			bean.setEndMarketName("");
    		}
        }
        
        
        //bean.setStatus(SynchroGlobal.Status.getName(project.getStatus().intValue()).toString());
        
        if(project.getNewSynchro())
        {
        	 bean.setStatus(SynchroGlobal.ProjectStatusNew.getName(project.getStatus().intValue()).toString());
        	 bean.setNewSynchro(project.getNewSynchro());
        }
        else
        {
        	 bean.setStatus(SynchroGlobal.Status.getName(project.getStatus().intValue()).toString());
        	 bean.setNewSynchro(project.getNewSynchro());
        }
       
        bean.setMethodology(SynchroDAOUtil.getMethodologyNames(StringUtils.join(project.getMethodologyDetails(),",")));
        bean.setTotalCost(project.getTotalCost());
        bean.setUrl("/new-synchro/tpd-summary!input.jspa?projectID="+project.getProjectID());
        bean.setProjectStage(SynchroGlobal.ProjectStatusNew.getName(project.getStatus()));
        bean.setProjectManagerName(project.getProjectManagerName());
        bean.setProjectTrackStatus(project.getProjectTrackStatus());
        
        
       
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


    public static ProjectManagerNew getSynchroProjectManagerNew() {
        if(synchroProjectManagerNew == null){
            synchroProjectManagerNew = JiveApplication.getContext().getSpringBean("synchroProjectManagerNew");
        }
        return synchroProjectManagerNew;
    }
    
    public static PIBManagerNew getPIBManagerNew() {
        if(pibManagerNew == null){
        	pibManagerNew = JiveApplication.getContext().getSpringBean("pibManagerNew");
        }
        return pibManagerNew;
    }
    
    public static ProposalManagerNew getProposalManagerNew() {
        if(proposalManagerNew == null){
        	proposalManagerNew = JiveApplication.getContext().getSpringBean("proposalManagerNew");
        }
        return proposalManagerNew;
    }
    
    public static ReportSummaryManagerNew getReportSummaryManagerNew() {
        if(reportSummaryManagerNew == null){
        	reportSummaryManagerNew = JiveApplication.getContext().getSpringBean("reportSummaryManagerNew");
        }
        return reportSummaryManagerNew;
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

	public Boolean getNewSynchro() {
		return newSynchro;
	}

	public void setNewSynchro(Boolean newSynchro) {
		this.newSynchro = newSynchro;
	}

	public String getProjectManagerName() {
		return projectManagerName;
	}

	public void setProjectManagerName(String projectManagerName) {
		this.projectManagerName = projectManagerName;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	public String getMethodology() {
		return methodology;
	}

	public void setMethodology(String methodology) {
		this.methodology = methodology;
	}

	public String getProjectStage() {
		return projectStage;
	}

	public void setProjectStage(String projectStage) {
		this.projectStage = projectStage;
	}

	public String getProjectTrackStatus() {
		return projectTrackStatus;
	}

	public void setProjectTrackStatus(String projectTrackStatus) {
		this.projectTrackStatus = projectTrackStatus;
	}

	public String getTpdPreviouslySubmitted() {
		return tpdPreviouslySubmitted;
	}

	public void setTpdPreviouslySubmitted(String tpdPreviouslySubmitted) {
		this.tpdPreviouslySubmitted = tpdPreviouslySubmitted;
	}

	public Date getTpdLastSubmittedDate() {
		return tpdLastSubmittedDate;
	}

	public void setTpdLastSubmittedDate(Date tpdLastSubmittedDate) {
		this.tpdLastSubmittedDate = tpdLastSubmittedDate;
	}

	public Integer getBudgetYear() {
		return budgetYear;
	}

	public void setBudgetYear(Integer budgetYear) {
		this.budgetYear = budgetYear;
	}

	public String getBudgetLocation() {
		return budgetLocation;
	}

	public void setBudgetLocation(String budgetLocation) {
		this.budgetLocation = budgetLocation;
	}

	public String getProposalLegalApproval() {
		return proposalLegalApproval;
	}

	public void setProposalLegalApproval(String proposalLegalApproval) {
		this.proposalLegalApproval = proposalLegalApproval;
	}

	public Date getProposalLegalApprovalDate() {
		return proposalLegalApprovalDate;
	}

	public void setProposalLegalApprovalDate(Date proposalLegalApprovalDate) {
		this.proposalLegalApprovalDate = proposalLegalApprovalDate;
	}

	public String getTpdResearchDoneOn() {
		return tpdResearchDoneOn;
	}

	public void setTpdResearchDoneOn(String tpdResearchDoneOn) {
		this.tpdResearchDoneOn = tpdResearchDoneOn;
	}

	public String getTpdProductDescription() {
		return tpdProductDescription;
	}

	public void setTpdProductDescription(String tpdProductDescription) {
		this.tpdProductDescription = tpdProductDescription;
	}

	public String getTpdProductionModification() {
		return tpdProductionModification;
	}

	public void setTpdProductionModification(String tpdProductionModification) {
		this.tpdProductionModification = tpdProductionModification;
	}

	public Date getTpdProductModificationDate() {
		return tpdProductModificationDate;
	}

	public void setTpdProductModificationDate(Date tpdProductModificationDate) {
		this.tpdProductModificationDate = tpdProductModificationDate;
	}

	public String getTpdTAOCode() {
		return tpdTAOCode;
	}

	public void setTpdTAOCode(String tpdTAOCode) {
		this.tpdTAOCode = tpdTAOCode;
	}

	public Date getSkuSubmissionDate1() {
		return skuSubmissionDate1;
	}

	public void setSkuSubmissionDate1(Date skuSubmissionDate1) {
		this.skuSubmissionDate1 = skuSubmissionDate1;
	}

	public String getSkuDetails1() {
		return skuDetails1;
	}

	public void setSkuDetails1(String skuDetails1) {
		this.skuDetails1 = skuDetails1;
	}

	public Date getTpdSummaryLegalApprovalDate() {
		return tpdSummaryLegalApprovalDate;
	}

	public void setTpdSummaryLegalApprovalDate(Date tpdSummaryLegalApprovalDate) {
		this.tpdSummaryLegalApprovalDate = tpdSummaryLegalApprovalDate;
	}

	public String getTpdSummaryLegalApprover() {
		return tpdSummaryLegalApprover;
	}

	public void setTpdSummaryLegalApprover(String tpdSummaryLegalApprover) {
		this.tpdSummaryLegalApprover = tpdSummaryLegalApprover;
	}

	public String getBriefUploaded() {
		return briefUploaded;
	}

	public void setBriefUploaded(String briefUploaded) {
		this.briefUploaded = briefUploaded;
	}

	public String getBriefLegalApprovalReceived() {
		return briefLegalApprovalReceived;
	}

	public void setBriefLegalApprovalReceived(String briefLegalApprovalReceived) {
		this.briefLegalApprovalReceived = briefLegalApprovalReceived;
	}

	public String getProposalUploaded() {
		return proposalUploaded;
	}

	public void setProposalUploaded(String proposalUploaded) {
		this.proposalUploaded = proposalUploaded;
	}

	public String getProposalLegalApprovalReceived() {
		return proposalLegalApprovalReceived;
	}

	public void setProposalLegalApprovalReceived(
			String proposalLegalApprovalReceived) {
		this.proposalLegalApprovalReceived = proposalLegalApprovalReceived;
	}

	public String getReportUploaded() {
		return reportUploaded;
	}

	public void setReportUploaded(String reportUploaded) {
		this.reportUploaded = reportUploaded;
	}

	public String getReportLegalApprovalReceived() {
		return reportLegalApprovalReceived;
	}

	public void setReportLegalApprovalReceived(String reportLegalApprovalReceived) {
		this.reportLegalApprovalReceived = reportLegalApprovalReceived;
	}

	public String getIrisSummaryUploaded() {
		return irisSummaryUploaded;
	}

	public void setIrisSummaryUploaded(String irisSummaryUploaded) {
		this.irisSummaryUploaded = irisSummaryUploaded;
	}

	public String getIrisLegalApprovalReceived() {
		return irisLegalApprovalReceived;
	}

	public void setIrisLegalApprovalReceived(String irisLegalApprovalReceived) {
		this.irisLegalApprovalReceived = irisLegalApprovalReceived;
	}

	public String getTpdSummaryUploaded() {
		return tpdSummaryUploaded;
	}

	public void setTpdSummaryUploaded(String tpdSummaryUploaded) {
		this.tpdSummaryUploaded = tpdSummaryUploaded;
	}

	public String getTpdSummaryLegalApprovalReceived() {
		return tpdSummaryLegalApprovalReceived;
	}

	public void setTpdSummaryLegalApprovalReceived(
			String tpdSummaryLegalApprovalReceived) {
		this.tpdSummaryLegalApprovalReceived = tpdSummaryLegalApprovalReceived;
	}

	public String getReviewDone() {
		return reviewDone;
	}

	public void setReviewDone(String reviewDone) {
		this.reviewDone = reviewDone;
	}

	public String getTpdLastSubmittedDateString() {
		return tpdLastSubmittedDateString;
	}

	public void setTpdLastSubmittedDateString(String tpdLastSubmittedDateString) {
		this.tpdLastSubmittedDateString = tpdLastSubmittedDateString;
	}

	public String getBriefUploadColor() {
		return briefUploadColor;
	}

	public void setBriefUploadColor(String briefUploadColor) {
		this.briefUploadColor = briefUploadColor;
	}

	public String getBriefLegalApprovalReceivedColor() {
		return briefLegalApprovalReceivedColor;
	}

	public void setBriefLegalApprovalReceivedColor(
			String briefLegalApprovalReceivedColor) {
		this.briefLegalApprovalReceivedColor = briefLegalApprovalReceivedColor;
	}

	public String getProposalUploadedColor() {
		return proposalUploadedColor;
	}

	public void setProposalUploadedColor(String proposalUploadedColor) {
		this.proposalUploadedColor = proposalUploadedColor;
	}

	public String getProposalLegalApprovalReceivedColor() {
		return proposalLegalApprovalReceivedColor;
	}

	public void setProposalLegalApprovalReceivedColor(
			String proposalLegalApprovalReceivedColor) {
		this.proposalLegalApprovalReceivedColor = proposalLegalApprovalReceivedColor;
	}

	public String getReportUploadedColor() {
		return reportUploadedColor;
	}

	public void setReportUploadedColor(String reportUploadedColor) {
		this.reportUploadedColor = reportUploadedColor;
	}

	public String getReportLegalApprovalReceivedColor() {
		return reportLegalApprovalReceivedColor;
	}

	public void setReportLegalApprovalReceivedColor(
			String reportLegalApprovalReceivedColor) {
		this.reportLegalApprovalReceivedColor = reportLegalApprovalReceivedColor;
	}

	public String getIrisSummaryUploadedColor() {
		return irisSummaryUploadedColor;
	}

	public void setIrisSummaryUploadedColor(String irisSummaryUploadedColor) {
		this.irisSummaryUploadedColor = irisSummaryUploadedColor;
	}

	public String getIrisLegalApprovalReceivedColor() {
		return irisLegalApprovalReceivedColor;
	}

	public void setIrisLegalApprovalReceivedColor(
			String irisLegalApprovalReceivedColor) {
		this.irisLegalApprovalReceivedColor = irisLegalApprovalReceivedColor;
	}

	public String getTpdSummaryUploadedColor() {
		return tpdSummaryUploadedColor;
	}

	public void setTpdSummaryUploadedColor(String tpdSummaryUploadedColor) {
		this.tpdSummaryUploadedColor = tpdSummaryUploadedColor;
	}

	public String getTpdSummaryLegalApprovalReceivedColor() {
		return tpdSummaryLegalApprovalReceivedColor;
	}

	public void setTpdSummaryLegalApprovalReceivedColor(
			String tpdSummaryLegalApprovalReceivedColor) {
		this.tpdSummaryLegalApprovalReceivedColor = tpdSummaryLegalApprovalReceivedColor;
	}

	public String getReviewDoneColor() {
		return reviewDoneColor;
	}

	public void setReviewDoneColor(String reviewDoneColor) {
		this.reviewDoneColor = reviewDoneColor;
	}

	public List<TPDSKUDetails> getTpdSkuDetails() {
		return tpdSkuDetails;
	}

	public void setTpdSkuDetails(List<TPDSKUDetails> tpdSkuDetails) {
		this.tpdSkuDetails = tpdSkuDetails;
	}

	public String getTpdSummaryAttUploaded() {
		return tpdSummaryAttUploaded;
	}

	public void setTpdSummaryAttUploaded(String tpdSummaryAttUploaded) {
		this.tpdSummaryAttUploaded = tpdSummaryAttUploaded;
	}

	
}
