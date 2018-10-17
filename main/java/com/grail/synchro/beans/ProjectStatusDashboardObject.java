package com.grail.synchro.beans;

import java.util.Comparator;

public class ProjectStatusDashboardObject implements Comparable<ProjectStatusDashboardObject>  {
	private Long projectID;
	private String projectName;
    private String owner; 
    private String startYear;
    private String region;
    private String country;
    private String brand;
    private String supplierGroup;
    private String status;
    
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
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getStatus() {
		return status;
	}
	
	public String getStartYear() {
		return startYear;
	}
	public void setStartYear(String startYear) {
		this.startYear = startYear;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getSupplierGroup() {
		return supplierGroup;
	}
	public void setSupplierGroup(String supplierGroup) {
		this.supplierGroup = supplierGroup;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	/*
     * Comparator implementation to Sort Order object based on Project ID
     */
    public static class OrderByID implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o1.projectID > o2.projectID ? 1 : (o1.projectID < o2.projectID ? -1 : 0);
        }
    }
    
    public static class OrderByIDDesc implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o2.projectID > o1.projectID ? 1 : (o2.projectID < o1.projectID ? -1 : 0);
        }
    }

    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project name.
     */
    public static class OrderByName implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o1.projectName.toLowerCase().compareTo(o2.projectName.toLowerCase());
        }
    }
    
    public static class OrderByNameDesc implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o2.projectName.toLowerCase().compareTo(o1.projectName.toLowerCase());
        }
    }
    
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project owner.
     */
    public static class OrderByOwner implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o1.owner.toLowerCase().compareTo(o2.owner.toLowerCase());
        }
    }
    
    public static class OrderByOwnerDesc implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o2.owner.toLowerCase().compareTo(o1.owner.toLowerCase());
        }
    }
    
    /*
    * Comparator implementation to Sort Order object based on Project Year
    */
    
    public static class OrderByYear implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o1.startYear.compareTo(o2.startYear);
        }
    }
    
    public static class OrderByYearDesc implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o2.startYear.compareTo(o1.startYear);
        }
    }


    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project owner.
     */
    public static class OrderByBrand implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o1.brand.toLowerCase().compareTo(o2.brand.toLowerCase());
        }
    }
    
    public static class OrderByBrandDesc implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o2.brand.toLowerCase().compareTo(o1.brand.toLowerCase());
        }
    }
    
    
   
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project status.
     */
    public static class OrderByStatus implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o1.status.toLowerCase().compareTo(o2.status.toLowerCase());
        }
    }
    
    public static class OrderByStatusDesc implements Comparator<ProjectStatusDashboardObject> {

        @Override
        public int compare(ProjectStatusDashboardObject o1, ProjectStatusDashboardObject o2) {
            return o2.status.toLowerCase().compareTo(o1.status.toLowerCase());
        }
    }
    
    
	@Override
	public int compareTo(ProjectStatusDashboardObject o) {
		return this.projectID > o.projectID ? 1 : (this.projectID < o.projectID ? -1 : 0);
	}
}
