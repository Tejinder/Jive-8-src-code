/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.api.core.v3.providers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jivesoftware.api.core.v3.converters.EntityConverter;
import com.jivesoftware.api.core.v3.converters.SummaryEntityConverter;
import com.jivesoftware.api.core.v3.converters.content.ContentTagEntityConverter;
import com.jivesoftware.api.core.v3.converters.users.PersonEntityConverter;
import com.jivesoftware.api.core.v3.entities.ContentEntity;
import com.jivesoftware.api.core.v3.entities.Entity;
import com.jivesoftware.api.core.v3.entities.PlaceEntity;
import com.jivesoftware.api.core.v3.entities.SearchableObjectEntity;
import com.jivesoftware.api.core.v3.entities.content.ContentTagEntity;
import com.jivesoftware.api.core.v3.entities.tiles.PageEntity;
import com.jivesoftware.api.core.v3.entities.users.PersonEntity;
import com.jivesoftware.api.core.v3.exceptions.BadRequestException;
import com.jivesoftware.api.core.v3.exceptions.CoreErrorCodes;
import com.jivesoftware.api.core.v3.exceptions.ForbiddenException;
import com.jivesoftware.api.core.v3.exceptions.NotFoundException;
import com.jivesoftware.api.core.v3.exceptions.WebServiceException;
import com.jivesoftware.api.core.v3.providers.places.PlaceProvider;
import com.jivesoftware.api.core.v3.types.CoreObjectType;
import com.jivesoftware.api.core.v3.util.ActivityDirective;
import com.jivesoftware.api.core.v3.util.ActivityDirectiveUtil;
import com.jivesoftware.api.core.v3.util.Filter;
import com.jivesoftware.api.core.v3.util.RTCAdapterProvider;
import com.jivesoftware.api.core.v3.util.ThreadLocalDateFormat;
import com.jivesoftware.api.core.v3.util.ThreadLocalDateOnlyFormat;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.UserObjectType;
import com.jivesoftware.base.event.SearchEvent;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.community.ContentTag;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.SystemContainer;
import com.jivesoftware.community.aaa.SystemAuthentication;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.aaa.authz.SudoExecutor;
import com.jivesoftware.community.carousel.container.objecttype.CarouselContainerObjectType;
import com.jivesoftware.community.extendedinvitation.impl.ExtendedInvitationManagerImpl;
import com.jivesoftware.community.impl.CommunityHierarchyHelper;
import com.jivesoftware.community.integration.tile.Tile;
import com.jivesoftware.community.integration.tilepage.TilePage;
import com.jivesoftware.community.objecttype.impl.GroupObjectType;
import com.jivesoftware.community.objecttype.impl.TilePageObjectType;
import com.jivesoftware.community.outcome.OutcomeType;
import com.jivesoftware.community.rtc.RTCSettingsManager;
import com.jivesoftware.community.rtc.collaboration.Collaboration;
import com.jivesoftware.community.rtc.interaction.Interaction;
import com.jivesoftware.community.search.DefaultSearchIndexField;
import com.jivesoftware.community.search.PromotedResults;
import com.jivesoftware.community.search.PromotedResultsHelper;
import com.jivesoftware.community.search.SearchLogger;
import com.jivesoftware.community.search.SearchQueryCriteria;
import com.jivesoftware.community.search.SearchQueryCriteriaBuilder;
import com.jivesoftware.community.search.SearchQueryManager;
import com.jivesoftware.community.search.SearchResult;
import com.jivesoftware.community.search.SearchSettingsManager;
import com.jivesoftware.community.search.UuidSearchQueryCriteriaComponent;
import com.jivesoftware.community.search.action.SearchActionHelper;
import com.jivesoftware.community.search.user.ProfileSearchCriteria;
import com.jivesoftware.community.search.user.ProfileSearchCriteriaBuilder;
import com.jivesoftware.community.search.user.ProfileSearchQueryManager;
import com.jivesoftware.community.search.user.ProfileSearchResult;
import com.jivesoftware.community.user.profile.DefaultProfileFields;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.community.user.profile.ProfileFieldManager;
import com.jivesoftware.community.user.profile.ProfileSearchFilter;
import com.jivesoftware.community.util.BasePermHelper;
import com.jivesoftware.util.DateUtils;
import com.jivesoftware.util.LocaleUtils;
import com.jivesoftware.util.StringUtils;
import com.jivesoftware.util.spell.SpellChecker;
import com.jivesoftware.util.spell.SpellSession;
import com.newrelic.api.agent.Trace;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.jivesoftware.community.search.SearchQueryCriteria.DefaultSort.MODIFICATION_DATE;
import static com.jivesoftware.community.search.SearchQueryCriteria.DefaultSort.RELEVANCE;
import static com.jivesoftware.community.search.SearchQueryCriteria.SortOrder.ASCENDING;
import static com.jivesoftware.community.search.SearchQueryCriteria.SortOrder.DESCENDING;


/**
 * <p>Adapter between search services and internal search API.</p>
 */
public class SearchProvider extends AbstractObjectProvider {

    protected static final Logger log = Logger.getLogger(SearchProvider.class);

    public static final String FIRE_SEARCH_EVENTS_PROPERTY = "api.core.v3.fire_search_events";

    // ---------------------------------------------------------------------------------------------- Instance Variables

    // DateFormat are not thread-safe so we are keeping one local per thread
    private static final ThreadLocalDateFormat DATE_FORMAT = new ThreadLocalDateFormat();
    private static final ThreadLocalDateOnlyFormat DATE_ONLY_FORMAT = new ThreadLocalDateOnlyFormat();

    protected ProfileField companyProfileField;
    protected Filter contentTypesFilter;
    protected ProfileField departmentProfileField;
    protected ProfileField locationProfileField;
    protected Filter placeTypesFilter;
    protected Filter placeAndPageTypesFilter;
    protected ProfileField titleProfileField;
    protected ProfileField hireDateProfileField;

    private SearchActionHelper searchActionHelper;

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Trace
    public String getCorrectedQuery(List<String> filters) {
        if (!suggestedQueryEnabled) {
            return null;
        }
        if (filters == null) {
            return null;
        }
        Filter search = null;
        for (String filter : filters) {
            if (filter.startsWith("search(")) {
                search = new Filter(filter);
                break;
            }
        }
        if (search == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String param : search.getParams()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(param);
        }
        String q = sb.toString();
        try {
            SpellSession spellSession = SpellChecker.createSession(q, getLocale().toString());
            boolean foundAlternate = false;
            if (spellSession != null) {
                for (int result = spellSession.next(); result != SpellSession.OK; result = spellSession.next()) {
                    if (result == SpellSession.MISSPELLED_WORD) {
                        String[] str = spellSession.getSuggestions();
                        if (str.length > 0) {
                            spellSession.replace(str[0]);
                            foundAlternate = true;
                        }
                        else {
                            log.error("Unexpected spell checking result code: " + result);
                            spellSession.ignore();
                        }
                    }
                    else if (result == SpellSession.DOUBLED_WORD) {
                        spellSession.delete();
                    }
                }
                if (foundAlternate) {
                    String suggestion = "search(" + StringUtils.stripTags(spellSession.getText().replace(" ",",")) + ")";
                    return suggestion;
                }
            }
        }
        catch (Exception e) {
            log.warn("Got error searching for term '" + q + "' and trying to run spell check", e);
        }
        return null;
    }

    @Trace
    public Locale getLocale() {
        return localeProvider.get();
    }

    @Trace
    public List<Entity> searchContent(List<String> filters, List<String> directives, boolean collapse,
            boolean highlight, boolean socialSearch, boolean returnScore, String sort, int startIndex, int count,
            String origin, String fields)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
        
    	System.out.println("In Search Content in Search Provider");
    	if (log.isDebugEnabled()) {
            String search = new StringBuilder()
                .append("Content search: filters[").append(filters).append("]")
                .append(", collapse=").append(collapse)
                .append(", highlight=").append(highlight)
                .append(", socialSearch=").append(socialSearch)
                .append(", returnScore=").append(returnScore)
                .append(", sort=").append(sort)
                .append(", startIndex=").append(startIndex)
                .append(", count=").append(count)
                .append(", origin=").append(origin)
                .append(", fields=").append(fields)
                .toString();
            log.debug(search);
        }

        Set<ActivityDirective> activityDirectives = validateActivityDirectives(directives);
        boolean includeRTC = ActivityDirectiveUtil.hasBooleanValue(activityDirectives, ActivityDirective.Type.include_rtc, true);

        List<Filter> exprs = validateFilters(filters, contentAppliers, true);
        addContentTypesFilter(exprs);
        addRtcTypesWithAuthorFilter(exprs);
        validatePagination(startIndex, count);
        sort = validateSort(sort);
        SearchQueryCriteriaBuilder builder = builder(exprs, collapse, sort, contentAppliers);

