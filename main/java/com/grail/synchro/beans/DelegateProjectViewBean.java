package com.grail.synchro.beans;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class DelegateProjectViewBean extends BeanObject {
    private Long projectID;
    private String projectName;
    private String owner;
    private String status;
    private Long projectOwner;
    private String url;
    private String spiContactName;
    private Boolean multimarket = false;
    private static UserManager userManager;
    private static ProjectManager synchroProjectManager;
    private static ProjectSpecsManager projectSpecsManager;
    private Boolean confidential = false;
    private Long spiContact;
    private  List<Long> endMarketIDs;
    private String otherSPIContacts;
    private String otherLegalContacts;
    private String otherProductContacts;
    
    private String otherAgencyContacts;
    
    private static PIBManager pibManager;
    private  List<String> endMarketName;
    private  List<String> endMarketProjectOwner;
    private  List<String> endMarketSPIContact;
    
    private List<String> endMarketOtherSPIContacts;
    private List<String> endMarketOtherLegalContacts;
    private List<String> endMarketOtherProductContacts;
    
    private Long agencyContact;
    private String agencyContactName;
    
    public DelegateProjectViewBean() {
    }

    public DelegateProjectViewBean(Long projectID, String projectName, String owner,
                                    String status, Long projectOwner,
                                    boolean multiMarket, String url) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.owner = owner;
        this.status = status;
        this.projectOwner = projectOwner;
        this.multimarket = multiMarket;
        this.url = url;
    }

    public static DelegateProjectViewBean toDelegateProjectViewBean(final Project project){

    	DelegateProjectViewBean bean = new DelegateProjectViewBean();
        bean.setProjectID(project.getProjectID());
        bean.setProjectName(project.getName());
        bean.setMultimarket(project.getMultiMarket());
        bean.setProjectOwner(project.getProjectOwner());
        bean.setConfidential(project.getConfidential());

        try {
            bean.setOwner(getUserManager().getUser(project.getProjectOwner()).getName());
        } catch (UserNotFoundException e) {
            bean.setOwner("");
        }


        if(project.getSpiContact() != null && project.getSpiContact().size() > 0) {
            try {
                bean.setSpiContact(project.getSpiContact().get(0));
            	bean.setSpiContactName(getUserManager().getUser(project.getSpiContact().get(0)).getName());
            } catch (UserNotFoundException e) {
                bean.setSpiContactName("");
            }
        } else {
            bean.setSpiContactName("");
        }

      
       
        bean.setStatus(SynchroGlobal.Status.getName(project.getStatus().intValue()).toString());
       
        bean.setUrl(ProjectStage.generateURL(project, ProjectStage.getCurrentStageNumber(project)));
        List<Long> endMarketIDs = getSynchroProjectManager().getEndMarketIDs(project.getProjectID());
        bean.setEndMarketIDs(endMarketIDs);
        
        if(project.getMultiMarket())
        {
        	PIBStakeholderList pibStakeholders = getPIBManager().getPIBStakeholderList(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        	if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null)
        	{
	        	try {
	                bean.setAgencyContact(pibStakeholders.getAgencyContact1());
	            	bean.setAgencyContactName(getUserManager().getUser(pibStakeholders.getAgencyContact1()).getName());
	            } catch (UserNotFoundException e) {
	                bean.setAgencyContactName("");
	            }
        	}
        }
        else
        {
        	PIBStakeholderList pibStakeholders = getPIBManager().getPIBStakeholderList(project.getProjectID(), endMarketIDs.get(0));
        	if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null)
        	{
	        	try {
	                bean.setAgencyContact(pibStakeholders.getAgencyContact1());
	            	bean.setAgencyContactName(getUserManager().getUser(pibStakeholders.getAgencyContact1()).getName());
	            } catch (UserNotFoundException e) {
	                bean.setAgencyContactName("");
	            }
        	}
        }
        
        List<String> endMarketName = new ArrayList<String>();
        
        
        List<String> endMarketProjectOwnerName = new ArrayList<String>();
        List<String> endMarketSPIContactName = new ArrayList<String>();
        
        List<String> otherEMSPI = new ArrayList<String>();
        List<String> otherEMLegal = new ArrayList<String>();
        List<String> otherEMProduct = new ArrayList<String>();
        
        for(Long emId: endMarketIDs)
        {
        	endMarketName.add(SynchroGlobal.getEndMarkets().get(emId.intValue()));
        	List<FundingInvestment> fundingInvestments = getSynchroProjectManager().getProjectInvestments(project.getProjectID(),emId);
        	if(fundingInvestments!=null && fundingInvestments.size()>0)
        	{
        		FundingInvestment fi = fundingInvestments.get(0);
        		if(fi.getFieldworkMarketID().intValue()==emId.intValue())
				{
					try
                	{
						endMarketProjectOwnerName.add(getUserManager().getUser(fi.getProjectContact()).getName());
                	}
                	catch (UserNotFoundException e) {
                		endMarketProjectOwnerName.add("");
                    }
					try
                	{
						endMarketSPIContactName.add(getUserManager().getUser(fi.getSpiContact()).getName());
                	}
					catch (UserNotFoundException e) {
						endMarketSPIContactName.add("");
                    }
				
				}
		  	}
        	else
        	{
        		endMarketProjectOwnerName.add("");
        		endMarketSPIContactName.add("");
        	}
        	
        	// Adding the other SPI Contacts for End Markets 
        	String otherEMSPIContacts=getPIBManager().getOtherSPIContact(project.getProjectID(),emId);
            
            if(otherEMSPIContacts!=null && !otherEMSPIContacts.equals(""))
            {
            	if(otherEMSPIContacts.contains(","))
            	{
            		List<String> otherSPIConList = new ArrayList<String>();
            		String[] splitCont = otherEMSPIContacts.split(",");
                    for(int i = 0; i < splitCont.length; i++) {
                    	try
                    	{
                    		otherSPIConList.add(getUserManager().getUser(new Long(splitCont[i])).getName());
                    	}
                    	catch (UserNotFoundException e) {
                    		otherSPIConList.add("");
                        }
                    }
                    otherEMSPI.add(Joiner.on("~").join(otherSPIConList));
            	}
            	else
            	{
            		try
            		{
            			otherEMSPI.add(getUserManager().getUser(new Long(otherEMSPIContacts)).getName());
            		}
            		catch (UserNotFoundException e) {
            			otherEMSPI.add("");
                    }
            	}
            }
            else
            {
            	otherEMSPI.add("");
            }
            
         // Adding the other Legal Contacts for End Markets 
        	String otherEMLegalContacts=getPIBManager().getOtherLegalContact(project.getProjectID(),emId);
            
            if(otherEMLegalContacts!=null && !otherEMLegalContacts.equals(""))
            {
            	if(otherEMLegalContacts.contains(","))
            	{
            		List<String> otherLegalConList = new ArrayList<String>();
            		String[] splitCont = otherEMLegalContacts.split(",");
                    for(int i = 0; i < splitCont.length; i++) {
                    	try
                    	{
                    		otherLegalConList.add(getUserManager().getUser(new Long(splitCont[i])).getName());
                    	}
                    	catch (UserNotFoundException e) {
                    		otherLegalConList.add("");
                        }
                    }
                    otherEMLegal.add(Joiner.on("~").join(otherLegalConList));
            	}
            	else
            	{
            		try
            		{
            			otherEMLegal.add(getUserManager().getUser(new Long(otherEMLegalContacts)).getName());
            		}
            		catch (UserNotFoundException e) {
            			otherEMLegal.add("");
                    }
            	}
            }
            else
            {
            	otherEMLegal.add("");
            }
            
         // Adding the other Product Contacts for End Markets 
        	String otherEMProductContacts=getPIBManager().getOtherProductContact(project.getProjectID(),emId);
            
            if(otherEMProductContacts!=null && !otherEMProductContacts.equals(""))
            {
            	if(otherEMProductContacts.contains(","))
            	{
            		List<String> otherProductConList = new ArrayList<String>();
            		String[] splitCont = otherEMProductContacts.split(",");
                    for(int i = 0; i < splitCont.length; i++) {
                    	try
                    	{
                    		otherProductConList.add(getUserManager().getUser(new Long(splitCont[i])).getName());
                    	}
                    	catch (UserNotFoundException e) {
                    		otherProductConList.add("");
                        }
                    }
                    otherEMProduct.add(Joiner.on("~").join(otherProductConList));
            	}
            	else
            	{
            		try
            		{
            			otherEMProduct.add(getUserManager().getUser(new Long(otherEMProductContacts)).getName());
            		}
            		catch (UserNotFoundException e) {
            			otherEMProduct.add("");
                    }
            	}
            }
            else
            {
            	otherEMProduct.add("");
            }
        }
        
        bean.setEndMarketOtherSPIContacts(otherEMSPI);
        bean.setEndMarketOtherLegalContacts(otherEMLegal);
        bean.setEndMarketOtherProductContacts(otherEMProduct);
        
        bean.setEndMarketProjectOwner(endMarketProjectOwnerName);
        bean.setEndMarketSPIContact(endMarketSPIContactName);
        bean.setEndMarketName(endMarketName);
        String otherSPIContacts="";
        if(project.getMultiMarket())
        {
        	otherSPIContacts = getPIBManager().getOtherSPIContact(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        else
        {
            otherSPIContacts = getPIBManager().getOtherSPIContact(project.getProjectID(), bean.getEndMarketIDs().get(0));
        }
        if(otherSPIContacts!=null && !otherSPIContacts.equals(""))
        {
        	if(otherSPIContacts.contains(","))
        	{
        		List<String> otherSPIConList = new ArrayList<String>();
        		String[] splitCont = otherSPIContacts.split(",");
                for(int i = 0; i < splitCont.length; i++) {
                	try
                	{
                		otherSPIConList.add(getUserManager().getUser(new Long(splitCont[i])).getName());
                	}
                	catch (UserNotFoundException e) {
                		otherSPIConList.add("");
                    }
                }
                bean.setOtherSPIContacts(Joiner.on(",").join(otherSPIConList));
        	}
        	else
        	{
        		try
        		{
        			bean.setOtherSPIContacts(getUserManager().getUser(new Long(otherSPIContacts)).getName());
        		}
        		catch (UserNotFoundException e) {
                    bean.setOtherSPIContacts("");
                }
        	}
        }
        else
        {
        	bean.setOtherSPIContacts("");
        }
       // bean.setOtherSPIContacts(getPIBManager().getOtherSPIContact(project.getProjectID(), bean.getEndMarketIDs().get(0)));
        String otherLegalContacts = "";
        if(project.getMultiMarket())
        {
        	otherLegalContacts = getPIBManager().getOtherLegalContact(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        else
        {
        	otherLegalContacts = getPIBManager().getOtherLegalContact(project.getProjectID(), bean.getEndMarketIDs().get(0));
        }
        
        if(otherLegalContacts!=null && !otherLegalContacts.equals(""))
        {
        	if(otherLegalContacts.contains(","))
        	{
        		List<String> otherLegalConList = new ArrayList<String>();
        		String[] splitCont = otherLegalContacts.split(",");
                for(int i = 0; i < splitCont.length; i++) {
                	try
                	{
                		otherLegalConList.add(getUserManager().getUser(new Long(splitCont[i])).getName());
                	}
                	catch (UserNotFoundException e) {
                		otherLegalConList.add("");
                    }
                }
                bean.setOtherLegalContacts(Joiner.on(",").join(otherLegalConList));
        	}
        	else
        	{
        		try
        		{
        			bean.setOtherLegalContacts(getUserManager().getUser(new Long(otherLegalContacts)).getName());
        		}
        		catch (UserNotFoundException e) {
                    bean.setOtherLegalContacts("");
                }
        	}
        }
        else
        {
        	bean.setOtherLegalContacts("");
        }
        
        String otherProductContacts = "";
        
        if(project.getMultiMarket())
        {
        	otherProductContacts = getPIBManager().getOtherProductContact(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        else
        {
        	otherProductContacts = getPIBManager().getOtherProductContact(project.getProjectID(), bean.getEndMarketIDs().get(0));
        }
        		
        
        if(otherProductContacts!=null && !otherProductContacts.equals(""))
        {
        	if(otherProductContacts.contains(","))
        	{
        		List<String> otherProductConList = new ArrayList<String>();
        		String[] splitCont = otherProductContacts.split(",");
                for(int i = 0; i < splitCont.length; i++) {
                	try
                	{
                		otherProductConList.add(getUserManager().getUser(new Long(splitCont[i])).getName());
                	}
                	catch (UserNotFoundException e) {
                		otherProductConList.add("");
                    }
                }
                bean.setOtherProductContacts(Joiner.on(",").join(otherProductConList));
        	}
        	else
        	{
        		try
        		{
        			bean.setOtherProductContacts(getUserManager().getUser(new Long(otherProductContacts)).getName());
        		}
        		catch (UserNotFoundException e) {
                    bean.setOtherProductContacts("");
                }
        	}
        }
        else
        {
        	bean.setOtherProductContacts("");
        }
       // Setting the other Agency Contacts
        String otherAgencyContacts = "";
        if(project.getMultiMarket())
        {
        	otherAgencyContacts = getPIBManager().getOtherAgencyContact(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        else
        {
        	otherAgencyContacts = getPIBManager().getOtherAgencyContact(project.getProjectID(), bean.getEndMarketIDs().get(0));
        }
        
        if(otherAgencyContacts!=null && !otherAgencyContacts.equals(""))
        {
        	if(otherAgencyContacts.contains(","))
        	{
        		List<String> otherAgencyConList = new ArrayList<String>();
        		String[] splitCont = otherAgencyContacts.split(",");
                for(int i = 0; i < splitCont.length; i++) {
                	try
                	{
                		otherAgencyConList.add(getUserManager().getUser(new Long(splitCont[i])).getName());
                	}
                	catch (UserNotFoundException e) {
                		otherAgencyConList.add("");
                    }
                }
                bean.setOtherAgencyContacts(Joiner.on(",").join(otherAgencyConList));
        	}
        	else
        	{
        		try
        		{
        			bean.setOtherAgencyContacts(getUserManager().getUser(new Long(otherAgencyContacts)).getName());
        		}
        		catch (UserNotFoundException e) {
                    bean.setOtherAgencyContacts("");
                }
        	}
        }
        else
        {
        	bean.setOtherAgencyContacts("");
        }
        
       //bean.setOtherProductContacts("Vikram, Mayank");
        return bean;
    }

    public static String generateURL(final Project project) {
        StringBuilder urlBuilder = new StringBuilder();
        return urlBuilder.toString();
    }

    public static UserManager getUserManager() {
        if(userManager == null){
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }

    public static ProjectSpecsManager getProjectSpecsManager() {
        if(projectSpecsManager == null){
            projectSpecsManager = JiveApplication.getContext().getSpringBean("projectSpecsManager");
        }
        return projectSpecsManager;
    }


    public static ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager == null){
            synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;
    }
    public static PIBManager getPIBManager() {
        if(pibManager == null){
        	pibManager = JiveApplication.getContext().getSpringBean("pibManager");
        }
        return pibManager;
    }

    public Long getProjectID() {
        return projectID;
    }
    public void setProjectID(Long projectID) {
        this.projectID = projectID;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

  

    public Boolean getMultimarket() {
        return multimarket;
    }

    public void setMultimarket(Boolean multimarket) {
        this.multimarket = multimarket;
    }

    public Long getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(Long projectOwner) {
        this.projectOwner = projectOwner;
    }

    

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getConfidential() {
        return confidential;
    }

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
    }

	public String getSpiContactName() {
		return spiContactName;
	}

	public void setSpiContactName(String spiContactName) {
		this.spiContactName = spiContactName;
	}

	public Long getSpiContact() {
		return spiContact;
	}

	public void setSpiContact(Long spiContact) {
		this.spiContact = spiContact;
	}

	public List<Long> getEndMarketIDs() {
		return endMarketIDs;
	}

	public void setEndMarketIDs(List<Long> endMarketIDs) {
		this.endMarketIDs = endMarketIDs;
	}

	public String getOtherSPIContacts() {
		return otherSPIContacts;
	}

	public void setOtherSPIContacts(String otherSPIContacts) {
		this.otherSPIContacts = otherSPIContacts;
	}

	public String getOtherLegalContacts() {
		return otherLegalContacts;
	}

	public void setOtherLegalContacts(String otherLegalContacts) {
		this.otherLegalContacts = otherLegalContacts;
	}

	public String getOtherProductContacts() {
		return otherProductContacts;
	}

	public void setOtherProductContacts(String otherProductContacts) {
		this.otherProductContacts = otherProductContacts;
	}

	public List<String> getEndMarketName() {
		return endMarketName;
	}

	public void setEndMarketName(List<String> endMarketName) {
		this.endMarketName = endMarketName;
	}

	public List<String> getEndMarketProjectOwner() {
		return endMarketProjectOwner;
	}

	public void setEndMarketProjectOwner(List<String> endMarketProjectOwner) {
		this.endMarketProjectOwner = endMarketProjectOwner;
	}

	public List<String> getEndMarketSPIContact() {
		return endMarketSPIContact;
	}

	public void setEndMarketSPIContact(List<String> endMarketSPIContact) {
		this.endMarketSPIContact = endMarketSPIContact;
	}

	public List<String> getEndMarketOtherSPIContacts() {
		return endMarketOtherSPIContacts;
	}

	public void setEndMarketOtherSPIContacts(List<String> endMarketOtherSPIContacts) {
		this.endMarketOtherSPIContacts = endMarketOtherSPIContacts;
	}

	public List<String> getEndMarketOtherLegalContacts() {
		return endMarketOtherLegalContacts;
	}

	public void setEndMarketOtherLegalContacts(
			List<String> endMarketOtherLegalContacts) {
		this.endMarketOtherLegalContacts = endMarketOtherLegalContacts;
	}

	public List<String> getEndMarketOtherProductContacts() {
		return endMarketOtherProductContacts;
	}

	public void setEndMarketOtherProductContacts(
			List<String> endMarketOtherProductContacts) {
		this.endMarketOtherProductContacts = endMarketOtherProductContacts;
	}

	public Long getAgencyContact() {
		return agencyContact;
	}

	public void setAgencyContact(Long agencyContact) {
		this.agencyContact = agencyContact;
	}

	public String getAgencyContactName() {
		return agencyContactName;
	}

	public void setAgencyContactName(String agencyContactName) {
		this.agencyContactName = agencyContactName;
	}

	public String getOtherAgencyContacts() {
		return otherAgencyContacts;
	}

	public void setOtherAgencyContacts(String otherAgencyContacts) {
		this.otherAgencyContacts = otherAgencyContacts;
	}

	

  
}
