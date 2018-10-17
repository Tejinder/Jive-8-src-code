package com.grail.synchro.action.reports;

import javax.servlet.http.Cookie;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class AgencyEvaluationReportAction extends JiveActionSupport {

	private SynchroReportManager synchroReportManager;
	private ProjectManager synchroProjectManager;
	private PermissionManager permissionManager;
	private String token;
	private String tokenCookie;

		public PermissionManager getPermissionManager() {
	        if(permissionManager == null){
	        	permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
	        }
	        return permissionManager;
	    }
	 	
	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
		this.synchroProjectManager = synchroProjectManager;
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
    	if(SynchroPermHelper.isCommunicationAgencyUser() || SynchroPermHelper.isExternalAgencyUser())
    		return UNAUTHORIZED;
        return INPUT;
    }
    
    @Override
    public String execute(){
    	
    	/*// This will check whether the user has accepted the Disclaimer or not.
    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
    	{
    		return UNAUTHORIZED;
    	}
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
            	return UNAUTHORIZED;
            }
        }
    	if(SynchroPermHelper.isCommunicationAgencyUser() || SynchroPermHelper.isExternalAgencyUser())
    		return UNAUTHORIZED;
    	
    	EvaluationAgencyReportFilters evaluationAgencyReportFilters = new EvaluationAgencyReportFilters();
    	List<AgencyEvaluationReport> agencyEvaluationReportList = new ArrayList<AgencyEvaluationReport>();
 	   if(getRequest().getMethod().equalsIgnoreCase("POST")) {
		   
		   ServletRequestDataBinder binder = new ServletRequestDataBinder(evaluationAgencyReportFilters);
           binder.bind(getRequest());
           	List<Long> projectIDs = synchroReportManager.getAgencyEvaluationReport(evaluationAgencyReportFilters);
           	for(Long projectID : projectIDs)
           	{
           	try{
	             AgencyEvaluationReport agencyEvaluationReport = new AgencyEvaluationReport();
	           	 Project project = synchroProjectManager.get(projectID);
	           	 List<CoordinationDetail> coordinationDetails = synchroProjectManager.getCoordinationDetails(projectID);
	        	 agencyEvaluationReport.setCoordinationDetails(coordinationDetails);
	        	 agencyEvaluationReport.setProjectID(projectID);
	        	 agencyEvaluationReport.setOwner(project.getOwnerID());
	        	 agencyEvaluationReport.setProjectDescription(project.getDescription());
	        	 agencyEvaluationReport.setStartMonth(project.getStartMonth());
	        	 agencyEvaluationReport.setStartYear(project.getStartYear());
	        	 agencyEvaluationReport.setEndMonth(project.getEndMonth());
	        	 agencyEvaluationReport.setEndYear(project.getEndYear());
	        	 agencyEvaluationReportList.add(agencyEvaluationReport);
           	}catch(Exception e){log.error("Error while getting Agency Evaluation details for project id "+projectID);}
           	}
             HSSFWorkbook workbook = SynchroReportUtil.generateAgencyEvaluationReport(agencyEvaluationReportList, evaluationAgencyReportFilters.getStartMonth(), evaluationAgencyReportFilters.getStartYear(), evaluationAgencyReportFilters.getEndMonth(), evaluationAgencyReportFilters.getEndYear());
       try{
    		response.setHeader("Content-Disposition", "attachment;filename=AgencyEvaluationreport.xls");
    		response.setContentType("application/vnd.ms-excel");
    		generateTokenCookie();
        	workbook.write(response.getOutputStream());
    	     
    	} catch (FileNotFoundException e) {
    	    e.printStackTrace();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
	   }*/
        return SUCCESS;
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
}
