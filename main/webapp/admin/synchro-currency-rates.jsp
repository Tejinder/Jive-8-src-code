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
    String title = "Currency Rates";
    JiveContext ctx = JiveApplication.getContext();
    JiveLicense license = ctx.getLicenseManager().getJiveLicense();
	Map<Integer, String> currencies = SynchroGlobal.getCurrencies();
	SynchroUtils utils = new SynchroUtils();	
	for(Integer key : currencies.keySet())
	{
		String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencies.get(key));		
		String propValue = ParamUtils.getParameter(request,currencyKey);
		
		if(propValue != null)
		{
			String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase());
            //utils.updateExchangeRate(key);
			if(currencyValue==null || (currencyValue!=null && !currencyValue.equals(propValue)))
				{				
					JiveGlobals.setJiveProperty(currencyKey.toLowerCase(),propValue.trim());
					
					utils.setExchangeRate(key);
				}				
		}
    } 
	
%>

<head>
    <title><%= title %></title>
    <content tag="pagetitle"><%= title %></content>
    <content tag="pageID">currency-rates</content>
    <content tag="pagehelp">
        <h3>Currency</h3>       
    </content>
</head>
<form action="/admin/synchro-currency-rates.jsp" name="currency-form" id="currency-form" method="POST">

<div class="jive-table">
<table cellpadding="0" cellspacing="0" border="0" width="100%">
<thead>
    <tr>
        <th colspan="3">
           Currency Rates
        </th>
    </tr>
</thead>
<tbody>
<%
	
	currencies = SynchroGlobal.getCurrencies();
	Map<Integer, String> currencyDescriptions = SynchroGlobal.getCurrencyDescriptions();
	for(Integer key : currencies.keySet())
	{
	%>
	<tr>
		<td>
		<%=currencies.get(key)%>
		</td>
		
		<td>
		<%=currencyDescriptions.get(key)%>
		</td>
		<td>
			<%
			String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencies.get(key));
			String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
			%>
			<input class="currency-field numericfield" type="text" name="<%=currencyKey%>" value="<%=currencyValue%>" /><span class="jive-error-message" style="display:none;">Please enter numeric value</span>
		</td>
	</tr>
	<%
	
	}
%>
</tbody>
</table>
</div>
</form>
<input type="button" id="save" value="Save" />

<br />

<script type="text/javascript">
	$j( "#save" ).click(function() {
		var error = false;
		$j("form#currency-form .jive-error-message").each(function(){
			if($j(this).css("display") == "block")
			{
			error = true;
			return;
			}
		});
		if(!error)
		{
			$j("#currency-form").submit();
		}	
	});
	
	function isDecInteger(amount) {
    var numbers = /(^\d*(?:\.|\,)?\d*[1-9]+\d*$)|(^[1-9]+\d*(?:\.|\,)\d*$)/;
    if (amount.val().match(numbers)) {
        return true;
    } else {
        amount.focus();
        return false;
    }
}
	$j(".numericfield").change(function(event) {
		if($j(this).val() != "")
		{
		if (!isDecInteger($j(this))) {
			$j(this).next().show();
		}
		else
		{
			$j(this).next().hide();
		}
		}
	});
</script>