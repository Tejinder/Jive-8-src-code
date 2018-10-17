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
import com.jivesoftware.community.aaa.AnonymousUser;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Mutable object to be used in constructing an immutable {@link SearchQueryCriteria}
 */
public class SearchQueryCriteriaBuilder {
    private String queryString;
    private boolean addWildcard = false;
    private Set<Long> tagSetIDs = Sets.newHashSet();
    private long beforeDate = -1;
    private long afterDate = -1;
    private SearchQueryCriteria.Sort sort = SearchQueryCriteria.DefaultSort.RELEVANCE;
    private SearchQueryCriteria.SortOrder sortOrder = SearchQueryCriteria.SortOrder.DESCENDING;
    private Set<Integer> objectTypes = Sets.newHashSet();
    private boolean collapseParents = false;
    private boolean preserveEntityOrder = false;
    private String language;
    private long userID = AnonymousUser.ANONYMOUS_ID;
    private Set<EntityDescriptor> containers = Sets.newHashSet();
    private Set<EntityDescriptor> sources = Sets.newHashSet();
    private Set<String> outcomeIds = Sets.newHashSet();
    private Set<String> outcomeTypes = Sets.newHashSet();
    private EntityDescriptor filteredObject;
    private SearchIndexField searchField;
    private Map<SearchIndexField, String> fieldFilters = Maps.newHashMap();
    private List<SearchQueryCriteriaComponent> components = Lists.newLinkedList();
    private List<EntityDescriptor> entities = Lists.newLinkedList();
    private boolean performHighlighting = false;
    private boolean socialSearch = true;
    private boolean returnScore = false;
    private TimeZone timeZone = null;
    private Locale locale;
    private long rtcAuthorID = AnonymousUser.ANONYMOUS_ID;
    private Set<Integer> rtcObjectTypes = Sets.newHashSet();

    private boolean documentsOnly = false;
    private List<String> extendedProperties;
    private boolean applyExtendedPropertyFilters = false;
    private List<String> dateRange;

    
    public SearchQueryCriteriaBuilder(EntityDescriptor filteredObject) {
        this.filteredObject = filteredObject;
    }

    public SearchQueryCriteriaBuilder(String queryString) {
        this.queryString = queryString;
    }

    public SearchQueryCriteriaBuilder(SearchQueryCriteria start) {
        this.queryString = start.getQueryString();
        this.tagSetIDs.addAll(start.getTagSetIDs());
        this.beforeDate = start.getBeforeDate() != null ? start.getBeforeDate().getTime() : -1L;
        this.afterDate = start.getAfterDate() != null ? start.getAfterDate().getTime() : -1L;
        this.sort = start.getSort();
        this.sortOrder = start.getSortOrder();
        this.objectTypes.addAll(Sets.newHashSet(start.getObjectTypeIDs()));
        this.collapseParents = start.isCollapseParents();
        this.language = start.getLanguage();
        this.userID = start.getUserID();
        this.containers.addAll(start.getContainers());
        this.sources.addAll(start.getSources());
        this.outcomeIds.addAll(start.getOutcomeIds());
        this.outcomeTypes.addAll(start.getOutcomeTypes());
        this.filteredObject = start.getFilteredObject() != null ?
                new EntityDescriptor(start.getFilteredObject()) : null;
        this.searchField = start.getSearchedField();
        this.fieldFilters.putAll(start.getFilteredFields());
        this.components.addAll(start.getComponents());
        this.entities.addAll(start.getEntities());
        this.preserveEntityOrder = start.isPreserveEntityOrder();
        this.performHighlighting = start.isPerformHighlighting();
        this.socialSearch = start.isSocialSearch();
        this.returnScore = start.returnScore();
        this.timeZone = start.getTimeZone();
        this.locale = start.getLocale();
        this.rtcAuthorID = start.getRtcAuthorID();
        this.rtcObjectTypes = start.getRtcObjectTypes();
        this.documentsOnly = start.getDocumentsOnly();
        this.extendedProperties = start.getExtendedProperties();
        this.applyExtendedPropertyFilters = start.isApplyExtendedPropertyFilters();
        this.dateRange = start.getDateRange();
    }

