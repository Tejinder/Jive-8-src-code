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
				 com.grail.synchro.beans.MetaField,
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
    </style>
	
<script type="text/javascript" src="<s:url value='/dwr/interface/MetaFieldUtil.js'/>"></script>

<style type="text/css">
	@import "<s:url value='/admin/style/synchro-admin.css'/>";
	#methodology-other{width: 100% !important;float: left !important;margin: 10px !important;}
	#methodology-other label{float: left !important;min-width: 200px !important;}
	#methodology-other select{width: 71px !important;height: 20px !important;min-height: 10px !important;float: left !important;margin-left: 20px !important;}
	#methodology-others select{width: 200px !important;}
	.j-form-row textarea, .j-form-row input[type=text]{width: 550px !important;}
	#methodology-other-prop{float: left;width: 100%;margin:5px;}
	#methodology-other-prop select{float: right !important;width: 50px !important;}
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
    <content tag="pageID">proposed-methodology</content>
    <content tag="pagehelp">
        <h3>Proposed Methodologies</h3>       
    </content>
</head>

<form action="/admin/synchro-proposed-methodology.jsp" name="synchro-metaform" id="synchro-metaform" method="POST">

        <table class="space-table" width="100%" border="0" cellpadding="0" cellspacing="0">
			<tbody>
				<tr>
					<td colspan="4">
						<p style="padding: 5px;">
							Drag and drop to reorder proposed methodologies.</p>
						<div id="tree-div">
							<ul class="jstree">
								<li id="space_1" class="">
									
									<span>Proposed Methodology</span>
									<ul id="space_1_ul" class="ui-sortable" style="display: block;">
	
	<%
		Integer defaultValue = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_PROPOSEDMETHOGOLOGY_ID, -1);
		Map<Integer, String> methodologies = SynchroGlobal.getMethodologies();	
		Map<Integer, MetaField> methodologyProperties = SynchroGlobal.getMethodologyProperties();	
		for(Integer idm : methodologyProperties.keySet())
		{
			System.out.println("IDm " + idm + " | " + methodologyProperties.get(idm).isLessFrequent());
			//
		}
		for(Integer id : methodologies.keySet())
		{			
			String name = methodologies.get(id);
		if(defaultValue==id){%>
		<li id="<%=id%>" class="main selected-property">
		<%}else{%>
		<li id="<%=id%>" class="main">
		<%}%>
		<span><%=name%></span>		
			<div class="brands-select">
			<% if(defaultValue==id)
			{%>
			<span class="default-prop" id="default-prop-<%=id%>">Default</span>
			<%
			}
			else
			{%>
			<span class="set-default-prop" id="default-prop-<%=id%>">Set as Default</span>
			<%}%>
		
			<a href="javascript:void(0);" onclick="editProperty(<%=id%>)">
				<img src="../images/jive-icon-edit-16x16.gif" width="16" height="16" alt="Edit Proposed Methodology" border="0">
			</a>
			<a class="enable-delete-link" onclick="deleteProperty(<%=id%>)">
				<img src="../images/jive-icon-delete-16x16.gif" width="16" height="16" alt="Delete Property" border="0">
			</a>
			<ul id="edit-box-<%=id%>" style="display:none">		
				<li>
					<div class="j-form-row">
						<textarea cols="70" rows="5" id="jive-propValue-<%=id%>" name="propValue" ><%=name%></textarea>			
					</div>
				
					<span id="jive-propValue-error-<%=id%>" class="jive-error-text error" style="display:none"><br>Please enter a property value (max 250 characters).</span>	
				</li>
				<li>
					<div class="form-select_div">
					<label><span class="list">All Methodology Groups</span></label>
					<!-- All Properties -->					
					<select size="3" name="all-properties-<%=id%>" id="all-properties-<%=id%>" multiple="yes">
					
					<%
					Map<Integer, String> groupsAll = SynchroGlobal.getMethodologyGroups(true, new Long(1));
					
					for(Integer groupID : groupsAll.keySet())
					{
						Map<Integer, String> methodologiesByGroup = SynchroGlobal.getMethodologiesByGroup(false, Long.parseLong(String.valueOf(groupID)));
						String groupName = groupsAll.get(groupID);
						if(methodologiesByGroup.containsKey(id))
						{					
						%>
							<option value="<%=groupID%>" selected="true"><%=groupName%></option>
						<%
						}
						else
						{
						%>
							<option value="<%=groupID%>"><%=groupName%></option>
						<%
						}					
					}
					%>
					</select>
					
					<div class="action_buttons">						
						<input id="add-property-<%=id%>" type="button" value='>>' class="left_arrow" />
						<input id="remove-property-<%=id%>" type="button" value='<<' class="right_arrow" />
					</div>
				</div>
				<div class="form-select_div_brand">
					<label><span class="list">Selected Methodology Groups</span></label>
					<select size="3" name="selected-properties-<%=id%>" id="selected-properties-<%=id%>" multiple="yes" class=""></select>
				</div>
				<script type="text/javascript">
					$j(function() {
						$j("#add-property-<%=id%>, #remove-property-<%=id%>").click(function(event) {
							var id = $j(event.target).attr("id");
							var selectFrom = id == "add-property-<%=id%>" ? "#all-properties-<%=id%>" : "#selected-properties-<%=id%>";
							var moveTo = (id == "add-property-<%=id%>") ? "#selected-properties-<%=id%>" : "#all-properties-<%=id%>";
							var selectedItems = $j(selectFrom + " :selected").toArray();
							$j(moveTo).append(selectedItems);			
						});
						$j("#add-property-<%=id%>").click();
					});
				</script>
				
				</li>
				
				<% MetaField metafld = methodologyProperties.get(id); %>
					<div id="methodology-others">
						<div id="methodology-other">
						<label>Is Less Frequent?</label>
							<select id="isLessFrequent-<%=id%>">
								<option value="0">No</option>
								<%
								if(metafld.isLessFrequent())
								{ %>
								<option value="1" selected="true">Yes</option>
								<%}
								else
								{%>
								<option value="1">Yes</option>
								<%}
								%>
							</select>
						</div>
						<div id="methodology-other">
						<label>Is Brief Exception?</label>
							<select id="briefException-<%=id%>">
								<option value="0">No</option>
								<%
								if(metafld.isBriefException())
								{ %>
								<option value="1" selected="true">Yes</option>
								<%}
								else
								{%>
								<option value="1">Yes</option>
								<%}
								%>
							</select>
						</div>
						<div id="methodology-other">
						<label>Is Proposal Exception?</label>
							<select id="proposalException-<%=id%>">
								<option value="0">No</option>
								<%
								if(metafld.isProposalException())
								{ %>
								<option value="1" selected="true">Yes</option>
								<%}
								else
								{%>
								<option value="1">Yes</option>
								<%}
								%>
							</select>
						</div>
						<div id="methodology-other">
						<label>Is Agency Waiver Exception?</label>
							<select id="agencyWaiverException-<%=id%>">
								<option value="0">No</option>
								<%
								if(metafld.isAgencyWaiverException())
								{ %>
								<option value="1" selected="true">Yes</option>
								<%}
								else
								{%>
								<option value="1">Yes</option>
								<%}
								%>
							</select>
						</div>						
						<div id="methodology-other">
						<label>Is Report Summary Exception?</label>
							<select id="repSummaryException-<%=id%>">
								<option value="0">No</option>
								<%
								if(metafld.isRepSummaryException())
								{ %>
								<option value="1" selected="true">Yes</option>
								<%}
								else
								{%>
								<option value="1">Yes</option>
								<%}
								%>
							</select>
						</div>
						<div id="methodology-other">
						<label>Is Brand Specific?</label>
							<select id="brandSpecific-<%=id%>">
							
								<option value="0" <% if(metafld.getBrandSpecific()==0){ %> selected="true" <%} %>>Select</option>
								<option value="1" <% if(metafld.getBrandSpecific()==1){ %> selected="true" <%} %>>Yes</option>
								<option value="2" <% if(metafld.getBrandSpecific()==2){ %> selected="true" <%} %>>No</option>
								
								
							</select>
						</div>
					</div>
					
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
			<div class="add-prop-header">Add Proposed Methodology</div>
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
		<p class="select-region-heading">Select Methodology Groups</p>		
				<div class="brands-select">
					<select id="select-properties" multiple style="width:200px !important;">
					<%
					Map<Integer, String> allGroups = SynchroGlobal.getMethodologyGroups(true, Long.parseLong("1"));

					for(Integer groupId : allGroups.keySet())
						{
						String gname = allGroups.get(groupId);				
						%>
						<option value="<%=groupId%>"><%=gname%></option>
						<%
					}%>
					</select>
				</div>	
		
			</td>
			<td class="other-properties-main">
			<div id="methodology-other-prop"><label>Is Less Frequent?</label>
				<select id="isLessFrequent-new">
					<option value="0">No</option>
					<option value="1">Yes</option>
				</select>
			</div>
			<div id="methodology-other-prop">
				<label>Is Brief Exception?</label>
				<select id="briefException-new">
					<option value="0">No</option>
					<option value="1">Yes</option>
				</select>
			</div>
			<div id="methodology-other-prop">
				<label>Is Proposal Exception?</label>
				<select id="proposalException-new">
					<option value="0">No</option>
					<option value="1">Yes</option>
				</select>
			</div>
			<div id="methodology-other-prop">
				<label>Is Agency Waiver Exception?</label>
				<select id="agencyWaiverException-new">
					<option value="0">No</option>
					<option value="1">Yes</option>
				</select>
			</div>
			<div id="methodology-other-prop">
				<label>Is Report Summary Exception?</label>
				<select id="repSummaryException-new">
					<option value="0">No</option>
					<option value="1">Yes</option>
				</select>
			</div>	
			<div id="methodology-other-prop">
				<label>Is Brand Specific?</label>
				<select id="brandSpecific-new">
					<option value="2">No</option>
					<option value="1">Yes</option>
				</select>
			</div>				
			</td>
			</tr>		
		
		
	<tr>	
		<td colspan="3">
			<input type="button" id="add" name="add" value="Add Proposed Methodology" />			
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
	$j("#add-box-others").hide();
}

