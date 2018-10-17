<%--
  ~ $Revision: 1.1 $
  ~ $Date: 2017/11/06 06:14:09 $
  ~
  ~ Copyright (C) 1999-2010 Jive Software. All rights reserved.
  ~
  ~ This software is the proprietary information of Jive Software. Use is subject to license terms.
  --%>

<%@ page import="com.jivesoftware.community.mail.EmailMessage,
                 com.jivesoftware.community.util.SkinUtils,
                 com.jivesoftware.community.web.struts.JiveTokenInterceptor,
                 com.jivesoftware.util.ValidationUtil,
                 com.jivesoftware.community.RegistrationManager,
                 com.jivesoftware.community.user.registration.UserAccountFormValidator,
                 com.jivesoftware.community.validation.form.FormField,
                 com.jivesoftware.community.validation.form.FormFieldError,
                 com.jivesoftware.community.validation.form.KeyedFormFieldError,
                 com.jivesoftware.community.valuecase.ValueCaseManager,
                 org.apache.commons.lang.StringEscapeUtils,
                 java.util.HashMap"
    contentType="text/html"
    errorPage="error.jsp"
%>

<%@ page import="java.util.Map" %>
<%@ page import="com.jivesoftware.community.externalcollaboration.SharedGroupManager" %>
<%@ page import="com.jivesoftware.community.eae.EAEUnavailableException" %>

<%@ page import="java.util.HashMap" %>
<%@ page import="com.grail.synchro.dwr.service.UserDepartmentsService" %>
<%@ page import="com.grail.synchro.beans.UserDepartment" %>
<%@ page import="com.grail.synchro.SynchroGlobal" %>
<%@ page import="com.grail.synchro.SynchroConstants" %>
<%@ page import="com.grail.synchro.util.SynchroUserPropertiesUtil" %>

<%@ taglib uri="struts-tags" prefix="s" %>

<%@ include file="include/global.jspf" %>

