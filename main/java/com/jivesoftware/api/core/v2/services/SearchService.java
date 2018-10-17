/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.api.core.v2.services;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.jivesoftware.api.core.OpenClientError;
import com.jivesoftware.api.core.OpenClientErrorBuilder;
import com.jivesoftware.api.core.v2.OpenClientContainerType;
import com.jivesoftware.api.core.v2.OpenClientContentType;
import com.jivesoftware.api.core.v2.OpenClientObjectType;
import com.jivesoftware.api.core.v2.entities.EntityCollection;
import com.jivesoftware.api.core.v2.entities.containers.ContainerEntity;
import com.jivesoftware.api.core.v2.entities.content.summary.ContentEntitySummary;
import com.jivesoftware.api.core.v2.entities.users.UserEntity;
import com.jivesoftware.api.core.v2.entities.users.UserEntitySummary;
import com.jivesoftware.api.core.v2.providers.SearchProvider;
import com.jivesoftware.api.core.v2.providers.users.UserProvider;
import com.jivesoftware.api.core.v2.types.OpenClientTypeFunctions;
import com.jivesoftware.api.core.v2.util.DateRange;
import com.jivesoftware.api.core.v2.util.Paginator;
import com.jivesoftware.api.core.v2.util.TypeFilterUtil;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.JiveObjectLoader;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.search.action.SearchActionHelper;
import com.jivesoftware.community.webservices.rest.ErrorBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * OpenClient endpoint for searching for content
 *
 * @since Jive SBS Steelhead
 */
@Produces("application/json")
@Path("/search")
public class SearchService extends BaseService {

    private SearchProvider searchProvider;
    private JiveObjectLoader jiveObjectLoader;
    private UserProvider userProvider;
    private SearchActionHelper searchHelper;
    private Function<String, Date> stringDateTimeConverter;
    private Function<Date, String> dateTimeStringConverter;
    private TypeFilterUtil typeFilterUtil;

    @Required
    public final void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Required
    public final void setDateTimeConverter(Function<String, Date> dateTimeConverter) {
        this.stringDateTimeConverter = dateTimeConverter;
    }

    @Required
    public final void setDateTimeStringConverter(Function<Date, String> dateTimeStringConverter) {
        this.dateTimeStringConverter = dateTimeStringConverter;
    }

    @Required
    public final void setTypeFilterUtil(TypeFilterUtil typeFilterUtil) {
        this.typeFilterUtil = typeFilterUtil;
    }

    @Required
    public void setJiveObjectLoader(JiveObjectLoader jiveObjectLoader) {
        this.jiveObjectLoader = jiveObjectLoader;
    }

    @Required
    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Required
    public void setSearchHelper(SearchActionHelper searchHelper) {
        this.searchHelper = searchHelper;
    }

