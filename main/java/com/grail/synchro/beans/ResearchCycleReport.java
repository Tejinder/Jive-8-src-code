package com.grail.synchro.beans;

import java.util.List;

public class ResearchCycleReport {
	 private Long projectID;
	 private String name;
	 private Long ownerID;
	 private Integer status;
	 private Integer startMonth;
	 private Integer startYear;
	 private Integer endMonth;
	 private Integer endYear;
	
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getOwnerID() {
		return ownerID;
	}
	public void setOwnerID(Long ownerID) {
		this.ownerID = ownerID;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getStartMonth() {
		return startMonth;
	}
	public void setStartMonth(Integer startMonth) {
		this.startMonth = startMonth;
	}
	public Integer getStartYear() {
		return startYear;
	}
	public void setStartYear(Integer startYear) {
		this.startYear = startYear;
	}
	public Integer getEndMonth() {
		return endMonth;
	}
	public void setEndMonth(Integer endMonth) {
		this.endMonth = endMonth;
	}
	public Integer getEndYear() {
		return endYear;
	}
	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}

}
