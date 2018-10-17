/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jivesoftware.community.EntityDescriptor;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * A SearchQueryCriteria object encapsulates the constraints and requirements for a search query.
 */
public class SearchQueryCriteria {

    /**
     * Sort specifies the search index fields that can be used to sort search results
     */
    public interface Sort {

        /**
         * Returns a String key representation for the sort
         *
         * @return String
         */
        String getKey();
    }

    public enum DefaultSort implements Sort {
        RELEVANCE("relevance"), LIKES("likes"), RATING("rating"), SUBJECT("subject"), MODIFICATION_DATE("date");

        private String key;

        DefaultSort(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        /**
         * A helper method to get a specific Sort object from its key
         *
         * @param key the key value of the Sort object
         * @return the Sort object corresponding to the key value. <tt>NULL</tt> if the key is invalid.
         */
        public static DefaultSort getSort(String key) {
            for (DefaultSort sort : values()) {
                if (sort.key.equals(key)) {
                    return sort;
                }
            }

            return null;
        }
    }

    /**
     * Represents the order in which search results will be returned
     */
    public enum SortOrder {
        DESCENDING(0), ASCENDING(1);

        private int key = 0;

        SortOrder(int key) {
            this.key = key;
        }

        public int getKey() {
            return key;
        }

        /**
         * A helper method to get a specific SortOrder object from its key
         *
         * @param key the key value of the SortOrder object
         * @return the SortOrder object corresponding to the key value. <tt>NULL</tt> if the key is invalid.
         */
        public static SortOrder getSortOrder(int key) {
            switch (key) {
                case 0: return DESCENDING;
                case 1: return ASCENDING;
                default: return null;
            }
        }
    }

    private final String queryString;
    private final Set<Long> tagSetIDs;
    private final long beforeDate;
    private final long afterDate;
    private final Sort sort;
    private final SortOrder sortOrder;
    private final Map<SearchIndexField, String> fieldFilters;
    private final SearchIndexField searchedField;
    private final Set<Integer> objectTypes;
    private final boolean collapseParents;
    private final String language;
    private final long userID;
    private final Set<EntityDescriptor> containers;
    private final Set<EntityDescriptor> sources;
    private final Set<String> outcomeIds;
    private final Set<String> outcomeTypes;
    private final EntityDescriptor filteredObject;
    private final List<SearchQueryCriteriaComponent> components;
    private final List<EntityDescriptor> entities;
    private final boolean preserveEntityOrder;
    private final boolean performHighlighting;
    private final boolean socialSearch;
    private final boolean returnScore;
    private final TimeZone timeZone;
    private final Locale locale;
    private final long rtcAuthorID;
    private final Set<Integer> rtcObjectTypes;
    
    private final boolean documentsOnly;
    private final List<String> extendedProperties;
    private final boolean applyExtendedPropertyFilters;
    private final List<String> dateRange;

    public SearchQueryCriteria(SearchQueryCriteriaBuilder builder) {
        this.queryString = builder.getQueryString();
        this.tagSetIDs = Sets.newHashSet(builder.getTagSetIDs());
        this.beforeDate = builder.getBeforeDate();
        this.afterDate = builder.getAfterDate();
        this.sort = builder.getSort();
        this.sortOrder = builder.getSortOrder();
        this.objectTypes = Sets.newHashSet(builder.getObjectTypes());
        this.collapseParents = builder.isCollapseParents();
        this.language = builder.getLanguage();
        this.userID = builder.getUserID();
        this.containers = Sets.newHashSet(builder.getContainers());
        this.sources = Sets.newHashSet(builder.getSources());
        this.outcomeIds = Sets.newHashSet(builder.getOutcomeIds());
        this.outcomeTypes = Sets.newHashSet(builder.getOutcomeTypes());
        this.filteredObject =
                builder.getFilteredObject() != null ? new EntityDescriptor(builder.getFilteredObject()) : null;
        this.searchedField = builder.getSearchField();
        this.fieldFilters = Maps.newHashMap(builder.getFieldFilters());
        this.components = Lists.newLinkedList(builder.getComponents());
        this.entities = Lists.newLinkedList(builder.getEntities());
        this.preserveEntityOrder = builder.isPreserveEntityOrder();
        this.performHighlighting = builder.isPerformHighlighting();
        this.socialSearch = builder.isSocialSearch();
        this.returnScore = builder.returnScore();
        this.timeZone = builder.getTimeZone();
        this.locale = builder.getLocale();
        this.rtcAuthorID = builder.getRtcAuthorID();
        this.rtcObjectTypes = Sets.newHashSet(builder.getRtcObjectTypes());
        this.documentsOnly = builder.getDocumentsOnly();
        this.extendedProperties = builder.getExtendedProperties();
        this.applyExtendedPropertyFilters = builder.isApplyExtendedPropertyFilters();
        this.dateRange = builder.getDateRange();
    }

