/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.user.preferences.action;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtils;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserAlreadyExistsException;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.ThreadActionSupport;
import com.jivesoftware.community.action.util.SoyActionUtil;
import com.jivesoftware.community.eae.mail.NotificationSettingsManager;
import com.jivesoftware.community.eae.mail.dao.NotificationSettingsBean;
import com.jivesoftware.community.eae.mail.engagement.DigestEmailManagerImpl;
import com.jivesoftware.community.eae.mail.engagement.DigestFrequencySetting;
import com.jivesoftware.community.eae.streams.StreamNameTooLongException;
import com.jivesoftware.community.inbox.InboxEntry;
import com.jivesoftware.community.inbox.InboxManager;
import com.jivesoftware.community.inbox.entry.UpgradeEmailPreferencesEntry;
import com.jivesoftware.community.mail.util.TemplateUtil;
import com.jivesoftware.community.navbar.NavMenuLink;
import com.jivesoftware.community.onboarding.OnboardingManager;
import com.jivesoftware.community.search.DefaultSearchSettingsManager;
import com.jivesoftware.community.search.SearchSettingsManager;
import com.jivesoftware.community.search.action.SearchActionHelper;
import com.jivesoftware.community.solution.annotations.InjectConfiguration;
import com.jivesoftware.community.user.preferences.util.EmailFrequencyOption;
import com.jivesoftware.community.user.preferences.util.PreferenceItem;
import com.jivesoftware.community.web.JiveResourceResolver;
import com.jivesoftware.community.welcome.WelcomeConfiguration;
import com.jivesoftware.eae.service.client.StreamNameNotUniqueException;
import com.jivesoftware.eae.service.client.api.Stream;
import com.jivesoftware.eae.service.client.api.StreamConfiguration;
import com.jivesoftware.util.DateUtils;
import com.jivesoftware.util.LocaleUtils;
import com.opensymphony.xwork2.ActionContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nullable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.jivesoftware.community.eae.mail.engagement.DigestEmailManagerImpl.DIGEST_USER_PROP;
import static com.jivesoftware.eae.constants.StreamConstants.StreamSource.publication;

/**
 * An action for setting users preferences, account settings and password.
 */
public class UserPreferencesAction extends PreferencesActionSupport {
    private static final Logger log = LogManager.getLogger(UserPreferencesAction.class);

    public static final String USER_HOMEPAGE = "jive.homepage";
    public static final String USER_HOMEPAGE_WELCOME = "/welcome";
    public static final String USER_HOMEPAGE_NEWS = "/news";
    public static final String USER_HOMEPAGE_INBOX = "/inbox";
    public static final String USER_NEWSPAGE = "jive.newspage";
    public static final String USER_INBOXPAGE = "jive.inboxpage";
    public static final String USERS_CHOOSE_HOMEPAGE = "skin.default.usersChooseHomepage";
    public static final boolean USERS_CHOOSE_HOMEPAGE_DEFAULT = true;
    public static final String DEFAULT_HOMEPAGE_KEY = "jive-nav-link-dashboard";
    public static final String DEFAULT_HOMEPAGE_KEY_BACKUP = "jive-nav-link-activity";
    public static final String DEFAULT_INBOXPAGE_KEY_BACKUP = "jive-nav-link-inbox";

    public static final String WCAG_STYLES = "wcagStyles";

    private String threadMode;
    private String commentThreadMode;
    private String userLocale;
    private String timezone;
    private boolean showDefaultSpotlightResults = true;
    private boolean userOnboardingEnabled;
    private Boolean contentTagEditMode; // if true, when viewing content an author
    // will see the form field for editing tags open. if false the tag form
    // will not be displayed until the author chooses to add or edit tags.
    // default setting is false, so default behavior is to not show the form field
    // until the add/edit tag link is chosen

    private boolean success;

    private NotificationSettingsManager notificationSettingsManager;
    private InboxManager inboxManager;
    private OnboardingManager onboardingManager;
    private WelcomeConfiguration welcomeConfiguration;

