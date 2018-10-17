package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectHistory;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.beans.ReadTrackerObject;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ReadTrackerManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;

@Decorate(false)
public class ProjectHistoryPaginationAction extends JiveActionSupport{ 
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ProjectHistoryPaginationAction.class);
    private List<ProjectHistory> projectHistoryByPage = new ArrayList<ProjectHistory>();
	private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages;
	private Integer start;
    private Integer end;
    private List<ProjectHistory> projectHistoryList;
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private ProjectManager synchroProjectManager;
    private ReadTrackerManager synchroReadTrackerManager;
    private List<ReadTrackerObject> readTrackerObjects;
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private String sortField;
    private Integer ascendingOrder;
    
    public String getSortField() {
		return sortField;
	}

	public Integer getAscendingOrder() {
		return ascendingOrder;
	}
    public void setSynchroReadTrackerManager(
			ReadTrackerManager synchroReadTrackerManager) {
		this.synchroReadTrackerManager = synchroReadTrackerManager;
	}
    
	public ReadTrackerManager getSynchroReadTrackerManager() {
		 if (synchroReadTrackerManager == null) 
		 {
			 synchroReadTrackerManager = JiveApplication.getContext().getSpringBean("synchroReadTrackerManager");
	     }
		return synchroReadTrackerManager;
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
    
	public List<ProjectHistory> getProjectHistoryByPage() {
		return projectHistoryByPage;
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

	public String execute()
	{	 
		// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	 
    	 ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
         binder.bind(getRequest());
         
		readTrackerObjects = getSynchroReadTrackerManager().getUserReadInfo(getUser().getID(), projectResultFilter);
		projectHistoryList = getProjectPaginationObjectListBean(readTrackerObjects);
		results=0;
		 pages=0;
		 if(projectHistoryList != null && keyword != null && !keyword.trim().equals(""))
		 {
			 projectHistoryByPage = searchProjectList(projectHistoryList);
			 projectHistoryByPage = sortObjectList(projectHistoryByPage);
			 if(projectHistoryByPage != null && projectHistoryByPage.size() > 0)
			 {
			 results = projectHistoryByPage.size();
		     pages = results%pageLimit==0?results/pageLimit:results/pageLimit+1;
		     setPaginationFilter(page,results);
		     projectHistoryByPage = projectHistoryByPage.subList(start, end);
			 }
		 }
		 
		 else if(projectHistoryList != null && (keyword == null || keyword.trim().equals("")))
		 {
			 projectHistoryList = sortObjectList(projectHistoryList);
			 results = projectHistoryList.size();
		     pages = results%pageLimit==0?results/pageLimit:results/pageLimit+1;
		     setPaginationFilter(page,results);
		    
		     projectHistoryByPage = projectHistoryList.subList(start, end);
		 }
		 return SUCCESS;
		
	}
	
	private void setPaginationFilter(int page,int results) {
		 start = (this.page-1)*pageLimit;
		 end = start + pageLimit;
		 end = end>=this.results?this.results:end;
	}

	private List<ProjectHistory>  searchProjectList(List<ProjectHistory>  projectHistoryList)
	{
		List<ProjectHistory> projects = new ArrayList<ProjectHistory> ();
		for(ProjectHistory projectHistory : projectHistoryList)
		{
			if((projectHistory.getProjectID().toString()).contains(keyword) || projectHistory.getProjectName().toUpperCase().contains(keyword.toUpperCase()) || projectHistory.getOwner().toUpperCase().contains(keyword.toUpperCase()) || projectHistory.getStartYear().toUpperCase().contains(keyword.toUpperCase()) || projectHistory.getRegion().toUpperCase().contains(keyword.toUpperCase()) || projectHistory.getCountry().toUpperCase().contains(keyword.toUpperCase()) || projectHistory.getBrand().toUpperCase().contains(keyword.toUpperCase()))
			{
				projects.add(projectHistory);
			}
		}
		return projects;
		
	}
	
    public List<ProjectHistory> getProjectPaginationObjectListBean(List<ReadTrackerObject> readTrackerObjects)
    {
    	List<ProjectHistory> projectHistoryList = new ArrayList<ProjectHistory>();
		for(ReadTrackerObject readTrackerObject : readTrackerObjects)
		{			
				try{
					
					ProjectHistory projectHistory = new ProjectHistory();
					Project project = synchroProjectManager.get(readTrackerObject.getProjectID());
					if(SynchroPermHelper.hasValidProjectStatus(project.getProjectID()) && SynchroPermHelper.hasProjectAccess(project.getProjectID()))
					{
						Calendar cal = Calendar.getInstance();
					    cal.setTime(project.getStartDate());
					    int startYear = cal.get(Calendar.YEAR);
						if(projectResultFilter.getStartYear().intValue() == -1 || startYear == projectResultFilter.getStartYear().intValue())
							{
								projectHistory.setProjectID(readTrackerObject.getProjectID());
								projectHistory.setProjectName(project.getName());
								projectHistory.setStartYear(startYear+"");
								//Getting End MarketList for project 
								List<Long> endMarkets = synchroProjectManager.getEndMarketIDs(project.getProjectID());
								projectHistory.setRegion(StringUtils.join(SynchroUtils.getRegionsList(endMarkets), ", "));					
								projectHistory.setCountry(StringUtils.join(SynchroUtils.getEndMarketsList(endMarkets), ", "));
								projectHistory.setBrand(SynchroGlobal.getBrands(true, new Long(1)).get(Integer.parseInt(project.getBrand().toString())));
								projectHistory.setStatus(SynchroGlobal.Status.getName(project.getStatus()));
								if(project.getProjectOwner() > 0)
								{
									projectHistory.setOwner(userManager.getUser(project.getProjectOwner()).getName());
								}
								else
								{
									projectHistory.setOwner("Anonymous");
								}
								projectHistory.setStageID(readTrackerObject.getStageID());
                                projectHistory.setUrl(ProjectStage.generateURL(project, ProjectStage.getCurrentStageNumber(project)));
								projectHistoryList.add(projectHistory);
							}
					}
					
				}catch(Exception e){LOGGER.error("Error while getting history details for user   "+getUser().getID()+" Error: " +e.getMessage());}
		}
		
		return projectHistoryList;
    }
    private List<ProjectHistory> sortObjectList(List<ProjectHistory> list)
	{		
		if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().trim().equals(""))
		{
			if(projectResultFilter.getSortField().equals("id"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectHistory.OrderByID());
				}
				else
				{
					Collections.sort(list, new ProjectHistory.OrderByIDDesc());
				}
				
			}
			else if(projectResultFilter.getSortField().equals("name"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectHistory.OrderByName());
				}
				else
				{
					Collections.sort(list, new ProjectHistory.OrderByNameDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("owner"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectHistory.OrderByOwner());
				}
				else
				{
					Collections.sort(list, new ProjectHistory.OrderByOwnerDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("year"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectHistory.OrderByYear());
				}
				else
				{
					Collections.sort(list, new ProjectHistory.OrderByYearDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("brand"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectHistory.OrderByBrand());
				}
				else
				{
					Collections.sort(list, new ProjectHistory.OrderByBrandDesc());
				}
			}

			sortField = projectResultFilter.getSortField();
			ascendingOrder = projectResultFilter.getAscendingOrder();
		}
		
		return list;
	}
}
