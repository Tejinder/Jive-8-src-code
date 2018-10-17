package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.CurrentStatusViewBean;
import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;

@Decorate(false)
public class ProjectCurrentStatusDashboardPaginationAction extends JiveActionSupport {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProjectCurrentStatusDashboardPaginationAction.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private List<CurrentStatusViewBean> currentStatusBean;
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private ProjectManager synchroProjectManager;
   
    private StageManager stageManager;
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
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
       // setPagination(synchroProjectManager.getPendingActivitiesTotalCount(getSearchFilter(), getUser().getID()).intValue());
    	 setPagination(synchroProjectManager.getTotalCount(getSearchFilter()).intValue());
        updatePage();
        return SUCCESS;

    }

    public void setPagination(final Integer count) {
        if(count > LIMIT) {
            double temp = count / (LIMIT * 1.0);
            if(count%LIMIT == 0) {
                pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }
    }

    public void updatePage() {
        start = (page-1) * LIMIT;
        end = start + LIMIT;
        //pendingActivities = synchroProjectManager.getPendingActivities(getSearchFilter(), getUser().getID());
        
       // List<Project> projectList  = synchroProjectManager.getProjects(getSearchFilter());
       
        
        currentStatusBean = this.toProjectPaginationBeans(synchroProjectManager.getProjects(getSearchFilter()));
    }

    private ProjectResultFilter getSearchFilter() {
        projectResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());

        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
            // Fetch only those projects which are in PIT OPEN, PIB OPEN or Other Stage Open stage (Proposal, Project Specs, Report Summary),
            // and Completed projects as well (for showing Project Evaluation pending projects)
            statuses.add(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.PIT_OPEN.ordinal()));
            
            statuses.add(new Long(SynchroGlobal.Status.PIB_OPEN.ordinal()));
            //statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_ANALYSIS.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_IRIS.ordinal()));
           
            //https://svn.sourcen.com/issues/19916
            // statuses.add(new Long(SynchroGlobal.Status.COMPLETED.ordinal()));
           // statuses.add(new Long(SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal()));
            projectResultFilter.setProjectStatusFields(statuses);
        }

        if(projectResultFilter.getKeyword() == null || projectResultFilter.getKeyword().equals("")) {
            projectResultFilter.setKeyword(keyword);
        }
        if(projectResultFilter.getStart() == null) {
            projectResultFilter.setStart(start);
        }
        if(projectResultFilter.getLimit() == null) {
            projectResultFilter.setLimit(LIMIT);
        }
       
        if(projectResultFilter.getSortField()!=null && projectResultFilter.getSortField().equalsIgnoreCase("pendingActivity"))
        {
        	projectResultFilter.setSortField("");
        	sortField = "pendingActivity";
        }
        if(projectResultFilter.getSortField()!=null && projectResultFilter.getSortField().equalsIgnoreCase("personResponsible"))
        {
        	projectResultFilter.setSortField("");
        	sortField = "personResponsible";
        }
        return projectResultFilter;
    }

    public List<CurrentStatusViewBean> toProjectPaginationBeans(final List<Project> projects) {
        List<CurrentStatusViewBean> beans = new ArrayList<CurrentStatusViewBean>();
        for(Project project: projects) {
        	beans.add(CurrentStatusViewBean.toCurrentStatusViewBean(project));
        }
        
        if(sortField!=null && sortField.equalsIgnoreCase("pendingActivity"))
		{
        	if(projectResultFilter.getAscendingOrder() == 0)
			{
				
				Collections.sort(beans, new CurrentStatusViewBean.OrderByActivity());
			}
			else
			{
				
				Collections.sort(beans, new CurrentStatusViewBean.OrderByActivityDesc());
			}
		 	
		}
        if(sortField!=null && sortField.equalsIgnoreCase("personResponsible"))
		{
        	if(projectResultFilter.getAscendingOrder() == 0)
			{
				
				Collections.sort(beans, new CurrentStatusViewBean.OrderByPersonResponsible());
			}
			else
			{
				
				Collections.sort(beans, new CurrentStatusViewBean.OrderByPersonResponsibleDesc());
			}
		 	
		}
        return beans;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

	public List<CurrentStatusViewBean> getCurrentStatusBean() {
		return currentStatusBean;
	}

	public void setCurrentStatusBean(List<CurrentStatusViewBean> currentStatusBean) {
		this.currentStatusBean = currentStatusBean;
	}

  
}
