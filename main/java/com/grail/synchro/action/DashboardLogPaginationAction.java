package com.grail.synchro.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.ActivityLog;
import com.grail.synchro.beans.LogDashboardViewBean;
import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.ActivityLogManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.search.filter.LogResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.util.StringUtils;

@Decorate(false)
public class DashboardLogPaginationAction extends JiveActionSupport{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DashboardLogPaginationAction.class);
    private List<LogDashboardViewBean> logs = null;
    private Integer page = 1;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.LOG_DASHBOARD_PAGE_LIMIT, 10);
    private Integer results;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;
    private ActivityLogManager activityLogManager;
    private LogResultFilter logResultFilter;
    private InputStream downloadStream;
    private final String DOWNLOAD_LOG = "downloadLog";
    private String downloadFilename;
    private long userID;
    private Long projectID;
    private ProjectManager synchroProjectManager;

    public void setActivityLogManager(ActivityLogManager activityLogManager) {
		this.activityLogManager = activityLogManager;
	}


	public String execute()
    {
		User user = SynchroPermHelper.getEffectiveUser();
		Long userID = -1L;
		
		if(user!=null && user.getID()>0 && !SynchroPermHelper.isSynchroAdmin(user))
		{
			userID = new Long(user.getID());
		}
		
		setPagination(activityLogManager.getTotalCount(getSearchFilter(), userID).intValue());
        updatePage(userID);
        return SUCCESS;
    }

    public void setPagination(final Integer count) {
        if(count > limit) {
            double temp = count / (limit * 1.0);
            if(count%limit == 0) {
                pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }
    }

    public void updatePage(final Long userID) {
        start = (page-1) * limit;
        end = start + limit;
        logs = this.toLogPaginationBeans(activityLogManager.getActivityLogs(getSearchFilter(), userID));
    }

    private LogResultFilter getSearchFilter() {
        logResultFilter = new LogResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(logResultFilter);
        binder.bind(getRequest());
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateLog"))) {
        	logResultFilter.setStartDateLog(DateUtils.parse(getRequest().getParameter("startDateLog")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateLog"))) {
        	logResultFilter.setEndDateLog(DateUtils.parse(getRequest().getParameter("endDateLog")));
        }
        
        if(logResultFilter.getKeyword() == null || logResultFilter.getKeyword().equals("")) {
        	logResultFilter.setKeyword(keyword);
        }
        if(logResultFilter.getProjectID() == null) {
        	logResultFilter.setProjectID(projectID);
        }
        logResultFilter.setStart(start);
        logResultFilter.setLimit(limit);       

        return logResultFilter;
    }

    private void setPaginationFilter(int page,int results) {
        start = (this.page-1)*limit;
        end = start + limit;
    }


    public List<LogDashboardViewBean> toLogPaginationBeans(final List<ActivityLog> logs) {
        List<LogDashboardViewBean> beans = new ArrayList<LogDashboardViewBean>();
        for(ActivityLog log: logs) {
        
        		beans.add(LogDashboardViewBean.toLogDashboardViewBean(log));
        }
        return beans;
    }


    public String downloadLogFile() throws IOException {
        if(getUser() == null) 
        {
        	return UNAUTHORIZED;        
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        LOG.info("Downloading Log extract by " +getUser().getFirstName() + " " + getUser().getLastName() + " on " + new Date());
        Calendar calendar = Calendar.getInstance();
        downloadFilename = "Log Extract_" +
                calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) +
                ".csv";

        StringBuilder log = new StringBuilder();

        StringBuilder header = new StringBuilder();
        header.append("Timestamp").append(",");
        header.append("Username").append(",");
        header.append("Portal Name").append(",");
        header.append("Page Access").append(",");
        header.append("Activity Type").append(",");
        header.append("Activity Description").append(",");
        header.append("Project Code").append(",");
        header.append("Project Name").append(",");
        log.append(header.toString()).append("\n");

        User user = SynchroPermHelper.getEffectiveUser();
		Long userID = -1L;
		
		if(user!=null && user.getID()>0 && !SynchroPermHelper.isSynchroAdmin(user))
		{
			userID = user.getID();
		}
        
		start = 0;
		limit = -1;
		List<LogDashboardViewBean> logExtract = this.toLogPaginationBeans(activityLogManager.getActivityLogs(getSearchFilter(), userID));
        
        if(logExtract != null && logExtract.size() > 0) {
            for(LogDashboardViewBean bean:logExtract) {
                if(bean.getDescriptions()!=null && bean.getDescriptions().size() > 0)
                {
                	for(String description : bean.getDescriptions())
                	{
                		StringBuilder data = new StringBuilder();
                    	if(bean.getTimestamp() != null) {
                            data.append(bean.getTimestamp()).append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        if(bean.getUsername() != null) {
                            data.append("\"").append(bean.getUsername()).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        if(bean.getPortalname() != null) {
                        	data.append("\"").append(bean.getPortalname()).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        if(bean.getPage() != null) {
                        	data.append("\"").append(bean.getPage()).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        if(bean.getActivity() != null) {
                        	data.append("\"").append(bean.getActivity()).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        if(bean.getDescriptions() != null) {
                        	data.append("\"").append(description).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }                       
                        if(bean.getProjectID()!= null && bean.getProjectID()>0) {
                        	data.append("\"").append(bean.getProjectID()).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        if(bean.getProjectName() != null) {
                        	data.append("\"").append(bean.getProjectName()).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        log.append(data.toString()).append("\n");
                	}
                }
             }
        }

        downloadStream = new ByteArrayInputStream(log.toString().getBytes("utf-8"));
        
        return DOWNLOAD_LOG;
    }
    
    public String downloadProjectLogFile() throws IOException {
        if(getUser() == null) 
        {
        	return UNAUTHORIZED;        
        }

        String projectName = StringUtils.EMPTY;
        Long projectCode = getSearchFilter().getProjectID();
        if(projectCode!=null &&  projectCode> 0)
        {
        	Project project = synchroProjectManager.get(projectCode);
        	if(project!=null)
        		projectName = project.getName();
        }
        else
        {
        	return UNAUTHORIZED;
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        LOG.info("Downloading Project Log extract by " +getUser().getFirstName() + " " + getUser().getLastName() + " on " + new Date());
        Calendar calendar = Calendar.getInstance();
        downloadFilename = "Log Extract_" +
                calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) +
                ".csv";

        StringBuilder log = new StringBuilder();
        log.append(projectName + " (" + SynchroUtils.generateProjectCode(projectID)+")").append("\n");
        
        StringBuilder header = new StringBuilder();  
        header.append("Activity Type").append(",");
        header.append("Activity Description").append(",");
        header.append("Username").append(",");
        header.append("Timestamp").append(",");        
        log.append(header.toString()).append("\n");

        User user = SynchroPermHelper.getEffectiveUser();
		Long userID = -1L;
		
		if(user!=null && user.getID()>0 && !SynchroPermHelper.isSynchroAdmin(user))
		{
			userID = user.getID();
		}
        
		start = 0;
		limit = -1;
		List<LogDashboardViewBean> logExtract = this.toLogPaginationBeans(activityLogManager.getActivityLogs(getSearchFilter(), userID));
        
        if(logExtract != null && logExtract.size() > 0) {
            for(LogDashboardViewBean bean:logExtract) {
                if(bean.getDescriptions()!=null && bean.getDescriptions().size() > 0)
                {
                	for(String description : bean.getDescriptions())
                	{
                		StringBuilder data = new StringBuilder();
                		if(bean.getActivity() != null) {
                        	data.append("\"").append(bean.getActivity()).append("\"").append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                		 if(bean.getDescriptions() != null) {
                         	data.append("\"").append(description).append("\"").append(",");
                         } else {
                             data.append(" ").append(",");
                         }
                		 if(bean.getUsername() != null) {
                             data.append("\"").append(bean.getUsername()).append("\"").append(",");
                         } else {
                             data.append(" ").append(",");
                         }
                    	if(bean.getTimestamp() != null) {
                            data.append(bean.getTimestamp()).append(",");
                        } else {
                            data.append(" ").append(",");
                        }
                        log.append(data.toString()).append("\n");
                	}
                }
             }
        }

        downloadStream = new ByteArrayInputStream(log.toString().getBytes("utf-8"));
        
        return DOWNLOAD_LOG;
    }
    
    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }
    
    public Integer getPages() {
        return pages;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }


    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public void setResults(Integer results) {
        this.results = results;
    }

    public Integer getResults() {
        return results;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }


    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }


	public List<LogDashboardViewBean> getLogs() {
		return logs;
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


	public long getUserID() {
		return userID;
	}


	public void setUserID(Long userID) {
		this.userID = userID;
	}


	public Long getProjectID() {
		return projectID;
	}


	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	
	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }    
    
}