    /**
     * Returns the actual text string to query with.  Note that if a non null value is available from
     * {@link #getFilteredObject()}, this query string will not be used when a query is executed
     *
     * @return String
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Returns the date criteria which is used to include only content with a modification date before a particular
     * date
     *
     * @return {@link java.util.Date}
     */
    public Date getBeforeDate() {
        if (beforeDate > 0) {
            return new Date(beforeDate);
        }
        return null;
    }

    /**
     * Returns the date criteria which is used to include only content with a modification date after a particular
     * date
     *
     * @return {@link java.util.Date}
     */
    public Date getAfterDate() {
        if (afterDate > 0) {
            return new Date(afterDate);
        }
        return null;
    }

    /**
     * Returns the user ID which is used to include only content created by a particular user
     *
     * @return user ID
     */
    public long getUserID() {
        return userID;
    }

    /**
     * Returns a set of {@link com.jivesoftware.community.EntityDescriptor} representations of containers which is
     * used to limit results to only content that resides in a particular container or containers
     *
     * @return Set of {@link com.jivesoftware.community.EntityDescriptor}
     */
    public Set<EntityDescriptor> getContainers() {
        return Sets.newHashSet(containers);
    }

    /**
     * Returns a set of {@link com.jivesoftware.community.EntityDescriptor} representations of sources which is
     * used to limit results to only content that originated from a particular external source.
     *
     * @return Set of {@link com.jivesoftware.community.EntityDescriptor}
     */
    public Set<EntityDescriptor> getSources() {
        return Sets.newHashSet(sources);
    }

    /**
     * Returns a set of {@link java.lang.String} representations of outcome type ids which is
     * used to limit results to only content that includes the designated outcomes.
     *
     * @return Set of {@link java.lang.String}
     */
    public Set<String> getOutcomeIds() {
        return Sets.newHashSet(outcomeIds);
    }

    /**
     * Returns a set of {@link java.lang.String} representations of outcome type names which is
     * used to limit results to only content that includes the designated outcomes.
     *
     * @return Set of {@link java.lang.String}
     */
    public Set<String> getOutcomeTypes() {
        return Sets.newHashSet(outcomeTypes);
    }

    /**
     * Returns the {@link com.jivesoftware.community.EntityDescriptor} representation of the content object which is
     * used to perform a "More Like This" query where content that is deemed similar to the specified object is what
     * is returned by the query.
     *
     * @return {@link com.jivesoftware.community.EntityDescriptor}
     */
    public EntityDescriptor getFilteredObject() {
        if (filteredObject != null) {
            return new EntityDescriptor(filteredObject);
        }

        return null;
    }

    public Map<SearchIndexField, String> getFilteredFields() {
        return Maps.newHashMap(fieldFilters);
    }

    public Set<Long> getTagSetIDs() {
        return Sets.newHashSet(tagSetIDs);
    }

    /**
     * Returns the method by which search query results are to be sorted
     *
     * @return {@link com.jivesoftware.community.search.SearchQueryCriteria.Sort}
     */
    public Sort getSort() {
        return sort;
    }

    /**
     * Returns the order by which search query results are to be sorted
     *
     * @return {@link com.jivesoftware.community.search.SearchQueryCriteria.SortOrder}
     */
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    /**
     * Indicates whether the order of the objects returned matches the entity restriction set provided
     *
     * @return true if order is preserved
     */
    public boolean isPreserveEntityOrder() {
        return preserveEntityOrder;
    }

