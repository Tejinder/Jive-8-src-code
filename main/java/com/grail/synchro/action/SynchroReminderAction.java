package com.grail.synchro.action;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.GeneralReminderBean;
import com.grail.synchro.beans.GeneralReminderResultFilter;
import com.grail.synchro.beans.ProjectReminderBean;
import com.grail.synchro.manager.SynchroReminderManager;
import com.grail.synchro.util.SynchroReminderUtils;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.util.LocaleUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/18/15
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroReminderAction extends JiveActionSupport {

    private static final Logger LOG = Logger.getLogger(SynchroReminderAction.class);

    private Integer reminderType = SynchroGlobal.SynchroReminderType.PROJECT_REMINDER.getId();
    private static final String DISMISS_PROJECT_REMINDER = "dismissProjectReminder";
    private static final String DISMISS_GENERAL_REMINDER = "dismissGeneralReminder";

    private String successURL = "/synchro/reminder!input.jspa";

    private SynchroReminderManager synchroReminderManager;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Date currentDate = null;

    private ProjectReminderBean projectReminder;
    private GeneralReminderBean generalReminder;

    @Override
    public String input() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(JiveGlobals.getTimeZone());
        currentDate = calendar.getTime();
        if(request.getParameter("reminderType") != null && request.getParameter("id") != null) {
            Long reminderId = Long.parseLong(request.getParameter("id"));

            Integer reminderType = Integer.parseInt(request.getParameter("reminderType"));

            successURL += "?id="+reminderId+"&reminderType="+reminderType;

            if(reminderType.equals(SynchroGlobal.SynchroReminderType.PROJECT_REMINDER.getId())) {
                projectReminder = synchroReminderManager.getProjectReminder(reminderId);
            } else if(reminderType.equals(SynchroGlobal.SynchroReminderType.GENERAL_REMINDER.getId())) {
                generalReminder = synchroReminderManager.getGeneralReminder(reminderId);
            }
        }
        return INPUT;
    }


    @Override
    public String execute() {
        return SUCCESS;
    }

    public String setProjectReminder() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(JiveGlobals.getTimeZone());
        ProjectReminderBean projectReminder = new ProjectReminderBean();
        if(request.getParameter("id") != null && Long.parseLong(request.getParameter("id")) > 0) {
            successURL += "?id="+request.getParameter("id")+"&reminderType=1";
            projectReminder = synchroReminderManager.getProjectReminder(Long.parseLong(request.getParameter("id")));
        }
        if(generalReminder == null) {
            projectReminder = new ProjectReminderBean();
        }
        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectReminder);
        binder.bind(request);

        if(request.getParameter("rangeStartDate") != null && !request.getParameter("rangeStartDate").equals("")) {
            try {
                Calendar cal = Calendar.getInstance();
                calendar.setTime(simpleDateFormat.parse(request.getParameter("rangeStartDate")));
                calendar.setTimeZone(JiveGlobals.getTimeZone());
                projectReminder.setRangeStartDate(calendar.getTime());
            } catch (ParseException e) {
                LOG.error(e.getMessage());
            }
        }
        if(request.getParameter("rangeEndType") != null) {
            Integer rangeEndType = Integer.parseInt(request.getParameter("rangeEndType"));
            if(rangeEndType.intValue() == 1) {
                projectReminder.setRangeEndDate(null);
                projectReminder.setRangeEndAfter(null);
            } else if(rangeEndType.intValue() == 2) {
                projectReminder.setRangeEndDate(null);
            }  else if(rangeEndType.intValue() == 3) {
                projectReminder.setRangeEndAfter(null);
                if(request.getParameter("rangeEndDate") != null && !request.getParameter("rangeEndDate").equals("")) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        calendar.setTime(simpleDateFormat.parse(request.getParameter("rangeEndDate")));
                        calendar.setTimeZone(JiveGlobals.getTimeZone());
                        projectReminder.setRangeEndDate(calendar.getTime());
                    } catch (ParseException e) {
                        LOG.error(e.getMessage());
                    }
                }

            }
        }

        List<Long> remindToList = new ArrayList<Long>();
        if(projectReminder.getProjectReminderType().equals(2)) {
            String remindToStr = request.getParameter("remindTo");
            if(remindToStr != null && !remindToStr.equals("")) {
                for(String remStr: remindToStr.split(",")) {
                    remindToList.add(Long.parseLong(remStr));
                }
            }
        } else {
            remindToList.add(getUser().getID());
        }

        projectReminder.setRemindTo(remindToList);
        if(projectReminder.getId() == null || projectReminder.getId() <= 0) {
            projectReminder.setCreatedBy(getUser().getID());
            projectReminder.setCreatedDate(calendar.getTime());
        }

        if(projectReminder.getFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.DAILY.getId())) {
            if(projectReminder.getDailyFrequencyType().equals(2)) {
                projectReminder.setDailyFrequency(null);
            }
        } else if(projectReminder.getDailyFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.WEEKLY.getId())) {
            // TODO
        } else if(projectReminder.getDailyFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.MONTHLY.getId())) {
            if(projectReminder.getMonthlyFrequencyType().equals(1)) {
                projectReminder.setMonthlyDayOfWeek(null);
                projectReminder.setMonthlyWeekOfMonth(null);
            } else if(projectReminder.getMonthlyFrequencyType().equals(2)) {
                projectReminder.setMonthlyDayOfMonth(null);
            }
        } else if(projectReminder.getDailyFrequencyType().equals(SynchroGlobal.ProjectReminderFrequencyType.YEARLY.getId())) {
            if(projectReminder.getYearlyFrequencyType().equals(1)) {
                projectReminder.setYearlyWeekOfMonth(null);
                projectReminder.setYearlyDayOfWeek(null);
            } else if(projectReminder.getYearlyFrequencyType().equals(2)) {
                projectReminder.setYearlyDayOfMonth(null);
            }
        }
        projectReminder.setNextReminderOn(SynchroReminderUtils.getNextProjectReminderDate(projectReminder));
        synchroReminderManager.saveProjectReminder(projectReminder);
        SynchroReminderUtils.initiateProjectReminder(projectReminder);
        return SUCCESS;
    }

    public String setGeneralReminder() {
        GeneralReminderBean generalReminder = new GeneralReminderBean();
        if(request.getParameter("id") != null && Long.parseLong(request.getParameter("id")) > 0) {
            successURL += "?id="+request.getParameter("id")+"&reminderType=2";
            generalReminder = synchroReminderManager.getGeneralReminder(Long.parseLong(request.getParameter("id")));
        }
        if(generalReminder == null) {
            generalReminder = new GeneralReminderBean();
        }
        ServletRequestDataBinder binder = new ServletRequestDataBinder(generalReminder);
        binder.bind(request);


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(JiveGlobals.getTimeZone());
        generalReminder.setRemindTo(getUser().getID());
        generalReminder.setCreatedBy(getUser().getID());
        generalReminder.setCreatedDate(calendar.getTime());

        Calendar reminderDateCalendar = null;

        if(request.getParameter("reminderDate") != null && !request.getParameter("reminderDate").equals("")) {
            try{
                Date reminderDate = simpleDateFormat.parse(request.getParameter("reminderDate"));
                reminderDateCalendar = Calendar.getInstance();
                reminderDateCalendar.setTime(reminderDate);
                reminderDateCalendar.setTimeZone(JiveGlobals.getTimeZone());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(reminderDateCalendar != null) {
            if(request.getParameter("hours") != null && !request.getParameter("hours").equals("")) {

                String meridian = "am";
                if(request.getParameter("meridian") != null && !request.getParameter("meridian").equals("")) {
                    meridian = request.getParameter("meridian");
                }
//                if (meridian.equalsIgnoreCase("am")) {
//                    reminderDateCalendar.set(Calendar.AM_PM, Calendar.AM);
//                } else if (meridian.equalsIgnoreCase("pm")) {
//                    reminderDateCalendar.set(Calendar.AM_PM, Calendar.PM);
//                }
                Integer hours = Integer.parseInt(request.getParameter("hours").toString());
                if(meridian.equals("pm") && hours != 12) {
                    hours += 12;
                } else if(meridian.equals("am") && hours == 12) {
                    hours = 0;
                }
                reminderDateCalendar.set(Calendar.HOUR_OF_DAY, hours);
            }

            if(request.getParameter("minutes") != null && !request.getParameter("minutes").equals("")) {
                reminderDateCalendar.set(Calendar.MINUTE, Integer.parseInt(request.getParameter("minutes").toString()));
            }


        }
        if(reminderDateCalendar != null) {
            generalReminder.setReminderDate(reminderDateCalendar.getTime());
        }
        synchroReminderManager.saveGeneralReminder(generalReminder);
        SynchroReminderUtils.initiateGeneralReminder(generalReminder);
        return SUCCESS;
    }

    public String dismissProjectReminder() {
        if(request.getParameter("id") != null) {
            synchroReminderManager.updateProjectReminderStatus(Long.parseLong(request.getParameter("id")), false);
        }
        return DISMISS_PROJECT_REMINDER;
    }

    public String dismissGeneralReminder() {
        if(request.getParameter("id") != null) {
            synchroReminderManager.updateGeneralReminderStatus(Long.parseLong(request.getParameter("id")), false);
        }
        return DISMISS_GENERAL_REMINDER;
    }

    public Integer getReminderType() {
        return reminderType;
    }

    public void setReminderType(Integer reminderType) {
        this.reminderType = reminderType;
    }

    public String getSuccessURL() {
        return successURL;
    }

    public void setSuccessURL(String successURL) {
        this.successURL = successURL;
    }

    public SynchroReminderManager getSynchroReminderManager() {
        return synchroReminderManager;
    }

    public void setSynchroReminderManager(SynchroReminderManager synchroReminderManager) {
        this.synchroReminderManager = synchroReminderManager;
    }


    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public ProjectReminderBean getProjectReminder() {
        return projectReminder;
    }

    public void setProjectReminder(ProjectReminderBean projectReminder) {
        this.projectReminder = projectReminder;
    }

    public GeneralReminderBean getGeneralReminder() {
        return generalReminder;
    }

    public void setGeneralReminder(GeneralReminderBean generalReminder) {
        this.generalReminder = generalReminder;
    }
}
