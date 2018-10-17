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
        String title = "Project Department Mapping";
        JiveContext ctx = JiveApplication.getContext();
        JiveLicense license = ctx.getLicenseManager().getJiveLicense();
        String root = ServletActionContext.getServletContext().getRealPath("/");

        if(request.getParameter("update") != null && Boolean.parseBoolean(request.getParameter("update"))) {
            boolean success = processMapping(root);
        }
    %>

    <%!

        UserDepartmentsService service = JiveApplication.getContext().getSpringBean("userDepartmentsService");

        Set<String> projectUpdateScripts = null;

        Set<String> unAvailableDepartments = null;

        Map<Long, Long> projectDepartmentMapping = null;

        public boolean processMapping(String root) throws FileNotFoundException, IOException {
            unAvailableDepartments =  new LinkedHashSet<String>();
            projectDepartmentMapping = new LinkedHashMap<Long, Long>();
            projectUpdateScripts = new LinkedHashSet<String>();
            String content= "";

            FileInputStream fileInputStream = new FileInputStream(root + "/ProjectAgencyMapping.xls");
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);

            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator rows = sheet.rowIterator();
            rows.next();
            while (rows.hasNext()) {
                HSSFRow row = (HSSFRow) rows.next();
                Double projectId = row.getCell(0).getNumericCellValue();
                String reqDepartmentName = row.getCell(1).getStringCellValue().trim();
                if(reqDepartmentName.equalsIgnoreCase("NOT DEFINED")) {
                    projectDepartmentMapping.put(projectId.longValue(), -1L);
                } else {
                    UserDepartment userDepartment = service.getByName(reqDepartmentName);
                    if(userDepartment == null || userDepartment.getId() == null) {
                        unAvailableDepartments.add(reqDepartmentName);
                    } else {
                        projectDepartmentMapping.put(projectId.longValue(), userDepartment.getId());
                    }
                }
            }

            if(unAvailableDepartments.size() > 0) {
                Iterator<String> it = unAvailableDepartments.iterator();
                while (it.hasNext()) {
                    String deptName = it.next();
                    service.save(null, deptName,  deptName);
                }
                processMapping(root);

            } else {
                if(projectDepartmentMapping != null &&  projectDepartmentMapping.size() > 0) {
                    for(Long key: projectDepartmentMapping.keySet()) {
                        String script = "UPDATE grailproject SET agencyDept="+projectDepartmentMapping.get(key)+" WHERE projectId = "+key+";";
                        projectUpdateScripts.add(script);
                    }

                    if(projectUpdateScripts.size() > 0) {
                        writeToFile(root +"/projectdepartmentupdatescript.sql", org.apache.commons.lang.StringUtils.join(projectUpdateScripts,"\n"));
                    }
                }
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
<form action="project-department-mapping.jsp" method="post">
    <input type="hidden" name="update" value="true">
    <input type="submit" value="Update">
</form>

</body>