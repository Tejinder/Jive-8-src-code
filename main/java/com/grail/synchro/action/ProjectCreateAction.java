package com.grail.synchro.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.Project;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtils;
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
public class ProjectCreateAction extends JiveActionSupport implements Preparable {

    private static final Logger LOGGER = Logger.getLogger(ProjectCreateAction.class);
    private Project project;


    private ProjectManager synchroProjectManager;
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
            project = this.synchroProjectManager.get(projectID);
            endMarketDetails = this.synchroProjectManager.getEndMarketDetails(projectID);
            List<String> initialCost = new ArrayList<String>();
            List<Long> initialCostCurrency = new ArrayList<Long>();
            List<Long> spiContact = new ArrayList<Long>();
           if(endMarketDetails!=null && endMarketDetails.size()>0)
           {
	            initialCost.add(endMarketDetails.get(0).getInitialCost().toString());
	            initialCostCurrency.add(endMarketDetails.get(0).getInitialCostCurrency().longValue());
	            spiContact.add(endMarketDetails.get(0).getSpiContact());
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
        LOGGER.info("Checking Draft Location --"+ getRequest().getParameter("draftLocation"));
        // Apply request binding ONLY if the request is of type POST
        if(getRequest().getMethod().equalsIgnoreCase("POST")){
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOGGER.debug("Error occurred while binding the request object with the Project bean in Create Project Action");
                input();
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("startDate"))) {
                this.project.setStartDate(DateUtils.parse(getRequest().getParameter("startDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("endDate"))) {
                this.project.setEndDate(DateUtils.parse(getRequest().getParameter("endDate")));
            }
            if(StringUtils.isNotBlank(getRequest().getParameter("projectID"))) {
                project.setProjectID(projectID);
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
            // This will check whether the user has accepted the Disclaimer or not.
//            if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//            {
//                return "disclaimererror";
//            }

            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
                return UNAUTHORIZED;
            }
        }
        if(!SynchroPermHelper.canInitiateProject(jiveUser))
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
        if(StringUtils.isEmpty(project.getDescription())){
            addFieldError("description", "Please enter the Project Description");
        }
    }


    public String execute() {

        //Authentication layer
        if(!SynchroPermHelper.canInitiateProject(getUser()))
        {
            return UNAUTHORIZED;
        }

        //TODO : Process Save Functions
        String result = SUCCESS;

        project.setMultiMarket(false);

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
            if(draft!=null && draft.equalsIgnoreCase("draft"))
            {
                project.setStatus(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
                result = INPUT;
                isDraft = true;
            }
            else if(draftLocation!=null && !draftLocation.equals("") && draftLocation.contains("/"))
            {
                project.setStatus(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
                isDraft = true;
            }
            else
            {
                project.setStatus(new Long(SynchroGlobal.Status.PIT_OPEN.ordinal()));
                isDraft = false;
            }

            //Check if project is Edited and saved as Draft mode again
            if(project!=null && project.getProjectID()!=null && isDraft)
            {
            	isEdited = true;
            }

            project = synchroProjectManager.save(project);
            
            //Add Audit Logs for Project Create/Draft respectively
            if(isDraft && !isEdited)
            {
            	 //Add User Audit details for Project Draft version
            	SynchroLogUtils.addLog(project, SynchroGlobal.LogActivity.PIT_DRAFT, SynchroGlobal.Activity.ADD.getId(), getText("logger.project.draft"));
            }
            else if(isEdited)
            {
            	//Add User Audit details for Project Create activity
            	SynchroLogUtils.addLog(project, SynchroGlobal.LogActivity.PIT_CREATE, SynchroGlobal.Activity.EDIT.getId(), getText("logger.project.draft"));
            }
            else
            {
            	//Add User Audit details for Project Create activity
            	SynchroLogUtils.addLog(project, SynchroGlobal.LogActivity.PIT_CREATE, SynchroGlobal.Activity.ADD.getId(), getText("logger.project.create"));
            }
            
            result = saveEndMarketDetails();
            projectID = project.getProjectID();

        }catch(IllegalArgumentException ie)
        {
            LOGGER.debug("Community Name already exists" + ie.getMessage());
            addFieldError("name", "Project name is already used");
            result = INPUT;
        }
        catch(Exception e)
        {
            LOGGER.debug("Error while saving project details to database " + e.getMessage());
            result = INPUT;
        }
        // Don't send the notifications in case the project is SAVED as DRAFT and it should return on the same page.
        if(draft!=null && draft.equalsIgnoreCase("draft"))
        {
            if(isNewProject)
            {
                return "draft-initial";
            }
            else
            {
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
        synchroProjectManager.deleteEndMarketDetail(project.getProjectID());

        if(project.getProjectID()!=null && project.getEndMarkets() != null){
            for(Long endMarketID : project.getEndMarkets()){
                final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
                if(project.getInitialCost()!=null && project.getInitialCost().size()>0)
                {
                	endMarketInvestmentDetail.setInitialCost(SynchroUtils.formatDate(project.getInitialCost().get(0)));
                }
                if(project.getInitialCostCurrency()!=null)
                {
                    endMarketInvestmentDetail.setInitialCostCurrency(project.getInitialCostCurrency().get(0));
                }
                if(project.getSpiContact()!=null && project.getSpiContact().size()>0)
                {
                	endMarketInvestmentDetail.setSpiContact(project.getSpiContact().get(0));
                }
                endMarketInvestmentDetail.setCreationBy(userID);
                endMarketInvestmentDetail.setModifiedBy(userID);
                synchroProjectManager.saveEndMarketDetail(endMarketInvestmentDetail);
                synchroProjectManager.setAllEndMarketStatus(project.getProjectID(), SynchroGlobal.ProjectActivationStatus.OPEN.ordinal());
            }
        }
        return SUCCESS;
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
    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
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

        if(project!=null && project.getProjectOwner()>0)
        {

            /**
             * Email Notification to Project Owner and SPi Contact
             */
            try{
                User owner = userManager.getUser(project.getProjectOwner());
                User spi = userManager.getUser(project.getSpiContact().get(0));
                String projectURL =  SynchroUtils.getJiveURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID();
                String subject = String.format(SynchroGlobal.EmailNotification.ADD_PROJECT_OWNER.getSubject(), project.getName(),project.getProjectID());
                String body = String.format(SynchroGlobal.EmailNotification.ADD_PROJECT_OWNER.getMessageBody(),projectURL, project.getName(),project.getProjectID());

                //EmailNotification#11 recipients for project create notification
                //String recipients = owner.getEmail()+','+spi.getEmail();
                //String recipients = owner.getEmail();
                String recipients = "";
                //https://www.svn.sourcen.com//issues/19363
                if(project.getBriefCreator().intValue()!=project.getProjectOwner().intValue())
                {
                    recipients = owner.getEmail();
                }
                if(project.getBriefCreator().intValue()!=project.getSpiContact().get(0).intValue())
                {
                    if(project.getBriefCreator().intValue()!=project.getProjectOwner().intValue())
                    {
                        recipients = owner.getEmail()+','+spi.getEmail();
                    }
                    else
                    {
                        recipients = spi.getEmail();
                    }

                }


                if(recipients.length()>0)
                {
                    EmailMessage email = stageManager.populateNotificationEmail(recipients, subject, body,"pit.project.created.htmlBody","pit.project.created.subject");
                    //email.getContext().put("projectId", projectID);
                    email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
                    email.getContext().put("projectName",project.getName());
                    email.getContext().put ("stageUrl",projectURL);
                    stageManager.sendNotification(getUser(),email);

                    //Email Notification TimeStamp Storage
                    EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                    emailNotBean.setProjectID(projectID);
                    //emailNotBean.setEndmarketID(endMarketId);
                    //	emailNotBean.setAgencyID(agencyID);
                    emailNotBean.setStageID(SynchroConstants.PIT_STAGE);
                    emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                    emailNotBean.setEmailDesc("Notification | You have been added to a New project");

                    emailNotBean.setEmailSubject("Notification | You have been added to a New project");
                    emailNotBean.setEmailSender(getUser().getEmail());
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

}
