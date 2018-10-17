package com.grail.synchro.util;

import com.grail.beans.GrailBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.manager.KantarBriefTemplateManager;
import com.grail.manager.GrailBriefTemplateManager;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.*;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectWaiverManager;
import com.grail.synchro.manager.SynchroReminderManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.util.BATGlobal;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.proxy.UserProxy;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.PIBManagerNew;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/2/15
 * Time: 6:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroReminderUtils {

    private static EmailManager emailManager;
    protected static UserManager userManager;

    private static final Logger LOG = Logger.getLogger(SynchroReminderUtils.class);

    private static Map<Long, Set<Long>> userProjectRemindersMap = new HashMap<Long, Set<Long>>();
    private static Map<Long, Set<Long>> userGeneralRemindersMap = new HashMap<Long, Set<Long>>();

    private static Set<Long> scheduledProjectReminders = new HashSet<Long>();
    private static Set<Long> scheduledGeneralReminders = new HashSet<Long>();

    private static SynchroReminderManager synchroReminderManager;
    private static ProjectManager synchroProjectManager;
    private static ProjectWaiverManager projectWaiverManager;
    private static GrailBriefTemplateManager grailBriefTemplateManager;
    private static KantarBriefTemplateManager kantarBriefTemplateManager;

    private static ProjectManagerNew synchroProjectManagerNew;
    
    private static PIBManagerNew pibManagerNew;
    
    public static Integer getUserAlertCount(final Long userId) {
        User user = SynchroPermHelper.getEffectiveUser();

//        Long count = getSynchroProjectManager().getPendingActivitiesTotalCount(getPendingActivitySearchFilter(), user.getID());
        Long count = getSynchroProjectManager().getPendingActivityViewCount(getPendingActivitySearchFilter(), user.getID());
        return count.intValue();
//        Integer projectRemindersCount = getSynchroReminderManager().getProjectReminderUnViewedCount(user.getID(), null);
//
//        Integer generalRemindersCount = getSynchroReminderManager().getGeneralReminderUnViewedCount(user.getID(), null);
//        return (projectRemindersCount.intValue() + generalRemindersCount.intValue());
//        ProjectReminderResultFilter projectReminderResultFilter = new ProjectReminderResultFilter();
//        List<Long> owners = new ArrayList<Long>();
//        owners.add(user.getID());
//        projectReminderResultFilter.setDate(Calendar.getInstance().getTime());
//        projectReminderResultFilter.setOwners(owners);
//        projectReminderResultFilter.setShowOnlyActiveReminders(false);
//        Integer projectRemindersCount = getSynchroReminderManager().getProjectRemindersTotalCount(projectReminderResultFilter);
//
//        GeneralReminderResultFilter generalReminderResultFilter = new GeneralReminderResultFilter();
//        generalReminderResultFilter.setOwner(user.getID());
//        generalReminderResultFilter.setDate(Calendar.getInstance().getTime());
//        generalReminderResultFilter.setShowOnlyActiveReminders(false);
//        Integer generalRemindersCount = getSynchroReminderManager().getGeneralRemindersTotalCount(generalReminderResultFilter);
//
//        return (projectRemindersCount.intValue() + generalRemindersCount.intValue());
    }



    public static void processReminders() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
        userGeneralRemindersMap = new HashMap<Long, Set<Long>>();
        scheduledGeneralReminders = new HashSet<Long>();
        GeneralReminderResultFilter generalReminderResultFilter = new GeneralReminderResultFilter();
        generalReminderResultFilter.setDate(calendar.getTime());
        List<GeneralReminderBean> generalReminders = getSynchroReminderManager().getGeneralReminders(generalReminderResultFilter);
        if(generalReminders != null && generalReminders.size() > 0) {
            for(GeneralReminderBean generalReminder : generalReminders) {
                initiateGeneralReminder(generalReminder);
            }
        }

        userProjectRemindersMap = new HashMap<Long, Set<Long>>();
        scheduledProjectReminders = new HashSet<Long>();
        ProjectReminderResultFilter projectReminderResultFilter = new ProjectReminderResultFilter();
//        projectReminderResultFilter.setDate(calendar.getTime());
        List<ProjectReminderBean> projectReminders = getSynchroReminderManager().getProjectReminders(projectReminderResultFilter);
        if(projectReminders != null && projectReminders.size() > 0) {
            for(ProjectReminderBean projectReminder : projectReminders) {
                if(projectReminder.getLastReminderSentOn() == null) {
                    getSynchroReminderManager().updateProjectReminderScheduleDates(projectReminder.getLastReminderSentOn(), getNextProjectReminderDate(projectReminder), projectReminder.getId());
                }
                initiateProjectReminder(projectReminder);
            }
        }


    }

    public static void initiateProjectReminder(final ProjectReminderBean bean) {
        if(bean != null && bean.getNextReminderOn() != null) {
            Date time = bean.getNextReminderOn();
            if(time != null) {
                Timer timer = new Timer();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
                Calendar nextDateCalendar = Calendar.getInstance();
                nextDateCalendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
                nextDateCalendar.setTime(time);

                if(nextDateCalendar.getTime().getTime() < calendar.getTime().getTime()) {
                    nextDateCalendar = calendar;
                    bean.setNextReminderOn(calendar.getTime());
                }

                int year = nextDateCalendar.get(nextDateCalendar.YEAR);
                int month = nextDateCalendar.get(nextDateCalendar.MONTH);
                int day = nextDateCalendar.get(nextDateCalendar.DATE);

                notifyDraftProjects(bean);

                if(calendar.get(Calendar.DATE) == day && calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year) {
                    if(!scheduledProjectReminders.contains(bean.getId())) {

                        timer.schedule(getProjectReminderTimerTask(bean), nextDateCalendar.getTime());
                        if(bean.getRemindTo() != null && bean.getRemindTo().size() > 0) {
                            for(Long userId : bean.getRemindTo()) {
                                Set<Long> remindersSet = null;
                                if(userProjectRemindersMap.containsKey(userId)) {
                                    remindersSet = userProjectRemindersMap.get(userId);
                                }
                                if(remindersSet == null) {
                                    remindersSet = new HashSet<Long>();
                                }
                                remindersSet.add(bean.getId());
                                userProjectRemindersMap.put(userId, remindersSet);
                            }
                        }
                        scheduledProjectReminders.add(bean.getId());
                    }
                }
            }
        }
    }

    public static void notifyDraftProjects(final ProjectReminderBean bean) {
        if(bean.getCategoryTypes() != null && bean.getCategoryTypes().size() > 0
                && bean.getCategoryTypes().contains(new Long(SynchroGlobal.ProjectReminderCategoryType.DRAFT_PROJECT.getId()))) {
            if(bean.getDraftProjectRemindBefore() != null && bean.getDraftProjectRemindBefore().intValue() > 0) {
                if(bean.getRemindTo() != null && bean.getRemindTo().size() > 0) {
                    try {
                        User userProxy = getUserManager().getUser(bean.getCreatedBy());
                        User user = ((UserProxy) userProxy).getUnproxiedObject();
                        ProjectResultFilter draftFilter = getDraftProjectsSearchFilter(user);
                        draftFilter.setDraftProjectRemindOffset(bean.getDraftProjectRemindBefore());
                        List<Project> draftProjects = getSynchroProjectManager().getProjects(draftFilter);
                        if(draftProjects != null && draftProjects.size() > 0) {
                            StringBuilder bodyBuilder = new StringBuilder();
                            bodyBuilder.append("Hi").append("<br/><br/>");
                            bodyBuilder.append("<p>This is to notify that following are the list of draft projects awaiting to start. Please click on the link for each project to start project</p>");
                            bodyBuilder.append("<p><i>(Click on the link to view the project details, please note that the link will only work for users on the BAT network and that users not on the network will need to search for the project via their own dashboard.)</i></p><br/>");
                            bodyBuilder.append("<table border='0' cellspacing='0'>");
                            bodyBuilder.append("<thead>");
                            String thStyle = "background-color:#ccc;width:100px;border:1px solid #999;";
                            bodyBuilder.append("<th style='"+thStyle+"'>Project Code</th>");
                            bodyBuilder.append("<th style='"+thStyle+"'>Project Name</th>");
                            bodyBuilder.append("<th style='"+thStyle+"'>SP&I Contact</th>");
                            bodyBuilder.append("<th style='"+thStyle+"'>Pending Activity Type</th>");
                            bodyBuilder.append("<th style='"+thStyle+"'>Pending Activity Link</th>");
                            bodyBuilder.append("</thead>");

                            bodyBuilder.append("<tbody>");
                            String tdHeaderStyle = "background-color:#000;color:#fff;font-weight:bold;text-align:center;border:1px solid #999";
                            String tdStyle = "background-color:#ccc;text-align:center;border:1px solid #999";
                            bodyBuilder.append("<tr><td colspan='5' style='"+tdHeaderStyle+"'>"+SynchroGlobal.ProjectReminderCategoryType.DRAFT_PROJECT.getName()+"</td></tr>");
                            for(Project draftProject : draftProjects) {
                                bodyBuilder.append("<tr>");
                                String link = JiveGlobals.getJiveProperty("jiveURL") + "/" + ProjectStage.generateURL(draftProject, ProjectStage.getCurrentStageNumber(draftProject));
                                bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+generateProjectCode(draftProject.getProjectID(), "")+"</a></td>");
                                bodyBuilder.append("<td style='"+tdStyle+"'>"+draftProject.getName()+"</td>");
                                String spiContactName = "";
                                if(draftProject.getSpiContact() != null && draftProject.getSpiContact().size() > 0) {
                                    try {
                                        User spiUser = getUserManager().getUser(draftProject.getSpiContact().get(0));
                                        User spiUserProxy = ((UserProxy) spiUser).getUnproxiedObject();
                                        spiContactName = spiUserProxy.getName();
                                    } catch (UserNotFoundException e) {
                                        LOG.error(e.getMessage());
                                    }
                                }
                                bodyBuilder.append("<td style='"+tdStyle+"'>"+spiContactName+"</td>");
                                bodyBuilder.append("<td style='"+tdStyle+"'>Draft (PIT) - <br/>Start Project Pending</td>");
                                bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+link+"</a></td>");
                                bodyBuilder.append("</tr>");
                            }

                            bodyBuilder.append("</tbody>");
                            bodyBuilder.append("</table>");
                            for(Long userId : bean.getRemindTo()) {
                                try {
                                    User remindToUserProxy = getUserManager().getUser(userId);
                                    User remindToUser = ((UserProxy) remindToUserProxy).getUnproxiedObject();
                                    EmailMessage message = new EmailMessage();
                                    Map<String, Object> messageContext = message.getContext();
                                    messageContext.put("portalType", BATGlobal.PortalType.SYNCHRO.toString());
                                    message.setSender("Assistance", JiveGlobals.getJiveProperty("system.adminuser.email", "assistance@batinsights.com"));
                                    message.addRecipient(remindToUser.getName(), remindToUser.getEmail());
                                    message.setSubject("SynchrO Reminder");
                                    message.setTextBody(bodyBuilder.toString());
                                    message.setHtmlBody(bodyBuilder.toString());
                                    message.setLocale(JiveGlobals.getLocale());
                                    getEmailManager().send(message);
                                } catch (UserNotFoundException e) {
                                    LOG.error("User to be reminded is not found");
                                }
                            }
                        }
                    } catch (UserNotFoundException e) {
                        LOG.error(e.getMessage());
                    }
//                    }
                }
            }
        }
    }

    public static void initiateGeneralReminder(final GeneralReminderBean bean) {
        if(bean != null && bean.getReminderDate() != null) {
            Calendar reminderDateCal = Calendar.getInstance();
            reminderDateCal.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
            reminderDateCal.setTime(bean.getReminderDate());
            Timer timer = new Timer();
            Date time = null;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
            int day = calendar.get(Calendar.DATE);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            if(day == reminderDateCal.get(Calendar.DATE) && month == reminderDateCal.get(Calendar.MONTH) && year == reminderDateCal.get(Calendar.YEAR)) {
                time = reminderDateCal.getTime();
            }
            if(time != null) {
                if(!scheduledGeneralReminders.contains(bean.getId())) {
                    timer.schedule(getGeneralReminderTimerTask(bean), time);
                    Set<Long> remindersSet = null;
                    if(userGeneralRemindersMap.containsKey(bean.getRemindTo())) {
                        remindersSet = userGeneralRemindersMap.get(bean.getRemindTo());
                    }
                    if(remindersSet == null) {
                        remindersSet = new HashSet<Long>();
                    }
                    remindersSet.add(bean.getId());
                    userGeneralRemindersMap.put(bean.getRemindTo(), remindersSet);
                    scheduledGeneralReminders.add(bean.getId());
                }
            }
        }

    }

    public static ProjectReminderTimerTask getProjectReminderTimerTask(final ProjectReminderBean bean) {
        ProjectReminderTimerTask timerTask = new ProjectReminderTimerTask(bean);
        return timerTask;
    }

    public static GeneralReminderTimerTask getGeneralReminderTimerTask(final GeneralReminderBean bean) {
        GeneralReminderTimerTask timerTask = new GeneralReminderTimerTask(bean);
        return timerTask;
    }

    public static class ProjectReminderTimerTask extends TimerTask {
        private ProjectReminderBean bean;

        public ProjectReminderTimerTask(ProjectReminderBean bean) {
            this.bean = bean;
        }

        @Override
        public void run() {

            Calendar todayCal = Calendar.getInstance();
            todayCal.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());

            try {
                User createdUserProxy = getUserManager().getUser(bean.getCreatedBy());
                User createdUser = ((UserProxy) createdUserProxy).getUnproxiedObject();
                String reminderBody = getProjectReminderBody(createdUser, bean.getCategoryTypes());
                if(reminderBody != null && !reminderBody.equals("")) {
                    List<Long> remindToList = bean.getRemindTo();
                    for(Long remindTo: remindToList) {
                        try{
                            User remindUser = getUserManager().getUser(remindTo);
                            User user = ((UserProxy) remindUser).getUnproxiedObject();
                            EmailMessage message = new EmailMessage();
                            Map<String, Object> messageContext = message.getContext();
                            messageContext.put("portalType", BATGlobal.PortalType.SYNCHRO.toString());
                            message.setSender("Assistance", JiveGlobals.getJiveProperty("system.adminuser.email", "assistance@batinsights.com"));
                            message.addRecipient(user.getName(), user.getEmail());
                            message.setSubject("SynchrO Reminder");
                            String bodyContent = reminderBody;
                            message.setTextBody(bodyContent);
                            message.setHtmlBody(bodyContent);
                            message.setLocale(JiveGlobals.getLocale());

                            getEmailManager().send(message);
                        } catch (UserNotFoundException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                }
            }  catch (UserNotFoundException e) {
                LOG.error(e.getMessage());
            }
            if(bean.getRangeEndDate() != null) {
                Date endDt = bean.getRangeEndDate();
                Calendar rangeEndCal = Calendar.getInstance();
                rangeEndCal.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
                rangeEndCal.setTime(endDt);

                if(todayCal.get(Calendar.YEAR) == rangeEndCal.get(Calendar.YEAR) && todayCal.get(Calendar.MONTH) == rangeEndCal.get(Calendar.MONTH)
                        && todayCal.get(Calendar.DATE) == rangeEndCal.get(Calendar.DATE)) {
                    bean.setActive(false);
                }
            }


            bean.setLastReminderSentOn(todayCal.getTime());
            bean.setNextReminderOn(getNextProjectReminderDate(bean));
            Integer reminderCount = bean.getTotalReminderCount() == null?0:bean.getTotalReminderCount();
            bean.setTotalReminderCount(reminderCount+1);

            if(bean.getRangeEndAfter() != null && bean.getRangeEndAfter() > 0) {
                Integer totalReminderCount = bean.getTotalReminderCount() == null?0:bean.getTotalReminderCount();
                if(totalReminderCount >= bean.getRangeEndAfter()) {
                    bean.setActive(false);
                }
            }
            getSynchroReminderManager().updateProjectReminder(bean);
        }

        public ProjectReminderBean getBean() {
            return bean;
        }

        public void setBean(ProjectReminderBean bean) {
            this.bean = bean;
        }
    }

    public static Date getNextProjectReminderDate(final ProjectReminderBean bean) {
        Calendar nextDateCalendar = null;
        boolean isNew = false;
        if(bean.getNextReminderOn() != null) {
            nextDateCalendar = Calendar.getInstance();
            nextDateCalendar.setTime(bean.getNextReminderOn());
            nextDateCalendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
        } else if(bean.getRangeStartDate() != null) {
            nextDateCalendar = Calendar.getInstance();
            nextDateCalendar.setTime(bean.getRangeStartDate());
            nextDateCalendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
            if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.DAILY.getId())
                    || bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.WEEKLY.getId())) {
                Calendar currentDateCalendar = Calendar.getInstance();
                currentDateCalendar.set(Calendar.MINUTE, 0);
                currentDateCalendar.set(Calendar.SECOND, 0);
                currentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                currentDateCalendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
                if(currentDateCalendar.getTime().getTime() > nextDateCalendar.getTime().getTime()) {
                    nextDateCalendar = currentDateCalendar;
                }
            }
            isNew = true;
        }
        nextDateCalendar.set(Calendar.MINUTE, 0);
        nextDateCalendar.set(Calendar.SECOND, 0);
        nextDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        if(nextDateCalendar != null) {

            if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.DAILY.getId())) {
                getDailyFreqNextDate(bean, nextDateCalendar, isNew);
            } else if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.WEEKLY.getId())) {
                if(bean.getWeekdayTypes() != null && bean.getWeekdayTypes().size() > 0) {
                    getWeeklyFreqNextDate(bean, nextDateCalendar, isNew);
                } else {
                    return null;
                }
            } else if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.MONTHLY.getId())) {
                getMonthlyFreqNextDate(bean, nextDateCalendar, isNew);
            } else if(bean.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.YEARLY.getId())) {
                getYearlyFreqNextDate(bean, nextDateCalendar, isNew);
            }
            return nextDateCalendar.getTime();
        } else {
            return null;
        }



    }

    private static void getDailyFreqNextDate(final ProjectReminderBean bean, final Calendar nextDateCalendar, final boolean isNew) {
        if(bean.getDailyFrequencyType().equals(1)) {
            Integer dailyFrequency = bean.getDailyFrequency();
            if(!isNew) {
                nextDateCalendar.set(Calendar.DATE, nextDateCalendar.get(Calendar.DATE) + dailyFrequency);
            }
        } else if(bean.getDailyFrequencyType().equals(2)) {
            Integer dayOfWeek = nextDateCalendar.get(Calendar.DAY_OF_WEEK);
            if(isNew) {
                if(dayOfWeek == 1 || dayOfWeek == 7) {   // skip week ends
                    nextDateCalendar.set(Calendar.DATE, nextDateCalendar.get(Calendar.DATE) + 1);
                }
            } else {
                if(dayOfWeek == 6) {   // skip week ends
                    nextDateCalendar.set(Calendar.DATE, nextDateCalendar.get(Calendar.DATE) + 2);
                } else {
                    nextDateCalendar.set(Calendar.DATE, nextDateCalendar.get(Calendar.DATE) + 1);
                }
            }
        }
    }

    private static void getWeeklyFreqNextDate(final ProjectReminderBean bean, final Calendar nextDateCalendar, final boolean isNew) {
        Integer weeklyFrequency = bean.getWeeklyFrequency();
        Integer currDayOfWeek = nextDateCalendar.get(Calendar.DAY_OF_WEEK);
        int len = bean.getWeekdayTypes().size();
        if(len > 0) {
            int dow = 0, dayCount = 0;
            int lastDow = bean.getWeekdayTypes().get(len-1);
            if(isNew && bean.getWeekdayTypes().contains(currDayOfWeek)) {
                dayCount = 0;
            } else {
                if(currDayOfWeek == lastDow) {
                    dow = bean.getWeekdayTypes().get(0);
                    dayCount = (isNew?0:(7 * weeklyFrequency))  - (currDayOfWeek - dow);
                } else if(lastDow > currDayOfWeek) {
                    dayCount = lastDow - currDayOfWeek;
                }
            }
            nextDateCalendar.set(Calendar.DATE, nextDateCalendar.get(Calendar.DATE) + dayCount);

        }
    }

    private static void getMonthlyFreqNextDate(final ProjectReminderBean bean, final Calendar nextDateCalendar, final boolean isNew) {
        Integer monthlyFrequency = bean.getMonthlyFrequency();
        Date currDateTime = nextDateCalendar.getTime();
        if(bean.getMonthlyFrequencyType().equals(1)) {
            Integer lastDayOfMonth = nextDateCalendar.getActualMaximum(Calendar.DATE);
            Integer dayOfMonth = bean.getMonthlyDayOfMonth();
            Integer currDayOfMonth = nextDateCalendar.get(Calendar.DAY_OF_MONTH);
            nextDateCalendar.set(Calendar.DATE, ((dayOfMonth > lastDayOfMonth)?lastDayOfMonth:dayOfMonth));
        } else if(bean.getMonthlyFrequencyType().equals(2)) {
            Integer weekOfMonth = bean.getMonthlyWeekOfMonth();
            Integer dayOfWeek = bean.getMonthlyDayOfWeek();
            Integer currDayOfMonth = nextDateCalendar.get(Calendar.DAY_OF_MONTH);
            nextDateCalendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
            nextDateCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            //Integer dayOfMonth = nextDateCalendar.get(Calendar.DAY_OF_MONTH);
        }
        Date updatedDateTime = nextDateCalendar.getTime();
        if(isNew) {
            if(currDateTime.getDate() > updatedDateTime.getDate()) {
                nextDateCalendar.set(Calendar.MONTH, (nextDateCalendar.get(Calendar.MONTH) + monthlyFrequency));
                if(bean.getMonthlyFrequencyType().equals(2)) {
                    nextDateCalendar.set(Calendar.WEEK_OF_MONTH, bean.getMonthlyWeekOfMonth());
                    nextDateCalendar.set(Calendar.DAY_OF_WEEK, bean.getMonthlyDayOfWeek());
                }
            }
        } else {
            nextDateCalendar.set(Calendar.MONTH, (nextDateCalendar.get(Calendar.MONTH) + monthlyFrequency));
            if(bean.getMonthlyFrequencyType().equals(2)) {
                nextDateCalendar.set(Calendar.WEEK_OF_MONTH, bean.getMonthlyWeekOfMonth());
                nextDateCalendar.set(Calendar.DAY_OF_WEEK, bean.getMonthlyDayOfWeek());
            }
        }

    }

    private static void getYearlyFreqNextDate(final ProjectReminderBean bean, final Calendar nextDateCalendar, final boolean isNew) {
        Integer yearlyFrequency = bean.getYearlyFrequency();
        Date currDateTime = nextDateCalendar.getTime();
        if(bean.getYearlyFrequencyType().equals(1)) {
            Integer monthOfYear = bean.getYearlyMonthOfYear();
            Integer dayOfMonth = bean.getYearlyDayOfMonth();
            nextDateCalendar.set(Calendar.DATE, dayOfMonth);
            nextDateCalendar.set(Calendar.MONTH, monthOfYear-1);
        } else if(bean.getYearlyFrequencyType().equals(2)) {
            Integer monthOfYear = bean.getYearlyMonthOfYear();
            Integer weekOfMonth = bean.getYearlyWeekOfMonth();
            Integer dayOfWeek = bean.getYearlyDayOfWeek();
            nextDateCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            nextDateCalendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
            nextDateCalendar.set(Calendar.MONTH, monthOfYear-1);
        }
        Date updatedDateTime = nextDateCalendar.getTime();

        if(isNew){
            if(currDateTime.getTime() > updatedDateTime.getTime()) {
                nextDateCalendar.set(Calendar.YEAR, (nextDateCalendar.get(Calendar.YEAR) + yearlyFrequency));
                if(bean.getYearlyFrequencyType().equals(2)) {
                    nextDateCalendar.set(Calendar.DAY_OF_WEEK, bean.getYearlyDayOfWeek());
                    nextDateCalendar.set(Calendar.WEEK_OF_MONTH, bean.getYearlyWeekOfMonth());
                    nextDateCalendar.set(Calendar.MONTH, bean.getYearlyMonthOfYear()-1);
                }
            }
        }  else {
            nextDateCalendar.set(Calendar.YEAR, (nextDateCalendar.get(Calendar.YEAR) + yearlyFrequency));
            if(bean.getYearlyFrequencyType().equals(2)) {
                nextDateCalendar.set(Calendar.DAY_OF_WEEK, bean.getYearlyDayOfWeek());
                nextDateCalendar.set(Calendar.WEEK_OF_MONTH, bean.getYearlyWeekOfMonth());
                nextDateCalendar.set(Calendar.MONTH, bean.getYearlyMonthOfYear()-1);
            }
        }
    }

    public static class GeneralReminderTimerTask extends TimerTask {

        private GeneralReminderBean bean;

        public GeneralReminderTimerTask(GeneralReminderBean bean) {
            this.bean = bean;
        }

        @Override
        public void run() {
            if(bean.getRemindTo() != null && bean.getRemindTo().intValue() > 0) {
                try {
                    User remindUser = getUserManager().getUser(bean.getRemindTo());
                    User user = ((UserProxy) remindUser).getUnproxiedObject();
                    EmailMessage message = new EmailMessage();
                    Map<String, Object> messageContext = message.getContext();
                    messageContext.put("portalType", BATGlobal.PortalType.SYNCHRO.toString());
                    message.setSender("Assistance", JiveGlobals.getJiveProperty("system.adminuser.email", "assistance@batinsights.com"));
                    message.addRecipient(user.getName(), user.getEmail());
                    message.setSubject("SynchrO Reminder");
                    message.setTextBody(bean.getReminderBody());
                    message.setHtmlBody(bean.getReminderBody());
                    message.setLocale(JiveGlobals.getLocale());
                    getEmailManager().send(message);
                    bean.setActive(false);
                    getSynchroReminderManager().saveGeneralReminder(bean);
                } catch (UserNotFoundException e) {
                    LOG.error(e.getMessage());
                }
            }


        }

        public GeneralReminderBean getBean() {
            return bean;
        }

        public void setBean(GeneralReminderBean bean) {
            this.bean = bean;
        }
    }

    public static String getProjectReminderBody(final User user, final List<Long> categoryTypes) {
        boolean canSendNotification = false;
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Hi").append("<br/><br/>");
        bodyBuilder.append("<p>This is to notify that following are the list of pending activities awaiting to be completed. Please click on the link for each project to review and complete the activity</p>");
        bodyBuilder.append("<p><i>(Click on the link to view the project details, please note that the link will only work for users on the BAT network and that users not on the network will need to search for the project via their own dashboard.)</i></p><br/>");
        bodyBuilder.append("<table border='0' cellspacing='0'>");
        bodyBuilder.append("<thead>");
        String thStyle = "background-color:#ccc;width:100px;border:1px solid #999;";
        bodyBuilder.append("<th style='"+thStyle+"'>Project Code</th>");
        bodyBuilder.append("<th style='"+thStyle+"'>Project Name</th>");
        bodyBuilder.append("<th style='"+thStyle+"'>SP&I Contact</th>");
        bodyBuilder.append("<th style='"+thStyle+"'>Pending Activity Type</th>");
        bodyBuilder.append("<th style='"+thStyle+"'>Pending Activity Link</th>");
        bodyBuilder.append("</thead>");

        bodyBuilder.append("<tbody>");
        String tdHeaderStyle = "background-color:#000;color:#fff;font-weight:bold;text-align:center;border:1px solid #999";
        String tdStyle = "background-color:#ccc;text-align:center;border:1px solid #999";

        if(categoryTypes.contains(new Long(SynchroGlobal.ProjectReminderCategoryType.WAIVERS.getId()))) {
            List<ProjectWaiver> projectWaivers = getProjectWaiverManager().getPendingApprovalWaivers(user, new ProjectResultFilter());
            if(projectWaivers != null && projectWaivers.size() > 0) {
                bodyBuilder.append("<tr><td colspan='5' style='"+tdHeaderStyle+"'>"+SynchroGlobal.ProjectReminderCategoryType.WAIVERS.getName()+"</td></tr>");
                for(ProjectWaiver projectWaiver : projectWaivers) {
                    String link = JiveGlobals.getJiveProperty("jiveURL") + "/" + "synchro/project-waiver!input.jspa?projectWaiverID="+projectWaiver.getWaiverID();
                    bodyBuilder.append("<tr>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+generateProjectCode(projectWaiver.getWaiverID(), "")+"</a></td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+projectWaiver.getName()+"</td>");
                    String spiContactName = "";
                    if(projectWaiver.getApproverID() != null) {
                        try {
                            User spiUser = getUserManager().getUser(projectWaiver.getApproverID());
                            User spiUserProxy = ((UserProxy) spiUser).getUnproxiedObject();
                            spiContactName = spiUserProxy.getName();
                        } catch (UserNotFoundException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+spiContactName+"</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>Process Waiver Approval Pending</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+link+"</a></td>");
                    bodyBuilder.append("</tr>");
                }
                canSendNotification = true;
            }
        }

        if(categoryTypes.contains(new Long(SynchroGlobal.ProjectReminderCategoryType.DRAFT_PROJECT.getId()))) {
            List<Project> draftProjects = getSynchroProjectManager().getProjects(getDraftProjectsSearchFilter(user));
            if(draftProjects != null && draftProjects.size() > 0) {
                bodyBuilder.append("<tr><td colspan='5' style='"+tdHeaderStyle+"'>"+SynchroGlobal.ProjectReminderCategoryType.DRAFT_PROJECT.getName()+"</td></tr>");
                for(Project draftProject : draftProjects) {
                    bodyBuilder.append("<tr>");
                    String link = JiveGlobals.getJiveProperty("jiveURL") + "/" + ProjectStage.generateURL(draftProject, ProjectStage.getCurrentStageNumber(draftProject));
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+generateProjectCode(draftProject.getProjectID(), "")+"</a></td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+draftProject.getName()+"</td>");
                    String spiContactName = "";
                    if(draftProject.getSpiContact() != null && draftProject.getSpiContact().size() > 0) {
                        try {
                            User spiUser = getUserManager().getUser(draftProject.getSpiContact().get(0));
                            User spiUserProxy = ((UserProxy) spiUser).getUnproxiedObject();
                            spiContactName = spiUserProxy.getName();
                        } catch (UserNotFoundException e) {
                            LOG.error(e.getMessage());
                        }

                    }
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+spiContactName+"</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>Draft (PIT) -<br/>Start Project Pending</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+link+"</a></td>");
                    bodyBuilder.append("</tr>");
                }
                canSendNotification = true;
            }
        }
        if(categoryTypes.contains(new Long(SynchroGlobal.ProjectReminderCategoryType.PROJECT_PENDING_ACTIVITY.getId()))) {
            List<ProjectPendingActivityViewBean> projectPendingActivities = getSynchroProjectManager().getPendingActivities(getPendingActivitySearchFilter(), user.getID());
            if(projectPendingActivities != null && projectPendingActivities.size() > 0) {
                bodyBuilder.append("<tr><td colspan='5' style='"+tdHeaderStyle+"'>"+SynchroGlobal.ProjectReminderCategoryType.PROJECT_PENDING_ACTIVITY.getName()+"</td></tr>");
                for(ProjectPendingActivityViewBean pendingProject : projectPendingActivities) {
                    String link = JiveGlobals.getJiveProperty("jiveURL") + "/" + pendingProject.getActivityLink();
                    bodyBuilder.append("<tr>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+generateProjectCode(pendingProject.getProjectID(), "")+"</a></td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+pendingProject.getProjectName()+"</td>");
                    Project project = getSynchroProjectManager().get(pendingProject.getProjectID());
                    String spiContactName = "";
                    if(project.getSpiContact() != null && project.getSpiContact().size() > 0) {
                        try {
                            User spiUser = getUserManager().getUser(project.getSpiContact().get(0));
                            User spiUserProxy = ((UserProxy) spiUser).getUnproxiedObject();
                            spiContactName = spiUserProxy.getName();
                        } catch (UserNotFoundException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+spiContactName+"</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+pendingProject.getPendingActivity()+"</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+link+"</a></td>");
                    bodyBuilder.append("</tr>");
                }
                canSendNotification = true;
            }
        }

        if(categoryTypes.contains(new Long(SynchroGlobal.ProjectReminderCategoryType.GRAIL_PENDING_ACTIVITY.getId()))) {
            List<GrailBriefTemplate> grailBriefTemplates = getGrailBriefTemplateManager().getPendingActivities(user.getID());
            if(grailBriefTemplates != null && grailBriefTemplates.size() > 0) {
                bodyBuilder.append("<tr><td colspan='5' style='"+tdHeaderStyle+"'>"+SynchroGlobal.ProjectReminderCategoryType.GRAIL_PENDING_ACTIVITY.getName()+"</td></tr>");
                for(GrailBriefTemplate grailBriefTemplate : grailBriefTemplates) {
                    String link = JiveGlobals.getJiveProperty("jiveURL") + "/" + "/grail/brief-template!input.jspa?id="+grailBriefTemplate.getId();
                    bodyBuilder.append("<tr>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+generateProjectCode(grailBriefTemplate.getId(), "G")+"</a></td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>Grail Brief</td>");
                    String spiContactName = "";
                    if(grailBriefTemplate.getBatContact() != null && grailBriefTemplate.getBatContact().intValue() > 0) {
                        try {
                            User spiUser = getUserManager().getUser(grailBriefTemplate.getBatContact());
                            User spiUserProxy = ((UserProxy) spiUser).getUnproxiedObject();
                            spiContactName = spiUserProxy.getName();
                        } catch (UserNotFoundException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+spiContactName+"</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>In Progress-Cost Pending</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+link+"</a></td>");
                    bodyBuilder.append("</tr>");
                }
                canSendNotification = true;
            }
        }
        if(categoryTypes.contains(new Long(SynchroGlobal.ProjectReminderCategoryType.KANTAR_PENDING_ACTIVITY.getId()))) {
            List<KantarBriefTemplate> kantarBriefTemplates = getKantarBriefTemplateManager().getPendingActivities(user.getID());
            if(kantarBriefTemplates != null && kantarBriefTemplates.size() > 0) {
                bodyBuilder.append("<tr><td colspan='5' style='"+tdHeaderStyle+"'>"+SynchroGlobal.ProjectReminderCategoryType.KANTAR_PENDING_ACTIVITY.getName()+"</td></tr>");
                for(KantarBriefTemplate kantarBriefTemplate : kantarBriefTemplates) {
                    String link = JiveGlobals.getJiveProperty("jiveURL") + "/" + "/kantar/brief-template!input.jspa?id="+kantarBriefTemplate.getId();
                    bodyBuilder.append("<tr>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+generateProjectCode(kantarBriefTemplate.getId(), "K")+"</a></td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>Kantar Brief</td>");
                    String spiContactName = "";
                    if(kantarBriefTemplate.getBatContact() != null && kantarBriefTemplate.getBatContact().intValue() > 0) {
                        try {
                            User spiUser = getUserManager().getUser(kantarBriefTemplate.getBatContact());
                            User spiUserProxy = ((UserProxy) spiUser).getUnproxiedObject();
                            spiContactName = spiUserProxy.getName();
                        } catch (UserNotFoundException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                    bodyBuilder.append("<td style='"+tdStyle+"'>"+spiContactName+"</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'>In Progress-Cost Pending</td>");
                    bodyBuilder.append("<td style='"+tdStyle+"'><a href='"+link+"'>"+link+"</a></td>");
                    bodyBuilder.append("</tr>");
                }
                canSendNotification = true;
            }
        }
        bodyBuilder.append("</tbody>");
        bodyBuilder.append("</table>");
        if(canSendNotification) {
            return bodyBuilder.toString();
        } else {
            return "";
        }
    }

    public static String generateProjectCode(final Long id, final String prepend) {
        if(id != null) {
            int maxDigits = 5;
            String result = "";
            int len = id.toString().length();
            if(id > 0 && len < maxDigits) {
                result += prepend;
                for(int i = 0; i < (maxDigits - len); i++) {
                    result += "0";
                }
                result += id.toString();
                return result;
            } else {
                return prepend + id.toString();
            }
        } else {
            return "";
        }
    }

    public static ProjectResultFilter getPendingActivitySearchFilter() {
        ProjectResultFilter projectResultFilter = new ProjectResultFilter();

        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
            statuses.add(new Long(SynchroGlobal.Status.PIB_OPEN.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_ANALYSIS.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.INPROGRESS_IRIS.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.COMPLETED.ordinal()));
            statuses.add(new Long(SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal()));
            projectResultFilter.setProjectStatusFields(statuses);
        }

        return projectResultFilter;
    }

    public static ProjectResultFilter getDraftProjectsSearchFilter(final User user) {
        ProjectResultFilter projectResultFilter = new ProjectResultFilter();
        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
            statuses.add(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
            projectResultFilter.setProjectStatusFields(statuses);
        }
        projectResultFilter.setFetchOnlyDraftProjects(true);
        projectResultFilter.setFetchOnlyUserSpecificProjects(true);
        projectResultFilter.setUser(user);
        projectResultFilter.setIgnoreSuperUserAccess(true);
        return projectResultFilter;
    }


    public static SynchroReminderManager getSynchroReminderManager() {
        if(synchroReminderManager == null) {
            synchroReminderManager = JiveApplication.getContext().getSpringBean("synchroReminderManager");
        }
        return synchroReminderManager;
    }

    public static ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null) {
            synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;
    }
    
    public static ProjectManagerNew getSynchroProjectManagerNew() {
        if(synchroProjectManagerNew == null) {
            synchroProjectManagerNew = JiveApplication.getContext().getSpringBean("synchroProjectManagerNew");
        }
        return synchroProjectManagerNew;
    }

    public static ProjectWaiverManager getProjectWaiverManager() {
        if(projectWaiverManager == null) {
            projectWaiverManager = JiveApplication.getContext().getSpringBean("projectWaiverManager");
        }
        return projectWaiverManager;
    }

    public static GrailBriefTemplateManager getGrailBriefTemplateManager() {
        if(grailBriefTemplateManager == null) {
            grailBriefTemplateManager = JiveApplication.getContext().getSpringBean("grailBriefTemplateManager");
        }
        return grailBriefTemplateManager;
    }

    public static KantarBriefTemplateManager getKantarBriefTemplateManager() {
        if(kantarBriefTemplateManager == null) {
            kantarBriefTemplateManager = JiveApplication.getContext().getSpringBean("kantarBriefTemplateManager");
        }
        return kantarBriefTemplateManager;
    }

    public static void setProjectWaiverManager(ProjectWaiverManager projectWaiverManager) {
        SynchroReminderUtils.projectWaiverManager = projectWaiverManager;
    }

    public static EmailManager getEmailManager() {
        if(emailManager == null) {
            emailManager = JiveApplication.getContext().getSpringBean("emailManager");
        }
        return emailManager;
    }

    public static UserManager getUserManager() {
        if(userManager == null) {
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }

    public static PIBManagerNew getPIBManagerNew() {
        if(pibManagerNew == null) {
        	pibManagerNew = JiveApplication.getContext().getSpringBean("pibManagerNew");
        }
        return pibManagerNew;
    }

    
    public static TimeZone getRemindersTimeZone() {
        String timeZoneID = JiveGlobals.getJiveProperty("reminder.timeZone",JiveGlobals.getJiveProperty("locale.timeZone"));
        return TimeZone.getTimeZone(timeZoneID);
    }
    
    public static Integer getPendingActivitiesCount() {
        User user = SynchroPermHelper.getEffectiveUser();
       
        ProjectResultFilter resultFilter = getPendingActivitySearchFilterNew();
        Long count = getSynchroProjectManagerNew().getPendingActivitiesTotalCount(resultFilter, user.getID());
        
        if(SynchroPermHelper.isSynchroSystemOwner() || SynchroPermHelper.isSystemAdmin())
        {
	        Long pendingMethWaiverCount = getPIBManagerNew().getPIBPendingMethodologyWaiversTotalCount(resultFilter);
	        Long pendingAgencyWaiverCount = getPIBManagerNew().getPIBPendingAgencyWaiversTotalCount(resultFilter);
	        return count.intValue() + pendingMethWaiverCount.intValue() + pendingAgencyWaiverCount.intValue();
        }
        
        return count.intValue();
    }
    
    public static ProjectResultFilter getPendingActivitySearchFilterNew() 
    {
        ProjectResultFilter projectResultFilter = new ProjectResultFilter();

        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) 
        {
        	  List<Long> statuses = new ArrayList<Long>();
              statuses.add(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
              statuses.add(new Long(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal()));
              statuses.add(new Long(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal()));
              statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal()));
              statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal()));
              statuses.add(new Long(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal()));
              
              projectResultFilter.setProjectStatusFields(statuses);
        }

        return projectResultFilter;
    }
}
