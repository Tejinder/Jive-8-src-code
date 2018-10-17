/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;


import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.util.BATConstants;
import com.grail.util.URLUtils;
import com.jivesoftware.base.EmailAlreadyExistsException;
import com.jivesoftware.base.ldap.LdapConfiguration;
import com.jivesoftware.community.Captcha;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.RefererHelper;
import com.jivesoftware.community.aaa.AccessManager;
import com.jivesoftware.community.aaa.LoginThrottle;
import com.jivesoftware.community.aaa.authz.FormAuthenticationRedirectStrategy;
import com.jivesoftware.community.aaa.sso.external.login.ExternalLogin;
import com.jivesoftware.community.aaa.sso.external.login.ExternalLoginConfiguration;
import com.jivesoftware.community.aaa.sso.external.login.ExternalLoginManager;
import com.jivesoftware.community.aaa.sso.facebook.FacebookConfiguration;
import com.jivesoftware.community.aaa.sso.kerberos.KerberosConfiguration;
import com.jivesoftware.community.aaa.sso.saml.SAMLConfiguration;
import com.jivesoftware.community.action.util.ActionUtils;
import com.jivesoftware.community.action.util.AlwaysAllowAnonymous;
import com.jivesoftware.community.action.util.CookieUtils;
import com.jivesoftware.community.impl.RegistrationConfiguration;
import com.jivesoftware.community.mail.util.EmailValidationHelper;
import com.jivesoftware.community.solution.annotations.InjectConfiguration;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.community.web.struts.SetReferer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import com.jivesoftware.community.lifecycle.JiveApplication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import java.util.*;

import static com.jivesoftware.community.aaa.sso.SsoConstants.LOGIN_TYPE_COOKIE;
import static com.jivesoftware.community.aaa.sso.SsoConstants.LOGIN_TYPE_SAML;
import static com.jivesoftware.util.StringUtils.isEmpty;
import static com.jivesoftware.util.StringUtils.isNotBlank;
import static com.jivesoftware.util.StringUtils.isValidEmailAddress;

/**
 * <p/>
 * Unlike other Actions that have declared views, the views for this action largely depend on the context it's being
 * executed from. For that reason, this Action expects to be told where to redirect to after 1) a successful login and
 * 2) when the user cancels the login. These parameters can be set as properties of this action (parameters viewed with
 * respect to the web).
 * <p>
 * <p/>
 * <p/>
 * As of 2.0 this action does not handle any actual authentication, it's main role is to properly redirect.
 */
@AlwaysAllowAnonymous
@SetReferer(false)
public class LoginAction extends JiveActionSupport {

    private static final Logger log = LogManager.getLogger(LoginAction.class);

    private ExternalLoginConfiguration externalLoginConfiguration;
    private FacebookConfiguration facebookConfiguration;
    private ExternalLoginManager externalLoginManager;
    private KerberosConfiguration kerberosConfiguration;
    private EmailValidationHelper emailValidationHelper;
    private FormAuthenticationRedirectStrategy formAuthenticationRedirectStrategy;
    private SAMLConfiguration samlConfiguration;
    private AccessManager accessManager;
    private LdapConfiguration ldapConfiguration;
    private RegistrationConfiguration registrationConfiguration;
    private RefererHelper refererHelper;
    private static PermissionManager permissionManager;

    public void setEmailValidationHelper(EmailValidationHelper emailValidationHelper) {
        this.emailValidationHelper = emailValidationHelper;
    }

    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    // Parameters //
    protected String successURL;

    protected boolean serviceUnavailable = false;
    protected boolean loginBanned = false;
    protected boolean authzFailed = false;
    protected boolean authnFailed = false;
    protected boolean invalidCaptcha = false;
    protected boolean accountDisabled = false;
    protected boolean approvalRequired = false;
    protected boolean approvalRejected = false;
    protected boolean accountExists = false;
    protected boolean licenseExceeded = false;
    protected boolean loginThrottled = false;
    protected boolean registerOnly = false;
    protected boolean partnersDisabled = false;
    protected boolean nonUniqueCredentials = false;
    protected boolean ssoLoginBypassCookie = false;

    protected String emailAddress;
    protected boolean validationSent;

    private LoginThrottle loginThrottle;

    private Captcha captcha;
    private String captchaImageUrl;

    @Override
    public String input() {
        return INPUT;
    }

