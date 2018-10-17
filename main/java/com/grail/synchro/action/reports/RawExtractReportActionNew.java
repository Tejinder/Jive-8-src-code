package com.grail.synchro.action.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroRawExtractUtil;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Tejinder
 * Date: 12/30/16
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class RawExtractReportActionNew extends JiveActionSupport {
    private final Logger LOG = Logger.getLogger(RawExtractReportActionNew.class);
    private PermissionManager permissionManager;

    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;
    private String downloadStreamType = "application/vnd.ms-excel";

    private SynchroReportManager synchroReportManager;
    private ProjectManagerNew synchroProjectManagerNew;
    
    private ProjectResultFilter projectResultFilter;
    

    @Override
    public String execute() {
        if(getUser() != null) {
//            if(!SynchroPermHelper.hasGenerateReportAccess(getUser())) {
//                 return UNAUTHORIZED;
//            }
            if(SynchroPermHelper.isExternalAgencyUser(getUser()) || SynchroPermHelper.isCommunicationAgencyUser(getUser())) {
                return UNAUTHORIZED;
            }

            if(!(SynchroPermHelper.isSynchroMiniAdmin(getUser())
                    || SynchroPermHelper.isSynchroAdmin(getUser())
                    || SynchroPermHelper.isSynchroGlobalSuperUser(getUser())
            )) {
                return UNAUTHORIZED;
            }


        } else {
            return UNAUTHENTICATED;
        }
        return SUCCESS;
    }

    public String downloadReport() throws IOException {
    	
    	processRawExtactReport();
    	
    	//Audit Logs
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
				0, "Raw Extract - Download Report", "", -1L, getUser().getID());
      
        
    	return DOWNLOAD_REPORT;
    }

    private void processRawExtactReport() throws IOException {
        HSSFWorkbook workbook = null;
        workbook = new HSSFWorkbook();
        downloadFilename = "AllProjectsSummary.xls";
       
       
   	 	Calendar calendar = Calendar.getInstance();
        String timeStamp = calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
       
       StringBuilder generatedBy = new StringBuilder();
       generatedBy.append("All Projects Report ").append("\n");
       String userName = getUser().getName();
       generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
       //generatedBy.append("\"").append("Filters:").append(Joiner.on(",").join(selectedFilters)).append("\"").append("\n");
       
       List<Project> globalProjects = new ArrayList<Project>();
       List<Project> regionalProjects = new ArrayList<Project>();
       List<Project> endMarketProjects = new ArrayList<Project>();
       
       List<Long> projectIds = new ArrayList<Long>();
       if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner() || SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isLegaUserType())
       {
	       projectResultFilter = getGlobalSearchFilter();
	       projectResultFilter.setLimit(null);
	 	   projectResultFilter.setStart(null);
	 	   globalProjects= synchroProjectManagerNew.getProjects(projectResultFilter);
       }
       if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner() || SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isLegaUserType())
       {
	       projectResultFilter = getRegionalSearchFilter();
	       projectResultFilter.setLimit(null);
	 	   projectResultFilter.setStart(null);
	 	  regionalProjects= synchroProjectManagerNew.getProjects(projectResultFilter);
       }
       
       if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner() || SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isEndMarketUserType() || SynchroPermHelper.isLegaUserType())
       {
	       projectResultFilter = getEndMarketSearchFilter();
	       projectResultFilter.setLimit(null);
	 	   projectResultFilter.setStart(null);
	 	   endMarketProjects= synchroProjectManagerNew.getProjects(projectResultFilter);
       }
       
     
       
       if(globalProjects!=null && globalProjects.size()>0)
       {
    	   for(Project bean : globalProjects)
    	   {
    		   projectIds.add(bean.getProjectID());
    	   }
       }
       if(regionalProjects!=null && regionalProjects.size()>0)
       {
    	   for(Project bean : regionalProjects)
    	   {
    		   projectIds.add(bean.getProjectID());
    	   }
       }
       if(endMarketProjects!=null && endMarketProjects.size()>0)
       {
    	   for(Project bean : endMarketProjects)
    	   {
    		   projectIds.add(bean.getProjectID());
    	   }
       }
       
      // This will sort the ProjectIds
       Collections.sort(projectIds);
       
        workbook = SynchroRawExtractUtil.generateRawExtract(workbook,generatedBy.toString(),"AllProjects", projectIds);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
              
    }
    
    
    private ProjectResultFilter getGlobalSearchFilter() 
    {
        projectResultFilter = new ProjectResultFilter();

        String bYearSelected = getRequest().getParameter("budgetYearData");
        if(StringUtils.isNotBlank(bYearSelected))
        {
     	   projectResultFilter.setBudgetYearSelected(new Long(bYearSelected));
        }
        
        projectResultFilter.setFetchOnlyUserSpecificProjects(true);
        projectResultFilter.setFetchGlobalProjects(true);
        
        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
       
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal()));
            projectResultFilter.setProjectStatusFields(statuses);
        }
        
     // Cancel Projects should be accessible to Admin users and Synchro System Owners only
        if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner())
        {
        	 projectResultFilter.setFetchCancelProjects(true);
        }
        
       
        return projectResultFilter;
    }
    
    private ProjectResultFilter getRegionalSearchFilter() 
    {
        projectResultFilter = new ProjectResultFilter();

        String bYearSelected = getRequest().getParameter("budgetYearData");
        if(StringUtils.isNotBlank(bYearSelected))
        {
     	   projectResultFilter.setBudgetYearSelected(new Long(bYearSelected));
        }
        
        projectResultFilter.setFetchOnlyUserSpecificProjects(true);
        projectResultFilter.setFetchRegionalProjects(true);
        
        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
       
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal()));
            projectResultFilter.setProjectStatusFields(statuses);
        }
        
     // Cancel Projects should be accessible to Admin users and Synchro System Owners only
        if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner())
        {
        	 projectResultFilter.setFetchCancelProjects(true);
        }
        
       
        return projectResultFilter;
    }
    
    private ProjectResultFilter getEndMarketSearchFilter() 
    {
        projectResultFilter = new ProjectResultFilter();

        String bYearSelected = getRequest().getParameter("budgetYearData");
        if(StringUtils.isNotBlank(bYearSelected))
        {
     	   projectResultFilter.setBudgetYearSelected(new Long(bYearSelected));
        }
        
        projectResultFilter.setFetchOnlyUserSpecificProjects(true);
        projectResultFilter.setFetchEndMarketProjects(true);
        
        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            List<Long> statuses = new ArrayList<Long>();
       
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal()));
            statuses.add(new Long(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal()));
            projectResultFilter.setProjectStatusFields(statuses);
        }
        
     // Cancel Projects should be accessible to Admin users and Synchro System Owners only
        if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner())
        {
        	 projectResultFilter.setFetchCancelProjects(true);
        }
        
       
        return projectResultFilter;
    }
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public SynchroReportManager getSynchroReportManager() {
        return synchroReportManager;
    }

    public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
        this.synchroReportManager = synchroReportManager;
    }

    public InputStream getDownloadStream() {
        return downloadStream;
    }

    public void setDownloadStream(InputStream downloadStream) {
        this.downloadStream = downloadStream;
    }

    public String getDownloadFilename() {
        return downloadFilename;
    }

    public void setDownloadFilename(String downloadFilename) {
        this.downloadFilename = downloadFilename;
    }

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public void setSynchroProjectManagerNew(
			ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}

	public String getDownloadStreamType() {
		return downloadStreamType;
	}

	public void setDownloadStreamType(String downloadStreamType) {
		this.downloadStreamType = downloadStreamType;
	}

	public ProjectResultFilter getProjectResultFilter() {
		return projectResultFilter;
	}

	public void setProjectResultFilter(ProjectResultFilter projectResultFilter) {
		this.projectResultFilter = projectResultFilter;
	}


}
