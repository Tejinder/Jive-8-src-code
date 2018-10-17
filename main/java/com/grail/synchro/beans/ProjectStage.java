package com.grail.synchro.beans;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.*;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.lifecycle.JiveApplication;


import java.util.List;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class ProjectStage {
    private Integer stageNumber;
    private String stageName;
    private String stageURL;
    private String description;
    private boolean isCurrentStage = false;
    private boolean hasAccess = false;
    private boolean isProjectEvaluationStage = false;
    private boolean isProjectEvaluationCompleted = false;
    private boolean isProjectCompleted = false;
    private static ProposalManager proposalManager;
    private static SynchroUtils synchroUtils;
    private static PIBManager pibManager;
    private static ProjectManager synchroProjectManager;
    private static ProjectSpecsManager projectSpecsManager;
    private static ReportSummaryManager reportSummaryManager;
    private static ProjectEvaluationManager projectEvaluationManager;
    private static Integer currentStageNumber;
    private static Project currentProject;



    public ProjectStage(final Project project, final StageType stageType, final boolean hasAccess) {
        this.stageNumber = stageType.getStageNumber();
        this.stageName = stageType.getStageName();
        this.description = stageType.getStageName();
        this.stageURL = generateURL(project, stageType.getStageNumber());
        this.isCurrentStage = (this.stageNumber == getCurrentStageNumber(project));
        this.hasAccess = hasAccess;
        this.isProjectCompleted = (project.getStatus() == SynchroGlobal.Status.COMPLETED.ordinal());
    }

    public static String generateURL(final Project project, final Integer stageNumber) {
        StringBuilder urlBuilder = new StringBuilder();
        if(project != null) {
            boolean isMultiMarket = project.getMultiMarket();

            StringBuilder subURLBuilder = new StringBuilder();
            
            // This is done for changing the URL for Project in Draft Status
            if(project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())
            {
            	if(isMultiMarket) {
                    subURLBuilder.append("create-multimarket-project!input.jspa");
                } else {
                    subURLBuilder.append("create-project!input.jspa");
                }
            }
            else if(stageNumber.equals(StageType.PIB.getStageNumber())) {
                if(isMultiMarket) {
                    subURLBuilder.append("pib-multi-details!input.jspa");
                } else {
                    subURLBuilder.append("pib-details!input.jspa");
                }
            } else if(stageNumber.equals(StageType.PROPOSAL.getStageNumber())) {
                //TODO: Need to update once multi-market project integrated for proposal
                if(isMultiMarket) {
                    subURLBuilder.append("proposal-multi-details!input.jspa");
                } else {
                    subURLBuilder.append("proposal-details!input.jspa");
                }
            } else if(stageNumber.equals(StageType.PROJECT_SPECS.getStageNumber())) {
                //TODO: Need to update once multi-market project integrated for project specs
                if(isMultiMarket) {
                    subURLBuilder.append("project-multi-specs!input.jspa");
                } else {
                    subURLBuilder.append("project-specs!input.jspa");
                }
            } else if(stageNumber.equals(StageType.REPORT.getStageNumber())) {
                //TODO: Need to update once multi-market project integrated for reports
                
                if(isMultiMarket) {
                	subURLBuilder.append("report-multi-summary!input.jspa");
                } else {
                	subURLBuilder.append("report-summary!input.jspa");
                }
            } else if(stageNumber.equals(StageType.PROJECT_EVALUATION.getStageNumber())) {
                //TODO: Need to update once multi-market project integrated for project evaluation
            	 if(isMultiMarket) {
            		 subURLBuilder.append("project-multi-eval!input.jspa");
                 } else {
                	 subURLBuilder.append("project-eval!input.jspa");
                 }
            	
            }

            if(subURLBuilder.length() > 0) {
                urlBuilder.append("/synchro/")
                        .append(subURLBuilder.toString())
                        .append("?projectID=").append(project.getProjectID());
            }

        }
        return urlBuilder.toString();
    }
    
    public static String generateURLNew(final Project project) {
        StringBuilder urlBuilder = new StringBuilder();
        if(project != null) {
            

            StringBuilder subURLBuilder = new StringBuilder();
            
            // This is for End Market Fieldwork Projects
            if(project.getFieldWorkStudy()!=null && project.getFieldWorkStudy()==1)
            {
	            
	            if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.DRAFT.ordinal())
	            {
	               subURLBuilder.append("create-project!input.jspa");
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()||project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal())
	            {
	                subURLBuilder.append("proposal-details-fieldwork!input.jspa");
	            }
	           
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal())
	            {
	                subURLBuilder.append("project-eval-fieldwork!input.jspa");
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.CLOSE.ordinal())
	            {
	                subURLBuilder.append("project-close-fieldwork!input.jspa");
	            }
            }
            else
            {
            	if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.DRAFT.ordinal())
	            {
	               // As Global/Regional NON EU projects will have different URL for Project Creation
            	   if(project!=null && project.getProcessType()!=null && project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId())
	               {
	            	   // This check is for ONLY GLOBAL TYPE PROJECTS
            		   if(project.getOnlyGlobalType()!=null && project.getOnlyGlobalType().intValue()==1)
	            	   {
	            		   subURLBuilder.append("create-project-global!input.jspa");
	            		   if(subURLBuilder.length() > 0)
		            	   {
		                       urlBuilder.append("/new-synchro/")
		                               .append(subURLBuilder.toString())
		                               .append("?projectID=").append(project.getProjectID()).append("&globalProjectType=GLOBAL");
		            	   }
	            	   }
	            	   else
	            	   {
	            		   subURLBuilder.append("create-project-global!input.jspa");
	            	   }
	               }
            	   // This is done for DRAFT projects for GLOBAL EU PROCESS
            	   else if(project!=null && project.getProcessType()!=null && (project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId()))
	               {
	            	   subURLBuilder.append("create-project!input.jspa");
	            	   
	            	   if(subURLBuilder.length() > 0)
	            	   {
	                       urlBuilder.append("/new-synchro/")
	                               .append(subURLBuilder.toString())
	                               .append("?projectID=").append(project.getProjectID()).append("&globalProjectType=EU");
	            	   }
	               }
            	   else
	               {
	            	   subURLBuilder.append("create-project!input.jspa");
	               }
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
	            {
	                subURLBuilder.append("pib-details!input.jspa");
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal())
	            {
	                subURLBuilder.append("proposal-details!input.jspa");
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal())
	            {
	                subURLBuilder.append("project-specs!input.jspa");
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal())
	            {
	            	subURLBuilder.append("report-summary!input.jspa");
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal())
	            {
	                subURLBuilder.append("project-eval!input.jspa");
	            }
	            else if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.CLOSE.ordinal())
	            {
	                subURLBuilder.append("project-close!input.jspa");
	            }
            }
	            
       
            if(subURLBuilder.length() > 0 && urlBuilder.toString().equals("")) {
                urlBuilder.append("/new-synchro/")
                        .append(subURLBuilder.toString())
                        .append("?projectID=").append(project.getProjectID());
            }
            
            // This is done for Migrated projects as all the migrated projects need to refer to PIB Brief Stage.
            if(project.getIsMigrated())
            {
            	// The migrated closed project should get opened on Closed stage only.
            	if(project.getStatus().intValue()==SynchroGlobal.ProjectStatusNew.CLOSE.ordinal())
            	{
            		
            	}
            	else
            	{
	            	// If the project is saved as part of new Synchro once it is migrated then it should go to corresponding stage, otherwise it should go to PIB stage
	            	if(project.getHasNewSynchroSaved()!=null && project.getHasNewSynchroSaved())
	            	{
	            		
	            	}
	            	else if(project.getFieldWorkStudy()!=null && project.getFieldWorkStudy()==1)
	            	{
	            		urlBuilder = new StringBuilder();
	            		urlBuilder.append("/new-synchro/proposal-details-fieldwork!input.jspa").append("?projectID=").append(project.getProjectID());
	            	}
	            	else
	            	{
		            	urlBuilder = new StringBuilder();
		            	urlBuilder.append("/new-synchro/pib-details!input.jspa").append("?projectID=").append(project.getProjectID());
	            	}
            	}
            }
            
        }
        return urlBuilder.toString();
    }

    public static String generateURL(final Long projectID, final Integer stageNumber) {
        Project project = getSynchroProjectManager().get(projectID);
        return generateURL(project, stageNumber);
    }

    public static Integer getCurrentStageNumber(final Project project) {
        if(currentStageNumber == null || currentProject == null || currentProject != project) {
            currentProject = project;
            currentStageNumber = -1;

            // Check if PIB started
            List<ProjectInitiation> projectInitiations = getPibManager().getPIBDetails(project.getProjectID());
            if(projectInitiations != null && projectInitiations.size() > 0) {
                if(projectInitiations.get(0).getStatus() != null
                        && projectInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.PIB_STARTED.ordinal()
                        && projectInitiations.get(0).getStatus() <= SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal()) {
                    currentStageNumber = 2;
                }
            } else if(project != null) {
                currentStageNumber = 2;
            }

            // Check If proposal started
            List<ProposalInitiation> proposalInitiations = getProposalManager().getProposalDetails(project.getProjectID());
            if(proposalInitiations != null && proposalInitiations.size() > 0
                    && proposalInitiations.get(0).getStatus() != null
                    && proposalInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal()
                    && proposalInitiations.get(0).getStatus() <= SynchroGlobal.StageStatus.PROPOASL_AWARDED.ordinal()) {
                currentStageNumber = 3;
            }

            List<ProjectSpecsInitiation> projectSpecsInitiations = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID());
            if(projectSpecsInitiations != null && projectSpecsInitiations.size() > 0
                    && projectSpecsInitiations.get(0).getStatus() != null
                    && projectSpecsInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal()
                    && projectSpecsInitiations.get(0).getStatus() <= SynchroGlobal.StageStatus.PROJECT_SPECS_COMPLETED.ordinal()) {
                currentStageNumber = 4;
            }
            
            // This is done for enabling the Project Specs Stage enabled in case the Admin user has clicked on PARTIAL APPROVAL/Req for Clarification 
            // button once the Project Specs is APPROVED and Report Summary is enabled.
            boolean isProjectSpecsStageEnabled = false;
            if(project.getMultiMarket())
            {
            	List<ProjectSpecsInitiation> projectSpecsInitiationAM = getProjectSpecsManager().getProjectSpecsInitiation(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            	if(projectSpecsInitiationAM != null && projectSpecsInitiationAM.size() > 0  && projectSpecsInitiationAM.get(0).getIsApproved()!=null
            			&& projectSpecsInitiationAM.get(0).getIsApproved()>0)
            	{
            		isProjectSpecsStageEnabled = true;
            	}
            }
            else
            {
            	if(projectSpecsInitiations != null && projectSpecsInitiations.size() > 0  && projectSpecsInitiations.get(0).getIsApproved()!=null
            			&& projectSpecsInitiations.get(0).getIsApproved()>0)
            	{
            		isProjectSpecsStageEnabled = true;
            	}
            }
            
            List<ReportSummaryInitiation> reportSummaryInitiations = getReportSummaryManager().getReportSummaryInitiation(project.getProjectID());
            if(reportSummaryInitiations != null && reportSummaryInitiations.size() > 0
                    && reportSummaryInitiations.get(0).getStatus() != null && isProjectSpecsStageEnabled) {
                if(reportSummaryInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal()
                        && reportSummaryInitiations.get(0).getStatus() < SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal()) {
                    currentStageNumber = 5;
                } else if(reportSummaryInitiations.get(0).getStatus() >= SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal()) {
                    currentStageNumber = 6;
                }

            }

        }

        return currentStageNumber;
    }


    public static Integer getCurrentStageNumber(final Long projectID) {
        Project project = getSynchroProjectManager().get(projectID);
        return getCurrentStageNumber(project);
    }

    public ProjectStage(Integer stageNumber, String stageName, String stageURL,
                        String description, boolean currentStage, boolean hasAccess, boolean projectEvaluationStage,
                        boolean projectEvaluationCompleted) {
        this.stageNumber = stageNumber;
        this.stageName = stageName;
        this.stageURL = stageURL;
        this.description = description;
        this.isCurrentStage = currentStage;
        this.hasAccess = hasAccess;
        this.isProjectEvaluationStage = projectEvaluationStage;
        this.isProjectEvaluationCompleted = projectEvaluationCompleted;
    }

    public Integer getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(Integer stageNumber) {
        this.stageNumber = stageNumber;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStageURL() {
        return stageURL;
    }

    public void setStageURL(String stageURL) {
        this.stageURL = stageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCurrentStage() {
        return isCurrentStage;
    }

    public void setCurrentStage(boolean currentStage) {
        isCurrentStage = currentStage;
    }

    public boolean isHasAccess() {
        return hasAccess;
    }

    public void setHasAccess(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public boolean isProjectEvaluationStage() {
        return isProjectEvaluationStage;
    }

    public void setProjectEvaluationStage(boolean projectEvaluationStage) {
        isProjectEvaluationStage = projectEvaluationStage;
    }

    public boolean isProjectEvaluationCompleted() {
        return isProjectEvaluationCompleted;
    }

    public void setProjectEvaluationCompleted(boolean projectEvaluationCompleted) {
        isProjectEvaluationCompleted = projectEvaluationCompleted;
    }


    public boolean isProjectCompleted() {
        return isProjectCompleted;
    }

    public void setProjectCompleted(boolean projectCompleted) {
        isProjectCompleted = projectCompleted;
    }

    public static ProposalManager getProposalManager() {
        if(proposalManager == null) {
            return JiveApplication.getContext().getSpringBean("proposalManager");
        }
        return proposalManager;
    }


    public static SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    public static PIBManager getPibManager() {
        if(pibManager == null) {
            return JiveApplication.getContext().getSpringBean("pibManager");
        }
        return pibManager;

    }
    public static ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null) {
            return JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;

    }

    public static ProjectSpecsManager getProjectSpecsManager() {
        if(projectSpecsManager == null) {
            return JiveApplication.getContext().getSpringBean("projectSpecsManager");
        }
        return projectSpecsManager;

    }
    public static ReportSummaryManager getReportSummaryManager() {
        if(reportSummaryManager == null) {
            return JiveApplication.getContext().getSpringBean("reportSummaryManager");
        }
        return reportSummaryManager;

    }
    public static ProjectEvaluationManager getProjectEvaluationManager() {
        if(projectEvaluationManager == null) {
            return JiveApplication.getContext().getSpringBean("projectEvaluationManager");
        }
        return projectEvaluationManager;
    }

    public enum StageType {
        PIT(1, "PIT"),
        PIB(2, "PIB"),
        PROPOSAL(3, "Proposal"),
        PROJECT_SPECS(4, "Project Specs"),
        REPORT(5, "Report"),
        PROJECT_EVALUATION(6, "Project Evaluation");

        Integer number;
        String stageName;

        private StageType(Integer number, String stageName) {
            this.number = number;
            this.stageName = stageName;
        }

        public Integer getStageNumber() {
            return this.number;
        }

        public String getStageName() {
            return this.stageName;
        }

        @Override
        public String toString() {
            return this.stageName;
        }
    }
}
