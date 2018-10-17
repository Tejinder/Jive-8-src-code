package com.grail.synchro.action.reports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.DataExtractReport;
import com.grail.synchro.beans.DataExtractReportFilters;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroReportUtil;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class FinancialDataExtractAction extends JiveActionSupport {
	
	private SynchroReportManager synchroReportManager;
	private static final Logger LOG = Logger.getLogger(FinancialDataExtractAction.class);
	 private PermissionManager permissionManager;
	 private String token;
		private String tokenCookie;
		    
		 	public void setTokenCookie(String tokenCookie) {
			this.tokenCookie = tokenCookie;
		}

			public void setToken(String token) {
			this.token = token;
		}
	 	public PermissionManager getPermissionManager() {
	        if(permissionManager == null){
	        	permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
	        }
	        return permissionManager;
	    }
    @Override
    public String input(){
       //TODO Permission check
    	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
            	return UNAUTHORIZED;
            }
        }
        if(SynchroPermHelper.isCommunicationAgencyUser() || SynchroPermHelper.isExternalAgencyUser())
    		return UNAUTHORIZED;
        return INPUT;
    }
    
    @Override
    public String execute(){
        //TODO Permission check 
    	if(SynchroPermHelper.isCommunicationAgencyUser() || SynchroPermHelper.isExternalAgencyUser())
    		return UNAUTHORIZED;
    	
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
            	return UNAUTHORIZED;
            }
        }
     	DataExtractReportFilters financialDataExtractFilters = new DataExtractReportFilters() ;
         //TODO Permission check
 		if (getRequest().getMethod().equalsIgnoreCase("POST")) {

 			ServletRequestDataBinder binder = new ServletRequestDataBinder(
 					financialDataExtractFilters);
 			binder.bind(getRequest());
 			
 			List<DataExtractReport> financialDataExtract = synchroReportManager
 					.getProjectDataExtractReport(financialDataExtractFilters);
 			HSSFWorkbook workbook = SynchroReportUtil
 					.generateDataExtractReport(financialDataExtractFilters,financialDataExtract);
 			try {
 				response.setHeader("Content-Disposition",
 						"attachment;filename=FinancialDataExtract.xls");
 				response.setContentType("application/vnd.ms-excel");
 				generateTokenCookie();
 				workbook.write(response.getOutputStream());
 				LOG.debug("Project Data Extract has been created successfully");

 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
         return SUCCESS;
     }

	public SynchroReportManager getSynchroReportManager() {
		return synchroReportManager;
	}

	public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
		this.synchroReportManager = synchroReportManager;
	}
    private void generateTokenCookie()
    {
    	if(token!=null && tokenCookie!=null)
    	{
    		Cookie cookie = new Cookie(tokenCookie, token);
    		cookie.setMaxAge(1000*60*10);    	
    		response.addCookie(cookie);
    	}
    	
    }
}
