package com.grail.synchro.beans;

import com.grail.synchro.SynchroGlobal;

/**
 * @author Kanwar Grewal
 * @version 4.0, Date: 12/03/13
 */
public class ProjectStatus extends BeanObject {

	private Long projectID;
	private Long endMarketID;
	private Long status;
	
	public ProjectStatus()
	{
		   
	}
	
	public ProjectStatus(final Long projectID, final Long endMarketID)
	{
		   final Long currentTime = System.currentTimeMillis();
	        this.projectID = projectID;
	        this.endMarketID = endMarketID;
	        this.status=new Long(SynchroGlobal.Status.PIT_OPEN.ordinal());
	        setCreationDate(currentTime);
	        setModifiedDate(currentTime);
	}
	
	public ProjectStatus(final Long projectID, final Long endMarketID, final Long Status)
	{
		   final Long currentTime = System.currentTimeMillis();
	        this.projectID = projectID;
	        this.endMarketID = endMarketID;
	        this.status=new Long(status);
	        setCreationDate(currentTime);
	        setModifiedDate(currentTime);
	}
	
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getEndMarketID() {
		return endMarketID;
	}
	public void setEndMarketID(Long endMarketID) {
		this.endMarketID = endMarketID;
	}
	public Long getStatus() {
		return status;
	}
	public void setStatus(Long status) {
		this.status = status;
	}
	
}
