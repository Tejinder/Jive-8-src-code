package com.grail.synchro.manager.impl;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.*;
import com.grail.synchro.dao.SynchroReminderDAO;
import com.grail.synchro.manager.SynchroReminderManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/2/15
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroReminderManagerImpl implements SynchroReminderManager {

    private SynchroReminderDAO synchroReminderDAO;

    @Override
    @Transactional
    public void saveGeneralReminder(final GeneralReminderBean bean) {
        if(bean != null) {
            Long id = null;
            if(bean.getId() != null && bean.getId() > 0) {
                id = synchroReminderDAO.updateGeneralReminder(bean);
            } else {
                id = synchroReminderDAO.saveGeneralReminder(bean);
                bean.setId(id);
            }
        }
    }

    @Override
    @Transactional
    public void saveProjectReminder(final ProjectReminderBean bean) {
        if(bean != null) {
            Long id = null;

            if(bean.getId() != null && bean.getId() > 0) {
                id = synchroReminderDAO.updateProjectReminder(bean);
            } else {
                id = synchroReminderDAO.saveProjectReminder(bean);
                bean.setId(id);
            }
            if(bean.isOverrideExistingCategoryTypes()) {
                overrideExistingCategoryTypes(bean);
            }
            if(bean.getId() != null && bean.getId() > 0) {
                processProjectReminderDetails(bean);
            }
        }
    }

    @Override
    @Transactional
    public void overrideExistingCategoryTypes(final ProjectReminderBean bean) {
        synchroReminderDAO.overrideExistingCategoryTypes(bean);
    }

    @Override
    @Transactional
    public void updateProjectReminder(final ProjectReminderBean bean) {
        synchroReminderDAO.updateProjectReminder(bean);
    }

    @Override
    public void updateProjectReminderScheduleDates(final Date lastReminderOn, final Date nextReminderOn, final Long reminderId) {
         synchroReminderDAO.updateProjectReminderScheduleDates(lastReminderOn, nextReminderOn, reminderId);
    }

    private void processProjectReminderDetails(final ProjectReminderBean bean) {
        deleteProjectReminderUserMapping(bean.getId());
        for(Long uId : bean.getRemindTo()) {
            saveProjectReminderUserMapping(bean.getId(), uId);
        }

        if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.DAILY.getId())) {
            deleteProjectReminderDailyFreq(bean.getId());
            saveProjectReminderDailyFreq(bean);
        } else if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.WEEKLY.getId())) {
            deleteProjectReminderWeeklyFreq(bean.getId());
            saveProjectReminderWeeklyFreq(bean);
        } else if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.MONTHLY.getId())) {
            deleteProjectReminderMonthlyFreq(bean.getId());
            saveProjectReminderMonthlyFreq(bean);
        } else if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.YEARLY.getId())) {
            deleteProjectReminderYearlyFreq(bean.getId());
            saveProjectReminderYearlyFreq(bean);
        }
    }

    @Override
    @Transactional
    public void saveProjectReminderUserMapping(final Long reminderId, final Long userId) {
        synchroReminderDAO.saveProjectReminderUserMapping(reminderId, userId);
    }

    @Override
    @Transactional
    public void deleteProjectReminderUserMapping(final Long reminderId) {
        synchroReminderDAO.deleteProjectReminderUserMapping(reminderId);
    }

    @Override
    @Transactional
    public void saveProjectReminderDailyFreq(final ProjectReminderBean bean) {
        synchroReminderDAO.saveProjectReminderDailyFreq(bean);
    }

    @Override
    @Transactional
    public void deleteProjectReminderDailyFreq(final Long reminderId) {
        synchroReminderDAO.deleteProjectReminderDailyFreq(reminderId);
    }

    @Override
    @Transactional
    public void saveProjectReminderWeeklyFreq(final ProjectReminderBean bean) {
        synchroReminderDAO.saveProjectReminderWeeklyFreq(bean);
    }

    @Override
    @Transactional
    public void deleteProjectReminderWeeklyFreq(final Long reminderId) {
        synchroReminderDAO.deleteProjectReminderWeeklyFreq(reminderId);
    }

    @Override
    @Transactional
    public void saveProjectReminderMonthlyFreq(final ProjectReminderBean bean) {
        synchroReminderDAO.saveProjectReminderMonthlyFreq(bean);
    }

    @Override
    @Transactional
    public void deleteProjectReminderMonthlyFreq(final Long reminderId) {
        synchroReminderDAO.deleteProjectReminderMonthlyFreq(reminderId);
    }

    @Override
    @Transactional
    public void saveProjectReminderYearlyFreq(final ProjectReminderBean bean) {
        synchroReminderDAO.saveProjectReminderYearlyFreq(bean);
    }

    @Override
    @Transactional
    public void deleteProjectReminderYearlyFreq(final Long reminderId) {
        synchroReminderDAO.deleteProjectReminderYearlyFreq(reminderId);
    }

    @Override
    @Transactional
    public List<ProjectReminderBean> getProjectReminders(final ProjectReminderResultFilter filter) {
        return synchroReminderDAO.getProjectReminders(filter);
    }

    @Override
    @Transactional
    public Integer getProjectRemindersTotalCount(final ProjectReminderResultFilter filter) {
        return synchroReminderDAO.getProjectRemindersTotalCount(filter);
    }

    @Override
    public ProjectReminderBean getProjectReminder(final Long reminderId) {
        return synchroReminderDAO.getProjectReminder(reminderId);
    }

    @Override
    @Transactional
    public List<GeneralReminderBean> getGeneralReminders(final GeneralReminderResultFilter filter) {
        return synchroReminderDAO.getGeneralReminders(filter);
    }

    @Override
    @Transactional
    public Integer getGeneralRemindersTotalCount(final GeneralReminderResultFilter filter) {
        return synchroReminderDAO.getGeneralRemindersTotalCount(filter);
    }

    @Override
    @Transactional
    public GeneralReminderBean getGeneralReminder(final Long reminderId) {
        return synchroReminderDAO.getGeneralReminder(reminderId);
    }

    @Override
    @Transactional
    public List<GeneralReminderViewsBean> getGeneralReminderViews(final Long reminderId, final Long userId, final Date date) {
        return synchroReminderDAO.getGeneralReminderViews(reminderId, userId, date);
    }

    @Override
    @Transactional
    public List<GeneralReminderViewsBean> getGeneralReminderViews(final Long reminderId, final Long userId) {
        return getGeneralReminderViews(reminderId, userId, Calendar.getInstance().getTime());
    }

    @Override
    @Transactional
    public List<ProjectReminderViewsBean> getProjectReminderViews(final Long reminderId, final Long userId, final Date date) {
        return synchroReminderDAO.getProjectReminderViews(reminderId, userId, date);
    }

    @Override
    @Transactional
    public List<ProjectReminderViewsBean> getProjectReminderViews(final Long reminderId, final Long userId) {
        return getProjectReminderViews(reminderId, userId, Calendar.getInstance().getTime());
    }

    @Override
    @Transactional
    public Integer getProjectReminderUnViewedCount(final Long userId, final Date date) {
        return synchroReminderDAO.getProjectReminderUnViewedCount(userId, date);
    }

    @Override
    @Transactional
    public Integer getGeneralReminderUnViewedCount(Long userId, final Date date) {
        return synchroReminderDAO.getGeneralReminderUnViewedCount(userId, date);
    }

    @Override
    @Transactional
    public void updateProjectReminderViews(final Set<Long> reminderIds, final Long userId) {
        synchroReminderDAO.updateProjectReminderViews(reminderIds, userId);
    }

    @Override
    @Transactional
    public void updateGeneralReminderViews(final Set<Long> reminderIds, final Long userId) {
         synchroReminderDAO.updateGeneralReminderViews(reminderIds, userId);
    }

    @Override
    @Transactional
    public void updateProjectReminderStatus(final Long reminderId, final boolean active) {
        synchroReminderDAO.updateProjectReminderStatus(reminderId, active);
    }

    @Override
    @Transactional
    public void updateGeneralReminderStatus(final Long reminderId, final boolean active) {
        synchroReminderDAO.updateGeneralReminderStatus(reminderId, active);
    }

    @Override
    @Transactional
    public List<Long> checkForAvailableCategoryTypes(final List<Long> categoryTypes, final Long userId, final Long reminderId) {
        return synchroReminderDAO.checkForAvailableCategoryTypes(categoryTypes, userId, reminderId);
    }

    @Override
    @Transactional
    public List<Long> checkForAvailableCategoryTypes(List<Long> categoryTypes, List<Long> userIds, Long reminderId) {
        return synchroReminderDAO.checkForAvailableCategoryTypes(categoryTypes, userIds, reminderId);
    }

    public SynchroReminderDAO getSynchroReminderDAO() {
        return synchroReminderDAO;
    }

    public void setSynchroReminderDAO(SynchroReminderDAO synchroReminderDAO) {
        this.synchroReminderDAO = synchroReminderDAO;
    }
}
