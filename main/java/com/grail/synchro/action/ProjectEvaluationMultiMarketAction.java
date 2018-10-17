package com.grail.synchro.action;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.URLUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.mail.EmailMessage;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: tejinder
 * @since: 1.0
 * Action class for Project Evaluation Stage for Multi Market
 */
public class ProjectEvaluationMultiMarketAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ProjectEvaluationMultiMarketAction.class);
    //Spring Managers
    private ProjectEvaluationManager projectEvaluationManager;
    private ProjectManager synchroProjectManager;
    private ReportSummaryManager reportSummaryManager;
    
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
    private Boolean isAboveMarket;
    private String redirectURL;
    
    private StageManager stageManager;
    
    public void prepare() throws Exception {
    	
        final String id = getRequest().getParameter("projectID");
        if(id != null ) {

                try{
                    projectID = Long.parseLong(id);
                    
                } catch (NumberFormatException nfEx) {
                    LOG.error("Invalid ProjectID ");
                    throw nfEx;
                }
               
                project = this.synchroProjectManager.get(projectID);
                
               // endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
                endMarketDetails = this.synchroProjectManager.getEndMarketDetails(projectID);
                for(EndMarketInvestmentDetail endmarketDetail : endMarketDetails)
                {
                	if(endmarketDetail.getEndMarketID()>0 && endmarketDetail.getEndMarketID().intValue()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID.intValue())
                	{
                		int status  = this.synchroProjectManager.getEndMarketStatus(projectID, endmarketDetail.getEndMarketID());
                		if(status>=0)
                		{
                			endmarketDetail.setStatus(status);
                		}
                	}                	
                }
               
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
                	
                	 redirectURL="/synchro/project-multi-eval!input.jspa?projectID="+projectID+"&endMarketId="+endMarketId;
                }
                else
                {
                	isAboveMarket=true;
                	// To add the Above Market Endmarket id
                	//endMarketId=endMarketDetails.get(0).getEndMarketID();
                	endMarketId = SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID;
                	redirectURL="/synchro/project-multi-eval!input.jspa?projectID="+projectID;
                }
                
                //Audit Log trails
                List<ProjectEvaluationInitiation> initiationList_DB = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketId);
                if(initiationList_DB!=null && initiationList_DB.size() > 0)
                {
                	projectEvaluationInitiation_DB = initiationList_DB.get(0);
                }
                
                List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketId);
           
                if( initiationList != null && initiationList.size() > 0) {
                    this.projectEvaluationInitiation = initiationList.get(0);
                
                }  else {
                    this.projectEvaluationInitiation = new ProjectEvaluationInitiation();
                    
                    isSave = true;
                }
               
                stageId = SynchroGlobal.getProjectActivityTab().get("projectEvaluation");
    		
    			
    	
        }
      
        if(getRequest().getMethod() == "POST") {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.projectEvaluationInitiation);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the Project Evaluation Initiation bean.");
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
    	if ((SynchroPermHelper.hasProjectAccessMultiMarket(projectID) && SynchroPermHelper.canViewStage(projectID, stageId)) || SynchroPermHelper.canAccessProject(projectID)) {
    		
    		//Code to check if endmarket is not cancelled as per SVN 19036
    		String emId = getRequest().getParameter("endMarketId");
            if(emId!=null && !emId.equals("") && !emId.equals(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID.toString()))
            {
            	 try{
            		 endMarketId=Long.parseLong(emId);
                 } catch (NumberFormatException nfEx) {
                     LOG.error("Invalid End Market Id ");
                     throw nfEx;
                 }
            	 
            	Integer status = this.synchroProjectManager.getEndMarketStatus(projectID, endMarketId);
            	if(status!=null && status==SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal())
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

        LOG.info("Save the Project Evaluation Details ...."+projectEvaluationInitiation);

      //  if( projectInitiation != null && ribDocument != null){
        if( projectEvaluationInitiation != null){
        	projectEvaluationInitiation.setProjectID(projectID);
        	if(isSave) {
        		projectEvaluationInitiation.setCreationBy(getUser().getID());
        		projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
                
        		projectEvaluationInitiation.setModifiedBy(getUser().getID());
        		projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
                
        		// Only when the Ratings (either IM, LM or FA) have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
        		if((projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfIM()>0)
                        || (projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation.getAgencyPerfLM()>0) 
                        || (projectEvaluationInitiation.getAgencyPerfFA()!=null && projectEvaluationInitiation.getAgencyPerfFA()>0))
                {
        			//synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal());
        			List<ReportSummaryInitiation> repSummList = reportSummaryManager.getReportSummaryInitiation(projectID);
        			// https://www.svn.sourcen.com/issues/17626
        			if(repSummList!=null && repSummList.size()>0 && repSummList.get(0).getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal())
        			{
        				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal());
        			}
        			projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());
                }
                this.projectEvaluationManager.saveProjectEvaluationDetails(projectEvaluationInitiation);

               
            }
            else {
            	projectEvaluationInitiation.setModifiedBy(getUser().getID());
            	projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
            	// Only when the Ratings (either IM, LM or FA) have been given then the status of the Project to be changed to COMPLETED PROJECT EVALUATION
        		if((projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfIM()>0)
                        || (projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation.getAgencyPerfLM()>0) 
                        || (projectEvaluationInitiation.getAgencyPerfFA()!=null && projectEvaluationInitiation.getAgencyPerfFA()>0))
                {
        			List<ReportSummaryInitiation> repSummList = reportSummaryManager.getReportSummaryInitiation(projectID);
        			// https://www.svn.sourcen.com/issues/17626
        			if(repSummList!=null && repSummList.size()>0 && repSummList.get(0).getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal())
        			{
        				synchroProjectManager.updateProjectStatus(projectID, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal());
        			}
        			
        			projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());
                }
                this.projectEvaluationManager.updateProjectEvaluationDetails(projectEvaluationInitiation);
            }
            
        	// https://www.svn.sourcen.com/issues/18858
        	//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-multi-eval!input.jspa?projectID=" + projectID;
        	String baseUrl = URLUtils.getBaseURL(request);
        	String stageUrl = baseUrl+"/synchro/project-multi-eval!input.jspa?projectID=" + projectID;
        	if(SynchroPermHelper.isAwardedExternalAgencyUser(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
        	{
        		String stakeholdersRoles = SynchroConstants.PROJECT_OWNER_ROLE +","+ SynchroConstants.SYNCHRO_GLOBAL_PROJECT_CONTACT_GROUP_NAME;
                String stakeholders = stageManager.getNotificationRecipients(stakeholdersRoles, projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
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
                String externalAgency = stageManager.getNotificationRecipients(externalAgencyRoles, projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        		
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
        	}
        	
        	//Audit logs for field checks
        	SynchroLogUtils.EvaluationMultiMarketSave(project, projectEvaluationInitiation, projectEvaluationInitiation_DB, endMarketId);
        	
        	//Audit Logs: Project Evaluation SAVE
            String i18Text = getText("logger.project.saved.text");
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
    								SynchroGlobal.LogProjectStage.PROJECT_EVALUATION.getId(), i18Text, project.getName(), 
    										project.getProjectID(), getUser().getID(), endMarketId);	
        } else {
            LOG.error("Project Evaluation Initiation was null  ");
            addActionError("Project Evaluation Initiation was null.");
        }
        return SUCCESS;
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

	public ProjectEvaluationManager getProjectEvaluationManager() {
		return projectEvaluationManager;
	}

	public void setProjectEvaluationManager(
			ProjectEvaluationManager projectEvaluationManager) {
		this.projectEvaluationManager = projectEvaluationManager;
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

	public ReportSummaryManager getReportSummaryManager() {
		return reportSummaryManager;
	}

	public void setReportSummaryManager(ReportSummaryManager reportSummaryManager) {
		this.reportSummaryManager = reportSummaryManager;
	}

	public Boolean getIsAboveMarket() {
		return isAboveMarket;
	}

	public void setIsAboveMarket(Boolean isAboveMarket) {
		this.isAboveMarket = isAboveMarket;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}


}
