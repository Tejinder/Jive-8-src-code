/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.action.beans;

import com.jivesoftware.community.outcome.Outcome;
import com.jivesoftware.community.places.rest.impl.PublishBarViewBean;
import com.jivesoftware.community.web.component.ActionLink;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.jivesoftware.community.content.discussion.action.CreateDiscussionAction.isDiscussionQuest;

public class BaseContentActionBean {

    private boolean hasAttachPerms;
    private boolean hasImagePerms;
    private boolean guest;
    private String displayName;
    private String communityName;

    private String subject;
    private String body;

    private String token;
    private String tokenName;
    private String videoPickerURL;
    private String cancelURL;
    private long objectID;
    private int contentType;
    private boolean externallyVisible;
    private boolean keepAliveEnabled;

    private PublishBarViewBean publishBarViewBean;
    private AttachmentConfigActionBean attachmentConfigActionBean;
    private ContainerContextActionBean containerContextBean;
    private UserContextBean userContextBean;
    private AppContextBean appContextBean;

    private AttachedFileActionBean[] attachments = new AttachedFileActionBean[0];

    private boolean isMobileUI;
    private boolean isCreate = false;
    private boolean isMinorEdit;
    private String preferredMode;

    private String fromQuest;
    private int questStep;
    private String defaultVisibility;
    
    private Collection<String> countryList;
    private Collection<String> brandList;
    private Collection<String> methodologyList;
    private Collection<String> periodYear;
    private Collection<String> periodMonth;

    public Collection<String> getBrandList() {
		return brandList;
	}

	public void setBrandList(Collection<String> brandList) {
		this.brandList = brandList;
	}

	public Collection<String> getMethodologyList() {
		return methodologyList;
	}

	public void setMethodologyList(Collection<String> methodologyList) {
		this.methodologyList = methodologyList;
	}

	public Collection<String> getPeriodYear() {
		return periodYear;
	}

	public void setPeriodYear(Collection<String> periodYear) {
		this.periodYear = periodYear;
	}

	public Collection<String> getPeriodMonth() {
		return periodMonth;
	}

	public void setPeriodMonth(Collection<String> periodMonth) {
		this.periodMonth = periodMonth;
	}

	public Collection<String> getCountryList() {
		return countryList;
	}

	public void setCountryList(Collection<String> countryList) {
		this.countryList = countryList;
	}

	@SuppressWarnings("unchecked")
    private Set<Outcome> outcomes = Collections.EMPTY_SET;
   
    public boolean isHasAttachPerms() {
        return hasAttachPerms;
    }

    public void setHasAttachPerms(boolean hasAttachPerms) {
        this.hasAttachPerms = hasAttachPerms;
    }

    public boolean isHasImagePerms() {
        return hasImagePerms;
    }

    public void setHasImagePerms(boolean hasImagePerms) {
        this.hasImagePerms = hasImagePerms;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getVideoPickerURL() {
        return videoPickerURL;
    }

    public void setVideoPickerURL(String videoPickerURL) {
        this.videoPickerURL = videoPickerURL;
    }

    public String getCancelURL() {
        return cancelURL;
    }

    public void setCancelURL(String cancelURL) {
        this.cancelURL = cancelURL;
    }

    public PublishBarViewBean getPublishBarViewBean() {
        return publishBarViewBean;
    }

    public void setPublishBarViewBean(PublishBarViewBean publishBarViewBean) {
        this.publishBarViewBean = publishBarViewBean;
    }

    public AttachmentConfigActionBean getAttachmentConfigActionBean() {
        return attachmentConfigActionBean;
    }

    public void setAttachmentConfigActionBean(AttachmentConfigActionBean attachmentConfigActionBean) {
        this.attachmentConfigActionBean = attachmentConfigActionBean;
    }

    public ContainerContextActionBean getContainerContextBean() {
        return containerContextBean;
    }

    /**
     * Set the container where the content should be created in, by default.  This allows us to support
     * action "create" links as well as the editing case.  This information should be considered optional.
     *
     * @param containerContextBean container context bean
     */
    public void setContainerContextBean(ContainerContextActionBean containerContextBean) {
        this.containerContextBean = containerContextBean;
    }

    public UserContextBean getUserContextBean() {
        return userContextBean;
    }

    public void setUserContextBean(UserContextBean userContextBean) {
        this.userContextBean = userContextBean;
    }

    /**
     * Set to true if the client's browser does not support contentEditable.  iOS prior to 5.0 had this problem.
     * @return true if the client's browser does not support contentEditable
     */
    public boolean isMobileUI() {
        return isMobileUI;
    }

    public void setMobileUI(boolean mobileUI) {
        isMobileUI = mobileUI;
    }

    /**
     * Set to true if we're creating new content, as opposed to editing existing content.
     * @return true if we're creating new content
     */
    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean create) {
        isCreate = create;
    }

    public boolean isMinorEdit() {
        return isMinorEdit;
    }

    public void setMinorEdit(boolean minorEdit) {
        isMinorEdit = minorEdit;
    }

    public AttachedFileActionBean[] getAttachments() {
        return attachments;
    }

    public void setAttachments(AttachedFileActionBean[] attachments) {
        this.attachments = attachments;
    }

    public long getObjectID() {
        return objectID;
    }

    public void setObjectID(long objectID) {
        this.objectID = objectID;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public String getPreferredMode() {
        return preferredMode;
    }

    public void setPreferredMode(String preferredMode) {
        this.preferredMode = preferredMode;
    }

    public AppContextBean getAppContextBean() {
        return appContextBean;
    }

    public void setAppContextBean(AppContextBean appContextBean) {
        this.appContextBean = appContextBean;
    }

    public String getFromQuest() {
        return fromQuest;
    }

    public void setFromQuest(String fromQuest) {
        this.fromQuest = fromQuest;
    }

    public String getDefaultVisibility() {
        return defaultVisibility;
    }

    public void setDefaultVisibility(String defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    public int getQstep() {
        return questStep;
    }

    public void setQstep(int questStep) {
        this.questStep = questStep;
    }

    public boolean isDiscussionAction() {
        return isDiscussionQuest(fromQuest);
    }

    public boolean isExternallyVisible() {
        return externallyVisible;
    }

    public void setExternallyVisible(boolean externallyVisible) {
        this.externallyVisible = externallyVisible;
    }

    public Set<Outcome> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(Set<Outcome> outcomes) {
        this.outcomes = outcomes;
    }

    public void setKeepAliveEnabled(boolean keepAliveEnabled) {
        this.keepAliveEnabled = keepAliveEnabled;
    }

    public boolean isKeepAliveEnabled() {

        return keepAliveEnabled;
    }
}
