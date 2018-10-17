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
<%@ page import="com.grail.synchro.dwr.service.UserDepartmentsService" %>
<%@ page import="com.grail.synchro.beans.UserDepartment" %>

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

<script type="text/javascript" src="<s:url value='/dwr/interface/UserDepartmentsService.js'/>"></script>

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
    String title = "Departments";
    JiveContext ctx = JiveApplication.getContext();
    JiveLicense license = ctx.getLicenseManager().getJiveLicense();


%>

<head>
    <title><%= title %></title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">departments</content>
    <content tag="pagehelp">
        <h3>Departments</h3>
    </content>
</head>


<form action="user-departments.jsp" name="synchro-metaform" id="synchro-metaform" method="POST">
    <div class="jive-table">
        <%--<table cellpadding="0" cellspacing="0" border="0" width="100%">--%>
        <%--<thead>--%>
        <%--<tr>--%>
        <%--<th colspan="4">Departments</th>--%>
        <%--</tr>--%>
        <%--</thead>--%>
        <%--<tbody>--%>
        <%--<%--%>
        <%--UserDepartmentsService service = JiveApplication.getContext().getSpringBean("userDepartmentsService");--%>
        <%--List<UserDepartment> userDepartments = service.getAll();--%>
        <%--if(userDepartments != null && userDepartments.size() > 0) {--%>
        <%--for(UserDepartment userDepartment: userDepartments) {%>--%>
        <%--<tr>--%>
        <%--<td>--%>
        <%--<span><%=userDepartment.getName()%></span>--%>
        <%--</td>--%>
        <%--<td>--%>
        <%--<span><%=userDepartment.getDescription()%></span>--%>
        <%--</td>--%>
        <%--<td>--%>
        <%--<a class="enable-delete-link" onclick="deleteDepartment(<%=userDepartment.getId()%>)">--%>
        <%--<img src="../images/jive-icon-delete-16x16.gif" width="16" height="16" alt="Delete Property" border="0">--%>
        <%--</a>--%>
        <%--</td>--%>
        <%--<td>--%>
        <%--<a href="javascript:void(0);" onclick="editDepartment(<%=userDepartment.getId()%>)">--%>
        <%--<img src="../images/jive-icon-edit-16x16.gif" width="16" height="16" alt="Edit Region Name" border="0">--%>
        <%--</a>--%>
        <%--</td>--%>
        <%--</tr>--%>
        <%--<%--%>
        <%--}--%>
        <%--} else {%>--%>
        <%--<tr><td colspan="4"><span>No departments available</span></td></tr>--%>
        <%--<%}%>--%>
        <%--</tbody>--%>
        <%--</table>--%>
    </div>
    <div id="department-form" class="department-form add" style="display: none;border: 1px 0px; ">
        <div class="add-prop-header">Add Department</div>
        <div class="j-form-row">
            <label for="departmentName">Name</label>
            <input id="departmentName" type="text" name="departmentName">
            <span class="jive-error-text" style="display: none;"><br> Please enter a valid department.</span>
        </div>
        <div class="j-form-row">
            <label for="departmentDescription">Description</label>
            <textarea id="departmentDescription" name="departmentDescription"></textarea>
        </div>
        <div>
            <input class="add-btn" type="button" id="add-btn" value="Save" name="Save" />
            <input class="add-cancel" type="button" id="cancel-btn" value="Cancel" name="Cancel" />
        </div>
    </div>
    <div id="add-department-button">
        <input type="button" id="add-department" name="add" value="Add Department" />
    </div>

</form>


