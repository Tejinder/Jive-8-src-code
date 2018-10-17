package com.grail.synchro.beans;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/26/15
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneralReminderBean {
    private Long id;
    private Long remindTo;
    private String reminderBody;
    private Date reminderDate;
    private Long createdBy;
    private Date createdDate;
    private boolean isActive = true;
    private boolean isViewed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRemindTo() {
        return remindTo;
    }

    public void setRemindTo(Long remindTo) {
        this.remindTo = remindTo;
    }

    public String getReminderBody() {
        return reminderBody;
    }

    public void setReminderBody(String reminderBody) {
        this.reminderBody = reminderBody;
    }

    public Date getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(Date reminderDate) {
        this.reminderDate = reminderDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }
}
