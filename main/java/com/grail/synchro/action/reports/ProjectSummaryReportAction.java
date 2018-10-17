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
 * This class handles all operation related to the Reports Generation.
 *
 * @author: Tejinder
 * @since: 1.0
 */

public class ProjectSummaryReportAction extends JiveActionSupport {

    private static final Logger LOG = Logger.getLogger(ProjectSummaryReportAction.class);
    private Long projectID;
    private SynchroReportManager synchroReportManager;
    private PermissionManager permissionManager;
    private String token;
	private String tokenCookie;
	private Boolean reportcatalogue = true;
    private boolean adminUser = false;
    
	public boolean isAdminUser() {
		return adminUser;
	}
	
 	public Boolean getReportcatalogue() {
	return reportcatalogue;
}
 	public PermissionManager getPermissionManager() {
        if(permissionManager == null){
        	permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }
 	
    @Override
    public String execute(){
    	//TODO Actions
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
        adminUser = SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin();
    	return SUCCESS;
    }
    public String extractReport(){
    	
    	HSSFWorkbook projectSummary = synchroReportManager.getProjectSummaryReport(projectID);
    	try {
    		response.setHeader("Content-Disposition", "attachment;filename=ProjectSummaryReport.xls");
    		response.setContentType("application/vnd.ms-excel");
    		generateTokenCookie();
    		projectSummary.write(response.getOutputStream()); // Write workbook to response.
    	     
    	} catch (FileNotFoundException e) {
    	    e.printStackTrace();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	//return SUCCESS;
    	return null;
    }
    
    
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public SynchroReportManager getSynchroReportManager() {
		return synchroReportManager;
	}
	public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
		this.synchroReportManager = synchroReportManager;
	}
	
    private void generateTokenCookie()
    {
    	token = request.getParameter("token");
    	tokenCookie = request.getParameter("tokenCookie");
    	if(token!=null && tokenCookie!=null)
    	{
    		Cookie cookie = new Cookie(tokenCookie, token);
    		cookie.setMaxAge(1000*60*10);    	
    		response.addCookie(cookie);
    	}
    	
    }

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenCookie() {
		return tokenCookie;
	}

	public void setTokenCookie(String tokenCookie) {
		this.tokenCookie = tokenCookie;
	}
    
}
