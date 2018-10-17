/*
 * $Revision: 1.1 $
 * $Date: 2017/11/06 06:14:08 $
 *
 * Copyright (C) 1999-2009 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.web.struts;

import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Action;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.web.ServletUtils;
import com.jivesoftware.community.JiveConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

public class JiveLoginInterceptor implements Interceptor {

    private static final Logger log = LogManager.getLogger(JiveLoginInterceptor.class);

    public static final String PARAM_KEY = "__jiveLoginParameters";
    public static final String ACTION_NAME_KEY = "__jiveLoginRedirect";

    public void init() {
    }

    public String intercept(ActionInvocation invocation) throws Exception {
        String result;

        before(invocation);
        result = invocation.invoke();
        return after(invocation, result);
    }

    public void destroy() {
    }

    private String after(ActionInvocation invocation, String result) throws Exception {
        ActionContext context = invocation.getInvocationContext();
        Map session = context.getSession();

        if (session == null) {
            log.error("Unable to retrieve session from ActionInvocation");
            return result;
        }

        // see if the result returned is 'login'
        // if so, set the login success url and put the action's parameters into the session
        if (Action.LOGIN.equals(result)) {
            HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
            request.getSession().setAttribute(JiveConstants.LOGIN_SUCCESS_URL, getPageURL(context));
            Map params = new HashMap();
            Iterator iter = context.getParameters().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();

                if (value != null) {
                    params.put(name, value);
                }
            }

            session.put(PARAM_KEY, params);
            session.put(ACTION_NAME_KEY, context.getName());
        }

        return result;
    }

    private void before(ActionInvocation invocation) throws Exception {
        String currentActionName = invocation.getInvocationContext().getName();
        ActionContext context = invocation.getInvocationContext();
        Map session = context.getSession();

        if (session == null) {
            log.error("Unable to retrieve session from ActionInvocation");
            return;
        }

        // check to see if the action url == the url in the session key
        // if so, set the parameters and clean the session entry
        else if (currentActionName.equals(session.get(ACTION_NAME_KEY))) {
            Map params = (Map) session.get(PARAM_KEY);
            if (params != null) {
                context.getParameters().putAll(params);
            }
            else {
                log.warn("Params map was null after returning from login action");
            }
            // remove the key from the session
            session.remove(ACTION_NAME_KEY);
            session.remove(PARAM_KEY);
        }

        else if (invocation.getAction() instanceof JiveActionSupport) {
            JiveActionSupport action = (JiveActionSupport) invocation.getAction();
            if (action.getAuthToken() == null || action.getAuthToken().isAnonymous()) {
                // ignore login / logout actions to avoid redirecting back to login / logout when complete
                if (!currentActionName.equals("opensearch") && !currentActionName.equals("login") &&
                        !currentActionName.equals("logout") && !currentActionName.equals("account") &&
                        !currentActionName.equals("create-account") && !currentActionName.equals("emailPasswordToken") &&
                        !currentActionName.equals("resetPassword") && !currentActionName.equals("validate")
                        && !currentActionName.equals("help")) {
                    String pageURL = getPageURL(context);
                    session.put(JiveConstants.LOGIN_SUCCESS_URL, pageURL);
                    session.put(JiveConstants.LOGIN_CANCEL_URL, pageURL);
                }
            }
        }
    }

    /**
     * Returns the URL of the current page as a string. This only returns the url and it's get
     * parameters, not all the parameters of the action.
     *
     * @param ctx the action context
     * @return the URL of the current page
     */
    public String getPageURL(ActionContext ctx) {
        HttpServletRequest request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
        // Retrieve the url of the current page
        StringBuffer page = new StringBuffer();
        page.append(ServletUtils.getServletPath(request));
        String queryString = request.getQueryString();
        if (queryString != null && !"".equals(queryString.trim())) {
            page.append('?').append(queryString);
        }

        return page.toString();
    }

}
