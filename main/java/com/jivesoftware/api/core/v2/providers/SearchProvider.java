/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.api.core.v2.providers;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.jivesoftware.api.core.v2.ContainerTypeRegistry;
import com.jivesoftware.api.core.v2.ContentTypeRegistry;
import com.jivesoftware.api.core.v2.OpenClientContainerType;
import com.jivesoftware.api.core.v2.OpenClientContentType;
import com.jivesoftware.api.core.v2.OpenClientObjectType;
import com.jivesoftware.api.core.v2.entities.Entity;
import com.jivesoftware.api.core.v2.entities.containers.ContainerEntity;
import com.jivesoftware.api.core.v2.entities.content.summary.ContentEntitySummary;
import com.jivesoftware.api.core.v2.types.OpenClientTypeFunctions;
import com.jivesoftware.api.core.v2.util.DateRange;
import com.jivesoftware.api.core.v2.util.Paginator;
import com.jivesoftware.base.event.SearchEvent;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContainerManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.search.SearchQueryCriteria;
import com.jivesoftware.community.search.SearchQueryCriteriaBuilder;
import com.jivesoftware.community.search.SearchQueryManager;
import com.jivesoftware.community.search.SearchResult;
import com.jivesoftware.community.web.JiveResourceResolver;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.WebApplicationException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.jivesoftware.api.core.OpenClientError.invalid_date_window;
import static com.jivesoftware.api.core.OpenClientError.invalid_sort_order;
import static com.jivesoftware.api.core.OpenClientError.sort_order_not_supported;
import static com.jivesoftware.api.core.v2.types.OpenClientTypePredicates.searchable;
import static com.jivesoftware.community.search.SearchQueryCriteria.DefaultSort.LIKES;
import static com.jivesoftware.community.search.SearchQueryCriteria.DefaultSort.MODIFICATION_DATE;
import static com.jivesoftware.community.search.SearchQueryCriteria.DefaultSort.RELEVANCE;
import static com.jivesoftware.community.search.SearchQueryCriteria.DefaultSort.SUBJECT;
import static com.jivesoftware.community.search.SearchQueryCriteria.SortOrder.ASCENDING;
import static com.jivesoftware.community.search.SearchQueryCriteria.SortOrder.DESCENDING;
import static com.jivesoftware.community.webservices.rest.ErrorBuilder.badRequest;

public class SearchProvider extends BaseProvider {

    public static final String FIRE_SEARCH_EVENTS_PROPERTY = "api.core.v2.fire_search_events";

    private SearchQueryManager searchManager;
    private ContentTypeRegistry contentTypeRegistry;
    private JiveContainerManager jiveContainerManager;
    private ContainerTypeRegistry containerTypeRegistry;
    private Function<JiveObject, ContentEntitySummary> contentSearchConverter;
    private Function<JiveObject, ContainerEntity> containerSearchConverter;
    private EventDispatcher eventDispatcher;

    private Set<Integer> searchContentObjectTypeIds;
    private Set<Integer> searchContainerObjectTypeIds;

    @Required
    public final void setSearchManager(SearchQueryManager searchManager) {
        this.searchManager = searchManager;
    }

    @Required
    public void setJiveContainerManager(JiveContainerManager jiveContainerManager) {
        this.jiveContainerManager = jiveContainerManager;
    }

    @Required
    public final void setContentTypeRegistry(ContentTypeRegistry contentTypeRegistry) {
        this.contentTypeRegistry = contentTypeRegistry;
    }

    @Required
    public final void setContainerTypeRegistry(ContainerTypeRegistry containerTypeRegistry) {
        this.containerTypeRegistry = containerTypeRegistry;
    }

    @Required
    public final void setContentSearchConverter(Function<JiveObject, ContentEntitySummary> contentSearchConverter) {
        this.contentSearchConverter = contentSearchConverter;
    }

