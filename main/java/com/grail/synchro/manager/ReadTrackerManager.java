package com.grail.synchro.manager;

import java.util.List;

import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.beans.ReadTrackerObject;


/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */

public interface ReadTrackerManager {

	public void saveUserReadInfo(final ReadTrackerObject readTrackerObject);
	
	public List<ReadTrackerObject> getUserReadInfo(final Long userID);
	
	public List<ReadTrackerObject> getUserReadInfo(final Long userID, final ProjectResultFilter projectResultFilter);
}