    /**
     * Searches for content
     *
     * @example
     * JSON Response Payload
{
  "data" : [ {
    "type" : "post",
    "subject" : "lion nPsi7QTfmGik0epyWR9lRVWnrTcawd",
    "author" : {
      "name" : "Administrator",
      "username" : "admin",
      "links" : {
        "alt" : "http://localhost:50001/oc/v1/users/1",
        "avatar" : "http://localhost:50001/oc/v1/avatars/default"
      }
    },
    "replyCount" : 0,
    "likeCount" : 0,
    "contentSummary" : "6VhaGwWbXKZsq2wByCrWG4f0GS2FMw",
    "links" : {
      "alt" : "http://localhost:50001/oc/v1/posts/1021"
    }
  }, {
    "type" : "post",
    "subject" : "lion pNFkVbSlmAJecI5CI48TtMAtVAXd3Y",
    "author" : {
      "name" : "Administrator",
      "username" : "admin",
      "links" : {
        "alt" : "http://localhost:50001/oc/v1/users/1",
        "avatar" : "http://localhost:50001/oc/v1/avatars/default"
      }
    },
    "replyCount" : 0,
    "likeCount" : 0,
    "contentSummary" : "lxY0ynTG5IOAs3Nk8OCt6Uc138TsXQ",
    "links" : {
      "alt" : "http://localhost:50001/oc/v1/posts/1033"
    }
  }, {
    "type" : "post",
    "subject" : "lion Utrsw5zX5HxdlXgDsv3iGI0WYPgV9q",
    "author" : {
      "name" : "Administrator",
      "username" : "admin",
      "links" : {
        "alt" : "http://localhost:50001/oc/v1/users/1",
        "avatar" : "http://localhost:50001/oc/v1/avatars/default"
      }
    },
    "replyCount" : 0,
    "likeCount" : 0,
    "contentSummary" : "n084QxUOwtfR4CgHHXIgcGfB05Ehwa",
    "links" : {
      "alt" : "http://localhost:50001/oc/v1/posts/1005"
    }
  }, {
    "type" : "post",
    "subject" : "lion X32T1qP9BOCXpswKkgtxJxiuwtAJO3",
    "author" : {
      "name" : "Administrator",
      "username" : "admin",
      "links" : {
        "alt" : "http://localhost:50001/oc/v1/users/1",
        "avatar" : "http://localhost:50001/oc/v1/avatars/default"
      }
    },
    "replyCount" : 0,
    "likeCount" : 0,
    "contentSummary" : "4pdXSMeFMJqC08heYH58xUvBV1DO7F",
    "links" : {
      "alt" : "http://localhost:50001/oc/v1/posts/1029"
    }
  }, {
    "type" : "discussion",
    "subject" : "lion f3oPJ5PGzFrZvVyQ67TCiRk6GLkIXj",
    "author" : {
      "name" : "user-zE3416YXEi8RG405",
      "username" : "user-ze3416yxei8rg405",
      "links" : {
        "alt" : "http://localhost:50001/oc/v1/users/2000",
        "avatar" : "http://localhost:50001/oc/v1/avatars/default"
      }
    },
    "replyCount" : 0,
    "likeCount" : 0,
    "contentSummary" : "Psauob7SsH5B0EF6INHXCC02GFcFuG",
    "links" : {
      "alt" : "http://localhost:50001/oc/v1/discussions/1000"
    }
  }, {
    "type" : "discussion",
    "subject" : "lion HFc1rR4ZpV1YUhpe3Tobbj7dL1Mw34",
    "author" : {
      "name" : "user-Y5HrCk7462tk94IH",
      "username" : "user-y5hrck7462tk94ih",
      "links" : {
        "alt" : "http://localhost:50001/oc/v1/users/2016",
        "avatar" : "http://localhost:50001/oc/v1/avatars/default"
      }
    },
    "replyCount" : 0,
    "likeCount" : 0,
    "contentSummary" : "xPArVspBs1nX0OzzUuBHANXG6sFazT",
    "links" : {
      "alt" : "http://localhost:50001/oc/v1/discussions/1004"
    }
  } ],
  "links" : {
    "next" : "http://localhost:50001/oc/v1/search/?type=post&amp;type=discussion&amp;q=lion&amp;limit=6&amp;offset=12",
    "previous" : "http://localhost:50001/oc/v1/search/?type=post&amp;type=discussion&amp;q=lion&amp;limit=6"
  }
}
     * @param q A text string that will be searched for within Jive.
     * @param contentTypeNames set of content types to include in the search. When not specified all content types available
     * to open client will be searched.
     * @param limit the maximum number of results to return. If there are
     * fewer results available, then fewer results than the limit will be returned.
     * @param offset the number of results which should be skipped in the returned array. For instance,
     * if the first 25 results have already been retrieved then results after the 25th result can be retrieved by
     * specifying an offset of 25. The minimum value for the offset is 0, specifying anything less than 0 for the offset
     * will result in an exception.
     * @param fromDateStr older items in terms of modification time (day granularity) will be excluded from the search
     * results.
     * @param toDateStr newer items in terms of modification time (day granularity) will be excluded from the search
     * results.
     * @param containerName name of the container to search to restrict the search to.  If this is specified, then neither
      * containerID nor containerTypeID can be specified
     * @param containerID ID of the container to restrict the search to.  If this is specified, then containerTypeID must also be
     * specified, and containerName must not
     * @param containerTypeID ID of the container type to restrict the search to.  If this is specified, then containerID must also
     * be specified, and containerName must not
     * @param username Username of the content author to restrict the search to.  If this is specified, then userID cannot be specified.
     * @param userID User ID of the content author to restrict the search to.  If this is specified, then username cannot be specified.
     * @param sort the field to sort on ("relevance", "likes", "subject", "modificationDate"). The default if sort is
     * not provided is "relevance"
     * @param sortOrder the desired sort order ("descending", "ascending"). The default order if none is provided is
     * "descending". Please note that ascending and descending do not necessary produce exact opposites of one another.
     * This is because there are often additional criteria that is used to sort items that have matching primary
     * criteria.
     * @param origin the origin of this search request. Used mainly for analytics to keep track of who's making the request
     * @return the JSON representation of the search results. See {@link ContentEntitySummary}
     * @see com.jivesoftware.api.core.OpenClientError#missing_query
     * @see com.jivesoftware.api.core.OpenClientError#object_type_not_found
     * @see com.jivesoftware.api.core.OpenClientError#invalid_date_format
     * @see com.jivesoftware.api.core.OpenClientError#invalid_date_window
     */
    @GET
    @Path("/content")
    public Response searchContent(
            @QueryParam("q") @DefaultValue("") String q,
            @QueryParam("type") Set<String> contentTypeNames,

            @QueryParam("from") @DefaultValue("") String fromDateStr,
            @QueryParam("to") @DefaultValue("") String toDateStr,

            // ways to identify a container type
            @QueryParam("containerTypeID") Integer containerTypeID,
            @QueryParam("containerID") Long containerID,

            // ways to identify a container for the argument type
            @QueryParam("container") String containerName,

            // ways to identify a user
            @QueryParam("user") String username,
            @QueryParam("userID") Long userID,

            @QueryParam("limit") @DefaultValue("25") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,


            @QueryParam("sort") @DefaultValue("relevance") String sort,
            @QueryParam("sortOrder") @DefaultValue("descending") String sortOrder,

            // way to identify search location origin ie spotlight
            @QueryParam("origin") @DefaultValue("unknown")String origin)
    {
        
    	System.out.println("Inside SearchService WEB SERVICE ==");
    	Set<JiveContainer> container =
            resolveContainers(containerTypeID, containerID, containerName);

        userID = validateUserID( userID, username );

        DateRange dateRange = validateDateRange(fromDateStr, toDateStr);
        contentTypeNames = prepareStringSetParameter(contentTypeNames);
        Set<OpenClientContentType> contentTypes = contentTypeNames != null ? typeFilterUtil
                .resolveContentTypes(contentTypeNames) : null;

        Paginator paginator = paginationHelper.getPaginator(offset, limit);
        SearchProvider.SortCriteria sortCriteria = new SearchProvider.SortCriteria(sort, sortOrder);

        Collection<ContentEntitySummary> results =
                searchProvider.searchContent(q, container, contentTypes, userID, paginator, dateRange, sortCriteria, origin);

        results = searchProvider.addContainerInformation(results);

        EntityCollection<ContentEntitySummary> contents = attachPagination(paginator, results,
                additionalParameters(q, contentTypes, containerTypeID, containerID, userID, dateRange, sortCriteria));
        return Response.ok(contents).build();
    }


