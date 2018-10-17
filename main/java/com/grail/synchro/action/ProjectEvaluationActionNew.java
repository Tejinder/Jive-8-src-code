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
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: tejinder
 * @since: 1.0
 * Action class for Project Evaluation Stage
 */
public class ProjectEvaluationActionNew extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ProjectEvaluationAction.class);
    //Spring Managers
    private ProjectEvaluationManagerNew projectEvaluationManagerNew;
    private ProjectManagerNew synchroProjectManagerNew;
    private ReportSummaryManagerNew reportSummaryManagerNew;
    
    //Form related fields
    private ProjectEvaluationInitiation projectEvaluationInitiation;
    private ProjectEvaluationInitiation projectEvaluationInitiation_DB;
    private Project project;
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
	
	private String methodologyWaiverAction;
	private String kantarMethodologyWaiverAction;
	private EmailNotificationManager emailNotificationManager;
	
	private SynchroUtils synchroUtils;
	private ReportSummaryInitiation reportSummaryInitiation;
	
	private String redirectURL;
	private List<Long> endMarketIds;
	
	private List<Long> uniqueAgencyId = new ArrayList<Long>();
	
	private ProjectInitiation projectInitiation_DB;
	private ProposalInitiation proposalInitiation_DB;
	private Project project_DB;
	
	Map<Long, List<User>> endMarketLegalApprovers;
	private List<ProjectCostDetailsBean> projectCostDetails;
	
	private AttachmentHelper attachmentHelper;
    private Long attachmentFieldID;
    private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long fieldCategoryId;
    private String attachmentName;
	
    private String repTypeId;
    private String reportOrderId;
    
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
                endMarketIds = this.synchroProjectManagerNew.getEndMarketIDs(projectID);
             	
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
                
                List<ProposalInitiation> proposalInitiationList_DB = this.proposalManagerNew.getProposalInitiationNew(projectID);
            	if( proposalInitiationList_DB != null && proposalInitiationList_DB.size() > 0) {
            		proposalInitiation_DB = proposalInitiationList_DB.get(0);            		
            	}
            	
            	List<ProjectInitiation> projectInitiationList_DB = this.pibManagerNew.getPIBDetailsNew(projectID);
            	if( projectInitiationList_DB != null && projectInitiationList_DB.size() > 0) {
            		projectInitiation_DB = projectInitiationList_DB.get(0);            		
            	}
            	
                pibMethodologyWaiver = this.pibManagerNew.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                projectCostDetailsList = this.synchroProjectManagerNew.getProjectCostDetails(projectID);
                
                endMarketLegalApprovers = getSynchroUtils().getLegalApprovers();
                
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
        	
        	if(redirectURL.contains("project-eval"))
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

        LOG.info("Save the Project Evaluation Details ...."+projectEvaluationInitiation);

      //  if( projectInitiation != null && ribDocument != null){
        if( projectEvaluationInitiation != null){
        	projectEvaluationInitiation.setProjectID(projectID);
        	if(isSave) {
        		projectEvaluationInitiation.setCreationBy(getUser().getID());
        		projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
                
        		projectEvaluationInitiation.setModifiedBy(getUser().getID());
        		projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
                
        		
        	 	 String agencyId =  getRequest().getParameter("agency");
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
                 
                 String[] agencies =  getRequest().getParameterValues("agencies");
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
		         		if(agencyRatings!=null && agencyRatings[i]!=null && StringUtils.isNotBlank(agencyRatings[i]))
		         		{
		         			projectEvaluationInitiation.setAgencyRating(new Integer(agencyRatings[i]));
		         		}
		         		projectEvaluationInitiation.setAgencyComments(agencyComments[i]);	 
			        	 projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());         	 
			        	 this.projectEvaluationManagerNew.saveProjectEvaluationDetails(projectEvaluationInitiation);
            		 }
		         		
            	 }

               
            }
            else {
            	this.projectEvaluationManagerNew.deleteProjectEvaluationInitiation(projectID);
            	projectEvaluationInitiation.setCreationBy(getUser().getID());
        		projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
                
        		projectEvaluationInitiation.setModifiedBy(getUser().getID());
        		projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
                
        		
        	 	 String agencyId =  getRequest().getParameter("agency");
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
                 
                 String[] agencies =  getRequest().getParameterValues("agencies");
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
        	// https://www.svn.sourcen.com/issues/18858
        	//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-eval!input.jspa?projectID=" + projectID;
        	/*String baseUrl = URLUtils.getBaseURL(request);
        	String stageUrl = baseUrl+"/synchro/project-eval!input.jspa?projectID=" + projectID;
        	if(SynchroPermHelper.isAwardedExternalAgencyUser(projectID, endMarketDetails.get(0).getEndMarketID()))
        	{
        		String stakeholdersRoles = SynchroConstants.PROJECT_OWNER_ROLE;
                String stakeholders = stageManager.getNotificationRecipients(stakeholdersRoles, projectID, endMarketDetails.get(0).getEndMarketID());
        		EmailMessage email = stageManager.populateNotificationEmail(stakeholders, null, null,"projectEvaluation.notifyStakeholders.htmlBody","projectEvaluation.notifyStakeholders.subject");
				
				//email.getContext().put("projectId", projectID);
        		email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
				email.getContext().put("projectName",project.getName());
				email.getContext().put ("stageUrl",stageUrl);
				stageManager.sendNotification(getUser(),email);

        	}
        	else
        	{
        		//String externalAgencyRoles = SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME;
        		//https://www.svn.sourcen.com/issues/18947
        		String externalAgencyRoles = SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE;
                String externalAgency = stageManager.getNotificationRecipients(externalAgencyRoles, projectID, endMarketDetails.get(0).getEndMarketID());
        		if(externalAgency!=null && !externalAgency.equalsIgnoreCase(""))
        		{
	                EmailMessage email = stageManager.populateNotificationEmail(externalAgency, null, null,"projectEvaluation.notifyAgency.htmlBody","projectEvaluation.notifyAgency.subject");
					
					if(email!=null)
					{
		        		//email.getContext().put("projectId", projectID);
						email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
						email.getContext().put("projectName",project.getName());
						email.getContext().put ("stageUrl",stageUrl);
						stageManager.sendNotification(getUser(),email);
					}
        		}
        	}*/
            //Audit logs for field checks
        	SynchroLogUtilsNew.EvaluationSaveNew(project, projectEvaluationInitiation, projectEvaluationInitiation_DB);
        	
        	//Audit Logs: Project Evaluation SAVE
            String i18Text = getText("logger.project.eval.saved.text");
            SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
    								SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), i18Text, project.getName(), 
    										project.getProjectID(), getUser().getID());	
            
        } else {
            LOG.error("Project Evaluation Initiation was null  ");
            addActionError("Project Evaluation Initiation was null.");
        }
        
        if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
        {
        	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.CLOSE.ordinal());
        	return "project-close";
        }
        
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
 				SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.pib.cancel"), project.getName(), 
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
 				SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.pib.enable"), project.getName(), 
 						project.getProjectID(), getUser().getID());
    
    	return SUCCESS;
    }

    /**
     * This method will reset Report Summary For Admin User
     * @return
     */
    public String resetReportSummary(){
    	
    	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
    	SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
				SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.projectevaluataion.reset"), project.getName(), 
						project.getProjectID(), getUser().getID());
    	return "report-summary";
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
    	
    	resetRS();
    	resetProjectEval();;
    	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    	
	    SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
   				SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.proposal.reset"), project.getName(), 
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
    	
    	resetRS();
    	resetProjectEval();
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
				SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.pib.reset"), project.getName(), 
						project.getProjectID(), getUser().getID());
    	return "pib-details";
    }
    
    public void resetRS()
	 {
		//This will wipe off the Report Summary Details  
    	this.reportSummaryManagerNew.deleteReportSummaryDetailsNew(projectID);
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	    	{
	    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	    		for(AttachmentBean attachment : attachments)
	    		{
	    			try
	    			{
	    				reportSummaryManagerNew.removeAttachment(attachment.getID());
	    			}
	    			catch(Exception e)
	    	    	{
	    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
	    	    	}
	    		}
	    	}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TOP_LINE_REPORT.getId())!=null)
	    	{
	    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TOP_LINE_REPORT.getId());
	    		for(AttachmentBean attachment : attachments)
	    		{
	    			try
	    			{
	    				reportSummaryManagerNew.removeAttachment(attachment.getID());
	    			}
	    			catch(Exception e)
	    	    	{
	    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
	    	    	}
	    		}
	    	}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.EXECUTIVE_PRESENTATION_REPORT.getId())!=null)
	    	{
	    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.EXECUTIVE_PRESENTATION_REPORT.getId());
	    		for(AttachmentBean attachment : attachments)
	    		{
	    			try
	    			{
	    				reportSummaryManagerNew.removeAttachment(attachment.getID());
	    			}
	    			catch(Exception e)
	    	    	{
	    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
	    	    	}
	    		}
	    	}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId())!=null)
	    	{
	    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId());
	    		for(AttachmentBean attachment : attachments)
	    		{
	    			try
	    			{
	    				reportSummaryManagerNew.removeAttachment(attachment.getID());
	    			}
	    			catch(Exception e)
	    	    	{
	    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
	    	    	}
	    		}
	    	}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId())!=null)
	    	{
	    		List<AttachmentBean> attachments =  attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId());
	    		for(AttachmentBean attachment : attachments)
	    		{
	    			try
	    			{
	    				reportSummaryManagerNew.removeAttachment(attachment.getID());
	    			}
	    			catch(Exception e)
	    	    	{
	    	    		 LOG.error("Exception while removing attachment Id --"+attachment.getID());
	    	    	}
	    		}
	    	}
	 }
    
    public void resetProjectEval()
    {
    	projectEvaluationManagerNew.deleteProjectEvaluationInitiation(projectID);
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
    public String updateWaiver(){

        User pibMethApp = null;

        String projectOwnerEmail="";
        String spiContactEmail="";
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/project-eval!input.jspa?projectID=" + projectID;

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
            SynchroLogUtils.PIBWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId());
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
                emailNotBean.setStageID(SynchroConstants.PE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
                
            }
            //Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.waiver.approve"), project.getName(), 
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
                emailNotBean.setStageID(SynchroConstants.PE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
            
          //Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.waiver.reject"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PE_STAGE);
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
					SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PE_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSubject("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.waiver.send.approve"), project.getName(), 
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
        String stageUrl = baseUrl+"/new-synchro/project-eval!input.jspa?projectID=" + projectID;

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
            SynchroLogUtilsNew.PIBKantarWaiverSave(pibKantarMethodologyWaiver_DB, pibKantarMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId());
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
                emailNotBean.setStageID(SynchroConstants.PE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }

          //Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.kantar.approve"), project.getName(), 
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
                emailNotBean.setStageID(SynchroConstants.PE_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
          //Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.kantar.reject"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PE_STAGE);
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
					SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.kantar.request.inf"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PE_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSubject("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), getText("logger.project.kantar.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
       
       // return SUCCESS;
        return "agencyWaiver";
    }
    
    
    /**
     * This method will update the PIT from the RS screen
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
        
        if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner())
        {
        	synchroProjectManagerNew.deleteProjectCostDetails(projectID);
        	saveProjectCostDetails();
        }
        
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
    	projectSpecsInitiation.setDocumentation(SynchroUtils.fixBulletPoint(getRequest().getParameter("documentation")));
    	projectSpecsInitiation.setDocumentationText(getRequest().getParameter("documentationText"));
    	projectSpecsInitiation.setProjectID(projectID);
    	    
        this.projectSpecsManagerNew.updateProjectSpecsDetailsNew(projectSpecsInitiation);
        
        // This is done as some attachments are directly added through addAttachment Method
        if((SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner()))
        {
        	this.reportSummaryManagerNew.deleteReportSummaryDetailsNew(projectID);
        	 saveReportSummaryDetails();
        }
       
        
    	//Audit Logs : PIT
        SynchroLogUtilsNew.PITSaveNew(project_DB, project, projectInitiation_DB, projectInitiation, proposalInitiation_DB, proposalInitiation);
        
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
    
    
    
    private void saveReportSummaryDetails()
    {
    	List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
         
    	 String reportType =  getRequest().getParameter("reportType");
    	 String reportTypeLegalApprover =  getRequest().getParameter("reportTypeLegalApprover");
    	 String irisSummaryLegalApprover = getRequest().getParameter("irisSummaryLegalApprover");
    	 String tpdSummaryLegalApprover = getRequest().getParameter("tpdSummaryLegalApprover");
    	 String tpdSummaryDate = getRequest().getParameter("tpdSummaryDate"); 
    	 String reportTypeAttachment = getRequest().getParameter("reportTypeAttachment"); 
    	 
    	 String irisAttachment = getRequest().getParameter("irisAttachment");
    	 String tpdAttachment = getRequest().getParameter("tpdAttachment");
    	 
    	 
    	 String[] reportTypes =  getRequest().getParameterValues("reportTypes");
    	 String[] reportTypeLegalApprovers =  getRequest().getParameterValues("reportTypeLegalApprovers");
    	 String[] irisSummaryLegalApprovers = getRequest().getParameterValues("irisSummaryLegalApprovers");
    	 String[] tpdSummaryLegalApprovers = getRequest().getParameterValues("tpdSummaryLegalApprovers");
    	 String[] tpdSummaryDates = getRequest().getParameterValues("tpdSummaryDates");
    	 String[] reportTypeAttachments = getRequest().getParameterValues("reportTypeAttachments");
    	 
    	 String[] irisAttachments = getRequest().getParameterValues("irisAttachments");
    	 String[] tpdAttachments = getRequest().getParameterValues("tpdAttachments");
    	 
    	 ReportSummaryDetails rsbean = new ReportSummaryDetails();
    	 
    	 int reportOrderCounter = 1;
    	 if(StringUtils.isNotBlank(reportType))
    	 {
    		 rsbean.setProjectID(projectID);
    		 if(reportType.equalsIgnoreCase("1"))
    		 {
    			 rsbean.setReportType(SynchroGlobal.ReportType.FULL_REPORT.getId());
    			/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
    			 {
    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
    				 List<Long> attachmentIds = new ArrayList<Long>();
    				 for(AttachmentBean att: attachments)
    				 {
    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null)
    					 {
    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId()).get(reportOrderCounter).contains(att.getID()))
    						 {
    							 attachmentIds.add(new Long(att.getID()));
    						 }
    					 }
    				 }
    				 rsbean.setAttachmentId(attachmentIds);
    			 }*/
    			 if(StringUtils.isNotBlank(reportTypeAttachment))
    			 {
    				 List<Long> attachmentIds = new ArrayList<Long>();
    				 attachmentIds = getAttachmentIDs(reportTypeAttachment);
    				 rsbean.setAttachmentId(attachmentIds);
    			 }
    		 }
    		 else if(reportType.equalsIgnoreCase("2"))
    		 {
    			 rsbean.setReportType(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId());
    			/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
    			 {
    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
    				 List<Long> attachmentIds = new ArrayList<Long>();
    				 for(AttachmentBean att: attachments)
    				 {
    					 //attachmentIds.add(new Long(att.getID()));
    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null)
    					 {
    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()).get(reportOrderCounter).contains(att.getID()))
    						 {
    							 attachmentIds.add(new Long(att.getID()));
    						 }
    					 }
    				 }
    				 rsbean.setAttachmentId(attachmentIds);
    			 }*/
    			 if(StringUtils.isNotBlank(reportTypeAttachment))
    			 {
    				 List<Long> attachmentIds = new ArrayList<Long>();
    				 attachmentIds = getAttachmentIDs(reportTypeAttachment);
    				 rsbean.setAttachmentId(attachmentIds);
    			 }
    		 }
    		 else if(reportType.equalsIgnoreCase("3"))
    		 {
    			 rsbean.setReportType(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId());
    			/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
    			 {
    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
    				 List<Long> attachmentIds = new ArrayList<Long>();
    				 for(AttachmentBean att: attachments)
    				 {
    					// attachmentIds.add(new Long(att.getID()));
    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null)
    					 {
    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId()).get(reportOrderCounter).contains(att.getID()))
    						 {
    							 attachmentIds.add(new Long(att.getID()));
    						 }
    					 }
    				 }
    				 rsbean.setAttachmentId(attachmentIds);
    			 }*/
    			 if(StringUtils.isNotBlank(reportTypeAttachment))
    			 {
    				 List<Long> attachmentIds = new ArrayList<Long>();
    				 attachmentIds = getAttachmentIDs(reportTypeAttachment);
    				 rsbean.setAttachmentId(attachmentIds);
    			 }
    		 }
    		 rsbean.setLegalApprover(reportTypeLegalApprover);
    		 rsbean.setReportOrderId(reportOrderCounter);
    		 reportOrderCounter++;
    		 
    		 reportSummaryDetailsList.add(rsbean);
    	 } 
    	 // Add a blank Report Type Report
    	 else
    	 {
    		 rsbean = new ReportSummaryDetails();
    		 rsbean.setProjectID(projectID);
    		// rsbean.setReportType(SynchroGlobal.ReportType.FULL_REPORT.getId());
    		 rsbean.setReportType(SynchroGlobal.ReportType.FULL_REPORT_BLANK.getId());
    		 if(StringUtils.isNotBlank(reportTypeAttachment))
			 {
				 List<Long> attachmentIds = new ArrayList<Long>();
				 attachmentIds = getAttachmentIDs(reportTypeAttachment);
				 rsbean.setAttachmentId(attachmentIds);
			 }
    		 rsbean.setLegalApprover(reportTypeLegalApprover);
    		 rsbean.setReportOrderId(reportOrderCounter);
    		 reportOrderCounter++;
    		 reportSummaryDetailsList.add(rsbean);
    	 }
    	 
    	 if(reportTypes!=null && reportTypes.length>0)
    	 {
    		 
    		 for(int i=0;i<reportTypes.length;i++)
    		 {
	    		 rsbean = new ReportSummaryDetails();
	    		 rsbean.setProjectID(projectID);
	    		 if(reportTypes[i].equalsIgnoreCase("1"))
	    		 {
	    			 rsbean.setReportType(SynchroGlobal.ReportType.FULL_REPORT.getId());
	    			/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	    			 {
	    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	    				 List<Long> attachmentIds = new ArrayList<Long>();
	    				 for(AttachmentBean att: attachments)
	    				 {
	    					// attachmentIds.add(new Long(att.getID()));
	    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId())!=null)
	    					 {
	    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.FULL_REPORT.getId()).get(reportOrderCounter).contains(att.getID()))
	    						 {
	    							 attachmentIds.add(new Long(att.getID()));
	    						 }
	    					 }
	    				 }
	    				 rsbean.setAttachmentId(attachmentIds);
	    			 }*/
	    			 if(reportTypeAttachments!=null && (reportTypeAttachments.length>=i+1))
	    			 {
		    			 if(reportTypeAttachments!=null && StringUtils.isNotBlank(reportTypeAttachments[i]))
		    			 {
		    				 List<Long> attachmentIds = new ArrayList<Long>();
		    				 attachmentIds = getAttachmentIDs(reportTypeAttachments[i]);
		    				 rsbean.setAttachmentId(attachmentIds);
		    			 }
	    			 }
	    		 }
	    		 else if(reportTypes[i].equalsIgnoreCase("2"))
	    		 {
	    			 rsbean.setReportType(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId());
	    			/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	    			 {
	    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	    				 List<Long> attachmentIds = new ArrayList<Long>();
	    				 for(AttachmentBean att: attachments)
	    				 {
	    					// attachmentIds.add(new Long(att.getID()));
	    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId())!=null)
	    					 {
	    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TOP_LINE_REPORT.getId()).get(reportOrderCounter).contains(att.getID()))
	    						 {
	    							 attachmentIds.add(new Long(att.getID()));
	    						 }
	    					 }
	    				 }
	    				 rsbean.setAttachmentId(attachmentIds);
	    			 }*/
	    			 if(reportTypeAttachments!=null && (reportTypeAttachments.length>=i+1))
	    			 {
		    			 if(reportTypeAttachments!=null && StringUtils.isNotBlank(reportTypeAttachments[i]))
		    			 {
		    				 List<Long> attachmentIds = new ArrayList<Long>();
		    				 attachmentIds = getAttachmentIDs(reportTypeAttachments[i]);
		    				 rsbean.setAttachmentId(attachmentIds);
		    			 }
	    			 }
	    		 }
	    		 else if(reportTypes[i].equalsIgnoreCase("3"))
	    		 {
	    			 rsbean.setReportType(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId());
	    			 
	    			/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	    			 {
	    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	    				 List<Long> attachmentIds = new ArrayList<Long>();
	    				 for(AttachmentBean att: attachments)
	    				 {
	    					// attachmentIds.add(new Long(att.getID()));
	    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId())!=null)
	    					 {
	    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.EXECUTIVE_PRESENTATION.getId()).get(reportOrderCounter).contains(att.getID()))
	    						 {
	    							 attachmentIds.add(new Long(att.getID()));
	    						 }
	    					 }
	    				 }
	    				 rsbean.setAttachmentId(attachmentIds);
	    			 }*/
	    			 if(reportTypeAttachments!=null && (reportTypeAttachments.length>=i+1))
	    			 {
		    			 if(reportTypeAttachments!=null &&  StringUtils.isNotBlank(reportTypeAttachments[i]))
		    			 {
		    				 List<Long> attachmentIds = new ArrayList<Long>();
		    				 attachmentIds = getAttachmentIDs(reportTypeAttachments[i]);
		    				 rsbean.setAttachmentId(attachmentIds);
		    			 }
	    			 }
	    		 }
	    		 // This is for attaching in case Blank Report Type is selected
	    		 else if(reportTypes[i].equalsIgnoreCase(""))
	    		 {
	    			 rsbean.setReportType(SynchroGlobal.ReportType.FULL_REPORT_BLANK.getId());
	    			
	    			if(reportTypeAttachments!=null && (reportTypeAttachments.length>=i+1))
	    			{
		    			 if(reportTypeAttachments!=null  && StringUtils.isNotBlank(reportTypeAttachments[i]))
		    			 {
		    				 List<Long> attachmentIds = new ArrayList<Long>();
		    				 attachmentIds = getAttachmentIDs(reportTypeAttachments[i]);
		    				 rsbean.setAttachmentId(attachmentIds);
		    			 }
	    			}
    			
	    		 }
	    		 rsbean.setLegalApprover(reportTypeLegalApprovers[i]);
	    		 rsbean.setReportOrderId(reportOrderCounter);
	    		 reportOrderCounter++;
	    		 reportSummaryDetailsList.add(rsbean);
    		 }
    	 }
    	 
    	 if(StringUtils.isNotBlank(irisSummaryLegalApprover))
    	 {
    		 rsbean = new ReportSummaryDetails();
    		 rsbean.setProjectID(projectID);
    		 rsbean.setReportType(SynchroGlobal.ReportType.IRIS_SUMMARY.getId());
    		 rsbean.setLegalApprover(irisSummaryLegalApprover);
    		 rsbean.setReportOrderId(reportOrderCounter);
    		
    		
    		 
    		/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId())!=null)
			 {
				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId());
				 List<Long> attachmentIds = new ArrayList<Long>();
				 for(AttachmentBean att: attachments)
				 {
					// attachmentIds.add(new Long(att.getID()));
					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null)
					 {
						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(reportOrderCounter).contains(att.getID()))
						 {
							 attachmentIds.add(new Long(att.getID()));
						 }
					 }
				 }
				 rsbean.setAttachmentId(attachmentIds);
			 }*/
    		 if(StringUtils.isNotBlank(irisAttachment))
			 {
				 List<Long> attachmentIds = new ArrayList<Long>();
				 attachmentIds = getAttachmentIDs(irisAttachment);
				 rsbean.setAttachmentId(attachmentIds);
			 }
    		 
    		 reportOrderCounter++;
    		 reportSummaryDetailsList.add(rsbean);
    	 }
    	// Added a Blank IRIS Summary Row
    	 else
    	 {
    		 rsbean = new ReportSummaryDetails();
    		 rsbean.setProjectID(projectID);
    		 rsbean.setReportType(SynchroGlobal.ReportType.IRIS_SUMMARY.getId());
    		 rsbean.setLegalApprover(irisSummaryLegalApprover);
    		 rsbean.setReportOrderId(reportOrderCounter);
    		 

    		 /*if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId())!=null)
			 {
				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId());
				 List<Long> attachmentIds = new ArrayList<Long>();
				 for(AttachmentBean att: attachments)
				 {
					// attachmentIds.add(new Long(att.getID()));
					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null)
					 {
						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(reportOrderCounter).contains(att.getID()))
						 {
							 attachmentIds.add(new Long(att.getID()));
						 }
					 }
				 }
				 rsbean.setAttachmentId(attachmentIds);
			 }*/
    		 if(StringUtils.isNotBlank(irisAttachment))
			 {
				 List<Long> attachmentIds = new ArrayList<Long>();
				 attachmentIds = getAttachmentIDs(irisAttachment);
				 rsbean.setAttachmentId(attachmentIds);
			 }
    		 
    		 reportOrderCounter++;
    		 reportSummaryDetailsList.add(rsbean);
    	 }
    	 
    	 if(irisSummaryLegalApprovers!=null && irisSummaryLegalApprovers.length>0)
    	 {
    		// int reportOrderCounter = 2;
    		 for(int i=0;i<irisSummaryLegalApprovers.length;i++)
    		 {
    			 rsbean = new ReportSummaryDetails();
        		 rsbean.setProjectID(projectID);
        		 rsbean.setReportType(SynchroGlobal.ReportType.IRIS_SUMMARY.getId());
        		 rsbean.setLegalApprover(irisSummaryLegalApprovers[i]);
        		 rsbean.setReportOrderId(reportOrderCounter);
        		
        		 
        		 
        		/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId())!=null)
    			 {
    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId());
    				 List<Long> attachmentIds = new ArrayList<Long>();
    				 for(AttachmentBean att: attachments)
    				 {
    					// attachmentIds.add(new Long(att.getID()));
    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId())!=null)
    					 {
    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.IRIS_SUMMARY.getId()).get(reportOrderCounter).contains(att.getID()))
    						 {
    							 attachmentIds.add(new Long(att.getID()));
    						 }
    					 }
    				 }
    				 rsbean.setAttachmentId(attachmentIds);
    			 }
        		 */
        		 if(irisAttachments!=null && (irisAttachments.length>=i+1))
        		 {
	        		 if(irisAttachments!=null && StringUtils.isNotBlank(irisAttachments[i]))
	    			 {
	    				 List<Long> attachmentIds = new ArrayList<Long>();
	    				 attachmentIds = getAttachmentIDs(irisAttachments[i]);
	    				 rsbean.setAttachmentId(attachmentIds);
	    			 }
        		 }
        		 reportOrderCounter++;
        		 reportSummaryDetailsList.add(rsbean);
    		 }
    	 }
    	 
    	 // Incase of End Market Non EU we dont have to save TPD Summary Details
    	 if(project.getProcessType()!=SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId())
    	 {	 
    		//http://redmine.nvish.com/redmine/issues/245 : Only if the status is 'May have to be Submitted' then only save the TPD Summary
    		 
    		 if((project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() 
	    			 || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()) && (proposalInitiation.getLegalApprovalStatus()!=null && proposalInitiation.getLegalApprovalStatus()==1))
	    	 {
    		 
	    		 if(StringUtils.isNotBlank(tpdSummaryLegalApprover))
		    	 {
		    		 rsbean = new ReportSummaryDetails();
		    		 rsbean.setProjectID(projectID);
		    		 rsbean.setReportType(SynchroGlobal.ReportType.TPD_SUMMARY.getId());
		    		 rsbean.setLegalApprover(tpdSummaryLegalApprover);
		    		 rsbean.setReportOrderId(reportOrderCounter);
		    		
		    		 if(StringUtils.isNotBlank(tpdSummaryDate))
		    		 {
		    	            rsbean.setLegalApprovalDate(DateUtils.parse(tpdSummaryDate));
		    	     }
		    		 
		    		 
		    		 /*if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId())!=null)
					 {
						 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId());
						 List<Long> attachmentIds = new ArrayList<Long>();
						 for(AttachmentBean att: attachments)
						 {
							// attachmentIds.add(new Long(att.getID()));
							 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null)
							 {
								 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(reportOrderCounter).contains(att.getID()))
								 {
									 attachmentIds.add(new Long(att.getID()));
								 }
							 }
						 }
						 rsbean.setAttachmentId(attachmentIds);
					 }
		    		 */
		    		 if(StringUtils.isNotBlank(tpdAttachment))
	    			 {
	    				 List<Long> attachmentIds = new ArrayList<Long>();
	    				 attachmentIds = getAttachmentIDs(tpdAttachment);
	    				 rsbean.setAttachmentId(attachmentIds);
	    			 }
		    		 reportOrderCounter++;
		    		 reportSummaryDetailsList.add(rsbean);
		    	 }
		    	 // Added a Blank TPD Summary Row
		    	 else
		    	 {
		    		 rsbean = new ReportSummaryDetails();
		    		 rsbean.setProjectID(projectID);
		    		 rsbean.setReportType(SynchroGlobal.ReportType.TPD_SUMMARY.getId());
		    		 rsbean.setLegalApprover(tpdSummaryLegalApprover);
		    		 rsbean.setReportOrderId(reportOrderCounter);
		    		 
		    		 if(StringUtils.isNotBlank(tpdSummaryDate))
		    		 {
		    	            rsbean.setLegalApprovalDate(DateUtils.parse(tpdSummaryDate));
		    	     }
		    		 
		    		/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId())!=null)
					 {
						 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId());
						 List<Long> attachmentIds = new ArrayList<Long>();
						 for(AttachmentBean att: attachments)
						 {
							// attachmentIds.add(new Long(att.getID()));
							 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null)
							 {
								 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(reportOrderCounter).contains(att.getID()))
								 {
									 attachmentIds.add(new Long(att.getID()));
								 }
							 }
						 }
						 rsbean.setAttachmentId(attachmentIds);
					 }*/
		    		 if(StringUtils.isNotBlank(tpdAttachment))
	    			 {
	    				 List<Long> attachmentIds = new ArrayList<Long>();
	    				 attachmentIds = getAttachmentIDs(tpdAttachment);
	    				 rsbean.setAttachmentId(attachmentIds);
	    			 }
		    		 
		    		 reportOrderCounter++;
		    		 reportSummaryDetailsList.add(rsbean);
		    	 }
	    		 
	    	 
	         
	    	
	    	 
	    	 
	    	
	    	 
	    	 if(tpdSummaryLegalApprovers!=null && tpdSummaryLegalApprovers.length>0)
	    	 {
	    		// int reportOrderCounter = 2;
	    		 for(int i=0;i<tpdSummaryLegalApprovers.length;i++)
	    		 {
	    			 rsbean = new ReportSummaryDetails();
	        		 rsbean.setProjectID(projectID);
	        		 rsbean.setReportType(SynchroGlobal.ReportType.TPD_SUMMARY.getId());
	        		 rsbean.setLegalApprover(tpdSummaryLegalApprovers[i]);
	        		 rsbean.setReportOrderId(reportOrderCounter);
	        		 // TODO :  Add the appropriate Date for each row
	        		 try
	        		 {
		        		 if(StringUtils.isNotBlank(tpdSummaryDates[i]))
		        		 {
		        	            rsbean.setLegalApprovalDate(DateUtils.parse(tpdSummaryDates[i]));
		        	     }
	        		 }
	        		 catch(Exception e)
	        		 {
	        			 
	        		 }
	        		 
	        		 
	        		 
	        		 
	        		/* if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId())!=null)
	    			 {
	    				 List<AttachmentBean> attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId());
	    				 List<Long> attachmentIds = new ArrayList<Long>();
	    				 for(AttachmentBean att: attachments)
	    				 {
	    					// attachmentIds.add(new Long(att.getID()));
	    					 if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null)
	    					 {
	    						 if(reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(reportOrderCounter)!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId()).get(reportOrderCounter).contains(att.getID()))
	    						 {
	    							 attachmentIds.add(new Long(att.getID()));
	    						 }
	    					 }
	    				 }
	    				 rsbean.setAttachmentId(attachmentIds);
	    			 }*/
	        		 
	        		 if(tpdAttachments!=null && (tpdAttachments.length>=i+1))
	        		 {
		        		 if(tpdAttachments!=null && StringUtils.isNotBlank(tpdAttachments[i]))
		    			 {
		    				 List<Long> attachmentIds = new ArrayList<Long>();
		    				 attachmentIds = getAttachmentIDs(tpdAttachments[i]);
		    				 rsbean.setAttachmentId(attachmentIds);
		    			 }
	        		 }
	        		 reportOrderCounter++;
	        		 reportSummaryDetailsList.add(rsbean);
	    		 }
	    	 }
	    	 }
    	 }
    	 
    	 
    	 this.reportSummaryManagerNew.saveReportSummaryDetailsNew(reportSummaryDetailsList);
    	 
    	 
    }
    public List<Long> getAttachmentIDs(String array)
    {
    	List<Long> list = new ArrayList<Long>();
    	if(array!=null && array.length()>0)
    	{
	    	for (String id : array.split(","))
	    	{
	    	    if(StringUtils.isNotBlank(id))
	    	    {
	    	    	list.add(new Long(id));
	    	    }
	    	}
    	}
    	return list;
    }
    
