package com.grail.kantar.action;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplateFilter;
import com.grail.kantar.manager.KantarBriefTemplateManager;
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
 * Date: 10/29/14
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class KantarDashboardPaginationAction extends JiveActionSupport {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(KantarDashboardPaginationAction.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private List<KantarBriefTemplate> kantarBriefTemplates;
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;

    private KantarBriefTemplateFilter kantarBriefTemplateFilter;
    private KantarBriefTemplateManager kantarBriefTemplateManager;

    @Override
    public String execute() {

        setPagination(kantarBriefTemplateManager.getTotalCount(getSearchFilter()));
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
        kantarBriefTemplates = kantarBriefTemplateManager.getAll(getSearchFilter());
    }


    private KantarBriefTemplateFilter getSearchFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        kantarBriefTemplateFilter = new KantarBriefTemplateFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(kantarBriefTemplateFilter);
        binder.bind(getRequest());

        if(kantarBriefTemplateFilter.getKeyword() == null || kantarBriefTemplateFilter.getKeyword().equals("")) {
            kantarBriefTemplateFilter.setKeyword(keyword);
        }
        if(kantarBriefTemplateFilter.getStart() == null) {
            kantarBriefTemplateFilter.setStart(start);
        }
        if(kantarBriefTemplateFilter.getLimit() == null) {
            kantarBriefTemplateFilter.setLimit(LIMIT);
        }

        if(request.getParameter("deliveryDateFrom") != null && !request.getParameter("deliveryDateFrom").equals("")) {
            try {
                this.kantarBriefTemplateFilter.setDeliveryDateFrom(dateFormat.parse(request.getParameter("deliveryDateFrom")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(request.getParameter("deliveryDateTo") != null && !request.getParameter("deliveryDateTo").equals("")) {
            try {
                this.kantarBriefTemplateFilter.setDeliveryDateTo(dateFormat.parse(request.getParameter("deliveryDateTo")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return kantarBriefTemplateFilter;
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

    public List<KantarBriefTemplate> getKantarBriefTemplates() {
        return kantarBriefTemplates;
    }

    public void setKantarBriefTemplates(List<KantarBriefTemplate> kantarBriefTemplates) {
        this.kantarBriefTemplates = kantarBriefTemplates;
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

    public KantarBriefTemplateManager getKantarBriefTemplateManager() {
        return kantarBriefTemplateManager;
    }

    public void setKantarBriefTemplateManager(KantarBriefTemplateManager kantarBriefTemplateManager) {
        this.kantarBriefTemplateManager = kantarBriefTemplateManager;
    }
}
