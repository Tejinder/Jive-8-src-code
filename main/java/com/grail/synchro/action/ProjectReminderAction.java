package com.grail.synchro.action;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ProjectReminderBean;
import com.grail.synchro.manager.SynchroReminderManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroReminderUtils;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/19/15
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class ProjectReminderAction extends JiveActionSupport {

    private SynchroReminderManager synchroReminderManager;

    private Long id;
    private ProjectReminderBean projectReminder;

    private InputStream setReminderStatus;
    private static final String SET_REMINDER_RESPONSE = "setReminderResponse";

    private InputStream dismissStatus;
    private static final String DISMISS_REMINDER_RESPONSE = "dismissReminderResponse";

    private InputStream availableCategoriesCheckStatus;
    private static final String AVAILABLE_CATEGORY_TYPE_CHECK_RESPONSE = "availableCategoriesCheckResponse";

    private Date currentDate = null;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static final Logger LOGGER = Logger.getLogger(ProjectReminderAction.class);


    @Override
    public String input() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
        currentDate = calendar.getTime();
        processDefaults();
        return INPUT;
    }

    public void processDefaults() {
        if(request.getParameter("id") != null) {
            id = Long.parseLong(request.getParameter("id"));
        }

        if(id != null && id > 0) {
            projectReminder = synchroReminderManager.getProjectReminder(id);
        }
    }

    @Override
    public String execute() {
        return SUCCESS;
    }

    public String setReminder() {
    	Boolean newReminder = true;
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());

        ProjectReminderBean projectReminder = new ProjectReminderBean();

        if(request.getParameter("id") != null && Long.parseLong(request.getParameter("id")) > 0) {
            projectReminder = synchroReminderManager.getProjectReminder(Long.parseLong(request.getParameter("id")));
            newReminder = false;
        }

        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectReminder);
        binder.bind(request);

        if(request.getParameter("rangeStartDate") != null && !request.getParameter("rangeStartDate").equals("")) {
            try {
                Calendar cal = Calendar.getInstance();
                calendar.setTime(simpleDateFormat.parse(request.getParameter("rangeStartDate")));
                projectReminder.setRangeStartDate(calendar.getTime());
                calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
            } catch (ParseException e) {
            	LOGGER.error(e.getMessage());
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
                        projectReminder.setRangeEndDate(calendar.getTime());
                        calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
                    } catch (ParseException e) {
                    	LOGGER.error(e.getMessage());
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

        if(projectReminder.getCreatedBy() == null || projectReminder.getCreatedBy() <= 0) {
            projectReminder.setCreatedBy(getUser().getID());

        }
        if(projectReminder.getCreatedDate() == null) {
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

        if(projectReminder.getNextReminderOn() == null) {
            projectReminder.setNextReminderOn(SynchroReminderUtils.getNextProjectReminderDate(projectReminder));
        }

        try {
            synchroReminderManager.saveProjectReminder(projectReminder);
            SynchroReminderUtils.initiateProjectReminder(projectReminder);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }

        try {
            out.put("data", result);
            setReminderStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
            
            //Audit Logs: Add Project Reminder     
            if(newReminder)
            {
            	String i18Text = getText("logger.reminder.add.project.txt");
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.ALERTSANDREMINDERS.getId(), SynchroGlobal.Activity.ADD.getId(), 
        								0, i18Text, "", -1L, getUser().getID());	
            }
            else
            {
            	String i18Text = getText("logger.reminder.edit.project.txt");
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.ALERTSANDREMINDERS.getId(), SynchroGlobal.Activity.EDIT.getId(), 
        								0, i18Text, "", -1L, getUser().getID());
            }
            
            
        } catch (UnsupportedEncodingException e) {
        	LOGGER.error(e.getMessage());
        }
        return SET_REMINDER_RESPONSE;
    }

    public String dismiss() {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();

        if(request.getParameter("id") != null) {
            try {
                synchroReminderManager.updateProjectReminderStatus(Long.parseLong(request.getParameter("id")), false);
                result.put("success", true);
            } catch (Exception e) {
                result.put("success", false);
            }
        }

        try {
            out.put("data", result);
            dismissStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
            
            //Audit Logs: Dismiss Project Reminder          
            String i18Text = getText("logger.reminder.dismiss.project.txt");
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.ALERTSANDREMINDERS.getId(), SynchroGlobal.Activity.DELETE.getId(), 
    								0, i18Text, "", -1L, getUser().getID());
            
        } catch (UnsupportedEncodingException e) {
        	LOGGER.error(e.getMessage());
        }
        return DISMISS_REMINDER_RESPONSE;
    }

    public String checkAvailableCategories() {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();

        Long reminderId = null;
        if(request.getParameter("id") != null && Long.parseLong(request.getParameter("id")) > 0) {
            reminderId = Long.parseLong(request.getParameter("id"));
        }

        if(request.getParameter("categoryTypes") != null && !request.getParameter("categoryTypes").equals("")) {
            List<Long> categoryTypes = new ArrayList<Long>();
            String [] ctList = request.getParameter("categoryTypes").split(",");
            for(String ct: ctList) {
                categoryTypes.add(Long.parseLong(ct));
            }

            List<Long> userIds = new ArrayList<Long>();
            if(request.getParameter("users") != null && !request.getParameter("users").equals("")) {
                String [] uList = request.getParameter("users").split(",");
                for(String u: uList) {
                    userIds.add(Long.parseLong(u));
                }
            }
            try{
                List<Long> avlCategoryTypes = synchroReminderManager.checkForAvailableCategoryTypes(categoryTypes, userIds, reminderId);
                out.put("data", avlCategoryTypes);
            } catch (Exception e) {
                e.printStackTrace();
                out.put("data", new ArrayList());
            }

            try {
                availableCategoriesCheckStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
            	LOGGER.error(e.getMessage());
            }
        }

        return AVAILABLE_CATEGORY_TYPE_CHECK_RESPONSE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectReminderBean getProjectReminder() {
        return projectReminder;
    }

    public void setProjectReminder(ProjectReminderBean projectReminder) {
        this.projectReminder = projectReminder;
    }

    public SynchroReminderManager getSynchroReminderManager() {
        return synchroReminderManager;
    }

    public void setSynchroReminderManager(SynchroReminderManager synchroReminderManager) {
        this.synchroReminderManager = synchroReminderManager;
    }

    public InputStream getSetReminderStatus() {
        return setReminderStatus;
    }

    public void setSetReminderStatus(InputStream setReminderStatus) {
        this.setReminderStatus = setReminderStatus;
    }

    public InputStream getDismissStatus() {
        return dismissStatus;
    }

    public void setDismissStatus(InputStream dismissStatus) {
        this.dismissStatus = dismissStatus;
    }

    public InputStream getAvailableCategoriesCheckStatus() {
        return availableCategoriesCheckStatus;
    }

    public void setAvailableCategoriesCheckStatus(InputStream availableCategoriesCheckStatus) {
        this.availableCategoriesCheckStatus = availableCategoriesCheckStatus;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }
}