<% // Permission check
    accessHelper.assertPageAccess("usersgroups-newuser");

    // get parameters
    String name = ParamUtils.getParameter(request, "name");
    if(name != null) {
        name = name.trim();
    }
    String firstName = ParamUtils.getParameter(request, "firstName");
    if(firstName != null) {
        firstName = firstName.trim();
    }
    String lastName = ParamUtils.getParameter(request, "lastName");
    if(lastName != null) {
        lastName = lastName.trim();
    }
    String email = ParamUtils.getParameter(request, "email");
    String username = ParamUtils.getParameter(request, "username");
    String password = ParamUtils.getParameter(request, "password");
    String passwordconfirm = ParamUtils.getParameter(request, "passwordconfirm");
    boolean createanother = request.getParameter("createanother") != null;
    boolean create = request.getParameter("create") != null || createanother;
    boolean welcomeEmail = request.getParameter("welcomeEmail") != null;
    String typeAsString = ParamUtils.getParameter(request, "type");

    // user first name, last name or full name
    boolean lastNameFirstNameEnabled = JiveGlobals.getJiveBooleanProperty(JiveConstants.USER_LAST_NAME_FIRST_NAME_ENABLED);

    JiveLicenseManagerImpl licenseManager = jiveContext.getLicenseManager();

    if (request.getParameter("cancel") != null) {
        response.sendRedirect("user-search.jspa");
        return;
    }
    RegistrationManager registrationManager = (RegistrationManager) jiveContext.getSpringBean("registrationManager");
    UserAccountFormValidator userAccountFormValidator = (UserAccountFormValidator) jiveContext.getSpringBean("userAccountFormValidator");
    SharedGroupManager sharedGroupManager = jiveContext.getSharedGroupManager();
    boolean isSharedGroupsEnabled = sharedGroupManager.isEnabled();

    UserManager iUserManager = jiveContext.getUserManager();

    String tokenName = "user.create.token";
    String token = request.getParameter(tokenName);

    Map<String,String> errors = new HashMap<String,String>();
    if (create) {
        boolean isValidToken = JiveTokenInterceptor.isValidToken(tokenName, token);
        if (!isValidToken) {
            throw new UnauthorizedException("Invalid token provided");
        }

        checkForPost(request);
        Logger log = LogManager.getLogger("com.jivesoftware");

        // Validate
        if (!registrationManager.isUsernameIsEmail()) {
            if (username == null || "".equals(username.trim())) {
                errors.put("username", "");
            } else {
                // check for invalid characters
                if (!ValidationUtil.isUsernameValid(username.trim())) {
                    errors.put("usernameChar", "");
                }
            }
        }

        if (lastNameFirstNameEnabled) {
            if (firstName == null || "".equals(firstName.trim()) || firstName.trim().length() < 1) {
                errors.put("firstName", "");
            }
            else if(!StringUtils.isWebSafeString(firstName)) {
                errors.put("firstNameInvalid", "");
            }
            if (lastName == null || "".equals(lastName.trim()) || lastName.trim().length() < 1) {
                errors.put("lastName", "");
            }
            else if(!StringUtils.isWebSafeString(lastName)) {
                errors.put("lastNameInvalid", "");
            }
        }
        else {
            if (name == null || "".equals(name.trim()) || name.trim().length() < 1) {
                errors.put("name", "");
            }
            else if(!StringUtils.isWebSafeString(name)) {
                errors.put("nameInvalid", "");
            }
        }
        try{
            email = StringUtils.validateEmailAddress(email);
        }catch(InvalidEmailException iee){
            errors.put("email", "");
        }
        if (password == null || "".equals(password.trim()) || password.trim().length() < 4) {
            errors.put("password", "");
        }
        if (passwordconfirm == null || "".equals(passwordconfirm.trim())
                || passwordconfirm.trim().length() < 4)
        {
            errors.put("passwordconfirm", "");
        }
        if (password != null && passwordconfirm != null && !password.equals(passwordconfirm)) {
            errors.put("passwordmatch", "");
        }
        if (errors.size() == 0) {
            UserManager userManager = jiveContext.getUserManager();
            try {
                UserTemplate template;
                if (registrationManager.isUsernameIsEmail()) {
                    username = email;
                }
                if (lastNameFirstNameEnabled) {
                    template = new UserTemplate(username, password, email, firstName, lastName);
                }
                else {
                    template = new UserTemplate(username, password, email, name);
                }
                template.setType(User.Type.getById(Integer.parseInt(typeAsString)));
                template.setStatus(User.Status.registered);
                User newUser = userManager.createUser(template);

                if (welcomeEmail) {
                   // ValueCaseManager valueCaseManager = (ValueCaseManager) jiveContext.getSpringBean("valueCaseManager");
                   // valueCaseManager.sendWelcomeEmail(newUser, password);
				    EmailMessage message = new EmailMessage();
                    message.setTextBodyProperty("usercreation.welcome.email.textBody");
                    message.setHtmlBodyProperty("usercreation.welcome.email.htmlBody");
                    message.setSubjectProperty("usercreation.welcome.email.subject");
                    message.addRecipient(SkinUtils.getDisplayName(newUser), newUser.getEmail());
                    message.getContext().put("newUser", newUser);
                    message.getContext().put("newUserDisplayName", SkinUtils.getUserDisplayName(newUser.getUsername()));
                    message.getContext().put("newUserUsername", newUser.getUsername());
                    message.getContext().put("jiveURL", JiveGlobals.getJiveProperty("jiveURL"));
                    message.getContext().put("password", password);
                    jiveContext.getEmailManager().send(message);
                }

                if (createanother) {
                    response.sendRedirect("user-create.jsp?success=true");
                }
                else {
                    response.sendRedirect(
                            "editUserProfile!input.jspa?userId=" + newUser.getID() + "&success=true");
                }
                return;
            }
            catch (UserAlreadyExistsException uaee) {
                errors.put("alreadyexists", "");
            }
            catch (EmailAlreadyExistsException uaee) {
                errors.put("emailAlreadyExists", "");
            }
            catch (UnauthorizedException ue) {
                errors.put("noperms", "");
            }
            catch (EAEUnavailableException e) {
                log.error("Exception attempting to create user.", e);
                errors.put("global", "The Enterprise Activity Engine service must be available to perform this action.");
            }
            catch(Exception ex) {
                errors.put("global", "Failed to add user: '" + ex.getLocalizedMessage() + "'.");
                log.error("Exception attempting to create user.", ex);
            }
        }
    }

    // Title of this page and breadcrumbs
    String title = "Create User";

    String tokenGuid = JiveTokenInterceptor.setToken(tokenName);
%>

<head>
	<link rel="stylesheet" href="<s:url value='/admin/style/synchro-admin.css'/>">
    <title><%= title %></title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">usersgroups-newuser</content>
    <content tag="pagehelp">
        <h3>Help Section Here</h3>
        <p>This is the help section of the page.</p>
    </content>
</head>
<body>

<s:action name="license-notify" executeResult="true" >
    <s:param name="formResult">user-create.jsp</s:param>
</s:action>

