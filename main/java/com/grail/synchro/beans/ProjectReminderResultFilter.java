package com.grail.synchro.beans;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/2/15
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectReminderResultFilter {
    private List<Long> owners;
    private Date date;
    private Integer start;
    private Integer limit;
    private String sortField;
    private Integer ascendingOrder;
    private boolean showOnlyActiveReminders = true;
    private boolean fetchUnviewedReminders = false;

    public List<Long> getOwners() {
        return owners;
    }

    public void setOwners(List<Long> owners) {
        this.owners = owners;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public boolean isShowOnlyActiveReminders() {
        return showOnlyActiveReminders;
    }

    public void setShowOnlyActiveReminders(boolean showOnlyActiveReminders) {
        this.showOnlyActiveReminders = showOnlyActiveReminders;
    }

    public boolean isFetchUnviewedReminders() {
        return fetchUnviewedReminders;
    }

    public void setFetchUnviewedReminders(boolean fetchUnviewedReminders) {
        this.fetchUnviewedReminders = fetchUnviewedReminders;
    }
}
