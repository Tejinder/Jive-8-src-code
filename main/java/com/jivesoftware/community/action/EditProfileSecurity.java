/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtils;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.base.util.UserPermHelper;
import com.jivesoftware.community.action.util.AlwaysDisallowAnonymous;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.filter.MemberFilter;
import com.jivesoftware.community.browse.sort.ModificationDateSort;
import com.jivesoftware.community.browse.util.CastingIterator;
import com.jivesoftware.community.externalcollaboration.SharedGroupManager;
import com.jivesoftware.community.socialgroup.SocialGroup;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.UserProfile;
import com.jivesoftware.community.user.profile.security.ProfileSecurityLevel;
import com.jivesoftware.community.user.profile.security.ProfileSecurityLevelComparator;
import com.jivesoftware.community.user.profile.security.ProfileSecurityLevelView;
import com.jivesoftware.community.user.profile.security.ProfileSecurityManager;
import com.jivesoftware.util.LocaleUtils;
import com.opensymphony.xwork2.Preparable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@AlwaysDisallowAnonymous
public class EditProfileSecurity extends EditProfile implements Preparable {

    private ProfileSecurityManager profileSecurityManager;
    private Map<String, List<ProfileSecurityLevelView>> securityLevelOptions;
    private List<ProfileSecurityLevelView> previewLevels;
    private SharedGroupManager sharedGroupManager;

    public void setProfileSecurityManager(ProfileSecurityManager profileSecurityManager) {
        this.profileSecurityManager = profileSecurityManager;
    }

    public void setSharedGroupManagerImpl(SharedGroupManager sharedGroupManager) {
        this.sharedGroupManager = sharedGroupManager;
    }

    private long nameSecurityLevelID;
    private long emailSecurityLevelID;
    private long imageSecurityLevelID;
    private long lastLoginSecurityLevelID;
    private long creationDateSecurityLevelID;
    private long presenceSecurityLevelID;

    private boolean presenceEnabled;

    private Iterator<SocialGroup> socialGroups;

    private Map<Long, EditProfileFieldValue> profile;

