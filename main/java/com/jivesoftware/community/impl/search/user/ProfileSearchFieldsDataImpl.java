/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl.search.user;

import com.jivesoftware.base.User;
import com.jivesoftware.community.search.user.UserSearchInfo;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.*;

public class ProfileSearchFieldsDataImpl implements ProfileSearchFieldsData {

    private final UserSearchInfo userSearchInfo;
    private final String nameACL;
    private final long nameSecurityLevelID;
    private final String emailACL;
    private final long emailSecurityLevelID;
    private final String creationDateACL;
    private final long creationDateSecurityLevelID;
    private final String lastLoggedInDateACL;
    private final long lastLoggedInDateSecurityLevelID;
    private final String lastProfileUpdateACL;
    private final long lastProfileUpdateSecurityLevelID;
    private final String modificationDateACL;
    private final long modificationDateSecurityLevelID;
    private final String initialLoginDateACL;
    private final long initialLoginDateSecurityLevelID;
    private final List<UserProfileFieldSearchData> profileFieldsData;
    private final List<ProfileSearchKeywordsData> keywordsData;
    private final boolean statusLevelsEnabled;
    private final String brand;
    private final String region;
    private final String country;
    private final String role;
    private final String jobTitle;
    private final String fromSynchro;

    public ProfileSearchFieldsDataImpl(UserSearchInfo userSearchInfo, String nameACL, long nameSecurityLevelID,
            String emailACL, long emailSecurityLevelID, String creationDateACL, long creationDateSecurityLevelID,
            String lastLoggedInDateACL, long lastLoggedInDateSecurityLevelID,
            String modificationDateACL, long modificationDateSecurityLevelID,
            String lastProfileUpdateACL, long lastProfileUpdateSecurityLevelID,
            String initialLoginDateACL, long initialLoginDateSecurityLevelID,
            List<UserProfileFieldSearchData> profileFieldsData, List<ProfileSearchKeywordsData> keywordsData,
            boolean statusLevelsEnabled, String brand, String region, String country, String role, String jobTitle, String fromSynchro)
    {
        this.userSearchInfo = userSearchInfo;
        this.nameACL = nameACL;
        this.nameSecurityLevelID = nameSecurityLevelID;
        this.emailACL = emailACL;
        this.emailSecurityLevelID = emailSecurityLevelID;
        this.creationDateACL = creationDateACL;
        this.creationDateSecurityLevelID = creationDateSecurityLevelID;
        this.lastLoggedInDateACL = lastLoggedInDateACL;
        this.lastLoggedInDateSecurityLevelID = lastLoggedInDateSecurityLevelID;
        this.lastProfileUpdateACL = lastProfileUpdateACL;
        this.lastProfileUpdateSecurityLevelID = lastProfileUpdateSecurityLevelID;
        this.modificationDateACL = modificationDateACL;
        this.modificationDateSecurityLevelID = modificationDateSecurityLevelID;
        this.initialLoginDateACL = initialLoginDateACL;
        this.initialLoginDateSecurityLevelID = initialLoginDateSecurityLevelID; 
        this.profileFieldsData = profileFieldsData;
        this.keywordsData = keywordsData;
        this.statusLevelsEnabled = statusLevelsEnabled;
        this.brand = brand;
        this.region = region;
        this.country = country;
        this.role = role;
        this.jobTitle = jobTitle;
        this.fromSynchro = fromSynchro;
    }

    public long getUserID() {
        return userSearchInfo.user.getID();
    }

    public String getUsername() {
        return userSearchInfo.user.getUsername();
    }

    public boolean isEnabled() {
        return userSearchInfo.user.isEnabled();
    }

    public User.Type getUserType() {
        return userSearchInfo.user.getType();
    }

    public String getName() {
        return userSearchInfo.user.getName();
    }

    public String getNameACL() {
        return nameACL;
    }

    public long getNameSecurityLevelID() {
        return nameSecurityLevelID;
    }

    public String getLastName() {
        return userSearchInfo.user.getLastName();
    }

    public String getEmail() {
        return userSearchInfo.user.getEmail();
    }

    public String getEmailACL() {
        return emailACL;
    }

    public long getEmailSecurityLevelID() {
        return emailSecurityLevelID;
    }

    public Date getCreationDate() {
        return userSearchInfo.user.getCreationDate();
    }

    public String getCreationDateACL() {
        return creationDateACL;
    }

    public long getCreationDateSecurityLevelID() {
        return creationDateSecurityLevelID;
    }

    public Date getLastLoggedInDate() {
        return userSearchInfo.user.getLastLoggedIn();
    }

    public String getLastLoggedInDateACL() {
        return lastLoggedInDateACL;
    }

    public long getLastLoggedInDateSecurityLevelID() {
        return lastLoggedInDateSecurityLevelID;
    }

    public Date getLastProfileUpdate() {
        return userSearchInfo.user.getLastProfileUpdate();
    }

    public String getLastProfileUpdateACL() {
        return lastProfileUpdateACL;
    }

    public long getLastProfileUpdateSecurityLevelID() {
        return lastProfileUpdateSecurityLevelID;
    }

    public Date getModificationDate() {
        Date modificationDate = userSearchInfo.user.getModificationDate();
        if (modificationDate == null) {
            modificationDate = userSearchInfo.user.getCreationDate();
        }
        return modificationDate;
    }

    public String getModificationDateACL() {
        return modificationDateACL;
    }

    @Override
    public Date getInitialLoginDate() {
        return userSearchInfo.user.getInitialLoginDate();
    }

    @Override
    public String getInitialLoginDateACL() {
        return initialLoginDateACL;
    }

    public long getModificationDateSecurityLevelID() {
        return modificationDateSecurityLevelID;
    }

    public long getTotalStatusPoints() {
        if (statusLevelsEnabled) {
            return userSearchInfo.totalStatusPoints;
        }

        return -1L;
    }

    /*
    public Map<EntityDescriptor, Long> getContainerStatusPointMap() {
        if (statusLevelsEnabled) {
            return userSearchInfo.containerStatusPoints;
        }

        return Collections.emptyMap();
    }
    */

    public Set<Long> getTagIDs() {
        return userSearchInfo.tagIDs;
    }

    public Set<String> getTags() {
        return userSearchInfo.tags;
    }

    public List<String> getExpertiseTags() {
        return userSearchInfo.expertiseTags;
    }

    public List<UserProfileFieldSearchData> getProfileFieldsData() {
        return profileFieldsData;
    }

    public List<ProfileSearchKeywordsData> getKeywordsData() {
        return keywordsData;
    }

    public long getInitialLoginDateSecurityLevelID() {
        return initialLoginDateSecurityLevelID;
    }

    @Override
    public Set<Long> getSharedGroups() {
        return userSearchInfo.sharedGroups;
    }
    
    public String getBrand() {
        return brand;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public String getRole() {
        return role;
    }

    public String getJobTitle() {
        return jobTitle;
    }
    
    public String getFromSynchro() {
		return fromSynchro;
	}
}
