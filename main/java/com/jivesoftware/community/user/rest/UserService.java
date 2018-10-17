/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.user.rest;

import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.browse.rest.impl.ItemsViewBean;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Collection;

@Produces("application/json")
@Path("/users")
public interface UserService {

    @GET
    @Path("/{userID}")
    public UserItemBean getUser(@PathParam("userID") long userID,
            @QueryParam("objectID") @DefaultValue("-1") long objectID,
            @QueryParam("objectType") @DefaultValue("-1") int objectType,
            @QueryParam("entitlement") @DefaultValue("VIEW") EntitlementTypeProvider.EntitlementType entitlement);


    @GET
    @Path("/search/{query}")
    public UserPickerResult searchUsers(@PathParam("query") String query,
            @QueryParam("objectID") @DefaultValue("-1") long objectID,
            @QueryParam("objectType") @DefaultValue("-1") int objectType,
            @QueryParam("entitlement") @DefaultValue("VIEW") EntitlementTypeProvider.EntitlementType entitlement,
            @QueryParam("emailAllowed") @DefaultValue("true") boolean emailAllowed,
            @QueryParam("userAllowed") @DefaultValue("true") boolean userAllowed,
            @QueryParam("listAllowed") @DefaultValue("true") boolean listAllowed,
            @QueryParam("canInvitePartners") @DefaultValue("false") boolean canInvitePartners,
            @QueryParam("canInviteJustPartners") @DefaultValue("false") boolean canInviteJustPartners,
            @QueryParam("canInvitePreprovisioned") @DefaultValue("false") boolean canInvitePreprovisioned,
            @QueryParam("invitePreprovisionedDomainRestricted") @DefaultValue("false") boolean invitePreprovisionedDomainRestricted,
            @QueryParam("filterID") Collection<String> filterID,
            @QueryParam("propNames") Collection<String> propNames,
            @QueryParam("numResults") @DefaultValue("-1") int numResults,
            @QueryParam("srole") @DefaultValue("0") int srole);

    @GET
    @Path("/{userID}/browse")
    public ItemsViewBean<UserItemBean> getUsers(@PathParam("userID") String userID,
            @QueryParam("containerType") @DefaultValue("0") int containerType,
            @QueryParam("containerID") @DefaultValue("0") long containerID,
            @QueryParam("filterGroupID") @DefaultValue("people") String filterGroupID,
            @QueryParam("filterID") Collection<String> filterID,
            @QueryParam("sortKey") @DefaultValue("") String sortKey,
            @QueryParam("sortOrder") @DefaultValue("-1") int sortOrder,
            @QueryParam("start") @DefaultValue("0") int start,
            @QueryParam("numResults") @DefaultValue("20") int numResults,
            @QueryParam("query") @DefaultValue("") String query,
            @QueryParam("activityTime") @DefaultValue("0") long activityTime,
            @QueryParam("token") @DefaultValue("") String token,
            @QueryParam("propertyNames") Collection<String> propertyNames);

    @GET
    @Path("/{userID}/orgchart")
    public OrgChartItemsViewBean getOrgChart(@PathParam("userID") String userID);

    @GET
    @Path("/{userID}/directreports")
    public ItemsViewBean<UserItemBean> getDirectReports(@PathParam("userID") long userID);

    @GET
    @Path("/{userID}/directreports/count")
    public int getDirectReportCount(@PathParam("userID") long userID);

    @GET
    @Path("/{userID}/related/common")
    public ItemsViewBean<UserItemBean> getCommonRelatedUsers(@PathParam("userID") long userID);

    @GET
    @Path("/{userID}/related/common/count")
    public int getCommonRelatedUsersCount(@PathParam("userID") long userID);

    @GET
    @Path("/{userID}/prop/{propName}")
    public String getUserProperty(@PathParam("userID") String userID, @PathParam("propName") String propName);

    @POST
    @Path("/{userID}/prop/{propName}")
    public void setUserProperty(@PathParam("userID") String userID, @PathParam("propName") String propName, String propValue);

    @DELETE
    @Path("/{userID}/prop/{propName}")
    public void removeUserProperty(@PathParam("userID") String userID, @PathParam("propName") String propName);

    @GET
    @Path("/{userID}/login/processing")
    public boolean isLoginProcessing(@PathParam("userID") long userID);

}
