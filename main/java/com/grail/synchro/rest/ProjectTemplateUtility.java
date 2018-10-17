package com.grail.synchro.rest;

import org.apache.log4j.Logger;

import com.grail.synchro.manager.ProjectManager;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * RESTful Action related to Loading and Creating Synchro Project Templates
 *
 * @author: Kanwar Grewal
 * @version: 4.0
 */
public class ProjectTemplateUtility extends RemoteSupport  {

    private static final Logger LOGGER = Logger.getLogger(ProjectTemplateUtility.class);

    private ProjectManager synchroProjectManager;
    

	public ProjectManager getSynchroProjectManager() {
		 if (synchroProjectManager == null) 
		 {
			 synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
	     }
		return synchroProjectManager;
	}


	public void create()
	{
		//TODO
	}
	
	public void update()
	{
		//TODO
	}
	public void get()
	{
		//TODO
	}
	public void getAll()
	{
		//TODO
	}
	public void delete()
	{
		//TODO
	}
	
}
