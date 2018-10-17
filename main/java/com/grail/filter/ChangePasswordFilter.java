
package com.grail.filter;

import com.grail.util.BATConstants;
import com.grail.util.BATUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserAlreadyExistsException;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.aaa.JiveUserDetails;
import com.jivesoftware.community.aaa.SystemUser;
import com.jivesoftware.community.user.rest.UserService;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Anantha
 * Date: 10/18/11
 * Time: 3:10 PM
 */
public class ChangePasswordFilter implements Filter {

    private UserManager userManager;

    private UserService userService;

    private static final Logger log = Logger.getLogger(ChangePasswordFilter.class);


    public void init(FilterConfig filterConfig) throws ServletException {
        //Do Nothing
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpSession session = req.getSession();

        User jiveUser = getUser();
//                User jiveUser = userManager.getUser(loggedInUser.getID());
        String isCancel = (String) req.getParameter("isPWDCancel");
        if( !StringUtils.isNullOrEmpty(isCancel) && isCancel.equalsIgnoreCase("true"))
        {

            log.info("User decided to cancel instead of Changing the Password. Therefore, user is being logged out.");
            session.invalidate();
            ((HttpServletResponse)servletResponse).sendRedirect(JiveGlobals.getJiveProperty("jiveURL")+"/logout.jspa");
            return;
        }

        if( jiveUser!= null) {
            long userID = jiveUser.getID();
            boolean isPwdExpired = BATUtils.isPasswordExpired(jiveUser);
            boolean isProcessed = false;
            String processed = (String)session.getAttribute("isProcessed");
            if( !StringUtils.isNullOrEmpty(processed) )
            {
                isProcessed = Boolean.valueOf(processed);
            }

            //To send email for password change/expiry notification
            if(BATUtils.isPasswordNotificationRequired(jiveUser)) {
                // Handling password notification logic
                String pwd_notification_flag = jiveUser.getProperties().get(BATConstants.GRAIL_BAT_PASSWORD_CHANGE_NOTIFICATION);

                boolean multipleEmailNotifications = JiveGlobals.getJiveBooleanProperty(BATConstants.GRAIL_BAT_SEND_MULTIPLE_PASSWORD_NOTIFICATIONS, false);

                // GRAIL_BAT_PASSWORD_CHANGE_NOTIFICATION = 0, if no notifications sent, 1 if notification sent
                if((StringUtils.isNullOrEmpty(pwd_notification_flag) || pwd_notification_flag.equals("0"))
                        || (multipleEmailNotifications)){
                    //Send Email notification to the user, if the last updated date of the password is within password notification date range
                    try {
                        BATUtils.sendPasswordNotificationEmail(jiveUser);
                    } catch (UserAlreadyExistsException e) {
                        log.error("User Already Exists : ",e);
                    } catch (UserNotFoundException e) {
                        log.error("User Not Found : ",e);
                    }
                }
            }

            if(isPwdExpired && !isProcessed ){
                //Therefore redirect the User to the change-pwd page
                log.debug("\n\n  Password expired for the User ID " + jiveUser.getID() + "  ********************");
                session.setAttribute("passwordExpired", "true");
                session.setAttribute("isProcessed","true");

                log.info("redirecting to change-password-expired!input.jspa with userID");
                ((HttpServletResponse)servletResponse).sendRedirect("change-password-expired!input.jspa?userID=" + userID);

                return;
            }
        }



        filterChain.doFilter(servletRequest, servletResponse);
    }


    public void destroy() {
        //Do Nothing
    }

    /***
     * This method will return jive user object
     * @return
     */
    private User getUser()
    {
        User user = null;
        //extract the existing authentication
        final SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();

        JiveAuthentication jiveAuth = null;

        if( auth != null ) {
            if( auth instanceof UsernamePasswordAuthenticationToken){
                user  = ((JiveUserDetails)auth.getPrincipal()).getUser();

            }
            if( auth instanceof JiveAuthentication ) {
                jiveAuth = (JiveAuthentication) auth;

                if( (!(jiveAuth.isAnonymous())) && jiveAuth.isAuthenticated() && jiveAuth.getUserID() > 0 &&
                        jiveAuth.getUserID() != SystemUser.SYSTEM_USER_ID) {

                    user = jiveAuth.getUser();

                }
            }
        }

        log.debug(( (user != null) ? "Jive User - "+ user.toString() : "JiveUser not found for password check." ));



        return user;

    }

    /**
     * Create an application user from the given user information.
     * This supports upstream auth providers alleviating them
     * from the need to universally create a user.
     * @param user
     * @return
     */
    protected User createApplicationUser(User user) {
        try {
            log.info("Creating application user");
            return this.userManager.createApplicationUser(user);
        }
        catch(Exception ex) {
            log.error("Exception in creating application user");
            return null;
        }
    }
}
