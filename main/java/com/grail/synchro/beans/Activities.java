package com.grail.synchro.beans;

/**
 * @author Tejinder
 * @version 1.0
 */
public class Activities extends BeanObject {
	private String activityName;
	private String activityURL;
	private String activityStatus;

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getActivityURL() {
		return activityURL;
	}

	public void setActivityURL(String activityURL) {
		this.activityURL = activityURL;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}
}
