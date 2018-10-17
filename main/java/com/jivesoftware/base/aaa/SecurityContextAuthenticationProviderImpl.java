/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.base.aaa;

import com.jivesoftware.community.aaa.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.Permissions;
import com.jivesoftware.community.aaa.authz.Entitlement;
import com.jivesoftware.community.aaa.authz.EntitlementProvider;
import com.jivesoftware.community.entitlements.AdminEntitlementContainer;
import com.jivesoftware.community.internal.ExtendedPermissionsManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

/**
 * Spring Security implementation of the AuthenticationProvider API based on the system configured SecurityContextHolderStrategy.
 *
 * @see
 */
public class SecurityContextAuthenticationProviderImpl implements AuthenticationProvider {

    private static final Logger log = LogManager.getLogger(SecurityContextAuthenticationProviderImpl.class);

    private ExtendedPermissionsManager permissionsManager;
    private EntitlementProvider entitlementProvider;

    public JiveAuthentication getAuthentication() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (null == auth) {
            log.debug("No authentication associated with current context. Defaulting to anonymous");
            return new AnonymousAuthentication();
        }
        if(auth instanceof UsernamePasswordAuthenticationToken) {
            User user = ((JiveUserDetails)auth.getPrincipal()).getUser();
            return new JiveUserAuthentication(user);
        }
        Assert.isAssignable(JiveAuthentication.class, auth.getClass(), "The Spring Security context authentication " +
                "object should be an instance of JiveAuthentication at this point.");

        //this should universally be true if the interceptor stack is configured properly
        return (JiveAuthentication) auth;
    }

    public JiveSecurityContext getSecurityContext() {

        Assert.isAssignable(JiveSecurityContext.class, SecurityContextHolder.getContext().getClass(),
                "Security context must be an instance of JiveSecurityContext at this point if you wish to use the" +
                        "authentication provider to access it");

        return (JiveSecurityContext) SecurityContextHolder.getContext();
    }

    public void setPermissionsManager(ExtendedPermissionsManager pm) {
        this.permissionsManager = pm;
    }

    public void setEntitlementProvider(EntitlementProvider ep) {
        this.entitlementProvider = ep;
    }

    public boolean isSystemAdmin() {
        //system authentication is equivalent to the admin
        //noinspection SimplifiableIfStatement
        if (this.getAuthentication() instanceof SystemAuthentication) {
            return true;
        }

        JiveContainer system = AdminEntitlementContainer.getInstance();
        User user = getAuthentication().getUser();
        return entitlementProvider.isUserEntitled(user, system, Entitlement.ADMINISTER);
    }

    /**
     * Returns a legacy AuthToken based on the user from the authentication provider. Generally this should be avoided
     * for new functionality which should instead operate on the Authentication and its GrantedAuthority array.
     * <p/>
     * Direct use of the AuthToken is discuraged as of 2.0;
     *
     * @return the Jive AuthToken for the current user.
     */
    @Deprecated
    public AuthToken getAuthToken() {

        final JiveAuthentication authentication = getAuthentication();

        //in most contexts, we assume that the authentication exists
        //this code could possibly be called in a broader context
        //so we check and fall back gracefully to the anonymous token
        if (null == authentication || !(authentication.isAuthenticated())) {
            return new AnonymousUser();
        }

        return authentication.getAuthenticationToken();
    }

    /**
     * Returns the permissions for the current user given the designated object type and object id.
     *
     * @param objectType the objectType for the object to get permissions for
     * @param objectId the objectID for the object to get permissions for
     * @return the permissions for the current user given the designated object type and object id.
     */
    public Permissions getPermissions(final int objectType, final long objectId) {
        JiveAuthentication auth = getAuthentication();
        return permissionsManager.getPermissions(auth, objectType, objectId);
    }

    /**
     * Returns permissions for a given user on a given object type and id. This is really a convenience to the code
     * below which is common across several usages.
     *
     * @param userId
     * @param objectType
     * @param objectId
     * @return
     */
    public Permissions getPermissions(final long userId, final int objectType, final long objectId) {
        return permissionsManager.getPermissions(userId, objectType, objectId);
    }

    public Permissions getCommunityPermissions(long communityId) {
        JiveAuthentication auth = getAuthentication();
        return permissionsManager.getCommunityPermissions(auth, communityId);
    }

    public Permissions getCommunityPermissions(long userId, long communityId) {
        return permissionsManager.getCommunityPermissions(userId, communityId);
    }

    /**
     * Returns the JiveUser associated with this request.
     *
     * @return the JiveUser associated with this request.
     */
    public User getJiveUser() {
        User user = getAuthentication().getUser();
        return user == null ? new AnonymousUser() : user;
    }

    public long getJiveUserID() {
        return getAuthentication().getUserID();
    }
}