    public SearchQueryCriteriaBuilder setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public String getQueryString() {
        return queryString;
    }

    public SearchQueryCriteriaBuilder setSearchField(SearchIndexField searchField) {
        this.searchField = searchField;
        return this;
    }

    public SearchIndexField getSearchField() {
        return searchField;
    }

    public SearchQueryCriteriaBuilder addFieldFilter(SearchIndexField field, String value) {
        fieldFilters.put(field, value);
        return this;
    }

    public Map<SearchIndexField, String> getFieldFilters() {
        return fieldFilters;
    }

    public SearchQueryCriteriaBuilder setFilteredObject(EntityDescriptor filteredObject) {
        this.filteredObject = new EntityDescriptor(filteredObject);
        return this;
    }

    public EntityDescriptor getFilteredObject() {
        return filteredObject;
    }

    public SearchQueryCriteriaBuilder addContainer(EntityDescriptor container) {
        this.containers.add(container);
        return this;
    }

    public SearchQueryCriteriaBuilder setContainers(Set<EntityDescriptor> containers) {
        this.containers = Sets.newHashSet(containers);
        return this;
    }

    public Set<EntityDescriptor> getContainers() {
        return containers;
    }

    public SearchQueryCriteriaBuilder addSource(EntityDescriptor source) {
        this.sources.add(source);
        return this;
    }

    public SearchQueryCriteriaBuilder setSources(Set<EntityDescriptor> sources) {
        this.sources = Sets.newHashSet(sources);
        return this;
    }

    public Set<EntityDescriptor> getSources() {
        return sources;
    }

    public Set<String> getOutcomeTypes() {
        return outcomeTypes;
    }

    public SearchQueryCriteriaBuilder setOutcomeTypes(Set<String> outcomeTypes) {
        this.outcomeTypes = Sets.newHashSet(outcomeTypes);
        return this;
    }

    public SearchQueryCriteriaBuilder addOutcomeType(int outcomeType) {
        this.outcomeTypes.add(Integer.toString(outcomeType));
        return this;
    }

    public Set<String> getOutcomeIds() {
        return outcomeIds;
    }

    public SearchQueryCriteriaBuilder setOutcomeIds(Set<String> outcomeIds) {
        this.outcomeIds = Sets.newHashSet(outcomeIds);
        return this;
    }

    public SearchQueryCriteriaBuilder addOutcomeId(long outcomeId) {
        this.outcomeIds.add(Long.toString(outcomeId));
        return this;
    }

    public SearchQueryCriteriaBuilder setUserID(long userID) {
        this.userID = userID;
        return this;
    }

    public long getUserID() {
        return userID;
    }

    public SearchQueryCriteriaBuilder setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public SearchQueryCriteriaBuilder collapseParents() {
        this.collapseParents = true;
        return this;
    }

    public boolean isCollapseParents() {
        return collapseParents;
    }

    public SearchQueryCriteriaBuilder addObjectType(int type) {
        this.objectTypes.add(type);
        return this;
    }

    public SearchQueryCriteriaBuilder setObjectTypes(Set<Integer> types) {
        this.objectTypes = Sets.newHashSet(types);
        return this;
    }

    public Set<Integer> getObjectTypes() {
        return objectTypes;
    }