    boolean includePostContentInEmail = true;
    boolean notifyDirectActions;
    boolean notifyModerationQueue;
    boolean notifyInboxNotifications;
    private Map<String, Boolean> notifyPreferences;
    private EmailFrequencyOption emailFrequencyOption;
    private boolean receiveEmails;
    private String homePage;

    // System prefs //

    private boolean usersChooseLocale = JiveGlobals.getJiveBooleanProperty("skin.default.usersChooseLocale", true);
    private boolean usersChooseThreadMode = JiveGlobals
            .getJiveBooleanProperty("skin.default.usersChooseThreadMode", true);
    private String defaultThreadMode = JiveGlobals.getJiveProperty(ThreadActionSupport.THREAD_MODE_PROPERTY_NAME, "flat");
    private boolean usersChooseCommentThreadMode = JiveGlobals
            .getJiveBooleanProperty("skin.default.usersChooseCommentThreadMode", true);
    private String defaultCommentThreadMode = JiveGlobals.getJiveProperty("skin.default.commentThreadMode", "flat");
    private boolean usersChooseTimeZone = JiveGlobals.getJiveBooleanProperty("skin.default.editTimeZoneEnabled", true);
    private boolean wcagStyles = false;
    private boolean defaultContentTagEditMode = getDefaultContentTagEditMode();
    private boolean usersChooseContentTagEditMode = JiveGlobals.getJiveBooleanProperty("skin.default.usersChooseContentTagEditMode", true);

    private SearchSettingsManager searchSettingsManager;
    private SearchActionHelper searchActionHelper;
    String defaultSearchLanguage;
    public static final String EDIT_CONTENT_TAGS_BY_DEFAULT = "jive.user.pref.edit.content.tags.default";

    private Collection<NavMenuLink> navBarLinks;

    @Required
    public void setNavBarLinks(Collection<NavMenuLink> navBarLinks) {
        this.navBarLinks = navBarLinks;
    }

    public String getTemplate() {
        return "jive.preferences.generalPreferences";
    }

    @Override
    public String getActiveTab() {
        return "general-preferences";
    }

    public Map<String, Object> getTemplateData() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("actionURL", "user-preferences.jspa");

        data.put("usersChooseThreadMode", isUsersChooseThreadMode());
        data.put("threadMode", getThreadMode());
        data.put("usersChooseCommentThreadMode", isUsersChooseCommentThreadMode());
        data.put("commentThreadMode", getCommentThreadMode());
        data.put("usersChooseContentTagEditMode", isUsersChooseContentTagEditMode());
        data.put("contentTagEditMode", getContentTagEditMode());
        data.put("showDefaultSpotlightResults", isShowDefaultSpotlightResults());
        data.put("onboardingEnabled", onboardingManager.canSetOnboardingEnabled(getUser()));
        data.put("userOnboardingEnabled", isUserOnboardingEnabled());
        data.put("usersChooseLocale", isUsersChooseLocale());
        data.put("usersChoseHomePage", isUsersChooseHomePage());
        data.put("currentHomePage", getCurrentHomePage(getUser()));
        data.put("homePageOptions", getHomePageOptions());

        setLocales("supportedLocales", getSupportedLocales(), data);
        setLocales("locales", getLocales(), data);

        Locale userLocaleObj =
                StringUtils.isEmpty(getUserLocale()) ? null : LocaleUtils.localeCodeToLocale(getUserLocale());

        if (userLocaleObj == null) {
            data.put("userDisplayLanguage", "NO_LANGUAGE");
        }
        else {
            data.put("userDisplayLanguage", userLocaleObj.getDisplayLanguage(getLocale()));
        }

