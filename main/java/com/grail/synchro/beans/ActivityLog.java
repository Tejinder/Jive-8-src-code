package com.grail.synchro.beans;

/**
 * @author Kanwar Grewal
 * @version 1.0
 */
public class ActivityLog {
	
	private String userName;
	private Long userID;
	private String portal;
	private Integer page;
	private Integer type;
	private Integer stage;
	private String jsonValue;//stores JSON Value as String variable
	private Long projectID;
	private String projectName;
	private Long endmarketID;
	private Long timestamp;
	private Long waiverID;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPortal() {
		return portal;
	}
	public void setPortal(String portal) {
		this.portal = portal;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	public String getJsonValue() {
		return jsonValue;
	}
	public void setJsonValue(String jsonValue) {
		this.jsonValue = jsonValue;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Long getUserID() {
		return userID;
	}
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Integer getStage() {
		return stage;
	}
	public void setStage(Integer stage) {
		this.stage = stage;
	}
	public Long getEndmarketID() {
		return endmarketID;
	}
	public void setEndmarketID(Long endmarketID) {
		this.endmarketID = endmarketID;
	}
	public Long getWaiverID() {
		return waiverID;
	}
	public void setWaiverID(Long waiverID) {
		this.waiverID = waiverID;
	}
	
	
}
