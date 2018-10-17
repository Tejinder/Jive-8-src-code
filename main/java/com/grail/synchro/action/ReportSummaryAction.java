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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectManager;
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
 * Action class for Report Summary Stage
 */
public class ReportSummaryAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ReportSummaryAction.class);
    //Spring Managers
    private ReportSummaryManager reportSummaryManager;
    private ProjectManager synchroProjectManager;
    private PIBManager pibManager;
    private ProjectEvaluationManager projectEvaluationManager;
    
    //Form related fields
    private ReportSummaryInitiation reportSummaryInitiation;

    private String attachmentName;
   
    private SynchroToIRIS synchroToIRIS;
    private SynchroToIRIS synchroToIRIS_DB;
    // This field will tell whether all the Fields on the SynchroTOIRIS window have been filled or not.
    private Boolean isAllSynchroToIRISFilled;




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
	
	List<SynchroStageToDoListBean> stageToDoList;
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
    private List<EndMarketInvestmentDetail> endMarketDetails;
    private Long endMarketId;
   
    //This map will contain the list of attachments for each field
    private Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();
    
	// This will contain  the Approvers data for the Checklist Tab
	Map<String,Map<String,String>> stageApprovers = new LinkedHashMap<String, Map<String,String>>();
	DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private Map<Long,Long> attachmentUser;
	private Boolean showMandatoryFieldsError;
	
	private EmailNotificationManager emailNotificationManager;
	
	private String subjectAdminSendForApproval;
	private String messageAdminSendForApproval;
	private String adminSendForApprovalRecipents;
	
	private String subjectAdminNeedsRevision;
	private String messageAdminNeedsRevision;
	private String adminNeedsRevisionRecipents;
	
	private String subjectAdminSendToProjectOwner;
	private String messageAdminSendToProjectOwner;
	private String adminSendToProjectOwnerRecipents;
		
	private String subjectAdminApprove;
	private String messageAdminApprove;
	private String adminAdminApproveRecipents;
	
	private String subjectAdminUploadToIris;
	private String messageAdminUploadToIris;
	private String adminAdminUploadToIrisRecipents;
	
	private String subjectAdminUploadToCPSIDatabase;
	private String messageAdminUploadToCPSIDatabase;
	private String adminUploadToCPSIDatabaseRecipents;
	
	private String subjectAdminSummaryUploadToIris;
	private String messageAdminSummaryUploadToIris;
	private String adminSummaryUploadToIrisRecipents;
	
	private File[] mailAttachment;
	private String[] mailAttachmentFileName;
	private String[] mailAttachmentContentType;
	private Long attachmentFieldID;
	
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
            /*    if(validationError!=null && validationError.equals("true"))
                {
                	showMandatoryFieldsError=true;
                }
              */ 
                project = this.synchroProjectManager.get(projectID);
                
               // endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
                endMarketDetails = this.synchroProjectManager.getEndMarketDetails(projectID);
             	
                List<ReportSummaryInitiation> initiationList = this.reportSummaryManager.getReportSummaryInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
                
                this.synchroToIRIS = this.reportSummaryManager.getSynchroToIRIS(projectID, endMarketDetails.get(0).getEndMarketID(), project);
                if(synchroToIRIS==null)
                {
                	synchroToIRIS = new SynchroToIRIS();
                }
                
                //Audit Logs
                this.synchroToIRIS_DB = this.reportSummaryManager.getSynchroToIRIS(projectID, endMarketDetails.get(0).getEndMarketID(), project);
                List<ReportSummaryInitiation> initiationList_DB = this.reportSummaryManager.getReportSummaryInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
                
                if(initiationList_DB!=null && initiationList_DB.size() > 0)
                {
                	 this.reportSummaryInitiation_DB = initiationList_DB.get(0);
                }
                
                
                attachmentMap = this.reportSummaryManager.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
                if( initiationList != null && initiationList.size() > 0) {
                    this.reportSummaryInitiation = initiationList.get(0);
                  //  projectSpecsReporting = this.projectSpecsManager.getProjectSpecsReporting(projectID,endMarketDetails.get(0).getEndMarketID());
                   // projectSpecsEMDetails = this.projectSpecsManager.getProjectSpecsEMDetails(projectID,endMarketDetails.get(0).getEndMarketID());
                    
                    if(endMarketId!=null && endMarketId>0)
                    {
                    	 attachmentMap = this.reportSummaryManager.getDocumentAttachment(projectID, endMarketId);
                    }
                    else
                    {
                    	 attachmentMap = this.reportSummaryManager.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
                    	// pibStakeholderList = this.proposalManager.getPIBStakeholderList(projectID, endMarketIds.get(0));
                    }

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
    			//approvers = stageManager.getStageApprovers(stageId.longValue(), project);
    		//	editStage=SynchroPermHelper.canEditStageDocument(ribDocument,projectID);
    			
    			String spiUserName="";
    			String spiApprovalDate=null;
    		//	String legalUserName="";
    		//	String legalAppDate=null;
    			if(reportSummaryInitiation.getSpiApprover() >0)
    			{
    				spiUserName = userManager.getUser(reportSummaryInitiation.getSpiApprover()).getName();
    			}
    			else if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact() > 0)
    			{
    				spiUserName = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName();
    			}
    			if(reportSummaryInitiation.getSpiApprovalDate()!=null)
    			{
    				spiApprovalDate = df.format(reportSummaryInitiation.getSpiApprovalDate());
    			}
    			/*PIBStakeholderList pibStakeholderList = pibManager.getPIBStakeholderList(projectID, endMarketDetails.get(0).getEndMarketID());
    			if(pibStakeholderList!=null && pibStakeholderList.getGlobalLegalContact()!=null)
    			{
    				legalUserName = userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getName();
    				if(reportSummaryInitiation.getLegalApprovalDate()!=null)
    				{
    					legalAppDate = df.format(reportSummaryInitiation.getLegalApprovalDate());
    				}
    				
    			}*/
    			approvers.put(spiUserName, spiApprovalDate);
    			//approvers.put(legalUserName , legalAppDate);
    			editStage = SynchroPermHelper.canEditProjectByStatus(projectID);
    			//TODO - To add the project and stage status check over here whether the Proposal stage is completed or not. 
    			String baseUrl = URLUtils.getBaseURL(request);
    			if(editStage)
    			{
    				stageToDoList = stageManager.getToDoListTabs(getUser(), projectID,stageId,project.getName(),endMarketDetails.get(0).getEndMarketID(),baseUrl);
    			}
    			else
    			{
    				//stageToDoList = stageManager.getDisabledToDoListTabs(stageId);
    				stageToDoList = stageManager.getDisabledToDoListTabs(getUser(), projectID,stageId,project.getName(),endMarketDetails.get(0).getEndMarketID(),baseUrl);
    			}
    			String stageUrl = baseUrl+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
    			
    			if(subjectAdminSendForApproval==null)
                {
    				subjectAdminSendForApproval = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
    				subjectAdminSendForApproval=subjectAdminSendForApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectAdminSendForApproval=subjectAdminSendForApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminSendForApproval==null)
                {
                  //  String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
                    messageAdminSendForApproval = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                    messageAdminSendForApproval=messageAdminSendForApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminSendForApproval=messageAdminSendForApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminSendForApproval=messageAdminSendForApproval.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact() > 0)
                {
                	adminSendForApprovalRecipents=userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                }
                else
                {
                	adminSendForApprovalRecipents="";
                }
                
                if(subjectAdminNeedsRevision==null)
                {
                	subjectAdminNeedsRevision = TemplateUtil.getTemplate("needs.revision.reportSummary.subject", JiveGlobals.getLocale());
                	subjectAdminNeedsRevision=subjectAdminNeedsRevision.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminNeedsRevision=subjectAdminNeedsRevision.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminNeedsRevision==null)
                {
                   // String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
                    messageAdminNeedsRevision = TemplateUtil.getHtmlEscapedTemplate("needs.revision.reportSummary.htmlBody", JiveGlobals.getLocale());
                    messageAdminNeedsRevision=messageAdminNeedsRevision.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminNeedsRevision=messageAdminNeedsRevision.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminNeedsRevision=messageAdminNeedsRevision.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                adminNeedsRevisionRecipents=stageManager.getNotificationRecipients(SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE, projectID, endMarketDetails.get(0).getEndMarketID());
                
                if(subjectAdminSendToProjectOwner==null)
                {
                	subjectAdminSendToProjectOwner = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
                	subjectAdminSendToProjectOwner=subjectAdminSendToProjectOwner.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminSendToProjectOwner=subjectAdminSendToProjectOwner.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminSendToProjectOwner==null)
                {
                 //   String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
                    messageAdminSendToProjectOwner = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
                    messageAdminSendToProjectOwner=messageAdminSendToProjectOwner.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminSendToProjectOwner=messageAdminSendToProjectOwner.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminSendToProjectOwner=messageAdminSendToProjectOwner.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                adminSendToProjectOwnerRecipents=stageManager.getNotificationRecipients(SynchroConstants.PROJECT_OWNER_ROLE, projectID, endMarketDetails.get(0).getEndMarketID());
                
                if(subjectAdminApprove==null)
                {
                	subjectAdminApprove = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
                	subjectAdminApprove=subjectAdminApprove.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminApprove=subjectAdminApprove.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminApprove==null)
                {
                //    String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
                    messageAdminApprove = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                    messageAdminApprove=messageAdminApprove.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminApprove=messageAdminApprove.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminApprove=messageAdminApprove.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                adminAdminApproveRecipents=stageManager.getNotificationRecipients(SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE+","+SynchroConstants.PROJECT_OWNER_ROLE, projectID, endMarketDetails.get(0).getEndMarketID());
                
                if(subjectAdminUploadToIris==null)
                {
                	subjectAdminUploadToIris = TemplateUtil.getTemplate("reportSummary.upload.on.iris.subject", JiveGlobals.getLocale());
                	subjectAdminUploadToIris=subjectAdminUploadToIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminUploadToIris=subjectAdminUploadToIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminUploadToIris==null)
                {
                 //   String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
                    messageAdminUploadToIris = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
                    messageAdminUploadToIris=messageAdminUploadToIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminUploadToIris=messageAdminUploadToIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminUploadToIris=messageAdminUploadToIris.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
              //  adminAdminUploadToIrisRecipents=userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                
                if(subjectAdminUploadToCPSIDatabase==null)
                {
                	subjectAdminUploadToCPSIDatabase = TemplateUtil.getTemplate("reportSummary.upload.on.c.psi.database.subject", JiveGlobals.getLocale());
                	subjectAdminUploadToCPSIDatabase=subjectAdminUploadToCPSIDatabase.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminUploadToCPSIDatabase=subjectAdminUploadToCPSIDatabase.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminUploadToCPSIDatabase==null)
                {
                 //   String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
                    messageAdminUploadToCPSIDatabase = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.c.psi.database.htmlBody", JiveGlobals.getLocale());
                    messageAdminUploadToCPSIDatabase=messageAdminUploadToCPSIDatabase.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminUploadToCPSIDatabase=messageAdminUploadToCPSIDatabase.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminUploadToCPSIDatabase=messageAdminUploadToCPSIDatabase.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
              //  adminUploadToCPSIDatabaseRecipents=userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                
                if(subjectAdminSummaryUploadToIris==null)
                {
                	subjectAdminSummaryUploadToIris = TemplateUtil.getTemplate("reportSummary.summary.upload.on.iris.subject", JiveGlobals.getLocale());
                	subjectAdminSummaryUploadToIris=subjectAdminSummaryUploadToIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminSummaryUploadToIris=subjectAdminSummaryUploadToIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminSummaryUploadToIris==null)
                {
                 //   String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + project.getProjectID();
                    messageAdminSummaryUploadToIris = TemplateUtil.getHtmlEscapedTemplate("reportSummary.summary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
                    messageAdminSummaryUploadToIris=messageAdminSummaryUploadToIris.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminSummaryUploadToIris=messageAdminSummaryUploadToIris.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminSummaryUploadToIris=messageAdminSummaryUploadToIris.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact() > 0)
                {               	
                    adminSummaryUploadToIrisRecipents=stageManager.getNotificationRecipients(SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE+","+SynchroConstants.PROJECT_OWNER_ROLE, projectID, endMarketDetails.get(0).getEndMarketID())+","+userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();;
                }
                else
                {
                	adminSummaryUploadToIrisRecipents=stageManager.getNotificationRecipients(SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE+","+SynchroConstants.PROJECT_OWNER_ROLE, projectID, endMarketDetails.get(0).getEndMarketID());
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
    	
    	if ((SynchroPermHelper.hasProjectAccess(projectID) && SynchroPermHelper.canViewStage(projectID, 4)) || SynchroPermHelper.canAccessProject(projectID)) {
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
        
        	if(reportSummaryInitiation.getLegalApproval() && reportSummaryInitiation.getLegalApprover()!=null && !reportSummaryInitiation.getLegalApprover().equals(""))
        	{
        		reportSummaryInitiation.setRepSummaryLegalApprovalDate(new Date(System.currentTimeMillis()));
        	}
        	
        	// https://svn.sourcen.com/issues/19652
            
        	reportSummaryInitiation.setComments(SynchroUtils.fixBulletPoint(reportSummaryInitiation.getComments()));
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
            SynchroLogUtils.ReportSave(project, reportSummaryInitiation, reportSummaryInitiation_DB);
        } else {
            LOG.error("Report Summary Initiation was null  ");
            addActionError("Report Summary Initiation was null.");
        }

      //Audit Logs: Report SUmmary SAVE
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());  
       
        /*if(manFieldsError)
        {
        	redirectURL="/synchro/report-summary!input.jspa?projectID="+projectID+"&validationError=true";
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
	 
    	/* if(StringUtils.isNotBlank(getRequest().getParameter("fieldWorkStartDate"))) {
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
			// This check is put in case the Report Stage is already completed and as part of email notification window the status should not be changed again.
         	// This is put as part of Quick Fix changes.
         	if(reportSummaryInitiation.getStatus()!=null && reportSummaryInitiation.getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal())
         	{
         		
         	}
         	else
         	{
         		 synchroToIRIS.setIrisSummaryRequired(null);
         	}
			// synchroToIRIS.setIrisSummaryRequired(null);
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
         		//https://svn.sourcen.com/issues/19987
         		//	synchroToIRIS.getFieldWorkStartDate()!=null && 
         		//	synchroToIRIS.getFieldWorkEndDate()!=null && 
         			synchroToIRIS.getReportDate()!=null &&
         			(synchroToIRIS.getSummaryWrittenBy()!=null && synchroToIRIS.getSummaryWrittenBy()>0) && 
         			//StringUtils.isNotBlank(synchroToIRIS.getRelatedStudy()) &&
         			StringUtils.isNotBlank(synchroToIRIS.getTags()) && 
         			synchroToIRIS.getAllDocsEnglish() && 
         			synchroToIRIS.getDisclaimer() &&
         			(synchroToIRIS.getBrand()!=null && synchroToIRIS.getBrand() >0))
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

    /**
	 * This method will perform the notification activities for the To Do List Actions for each stage.
	 * 
	 */
	public String sendNotification() {
		
		//Email Notification TimeStamp Storage
    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
    	emailNotBean.setProjectID(projectID);
    	emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
       	emailNotBean.setStageID(SynchroConstants.RS_STAGE);
       	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
       	
		if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND FOR APPROVAL"))
		{
			reportSummaryManager.updateSendForApproval(projectID,endMarketDetails.get(0).getEndMarketID(),1);
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
			//reportSummaryManager.updateSendForApproval(projectID,endMarketDetails.get(0).getEndMarketID(),null);
			reportSummaryManager.updateNeedRevision(projectID,endMarketDetails.get(0).getEndMarketID());
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
			reportSummaryManager.updateUploadToIRIS(projectID,endMarketDetails.get(0).getEndMarketID(),1);
			
			//Audit Logs: Notification
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.reportsummary.notf.uploadiris");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
			
		}
		
		if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("UPLOAD TO C-PSI DATABASE"))
		{
			reportSummaryManager.updateUploadToCPSIDatabase(projectID,endMarketDetails.get(0).getEndMarketID(),1);

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
			this.reportSummaryManager.approveSPI(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());



			// This is done in case the second option is clicked (Summary for IRIS not required) then the project should get completed.
			if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired()==2 && StringUtils.isNotBlank(synchroToIRIS.getIrisOptionRationale()))
			{
				this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
				
				 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
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
			this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
			
			 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
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
		/*if(approve!=null && approve.equals("approveSPI"))
		{
			this.reportSummaryManager.approveSPI(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
			// Only when both the SPI and Legal Approval are done Report Summary Stage is completed
			if(reportSummaryInitiation.getIsLegalApproved())
			{
				this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
				
				 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
				 ProjectEvaluationInitiation projectEvaluationInitiation = null;
				 if( initiationList != null && initiationList.size() > 0) {
					 projectEvaluationInitiation = initiationList.get(0);
					// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
		        		if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
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
		}
		if(approve!=null && approve.equals("approveLegal"))
		{
			this.reportSummaryManager.approveLegal(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
			// Only when both the SPI and Legal Approval are done Report Summary Stage is completed
			if(reportSummaryInitiation.getIsSPIApproved())
			{
				this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
				
				 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
				 ProjectEvaluationInitiation projectEvaluationInitiation = null;
				 if( initiationList != null && initiationList.size() > 0) {
					 projectEvaluationInitiation = initiationList.get(0);
					// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
		        		if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
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
			
		}*/
		
		return SUCCESS;
	}
	
	/**
     * This method will move the stage from Report to next stage
     * @return
     */
    public String moveToNextStage() 
    {
    	LOG.info("Inside moveToNextStage--- "+ projectID); 
		this.reportSummaryManager.approveSPI(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
		// This is done in case the second option is clicked (Summary for IRIS not required) then the project should get completed.
	/*	if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired()==2 && StringUtils.isNotBlank(synchroToIRIS.getIrisOptionRationale()))
		{
			this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
			
			 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
			 ProjectEvaluationInitiation projectEvaluationInitiation = null;
			 if( initiationList != null && initiationList.size() > 0) {
				 projectEvaluationInitiation = initiationList.get(0);
				// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
	        		//if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
				// Only when the Ratings (either IM, LM or FA) have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
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
		}
/  */
    	//return SUCCESS;
		//return SUCCESS;
		
		this.reportSummaryManager.updateReportSummaryStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
		
		 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
		 ProjectEvaluationInitiation projectEvaluationInitiation = null;
		 if( initiationList != null && initiationList.size() > 0) {
			 projectEvaluationInitiation = initiationList.get(0);
			// Only when the Ratings have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
       		//if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfLM()!=null)
			// Only when the Ratings (either IM, LM or FA) have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
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
            												project.getProjectID(), getUser().getID());
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
       /* Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
        try {
        	reportSummaryManager.removeAttachment(attachmentId);
            result.put("success", true);
            result.put("message", "Successfully removed document");
        } catch (AttachmentException e) {
            result.put("success", false);
            result.put("message", "Attachment not found.");
        } catch (UnauthorizedException e) {
            result.put("success", false);
            result.put("message", "Unauthorized to remove document.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return null;*/
		try
		{
			reportSummaryManager.removeAttachment(attachmentId);
			
			//https://www.svn.sourcen.com//issues/19256
			if(endMarketId!=null && endMarketId>0)
            {
            	 attachmentMap = this.reportSummaryManager.getDocumentAttachment(projectID, endMarketId);
            }
            else
            {
            	 attachmentMap = this.reportSummaryManager.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
            }
			if(!attachmentMap.containsKey(SynchroAttachmentObject.FULL_REPORT.getId()))
			{
				reportSummaryManager.updateFullReport(projectID, null);
			}
		/*	if(!attachmentMap.containsKey(SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()))
			{
				reportSummaryManager.updateSummaryForIris(projectID, null);
			}
			*/
			//Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName +" deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID());
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

	public List<EndMarketInvestmentDetail> getEndMarketDetails() {
		return endMarketDetails;
	}

	public void setEndMarketDetails(List<EndMarketInvestmentDetail> endMarketDetails) {
		this.endMarketDetails = endMarketDetails;
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

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}

	public String getSubjectAdminSendForApproval() {
		return subjectAdminSendForApproval;
	}

	public void setSubjectAdminSendForApproval(String subjectAdminSendForApproval) {
		this.subjectAdminSendForApproval = subjectAdminSendForApproval;
	}

	public String getMessageAdminSendForApproval() {
		return messageAdminSendForApproval;
	}

	public void setMessageAdminSendForApproval(String messageAdminSendForApproval) {
		this.messageAdminSendForApproval = messageAdminSendForApproval;
	}

	public String getAdminSendForApprovalRecipents() {
		return adminSendForApprovalRecipents;
	}

	public void setAdminSendForApprovalRecipents(
			String adminSendForApprovalRecipents) {
		this.adminSendForApprovalRecipents = adminSendForApprovalRecipents;
	}

	public String getSubjectAdminNeedsRevision() {
		return subjectAdminNeedsRevision;
	}

	public void setSubjectAdminNeedsRevision(String subjectAdminNeedsRevision) {
		this.subjectAdminNeedsRevision = subjectAdminNeedsRevision;
	}

	public String getMessageAdminNeedsRevision() {
		return messageAdminNeedsRevision;
	}

	public void setMessageAdminNeedsRevision(String messageAdminNeedsRevision) {
		this.messageAdminNeedsRevision = messageAdminNeedsRevision;
	}

	public String getAdminNeedsRevisionRecipents() {
		return adminNeedsRevisionRecipents;
	}

	public void setAdminNeedsRevisionRecipents(String adminNeedsRevisionRecipents) {
		this.adminNeedsRevisionRecipents = adminNeedsRevisionRecipents;
	}

	public String getSubjectAdminSendToProjectOwner() {
		return subjectAdminSendToProjectOwner;
	}

	public void setSubjectAdminSendToProjectOwner(
			String subjectAdminSendToProjectOwner) {
		this.subjectAdminSendToProjectOwner = subjectAdminSendToProjectOwner;
	}

	public String getMessageAdminSendToProjectOwner() {
		return messageAdminSendToProjectOwner;
	}

	public void setMessageAdminSendToProjectOwner(
			String messageAdminSendToProjectOwner) {
		this.messageAdminSendToProjectOwner = messageAdminSendToProjectOwner;
	}

	public String getAdminSendToProjectOwnerRecipents() {
		return adminSendToProjectOwnerRecipents;
	}

	public void setAdminSendToProjectOwnerRecipents(
			String adminSendToProjectOwnerRecipents) {
		this.adminSendToProjectOwnerRecipents = adminSendToProjectOwnerRecipents;
	}

	public String getSubjectAdminApprove() {
		return subjectAdminApprove;
	}

	public void setSubjectAdminApprove(String subjectAdminApprove) {
		this.subjectAdminApprove = subjectAdminApprove;
	}

	public String getMessageAdminApprove() {
		return messageAdminApprove;
	}

	public void setMessageAdminApprove(String messageAdminApprove) {
		this.messageAdminApprove = messageAdminApprove;
	}

	public String getAdminAdminApproveRecipents() {
		return adminAdminApproveRecipents;
	}

	public void setAdminAdminApproveRecipents(String adminAdminApproveRecipents) {
		this.adminAdminApproveRecipents = adminAdminApproveRecipents;
	}

	public String getSubjectAdminUploadToIris() {
		return subjectAdminUploadToIris;
	}

	public void setSubjectAdminUploadToIris(String subjectAdminUploadToIris) {
		this.subjectAdminUploadToIris = subjectAdminUploadToIris;
	}

	public String getMessageAdminUploadToIris() {
		return messageAdminUploadToIris;
	}

	public void setMessageAdminUploadToIris(String messageAdminUploadToIris) {
		this.messageAdminUploadToIris = messageAdminUploadToIris;
	}

	public String getAdminAdminUploadToIrisRecipents() {
		return adminAdminUploadToIrisRecipents;
	}

	public void setAdminAdminUploadToIrisRecipents(
			String adminAdminUploadToIrisRecipents) {
		this.adminAdminUploadToIrisRecipents = adminAdminUploadToIrisRecipents;
	}

	public String getSubjectAdminUploadToCPSIDatabase() {
		return subjectAdminUploadToCPSIDatabase;
	}

	public void setSubjectAdminUploadToCPSIDatabase(
			String subjectAdminUploadToCPSIDatabase) {
		this.subjectAdminUploadToCPSIDatabase = subjectAdminUploadToCPSIDatabase;
	}

	public String getMessageAdminUploadToCPSIDatabase() {
		return messageAdminUploadToCPSIDatabase;
	}

	public void setMessageAdminUploadToCPSIDatabase(
			String messageAdminUploadToCPSIDatabase) {
		this.messageAdminUploadToCPSIDatabase = messageAdminUploadToCPSIDatabase;
	}

	public String getAdminUploadToCPSIDatabaseRecipents() {
		return adminUploadToCPSIDatabaseRecipents;
	}

	public void setAdminUploadToCPSIDatabaseRecipents(
			String adminUploadToCPSIDatabaseRecipents) {
		this.adminUploadToCPSIDatabaseRecipents = adminUploadToCPSIDatabaseRecipents;
	}

	public String getSubjectAdminSummaryUploadToIris() {
		return subjectAdminSummaryUploadToIris;
	}

	public void setSubjectAdminSummaryUploadToIris(
			String subjectAdminSummaryUploadToIris) {
		this.subjectAdminSummaryUploadToIris = subjectAdminSummaryUploadToIris;
	}

	public String getMessageAdminSummaryUploadToIris() {
		return messageAdminSummaryUploadToIris;
	}

	public void setMessageAdminSummaryUploadToIris(
			String messageAdminSummaryUploadToIris) {
		this.messageAdminSummaryUploadToIris = messageAdminSummaryUploadToIris;
	}

	public String getAdminSummaryUploadToIrisRecipents() {
		return adminSummaryUploadToIrisRecipents;
	}

	public void setAdminSummaryUploadToIrisRecipents(
			String adminSummaryUploadToIrisRecipents) {
		this.adminSummaryUploadToIrisRecipents = adminSummaryUploadToIrisRecipents;
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
