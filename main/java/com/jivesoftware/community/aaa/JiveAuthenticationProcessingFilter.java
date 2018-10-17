/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.aaa;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.util.CookieUtils;
import com.jivesoftware.community.content.action.BanHelper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.jivesoftware.community.aaa.sso.SsoConstants.LOGIN_TYPE_COOKIE;
import static com.jivesoftware.community.aaa.sso.SsoConstants.LOGIN_TYPE_FORM;

/**
 * Authentication processing filter that extracts login and password fields as expected from existing Jive
 * applications.
 */
public class JiveAuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger log = LogManager.getLogger(JiveAuthenticationProcessingFilter.class);

    private UserManager userManager;
    private BanHelper banHelper;

    @Override
    protected String obtainPassword(HttpServletRequest request) {
    	 String pwd = request.getParameter("password");
         if(pwd == null)
         {
             pwd =(String) (request.getSession().getAttribute("password"));
         }
         //DisclaimerAction.printOut("in returning username 1111 roland pwd=" + pwd);
         //return request.getParameter("password");
         return pwd;
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
    	 String username = request.getParameter("username");
         if(username == null)
         {
             username =(String) (request.getSession().getAttribute("username"));
         }
        // DisclaimerAction.printOut("in returning username 222 roland username="+username);
         //return request.getParameter("username");
         return username;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.debug("Beginning form-based authentication attempt.");

        if (!"POST".equals(request.getMethod())) {
            throw new MethodNotSupportedException("Only HTTP POST is allowed.");
        }

        try {
            request.setCharacterEncoding(JiveGlobals.getCharacterEncoding());
        }
        catch (UnsupportedEncodingException uee) {
            log.warn("Failed to establish request encoding.", uee);
        }

        if (banHelper.isIPBanned(request)) {
            throw new UserLoginBannedException("This IP has been banned.");
        }

        Authentication auth = super.attemptAuthentication(request, response);
        log.debug("Form-based authentication attempt complete.");

        User user = null;

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthentication)) {
            if (auth instanceof UsernamePasswordAuthenticationToken) {
                try {
                    user = new UserTemplate(userManager.getUser(auth.getName()));
                }
                catch (UserNotFoundException e) {
                    log.debug("User " + auth.getName() + " not found when attempting to look up via org.springframework.security.authentication.UsernamePasswordAuthenticationToken");
                    user = new AnonymousUser();
                }
            }
            else if (auth instanceof JiveAuthentication) {
                user = new UserTemplate(((JiveAuthentication) auth).getUser());
            }
        }

        //verify we do not have an external user
        if (user != null && user.isExternal()) {
            log.warn("Rejecting authentication attempt by external user " + user.toString() + ".");
            throw new UsernameNotFoundException(user.getUsername() + " not found.");
        }

        if (banHelper.isUserBanned(user)) {
            throw new UserLoginBannedException("User login has been banned.");
        }

        return new JiveUserAuthentication(user);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication auth) throws IOException, ServletException
    {
        CookieUtils.setCookie(request, response, LOGIN_TYPE_COOKIE, LOGIN_TYPE_FORM);

        super.successfulAuthentication(request, response, auth);    //call super so we store auth in context
    }

    @Required
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Required
    public void setBanHelper(BanHelper banHelper) {
        this.banHelper = banHelper;
    }
}
