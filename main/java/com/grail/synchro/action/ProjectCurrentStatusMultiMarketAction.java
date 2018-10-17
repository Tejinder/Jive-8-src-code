package com.grail.synchro.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.MultiMarketProject;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCurrentStatus;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectStagePendingFields;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectCurrentStatusManager;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.URLUtils;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.InputStreamDataSource;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;


/**
 * @author Tejinder
 * @version 1.0, 
 */
public class ProjectCurrentStatusMultiMarketAction extends JiveActionSupport implements Preparable{
 
    private static final Logger LOGGER = Logger.getLogger(ProjectCurrentStatusMultiMarketAction.class);
    private Long projectID;
    private Long endMarketId;
    private Project project;
    private ProjectManager synchroProjectManager;
    private PIBManager pibManager;
    private ProposalManager proposalManager;
    private ProjectSpecsManager projectSpecsManager;
    private ReportSummaryManager reportSummaryManager;
    private StageManager stageManager;
    private EmailNotificationManager emailNotificationManager;
    private ProjectEvaluationManager projectEvaluationManager;
    
    private List<Long> emIds;
    
    private List<ProjectCurrentStatus> pitCurrentStatusList;
    private List<ProjectCurrentStatus> pibCurrentStatusList;
    
    private Map<Long,List<ProjectStagePendingFields>> pibPendingFieldsListMap;
    
    private List<ProjectCurrentStatus> proposalCurrentStatusList;
    private List<ProjectStagePendingFields> proposalPendingFieldsList;
    
    private List<ProjectCurrentStatus> projectSpecsCurrentStatusList;
    private Map<Long,List<ProjectStagePendingFields>> psPendingFieldsListMap;
    
    private List<ProjectCurrentStatus> reportCurrentStatusList;    
    private Map<Long,List<ProjectStagePendingFields>> rsPendingFieldsListMap;
    
    private List<ProjectCurrentStatus> projectEvalCurrentStatusList;
    
    private ProjectCurrentStatusManager projectCurrentStatusManger;
    
    private PIBMethodologyWaiver pibMethodologyWaiver;
    private PIBMethodologyWaiver pibKantarMethodologyWaiver;
    
    private String pitStageUrl;
    private String pibStageUrl;
    private String proposalStageUrl;
    private String projectSpecsStageUrl;
    private String reportSummaryStageUrl;
    private String projectEvaluationStageUrl;
    
    private String pibStatus;
    private String proposalStatus;
    private String projectSpecsStatus;
    private String reportSummaryStatus;
    
    private String notificationTabId;
    private String recipients;
    private String subject;
    private String messageBody;
    
    private File[] mailAttachment;
	private String[] mailAttachmentFileName;
	private String[] mailAttachmentContentType;
	
	private List<FundingInvestment> fundingInvestments;
	
	private ProjectInitiation projectInitiation;
	private ProposalInitiation proposalInitiation;
	
	 // This flag will tell whether the Project Specs have been approved for all the End Markets or not.
    private Boolean allProjectSpecsArroved = false;
    
