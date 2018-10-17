package com.grail.synchro.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.google.common.collect.Lists;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectSpecsReporting;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ProposalReporting;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
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
import com.jivesoftware.base.UserNotFoundException;
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
 */
public class ProjectSpecsMultiMarketAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ProjectSpecsMultiMarketAction.class);
    //Spring Managers
    private ProjectSpecsManager projectSpecsManager;
    private ProjectManager synchroProjectManager;
    private ReportSummaryManager reportSummaryManager;
    private PIBManager pibManager;
    //Form related fields
    private ProjectSpecsInitiation projectSpecsInitiation;
    private Project project;
    private Long projectID;
     
    private ProjectSpecsReporting projectSpecsReporting;
    private ProjectSpecsEndMarketDetails projectSpecsEMDetails;
    private String attachmentName;
    
    private ProjectSpecsReporting projectSpecsReporting_DB;
    private ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB;
    private List<EndMarketInvestmentDetail> endMarketDetails_DB;
    private ProjectInitiation projectInitiation_DB;
    private ProjectSpecsInitiation projectSpecsInitiation_DB;

    
    private boolean isSave;
    
    private boolean editStage;
   
    private String notificationTabId;

	private String redirectURL;
	private String approve;
	private String recipients;
	private String subject;
	private String messageBody;
	
	List<SynchroStageToDoListBean> stageToDoList= new ArrayList<SynchroStageToDoListBean>();
	private Integer stageId;
		
	private Map<String, String> approvers = new LinkedHashMap<String, String>();
	private StageManager stageManager;
	
    private AttachmentHelper attachmentHelper;
    private Long attachmentFieldID;
    private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long fieldCategoryId;
    //private List<Long> endMarketIds;
 //   private List<EndMarketInvestmentDetail> endMarketDetails;
    private Long endMarketId;
    // This field will contain the updated SingleMarketId in case the End market is changed
    private Long updatedSingleMarketId;
    //This map will contain the list of attachments for each field
    private Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();
    
	// This will contain  the Approvers data for the Checklist Tab
	Map<String,Map<String,String>> stageApprovers = new LinkedHashMap<String, Map<String,String>>();
	// This will containg the Agency Users
	Map<String,Long> agencyMap = new LinkedHashMap<String, Long>();
	DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private Map<Long,Long> attachmentUser;
	// This will containg the Methodology Waiver Related fields
	//private PSMethodologyWaiver psMethodologyWaiver;
	
	// This will containg the Methodology Waiver Related fields
    private PIBMethodologyWaiver pibMethodologyWaiver;

	// This field will check whether the user click on PS Methodology Waiver is Approve or Reject button or Send for Information or Request more information 
	private String methodologyWaiverAction;
	private Long projSpecsEndMarketId;
	private Boolean showMandatoryFieldsError;
	private Boolean isAboveMarket;
	private List<ProjectSpecsEndMarketDetails> psEMList;
	private List<ProjectSpecsEndMarketDetails> psEMList_DB;
	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
	// This flag is used to enable the Notification buttons on the PIB Stage by checking the status of individual End Market.
    private Boolean allEndMarketSaved;
    private String subjectSendToProjOwner;
	private String messageBodySendToProjOwner;
	private String awardedExternalAgency;
	
	private String subjectReqForClarification;
	private String messageBodyReqForClarification;
	
	private String subjectFinalApproval;
	private String messageBodyFinalApproval;
	
	private SynchroUtils synchroUtils;
	private String aboveMarketProjectContact;
	private List<FundingInvestment> fundingInvestments;
	private List<Long> emIds = new ArrayList<Long>();
	
	 // This flag is used to check whether the Legal Approval are saved on all the End Markets or not.
    private Boolean allEndMarketLegalApprovalSaved = true; 
    private String legalApproverEndMarkets;
    
    private Date projectEndDate;
    private Date projectEndDateLatest;
	
    // These variables will have the ProjectOwner and Project Contact values for the endMarkets other than Above Market
    private Long endMarketProjectOwner;
    private Long endMarketProjectContact;
    
    private EmailNotificationManager emailNotificationManager;
    
    // This variable will tell whether the above Market has been approved or not.
    private Boolean aboveMarketApproved = false; 
    
    // This variable will contain the status of End Market
    private Integer endMarketStatus;
    
    private ProposalManager proposalManager;
    
    private List<Long> updatedEndMarkets;
    
    private File[] mailAttachment;
	private String[] mailAttachmentFileName;
	private String[] mailAttachmentContentType;
	
    private BigDecimal aboveMarketFinalCost;
    private Integer aboveMarketFinalCostType;
    private BigDecimal aboveMarketFinalCost_DB;
    private Integer aboveMarketFinalCostType_DB;
    
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
              /*  if(validationError!=null && validationError.equals("true"))
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
                	 redirectURL="/synchro/project-multi-specs!input.jspa?projectID="+projectID+"&endMarketId="+endMarketId;
                }
                else
                {
                	isAboveMarket=true;
                	// To add the Above Market Endmarket id
                	//endMarketId=endMarketDetails.get(0).getEndMarketID();
                	endMarketId = SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID;
                	redirectURL="/synchro/project-multi-specs!input.jspa?projectID="+projectID;
                	
                }
                fundingInvestments = this.synchroProjectManager.getProjectInvestments(projectID);
               // endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
             //   endMarketDetails = this.synchroProjectManager.getEndMarketDetails(projectID);
             	
                //List<ProjectSpecsInitiation> initiationList = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
                List<ProjectSpecsInitiation> initiationList = this.projectSpecsManager.getProjectSpecsInitiation(projectID,endMarketId);
                projSpecsEndMarketId = initiationList.get(0).getEndMarketID();
                
              //Audit Logs
                
                List<ProjectSpecsInitiation> initiationList_DB = this.projectSpecsManager.getProjectSpecsInitiation(projectID, endMarketId);
                if( initiationList_DB != null && initiationList_DB.size() > 0) {
                	
                	 this.projectSpecsInitiation_DB = initiationList_DB.get(0);
                     projectSpecsReporting_DB = this.projectSpecsManager.getProjectSpecsReporting(projectID,projSpecsEndMarketId);
                     projectSpecsEMDetails_DB = this.projectSpecsManager.getProjectSpecsEMDetails(projectID,projSpecsEndMarketId);
                     endMarketDetails_DB = this.synchroProjectManager.getEndMarketDetails(projectID);
                }
                if(projectID>0)
                {
                	List<ProjectInitiation> projectInitiationList_DB = this.pibManager.getPIBDetails(projectID);	
                	if(projectInitiationList_DB!=null && projectInitiationList_DB.size() > 0)
                	{
                		projectInitiation_DB = projectInitiationList_DB.get(0);
                	}
                }
               
                //psMethodologyWaiver = this.projectSpecsManager.getPSMethodologyWaiver(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                pibMethodologyWaiver = this.pibManager.getPIBMethodologyWaiver(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                if(pibMethodologyWaiver==null)
                {
                	pibMethodologyWaiver = new PIBMethodologyWaiver();
    	        }
                //attachmentMap = this.projectSpecsManager.getDocumentAttachment(projectID, projSpecsEndMarketId);
                attachmentMap = this.projectSpecsManager.getDocumentAttachment(projectID, endMarketId);
                if( initiationList != null && initiationList.size() > 0) {
                	
                	
                    this.projectSpecsInitiation = initiationList.get(0);
                    
                    //Code patch to fix issue Bug #19103
                    if(endMarketId!=null && endMarketId!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
                	{
                		List<ProjectSpecsInitiation> projectSpecsList = this.projectSpecsManager.getProjectSpecsInitiation(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                		if(projectSpecsList!=null && projectSpecsList.size()>0)
                		{
                			ProjectSpecsInitiation aboveMarketProjectSpecsInitiation = projectSpecsList.get(0);
                			this.projectSpecsInitiation.setDeviationFromSM(aboveMarketProjectSpecsInitiation.getDeviationFromSM());
                			
                			// This will set the Final Cost fields for Fieldwork pop for Above Market.
                			this.aboveMarketFinalCost = aboveMarketProjectSpecsInitiation.getAboveMarketFinalCost();
                		    this.aboveMarketFinalCostType = aboveMarketProjectSpecsInitiation.getAboveMarketFinalCostType();
                			
                		}
                	}
                    else
                    {
                    	// This will set the Final Cost fields for Fieldwork pop for Above Market.
            			this.aboveMarketFinalCost = projectSpecsInitiation.getAboveMarketFinalCost();
            		    this.aboveMarketFinalCostType = projectSpecsInitiation.getAboveMarketFinalCostType();
                    }
                   // projectSpecsReporting = this.projectSpecsManager.getProjectSpecsReporting(projectID,projSpecsEndMarketId);
                   // projectSpecsEMDetails = this.projectSpecsManager.getProjectSpecsEMDetails(projectID,projSpecsEndMarketId);
                    projectSpecsReporting = this.projectSpecsManager.getProjectSpecsReporting(projectID,endMarketId);
                    projectSpecsEMDetails = this.projectSpecsManager.getProjectSpecsEMDetails(projectID,endMarketId);
                    psEMList = this.projectSpecsManager.getProjectSpecsEMDetails(projectID);
                    
                    psEMList_DB = this.projectSpecsManager.getProjectSpecsEMDetails(projectID);
                    
                    if(psEMList!=null && psEMList.size()>0)
                    {
                    	for(ProjectSpecsEndMarketDetails psem: psEMList)
                    	{
                    		emIds.add(psem.getEndMarketID());
                    		projectEndDate = psem.getProjectEndDate();
                    		projectEndDateLatest = psem.getProjectEndDateLatest();
                    	}
                    }
                    
                    allEndMarketSaved = this.projectSpecsManager.allPSMarketSaved(projectID,psEMList.size()+1);

                }  else {
                    this.projectSpecsInitiation = new ProjectSpecsInitiation();
                    
                    isSave = true;
                }
                endMarketStatus = this.synchroProjectManager.getEndMarketStatus(projectID, endMarketId);
                
                List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
                for(Integer i : attachmentMap.keySet())
                {
                	abList.addAll(attachmentMap.get(i));
                }
                attachmentUser = pibManager.getAttachmentUser(abList);
                if(projectSpecsReporting==null)
                {
                	projectSpecsReporting=new ProjectSpecsReporting();
                }
                if(projectSpecsEMDetails==null)
                {
                	projectSpecsEMDetails=new ProjectSpecsEndMarketDetails();
                	projectSpecsEMDetails.setProjectID(projectID);
                	
                }
                if(psEMList==null)
                {
                	psEMList=new ArrayList<ProjectSpecsEndMarketDetails>();
                }
            
    			stageId = SynchroGlobal.getProjectActivityTab().get("research");
    		
    			
    			String spiUserName="";
    			String spiApprovalDate=null;
    		
    			if(projectSpecsInitiation.getApprover() >0)
    			{
    				spiUserName = userManager.getUser(projectSpecsInitiation.getApprover()).getName();
    			}
    			/*else
    			{
    				if(isAboveMarket)
        			{
    					spiUserName = userManager.getUser(project.getProjectOwner()).getName();
        			}
    				else
    				{
    					spiUserName = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName();
    				}
    			}*/
    			if(projectSpecsInitiation.getApprovedDate()!=null)
    			{
    				spiApprovalDate = df.format(projectSpecsInitiation.getApprovedDate());
    			}
    			
    			approvers.put(spiUserName, spiApprovalDate);
    		
    			editStage=SynchroPermHelper.canEditProjectByStatus(projectID);
    			//TODO - To add the project and stage status check over here whether the Proposal stage is completed or not. 
    		/*	if(editStage)
    			{
    				stageToDoList = stageManager.getToDoListTabs(getUser(), projectID,stageId,project.getName(),endMarketDetails.get(0).getEndMarketID());
    			}
    			else
    			{
    				//stageToDoList = stageManager.getDisabledToDoListTabs(stageId);
    				stageToDoList = stageManager.getDisabledToDoListTabs(getUser(), projectID,stageId,project.getName(),endMarketDetails.get(0).getEndMarketID());
    			}
    			*/
    			String baseUrl = URLUtils.getBaseURL(request);
    			String stageUrl = baseUrl+"/synchro/project-multi-specs!input.jspa?projectID=" + project.getProjectID();
    			
    			if(subjectSendToProjOwner==null)
    			{
    			
    				subjectSendToProjOwner = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
    				subjectSendToProjOwner=subjectSendToProjOwner.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectSendToProjOwner=subjectSendToProjOwner.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodySendToProjOwner==null)
    			{
    				//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-multi-specs!input.jspa?projectID=" + project.getProjectID();
    				
    				messageBodySendToProjOwner = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
    				messageBodySendToProjOwner=messageBodySendToProjOwner.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodySendToProjOwner=messageBodySendToProjOwner.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	            messageBodySendToProjOwner=messageBodySendToProjOwner.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			List<User> externalAgencyUsers = Lists.newArrayList(synchroUtils.getAwardedExternalAgencyUsers(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID));
    			if(externalAgencyUsers!=null && externalAgencyUsers.size()>0)
    			{
    				awardedExternalAgency = externalAgencyUsers.get(0).getEmail();
    			}
    			
    			if(subjectReqForClarification==null)
    			{
    			
    				subjectReqForClarification = TemplateUtil.getTemplate("ps.request.clarification.subject", JiveGlobals.getLocale());
    				subjectReqForClarification=subjectReqForClarification.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectReqForClarification=subjectReqForClarification.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				
    			}
    			if(messageBodyReqForClarification==null)
    			{
    				//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-multi-specs!input.jspa?projectID=" + project.getProjectID();
    				
    				messageBodyReqForClarification = TemplateUtil.getHtmlEscapedTemplate("ps.request.clarification.htmlBody", JiveGlobals.getLocale());
    				messageBodyReqForClarification=messageBodyReqForClarification.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodyReqForClarification=messageBodyReqForClarification.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodyReqForClarification=messageBodyReqForClarification.replaceAll("\\$\\{stageUrl\\}", stageUrl);
       			}
    			if(subjectFinalApproval==null)
    			{
    			
    				subjectFinalApproval = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
    				subjectFinalApproval=subjectFinalApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectFinalApproval=subjectFinalApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				subjectFinalApproval=subjectFinalApproval.replaceAll("\\$\\{userName\\}", getUser().getName());
    				
    			}
    			if(messageBodyFinalApproval==null)
    			{
    				//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-multi-specs!input.jspa?projectID=" + project.getProjectID();
    				
    				messageBodyFinalApproval = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
    				messageBodyFinalApproval=messageBodyFinalApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				messageBodyFinalApproval=messageBodyFinalApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    				messageBodyFinalApproval=messageBodyFinalApproval.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    				messageBodyFinalApproval=messageBodyFinalApproval.replaceAll("\\$\\{userName\\}", getUser().getName());
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
    			
    			// This will check whether the Above Market is approved or not for the Final Approvals button to be enabled for other End Market Tabs
    			ProjectSpecsInitiation aMPSList = this.projectSpecsManager.getProjectSpecsInitiation(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).get(0);
    			if(aMPSList!=null && aMPSList.getIsApproved()!=null && aMPSList.getIsApproved()==1)
    			{
    				aboveMarketApproved = true;
    			}
    			
    			
    			StringBuffer legalAppEndMarkets = new StringBuffer();
    			List<ProjectSpecsInitiation> allPSList = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
    			for(ProjectSpecsInitiation pSpecs:allPSList)
    			{
    				if(endMarketId!=pSpecs.getEndMarketID())
    				{
	    				if(allEndMarketLegalApprovalSaved && pSpecs.getLegalApprovalStimulus() && pSpecs.getLegalApproverStimulus()!=null && !pSpecs.getLegalApproverStimulus().equalsIgnoreCase("")
	    						&& pSpecs.getLegalApprovalScreener() && pSpecs.getLegalApproverScreener()!=null && !pSpecs.getLegalApproverScreener().equalsIgnoreCase("")
	    						&& pSpecs.getLegalApprovalCCCA() && pSpecs.getLegalApproverCCCA()!=null && !pSpecs.getLegalApproverCCCA().equalsIgnoreCase("")
	    						&& pSpecs.getLegalApprovalQuestionnaire() && pSpecs.getLegalApproverQuestionnaire()!=null && !pSpecs.getLegalApproverQuestionnaire().equalsIgnoreCase("")
	    						&& pSpecs.getLegalApprovalDG() && pSpecs.getLegalApproverDG()!=null && !pSpecs.getLegalApproverDG().equalsIgnoreCase(""))
	                    {
	                    	allEndMarketLegalApprovalSaved = true;
	                    }
	                    else
	                    {
	                    	if(!(pSpecs.getLegalApprovalStimulus() && pSpecs.getLegalApproverStimulus()!=null && !pSpecs.getLegalApproverStimulus().equalsIgnoreCase("")
		    						&& pSpecs.getLegalApprovalScreener() && pSpecs.getLegalApproverScreener()!=null && !pSpecs.getLegalApproverScreener().equalsIgnoreCase("")
		    						&& pSpecs.getLegalApprovalCCCA() && pSpecs.getLegalApproverCCCA()!=null && !pSpecs.getLegalApproverCCCA().equalsIgnoreCase("")
		    						&& pSpecs.getLegalApprovalQuestionnaire() && pSpecs.getLegalApproverQuestionnaire()!=null && !pSpecs.getLegalApproverQuestionnaire().equalsIgnoreCase("")
		    						&& pSpecs.getLegalApprovalDG() && pSpecs.getLegalApproverDG()!=null && !pSpecs.getLegalApproverDG().equalsIgnoreCase("")))
	                    	{
		                    	allEndMarketLegalApprovalSaved = false;
		                    	if(legalAppEndMarkets.length()>0)
		                    	{
		                    		if(pSpecs.getEndMarketID()==SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
		                    		{
		                    			legalAppEndMarkets.append("<br> - Above Market");
		                    		}
		                    		else
		                    		{
		                    			legalAppEndMarkets.append("<br> - "+ SynchroGlobal.getEndMarkets().get(Integer.valueOf(pSpecs.getEndMarketID()+"")));
		                    		}
		                    	}
		                    	else
		                    	{
		                    		if(pSpecs.getEndMarketID()==SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
		                    		{
		                    			legalAppEndMarkets.append("<br> - Above Market");
		                    		}
		                    		else
		                    		{
		                    			legalAppEndMarkets.append(" - "+SynchroGlobal.getEndMarkets().get(Integer.valueOf(pSpecs.getEndMarketID()+"")));
		                    		}
		                    	}
	                    	}
	                 
	                    }
    				}
    			}
    			legalApproverEndMarkets = legalAppEndMarkets.toString();
    			
    			//https://www.svn.sourcen.com/issues/18820
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

    			
    			
        }

        
        // Contenttype check is required to skip the below binding in case odf adding attachments
        if(getRequest().getMethod() == "POST" && !getRequest().getContentType().startsWith("multipart/form-data") && getRequest().getParameter("attachmentId")==null) {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.projectSpecsInitiation);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Project Specs Initiation bean.");
                input();
            }

//            if(this.projectSpecsInitiation.getMethodologyGroup() != null && this.projectSpecsInitiation.getMethodologyGroup().intValue() > 0) {
//                Long mtId = SynchroGlobal.getMethodologyTypeByGroup(this.projectSpecsInitiation.getMethodologyGroup());
//                this.projectSpecsInitiation.setMethodologyType(mtId);
//            }

            if(this.projectSpecsInitiation.getProposedMethodology() != null && this.projectSpecsInitiation.getProposedMethodology().size() > 0) {
                this.projectSpecsInitiation.setMethodologyType(SynchroGlobal.getMethodologyTypeByProsedMethodologies(this.projectSpecsInitiation.getProposedMethodology()));
            }

            if(projectSpecsInitiation.getProjectOwner()==null && getRequest().getParameter("projectOwner")!=null)
            {
            	projectSpecsInitiation.setProjectOwner(Long.parseLong(getRequest().getParameter("projectOwner")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("startDate"))) {
                projectSpecsInitiation.setStartDate(DateUtils.parse(getRequest().getParameter("startDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("endDate"))) {
                projectSpecsInitiation.setEndDate(DateUtils.parse(getRequest().getParameter("endDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("stimuliDate"))) {
                projectSpecsInitiation.setStimuliDate(DateUtils.parse(getRequest().getParameter("stimuliDate")));
            }

         // To map the Agency End Market Details
            binder = new ServletRequestDataBinder(this.projectSpecsEMDetails);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Project Specs End Market Detail bean.");
                if(projectSpecsInitiation.getMethodologyType().intValue()==1 || projectSpecsInitiation.getMethodologyType().intValue()==2 || projectSpecsInitiation.getMethodologyType().intValue()==3 ||  projectSpecsInitiation.getMethodologyType().intValue()==5)
                {
	                if(projectSpecsEMDetails.getTotalCost()==null)
	                {
	                	projectSpecsEMDetails.setTotalCost(BigDecimal.valueOf(new Integer("0")));
	                }
	                if(projectSpecsEMDetails.getIntMgmtCost()==null)
	                {
	                	projectSpecsEMDetails.setIntMgmtCost(BigDecimal.valueOf(new Integer("0")));
	                }
	                if(projectSpecsEMDetails.getLocalMgmtCost()==null)
	                {
	                	projectSpecsEMDetails.setLocalMgmtCost(BigDecimal.valueOf(new Integer("0")));
	                }
	                if(projectSpecsEMDetails.getFieldworkCost()==null)
	                {
	                	projectSpecsEMDetails.setFieldworkCost(BigDecimal.valueOf(new Integer("0")));
	                }
	                if(projectSpecsEMDetails.getOperationalHubCost()==null)
	                {
	                	projectSpecsEMDetails.setOperationalHubCost(BigDecimal.valueOf(new Integer("0")));
	                }
	                if(projectSpecsEMDetails.getOtherCost()==null)
	                {
	                	projectSpecsEMDetails.setOtherCost(BigDecimal.valueOf(new Integer("0")));
	                }
                }
                if(projectSpecsInitiation.getMethodologyType().intValue()==1 || projectSpecsInitiation.getMethodologyType().intValue()==3)
                {
	                if(projectSpecsEMDetails.getTotalNoInterviews()==null)
	                {
	                	projectSpecsEMDetails.setTotalNoInterviews(new Integer("0"));
	                }
	                if(projectSpecsEMDetails.getTotalNoOfVisits()==null)
	                {
	                	projectSpecsEMDetails.setTotalNoOfVisits(new Integer("0"));
	                }
	                if(projectSpecsEMDetails.getAvIntDuration()==null)
	                {
	                	projectSpecsEMDetails.setAvIntDuration(new Integer("0"));
	                }
                }
                if(projectSpecsInitiation.getMethodologyType().intValue()==2 || projectSpecsInitiation.getMethodologyType().intValue()==3)
                {    
	                if(projectSpecsEMDetails.getTotalNoOfGroups()==null)
	                {
	                	projectSpecsEMDetails.setTotalNoOfGroups(new Integer("0"));
	                }
	                if(projectSpecsEMDetails.getInterviewDuration()==null)
	                {
	                	projectSpecsEMDetails.setInterviewDuration(new Integer("0"));
	                }
	                if(projectSpecsEMDetails.getNoOfRespPerGroup()==null)
	                {
	                	projectSpecsEMDetails.setNoOfRespPerGroup(new Integer("0"));
	                }
                }
                if(projectSpecsInitiation.getMethodologyType().intValue()==4)
                {
	                if(projectSpecsEMDetails.getTotalCost()==null)
	                {
	                	projectSpecsEMDetails.setTotalCost(BigDecimal.valueOf(new Integer("0")));
	                }
                }
                input();
            }
            if(SynchroPermHelper.isSystemAdmin(getUser()))
            {
	            if(getRequest().getParameterValues("dataCollectionMethod_"+projSpecsEndMarketId) ==null)
	            {
	            	projectSpecsEMDetails.setDataCollectionMethod(null);
	            }
	            if(getRequest().getParameterValues("dataCollectionMethod_"+projSpecsEndMarketId) !=null)
	            {
	            	String[] dataCollection = getRequest().getParameterValues("dataCollectionMethod_"+projSpecsEndMarketId);
	            	List<Long> dataColl = new ArrayList<Long>();
	            	for(int j=0;j<dataCollection.length;j++)
	            	{
	            		dataColl.add(new Long(dataCollection[j]));
	            	}
	            	projectSpecsEMDetails.setDataCollectionMethod(dataColl);
	            }
            }
          
            // To map the Methodology Waiver Fields
            
            pibMethodologyWaiver.setProjectID(projectID);
            pibMethodologyWaiver.setMethodologyDeviationRationale(getRequest().getParameter("methodologyDeviationRationale"));
        	try
        	{
        		pibMethodologyWaiver.setEndMarketID(Long.valueOf(getRequest().getParameter("endMarketId")));
        		pibMethodologyWaiver.setMethodologyApprover(Long.valueOf(getRequest().getParameter("methodologyApprover")));
        	}
        	catch(NumberFormatException ne)
        	{
        		 
        	}
        	if(getRequest().getParameter("methodologyWaiverAction")!=null && getRequest().getParameter("methodologyWaiverAction").equals("Approve"))
        	{
        		methodologyWaiverAction = "Approve";
        		pibMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
        	}
        	if(getRequest().getParameter("methodologyWaiverAction")!=null && getRequest().getParameter("methodologyWaiverAction").equals("Reject"))
        	{
        		methodologyWaiverAction = "Reject";
        		pibMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
        	}
        	if(getRequest().getParameter("methodologyWaiverAction")!=null && getRequest().getParameter("methodologyWaiverAction").equals("Send for Information"))
        	{
        		methodologyWaiverAction = "Send for Information";
        	}
        	if(getRequest().getParameter("methodologyWaiverAction")!=null && getRequest().getParameter("methodologyWaiverAction").equals("Request more Information"))
        	{
        		methodologyWaiverAction = "Request more Information";
        		pibMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
        	}
            
            
            if(getRequest().getParameter("geoSpread")!=null && getRequest().getParameter("geoSpread").equals("geoSpreadNational"))
            {
            	projectSpecsEMDetails.setGeoSpreadNational(true);
            	projectSpecsEMDetails.setGeoSpreadUrban(false);
            }
            else if(getRequest().getParameter("geoSpread")!=null && getRequest().getParameter("geoSpread").equals("geoSpreadUrban"))
            {
            	projectSpecsEMDetails.setGeoSpreadNational(false);
            	projectSpecsEMDetails.setGeoSpreadUrban(true);
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("fwStartDate"))) {
                projectSpecsEMDetails.setFwStartDate(DateUtils.parse(getRequest().getParameter("fwStartDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("fwEndDate"))) {
                projectSpecsEMDetails.setFwEndDate(DateUtils.parse(getRequest().getParameter("fwEndDate")));
            }

            if(StringUtils.isNotBlank(getRequest().getParameter("fwStartDateLatest"))) {
                projectSpecsEMDetails.setFwStartDateLatest(DateUtils.parse(getRequest().getParameter("fwStartDateLatest")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("fwEndDateLatest"))) {
                projectSpecsEMDetails.setFwEndDateLatest(DateUtils.parse(getRequest().getParameter("fwEndDateLatest")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("projectEndDateLatest"))) {
                projectSpecsEMDetails.setProjectEndDateLatest(DateUtils.parse(getRequest().getParameter("projectEndDateLatest")));
            }
            // To Map the Change FieldWork/Cost Status fields

            for(ProjectSpecsEndMarketDetails psEM: psEMList)
            {
            	/*if(getRequest().getParameter("projectEndDateLatest_"+psEM.getEndMarketID())!=null && !getRequest().getParameter("projectEndDateLatest_"+psEM.getEndMarketID()).equalsIgnoreCase(""))
            	{
            		psEM.setProjectEndDateLatest(dateFormatter.parse(getRequest().getParameter("projectEndDateLatest_"+psEM.getEndMarketID())));
               	}*/
            	if(getRequest().getParameter("projectEndDateLatest")!=null && !getRequest().getParameter("projectEndDateLatest").equalsIgnoreCase(""))
            	{
            		psEM.setProjectEndDateLatest(dateFormatter.parse(getRequest().getParameter("projectEndDateLatest")));
               	}
            	if(getRequest().getParameter("fwStartDateLatest_"+psEM.getEndMarketID())!=null && !getRequest().getParameter("fwStartDateLatest_"+psEM.getEndMarketID()).equalsIgnoreCase(""))
            	{
            		psEM.setFwStartDateLatest(dateFormatter.parse(getRequest().getParameter("fwStartDateLatest_"+psEM.getEndMarketID())));
               	}
            	if(getRequest().getParameter("fwEndDateLatest_"+psEM.getEndMarketID())!=null && !getRequest().getParameter("fwEndDateLatest_"+psEM.getEndMarketID()).equalsIgnoreCase(""))
            	{
            		psEM.setFwEndDateLatest(dateFormatter.parse(getRequest().getParameter("fwEndDateLatest_"+psEM.getEndMarketID())));
               	}
            	if(getRequest().getParameter("latestFWComments_"+psEM.getEndMarketID())!=null && !getRequest().getParameter("latestFWComments_"+psEM.getEndMarketID()).equalsIgnoreCase(""))
            	{
            		psEM.setLatestFWComments(getRequest().getParameter("latestFWComments_"+psEM.getEndMarketID()));
               	}
            	
            	if(getRequest().getParameter("finalCost_"+psEM.getEndMarketID())!=null && !getRequest().getParameter("finalCost_"+psEM.getEndMarketID()).equalsIgnoreCase(""))
            	{
            		psEM.setFinalCost(BigDecimal.valueOf(new Long(getRequest().getParameter("finalCost_"+psEM.getEndMarketID()))));
               	}
            	if(getRequest().getParameter("finalCostType_"+psEM.getEndMarketID())!=null && !getRequest().getParameter("finalCostType_"+psEM.getEndMarketID()).equalsIgnoreCase(""))
            	{
            		psEM.setFinalCostType(Integer.valueOf(getRequest().getParameter("finalCostType_"+psEM.getEndMarketID())));
               	}
            	if(getRequest().getParameter("finalCostComments_"+psEM.getEndMarketID())!=null && !getRequest().getParameter("finalCostComments_"+psEM.getEndMarketID()).equalsIgnoreCase(""))
            	{
            		psEM.setFinalCostComments(getRequest().getParameter("finalCostComments_"+psEM.getEndMarketID()));
               	}
            }
         }
    }

    public String input() {
      	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	
    	if ((SynchroPermHelper.hasProjectAccessMultiMarket(projectID) && SynchroPermHelper.canViewStage(projectID, 3)) || SynchroPermHelper.canAccessProject(projectID)) {
    		return INPUT;	
    	}
    	else
    	{
    		return UNAUTHORIZED;
    	}	
    }

    public String execute(){

        LOG.info("Save the Project Specs Details ...."+projectSpecsInitiation);
        Boolean manFieldsError = false;
      //  if( projectInitiation != null && ribDocument != null){
        if( projectSpecsInitiation != null){
        	projectSpecsInitiation.setProjectID(projectID);
        
        
     
            if((projectSpecsInitiation.getBizQuestion()!=null && !projectSpecsInitiation.getBizQuestion().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
            		&& (projectSpecsInitiation.getResearchObjective()!=null && !projectSpecsInitiation.getResearchObjective().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
            		&& (projectSpecsInitiation.getActionStandard()!=null && !projectSpecsInitiation.getActionStandard().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
            		&& (projectSpecsInitiation.getResearchDesign()!=null && !projectSpecsInitiation.getResearchDesign().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
            		&& (projectSpecsInitiation.getSampleProfile()!=null && !projectSpecsInitiation.getSampleProfile().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
            		&& (projectSpecsInitiation.getStimulusMaterial()!=null && !projectSpecsInitiation.getStimulusMaterial().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
            		&& (projectSpecsInitiation.getStimulusMaterialShipped()!=null && !projectSpecsInitiation.getStimulusMaterialShipped().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId())!=null)
            		//&& projectSpecsInitiation.getOthers()!=null && !projectSpecsInitiation.getOthers().equals("")
            		
            		&& (projectSpecsInitiation.getTopLinePresentation()!=null && projectSpecsInitiation.getTopLinePresentation()|| projectSpecsInitiation.getPresentation()!=null && projectSpecsInitiation.getPresentation()
            		|| projectSpecsInitiation.getFullreport()!=null && projectSpecsInitiation.getFullreport() || projectSpecsInitiation.getGlobalSummary()!=null && projectSpecsInitiation.getGlobalSummary())
            		//&& projectSpecsInitiation.getOtherReportingRequirements()!=null && !projectSpecsInitiation.getOtherReportingRequirements().equals("")
            		&& (projectSpecsInitiation.getScreener()!=null && !projectSpecsInitiation.getScreener().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null)
            		&& (projectSpecsInitiation.getConsumerCCAgreement()!=null && !projectSpecsInitiation.getConsumerCCAgreement().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null)
            		&& (projectSpecsInitiation.getQuestionnaire()!=null && !projectSpecsInitiation.getQuestionnaire().equals("")|| attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null)
            		&& (projectSpecsInitiation.getDiscussionguide()!=null && !projectSpecsInitiation.getDiscussionguide().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null))
            		
            {
            	// Quantitative
            	if(projectSpecsInitiation.getMethodologyType().intValue()==1 && !isAboveMarket)
            	{
            		if(projectSpecsEMDetails.getTotalCost()!=null && projectSpecsEMDetails.getIntMgmtCost()!=null && projectSpecsEMDetails.getLocalMgmtCost()!=null
                    		&& projectSpecsEMDetails.getFieldworkCost()!=null 
                    		&& projectSpecsEMDetails.getProposedFWAgencyNames()!=null && !projectSpecsEMDetails.getProposedFWAgencyNames().equals("")
                    		&& projectSpecsEMDetails.getFwEndDate()!=null && projectSpecsEMDetails.getFwStartDate()!=null
                    		//&& projectSpecsEMDetails.getDataCollectionMethod()!=null
            				&& projectSpecsEMDetails.getTotalNoInterviews()!=null && projectSpecsEMDetails.getTotalNoInterviews().intValue()>-1
                    		&& projectSpecsEMDetails.getTotalNoOfVisits()!=null && projectSpecsEMDetails.getTotalNoOfVisits().intValue()>-1
                    		&& projectSpecsEMDetails.getAvIntDuration()!=null && projectSpecsEMDetails.getAvIntDuration().intValue()>-1)
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());
            		}
            		else
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
            			manFieldsError=true;
            		}
            	}
            	// Qualitative
            	else if(projectSpecsInitiation.getMethodologyType().intValue()==2 && !isAboveMarket)
            	{
            		if(projectSpecsEMDetails.getTotalCost()!=null && projectSpecsEMDetails.getIntMgmtCost()!=null && projectSpecsEMDetails.getLocalMgmtCost()!=null
                    		&& projectSpecsEMDetails.getFieldworkCost()!=null 
                    		&& projectSpecsEMDetails.getProposedFWAgencyNames()!=null && !projectSpecsEMDetails.getProposedFWAgencyNames().equals("")
                    		&& projectSpecsEMDetails.getFwEndDate()!=null && projectSpecsEMDetails.getFwStartDate()!=null
                    		//&& projectSpecsEMDetails.getDataCollectionMethod()!=null
            				&& projectSpecsEMDetails.getTotalNoOfGroups()!=null && projectSpecsEMDetails.getTotalNoOfGroups().intValue()>-1 
            				&& projectSpecsEMDetails.getInterviewDuration()!=null && projectSpecsEMDetails.getInterviewDuration().intValue()>-1
            				&& projectSpecsEMDetails.getNoOfRespPerGroup()!=null && projectSpecsEMDetails.getNoOfRespPerGroup().intValue()>-1)
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());
            		}
            		else
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
            			manFieldsError=true;
            		}
            	}
            	//Quant & Qual
            	else if(projectSpecsInitiation.getMethodologyType().intValue()==3 && !isAboveMarket)
            	{
            		if(projectSpecsEMDetails.getTotalCost()!=null && projectSpecsEMDetails.getIntMgmtCost()!=null && projectSpecsEMDetails.getLocalMgmtCost()!=null
                    		&& projectSpecsEMDetails.getFieldworkCost()!=null 
                    		&& projectSpecsEMDetails.getProposedFWAgencyNames()!=null && !projectSpecsEMDetails.getProposedFWAgencyNames().equals("")
                    		&& projectSpecsEMDetails.getFwEndDate()!=null && projectSpecsEMDetails.getFwStartDate()!=null
                    	//	&& projectSpecsEMDetails.getDataCollectionMethod()!=null
                    		&& projectSpecsEMDetails.getTotalNoInterviews()!=null && projectSpecsEMDetails.getTotalNoInterviews().intValue()>-1
                    		&& projectSpecsEMDetails.getTotalNoOfVisits()!=null && projectSpecsEMDetails.getTotalNoOfVisits().intValue()>-1
                    		&& projectSpecsEMDetails.getAvIntDuration()!=null && projectSpecsEMDetails.getAvIntDuration().intValue()>-1
                    		&& projectSpecsEMDetails.getTotalNoOfGroups()!=null && projectSpecsEMDetails.getTotalNoOfGroups().intValue()>-1 
            				&& projectSpecsEMDetails.getInterviewDuration()!=null && projectSpecsEMDetails.getInterviewDuration().intValue()>-1
            				&& projectSpecsEMDetails.getNoOfRespPerGroup()!=null && projectSpecsEMDetails.getNoOfRespPerGroup().intValue()>-1)
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());
            		}
            		else
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
            			manFieldsError=true;
            		}
            	}
            	//Desk Research/Advanced Analytics
            	else if(!isAboveMarket)
            	{
            		if(projectSpecsEMDetails.getTotalCost()!=null )
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());
            		}
            		else
            		{
            			projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
            			manFieldsError=true;
            		}
            	}
            	else
            	{
            		projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());
            	}
            	
            }
            else
            {
            	projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
            	manFieldsError=true;
            }
            
            // Update the SPI contact
           if(projectSpecsInitiation.getSpiContact()!=null && projectSpecsInitiation.getSpiContact()>0)
           {
            //  this.synchroProjectManager.updateSPIContact(projectID, projectSpecsInitiation.getEndMarketID(), projectSpecsInitiation.getSpiContact());
           }
           if(projectSpecsInitiation.getProjectOwner()!=null && projectSpecsInitiation.getProjectOwner()>0)
           {
        	  // this.synchroProjectManager.updateOwner(projectID, projectSpecsInitiation.getProjectOwner());
           }
            
           // https://svn.sourcen.com/issues/19652
           
           projectSpecsInitiation.setScreener(SynchroUtils.fixBulletPoint(projectSpecsInitiation.getScreener()));
           projectSpecsInitiation.setConsumerCCAgreement(SynchroUtils.fixBulletPoint(projectSpecsInitiation.getConsumerCCAgreement()));
           projectSpecsInitiation.setQuestionnaire(SynchroUtils.fixBulletPoint(projectSpecsInitiation.getQuestionnaire()));
           projectSpecsInitiation.setDiscussionguide(SynchroUtils.fixBulletPoint(projectSpecsInitiation.getDiscussionguide()));
           
           if(projectSpecsInitiation.getLegalApprovalCCCA() && projectSpecsInitiation.getLegalApproverCCCA()!=null && !projectSpecsInitiation.getLegalApproverCCCA().equals("")
      				&& projectSpecsInitiation.getLegalApprovalDG() && projectSpecsInitiation.getLegalApproverDG()!=null && !projectSpecsInitiation.getLegalApproverDG().equals("")
      				&& projectSpecsInitiation.getLegalApprovalQuestionnaire() && projectSpecsInitiation.getLegalApproverQuestionnaire()!=null && !projectSpecsInitiation.getLegalApproverQuestionnaire().equals("")
      				&& projectSpecsInitiation.getLegalApprovalScreener() && projectSpecsInitiation.getLegalApproverScreener()!=null && !projectSpecsInitiation.getLegalApproverScreener().equals("")
      				&& projectSpecsInitiation.getLegalApprovalStimulus() && projectSpecsInitiation.getLegalApproverStimulus()!=null && !projectSpecsInitiation.getLegalApproverStimulus().equals(""))
              {
           	   projectSpecsInitiation.setPsLegalApprovalDate(new Date(System.currentTimeMillis()));
              }
           
            if(isSave) {
            	projectSpecsInitiation.setCreationBy(getUser().getID());
            	projectSpecsInitiation.setCreationDate(System.currentTimeMillis());
                
            	projectSpecsInitiation.setModifiedBy(getUser().getID());
            	projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());
                
                this.projectSpecsManager.saveProjectSpecsDetails(projectSpecsInitiation);
                this.projectSpecsManager.saveProjectSpecsEMDetails(projectSpecsEMDetails);
               
            }
            else {
            	projectSpecsInitiation.setModifiedBy(getUser().getID());
            	projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());
            	
            	//https://www.svn.sourcen.com/issues/17817
            	if(projectSpecsInitiation.getIsScreenerCCApproved()==2)
            	{
            		projectSpecsInitiation.setIsScreenerCCApproved(null);
            	}
            	if(projectSpecsInitiation.getIsQDGApproved()==2)
            	{
            		projectSpecsInitiation.setIsQDGApproved(null);
            	}
                this.projectSpecsManager.updateProjectSpecsDetails(projectSpecsInitiation);
                this.projectSpecsManager.updateProjectSpecsEMDetails(projectSpecsEMDetails);
            }
            
          //Audit Logs : Project Specs Save          
            SynchroLogUtils.ProjectSpecsMultiMarketSave(project, projectSpecsInitiation_DB, projectSpecsInitiation, projectSpecsEMDetails_DB, projectSpecsEMDetails, projectSpecsReporting_DB, endMarketDetails_DB, projectInitiation_DB, endMarketId, getUser());
            
        } else {
            LOG.error("Project Specs Initiation was null  ");
            LOG.error("RIB Document has not been configured during the Wizard setup.");
            addActionError("RIB document missing.");
        }
        
      //Audit Logs: Project Specs SAVE
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID(), endMarketId);
      /*  if(manFieldsError)
        {
        	if(isAboveMarket)
        	{
        		redirectURL="/synchro/project-multi-specs!input.jspa?projectID="+projectID+"&validationError=true";
        	}
        	else
        	{
        		redirectURL="/synchro/project-multi-specs!input.jspa?projectID="+projectID+"&endMarketId="+endMarketId+"&validationError=true";
        	}
        	return "validationError";
        }*/
        return SUCCESS;
    }
    /**
     * This method will update the PIB Waiver related fields
     * @return
     */
    public String updateWaiver(){

    	User pibMethApp = null;  
    	
    	String projectOwnerEmail="";
    	String spiContactEmail="";
    	//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-multi-specs!input.jspa?projectID=" + projectID;
    	String baseUrl = URLUtils.getBaseURL(request);
    	String stageUrl = baseUrl+"/synchro/project-multi-specs!input.jspa?projectID=" + projectID;
    	try
	      {
		        pibMethApp = userManager.getUser(pibMethodologyWaiver.getMethodologyApprover());
		        if(project.getProjectOwner()!=null)
		        {
		        	projectOwnerEmail = userManager.getUser(project.getProjectOwner()).getEmail();
		        }
		       /* if(endMarketDetails.get(0).getSpiContact()!=null)
		        {
		        	spiContactEmail = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
		        }*/
		        
		   }
	       catch(UserNotFoundException ue)
	       {
	        	
	      }
    	
    	//Save Audit logs for change in Methodology Waiver related fields
    	
        if(projectID!=null && projectID > 0)
        {
        	final PIBMethodologyWaiver pibMethodologyWaiver_DB = this.pibManager.getPIBMethodologyWaiver(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            SynchroLogUtils.PIBWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
        }
        
    	if(methodologyWaiverAction!=null && methodologyWaiverAction.equals("Approve"))
    	{
    		// this.projectSpecsManager.approvePSMethodologyWaiver(psMethodologyWaiver);
    		this.pibManager.approvePIBMethodologyWaiver(pibMethodologyWaiver);
    		// emailMessage = stageManager.populateNotificationEmail(projectOwnerEmail+","+spiContactEmail, "Waiver Approved for Project Id - " + project.getProjectID(), "Waiver Approved",null,null);
    		 String recp = projectOwnerEmail+","+spiContactEmail; 
   			EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"ps.approve.meth.waiver.htmlBody","ps.approve.meth.waiver.subject");
   			
   			//email.getContext().put("projectId", projectID);
   			email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
   			email.getContext().put("projectName",project.getName());
   			email.getContext().put ("stageUrl",stageUrl);
   			
    		stageManager.sendNotification(getUser(),email);
    		
    		//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Methodology Waiver Approved");
	    	emailNotBean.setEmailSubject("Notification | Methodology Waiver Approved");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), getText("logger.project.waiver.approve"), project.getName(), 
            												project.getProjectID(), getUser().getID());    			 
    			 
    	}
    	else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Reject"))
    	{
    		// this.projectSpecsManager.rejectPSMethodologyWaiver(psMethodologyWaiver);
    		 this.pibManager.rejectPIBMethodologyWaiver(pibMethodologyWaiver);
    		 //emailMessage = stageManager.populateNotificationEmail(projectOwnerEmail+","+spiContactEmail, "Waiver Rejected for Project Id - " + project.getProjectID(), "Waiver Rejected",null,null);
    		 String recp = projectOwnerEmail+","+spiContactEmail; 
    			EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"ps.reject.meth.waiver.htmlBody","ps.reject.meth.waiver.subject");
    			
    			//email.getContext().put("projectId", projectID);
    			email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
    			email.getContext().put("projectName",project.getName());
    			email.getContext().put ("stageUrl",stageUrl);
    			
     		    stageManager.sendNotification(getUser(),email);
     		    
     		   //Email Notification TimeStamp Storage
    	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
    	    	emailNotBean.setProjectID(projectID);
    	    	emailNotBean.setEndmarketID(endMarketId);
    	    	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
    	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
    	    	emailNotBean.setEmailDesc("Notification | Methodology Waiver Rejected");
    	    	emailNotBean.setEmailSubject("Notification | Methodology Waiver Rejected");
    	    	emailNotBean.setEmailSender(getUser().getEmail());
    	    	emailNotBean.setEmailRecipients(recp);
    	    	emailNotificationManager.saveDetails(emailNotBean);
    	    	
    	    	//Reject Audit logs
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
                										SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), getText("logger.project.waiver.reject"), project.getName(), 
                												project.getProjectID(), getUser().getID());
                
    	}
    	else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Send for Information"))
    	{
    		//emailMessage = stageManager.populateNotificationEmail(pibMethApp.getEmail()+","+projectOwnerEmail, "Waiver sent for Information for Project Id - " + project.getProjectID(), "Waiver sent for Information",null,null);
  		    //stageManager.sendNotification(getUser(),emailMessage);
    		 String recp = pibMethApp.getEmail()+","+projectOwnerEmail; 
  			EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"ps.send.for.information.meth.waiver.htmlBody","ps.send.for.information.meth.waiver.subject");
  			
  			//email.getContext().put("projectId", projectID);
  			email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
  			email.getContext().put("projectName",project.getName());
  			email.getContext().put ("stageUrl",stageUrl);
  			
   		    stageManager.sendNotification(getUser(),email);
   		    
   		 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSubject("Notification | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
   		    
   		 /*   psMethodologyWaiver.setCreationBy(getUser().getID());
   		    psMethodologyWaiver.setCreationDate(System.currentTimeMillis());
	         
   		    psMethodologyWaiver.setModifiedBy(getUser().getID());
   		    psMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
   		    psMethodologyWaiver.setIsApproved(null); 
   		    psMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal());
   		    this.projectSpecsManager.savePSMethodologyWaiver(psMethodologyWaiver);
	   		*/
   		    pibMethodologyWaiver.setCreationBy(getUser().getID());
	        pibMethodologyWaiver.setCreationDate(System.currentTimeMillis());
	        pibMethodologyWaiver.setModifiedBy(getUser().getID());
	        pibMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
	        pibMethodologyWaiver.setIsApproved(null);
	        pibMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
	
	         //https://www.svn.sourcen.com/issues/17809 - Comments need to be saved in case of Send for Information
	         this.pibManager.savePIBMethodologyWaiver(pibMethodologyWaiver);
         
	         // This is done for issue https://www.svn.sourcen.com//issues/17663
            ProjectInitiation projectInitiation = new ProjectInitiation();
            projectInitiation.setProjectID(projectID);
            projectInitiation.setDeviationFromSM(new Integer("1"));
            projectInitiation.setModifiedBy(getUser().getID());
            projectInitiation.setModifiedDate(System.currentTimeMillis());
            this.pibManager.updatePIBDeviation(projectInitiation);
	       
	        projectSpecsInitiation.setDeviationFromSM(new Integer("1"));
	        projectSpecsInitiation.setModifiedBy(getUser().getID());
	        projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());
	        this.projectSpecsManager.updateProjectSpecsDetails(projectSpecsInitiation);
	        
	      //Send for information Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), getText("logger.project.waiver.send.inf"), project.getName(), 
							project.getProjectID(), getUser().getID());
  	 	}
    	else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Request more Information"))
    	{
    		//TODO: change the subject and message
  		  // emailMessage = stageManager.populateNotificationEmail(projectOwnerEmail+","+spiContactEmail, "Waiver requested for more information for Project Id - " + project.getProjectID(), "Waiver requested for more information",null,null);
  		   //stageManager.sendNotification(getUser(),emailMessage);
    		String recp = projectOwnerEmail+","+spiContactEmail; 
    		EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"ps.request.more.information.meth.waiver.htmlBody","ps.request.more.information.meth.waiver.subject");
    		//email.getContext().put("projectId", projectID);
    		email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
    		email.getContext().put("projectName",project.getName());
    		email.getContext().put ("stageUrl",stageUrl);
    		
     		stageManager.sendNotification(getUser(),email);
     		
     		//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | More information required on Methodology Waiver");
	    	emailNotBean.setEmailSubject("Notification | More information required on Methodology Waiver");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
  		  // projectSpecsManager.reqForInfoPSMethodologyWaiver(psMethodologyWaiver);
	    	 pibManager.reqForInfoPIBMethodologyWaiver(pibMethodologyWaiver);
  		  // projectSpecsManager.updateProjectSpecsStatus(projectID, endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.PROJECT_SPECS_METH_WAIV_MORE_INFO_REQ.ordinal());
	    	 
	    	//Request More Information Audit logs
	            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
						SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
								project.getProjectID(), getUser().getID());
     	}
    	else
    	{
    		/*psMethodologyWaiver.setCreationBy(getUser().getID());
    		psMethodologyWaiver.setCreationDate(System.currentTimeMillis());
	         
    		psMethodologyWaiver.setModifiedBy(getUser().getID());
    		psMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
    		psMethodologyWaiver.setIsApproved(null); 
    		  psMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
	       
    		this.projectSpecsManager.savePSMethodologyWaiver(psMethodologyWaiver);
	        */
    		pibMethodologyWaiver.setCreationBy(getUser().getID());
            pibMethodologyWaiver.setCreationDate(System.currentTimeMillis());

            pibMethodologyWaiver.setModifiedBy(getUser().getID());
            pibMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
            pibMethodologyWaiver.setIsApproved(null);
            pibMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
            this.pibManager.savePIBMethodologyWaiver(pibMethodologyWaiver);
            
            // This is done for issue https://www.svn.sourcen.com//issues/17663
            ProjectInitiation projectInitiation = new ProjectInitiation();
            projectInitiation.setProjectID(projectID);
            projectInitiation.setDeviationFromSM(new Integer("1"));
            projectInitiation.setModifiedBy(getUser().getID());
            projectInitiation.setModifiedDate(System.currentTimeMillis());
            this.pibManager.updatePIBDeviation(projectInitiation);
            
	        projectSpecsInitiation.setDeviationFromSM(new Integer("1"));
	        projectSpecsInitiation.setModifiedBy(getUser().getID());
	        projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());
	       // this.projectSpecsManager.updateProjectSpecsDetails(projectSpecsInitiation);
	        this.projectSpecsManager.updatePSDeviation(projectSpecsInitiation);
	        
	     
	        projectSpecsManager.updateProjectSpecsStatus(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, SynchroGlobal.StageStatus.PROJECT_SPECS_METH_WAIV_APP_PENDING.ordinal());
	       // emailMessage = stageManager.populateNotificationEmail(pibMethApp.getEmail()+","+projectOwnerEmail, "Waiver sent for Approval for Project Id - " + project.getProjectID(), "Waiver sent for Approval. Please approve",null,null);
	        String recp = pibMethApp.getEmail()+","+projectOwnerEmail; 
			EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"ps.send.for.approval.meth.waiver.htmlBody","ps.send.for.approval.meth.waiver.subject");
			
			//email.getContext().put("projectId", projectID);
			email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
			email.getContext().put("projectName",project.getName());
			email.getContext().put ("stageUrl",stageUrl);
			
			stageManager.sendNotification(getUser(),email);
			
			  //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
	    	emailNotBean.setEmailDesc("Action Required | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSubject("Action Required | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), getText("logger.project.waiver.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
		}
    	//stageManager.sendNotification(getUser(),emailMessage);
        return SUCCESS;
    }
    
    /**
	 * This method will perform the notification activities for the To Do List Actions for each stage.
	 * 
	 */
	public String sendNotification() {
		//EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
		//stageManager.sendNotification(getUser(),email);
		if(notificationTabId!=null && notificationTabId.equals("SEND FOR APPROVAL"))
		{
			this.projectSpecsManager.updateProjectSpecsSendForApproval(projectID, endMarketId, 1);
			//this.projectSpecsManager.updateRequestClarificationModification(projectID, endMarketId, null);
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			

			 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	       	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
	    	emailNotBean.setEmailDesc("SEND FOR APPROVAL");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit Logs: Send to SPI for Approval
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.specs.notfic.spi");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
		else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("Request for Clarification"))
		{
			this.projectSpecsManager.updateProjectSpecsSendForApproval(projectID, endMarketId, null);
			this.projectSpecsManager.updateRequestClarificationModification(projectID, endMarketId, 1);
			//this.projectSpecsManager.updateProjectSpecsStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	       	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("REQUEST FOR CLARIFICATION");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Request for Clarification Audit logs
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.specs.notfic.requestclarf");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
		
		else if(approve!=null && !approve.equals(""))
		{
			this.projectSpecsManager.approve(getUser(),projectID,endMarketId);
			//this.projectSpecsManager.updateProjectSpecsStatus(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
			
			boolean allPSEMApproved = true;
			
			List<ProjectSpecsInitiation> allPSList = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
			for(ProjectSpecsInitiation ps: allPSList)
			{
				// Only when the End Market Id is Above Market Id then make the entries for Report Summary for all the End Markets 
				if(endMarketId.intValue()==SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID.intValue())
				{
					// This has been done in case the Admin user Approves the PS once the Project is in Report Summary stage, then in that case
					// the Report Summary details should not be copied again.
					List<ReportSummaryInitiation> rsList = reportSummaryManager.getReportSummaryInitiation(projectID, ps.getEndMarketID());
					if(rsList!=null && rsList.size()>0)
					{
						
					}
					else
					{
						ReportSummaryInitiation reportSummary = new ReportSummaryInitiation();
						reportSummary.setProjectID(projectID);
						reportSummary.setEndMarketID(ps.getEndMarketID());
						reportSummary.setStatus(SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal());
						reportSummary.setCreationBy(getUser().getID());
						reportSummary.setCreationDate(System.currentTimeMillis());
			            
						reportSummary.setModifiedBy(getUser().getID());
						reportSummary.setModifiedDate(System.currentTimeMillis());
						reportSummaryManager.saveReportSummaryDetails(reportSummary);
					}
				}
				if(allPSEMApproved && ps.getIsApproved()!=1)
				{
					allPSEMApproved = false;
				}
				
			}
				
			// Update the Status of Project Specs stage to COMPLETED only when all the End Markets are filled
			if(allPSEMApproved)
			{
				this.projectSpecsManager.updateProjectSpecsStatus(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
			}
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	       	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("FINAL APPROVAL");
	    	
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Final Approval Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), getText("logger.project.specs.final.approval"), project.getName(), 
            												project.getProjectID(), getUser().getID());
            
	    	//Request for Final Approval Audit logs
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.specs.notfic.approval");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
		else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NOTIFY_ABOVE_MARKET_SPI_CONTACT"))
		{
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	       	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("NOTIFY_ABOVE_MARKET_SPI_CONTACT");
	    	
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Notify Above Market Contacts Audit logs
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.specs.notfic.abovemarket");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
		else
		{
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
		}
	
		return SUCCESS;
	}
	
	 /**
     * This method will move the stage from Project Specs to Report
     * @return
     */
    public String moveToNextStage() 
    {
    	LOG.info("Inside moveToNextStage PS Multi Market Action--- "+ projectID); 


		this.projectSpecsManager.approve(getUser(),projectID,endMarketId);
		//this.projectSpecsManager.updateProjectSpecsStatus(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
		
		boolean allPSEMApproved = true;
		
		List<ProjectSpecsInitiation> allPSList = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
		for(ProjectSpecsInitiation ps: allPSList)
		{
			// Only when the End Market Id is Above Market Id then make the entries for Report Summary for all the End Markets 
			if(endMarketId.intValue()==SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID.intValue())
			{
				// This has been done in case the Admin user Approves the PS once the Project is in Report Summary stage, then in that case
				// the Report Summary details should not be copied again.
				List<ReportSummaryInitiation> rsList = reportSummaryManager.getReportSummaryInitiation(projectID, ps.getEndMarketID());
				if(rsList!=null && rsList.size()>0)
				{
					
				}
				else
				{
					ReportSummaryInitiation reportSummary = new ReportSummaryInitiation();
					reportSummary.setProjectID(projectID);
					reportSummary.setEndMarketID(ps.getEndMarketID());
					reportSummary.setStatus(SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal());
					reportSummary.setCreationBy(getUser().getID());
					reportSummary.setCreationDate(System.currentTimeMillis());
		            
					reportSummary.setModifiedBy(getUser().getID());
					reportSummary.setModifiedDate(System.currentTimeMillis());
					reportSummaryManager.saveReportSummaryDetails(reportSummary);
				}
			}
			if(allPSEMApproved && ps.getIsApproved()!=1)
			{
				allPSEMApproved = false;
			}
			
		}
			
		// Update the Status of Project Specs stage to COMPLETED only when all the End Markets are filled
		if(allPSEMApproved)
		{
			this.projectSpecsManager.updateProjectSpecsStatus(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
		}
		
    	//return SUCCESS;
		 //  Clicking on ‘Move to the next stage’ will navigate user to the next stage 
        return "moveToNextStage";
    }
	
	/**
	 * This method will update the Change FieldWork Fields
	 * @return
	 */
	public String updateFielwork(){
		
		// Get Above market final cost for Audit logs 
		List<ProjectSpecsInitiation> projectSpecsList_DB = this.projectSpecsManager.getProjectSpecsInitiation(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		if(projectSpecsList_DB!=null && projectSpecsList_DB.size()>0)
		{
			ProjectSpecsInitiation aboveMarketProjectSpecsInitiation = projectSpecsList_DB.get(0);
		    this.aboveMarketFinalCost_DB = aboveMarketProjectSpecsInitiation.getAboveMarketFinalCost();
		    this.aboveMarketFinalCostType_DB = aboveMarketProjectSpecsInitiation.getAboveMarketFinalCostType();
		}
		
		// Issue https://www.svn.sourcen.com/issues/17665
		for(ProjectSpecsEndMarketDetails psEM : psEMList)
		{
			psEM.setFwEndDate(psEM.getFwEndDateLatest());
			psEM.setFwStartDate(psEM.getFwStartDateLatest());
			psEM.setProjectEndDate(psEM.getProjectEndDateLatest());
			this.projectSpecsManager.updateProjectSpecsFieldWorkDetailsMM(psEM);
			//https://svn.sourcen.com/issues/19625
			//https://svn.sourcen.com/issues/19839
			//this.projectSpecsManager.updateProjectSpecsAMFinalCost(projectID, psEM.getEndMarketID(),psEM.getFinalCost(),psEM.getFinalCostType());
			this.projectSpecsManager.updateProjectSpecsAMFinalCost(projectID, psEM.getEndMarketID(),projectSpecsInitiation.getAboveMarketFinalCost(),projectSpecsInitiation.getAboveMarketFinalCostType());
			
			
		}
		//https://www.svn.sourcen.com/issues/18825
		this.projectSpecsManager.updateProjectSpecsAMFinalCost(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,projectSpecsInitiation.getAboveMarketFinalCost(),projectSpecsInitiation.getAboveMarketFinalCostType());
		
		//Audit Logs TODO
		
		SynchroLogUtils.ProjectFieldworkMultimarketSave(project, psEMList_DB, psEMList, projectSpecsInitiation.getAboveMarketFinalCost(), projectSpecsInitiation.getAboveMarketFinalCostType(), aboveMarketFinalCost_DB, aboveMarketFinalCostType_DB);
		
		//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-multi-specs!input.jspa?projectID=" + projectID;
		String baseUrl = URLUtils.getBaseURL(request);
		String stageUrl = baseUrl+"/synchro/project-multi-specs!input.jspa?projectID=" + projectID;
		
		// If Costs/Fieldwork dates on “Change Fieldwork/Cost Status” pop up are changed by external agency then an 
		// automatic notification should go out to SP&I that cost or dates have been changed.
		//if(SynchroPermHelper.isExternalAgencyUser(projectID, endMarketDetails.get(0).getEndMarketID()))
		if(SynchroPermHelper.isExternalAgencyUser(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
		{
			try
			{	
				//String recp = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
				String recp = userManager.getUser(project.getProjectOwner()).getEmail();
				EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"fieldwork.cost.dates.changes.ps.htmlBody","fieldwork.cost.dates.changes.ps.subject");
				
				//email.getContext().put("projectId", projectID);
				email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
				email.getContext().put("projectName",project.getName());
				email.getContext().put ("stageUrl",stageUrl);
				
				stageManager.sendNotification(getUser(),email);
				
				EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
		    	emailNotBean.setProjectID(projectID);
		    	emailNotBean.setEndmarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		       	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
		    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
		    	emailNotBean.setEmailDesc("Notification | Field work Dates/Cost Changed ");
		    	
		    	emailNotBean.setEmailSubject("Notification | Field work Dates/Cost Changed ");
		    	emailNotBean.setEmailSender(getUser().getEmail());
		    	emailNotBean.setEmailRecipients(recp);
		    	
		    	emailNotificationManager.saveDetails(emailNotBean);
			}
			catch(UserNotFoundException ue)
			{
				LOG.error("User not found while sending notification for update FieldWork --");
			}
		}
        return SUCCESS;
    }
	
	 public String updateEndMarkets(){

	    	//TODO
	    	
	        if(updatedEndMarkets!=null && updatedEndMarkets.size()>0)
	        {
	            // First Delete all the End Markets
	         //   synchroProjectManager.deleteEndMarketDetail(projectID);
	         //   this.pibManager.deletePIBEndMarket(projectID);
	         //   this.pibManager.deletePIBMWEndMarket(projectID);
	         //   this.pibManager.deletePIBReportingEndMarket(projectID);
	         //   this.pibManager.deletePIBStakeholderEndMarket(projectID);

	            for(Long emId:emIds)
	            {
	            	if(!updatedEndMarkets.contains(emId))
	            	{
	            		// First Delete all the End Markets which are not in the updated list
	            		/*
	            		synchroProjectManager.deleteEndMarketStatus(projectID, emId);
	                    synchroProjectManager.deleteEndMarketDetail(projectID,emId);
	                    this.pibManager.deletePIBEndMarket(projectID,emId);
	                    this.pibManager.deletePIBMWEndMarket(projectID,emId);
	                    this.pibManager.deletePIBReportingEndMarket(projectID,emId);
	                    this.pibManager.deletePIBStakeholderEndMarket(projectID,emId);
	                    */
	                    this.projectSpecsManager.deleteProjectSpecsDetails(projectID,emId);
	                    this.reportSummaryManager.deleteReportSummaryDetails(projectID, emId);
	            	}
	            }
	            for(Long updatedEMID:updatedEndMarkets)
	            {
	                
	            	if(!emIds.contains(updatedEMID))
	            	{
	            		Long agencyId = synchroUtils.getAwardedExternalAgencyUserID(project.getProjectID(), updatedEMID);
	            		ProposalInitiation proposalInitiation = this.proposalManager.getProposalDetails(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, agencyId).get(0);
	                	ProposalReporting propoalReporting = this.proposalManager.getProposalReporting(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, agencyId);
	                	ProjectInitiation projectInitiationAboveMarket = this.pibManager.getPIBDetails(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).get(0);
	                	
		                ProjectSpecsInitiation projectSpecsInitiation = new ProjectSpecsInitiation();
		            	projectSpecsInitiation.setProjectID(project.getProjectID());
		            	projectSpecsInitiation.setActionStandard(proposalInitiation.getActionStandard());
		            	projectSpecsInitiation.setBizQuestion(proposalInitiation.getBizQuestion());
		            	projectSpecsInitiation.setCreationBy(proposalInitiation.getCreationBy());
		            	projectSpecsInitiation.setCreationDate(proposalInitiation.getCreationDate());
		            	projectSpecsInitiation.setEndMarketID(updatedEMID);
		            	projectSpecsInitiation.setModifiedBy(proposalInitiation.getModifiedBy());
		            	projectSpecsInitiation.setModifiedDate(proposalInitiation.getModifiedDate());
		            	projectSpecsInitiation.setNpiReferenceNo(proposalInitiation.getNpiReferenceNo());
		            	projectSpecsInitiation.setResearchDesign(proposalInitiation.getResearchDesign());
		            	projectSpecsInitiation.setResearchObjective(proposalInitiation.getResearchObjective());
		            	projectSpecsInitiation.setSampleProfile(proposalInitiation.getSampleProfile());
		            	projectSpecsInitiation.setStimuliDate(proposalInitiation.getStimuliDate());
		            	projectSpecsInitiation.setStimulusMaterial(proposalInitiation.getStimulusMaterial());
		            	projectSpecsInitiation.setStimulusMaterialShipped(proposalInitiation.getStimulusMaterialShipped());
		            	projectSpecsInitiation.setOthers(proposalInitiation.getOthers());
		            	projectSpecsInitiation.setDescription(project.getDescription());
		            	projectSpecsInitiation.setDeviationFromSM(projectInitiationAboveMarket.getDeviationFromSM());
		            	
		            	projectSpecsInitiation.setBrand(proposalInitiation.getBrand());
		            	projectSpecsInitiation.setProjectOwner(proposalInitiation.getProjectOwner());
		            	projectSpecsInitiation.setSpiContact(proposalInitiation.getSpiContact());
		            	projectSpecsInitiation.setProposedMethodology(proposalInitiation.getProposedMethodology());
		            	projectSpecsInitiation.setMethodologyGroup(proposalInitiation.getMethodologyGroup());
		            	projectSpecsInitiation.setMethodologyType(proposalInitiation.getMethodologyType());
		            	projectSpecsInitiation.setStartDate(proposalInitiation.getStartDate());
		            	projectSpecsInitiation.setEndDate(proposalInitiation.getEndDate());
		            	
		            	projectSpecsInitiation.setScreener("Enter text and/or attach documents");
		            	projectSpecsInitiation.setConsumerCCAgreement("Enter text and/or attach documents");
		            	projectSpecsInitiation.setQuestionnaire("Enter text and/or attach documents");
		            	projectSpecsInitiation.setDiscussionguide("Enter text and/or attach documents");
		            	projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal()); 
		            	
		            	projectSpecsInitiation.setOtherReportingRequirements(propoalReporting.getOtherReportingRequirements());
		            	projectSpecsInitiation.setPresentation(propoalReporting.getPresentation());
		            	projectSpecsInitiation.setTopLinePresentation(propoalReporting.getTopLinePresentation());
		            	projectSpecsInitiation.setFullreport(propoalReporting.getFullreport());
		            	projectSpecsInitiation.setGlobalSummary(propoalReporting.getGlobalSummary());
		            	
		            	//https://www.svn.sourcen.com/issues/18825
		            	projectSpecsInitiation.setAboveMarketFinalCost(projectInitiationAboveMarket.getLatestEstimate());
		            	projectSpecsInitiation.setAboveMarketFinalCostType(projectInitiationAboveMarket.getLatestEstimateType());
		            	
		            	// We are keeping the Category Type at Project Specs level as well.
		            	projectSpecsInitiation.setCategoryType(project.getCategoryType());
		            	
		            	
		            	projectSpecsManager.saveProjectSpecsDetails(projectSpecsInitiation);
		            	
		            	ProjectSpecsEndMarketDetails projectSpecsEMDetails = new ProjectSpecsEndMarketDetails();
			        	
			        	projectSpecsEMDetails.setProjectID(project.getProjectID());
			        	projectSpecsEMDetails.setEndMarketID(updatedEMID);
			        	
			        	projectSpecsManager.saveProjectSpecsEMDetails(projectSpecsEMDetails);
	            	}
	            }
	        }
	        /*	this.synchroProjectManager.updateSingleEndMarketId(projectID, updatedSingleMarketId);
	       // TODO : Here add the logic to update the End Market Id in PIB corresponding tables
	       this.pibManager.dupdatePIBMethWaiverSingleEndMarketId(projectID, updatedSingleMarketId);
	       this.pibManager.updatePIBReportingSingleEndMarketId(projectID, updatedSingleMarketId);
	       this.pibManager.updatePIBStakeholderListSingleEndMarketId(projectID, updatedSingleMarketId);*/
	        redirectURL="/synchro/project-multi-specs!input.jspa?projectID="+projectID;
	        
	      //Audit logs for Endmarket Changes
	        SynchroLogUtils.ProjectSpecsCountryUpdate(project, emIds, updatedEndMarkets);
	        
	        return SUCCESS;
	    }
	
	public String addAttachment() throws UnsupportedEncodingException {
       
        LOG.info("Checking File Name"+attachFileFileName);
        LOG.info("Checking File Content Type"+attachFileContentType);
        Map<String, Object> result = new HashMap<String, Object>();
        try
        {
        	projectSpecsManager.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID());
        	
        	//Add Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment" + "- " +attachFileFileName;
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), description, project.getName(), 
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
			projectSpecsManager.removeAttachment(attachmentId);
			
			//Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName +" deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID(), endMarketId);
		}
		catch (Exception e) {
            LOG.error("Exception while removing attachment Id --"+ attachmentId);
        }
		 return SUCCESS;
    }
	
	
	public String exportToPDF()
    {
		try
		{
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Document document=new Document();
			NumberFormat nf = NumberFormat.getInstance();
			response.setContentType("application/pdf");
		    response.addHeader("Content-Disposition", "attachment; filename=ProjectSpecsPDF.pdf");
			PdfWriter.getInstance(document,response.getOutputStream()); 
			document.open(); 
			
			document.add(new Paragraph("Project Code : "+ project.getProjectID()));
			document.add(new Paragraph("Project Name : "+ project.getName()));
			document.add(new Paragraph("Brand / Non-Branded :  "+ SynchroGlobal.getBrands().get(projectSpecsInitiation.getBrand().intValue())));
			document.add(new Paragraph("Country : "+ SynchroGlobal.getEndMarkets().get(Integer.valueOf(projectSpecsInitiation.getEndMarketID()+""))));
			document.add(new Paragraph("Project Owner : "+ userManager.getUser(project.getProjectOwner()).getName()));
			document.add(new Paragraph("PIT Creator : "+ userManager.getUser(project.getBriefCreator()).getName()));
		//	document.add(new Paragraph("SPI Contact : "+ userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName()));
			if(project.getProposedMethodology()!=null && SynchroGlobal.getMethodologies().get(projectSpecsInitiation.getProposedMethodology().get(0).intValue())!=null)
			{
				document.add(new Paragraph("Proposed Methodology : "+ SynchroGlobal.getMethodologies().get(projectSpecsInitiation.getProposedMethodology().get(0).intValue())));
			}
			else
			{
				document.add(new Paragraph("Proposed Methodology : "));
			}
			document.add(new Paragraph("Project Start (Commissioning) : "+ df.format(projectSpecsInitiation.getStartDate())));
			document.add(new Paragraph("Project End (Results) : "+ df.format(projectSpecsInitiation.getEndDate())));
			
			StringBuffer categoryTypes = new StringBuffer();
			for(int i=0;i<project.getCategoryType().size();i++)
			{
				if(i>0)
				{
					categoryTypes.append(","+SynchroGlobal.getProductTypes().get(project.getCategoryType().get(i).intValue()));
				}
				else
				{
					categoryTypes.append(SynchroGlobal.getProductTypes().get(project.getCategoryType().get(i).intValue()));
				}
			}
			document.add(new Paragraph("Category Type : "+ categoryTypes.toString()));
			document.add(new Paragraph("Methodolgy Type : "+ SynchroGlobal.getProjectIsMapping().get(projectSpecsInitiation.getMethodologyType().intValue())));
			document.add(new Paragraph("Methodology Group : "+ SynchroGlobal.getMethodologyGroups(true, projectSpecsInitiation.getMethodologyGroup()).get(projectSpecsInitiation.getMethodologyGroup().intValue())));
			boolean isExternalAgencyUser = SynchroPermHelper.isExternalAgencyUser(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			if(!isExternalAgencyUser)
			{
				/*if(endMarketDetails.get(0).getInitialCost()!=null)
				{
					document.add(new Paragraph("Estimated Cost : "+ nf.format(endMarketDetails.get(0).getInitialCost()) + " "+  SynchroGlobal.getCurrencies().get(endMarketDetails.get(0).getInitialCostCurrency().intValue())));
				}
				else
				{
					document.add(new Paragraph("Estimated Cost : "));
				}*/
				List<ProjectInitiation> initiationList = this.pibManager.getPIBDetails(projectID);
				/*if(initiationList.get(0).getLatestEstimate()!=null)
				{
					document.add(new Paragraph("Latest Cost : "+ nf.format(initiationList.get(0).getLatestEstimate()) + " "+  SynchroGlobal.getCurrencies().get(initiationList.get(0).getLatestEstimateType().intValue())));
				}
				else
				{
					document.add(new Paragraph("Latest Cost : "));
				}
				*/
				if(projectSpecsEMDetails.getFinalCost()!=null)
				{
					document.add(new Paragraph("Final Cost : "+ nf.format(projectSpecsEMDetails.getFinalCost()) + " "+  SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getFinalCostType().intValue())));
				}
				else
				{
					document.add(new Paragraph("Final Cost : "));
				}
			}
			//document.add(new Paragraph("Latest Estimate : "+ projectInitiation.getLatestEstimate() + " "+ SynchroGlobal.getCurrencies().get(projectInitiation.getLatestEstimateType().intValue())));
			document.add(new Paragraph("NPI Number (if appropriate) : "+ projectSpecsInitiation.getNpiReferenceNo()));
			
			
			if(projectSpecsInitiation.getDeviationFromSM()!=null && projectSpecsInitiation.getDeviationFromSM()==1)
			{
				document.add(new Paragraph("Request for Methodology Waiver : Yes"));
			}
			else
			{
				document.add(new Paragraph("Request for Methodology Waiver : No"));
			}
			if(projectSpecsInitiation.getPoNumber()!=null)
			{
				document.add(new Paragraph("PO Number : "+ projectSpecsInitiation.getPoNumber()));
			}
			else
			{
				document.add(new Paragraph("PO Number : "));
			}
			document.add(new Paragraph("Project Description : "+ projectSpecsInitiation.getDescription()));
					
			if(projectSpecsInitiation.getBizQuestion()!=null)
			{
				document.add(new Paragraph("Business Questions : "+ projectSpecsInitiation.getBizQuestion()));
			}
			else
			{
				document.add(new Paragraph("Business Questions : "));
			}
			if(projectSpecsInitiation.getResearchObjective()!=null)
			{
				document.add(new Paragraph("Research Objectives(s) : "+ projectSpecsInitiation.getResearchObjective()));
			}
			else
			{
				document.add(new Paragraph("Research Objectives(s) : "));
			}
			if(projectSpecsInitiation.getActionStandard()!=null)
			{
				document.add(new Paragraph("Action Standard(s) : "+ projectSpecsInitiation.getActionStandard()));
			}
			else
			{
				document.add(new Paragraph("Action Standard(s) : "));
			}
			if(projectSpecsInitiation.getResearchDesign()!=null)
			{
				document.add(new Paragraph("Methodology Approach and Research Design : "+ projectSpecsInitiation.getResearchDesign()));
			}
			else
			{
				document.add(new Paragraph("Methodology Approach and Research Design : "));
			}
			if(projectSpecsInitiation.getSampleProfile()!=null)
			{
				document.add(new Paragraph("Sample Profile(Research) : "+ projectSpecsInitiation.getSampleProfile()));
			}
			else
			{
				document.add(new Paragraph("Sample Profile(Research) : "));
			}
			if(projectSpecsInitiation.getStimulusMaterial()!=null)
			{
				document.add(new Paragraph("Stimulus Material : "+ projectSpecsInitiation.getStimulusMaterial()));
			}
			else
			{
				document.add(new Paragraph("Stimulus Material : "));
			}
			if(projectSpecsInitiation.getStimulusMaterialShipped()!=null)
			{
				document.add(new Paragraph("Stimulus Material need to be shipped to : "+ projectSpecsInitiation.getStimulusMaterialShipped()));
			}
			else
			{
				document.add(new Paragraph("Stimulus Material need to be shipped to : "));
			}
			if(projectSpecsInitiation.getOthers()!=null)
			{
				document.add(new Paragraph("Other Comments : "+ projectSpecsInitiation.getOthers()));
			}
			else
			{
				document.add(new Paragraph("Other Comments : "));
			}
			if(projectSpecsInitiation.getStimuliDate()!=null)
			{
				document.add(new Paragraph("Date Stimuli Available(in Agency) : "+ df.format(projectSpecsInitiation.getStimuliDate())));
			}
			else
			{	
				document.add(new Paragraph("Date Stimuli Available(in Agency) : "));
			}
			StringBuffer repRequirements = new StringBuffer();
			if(projectSpecsReporting.getTopLinePresentation()!=null && projectSpecsReporting.getTopLinePresentation())
			{
				repRequirements.append("Top Line Presentation");
			}
			if(projectSpecsReporting.getPresentation()!=null && projectSpecsReporting.getPresentation())
			{
				if(repRequirements.length()>0)
				{
					repRequirements.append(",Presentation");
				}
				else
				{
					repRequirements.append("Presentation");
				}
			}
			if(projectSpecsReporting.getFullreport()!=null && projectSpecsReporting.getFullreport())
			{
				if(repRequirements.length()>0)
				{
					repRequirements.append(",Full Report");
				}
				else
				{
					repRequirements.append("Full Report");
				}
			}
		
			document.add(new Paragraph("Reporting Requirements : "+ repRequirements.toString()));

			
			if(projectSpecsReporting.getOtherReportingRequirements()!=null)
			{
				document.add(new Paragraph("Other Reporting Requirements : "+ projectSpecsReporting.getOtherReportingRequirements()));
			}
			else
			{
				document.add(new Paragraph("Other Reporting Requirements : "));
			}
			if(projectSpecsInitiation.getScreener()!=null)
			{
				document.add(new Paragraph("Screener : "+ projectSpecsInitiation.getScreener()));
			}
			else
			{
				document.add(new Paragraph("Screener : "));
			}
			if(projectSpecsInitiation.getConsumerCCAgreement()!=null)
			{
				document.add(new Paragraph("Consumer Aggreement : "+ projectSpecsInitiation.getConsumerCCAgreement()));
			}
			else
			{
				document.add(new Paragraph("Consumer Aggreement : "));
			}
			if(projectSpecsInitiation.getQuestionnaire()!=null)
			{
				document.add(new Paragraph("Questionnaire : "+ projectSpecsInitiation.getQuestionnaire()));
			}
			else
			{
				document.add(new Paragraph("Questionnaire : "));
			}
			if(projectSpecsInitiation.getDiscussionguide()!=null)
			{
				document.add(new Paragraph("Discussion Guide : "+ projectSpecsInitiation.getDiscussionguide()));
			}
			else
			{
				document.add(new Paragraph("Discussion Guide : "));
			}
			if(projectSpecsEMDetails.getTotalCost()!=null)
			{
				document.add(new Paragraph("Total Cost : " +nf.format(projectSpecsEMDetails.getTotalCost()) +" "+  SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getTotalCostType().intValue()) ));
			}
			else
			{
				document.add(new Paragraph("Total Cost : "));
			}
			if(projectSpecsInitiation.getMethodologyType().intValue()!=4 )
	    	{
				if(projectSpecsEMDetails.getIntMgmtCost()!=null)
				{
					document.add(new Paragraph("International Management Cost : "+ nf.format(projectSpecsEMDetails.getIntMgmtCost()) + " "+ SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getIntMgmtCostType().intValue())));
				}
				else
				{
					document.add(new Paragraph("International Management Cost : "));
				}
				if(projectSpecsEMDetails.getLocalMgmtCost()!=null)
				{
					document.add(new Paragraph("Local Management Cost : "+projectSpecsEMDetails.getLocalMgmtCost() + " "+ SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getLocalMgmtCostType().intValue())));
				}
				else
				{
					document.add(new Paragraph("Local Management Cost : "));
				}
				if(projectSpecsEMDetails.getFieldworkCost()!=null)
				{
					document.add(new Paragraph("Fieldwork Cost : "+nf.format(projectSpecsEMDetails.getFieldworkCost())+" " + SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getFieldworkCostType().intValue())));
				}
				else
				{
					document.add(new Paragraph("Fieldwork Cost : "));
				}
				if(projectSpecsEMDetails.getProposedFWAgencyNames()!=null)
				{
					document.add(new Paragraph("Name of Proposed Fieldwork Agencies : "+ projectSpecsEMDetails.getProposedFWAgencyNames()));
				}
				else
				{
					document.add(new Paragraph("Name of Proposed Fieldwork Agencies : "));
				}
				if(projectSpecsEMDetails.getFwStartDate()!=null)
				{
					document.add(new Paragraph("Estimated Fieldwork Start : " + df.format(projectSpecsEMDetails.getFwStartDate())));
				}
				else
				{
					document.add(new Paragraph("Estimated Fieldwork Start : "));
				}
				if(projectSpecsEMDetails.getFwEndDate()!=null)
				{
					document.add(new Paragraph("Estimated Fieldwork Completion : " + df.format(projectSpecsEMDetails.getFwEndDate())));
				}
				else
				{
					document.add(new Paragraph("Estimated Fieldwork Completion : "));
				}
				
				
				StringBuffer dataCollection = new StringBuffer();
				if(projectSpecsEMDetails.getDataCollectionMethod()!=null)
				{
					for(Long dc:projectSpecsEMDetails.getDataCollectionMethod())
					{
						if(dataCollection.length()>0)
						{
							dataCollection.append(","+SynchroGlobal.getDataCollections().get(dc.intValue()));
						}
						else
						{
							dataCollection.append(SynchroGlobal.getDataCollections().get(dc.intValue()));
						}
						
					}
				}
				document.add(new Paragraph("Data Collection Method : " + dataCollection));
	    	}
			
			if(projectSpecsInitiation.getMethodologyType().intValue()==1 || projectSpecsInitiation.getMethodologyType().intValue()==3)
	    	{
				document.add(new Paragraph("Quantitative : "));
				if( projectSpecsEMDetails.getTotalNoInterviews()!=null)
				{
					document.add(new Paragraph("Total Number of Interviews : " + projectSpecsEMDetails.getTotalNoInterviews()));
				}
				else
				{
					document.add(new Paragraph("Total Number of Interviews : " ));
				}
				if(projectSpecsEMDetails.getTotalNoOfVisits()!=null)
				{
					document.add(new Paragraph("Total Number of Visits per Respondent : " + projectSpecsEMDetails.getTotalNoOfVisits()));
				}
				else
				{
					document.add(new Paragraph("Total Number of Visits per Respondent : "));
				}
				if(projectSpecsEMDetails.getAvIntDuration()!=null)
				{
					document.add(new Paragraph("Average Interview Duration : " + projectSpecsEMDetails.getAvIntDuration()));
				}
				else
				{
					document.add(new Paragraph("Average Interview Duration : "));
				}
	    	}
			if(projectSpecsInitiation.getMethodologyType().intValue()==2 || projectSpecsInitiation.getMethodologyType().intValue()==3)
	    	{
			
				document.add(new Paragraph("Qualitative : "));
				
				if(projectSpecsEMDetails.getTotalNoOfGroups()!=null)
				{
					document.add(new Paragraph("Total No of Groups/In-Dept Interviews : " + projectSpecsEMDetails.getTotalNoOfGroups()));
				}
				else
				{
					document.add(new Paragraph("Total No of Groups/In-Dept Interviews : "));
				}
				if(projectSpecsEMDetails.getInterviewDuration()!=null)
				{
					document.add(new Paragraph("Group/In-Interview Duration : " + projectSpecsEMDetails.getInterviewDuration()));
				}
				else
				{
					document.add(new Paragraph("Group/In-Interview Duration : " ));
				}
				if(projectSpecsEMDetails.getNoOfRespPerGroup()!=null)
				{
					document.add(new Paragraph("Number of Respondents per Group : " + projectSpecsEMDetails.getNoOfRespPerGroup()));
				}
				else
				{
					document.add(new Paragraph("Number of Respondents per Group : "));
				}
	    	}
			if(projectSpecsInitiation.getMethodologyType().intValue()==1 || projectSpecsInitiation.getMethodologyType().intValue()==2 || projectSpecsInitiation.getMethodologyType().intValue()==3)
	    	{
				StringBuffer geoSpread = new StringBuffer();
				if(projectSpecsEMDetails.getGeoSpreadNational()!=null && projectSpecsEMDetails.getGeoSpreadNational())
				{
					geoSpread.append("National");
				}
				if(projectSpecsEMDetails.getGeoSpreadUrban()!=null && projectSpecsEMDetails.getGeoSpreadUrban())
				{
					if(geoSpread.length()>0)
					{
						geoSpread.append(", Urban Only");
					}
					else
					{
						geoSpread.append("Urban Only");
					}
				}
				document.add(new Paragraph("Geographical Spread : "+ geoSpread.toString()));
				if(projectSpecsEMDetails.getGeoSpreadUrban()!=null && projectSpecsEMDetails.getGeoSpreadUrban())
				{
					document.add(new Paragraph("Cities (Urban Only) : "+ projectSpecsEMDetails.getCities()));
				}
	    	}
			
			document.close(); 
			
		       // document.write(response.getOutputStream());
	    }
        catch (IOException e) {
    	    e.printStackTrace();
    	}
		catch (DocumentException e) {
    	    e.printStackTrace();
    	}
		catch (UserNotFoundException e) {
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

	public Long getUpdatedSingleMarketId() {
		return updatedSingleMarketId;
	}

	public void setUpdatedSingleMarketId(Long updatedSingleMarketId) {
		this.updatedSingleMarketId = updatedSingleMarketId;
	}

	

	public ProjectSpecsManager getProjectSpecsManager() {
		return projectSpecsManager;
	}

	public void setProjectSpecsManager(ProjectSpecsManager projectSpecsManager) {
		this.projectSpecsManager = projectSpecsManager;
	}

	public ProjectSpecsInitiation getProjectSpecsInitiation() {
		return projectSpecsInitiation;
	}

	public void setProjectSpecsInitiation(
			ProjectSpecsInitiation projectSpecsInitiation) {
		this.projectSpecsInitiation = projectSpecsInitiation;
	}

	public ProjectSpecsReporting getProjectSpecsReporting() {
		return projectSpecsReporting;
	}

	public void setProjectSpecsReporting(ProjectSpecsReporting projectSpecsReporting) {
		this.projectSpecsReporting = projectSpecsReporting;
	}

	public ProjectSpecsEndMarketDetails getProjectSpecsEMDetails() {
		return projectSpecsEMDetails;
	}

	public void setProjectSpecsEMDetails(
			ProjectSpecsEndMarketDetails projectSpecsEMDetails) {
		this.projectSpecsEMDetails = projectSpecsEMDetails;
	}

	public Map<Integer, List<AttachmentBean>> getAttachmentMap() {
		return attachmentMap;
	}

	public void setAttachmentMap(Map<Integer, List<AttachmentBean>> attachmentMap) {
		this.attachmentMap = attachmentMap;
	}

	public PIBManager getPibManager() {
		return pibManager;
	}

	public void setPibManager(PIBManager pibManager) {
		this.pibManager = pibManager;
	}

	public ReportSummaryManager getReportSummaryManager() {
		return reportSummaryManager;
	}

	public void setReportSummaryManager(ReportSummaryManager reportSummaryManager) {
		this.reportSummaryManager = reportSummaryManager;
	}

	public Map<Long, Long> getAttachmentUser() {
		return attachmentUser;
	}

	public void setAttachmentUser(Map<Long, Long> attachmentUser) {
		this.attachmentUser = attachmentUser;
	}


	public Long getProjSpecsEndMarketId() {
		return projSpecsEndMarketId;
	}

	public void setProjSpecsEndMarketId(Long projSpecsEndMarketId) {
		this.projSpecsEndMarketId = projSpecsEndMarketId;
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

	public List<ProjectSpecsEndMarketDetails> getPsEMList() {
		return psEMList;
	}

	public void setPsEMList(List<ProjectSpecsEndMarketDetails> psEMList) {
		this.psEMList = psEMList;
	}

	public Boolean getAllEndMarketSaved() {
		return allEndMarketSaved;
	}

	public void setAllEndMarketSaved(Boolean allEndMarketSaved) {
		this.allEndMarketSaved = allEndMarketSaved;
	}

	public String getSubjectSendToProjOwner() {
		return subjectSendToProjOwner;
	}

	public void setSubjectSendToProjOwner(String subjectSendToProjOwner) {
		this.subjectSendToProjOwner = subjectSendToProjOwner;
	}

	public String getMessageBodySendToProjOwner() {
		return messageBodySendToProjOwner;
	}

	public void setMessageBodySendToProjOwner(String messageBodySendToProjOwner) {
		this.messageBodySendToProjOwner = messageBodySendToProjOwner;
	}

	public String getAwardedExternalAgency() {
		return awardedExternalAgency;
	}

	public void setAwardedExternalAgency(String awardedExternalAgency) {
		this.awardedExternalAgency = awardedExternalAgency;
	}

	public SynchroUtils getSynchroUtils() {
		return synchroUtils;
	}

	public void setSynchroUtils(SynchroUtils synchroUtils) {
		this.synchroUtils = synchroUtils;
	}

	public String getMessageBodyReqForClarification() {
		return messageBodyReqForClarification;
	}

	public void setMessageBodyReqForClarification(
			String messageBodyReqForClarification) {
		this.messageBodyReqForClarification = messageBodyReqForClarification;
	}

	public String getSubjectReqForClarification() {
		return subjectReqForClarification;
	}

	public void setSubjectReqForClarification(String subjectReqForClarification) {
		this.subjectReqForClarification = subjectReqForClarification;
	}

	public String getSubjectFinalApproval() {
		return subjectFinalApproval;
	}

	public void setSubjectFinalApproval(String subjectFinalApproval) {
		this.subjectFinalApproval = subjectFinalApproval;
	}

	public String getMessageBodyFinalApproval() {
		return messageBodyFinalApproval;
	}

	public void setMessageBodyFinalApproval(String messageBodyFinalApproval) {
		this.messageBodyFinalApproval = messageBodyFinalApproval;
	}

	public String getAboveMarketProjectContact() {
		return aboveMarketProjectContact;
	}

	public void setAboveMarketProjectContact(String aboveMarketProjectContact) {
		this.aboveMarketProjectContact = aboveMarketProjectContact;
	}

	public List<FundingInvestment> getFundingInvestments() {
		return fundingInvestments;
	}

	public void setFundingInvestments(List<FundingInvestment> fundingInvestments) {
		this.fundingInvestments = fundingInvestments;
	}

	public List<Long> getEmIds() {
		return emIds;
	}

	public void setEmIds(List<Long> emIds) {
		this.emIds = emIds;
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

	public Date getProjectEndDate() {
		return projectEndDate;
	}

	public void setProjectEndDate(Date projectEndDate) {
		this.projectEndDate = projectEndDate;
	}

	public Date getProjectEndDateLatest() {
		return projectEndDateLatest;
	}

	public void setProjectEndDateLatest(Date projectEndDateLatest) {
		this.projectEndDateLatest = projectEndDateLatest;
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

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}

	public Boolean getAboveMarketApproved() {
		return aboveMarketApproved;
	}

	public void setAboveMarketApproved(Boolean aboveMarketApproved) {
		this.aboveMarketApproved = aboveMarketApproved;
	}

	public Integer getEndMarketStatus() {
		return endMarketStatus;
	}

	public void setEndMarketStatus(Integer endMarketStatus) {
		this.endMarketStatus = endMarketStatus;
	}

	public ProposalManager getProposalManager() {
		return proposalManager;
	}

	public void setProposalManager(ProposalManager proposalManager) {
		this.proposalManager = proposalManager;
	}

	public List<Long> getUpdatedEndMarkets() {
		return updatedEndMarkets;
	}

	public void setUpdatedEndMarkets(List<Long> updatedEndMarkets) {
		this.updatedEndMarkets = updatedEndMarkets;
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

	public PIBMethodologyWaiver getPibMethodologyWaiver() {
		return pibMethodologyWaiver;
	}

	public void setPibMethodologyWaiver(PIBMethodologyWaiver pibMethodologyWaiver) {
		this.pibMethodologyWaiver = pibMethodologyWaiver;
	}

	public Integer getAboveMarketFinalCostType() {
		return aboveMarketFinalCostType;
	}

	public void setAboveMarketFinalCostType(Integer aboveMarketFinalCostType) {
		this.aboveMarketFinalCostType = aboveMarketFinalCostType;
	}

	public BigDecimal getAboveMarketFinalCost() {
		return aboveMarketFinalCost;
	}

	public void setAboveMarketFinalCost(BigDecimal aboveMarketFinalCost) {
		this.aboveMarketFinalCost = aboveMarketFinalCost;
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