    @Override
    public String execute() {

       //populateSuccessURL();
        HttpSession httpSession = request.getSession();
        if(httpSession.getAttribute(BATConstants.BAT_DISCLAIMER_ACCEPTED) != null) {
            httpSession.removeAttribute(BATConstants.BAT_DISCLAIMER_ACCEPTED);
        }

        if(httpSession.getAttribute(BATConstants.GRAIL_PORTAL_TYPE) != null) {
            httpSession.removeAttribute(BATConstants.GRAIL_PORTAL_TYPE);
        }

        if(httpSession.getAttribute(BATConstants.GRAIL_REFERRER_URL) != null) {
            httpSession.removeAttribute(BATConstants.GRAIL_REFERRER_URL);
        }

        if(SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL) == null
                || SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL).equals("")) {
            SynchroGlobal.getAppProperties().put(BATConstants.BAT_BASE_URL, URLUtils.getBaseURL(request));
        }
        
        // logged in users should be redirected, unless they were directed here because they lacked authorization
        if (!authProvider.getAuthentication().isAnonymous() && !authzFailed) {
        	// populateSuccessURL is added over here as it will redirect to login page always if the user is not loggedin. 
        	populateSuccessURL();
        	try {
                String url = "/";
                if (isNotBlank(successURL) && !successURL.contains("sso-confirm")) {
                    
                    if (successURL.contains("/index.jspa")) {
                        successURL = generateRedirectURL();
                    } 
                    url = successURL;
                }
                else
                { 
                	successURL = generateRedirectURL();
                	url = successURL;
                }

                formAuthenticationRedirectStrategy.sendRedirect(getRequest(), getResponse(), url);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            return NONE;
        }

        // for kerberos, we want to set the status of the page to unauthorized with an authentication challenge
        if (kerberosConfiguration.isEnabled()
                && authProvider.getAuthentication().isAnonymous()
                && isNotAfterLogout()) {
            if (receivedNtlmNegotiate() ) {
                // this is an NTLM type 2 message to convince IE to post form data after starting NTLM auth
                response.addHeader("Authenticate", "Negotiate TlRMTVNTUAAwMTIzNDU2Nzg5YWJjZGVm");
            } else {
                response.addHeader("WWW-Authenticate", "Negotiate");
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return UNAUTHENTICATED;
        }

        if (sharedGroupManager.isEnabled()) {
            Cookie loginTypeCookie = CookieUtils.getCookie(request, LOGIN_TYPE_COOKIE);
            if (loginTypeCookie != null && LOGIN_TYPE_SAML.equals(loginTypeCookie.getValue())) {
                ssoLoginBypassCookie = true;
            }
        }

        if (serviceUnavailable) {
            addActionError(getText("login.err.serviceUnavailable.text"));
            return UNAUTHENTICATED;
        }
        if (loginBanned) {
            addActionError(getText("login.err.banned_login.text"));
            return UNAUTHENTICATED;
        }
        if (nonUniqueCredentials) {
            addActionError(getText("login.err.nonUniqueCredentials.text"));
            return UNAUTHENTICATED;
        }

        if (authzFailed) {
            if (authProvider.getAuthentication().isAnonymous()) {
                addActionError(getText("login.wrn.notAuthToViewCnt.info"));
                return UNAUTHENTICATED;
            }
            else {
                return UNAUTHORIZED;
            }
        }

        if (authnFailed || loginThrottled) {
            if (authProvider.getAuthentication().isAnonymous()) {
                setUpCaptchaIfNeeded();
                if (loginThrottled) {
                    addActionError(getText("login.err.exceedLoginAttemptLimit.text", "", "" + getLoginDelay()));
                    return UNAUTHENTICATED;
                }

                //if one of the sso authentication filters bumped us back out to the login page we want to show the appropriate error
                if (request.getParameter("error") != null) {
                    addActionError(getText(request.getParameter("error")));
                } else {
                    addActionError(getText("login.err.invalid_login.text"));
                }

                return UNAUTHENTICATED;
            }
            else {
                // redirect to previous page (or index) if the user is already logged in
                try {
                    formAuthenticationRedirectStrategy.sendRedirect(getRequest(), getResponse(), "/");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return NONE;
            }
        }

        if (accountDisabled) {
            addActionError(getText("login.err.account_disabled.text"));
            return UNAUTHENTICATED;
        }

        if (approvalRequired) {
            return UNAUTHENTICATED;
        }

        if (approvalRejected) {
            addActionError(getText("login.err.acctRejected.text"));
            return UNAUTHENTICATED;
        }

        if (accountExists) {
            return UNAUTHENTICATED;
        }

        if (licenseExceeded) {
            addActionError(getText("login.err.exceedLicSeatLmt.text"));
            return UNAUTHENTICATED;
        }

        if (partnersDisabled) {
            addActionError(getText("login.err.partnersDisabled.text"));
            return UNAUTHENTICATED;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Unauthenticated access attempt for resource %s by %s.",
                    request.getRequestURI(), ActionUtils.getRemoteAddress()));
        }

        setUpCaptchaIfNeeded();

        return UNAUTHENTICATED;
    }

    private String generateRedirectURL() {
//      if(isSynchroPortalEnabled) {
//          boolean hasRKPAccess = getPermissionManager().isRKPUser(getUser());
//          boolean hasSynchroAccess = getPermissionManager().isSynchroUser(getUser());
//          if(hasRKPAccess && hasSynchroAccess) {
//              return SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL;
//          } else if(hasRKPAccess) {
//              return SynchroConstants.RKP_DISCLIAMER_URL;
//          } else if(hasSynchroAccess) {
//              return SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL;
//          }
//      }
//      return SynchroConstants.RKP_DISCLIAMER_URL;
      return BATConstants.DISCLAIMER_URL;
  }
    private void populateSuccessURL() {
        if (isNotAfterLogout()) {
            successURL = refererHelper.getRefererURL(request);
        }
    }

    private boolean isNotAfterLogout() {
        return request.getParameter("logout") == null;
    }

    private boolean receivedNtlmNegotiate() {
        if (request.getHeader("Authorization") != null) {
            log.debug("Received Authorization header " + request.getHeader("Authorization"));
            return request.getHeader("Authorization").startsWith("Negotiate TlRM");
        }
        return false;
    }

    private void setUpCaptchaIfNeeded() {
        if ((isLoginThrottled() || isInvalidCaptcha()) && captcha.isLoginCaptchaEnabled()) {
            String username = request.getParameter("username");
            if (!loginThrottle.isLoginAllowed(username) || captcha.needsCaptcha(username)) {
                setCaptchaImageUrl(captcha.setup(username));
            }
        }
    }

    public String register() {
        if (isEmpty(emailAddress)) {
            addFieldError("emailAddress", getText("register.email.required"));
            return INPUT;
        } else if (!isValidEmailAddress(emailAddress)) {
            addFieldError("emailAddress", getText("register.email.invalid"));
            return INPUT;
        } else if (!emailValidationHelper.isApprovedPlusAddress(emailAddress)) {
            addFieldError("emailAddress", getText("register.email.notcomp.plus"));
            return INPUT;
        } else if (emailValidationHelper.isBlacklistedDomainsEnabled(emailAddress) && emailValidationHelper.isBlacklistedDomain(emailAddress)) {
            addFieldError("emailAddress", getText("register.email.blacklisted"));
            return INPUT;
        } else {
            if (!emailValidationHelper.isApprovedDomain(emailAddress)) {
                List<String> args = new ArrayList<>(1);
                if (SkinUtils.isSingleDomain()) {
                    args.add(SkinUtils.getCompanyDomain());
                    addFieldError("emailAddress", getText("register.email.notcomp", args));
                } else {
                    args.add(SkinUtils.getCompanyName());
                    addFieldError("emailAddress", getText("register.email.notcomp.multi", args));
                    addFieldError("emailAddress", SkinUtils.getAllCompanyDomains());
                }
                return INPUT;
            }
        }

        try {
            emailValidationHelper.sendRegistrationValidationRequest(emailAddress, getLocale());
        } catch (EmailAlreadyExistsException e) {
            List<String> args = new ArrayList<>(1);
            args.add(emailAddress);
            addFieldError("emailAddress", getText("register.email.exists", args));
            return INPUT;
        }

        validationSent = true;
        return INPUT;
    }

    public boolean isSharedGroupEnabled() {
        return sharedGroupManager.isEnabled();
    }

    public boolean isSamlEnabled() {
        return samlConfiguration.isEnabled();
    }

    public String getSuccessURL() {
        return successURL;
    }

    public void setServiceUnavailable(boolean serviceUnavailable) {
        this.serviceUnavailable = serviceUnavailable;
    }

    public void setAuthzFailed(boolean authzFailed) {
        this.authzFailed = authzFailed;
    }

    public void setAuthnFailed(boolean authnFailed) {
        this.authnFailed = authnFailed;
    }

    public void setAccountDisabled(boolean accountDisabled) {
        this.accountDisabled = accountDisabled;
    }

    public void setPartnersDisabled(boolean partnersDisabled) {
        this.partnersDisabled = partnersDisabled;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public void setApprovalRejected(boolean approvalRejected) {
        this.approvalRejected = approvalRejected;
    }

    public boolean isGuestAllowed() {
        return accessManager.isGuestAccessAllowed();
    }

    public void setLoginBanned(boolean loginBanned) {
        this.loginBanned = loginBanned;
    }

    public void setLicenseExceeded(boolean licenseExceeded) {
        this.licenseExceeded = licenseExceeded;
    }

    public boolean isNonUniqueCredentials() {
        return nonUniqueCredentials;
    }

    public void setNonUniqueCredentials(boolean nonUniqueCredentials) {
        this.nonUniqueCredentials = nonUniqueCredentials;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        if (emailAddress != null) {
            this.emailAddress = emailAddress.trim();
        }
    }

    public boolean isValidationSent() {
        return validationSent;
    }

    public void setLoginThrottled(boolean loginThrottled) {
        this.loginThrottled = loginThrottled;
    }

    public boolean isLoginThrottled() {
        return loginThrottled;
    }

    public boolean isInvalidCaptcha() {
        return invalidCaptcha;
    }

    public void setInvalidCaptcha(boolean invalidCaptcha) {
        this.invalidCaptcha = invalidCaptcha;
    }

    public boolean isCaptchaEnabled() {
        return JiveGlobals.getJiveBooleanProperty("login.captcha.enabled", false);
    }

    public LoginThrottle getLoginThrottle() {
        return loginThrottle;
    }

    public void setLoginThrottle(LoginThrottle loginThrottle) {
        this.loginThrottle = loginThrottle;
    }

    public int getLoginDelay() {
        return getLoginThrottle().getDelay();
    }

    public Captcha getCaptcha() {
        return captcha;
    }

    public void setCaptcha(Captcha captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaImageUrl() {
        return captchaImageUrl;
    }

    public void setCaptchaImageUrl(String captchaImageUrl) {
        this.captchaImageUrl = captchaImageUrl;
    }

    @Override
    public boolean isValidationEnabled() {
        return registrationManager.isValidationEnabled();
    }

    public void setRegisterOnly(boolean registerOnly) {
        this.registerOnly = registerOnly;
    }

    public boolean isRegisterOnly() {
        return registerOnly;
    }

    public boolean isRequestEmailForUsername() {
        return registrationConfiguration.isUseEmailForUsername()
            && !kerberosConfiguration.isEnabled()
            && !isFacebookEnabled()
            && !ldapConfiguration.isConfigured()
            && !externalLoginConfiguration.isEnabled()
            && !samlConfiguration.isEnabled();
    }

    public boolean isRememberMeEnabled() {
        return !JiveGlobals.getJiveBooleanProperty("login.rememberme.disabled", false);
    }

    public boolean isFacebookEnabled() {
        return facebookConfiguration.isEnabled();
    }

    public String getFacebookApplicationID() {
        return facebookConfiguration.getApplicationID();
    }

    public boolean isExternalLoginEnabled() {
        return externalLoginConfiguration.isEnabled();
    }

    public List<ExternalLogin> getExternalLogins() {
        return externalLoginManager.getVisible();
    }

    public String getCustomIntro() {
        return JiveGlobals.getJiveProperty("login.intro.customHtml");
    }

    public boolean isAccountExists() {
        return accountExists;
    }

    public void setAccountExists(boolean accountExists) {
        this.accountExists = accountExists;
    }

    public boolean isSsoLoginBypassCookie() {
        return ssoLoginBypassCookie;
    }

    public void setSsoLoginBypassCookie(boolean ssoLoginBypassCookie) {
        this.ssoLoginBypassCookie = ssoLoginBypassCookie;
    }

    @Required
    public void setExternalLoginConfiguration(ExternalLoginConfiguration externalLoginConfiguration) {
        this.externalLoginConfiguration = externalLoginConfiguration;
    }

    @Required
    public void setKerberosConfiguration(KerberosConfiguration kerberosConfiguration) {
        this.kerberosConfiguration = kerberosConfiguration;
    }

    @Required
    public void setExternalLoginManager(ExternalLoginManager externalLoginManager) {
        this.externalLoginManager = externalLoginManager;
    }

    @Required
    public void setFacebookConfiguration(FacebookConfiguration facebookConfiguration) {
        this.facebookConfiguration = facebookConfiguration;
    }

    @Required
    public void setFormAuthenticationRedirectStrategy(FormAuthenticationRedirectStrategy formAuthenticationRedirectStrategy) {
        this.formAuthenticationRedirectStrategy = formAuthenticationRedirectStrategy;
    }

    @Required
    public void setSamlConfiguration(SAMLConfiguration samlConfiguration) {
        this.samlConfiguration = samlConfiguration;
    }

    @Required
    public void setLdapConfiguration(LdapConfiguration ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

    @InjectConfiguration
    public void setRegistrationConfiguration(RegistrationConfiguration registrationConfiguration) {
        this.registrationConfiguration = registrationConfiguration;
    }

    @Required
    public void setRefererHelper(RefererHelper refererHelper) {
        this.refererHelper = refererHelper;
    }
    
    public static PermissionManager getPermissionManager() {
        if(permissionManager == null) {
            return JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }
}
