package com.grail.synchro.search.filter;


import java.util.Date;
import java.util.List;

import com.jivesoftware.base.User;

public class LogResultFilter {
	
	private Date startDateLog;
	private Date endDateLog;
    private List<Long> portalFields;
    private List<Long> pageFields;
    private List<Long> activityFields;
    private String keyword;
    private Integer start;
    private Integer limit;
    private String sortField;
    private Integer ascendingOrder;
    private User user;
    private String name;
    private Long projectID;
    private Long waiverID;
	

	public Date getStartDateLog() {
		return startDateLog;
	}
	public void setStartDateLog(Date startDateLog) {
		this.startDateLog = startDateLog;
	}
	public Date getEndDateLog() {
		return endDateLog;
	}
	public void setEndDateLog(Date endDateLog) {
		this.endDateLog = endDateLog;
	}
	public List<Long> getPortalFields() {
		return portalFields;
	}
	public void setPortalFields(List<Long> portalFields) {
		this.portalFields = portalFields;
	}
	public List<Long> getPageFields() {
		return pageFields;
	}
	public void setPageFields(List<Long> pageFields) {
		this.pageFields = pageFields;
	}
	public List<Long> getActivityFields() {
		return activityFields;
	}
	public void setActivityFields(List<Long> activityFields) {
		this.activityFields = activityFields;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	public Integer getLimit() {
		return limit;
	}
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}
	public Integer getAscendingOrder() {
		return ascendingOrder;
	}
	public void setAscendingOrder(Integer ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getProjectID() {
		return projectID;
	}
	
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getWaiverID() {
		return waiverID;
	}
	public void setWaiverID(Long waiverID) {
		this.waiverID = waiverID;
	}
	
}