function deleteProperty(id)
{
	var res = confirm("Delete item?");
	if (res == true)
	  {
	  MetaFieldUtil.deleteMethodology(id, {
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
	MetaFieldUtil.sortMethodology(orderMap, {
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
   var select_id ="selected-properties-"+id;
   var textarea = $j("#jive-propValue-"+id);
   var arr = $j("#"+select_id).find("option").map(function() { return this.value; }).get();	
   console.log("array " + arr.join(','));
   var isLessFrequentID ="isLessFrequent-"+id;
   var briefExceptionID ="briefException-"+id;
   var proposalExceptionID ="proposalException-"+id;
   var agencyWaiverExceptionID ="agencyWaiverException-"+id;
   var repSummaryException ="repSummaryException-"+id;
   
   var brandSpecific ="brandSpecific-"+id;
   
  if(textarea.val()!=null && $j.trim(textarea.val())!="" && textarea.val().length < 250)
  {
	MetaFieldUtil.updateMethodology(id, textarea.val(), arr.join(','), $j("#"+isLessFrequentID).val(),$j("#"+briefExceptionID).val(),$j("#"+proposalExceptionID).val(),$j("#"+agencyWaiverExceptionID).val(),$j("#"+repSummaryException).val(),$j("#"+brandSpecific).val(), {
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
   $j("#add-box-others").show();
   $j("#add").attr("disabled",true);
});

$j("#cancel-btn").click(function(){
$j(".jive-error-text").each(function(){ 
    $j(this).hide();
  });
   $j("#add-box").hide();
   $j("#add-box-others").hide();
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
	var selectedValues = $j('#select-properties').val();	
	MetaFieldUtil.addMethodology(textarea.val(),selectedValues.join(","), $j("#isLessFrequent-new").val(), $j("#briefException-new").val(), $j("#proposalException-new").val(), $j("#agencyWaiverException-new").val(), $j("#repSummaryException-new").val(),$j("#brandSpecific-new").val(), {
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
		
	$j(".set-default-prop").click(function(){
		var id = $j(this).attr("id");
		var $id = id.substring(('default-prop-').length);
		MetaFieldUtil.updateDefaultValue('grail.synchro.default.proposedmethodology',$id, {
		callback: function(result) {
			$j("#synchro-metaform").submit();
		},
		async: false,
		timeout: 20000
		});
	});
	
	});



</script>