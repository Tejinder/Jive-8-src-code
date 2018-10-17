package com.grail.synchro.action;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverCatalogueBean;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectWaiverManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/7/14
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class WaiverPendingActivityPaginationAction extends JiveActionSupport {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProjectPendingActivityPaginationAction.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private List<ProjectWaiverCatalogueBean> waiverPendingActivities;
    ProjectResultFilter waiverResultFilter = new ProjectResultFilter();
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;
    private ProjectWaiverManager projectWaiverManager;

    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
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
        setPagination(projectWaiverManager.getPendingActivityTotalCount(getUser(), getSearchFilter()).intValue());
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
        List<ProjectWaiver> projectWaivers = projectWaiverManager.getPendingApprovalWaivers(getUser(), getSearchFilter());
        if(projectWaivers != null && projectWaivers.size() > 0) {
            waiverPendingActivities =  toProjectWaiverCatalogueBeans(projectWaivers);
        }
    }

    private ProjectResultFilter getSearchFilter() {
        waiverResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(waiverResultFilter);
        binder.bind(getRequest());

        if(waiverResultFilter.getKeyword() == null || waiverResultFilter.getKeyword().equals("")) {
            waiverResultFilter.setKeyword(keyword);
        }
        if(waiverResultFilter.getStart() == null) {
            waiverResultFilter.setStart(start);
        }
        if(waiverResultFilter.getLimit() == null) {
            waiverResultFilter.setLimit(LIMIT);
        }

        return waiverResultFilter;
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
                    bean.setWaiverName(projectWaiver.getName());
                    bean.setInitiator(userManager.getUser(projectWaiver.getCreationBy()).getName());
                    bean.setYear(year);
                    processCountryAndRegion(bean, projectWaiver);
                    bean.setBrand(SynchroGlobal.getBrands(true, null).get(projectWaiver.getBrand().intValue()));
                    if(projectWaiver.getApproverID() != null) {
                        User approver = userManager.getUser(projectWaiver.getApproverID());
                        if(approver != null) {
                            bean.setApprover(approver.getName());
                        }
                    }

                    bean.setStatus(SynchroGlobal.ProjectWaiverStatus.getName(projectWaiver.getStatus()));
                    projectCataloguesAll.add(bean);
                } catch (UserNotFoundException e) {
                    LOG.info(e.getMessage(), e);
                }
            }
        }
        return projectCataloguesAll;
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

    public List<ProjectWaiverCatalogueBean> getWaiverPendingActivities() {
        return waiverPendingActivities;
    }


    public ProjectResultFilter getWaiverResultFilter() {
        return waiverResultFilter;
    }

    public void setWaiverResultFilter(ProjectResultFilter waiverResultFilter) {
        this.waiverResultFilter = waiverResultFilter;
    }

    public Integer getPageLimit() {
        return pageLimit;
    }

    public void setPageLimit(Integer pageLimit) {
        this.pageLimit = pageLimit;
    }

    public ProjectWaiverManager getProjectWaiverManager() {
        return projectWaiverManager;
    }

    public void setProjectWaiverManager(ProjectWaiverManager projectWaiverManager) {
        this.projectWaiverManager = projectWaiverManager;
    }
}
