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

<jsp:useBean id="jivepageinfo" scope="request" class="com.jivesoftware.base.admin.AdminPageBean" />

<%@ include file="include/global.jspf" %>

<%--
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/jquery.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.core.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.widget.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.mouse.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.sortable.js' />"></script>
<script type="text/javascript" src="<s:url value='/resources/scripts/jive/namespace.js' />"></script> --%>

<script type="text/javascript" src="/8.0.3.1619a91/resources/scripts/jquery/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="/8.0.3.1619a91/resources/scripts/jquery/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="/8.0.3.1619a91/resources/scripts/jquery/ui/jquery.ui.mouse.js"></script>
<script type="text/javascript" src="/8.0.3.1619a91/resources/scripts/jquery/ui/jquery.ui.sortable.js"></script>
<script type="text/javascript" src="/8.0.3.1619a91/resources/scripts/jive/namespace.js"></script>
	
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
    String title = "Update Raw Extract";
    JiveContext ctx = JiveApplication.getContext();
    JiveLicense license = ctx.getLicenseManager().getJiveLicense();
    String lastUpdate = JiveGlobals.getJiveProperty("synchro.raw.extract.lastupdate");
%>


<head>
    <title><%= title %></title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">updateRawExtract</content>
    <content tag="pagehelp">
        <h3>Update Raw Extract</h3>
    </content>
</head>
<div class="raw-extract-update-container">
    <div class="raw-extract-update-status" style="display: none;">
        <span class="jive-icon-med jive-icon-info"></span>
        <span class="message">Updated successfully.</span>
    </div>

    <span>Initiate a full raw extract update process.</span>
    <div class="raw-extract-last-updated">
        <span class="label">Last Updated: </span>
        <span class="content"><%=lastUpdate%></span>
    </div>

    <input type="button" value="Update" style="display: block;" onclick="updateExtract()">

    <div style="display:none;" class="update-inprogress-message">
        <span class="progress-icon"></span>
        <span>Please wait until update process complete.</span>
    </div>
</div>


<script type="text/javascript">

    function updateExtract() {
        $j("body").attr("disabled", true);
        $j(".update-inprogress-message").show();
        $j(".raw-extract-update-status").hide();

        setTimeout(function(){
            RawExtractService.update({
                callback: function(data) {
                    $j("body").removeAttr("disabled");
                    $j(".update-inprogress-message").hide();
                    $j(".raw-extract-update-status").show();
                    if(data.success) {
                        $j(".raw-extract-update-status").removeClass("error");
                        $j(".raw-extract-update-status").addClass("success");
                    } else {
                        $j(".raw-extract-update-status").removeClass("success");
                        $j(".raw-extract-update-status").addClass("error");
                    }
                    $j(".raw-extract-update-status .message").html(data.message);
                    $j(".raw-extract-last-updated .content").html(data.lastUpdated)
                    setTimeout(function(){$j(".raw-extract-update-status").hide();},5000);
                },
                async: false
            });
        }, 500);
    }



</script>





