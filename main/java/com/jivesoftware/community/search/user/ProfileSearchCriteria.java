/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.search.user;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.search.SearchQueryCriteriaComponent;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.community.user.profile.ProfileSearchFilter;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.*;

/**
 * Contains the necessary information required to perform a user search.
 *
 * @javadoc api
 */
public class ProfileSearchCriteria {

    /**
     * Representation of a type of sort to be done on the results of a profile search
     */
    public interface Sort {

        /**
         * Returns the String key representation of the object
         *
         * @return String key
         */
        String getKey();
    }

    public enum DefaultSort implements Sort {
        CREATION_DATE("creationDate"),
        CREATION_DATE_ASC("creationDate"),
        INITIAL_LOGIN_DATE("initialLoginDate"),
        INITIAL_LOGIN_DATE_ASC("initialLoginDate"),
        STATUS_LEVEL("statusLevel"),
        USERNAME("username"),
        USER_ID("userID"),
        NAME("name"),
        LAST_NAME("lastName"),
        RELEVANCE("relevance"),
        EMAIL("email"),
        LAST_LOGGED_IN("lastLoggedIn"),
        MODIFICATION_DATE("modificationDate"),
        MODIFICATION_DATE_ASC("modificationDate"),
        LAST_PROFILE_UPDATE("lastProfileUpdate"),
        LAST_PROFILE_UPDATE_ASC("lastProfileUpdateAsc");

        private final String key;

        DefaultSort(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public static Sort getSort(String key) {
            for (DefaultSort sort : values()) {
                if (sort.key.equals(key)) {
                    return sort;
                }
            }

            return null;
        }
    }

    private static final Logger log = LogManager.getLogger(ProfileSearchCriteria.class);

    private final String keywords;
    private final long filterUserID;
    private final String prefix;
    private final List<ProfileSearchFilter> filters;
    private final Set<String> tags;
    private final boolean searchUsername;
    private final boolean searchName;
    private final boolean searchNamePhonetically;
    private boolean searchNameUsingNGrams;
    private final boolean searchEmail;
    private final boolean searchProfile;
    private final long communityID;
    private final Sort sort;
    private final long minCreationDate;
    private final long maxCreationDate;
    private final long minLastProfileUpdate;
    private final long maxLastProfileUpdate;
    private final long minModificationDate;
    private final long maxModificationDate;
    private final boolean returnDisabledUsers;
    private final boolean returnRegularUsers;
    private final boolean returnExternalUsers;
    private final boolean returnPartnerUsers;
    private final boolean returnOnlineUsers;
    private final int start;
    private final int range;
    private final boolean returnCountOnly;
    private final boolean returnUserIDsOnly;
    private final boolean returnActiveUserIDsOnly;
    private final boolean calculateFacets;
    private final boolean includeAppliedFacets;
    private final Locale userLocale;
    private final long querierID;
    private final List<EntityDescriptor> entities;
    private final boolean preserveEntityOrder;
    private final List<SearchQueryCriteriaComponent> components;
    private final Map<String, String> properties;
    private final String fromSynchro;