    private Long validateUserID( Long userID, String username )
    {
        if ( userID == null && username == null )
        {
            return null;
        }
        if ( userID != null && username != null )
        {
            throw ErrorBuilder.badRequest(ErrorBuilder.ERROR_CODE_INVALID_PARAM,
                "Can specify user or userID, but not both");
        }
        if ( username != null )
        {
            UserEntity user = userProvider.getUserByUsername(username);
            userID = user.getId();
        }
        return userID;
    }


    private Set<JiveContainer> resolveContainers(
            Integer containerTypeID, Long containerID,
            String containerName) {
        if (containerTypeID == null && containerID == null && containerName == null)
        {
            return null;
        }
        if ( (containerTypeID != null || containerID != null) && containerName != null )
        {
            throw ErrorBuilder.badRequest(ErrorBuilder.ERROR_CODE_INVALID_PARAM,
                "Can specify container, or containerID and containerTypeID, but not both");
        }
        if ( containerName != null )
        {
            return resolveContainersByName(containerName);
        }else if ( containerTypeID != null && containerID != null) {
            return resolveContainersByID(containerTypeID, containerID);
        }else if ( containerTypeID != null ) {
            throw ErrorBuilder.missingParam("containerID");
        }else{
            throw ErrorBuilder.missingParam("containerTypeID");
        }
    }

