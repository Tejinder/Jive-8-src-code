package com.grail.synchro.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map; 
import java.util.Date;

import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class ProjectPendingActivityViewBean implements Comparable<ProjectPendingActivityViewBean> {
	private Long projectID;
	private String projectName;
    private String owner; 
    private String startYear;
    private String country;
    private String brand;   
    private String status;
    private String pendingActivity;
    private String activityLink;
    private static UserManager userManager;
    private static StageManager stageManager;
    
    private String projectType;
    private String projectManager;
    private String methodology;
    
    private Date startDate;

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
	public String getPendingActivity() {
		return pendingActivity;
	}
	public void setPendingActivity(String pendingActivity) {
		this.pendingActivity = pendingActivity;
	}
	public String getActivityLink() {
		return activityLink;
	}
	public void setActivityLink(String activityLink) {
		this.activityLink = activityLink;
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
	public void setStatus(String status) {
		this.status = status;
	}

    public static ProjectPendingActivityViewBean toProjectPendingActivityViewBean(final Project project) {
        ProjectPendingActivityViewBean bean = new ProjectPendingActivityViewBean();
        bean.setProjectID(project.getProjectID());
        bean.setProjectName(project.getName());
        try {
            bean.setOwner(getUserManager().getUser(project.getProjectOwner()).getName());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        Calendar startDate = Calendar.getInstance();
        startDate.setTime(project.getStartDate());
        bean.setStartYear(String.valueOf(startDate.get(Calendar.YEAR)));

        bean.setBrand(SynchroUtils.getBrandFields().get(Integer.parseInt(project.getBrand().toString())));
        Map<String, String> pendingActivities = getStageManager().getPendingActivity(project);
        for(String key: pendingActivities.keySet())
        {
        	 bean.setPendingActivity(key);
             bean.setActivityLink(pendingActivities.get(key));
        }
        //bean.setActivityLink("TODO");
        //bean.setPendingActivity("TODO");
        return bean;
    }

    public static List<ProjectPendingActivityViewBean> toProjectPendingActivityViewBeans(final List<Project> projects) {
        List<ProjectPendingActivityViewBean> beans = new ArrayList<ProjectPendingActivityViewBean>();
        for(Project project: projects) {
        	ProjectPendingActivityViewBean prActBean =  toProjectPendingActivityViewBean(project);
        	if(prActBean.getActivityLink()!=null && prActBean.getPendingActivity()!=null
        			&& !prActBean.getActivityLink().equals("") && !prActBean.getPendingActivity().equals(""))
        	{
        		beans.add(prActBean);
        	}
        }
        return beans;
    }

    public static UserManager getUserManager() {
        if(userManager == null){
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }
    public static StageManager getStageManager() {
        if(stageManager == null){
        	stageManager = JiveApplication.getContext().getSpringBean("stageManager");
        }
        return stageManager;
    }

	/*
     * Comparator implementation to Sort Order object based on Project ID
     */
    public static class OrderByID implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o1.projectID > o2.projectID ? 1 : (o1.projectID < o2.projectID ? -1 : 0);
        }
    }
    
    public static class OrderByIDDesc implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o2.projectID > o1.projectID ? 1 : (o2.projectID < o1.projectID ? -1 : 0);
        }
    }

    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project name.
     */
    public static class OrderByName implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o1.projectName.toLowerCase().compareTo(o2.projectName.toLowerCase());
        }
    }
    
    public static class OrderByNameDesc implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o2.projectName.toLowerCase().compareTo(o1.projectName.toLowerCase());
        }
    }
    
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project owner.
     */
    public static class OrderByOwner implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o1.owner.toLowerCase().compareTo(o2.owner.toLowerCase());
        }
    }
    
    public static class OrderByOwnerDesc implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o2.owner.toLowerCase().compareTo(o1.owner.toLowerCase());
        }
    }
    
    /*
    * Comparator implementation to Sort Order object based on Project Year
    */
    
    public static class OrderByYear implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o1.startYear.compareTo(o2.startYear);
        }
    }
    
    public static class OrderByYearDesc implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o2.startYear.compareTo(o1.startYear);
        }
    }


    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project owner.
     */
    public static class OrderByBrand implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o1.brand.toLowerCase().compareTo(o2.brand.toLowerCase());
        }
    }
    
    public static class OrderByBrandDesc implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o2.brand.toLowerCase().compareTo(o1.brand.toLowerCase());
        }
    }
    
    
   
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project status.
     */
    public static class OrderByActivity implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o1.pendingActivity.toLowerCase().compareTo(o2.pendingActivity.toLowerCase());
        }
    }
    
    public static class OrderByActivityDesc implements Comparator<ProjectPendingActivityViewBean> {

        @Override
        public int compare(ProjectPendingActivityViewBean o1, ProjectPendingActivityViewBean o2) {
            return o2.pendingActivity.toLowerCase().compareTo(o1.pendingActivity.toLowerCase());
        }
    }
    
    
	@Override
	public int compareTo(ProjectPendingActivityViewBean o) {
		return this.projectID > o.projectID ? 1 : (this.projectID < o.projectID ? -1 : 0);
	}
	public String getProjectType() {
		return projectType;
	}
	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}
	public String getProjectManager() {
		return projectManager;
	}
	public void setProjectManager(String projectManager) {
		this.projectManager = projectManager;
	}
	public String getMethodology() {
		return methodology;
	}
	public void setMethodology(String methodology) {
		this.methodology = methodology;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}