    public ProfileSearchCriteria(ProfileSearchCriteriaBuilder builder) {
        this.keywords = builder.getKeywords();
        this.filterUserID = builder.getFilterUserID();
        this.prefix = builder.getPrefix();
        this.filters = builder.getFilters() == null ? Collections.<ProfileSearchFilter>emptyList() : Lists.newLinkedList(builder.getFilters());
        this.tags = builder.getTags() == null ? Collections.<String>emptySet() : Sets.newHashSet(builder.getTags());
        this.searchUsername = builder.isSearchUsername();
        this.searchName = builder.isSearchName();
        this.searchNamePhonetically = builder.isSearchNamePhonetically();
        this.searchNameUsingNGrams = builder.isSearchNameUsingNGrams();
        this.searchEmail = builder.isSearchEmail();
        this.searchProfile = builder.isSearchProfile();
        this.communityID = builder.getCommunityID();
        this.sort = builder.getSort();
        this.minCreationDate = builder.getMinCreationDate() != null ? builder.getMinCreationDate().getTime() : -1L;
        this.maxCreationDate = builder.getMaxCreationDate() != null ? builder.getMaxCreationDate().getTime() : -1L;
        this.minLastProfileUpdate = builder.getMinLastProfileUpdate() != null ? builder.getMinLastProfileUpdate().getTime() : -1L;
        this.maxLastProfileUpdate = builder.getMaxLastProfileUpdate() != null ? builder.getMaxLastProfileUpdate().getTime() : -1L;
        this.minModificationDate = builder.getMinModificationDate() != null ? builder.getMinModificationDate().getTime() : -1L;
        this.maxModificationDate = builder.getMaxModificationDate() != null ? builder.getMaxModificationDate().getTime() : -1L;
        this.returnDisabledUsers = builder.isReturnDisabledUsers();
        this.returnRegularUsers = builder.isReturnRegularUsers();
        this.returnExternalUsers = builder.isReturnExternalUsers();
        this.returnPartnerUsers = builder.isReturnPartnerUsers();
        this.returnOnlineUsers = builder.isReturnOnlineUsers();
        this.start = builder.getStart();
        this.range = builder.getRange();
        this.returnCountOnly = builder.isReturnCountOnly();
        this.returnUserIDsOnly = builder.isReturnUserIDsOnly();
        this.returnActiveUserIDsOnly = builder.isReturnActiveUserIDsOnly();
        this.calculateFacets = builder.isCalculateFacets();
        this.includeAppliedFacets = builder.isIncludeAppliedFacets();
        this.userLocale = builder.getUserLocale();
        this.querierID = builder.getQuerierID();
        this.entities = Lists.newLinkedList(builder.getEntities());
        this.preserveEntityOrder = builder.isPreserveEntityOrder();
        this.components = builder.getComponents() == null ? Collections.<SearchQueryCriteriaComponent>emptyList() : Lists.newLinkedList(builder.getComponents());
        if (!this.isSearchEmail() && !this.isSearchName() && !this.isSearchProfile() && !this.isSearchUsername()) {
            throw new IllegalArgumentException("At least one of the following must be true: search email, search name, search profile, or search username.");
        }
        this.properties = builder.getProperties();
        this.fromSynchro = builder.getFromSynchro();
    }

    /**
     * Gets the keywords for the user search.
     *
     * @return the keywords for the user search.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Returns the filterUserID for the search which is specified when the search is intended to find users similar
     * to a specified user
     *
     * @return user ID of the user to find similar results for
     */
    public long getFilterUserID() {
        return filterUserID;
    }

    /**
     * Get a single-character prefix used to filter names, such as "A" which would return all users with first names, last
     * names, and usernames beginning in "A".
     *
     * @return a single character String.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets a list of {@link ProfileSearchFilter}s used to filter user results for this query.
     *
     * @return a list of {@link ProfileSearchFilter}s used to filter user results for this query.
     */
    public List<ProfileSearchFilter> getFilters() {
        return Lists.newLinkedList(filters);
    }

    /**
     * Gets the sort used for this query.
     * @return sorting strategy for this query.
     */
    public Sort getSort() {
        return sort;
    }

    /**
     * Get the minimum document creation date for this query.
     * @return
     */
    public Date getMinCreationDate() {
        if (minCreationDate != -1L) {
            return new Date(minCreationDate);
        }

        return null;
    }

    /**
     * Get the maximum document creation date for this query.
     * @return
     */
    public Date getMaxCreationDate() {
        if (maxCreationDate != -1L) {
            return new Date(maxCreationDate);
        }

        return null;
    }

    /**
     * Get the minimum last profile update for this query.
     * @return
     */
    public Date getMinLastProfileUpdate() {
        if (minLastProfileUpdate != -1L) {
            return new Date(minLastProfileUpdate);
        }
        return null;
    }

    /**
     * Get the maximum last profile update for this query.
     * @return
     */
    public Date getMaxLastProfileUpdate() {
        if (maxLastProfileUpdate != -1L) {
            return new Date(maxLastProfileUpdate);
        }
        return null;
    }

    /**
     * Get the minimum document modification date for this query.
     * @return
     */
    public Date getMinModificationDate() {
        if (minModificationDate != -1L) {
            return new Date(minModificationDate);
        }

        return null;
    }

    /**
     * Get the maximum document modification date for this query.
     * @return
     */
    public Date getMaxModificationDate() {
        if (maxModificationDate != -1L) {
            return new Date(maxModificationDate);
        }

        return null;
    }

    /**
     * Get the tags for this query.  User must have these tags to appear in results.
     * @return
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Gets the community used for this query. Null unless the user wants to find an expert
     * in a specific community.
     * @return the community the user wants to find an expert in.
     */
    public long getCommunityID() {
        return communityID;
    }

