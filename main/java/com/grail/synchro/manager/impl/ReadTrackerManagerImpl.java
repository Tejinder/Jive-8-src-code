package com.grail.synchro.manager.impl;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.beans.ReadTrackerObject;
import com.grail.synchro.dao.ReadTrackerDAO;
import com.grail.synchro.manager.ReadTrackerManager;

/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class ReadTrackerManagerImpl implements ReadTrackerManager {

	private ReadTrackerDAO synchroReadTrackerDAO;
	

	public void setSynchroReadTrackerDAO(ReadTrackerDAO synchroReadTrackerDAO) {
		this.synchroReadTrackerDAO = synchroReadTrackerDAO;
	}

	@Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void saveUserReadInfo(final ReadTrackerObject readTrackerObject) {
		synchroReadTrackerDAO.saveUserReadInfo(readTrackerObject);
    }
	
	@Override
	public List<ReadTrackerObject> getUserReadInfo(final Long userID)
	{
		return synchroReadTrackerDAO.getUserReadInfo(userID);
		
	}
	
	@Override
	public List<ReadTrackerObject> getUserReadInfo(final Long userID, final ProjectResultFilter projectResultFilter)
	{
		return synchroReadTrackerDAO.getUserReadInfo(userID, projectResultFilter);
		
	}
}
