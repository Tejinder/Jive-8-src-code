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
    <content tag="pageID">methodology-groups</content>
    <content tag="pagehelp">
        <h3>Methodology Groups</h3>
    </content>
</head>

<form action="/admin/synchro-methodology-groups.jsp" name="synchro-metaform" id="synchro-metaform" method="POST">

    <table class="space-table" width="100%" border="0" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td>
                <p style="padding: 5px;">
                    Drag and drop to reorder methodology groups.</p>
                <div id="tree-div">
                    <ul class="jstree">
                        <li id="space_1" class="">

                            <span>Methodology Groups</span>
                            <ul id="space_1_ul" class="ui-sortable" style="display: block;">

                                <%
                                    Integer defaultValue = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_METHOGOLOGYGROUP_ID, -1);
                                    Map<Integer, String> methodologyGroups = SynchroGlobal.getMethodologyGroups(true, new Long(1));

                                    for(Integer id : methodologyGroups.keySet())
                                    {
                                        String name = methodologyGroups.get(id);
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
                                            <img src="../images/jive-icon-edit-16x16.gif" width="16" height="16" alt="Edit Methodology Group" border="0">
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
                                                    <label><span class="list">All Proposed Methodologies</span></label>
                                                    <!-- All Properties -->
                                                    <select size="3" name="all-properties-<%=id%>" id="all-properties-<%=id%>" multiple="yes">
                                                        <%
                                                            Map<Integer, String> methodologies = SynchroGlobal.getMethodologies();
                                                            Map<Integer, Map<Integer, String>> mapping = SynchroGlobal.getMethodologyMapping();
                                                            Map<Integer, String> methdologiesSelected = mapping.get(id);
                                                            for(Integer methodologyID : methodologies.keySet())
                                                            {
                                                                String methodologyName = methodologies.get(methodologyID);
                                                                if(methdologiesSelected.containsKey(methodologyID))
                                                                {
                                                        %>
                                                        <option value="<%=methodologyID%>" selected="true"><%=methodologyName%></option>
                                                        <%}
                                                        else
                                                        {%>
                                                        <option value="<%=methodologyID%>"><%=methodologyName%></option>
                                                        <%      }
                                                        }%>
                                                    </select>
                                                    <div class="action_buttons">
                                                        <input id="add-property-<%=id%>" type="button" value='>>' class="left_arrow" />
                                                        <input id="remove-property-<%=id%>" type="button" value='<<' class="right_arrow" />
                                                    </div>
                                                </div>
                                                <div class="form-select_div_brand">
                                                    <label><span class="list">Selected Proposed Methodologies</span></label>
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
                <div class="add-prop-header">Add Methodology Group</div>
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
                <p class="select-region-heading">Select Proposed Methodology</p>
                <div class="brands-select">
                    <select id="select-properties" multiple>
                        <%
                            Map<Integer, String> methdologies = SynchroGlobal.getMethodologies();
                            for(Integer id : methdologies.keySet())
                            {
                                String name = methdologies.get(id);
                        %>
                        <option value="<%=id%>"><%=name%></option>
                        <%
                            }	%>
                    </select>
                </div>

            </td>
        </tr>
        <tr>
            <td colspan="3">
                <input type="button" id="add" name="add" value="Add Methodology Group" />
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
            MetaFieldUtil.deleteMethodologyGroup(id, {
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
                orderMap[$j(this).attr("id")] = idx;
                idx++;
            }
        });

        MetaFieldUtil.sortMethodologyGroup(orderMap, {
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
        if(textarea.val()!=null && $j.trim(textarea.val())!="" && textarea.val().length < 250)
        {
            MetaFieldUtil.updateMethodologyGroup(id,textarea.val(), arr.join(','), {
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
                var selectedValues = $j('#select-properties').val();
                MetaFieldUtil.addMethodologyGroup(textarea.val(),selectedValues.join(","), {
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
            MetaFieldUtil.updateDefaultValue('grail.synchro.default.methodologygroup',$id, {
                callback: function(result) {
                    $j("#synchro-metaform").submit();
                },
                async: false,
                timeout: 20000
            });
        });

    });



</script>