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
 * Action class for Project Evaluation Stage
 */
public class ProjectEvaluationAction extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(ProjectEvaluationAction.class);
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
             	
                List<ProjectEvaluationInitiation> initiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
             
                //Audit Log trails
                List<ProjectEvaluationInitiation> initiationList_DB = this.projectEvaluationManager.getProjectEvaluationInitiation(projectID,endMarketDetails.get(0).getEndMarketID());
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
                    
        }
    }

    public String input() {
      	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	if ((SynchroPermHelper.hasProjectAccess(projectID) && SynchroPermHelper.canViewStage(projectID, stageId)) || SynchroPermHelper.canAccessProject(projectID)) {
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
        	//String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-eval!input.jspa?projectID=" + projectID;
        	String baseUrl = URLUtils.getBaseURL(request);
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
        	}
            //Audit logs for field checks
        	SynchroLogUtils.EvaluationSave(project, projectEvaluationInitiation, projectEvaluationInitiation_DB);
        	
        	//Audit Logs: Project Evaluation SAVE
            String i18Text = getText("logger.project.saved.text");
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
    								SynchroGlobal.LogProjectStage.PROJECT_EVALUATION.getId(), i18Text, project.getName(), 
    										project.getProjectID(), getUser().getID());	
            
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

	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}


}
