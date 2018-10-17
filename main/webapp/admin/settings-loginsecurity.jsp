<%--
  - $Revision: 1.1 $
  - $Date: 2017/11/06 06:14:09 $
  -
  - Copyright (C) 1999-2008 Jive Software. All rights reserved.
  - This software is the proprietary information of Jive Software. Use is subject to license terms.
--%>

<%@ page import="com.jivesoftware.community.JiveGlobals,
                 com.jivesoftware.util.DateUtils,
                 com.jivesoftware.community.*,
                 com.jivesoftware.community.action.util.ParamUtils,
                 java.util.*,
                 com.jivesoftware.community.impl.dao.DateRangeBean,
                 com.jivesoftware.base.*,
                 com.jivesoftware.community.web.struts.JiveTokenInterceptor"
         errorPage="error.jsp"
        %>

<%@ include file="include/global.jspf" %>

<%@ taglib uri="jivetags" prefix="jive" %>
<%@ taglib uri="struts-tags" prefix="s" %>

<jsp:useBean id="jivepageinfo" scope="request" class="com.jivesoftware.base.admin.AdminPageBean"/>

<%

    // Permission check
    if (!isSystemAdmin) {
        throw new UnauthorizedException("You don't have admin privileges to perform this operation.");
    }

    String tokenName = "admin.login.security.token";
    String token = request.getParameter(tokenName);

    // get boolean parameters (button pressed)
    boolean save = request.getParameter("save") != null;

    // setting parameters
    boolean isLoginThrottleEnabled = ParamUtils.getBooleanParameter(request, "isLoginThrottleEnabled");
    int loginThrottleDelay = ParamUtils.getIntParameter(request, "loginThrottleDelay", 10);
    if(loginThrottleDelay <= 0){
        loginThrottleDelay = 3;
    }

    int loginThrottleAttempts = ParamUtils.getIntParameter(request, "loginThrottleAttempts", 3);
    if(loginThrottleAttempts <= 0){
        loginThrottleAttempts = 1;
    }

    boolean isLoginCaptchaEnabled = ParamUtils.getBooleanParameter(request, "isLoginCaptchaEnabled");
    int captchaSize = ParamUtils.getIntParameter(request, "captchaSize", 8);
    String message = "";
    if (save) {
        checkForPost(request);
        boolean isValidToken = JiveTokenInterceptor.isValidToken(tokenName, token);
        if (!isValidToken) {
            throw new UnauthorizedException("Invalid token provided");
        }

        JiveGlobals.setJiveProperty("login.throttle.enabled", String.valueOf(isLoginThrottleEnabled));
        JiveGlobals.setJiveProperty("login.throttle.delay", String.valueOf(loginThrottleDelay));
        JiveGlobals.setJiveProperty("login.throttle.maxContinuousAttempts", String.valueOf(loginThrottleAttempts));
        JiveGlobals.setJiveProperty("login.captcha.enabled", String.valueOf(isLoginCaptchaEnabled));
        JiveGlobals.setJiveProperty("login.captcha.size", String.valueOf(captchaSize));
        message = "success";
    }
    else {
        isLoginThrottleEnabled = JiveGlobals.getJiveBooleanProperty("login.throttle.enabled", false);
        loginThrottleDelay = JiveGlobals.getJiveIntProperty("login.throttle.delay", 10);
        loginThrottleAttempts = JiveGlobals.getJiveIntProperty("login.throttle.maxContinuousAttempts", 3);
        isLoginCaptchaEnabled = JiveGlobals.getJiveBooleanProperty("login.captcha.enabled", false);
        captchaSize = JiveGlobals.getJiveIntProperty("login.captcha.size", 8);
    }

    // Title of this page and breadcrumbs
    String title = "Login Security Settings";
    String tokenGuid = JiveTokenInterceptor.setToken(tokenName);
