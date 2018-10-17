package com.grail.synchro.scheduling.quartz;

import com.grail.synchro.exceptions.ProjectCostCaptureUpdateProcessException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 12:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCostsCaptureJob extends QuartzJobBean {

    private ProjectCostsCaptureTask projectCostsCaptureTask;

    public ProjectCostsCaptureTask getProjectCostsCaptureTask() {
        return projectCostsCaptureTask;
    }

    public void setProjectCostsCaptureTask(ProjectCostsCaptureTask projectCostsCaptureTask) {
        this.projectCostsCaptureTask = projectCostsCaptureTask;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            this.projectCostsCaptureTask.captureCosts();
        } catch (ProjectCostCaptureUpdateProcessException e) {
            e.printStackTrace();
        }
    }
}
