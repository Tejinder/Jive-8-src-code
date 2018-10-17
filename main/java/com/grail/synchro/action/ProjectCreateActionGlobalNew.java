package com.grail.synchro.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroLogUtilsNew;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;

/**
 * @author: Kanwar Grewal
 * @version 4.0
 */
public class ProjectCreateActionGlobalNew extends JiveActionSupport implements Preparable {

    private static final Logger LOGGER = Logger.getLogger(ProjectCreateActionGlobalNew.class);
    private Project project;
    
    private Project project_DB = null;
    
    private List<Long> endMarketIds;
    private List<Long> endMarketIds_DB;


    private ProjectManagerNew synchroProjectManagerNew;
    private Long projectID;
    private Long templateID;
    private Boolean initiatePage = true;
    private EmailManager emailManager;
    private StageManager stageManager;
    private PermissionManager permissionManager;
    private EmailNotificationManager emailNotificationManager;

    private String draft;
    private List<EndMarketInvestmentDetail> endMarketDetails;
    private Long endMarketId;

    private String draftLocation;
    private String redirectUrl;
    private Boolean isDraft = false;
    private Boolean isEdited = false;
    private List<ProjectCostDetailsBean> projectCostDetails;
    
    List<ProjectCostDetailsBean> projectCostDetailsList;
   
    
    // This is used in case of Global EU Project Type Radio box selection
    private String globalProjectType;

