/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;

import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Captcha;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.util.AlwaysAllowAnonymous;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.user.registration.UserAccountFormValidator;
import com.jivesoftware.community.user.registration.rest.HumanValidationField;
import com.jivesoftware.community.user.registration.rest.UserRegistration;
import com.jivesoftware.community.user.registration.rest.UserRegistrationService;
import com.jivesoftware.community.web.soy.SoyModelDriven;
import com.jivesoftware.community.web.struts.SetReferer;
import com.jivesoftware.community.web.view.AbstractPageTemplateModel;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Action to create the user profile when the user object is created.
 */
@AlwaysAllowAnonymous
@SetReferer(false)
public class CreateNewUserAccountAction extends JiveActionSupport implements SoyModelDriven {

    protected static final Logger log = LogManager.getLogger(CreateNewUserAccountAction.class.getName());

    public static final String UNAVAILABLE = "unavailable";

    private UserRegistrationService userRegistrationService;
    private Captcha captcha;
    private UserRegistrationFormView userRegistrationFormView;

    private String username;
    private String email;
    private String validationKey;
    private String valueCase;
    private String invitationID;

    public void setUserRegistrationService(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    public void setCaptcha(Captcha captcha) {
        this.captcha = captcha;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setValidationKey(String validationKey) {
        this.validationKey = validationKey;
    }

    public void setValueCase(String valueCase) {
        this.valueCase = valueCase;
    }

    public void setInvitationID(String invitationID) {
        this.invitationID = invitationID;
    }

    @Override
    // overridden to not throw exception
    public String execute() {

        //if user is already authenticated take them into the app
        User user = getUser();
        if (!(user.isAnonymous())) {
            return LOGIN;
        }

        //token validation if fail redirect to validateaction

        userRegistrationFormView = new UserRegistrationFormView();

        try {
            UserRegistration userRegistration = userRegistrationService.get(false);
            if (StringUtils.isNotBlank(username)) {
                userRegistration.getFieldByName(UserAccountFormValidator.FIELD_USERNAME).setVal(username);
            }
            if (StringUtils.isNotBlank(email)) {
                userRegistration.getFieldByName(UserAccountFormValidator.FIELD_EMAIL).setVal(email);
                if (registrationManager.isUsernameIsEmail()) {
                    userRegistration.getFieldByName(UserAccountFormValidator.FIELD_USERNAME).setVal(email);
                }
            }

            userRegistration.setValidationKey(getRequest().getParameter("token"));
            if (!isBlank(valueCase)) {
                userRegistration.setValueCase(valueCase);
            }

            if (!isBlank(invitationID)) {
                userRegistration.setInvitationID(invitationID);
            }

            HumanValidationField humanValidationField = (HumanValidationField) userRegistration.getFieldByName(UserAccountFormValidator.FIELD_HUMAN_VALIDATION);
            if (humanValidationField != null) {
                humanValidationField.getProp().put(HumanValidationField.PROP_IMAGE_URL, captcha.setup(StringUtils.firstNonBlank(username, email)));
            }

            userRegistrationFormView.setForm(userRegistration);
            userRegistrationFormView.setSuccessURL(getSuccessURL());
            userRegistrationFormView.setForceSecure(isForceSecure());
        } catch (UnauthorizedException e) {
            log.warn(e.getMessage());
            return UNAUTHORIZED;
        } catch (UnsupportedOperationException e) {
            userRegistrationFormView.setAdminEmailAddress(getAdminEmail());
            log.warn(e.getMessage(),e);
            return UNAVAILABLE;
        } catch (WebApplicationException e) {
            log.error(e.getMessage(),e);
            return ERROR;
        }

        return SUCCESS;
    }

    private String getAdminEmail() {
        String adminEmail = JiveGlobals.getJiveProperty(EmailManager.ADMINEMAIL);
        if (adminEmail == null) {
            log.error("system.adminuser.email property could not be found ");
        }
        return adminEmail;
    }

    private boolean isForceSecure() {
        return JiveGlobals.getJiveBooleanProperty("jive.auth.forceSecure", false);
    }

    private String getSuccessURL() {
        return getRedirectURL();
    }

    @Override
    public UserRegistrationFormView getModel() {
        return userRegistrationFormView;
    }

    public static class UserRegistrationFormView extends AbstractPageTemplateModel {

        private boolean forceSecure;
        private boolean preview;
        private UserRegistration form;
        private String adminEmailAddress;
        private String successURL;

        public UserRegistration getForm() {
            return form;
        }

        public void setForm(UserRegistration form) {
            this.form = form;
        }

        public String getAdminEmailAddress() {
            return adminEmailAddress;
        }

        public void setAdminEmailAddress(String adminEmailAddress) {
            this.adminEmailAddress = adminEmailAddress;
        }

        public String getSuccessURL() {
            return successURL;
        }

        public void setSuccessURL(String successURL) {
            this.successURL = successURL;
        }

        public boolean isPreview() {
            return preview;
        }

        public void setPreview(boolean preview) {
            this.preview = preview;
        }

        public void setForceSecure(boolean forceSecure) {
            this.forceSecure = forceSecure;
        }

        public boolean isForceSecure() {
            return forceSecure;
        }
    }
}
