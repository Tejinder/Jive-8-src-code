package com.grail.synchro.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.struts2.ServletActionContext;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.ExportWordUtil;
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
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.user.profile.ProfileManager;
import com.jivesoftware.util.InputStreamDataSource;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: Tejinder
 * @since: 1.0
 */
public class ProposalActionNew extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ProposalAction.class);

    //Spring Managers
    private PIBManagerNew pibManagerNew;
    private ProjectManagerNew synchroProjectManagerNew;
    private ProfileManager profileManager;

    //Form related fields
    private ProjectInitiation projectInitiation;
    private ProjectInitiation projectInitiation_DB;
    private ProposalInitiation proposalInitiation;
    private ProposalInitiation proposalInitiation_DB;
    private List<EndMarketInvestmentDetail> endMarketDetails_DB;
    private EndMarketInvestmentDetail endMarketDetail_DB = null; 
    private PIBStakeholderList pibStakeholderList_DB;
    private Project project;
    private Project project_DB = null;
    private PIBReporting pibReporting_DB = null;
    private Long projectID;


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

    
    private Integer stageId;

   
    private StageManager stageManager;
 
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
    private Long attachmentFieldID;
    private String attachmentName;
    private Long fieldCategoryId;
    private List<EndMarketInvestmentDetail> endMarketDetails;
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

    // This field will check whether the user click on PIB Kantar Methodology Waiver is Approve or Reject button or Send for Information or Request more information
    private String kantarMethodologyWaiverAction;

 // This field will check whether the request is from Pending Actions Page or not
    private String pageRequest;

    //private UserManager userManager;


    private List<ProjectInitiation> initiationList;
    private List<ProposalInitiation> proposalInitiationList;
    private Map<Long,Long> attachmentUser;
    private Boolean showMandatoryFieldsError;
    private ProposalManagerNew proposalManagerNew;

    // This flag will check whether a proposal has been awarded to an agency or not
    private Boolean isProposalAwarded=false;

    private String subjectSendToProjOwner;
    private String messageBodySendToProjOwner;
    private String subjectSendToSPI;
    private String messageBodySendToSPI;

    private String subjectAdminPIBComplete;
    private String messageBodyAdminPIBComplete;
    private String adminPIBCompleteRecipents;

    private EmailNotificationManager emailNotificationManager;
    private File[] mailAttachment;
    private String[] mailAttachmentFileName;
    private String[] mailAttachmentContentType;

    private String otherBATUsers;
    
    List<ProjectCostDetailsBean> projectCostDetailsList;
    private SynchroUtils synchroUtils;
    Map<Long, List<User>> endMarketLegalApprovers;
    private List<ProjectCostDetailsBean> projectCostDetails;
    private List<Long> endMarketIds;
    private Map<Integer, BigDecimal> totalCosts;

    public void prepare() throws Exception {

        final String id = getRequest().getParameter("projectID");

        if(id  != null ) {

            try{
                if(projectID==null)
                    projectID = Long.parseLong(id);
            } catch (NumberFormatException nfEx) {
                LOG.error("Invalid ProjectID ");
                throw nfEx;
            }
            project = this.synchroProjectManagerNew.get(projectID);
            String validationError = getRequest().getParameter("validationError");
          /*  if(validationError!=null && validationError.equals("true"))
            {
                showMandatoryFieldsError=true;
            }
*/

            endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
            endMarketIds = this.synchroProjectManagerNew.getEndMarketIDs(projectID);

            //    methDeviationField=stageManager.checkPartialMethodologyValidation(project);


            initiationList = this.pibManagerNew.getPIBDetailsNew(projectID);
            proposalInitiationList = this.proposalManagerNew.getProposalInitiationNew(projectID);

            pibMethodologyWaiver = this.pibManagerNew.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());

            pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
            
            projectCostDetailsList = this.synchroProjectManagerNew.getProjectCostDetails(projectID);
            
            totalCosts = this.synchroProjectManagerNew.getTotalCosts(projectCostDetailsList, project.getTotalCost());
            
            endMarketLegalApprovers = getSynchroUtils().getLegalApprovers();

            //attachmentMap = this.proposalManagerNew.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID(), new Long("-1"));
            attachmentMap = this.proposalManagerNew.getDocumentAttachment(projectID, new Long("-1"), new Long("-1"));
            
            if(pibMethodologyWaiver==null)
            {
                pibMethodologyWaiver = new PIBMethodologyWaiver();
            }
            if(pibKantarMethodologyWaiver==null)
            {
                pibKantarMethodologyWaiver = new PIBMethodologyWaiver();
            }
            if( proposalInitiationList != null && proposalInitiationList.size() > 0) {
                this.proposalInitiation = proposalInitiationList.get(0);
              
              /*  if(endMarketId!=null && endMarketId>0)
                {
                    attachmentMap = this.proposalManagerNew.getDocumentAttachment(projectID, endMarketId, new Long("-1"));
                    //pibStakeholderList = this.pibManager.getPIBStakeholderList(projectID, endMarketId);
                }
                else
                {
                    attachmentMap = this.proposalManagerNew.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID(), new Long("-1"));
                    //pibStakeholderList = this.pibManager.getPIBStakeholderList(projectID, endMarketDetails.get(0).getEndMarketID());
                }*/

            }  else {
                this.proposalInitiation = new ProposalInitiation();
              
               
                isSave = true;
            }
            
            if( initiationList != null && initiationList.size() > 0) {
                this.projectInitiation = initiationList.get(0);
              
            }  else {
                this.projectInitiation = new ProjectInitiation();
              
            }
            List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
            for(Integer i : attachmentMap.keySet())
            {
                abList.addAll(attachmentMap.get(i));
            }
            attachmentUser = pibManagerNew.getAttachmentUser(abList);
            // Moved from Input Action --
            //    String status=ribDocument.getProperties().get(SynchroConstants.STAGE_STATUS);
            stageId = SynchroGlobal.getProjectActivityTab().get(INPUT);
           // approvers = stageManager.getStageApprovers(stageId.longValue(), project);
            //	editStage=SynchroPermHelper.canEditStageDocument(ribDocument,projectID);
            //editStage=SynchroPermHelper.canEditProjectByStatus(projectID);
            //TODO - To add the project and stage status check over here whether the PIB stage is completed or not.
            String baseUrl = URLUtils.getRSATokenBaseURL(request);
            User user = getUser();
            String timeZone = user.getProperties().get(JiveConstants.USER_TIMEZONE_PROP_NAME);
            String locale  = user.getProperties().get(JiveConstants.USER_LOCALE_PROP_NAME);
            if(editStage)
            {
              //  stageToDoList = stageManager.getToDoListTabs(getUser(), projectID,stageId,project.getName(),endMarketDetails.get(0).getEndMarketID(),baseUrl);
            }
            else
            {
                
               // stageToDoList = stageManager.getDisabledToDoListTabs(getUser(), projectID,stageId,project.getName(),endMarketDetails.get(0).getEndMarketID(),baseUrl);
            }

         

        }

      

        // Contenttype check is required to skip the below binding in case odf adding attachments
        if(getRequest().getMethod() == "POST" && !getRequest().getContentType().startsWith("multipart/form-data") && getRequest().getParameter("attachmentId")==null) {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.proposalInitiation);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Proposal bean.");
              
                input();
            }

         

            // To map the Project Parameters
            binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());


            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Project bean.");
                input();
            }

