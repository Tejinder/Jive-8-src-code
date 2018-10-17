package com.grail.synchro.action;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
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
import com.grail.synchro.beans.TPDSKUDetails;
import com.grail.synchro.beans.TPDSummary;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectSpecsManagerNew;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.manager.TPDSummaryManager;
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
import com.jivesoftware.community.action.util.ActionUtils;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: tejinder
 * @since: 1.0
 * Action class for Project Close Stage
 */
public class TPDSummaryAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(TPDSummaryAction.class);
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
	
	// This map will contain the attachments for each row for TPD SKU Detail section
	Map<Integer, List<AttachmentBean>> tpdSKUAttachmentMap = new HashMap<Integer, List<AttachmentBean>>();
	
	private List<AttachmentBean> tpdSummaryLegalApproverAtt = new ArrayList<AttachmentBean>();
	private Map<Integer, Map<Integer, List<Long>>> reportSummaryAttachments = new HashMap<Integer, Map<Integer,List<Long>>>();
	private Map<Long,Long> attachmentUser;
	private ReportSummaryInitiation reportSummaryInitiation;
	
	private String methodologyWaiverAction;
	private String kantarMethodologyWaiverAction;
	private EmailNotificationManager emailNotificationManager;
	
	private SynchroUtils synchroUtils;
	
	private String redirectURL;
	private List<Long> endMarketIds;
	
	private TPDSummary tpdSummary;
	private TPDSummary tpdSummary_DB;
	
	private List<TPDSKUDetails> tpdSKUDetails;
	
	private TPDSummaryManager tpdSummaryManager;
	private Map<Long, List<User>> endMarketLegalApprovers;
	
	/*private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;*/
    private Long attachmentId;
    private Long attachmentFieldID;
    private String attachmentName;
    private Long fieldCategoryId;
    
    
    private long[] removeAttachID;
    private File[] attachFile;
    
    private File[] tpdattachFile;
    
    private String[] attachFileContentType;
    private String[] attachFileFileName;
    
    private String[] tpdattachFileContentType;
    private String[] tpdattachFileFileName;
    
    private int repTypeId;
    private int reportOrderId;
    
    private int rowId = -1;
    
    private String tpdSummaryRSLegalApprover;
    private List<AttachmentBean> tpdSummaryRSLegalApproverAtt = new ArrayList<AttachmentBean>();
    
    private boolean showTPDSummaryFields = true;
    
	
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
                    
                    
                }
                pibMethodologyWaiver = this.pibManagerNew.getPIBMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
                projectCostDetailsList = this.synchroProjectManagerNew.getProjectCostDetails(projectID);
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
                
                List<TPDSummary> tpdSummaryList = tpdSummaryManager.getTPDSummaryDetails(projectID);
                tpdSKUDetails = tpdSummaryManager.getTPDSKUDetails(projectID);
                if(tpdSummaryList!=null && tpdSummaryList.size()>0)
                {
                	tpdSummary= tpdSummaryList.get(0);
                	isSave = false;
                }
                else
                {
                	tpdSummary= new TPDSummary();
                	isSave = true;
                }
                if(tpdSKUDetails==null)
                {
                	tpdSKUDetails = new ArrayList<TPDSKUDetails>();
                }
                
                List<TPDSummary> tpdSummaryList_DB = tpdSummaryManager.getTPDSummaryDetails(projectID);
            	if( tpdSummaryList_DB != null && tpdSummaryList_DB.size() > 0) {
            		tpdSummary_DB = tpdSummaryList_DB.get(0);   		
            	}
            	
                //attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
                attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(projectID, new Long("-1"));
                
                if(tpdSKUDetails!=null && tpdSKUDetails.size()>0)
                {
                	tpdSKUAttachmentMap = this.reportSummaryManagerNew.getTPDSKUDocumentAttachment(projectID, new Long("-1"),tpdSKUDetails);
                	/*if(tpdSKUAttachmentMap!=null && tpdSKUAttachmentMap.size()>0)
                	{
                		attachmentMap.putAll(tpdSKUAttachmentMap);
                	}*/
                }
                
                if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_LEGAL_APPROVAL.getId())!=null)
                {
                	tpdSummaryLegalApproverAtt = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_LEGAL_APPROVAL.getId());
                }
                List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
                for(Integer i : attachmentMap.keySet())
                {
                	abList.addAll(attachmentMap.get(i));
                }
                attachmentUser = pibManagerNew.getAttachmentUser(abList);
                reportSummaryAttachments = this .reportSummaryManagerNew.getReportSummaryAttachmentDetails(projectID);
                stageId = SynchroGlobal.getProjectActivityTab().get("projectEvaluation");
                
               
                if(reportSummaryAttachments!=null && reportSummaryAttachments.get(SynchroGlobal.ReportType.TPD_SUMMARY.getId())!=null)
                {
                	showTPDSummaryFields = false;
                }
                
                // If the Report Summary stage hasn't crossed then dont display the TPD Summary legal approver attachment options
                if(project.getStatus()!=null && project.getStatus().intValue() < SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal())
                {
                	
                	showTPDSummaryFields = false;
                	
                }
               
            	
                endMarketLegalApprovers = getSynchroUtils().getLegalApprovers();
    			
    		//	editStage=SynchroPermHelper.canEditStageDocument(ribDocument,projectID);
    			//editStage=true;
              //  editStage=SynchroPermHelper.canEditProjectEvaluation(projectID);
    			 
    			
    	
        }
      
        if(getRequest().getMethod() == "POST" && !getRequest().getServletPath().contains("updateProposalDetails.jspa")) {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.tpdSummary);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the TPD Summary Bean");
                input();
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
                    
        }
    }

    public String input() {

    	if (SynchroPermHelper.hasProjectAccessNew1(projectID) ||  SynchroPermHelper.userTypeAccess(projectID) || SynchroPermHelper.isLegaUserType() || SynchroPermHelper.canViewTPDSubmission()) {
    		//return INPUT;
    		redirectURL = ProjectStage.generateURLNew(project);
    		
    		// This check for moving the project out of TPD Summary Dashboard once the Status is changed to Don't have to be submitted 
    		//if(proposalInitiation!=null && proposalInitiation.getLegalApprovalStatus()==2)
    		if(tpdSummary!=null && tpdSummary.getLegalApprovalStatus()!=null && tpdSummary.getLegalApprovalStatus()==2)
    		{
    			//return UNAUTHORIZED;
    			//return "tpd-dashboard";
    		}
    		
    		// Only System Admin can access the Cancel Projects
        	if(project.getIsCancel())
        	{
        		if(!SynchroPermHelper.isSystemAdmin())
        		{
        			return UNAUTHORIZED;
        		}
        	}
        	
        	
        	return INPUT;
        	/*if(redirectURL.contains("project-close"))
            {
            	return INPUT;
        		//return SUCCESS;
            }
            else
            {
            	
            	return "redirectNextStage";
            }*/
    	}
    	else
    	{
    		return UNAUTHORIZED;
    	}
    }

    public String execute(){
        
    	System.out.println("TPD SUmmarry Project Id --->>"+ tpdSummary.getProjectID());
    	tpdSummary.setCreationBy(getUser().getID());
    	tpdSummary.setCreationDate(System.currentTimeMillis());
        
    	tpdSummary.setModifiedBy(getUser().getID());
    	tpdSummary.setModifiedDate(System.currentTimeMillis());
    	
    	 if(StringUtils.isNotBlank(getRequest().getParameter("tpdModificationDate"))) {
             this.tpdSummary.setTpdModificationDate(DateUtils.parse(getRequest().getParameter("tpdModificationDate")));
         }
    	if(isSave)
    	{
    		tpdSummaryManager.save(tpdSummary);
    	}
    	else
    	{
    		tpdSummaryManager.update(tpdSummary);
    	}
    	
    	
   	 String[] subDates = getRequest().getParameterValues("submissionDates");
     String[] skuDetails = getRequest().getParameterValues("skuDetails");
     System.out.println("TPD SUmmarry subDates>>"+ subDates);
     System.out.println("TPD SUmmarry skuDetails>>"+ skuDetails);
     
     //http://redmine.nvish.com/redmine/issues/497 System Owner and Admin User can edit the disabled rows as well.
    /* if((SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner()))
     {
    	 tpdSummaryManager.deleteTPDSKUDetails(projectID);
     }
     
     if(StringUtils.isNotBlank(getRequest().getParameter("isRowSave")) && getRequest().getParameter("isRowSave").equalsIgnoreCase("yes")) 
	{
    	 tpdSummaryManager.deleteTPDSKUDetails(projectID);
    }
    */
     tpdSummaryManager.deleteTPDSKUDetails(projectID);
     if(StringUtils.isNotBlank(getRequest().getParameter("isRowSave")) && getRequest().getParameter("isRowSave").equalsIgnoreCase("no"))
     {
    	 System.out.println("Indside if");
     }
     else
     {
    	 saveTPDSKUDetails(tpdSummary);
     }
     
     // This is done as some attachments are directly added through addAttachment Method
 	this.reportSummaryManagerNew.deleteReportSummaryDetailsNew(projectID);
 	
 	// This is for deleting the attachments added for a row and will get triggered when a row is deleted
 	 deleteRowAttachments();
     saveReportSummaryDetails();
     
     //Audit Log: Add User Audit details for TPD Summary Save               
	 SynchroLogUtilsNew.TPDSummarySaveNew(tpdSummary_DB, tpdSummary, project);
	 
     //Audit Logs: TPD SUMMARY SAVE
     String i18Text = getText("logger.tpd.summary.saved.text");
     SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.TPD_SUMMARY.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());
    	
    	return SUCCESS;
    }
    
    
    private void saveTPDSKUDetails(TPDSummary tpdSummary)
    {
    	tpdSKUDetails = new ArrayList<TPDSKUDetails>();
    	
    	 String[] subDates = getRequest().getParameterValues("submissionDates");
         String[] skuDetails = getRequest().getParameterValues("skuDetails");
         
         String[] taoCodes = getRequest().getParameterValues("taoCodes");
         String[] hasProductionModifications = getRequest().getParameterValues("hasProductModifications");
         String[] tpdModifcationDates = getRequest().getParameterValues("tpdModificationDates");
         
         //String[] sameAsPrevSubmitted = getRequest().getParameterValues("sameAsPrevSubmitted");
         String[] sameAsPrevSubmitted = null;
         
         String sameAsPrevSubmittedHidden = getRequest().getParameter("sameAsPrevSubmittedHidden");
         if(StringUtils.isNotBlank(sameAsPrevSubmittedHidden))
         {
        	 sameAsPrevSubmitted=sameAsPrevSubmittedHidden.split(",");
         }
         
         
         String[] rowIds = getRequest().getParameterValues("rowIds");
         
         String[] savedRowIds = getRequest().getParameterValues("savedRowIds");
         
         System.out.println("TPD SUmmarry savedRowIds>>"+ savedRowIds);
         
         List<String> savedRowIDList = new ArrayList<String>();
         
         if(savedRowIds!=null)
         {
        	 for(int i=0;i<savedRowIds.length;i++)
      		{
        		 savedRowIDList.add(savedRowIds[i]);
      			
      		}
         }
         
         String[] updatedRowIds = null;
         List<String> updatedRowID = new ArrayList<String>();
         
         if(rowIds!=null && rowId >0)
     	 {
        	
        	 for(int i=0;i<rowIds.length;i++)
     		{
     			if(rowIds[i].equalsIgnoreCase(rowId+""))
     			{
     				
     			}
     			else
     			{
     				updatedRowID.add(rowIds[i]);
     			}
     		}
        	 
        	 updatedRowIds = updatedRowID.toArray(new String[0]);
     	 }
         else
         {
        	 updatedRowIds = rowIds;
         }
         
         
         
         String[] skuRowSave = getRequest().getParameterValues("skuRowSave");
         
         System.out.println("TPD SUmmarry subDates>>"+ subDates);
         System.out.println("TPD SUmmarry skuDetails>>"+ skuDetails);
         System.out.println("TPD SUmmarry hasProductionModifications>>"+ hasProductionModifications);
         System.out.println("TPD SUmmarry tpdModifcationDates>>"+ tpdModifcationDates);
         
         System.out.println("TPD SUmmarry sameAsPrevSubmitted>>"+ sameAsPrevSubmitted);
         
         Date tpdSubmissionDate = null;
         
         if(taoCodes!=null && taoCodes.length>0)
       	 {
        	 for(int i=0;i<taoCodes.length;i++)
        	 {
	        	 TPDSKUDetails skuBean = new TPDSKUDetails();
	         	 skuBean.setProjectID(projectID);
	         	 skuBean.setTaoCode(taoCodes[i]);
	         	 try
	         	 {
		         	 if(subDates!=null && subDates[i]!=null && StringUtils.isNotBlank(subDates[i]))
		         	 {
		         		skuBean.setSubmissionDate(DateUtils.parse(subDates[i]));
		         		
		         		// This logic is as per http://redmine.nvish.com/redmine/issues/497.
		         		
		         	// The Submission Date from the first row should be displayed always
		         		if(i==0)
		         		{
		         			tpdSubmissionDate = DateUtils.parse(subDates[i]);
		         		}
		         		else
		         		{
		         			/*if(tpdSubmissionDate!=null && tpdSubmissionDate.before(DateUtils.parse(subDates[i])))
		         			{
		         				tpdSubmissionDate = DateUtils.parse(subDates[i]);
		         			}*/
		         			
		         			
		         		}
		         	 }
		         	 if(hasProductionModifications!=null && hasProductionModifications[i]!=null && StringUtils.isNotBlank(hasProductionModifications[i]))
		         	 {
		         		skuBean.setHasProductModification(Integer.parseInt(hasProductionModifications[i]));
		         	 }
		         	 
		         	 if(tpdModifcationDates!=null && tpdModifcationDates[i]!=null && StringUtils.isNotBlank(tpdModifcationDates[i]))
		         	 {
		         		skuBean.setTpdModificationDate(DateUtils.parse(tpdModifcationDates[i]));
		         	 }
		         	 
		         	// This is done for preserving the freeze status of the row in case Attachment is added 
		         	try
		         	{
			         	/* if(skuRowSave!=null && skuRowSave[i]!=null && skuRowSave[i].equalsIgnoreCase("yes"))
			         	{
			         		//skuBean.setIsRowSaved(true);
			         		
			         	}*/
			         	// This is done in case user has created more than 2 rows without saving and then delete one of the row, then the order of earlier row saved is not getting preserved.
		         		 if(updatedRowIds!=null && updatedRowIds[i]!=null && StringUtils.isNotBlank(updatedRowIds[i]) && savedRowIDList.contains(updatedRowIds[i]))
			         	 {
			         		skuBean.setIsRowSaved(true);
			         		
			         	 }
		         	}
		         	catch(Exception e)
		         	{
		         		
		         	}
		         	 
		         	try
		         	{
			         	 if(sameAsPrevSubmitted!=null && sameAsPrevSubmitted[i]!=null && sameAsPrevSubmitted[i].equalsIgnoreCase("yes"))
			         	 {
			         		skuBean.setSameAsPrevSubmitted(true);
			         	 }
			         	else
			         	{
			         		skuBean.setSameAsPrevSubmitted(false);
			         	}
		         	}
		         	catch(Exception e)
		         	{
		         		skuBean.setSameAsPrevSubmitted(false);
		         	}
		         	/* if(rowIds!=null && rowIds[i]!=null && StringUtils.isNotBlank(rowIds[i]))
		         	 {
		         		skuBean.setRowId(Integer.parseInt(rowIds[i]));
		         	 }*/
		         	if(updatedRowIds!=null && updatedRowIds[i]!=null && StringUtils.isNotBlank(updatedRowIds[i]))
		         	 {
		         		skuBean.setRowId(Integer.parseInt(updatedRowIds[i]));
		         		
		         	 }
		         	
		         	
	         	 }
	         	 catch(Exception e)
	         	 {
	         		 e.printStackTrace();
	         	 }
	         	tpdSKUDetails.add(skuBean);
        	 }
       	 }
         
         if(tpdSKUDetails!=null && tpdSKUDetails.size()>0)
         {
        	for(TPDSKUDetails skuBean: tpdSKUDetails)
        	{
        		// This is done for saving the flag in case the save button is clicked, so that on adding attachment in TPD SKU Details the row should remain enabled. 
        		if(StringUtils.isNotBlank(getRequest().getParameter("isRowSave")) && getRequest().getParameter("isRowSave").equalsIgnoreCase("yes")) 
        		{
        			 skuBean.setIsRowSaved(true);
        	    }
        		tpdSummaryManager.saveSKU(skuBean);
        	}
        	
        	// This has been done to update the Previously Submitted details for Previously submitted and Last submission Date fields
        	if(tpdSubmissionDate==null)
        	{
        		tpdSummary.setLastSubmissionDate(new Date());
        	}
        	else
        	{
        		tpdSummary.setLastSubmissionDate(tpdSubmissionDate);
        	}
        	tpdSummaryManager.updateTPDPrevSubmission(tpdSummary);
         }
    	
    }
    
    
    public void handleAttachments() {
		
        // add any attachments that we've been told to add
        if (attachFile != null && attachFile.length != 0) {
            // Check to see that the incoming request is a multipart request.
            if (ActionUtils.isMultiPart(ServletActionContext.getRequest())) {
                addAttachments();
            }
            else {
                addFieldError("attachFile", getText("attach.err.upload_errors.text"));
            }
        }
        
        if (tpdattachFile != null && tpdattachFile.length != 0) {
            // Check to see that the incoming request is a multipart request.
            if (ActionUtils.isMultiPart(ServletActionContext.getRequest())) {
            	addTPDAttachments();
            }
            else {
                addFieldError("attachFile", getText("attach.err.upload_errors.text"));
            }
        }
        
        if(removeAttachID!=null && removeAttachID.length > 0)
        {
        	for(int i=0;i<removeAttachID.length;i++)
        	{
        		try
        		{
        			pibManagerNew.removeAttachment(removeAttachID[i]);
        		}
        		catch (Exception e) {
                    LOG.error("Exception while removing attachment Id --"+ removeAttachID[i]);
                }
        	}
        }
}

 protected void addAttachments() throws UnauthorizedException {
        if (attachFile == null) {
            return;
        }
        log.debug("attachFile size was " + attachFile.length);

        for (int i = 0; i < attachFile.length; i++) {	            
            File file = attachFile[i];
            if (file == null) {
                log.debug("File was null, skipping");
                continue;
            }

            log.debug("File size was " + file.length());
            String fileName = attachFileFileName[i];
            String contentType = attachFileContentType[i];
            try
            {
            	//projectWaiverManager.addAttachment(attachFile[i], attachFileFileName[i], attachFileContentType[i], projectWaiverID, getUser().getID());
            	//attachments = projectWaiverManager.getFieldAttachments(projectWaiverID);
            	
            	pibManagerNew.addAttachment(attachFile[i], attachFileFileName[i], attachFileContentType[i], projectID, new Long("-1"),new Long(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_LEGAL_APPROVAL.getId()),getUser().getID());
                attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, new Long("-1"), new Long(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_LEGAL_APPROVAL.getId())));
                
            }
            catch (AttachmentException ae) {
               log.error("AttachmentException - " + ae.getMessage());
               //handleAttachmentException(file, fileName, contentType, ae);
            } catch (UnauthorizedException ue) {
            	log.error("UnauthorizedException - " + ue.getMessage());
            } catch (Exception e) {
            	log.error("Exception - " + e.getMessage());
            }
        }
        
       
    }
 
 
 protected void addTPDAttachments() throws UnauthorizedException {
     if (tpdattachFile == null) {
         return;
     }
     log.debug("tpdattachFile size was " + tpdattachFile.length);
     
     boolean isTPDRowSaved = false;

     for (int i = 0; i < tpdattachFile.length; i++) {	            
         File file = tpdattachFile[i];
         if (file == null) {
             log.debug("File was null, skipping");
             continue;
         }

         log.debug("File size was " + file.length());
         String fileName = tpdattachFileFileName[i];
         String contentType = tpdattachFileContentType[i];
         try
         {
         	
         //	pibManagerNew.addAttachment(attachFile[i], attachFileFileName[i], attachFileContentType[i], projectID, new Long("-1"),new Long(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_LEGAL_APPROVAL.getId()),getUser().getID());
          //   attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, new Long("-1"), new Long(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_LEGAL_APPROVAL.getId())));
        	 long attachId = reportSummaryManagerNew.addAttachment(tpdattachFile[i], tpdattachFileFileName[i], tpdattachFileContentType[i], projectID,new Long("-1"),new Long(SynchroGlobal.SynchroAttachmentObject.TPD_SUMMARY_REPORT.getId()),getUser().getID());
	         List<ReportSummaryDetails> rsdList = this.reportSummaryManagerNew.getReportSummaryDetails(projectID);
	         
	         reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	         
	         if(rsdList==null || rsdList.size()==0)
	         {
	        	 int reportOrderCounter = 1;
	        	 ReportSummaryDetails rsbean = new ReportSummaryDetails();
	    		 rsbean.setProjectID(projectID);
	    		 rsbean.setReportType(SynchroGlobal.ReportType.FULL_REPORT_BLANK.getId());
	    		 rsbean.setLegalApprover("");
	    		 rsbean.setReportOrderId(reportOrderCounter);
	    		 reportOrderCounter++;
	    		 reportSummaryDetailsList.add(rsbean);
	    		 
	    		 rsbean = new ReportSummaryDetails();
	    		 rsbean.setProjectID(projectID);
	    		 rsbean.setReportType(SynchroGlobal.ReportType.IRIS_SUMMARY.getId());
	    		 rsbean.setLegalApprover("");
	    		 rsbean.setReportOrderId(reportOrderCounter);
	    		
	    		 reportOrderCounter++;
	    		 reportSummaryDetailsList.add(rsbean);
	    		 
	    		 rsbean = new ReportSummaryDetails();
	    		 rsbean.setProjectID(projectID);
	    		 rsbean.setReportType(SynchroGlobal.ReportType.TPD_SUMMARY.getId());
	    		 rsbean.setLegalApprover(tpdSummaryRSLegalApprover);
	    		 
	    		 if(StringUtils.isNotBlank(getRequest().getParameter("tpdSummaryRSLegalApprovalDate")))
	         	 {
	    			 rsbean.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("tpdSummaryRSLegalApprovalDate")));
	         	
	         	 }
	    		 rsbean.setReportOrderId(reportOrderCounter);
	    		
	    		 List<Long> atIds  = new ArrayList<Long>();
	    		 atIds.add(new Long(attachId));
	    		 rsbean.setAttachmentId(atIds);
	    		 
	    		 reportOrderCounter++;
	    		 reportSummaryDetailsList.add(rsbean);
	    	
	    		 this.reportSummaryManagerNew.saveReportSummaryDetailsNew(reportSummaryDetailsList);
	    		 isTPDRowSaved = true;
	         }
	         else
	         {
	        	 
	        	 ReportSummaryDetails rsbean = new ReportSummaryDetails();
	    		 rsbean.setProjectID(projectID);
	    		 rsbean.setReportType(SynchroGlobal.ReportType.TPD_SUMMARY.getId());
	    		 rsbean.setLegalApprover(tpdSummaryRSLegalApprover);
	    		
	    		 Integer maxReportOrderId = this.reportSummaryManagerNew.getMaxReportOrderId(projectID);
	    		 
	    		 if(StringUtils.isNotBlank(getRequest().getParameter("tpdSummaryRSLegalApprovalDate")))
	    		 {
	    			 rsbean.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("tpdSummaryRSLegalApprovalDate")));
	    		 }
	    			 
	    				 
	    		 List<Long> atIds  = new ArrayList<Long>();
	    		 atIds.add(new Long(attachId));
	    		 rsbean.setAttachmentId(atIds);
	    		 
	    		 
	    		 if(isTPDRowSaved)
	    		 {
	    			 rsbean.setReportOrderId(maxReportOrderId);
	    			 reportSummaryDetailsList.add(rsbean);
	    			 this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
	    		 }
	    		 else
	    		 {
	    			 rsbean.setReportOrderId(++maxReportOrderId);
	    			 reportSummaryDetailsList.add(rsbean);
	    			 this.reportSummaryManagerNew.saveReportSummaryDetailsNew(reportSummaryDetailsList);
	    			 isTPDRowSaved = true;
	    			 
	    		 }
	    		 
	    		 
	         }
         }
         catch (AttachmentException ae) {
            log.error("AttachmentException - " + ae.getMessage());
            //handleAttachmentException(file, fileName, contentType, ae);
         } catch (UnauthorizedException ue) {
         	log.error("UnauthorizedException - " + ue.getMessage());
         } catch (Exception e) {
         	log.error("Exception - " + e.getMessage());
         }
     }
     
    
 }
    
    public String addAttachment() throws UnsupportedEncodingException {
	   if (attachFile == null) {
		   return SUCCESS;
       }
        Map<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i < attachFile.length; i++)
	    {	 
	        
        	if(rowId >0)
        	{
        		System.out.print("rowId ---"+ rowId);
        		
        		 List<TPDSKUDetails> tpdSkuDetailList = tpdSummaryManager.getTPDSKUDetailsRowId(projectID, rowId);
        		 if(tpdSkuDetailList!=null && tpdSkuDetailList.size()>0)
        		 {
        			 
        		 }
        		 else
        		 {
        			 TPDSKUDetails tpdSKUDetails = new TPDSKUDetails();
        			 tpdSKUDetails.setProjectID(projectID);
        			 tpdSKUDetails.setRowId(rowId);
        			 tpdSummaryManager.saveSKU(tpdSKUDetails);
        		 }
        		try
        		{
        			long attachId = reportSummaryManagerNew.addTPDSKUAttachment(attachFile[i], attachFileFileName[i], attachFileContentType[i], projectID,new Long("-1"),fieldCategoryId,getUser().getID(), rowId);
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
        	}
        	
        	else
        	{
	        	try
		        {
		        	System.out.print("repTypeId ---"+ repTypeId);
		        	System.out.print("repOrderId ---"+reportOrderId);
		        	//long attachId = reportSummaryManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID());
		        	long attachId = reportSummaryManagerNew.addAttachment(attachFile[i], attachFileFileName[i], attachFileContentType[i], projectID,new Long("-1"),fieldCategoryId,getUser().getID());
		        	
		        	//TODO : Need to revisit this logic again
		        	// This is done to preserve atleast one row for All Report Types on UI
		        	//List<ReportSummaryInitiation> initiationList = this.reportSummaryManagerNew.getReportSummaryInitiation(projectID);
		        	List<ReportSummaryDetails> rsdList = reportSummaryDetailsList = this.reportSummaryManagerNew.getReportSummaryDetails(projectID);
		        	
		        	// This will save the Report Summary Details initially
		        	if(rsdList==null || rsdList.size()==0)
		        	{
		        		saveReportSummaryDetails();
		        		if(reportOrderId==3)
		        		{
		        			ReportSummaryDetails rsdbean = new ReportSummaryDetails();
		    	        	rsdbean.setProjectID(projectID);
		    	        	rsdbean.setReportOrderId(reportOrderId);
		    	        	//rsdbean.setLegalApprover("");
		    	        	rsdbean.setReportType(repTypeId);
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
		        		if(reportOrderId==-1)
			        	{
			        		//Add the logic here for Adding attachment for new row without saving the form
			        		Integer maxReportOrderId = new Integer(0);
			        		maxReportOrderId = this.reportSummaryManagerNew.getMaxReportOrderId(projectID, repTypeId);
			        		
			        		// shift the reportOrderId by 1.
			        		//reportSummaryManagerNew.updateReportOrderId(projectID, (maxReportOrderId+1));
			        		
			        		List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
				        	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
				        	rsdbean.setProjectID(projectID);
				        	//rsdbean.setReportOrderId(maxReportOrderId+1);
				        	rsdbean.setReportOrderId(maxReportOrderId);
				        	//rsdbean.setLegalApprover("");
				        	rsdbean.setReportType(repTypeId);
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
				        	rsdbean.setReportOrderId(reportOrderId);
				        	//rsdbean.setLegalApprover("");
				        	rsdbean.setReportType(repTypeId);
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
		        	//Add Attachment Audit logs
		            String description = SynchroGlobal.SynchroAttachmentObject.getById(fieldCategoryId.intValue()) + " Attachment" + "- " +attachFileFileName;
		            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
		            										SynchroGlobal.LogProjectStage.TPD_SUMMARY.getId(), description, project.getName(), 
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
        	}
	    }
        return SUCCESS;
    }
    public String removeAttachment() throws UnsupportedEncodingException {
        try
        {
          //  pibManagerNew.removeAttachment(attachmentId);
            reportSummaryManagerNew.removeAttachment(attachmentId);
			reportSummaryManagerNew.deleteReportSummaryAttachment(attachmentId);
            
          //Remove Attachment Audit logs
            String description = SynchroGlobal.SynchroAttachmentObject.getById(attachmentFieldID.intValue()) + " Attachment" + "- " + attachmentName + " deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DELETE.getId(), 
									SynchroGlobal.LogProjectStage.TPD_SUMMARY.getId(), description, project.getName(), 
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
    
    private void deleteRowAttachments()
    {
    	if(rowId !=-1)
    	{
    		 try
    		 {
	    		 List<AttachmentBean> attachments = tpdSKUAttachmentMap.get(rowId);
	    		 if(attachments!=null && attachments.size()>0)
	    		 {
	    			 for(AttachmentBean att: attachments)
	    			 {
	    				 reportSummaryManagerNew.removeAttachment(att.getID());
	    			 }
	    		 }
    		 }
    		 catch(Exception e)
    		 {
    			 
    		 }
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
    		 
    		/* if((project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() 
	    			 || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()) && (proposalInitiation.getLegalApprovalStatus()!=null && proposalInitiation.getLegalApprovalStatus()==1))
	    	 {
    		 */

    		 
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
 
    public String updateProposalDetails()
    {
    	/*List<ProposalInitiation> proposalInitiationList = this.proposalManagerNew.getProposalInitiationNew(projectID);
    	
    	if(proposalInitiationList!=null && proposalInitiationList.size()>0)
    	{
	    	proposalInitiation = proposalInitiationList.get(0);
	    	
	    	
	    	proposalInitiation.setModifiedBy(getUser().getID());
	    	proposalInitiation.setModifiedDate(System.currentTimeMillis());
	    	
	    	if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusTPD")))
	    	{
	    		Integer legalStatus = Integer.parseInt(getRequest().getParameter("legalApprovalStatusTPD"));
	    		proposalInitiation.setLegalApprovalStatus(legalStatus);
	    	}
	    	
	    	//http://redmine.nvish.com/redmine/issues/389
	    	//proposalInitiation.setLegalApprovalStatus(2);
	    	
	    	if(StringUtils.isNotBlank(getRequest().getParameter("proposalLegalApprover")))
	    	{
	    		Long proposalLegalApprover = Long.parseLong(getRequest().getParameter("proposalLegalApprover"));
	    		proposalInitiation.setProposalLegalApprover(proposalLegalApprover);
	    	}
	    	if(StringUtils.isNotBlank(getRequest().getParameter("tpdLegalApprovalDate")))
	    	{
	    		 proposalInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("tpdLegalApprovalDate")));
	    	}
	    	
	    	this.proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
    	}*/
    		
	    	
	    	
    		
    		tpdSummary.setCreationBy(getUser().getID());
        	tpdSummary.setCreationDate(System.currentTimeMillis());
            
        	tpdSummary.setModifiedBy(getUser().getID());
        	tpdSummary.setModifiedDate(System.currentTimeMillis());
        	
        	tpdSummary.setProjectID(projectID);
        	
        	if(isSave)
        	{
        		tpdSummaryManager.save(tpdSummary);
        	}
	    	
	    	if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusTPD")))
	    	{
	    		Integer legalStatus = Integer.parseInt(getRequest().getParameter("legalApprovalStatusTPD"));
	    		tpdSummary.setLegalApprovalStatus(legalStatus);
	    	}
	    	
	    	//http://redmine.nvish.com/redmine/issues/389
	    	//proposalInitiation.setLegalApprovalStatus(2);
	    	
	    	if(StringUtils.isNotBlank(getRequest().getParameter("proposalLegalApprover")))
	    	{
	    		Long proposalLegalApprover = Long.parseLong(getRequest().getParameter("proposalLegalApprover"));
	    		tpdSummary.setLegalApprover(proposalLegalApprover);
	    	}
	    	if(StringUtils.isNotBlank(getRequest().getParameter("tpdLegalApprovalDate")))
	    	{
	    		tpdSummary.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("tpdLegalApprovalDate")));
	    	}
	    	
	    	 // Setting the Legal Approver for EU Offline cases
	    	tpdSummary.setLegalApproverOffline(getRequest().getParameter("legalApproverOffline"));
	    	
	    	this.tpdSummaryManager.updateLegalApprovalDetails(tpdSummary);
	    	
	    	
	    	// This is done for making the last stage status in sync with the TPD Summary.
	    	if(proposalInitiation!=null && proposalInitiation.getProjectID() > 0 && project.getStatus().intValue() > SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
	    	{
	    		if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusTPD")))
		    	{
		    		Integer legalStatus = Integer.parseInt(getRequest().getParameter("legalApprovalStatusTPD"));
		    		proposalInitiation.setLegalApprovalStatus(legalStatus);
		    	}
	    		if(StringUtils.isNotBlank(getRequest().getParameter("proposalLegalApprover")))
		    	{
		    		Long proposalLegalApprover = Long.parseLong(getRequest().getParameter("proposalLegalApprover"));
		    		proposalInitiation.setProposalLegalApprover(proposalLegalApprover);
		    	}
		    	if(StringUtils.isNotBlank(getRequest().getParameter("tpdLegalApprovalDate")))
		    	{
		    		proposalInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("tpdLegalApprovalDate")));
		    	}
		    	
		    	 // Setting the Legal Approver for EU Offline cases
		    	proposalInitiation.setProposalLegalApproverOffline(getRequest().getParameter("legalApproverOffline"));
		    	
		    	proposalInitiation.setModifiedBy(getUser().getID());
		    	proposalInitiation.setModifiedDate(System.currentTimeMillis());
		    	this.tpdSummaryManager.updateProposalLegalApprovalDetails(proposalInitiation);
	    	}
	    	else if (projectInitiation!=null && projectInitiation.getProjectID() > 0 && project.getStatus().intValue() == SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
	    	{
	    		if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusTPD")))
		    	{
		    		Integer legalStatus = Integer.parseInt(getRequest().getParameter("legalApprovalStatusTPD"));
		    		projectInitiation.setLegalApprovalStatus(legalStatus);
		    	}
	    		if(StringUtils.isNotBlank(getRequest().getParameter("proposalLegalApprover")))
		    	{
		    		Long proposalLegalApprover = Long.parseLong(getRequest().getParameter("proposalLegalApprover"));
		    		projectInitiation.setBriefLegalApprover(proposalLegalApprover);
		    	}
		    	if(StringUtils.isNotBlank(getRequest().getParameter("tpdLegalApprovalDate")))
		    	{
		    		projectInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("tpdLegalApprovalDate")));
		    	}
		    	
		   	 // Setting the Legal Approver for EU Offline cases
		    	projectInitiation.setBriefLegalApproverOffline(getRequest().getParameter("legalApproverOffline"));
		    	
		    	projectInitiation.setModifiedBy(getUser().getID());
		    	projectInitiation.setModifiedDate(System.currentTimeMillis());
		    	this.tpdSummaryManager.updatePIBLegalApprovalDetails(projectInitiation);
	    	}
	    	// This is done in case Brief is completed and Proposal is not Saved yet
	    	else if (projectInitiation!=null && projectInitiation.getProjectID() > 0 && project.getStatus().intValue() == SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal())
	    	{
	    		if(StringUtils.isNotBlank(getRequest().getParameter("legalApprovalStatusTPD")))
		    	{
		    		Integer legalStatus = Integer.parseInt(getRequest().getParameter("legalApprovalStatusTPD"));
		    		projectInitiation.setLegalApprovalStatus(legalStatus);
		    	}
	    		if(StringUtils.isNotBlank(getRequest().getParameter("proposalLegalApprover")))
		    	{
		    		Long proposalLegalApprover = Long.parseLong(getRequest().getParameter("proposalLegalApprover"));
		    		projectInitiation.setBriefLegalApprover(proposalLegalApprover);
		    	}
		    	if(StringUtils.isNotBlank(getRequest().getParameter("tpdLegalApprovalDate")))
		    	{
		    		projectInitiation.setLegalApprovalDate(DateUtils.parse(getRequest().getParameter("tpdLegalApprovalDate")));
		    	}
		    	 // Setting the Legal Approver for EU Offline cases
		    	projectInitiation.setBriefLegalApproverOffline(getRequest().getParameter("legalApproverOffline"));
		    	
		    	projectInitiation.setModifiedBy(getUser().getID());
		    	projectInitiation.setModifiedDate(System.currentTimeMillis());
		    	this.tpdSummaryManager.updatePIBLegalApprovalDetails(projectInitiation);
	    	}
	    	
	    	
	    	
	    	handleAttachments();
	    	 //Audit Logs : TPD Summary Update Status 
	        SynchroLogUtilsNew.TPDSummmaryUpdateStatus(tpdSummary_DB,tpdSummary, project);
	        
	        String i18Text = getText("logger.tpd.summary.update.status.text");
	        SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
									SynchroGlobal.LogProjectStage.TPD_SUMMARY.getId(), i18Text, project.getName(), 
											project.getProjectID(), getUser().getID());
	        
    	
    	
    	return SUCCESS;
    	
    }
    public String updateWaiver(){

        User pibMethApp = null;

        String projectOwnerEmail="";
        String spiContactEmail="";
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/project-close!input.jspa?projectID=" + projectID;

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
            SynchroLogUtils.PIBWaiverSave(pibMethodologyWaiver_DB, pibMethodologyWaiver, project, SynchroGlobal.LogProjectStage.PIB.getId());
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.approve"), project.getName(), 
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.reject"), project.getName(), 
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
            
            email.getContext().put ("waiverInitiator",getUser().getName());
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.request.inf"), project.getName(), 
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.waiver.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
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
        //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID=" + projectID;
        String baseUrl = URLUtils.getRSATokenBaseURL(request);
        String stageUrl = baseUrl+"/new-synchro/project-close!input.jspa?projectID=" + projectID;

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
            SynchroLogUtils.PIBKantarWaiverSave(pibKantarMethodologyWaiver_DB, pibKantarMethodologyWaiver, project);
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.approve"), project.getName(), 
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
            										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.reject"), project.getName(), 
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
            
            email.getContext().put ("waiverInitiator",getUser().getName());
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.request.inf"), project.getName(), 
							project.getProjectID(), getUser().getID());
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
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.send.approve"), project.getName(), 
							project.getProjectID(), getUser().getID());
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

	public List<Long> getEndMarketIds() {
		return endMarketIds;
	}

	public void setEndMarketIds(List<Long> endMarketIds) {
		this.endMarketIds = endMarketIds;
	}

	public TPDSummary getTpdSummary() {
		return tpdSummary;
	}

	public void setTpdSummary(TPDSummary tpdSummary) {
		this.tpdSummary = tpdSummary;
	}

	public TPDSummaryManager getTpdSummaryManager() {
		return tpdSummaryManager;
	}

	public void setTpdSummaryManager(TPDSummaryManager tpdSummaryManager) {
		this.tpdSummaryManager = tpdSummaryManager;
	}

	public List<TPDSKUDetails> getTpdSKUDetails() {
		return tpdSKUDetails;
	}

	public void setTpdSKUDetails(List<TPDSKUDetails> tpdSKUDetails) {
		this.tpdSKUDetails = tpdSKUDetails;
	}

	public Map<Long, List<User>> getEndMarketLegalApprovers() {
		return endMarketLegalApprovers;
	}

	public void setEndMarketLegalApprovers(
			Map<Long, List<User>> endMarketLegalApprovers) {
		this.endMarketLegalApprovers = endMarketLegalApprovers;
	}

	/*public File getAttachFile() {
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
	}*/

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

	public TPDSummary getTpdSummary_DB() {
		return tpdSummary_DB;
	}

	public void setTpdSummary_DB(TPDSummary tpdSummary_DB) {
		this.tpdSummary_DB = tpdSummary_DB;
	}

	public long[] getRemoveAttachID() {
		return removeAttachID;
	}

	public void setRemoveAttachID(long[] removeAttachID) {
		this.removeAttachID = removeAttachID;
	}

	public File[] getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(File[] attachFile) {
		this.attachFile = attachFile;
	}

	public String[] getAttachFileContentType() {
		return attachFileContentType;
	}

	public void setAttachFileContentType(String[] attachFileContentType) {
		this.attachFileContentType = attachFileContentType;
	}

	public String[] getAttachFileFileName() {
		return attachFileFileName;
	}

	public void setAttachFileFileName(String[] attachFileFileName) {
		this.attachFileFileName = attachFileFileName;
	}

	public List<AttachmentBean> getTpdSummaryLegalApproverAtt() {
		return tpdSummaryLegalApproverAtt;
	}

	public void setTpdSummaryLegalApproverAtt(
			List<AttachmentBean> tpdSummaryLegalApproverAtt) {
		this.tpdSummaryLegalApproverAtt = tpdSummaryLegalApproverAtt;
	}

	public int getRepTypeId() {
		return repTypeId;
	}

	public void setRepTypeId(int repTypeId) {
		this.repTypeId = repTypeId;
	}

	public int getReportOrderId() {
		return reportOrderId;
	}

	public void setReportOrderId(int reportOrderId) {
		this.reportOrderId = reportOrderId;
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public Map<Integer, List<AttachmentBean>> getTpdSKUAttachmentMap() {
		return tpdSKUAttachmentMap;
	}

	public void setTpdSKUAttachmentMap(
			Map<Integer, List<AttachmentBean>> tpdSKUAttachmentMap) {
		this.tpdSKUAttachmentMap = tpdSKUAttachmentMap;
	}

	public File[] getTpdattachFile() {
		return tpdattachFile;
	}

	public void setTpdattachFile(File[] tpdattachFile) {
		this.tpdattachFile = tpdattachFile;
	}

	public String[] getTpdattachFileContentType() {
		return tpdattachFileContentType;
	}

	public void setTpdattachFileContentType(String[] tpdattachFileContentType) {
		this.tpdattachFileContentType = tpdattachFileContentType;
	}

	public String[] getTpdattachFileFileName() {
		return tpdattachFileFileName;
	}

	public void setTpdattachFileFileName(String[] tpdattachFileFileName) {
		this.tpdattachFileFileName = tpdattachFileFileName;
	}

	public String getTpdSummaryRSLegalApprover() {
		return tpdSummaryRSLegalApprover;
	}

	public void setTpdSummaryRSLegalApprover(String tpdSummaryRSLegalApprover) {
		this.tpdSummaryRSLegalApprover = tpdSummaryRSLegalApprover;
	}

	public List<AttachmentBean> getTpdSummaryRSLegalApproverAtt() {
		return tpdSummaryRSLegalApproverAtt;
	}

	public void setTpdSummaryRSLegalApproverAtt(
			List<AttachmentBean> tpdSummaryRSLegalApproverAtt) {
		this.tpdSummaryRSLegalApproverAtt = tpdSummaryRSLegalApproverAtt;
	}

	public boolean isShowTPDSummaryFields() {
		return showTPDSummaryFields;
	}

	public void setShowTPDSummaryFields(boolean showTPDSummaryFields) {
		this.showTPDSummaryFields = showTPDSummaryFields;
	}

	

}
