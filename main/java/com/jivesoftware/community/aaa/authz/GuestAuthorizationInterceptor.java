/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.aaa.authz;

import com.jivesoftware.base.aaa.AuthenticationProvider;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.aaa.AccessManager;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.ActionUtils;
import com.jivesoftware.community.action.util.AlwaysAllowAnonymous;
import com.jivesoftware.community.action.util.AlwaysDisallowAnonymous;
import com.jivesoftware.community.web.struts.util.ActionInvocationUtils;
import com.jivesoftware.util.URLUtils;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Struts2 interceptor for performing basic authorization checks on the incoming request. For CS2.0, this is largely
 * action driven. This interceptor will accommodate action-driven authz, but any FTL or JSP access will not be guarded
 * by this interceptor. This is the successor to the older AuthTokenInterceptor.
 *
 * @since 2.0
 */
public class GuestAuthorizationInterceptor implements Interceptor {

    private AuthenticationProvider authProvider;
    private HttpServletRequest httpServletRequest;
    private AccessManager accessManager;
    
   

    public String intercept(ActionInvocation ai) throws Exception {

        JiveAuthentication auth = authProvider.getAuthentication();

        //add the authentication to the invocation context
        ai.getInvocationContext().put("authentication", auth);
        
        HttpServletRequest request = this.getHttpServletRequest();
        String requestURI = URLUtils.toContextRelativeUrl(request.getRequestURI());
        
        if(requestURI.contains("osp!input.jspa"))
        {
        	
        	return ai.invoke();
        }

        if (auth.isAnonymous()) {
            // example: internal community like Brewspace but the action we're requesting doesn't allow anonymous
            // requests (ie: login action does, edit profile does not)
            if (!accessManager.isGuestAccessAllowed() && !alwaysAllowsAnonymousOnAction(ai)) {
                return this.writeRefererUriAsRequestParameter(ai);
            }

            // example: external community like discussions.apple.com but the action we're requesting requires
            // an authenticated user
            if (accessManager.isGuestAccessAllowed() && alwaysRequiresAuthenticatedUserOnAction(ai)) {
                return this.writeRefererUriAsRequestParameter(ai);
            }
        }

        return ai.invoke();
    }

    String writeRefererUriAsRequestParameter(ActionInvocation ai) throws UnsupportedEncodingException {
        HttpServletRequest request = this.getHttpServletRequest();
        String requestURI = URLUtils.toContextRelativeUrl(request.getRequestURI());

        if (request.getQueryString() != null) {
            requestURI += "?" + request.getQueryString();
        }

       /* if(requestURI.contains("osp!input.jspa"))
        {
        	
        	return "osppage";
        }*/
        // Encoded twice intentionally to preserve the original form during decode
        requestURI = URLEncoder.encode(requestURI, JiveGlobals.getCharacterEncoding());
        requestURI = URLEncoder.encode(requestURI, JiveGlobals.getCharacterEncoding());

        ai.getInvocationContext().put(JiveConstants.REFERER_KEY, requestURI);
        request.setAttribute(JiveConstants.REFERER_KEY, requestURI);

       
        return JiveActionSupport.UNAUTHENTICATED;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    HttpServletRequest getHttpServletRequest() {
        if (this.httpServletRequest == null) {
            return ActionUtils.getHttpServletRequest();
        }
        return this.httpServletRequest;
    }

    public void setAuthenticationProvider(AuthenticationProvider authProvider) {
        this.authProvider = authProvider;
    }

    /*
     * Under some circumstances, we want to allow requests to proceed to actions EVEN if the deployment doesn't
     * allow anonymous users. Example: login action must allow anonymous users or else no one would be able to login.
     *
     */
    protected boolean alwaysAllowsAnonymousOnAction(ActionInvocation actionInvocation) {
        return ActionInvocationUtils.isAnnotationPresent(AlwaysAllowAnonymous.class, actionInvocation);
    }

    /*
     * Under no circumstances should a request proceed to this action without having an authenticated user.  Example:
     * you have to be logged in to edit a profile or create a container.
     *
     */
    protected boolean alwaysRequiresAuthenticatedUserOnAction(ActionInvocation actionInvocation) {
        return ActionInvocationUtils.isAnnotationPresent(AlwaysDisallowAnonymous.class, actionInvocation);
    }

    public void init() {
        //no-op
    }

    public void destroy() {
        //no-op
    }

    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

}
