/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.search.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.search.SearchQueryCriteriaComponent;
import com.jivesoftware.community.user.profile.ProfileSearchFilter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.*;
/**
 * Mutable object to be used to construct an immutable {@link com.jivesoftware.community.search.user.ProfileSearchCriteria}
 */
public class ProfileSearchCriteriaBuilder {

    private static final Logger log = LogManager.getLogger(ProfileSearchCriteriaBuilder.class);

    private final long querierID;

    private String keywords;
    private long filterUserID = AnonymousUser.ANONYMOUS_ID;
    private String prefix;
    private List<ProfileSearchFilter> filters;
    private Set<String> tags;
    private boolean searchUsername = true;
    private boolean searchName = true;
    private boolean searchNamePhonetically = false;
    private boolean searchNameUsingNGrams = true;
    private boolean searchEmail = true;
    private boolean searchProfile = true;
    private long communityID = -1L;
    private ProfileSearchCriteria.Sort sort = ProfileSearchCriteria.DefaultSort.RELEVANCE;
    private long minCreationDate = -1L;
    private long maxCreationDate = -1L;
    private long minLastProfileUpdate = -1L;
    private long maxLastProfileUpdate = -1L;
    private long minModificationDate = -1L;
    private long maxModificationDate = -1L;
    private boolean returnDisabledUsers;
    private boolean returnRegularUsers = true;
    private boolean returnExternalUsers;
    private boolean returnPartnerUsers = true;
    private boolean returnOnlineUsers;
    private int start = 0;
    private int range = -1;
    private boolean returnCountOnly;
    private boolean returnUserIDsOnly;
    private boolean returnActiveUserIDsOnly;
    private boolean calculateFacets;
    private boolean includeAppliedFacets;
    private List<EntityDescriptor> entities = Lists.newLinkedList();
    private boolean preserveEntityOrder = false;
    private Locale userLocale;
    private List<SearchQueryCriteriaComponent> components = Lists.newLinkedList();
    
    private Map<String, String> properties = new HashMap<String, String>();
    private String fromSynchro;

    @JsonCreator
    public ProfileSearchCriteriaBuilder(@JsonProperty("querierUserID") long querierUserID) {
        this.querierID = querierUserID;
    }

    public ProfileSearchCriteriaBuilder(long querierUserID, String keywords) {
        this.querierID = querierUserID;
        this.keywords = keywords;
    }

    public ProfileSearchCriteriaBuilder(ProfileSearchCriteria criteria) {
        this.querierID = criteria.getQuerierID();
        this.keywords = criteria.getKeywords();
        this.filterUserID = criteria.getFilterUserID();
        this.prefix = criteria.getPrefix();
        this.filters = criteria.getFilters();
        this.tags = criteria.getTags();
        this.searchUsername = criteria.isSearchUsername();
        this.searchName = criteria.isSearchName();
        this.searchNamePhonetically = criteria.isSearchNamePhonetically();
        this.searchNameUsingNGrams = criteria.isSearchNameUsingNGrams();
        this.searchEmail = criteria.isSearchEmail();
        this.searchProfile = criteria.isSearchProfile();
        this.communityID = criteria.getCommunityID();
        this.sort = criteria.getSort();
        this.minCreationDate = criteria.getMinCreationDate() != null ? criteria.getMinCreationDate().getTime() : -1L;
        this.maxCreationDate = criteria.getMaxCreationDate() != null ? criteria.getMaxCreationDate().getTime() : -1L;
        this.minLastProfileUpdate = criteria.getMinLastProfileUpdate() != null ? criteria.getMinLastProfileUpdate().getTime() : -1L;
        this.maxLastProfileUpdate = criteria.getMaxLastProfileUpdate() != null ? criteria.getMaxLastProfileUpdate().getTime() : -1L;
        this.minModificationDate = criteria.getMinModificationDate() != null ? criteria.getMinModificationDate().getTime() : -1L;
        this.maxModificationDate = criteria.getMaxModificationDate() != null ? criteria.getMaxModificationDate().getTime() : -1L;
        this.returnDisabledUsers = criteria.isReturnDisabledUsers();
        this.returnRegularUsers = criteria.isReturnRegularUsers();
        this.returnExternalUsers = criteria.isReturnExternalUsers();
        this.returnPartnerUsers = criteria.isReturnPartnerUsers();
        this.returnOnlineUsers = criteria.isReturnOnlineUsers();
        this.start = criteria.getStart();
        this.range = criteria.getRange();
        this.returnCountOnly = criteria.isReturnCountOnly();
        this.returnUserIDsOnly = criteria.isReturnUserIDsOnly();
        this.returnActiveUserIDsOnly = criteria.isReturnActiveUserIDsOnly();
        this.calculateFacets = criteria.isCalculateFacets();
        this.includeAppliedFacets = criteria.isIncludeAppliedFacets();
        this.entities.addAll(criteria.getEntities());
        this.preserveEntityOrder = criteria.isPreserveEntityOrder();
        this.userLocale = criteria.getUserLocale();
        this.components.addAll(criteria.getComponents());
        this.properties.putAll(criteria.getProperties());
        this.fromSynchro = criteria.getFromSynchro();
    }

