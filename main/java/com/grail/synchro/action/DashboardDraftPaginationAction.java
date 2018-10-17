package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.UserNotFoundException;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;

@Decorate(false)
public class DashboardDraftPaginationAction extends JiveActionSupport{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DashboardDraftPaginationAction.class);
    private List<ProjectDashboardViewBean> projects = null;
    private Integer page = 1;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, 10);
    private Integer results;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;
    private ProjectManager synchroProjectManager;
    private ProjectResultFilter projectResultFilter;
    private SynchroUtils synchroUtils;
    
    public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }


    public String execute()
    {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }

        setPagination(synchroProjectManager.getTotalCount(getSearchFilter()).intValue());
        updatePage();
        return SUCCESS;

    }

    public void setPagination(final Integer count) {
        if(count > limit) {
            double temp = count / (limit * 1.0);
            if(count%limit == 0) {
                pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }
    }

    public void updatePage() {
        start = (page-1) * limit;
        end = start + limit;
         
        List<Project> projectList  = synchroProjectManager.getProjects(getSearchFilter());
        for(Project project : projectList )
        {
        	getSynchroUtils().updateProjectStatus(project);
        	
        }
        
        projects = this.toProjectPaginationBeans(synchroProjectManager.getProjects(getSearchFilter()));
        
    }

    private ProjectResultFilter getSearchFilter() {
        projectResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());
        
        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
            // Fetch only those projects which are in DRAFT
            
//          
            statuses.add(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
            //statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()));
            //statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal()));
           // statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal()));
           // statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_ANALYSIS.ordinal()));
           // statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_IRIS.ordinal()));
            //statuses.add(new Long(SynchroGlobal.Status.COMPLETED.ordinal()));
            //statuses.add(new Long(SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal()));
            projectResultFilter.setProjectStatusFields(statuses);
        }

        if(projectResultFilter.getKeyword() == null || projectResultFilter.getKeyword().equals("")) {
            projectResultFilter.setKeyword(keyword);
        }
        projectResultFilter.setStart(start);
        projectResultFilter.setLimit(limit);

//        if(projectResultFilter.getSortField() == null || projectResultFilter.getSortField().equals("")) {
//            projectResultFilter.setSortField("status");
//        }
//
//        if(projectResultFilter.getAscendingOrder() == null) {
//            projectResultFilter.setAscendingOrder(0);
//        }


        projectResultFilter.setFetchOnlyUserSpecificProjects(true);

        return projectResultFilter;
    }

    private void setPaginationFilter(int page,int results) {
        start = (this.page-1)*limit;
        end = start + limit;
//        end = end >= this.results?this.results:end;
    }


    public List<ProjectDashboardViewBean> toProjectPaginationBeans(final List<Project> projects) {
        List<ProjectDashboardViewBean> beans = new ArrayList<ProjectDashboardViewBean>();
        for(Project project: projects) {
            //https://www.svn.sourcen.com/issues/17926
        	
        	//https://svn.sourcen.com/issues/19574
//        	if(project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())
//        	{
//        		if(project.getBriefCreator()==getUser().getID())
//        		{
//        			beans.add(ProjectDashboardViewBean.toProjectDashboardViewBean(project));
//        		}
//        	}
//        	else
//        	{
        		beans.add(ProjectDashboardViewBean.toProjectDashboardViewBean(project));
//        	}
        }
        return beans;
    }


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



    public List<ProjectDashboardViewBean> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDashboardViewBean> projects) {
        this.projects = projects;
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

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
