package com.grail.synchro.manager;

import java.util.List;

import com.grail.synchro.beans.ActivityLog;
import com.grail.synchro.search.filter.LogResultFilter;

/**
 * @author Kanwar Grewal
 * @version 1.0
 */

public interface ActivityLogManager {
	List<ActivityLog> getActivityLogs();
	List<ActivityLog> getActivityLogs(final LogResultFilter filter, final Long userID);
    List<ActivityLog> getActivityLogs(final Long userID);

    void saveActivityLog(ActivityLog activityLog);
    
    Long getTotalCount(final LogResultFilter filter, final Long userID);
}
