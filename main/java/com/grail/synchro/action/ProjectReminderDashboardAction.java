package com.grail.synchro.action;

import com.grail.synchro.beans.ProjectReminderBean;
import com.grail.synchro.manager.SynchroReminderManager;
import com.jivesoftware.community.action.JiveActionSupport;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/6/15
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectReminderDashboardAction extends JiveActionSupport {
    private SynchroReminderManager synchroReminderManager;

    private InputStream getProjectReminderStatus;
    private static final String GET_PROJECT_REMINDER_RESPONSE = "addDocResponse";

    private ProjectReminderBean projectReminder;

    @Override
    public String input() {
        return INPUT;
    }

    @Override
    public String execute() {
        return SUCCESS;
    }

    public String showProjectReminderPopup() {
        if(request.getParameter("reminderId") != null) {
            projectReminder = synchroReminderManager.getProjectReminder(Long.parseLong(request.getParameter("reminderId")));
        }
        return GET_PROJECT_REMINDER_RESPONSE;
    }

    public SynchroReminderManager getSynchroReminderManager() {
        return synchroReminderManager;
    }

    public void setSynchroReminderManager(SynchroReminderManager synchroReminderManager) {
        this.synchroReminderManager = synchroReminderManager;
    }

    public ProjectReminderBean getProjectReminder() {
        return projectReminder;
    }

    public void setProjectReminder(ProjectReminderBean projectReminder) {
        this.projectReminder = projectReminder;
    }

    public InputStream getGetProjectReminderStatus() {
        return getProjectReminderStatus;
    }

    public void setGetProjectReminderStatus(InputStream getProjectReminderStatus) {
        this.getProjectReminderStatus = getProjectReminderStatus;
    }
}
