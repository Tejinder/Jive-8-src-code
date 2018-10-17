package com.grail.kantar.action;

import com.grail.GrailGlobals;
import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.manager.KantarBriefTemplateManager;
import com.grail.kantar.util.KantarGlobals;
import com.grail.kantar.util.KantarMakeRequestUtils;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.BATGlobal;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
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
 * Date: 10/16/14
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarBriefTemplateAction extends JiveActionSupport implements Preparable {
    private EmailManager emailManager;
    private ProfileManager profileManager;
    private KantarBriefTemplateManager kantarBriefTemplateManager;
    private KantarBriefTemplate kantarBriefTemplate;
    private Long id;
    private Long  defaultCurrency;
    private boolean canEditProject = false;
    private boolean isNewProject = false;
    private static final String CREATION_SUCCESS = "createSuccess";
    private Map<String, String> errorMap = new HashMap<String, String>();
    private String redirectURL = "/kantar/brief-template!input.jspa";
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
            this.kantarBriefTemplate = kantarBriefTemplateManager.get(id);
            if(kantarBriefTemplate != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
                    (kantarBriefTemplate.getBatContact() != null && kantarBriefTemplate.getBatContact().equals(getUser().getID()))
                    || (kantarBriefTemplate.getCreatedBy() != null && kantarBriefTemplate.getCreatedBy().equals(getUser().getID())))) {
                canEditProject = true;
            }
            isNewProject = false;
        } else {
            this.kantarBriefTemplate = new KantarBriefTemplate();
            isNewProject = true;
        }

        ServletRequestDataBinder binder = new ServletRequestDataBinder(this.kantarBriefTemplate);
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
            this.kantarBriefTemplate = kantarBriefTemplateManager.get(id);
            if(!SynchroPermHelper.canAccessKantarProject(kantarBriefTemplate, getUser())) {
                return UNAUTHORIZED;
            }
            if(kantarBriefTemplate != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
                    (kantarBriefTemplate.getBatContact() != null && kantarBriefTemplate.getBatContact().equals(getUser().getID()))
                    || (kantarBriefTemplate.getCreatedBy() != null && kantarBriefTemplate.getCreatedBy().equals(getUser().getID())))) {
                canEditProject = true;
            }
            isNewProject = false;
            if(kantarBriefTemplate != null && request.getParameter("validationError") != null
                    && Boolean.parseBoolean(request.getParameter("validationError"))) {
                if(kantarBriefTemplate.getFinalCost() == null) {
                    showFinalCostError = true;
                    showFinalCostCurrencyError = false;
                } else if(kantarBriefTemplate.getFinalCostCurrency() == null || kantarBriefTemplate.getFinalCostCurrency().longValue() <= 0) {
                    showFinalCostCurrencyError = true;
                    showFinalCostError = false;
                }
            }
        } else {
            if(!SynchroPermHelper.canCreateKantarProject(getUser())) {
                return UNAUTHORIZED;
            }
            this.kantarBriefTemplate = new KantarBriefTemplate();
            isNewProject = true;
        }

        return INPUT;
    }

    @Override
    public String execute() {
        String result = SUCCESS;

        if(kantarBriefTemplate.getDraft()) {
            result = NONE;
        } else {
            if(id != null && id > 0) {
                errorMap = new HashMap<String, String>();
                redirectURL = redirectURL + "?id="+id;
                if(kantarBriefTemplate != null && kantarBriefTemplate.getId() != null && !kantarBriefTemplate.getDraft()) {
                    if(kantarBriefTemplate.getFinalCost() == null || kantarBriefTemplate.getFinalCostCurrency() == null
                            || kantarBriefTemplate.getFinalCostCurrency().longValue() <= 0) {
                        redirectURL += "&validationError=true";
                    }
                }
                result = SUCCESS;
            } else {
                result  = CREATION_SUCCESS;
            }
        }

        if(canEditProject || isNewProject) {
            User user = getUser();
            Date date = new Date();
            String recipient = JiveGlobals.getJiveProperty("kantar.portal.email.notification.recipient", "assistance@batinsights.com");

            String deliveryDate = request.getParameter("deliveryDate");
            if(deliveryDate != null && !deliveryDate.equals("")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    this.kantarBriefTemplate.setDeliveryDate(dateFormat.parse(deliveryDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if(kantarBriefTemplate != null) {
                kantarBriefTemplate.setSender(user.getID());
                kantarBriefTemplate.setCapturedDate(new Date());
                kantarBriefTemplate.setRecipientEmail(recipient);
                kantarBriefTemplate.setModifiedBy(getUser().getID());
                kantarBriefTemplate.setModificationDate(date);
                if(kantarBriefTemplate.getCreatedBy() == null || kantarBriefTemplate.getCreatedBy().intValue() == 0) {
                    kantarBriefTemplate.setCreatedBy(getUser().getID());
                }
                if(kantarBriefTemplate.getCreationDate() == null) {
                    kantarBriefTemplate.setCreationDate(date);
                }
                if(kantarBriefTemplate.getStatus() == null || kantarBriefTemplate.getStatus().intValue() == 0) {
                    kantarBriefTemplate.setStatus(KantarGlobals.KantarBriefTemplateStatusType.IN_PROGRESS.getId());
                }
                if(!kantarBriefTemplate.getDraft()) {
                    KantarMakeRequestUtils.processClicks(ServletActionContext.getServletContext(), 1, 0, 0);
                }


                id = getKantarBriefTemplateManager().save(kantarBriefTemplate);
                if(id != null && id > 0 && !kantarBriefTemplate.getDraft()) {
                    kantarBriefTemplate.setId(id);



                    if(isNewProject && recipient != null && !recipient.equals("")) {

                        EmailMessage message = new EmailMessage();

                        Map<String, Object> messageContext = message.getContext();

                        Map<String, Object> kantarBriefTemplateMap = KantarGlobals.toKantarBriefTemplateMap(kantarBriefTemplate, user);
                        messageContext.putAll(kantarBriefTemplateMap);

                        messageContext.put("portalType", BATGlobal.PortalType.KANTAR.toString());

                        message.addRecipient("Kantar", recipient);
                        message.setHtmlBodyProperty("kantar.portal.brief.template.notification.htmlBody");
                        message.setHtmlBody("kantar.portal.brief.template.notification.htmlBody");
                        message.setTextBodyProperty("kantar.portal.brief.template.notification.textBody");
                        message.setTextBody("kantar.portal.brief.template.notification.textBody");
                        message.setSubjectProperty("kantar.portal.brief.template.notification.subject");
                        message.setLocale(JiveGlobals.getLocale());
                        message.setSender(kantarBriefTemplateMap.get("userName").toString(), kantarBriefTemplateMap.get("userEmail").toString());

                        emailManager.send(message);
                    }
                }

            }
        }
        
        if(isNewProject)
        {
        	SynchroLogUtils.addLog(SynchroGlobal.PortalType.KANTAR.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.ADD.getId(), 
					0, "Brief Template Creation", "", -1L, getUser().getID());
        }
        else
        {
        	SynchroLogUtils.addLog(SynchroGlobal.PortalType.KANTAR.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
					0, "Brief Template Updated", "", -1L, getUser().getID());
        }
        return result;
    }

//    public void validateForm() {
//        if(kantarBriefTemplate != null && kantarBriefTemplate.getId() != null && !kantarBriefTemplate.getDraft()) {
//            errorMap = new HashMap<String, String>();
//            if(kantarBriefTemplate.getFinalCost() == null) {
//                errorMap.put("finalCost", "Please enter final cost.");
//            }
//
//            if(kantarBriefTemplate.getFinalCostCurrency() == null || kantarBriefTemplate.getFinalCostCurrency() <= 0) {
//                errorMap.put("finalCostCurrency", "Please select final cost currency.");
//            }
//        }
//    }

//    @Override
//    public void validate() {
//        if(kantarBriefTemplate != null && kantarBriefTemplate.getId() != null && !kantarBriefTemplate.getDraft()) {
//            if(kantarBriefTemplate.getFinalCost() == null) {
//                addFieldError("finalCost", "Please enter final cost.");
//            }
//
//            if(kantarBriefTemplate.getFinalCostCurrency() == null || kantarBriefTemplate.getFinalCostCurrency() <= 0) {
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


    public KantarBriefTemplate getKantarBriefTemplate() {
        return kantarBriefTemplate;
    }

    public void setKantarBriefTemplate(KantarBriefTemplate kantarBriefTemplate) {
        this.kantarBriefTemplate = kantarBriefTemplate;
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

    public KantarBriefTemplateManager getKantarBriefTemplateManager() {
        return kantarBriefTemplateManager;
    }

    public void setKantarBriefTemplateManager(KantarBriefTemplateManager kantarBriefTemplateManager) {
        this.kantarBriefTemplateManager = kantarBriefTemplateManager;
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

