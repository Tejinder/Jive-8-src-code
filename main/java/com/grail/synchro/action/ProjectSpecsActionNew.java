package com.grail.synchro.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectSpecsManagerNew;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroLogUtilsNew;
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
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.mail.util.TemplateUtil;
import com.jivesoftware.util.InputStreamDataSource;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;
/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectSpecsActionNew extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(PIBAction.class);
    //Spring Managers
    private ProjectSpecsManagerNew projectSpecsManagerNew;
    private ProjectManagerNew synchroProjectManagerNew;
    private ReportSummaryManagerNew reportSummaryManagerNew;
    private PIBManagerNew pibManagerNew;
    private ProposalManagerNew proposalManagerNew;
    //Form related fields
    private ProjectSpecsInitiation projectSpecsInitiation;
    private ProjectSpecsInitiation projectSpecsInitiation_DB;
    private Project project;
    private Project project_DB;
    private Long projectID;
 
    private ProjectSpecsEndMarketDetails projectSpecsEMDetails;
    private String attachmentName;
    
    private boolean isSave;
    
    private boolean editStage;
   
    private String notificationTabId;

	private String redirectURL;
	private String approve;
	private String recipients;
	private String subject;
	private String messageBody;
	
	
	private Integer stageId;
		
	
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
    
	DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private Map<Long,Long> attachmentUser;
	// This will containg the Methodology Waiver Related fields
	//private PSMethodologyWaiver psMethodologyWaiver;
	private PIBMethodologyWaiver pibMethodologyWaiver;

	// This field will check whether the user click on PS Methodology Waiver is Approve or Reject button or Send for Information or Request more information 
	private String methodologyWaiverAction;
	 private String kantarMethodologyWaiverAction;
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
	
	List<ProjectCostDetailsBean> projectCostDetailsList;
    private SynchroUtils synchroUtils;
	private PIBMethodologyWaiver pibKantarMethodologyWaiver;
	private ProjectInitiation projectInitiation;
	private ProposalInitiation proposalInitiation;
	Map<Long, List<User>> endMarketLegalApprovers;
	private List<ProjectCostDetailsBean> projectCostDetails; 
	private List<Long> endMarketIds;
	
	private ProjectInitiation projectInitiation_DB;
	private ProposalInitiation proposalInitiation_DB;
	
	private Map<Integer, BigDecimal> totalCosts;
	 
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
                project = this.synchroProjectManagerNew.get(projectID);
                project_DB = this.synchroProjectManagerNew.get(projectID);
                
               // endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
                endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
                endMarketIds = this.synchroProjectManagerNew.getEndMarketIDs(projectID);
             	
                //List<ProjectSpecsInitiation> initiationList = this.projectSpecsManager.getProjectSpecsInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
                List<ProjectSpecsInitiation> initiationList = this.projectSpecsManagerNew.getProjectSpecsInitiationNew(projectID);
                //projSpecsEndMarketId = initiationList.get(0).getEndMarketID();
                
              
                pibMethodologyWaiver = this.pibManagerNew.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                projectCostDetailsList = this.synchroProjectManagerNew.getProjectCostDetails(projectID);
                
                totalCosts = this.synchroProjectManagerNew.getTotalCosts(projectCostDetailsList, project.getTotalCost());
                
                endMarketLegalApprovers = getSynchroUtils().getLegalApprovers();
                if(pibMethodologyWaiver==null)
                {
                	pibMethodologyWaiver = new PIBMethodologyWaiver();
    	        }
                if(pibKantarMethodologyWaiver==null)
                {
                	pibKantarMethodologyWaiver = new PIBMethodologyWaiver();
    	        }
               
                // attachmentMap = this.projectSpecsManagerNew.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
                
                attachmentMap = this.projectSpecsManagerNew.getDocumentAttachment(projectID, new Long("-1"));
                
                if( initiationList != null && initiationList.size() > 0) {
                    this.projectSpecsInitiation = initiationList.get(0);
                   
                 
                    projectSpecsEMDetails = this.projectSpecsManagerNew.getProjectSpecsEMDetails(projectID,endMarketDetails.get(0).getEndMarketID());
                 
                }  else {
                    this.projectSpecsInitiation = new ProjectSpecsInitiation();
                    
                    isSave = true;
                }
                
                List<ProjectSpecsInitiation> initiationList_DB = this.projectSpecsManagerNew.getProjectSpecsInitiationNew(projectID);
            	if( initiationList_DB != null && initiationList_DB.size() > 0) {
            		 projectSpecsInitiation_DB = initiationList_DB.get(0); 		
            	}
            	
            	List<ProposalInitiation> proposalInitiationList_DB = this.proposalManagerNew.getProposalInitiationNew(projectID);
            	if( proposalInitiationList_DB != null && proposalInitiationList_DB.size() > 0) {
            		proposalInitiation_DB = proposalInitiationList_DB.get(0);            		
            	}
            	
            	List<ProjectInitiation> projectInitiationList_DB = this.pibManagerNew.getPIBDetailsNew(projectID);
            	if( projectInitiationList_DB != null && projectInitiationList_DB.size() > 0) {
            		projectInitiation_DB = projectInitiationList_DB.get(0);            		
            	}
                
                List<ProjectInitiation> pibInitiationList = this.pibManagerNew.getPIBDetailsNew(projectID);
                List<ProposalInitiation> proposalInitiationList = this.proposalManagerNew.getProposalInitiationNew(projectID);
                
                if( pibInitiationList != null && pibInitiationList.size() > 0) {
                    this.projectInitiation = pibInitiationList.get(0);
                 
                   
                 
                }  else {
                    this.projectInitiation = new ProjectInitiation();
                    
                }
                if( proposalInitiationList != null && proposalInitiationList.size() > 0) {
                    this.proposalInitiation = proposalInitiationList.get(0);
                 
                   
                 
                }  else {
                    this.proposalInitiation = new ProposalInitiation();
                    
                    
                }
                
                List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
                for(Integer i : attachmentMap.keySet())
                {
                	abList.addAll(attachmentMap.get(i));
                }
                attachmentUser = pibManagerNew.getAttachmentUser(abList);
                
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
    			
    		//	editStage=SynchroPermHelper.canEditProjectByStatus(projectID);
    			//TODO - To add the project and stage status check over here whether the Proposal stage is completed or not. 
    			String baseUrl = URLUtils.getBaseURL(request);
    			
    			
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
               
        }

        
        // Contenttype check is required to skip the below binding in case odf adding attachments
        if(getRequest().getMethod() == "POST" && !getRequest().getContentType().startsWith("multipart/form-data") && getRequest().getParameter("attachmentId")==null) {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.projectSpecsInitiation);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Project Specs Initiation bean.");
                input();
            }

            // To map the Project Parameters
            binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());

            
        
            if(StringUtils.isNotBlank(getRequest().getParameter("startDate"))) {
                this.project.setStartDate(DateUtils.parse(getRequest().getParameter("startDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("endDate"))) {
                this.project.setEndDate(DateUtils.parse(getRequest().getParameter("endDate")));
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
            if(getRequest().getParameter("kantarMethodologyWaiverAction")!=null && getRequest().getParameter("kantarMethodologyWaiverAction").equals("Waiver Attachment"))
            {
                kantarMethodologyWaiverAction = "Waiver Attachment";
                pibKantarMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
            }
          

        }
    }

    public String input() {

    	if (SynchroPermHelper.hasProjectAccessNew1(projectID) ||  SynchroPermHelper.userTypeAccess(projectID) || SynchroPermHelper.isLegaUserType()) {
    		//return INPUT;
    		redirectURL = ProjectStage.generateURLNew(project);
    		
    		 // This scenario is for in case the Legal Approver has changed its Budget Location or End Market
	       	 if(SynchroPermHelper.isLegaUserType())
	       	 {
	       		 if(SynchroPermHelper.legaUserTypeCheck(project))
	       		 {
	       			 
	       		 }
	       		 else
	       		 {
	       			 return UNAUTHORIZED;
	       		 }
	       	 }
       	 
    		// Only System Admin can access the Cancel Projects
        	if(project.getIsCancel())
        	{
        		//if(!SynchroPermHelper.isSystemAdmin())
        		if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner())
        		{
        			
        		}
        		else
        		{
        			return UNAUTHORIZED;
        		}
        	}
        	
        	if(redirectURL.contains("project-specs"))
            {
            	return INPUT;
            }
            else
            {
            	
            	return "redirectNextStage";
            }
    	}
    	else
    	{
    		return UNAUTHORIZED;
    	}
		/*
    	if ((SynchroPermHelper.hasProjectAccess(projectID) && SynchroPermHelper.canViewStage(projectID, 3)) || SynchroPermHelper.canAccessProject(projectID)) {
    		return INPUT;	
    	}
    	else
    	{
    		return UNAUTHORIZED;
    	}*/	
    }

    public String execute(){

        LOG.info("Save the Project Specs Details ...."+projectSpecsInitiation);
        Boolean manFieldsError = false;
      
        project.setDescription(SynchroUtils.fixBulletPoint(project.getDescription()));
        project.setProjectID(projectID);
        
        project.setModifiedBy(getUser().getID());
        project.setModifiedDate(System.currentTimeMillis());
       
        if(StringUtils.isNotBlank(getRequest().getParameter("deviationFromSM")))
        {
        	project.setMethWaiverReq(Integer.parseInt(getRequest().getParameter("deviationFromSM")));
        }
        
        project.setHasNewSynchroSaved(true);
        synchroProjectManagerNew.updateProjectNew(project);
        
         updateReferenceEndMarkets();
        
        if( projectSpecsInitiation != null){
        	projectSpecsInitiation.setProjectID(projectID);
        }
        if(isSave) {
        	projectSpecsInitiation.setCreationBy(getUser().getID());
        	projectSpecsInitiation.setCreationDate(System.currentTimeMillis());

        	projectSpecsInitiation.setModifiedBy(getUser().getID());
        	projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());

           
            // https://svn.sourcen.com/issues/19652
        	projectSpecsInitiation.setDocumentation(SynchroUtils.fixBulletPoint(projectSpecsInitiation.getDocumentation()));
        	projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());
           
            this.projectSpecsManagerNew.saveProjectSpecsDetails(projectSpecsInitiation);
            
            //Audit Log: Add User Audit details for PIB Save               
            SynchroLogUtilsNew.ProjectSpecsSaveNew(projectSpecsInitiation_DB, projectSpecsInitiation, project_DB, project);         

        }
        else {
        	projectSpecsInitiation.setModifiedBy(getUser().getID());
        	projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());

        	projectSpecsInitiation.setDocumentation(SynchroUtils.fixBulletPoint(projectSpecsInitiation.getDocumentation()));
       
		 if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
            {
            	projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
            	//Update Project Status to Report Summary Stage
            	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
            }
            else
            {
            	projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
            	
            }
            this.projectSpecsManagerNew.updateProjectSpecsDetailsNew(projectSpecsInitiation);
            
          //Audit Log: Add User Audit details for PIB Save               
          SynchroLogUtilsNew.ProjectSpecsSaveNew(projectSpecsInitiation_DB, projectSpecsInitiation, project_DB, project);

        }
        
        //Audit Logs: PROJECT SPECS SAVE
        String i18Text = getText("logger.project.documentation.saved.text");
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
        
        if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
        {
        	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
        	return "report-summary";
        }
        
        
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
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/project-specs!input.jspa?projectID=" + projectID;

        try
        {
           // pibMethApp = userManager.getUser(pibMethodologyWaiver.getMethodologyApprover());
            if(pibMethodologyWaiver!=null && pibMethodologyWaiver.getMethodologyApprover()!=null && pibMethodologyWaiver.getMethodologyApprover() >0)
        	{
        		pibMethApp = userManager.getUser(pibMethodologyWaiver.getMethodologyApprover());
        	}
            if(project.getProjectOwner()!=null)
            {
                //projectOwnerEmail = userManager.getUser(project.getProjectOwner()).getEmail();
                projectOwnerEmail = SynchroUtils.getUserEmail(project.getProjectOwner());
            }
            if(endMarketDetails.get(0).getSpiContact()!=null)
            {
                //spiContactEmail = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                spiContactEmail = SynchroUtils.getUserEmail(endMarketDetails.get(0).getSpiContact());
            }

        }
        catch(UserNotFoundException ue)
        {

        }
        
      //Save Audit logs for change in Methodology Waiver related fields
        if(projectID!=null && endMarketDetails!=null && endMarketDetails.size()>0 && endMarketDetails.get(0).getEndMarketID()!=null)
        {
        	final PIBMethodologyWaiver pibMethodologyWaiver_DB = this.pibManagerNew.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
            SynchroLogUtils.PIBWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId());
        }
        
        if(methodologyWaiverAction!=null && methodologyWaiverAction.equals("Approve"))
        {
            this.pibManagerNew.approvePIBMethodologyWaiver(pibMethodologyWaiver);

            //EmailNotification#4 Recipients for Approval
            if(projectID!=null)
            {
                //String recp = projectOwnerEmail+","+spiContactEmail;
                
                Long waiverRequestor = pibMethodologyWaiver.getCreationBy();
                String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
                
                String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
                String adminName = SynchroPermHelper.getSystemAdminName();
                
                EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"approve.waiver.new.htmlBody","approve.waiver.new.subject");

                //email.getContext().put("projectId", projectID);

                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                email.getContext().put("projectName",project.getName());
                email.getContext().put ("stageUrl",stageUrl);
                //This is the Approver Name
                email.getContext().put ("waiverApprover",getUser().getName());
                
                email.getContext().put ("waiverType","Methodology");
                
                stageManager.sendNotificationNew(adminName, adminEmail, email);

                //Email Notification TimeStamp Storage
                EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                emailNotBean.setProjectID(projectID);
                emailNotBean.setEndmarketID(endMarketId);
                emailNotBean.setStageID(SynchroConstants.PS_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
                
            }
            //Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.waiver.approve"), project.getName(), 
            												project.getProjectID(), getUser().getID());
        }
        else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Reject"))
        {
            this.pibManagerNew.rejectPIBMethodologyWaiver(pibMethodologyWaiver);

            //EmailNotification#5 Recipients for Reject
            if(projectID!=null)
            {
               // String recp = projectOwnerEmail+","+spiContactEmail;
            	 Long waiverRequestor = pibMethodologyWaiver.getCreationBy();
                 String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
                 
                 String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
                 String adminName = SynchroPermHelper.getSystemAdminName();
                 
                 
                 
                EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"reject.waiver.new.htmlBody","reject.waiver.new.subject");

                //email.getContext().put("projectId", projectID);
                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                email.getContext().put("projectName",project.getName());
                email.getContext().put ("stageUrl",stageUrl);
                
                email.getContext().put ("waiverRejector",getUser().getName());
                email.getContext().put ("waiverType","Methodology");
                stageManager.sendNotificationNew(adminName, adminEmail, email);


                //Email Notification TimeStamp Storage
                EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                emailNotBean.setProjectID(projectID);
                emailNotBean.setEndmarketID(endMarketId);
                emailNotBean.setStageID(SynchroConstants.PS_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
            
          //Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.waiver.reject"), project.getName(), 
            												project.getProjectID(), getUser().getID());
        }
        else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Send for Information"))
        {
         
        }
        else if (methodologyWaiverAction!=null && methodologyWaiverAction.equals("Request more Information"))
        {
            //TODO: change the subject and message
            //EmailNotification#3 Recipients for Request more Information
            //String recp = projectOwnerEmail+","+spiContactEmail;
            Long waiverRequestor = pibMethodologyWaiver.getCreationBy();
            String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
            
            
            String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
            String adminName = SynchroPermHelper.getSystemAdminName();
            
            EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"request.more.information.waiver.new.htmlBody","request.more.information.waiver.new.subject");
            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            // email.getContext().put ("waiverInitiator",getUser().getName());
            try
            {
            	email.getContext().put ("waiverInitiator",userManager.getUser(waiverRequestor).getName());
            }
            catch(UserNotFoundException e)
            {
            	e.printStackTrace();
            	email.getContext().put ("waiverInitiator","");
            }
            
            
            email.getContext().put ("waiverType","Methodology");
            
            stageManager.sendNotificationNew(adminName, adminEmail, email);

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            emailNotBean.setEndmarketID(endMarketId);
            emailNotBean.setStageID(SynchroConstants.PS_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("Notification | More information required on Methodology Waiver");
            emailNotBean.setEmailSubject("Notification | More information required on Methodology Waiver");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(waiverRequestorEmail);
            emailNotificationManager.saveDetails(emailNotBean);

            pibManagerNew.reqForInfoPIBMethodologyWaiver(pibMethodologyWaiver);
            //pibManager.updatePIBStatus(projectID, SynchroGlobal.StageStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal());
            
          //Request More Information Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
        else
        {
            pibMethodologyWaiver.setCreationBy(getUser().getID());
            pibMethodologyWaiver.setCreationDate(System.currentTimeMillis());

            pibMethodologyWaiver.setModifiedBy(getUser().getID());
            pibMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
            pibMethodologyWaiver.setIsApproved(null);
            pibMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
            
            
            // The System Owner will be the Methodology Approver
            List<User> systemOwnerList = getSynchroUtils().getSynchroSystemOwnerUsers();
            if(systemOwnerList!=null && systemOwnerList.size()>0)
            {
            	pibMethodologyWaiver.setMethodologyApprover(systemOwnerList.get(0).getID());
            }
            this.pibManagerNew.savePIBMethodologyWaiver(pibMethodologyWaiver);
           
           try
           {
        	   pibMethApp = userManager.getUser(pibMethodologyWaiver.getMethodologyApprover());
           }
           catch(UserNotFoundException ue)
           {

           }
           project.setMethWaiverReq(new Integer("1"));
            synchroProjectManagerNew.updateProjectMW(project);
         
            //EmailNotification#1 Recipients for Approval (Methodology Waiver)
            
            //String recp = pibMethApp.getEmail()+","+projectOwnerEmail;
            String recp = pibMethApp.getEmail();
            String adminEmail = SynchroPermHelper.getSystemAdminEmail();
            
            String adminName = SynchroPermHelper.getSystemAdminName();
            
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"send.for.approval.waiver.new.htmlBody","send.for.approval.waiver.new.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            email.getContext().put ("waiverInitiator",getUser().getName());
            
            email.getContext().put ("waiverType","Methodology");
            
            stageManager.sendNotificationNew(adminName, adminEmail, email);

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            emailNotBean.setEndmarketID(endMarketId);
            emailNotBean.setStageID(SynchroConstants.PS_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSubject("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.waiver.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
        
      
        //stageManager.sendNotification(getUser(),emailMessage);
       
      
       // return SUCCESS;
        return "methWaiver";
    }

    /**
     * This method will update the PIB Kantar Waiver related fields
     * @return
     */
    public String updateKantarWaiver(){

        User pibMethApp = null;

        String projectOwnerEmail="";
        String spiContactEmail="";
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/project-specs!input.jspa?projectID=" + projectID;

        try
        {
        	if(pibKantarMethodologyWaiver!=null && pibKantarMethodologyWaiver.getMethodologyApprover()!=null && pibKantarMethodologyWaiver.getMethodologyApprover() >0)
        	{
        		pibMethApp = userManager.getUser(pibKantarMethodologyWaiver.getMethodologyApprover());
        	}
            if(project.getProjectOwner()!=null)
            {
                //projectOwnerEmail = userManager.getUser(project.getProjectOwner()).getEmail();
                projectOwnerEmail = SynchroUtils.getUserEmail(project.getProjectOwner());
            }
            if(endMarketDetails.get(0).getSpiContact()!=null)
            {
                //spiContactEmail = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                spiContactEmail = SynchroUtils.getUserEmail(endMarketDetails.get(0).getSpiContact());
            }

        }
        catch(UserNotFoundException ue)
        {

        }
        LOG.info("Checking SPI CONTACT in KANTAR WAIVER -- " + spiContactEmail);
        LOG.info("Checking SPI CONTACT in KANTAR WAIVER USER ID -- " + endMarketDetails.get(0).getSpiContact());

      //Save Audit logs for change in Methodology Waiver related fields
        if(projectID!=null && endMarketDetails!=null && endMarketDetails.size()>0 && endMarketDetails.get(0).getEndMarketID()!=null)
        {
        	final PIBMethodologyWaiver pibKantarMethodologyWaiver_DB = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
            SynchroLogUtilsNew.PIBKantarWaiverSave(pibKantarMethodologyWaiver_DB, pibKantarMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId());
        }        
        
        
        if(kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Approve"))
        {
            this.pibManagerNew.approvePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);

            //EmailNotification#4 Recipients for Approval
            if(projectID!=null)
            {
                //String recp = projectOwnerEmail+","+spiContactEmail;
                
                Long waiverRequestor = pibKantarMethodologyWaiver.getCreationBy();
                String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
                
               // EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"pib.approve.kantar.waiver.htmlBody","pib.approve.kantar.waiver.subject");
                
                
                
                String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
                String adminName = SynchroPermHelper.getSystemAdminName();
                
                EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"approve.waiver.new.htmlBody","approve.waiver.new.subject");

                
                //email.getContext().put("projectId", projectID);

                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                email.getContext().put("projectName",project.getName());
                email.getContext().put ("stageUrl",stageUrl);
                email.getContext().put ("waiverApprover",getUser().getName());
                email.getContext().put ("waiverType","Agency");
                
                stageManager.sendNotificationNew(adminName, adminEmail, email);

                //Email Notification TimeStamp Storage
                EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                emailNotBean.setProjectID(projectID);
                emailNotBean.setEndmarketID(endMarketId);
                emailNotBean.setStageID(SynchroConstants.PS_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }

          //Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.kantar.approve"), project.getName(), 
            												project.getProjectID(), getUser().getID());
        }
        else if (kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Reject"))
        {
            this.pibManagerNew.rejectPIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);

            //EmailNotification#5 Recipients for Reject
            if(projectID!=null)
            {
                //String recp = projectOwnerEmail+","+spiContactEmail;
            	 Long waiverRequestor = pibKantarMethodologyWaiver.getCreationBy();
                 String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
                
                String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
                String adminName = SynchroPermHelper.getSystemAdminName();
                
                
                
               EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"reject.waiver.new.htmlBody","reject.waiver.new.subject");

               //email.getContext().put("projectId", projectID);
               email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
               email.getContext().put("projectName",project.getName());
               email.getContext().put ("stageUrl",stageUrl);
               
               email.getContext().put ("waiverRejector",getUser().getName());
               email.getContext().put ("waiverType","Agency");
               stageManager.sendNotificationNew(adminName, adminEmail, email);
                
                
                

                //Email Notification TimeStamp Storage
                EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                emailNotBean.setProjectID(projectID);
                emailNotBean.setEndmarketID(endMarketId);
                emailNotBean.setStageID(SynchroConstants.PS_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
          //Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.kantar.reject"), project.getName(), 
            												project.getProjectID(), getUser().getID());
        }
        else if (kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Send for Information"))
        {
        	
        }
        else if (kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Request more Information"))
        {
            //TODO: change the subject and message
            //EmailNotification#3 Recipients for Request more Information
          //  String recp = projectOwnerEmail+","+spiContactEmail;
            Long waiverRequestor = pibKantarMethodologyWaiver.getCreationBy();
            String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
            
            String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
            String adminName = SynchroPermHelper.getSystemAdminName();
            
            EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"request.more.information.waiver.new.htmlBody","request.more.information.waiver.new.subject");
            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            
            // email.getContext().put ("waiverInitiator",getUser().getName());
            try
            {
            	email.getContext().put ("waiverInitiator",userManager.getUser(waiverRequestor).getName());
            }
            catch(UserNotFoundException e)
            {
            	e.printStackTrace();
            	email.getContext().put ("waiverInitiator","");
            }
            
            email.getContext().put ("waiverType","Agency");
            stageManager.sendNotificationNew(adminName, adminEmail, email);

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            emailNotBean.setEndmarketID(endMarketId);
            emailNotBean.setStageID(SynchroConstants.PS_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("Notification | More information required on Kantar Agency Waiver");
            emailNotBean.setEmailSubject("Notification | More information required on Kantar Agency Waiver");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(waiverRequestorEmail);
            emailNotificationManager.saveDetails(emailNotBean);

            pibManagerNew.reqForInfoPIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
            //pibManager.updatePIBStatus(projectID, SynchroGlobal.StageStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal());
            
          //Request More Information Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.kantar.request.inf"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
        else if (kantarMethodologyWaiverAction!=null && kantarMethodologyWaiverAction.equals("Waiver Attachment"))
        {
        	pibKantarMethodologyWaiver.setCreationBy(getUser().getID());
            pibKantarMethodologyWaiver.setCreationDate(System.currentTimeMillis());

            pibKantarMethodologyWaiver.setModifiedBy(getUser().getID());
            pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
          
           	
           	if(pibKantarMethodologyWaiver!=null && pibKantarMethodologyWaiver.getIsApproved()!=null)
            {
            	pibKantarMethodologyWaiver.setIsApproved(pibKantarMethodologyWaiver.getIsApproved());
            }
            else
            {
            	pibKantarMethodologyWaiver.setIsApproved(null);
            }
            
            if(pibKantarMethodologyWaiver!=null && pibKantarMethodologyWaiver.getStatus()!=null)
            {
            	pibKantarMethodologyWaiver.setStatus(pibKantarMethodologyWaiver.getStatus());
            }
            else
            {
            	pibKantarMethodologyWaiver.setStatus(-1);
            }
           
            
           // The System Owner will be the Methodology Approver
            List<User> systemOwnerList = getSynchroUtils().getSynchroSystemOwnerUsers();
            if(systemOwnerList!=null && systemOwnerList.size()>0)
            {
            	pibKantarMethodologyWaiver.setMethodologyApprover(systemOwnerList.get(0).getID());
            }
            this.pibManagerNew.savePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
           
        }
        else
        {
            pibKantarMethodologyWaiver.setCreationBy(getUser().getID());
            pibKantarMethodologyWaiver.setCreationDate(System.currentTimeMillis());

            pibKantarMethodologyWaiver.setModifiedBy(getUser().getID());
            pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
            pibKantarMethodologyWaiver.setIsApproved(null);
            pibKantarMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
            
           // The System Owner will be the Methodology Approver
            List<User> systemOwnerList = getSynchroUtils().getSynchroSystemOwnerUsers();
            if(systemOwnerList!=null && systemOwnerList.size()>0)
            {
            	pibKantarMethodologyWaiver.setMethodologyApprover(systemOwnerList.get(0).getID());
            }
            this.pibManagerNew.savePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
           
           try
           {
        	   pibMethApp = userManager.getUser(pibKantarMethodologyWaiver.getMethodologyApprover());
           }
           catch(UserNotFoundException ue)
           {

           }
            //String recp = pibMethApp.getEmail()+","+projectOwnerEmail;
           String recp = pibMethApp.getEmail();
           
            
            
            String adminEmail = SynchroPermHelper.getSystemAdminEmail();
            
            String adminName = SynchroPermHelper.getSystemAdminName();
            
            EmailMessage email = stageManager.populateNotificationEmail(recp, null, null,"send.for.approval.waiver.new.htmlBody","send.for.approval.waiver.new.subject");

            //email.getContext().put("projectId", projectID);
            email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            email.getContext().put("projectName",project.getName());
            email.getContext().put ("stageUrl",stageUrl);
            email.getContext().put ("waiverInitiator",getUser().getName());
            email.getContext().put ("waiverType","Agency");
            
            stageManager.sendNotificationNew(adminName, adminEmail, email);
            

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            emailNotBean.setEndmarketID(endMarketId);
            emailNotBean.setStageID(SynchroConstants.PS_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSubject("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.kantar.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
        
       // return SUCCESS;
        return "agencyWaiver";
    }
    
    
    /**
     * This method will reset Proposal Legal Aproval Details for Admin User
     * @return
     */
    public String resetProposal(){
    	
    	proposalManagerNew.resetProposal(proposalInitiation);   
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
    	{
    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId());
    		for(AttachmentBean attachment : attachments)
    		{
    			try
    			{
    				proposalManagerNew.removeAttachment(attachment.getID());
    			}
    			catch(Exception e)
    	    	{
    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
    	    	}
    		}
    	}
    	
    	projectSpecsManagerNew.resetProjectSpecs(projectSpecsInitiation);
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId())!=null)
    	{
    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId());
    		for(AttachmentBean attachment : attachments)
    		{
    			try
    			{
    				projectSpecsManagerNew.removeAttachment(attachment.getID());
    			}
    			catch(Exception e)
    	    	{
    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
    	    	}
    		}
    	}
    	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    	
	    SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
   				SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.proposal.reset"), project.getName(), 
   						project.getProjectID(), getUser().getID());
           
	    return "proposal-details";
    }
    
    /**
     * This method will reset PIB Legal Approval Details for Admin User
     * @return
     */
    public String resetPIB(){
    	
    	proposalManagerNew.resetProposal(proposalInitiation);   
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
    	{
    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId());
    		for(AttachmentBean attachment : attachments)
    		{
    			try
    			{
    				proposalManagerNew.removeAttachment(attachment.getID());
    			}
    			catch(Exception e)
    	    	{
    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
    	    	}
    		}
    	}
    	
    	projectSpecsManagerNew.resetProjectSpecs(projectSpecsInitiation);
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId())!=null)
    	{
    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId());
    		for(AttachmentBean attachment : attachments)
    		{
    			try
    			{
    				projectSpecsManagerNew.removeAttachment(attachment.getID());
    			}
    			catch(Exception e)
    	    	{
    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
    	    	}
    		}
    	}
    	pibManagerNew.resetPIB(projectInitiation);
    	
    	// We need to remove all the details for PIB Reset
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId())!=null)
    	{
    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId());
    		for(AttachmentBean attachment : attachments)
    		{
    			try
    			{
    				pibManagerNew.removeAttachment(attachment.getID());
    			}
    			catch(Exception e)
    	    	{
    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
    	    	}
    		}
    	}
    	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal());
    	
    	SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
				SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.pib.reset"), project.getName(), 
						project.getProjectID(), getUser().getID());
    	return "pib-details";
    }
    
    /**
     * This method will update the PIT from the PIB screen
     * @return
     */
    public String updatePIT(){

        project.setModifiedBy(getUser().getID());
        project.setModifiedDate(System.currentTimeMillis());

        //https://www.svn.sourcen.com//issues/19285
        String projectName = getRequest().getParameter("projectName");
        if(projectName!=null)
        {
            this.project.setName(projectName);
        }
        
     /*   if(project.getProjectOwner()==null && getRequest().getParameter("projectOwnerOri")!=null)
        {
        	project.setProjectOwner(new Long(getRequest().getParameter("projectOwnerOri")));
        }
       */ 
        if(StringUtils.isNotBlank(getRequest().getParameter("startDate1"))) {
            this.project.setStartDate(DateUtils.parse(getRequest().getParameter("startDate1")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDate1"))) {
            this.project.setEndDate(DateUtils.parse(getRequest().getParameter("endDate1")));
        }
        
        // http://redmine.nvish.com/redmine/issues/341
        if(project.getBrandSpecificStudy()!=null && project.getBrandSpecificStudy()==1)
        {
        	project.setBrandSpecificStudyType(new Integer("-1"));
        }
        else if(project.getBrandSpecificStudy()!=null && project.getBrandSpecificStudy()==2)
        {
        	project.setBrand(new Long("-1"));
        }
        
        project.setHasNewSynchroSaved(true);
        synchroProjectManagerNew.updatePIT(project, null);
        
       //http://redmine.nvish.com/redmine/issues/261 and http://redmine.nvish.com/redmine/issues/260
        projectInitiation.setBrief(SynchroUtils.fixBulletPoint(getRequest().getParameter("brief")));
        projectInitiation.setBriefText(getRequest().getParameter("briefText"));
        
        // This is for Admin User, in case he updated the Legal Approval Status from PIT Window
        // http://redmine.nvish.com/redmine/issues/324
        if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusPitBrief")))
        {
        	projectInitiation.setLegalApprovalStatus(Integer.parseInt(getRequest().getParameter("legalApprovalStatusPitBrief")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("briefLegalApprover")))
        {
        	projectInitiation.setBriefLegalApprover(Long.parseLong(getRequest().getParameter("briefLegalApprover")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalDateBrief"))) {
            this.projectInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("legalApprovalDateBrief")));
        }
       
        projectInitiation.setModifiedBy(getUser().getID());
        projectInitiation.setModifiedDate(System.currentTimeMillis());
        
        // Setting the Brief Legal Approver for EU Offline cases
        projectInitiation.setBriefLegalApproverOffline(getRequest().getParameter("briefLegalApproverOffline"));
        
        this.pibManagerNew.updatePIBDetailsNew(projectInitiation);
        
        proposalInitiation.setProposal(SynchroUtils.fixBulletPoint(getRequest().getParameter("proposal")));
        proposalInitiation.setProposalText(getRequest().getParameter("proposalText"));
        
        proposalInitiation.setModifiedBy(getUser().getID());
        proposalInitiation.setModifiedDate(System.currentTimeMillis());
        
        if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusPitProposal")))
        {
        	proposalInitiation.setLegalApprovalStatus(Integer.parseInt(getRequest().getParameter("legalApprovalStatusPitProposal")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("proposalLegalApprover")))
        {
        	proposalInitiation.setProposalLegalApprover(Long.parseLong(getRequest().getParameter("proposalLegalApprover")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalDateProposal"))) {
            this.proposalInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("legalApprovalDateProposal")));
        }
        
        // Setting the Proposal Legal Approver for EU Offline cases
        proposalInitiation.setProposalLegalApproverOffline(getRequest().getParameter("proposalLegalApproverOffline"));
        
        this.proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
        
        
        synchroProjectManagerNew.deleteProjectCostDetails(projectID);
        saveProjectCostDetails();
       String initialCost = getRequest().getParameter("initialCost");
        
        EndMarketInvestmentDetail endMarketDetail = new EndMarketInvestmentDetail();
        
               
        // This is done as per http://redmine.nvish.com/redmine/issues/391
        // http://redmine.nvish.com/redmine/issues/499
        //if((SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner()) && project.getProjectType()==3)
        if((SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner()))
        {
        	saveEndMarketDetails();
        }
        
        saveFundingEndMarkets();
        
        updateReferenceEndMarkets();
        
        //Audit Logs : PIT
    //    SynchroLogUtils.PITSave(project_DB, project, endMarketDetail_DB, endMarketDetail);
        
      
      
        
        
        

    	projectSpecsInitiation.setModifiedBy(getUser().getID());
    	projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());
    	projectSpecsInitiation.setProjectID(projectID);
    	
   
    	if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
        {
        	projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
        	//Update Project Status to Report Summary Stage
        	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
        }
     //   else
       
      //  this.projectSpecsManagerNew.updateProjectSpecsDetailsNew(projectSpecsInitiation);
        
    	//Audit Logs : PIT
        SynchroLogUtilsNew.PITSaveNew(project_DB, project, projectInitiation_DB, projectInitiation, proposalInitiation_DB, proposalInitiation);
        
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.VIEWEDITPIT.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
        
        
        if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
        {
        	
        	return "report-summary";
        }
        

        return SUCCESS;
    }
    	private void updateReferenceEndMarkets()
    {
    	String[] referenceEndMarkets = getRequest().getParameterValues("referenceEndMarkets");
    	String[] referenceSynchroCodes = getRequest().getParameterValues("referenceSynchroCodes");
    	if(referenceEndMarkets!=null && referenceEndMarkets.length>0)
    	{
    		for(int i=0;i<referenceEndMarkets.length;i++)
   		 	{
    			try
    			{
	    			if(referenceSynchroCodes!=null && referenceSynchroCodes[i]!=null && StringUtils.isNotBlank(referenceSynchroCodes[i]))
	    			{
	    				EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), new Long(referenceEndMarkets[i]));
	    				endMarketInvestmentDetail.setReferenceSynchroCode(new Long(referenceSynchroCodes[i]));
	    				synchroProjectManagerNew.updateReferenceEndMarkets(endMarketInvestmentDetail);
	    			}
    			}
    			catch(Exception e)
    			{
    				 LOG.debug("Exception while updating reference Synchro Codes" + e.getMessage());
    			}
   		 	}
    	}
    }
  //Save/Update Funding End Market Details
    private String saveFundingEndMarkets() {
       
    	if(project.getEndMarkets()==null)
    	{
    		project.setEndMarkets(endMarketIds);
    	}
        if(project.getProjectID()!=null && project.getFundingMarkets() != null && project.getEndMarkets()!=null){
            
        	for(Long endMarketID : project.getEndMarkets()){
                if(endMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
	            	endMarketInvestmentDetail.setIsFundingMarket(false);
	                synchroProjectManagerNew.updateFundingEndMarkets(endMarketInvestmentDetail);
                }   
            }
        	for(Long fundingMarketID : project.getFundingMarkets()){
                if(fundingMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), fundingMarketID);
	            	endMarketInvestmentDetail.setIsFundingMarket(true);
	                synchroProjectManagerNew.updateFundingEndMarkets(endMarketInvestmentDetail);
	               
	             
                }   
            }
        }
        return SUCCESS;
    }
    
  //Save End Market Investment Details
    private String saveEndMarketDetails() {
        final Long userID = getUser().getID();
        // The Existing end market needs to be deleted for SAVE a Draft
        synchroProjectManagerNew.deleteEndMarketDetail(project.getProjectID());

        if(project.getProjectID()!=null && project.getEndMarkets() != null){
            for(Long endMarketID : project.getEndMarkets()){
                if(endMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
	                if(project.getInitialCost()!=null && project.getInitialCost().size()>0)
	                {
	                	endMarketInvestmentDetail.setInitialCost(SynchroUtils.formatDate(project.getInitialCost().get(0)));
	                }
	                if(project.getInitialCostCurrency()!=null && project.getInitialCostCurrency().size()>0)
	                {
	                    endMarketInvestmentDetail.setInitialCostCurrency(project.getInitialCostCurrency().get(0));
	                }
	                if(project.getSpiContact()!=null && project.getSpiContact().size()>0)
	                {
	                	endMarketInvestmentDetail.setSpiContact(project.getSpiContact().get(0));
	                }
	                endMarketInvestmentDetail.setCreationBy(userID);
	                endMarketInvestmentDetail.setModifiedBy(userID);
	                synchroProjectManagerNew.saveEndMarketDetail(endMarketInvestmentDetail);
	              //  synchroProjectManager.setAllEndMarketStatus(project.getProjectID(), SynchroGlobal.ProjectActivationStatus.OPEN.ordinal());
                }   
            }
        }
        return SUCCESS;
    }
    private void saveProjectCostDetails()
    {
      	 projectCostDetails = new ArrayList<ProjectCostDetailsBean>();
        	ProjectCostDetailsBean pcbean = new ProjectCostDetailsBean();
        	 /*String agency =  getRequest().getParameter("agency");
        	 String costComponent =  getRequest().getParameter("costComponent");
        	 String currency = getRequest().getParameter("currency");
        	 String agencyCost = getRequest().getParameter("agencyCost");
        	 
        	 
        	 if(agency!=null &&  !agency.equalsIgnoreCase("") && costComponent!=null && !costComponent.equalsIgnoreCase("") 
        			 && currency!=null && !currency.equalsIgnoreCase("") && agencyCost!=null && !agencyCost.equalsIgnoreCase(""))
        	 {
     	    	 
     	    	 pcbean.setProjectId(project.getProjectID());
     	         pcbean.setAgencyId(new Long(agency));
     	         pcbean.setCostComponent(new Integer(costComponent));
     	         pcbean.setCostCurrency(new Integer(currency));
     	         pcbean.setEstimatedCost(new BigDecimal(agencyCost.replaceAll(",","")));
     	         projectCostDetails.add(pcbean);
        	 }
             
             */
        	
        	//String[] hiddenAgencies = getRequest().getParameterValues("hiddenAgencies");
        	String hiddenAgencies = getRequest().getParameter("hiddenAgencies");
        	
        	 String[] agencies = getRequest().getParameterValues("agencies");
             String[] costComponents = getRequest().getParameterValues("costComponents");
             String[] currencies = getRequest().getParameterValues("currencies");
             String[] agencyCosts = getRequest().getParameterValues("agencyCosts");
             
            // This check is done as for Disabled Chosen Select Values, they are coming as null. So trying to fetch those null values through hidden field.
            // http://redmine.nvish.com/redmine/issues/311
             if(agencies==null)
             {
            	 if(hiddenAgencies!=null && StringUtils.isNotBlank(hiddenAgencies))
            	 {
            		 hiddenAgencies = hiddenAgencies.replaceFirst(",", "");
            		 if(hiddenAgencies.contains(","))
            		 {
            			 agencies = hiddenAgencies.split(",");
            		 }
            		 else
            		 {
            			 agencies = hiddenAgencies.split(",");
            		 }
            	 }
             }
             
        	 if(agencies!=null && agencies.length>0)
        	 {
        		// for(int i=0;i<agencies.length;i++)
        		 for(int i=(agencies.length-1);i>=0;i--)
        		 {
        			
        			if(StringUtils.isNotBlank(agencies[i]) && StringUtils.isNotBlank(costComponents[i]) && StringUtils.isNotBlank(currencies[i]) && StringUtils.isNotBlank(agencyCosts[i]))
     			{
     	   			 pcbean = new ProjectCostDetailsBean();
     	   	    	 pcbean.setProjectId(project.getProjectID());
     	   	         
     	   	    	try
     	   	    	{
     	   	    		pcbean.setAgencyId(new Long(agencies[i]));
     	   	    	}
     	   	    	catch(Exception e)
     	   	    	{
     	   	    		
     	   	    	}
     	   	         
     	   	         try
     	   	         {
     	   	        	 pcbean.setCostComponent(new Integer(costComponents[i]));
     	   	         }
     	   	         catch(Exception e)
     	   	         {
     	   	        	 
     	   	         }
     	   	         try
     	   	         {
     	   	        	 pcbean.setCostCurrency(new Integer(currencies[i]));
     	   	         }
     	   	         catch(Exception e)
     	   	         {
     	   	        	 
     	   	         }
     	   	         try
     	   	         {
     	   	        	 pcbean.setEstimatedCost(new BigDecimal(agencyCosts[i].replaceAll(",","")));
     	   	         }
     	   	         catch(Exception e)
     	   	         {
     	   	        	 
     	   	         }
     	   	         projectCostDetails.add(pcbean);
     			}
        		 }
        	 }
        	 if(projectCostDetails!=null && projectCostDetails.size()>0)
        	 {
        		 //synchroProjectManagerNew.saveProjectCoseDetails(projectCostDetails);
        		 
        		 if(StringUtils.isNotBlank(getRequest().getParameter("totalCostHidden"))) {
                  project.setTotalCost(new BigDecimal(getRequest().getParameter("totalCostHidden").replaceAll(",","")));

              }
        		 synchroProjectManagerNew.saveProjectCoseDetails(projectCostDetails, project.getTotalCost());
        	 }
        }
    
    
    /**
     * This method will Cancel the Project
     * @return
     */
    public String cancelProject(){
    	
    	//synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.CANCEL.ordinal());
    	
    	synchroProjectManagerNew.updateCancelProject(projectID, new Integer("1"));
    	
    	SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
 				SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.pib.cancel"), project.getName(), 
 						project.getProjectID(), getUser().getID());
    
    	//return SUCCESS;
    	
    	if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner() || SynchroPermHelper.isGlobalUserType())
    	{
    		redirectURL="/new-synchro/global-dashboard-open.jspa";
    	}
    	if(SynchroPermHelper.isRegionalUserType())
    	{
    		redirectURL="/new-synchro/regional-dashboard-open.jspa";
    	}
    	if(SynchroPermHelper.isEndMarketUserType())
    	{
    		redirectURL="/new-synchro/em-dashboard-open.jspa";
    	}
    	//return SUCCESS;
    	return "cancel";
    }
    
    /**
     * This method will Enable the cancelled Project
     * @return
     */
    public String enableProject(){
    	
      	
    	synchroProjectManagerNew.updateCancelProject(projectID, new Integer("0"));
    
    	SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
 				SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), getText("logger.project.pib.enable"), project.getName(), 
 						project.getProjectID(), getUser().getID());
    	
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
			this.projectSpecsManagerNew.updateProjectSpecsSendForApproval(projectID, null, 1);
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
			this.projectSpecsManagerNew.updateProjectSpecsSendForApproval(projectID, endMarketDetails.get(0).getEndMarketID(), null);
			this.projectSpecsManagerNew.updateRequestClarificationModification(projectID,endMarketDetails.get(0).getEndMarketID(), 1);
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
			this.projectSpecsManagerNew.approve(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
			this.projectSpecsManagerNew.updateProjectSpecsStatus(projectID,endMarketDetails.get(0).getEndMarketID(),SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
			
			// This has been done in case the Admin user Approves the PS once the Project is in Report Summary stage, then in that case
			// the Report Summary details should not be copied again.
			List<ReportSummaryInitiation> rsList = reportSummaryManagerNew.getReportSummaryInitiation(projectID, endMarketDetails.get(0).getEndMarketID());
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
				reportSummaryManagerNew.saveReportSummaryDetails(reportSummary);
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
    	
        return "moveToNextStage";
    }
    
	
	public String approveScreener(){
		this.projectSpecsManagerNew.approveScreener(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
        return SUCCESS;
    }
	public String rejectScreener(){
		this.projectSpecsManagerNew.rejectScreener(projectID,endMarketDetails.get(0).getEndMarketID());
        return SUCCESS;
    }
	public String approveQDG(){
		this.projectSpecsManagerNew.approveQDG(getUser(),projectID,endMarketDetails.get(0).getEndMarketID());
        return SUCCESS;
    }
	public String rejectQDG(){
		this.projectSpecsManagerNew.rejectQDG(projectID,endMarketDetails.get(0).getEndMarketID());
        return SUCCESS;
    }
	public String addAttachment() throws UnsupportedEncodingException {
       
        LOG.info("Checking File Name"+attachFileFileName);
        LOG.info("Checking File Content Type"+attachFileContentType);
        Map<String, Object> result = new HashMap<String, Object>();
        try
        {
        	/*projectSpecsManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID());
        	attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, endMarketId, fieldCategoryId));
        	*/
        	
        	// This has been done so that in case the end Market is changed for a project, then the attachments should remain intact.
        	projectSpecsManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID, new Long("-1"),fieldCategoryId,getUser().getID());
        	attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, new Long("-1"), fieldCategoryId));
        	 
        	//Add Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment" + "- " +attachFileFileName;
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), description, project.getName(), 
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
			projectSpecsManagerNew.removeAttachment(attachmentId);
			
			 //Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName +" deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.PROJECT_IN_PROGRESS.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID());
		}
		catch (Exception e) {
            LOG.error("Exception while removing attachment Id --"+ attachmentId);
        }
		 return SUCCESS;
    }
	
	
	
    
   

    public void setSynchroProjectManagerNew(final ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
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

	public ProjectSpecsManagerNew getProjectSpecsManagerNew() {
		return projectSpecsManagerNew;
	}

	public void setProjectSpecsManagerNew(ProjectSpecsManagerNew projectSpecsManagerNew) {
		this.projectSpecsManagerNew = projectSpecsManagerNew;
	}

	public ProjectSpecsInitiation getProjectSpecsInitiation() {
		return projectSpecsInitiation;
	}

	public void setProjectSpecsInitiation(
			ProjectSpecsInitiation projectSpecsInitiation) {
		this.projectSpecsInitiation = projectSpecsInitiation;
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

	public PIBManagerNew getPibManagerNew() {
		return pibManagerNew;
	}

	public void setPibManagerNew(PIBManagerNew pibManagerNew) {
		this.pibManagerNew = pibManagerNew;
	}

	public ReportSummaryManagerNew getReportSummaryManagerNew() {
		return reportSummaryManagerNew;
	}

	public void setReportSummaryManagerNew(ReportSummaryManagerNew reportSummaryManagerNew) {
		this.reportSummaryManagerNew = reportSummaryManagerNew;
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

	public PIBMethodologyWaiver getPibKantarMethodologyWaiver() {
		return pibKantarMethodologyWaiver;
	}

	public void setPibKantarMethodologyWaiver(
			PIBMethodologyWaiver pibKantarMethodologyWaiver) {
		this.pibKantarMethodologyWaiver = pibKantarMethodologyWaiver;
	}

	public List<ProjectCostDetailsBean> getProjectCostDetailsList() {
		return projectCostDetailsList;
	}

	public void setProjectCostDetailsList(
			List<ProjectCostDetailsBean> projectCostDetailsList) {
		this.projectCostDetailsList = projectCostDetailsList;
	}
	 public SynchroUtils getSynchroUtils() {
	        if(synchroUtils == null){
	            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
	        }
	        return synchroUtils;
	    }

	public ProjectInitiation getProjectInitiation() {
		return projectInitiation;
	}

	public void setProjectInitiation(ProjectInitiation projectInitiation) {
		this.projectInitiation = projectInitiation;
	}

	public ProposalInitiation getProposalInitiation() {
		return proposalInitiation;
	}

	public void setProposalInitiation(ProposalInitiation proposalInitiation) {
		this.proposalInitiation = proposalInitiation;
	}

	public ProposalManagerNew getProposalManagerNew() {
		return proposalManagerNew;
	}

	public void setProposalManagerNew(ProposalManagerNew proposalManagerNew) {
		this.proposalManagerNew = proposalManagerNew;
	}

	public String getKantarMethodologyWaiverAction() {
		return kantarMethodologyWaiverAction;
	}

	public void setKantarMethodologyWaiverAction(
			String kantarMethodologyWaiverAction) {
		this.kantarMethodologyWaiverAction = kantarMethodologyWaiverAction;
	}
	
		public Map<Long, List<User>> getEndMarketLegalApprovers() {
		return endMarketLegalApprovers;
	}

	public void setEndMarketLegalApprovers(
			Map<Long, List<User>> endMarketLegalApprovers) {
		this.endMarketLegalApprovers = endMarketLegalApprovers;
	}

	public List<ProjectCostDetailsBean> getProjectCostDetails() {
		return projectCostDetails;
	}

	public void setProjectCostDetails(
			List<ProjectCostDetailsBean> projectCostDetails) {
		this.projectCostDetails = projectCostDetails;
	}
	public List<Long> getEndMarketIds() {
		return endMarketIds;
	}

	public void setEndMarketIds(List<Long> endMarketIds) {
		this.endMarketIds = endMarketIds;
	}

	public Map<Integer, BigDecimal> getTotalCosts() {
		return totalCosts;
	}

	public void setTotalCosts(Map<Integer, BigDecimal> totalCosts) {
		this.totalCosts = totalCosts;
	}
}
