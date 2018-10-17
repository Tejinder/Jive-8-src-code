<%--
  ~ $Revision: 1.1 $
  ~ $Date: 2017/11/06 06:14:09 $
  ~
  ~ Copyright (C) 1999-2011 Jive Software. All rights reserved.
  ~
  ~ This software is the proprietary information of Jive Software. Use is subject to license terms.
  --%>

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

<jsp:useBean id="jivepageinfo" scope="request" class="com.jivesoftware.base.admin.AdminPageBean" />

<%@ include file="include/global.jspf" %>
	
	<%--<script type="text/javascript" src="<s:url value='/resources/scripts/jquery/jquery.js' />"></script>
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
    <style type="text/css">
        @import "<s:url value='/styles/jive-popup.css'/>";
        @import "<s:url value='/admin/style/jstree.css'/>";
    </style>
	
<script type="text/javascript" src="<s:url value='/dwr/interface/MetaFieldUtil.js'/>"></script>

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
    String title = "Synchro Configuration";
    JiveContext ctx = JiveApplication.getContext();
    JiveLicense license = ctx.getLicenseManager().getJiveLicense();		
	
	
%>

<head>
    <title><%= title %></title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">synchro-jobtitle</content>
    <content tag="pagehelp">
        <h3>Job Titles</h3>       
    </content>
</head>

<form action="/admin/synchro-jobtitle.jsp" name="synchro-metaform" id="synchro-metaform" method="POST">

        <table class="space-table" width="100%" border="0" cellpadding="0" cellspacing="0">
			<tbody>
				<tr>
					<td>
						<p style="padding: 5px;">
							Drag and drop to reorder areas.</p>
						<div id="tree-div">
							<ul class="jstree">
								<li id="space_1" class="">									
									<span>Job Titles</span>
									<ul id="space_1_ul" class="ui-sortable" style="display: block;">
	
	<%		
		Map<Integer, String> jobTitles = SynchroGlobal.getJobTitles();		

		for(Integer id : jobTitles.keySet())
		{
		String name = jobTitles.get(id);
		%>
		<li id="<%=id%>" class="main">
		<span><%=name%></span>	
			<div class="brands-select">			
			<a href="javascript:void(0);" onclick="editProperty(<%=id%>)">
				<img src="../images/jive-icon-edit-16x16.gif" width="16" height="16" alt="Edit Job Title" border="0">
			</a>
			<a class="enable-delete-link" onclick="deleteProperty(<%=id%>)">
				<img src="../images/jive-icon-delete-16x16.gif" width="16" height="16" alt="Delete Property" border="0">
			</a>
			
			<ul id="edit-box-<%=id%>" style="display:none">		
				<li class="hidden-list">
					<div class="j-form-row">
						<textarea cols="70" rows="5" id="jive-propValue-<%=id%>" name="propValue" ><%=name%></textarea>			
					</div>					
					<span id="jive-propValue-error-<%=id%>" class="jive-error-text error" style="display:none"><br>Please enter a property value (max 250 characters).</span>	
				</li>			
			<div>
				<input class="save" type="button" id="<%=id%>" value="Save" name="Save" />
				<input class="edit-cancel" type="button" id="cancel-<%=id%>" value="Cancel" name="Cancel" />			
			</div>
			</ul>
			
		</li>
		
			
		<%
		}
	
	%>	
									</ul>
								</li>
							</ul>
						</div>
					</td>
				</tr>
	
	<tr id="add-box" style="display:none;">	
		<td>
			<div class="add-prop-header">Add Job Title</div>
			<div class="j-form-row">
				<textarea cols="70" rows="5" id="jive-propValue" name="propValue" ></textarea>			
			</div>
			<div>
				<input class="add-btn" type="button" id="add-btn" value="Save" name="Save" />
				<input class="add-cancel" type="button" id="cancel-btn" value="Cancel" name="Cancel" />
			</div>
			<span id="jive-propValue-error" class="jive-error-text error" style="display:none"><br>Please enter a property value (max 250 characters).</span>
			</td>			
		</tr>
	<tr>	
		<td colspan="3">
			<input type="button" id="add" name="add" value="Add Job Title" />			
			<input type="button" id="updatesort" name="add" value="Apply Sort" disabled onclick="updateSortOrder();"/>			
		</td>	
		
	</tr>
	