    /**
     * Returns <tt>true</tt> if the user's username field is to be searched, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the user's username field is to be searched, <tt>false</tt> otherwise.
     */
    public boolean isSearchUsername() {
        return searchUsername;
    }

    /**
     * Returns <tt>true</tt> if the user's name field is to be searched, <tt>false</tt> otherwise.
     * Note: The user's name field will not be searchable regardless of this value if
     * {@link com.jivesoftware.base.User#isNameVisible()} is <tt>false</tt>.
     *
     * @return <tt>true</tt> if the user's name field is to be searched, <tt>false</tt> otherwise.
     */
    public boolean isSearchName() {
        return searchName;
    }

     /**
     * Returns <tt>true</tt> if the user's name field is to be searched phonetically, <tt>false</tt> otherwise.
     * Note: The user's name field will not be searchable regardless of this value if
     * {@link com.jivesoftware.base.User#isNameVisible()} is <tt>false</tt>.
     *
     * @return <tt>true</tt> if the user's name field is to be searched phonetically, <tt>false</tt> otherwise.
     */
    public boolean isSearchNamePhonetically() {
        return searchNamePhonetically;
    }

    /**
     * Returns <tt>true</tt> if the user's name field is to be searched using NGrams, <tt>false</tt> otherwise.
     * Note: The user's name field will not be searchable regardless of this value if
     * {@link com.jivesoftware.base.User#isNameVisible()} is <tt>false</tt>.
     *
     * @return <tt>true</tt> if the user's name field is to be searched using NGrams, <tt>false</tt> otherwise.
     */
    public boolean isSearchNameUsingNGrams() {
        return searchNameUsingNGrams;
    }

    /**
     * Returns <tt>true</tt> if the user's email field is to be searched, <tt>false</tt> otherwise.
     * Note: The user's email field will not be searchable regardless of this value if
     * {@link com.jivesoftware.base.User#isEmailVisible()} is <tt>false</tt>.
     *
     * @return <tt>true</tt> if the user's email field is to be searched, <tt>false</tt> otherwise.
     */
    public boolean isSearchEmail() {
        return searchEmail;
    }

    /**
     * Returns <tt>true</tt> if the user's profile field values are to be searched, <tt>false</tt> otherwise.
     * Note: The user's profile field values will not be searchable regardless of this value if
     * {@link ProfileField#isVisibleToUsers()} is <tt>false</tt>.
     *
     * @return <tt>true</tt> if the user's profile field values are to be searched, <tt>false</tt> otherwise.
     */
    public boolean isSearchProfile() {
        return searchProfile;
    }

    /**
     * Get the user performing the query.
     *
     * @return the user performing the query.
     */
    public long getQuerierID() {
        return querierID;
    }

    /**
     * Return <tt>True</tt> if the search returns disabled users.
     *
     * @return true if the search returns disabled users, false otherwise.
     */
    public boolean isReturnDisabledUsers() {
        return returnDisabledUsers;
    }

    /**
     * Return <tt>True</tt> if the search returns regular users.
     * @return true if the search returns regular users, false otherwise.
     */
    public boolean isReturnRegularUsers() {
        return returnRegularUsers;
    }

    /**
     * Return <tt>True</tt> if the search returns external (Cloud) users.
     *
     * @return true if the search returns external users, false otherwise.
     */
    public boolean isReturnExternalUsers() {
        return returnExternalUsers;
    }

    /**
     * Return <tt>True</tt> if the search returns partner users.
     *
     * @return true if the search returns partner users, false otherwise.
     */
    public boolean isReturnPartnerUsers() {
        return returnPartnerUsers;
    }

    /**
     * Return <tt>True</tt> if the search returns online users only.
     *
     * @return true if the search returns online users only, false otherwise.
     */
    public boolean isReturnOnlineUsers() {
        return returnOnlineUsers;
    }

    /**
     * The starting index of returned user results, with respect to total hits.  This is used for pagination.
     * Default is zero.
     *
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     * The number of returned user results.  Used together with 'start' for pagination. Default is Integer.MAX_VALUE,
     * which will return all available results.
     *
     * @return
     */
    public int getRange() {
        return range;
    }

    /**
     * If count only, don't populate userIDs or users in the returned ProfileSearchResults object.  Default is false.
     *
     * @return
     */
    public boolean isReturnCountOnly() {
        return returnCountOnly;
    }

    /**
     * If userIDs only, don't populate users in the returned ProfileSearchResults object.  Default is false.
     *
     * @return
     */
    public boolean isReturnUserIDsOnly() {
        return returnUserIDsOnly;
    }