    public SearchQueryCriteriaBuilder setSortOrder(SearchQueryCriteria.SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public SearchQueryCriteria.SortOrder getSortOrder() {
        return sortOrder;
    }

    public SearchQueryCriteriaBuilder setSort(SearchQueryCriteria.Sort sort) {
        this.sort = sort;
        return this;
    }

    public SearchQueryCriteria.Sort getSort() {
        return sort;
    }
    
    public boolean isPreserveEntityOrder() {
        return preserveEntityOrder;
    }

    public void setPreserveEntityOrder(boolean preserveEntityOrder) {
        this.preserveEntityOrder = preserveEntityOrder;
    }

    public SearchQueryCriteriaBuilder setAfterDate(Date afterDate) {
        if (afterDate != null) {
            this.afterDate = afterDate.getTime();
        }
        return this;
    }

    public long getAfterDate() {
        return afterDate;
    }

    public SearchQueryCriteriaBuilder setBeforeDate(Date beforeDate) {
        if (beforeDate != null) {
            this.beforeDate = beforeDate.getTime();
        }
        return this;
    }

    public long getBeforeDate() {
        return beforeDate;
    }

    public SearchQueryCriteriaBuilder addTagSetID(long tagSetID) {
        this.tagSetIDs.add(tagSetID);
        return this;
    }

    public SearchQueryCriteriaBuilder setTagSetIDs(Set<Long> tagSetIDs) {
        this.tagSetIDs = Sets.newHashSet(tagSetIDs);
        return this;
    }

    public Set<Long> getTagSetIDs() {
        return tagSetIDs;
    }

    public SearchQueryCriteriaBuilder addEntity(EntityDescriptor entityDescriptor) {
        this.entities.add(entityDescriptor);
        return this;
    }

    public SearchQueryCriteriaBuilder setEntities(List<? extends EntityDescriptor> entities) {
        this.entities = Lists.newLinkedList(entities);
        return this;
    }

    public List<EntityDescriptor> getEntities() {
        return entities;
    }

    public SearchQueryCriteriaBuilder addComponent(SearchQueryCriteriaComponent component) {
        this.components.add(component);
        return this;
    }

    public List<SearchQueryCriteriaComponent> getComponents() {
        return components;
    }

    public SearchQueryCriteriaBuilder setPerformHighlighting(boolean performHighlighting) {
        this.performHighlighting = performHighlighting;
        return this;
    }

    public boolean isPerformHighlighting() {
        return performHighlighting;
    }

    public SearchQueryCriteriaBuilder setSocialSearch(boolean socialSearch) {
        this.socialSearch = socialSearch;
        return this;
    }

    public boolean isSocialSearch() {
        return socialSearch;
    }

    public SearchQueryCriteriaBuilder setReturnScore(boolean returnScore) {
        this.returnScore = returnScore;
        return this;
    }

    public boolean returnScore() {
        return returnScore;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public SearchQueryCriteriaBuilder setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public SearchQueryCriteriaBuilder setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public long getRtcAuthorID() {
        return rtcAuthorID;
    }

    public SearchQueryCriteriaBuilder setRtcAuthorID(long rtcAuthorID) {
        this.rtcAuthorID = rtcAuthorID;
        return this;
    }

    public Set<Integer> getRtcObjectTypes() {
        return rtcObjectTypes;
    }

    public SearchQueryCriteriaBuilder setRtcObjectTypes(Set<Integer> rtcObjectTypes) {
        this.rtcObjectTypes = rtcObjectTypes;
        return this;
    }

    public SearchQueryCriteriaBuilder addRtcObjectType(int type) {
        this.rtcObjectTypes.add(type);
        return this;
    }

    public SearchQueryCriteriaBuilder setAddWildcard(boolean addWildcard) {
        this.addWildcard = addWildcard;
        return this;
    }

    public SearchQueryCriteria build() {
        if (this.addWildcard && !this.queryString.endsWith("*")) {
            this.queryString = this.queryString + "*";
        }
        return new SearchQueryCriteria(this);
    }
    public boolean getDocumentsOnly() {
        return documentsOnly;
    }

    public void setDocumentsOnly(boolean documentsOnly) {
        this.documentsOnly = documentsOnly;
    }

    public List<String> getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(List<String> extendedProperties) {
        this.extendedProperties = extendedProperties;
    }

    public boolean isApplyExtendedPropertyFilters() {
        return applyExtendedPropertyFilters;
    }

    public void setApplyExtendedPropertyFilters(boolean applyExtendedPropertyFilters) {
        this.applyExtendedPropertyFilters = applyExtendedPropertyFilters;
    }

    public List<String> getDateRange() {
        return dateRange;
    }

    public void setDateRange(List<String> dateRange) {
        this.dateRange = dateRange;
    }
}
