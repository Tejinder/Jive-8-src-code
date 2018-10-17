package com.grail.synchro.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.google.common.collect.Lists;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.grail.util.URLUtils;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.mail.util.TemplateUtil;
import com.jivesoftware.util.InputStreamDataSource;
import com.jivesoftware.util.StringUtils;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: tejinder
 * @since: 1.0
 * Action class for Report Summary Stage for Multi Market
 */
public class ReportSummaryMultiMarketAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ReportSummaryMultiMarketAction.class);
    //Spring Managers
    private ReportSummaryManager reportSummaryManager;
    private ProjectManager synchroProjectManager;
    private PIBManager pibManager;
    private ProjectEvaluationManager projectEvaluationManager;
    private String attachmentName;
    //Form related fields
    private ReportSummaryInitiation reportSummaryInitiation;
    private ReportSummaryInitiation reportSummaryInitiation_DB;
    private Project project;
    private Long projectID;
    private boolean isSave;
    
    private boolean editStage;
   
    private String notificationTabId;

	private String redirectURL;
	private String approve;
	private String recipients;
	private String subject;
	private String messageBody;
	
	List<SynchroStageToDoListBean> stageToDoList = new ArrayList<SynchroStageToDoListBean>();;
	private Integer stageId;
		
	private Map<String, String> approvers = new LinkedHashMap<String, String>();
	private StageManager stageManager;
	
    private AttachmentHelper attachmentHelper;
	
    private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long fieldCategoryId;
    //private List<Long> endMarketIds;
    
    private Long endMarketId;
   
    //This map will contain the list of attachments for each field
    private Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();
    
	// This will contain  the Approvers data for the Checklist Tab
	Map<String,Map<String,String>> stageApprovers = new LinkedHashMap<String, Map<String,String>>();
	DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private Map<Long,Long> attachmentUser;
	private Boolean showMandatoryFieldsError;
	private Boolean isAboveMarket;
	private List<ReportSummaryInitiation> reportSummaryList;
	// This flag is used to enable the Notification buttons on the PIB Stage by checking the status of individual End Market.
    private Boolean allEndMarketSaved;
    
    private String subjectSendForApproval;
	private String messageBodySendForApproval;
	
	private String subjectNeedsRevision;
	private String messageBodyNeedsRevision;
	
	private String subjectSendToProjectContact;
	private String messageBodySendToProjectContact;
	
	private String subjectApprove;
	private String messageBodyApprove;
	
	private String subjectUploadToIris;
	private String messageBodyUploadToIris;
	
	private String subjectUploadToCPSIDatabase;
	private String messageBodyUploadToCPSIDatabase;
			
	private String subjectSummaryUploadIris;
	private String messageBodySummaryUploadIris;
	
	private SynchroUtils synchroUtils;
	private String aboveMarketProjectContact;
	private String awardedExternalAgency;
	private String synchroAdminUsers;
	
	 // This flag is used to check whether the Legal Approval are saved on all the End Markets or not.
    private Boolean allEndMarketLegalApprovalSaved = true; 
    private String legalApproverEndMarkets;
    private String requiredLegalApproverEndMarkets;
	
	private Boolean legalApprovalRequired = false;
	
	  // These variables will have the ProjectOwner and Project Contact values for the endMarkets other than Above Market
    private Long endMarketProjectOwner;
    private Long endMarketProjectContact;
       
    // This variable will contain the status of End Market
    private Integer endMarketStatus;
    
    // This flag will tell whether the Project Specs have been approved for all the End Markets or not.
    private Boolean allProjectSpecsArroved = false;
    private ProjectSpecsManager projectSpecsManager;
    
	private File[] mailAttachment;
	private String[] mailAttachmentFileName;
	private String[] mailAttachmentContentType;
	
	private SynchroToIRIS synchroToIRIS;
	private SynchroToIRIS synchroToIRIS_DB;
	private List<Long> emIds;
	private Long attachmentFieldID;
    
    // This field will tell whether all the Fields on the SynchroTOIRIS window have been filled or not.
    private Boolean isAllSynchroToIRISFilled;
    
    public Boolean getLegalApprovalRequired() {
		return legalApprovalRequired;
	}

	private EmailNotificationManager emailNotificationManager;
    
	public void prepare() throws Exception {
        final String id = getRequest().getParameter("projectID");
        

        if(id != null ) {

                try{
                    projectID = Long.parseLong(id);
                    
                } catch (NumberFormatException nfEx) {
                    LOG.error("Invalid ProjectID ");
                    throw nfEx;
                }
                String validationError = getRequest().getParameter("validationError");
                /*if(validationError!=null && validationError.equals("true"))
                {
                	showMandatoryFieldsError=true;
                }
               */
                project = this.synchroProjectManager.get(projectID);
                
                String emId = getRequest().getParameter("endMarketId");
                if(emId!=null && !emId.equals("") && !emId.equals(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID.toString()))
                {
                	isAboveMarket=false;
                	
                	 try{
                		 endMarketId=Long.parseLong(emId);
                     } catch (NumberFormatException nfEx) {
                         LOG.error("Invalid End Market Id ");
                         throw nfEx;
                     }
                	 redirectURL="/synchro/report-multi-summary!input.jspa?projectID="+projectID+"&endMarketId="+endMarketId;
                }
                else
                {
                	isAboveMarket=true;
                	// To add the Above Market Endmarket id
                	//endMarketId=endMarketDetails.get(0).getEndMarketID();
                	endMarketId = SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID;
                	redirectURL="/synchro/report-multi-summary!input.jspa?projectID="+projectID;
                	
                }
                
               // endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
               // endMarketDetails = this.synchroProjectManager.getEndMarketDetails(projectID);
             	
                List<ReportSummaryInitiation> initiationList = this.reportSummaryManager.getReportSummaryInitiation(projectID,endMarketId);
                reportSummaryList = this.reportSummaryManager.getReportSummaryInitiation(projectID);
                allEndMarketSaved = this.reportSummaryManager.allRepSummaryMarketSaved(projectID,reportSummaryList.size());
                
                attachmentMap = this.reportSummaryManager.getDocumentAttachment(projectID, endMarketId);
                
                endMarketStatus = this.synchroProjectManager.getEndMarketStatus(projectID, endMarketId);
                
                emIds = this.synchroProjectManager.getEndMarketIDs(projectID);
                
                this.synchroToIRIS = this.reportSummaryManager.getSynchroToIRIS(projectID, endMarketId, project);
                if(synchroToIRIS==null)
                {
                	synchroToIRIS = new SynchroToIRIS();
                }
                
              //Audit Logs
                this.synchroToIRIS_DB = this.reportSummaryManager.getSynchroToIRIS(projectID, endMarketId, project);
                List<ReportSummaryInitiation> initiationList_DB = this.reportSummaryManager.getReportSummaryInitiation(projectID, endMarketId);
                
                if(initiationList_DB!=null && initiationList_DB.size() > 0)
                {
                	 this.reportSummaryInitiation_DB = initiationList_DB.get(0);
                }
                
                if( initiationList != null && initiationList.size() > 0) {
                    this.reportSummaryInitiation = initiationList.get(0);
                 

                }  else {
                    this.reportSummaryInitiation = new ReportSummaryInitiation();
                    
                    isSave = true;
                }
               
                List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
                for(Integer i : attachmentMap.keySet())
                {
                	abList.addAll(attachmentMap.get(i));
                }
                attachmentUser = pibManager.getAttachmentUser(abList);
                
                // Moved from Input Action --
            //    String status=ribDocument.getProperties().get(SynchroConstants.STAGE_STATUS);
    			stageId = SynchroGlobal.getProjectActivityTab().get("screener");
    	
    			
    			String spiUserName="";
    			String spiApprovalDate=null;
    	
    			
    			if(reportSummaryInitiation.getSpiApprover() >0)
    			{
    				spiUserName = userManager.getUser(reportSummaryInitiation.getSpiApprover()).getName();
    			}
    			if(reportSummaryInitiation.getSpiApprovalDate()!=null)
    			{
    				spiApprovalDate = df.format(reportSummaryInitiation.getSpiApprovalDate());
    			}
    			
    			approvers.put(spiUserName, spiApprovalDate);
    			//approvers.put(legalUserName , legalAppDate);
    			editStage=SynchroPermHelper.canEditProjectByStatus(projectID);
    			//TODO - To add the project and stage status check over here whether the Proposal stage is completed or not. 
    			
    			//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-multi-summary!input.jspa?projectID=" + project.getProjectID();
    			String baseUrl = URLUtils.getBaseURL(request);
    			String stageUrl = baseUrl+"/synchro/report-multi-summary!input.jspa?projectID=" + project.getProjectID();
    			if(subjectSendForApproval==null)
    			{
     				subjectSendForApproval = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
    				subjectSendForApproval=subjectSendForApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectSendForApproval=subjectSendForApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodySendForApproval==null)
    			{
    				messageBodySendForApproval = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
    				messageBodySendForApproval=messageBodySendForApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodySendForApproval=messageBodySendForApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodySendForApproval=messageBodySendForApproval.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			if(subjectNeedsRevision==null)
    			{
    				subjectNeedsRevision = TemplateUtil.getTemplate("needs.revision.reportSummary.subject", JiveGlobals.getLocale());
    				subjectNeedsRevision=subjectNeedsRevision.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectNeedsRevision=subjectNeedsRevision.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodyNeedsRevision==null)
    			{
    				messageBodyNeedsRevision = TemplateUtil.getHtmlEscapedTemplate("needs.revision.reportSummary.htmlBody", JiveGlobals.getLocale());
    				messageBodyNeedsRevision=messageBodyNeedsRevision.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodyNeedsRevision=messageBodyNeedsRevision.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodyNeedsRevision=messageBodyNeedsRevision.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			
    			if(subjectSendToProjectContact==null)
    			{
    				subjectSendToProjectContact = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
    				subjectSendToProjectContact=subjectSendToProjectContact.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectSendToProjectContact=subjectSendToProjectContact.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodySendToProjectContact==null)
    			{
    				messageBodySendToProjectContact = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
    				messageBodySendToProjectContact=messageBodySendToProjectContact.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodySendToProjectContact=messageBodySendToProjectContact.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodySendToProjectContact=messageBodySendToProjectContact.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			
    			
    			if(subjectApprove==null)
    			{
    				subjectApprove = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
    				subjectApprove=subjectApprove.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectApprove=subjectApprove.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodyApprove==null)
    			{
    				messageBodyApprove = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
    				messageBodyApprove=messageBodyApprove.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodyApprove=messageBodyApprove.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodyApprove=messageBodyApprove.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			
    			if(subjectUploadToIris==null)
    			{
    				subjectUploadToIris = TemplateUtil.getTemplate("reportSummary.upload.on.iris.subject", JiveGlobals.getLocale());
    				subjectUploadToIris=subjectUploadToIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectUploadToIris=subjectUploadToIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodyUploadToIris==null)
    			{
    				messageBodyUploadToIris = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
    				messageBodyUploadToIris=messageBodyUploadToIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodyUploadToIris=messageBodyUploadToIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodyUploadToIris=messageBodyUploadToIris.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			if(subjectUploadToCPSIDatabase==null)
    			{
    				subjectUploadToCPSIDatabase = TemplateUtil.getTemplate("reportSummary.upload.on.c.psi.database.subject", JiveGlobals.getLocale());
    				subjectUploadToCPSIDatabase=subjectUploadToCPSIDatabase.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectUploadToCPSIDatabase=subjectUploadToCPSIDatabase.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodyUploadToCPSIDatabase==null)
    			{
    				messageBodyUploadToCPSIDatabase = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.c.psi.database.htmlBody", JiveGlobals.getLocale());
    				messageBodyUploadToCPSIDatabase=messageBodyUploadToCPSIDatabase.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodyUploadToCPSIDatabase=messageBodyUploadToCPSIDatabase.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodyUploadToCPSIDatabase=messageBodyUploadToCPSIDatabase.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			
    			if(subjectSummaryUploadIris==null)
    			{
    				subjectSummaryUploadIris = TemplateUtil.getTemplate("reportSummary.summary.upload.on.iris.subject", JiveGlobals.getLocale());
    				subjectSummaryUploadIris=subjectSummaryUploadIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectSummaryUploadIris=subjectSummaryUploadIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodySummaryUploadIris==null)
    			{
    				messageBodySummaryUploadIris = TemplateUtil.getHtmlEscapedTemplate("reportSummary.summary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
    				messageBodySummaryUploadIris=messageBodySummaryUploadIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodySummaryUploadIris=messageBodySummaryUploadIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodySummaryUploadIris=messageBodySummaryUploadIris.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			List<User> externalAgencyUsers = Lists.newArrayList(synchroUtils.getAwardedExternalAgencyUsers(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID));
    			if(externalAgencyUsers!=null && externalAgencyUsers.size()>0)
    			{
    				awardedExternalAgency = externalAgencyUsers.get(0).getEmail();
    			}
    			HashSet<User> aboveMarketUsers = (HashSet<User>) synchroUtils.getAboveMarketProjectContact(projectID);
    			for(User user: aboveMarketUsers)
    			{
    				if(aboveMarketProjectContact!=null && aboveMarketProjectContact.length()>0)
    				{
    					aboveMarketProjectContact = aboveMarketProjectContact+","+user.getEmail();
    				}
    				else
    				{
    					aboveMarketProjectContact = user.getEmail();
    				}
    			}
    			HashSet<User> adminUsers = (HashSet<User>) synchroUtils.getSynchroAdminUsers();
    			for(User user: adminUsers)
    			{
    				if(synchroAdminUsers!=null && synchroAdminUsers.length()>0)
    				{
    					synchroAdminUsers = synchroAdminUsers+","+((user.getEmail() != null)?user.getEmail():"");
    				}
    				else
    				{
    					synchroAdminUsers = (user.getEmail() != null)?user.getEmail():"" ;
    				}
    			}
    			StringBuffer legalAppEndMarkets = new StringBuffer();
    			
    			//TODO Kanwar
    			Boolean legalPending = false;
    			for(ReportSummaryInitiation rSInitiation:reportSummaryList)
    			{
    				if(rSInitiation.getFullReport() && rSInitiation.getSummaryForIRIS() && rSInitiation.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
    				{    					
    					if(rSInitiation.getLegalApproval() && rSInitiation.getLegalApprover()!=null && !rSInitiation.getLegalApprover().equalsIgnoreCase(""))
    					{
    						legalPending = false;
    					}
    					else
    					{
    						legalAppEndMarkets.append(" - "+SynchroGlobal.getEndMarkets().get(Integer.valueOf(rSInitiation.getEndMarketID()+"")));
    						legalAppEndMarkets.append("<br>");
    						legalPending = true;
    					}
    					
    					if(legalPending)
    						legalApprovalRequired = true;
    				}
    			}
    			
    			requiredLegalApproverEndMarkets = legalAppEndMarkets.toString();
    			
    			legalAppEndMarkets = new StringBuffer();
    			
    			for(ReportSummaryInitiation rSInitiation:reportSummaryList)
    			{
    				if(endMarketId!=rSInitiation.getEndMarketID())
    				{
	    				if(allEndMarketLegalApprovalSaved && rSInitiation.getLegalApproval() && rSInitiation.getLegalApprover()!=null && !rSInitiation.getLegalApprover().equalsIgnoreCase(""))
	                    {
	                    	allEndMarketLegalApprovalSaved = true;
	                    }
	                    else
	                    {
	                    	//https://www.svn.sourcen.com/issues/18746
	                    	if(!(rSInitiation.getLegalApproval() && rSInitiation.getLegalApprover()!=null && !rSInitiation.getLegalApprover().equalsIgnoreCase("")))
	                    	{
		                    	allEndMarketLegalApprovalSaved = false;
		                    	if(legalAppEndMarkets.length()>0)
		                    	{
		                    		if(rSInitiation.getEndMarketID()==SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
		                    		{
		                    			legalAppEndMarkets.append("<br> - Above Market");
		                    		}
		                    		else
		                    		{
		                    			legalAppEndMarkets.append("<br> - "+ SynchroGlobal.getEndMarkets().get(Integer.valueOf(rSInitiation.getEndMarketID()+"")));
		                    		}
		                    	}
		                    	else
		                    	{
		                    		if(rSInitiation.getEndMarketID()==SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
		                    		{
		                    			legalAppEndMarkets.append("<br> - Above Market");
		                    		}
		                    		else
		                    		{
		                    			legalAppEndMarkets.append(" - "+SynchroGlobal.getEndMarkets().get(Integer.valueOf(rSInitiation.getEndMarketID()+"")));
		                    		}
		                    	}
	                    	}
	                 
	                    }
    				}
    			}
    			legalApproverEndMarkets = legalAppEndMarkets.toString();
    			
    			List<FundingInvestment> fundingInvestments = this.synchroProjectManager.getProjectInvestments(projectID);
    			if(!isAboveMarket)
        		{
        			for(FundingInvestment fi: fundingInvestments)
        			{
        				if(!fi.getAboveMarket() && fi.getFieldworkMarketID().intValue()==endMarketId.intValue())
        				{
        					endMarketProjectOwner=fi.getProjectContact();
        					endMarketProjectContact=fi.getSpiContact();
        				}
        			}
        			if(endMarketProjectOwner==null)
        			{
        				for(FundingInvestment fi: fundingInvestments)
            			{
            				if(fi.getAboveMarket() && fi.getInvestmentType()==SynchroGlobal.InvestmentType.GlOBAL.getId())
            				{
            					endMarketProjectOwner=fi.getProjectContact();
            					endMarketProjectContact=fi.getProjectContact();
            				}
            			}
        			}
        			if(endMarketProjectOwner==null)
        			{
        				for(FundingInvestment fi: fundingInvestments)
            			{
            				if(fi.getAboveMarket() && fi.getInvestmentType()==SynchroGlobal.InvestmentType.REGION.getId())
            				{
            					endMarketProjectOwner=fi.getProjectContact();
            					endMarketProjectContact=fi.getProjectContact();
            				}
            			}
        			}
        			if(endMarketProjectOwner==null)
        			{
        				for(FundingInvestment fi: fundingInvestments)
            			{
            				if(fi.getAboveMarket() && fi.getInvestmentType()==SynchroGlobal.InvestmentType.AREA.getId())
            				{
            					endMarketProjectOwner=fi.getProjectContact();
            					endMarketProjectContact=fi.getProjectContact();
            				}
            			}
        			}
        			if(endMarketProjectOwner==null)
        			{
        				endMarketProjectOwner= project.getProjectOwner();
        				endMarketProjectContact= project.getProjectOwner();
        			}
        		}
    			//https://www.svn.sourcen.com/issues/19060 - Last point
    			List<ProjectSpecsInitiation> projectSpecsList = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
    			if(projectSpecsList!=null && projectSpecsList.size()>0)
    			{
    				for(ProjectSpecsInitiation projectSpecs: projectSpecsList)
    				{
    					if(projectSpecs.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
    					{
    						if(projectSpecs.getIsApproved()!=1)
    						{
    							Integer psEMStatus = this.synchroProjectManager.getEndMarketStatus(projectID, projectSpecs.getEndMarketID());
        						if(psEMStatus==null || psEMStatus==SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal())
        						{
        							allProjectSpecsArroved=true;
        							
        						}
        						else
        						{
        							allProjectSpecsArroved=false;
        							break;
        						}
    						}
    						
    					}
    					else
    					{
    						allProjectSpecsArroved=true;
    					}
    				}
    			}
    		
        }

        
        // Contenttype check is required to skip the below binding in case odf adding attachments
        if(getRequest().getMethod() == "POST" && !getRequest().getContentType().startsWith("multipart/form-data") && getRequest().getParameter("attachmentId")==null) {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.reportSummaryInitiation);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Project Specs Initiation bean.");
                input();
            }
                   
            // To map the Project Parameters
            binder = new ServletRequestDataBinder(this.synchroToIRIS);
            binder.bind(getRequest());

            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the SynchroToIRIS bean.");
                input();
            }
        }
    }

    public String input() {
      	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	
    	if ((SynchroPermHelper.hasProjectAccessMultiMarket(projectID) && SynchroPermHelper.canViewStage(projectID, 4)) || SynchroPermHelper.canAccessProject(projectID)) {
    		return INPUT;	
    	}
    	else
    	{
    		return UNAUTHORIZED;
    	}	
    }

    public String execute(){

        LOG.info("Save the Project Report Summary Details ...."+reportSummaryInitiation);
        Boolean manFieldsError = false;
      //  if( projectInitiation != null && ribDocument != null){
        if( reportSummaryInitiation != null){
        	reportSummaryInitiation.setProjectID(projectID);
        	
        	// https://svn.sourcen.com/issues/19652
            
        	reportSummaryInitiation.setComments(SynchroUtils.fixBulletPoint(reportSummaryInitiation.getComments()));
        	
        	if(reportSummaryInitiation.getLegalApproval() && reportSummaryInitiation.getLegalApprover()!=null && !reportSummaryInitiation.getLegalApprover().equals(""))
        	{
        		reportSummaryInitiation.setRepSummaryLegalApprovalDate(new Date(System.currentTimeMillis()));
        	}
         
            if(isSave) {
            	reportSummaryInitiation.setCreationBy(getUser().getID());
            	reportSummaryInitiation.setCreationDate(System.currentTimeMillis());
                
            	reportSummaryInitiation.setModifiedBy(getUser().getID());
            	reportSummaryInitiation.setModifiedDate(System.currentTimeMillis());
                
                this.reportSummaryManager.saveReportSummaryDetails(reportSummaryInitiation);

               
            }
            else {
            	reportSummaryInitiation.setModifiedBy(getUser().getID());
            	reportSummaryInitiation.setModifiedDate(System.currentTimeMillis());
            	// https://www.svn.sourcen.com/issues/17668
            	/*if(reportSummaryInitiation.getNeedRevision())
            	{
            		reportSummaryInitiation.setSendForApproval(false);
            		//reportSummaryInitiation.setNeedRevision(false);
            	}*/
                if((reportSummaryInitiation.getComments()!=null && !reportSummaryInitiation.getComments().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.COMMENTS.getId())!=null))
                {
                	// This check is put in case the Report Stage is already completed and as part of email notification window the status should not be changed again.
                	// This is put as part of Quick Fix changes.
                	if(reportSummaryInitiation.getStatus()!=null && reportSummaryInitiation.getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal())
                	{
                		
                	}
                	else
                	{
                		reportSummaryInitiation.setStatus(SynchroGlobal.StageStatus.REPORT_SUMMARY_SAVED.ordinal());
                	}
                }
                else
                {
                	manFieldsError=true;
                }
            	this.reportSummaryManager.updateReportSummaryDetails(reportSummaryInitiation);
            }
            updateSynchroToIRIS();
          //Audit Logs: Report Summary
            SynchroLogUtils.ReportMultiMarketSave(project, reportSummaryInitiation, reportSummaryInitiation_DB, endMarketId);
            
        } else {
            LOG.error("Report Summary Initiation was null  ");
            addActionError("Report Summary Initiation was null.");
        }
        
      //Audit Logs: Report SUmmary SAVE
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID(), endMarketId);  
        
      /*  if(manFieldsError)
        {
        	
        	if(isAboveMarket)
        	{
        		redirectURL="/synchro/report-multi-summary!input.jspa?projectID="+projectID+"&validationError=true";
        	}
        	else
        	{
        		redirectURL="/synchro/report-multi-summary!input.jspa?projectID="+projectID+"&endMarketId="+endMarketId+"&validationError=true";
        		
        	}
        	return "validationError";
        }
        */
        return SUCCESS;
    }
    
    /**
     * This method will update the SynchroToIRIS from the RS screen
     * @return
     */
    
    public String updateSynchroToIRIS(){
    	synchroToIRIS.setCreationBy(getUser().getID());
    	synchroToIRIS.setCreationDate(System.currentTimeMillis());
        
    	synchroToIRIS.setModifiedBy(getUser().getID());
    	synchroToIRIS.setModifiedDate(System.currentTimeMillis());
	 
    	 /*if(StringUtils.isNotBlank(getRequest().getParameter("fieldWorkStartDate"))) {
         this.synchroToIRIS.setFieldWorkStartDate(DateUtils.parse(getRequest().getParameter("fieldWorkStartDate")));
		 }
		 
    	if(StringUtils.isNotBlank(getRequest().getParameter("fieldWorkEndDate"))) {
		     this.synchroToIRIS.setFieldWorkEndDate(DateUtils.parse(getRequest().getParameter("fieldWorkEndDate")));
		 }
		 if(StringUtils.isNotBlank(getRequest().getParameter("reportDate"))) {
		     this.synchroToIRIS.setReportDate(DateUtils.parse(getRequest().getParameter("reportDate")));
		 }
		 
		 if(StringUtils.isNotBlank(getRequest().getParameter("allDocsEnglish")))
		 {
			 synchroToIRIS.setAllDocsEnglish(true);
		 }
		 else
		 {
			 synchroToIRIS.setAllDocsEnglish(false);
		 }
		 if(StringUtils.isNotBlank(getRequest().getParameter("irisDisclaimer")) || StringUtils.isNotBlank(getRequest().getParameter("irisdisclaimer")))
		 {
			 synchroToIRIS.setDisclaimer(true);
		 }
		 else
		 {
			 synchroToIRIS.setDisclaimer(false);
		 }*/
		 if(StringUtils.isNotBlank(getRequest().getParameter("irisSummaryRequired")) && getRequest().getParameter("irisSummaryRequired").equalsIgnoreCase("irisSummaryRequired"))
		 {
			 synchroToIRIS.setIrisSummaryRequired(1);
		 }
		 else if(StringUtils.isNotBlank(getRequest().getParameter("irisSummaryRequired")) && getRequest().getParameter("irisSummaryRequired").equalsIgnoreCase("irisSummaryNotRequired"))
		 {
			 synchroToIRIS.setIrisSummaryRequired(2);
		 }
		 else
		 {
			// synchroToIRIS.setIrisSummaryRequired(null);
			// This check is put in case the Report Stage is already completed and as part of email notification window the status should not be changed again.
	        // This is put as part of Quick Fix changes.
	        if(reportSummaryInitiation.getStatus()!=null && reportSummaryInitiation.getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal())
	        {
	         		
	        }
	        else
	        {
	        	 synchroToIRIS.setIrisSummaryRequired(null);
	        }
		 }
		 
    	this.reportSummaryManager.updateSynchroToIRIS(synchroToIRIS);
    //	SynchroLogUtils.IRISSave(project, synchroToIRIS, synchroToIRIS_DB);
    	
    	 if(synchroToIRIS!=null)
         {
         	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null)
 			{
 				isAllSynchroToIRISFilled=true;
 			}
         	//https://svn.sourcen.com/issues/19891
         	else if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired().intValue()==2 && StringUtils.isNotBlank(synchroToIRIS.getIrisOptionRationale()))
         	{
         		isAllSynchroToIRISFilled=true;
         	}
         	else
         	{
         		isAllSynchroToIRISFilled=false;
         	}
         }
         else
     	{
     		isAllSynchroToIRISFilled=false;
     	}
    	
    	//When all the fields have be filled and the user clicks ‘SAVE’, the checkbox ‘Summary for IRIS’ on the report stage will automatically be checked.
    	if(isAllSynchroToIRISFilled)
    	{
    		this.reportSummaryManager.updateSummaryForIris(projectID, 1);
    	}
    	else
    	{
    		this.reportSummaryManager.updateSummaryForIris(projectID, null);
    	}
    	
    	
    	return SUCCESS;
    }
    
    public String updateSynchroToIRISOLD(){
    	synchroToIRIS.setCreationBy(getUser().getID());
    	synchroToIRIS.setCreationDate(System.currentTimeMillis());
        
    	synchroToIRIS.setModifiedBy(getUser().getID());
    	synchroToIRIS.setModifiedDate(System.currentTimeMillis());
	 
    	 if(StringUtils.isNotBlank(getRequest().getParameter("fieldWorkStartDate"))) {
         this.synchroToIRIS.setFieldWorkStartDate(DateUtils.parse(getRequest().getParameter("fieldWorkStartDate")));
		 }
		 
    	if(StringUtils.isNotBlank(getRequest().getParameter("fieldWorkEndDate"))) {
		     this.synchroToIRIS.setFieldWorkEndDate(DateUtils.parse(getRequest().getParameter("fieldWorkEndDate")));
		 }
		 if(StringUtils.isNotBlank(getRequest().getParameter("reportDate"))) {
		     this.synchroToIRIS.setReportDate(DateUtils.parse(getRequest().getParameter("reportDate")));
		 }
		 
		 if(StringUtils.isNotBlank(getRequest().getParameter("allDocsEnglish")))
		 {
			 synchroToIRIS.setAllDocsEnglish(true);
		 }
		 else
		 {
			 synchroToIRIS.setAllDocsEnglish(false);
		 }
		 if(StringUtils.isNotBlank(getRequest().getParameter("irisDisclaimer")) || StringUtils.isNotBlank(getRequest().getParameter("irisdisclaimer")))
		 {
			 synchroToIRIS.setDisclaimer(true);
		 }
		 else
		 {
			 synchroToIRIS.setDisclaimer(false);
		 }
		 if(StringUtils.isNotBlank(getRequest().getParameter("irisSummaryRequired")) && getRequest().getParameter("irisSummaryRequired").equalsIgnoreCase("irisSummaryRequired"))
		 {
			 synchroToIRIS.setIrisSummaryRequired(1);
		 }
		 else if(StringUtils.isNotBlank(getRequest().getParameter("irisSummaryRequired")) && getRequest().getParameter("irisSummaryRequired").equalsIgnoreCase("irisSummaryNotRequired"))
		 {
			 synchroToIRIS.setIrisSummaryRequired(2);
		 }
		 else
		 {
			 synchroToIRIS.setIrisSummaryRequired(null);
		 }
		 
    	this.reportSummaryManager.updateSynchroToIRIS(synchroToIRIS);
    	SynchroLogUtils.IRISSave(project, synchroToIRIS, synchroToIRIS_DB);
    	
    	 if(synchroToIRIS!=null)
         {
         	if(StringUtils.isNotBlank(synchroToIRIS.getProjectDesc()) &&
         			StringUtils.isNotBlank(synchroToIRIS.getResearchObjective()) &&
         			StringUtils.isNotBlank(synchroToIRIS.getBizQuestion()) && 
         			StringUtils.isNotBlank(synchroToIRIS.getActionStandard()) &&
         			StringUtils.isNotBlank(synchroToIRIS.getConclusions()) &&
         			StringUtils.isNotBlank(synchroToIRIS.getKeyFindings()) && 
         			StringUtils.isNotBlank(synchroToIRIS.getRespondentType()) && 
         			StringUtils.isNotBlank(synchroToIRIS.getSampleSize()) && 
         		// https://svn.sourcen.com/issues/19987
         		//	synchroToIRIS.getFieldWorkStartDate()!=null && 
         		//	synchroToIRIS.getFieldWorkEndDate()!=null && 
         			synchroToIRIS.getReportDate()!=null &&
         			(synchroToIRIS.getSummaryWrittenBy()!=null && synchroToIRIS.getSummaryWrittenBy()>0) && 
         		//	StringUtils.isNotBlank(synchroToIRIS.getRelatedStudy()) &&
         			StringUtils.isNotBlank(synchroToIRIS.getTags()) && 
         			synchroToIRIS.getAllDocsEnglish() && 
         			synchroToIRIS.getDisclaimer() && 
         			(synchroToIRIS.getBrand()!=null && synchroToIRIS.getBrand() >0))
         			{
         				isAllSynchroToIRISFilled=true;
         			}
         	else if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired()==2 && StringUtils.isNotBlank(synchroToIRIS.getIrisOptionRationale()))
         	{
         		isAllSynchroToIRISFilled=true;
         	}
         	else
         	{
         		isAllSynchroToIRISFilled=false;
         	}
         }
         else
     	{
     		isAllSynchroToIRISFilled=false;
     	}
    	
    	//When all the fields have be filled and the user clicks ‘SAVE’, the checkbox ‘Summary for IRIS’ on the report stage will automatically be checked.
    	if(isAllSynchroToIRISFilled)
    	{
    		this.reportSummaryManager.updateSummaryForIris(projectID, 1);
    	}
    	else
    	{
    		this.reportSummaryManager.updateSummaryForIris(projectID, null);
    	}
    	
    	
    	return SUCCESS;
    }
    
    /**
	 * This method will perform the notification activities for the To Do List Actions for each stage.
	 * 
	 */
	public String sendNotification() {
		
		//Email Notification TimeStamp Storage
    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
    	emailNotBean.setProjectID(projectID);
    	emailNotBean.setEndmarketID(endMarketId);
       	emailNotBean.setStageID(SynchroConstants.RS_STAGE);
       	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
		
		if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND FOR APPROVAL"))
		{
			reportSummaryManager.updateSendForApproval(projectID,endMarketId,1);
			//reportSummaryManager.updateSendForApproval(projectID,1);
			emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
			
			//Audit Logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), getText("logger.project.reportsummary.sendapproval"), project.getName(), 
            												project.getProjectID(), getUser().getID());
            
          //Audit Logs: Notification
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.reportsummary.notf.sendapproval");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
		if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NEEDS REVISION"))
		{
			//reportSummaryManager.updateSendForApproval(projectID,endMarketId,null);
			reportSummaryManager.updateNeedRevision(projectID,endMarketId);
			emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
			
			//Audit Logs: Notification
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.reportsummary.notf.needrevision");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
		
		if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("UPLOAD TO IRIS"))
		{
			reportSummaryManager.updateUploadToIRIS(projectID,endMarketId,1);
			
			//Audit Logs: Notification
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.reportsummary.notf.uploadiris");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
			
		}
		
		if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("UPLOAD TO C-PSI DATABASE"))
		{
			reportSummaryManager.updateUploadToCPSIDatabase(projectID,endMarketId,1);
			
			//Audit Logs: Notification
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.reportsummary.notf.uploadcpsi");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
			
		}
		
		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
		email = handleAttachments(email);
		stageManager.sendNotification(getUser(),email);
		
		
    	emailNotBean.setEmailDesc(subject);
    	
    	emailNotBean.setEmailSubject(subject);
    	emailNotBean.setEmailSender(getUser().getEmail());
    	emailNotBean.setEmailRecipients(recipients);
    	
    	emailNotificationManager.saveDetails(emailNotBean);
    	
		
		if(approve!=null && approve.equals("approve"))
		{
			this.reportSummaryManager.approveSPI(getUser(),projectID,endMarketId);
			// This is done in case the second option is clicked (Summary for IRIS not required) then the project should get completed.
			if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired()==2 && StringUtils.isNotBlank(synchroToIRIS.getIrisOptionRationale()))
			{
				this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketId,SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
				
				 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketId);
				 ProjectEvaluationInitiation projectEvaluationInitiation = null;
				 if( initiationList != null && initiationList.size() > 0) {
					 projectEvaluationInitiation = initiationList.get(0);
					// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
		        		//if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
		        		if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfIM()>0 && projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation.getAgencyPerfLM()>0)
		                {
		        			synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal());
		                }
		        		else
		        		{
		        			// Update the project status to COMPLETED once the Report Summary is completed.
		    				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
		        		}
	            } 
				 else
				 {
					// Update the project status to COMPLETED once the Report Summary is completed.
					synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
				 }
			}
			
			//Audit Logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), getText("logger.project.reportsummary.approve"), project.getName(), 
            												project.getProjectID(), getUser().getID());
            
          //Audit Logs: Notification
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.reportsummary.notf.approve");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
		if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SUMMARY UPLOADED TO IRIS"))
		{
			this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketId,SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
			
			 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketId);
			 ProjectEvaluationInitiation projectEvaluationInitiation = null;
			 if( initiationList != null && initiationList.size() > 0) {
				 projectEvaluationInitiation = initiationList.get(0);
				// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
	        		//if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
	        		if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfIM()>0 && projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation.getAgencyPerfLM()>0)
	                {
	        			synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal());
	                }
	        		else
	        		{
	        			// Update the project status to COMPLETED once the Report Summary is completed.
	    				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
	        		}
            } 
			 else
			 {
				// Update the project status to COMPLETED once the Report Summary is completed.
				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
			 }
			 
			//Audit Logs: Notification
	            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
	            String description = getText("logger.project.reportsummary.notf.uploadiris");
		                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
		        									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
		        											project.getProjectID(), getUser().getID(), userNameList);
		}
		
		
		return SUCCESS;
	}
	
	/**
     * This method will move the stage from Report to next stage
     * @return
     */
    public String moveToNextStage() 
    {
    	LOG.info("Inside moveToNextStage RS MM Action--- "+ projectID); 
		

		this.reportSummaryManager.approveSPI(getUser(),projectID,endMarketId);
		// This is done in case the second option is clicked (Summary for IRIS not required) then the project should get completed.
		/*if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired()==2 && StringUtils.isNotBlank(synchroToIRIS.getIrisOptionRationale()))
		{
			this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketId,SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
			
			 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketId);
			 ProjectEvaluationInitiation projectEvaluationInitiation = null;
			 if( initiationList != null && initiationList.size() > 0) {
				 projectEvaluationInitiation = initiationList.get(0);
				// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
	        		//if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
	        		if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfIM()>0 && projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation.getAgencyPerfLM()>0)
	                {
	        			synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal());
	                }
	        		else
	        		{
	        			// Update the project status to COMPLETED once the Report Summary is completed.
	    				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
	        		}
            } 
			 else
			 {
				// Update the project status to COMPLETED once the Report Summary is completed.
				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
			 }
		}*/
		
		this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketId,SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
		
		 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketId);
		 ProjectEvaluationInitiation projectEvaluationInitiation = null;
		 if( initiationList != null && initiationList.size() > 0) {
			 projectEvaluationInitiation = initiationList.get(0);
			// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
       		//if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
       			if((projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfIM()>0)
                        || (projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation.getAgencyPerfLM()>0) 
                        || (projectEvaluationInitiation.getAgencyPerfFA()!=null && projectEvaluationInitiation.getAgencyPerfFA()>0))
               {
       			synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal());
               }
       		else
       		{
       			// Update the project status to COMPLETED once the Report Summary is completed.
   				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
       		}
       } 
		 else
		 {
			// Update the project status to COMPLETED once the Report Summary is completed.
			synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED.ordinal());
		 }
		
    	//return SUCCESS;
		 //  Clicking on ‘Move to the next stage’ will navigate user to the next stage 
	       return "moveToNextStage";
    }
	public String addAttachment() throws UnsupportedEncodingException {
       
         Map<String, Object> result = new HashMap<String, Object>();
        try
        {
        	reportSummaryManager.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID());
        	
        	//Add Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment" + "- " +attachFileFileName;
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
            										SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
            												project.getProjectID(), getUser().getID(), endMarketId);
        }
        catch (AttachmentException ae) {
            result.put("success", false);
            result.put("message", "Unable to upload file.");
        } catch (UnauthorizedException ue) {
            result.put("success", false);
            result.put("message", "Unauthorized.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return SUCCESS;
    }
	public String removeAttachment() throws UnsupportedEncodingException {
      
		try
		{
			reportSummaryManager.removeAttachment(attachmentId);
			
			//https://www.svn.sourcen.com//issues/19256
			
            attachmentMap = this.reportSummaryManager.getDocumentAttachment(projectID, endMarketId);
            
			if(!attachmentMap.containsKey(SynchroAttachmentObject.FULL_REPORT.getId()))
			{
				reportSummaryManager.updateFullReport(projectID, endMarketId, null);
			}
		/*	if(!attachmentMap.containsKey(SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()))
			{
				reportSummaryManager.updateSummaryForIris(projectID, endMarketId, null);
			}
			*/
			//Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName +" deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID(), endMarketId);
            
		}
		catch (Exception e) {
            LOG.error("Exception while removing attachment Id --"+ attachmentId);
        }
		 return SUCCESS;
    }
	
	public String exportToWordPIB()
    {
    	XWPFDocument document = new XWPFDocument();   
        XWPFParagraph tmpParagraph = document.createParagraph();   
        XWPFRun tmpRun = tmpParagraph.createRun();   
        tmpRun.setText("HELLO");   
        tmpRun.setFontSize(18);   
        try
        {
	        response.setContentType("application/msword");
	        response.addHeader("Content-Disposition", "attachment; filename=PIBWord.docx");
	        document.write(response.getOutputStream());
        }
        catch (IOException e) {
    	    e.printStackTrace();
    	}
        
        return null;
    }
	public String exportToPDFPIB()
    {
		try
		{
			Document document=new Document();
			response.setContentType("application/pdf");
		    response.addHeader("Content-Disposition", "attachment; filename=PIBPDF.pdf");
			PdfWriter.getInstance(document,response.getOutputStream()); 
			document.open(); 
			document.add(new Paragraph("Welcome to Page 1.1"));
			document.close(); 
			
		       // document.write(response.getOutputStream());
	    }
        catch (IOException e) {
    	    e.printStackTrace();
    	}
		catch (DocumentException e) {
    	    e.printStackTrace();
    	}
        
        return null;
    }
	
    
   

    public void setSynchroProjectManager(final ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

 
    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

    public boolean isSave() {
        return isSave;
    }

    public void setSave(final boolean save) {
        isSave = save;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }
  
	public boolean isEditStage() {
		return editStage;
	}

	public void setEditStage(boolean editStage) {
		this.editStage = editStage;
	}

	public String getNotificationTabId() {
		return notificationTabId;
	}

	public void setNotificationTabId(String notificationTabId) {
		this.notificationTabId = notificationTabId;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public String getApprove() {
		return approve;
	}

	public void setApprove(String approve) {
		this.approve = approve;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public List<SynchroStageToDoListBean> getStageToDoList() {
		return stageToDoList;
	}

	public void setStageToDoList(List<SynchroStageToDoListBean> stageToDoList) {
		this.stageToDoList = stageToDoList;
	}

	public Integer getStageId() {
		return stageId;
	}

	public void setStageId(Integer stageId) {
		this.stageId = stageId;
	}

	

	
	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

	public Map<String, Map<String, String>> getStageApprovers() {
		return stageApprovers;
	}

	public void setStageApprovers(Map<String, Map<String, String>> stageApprovers) {
		this.stageApprovers = stageApprovers;
	}
	
	public Map<String, String> getApprovers() {
		return approvers;
	}

	public AttachmentHelper getAttachmentHelper() {
		return attachmentHelper;
	}

	public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
		this.attachmentHelper = attachmentHelper;
	}

	public File getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(File attachFile) {
		this.attachFile = attachFile;
	}

	public String getAttachFileContentType() {
		return attachFileContentType;
	}

	public void setAttachFileContentType(String attachFileContentType) {
		this.attachFileContentType = attachFileContentType;
	}

	public String getAttachFileFileName() {
		return attachFileFileName;
	}

	public void setAttachFileFileName(String attachFileFileName) {
		this.attachFileFileName = attachFileFileName;
	}

	public Long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public Long getFieldCategoryId() {
		return fieldCategoryId;
	}

	public void setFieldCategoryId(Long fieldCategoryId) {
		this.fieldCategoryId = fieldCategoryId;
	}

	
	public Long getEndMarketId() {
		return endMarketId;
	}

	public void setEndMarketId(Long endMarketId) {
		this.endMarketId = endMarketId;
	}

	

	public Map<Integer, List<AttachmentBean>> getAttachmentMap() {
		return attachmentMap;
	}

	public void setAttachmentMap(Map<Integer, List<AttachmentBean>> attachmentMap) {
		this.attachmentMap = attachmentMap;
	}

	public ReportSummaryManager getReportSummaryManager() {
		return reportSummaryManager;
	}

	public void setReportSummaryManager(ReportSummaryManager reportSummaryManager) {
		this.reportSummaryManager = reportSummaryManager;
	}

	public ReportSummaryInitiation getReportSummaryInitiation() {
		return reportSummaryInitiation;
	}

	public void setReportSummaryInitiation(
			ReportSummaryInitiation reportSummaryInitiation) {
		this.reportSummaryInitiation = reportSummaryInitiation;
	}

	public PIBManager getPibManager() {
		return pibManager;
	}

	public void setPibManager(PIBManager pibManager) {
		this.pibManager = pibManager;
	}

	public ProjectEvaluationManager getProjectEvaluationManager() {
		return projectEvaluationManager;
	}

	public void setProjectEvaluationManager(
			ProjectEvaluationManager projectEvaluationManager) {
		this.projectEvaluationManager = projectEvaluationManager;
	}

	public Map<Long, Long> getAttachmentUser() {
		return attachmentUser;
	}

	public void setAttachmentUser(Map<Long, Long> attachmentUser) {
		this.attachmentUser = attachmentUser;
	}

	public Boolean getShowMandatoryFieldsError() {
		return showMandatoryFieldsError;
	}

	public void setShowMandatoryFieldsError(Boolean showMandatoryFieldsError) {
		this.showMandatoryFieldsError = showMandatoryFieldsError;
	}

	public Boolean getIsAboveMarket() {
		return isAboveMarket;
	}

	public void setIsAboveMarket(Boolean isAboveMarket) {
		this.isAboveMarket = isAboveMarket;
	}

	public List<ReportSummaryInitiation> getReportSummaryList() {
		return reportSummaryList;
	}

	public void setReportSummaryList(List<ReportSummaryInitiation> reportSummaryList) {
		this.reportSummaryList = reportSummaryList;
	}

	public Boolean getAllEndMarketSaved() {
		return allEndMarketSaved;
	}

	public void setAllEndMarketSaved(Boolean allEndMarketSaved) {
		this.allEndMarketSaved = allEndMarketSaved;
	}

	public String getSubjectSendForApproval() {
		return subjectSendForApproval;
	}

	public void setSubjectSendForApproval(String subjectSendForApproval) {
		this.subjectSendForApproval = subjectSendForApproval;
	}

	public String getMessageBodySendForApproval() {
		return messageBodySendForApproval;
	}

	public void setMessageBodySendForApproval(String messageBodySendForApproval) {
		this.messageBodySendForApproval = messageBodySendForApproval;
	}

	public String getSubjectNeedsRevision() {
		return subjectNeedsRevision;
	}

	public void setSubjectNeedsRevision(String subjectNeedsRevision) {
		this.subjectNeedsRevision = subjectNeedsRevision;
	}

	public String getMessageBodyNeedsRevision() {
		return messageBodyNeedsRevision;
	}

	public void setMessageBodyNeedsRevision(String messageBodyNeedsRevision) {
		this.messageBodyNeedsRevision = messageBodyNeedsRevision;
	}

	public String getSubjectSendToProjectContact() {
		return subjectSendToProjectContact;
	}

	public void setSubjectSendToProjectContact(String subjectSendToProjectContact) {
		this.subjectSendToProjectContact = subjectSendToProjectContact;
	}

	public String getMessageBodySendToProjectContact() {
		return messageBodySendToProjectContact;
	}

	public void setMessageBodySendToProjectContact(
			String messageBodySendToProjectContact) {
		this.messageBodySendToProjectContact = messageBodySendToProjectContact;
	}

	public String getSubjectApprove() {
		return subjectApprove;
	}

	public void setSubjectApprove(String subjectApprove) {
		this.subjectApprove = subjectApprove;
	}

	public String getMessageBodyApprove() {
		return messageBodyApprove;
	}

	public void setMessageBodyApprove(String messageBodyApprove) {
		this.messageBodyApprove = messageBodyApprove;
	}

	public String getSubjectUploadToIris() {
		return subjectUploadToIris;
	}

	public void setSubjectUploadToIris(String subjectUploadToIris) {
		this.subjectUploadToIris = subjectUploadToIris;
	}

	public String getMessageBodyUploadToIris() {
		return messageBodyUploadToIris;
	}

	public void setMessageBodyUploadToIris(String messageBodyUploadToIris) {
		this.messageBodyUploadToIris = messageBodyUploadToIris;
	}

	public String getSubjectUploadToCPSIDatabase() {
		return subjectUploadToCPSIDatabase;
	}

	public void setSubjectUploadToCPSIDatabase(String subjectUploadToCPSIDatabase) {
		this.subjectUploadToCPSIDatabase = subjectUploadToCPSIDatabase;
	}

	public String getMessageBodyUploadToCPSIDatabase() {
		return messageBodyUploadToCPSIDatabase;
	}

	public void setMessageBodyUploadToCPSIDatabase(
			String messageBodyUploadToCPSIDatabase) {
		this.messageBodyUploadToCPSIDatabase = messageBodyUploadToCPSIDatabase;
	}

	public String getSubjectSummaryUploadIris() {
		return subjectSummaryUploadIris;
	}

	public void setSubjectSummaryUploadIris(String subjectSummaryUploadIris) {
		this.subjectSummaryUploadIris = subjectSummaryUploadIris;
	}

	public String getMessageBodySummaryUploadIris() {
		return messageBodySummaryUploadIris;
	}

	public void setMessageBodySummaryUploadIris(String messageBodySummaryUploadIris) {
		this.messageBodySummaryUploadIris = messageBodySummaryUploadIris;
	}

	public SynchroUtils getSynchroUtils() {
		return synchroUtils;
	}

	public void setSynchroUtils(SynchroUtils synchroUtils) {
		this.synchroUtils = synchroUtils;
	}

	public String getAboveMarketProjectContact() {
		return aboveMarketProjectContact;
	}

	public void setAboveMarketProjectContact(String aboveMarketProjectContact) {
		this.aboveMarketProjectContact = aboveMarketProjectContact;
	}

	public String getAwardedExternalAgency() {
		return awardedExternalAgency;
	}

	public void setAwardedExternalAgency(String awardedExternalAgency) {
		this.awardedExternalAgency = awardedExternalAgency;
	}

	public String getSynchroAdminUsers() {
		return synchroAdminUsers;
	}

	public void setSynchroAdminUsers(String synchroAdminUsers) {
		this.synchroAdminUsers = synchroAdminUsers;
	}

	public Boolean getAllEndMarketLegalApprovalSaved() {
		return allEndMarketLegalApprovalSaved;
	}

	public void setAllEndMarketLegalApprovalSaved(
			Boolean allEndMarketLegalApprovalSaved) {
		this.allEndMarketLegalApprovalSaved = allEndMarketLegalApprovalSaved;
	}

	public String getLegalApproverEndMarkets() {
		return legalApproverEndMarkets;
	}

	public void setLegalApproverEndMarkets(String legalApproverEndMarkets) {
		this.legalApproverEndMarkets = legalApproverEndMarkets;
	}

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}

	public String getRequiredLegalApproverEndMarkets() {
		return requiredLegalApproverEndMarkets;
	}

	public Long getEndMarketProjectOwner() {
		return endMarketProjectOwner;
	}

	public void setEndMarketProjectOwner(Long endMarketProjectOwner) {
		this.endMarketProjectOwner = endMarketProjectOwner;
	}

	public Long getEndMarketProjectContact() {
		return endMarketProjectContact;
	}

	public void setEndMarketProjectContact(Long endMarketProjectContact) {
		this.endMarketProjectContact = endMarketProjectContact;
	}

	public Integer getEndMarketStatus() {
		return endMarketStatus;
	}

	public void setEndMarketStatus(Integer endMarketStatus) {
		this.endMarketStatus = endMarketStatus;
	}

	public Boolean getAllProjectSpecsArroved() {
		return allProjectSpecsArroved;
	}

	public void setAllProjectSpecsArroved(Boolean allProjectSpecsArroved) {
		this.allProjectSpecsArroved = allProjectSpecsArroved;
	}

	public ProjectSpecsManager getProjectSpecsManager() {
		return projectSpecsManager;
	}

	public void setProjectSpecsManager(ProjectSpecsManager projectSpecsManager) {
		this.projectSpecsManager = projectSpecsManager;
	}	
	private EmailMessage handleAttachments(EmailMessage email)
	{		
		if(mailAttachment!=null && mailAttachment.length>0)
		{
			try {
				    for(int i=0; i<mailAttachment.length; i++)
				    {
				    	InputStream fileInputStream = new FileInputStream(mailAttachment[i]);
				        email.addAttachment(new InputStreamDataSource(mailAttachmentFileName[i], mailAttachmentContentType[i], fileInputStream));
				        fileInputStream.close();
				    }
			} catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
			} catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
			} catch (Exception e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
			}
		}
		return email;
	}
	
	
	public File[] getMailAttachment() {
		return mailAttachment;
	}

	public void setMailAttachment(File[] mailAttachment) {
		this.mailAttachment = mailAttachment;
	}

	public String[] getMailAttachmentFileName() {
		return mailAttachmentFileName;
	}

	public void setMailAttachmentFileName(String[] mailAttachmentFileName) {
		this.mailAttachmentFileName = mailAttachmentFileName;
	}

	public String[] getMailAttachmentContentType() {
		return mailAttachmentContentType;
	}

	public void setMailAttachmentContentType(String[] mailAttachmentContentType) {
		this.mailAttachmentContentType = mailAttachmentContentType;
	}

	public SynchroToIRIS getSynchroToIRIS() {
		return synchroToIRIS;
	}

	public void setSynchroToIRIS(SynchroToIRIS synchroToIRIS) {
		this.synchroToIRIS = synchroToIRIS;
	}

	public Boolean getIsAllSynchroToIRISFilled() {
		return isAllSynchroToIRISFilled;
	}

	public void setIsAllSynchroToIRISFilled(Boolean isAllSynchroToIRISFilled) {
		this.isAllSynchroToIRISFilled = isAllSynchroToIRISFilled;
	}

	public List<Long> getEmIds() {
		return emIds;
	}

	public void setEmIds(List<Long> emIds) {
		this.emIds = emIds;
	}

	public Long getAttachmentFieldID() {
		return attachmentFieldID;
	}

	public void setAttachmentFieldID(Long attachmentFieldID) {
		this.attachmentFieldID = attachmentFieldID;
	}
	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}
	
}
