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
    String title = "Country Currency List";
    JiveContext ctx = JiveApplication.getContext();
    JiveLicense license = ctx.getLicenseManager().getJiveLicense();		
	
	
%>

<head>
    <title><%= title %></title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">country-currency</content>
    <content tag="pagehelp">
        <h3>End Markets</h3>       
    </content>
</head>

<form action="/admin/synchro-country-currency.jsp" name="synchro-metaform" id="synchro-metaform" method="POST">

        <table class="space-table" width="100%" border="0" cellpadding="0" cellspacing="0">
			<tbody>
				<tr>
					<td>					
						<div id="tree-div">
							<ul class="jstree">
								<li id="space_1" class="">									
									<span>Country</span>
									<ul id="space_1_ul" class="ui-sortable" style="display: block;">
	
	<%
		Map<Integer, String> endMarkets = SynchroGlobal.getEndMarkets();	
		Map<Integer, String> currencies = SynchroGlobal.getCurrencies();
		Map<String, String> mapping = SynchroGlobal.getCountryCurrencyMap();

		for(Integer id : endMarkets.keySet())
		{
			String name = endMarkets.get(id);
		%>
		<li id="<%=id%>" class="main">
		<span><%=name%></span>		
			<select disabled id="select-region-<%=id%>">
			<option value="-1">Select</option>
			<%			
			for(Integer currencyID : currencies.keySet())
				{
					String currencyName = currencies.get(currencyID);					
					
					if(mapping.containsKey(id.toString()) && (currencyID.toString()).equals(mapping.get(id.toString())))
					{%>
					<option selected="true" value="<%=currencyID%>"><%=currencyName%></option>
					<%}
					else
					{%>
					<option value="<%=currencyID%>"><%=currencyName%></option>
					<%}
				}
			%>	
			</select>
			
				<a href="javascript:void(0);" onclick="editProperty(<%=id%>)">
					<img src="../images/jive-icon-edit-16x16.gif" width="16" height="16" alt="Edit Currency" border="0">
				</a>
			<ul id="edit-box-<%=id%>" style="display:none">		
				<li>
					<div class="j-form-row">
						
					</div>
					<div>
						<input class="save" type="button" id="<%=id%>" value="Save" name="Save" />
						<input class="edit-cancel" type="button" id="cancel-<%=id%>" value="Cancel" name="Cancel" />			
					</div>
					<span id="jive-propValue-error-<%=id%>" class="jive-error-text error" style="display:none"><br>Please enter a property value (max 250 characters).</span>	
				</li>
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
	<tr>		
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
}

$j(".save").click(function(){
$j(".jive-error-text").each(function(){ 
		$j(this).hide();
	 });
	

   var id = $j(this).attr("id");  
   var select_id ="select-region-"+id;   
   
  if($j("#"+select_id).val()>0)
  {
	MetaFieldUtil.setCountryCurrencyMapping(id, $j("#"+select_id).val(), {
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
	$j(".edit-cancel").click(function(){
		
		$j(".jive-error-text").each(function(){
			$j(this).hide();
		  });
		  
		 $j("#add").removeAttr("disabled");
		 $j(this).parent().parent().parent().hide();
		 
		 $j("[id^='select-region-']").each(function(){ 
			$j(this).attr('disabled', 'disabled');
		  });
	});		
});



</script>