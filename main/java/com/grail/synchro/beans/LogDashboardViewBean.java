package com.grail.synchro.beans;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtilsNew;



public class LogDashboardViewBean extends BeanObject {
   
	private String timestamp;
	private String username;
	private String portalname;
	private String page;
	private String activity;
	private List<String> descriptions;
	private Long projectID;
	private String projectName;
	private Long waiverID;
	
	public static LogDashboardViewBean toLogDashboardViewBean(final ActivityLog activityLog)
	{
		LogDashboardViewBean bean = new LogDashboardViewBean();
		bean.setTimestamp(activityLog.getTimestamp()+"");
		bean.setUsername(activityLog.getUserName());
		bean.setPortalname(activityLog.getPortal());
		
		Date date = new Date(activityLog.getTimestamp());
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");		
		bean.setTimestamp(formatter.format(date));
		
		if(activityLog.getPage()!=null && activityLog.getPage() >0)
		{
			bean.setPage(SynchroGlobal.PageType.getById(activityLog.getPage()).getDescription());						
		}
		else
		{
			bean.setPage("");
		}

		if(activityLog.getType()!=null && activityLog.getType() >0)
		{
			bean.setActivity(SynchroGlobal.Activity.getById(activityLog.getType()).getDescription());						
		}
		else
		{
			bean.setActivity("");
		}

		bean.setDescriptions(SynchroLogUtilsNew.parseJSONToList(activityLog.getJsonValue(), activityLog.getStage(), activityLog.getType()));
		if(activityLog.getProjectID()!=null && activityLog.getProjectID()>0)
		{
			bean.setProjectID(activityLog.getProjectID());
		}
		
		if(activityLog.getWaiverID()!=null && activityLog.getWaiverID()>0)
		{
			bean.setWaiverID(activityLog.getWaiverID());
		}
		
		bean.setProjectName(activityLog.getProjectName());
		
		return bean;
		
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPortalname() {
		return portalname;
	}
	public void setPortalname(String portalname) {
		this.portalname = portalname;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	
	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public List<String> getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Long getWaiverID() {
		return waiverID;
	}

	public void setWaiverID(Long waiverID) {
		this.waiverID = waiverID;
	}
	
	
}
