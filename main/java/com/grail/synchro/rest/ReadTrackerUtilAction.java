package com.grail.synchro.rest;

import org.apache.log4j.Logger;

import com.grail.synchro.beans.ReadTrackerObject;
import com.grail.synchro.manager.ReadTrackerManager;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * RESTful Action related to project Read tracker
 *
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class ReadTrackerUtilAction extends RemoteSupport  {

    private static final Logger LOG = Logger.getLogger(ReadTrackerUtilAction.class);

    private ReadTrackerManager synchroReadTrackerManager;

	public ReadTrackerManager getSynchroReadTrackerManager() {
		 if (synchroReadTrackerManager == null) 
		 {
			 synchroReadTrackerManager = JiveApplication.getContext().getSpringBean("synchroReadTrackerManager");
	     }
		return synchroReadTrackerManager;
	}

	public void setSynchroReadTrackerManager(
			ReadTrackerManager synchroReadTrackerManager) {
		this.synchroReadTrackerManager = synchroReadTrackerManager;
	}


	public void setReadTrackInfo(final Long projectID, final Long userID, final Long StageID )
	{
		ReadTrackerObject readTrackerObject = new ReadTrackerObject();
		try{
		readTrackerObject.setProjectID(projectID);
		readTrackerObject.setUserID(userID);
		readTrackerObject.setStageID(StageID);
		final Long currentTime = System.currentTimeMillis();
		readTrackerObject.setReadDate(currentTime);
		getSynchroReadTrackerManager().saveUserReadInfo(readTrackerObject);
		}catch(Exception e){LOG.error("Error while updating project read tracker info ..."+e.getMessage());}
	}
}
