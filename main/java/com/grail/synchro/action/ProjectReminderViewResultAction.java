package com.grail.synchro.action;

import com.grail.beans.GrailBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.manager.KantarBriefTemplateManager;
import com.grail.manager.GrailBriefTemplateManager;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectPendingActivityViewBean;
import com.grail.synchro.beans.ProjectReminderBean;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectWaiverManager;
import com.grail.synchro.manager.SynchroReminderManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroReminderUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/6/15
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectReminderViewResultAction extends JiveActionSupport {
    private SynchroReminderManager synchroReminderManager;

    private ProjectManager synchroProjectManager;
    private ProjectWaiverManager projectWaiverManager;
    private GrailBriefTemplateManager grailBriefTemplateManager;
    private KantarBriefTemplateManager kantarBriefTemplateManager;

    private ProjectReminderBean projectReminder;
    private List<Project> draftProjects;
    List<ProjectPendingActivityViewBean> projectPendingActivities;
    List<ProjectWaiver> projectWaivers;
    List<GrailBriefTemplate> grailBriefTemplates;
    List<KantarBriefTemplate> kantarBriefTemplates;


    @Override
    public String input() {
        if(request.getParameter("id") != null) {
            projectReminder = synchroReminderManager.getProjectReminder(Long.parseLong(request.getParameter("id")));
            if(projectReminder != null) {
                if(projectReminder.getCategoryTypes() != null && projectReminder.getCategoryTypes().size() > 0) {
                    if(projectReminder.getCategoryTypes().contains(new Long(SynchroGlobal.ProjectReminderCategoryType.DRAFT_PROJECT.getId()))) {
                        draftProjects = getSynchroProjectManager().getProjects(SynchroReminderUtils.getDraftProjectsSearchFilter(getUser()));
                    }
                    if(projectReminder.getCategoryTypes().contains(new Long(SynchroGlobal.ProjectReminderCategoryType.PROJECT_PENDING_ACTIVITY.getId()))) {
                        projectPendingActivities = getSynchroProjectManager().getPendingActivities(SynchroReminderUtils.getPendingActivitySearchFilter(), getUser().getID());
                    }
                    if(projectReminder.getCategoryTypes().contains(new Long(SynchroGlobal.ProjectReminderCategoryType.WAIVERS.getId()))) {
                        projectWaivers = getProjectWaiverManager().getPendingApprovalWaivers(getUser(), new ProjectResultFilter());
                    }
                    if(projectReminder.getCategoryTypes().contains(new Long(SynchroGlobal.ProjectReminderCategoryType.GRAIL_PENDING_ACTIVITY.getId()))) {
                        grailBriefTemplates = getGrailBriefTemplateManager().getPendingActivities(getUser().getID());
                    }
                    if(projectReminder.getCategoryTypes().contains(new Long(SynchroGlobal.ProjectReminderCategoryType.KANTAR_PENDING_ACTIVITY.getId()))) {
                        kantarBriefTemplates = getKantarBriefTemplateManager().getPendingActivities(getUser().getID());
                    }
                }
            }
        }
        return INPUT;
    }

    @Override
    public String execute() {
        return SUCCESS;
    }

    public SynchroReminderManager getSynchroReminderManager() {
        return synchroReminderManager;
    }

    public void setSynchroReminderManager(SynchroReminderManager synchroReminderManager) {
        this.synchroReminderManager = synchroReminderManager;
    }

    public ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null) {
            synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;
    }

    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        synchroProjectManager = synchroProjectManager;
    }

    public ProjectWaiverManager getProjectWaiverManager() {
        if(projectWaiverManager == null) {
            projectWaiverManager = JiveApplication.getContext().getSpringBean("projectWaiverManager");
        }
        return projectWaiverManager;
    }

    public void setProjectWaiverManager(ProjectWaiverManager projectWaiverManager) {
        projectWaiverManager = projectWaiverManager;
    }

    public GrailBriefTemplateManager getGrailBriefTemplateManager() {
        if(grailBriefTemplateManager == null) {
            grailBriefTemplateManager = JiveApplication.getContext().getSpringBean("grailBriefTemplateManager");
        }
        return grailBriefTemplateManager;
    }

    public void setGrailBriefTemplateManager(GrailBriefTemplateManager grailBriefTemplateManager) {
        grailBriefTemplateManager = grailBriefTemplateManager;
    }

    public KantarBriefTemplateManager getKantarBriefTemplateManager() {
        if(kantarBriefTemplateManager == null) {
            kantarBriefTemplateManager = JiveApplication.getContext().getSpringBean("kantarBriefTemplateManager");
        }
        return kantarBriefTemplateManager;
    }

    public void setKantarBriefTemplateManager(KantarBriefTemplateManager kantarBriefTemplateManager) {
        kantarBriefTemplateManager = kantarBriefTemplateManager;
    }

    public ProjectReminderBean getProjectReminder() {
        return projectReminder;
    }

    public void setProjectReminder(ProjectReminderBean projectReminder) {
        this.projectReminder = projectReminder;
    }

    public List<Project> getDraftProjects() {
        return draftProjects;
    }

    public void setDraftProjects(List<Project> draftProjects) {
        this.draftProjects = draftProjects;
    }

    public List<ProjectPendingActivityViewBean> getProjectPendingActivities() {
        return projectPendingActivities;
    }

    public void setProjectPendingActivities(List<ProjectPendingActivityViewBean> projectPendingActivities) {
        this.projectPendingActivities = projectPendingActivities;
    }

    public List<ProjectWaiver> getProjectWaivers() {
        return projectWaivers;
    }

    public void setProjectWaivers(List<ProjectWaiver> projectWaivers) {
        this.projectWaivers = projectWaivers;
    }

    public List<GrailBriefTemplate> getGrailBriefTemplates() {
        return grailBriefTemplates;
    }

    public void setGrailBriefTemplates(List<GrailBriefTemplate> grailBriefTemplates) {
        this.grailBriefTemplates = grailBriefTemplates;
    }

    public List<KantarBriefTemplate> getKantarBriefTemplates() {
        return kantarBriefTemplates;
    }

    public void setKantarBriefTemplates(List<KantarBriefTemplate> kantarBriefTemplates) {
        this.kantarBriefTemplates = kantarBriefTemplates;
    }
}
