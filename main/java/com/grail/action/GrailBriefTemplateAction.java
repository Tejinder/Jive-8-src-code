package com.grail.action;

import com.grail.GrailGlobals;
import com.grail.beans.GrailBriefTemplate;
import com.grail.manager.GrailBriefTemplateManager;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.BATGlobal;
import com.grail.util.GrailMakeRequestUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;
import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailBriefTemplateAction extends JiveActionSupport implements Preparable {
    private EmailManager emailManager;
    private ProfileManager profileManager;
    private GrailBriefTemplateManager grailBriefTemplateManager;
    private GrailBriefTemplate grailBriefTemplate;
    private Long id;
    private Long  defaultCurrency;
    private boolean canEditProject = false;
    private boolean isNewProject = false;
    private static final String CREATION_SUCCESS = "createSuccess";
    private Map<String, String> errorMap = new HashMap<String, String>();
    private String redirectURL = "/grail/brief-template!input.jspa";
    private boolean showFinalCostError = false;
    private boolean showFinalCostCurrencyError = false;


    @Override
    public void prepare() throws Exception {
        if(getUser() != null) {
            defaultCurrency = findDefaultCurrency(getUser());
        }
        errorMap = new HashMap<String, String>();
        canEditProject = false;
        isNewProject = false;

        if(request.getParameter("id") != null && !request.getParameter("id").equals("")) {
            id = Long.parseLong(request.getParameter("id"));
        }

        if(id != null && id > 0) {
            this.grailBriefTemplate = grailBriefTemplateManager.get(id);
            if(grailBriefTemplate != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
                    (grailBriefTemplate.getBatContact() != null && grailBriefTemplate.getBatContact().equals(getUser().getID()))
                            || (grailBriefTemplate.getCreatedBy() != null && grailBriefTemplate.getCreatedBy().equals(getUser().getID())))) {
                canEditProject = true;

            }
            isNewProject = false;
        } else {
            this.grailBriefTemplate = new GrailBriefTemplate();
            isNewProject = true;
        }

        ServletRequestDataBinder binder = new ServletRequestDataBinder(this.grailBriefTemplate);
        binder.bind(getRequest());

    }

    @Override
    public String input() {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }
        errorMap = new HashMap<String, String>();

        canEditProject = false;
        isNewProject = false;
        showFinalCostError = false;
        showFinalCostCurrencyError = false;

        if(request.getParameter("id") != null && !request.getParameter("id").equals("")) {
            id = Long.parseLong(request.getParameter("id"));
        }
        if(id != null && id > 0) {
            this.grailBriefTemplate = grailBriefTemplateManager.get(id);
            if(!SynchroPermHelper.canAccessGrailProject(grailBriefTemplate, getUser())) {
                return UNAUTHORIZED;
            }
            if(grailBriefTemplate != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
                    (grailBriefTemplate.getBatContact() != null && grailBriefTemplate.getBatContact().equals(getUser().getID()))
                            || (grailBriefTemplate.getCreatedBy() != null && grailBriefTemplate.getCreatedBy().equals(getUser().getID())))) {
                canEditProject = true;
            }
            isNewProject = false;
            if(grailBriefTemplate != null && request.getParameter("validationError") != null
                    && Boolean.parseBoolean(request.getParameter("validationError"))) {
                if(grailBriefTemplate.getFinalCost() == null) {
                    showFinalCostError = true;
                    showFinalCostCurrencyError = false;
                } else if(grailBriefTemplate.getFinalCostCurrency() == null || grailBriefTemplate.getFinalCostCurrency().longValue() <= 0) {
                    showFinalCostCurrencyError = true;
                    showFinalCostError = false;
                }
            }
        } else {
            if(!SynchroPermHelper.canCreateGrailProject(getUser())) {
                return UNAUTHORIZED;
            }
            this.grailBriefTemplate = new GrailBriefTemplate();
            isNewProject = true;
        }

        return INPUT;
    }

    @Override
    public String execute() {
        String result = SUCCESS;

        if(grailBriefTemplate.getDraft()) {
            result = NONE;
        } else {
            if(id != null && id > 0) {
                errorMap = new HashMap<String, String>();
                redirectURL = redirectURL + "?id="+id;
                if(grailBriefTemplate != null && grailBriefTemplate.getId() != null && !grailBriefTemplate.getDraft()) {
                    if(grailBriefTemplate.getFinalCost() == null || grailBriefTemplate.getFinalCostCurrency() == null
                            || grailBriefTemplate.getFinalCostCurrency().longValue() <= 0) {
                        redirectURL += "&validationError=true";
                    }
                }
                result = SUCCESS;
            } else {
                result  = CREATION_SUCCESS;
            }
        }

        //Audit logs
        /*if(isNewProject)
        {
        	SynchroLogUtils.addLog(SynchroGlobal.PortalType.KANTAR.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.ADD.getId(), 
					0, "Brief Template Creation", "", -1L, getUser().getID());	
        }
        else
        {
        	SynchroLogUtils.addLog(SynchroGlobal.PortalType.KANTAR.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
					0, "Brief Template Updated", "", -1L, getUser().getID());
        }
    	*/
    	
        if(canEditProject || isNewProject) {
            User user = getUser();
            Date date = new Date();
            String recipient = JiveGlobals.getJiveProperty("grail.portal.email.notification.recipient", "assistance@batinsights.com");

            String deliveryDate = request.getParameter("deliveryDate");
            if(deliveryDate != null && !deliveryDate.equals("")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    this.grailBriefTemplate.setDeliveryDate(dateFormat.parse(deliveryDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if(grailBriefTemplate != null) {
                grailBriefTemplate.setSender(user.getID());
                grailBriefTemplate.setCapturedDate(new Date());
                grailBriefTemplate.setRecipientEmail(recipient);
                grailBriefTemplate.setModifiedBy(getUser().getID());
                grailBriefTemplate.setModificationDate(date);
                if(grailBriefTemplate.getCreatedBy() == null || grailBriefTemplate.getCreatedBy().intValue() == 0) {
                    grailBriefTemplate.setCreatedBy(getUser().getID());
                }
                if(grailBriefTemplate.getCreationDate() == null) {
                    grailBriefTemplate.setCreationDate(date);
                }
                if(grailBriefTemplate.getStatus() == null || grailBriefTemplate.getStatus().intValue() == 0) {
                    grailBriefTemplate.setStatus(GrailGlobals.GrailBriefTemplateStatusType.IN_PROGRESS.getId());
                }
                if(!grailBriefTemplate.getDraft()) {
                    GrailMakeRequestUtils.processClicks(ServletActionContext.getServletContext(), 1, 0, 0);
                }


                id = getGrailBriefTemplateManager().save(grailBriefTemplate);
                if(id != null && id > 0 && !grailBriefTemplate.getDraft()) {
                    grailBriefTemplate.setId(id);



                    if(isNewProject && recipient != null && !recipient.equals("")) {

                        EmailMessage message = new EmailMessage();

                        Map<String, Object> messageContext = message.getContext();

                        Map<String, Object> grailBriefTemplateMap = GrailGlobals.toGrailBriefTemplateMap(grailBriefTemplate, user);
                        messageContext.putAll(grailBriefTemplateMap);

                        messageContext.put("portalType", BATGlobal.PortalType.GRAIL.toString());

                        message.addRecipient("Grail", recipient);
                        message.setHtmlBodyProperty("grail.portal.brief.template.notification.htmlBody");
                        message.setHtmlBody("grail.portal.brief.template.notification.htmlBody");
                        message.setTextBodyProperty("grail.portal.brief.template.notification.textBody");
                        message.setTextBody("grail.portal.brief.template.notification.textBody");
                        message.setSubjectProperty("grail.portal.brief.template.notification.subject");
                        message.setLocale(JiveGlobals.getLocale());
                        message.setSender(grailBriefTemplateMap.get("userName").toString(), grailBriefTemplateMap.get("userEmail").toString());

                        emailManager.send(message);
                    }
                }

            }
        }
        
        if(isNewProject)
        {
        	SynchroLogUtils.addLog(SynchroGlobal.PortalType.GRAIL.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.ADD.getId(), 
					0, "Brief Template Creation", "", -1L, getUser().getID());
        }
        else
        {
        	SynchroLogUtils.addLog(SynchroGlobal.PortalType.GRAIL.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
					0, "Brief Template Updated", "", -1L, getUser().getID());
        }
        return result;
    }

//    public void validateForm() {
//        if(grailBriefTemplate != null && grailBriefTemplate.getId() != null && !grailBriefTemplate.getDraft()) {
//            errorMap = new HashMap<String, String>();
//            if(grailBriefTemplate.getFinalCost() == null) {
//                errorMap.put("finalCost", "Please enter final cost.");
//            }
//
//            if(grailBriefTemplate.getFinalCostCurrency() == null || grailBriefTemplate.getFinalCostCurrency() <= 0) {
//                errorMap.put("finalCostCurrency", "Please select final cost currency.");
//            }
//        }
//    }

//    @Override
//    public void validate() {
//        if(grailBriefTemplate != null && grailBriefTemplate.getId() != null && !grailBriefTemplate.getDraft()) {
//            if(grailBriefTemplate.getFinalCost() == null) {
//                addFieldError("finalCost", "Please enter final cost.");
//            }
//
//            if(grailBriefTemplate.getFinalCostCurrency() == null || grailBriefTemplate.getFinalCostCurrency() <= 0) {
//                addFieldError("finalCostCurrency", "Please select final cost currency.");
//            }
//        }
//    }

    public Long findDefaultCurrency(final User user) {
        Integer defaultCountry = SynchroUtils.getCountryByUser(user);
        Map<String, String> countryCurrencyMap = SynchroGlobal.getCountryCurrencyMap();
        Long currencyId = -1L;
        if(defaultCountry != null && defaultCountry > 0 && countryCurrencyMap.containsKey(defaultCountry.toString())) {
            try{
                currencyId = Long.parseLong(countryCurrencyMap.get(defaultCountry.toString()));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return currencyId;
    }


    public EmailManager getEmailManager() {
        return emailManager;
    }

    public void setEmailManager(EmailManager emailManager) {
        this.emailManager = emailManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public GrailBriefTemplateManager getGrailBriefTemplateManager() {
        return grailBriefTemplateManager;
    }

    public void setGrailBriefTemplateManager(GrailBriefTemplateManager grailBriefTemplateManager) {
        this.grailBriefTemplateManager = grailBriefTemplateManager;
    }

    public GrailBriefTemplate getGrailBriefTemplate() {
        return grailBriefTemplate;
    }

    public void setGrailBriefTemplate(GrailBriefTemplate grailBriefTemplate) {
        this.grailBriefTemplate = grailBriefTemplate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(Long defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isCanEditProject() {
        return canEditProject;
    }

    public void setCanEditProject(boolean canEditProject) {
        this.canEditProject = canEditProject;
    }

    public boolean isNewProject() {
        return isNewProject;
    }

    public void setNewProject(boolean newProject) {
        isNewProject = newProject;
    }

    public Map<String, String> getErrorMap() {
        return errorMap;
    }

    public void setErrorMap(Map<String, String> errorMap) {
        this.errorMap = errorMap;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public boolean isShowFinalCostError() {
        return showFinalCostError;
    }

    public void setShowFinalCostError(boolean showFinalCostError) {
        this.showFinalCostError = showFinalCostError;
    }

    public boolean isShowFinalCostCurrencyError() {
        return showFinalCostCurrencyError;
    }

    public void setShowFinalCostCurrencyError(boolean showFinalCostCurrencyError) {
        this.showFinalCostCurrencyError = showFinalCostCurrencyError;
    }
}