        data.put("userLocale", getUserLocale());
        data.put("isUserLocaleSet", getUserLocale() != null);
        data.put("usersChooseTimezone", isUsersChooseTimezone());
        data.put("timezoneID", getTimeZone().getID());
        setTimezoneList(data, getLocale());
        data.put(WCAG_STYLES, isWcagStyles());
        data.put("multipleLanguageSearchEnabled", isMultipleLanguageSearchEnabled());
        data.put("defaultSearchLanguage", getDefaultSearchLanguage());
        data.put("userSystemDefaultSearchLang", getUserSystemDefaultSearchLang());
        setAllowedSearchLanguages(data);

        Date date = new Date();

        if (getTimezone() == null || userLocaleObj == null) {
            data.put("newDate", new DateUtils(request, getUser()).formatDate(date));
            data.put("newTime", new DateUtils(request, getUser()).getFullFormatTime(date));
        }
        else {
            TimeZone tz = TimeZone.getTimeZone(getTimezone());
            data.put("newDate", new DateUtils(userLocaleObj, tz).formatDate(date));
            data.put("newTime", new DateUtils(userLocaleObj, tz).getFullFormatTime(date));
        }

        // email preferences data
        data.put("streams", streamHelper.getUserStreams(getUser(), getLocale())
                .stream()
                .filter(stream -> !stream.getSource().equals(publication.toString()))
                .collect(Collectors.toList()));
        data.put("emailNotificationsEnabled", notificationSettingsManager.isNotificationsEnabled());
        data.put("digestEmailEnabled", isDigestEmailEnabled());
        data.put(TemplateUtil.INCLUDE_POST_CONTENT_KEY, isIncludePostContentInEmail());
        data.put("userEmail", getUserEmailAddress());
        data.put("userID", getUserID());
        data.put("username", getUser().getUsername());

        boolean canUpdateEmail = getUser().isSetEmailSupported() &&
                JiveGlobals.getJiveBooleanProperty("skin.default.changeEmailEnabled", true);
        data.put("canUpdateEmail", canUpdateEmail);

        data.put("notifyDirectActions", isNotifyDirectActions());
        data.put("notifyModerationQueue", isNotifyModerationQueue());
        data.put("notifyInboxNotifications", isNotifyInboxNotifications());
        data.put("receiveEmails", isReceiveEmails());

        data.put("isDigestWeekly", isReceiveEmails() && emailFrequencyOption == EmailFrequencyOption.weekly_digest);
        data.put("digestWeeklyValue", EmailFrequencyOption.weekly_digest.name());

        data.put("isDigestSemiWeekly", isReceiveEmails() && emailFrequencyOption == EmailFrequencyOption.semi_weekly_digest);
        data.put("digestSemiWeeklyValue", EmailFrequencyOption.semi_weekly_digest.name());

        data.put("isDigestDaily", isReceiveEmails() && emailFrequencyOption == EmailFrequencyOption.daily_digest);
        data.put("digestDailyValue", EmailFrequencyOption.daily_digest.name());

        data.put("isDigestHourly", isReceiveEmails() && emailFrequencyOption == EmailFrequencyOption.hourly_digest);
        data.put("digestHourlyValue", EmailFrequencyOption.hourly_digest.name());

        data.put("isEmailNone", !isReceiveEmails() || emailFrequencyOption == EmailFrequencyOption.never);
        data.put("emailNoneValue", EmailFrequencyOption.never.name());

        data.put("rootCommunityName", getContainer().getName());
        
        SoyActionUtil.setToken("user.preferences", data);
        SoyActionUtil.mapActionErrors(this, data);

