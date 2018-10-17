package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverCatalogueBean;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProcessWaiverManagerNew;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.StringUtils;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
@Decorate(false)
public class ProjectWaiverCataloguePaginationActionNew extends JiveActionSupport {

    private static Logger LOG = Logger.getLogger(ProjectWaiverCataloguePaginationActionNew.class);

    private List<ProjectWaiverCatalogueBean> projectCatalogues = new ArrayList<ProjectWaiverCatalogueBean>();
    private Integer page = 1;
    private Integer results;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_WAIVER_CATALOGUE_PAGE_LIMIT, 10);
    private String keyword;
    private SynchroUtils synchroUtils;
    private ProcessWaiverManagerNew processWaiverManagerNew;
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private List<ProjectWaiver> projectWaivers = new ArrayList<ProjectWaiver>();
    private String sortField;
    private Integer ascendingOrder;
    private PIBManagerNew pibManagerNew;
    private ProjectManagerNew synchroProjectManagerNew;
    
    public String getSortField() {
		return sortField;
	}

	public Integer getAscendingOrder() {
		return ascendingOrder;
	}
	
	public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    @Override
    public String execute() {
    	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    
    	/*if(!getSynchroUtils().canAccessProjectWaiver(getUser()))
    	{
    		return UNAUTHORIZED;
    	}
    	*/
    	ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());
        
        if(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroSystemOwner())
		{
        	projectWaivers = processWaiverManagerNew.getAllByResultFilter(projectResultFilter);	
		}
		else
		{
			projectWaivers = processWaiverManagerNew.getAllByResultFilter(getUser(), projectResultFilter);	
		}
		
		if(projectWaivers != null && projectWaivers.size() > 0)
		{
			results = projectWaivers.size();
			List<ProjectWaiverCatalogueBean> projectCataloguesAll =  toProjectWaiverCatalogueBeans(projectWaivers);
			results=0;
			pages=0;
			 if(projectCataloguesAll != null && keyword != null && !keyword.trim().equals(""))
			 {
				 projectCatalogues = searchProjectWaiverList(projectCataloguesAll);
				 projectCatalogues = sortObjectList(projectCatalogues);
				 if(projectCatalogues != null && projectCatalogues.size() > 0)
				 {
					 results = projectCataloguesAll.size();
				     pages = results%limit==0?results/limit:results/limit+1;
				     setPaginationFilter(page,results);
				     projectCatalogues = projectCataloguesAll.subList(start, end);
				 }
			 }
			 
			 else if(projectCataloguesAll != null && (keyword == null || keyword.trim().equals("")))
			 {
				 projectCataloguesAll = sortObjectList(projectCataloguesAll);
				 results = projectCataloguesAll.size();
			     pages = results%limit==0?results/limit:results/limit+1;
			     setPaginationFilter(page,results);
			     projectCatalogues = projectCataloguesAll.subList(start, end);
			 }
		}
		return SUCCESS;
    }

    private List<ProjectWaiverCatalogueBean> toProjectWaiverCatalogueBeans(final List<ProjectWaiver> projectWaivers) {
        List<ProjectWaiverCatalogueBean> projectCataloguesAll = new ArrayList<ProjectWaiverCatalogueBean>();
        if(projectWaivers != null && !projectWaivers.isEmpty()) {
            for(ProjectWaiver projectWaiver : projectWaivers) {
	                try {
	                	Calendar calender = Calendar.getInstance();
	                	Date creationDate=new Date(projectWaiver.getCreationDate());
	                	calender.setTime(creationDate);
		                Integer year = calender.get(Calendar.YEAR);
		                
		                
		                
		                ProjectWaiverCatalogueBean bean = new ProjectWaiverCatalogueBean();
	                    bean.setWaiverID(projectWaiver.getWaiverID());
	                    
	                    
	                    // Fetch only Process Waivers here and not the Agency Waivers, as we have separate dashoboard for Agency Waivers.
	                    
	                    
	                    bean.setWaiverUrl("/new-synchro/project-waiver!input.jspa?projectWaiverID="+projectWaiver.getWaiverID());
                    	bean.setWaiverName(projectWaiver.getName());
	                    bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
	                    bean.setYear(year);
	                    processCountryAndRegion(bean, projectWaiver);
	                    bean.setBrand(SynchroGlobal.getBrands(true, null).get(projectWaiver.getBrand().intValue()));
	                    bean.setApprover(userManager.getUser(projectWaiver.getApproverID()).getName());
	                    bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
	                    projectCataloguesAll.add(bean);
		                
	                    /*if(projectResultFilter.getStartYear().intValue() == -1 || year.intValue() == projectResultFilter.getStartYear().intValue())
		                {
			                    
			                    
			                    if(projectWaiver.getIsKantar()!=null && projectWaiver.getIsKantar())
			                    {
			                    	 // Set The URLs
			                    	 Long projectId = pibManagerNew.getProjectId(projectWaiver.getWaiverID());
			                    	 // This check has been added to handle the scenario in which the Process Waiver Id exists but there in no project id corresponding to that.
			                    	 if(projectId!=null && projectId.longValue() > 0)
			                    	 {
				                    	 Project project = synchroProjectManagerNew.get(projectId);
				                    	 if(project.getMultiMarket())
				                    	 {
				                    		 bean.setWaiverUrl("/new-synchro/pib-multi-details!input.jspa?projectID="+projectId);
				                    		 bean.setCountry("Multi-Market");
				                    	 }
				                    	 else
				                    	 {
				                    		 bean.setWaiverUrl("/new-synchro/pib-details!input.jspa?projectID="+projectId);
				                    		 List<Long> emIds = synchroProjectManagerNew.getEndMarketIDs(projectId);
				                    		// bean.setCountry(SynchroGlobal.getEndMarkets().get(emIds.get(0).intValue()));
				                    		 if(emIds!=null && emIds.size()>0)
				                    		 {
				                    			 bean.setCountry(SynchroGlobal.getEndMarkets().get(emIds.get(0).intValue()));
				                    		 }
				                    		 else
				                    		 {
				                    			 bean.setCountry("");
				                    		 }
				                    	 }
				                    	 bean.setWaiverName(project.getName());
						                 bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
						                 bean.setYear(year);
						               //  bean.setCountry("N/A");
						                 bean.setRegion("");
						                 bean.setBrand("");
						                 bean.setApprover(userManager.getUser(projectWaiver.getApproverID()).getName());
						                 bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
						                 projectCataloguesAll.add(bean);
			                    	 }
			                    }
			                    else
			                    {
				                    bean.setWaiverUrl("/new-synchro/project-waiver!input.jspa?projectWaiverID="+projectWaiver.getWaiverID());
			                    	bean.setWaiverName(projectWaiver.getName());
				                    bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
				                    bean.setYear(year);
				                    processCountryAndRegion(bean, projectWaiver);
				                    bean.setBrand(SynchroGlobal.getBrands(true, null).get(projectWaiver.getBrand().intValue()));
				                    bean.setApprover(userManager.getUser(projectWaiver.getApproverID()).getName());
				                    bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
				                    projectCataloguesAll.add(bean);
			                    }
			                   // projectCataloguesAll.add(bean);
		                }*/
	                } catch (UserNotFoundException e) {
	                    LOG.info(e.getMessage(), e);
	                }
            }
        }
      return projectCataloguesAll;
    }

	private List<ProjectWaiverCatalogueBean>  searchProjectWaiverList(final List<ProjectWaiverCatalogueBean> projectCataloguesAll)
	{
		List<ProjectWaiverCatalogueBean> projectCatalogues = new ArrayList<ProjectWaiverCatalogueBean> ();
		for(ProjectWaiverCatalogueBean projectWaiverBean : projectCataloguesAll)
		{
			if((projectWaiverBean.getWaiverID().toString()).contains(keyword) || projectWaiverBean.getWaiverName().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getInitiator().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getRegion().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getCountry().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getBrand().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getStatus().toUpperCase().contains(keyword.toUpperCase()) || projectWaiverBean.getApprover().toUpperCase().contains(keyword.toUpperCase()))
			{
				projectCatalogues.add(projectWaiverBean);
			}
		}
		return projectCatalogues;
		
	}
	
    private void processCountryAndRegion(final ProjectWaiverCatalogueBean bean, final ProjectWaiver projectWaiver) {
        Set<String> countryList = new HashSet<String>();
        Set<String> regionList = new HashSet<String>();
        if(projectWaiver.getEndMarkets() != null && !projectWaiver.getEndMarkets().isEmpty()) {
            int idx = 0;
            for(Long mrktId: projectWaiver.getEndMarkets()) {
                String country = SynchroGlobal.getEndMarkets().get(mrktId.intValue());
                if(country != null) {
                    countryList.add(country);
                }
                //String region = SynchroGlobal.getRegionEndMarket().get(mrktId.intValue());
                String region ="Global";
                if(region != null) {
                    regionList.add(region);
                }
            }
        }
        bean.setCountry(StringUtils.join(countryList.toArray(), ", "));
        bean.setRegion(StringUtils.join(regionList.toArray(), ", "));
    }
    
	
	private void setPaginationFilter(int page,int results) {
		 start = (this.page-1)*limit;
		 end = start + limit;
		 end = end>=this.results?this.results:end;
	}

    public List<ProjectWaiverCatalogueBean> getProjectCatalogues() {
        return projectCatalogues;
    }

    public void setProjectCatalogues(List<ProjectWaiverCatalogueBean> projectCatalogues) {
        this.projectCatalogues = projectCatalogues;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getResults() {
        return results;
    }

    public void setResults(Integer results) {
        this.results = results;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    
    
    private List<ProjectWaiverCatalogueBean> sortObjectList(List<ProjectWaiverCatalogueBean> list)
	{		
		if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().trim().equals(""))
		{
			if(projectResultFilter.getSortField().equals("id"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByID());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByIDDesc());
				}
				
			}
			else if(projectResultFilter.getSortField().equals("name"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByName());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByNameDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("owner"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByOwner());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByOwnerDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("year"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByYear());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByYearDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("brand"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByBrand());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByBrandDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("approver"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByApprover());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByApproverDesc());
				}
			}
			else if(projectResultFilter.getSortField().equals("status"))
			{
				if(projectResultFilter.getAscendingOrder() == 0)
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByStatus());
				}
				else
				{
					Collections.sort(list, new ProjectWaiverCatalogueBean.OrderByStatusDesc());
				}
			}

			sortField = projectResultFilter.getSortField();
			ascendingOrder = projectResultFilter.getAscendingOrder();
		}
		
		return list;
	}

	public ProcessWaiverManagerNew getProcessWaiverManagerNew() {
		return processWaiverManagerNew;
	}

	public void setProcessWaiverManagerNew(
			ProcessWaiverManagerNew processWaiverManagerNew) {
		this.processWaiverManagerNew = processWaiverManagerNew;
	}
	public PIBManagerNew getPibManagerNew() {
		return pibManagerNew;
	}

	public void setPibManagerNew(PIBManagerNew pibManagerNew) {
		this.pibManagerNew = pibManagerNew;
	}

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public void setSynchroProjectManagerNew(ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}
}