    private Set<JiveContainer> resolveContainersByID(Integer containerTypeID, Long containerID) {
        try {
            JiveContainer container = jiveObjectLoader.getJiveContainer(containerTypeID, containerID);
            if ( container == null )
            {
                throw containerNotFound(containerTypeID, containerID);
            }
            Set<JiveContainer> containers = new HashSet<JiveContainer>(5);
            containers.add(container);
            return containers;
        }
        catch(UnauthorizedException e) {
            throw ErrorBuilder.forbidden(
                    String.format("not authorized for the container %s %s", containerTypeID, containerID));
        }
        catch (NotFoundException e) {
            throw containerNotFound(containerTypeID, containerID);
        }
    }


    private static WebApplicationException containerNotFound(Integer containerTypeID, Long containerID) {
        return ErrorBuilder.notFound(
                ErrorBuilder.ERROR_CODE_CONTAINER_NOT_FOUND,
                String.format("Container not found: containerTypeID=%s containerID=%s", containerTypeID, containerID));
    }


    private Set<JiveContainer> resolveContainersByName(String containerName) {
        Set<JiveContainer> containers = new HashSet<JiveContainer>(5);
        Iterator<JiveObject> containerResults = searchHelper.searchForContainersByName(containerName).results().iterator();
        if ( ! containerResults.hasNext() )
        {
            throw ErrorBuilder.notFound(
                    ErrorBuilder.ERROR_CODE_CONTAINER_NOT_FOUND,
                    String.format("Container not found: %s", containerName));
        } else {
            while( containerResults.hasNext() )
            {
                containers.add( (JiveContainer) containerResults.next() );
            }
        }
        return containers;
    }


    /**
     * Searches for places
     *
     * @example
     * JSON Response Payload
TBD
     * @param q the query string
     * @param containerTypeNames set of container types to include in the search. When not specified all container types available
     * to open client will be searched.
     * @param limit the maximum number of results to return. If there are
     * fewer results available, then fewer results than the limit will be returned.
     * @param offset the number of results which should be skipped in the returned array. For instance,
     * if the first 25 results have already been retrieved then results after the 25th result can be retrieved by
     * specifying an offset of 25. The minimum value for the offset is 0, specifying anything less than 0 for the offset
     * will result in an exception.
     * @param fromDateStr older items in terms of modification time (day granularity) will be excluded from the search
     * results.
     * @param toDateStr newer items in terms of modification time (day granularity) will be excluded from the search
     * results.
     * @param sort the field to sort on ("relevance", "likes", "subject", "modificationDate"). The default if sort is
     * not provided is "relevance"
     * @param sortOrder the desired sort order ("descending", "ascending"). The default order if none is provided is
     * "descending".
     * @param origin the origin of this search request. Used mainly for analytics to keep track of who's making the request
     * @return the JSON representation of the search results. See {@link ContainerEntity}
     * @see com.jivesoftware.api.core.OpenClientError#missing_query
     * @see com.jivesoftware.api.core.OpenClientError#object_type_not_found
     * @see com.jivesoftware.api.core.OpenClientError#invalid_date_format
     * @see com.jivesoftware.api.core.OpenClientError#invalid_date_window
     */
    @GET
    @Path("/places")
    public Response searchPlaces(
            @QueryParam("q") @DefaultValue("") String q,
            @QueryParam("type") Set<String> containerTypeNames,
            @QueryParam("limit") @DefaultValue("25") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("from") @DefaultValue("") String fromDateStr,
            @QueryParam("to") @DefaultValue("") String toDateStr,
            @QueryParam("sort") @DefaultValue("relevance") String sort,
            @QueryParam("sortOrder") @DefaultValue("descending") String sortOrder,
            @QueryParam("origin") @DefaultValue("unknown")String origin)
    {
        DateRange dateRange = validateDateRange(fromDateStr, toDateStr);
        Set<OpenClientContainerType> containerTypes = typeFilterUtil.resolveContainerTypes(
                prepareStringSetParameter(containerTypeNames));
        SearchProvider.SortCriteria sortCriteria = new SearchProvider.SortCriteria(sort, sortOrder);

        Paginator paginator = paginationHelper.getPaginator(offset, limit);
        EntityCollection<ContainerEntity> results = attachPagination(paginator, searchProvider.searchPlaces(q, containerTypes, paginator, dateRange, origin),
                additionalParameters(q, containerTypes, null, null, null, dateRange, sortCriteria));
        return Response.ok(results).build();
    }

