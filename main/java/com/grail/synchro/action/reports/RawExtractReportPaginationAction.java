package com.grail.synchro.action.reports;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.RawExtractReportBean;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/27/14
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class RawExtractReportPaginationAction extends JiveActionSupport {

    private SynchroReportManager synchroReportManager;

    private List<RawExtractReportBean> rawExtractReportList;

    private String keyword;
    private Integer page = 1;
    private Integer pages = 0;
    private Integer start = 0;
    private Integer end;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.STANDARD_REPORT_PAGE_LIMIT, 10);

    @Override
    public String execute() {
        setPagination(synchroReportManager.getRawExtractReportTotalCount(getFilter()).intValue());
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
        rawExtractReportList = synchroReportManager.getRawExtractReport(getFilter());
    }

    private StandardReportFilter getFilter() {
        StandardReportFilter filter = new StandardReportFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(filter);
        binder.bind(getRequest());

        filter.setStart(start);
        filter.setLimit(limit);
        if(filter.getKeyword() == null || filter.getKeyword().equals("")) {
            filter.setKeyword(keyword);
        }
        if(filter.getStart() == null) {
            filter.setStart(start);
        }
        if(filter.getLimit() == null) {
            filter.setLimit(limit);
        }

        return filter;
    }

    public SynchroReportManager getSynchroReportManager() {
        return synchroReportManager;
    }

    public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
        this.synchroReportManager = synchroReportManager;
    }

    public List<RawExtractReportBean> getRawExtractReportList() {
        return rawExtractReportList;
    }

    public void setRawExtractReportList(List<RawExtractReportBean> rawExtractReportList) {
        this.rawExtractReportList = rawExtractReportList;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
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
}