        return data;
    }

    private List<PreferenceItem> getHomePageOptions() {
        final ImmutableList.Builder<PreferenceItem> list = ImmutableList.builder();
        if (welcomeConfiguration.isVisible()) {
            list.add(new PreferenceItem(USER_HOMEPAGE_WELCOME, LocaleUtils.getLocalizedString("global.home")));
        }
        list.add(new PreferenceItem(USER_HOMEPAGE_NEWS, LocaleUtils.getLocalizedString("nav.bar.news.link")));
        list.add(new PreferenceItem(USER_HOMEPAGE_INBOX, LocaleUtils.getLocalizedString("global.inbox")));
        return list.build();
    }

    private String getCurrentHomePage(User user) {
        String homePage = (user != null && !user.isAnonymous() && user.getProperties() != null)
                          ? user.getProperties().get(USER_HOMEPAGE) : null;

        if (homePage == null) {
            if (welcomeConfiguration.isVisible()) {
                return USER_HOMEPAGE_WELCOME;
            } else {
                return USER_HOMEPAGE_NEWS;
            }
        }
        return homePage;
    }

    private Boolean isUsersChooseHomePage() {
        return JiveGlobals.getJiveBooleanProperty(USERS_CHOOSE_HOMEPAGE, USERS_CHOOSE_HOMEPAGE_DEFAULT);
    }

    protected void setAllowedSearchLanguages(Map<String, Object> data) {
        List<PreferenceItem> languages = Lists.transform(getAllowedSearchLanguages(), new Function<String, PreferenceItem>() {
            public PreferenceItem apply(@Nullable String language) {
                if (language == null) {
                    return null;
                }
                else {
                    return new PreferenceItem(language, getLanguageCodeDisplayName(language));
                }
            }
        });

        data.put("allowedSearchLanguages", languages);
    }

    // Webwork methods //

    public void validate() {

        if (usersChooseThreadMode && threadMode == null) {
            addFieldError("topicMode", "");
        }
        if (usersChooseCommentThreadMode && commentThreadMode == null) {
            addFieldError("commentThreadMode", "");
        }
        if (usersChooseLocale && userLocale == null) {
            addFieldError("userLocale", "");
        }
        if (usersChooseTimeZone && timezone == null) {
            addFieldError("timezone", "");
        }
    }

    public String cancel() {
        return CANCEL;
    }

    /**
     * Returns a list of locales for which the application has translations as a sorted list of Locale objects. The
     * sorting is based upon the locales' language display name.
     *
     * @return a list of available locales.
     */
    public List<Locale> getSupportedLocales() {
        // remove locales with the same display language, actual language selection happens in a separate field.
        List<Locale> tmpSupportedLocales = filterUniqueLanguages(LocaleUtils.SUPPORTED_LOCALES);
        if (getUserLocale() != null) {
            final Locale userLocale = LocaleUtils.localeCodeToLocale(getUserLocale());
            Collections.sort(tmpSupportedLocales, new Comparator<Locale>() {
                public int compare(Locale loc1, Locale loc2) {
                    return loc1.getDisplayLanguage(userLocale).compareTo(loc2.getDisplayLanguage(userLocale));
                }
            });
        }
        else {
            Collections.sort(tmpSupportedLocales, new Comparator<Locale>() {
                public int compare(Locale loc1, Locale loc2) {
                    return loc1.getDisplayLanguage().compareTo(loc2.getDisplayLanguage());
                }
            });
        }
        return Collections.unmodifiableList(tmpSupportedLocales);
    }

    private List<Locale> filterUniqueLanguages(List<Locale> tmpSupportedLocales) {
        Map<String, Locale> uniqueLanguages = Maps.newHashMap();
        for (Locale locale : tmpSupportedLocales) {
            uniqueLanguages.put(locale.getDisplayLanguage(), locale);
        }
        return new ArrayList(uniqueLanguages.values());
    }

    /**
     * Sets current user preferences in all the fields.
     */
    public String input() {
        if (isGuest()) {
            this.createRefererUrlAndStoreInActionContext();
            return UNAUTHENTICATED;
        }

        User user = getUser();

        if (usersChooseThreadMode) {
            setThreadMode(user.getProperties().get(JiveConstants.USER_DISCUSSION_THREAD_MODE));
            if (getThreadMode() == null) {
                setThreadMode(defaultThreadMode);
            }
        }

        if (isUsersChooseCommentThreadMode()) {
            setCommentThreadMode(user.getProperties().get(JiveConstants.USER_COMMENT_THREAD_MODE), defaultCommentThreadMode);
        }

        if (usersChooseLocale) {
            setUserLocale(user.getProperties().get(JiveConstants.USER_LOCALE_PROP_NAME));
        }

        if (usersChooseTimeZone) {
            setTimezone(user.getProperties().get(JiveConstants.USER_TIMEZONE_PROP_NAME));
            if (getTimezone() == null) {
                setTimezone(JiveGlobals.getTimeZone().getID());
            }
        }

        setWcagStyles(Boolean.valueOf(user.getProperties().get(WCAG_STYLES))); //defaults to false

        if (isMultipleLanguageSearchEnabled()) {
            setDefaultSearchLanguage(searchActionHelper.getDefaultSearchLanguage(user));
        }

        setShowDefaultSpotlightResults(searchSettingsManager.isSpotlightSearchDefaultEnabled(user));

        if (onboardingManager.canSetOnboardingEnabled(getUser())) {
            setUserOnboardingEnabled(onboardingManager.isOnboardingEnabled(user));
        }

        if (usersChooseContentTagEditMode) {
            String editByDefault = user.getProperties().get(EDIT_CONTENT_TAGS_BY_DEFAULT);

            if (editByDefault == null) {
                setContentTagEditMode(defaultContentTagEditMode);
            }
            else {
                setContentTagEditMode(Boolean.valueOf(editByDefault));
            }
        }

        NotificationSettingsBean notificationSettings = notificationSettingsManager.getSettings(user);

        assert notificationSettings != null;

        emailFrequencyOption = EmailFrequencyOption.never;

        receiveEmails = notificationSettings.isReceiveEmails();

        if (notificationSettingsManager.isNotificationsEnabled()) {

            includePostContentInEmail = notificationSettings.isIncludePostContentInEmails();

            notifyDirectActions = notificationSettings.isNotifyDirectActions();
            notifyModerationQueue = notificationSettings.isNotifyModerationQueue();
            notifyInboxNotifications = notificationSettings.isNotifyInboxNotifications();
        }

        // set user's email digest property
        if (isDigestEmailEnabled()) {
            DigestFrequencySetting digestFrequency = notificationSettings.getDigestFrequency(user.getProperties().get(DIGEST_USER_PROP));
            emailFrequencyOption = EmailFrequencyOption.valueOf(digestFrequency);
        }

        if (isSuccess()) {
            addActionMessage(getText("settings.update_successful.text"));
        }

        return INPUT;
    }

    /**
     * Because the referring URL will, ultimately, become a URL parameter, it needs to be encoded twice since it is,
     * itself, a URL.
     */
    void createRefererUrlAndStoreInActionContext() {
        try {
            String referrer = request.getRequestURI();
            if (StringUtils.isNotBlank(request.getQueryString())) {
                referrer += "?" + request.getQueryString();
            }
            referrer = URLEncoder.encode(referrer, JiveGlobals.getCharacterEncoding());
            referrer = URLEncoder.encode(referrer, JiveGlobals.getCharacterEncoding());
            this.getActionContext().put(JiveConstants.REFERER_KEY, referrer);
        } catch (Exception e) {
            log.error("Unable to encode redirect URL.");
        }
    }

    ActionContext getActionContext() {
        return ServletActionContext.getActionContext(request);
    }

    /**
     * Updates user settings.
     */
    public String execute() {
        User user = getUser();
        try {

            if (onboardingManager.canSetOnboardingEnabled(getUser())) {
                onboardingManager.setOnboardingEnabled(user, userOnboardingEnabled);
            }

            user = new UserTemplate(userManager.getUser(user.getID()));

            if (getThreadMode() != null && isUsersChooseThreadMode()) {
                user.getProperties().put(JiveConstants.USER_DISCUSSION_THREAD_MODE, getThreadMode());
            }

            if (getCommentThreadMode() != null && isUsersChooseCommentThreadMode()) {
                user.getProperties().put(JiveConstants.USER_COMMENT_THREAD_MODE, getCommentThreadMode());
            }

            if (getUserLocale() != null && isUsersChooseLocale()) {
                if (getUserLocale().equals("NO_LOCALE")) {
                    user.getProperties().remove(JiveConstants.USER_LOCALE_PROP_NAME);
                }
                else {
                    user.getProperties().put(JiveConstants.USER_LOCALE_PROP_NAME, getUserLocale());
                }
            }
            if (getTimezone() != null) {
                user.getProperties().put(JiveConstants.USER_TIMEZONE_PROP_NAME, getTimezone());
            }

            user.getProperties().put(WCAG_STYLES, String.valueOf(wcagStyles));

            if (isMultipleLanguageSearchEnabled()) {
                user.getProperties().put(JiveConstants.USER_SEARCH_DEFAULT_LANG, defaultSearchLanguage);
            }

            if (isUsersChooseHomePage()) {
                user.getProperties().put(USER_HOMEPAGE, homePage);
            }

            user.getProperties().put(DefaultSearchSettingsManager.USER_SPOTLIGHT_DEFAULT_PROPERTY, String.valueOf(showDefaultSpotlightResults));

            setTagEditDefault(user);

            // email settings

            NotificationSettingsBean notificationSettings = notificationSettingsManager.getSettings(user);
            assert notificationSettings != null;

            notificationSettings.setReceiveEmails(isReceiveEmails());

            if (notificationSettingsManager.isNotificationsEnabled() && notifyPreferences != null) {
                notificationSettings.setIncludePostContentInEmails(includePostContentInEmail);

                for (Map.Entry<String, Boolean> entry : notifyPreferences.entrySet()) {
                    if (StringUtils.isNumeric(entry.getKey())) {
                        StreamConfiguration config = streamManager.getStream(new Stream(Long.valueOf(entry.getKey())));
                        config.setReceiveEmails(isReceiveEmails() && entry.getValue());
                        try {
                            streamManager.modifyStream(config);
                        }
                        catch (StreamNameNotUniqueException ex) {
                            log.error(ex.getMessage(), ex);
                            return ERROR;
                        }
                        catch (StreamNameTooLongException ex) {
                            log.error(ex.getMessage(), ex);
                            return ERROR;
                        }
                    }
                    else if (entry.getKey().equals("directActions")) {
                        notificationSettings.setNotifyDirectActions(isReceiveEmails() && entry.getValue());
                    }
                    else if (entry.getKey().equals("notifyModerationQueue")) {
                        notificationSettings.setNotifyModerationQueue(isReceiveEmails() && entry.getValue());
                    }
                    else if (entry.getKey().equals("inboxNotifications")) {
                        notificationSettings.setNotifyInboxNotifications(isReceiveEmails() && entry.getValue());
                    }
                }
            }

            if (emailFrequencyOption != null) {
                notificationSettings.setReceiveHTMLEmails(true);
                notificationSettings.setEmailFrequency(emailFrequencyOption.getEmailFrequency());

                // set email digest property, but only if it is one of the valid property values
                if (isDigestEmailEnabled()) {
                    EmailFrequencyOption.setDigestEmailFrequencyProp(user, emailFrequencyOption.getDigestFrequency());
                }
            }

            //user has upgraded their settings
            notificationSettings.setUpgraded(true);

            notificationSettingsManager.saveSettings(user, notificationSettings);

            try {
                userManager.updateUser(user);
            }
            catch (UserNotFoundException e) {
                log.error(e.getMessage(), e);
                addActionError(getText("error.notfound.user"));
                return ERROR;
            }
            catch (UserAlreadyExistsException e) {
                log.error(e.getMessage(), e);
                return ERROR;
            }

            try {
                // Remove action queue item if necessary
                InboxEntry entry = inboxManager.get(UpgradeEmailPreferencesEntry.code, user.getID(), user.getObjectType(), user.getID(), InboxEntry.State.awaiting_action);
                if (entry != null) {
                    inboxManager.updateState(entry.getID(), InboxEntry.State.accepted);
                }
            }
            catch (Exception e) {
                //they will have to remove item manually
                log.warn("Error updating state of email preference action queue item for user " + user, e);
            }

        }
        catch (UnauthorizedException e) {
            return UNAUTHORIZED;
        }
        catch (UserNotFoundException e) {
            return ERROR;
        }
        //Audit Logs: Profile EDIT      
        String i18Text = getText("logger.profile.preferences.edit");
        SynchroLogUtils.addLog("", SynchroGlobal.PageType.PROFILE.getId(), SynchroGlobal.Activity.EDIT.getId(), 0, i18Text, "", -1L, getUser().getID());
        return SUCCESS;

    }

    private void setTagEditDefault(User user) {
        if (isUsersChooseContentTagEditMode()) {
            user.getProperties().put(EDIT_CONTENT_TAGS_BY_DEFAULT,
                    getContentTagEditMode() == null ? Boolean.FALSE.toString() : getContentTagEditMode().toString());
        }
    }

    // Getters and setters for the params //

    public String getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(String threadMode) {
        this.threadMode = threadMode;
    }

    public String getCommentThreadMode() {
        return commentThreadMode;
    }

    public void setCommentThreadMode(String commentThreadMode) {
        this.commentThreadMode = commentThreadMode;
    }

    public void setCommentThreadMode(String commentThreadMode, String defaultMode) {
        this.setCommentThreadMode(commentThreadMode);
        if (getCommentThreadMode() == null) {
            setCommentThreadMode(defaultMode);
        }
    }

    public String getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getContentTagEditMode() {
        return contentTagEditMode;
    }

    public void setContentTagEditMode(Boolean contentTagEditMode) {
        this.contentTagEditMode = contentTagEditMode;
    }

    public Boolean isShowDefaultSpotlightResults() {
        return showDefaultSpotlightResults;
    }

    public void setShowDefaultSpotlightResults(Boolean showDefaultSpotlightResults) {
        this.showDefaultSpotlightResults = showDefaultSpotlightResults;
    }

    public Boolean isUserOnboardingEnabled() {
        return userOnboardingEnabled;
    }

    public void setUserOnboardingEnabled(Boolean userOnboardingEnabled) {
        this.userOnboardingEnabled = userOnboardingEnabled;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Getters for defaults and min/max lengths //

    public boolean isUsersChooseLocale() {
        return usersChooseLocale;
    }

    public boolean isUsersChooseThreadMode() {
        return usersChooseThreadMode;
    }

    public boolean isUsersChooseCommentThreadMode() {
        return usersChooseCommentThreadMode;
    }

    public boolean isUsersChooseTimezone() {
        return usersChooseTimeZone;
    }

    public boolean isWcagStyles() {
        return wcagStyles;
    }

    public void setWcagStyles(boolean wcagStyles) {
        this.wcagStyles = wcagStyles;
    }

    public boolean isUsersChooseContentTagEditMode() {
        return usersChooseContentTagEditMode;
    }

    public void setUsersChooseContentTagEditMode(boolean usersChooseContentTagEditMode) {
        this.usersChooseContentTagEditMode = usersChooseContentTagEditMode;
    }

    public String[][] getThreadModes() {
        return new String[][]{{"flat", getText("thread.flat.listitem")},
                {"threaded", getText("thread.threaded.listitem")}};
    }

    public boolean acceptableParameterName(String string) {
        return !string.startsWith("profile[") && super.acceptableParameterName(string);
    }

    public void setSearchSettingsManager(SearchSettingsManager searchSettingsManager) {
        this.searchSettingsManager = searchSettingsManager;
    }

    public void setSearchActionHelper(SearchActionHelper searchActionHelper) {
        this.searchActionHelper = searchActionHelper;
    }

    public final String getCancel() {
        StringBuilder buffer = new StringBuilder(JiveResourceResolver.getJiveObjectURL(this.getUser()));
        buffer.append("?view=profile");
        return buffer.toString();
    }

    public boolean isMultipleLanguageSearchEnabled() {
        return searchSettingsManager.isMultipleLanguageSearchEnabled();
    }

    public List<String> getAllowedSearchLanguages() {
        return searchSettingsManager.getAllowedSearchLanguages();
    }

    public String getDefaultSearchLanguage() {
        return defaultSearchLanguage;
    }

    public void setDefaultSearchLanguage(String defaultSearchLanguage) {
        this.defaultSearchLanguage = defaultSearchLanguage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    /**
     * Returns the default search language that would be used for the user in the case
     * where the user has <b>not</b> chosen to be able to see the search language and
     * thus has <b>not</b> chosen a default search language
     *
     * @return String language code
     */
    public String getUserSystemDefaultSearchLang() {
        return searchActionHelper.getUserSystemDefaultSearchLanguage(getUser());
    }

    public static Boolean getDefaultContentTagEditMode() {
        return JiveGlobals.getJiveBooleanProperty("skin.default.defaultContentTagEditMode", false);
    }

    public static Boolean getUserDefaultContentTagEditMode(User user) {
        String editMode = user != null && !user.isAnonymous() && user.getProperties() != null
                          ? user.getProperties().get(EDIT_CONTENT_TAGS_BY_DEFAULT) : null;

        if (editMode == null) {
            return getDefaultContentTagEditMode();
        }
        else {
            return Boolean.valueOf(editMode);
        }
    }

    public boolean isDigestEmailEnabled() {
        return DigestEmailManagerImpl.isDigestEmailEnabled();
    }

    public EmailFrequencyOption getEmailFrequencyOption() {
        return emailFrequencyOption;
    }

    public void setEmailFrequencyOption(String emailFrequencyOption) {
        try {
            this.emailFrequencyOption = EmailFrequencyOption.valueOf(emailFrequencyOption);
        }
        catch (IllegalArgumentException e) {
            this.emailFrequencyOption = EmailFrequencyOption.never;
        }
    }

    protected String getUserEmailAddress() {
        if (StringUtils.isEmpty(getUser().getEmail())) {
            return getText("global.na");
        }
        else {
            return getUser().getEmail();
        }
    }

    public boolean isIncludePostContentInEmail() {
        return includePostContentInEmail;
    }

    public void setIncludePostContentInEmail(boolean includePostContentInEmail) {
        this.includePostContentInEmail = includePostContentInEmail;
    }

    public boolean isNotifyDirectActions() {
        return notifyDirectActions;
    }

    public boolean isNotifyModerationQueue() {
        return notifyModerationQueue;
    }

    public boolean isNotifyInboxNotifications() {
        return notifyInboxNotifications;
    }

    public Map<String, Boolean> getNotifyPreferences() {
        return notifyPreferences;
    }

    public void setNotifyPreferences(Map<String, Boolean> notifyPreferences) {
        this.notifyPreferences = notifyPreferences;
    }

    public boolean isReceiveEmails() {
        return receiveEmails;
    }

    public void setReceiveEmails(boolean receiveEmails) {
        this.receiveEmails = receiveEmails;
    }

    public void setNotificationSettingsManager(NotificationSettingsManager notificationSettingsManager) {
        this.notificationSettingsManager = notificationSettingsManager;
    }

    public void setInboxManagerImpl(InboxManager inboxManager) {
        this.inboxManager = inboxManager;
    }

    public void setOnboardingManager(OnboardingManager onboardingManager) {
        this.onboardingManager = onboardingManager;
    }

    @InjectConfiguration
    public void setWelcomeConfiguration(WelcomeConfiguration welcomeConfiguration) {
        this.welcomeConfiguration = welcomeConfiguration;
    }
}