    public void prepare() throws Exception {
    	System.out.println("Inside prepare ProjectCreate Action --");
        final String id = getRequest().getParameter("projectID");

        if(StringUtils.isNotBlank(id))
        {
            try
            {
                if(projectID==null)
                    projectID = Long.parseLong(id);
            } catch (NumberFormatException nfEx) {
            	LOGGER.error("Invalid ProjectID ");
                throw nfEx;
            }
            project = this.synchroProjectManagerNew.get(projectID);
            project_DB = this.synchroProjectManagerNew.get(projectID);
            endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
            endMarketIds = this.synchroProjectManagerNew.getEndMarketIDs(projectID);
            endMarketIds_DB = this.synchroProjectManagerNew.getEndMarketIDs(projectID);
            
            projectCostDetailsList = this.synchroProjectManagerNew.getProjectCostDetails(projectID);
            List<String> initialCost = new ArrayList<String>();
            List<Long> initialCostCurrency = new ArrayList<Long>();
            List<Long> spiContact = new ArrayList<Long>();
           if(endMarketDetails!=null && endMarketDetails.size()>0)
           {
	            //initialCost.add(endMarketDetails.get(0).getInitialCost().toString());
	            //initialCostCurrency.add(endMarketDetails.get(0).getInitialCostCurrency().longValue());
	            //spiContact.add(endMarketDetails.get(0).getSpiContact());
	            endMarketId = endMarketDetails.get(0).getEndMarketID();
           }
            project.setInitialCost(initialCost);
            project.setInitialCostCurrency(initialCostCurrency);
            project.setSpiContact(spiContact);


           
        }
        else
        {
            this.project = new Project();
        }
        globalProjectType = getRequest().getParameter("globalProjectType");
        
        LOGGER.info("Checking Draft Location --"+ getRequest().getParameter("draftLocation"));
        if(StringUtils.isNotBlank(getRequest().getParameter("globalProjectType"))) {
            project.setOnlyGlobalType(1);
        }
        // Apply request binding ONLY if the request is of type POST
        if(getRequest().getMethod().equalsIgnoreCase("POST")){
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());
            /*if(binder.getBindingResult().hasErrors()){
                LOGGER.debug("Error occurred while binding the request object with the Project bean in Create Project Action");
                input();
            }
            */
            System.out.println("Category Type --" + getRequest().getParameter("categoryType"));
            System.out.println("Category Type --" + getRequest().getParameterValues("categoryType"));
            if(StringUtils.isNotBlank(getRequest().getParameter("startDate"))) {
                this.project.setStartDate(DateUtils.parse(getRequest().getParameter("startDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("endDate"))) {
                this.project.setEndDate(DateUtils.parse(getRequest().getParameter("endDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("projectID"))) {
                project.setProjectID(projectID);
            }
           
            if(StringUtils.isNotBlank(getRequest().getParameter("globalProjectType"))) {
                project.setOnlyGlobalType(1);
            }
           
           
//            if(this.project.getProposedMethodology() != null && this.project.getProposedMethodology().size() > 0) {
//                Long pmId = this.project.getProposedMethodology().get(0);
//                if(pmId != null) {
//                    Long mId = SynchroGlobal.getMethodologyTypeByProposedMethodlogy(pmId);
//                    if(mId != null) {
//                        this.project.setMethodologyType(mId);
//                    }
//                }
//            }

            if(this.project.getProposedMethodology() != null && this.project.getProposedMethodology().size() > 0) {
                  this.project.setMethodologyType(SynchroGlobal.getMethodologyTypeByProsedMethodologies(this.project.getProposedMethodology()));
            }
            this.draft = getRequest().getParameter("draft");
            this.draftLocation = getRequest().getParameter("draftLocation");
        }
    }


    @Override
    public String input() {
    	  //Authentication layer
        final User jiveUser = getUser();
        if(jiveUser != null) {
          

            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
                return UNAUTHORIZED;
            }
        }
        if(!SynchroPermHelper.canInitiateProject_NEW())
        {
            return UNAUTHORIZED;
        }
        /*
          if(!SynchroPermHelper.canInitiateProject_NEW())
          {
              return UNAUTHORIZED;
          }
          */
        //TODO Process INPUT Parameters
        if(this.project.getProjectID() == null){
            this.project = new Project();
        }else{
            //ProjectTemplate template = synchroProjectManager.getTemplate(templateID);
            //  processTemplate(template);
        }
        return INPUT;
    }

    @Override
    public void validate() {

        if(StringUtils.isEmpty(project.getName())){
            addFieldError("name", "Please enter the Project name");
        }
       /* if(StringUtils.isEmpty(project.getDescription())){
            addFieldError("description", "Please enter the Project Description");
        }*/
    }


    public String execute() {

        //Authentication layer
        if(!SynchroPermHelper.canInitiateProject_NEW())
        {
            return UNAUTHORIZED;
        }

        //TODO : Process Save Functions
        String result = SUCCESS;
        
        
   
     //   project.setMultiMarket(false);

        // This flag will check whether a new project is getting saved or an existing one. https://svn.sourcen.com/issues/19604
        boolean isNewProject = false;
        if(project.getProjectID() == null)
        {
            isNewProject=true;
        }
        else
        {
            isNewProject=false;
        }

        try{
            //result = createSynchroCommunity();
            User user = getUser();
            project.setBriefCreator(user.getID());
            if(draft!=null && draft.contains("draft"))
            {
                //project.setStatus(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
            	project.setStatus(new Long(SynchroGlobal.ProjectStatusNew.DRAFT.ordinal()));
                result = INPUT;
                isDraft = true;
            }
            else if(draftLocation!=null && !draftLocation.equals("") && draftLocation.contains("/"))
            {
              //  project.setStatus(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
            	project.setStatus(new Long(SynchroGlobal.ProjectStatusNew.DRAFT.ordinal()));
                isDraft = true;
            }
            else
            {
                //project.setStatus(new Long(SynchroGlobal.Status.PIT_OPEN.ordinal()));
            	project.setStatus(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
                isDraft = false;
            }

            //Check if project is Edited and saved as Draft mode again
            if(project!=null && project.getProjectID()!=null && isDraft)
            {
            	isEdited = true;
            }
            
            // Setting project Type : Global, Regional, EndMarket
            // The second check is in case the Global User is initiating the project, then it will be treated as Global Project
            if(SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner())
            {
            	//project.setProjectType(SynchroGlobal.ProjectType.GLOBAL.getId());
            	
            	// This is done in case the Admin or System Owner tries to create a project with Budget Location as Region, then that project should be created as Regional Project
            	if(project.getBudgetLocation()!=null && SynchroGlobal.getRegions().keySet().contains(project.getBudgetLocation()))
            	{
            		project.setProjectType(SynchroGlobal.ProjectType.REGIONAL.getId());
            	}
            	else
            	{
            		project.setProjectType(SynchroGlobal.ProjectType.GLOBAL.getId());
            	}
            }
            else if(SynchroPermHelper.isRegionalUserType())
            {
            	project.setProjectType(SynchroGlobal.ProjectType.REGIONAL.getId());
            }
            else if(SynchroPermHelper.isEndMarketUserType())
            {
            	project.setProjectType(SynchroGlobal.ProjectType.ENDMARKET.getId());
            }
            
            // As this action is called specifically only for Global Non EU process.
            project.setProcessType(SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId());
            /*if(project.getEndMarkets() != null && project.getEndMarkets().size()>0)
            {
            	// If the Fieldwork Study Type is Yes then the Process type is Fieldwork
            	if(project.getFieldWorkStudy()!=null && project.getFieldWorkStudy()==1)
            	{
            		project.setProcessType(SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId());
            		
            	}
            	else if(project.getProjectType()!=null)
            	{
            		project.setProcessType(SynchroUtils.getProjectProcessType(project.getProjectType(), project.getEndMarkets().get(0)));
            	}
            }*/
            /*if(StringUtils.isNotBlank(getRequest().getParameter("euMarketConfirmation")))
            {
            	project.setEuMarketConfirmation(Integer.parseInt(getRequest().getParameter("euMarketConfirmation")));
            }*/
            
            // http://redmine.nvish.com/redmine/issues/341
            if(project.getBrandSpecificStudy()!=null && project.getBrandSpecificStudy()==1)
            {
            	project.setBrandSpecificStudyType(new Integer("-1"));
            }
            else if(project.getBrandSpecificStudy()!=null && project.getBrandSpecificStudy()==2)
            {
            	project.setBrand(new Long("-1"));
            }
           
            project.setHasNewSynchroSaved(true);
            
            project = synchroProjectManagerNew.save(project);
            synchroProjectManagerNew.deleteProjectCostDetails(projectID);
            saveProjectCostDetails();
      
            
            
            result = saveEndMarketDetails();
            updateReferenceEndMarkets();
            saveFundingEndMarkets();
            projectID = project.getProjectID();
            
          //Add Audit Logs for Project Create/Draft respectively
            if(isDraft && !isEdited)
            {
            	 //Add User Audit details for Project Draft version
            	SynchroLogUtilsNew.addLog(project, SynchroGlobal.LogActivity.PIT_DRAFT, SynchroGlobal.Activity.ADD.getId(), getText("logger.project.draft"));
            	
            	SynchroLogUtilsNew.ProjectInitiationSaveNew(project_DB, project, SynchroGlobal.LogProjectStage.DRAFT.getId(), endMarketIds_DB, endMarketIds);
            }
            else if(isEdited)
            {
            	//Add User Audit details for Project Create activity
            	SynchroLogUtilsNew.addLog(project, SynchroGlobal.LogActivity.PIT_CREATE, SynchroGlobal.Activity.EDIT.getId(), getText("logger.project.draft"));
            	SynchroLogUtilsNew.ProjectInitiationSaveNew(project_DB, project, SynchroGlobal.LogProjectStage.CREATE.getId(), endMarketIds_DB, endMarketIds);
            }
            else
            {
            	//Add User Audit details for Project Create activity
            	SynchroLogUtilsNew.addLog(project, SynchroGlobal.LogActivity.PIT_CREATE, SynchroGlobal.Activity.ADD.getId(), getText("logger.project.create"));
            	SynchroLogUtilsNew.ProjectInitiationSaveNew(project_DB, project, SynchroGlobal.LogProjectStage.CREATE.getId(), endMarketIds_DB, endMarketIds);
            }

        }catch(IllegalArgumentException ie)
        {
            LOGGER.debug("Community Name already exists" + ie.getMessage());
            addFieldError("name", "Project name is already used");
            result = INPUT;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        	LOGGER.debug("Error while saving project details to database " + e.getMessage());
            result = INPUT;
        }
        // Don't send the notifications in case the project is SAVED as DRAFT and it should return on the same page.
        if(draft!=null && draft.contains("draft"))
        {
            if(isNewProject)
            {
            	if(StringUtils.isNotBlank(getRequest().getParameter("globalProjectType"))) {
            		redirectUrl="/new-synchro/create-project-global!input.jspa?projectID="+projectID+"&globalProjectType=GLOBAL&savefirst=true";
                }
            	else
            	{
            		redirectUrl="/new-synchro/create-project-global!input.jspa?projectID="+projectID+"&savefirst=true";
            	}
            	
            	return "draft-initial";
            }
            else
            {
            	if(StringUtils.isNotBlank(getRequest().getParameter("globalProjectType"))) {
            		redirectUrl="/new-synchro/create-project-global!input.jspa?projectID="+projectID+"&globalProjectType=GLOBAL";
                }
            	else
            	{
            		redirectUrl="/new-synchro/create-project-global!input.jspa?projectID="+projectID;
            	}
            	return "draft";
            }
        }
        //if(draftLocation!=null && !draftLocation.equals(""))
        if(draftLocation!=null && !draftLocation.equals("") && draftLocation.contains("/"))
        {

            this.redirectUrl = draftLocation;
            LOGGER.info("Checking redirectUrl --"+ getRequest().getParameter("draftLocation"));
            return "draftLocation";
        }
        if(result.equals(SUCCESS))
        {
            sendEmailNotifications(project);
        }


        return result;
    }


    //Save End Market Investment Details
    private String saveEndMarketDetails() {
        final Long userID = getUser().getID();
        // The Existing end market needs to be deleted for SAVE a Draft
        synchroProjectManagerNew.deleteEndMarketDetail(project.getProjectID());

        if(project.getProjectID()!=null && project.getEndMarkets() != null){
            for(Long endMarketID : project.getEndMarkets()){
                if(endMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
	                if(project.getInitialCost()!=null && project.getInitialCost().size()>0)
	                {
	                	endMarketInvestmentDetail.setInitialCost(SynchroUtils.formatDate(project.getInitialCost().get(0)));
	                }
	                if(project.getInitialCostCurrency()!=null && project.getInitialCostCurrency().size()>0)
	                {
	                    endMarketInvestmentDetail.setInitialCostCurrency(project.getInitialCostCurrency().get(0));
	                }
	                if(project.getSpiContact()!=null && project.getSpiContact().size()>0)
	                {
	                	endMarketInvestmentDetail.setSpiContact(project.getSpiContact().get(0));
	                }
	                endMarketInvestmentDetail.setCreationBy(userID);
	                endMarketInvestmentDetail.setModifiedBy(userID);
	                synchroProjectManagerNew.saveEndMarketDetail(endMarketInvestmentDetail);
	               
	              //  synchroProjectManager.setAllEndMarketStatus(project.getProjectID(), SynchroGlobal.ProjectActivationStatus.OPEN.ordinal());
                }   
            }
        }
        return SUCCESS;
    }

    private void updateReferenceEndMarkets()
    {
    	String[] referenceEndMarkets = getRequest().getParameterValues("referenceEndMarkets");
    	String[] referenceSynchroCodes = getRequest().getParameterValues("referenceSynchroCodes");
    	if(referenceEndMarkets!=null && referenceEndMarkets.length>0)
    	{
    		for(int i=0;i<referenceEndMarkets.length;i++)
   		 	{
    			try
    			{
	    			if(referenceSynchroCodes!=null && referenceSynchroCodes[i]!=null && StringUtils.isNotBlank(referenceSynchroCodes[i]))
	    			{
	    				EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), new Long(referenceEndMarkets[i]));
	    				endMarketInvestmentDetail.setReferenceSynchroCode(new Long(referenceSynchroCodes[i]));
	    				synchroProjectManagerNew.updateReferenceEndMarkets(endMarketInvestmentDetail);
	    			}
    			}
    			catch(Exception e)
    			{
    				 LOGGER.debug("Exception while updating reference Synchro Codes" + e.getMessage());
    			}
   		 	}
    	}
    }
    
  //Save/Update Funding End Market Details
    private String saveFundingEndMarkets() {
       

        if(project.getProjectID()!=null && project.getFundingMarkets() != null && project.getEndMarkets()!=null){
            
        	for(Long endMarketID : project.getEndMarkets()){
                if(endMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
	            	endMarketInvestmentDetail.setIsFundingMarket(false);
	                synchroProjectManagerNew.updateFundingEndMarkets(endMarketInvestmentDetail);
                }   
            }
        	for(Long fundingMarketID : project.getFundingMarkets()){
                if(fundingMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), fundingMarketID);
	            	endMarketInvestmentDetail.setIsFundingMarket(true);
	                synchroProjectManagerNew.updateFundingEndMarkets(endMarketInvestmentDetail);
	               
	             
                }   
            }
        }
        return SUCCESS;
    }
    private void saveProjectCostDetails()
    {
    	 projectCostDetails = new ArrayList<ProjectCostDetailsBean>();
    	 ProjectCostDetailsBean pcbean = new ProjectCostDetailsBean();
         
    	/* String agency =  getRequest().getParameter("agency");
    	 String costComponent =  getRequest().getParameter("costComponent");
    	 String currency = getRequest().getParameter("currency");
    	 String agencyCost = getRequest().getParameter("agencyCost");
    	 ProjectCostDetailsBean pcbean = new ProjectCostDetailsBean();
    	 
    	 if(agency!=null &&  !agency.equalsIgnoreCase("") && costComponent!=null && !costComponent.equalsIgnoreCase("") 
    			 && currency!=null && !currency.equalsIgnoreCase("") && agencyCost!=null && !agencyCost.equalsIgnoreCase(""))
    	 {
	    	 
	    	 pcbean.setProjectId(project.getProjectID());
	         pcbean.setAgencyId(new Long(agency));
	         pcbean.setCostComponent(new Integer(costComponent));
	         pcbean.setCostCurrency(new Integer(currency));
	         pcbean.setEstimatedCost(new BigDecimal(agencyCost.replaceAll(",","")));
	         projectCostDetails.add(pcbean);
    	 }
         */
         
    	 String[] agencies = getRequest().getParameterValues("agencies");
         String[] costComponents = getRequest().getParameterValues("costComponents");
         String[] currencies = getRequest().getParameterValues("currencies");
         String[] agencyCosts = getRequest().getParameterValues("agencyCosts");
         
    	 if(agencies!=null && agencies.length>0)
    	 {
    		// for(int i=0;i<agencies.length;i++)
    		 for(int i=(agencies.length-1);i>=0;i--)
    		 {
    			
    			 if(StringUtils.isNotBlank(agencies[i]) && StringUtils.isNotBlank(costComponents[i]) && StringUtils.isNotBlank(currencies[i]) && StringUtils.isNotBlank(agencyCosts[i]))
    			 {
	    			 pcbean = new ProjectCostDetailsBean();
	    	    	 pcbean.setProjectId(project.getProjectID());
	    	         
	    	    	try
	    	    	{
	    	    		pcbean.setAgencyId(new Long(agencies[i]));
	    	    	}
	    	    	catch(Exception e)
	    	    	{
	    	    		
	    	    	}
	    	         
	    	         try
	    	         {
	    	        	 pcbean.setCostComponent(new Integer(costComponents[i]));
	    	         }
	    	         catch(Exception e)
	    	         {
	    	        	 
	    	         }
	    	         try
	    	         {
	    	        	 pcbean.setCostCurrency(new Integer(currencies[i]));
	    	         }
	    	         catch(Exception e)
	    	         {
	    	        	 
	    	         }
	    	         try
	    	         {
	    	        	 pcbean.setEstimatedCost(new BigDecimal(agencyCosts[i].replaceAll(",","")));
	    	         }
	    	         catch(Exception e)
	    	         {
	    	        	 
	    	         }
	    	         projectCostDetails.add(pcbean);
    			 }
    		 }
    	 }
    	 if(projectCostDetails!=null && projectCostDetails.size()>0)
    	 {
    		 if(StringUtils.isNotBlank(getRequest().getParameter("totalCostHidden"))) {
                 project.setTotalCost(new BigDecimal(getRequest().getParameter("totalCostHidden").replaceAll(",","")));
 
             }
    		 
    		 synchroProjectManagerNew.saveProjectCoseDetails(projectCostDetails, project.getTotalCost());
    	 }
    }


    /*
     private void processTemplate(ProjectTemplate template)
     {
         this.project = new Project();
         this.project.setName(template.getName());
         this.project.setDescription(template.getDescription());
         this.project.setOwnerID(template.getOwnerID());
         this.project.setCategoryType(template.getCategoryType());
         this.project.setBrand(template.getBrand());
         this.project.setMethodology(template.getMethodology());
         this.project.setMethodologyGroup(template.getMethodologyGroup());
         this.project.setProposedMethodology(template.getProposedMethodology());
         this.project.setEndMarkets(template.getEndMarkets());
         this.project.setStartDate(template.getStartDate());
         this.project.setEndDate(template.getEndDate());
         this.project.setSpi(template.getSpi());
     }
    */
    public void setSynchroProjectManagerNew(ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
    }

    public Long getProjectID() {
        return projectID;
    }


    public void setProjectID(Long projectID) {
        this.projectID = projectID;
    }
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    public void setTemplateID(Long templateID) {
        this.templateID = templateID;
    }


    public Boolean getInitiatePage() {
        return initiatePage;
    }


    public void setEmailManager(EmailManager emailManager) {
        this.emailManager = emailManager;
    }

    public PermissionManager getPermissionManager() {
        if(permissionManager == null){
            permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }


    public void sendEmailNotifications(Project project)
    {

        if(project!=null && project.getBriefCreator()>0)
        {

            /**
             * Email Notification to Project Initiator
             */
            try{
                User projectInitiator = userManager.getUser(project.getBriefCreator());
                
                String adminEmail = SynchroPermHelper.getSystemAdminEmail();
                
                String adminName = SynchroPermHelper.getSystemAdminName();
          
                String projectURL="";
                if(project.getFieldWorkStudy()!=null && project.getFieldWorkStudy()==1)
                {
                	projectURL =  SynchroUtils.getJiveURL()+"/new-synchro/proposal-details-fieldwork!input.jspa?projectID="+project.getProjectID();
                }
                else
                {
                	projectURL =  SynchroUtils.getJiveURL()+"/new-synchro/pib-details!input.jspa?projectID="+project.getProjectID();
                }
                        
                String recipients = projectInitiator.getEmail();
                if(recipients.length()>0)
                {
                    EmailMessage email = stageManager.populateNotificationEmail(recipients, null, null,"pit.project.created.new.htmlBody","pit.project.created.new.subject");
                    //email.getContext().put("projectId", projectID);
                    email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                    email.getContext().put("projectName",project.getName());
                    email.getContext().put ("stageUrl",projectURL);
                    
                    stageManager.sendNotificationNew(adminName, adminEmail, email);

                    //Email Notification TimeStamp Storage
                    EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                    emailNotBean.setProjectID(projectID);
                    //emailNotBean.setEndmarketID(endMarketId);
                    //	emailNotBean.setAgencyID(agencyID);
                    emailNotBean.setStageID(SynchroConstants.PIT_STAGE);
                    emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                    emailNotBean.setEmailDesc("Notification | New Project Initiated");

                    emailNotBean.setEmailSubject("Notification | New Project Initiated");
                    emailNotBean.setEmailSender(adminEmail);
                    emailNotBean.setEmailRecipients(recipients);
                    emailNotificationManager.saveDetails(emailNotBean);
                }

            } catch (com.jivesoftware.base.UserNotFoundException e) {
                // TODO Auto-generated catch block
            	LOGGER.error("Error sending email notification to Project owner - User Not Found " + e.getMessage());
            }

        }
    }


    public String getDraft() {
        return draft;
    }


    public void setDraft(String draft) {
        this.draft = draft;
    }


    public List<EndMarketInvestmentDetail> getEndMarketDetails() {
        return endMarketDetails;
    }


    public void setEndMarketDetails(List<EndMarketInvestmentDetail> endMarketDetails) {
        this.endMarketDetails = endMarketDetails;
    }


    public Long getEndMarketId() {
        return endMarketId;
    }


    public void setEndMarketId(Long endMarketId) {
        this.endMarketId = endMarketId;
    }


    public String getDraftLocation() {
        return draftLocation;
    }


    public void setDraftLocation(String draftLocation) {
        this.draftLocation = draftLocation;
    }


    public String getRedirectUrl() {
        return redirectUrl;
    }


    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }


	public List<ProjectCostDetailsBean> getProjectCostDetailsList() {
		return projectCostDetailsList;
	}


	public void setProjectCostDetailsList(
			List<ProjectCostDetailsBean> projectCostDetailsList) {
		this.projectCostDetailsList = projectCostDetailsList;
	}
	public List<ProjectCostDetailsBean> getProjectCostDetails() {
		return projectCostDetails;
	}


	public void setProjectCostDetails(
			List<ProjectCostDetailsBean> projectCostDetails) {
		this.projectCostDetails = projectCostDetails;
	}


	public EmailNotificationManager getEmailNotificationManager() {
        return emailNotificationManager;
    }


    public void setEmailNotificationManager(
            EmailNotificationManager emailNotificationManager) {
        this.emailNotificationManager = emailNotificationManager;
    }


    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }


	public List<Long> getEndMarketIds() {
		return endMarketIds;
	}


	public void setEndMarketIds(List<Long> endMarketIds) {
		this.endMarketIds = endMarketIds;
	}


	public Project getProject_DB() {
		return project_DB;
	}


	public void setProject_DB(Project project_DB) {
		this.project_DB = project_DB;
	}


	public List<Long> getEndMarketIds_DB() {
		return endMarketIds_DB;
	}


	public void setEndMarketIds_DB(List<Long> endMarketIds_DB) {
		this.endMarketIds_DB = endMarketIds_DB;
	}


	public String getGlobalProjectType() {
		return globalProjectType;
	}


	public void setGlobalProjectType(String globalProjectType) {
		this.globalProjectType = globalProjectType;
	}

}
