package com.grail.synchro.action.reports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.ResearchCycleReport;
import com.grail.synchro.beans.ResearchCycleReportFilters;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.SynchroReportUtil;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class ProjectResearchCycleReportAction extends JiveActionSupport {
	private SynchroReportManager synchroReportManager;
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
    public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
		this.synchroReportManager = synchroReportManager;
	}

	@Override
    public String input(){
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
       //TODO Permission check
        return INPUT;
    }
    
    @Override
    public String execute(){
    	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return UNAUTHORIZED;
//    	}
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
            	return UNAUTHORIZED;
            }
        }
    	ResearchCycleReportFilters researchCycleReportFilters = new ResearchCycleReportFilters() ;
       //TODO Permission check
    	   if(getRequest().getMethod().equalsIgnoreCase("POST")) {
    		   
    		   ServletRequestDataBinder binder = new ServletRequestDataBinder(researchCycleReportFilters);
               binder.bind(getRequest());
               List<ResearchCycleReport> researchCycleReportList = synchroReportManager.getResearchCycleReport(researchCycleReportFilters);
               
               HSSFWorkbook workbook = SynchroReportUtil.generateResearchCycleReport(researchCycleReportFilters, researchCycleReportList, researchCycleReportFilters.getStartMonth(), researchCycleReportFilters.getStartYear(), researchCycleReportFilters.getEndMonth(), researchCycleReportFilters.getEndYear());
           	try{
        		response.setHeader("Content-Disposition", "attachment;filename=ResearchCycleReport.xls");
        		response.setContentType("application/vnd.ms-excel");
        		generateTokenCookie();
            	workbook.write(response.getOutputStream());
        	} catch (FileNotFoundException e) {
        	    e.printStackTrace();
        	} catch (IOException e) {
        	    e.printStackTrace();
        	}
    	   }
    	   
        return SUCCESS;
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
