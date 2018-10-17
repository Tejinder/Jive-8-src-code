package com.grail.synchro.action;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.GeneralReminderBean;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/19/15
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class GeneralReminderAction extends JiveActionSupport {

    private SynchroReminderManager synchroReminderManager;

    private Long id;
    private GeneralReminderBean generalReminder;

    private InputStream setReminderStatus;
    private static final String SET_REMINDER_RESPONSE = "setReminderResponse";

    private InputStream dismissStatus;
    private static final String DISMISS_REMINDER_RESPONSE = "dismissReminderResponse";

    private Date currentDate = null;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    private static final Logger LOG = Logger.getLogger(GeneralReminderAction.class);

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
            generalReminder = synchroReminderManager.getGeneralReminder(id);
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

        GeneralReminderBean generalReminder = new GeneralReminderBean();
        if(request.getParameter("id") != null && Long.parseLong(request.getParameter("id")) > 0) {
            generalReminder = synchroReminderManager.getGeneralReminder(Long.parseLong(request.getParameter("id")));
            newReminder = false;
        }
        if(generalReminder == null) {
            generalReminder = new GeneralReminderBean();
        }
        ServletRequestDataBinder binder = new ServletRequestDataBinder(generalReminder);
        binder.bind(request);


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
        generalReminder.setRemindTo(getUser().getID());

        if(generalReminder.getCreatedBy() == null || generalReminder.getCreatedBy() < 0) {
            generalReminder.setCreatedBy(getUser().getID());
        }
        if(generalReminder.getCreatedDate() == null) {
            generalReminder.setCreatedDate(calendar.getTime());
        }


        Calendar reminderDateCalendar = null;

        if(request.getParameter("reminderDate") != null && !request.getParameter("reminderDate").equals("")) {
            try{
                Date reminderDate = simpleDateFormat.parse(request.getParameter("reminderDate"));
                reminderDateCalendar = Calendar.getInstance();
                reminderDateCalendar.setTime(reminderDate);
                reminderDateCalendar.setTimeZone(SynchroReminderUtils.getRemindersTimeZone());
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

        try {
            synchroReminderManager.saveGeneralReminder(generalReminder);
            SynchroReminderUtils.initiateGeneralReminder(generalReminder);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }


        try {
            out.put("data", result);
            setReminderStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
            
          //Audit Logs: Add General Reminder          
            if(newReminder)
            {
            	String i18Text = getText("logger.reminder.add.general.txt");
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.ALERTSANDREMINDERS.getId(), SynchroGlobal.Activity.ADD.getId(), 
        								0, i18Text, "", -1L, getUser().getID());	
            }
            else
            {
            	String i18Text = getText("logger.reminder.edit.general.txt");
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.ALERTSANDREMINDERS.getId(), SynchroGlobal.Activity.EDIT.getId(), 
        								0, i18Text, "", -1L, getUser().getID());
            }
            
            
            
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
        }
        return SET_REMINDER_RESPONSE;
    }

    public String dismiss() {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();

        if(request.getParameter("id") != null) {
            try {
                synchroReminderManager.updateGeneralReminderStatus(Long.parseLong(request.getParameter("id")), false);
                result.put("success", true);
            } catch (Exception e) {
                result.put("success", false);
            }
        }



        try {
            out.put("data", result);
            dismissStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
          //Audit Logs: Dismiss General Reminder          
            String i18Text = getText("logger.reminder.dismiss.general.txt");
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.ALERTSANDREMINDERS.getId(), SynchroGlobal.Activity.DELETE.getId(), 
    								0, i18Text, "", -1L, getUser().getID());
            
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
        }
        return DISMISS_REMINDER_RESPONSE;
    }


    public SynchroReminderManager getSynchroReminderManager() {
        return synchroReminderManager;
    }

    public void setSynchroReminderManager(SynchroReminderManager synchroReminderManager) {
        this.synchroReminderManager = synchroReminderManager;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GeneralReminderBean getGeneralReminder() {
        return generalReminder;
    }

    public void setGeneralReminder(GeneralReminderBean generalReminder) {
        this.generalReminder = generalReminder;
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

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }
}
