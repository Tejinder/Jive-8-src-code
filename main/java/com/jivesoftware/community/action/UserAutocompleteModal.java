/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;

import com.google.common.base.Predicate;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.user.relationships.UserRelationship;
import com.jivesoftware.community.user.relationships.UserRelationshipList;
import com.jivesoftware.community.user.relationships.UserRelationshipView;
import com.jivesoftware.community.util.PredicatedCollection;
import com.jivesoftware.community.web.struts.SetReferer;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.*;

@SetReferer(false)
@Decorate(false)
public class UserAutocompleteModal extends AbstractPeopleSearchAction {

    public static final String SELECTED_USER_DELIM = ",";

    public static final String VIEW_CONNECTIONS = "connections";
    public static final String VIEW_ORGCHART = "orgchart";
    public static final String VIEW_ADDRESSBOOK = "addressbook";

    private boolean multiple;
    private long labelID;

    // labels
    private Collection<UserRelationshipList> userRelationshipLists;
    // connections or orgchart
    private Collection<UserRelationshipView> userRelationshipViews;

    private String selectedUsers;
    private List<User> selectedUserObjects;
    private Iterable<User> addressbook;
    private boolean showAddressbook = false;

    private boolean usernameEnabled = true;
    private boolean emailEnabled = true;
    private boolean nameEnabled = true;
    private boolean profileEnabled = true;

    private boolean searchOptionsVisible = false; 
    
    private Integer brand = -1;
    private Integer region = -1;
    private Integer country = -1;
    private Integer role = -1;
    private Integer jobTitle = -1;
    private String fromSynchro;
    private boolean roleEnabled = true;
    private boolean brandEnabled = true;
    private boolean regionEnabled = true;
    private boolean countryEnabled = true;
    private boolean jobTitleEnabled = true;
    private boolean ownerfield = false;
    private Long selectedUserID = -1L;
    private boolean hideInvite = false;
    

    @Override
    public String execute() {
        setSocGrpSupported(false);
        setTagCloudSupported(false);
        setPeopleStatsSupported(false);

        if (StringUtils.isEmpty(view)) {
            view = VIEW_ALPHA;
        }

        if (StringUtils.isEmpty(getQuery())) {
            usernameEnabled = true;
            nameEnabled = true;
            emailEnabled = true;
            profileEnabled = true;
        }

        if (VIEW_SEARCH.equals(view) && !usernameEnabled && !nameEnabled && !emailEnabled && !profileEnabled) {
            searchOptionsVisible = true;
            addActionError(getText("search.noSelectedFields.error.text"));
            return SUCCESS;
        }

        if (VIEW_CONNECTIONS.equals(view)) {
            // load up connection lables
            userRelationshipLists = userRelationshipManager.getRelationshipListsByOwner(getUser());
            // load up connections (by label or all)
            UserRelationshipList userRelationshipList = null;
            if (labelID > 0) {
                userRelationshipList = userRelationshipManager.getRelationshipList(labelID);
            }
            if (userRelationshipList != null) {
                userRelationshipViews = userRelationshipManager.getRelationshipListUsers(userRelationshipManager.getRelationshipList(labelID));
            } else {
                userRelationshipViews = userRelationshipManager.getCurrentRelationships(getUser(), userRelationshipManager.getDefaultMeshRelationshipGraph(), true);
            }

           /* if (!isCanInvitePartners()) {
                excludePartnerUsersFromLists();
                excludePartnerUsersFromViews();
            }*/
            return SUCCESS;

        } else if (VIEW_ORGCHART.equals(view) && isOrgChartingEnabled()) {

            userRelationshipViews = userRelationshipManager.getCurrentRelationships(getUser(), userRelationshipManager.getDefaultHierarchicalRelationshipGraph(), true);
          /*  if (!isCanInvitePartners()) {
                excludePartnerUsersFromViews();
            }
*/
            return SUCCESS;
        }
        else if (VIEW_ADDRESSBOOK.equals(view)) {
            addressbook = presenceManager.getRoster(getUser()).getUsers();
        }

        return super.execute();

    }

 /*   private void excludePartnerUsersFromViews() {
        userRelationshipViews = new PredicatedCollection<UserRelationshipView>(userRelationshipViews, new Predicate<UserRelationshipView>() {
            @Override
            public boolean apply(@Nullable UserRelationshipView userRelationshipView) {
                if (userRelationshipView == null) {
                    return false;
                }
                if (userRelationshipView.getPerson().isPartner()) {
                    return false;
                }
                else {
                    return true;
                }
            }
        });
    }

    private void excludePartnerUsersFromLists() {
        if (userRelationshipLists != null) {
            for (UserRelationshipList userRelationshipList : userRelationshipLists) {
                if (userRelationshipList.getRelationships() != null) {
                    userRelationshipList.setRelationships(new PredicatedCollection<UserRelationship>(userRelationshipList.getRelationships(), new Predicate<UserRelationship>() {
                        @Override
                        public boolean apply(@Nullable UserRelationship userRelationship) {
                            if (userRelationship == null) {
                                return false;
                            }
                            try {
                                User user = userManager.getUser(userRelationship.getRelatedUserID());
                                if (user.isPartner()) {
                                    return false;
                                }
                                else {
                                    return true;
                                }
                            }
                            catch (UserNotFoundException e) {
                                return false;
                            }
                        }
                    }));
                }
            }
        }
    }
*/
    @Override
    public String input() {
        if (StringUtils.isEmpty(view)) {
            //view = showBrowse ? VIEW_ALPHA : VIEW_CONNECTIONS;
        	 view = VIEW_ALPHA;
        }
        return execute();
    }