    /**
     * If true, only return userIDs of "active" users, meaning users who have logged in at least once.
     *
     * @return
     */
    public boolean isReturnActiveUserIDsOnly() {
        return returnActiveUserIDsOnly;
    }

    public boolean isCalculateFacets() {
        return calculateFacets;
    }

    public boolean isIncludeAppliedFacets() {
        return includeAppliedFacets;
    }

    /**
     * Returns a list of entity descriptors by which the final result set will be intersected.
     *
     * @return a set of {@link com.jivesoftware.community.EntityDescriptor}s.
     */
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

    /**
     * Returns the locale of the user who has created this query, or null if one has not been set.
     *
     * @return the locale of the user who has created this query, or null if one has not been set.
     */
    public Locale getUserLocale() {
        return userLocale;
    }

    public List<SearchQueryCriteriaComponent> getComponents() {
        return Lists.newLinkedList(components);
    }

    /**
     * @return true if at least one tag has been specified, false if no tags have been used as criteria
     */
    public boolean areTagsSpecified() {
        if (tags == null) {
            return false;
        }
        for (String tag : tags) {
            if (StringUtils.isNotBlank(tag)) {
                return true;
            }
        }
        return false;
    }
    public Map<String, String> getProperties() {
        return properties;
    }

    
    public String getFromSynchro() {
		return fromSynchro;
	}

