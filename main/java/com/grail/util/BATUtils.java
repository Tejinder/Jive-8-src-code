package com.grail.util;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserAlreadyExistsException;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.user.rest.UserService;
import com.jivesoftware.util.DateUtils;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Anantha
 * Date: 10/17/11
 * Time: 5:59 PM
 * This class will be having all the customized methods for the functionality of - PasswordValidity
 */
public class BATUtils {

    private static UserManager userManager;

    private static UserService userService;

    private static final Logger log = Logger.getLogger(BATUtils.class);

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        BATUtils.userService = userService;
    }

    /***
     * To verify whether the password is expired or not
     **/
    public static boolean  isPasswordExpired(User user){

        boolean pwdExpired = false;
        double days = 0;
        //if(user.getProperties().containsKey(BATConstants.GRAIL_BAT_PASSWORD_LAST_MODIFIED_DATE)) {
        days = calculateDaysSincePwdChanged(user);

        //Pwd change is mandatory if no info abt last pwd change available in user properties.
        if(days == 0 || days >= JiveGlobals.getJiveIntProperty(BATConstants.GRAIL_BAT_PASSWORD_VALID_DAYS, 90)) {
            pwdExpired = true;
        }
        //}


        //log.info("value of pwdExpired flag = " + pwdExpired);
        return pwdExpired;
    }

    /***
     * To verify whether the password notification date reached
     */
    public static boolean isPasswordNotificationRequired(User user){
        boolean pwdNotificationRequired = false;
        // Number of days before password getting expired, notification has to be sent to the user - configuration property
        int numDaysBeforeNotification = Integer.valueOf(JiveGlobals.getJiveIntProperty(BATConstants.GRAIL_BAT_PASSWORD_NOTIFICATION_DAYS,10));
        // Number of days since password changed
        double numDaysSincePwdChanged = calculateDaysSincePwdChanged(user);
        // Number of days for password to get expired - configuration property
        int numDaysForPwdExpiry = Integer.valueOf(JiveGlobals.getJiveIntProperty(BATConstants.GRAIL_BAT_PASSWORD_VALID_DAYS, 90));

        // check whether current date falls into the password notification period
        if((numDaysSincePwdChanged >= numDaysBeforeNotification ) && (numDaysSincePwdChanged <= numDaysForPwdExpiry))
            pwdNotificationRequired = true;

        log.debug("User with E-mail "+user.getEmail()+" Password notification required is "+ pwdNotificationRequired);
        //pwdNotificationRequired = true;
        return pwdNotificationRequired;
    }

    /***
     * Calculates number of days since password modified
     * @param user
     * @return numDaysBtwDates
     */
    public static double calculateDaysSincePwdChanged(User user){
        double numDaysBtwDates = 0;
        Date lastPwdModifiedDate = null;
        DateFormat formatter = new SimpleDateFormat("MM-dd-yy HH:mm:ss");
        //log.info("fetching last password changed date from user extended property");
        if(user.getProperties().containsKey(BATConstants.GRAIL_BAT_PASSWORD_LAST_MODIFIED_DATE)) {
            String lastPwdModifiedStringDate = user.getProperties().get(BATConstants.GRAIL_BAT_PASSWORD_LAST_MODIFIED_DATE);

            // Set user last pwd modified date to september 18, 2011
            //lastPwdModifiedStringDate = "07-25-2011 00:00:00"; // MM-dd-yyyy

            try{
                lastPwdModifiedDate = formatter.parse(lastPwdModifiedStringDate);
            }catch (Exception ex){
                log.error(ex.getMessage());
                log.error("returning with boolean true, conveying that last modified date doesn't exists hence need to change password");
                return 0; //Pwd change is mandatory if no info abt last pwd change available in user properties.
            }

            if(StringUtils.isNotEmpty(lastPwdModifiedStringDate))
                numDaysBtwDates = DateUtils.daysBetween(lastPwdModifiedDate, new Date());
        }
        return numDaysBtwDates;
    }

    /***
     * To send password change notification email
     * @param user
     */
    public static void sendPasswordNotificationEmail(User user) throws UserAlreadyExistsException, UserNotFoundException {
        EmailMessage message = new EmailMessage();

        message.setHtmlBodyProperty("user.pwd.notification.email.htmlBody");
        message.setSubjectProperty("user.pwd.notification.email.subject");
        message.setLocale(JiveGlobals.getLocale());
        message.addRecipient(user.getName(), user.getEmail());
        message.getContext().put("username", user.getUsername());
        message.getContext().put ("user",user);
        // Send password notification email
        JiveApplication.getContext().getEmailManager().send(message);
        // Update password notification status
        //user.getProperties().put(BATConstants.GRAIL_BAT_PASSWORD_CHANGE_NOTIFICATION,"1");
        try {
           userService.setUserProperty(String.valueOf(user.getID()),BATConstants.GRAIL_BAT_PASSWORD_CHANGE_NOTIFICATION,"1");
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