</tbody>
</table>
</form>

<script type="text/javascript">
function editProperty(id)
{
 $j(".jive-error-text").each(function(){ 
    $j(this).hide();
  });
	$j("#add").attr("disabled",false);
	
  $j('[id^="select-region-"').each(function(){ 
     $j(this).attr('disabled', 'disabled');
  });
  $j('ul[id^="edit-box-"]').each(function(){
     $j(this).hide();
  }); 
	var row_id = "edit-box-"+id;	
	$j("#"+row_id).show();
	
	var select_id ="select-region-"+id;	
	$j("#"+select_id).removeAttr("disabled");
	$j("#add-box").hide();
}

function deleteProperty(id)
{
	var res = confirm("Delete item?");
	if (res == true)
	  {
	  MetaFieldUtil.deleteJobTitle(id, {
		callback: function(result) {
			$j("#synchro-metaform").submit();
		},
		async: false,
		timeout: 20000
		});		
	  }
}

function updateSortOrder()
{
	$j("#updatesort").attr("disabled", true);
	 var orderMap = {};
	 var idx = 1;
	 $j('.ui-sortable .main').each(function( index ) {	
		if($j(this).hasClass("main")) 
		{
		console.log("ID "+$j(this).attr("id") + " | Sort No " + idx);
			orderMap[$j(this).attr("id")] = idx;
			idx++;
		}
	});
	console.log(orderMap);
	MetaFieldUtil.sortJobTitle(orderMap, {
		callback: function(result) {
			$j("#synchro-metaform").submit();
		},
		async: false,
		timeout: 20000
		});
		
	
}

$j(".save").click(function(){
$j(".jive-error-text").each(function(){ 
		$j(this).hide();
	 });
	

   var id = $j(this).attr("id");   
   var textarea = $j("#jive-propValue-"+id);  
  if(textarea.val()!=null && $j.trim(textarea.val())!="" && textarea.val().length < 250)
  {
	MetaFieldUtil.updateJobTitle(id, textarea.val(), {
		callback: function(result) {
			$j("#synchro-metaform").submit();
		},
		async: false,
		timeout: 20000
		});	
  	$j(this).attr("disabled", "disabled");
	$j(this).attr("value", "Saving");
	$j(".edit-cancel").attr("disabled", "disabled");	
  }
   else
   {  
		$j("#jive-propValue-error-"+id).show();
   }   
});

$j(document).ready(function () {
$j("#add").click(function(){
	$j('[id^="select-region-"').each(function(){ 
		$j(this).attr('disabled', 'disabled');
	});
	$j('ul[id^="edit-box-"]').each(function(){
		$j(this).hide();
	});
	
   $j("#add-box").show();
   $j("#add").attr("disabled",true);
});

$j("#cancel-btn").click(function(){
$j(".jive-error-text").each(function(){ 
    $j(this).hide();
  });
   $j("#add-box").hide();
   $j("#add").attr("disabled",false);
});

$j(".edit-cancel").click(function(){
$j(".jive-error-text").each(function(){ 
    $j(this).hide();
  });
 $j("#add").removeAttr("disabled");
	$j(this).parent().parent().hide();
	$j("[id^='select-region-']").each(function(){ 
    $j(this).attr('disabled', 'disabled');
  });
});

$j("#add-btn").click(function(){
$j(".jive-error-text").each(function(){ 
    $j(this).hide();
  });
  var region = $j("#select-region").val();
  var textarea = $j("#jive-propValue");  
  if(textarea.val()!=null && $j.trim(textarea.val())!="" && textarea.val().length < 250)
  {
  	$j(this).attr("disabled", "disabled");
	$j(this).attr("value", "Saving");
	$j(".add-cancel").attr("disabled", "disabled");	
	MetaFieldUtil.addJobTitle(textarea.val(),{
		callback: function(result) {
			$j("#synchro-metaform").submit();
		},
		async: false,
		timeout: 20000
		});	
   }
   else
   {  
		$j("#jive-propValue-error").show();
   }
});

/**
** Drag Drop jQuery
**/
	$j( ".ui-sortable" ).sortable({
			stop: function(event, ui) {$j("#updatesort").removeAttr("disabled");}
	});
//	$j( ".ui-sortable" ).disableSelection();	
	});
</script>