    public long getQuerierID() {
        return querierID;
    }

    public String getKeywords() {
        return keywords;
    }

    public ProfileSearchCriteriaBuilder setKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }

    public long getFilterUserID() {
        return filterUserID;
    }

    public ProfileSearchCriteriaBuilder setFilterUserID(long userID) {
        this.filterUserID = userID;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ProfileSearchCriteriaBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public List<ProfileSearchFilter> getFilters() {
        return filters;
    }

    public ProfileSearchCriteriaBuilder setFilters(List<ProfileSearchFilter> filters) {
        this.filters = filters;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public ProfileSearchCriteriaBuilder setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public ProfileSearchCriteriaBuilder setTag(String tag) {
        this.tags = Sets.newHashSet();
        tags.add(tag);
        return this;
    }

    public boolean isSearchUsername() {
        return searchUsername;
    }

    public ProfileSearchCriteriaBuilder setSearchUsername(boolean searchUsername) {
        this.searchUsername = searchUsername;
        return this;
    }

    public boolean isSearchName() {
        return searchName;
    }

    public ProfileSearchCriteriaBuilder setSearchName(boolean searchName) {
        this.searchName = searchName;
        return this;
    }

    public boolean isSearchNamePhonetically() {
        return searchNamePhonetically;
    }

    public ProfileSearchCriteriaBuilder setSearchNamePhonetically(boolean searchNamePhonetically) {
        this.searchNamePhonetically = searchNamePhonetically;
        return this;
    }

    public boolean isSearchNameUsingNGrams() {
        return searchNameUsingNGrams;
    }

    public ProfileSearchCriteriaBuilder setSearchNameUsingNGrams(boolean searchNameUsingNGrams) {
        this.searchNameUsingNGrams = searchNameUsingNGrams;
        return this;
    }

    public boolean isSearchEmail() {
        return searchEmail;
    }

    public ProfileSearchCriteriaBuilder setSearchEmail(boolean searchEmail) {
        this.searchEmail = searchEmail;
        return this;
    }

    public boolean isSearchProfile() {
        return searchProfile;
    }

    public ProfileSearchCriteriaBuilder setSearchProfile(boolean searchProfile) {
        this.searchProfile = searchProfile;
        return this;
    }

    public long getCommunityID() {
        return communityID;
    }

    public ProfileSearchCriteriaBuilder setCommunityID(Long communityID) {
        this.communityID = communityID;
        return this;
    }

    public ProfileSearchCriteria.Sort getSort() {
        return sort;
    }

    public ProfileSearchCriteriaBuilder setSort(ProfileSearchCriteria.Sort sort) {
        this.sort = sort;
        return this;
    }

    public Date getMinCreationDate() {
        if (minCreationDate != -1L) {
            return new Date(minCreationDate);
        }

        return null;
    }

    public ProfileSearchCriteriaBuilder setMinCreationDate(Date minCreationDate) {
        if (minCreationDate == null) {
            this.minCreationDate = -1L;
        }
        else {
            this.minCreationDate = minCreationDate.getTime();
        }
        return this;
    }

    public Date getMaxCreationDate() {
        if (maxCreationDate != -1L) {
            return new Date(maxCreationDate);
        }

        return null;
    }

    public ProfileSearchCriteriaBuilder setMaxCreationDate(Date maxCreationDate) {
        if (maxCreationDate == null) {
            this.maxCreationDate = -1L;
        }
        else {
            this.maxCreationDate = maxCreationDate.getTime();
        }
        return this;
    }

    public Date getMinLastProfileUpdate() {
        if (minLastProfileUpdate != -1L) {
            return new Date(minLastProfileUpdate);
        }

        return null;
    }

    public ProfileSearchCriteriaBuilder setMinLastProfileUpdate(Date minLastProfileUpdate) {
        if (minLastProfileUpdate == null) {
            this.minLastProfileUpdate = -1L;
        }
        else {
            this.minLastProfileUpdate = minLastProfileUpdate.getTime();
        }
        return this;
    }

    public Date getMaxLastProfileUpdate() {
        if (maxLastProfileUpdate != -1L) {
            return new Date(maxLastProfileUpdate);
        }

        return null;
    }

    public ProfileSearchCriteriaBuilder setMaxLastProfileUpdate(Date maxLastProfileUpdate) {
        if (maxLastProfileUpdate == null) {
            this.maxLastProfileUpdate = -1L;
        }
        else {
            this.maxLastProfileUpdate = maxLastProfileUpdate.getTime();
        }
        return this;
    }

    public Date getMinModificationDate() {
        if (minModificationDate != -1L) {
            return new Date(minModificationDate);
        }

        return null;
    }

    public ProfileSearchCriteriaBuilder setMinModificationDate(Date minModificationDate) {
        if (minModificationDate == null) {
            this.minModificationDate = -1L;
        }
        else {
            this.minModificationDate = minModificationDate.getTime();
        }
        return this;
    }

    public Date getMaxModificationDate() {
        if (maxModificationDate != -1L) {
            return new Date(maxModificationDate);
        }

        return null;
    }

    public ProfileSearchCriteriaBuilder setMaxModificationDate(Date maxModificationDate) {
        if (maxModificationDate == null) {
            this.maxModificationDate = -1L;
        }
        else {
            this.maxModificationDate = maxModificationDate.getTime();
        }
        return this;
    }

    public boolean isReturnDisabledUsers() {
        return returnDisabledUsers;
    }

    public ProfileSearchCriteriaBuilder setReturnDisabledUsers(boolean returnDisabledUsers) {
        this.returnDisabledUsers = returnDisabledUsers;
        return this;
    }

    public boolean isReturnRegularUsers() {
        return returnRegularUsers;
    }

    public boolean isReturnExternalUsers() {
        return returnExternalUsers;
    }

    public ProfileSearchCriteriaBuilder setReturnRegularUsers(boolean returnRegularUsers) {
        this.returnRegularUsers = returnRegularUsers;
        return this;
    }

    public ProfileSearchCriteriaBuilder setReturnExternalUsers(boolean returnExternalUsers) {
        this.returnExternalUsers = returnExternalUsers;
        return this;
    }

    public boolean isReturnPartnerUsers() {
        return returnPartnerUsers;
    }

    public ProfileSearchCriteriaBuilder setReturnPartnerUsers(boolean returnPartnerUsers) {
        this.returnPartnerUsers = returnPartnerUsers;
        return this;
    }

    public boolean isReturnOnlineUsers() {
        return returnOnlineUsers;
    }

    public ProfileSearchCriteriaBuilder setReturnOnlineUsers(boolean returnOnlineUsers) {
        this.returnOnlineUsers = returnOnlineUsers;
        return this;
    }

    public int getStart() {
        return start;
    }

    public ProfileSearchCriteriaBuilder setStart(int start) {
        this.start = start;
        return this;
    }

    public int getRange() {
        return range;
    }

    public ProfileSearchCriteriaBuilder setRange(int range) {
        this.range = range;
        return this;
    }

    public boolean isReturnCountOnly() {
        return returnCountOnly;
    }

    public ProfileSearchCriteriaBuilder setReturnCountOnly(boolean returnCountOnly) {
        this.returnCountOnly = returnCountOnly;
        return this;
    }

    public boolean isReturnUserIDsOnly() {
        return returnUserIDsOnly;
    }

    public ProfileSearchCriteriaBuilder setReturnUserIDsOnly(boolean returnUserIDsOnly) {
        this.returnUserIDsOnly = returnUserIDsOnly;
        return this;
    }

    public boolean isReturnActiveUserIDsOnly() {
        return returnActiveUserIDsOnly;
    }

    public ProfileSearchCriteriaBuilder setReturnActiveUserIDsOnly(boolean returnActiveUserIDsOnly) {
        this.returnActiveUserIDsOnly = returnActiveUserIDsOnly;
        return this;
    }

    public boolean isCalculateFacets() {
        return calculateFacets;
    }

    public ProfileSearchCriteriaBuilder setCalculateFacets(boolean calculateFacets) {
        this.calculateFacets = calculateFacets;
        return this;
    }

    public boolean isIncludeAppliedFacets() {
        return includeAppliedFacets;
    }

    public ProfileSearchCriteriaBuilder setIncludeAppliedFacets(boolean includeAppliedFacets) {
        this.includeAppliedFacets = includeAppliedFacets;
        return this;
    }

    public Locale getUserLocale() {
        return userLocale;
    }

    public ProfileSearchCriteriaBuilder setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
        return this;
    }

    public ProfileSearchCriteriaBuilder addEntity(EntityDescriptor entityDescriptor) {
        this.entities.add(entityDescriptor);
        return this;
    }

    public ProfileSearchCriteriaBuilder setEntities(List<? extends EntityDescriptor> entities) {
        this.entities = Lists.newLinkedList(entities);
        return this;
    }

    public List<EntityDescriptor> getEntities() {
        return entities;
    }

    /**
     * Indicates whether the order of the objects returned matches the entity restriction set provided
     * @return true if order is preserved
     */
    public boolean isPreserveEntityOrder() {
        return preserveEntityOrder;
    }
    
    public ProfileSearchCriteriaBuilder setPreserveEntityOrder(boolean preserveEntityOrder) {
        this.preserveEntityOrder = preserveEntityOrder;
        return this;
    }

    public ProfileSearchCriteriaBuilder addComponent(SearchQueryCriteriaComponent component) {
        this.components.add(component);
        return this;
    }

    public List<SearchQueryCriteriaComponent> getComponents() {
        return components;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    
    public String getFromSynchro() {
		return fromSynchro;
	}

	public void setFromSynchro(String fromSynchro) {
		this.fromSynchro = fromSynchro;
	}
    public ProfileSearchCriteria build() {
        return new ProfileSearchCriteria(this);
    }

    /**
     * Constructs a {@link com.jivesoftware.community.search.user.ProfileSearchCriteria} object to be used to query
     * a result of the newest Jive SBS users based on their creation date from the search index
     *
     * @param querierUserID the user performing the query
     * @param start the start index for the results
     * @param range the number of results to retrieve
     * @return a criteria object specifying settings to retrieve the newest users
     */
    public static ProfileSearchCriteria buildNewestMembersCriteria(long querierUserID, int start, int range) {
        return new ProfileSearchCriteriaBuilder(querierUserID)
                .setSort(ProfileSearchCriteria.DefaultSort.CREATION_DATE)
                .setSearchUsername(false)
                .setSearchName(true)
                .setSearchNamePhonetically(false)
                .setSearchProfile(false)
                .setSearchEmail(false)
                .setReturnDisabledUsers(false)
                .setReturnOnlineUsers(false)
                .setReturnExternalUsers(false)
                .setReturnPartnerUsers(false)
                .setStart(start)
                .setRange(range)
                .build();
    }

    /**
     * Constructs a {@link com.jivesoftware.community.search.user.ProfileSearchCriteria} object to be used to query a
     * result of users with the top status levels from the search index
     *
     * @param querierUserID the user performing the query
     * @param start the start index for the results
     * @param range the number of results to retrieve
     * @return a criteria object specifying the settings to retrieve users sorted by their status level
     */
    public static ProfileSearchCriteria buildTopMembersCriteria(long querierUserID, int start, int range) {
        return new ProfileSearchCriteriaBuilder(querierUserID)
                .setSort(ProfileSearchCriteria.DefaultSort.STATUS_LEVEL)
                .setSearchUsername(false)
                .setSearchName(true)
                .setSearchNamePhonetically(false)
                .setSearchProfile(false)
                .setSearchEmail(false)
                .setReturnDisabledUsers(false)
                .setReturnOnlineUsers(false)
                .setReturnExternalUsers(false)
                .setReturnPartnerUsers(false)
                .setStart(start)
                .setRange(range)
                .build();
    }
}
