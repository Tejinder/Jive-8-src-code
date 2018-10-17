/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.aaa;

import com.grail.synchro.SynchroConstants;
import com.grail.util.BATConstants;
import com.grail.util.BATGlobal;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.Ban;
import com.jivesoftware.community.BanLevel;
import com.jivesoftware.community.BanManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.aaa.stateless.MDCHydrator;
import com.jivesoftware.community.action.DisclaimerAction;
import com.jivesoftware.community.action.util.ActionUtils;
import com.jivesoftware.community.action.util.CookieUtils;
import com.jivesoftware.community.license.JiveLicenseManager;
import org.apache.commons.lang.StringUtils;

import com.jivesoftware.community.license.JiveLicenseManagerImpl;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication filter responsible for two Jive-specific behaviors. First is to enforce high-level CS vs. CSC
 * behaviors - i.e. cutting-off anonymous users for CS installations.
 * <p/>
 * The second responsibility of this class is to coerce all potential upstream authentication provider mechanisms into
 * one universal JiveAuthentication. This alleviates the downstream application from being concerned with the multiple
 * types of authentication supported. The added benefit of allowing multiple types of authentication to be returned out
 * of the authentication providers should additionally make extension of auth mechanisms further simplified.
 *
 * @deprecated instead, com.jivesoftware.community.aaa.PostAuthenticationSetupStrategy should be called at the end of your
 * authentication processing filters lifecycle.
 * @see JiveAuthenticationProcessingFilter
 */
@Deprecated
public class JiveAuthenticationTranslationFilter implements Filter, InitializingBean, ApplicationEventPublisherAware {

    private static final Logger log = Logger.getLogger(JiveAuthenticationTranslationFilter.class);

    /**
     * Cookie used to show that the current user is logged in. This is used for things such
     * as caching for Akamai
     */
    public static final String COOKIE_LOGGED_IN = "jive.user.loggedIn";

    /**
     * Cookie used for things like Akamai to tell information about a specific server.
     */
    public static final String COOKIE_JIVE_SERVER_INFO = "jive.server.info";

    private ApplicationEventPublisher applicationEventPublisher;
    private UserManager userManager;
    private JiveLicenseManagerImpl licenseManager;
    private BanManager banManager;
    private JiveAuthenticationTranslationHelper authenticationTranslationHelper;

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setLicenseManager(JiveLicenseManagerImpl licenseManager) {
        this.licenseManager = licenseManager;
    }

    public void setBanManager(BanManager banManager) {
        this.banManager = banManager;
    }

    public void setAuthenticationTranslationHelper(
            JiveAuthenticationTranslationHelper authenticationTranslationHelper)
    {
        this.authenticationTranslationHelper = authenticationTranslationHelper;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        //no-op, spring managed
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        //extract the existing authentication
        final SecurityContext context = SecurityContextHolder.getContext();

        //DisclaimerAction.printOut(" roland do filter jiveauthentiTranslation filter111....");
        final String requestURI = ((HttpServletRequest)servletRequest).getRequestURI();
        final String contextPath = ((HttpServletRequest)servletRequest).getContextPath();
        // Strip the servlet context from the requestURI.  This removes a substring only
        // if it is at the begining of a source string, otherwise returns the source string.
        String uriAfterContextPath = StringUtils.removeStart(requestURI, contextPath);

        //	/**********
        if(requestURI.contains("disclaimer.jspa") || ((HttpServletRequest)servletRequest).getSession().getAttribute("username") != null) {
            DisclaimerAction.printOut(" in going to attempt Authentication 1412 as uri is disclimjspa-- roland Jiveauttrhanslate i ");
            //JiveAuthenticationProcessingFilter jivAut = new JiveAuthenticationProcessingFilter();
            //jivAut.attemptAuthentication((HttpServletRequest)servletRequest);
            //filterChain.doFilter(servletRequest, servletResponse);
            //return;

        }
        //****************/
        
        if (null == context) {
            throw new ServletException("Invalid filter configuration: Must be located after security context "
                    + "has been established.");
        }

        Authentication auth = context.getAuthentication();

        if (auth == null) {
            auth = new AnonymousAuthentication();
        }

        if (auth instanceof OAuth2Authentication) {
            auth = ((OAuth2Authentication) auth).getUserAuthentication();
        }

        boolean updateLastLoggedInOnSuccess = false;

        /*
            NOTE: this block is purely legacy and serves no purpose in the actual core app.  Last logged in date is now
            updated in com.jivesoftware.community.aaa.DefaultPostAuthenticationSetupStrategy.
         */
        JiveAuthentication jiveAuth = null;
        if (!(auth instanceof JiveAuthentication)) {
            /*
            I think this branch is what happens the 1st time you get through SPRING SECURITY authentication.
             We have various SPRING SECURITY Auth classes (UsernamePasswordAuthToken, RememberMeAuthToken, etc).
             We coerce these into our JiveAuthentication object and store that, so subsequent passes through this
             class skip this branch
             */

            //try to load the user from the existing authentication
            User user = authenticationTranslationHelper.extractJiveUser(auth);

            if (null == user) {
                log.warn("No jive user found in authentication.");
                throw new InsufficientAuthenticationException("Jive User not located in authentication.");
            }

            //replace the existing authentication with a jive authentication and continue down the filter
            jiveAuth = new JiveUserAuthentication(user);

            //set the current authentication to the jive auth for the life of the user's session
            context.setAuthentication(jiveAuth);

            updateLastLoggedInOnSuccess = authenticationTranslationHelper.needUpdateLastLoggedIn(jiveAuth);

        }
        else {
            jiveAuth = (JiveAuthentication) auth;
        }

        if ((!(jiveAuth.isAnonymous())) && jiveAuth.isAuthenticated() && jiveAuth.getUserID() < 0 &&
                jiveAuth.getUserID() != SystemUser.SYSTEM_USER_ID)
        {
            User user = createApplicationUser(jiveAuth.getUser());
            log.info("New application user created for " + user);
        }

        //CS-4928 - check that the user is not disabled at the application layer
        //CS-5587 - Runtime changes to isEnabled status are not getting updated in jiveAuth's user object, so the check
        // is done against a user object obtained from the manager.
        if (null != jiveAuth.getUser() && jiveAuth.getUserID() > 0 &&
                !userManager.getUser(jiveAuth.getUser()).isEnabled())
        {
            //CS-5587 - Terminate any open sessions if the user is disabled.
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                HttpServletResponse response = (HttpServletResponse) servletResponse;

                //invalidate session and clear cookies
                request.getSession().invalidate();
                CookieUtils.deleteCookies(request, response);
            }
            final String msg = jiveAuth.getUser() + " failed login due to a disabled application account";
            log.warn(msg);
            throw new DisabledException(msg);
        }