<script type="text/javascript">
    $j(document).ready(function(){

        showDepartments();

        $j("#add-department").click(function(){
            $j(".department-form").hide();
            $j("#department-form").show();
            $j("#add-department-button").hide();

        });
        $j("#add-btn").click(function(){
            var depName = $j("#departmentName").val();
            var depDesc = $j("#departmentDescription").val();
            saveDepartment(null, depName, depDesc)
        });

        $j("#cancel-btn").click(function(){
            $j("#department-form").hide();
            $j("#add-department-button").show();
            $j(".jive-error-text").hide();
            $j("#synchro-metaform").submit();
        });

    });
    function showDepartments() {
        $j(".jive-table").html("");
        var table = '<table cellpadding="0" cellspacing="0" border="0" width="100%">';
        table += ' <thead><tr><th colspan="4">Departments</th></tr></thead>';
    <%
        UserDepartmentsService service = JiveApplication.getContext().getSpringBean("userDepartmentsService");
        List<UserDepartment> userDepartments = service.getAll();
        if(userDepartments != null && userDepartments.size() > 0) {
         for(UserDepartment userDepartment: userDepartments) {%>
        table += '<tr>';
        table += '<td><span><%=userDepartment.getName().trim()%></span></td>';
        table += '<td><span><%=userDepartment.getDescription().trim()%></span></td>';
        table += '<td><a class="enable-delete-link" onclick="deleteDepartment(<%=userDepartment.getId()%>)"><img src="../images/jive-icon-delete-16x16.gif" width="16" height="16" alt="Delete Property" border="0"></a></td>';
        table += '<td><a href="javascript:void(0);" onclick="editDepartment(<%=userDepartment.getId()%>)"><img src="../images/jive-icon-edit-16x16.gif" width="16" height="16" alt="Edit Region Name" border="0"></a></td>';
        table += '</tr>';
        table += '<tr>';
        table += '<td colspan="4" id="edit-department-form-<%=userDepartment.getId()%>" class="department-form edit"  style="display: none;">';
        table += '<div>';
        table += '<div class="j-form-row">';
        table += '<label for="departmentName-<%=userDepartment.getId()%>">Name</label>';
        table += '<input id="departmentName-<%=userDepartment.getId()%>" type="text" name="departmentName">'
        table += '<span class="jive-error-text-<%=userDepartment.getId()%>" style="display: none;"><br> Please enter a valid department.</span>';
        table += '</div>';
        table += '<div class="j-form-row">';
        table += '<label for="departmentDescription-<%=userDepartment.getId()%>">Description</label>';
        table += '<textarea id="departmentDescription-<%=userDepartment.getId()%>" name="departmentDescription"></textarea>';
        table += '</div>';
        table += '<div>';
        table += '<input class="add-btn" type="button" id="edit-btn" value="Save" name="Save" onclick="updateDepartment(<%=userDepartment.getId()%>)"/>';
        table += '<input class="add-cancel" type="button" id="edit-cancel-btn" value="Cancel" name="Cancel" onclick="closeEditForm(<%=userDepartment.getId()%>)"/>';
        table += '</div>';
        table += '</div>';
        table += '</td>';
        table += '</tr>';
    <%
      }
  } else {%>
        table += '<tr><td colspan="4"><span>No departments available</span></td></tr>';
    <%}%>
        table += '</table>';
        $j(".jive-table").html(table);
    }
    function editDepartment(id) {
        UserDepartmentsService.get(id,{
            callback: function(department) {
                $j(".department-form").hide();
                $j("#edit-department-form-"+id).show();
                $j("#departmentName-"+id).val(department.name);
                $j("#departmentDescription-"+id).val(department.description);
            },
            async: false,
            timeout: 20000
        });
    }
    function updateDepartment(id) {
        var depName = $j("#departmentName-"+id).val();
        var depDesc = $j("#departmentDescription-"+id).val();
        saveDepartment(id, depName, depDesc)
    }

    function saveDepartment(id, depName,depDesc) {
        if(depName != "") {
            UserDepartmentsService.save(id, depName,depDesc,{
                callback: function(result) {
                    $j("#synchro-metaform").submit();
                },
                async: false,
                timeout: 20000
            });
        } else {
            if(id != null) {
                $j(".jive-error-text-"+id).show();
            } else {
                $j(".jive-error-text").show();
            }
        }
    }
    function closeEditForm(id) {
        $j(".jive-error-text-"+id).hide();
        $j("#edit-department-form-"+id).hide();
        //$j("#synchro-metaform").submit();
    }
    function deleteDepartment(id) {
        UserDepartmentsService.deleteDepartment(id,{
            callback: function(result) {
                $j("#synchro-metaform").submit();
            }
        });
    }

    function clearForm() {
        $j("#departmentName").val("");
        $j("#departmentDescription").val("");
    }

</script>

