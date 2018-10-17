package com.grail.synchro.manager.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.ActivityLog;
import com.grail.synchro.dao.ActivityLogDAO;
import com.grail.synchro.manager.ActivityLogManager;
import com.grail.synchro.search.filter.LogResultFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;


/**
 * @author Kanwar Grewal
 * @version 1.0
 */
public class ActivityLogManagerImpl implements ActivityLogManager {
    private static final Logger LOG = Logger.getLogger(ActivityLogManagerImpl.class);

    private ActivityLogDAO activityLogDAO;
    private UserManager userManager;
    
   
	public UserManager getUserManager() {

		 if (userManager == null) 
		 {
			 userManager = JiveApplication.getContext().getSpringBean("userManager");
	     }
		return userManager;
	
	}

	public ActivityLogDAO getActivityLogDAO() {
		return activityLogDAO;
	}

	public void setActivityLogDAO(ActivityLogDAO activityLogDAO) {
		this.activityLogDAO = activityLogDAO;
	}

	@Override
    public List<ActivityLog> getActivityLogs() {
        return activityLogDAO.getActivityLogs();
    }
	
	@Override
    public List<ActivityLog> getActivityLogs(final LogResultFilter filter, final Long userID) {
        return activityLogDAO.getActivityLogs(filter, userID);
    }
	
    @Override
    public List<ActivityLog> getActivityLogs(final Long userID) {
        return activityLogDAO.getActivityLogs(userID);
    }
    
    
    
    @Override
    public void saveActivityLog(ActivityLog activityLog) {
    	activityLog = updateAuditFields(activityLog);
        //createCurrentTableIfNotExists();
        this.activityLogDAO.saveActivityLog(activityLog);
    }

    public void createCurrentTableIfNotExists() {
        activityLogDAO.createTableIfNotExists();
    }



    private ActivityLog updateAuditFields(final ActivityLog activityLog){
    	Long userID = null;
    	User user = null;
        final Long currentTime = System.currentTimeMillis();    
        
        if(activityLog.getUserID()==null)
        {
        	user = JiveApplication.getContext().getAuthenticationProvider().getJiveUser();
            userID = user.getID();
            activityLog.setUserID(userID);	
        }
        else
        {
        	userID = activityLog.getUserID();
        	try{
        	user = getUserManager().getUser(userID);
        	}catch(UserNotFoundException uex)
        	{
        		LOG.error("User Not found" + userID);
        		uex.printStackTrace();
        	}
        }
        
        if(userID>0 && user!=null)
        {
        	activityLog.setUserName(user.getUsername());	
        }
        
        activityLog.setTimestamp(currentTime);
        return activityLog;
    }
    
    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getTotalCount(final LogResultFilter filter, final Long userID) {
        return activityLogDAO.getTotalCount(filter, userID);
    }
}