//            if(this.project.getMethodologyGroup() != null && this.project.getMethodologyGroup().intValue() > 0) {
//                Long mtId = SynchroGlobal.getMethodologyTypeByGroup(this.project.getMethodologyGroup());
//                this.project.setMethodologyType(mtId);
//            }


           
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
            if(getRequest().getParameter("kantarMethodologyWaiverAction")!=null && getRequest().getParameter("kantarMethodologyWaiverAction").equals("Waiver Attachment"))
            {
                kantarMethodologyWaiverAction = "Waiver Attachment";
                pibKantarMethodologyWaiver.setMethodologyApproverComment(getRequest().getParameter("methodologyApproverComment"));
            }
            pageRequest = getRequest().getParameter("pageRequest");
            
            if(StringUtils.isNotBlank(getRequest().getParameter("startDate"))) {
                this.project.setStartDate(DateUtils.parse(getRequest().getParameter("startDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("endDate"))) {
                this.project.setEndDate(DateUtils.parse(getRequest().getParameter("endDate")));
            }
	    if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalDate"))) {
                this.proposalInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("legalApprovalDate")));
            }
            try
            {
	            if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusO"))) {
	                this.proposalInitiation.setLegalApprovalStatus(Integer.parseInt(getRequest().getParameter("legalApprovalStatusO")));
	            }
            }
            catch(Exception e)
            {
            	
            }
        }

        
      //Audit Logs: Get Project & ProjectInitiationfrom Database for compare needed in Audit trails
        if(projectID!=null && projectID>0)
        {
        	project_DB = this.synchroProjectManagerNew.get(projectID);
        	
        	List<ProposalInitiation> initiationList_DB = this.proposalManagerNew.getProposalInitiationNew(projectID);
        	if( initiationList_DB != null && initiationList_DB.size() > 0) {
        		proposalInitiation_DB = initiationList_DB.get(0);            		
        	}
        	
        	List<ProjectInitiation> projectInitiationList_DB = this.pibManagerNew.getPIBDetailsNew(projectID);
        	if( projectInitiationList_DB != null && projectInitiationList_DB.size() > 0) {
        		projectInitiation_DB = projectInitiationList_DB.get(0);            		
        	}
        
        }
    }

    public String input() {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
         if (SynchroPermHelper.hasProposalAccessNew(projectID) ||  SynchroPermHelper.userTypeAccess(projectID) || SynchroPermHelper.isLegaUserType()) {
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
         	if(redirectURL.contains("proposal-details"))
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

        LOG.info("Save the proposal Details ...."+proposalInitiation);
        
        Boolean manFieldsError = false;
        //  if( projectInitiation != null && ribDocument != null){
        if( proposalInitiation != null){
        	proposalInitiation.setProjectID(projectID);

            
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
            
            // Setting the Proposal Legal Approver for EU Offline cases
            if(getRequest().getParameter("proposalLegalApproverOffline")!=null)
            {
            	proposalInitiation.setProposalLegalApproverOffline(getRequest().getParameter("proposalLegalApproverOffline"));
            }
           
            if(isSave) {
            	proposalInitiation.setCreationBy(getUser().getID());
            	proposalInitiation.setCreationDate(System.currentTimeMillis());

            	proposalInitiation.setModifiedBy(getUser().getID());
            	proposalInitiation.setModifiedDate(System.currentTimeMillis());

               
                // https://svn.sourcen.com/issues/19652
            	proposalInitiation.setProposal(SynchroUtils.fixBulletPoint(proposalInitiation.getProposal()));
            	proposalInitiation.setProposalLegalApprover(proposalInitiation.getProposalLegalApprover());
		        //proposalInitiation.setStatus(SynchroGlobal.StageStatus.PROPOSAL_SAVED.ordinal());
            	
		        
		        //For Synchro New Requirements we manage only 2 status for PIB : PIB SAVED and PIB Completed
                 if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
                 {
                 	proposalInitiation.setStatus(SynchroGlobal.StageStatus.PROPOSAL_SUBMITTED.ordinal());
                 	//Update Project Status to Project Specs Stage
                 	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
                 }
                 else
                 {
                 	proposalInitiation.setStatus(SynchroGlobal.StageStatus.PROPOSAL_SAVED.ordinal());
                 }
		        
		        
		        if(getRequest().getParameter("legalSignOffReqValue")!=null && !getRequest().getParameter("legalSignOffReqValue").equalsIgnoreCase(""))
                {
		        	//proposalInitiation.setProposal("");
		        	//proposalInitiation.setProposalText("");
		        	proposalInitiation.setLegalSignOffRequired(new Integer("1"));
                }
                else
                {
                	proposalInitiation.setLegalSignOffRequired(new Integer("0"));
                }
               
                this.proposalManagerNew.saveProposalDetails(proposalInitiation);
                
              //Audit Log: Add User Audit details for PIB Save               
                SynchroLogUtilsNew.ProposalSaveNew(proposalInitiation_DB, proposalInitiation, project_DB, project);                

            }
            else {
            	proposalInitiation.setModifiedBy(getUser().getID());
            	proposalInitiation.setModifiedDate(System.currentTimeMillis());

                Boolean isAdminUser = SynchroPermHelper.isSynchroAdmin(getUser());

                proposalInitiation.setProposal(SynchroUtils.fixBulletPoint(proposalInitiation.getProposal()));
               
                proposalInitiation.setProposalLegalApprover(proposalInitiation.getProposalLegalApprover());
                if(proposalInitiation.getProposalLegalApprover()==null && getRequest().getParameter("proposalLegalApproverOri")!=null)
                {
                	proposalInitiation.setProposalLegalApprover(new Long(getRequest().getParameter("proposalLegalApproverOri")));
                }

                if(getRequest().getParameter("legalSignOffReqValue")!=null && !getRequest().getParameter("legalSignOffReqValue").equalsIgnoreCase(""))
                {
		        	//proposalInitiation.setProposal("");
		        	//proposalInitiation.setProposalText("");
		        	proposalInitiation.setLegalSignOffRequired(new Integer("1"));
                }
                else
                {
                	proposalInitiation.setLegalSignOffRequired(new Integer("0"));
                }
           	 // For Synchro New Requirements we manage only 2 status for PIB : PIB SAVED and PIB Completed
                if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
                {
                	proposalInitiation.setStatus(SynchroGlobal.StageStatus.PROPOSAL_SUBMITTED.ordinal());
                	//Update Project Status to Project Specs Stage
                	synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
                }
                else
                {
                	proposalInitiation.setStatus(SynchroGlobal.StageStatus.PROPOSAL_SAVED.ordinal());
                }
                this.proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
                
              //Audit Log: Add User Audit details for PIB Save               
                SynchroLogUtilsNew.ProposalSaveNew(proposalInitiation_DB, proposalInitiation, project_DB, project);   

            }


        } else {
            LOG.error("Proposal Initiation was null  ");
            addActionError("Proposal Initiation missing.");
        }
        
      //Audit Logs: PIB SAVE
        String i18Text = getText("logger.project.proposal.saved.text");
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
        
       /* if(manFieldsError)
        {
            redirectURL="/synchro/pib-details!input.jspa?projectID="+projectID+"&validationError=true";
            return "validationError";
        }
		*/

        if(getRequest().getParameter("confirmProject")!=null && !getRequest().getParameter("confirmProject").equalsIgnoreCase(""))
        {
        	
        	return "project-specs";
        }
        
        return SUCCESS;
    }

    /**
     * This method will update the Proposal Send for Approval Field
     * @return
     */
    public String updateSendForApproval(){
    	
    	if(proposalInitiation.getStatus()==null)
    	{
    		 proposalInitiation.setCreationBy(getUser().getID());
    		 proposalInitiation.setCreationDate(System.currentTimeMillis());

             proposalInitiation.setModifiedBy(getUser().getID());
             proposalInitiation.setModifiedDate(System.currentTimeMillis());
             proposalInitiation.setStatus(SynchroGlobal.StageStatus.PROPOSAL_SAVED.ordinal());
             
             this.proposalManagerNew.saveProposalDetails(proposalInitiation);
    	}
    	
    	proposalInitiation.setSendForApproval(1);
    	proposalInitiation.setNeedsDiscussion(null);
    	proposalInitiation.setModifiedBy(getUser().getID());
    	proposalInitiation.setModifiedDate(System.currentTimeMillis());
    	
    	proposalInitiation.setSendForApprovalDate(new Date());
    	if(StringUtils.isNotBlank(request.getParameter("proposalLegalApproverHidden")))
        {
    		proposalInitiation.setProposalLegalApprover(new Long(request.getParameter("proposalLegalApproverHidden")));
        }
    	
    	proposalManagerNew.updateProposalSendForApproval(proposalInitiation);
    	
// Send Notification
        
        Long proposalLegalApprover = proposalInitiation.getProposalLegalApprover();
        String proposalLegalApproverEmail = SynchroUtils.getUserEmail(proposalLegalApprover);
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/proposal-details!input.jspa?projectID=" + projectID;
        
	String adminEmail = SynchroPermHelper.getSystemAdminEmail();
        
        String adminName = SynchroPermHelper.getSystemAdminName();
        EmailMessage email = stageManager.populateNotificationEmail(proposalLegalApproverEmail, null, null,"pib.brief.sendforapproval.htmlBody","pib.brief.sendforapproval.subject");

        //email.getContext().put("projectId", projectID);

        email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
        email.getContext().put("projectName",project.getName());
        email.getContext().put ("stageUrl",stageUrl);
        email.getContext().put ("requesterName",getUser().getName());
        email.getContext().put ("reviewType","Proposal");
        stageManager.sendNotificationNew(adminName, adminEmail, email);

        //Email Notification TimeStamp Storage
        EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
        emailNotBean.setProjectID(projectID);
        emailNotBean.setEndmarketID(endMarketId);
        emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
        emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
        emailNotBean.setEmailDesc("Action Requied | Proposal Send for Approval");
        emailNotBean.setEmailSubject("Action Requied | Proposal Send for Approval");
        emailNotBean.setEmailSender(adminEmail);
        emailNotBean.setEmailRecipients(proposalLegalApproverEmail);
        emailNotificationManager.saveDetails(emailNotBean);
        
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.proposal.send.for.approval"), project.getName(), 
						project.getProjectID(), getUser().getID());
        
    	return SUCCESS;
    }
    
    /**
     * This method will reset Proposal Legal Aproval Details for Admin User
     * @return
     */
    public String resetProposal(){
    	
    	proposalManagerNew.resetProposal(proposalInitiation);   
    	// We need to remove all the details for PIB Reset
    	
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
	   SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
   				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.proposal.reset"), project.getName(), 
   						project.getProjectID(), getUser().getID());
           
    	return SUCCESS;
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
				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.pib.reset"), project.getName(), 
						project.getProjectID(), getUser().getID());
    	return "pib-details";
    }
    
    /**
     * This method will be called when send Reminder is clicked
     * @return
     */
    public String updateSendReminder(){
    	
    	proposalInitiation.setSendReminderDate(new Date());
    	
    	proposalManagerNew.updateProposalSendForReminder(proposalInitiation);
    	// Send Notification
        
    	  Long proposalLegalApprover = proposalInitiation.getProposalLegalApprover();
          String proposalLegalApproverEmail = SynchroUtils.getUserEmail(proposalLegalApprover);
          String baseUrl = URLUtils.getRSATokenBaseURL(request);
          String stageUrl = baseUrl+"/new-synchro/proposal-details!input.jspa?projectID=" + projectID;
        
        EmailMessage email = stageManager.populateNotificationEmail(proposalLegalApproverEmail, null, null,"pib.brief.sendforapproval.reminder.htmlBody","pib.brief.sendforapproval.reminder.subject");

        //email.getContext().put("projectId", projectID);

        email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
        email.getContext().put("projectName",project.getName());
        email.getContext().put ("stageUrl",stageUrl);
        email.getContext().put ("requesterName",getUser().getName());
        email.getContext().put ("reviewType","Proposal");
        
	String adminEmail = SynchroPermHelper.getSystemAdminEmail();
        
        String adminName = SynchroPermHelper.getSystemAdminName();
        stageManager.sendNotificationNew(adminName, adminEmail, email);


        //Email Notification TimeStamp Storage
        EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
        emailNotBean.setProjectID(projectID);
        emailNotBean.setEndmarketID(endMarketId);
        emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
        emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
        emailNotBean.setEmailDesc("Action Requied  | Send Reminder to Proposal Legal Approver");
        emailNotBean.setEmailSubject("Action Requied  | Send Reminder to Proposal Legal Approver");
        emailNotBean.setEmailSender(getUser().getEmail());
        emailNotBean.setEmailRecipients(adminEmail);
        emailNotificationManager.saveDetails(emailNotBean);
        
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
   				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.proposal.send.reminder"), project.getName(), 
   						project.getProjectID(), getUser().getID());
           
    
    	return SUCCESS;
    }
    /**
     * This method will update the Proposal Needs Discussion
     * @return
     */
    public String updateNeedsDisussion(){
    	
    	proposalInitiation.setSendForApproval(null);
    	proposalInitiation.setNeedsDiscussion(1);
    	proposalInitiation.setModifiedBy(getUser().getID());
    	proposalInitiation.setModifiedDate(System.currentTimeMillis());
    	proposalManagerNew.updateProposalNeedsDiscussion(proposalInitiation);
    	
 // Send Notification
        
        Long projectInitiator = project.getBriefCreator();
        String projectInitiatorEmail = SynchroUtils.getUserEmail(projectInitiator);
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/proposal-details!input.jspa?projectID=" + projectID;
        
        EmailMessage email = stageManager.populateNotificationEmail(projectInitiatorEmail, null, null,"pib.brief.needDiscussion.htmlBody","pib.brief.needDiscussion.subject");

        //email.getContext().put("projectId", projectID);

        email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
        email.getContext().put("projectName",project.getName());
        email.getContext().put ("stageUrl",stageUrl);
        email.getContext().put ("reviewType","Proposal");
        try
        {
        	email.getContext().put ("legalUserName",userManager.getUser(projectInitiation.getBriefLegalApprover().longValue()).getName());
        }
        catch(UserNotFoundException e)
        {
        	
        }
         String adminEmail = SynchroPermHelper.getSystemAdminEmail();
        
        String adminName = SynchroPermHelper.getSystemAdminName();
        stageManager.sendNotificationNew(adminName, adminEmail, email);

        //Email Notification TimeStamp Storage
        EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
        emailNotBean.setProjectID(projectID);
        emailNotBean.setEndmarketID(endMarketId);
        emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
        emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
        emailNotBean.setEmailDesc("Action Required | Need Discussion on Proposal");
        emailNotBean.setEmailSubject("Action Required | Need Discussion on Proposal");
        emailNotBean.setEmailSender(adminEmail);
        emailNotBean.setEmailRecipients(projectInitiatorEmail);
        emailNotificationManager.saveDetails(emailNotBean);
        
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
   				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.proposal.need.discussion"), project.getName(), 
   						project.getProjectID(), getUser().getID());
        
    	return SUCCESS;
    }
    
    /**
     * This method will update the Proposal Needs Discussion
     * @return
     */
    public String confirmLegalApprovalSubmission(){
    	
    	proposalInitiation.setSendForApproval(null);
    	proposalInitiation.setNeedsDiscussion(null);
    	proposalInitiation.setModifiedBy(getUser().getID());
    	proposalInitiation.setModifiedDate(System.currentTimeMillis());
    	proposalInitiation.setLegalApprovalDate(new Date());
    	proposalInitiation.setLegalApprovalStatus(proposalInitiation.getLegalApprovalStatus());
    	proposalManagerNew.confirmLegalApprovalSubmission(proposalInitiation);
    	
// Send Notification
        
        Long projectOwner = project.getBriefCreator();
        String projectOwnerEmail = SynchroUtils.getUserEmail(projectOwner);
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/proposal-details!input.jspa?projectID=" + projectID;
        
        EmailMessage email = stageManager.populateNotificationEmail(projectOwnerEmail, null, null,"pib.brief.confirmSubmission.htmlBody","pib.brief.confirmSubmission.subject");

        //email.getContext().put("projectId", projectID);

        email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
        email.getContext().put("projectName",project.getName());
        email.getContext().put ("stageUrl",stageUrl);
        email.getContext().put ("reviewType","Proposal");
        
        try
        {
        	email.getContext().put ("legalUserName",userManager.getUser(projectInitiation.getBriefLegalApprover().longValue()).getName());
        }
        catch(UserNotFoundException e)
        {
        	
        }
        String adminEmail = SynchroPermHelper.getSystemAdminEmail();
        
        String adminName = SynchroPermHelper.getSystemAdminName();
        stageManager.sendNotificationNew(adminName, adminEmail, email);

        //Email Notification TimeStamp Storage
        EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
        emailNotBean.setProjectID(projectID);
        emailNotBean.setEndmarketID(endMarketId);
        emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
        emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
        emailNotBean.setEmailDesc("Notification | Confirm Submission on Proposal");
        emailNotBean.setEmailSubject("Notification | Confirm Submission on Proposal");
        emailNotBean.setEmailSender(adminEmail);
        emailNotBean.setEmailRecipients(projectOwnerEmail);
        emailNotificationManager.saveDetails(emailNotBean);
        
      //http://redmine.nvish.com/redmine/issues/493
        StringBuilder confirmButtton = new StringBuilder("");
        confirmButtton.append(getText("logger.project.proposal.confirm.legalApproval"));
        if(proposalInitiation.getLegalApprovalStatus()==1)
        {
        	confirmButtton.append(" Status : May have to be Submitted");
        }
        else
        {
        	confirmButtton.append(" Status : Does not have to be Submitted");
        }
        
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
   				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), confirmButtton.toString(), project.getName(), 
   						project.getProjectID(), getUser().getID());
        
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
    				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.pib.cancel"), project.getName(), 
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
				SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.pib.enable"), project.getName(), 
						project.getProjectID(), getUser().getID());
    
    	return SUCCESS;
    }
    
    private boolean isMandatoryFieldsFilled(ProjectInitiation projectInitiation) {
        boolean filled = false;
        if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
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
        else
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
        return filled;
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
        String stageUrl="";
        if(project.getFieldWorkStudy()!=null && project.getFieldWorkStudy()==1)
        {
        	stageUrl = baseUrl+"/new-synchro/proposal-fieldwork-details!input.jspa?projectID=" + projectID;
        }
        else
        {
        	stageUrl = baseUrl+"/new-synchro/proposal-details!input.jspa?projectID=" + projectID;
        }

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
            SynchroLogUtils.PIBWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId());
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
                emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Approved ");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
                
            }
            //Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.waiver.approve"), project.getName(), 
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
                emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Methodology Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
            
          //Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.waiver.reject"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
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
					SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSubject("Action Required | Methodology Waiver Approval ");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.waiver.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
        
      
        //stageManager.sendNotification(getUser(),emailMessage);
       
        if(StringUtils.isNotBlank(pageRequest)) {
           return "pending-actions";
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
        String stageUrl="";
        if(project.getFieldWorkStudy()!=null && project.getFieldWorkStudy()==1)
        {
        	stageUrl = baseUrl+"/new-synchro/proposal-fieldwork-details!input.jspa?projectID=" + projectID;
        }
        else
        {
        	stageUrl = baseUrl+"/new-synchro/proposal-details!input.jspa?projectID=" + projectID;
        }

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
            SynchroLogUtilsNew.PIBKantarWaiverSave(pibKantarMethodologyWaiver_DB, pibKantarMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId());
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
                emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Approved");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }

          //Approve Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.kantar.approve"), project.getName(), 
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
                emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Rejected");
                emailNotBean.setEmailSender(adminEmail);
                emailNotBean.setEmailRecipients(waiverRequestorEmail);
                emailNotificationManager.saveDetails(emailNotBean);
            }
          //Reject Audit logs
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.kantar.reject"), project.getName(), 
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
            emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
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
					SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.kantar.request.inf"), project.getName(), 
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
            
           // This is done in case the Waiver Document attachment is added the status should not be changed to pending.
            if(StringUtils.isNotBlank(pibKantarMethodologyWaiver.getMethodologyDeviationRationale()))
            {
            	pibKantarMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
            }
            
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
            emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSubject("Action Required | Kantar Agency Waiver has been initiated");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(recp);
            emailNotificationManager.saveDetails(emailNotBean);
            
            //Audit logs for Waiver Send for Approval
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.kantar.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
        }
        if(StringUtils.isNotBlank(pageRequest)) {
            return "pending-actions";
         }
        //return SUCCESS;
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
        
      //http://redmine.nvish.com/redmine/issues/261 and http://redmine.nvish.com/redmine/issues/260
        projectInitiation.setBrief(SynchroUtils.fixBulletPoint(getRequest().getParameter("brief")));
        projectInitiation.setBriefText(getRequest().getParameter("briefText"));
        
        projectInitiation.setModifiedBy(getUser().getID());
        projectInitiation.setModifiedDate(System.currentTimeMillis());
        
        
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
        
        // Setting the Brief Legal Approver for EU Offline cases
        projectInitiation.setBriefLegalApproverOffline(getRequest().getParameter("briefLegalApproverOffline"));
        
        if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalDateBrief"))) {
            this.projectInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("legalApprovalDateBrief")));
        }
        
        this.pibManagerNew.updatePIBDetailsNew(projectInitiation);
        
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
        SynchroLogUtilsNew.PITSaveNew(project_DB, project, projectInitiation_DB, projectInitiation, null, null);
        
        String i18Text = getText("logger.project.saved.text");
        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.VIEWEDITPIT.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
        
        

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
    /**
     * This method will perform the notification activities for the To Do List Actions for each stage.
     *
     */
    public String sendNotification() {

        //EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,"pib.complete.notifyAgency.htmlBody","pib.complete.notifyAgency.subject");
        if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND_TO_PROJECT_OWNER"))
        {
            List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
            this.pibManagerNew.updateNotifyPO(projectID, 1);
            
            EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody, null, null);
            email = handleAttachments(email);
            stageManager.sendNotification(getUser(),email);

            
            //Sending Agency User Emails individually
            for(String agencyEmail : agencyEmails)
            {
                email = stageManager.populateNotificationEmail(agencyEmail, subject, messageBody, null, null);
                //TODO handle email attachments
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
            String description = getText("logger.pib.notify.owner");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
        									SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        											project.getProjectID(), getUser().getID(), userNameList);
        }
        else if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND_TO_SPI"))
        {
            List<String> agencyEmails = SynchroUtils.fetchAgencyUsers(recipients);
            this.pibManagerNew.updateNotifySPI(projectID, 1);
            EmailMessage email = stageManager.populateNotificationEmail(SynchroUtils.removeAgencyUsersFromRecipients(recipients), subject, messageBody, null, null);
            //TODO handle email attachments
            email = handleAttachments(email);
            stageManager.sendNotification(getUser(),email);

            //Sending Agency User Emails individually
            for(String agencyEmail : agencyEmails)
            {
                email = stageManager.populateNotificationEmail(agencyEmail, subject, messageBody, null, null);
                //TODO handle email attachments
                email = handleAttachments(email);
                stageManager.sendNotification(getUser(),email);
            }

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            emailNotBean.setEndmarketID(endMarketId);
            emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("SEND_TO_SPI");
            emailNotBean.setEmailSubject(subject);
            emailNotBean.setEmailSender(getUser().getEmail());
            emailNotBean.setEmailRecipients(recipients);
            emailNotificationManager.saveDetails(emailNotBean);
            
           //Audit Logs: Notify SP&I
            List<String> userNameList = SynchroUtils.fetchUserNames(recipients);
            String description = getText("logger.pib.notify.spi");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), 
                    		SynchroGlobal.Activity.NOTIFICATION.getId(), SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        							 project.getProjectID(), getUser().getID(), userNameList);
        }

        else
        {
            //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID=" + projectID;
            String baseUrl = URLUtils.getRSATokenBaseURL(request);
            String stageUrl = baseUrl+"/synchro/pib-details!input.jspa?projectID=" + projectID;

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
                        //TODO handle email attachments
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
                    //TODO handle email attachments
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
            }
            
            //Collects all email ids to which notification is sent for addition to Audit Logs
            StringBuilder userEmailList = new StringBuilder();
            try
            {
                // Automatic Notification for Project Owner
                if(project.getProjectOwner()!=null && project.getProjectOwner()>0 && !SynchroUtils.isReferenceID(project.getProjectOwner()))
                {
                    EmailMessage email = stageManager.populateNotificationEmail(userManager.getUser(project.getProjectOwner()).getEmail(), null, null,"pib.complete.notifyProjectOwner.htmlBody","pib.complete.notifyProjectOwner.subject");
                    //email.getContext().put("projectId", projectID);
                    email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                    email.getContext().put("projectName",project.getName());
                    email.getContext().put ("stageUrl",stageUrl);
                    //TODO handle email attachments
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
                    
                    userEmailList.append(userManager.getUser(project.getProjectOwner()).getEmail());
	            	userEmailList.append(",");
                }
                // Automatic Notification for Legal Users
                if(pibStakeholderList.getGlobalLegalContact()!=null && pibStakeholderList.getGlobalLegalContact() > 0 )
                {
                    EmailMessage email = stageManager.populateNotificationEmail(userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getEmail(), null, null,"pib.complete.LegalUser.htmlBody","pib.complete.LegalUser.subject");
                    //email.getContext().put("projectId", projectID);
                    email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                    email.getContext().put("projectName",project.getName());
                    email.getContext().put ("stageUrl",stageUrl);
                    //TODO handle email attachments
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
                    
                    userEmailList.append(userManager.getUser(pibStakeholderList.getGlobalLegalContact()).getEmail());
	            	userEmailList.append(",");
                }
                // Automatic Notification for Procurement and Communication Agency Users
                if((pibStakeholderList.getGlobalProcurementContact()!=null && pibStakeholderList.getGlobalProcurementContact() > 0) || (pibStakeholderList.getGlobalCommunicationAgency()!=null && pibStakeholderList.getGlobalCommunicationAgency() > 0))
                {
                    StringBuffer proCAUsers = new StringBuffer();
                    if(pibStakeholderList.getGlobalProcurementContact()!=null && pibStakeholderList.getGlobalProcurementContact() > 0)
                    {
                        proCAUsers.append(userManager.getUser(pibStakeholderList.getGlobalProcurementContact()).getEmail());
                    
                        userEmailList.append(userManager.getUser(pibStakeholderList.getGlobalProcurementContact()).getEmail());
    	            	userEmailList.append(",");
                    }
                    if(pibStakeholderList.getGlobalCommunicationAgency()!=null && pibStakeholderList.getGlobalCommunicationAgency() > 0)
                    {
                        if(proCAUsers.length()>0)
                        {
                            proCAUsers.append(","+userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getEmail());
                        
                            userEmailList.append(userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getEmail());
        	            	userEmailList.append(",");
                        }
                        else
                        {
                            proCAUsers.append(userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getEmail());
                            
                            userEmailList.append(userManager.getUser(pibStakeholderList.getGlobalCommunicationAgency()).getEmail());
        	            	userEmailList.append(",");
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
            if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()!=SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                stageManager.updateStageStatus(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
            }

            // Update the project status to IN PROGRESS once the PIB is completed.
            //	synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_OPEN.ordinal());
            synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal());
            
            if(StringUtils.isNotBlank(recipients)){
            	userEmailList.append(recipients);	
            }
            List<String> userNameList = SynchroUtils.fetchUserNames(userEmailList!=null?userEmailList.toString():StringUtils.EMPTY);
            String description = getText("logger.pib.notify.complete");
                    SynchroLogUtils.addNotificationLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), 
                    		SynchroGlobal.Activity.NOTIFICATION.getId(), SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
        							 project.getProjectID(), getUser().getID(), userNameList);
                    
        }


        return SUCCESS;
    }

    /**
     * This method will move the stage from PIB to Proposal
     * @return
     */
    public String moveToNextStage() 
    {
    	LOG.info("Inside moveToNextStage--- "+ projectID); 
        stageManager.updateStageStatus(projectID, endMarketId, 1, SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal(),getUser(), null);
        if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()!=SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
        {
            stageManager.updateStageStatus(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
        }
        // Update the project status to IN PROGRESS once the PIB is completed.
        //	synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_OPEN.ordinal());
        synchroProjectManagerNew.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal());
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
            /*proposalManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID(), new Long("-1"));
            attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, endMarketId, fieldCategoryId));
            */
        	
         // This has been done so that in case the end Market is changed for a project, then the attachments should remain intact.
            proposalManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,new Long("-1"),fieldCategoryId,getUser().getID(), new Long("-1"));
            attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, new Long("-1"), fieldCategoryId));
            
          //Add Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment" + "- " +attachFileFileName;
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
            										SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), description, project.getName(), 
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
									SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), description, project.getName(), 
											project.getProjectID(), getUser().getID());
        }
        catch (Exception e) {
            LOG.error("Exception while removing attachment Id --"+ attachmentId);
        }
        return SUCCESS;
        /* Map<String, Object> result = new HashMap<String, Object>();
       JSONObject out = new JSONObject();
       try {
           pibManager.removeAttachment(attachmentId);
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
    }




    public String exportToWordPIBOLD()
    {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph tmpParagraph = document.createParagraph();
        XWPFRun tmpRun = tmpParagraph.createRun();
        tmpRun.setText("Project Description : "+ project.getDescriptionText());
        tmpRun.addBreak();
        /*tmpRun.setText("Business Question : "+ projectInitiation.getBizQuestionText());
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
        tmpRun.addBreak();*/
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

        //tmpRun.setFontSize(18);
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

    public String exportToWordPIBOLD1()
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

        return null;
    }
    public String exportToWordPIB() throws InvalidFormatException, Docx4JException
    {

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

        //https://svn.sourcen.com/issues/19491 - Add the Project Name and code
        ExportWordUtil.createPageHeader(wordMLPackage,project.getName() +" ("+ SynchroUtils.generateProjectCode(projectID) + ")");
        ExportWordUtil.createPageHeader(wordMLPackage,"Project Initiating Brief (PIB)");

        ExportWordUtil.createContentHeader(wordMLPackage, "Project Description:");
        if(StringUtils.isEmpty(project.getDescription()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
            
            try
            {
            	ExportWordUtil.createContent(wordMLPackage,project.getDescription().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Business Question:");
        if(StringUtils.isEmpty(projectInitiation.getBizQuestion()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
            try
            {
            	ExportWordUtil.createContent(wordMLPackage, projectInitiation.getBizQuestion().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	            /*if(projectInitiation.getBizQuestion().contains("<br") && !projectInitiation.getBizQuestion().contains("<br>"))
	            {
	            	ExportWordUtil.createContent(wordMLPackage, projectInitiation.getBizQuestion().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;").replaceAll("<br","<br />"));
	            }*/
            	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Research Objectives(s):");
        if(StringUtils.isEmpty(projectInitiation.getResearchObjective()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
            try
            {
            	ExportWordUtil.createContent(wordMLPackage, projectInitiation.getResearchObjective().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Action Standard(s):");
        if(StringUtils.isEmpty(projectInitiation.getActionStandard()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
            try
            {
            	 ExportWordUtil.createContent(wordMLPackage, projectInitiation.getActionStandard().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Methodology Approach and Research Design:");
        if(StringUtils.isEmpty(projectInitiation.getResearchDesign()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
           
            try
            {
            	 ExportWordUtil.createContent(wordMLPackage, projectInitiation.getResearchDesign().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Sample Profile (Research):");
        if(StringUtils.isEmpty(projectInitiation.getSampleProfile()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
           
            try
            {
            	 ExportWordUtil.createContent(wordMLPackage, projectInitiation.getSampleProfile().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Stimulus Material:");
        if(StringUtils.isEmpty(projectInitiation.getStimulusMaterial()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
           
            try
            {
            	 ExportWordUtil.createContent(wordMLPackage, projectInitiation.getStimulusMaterial().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Other Comments:");
        if(StringUtils.isEmpty(projectInitiation.getOthers()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
            
            try
            {
            	ExportWordUtil.createContent(wordMLPackage, projectInitiation.getOthers().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        ExportWordUtil.createContentHeader(wordMLPackage,"Other Reporting Requirements:");
        if(StringUtils.isEmpty(pibReporting.getOtherReportingRequirements()))
        {
            ExportWordUtil.createContent(wordMLPackage," ");
        }
        else
        {
            
            try
            {
            	ExportWordUtil.createContent(wordMLPackage, pibReporting.getOtherReportingRequirements().replaceAll("&nbsp;", " ").replaceAll("& ", "&amp; ").replaceAll("&bull;", "&#8226; ").replaceAll("&lsquo;", "&#8216; ").replaceAll("&rsquo;", "&#8217; ").replaceAll("&eacute;", "&#233; ").replaceAll("&ndash;", "&#8211; ").replaceAll("&uacute;", "&#250; ").replaceAll("&ldquo;", "&#8220; ").replaceAll("&rdquo;", "&#8221; ").replaceAll("(&(?!#))", "&amp;"));
            }
            catch(Docx4JException de)
            {
	           	de.printStackTrace();
            	ExportWordUtil.createContent(wordMLPackage," ");
            }
        }

        try
        {
            String root = ServletActionContext.getServletContext().getRealPath("/");

            File path = new File(root + "/pibwordexport");

            if (!path.exists()) {
                path.mkdirs();
            }
            // File generated = new File(root + "/PIBDoc.docx");
            File generated = new File(path + File.separator + "PIBDoc.docx");
            //final File generated = new File("c://PIBDoc1.docx");//File.createTempFile("PIBDoc", ".docx");
            //	if (!generated.exists()) {
            //		generated.mkdirs();
            //  }
            response.setContentType("application/msword");
            response.addHeader("Content-Disposition", "attachment; filename=PIBWord.docx");
            wordMLPackage.save(generated);
            FileInputStream fis = new FileInputStream(generated);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
                // System.out.println( buf.length);

            } catch (IOException ex) {
            }
            byte[] bytes = bos.toByteArray();
            response.getOutputStream().write(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Docx4JException e) {
            e.printStackTrace();
        }
        
      //PIB Export to Word Audit logs
        String description = SynchroGlobal.LogProjectStage.PIB.getDescription() + "- " + getText("logger.project.export.word"); 
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
								SynchroGlobal.LogProjectStage.PIB.getId(), description, project.getName(), 
										project.getProjectID(), getUser().getID());

        return null;
    }

    



    public void setSynchroProjectManagerNew(final ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
    }

    public void setPibManagerNew(final PIBManagerNew pibManagerNew) {
        this.pibManagerNew = pibManagerNew;
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

    
    public Long getAttachmentFieldID() {
		return attachmentFieldID;
	}

	public void setAttachmentFieldID(Long attachmentFieldID) {
		this.attachmentFieldID = attachmentFieldID;
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

    public Boolean getIsProposalAwarded() {
        return isProposalAwarded;
    }

    public void setIsProposalAwarded(Boolean isProposalAwarded) {
        this.isProposalAwarded = isProposalAwarded;
    }

    public ProposalManagerNew getProposalManagerNew() {
        return proposalManagerNew;
    }

    public void setProposalManagerNew(ProposalManagerNew proposalManagerNew) {
        this.proposalManagerNew = proposalManagerNew;
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

    public String getSubjectSendToSPI() {
        return subjectSendToSPI;
    }

    public void setSubjectSendToSPI(String subjectSendToSPI) {
        this.subjectSendToSPI = subjectSendToSPI;
    }

    public String getMessageBodySendToSPI() {
        return messageBodySendToSPI;
    }

    public void setMessageBodySendToSPI(String messageBodySendToSPI) {
        this.messageBodySendToSPI = messageBodySendToSPI;
    }

    public EmailNotificationManager getEmailNotificationManager() {
        return emailNotificationManager;
    }

    public void setEmailNotificationManager(
            EmailNotificationManager emailNotificationManager) {
        this.emailNotificationManager = emailNotificationManager;
    }

    public String getSubjectAdminPIBComplete() {
        return subjectAdminPIBComplete;
    }

    public void setSubjectAdminPIBComplete(String subjectAdminPIBComplete) {
        this.subjectAdminPIBComplete = subjectAdminPIBComplete;
    }

    public String getMessageBodyAdminPIBComplete() {
        return messageBodyAdminPIBComplete;
    }

    public void setMessageBodyAdminPIBComplete(String messageBodyAdminPIBComplete) {
        this.messageBodyAdminPIBComplete = messageBodyAdminPIBComplete;
    }

    public String getAdminPIBCompleteRecipents() {
        return adminPIBCompleteRecipents;
    }

    public void setAdminPIBCompleteRecipents(String adminPIBCompleteRecipents) {
        this.adminPIBCompleteRecipents = adminPIBCompleteRecipents;
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

    public String getKantarMethodologyWaiverAction() {
        return kantarMethodologyWaiverAction;
    }

    public void setKantarMethodologyWaiverAction(
            String kantarMethodologyWaiverAction) {
        this.kantarMethodologyWaiverAction = kantarMethodologyWaiverAction;
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

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public ProposalInitiation getProposalInitiation() {
		return proposalInitiation;
	}

	public void setProposalInitiation(ProposalInitiation proposalInitiation) {
		this.proposalInitiation = proposalInitiation;
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