        // Check if the user login has been banned but only if the user isn't anonymous
        if (banManager.isBanningEnabled() && !jiveAuth.isAnonymous()) {
            Ban loginBan = banManager.getBan(jiveAuth.getUserID(), BanLevel.BAN_LOGIN);
            if (loginBan != null) {
                checkIsBanned(loginBan);
            }
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                Ban ipBan = banManager.getBan(ActionUtils.getRemoteAddress(request), BanLevel.BAN_LOGIN);
                if (ipBan != null) {
                    checkIsBanned(ipBan);
                }
            }
        }

        if (updateLastLoggedInOnSuccess) {
            authenticationTranslationHelper.updateLastLoggedInTime(jiveAuth);
        }

        // auth shouldn't ever be null by this point
        // Set login Cookie information
        addLoginCookie(jiveAuth, (HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);

        // Adds server information cookie
        addServerInfoCookie((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);

        // Adds MDC context for the user
        MDC.put(MDCHydrator.USERNAME_KEY, jiveAuth.getName());

        // CS-22377 make sure auth is absolute latest from userManager if possible
        if (auth instanceof JiveUserAuthentication) {
            try {
                User latest = userManager.getUser(((JiveUserAuthentication) auth).getUserID());
                context.setAuthentication(new JiveUserAuthentication(latest));
            }
            catch (Exception e) {
                log.error("Error reloading JiveUserAuthentication with the latest user copy", e);
            }
        }

        //CS-3445, do not allow downstream coding errors to propagate the system authentication back to the session
        try {
           // filterChain.doFilter(servletRequest, servletResponse);
            HttpSession session = ((HttpServletRequest)servletRequest).getSession();
            String userName = auth.getName();
            filterChain.doFilter(servletRequest, servletResponse);
        }
        catch (ServletException se) {
            throw se;
        }
        catch (IOException ioe) {
            throw ioe;
        }
        finally {

            MDC.remove(MDCHydrator.USERNAME_KEY);

            final Authentication postRequestAuth = context.getAuthentication();

            if (postRequestAuth instanceof SystemAuthentication) {
                final HttpServletRequest request = (HttpServletRequest) servletRequest;
                final Logger log = Logger.getLogger(JiveAuthenticationTranslationFilter.class.getName());
                log.error("System authentication propogation out of request handling. Previous authentication "
                        + "will be restored possibly resulting in a logout (uri='" + request.getRequestURI() +
                        "';auth='" +
                        auth + ";thread=" + Thread.currentThread().getName() + ";time=" + System.currentTimeMillis() +
                        ").");
                context.setAuthentication(auth);
            }
        }


    }

    private void checkIsBanned(Ban ban) {
        final long now = new Date().getTime();
        final long start = ban.getCreationDate() == null ? 0 : ban.getCreationDate().getTime();
        if (now > start) {
            final long end = ban.getExpirationDate() == null ? Long.MAX_VALUE : ban.getExpirationDate().getTime();
            if (now < end) {
                throw new UserLoginBannedException("User login has been banned.");
            }
        }
    }

    /**
     * Lifecycle cleanup.
     */
    public void destroy() {
        //release local references
        this.applicationEventPublisher = null;
        this.userManager = null;
        this.banManager = null;
        log.info("Successfully destroyed.");
    }

    /**
     * Explicit spring validation performed after dependency injection.
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (null == userManager) {
            throw new RuntimeException("UserManager not configured.");
        }
    }

    /**
     * Create an application user from the given user information.
     * This supports upstream auth providers alleviating them
     * from the need to universally create a user.
     *
     * @param user
     * @return
     */
    protected User createApplicationUser(User user) {
        try {
            return this.userManager.createApplicationUser(user);
        }
        catch (Exception ex) {
            log.warn("Unable to create application user for " + user, ex);
            return null;
        }
    }

    protected User extractJiveUser(Authentication auth) {
        // moved to helper - leaving here 'cause it's protected
        return authenticationTranslationHelper.extractJiveUser(auth);
    }

    protected User resolveUser(Object candidate) {
        // moved to helper - leaving here 'cause it's protected
        return authenticationTranslationHelper.resolveUser(candidate);
    }

    protected User setLastLoggedInDate(User user) {
        // moved to helper - leaving here 'cause it's protected
        return authenticationTranslationHelper.setLastLoggedInDate(user);
    }

    public void addLoginCookie(JiveAuthentication auth, HttpServletRequest request, HttpServletResponse response) {
        if (JiveGlobals.getJiveBooleanProperty(COOKIE_LOGGED_IN + ".cookie", true)) {
            if (auth == null || auth.isAnonymous()) {
                Cookie cookie = CookieUtils.getCookie(request, COOKIE_LOGGED_IN);
                if (cookie != null) {
                    CookieUtils.deleteCookie(request, response, cookie);
                }
            }
            else {
                Cookie existing = CookieUtils.getCookie(request, COOKIE_LOGGED_IN);
                if (existing == null || !"true".equals(existing.getValue())) {
                    CookieUtils.setCookie(request, response, COOKIE_LOGGED_IN, "true", -1);
                }
            }
        }
    }

    private static final Map<String, String> localAddrNameCache = new HashMap<>();
    private static final Map<String, String> serverInfoLookupCache = new HashMap<>();
    private InitialContext globalContext;

    public void addServerInfoCookie(HttpServletRequest request, HttpServletResponse response) {
        if (JiveGlobals.getJiveBooleanProperty(COOKIE_JIVE_SERVER_INFO + ".cookie", true)) {
            if (globalContext == null) {
                try {
                    globalContext = new InitialContext();
                }
                catch (NamingException e) {
                    // ignored
                }
            }
            StringBuilder builder = new StringBuilder(128);
            builder.append("serverName=");
            builder.append(getServerInfoCookieProperty(globalContext, "serverName", request.getServerName()));
            builder.append(":");

            builder.append("serverPort=");
            builder.append(getServerInfoCookieProperty(globalContext, "serverPort",
                    String.valueOf(request.getServerPort())));
            builder.append(":");

            builder.append("contextPath=");
            builder.append(getServerInfoCookieProperty(globalContext, "contextPath", request.getContextPath()));
            builder.append(":");

            String localAddr = request.getLocalAddr();
            String localName = localAddrNameCache.get(localAddr);
            if (localName == null) {
                // This call goes out to local DNS/hostname resolution routines - kinda expensive
                // If someone changes the local hostname while the server is running, then the server
                // will need to be restarted.
                localName = request.getLocalName();
                localAddrNameCache.put(localAddr, localName);
            }

            builder.append("localName=");
            builder.append(getServerInfoCookieProperty(globalContext, "localName", localName));
            builder.append(":");

            builder.append("localPort=");
            builder.append(getServerInfoCookieProperty(globalContext, "localPort",
                    String.valueOf(request.getLocalPort())));
            builder.append(":");

            builder.append("localAddr=");
            builder.append(getServerInfoCookieProperty(globalContext, "localAddr", localAddr));

            String value = builder.toString();
            Cookie existing = CookieUtils.getCookie(request, COOKIE_JIVE_SERVER_INFO);

            if (existing == null || !existing.getValue().equals(value)) {
                CookieUtils.setCookie(request, response, COOKIE_JIVE_SERVER_INFO, value, -1);
            }

        }
    }

    /**
     * This method will see if a property for the jive.server.info cookie is on the initial context
     * under java:comp/env/jive/{myprop} if not found there it will check jive local properties for
     * the property under jive.server.info.{myprop}
     *
     * @param ctx        used to look up values in the jndi context
     * @param property   The property to lookup
     * @param defaultVal The default value if no values is found
     * @return The value found
     */
    protected String getServerInfoCookieProperty(InitialContext ctx, String property,
                                                 String defaultVal)
    {
        String result = null;
        if (serverInfoLookupCache.containsKey(property)) {
            // might be null
            result = serverInfoLookupCache.get(property);
        }
        else {
            try {
                if (ctx != null) {
                    result = (String) ctx.lookup("java:comp/env/jive/" + property);
                }
            }
            catch (NamingException e) {
                // ignored
            }

            if (result == null) {
                result = JiveGlobals.getLocalProperty(COOKIE_JIVE_SERVER_INFO + "." + property);
            }
            serverInfoLookupCache.put(property, result);
        }

        if (result == null) {
            result = defaultVal;
        }

        return result;
    }

}