%>
<html>
<head>
    <title><%= title %>
    </title>
    <style type="text/css">
        select, input {
            font-family: verdana, arial;
            font-size: 8pt;
        }

        .date {
            color: #00f;
            border: 0 0 1px;
            border-style: dotted;
            border-color: #00f;
        }

        .buttons TD {
            padding: 3px;
        }

        .buttons .icon-label {
            padding-right: 1em;
        }

        .log-info {
            border: 0px 1px 1px;
            border-color: #ccc;
            border-style: solid;
        }

        iframe {
            border: 1px #666 solid;
        }
    </style>

    <content tag="pagetitle"><%= title %>
    </content>
    <content tag="pageID">settings-loginsecurity</content>
    <content tag="pagehelp">
        <h3>Help Section Here</h3>

        <p>
            Login Security settings can be modified here..
        </p>
    </content>
</head>
<body>

<%
    String errorMessage = "";
    if ("success".equals(message)) {
        errorMessage = "Settings saved successfully.";
    }

%>

<jsp:include page="include/message.jsp">
    <jsp:param name="type" value="success"/>
    <jsp:param name="message" value="<%= errorMessage %>"/>
</jsp:include>

<div id="logviewer">

    <form action="settings-loginsecurity.jsp" name="loginSecurityForm" method="post">
        <input type="hidden" name="<%= tokenName %>" value="<%= tokenGuid %>" />
        <input type="hidden" name="save" values="save">
        <div class="jive-table">
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <thead>
                <tr>
                    <th colspan="2">Login Security Settings</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>Login Throttling</td>
                    <td>
                        <table>
                            <tr><td>
                                <input type="checkbox" id="isLoginThrottleEnabled" name="isLoginThrottleEnabled" <% if(isLoginThrottleEnabled){ %>checked<% } %> value="true" onclick="switchLoginThrottleOptions();"/>
                            </td>
                                <td>Enabled</td>
                            </tr>
                            <tr>
                                <td><input type="text" id="loginThrottleAttempts" name="loginThrottleAttempts" value="<%=loginThrottleAttempts%>" size="2" maxlength="2"/></td>
                                <td>Number of failed attempts before forced delay. Must be a value greater than 0</td>
                            </tr>
                            <tr>
                                <td><input type="text" id="loginThrottleDelay" name="loginThrottleDelay" value="<%=loginThrottleDelay%>" size="3" maxlength="3"/></td>
                                <td >Forced delay in seconds. Must be a value greater than 0</td>

                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td>Login Captcha</td>
                    <td>
                        <table>
                            <tr>
                                <td><input type="checkbox" id="isLoginCaptchaEnabled" name="isLoginCaptchaEnabled" <% if(isLoginCaptchaEnabled){ %>checked<% } %> value="true" onclick="switchCaptchaOptions();"/>
                                </td>
                                <td>Enabled</td>
                            </tr>
                            <tr>
                                <td><input type="text" id="captchaSize" name="captchaSize" value="<%=captchaSize%>" size="2" maxlength="3"/></td>
                                <td>Captcha size</td>
                            </tr>

                        </table>
                    </td>
                </tr>
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="2"><input type="submit" name="save" value="Save Settings"/></td>
                </tr>
                </tfoot>
            </table>
        </div>
    </form>
</div>
<br/>
<script type="text/javascript" language="JavaScript">
    function switchLoginThrottleOptions() {
        var enabled = document.getElementById('isLoginThrottleEnabled').checked;
        var elements = ["loginThrottleAttempts", "loginThrottleDelay", "isLoginCaptchaEnabled"];
        for(var i = 0; i <elements.length ; i++){
            document.getElementById(elements[i]).disabled = !enabled;
            if(!enabled){
                if(document.getElementById(elements[i]).checked){
                    document.getElementById(elements[i]).checked = false;
                }
            }
        }
    }
    function switchCaptchaOptions() {
        var enabled = document.getElementById('isLoginCaptchaEnabled').checked;
        var elements = ["captchaSize"];
        for(var i = 0; i <elements.length ; i++){
            document.getElementById(elements[i]).disabled = !enabled;
        }
    }
    switchLoginThrottleOptions();
    switchCaptchaOptions();
</script>


</body>
</html>