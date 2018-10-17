package com.grail.synchro.manager;

import com.grail.synchro.beans.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/2/15
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SynchroReminderManager {
    void saveGeneralReminder(final GeneralReminderBean bean);
    void saveProjectReminder(final ProjectReminderBean bean);
    void overrideExistingCategoryTypes(final ProjectReminderBean bean);
    void updateProjectReminder(final ProjectReminderBean bean);
    void updateProjectReminderScheduleDates(final Date lastReminderOn, final Date nextReminderOn,final Long reminderId);

    void saveProjectReminderUserMapping(final Long reminderId, final Long userId);
    void deleteProjectReminderUserMapping(final Long reminderId);

    void saveProjectReminderDailyFreq(final ProjectReminderBean bean);
    void deleteProjectReminderDailyFreq(final Long reminderId);

    void saveProjectReminderWeeklyFreq(final ProjectReminderBean bean);
    void deleteProjectReminderWeeklyFreq(final Long reminderId);

    void saveProjectReminderMonthlyFreq(final ProjectReminderBean bean);
    void deleteProjectReminderMonthlyFreq(final Long reminderId);

    void saveProjectReminderYearlyFreq(final ProjectReminderBean bean);
    void deleteProjectReminderYearlyFreq(final Long reminderId);

    List<ProjectReminderBean> getProjectReminders(final ProjectReminderResultFilter filter);
    Integer getProjectRemindersTotalCount(final ProjectReminderResultFilter filter);
    ProjectReminderBean getProjectReminder(final Long reminderId);

    List<GeneralReminderBean> getGeneralReminders(final GeneralReminderResultFilter filter);
    Integer getGeneralRemindersTotalCount(final GeneralReminderResultFilter filter);
    GeneralReminderBean getGeneralReminder(final Long reminderId);

    List<GeneralReminderViewsBean> getGeneralReminderViews(final Long reminderId, final Long userId, final Date date);
    List<GeneralReminderViewsBean> getGeneralReminderViews(final Long reminderId, final Long userId);

    List<ProjectReminderViewsBean> getProjectReminderViews(final Long reminderId, final Long userId, final Date date);
    List<ProjectReminderViewsBean> getProjectReminderViews(final Long reminderId, final Long userId);

    Integer getProjectReminderUnViewedCount(final Long userId, final Date date);
    Integer getGeneralReminderUnViewedCount(final Long userId, final Date date);

    void updateProjectReminderViews(final Set<Long> reminderIds, final Long userId);
    void updateGeneralReminderViews(final Set<Long> reminderIds, final Long userId);

    void updateProjectReminderStatus(final Long reminderId, final boolean active);
    void updateGeneralReminderStatus(final Long reminderId, final boolean active);

    List<Long> checkForAvailableCategoryTypes(final List<Long> categoryTypes, final Long userId, final Long reminderId);

    List<Long> checkForAvailableCategoryTypes(final List<Long> categoryTypes, final List<Long> userIds, final Long reminderId);

}