        if (getFilter(exprs, "place") != null) {
            Filter depthFilter = getFilter(exprs, "depth");
            if (depthFilter != null) {
                String depthValue = depthFilter.getParams()[0];
                builder.setContainers(getFilteredPlaces(builder.getContainers(), depthValue));
            }
        }

        builder.setPerformHighlighting(highlight);
        builder.setSocialSearch(socialSearch);
        builder.setReturnScore(returnScore);

        String uuid = UUID.randomUUID().toString();
        builder.addComponent(new UuidSearchQueryCriteriaComponent(uuid));

        StringBuilder searchLogging = new StringBuilder("content: ");
        searchLogging.append("userid=").append(authenticationProvider.getJiveUserID())
                .append(",origin=").append(origin)
                .append(",uuid=").append(uuid)
                .append(",results=");

        SearchQueryCriteria criteria = builder.build();
        if (log.isDebugEnabled()) {
            log.debug("Searching content with criteria " + criteria);
        }

        long start = System.currentTimeMillis();
        SearchResult result = searchQueryManager.executeQuery(criteria);
        long duration = System.currentTimeMillis() - start;

        if (log.isDebugEnabled()) {
            log.debug("Content: search query manager took " + duration + "ms to execute query");
        }

        List<Entity> entities = Lists.newLinkedList();

        int resultIndex = 0;
        boolean isPromotedResultsEnabled = promotedResultsHelper.isPromotedResultsEnabled();

        if (isPromotedResultsEnabled) {
            PromotedResults promotedResults = promotedResultsHelper.getPromotedResultsForLocale(criteria.getLanguage());
            List<EntityDescriptor> matches = promotedResults.getMatches(criteria.getQueryString());

            int offset = Math.min(matches.size(), count);

            // if this is the first page of results, and promoted results exist, then request fewer results from search
            if (startIndex == 0 && matches.size() > 0) {
                addPromotedResults(entities, matches, fields, uuid, startIndex, count);
                count -= offset;
                resultIndex = entities.size();
            }

            // if this is not the first page of results, but there were promoted results that would have appeared on
            // the first page, then adjust where we start iterating over results
            if (startIndex != 0 && matches.size() > 0) {
                startIndex -= offset;
                resultIndex = startIndex;
            }
        }

        start = System.currentTimeMillis();
        for (JiveObject object : result.results(startIndex, count)) {
            if (!includeRTC && object instanceof Collaboration) {
                object = rtcAdapterProvider.adaptCollaborationToDirectMessage((Collaboration) object);
            }
            else if (!includeRTC && object instanceof Interaction) {
                object = rtcAdapterProvider.adaptInteractionToComment((Interaction) object);
            }
            CoreObjectType coreObjectType = coreObjectTypeProvider.getObjectType(object);
            if (coreObjectType == null) {
                log.error("SEARCH RESULTS ERROR: No Core API object type for object type " + object.getObjectType() + " and ID " + object.getID());
                log.error("Jive Object returned by Search: " + object);
                continue;
            }
            EntityConverter entityConverter = coreObjectType.getEntityConverter();
            if (entityConverter == null) {
                log.error("SEARCH RESULTS ERROR:  No Core API Entity Converter for object type " + object.getObjectType() + " and ID " + object.getID());
                log.error("Jive Object returned by Search: " + object);
                continue;
            }
            try {
                Entity entity = entityConverter.convert(object, fields);
                if (entity instanceof SearchableObjectEntity) {
                    SearchableObjectEntity soe = (SearchableObjectEntity) entity;
                    if (highlight) {
                        Map<SearchQueryManager.HighlightField, String> highlights = result.highlights(new EntityDescriptor(object));
                        if (log.isDebugEnabled()) {
                            log.debug("Matched content object " + object);
                            if (highlights.size() == 0) {
                                log.debug("No highlight fields returned for this content object");
                            }
                            else {
                                for (Map.Entry<SearchQueryManager.HighlightField, String> entry : highlights.entrySet()) {
                                    log.debug("Highlight field " + entry.getKey().name() + ": " + entry.getValue());
                                }
                            }
                        }

                        soe.setHighlightBody(highlights.get(SearchQueryManager.HighlightField.BODY));
                        soe.setHighlightSubject(highlights.get(SearchQueryManager.HighlightField.SUBJECT));
                        soe.setHighlightTags(highlights.get(SearchQueryManager.HighlightField.TAGS));
                    }

                    if (returnScore) {
                        Map<String, String> rankings = result.searchRankings(new EntityDescriptor(object));
                        soe.setSearchRankings(rankings);
                    }

                    soe.setUuid(uuid);
                    soe.setIndex("" + resultIndex++);
                }
                if ((object instanceof JiveContentObject) && (entity instanceof ContentEntity)) {
                    addResultsFields((JiveContentObject) object, (ContentEntity) entity);
                }

                entities.add(entity);

                searchLogging.append(" [" + object.getObjectType() + "," + object.getID() + "]");
            } catch (Exception e) {
                log.error("SEARCH RESULTS ERROR: Entity conversion error for object type " + object.getObjectType() + " and ID " + object.getID(), e);
                log.error("Jive Object returned by Search: " + object);
            }
        }
        SearchLogger.logInfo(searchLogging.toString());

        duration = System.currentTimeMillis() - start;
        if (log.isDebugEnabled()) {
            log.debug("Content: object iteration in searchContent took " + duration + "ms and returned " + entities.size() + " entities");
        }