    @Override
    public void prepare() {

        super.prepare();

        presenceEnabled = chatPresenceManagerImpl.isPresenceAvailable();

        long targetUserID = getTargetUserID();
        User targetUser = getUser(targetUserID);
        Locale targetUserLocale = LocaleUtils.getCurrentUserLocale();
        socialGroups = new CastingIterator<SocialGroup> (
                browseManager.getContainers(
                        ImmutableSet.<BrowseFilter>of(new MemberFilter().getBoundInstance(targetUserID)),
                        new ModificationDateSort(), 0, Integer.MAX_VALUE));

        securityLevelOptions = Maps.newHashMap();

        List<ProfileSecurityLevel> availableLevels = profileSecurityManager.getAvailableProfileSecurityLevels(targetUser);

        //levels for username
        ProfileSecurityLevel usernameLevel = availableLevels.get(0);
        securityLevelOptions.put("username",
                Lists.newArrayList(
                        new ProfileSecurityLevelView(usernameLevel,
                                LocaleUtils.getLocalizedString(usernameLevel.getNameKey(), targetUserLocale))));

        //levels for name
        List<ProfileSecurityLevel> nameAvailableLevels = profileSecurityManager
                .getNonProfileFieldAvailableSecurityLevels(ProfileSecurityManager.NAME_PROFILE_SECURITY_LEVEL_OPTIONS,
                        targetUser);
        securityLevelOptions.put("name", createProfileSecurityLevelViews(nameAvailableLevels));

        //levels for email
        List<ProfileSecurityLevel> emailAvailableLevels = profileSecurityManager
                .getNonProfileFieldAvailableSecurityLevels(ProfileSecurityManager.EMAIL_PROFILE_SECURITY_LEVEL_OPTIONS,
                        targetUser);
        securityLevelOptions.put("email", createProfileSecurityLevelViews(emailAvailableLevels));

        //levels for image
        if (isProfileImageEnabled()) {
            List<ProfileSecurityLevel> imageAvailableLevels = profileSecurityManager
                    .getNonProfileFieldAvailableSecurityLevels(
                            ProfileSecurityManager.IMAGE_PROFILE_SECURITY_LEVEL_OPTIONS, targetUser);
            securityLevelOptions.put("image", createProfileSecurityLevelViews(imageAvailableLevels));
        }


        //levels for last login
        List<ProfileSecurityLevel> lastLoginAvailableLevels = profileSecurityManager
                .getNonProfileFieldAvailableSecurityLevels(ProfileSecurityManager.LAST_LOGIN_PROFILE_SECURITY_LEVEL_OPTIONS,
                        targetUser);
        securityLevelOptions.put("lastLogin", createProfileSecurityLevelViews(lastLoginAvailableLevels));

        //levels for creation date (member since)
        List<ProfileSecurityLevel> creationDateAvailableLevels = profileSecurityManager
                .getNonProfileFieldAvailableSecurityLevels(ProfileSecurityManager.CREATION_DATE_PROFILE_SECURITY_LEVEL_OPTIONS,
                        targetUser);
        securityLevelOptions.put("creationDate", createProfileSecurityLevelViews(creationDateAvailableLevels));

        //levels for presence
        if (isPresenceEnabled()) {
            List<ProfileSecurityLevel> presenceAvailableLevels = profileSecurityManager
                    .getNonProfileFieldAvailableSecurityLevels(
                            ProfileSecurityManager.PRESENCE_PROFILE_SECURITY_LEVEL_OPTIONS, targetUser);
            securityLevelOptions.put("presence", createProfileSecurityLevelViews(presenceAvailableLevels));
        }

        //profile fields
        for (ProfileField profileField : fields.values()) {
            List<ProfileSecurityLevelView> levelViews = Lists.newArrayList();
            List<Long> levelIDs = profileField.getAvailableSecurityLevelIDs();
            for (ProfileSecurityLevel psl : availableLevels) {
                if (levelIDs.contains(psl.getID())) {
                    levelViews.add(new ProfileSecurityLevelView(psl, LocaleUtils.getLocalizedString(psl.getNameKey(), targetUserLocale)));
                }
            }
            securityLevelOptions.put(String.valueOf(profileField.getID()), levelViews);
        }

        previewLevels = new ArrayList<ProfileSecurityLevelView>();
        for (ProfileSecurityLevel availableLevel : availableLevels) {
            if (availableLevel.isPreviewable()) {
                previewLevels.add(new ProfileSecurityLevelView(availableLevel,
                        getTextWithAlternate(targetUserLocale,
                                availableLevel.getNameKey(),
                                availableLevel.getNameKey()+".preview")));
            }
        }

        try {
            User u = userManager.getUser(targetUserID);    //get a fresh user copy so the props are up to date
            this.nameSecurityLevelID = profileSecurityManager
                    .getNonProfileFieldSecurityLevelID(User.NAME_PROFILE_SECURITY_LEVEL, u);
            this.emailSecurityLevelID = profileSecurityManager
                    .getNonProfileFieldSecurityLevelID(User.EMAIL_PROFILE_SECURITY_LEVEL, u);
            this.imageSecurityLevelID = profileSecurityManager
                    .getNonProfileFieldSecurityLevelID(User.IMAGE_PROFILE_SECURITY_LEVEL, u);
            this.lastLoginSecurityLevelID = profileSecurityManager
                    .getNonProfileFieldSecurityLevelID(User.LAST_LOGIN_PROFILE_SECURITY_LEVEL, u);
            this.creationDateSecurityLevelID = profileSecurityManager
                    .getNonProfileFieldSecurityLevelID(User.CREATION_DATE_PROFILE_SECURITY_LEVEL, u);
            this.presenceSecurityLevelID = profileSecurityManager
                    .getNonProfileFieldSecurityLevelID(User.PRESENCE_PROFILE_SECURITY_LEVEL, u);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private String getTextWithAlternate(Locale locale, String key, String alternateKey) {
        if (alternateKey != null) {
            String text = LocaleUtils.getLocalizedString(alternateKey, locale);
            if (text.equals(alternateKey)) {
                return LocaleUtils.getLocalizedString(key, locale);
            }
            else {
                return text;
            }
        }
        else {
            return LocaleUtils.getLocalizedString(key, locale);
        }
    }

    public Map<Long, EditProfileFieldValue> getProfile() {
        return profile;
    }

    public void setProfile(Map<Long, EditProfileFieldValue> profile) {
        this.profile = profile;
    }

    public boolean isProfileImageEnabled() {
        return profileManager.isProfileImageEnabled();
    }

    public List<ProfileSecurityLevelView> getPreviewLevels() {
        return previewLevels;
    }

    public Map<String, List<ProfileSecurityLevelView>> getSecurityLevelOptions() {
        return securityLevelOptions;
    }

    public UserProfile getProfileObject() {
        return userProfileProvider.get(getTargetUser());
    }

    public Iterator<SocialGroup> getSocialGroups() {
        return socialGroups;
    }

    public long getNameSecurityLevelID() {
        return nameSecurityLevelID;
    }

    public void setNameSecurityLevelID(long nameSecurityLevelID) {
        this.nameSecurityLevelID = nameSecurityLevelID;
    }

    public long getEmailSecurityLevelID() {
        return emailSecurityLevelID;
    }

    public void setEmailSecurityLevelID(long emailSecurityLevelID) {
        this.emailSecurityLevelID = emailSecurityLevelID;
    }

    public long getImageSecurityLevelID() {
        return imageSecurityLevelID;
    }

    public void setImageSecurityLevelID(long imageSecurityLevelID) {
        this.imageSecurityLevelID = imageSecurityLevelID;
    }

    public long getLastLoginSecurityLevelID() {
        return lastLoginSecurityLevelID;
    }

    public void setLastLoginSecurityLevelID(long lastLoginSecurityLevelID) {
        this.lastLoginSecurityLevelID = lastLoginSecurityLevelID;
    }

    public long getCreationDateSecurityLevelID() {
        return creationDateSecurityLevelID;
    }

    public void setCreationDateSecurityLevelID(long creationDateSecurityLevelID) {
        this.creationDateSecurityLevelID = creationDateSecurityLevelID;
    }

    public long getPresenceSecurityLevelID() {
        return presenceSecurityLevelID;
    }

    public void setPresenceSecurityLevelID(long presenceSecurityLevelID) {
        this.presenceSecurityLevelID = presenceSecurityLevelID;
    }

    public boolean isPresenceEnabled() {
        return presenceEnabled;
    }

    @Override
    public void validate() {

        //non-profile fields
        if (!optionsContain("name", nameSecurityLevelID)) {
            addFieldError("nameSecurityLevelID", getText("prof.sec.err.select"));
        }
        if (!optionsContain("email", emailSecurityLevelID)) {
            addFieldError("emailSecurityLevelID", getText("prof.sec.err.select"));
        }
        if (isProfileImageEnabled()) {
            if (!optionsContain("image", imageSecurityLevelID)) {
                addFieldError("imageSecurityLevelID", getText("prof.sec.err.select"));
            }
        }
        if (!optionsContain("lastLogin", lastLoginSecurityLevelID)) {
            addFieldError("lastLoginSecurityLevelID", getText("prof.sec.err.select"));
        }
        if (!optionsContain("creationDate", creationDateSecurityLevelID)) {
            addFieldError("creationDateSecurityLevelID", getText("prof.sec.err.select"));
        }
        if (isPresenceEnabled()) {
            if (!optionsContain("presence", presenceSecurityLevelID)) {
                addFieldError("presenceSecurityLevelID", getText("prof.sec.err.select"));
            }
        }

        //profile fields
        for (ProfileField profileField : fields.values()) {
            String fieldKey = String.valueOf(profileField.getID());
            long securityLevelID = profile.get(profileField.getID()).getEffectiveSecurityLevelID();
            if (!optionsContain(fieldKey, securityLevelID)) {
                addFieldError("profile[" + fieldKey + "].securityLevelID", getText("prof.sec.err.select"));
            }
        }
    }

    private boolean optionsContain(String key, long levelID) {
        for (ProfileSecurityLevelView profileSecurityLevelView : securityLevelOptions.get(key)) {
            if (profileSecurityLevelView.getID() == levelID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String input() {

        if (!isAuthorized()) {
            return UNAUTHORIZED;
        }

        super.input();
        profile = new LinkedHashMap<Long, EditProfileFieldValue>(super.getProfile());
        User profileOwner = getTargetUser();

        //if profile is empty, populate it and make sure profile fields are set to effective sec level IDs
        for (Long fieldID : fields.keySet()) {
            if (profile.get(fieldID) == null) {
                ProfileField field = fields.get(fieldID);
                ProfileFieldValue value = new ProfileFieldValue(field);
                //ensure any newly created fields is set to valid security level
                value.setEffectiveSecurityLevelID(profileSecurityManager.getEffectiveProfileSecurityLevelID(field, value, profileOwner));
                profile.put(fieldID, new EditProfileFieldValue(value));
            }
        }

        return INPUT;
    }

    public boolean acceptableParameterName(String parameterName) {
        return parameterName.startsWith("profile[")  || super.acceptableParameterName(parameterName);
    }

    @Override
    public String execute() {

        if (!isAuthorized()) {
            return UNAUTHORIZED;
        }

        UserTemplate user = getTargetUser();

        if (profile == null) {
            profile = Collections.EMPTY_MAP;
        }

        Map<Long, ProfileFieldValue> latestProfile = new LinkedHashMap<Long, ProfileFieldValue>(profileManager.getProfile(user));
        List<ProfileFieldValue> list = new ArrayList<ProfileFieldValue>(profile.size());
        for (ProfileField field : fields.values()) {

            //get latest value from manager so no field values are modified
            ProfileFieldValue up = latestProfile.get(field.getID());
            if (up == null) {
                //ensure a value exists
                up = new ProfileFieldValue(field);
            }

            //grab the profile field value from the form
            ProfileFieldValue formValue = profile.get(field.getID());
            if (formValue != null) {
                boolean needToUpdateFieldSecurityLevelFromForm = true;
                if (up.getEffectiveSecurityLevelID() == ProfileSecurityLevel.DefaultProfileSecurityLevel.ALL_BUT_PARTNER_USERS.getID() &&
                        !sharedGroupManager.isEnabled()) {
                    ProfileSecurityLevelComparator profileSecurityLevelComparator = new ProfileSecurityLevelComparator();
                    if (profileSecurityLevelComparator.compareByIdScore(up.getEffectiveSecurityLevelID(),formValue.getEffectiveSecurityLevelID()) > 0) {
                        needToUpdateFieldSecurityLevelFromForm = false;
                    }
                }
                if (needToUpdateFieldSecurityLevelFromForm) {
//                    if a security level has been set, use that
                    up.setSecurityLevelID(formValue.getEffectiveSecurityLevelID());
                }

                //save the change
                list.add(up);
            }
        }

        try {
            profileManager.setProfile(user, list);
            User u = userManager.getUser(user.getID());
            profileSecurityManager
                    .setNonProfileFieldSecurityLevelID(User.NAME_PROFILE_SECURITY_LEVEL, u, nameSecurityLevelID);
            profileSecurityManager
                    .setNonProfileFieldSecurityLevelID(User.EMAIL_PROFILE_SECURITY_LEVEL, u, emailSecurityLevelID);
            if (isProfileImageEnabled()) {
                // only update profile image security levels if its enabled
                profileSecurityManager
                        .setNonProfileFieldSecurityLevelID(User.IMAGE_PROFILE_SECURITY_LEVEL, u, imageSecurityLevelID);
            }
            profileSecurityManager
                    .setNonProfileFieldSecurityLevelID(User.LAST_LOGIN_PROFILE_SECURITY_LEVEL, u, lastLoginSecurityLevelID);
            profileSecurityManager
                    .setNonProfileFieldSecurityLevelID(User.CREATION_DATE_PROFILE_SECURITY_LEVEL, u, creationDateSecurityLevelID);
            if (isPresenceEnabled()) {
                // only update presence security levels if its enabled
                profileSecurityManager.setNonProfileFieldSecurityLevelID(User.PRESENCE_PROFILE_SECURITY_LEVEL, u,
                        presenceSecurityLevelID);
            }
            userManager.updateUser(u);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return ERROR;
        }

        addActionMessage(getText("profile.edit_user_profile_security.saved.text"));

        input();


        //Audit Logs: Profile EDIT      
          String i18Text = getText("logger.profile.edit");
          SynchroLogUtils.addLog("", SynchroGlobal.PageType.PROFILE.getId(), SynchroGlobal.Activity.EDIT.getId(), 0, i18Text, "", -1L, getUser().getID());
          
        return SUCCESS;
    }

    private boolean isAuthorized() {
        if (getTargetUserID() != getUserID()) {
            if (!UserPermHelper.isGlobalUserAdmin()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the user for the given ID. If no user can be found for the given user, return null instead.
     *
     * @param userId
     *            The ID of the desired user.
     * @return The user that corresponds to the given ID or null if no user can be found.
     */
    private User getUser(long userId) {
        try {
            return userManager.getUser(userId);
        } catch (UserNotFoundException e) {
            return null;
        }
    }

}
