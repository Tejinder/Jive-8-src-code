package com.grail.synchro.rest;

import org.apache.log4j.Logger;

import com.grail.synchro.manager.ProjectManager;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * RESTful Action related to track exchange rate related data
 *
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class ExchangeRateUtilAction extends JiveActionSupport {

    private static final Logger LOG = Logger.getLogger(ExchangeRateUtilAction.class);

    private ProjectManager synchroProjectManager;


	public ProjectManager getSynchroProjectManager() {
		 if (synchroProjectManager == null) 
		 {
			 synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
	     }
		return synchroProjectManager;
	}

	public void setExchangeRate(Integer currencyID)
	{
		//TODO Permissions only for Jive/Synchro admin
		try{
			synchroProjectManager.saveExchangeRate(currencyID);
		}catch(Exception e){log.error("Error saving the exchange rate information for currency ID "+currencyID);}
		
	}
}
