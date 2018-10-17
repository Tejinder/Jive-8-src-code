package com.grail.synchro.beans;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/24/15
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectReminderBean {
    private Long id;
    private Integer projectReminderType;
    private Integer remindToType;
    private List<Long> remindTo;
    private List<Long> categoryTypes;
    private Integer draftProjectRemindBefore;
    private Integer frequencyType;
    private Integer dailyFrequencyType;
    private Integer dailyFrequency;
    private Integer weeklyFrequency;
    private List<Integer> weekdayTypes;
    private Integer monthlyFrequencyType;
    private Integer monthlyFrequency;
    private Integer monthlyWeekOfMonth;
    private Integer monthlyDayOfWeek;
    private Integer monthlyDayOfMonth;
    private Integer yearlyFrequencyType;
    private Integer yearlyFrequency;
    private Integer yearlyDayOfMonth;
    private Integer yearlyWeekOfMonth;
    private Integer yearlyDayOfWeek;
    private Integer yearlyMonthOfYear;
    private Date rangeStartDate;
    private Integer rangeEndType;
    private Integer rangeEndAfter;
    private Date rangeEndDate;
    private Date createdDate;
    private Long createdBy;
    private boolean isActive = true;
    private Date lastReminderSentOn;
    private Date nextReminderOn;
    private Integer totalReminderCount = 0;
    private boolean isViewed;
    private boolean overrideExistingCategoryTypes = false;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProjectReminderType() {
        return projectReminderType;
    }

    public void setProjectReminderType(Integer projectReminderType) {
        this.projectReminderType = projectReminderType;
    }

    public Integer getRemindToType() {
        return remindToType;
    }

    public void setRemindToType(Integer remindToType) {
        this.remindToType = remindToType;
    }

    public List<Long> getRemindTo() {
        return remindTo;
    }

    public void setRemindTo(List<Long> remindTo) {
        this.remindTo = remindTo;
    }

    public List<Long> getCategoryTypes() {
        return categoryTypes;
    }

    public void setCategoryTypes(List<Long> categoryTypes) {
        this.categoryTypes = categoryTypes;
    }

    public Integer getDraftProjectRemindBefore() {
        return draftProjectRemindBefore;
    }

    public void setDraftProjectRemindBefore(Integer draftProjectRemindBefore) {
        this.draftProjectRemindBefore = draftProjectRemindBefore;
    }

    public Integer getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(Integer frequencyType) {
        this.frequencyType = frequencyType;
    }

    public Integer getDailyFrequencyType() {
        return dailyFrequencyType;
    }

    public void setDailyFrequencyType(Integer dailyFrequencyType) {
        this.dailyFrequencyType = dailyFrequencyType;
    }

    public Integer getDailyFrequency() {
        return dailyFrequency;
    }

    public void setDailyFrequency(Integer dailyFrequency) {
        this.dailyFrequency = dailyFrequency;
    }

    public Integer getWeeklyFrequency() {
        return weeklyFrequency;
    }

    public void setWeeklyFrequency(Integer weeklyFrequency) {
        this.weeklyFrequency = weeklyFrequency;
    }

    public List<Integer> getWeekdayTypes() {
        return weekdayTypes;
    }

    public void setWeekdayTypes(List<Integer> weekdayTypes) {
        this.weekdayTypes = weekdayTypes;
    }

    public Integer getMonthlyFrequencyType() {
        return monthlyFrequencyType;
    }

    public void setMonthlyFrequencyType(Integer monthlyFrequencyType) {
        this.monthlyFrequencyType = monthlyFrequencyType;
    }

    public Integer getMonthlyFrequency() {
        return monthlyFrequency;
    }

    public void setMonthlyFrequency(Integer monthlyFrequency) {
        this.monthlyFrequency = monthlyFrequency;
    }

    public Integer getMonthlyWeekOfMonth() {
        return monthlyWeekOfMonth;
    }

    public void setMonthlyWeekOfMonth(Integer monthlyWeekOfMonth) {
        this.monthlyWeekOfMonth = monthlyWeekOfMonth;
    }

    public Integer getMonthlyDayOfWeek() {
        return monthlyDayOfWeek;
    }

    public void setMonthlyDayOfWeek(Integer monthlyDayOfWeek) {
        this.monthlyDayOfWeek = monthlyDayOfWeek;
    }

    public Integer getMonthlyDayOfMonth() {
        return monthlyDayOfMonth;
    }

    public void setMonthlyDayOfMonth(Integer monthlyDayOfMonth) {
        this.monthlyDayOfMonth = monthlyDayOfMonth;
    }

    public Integer getYearlyFrequencyType() {
        return yearlyFrequencyType;
    }

    public void setYearlyFrequencyType(Integer yearlyFrequencyType) {
        this.yearlyFrequencyType = yearlyFrequencyType;
    }

    public Integer getYearlyFrequency() {
        return yearlyFrequency;
    }

    public void setYearlyFrequency(Integer yearlyFrequency) {
        this.yearlyFrequency = yearlyFrequency;
    }

    public Integer getYearlyDayOfMonth() {
        return yearlyDayOfMonth;
    }

    public void setYearlyDayOfMonth(Integer yearlyDayOfMonth) {
        this.yearlyDayOfMonth = yearlyDayOfMonth;
    }

    public Integer getYearlyWeekOfMonth() {
        return yearlyWeekOfMonth;
    }

    public void setYearlyWeekOfMonth(Integer yearlyWeekOfMonth) {
        this.yearlyWeekOfMonth = yearlyWeekOfMonth;
    }

    public Integer getYearlyDayOfWeek() {
        return yearlyDayOfWeek;
    }

    public void setYearlyDayOfWeek(Integer yearlyDayOfWeek) {
        this.yearlyDayOfWeek = yearlyDayOfWeek;
    }

    public Integer getYearlyMonthOfYear() {
        return yearlyMonthOfYear;
    }

    public void setYearlyMonthOfYear(Integer yearlyMonthOfYear) {
        this.yearlyMonthOfYear = yearlyMonthOfYear;
    }

    public Date getRangeStartDate() {
        return rangeStartDate;
    }

    public void setRangeStartDate(Date rangeStartDate) {
        this.rangeStartDate = rangeStartDate;
    }

    public Integer getRangeEndType() {
        return rangeEndType;
    }

    public void setRangeEndType(Integer rangeEndType) {
        this.rangeEndType = rangeEndType;
    }

    public Integer getRangeEndAfter() {
        return rangeEndAfter;
    }

    public void setRangeEndAfter(Integer rangeEndAfter) {
        this.rangeEndAfter = rangeEndAfter;
    }

    public Date getRangeEndDate() {
        return rangeEndDate;
    }

    public void setRangeEndDate(Date rangeEndDate) {
        this.rangeEndDate = rangeEndDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getLastReminderSentOn() {
        return lastReminderSentOn;
    }

    public void setLastReminderSentOn(Date lastReminderSentOn) {
        this.lastReminderSentOn = lastReminderSentOn;
    }

    public Date getNextReminderOn() {
        return nextReminderOn;
    }

    public void setNextReminderOn(Date nextReminderOn) {
        this.nextReminderOn = nextReminderOn;
    }

    public Integer getTotalReminderCount() {
        return totalReminderCount;
    }

    public void setTotalReminderCount(Integer totalReminderCount) {
        this.totalReminderCount = totalReminderCount;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }

    public boolean isOverrideExistingCategoryTypes() {
        return overrideExistingCategoryTypes;
    }

    public void setOverrideExistingCategoryTypes(boolean overrideExistingCategoryTypes) {
        this.overrideExistingCategoryTypes = overrideExistingCategoryTypes;
    }
}