        if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_CONTENT, criteria));
            fireOriginEvent(origin, criteria);
        }

        return entities;
    }

    private void addPromotedResults(List<Entity> entities, List<EntityDescriptor> matches, String fields, String uuid,
            int startIndex, int count) {
        if (startIndex != 0) {
            return;
        }

        if (startIndex < count) {
            for (EntityDescriptor ed : matches) {
                CoreObjectType coreObjectType = coreObjectTypeProvider.getObjectType(ed.getObjectType());
                JiveContentObject jco = null;
                try {
                    jco = coreObjectType.getContentProvider().lookupContent(ed.getID());
                }
                catch (NotFoundException e) {
                    // Another UGLY hack to simulate that files are a content type
                    if ("file".equals(coreObjectType.getName())) {
                        coreObjectType = coreObjectTypeProvider.getObjectTypeByName("document");
                        jco = coreObjectType.getContentProvider().lookupContent(ed.getID());
                    }
                    else {
                        throw e;
                    }
                }

                EntityConverter entityConverter = coreObjectType.getEntityConverter();
                Entity entity = entityConverter.convert(jco, fields);
                if (entity instanceof SearchableObjectEntity) {
                    SearchableObjectEntity soe = (SearchableObjectEntity) entity;
                    soe.setPromotedResult(true);
                    soe.setUuid(uuid);
                    soe.setIndex("" + startIndex++);
                    Map<String, String> rankings = Collections.emptyMap();
                    soe.setSearchRankings(rankings);
                }

                if (entity instanceof ContentEntity) {
                    addResultsFields(jco, (ContentEntity) entity);
                }

                entities.add(entity);
            }
        }
    }

    @Trace
    public List<PersonEntity> searchPeople(List<String> filters, String viewContentURI, String sort, int startIndex,
            int count, String origin, String fields, boolean searchRequired)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
        if (log.isDebugEnabled()) {
            String search = new StringBuilder()
                    .append("People search: filters[").append(filters).append("]")
                    .append(", sort=").append(sort)
                    .append(", startIndex=").append(startIndex)
                    .append(", count=").append(count)
                    .append(", origin=").append(origin)
                    .append(", fields=").append(fields)
                    .toString();
            log.debug(search);
        }

        List<PersonEntity> entities = Lists.newArrayList();
        validatePagination(startIndex, count);
        List<Filter> exprs = validateFilters(filters, peopleAppliers, searchRequired);

        ProfileSearchCriteriaBuilder builder = builder(exprs, sort, searchRequired)
                .setStart(startIndex)
                .setRange(count);

        String uuid = UUID.randomUUID().toString();
        builder.addComponent(new UuidSearchQueryCriteriaComponent(uuid));

        StringBuilder searchLogging = new StringBuilder("people: ");
        searchLogging.append("userid=").append(authenticationProvider.getJiveUserID())
                .append(",origin=").append(origin)
                .append(",uuid=").append(uuid)
                .append(",results=");

        ProfileSearchCriteria criteria = builder.build();
        if (log.isDebugEnabled()) {
            log.debug("Searching people with criteria " + criteria);
        }

        long start = System.currentTimeMillis();
        ProfileSearchResult result = profileSearchQueryManager.executeSearch(criteria);
        long duration = System.currentTimeMillis() - start;

        if (log.isDebugEnabled()) {
            log.debug("People: profile search query manager took " + duration + "ms to execute query");
        }

        Filter viewContentFilter = getFilter(exprs, "view-content");
        JiveContentObject filterContentObject = null;
        if (viewContentFilter != null) {
            String uri = viewContentFilter.getParams()[0];
            String contentID = extractContentID(uri);
            filterContentObject = resolveContentID(contentID);
            if (filterContentObject == null) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.illegal_content_uri", uri)
                        .code(CoreErrorCodes.CONTENT_INVALID_URI)
                        .build();
            }
        }

        JiveContentObject viewContentObject = null;
        if (viewContentURI != null) {
            String contentID = extractContentID(viewContentURI);
            viewContentObject = resolveContentID(contentID);
            if (viewContentObject == null) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.illegal_content_uri", viewContentURI)
                        .code(CoreErrorCodes.CONTENT_INVALID_URI)
                        .build();
            }
        }

        start = System.currentTimeMillis();
        Iterable<User> userIterable = result.results();
        int index = 0;
        for (User user : userIterable) {
            if (log.isDebugEnabled()) {
                log.debug("Matched person object " + user);
            }
            if (filterContentObject != null) {
                // We need to remove users that cannot see the requested content
                if (!entitlementTypeProvider.isUserEntitled(user, filterContentObject, EntitlementTypeProvider.EntitlementType.VIEW)) {
                    continue;
                }
            }
            try {
                PersonEntity entity = personEntityConverter.convert(user, fields);
                if (viewContentObject != null) {
                    boolean canView = entitlementTypeProvider.isUserEntitled(user, viewContentObject, EntitlementTypeProvider.EntitlementType.VIEW);
                    entity.getJive().setViewContent(canView);
                    entity.setUuid(uuid);
                    entity.setIndex("" + index++);
                }
                addResultsFields(user, entity);
                entities.add(entity);
                searchLogging.append(" [" + user.getObjectType() + "," + user.getID() + "]");
            } catch (Exception e) {
                log.error("SEARCH RESULTS ERROR:  Entity conversion error for person " + user.getID(), e);
                log.error("Person returned by search: " + user);
            }
        }
        SearchLogger.logInfo(searchLogging.toString());

        duration = System.currentTimeMillis() - start;
        if (log.isDebugEnabled()) {
            log.debug("People: object iteration in searchPeople took " + duration + "ms and returned " + entities.size() + " entities");
        }

        if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_USER, criteria));
            fireOriginEvent(origin, criteria);
        }

        return entities;
    }

    @Trace
    public List<PlaceEntity> searchPlaces(List<String> filters, boolean collapse, boolean highlight,
            boolean socialSearch, boolean returnScore, String sort, int startIndex, int count, String origin,
            String fields)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
        if (log.isDebugEnabled()) {
            String search = new StringBuilder()
                    .append("Places search: filters[").append(filters).append("]")
                    .append(", collapse=").append(collapse)
                    .append(", highlight=").append(highlight)
                    .append(", socialSearch=").append(socialSearch)
                    .append(", returnScore=").append(returnScore)
                    .append(", sort=").append(sort)
                    .append(", startIndex=").append(startIndex)
                    .append(", count=").append(count)
                    .append(", origin=").append(origin)
                    .append(", fields=").append(fields)
                    .toString();
            log.debug(search);
        }

        List<Filter> exprs = validateFilters(filters, placesAppliers, true);
        addPlaceTypesFilter(exprs);
        validatePagination(startIndex, count);
        sort = validateSort(sort);
        SearchQueryCriteriaBuilder builder = builder(exprs, collapse, sort, placesAppliers);

        builder.setPerformHighlighting(highlight);
        builder.setSocialSearch(socialSearch);
        builder.setReturnScore(returnScore);

        String uuid = UUID.randomUUID().toString();
        builder.addComponent(new UuidSearchQueryCriteriaComponent(uuid));

        StringBuilder searchLogging = new StringBuilder("places: ");
        searchLogging.append("userid=").append(authenticationProvider.getJiveUserID())
                .append(",origin=").append(origin)
                .append(",uuid=").append(uuid)
                .append(",results=");

        SearchQueryCriteria criteria = builder.build();
        if (log.isDebugEnabled()) {
            log.debug("Searching places with criteria " + criteria);
        }

        long start = System.currentTimeMillis();
        SearchResult result = searchQueryManager.executeQuery(criteria);
        long duration = System.currentTimeMillis() - start;

        if (log.isDebugEnabled()) {
            log.debug("Places: search query manager took " + duration + "ms to execute query");
        }

        start = System.currentTimeMillis();
        List<PlaceEntity> entities = Lists.newArrayList();
        int index = 0;
        for (JiveObject object : result.results(startIndex, count)) {
            if (!(object instanceof JiveContainer)) {
                throw coreErrorBuilder.internalServerErrorException("api.core.v3.error.illegal_search_places_match", "" + object.getObjectType())
                        .build();
            }
            JiveContainer place = (JiveContainer) object;
            CoreObjectType coreObjectType = coreObjectTypeProvider.getObjectType(place);
            if (coreObjectType == null) {
                log.error("SEARCH RESULTS ERROR: No Core API object type for object type " + object.getObjectType() + " and ID " + object.getID());
                log.error("Jive Object returned by Search: " + object);
                continue;
            }
            PlaceProvider<JiveContainer, PlaceEntity> placeProvider = coreObjectType.getPlaceProvider();
            if (placeProvider == null) {
                log.error("SEARCH RESULTS ERROR:  No Core API Place Provider for object type " + object.getObjectType() + " and ID " + object.getID());
                log.error("Jive Object returned by Search: " + object);
                continue;
            }
            try {
                PlaceEntity entity = placeProvider.getPlace(place, fields);
                if (highlight) {
                    Map<SearchQueryManager.HighlightField, String> highlights =
                            result.highlights(new EntityDescriptor(place));
                    if (log.isDebugEnabled()) {
                        log.debug("Matched place object " + place);
                        if (highlights.size() == 0) {
                            log.debug("No highlight fields returned for this place object");
                        }
                        else {
                            for (Map.Entry<SearchQueryManager.HighlightField, String> entry : highlights.entrySet()) {
                                log.debug("Highlight field " + entry.getKey().name() + ": " + entry.getValue());
                            }
                        }
                    }

                    entity.setHighlightBody(highlights.get(SearchQueryManager.HighlightField.BODY));
                    entity.setHighlightSubject(highlights.get(SearchQueryManager.HighlightField.SUBJECT));
                    entity.setHighlightTags(highlights.get(SearchQueryManager.HighlightField.TAGS));
                }

                if (returnScore) {
                    Map<String, String> rankings = result.searchRankings(new EntityDescriptor(object));
                    entity.setSearchRankings(rankings);
                }

                entity.setUuid(uuid);
                entity.setIndex("" + index++);

                addResultsFields(place, entity);
                entities.add(entity);
                searchLogging.append(" [" + object.getObjectType() + "," + object.getID() + "]");
            }
            catch (Exception e) {
                log.error("SEARCH RESULTS ERROR: Entity conversion error for object type " + object.getObjectType() + " and ID " + object.getID(), e);
                log.error("Jive Object returned by Search: " + object);
            }
        }
        SearchLogger.logInfo(searchLogging.toString());

        duration = System.currentTimeMillis() - start;
        if (log.isDebugEnabled()) {
            log.debug("Places: object iteration in searchPlaces took " + duration + "ms and returned " + entities.size() + " entities");
        }

        if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_PLACE, criteria));
            fireOriginEvent(origin, criteria);
        }

        return entities;
    }

    // method to search ONLY PAGES AND PLACES, modeled after searchPlaces
    // todo use a generic "search" method instead and pass in types via filter
    @Trace
    public List<Entity> searchPlacesAndPages(List<String> filters, boolean collapse, boolean highlight,
            boolean socialSearch, boolean returnScore, String sort, int startIndex, int count, String origin,
            String fields)
            throws BadRequestException, ForbiddenException, NotFoundException
    {
        if (log.isDebugEnabled()) {
            String search = new StringBuilder()
                    .append("General search: filters[").append(filters).append("]")
                    .append(", collapse=").append(collapse)
                    .append(", highlight=").append(highlight)
                    .append(", socialSearch=").append(socialSearch)
                    .append(", returnScore=").append(returnScore)
                    .append(", sort=").append(sort)
                    .append(", startIndex=").append(startIndex)
                    .append(", count=").append(count)
                    .append(", origin=").append(origin)
                    .append(", fields=").append(fields)
                    .toString();
            log.debug(search);
        }

        List<Filter> exprs = validateFilters(filters, placesAppliers, true);
        addPlaceAndPageTypesFilter(exprs);

        validatePagination(startIndex, count);
        sort = validateSort(sort);
        SearchQueryCriteriaBuilder builder = builder(exprs, collapse, sort, placesAppliers);

        builder.setPerformHighlighting(highlight);
        builder.setSocialSearch(socialSearch);
        builder.setReturnScore(returnScore);

        String uuid = UUID.randomUUID().toString();
        builder.addComponent(new UuidSearchQueryCriteriaComponent(uuid));

        StringBuilder searchLogging = new StringBuilder("places + pages: ");
        searchLogging.append("userid=").append(authenticationProvider.getJiveUserID())
                .append(",origin=").append(origin)
                .append(",uuid=").append(uuid)
                .append(",results=");

        SearchQueryCriteria criteria = builder.build();
        if (log.isDebugEnabled()) {
            log.debug("Searching places and pages with criteria " + criteria);
        }

        long start = System.currentTimeMillis();
        SearchResult result = searchQueryManager.executeQuery(criteria);
        long duration = System.currentTimeMillis() - start;

        if (log.isDebugEnabled()) {
            log.debug("Places + Pages: search query manager took " + duration + "ms to execute query");
        }

        start = System.currentTimeMillis();
        List<Entity> entities = Lists.newArrayList();
        int index = 0;
        for (JiveObject object : result.results(startIndex, count)) {
            try {
                if (object instanceof TilePage) {

                    CoreObjectType coreObjectType = coreObjectTypeProvider.getObjectType(object);
                    EntityConverter entityConverter = coreObjectType.getEntityConverter();
                    PageEntity entity = (PageEntity) entityConverter.convert(object, fields);
                    addResultsFields((TilePage) object, entity);
                    entities.add(entity);

                } else if (object instanceof JiveContainer) {

                    JiveContainer place = (JiveContainer) object;
                    CoreObjectType coreObjectType = coreObjectTypeProvider.getObjectType(place);
                    if (coreObjectType == null) {
                        log.error(
                                "SEARCH RESULTS ERROR: No Core API object type for object type " + object.getObjectType() +
                                        " and ID " + object.getID());
                        log.error("Jive Object returned by Search: " + object);
                        continue;
                    }
                    PlaceProvider<JiveContainer, PlaceEntity> placeProvider = coreObjectType.getPlaceProvider();
                    if (placeProvider == null) {
                        log.error("SEARCH RESULTS ERROR:  No Core API Place Provider for object type " +
                                object.getObjectType() + " and ID " + object.getID());
                        log.error("Jive Object returned by Search: " + object);
                        continue;
                    }
                    PlaceEntity entity = placeProvider.getPlace(place, fields);

                    if (highlight) {
                        Map<SearchQueryManager.HighlightField, String> highlights =
                                result.highlights(new EntityDescriptor(place));
                        if (log.isDebugEnabled()) {
                            log.debug("Matched place object " + place);
                            if (highlights.size() == 0) {
                                log.debug("No highlight fields returned for this place object");
                            }
                            else {
                                for (Map.Entry<SearchQueryManager.HighlightField, String> entry : highlights.entrySet()) {
                                    log.debug("Highlight field " + entry.getKey().name() + ": " + entry.getValue());
                                }
                            }
                        }

                        entity.setHighlightBody(highlights.get(SearchQueryManager.HighlightField.BODY));
                        entity.setHighlightSubject(highlights.get(SearchQueryManager.HighlightField.SUBJECT));
                        entity.setHighlightTags(highlights.get(SearchQueryManager.HighlightField.TAGS));
                    }

                    if (returnScore) {
                        Map<String, String> rankings = result.searchRankings(new EntityDescriptor(object));
                        entity.setSearchRankings(rankings);
                    }

                    entity.setUuid(uuid);
                    entity.setIndex("" + index++);

                    addResultsFields(place, entity);
                    entities.add(entity);
                } else {
                    // todo replace with appropriate error message including pages and places
                    throw coreErrorBuilder.internalServerErrorException("api.core.v3.error.illegal_search_places_match",
                            "" + object.getObjectType())
                            .build();
                }

                searchLogging.append(" [" + object.getObjectType() + "," + object.getID() + "]");
            }
            catch (WebServiceException e) {
                throw e;
            }
            catch (Exception e) {
                log.error("SEARCH RESULTS ERROR: Entity conversion error for object type " + object.getObjectType() + " and ID " + object.getID(), e);
                log.error("Jive Object returned by Search: " + object);
            }
        }
        SearchLogger.logInfo(searchLogging.toString());

        duration = System.currentTimeMillis() - start;
        if (log.isDebugEnabled()) {
            log.debug("Places: object iteration in search took " + duration + "ms and returned " + entities.size() + " entities");
        }

        if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
            // todo add new SearchEvent.Type for this type of search
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_PLACE, criteria));
            fireOriginEvent(origin, criteria);
        }

        return entities;
    }


    @Trace
    public List<ContentTagEntity> searchTags(List<String> filters, String sort, int startIndex, int count,
            String origin, String fields)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
        if (log.isDebugEnabled()) {
            String search = new StringBuilder()
                    .append("Tags search: filters[").append(filters).append("]")
                    .append(", startIndex=").append(startIndex)
                    .append(", count=").append(count)
                    .append(", origin=").append(origin)
                    .append(", fields=").append(fields)
                    .toString();
            log.debug(search);
        }

        List<Filter> exprs = validateFilters(filters, tagAppliers, true);
        validatePagination(startIndex, count);
        sort = validateSort(sort);

        List<Integer> searchableTypes = Arrays.asList(JiveConstants.TAG);

        SearchQueryCriteriaBuilder builder = builder(exprs, false, sort, tagAppliers)
                .setObjectTypes(new HashSet(searchableTypes));

        String uuid = UUID.randomUUID().toString();
        builder.addComponent(new UuidSearchQueryCriteriaComponent(uuid));

        StringBuilder searchLogging = new StringBuilder("tags: ");
        searchLogging.append("userid=").append(authenticationProvider.getJiveUserID())
                .append(",origin=").append(origin)
                .append(",uuid=").append(uuid)
                .append(",results=");

        SearchQueryCriteria criteria = builder.build();
        if (log.isDebugEnabled()) {
            log.debug("Searching tags with criteria " + criteria);
        }

        long start = System.currentTimeMillis();
        SearchResult result = searchQueryManager.executeQuery(criteria);
        long duration = System.currentTimeMillis() - start;

        if (log.isDebugEnabled()) {
            log.debug("Tags: search query manager took " + duration + "ms to execute query");
        }

        List<ContentTagEntity> entities = Lists.newArrayList();
        int index = 0;
        for (JiveObject object : result.results(startIndex, count)) {
            if (!(object instanceof ContentTag)) {
                throw coreErrorBuilder.internalServerErrorException("api.core.v3.error.illegal_search_tags_match", "" + object.getObjectType())
                        .build();
            }

            ContentTag tag = (ContentTag) object;
            try {
                ContentTagEntity entity = contentTagEntityConverter.convert(tag, fields);
                entity.setUuid(uuid);
                entity.setIndex("" + index++);
                entities.add(entity);
                searchLogging.append(" [" + object.getObjectType() + "," + object.getID() + "]");
            }
            catch (Exception e) {
                log.error("SEARCH RESULTS ERROR: Entity conversion error for object type " + object.getObjectType() + " and ID " + object.getID(), e);
                log.error("Jive Object returned by Search: " + object);
            }
        }
        SearchLogger.logInfo(searchLogging.toString());

        if (JiveGlobals.getJiveBooleanProperty(FIRE_SEARCH_EVENTS_PROPERTY, true)) {
            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.TAG, criteria));
            fireOriginEvent(origin, criteria);
        }

        return entities;
    }

    // ------------------------------------------------------------------------------------------------- Support Methods

    protected void addContentTypesFilter(List<Filter> exprs) {
        for (Filter expr : exprs) {
            if ("type".equals(expr.getName())) {
                return;
            }
        }
        exprs.add(contentTypesFilter());
    }

    protected void addRtcTypesWithAuthorFilter(List<Filter> exprs) {
        if (rtcSettingsManager.isActive()) {
            for (Filter expr : exprs) {
                if ("rtcTypesWithAuthor".equals(expr.getName())) {
                    return;
                }
            }
            exprs.add(new Filter("rtcTypesWithAuthor(true)"));
        }
    }

    protected void addPlaceTypesFilter(List<Filter> exprs) {
        for (Filter expr : exprs) {
            if ("type".equals(expr.getName())) {
                return;
            }
        }
        exprs.add(placeTypesFilter());
    }

    protected void addPlaceAndPageTypesFilter(List<Filter> exprs) {
        for (Filter expr : exprs) {
            if ("type".equals(expr.getName())) {
                return;
            }
        }
        exprs.add(placeAndPageTypesFilter());
    }

    protected void addResultsFields(JiveContentObject content, ContentEntity entity) {
        JiveObject parentObject = null;
        JiveObject parentPlace = null;
        try {
            parentObject = jiveObjectProvider.parent(content);
            parentPlace = resolveParentPlace(content);
        } catch (ForbiddenException e) {
            if (!forceAddParentFields(content, entity)) {
                // we're not an extended author, so the ForbiddenException is legitimate and need to be thrown
                // up the stack
                throw e;
            }
            return;
        }

        // if we get here, we weren't forbidden to see the parent and we weren't an extended author
        if ((parentObject != null) && (parentObject instanceof JiveContentObject)) {
            if (entity.included("parentContent")) {
                entity.setParentContent(summaryEntityConverter.convert(parentObject, null));
            }
        }
        if ((parentPlace != null) && !(parentPlace instanceof SystemContainer)) {
            if (entity.included("parentPlace")) {
             entity.setParentPlace(summaryEntityConverter.convert(parentPlace, null));
            }
        }
    }

    protected boolean forceAddParentFields(final JiveContentObject jiveContentObject, final ContentEntity entity) {
        // only force add the parent if the viewer is actually an extended author
        if (extendedInvitationManagerImpl.isExtendedAuthor(jiveContentObject, getViewer())) {
            Callable<Void> c = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    JiveObject parentObject = jiveObjectProvider.parent(jiveContentObject);
                    if ((parentObject != null) && (parentObject instanceof JiveContentObject)) {
                        if (entity.included("parentContent")) {
                            entity.setParentContent(summaryEntityConverter.convert(parentObject, null));
                        }
                    }
                    JiveContainer parentPlace = resolveParentPlace(jiveContentObject);
                    if ((parentPlace != null) && !(parentPlace instanceof SystemContainer)) {
                        if (entity.included("parentPlace")) {
                            entity.setParentPlace(summaryEntityConverter.convert(parentPlace, null));

                        }
                    }
                    return null;
                }
            };
            SudoExecutor sudoExecutor = new SudoExecutor(authenticationProvider, new SystemAuthentication());
            try {
                sudoExecutor.executeCallable(c);
                return true;
            }
            catch (Exception e) {
                throw coreErrorBuilder.internalServerErrorException(e.getMessage(), e).build();
            }
        }
        return false;
    }

    protected void addResultsFields(User user, PersonEntity entity) {
    }

    protected void addResultsFields(JiveContainer place, PlaceEntity entity) {
        if (entity.included("parentPlace")) {
            JiveObject parentObject = jiveObjectProvider.parent(place);
            if ((parentObject != null) && !(parentObject instanceof SystemContainer) &&
                    ((parentObject instanceof JiveContainer) || (parentObject instanceof User)))
            {
                entity.setParentPlace(summaryEntityConverter.convert(parentObject, null));
            }
        }
    }

    // to render page results correctly in search results
    protected void addResultsFields(TilePage tilePage, PageEntity entity) {
        if (entity.included("parentPlace")) {
            JiveObject parentObject = jiveObjectProvider.parent(tilePage);
            if ((parentObject != null) && !(parentObject instanceof SystemContainer) &&
                    ((parentObject instanceof JiveContainer) || (parentObject instanceof User)))
            {
                entity.setParentPlace(summaryEntityConverter.convert(parentObject, null));
            }
        }
    }

    // Criteria builder for people searches
    protected ProfileSearchCriteriaBuilder builder(List<Filter> exprs, String sort, boolean searchRequired) {

        // Construct the initial default criteria builder
        ProfileSearchCriteriaBuilder builder =
                new ProfileSearchCriteriaBuilder(authenticationProvider.getJiveUserID())
                    .setReturnDisabledUsers(false)
                    .setReturnExternalUsers(false)
                    .setReturnOnlineUsers(false)
                    .setReturnPartnerUsers(true)
                    .setReturnRegularUsers(true)
                    .setUserLocale(getLocale())
                ;

        for (Filter expr : exprs) {
            ProfileApplier applier = peopleAppliers.get(expr.getName());
            applier.apply(builder, expr);
        }

        // Apply the specified sort field and order
        ProfileSearchCriteria.Sort peopleSort = peopleSorts.get(sort);
        if (peopleSort == null) {
            throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_sort_type", sort)
                    .code(CoreErrorCodes.INVALID_SORT)
                    .build();
        }
        builder.setSort(peopleSort);

        return builder;
    }

    protected String validateSort(String sort) throws BadRequestException {
        if (sort == null) {
            sort = "relevanceDesc";
        }
        for (String valid : validSorts) {
            if (valid.equals(sort)) {
                return sort;
            }
        }
        throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_sort_expression", sort)
                .code(CoreErrorCodes.INVALID_SORT)
                .build();
    }

    // Criteria builder for contents and places searches
    protected SearchQueryCriteriaBuilder builder(List<Filter> exprs, boolean collapse, String sort, Map<String, Applier> appliers) {
        SearchQueryCriteriaBuilder builder = null;
        // Must use the "search" filter first because the query string is the constructor argument
        boolean hasLocaleFilter = false;
        boolean hasLanguageFilter = false;
        for (Filter expr : exprs) {
            if ("locale".equals(expr.getName())) {
                hasLocaleFilter = true;
            }
            if ("language".equals(expr.getName())) {
                hasLanguageFilter = true;
            }
            if ("search".equals(expr.getName())) {
                String queryString = StringUtils.join(expr.getParams(), " ");
                builder = new SearchQueryCriteriaBuilder(queryString)
                    .setPerformHighlighting(true);
            }
        }
        if (collapse) {
            builder.collapseParents();
        }
        // Then, apply all the other filters to customize the results
        for (Filter expr : exprs) {
            if (!"search".equals(expr.getName())) {
                Applier applier = appliers.get(expr.getName());
                if (applier == null) {
                    throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", expr.toString())
                            .code(CoreErrorCodes.FILTER_INVALID)
                            .build();
                }
                applier.apply(builder, expr);
            }
        }

        if ( !hasLocaleFilter && !hasLanguageFilter ) {
            // if no locale override, then apply user preferences locale
            builder.setLanguage(getUserLanguageString());
        } else {
            if ( hasLocaleFilter && !hasLanguageFilter ) {
                // a locale was set, but no language. in this case set the language to the locale
                builder.setLanguage( builder.getLocale().getLanguage() );
            }
        }

        // Apply sort criteria
        if ("relevanceDesc".equals(sort)) {
            builder = builder.setSort(RELEVANCE).setSortOrder(DESCENDING);
        }
        else if ("updatedAsc".equals(sort)) {
            builder = builder.setSort(MODIFICATION_DATE).setSortOrder(ASCENDING);
        }
        else if ("updatedDesc".equals(sort)) {
            builder = builder.setSort(MODIFICATION_DATE).setSortOrder(DESCENDING);
        }
        return builder;
    }

    protected Filter contentTypesFilter() {
        if (contentTypesFilter == null) {
            initTypesFilters();
        }
        return contentTypesFilter;
    }

    protected void initTypesFilters() {
        StringBuilder contentTypes = new StringBuilder();
        StringBuilder placeTypes = new StringBuilder();
        for (CoreObjectType coreObjectType : coreObjectTypeProvider.getObjectTypes()) {
            // SEARCH-660 - Temporarily remove bookmarks (favorites) from default content types list
            // SEARCH-857 - Remove Threads from default content types list
            // JIVE-20604 - Remove external URLs from default content types list
            // JIVE-37491 - Remove carousels from default content types
            // JIVE-49572 - Remove TilePage from defaults, added manually via own filter placeAndPageTypesFilter
            if (coreObjectType.getObjectType() != null &&
                    (coreObjectType.getObjectType().getID() == JiveConstants.FAVORITE ||
                     coreObjectType.getObjectType().getID() == JiveConstants.EXTERNAL_URL ||
                     coreObjectType.getObjectType().getID() == JiveConstants.THREAD ||
                     coreObjectType.getObjectType().getID() == CarouselContainerObjectType.CONTAINER_TYPE_ID ||
                     coreObjectType.getObjectType().getID() == TilePageObjectType.TILE_PAGE_TYPE_ID))
            {
                continue;
            }
            // See related patch in TypeApplier.apply()
            if (coreObjectType.isSearchable() && !coreObjectType.isPlace() &&
                !(coreObjectType.getObjectType() instanceof GroupObjectType) &&
                !(coreObjectType.getObjectType() instanceof UserObjectType))
            {
                if (contentTypes.length() > 0) {
                    contentTypes.append(",");
                }
                contentTypes.append(coreObjectType.getName());
            }
            if (coreObjectType.isPlace()) {
                if (placeTypes.length() > 0) {
                    placeTypes.append(",");
                }
                placeTypes.append(coreObjectType.getName());
            }
        }
        contentTypesFilter = new Filter("type(" + contentTypes.toString() + ")");
        placeTypesFilter = new Filter("type(" + placeTypes.toString() + ")");

        String placeAndPageTypes = placeTypes.length() > 0 ? placeTypes.toString() + "," + PageEntity.OBJECT_TYPE : PageEntity.OBJECT_TYPE;
        placeAndPageTypesFilter = new Filter("type(" + placeAndPageTypes + ")");
    }

    protected String keywords(List<Filter> exprs) {
        for (Filter expr : exprs) {
            if ("search".equals(expr.getName())) {
                StringBuilder keywords = new StringBuilder();
                for (String param : expr.getParams()) {
                    if (keywords.length() > 0) {
                        keywords.append(" ");
                    }
                    keywords.append(param);
                }
                return keywords.toString();
            }
        }
        return null;
    }

    protected Filter getFilter(List<Filter> exprs, String filterName) {
        for (Filter filter : exprs) {
            if (filter.getName().equals(filterName)) {
                return filter;
            }
        }
        return null;
    }

    protected Set<EntityDescriptor> getFilteredPlaces(Set<EntityDescriptor> containers, String depthValue) {
        return communityHierarchyHelper.getContainers(containers, depthValue);
    }

    /**
     * <p>Parse and return a Date object from a JSON formatted DATE string. This can either be a date only
     * (yyyy-mm-dd), or a full timestamp (the time part will be chopped off).</p>
     *
     * @param value the date string to be parsed
     */
    protected Date parseDateOnly(String value) throws BadRequestException {
        if ((value == null) || (value.length() < 10)) {
            throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_date_format", value)
                    .code(CoreErrorCodes.INVALID_DATE)
                    .build();
        }
        value = value.substring(0, 10);
        try {
            Date date = DATE_ONLY_FORMAT.get().parse(value);
            return date;
        }
        catch (ParseException e) {
            throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_date_format", value)
                    .code(CoreErrorCodes.INVALID_DATE)
                    .build();
        }
    }

    protected Filter placeTypesFilter() {
        if (placeTypesFilter == null) {
            initTypesFilters();
        }
        return placeTypesFilter;
    }

    protected Filter placeAndPageTypesFilter() {
        if (placeAndPageTypesFilter == null) {
            initTypesFilters();
        }
        return placeAndPageTypesFilter;
    }

    protected <T extends BaseApplier> List<Filter> validateFilters(List<String> filters, Map<String, T> appliers, boolean searchRequired) throws BadRequestException {
        boolean search = false;
        List<Filter> exprs = Lists.newArrayList();
        Set<String> names = Sets.newHashSet();
        for (String filter : filters) {
            Filter expr = null;
            try {
                expr = new Filter(filter);
            } catch (IllegalArgumentException e) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter)
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            if (names.contains(expr.getName())) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.duplicate_filter_name", expr.getName())
                        .code(CoreErrorCodes.FILTER_DUPLICATE)
                        .build();
            }
            names.add(expr.getName());
            if ("search".equals(expr.getName())) {
                search = true;
            }
            BaseApplier applier = appliers.get(expr.getName());
            if (applier == null) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter)
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            exprs.add(expr);
        }
        if (!search && searchRequired) {
            throw coreErrorBuilder.badRequestException("api.core.v3.error.missing_search_filter")
                    .code(CoreErrorCodes.FILTER_MISSING)
                    .build();
        }
        return exprs;
    }

    // ------------------------------------------------------------------------------------------------- Support Classes

    protected interface BaseApplier {
    }

    protected interface Applier extends BaseApplier {
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
            throws BadRequestException, ForbiddenException, NotFoundException;
    }

    protected interface ProfileApplier extends BaseApplier {
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
            throws BadRequestException, ForbiddenException, NotFoundException;
    }

    private String getUserLanguageString() {
        User user = BasePermHelper.getEffectiveUser();
        return searchActionHelper.getDefaultSearchLanguage(user);
    }

    protected class AfterApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            Date date = parseDateOnly(filter.getParams()[0]);
            builder.setAfterDate(date);
        }
    }

    protected class BeforeApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            Date date = parseDateOnly(filter.getParams()[0]);
            // The search subsystem rounds dates so if we ask for things before 3/27/2012 then
            // the search subsystem will change the date to 3/27/2012 11:59:59PM thus including the
            // entire 3/27/2012 day in the results. When asking after 3/27/2012, the search subsystem\
            // already includes the date so we do not want to include it again. Therefore we subtract one day
            date = DateUtils.hoursBefore(24, date);
            builder.setBeforeDate(date);
        }
    }

    protected class CompanyApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (companyProfileField == null) {
                throw coreErrorBuilder.serviceUnavailableException("api.core.v3.error.unsupported_filter_type", filter.getName())
                        .code(CoreErrorCodes.FILTER_NOT_SUPPORTED)
                        .build();
            }
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            List<ProfileSearchFilter> filters = builder.getFilters();
            if (filters == null) {
                filters = Lists.newArrayList();
                builder.setFilters(filters);
            }
            filters.add(new ProfileSearchFilter(companyProfileField.getID(), filter.getParams()[0]));
        }
    }

    protected class DepartmentApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (departmentProfileField == null) {
                throw coreErrorBuilder.serviceUnavailableException("api.core.v3.error.unsupported_filter_type", filter.getName())
                        .code(CoreErrorCodes.FILTER_NOT_SUPPORTED)
                        .build();
            }
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            List<ProfileSearchFilter> filters = builder.getFilters();
            if (filters == null) {
                filters = Lists.newArrayList();
                builder.setFilters(filters);
            }
            filters.add(new ProfileSearchFilter(departmentProfileField.getID(), filter.getParams()[0]));
        }
    }

    protected class DepthApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            if (!communityHierarchyHelper.isValidDepthFilterValue(filter.getParams()[0])) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
        }
    }

    protected class HireDateProfileApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (hireDateProfileField == null) {
                throw coreErrorBuilder.serviceUnavailableException("api.core.v3.error.unsupported_filter_type", filter.getName())
                        .code(CoreErrorCodes.FILTER_NOT_SUPPORTED)
                        .build();
            }
            if ((filter.getParams().length < 1) || (filter.getParams().length > 2)) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_dates", filter.toString())
                        .code(CoreErrorCodes.INVALID_DATE)
                        .build();
            }

            List<ProfileSearchFilter> filters = builder.getFilters();
            if (filters == null) {
                filters = Lists.newArrayList();
                builder.setFilters(filters);
            }
            ProfileSearchFilter searchFilter = new ProfileSearchFilter(hireDateProfileField.getID(), null);
            searchFilter.setMinValue(DateUtils.formatToLocalDate(parseDate(filter.getParams()[0]), getLocale()));
            if (filter.getParams().length == 2) {
                searchFilter.setMaxValue(DateUtils.formatToLocalDate(parseDate(filter.getParams()[1]), getLocale()));
            }
            filters.add(searchFilter);
        }
    }

    protected class IncludeDisabledApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            boolean apply = true;
            if (filter.getParams().length == 1) {
                apply = Boolean.parseBoolean(filter.getParams()[0]);
            } else if (filter.getParams().length > 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            builder.setReturnDisabledUsers(apply);
        }
    }

    protected class IncludeExternalApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            boolean apply = true;
            if (filter.getParams().length == 1) {
                apply = Boolean.parseBoolean(filter.getParams()[0]);
            } else if (filter.getParams().length > 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            builder.setReturnExternalUsers(apply);
        }
    }

    protected class IncludeOnlineApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            boolean apply = true;
            if (filter.getParams().length == 1) {
                apply = Boolean.parseBoolean(filter.getParams()[0]);
            } else if (filter.getParams().length > 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            builder.setReturnOnlineUsers(apply);
        }
    }

    protected class IncludePartnerApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            boolean apply = true;
            if (filter.getParams().length == 1) {
                apply = Boolean.parseBoolean(filter.getParams()[0]);
            } else if (filter.getParams().length > 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            builder.setReturnPartnerUsers(apply);
        }
    }

    protected class IncludeRegularApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            boolean apply = true;
            if (filter.getParams().length == 1) {
                apply = Boolean.parseBoolean(filter.getParams()[0]);
            } else if (filter.getParams().length > 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            builder.setReturnRegularUsers(apply);
        }
    }

    protected class LanguageApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            String languageString = filter.getParams()[0];
            Locale locale = LocaleUtils.localeCodeToLocale(languageString);
            if ( locale != null ) {
                builder = builder.setLanguage(locale.getLanguage());
            }
        }
    }

    protected class LastProfileUpdateProfileApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if ((filter.getParams().length < 1) || (filter.getParams().length > 2)) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_dates", filter.toString())
                        .code(CoreErrorCodes.INVALID_DATE)
                        .build();
            }
            Date minUpdatedDate = parseDate(filter.getParams()[0]);
            Date maxUpdatedDate = (filter.getParams().length == 2) ? parseDate(filter.getParams()[1]) : null;
            builder.setMinLastProfileUpdate(minUpdatedDate)
                   .setMaxLastProfileUpdate(maxUpdatedDate);
        }
    }

    protected class LocaleApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
             if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            String localeString = filter.getParams()[0];
            Locale locale = LocaleUtils.localeCodeToLocale(localeString);
            if ( locale != null ) {
                builder = builder.setLocale(locale);
            }
        }
    }

    protected class LocationApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (locationProfileField == null) {
                throw coreErrorBuilder.serviceUnavailableException("api.core.v3.error.unsupported_filter_type", filter.getName())
                        .code(CoreErrorCodes.FILTER_NOT_SUPPORTED)
                        .build();
            }
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            List<ProfileSearchFilter> filters = builder.getFilters();
            if (filters == null) {
                filters = Lists.newArrayList();
                builder.setFilters(filters);
            }
            filters.add(new ProfileSearchFilter(locationProfileField.getID(), filter.getParams()[0]));
        }
    }

    protected class MoreLikeApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            String contentURI = filter.getParams()[0];
            String contentID = extractContentID(contentURI);
            JiveContentObject content = resolveContentID(contentID);
            if (content == null) {
                throw coreErrorBuilder.notFoundException("api.core.v3.error.missing_content_id", contentID)
                        .code(CoreErrorCodes.CONTENT_INVALID_URI)
                        .build();
            }
            builder = builder.setFilteredObject(new EntityDescriptor(content));
        }
    }

    protected class NameOnlyApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            boolean apply = true;
            if (filter.getParams().length == 1) {
                apply = Boolean.parseBoolean(filter.getParams()[0]);
            } else if (filter.getParams().length > 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            if (apply) {
                builder.setSearchEmail(false)
                        .setSearchName(true)
                        .setSearchProfile(false)
                        .setSearchUsername(false)
                ;
            }
        }
    }

    protected class NameOrSubjectOnlyApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length > 0) {
                boolean apply = Boolean.parseBoolean(filter.getParams()[0]);
                if (!apply) {
                    return;
                }
            }
            builder.setSearchField(DefaultSearchIndexField.subject);
        }
    }

    protected class OutcomeApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
            throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            String outcomeName = filter.getParams()[0];
            OutcomeType outcomeType = outcomeTypeManager.getOutcomeType(outcomeName);
            if (null == outcomeType) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_outcometype_filter", outcomeName)
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            //builder.addOutcomeId(outcomeType.getID());
            builder.addOutcomeType(outcomeType.getID());
        }
    }

    protected class RtcTypesWithAuthorApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            try {
                if (filter.getParams().length != 1) {
                    throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                            .code(CoreErrorCodes.FILTER_INVALID)
                            .build();
                }
                boolean rtcFilterMode = Boolean.valueOf(filter.getParams()[0]);
                if (rtcFilterMode && rtcSettingsManager.isActive()) {
                    builder.setRtcAuthorID(authenticationProvider.getJiveUserID());
                    CoreObjectType rtcObjectType = coreObjectTypeProvider.getObjectTypeByName("interaction");
                    if (rtcObjectType != null) {
                        builder.addRtcObjectType(rtcObjectType.getObjectType().getID());
                    }
                    rtcObjectType = coreObjectTypeProvider.getObjectTypeByName("collaboration");
                    if (rtcObjectType != null) {
                        builder.addRtcObjectType(rtcObjectType.getObjectType().getID());
                    }
                }
            }
            catch (Exception e) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
        }
    }

