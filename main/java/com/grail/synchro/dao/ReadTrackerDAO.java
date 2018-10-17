package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.beans.ReadTrackerObject;


/**
 * @author Kanwar Grewal
 * @version 1.0, Date 5/30/13
 */

public interface ReadTrackerDAO {
	
	public void saveUserReadInfo(final ReadTrackerObject readTrackerObject);
	 
	 public List<ReadTrackerObject> getUserReadInfo(final Long userID);
	 
	 public List<ReadTrackerObject> getUserReadInfo(final Long userID, final ProjectResultFilter projectResultFilter);
}