    /**
     * Returns whether search results should be "collapsed" if they have a specified parent.  This means that
     * if a query matches multiple pieces of content from a particular parent, only the first seen child piece of
     * content will be included in the results.  For example, if a query matches multiple messages from a particular
     * thread, only the first seen message will be in the results.
     *
     * @return
     */
    public boolean isCollapseParents() {
        return this.collapseParents;
    }

    /**
     * Returns the index field which is used to specify that the query be evaluated only against the content of a
     * particular field in the index rather than against the default search field
     *
     * @return {@link SearchIndexField}
     */
    public SearchIndexField getSearchedField() {
        return searchedField;
    }

    /**
     * Returns the Set of object type IDs which are used to limit the search results to only content of particular
     * types.
     *
     * @return Set of Integer
     */
    public Set<Integer> getObjectTypeIDs() {
        return Sets.newHashSet(objectTypes);
    }

    /**
     * Returns the language that the search query is to be executed in which is used to choose an appropriate
     * analyzer for parsing the search query text and result content
     *
     * @return String
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the list of {@link com.jivesoftware.community.search.SearchQueryCriteriaComponent}s which are used to pass
     * implementation specific search criteria information to the search functionality implementation.
     *
     * @return List of {@link com.jivesoftware.community.search.SearchQueryCriteriaComponent} objects
     */
    public List<SearchQueryCriteriaComponent> getComponents() {
        return Lists.newLinkedList(components);
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
     * Returns whether the highlighting information should be retrieved as part of the query
     *
     * @return true if highlights should be retrieved
     */
    public boolean isPerformHighlighting() {
        return performHighlighting;
    }

    /**
     * Returns whether the social signals should affect query results
     *
     * @return true if social search should be used
     */
    public boolean isSocialSearch() {
        return socialSearch;
    }

    /**
     * Returns whether the search scores for query results should be included
     *
     * @return true if search scores for query results should be included
     */
    public boolean returnScore() {
        return returnScore;
    }

    /**
     * Returns the time zone to be used for working with any Dates contained in this criteria object
     *
     * @return TimeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Returns the locale to be used for working with any Dates contained in this criteria object
     *
     * @return Locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the real-time author ID contained in this criteria object.
     *
     * @return user ID
     */
    public long getRtcAuthorID() {
        return rtcAuthorID;
    }

    /**
     * Returns the real-time object types contained in this criteria object.
     *
     * @return Set of object type IDs
     */
    public Set<Integer> getRtcObjectTypes() {
        return Sets.newHashSet(rtcObjectTypes);
    }

    public boolean getDocumentsOnly() {
        return documentsOnly;
    }

    public List<String> getExtendedProperties() {
        return extendedProperties;
    }

    public boolean isApplyExtendedPropertyFilters() {
        return applyExtendedPropertyFilters;
    }

    public List<String> getDateRange() {
        return dateRange;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SearchQueryCriteria that = (SearchQueryCriteria) o;

        if (afterDate != that.afterDate) {
            return false;
        }
        if (beforeDate != that.beforeDate) {
            return false;
        }
        if (collapseParents != that.collapseParents) {
            return false;
        }
        if (performHighlighting != that.performHighlighting) {
            return false;
        }
        if (socialSearch != that.socialSearch) {
            return false;
        }
        if (returnScore != that.returnScore) {
            return false;
        }
        if (preserveEntityOrder != that.preserveEntityOrder) {
            return false;
        }
        if (rtcAuthorID != that.rtcAuthorID) {
            return false;
        }
        if (userID != that.userID) {
            return false;
        }
        if (components != null ? !components.equals(that.components) : that.components != null) {
            return false;
        }
        if (containers != null ? !containers.equals(that.containers) : that.containers != null) {
            return false;
        }
        if (entities != null ? !entities.equals(that.entities) : that.entities != null) {
            return false;
        }
        if (fieldFilters != null ? !fieldFilters.equals(that.fieldFilters) : that.fieldFilters != null) {
            return false;
        }
        if (filteredObject != null ? !filteredObject.equals(that.filteredObject) : that.filteredObject != null) {
            return false;
        }
        if (language != null ? !language.equals(that.language) : that.language != null) {
            return false;
        }
        if (locale != null ? !locale.equals(that.locale) : that.locale != null) {
            return false;
        }
        if (objectTypes != null ? !objectTypes.equals(that.objectTypes) : that.objectTypes != null) {
            return false;
        }
        if (outcomeIds != null ? !outcomeIds.equals(that.outcomeIds) : that.outcomeIds != null) {
            return false;
        }
        if (outcomeTypes != null ? !outcomeTypes.equals(that.outcomeTypes) : that.outcomeTypes != null) {
            return false;
        }
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null) {
            return false;
        }
        if (rtcObjectTypes != null ? !rtcObjectTypes.equals(that.rtcObjectTypes) : that.rtcObjectTypes != null) {
            return false;
        }
        if (searchedField != null ? !searchedField.equals(that.searchedField) : that.searchedField != null) {
            return false;
        }
        if (sort != null ? !sort.equals(that.sort) : that.sort != null) {
            return false;
        }
        if (sortOrder != that.sortOrder) {
            return false;
        }
        if (sources != null ? !sources.equals(that.sources) : that.sources != null) {
            return false;
        }
        if (tagSetIDs != null ? !tagSetIDs.equals(that.tagSetIDs) : that.tagSetIDs != null) {
            return false;
        }
        if (timeZone != null ? !timeZone.equals(that.timeZone) : that.timeZone != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "SearchQueryCriteria{" +
                "queryString='" + queryString + '\'' +
                ", tagSetIDs=" + tagSetIDs +
                ", beforeDate=" + beforeDate +
                ", afterDate=" + afterDate +
                ", sort=" + sort +
                ", sortOrder=" + sortOrder +
                ", fieldFilters=" + fieldFilters +
                ", searchedField=" + searchedField +
                ", objectTypes=" + objectTypes +
                ", collapseParents=" + collapseParents +
                ", language='" + language + '\'' +
                ", userID=" + userID +
                ", containers=" + containers +
                ", sources=" + sources +
                ", outcomeIds=" + outcomeIds +
                ", outcomeTypes=" + outcomeTypes +
                ", filteredObject=" + filteredObject +
                ", components=" + components +
                ", entities=" + entities +
                ", preserveEntityOrder=" + preserveEntityOrder +
                ", performHighlighting=" + performHighlighting +
                ", socialSearch=" + socialSearch +
                ", returnScore=" + returnScore +
                ", timeZone=" + timeZone +
                ", locale=" + locale +
                ", rtcAuthorID=" + rtcAuthorID +
                ", rtcObjectTypes=" + rtcObjectTypes +
                '}';
    }

    @Override
    public int hashCode() {
        int result = queryString != null ? queryString.hashCode() : 0;
        result = 31 * result + (tagSetIDs != null ? tagSetIDs.hashCode() : 0);
        result = 31 * result + (int) (beforeDate ^ (beforeDate >>> 32));
        result = 31 * result + (int) (afterDate ^ (afterDate >>> 32));
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
        result = 31 * result + (fieldFilters != null ? fieldFilters.hashCode() : 0);
        result = 31 * result + (searchedField != null ? searchedField.hashCode() : 0);
        result = 31 * result + (objectTypes != null ? objectTypes.hashCode() : 0);
        result = 31 * result + (collapseParents ? 1 : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (int) (userID ^ (userID >>> 32));
        result = 31 * result + (containers != null ? containers.hashCode() : 0);
        result = 31 * result + (sources != null ? sources.hashCode() : 0);
        result = 31 * result + (outcomeIds != null ? outcomeIds.hashCode() : 0);
        result = 31 * result + (outcomeTypes != null ? outcomeTypes.hashCode() : 0);
        result = 31 * result + (filteredObject != null ? filteredObject.hashCode() : 0);
        result = 31 * result + (components != null ? components.hashCode() : 0);
        result = 31 * result + (entities != null ? entities.hashCode() : 0);
        result = 31 * result + (preserveEntityOrder ? 1 : 0);
        result = 31 * result + (performHighlighting ? 1 : 0);
        result = 31 * result + (socialSearch ? 1 : 0);
        result = 31 * result + (returnScore ? 1 : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (int) (rtcAuthorID ^ (rtcAuthorID >>> 32));
        result = 31 * result + (rtcObjectTypes != null ? rtcObjectTypes.hashCode() : 0);
        return result;
    }
}
