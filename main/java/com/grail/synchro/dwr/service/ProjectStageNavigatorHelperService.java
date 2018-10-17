package com.grail.synchro.dwr.service;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.*;
import com.grail.synchro.manager.*;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class ProjectStageNavigatorHelperService extends RemoteSupport {
    private static final Logger LOG = Logger.getLogger(ProjectStageNavigatorHelperService.class);
    private static ProposalManager proposalManager;
    private static ProjectManager synchroProjectManager;
    private SynchroUtils synchroUtils;
    private PIBManager pibManager;
    private ProjectSpecsManager projectSpecsManager;
    private ReportSummaryManager reportSummaryManager;
    private ProjectEvaluationManager projectEvaluationManager;

    public boolean hasAccess(final Long projectId, final Integer stageId) {
        boolean bool = SynchroPermHelper.canViewStage(projectId, stageId);
        return bool;
    }

    public boolean hasProposalAwardedAccess(final Long projectId, final Integer stageId) {
        boolean bool = false;
        List<ProposalInitiation> proposalInitiation = getProposalManager().getProposalDetails(projectId);
        if(proposalInitiation.size() > 0 && proposalInitiation.get(proposalInitiation.size() - 1).getIsAwarded()
                && (proposalInitiation.get(proposalInitiation.size() - 1).getStatus() >= SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal())) {
            bool = true;
        }
        return bool;
    }
    public boolean canChangeProjectStatus(final Long projectId) {
        boolean bool = SynchroPermHelper.canEditproject(SynchroPermHelper.getEffectiveUser(), projectId);
        return bool;
    }

    public List<ProjectStage> getStages(final Long projectID) {
        List<ProjectStage> projectStages = new ArrayList<ProjectStage>();
        Project project = getSynchroProjectManager().get(projectID);
        if(project != null) {
            // Stage 1
            projectStages.add(new ProjectStage(project, ProjectStage.StageType.PIT, false));

            // Stage 2
            projectStages.add(new ProjectStage(project, ProjectStage.StageType.PIB, true));

            // Stage 3
            projectStages.add(new ProjectStage(project, ProjectStage.StageType.PROPOSAL,
                    SynchroPermHelper.canViewStage(projectID, 2)));

            // Stage 4
            projectStages.add(new ProjectStage(project, ProjectStage.StageType.PROJECT_SPECS,
                    SynchroPermHelper.canViewStage(projectID, 3)));

            // Stage 5
            projectStages.add(new ProjectStage(project, ProjectStage.StageType.REPORT,
                    SynchroPermHelper.canViewStage(projectID,4)));

            // Stage 6
            ProjectStage projEvalStage = new ProjectStage(project,
                    ProjectStage.StageType.PROJECT_EVALUATION, SynchroPermHelper.canViewStage(projectID, 5));
            projEvalStage.setProjectEvaluationStage(true);
            projEvalStage.setProjectEvaluationCompleted(isProjectEvaluationCompleted(project));
            projectStages.add(projEvalStage);
        }

        return projectStages;
    }

    private boolean isProjectEvaluationCompleted(final Project project) {
        boolean completed = false;
        if(project.getStatus().equals(SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal())) {
            completed = true;
        }
//        // Check if project evaluation completed
//        List<ProjectEvaluationInitiation> projectEvaluationInitiations = getProjectEvaluationManager().getProjectEvaluationInitiation(projectID);
////        if(projectEvaluationInitiations != null && projectEvaluationInitiations.size() > 0 &&
////                projectEvaluationInitiations.get(0).getAgencyPerfIM() != null && projectEvaluationInitiations.get(0).getAgencyPerfIM()>0
////                && projectEvaluationInitiations.get(0).getAgencyPerfLM()!=null && projectEvaluationInitiations.get(0).getAgencyPerfLM()>0) {
////            completed = true;
////        }
//
//        if(projectEvaluationInitiations != null && projectEvaluationInitiations.size() > 0
//                && (StringUtils.isNotEmpty(projectEvaluationInitiations.get(0).getBatCommentsIM()) ||
//                StringUtils.isNotEmpty(projectEvaluationInitiations.get(0).getBatCommentsLM()) ||
//                (projectEvaluationInitiations.get(0).getAgencyPerfIM() != null && projectEvaluationInitiations.get(0).getAgencyPerfIM() > 0) ||
//                StringUtils.isNotEmpty(projectEvaluationInitiations.get(0).getAgencyCommentsIM()) ||
//                StringUtils.isNotEmpty(projectEvaluationInitiations.get(0).getAgencyCommentsLM()) ||
//                (projectEvaluationInitiations.get(0).getAgencyPerfLM() != null && projectEvaluationInitiations.get(0).getAgencyPerfLM() > 0)
//                )) {
//            completed = true;
//        }
        return completed;
    }


    public ProposalManager getProposalManager() {
        if(proposalManager == null) {
            return JiveApplication.getContext().getSpringBean("proposalManager");
        }
        return proposalManager;
    }

    public ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null) {
            return JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;

    }

    public ProjectEvaluationManager getProjectEvaluationManager() {
        if(projectEvaluationManager == null) {
            return JiveApplication.getContext().getSpringBean("projectEvaluationManager");
        }
        return projectEvaluationManager;
    }

}
