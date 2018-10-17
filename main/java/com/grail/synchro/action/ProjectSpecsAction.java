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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectSpecsReporting;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManager;
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
public class ProjectSpecsAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(PIBAction.class);
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
	
	List<SynchroStageToDoListBean> stageToDoList;
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
    private List<EndMarketInvestmentDetail> endMarketDetails;
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
	private PIBMethodologyWaiver pibMethodologyWaiver;

	// This field will check whether the user click on PS Methodology Waiver is Approve or Reject button or Send for Information or Request more information 
	private String methodologyWaiverAction;
	private Long projSpecsEndMarketId;
	private Boolean showMandatoryFieldsError;
	private EmailNotificationManager emailNotificationManager;
	
	private String subjectAdminNotifySPI;
	private String messageAdminNotifySPI;
	private String adminNotifySPIRecipents;
	
	private String subjectAdminReqForClatrification;
	private String messageAdminReqForClatrification;
	private String adminReqForClatrificationRecipents;
	
	private String subjectAdminFinalApproval;
	private String messageAdminFinalApproval;
	private String adminFinalApprovalRecipents;
	
	private File[] mailAttachment;
	private String[] mailAttachmentFileName;
	private String[] mailAttachmentContentType;
	
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
                
               // endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
                endMarketDetails = this.synchroProjectManager.getEndMarketDetails(projectID);
             	
                //List<ProjectSpecsInitiation> initiationList = this.projectSpecsManager.getProjectSpecsInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
                List<ProjectSpecsInitiation> initiationList = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
                projSpecsEndMarketId = initiationList.get(0).getEndMarketID();
                
                //Audit Logs
                List<ProjectSpecsInitiation> initiationList_DB = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
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
                                
                
                
               // psMethodologyWaiver = this.projectSpecsManager.getPSMethodologyWaiver(projectID, projSpecsEndMarketId);
                pibMethodologyWaiver = this.pibManager.getPIBMethodologyWaiver(projectID, projSpecsEndMarketId);
                if(pibMethodologyWaiver==null)
                {
                	pibMethodologyWaiver = new PIBMethodologyWaiver();
    	        }
                attachmentMap = this.projectSpecsManager.getDocumentAttachment(projectID, projSpecsEndMarketId);
                if( initiationList != null && initiationList.size() > 0) {
                    this.projectSpecsInitiation = initiationList.get(0);
                    projectSpecsReporting = this.projectSpecsManager.getProjectSpecsReporting(projectID,projSpecsEndMarketId);
                    projectSpecsEMDetails = this.projectSpecsManager.getProjectSpecsEMDetails(projectID,projSpecsEndMarketId);
                    
                    /*if(endMarketId!=null && endMarketId>0)
                    {
                    	 attachmentMap = this.projectSpecsManager.getDocumentAttachment(projectID, endMarketId);
                    }
                    else
                    {
                    	 attachmentMap = this.projectSpecsManager.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
                    	// pibStakeholderList = this.proposalManager.getPIBStakeholderList(projectID, endMarketIds.get(0));
                    }*/

                }  else {
                    this.projectSpecsInitiation = new ProjectSpecsInitiation();
                    
                    isSave = true;
                }
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
                // Moved from Input Action --
            //    String status=ribDocument.getProperties().get(SynchroConstants.STAGE_STATUS);
    			stageId = SynchroGlobal.getProjectActivityTab().get("research");
    			//approvers = stageManager.getStageApprovers(stageId.longValue(), project);
    			
    			String spiUserName="";
    			String spiApprovalDate=null;
    			//String legalUserName="";
    			//String screenerCCAppDate=null;
    			//String qdgAppDate=null;
    			if(projectSpecsInitiation.getApprover() >0)
    			{
    				spiUserName = userManager.getUser(projectSpecsInitiation.getApprover()).getName();
    			}
    			else if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact() > 0)
    			{
    				spiUserName = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName();
    			}
    			if(projectSpecsInitiation.getApprovedDate()!=null)
    			{
    				spiApprovalDate = df.format(projectSpecsInitiation.getApprovedDate());
    			}
    			/*PIBStakeholderList pibStakeholderList = pibManager.getPIBStakeholderList(projectID, endMarketDetails.get(0).getEndMarketID());
    			if(pibStakeholderList!=null && pibStakeholderList.getGlobalLegalContact()!=null)
    			{
    				legalUserName = userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getName();
    				if(projectSpecsInitiation.getScreenerCCApprovedDate()!=null)
    				{
    					screenerCCAppDate = df.format(projectSpecsInitiation.getScreenerCCApprovedDate());
    				}
    				if(projectSpecsInitiation.getQdgApprovedDate()!=null)
    				{
    					qdgAppDate =  df.format(projectSpecsInitiation.getQdgApprovedDate());
    				}
    			}*/
    			approvers.put(spiUserName, spiApprovalDate);
    			//approvers.put(legalUserName + "(PS,S,CC)", screenerCCAppDate);
    			//approvers.put(legalUserName + "(Q & DG)", qdgAppDate);
    		//	editStage=SynchroPermHelper.canEditStageDocument(ribDocument,projectID);
    			editStage=SynchroPermHelper.canEditProjectByStatus(projectID);
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
    			
    			String stageUrl = baseUrl+"/synchro/project-specs!input.jspa?projectID=" + project.getProjectID();
    			if(subjectAdminNotifySPI==null)
                {
    				subjectAdminNotifySPI = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
    				subjectAdminNotifySPI=subjectAdminNotifySPI.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    				subjectAdminNotifySPI=subjectAdminNotifySPI.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminNotifySPI==null)
                {
                   // String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID=" + project.getProjectID();
                    messageAdminNotifySPI = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
                    messageAdminNotifySPI=messageAdminNotifySPI.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminNotifySPI=messageAdminNotifySPI.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminNotifySPI=messageAdminNotifySPI.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                if(!SynchroUtils.isReferenceID(endMarketDetails.get(0).getSpiContact()))
                {
                	if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact() > 0)
                	{
                		adminNotifySPIRecipents=userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                	}
                	else
                	{
                		adminNotifySPIRecipents="";
                	}
                }
                else
                {
                	adminNotifySPIRecipents="Stakeholder Requested";
                }
                if(subjectAdminReqForClatrification==null)
                {
                	subjectAdminReqForClatrification = TemplateUtil.getTemplate("ps.request.clarification.subject", JiveGlobals.getLocale());
                	subjectAdminReqForClatrification=subjectAdminReqForClatrification.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminReqForClatrification=subjectAdminReqForClatrification.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminReqForClatrification==null)
                {
                   // String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID=" + project.getProjectID();
                    messageAdminReqForClatrification = TemplateUtil.getHtmlEscapedTemplate("ps.request.clarification.htmlBody", JiveGlobals.getLocale());
                    messageAdminReqForClatrification=messageAdminReqForClatrification.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminReqForClatrification=messageAdminReqForClatrification.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminReqForClatrification=messageAdminReqForClatrification.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                adminReqForClatrificationRecipents = stageManager.getNotificationRecipients(SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE, projectID, endMarketDetails.get(0).getEndMarketID());
                if(subjectAdminFinalApproval==null)
                {
                	subjectAdminFinalApproval = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
                	subjectAdminFinalApproval=subjectAdminFinalApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                	subjectAdminFinalApproval=subjectAdminFinalApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                }
                if(messageAdminFinalApproval==null)
                {
                    //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID=" + project.getProjectID();
                    messageAdminFinalApproval = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
                    messageAdminFinalApproval=messageAdminFinalApproval.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    messageAdminFinalApproval=messageAdminFinalApproval.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
                    messageAdminFinalApproval=messageAdminFinalApproval.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                }
                adminFinalApprovalRecipents = stageManager.getNotificationRecipients(SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE, projectID, endMarketDetails.get(0).getEndMarketID());
    			
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

         // These has been done as part of Jive 8 Upgradation
            if(projectSpecsInitiation.getProjectOwner()==null && getRequest().getParameter("projectOwnerOri")!=null)
            {
            	projectSpecsInitiation.setProjectOwner(new Long(getRequest().getParameter("projectOwnerOri")));
            }
            
            if(projectSpecsInitiation.getProjectOwner()==null && getRequest().getParameter("projectOwner")!=null)
            {
            	projectSpecsInitiation.setProjectOwner(Long.parseLong(getRequest().getParameter("projectOwner")));
            }
            
            if(projectSpecsInitiation.getSpiContact()==null && getRequest().getParameter("spiContactOri")!=null)
            {
            	projectSpecsInitiation.setSpiContact(new Long(getRequest().getParameter("spiContactOri")));
            }
            
            if(projectSpecsInitiation.getSpiContact()==null && getRequest().getParameter("spiContact")!=null)
            {
            	projectSpecsInitiation.setSpiContact(Long.parseLong(getRequest().getParameter("spiContact")));
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
            
            if(getRequest().getParameter("estimatedCost")!=null)
            {
            	try
            	{
	            	projectSpecsInitiation.setEstimatedCost(BigDecimal.valueOf(Double.valueOf(getRequest().getParameter("estimatedCost").replaceAll(",",""))));
	            	projectSpecsInitiation.setEstimatedCostType(Integer.valueOf(getRequest().getParameter("estimatedCostType")));
            	}
            	catch(Exception e)
            	{
            		LOG.error("Error while getting Estimated Cost Type");
            	}
            }
            if(getRequest().getParameter("latestEstimate")!=null)
            {
            	
            	try
            	{
            		projectSpecsInitiation.setLatestEstimate(BigDecimal.valueOf(Double.valueOf(getRequest().getParameter("latestEstimate").replaceAll(",", ""))));
                	projectSpecsInitiation.setLatestEstimateType(Integer.valueOf(getRequest().getParameter("latestEstimateType")));
            	}
            	catch(Exception e)
            	{
            		LOG.error("Error while getting Estimated Cost Type");
            	}
            	
            }
            
         // To map the Agency End Market Details
            binder = new ServletRequestDataBinder(this.projectSpecsEMDetails);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Proposal End Market Detail bean in Project Specs");
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
            
            // Data Collection is editable only in case of System Admin user.
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

            if(getRequest().getParameter("updatedSingleMarketId")!=null)
            {
            	updatedSingleMarketId = Long.valueOf(getRequest().getParameter("updatedSingleMarketId"));
            	//TODO : Currently this is done to update the Single End Market for grailPIB,
            	// grailPIBReporting and grailpibstakeholderlist and grailendmarketinvestment tables
            	// Need to revisit this logic in case of Multiple End Markets
            	projectSpecsInitiation.setEndMarketID(updatedSingleMarketId);
            	projectSpecsEMDetails.setEndMarketID(updatedSingleMarketId);
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

        }
    }

    public String input() {
      	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	
    	if ((SynchroPermHelper.hasProjectAccess(projectID) && SynchroPermHelper.canViewStage(projectID, 3)) || SynchroPermHelper.canAccessProject(projectID)) {
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
        
        
         //   this.synchroProjectManager.save(project);
            if(updatedSingleMarketId!=null)
            {
            	//this.synchroProjectManager.updateSingleEndMarketId(projectID, updatedSingleMarketId);
            	//update Endmarket in grailProjectSpecs
            	//update Endmarket in grailprojectspecsemdetails
            	//update Endmarket in grailprojectspecsreporting
            	this.projectSpecsManager.updateProjectSpecsEndMarketId(projectID, updatedSingleMarketId);
            	projectSpecsInitiation.setEndMarketID(updatedSingleMarketId);
            	projectSpecsEMDetails.setEndMarketID(updatedSingleMarketId);
            	//https://www.svn.sourcen.com/issues/18000
            	if(attachmentUser!=null && attachmentUser.size()>0)
            	{
            		for(Long attId:attachmentUser.keySet())
            		{
            			this.pibManager.updateDocumentAttachment(attId, projectID, updatedSingleMarketId);
            		}
            	}
            }
            if((projectSpecsInitiation.getBizQuestion()!=null && !projectSpecsInitiation.getBizQuestion().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
            		&& (projectSpecsInitiation.getResearchObjective()!=null && !projectSpecsInitiation.getResearchObjective().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
            		&& (projectSpecsInitiation.getActionStandard()!=null && !projectSpecsInitiation.getActionStandard().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
            		&& (projectSpecsInitiation.getResearchDesign()!=null && !projectSpecsInitiation.getResearchDesign().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
            		&& (projectSpecsInitiation.getSampleProfile()!=null && !projectSpecsInitiation.getSampleProfile().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
            		&& (projectSpecsInitiation.getStimulusMaterial()!=null && !projectSpecsInitiation.getStimulusMaterial().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
            		&& (projectSpecsInitiation.getStimulusMaterialShipped()!=null && !projectSpecsInitiation.getStimulusMaterialShipped().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId())!=null)
            		//&& projectSpecsInitiation.getOthers()!=null && !projectSpecsInitiation.getOthers().equals("")
            		
            		&& (projectSpecsInitiation.getTopLinePresentation()!=null && projectSpecsInitiation.getTopLinePresentation()|| projectSpecsInitiation.getPresentation()!=null && projectSpecsInitiation.getPresentation()
            		|| projectSpecsInitiation.getFullreport()!=null && projectSpecsInitiation.getFullreport())
            		//&& projectSpecsInitiation.getOtherReportingRequirements()!=null && !projectSpecsInitiation.getOtherReportingRequirements().equals("")
            		&& (projectSpecsInitiation.getScreener()!=null && !projectSpecsInitiation.getScreener().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null)
            		&& (projectSpecsInitiation.getConsumerCCAgreement()!=null && !projectSpecsInitiation.getConsumerCCAgreement().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null)
            		&& (projectSpecsInitiation.getQuestionnaire()!=null && !projectSpecsInitiation.getQuestionnaire().equals("")|| attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null)
            		&& (projectSpecsInitiation.getDiscussionguide()!=null && !projectSpecsInitiation.getDiscussionguide().equals("") || attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null))
            		
            {
            	// Quantitative
            	if(projectSpecsInitiation.getMethodologyType().intValue()==1)
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
            	else if(projectSpecsInitiation.getMethodologyType().intValue()==2)
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
            	else if(projectSpecsInitiation.getMethodologyType().intValue()==3)
            	{
            		if(projectSpecsEMDetails.getTotalCost()!=null && projectSpecsEMDetails.getIntMgmtCost()!=null && projectSpecsEMDetails.getLocalMgmtCost()!=null
                    		&& projectSpecsEMDetails.getFieldworkCost()!=null 
                    		&& projectSpecsEMDetails.getProposedFWAgencyNames()!=null && !projectSpecsEMDetails.getProposedFWAgencyNames().equals("")
                    		&& projectSpecsEMDetails.getFwEndDate()!=null && projectSpecsEMDetails.getFwStartDate()!=null
                    		//&& projectSpecsEMDetails.getDataCollectionMethod()!=null
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
            	else
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
            	
            }
            else
            {
            	projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
            	manFieldsError=true;
            }
            
            // Update the SPI contact
           if(projectSpecsInitiation.getSpiContact()!=null && projectSpecsInitiation.getSpiContact()>0)
           {
              this.synchroProjectManager.updateSPIContact(projectID, projectSpecsInitiation.getEndMarketID(), projectSpecsInitiation.getSpiContact());
           }
           if(projectSpecsInitiation.getProjectOwner()!=null && projectSpecsInitiation.getProjectOwner()>0)
           {
        	   this.synchroProjectManager.updateOwner(projectID, projectSpecsInitiation.getProjectOwner());
           }
           if(projectSpecsInitiation.getEstimatedCost()!=null && SynchroPermHelper.isSystemAdmin(getUser()))
           {
        	   EndMarketInvestmentDetail endMarketDetail = new EndMarketInvestmentDetail();
               endMarketDetail.setProjectID(projectID);
               endMarketDetail.setEndMarketID(projectSpecsInitiation.getEndMarketID());
               endMarketDetail.setInitialCost(projectSpecsInitiation.getEstimatedCost());
               endMarketDetail.setInitialCostCurrency(Long.valueOf(projectSpecsInitiation.getEstimatedCostType()));
               
        	   this.synchroProjectManager.updateInitialCostSingleEM(endMarketDetail);
           }
           if(projectSpecsInitiation.getLatestEstimate()!=null && SynchroPermHelper.isSystemAdmin(getUser()))
           {
        	   ProjectInitiation pi = new ProjectInitiation();
        	   pi.setProjectID(projectID);
        	   pi.setEndMarketID(projectSpecsInitiation.getEndMarketID());
        	   pi.setLatestEstimate(projectSpecsInitiation.getLatestEstimate());
        	   pi.setLatestEstimateType(projectSpecsInitiation.getLatestEstimateType());
        	   this.pibManager.updateLatestEstimate(pi);
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
            SynchroLogUtils.ProjectSpecsSave(project, projectSpecsInitiation_DB, projectSpecsInitiation, projectSpecsEMDetails_DB, projectSpecsEMDetails, projectSpecsReporting_DB, endMarketDetails_DB, projectInitiation_DB);
            
        } else {
            LOG.error("Project Specs Initiation was null");
            LOG.error("RIB Document has not been configured during the Wizard setup.");
            addActionError("RIB document missing.");
        }
        
      //Audit Logs: Project Specs SAVE
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
        
        /*if(manFieldsError)
        {
        	redirectURL="/synchro/project-specs!input.jspa?projectID="+projectID+"&validationError=true";
        	return "validationError";
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
    	//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID=" + projectID;
    	String baseUrl = URLUtils.getBaseURL(request);
    	String stageUrl = baseUrl+"/synchro/project-specs!input.jspa?projectID=" + projectID;
    	try
	      {
		        pibMethApp = userManager.getUser(pibMethodologyWaiver.getMethodologyApprover());
		        if(project.getProjectOwner()!=null)
		        {
		        	projectOwnerEmail = userManager.getUser(project.getProjectOwner()).getEmail();
		        }
		        if(endMarketDetails.get(0).getSpiContact()!=null)
		        {
		        	spiContactEmail = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
		        }
		        
		   }
	       catch(UserNotFoundException ue)
	       {
	        	LOG.error("User Not Found " + ue.getStackTrace());
	      }
    	
    	//Save Audit logs for change in Methodology Waiver related fields
        if(projectID!=null && endMarketDetails!=null && endMarketDetails.size()>0 && endMarketDetails.get(0).getEndMarketID()!=null)
        {
        	final PIBMethodologyWaiver pibMethodologyWaiver_DB = this.pibManager.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
            SynchroLogUtils.PIBWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
        }
        
    	if(methodologyWaiverAction!=null && methodologyWaiverAction.equals("Approve"))
    	{
    		 //this.projectSpecsManager.approvePSMethodologyWaiver(psMethodologyWaiver);
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
    		 //this.projectSpecsManager.rejectPSMethodologyWaiver(psMethodologyWaiver);
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
	    	
   		    
   		  /*  psMethodologyWaiver.setCreationBy(getUser().getID());
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
	    	
  		   //projectSpecsManager.reqForInfoPSMethodologyWaiver(psMethodologyWaiver);
	    	 pibManager.reqForInfoPIBMethodologyWaiver(pibMethodologyWaiver);
  		  // projectSpecsManager.updateProjectSpecsStatus(projectID, endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.PROJECT_SPECS_METH_WAIV_MORE_INFO_REQ.ordinal());
	    	 
	    	//Request More Information Audit logs
	            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
						SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
								project.getProjectID(), getUser().getID());
     	}
    	else
    	{
    	/*	psMethodologyWaiver.setCreationBy(getUser().getID());
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
	        
	      
	        projectSpecsManager.updateProjectSpecsStatus(projectID, endMarketDetails.get(0).getEndMarketID(), SynchroGlobal.StageStatus.PROJECT_SPECS_METH_WAIV_APP_PENDING.ordinal());
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
			//this.projectSpecsManager.updateProjectSpecsSendForApproval(projectID, endMarketDetails.get(0).getEndMarketID(), 1);
			this.projectSpecsManager.updateProjectSpecsSendForApproval(projectID, null, 1);
		//	this.projectSpecsManager.updateRequestClarificationModification(projectID, null);
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
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
			this.projectSpecsManager.updateProjectSpecsSendForApproval(projectID, endMarketDetails.get(0).getEndMarketID(), null);
			this.projectSpecsManager.updateRequestClarificationModification(projectID,endMarketDetails.get(0).getEndMarketID(), 1);
			//this.projectSpecsManager.updateProjectSpecsStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
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
			this.projectSpecsManager.approve(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
			this.projectSpecsManager.updateProjectSpecsStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
			
			// This has been done in case the Admin user Approves the PS once the Project is in Report Summary stage, then in that case
			// the Report Summary details should not be copied again.
			List<ReportSummaryInitiation> rsList = reportSummaryManager.getReportSummaryInitiation(projectID, endMarketDetails.get(0).getEndMarketID());
			if(rsList!=null && rsList.size()>0)
			{
				
			}
			else
			{
				ReportSummaryInitiation reportSummary = new ReportSummaryInitiation();
				reportSummary.setProjectID(projectID);
				reportSummary.setEndMarketID(endMarketDetails.get(0).getEndMarketID());
				reportSummary.setStatus(SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal());
				reportSummary.setCreationBy(getUser().getID());
				reportSummary.setCreationDate(System.currentTimeMillis());
	            
				reportSummary.setModifiedBy(getUser().getID());
				reportSummary.setModifiedDate(System.currentTimeMillis());
				reportSummaryManager.saveReportSummaryDetails(reportSummary);
			}
			EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,null,null);
			email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			 //Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
	       	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("FINAL APPROVAL");
	    	
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	//Final Approval
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.project.specs.notfic.approval");
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
    	LOG.info("Inside moveToNextStage--- "+ projectID); 

		this.projectSpecsManager.approve(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
		this.projectSpecsManager.updateProjectSpecsStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
		
		// This has been done in case the Admin user Approves the PS once the Project is in Report Summary stage, then in that case
		// the Report Summary details should not be copied again.
		List<ReportSummaryInitiation> rsList = reportSummaryManager.getReportSummaryInitiation(projectID, endMarketDetails.get(0).getEndMarketID());
		if(rsList!=null && rsList.size()>0)
		{
			
		}
		else
		{
			ReportSummaryInitiation reportSummary = new ReportSummaryInitiation();
			reportSummary.setProjectID(projectID);
			reportSummary.setEndMarketID(endMarketDetails.get(0).getEndMarketID());
			reportSummary.setStatus(SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal());
			reportSummary.setCreationBy(getUser().getID());
			reportSummary.setCreationDate(System.currentTimeMillis());
            
			reportSummary.setModifiedBy(getUser().getID());
			reportSummary.setModifiedDate(System.currentTimeMillis());
			reportSummaryManager.saveReportSummaryDetails(reportSummary);
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
		// Issue https://www.svn.sourcen.com/issues/17665
		projectSpecsEMDetails.setFwEndDate(projectSpecsEMDetails.getFwEndDateLatest());
		projectSpecsEMDetails.setFwStartDate(projectSpecsEMDetails.getFwStartDateLatest());
		projectSpecsEMDetails.setProjectEndDate(projectSpecsEMDetails.getProjectEndDateLatest());
		//this.projectSpecsManager.updateProjectSpecsEMDetails(projectSpecsEMDetails);
		this.projectSpecsManager.updateProjectSpecsFieldWorkDetails(projectSpecsEMDetails);
		//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID=" + projectID;
		String baseUrl = URLUtils.getBaseURL(request);
		String stageUrl = baseUrl+"/synchro/project-specs!input.jspa?projectID=" + projectID;
		
		// If Costs/Fieldwork dates on “Change Fieldwork/Cost Status” pop up are changed by external agency then an 
		// automatic notification should go out to SP&I that cost or dates have been changed.
		if(SynchroPermHelper.isExternalAgencyUser(projectID, endMarketDetails.get(0).getEndMarketID()))
		{
			try
			{	
				String recp = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail(); 
				EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"fieldwork.cost.dates.changes.ps.htmlBody","fieldwork.cost.dates.changes.ps.subject");
				
				//email.getContext().put("projectId", projectID);
				email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
				email.getContext().put("projectName",project.getName());
				email.getContext().put ("stageUrl",stageUrl);
				
				stageManager.sendNotification(getUser(),email);
				
				EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
		    	emailNotBean.setProjectID(projectID);
		    	emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
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
				LOG.error("User not found while sending notification for update FieldWork --"+endMarketDetails.get(0).getSpiContact());
			}
		}
		
		SynchroLogUtils.ProjectFieldworkSave(project, projectSpecsEMDetails, projectSpecsEMDetails_DB);
        return SUCCESS;
    }
	public String approveScreener(){
		this.projectSpecsManager.approveScreener(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
        return SUCCESS;
    }
	public String rejectScreener(){
		this.projectSpecsManager.rejectScreener(projectID,endMarketDetails.get(0).getEndMarketID());
        return SUCCESS;
    }
	public String approveQDG(){
		this.projectSpecsManager.approveQDG(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
        return SUCCESS;
    }
	public String rejectQDG(){
		this.projectSpecsManager.rejectQDG(projectID,endMarketDetails.get(0).getEndMarketID());
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
        /*Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
        try {
        	projectSpecsManager.removeAttachment(attachmentId);
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
			projectSpecsManager.removeAttachment(attachmentId);
			
			 //Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName +" deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID());
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
			document.add(new Paragraph("SPI Contact : "+ userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName()));
			//TODO kanwar
			if(project.getProposedMethodology()!=null && SynchroGlobal.getMethodologies().get(project.getProposedMethodology()!=null?project.getProposedMethodology().get(0).intValue():-1)!=null)
			{
				for(Long mid : project.getProposedMethodology())
				{
					document.add(new Paragraph("Proposed Methodology : "+ SynchroGlobal.getMethodologies().get(mid.intValue())));
				}
				
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
			boolean isExternalAgencyUser = SynchroPermHelper.isExternalAgencyUser(project.getProjectID(),endMarketDetails.get(0).getEndMarketID());
			if(!isExternalAgencyUser)
			{
				if(endMarketDetails.get(0).getInitialCost()!=null)
				{
					document.add(new Paragraph("Estimated Cost : "+ nf.format(endMarketDetails.get(0).getInitialCost()) + " "+  SynchroGlobal.getCurrencies().get(endMarketDetails.get(0).getInitialCostCurrency().intValue())));
				}
				else
				{
					document.add(new Paragraph("Estimated Cost : "));
				}
				List<ProjectInitiation> initiationList = this.pibManager.getPIBDetails(projectID);
				if(initiationList.get(0).getLatestEstimate()!=null)
				{
					document.add(new Paragraph("Latest Cost : "+ nf.format(initiationList.get(0).getLatestEstimate()) + " "+  SynchroGlobal.getCurrencies().get(initiationList.get(0).getLatestEstimateType().intValue())));
				}
				else
				{
					document.add(new Paragraph("Latest Cost : "));
				}
				
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

	public List<EndMarketInvestmentDetail> getEndMarketDetails() {
		return endMarketDetails;
	}

	public void setEndMarketDetails(List<EndMarketInvestmentDetail> endMarketDetails) {
		this.endMarketDetails = endMarketDetails;
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

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}

	public String getSubjectAdminNotifySPI() {
		return subjectAdminNotifySPI;
	}

	public void setSubjectAdminNotifySPI(String subjectAdminNotifySPI) {
		this.subjectAdminNotifySPI = subjectAdminNotifySPI;
	}

	public String getMessageAdminNotifySPI() {
		return messageAdminNotifySPI;
	}

	public void setMessageAdminNotifySPI(String messageAdminNotifySPI) {
		this.messageAdminNotifySPI = messageAdminNotifySPI;
	}

	public String getAdminNotifySPIRecipents() {
		return adminNotifySPIRecipents;
	}

	public void setAdminNotifySPIRecipents(String adminNotifySPIRecipents) {
		this.adminNotifySPIRecipents = adminNotifySPIRecipents;
	}

	public String getSubjectAdminReqForClatrification() {
		return subjectAdminReqForClatrification;
	}

	public void setSubjectAdminReqForClatrification(
			String subjectAdminReqForClatrification) {
		this.subjectAdminReqForClatrification = subjectAdminReqForClatrification;
	}

	public String getMessageAdminReqForClatrification() {
		return messageAdminReqForClatrification;
	}

	public void setMessageAdminReqForClatrification(
			String messageAdminReqForClatrification) {
		this.messageAdminReqForClatrification = messageAdminReqForClatrification;
	}

	public String getAdminReqForClatrificationRecipents() {
		return adminReqForClatrificationRecipents;
	}

	public void setAdminReqForClatrificationRecipents(
			String adminReqForClatrificationRecipents) {
		this.adminReqForClatrificationRecipents = adminReqForClatrificationRecipents;
	}

	public String getSubjectAdminFinalApproval() {
		return subjectAdminFinalApproval;
	}

	public void setSubjectAdminFinalApproval(String subjectAdminFinalApproval) {
		this.subjectAdminFinalApproval = subjectAdminFinalApproval;
	}

	public String getMessageAdminFinalApproval() {
		return messageAdminFinalApproval;
	}

	public void setMessageAdminFinalApproval(String messageAdminFinalApproval) {
		this.messageAdminFinalApproval = messageAdminFinalApproval;
	}

	
	public String getAdminFinalApprovalRecipents() {
		return adminFinalApprovalRecipents;
	}

	public void setAdminFinalApprovalRecipents(String adminFinalApprovalRecipents) {
		this.adminFinalApprovalRecipents = adminFinalApprovalRecipents;
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
