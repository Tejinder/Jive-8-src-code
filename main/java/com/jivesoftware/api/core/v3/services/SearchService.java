/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.api.core.v3.services;

import com.google.common.collect.ImmutableMultimap;
import com.jivesoftware.api.core.v3.documentation.Undocumented;
import com.jivesoftware.api.core.v3.entities.ContentEntity;
import com.jivesoftware.api.core.v3.entities.Entities;
import com.jivesoftware.api.core.v3.entities.Entity;
import com.jivesoftware.api.core.v3.entities.PlaceEntity;
import com.jivesoftware.api.core.v3.entities.content.ContentTagEntity;
import com.jivesoftware.api.core.v3.entities.users.PersonEntity;
import com.jivesoftware.api.core.v3.exceptions.BadRequestException;
import com.jivesoftware.api.core.v3.exceptions.ForbiddenException;
import com.jivesoftware.api.core.v3.exceptions.NotFoundException;
import com.jivesoftware.api.core.v3.exceptions.OK;
import com.jivesoftware.api.core.v3.js.StaticJavaScriptMethod;
import com.jivesoftware.api.core.v3.providers.SearchProvider;
import com.jivesoftware.api.core.v3.types.CoreObjectType;
import com.jivesoftware.api.core.v3.types.CoreObjectTypeProvider;
import com.jivesoftware.api.core.v3.util.Paginator;
import com.jivesoftware.base.User;
import com.jivesoftware.base.aaa.AuthenticationProvider;
import com.jivesoftware.base.event.JivePropertyEvent;
import com.jivesoftware.base.event.SearchCompleteEventDetailExtractor;
import com.jivesoftware.base.event.SearchEvent;
import com.jivesoftware.base.event.v2.EventListener;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.service.client.JiveSingleTenantService;
import com.jivesoftware.service.logging.common.logger.Metric;
import com.jivesoftware.service.logging.common.logger.MetricLogger;
import com.jivesoftware.service.logging.common.logger.MetricLoggerFactory;
import com.jivesoftware.service.pojo.Tenancy;
import com.newrelic.api.agent.Trace;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * <p>Web services for searching content, people, and places.</p>
 */
@Path("/search")
@Produces("application/json")
public class SearchService extends AbstractService implements EventListener<JivePropertyEvent> {

    // Property identifying the minimum number of search results to NOT trigger a "did you mean" suggestion
    public static final String DID_YOU_MEAN_PROPERTY = "jive.search.did_you_mean.minimum";
    public static final int DID_YOU_MEAN_VALUE = 5;

    // -------------------------------------------------------------------------------------------------- Public Methods

