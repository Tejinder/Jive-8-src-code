package com.grail.synchro.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.subethamail.smtp.util.EmailUtils;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.MultiMarketProject;
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
 * @version 4.1
 */
public class ProjectMultiMarketCreateAction extends JiveActionSupport implements Preparable {

    private static final Logger LOGGER = Logger.getLogger(ProjectMultiMarketCreateAction.class);
    private MultiMarketProject project;
    private ProjectManager synchroProjectManager;
    private Long projectID;
    private Long templateID;
    private Boolean initiatePage = true;
    private EmailManager emailManager;
    private StageManager stageManager;
    private PermissionManager permissionManager;
    private EmailNotificationManager emailNotificationManager;

    private String draft;
    private List<Long> endMarketIds;
    private List<FundingInvestment> fundingInvestments;

    private String draftLocation;
    private String redirectUrl;

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

        final String id = getRequest().getParameter("projectID");
        this.project = new MultiMarketProject();
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
            Project lProject = this.synchroProjectManager.get(projectID);
            endMarketIds = this.synchroProjectManager.getEndMarketIDs(projectID);
            //List<String> initialCost = new ArrayList<String>();
            //List<Long> initialCostCurrency = new ArrayList<Long>();
            //List<Long> spiContact = new ArrayList<Long>();
            //initialCost.add(endMarketDetails.get(0).getInitialCost().toString());
            //initialCostCurrency.add(endMarketDetails.get(0).getInitialCostCurrency().longValue());
            //spiContact.add(endMarketDetails.get(0).getSpiContact());
            //project.setInitialCost(initialCost);
            //project.setInitialCostCurrency(initialCostCurrency);
            //project.setSpiContact(spiContact);
            //endMarketId = endMarketDetails.get(0).getEndMarketID();


            project.setProjectID(projectID);
            project.setName(lProject.getName());
            project.setDescription(lProject.getDescription());
            project.setDescriptionText(lProject.getDescriptionText());
            project.setCategoryType(lProject.getCategoryType());
            project.setBrand(lProject.getBrand());
            project.setMethodologyType(lProject.getMethodologyType());
            project.setProposedMethodology(lProject.getProposedMethodology());
            project.setStartDate(lProject.getStartDate());
            project.setEndDate(lProject.getEndDate());
            project.setProjectOwner(lProject.getProjectOwner());
            project.setBriefCreator(lProject.getBriefCreator());
            project.setMultiMarket(lProject.getMultiMarket());
            project.setStatus(lProject.getStatus());
            project.setConfidential(lProject.getConfidential());
            project.setCapRating(lProject.getCapRating());
            project.setTotalCost(lProject.getTotalCost());
            project.setTotalCostCurrency(lProject.getTotalCostCurrency());
            project.setEndMarkets(endMarketIds);
            project.setMethodologyGroup(lProject.getMethodologyGroup());
            project.setRegions(lProject.getRegions());

            project.setAreas(lProject.getAreas());
            project.setBudgetYear(lProject.getBudgetYear());


