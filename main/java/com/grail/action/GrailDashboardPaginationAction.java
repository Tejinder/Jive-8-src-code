package com.grail.action;

import com.grail.beans.GrailBriefTemplate;
import com.grail.beans.GrailBriefTemplateFilter;
import com.grail.manager.GrailBriefTemplateManager;
import com.grail.synchro.SynchroConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/7/15
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class GrailDashboardPaginationAction extends JiveActionSupport {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GrailDashboardPaginationAction.class);

    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private List<GrailBriefTemplate> grailBriefTemplates;
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;

    private GrailBriefTemplateFilter grailBriefTemplateFilter;
    private GrailBriefTemplateManager grailBriefTemplateManager;


    @Override
    public String execute() {

        setPagination(grailBriefTemplateManager.getTotalCount(getSearchFilter()));
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
        grailBriefTemplates = grailBriefTemplateManager.getAll(getSearchFilter());
    }


    private GrailBriefTemplateFilter getSearchFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        grailBriefTemplateFilter = new GrailBriefTemplateFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(grailBriefTemplateFilter);
        binder.bind(getRequest());

        if(grailBriefTemplateFilter.getKeyword() == null || grailBriefTemplateFilter.getKeyword().equals("")) {
            grailBriefTemplateFilter.setKeyword(keyword);
        }
        if(grailBriefTemplateFilter.getStart() == null) {
            grailBriefTemplateFilter.setStart(start);
        }
        if(grailBriefTemplateFilter.getLimit() == null) {
            grailBriefTemplateFilter.setLimit(LIMIT);
        }

        if(request.getParameter("deliveryDateFrom") != null && !request.getParameter("deliveryDateFrom").equals("")) {
            try {
                this.grailBriefTemplateFilter.setDeliveryDateFrom(dateFormat.parse(request.getParameter("deliveryDateFrom")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(request.getParameter("deliveryDateTo") != null && !request.getParameter("deliveryDateTo").equals("")) {
            try {
                this.grailBriefTemplateFilter.setDeliveryDateTo(dateFormat.parse(request.getParameter("deliveryDateTo")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return grailBriefTemplateFilter;
    }


    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public static Integer getLIMIT() {
        return LIMIT;
    }

    public static void setLIMIT(Integer LIMIT) {
        GrailDashboardPaginationAction.LIMIT = LIMIT;
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

    public List<GrailBriefTemplate> getGrailBriefTemplates() {
        return grailBriefTemplates;
    }

    public void setGrailBriefTemplates(List<GrailBriefTemplate> grailBriefTemplates) {
        this.grailBriefTemplates = grailBriefTemplates;
    }

    public Integer getPageLimit() {
        return pageLimit;
    }

    public void setPageLimit(Integer pageLimit) {
        this.pageLimit = pageLimit;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

    public void setAscendingOrder(Integer ascendingOrder) {
        this.ascendingOrder = ascendingOrder;
    }

    public GrailBriefTemplateFilter getGrailBriefTemplateFilter() {
        return grailBriefTemplateFilter;
    }

    public void setGrailBriefTemplateFilter(GrailBriefTemplateFilter grailBriefTemplateFilter) {
        this.grailBriefTemplateFilter = grailBriefTemplateFilter;
    }

    public GrailBriefTemplateManager getGrailBriefTemplateManager() {
        return grailBriefTemplateManager;
    }

    public void setGrailBriefTemplateManager(GrailBriefTemplateManager grailBriefTemplateManager) {
        this.grailBriefTemplateManager = grailBriefTemplateManager;
    }
}