    /**
     * <p>Search for and return content objects that match the specified filter criteria, in the specified order.</p>
     *
     * <p>This service supports the following filters.  Only one filter of each type is allowed.
     * Parameters, when used, should be wrapped
     * in parentheses, and multiple values separated by commas.  See the examples for clarification.</p>
     *
     * <table class="refTable">
     *     <thead>
     *         <tr>
     *             <th>Filter</th>
     *             <th>Params</th>
     *             <th>Example</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>after</td>
     *             <td>
     *                 Select content objects last modified after the specified date/time.
     *             </td>
     *             <td>?filter=after(2012-01-31T19:13:29.851+0000)</td>
     *         </tr>
     *         <tr>
     *             <td>author</td>
     *             <td>
     *                 Select content objects authored by the specified person. The parameter value must be either a
     *                 full or partial (starting with "/people/") URI for the desired person.
     *             </td>
     *             <td>?filter=author(/people/4321)</td>
     *         </tr>
     *         <tr>
     *             <td>before</td>
     *             <td>
     *                 Select content objects last modified before the specified date/time.
     *             </td>
     *             <td>?filter=after(2012-01-31T19:13:29.851+0000)</td>
     *         </tr>
     *         <tr>
     *             <td>morelike</td>
     *             <td>
     *                 Select content objects that are similar to the specified content object.
     *             </td>
     *             <td>?filter=morelike(/content/1234)</td>
     *         </tr>
     *         <tr>
     *             <td>place</td>
     *             <td>
     *                 Select content objects that are contained in the specified place or places.  The parameter
     *                 value(s) must be full or partial (starting with "/places/") URI for the desired place(s).
     *             </td>
     *             <td>?filter=place(/places/2222,places/3333)</td>
     *         </tr>
     *         <tr>
     *             <td>search</td>
     *             <td>
     *                 One or more search terms, separated by commas. You must escape
     *                 any of the following special characters embedded in the search terms:  comma (","),
     *                 backslash ("\"), left parenthesis ("("), and right parenthesis (")"), by preceding them
     *                 with a backslash. Remember to URL encode any special character.
     *             </td>
     *             <td>?filter=search(test,report) or ?filter=search(10%2C000)</td>
     *         </tr>
     *         <tr>
     *             <td>source</td>
     *             <td>
     *                 Return external stream entries that were originated by one or more specified external stream
     *                 definitions.  This only makes sense if you also add a <code>type(extStreamActivity)</code> filter.
     *             </td>
     *             <td>?filter=source(/extstreams/123,/extstreams/456)</td>
     *         </tr>
     *         <tr>
     *             <td>subjectonly</td>
     *             <td>
     *                 Optional boolean value indicating whether or not to limit search results
     *                 to only content objects whose subject matches the search keywords.  Defaults to <code>true</code>.
     *             </td>
     *             <td>?filter=subjectonly(true) or ?filter=subjectonly</td>
     *         </tr>
     *         <tr>
     *             <td>type</td>
     *             <td>
     *                 One or more object types of desired content types separated by commas.
     *             </td>
     *             <td>?filter=type(document,post)</td>
     *         </tr>
     *         <tr>
     *             <td>depth</td>
     *             <td>
     *                 Select content objects recursively given a depth level. Only applies when a place filter is also
     *                 specified and that place is a community object type, since social groups aren't hierarchical.
     *                 The default depth level is NONE, which is not recursive and will only return content in the
     *                 immediate space. CHILDREN will include the specified space and its direct subspaces, i.e. a
     *                 recursive search one level deep. ALL will perform a fully recursive search using all subspaces.
     *                 For performance reasons you cannot do a recursive search from the root container, and there is a
     *                 system property that can limit the total number of subspaces used (jive.searchProvider.placeFilter.maxCommunities)
     *             </td>
     *             <td>?filter=depth(NONE) or ?filter=depth(CHILDREN) or ?filter=depth(ALL)</td>
     *         </tr>
     *         <tr>
     *             <td>outcomeType</td>
     *             <td>
     *                 One outcome type. Some values are: DECISION, FINALIZED, PENDING, HELPFUL, RESOLVED, SUCCESS,
     *                 OUTDATED, OFFICIAL or WIP.
     *             </td>
     *             <td>?filter=outcomeType(HELPFUL)</td>
     *         </tr>
     *         <tr>
     *             <td>language</td>
     *             <td>
     *                 One language.
     *             </td>
     *             <td>?filter=language(en)</td>
     *         </tr>
     *         <tr>
     *             <td>locale</td>
     *             <td>
     *                 One locale.
     *             </td>
     *             <td>?filter=locale(en_US)</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * <p>This service supports the following sort types.</p>
     *
     * <table class="refTable">
     *     <thead>
     *         <tr>
     *             <th>Sort</th>
     *             <th>Description</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>relevanceDesc</td>
     *             <td>Sort by relevance, in descending order.  This is the default sort order.</td>
     *         </tr>
     *         <tr>
     *             <td>updatedAsc</td>
     *             <td>Sort by the date this content object was most recently updated, in ascending order.</td>
     *         </tr>
     *         <tr>
     *             <td>updatedDesc</td>
     *             <td>Sort by the date this content object was most recently updated, in descending order.</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * <table class="refTable">
     *     <thead>
     *         <tr>
     *             <th>Directive</th>
     *             <th>Params</th>
     *             <th>Example</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>include_rtc</td>
     *             <td>
     *                 Optional. Indicates whether RTC entries should be returned instead of being
     *                 transformed into direct messages. Default: false if unspecified, or true if
     *                 specified with no params.
     *             </td>
     *             <td>?directive=include_rtc</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * <p>The returned list may contain a mixture of content object entities of various types.  On any given
     * content object entity, use the <code>type</code> field to determine the type of that particular content object.</p>
     *
     * @param filters Filter expression(s) used to select matching results
     * @param directives Special directive(s) used to transform results
     * @param collapse Flag indicating that search results should be "collapsed" if they have the same parent
     * @param highlight Flag indicating that search results will include fields relating to highlighting. Since 3.8
     * @param socialSearch Flag indicating that search results will be affected by social signals
     * @param returnScore Flag indicating that search service should return result scores
     * @param sort Sort expression used to order results
     * @param startIndex Zero-relative index of the first matching result to return
     * @param count Maximum number of matches to return
     * @param origin Client that sent this search request
     * @param fields Fields to include in returned matches
     *
     * @retrieves {@link ContentEntity}[] for matching content objects
     * @exception OK Request was successful
     * @exception BadRequestException An input parameter is missing or malformed
     * @exception ForbiddenException You attempted to reference an object that you do not have access to
     * @exception NotFoundException You attempted to reference an object that does not exist
     */
    @GET
    @Path("/contents")
    @StaticJavaScriptMethod(name = "osapi.jive.corev3.contents.search")
    @Trace
    public Entities<Entity> searchContents(@QueryParam("filter") List<String> filters,
                                           @QueryParam("directive") List<String> directives,
                                           @QueryParam("collapse") @DefaultValue("false") boolean collapse,
                                           @QueryParam("highlight") @DefaultValue("true") boolean highlight,
                                           @QueryParam("socialSearch") @DefaultValue("true") boolean socialSearch,
                                           @QueryParam("returnScore") @DefaultValue("false") boolean returnScore,
                                           @QueryParam("sort") @DefaultValue("relevanceDesc") String sort,
                                           @QueryParam("startIndex") @DefaultValue("0") int startIndex,
                                           @QueryParam("count") @DefaultValue("25") int count,
                                           @QueryParam("origin") @DefaultValue("unknown") String origin,
                                           @QueryParam("fields") String fields)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
       