    /**
     * Check whether or not this query has filters with non-null values.
     * @return true, if any non-null filters, false otherwise
     */
    public boolean isFiltered() {
        if (StringUtils.isNotBlank(this.prefix)) {
            return true;
        }
        if (areTagsSpecified()) {
            return true;
        }
        if (this.communityID != -1L) {
            return true;
        }
        if (this.maxCreationDate != -1L || this.minCreationDate != -1L) {
            return true;
        }
        if (this.isReturnDisabledUsers() || this.isReturnExternalUsers() || this.isReturnActiveUserIDsOnly()
                || this.isReturnOnlineUsers() || this.isReturnPartnerUsers()) {
            return true;
        }
        if (this.filters != null) {
            for (ProfileSearchFilter filter : filters) {
                if (!filter.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether or not the query has keywords, a prefix, or any filters, and is not community or date specific.
     * @return if the query matches the conditions above.
     */
    public boolean isBlank() {
        return StringUtils.isBlank(this.keywords) && !isFiltered();
    }

    @Override
    public int hashCode() {
        int result = keywords != null ? keywords.hashCode() : 0;
        result = 31 * result + (int) (filterUserID ^ (filterUserID >>> 32));
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (filters != null ? filters.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (searchUsername ? 1 : 0);
        result = 31 * result + (searchName ? 1 : 0);
        result = 31 * result + (searchNamePhonetically ? 1 : 0);
        result = 31 * result + (searchNameUsingNGrams ? 1 : 0);
        result = 31 * result + (searchEmail ? 1 : 0);
        result = 31 * result + (searchProfile ? 1 : 0);
        result = 31 * result + (int) (communityID ^ (communityID >>> 32));
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (int) (minCreationDate ^ (minCreationDate >>> 32));
        result = 31 * result + (int) (maxCreationDate ^ (maxCreationDate >>> 32));
        result = 31 * result + (int) (minModificationDate ^ (minModificationDate >>> 32));
        result = 31 * result + (int) (maxModificationDate ^ (maxModificationDate >>> 32));
        result = 31 * result + (returnDisabledUsers ? 1 : 0);
        result = 31 * result + (returnRegularUsers ? 1 : 0);
        result = 31 * result + (returnExternalUsers ? 1 : 0);
        result = 31 * result + (returnPartnerUsers ? 1 : 0);
        result = 31 * result + (returnOnlineUsers ? 1 : 0);
        result = 31 * result + start;
        result = 31 * result + range;
        result = 31 * result + (returnCountOnly? 1 : 0);
        result = 31 * result + (returnUserIDsOnly ? 1 : 0);
        result = 31 * result + (returnActiveUserIDsOnly ? 1 : 0);
        result = 31 * result + (calculateFacets ? 1 : 0);
        result = 31 * result + (userLocale != null ? userLocale.hashCode() : 0);
        result = 31 * result + (int) (querierID ^ (querierID >>> 32));
        result = 31 * result + (components != null ? components.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("ProfileSearchCriteria");
        sb.append("{keywords='").append(keywords).append('\'');
        sb.append(", filterUserID=").append(filterUserID);
        sb.append(", prefix='").append(prefix).append('\'');
        sb.append(", filters=").append(filters);
        sb.append(", tags=").append(tags);
        sb.append(", searchUsername=").append(searchUsername);
        sb.append(", searchName=").append(searchName);
        sb.append(", searchNamePhonetically=").append(searchNamePhonetically);
        sb.append(", searchNameUsingNGrams=").append(searchNameUsingNGrams);
        sb.append(", searchEmail=").append(searchEmail);
        sb.append(", searchProfile=").append(searchProfile);
        sb.append(", communityID=").append(communityID);
        sb.append(", sort=").append(sort);
        sb.append(", minCreationDate=").append(minCreationDate);
        sb.append(", maxCreationDate=").append(maxCreationDate);
        sb.append(", minModificationDate=").append(minModificationDate);
        sb.append(", maxModificationDate=").append(maxModificationDate);
        sb.append(", returnDisabledUsers=").append(returnDisabledUsers);
        sb.append(", returnRegularUsers=").append(returnRegularUsers);
        sb.append(", returnExternalUsers=").append(returnExternalUsers);
        sb.append(", returnPartnerUsers=").append(returnPartnerUsers);
        sb.append(", returnOnlineUsers=").append(returnOnlineUsers);
        sb.append(", start=").append(start);
        sb.append(", range=").append(range);
        sb.append(", returnCountOnly=").append(returnCountOnly);
        sb.append(", returnUserIDsOnly=").append(returnUserIDsOnly);
        sb.append(", returnActiveUserIDsOnly=").append(returnActiveUserIDsOnly);
        sb.append(", calculateFacets=").append(calculateFacets);
        sb.append(", includeAppliedFacets=").append(includeAppliedFacets);
        sb.append(", userLocale=").append(userLocale);
        sb.append(", querierID=").append(querierID);
        sb.append(", preserveEntityOrder=").append(preserveEntityOrder);
        sb.append(", components=").append(components);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProfileSearchCriteria that = (ProfileSearchCriteria) o;

        if (calculateFacets != that.calculateFacets) {
            return false;
        }
        if (communityID != that.communityID) {
            return false;
        }
        if (filterUserID != that.filterUserID) {
            return false;
        }
        if (maxCreationDate != that.maxCreationDate) {
            return false;
        }
        if (minCreationDate != that.minCreationDate) {
            return false;
        }
        if (maxModificationDate != that.maxModificationDate) {
            return false;
        }
        if (minModificationDate != that.minModificationDate) {
            return false;
        }
        if (querierID != that.querierID) {
            return false;
        }
        if (range != that.range) {
            return false;
        }
        if (returnActiveUserIDsOnly != that.returnActiveUserIDsOnly) {
            return false;
        }
        if (returnCountOnly != that.returnCountOnly) {
            return false;
        }
        if (returnDisabledUsers != that.returnDisabledUsers) {
            return false;
        }
        if (returnRegularUsers != that.returnRegularUsers) {
            return false;
        }
        if (returnExternalUsers != that.returnExternalUsers) {
            return false;
        }
        if (returnPartnerUsers != that.returnPartnerUsers) {
            return false;
        }
        if (returnOnlineUsers != that.returnOnlineUsers) {
            return false;
        }
        if (returnUserIDsOnly != that.returnUserIDsOnly) {
            return false;
        }
        if (searchEmail != that.searchEmail) {
            return false;
        }
        if (searchName != that.searchName) {
            return false;
        }
        if (searchNamePhonetically != that.searchNamePhonetically) {
            return false;
        }
        if (searchNameUsingNGrams != that.searchNameUsingNGrams) {
            return false;
        }
        if (searchProfile != that.searchProfile) {
            return false;
        }
        if (searchUsername != that.searchUsername) {
            return false;
        }
        if (start != that.start) {
            return false;
        }
        if (components != null ? !components.equals(that.components) : that.components != null) {
            return false;
        }
        if (filters != null ? !filters.equals(that.filters) : that.filters != null) {
            return false;
        }
        if (keywords != null ? !keywords.equals(that.keywords) : that.keywords != null) {
            return false;
        }
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) {
            return false;
        }
        if (sort != null ? !sort.equals(that.sort) : that.sort != null) {
            return false;
        }
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) {
            return false;
        }
        if (userLocale != null ? !userLocale.equals(that.userLocale) : that.userLocale != null) {
            return false;
        }

        return true;
    }
}

