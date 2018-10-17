package com.grail.synchro.dwr.service;

import com.grail.synchro.exceptions.ProjectCostCaptureUpdateProcessException;
import com.grail.synchro.scheduling.quartz.ProjectCostsCaptureTask;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/4/14
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class RawExtractService extends RemoteSupport {

    public Map<String, Object> update() {
        Map<String, Object> status = new HashMap<String, Object>();
        try {
            ProjectCostsCaptureTask projectCostsCaptureTask  = JiveApplication.getContext().getSpringBean("projectCostsCaptureTask");
            projectCostsCaptureTask.captureCosts();
            status.put("success", true);
            status.put("message", "Updated successfully.");
        } catch (ProjectCostCaptureUpdateProcessException e) {
            status.put("success", false);
            status.put("message", e.getMessage());
        }
        status.put("lastUpdated", JiveGlobals.getJiveProperty("synchro.raw.extract.lastupdate"));

        return status;
    }
}