    public void prepare() throws Exception {
        final String id = getRequest().getParameter("projectID");
        
        LOGGER.info("Inside Prepare ProjectCurrentStatusAction ---");
        if(id != null ) {

                try{
                    projectID = Long.parseLong(id);
                    
                } catch (NumberFormatException nfEx) {
                	LOGGER.error("Invalid ProjectID ");
                    throw nfEx;
                }
                
                project = this.synchroProjectManager.get(projectID);
                //List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
                emIds = synchroProjectManager.getEndMarketIDs(projectID);
                
                endMarketId = emIds.get(0);
                
                pibMethodologyWaiver = this.pibManager.getPIBMethodologyWaiver(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                pibKantarMethodologyWaiver = this.pibManager.getPIBKantarMethodologyWaiver(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                
                String baseUrl = URLUtils.getRSATokenBaseURL(request);
                
                pitStageUrl=baseUrl+"/synchro/create-multimarket-project!input.jspa?projectID=" + projectID;
                pitCurrentStatusList = projectCurrentStatusManger.getPITStatusList(projectID, pitStageUrl);
                
              //  List<ProjectInitiation> initiationList = pibManager.getPIBDetails(projectID);
               
                pibStageUrl = baseUrl+"/synchro/pib-multi-details!input.jspa?projectID=" + projectID;
              //  pibCurrentStatusList = projectCurrentStatusManger.getPIBStatusList(projectID, pibStageUrl,emIds,pibMethodologyWaiver, pibKantarMethodologyWaiver, initiationList );
                
                fundingInvestments =  synchroProjectManager.getProjectInvestments(projectID);
                pibCurrentStatusList = projectCurrentStatusManger.getPIBMultiStatusList(projectID, pibStageUrl,emIds,pibMethodologyWaiver, pibKantarMethodologyWaiver, fundingInvestments);
                pibPendingFieldsListMap = projectCurrentStatusManger.getPIBMultiPendingFields(projectID, emIds);
                
                List<ProjectInitiation> initiationList = pibManager.getPIBDetails(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                
                if(initiationList!=null && initiationList.size()>0)
                {
                	projectInitiation = initiationList.get(0);
                }
                
                if(initiationList!=null && initiationList.size()>0 && initiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
                {
                	pibStatus="Completed";
                }
                else
                {
                	pibStatus="Pending";
                }
                if(pibMethodologyWaiver==null)
                {
                	pibMethodologyWaiver = new PIBMethodologyWaiver();
                }
                if(pibKantarMethodologyWaiver==null)
                {
                	pibKantarMethodologyWaiver = new PIBMethodologyWaiver();
                }
               
                List<ProposalInitiation> proposalInitiationList = proposalManager.getProposalDetails(projectID);
                proposalStageUrl = baseUrl+"/synchro/proposal-multi-details!input.jspa?projectID=" + projectID;
                proposalCurrentStatusList = projectCurrentStatusManger.getProposalMultiStatusList(proposalInitiationList,projectID, proposalStageUrl, fundingInvestments);
                proposalPendingFieldsList = projectCurrentStatusManger.getProposalMultiPendingFields(proposalInitiationList, projectID, emIds);
                
                if(proposalInitiationList!=null && proposalInitiationList.size()>0)
                {
                	proposalInitiation= proposalInitiationList.get(0);
                }
                if(proposalInitiationList!=null && proposalInitiationList.size()>0 && proposalInitiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PROPOASL_AWARDED.ordinal())
                {
                	proposalStatus="Completed";
                }
                else
                {
                	proposalStatus="Pending";
                }
             
                List<ProjectSpecsInitiation> projectSpecsInitiationList = projectSpecsManager.getProjectSpecsInitiation(projectID);
                
                if(projectSpecsInitiationList!=null && projectSpecsInitiationList.size()>0 && projectSpecsInitiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal())
                {
                	projectSpecsStatus="Completed";
                }
                else
                {
                	projectSpecsStatus="Pending";
                }
                
                projectSpecsStageUrl = baseUrl+"/synchro/project-multi-specs!input.jspa?projectID=" + projectID;
                
              //  projectSpecsCurrentStatusList = projectCurrentStatusManger.getProjectSpecsStatusList(projectSpecsInitiationList,projectID, projectSpecsStageUrl);
                projectSpecsCurrentStatusList = projectCurrentStatusManger.getProjectSpecsMultiStatusList(projectID, projectSpecsStageUrl, fundingInvestments);
             //   projectSpecsPendingFieldsList = projectCurrentStatusManger.getProjectSpecsPendingFields(projectSpecsInitiationList, projectID, emIds);
                psPendingFieldsListMap = projectCurrentStatusManger.getProjectSpecsMultiPendingFields(projectID, emIds);
                
                List<ReportSummaryInitiation> reportSummaryInitiationList = reportSummaryManager.getReportSummaryInitiation(projectID);
                
                if(reportSummaryInitiationList!=null && reportSummaryInitiationList.size()>0 && reportSummaryInitiationList.get(0).getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal())
                {
                	reportSummaryStatus="Completed";
                }
                else
                {
                	reportSummaryStatus="Pending";
                }
             
    			if(projectSpecsInitiationList!=null && projectSpecsInitiationList.size()>0)
    			{
    				for(ProjectSpecsInitiation projectSpecs: projectSpecsInitiationList)
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
                reportSummaryStageUrl = baseUrl+"/synchro/report-multi-summary!input.jspa?projectID=" + projectID;
                reportCurrentStatusList = projectCurrentStatusManger.getReportSummaryMultiStatusList(projectID, reportSummaryStageUrl, fundingInvestments);
               // reportSummaryPendingFieldsList = projectCurrentStatusManger.getReportSummaryPendingFields(reportSummaryInitiationList, projectID, emIds);
                rsPendingFieldsListMap = projectCurrentStatusManger.getReportSummaryMultiPendingFields(projectID, emIds);
                
                
                
                projectEvaluationStageUrl = baseUrl+"/synchro/project-multi-eval!input.jspa?projectID=" + projectID;
                projectEvalCurrentStatusList = projectCurrentStatusManger.getProjectEvaluationMultiStatusList(projectID, projectEvaluationStageUrl, emIds, fundingInvestments);
                
               
        }
      
        
    }
 	
    public String input() {

    	if (SynchroPermHelper.hasPIBAccessMultiMarket(projectID) || SynchroPermHelper.canAccessProject(projectID)) {
    		return INPUT;	
    	}
    	else
    	{
    		return UNAUTHORIZED;
    	}	
    }

    public String sendNotification() {

        //EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,"pib.complete.notifyAgency.htmlBody","pib.complete.notifyAgency.subject");
        
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("PENDING_ACTION_PERSON_RESPONSIBLE"))
		{
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("PENDING_ACTION_PERSON_RESPONSIBLE");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
		}
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("PIB_COMPLETE_NOTIFY_AGENCY"))
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
	            
	            PIBStakeholderList pibStakeholderList =  this.pibManager.getPIBStakeholderList(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
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
	        	LOGGER.error("Project Owner not found while sending Notification for PIB Complete"+project.getProjectOwner());
	        }
            stageManager.updateStageStatus(projectID, endMarketId, 1, SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal(),getUser(), null);
            // Update the status for all the End Market Ids
            for(Long emid: emIds)
            {
                stageManager.updateStageStatus(projectID, emid, 1, SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal(),getUser(), null);
            }
            
            Map<Integer, List<AttachmentBean>> attachmentMap = this.pibManager.getDocumentAttachment(projectID, endMarketId);
            if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()!=SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                //stageManager.updateStageStatus(projectID, endMarketId, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
                stageManager.updateMultiMarketStageStatus(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
            }
            
            // This is done in case the End Market is activated before the proposal is awarded , then we have to move that end market details to 
            // Proposal tab as well.
            if(projectInitiation.getStatus()!=null && projectInitiation.getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                stageManager.updateActivatedProposalEM(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, 2, SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal(),getUser(), attachmentMap);
            }

            // Update the project status to IN PROGRESS once the PIB is completed.
            //	synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_OPEN.ordinal());
            synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal());

        
	    	
		}
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND_PROPOSAL_TO_BAT"))
		{
    	
            this.proposalManager.submitProposal(projectID, proposalInitiation.getAgencyID());
            //this.proposalManager.updateRequestClarificationModification(projectID, agencyID, null);
            try
            {
                //EmailNotification#7 Fetch Recipients for sending Proposal Submit notification
                String recp="";
                List<FundingInvestment> fundingInvestment = synchroProjectManager.getProjectInvestments(projectID);
                for(FundingInvestment fd:fundingInvestment )
                {
                    if(fd.getAboveMarket())
                    {
                        recp = userManager.getUser(fd.getProjectContact()).getEmail();
                    }
                }
                if(recp.length()>0)
                {
                    recp=recp+","+ userManager.getUser(project.getProjectOwner()).getEmail();
                }
                else
                {
                    recp = userManager.getUser(project.getProjectOwner()).getEmail();
                }
                //String recp = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getEmail();
                // EmailMessage email = stageManager.populateNotificationEmail(recp, subject, messageBody,"agency.submit.proposal.htmlBody","agency.submit.proposal.subject");
                EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody,"agency.submit.proposal.htmlBody","agency.submit.proposal.subject");
                //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/proposal-multi-details!input.jspa?projectID=" + projectID;
                String baseUrl = URLUtils.getBaseURL(request);
                String stageUrl = baseUrl+"/synchro/proposal-multi-details!input.jspa?projectID=" + projectID;
                //email.getContext().put("projectId", projectID);
                email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                email.getContext().put("projectName",project.getName());
                email.getContext().put("agencyName", userManager.getUser(proposalInitiation.getAgencyID()).getName());
                email.getContext().put ("stageUrl",stageUrl);

                //String sub = "Proposal for Project -"+project.getName() +"has been submitted";
                //String mess = "Proposal for  Project -"+project.getName() +"has been submitted by Agency -" + userManager.getUser(agencyID).getName();

                email = handleAttachments(email);
                stageManager.sendNotification(getUser(),email);

                //Email Notification TimeStamp Storage
                EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                emailNotBean.setProjectID(projectID);
                emailNotBean.setEndmarketID(endMarketId);
                emailNotBean.setAgencyID(proposalInitiation.getAgencyID());
                emailNotBean.setStageID(SynchroConstants.PROPOSAL_STAGE);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("PROPOSAL SUBMITTED");
                emailNotBean.setEmailSubject(subject);
                emailNotBean.setEmailSender(getUser().getEmail());
                //emailNotBean.setEmailRecipients(recp);
                emailNotBean.setEmailRecipients(recipients);
                emailNotificationManager.saveDetails(emailNotBean);
            }
            catch(UserNotFoundException ue)
            {
            	LOGGER.error("User not found while submit Proposal");
            }
     	
		}
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NOTIFY_BAT_ABOUT_PROPOSAL_REVISION"))
		{
    		
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("NOTIFY_BAT_ABOUT_PROPOSAL_REVISION");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	this.proposalManager.updateRequestClarificationModification(projectID,  proposalInitiation.getAgencyID(), null);
	    	this.proposalManager.submitProposal(projectID, proposalInitiation.getAgencyID());
	    	
		}
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND_PROPOSAL_TO_PROJECT_OWNER"))
		{
    		
    	/*	EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("SEND_PROPOSAL_TO_PROJECT_OWNER");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	*/
    		
	    	EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
            email = handleAttachments(email);
            stageManager.sendNotification(getUser(),email);
            this.proposalManager.updateSendToProjectOwner(projectID, proposalInitiation.getAgencyID(), 1);
            this.proposalManager.updateRequestClarificationModification(projectID, proposalInitiation.getAgencyID(), null);

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            emailNotBean.setEndmarketID(endMarketId);
            emailNotBean.setAgencyID(proposalInitiation.getAgencyID());
            emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("PROPOSAL_SEND_TO_PROJECT_OWNER");
            emailNotBean.setEmailSubject(subject);
            emailNotBean.setEmailSender(getUser().getEmail());
            emailNotBean.setEmailRecipients(recipients);
            emailNotificationManager.saveDetails(emailNotBean);
	    	
		}
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NOTIFY_BAT_PROJECT_SPECS_REVISION"))
		{
    		
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(endMarketId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("NOTIFY_BAT_PROJECT_SPECS_REVISION");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
		}
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("APPROVE_PROJECT_SPECS"))
		{
    		Long emId = Long.parseLong(request.getParameter("emailEndMarketId"));
    		LOGGER.info("Checking endMarketId While PS APPROVE --" + emId);
			this.projectSpecsManager.approve(getUser(),projectID,emId);
			//this.projectSpecsManager.updateProjectSpecsStatus(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal());
			
			boolean allPSEMApproved = true;
			
			List<ProjectSpecsInitiation> allPSList = this.projectSpecsManager.getProjectSpecsInitiation(projectID);
			for(ProjectSpecsInitiation ps: allPSList)
			{
				// Only when the End Market Id is Above Market Id then make the entries for Report Summary for all the End Markets 
				if(emId.intValue()==SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID.intValue())
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
	    	emailNotBean.setEndmarketID(emId);
	       	emailNotBean.setStageID(SynchroConstants.PS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("FINAL APPROVAL");
	    	
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	
	    	emailNotificationManager.saveDetails(emailNotBean);
		
	    	
		}
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("SEND_FOR_APPROVAL_REPORTS"))
		{
    		Long emId = Long.parseLong(request.getParameter("emailEndMarketId"));
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(emId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("SEND_FOR_APPROVAL_REPORTS");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	
    		LOGGER.info("Checking endMarketId While SEND FOR APPROVAL REPORT --" + emId);
	    	reportSummaryManager.updateSendForApproval(projectID,emId,1);
	    	
		}
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("NOTIFY_BAT_REPORTS_REVISION"))
		{
    		Long emId = Long.parseLong(request.getParameter("emailEndMarketId"));
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(emId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("NOTIFY_BAT_REPORTS_REVISION");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	
	    	reportSummaryManager.updateSendForApproval(projectID,emId,1);
	    	
		}
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("APPROVE_REPORTS"))
		{
    		Long emId = Long.parseLong(request.getParameter("emailEndMarketId"));
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(emId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("APPROVE_REPORTS");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	

			this.reportSummaryManager.approveSPI(getUser(),projectID,emId);
			
			SynchroToIRIS synchroToIRIS = this.reportSummaryManager.getSynchroToIRIS(projectID, emId, project);
			// This is done in case the second option is clicked (Summary for IRIS not required) then the project should get completed.
			if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired()==2 && StringUtils.isNotBlank(synchroToIRIS.getIrisOptionRationale()))
			{
				this.reportSummaryManager.updateReportSummaryStatus(projectID,emId,SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal());
				
				 List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,emId);
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
		
	    	
		}
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("UPLOAD_TO_IRIS_REPORTS"))
		{
    		Long emId = Long.parseLong(request.getParameter("emailEndMarketId"));
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(emId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("UPLOAD_TO_IRIS_REPORTS");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	reportSummaryManager.updateUploadToIRIS(projectID,emId,1);
	    	
		}
    	
    	if(notificationTabId!=null && notificationTabId.equalsIgnoreCase("UPLOAD_TO_C_PSI_DATABASE_REPORTS"))
		{
    		Long emId = Long.parseLong(request.getParameter("emailEndMarketId"));
    		EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, messageBody, null, null);
    		email = handleAttachments(email);
			stageManager.sendNotification(getUser(),email);
			
			//Email Notification TimeStamp Storage
	    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	    	emailNotBean.setProjectID(projectID);
	    	emailNotBean.setEndmarketID(emId);
	    	emailNotBean.setStageID(SynchroConstants.PENDING_ACTIONS_STAGE);
	    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
	    	emailNotBean.setEmailDesc("UPLOAD_TO_C_PSI_DATABASE_REPORTS");
	    	emailNotBean.setEmailSubject(subject);
	    	emailNotBean.setEmailSender(getUser().getEmail());
	    	emailNotBean.setEmailRecipients(recipients);
	    	emailNotificationManager.saveDetails(emailNotBean);
	    	reportSummaryManager.updateUploadToCPSIDatabase(projectID,emId,1);
	    	
		}
    	
    	return SUCCESS;
    }
    
