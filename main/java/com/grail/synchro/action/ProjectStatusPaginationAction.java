package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectGraphBar;
import com.grail.synchro.beans.ProjectStatusDashboardObject;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;

@Decorate(false)
public class ProjectStatusPaginationAction extends JiveActionSupport{ 
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ProjectStatusPaginationAction.class);
    private List<ProjectStatusDashboardObject> projectsByPage = new ArrayList<ProjectStatusDashboardObject>();
	private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages;
	private Integer start;
    private Integer end;
    private List<ProjectStatusDashboardObject> projectStatusDashboardObjects;
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private ProjectManager synchroProjectManager;
    
    private List<Project> projects;
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private String sortField;
    private Integer ascendingOrder;
    
    public String getSortField() {
		return sortField;
	}

	public Integer getAscendingOrder() {
		return ascendingOrder;
	}

	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
		this.synchroProjectManager = synchroProjectManager;
	}

	public Integer getPages() {
		return pages;
	}
    
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
    
    public List<ProjectStatusDashboardObject> getProjectsByPage() {
		return projectsByPage;
	}

	public void setPages(Integer pages) {
		this.pages = pages;
	}

	public void setResults(Integer results) {
		this.results = results;
	}

	public Integer getResults() {
		return results;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public ProjectGraphBar getProjectGraphBar() {
		return projectGraphBar;
	}

	private ProjectGraphBar projectGraphBar;

	public String execute()
	{
		// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	
    	ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());
		if(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin())
		{
			projects = synchroProjectManager.getProjects(projectResultFilter);
		}
		else
		{
			projects = synchroProjectManager.getProjectsByUserAndResultFilter(getUser().getID(), projectResultFilter);
		}
		
		if(projects != null && projects.size() > 0)
		{
			results = projects.size();
			projectStatusDashboardObjects =  getProjectPaginationObjectListBean(projects);
			results=0;
			pages=0;
			 if(projectStatusDashboardObjects != null && keyword != null && !keyword.trim().equals(""))
			 {
				 projectsByPage = searchProjectList(projectStatusDashboardObjects);
				 projectsByPage = sortObjectList(projectsByPage);
				 if(projectsByPage != null && projectsByPage.size() > 0)
				 {
					 results = projectsByPage.size();
				     pages = results%pageLimit==0?results/pageLimit:results/pageLimit+1;
				     setPaginationFilter(page,results);
					 projectsByPage = projectsByPage.subList(start, end);
				 }
			 }
			 
			 else if(projectStatusDashboardObjects != null && (keyword == null || keyword.trim().equals("")))
			 {
				 projectStatusDashboardObjects = sortObjectList(projectStatusDashboardObjects);
				 results = projectStatusDashboardObjects.size();
			     pages = results%pageLimit==0?results/pageLimit:results/pageLimit+1;
			     setPaginationFilter(page,results);
				 projectsByPage = projectStatusDashboardObjects.subList(start, end);
			 }
		}
		return SUCCESS;
	}
	
	private void setPaginationFilter(int page,int results) {
		 start = (this.page-1)*pageLimit;
		 end = start + pageLimit;
		 end = end>=this.results?this.results:end;
	}

	private List<ProjectStatusDashboardObject>  searchProjectList(List<ProjectStatusDashboardObject>  projectPaginationObjects)
	{
		List<ProjectStatusDashboardObject> projects = new ArrayList<ProjectStatusDashboardObject> ();
		for(ProjectStatusDashboardObject projectbean : projectPaginationObjects)
		{
			if((projectbean.getProjectID().toString()).contains(keyword) || projectbean.getProjectName().toUpperCase().contains(keyword.toUpperCase()) || projectbean.getOwner().toUpperCase().contains(keyword.toUpperCase()) || projectbean.getStatus().toUpperCase().contains(keyword.toUpperCase()) || projectbean.getStartYear().toUpperCase().contains(keyword.toUpperCase()) || projectbean.getRegion().toUpperCase().contains(keyword.toUpperCase()) || projectbean.getCountry().toUpperCase().contains(keyword.toUpperCase()) || projectbean.getBrand().toUpperCase().contains(keyword.toUpperCase()) || projectbean.getSupplierGroup().toUpperCase().contains(keyword.toUpperCase()))
			{
				projects.add(projectbean);
			}
		}
		return projects;
		
	}
	
	public List<ProjectStatusDashboardObject> getProjectPaginationObjectListBean(List<Project> projects)
    {
    	List<ProjectStatusDashboardObject> projectStatusDashboardObjects = new ArrayList<ProjectStatusDashboardObject>();
    	/*
		for(Project project : projects)
		{
			if(!SynchroGlobal.Status.getName(project.getStatus()).equalsIgnoreCase("DRAFT") && SynchroPermHelper.canChangeProjectStatusDashboard(getUser(), project.getProjectID()))
			 {
				if(projectResultFilter.getStartYear().intValue() == -1 || project.getStartYear().intValue() == projectResultFilter.getStartYear().intValue())
				{
					try{
						ProjectStatusDashboardObject projectStatusDashboardObject = new ProjectStatusDashboardObject();
						projectStatusDashboardObject.setProjectID(project.getProjectID());
						projectStatusDashboardObject.setProjectName(project.getName());
						projectStatusDashboardObject.setStartYear(project.getStartYear().toString());
						//Getting End MarketList for project 
						List<Long> endMarkets = synchroProjectManager.getEndMarketIDs(project.getProjectID());
						//coordinationDetailsManager.get
						List<Integer> coSuppliers = coordinationDetailsManager.getCoordinationSupplierGroupList(project.getProjectID());
						List<Integer> fwSuppliers = coordinationDetailsManager.getFieldWorkSupplierGroupList(project.getProjectID());
						projectStatusDashboardObject.setRegion(StringUtils.join(SynchroUtils.getRegionsList(endMarkets), ", "));					
						projectStatusDashboardObject.setCountry(StringUtils.join(SynchroUtils.getEndMarketsList(endMarkets), ", "));
						projectStatusDashboardObject.setBrand(SynchroGlobal.getBrands(false, project.getProductType()).get(Integer.parseInt(project.getBrand().toString())));
						List<String> supplierGroups = SynchroUtils.getUniqueSupplierList(coSuppliers, fwSuppliers);
						if(supplierGroups.size() > 0)
						{
							projectStatusDashboardObject.setSupplierGroup(StringUtils.join(supplierGroups, ", "));
						}
						else
						{
							projectStatusDashboardObject.setSupplierGroup("");
						}
						
						projectStatusDashboardObject.setStatus(SynchroGlobal.getProjectStatusNames().get(SynchroGlobal.Status.getName(project.getStatus())));
						if(project.getOwnerID() > 0)
						{
							projectStatusDashboardObject.setOwner(userManager.getUser(project.getOwnerID()).getName());
						}
						else
						{
							projectStatusDashboardObject.setOwner("Anonymous");
						}
						projectStatusDashboardObjects.add(projectStatusDashboardObject);
					}catch(Exception e){LOGGER.error("Error while getting project details for project  All  "+project.getProjectID()+" Error: " +e.getMessage());}
			 }
			}
		}
		*/
		return projectStatusDashboardObjects;
    }
	
	private List<ProjectStatusDashboardObject> sortObjectList(List<ProjectStatusDashboardObject> list)
	{		
		if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().trim().equals(""))
		{
			if(projectResultFilter.getSortField().equals("id"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByID());
				}
				else
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByIDDesc());
				}
				
			}
			else if(projectResultFilter.getSortField().equals("name"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByName());
				}
				else
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByNameDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("owner"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByOwner());
				}
				else
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByOwnerDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("year"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByYear());
				}
				else
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByYearDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("brand"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByBrand());
				}
				else
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByBrandDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("status"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByStatus());
				}
				else
				{
					Collections.sort(list, new ProjectStatusDashboardObject.OrderByStatusDesc());
				}
			}

			sortField = projectResultFilter.getSortField();
			ascendingOrder = projectResultFilter.getAscendingOrder();
		}
		
		return list;
	}
}
