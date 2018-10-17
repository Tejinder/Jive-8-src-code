package com.grail.synchro.action;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;
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
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: tejinder
 * @since: 1.0
 * Action class for Project Close Fieldwork  Stage
 */
public class ProjectCloseActionFieldwork extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ProjectCloseActionFieldwork.class);
    //Spring Managers
    private ProjectEvaluationManagerNew projectEvaluationManagerNew;
    private ProjectManagerNew synchroProjectManagerNew;
    private ReportSummaryManagerNew reportSummaryManagerNew;
    
    //Form related fields
    private ProjectEvaluationInitiation projectEvaluationInitiation;
    private ProjectEvaluationInitiation projectEvaluationInitiation_DB;
    private Project project;
    private Project project_DB = null;
    private Long projectID;
    private boolean isSave;
    
    private boolean editStage;
	
   //private List<Long> endMarketIds;
    private List<EndMarketInvestmentDetail> endMarketDetails;
    private Long endMarketId;
    private Integer stageId;
    private StageManager stageManager;
   	
    List<ProjectCostDetailsBean> projectCostDetailsList;
    
	private PIBMethodologyWaiver pibKantarMethodologyWaiver;
	private ProjectInitiation projectInitiation;
	private ProposalInitiation proposalInitiation;
	private ProjectSpecsInitiation projectSpecsInitiation;
	private PIBMethodologyWaiver pibMethodologyWaiver;

	private ProposalManagerNew proposalManagerNew;
	private ProjectSpecsManagerNew projectSpecsManagerNew;
	private PIBManagerNew pibManagerNew;
	private List<ProjectEvaluationInitiation> initiationList;
	
	private List<ReportSummaryDetails> reportSummaryDetailsList;
	private Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();
	private Map<Integer, Map<Integer, List<Long>>> reportSummaryAttachments = new HashMap<Integer, Map<Integer,List<Long>>>();
	private Map<Long,Long> attachmentUser;
	private ReportSummaryInitiation reportSummaryInitiation;
	
	private String methodologyWaiverAction;
	private String kantarMethodologyWaiverAction;
	private EmailNotificationManager emailNotificationManager;
	
	private SynchroUtils synchroUtils;
	private String redirectURL;
	
	private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long attachmentFieldID;
    private String attachmentName;
    private Long fieldCategoryId;
    private List<ProjectCostDetailsBean> projectCostDetails;
    
    private List<Long> uniqueAgencyId = new ArrayList<Long>();
	
    public void prepare() throws Exception {
        final String id = getRequest().getParameter("projectID");
        

        if(id != null ) {

                try{
                    projectID = Long.parseLong(id);
                    
                } catch (NumberFormatException nfEx) {
                    LOG.error("Invalid ProjectID ");
                    throw nfEx;
                }
               
                project = this.synchroProjectManagerNew.get(projectID);
                
                project_DB = this.synchroProjectManagerNew.get(projectID);
                
               // endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
                endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
             	
               initiationList = this.projectEvaluationManagerNew.getProjectEvaluationInitiation(projectID);
             
                //Audit Log trails
                List<ProjectEvaluationInitiation> initiationList_DB = this.projectEvaluationManagerNew.getProjectEvaluationInitiation(projectID);
                if(initiationList_DB!=null && initiationList_DB.size() > 0)
                {
                	projectEvaluationInitiation_DB = initiationList_DB.get(0);
                }
                
                if( initiationList != null && initiationList.size() > 0) {
                    this.projectEvaluationInitiation = initiationList.get(0);
                
                }  else {
                    this.projectEvaluationInitiation = new ProjectEvaluationInitiation();
                    
                    isSave = true;
                }
                pibMethodologyWaiver = this.pibManagerNew.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                projectCostDetailsList = this.synchroProjectManagerNew.getProjectCostDetails(projectID);
                
                populateUniqueAgencyId();
                
                if(pibMethodologyWaiver==null)
                {
                	pibMethodologyWaiver = new PIBMethodologyWaiver();
    	        }
                if(pibKantarMethodologyWaiver==null)
                {
                	pibKantarMethodologyWaiver = new PIBMethodologyWaiver();
    	        }
                List<ProjectInitiation> pibInitiationList = this.pibManagerNew.getPIBDetailsNew(projectID);
                List<ProposalInitiation> proposalInitiationList = this.proposalManagerNew.getProposalInitiationNew(projectID);
                List<ProjectSpecsInitiation> psInitiationList = this.projectSpecsManagerNew.getProjectSpecsInitiationNew(projectID);
               
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
                if( psInitiationList != null && psInitiationList.size() > 0) {
                    this.projectSpecsInitiation = psInitiationList.get(0);
                 
                }  else {
                    this.projectSpecsInitiation = new ProjectSpecsInitiation();
                    
                   
                }
                
                List<ReportSummaryInitiation> rsInitiationList = this.reportSummaryManagerNew.getReportSummaryInitiation(projectID);
                if( rsInitiationList != null && rsInitiationList.size() > 0) {
                    this.reportSummaryInitiation = rsInitiationList.get(0);
                }
                
                reportSummaryDetailsList = this.reportSummaryManagerNew.getReportSummaryDetails(projectID);
                //attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
                
                attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(projectID, new Long("-1"));
                
                List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
                for(Integer i : attachmentMap.keySet())
                {
                	abList.addAll(attachmentMap.get(i));
                }
                attachmentUser = pibManagerNew.getAttachmentUser(abList);
                reportSummaryAttachments = this .reportSummaryManagerNew.getReportSummaryAttachmentDetails(projectID);
                stageId = SynchroGlobal.getProjectActivityTab().get("projectEvaluation");
    			
    		//	editStage=SynchroPermHelper.canEditStageDocument(ribDocument,projectID);
    			//editStage=true;
              //  editStage=SynchroPermHelper.canEditProjectEvaluation(projectID);
    			 
    			
    	
        }
      
        if(getRequest().getMethod() == "POST") {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.projectEvaluationInitiation);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Project Evaluation Initiation bean.");
                input();
            }
            
            // To map the Project Parameters
            binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());
            
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

    	if (SynchroPermHelper.hasProjectAccessNew1(projectID) ||  SynchroPermHelper.userTypeAccess(projectID)) {
    		//return INPUT;
    		redirectURL = ProjectStage.generateURLNew(project);
    		
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
        	
        	if(redirectURL.contains("project-close-fieldwork"))
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
    }

    public String execute(){
        return SUCCESS;
    }
    
    /**
     * This method will Cancel the Project
     * @return
     */
    public String cancelProject(){
    	
    	//synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.CANCEL.ordinal());
    	
    	synchroProjectManagerNew.updateCancelProject(projectID, new Integer("1"));
    	SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
 				SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.pib.cancel"), project.getName(), 
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
 				SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.pib.enable"), project.getName(), 
 						project.getProjectID(), getUser().getID());
    
    	return SUCCESS;
    }
    
    public String updateWaiver(){

        User pibMethApp = null;

        String projectOwnerEmail="";
        String spiContactEmail="";
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/project-close-fieldwork!input.jspa?projectID=" + projectID;

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
            

        }
        catch(UserNotFoundException ue)
        {

        }
        
      //Save Audit logs for change in Methodology Waiver related fields
        if(projectID!=null && endMarketDetails!=null && endMarketDetails.size()>0 && endMarketDetails.get(0).getEndMarketID()!=null)
        {
        	final PIBMethodologyWaiver pibMethodologyWaiver_DB = this.pibManagerNew.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
            SynchroLogUtilsNew.PIBWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId());
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
                emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
                
            }
            //Approve Audit logs
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.waiver.approve"), project.getName(), 
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
                emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
            
          //Reject Audit logs
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.waiver.reject"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("Notification | More information required on Methodology Waiver");
            emailNotBean.setEmailSubject("Notification | More information required on Methodology Waiver");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(waiverRequestorEmail);
            emailNotificationManager.saveDetails(emailNotBean);

            pibManagerNew.reqForInfoPIBMethodologyWaiver(pibMethodologyWaiver);
            //pibManager.updatePIBStatus(projectID, SynchroGlobal.StageStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal());
            
          //Request More Information Audit logs
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSubject("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.waiver.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
        
      
      
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
        String stageUrl = baseUrl+"/new-synchro/project-close-fieldwork!input.jspa?projectID=" + projectID;

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
            SynchroLogUtilsNew.PIBKantarWaiverSave(pibKantarMethodologyWaiver_DB, pibKantarMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId());
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
                emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }

          //Approve Audit logs
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.kantar.approve"), project.getName(), 
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
                emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
          //Reject Audit logs
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.kantar.reject"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("Notification | More information required on Kantar Agency Waiver");
            emailNotBean.setEmailSubject("Notification | More information required on Kantar Agency Waiver");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(waiverRequestorEmail);
            emailNotificationManager.saveDetails(emailNotBean);

            pibManagerNew.reqForInfoPIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
            //pibManager.updatePIBStatus(projectID, SynchroGlobal.StageStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal());
            
          //Request More Information Audit logs
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.kantar.request.inf"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PROJECT_CLOSE_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSubject("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), getText("logger.project.kantar.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
       
       // return SUCCESS;
        return "agencyWaiver";
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
        
        synchroProjectManagerNew.deleteProjectCostDetails(projectID);
        saveProjectCostDetails();
        
        
        proposalInitiation.setProposal(SynchroUtils.fixBulletPoint(getRequest().getParameter("proposal")));
        proposalInitiation.setProposalText(getRequest().getParameter("proposalText"));
        
        proposalInitiation.setModifiedBy(getUser().getID());
        proposalInitiation.setModifiedDate(System.currentTimeMillis());
        this.proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
        
        updateProjectEvaluation();
        
       String initialCost = getRequest().getParameter("initialCost");
        
        EndMarketInvestmentDetail endMarketDetail = new EndMarketInvestmentDetail();
       
        
        // This is done as per http://redmine.nvish.com/redmine/issues/391
        // http://redmine.nvish.com/redmine/issues/499
        if((SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner()))
        {
        	saveEndMarketDetails();
        }
    
    
        //Audit Logs : PIT
        SynchroLogUtilsNew.PITSaveNew(project_DB, project, null, null, null, null);
        
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.VIEWEDITPIT.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
        

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
        	 String[] agencies = getRequest().getParameterValues("agencies");
             String[] costComponents = getRequest().getParameterValues("costComponents");
             String[] currencies = getRequest().getParameterValues("currencies");
             String[] agencyCosts = getRequest().getParameterValues("agencyCosts");
             
             String hiddenAgencies = getRequest().getParameter("hiddenAgencies");
             
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
        		 //for(int i=0;i<agencies.length;i++)
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
    
    public String addAttachment() throws UnsupportedEncodingException {

        LOG.info("Checking File Name"+attachFileFileName);
        LOG.info("Checking File Content Type"+attachFileContentType);
        Map<String, Object> result = new HashMap<String, Object>();
        try
        {        	
           /* proposalManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID(), new Long("-1"));
            attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, endMarketId, fieldCategoryId));*/
        	
        	 proposalManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,new Long("-1"),fieldCategoryId,getUser().getID(), new Long("-1"));
             attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, new Long("-1"), fieldCategoryId));
            
          //Add Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment" + "- " +attachFileFileName;
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), description, project.getName(), 
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
        try
        {
            pibManagerNew.removeAttachment(attachmentId);
            
          //Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName + " deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.PROJECT_CLOSE.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID());
        }
        catch (Exception e) {
            LOG.error("Exception while removing attachment Id --"+ attachmentId);
        }
        return SUCCESS;
      
    }
    
  //This method is for getting unique Agency Id to be displayed on Project Evaluation screen as per http://redmine.nvish.com/redmine/issues/420 
    public void populateUniqueAgencyId()
    {
    	for(ProjectCostDetailsBean pcb:projectCostDetailsList)
    	{
    		if(!uniqueAgencyId.contains(pcb.getAgencyId()))
    		{
    			uniqueAgencyId.add(pcb.getAgencyId());
    		}
    	}
    }
    
    private void updateProjectEvaluation()
    {

    	this.projectEvaluationManagerNew.deleteProjectEvaluationInitiation(projectID);
    	projectEvaluationInitiation.setCreationBy(getUser().getID());
		projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
        
		projectEvaluationInitiation.setModifiedBy(getUser().getID());
		projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
        
		
	 	 String agencyId =  getRequest().getParameter("project-eval-agency");
	 	 String agencyRating =  getRequest().getParameter("rating");
	 	 String agencyComment = getRequest().getParameter("comment");
	 	 if(StringUtils.isNotBlank(agencyId))
	 	 {
	 		projectEvaluationInitiation.setAgencyId(new Long(agencyId));
	 	 }
	 	 if(StringUtils.isNotBlank(agencyRating))
   	 	 {
   	 		projectEvaluationInitiation.setAgencyRating(new Integer(agencyRating));
   	 	 }
    	 projectEvaluationInitiation.setAgencyComments(agencyComment);	 
    	 projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());         	 
		
         this.projectEvaluationManagerNew.saveProjectEvaluationDetails(projectEvaluationInitiation);
         
         String[] agencies =  getRequest().getParameterValues("project-eval-agencies");
    	 String[] agencyRatings =  getRequest().getParameterValues("ratings");
    	 String[] agencyComments = getRequest().getParameterValues("comments");
    	 if(agencies!=null && agencies.length>0)
    	 {
    		 for(int i=0;i<agencies.length;i++)
    		 {
        		 projectEvaluationInitiation = new ProjectEvaluationInitiation();
        		 projectEvaluationInitiation.setProjectID(projectID);
        		 projectEvaluationInitiation.setCreationBy(getUser().getID());
         		 projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
                projectEvaluationInitiation.setModifiedBy(getUser().getID());
         		projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
         		if(StringUtils.isNotBlank(agencies[i]))
     			{
         			projectEvaluationInitiation.setAgencyId(new Long(agencies[i]));
     			}
         		if(agencyRatings!=null && agencyRatings[i]!=null)
         		{
         			if(StringUtils.isNotBlank(agencyRatings[i]))
         			{
         				projectEvaluationInitiation.setAgencyRating(new Integer(agencyRatings[i]));
         			}
         			
         		}
         		projectEvaluationInitiation.setAgencyComments(agencyComments[i]);	 
	        	 projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());         	 
	        	 this.projectEvaluationManagerNew.saveProjectEvaluationDetails(projectEvaluationInitiation);
    		 }
         		
    	 }
    
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

	public ProjectEvaluationManagerNew getProjectEvaluationManagerNew() {
		return projectEvaluationManagerNew;
	}

	public void setProjectEvaluationManagerNew(
			ProjectEvaluationManagerNew projectEvaluationManagerNew) {
		this.projectEvaluationManagerNew = projectEvaluationManagerNew;
	}

	public ProjectEvaluationInitiation getProjectEvaluationInitiation() {
		return projectEvaluationInitiation;
	}

	public void setProjectEvaluationInitiation(
			ProjectEvaluationInitiation projectEvaluationInitiation) {
		this.projectEvaluationInitiation = projectEvaluationInitiation;
	}

	public Integer getStageId() {
		return stageId;
	}

	public void setStageId(Integer stageId) {
		this.stageId = stageId;
	}

	public ReportSummaryManagerNew getReportSummaryManagerNew() {
		return reportSummaryManagerNew;
	}

	public void setReportSummaryManagerNew(ReportSummaryManagerNew reportSummaryManagerNew) {
		this.reportSummaryManagerNew = reportSummaryManagerNew;
	}

	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

	public List<ProjectCostDetailsBean> getProjectCostDetailsList() {
		return projectCostDetailsList;
	}

	public void setProjectCostDetailsList(
			List<ProjectCostDetailsBean> projectCostDetailsList) {
		this.projectCostDetailsList = projectCostDetailsList;
	}

	public PIBMethodologyWaiver getPibKantarMethodologyWaiver() {
		return pibKantarMethodologyWaiver;
	}

	public void setPibKantarMethodologyWaiver(
			PIBMethodologyWaiver pibKantarMethodologyWaiver) {
		this.pibKantarMethodologyWaiver = pibKantarMethodologyWaiver;
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

	public ProjectSpecsInitiation getProjectSpecsInitiation() {
		return projectSpecsInitiation;
	}

	public void setProjectSpecsInitiation(
			ProjectSpecsInitiation projectSpecsInitiation) {
		this.projectSpecsInitiation = projectSpecsInitiation;
	}

	public PIBMethodologyWaiver getPibMethodologyWaiver() {
		return pibMethodologyWaiver;
	}

	public void setPibMethodologyWaiver(PIBMethodologyWaiver pibMethodologyWaiver) {
		this.pibMethodologyWaiver = pibMethodologyWaiver;
	}

	public ProposalManagerNew getProposalManagerNew() {
		return proposalManagerNew;
	}

	public void setProposalManagerNew(ProposalManagerNew proposalManagerNew) {
		this.proposalManagerNew = proposalManagerNew;
	}

	public ProjectSpecsManagerNew getProjectSpecsManagerNew() {
		return projectSpecsManagerNew;
	}

	public void setProjectSpecsManagerNew(ProjectSpecsManagerNew projectSpecsManagerNew) {
		this.projectSpecsManagerNew = projectSpecsManagerNew;
	}

	public PIBManagerNew getPibManagerNew() {
		return pibManagerNew;
	}

	public void setPibManagerNew(PIBManagerNew pibManagerNew) {
		this.pibManagerNew = pibManagerNew;
	}

	public List<ProjectEvaluationInitiation> getInitiationList() {
		return initiationList;
	}

	public void setInitiationList(List<ProjectEvaluationInitiation> initiationList) {
		this.initiationList = initiationList;
	}

	public List<ReportSummaryDetails> getReportSummaryDetailsList() {
		return reportSummaryDetailsList;
	}

	public void setReportSummaryDetailsList(
			List<ReportSummaryDetails> reportSummaryDetailsList) {
		this.reportSummaryDetailsList = reportSummaryDetailsList;
	}

	public Map<Integer, List<AttachmentBean>> getAttachmentMap() {
		return attachmentMap;
	}

	public void setAttachmentMap(Map<Integer, List<AttachmentBean>> attachmentMap) {
		this.attachmentMap = attachmentMap;
	}

	public Map<Integer, Map<Integer, List<Long>>> getReportSummaryAttachments() {
		return reportSummaryAttachments;
	}

	public void setReportSummaryAttachments(
			Map<Integer, Map<Integer, List<Long>>> reportSummaryAttachments) {
		this.reportSummaryAttachments = reportSummaryAttachments;
	}

	public Map<Long, Long> getAttachmentUser() {
		return attachmentUser;
	}

	public void setAttachmentUser(Map<Long, Long> attachmentUser) {
		this.attachmentUser = attachmentUser;
	}

	public ReportSummaryInitiation getReportSummaryInitiation() {
		return reportSummaryInitiation;
	}

	public void setReportSummaryInitiation(
			ReportSummaryInitiation reportSummaryInitiation) {
		this.reportSummaryInitiation = reportSummaryInitiation;
	}
	public String getMethodologyWaiverAction() {
		return methodologyWaiverAction;
	}

	public void setMethodologyWaiverAction(String methodologyWaiverAction) {
		this.methodologyWaiverAction = methodologyWaiverAction;
	}

	public String getKantarMethodologyWaiverAction() {
		return kantarMethodologyWaiverAction;
	}

	public void setKantarMethodologyWaiverAction(
			String kantarMethodologyWaiverAction) {
		this.kantarMethodologyWaiverAction = kantarMethodologyWaiverAction;
	}

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}
	public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public ProjectEvaluationInitiation getProjectEvaluationInitiation_DB() {
		return projectEvaluationInitiation_DB;
	}

	public void setProjectEvaluationInitiation_DB(
			ProjectEvaluationInitiation projectEvaluationInitiation_DB) {
		this.projectEvaluationInitiation_DB = projectEvaluationInitiation_DB;
	}

	public Project getProject_DB() {
		return project_DB;
	}

	public void setProject_DB(Project project_DB) {
		this.project_DB = project_DB;
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

	public Long getAttachmentFieldID() {
		return attachmentFieldID;
	}

	public void setAttachmentFieldID(Long attachmentFieldID) {
		this.attachmentFieldID = attachmentFieldID;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public Long getFieldCategoryId() {
		return fieldCategoryId;
	}

	public void setFieldCategoryId(Long fieldCategoryId) {
		this.fieldCategoryId = fieldCategoryId;
	}

	public List<ProjectCostDetailsBean> getProjectCostDetails() {
		return projectCostDetails;
	}

	public void setProjectCostDetails(
			List<ProjectCostDetailsBean> projectCostDetails) {
		this.projectCostDetails = projectCostDetails;
	}

	public List<Long> getUniqueAgencyId() {
		return uniqueAgencyId;
	}

	public void setUniqueAgencyId(List<Long> uniqueAgencyId) {
		this.uniqueAgencyId = uniqueAgencyId;
	}

	

}