//    protected class PhoneticApplier implements Applier {
//        @Override
//        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
//                throws BadRequestException, ForbiddenException, NotFoundException
//        {
//            throw coreErrorBuilder.notImplementedException("api.core.v3.error.filter_not_supported")
//                    .build();
//        }
//    }
//
    protected class PlaceApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length < 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            for (String placeURI : filter.getParams()) {
                JiveContainer place = resolvePlace(placeURI);
                builder = builder.addContainer(new EntityDescriptor(place));
            }
        }
    }

    protected class PublishedProfileApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if ((filter.getParams().length < 1) || (filter.getParams().length > 2)) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_dates", filter.toString())
                        .code(CoreErrorCodes.INVALID_DATE)
                        .build();
            }
            Date minCreatedDate = parseDate(filter.getParams()[0]);
            Date maxCreatedDate = (filter.getParams().length == 2) ? parseDate(filter.getParams()[1]) : null;
            builder.setMinCreationDate(minCreatedDate)
                    .setMaxCreationDate(maxCreatedDate);
        }
    }

    // Not used because search keywords are handled specially, but must be defined for filter validation
    protected class SearchApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {

        }
    }

    protected class SearchProfileApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length < 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            String keywords = StringUtils.join(filter.getParams(), ' ');
            builder.setKeywords(keywords);
        }
    }

    protected class SourceApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length < 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            for (String sourceURI : filter.getParams()) {
                Tile source = resolveExternalStreamDefinition(sourceURI);
                builder = builder.addSource(new EntityDescriptor(source));
            }
        }
    }

    protected class TagProfileApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length < 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            builder.setTags(Sets.newHashSet(filter.getParams()));
        }
    }

    protected class TitleApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (titleProfileField == null) {
                throw coreErrorBuilder.serviceUnavailableException("api.core.v3.error.unsupported_filter_type", filter.getName())
                        .code(CoreErrorCodes.FILTER_NOT_SUPPORTED)
                        .build();
            }
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            List<ProfileSearchFilter> filters = builder.getFilters();
            if (filters == null) {
                filters = Lists.newArrayList();
                builder.setFilters(filters);
            }
            filters.add(new ProfileSearchFilter(titleProfileField.getID(), filter.getParams()[0]));
        }
    }

    protected class TypeApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length < 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            for (String type : filter.getParams()) {
                CoreObjectType coreObjectType = coreObjectTypeProvider.getObjectTypeByName(type);
                if (coreObjectType == null) {
                    if (isKnownButAbsentType(type)) {
                        // Ignore requests to filter by this "unknown" type
                        continue;
                    }
                    else {
                        throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_object_type", type)
                                .code(CoreErrorCodes.TYPE_INVALID)
                                .build();
                    }
                }

                // SEARCH-660 Temporarily disallow asking for bookmarks (favorites) as a content type
                if ((coreObjectType.getObjectType() != null) &&
                    (coreObjectType.getObjectType().getID() == JiveConstants.FAVORITE)) {
                    throw coreErrorBuilder.badRequestException("api.core.v3.error.illegal_search_bookmarks")
                            .build();
                }

                // Don't request RTC types if RTC is off.  This would be better done in the initTypesFilters,
                // but that would require more synchronization
                if (!rtcSettingsManager.isActive() && (coreObjectType.getObjectType() != null) &&
                        (coreObjectType.getObjectType().getID() == JiveConstants.RTC_COLLABORATION ||
                                coreObjectType.getObjectType().getID() == JiveConstants.RTC_INTERACTION)) {
                    continue;
                }

                // See related change in initTypeFilters()
                builder.addObjectType(coreObjectType.getObjectType().getID());
            }
        }
    }

    protected class UpdatedProfileApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if ((filter.getParams().length < 1) || (filter.getParams().length > 2)) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_dates", filter.toString())
                        .code(CoreErrorCodes.INVALID_DATE)
                        .build();
            }
            Date minUpdatedDate = parseDate(filter.getParams()[0]);
            Date maxUpdatedDate = (filter.getParams().length == 2) ? parseDate(filter.getParams()[1]) : null;
            builder.setMinModificationDate(minUpdatedDate)
                   .setMaxModificationDate(maxUpdatedDate);
        }
    }

    protected class UserIDApplier implements Applier {
        @Override
        public void apply(SearchQueryCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            String personID = null;
            try {
                if (filter.getParams().length != 1) {
                    throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                            .code(CoreErrorCodes.FILTER_INVALID)
                            .build();
                }
                personID = extractPersonID(filter.getParams()[0]);
                long userID = validatePersonID(personID);
                userManager.getUser(userID);
                builder = builder.setUserID(userID);
            }
            catch (NumberFormatException e) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            catch (UserNotFoundException e) {
                throw coreErrorBuilder.notFoundException("api.core.v3.error.missing_user_id", personID)
                        .code(CoreErrorCodes.USER_MISSING_ID)
                        .build();
            }
        }
    }

    protected class ViewContentApplier implements ProfileApplier {
        @Override
        public void apply(ProfileSearchCriteriaBuilder builder, Filter filter)
                throws BadRequestException, ForbiddenException, NotFoundException
        {
            if (filter.getParams().length != 1) {
                throw coreErrorBuilder.badRequestException("api.core.v3.error.invalid_filter_expression", filter.toString())
                        .code(CoreErrorCodes.FILTER_INVALID)
                        .build();
            }
            // Do nothing since filtering is done in the API layer. Search service
            // does not have support for this filter (yet)
        }
    }

    protected Map<String, Applier> contentAppliers = Maps.newHashMap();
    {
        contentAppliers.put("after", new AfterApplier());
        contentAppliers.put("author", new UserIDApplier());
        contentAppliers.put("before", new BeforeApplier());
        contentAppliers.put("depth", new DepthApplier());
        contentAppliers.put("locale", new LocaleApplier());
        contentAppliers.put("language", new LanguageApplier());
        contentAppliers.put("morelike", new MoreLikeApplier());
        contentAppliers.put("outcomeType", new OutcomeApplier());
        contentAppliers.put("place", new PlaceApplier());
        contentAppliers.put("search", new SearchApplier());
        contentAppliers.put("source", new SourceApplier());
        contentAppliers.put("subject", new NameOrSubjectOnlyApplier()); // Deprecated, undocumented, replaced by subjectonly, but maintained for backwards compatibility
        contentAppliers.put("subjectonly", new NameOrSubjectOnlyApplier());
        contentAppliers.put("type", new TypeApplier());
        contentAppliers.put("rtcTypesWithAuthor", new RtcTypesWithAuthorApplier());
    }

    protected Map<String, ProfileApplier> peopleAppliers = Maps.newHashMap();
    {
        peopleAppliers.put("company", new CompanyApplier());
        peopleAppliers.put("department", new DepartmentApplier());
        peopleAppliers.put("hire-date", new HireDateProfileApplier());
        peopleAppliers.put("include-disabled", new IncludeDisabledApplier());
        peopleAppliers.put("include-external", new IncludeExternalApplier());
        peopleAppliers.put("include-online", new IncludeOnlineApplier());
        peopleAppliers.put("include-partner", new IncludePartnerApplier());
//        peopleAppliers.put("include-regular", new IncludeRegularApplier());
        peopleAppliers.put("lastProfileUpdate", new LastProfileUpdateProfileApplier());
        peopleAppliers.put("location", new LocationApplier());
        peopleAppliers.put("nameonly", new NameOnlyApplier());
        peopleAppliers.put("published", new PublishedProfileApplier());
        peopleAppliers.put("search", new SearchProfileApplier());
        peopleAppliers.put("tag", new TagProfileApplier());
        peopleAppliers.put("title", new TitleApplier());
        peopleAppliers.put("updated", new UpdatedProfileApplier());
        peopleAppliers.put("view-content", new ViewContentApplier());
    }

    protected Map<String, ProfileSearchCriteria.Sort> peopleSorts = Maps.newHashMap();
    {
        peopleSorts.put("dateJoinedAsc", ProfileSearchCriteria.DefaultSort.CREATION_DATE_ASC);
        peopleSorts.put("dateJoinedDesc", ProfileSearchCriteria.DefaultSort.CREATION_DATE);
        peopleSorts.put("firstNameAsc", ProfileSearchCriteria.DefaultSort.NAME);
        peopleSorts.put("lastNameAsc", ProfileSearchCriteria.DefaultSort.LAST_NAME);
        peopleSorts.put("relevanceDesc", ProfileSearchCriteria.DefaultSort.RELEVANCE);
        peopleSorts.put("statusLevelDesc", ProfileSearchCriteria.DefaultSort.STATUS_LEVEL);
        peopleSorts.put("updatedAsc", ProfileSearchCriteria.DefaultSort.MODIFICATION_DATE_ASC);
        peopleSorts.put("updatedDesc", ProfileSearchCriteria.DefaultSort.MODIFICATION_DATE);
        peopleSorts.put("lastProfileUpdateAsc", ProfileSearchCriteria.DefaultSort.LAST_PROFILE_UPDATE_ASC);
        peopleSorts.put("lastProfileUpdateDesc", ProfileSearchCriteria.DefaultSort.LAST_PROFILE_UPDATE);
    }

    protected Map<String, Applier> placesAppliers = Maps.newHashMap();
    {
        placesAppliers.put("nameonly", new NameOrSubjectOnlyApplier());
//        placesAppliers.put("owner", new UserIDApplier());
        placesAppliers.put("search", new SearchApplier());
        placesAppliers.put("type", new TypeApplier());
        placesAppliers.put("locale", new LocaleApplier());
        placesAppliers.put("language", new LanguageApplier());
    }

    protected Map<String, Applier> tagAppliers = Maps.newHashMap();
    {
        tagAppliers.put("search", new SearchApplier());
    }

    protected String[] validSorts = {
         "relevanceDesc",
         "updatedAsc",
         "updatedDesc"
    };

    protected void fireOriginEvent(String origin, SearchQueryCriteria criteria) {
        try {
            if (SearchEvent.Origin.valueOf(origin.toUpperCase()).equals(SearchEvent.Origin.SPOTLIGHT)) {
                eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SPOTLIGHT, criteria));
            }
        } catch (IllegalArgumentException e) {
            log.warn("unknown origin string: " + origin);
        }
    }

    protected void fireOriginEvent(String origin, ProfileSearchCriteria criteria) {
        try {
            if (SearchEvent.Origin.valueOf(origin.toUpperCase()).equals(SearchEvent.Origin.SPOTLIGHT)) {
                eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SPOTLIGHT, criteria));
            }
        } catch (IllegalArgumentException e) {
            log.warn("unknown origin string: " + origin);
        }
    }

    // ----------------------------------------------------------------------------------------------------- Injectables

    protected CommunityHierarchyHelper communityHierarchyHelper;
    protected EntitlementTypeProvider entitlementTypeProvider;
    protected EventDispatcher eventDispatcher;
    protected PersonEntityConverter personEntityConverter;
    protected ContentTagEntityConverter contentTagEntityConverter;
    protected ProfileFieldManager profileFieldManager;
    protected ProfileSearchQueryManager profileSearchQueryManager;
    protected SearchQueryManager searchQueryManager;
    protected boolean suggestedQueryEnabled = JiveGlobals.getJiveBooleanProperty("search.spellcheck.enabled", true);
    protected SummaryEntityConverter summaryEntityConverter;
    protected RTCAdapterProvider rtcAdapterProvider;
    protected RTCSettingsManager rtcSettingsManager;
    protected ExtendedInvitationManagerImpl extendedInvitationManagerImpl;
    protected SearchSettingsManager searchSettingsManager;
    protected PromotedResultsHelper promotedResultsHelper;

    @Required
    public void setCommunityHierarchyHelper(CommunityHierarchyHelper communityHierarchyHelper) {
        this.communityHierarchyHelper = communityHierarchyHelper;
    }

    @Required
    public void setEntitlementTypeProvider(EntitlementTypeProvider entitlementTypeProvider) {
        this.entitlementTypeProvider = entitlementTypeProvider;
    }

    @Required
    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    @Required
    public void setPersonEntityConverter(PersonEntityConverter personEntityConverter) {
        this.personEntityConverter = personEntityConverter;
    }

    @Required
    public void setContentTagEntityConverter(ContentTagEntityConverter contentTagEntityConverter) {
        this.contentTagEntityConverter = contentTagEntityConverter;
    }

    public void setProfileFieldManager(ProfileFieldManager profileFieldManager) {
        this.profileFieldManager = profileFieldManager;
        this.companyProfileField = profileFieldManager.getProfileField(DefaultProfileFields.COMPANY);
        this.departmentProfileField = profileFieldManager.getProfileField(DefaultProfileFields.DEPARTMENT);
        this.locationProfileField = profileFieldManager.getProfileField(DefaultProfileFields.LOCATION);
        this.titleProfileField = profileFieldManager.getProfileField(DefaultProfileFields.TITLE);
        this.hireDateProfileField = profileFieldManager.getProfileField(DefaultProfileFields.HIRE_DATE);
    }

    @Required
    public void setProfileSearchQueryManager(ProfileSearchQueryManager profileSearchQueryManager) {
        this.profileSearchQueryManager = profileSearchQueryManager;
    }

    @Required
    public void setSearchQueryManager(SearchQueryManager searchQueryManager) {
        this.searchQueryManager = searchQueryManager;
    }

    // Defaults to the system property listed above, or true if not set
    public void setSuggestedQueryEnabled(boolean suggestedQueryEnabled) {
        this.suggestedQueryEnabled = suggestedQueryEnabled;
    }

    @Required
    public void setSummaryEntityConverter(SummaryEntityConverter summaryEntityConverter) {
        this.summaryEntityConverter = summaryEntityConverter;
    }

    @Required
    public void setSearchActionHelper(SearchActionHelper searchActionHelper) {
        this.searchActionHelper = searchActionHelper;
    }

    @Required
    public void setSearchSettingsManager(SearchSettingsManager searchSettingsManager) {
        this.searchSettingsManager = searchSettingsManager;
    }

    @Required
    public void setPromotedResultsHelper(PromotedResultsHelper promotedResultsHelper) {
        this.promotedResultsHelper = promotedResultsHelper;
    }

    @Required
    public void setRtcAdapterProvider(RTCAdapterProvider rtcAdapterProvider) {
        this.rtcAdapterProvider = rtcAdapterProvider;
    }

    @Required
    public void setRtcSettingsManager(RTCSettingsManager rtcSettingsManager) {
        this.rtcSettingsManager = rtcSettingsManager;
    }

    @Required
    public void setExtendedInvitationManagerImpl(ExtendedInvitationManagerImpl extendedInvitationManagerImpl) {
        this.extendedInvitationManagerImpl = extendedInvitationManagerImpl;
    }
}