    	System.out.println("Inside SearchService WEB SERVICE v3  ==");
    	List<Entity> results = searchProvider.searchContent(filters, directives, collapse, highlight, socialSearch,
                returnScore, sort, startIndex, count, origin, fields);
        Paginator paginator = paginationHelper.getPaginator(startIndex, count, null);
        ImmutableMultimap.Builder<String, String> parameterBuilder = ImmutableMultimap.builder();
        addQueryParam("collapse", collapse, parameterBuilder);
        addQueryParam("sort", sort, parameterBuilder);
        addQueryParam("fields", fields, parameterBuilder);
        addQueryParam("filter", filters, parameterBuilder);
        addQueryParam("origin", origin, parameterBuilder);
        addQueryParam("highlight", highlight, parameterBuilder);
        addQueryParam("socialSearch", socialSearch, parameterBuilder);
        addQueryParam("returnScore", returnScore, parameterBuilder);
        Entities<Entity> entities = attachPagination(paginator, results, parameterBuilder.build());
        if (results.size() < didYouMeanMinimum) {
            entities.setSuggestedQuery(searchProvider.getCorrectedQuery(filters));
        }
        return entities;
    }

    /**
     * <p>Search for and return people that match the specified filter criteria, in the specified order.</p>
     *
     * <p>This service supports the following filters.  Only one filter of each type is allowed.
     * Parameters, when used, should be wrapped
     * in parentheses, and multiple values separated by commas.  See the examples for clarification.</p>
     *
     * <table class="refTable">
     *     <thead>
     *         <tr>
     *             <th>Filter</th>
     *             <th>Params</th>
     *             <th>Example</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>company</td>
     *             <td>
     *                 Single value to match against the <code>Company</code> profile field.
     *             </td>
     *             <td>?filter=company(Jive+Software)</td>
     *         </tr>
     *         <tr>
     *             <td>department</td>
     *             <td>
     *                 Single value to match against the <code>Department</code> profile field.
     *             </td>
     *             <td>?filter=department(Engineering)</td>
     *         </tr>
     *         <tr>
     *             <td>hire-date</td>
     *             <td>
     *                 One or two dates in ISO-8601 format.  One date indicates selection of all people
     *                 hired on or after the specified date.  Two dates indicates selection of all people
     *                 hired between the specified dates.
     *             </td>
     *             <td>?filter=hire-date(2012-01-31T22:46:12.044%2B0000,2012-12-03T22:46:12.044%2B0000)</td>
     *         </tr>
     *         <tr>
     *             <td>include-disabled</td>
     *             <td>
     *                 Optional boolean value indicating whether disabled users should be returned
     *                 (without a filter, defaults to false).
     *             </td>
     *             <td>?filter=include-disabled or ?filter=include-disabled(true)</td>
     *         </tr>
     *         <tr>
     *             <td>include-external</td>
     *             <td>
     *                 Optional boolean value indicating whether external (non-person) users should be returned
     *                 (without a filter, defaults to false).
     *             </td>
     *             <td>?filter=include-external or ?filter=include-external(true)</td>
     *         </tr>
     *         <tr>
     *             <td>include-online</td>
     *             <td>
     *                 Optional boolean value indicating whether only online users should be returned
     *                 (without a filter, defaults to false).
     *             </td>
     *             <td>?filter=include-online or ?filter=include-online(true)</td>
     *         </tr>
     *         <tr>
     *             <td>include-partner</td>
     *             <td>
     *                 Optional boolean value indicating whether partner (external contributor) users should be returned
     *                 (without a filter, defaults to true).
     *             </td>
     *             <td>?filter=include-partner(false)</td>
     *         </tr>
     *         <tr>
     *             <td>lastProfileUpdate</td>
     *             <td>
     *                 One or two timestamps in ISO-8601 format.  If one timestamp is specified, all persons
     *                 who have updated their profile since that timestamp will be selected.
     *                 If two timestamps are specified, all persons
     *                 who updated their profile in the specified range will be selected.
     *             </td>
     *             <td>?filter=lastProfileUpdate(2012-01-31T22:46:12.044%2B0000,2012-12-03T22:46:12.044%2B0000)</td>
     *         </tr>
     *         <tr>
     *             <td>location</td>
     *             <td>
     *                 Single value to match against the <code>Location</code> profile field.
     *             </td>
     *             <td>?filter=location(Portland)</td>
     *         </tr>
     *         <tr>
     *               <td>nameonly</td>
     *               <td>
     *                   Optional boolean value indicating whether or not to limit search results
     *                   to only people that match by name.  Without a filter, defaults to <code>false</code>.
     *               </td>
     *               <td>?filter=nameonly(true) or ?filter=nameonly</td>
     *         </tr>
     *         <tr>
     *             <td>published</td>
     *             <td>
     *                 One or two timestamps in ISO-8601 format.  If one timestamp is specified, all persons
     *                 created since that timestamp will be selected.  If two timestamps are specified, all persons
     *                 created in the specified range will be selected.
     *             </td>
     *             <td>?filter=updated(2012-01-31T22:46:12.044%2B0000,2012-12-03T22:46:12.044%2B0000)</td>
     *         </tr>
     *         <tr>
     *             <td>search</td>
     *             <td>
     *                 One or more search terms, separated by commas.  You must escape
     *                 any of the following special characters embedded in the search terms:  comma (","),
     *                 backslash ("\"), left parenthesis ("("), and right parenthesis (")") by preceding them
     *                 with a backslash.  This field is required on a search. Remember to URL encode any
     *                 special character.
     *             </td>
     *             <td>?filter=search(test,report) or ?filter=search(10%2C000)</td>
     *         </tr>
     *         <tr>
     *             <td>tag</td>
     *             <td>
     *                 One or more tag values, separated by commas.  A match on any of the tags will cause
     *                 this person to be returned.
     *             </td>
     *             <td>?filter=tag(sales,performance)</td>
     *         </tr>
     *         <tr>
     *             <td>title</td>
     *             <td>
     *                 Single value to match against the <code>Title</code> profile field.
     *             </td>
     *             <td>?filter=title(Marketing+Manager)</td>
     *         </tr>
     *         <tr>
     *             <td>updated</td>
     *             <td>
     *                 One or two timestamps in ISO-8601 format.  If one timestamp is specified, all persons
     *                 updated since that timestamp will be selected.  If two timestamps are specified, all persons
     *                 updated in the specified range will be selected.
     *             </td>
     *             <td>?filter=updated(2012-01-31T22:46:12.044%2B0000,2012-12-03T22:46:12.044%2B0000)</td>
     *         </tr>
     *         <tr>
     *             <td>view-content</td>
     *             <td>
     *                 Only persons that can view the requested content will be selected. <b>Since: 3.4</b>
     *             </td>
     *             <td>?filter=view-content(/content/1234)</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * <p>This service supports the following sort types.</p>
     *
     * <table class="refTable">
     *     <thead>
     *         <tr>
     *             <th>Sort</th>
     *             <th>Description</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>dateJoinedAsc</td>
     *             <td>Sort by joined date in ascending order.</td>
     *         </tr>
     *         <tr>
     *             <td>dateJoinedDesc</td>
     *             <td>Sort by joined date in descending order.</td>
     *         </tr>
     *         <tr>
     *             <td>firstNameAsc</td>
     *             <td>Sort by first name in ascending order.</td>
     *         </tr>
     *         <tr>
     *             <td>lastNameAsc</td>
     *             <td>Sort by last name in ascending order.</td>
     *         </tr>
     *         <tr>
     *             <td>lastProfileUpdateAsc</td>
     *             <td>Sort by last profile update date/time in ascending order.</td>
     *         </tr>
     *         <tr>
     *             <td>lastProfileUpdateDesc</td>
     *             <td>Sort by last profile update date/time in descending order.</td>
     *         </tr>
     *         <tr>
     *             <td>relevanceDesc</td>
     *             <td>Sort by relevance, in descending order.  This is the default sort order.</td>
     *         </tr>
     *         <tr>
     *             <td>statusLevelDesc</td>
     *             <td>Sort by status level in descending order.</td>
     *         </tr>
     *         <tr>
     *             <td>updatedAsc</td>
     *             <td>Sort by the date this person was most recently updated, in ascending order.</td>
     *         </tr>
     *         <tr>
     *             <td>updatedDesc</td>
     *             <td>Sort by the date this person was most recently updated, in descending order.</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * @param filters Filter expression(s) used to select matching results
     * @param viewContentURI Optional. viewContent inside of Jive that is inside of Person will indicate
     *                       if the user can see the requested content. <i>Since 3.5</i>
     * @param sort Sort expression used to order results
     * @param startIndex Zero-relative index of the first matching result to return
     * @param count Maximum number of matches to return
     * @param origin Client that sent this search request
     * @param fields Fields to include in returned matches
     *
     * @retrieves {@link PersonEntity}[] listing the matching people
     * @exception OK Request was successful
     * @exception BadRequestException An input parameter is missing or malformed
     * @exception ForbiddenException You attempted to reference an object that you do not have access to
     * @exception NotFoundException You attempted to reference an object that does not exist
     */
    @GET
    @Path("/people")
    @StaticJavaScriptMethod(name = "osapi.jive.corev3.people.search")
    @Trace
    public Entities<PersonEntity> searchPeople(@QueryParam("filter") List<String> filters,
                                               @QueryParam("viewContentURI") String viewContentURI,
                                               @QueryParam("sort") @DefaultValue("relevanceDesc") String sort,
                                               @QueryParam("startIndex") @DefaultValue("0") int startIndex,
                                               @QueryParam("count") @DefaultValue("25") int count,
                                               @QueryParam("origin") @DefaultValue("unknown") String origin,
                                               @QueryParam("fields") String fields)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
        List<PersonEntity> results = searchProvider.searchPeople(filters, viewContentURI, sort, startIndex, count, origin, fields, true);
        Paginator paginator = paginationHelper.getPaginator(startIndex, count, null);
        ImmutableMultimap.Builder<String, String> parameterBuilder = ImmutableMultimap.builder();
        addQueryParam("sort", sort, parameterBuilder);
        addQueryParam("fields", fields, parameterBuilder);
        addQueryParam("filter", filters, parameterBuilder);
        addQueryParam("origin", origin, parameterBuilder);
        Entities<PersonEntity> entities = attachPagination(paginator, results, parameterBuilder.build());
        if (results.size() < didYouMeanMinimum) {
            entities.setSuggestedQuery(searchProvider.getCorrectedQuery(filters));
        }
        return entities;
    }

    /**
     * UNDOCUMENTED -- currently only supports searching places and pages.  Future enhancements should make this enpoint
     * generic to return all searchable types based on the filters
     *
     * @param filters
     * @param collapse
     * @param highlight
     * @param socialSearch
     * @param returnScore
     * @param sort
     * @param startIndex
     * @param count
     * @param origin
     * @param fields
     * @return
     * @throws BadRequestException
     * @throws ForbiddenException
     * @throws NotFoundException
     */
    @GET
    @Path("/")
    @Undocumented
    @Trace
    public Entities<Entity> search(@QueryParam("filter") List<String> filters,
                                @QueryParam("collapse") @DefaultValue("false") boolean collapse,
                                @QueryParam("highlight") @DefaultValue("true") boolean highlight,
                                @QueryParam("socialSearch") @DefaultValue("true") boolean socialSearch,
                                @QueryParam("returnScore") @DefaultValue("false") boolean returnScore,
                                @QueryParam("sort") @DefaultValue("relevanceDesc") String sort,
                                @QueryParam("startIndex") @DefaultValue("0") int startIndex,
                                @QueryParam("count") @DefaultValue("25") int count,
                                @QueryParam("origin") @DefaultValue("unknown") String origin,
                                @QueryParam("fields") String fields)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
        List<Entity> results = searchProvider.searchPlacesAndPages(filters, collapse, highlight, socialSearch,
                returnScore, sort, startIndex, count, origin, fields);
        Paginator paginator = paginationHelper.getPaginator(startIndex, count, null);
        ImmutableMultimap.Builder<String, String> parameterBuilder = ImmutableMultimap.builder();
        addQueryParam("collapse", collapse, parameterBuilder);
        addQueryParam("sort", sort, parameterBuilder);
        addQueryParam("fields", fields, parameterBuilder);
        addQueryParam("filter", filters, parameterBuilder);
        addQueryParam("origin", origin, parameterBuilder);
        addQueryParam("highlight", highlight, parameterBuilder);
        addQueryParam("socialSearch", socialSearch, parameterBuilder);
        addQueryParam("returnScore", returnScore, parameterBuilder);
        Entities<Entity> entities = attachPagination(paginator, results, parameterBuilder.build());
        if (results.size() < didYouMeanMinimum) {
            entities.setSuggestedQuery(searchProvider.getCorrectedQuery(filters));
        }
        return entities;
    }

    /**
     * <p>Search for and return places that match the specified filter criteria, in the specified order.</p>
     *
     * <p>This service supports the following filters.  Only one filter of each type is allowed.
     * Parameters, when used, should be wrapped
     * in parentheses, and multiple values separated by commas.  See the examples for clarification.</p>
     *
     * <table class="refTable">
     *     <thead>
     *         <tr>
     *             <th>Filter</th>
     *             <th>Params</th>
     *             <th>Example</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *               <td>nameonly</td>
     *               <td>
     *                   Optional boolean value indicating whether or not to limit search results
     *                   to only places whose name matches the search keywords.  Defaults to <code>true</code>.
     *               </td>
     *               <td>?filter=nameonly(true) or ?filter=nameonly</td>
     *         </tr>
     *         <tr>
     *             <td>search</td>
     *             <td>
     *                 One or more search terms, separated by commas. You must escape
     *                 any of the following special characters embedded in the search terms:  comma (","),
     *                 backslash ("\"), left parenthesis ("("), and right parenthesis (")") by preceding them
     *                 with a backslash. Remember to URL encode any special character.
     *             </td>
     *             <td>?filter=search(test,report) or ?filter=search(10%2C000)</td>
     *         </tr>
     *         <tr>
     *             <td>type</td>
     *             <td>
     *                 One or more object types of desired contained places (blog, group, project, space)
     *                 separated by commas.
     *             </td>
     *             <td>?filter=type(blog,project)</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * <p>This service supports the following sort types.</p>
     *
     * <table class="refTable">
     *     <thead>
     *         <tr>
     *             <th>Sort</th>
     *             <th>Description</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>relevanceDesc</td>
     *             <td>Sort by relevance, in descending order.  This is the default sort order.</td>
     *         </tr>
     *         <tr>
     *             <td>updatedAsc</td>
     *             <td>Sort by the date this place was most recently updated, in ascending order.</td>
     *         </tr>
     *         <tr>
     *             <td>updatedDesc</td>
     *             <td>Sort by the date this place was most recently updated, in descending order.</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * <p>The returned list may contain a mixture of place entities of various types.  On any given place entity,
     * use the <code>type</code> field to determine the type of that particular place.</p>
     *
     * @param filters Filter expression(s) used to select matching results
     * @param collapse Flag indicating that search results should be "collapsed" if they have the same parent
     * @param highlight Flag indicating that search results will include fields relating to highlighting. Since 3.8
     * @param socialSearch Flag indicating that search results will be affected by social signals
     * @param returnScore Flag indicating that search service should return result scores
     * @param sort Sort expression used to order results
     * @param startIndex Zero-relative index of the first matching result to return
     * @param count Maximum number of matches to return
     * @param origin Client that sent this search request
     * @param fields Fields to include in returned matches
     *
     * @retrieves {@link PlaceEntity}[] containing the matching places
     * @exception OK Request was successful
     * @exception BadRequestException If an input parameter is missing or malformed
     * @exception ForbiddenException If you attempt to reference an object that you do not have access to
     * @exception NotFoundException If you attempt to reference an object that does not exist
     */
    @GET
    @Path("/places")
    @StaticJavaScriptMethod(name = "osapi.jive.corev3.places.search")
    @Trace
    public Entities<PlaceEntity> searchPlaces(@QueryParam("filter") List<String> filters,
            @QueryParam("collapse") @DefaultValue("false") boolean collapse,
            @QueryParam("highlight") @DefaultValue("true") boolean highlight,
            @QueryParam("socialSearch") @DefaultValue("true") boolean socialSearch,
            @QueryParam("returnScore") @DefaultValue("false") boolean returnScore,
            @QueryParam("sort") @DefaultValue("relevanceDesc") String sort,
            @QueryParam("startIndex") @DefaultValue("0") int startIndex,
            @QueryParam("count") @DefaultValue("25") int count,
            @QueryParam("origin") @DefaultValue("unknown") String origin,
            @QueryParam("fields") String fields)
            throws BadRequestException, ForbiddenException, NotFoundException
    {
        List<PlaceEntity> results = searchProvider.searchPlaces(filters, collapse, highlight, socialSearch,
                returnScore, sort, startIndex, count, origin, fields);
        Paginator paginator = paginationHelper.getPaginator(startIndex, count, null);
        ImmutableMultimap.Builder<String, String> parameterBuilder = ImmutableMultimap.builder();
        addQueryParam("collapse", collapse, parameterBuilder);
        addQueryParam("sort", sort, parameterBuilder);
        addQueryParam("fields", fields, parameterBuilder);
        addQueryParam("filter", filters, parameterBuilder);
        addQueryParam("origin", origin, parameterBuilder);
        addQueryParam("highlight", highlight, parameterBuilder);
        addQueryParam("socialSearch", socialSearch, parameterBuilder);
        addQueryParam("returnScore", returnScore, parameterBuilder);
        Entities<PlaceEntity> entities = attachPagination(paginator, results, parameterBuilder.build());
        if (results.size() < didYouMeanMinimum) {
            entities.setSuggestedQuery(searchProvider.getCorrectedQuery(filters));
        }
        return entities;
    }

    @GET
    @Path("/tags")
    @StaticJavaScriptMethod(name = "osapi.jive.corev3.tags.search")
    @Trace
    public Entities<ContentTagEntity> searchTags(@QueryParam("filter") List<String> filters,
                                                 @QueryParam("sort") @DefaultValue("relevanceDesc") String sort,
                                                 @QueryParam("startIndex") @DefaultValue("0") int startIndex,
                                                 @QueryParam("count") @DefaultValue("25") int count,
                                                 @QueryParam("origin") @DefaultValue("unknown") String origin,
                                                 @QueryParam("fields") String fields)
        throws BadRequestException, ForbiddenException, NotFoundException
    {
        List<ContentTagEntity> results = searchProvider.searchTags(filters, sort, startIndex, count, origin, fields);
        Paginator paginator = paginationHelper.getPaginator(startIndex, count, null);
        ImmutableMultimap.Builder<String, String> parameterBuilder = ImmutableMultimap.builder();
        addQueryParam("sort", sort, parameterBuilder);
        addQueryParam("fields", fields, parameterBuilder);
        addQueryParam("filter", filters, parameterBuilder);
        addQueryParam("origin", origin, parameterBuilder);
        Entities<ContentTagEntity> entities = attachPagination(paginator, results, parameterBuilder.build());
        return entities;
    }

    /**
     * <p>Internal REST endpoint for logging Jive user interactions with the Search UI in JSON format</p>
     */
    @POST
    @Path("/monitor")
    @Consumes("application/json")
    @Trace
    public void logSearchQuery(String jsonString) {

        User user = authenticationProvider.getJiveUser();
        Tenancy tenancy = jiveSingleTenantService.getTenancyProvider().tenancy(user.getID());

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            jsonObject.put("tenantID", tenancy.getTenantId());
            jsonObject.put("userID", user.getID());
            translateObjectTypes(jsonObject);

            String modifiedJsonString = jsonObject.toString();
            searchUserTrackingLog.info(modifiedJsonString);
            dispatchAnalyticsAndMetrics(modifiedJsonString);
        }
        catch (Exception exception) {
            log.warn("Unable to build json object for query tracking from input: " + jsonString, exception);
        }
    }

    private void translateObjectTypes(JSONObject jsonObject) throws JSONException {
        JSONArray queries = jsonObject.getJSONArray("queries");

        for (int i=0; i<queries.length(); i++) {
            JSONObject query = queries.getJSONObject(i);
            JSONArray results = query.getJSONArray("results");

            for (int j=0; j<results.length(); j++) {
                replaceTypeField(results.getJSONObject(j));
            }

            JSONObject choice = null;
            try {
                choice = query.getJSONObject("choice");
            }
            catch (JSONException e)  { /* I don't care */ }

            if (choice != null) {
                if (!choice.has("item")) {
                    throw coreErrorBuilder.badRequestException("api.core.v3.error.missing_field", "item").build();
                }
                replaceTypeField(choice.getJSONObject("item"));
            }
        }
    }
    
    private void replaceTypeField(JSONObject objectWithTypeField) throws JSONException {
        String objectTypeString = objectWithTypeField.getString("type");

        if (!StringUtils.isNumeric(objectTypeString)) {
            CoreObjectType objectType = coreObjectTypeProvider.getObjectTypeByName(objectTypeString);

            if (objectType != null) {
                objectWithTypeField.put("type", String.valueOf(objectType.getObjectType().getID()));
            }
        }
    }

    private void dispatchAnalyticsAndMetrics(String searchNotificationJson) {
        SearchCompleteEventDetailExtractor detailExtractor =
                new SearchCompleteEventDetailExtractor(searchNotificationJson);

        final SearchEvent.Type searchType = detailExtractor.getSearchType();

        if ( searchType != null ) {
            // Parse succeeded.

            String queryTerms = detailExtractor.getQueryTerms();
            EntityDescriptor selectedObject = detailExtractor.getSelectedObject();
            String finalQuery = detailExtractor.getFinalQuery();

            SearchEvent event =
                    new SearchEvent(searchType, queryTerms, selectedObject, finalQuery);

            searchProvider.getEventDispatcher().fire(event);

            Metric metric
                    = METRIC_LOGGER.metric("METRICS_SEARCH_COMPLETE")
                    .put("searchType", searchType.toString())
                    .put("queryTerms", queryTerms);

            if ( selectedObject != null ) {
                metric.put("selectedObjectType", selectedObject.getObjectType());
                metric.put("selectedObjectId", selectedObject.getID());
            }

            metric.send();
        }
    }

    // ------------------------------------------------------------------------------------------------- Support Methods

    protected int didYouMeanMinimum = JiveGlobals.getJiveIntProperty(DID_YOU_MEAN_PROPERTY, DID_YOU_MEAN_VALUE);

    @Override
    public void handle(JivePropertyEvent event) {
        if (DID_YOU_MEAN_PROPERTY.equals(event.getName())) {
            didYouMeanMinimum = JiveGlobals.getJiveIntProperty(DID_YOU_MEAN_PROPERTY, DID_YOU_MEAN_VALUE);
        }
    }

    // ----------------------------------------------------------------------------------------------------- Injectables

    protected SearchProvider searchProvider;
    protected AuthenticationProvider authenticationProvider;
    protected JiveSingleTenantService jiveSingleTenantService;
    protected CoreObjectTypeProvider coreObjectTypeProvider;

    private static final Logger log = LogManager.getLogger(SearchService.class);
    private static final Logger searchUserTrackingLog = LogManager.getLogger("SearchUserTracking");
    private static final MetricLogger METRIC_LOGGER = MetricLoggerFactory.getLogger();

    @Required
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Required
    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Required
    public void setJiveSingleTenantService(JiveSingleTenantService jiveSingleTenantService) {
        this.jiveSingleTenantService = jiveSingleTenantService;
    }

    @Required
    public void setCoreObjectTypeProvider(CoreObjectTypeProvider coreObjectTypeProvider) {
        this.coreObjectTypeProvider = coreObjectTypeProvider;
    }
}
