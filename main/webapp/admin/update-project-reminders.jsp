<%@ page import="com.jivesoftware.base.database.*,
                 com.jivesoftware.base.profile.MultiProviderUserManager,
              
                 com.grail.synchro.SynchroGlobal,
                 com.grail.synchro.SynchroConstants,
				 com.grail.synchro.util.SynchroUtils,
				 com.jivesoftware.community.action.util.ParamUtils,
                 java.util.Map,
                 com.jivesoftware.community.JiveGlobals,
                 com.jivesoftware.license.License,
                 com.jivesoftware.community.license.JiveLicense,
                 com.jivesoftware.util.StringUtils,
                 com.jivesoftware.util.TimeFormat,
             
                 java.sql.DatabaseMetaData,
                 java.text.SimpleDateFormat"
         errorPage="error.jsp"
        %>
<%@ page import="com.grail.synchro.dwr.service.RawExtractService" %>
<%@ page import="com.grail.synchro.scheduling.quartz.ProjectCostsCaptureTask" %>
<%@ page import="com.grail.synchro.scheduling.quartz.SynchroReminderTask" %>

<jsp:useBean id="jivepageinfo" scope="request" class="com.jivesoftware.base.admin.AdminPageBean" />

<%@ include file="include/global.jspf" %>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/jquery.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.core.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.widget.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.mouse.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.sortable.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jive/namespace.js' />"></script>
<script type="text/javascript" src="<s:url value='/admin/scripts/jstree.js' />"></script>

<script type="text/javascript" src="<s:url value='/resources/scripts/dwr/engine.js'/>"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/dwr/util.js'/>"></script>
<script type="text/javascript" src="<s:url value='/dwr/interface/CommunityUtils.js'/>"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/apps/admin/community_manager.js' />"></script>

<script type="text/javascript" src="<s:url value='/dwr/interface/RawExtractService.js'/>"></script>

<style type="text/css">
    @import "<s:url value='/admin/style/synchro-admin.css'/>";
</style>
<%!
    static final Logger systemInfoLogger = LogManager.getLogger("com.jivesoftware");
%>
<% // Only allow system admins to see this page
    if (!isSystemAdmin) {
        throw new UnauthorizedException("You don't have admin privileges to perform this operation.");
    }
%>

<% // Title of this page and breadcrumbs
    JiveContext ctx = JiveApplication.getContext();
    JiveLicense license = ctx.getLicenseManager().getJiveLicense();

    if(request.getParameter("updateProjectReminders") != null && Boolean.parseBoolean(request.getParameter("updateProjectReminders"))) {
        SynchroReminderTask synchroReminderTask  = JiveApplication.getContext().getSpringBean("synchroReminderTask");
        synchroReminderTask.manageReminders();
    }
%>


<head>
    <title>Update Project Reminders</title>

</head>
<form action="update-project-reminders.jsp" method="post">
    <input type="hidden" name="updateProjectReminders" value="true">
    <input type="submit" value="Update">

 </form>




