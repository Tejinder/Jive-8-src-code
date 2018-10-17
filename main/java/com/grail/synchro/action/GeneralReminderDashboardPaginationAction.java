package com.grail.synchro.action;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.GeneralReminderBean;
import com.grail.synchro.beans.GeneralReminderResultFilter;
import com.grail.synchro.manager.SynchroReminderManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/6/15
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class GeneralReminderDashboardPaginationAction extends JiveActionSupport {

    private SynchroReminderManager synchroReminderManager;

    private List<GeneralReminderBean> generalReminders;
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

    private GeneralReminderResultFilter generalReminderResultFilter;

    @Override
    public String input() {
        return INPUT;
    }

    @Override
    public String execute() {
        setPagination(synchroReminderManager.getGeneralRemindersTotalCount(getSearchFilter()));
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
        generalReminders = synchroReminderManager.getGeneralReminders(getSearchFilter());
    }

    private GeneralReminderResultFilter getSearchFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        generalReminderResultFilter = new GeneralReminderResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(generalReminderResultFilter);
        binder.bind(getRequest());

        if(generalReminderResultFilter.getStart() == null) {
            generalReminderResultFilter.setStart(start);
        }
        if(generalReminderResultFilter.getLimit() == null) {
            generalReminderResultFilter.setLimit(LIMIT);
        }

        generalReminderResultFilter.setOwner(getUser().getID());

        generalReminderResultFilter.setDate(null);
//        generalReminderResultFilter.setDate(Calendar.getInstance().getTime());
//        generalReminderResultFilter.setShowOnlyActiveReminders(true);

        return generalReminderResultFilter;
    }

    public String updateReminderViews() {
        Set<Long> reminderIds = new HashSet<Long>();
        String reminderIdsStr = request.getParameter("reminderIds");
        if(reminderIdsStr != null && !reminderIdsStr.equals("")) {
            String[] idsArr = reminderIdsStr.split(",");
            if(idsArr != null && idsArr.length > 0) {
                for(String reminderIdStr: idsArr) {
                    GeneralReminderBean bean = synchroReminderManager.getGeneralReminder(Long.parseLong(reminderIdStr));
                    if(bean != null && bean.getId() != null && !bean.isViewed()) {
                        reminderIds.add(bean.getId());
                    }
                }
            }
        }

        if(reminderIds.size() > 0) {
            synchroReminderManager.updateGeneralReminderViews(reminderIds, getUser().getID());
        }
        return NONE;
    }


    public SynchroReminderManager getSynchroReminderManager() {
        return synchroReminderManager;
    }

    public void setSynchroReminderManager(SynchroReminderManager synchroReminderManager) {
        this.synchroReminderManager = synchroReminderManager;
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

    public GeneralReminderResultFilter getGeneralReminderResultFilter() {
        return generalReminderResultFilter;
    }

    public void setGeneralReminderResultFilter(GeneralReminderResultFilter generalReminderResultFilter) {
        this.generalReminderResultFilter = generalReminderResultFilter;
    }

    public List<GeneralReminderBean> getGeneralReminders() {
        return generalReminders;
    }

    public void setGeneralReminders(List<GeneralReminderBean> generalReminders) {
        this.generalReminders = generalReminders;
    }
}
