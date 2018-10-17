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
    <script type="text/javascript" src="<s:url value='/resources/scripts/jive/namespace.js' />"></script>--%>
	
	
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
		#other-options-approval{width: 100% !important; float: left !important;}
		.jstree li li a.enable-delete-link{}
		#other-options-approval > div {    float: left;
		width: 100%;}
		.jstree li li a{    position: absolute;
		right: 0;}
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
    String title = "Synchro EndMarkets";
    JiveContext ctx = JiveApplication.getContext();
    //JiveLicense license = ctx.getLicenseManager().getJiveLicense();		
	
	
%>

<head>
    <title><%= title %>11</title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">end-market</content>
    <content tag="pagehelp">
        <h3>End Markets</h3>       
    </content>
</head>

<form action="/admin/synchro-endmarkets.jsp" name="synchro-metaform" id="synchro-metaform" method="POST">

        <table class="space-table" width="100%" border="0" cellpadding="0" cellspacing="0">
			<tbody>
				<tr>
					<td>
						<p style="padding: 5px;">
							Drag and drop to reorder end markets at the same level. </p>
						<div id="tree-div">
							<ul class="jstree">
								<li id="space_1" class="">									
									<span>End Markets</span>
									<ul id="space_1_ul" class="ui-sortable" style="display: block;">
	
	<%
		Map<Integer, String> endMarkets = SynchroGlobal.getEndMarkets();			
		Map<Integer, String> regions = SynchroGlobal.getRegions();
		Map<Integer, Map<Integer, String>> mapping = SynchroGlobal.getRegionEndMarketsMapping();
		Map<Integer, Integer> approvalTypeEMMap = SynchroGlobal.getEndmarketApprovalTypeMap();	
		Map<Integer, Integer> marketTypeEMMap = SynchroGlobal.getEndmarketMarketTypeMap();
		
		Map<Integer, Integer> t20_t40_TypeEMMap = SynchroGlobal.getEndmarketT20_T40_TypeMap();
		
		Map<Integer, String> approvalTypes = SynchroGlobal.getEMApprovalTypes();
		Map<Integer, String> marketTypes = SynchroGlobal.getEUMarketTypes();
		Map<Integer, String> t20_t40_Types = SynchroGlobal.getT20_T40_Types();
		for(Integer id : endMarkets.keySet())
		{
			String name = endMarkets.get(id);
		%>
		<li id="<%=id%>" class="main">
		<span><%=name%></span>		
			<select disabled id="select-region-<%=id%>">
                <option value="-1">-- None --</option>
			<%
			for(Integer regionID : regions.keySet())
				{
					String regionName = regions.get(regionID);
					Map<Integer, String> endMarketsByRegion = mapping.get(regionID);
					if(endMarketsByRegion.containsKey(id))
					{%>
					<option selected="true" value="<%=regionID%>"><%=regionName%></option>
					<%}
					else
					{%>
					<option value="<%=regionID%>"><%=regionName%></option>
					<%}
				}
			%>	
			</select>
			
			<div id="other-options-approval">
				<div id="approval-type-main">
					<span>Approval Type</span>
					<select disabled id="select-approval-<%=id%>">
						<option value="-1">-- None --</option>
						<%
						for(Integer approvalType : approvalTypes.keySet())
							{
								String approvalName = approvalTypes.get(approvalType);								
								if(approvalTypeEMMap.get(id)==approvalType)
								{%>
								<option selected="true" value="<%=approvalType%>"><%=approvalName%></option>
								<%}
								else
								{%>
								<option value="<%=approvalType%>"><%=approvalName%></option>
								<%}
							}
						%>				
					</select>
				</div>
				<div id="market-type-main">
					<span>EU Market</span>
					<div id="other-options-markettype">
					<select disabled id="select-markettype-<%=id%>">
						<option value="-1">-- None --</option>
						<%
						for(Integer marketType : marketTypes.keySet())
							{
								String marketName = marketTypes.get(marketType);								
								if(marketTypeEMMap.get(id)==marketType)
								{%>
								<option selected="true" value="<%=marketType%>"><%=marketName%></option>
								<%}
								else
								{%>
								<option value="<%=marketType%>"><%=marketName%></option>
								<%}
							}
						%>					
					</select>
					</div>
				</div>
				
				<div id="t20-type-main">
					<span>T20/T40</span>
					<div id="other-options-markettype">
					<select disabled id="select-t20-t40-type-<%=id%>">
						<option value="-1" >-- None --</option>
						<%
						for(Integer t20_t40_Type : t20_t40_Types.keySet())
						{					
							String t20_t40 = t20_t40_Types.get(t20_t40_Type);																
							
							if(t20_t40_TypeEMMap.get(id)==t20_t40_Type)
							{%>
							
							<option selected="true" value="<%=t20_t40_Type%>"><%=t20_t40%></option>
							<%}
							else
							{%>
							<option value="<%=t20_t40_Type%>"><%=t20_t40%></option>
							<%}
						}
						%>					
					</select>
					</div>
				</div>
			</div>
				<a href="javascript:void(0);" onclick="editProperty(<%=id%>)">
					<img src="../images/jive-icon-edit-16x16.gif" width="16" height="16" alt="Edit End Market Name" border="0">
				</a>
				<a class="enable-delete-link" onclick="deleteProperty(<%=id%>)">
					<img src="../images/jive-icon-delete-16x16.gif" width="16" height="16" alt="Delete Property" border="0">
				</a>
			
			<ul id="edit-box-<%=id%>" style="display:none">		
				<li>
					<div class="j-form-row">
						<textarea cols="70" rows="5" id="jive-propValue-<%=id%>" name="propValue" ><%=name%></textarea>			
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
	
	<tr id="add-box" style="display:none;">	
		<td>
			<div class="add-prop-header">Add End Market Details</div>
			<div class="j-form-row">
				<textarea cols="70" rows="5" id="jive-propValue" name="propValue" ></textarea>			
			</div>
			<div>
				<input class="add-btn" type="button" id="add-btn" value="Save" name="Save" />
				<input class="add-cancel" type="button" id="cancel-btn" value="Cancel" name="Cancel" />
			</div>
			<span id="jive-propValue-error" class="jive-error-text error" style="display:none"><br>Please enter a property value (max 250 characters).</span>
			</td>
			<td style="vertical-align:top" colspan="2">
		<p class="select-region-heading">Region</p>
		<select id="select-region">
		<%
		for(Integer regionID : regions.keySet())
			{
			String regionName = regions.get(regionID);
			%>
				<option value="<%=regionID%>"><%=regionName%></option>
			<%}
		%>	
		</select>
		
	<div id="other-options-approval">
				<div id="approval-type-main">
					<span>Approval Type</span>
					<select id="select-approval">
						<option value="-1" disabled>-- None --</option>
						<%
						for(Integer approvalType : approvalTypes.keySet())
							{
								String approvalName = approvalTypes.get(approvalType);	%>	
								<option value="<%=approvalType%>"><%=approvalName%></option>
						<%	}
						%>				
					</select>
				</div>
				<div id="market-type-main">
					<span>EU Market</span>
					<div id="other-options-markettype">
					<select id="select-markettype">
						<option value="-1" disabled>-- None --</option>
						<%
						for(Integer marketType : marketTypes.keySet())
						{					
							String marketName = marketTypes.get(marketType);																
							%>
							<option value="<%=marketType%>"><%=marketName%></option>
							<%}
						%>					
					</select>
					</div>
				</div>
				
				<div id="t20-type-main">
					<span>T20/T40</span>
					<div id="other-options-markettype">
					<select id="select-t20-t40-type">
						<option value="-1" disabled>-- None --</option>
						<%
						for(Integer t20_t40_Type : t20_t40_Types.keySet())
						{					
							String t20_t40 = t20_t40_Types.get(t20_t40_Type);																
							%>
							<option value="<%=t20_t40_Type%>"><%=t20_t40%></option>
							<%}
						%>					
					</select>
					</div>
				</div>
			</div>		
			</td>
		</tr>
	<tr>	
		<td colspan="3">
			<input type="button" id="add" name="add" value="Add End Market" />			
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
  $j('tr[id^="edit-box-"]').each(function(){
     $j(this).hide();
  }); 
	var row_id = "edit-box-"+id;	
	$j("#"+row_id).show();
	
	var select_id ="select-region-"+id;	
	$j("#"+select_id).removeAttr("disabled");
	
	var select_id ="select-approval-"+id;	
	$j("#"+select_id).removeAttr("disabled");
	
	var select_id ="select-markettype-"+id;	
	$j("#"+select_id).removeAttr("disabled");
	
	var select_id ="select-t20-t40-type-"+id;	
	$j("#"+select_id).removeAttr("disabled");
	
	

	$j("#add-box").hide();
}

