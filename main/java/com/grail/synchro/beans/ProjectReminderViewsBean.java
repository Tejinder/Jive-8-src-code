package com.grail.synchro.beans;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/11/15
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectReminderViewsBean {
    private Long reminderId;
    private Date viewedDate;
    private Long viewedBy;

    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public Date getViewedDate() {
        return viewedDate;
    }

    public void setViewedDate(Date viewedDate) {
        this.viewedDate = viewedDate;
    }

    public Long getViewedBy() {
        return viewedBy;
    }

    public void setViewedBy(Long viewedBy) {
        this.viewedBy = viewedBy;
    }
}
