package com.grail.kantar.action;

import com.grail.kantar.beans.KantarReportBean;
import com.grail.kantar.beans.KantarReportResultFilter;
import com.grail.kantar.manager.KantarReportManager;
import com.grail.synchro.SynchroConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class KantarReportDashboardPaginationAction  extends JiveActionSupport {

    private static final Logger LOGGER = Logger.getLogger(KantarReportDashboardPaginationAction.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;

    private List<KantarReportBean> kantarReports;

    private KantarReportManager kantarReportManager;
    private KantarReportResultFilter kantarReportResultFilter;

    @Override
    public String execute() {
        setPagination(kantarReportManager.getTotalCount(getSearchFilter(), getUser()));
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
        kantarReports = kantarReportManager.getAll(getSearchFilter(), getUser());
    }


    private KantarReportResultFilter getSearchFilter() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        kantarReportResultFilter = new KantarReportResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(kantarReportResultFilter);
        binder.bind(getRequest());

        if(kantarReportResultFilter.getKeyword() == null || kantarReportResultFilter.getKeyword().equals("")) {
            kantarReportResultFilter.setKeyword(keyword);
        }
        if(kantarReportResultFilter.getStart() == null) {
            kantarReportResultFilter.setStart(start);
        }
        if(kantarReportResultFilter.getLimit() == null) {
            kantarReportResultFilter.setLimit(LIMIT);
        }

        if(request.getParameter("reportTypeId") != null) {
            if(kantarReportResultFilter.getReportTypes() == null) {
                kantarReportResultFilter.setReportTypes(new ArrayList<Integer>());
            }
            kantarReportResultFilter.getReportTypes().add(Integer.parseInt(request.getParameter("reportTypeId")));
        }

        if(request.getParameter("otherType") != null) {
            kantarReportResultFilter.setOtherType(Boolean.parseBoolean(request.getParameter("otherType")));
        }

        return kantarReportResultFilter;
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
        KantarReportDashboardPaginationAction.LIMIT = LIMIT;
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

    public KantarReportManager getKantarReportManager() {
        return kantarReportManager;
    }

    public void setKantarReportManager(KantarReportManager kantarReportManager) {
        this.kantarReportManager = kantarReportManager;
    }

    public List<KantarReportBean> getKantarReports() {
        return kantarReports;
    }

    public void setKantarReports(List<KantarReportBean> kantarReports) {
        this.kantarReports = kantarReports;
    }

    public KantarReportResultFilter getKantarReportResultFilter() {
        return kantarReportResultFilter;
    }

    public void setKantarReportResultFilter(KantarReportResultFilter kantarReportResultFilter) {
        this.kantarReportResultFilter = kantarReportResultFilter;
    }
}
