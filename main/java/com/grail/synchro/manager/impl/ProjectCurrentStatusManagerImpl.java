package com.grail.synchro.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.StageStatus;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCurrentStatus;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectStagePendingFields;
import com.grail.synchro.beans.ProposalEndMarketDetails;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectCurrentStatusManager;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.mail.util.TemplateUtil;
import com.jivesoftware.util.StringUtils;

/**
 * @author Tejinder
 */
public class ProjectCurrentStatusManagerImpl implements ProjectCurrentStatusManager {

    private static Logger LOG = Logger.getLogger(ProjectCurrentStatusManagerImpl.class);
    private ProjectManager synchroProjectManager;
    private UserManager userManager = null;
    private PIBManager pibManager;
    private ProposalManager proposalManager;
    private ProjectSpecsManager projectSpecsManager;
    private ReportSummaryManager reportSummaryManager;
    
    private SynchroUtils synchroUtils;
    private ProjectEvaluationManager projectEvaluationManager;
    private StageManager stageManager;
    
    @Override
    public List<ProjectCurrentStatus> getPITStatusList(final long projectID, String stageUrl) {
      
    	Project project = synchroProjectManager.get(projectID);
    	
    	List<ProjectCurrentStatus> pitCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus pitCurrentStatus = null;
    	
    	//String stageUrl=baseUrl+"/synchro/create-project!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        
    	
		
    	if(project.getIsSave())
    	{
    		pitCurrentStatus = new ProjectCurrentStatus();
	        pitCurrentStatus.setActivityDesc("PIT Draft");
	      
	        pitCurrentStatus.setStatus("Complete");
	        
	        messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pitCurrentStatus.getActivityDesc()));
	        pitCurrentStatus.setPersonRespSubject(subject);
	        pitCurrentStatus.setPersonRespMessage(messageBody);
	        
	        
	        try
	        {
	        	pitCurrentStatus.setPersonResponsible(userManager.getUser(project.getBriefCreator()).getName());
	        	pitCurrentStatus.setPersonRespEmail(userManager.getUser(project.getBriefCreator()).getEmail());
	        }
	        catch(UserNotFoundException ue)
	        {
	        	pitCurrentStatus.setPersonResponsible("");
	        	pitCurrentStatus.setPersonRespEmail("");
	        }
	        if(project.getProjectSaveDate()!=null)
	        {
	        	pitCurrentStatus.setCompletionDate(project.getProjectSaveDate());
	        }
	        //pitCurrentStatus.setNextStep("Confirm PIT");
	        pitCurrentStatus.setNextStep("");
	        pitCurrentStatusList.add(pitCurrentStatus);
    	}
        pitCurrentStatus = new ProjectCurrentStatus();
        pitCurrentStatus.setActivityDesc("PIT Created");
        
        
        messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pitCurrentStatus.getActivityDesc()));
        pitCurrentStatus.setPersonRespSubject(subject);
        pitCurrentStatus.setPersonRespMessage(messageBody);
    	
        if(project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())
        {
        	pitCurrentStatus.setStatus("Pending");
        	pitCurrentStatus.setNextStepLink(stageUrl);
        	pitCurrentStatus.setNextStep("Confirm PIT");
        }
        else
        {
        	pitCurrentStatus.setStatus("Complete");
        	pitCurrentStatus.setNextStep("");
        	if(project.getProjectStartDate()!=null)
 	        {
 	        	pitCurrentStatus.setCompletionDate(project.getProjectStartDate());
 	        }
        }
        try
        {
        	pitCurrentStatus.setPersonResponsible(userManager.getUser(project.getBriefCreator()).getName());
        	pitCurrentStatus.setPersonRespEmail(userManager.getUser(project.getBriefCreator()).getEmail());
        }
        catch(UserNotFoundException ue)
        {
        	pitCurrentStatus.setPersonResponsible("");
        	pitCurrentStatus.setPersonRespEmail("");
        }
      //  pitCurrentStatus.setNextStep("");
        pitCurrentStatusList.add(pitCurrentStatus);
        return pitCurrentStatusList;
    }
    
    @Override
    public List<ProjectCurrentStatus> getPIBStatusList(final long projectID, String stageUrl,List<Long> emIds, PIBMethodologyWaiver pibMW, PIBMethodologyWaiver pibKantarMW, List<ProjectInitiation> initiationList ) {
      
    	//List<ProjectInitiation> initiationList = pibManager.getPIBDetails(projectID);
    	Project project = synchroProjectManager.get(projectID);
    	//List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	List<EndMarketInvestmentDetail> emDetailList = synchroProjectManager.getEndMarketDetails(projectID);
    	
    	List<ProjectCurrentStatus> pibCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus pibCurrentStatus = null;
    	
    	//String stageUrl = baseUrl+"/synchro/pib-details!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	
    	//Project Brief  
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		//if(initiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
    		if(initiationList.get(0).getStatus()>=SynchroGlobal.StageStatus.PIB_SAVED.ordinal())
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Project Brief");    		      
    		    pibCurrentStatus.setStatus("Complete");
    		    
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    if(initiationList.get(0).getPibSaveDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(initiationList.get(0).getPibSaveDate());
    		    }
    		    pibCurrentStatus.setNextStep("");
    		    pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Project Brief");    		      
    		    pibCurrentStatus.setStatus("Pending");
    		    
    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("Complete Project Brief");
            	pibCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
            	pibCurrentStatus.setMandatory(true);

    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		
    	}
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
    	else
    	{
    		pibCurrentStatus=new ProjectCurrentStatus();
			pibCurrentStatus.setActivityDesc("Project Brief");    		      
		    pibCurrentStatus.setStatus("Pending");
		    
		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	        pibCurrentStatus.setPersonRespSubject(subject);
	        pibCurrentStatus.setPersonRespMessage(messageBody);
	        
		    try
	        {
		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        }
	        catch(UserNotFoundException ue)
	        {
	        	pibCurrentStatus.setPersonResponsible("");
	        	pibCurrentStatus.setPersonRespEmail("");
	        }
		    pibCurrentStatus.setNextStep("Complete Project Brief");
        	pibCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
        	pibCurrentStatus.setMandatory(true);

		    pibCurrentStatusList.add(pibCurrentStatus);
    	}
    	
    	//Methodology Waiver Approval
    //	PIBMethodologyWaiver pibMW = pibManager.getPIBMethodologyWaiver(projectID, emIds.get(0));
    	if(pibMW!=null)
    	{
    		if(pibMW.getIsApproved()!=null && pibMW.getIsApproved()==1)
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Methodology Waiver Approval");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        	
    	        }
    		    if(pibMW.getModifiedDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(new Date(pibMW.getModifiedDate()));
    		    }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Methodology Waiver Approval");    	

    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    pibCurrentStatus.setStatus("Pending");
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    	}
    	
    	// Legal Approval
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if((initiationList.get(0).getLegalApprovalRcvd() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals(""))
    				|| initiationList.get(0).getLegalApprovalNotReq())
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Legal Approval");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("");
    		    pibCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getPibLegalApprovalDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(initiationList.get(0).getPibLegalApprovalDate());
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Legal Approval");    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("Advise Legal Approval");
    		    pibCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
    		    pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		
    	}
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
    	else
    	{
    		pibCurrentStatus=new ProjectCurrentStatus();
			pibCurrentStatus.setActivityDesc("Legal Approval");    		      
		    pibCurrentStatus.setStatus("Pending");

		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	        pibCurrentStatus.setPersonRespSubject(subject);
	        pibCurrentStatus.setPersonRespMessage(messageBody);
	        
		    try
	        {
		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
	        }
	        catch(UserNotFoundException ue)
	        {
	        	pibCurrentStatus.setPersonResponsible("");
	        	pibCurrentStatus.setPersonRespEmail("");
	        }
		    pibCurrentStatus.setNextStep("Advise Legal Approval");
		    pibCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
		    pibCurrentStatus.setMandatory(true);
		    pibCurrentStatusList.add(pibCurrentStatus);
    	}
    	
    	
    	//Brief Approval and Agency intimation
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Brief Approval and Agency intimation");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    	        String nextStepSubject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                pibCurrentStatus.setNextStepSubject(nextStepSubject);
                pibCurrentStatus.setNextStepMessage(nextStepMessage);
                
                pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients(SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME, projectID, emIds.get(0)));
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("");
    		    pibCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getPibCompletionDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(initiationList.get(0).getPibCompletionDate());
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Brief Approval and Agency intimation");    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    	        String nextStepSubject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                pibCurrentStatus.setNextStepSubject(nextStepSubject);
                pibCurrentStatus.setNextStepMessage(nextStepMessage);
                
                pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients(SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME, projectID, emIds.get(0)));
                
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    if((initiationList.get(0).getLegalApprovalRcvd() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals(""))
        				|| initiationList.get(0).getLegalApprovalNotReq())
    		    {
    		    	pibCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	pibCurrentStatus.setNextStepEnable(false);
    		    }
    		    pibCurrentStatus.setNextStep("Send Brief to Agency");
    		    pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		
    	}
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
    	else
    	{
    		pibCurrentStatus=new ProjectCurrentStatus();
			pibCurrentStatus.setActivityDesc("Brief Approval and Agency intimation");    		      
		    pibCurrentStatus.setStatus("Pending");

		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	        pibCurrentStatus.setPersonRespSubject(subject);
	        pibCurrentStatus.setPersonRespMessage(messageBody);
	        
	        String nextStepSubject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
            String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
            
            pibCurrentStatus.setNextStepSubject(nextStepSubject);
            pibCurrentStatus.setNextStepMessage(nextStepMessage);
            
            pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients(SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME, projectID, emIds.get(0)));
            
	        
		    try
	        {
		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
	        }
	        catch(UserNotFoundException ue)
	        {
	        	pibCurrentStatus.setPersonResponsible("");
	        	pibCurrentStatus.setPersonRespEmail("");
	        }
		   
		   	pibCurrentStatus.setNextStepEnable(false);
		   
		    pibCurrentStatus.setNextStep("Send Brief to Agency");
		    pibCurrentStatus.setMandatory(true);
		    pibCurrentStatusList.add(pibCurrentStatus);
    	}
    	
    	//Agency Waiver Approval
    	//PIBMethodologyWaiver pibKantarMW = pibManager.getPIBKantarMethodologyWaiver(projectID, emIds.get(0));
    	if(pibKantarMW!=null)
    	{
    		if(pibKantarMW.getIsApproved()!=null && pibKantarMW.getIsApproved()==1)
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Agency Waiver Approval");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibKantarMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibKantarMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    if(pibKantarMW.getModifiedDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(new Date(pibKantarMW.getModifiedDate()));
    		    }
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Agency Waiver Approval");    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibKantarMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibKantarMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    	}
    	return pibCurrentStatusList;
    }
    @Override
    public List<ProjectStagePendingFields> getPIBPendingFields(List<ProjectInitiation> initiationList, Long projectID, List<Long> emIds)
    {
    	List<ProjectStagePendingFields> pibPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	Map<Integer, List<AttachmentBean>> attachmentMap = pibManager.getDocumentAttachment(projectID, emIds.get(0));
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		ProjectStagePendingFields pibPendingFields = new ProjectStagePendingFields();
    		if(initiationList.get(0).getLatestEstimate()!=null)
    		{
    			pibPendingFields.setFieldName("Latest Estimate");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields.setFieldName("Latest Estimate");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getBizQuestion()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Business Question");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Business Question");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getResearchObjective()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Research Objectives(s)");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Research Objectives(s)");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getActionStandard()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Action Standard(s)");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Action Standard(s)");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getResearchDesign()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Methodology Approach and Research Design");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Methodology Approach and Research Design");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getSampleProfile()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Sample Profile(Research)");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Sample Profile(Research)");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getStimulusMaterial()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Stimulus Material");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Stimulus Material");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getOthers()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Comments");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Comments");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(initiationList.get(0).getStimuliDate()!=null)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Date Stimuli Available (in Research Agency)");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Date Stimuli Available (in Research Agency)");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		PIBReporting pibReporting = pibManager.getPIBReporting(projectID);
    		
    		//https://svn.sourcen.com/issues/19814
    		if(pibReporting!=null && (pibReporting.getFullreport() || pibReporting.getTopLinePresentation() || pibReporting.getPresentation() || pibReporting.getGlobalSummary()))
       		{
    			pibPendingFields = new ProjectStagePendingFields();
        		pibPendingFields.setFieldName("Reporting Requirement");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
        		pibPendingFields.setFieldName("Reporting Requirement");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		
    		if(pibReporting!=null && !StringUtils.isEmpty(pibReporting.getOtherReportingRequirements()))
       		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Reporting Requirements");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Reporting Requirements");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		
    		PIBStakeholderList pibStakeholderList = pibManager.getPIBStakeholderList(projectID);
    		if(pibStakeholderList!=null && pibStakeholderList.getAgencyContact1()!=null && pibStakeholderList.getAgencyContact1()>0)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Agency Contact");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Agency Contact");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
			
    		if(pibStakeholderList!=null && pibStakeholderList.getGlobalLegalContact()!=null && pibStakeholderList.getGlobalLegalContact()>0)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Legal Contact");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Legal Contact");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(pibStakeholderList!=null && pibStakeholderList.getProductContact()!=null && pibStakeholderList.getProductContact()>0)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Product Contact");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Product Contact");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
			
    	}
    	return pibPendingFieldsList;
    }
    @Override
    public List<ProjectCurrentStatus> getProposalStatusList(List<ProposalInitiation> initiationList, final long projectID, String stageUrl)
    {
    	//List<ProposalInitiation> initiationList = proposalManager.getProposalDetails(projectID);
    	Project project = synchroProjectManager.get(projectID);
    	List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	List<EndMarketInvestmentDetail> emDetailList = synchroProjectManager.getEndMarketDetails(projectID);
    	
    	//String stageUrl = baseUrl+"/synchro/proposal-details!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	
    	List<ProjectCurrentStatus> proposalCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus proposalCurrentStatus = null;
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SAVED.ordinal())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Project Proposal");    		   
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		        
    			proposalCurrentStatus.setStatus("Complete");
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getProposalSaveDate()!=null)
    		    {
    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getProposalSaveDate());
    		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Project Proposal");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("Complete Proposal");
    		    proposalCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
    		    proposalCurrentStatus.setMandatory(true);
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SUBMITTED.ordinal())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Submission");    		      
    			proposalCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("agency.submit.proposal.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("agency.submit.proposal.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    		    	//proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(true);
    		    
    		    if(initiationList.get(0).getProposalSubmitDate()!=null)
    		    {
    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getProposalSubmitDate());
    		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Submission");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("agency.submit.proposal.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("agency.submit.proposal.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    		    	//proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("Send Proposal to BAT");
    		    // Send Proposal To BAT is enabled only when the Proposall is SAVED and all mandatory fields are filled.
    		    if(initiationList.get(0).getStatus()==StageStatus.PROPOSAL_SAVED.ordinal())
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(false);
    		    }
    		    proposalCurrentStatus.setMandatory(true);
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getReqClarificationReqClicked())
    		{
	    		//https://svn.sourcen.com/issues/19944
    			if(!initiationList.get(0).getIsReqClariModification() || initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SUBMITTED.ordinal())
	    		{
	    			proposalCurrentStatus=new ProjectCurrentStatus();
	    			proposalCurrentStatus.setActivityDesc("Proposal Revision and Update to BAT");    		      
	    			proposalCurrentStatus.setStatus("Complete");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
	    			proposalCurrentStatus.setPersonRespSubject(subject);
	    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
	    		    
	    		    
	    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.spi.subject", JiveGlobals.getLocale());
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.spi.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                
	                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
	                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    		    
	    		    try
	    	        {
	    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
	    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
	    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	proposalCurrentStatus.setPersonResponsible("");
	    	        	proposalCurrentStatus.setPersonRespEmail("");
	    	        	proposalCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    proposalCurrentStatus.setNextStep("");
	    		    proposalCurrentStatus.setMandatory(true);
	    		    
	    		    if(initiationList.get(0).getReqClarificationReqDate()!=null)
	    		    {
	    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getReqClarificationReqDate());
	    		    }
	    		    
	    		    proposalCurrentStatusList.add(proposalCurrentStatus);
	    		}
	    		else
	    		{
	    			proposalCurrentStatus=new ProjectCurrentStatus();
	    			proposalCurrentStatus.setActivityDesc("Proposal Revision and Update to BAT");    		      
	    			proposalCurrentStatus.setStatus("Pending");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
	    			proposalCurrentStatus.setPersonRespSubject(subject);
	    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
	    		    
	    		    
	    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.spi.subject", JiveGlobals.getLocale());
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.spi.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                
	                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
	                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    		    
	    		    try
	    	        {
	    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
	    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
	    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	proposalCurrentStatus.setPersonResponsible("");
	    	        	proposalCurrentStatus.setPersonRespEmail("");
	    	        	proposalCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    proposalCurrentStatus.setNextStep("Notify BAT about Proposal Revision");
	    		    proposalCurrentStatus.setMandatory(true);
	    		    proposalCurrentStatusList.add(proposalCurrentStatus);
	    		}
    		}
    		
    	}
    	
    	// https://svn.sourcen.com/issues/19944
    	/*
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SUBMITTED.ordinal() && initiationList.get(0).getIsSendToProjectOwner())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Review");    		      
    			proposalCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.projectowner.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(false);
    		    
    		    if(initiationList.get(0).getPropSendToOwnerDate()!=null)
    		    {
    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getPropSendToOwnerDate());
    		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Review");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.projectowner.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    // Send Proposal to Project Owner is enabled only when the Proposal is SUBMITTED.
    		    if(initiationList.get(0).getIsPropSubmitted())
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(false);
    		    }
    		    proposalCurrentStatus.setMandatory(false);
    		    proposalCurrentStatus.setNextStep("Send Proposal to Project Owner");
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	*/
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()==StageStatus.PROPOASL_AWARDED.ordinal())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Award");    		      
    			proposalCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getProposalAwardDate()!=null)
    		    {
    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getProposalAwardDate());
    		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Award");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("Award Proposal");
    		    // Award Proposal to Project Owner is enabled only when the Proposal is SUBMITTED.
    		    if(initiationList.get(0).getIsPropSubmitted())
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(false);
    		    }
    		    proposalCurrentStatus.setMandatory(true);
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	return proposalCurrentStatusList;
    	
    }
    
    @Override
    public List<ProjectStagePendingFields> getProposalPendingFields(List<ProposalInitiation> initiationList, Long projectID, List<Long> emIds)
    {
    	List<ProjectStagePendingFields> proposalPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		Map<Integer, List<AttachmentBean>> attachmentMap = proposalManager.getDocumentAttachment(projectID, emIds.get(0), initiationList.get(0).getAgencyID());
    		ProjectStagePendingFields proposalPendingFields = new ProjectStagePendingFields();
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getStimulusMaterialShipped()))
    		{
    			
    			proposalPendingFields.setFieldName("Stimulus Material to be shipped to");
    			proposalPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		else
    		{
    			
    			proposalPendingFields.setFieldName("Stimulus Material to be shipped to");
    			proposalPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		
    		ProposalEndMarketDetails proposalEMDetails = proposalManager.getProposalEMDetails(projectID, initiationList.get(0).getEndMarketID(), initiationList.get(0).getAgencyID());
    		if(proposalEMDetails!=null)
    		{
    			if(proposalEMDetails.getTotalCost()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Cost");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getIntMgmtCost()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("International Management Cost - Research Hub Cost");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("International Management Cost - Research Hub Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getLocalMgmtCost()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Local Management Cost");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Local Management Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getFieldworkCost()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Fieldwork Cost");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Fieldwork Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getOperationalHubCost()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Operational Hub Cost");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Operational Hub Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getOtherCost()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Other Cost");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Other Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(StringUtils.isEmpty(proposalEMDetails.getProposedFWAgencyNames()))
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Name of Proposed Fieldwork Agencies");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Name of Proposed Fieldwork Agencies");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getFwStartDate()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Estimated Fieldwork Start");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Estimated Fieldwork Start");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getFwEndDate()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Estimated Fieldwork Completion");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Estimated Fieldwork Completion");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getDataCollectionMethod()!=null && proposalEMDetails.getDataCollectionMethod().size()>0)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Data Collection Methods");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Data Collection Methods");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getTotalNoInterviews()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Number of Interviews");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Number of Interviews");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getTotalNoOfVisits()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Number of Visits per Respondent");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Number of Visits per Respondent");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
    			if(proposalEMDetails.getAvIntDuration()!=null)
        		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Average Interview Duration (in minutes)");
        			proposalPendingFields.setInformationProvided(true);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}
        		else
        		{
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Average Interview Duration (in minutes)");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		}

    		}
    		//https://svn.sourcen.com/issues/19812
    		else
    		{
				proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Total Cost");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("International Management Cost - Research Hub Cost");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Local Management Cost");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Fieldwork Cost");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Operational Hub Cost");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Other Cost");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Name of Proposed Fieldwork Agencies");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
			
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Estimated Fieldwork Start");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
			
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Estimated Fieldwork Completion");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
			
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Data Collection Methods");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
			
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Total Number of Interviews");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
			
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Total Number of Visits per Respondent");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		
			
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Average Interview Duration (in minutes)");
    			proposalPendingFields.setInformationProvided(false);
    			proposalPendingFields.setAttachmentDone("N/A");
    			
    			proposalPendingFieldsList.add(proposalPendingFields);
    		    		
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getProposalCostTemplate()))
    		{
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Proposal and Cost Template");
    			proposalPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		else
    		{
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Proposal and Cost Template");
    			proposalPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		
			
    	}
    	return proposalPendingFieldsList;
    }
    @Override
    public List<ProjectCurrentStatus> getProjectSpecsStatusList(List<ProjectSpecsInitiation> initiationList,final long projectID, String stageUrl)
	{
    	//List<ProjectSpecsInitiation> initiationList = projectSpecsManager.getProjectSpecsInitiation(projectID);
    	Project project = synchroProjectManager.get(projectID);
    	List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	List<EndMarketInvestmentDetail> emDetailList = synchroProjectManager.getEndMarketDetails(projectID);
    	
    	Long awardedExternalAgency = synchroUtils.getAwardedExternalAgencyUserID(projectID, emIds.get(0));
    	
    	List<ProjectCurrentStatus> projectSpecsCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus projectSpecsCurrentStatus = null;
    	
    	//String stageUrl = baseUrl+"/synchro/project-specs!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		//https://svn.sourcen.com/issues/19820
    		if(initiationList.get(0).getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() && initiationList.get(0).getIsSendForApproval()==1)
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs");    		      
    			projectSpecsCurrentStatus.setStatus("Complete");
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    		        
    		    
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("");
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getProjectSpecsSaveDate()!=null)
    		    {
    		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getProjectSpecsSaveDate());
    		    }
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    		else
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs");    		      
    			projectSpecsCurrentStatus.setStatus("Pending");
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("Complete Project Specs");
    		    projectSpecsCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    	}
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() 
    				&& initiationList.get(0).getLegalApprovalCCCA() && initiationList.get(0).getLegalApproverCCCA()!=null && !initiationList.get(0).getLegalApproverCCCA().equals("")
    				&& initiationList.get(0).getLegalApprovalDG() && initiationList.get(0).getLegalApproverDG()!=null && !initiationList.get(0).getLegalApproverDG().equals("")
    				&& initiationList.get(0).getLegalApprovalQuestionnaire() && initiationList.get(0).getLegalApproverQuestionnaire()!=null && !initiationList.get(0).getLegalApproverQuestionnaire().equals("")
    				&& initiationList.get(0).getLegalApprovalScreener() && initiationList.get(0).getLegalApproverScreener()!=null && !initiationList.get(0).getLegalApproverScreener().equals("")
    				&& initiationList.get(0).getLegalApprovalStimulus() && initiationList.get(0).getLegalApproverStimulus()!=null && !initiationList.get(0).getLegalApproverStimulus().equals("")
    				)
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Legal Approval");    		      
    			projectSpecsCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("");
    		    projectSpecsCurrentStatus.setNextStepLink(stageUrl);
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getPsLegalApprovalDate()!=null)
    		    {
    		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getPsLegalApprovalDate());
    		    }
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    		else
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Legal Approval");    		      
    			projectSpecsCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("Advise Legal Approval");
    		    projectSpecsCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() && initiationList.get(0).getIsReqClariModification())
    		{
    			// This check is for https://svn.sourcen.com/issues/19822
    			if(initiationList.get(0).getIsSendForApproval()!=null && initiationList.get(0).getIsSendForApproval()==1)
    			{
	    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
	    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Revision and Update to BAT");    		      
	    			projectSpecsCurrentStatus.setStatus("Complete");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		              
		            projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
		            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	    			
	    		    try
	    	        {
	    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	//projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	projectSpecsCurrentStatus.setPersonResponsible("");
	    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		   // projectSpecsCurrentStatus.setNextStep("Notify BAT about Project Specs Revision");
	    		    projectSpecsCurrentStatus.setNextStep("");
	    		    projectSpecsCurrentStatus.setMandatory(true);
	    		    if(initiationList.get(0).getReqClarificationModDate()!=null)
	    		    {
	    		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getReqClarificationModDate());
	    		    }
	    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    			}
    			else
    			{
    				projectSpecsCurrentStatus=new ProjectCurrentStatus();
	    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Revision and Update to BAT");    		      
	    			projectSpecsCurrentStatus.setStatus("Pending");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		              
		            projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
		            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	    			
	    		    try
	    	        {
	    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	//projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	projectSpecsCurrentStatus.setPersonResponsible("");
	    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    projectSpecsCurrentStatus.setNextStep("Notify BAT about Project Specs Revision");
	    		    projectSpecsCurrentStatus.setMandatory(true);
	    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    			}
    		}
    	}
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()==StageStatus.PROJECT_SPECS_COMPLETED.ordinal())
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Approval");    		      
    			projectSpecsCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	              
	            nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            
    		    projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	            
    		    projectSpecsCurrentStatus.setNextStep("");
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getApprovedDate()!=null)
    		    {
    		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getApprovedDate());
    		    }
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    		else
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Approval");    		      
    			projectSpecsCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	              
	            nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            
    		    projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	            
    		    projectSpecsCurrentStatus.setNextStep("Approve Project Specs");
    		    
    		   // Approve Project Specs will be enabled only when the Send for Approval button is clicked.
    		    if(initiationList.get(0).getIsSendForApproval()!=null && initiationList.get(0).getIsSendForApproval()==1 && initiationList.get(0).getLegalApprovalCCCA() && initiationList.get(0).getLegalApproverCCCA()!=null && !initiationList.get(0).getLegalApproverCCCA().equals("")
        				&& initiationList.get(0).getLegalApprovalDG() && initiationList.get(0).getLegalApproverDG()!=null && !initiationList.get(0).getLegalApproverDG().equals("")
        				&& initiationList.get(0).getLegalApprovalQuestionnaire() && initiationList.get(0).getLegalApproverQuestionnaire()!=null && !initiationList.get(0).getLegalApproverQuestionnaire().equals("")
        				&& initiationList.get(0).getLegalApprovalScreener() && initiationList.get(0).getLegalApproverScreener()!=null && !initiationList.get(0).getLegalApproverScreener().equals("")
        				&& initiationList.get(0).getLegalApprovalStimulus() && initiationList.get(0).getLegalApproverStimulus()!=null && !initiationList.get(0).getLegalApproverStimulus().equals("")
        				)
    		    {
    		    	projectSpecsCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	projectSpecsCurrentStatus.setNextStepEnable(false);
    		    }
    		    
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    	}
    	return projectSpecsCurrentStatusList;
	}
    
    @Override
    public List<ProjectStagePendingFields> getProjectSpecsPendingFields(List<ProjectSpecsInitiation> initiationList, Long projectID, List<Long> emIds)
    {
    	List<ProjectStagePendingFields> projectSpecsPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	Map<Integer, List<AttachmentBean>> attachmentMap = projectSpecsManager.getDocumentAttachment(projectID, emIds.get(0));
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		ProjectStagePendingFields projectSpecsPendingFields = new ProjectStagePendingFields();
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getStimulusMaterial()))
    		{
    			
    			projectSpecsPendingFields.setFieldName("Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			
    			projectSpecsPendingFields.setFieldName("Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getScreener()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Screener");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Screener");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getConsumerCCAgreement()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Consumer Contract and Confidentiality Agreement");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Consumer Contract and Confidentiality Agreement");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getQuestionnaire()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Questionnaire/Discussion guide");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Questionnaire/Discussion guide");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getDiscussionguide()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Actual Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Actual Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
   		
    	}
    	return projectSpecsPendingFieldsList;
    }
    @Override
    public List<ProjectCurrentStatus> getReportSummaryStatusList(List<ReportSummaryInitiation> initiationList ,final long projectID, String stageUrl)
	{
    	//List<ReportSummaryInitiation> initiationList = reportSummaryManager.getReportSummaryInitiation(projectID);
    	Project project = synchroProjectManager.get(projectID);
    	List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	List<EndMarketInvestmentDetail> emDetailList = synchroProjectManager.getEndMarketDetails(projectID);
    	
    	Long awardedExternalAgency = synchroUtils.getAwardedExternalAgencyUserID(projectID, emIds.get(0));
    	
    	List<ProjectCurrentStatus> reportSummaryCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus reportSummaryCurrentStatus = null;
    	
    	//String stageUrl = baseUrl+"/synchro/report-summary!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getSendForApproval())
    		{
    			// This check is because of : https://svn.sourcen.com/issues/19826
    			//https://svn.sourcen.com/issues/19916
    			if((initiationList.get(0).getFullReport() || initiationList.get(0).getSummaryReport() || initiationList.get(0).getSummaryForIRIS()) && !(initiationList.get(0).getNeedRevision()))
    			{
    				
	    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
	    			reportSummaryCurrentStatus.setActivityDesc("Reports");    		      
	    			reportSummaryCurrentStatus.setStatus("Complete");
	    			
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                 
	                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	    			
	    		    try
	    	        {
	    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	reportSummaryCurrentStatus.setPersonResponsible("");
	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    reportSummaryCurrentStatus.setNextStep("");
	    		    reportSummaryCurrentStatus.setMandatory(true);
	    		    
	    		    if(initiationList.get(0).getRepSummarySaveDate()!=null)
	    		    {
	    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getRepSummarySaveDate());
	    		    }
	    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			}
    			else
    			{
    				reportSummaryCurrentStatus=new ProjectCurrentStatus();
        			reportSummaryCurrentStatus.setActivityDesc("Reports");    		      
        			reportSummaryCurrentStatus.setStatus("Pending");
        			
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
        			
        			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                     
                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
        			
        		    try
        	        {
        		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
        		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
        		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	reportSummaryCurrentStatus.setPersonResponsible("");
        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
        	        }
        		    reportSummaryCurrentStatus.setNextStep("Upload Reports");
        		  //https://svn.sourcen.com/issues/19945 - Now upload report will be a link to Report Stage
        		    reportSummaryCurrentStatus.setNextStepLink(stageUrl);
        		    
        		    
        		    //Upload Reports will be enabled only when the Report is SAVED and the Full Report and Summary for IRIS checkbox is selected
        		    if(initiationList.get(0).getStatus()==StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getFullReport()!=null 
        		    		&& initiationList.get(0).getFullReport() && initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS())
        		    {
        		    	reportSummaryCurrentStatus.setNextStepEnable(true);
        		    }
        		    else
        		    {
        		    	reportSummaryCurrentStatus.setNextStepEnable(false);
        		    }
        		    reportSummaryCurrentStatus.setMandatory(true);
        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			}
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    reportSummaryCurrentStatus.setNextStep("Upload Reports");
    		    
    		   //https://svn.sourcen.com/issues/19945 - Now upload report will be a link to Report Stage
    		    reportSummaryCurrentStatus.setNextStepLink(stageUrl);
    		    
    		   //Upload Reports will be enabled only when the Report is SAVED and the Full Report and Summary for IRIS checkbox is selected
    		    if(initiationList.get(0).getStatus()==StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getFullReport()!=null 
    		    		&& initiationList.get(0).getFullReport() && initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS())
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    	}
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		//if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getNeedRevision())
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getNeedRevisionClicked())
    		{
    			if(!initiationList.get(0).getNeedRevision())
    			{

	    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
	    			reportSummaryCurrentStatus.setActivityDesc("Reports Revision and Update to BAT");    		      
	    			reportSummaryCurrentStatus.setStatus("Complete");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                 
	                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    			
	    		    try
	    	        {
	    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    //	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	reportSummaryCurrentStatus.setPersonResponsible("");
	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    reportSummaryCurrentStatus.setNextStep("");
	    		    reportSummaryCurrentStatus.setMandatory(true);
	    		    
	    		    if(initiationList.get(0).getNeedRevisionClickDate()!=null)
	    		    {
	    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getNeedRevisionClickDate());
	    		    }
	    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			
    			}
    			else
    			{
	    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
	    			reportSummaryCurrentStatus.setActivityDesc("Reports Revision and Update to BAT");    		      
	    			reportSummaryCurrentStatus.setStatus("Pending");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                 
	                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    			
	    		    try
	    	        {
	    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    //	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	reportSummaryCurrentStatus.setPersonResponsible("");
	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    reportSummaryCurrentStatus.setNextStep("Notify BAT about Reports Revision");
	    		    reportSummaryCurrentStatus.setMandatory(true);
	    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			}
    		}
    		
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() 
    				&& initiationList.get(0).getLegalApproval() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals(""))
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Legal Approval");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        }
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    
    		    if(initiationList.get(0).getRepSummaryLegalApprovalDate()!=null)
    		    {
    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getRepSummaryLegalApprovalDate());
    		    }
    		    
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Legal Approval");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        }
    		    reportSummaryCurrentStatus.setNextStep("Advise Legal Approval");
    		    reportSummaryCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() &&  initiationList.get(0).getIsSPIApproved())
    				
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports Approval");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getSpiApprovalDate()!=null)
    		    {
    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getSpiApprovalDate());
    		    }
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports Approval");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
    		    reportSummaryCurrentStatus.setNextStep("Approve Reports");
    		    
    		  //Approve Upload Reports will be enabled only when the Report is SAVED and the Full Report and Summary for IRIS checkbox is selected and Send for Approval is clicked
    		    if(initiationList.get(0).getStatus()==StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getFullReport()!=null 
    		    		&& initiationList.get(0).getFullReport() && initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS()
    		    		&& initiationList.get(0).getSendForApproval()!=null && initiationList.get(0).getSendForApproval()
    		    		&& initiationList.get(0).getLegalApproval() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals(""))
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}

    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() &&  initiationList.get(0).getIsSPIApproved() &&  initiationList.get(0).getUploadToIRIS())
    				
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for IRIS Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	//reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String uploadToIrisEmail = JiveGlobals.getJiveProperty("upload.iris.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToIrisEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.iris.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    
    		    if(initiationList.get(0).getIrisUploadDate()!=null)
    		    {
    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getIrisUploadDate());
    		    }
    		    
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for IRIS Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	//reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String uploadToIrisEmail = JiveGlobals.getJiveProperty("upload.iris.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToIrisEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.iris.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    reportSummaryCurrentStatus.setNextStep("Request for Upload to IRIS");
    		    
    		  //Request for Upload to IRIS Reports will be enabled only when the Report is APPROVED and Legal Apprval is done
    		    if( initiationList.get(0).getIsSPIApproved()!=null && initiationList.get(0).getIsSPIApproved()
    		    		&& initiationList.get(0).getLegalApproval()!=null && initiationList.get(0).getLegalApproval())
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() &&  initiationList.get(0).getIsSPIApproved() &&  initiationList.get(0).getUploadToCPSIDdatabase())
    				
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for C-PSI Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	//reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    String uploadToCPSIDatabaseEmail = JiveGlobals.getJiveProperty("upload.cpsi.database.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToCPSIDatabaseEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.c.psi.database.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.c.psi.database.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getCpsiUploadDate()!=null)
    		    {
    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getCpsiUploadDate());
    		    }
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for C-PSI Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    //	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        //	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String uploadToCPSIDatabaseEmail = JiveGlobals.getJiveProperty("upload.cpsi.database.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToCPSIDatabaseEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.c.psi.database.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.c.psi.database.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
    		    reportSummaryCurrentStatus.setNextStep("Request for Upload to C-PSI Database");
    		    
    		  //Request for Upload to C-PSI Database Reports will be enabled only when the Report is APPROVED and Legal Apprval is done
    		    if( initiationList.get(0).getIsSPIApproved()!=null && initiationList.get(0).getIsSPIApproved()
    		    		&& initiationList.get(0).getLegalApproval()!=null && initiationList.get(0).getLegalApproval())
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}
    	
    	
    	return reportSummaryCurrentStatusList;
	}
    
    @Override
    public List<ProjectStagePendingFields> getReportSummaryPendingFields(List<ReportSummaryInitiation> initiationList, Long projectID, List<Long> emIds)
    {
    	List<ProjectStagePendingFields> reportSummaryPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	Map<Integer, List<AttachmentBean>> attachmentMap = reportSummaryManager.getDocumentAttachment(projectID, emIds.get(0));
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		ProjectStagePendingFields reportSummaryPendingFields = new ProjectStagePendingFields();
    		
    		if(initiationList.get(0).getFullReport()!=null && initiationList.get(0).getFullReport())
    		{
    			
    			reportSummaryPendingFields.setFieldName("Research Report(s)");
    			reportSummaryPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		else
    		{
    			
    			reportSummaryPendingFields.setFieldName("Research Report(s)");
    			reportSummaryPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		
    		if(initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS())
    		{
    			reportSummaryPendingFields = new ProjectStagePendingFields();
    			reportSummaryPendingFields.setFieldName("Summary for IRIS");
    			reportSummaryPendingFields.setInformationProvided(true);
    			//https://svn.sourcen.com/issues/20030
    			/*if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}*/
    			reportSummaryPendingFields.setAttachmentDone("Yes");
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		else
    		{
    			reportSummaryPendingFields = new ProjectStagePendingFields();
    			reportSummaryPendingFields.setFieldName("Summary for IRIS");
    			reportSummaryPendingFields.setInformationProvided(false);
    		/*	if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}*/
    			reportSummaryPendingFields.setAttachmentDone("No");
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		
    			
    	}
    	return reportSummaryPendingFieldsList;
    }
    
    @Override
    public List<ProjectCurrentStatus> getProjectEvaluationStatusList(final long projectID, String stageUrl)
    {
    	List<ProjectEvaluationInitiation> initiationList = projectEvaluationManager.getProjectEvaluationInitiation(projectID);
    	Project project = synchroProjectManager.get(projectID);
    	
    	List<ProjectCurrentStatus> projectEvaluationCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus projectEvaluationCurrentStatus = null;
    	
    	//String stageUrl = baseUrl+"/synchro/project-eval!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()==StageStatus.PROJ_EVAL_COMPLETED.ordinal())
    		{
    			projectEvaluationCurrentStatus=new ProjectCurrentStatus();
    			projectEvaluationCurrentStatus.setActivityDesc("Agency Evaluation");    		      
    			projectEvaluationCurrentStatus.setStatus("Complete");
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectEvaluationCurrentStatus.getActivityDesc()));
    			projectEvaluationCurrentStatus.setPersonRespSubject(subject);
    			projectEvaluationCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectEvaluationCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	projectEvaluationCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectEvaluationCurrentStatus.setPersonResponsible("");
    	        	projectEvaluationCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectEvaluationCurrentStatus.setNextStep("Complete Evaluation");
    		    projectEvaluationCurrentStatus.setNextStepLink(stageUrl);
    		    if(initiationList.get(0).getModifiedDate()!=null)
    		    {
    		    	projectEvaluationCurrentStatus.setCompletionDate(new Date(initiationList.get(0).getModifiedDate()));
    		    }
    		    projectEvaluationCurrentStatusList.add(projectEvaluationCurrentStatus);
    		}
    		
    	}
    	return projectEvaluationCurrentStatusList;
    }
    
    @Override
    public List<ProjectCurrentStatus> getPIBMultiStatusList(final long projectID, String stageUrl,List<Long> emIds, PIBMethodologyWaiver pibMW, PIBMethodologyWaiver pibKantarMW, List<FundingInvestment> fundingInvestmentList) {
      
    	List<ProjectInitiation> initiationList = pibManager.getPIBDetails(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	List<ProjectInitiation> endMarketInitiationList = new ArrayList<ProjectInitiation>();
    	
    //	List<FundingInvestment> fundingInvestmentList = synchroProjectManager.getProjectInvestments(projectID);
    	Map<Long, FundingInvestment> fundingInvestmentMap = new HashMap<Long, FundingInvestment>();
    	if(fundingInvestmentList!=null && fundingInvestmentList.size()>0)
    	{
    		for(FundingInvestment fi : fundingInvestmentList)
    		{
    			if(fi.getAboveMarket())
    			{
    				fundingInvestmentMap.put(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, fi);
    			}
    			else
    			{
    				fundingInvestmentMap.put(fi.getFieldworkMarketID(), fi);
    			}
    		}
    	}
    	for(Long endMarketId : emIds)
    	{
    		List<ProjectInitiation> pibEMList = pibManager.getPIBDetails(projectID,endMarketId);
    		if(pibEMList!=null && pibEMList.size()>0)
    		{
    			endMarketInitiationList.add(pibEMList.get(0));
    		}
    	}
    	
    	Project project = synchroProjectManager.get(projectID);
    	//List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	List<EndMarketInvestmentDetail> emDetailList = synchroProjectManager.getEndMarketDetails(projectID);
    	
    	List<ProjectCurrentStatus> pibCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus pibCurrentStatus = null;
    	
    	//String stageUrl = baseUrl+"/synchro/pib-details!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	
    	//Project Brief - Above Market  
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		//if(initiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
    		if(initiationList.get(0).getStatus()>=SynchroGlobal.StageStatus.PIB_SAVED.ordinal())
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Project Brief - Above Market");    		      
    		    pibCurrentStatus.setStatus("Complete");
    		    pibCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    		    
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("");
    		    pibCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getPibSaveDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(initiationList.get(0).getPibSaveDate());
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Project Brief - Above Market");    		      
    		    pibCurrentStatus.setStatus("Pending");
    		    pibCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    		    
    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("Complete Project Brief");
            	pibCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
            	pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		
    	}
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
		else
		{
			pibCurrentStatus=new ProjectCurrentStatus();
			pibCurrentStatus.setActivityDesc("Project Brief - Above Market");    		      
		    pibCurrentStatus.setStatus("Pending");
		    pibCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		    
		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	        pibCurrentStatus.setPersonRespSubject(subject);
	        pibCurrentStatus.setPersonRespMessage(messageBody);
	        
		    try
	        {
		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        }
	        catch(UserNotFoundException ue)
	        {
	        	pibCurrentStatus.setPersonResponsible("");
	        	pibCurrentStatus.setPersonRespEmail("");
	        }
		    pibCurrentStatus.setNextStep("Complete Project Brief");
        	pibCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
        	pibCurrentStatus.setMandatory(true);
		    pibCurrentStatusList.add(pibCurrentStatus);
		}
    	
    	// Legal Approval - Above Market
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if((initiationList.get(0).getLegalApprovalRcvd() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals(""))
    				|| initiationList.get(0).getLegalApprovalNotReq())
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Legal Approval - Above Market");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("");
    		    pibCurrentStatus.setMandatory(true);
    		    
    		    if(initiationList.get(0).getPibLegalApprovalDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(initiationList.get(0).getPibLegalApprovalDate());
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Legal Approval - Above Market");    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("Advise Legal Approval - Above Market");
    		    pibCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
    		    pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		
    	}
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
    	else
    	{
    		pibCurrentStatus=new ProjectCurrentStatus();
			pibCurrentStatus.setActivityDesc("Legal Approval - Above Market");    		      
		    pibCurrentStatus.setStatus("Pending");

		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	        pibCurrentStatus.setPersonRespSubject(subject);
	        pibCurrentStatus.setPersonRespMessage(messageBody);
	        
		    try
	        {
		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        }
	        catch(UserNotFoundException ue)
	        {
	        	pibCurrentStatus.setPersonResponsible("");
	        	pibCurrentStatus.setPersonRespEmail("");
	        }
		    pibCurrentStatus.setNextStep("Advise Legal Approval - Above Market");
		    pibCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
		    pibCurrentStatus.setMandatory(true);
		    pibCurrentStatusList.add(pibCurrentStatus);
    	}
    	
    	// Cost Approval - Above Market
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(fundingInvestmentMap!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getApprovalStatus()!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getApprovalStatus() )
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Cost Approval - Above Market");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    if(fundingInvestmentMap!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()!=null)
    		    {
	    	        try
	    	        {
	    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()).getName());
	    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	pibCurrentStatus.setPersonResponsible("");
	    	        	pibCurrentStatus.setPersonRespEmail("");
	    	        }
    		    }
    		    else
    		    {
    		    	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    		    }
    		    pibCurrentStatus.setNextStep("");
    		    pibCurrentStatus.setMandatory(false);
    		    if(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getApprovalDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getApprovalDate());
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Cost Approval - Above Market");    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    	        if(fundingInvestmentMap!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()!=null)
    		    {
	    	        try
	    	        {
	    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()).getName());
	    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	pibCurrentStatus.setPersonResponsible("");
	    	        	pibCurrentStatus.setPersonRespEmail("");
	    	        }
    		    }
    	        else
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    	        
    		    pibCurrentStatus.setNextStep("Provide Cost Approval - Above Market");
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		
    	}
    	
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
    	else
    	{

			pibCurrentStatus=new ProjectCurrentStatus();
			pibCurrentStatus.setActivityDesc("Cost Approval - Above Market");    		      
		    pibCurrentStatus.setStatus("Pending");

		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	        pibCurrentStatus.setPersonRespSubject(subject);
	        pibCurrentStatus.setPersonRespMessage(messageBody);
	        
	        if(fundingInvestmentMap!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)!=null && fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()!=null)
		    {
    	        try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).getProjectContact()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
		    }
	        else
	        {
	        	pibCurrentStatus.setPersonResponsible("");
	        	pibCurrentStatus.setPersonRespEmail("");
	        }
	        
		    pibCurrentStatus.setNextStep("Provide Cost Approval - Above Market");
		    pibCurrentStatus.setMandatory(false);
		    pibCurrentStatusList.add(pibCurrentStatus);
		
    	}
    	
    	// Project Brief - End Market
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ProjectInitiation pibEM: endMarketInitiationList )
    		{
	    		//if(pibEM.getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
    			if(pibEM.getStatus()>=SynchroGlobal.StageStatus.PIB_SAVED.ordinal())
	    		{
	    			pibCurrentStatus=new ProjectCurrentStatus();
	    			pibCurrentStatus.setActivityDesc("Project Brief - " + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));    		      
	    		    pibCurrentStatus.setStatus("Complete");
	    		    pibCurrentStatus.setEndMarketID(pibEM.getEndMarketID());
	    		    
	    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	    	        pibCurrentStatus.setPersonRespSubject(subject);
	    	        pibCurrentStatus.setPersonRespMessage(messageBody);
	    	        
	    		    //https://svn.sourcen.com/issues/19899
	    	        if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
	        		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
        		    //https://svn.sourcen.com/issues/19921
	    	        else
        		    {
        		    	try
	        	        {
        		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
            	        	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
	    		    pibCurrentStatus.setNextStep("");
	    		    pibCurrentStatus.setMandatory(true);
	    		    if(pibEM.getPibSaveDate()!=null)
	    		    {
	    		    	pibCurrentStatus.setCompletionDate(pibEM.getPibSaveDate());
	    		    }
	    		    pibCurrentStatusList.add(pibCurrentStatus);
	    		}
	    		else
	    		{
	    			pibCurrentStatus=new ProjectCurrentStatus();
	    			pibCurrentStatus.setActivityDesc("Project Brief - " + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));	      
	    		    pibCurrentStatus.setStatus("Pending");
	    		    pibCurrentStatus.setEndMarketID(pibEM.getEndMarketID());
	    		    
	    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	    	        pibCurrentStatus.setPersonRespSubject(subject);
	    	        pibCurrentStatus.setPersonRespMessage(messageBody);
	    	        
	    	        //https://svn.sourcen.com/issues/19899
	    	        if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
	        		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
	    	      //https://svn.sourcen.com/issues/19921
	    	        else
        		    {
        		    	try
	        	        {
        		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
            	        	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
        		    
	    		    pibCurrentStatus.setNextStep("Complete Project Brief");
	            	pibCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+pibEM.getEndMarketID()+"&validationError=true");
	
	            	pibCurrentStatus.setMandatory(true);
	    		    pibCurrentStatusList.add(pibCurrentStatus);
	    		}
    			

    			if((pibEM.getLegalApprovalRcvd() && pibEM.getLegalApprover()!=null && !pibEM.getLegalApprover().equals(""))
        				|| pibEM.getLegalApprovalNotReq())
        		{
        			pibCurrentStatus=new ProjectCurrentStatus();
        			pibCurrentStatus.setActivityDesc("Legal Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));		      
        		    pibCurrentStatus.setStatus("Complete");

        		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
        	        pibCurrentStatus.setPersonRespSubject(subject);
        	        pibCurrentStatus.setPersonRespMessage(messageBody);
        	       
        	      //  List<FundingInvestment> fundingInvesment = synchroProjectManager.getProjectInvestments(projectID, pibEM.getEndMarketID());
        		    if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
	        		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
        		  //https://svn.sourcen.com/issues/19921
	    	        else
        		    {
        		    	try
	        	        {
        		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
            	        	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
        		    pibCurrentStatus.setNextStep("");
        		    pibCurrentStatus.setMandatory(true);
        		    if(pibEM.getPibLegalApprovalDate()!=null)
        		    {
        		    	pibCurrentStatus.setCompletionDate(pibEM.getPibLegalApprovalDate());
        		    }
        		    pibCurrentStatusList.add(pibCurrentStatus);
        		}
        		else
        		{
        			pibCurrentStatus=new ProjectCurrentStatus();
        			pibCurrentStatus.setActivityDesc("Legal Approval - "  + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));    		      
        		    pibCurrentStatus.setStatus("Pending");

        		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
        	        pibCurrentStatus.setPersonRespSubject(subject);
        	        pibCurrentStatus.setPersonRespMessage(messageBody);
        	        
        	       // List<FundingInvestment> fundingInvesment = synchroProjectManager.getProjectInvestments(projectID, pibEM.getEndMarketID());
        	        if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
	        		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
        	      //https://svn.sourcen.com/issues/19921
	    	        else
        		    {
        		    	try
	        	        {
        		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
            	        	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	pibCurrentStatus.setPersonResponsible("");
	        	        	pibCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
        		    pibCurrentStatus.setNextStep("Advise Legal Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));
        		    pibCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+pibEM.getEndMarketID()+"&legalApprover=true");
        		    pibCurrentStatus.setMandatory(true);
        		    pibCurrentStatusList.add(pibCurrentStatus);
        		}
    			

	    		if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getApprovalStatus()!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getApprovalStatus() )
	    		{
	    			pibCurrentStatus=new ProjectCurrentStatus();
	    			pibCurrentStatus.setActivityDesc("Cost Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));
	    		    pibCurrentStatus.setStatus("Complete");
	
	    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	    	        pibCurrentStatus.setPersonRespSubject(subject);
	    	        pibCurrentStatus.setPersonRespMessage(messageBody);
	    	        
	    		    try
	    	        {
	    		    	if(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
	    		    	{
		    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
		    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	    		    	}
	    		    	else
	    		    	{
	    		    		pibCurrentStatus.setPersonResponsible("");
		    	        	pibCurrentStatus.setPersonRespEmail("");
	    		    	}
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	pibCurrentStatus.setPersonResponsible("");
	    	        	pibCurrentStatus.setPersonRespEmail("");
	    	        }
	    		    pibCurrentStatus.setNextStep("");
	    		    pibCurrentStatus.setMandatory(false);
	    		    if(fundingInvestmentMap.get(pibEM.getEndMarketID()).getApprovalDate()!=null)
	    		    {
	    		    	pibCurrentStatus.setCompletionDate(fundingInvestmentMap.get(pibEM.getEndMarketID()).getApprovalDate());
	    		    }
	    		    pibCurrentStatusList.add(pibCurrentStatus);
	    		}
	    		else
	    		{
	    			pibCurrentStatus=new ProjectCurrentStatus();
	    			pibCurrentStatus.setActivityDesc("Cost Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));
	    		    pibCurrentStatus.setStatus("Pending");
	
	    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	    	        pibCurrentStatus.setPersonRespSubject(subject);
	    	        pibCurrentStatus.setPersonRespMessage(messageBody);
	    	        
	    		    try
	    	        {
	    		    	if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
	    		    	{
		    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
		    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	    		    	}
	    		    	else
	    		    	{
	    		    		pibCurrentStatus.setPersonResponsible("");
		    	        	pibCurrentStatus.setPersonRespEmail("");
	    		    	}
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	pibCurrentStatus.setPersonResponsible("");
	    	        	pibCurrentStatus.setPersonRespEmail("");
	    	        }
	    		    pibCurrentStatus.setNextStep("Provide Cost Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(pibEM.getEndMarketID()+"")));
	    		    pibCurrentStatus.setMandatory(false);
	    		    pibCurrentStatusList.add(pibCurrentStatus);
	    		}
    		
    		
    		}
    		
    	}
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
    	else
    	{
    		for(Long endMarketId : emIds)
        	{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Project Brief - " + SynchroGlobal.getEndMarkets().get(new Integer(endMarketId+"")));	      
    		    pibCurrentStatus.setStatus("Pending");
    		    pibCurrentStatus.setEndMarketID(endMarketId);
    		    
    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    	        //https://svn.sourcen.com/issues/19899
    	        if(fundingInvestmentMap!=null && fundingInvestmentMap.get(endMarketId)!=null && fundingInvestmentMap.get(endMarketId).getSpiContact()!=null)
    		    {
        	        try
        	        {
        		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(endMarketId).getSpiContact()).getName());
        		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(endMarketId).getSpiContact()).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	pibCurrentStatus.setPersonResponsible("");
        	        	pibCurrentStatus.setPersonRespEmail("");
        	        }
    		    }
    	      //https://svn.sourcen.com/issues/19921
    	        else
    		    {
    		    	try
        	        {
    		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        	        	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	pibCurrentStatus.setPersonResponsible("");
        	        	pibCurrentStatus.setPersonRespEmail("");
        	        }
    		    	
    		    }
    		    pibCurrentStatus.setNextStep("Complete Project Brief");
            	pibCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+endMarketId+"&validationError=true");

            	pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		    

    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Legal Approval - "  + SynchroGlobal.getEndMarkets().get(new Integer(endMarketId+"")));    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    	       // List<FundingInvestment> fundingInvesment = synchroProjectManager.getProjectInvestments(projectID, pibEM.getEndMarketID());
    	        if(fundingInvestmentMap!=null && fundingInvestmentMap.get(endMarketId)!=null && fundingInvestmentMap.get(endMarketId).getSpiContact()!=null)
    		    {
        	        try
        	        {
        		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(endMarketId).getSpiContact()).getName());
        		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(endMarketId).getSpiContact()).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	pibCurrentStatus.setPersonResponsible("");
        	        	pibCurrentStatus.setPersonRespEmail("");
        	        }
    		    }
    	      //https://svn.sourcen.com/issues/19921
    	        else
    		    {
    		    	try
        	        {
    		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        	        	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	pibCurrentStatus.setPersonResponsible("");
        	        	pibCurrentStatus.setPersonRespEmail("");
        	        }
    		    	
    		    }
    		    pibCurrentStatus.setNextStep("Advise Legal Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(endMarketId+"")));
    		    pibCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+endMarketId+"&legalApprover=true");
    		    pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		    

    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Cost Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(endMarketId+"")));
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	if(fundingInvestmentMap!=null && fundingInvestmentMap.get(endMarketId)!=null && fundingInvestmentMap.get(endMarketId).getSpiContact()!=null)
    		    	{
	    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(endMarketId).getSpiContact()).getName());
	    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(endMarketId).getSpiContact()).getEmail());
    		    	}
    		    	else
    		    	{
    		    		pibCurrentStatus.setPersonResponsible("");
	    	        	pibCurrentStatus.setPersonRespEmail("");
    		    	}
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("Provide Cost Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(endMarketId+"")));
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		
    		
        	}
    	}
    	
    	
    	
    	//Methodology Waiver Approval
    //	PIBMethodologyWaiver pibMW = pibManager.getPIBMethodologyWaiver(projectID, emIds.get(0));
    	if(pibMW!=null)
    	{
    		if(pibMW.getIsApproved()!=null && pibMW.getIsApproved()==1)
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Methodology Waiver Approval");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        	
    	        }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    pibCurrentStatus.setMandatory(false);
    		    if(pibMW.getModifiedDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(new Date(pibMW.getModifiedDate()));
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Methodology Waiver Approval");    	

    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    pibCurrentStatus.setStatus("Pending");
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    	}
    	
    	
    	
    	//Brief Approval and Agency intimation
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Brief Approval and Agency intimation");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    	        String nextStepSubject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                pibCurrentStatus.setNextStepSubject(nextStepSubject);
                pibCurrentStatus.setNextStepMessage(nextStepMessage);
                
                pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients(SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME, projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID));
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("");
    		    pibCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getPibCompletionDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(initiationList.get(0).getPibCompletionDate());
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Brief Approval and Agency intimation");    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    	        //https://svn.sourcen.com/issues/19850
    	        Boolean allEndMarketSaved = pibManager.allPIBMarketSaved(projectID,endMarketInitiationList.size()+1);
    	        
    	      
    	        if(allEndMarketSaved)
    	        {
	    	        
	    	        String nextStepSubject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                
	                pibCurrentStatus.setNextStepSubject(nextStepSubject);
	                pibCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	                pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients(SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME, projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID));
	                
    	        }
    	        
    	        if(allEndMarketSaved && (initiationList.get(0).getLegalApprovalRcvd() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals("")
        				|| initiationList.get(0).getLegalApprovalNotReq()))
    	        {
    	        	pibCurrentStatus.setNextStepEnable(true);
    	        }
    	        else
    	        {
    	        	pibCurrentStatus.setNextStepEnable(false);
    	        }
    	        
    	    
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("Send Brief to Agency");
    		    pibCurrentStatus.setMandatory(true);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		
    	}
    	
    	// This else block is for https://svn.sourcen.com/issues/19940. In case the Project is in PIT_OPEN state
    	else
    	{
    		pibCurrentStatus=new ProjectCurrentStatus();
			pibCurrentStatus.setActivityDesc("Brief Approval and Agency intimation");    		      
		    pibCurrentStatus.setStatus("Pending");

		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	        pibCurrentStatus.setPersonRespSubject(subject);
	        pibCurrentStatus.setPersonRespMessage(messageBody);
	        
	        //https://svn.sourcen.com/issues/19850
	        Boolean allEndMarketSaved = pibManager.allPIBMarketSaved(projectID,endMarketInitiationList.size()+1);
	       
	        if(allEndMarketSaved)
	        {
    	        
    	        String nextStepSubject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                pibCurrentStatus.setNextStepSubject(nextStepSubject);
                pibCurrentStatus.setNextStepMessage(nextStepMessage);
                
                pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients(SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME, projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID));
                
	        }
	        	pibCurrentStatus.setNextStepEnable(false);
	        	
		    try
	        {
		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        }
	        catch(UserNotFoundException ue)
	        {
	        	pibCurrentStatus.setPersonResponsible("");
	        	pibCurrentStatus.setPersonRespEmail("");
	        }
		    pibCurrentStatus.setNextStep("Send Brief to Agency");
		    pibCurrentStatus.setMandatory(true);
		    pibCurrentStatusList.add(pibCurrentStatus);
    	}
    	//Notify Above Market on the brief changes
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ProjectInitiation pibEM: endMarketInitiationList )
    		{
	    		if(pibEM.getNotifyAboveMarketContacts())
	    		{
	    			pibCurrentStatus=new ProjectCurrentStatus();
	    			pibCurrentStatus.setActivityDesc("Notify Above Market on the brief changes");
	    		    pibCurrentStatus.setStatus("Complete");
	
	    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	    	        pibCurrentStatus.setPersonRespSubject(subject);
	    	        pibCurrentStatus.setPersonRespMessage(messageBody);
	    	        
	    	        String nextStepSubject = TemplateUtil.getTemplate("pib.notifyAboveMarketContacts.subject", JiveGlobals.getLocale());
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.notifyAboveMarketContacts.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                
	                pibCurrentStatus.setNextStepSubject(nextStepSubject);
	                pibCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	                
	                pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients((SynchroConstants.PROJECT_OWNER_ROLE +","+ SynchroConstants.SYNCHRO_GLOBAL_PROJECT_CONTACT_GROUP_NAME), projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID));
	                
	    		    try
	    	        {
	    		    	if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
	    		    	{
	    		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
	    		    		pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	    		    	}
	    		    	else
	    		    	{
	    		    		pibCurrentStatus.setPersonResponsible("");
		    	        	pibCurrentStatus.setPersonRespEmail("");
	    		    	}
	    		    	
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	pibCurrentStatus.setPersonResponsible("");
	    	        	pibCurrentStatus.setPersonRespEmail("");
	    	        }
	    		    pibCurrentStatus.setNextStep("");
	    		    pibCurrentStatus.setMandatory(false);
	    		    if(pibEM.getPibNotifyAMContactsDate()!=null)
	    		    {
	    		    	pibCurrentStatus.setCompletionDate(pibEM.getPibNotifyAMContactsDate());
	    		    }
	    		    pibCurrentStatusList.add(pibCurrentStatus);
	    		}
	    		//https://svn.sourcen.com/issues/19921
	    		/*else
	    		{
	    			pibCurrentStatus=new ProjectCurrentStatus();
	    			pibCurrentStatus.setActivityDesc("Notify Above Market on the brief changes");
	    		    pibCurrentStatus.setStatus("Pending");
	
	    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
	    	        pibCurrentStatus.setPersonRespSubject(subject);
	    	        pibCurrentStatus.setPersonRespMessage(messageBody);
	    	        
	    	        String nextStepSubject = TemplateUtil.getTemplate("pib.notifyAboveMarketContacts.subject", JiveGlobals.getLocale());
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("pib.notifyAboveMarketContacts.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                
	                pibCurrentStatus.setNextStepSubject(nextStepSubject);
	                pibCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	                
	                pibCurrentStatus.setNextStepPersonEmail(stageManager.getNotificationRecipients((SynchroConstants.PROJECT_OWNER_ROLE +","+ SynchroConstants.SYNCHRO_GLOBAL_PROJECT_CONTACT_GROUP_NAME), projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID));
	              
	    		    try
	    	        {
	    		    	if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pibEM.getEndMarketID())!=null && fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()!=null)
	    		    	{
	    		    		pibCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getName());
	    		    		pibCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pibEM.getEndMarketID()).getSpiContact()).getEmail());
	    		    	}
	    		    	else
	    		    	{
	    		    		pibCurrentStatus.setPersonResponsible("");
		    	        	pibCurrentStatus.setPersonRespEmail("");
	    		    	}
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	pibCurrentStatus.setPersonResponsible("");
	    	        	pibCurrentStatus.setPersonRespEmail("");
	    	        }
	    		    pibCurrentStatus.setNextStep("Notify Above Market SP&I");
	    		//    pibCurrentStatus.setNextStepLink(stageUrl);
	    		    pibCurrentStatus.setMandatory(false);
	    		    pibCurrentStatusList.add(pibCurrentStatus);
	    		}*/
    		}
    	}
    	//Agency Waiver Approval
    	//PIBMethodologyWaiver pibKantarMW = pibManager.getPIBKantarMethodologyWaiver(projectID, emIds.get(0));
    	if(pibKantarMW!=null)
    	{
    		if(pibKantarMW.getIsApproved()!=null && pibKantarMW.getIsApproved()==1)
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Agency Waiver Approval");    		      
    		    pibCurrentStatus.setStatus("Complete");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibKantarMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibKantarMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    pibCurrentStatus.setMandatory(false);
    		    if(pibKantarMW.getModifiedDate()!=null)
    		    {
    		    	pibCurrentStatus.setCompletionDate(new Date(pibKantarMW.getModifiedDate()));
    		    }
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    		else
    		{
    			pibCurrentStatus=new ProjectCurrentStatus();
    			pibCurrentStatus.setActivityDesc("Agency Waiver Approval");    		      
    		    pibCurrentStatus.setStatus("Pending");

    		    messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    		    messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(pibCurrentStatus.getActivityDesc()));
    	        pibCurrentStatus.setPersonRespSubject(subject);
    	        pibCurrentStatus.setPersonRespMessage(messageBody);
    	        
    		    try
    	        {
    		    	pibCurrentStatus.setPersonResponsible(userManager.getUser(pibKantarMW.getMethodologyApprover()).getName());
    		    	pibCurrentStatus.setPersonRespEmail(userManager.getUser(pibKantarMW.getMethodologyApprover()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	pibCurrentStatus.setPersonResponsible("");
    	        	pibCurrentStatus.setPersonRespEmail("");
    	        }
    		    pibCurrentStatus.setNextStep("View/Approve Waiver");
    		    pibCurrentStatus.setMandatory(false);
    		    pibCurrentStatusList.add(pibCurrentStatus);
    		}
    	}
    	return pibCurrentStatusList;
    }
    
    @Override
    public Map<Long,List<ProjectStagePendingFields>> getPIBMultiPendingFields(Long projectID, List<Long> emIds)
    {
    	List<ProjectInitiation> initiationList = pibManager.getPIBDetails(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	List<ProjectInitiation> endMarketInitiationList = new ArrayList<ProjectInitiation>();
    
    	for(Long endMarketId : emIds)
    	{
    		List<ProjectInitiation> pibEMList = pibManager.getPIBDetails(projectID,endMarketId);
    		if(pibEMList!=null && pibEMList.size()>0)
    		{
    			endMarketInitiationList.add(pibEMList.get(0));
    		}
    	}
    	
    	Map<Long,List<ProjectStagePendingFields>> pibPendingFieldsListMap = new HashMap<Long, List<ProjectStagePendingFields>>();
    	
    	// This is for Above Market
    	List<ProjectStagePendingFields> pibPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	Map<Integer, List<AttachmentBean>> attachmentMap = pibManager.getDocumentAttachment(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	
    	PIBStakeholderList amPibStakeholderList = pibManager.getPIBStakeholderList(projectID);
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		ProjectStagePendingFields pibPendingFields = new ProjectStagePendingFields();
    		if(initiationList.get(0).getLatestEstimate()!=null)
    		{
    			pibPendingFields.setFieldName("Latest Estimate");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields.setFieldName("Latest Estimate");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getBizQuestion()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Business Question");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Business Question");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getResearchObjective()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Research Objectives(s)");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Research Objectives(s)");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getActionStandard()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Action Standard(s)");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Action Standard(s)");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getResearchDesign()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Methodology Approach and Research Design");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Methodology Approach and Research Design");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getSampleProfile()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Sample Profile(Research)");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Sample Profile(Research)");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getStimulusMaterial()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Stimulus Material");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Stimulus Material");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(StringUtils.isEmpty(initiationList.get(0).getOthers()))
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Comments");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Comments");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(initiationList.get(0).getStimuliDate()!=null)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Date Stimuli Available (in Research Agency)");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Date Stimuli Available (in Research Agency)");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		PIBReporting pibReporting = pibManager.getPIBReporting(projectID);
    		
    		if(pibReporting!=null && pibReporting.getFullreport() && pibReporting.getTopLinePresentation() && pibReporting.getPresentation() && pibReporting.getGlobalSummary())
       		{
    			pibPendingFields = new ProjectStagePendingFields();
        		pibPendingFields.setFieldName("Reporting Requirement");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
        		pibPendingFields.setFieldName("Reporting Requirement");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		
    		if(pibReporting!=null && !StringUtils.isEmpty(pibReporting.getOtherReportingRequirements()))
       		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Reporting Requirements");
    			pibPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Other Reporting Requirements");
    			pibPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()).size()>0)
    			{
    				pibPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				pibPendingFields.setAttachmentDone("No");
    			}
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		
    		
    		if(amPibStakeholderList!=null && amPibStakeholderList.getAgencyContact1()!=null && amPibStakeholderList.getAgencyContact1()>0)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Agency Contact");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Agency Contact");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
			
    		if(amPibStakeholderList!=null && amPibStakeholderList.getGlobalLegalContact()!=null && amPibStakeholderList.getGlobalLegalContact()>0)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Legal Contact");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Legal Contact");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		if(amPibStakeholderList!=null && amPibStakeholderList.getProductContact()!=null && amPibStakeholderList.getProductContact()>0)
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Product Contact");
    			pibPendingFields.setInformationProvided(true);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
    		else
    		{
    			pibPendingFields = new ProjectStagePendingFields();
    			pibPendingFields.setFieldName("Product Contact");
    			pibPendingFields.setInformationProvided(false);
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    		}
			
    	}
    	
    	pibPendingFieldsListMap.put(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, pibPendingFieldsList);
    	
    	// PIB - Pending Field List for End Markets
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ProjectInitiation pibEM: endMarketInitiationList )
    		{
    			pibPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	        attachmentMap = pibManager.getDocumentAttachment(projectID, pibEM.getEndMarketID());
    	    	
	    		ProjectStagePendingFields pibPendingFields = new ProjectStagePendingFields();
	    		pibPendingFields.setFieldName("Latest Estimate");
    			pibPendingFields.setInformationProvided(false);
    			//https://svn.sourcen.com/issues/19854
    			pibPendingFields.setDisplayInformation("N/A");
    			pibPendingFields.setAttachmentDone("N/A");
    			pibPendingFieldsList.add(pibPendingFields);
    			
	    		if(StringUtils.isEmpty(pibEM.getBizQuestion()))
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Business Question");
	    			pibPendingFields.setInformationProvided(false);
	    			//https://svn.sourcen.com/issues/19855
	    			pibPendingFields.setAttachmentDone("N/A");
	    			/*if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}*/
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Business Question");
	    			pibPendingFields.setInformationProvided(true);
	    			//https://svn.sourcen.com/issues/19855
	    			pibPendingFields.setAttachmentDone("N/A");
	    			/*
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}*/
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		if(StringUtils.isEmpty(pibEM.getResearchObjective()))
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Research Objectives(s)");
	    			pibPendingFields.setInformationProvided(false);
	    			//https://svn.sourcen.com/issues/19855
	    			pibPendingFields.setAttachmentDone("N/A");
	    			/*
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}*/
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Research Objectives(s)");
	    			pibPendingFields.setInformationProvided(true);
	    			//https://svn.sourcen.com/issues/19855
	    			pibPendingFields.setAttachmentDone("N/A");
	    			/*
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}*/
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		if(StringUtils.isEmpty(pibEM.getActionStandard()))
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Action Standard(s)");
	    			pibPendingFields.setInformationProvided(false);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Action Standard(s)");
	    			pibPendingFields.setInformationProvided(true);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		if(StringUtils.isEmpty(pibEM.getResearchDesign()))
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Methodology Approach and Research Design");
	    			pibPendingFields.setInformationProvided(false);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Methodology Approach and Research Design");
	    			pibPendingFields.setInformationProvided(true);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		
	    		if(StringUtils.isEmpty(pibEM.getSampleProfile()))
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Sample Profile(Research)");
	    			pibPendingFields.setInformationProvided(false);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Sample Profile(Research)");
	    			pibPendingFields.setInformationProvided(true);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		if(StringUtils.isEmpty(pibEM.getStimulusMaterial()))
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Stimulus Material");
	    			pibPendingFields.setInformationProvided(false);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Stimulus Material");
	    			pibPendingFields.setInformationProvided(true);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		if(StringUtils.isEmpty(pibEM.getOthers()))
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Other Comments");
	    			pibPendingFields.setInformationProvided(false);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Other Comments");
	    			pibPendingFields.setInformationProvided(true);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		if(pibEM.getStimuliDate()!=null)
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Date Stimuli Available (in Research Agency)");
	    			pibPendingFields.setInformationProvided(true);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Date Stimuli Available (in Research Agency)");
	    			pibPendingFields.setInformationProvided(false);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		PIBReporting pibReporting = pibManager.getPIBReporting(projectID, pibEM.getEndMarketID());
	    		
	    		if(pibReporting!=null && pibReporting.getFullreport() && pibReporting.getTopLinePresentation() && pibReporting.getPresentation() && pibReporting.getGlobalSummary())
	       		{
	    			pibPendingFields = new ProjectStagePendingFields();
	        		pibPendingFields.setFieldName("Reporting Requirement");
	    			pibPendingFields.setInformationProvided(true);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	        		pibPendingFields.setFieldName("Reporting Requirement");
	    			pibPendingFields.setInformationProvided(false);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		
	    		if(pibReporting!=null && !StringUtils.isEmpty(pibReporting.getOtherReportingRequirements()))
	       		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Other Reporting Requirements");
	    			pibPendingFields.setInformationProvided(true);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Other Reporting Requirements");
	    			pibPendingFields.setInformationProvided(false);
	    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()).size()>0)
	    			{
	    				pibPendingFields.setAttachmentDone("Yes");
	    			}
	    			else
	    			{
	    				pibPendingFields.setAttachmentDone("No");
	    			}
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		
	    		PIBStakeholderList pibStakeholderList = pibManager.getPIBStakeholderList(projectID, pibEM.getEndMarketID());
	    		
	    		
	    		//https://svn.sourcen.com/issues/19852
	    		if(amPibStakeholderList!=null && amPibStakeholderList.getAgencyContact1()!=null && amPibStakeholderList.getAgencyContact1()>0)
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Agency Contact");
	    			pibPendingFields.setInformationProvided(true);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Agency Contact");
	    			pibPendingFields.setInformationProvided(false);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
				
	    		if(pibStakeholderList!=null && pibStakeholderList.getGlobalLegalContact()!=null && pibStakeholderList.getGlobalLegalContact()>0)
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Legal Contact");
	    			pibPendingFields.setInformationProvided(true);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Legal Contact");
	    			pibPendingFields.setInformationProvided(false);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		if(pibStakeholderList!=null && pibStakeholderList.getProductContact()!=null && pibStakeholderList.getProductContact()>0)
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Product Contact");
	    			pibPendingFields.setInformationProvided(true);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
	    		else
	    		{
	    			pibPendingFields = new ProjectStagePendingFields();
	    			pibPendingFields.setFieldName("Product Contact");
	    			pibPendingFields.setInformationProvided(false);
	    			pibPendingFields.setAttachmentDone("N/A");
	    			pibPendingFieldsList.add(pibPendingFields);
	    		}
			
    	    	pibPendingFieldsListMap.put(pibEM.getEndMarketID(), pibPendingFieldsList);
    		}
    	}
    	return pibPendingFieldsListMap;
    }
    
    @Override
    
    public List<ProjectCurrentStatus> getProposalMultiStatusList(List<ProposalInitiation> initiationList, final long projectID, String stageUrl, List<FundingInvestment> fundingInvestmentList)
    {
    	//List<ProposalInitiation> initiationList = proposalManager.getProposalDetails(projectID);
    	Project project = synchroProjectManager.get(projectID);
    	List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	List<EndMarketInvestmentDetail> emDetailList = synchroProjectManager.getEndMarketDetails(projectID);
    	
    	//String stageUrl = baseUrl+"/synchro/proposal-details!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	Long aboveMarketProjectContact = null;
    	for(FundingInvestment fi: fundingInvestmentList)
    	{
    		if(fi.getAboveMarket())
    		{
    			aboveMarketProjectContact = fi.getProjectContact();
    		}
    	}
    	
    	List<ProjectCurrentStatus> proposalCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus proposalCurrentStatus = null;
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SAVED.ordinal())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Project Proposal");    		   
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		        
    			proposalCurrentStatus.setStatus("Complete");
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(true);
    		    
    		    if(initiationList.get(0).getProposalSaveDate()!=null)
    		    {
    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getProposalSaveDate());
    		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Project Proposal");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("Complete Proposal");
    		    proposalCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
    		    proposalCurrentStatus.setMandatory(true);
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SUBMITTED.ordinal())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Submission");    		      
    			proposalCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("agency.submit.proposal.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("agency.submit.proposal.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getProposalSubmitDate()!=null)
      		    {
      		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getProposalSubmitDate());
      		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Submission");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("agency.submit.proposal.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("agency.submit.proposal.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("Send Proposal to BAT");
    		    // Send Proposal To BAT is enabled only when the Proposall is SAVED and all mandatory fields are filled.
    		    if(initiationList.get(0).getStatus()==StageStatus.PROPOSAL_SAVED.ordinal())
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(false);
    		    }
    		    proposalCurrentStatus.setMandatory(true);
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getReqClarificationReqClicked())
    		{
    			https://svn.sourcen.com/issues/19944
	    		if(!initiationList.get(0).getIsReqClariModification() || initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SUBMITTED.ordinal())
    			{
	    			proposalCurrentStatus=new ProjectCurrentStatus();
	    			proposalCurrentStatus.setActivityDesc("Proposal Revision and Update to BAT");    		      
	    			proposalCurrentStatus.setStatus("Complete");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
	    			proposalCurrentStatus.setPersonRespSubject(subject);
	    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
	    		    
	    		    
	    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.spi.subject", JiveGlobals.getLocale());
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.spi.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                
	                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
	                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    		    
	    		    try
	    	        {
	    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
	    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
	    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	proposalCurrentStatus.setPersonResponsible("");
	    	        	proposalCurrentStatus.setPersonRespEmail("");
	    	        	proposalCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    proposalCurrentStatus.setNextStep("");
	    		    proposalCurrentStatus.setMandatory(true);
	    		    if(initiationList.get(0).getReqClarificationReqDate()!=null)
	    		    {
	    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getReqClarificationReqDate());
	    		    }
	    		    proposalCurrentStatusList.add(proposalCurrentStatus);
	    		}
	    		else
	    		{
	    			proposalCurrentStatus=new ProjectCurrentStatus();
	    			proposalCurrentStatus.setActivityDesc("Proposal Revision and Update to BAT");    		      
	    			proposalCurrentStatus.setStatus("Pending");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
	    			proposalCurrentStatus.setPersonRespSubject(subject);
	    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
	    		    
	    		    
	    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.spi.subject", JiveGlobals.getLocale());
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.spi.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                
	                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
	                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    		    
	    		    try
	    	        {
	    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(initiationList.get(0).getAgencyID()).getName());
	    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(initiationList.get(0).getAgencyID()).getEmail());
	    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	proposalCurrentStatus.setPersonResponsible("");
	    	        	proposalCurrentStatus.setPersonRespEmail("");
	    	        	proposalCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    proposalCurrentStatus.setNextStep("Notify BAT about Proposal Revision");
	    		    proposalCurrentStatus.setMandatory(true);
	    		    proposalCurrentStatusList.add(proposalCurrentStatus);
	    		}
    		}
	    		
    	}
    	
    	//https://svn.sourcen.com/issues/19944
    	/*
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROPOSAL_SUBMITTED.ordinal() && initiationList.get(0).getIsSendToProjectOwner())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Review");    		      
    			proposalCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.projectowner.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(false);
    		    if(initiationList.get(0).getPropSendToOwnerDate()!=null)
      		    {
      		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getPropSendToOwnerDate());
      		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Review");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("proposal.send.to.projectowner.subject", JiveGlobals.getLocale());
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("proposal.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                proposalCurrentStatus.setNextStepSubject(nextStepSubject);
                proposalCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    try
    	        {
    		    	
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(aboveMarketProjectContact).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(aboveMarketProjectContact).getEmail());
    		    	proposalCurrentStatus.setNextStepPersonEmail(userManager.getUser(aboveMarketProjectContact).getEmail());
    		    	
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    catch(Exception e)
    		    {
    		    	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        	proposalCurrentStatus.setNextStepPersonEmail("");
    		    }
    		    
    		 // Send Proposal to Project Owner is enabled only when the Proposal is SUBMITTED.
    		    if(initiationList.get(0).getIsPropSubmitted())
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(false);
    		    }
    		    proposalCurrentStatus.setNextStep("Send Proposal to Project Owner");
    		    proposalCurrentStatus.setMandatory(false);
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	*/
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()==StageStatus.PROPOASL_AWARDED.ordinal())
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Award");    		      
    			proposalCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("");
    		    proposalCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getProposalAwardDate()!=null)
    		    {
    		    	proposalCurrentStatus.setCompletionDate(initiationList.get(0).getProposalAwardDate());
    		    }
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    		else
    		{
    			proposalCurrentStatus=new ProjectCurrentStatus();
    			proposalCurrentStatus.setActivityDesc("Proposal Award");    		      
    			proposalCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(proposalCurrentStatus.getActivityDesc()));
    			proposalCurrentStatus.setPersonRespSubject(subject);
    		    proposalCurrentStatus.setPersonRespMessage(messageBody);
    		    
    		    try
    	        {
    		    	proposalCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	proposalCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	proposalCurrentStatus.setPersonResponsible("");
    	        	proposalCurrentStatus.setPersonRespEmail("");
    	        }
    		    proposalCurrentStatus.setNextStep("Award Proposal");
    		    
    		 // Award Proposal is enabled only when the Proposal is SUBMITTED.
    		    if(initiationList.get(0).getIsPropSubmitted())
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	proposalCurrentStatus.setNextStepEnable(false);
    		    }
    		    proposalCurrentStatus.setMandatory(true);
    		    proposalCurrentStatusList.add(proposalCurrentStatus);
    		}
    	}
    	return proposalCurrentStatusList;
    	
    }
    
    @Override
    public List<ProjectStagePendingFields> getProposalMultiPendingFields(List<ProposalInitiation> initiationList, Long projectID, List<Long> emIds)
    {
    	List<ProjectStagePendingFields> proposalPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		Map<Integer, List<AttachmentBean>> attachmentMap = proposalManager.getDocumentAttachment(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,initiationList.get(0).getAgencyID());
    		ProjectStagePendingFields proposalPendingFields = new ProjectStagePendingFields();
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getStimulusMaterialShipped()))
    		{
    			
    			proposalPendingFields.setFieldName("Stimulus Material to be shipped to");
    			proposalPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		else
    		{
    			
    			proposalPendingFields.setFieldName("Stimulus Material to be shipped to");
    			proposalPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL_SHIPPED.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getProposalCostTemplate()))
    		{
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Proposal and Cost Template");
    			proposalPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		else
    		{
    			proposalPendingFields = new ProjectStagePendingFields();
    			proposalPendingFields.setFieldName("Proposal and Cost Template");
    			proposalPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()).size()>0)
    			{
    				proposalPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				proposalPendingFields.setAttachmentDone("No");
    			}
    			proposalPendingFieldsList.add(proposalPendingFields);
    		}
    		
    		for(Long endMarketId : emIds)
    		{
	    		ProposalEndMarketDetails proposalEMDetails = proposalManager.getProposalEMDetails(projectID, endMarketId, initiationList.get(0).getAgencyID());
	    		if(proposalEMDetails!=null)
	    		{
	    			if(proposalEMDetails.getTotalCost()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Total Cost");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			proposalPendingFields.setEndMarketId(endMarketId);
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Total Cost");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			proposalPendingFields.setEndMarketId(endMarketId);
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getIntMgmtCost()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("International Management Cost - Research Hub Cost");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("International Management Cost - Research Hub Cost");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getLocalMgmtCost()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Local Management Cost");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Local Management Cost");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getFieldworkCost()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Fieldwork Cost");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Fieldwork Cost");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getOperationalHubCost()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Operational Hub Cost");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Operational Hub Cost");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getOtherCost()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Other Cost");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Other Cost");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(StringUtils.isEmpty(proposalEMDetails.getProposedFWAgencyNames()))
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Name of Proposed Fieldwork Agencies");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Name of Proposed Fieldwork Agencies");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getFwStartDate()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Estimated Fieldwork Start");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Estimated Fieldwork Start");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getFwEndDate()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Estimated Fieldwork Completion");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Estimated Fieldwork Completion");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getDataCollectionMethod()!=null && proposalEMDetails.getDataCollectionMethod().size()>0)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Data Collection Methods");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Data Collection Methods");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getTotalNoInterviews()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Total Number of Interviews");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Total Number of Interviews");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getTotalNoOfVisits()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Total Number of Visits per Respondent");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Total Number of Visits per Respondent");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    			if(proposalEMDetails.getAvIntDuration()!=null)
	        		{
	    				proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Average Interview Duration (in minutes)");
	        			proposalPendingFields.setInformationProvided(true);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	        		else
	        		{
	        			proposalPendingFields = new ProjectStagePendingFields();
	        			proposalPendingFields.setFieldName("Average Interview Duration (in minutes)");
	        			proposalPendingFields.setInformationProvided(false);
	        			proposalPendingFields.setAttachmentDone("N/A");
	        			
	        			proposalPendingFieldsList.add(proposalPendingFields);
	        		}
	    		}
	    		//https://svn.sourcen.com/issues/19812
	    		else
	    		{
    				proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			proposalPendingFields.setEndMarketId(endMarketId);
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("International Management Cost - Research Hub Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Local Management Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Fieldwork Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Operational Hub Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Other Cost");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Name of Proposed Fieldwork Agencies");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Estimated Fieldwork Start");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Estimated Fieldwork Completion");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Data Collection Methods");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Number of Interviews");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Total Number of Visits per Respondent");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
        			proposalPendingFields = new ProjectStagePendingFields();
        			proposalPendingFields.setFieldName("Average Interview Duration (in minutes)");
        			proposalPendingFields.setInformationProvided(false);
        			proposalPendingFields.setAttachmentDone("N/A");
        			
        			proposalPendingFieldsList.add(proposalPendingFields);
        		
	    		}

    		}
       	}
    	return proposalPendingFieldsList;
    }
    
    
    @Override
    public List<ProjectCurrentStatus> getProjectSpecsMultiStatusList(final long projectID, String stageUrl,List<FundingInvestment> fundingInvestmentList)
	{
    	List<ProjectSpecsInitiation> initiationList = projectSpecsManager.getProjectSpecsInitiation(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	Project project = synchroProjectManager.get(projectID);
    	List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	List<EndMarketInvestmentDetail> emDetailList = synchroProjectManager.getEndMarketDetails(projectID);
    	
    	Map<Long, FundingInvestment> fundingInvestmentMap = new HashMap<Long, FundingInvestment>();
    	if(fundingInvestmentList!=null && fundingInvestmentList.size()>0)
    	{
    		for(FundingInvestment fi : fundingInvestmentList)
    		{
    			if(fi.getAboveMarket())
    			{
    				fundingInvestmentMap.put(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, fi);
    			}
    			else
    			{
    				fundingInvestmentMap.put(fi.getFieldworkMarketID(), fi);
    			}
    		}
    	}
    	
    	List<ProjectSpecsInitiation> endMarketInitiationList = new ArrayList<ProjectSpecsInitiation>();
	
    	for(Long endMarketId : emIds)
    	{
    		List<ProjectSpecsInitiation> psEMList = projectSpecsManager.getProjectSpecsInitiation(projectID,endMarketId);
    		if(psEMList!=null && psEMList.size()>0)
    		{
    			endMarketInitiationList.add(psEMList.get(0));
    		}
    	}
	
	
    	
    	Long awardedExternalAgency = synchroUtils.getAwardedExternalAgencyUserID(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID );
    	
    	List<ProjectCurrentStatus> projectSpecsCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus projectSpecsCurrentStatus = null;
    	
    	//String stageUrl = baseUrl+"/synchro/project-specs!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		//https://svn.sourcen.com/issues/19820
    		if(initiationList.get(0).getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() && initiationList.get(0).getIsSendForApproval()==1)
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs - Above Market");    		      
    			projectSpecsCurrentStatus.setStatus("Complete");
    			projectSpecsCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    		        
    		    
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("");
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    
    		    if(initiationList.get(0).getProjectSpecsSaveDate()!=null)
    		    {
    		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getProjectSpecsSaveDate());
    		    }
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    		else
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs - Above Market");    		      
    			projectSpecsCurrentStatus.setStatus("Pending");
    			projectSpecsCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("Complete Project Specs - Above Market");
    		    projectSpecsCurrentStatus.setNextStepLink(stageUrl+"&validationError=true");
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() 
    				&& initiationList.get(0).getLegalApprovalCCCA() && initiationList.get(0).getLegalApproverCCCA()!=null && !initiationList.get(0).getLegalApproverCCCA().equals("")
    				&& initiationList.get(0).getLegalApprovalDG() && initiationList.get(0).getLegalApproverDG()!=null && !initiationList.get(0).getLegalApproverDG().equals("")
    				&& initiationList.get(0).getLegalApprovalQuestionnaire() && initiationList.get(0).getLegalApproverQuestionnaire()!=null && !initiationList.get(0).getLegalApproverQuestionnaire().equals("")
    				&& initiationList.get(0).getLegalApprovalScreener() && initiationList.get(0).getLegalApproverScreener()!=null && !initiationList.get(0).getLegalApproverScreener().equals("")
    				&& initiationList.get(0).getLegalApprovalStimulus() && initiationList.get(0).getLegalApproverStimulus()!=null && !initiationList.get(0).getLegalApproverStimulus().equals("")
    				)
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Legal Approval - Above Market");    		      
    			projectSpecsCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	//projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(emDetailList.get(0).getSpiContact()).getName());
    		    	//projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(emDetailList.get(0).getSpiContact()).getEmail());
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("");
    		    //projectSpecsCurrentStatus.setNextStepLink(stageUrl);
    		    projectSpecsCurrentStatus.setMandatory(true);

   		     	if(initiationList.get(0).getPsLegalApprovalDate()!=null)
       		    {
       		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getPsLegalApprovalDate());
       		    }
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    		else
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Legal Approval - Above Market");    		      
    			projectSpecsCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectSpecsCurrentStatus.setNextStep("Advise Legal Approval - Above Market");
    		    projectSpecsCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() && initiationList.get(0).getIsReqClariModification())
    		{
    			// This check is for https://svn.sourcen.com/issues/19822
    			if(initiationList.get(0).getIsSendForApproval()!=null && initiationList.get(0).getIsSendForApproval()==1)
    			{
	    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
	    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Revision and Update to BAT - Above Market");    		      
	    			projectSpecsCurrentStatus.setStatus("Complete");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		              
		            projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
		            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	    			
	    		    try
	    	        {
	    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	projectSpecsCurrentStatus.setPersonResponsible("");
	    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		   // projectSpecsCurrentStatus.setNextStep("Notify BAT about Project Specs Revision - Above Market");
	    		    projectSpecsCurrentStatus.setNextStep("");
	    		    projectSpecsCurrentStatus.setMandatory(false);
	    		    if(initiationList.get(0).getReqClarificationModDate()!=null)
	    		    {
	    		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getReqClarificationModDate());
	    		    }

	    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
	    		}
    			else
    			{
    				projectSpecsCurrentStatus=new ProjectCurrentStatus();
	    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Revision and Update to BAT - Above Market");    		      
	    			projectSpecsCurrentStatus.setStatus("Pending");
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
		            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
		              
		            projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
		            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	    			
	    		    try
	    	        {
	    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	projectSpecsCurrentStatus.setPersonResponsible("");
	    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    projectSpecsCurrentStatus.setNextStep("Notify BAT about Project Specs Revision - Above Market");
	    		    projectSpecsCurrentStatus.setMandatory(false);
	    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    			}
    		}
    		
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		//if(initiationList.get(0).getStatus()==StageStatus.PROJECT_SPECS_COMPLETED.ordinal())
    		//https://svn.sourcen.com/issues/19971
    		if(initiationList.get(0).getIsApproved()==1)
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Approval - Above Market");    		      
    			projectSpecsCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	              
	            nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            
    		    projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	            
    		    projectSpecsCurrentStatus.setNextStep("");
    		    projectSpecsCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getApprovedDate()!=null)
    		    {
    		    	projectSpecsCurrentStatus.setCompletionDate(initiationList.get(0).getApprovedDate());
    		    }
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    		else
    		{
    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
    			projectSpecsCurrentStatus.setActivityDesc("Project Specs Approval - Above Market");    		      
    			projectSpecsCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(awardedExternalAgency).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectSpecsCurrentStatus.setPersonResponsible("");
    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
    	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
      	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	              
	            nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
	            
    		    projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	            
    		    projectSpecsCurrentStatus.setNextStep("Approve Project Specs - Above Market");
    		    // Approve Project Specs will be enabled only when the Send for Approval button is clicked.
    		    if(initiationList.get(0).getIsSendForApproval()!=null && initiationList.get(0).getIsSendForApproval()==1 && initiationList.get(0).getLegalApprovalCCCA() && initiationList.get(0).getLegalApproverCCCA()!=null && !initiationList.get(0).getLegalApproverCCCA().equals("")
        				&& initiationList.get(0).getLegalApprovalDG() && initiationList.get(0).getLegalApproverDG()!=null && !initiationList.get(0).getLegalApproverDG().equals("")
        				&& initiationList.get(0).getLegalApprovalQuestionnaire() && initiationList.get(0).getLegalApproverQuestionnaire()!=null && !initiationList.get(0).getLegalApproverQuestionnaire().equals("")
        				&& initiationList.get(0).getLegalApprovalScreener() && initiationList.get(0).getLegalApproverScreener()!=null && !initiationList.get(0).getLegalApproverScreener().equals("")
        				&& initiationList.get(0).getLegalApprovalStimulus() && initiationList.get(0).getLegalApproverStimulus()!=null && !initiationList.get(0).getLegalApproverStimulus().equals(""))
    		    {
    		    	projectSpecsCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	projectSpecsCurrentStatus.setNextStepEnable(false);
    		    }
    		    projectSpecsCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    		    projectSpecsCurrentStatus.setMandatory(true);
    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
    		}
    	}
    	
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ProjectSpecsInitiation psEmInitiation: endMarketInitiationList)
    		{
    			//https://svn.sourcen.com/issues/19820
	    		if(psEmInitiation.getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() && psEmInitiation.getIsSendForApproval()==1)
	    		{
	    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
	    			projectSpecsCurrentStatus.setActivityDesc("Project Specs - " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
	    			projectSpecsCurrentStatus.setStatus("Complete");
	    			projectSpecsCurrentStatus.setEndMarketID(psEmInitiation.getEndMarketID());
	    			
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	    		        
	    		    
	    		    try
	    	        {
	    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	projectSpecsCurrentStatus.setPersonResponsible("");
	    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	    	        }
	    		    projectSpecsCurrentStatus.setNextStep("");
	    		    projectSpecsCurrentStatus.setMandatory(true);
	    		    
	    		    if(psEmInitiation.getProjectSpecsSaveDate()!=null)
	    		    {
	    		    	projectSpecsCurrentStatus.setCompletionDate(psEmInitiation.getProjectSpecsSaveDate());
	    		    }
	    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
	    		}
	    		else
	    		{
	    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
	    			projectSpecsCurrentStatus.setActivityDesc("Project Specs - " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));  
	    			projectSpecsCurrentStatus.setStatus("Pending");
	    			projectSpecsCurrentStatus.setEndMarketID(psEmInitiation.getEndMarketID());
	    			
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	    			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	    			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    		    try
	    	        {
	    		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	projectSpecsCurrentStatus.setPersonResponsible("");
	    	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	    	        }
	    		    projectSpecsCurrentStatus.setNextStep("Complete Project Specs - " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
	    		    projectSpecsCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+psEmInitiation.getEndMarketID()+"&validationError=true");
	    		    projectSpecsCurrentStatus.setMandatory(true);
	    		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
	    		}
	    		

    			if(psEmInitiation.getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() 
        				&& psEmInitiation.getLegalApprovalCCCA() && psEmInitiation.getLegalApproverCCCA()!=null && !psEmInitiation.getLegalApproverCCCA().equals("")
        				&& psEmInitiation.getLegalApprovalDG() && psEmInitiation.getLegalApproverDG()!=null && !psEmInitiation.getLegalApproverDG().equals("")
        				&& psEmInitiation.getLegalApprovalQuestionnaire() && psEmInitiation.getLegalApproverQuestionnaire()!=null && !psEmInitiation.getLegalApproverQuestionnaire().equals("")
        				&& psEmInitiation.getLegalApprovalScreener() && psEmInitiation.getLegalApproverScreener()!=null && !psEmInitiation.getLegalApproverScreener().equals("")
        				&& psEmInitiation.getLegalApprovalStimulus() && psEmInitiation.getLegalApproverStimulus()!=null && !psEmInitiation.getLegalApproverStimulus().equals("")
        				)
	    		{
        			projectSpecsCurrentStatus=new ProjectCurrentStatus();
        			projectSpecsCurrentStatus.setActivityDesc("Legal Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
        			projectSpecsCurrentStatus.setStatus("Complete");
        			
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
        			projectSpecsCurrentStatus.setPersonRespSubject(subject);
        			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
        			
        		  
        		    if(fundingInvestmentMap!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getName());
	        	        	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
        		  //https://svn.sourcen.com/issues/19980
	    	        else
        		    {
        		    	try
	        	        {
        		    		projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        		    		projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
        		   
        		    projectSpecsCurrentStatus.setNextStep("");
        		    projectSpecsCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+psEmInitiation.getEndMarketID());
        		    projectSpecsCurrentStatus.setMandatory(true);

       		        if(psEmInitiation.getPsLegalApprovalDate()!=null)
           		    {
           		    	projectSpecsCurrentStatus.setCompletionDate(psEmInitiation.getPsLegalApprovalDate());
           		    }
        		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
        		}
	    		else
	    		{
	    			projectSpecsCurrentStatus=new ProjectCurrentStatus();
        			projectSpecsCurrentStatus.setActivityDesc("Legal Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
        			projectSpecsCurrentStatus.setStatus("Pending");
        			
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
        			projectSpecsCurrentStatus.setPersonRespSubject(subject);
        			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
        			
        		
        			if(fundingInvestmentMap!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getName());
	        	        	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
        			//https://svn.sourcen.com/issues/19980
	    	        else
        		    {
        		    	try
	        	        {
        		    		projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        		    		projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
        		    projectSpecsCurrentStatus.setNextStep("Advise Legal Approval - " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
        		    projectSpecsCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+psEmInitiation.getEndMarketID()+"&legalApprover=true");
        		    projectSpecsCurrentStatus.setMandatory(true);
        		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
	    		}
    			

    			if(psEmInitiation.getStatus()>=StageStatus.PROJECT_SPECS_SAVED.ordinal() && psEmInitiation.getIsReqClariModification())
        		{
    				// This check is for https://svn.sourcen.com/issues/19822
        			if(psEmInitiation.getIsSendForApproval()!=null && psEmInitiation.getIsSendForApproval()==1)
        			{
	    				projectSpecsCurrentStatus=new ProjectCurrentStatus();
	        			projectSpecsCurrentStatus.setActivityDesc("Project Specs Revision and Update to BAT - "+ SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));   
		    			
	        			projectSpecsCurrentStatus.setStatus("Complete");
	        			
	        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	        			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	        			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	        			
	        			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
	          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
	    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    	              
	    	            projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
	    	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	        			
	        		    try
	        	        {
	        		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	        		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	        		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
	        	        }
	        		  //  projectSpecsCurrentStatus.setNextStep("Notify BAT about Project Specs Revision - "+ SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
	        		    projectSpecsCurrentStatus.setNextStep("");
	        		    projectSpecsCurrentStatus.setMandatory(false);

	       		        if(psEmInitiation.getReqClarificationModDate()!=null)
	       	    		{
	       	    		   	projectSpecsCurrentStatus.setCompletionDate(psEmInitiation.getReqClarificationModDate());
	       	    		}
	        		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
        			}
        			else
        			{
        				projectSpecsCurrentStatus=new ProjectCurrentStatus();
	        			projectSpecsCurrentStatus.setActivityDesc("Project Specs Revision and Update to BAT - "+ SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));   
		    			
	        			projectSpecsCurrentStatus.setStatus("Pending");
	        			
	        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
	        			projectSpecsCurrentStatus.setPersonRespSubject(subject);
	        			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
	        			
	        			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
	          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
	    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    	              
	    	            projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
	    	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
	        			
	        		    try
	        	        {
	        		    	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	        		    	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	        		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
	        	        }
	        		    projectSpecsCurrentStatus.setNextStep("Notify BAT about Project Specs Revision - "+ SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
	        		    projectSpecsCurrentStatus.setMandatory(false);
	        		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
        			}
        		}
    		

    			//if(psEmInitiation.getStatus()==StageStatus.PROJECT_SPECS_COMPLETED.ordinal())
    			//https://svn.sourcen.com/issues/19971
    	    	if(psEmInitiation.getIsApproved()==1)
    			{
        			projectSpecsCurrentStatus=new ProjectCurrentStatus();
        			projectSpecsCurrentStatus.setActivityDesc("Project Specs Approval " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));		      
        			projectSpecsCurrentStatus.setStatus("Complete");
        			
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
        			projectSpecsCurrentStatus.setPersonRespSubject(subject);
        			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
        			
        			if(fundingInvestmentMap!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getName());
	        	        	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
        			//https://svn.sourcen.com/issues/19980
	    	        else
        		    {
        		    	try
	        	        {
        		    		projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        		    		projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
        			
        		    try
        	        {
        		    //	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        		    //	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
        		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(awardedExternalAgency).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        //	projectSpecsCurrentStatus.setPersonResponsible("");
        	        //	projectSpecsCurrentStatus.setPersonRespEmail("");
        	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
        	        }
        		    
        		    String nextStepSubject = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	              
    	            nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
    	            
        		    projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
    	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
    	            
        		    projectSpecsCurrentStatus.setNextStep("");
        		    projectSpecsCurrentStatus.setEndMarketID(psEmInitiation.getEndMarketID());
        		    projectSpecsCurrentStatus.setMandatory(true);
        		    if(psEmInitiation.getApprovedDate()!=null)
        		    {
        		    	projectSpecsCurrentStatus.setCompletionDate(psEmInitiation.getApprovedDate());
        		    }
        		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
        		}
        		else
        		{
        			projectSpecsCurrentStatus=new ProjectCurrentStatus();
        			projectSpecsCurrentStatus.setActivityDesc("Project Specs Approval - "+ SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));	      
        			projectSpecsCurrentStatus.setStatus("Pending");
        			
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectSpecsCurrentStatus.getActivityDesc()));
        			projectSpecsCurrentStatus.setPersonRespSubject(subject);
        			projectSpecsCurrentStatus.setPersonRespMessage(messageBody);
        			
        		    try
        	        {
        		    	//projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        		    	//projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
        		    	projectSpecsCurrentStatus.setNextStepPersonEmail(userManager.getUser(awardedExternalAgency).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	//projectSpecsCurrentStatus.setPersonResponsible("");
        	        	//projectSpecsCurrentStatus.setPersonRespEmail("");
        	        	projectSpecsCurrentStatus.setNextStepPersonEmail("");
        	        }
        		    if(fundingInvestmentMap!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()!=null)
        		    {
	        	        try
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getName());
	        	        	projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(psEmInitiation.getEndMarketID()).getSpiContact()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    }
        		  //https://svn.sourcen.com/issues/19980
	    	        else
        		    {
        		    	try
	        	        {
        		    		projectSpecsCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
        		    		projectSpecsCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	projectSpecsCurrentStatus.setPersonResponsible("");
	        	        	projectSpecsCurrentStatus.setPersonRespEmail("");
	        	        }
        		    	
        		    }
        		    String nextStepSubject = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
          	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	              
    	            nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
    	            nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", projectSpecsCurrentStatus.getPersonResponsible());
    	            
        		    projectSpecsCurrentStatus.setNextStepSubject(nextStepSubject);
    	            projectSpecsCurrentStatus.setNextStepMessage(nextStepMessage);
    	            
        		    projectSpecsCurrentStatus.setNextStep("Approve Project Specs - " + SynchroGlobal.getEndMarkets().get(new Integer(psEmInitiation.getEndMarketID()+"")));
	    			
        		    // Approve Project Specs will be enabled only when the Send for Approval button is clicked.
        		    if(psEmInitiation.getIsSendForApproval()!=null && psEmInitiation.getIsSendForApproval()==1 && psEmInitiation.getLegalApprovalCCCA() && psEmInitiation.getLegalApproverCCCA()!=null && !psEmInitiation.getLegalApproverCCCA().equals("")
            				&& psEmInitiation.getLegalApprovalDG() && psEmInitiation.getLegalApproverDG()!=null && !psEmInitiation.getLegalApproverDG().equals("")
            				&& psEmInitiation.getLegalApprovalQuestionnaire() && psEmInitiation.getLegalApproverQuestionnaire()!=null && !psEmInitiation.getLegalApproverQuestionnaire().equals("")
            				&& psEmInitiation.getLegalApprovalScreener() && psEmInitiation.getLegalApproverScreener()!=null && !psEmInitiation.getLegalApproverScreener().equals("")
            				&& psEmInitiation.getLegalApprovalStimulus() && psEmInitiation.getLegalApproverStimulus()!=null && !psEmInitiation.getLegalApproverStimulus().equals("")
            				)
        		    {
        		    	projectSpecsCurrentStatus.setNextStepEnable(true);
        		    }
        		    else
        		    {
        		    	projectSpecsCurrentStatus.setNextStepEnable(false);
        		    }
        		    projectSpecsCurrentStatus.setEndMarketID(psEmInitiation.getEndMarketID());
        		    projectSpecsCurrentStatus.setMandatory(true);
        		    projectSpecsCurrentStatusList.add(projectSpecsCurrentStatus);
        		}
    		
    		
    		}
    	}
    	
    	
    	
    
    	return projectSpecsCurrentStatusList;
	}
    
    @Override
    public Map<Long,List<ProjectStagePendingFields>> getProjectSpecsMultiPendingFields( Long projectID, List<Long> emIds)
    {
    	
    	List<ProjectSpecsInitiation> initiationList = projectSpecsManager.getProjectSpecsInitiation(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	List<ProjectSpecsInitiation> endMarketInitiationList = new ArrayList<ProjectSpecsInitiation>();
    
    	for(Long endMarketId : emIds)
    	{
    		List<ProjectSpecsInitiation> psEMList = projectSpecsManager.getProjectSpecsInitiation(projectID,endMarketId);
    		if(psEMList!=null && psEMList.size()>0)
    		{
    			endMarketInitiationList.add(psEMList.get(0));
    		}
    	}
    	
    	Map<Long,List<ProjectStagePendingFields>> psPendingFieldsListMap = new HashMap<Long, List<ProjectStagePendingFields>>();
    	List<ProjectStagePendingFields> projectSpecsPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		ProjectStagePendingFields projectSpecsPendingFields = new ProjectStagePendingFields();
    		
    		Map<Integer, List<AttachmentBean>> attachmentMap = projectSpecsManager.getDocumentAttachment(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getStimulusMaterial()))
    		{
    			
    			projectSpecsPendingFields.setFieldName("Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			
    			projectSpecsPendingFields.setFieldName("Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getScreener()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Screener");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Screener");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getConsumerCCAgreement()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Consumer Contract and Confidentiality Agreement");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Consumer Contract and Confidentiality Agreement");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getQuestionnaire()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Questionnaire/Discussion guide");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Questionnaire/Discussion guide");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		
    		if(StringUtils.isEmpty(initiationList.get(0).getDiscussionguide()))
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Actual Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		else
    		{
    			projectSpecsPendingFields = new ProjectStagePendingFields();
    			projectSpecsPendingFields.setFieldName("Actual Stimulus Material");
    			projectSpecsPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()).size()>0)
    			{
    				projectSpecsPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				projectSpecsPendingFields.setAttachmentDone("No");
    			}
    			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
    		}
    		psPendingFieldsListMap.put(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, projectSpecsPendingFieldsList);
    	}
    	
    	// PS - Pending Field List for End Markets
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ProjectSpecsInitiation psEM: endMarketInitiationList )
    		{
    			projectSpecsPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    			ProjectStagePendingFields projectSpecsPendingFields = new ProjectStagePendingFields();
    			Map<Integer, List<AttachmentBean>> attachmentMap = projectSpecsManager.getDocumentAttachment(projectID, psEM.getEndMarketID());
        		if(StringUtils.isEmpty(psEM.getStimulusMaterial()))
        		{
        			
        			projectSpecsPendingFields.setFieldName("Stimulus Material");
        			projectSpecsPendingFields.setInformationProvided(false);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		else
        		{
        			
        			projectSpecsPendingFields.setFieldName("Stimulus Material");
        			projectSpecsPendingFields.setInformationProvided(true);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		
        		if(StringUtils.isEmpty(psEM.getScreener()))
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Screener");
        			projectSpecsPendingFields.setInformationProvided(false);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		else
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Screener");
        			projectSpecsPendingFields.setInformationProvided(true);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		
        		if(StringUtils.isEmpty(psEM.getConsumerCCAgreement()))
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Consumer Contract and Confidentiality Agreement");
        			projectSpecsPendingFields.setInformationProvided(false);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		else
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Consumer Contract and Confidentiality Agreement");
        			projectSpecsPendingFields.setInformationProvided(true);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		
        		if(StringUtils.isEmpty(psEM.getQuestionnaire()))
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Questionnaire/Discussion guide");
        			projectSpecsPendingFields.setInformationProvided(false);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		else
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Questionnaire/Discussion guide");
        			projectSpecsPendingFields.setInformationProvided(true);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		
        		if(StringUtils.isEmpty(psEM.getDiscussionguide()))
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Actual Stimulus Material");
        			projectSpecsPendingFields.setInformationProvided(false);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		else
        		{
        			projectSpecsPendingFields = new ProjectStagePendingFields();
        			projectSpecsPendingFields.setFieldName("Actual Stimulus Material");
        			projectSpecsPendingFields.setInformationProvided(true);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()).size()>0)
        			{
        				projectSpecsPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				projectSpecsPendingFields.setAttachmentDone("No");
        			}
        			projectSpecsPendingFieldsList.add(projectSpecsPendingFields);
        		}
        		psPendingFieldsListMap.put(psEM.getEndMarketID(), projectSpecsPendingFieldsList);
    		}
    	}
    	return psPendingFieldsListMap;
    }
    
    
    @Override
    public List<ProjectCurrentStatus> getReportSummaryMultiStatusList(final long projectID, String stageUrl, List<FundingInvestment> fundingInvestmentList)
	{
    	List<ReportSummaryInitiation> initiationList = reportSummaryManager.getReportSummaryInitiation(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	Project project = synchroProjectManager.get(projectID);
    	List<Long> emIds = synchroProjectManager.getEndMarketIDs(projectID);
    	
    	Long awardedExternalAgency = synchroUtils.getAwardedExternalAgencyUserID(projectID, emIds.get(0));
    	
    	List<ReportSummaryInitiation> endMarketInitiationList = new ArrayList<ReportSummaryInitiation>();
    	
    	for(Long endMarketId : emIds)
    	{
    		List<ReportSummaryInitiation> reportsEMList = reportSummaryManager.getReportSummaryInitiation(projectID, endMarketId);
    		if(reportsEMList!=null && reportsEMList.size()>0)
    		{
    			endMarketInitiationList.add(reportsEMList.get(0));
    		}
    	}
	
    	Map<Long, FundingInvestment> fundingInvestmentMap = new HashMap<Long, FundingInvestment>();
    	if(fundingInvestmentList!=null && fundingInvestmentList.size()>0)
    	{
    		for(FundingInvestment fi : fundingInvestmentList)
    		{
    			if(fi.getAboveMarket())
    			{
    				fundingInvestmentMap.put(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, fi);
    			}
    			else
    			{
    				fundingInvestmentMap.put(fi.getFieldworkMarketID(), fi);
    			}
    		}
    	}
    	List<ProjectCurrentStatus> reportSummaryCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus reportSummaryCurrentStatus = null;
    	
    	
    	
    	//String stageUrl = baseUrl+"/synchro/report-summary!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getSendForApproval())
    		{
    			// This check is because of : https://svn.sourcen.com/issues/19826
    			//https://svn.sourcen.com/issues/19916
    			if((initiationList.get(0).getFullReport() || initiationList.get(0).getSummaryReport() || initiationList.get(0).getSummaryForIRIS()) && !(initiationList.get(0).getNeedRevision()))
    			{
	    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
	    			reportSummaryCurrentStatus.setActivityDesc("Reports - Above Market");    		      
	    			reportSummaryCurrentStatus.setStatus("Complete");
	    			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                 
	                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	    			
	    		    try
	    	        {
	    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	reportSummaryCurrentStatus.setPersonResponsible("");
	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    reportSummaryCurrentStatus.setNextStep("");
	    		    reportSummaryCurrentStatus.setMandatory(true);
	    		    if(initiationList.get(0).getRepSummarySaveDate()!=null)
	    		    {
	    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getRepSummarySaveDate());
	    		    }
	    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			}
    			else
    			{
    				reportSummaryCurrentStatus=new ProjectCurrentStatus();
        			reportSummaryCurrentStatus.setActivityDesc("Reports - Above Market");    		      
        			reportSummaryCurrentStatus.setStatus("Pending");
        			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        			
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
        			
        			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                     
                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
        			
        		    try
        	        {
        		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
        		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	reportSummaryCurrentStatus.setPersonResponsible("");
        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
        	        }
        		    reportSummaryCurrentStatus.setNextStep("Upload Reports - Above Market");
        			
        		    //https://svn.sourcen.com/issues/19945 - Now upload report will be a link to Report Stage
        		    reportSummaryCurrentStatus.setNextStepLink(stageUrl);
        		    
        		    //Upload Reports will be enabled only when the Report is SAVED and the Full Report and Summary for IRIS checkbox is selected
        		    if(initiationList.get(0).getStatus()==StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getFullReport()!=null 
        		    		&& initiationList.get(0).getFullReport() && initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS())
        		    {
        		    	reportSummaryCurrentStatus.setNextStepEnable(true);
        		    }
        		    else
        		    {
        		    	reportSummaryCurrentStatus.setNextStepEnable(false);
        		    }
        		    reportSummaryCurrentStatus.setMandatory(true);
        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			}
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports - Above Market");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    reportSummaryCurrentStatus.setNextStep("Upload Reports - Above Market");

    		    //https://svn.sourcen.com/issues/19945 - Now upload report will be a link to Report Stage
    		    reportSummaryCurrentStatus.setNextStepLink(stageUrl);
    		    
    		    //Upload Reports will be enabled only when the Report is SAVED and the Full Report and Summary for IRIS checkbox is selected
    		    if(initiationList.get(0).getStatus()==StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getFullReport()!=null 
    		    		&& initiationList.get(0).getFullReport() && initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS())
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getNeedRevisionClicked())
    		{
    			if(!initiationList.get(0).getNeedRevision())
    			{
    		
	    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
	    			reportSummaryCurrentStatus.setActivityDesc("Reports Revision and Update to BAT - Above Market");    		      
	    			reportSummaryCurrentStatus.setStatus("Complete");
	    			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                 
	                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    			
	    		    try
	    	        {
	    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	reportSummaryCurrentStatus.setPersonResponsible("");
	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    reportSummaryCurrentStatus.setNextStep("");
	    		    reportSummaryCurrentStatus.setMandatory(false);
	    		    if(initiationList.get(0).getNeedRevisionClickDate()!=null)
	    		    {
	    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getNeedRevisionClickDate());
	    		    }
	    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			}
    			else
    			{
    				reportSummaryCurrentStatus=new ProjectCurrentStatus();
	    			reportSummaryCurrentStatus.setActivityDesc("Reports Revision and Update to BAT - Above Market");    		      
	    			reportSummaryCurrentStatus.setStatus("Pending");
	    			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			
	    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    			String nextStepSubject = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                 
	                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	                
	    			
	    		    try
	    	        {
	    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	    	        }
	    	        catch(UserNotFoundException ue)
	    	        {
	    	        	reportSummaryCurrentStatus.setPersonResponsible("");
	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	    	        }
	    		    reportSummaryCurrentStatus.setNextStep("Notify BAT about Reports Revision - Above Market");
	    		    reportSummaryCurrentStatus.setMandatory(false);
	    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    			}
    		}
    		
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() 
    				&& initiationList.get(0).getLegalApproval() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals(""))
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Legal Approval - Above Market");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        }
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getRepSummaryLegalApprovalDate()!=null)
     		    {
     		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getRepSummaryLegalApprovalDate());
     		    }
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Legal Approval - Above Market");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        }
    		    reportSummaryCurrentStatus.setNextStep("Advise Legal Approval - Above Market");
    		    reportSummaryCurrentStatus.setNextStepLink(stageUrl+"&legalApprover=true");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() &&  initiationList.get(0).getIsSPIApproved())
    				
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports Approval - Above Market");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getSpiApprovalDate()!=null)
    		    {
    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getSpiApprovalDate());
    		    }
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports Approval - Above Market");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
    		    reportSummaryCurrentStatus.setNextStep("Approve Reports - Above Market");
    		    
    		    //Approve Upload Reports will be enabled only when the Report is SAVED and the Full Report and Summary for IRIS checkbox is selected and Send for Approval is clicked
    		    if(initiationList.get(0).getStatus()==StageStatus.REPORT_SUMMARY_SAVED.ordinal() && initiationList.get(0).getFullReport()!=null 
    		    		&& initiationList.get(0).getFullReport() && initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS()
    		    		&& initiationList.get(0).getSendForApproval()!=null && initiationList.get(0).getSendForApproval() 
    		    		&& initiationList.get(0).getLegalApproval() && initiationList.get(0).getLegalApprover()!=null && !initiationList.get(0).getLegalApprover().equals(""))
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}
    	
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ReportSummaryInitiation rsInitiation: endMarketInitiationList)
    		{

        		if(rsInitiation.getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() && rsInitiation.getSendForApproval())
        		{
        			// This check is because of : https://svn.sourcen.com/issues/19826
        			//https://svn.sourcen.com/issues/19916
        			if((rsInitiation.getFullReport() || rsInitiation.getSummaryReport() || rsInitiation.getSummaryForIRIS()) && !(rsInitiation.getNeedRevision()))
        			
        			{
	        			reportSummaryCurrentStatus=new ProjectCurrentStatus();
	        			reportSummaryCurrentStatus.setActivityDesc("Reports - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));    		      
	        			reportSummaryCurrentStatus.setStatus("Complete");
	        			
	        			reportSummaryCurrentStatus.setEndMarketID(rsInitiation.getEndMarketID());
	        			
	        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	        			
	        			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
	         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                     
	                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	        			
	        		    try
	        	        {
	        		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	        		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	reportSummaryCurrentStatus.setPersonResponsible("");
	        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	        	        }
	        		    reportSummaryCurrentStatus.setNextStep("");
	        		    reportSummaryCurrentStatus.setMandatory(true);
	        		    if(rsInitiation.getRepSummarySaveDate()!=null)
		    		    {
		    		    	reportSummaryCurrentStatus.setCompletionDate(rsInitiation.getRepSummarySaveDate());
		    		    }
	        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        			}
        			else
        			{
        				reportSummaryCurrentStatus=new ProjectCurrentStatus();
            			reportSummaryCurrentStatus.setActivityDesc("Reports - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));
            			reportSummaryCurrentStatus.setStatus("Pending");
            			reportSummaryCurrentStatus.setEndMarketID(rsInitiation.getEndMarketID());
            			        			
            			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
            			reportSummaryCurrentStatus.setPersonRespSubject(subject);
            			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
            			
            			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
             	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
             	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                        String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                        nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                        nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                        nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                         
                        reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                        reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
            			
            		    try
            	        {
            		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
            		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
            		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
            	        }
            	        catch(UserNotFoundException ue)
            	        {
            	        	reportSummaryCurrentStatus.setPersonResponsible("");
            	        	reportSummaryCurrentStatus.setPersonRespEmail("");
            	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
            	        }
            		    
            		    reportSummaryCurrentStatus.setNextStep("Upload Reports - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));

            		  //https://svn.sourcen.com/issues/19945 - Now upload report will be a link to Report Stage
            		    reportSummaryCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+rsInitiation.getEndMarketID());
            		    
            		    reportSummaryCurrentStatus.setNextStepEnable(true);
            		    reportSummaryCurrentStatus.setMandatory(true);
            		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        			}
        		}
        		else
        		{
        			reportSummaryCurrentStatus=new ProjectCurrentStatus();
        			reportSummaryCurrentStatus.setActivityDesc("Reports - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));
        			reportSummaryCurrentStatus.setStatus("Pending");
        			reportSummaryCurrentStatus.setEndMarketID(rsInitiation.getEndMarketID());
        			        			
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
        			
        			String nextStepSubject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                     
                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
        			
        		    try
        	        {
        		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
        		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
        	        }
        	        catch(UserNotFoundException ue)
        	        {
        	        	reportSummaryCurrentStatus.setPersonResponsible("");
        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
        	        }
        		    
        		    reportSummaryCurrentStatus.setNextStep("Upload Reports - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));

        		    //https://svn.sourcen.com/issues/19945 - Now upload report will be a link to Report Stage
        		    reportSummaryCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+rsInitiation.getEndMarketID());
        		    
        		    reportSummaryCurrentStatus.setNextStepEnable(true);
        		    reportSummaryCurrentStatus.setMandatory(true);
        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        		}
        	
        		


    			if(rsInitiation.getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() && rsInitiation.getNeedRevisionClicked())
        		{
        			if(!rsInitiation.getNeedRevision())
        			{
    			
	        			reportSummaryCurrentStatus=new ProjectCurrentStatus();
	        			reportSummaryCurrentStatus.setActivityDesc("Reports Revision and Update to BAT - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));
	        			reportSummaryCurrentStatus.setStatus("Complete");
	        			reportSummaryCurrentStatus.setEndMarketID(rsInitiation.getEndMarketID());
	        			
	        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	        			
	        			String nextStepSubject = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
	         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                     
	                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	                    
	        			
	        		    try
	        	        {
	        		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	        		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	reportSummaryCurrentStatus.setPersonResponsible("");
	        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	        	        }
	        		    reportSummaryCurrentStatus.setNextStep("");
	        		    reportSummaryCurrentStatus.setMandatory(false);
	        		    if(rsInitiation.getNeedRevisionClickDate()!=null)
		    		    {
		    		    	reportSummaryCurrentStatus.setCompletionDate(rsInitiation.getNeedRevisionClickDate());
		    		    }
	        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        			}
        			else
        			{
        				reportSummaryCurrentStatus=new ProjectCurrentStatus();
	        			reportSummaryCurrentStatus.setActivityDesc("Reports Revision and Update to BAT - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));
	        			reportSummaryCurrentStatus.setStatus("Pending");
	        			reportSummaryCurrentStatus.setEndMarketID(rsInitiation.getEndMarketID());
	        			
	        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
	        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
	        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
	        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
	        			
	        			String nextStepSubject = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
	         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
	                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
	                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
	                     
	                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
	                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
	                    
	        			
	        		    try
	        	        {
	        		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(awardedExternalAgency).getName());
	        		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(awardedExternalAgency).getEmail());
	        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	reportSummaryCurrentStatus.setPersonResponsible("");
	        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	        	        }
	        		    reportSummaryCurrentStatus.setNextStep("Notify BAT about Reports Revision -"+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));
	        		    reportSummaryCurrentStatus.setMandatory(false);
	        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        			}
        		}
        		


        		if(rsInitiation.getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() 
        				&& rsInitiation.getLegalApproval() && rsInitiation.getLegalApprover()!=null && !rsInitiation.getLegalApprover().equals(""))
        		{
        			reportSummaryCurrentStatus=new ProjectCurrentStatus();
        			reportSummaryCurrentStatus.setActivityDesc("Legal Approval - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));    		      
        			reportSummaryCurrentStatus.setStatus("Complete");
        			
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
        			
        			if(fundingInvestmentMap!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()!=null)
        			{
	        		    try
		    	        {
	        		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getName());
	        		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getEmail());
		    	        }
		    	        catch(UserNotFoundException ue)
		    	        {
		    	        	reportSummaryCurrentStatus.setPersonResponsible("");
		    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
		    	        }
        			}
        			else
        			{
        				reportSummaryCurrentStatus.setPersonResponsible("");
	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
        			}
        		    reportSummaryCurrentStatus.setNextStep("");
        		    reportSummaryCurrentStatus.setMandatory(true);
        		    if(rsInitiation.getRepSummaryLegalApprovalDate()!=null)
         		    {
         		    	reportSummaryCurrentStatus.setCompletionDate(rsInitiation.getRepSummaryLegalApprovalDate());
         		    }
        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        		}
        		else
        		{
        			reportSummaryCurrentStatus=new ProjectCurrentStatus();
        			reportSummaryCurrentStatus.setActivityDesc("Legal Approval - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));   		      
        			reportSummaryCurrentStatus.setStatus("Pending");
        			
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
        			
        			if(fundingInvestmentMap!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()!=null)
        			{
        				try
        				{
	         		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getName());
	         		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getEmail());
	 	    	        }
	 	    	        catch(UserNotFoundException ue)
	 	    	        {
	 	    	        	reportSummaryCurrentStatus.setPersonResponsible("");
	 	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	 	    	        }
        			}
        			else
        			{
        				reportSummaryCurrentStatus.setPersonResponsible("");
 	    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
        			}
        		    reportSummaryCurrentStatus.setNextStep("Advise Legal Approval - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));
        		    reportSummaryCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+rsInitiation.getEndMarketID()+"&legalApprover=true");
        		    reportSummaryCurrentStatus.setMandatory(true);
        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        		}
        	


        		if(rsInitiation.getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() &&  rsInitiation.getIsSPIApproved())
        				
        		{
        			reportSummaryCurrentStatus=new ProjectCurrentStatus();
        			reportSummaryCurrentStatus.setActivityDesc("Reports Approval - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));    		      
        			reportSummaryCurrentStatus.setStatus("Complete");
        			
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
        			
        		
        			if(fundingInvestmentMap!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()!=null)
        			{
	        			try
	        	        {
	        				reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getName());
	         		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getEmail());
	        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	reportSummaryCurrentStatus.setPersonResponsible("");
	        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	        	        }
        			}
        			else
        			{
        				reportSummaryCurrentStatus.setPersonResponsible("");
        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
        	           	try
	        	        {
	        				reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	        	        }
        			}
        			
        		    
        		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                    
                    
                    nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                     
                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                    
        		    reportSummaryCurrentStatus.setNextStep("");
        		    reportSummaryCurrentStatus.setMandatory(true);
        		    if(rsInitiation.getSpiApprovalDate()!=null)
        		    {
        		    	reportSummaryCurrentStatus.setCompletionDate(rsInitiation.getSpiApprovalDate());
        		    }
        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        		}
        		else
        		{
        			reportSummaryCurrentStatus=new ProjectCurrentStatus();
        			reportSummaryCurrentStatus.setActivityDesc("Reports Approval - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));   		      
        			reportSummaryCurrentStatus.setStatus("Pending");
        			reportSummaryCurrentStatus.setEndMarketID(rsInitiation.getEndMarketID());
        			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
        	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
        	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
        	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
        			reportSummaryCurrentStatus.setPersonRespSubject(subject);
        			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
        			
        			if(fundingInvestmentMap!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID())!=null && fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()!=null)
        			{
	        			try
	        	        {
	        				reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getName());
	         		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(rsInitiation.getEndMarketID()).getSpiContact()).getEmail());
	        		    	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	reportSummaryCurrentStatus.setPersonResponsible("");
	        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
	        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	        	        }
        			}
        			else
        			{
        				reportSummaryCurrentStatus.setPersonResponsible("");
        	        	reportSummaryCurrentStatus.setPersonRespEmail("");
        	           	try
	        	        {
	        				reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
	        	        }
	        	        catch(UserNotFoundException ue)
	        	        {
	        	        	reportSummaryCurrentStatus.setNextStepPersonEmail("");
	        	        }
        			}
        			
        		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
         	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                    String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                    
                    
                    nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                    nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                     
                    reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                    reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
        		    reportSummaryCurrentStatus.setNextStep("Approve Reports - "+ SynchroGlobal.getEndMarkets().get(new Integer(rsInitiation.getEndMarketID()+"")));
        		    //Approve Upload Reports for End Markets will be enabled only when the Report is SAVED  and Send for Approval is clicked
        		    if(rsInitiation.getStatus()==StageStatus.REPORT_SUMMARY_SAVED.ordinal() 
        		    		&& rsInitiation.getSendForApproval()!=null && rsInitiation.getSendForApproval()
        		    		&& rsInitiation.getLegalApproval() && rsInitiation.getLegalApprover()!=null && !rsInitiation.getLegalApprover().equals(""))
        		    {
        		    	reportSummaryCurrentStatus.setNextStepEnable(true);
        		    }
        		    else
        		    {
        		    	reportSummaryCurrentStatus.setNextStepEnable(false);
        		    }
        		    reportSummaryCurrentStatus.setMandatory(true);
        		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
        		}
        		
        	
    		
    		
    		}
    	}
    	
    	
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() &&  initiationList.get(0).getIsSPIApproved() &&  initiationList.get(0).getUploadToIRIS())
    				
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for IRIS Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    //	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        //	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String uploadToIrisEmail = JiveGlobals.getJiveProperty("upload.iris.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToIrisEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.iris.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getIrisUploadDate()!=null)
    		    {
    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getIrisUploadDate());
    		    }
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for IRIS Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    //	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        //	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    
    		    String uploadToIrisEmail = JiveGlobals.getJiveProperty("upload.iris.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToIrisEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.iris.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
    		    reportSummaryCurrentStatus.setNextStep("Request for Upload to IRIS");
    		    
    		    //Request for Upload to IRIS Reports will be enabled only when the Report is APPROVED and Legal Apprval is done
    		    if( initiationList.get(0).getIsSPIApproved()!=null && initiationList.get(0).getIsSPIApproved()
    		    		&& initiationList.get(0).getLegalApproval()!=null && initiationList.get(0).getLegalApproval())
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()>=StageStatus.REPORT_SUMMARY_SAVED.ordinal() &&  initiationList.get(0).getIsSPIApproved() &&  initiationList.get(0).getUploadToCPSIDdatabase())
    				
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for C-PSI Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Complete");
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    	//reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        	//reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String uploadToCPSIDatabaseEmail = JiveGlobals.getJiveProperty("upload.cpsi.database.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToCPSIDatabaseEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.c.psi.database.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.c.psi.database.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
                
    		    reportSummaryCurrentStatus.setNextStep("");
    		    reportSummaryCurrentStatus.setMandatory(true);
    		    if(initiationList.get(0).getCpsiUploadDate()!=null)
    		    {
    		    	reportSummaryCurrentStatus.setCompletionDate(initiationList.get(0).getCpsiUploadDate());
    		    }
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		else
    		{
    			reportSummaryCurrentStatus=new ProjectCurrentStatus();
    			reportSummaryCurrentStatus.setActivityDesc("Reports for C-PSI Upload");    		      
    			reportSummaryCurrentStatus.setStatus("Pending");
    			reportSummaryCurrentStatus.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    			
    			messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(reportSummaryCurrentStatus.getActivityDesc()));
    			reportSummaryCurrentStatus.setPersonRespSubject(subject);
    			reportSummaryCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	reportSummaryCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	reportSummaryCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    		    //	reportSummaryCurrentStatus.setNextStepPersonEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	reportSummaryCurrentStatus.setPersonResponsible("");
    	        	reportSummaryCurrentStatus.setPersonRespEmail("");
    	        //	reportSummaryCurrentStatus.setNextStepPersonEmail("");
    	        }
    		    
    		    String uploadToCPSIDatabaseEmail = JiveGlobals.getJiveProperty("upload.cpsi.database.admin.email", "assistance@batinsights.com");
		    	reportSummaryCurrentStatus.setNextStepPersonEmail(uploadToCPSIDatabaseEmail);
		    	
    		    String nextStepSubject = TemplateUtil.getTemplate("reportSummary.upload.on.c.psi.database.subject", JiveGlobals.getLocale());
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
     	        nextStepSubject=nextStepSubject.replaceAll("\\$\\{projectName\\}", project.getName());
                String nextStepMessage = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.c.psi.database.htmlBody", JiveGlobals.getLocale());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{projectName\\}", project.getName());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                
                
                nextStepSubject=nextStepSubject.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                nextStepMessage=nextStepMessage.replaceAll("\\$\\{userName\\}", reportSummaryCurrentStatus.getPersonResponsible());
                 
                reportSummaryCurrentStatus.setNextStepSubject(nextStepSubject);
                reportSummaryCurrentStatus.setNextStepMessage(nextStepMessage);
    		    reportSummaryCurrentStatus.setNextStep("Request for Upload to C-PSI Database");
    		    
    		  //Request for Upload to C-PSI Database will be enabled only when the Report is APPROVED and Legal Apprval is done
    		    if( initiationList.get(0).getIsSPIApproved()!=null && initiationList.get(0).getIsSPIApproved()
    		    		&& initiationList.get(0).getLegalApproval()!=null && initiationList.get(0).getLegalApproval())
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(true);
    		    }
    		    else
    		    {
    		    	reportSummaryCurrentStatus.setNextStepEnable(false);
    		    }
    		    reportSummaryCurrentStatus.setMandatory(true);
    		     
    		    reportSummaryCurrentStatusList.add(reportSummaryCurrentStatus);
    		}
    		
    	}
       	
    	return reportSummaryCurrentStatusList;
	}

    @Override
    public Map<Long,List<ProjectStagePendingFields>> getReportSummaryMultiPendingFields( Long projectID, List<Long> emIds)
    {
    	List<ProjectStagePendingFields> reportSummaryPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    	
    	
    	List<ReportSummaryInitiation> initiationList = reportSummaryManager.getReportSummaryInitiation(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	List<ReportSummaryInitiation> endMarketInitiationList = new ArrayList<ReportSummaryInitiation>();
    	
    	Map<Long,List<ProjectStagePendingFields>> rsPendingFieldsListMap = new HashMap<Long, List<ProjectStagePendingFields>>();
        
    	for(Long endMarketId : emIds)
    	{
    		List<ReportSummaryInitiation> rsEMList = reportSummaryManager.getReportSummaryInitiation(projectID,endMarketId);
    		if(rsEMList!=null && rsEMList.size()>0)
    		{
    			endMarketInitiationList.add(rsEMList.get(0));
    		}
    	}
    	
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		ProjectStagePendingFields reportSummaryPendingFields = new ProjectStagePendingFields();
    		Map<Integer, List<AttachmentBean>> attachmentMap = reportSummaryManager.getDocumentAttachment(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    		if(initiationList.get(0).getFullReport()!=null && initiationList.get(0).getFullReport())
    		{
    			
    			reportSummaryPendingFields.setFieldName("Research Report(s)");
    			reportSummaryPendingFields.setInformationProvided(true);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		else
    		{
    			
    			reportSummaryPendingFields.setFieldName("Research Report(s)");
    			reportSummaryPendingFields.setInformationProvided(false);
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		
    		if(initiationList.get(0).getSummaryForIRIS()!=null && initiationList.get(0).getSummaryForIRIS())
    		{
    			reportSummaryPendingFields = new ProjectStagePendingFields();
    			reportSummaryPendingFields.setFieldName("Summary for IRIS");
    			reportSummaryPendingFields.setInformationProvided(true);
    			//https://svn.sourcen.com/issues/20030
    			reportSummaryPendingFields.setAttachmentDone("Yes");
    			/*
    			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}*/
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		else
    		{
    			reportSummaryPendingFields = new ProjectStagePendingFields();
    			reportSummaryPendingFields.setFieldName("Summary for IRIS");
    			reportSummaryPendingFields.setInformationProvided(false);
    			/*if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()).size()>0)
    			{
    				reportSummaryPendingFields.setAttachmentDone("Yes");
    			}
    			else
    			{
    				reportSummaryPendingFields.setAttachmentDone("No");
    			}*/
    			reportSummaryPendingFields.setAttachmentDone("No");
    			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
    		}
    		
    		rsPendingFieldsListMap.put(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,reportSummaryPendingFieldsList);	
    	}
    	
    	// RS - Pending Field List for End Markets
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ReportSummaryInitiation rsEM: endMarketInitiationList )
    		{
    			ProjectStagePendingFields reportSummaryPendingFields = new ProjectStagePendingFields();
    			reportSummaryPendingFieldsList = new ArrayList<ProjectStagePendingFields>();
    			Map<Integer, List<AttachmentBean>> attachmentMap = reportSummaryManager.getDocumentAttachment(projectID, rsEM.getEndMarketID());
    			
        		if(rsEM.getFullReport()!=null && rsEM.getFullReport())
        		{
        			
        			reportSummaryPendingFields.setFieldName("Research Report(s)");
        			reportSummaryPendingFields.setInformationProvided(true);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId()).size()>0)
        			{
        				reportSummaryPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				reportSummaryPendingFields.setAttachmentDone("No");
        			}
        			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
        		}
        		else
        		{
        			
        			reportSummaryPendingFields.setFieldName("Research Report(s)");
        			reportSummaryPendingFields.setInformationProvided(false);
        			if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId()).size()>0)
        			{
        				reportSummaryPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				reportSummaryPendingFields.setAttachmentDone("No");
        			}
        			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
        		}
        		
        		if(rsEM.getSummaryForIRIS()!=null && rsEM.getSummaryForIRIS())
        		{
        			reportSummaryPendingFields = new ProjectStagePendingFields();
        			reportSummaryPendingFields.setFieldName("Summary for IRIS");
        			reportSummaryPendingFields.setInformationProvided(true);
        			/*if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()).size()>0)
        			{
        				reportSummaryPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				reportSummaryPendingFields.setAttachmentDone("No");
        			}*/
        			reportSummaryPendingFields.setAttachmentDone("Yes");
        			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
        		}
        		else
        		{
        			reportSummaryPendingFields = new ProjectStagePendingFields();
        			reportSummaryPendingFields.setFieldName("Summary for IRIS");
        			reportSummaryPendingFields.setInformationProvided(false);
        			/*if(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId()).size()>0)
        			{
        				reportSummaryPendingFields.setAttachmentDone("Yes");
        			}
        			else
        			{
        				reportSummaryPendingFields.setAttachmentDone("No");
        			}*/
        			reportSummaryPendingFields.setAttachmentDone("No");
        			reportSummaryPendingFieldsList.add(reportSummaryPendingFields);
        		}
        		
        		rsPendingFieldsListMap.put(rsEM.getEndMarketID(),reportSummaryPendingFieldsList);
    		}
    	}
    	return rsPendingFieldsListMap;
    }
    
    @Override
    public List<ProjectCurrentStatus> getProjectEvaluationMultiStatusList(final long projectID, String stageUrl, List<Long> emIds, List<FundingInvestment> fundingInvestmentList)
    {
    	List<ProjectEvaluationInitiation> initiationList = projectEvaluationManager.getProjectEvaluationInitiation(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	
    	List<ProjectEvaluationInitiation> endMarketInitiationList = new ArrayList<ProjectEvaluationInitiation>();
    	
    	for(Long endMarketId : emIds)
    	{
    		List<ProjectEvaluationInitiation> peEMList = projectEvaluationManager.getProjectEvaluationInitiation(projectID, endMarketId);
    		if(peEMList!=null && peEMList.size()>0)
    		{
    			endMarketInitiationList.add(peEMList.get(0));
    		}
    	}
    	Map<Long, FundingInvestment> fundingInvestmentMap = new HashMap<Long, FundingInvestment>();
    	if(fundingInvestmentList!=null && fundingInvestmentList.size()>0)
    	{
    		for(FundingInvestment fi : fundingInvestmentList)
    		{
    			if(fi.getAboveMarket())
    			{
    				fundingInvestmentMap.put(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, fi);
    			}
    			else
    			{
    				fundingInvestmentMap.put(fi.getFieldworkMarketID(), fi);
    			}
    		}
    	}
    	
    	Project project = synchroProjectManager.get(projectID);
    	
    	List<ProjectCurrentStatus> projectEvaluationCurrentStatusList = new ArrayList<ProjectCurrentStatus>();
    	ProjectCurrentStatus projectEvaluationCurrentStatus = null;
    	
    	//String stageUrl = baseUrl+"/synchro/project-eval!input.jspa?projectID=" + projectID;
    	
    	String subject = TemplateUtil.getTemplate("pendingActions.notifyPersonResponsible.subject", JiveGlobals.getLocale());
    	subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	subject=subject.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	
    	
    	String messageBody = TemplateUtil.getHtmlEscapedTemplate("pendingActions.notifyPersonResponsible.htmlBody", JiveGlobals.getLocale());
    	messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectID));
    	messageBody=messageBody.replaceAll("\\$\\{projectName\\}", Matcher.quoteReplacement(project.getName()));
    	messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
    	
    	if(initiationList!=null && initiationList.size()>0)
    	{
    		if(initiationList.get(0).getStatus()==StageStatus.PROJ_EVAL_COMPLETED.ordinal())
    		{
    			projectEvaluationCurrentStatus=new ProjectCurrentStatus();
    			projectEvaluationCurrentStatus.setActivityDesc("Agency Evaluation - Above Market");    		      
    			projectEvaluationCurrentStatus.setStatus("Complete");
    			
    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectEvaluationCurrentStatus.getActivityDesc()));
    			projectEvaluationCurrentStatus.setPersonRespSubject(subject);
    			projectEvaluationCurrentStatus.setPersonRespMessage(messageBody);
    			
    		    try
    	        {
    		    	projectEvaluationCurrentStatus.setPersonResponsible(userManager.getUser(project.getProjectOwner()).getName());
    		    	projectEvaluationCurrentStatus.setPersonRespEmail(userManager.getUser(project.getProjectOwner()).getEmail());
    	        }
    	        catch(UserNotFoundException ue)
    	        {
    	        	projectEvaluationCurrentStatus.setPersonResponsible("");
    	        	projectEvaluationCurrentStatus.setPersonRespEmail("");
    	        }
    		    projectEvaluationCurrentStatus.setNextStep("Complete Evaluation - Above Market");
    		    projectEvaluationCurrentStatus.setNextStepLink(stageUrl);
    		    if(initiationList.get(0).getModifiedDate()!=null)
    		    {
    		    	projectEvaluationCurrentStatus.setCompletionDate(new Date(initiationList.get(0).getModifiedDate()));
    		    }
    		    projectEvaluationCurrentStatusList.add(projectEvaluationCurrentStatus);
    		}
    		
    	}
    	
    	if(endMarketInitiationList!=null && endMarketInitiationList.size()>0)
    	{
    		for(ProjectEvaluationInitiation pEval : endMarketInitiationList)
    		{
	    		if(pEval.getStatus()==StageStatus.PROJ_EVAL_COMPLETED.ordinal())
	    		{
	    			projectEvaluationCurrentStatus=new ProjectCurrentStatus();
	    			projectEvaluationCurrentStatus.setActivityDesc("Agency Evaluation - "+ SynchroGlobal.getEndMarkets().get(new Integer(pEval.getEndMarketId()+"")));    		      
	    			projectEvaluationCurrentStatus.setStatus("Complete");
	    			
	    			messageBody=messageBody.replaceAll("\\$\\{activityDescription\\}", Matcher.quoteReplacement(projectEvaluationCurrentStatus.getActivityDesc()));
	    			projectEvaluationCurrentStatus.setPersonRespSubject(subject);
	    			projectEvaluationCurrentStatus.setPersonRespMessage(messageBody);
	    			
	    		    if(fundingInvestmentMap!=null && fundingInvestmentMap.get(pEval.getEndMarketId())!=null && fundingInvestmentMap.get(pEval.getEndMarketId()).getSpiContact()!=null)
	    		    {
		    			try
		    	        {
		    		    	projectEvaluationCurrentStatus.setPersonResponsible(userManager.getUser(fundingInvestmentMap.get(pEval.getEndMarketId()).getSpiContact()).getName());
		    		    	projectEvaluationCurrentStatus.setPersonRespEmail(userManager.getUser(fundingInvestmentMap.get(pEval.getEndMarketId()).getSpiContact()).getEmail());
		    	        }
		    	        catch(UserNotFoundException ue)
		    	        {
		    	        	projectEvaluationCurrentStatus.setPersonResponsible("");
		    	        	projectEvaluationCurrentStatus.setPersonRespEmail("");
		    	        }
	    		    }
	    		    else
	    		    {
	    		    	projectEvaluationCurrentStatus.setPersonResponsible("");
	    	        	projectEvaluationCurrentStatus.setPersonRespEmail("");
	    		    }
	    		    projectEvaluationCurrentStatus.setNextStep("Complete Evaluation -" + SynchroGlobal.getEndMarkets().get(new Integer(pEval.getEndMarketId()+"")));
	    		    projectEvaluationCurrentStatus.setNextStepLink(stageUrl+"&endMarketId="+pEval.getEndMarketId());
	    		    if(pEval.getModifiedDate()!=null)
	    		    {
	    		    	projectEvaluationCurrentStatus.setCompletionDate(new Date(pEval.getModifiedDate()));
	    		    }
	    		    projectEvaluationCurrentStatusList.add(projectEvaluationCurrentStatus);
	    		}
    		}
    		
    	}
    	return projectEvaluationCurrentStatusList;
    }
	public ProjectManager getSynchroProjectManager() {
		return synchroProjectManager;
	}

	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
		this.synchroProjectManager = synchroProjectManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public PIBManager getPibManager() {
		return pibManager;
	}

	public void setPibManager(PIBManager pibManager) {
		this.pibManager = pibManager;
	}

	public ProposalManager getProposalManager() {
		return proposalManager;
	}

	public void setProposalManager(ProposalManager proposalManager) {
		this.proposalManager = proposalManager;
	}

	public ProjectSpecsManager getProjectSpecsManager() {
		return projectSpecsManager;
	}

	public void setProjectSpecsManager(ProjectSpecsManager projectSpecsManager) {
		this.projectSpecsManager = projectSpecsManager;
	}

	public SynchroUtils getSynchroUtils() {
		return synchroUtils;
	}

	public void setSynchroUtils(SynchroUtils synchroUtils) {
		this.synchroUtils = synchroUtils;
	}

	public ReportSummaryManager getReportSummaryManager() {
		return reportSummaryManager;
	}

	public void setReportSummaryManager(ReportSummaryManager reportSummaryManager) {
		this.reportSummaryManager = reportSummaryManager;
	}

	public ProjectEvaluationManager getProjectEvaluationManager() {
		return projectEvaluationManager;
	}

	public void setProjectEvaluationManager(
			ProjectEvaluationManager projectEvaluationManager) {
		this.projectEvaluationManager = projectEvaluationManager;
	}

	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

    }
