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
        String title = "User Country Mapping";
        JiveContext ctx = JiveApplication.getContext();
        JiveLicense license = ctx.getLicenseManager().getJiveLicense();


        if(request.getParameter("update") != null && Boolean.parseBoolean(request.getParameter("update"))) {
            String root = ServletActionContext.getServletContext().getRealPath("/");
            List<String> updateScripts = new ArrayList<String>();
            List<String> insertScripts = new ArrayList<String>();
            FileInputStream fileInputStream = new FileInputStream(root + "/UserCountryMapping.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext()) {
                XSSFRow row = (XSSFRow) rows.next();
                String email = row.getCell(0).getStringCellValue().toLowerCase();
                String reqCountry = row.getCell(1).getStringCellValue();

                String currCountry = row.getCell(3).getStringCellValue();
                boolean isEqual = row.getCell(4).getBooleanCellValue();

                if(reqCountry.equals("UK")) {
                    reqCountry = "U.K.";
                }

                SynchroUserManager synchroUserManager = JiveApplication.getContext().getSpringBean("synchroUserManager");
                User user =  synchroUserManager.getUserByEmail(email);

                String action = "none";
                if(user != null && !isEqual) {
                    boolean isUserPropExists = synchroUserManager.isUserPropExists(user.getID(), "grail.synchro.user.prop.country");
                    if(isUserPropExists) {
                        action = "update";
                    } else {
                        action = "insert";
                    }
                }
                if(action.equals("update")) {
                    String updateScript = "update jiveuserprop set propvalue='"+reqCountry.replaceAll(" ", "%20A")+"' where userid="+user.getID()+" AND name = 'grail.synchro.user.prop.country';";
                    updateScripts.add(updateScript);
                } else if(action.equals("insert")) {
                    String insertScript = "insert into jiveuserprop (userid,name,propvalue) VALUES ("+user.getID()+", 'grail.synchro.user.prop.country', '"+reqCountry.replaceAll(" ", "%20A")+"');";
                    insertScripts.add(insertScript);
                }
            }
            File file = null;
            String content = "";
            if(updateScripts.size() > 0) {
                writeToFile(root + "/jiveuserprop_update_scripts.sql", org.apache.commons.lang.StringUtils.join(updateScripts,"\n"));
            }
            if(insertScripts.size() > 0) {
                writeToFile(root + "/jiveuserprop_insert_scripts.sql", org.apache.commons.lang.StringUtils.join(insertScripts,"\n"));
            }

        }
    %>

    <%!
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
<form action="user-country-mapping.jsp" method="post">
    <input type="hidden" name="update" value="true">
    <input type="submit" value="Update">
</form>
</body>