public String addAttachment() throws UnsupportedEncodingException {
        
    	Map<String, Object> result = new HashMap<String, Object>();
        try
        {
        	System.out.print("repTypeId ---"+ repTypeId);
        	System.out.print("repOrderId ---"+reportOrderId);
        //	long attachId = reportSummaryManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID());

        	long attachId = reportSummaryManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,new Long("-1"),fieldCategoryId,getUser().getID());
        	
        	//TODO : Need to revisit this logic again
        	// This is done to preserve atleast one row for All Report Types on UI
        	//List<ReportSummaryInitiation> initiationList = this.reportSummaryManagerNew.getReportSummaryInitiation(projectID);
        	List<ReportSummaryDetails> rsdList = reportSummaryDetailsList = this.reportSummaryManagerNew.getReportSummaryDetails(projectID);
        	
        	if(StringUtils.isNotBlank(repTypeId) && StringUtils.isNotBlank(reportOrderId))
        	{
	        	// This will save the Report Summary Details initially
	        	if(rsdList==null || rsdList.size()==0)
	        	{
	        		saveReportSummaryDetails();
	        		if(new Integer(reportOrderId).intValue()==1)
	        		{
	        			ReportSummaryDetails rsdbean = new ReportSummaryDetails();
	    	        	rsdbean.setProjectID(projectID);
	    	        	rsdbean.setReportOrderId(new Integer(reportOrderId));
	    	        	//rsdbean.setLegalApprover("");
	    	        	rsdbean.setReportType(new Integer(repTypeId));
	    	        	List<Long> attachmentIds = new ArrayList<Long>();
	    			    attachmentIds.add(new Long(attachId));
	    			    rsdbean.setAttachmentId(attachmentIds);
	    			    reportSummaryDetailsList.add(rsdbean);
	    			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
	        		}
	        	}
	        	
	        	else
	        	{
		        	// reportOrderId -1 is used for New Row
	        		if(new Integer(reportOrderId).intValue()==-1)
		        	{
		        		//Add the logic here for Adding attachment for new row without saving the form
		        		Integer maxReportOrderId = new Integer(0);
		        		maxReportOrderId = this.reportSummaryManagerNew.getMaxReportOrderId(projectID, new Integer(repTypeId));
		        		
		        		// shift the reportOrderId by 1.
		        		//reportSummaryManagerNew.updateReportOrderId(projectID, (maxReportOrderId+1));
		        		
		        		List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
			        	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
			        	rsdbean.setProjectID(projectID);
			        	//rsdbean.setReportOrderId(maxReportOrderId+1);
			        	rsdbean.setReportOrderId(maxReportOrderId);
			        	//rsdbean.setLegalApprover("");
			        	rsdbean.setReportType(new Integer(repTypeId));
			        	List<Long> attachmentIds = new ArrayList<Long>();
					    attachmentIds.add(new Long(attachId));
					    rsdbean.setAttachmentId(attachmentIds);
					    reportSummaryDetailsList.add(rsdbean);
					    
					    // As the attachment Will be added as part of Auto Save so no need to save Report Summary Details. Only save the Attachment Details
					   // this.reportSummaryManagerNew.saveReportSummaryDetailsNew(reportSummaryDetailsList);
					    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
					
		        	}
		        	else
		        	{
			        	List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
			        	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
			        	rsdbean.setProjectID(projectID);
			        	rsdbean.setReportOrderId(new Integer(reportOrderId));
			        	//rsdbean.setLegalApprover("");
			        	rsdbean.setReportType(new Integer(repTypeId));
			        	List<Long> attachmentIds = new ArrayList<Long>();
					    attachmentIds.add(new Long(attachId));
					    rsdbean.setAttachmentId(attachmentIds);
					    reportSummaryDetailsList.add(rsdbean);
					    
					    // This is done to capture the use case in which we delete teh row from front end and then add attachment for new row without clicking on SAVE
					  /*  List<ReportSummaryDetails> rsdList1 = reportSummaryManagerNew.getReportSummaryDetails(projectID, repTypeId, reportOrderId);
					    if(rsdList1!=null && rsdList1.size()>0)
					    {
					    	
					    }
					    else
					    {
					    	 this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
					    }*/
					    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
		        	}	
	        	}
        	} 
        	//Add Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment" + "- " +attachFileFileName;
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), description, project.getName(), 
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
			//projectSpecsManagerNew.removeAttachment(attachmentId);
			reportSummaryManagerNew.removeAttachment(attachmentId);
			reportSummaryManagerNew.deleteReportSummaryAttachment(attachmentId);
			
			 //Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName +" deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.PROJECT_COMPLETE.getId(), description, project.getName(), 
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

	public ReportSummaryInitiation getReportSummaryInitiation() {
		return reportSummaryInitiation;
	}

	public void setReportSummaryInitiation(
			ReportSummaryInitiation reportSummaryInitiation) {
		this.reportSummaryInitiation = reportSummaryInitiation;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public List<Long> getEndMarketIds() {
		return endMarketIds;
	}

	public void setEndMarketIds(List<Long> endMarketIds) {
		this.endMarketIds = endMarketIds;
	}

	public List<Long> getUniqueAgencyId() {
		return uniqueAgencyId;
	}

	public void setUniqueAgencyId(List<Long> uniqueAgencyId) {
		this.uniqueAgencyId = uniqueAgencyId;
	}

	public ProjectEvaluationInitiation getProjectEvaluationInitiation_DB() {
		return projectEvaluationInitiation_DB;
	}

	public void setProjectEvaluationInitiation_DB(
			ProjectEvaluationInitiation projectEvaluationInitiation_DB) {
		this.projectEvaluationInitiation_DB = projectEvaluationInitiation_DB;
	}

	public ProjectInitiation getProjectInitiation_DB() {
		return projectInitiation_DB;
	}

	public void setProjectInitiation_DB(ProjectInitiation projectInitiation_DB) {
		this.projectInitiation_DB = projectInitiation_DB;
	}

	public ProposalInitiation getProposalInitiation_DB() {
		return proposalInitiation_DB;
	}

	public void setProposalInitiation_DB(ProposalInitiation proposalInitiation_DB) {
		this.proposalInitiation_DB = proposalInitiation_DB;
	}

	public Project getProject_DB() {
		return project_DB;
	}

	public void setProject_DB(Project project_DB) {
		this.project_DB = project_DB;
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

	public AttachmentHelper getAttachmentHelper() {
		return attachmentHelper;
	}

	public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
		this.attachmentHelper = attachmentHelper;
	}

	public Long getAttachmentFieldID() {
		return attachmentFieldID;
	}

	public void setAttachmentFieldID(Long attachmentFieldID) {
		this.attachmentFieldID = attachmentFieldID;
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

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getRepTypeId() {
		return repTypeId;
	}

	public void setRepTypeId(String repTypeId) {
		this.repTypeId = repTypeId;
	}

	public String getReportOrderId() {
		return reportOrderId;
	}

	public void setReportOrderId(String reportOrderId) {
		this.reportOrderId = reportOrderId;
	}

}