    @Required
    public final void setContainerSearchConverter(Function<JiveObject, ContainerEntity> containerSearchConverter) {
        this.containerSearchConverter = containerSearchConverter;
    }

    @Required
    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void init() {
        Set<OpenClientContentType<JiveObject, ?>> searchableContentTypes = Sets
                .filter(contentTypeRegistry.getTypes(), searchable());
        searchContentObjectTypeIds = getIds(searchableContentTypes);
        Set<OpenClientContainerType<JiveContainer, ContainerEntity>> searchableContainerTypes = Sets
                .filter(containerTypeRegistry.getTypes(), searchable());
        searchContainerObjectTypeIds = getIds(searchableContainerTypes);
    }

    private <C extends OpenClientObjectType> ImmutableSet<Integer> getIds(Set<C> types) {
        return ImmutableSet.copyOf(Iterables.transform(types, OpenClientTypeFunctions.searchObjectTypeIds()));
    }

    public Collection<ContainerEntity> searchPlaces(String query, Set<OpenClientContainerType> containerTypes,
            Paginator paginator, DateRange dateRange, String origin)
    {
        serviceValidation().validateQuery(query);
        Set<Integer> typesToSearch = containerTypes != null ? getIds(containerTypes) : searchContainerObjectTypeIds;
        SearchQueryCriteria criteria = containerSearchCriteriaBuilder(query, typesToSearch, dateRange).build();

        SearchResult result = searchManager.executeQuery(criteria);
        Iterable<JiveObject> results = result.results(paginator.getOffset(), paginator.getLimit());
        if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_CONTENT, criteria)); // TODO - no SEARCH_PLACES?
            fireOriginEvent(origin, criteria);
        }
        return ImmutableList.copyOf(Iterables.transform(results, containerSearchConverter));
    }

    public Collection<ContentEntitySummary> addContainerInformation(Collection<ContentEntitySummary> contentEntities)
    {
        for (ContentEntitySummary contentEntity: contentEntities) {
            Map<String, Object> parentContainer = contentEntity.getParentContainer();
            try{
                long containerId = (Long) parentContainer.get("id");
                int containerType = (Integer) parentContainer.get("type");
                JiveContainer jiveContainer = jiveContainerManager.getJiveContainer(containerType, containerId);
                parentContainer.put("name", jiveContainer.getName());
                parentContainer.put("url", JiveResourceResolver.getJiveObjectURL(jiveContainer, false));
            }
            catch (Exception e) {
            }
        }
        return contentEntities;
    }

    /**
     * Does a search with the default sort order. In v2 of the Core API there is no support for specifying a sort order
     * so it requires using the default behavior.
     *
     * @param q the query for the search which is being performed.
     * @param contentTypes the content types being searched across.
     * @param paginator the paginator representing the current page in the search results that the client is requesting.
     * @param dateRange the range of dates that the client is requesting.
     * @return the collection of content resulting from the requested search.
     */
    public Collection<ContentEntitySummary> searchContent(String q, Set<OpenClientContentType> contentTypes,
            Paginator paginator, DateRange dateRange, String origin)
    {
    	System.out.println("In Search Content in Search Provider v2");
    	SortCriteria sortCriteria = new SortCriteria("relevance", "descending");

        return searchContent(q, null, contentTypes, null, paginator, dateRange, sortCriteria, origin);
    }

    public Collection<ContentEntitySummary> searchContent(String query,
                                                          Set<JiveContainer> containers,
                                                          Set<OpenClientContentType> contentTypes,
                                                          Long authorUserID,
                                                          Paginator paginator, DateRange dateRange,
                                                          SortCriteria sortCriteria,
                                                          String origin)
    {
        
    	System.out.println("In Search Content in Search Provider v2 sss");
    	serviceValidation().validateQuery(query);
        Set<Integer> typesToSearch = contentTypes != null ? getIds(contentTypes) : searchContentObjectTypeIds;
        try {
            SearchQueryCriteria criteria = contentSearchCriteriaBuilder(query, containers, typesToSearch, authorUserID, dateRange, sortCriteria)
                    .build();

            SearchResult result = searchManager.executeQuery(criteria);
            if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
                eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_CONTENT, criteria));
                fireOriginEvent(origin, criteria);
            }
            Iterable<JiveObject> results = result.results(paginator.getOffset(), paginator.getLimit());
            return ImmutableList.copyOf(filterInvalidEntries(results));
        }
        catch (IllegalStateException e) {
            throw badRequest(invalid_date_window.getCode(), e.getMessage());
        }
    }

    private Iterable<ContentEntitySummary> filterInvalidEntries(Iterable<JiveObject> results) {
        Iterable<ContentEntitySummary> entities = Iterables.transform(results, contentSearchConverter);
        return Iterables.filter(entities, Predicates.notNull());
    }

    public <T extends Entity> Collection<T> searchContainer(JiveContainer container, String query, Paginator paginator,
            Class<T> searchObjectType, String origin) {
        Preconditions.checkNotNull(searchObjectType, "Search object type must be specified.");

        return doSearchContainer(container, query, paginator, searchObjectType, new SortCriteria(), origin);
    }

    public Collection<ContentEntitySummary> searchContainer(JiveContainer container, String query,
            Paginator paginator, String origin)
    {
        return doSearchContainer(container, query, paginator, null, new SortCriteria(), origin);
    }

    public <T extends Entity> Collection<T> searchContainer(JiveContainer container, String query, Paginator paginator,
            Class<T> searchObjectType, SortCriteria sortCriteria, String origin)
    {
        Preconditions.checkNotNull(searchObjectType, "Search object type must be specified.");
        Preconditions.checkNotNull(sortCriteria, "Sort criteria must be specified.");

        return doSearchContainer(container, query, paginator, searchObjectType, sortCriteria, origin);
    }

    private <T extends Entity> Collection<T> doSearchContainer(JiveContainer container, String query,
            Paginator paginator, Class<T> searchObjectType, SortCriteria sortCriteria, String origin)
    {
        OpenClientContentType type = searchObjectType != null ? contentTypeRegistry.get(searchObjectType) : null;
        Iterable<JiveObject> results = querySearchForContainer(container, query, paginator, type, sortCriteria, origin);
        Function<JiveObject, T> searchEntityConverter = findSearchEntityConverter(container, type);
        return ImmutableList.copyOf(Iterables.transform(results, searchEntityConverter));
    }

    private Iterable<JiveObject> querySearchForContainer(JiveContainer container, String query, Paginator paginator,
            OpenClientContentType type, SortCriteria sortCriteria, String origin) {
        serviceValidation().validateQuery(query);
        Set<Integer> types = type != null ? Sets.newHashSet(type.getSearchObjectTypeId()) : searchContentObjectTypeIds;

        SearchQueryCriteria criteria = contentSearchCriteriaBuilder(query, container, types, null, null, sortCriteria)
                .build();
        SearchResult result = searchManager.executeQuery(criteria);
        if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_PLACE, criteria));
            fireOriginEvent(origin, criteria);
        }
        return result.results(paginator.getOffset(), paginator.getLimit());
    }

    private SearchQueryCriteriaBuilder containerSearchCriteriaBuilder(String query, Set<Integer> types,
            DateRange dateRange)
    {
        SearchQueryCriteriaBuilder builder = new SearchQueryCriteriaBuilder(query)
                .setObjectTypes(types)
                .setSort(RELEVANCE)
                .setSortOrder(DESCENDING)
                .collapseParents();

        if (dateRange == null) {
            return builder;
        }

        if (dateRange.getLowerBound() != null) {
            builder.setAfterDate(dateRange.getLowerBound());
        }
        if (dateRange.getUpperBound() != null) {
            builder.setBeforeDate(dateRange.getUpperBound());
        }

        return builder;
    }

    private SearchQueryCriteriaBuilder contentSearchCriteriaBuilder(
            String query,
            JiveContainer container,
            Set<Integer> types,
            Long userID,
            DateRange dateRange,
            SortCriteria sortCriteria)
    {
        Set<JiveContainer> containerSet = null;
        if ( container != null )
        {
            containerSet = new HashSet<JiveContainer>(2);
            containerSet.add(container);
        }
        return contentSearchCriteriaBuilder(query, containerSet, types, userID, dateRange, sortCriteria);
    }


    private SearchQueryCriteriaBuilder contentSearchCriteriaBuilder(
            String query,
            Set<JiveContainer> containers,
            Set<Integer> types,
            Long userID,
            DateRange dateRange,
            SortCriteria sortCriteria)
    {
        SearchQueryCriteriaBuilder builder = new SearchQueryCriteriaBuilder(query)
                .setObjectTypes(types)
                .setSort(sortCriteria.getSort())
                .setSortOrder(sortCriteria.getSortOrder())
                .collapseParents();

        if (containers != null) {
            for ( JiveContainer container : containers )
            {
                builder.addContainer(new EntityDescriptor(container));
            }
        }

        if (dateRange != null) {
            if (dateRange.getLowerBound() != null) {
                builder.setAfterDate(dateRange.getLowerBound());
            }
            if (dateRange.getUpperBound() != null) {
                builder.setBeforeDate(dateRange.getUpperBound());
            }
        }

        if (userID != null)
        {
            builder.setUserID(userID);
        }

        return builder;
    }

    @SuppressWarnings({"unchecked"})
    private <T extends Entity> Function<JiveObject, T> findSearchEntityConverter(JiveContainer container,
            OpenClientContentType type)
    {
        if (type != null) {
            return container.getObjectType() == JiveConstants.USER_CONTAINER ? type.getMySearchEntityConverter()
                    : type.getSearchEntityConverter();
        }
        else {
            return (Function<JiveObject, T>) contentSearchConverter;
        }
    }

    private void fireOriginEvent(String origin, SearchQueryCriteria criteria) {
        if (SearchEvent.Origin.valueOf(origin.toUpperCase()).equals(SearchEvent.Origin.SPOTLIGHT)) {
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SPOTLIGHT, criteria));
        }
    }

    public static class SortCriteria {

        private SearchQueryCriteria.Sort sort = RELEVANCE;
        private SearchQueryCriteria.SortOrder sortOrder = DESCENDING;

        public SortCriteria() {
            this(RELEVANCE.getKey(), "" + DESCENDING.getKey());
        }

        public SortCriteria(String sort, String sortOrder) throws WebApplicationException {
            if (RELEVANCE.getKey().equals(sort)) {
                this.sort = RELEVANCE;
            }
            else if (LIKES.getKey().equals(sort)) {
                this.sort = LIKES;
            }
            else if (SUBJECT.getKey().equals(sort)) {
                this.sort = SUBJECT;
            }
            else if (MODIFICATION_DATE.getKey().equals(sort)) {
                this.sort = MODIFICATION_DATE;
            }
            else if (sort != null) {
                throw badRequest(sort_order_not_supported.getCode(), "Invalid sort field '" + sort
                        + "' was specified, must be one of 'date', 'likes', 'relevance', or 'subject'");
            }

            if ("descending".equals(sortOrder) || "0".equals(sortOrder)) {
                this.sortOrder = DESCENDING;
            }
            else if ("ascending".equals(sortOrder) || "1".equals(sortOrder)) {
                this.sortOrder = ASCENDING;
            }
            else {
                throw badRequest(invalid_sort_order.getCode(), "Invalid sort order '" + sortOrder
                        + "' was specified, must be one of 'ascending' or 'descending'");
            }
        }

        public SearchQueryCriteria.Sort getSort() {
            return this.sort;
        }

        public SearchQueryCriteria.SortOrder getSortOrder() {
            return this.sortOrder;
        }

    }

}
