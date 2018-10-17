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
<%@ page import="com.grail.manager.GrailBriefTemplateManager" %>
<%@ page import="com.grail.beans.GrailBriefTemplate" %>
<%@ page import="com.grail.GrailGlobals" %>

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

<style type="text/css">
    @import "<s:url value='/admin/style/synchro-admin.css'/>";
</style>
<%!
    static final Logger systemInfoLogger = LogManager.getLogger("com.jivesoftware");
    private String getEncodedText(final String text) {
        String encodedText = text.replaceAll("\"","\"\"");
        return encodedText;
    }
%>
<% // Only allow system admins to see this page
    if (!isSystemAdmin) {
        throw new UnauthorizedException("You don't have admin privileges to perform this operation.");
    }
%>

<% // Title of this page and breadcrumbs
    String title = "Grail Brief Templates Raw Extract";
    JiveContext ctx = JiveApplication.getContext();
    JiveLicense license = ctx.getLicenseManager().getJiveLicense();
    if(request.getParameter("downloadReport") != null && Boolean.parseBoolean(request.getParameter("downloadReport"))) {

        StringBuilder report = new StringBuilder();

        StringBuilder header = new StringBuilder();
        header.append("User Name").append(",");
        header.append("User Email").append(",");
        header.append("User Location").append(",");
        header.append("User Role").append(",");
        header.append("Research Needs and Priorities").append(",");
        header.append("Hypotheses and Business Needs").append(",");
        header.append("Markets").append(",");
        header.append("Products").append(",");
        header.append("Brands").append(",");
        header.append("Categories").append(",");
        header.append("Delivery Date").append(",");
        header.append("Output Format").append(",");
        header.append("BAT Contact");
        header.append("Recipient Email").append(",");
        header.append("Email Sent").append(",");
        header.append("Draft Template");
        report.append(header.toString()).append("\n");

        GrailBriefTemplateManager grailBriefTemplateManager = JiveApplication.getContext().getSpringBean("grailBriefTemplateManager");
        List<GrailBriefTemplate> grailBriefTemplates = grailBriefTemplateManager.getAll();
        if(grailBriefTemplates != null && grailBriefTemplates.size() > 0) {
            for(GrailBriefTemplate template: grailBriefTemplates) {
                Map<String, Object> templateMap = GrailGlobals.toGrailBriefTemplateMap(template, template.getSender());
                if(templateMap != null && !templateMap.isEmpty()) {
                    StringBuilder data = new StringBuilder();
                    boolean isDraft = false;
                    if(templateMap.containsKey("isDraft") && templateMap.get("isDraft") != null
                            && Boolean.parseBoolean(templateMap.get("isDraft").toString())) {
                       isDraft = true;
                    }

                    if(templateMap.containsKey("userName") && templateMap.get("userName") != null
                            && !templateMap.get("userName").equals("")) {
                        data.append("\"").append(templateMap.get("userName").toString().trim()).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("userEmail") && templateMap.get("userEmail") != null
                            && !templateMap.get("userEmail").equals("")) {
                        data.append("\"").append(templateMap.get("userEmail").toString().trim()).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("userLocation") && templateMap.get("userLocation") != null
                            && !templateMap.get("userLocation").equals("")) {
                        data.append("\"").append(templateMap.get("userLocation").toString().trim()).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("userRole") && templateMap.get("userRole") != null
                            && !templateMap.get("userRole").equals("")) {
                        data.append("\"").append(templateMap.get("userRole").toString().trim()).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("researchNeedsPriorities") && templateMap.get("researchNeedsPriorities") != null
                            && !templateMap.get("researchNeedsPriorities").equals("")) {
                        data.append("\"").append(getEncodedText(templateMap.get("researchNeedsPriorities").toString().trim())).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("hypothesisBusinessNeed") && templateMap.get("hypothesisBusinessNeed") != null
                            && !templateMap.get("hypothesisBusinessNeed").equals("")) {
                        data.append("\"").append(getEncodedText(templateMap.get("hypothesisBusinessNeed").toString().trim())).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("markets") && templateMap.get("markets") != null
                            && !templateMap.get("markets").equals("")) {
                        data.append("\"").append(getEncodedText(templateMap.get("markets").toString().trim())).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("products") && templateMap.get("products") != null
                            && !templateMap.get("products").equals("")) {
                        data.append("\"").append(getEncodedText(templateMap.get("products").toString().trim())).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("brands") && templateMap.get("brands") != null
                            && !templateMap.get("brands").equals("")) {
                        data.append("\"").append(getEncodedText(templateMap.get("brands").toString().trim())).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("categories") && templateMap.get("categories") != null
                            && !templateMap.get("categories").equals("")) {
                        data.append("\"").append(getEncodedText(templateMap.get("categories").toString().trim())).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("deliveryDate") && templateMap.get("deliveryDate") != null
                            && !templateMap.get("deliveryDate").equals("")) {
                        data.append("\"").append(templateMap.get("deliveryDate").toString().trim()).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("outputFormat") && templateMap.get("outputFormat") != null
                            && !templateMap.get("outputFormat").equals("")) {
                        data.append("\"").append(templateMap.get("outputFormat").toString().trim()).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }


                    if(templateMap.containsKey("batContact") && templateMap.get("batContact") != null
                            && !templateMap.get("batContact").equals("")) {
                        if(isDraft) {
                            data.append(" ").append(",");
                        } else {
                            data.append("\"").append(templateMap.get("batContact").toString().trim()).append("\"").append(",");
                        }
                    } else {
                        data.append(" ").append(",");
                    }

                    if(templateMap.containsKey("recipientEmail") && templateMap.get("recipientEmail") != null
                            && !templateMap.get("recipientEmail").equals("")) {
                        if(isDraft) {
                            data.append(" ").append(",");
                        } else {
                            data.append("\"").append(templateMap.get("recipientEmail").toString().trim()).append("\"").append(",");
                        }
                    } else {
                        data.append(" ").append(",");
                    }

                    if(isDraft) {
                        data.append("\"").append("No").append("\"").append(",");
                        data.append("\"").append("Yes").append("\"");
                    } else {
                        data.append("\"").append("Yes").append("\"").append(",");
                        data.append("\"").append("No").append("\"");
                    }



                    report.append(data.toString()).append("\n");
                }
            }
        }

        response.setContentType("application/csv");
        response.setHeader("content-disposition","filename=GrailBriefTemplatesRawExtract.csv"); // Filename
        PrintWriter outx = response.getWriter();
        outx.println(report.toString());
        outx.flush();
        outx.close();
    }
%>


<head>
    <title><%= title %></title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">grailBriefTemplatesRawExtract</content>
    <content tag="pagehelp">
        <h3>Grail Brief Templates Raw Extract</h3>
    </content>
</head>

<div>
    <form action="grail-brief-templates-raw-extract.jsp" method="post">
        <input type="hidden" name="downloadReport" value="true">
        <span style="display:block;">Click here to download</span>
        <input type="submit" value="Download Report">
    </form>
</div>




