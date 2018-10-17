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
<%@ page import="org.apache.poi.hssf.usermodel.HSSFWorkbook" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFSheet" %>
<%@ page import="org.apache.poi.hssf.usermodel.HSSFRow" %>
<%@ page import="org.apache.struts2.ServletActionContext" %>
<%@ page import="org.apache.poi.xssf.usermodel.XSSFWorkbook" %>
<%@ page import="org.apache.poi.xssf.usermodel.XSSFSheet" %>
<%@ page import="org.apache.poi.xssf.usermodel.XSSFRow" %>
<%@ page import="com.grail.synchro.manager.SynchroUserManager" %>
<%@ page import="com.grail.synchro.dwr.service.UserDepartmentsService" %>
<%@ page import="com.grail.synchro.beans.UserDepartment" %>

<jsp:useBean id="jivepageinfo" scope="request" class="com.jivesoftware.base.admin.AdminPageBean" />

<%@ include file="include/global.jspf" %>
<%--<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/jquery.js' />"></script>--%>
<%--<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.core.js' />"></script>--%>
<%--<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.widget.js' />"></script>--%>
<%--<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.mouse.js' />"></script>--%>
<%--<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/ui/ui.sortable.js' />"></script>--%>
<%--<script type="text/javascript" src="<s:url value='/resources/scripts/jive/namespace.js' />"></script>--%>
<%--<script type="text/javascript" src="<s:url value='/admin/scripts/jstree.js' />"></script>--%>



<%!
    static final Logger systemInfoLogger = LogManager.getLogger("com.jivesoftware");
%>
<% // Only allow system admins to see this page
    if (!isSystemAdmin) {
        throw new UnauthorizedException("You don't have admin privileges to perform this operation.");
    }
%>




<head>
    <title>User Country Mapping</title>
    <% // Title of this page and breadcrumbs
        String title = "Budget Year mapping";
        JiveContext ctx = JiveApplication.getContext();
        JiveLicense license = ctx.getLicenseManager().getJiveLicense();
        String root = ServletActionContext.getServletContext().getRealPath("/");

        if(request.getParameter("update") != null && Boolean.parseBoolean(request.getParameter("update"))) {
            boolean success = processMapping(root);
        }
    %>

    <%!


        Set<String> projectUpdateScripts = null;


        public boolean processMapping(String root) throws FileNotFoundException, IOException {
            projectUpdateScripts = new LinkedHashSet<String>();
            String content= "";

            FileInputStream fileInputStream = new FileInputStream(root + "/BudgetYearMapping.xls");
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext()) {
                HSSFRow row = (HSSFRow) rows.next();
                Double projectId = row.getCell(0).getNumericCellValue();
                Double budgetYear = row.getCell(1).getNumericCellValue();
                String script = "UPDATE grailproject SET budgetyear="+budgetYear.intValue()+" WHERE projectId = "+projectId.longValue()+";";
                projectUpdateScripts.add(script);
            }

            if(projectUpdateScripts.size() > 0) {
                writeToFile(root +"/projectbudgetyearmapping.sql", org.apache.commons.lang.StringUtils.join(projectUpdateScripts,"\n"));
            }

            return true;
        }
        public void writeToFile(final String fileName, final String content) throws IOException {
            File file = new File(fileName);
            if(file != null) {
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            }
        }
    %>


</head>
<body>
<form action="budget-year-mapping.jsp" method="post">
    <input type="hidden" name="update" value="true">
    <input type="submit" value="Update">
</form>

</body>