function deleteProperty(id)
{
	var res = confirm("Delete item?");
	if (res == true)
	  {
	  MetaFieldUtil.deleteEndMarket(id, {
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
	MetaFieldUtil.sortEndMarketField(orderMap, {
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
   var select_id ="select-region-"+id;
   var select_approval_id ="select-approval-"+id;
   var select_markettype_id ="select-markettype-"+id;
   
   var select_t20_t40_type_id ="select-t20-t40-type-"+id;
   
   var textarea = $j("#jive-propValue-"+id);
   
  if(textarea.val()!=null && $j.trim(textarea.val())!="" && textarea.val().length < 250)
  {
	MetaFieldUtil.updateEndMarket(id, textarea.val(), $j("#"+select_id).val(), $j("#"+select_approval_id).val(), $j("#"+select_markettype_id).val(), $j("#"+select_t20_t40_type_id).val(),  {
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
	$j('tr[id^="edit-box-"]').each(function(){
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
	$j(this).parent().parent().parent().hide();
	$j("[id^='select-region-']").each(function(){ 
    $j(this).attr('disabled', 'disabled');
  });
  
  $j("[id^='select-approval-']").each(function(){ 
    $j(this).attr('disabled', 'disabled');
  });
  
  $j("[id^='select-markettype-']").each(function(){ 
    $j(this).attr('disabled', 'disabled');
  });
});

$j("#add-btn").click(function(){
$j(".jive-error-text").each(function(){ 
    $j(this).hide();
  });
  var region = $j("#select-region").val();
  var approval = $j("#select-approval").val();
  var markettype = $j("#select-markettype").val();
  var t20_t40_type = $j("#select-t20-t40-type").val();
  
  var textarea = $j("#jive-propValue");  
  if(textarea.val()!=null && $j.trim(textarea.val())!="" && textarea.val().length < 250)
  {
  	$j(this).attr("disabled", "disabled");
	$j(this).attr("value", "Saving");
	$j(".add-cancel").attr("disabled", "disabled");
	
	MetaFieldUtil.addEndMarket(textarea.val(), region, approval, markettype, t20_t40_type, {
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