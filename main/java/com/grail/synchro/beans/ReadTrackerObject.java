package com.grail.synchro.beans;
/**
 * @author Kanwar
 * @version 1.0, Date: 08/03/13
 */

public class ReadTrackerObject {
	
	private Long projectID;
	private Long userID;
	private Long readDate;
	private Long stageID;
	
	
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getUserID() {
		return userID;
	}
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	public Long getReadDate() {
		return readDate;
	}
	public void setReadDate(Long readDate) {
		this.readDate = readDate;
	}
	public Long getStageID() {
		return stageID;
	}
	public void setStageID(Long stageID) {
		this.stageID = stageID;
	}

}
