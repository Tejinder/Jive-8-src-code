package com.grail.synchro.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.MultiMarketProject;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProposalManager;
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
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;
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
public class PIBMultiMarketAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(PIBMultiMarketAction.class);

    //Spring Managers
    private PIBManager pibManager;
    private ProjectManager synchroProjectManager;
    private ProfileManager profileManager;

    //Form related fields
    private ProjectInitiation projectInitiation;
    private Project project;
    private Long projectID;

    private ProjectInitiation projectInitiation_DB;
    private List<EndMarketInvestmentDetail> endMarketDetails_DB;
    private PIBStakeholderList pibStakeholderList_DB;
    private Project project_DB = null;
    private PIBReporting pibReporting_DB = null;
    
    private PIBReporting pibReporting = new PIBReporting();
    private PIBStakeholderList pibStakeholderList = new PIBStakeholderList();
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
    //private File[] attachFile;
    private Long attachmentFieldID;
    private String attachmentName;
	//private String[] attachFileContentType;
    //private String[] attachFileFileName;
    private int attachmentCount;
    private long[] removeAttachID;
    private long[] imageFile;
    private AttachmentHelper attachmentHelper;

    private String token;
    private String tokenCookie;



    private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long fieldCategoryId;
    private List<EndMarketInvestmentDetail> endMarketDetails;
    private List<Long> emIds;
    private Long endMarketId;
    // This field will contain the updated SingleMarketId in case the End market is changed
    private Long updatedSingleMarketId;
    //This map will contain the list of attachments for each field
    private Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();

    // This will contain  the Approvers data for the Checklist Tab
    Map<String,Map<String,String>> stageApprovers = new LinkedHashMap<String, Map<String,String>>();
    // This will containg the Methodology Waiver Related fields
    private PIBMethodologyWaiver pibMethodologyWaiver;
    
    // This will containg the Kantar Methodology Waiver Related fields
    private PIBMethodologyWaiver pibKantarMethodologyWaiver;
    
    // This field will check whether the user click on PIB Methodology Waiver is Approve or Reject button or Send for Information or Request more information
    private String methodologyWaiverAction;
    //private UserManager userManager;
    
    // This field will check whether the user click on PIB Kantar Methodology Waiver is Approve or Reject button or Send for Information or Request more information
    private String kantarMethodologyWaiverAction;
    
 // This field will check whether the request is from Pending Actions Page or not
    private String pageRequest;


    private List<ProjectInitiation> initiationList;
    private Boolean isAboveMarket;

    private List<FundingInvestment> fundingInvestments;

    // This will contain the Stakeholders for Above Market Section.
    private PIBStakeholderList pibAboveMarketStakeholderList = new PIBStakeholderList();
    private String marketAmendmentType;
    private Map<Long,Long> attachmentUser;

    // This flag is used to enable the Notification buttons on the PIB Stage by checking the status of individual End Market.
    private Boolean allEndMarketSaved;
    private String pibCompleteNotifyAgencyRecipents;
    private String pibCompleteNotifyAgencySubject;
    private String pibCompleteNotifyAgencyMessageBody;

    private String pibNotifyAgencyRecipents;
    private String pibNotifyAgencySubject;
    private String pibNotifyAgencyMessageBody;

    private String pibNotifyAboveMarketRecipents;
    private String pibNotifyAboveMarketSubject;
    private String pibNotifyAboveMarketMessageBody;

    private String pibApproveChangesRecipents;
    private String pibApproveChangesSubject;
    private String pibApproveChangesMessageBody;
    
    private String subjectSendToProjOwner;
   	private String messageBodySendToProjOwner;
   	private String subjectSendToProjectContact;
   	private String messageBodySendToProjectContact;
	private String aboveMarketProjectContact;

    // List of updated End Markets
    private List<Long> updatedEndMarkets;
    private Boolean showMandatoryFieldsError;

    private Map<Long, String> actionStandardMap = new HashMap<Long, String>();
    private Map<Long, String> researchDesignMap = new HashMap<Long, String>();
    private Map<Long, String> sampleProfileMap = new HashMap<Long, String>();
    private Map<Long, String> stimulusMaterialMap = new HashMap<Long, String>();
    // This flag will check whether a proposal has been awarded to an agency or not
    private Boolean isProposalAwarded=false;
    private ProposalManager proposalManager;

    // This flag is used to check whether the Legal Approval are saved on all the End Markets or not.
    private Boolean allEndMarketLegalApprovalSaved = true; 
    private String legalApproverEndMarkets;
    
    private Boolean canEditEM = true;
    
    // This variable whether the form is submitted from Email Notification button or from SAVE button.
    private String saveEmailForm;
    private Boolean isAutoSave = false;
    
    // These variables will have the ProjectOwner and Project Contact values for the endMarkets other than Above Market
    private Long endMarketProjectOwner;
    private Long endMarketProjectContact;
    private String pitUpdateOnly = "false";
    
    private EmailNotificationManager emailNotificationManager;
    
    // This will contain the End Market Project Stakeholders
    private String endMarketProjectUsers;
    private String subjectNotifyEndMarketContacts;
   	private String messageNotifyEndMarketContacts;
   	
   	private SynchroUtils synchroUtils;
   	private File[] mailAttachment;
	private String[] mailAttachmentFileName;
	private String[] mailAttachmentContentType;
	
	private String otherBATUsers;
	
    public void prepare() throws Exception {
        if(!getRequest().getRequestURI().contains("updatePIT")) {
            if(request.getParameter("autoSave") != null) {
                this.isAutoSave = Boolean.parseBoolean(request.getParameter("autoSave"));
            } else {
                this.isAutoSave = false;
            }

            final String id = getRequest().getParameter("projectID");
            if(!StringUtils.isNullOrEmpty(id)) {
                try{
                    projectID = Long.parseLong(id);
                } catch (NumberFormatException nfEx) {
                    LOG.error("Invalid ProjectID ");
                    throw nfEx;
                }
                project = this.synchroProjectManager.get(projectID);
                String validationError = getRequest().getParameter("validationError");
          /*      if(validationError!=null && validationError.equals("true"))
                {
                    showMandatoryFieldsError=true;
                }
*/
                endMarketDetails = this.synchroProjectManager.getEndMarketDetails(projectID);
                fundingInvestments = this.synchroProjectManager.getProjectInvestments(projectID);
              
                saveEmailForm = getRequest().getParameter("saveEmailForm");
                
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
                    pibAboveMarketStakeholderList=this.pibManager.getPIBStakeholderList(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);

                    redirectURL="/synchro/pib-multi-details!input.jspa?projectID="+projectID+"&endMarketId="+endMarketId;
                }
                else
                {
                    isAboveMarket=true;
                    // To add the Above Market Endmarket id
                    //endMarketId=endMarketDetails.get(0).getEndMarketID();
                    endMarketId = SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID;
                    redirectURL="/synchro/pib-multi-details!input.jspa?projectID="+projectID;

                }


                emIds = this.synchroProjectManager.getEndMarketIDs(projectID);
                //    methDeviationField=stageManager.checkPartialMethodologyValidation(project);


                initiationList = this.pibManager.getPIBDetails(projectID,endMarketId);
                allEndMarketSaved = this.pibManager.allPIBMarketSaved(projectID,endMarketDetails.size()+1);
                pibMethodologyWaiver = this.pibManager.getPIBMethodologyWaiver(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                pibKantarMethodologyWaiver = this.pibManager.getPIBKantarMethodologyWaiver(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                attachmentMap = this.pibManager.getDocumentAttachment(projectID, endMarketId);
                
                StringBuffer legalAppEndMarkets = new StringBuffer();
                
                for(EndMarketInvestmentDetail emd: endMarketDetails)
                {
                    List<ProjectInitiation> piList = this.pibManager.getPIBDetails(projectID,emd.getEndMarketID());
                    if(piList!=null && piList.size()>0)
                    {
                        actionStandardMap.put(emd.getEndMarketID(), piList.get(0).getActionStandard());
                        researchDesignMap.put(emd.getEndMarketID(), piList.get(0).getResearchDesign());
                        sampleProfileMap.put(emd.getEndMarketID(), piList.get(0).getSampleProfile());
                        stimulusMaterialMap.put(emd.getEndMarketID(), piList.get(0).getStimulusMaterial());
                        
                        boolean emFlag = true;
                        if(emd.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
                        {
		                    Integer status = synchroProjectManager.getEndMarketStatus(projectID, emd.getEndMarketID());
		                	if(status == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || status == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() || status == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal())
		                	{
		                		emFlag = false;
		                	}
                        }
                    	
                        if(emFlag)
                        {
	                        if(allEndMarketLegalApprovalSaved && ((piList.get(0).getLegalApprovalRcvd() && piList.get(0).getLegalApprover()!=null && !piList.get(0).getLegalApprover().equalsIgnoreCase("")) || (piList.get(0).getLegalApprovalNotReq())) )
	                        {
	                        	allEndMarketLegalApprovalSaved = true;
	                        }
	                        else
	                        {
	                        	allEndMarketLegalApprovalSaved = false;
	                        	if(!((piList.get(0).getLegalApprovalRcvd() && piList.get(0).getLegalApprover()!=null && !piList.get(0).getLegalApprover().equalsIgnoreCase("")) || (piList.get(0).getLegalApprovalNotReq())) )
	                        	{
		                        	if(legalAppEndMarkets.length()>0)
		                        	{
		                        		legalAppEndMarkets.append("<br> - "+ SynchroGlobal.getEndMarkets().get(emd.getEndMarketID().intValue()));
		                        	}
		                        	else
		                        	{
		                        		legalAppEndMarkets.append(" - "+SynchroGlobal.getEndMarkets().get(emd.getEndMarketID().intValue()));
		                        	}
	                        	}
	                     
	                        }
                        }
                    }

                }
                legalApproverEndMarkets = legalAppEndMarkets.toString();
                if(pibMethodologyWaiver==null)
                {
                    pibMethodologyWaiver = new PIBMethodologyWaiver();
                }
                
                if(pibKantarMethodologyWaiver==null)
                {
                	pibKantarMethodologyWaiver = new PIBMethodologyWaiver();
                }
                if( initiationList != null && initiationList.size() > 0) {
                    this.projectInitiation = initiationList.get(0);
                    //   pibReporting = this.pibManager.getPIBReporting(projectID);
                    
                    if(endMarketId!=null)
                    {
                        // attachmentMap = this.pibManager.getDocumentAttachment(projectID, endMarketId);
                        pibStakeholderList = this.pibManager.getPIBStakeholderList(projectID, endMarketId);

                        pibReporting = this.pibManager.getPIBReporting(projectID,endMarketId);
                    }
                    /* else
                    {
                         attachmentMap = this.pibManager.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
                         pibStakeholderList = this.pibManager.getPIBStakeholderList(projectID, endMarketDetails.get(0).getEndMarketID());
                         pibReporting = this.pibManager.getPIBReporting(projectID,endMarketDetails.get(0).getEndMarketID());
                    }*/

                }  else {
                    this.projectInitiation = new ProjectInitiation();
                    // If the initiationList is null it means the PIB is not saved yet.
                    projectInitiation.setStatus(SynchroGlobal.FinancialDetailsStatus.NONE.ordinal());
                   // projectInitiation.setNonKantar(false);
                    projectInitiation.setNonKantar(new Integer("0"));
                    isSave = true;
                    
                    // This is for QUICK FIX Feature. Need to copy the value of Total Cost to Latest Estimate Cost field initially
                   if(project!=null && project.getTotalCost()!=null && project.getTotalCostCurrency()!=null)
                    {
    	                projectInitiation.setLatestEstimate(project.getTotalCost());
    	                projectInitiation.setLatestEstimateType(project.getTotalCostCurrency().intValue());
                    }
                }
                
                //Code patch to fix issue Bug #19103
            	if(endMarketId!=null && endMarketId!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
            	{
            		List<ProjectInitiation> piList = this.pibManager.getPIBDetails(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            		if(piList!=null && piList.size()>0)
            		{
            			ProjectInitiation aboveMarketProjectInitiation = piList.get(0);
            			this.projectInitiation.setDeviationFromSM(aboveMarketProjectInitiation.getDeviationFromSM());
            		}
            	}
            	
                List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
                for(Integer i : attachmentMap.keySet())
                {
                    abList.addAll(attachmentMap.get(i));
                }
                attachmentUser = pibManager.getAttachmentUser(abList);

                // Moved from Input Action --
                //    String status=ribDocument.getProperties().get(SynchroConstants.STAGE_STATUS);
                stageId = SynchroGlobal.getProjectActivityTab().get(INPUT);
                approvers = stageManager.getStageApprovers(stageId.longValue(), project);
                editStage=SynchroPermHelper.canEditProjectByStatus(projectID);
                
                List<ProposalInitiation> iniList = this.proposalManager.getProposalDetails(projectID);
                if(iniList!=null && iniList.size()>0)
                {
                    for(ProposalInitiation pi:iniList)
                    {
                        if(pi.getIsAwarded())
                        {
                            setIsProposalAwarded(pi.getIsAwarded());
                        }
                    }

                }

                //String notRoles = SynchroConstants.PROJECT_OWNER_ROLE+","+SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME+","+SynchroConstants.JIVE_COMMUNICATION_AGECNY_GROUP_NAME+","+SynchroConstants.JIVE_PROCUREMENT_APPROVERS_GROUP_NAME+","+SynchroConstants.JIVE_LEGAL_APPROVERS_GROUP_NAME;
                String notRoles = SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME;
                pibCompleteNotifyAgencyRecipents = stageManager.getNotificationRecipients(notRoles, projectID, endMarketId);
                // As of now pibCompleteNotifyAgencyRecipents and pibNotifyAgencyRecipents are same.

                pibNotifyAgencyRecipents = stageManager.getNotificationRecipients(notRoles, projectID, endMarketId);

                // TODO Add other roles here
                String notAboveMarketRoles = SynchroConstants.PROJECT_OWNER_ROLE +","+ SynchroConstants.SYNCHRO_GLOBAL_PROJECT_CONTACT_GROUP_NAME;
                pibNotifyAboveMarketRecipents = stageManager.getNotificationRecipients(notAboveMarketRoles, projectID, endMarketId);
                pibApproveChangesRecipents = stageManager.getNotificationRecipients(notAboveMarketRoles, projectID, endMarketId);
                
                aboveMarketProjectContact = stageManager.getNotificationRecipients(SynchroConstants.SYNCHRO_GLOBAL_PROJECT_CONTACT_GROUP_NAME, projectID, endMarketId);
                
                String baseUrl = URLUtils.getBaseURL(request);
                String stageUrl = baseUrl+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
                
                if(pibCompleteNotifyAgencySubject==null)
                {
                    pibCompleteNotifyAgencySubject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
                    pibCompleteNotifyAgencySubject=pibCompleteNotifyAgencySubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    pibCompleteNotifyAgencySubject=pibCompleteNotifyAgencySubject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));

                }
                if(pibCompleteNotifyAgencyMessageBody==null)
                {
                   // String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
                    pibCompleteNotifyAgencyMessageBody = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
                    pibCompleteNotifyAgencyMessageBody=pibCompleteNotifyAgencyMessageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    pibCompleteNotifyAgencyMessageBody=pibCompleteNotifyAgencyMessageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    pibCompleteNotifyAgencyMessageBody=pibCompleteNotifyAgencyMessageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }

                if(pibNotifyAgencySubject==null)
                {
                    pibNotifyAgencySubject = TemplateUtil.getTemplate("pib.notifyAgency.subject", JiveGlobals.getLocale());
                    pibNotifyAgencySubject=pibNotifyAgencySubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(project.getProjectID()));
                    pibNotifyAgencySubject=pibNotifyAgencySubject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));

                }
                if(pibNotifyAgencyMessageBody==null)
                {
                    //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
                    pibNotifyAgencyMessageBody = TemplateUtil.getHtmlEscapedTemplate("pib.notifyAgency.htmlBody", JiveGlobals.getLocale());
                    pibNotifyAgencyMessageBody=pibNotifyAgencyMessageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(project.getProjectID()));
                    pibNotifyAgencyMessageBody=pibNotifyAgencyMessageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    pibNotifyAgencyMessageBody=pibNotifyAgencyMessageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }

                if(pibNotifyAboveMarketSubject==null)
                {
                    pibNotifyAboveMarketSubject = TemplateUtil.getTemplate("pib.notifyAboveMarketContacts.subject", JiveGlobals.getLocale());
                    pibNotifyAboveMarketSubject=pibNotifyAboveMarketSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    pibNotifyAboveMarketSubject=pibNotifyAboveMarketSubject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));

                }
                if(pibNotifyAboveMarketMessageBody==null)
                {
                  //  String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
                    pibNotifyAboveMarketMessageBody = TemplateUtil.getHtmlEscapedTemplate("pib.notifyAboveMarketContacts.htmlBody", JiveGlobals.getLocale());
                    pibNotifyAboveMarketMessageBody=pibNotifyAboveMarketMessageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    pibNotifyAboveMarketMessageBody=pibNotifyAboveMarketMessageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    pibNotifyAboveMarketMessageBody=pibNotifyAboveMarketMessageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }

                if(pibApproveChangesSubject==null)
                {
                    pibApproveChangesSubject = TemplateUtil.getTemplate("pib.approveChanges.subject", JiveGlobals.getLocale());
                    pibApproveChangesSubject=pibApproveChangesSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    pibApproveChangesSubject=pibApproveChangesSubject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));

                }
                if(pibApproveChangesMessageBody==null)
                {
                  //  String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
                    pibApproveChangesMessageBody = TemplateUtil.getHtmlEscapedTemplate("pib.approveChanges.htmlBody", JiveGlobals.getLocale());
                    pibApproveChangesMessageBody=pibApproveChangesMessageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    pibApproveChangesMessageBody=pibApproveChangesMessageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    pibApproveChangesMessageBody=pibApproveChangesMessageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                    if(!isAboveMarket)
                    {
                    	pibApproveChangesMessageBody=pibApproveChangesMessageBody.replaceAll("\\$\\{endMarket\\}", SynchroGlobal.getEndMarkets().get(endMarketId.intValue()));
                    }
                }
                
                if(subjectSendToProjOwner==null)
        		{
        			//subjectSendToProjOwner=String.format(SynchroGlobal.EmailNotification.PROPOSAL_SEND_TO_PROJECT_OWNER.getSubject(),project.getName());
        			//subject=String.format(SynchroGlobal.EmailNotification.PROPOSAL_REQ_CLARIFICATION.getSubject(),project.getName());
        			subjectSendToProjOwner = TemplateUtil.getTemplate("pib.send.to.projectowner.subject", JiveGlobals.getLocale());
        			subjectSendToProjOwner=subjectSendToProjOwner.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        			subjectSendToProjOwner=subjectSendToProjOwner.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        			
        		}
        		if(messageBodySendToProjOwner==null)
        		{
        		//	String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
        			//messageBodySendToProjOwner=String.format(SynchroGlobal.EmailNotification.PROPOSAL_SEND_TO_PROJECT_OWNER.getMessageBody(),project.getName(),stageUrl,stageUrl);
        			messageBodySendToProjOwner = TemplateUtil.getHtmlEscapedTemplate("pib.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
        			messageBodySendToProjOwner=messageBodySendToProjOwner.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        			messageBodySendToProjOwner=messageBodySendToProjOwner.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageBodySendToProjOwner=messageBodySendToProjOwner.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        		}
        		
        		if(subjectSendToProjectContact==null)
        		{
        			
        			subjectSendToProjectContact = TemplateUtil.getTemplate("pib.send.to.spi.subject", JiveGlobals.getLocale());
        			subjectSendToProjectContact=subjectSendToProjectContact.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        			subjectSendToProjectContact=subjectSendToProjectContact.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        			
        		}
        		if(messageBodySendToProjectContact==null)
        		{
        		//	String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
        			//messageBodySendToSPI=String.format(SynchroGlobal.EmailNotification.PROPOSAL_SEND_TO_SPI.getMessageBody(),project.getName(),stageUrl,stageUrl);
        			messageBodySendToProjectContact = TemplateUtil.getHtmlEscapedTemplate("pib.send.to.spi.htmlBody", JiveGlobals.getLocale());
        			messageBodySendToProjectContact=messageBodySendToProjectContact.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        			messageBodySendToProjectContact=messageBodySendToProjectContact.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        			messageBodySendToProjectContact=messageBodySendToProjectContact.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        		}
        		HashSet<User> endMarketProjectContacts = (HashSet<User>) synchroUtils.getEndMarketContacts(projectID);
                
            	if(endMarketProjectContacts!=null && endMarketProjectContacts.size()>0)
                {
                     for(User user: endMarketProjectContacts)
                     {
                         if(endMarketProjectUsers!=null && endMarketProjectUsers.length()>0)
                         {
                        	 endMarketProjectUsers = endMarketProjectUsers+","+user.getEmail();
                         }
                         else
                         {
                        	 endMarketProjectUsers = user.getEmail();
                         }
                     }
                }
            	if(subjectNotifyEndMarketContacts==null)
        		{
        			
            		subjectNotifyEndMarketContacts = TemplateUtil.getTemplate("pib.notifyAgency.endMarketContacts.subject", JiveGlobals.getLocale());
            		subjectNotifyEndMarketContacts=subjectNotifyEndMarketContacts.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(project.getProjectID()));
            		subjectNotifyEndMarketContacts=subjectNotifyEndMarketContacts.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        			
        		}
        		if(messageNotifyEndMarketContacts==null)
        		{
        		//	String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
        			//messageBodySendToSPI=String.format(SynchroGlobal.EmailNotification.PROPOSAL_SEND_TO_SPI.getMessageBody(),project.getName(),stageUrl,stageUrl);
        			messageNotifyEndMarketContacts = TemplateUtil.getHtmlEscapedTemplate("pib.notifyAgency.endMarketContacts.htmlBody", JiveGlobals.getLocale());
        			messageNotifyEndMarketContacts=messageNotifyEndMarketContacts.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(project.getProjectID()));
        			messageNotifyEndMarketContacts=messageNotifyEndMarketContacts.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        			messageNotifyEndMarketContacts=messageNotifyEndMarketContacts.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        		}
            
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
            
            List<String> otherBATList = new ArrayList<String>();
            String otherSPIUsers = pibManager.getOtherSPIContact(projectID, endMarketId);
            if(otherSPIUsers!=null && !otherSPIUsers.equals(""))
            {
            	if(otherSPIUsers.contains(","))
            	{
            		
            		String splitUsers[] = otherSPIUsers.split(",");
            		for(String spUser:splitUsers)
            		{
            			try
            			{
            				otherBATList.add(userManager.getUser(new Long(spUser)).getName());
            			}
            			catch(UserNotFoundException ue)
                        {
                            LOG.error("User Not Found while fetching other SPI Contacts" + spUser);
                        }
            		}
            	}
            	else
            	{
            		try
        			{
            			otherBATList.add(userManager.getUser(new Long(otherSPIUsers)).getName());
        			}
        			catch(UserNotFoundException ue)
                    {
                        LOG.error("User Not Found while fetching other SPI Contacts" + otherSPIUsers);
                    }
            	}
            }
            String otherLegalUsers = pibManager.getOtherLegalContact(projectID, endMarketId);
            
            if(otherLegalUsers!=null && !otherLegalUsers.equals(""))
            {
            	if(otherLegalUsers.contains(","))
            	{
            		
            		String splitUsers[] = otherLegalUsers.split(",");
            		for(String spUser:splitUsers)
            		{
            			try
            			{
            				otherBATList.add(userManager.getUser(new Long(spUser)).getName());
            			}
            			catch(UserNotFoundException ue)
                        {
                            LOG.error("User Not Found while fetching other Legal Contacts" + spUser);
                        }
            		}
            	}
            	else
            	{
            		try
        			{
            			otherBATList.add(userManager.getUser(new Long(otherLegalUsers)).getName());
        			}
        			catch(UserNotFoundException ue)
                    {
                        LOG.error("User Not Found while fetching other Legal Contacts" + otherLegalUsers);
                    }
            	}
            }
            String otherProductUsers = pibManager.getOtherProductContact(projectID, endMarketId);
            if(otherProductUsers!=null && !otherProductUsers.equals(""))
            {
            	if(otherProductUsers.contains(","))
            	{
            		
            		String splitUsers[] = otherProductUsers.split(",");
            		for(String spUser:splitUsers)
            		{
            			try
            			{
            				otherBATList.add(userManager.getUser(new Long(spUser)).getName());
            			}
            			catch(UserNotFoundException ue)
                        {
                            LOG.error("User Not Found while fetching other Product Contacts" + spUser);
                        }
            		}
            	}
            	else
            	{
            		try
        			{
            			otherBATList.add(userManager.getUser(new Long(otherProductUsers)).getName());
        			}
        			catch(UserNotFoundException ue)
                    {
                        LOG.error("User Not Found while fetching other Product Contacts" + otherProductUsers);
                    }
            	}
            }
            
            String otherAgencyUsers = pibManager.getOtherAgencyContact(projectID, endMarketId);
            
            if(otherAgencyUsers!=null && !otherAgencyUsers.equals(""))
            {
            	if(otherAgencyUsers.contains(","))
            	{
            		
            		String splitUsers[] = otherAgencyUsers.split(",");
            		for(String spUser:splitUsers)
            		{
            			try
            			{
            				otherBATList.add(userManager.getUser(new Long(spUser)).getName());
            			}
            			catch(UserNotFoundException ue)
                        {
                            LOG.error("User Not Found while fetching other Agency Contacts" + spUser);
                        }
            		}
            	}
            	else
            	{
            		try
        			{
            			otherBATList.add(userManager.getUser(new Long(otherAgencyUsers)).getName());
        			}
        			catch(UserNotFoundException ue)
                    {
                        LOG.error("User Not Found while fetching other Agency Contacts" + otherAgencyUsers);
                    }
            	}
            }
            
            otherBATUsers = Joiner.on(",").join(otherBATList);

            // Contenttype check is required to skip the below binding in case odf adding attachments
            if(getRequest().getMethod() == "POST" && !getRequest().getContentType().startsWith("multipart/form-data") && getRequest().getParameter("attachmentId")==null) {

                ServletRequestDataBinder binder = new ServletRequestDataBinder(this.projectInitiation);
                binder.bind(getRequest());
              /*  if(binder.getBindingResult().hasErrors()){
                    LOG.debug("Error occurred while binding the request object with the PIB bean.");
                    input();
                }*/
                if(binder.getBindingResult().hasErrors()){
                    LOG.debug("Error occurred while binding the request object with the PIB bean.");
                    if(projectInitiation.getLatestEstimate()==null)
                    {
                    	//projectInitiation.setLatestEstimate(BigDecimal.valueOf(new Integer("0")));
                    }
                    input();
                }

                LOG.info("Checking NON KANTAR PARAMAETER -----" + getRequest().getParameter("nonKantar"));
                LOG.info("Checking KANTAR PARAMAETER -----" + getRequest().getParameter("kantar"));
                LOG.info("Checking agencyUserBATContact1 PARAMAETER -----" + getRequest().getParameter("agencyUserBATContact1"));
                
                if(getRequest().getParameter("kantar")!=null && getRequest().getParameter("kantar").equals("true"))
                {
                	 //projectInitiation.setNonKantar(true);
                	projectInitiation.setNonKantar(new Integer("1"));
                
                 // The below step is done for retaining the original values in case there is no change in any of the user.
                    if(getRequest().getParameter("agencyContact1")!=null && !getRequest().getParameter("agencyContact1").equals(""))
                    {
                        projectInitiation.setAgencyContact1(Long.parseLong(getRequest().getParameter("agencyContact1")));
                    }
                    if(getRequest().getParameter("agencyUserBATContact1Optional")!=null && !getRequest().getParameter("agencyUserBATContact1Optional").equals(""))
                    {
                        projectInitiation.setAgencyContact1Optional(Long.parseLong(getRequest().getParameter("agencyUserBATContact1Optional")));
                    }
                    // This has been as part of Jive 8 upgradation to set the Agency 1 Option User
                    if((getRequest().getParameter("agencyContact1Optional")==null || getRequest().getParameter("agencyContact1Optional").equalsIgnoreCase("")) && (pibStakeholderList.getAgencyContact1Optional()!=null && pibStakeholderList.getAgencyContact1Optional().intValue() > 0))
                    {
                        projectInitiation.setAgencyContact1Optional(pibStakeholderList.getAgencyContact1Optional());
                    }
                }
                //https://svn.sourcen.com/issues/19669
                else if(getRequest().getParameter("nonKantar")!=null && getRequest().getParameter("nonKantar").equals("true"))
                {
                	 //projectInitiation.setNonKantar(false);
                	projectInitiation.setNonKantar(new Integer("2"));
                	
                	if(getRequest().getParameter("agencyUserBATContact1")!=null && !getRequest().getParameter("agencyUserBATContact1").equals(""))
                     {
                         projectInitiation.setAgencyContact1(Long.parseLong(getRequest().getParameter("agencyUserBATContact1")));
                     }
                     if(getRequest().getParameter("agencyUserBATContact1Optional")!=null && !getRequest().getParameter("agencyUserBATContact1Optional").equals(""))
                     {
                         projectInitiation.setAgencyContact1Optional(Long.parseLong(getRequest().getParameter("agencyUserBATContact1Optional")));
                     }
                     // This has been as part of Jive 8 upgradation to set the Agency 1 Option User
                     if((getRequest().getParameter("agencyUserBATContact1Optional")==null || getRequest().getParameter("agencyUserBATContact1Optional").equalsIgnoreCase("")) && (pibStakeholderList.getAgencyContact1Optional()!=null && pibStakeholderList.getAgencyContact1Optional().intValue() > 0))
                     {
                         projectInitiation.setAgencyContact1Optional(pibStakeholderList.getAgencyContact1Optional());
                     }
                }
                else
                {
                	 //projectInitiation.setNonKantar(false);
                	projectInitiation.setNonKantar(new Integer("0"));
                }

                // The below step is done for retaining the original values in case there is no change in any of the user.
                
               
                if(projectInitiation.getAgencyContact1()==null && getRequest().getParameter("agencyContact1")!=null && !getRequest().getParameter("agencyContact1").equals(""))
                {
                	
                	projectInitiation.setAgencyContact1(Long.parseLong(getRequest().getParameter("agencyContact1")));
                }
                else if(projectInitiation.getAgencyContact1()!=null && getRequest().getParameter("agencyContact1")!=null && !getRequest().getParameter("agencyContact1").equals("") && !((projectInitiation.getAgencyContact1()+"").equalsIgnoreCase(getRequest().getParameter("agencyContact1"))))
                {
                	
                	if(!getRequest().getParameter("agencyContact1").equalsIgnoreCase("0"))
                	{
                		projectInitiation.setAgencyContact1(Long.parseLong(getRequest().getParameter("agencyContact1")));
                	}
                }
               
                // This is done in case the Agency contact is there in from PIBStakeholderList table but from UI it is coming null
                // https://svn.sourcen.com/issues/20035
                if(projectInitiation.getAgencyContact1()==null && pibStakeholderList!=null && pibStakeholderList.getAgencyContact1()!=null)
                {
                	projectInitiation.setAgencyContact1(pibStakeholderList.getAgencyContact1());
                }
                
           
                
                if(projectInitiation.getAgencyContact2()==null && getRequest().getParameter("agencyContact2")!=null && !getRequest().getParameter("agencyContact2").equals(""))
                {
                    projectInitiation.setAgencyContact2(Long.parseLong(getRequest().getParameter("agencyContact2")));
                }
                if(projectInitiation.getAgencyContact3()==null && getRequest().getParameter("agencyContact3")!=null && !getRequest().getParameter("agencyContact3").equals(""))
                {
                    projectInitiation.setAgencyContact3(Long.parseLong(getRequest().getParameter("agencyContact3")));
                }
                if(projectInitiation.getAgencyContact1Optional()==null && getRequest().getParameter("agencyContact1Optional")!=null && !getRequest().getParameter("agencyContact1Optional").equals(""))
                {
                    projectInitiation.setAgencyContact1Optional(Long.parseLong(getRequest().getParameter("agencyContact1Optional")));
                }
                if(projectInitiation.getAgencyContact2Optional()==null && getRequest().getParameter("agencyContact2Optional")!=null && !getRequest().getParameter("agencyContact2Optional").equals(""))
                {
                    projectInitiation.setAgencyContact2Optional(Long.parseLong(getRequest().getParameter("agencyContact2Optional")));
                }
                if(projectInitiation.getAgencyContact3Optional()==null && getRequest().getParameter("agencyContact3Optional")!=null && !getRequest().getParameter("agencyContact3Optional").equals(""))
                {
                    projectInitiation.setAgencyContact3Optional(Long.parseLong(getRequest().getParameter("agencyContact3Optional")));
                }
                
                if(projectInitiation.getGlobalLegalContact()==null && getRequest().getParameter("globalLegalContact")!=null && !getRequest().getParameter("globalLegalContact").equals(""))
                {
                    projectInitiation.setGlobalLegalContact(Long.parseLong(getRequest().getParameter("globalLegalContact")));
                }
                if(projectInitiation.getGlobalProcurementContact()==null && getRequest().getParameter("globalProcurementContact")!=null && !getRequest().getParameter("globalProcurementContact").equals(""))
                {
                    projectInitiation.setGlobalProcurementContact(Long.parseLong(getRequest().getParameter("globalProcurementContact")));
                }
                if(projectInitiation.getGlobalCommunicationAgency()==null && getRequest().getParameter("globalCommunicationAgency")!=null && !getRequest().getParameter("globalCommunicationAgency").equals(""))
                {
                    projectInitiation.setGlobalCommunicationAgency(Long.parseLong(getRequest().getParameter("globalCommunicationAgency")));
                }
                if(projectInitiation.getSpiContact()==null && getRequest().getParameter("spiContact")!=null && !getRequest().getParameter("spiContact").equals(""))
                {
                    projectInitiation.setSpiContact(Long.parseLong(getRequest().getParameter("spiContact")));
                }

                if(StringUtils.isNotBlank(getRequest().getParameter("stimuliDate"))) {
                    projectInitiation.setStimuliDate(DateUtils.parse(getRequest().getParameter("stimuliDate")));
                }

                // To map the Project Parameters
                binder = new ServletRequestDataBinder(this.project);
                binder.bind(getRequest());

//                if(this.project.getMethodologyGroup() != null && this.project.getMethodologyGroup().intValue() > 0) {
//                    Long mtId = SynchroGlobal.getMethodologyTypeByGroup(this.project.getMethodologyGroup());
//                    this.project.setMethodologyType(mtId);
//                }

//                if(this.project.getProposedMethodology() != null && this.project.getProposedMethodology().size() > 0) {
//                    this.project.setMethodologyType(SynchroGlobal.getMethodologyTypeByProsedMethodologies(this.project.getProposedMethodology()));
//                }

                if(binder.getBindingResult().hasErrors()){
                    LOG.debug("Error occurred while binding the request object with the Project bean.");
                    input();
                }
                // These has been done as part of Jive 8 Upgradation
                if(project.getProjectOwner()==null && getRequest().getParameter("projectOwnerOri")!=null)
                {
                	project.setProjectOwner(new Long(getRequest().getParameter("projectOwnerOri")));
                }
                if(projectInitiation.getGlobalLegalContact()==null && getRequest().getParameter("globalLegalContactOri")!=null)
                {
                	projectInitiation.setGlobalLegalContact(new Long(getRequest().getParameter("globalLegalContactOri")));
                }
                if(projectInitiation.getProductContact()==null && getRequest().getParameter("productContactOri")!=null)
                {
                	projectInitiation.setProductContact(new Long(getRequest().getParameter("productContactOri")));
                }
                if(projectInitiation.getGlobalProcurementContact()==null && getRequest().getParameter("globalProcurementContactOri")!=null)
                {
                	projectInitiation.setGlobalProcurementContact(new Long(getRequest().getParameter("globalProcurementContactOri")));
                }
                if(projectInitiation.getGlobalCommunicationAgency()==null && getRequest().getParameter("globalCommunicationAgencyOri")!=null)
                {
                	projectInitiation.setGlobalCommunicationAgency(new Long(getRequest().getParameter("globalCommunicationAgencyOri")));
                }
                
                if(project.getProjectOwner()==null && getRequest().getParameter("projectOwner")!=null)
                {
                    project.setProjectOwner(Long.parseLong(getRequest().getParameter("projectOwner")));
                }
                /* if(getRequest().getParameter("updatedSingleMarketId")!=null)
                {
                    updatedSingleMarketId = Long.valueOf(getRequest().getParameter("updatedSingleMarketId"));
                    //TODO : Currently this is done to update the Single End Market for grailPIB,
                    // grailPIBReporting and grailpibstakeholderlist and grailendmarketinvestment tables
                    // Need to revisit this logic in case of Multiple End Markets
                    projectInitiation.setEndMarketID(updatedSingleMarketId);
                }*/
                
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
                
                // To map the Kantar Methodology Waiver Fields

                pibKantarMethodologyWaiver.setProjectID(projectID);
                pibKantarMethodologyWaiver.setMethodologyDeviationRationale(getRequest().getParameter("methodologyDeviationRationale"));
                try
                {
                	pibKantarMethodologyWaiver.setEndMarketID(Long.valueOf(getRequest().getParameter("endMarketId")));
                	pibKantarMethodologyWaiver.setMethodologyApprover(Long.valueOf(getRequest().getParameter("methodologyApprover")));
                }
                catch(NumberFormatException ne)
                {

                }
                if(getRequest().getParameter("kantarMethodologyWaiverAction")!=null && getRequest().getParameter("kantarMethodologyWaiverAction").equals("Approve"))
                {
                	kantarMethodologyWaiverAction = "Approve";
                    pibKantarMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
                }
                if(getRequest().getParameter("kantarMethodologyWaiverAction")!=null && getRequest().getParameter("kantarMethodologyWaiverAction").equals("Reject"))
                {
                	kantarMethodologyWaiverAction = "Reject";
                    pibKantarMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
                }
                if(getRequest().getParameter("kantarMethodologyWaiverAction")!=null && getRequest().getParameter("kantarMethodologyWaiverAction").equals("Send for Information"))
                {
                	kantarMethodologyWaiverAction = "Send for Information";
                }
                if(getRequest().getParameter("kantarMethodologyWaiverAction")!=null && getRequest().getParameter("kantarMethodologyWaiverAction").equals("Request more Information"))
                {
                	kantarMethodologyWaiverAction = "Request more Information";
                    pibKantarMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
                }
                
                pageRequest = getRequest().getParameter("pageRequest");
                
                if(StringUtils.isNotBlank(getRequest().getParameter("startDate"))) {
                    this.project.setStartDate(DateUtils.parse(getRequest().getParameter("startDate")));
                }
                if(StringUtils.isNotBlank(getRequest().getParameter("endDate"))) {
                    this.project.setEndDate(DateUtils.parse(getRequest().getParameter("endDate")));
                }

                if(getRequest().getParameter("marketAmendmentType")!=null && getRequest().getParameter("marketAmendmentType").equals("ActionStandard"))
                {
                    marketAmendmentType = "ActionStandard";
                }
                if(getRequest().getParameter("marketAmendmentType")!=null && getRequest().getParameter("marketAmendmentType").equals("ResearchDesign"))
                {
                    marketAmendmentType = "ResearchDesign";
                }
                if(getRequest().getParameter("marketAmendmentType")!=null && getRequest().getParameter("marketAmendmentType").equals("SampleProfile"))
                {
                    marketAmendmentType = "SampleProfile";
                }
                if(getRequest().getParameter("marketAmendmentType")!=null && getRequest().getParameter("marketAmendmentType").equals("StimulusMaterial"))
                {
                    marketAmendmentType = "StimulusMaterial";
                }
            }
        }

        
      //Audit Logs: Get Project & ProjectInitiationfrom Database for compare needed in Audit trails
        if(projectID!=null && projectID>0)
        {
        	project_DB = this.synchroProjectManager.get(projectID);
        	if(endMarketId!=null)
        	{
        		List<ProjectInitiation> initiationList_DB = this.pibManager.getPIBDetails(project.getProjectID(),endMarketId);
            	if( initiationList_DB != null && initiationList_DB.size() > 0) {
            		projectInitiation_DB = initiationList_DB.get(0);            		
            	}
            	endMarketDetails_DB = this.synchroProjectManager.getEndMarketDetails(projectID);
            	pibStakeholderList_DB = this.pibManager.getPIBStakeholderList(projectID, endMarketId);
            	pibReporting_DB = this.pibManager.getPIBReporting(projectID, endMarketId);
        	}
        	
        }
    }

    public String input() {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        if (SynchroPermHelper.hasPIBAccessMultiMarket(projectID) || SynchroPermHelper.canAccessProject(projectID)) {
        	
        	if(projectID!=null && endMarketId!=null &&  endMarketId>0)
            {
            	Integer status = synchroProjectManager.getEndMarketStatus(projectID, endMarketId);
            	if(status == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || status == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal())
            	{
            		canEditEM = false;
            	}
            	else if(status == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal())
            	{
            		return UNAUTHORIZED;            		
            	}
            }
        	
            return INPUT;
        }
        else
        {
            return UNAUTHORIZED;
        }

        

    }

    public String execute(){

        LOG.info("Save the PIB Details ...."+projectInitiation);
        
        Boolean manFieldsError = false;
        //  if( projectInitiation != null && ribDocument != null){
        if( projectInitiation != null){
            projectInitiation.setProjectID(projectID);
            projectInitiation.setEndMarketID(endMarketId);

            List<Long> activeEndMarketIds = new ArrayList<Long>();  
            List<ProposalInitiation> proposalAgencyList = this.proposalManager.getProposalDetails(projectID);
            
            for(ProposalInitiation pi:proposalAgencyList)
            {
               	if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
            	{
            		activeEndMarketIds.add(pi.getEndMarketID());
            	}
            }
            
           
           
            // PIB stage should be editable even if it is completed. Need to send the notification in that case.
            if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal() )
            {
                
            	if(pibAboveMarketStakeholderList==null || pibAboveMarketStakeholderList.getProjectID()==null)
            	{
            		pibAboveMarketStakeholderList=this.pibManager.getPIBStakeholderList(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            	}
            	 //boolean agencyChanged = false;
                if(pibAboveMarketStakeholderList.getAgencyContact1()!=null && pibAboveMarketStakeholderList.getAgencyContact1() > 0
                        && !SynchroUtils.isReferenceID(pibAboveMarketStakeholderList.getAgencyContact1()))
                {
                  
                    
                	if(projectInitiation.getAgencyContact1()!=null && pibAboveMarketStakeholderList.getAgencyContact1().intValue()!=projectInitiation.getAgencyContact1().intValue())
                    {
                    
                		if(!isProposalAwarded)
                        {
                			Map<Integer, List<AttachmentBean>> attBean = null;
                			// This is done for Quick Fix. The attachments should not be lost in case the Agency user is changed.It will return the attachments for the existing agency user.
                			attBean = proposalManager.removeAgencyOnly(pibAboveMarketStakeholderList.getProjectID(), pibAboveMarketStakeholderList.getEndMarketID(), pibAboveMarketStakeholderList.getAgencyContact1());
                            stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact1(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                            // This is done for Quick Fix. The attachments should not be lost in case the Agency user is changed.
                            if(attBean!=null)
                            {
                            	stageManager.copyAttachments(attBean, projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact1(), projectInitiation.getAgencyContact1());
                            }
                            // This is done for Quick Fix. The attachments should not be lost in case the Agency user is changed.
                            proposalManager.removeAttachment(attBean);
                            
                            for(Long actEndMarketId : activeEndMarketIds)
                            {
                            	 proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), actEndMarketId, pibAboveMarketStakeholderList.getAgencyContact1());
                                 stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact1(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                            }
                            //agencyChanged=true;
                           
                        }
                        // Admin user changes for https://www.svn.sourcen.com/issues/19286
                        if(isProposalAwarded && SynchroPermHelper.isSynchroAdmin(getUser()))
                        {
                    
                        	proposalManager.updateAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact1(),pibAboveMarketStakeholderList.getAgencyContact1());
                        	for(Long actEndMarketId : activeEndMarketIds)
                            {
                        		proposalManager.updateAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact1(),pibAboveMarketStakeholderList.getAgencyContact1());
                            }
                        }
                    }
                    
                    // isAboveMarket check is required as the Agency contacts are saved only for Above Markets.
                    // https://www.svn.sourcen.com/issues/19049
                    else if(projectInitiation.getAgencyContact1()==null && isAboveMarket)
                    {
                        if(!isProposalAwarded)
                        {
                            proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), pibAboveMarketStakeholderList.getEndMarketID(), pibAboveMarketStakeholderList.getAgencyContact1());
                            for(Long actEndMarketId : activeEndMarketIds)
                            {
                            	 proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), actEndMarketId, pibAboveMarketStakeholderList.getAgencyContact1());
                            }
                        }
                    }

                }
                else if(projectInitiation.getAgencyContact1()!=null && (pibAboveMarketStakeholderList.getAgencyContact1()==null || pibAboveMarketStakeholderList.getAgencyContact1()==0))
                {
                    if(!isProposalAwarded)
                    {
                        stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact1(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        for(Long actEndMarketId : activeEndMarketIds)
                        {
                        	stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact1(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        }
                    }
                    // Admin user changes for https://www.svn.sourcen.com/issues/19286
                    if(isProposalAwarded && SynchroPermHelper.isSynchroAdmin(getUser()))
                    {
                    	stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact1(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        for(Long actEndMarketId : activeEndMarketIds)
                        {
                        	stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact1(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        }
                    }
                }

                if(pibAboveMarketStakeholderList.getAgencyContact2()!=null && pibAboveMarketStakeholderList.getAgencyContact2() > 0
                        && !SynchroUtils.isReferenceID(pibAboveMarketStakeholderList.getAgencyContact2()))
                {
                   
                    if(projectInitiation.getAgencyContact2()!=null && pibAboveMarketStakeholderList.getAgencyContact2().intValue()!=projectInitiation.getAgencyContact2().intValue())
                    {
                        if(!isProposalAwarded)
                        {
                            proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), pibAboveMarketStakeholderList.getEndMarketID(), pibAboveMarketStakeholderList.getAgencyContact2());
                            stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact2(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                            //agencyChanged=true;
                            for(Long actEndMarketId : activeEndMarketIds)
                            {
                            	 proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), actEndMarketId, pibAboveMarketStakeholderList.getAgencyContact2());
                                 stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact2(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                            }
                        }
                     // Admin user changes for https://www.svn.sourcen.com/issues/19286
                        if(isProposalAwarded && SynchroPermHelper.isSynchroAdmin(getUser()))
                        {
                        	proposalManager.updateAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact2(),pibAboveMarketStakeholderList.getAgencyContact2());
                        	for(Long actEndMarketId : activeEndMarketIds)
                            {
                        		proposalManager.updateAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact2(),pibAboveMarketStakeholderList.getAgencyContact2());
                            }
                        }
                    }
                    // isAboveMarket check is required as the Agency contacts are saved only for Above Markets.
                    // https://www.svn.sourcen.com/issues/19049
                    else if(projectInitiation.getAgencyContact2()==null && isAboveMarket)
                    {
                        if(!isProposalAwarded)
                        {
                            proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), pibAboveMarketStakeholderList.getEndMarketID(), pibAboveMarketStakeholderList.getAgencyContact2());
                            for(Long actEndMarketId : activeEndMarketIds)
                            {
                            	proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), actEndMarketId, pibAboveMarketStakeholderList.getAgencyContact2());
                            }
                        }
                    }

                }
                else if(projectInitiation.getAgencyContact2()!=null && (pibAboveMarketStakeholderList.getAgencyContact2()==null || pibAboveMarketStakeholderList.getAgencyContact2()==0))
                {
                    if(!isProposalAwarded)
                    {
                        stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact2(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        for(Long actEndMarketId : activeEndMarketIds)
                        {
                        	stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact2(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        }
                    }
                    
                 // Admin user changes for https://www.svn.sourcen.com/issues/19286
                    if(isProposalAwarded && SynchroPermHelper.isSynchroAdmin(getUser()))
                    {
                    	stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact2(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        for(Long actEndMarketId : activeEndMarketIds)
                        {
                        	stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact2(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        }
                    }
                }
                if(pibAboveMarketStakeholderList.getAgencyContact3()!=null && pibAboveMarketStakeholderList.getAgencyContact3() > 0
                        && !SynchroUtils.isReferenceID(pibAboveMarketStakeholderList.getAgencyContact3()))
                {
                  
                    if(projectInitiation.getAgencyContact3()!=null && pibAboveMarketStakeholderList.getAgencyContact3().intValue()!=projectInitiation.getAgencyContact3().intValue())
                    {
                        if(!isProposalAwarded)
                        {
                            proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), pibAboveMarketStakeholderList.getEndMarketID(), pibAboveMarketStakeholderList.getAgencyContact3());
                            stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact3(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                            //agencyChanged=true;
                            for(Long actEndMarketId : activeEndMarketIds)
                            {
                            	   proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), actEndMarketId, pibAboveMarketStakeholderList.getAgencyContact3());
                                   stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact3(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                            }
                        }
                     // Admin user changes for https://www.svn.sourcen.com/issues/19286
                        if(isProposalAwarded && SynchroPermHelper.isSynchroAdmin(getUser()))
                        {
                        	proposalManager.updateAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact3(),pibAboveMarketStakeholderList.getAgencyContact3());
                        	for(Long actEndMarketId : activeEndMarketIds)
                            {
                        		proposalManager.updateAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact3(),pibAboveMarketStakeholderList.getAgencyContact3());
                            }
                        }
                    }
                    // isAboveMarket check is required as the Agency contacts are saved only for Above Markets.
                    // https://www.svn.sourcen.com/issues/19049
                    else if(projectInitiation.getAgencyContact3()==null && isAboveMarket)
                    {
                        if(!isProposalAwarded)
                        {
                            proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), pibAboveMarketStakeholderList.getEndMarketID(), pibAboveMarketStakeholderList.getAgencyContact3());
                            for(Long actEndMarketId : activeEndMarketIds)
                            {
                            	proposalManager.removeAgency(pibAboveMarketStakeholderList.getProjectID(), actEndMarketId, pibAboveMarketStakeholderList.getAgencyContact3());
                            }
                        }
                    }

                }
                else if(projectInitiation.getAgencyContact3()!=null && (pibAboveMarketStakeholderList.getAgencyContact3()==null || pibAboveMarketStakeholderList.getAgencyContact3()==0))
                {
                    if(!isProposalAwarded)
                    {
                        stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact3(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        for(Long actEndMarketId : activeEndMarketIds)
                        {
                        	stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact3(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        }
                    }
                    
                 // Admin user changes for https://www.svn.sourcen.com/issues/19286
                    if(isProposalAwarded && SynchroPermHelper.isSynchroAdmin(getUser()))
                    {
                    	stageManager.saveAgency(projectID, pibAboveMarketStakeholderList.getEndMarketID(), projectInitiation.getAgencyContact3(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        for(Long actEndMarketId : activeEndMarketIds)
                        {
                        	stageManager.saveAgency(projectID, actEndMarketId, projectInitiation.getAgencyContact3(), SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                        }
                    }
                }
            	
            	
            	if(!isAboveMarket && !activeEndMarketIds.contains(endMarketId))
                {
                	if(projectInitiation.getBizQuestion()!=null && !projectInitiation.getBizQuestion().equals("")
                            && projectInitiation.getResearchObjective()!=null && !projectInitiation.getResearchObjective().equals("")
                            && projectInitiation.getActionStandard()!=null && !projectInitiation.getActionStandard().equals("")
                            && projectInitiation.getResearchDesign()!=null && !projectInitiation.getResearchDesign().equals("")
                            && projectInitiation.getSampleProfile()!=null && !projectInitiation.getSampleProfile().equals("")
                            && projectInitiation.getStimulusMaterial()!=null && !projectInitiation.getStimulusMaterial().equals("")
                          //  && projectInitiation.getOthers()!=null && !projectInitiation.getOthers().equals("")

                            && (projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation()|| projectInitiation.getPresentation()!=null && projectInitiation.getPresentation()
                            || projectInitiation.getFullreport()!=null && projectInitiation.getFullreport() || projectInitiation.getGlobalSummary()!=null && projectInitiation.getGlobalSummary())
                        //    && projectInitiation.getOtherReportingRequirements()!=null && !projectInitiation.getOtherReportingRequirements().equals("")
                            && projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)

                    {
                       projectInitiation.setStatus(SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
                       
                    }
                    else
                    {
                        projectInitiation.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
                    }
                }
                else
                {
	            	StringBuffer stakeholders = new StringBuffer();
	            	List<String> agencyStakeholders = new ArrayList<String>();
	                try
	                {
	                	//Project Owner
	                    stakeholders.append(userManager.getUser(project.getProjectOwner()).getEmail());
	                    
	                    //SPI Contact
	                  /*  if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact() > 0)
	                    {
	                        stakeholders.append(","+ userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail());
	                    }
	                   */
	                    //Agency Contact No 1 
	                    if(pibStakeholderList.getAgencyContact1()!=null && pibStakeholderList.getAgencyContact1() > 0)
	                    {
	                       // stakeholders.append(","+ userManager.getUser(pibStakeholderList.getAgencyContact1()).getEmail());
	                    	agencyStakeholders.add(userManager.getUser(pibStakeholderList.getAgencyContact1()).getEmail());
	                    }
	                    
	                    //Agency Contact No 2
	                    if(pibStakeholderList.getAgencyContact2()!=null && pibStakeholderList.getAgencyContact2() > 0)
	                    {
	                        //stakeholders.append(","+ userManager.getUser(pibStakeholderList.getAgencyContact2()).getEmail());
	                    	agencyStakeholders.add(userManager.getUser(pibStakeholderList.getAgencyContact2()).getEmail());
	                    }
	                    
	                    //Agency Contact No 3
	                    if(pibStakeholderList.getAgencyContact3()!=null && pibStakeholderList.getAgencyContact3() > 0)
	                    {
	                        //stakeholders.append(","+ userManager.getUser(pibStakeholderList.getAgencyContact3()).getEmail());
	                    	agencyStakeholders.add(userManager.getUser(pibStakeholderList.getAgencyContact3()).getEmail());
	                    }
	                    
	                    //Legal Contact
	                    stakeholders.append(","+ userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getEmail());
	                    
	                    //Procurement Contact
	                    if(pibStakeholderList.getGlobalProcurementContact()!=null && pibStakeholderList.getGlobalProcurementContact() > 0)
	                    {
	                        stakeholders.append(","+ userManager.getUser(pibStakeholderList.getGlobalProcurementContact()).getEmail());
	                    }
	                    
	                    //Communication Agency
	                    if(pibStakeholderList.getGlobalCommunicationAgency()!=null && pibStakeholderList.getGlobalCommunicationAgency() > 0)
	                    {
	                        stakeholders.append(","+ userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getEmail());
	                    }
	                }
	                catch(UserNotFoundException ue)
	                {
	                    LOG.error("User Not Found while sending email after PIB is complete" + ue.getMessage());
	                }
	                //TODO : To add the logic to compare the changes for End Market Details
	                projectInitiation.setIsEndMarketChanged(true);
	
	              /*  String sub = SynchroGlobal.EmailNotification.PIB_CHANGE_AFTER_COMPETION.getSubject();
	               * 
	                String emailBody = String.format(SynchroGlobal.EmailNotification.PIB_CHANGE_AFTER_COMPETION.getMessageBody(),project.getProjectID());
	                EmailMessage email = stageManager.populateNotificationEmail(stakeholders.toString(),sub , emailBody,null,null);
	                */
	                if(!StringUtils.areEqualIgnoreCase(pitUpdateOnly, "true"))
	                {
	               //  https://www.svn.sourcen.com/issues/18840
	                // PIB Notify Agency email sent to all the Project Stakeholders except Agencies
	                EmailMessage email = stageManager.populateNotificationEmail(stakeholders.toString(), null, null,"pib.notifyAgency.htmlBody","pib.notifyAgency.subject");
	              //  String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
	                String baseUrl = URLUtils.getBaseURL(request);
	                String stageUrl = baseUrl+"/synchro/pib-multi-details!input.jspa?projectID=" + project.getProjectID();
	                
	                if(email!=null)
	                {
					//email.getContext().put("projectId", projectID);
		                email.getContext().put("projectId", SynchroUtils.generateProjectCode(project.getProjectID()));
						email.getContext().put("projectName",project.getName());
						email.getContext().put ("stageUrl",stageUrl);
		                stageManager.sendNotification(getUser(), email);
	                }
		                //Code to fix SVN Bug #19079
		                // PIB Notify Agency email sent to all the added Agencies - Agency 1, Agency 2, Agency 3 ONE By ONE
		                for(String agencyEmail : agencyStakeholders)
		                {
		                	email = stageManager.populateNotificationEmail(agencyEmail, null, null,"pib.notifyAgency.htmlBody","pib.notifyAgency.subject");
			                email.getContext().put("projectId", SynchroUtils.generateProjectCode(project.getProjectID()));
							email.getContext().put("projectName",project.getName());
							email.getContext().put ("stageUrl",stageUrl);
			                stageManager.sendNotification(getUser(), email);
		                }
	                }
                }
            }
            else
            {
                //Update the Project Status to PIB OPEN when the PIB is saved
                project.setStatus(Long.valueOf(SynchroGlobal.Status.PIB_OPEN.ordinal()));
                if(projectInitiation.getBizQuestion()!=null && !projectInitiation.getBizQuestion().equals("")
                        && projectInitiation.getResearchObjective()!=null && !projectInitiation.getResearchObjective().equals("")
                        && projectInitiation.getActionStandard()!=null && !projectInitiation.getActionStandard().equals("")
                        && projectInitiation.getResearchDesign()!=null && !projectInitiation.getResearchDesign().equals("")
                        && projectInitiation.getSampleProfile()!=null && !projectInitiation.getSampleProfile().equals("")
                        && projectInitiation.getStimulusMaterial()!=null && !projectInitiation.getStimulusMaterial().equals("")
                      //  && projectInitiation.getOthers()!=null && !projectInitiation.getOthers().equals("")

                        && (projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation()|| projectInitiation.getPresentation()!=null && projectInitiation.getPresentation()
                        || projectInitiation.getFullreport()!=null && projectInitiation.getFullreport() || projectInitiation.getGlobalSummary()!=null && projectInitiation.getGlobalSummary())
                    //    && projectInitiation.getOtherReportingRequirements()!=null && !projectInitiation.getOtherReportingRequirements().equals("")
                        && projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)

                {
                    if(isAboveMarket)
                    {
                        if(projectInitiation.getLatestEstimate()!=null)
                        {
                            projectInitiation.setStatus(SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
                        }
                        else
                        {
                            projectInitiation.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
                        }
                    }
                    else
                    {
                        projectInitiation.setStatus(SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
                    }
                }
                else
                {
                    projectInitiation.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
                }
            }
            if(isMandatoryFieldsFilled(projectInitiation,isAboveMarket)) {
                //manFieldsError=true;
                manFieldsError=false;
            }
            else
            {
                manFieldsError=true;
            }
            
            project.setDescription(SynchroUtils.fixBulletPoint(project.getDescription()));

            if(this.project.getProposedMethodology() != null && this.project.getProposedMethodology().size() > 0) {
                this.project.setMethodologyType(SynchroGlobal.getMethodologyTypeByProsedMethodologies(this.project.getProposedMethodology()));
            }
            
            

            if(projectInitiation.getAgencyContact1() != null && projectInitiation.getAgencyContact1().intValue() > 0) {
                try {
                    Map<Long, ProfileFieldValue> profileFieldMap = profileManager.getProfile(userManager.getUser(projectInitiation.getAgencyContact1()));
                    if(profileFieldMap!=null && profileFieldMap.get(2L) != null) {
                        String deptVal = profileFieldMap.get(2L).getValue();
                        if(deptVal != null && !deptVal.equals("")) {
                            this.project.setAgencyDept(Long.parseLong(deptVal));
                        }
                    }
                } catch (UserNotFoundException e) {
                    LOG.error(e.getMessage());
                }
            }


            this.synchroProjectManager.save(project);
            /* if(updatedSingleMarketId!=null)
            {
                this.synchroProjectManager.updateSingleEndMarketId(projectID, updatedSingleMarketId);
                // TODO : Here add the logic to update the End Market Id in PIB corresponding tables
                this.pibManager.updatePIBMethWaiverSingleEndMarketId(projectID, updatedSingleMarketId);
                this.pibManager.updatePIBReportingSingleEndMarketId(projectID, updatedSingleMarketId);
                this.pibManager.updatePIBStakeholderListSingleEndMarketId(projectID, updatedSingleMarketId);

            }*/
            // Update the SPI contact
            // this.synchroProjectManager.updateSPIContact(projectID, projectInitiation.getEndMarketID(), projectInitiation.getSpiContact());
            if(request.getParameter("autoSave") != null) {
                this.isAutoSave = Boolean.parseBoolean(request.getParameter("autoSave"));
            } else {
                this.isAutoSave = false;
            }

            // https://svn.sourcen.com/issues/19652
            projectInitiation.setBizQuestion(SynchroUtils.fixBulletPoint(projectInitiation.getBizQuestion()));
            projectInitiation.setResearchObjective(SynchroUtils.fixBulletPoint(projectInitiation.getResearchObjective()));
            projectInitiation.setActionStandard(SynchroUtils.fixBulletPoint(projectInitiation.getActionStandard()));
            projectInitiation.setResearchDesign(SynchroUtils.fixBulletPoint(projectInitiation.getResearchDesign()));
            projectInitiation.setSampleProfile(SynchroUtils.fixBulletPoint(projectInitiation.getSampleProfile()));
            projectInitiation.setStimulusMaterial(SynchroUtils.fixBulletPoint(projectInitiation.getStimulusMaterial()));
            projectInitiation.setOthers(SynchroUtils.fixBulletPoint(projectInitiation.getOthers()));
            projectInitiation.setOtherReportingRequirements(SynchroUtils.fixBulletPoint(projectInitiation.getOtherReportingRequirements()));
            
            if(projectInitiation.getStatus()==SynchroGlobal.StageStatus.PIB_SAVED.ordinal())
            {
            	projectInitiation.setPibSaveDate(new Date(System.currentTimeMillis()));
            }
            if((projectInitiation.getLegalApprovalRcvd() && projectInitiation.getLegalApprover()!=null && !projectInitiation.getLegalApprover().equals(""))
    				|| projectInitiation.getLegalApprovalNotReq())
            {
            	projectInitiation.setPibLegalApprovalDate(new Date(System.currentTimeMillis()));
            }
            
            if(isSave) {
                projectInitiation.setCreationBy(getUser().getID());
                projectInitiation.setCreationDate(System.currentTimeMillis());

                projectInitiation.setModifiedBy(getUser().getID());
                projectInitiation.setModifiedDate(System.currentTimeMillis());

                if((saveEmailForm!=null &&  saveEmailForm.contains("saveEmailForm")) || isAutoSave)
                {
                	this.pibManager.saveMultiPIBDetails(projectInitiation,endMarketDetails,isAboveMarket, false);
                	//Audit Log: Add User Audit details for PIB Save               
                	SynchroLogUtils.PIBMultiSave(projectInitiation_DB, projectInitiation, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, endMarketId);
                }
                else
                {
                	this.pibManager.saveMultiPIBDetails(projectInitiation,endMarketDetails,isAboveMarket, true);
                	//Audit Log: Add User Audit details for PIB Save               
                	SynchroLogUtils.PIBMultiSave(projectInitiation_DB, projectInitiation, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, endMarketId);
                }
            }
            else {
                projectInitiation.setCreationBy(getUser().getID());
                projectInitiation.setCreationDate(System.currentTimeMillis());
                projectInitiation.setModifiedBy(getUser().getID());
                projectInitiation.setModifiedDate(System.currentTimeMillis());
               
               // this.pibManager.updateMultiMarketPIBDetails(projectInitiation,endMarketDetails,isAboveMarket);
                
                if((saveEmailForm!=null && saveEmailForm.contains("saveEmailForm")) || isAutoSave)
                {
                	 this.pibManager.updateMultiMarketPIBDetails(projectInitiation,endMarketDetails,isAboveMarket, false);
                	//Audit Log: Add User Audit details for PIB Save               
                 	SynchroLogUtils.PIBMultiSave(projectInitiation_DB, projectInitiation, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, endMarketId);
                }
                else
                {
                	 this.pibManager.updateMultiMarketPIBDetails(projectInitiation,endMarketDetails,isAboveMarket, true);
                	//Audit Log: Add User Audit details for PIB Save               
                 	SynchroLogUtils.PIBMultiSave(projectInitiation_DB, projectInitiation, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, endMarketId);
                }

            }


        } else {
            LOG.error("Project Initiation was null  ");
            addActionError("Project Initiation missing.");
        }
        
      //Audit Logs: PIB SAVE
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.PIB.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID(), endMarketId);
      /*  if(manFieldsError)
        {
            redirectURL=redirectURL+"&validationError=true";
          
        }
        */
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
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getBaseURL(request);
        String stageUrl = baseUrl+"/synchro/pib-multi-details!input.jspa?projectID=" + projectID;

        try
        {
            pibMethApp = userManager.getUser(pibMethodologyWaiver.getMethodologyApprover());
            if(project.getProjectOwner()!=null)
            {
                //projectOwnerEmail = userManager.getUser(project.getProjectOwner()).getEmail();
                projectOwnerEmail = SynchroUtils.getUserEmail(project.getProjectOwner());
            }
            /*  if(endMarketDetails.get(0).getSpiContact()!=null)
            {
                //spiContactEmail = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                spiContactEmail = SynchroUtils.getUserEmail(endMarketDetails.get(0).getSpiContact());
            }*/

        }
        catch(UserNotFoundException ue)
        {

        }
        
      //Save Audit logs for change in Methodology Waiver related fields
        if(projectID!=null && endMarketId!=null)
        {
        	final PIBMethodologyWaiver pibMethodologyWaiver_DB = this.pibManager.getPIBMethodologyWaiver(projectID, endMarketId);
            SynchroLogUtils.PIBMultiWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PIB.getId(), endMarketId);
        }
        
        if(methodologyWaiverAction!=null && methodologyWaiverAction.equals("Approve"))
        {
            this.pibManager.approvePIBMethodologyWaiver(pibMethodologyWaiver);

            //EmailNotification#4 Recipients for Approval
            String recp = projectOwnerEmail+","+spiContactEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.approve.meth.waiver.htmlBody","pib.approve.meth.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
          //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Methodology Waiver Approved");
	    	emailNotBean.setEmailSubject("Notification | Methodology Waiver Approved");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.approve"), project.getName(), 
            												project.getProjectID(), getUser().getID(), endMarketId);

        }
        else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Reject"))
        {
            this.pibManager.rejectPIBMethodologyWaiver(pibMethodologyWaiver);

            //EmailNotification#5 Recipients for Reject
            String recp = projectOwnerEmail+","+spiContactEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.reject.meth.waiver.htmlBody","pib.reject.meth.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Methodology Waiver Rejected");
	    	emailNotBean.setEmailSubject("Notification | Methodology Waiver Rejected");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.reject"), project.getName(), 
            												project.getProjectID(), getUser().getID(), endMarketId);
        }
        else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Send for Information"))
        {
            //EmailNotification#2 Recipients for Send for Information
            String recp = pibMethApp.getEmail()+","+projectOwnerEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.send.for.information.meth.waiver.htmlBody","pib.send.for.information.meth.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSubject("Notification | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);

            pibMethodologyWaiver.setCreationBy(getUser().getID());
            pibMethodologyWaiver.setCreationDate(System.currentTimeMillis());

            pibMethodologyWaiver.setModifiedBy(getUser().getID());
            pibMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
            pibMethodologyWaiver.setIsApproved(null);
            pibMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());

            //https://www.svn.sourcen.com/issues/17809 - Comments need to be saved in case of Send for Information
            this.pibManager.savePIBMethodologyWaiver(pibMethodologyWaiver);


            if(initiationList!=null && initiationList.size()>0)
            {
                projectInitiation.setDeviationFromSM(new Integer("1"));
                projectInitiation.setModifiedBy(getUser().getID());
                projectInitiation.setModifiedDate(System.currentTimeMillis());
                // this.pibManager.updatePIBDetails(projectInitiation);
                this.pibManager.updatePIBDeviation(projectInitiation);
            }
            else
            {
                ProjectInitiation pi = new ProjectInitiation();
                pi.setProjectID(projectID);
                pi.setEndMarketID(pibMethodologyWaiver.getEndMarketID());
                pi.setDeviationFromSM(new Integer("1"));
                pi.setCreationBy(getUser().getID());
                pi.setCreationDate(System.currentTimeMillis());
                pi.setModifiedBy(getUser().getID());
                pi.setModifiedDate(System.currentTimeMillis());
                pi.setStatus(SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
                this.pibManager.savePIBDetails(pi);
              //Audit Log: Add User Audit details for PIB Save               
            	SynchroLogUtils.PIBMultiSave(projectInitiation_DB, projectInitiation, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, endMarketId);
            }
            // This is done as when the user click on Send for Information link on Methodology Waiver the Methodology Approver should
            // see the Approve and Reject button as enabled.
            //  pibManager.updatePIBStatus(projectID, SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
            
            //Send for information Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.send.inf"), project.getName(), 
							project.getProjectID(), getUser().getID(), endMarketId);

        }
        else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Request more Information"))
        {
            //TODO: change the subject and message
            //EmailNotification#3 Recipients for Request more Information
            String recp = projectOwnerEmail+","+spiContactEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.request.more.information.meth.waiver.htmlBody","pib.request.more.information.meth.waiver.subject");
            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | More information required on Methodology Waiver");
	    	emailNotBean.setEmailSubject("Notification | More information required on Methodology Waiver");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
            pibManager.reqForInfoPIBMethodologyWaiver(pibMethodologyWaiver);
            
          //Request More Information Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
							project.getProjectID(), getUser().getID(), endMarketId);

        }
        else
        {
            pibMethodologyWaiver.setCreationBy(getUser().getID());
            pibMethodologyWaiver.setCreationDate(System.currentTimeMillis());

            pibMethodologyWaiver.setModifiedBy(getUser().getID());
            pibMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
            pibMethodologyWaiver.setIsApproved(null);
            pibMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
            this.pibManager.savePIBMethodologyWaiver(pibMethodologyWaiver);
            // This is done for issue https://www.svn.sourcen.com//issues/17663
            if(initiationList!=null && initiationList.size()>0)
            {
                projectInitiation.setDeviationFromSM(new Integer("1"));
                projectInitiation.setModifiedBy(getUser().getID());
                projectInitiation.setModifiedDate(System.currentTimeMillis());
                // this.pibManager.updatePIBDetails(projectInitiation);
                this.pibManager.updatePIBDeviation(projectInitiation);
            }
            else
            {
                ProjectInitiation pi = new ProjectInitiation();
                pi.setProjectID(projectID);
                pi.setEndMarketID(pibMethodologyWaiver.getEndMarketID());
                pi.setDeviationFromSM(new Integer("1"));
                pi.setCreationBy(getUser().getID());
                pi.setCreationDate(System.currentTimeMillis());
                pi.setModifiedBy(getUser().getID());
                pi.setModifiedDate(System.currentTimeMillis());
                pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
                this.pibManager.savePIBDetails(pi);
              //Audit Log: Add User Audit details for PIB Save               
            	SynchroLogUtils.PIBMultiSave(projectInitiation_DB, pi, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, endMarketId);
            }
            // pibManager.updatePIBStatus(projectID, SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());

            // emailMessage = stageManager.populateNotificationEmail(pibMethApp.getEmail()+","+projectOwnerEmail, "Waiver sent for Approval for Project Id - " + project.getProjectID(), "Waiver sent for Approval. Please approve",null,null);

            //EmailNotification#1 Recipients for Approval (Methodology Waiver)
            String recp = pibMethApp.getEmail()+","+projectOwnerEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.send.for.approval.meth.waiver.htmlBody","pib.send.for.approval.meth.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);

            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
	    	emailNotBean.setEmailDesc("Action Required | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSubject("Action Required | Methodology Waiver has been initiated");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID(), endMarketId);
        }
        redirectURL="/synchro/pib-multi-details!input.jspa?projectID="+projectID;
        
        if(StringUtils.isNotBlank(pageRequest)) {
           return "pending-actions";
        }
        return SUCCESS;
    }
    
    
    /**
     * This method will update the PIB Kantar Waiver related fields
     * @return
     */
    public String updateKantarWaiver(){

        User pibMethApp = null;

        String projectOwnerEmail="";
        String spiContactEmail="";
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getBaseURL(request);
        String stageUrl = baseUrl+"/synchro/pib-multi-details!input.jspa?projectID=" + projectID;
        
        if(fundingInvestments==null)
        {
        	fundingInvestments = this.synchroProjectManager.getProjectInvestments(projectID);
        }
        for(FundingInvestment fi: fundingInvestments)
        {
        	if(fi.getAboveMarket())
        	{
        		 try
        	     {
        			 spiContactEmail = userManager.getUser(fi.getProjectContact()).getEmail();
        	     }
        		 catch(UserNotFoundException ue)
        	     {

        	     }
        	}
        }
        try
        {
            pibMethApp = userManager.getUser(pibKantarMethodologyWaiver.getMethodologyApprover());
            if(project.getProjectOwner()!=null)
            {
                //projectOwnerEmail = userManager.getUser(project.getProjectOwner()).getEmail();
                projectOwnerEmail = SynchroUtils.getUserEmail(project.getProjectOwner());
            }
            /*  if(endMarketDetails.get(0).getSpiContact()!=null)
            {
                //spiContactEmail = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                spiContactEmail = SynchroUtils.getUserEmail(endMarketDetails.get(0).getSpiContact());
            }*/

        }
        catch(UserNotFoundException ue)
        {

        }
        
      //Save Audit logs for change in Methodology Waiver related fields
        if(projectID!=null && endMarketId!=null)
        {
        	final PIBMethodologyWaiver pibKantarMethodologyWaiver_DB = this.pibManager.getPIBKantarMethodologyWaiver(projectID, endMarketId);
            SynchroLogUtils.PIBKantarWaiverSave(pibKantarMethodologyWaiver_DB, pibKantarMethodologyWaiver, project);
        }
        
        
        if(kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Approve"))
        {
        	this.pibManager.approvePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);

            //EmailNotification#4 Recipients for Approval
            String recp = projectOwnerEmail+","+spiContactEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.approve.kantar.waiver.htmlBody","pib.approve.kantar.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
          //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Approved");
	    	emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Approved");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.approve"), project.getName(), 
            												project.getProjectID(), getUser().getID(), endMarketId);
        

        }
        else if (kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Reject"))
        {
        	this.pibManager.rejectPIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);

            //EmailNotification#5 Recipients for Reject
            String recp = projectOwnerEmail+","+spiContactEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.reject.kantar.waiver.htmlBody","pib.reject.kantar.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Rejected");
	    	emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Rejected");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.reject"), project.getName(), 
            												project.getProjectID(), getUser().getID(), endMarketId);
        }
        else if (kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Send for Information"))
        {
            //EmailNotification#2 Recipients for Send for Information
            String recp = pibMethApp.getEmail()+","+projectOwnerEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.send.for.information.kantar.waiver.htmlBody","pib.send.for.information.kantar.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver has been initiated");
	    	emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver has been initiated");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);

	    	pibKantarMethodologyWaiver.setCreationBy(getUser().getID());
	    	pibKantarMethodologyWaiver.setCreationDate(System.currentTimeMillis());

	    	pibKantarMethodologyWaiver.setModifiedBy(getUser().getID());
	    	pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
	    	pibKantarMethodologyWaiver.setIsApproved(null);
	    	pibKantarMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());

            //https://www.svn.sourcen.com/issues/17809 - Comments need to be saved in case of Send for Information
	    	this.pibManager.savePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
	    	//Send for information Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.send.inf"), project.getName(), 
							project.getProjectID(), getUser().getID(), endMarketId);

        /*    if(initiationList!=null && initiationList.size()>0)
            {
                projectInitiation.setDeviationFromSM(new Integer("1"));
                projectInitiation.setModifiedBy(getUser().getID());
                projectInitiation.setModifiedDate(System.currentTimeMillis());
                // this.pibManager.updatePIBDetails(projectInitiation);
                this.pibManager.updatePIBDeviation(projectInitiation);
            }
            else
            {
                ProjectInitiation pi = new ProjectInitiation();
                pi.setProjectID(projectID);
                pi.setEndMarketID(pibMethodologyWaiver.getEndMarketID());
                pi.setDeviationFromSM(new Integer("1"));
                pi.setCreationBy(getUser().getID());
                pi.setCreationDate(System.currentTimeMillis());
                pi.setModifiedBy(getUser().getID());
                pi.setModifiedDate(System.currentTimeMillis());
                pi.setStatus(SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
                this.pibManager.savePIBDetails(pi);
            }*/
            // This is done as when the user click on Send for Information link on Methodology Waiver the Methodology Approver should
            // see the Approve and Reject button as enabled.
            //  pibManager.updatePIBStatus(projectID, SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());

        }
        else if (kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Request more Information"))
        {
            //TODO: change the subject and message
            //EmailNotification#3 Recipients for Request more Information
            String recp = projectOwnerEmail+","+spiContactEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.request.more.information.kantar.waiver.htmlBody","pib.request.more.information.kantar.waiver.subject");
            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("Notification | More information required on Kantar Agency Waiver");
	    	emailNotBean.setEmailSubject("Notification | More information required on Kantar Agency Waiver");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	

	    	pibManager.reqForInfoPIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
	    	
	    	//Request More Information Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.request.inf"), project.getName(), 
							project.getProjectID(), getUser().getID(), endMarketId);

        }
        else
        {
        	pibKantarMethodologyWaiver.setCreationBy(getUser().getID());
        	pibKantarMethodologyWaiver.setCreationDate(System.currentTimeMillis());

        	pibKantarMethodologyWaiver.setModifiedBy(getUser().getID());
        	pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
        	pibKantarMethodologyWaiver.setIsApproved(null);
        	pibKantarMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
        	this.pibManager.savePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
            // This is done for issue https://www.svn.sourcen.com//issues/17663
          /*  if(initiationList!=null && initiationList.size()>0)
            {
                projectInitiation.setDeviationFromSM(new Integer("1"));
                projectInitiation.setModifiedBy(getUser().getID());
                projectInitiation.setModifiedDate(System.currentTimeMillis());
                // this.pibManager.updatePIBDetails(projectInitiation);
                this.pibManager.updatePIBDeviation(projectInitiation);
            }
            else
            {
                ProjectInitiation pi = new ProjectInitiation();
                pi.setProjectID(projectID);
                pi.setEndMarketID(pibMethodologyWaiver.getEndMarketID());
                pi.setDeviationFromSM(new Integer("1"));
                pi.setCreationBy(getUser().getID());
                pi.setCreationDate(System.currentTimeMillis());
                pi.setModifiedBy(getUser().getID());
                pi.setModifiedDate(System.currentTimeMillis());
                pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
                this.pibManager.savePIBDetails(pi);
            }*/
          
            String recp = pibMethApp.getEmail()+","+projectOwnerEmail;
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"pib.send.for.approval.kantar.waiver.htmlBody","pib.send.for.approval.kantar.waiver.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);

            stageManager.sendNotification(getUser(),email);
            
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
	    	emailNotBean.setEmailDesc("Action Required | Kantar Agency Waiver has been initiated");
	    	emailNotBean.setEmailSubject("Action Required | Kantar Agency Waiver has been initiated");
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recp);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID(), endMarketId);
        }
        redirectURL="/synchro/pib-multi-details!input.jspa?projectID="+projectID;
       
        if(StringUtils.isNotBlank(pageRequest)) {
           return "pending-actions";
        }
        
        return SUCCESS;
    }
    /**
     * This method will do the Market Amendment
     * @return
     */
    public String marketAmendment(){

        projectInitiation.setCreationBy(getUser().getID());
        projectInitiation.setCreationDate(System.currentTimeMillis());

        projectInitiation.setModifiedBy(getUser().getID());
        projectInitiation.setModifiedDate(System.currentTimeMillis());
        projectInitiation.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
        projectInitiation.setDeviationFromSM(new Integer("0"));
        projectInitiation.setEndMarketID(endMarketId);
        if(marketAmendmentType!=null && marketAmendmentType.equals("ActionStandard"))
        {

            this.pibManager.updatePIBActionStandard(projectInitiation);
        }
        if(marketAmendmentType!=null && marketAmendmentType.equals("ResearchDesign"))
        {
            this.pibManager.updatePIBResearchDesign(projectInitiation);
        }
        if(marketAmendmentType!=null && marketAmendmentType.equals("SampleProfile"))
        {
            this.pibManager.updatePIBSampleProfile(projectInitiation);
        }
        if(marketAmendmentType!=null && marketAmendmentType.equals("StimulusMaterial"))
        {
            this.pibManager.updatePIBStimulusMaterial(projectInitiation);
        }
        redirectURL="/synchro/pib-multi-details!input.jspa?projectID="+projectID;
        return SUCCESS;
    }

    /**
     * This method will Update the EndMarkets
     * @return
     */
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
            		synchroProjectManager.deleteEndMarketStatus(projectID, emId);
                    synchroProjectManager.deleteEndMarketDetail(projectID,emId);
                    this.pibManager.deletePIBEndMarket(projectID,emId);
                    this.pibManager.deletePIBMWEndMarket(projectID,emId);
                    this.pibManager.deletePIBReportingEndMarket(projectID,emId);
                    this.pibManager.deletePIBStakeholderEndMarket(projectID,emId);
                    
                    synchroProjectManager.deleteInvestment(projectID, emId);
            	}
            }
            for(Long updatedEMID:updatedEndMarkets)
            {
                
            	if(!emIds.contains(updatedEMID))
            	{
            		//Add all the endmarkets which are new in the updated list
            		synchroProjectManager.setEndMarketStatus(projectID, updatedEMID, SynchroGlobal.ProjectActivationStatus.OPEN.ordinal());
	            	EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(projectID, updatedEMID);
	
	                endMarketInvestmentDetail.setCreationBy(getUser().getID());
	                endMarketInvestmentDetail.setModifiedBy(getUser().getID());
	                synchroProjectManager.saveEndMarketDetail(endMarketInvestmentDetail);
	
	                // This is done to save the Business Question and Research Objective fields from the Above Market Section to the new End Markets
	                //https://svn.sourcen.com/issues/19505
	                ProjectInitiation pi = new ProjectInitiation();
	                pi.setCreationBy(getUser().getID());
	                pi.setCreationDate(System.currentTimeMillis());
	                pi.setModifiedBy(getUser().getID());
	                pi.setModifiedDate(System.currentTimeMillis());
	                pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
	                pi.setDeviationFromSM(new Integer("0"));
	                pi.setEndMarketID(updatedEMID);
	                pi.setProjectID(projectID);
	                pi.setResearchObjective(projectInitiation.getResearchObjective());
	                pi.setBizQuestion(projectInitiation.getBizQuestion());
	          //      this.pibManager.updatePIBBusinessQuestion(pi);
	          //      this.pibManager.updatePIBResearchObjective(pi);
	                
	                
	               
    	       // 	ProjectInitiation pi = new ProjectInitiation();
    	       // 	pi.setCreationBy(projectInitiation.getCreationBy());
    	       // 	pi.setCreationDate(projectInitiation.getCreationDate());
    	       //  	pi.setModifiedBy(projectInitiation.getModifiedBy());
    	       // 	pi.setModifiedDate(projectInitiation.getModifiedDate());
    	       // 	pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
    	       // 	pi.setDeviationFromSM(new Integer("0"));
    	       // 	pi.setEndMarketID(emd.getEndMarketID());
    	       // 	pi.setProjectID(projectInitiation.getProjectID());
    	        	pi.setResearchObjective(projectInitiation.getResearchObjective());
    	        	pi.setBizQuestion(projectInitiation.getBizQuestion());
    	        	
    	        	pi.setActionStandard(projectInitiation.getActionStandard());
    	        	pi.setResearchDesign(projectInitiation.getResearchDesign());
    	        	pi.setSampleProfile(projectInitiation.getSampleProfile());
    	        	pi.setStimulusMaterial(projectInitiation.getStimulusMaterial());        	
    	        	
    	        	pi.setNpiReferenceNo(projectInitiation.getNpiReferenceNo());
    	        	pi.setStimuliDate(projectInitiation.getStimuliDate());
    	        	
    	        	if(pibReporting!=null)
    	        	{
    	        		pi.setTopLinePresentation(pibReporting.getTopLinePresentation());
        	        	pi.setPresentation(pibReporting.getPresentation());
        	        	pi.setFullreport(pibReporting.getFullreport());
        	        	pi.setGlobalSummary(pibReporting.getGlobalSummary());
        	        	pi.setOtherReportingRequirements(pibReporting.getOtherReportingRequirements());
        	        	pi.setOtherReportingRequirementsText(pibReporting.getOtherReportingRequirementsText());
    	        	}
    	        	else
    	        	{
	    	        	pi.setTopLinePresentation(projectInitiation.getTopLinePresentation());
	    	        	pi.setPresentation(projectInitiation.getPresentation());
	    	        	pi.setFullreport(projectInitiation.getFullreport());
	    	        	pi.setGlobalSummary(projectInitiation.getGlobalSummary());
	    	        	pi.setOtherReportingRequirements(projectInitiation.getOtherReportingRequirements());
	    	        	pi.setOtherReportingRequirementsText(projectInitiation.getOtherReportingRequirementsText());
    	        	}
    	        	pi.setOthers(projectInitiation.getOthers());
    	        //	pi.setGlobalLegalContact(projectInitiation.getGlobalLegalContact());
    	        	
    	        	   //Text Fields Copy
    	        	pi.setBizQuestionText(projectInitiation.getBizQuestionText());
    	        	pi.setResearchObjectiveText(projectInitiation.getResearchObjectiveText());
    	        	pi.setActionStandardText(projectInitiation.getActionStandardText());
    	        	pi.setResearchDesignText(projectInitiation.getResearchDesignText());
    	        	pi.setSampleProfileText(projectInitiation.getSampleProfileText());
    	        	pi.setStimulusMaterialText(projectInitiation.getStimulusMaterialText());
    	        	pi.setOthersText(projectInitiation.getOthersText());
    	        	
                    		
    	     
    	        	this.pibManager.updateMultiMarketOtherEM(pi);
    	        	//this.pibManager.updatePIBMultiMarketReporting(pi);
    	       
	                
            	}
            }
        }
        /*	this.synchroProjectManager.updateSingleEndMarketId(projectID, updatedSingleMarketId);
       // TODO : Here add the logic to update the End Market Id in PIB corresponding tables
       this.pibManager.dupdatePIBMethWaiverSingleEndMarketId(projectID, updatedSingleMarketId);
       this.pibManager.updatePIBReportingSingleEndMarketId(projectID, updatedSingleMarketId);
       this.pibManager.updatePIBStakeholderListSingleEndMarketId(projectID, updatedSingleMarketId);*/
        redirectURL="/synchro/pib-multi-details!input.jspa?projectID="+projectID;
        
      //Audit logs for Endmarket Changes
        SynchroLogUtils.PIBCountryUpdate(project, emIds, updatedEndMarkets);
        
        return SUCCESS;
    }

    /**
     * This method will update the PIT from the PIB screem
     * @return
     */
    public String updatePIT(){
    	project_DB = synchroProjectManager.get(projectID);
        this.project = synchroProjectManager.get(projectID);

        if(getRequest().getMethod().equalsIgnoreCase("POST")){
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
            	LOG.debug("Error occurred while binding the request object with the Multi Market Project bean in Create Project Action");
                input();
            }
        }
      //https://www.svn.sourcen.com//issues/19285
        String projectName = getRequest().getParameter("projectName");
        if(projectName!=null)
        {
        	this.project.setName(projectName);
        }
        this.project.setModifiedBy(getUser().getID());
        this.project.setModifiedDate(System.currentTimeMillis());

        MultiMarketProject multiMarketProject = new MultiMarketProject();
        ServletRequestDataBinder binder = new ServletRequestDataBinder(multiMarketProject);
        binder.bind(getRequest());
        if(binder.getBindingResult().hasErrors()){
            LOG.debug("Error occurred while binding the request object with the Multi Market Project bean in Create Project Action");
            input();
        }

        List<FundingInvestment> investments = synchroProjectManager.setProjectInvestments(getFundingInvestmentBean(multiMarketProject), projectID);

        synchroProjectManager.save(project);
        //Audit Logs : PIT
        SynchroLogUtils.PITMultiSave(project_DB, project);
        
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.VIEWEDITPIT.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
        
        String emId = getRequest().getParameter("endMarketId");
        if(emId!=null && !emId.equals("") && !emId.equals(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID.toString())) {
            redirectURL = "/synchro/pib-multi-details!input.jspa?projectID="+projectID+"&endMarketId="+endMarketId;
        } else {
            redirectURL = "/synchro/pib-multi-details!input.jspa?projectID="+projectID;
        }
        
        
        return SUCCESS;
    }

    private List<FundingInvestment> getFundingInvestmentBean(final MultiMarketProject multiMarketProject) {
        List<FundingInvestment> investments = new ArrayList<FundingInvestment>();
        int size = multiMarketProject.getProjectContact() != null ? multiMarketProject.getProjectContact().size() : 0;
        
        LOG.info("SIZE OF FUNDING INVESTMENT -- " + size);
        
        if(size > 0) {
            for(int i = 0; i < size; i++) {
                FundingInvestment investment = new FundingInvestment();
                if(multiMarketProject.getInvestmentID() != null && multiMarketProject.getInvestmentID().size() > 0) {
                    investment.setInvestmentID(multiMarketProject.getInvestmentID().get(i).longValue());
                }
                investment.setProjectID(multiMarketProject.getProjectID());
                investment.setAboveMarket(SynchroUtils.isAboveMarket(multiMarketProject.getInvestmentType().get(i).intValue()));
                investment.setInvestmentType(multiMarketProject.getInvestmentType().get(i));
                investment.setInvestmentTypeID(multiMarketProject.getInvestmentTypeID().get(i));
                investment.setFieldworkMarketID(multiMarketProject.getFieldworkMarketID().get(i));
                investment.setFundingMarketID(multiMarketProject.getFundingMarketID().get(i));
                investment.setProjectContact(multiMarketProject.getProjectContact().get(i));
                investment.setSpiContact(multiMarketProject.getSpiContact().get(i));
                investment.setEstimatedCost(SynchroUtils.formatDate(multiMarketProject.getInitialCost().get(i)));
                investment.setEstimatedCostCurrency(multiMarketProject.getInitialCostCurrency().get(i));
                if(multiMarketProject.getApproved().get(i)!=null && multiMarketProject.getApproved().get(i)==1)
                {
                    investment.setApproved(true);
                }
                else
                {
                    investment.setApproved(false);
                }
                
                if(multiMarketProject.getApprovalStatus().get(i)!=null)
                {
                	if(multiMarketProject.getApprovalStatus().get(i)==1)
                	{
                		investment.setApprovalStatus(true);
                	}
                	else if(multiMarketProject.getApprovalStatus().get(i)==0)
                	{
                		investment.setApprovalStatus(false);
                	}
                }
                else
                {
                	investment.setApprovalStatus(null);	
                }
                
                
                //Add to array
                investments.add(investment);
            }
        }

        return investments;
    }
    /**
     * This method will perform the notification activities for the To Do List Actions for each stage.
     *
     */
    public String sendNotification() {
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND_TO_PROJECT_OWNER"))
		{
    		List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
    		EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody, null, null);
    		email = handleAttachments(email);
    		stageManager.sendNotification(getUser(),email);
			
			//Sending Agency User Emails individually 
			for(String agencyEmail : agencyEmails)
			{
				email = stageManager.populateNotificationEmail(agencyEmail, subject, messageBody, null, null);
				email = handleAttachments(email);
				stageManager.sendNotification(getUser(),email);
			}
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("SEND_TO_PROJECT_OWNER");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit Logs: Notify Project Owner
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.pib.notify.spi");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
		}
    	else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND_TO_PROJECT_CONTACT"))
		{
    		List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
    		EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("SEND_TO_PROJECT_CONTACT");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit Logs: Notify SP&I
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.pib.notify.owner");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), 
                    		SynchroGlobal.Activity.NOTIFICATION.getId(), SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        							 project.getProjectID(), getUser().getID(), userNameList);
	    	
		}
    	else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NOTIFY_END_MARKET_CONTACTS"))
		{
    		List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
    		EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Sending Agency User Emails individually 
			for(String agencyEmail : agencyEmails)
			{
				email = stageManager.populateNotificationEmail(agencyEmail, subject, messageBody, null, null);
				email = handleAttachments(email);
				stageManager.sendNotification(getUser(),email);
			}
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("NOTIFY_END_MARKET_CONTACTS");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit Logs: Notify END MARKET CONTACTS
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.pib.notify.em.contact");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), 
                    		SynchroGlobal.Activity.NOTIFICATION.getId(), SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        							 project.getProjectID(), getUser().getID(), userNameList);
	    	
		}
    	else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NOTIFY_AGENCY"))
        {
    		List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
            EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody,null,null);
            email = handleAttachments(email);
            stageManager.sendNotification(getUser(),email);
            
          //Sending Agency User Emails individually 
			for(String agencyEmail : agencyEmails)
			{
				email = stageManager.populateNotificationEmail(agencyEmail, subject, messageBody, null, null);
				email = handleAttachments(email);
				stageManager.sendNotification(getUser(),email);
			}
			
          //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("NOTIFY_AGENCY");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit Logs: Notify END MARKET CONTACTS
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.pib.notify.agency");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), 
                    		SynchroGlobal.Activity.NOTIFICATION.getId(), SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        							 project.getProjectID(), getUser().getID(), userNameList);
	    	
        }
        else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NOTIFY_ABOVE_MARKET_CONTACTS"))
        {
        	List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
            EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody,null,null);
            email = handleAttachments(email);
            stageManager.sendNotification(getUser(),email);
            this.pibManager.updatePIBNotifyAboveMarketContact(projectID, endMarketId, 1);
            
          //Sending Agency User Emails individually 
			for(String agencyEmail : agencyEmails)
			{
				email = stageManager.populateNotificationEmail(agencyEmail, subject, messageBody, null, null);
				email = handleAttachments(email);
				stageManager.sendNotification(getUser(),email);
			}
			
          //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("NOTIFY_ABOVE_MARKET_CONTACTS");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Audit Logs: Notify END MARKET CONTACTS
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.pib.notify.abovemarket.contacts");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), 
                    		SynchroGlobal.Activity.NOTIFICATION.getId(), SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        							 project.getProjectID(), getUser().getID(), userNameList);
        }
        else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("APPROVE_CHANGES"))
        {
        	List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
            EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody,null,null);
            email = handleAttachments(email);
            stageManager.sendNotification(getUser(),email);
            this.pibManager.updatePIBApproveChanges(projectID, endMarketId, 1);
       
          //Sending Agency User Emails individually 
			for(String agencyEmail : agencyEmails)
			{
				email = stageManager.populateNotificationEmail(agencyEmail, subject, messageBody, null, null);
				email = handleAttachments(email);
				stageManager.sendNotification(getUser(),email);
			}
			
            //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("APPROVE_CHANGES");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//TODO Kanwar
	          //Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.pib.approve.text"), project.getName(), 
            												project.getProjectID(), getUser().getID(), endMarketId);
        }
        else
        {
        	try
	        {
	                          
                //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-multi-details!input.jspa?projectID=" + projectID;
        		String baseUrl = URLUtils.getBaseURL(request);
        		String stageUrl = baseUrl+"/synchro/pib-multi-details!input.jspa?projectID=" + projectID;
    	        if(recipients!=null && !recipients.equals(""))
    	        {
    	            // https://www.svn.sourcen.com/issues/18161
    	            if(recipients.contains(","))
    	            {
    	                String[] splitUser = recipients.split(",");
    	                for(int i=0;i<splitUser.length;i++)
    	                {
    	                    EmailMessage email = stageManager.populateNotificationEmail(splitUser[i], subject, messageBody,null,null);
    	                    //email.getContext().put("projectId", projectID);
    	                    email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
    	                    email.getContext().put ("projectName",project.getName());
    	                    email.getContext().put ("stageUrl",stageUrl);
    	                    email = handleAttachments(email);
    	                    stageManager.sendNotification(getUser(),email);
    	                    
    	                    //Email Notification TimeStamp Storage
    	        	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
    	        	    	emailNotBean.setProjectID(projectID);
    	        	    	emailNotBean.setEndmarketID(endMarketId);
    	        	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
    	        	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
    	        	    	emailNotBean.setEmailDesc("PIB Complete - Notify Agency");
    	        	    	emailNotBean.setEmailSubject(subject);
    	        	    	emailNotBean.setEmailSender(getUser().getEmail());
    	        	    	emailNotBean.setEmailRecipients(splitUser[i]);
    	        	    	emailNotificationManager.saveDetails(emailNotBean);
    	        	    	
    	                }
    	            }
    	            else
    	            {
    	                EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
    	                //email.getContext().put("projectId", projectID);
    	                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
    	                email.getContext().put ("projectName",project.getName());
    	
    	
    	                email.getContext().put ("stageUrl",stageUrl);
    	                email = handleAttachments(email);
    	                stageManager.sendNotification(getUser(),email);
    	                
    	              //Email Notification TimeStamp Storage
            	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            	    	emailNotBean.setProjectID(projectID);
            	    	emailNotBean.setEndmarketID(endMarketId);
            	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
            	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            	    	emailNotBean.setEmailDesc("PIB Complete - Notify Agency");
            	    	emailNotBean.setEmailSubject(subject);
            	    	emailNotBean.setEmailSender(getUser().getEmail());
            	    	emailNotBean.setEmailRecipients(recipients);
            	    	emailNotificationManager.saveDetails(emailNotBean);
    	            }
    	            StringBuilder userEmailList = new StringBuilder();
    	            //Audit Logs: PIB Complete Notify Agency + Project Owner
    	            if(!SynchroUtils.isReferenceID(project.getProjectOwner()))
    	            {
    	            	userEmailList.append(userManager.getUser(project.getProjectOwner()).getEmail());
    	            	userEmailList.append(",");
    	            }
    	            if(StringUtils.isNotBlank(recipients)){
    	            	userEmailList.append(recipients);	
    	            }
    	            List<String> userNameList = SynchroUtils.fetchUserNames(userEmailList!=null?userEmailList.toString():StringUtils.EMPTY);
    	            String description = getText("logger.pib.notify.complete");
    	                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), 
    	                    		SynchroGlobal.Activity.NOTIFICATION.getId(), SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
    	        							 project.getProjectID(), getUser().getID(), userNameList);
    	        }
                
        		// Automatic Notification for Project Owner
	            if(!SynchroUtils.isReferenceID(project.getProjectOwner()))
	            {
	                EmailMessage email = stageManager.populateNotificationEmail(userManager.getUser(project.getProjectOwner()).getEmail(), null, null,"pib.complete.notifyProjectOwner.htmlBody","pib.complete.notifyProjectOwner.subject");
	                //email.getContext().put("projectId", projectID);
	                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
	                email.getContext().put("projectName",project.getName());
	                email.getContext().put ("stageUrl",stageUrl);
	                email = handleAttachments(email);
	                stageManager.sendNotification(getUser(),email);
	                
	                //Email Notification TimeStamp Storage
        	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
        	    	emailNotBean.setProjectID(projectID);
        	    	emailNotBean.setEndmarketID(endMarketId);
        	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
        	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
        	    	emailNotBean.setEmailDesc("PIB Complete - Notify Project Owner");
        	    	emailNotBean.setEmailSubject(subject);
        	    	emailNotBean.setEmailSender(getUser().getEmail());
        	    	emailNotBean.setEmailRecipients(userManager.getUser(project.getProjectOwner()).getEmail());
        	    	emailNotificationManager.saveDetails(emailNotBean);
        	    	
	            }
	            // Automatic Notification for Legal Users
	            if(pibStakeholderList.getGlobalLegalContact()!=null && pibStakeholderList.getGlobalLegalContact() > 0 )
	            {
	                EmailMessage email = stageManager.populateNotificationEmail(userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getEmail(), null, null,"pib.complete.LegalUser.htmlBody","pib.complete.LegalUser.subject");
	                //email.getContext().put("projectId", projectID);
	                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
	                email.getContext().put("projectName",project.getName());
	                email.getContext().put ("stageUrl",stageUrl);
	                email = handleAttachments(email);
	                stageManager.sendNotification(getUser(),email);
	                
	                //Email Notification TimeStamp Storage
        	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
        	    	emailNotBean.setProjectID(projectID);
        	    	emailNotBean.setEndmarketID(endMarketId);
        	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
        	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
        	    	emailNotBean.setEmailDesc("PIB Complete - Legal user");
        	    	emailNotBean.setEmailSubject(subject);
        	    	emailNotBean.setEmailSender(getUser().getEmail());
        	    	emailNotBean.setEmailRecipients(userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getEmail());
        	    	emailNotificationManager.saveDetails(emailNotBean);
        	    	
	            }
	            // Automatic Notification for Procurement and Communication Agency Users
	            if((pibStakeholderList.getGlobalProcurementContact()!=null && pibStakeholderList.getGlobalProcurementContact() > 0) || (pibStakeholderList.getGlobalCommunicationAgency()!=null && pibStakeholderList.getGlobalCommunicationAgency() > 0))
	            {
	                StringBuffer proCAUsers = new StringBuffer();
	                if(pibStakeholderList.getGlobalProcurementContact()!=null && pibStakeholderList.getGlobalProcurementContact() > 0)
	                {
	                    proCAUsers.append(userManager.getUser(pibStakeholderList.getGlobalProcurementContact()).getEmail());
	                }
	                if(pibStakeholderList.getGlobalCommunicationAgency()!=null && pibStakeholderList.getGlobalCommunicationAgency() > 0)
	                {
	                    if(proCAUsers.length()>0)
	                    {
	                        proCAUsers.append(","+userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getEmail());
	                    }
	                    else
	                    {
	                        proCAUsers.append(userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getEmail());
	                    }
	                }
	                EmailMessage email = stageManager.populateNotificationEmail(proCAUsers.toString(), null, null,"pib.complete.ProcurementCAUsers.htmlBody","pib.complete.ProcurementCAUsers.subject");
	                //email.getContext().put("projectId", projectID);
	                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
	                email.getContext().put("projectName",project.getName());
	                email.getContext().put ("stageUrl",stageUrl);
	                email = handleAttachments(email);
	                stageManager.sendNotification(getUser(),email);
	                
	                //Email Notification TimeStamp Storage
        	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
        	    	emailNotBean.setProjectID(projectID);
        	    	emailNotBean.setEndmarketID(endMarketId);
        	    	emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
        	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
        	    	emailNotBean.setEmailDesc("PIB Complete - Notify Procurement and CA Users");
        	    	
        	    	emailNotBean.setEmailSubject(subject);
        	    	emailNotBean.setEmailSender(getUser().getEmail());
        	    	emailNotBean.setEmailRecipients(proCAUsers.toString());
        	    	
        	    	emailNotificationManager.saveDetails(emailNotBean);
	            }
	
	
	        }
	        catch(UserNotFoundException ue)
	        {
	            LOG.error("Project Owner not found while sending Notification for PIB Complete"+project.getProjectOwner());
	        }
            stageManager.updateStageStatus(projectID, endMarketId, 1, SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal(),getUser(), null);
            // Update the status for all the End Market Ids
            for(EndMarketInvestmentDetail emd: endMarketDetails)
            {
                stageManager.updateStageStatus(projectID, emd.getEndMarketID(), 1, SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal(),getUser(), null);
            }
            if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()!=SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                //stageManager.updateStageStatus(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                stageManager.updateMultiMarketStageStatus(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
            }
            
            // This is done in case the End Market is activated before the proposal is awarded , then we have to move that end market details to 
            // Proposal tab as well.
            if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                stageManager.updateActivatedProposalEM(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
            }

            // Update the project status to IN PROGRESS once the PIB is completed.
            //	synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_OPEN.ordinal());
            synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal());

        }
        return SUCCESS;
    }

    /**
     * This method will move the stage from PIB to Proposal
     * @return
     */
    public String moveToNextStage() 
    {
    	LOG.info("Inside moveToNextStage PIBMulti Market Action--- "+ projectID); 

        stageManager.updateStageStatus(projectID, endMarketId, 1, SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal(),getUser(), null);
        // Update the status for all the End Market Ids
        for(EndMarketInvestmentDetail emd: endMarketDetails)
        {
            stageManager.updateStageStatus(projectID, emd.getEndMarketID(), 1, SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal(),getUser(), null);
        }
        if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()!=SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
        {
            //stageManager.updateStageStatus(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
            stageManager.updateMultiMarketStageStatus(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
        }
        
        // This is done in case the End Market is activated before the proposal is awarded , then we have to move that end market details to 
        // Proposal tab as well.
        if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
        {
            stageManager.updateActivatedProposalEM(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
        }

        // Update the project status to IN PROGRESS once the PIB is completed.
        //	synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_OPEN.ordinal());
        synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal());

    
    	//return SUCCESS;
        //  Clicking on ‘Move to the next stage’ will navigate user to the next stage 
        return "moveToNextStage";
    }
    public String addAttachment() throws UnsupportedEncodingException {

        LOG.info("Checking File Name"+attachFileFileName);
        LOG.info("Checking File Content Type"+attachFileContentType);
        Map<String, Object> result = new HashMap<String, Object>();
        try
        {
            pibManager.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID());
            
            //Add Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
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
            pibManager.removeAttachment(attachmentId);
          //Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName + " deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID(), endMarketId);
        }
        catch (Exception e) {
            LOG.error("Exception while removing attachment Id --"+ attachmentId);
        }
        return SUCCESS;

    }

    private boolean isMandatoryFieldsFilled(ProjectInitiation projectInitiation, Boolean isAboveMarket) {
        boolean filled = false;
        if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal() && isAboveMarket)
        {
            if(projectInitiation.getLatestEstimate()!=null
                    && (projectInitiation.getBizQuestion()!=null && !projectInitiation.getBizQuestion().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
                    && (projectInitiation.getResearchObjective()!=null && !projectInitiation.getResearchObjective().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
                    && (projectInitiation.getActionStandard()!=null && !projectInitiation.getActionStandard().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
                    && (projectInitiation.getResearchDesign()!=null && !projectInitiation.getResearchDesign().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
                    && (projectInitiation.getSampleProfile()!=null && !projectInitiation.getSampleProfile().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
                    && (projectInitiation.getStimulusMaterial()!=null && !projectInitiation.getStimulusMaterial().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
                    //&& projectInitiation.getOthers()!=null && !projectInitiation.getOthers().equals("")

                    && (projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation()|| projectInitiation.getPresentation()!=null && projectInitiation.getPresentation()
                    || projectInitiation.getFullreport()!=null && projectInitiation.getFullreport())
                    //&& projectInitiation.getOtherReportingRequirements()!=null && !projectInitiation.getOtherReportingRequirements().equals("")
                    && projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)
            //&& (projectInitiation.getAgencyContact1()!=null && projectInitiation.getAgencyContact1()>0 || projectInitiation.getAgencyContact2()!=null && projectInitiation.getAgencyContact2()>0  || projectInitiation.getAgencyContact3()!=null && projectInitiation.getAgencyContact3()>0 ))


            {
                filled = true;
            }
        }
        else if(isAboveMarket)
        {
            if(projectInitiation.getLatestEstimate()!=null
                    && (projectInitiation.getBizQuestion()!=null && !projectInitiation.getBizQuestion().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
                    && (projectInitiation.getResearchObjective()!=null && !projectInitiation.getResearchObjective().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
                    && (projectInitiation.getActionStandard()!=null && !projectInitiation.getActionStandard().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
                    && (projectInitiation.getResearchDesign()!=null && !projectInitiation.getResearchDesign().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
                    && (projectInitiation.getSampleProfile()!=null && !projectInitiation.getSampleProfile().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
                    && (projectInitiation.getStimulusMaterial()!=null && !projectInitiation.getStimulusMaterial().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
                    //&& projectInitiation.getOthers()!=null && !projectInitiation.getOthers().equals("")

                    && (projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation()|| projectInitiation.getPresentation()!=null && projectInitiation.getPresentation()
                    || projectInitiation.getFullreport()!=null && projectInitiation.getFullreport())
                    //&& projectInitiation.getOtherReportingRequirements()!=null && !projectInitiation.getOtherReportingRequirements().equals("")
                    && projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0
                    && projectInitiation.getNonKantar()!=null && projectInitiation.getNonKantar()>0
                    && (projectInitiation.getAgencyContact1()!=null && projectInitiation.getAgencyContact1()>0 || projectInitiation.getAgencyContact2()!=null && projectInitiation.getAgencyContact2()>0  || projectInitiation.getAgencyContact3()!=null && projectInitiation.getAgencyContact3()>0 ))


            {
                filled = true;
            }
        }
        else
        {
            if( (projectInitiation.getActionStandard()!=null && !projectInitiation.getActionStandard().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
                    && (projectInitiation.getResearchDesign()!=null && !projectInitiation.getResearchDesign().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
                    && (projectInitiation.getSampleProfile()!=null && !projectInitiation.getSampleProfile().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
                    && (projectInitiation.getStimulusMaterial()!=null && !projectInitiation.getStimulusMaterial().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
                    //&& projectInitiation.getOthers()!=null && !projectInitiation.getOthers().equals("")

                    && (projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation()|| projectInitiation.getPresentation()!=null && projectInitiation.getPresentation()
                    || projectInitiation.getFullreport()!=null && projectInitiation.getFullreport())
                    //&& projectInitiation.getOtherReportingRequirements()!=null && !projectInitiation.getOtherReportingRequirements().equals("")
                    && projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)
            // && (projectInitiation.getAgencyContact1()!=null && projectInitiation.getAgencyContact1()>0 || projectInitiation.getAgencyContact2()!=null && projectInitiation.getAgencyContact2()>0  || projectInitiation.getAgencyContact3()!=null && projectInitiation.getAgencyContact3()>0 ))


            {
                filled = true;
            }
        }
        return filled;
    }


    public String exportToWordPIBOLD()
    {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph tmpParagraph = document.createParagraph();
        XWPFRun tmpRun = tmpParagraph.createRun();
        
        tmpRun.setText("Project Description : "+ project.getDescriptionText());
        tmpRun.addBreak();
      /*  tmpRun.setText("Business Question : "+ projectInitiation.getBizQuestionText());
        tmpRun.addBreak();
        tmpRun.setText("Research Objective(s) : "+ projectInitiation.getResearchObjectiveText());
        tmpRun.addBreak();
        tmpRun.setText("Action Standard(s) : "+ projectInitiation.getActionStandardText());
        tmpRun.addBreak();
        tmpRun.setText("Methodology Approach and Research Design : "+ projectInitiation.getResearchDesignText());
        tmpRun.addBreak();
        tmpRun.setText("Sample Profile(Research) : "+ projectInitiation.getSampleProfileText());
        tmpRun.addBreak();
        tmpRun.setText("Stimulus Material : "+ projectInitiation.getStimulusMaterialText());
        tmpRun.addBreak();
        tmpRun.setText("Other Comments : "+ projectInitiation.getOthersText());
        tmpRun.addBreak();
        tmpRun.setText("Other Reporting Requirements : "+ pibReporting.getOtherReportingRequirementsText());
        tmpRun.addBreak();

        tmpRun.setFontSize(18);*/
        XWPFParagraph tmpParagraph1 = document.createParagraph();
        XWPFRun tmpRun1 = tmpParagraph1.createRun();
        tmpRun1.setText("Business Question : "+ projectInitiation.getBizQuestionText());
        tmpRun1.addBreak();
        
        XWPFParagraph tmpParagraph2 = document.createParagraph();
        XWPFRun tmpRun2 = tmpParagraph2.createRun();
        tmpRun2.setText("Research Objective(s) : "+ projectInitiation.getResearchObjectiveText());
        tmpRun2.addBreak();
        
        XWPFParagraph tmpParagraph3 = document.createParagraph();
        XWPFRun tmpRun3 = tmpParagraph3.createRun();
        tmpRun3.setText("Action Standard(s) : "+ projectInitiation.getActionStandardText());
        tmpRun3.addBreak();
        
        XWPFParagraph tmpParagraph4 = document.createParagraph();
        XWPFRun tmpRun4 = tmpParagraph4.createRun();
        tmpRun4.setText("Methodology Approach and Research Design : "+ projectInitiation.getResearchDesignText());
        tmpRun4.addBreak();
       
        XWPFParagraph tmpParagraph5 = document.createParagraph();
        XWPFRun tmpRun5 = tmpParagraph5.createRun();
        tmpRun5.setText("Sample Profile(Research) : "+ projectInitiation.getSampleProfileText());
        tmpRun5.addBreak();
       
        XWPFParagraph tmpParagraph6 = document.createParagraph();
        XWPFRun tmpRun6 = tmpParagraph6.createRun();
        tmpRun6.setText("Stimulus Material : "+ projectInitiation.getStimulusMaterialText());
        tmpRun6.addBreak();
        
        XWPFParagraph tmpParagraph7 = document.createParagraph();
        XWPFRun tmpRun7 = tmpParagraph7.createRun();
        tmpRun7.setText("Other Comments : "+ projectInitiation.getOthersText());
        tmpRun7.addBreak();
       
        XWPFParagraph tmpParagraph8 = document.createParagraph();
        XWPFRun tmpRun8 = tmpParagraph8.createRun();
        tmpRun8.setText("Other Reporting Requirements : "+ pibReporting.getOtherReportingRequirementsText());
        tmpRun8.addBreak();

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
    public String exportToWordPIB()
    {
    	FileOutputStream fos = null;
	   //  String html = "<html><head><META http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><style type=\"text/css\">body.b1{white-space-collapsing:preserve;} body.b2{margin: 1.0in 1.25in 0.59097224in 1.0833334in;} span.s1{font-weight:bold;color:#333333;} span.s2{font-weight:bold;color:red;} span.s3{color:#333333;} span.s4{font-weight:bold;color:#333333;text-decoration:underline;} span.s5{font-size:36pt;font-weight:bold;color:#4bacc6;text-decoration:underline;} span.s6{color:red;} span.s7{font-style:italic;color:#333333;} span.s8{color:#333333;background-color:yellow;} p.p1{end-indent:0pt;text-align:center;hyphenate:none;font-family:Calibri;font-size:14pt;} p.p2{end-indent:0pt;text-align:center;hyphenate:none;font-family:Calibri;font-size:12pt;} p.p3{end-indent:0pt;text-align:start;hyphenate:none;font-family:Calibri;font-size:11pt;} p.p4{text-align:justify;hyphenate:none;font-family:Calibri;font-size:10pt;} p.p5{end-indent:0pt;text-align:justify;border-bottom:thin solid blue;border-left:thin solid blue;border-right:thin solid blue;border-top:thin solid blue;hyphenate:none;font-family:Calibri;font-size:11pt;} p.p6{end-indent:0pt;text-align:justify;hyphenate:none;font-family:Calibri;font-size:11pt;} p.p7{text-align:justify;hyphenate:none;font-family:Calibri;font-size:11pt;} p.p8{space-after:10pt;text-align:justify;hyphenate:none;font-family:Calibri;font-size:11pt;} p.p9{end-indent:0pt;space-after:10pt;text-align:justify;hyphenate:none;font-family:Calibri;font-size:11pt;} p.p10{end-indent:0pt;text-align:justify;hyphenate:none;font-family:Calibri;font-size:10pt;} p.p11{end-indent:0pt;text-align:justify;hyphenate:none;font-family:Myriad Pro;font-size:10pt;} p.p12{text-align:justify;hyphenate:none;font-family:Myriad Pro;font-size:10pt;} td.td1{width:1.3354167in;padding-start:0.0in;padding-end:0.0in;border-bottom:thin solid white;border-left:thin solid white;border-top:thin solid white;} td.td2{width:1.3423611in;padding-start:0.0in;padding-end:0.0in;border-bottom:thin solid white;border-left:thin solid white;border-right:thin solid white;border-top:thin solid white;} tr.r1{keep-together:always;} table.t1{table-layout:fixed;border-collapse:collapse;border-spacing:0;} </style><meta content=\"Kanwardeep Grewal\" name=\"author\" /></head><body class=\"b1 b2\"><p class=\"p1\"><span class=\"s1\">Project Initiating Brief (PIB) </span><span class=\"s1\">Field Population Guidance</span></p><p class=\"p2\"></p><table class=\"t1\"><tbody><tr class=\"r1\"><td class=\"td1\"><p class=\"p3\"></p></td><td class=\"td1\"><p class=\"p3\"></p></td><td class=\"td2\"><p class=\"p3\"></p></td></tr></tbody></table><p class=\"p4\"></p><p class=\"p5\"><span class=\"s2\">Document Purpose</span></p><p class=\"p6\"></p><p class=\"p4\"><span class=\"s3\">DOC Many of the sections of the Project Initiating Brief in the Synchro workflow management tool are either populated through the creation of the project or through defined options (via drop down menus, tick boxes, etc).  The purpose of this guide document is to provide guidance on populating the free text fields of the PIB with meaningful and useful content.</span></p><p class=\"p4\"></p><p class=\"p4\"></p><p class=\"p5\"><span class=\"s2\">Project Description:</span><p class=\"p6\"></p><p class=\"p4\"><span class=\"s3\"><b>TEJINDER</b></span></p></p></body></html>";
    	String html = "<html><head><META http-equiv='Content-Type' content='text/html; charset=utf-8'><style type='text/css'>body.b1{white-space-collapsing:preserve;}" +
     			"body.b2{margin: 1.0in 1.25in 0.59097224in 1.0833334in;}" +
     			"span.s1{font-weight:bold;color:#333333;}" +
     			"span.s2{font-weight:bold;color:red;}" +
     			"span.s3{color:#333333;}" +
     			"p.p1{end-indent:0pt;text-align:center;hyphenate:none;font-family:Calibri;font-size:14pt;}" +
     			"p.p2{end-indent:0pt;text-align:center;hyphenate:none;font-family:Calibri;font-size:12pt;}" +
     			"p.p3{end-indent:0pt;text-align:start;hyphenate:none;font-family:Calibri;font-size:11pt;}" +
     			"p.p4{text-align:justify;hyphenate:none;font-family:Calibri;font-size:10pt;}" +
     			"p.p5{end-indent:0pt;text-align:justify;border-bottom:thin solid blue;border-left:thin solid blue;border-right:thin solid blue;border-top:thin solid blue;hyphenate:none;font-family:Calibri;font-size:11pt;}" +
     			"p.p6{end-indent:0pt;text-align:justify;hyphenate:none;font-family:Calibri;font-size:11pt;}" +
     			"p.p7{end-indent:0pt;text-align:justify;hyphenate:none;font-family:Myriad Pro;font-size:10pt;}" +
     			"p.p8{text-align:justify;hyphenate:none;font-family:Myriad Pro;font-size:10pt;}" +
     			"td.td1{width:1.3354167in;padding-start:0.0in;padding-end:0.0in;border-bottom:thin solid white;border-left:thin solid white;border-top:thin solid white;}" +
     			"td.td2{width:1.35625in;padding-start:0.0in;padding-end:0.0in;border-bottom:thin solid white;border-left:thin solid white;border-right:thin solid white;border-top:thin solid white;}" +
     			"tr.r1{keep-together:always;}" +
     			"table.t1{table-layout:fixed;border-collapse:collapse;border-spacing:0;}" +
     			"</style><meta content='Kanwardeep Grewal' name='author'></head><body class='b1 b2'><p class='p1'><span class='s1'>Project Initiating Brief (PIB) </span><br><span class='s1'>Field Population Guidance</span></p><p class='p2'></p><table class='t1'><tbody><tr class='r1'><td class='td1'><p class='p3'></p></td><td class='td1'><p class='p3'></p></td><td class='td2'><p class='p3'></p></td></tr></tbody></table><p class='p4'></p><p class='p5'><span class='s2'>Document Purpose</span></p><p class='p6'></p><p class='p4'><span class='s3'>DOC Many of the sections of the Project Initiating Brief in the Synchro workflow management tool are either populated through the creation of the project or through defined options (via drop down menus, tick boxes, etc).  The purpose of this guide document is to provide guidance on populating the free text fields of the PIB with meaningful and useful content.</span></p><p class='p4'></p><p class='p4'></p><p class='p5'><span class='s2'>Project Description:</span></p><p class='p6'><span class='s3'>${projectdescription}</span></p><p class='p5'><span class='s2'>Business Question:</span></p><p class='p6'><span class='s3'>${businessquestion}</span></p><p class='p5'><span class='s2'>Research Objectives(s):</span></p><p class='p6'><span class='s3'>${researchobj}</span></p><p class='p5'><span class='s2'>Action Standard(s):</span></p><p class='p3'><span class='s3'>${actionstandard}</span></p><p class='p5'><span class='s2'>Methodology Approach and Research Design:</span></p><p class='p3'><span class='s3'>${researchDesign}</span></p><p class='p5'><span class='s2'>Sample Profile (Research):</span></p><p class='p3'><span class='s3'>${sampleProfile}</span></p><p class='p5'><span class='s2'>Stimulus Material:</span></p><p class='p3'><span class='s3'>${stimulusMaterial}</span></p><p class='p5'><span class='s2'>Other Comments:</span></p><p class='p3'><span class='s3'>${otherComments}</span></p><p class='p5'><span class='s2'>Other Reporting Requirements:</span></p><p class='p3'><span class='s3'>${otherReportingRequirements}</span></p><p class='p7'></p><p class='p8'></p></body></html>";
        html = html.replaceAll("\\$\\{projectdescription\\}", project.getDescription());
        html = html.replaceAll("\\$\\{businessquestion\\}", projectInitiation.getBizQuestion());
        html = html.replaceAll("\\$\\{researchobj\\}", projectInitiation.getResearchObjective());
        html = html.replaceAll("\\$\\{actionstandard\\}", projectInitiation.getActionStandard());
        html = html.replaceAll("\\$\\{researchDesign\\}", projectInitiation.getResearchDesign());
        html = html.replaceAll("\\$\\{sampleProfile\\}", projectInitiation.getSampleProfile());
        html = html.replaceAll("\\$\\{stimulusMaterial\\}", projectInitiation.getStimulusMaterial());
        html = html.replaceAll("\\$\\{otherComments\\}", projectInitiation.getOthers());
        html = html.replaceAll("\\$\\{otherReportingRequirements\\}", pibReporting.getOtherReportingRequirements());
    	
        
    	try
        {
            response.setContentType("application/msword");
            response.addHeader("Content-Disposition", "attachment; filename=PIBWord.doc");
            //document.write(response.getOutputStream());
          //  BufferedInputStream input = null;
            BufferedOutputStream output = null;
           // input = new BufferedInputStream(new FileInputStream(generatedFile), 10240);
            output = new BufferedOutputStream(response.getOutputStream());
            
            output.write(html.getBytes("UTF-8"));
            output.close();
           
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    	//PIB Export to Word Audit logs
        String description = SynchroGlobal.LogProjectStage.PIB.getDescription() + "- " + getText("logger.project.export.word"); 
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
								SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
										project.getProjectID(), getUser().getID());
        
        return null;
    }
    public String exportToPDFPIB()
    {
        try
        {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Document document=new Document();
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=PIBPDF.pdf");
            PdfWriter.getInstance(document,response.getOutputStream());
            document.open();
            document.add(new Paragraph("Project Code : "+ project.getProjectID()));
            document.add(new Paragraph("Project Name : "+ project.getName()));
            document.add(new Paragraph("Brand : "+ SynchroGlobal.getBrands().get(project.getBrand().intValue())));
            document.add(new Paragraph("Country : "+ SynchroGlobal.getEndMarkets().get(endMarketDetails.get(0).getEndMarketID().intValue())));
            document.add(new Paragraph("Project Owner : "+ userManager.getUser(project.getProjectOwner()).getName()));
            document.add(new Paragraph("PIT Creator : "+ userManager.getUser(project.getBriefCreator()).getName()));
            document.add(new Paragraph("SPI Contact : "+ userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName()));
            //TODO Proposed Methodology should return List of Values, instead of just first value from the List -- Kanwar
            if(project.getProposedMethodology()!=null && SynchroGlobal.getMethodologies().get(project.getProposedMethodology()!=null?project.getProposedMethodology().get(0).intValue():-1)!=null)
            {
                document.add(new Paragraph("Proposed Methodology : "+ SynchroGlobal.getMethodologies().get(project.getProposedMethodology()!=null?project.getProposedMethodology().get(0).intValue():-1)));
            }
            else
            {
                document.add(new Paragraph("Proposed Methodology : "));
            }
            document.add(new Paragraph("Project Start (Commissioning) : "+ df.format(project.getStartDate())));
            document.add(new Paragraph("Project End (Results) : "+ df.format(project.getEndDate())));
            if(projectInitiation.getDeviationFromSM()!=null && projectInitiation.getDeviationFromSM()==1)
            {
                document.add(new Paragraph("Request for Methodology Waiver : Yes"));
            }
            else
            {
                document.add(new Paragraph("Request for Methodology Waiver : No"));
            }
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
            document.add(new Paragraph("Methodolgy Type : "+ SynchroGlobal.getProjectIsMapping().get(project.getMethodologyType().intValue())));
            document.add(new Paragraph("Methodology Group : "+ SynchroGlobal.getMethodologyGroups(true, project.getMethodologyGroup()).get(project.getMethodologyGroup().intValue())));
            document.add(new Paragraph("Estimated Cost : "+ endMarketDetails.get(0).getInitialCost() + " "+  SynchroGlobal.getCurrencies().get(endMarketDetails.get(0).getInitialCostCurrency().intValue())));
            document.add(new Paragraph("Latest Estimate : "+ projectInitiation.getLatestEstimate() + " "+ SynchroGlobal.getCurrencies().get(projectInitiation.getLatestEstimateType().intValue())));
            document.add(new Paragraph("NPI Number : "+ projectInitiation.getNpiReferenceNo()));
            document.add(new Paragraph("Project Description : "+ project.getDescription()));
            document.add(new Paragraph("Business Questions : "+ projectInitiation.getBizQuestion()));
            document.add(new Paragraph("Research Objective(s) : "+ projectInitiation.getResearchObjective()));
            document.add(new Paragraph("Action Standard(s) : "+ projectInitiation.getActionStandard()));
            document.add(new Paragraph("Methodology Approach and Research Design : "+ projectInitiation.getResearchDesign()));
            document.add(new Paragraph("Sample Profile(Research) : "+ projectInitiation.getSampleProfile()));
            document.add(new Paragraph("Stimulus Material : "+ projectInitiation.getStimulusMaterial()));
            document.add(new Paragraph("Others : "+ projectInitiation.getOthers()));
            document.add(new Paragraph("Date Stimuli Available(in Agency) : "+ projectInitiation.getStimuliDate()));

            StringBuffer repRequirements = new StringBuffer();
            if(pibReporting.getTopLinePresentation())
            {
                repRequirements.append("TopLine Presentation");
            }
            if(pibReporting.getPresentation())
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
            if(pibReporting.getFullreport())
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
            document.add(new Paragraph("Other Reporting Requirements : "+ pibReporting.getOtherReportingRequirements()));
            if(pibStakeholderList.getAgencyContact1()>0)
            {
                document.add(new Paragraph("Agency Contact 1 : "+ userManager.getUser(pibStakeholderList.getAgencyContact1()).getName()));
            }
            else
            {
                document.add(new Paragraph("Agency Contact 1 : "));
            }
            if(pibStakeholderList.getAgencyContact2()>0)
            {
                document.add(new Paragraph("Agency Contact 2 : "+ userManager.getUser(pibStakeholderList.getAgencyContact2()).getName()));
            }
            else
            {
                document.add(new Paragraph("Agency Contact 2 : "));
            }
            if(pibStakeholderList.getAgencyContact3()>0)
            {
                document.add(new Paragraph("Agency Contact 3 : "+ userManager.getUser(pibStakeholderList.getAgencyContact3()).getName()));
            }
            else
            {
                document.add(new Paragraph("Agency Contact 3 : "));
            }
            if(pibStakeholderList.getGlobalLegalContact()>0)
            {
                document.add(new Paragraph("Legal Contact : "+ userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getName()));
            }
            else
            {
                document.add(new Paragraph("Legal Contact : "));
            }
            if(pibStakeholderList.getGlobalProcurementContact()>0)
            {
                document.add(new Paragraph("Procurement Contact : "+ userManager.getUser(pibStakeholderList.getGlobalProcurementContact()).getName()));
            }
            else
            {
                document.add(new Paragraph("Procurement Contact : "));
            }
            if(pibStakeholderList.getGlobalCommunicationAgency()>0)
            {
                document.add(new Paragraph("Communication Agency : "+ userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getName()));
            }
            else
            {
                document.add(new Paragraph("Communication Agency : "));
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
    public String exportToPDFPIT()
    {
        try
        {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Document document=new Document();
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=PITPDF.pdf");
            PdfWriter.getInstance(document,response.getOutputStream());
            document.open();

            document.add(new Paragraph("Project Name : "+ project.getName()));
            document.add(new Paragraph("Project Description : "+ project.getDescription()));
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
            document.add(new Paragraph("Brand/Non-Branded : "+ SynchroGlobal.getBrands().get(project.getBrand().intValue())));
            document.add(new Paragraph("Methodolgy Type : "+ SynchroGlobal.getProjectIsMapping().get(project.getMethodologyType().intValue())));
            document.add(new Paragraph("Methodology Group : "+ SynchroGlobal.getMethodologyGroups(true, project.getMethodologyGroup()).get(project.getMethodologyGroup().intValue())));
            //TODO Proposed Methodology should return List of Values, instead of just first value from the List -- Kanwar
            if(project.getProposedMethodology()!=null && SynchroGlobal.getMethodologies().get(project.getProposedMethodology()!=null?project.getProposedMethodology().get(0).intValue():-1)!=null)
            {
                document.add(new Paragraph("Proposed Methodology : "+ SynchroGlobal.getMethodologies().get(project.getProposedMethodology()!=null?project.getProposedMethodology().get(0).intValue():-1)));
            }
            else
            {
                document.add(new Paragraph("Proposed Methodology : "));
            }
            document.add(new Paragraph("End Market : "+ SynchroGlobal.getEndMarkets().get(endMarketDetails.get(0).getEndMarketID().intValue())));


            document.add(new Paragraph("Project Start (Commissioning) : "+ df.format(project.getStartDate())));
            document.add(new Paragraph("Project End (Results) : "+ df.format(project.getEndDate())));

            document.add(new Paragraph("Estimated Cost : "+ endMarketDetails.get(0).getInitialCost() + " "+  SynchroGlobal.getCurrencies().get(endMarketDetails.get(0).getInitialCostCurrency().intValue())));
            document.add(new Paragraph("Project Owner : "+ userManager.getUser(project.getProjectOwner()).getName()));

            document.add(new Paragraph("SPI Contact : "+ userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName()));


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

    public void setPibManager(final PIBManager pibManager) {
        this.pibManager = pibManager;
    }

    public ProjectInitiation getProjectInitiation() {
        return projectInitiation;
    }

    public void setProjectInitiation(final ProjectInitiation projectInitiation) {
        this.projectInitiation = projectInitiation;
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



    public PIBReporting getPibReporting() {
        return pibReporting;
    }

    public void setPibReporting(final PIBReporting pibReporting) {
        this.pibReporting = pibReporting;
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

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(int attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public long[] getRemoveAttachID() {
        return removeAttachID;
    }

    public void setRemoveAttachID(long[] removeAttachID) {
        this.removeAttachID = removeAttachID;
    }

    public long[] getImageFile() {
        return imageFile;
    }

    public void setImageFile(long[] imageFile) {
        this.imageFile = imageFile;
    }

    public AttachmentHelper getAttachmentHelper() {
        return attachmentHelper;
    }

    public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
        this.attachmentHelper = attachmentHelper;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenCookie() {
        return tokenCookie;
    }

    public void setTokenCookie(String tokenCookie) {
        this.tokenCookie = tokenCookie;
    }



    public PIBStakeholderList getPibStakeholderList() {
        return pibStakeholderList;
    }
    public void setPibStakeholderList(PIBStakeholderList pibStakeholderList) {
        this.pibStakeholderList = pibStakeholderList;
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

    public Map<Integer, List<AttachmentBean>> getAttachmentMap() {
        return attachmentMap;
    }

    public void setAttachmentMap(Map<Integer, List<AttachmentBean>> attachmentMap) {
        this.attachmentMap = attachmentMap;
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

    public PIBMethodologyWaiver getPibMethodologyWaiver() {
        return pibMethodologyWaiver;
    }

    public void setPibMethodologyWaiver(PIBMethodologyWaiver pibMethodologyWaiver) {
        this.pibMethodologyWaiver = pibMethodologyWaiver;
    }



    public List<EndMarketInvestmentDetail> getEndMarketDetails() {
        return endMarketDetails;
    }

    public void setEndMarketDetails(List<EndMarketInvestmentDetail> endMarketDetails) {
        this.endMarketDetails = endMarketDetails;
    }

    public List<Long> getEmIds() {
        return emIds;
    }

    public void setEmIds(List<Long> emIds) {
        this.emIds = emIds;
    }

    public Boolean getIsAboveMarket() {
        return isAboveMarket;
    }

    public void setIsAboveMarket(Boolean isAboveMarket) {
        this.isAboveMarket = isAboveMarket;
    }

    public List<FundingInvestment> getFundingInvestments() {
        return fundingInvestments;
    }

    public void setFundingInvestments(List<FundingInvestment> fundingInvestments) {
        this.fundingInvestments = fundingInvestments;
    }

    public PIBStakeholderList getPibAboveMarketStakeholderList() {
        return pibAboveMarketStakeholderList;
    }

    public void setPibAboveMarketStakeholderList(
            PIBStakeholderList pibAboveMarketStakeholderList) {
        this.pibAboveMarketStakeholderList = pibAboveMarketStakeholderList;
    }

    public Map<Long, Long> getAttachmentUser() {
        return attachmentUser;
    }

    public void setAttachmentUser(Map<Long, Long> attachmentUser) {
        this.attachmentUser = attachmentUser;
    }

    public Boolean getAllEndMarketSaved() {
        return allEndMarketSaved;
    }

    public void setAllEndMarketSaved(Boolean allEndMarketSaved) {
        this.allEndMarketSaved = allEndMarketSaved;
    }

    public String getPibCompleteNotifyAgencyRecipents() {
        return pibCompleteNotifyAgencyRecipents;
    }

    public void setPibCompleteNotifyAgencyRecipents(
            String pibCompleteNotifyAgencyRecipents) {
        this.pibCompleteNotifyAgencyRecipents = pibCompleteNotifyAgencyRecipents;
    }

    public String getPibCompleteNotifyAgencySubject() {
        return pibCompleteNotifyAgencySubject;
    }

    public void setPibCompleteNotifyAgencySubject(
            String pibCompleteNotifyAgencySubject) {
        this.pibCompleteNotifyAgencySubject = pibCompleteNotifyAgencySubject;
    }

    public String getPibCompleteNotifyAgencyMessageBody() {
        return pibCompleteNotifyAgencyMessageBody;
    }

    public void setPibCompleteNotifyAgencyMessageBody(
            String pibCompleteNotifyAgencyMessageBody) {
        this.pibCompleteNotifyAgencyMessageBody = pibCompleteNotifyAgencyMessageBody;
    }

    public List<Long> getUpdatedEndMarkets() {
        return updatedEndMarkets;
    }

    public void setUpdatedEndMarkets(List<Long> updatedEndMarkets) {
        this.updatedEndMarkets = updatedEndMarkets;
    }

    public String getPibNotifyAgencyRecipents() {
        return pibNotifyAgencyRecipents;
    }

    public void setPibNotifyAgencyRecipents(String pibNotifyAgencyRecipents) {
        this.pibNotifyAgencyRecipents = pibNotifyAgencyRecipents;
    }

    public String getPibNotifyAgencySubject() {
        return pibNotifyAgencySubject;
    }

    public void setPibNotifyAgencySubject(String pibNotifyAgencySubject) {
        this.pibNotifyAgencySubject = pibNotifyAgencySubject;
    }

    public String getPibNotifyAgencyMessageBody() {
        return pibNotifyAgencyMessageBody;
    }

    public void setPibNotifyAgencyMessageBody(String pibNotifyAgencyMessageBody) {
        this.pibNotifyAgencyMessageBody = pibNotifyAgencyMessageBody;
    }

    public String getPibNotifyAboveMarketRecipents() {
        return pibNotifyAboveMarketRecipents;
    }

    public void setPibNotifyAboveMarketRecipents(
            String pibNotifyAboveMarketRecipents) {
        this.pibNotifyAboveMarketRecipents = pibNotifyAboveMarketRecipents;
    }

    public String getPibNotifyAboveMarketSubject() {
        return pibNotifyAboveMarketSubject;
    }

    public void setPibNotifyAboveMarketSubject(String pibNotifyAboveMarketSubject) {
        this.pibNotifyAboveMarketSubject = pibNotifyAboveMarketSubject;
    }

    public String getPibNotifyAboveMarketMessageBody() {
        return pibNotifyAboveMarketMessageBody;
    }

    public void setPibNotifyAboveMarketMessageBody(
            String pibNotifyAboveMarketMessageBody) {
        this.pibNotifyAboveMarketMessageBody = pibNotifyAboveMarketMessageBody;
    }

    public String getPibApproveChangesRecipents() {
        return pibApproveChangesRecipents;
    }

    public void setPibApproveChangesRecipents(String pibApproveChangesRecipents) {
        this.pibApproveChangesRecipents = pibApproveChangesRecipents;
    }

    public String getPibApproveChangesSubject() {
        return pibApproveChangesSubject;
    }

    public void setPibApproveChangesSubject(String pibApproveChangesSubject) {
        this.pibApproveChangesSubject = pibApproveChangesSubject;
    }

    public String getPibApproveChangesMessageBody() {
        return pibApproveChangesMessageBody;
    }

    public void setPibApproveChangesMessageBody(String pibApproveChangesMessageBody) {
        this.pibApproveChangesMessageBody = pibApproveChangesMessageBody;
    }

    public Boolean getShowMandatoryFieldsError() {
        return showMandatoryFieldsError;
    }

    public void setShowMandatoryFieldsError(Boolean showMandatoryFieldsError) {
        this.showMandatoryFieldsError = showMandatoryFieldsError;
    }

    public Map<Long, String> getActionStandardMap() {
        return actionStandardMap;
    }

    public void setActionStandardMap(Map<Long, String> actionStandardMap) {
        this.actionStandardMap = actionStandardMap;
    }

    public Map<Long, String> getResearchDesignMap() {
        return researchDesignMap;
    }

    public void setResearchDesignMap(Map<Long, String> researchDesignMap) {
        this.researchDesignMap = researchDesignMap;
    }

    public Map<Long, String> getSampleProfileMap() {
        return sampleProfileMap;
    }

    public void setSampleProfileMap(Map<Long, String> sampleProfileMap) {
        this.sampleProfileMap = sampleProfileMap;
    }

    public Map<Long, String> getStimulusMaterialMap() {
        return stimulusMaterialMap;
    }

    public void setStimulusMaterialMap(Map<Long, String> stimulusMaterialMap) {
        this.stimulusMaterialMap = stimulusMaterialMap;
    }

    public Boolean getIsProposalAwarded() {
        return isProposalAwarded;
    }

    public void setIsProposalAwarded(Boolean isProposalAwarded) {
        this.isProposalAwarded = isProposalAwarded;
    }

    public ProposalManager getProposalManager() {
        return proposalManager;
    }

    public void setProposalManager(ProposalManager proposalManager) {
        this.proposalManager = proposalManager;
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

	public String getAboveMarketProjectContact() {
		return aboveMarketProjectContact;
	}

	public void setAboveMarketProjectContact(String aboveMarketProjectContact) {
		this.aboveMarketProjectContact = aboveMarketProjectContact;
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

	public Boolean getCanEditEM() {
		return canEditEM;
	}

	public void setCanEditEM(Boolean canEditEM) {
		this.canEditEM = canEditEM;
	}

	public String getSaveEmailForm() {
		return saveEmailForm;
	}

	public void setSaveEmailForm(String saveEmailForm) {
		this.saveEmailForm = saveEmailForm;
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

    public Boolean getAutoSave() {
        return isAutoSave;
    }

    public void setAutoSave(Boolean autoSave) {
        isAutoSave = autoSave;
    }

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}
	
	public void setPitUpdateOnly(String pitUpdateOnly) {
		this.pitUpdateOnly = pitUpdateOnly;
	}

	public String getEndMarketProjectUsers() {
		return endMarketProjectUsers;
	}

	public void setEndMarketProjectUsers(String endMarketProjectUsers) {
		this.endMarketProjectUsers = endMarketProjectUsers;
	}

	public String getSubjectNotifyEndMarketContacts() {
		return subjectNotifyEndMarketContacts;
	}

	public void setSubjectNotifyEndMarketContacts(
			String subjectNotifyEndMarketContacts) {
		this.subjectNotifyEndMarketContacts = subjectNotifyEndMarketContacts;
	}

	public String getMessageNotifyEndMarketContacts() {
		return messageNotifyEndMarketContacts;
	}

	public void setMessageNotifyEndMarketContacts(
			String messageNotifyEndMarketContacts) {
		this.messageNotifyEndMarketContacts = messageNotifyEndMarketContacts;
	}

	public SynchroUtils getSynchroUtils() {
		return synchroUtils;
	}

	public void setSynchroUtils(SynchroUtils synchroUtils) {
		this.synchroUtils = synchroUtils;
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

	public PIBMethodologyWaiver getPibKantarMethodologyWaiver() {
		return pibKantarMethodologyWaiver;
	}

	public void setPibKantarMethodologyWaiver(
			PIBMethodologyWaiver pibKantarMethodologyWaiver) {
		this.pibKantarMethodologyWaiver = pibKantarMethodologyWaiver;
	}

	public String getOtherBATUsers() {
		return otherBATUsers;
	}

	public void setOtherBATUsers(String otherBATUsers) {
		this.otherBATUsers = otherBATUsers;
	}

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

	public String getPageRequest() {
		return pageRequest;
	}

	public void setPageRequest(String pageRequest) {
		this.pageRequest = pageRequest;
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