<% if (!licenseManager.isSeatStatusBlocked() && iUserManager.isCreationSupported()) { %>

<% if (registrationManager.isUsernameIsEmail()) { %>
    <div id="jive-info-box" class="jive-info-box">
        <span class="jive-icon-med jive-icon-info"></span>
        Your system is configured to use a user's email address as their user name.
    </div>
<% } %>

<p>
This creates a user with no permissions and default privacy settings.
Once you create this user, you should edit their properties. To create the user and
go to their properties page click "Create User". To create a user then return to this
form click "Create &amp; Create Another User".
</p>

<% if (!registrationManager.isUsernameIsEmail()) { %>
    <p>Note that a user name may not contain
    <% if (!JiveGlobals.getJiveBooleanProperty("username.allowWhiteSpace",false)) { %>
    whitespace or
    <%	} %>
    any of the following characters:

    <strong>
    <% for (char c : ValidationUtil.getUsernameDisallowedChars()) {%>
        <%=c%>&nbsp;
    <%}%>
    </strong></p>
<% } %>

<%  if (errors.size() > 0) { %>

    <div class="jive-error">
    <table cellpadding="0" cellspacing="0" border="0">
    <tbody>
        <tr><td class="jive-icon"><img src="images/error-16x16.gif" width="16" height="16" border="0" alt="" /></td>
        <td class="jive-icon-label">
            <% if (errors.containsKey("global")) { %>
                <%= errors.get("global") %>
            <% } else if (errors.containsKey("noperms")) { %>
                Error -- you don't have permission to create a user.
            <%  } else { %>
                Error creating the user -- please check the fields below.
            <%  } %>
        </td></tr>
    </tbody>
    </table>
    </div><br />

<%	} %>

<%  if ("true".equals(request.getParameter("success"))) { %>

    <div class="jive-success">
    <table cellpadding="0" cellspacing="0" border="0">
    <tbody>
        <tr><td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0" alt="" /></td>
        <td class="jive-icon-label">
            User created successfully.
        </td></tr>
    </tbody>
    </table>
    </div><br />

<%	} %>

<%-- form --%>
<form action="user-create.jsp" method="post" name="createForm">

<input type="hidden" name="<%= tokenName %>" value="<%= org.apache.commons.lang3.StringEscapeUtils.escapeXml(tokenGuid) %>" />
<% if (!isSharedGroupsEnabled) { %>
    <input type="hidden" name="type" value="<%= User.Type.REGULAR.getId() %>" />
<% } %>


    <div class="jive-table">
<table cellpadding="0" cellspacing="0" border="0" width="100%">
<thead>
    <tr>
        <th>&nbsp;</th>
        <th>&nbsp;</th>
    </tr>
</thead>
<tbody>
    <% if (!registrationManager.isUsernameIsEmail()) { %>
    <tr>
        <td>Username:</td>
        <td>
            <input type="text" size="40" maxlength="254" name="username" value="<%= (username != null ? (username.equals("admin") ? "" : StringEscapeUtils.escapeHtml(username)) : "") %>">
            <%  if (errors.containsKey("username")) { %>
                <span class="jive-error-text">
                <br /> Please enter a valid username.
                </span>
            <%  } else if (errors.containsKey("alreadyexists")) { %>
                <span class="jive-error-text">
                <br /> Please choose a different username - this one already exists.
                </span>
            <%  } else if (errors.containsKey("usernameChar")) { %>
                <span class="jive-error-text">
                <br/> Invalid username. Make sure that your username does not contain any of the following characters:
                    <strong>
                        <% for (char c : ValidationUtil.getUsernameDisallowedChars()) {%>
                        <%=c%>&nbsp;
                        <%}%>
                    </strong>.
                </span>
            <% } %>
        </td>
    </tr>
    <%  } %>

    <% if (lastNameFirstNameEnabled) { %>

    <tr>
        <td>First Name:</td>
        <td>
            <input type="text" size="30" maxlength="100" name="firstName" value="<%= (firstName != null ? StringEscapeUtils.escapeHtml(firstName) : "") %>">
            <%  if (errors.containsKey("firstName")) { %>
                <span class="jive-error-text">
                <br /> Please enter a first name.
                </span>
            <%  } %>
            <%  if (errors.containsKey("firstNameInvalid")) { %>
                <span class="jive-error-text">
                <br/> Invalid first name. Make sure that your first name contains only alphanumerics, spaces, and some special characters, <%= StringUtils.OTHER_SAFE_CHARACTERS %>
                </span>
            <%  } %>
        </td>
    </tr>
    <tr>
        <td>Last Name:</td>
        <td>
            <input type="text" size="30" maxlength="100" name="lastName" value="<%= (lastName != null ? StringEscapeUtils.escapeHtml(lastName) : "") %>">
            <%  if (errors.containsKey("lastName")) { %>
                <span class="jive-error-text">
                <br /> Please enter a last name.
                </span>
            <%  } %>
            <%  if (errors.containsKey("lastNameInvalid")) { %>
                <span class="jive-error-text">
                <br/> Invalid last name. Make sure that your last name contains only alphanumerics, spaces, and some special characters, <%= StringUtils.OTHER_SAFE_CHARACTERS %>
                </span>
            <%  } %>
        </td>
    </tr>

    <% } else { %>

    <tr>
        <td>Name:</td>
        <td>
            <input type="text" size="30" maxlength="100" name="name" value="<%= (name != null ? StringEscapeUtils.escapeHtml(name) : "") %>">
            <%  if (errors.containsKey("name")) { %>
                <span class="jive-error-text">
                <br /> Please enter a name.
                </span>
            <%  } %>
            <%  if (errors.containsKey("nameInvalid")) { %>
                <span class="jive-error-text">
                <br/> Invalid name. Make sure that your name contains only alphanumerics, spaces, and some special characters, <%= StringUtils.OTHER_SAFE_CHARACTERS %>
                </span>
            <%  } %>

        </td>
    </tr>

    <% } %>


    <tr>
        <td>Email:</td>
        <td>
            <input type="text" size="40" maxlength="254" name="email" value="<%= (email != null ? StringEscapeUtils.escapeHtml(email) : "") %>">
            <%  if (errors.containsKey("email")) { %>
                <span class="jive-error-text">
                <br /> Please enter a valid email address.
                </span>
            <%  } else if (errors.containsKey("emailAlreadyExists")) { %>
                <span class="jive-error-text">
                <br /> Please choose a different email address - this one already exists.
                </span>
            <%  } %>
        </td>
    </tr>
    <% if (isSharedGroupsEnabled) { %>
    <tr>
        <td>User Type:</td>
        <td>
            <select id="type" name="type">
                <option selected="selected" value="<%= User.Type.REGULAR.getId() %>">Standard</option>
                <option value="<%= User.Type.PARTNER.getId() %>">External contributor</option>
            </select>
        </td>
    </tr>
    <%  } %>
    <tr>
        <td>Password:</td>
        <td>
            <input type="password" size="20" maxlength="100" name="password" value="" autocomplete="off">
            <%  if (errors.containsKey("password")) { %>
                <span class="jive-error-text">
                <br /> Please enter a password of at least 4 characters.
                </span>
            <%  } %>
        </td>
    </tr>
    <tr>
        <td>Confirm Password:</td>
        <td>
            <input type="password" size="20" maxlength="100" name="passwordconfirm" value="" autocomplete="off">
            <%  if (errors.containsKey("passwordconfirm")) { %>
                <span class="jive-error-text">
                <br /> Please confirm the password.
                </span>
            <%  } else if (errors.containsKey("passwordmatch")) { %>
                <span class="jive-error-text">
                <br /> Passwords don't match.
                </span>
            <%  } %>
        </td>
    </tr>
    <tr>
        <td>Send Welcome Email:</td>
        <td>
            <input type="checkbox" name="welcomeEmail" value="true"/>
        </td>
    </tr>
	
	<!-- Customization starts -->
<%--<tr class="synchro-user-profile-fields-portaltype">--%>
    <%--<td class="label">Portal Type:</td>--%>
    <%--<td>--%>
        <%--<input id="irisPortalType" type="radio" value="iris" name="portalType" checked="true" onchange="portalTypeChange(this)"><label for="irisPortalType">IRIS</label>--%>
        <%--<input id="synchroPortalType" type="radio" value="synchro" name="portalType" onchange="portalTypeChange(this)"><label for="synchroPortalType">Synchro</label>--%>
    <%--</td>--%>
    <%--<script type="text/javascript">--%>
        <%--function portalTypeChange(target) {--%>
            <%--if(target.value == 'synchro') {--%>
                <%--$j(".synchro-user-profile-fields").show();--%>
            <%--} else {--%>
                <%--$j(".synchro-user-profile-fields").hide();--%>
            <%--}--%>
        <%--}--%>
    <%--</script>--%>
<%--</tr>--%>
<%--<tr class="synchro-user-profile-fields" style="display: none;">--%>
    <%--<td>Departments:</td>--%>
    <%--<td>--%>
        <%--<div>--%>
            <%--<div class="form-select_div">--%>
                <%--<label><span class="list">All Departments</span></label>--%>
                <%--<select size="3" id="all-departments" name="all-departments" class="all" multiple="true">--%>
                    <%--<%--%>
                        <%--UserDepartmentsService service = JiveApplication.getContext().getSpringBean("userDepartmentsService");--%>
                        <%--List<UserDepartment> departments = service.getAll();--%>
                        <%--if(departments != null && departments.size() > 0) {--%>
                            <%--for(UserDepartment department: departments) {--%>
                    <%--%>--%>
                    <%--<option value="<%=department.getId()%>"><%=department.getName()%></option>--%>
                    <%--<%--%>
                            <%--}--%>
                        <%--}--%>
                    <%--%>--%>
                <%--</select>--%>
                <%--<div class="action_buttons">--%>
                    <%--<input id="add-departments" type="button" value=">>" class="left_arrow">--%>
                    <%--<input id="remove-departments" type="button" value="<<" class="right_arrow">--%>
                <%--</div>--%>
            <%--</div>--%>

            <%--<div class="form-select_div_brand">--%>
                <%--<label><span class="list">Selected Departments</span></label>--%>
                <%--<select id="selected-departments" name="selected-departments" multiple="true" size="3" class="selected">--%>
                <%--</select>--%>
            <%--</div>--%>
            <%--<script type="text/javascript">--%>
                <%--$j(function() {--%>
                    <%--$j("#add-departments, #remove-departments").click(function(event) {--%>
                        <%--var id = $j(event.target).attr("id");--%>
                        <%--var selectFrom = id == "add-departments" ? "#all-departments" : "#selected-departments";--%>
                        <%--var moveTo = (id == "add-departments") ? "#selected-departments" : "#all-departments";--%>
                        <%--var selectedItems = $j(selectFrom + " :selected").toArray();--%>
                        <%--$j(moveTo).append(selectedItems);--%>
                    <%--});--%>
                <%--});--%>
            <%--</script>--%>
        <%--</div>--%>
        <%--&lt;%&ndash;<select id="userDepartments" name="userDepartments" multiple="true" size="3">&ndash;%&gt;--%>
        <%--&lt;%&ndash;&lt;%&ndash;%>--%>
        <%--&lt;%&ndash;UserDepartmentsService service = JiveApplication.getContext().getSpringBean("userDepartmentsService");&ndash;%&gt;--%>
        <%--&lt;%&ndash;List<UserDepartment> departments = service.getAll();&ndash;%&gt;--%>
        <%--&lt;%&ndash;if(departments != null && departments.size() > 0) {&ndash;%&gt;--%>
        <%--&lt;%&ndash;for(UserDepartment department: departments) {%>&ndash;%&gt;--%>
        <%--&lt;%&ndash;<option value="<%=department.getId()%>"><%=department.getName()%></option>&ndash;%&gt;--%>
        <%--&lt;%&ndash;&lt;%&ndash;%>--%>
        <%--&lt;%&ndash;}&ndash;%&gt;--%>
        <%--&lt;%&ndash;}&ndash;%&gt;--%>
        <%--&lt;%&ndash;%>&ndash;%&gt;--%>
        <%--&lt;%&ndash;</select>&ndash;%&gt;--%>
    <%--</td>--%>
<%--</tr>--%>
<%--<tr class="synchro-user-profile-fields" style="display: none;">--%>
    <%--<td>Brands:</td>--%>
    <%--<td>--%>
        <%--<div>--%>
            <%--<div class="form-select_div">--%>
                <%--<label><span class="list">All Brands</span></label>--%>
                <%--<select size="3" id="all-brands" class="all" multiple="true">--%>
                    <%--<%--%>
                        <%--Map<Integer, String> brands = SynchroGlobal.getBrands();--%>
                        <%--Set<Integer> brandKeys = brands.keySet();--%>
                        <%--for(Integer brandKey: brandKeys) {--%>
                    <%--%>--%>
                    <%--<option value="<%=brandKey%>"><%=brands.get(brandKey)%></option>--%>
                    <%--<%--%>
                        <%--}--%>
                    <%--%>--%>
                <%--</select>--%>
                <%--<div class="action_buttons">--%>
                    <%--<input id="add-brands" type="button" value=">>" class="left_arrow">--%>
                    <%--<input id="remove-brands" type="button" value="<<" class="right_arrow">--%>
                <%--</div>--%>
            <%--</div>--%>

            <%--<div class="form-select_div_brand">--%>
                <%--<label><span class="list">Selected Brands</span></label>--%>
                <%--<select id="selected-brands" name="selected-brands" multiple="true" size="3" class="selected">--%>
                <%--</select>--%>
            <%--</div>--%>
            <%--<script type="text/javascript">--%>
                <%--$j(function() {--%>
                    <%--$j("#add-brands, #remove-brands").click(function(event) {--%>
                        <%--var id = $j(event.target).attr("id");--%>
                        <%--var selectFrom = id == "add-brands" ? "#all-brands" : "#selected-brands";--%>
                        <%--var moveTo = (id == "add-brands") ? "#selected-brands" : "#all-brands";--%>
                        <%--var selectedItems = $j(selectFrom + " :selected").toArray();--%>
                        <%--$j(moveTo).append(selectedItems);--%>
                    <%--});--%>
                <%--});--%>
            <%--</script>--%>
        <%--</div>--%>
    <%--</td>--%>
<%--</tr>--%>
<%--<tr class="synchro-user-profile-fields" style="display: none;">--%>
    <%--<td class="label">Access:</td>--%>
    <%--<td>--%>
        <%--<div>--%>
            <%--<input id="globalAccessType" type="radio" value="<%=SynchroGlobal.InvestmentType.GlOBAL.getId()%>" name="accessTypeGroup" checked="true" onchange="accessTypeChange(this)">--%>
            <%--<label for="globalAccessType"><%=SynchroGlobal.InvestmentType.GlOBAL.getDescription()%></label>--%>
            <%--<input id="regionalAccessType" type="radio" value="<%=SynchroGlobal.InvestmentType.REGION.getId()%>" name="accessTypeGroup" onchange="accessTypeChange(this)">--%>
            <%--<label for="regionalAccessType"><%=SynchroGlobal.InvestmentType.REGION.getDescription()%></label>--%>
            <%--<input id="areaAccessType" type="radio" value="<%=SynchroGlobal.InvestmentType.AREA.getId()%>" name="accessTypeGroup"  onchange="accessTypeChange(this)">--%>
            <%--<label for="areaAccessType"><%=SynchroGlobal.InvestmentType.AREA.getDescription()%></label>--%>
            <%--<input id="countryAccessType" type="radio" value="<%=SynchroGlobal.InvestmentType.COUNTRY.getId()%>" name="accessTypeGroup" onchange="accessTypeChange(this)">--%>
            <%--<label for="countryAccessType"><%=SynchroGlobal.InvestmentType.COUNTRY.getDescription()%></label>--%>
            <%--<script type="text/javascript">--%>
                <%--$j(document).ready(function(){--%>
                    <%--accessTypeChange(null)--%>
                <%--});--%>
                <%--function accessTypeChange(target) {--%>
                    <%--if(target != null) {--%>
                        <%--if(target.value == <%=SynchroGlobal.InvestmentType.GlOBAL.getId()%>) {--%>
                            <%--$j("#global-selection").show();--%>
                            <%--$j("#region-selection").hide();--%>
                            <%--$j("#area-selection").hide();--%>
                            <%--$j("#country-selection").hide();--%>
                        <%--} else if(target.value == <%=SynchroGlobal.InvestmentType.REGION.getId()%>) {--%>
                            <%--$j("#global-selection").hide();--%>
                            <%--$j("#region-selection").show();--%>
                            <%--$j("#area-selection").hide();--%>
                            <%--$j("#country-selection").hide();--%>
                        <%--} else if(target.value == <%=SynchroGlobal.InvestmentType.AREA.getId()%>) {--%>
                            <%--$j("#global-selection").hide();--%>
                            <%--$j("#region-selection").hide();--%>
                            <%--$j("#area-selection").show();--%>
                            <%--$j("#country-selection").hide();--%>
                        <%--} else if(target.value == <%=SynchroGlobal.InvestmentType.COUNTRY.getId()%>) {--%>
                            <%--$j("#global-selection").hide();--%>
                            <%--$j("#region-selection").hide();--%>
                            <%--$j("#area-selection").hide();--%>
                            <%--$j("#country-selection").show();--%>
                        <%--}--%>
                    <%--} else {--%>
                        <%--$j("#global-selection").show();--%>
                        <%--$j("#region-selection").hide();--%>
                        <%--$j("#area-selection").hide();--%>
                        <%--$j("#country-selection").hide();--%>
                    <%--}--%>
                <%--}--%>
            <%--</script>--%>
            <%--<div class="access-type-selection-container">--%>
                <%--<div id="global-selection" style="display: none;">--%>
                    <%--<input id="globalSuperUserCB" type="checkbox"  name="globalSuperUser">--%>
                    <%--<label for="globalSuperUserCB">Super user</label>--%>
                <%--</div>--%>
                <%--<div id="region-selection" style="display: none;">--%>
                    <%--<div class="form-select_div">--%>
                        <%--<label><span class="list">Normal user regions</span></label>--%>
                        <%--<select size="3" id="normal-user-regions" name="normal-user-regions" class="all" multiple="true">--%>
                            <%--<%--%>
                                <%--Map<Integer, String> regions = SynchroGlobal.getRegions();--%>
                                <%--Set<Integer> regionKeys = regions.keySet();--%>
                                <%--for(Integer regionKey: regionKeys) {--%>
                            <%--%>--%>
                            <%--<option value="<%=regionKey%>"><%=regions.get(regionKey)%></option>--%>
                            <%--<%--%>
                                <%--}--%>
                            <%--%>--%>
                        <%--</select>--%>
                        <%--<div class="action_buttons">--%>
                            <%--<input id="add-regions" type="button" value=">>" class="left_arrow">--%>
                            <%--<input id="remove-regions" type="button" value="<<" class="right_arrow">--%>
                        <%--</div>--%>
                    <%--</div>--%>

                    <%--<div class="form-select_div_brand">--%>
                        <%--<label><span class="list">Super user regions</span></label>--%>
                        <%--<select id="super-user-regions" name="super-user-regions" multiple="true" size="3" class="selected">--%>
                        <%--</select>--%>
                    <%--</div>--%>
                    <%--<script type="text/javascript">--%>
                        <%--$j(function() {--%>
                            <%--$j("#add-regions, #remove-regions").click(function(event) {--%>
                                <%--var id = $j(event.target).attr("id");--%>
                                <%--var selectFrom = id == "add-regions" ? "#normal-user-regions" : "#super-user-regions";--%>
                                <%--var moveTo = (id == "add-regions") ? "#super-user-regions" : "#normal-user-regions";--%>
                                <%--var selectedItems = $j(selectFrom + " :selected").toArray();--%>
                                <%--$j(moveTo).append(selectedItems);--%>
                            <%--});--%>
                        <%--});--%>
                    <%--</script>--%>
                <%--</div>--%>
                <%--<div id="area-selection" style="display: none;">--%>
                    <%--<div class="form-select_div">--%>
                        <%--<label><span class="list">Normal user areas</span></label>--%>
                        <%--<select size="3" id="normal-user-areas" name="normal-user-areas" class="all" multiple="true">--%>
                            <%--<%--%>
                                <%--Map<Integer, String> areas = SynchroGlobal.getAreas();--%>
                                <%--Set<Integer> areaKeys = areas.keySet();--%>
                                <%--for(Integer areaKey: areaKeys) {--%>
                            <%--%>--%>
                            <%--<option value="<%=areaKey%>"><%=areas.get(areaKey)%></option>--%>
                            <%--<%--%>
                                <%--}--%>
                            <%--%>--%>
                        <%--</select>--%>
                        <%--<div class="action_buttons">--%>
                            <%--<input id="add-areas" type="button" value=">>" class="left_arrow">--%>
                            <%--<input id="remove-areas" type="button" value="<<" class="right_arrow">--%>
                        <%--</div>--%>
                    <%--</div>--%>

                    <%--<div class="form-select_div_brand">--%>
                        <%--<label><span class="list">Super user areas</span></label>--%>
                        <%--<select id="super-user-areas" name="super-user-areas" multiple="true" size="3" class="selected">--%>
                        <%--</select>--%>
                    <%--</div>--%>
                    <%--<script type="text/javascript">--%>
                        <%--$j(function() {--%>
                            <%--$j("#add-areas, #remove-areas").click(function(event) {--%>
                                <%--var id = $j(event.target).attr("id");--%>
                                <%--var selectFrom = id == "add-areas" ? "#normal-user-areas" : "#super-user-areas";--%>
                                <%--var moveTo = (id == "add-areas") ? "#super-user-areas" : "#normal-user-areas";--%>
                                <%--var selectedItems = $j(selectFrom + " :selected").toArray();--%>
                                <%--$j(moveTo).append(selectedItems);--%>
                            <%--});--%>
                        <%--});--%>
                    <%--</script>--%>
                <%--</div>--%>
                <%--<div id="country-selection" style="display: none;">--%>
                    <%--<div class="form-select_div">--%>
                        <%--<label><span class="list">Normal user countries</span></label>--%>
                        <%--<select size="3" id="normal-user-countries" name="normal-user-countries" class="all" multiple="true">--%>
                            <%--<%--%>
                                <%--Map<Integer, String> countries = SynchroGlobal.getEndMarkets();--%>
                                <%--Set<Integer> countryKeys = countries.keySet();--%>
                                <%--for(Integer countryKey: countryKeys) {--%>
                            <%--%>--%>
                            <%--<option value="<%=countryKey%>"><%=countries.get(countryKey)%></option>--%>
                            <%--<%--%>
                                <%--}--%>
                            <%--%>--%>
                        <%--</select>--%>
                        <%--<div class="action_buttons">--%>
                            <%--<input id="add-countries" type="button" value=">>" class="left_arrow">--%>
                            <%--<input id="remove-countries" type="button" value="<<" class="right_arrow">--%>
                        <%--</div>--%>
                    <%--</div>--%>

                    <%--<div class="form-select_div_brand">--%>
                        <%--<label><span class="list">Super user countries</span></label>--%>
                        <%--<select id="super-user-countries" name="super-user-countries" multiple="true" size="3" class="selected">--%>
                        <%--</select>--%>
                    <%--</div>--%>
                    <%--<script type="text/javascript">--%>
                        <%--$j(function() {--%>
                            <%--$j("#add-countries, #remove-countries").click(function(event) {--%>
                                <%--var id = $j(event.target).attr("id");--%>
                                <%--var selectFrom = id == "add-countries" ? "#normal-user-countries" : "#super-user-countries";--%>
                                <%--var moveTo = (id == "add-countries") ? "#super-user-countries" : "#normal-user-countries";--%>
                                <%--var selectedItems = $j(selectFrom + " :selected").toArray();--%>
                                <%--$j(moveTo).append(selectedItems);--%>
                            <%--});--%>
                        <%--});--%>
                    <%--</script>--%>
                <%--</div>--%>
            <%--</div>--%>
        <%--</div>--%>
    <%--</td>--%>
<%--</tr>--%>
<%--<tr style="display: none;">--%>
    <%--<td colspan="2">--%>
        <%--<input id="departments" type="hidden" name="departments"/>--%>
        <%--<input id="brands" type="hidden" name="brands"/>--%>
        <%--<input id="accessType" type="hidden" name="accessType"/>--%>
        <%--<input id="globalAccessSuperUser" type="hidden" name="globalAccessSuperUser" value="false"/>--%>
        <%--<input id="superUserAccessList" type="hidden" name="superUserAccessList"/>--%>
    <%--</td>--%>
<%--</tr>--%>
<!-- Customization ends -->
</tbody>
<tfoot>
    <tr>
        <td colspan="2">
            <input type="submit" name="create" value="Create User">
            <input type="submit" name="createanother" value="Create &amp; Create Another User">
            <input type="submit" name="cancel" value="Cancel">
        </td>
    </tr>
</tfoot>
</table>
</div>
</form>

<script language="JavaScript" type="text/javascript">
if (document.createForm.username != undefined) {
    document.createForm.username.focus();
} else {
    document.createForm.firstName.focus();
}

$j(document).ready(function(){
        $j("#createForm").submit(function(event){
            var self = this;
            event.preventDefault();
            var portalType = $j("input[name=portalType]:checked").val();
            if(portalType == 'synchro') {
                var depList = [];
                $j("#selected-departments option").each(function(){
                    depList.push($j(this).val());
                });
                if(depList.length > 0) {
                    $j("#departments").val(depList.join(","));
                } else {
                    $j("#departments").val("");
                }

                var brandList = [];
                $j("#selected-brands option").each(function(){
                    brandList.push($j(this).val());
                });
                if(brandList.length > 0) {
                    $j("#brands").val(brandList.join(","));
                } else {
                    $j("#brands").val("");
                }

                var accessType =  $j("input[name=accessTypeGroup]:checked").val();

                $j("#accessType").val(accessType);

                var superUserAccessList = [];
                if(accessType == <%=SynchroGlobal.InvestmentType.GlOBAL.getId()%>) {
                    superUserAccessList = [];
                    $j("#superUserAccessList").val("");
                    if($j("#globalSuperUserCB").is(':checked')) {
                        $j("#globalAccessSuperUser").val("true");
                    } else {
                        $j("#globalAccessSuperUser").val("false");
                    }
                } else if(accessType == <%=SynchroGlobal.InvestmentType.REGION.getId()%>) {
                    $j("#globalAccessSuperUser").val("false");
                    superUserAccessList = [];
                    $j("#super-user-regions option").each(function(){
                        superUserAccessList.push($j(this).val());
                    });

                } else if(accessType == <%=SynchroGlobal.InvestmentType.AREA.getId()%>) {
                    $j("#globalAccessSuperUser").val("false");
                    superUserAccessList = [];
                    $j("#super-user-areas option").each(function(){
                        superUserAccessList.push($j(this).val());
                    });
                } else if(accessType == <%=SynchroGlobal.InvestmentType.COUNTRY.getId()%>) {
                    $j("#globalAccessSuperUser").val("false");
                    superUserAccessList = [];
                    $j("#super-user-countries option").each(function(){
                        superUserAccessList.push($j(this).val());
                    });
                }
                if(superUserAccessList.length > 0) {
                    $j("#superUserAccessList").val(superUserAccessList.join(","));
                } else {
                    $j("#superUserAccessList").val("");
                }
            } else {
                $j("#departments").val("");
                $j("#brands").val("");
                $j("#accessType").val("");
                $j("#globalAccessSuperUser").val("false");
                $j("#superUserAccessList").val("");
            }
            self.submit();
        });
    });
</script>
<%  }
    else if (!iUserManager.isCreationSupported()) { %>
        <p>User creation is not supported by the currently configured user manager.</p>
<%  } %>

</body>
</html>