    @GET
    @Path("/people")
    public Response searchPeople(
            @QueryParam("q") String q,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("25") int limit,
            @QueryParam("origin") @DefaultValue("unknown")String origin)
    {
        Paginator paginator = paginationHelper.getPaginator(offset, limit);
        ImmutableMultimap.Builder<String, String> parameterBuilder = ImmutableMultimap.builder();
        Collection<UserEntitySummary> users;
        if (q == null) {
            users = userProvider.getUsers(paginator, origin);
        }
        else {
            parameterBuilder.put("q", q);
            users = userProvider.searchUsers(q, paginator, origin);
        }
        EntityCollection<UserEntitySummary> results = attachPagination(paginator, users, parameterBuilder.build());
        return Response.ok(results).build();
    }

    private DateRange validateDateRange(String fromDateStr, String toDateStr) {
        Date fromDate = StringUtils.isNotBlank(fromDateStr) ? stringDateTimeConverter.apply(fromDateStr) : null;
        Date toDate = StringUtils.isNotBlank(toDateStr) ? stringDateTimeConverter.apply(toDateStr) : null;

        if (fromDate == null && toDate == null) {
            return null;
        }
        else if (fromDate != null && toDate != null && fromDate.getTime() > toDate.getTime()) {
            String message = String.format("Provided lower bound date, %s, is greater than the upper bound, %s.",
                    fromDateStr, toDateStr);
            throw OpenClientErrorBuilder.badRequest(OpenClientError.invalid_date_window.getCode(), message);
        }
        return new DateRange(fromDate, toDate);
    }

    protected <C extends OpenClientObjectType> Multimap<String, String> additionalParameters(
            String query, Set<C> types,
            Integer containerType,
            Long containerID,
            Long authorUserID,
            DateRange dateRange, SearchProvider.SortCriteria sortCriteria) {

        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        builder.put("q", query);

        if (types != null) {
            builder.putAll("type", Iterables.transform(types, OpenClientTypeFunctions.objectTypeName()));
        }

        if ( authorUserID != null )
        {
            builder.put("authorId", authorUserID.toString());
        }

        if ( containerType != null && containerID != null )
        {
            builder.put("containerTypeID", containerType.toString() );
            builder.put("containerID", containerID.toString() );
        }

        if (dateRange != null) {
            if (dateRange.getLowerBound() != null) {
                builder.put("from", dateTimeStringConverter.apply(dateRange.getLowerBound()));
            }
            if (dateRange.getUpperBound() != null) {
                builder.put("to", dateTimeStringConverter.apply(dateRange.getUpperBound()));
            }
        }

        if (sortCriteria != null) {
            builder.put("sort", sortCriteria.getSort().getKey());
            builder.put("sortOrder", "" + sortCriteria.getSortOrder().getKey());
        }

        return builder.build();
    }
}
