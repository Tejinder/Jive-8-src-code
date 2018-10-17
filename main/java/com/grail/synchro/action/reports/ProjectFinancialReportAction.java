package com.grail.synchro.action.reports;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * This class handles all operation related to the Project Financial Report.
 *
 * @author: Tejinder
 * @since: 1.0
 */

public class ProjectFinancialReportAction extends JiveActionSupport {

    private static final Logger LOG = Logger.getLogger(ProjectFinancialReportAction.class);
    private SynchroReportManager synchroReportManager;
    private Long projectID;
    private PermissionManager permissionManager;
    private String token;
	private String tokenCookie;
    private boolean adminUser = false;
    
	public boolean isAdminUser() {
		return adminUser;
	}
	private Boolean reportcatalogue = true;
	
	 	public Boolean getReportcatalogue() {
		return reportcatalogue;
	}

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
    public String execute(){
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
    	
    	adminUser = SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin();
        return SUCCESS;
    }

	public String extractReport() {

		HSSFWorkbook financeReport = synchroReportManager
				.getProjectFinancialReport(projectID);
		try {
			response.setHeader("Content-Disposition",
					"attachment;filename=ProjectFinancialReport.xls");
			response.setContentType("application/vnd.ms-excel");
			
			// Write workbook to response.
			generateTokenCookie();
			financeReport.write(response.getOutputStream()); 

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return SUCCESS;
		return null;
	}

	public SynchroReportManager getSynchroReportManager() {
		return synchroReportManager;
	}

	public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
		this.synchroReportManager = synchroReportManager;
	}

	public Long getProjectID() {
		return projectID;
	}

	public void setProjectID(Long projectID) {
		this.projectID = projectID;
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