            fundingInvestments = this.synchroProjectManager.getProjectInvestments(projectID);

        }
        if(getRequest().getMethod().equalsIgnoreCase("POST")){
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.project);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOGGER.debug("Error occurred while binding the request object with the Multi Market Project bean in Create Project Action");
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
            this.draft = getRequest().getParameter("draft");
            this.draftLocation = getRequest().getParameter("draftLocation");

            String[] markets = getRequest().getParameterValues("markets");
            LOGGER.info("Checking Markets  in Action-->"+markets);
            LOGGER.info("Checking Markets  VALUES in Action-->"+getRequest().getParameterValues("markets"));
            if(markets!=null && markets.length>0)
            {
                List<Long> endMarketIds = new ArrayList<Long>();
                List<Long> regionIds = new ArrayList<Long>();
                List<Long> areaIds = new ArrayList<Long>();
                for(String market:markets)
                {
                    if(market.contains("A"))
                    {
                        endMarketIds.add(new Long(market.replace("A", "")));
                    }
                    if(market.contains("B"))
                    {
                        regionIds.add(new Long(market.replace("B", "")));
                    }
                    if(market.contains("C"))
                    {
                        areaIds.add(new Long(market.replace("C", "")));
                    }
                }
                this.project.setRegions(regionIds);
                this.project.setAreas(areaIds);
                this.project.setEndMarkets(endMarketIds);

            }

            if(!StringUtils.isNotBlank(getRequest().getParameter("markets")))
            {
                this.project.setRegions(null);
                this.project.setAreas(null);
                this.project.setEndMarkets(null);
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
//            if(this.project.getMethodologyGroup() != null && this.project.getMethodologyGroup().intValue() > 0) {
//                Long mtId = SynchroGlobal.getMethodologyTypeByGroup(this.project.getMethodologyGroup());
//                this.project.setMethodologyType(mtId);
//            }

            if(this.project.getProposedMethodology() != null && this.project.getProposedMethodology().size() > 0) {
                this.project.setMethodologyType(SynchroGlobal.getMethodologyTypeByProsedMethodologies(this.project.getProposedMethodology()));
            }

            /* if(!StringUtils.isNotBlank(getRequest().getParameter("regions"))) {
                this.project.setRegions(null);
            }
            if(!StringUtils.isNotBlank(getRequest().getParameter("areas"))) {
                this.project.setAreas(null);
            }
            */
            LOGGER.info("Checking Region in Action-->"+project.getRegions());
            LOGGER.info("Checking Area in Action-->"+project.getAreas());
            LOGGER.info("Checking End Markets in Action-->"+project.getEndMarkets());

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

        if(this.project.getProjectID() == null){
            this.project = new MultiMarketProject();
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

        String result = SUCCESS;

        project.setMultiMarket(true);

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
            //project.setStatus(new Long(SynchroGlobal.Status.PIT_OPEN.ordinal()));
            if(draft!=null && draft.equalsIgnoreCase("draft"))
            {
                project.setStatus(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
                result = INPUT;
            }
            //else if(draftLocation!=null && !draftLocation.equals(""))
            else if(draftLocation!=null && !draftLocation.equals("") && draftLocation.contains("/"))
            {
                project.setStatus(new Long(SynchroGlobal.Status.DRAFT.ordinal()));
            }
            else
            {
                project.setStatus(new Long(SynchroGlobal.Status.PIT_OPEN.ordinal()));
            }
            Project projectUpdated = synchroProjectManager.save(project.toProjectbean(project));
            project.setProjectID(projectUpdated.getProjectID());
            //To save end market related details
            result = saveEndMarketDetails();

            //To save funding details getFundingInvestmentBean
            List<FundingInvestment> investments = synchroProjectManager.setProjectInvestments(getFundingInvestmentBean(), projectUpdated.getProjectID());

            projectID = project.getProjectID();

            //Audit Logs
            if(draft!=null && draft.equalsIgnoreCase("draft"))
            {
             
                SynchroLogUtils.addLog(project, SynchroGlobal.LogActivity.PIT_DRAFT, SynchroGlobal.Activity.ADD.getId(), getText("logger.project.draft"));
            }
            else if(draftLocation!=null && !draftLocation.equals("") && draftLocation.contains("/"))
            {
            	SynchroLogUtils.addLog(project, SynchroGlobal.LogActivity.PIT_CREATE, SynchroGlobal.Activity.EDIT.getId(), getText("logger.project.draft"));
            }
            else
            {
                SynchroLogUtils.addLog(project, SynchroGlobal.LogActivity.PIT_CREATE, SynchroGlobal.Activity.ADD.getId(), getText("logger.project.create"));
            }
            
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

        // if(draftLocation!=null && !draftLocation.equals(""))
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


    //Save End Market Details
    private String saveEndMarketDetails() {
        final Long userID = getUser().getID();

        // The Existing end market needs to be deleted for SAVE a Draft
        synchroProjectManager.deleteEndMarketDetail(project.getProjectID());

        if(project.getProjectID()!=null && project.getEndMarkets() != null){
            for(Long endMarketID : project.getEndMarkets()){
                final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
                /*endMarketInvestmentDetail.setInitialCost(formatDate(project.getInitialCost().get(0)));
                if(project.getInitialCostCurrency()!=null)
                {
                    endMarketInvestmentDetail.setInitialCostCurrency(project.getInitialCostCurrency().get(0));
                }
                endMarketInvestmentDetail.setSpiContact(project.getSpiContact().get(0));*/
                endMarketInvestmentDetail.setCreationBy(userID);
                endMarketInvestmentDetail.setModifiedBy(userID);
                synchroProjectManager.saveEndMarketDetail(endMarketInvestmentDetail);

                /**Updating status for all endmarkets added to this project*/
                synchroProjectManager.setAllEndMarketStatus(project.getProjectID(), SynchroGlobal.ProjectActivationStatus.OPEN.ordinal());
            }
        }
        return SUCCESS;
    }

    //Save Funding and Investment Details
    private String saveInvestmentDetails() {

        return SUCCESS;
    }

    private BigDecimal formatDate(String date)
    {
        date = date.replaceAll(",", "");
        return new BigDecimal(date);

    }

    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

    public Long getProjectID() {
        return projectID;
    }


    public void setProjectID(Long projectID) {
        this.projectID = projectID;
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


    public MultiMarketProject getProject() {
        return project;
    }


    public void setProject(MultiMarketProject project) {
        this.project = project;
    }

    public PermissionManager getPermissionManager() {
        if(permissionManager == null){
            permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }


    private void sendEmailNotifications(Project project)
    {
        try{
            String projectURL =  SynchroUtils.getJiveURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID();
            String subject = String.format(SynchroGlobal.EmailNotification.ADD_PROJECT_OWNER.getSubject(), project.getName(),project.getProjectID());
            String body = String.format(SynchroGlobal.EmailNotification.ADD_PROJECT_OWNER.getMessageBody(),projectURL, project.getName(),project.getProjectID());

            List<Long> recipientIDs = new ArrayList<Long>();
            List<Long> projectContacts = new ArrayList<Long>();
            if(project!=null && project.getProjectOwner()>0)
            {
                recipientIDs.add(project.getProjectOwner());
            }

            int size = this.project.getProjectContact()!=null?this.project.getProjectContact().size():0;

            if(size > 0)
            {
                for(int i=0; i<size; i++)
                {
                    Long projectContact = this.project.getProjectContact().get(i);
                    if(projectContact!=null && projectContact>0 && !projectContacts.contains(projectContact))
                    {
                        projectContacts.add(projectContact);
                    }

                    Long spiContact = this.project.getSpiContact().get(i);
                    if(spiContact!=null && spiContact>0 && !recipientIDs.contains(spiContact))
                    {
                        recipientIDs.add(spiContact);
                    }
                }
            }

            StringBuilder recipients = new StringBuilder();
            //Email notification to project owner and spi contacts
            if(recipientIDs.size() > 0)
            {
                for(Long recipientID : recipientIDs)
                {
                    try
                    {
                        User user = userManager.getUser(recipientID);
                        if(user != null && EmailUtils.isValidEmailAddress(user.getEmail()))
                        {
                            if(!recipients.toString().equals(""))
                            {
                                recipients.append(",");
                            }
                            recipients.append(user.getEmail());
                        }
                    }catch(Exception e){LOGGER.error("Multimarket New Project Create Notification : Error while getting user email for user - project owner/Spis ");}
                }
            }

            EmailMessage email = stageManager.populateNotificationEmail(recipients.toString(), subject, body,"pit.project.created.htmlBody","pit.project.created.subject");
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
            emailNotBean.setEmailRecipients(recipients.toString());
            emailNotificationManager.saveDetails(emailNotBean);



            //EmailNotification recipients to project contact notification
            StringBuilder contactRecipients = new StringBuilder();
            if(projectContacts.size() > 0)
            {
                for(Long projectContact : projectContacts)
                {
                    try
                    {
                        User user = userManager.getUser(projectContact);
                        if(user != null && EmailUtils.isValidEmailAddress(user.getEmail()))
                        {
                            if(!contactRecipients.toString().equals(""))
                            {
                                contactRecipients.append(",");
                            }
                            contactRecipients.append(user.getEmail());
                        }
                    }catch(Exception e){LOGGER.error("Multimarket New Project Create Notification : Error while getting user email for user - project contacs ");}
                }
            }
            EmailMessage contactEmail = stageManager.populateNotificationEmail(contactRecipients.toString(), subject, body,"pit.project.created.contacts.htmlBody","pit.project.created.contacts.subject");
            //contactEmail.getContext().put("projectId", projectID);
            contactEmail.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
            contactEmail.getContext().put("projectName",project.getName());
            contactEmail.getContext().put ("stageUrl",projectURL);
            stageManager.sendNotification(getUser(),contactEmail);


            //Email Notification TimeStamp Storage
            emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            //emailNotBean.setEndmarketID(endMarketId);
            //	emailNotBean.setAgencyID(agencyID);
            emailNotBean.setStageID(SynchroConstants.PIT_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
            emailNotBean.setEmailDesc("Action Required | New project on Synchro ");

            emailNotBean.setEmailSubject("Action Required | New project on Synchro ");
            emailNotBean.setEmailSender(getUser().getEmail());
            emailNotBean.setEmailRecipients(contactRecipients.toString());
            emailNotificationManager.saveDetails(emailNotBean);


        } catch (Exception e) {
            // TODO Auto-generated catch block
        	LOGGER.error("Error sending email notification to Project Stakeholders" + e.getMessage());
        }

    }

    private List<FundingInvestment> getFundingInvestmentBean()
    {
        List<FundingInvestment> investments = new ArrayList<FundingInvestment>();
        int size = project.getProjectContact()!=null?project.getProjectContact().size():0;

        if(size>0)
        {
            for(int i=0; i<size; i++)
            {
                FundingInvestment investment = new FundingInvestment();
                investment.setProjectID(project.getProjectID());
                investment.setAboveMarket(SynchroUtils.isAboveMarket(project.getInvestmentType().get(i).intValue()));
                investment.setInvestmentType(project.getInvestmentType().get(i));
                investment.setInvestmentTypeID(project.getInvestmentTypeID().get(i));
                investment.setFieldworkMarketID(project.getFieldworkMarketID().get(i));
                investment.setFundingMarketID(project.getFundingMarketID().get(i));
                investment.setProjectContact(project.getProjectContact().get(i));
                investment.setSpiContact(project.getSpiContact().get(i));
                if(project.getInitialCost().get(i) != null && !project.getInitialCost().get(i).equals("")) {
                    investment.setEstimatedCost(SynchroUtils.formatDate(project.getInitialCost().get(i)));
                }
                investment.setEstimatedCostCurrency(project.getInitialCostCurrency().get(i));
                if(project.getApproved().get(i)!=null && project.getApproved().get(i)==1)
                {
                    investment.setApproved(true);
                }
                else
                {
                    investment.setApproved(false);
                }

                if(project.getApprovalStatus().get(i)!=null)
                {
                    if(project.getApprovalStatus().get(i)==1)
                    {
                        investment.setApprovalStatus(true);
                    }
                    else if(project.getApprovalStatus().get(i)==0)
                    {
                        investment.setApprovalStatus(false);
                    }
                }
                else
                {
                    investment.setApprovalStatus(null);
                }
                LOGGER.info("INVESTMENT ID --- " + investment.getInvestmentID());
                LOGGER.debug("INVESTMENT ID --- " + investment.getInvestmentID());
                //Add to array
                investments.add(investment);
            }
        }
        LOGGER.info("INVESTMENTS SIZE  --- " + investments.size());
        LOGGER.debug("INVESTMENTS SIZE  --- " + investments.size());
        return investments;
    }

    public String getDraft() {
        return draft;
    }

    public void setDraft(String draft) {
        this.draft = draft;
    }

    public List<Long> getEndMarketIds() {
        return endMarketIds;
    }

    public void setEndMarketIds(List<Long> endMarketIds) {
        this.endMarketIds = endMarketIds;
    }

    public List<FundingInvestment> getFundingInvestments() {
        return fundingInvestments;
    }

    public void setFundingInvestments(List<FundingInvestment> fundingInvestments) {
        this.fundingInvestments = fundingInvestments;
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
