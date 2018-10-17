package com.grail.synchro.rest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.grail.synchro.util.SynchroLogUtils;
import com.jivesoftware.community.dwr.RemoteSupport;

/**
 * RESTful Action related to project Audit Logs
 *
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class SynchroLogUtilAction extends RemoteSupport  {

    private static final Logger LOG = Logger.getLogger(SynchroLogUtilAction.class);   


	public void addLogActivity(final String portalName, final int pageType, final int activityType, final int stage, final String text, String projectName, Long projectID, final Long userID)
	{
		System.out.println("Inside addLogActivity DWR ----");
		if(projectID !=null && userID != null && StringUtils.isNotBlank(text))
			SynchroLogUtils.addLog(portalName, pageType, activityType, stage, text, projectName, projectID, userID);
	}
	
	public void addEMLogActivity(final String portalName, final int pageType, final int activityType, final int stage, final String text, String projectName, Long projectID, final Long userID, final Long endmarketID)
	{
		if(projectID !=null && userID != null && StringUtils.isNotBlank(text))
			SynchroLogUtils.addLog(portalName, pageType, activityType, stage, text, projectName, projectID, userID, endmarketID);
	}
}