    public boolean isSearchOptionsVisible() {
        return searchOptionsVisible && VIEW_SEARCH.equals(view);
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public long getLabelID() {
        return labelID;
    }

    public void setLabelID(long labelID) {
        this.labelID = labelID;
    }

    public String getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(String selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public Collection<UserRelationshipList> getUserRelationshipLists() {
        return userRelationshipLists;
    }

    public Collection<UserRelationshipView> getUserRelationshipViews() {
        return userRelationshipViews;
    }
    public List<User> getSelectedUserObjects() {
        if (selectedUserObjects == null) {
            selectedUserObjects = new ArrayList<User>();
            if (!StringUtils.isEmpty(selectedUsers)) {
                String[] userIDs = selectedUsers.split(",");
                for (String userID: userIDs) {
                    try {
                        selectedUserObjects.add(userManager.getUser(Long.parseLong(userID)));
                    } catch (NumberFormatException e) {
                        log.debug("Could not parse long in user autocomplete", e);
                    }
                    catch (UserNotFoundException e) {
                        log.info("Failed to load user with id: " + userID, e);
                    }
                }
            }
        }
        return selectedUserObjects;
    }
    public Iterable<User> getAddressbook() {
        return addressbook;
    }

    public boolean isShowAddressbook() {
        return showAddressbook;
    }

    public void setShowAddressbook(boolean showAddressbook) {
        this.showAddressbook = showAddressbook;
    }

    @Override
    public boolean getUsernameEnabled() {
        return usernameEnabled;
    }

    public void setUsernameEnabled(boolean usernameEnabled) {
        this.usernameEnabled = usernameEnabled;
    }

    @Override
    public boolean getNameEnabled() {
        return nameEnabled;
    }

    public void setNameEnabled(boolean nameEnabled) {
        this.nameEnabled = nameEnabled;
    }

    @Override
    public boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    @Override
    public boolean getProfileEnabled() {
        return profileEnabled;
    }

    public void setProfileEnabled(boolean profileEnabled) {
        this.profileEnabled = profileEnabled;
    }
    
    public Integer getBrand() {
        return brand;
    }

    public void setBrand(Integer brand) {
        this.brand = brand;
    }

    public Integer getRegion() {
        return region;
    }

    public void setRegion(Integer region) {
        this.region = region;
    }

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    @Override
    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(Integer jobTitle) {
        this.jobTitle = jobTitle;
    }

    @Override
    public String getFromSynchro() {
		return fromSynchro;
	}

	public void setFromSynchro(String fromSynchro) {
		this.fromSynchro = fromSynchro;
	}

    public boolean isRoleEnabled() {
        return roleEnabled;
    }

    public void setRoleEnabled(boolean roleEnabled) {
        this.roleEnabled = roleEnabled;
    }

    public boolean isBrandEnabled() {
        return brandEnabled;
    }

    public void setBrandEnabled(boolean brandEnabled) {
        this.brandEnabled = brandEnabled;
    }

    public boolean isRegionEnabled() {
        return regionEnabled;
    }

    public void setRegionEnabled(boolean regionEnabled) {
        this.regionEnabled = regionEnabled;
    }

    public boolean isCountryEnabled() {
        return countryEnabled;
    }

    public void setCountryEnabled(boolean countryEnabled) {
        this.countryEnabled = countryEnabled;
    }

    public boolean isJobTitleEnabled() {
        return jobTitleEnabled;
    }

    public void setJobTitleEnabled(boolean jobTitleEnabled) {
        this.jobTitleEnabled = jobTitleEnabled;
    }

	public boolean isOwnerfield() {
		return ownerfield;
	}

	public void setOwnerfield(boolean ownerfield) {
		this.ownerfield = ownerfield;
	}

	public Long getSelectedUserID() {
		return selectedUserID;
	}

	public void setSelectedUserID(Long selectedUserID) {
		this.selectedUserID = selectedUserID;
	}

	public boolean isHideInvite() {
		return hideInvite;
	}

	public void setHideInvite(boolean hideInvite) {
		this.hideInvite = hideInvite;
	}
	
}
