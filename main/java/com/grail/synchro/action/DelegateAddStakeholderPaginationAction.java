package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.DelegateProjectViewBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;

@Decorate(false)
public class DelegateAddStakeholderPaginationAction extends JiveActionSupport{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DelegateAddStakeholderPaginationAction.class);
    private List<DelegateProjectViewBean> projects = null;
    private Integer page = 1;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, 10);
    private Integer results;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private String keyword;
    private String sortField;
    private Integer ascendingOrder;
    private ProjectManager synchroProjectManager;
    private ProjectResultFilter projectResultFilter;
    private SynchroUtils synchroUtils;
    private PIBManager pibManager;
    private ProposalManager proposalManager;
    
    public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }


    public String execute()
    {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }

        setPagination(synchroProjectManager.getTotalCount(getSearchFilter()).intValue());
        updatePage();
        return SUCCESS;

    }
    
    /*
     * This method will add the delegated users (Project Owner and SPI Contact)
     */
    public String delegateProject()
    {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        LOGGER.info("Checking Project ID --->"+ getRequest().getParameter("projectID"));
        LOGGER.info("Checking Project Owner --->"+ getRequest().getParameter("projectOwner"));
             
        LOGGER.info("Checking SPI Contact --->"+ getRequest().getParameter("spiContact"));
     
        
        LOGGER.info("Checking End Market ID --->"+ getRequest().getParameter("endMarketID"));
        
        
        Long spi_DB = null;
        Long agency_DB = null;     
        Long owner_DB = null;
        
        if(getRequest().getParameter("projectID")!=null && !getRequest().getParameter("projectID").equals(""))
        {
        	Project project_DB = synchroProjectManager.get(new Long(getRequest().getParameter("projectID")));
        	owner_DB  = project_DB.getProjectOwner();
        	
        	spi_DB = project_DB.getSpiContact().get(0);
        	Long endMarketId_DB = new Long(getRequest().getParameter("endMarketID"));
        	if(endMarketId_DB!=null && endMarketId_DB>0)
        	{
        		PIBStakeholderList pibStakeholders_DB = pibManager.getPIBStakeholderList(new Long(getRequest().getParameter("projectID")), endMarketId_DB);
            	if(pibStakeholders_DB!=null && pibStakeholders_DB.getAgencyContact1()!=null)
            	{
            		agency_DB = pibStakeholders_DB.getAgencyContact1();
            	}	
        	}
        	
        	
        if(getRequest().getParameter("projectOwner")!=null && !getRequest().getParameter("projectOwner").equals(""))
        {
	        Long projectOwner = new Long(getRequest().getParameter("projectOwner"));
	        
	        synchroProjectManager.updateOwner(new Long(getRequest().getParameter("projectID")),projectOwner);
        }
        if(getRequest().getParameter("spiContact")!=null && !getRequest().getParameter("spiContact").equals(""))
        {
	        Long spiContact = new Long(getRequest().getParameter("spiContact"));
	        Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
	        
	        synchroProjectManager.updateSPIContact(new Long(getRequest().getParameter("projectID")),endMarketId,spiContact);
        }
        
        LOGGER.info("Checking Agency Contact Owner --->"+ getRequest().getParameter("agencyContact"));
               
        
        LOGGER.info("Checking End Market ID --->"+ getRequest().getParameter("endMarketID"));
        
        
        if(getRequest().getParameter("agencyContact")!=null && !getRequest().getParameter("agencyContact").equals(""))
        {
	        Long agencyContact = new Long(getRequest().getParameter("agencyContact"));
	        Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
	        PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(new Long(getRequest().getParameter("projectID")), endMarketId);
	        pibManager.updateAgencyContact(new Long(getRequest().getParameter("projectID")),endMarketId, agencyContact);
	        
	        
	       
	        if(pibStakeholders!=null)
	        {
	        	 LOGGER.info("pibStakeholders Agency Contact ID --->"+ pibStakeholders.getAgencyContact1());
	        	proposalManager.updateAgency(new Long(getRequest().getParameter("projectID")),endMarketId, agencyContact,pibStakeholders.getAgencyContact1());
	        }
        }
        

       
        	
        SynchroLogUtils.stakeholdersEdit(getRequest().getParameter("spiContact"), spi_DB,  getRequest().getParameter("projectOwner"), owner_DB, 
            		getRequest().getParameter("agencyContact"), agency_DB, project_DB.getName(), project_DB.getProjectID());
        }       

        return "delegateProject";

    }
    /*
     * This method will add the delegated users (Agency Contact)
     */
    public String delegateProjectAgencyContacts()
    {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        LOGGER.info("Checking Project ID --->"+ getRequest().getParameter("projectID"));
        LOGGER.info("Checking Agency Contact Owner --->"+ getRequest().getParameter("agencyContact"));
               
        
        LOGGER.info("Checking End Market ID --->"+ getRequest().getParameter("endMarketID"));
        
        if(getRequest().getParameter("agencyContact")!=null && !getRequest().getParameter("agencyContact").equals(""))
        {
	        Long agencyContact = new Long(getRequest().getParameter("agencyContact"));
	        Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
	        PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(new Long(getRequest().getParameter("projectID")), endMarketId);
	        pibManager.updateAgencyContact(new Long(getRequest().getParameter("projectID")),endMarketId, agencyContact);
	        
	        
	        LOGGER.info("pibStakeholders Agency Contact ID --->"+ pibStakeholders.getAgencyContact1());
	        if(pibStakeholders!=null)
	        {
	        	proposalManager.updateAgency(new Long(getRequest().getParameter("projectID")),endMarketId, agencyContact,pibStakeholders.getAgencyContact1());
	        }      
            
        }
               
        return "delegateProject";

    }
    /*
     * This method will add the delegated users (Project Owner and SPI Contact) for MM projects
     */
    public String delegateProjectMM()
    {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        LOGGER.info("Checking Project ID --->"+ getRequest().getParameter("projectID"));
        LOGGER.info("Checking Above Market Project Owner --->"+ getRequest().getParameter("projectOwnermm"));
                
        LOGGER.info("Checking Above Market SPI Contact --->"+ getRequest().getParameter("spiContactmm"));
        if(getRequest().getParameter("spiContactmm")!=null && !getRequest().getParameter("spiContactmm").equals(""))
        {
        	Long spiContact = new Long(getRequest().getParameter("spiContactmm"));
	               
        	// For MM Above Market the SP&I Contact is the Project Owner
        	synchroProjectManager.updateOwner(new Long(getRequest().getParameter("projectID")),spiContact);
        }
        if(getRequest().getParameter("projectOwnermm")!=null && !getRequest().getParameter("projectOwnermm").equals(""))
        {
        	Long projectOwner = new Long(getRequest().getParameter("projectOwnermm"));
	               
        	// For MM Above Market the  Project Owner is the Project Contact in the Funding Investment Table
        	synchroProjectManager.updateProjectContact(projectOwner, new Long(getRequest().getParameter("projectID")),new Long("-1"));
        }
        
        // Agency Contact will be updated only at Above Market Level
        if(getRequest().getParameter("agencyContactmm")!=null && !getRequest().getParameter("agencyContactmm").equals(""))
        {
	        Long agencyContact = new Long(getRequest().getParameter("agencyContactmm"));
	       // Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
	        PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(new Long(getRequest().getParameter("projectID")), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	        pibManager.updateAgencyContact(new Long(getRequest().getParameter("projectID")),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, agencyContact);
	        
	        
	       
	        if(pibStakeholders!=null)
	        {
	        	 LOGGER.info("pibStakeholders Agency Contact ID --->"+ pibStakeholders.getAgencyContact1());
	        	proposalManager.updateAgency(new Long(getRequest().getParameter("projectID")),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, agencyContact,pibStakeholders.getAgencyContact1());
	        }
        }
        
        List<Long> endMarketIDs = synchroProjectManager.getEndMarketIDs(new Long(getRequest().getParameter("projectID")));
        if(endMarketIDs!=null && endMarketIDs.size()>0)
        {
        	for(Long emId:endMarketIDs)
        	{
        		LOGGER.info("End Market Name --->"+ SynchroGlobal.getEndMarkets().get(emId.intValue()));
        		LOGGER.info("End Market Project Owner --->"+ getRequest().getParameter("projectOwnermm"+emId+getRequest().getParameter("projectID")));
        		LOGGER.info("End Market SPI Contact --->"+ getRequest().getParameter("spiContactmm"+emId+getRequest().getParameter("projectID")));
        		// Updating Project Contact for End Market in Funding Investment
        		if(getRequest().getParameter("projectOwnermm"+emId+getRequest().getParameter("projectID"))!=null && !getRequest().getParameter("projectOwnermm"+emId+getRequest().getParameter("projectID")).equals(""))
        		{
        			Long projectOwner = new Long(getRequest().getParameter("projectOwnermm"+emId+getRequest().getParameter("projectID")));
        			synchroProjectManager.updateProjectContact(projectOwner, new Long(getRequest().getParameter("projectID")),emId);
        		}
        		// Updating SPI Contact for End Market in Funding Investment
        		if(getRequest().getParameter("spiContactmm"+emId+getRequest().getParameter("projectID"))!=null && !getRequest().getParameter("spiContactmm"+emId+getRequest().getParameter("projectID")).equals(""))
        		{
        			Long spiContact = new Long(getRequest().getParameter("spiContactmm"+emId+getRequest().getParameter("projectID")));
        			synchroProjectManager.updateFundingInvSPIContact(spiContact, new Long(getRequest().getParameter("projectID")),emId);
        		}
        	
        	}
        }
        
        
        
        return "delegateProject";

    }
    /*
     * This method will add the Stakeholders - Other SPI Contact, Other Legal Contact, Other Product Contact 
     */
    public String addStakeholders()
    {
        // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
    	 
        LOGGER.info("Checking Project ID --->"+ getRequest().getParameter("projectID"));
        LOGGER.info("Checking otherSPI --->"+ getRequest().getParameter("otherSPI"));
        
        
        LOGGER.info("Checking Other Legal --->"+ getRequest().getParameter("otherLegalContact"));
        LOGGER.info("Checking Other Product Contact --->"+ getRequest().getParameter("otherProductContact"));
        
        LOGGER.info("Checking End Market ID --->"+ getRequest().getParameter("endMarketID"));
        
        if(getRequest().getParameter("otherSPI")!=null && !getRequest().getParameter("otherSPI").equals(""))
        {
        	Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
        	pibManager.updateOtherSPIContact(getRequest().getParameter("otherSPI"), new Long(getRequest().getParameter("projectID")), endMarketId);
        
        }
        if(getRequest().getParameter("otherLegalContact")!=null && !getRequest().getParameter("otherLegalContact").equals(""))
        {
        	Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
        	pibManager.updateOtherLegalContact(getRequest().getParameter("otherLegalContact"), new Long(getRequest().getParameter("projectID")), endMarketId);
        
        }
        if(getRequest().getParameter("otherProductContact")!=null && !getRequest().getParameter("otherProductContact").equals(""))
        {
        	Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
        	pibManager.updateOtherProductContact(getRequest().getParameter("otherProductContact"), new Long(getRequest().getParameter("projectID")), endMarketId);
        
        }
        if(getRequest().getParameter("otherAgencyContact")!=null && !getRequest().getParameter("otherAgencyContact").equals(""))
        {
        	Long endMarketId = new Long(getRequest().getParameter("endMarketID"));
        	pibManager.updateOtherAgencyContact(getRequest().getParameter("otherAgencyContact"), new Long(getRequest().getParameter("projectID")), endMarketId);
        
        }
       
        SynchroLogUtils.stakeholdersSave(getRequest().getParameter("otherSPI"), getRequest().getParameter("otherLegalContact"), 
        								getRequest().getParameter("otherProductContact"), getRequest().getParameter("otherAgencyContact"), 
        								synchroProjectManager.get(new Long(getRequest().getParameter("projectID"))).getName(), new Long(getRequest().getParameter("projectID")));
        
        
        return "addStakeholders";

    }
    
    public String addStakeholdersMM()
    {
    	 // This will check whether the user has accepted the Disclaimer or not.
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        LOGGER.info("Checking Project ID --->"+ getRequest().getParameter("projectID"));
        LOGGER.info("Checking otherSPI --->"+ getRequest().getParameter("otherSPIMM"));
        
        
        LOGGER.info("Checking Other Legal --->"+ getRequest().getParameter("otherLegalContactMM"));
        LOGGER.info("Checking Other Product Contact --->"+ getRequest().getParameter("otherProductContactMM"));
        
        if(getRequest().getParameter("otherSPIMM")!=null && !getRequest().getParameter("otherSPIMM").equals(""))
        {
        	pibManager.updateOtherSPIContact(getRequest().getParameter("otherSPIMM"), new Long(getRequest().getParameter("projectID")), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        if(getRequest().getParameter("otherLegalContactMM")!=null && !getRequest().getParameter("otherLegalContactMM").equals(""))
        {
        	 pibManager.updateOtherLegalContact(getRequest().getParameter("otherLegalContactMM"), new Long(getRequest().getParameter("projectID")), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        if(getRequest().getParameter("otherProductContactMM")!=null && !getRequest().getParameter("otherProductContactMM").equals(""))
        {
        	pibManager.updateOtherProductContact(getRequest().getParameter("otherProductContactMM"), new Long(getRequest().getParameter("projectID")), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        if(getRequest().getParameter("otherAgencyContactMM")!=null && !getRequest().getParameter("otherAgencyContactMM").equals(""))
        {
        	pibManager.updateOtherAgencyContact(getRequest().getParameter("otherAgencyContactMM"), new Long(getRequest().getParameter("projectID")), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        }
        List<Long> endMarketIDs = synchroProjectManager.getEndMarketIDs(new Long(getRequest().getParameter("projectID")));
        if(endMarketIDs!=null && endMarketIDs.size()>0)
        {
        	for(Long emId:endMarketIDs)
        	{
        		LOGGER.info("End Market Name --->"+ SynchroGlobal.getEndMarkets().get(emId.intValue()));
        		LOGGER.info("End Market otherSPI --->"+ getRequest().getParameter("otherSPIMM"+emId+getRequest().getParameter("projectID")));
        		LOGGER.info("End Market Other Legal --->"+ getRequest().getParameter("otherLegalContactMM"+emId+getRequest().getParameter("projectID")));
        		LOGGER.info("End Market Other Product Contact --->"+ getRequest().getParameter("otherProductContactMM"+emId+getRequest().getParameter("projectID")));
        		
        		if(getRequest().getParameter("otherSPIMM"+emId+getRequest().getParameter("projectID"))!=null && !getRequest().getParameter("otherSPIMM"+emId+getRequest().getParameter("projectID")).equals(""))
                {
                	pibManager.updateOtherSPIContact(getRequest().getParameter("otherSPIMM"+emId+getRequest().getParameter("projectID")), new Long(getRequest().getParameter("projectID")), emId);
                }
                if(getRequest().getParameter("otherLegalContactMM"+emId+getRequest().getParameter("projectID"))!=null && !getRequest().getParameter("otherLegalContactMM"+emId+getRequest().getParameter("projectID")).equals(""))
                {
                	pibManager.updateOtherLegalContact(getRequest().getParameter("otherLegalContactMM"+emId+getRequest().getParameter("projectID")), new Long(getRequest().getParameter("projectID")), emId);
                }
                if(getRequest().getParameter("otherProductContactMM"+emId+getRequest().getParameter("projectID"))!=null && !getRequest().getParameter("otherProductContactMM"+emId+getRequest().getParameter("projectID")).equals(""))
                {
                	pibManager.updateOtherProductContact(getRequest().getParameter("otherProductContactMM"+emId+getRequest().getParameter("projectID")), new Long(getRequest().getParameter("projectID")), emId);
                }
        	
        	}
        }
      
        return "addStakeholders";
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

    public void updatePage() {
        start = (page-1) * limit;
        end = start + limit;
         
        List<Project> projectList  = synchroProjectManager.getProjects(getSearchFilter());
        for(Project project : projectList )
        {
        	getSynchroUtils().updateProjectStatus(project);
        	
        }
        
        projects = this.toDelegateProjectViewBean(synchroProjectManager.getProjects(getSearchFilter()));
        
    }

    private ProjectResultFilter getSearchFilter() {
        projectResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());

        if(projectResultFilter.getKeyword() == null || projectResultFilter.getKeyword().equals("")) {
            projectResultFilter.setKeyword(keyword);
        }
        projectResultFilter.setStart(start);
        projectResultFilter.setLimit(limit);

//        if(projectResultFilter.getSortField() == null || projectResultFilter.getSortField().equals("")) {
//            projectResultFilter.setSortField("status");
//        }
//
//        if(projectResultFilter.getAscendingOrder() == null) {
//            projectResultFilter.setAscendingOrder(0);
//        }


        projectResultFilter.setFetchOnlyUserSpecificProjects(true);
        projectResultFilter.setFetchProductContacts(false);
        projectResultFilter.setFetchDraftProjects(true);

        return projectResultFilter;
    }

    private void setPaginationFilter(int page,int results) {
        start = (this.page-1)*limit;
        end = start + limit;
//        end = end >= this.results?this.results:end;
    }


    public List<DelegateProjectViewBean> toDelegateProjectViewBean(final List<Project> projects) {
        List<DelegateProjectViewBean> beans = new ArrayList<DelegateProjectViewBean>();
        for(Project project: projects) {
           
        		beans.add(DelegateProjectViewBean.toDelegateProjectViewBean(project));
        }
        return beans;
    }


    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
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



    public List<DelegateProjectViewBean> getProjects() {
        return projects;
    }

    public void setProjects(List<DelegateProjectViewBean> projects) {
        this.projects = projects;
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


	public PIBManager getPibManager() {
		return pibManager;
	}


	public void setPibManager(PIBManager pibManager) {
		this.pibManager = pibManager;
	}


	public ProposalManager getProposalManager() {
		return proposalManager;
	}


	public void setProposalManager(ProposalManager proposalManager) {
		this.proposalManager = proposalManager;
	}
}