    /**
     * This method will update the PIT from the Pending Actions screen
     * @return
     */
    public String updatePIT(){

        this.project = synchroProjectManager.get(projectID);

        if(getRequest().getMethod().equalsIgnoreCase("POST")){
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
            	LOGGER.debug("Error occurred while binding the request object with the Multi Market Project bean in Create Project Action");
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
        	LOGGER.debug("Error occurred while binding the request object with the Multi Market Project bean in Create Project Action");
            input();
        }

        List<FundingInvestment> investments = synchroProjectManager.setProjectInvestments(getFundingInvestmentBean(multiMarketProject), projectID);

        synchroProjectManager.save(project);
      
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
                
                LOG.info("IS APPROVED --- " + investment.getApproved());
                LOG.info("PROJECT CONTACT --- " + investment.getProjectContact());
                
                //Add to array
                investments.add(investment);
            }
        }

        return investments;
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
	public Long getProjectID() {
		return projectID;
	}

	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}

	public List<ProjectCurrentStatus> getPitCurrentStatusList() {
		return pitCurrentStatusList;
	}

	public void setPitCurrentStatusList(
			List<ProjectCurrentStatus> pitCurrentStatusList) {
		this.pitCurrentStatusList = pitCurrentStatusList;
	}

	public List<ProjectCurrentStatus> getPibCurrentStatusList() {
		return pibCurrentStatusList;
	}

	public void setPibCurrentStatusList(
			List<ProjectCurrentStatus> pibCurrentStatusList) {
		this.pibCurrentStatusList = pibCurrentStatusList;
	}

	public List<ProjectCurrentStatus> getProposalCurrentStatusList() {
		return proposalCurrentStatusList;
	}

	public void setProposalCurrentStatusList(
			List<ProjectCurrentStatus> proposalCurrentStatusList) {
		this.proposalCurrentStatusList = proposalCurrentStatusList;
	}

	public List<ProjectCurrentStatus> getProjectSpecsCurrentStatusList() {
		return projectSpecsCurrentStatusList;
	}

	public void setProjectSpecsCurrentStatusList(
			List<ProjectCurrentStatus> projectSpecsCurrentStatusList) {
		this.projectSpecsCurrentStatusList = projectSpecsCurrentStatusList;
	}

	public List<ProjectCurrentStatus> getReportCurrentStatusList() {
		return reportCurrentStatusList;
	}

	public void setReportCurrentStatusList(
			List<ProjectCurrentStatus> reportCurrentStatusList) {
		this.reportCurrentStatusList = reportCurrentStatusList;
	}

	public List<ProjectCurrentStatus> getProjectEvalCurrentStatusList() {
		return projectEvalCurrentStatusList;
	}

	public void setProjectEvalCurrentStatusList(
			List<ProjectCurrentStatus> projectEvalCurrentStatusList) {
		this.projectEvalCurrentStatusList = projectEvalCurrentStatusList;
	}

	public ProjectCurrentStatusManager getProjectCurrentStatusManger() {
		return projectCurrentStatusManger;
	}

	public void setProjectCurrentStatusManger(
			ProjectCurrentStatusManager projectCurrentStatusManger) {
		this.projectCurrentStatusManger = projectCurrentStatusManger;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ProjectManager getSynchroProjectManager() {
		return synchroProjectManager;
	}

	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
		this.synchroProjectManager = synchroProjectManager;
	}

	public PIBMethodologyWaiver getPibMethodologyWaiver() {
		return pibMethodologyWaiver;
	}

	public void setPibMethodologyWaiver(PIBMethodologyWaiver pibMethodologyWaiver) {
		this.pibMethodologyWaiver = pibMethodologyWaiver;
	}

	public PIBManager getPibManager() {
		return pibManager;
	}

	public void setPibManager(PIBManager pibManager) {
		this.pibManager = pibManager;
	}

	public PIBMethodologyWaiver getPibKantarMethodologyWaiver() {
		return pibKantarMethodologyWaiver;
	}

	public void setPibKantarMethodologyWaiver(
			PIBMethodologyWaiver pibKantarMethodologyWaiver) {
		this.pibKantarMethodologyWaiver = pibKantarMethodologyWaiver;
	}

	
	public ProposalManager getProposalManager() {
		return proposalManager;
	}

	public void setProposalManager(ProposalManager proposalManager) {
		this.proposalManager = proposalManager;
	}

	public List<ProjectStagePendingFields> getProposalPendingFieldsList() {
		return proposalPendingFieldsList;
	}

	public void setProposalPendingFieldsList(
			List<ProjectStagePendingFields> proposalPendingFieldsList) {
		this.proposalPendingFieldsList = proposalPendingFieldsList;
	}

	public ProjectSpecsManager getProjectSpecsManager() {
		return projectSpecsManager;
	}

	public void setProjectSpecsManager(ProjectSpecsManager projectSpecsManager) {
		this.projectSpecsManager = projectSpecsManager;
	}

	
	public ReportSummaryManager getReportSummaryManager() {
		return reportSummaryManager;
	}

	public void setReportSummaryManager(ReportSummaryManager reportSummaryManager) {
		this.reportSummaryManager = reportSummaryManager;
	}


	public String getPibStageUrl() {
		return pibStageUrl;
	}

	public void setPibStageUrl(String pibStageUrl) {
		this.pibStageUrl = pibStageUrl;
	}

	public String getProposalStageUrl() {
		return proposalStageUrl;
	}

	public void setProposalStageUrl(String proposalStageUrl) {
		this.proposalStageUrl = proposalStageUrl;
	}

	public String getProjectSpecsStageUrl() {
		return projectSpecsStageUrl;
	}

	public void setProjectSpecsStageUrl(String projectSpecsStageUrl) {
		this.projectSpecsStageUrl = projectSpecsStageUrl;
	}

	public String getReportSummaryStageUrl() {
		return reportSummaryStageUrl;
	}

	public void setReportSummaryStageUrl(String reportSummaryStageUrl) {
		this.reportSummaryStageUrl = reportSummaryStageUrl;
	}

	public String getProjectEvaluationStageUrl() {
		return projectEvaluationStageUrl;
	}

	public void setProjectEvaluationStageUrl(String projectEvaluationStageUrl) {
		this.projectEvaluationStageUrl = projectEvaluationStageUrl;
	}

	public String getPitStageUrl() {
		return pitStageUrl;
	}

	public void setPitStageUrl(String pitStageUrl) {
		this.pitStageUrl = pitStageUrl;
	}

	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

	public String getNotificationTabId() {
		return notificationTabId;
	}

	public void setNotificationTabId(String notificationTabId) {
		this.notificationTabId = notificationTabId;
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

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
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

	public Long getEndMarketId() {
		return endMarketId;
	}

	public void setEndMarketId(Long endMarketId) {
		this.endMarketId = endMarketId;
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

	public Map<Long, List<ProjectStagePendingFields>> getPibPendingFieldsListMap() {
		return pibPendingFieldsListMap;
	}

	public void setPibPendingFieldsListMap(
			Map<Long, List<ProjectStagePendingFields>> pibPendingFieldsListMap) {
		this.pibPendingFieldsListMap = pibPendingFieldsListMap;
	}

	public Map<Long, List<ProjectStagePendingFields>> getPsPendingFieldsListMap() {
		return psPendingFieldsListMap;
	}

	public void setPsPendingFieldsListMap(
			Map<Long, List<ProjectStagePendingFields>> psPendingFieldsListMap) {
		this.psPendingFieldsListMap = psPendingFieldsListMap;
	}

	public Map<Long, List<ProjectStagePendingFields>> getRsPendingFieldsListMap() {
		return rsPendingFieldsListMap;
	}

	public void setRsPendingFieldsListMap(
			Map<Long, List<ProjectStagePendingFields>> rsPendingFieldsListMap) {
		this.rsPendingFieldsListMap = rsPendingFieldsListMap;
	}

	public String getPibStatus() {
		return pibStatus;
	}

	public void setPibStatus(String pibStatus) {
		this.pibStatus = pibStatus;
	}

	public String getProposalStatus() {
		return proposalStatus;
	}

	public void setProposalStatus(String proposalStatus) {
		this.proposalStatus = proposalStatus;
	}

	public String getProjectSpecsStatus() {
		return projectSpecsStatus;
	}

	public void setProjectSpecsStatus(String projectSpecsStatus) {
		this.projectSpecsStatus = projectSpecsStatus;
	}

	public String getReportSummaryStatus() {
		return reportSummaryStatus;
	}

	public void setReportSummaryStatus(String reportSummaryStatus) {
		this.reportSummaryStatus = reportSummaryStatus;
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

	public ProjectEvaluationManager getProjectEvaluationManager() {
		return projectEvaluationManager;
	}

	public void setProjectEvaluationManager(
			ProjectEvaluationManager projectEvaluationManager) {
		this.projectEvaluationManager = projectEvaluationManager;
	}

	public Boolean getAllProjectSpecsArroved() {
		return allProjectSpecsArroved;
	}

	public void setAllProjectSpecsArroved(Boolean allProjectSpecsArroved) {
		this.allProjectSpecsArroved = allProjectSpecsArroved;
	}


	

}
