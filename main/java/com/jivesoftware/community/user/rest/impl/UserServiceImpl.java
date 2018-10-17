/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.user.rest.impl;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal.UserRole;

import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.LongRunningLoginProcess;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserAlreadyExistsException;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.RegistrationManager;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.browse.BrowseFilterContext;
import com.jivesoftware.community.browse.BrowseFilterManager;
import com.jivesoftware.community.browse.BrowseIterator;
import com.jivesoftware.community.browse.BrowseManager;
import com.jivesoftware.community.browse.BrowsePerspective;
import com.jivesoftware.community.browse.filter.ArchetypeFilter;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.filter.ExcludeDisabledFilter;
import com.jivesoftware.community.browse.filter.ExcludePartnerFilter;
import com.jivesoftware.community.browse.filter.JoinDateFilter;
import com.jivesoftware.community.browse.filter.OrgChartFilter;
import com.jivesoftware.community.browse.filter.PerspectiveFilter;
import com.jivesoftware.community.browse.filter.PostProcessingFilter;
import com.jivesoftware.community.browse.filter.SearchBrowseFilter;
import com.jivesoftware.community.browse.filter.ShowOnlineUserFilter;
import com.jivesoftware.community.browse.filter.TagFilter;
import com.jivesoftware.community.browse.filter.group.BrowseFilterGroup;
import com.jivesoftware.community.browse.impl.BrowseResultPostProcessingIterator;
import com.jivesoftware.community.browse.impl.BrowseTokenUtil;
import com.jivesoftware.community.browse.rest.FilterBean;
import com.jivesoftware.community.browse.rest.impl.FilterUpdatingItemsViewBean;
import com.jivesoftware.community.browse.rest.impl.ItemsViewBean;
import com.jivesoftware.community.browse.rest.impl.JiveObjectIsVisibleToPartnerProviderImpl;
import com.jivesoftware.community.browse.rest.impl.SearchResultsInfo;
import com.jivesoftware.community.browse.sort.BrowseSort;
import com.jivesoftware.community.browse.sort.CreationDateSort;
import com.jivesoftware.community.browse.sort.PlaceDateJoinedSort;
import com.jivesoftware.community.browse.sort.ProfileFieldSort;
import com.jivesoftware.community.browse.sort.RecentActivitySort;
import com.jivesoftware.community.browse.sort.UserStatusLevelSort;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.invitation.InvitationConfiguration;
import com.jivesoftware.community.invitation.rest.InvitedUserProvider;
import com.jivesoftware.community.mail.util.EmailValidationHelper;
import com.jivesoftware.community.search.ResultCountComponent;
import com.jivesoftware.community.search.user.ProfileSearchCriteria;
import com.jivesoftware.community.search.user.ProfileSearchCriteriaBuilder;
import com.jivesoftware.community.search.user.ProfileSearchQueryManager;
import com.jivesoftware.community.search.user.ProfileSearchQuerySettingsManager;
import com.jivesoftware.community.search.user.ProfileSearchResult;
import com.jivesoftware.community.search.user.ProfileSearchSettingsManager;
import com.jivesoftware.community.search.user.ResultProfileFieldFacetComponent;
import com.jivesoftware.community.solution.annotations.InjectConfiguration;
import com.jivesoftware.community.user.browse.DateJoinedSort;
import com.jivesoftware.community.user.browse.ProfileFacetFilter;
import com.jivesoftware.community.user.browse.UserTypeFacetFilter;
import com.jivesoftware.community.user.profile.ProfileSearchFilter;
import com.jivesoftware.community.user.relationships.HierarchicalUserRelationshipGraph;
import com.jivesoftware.community.user.relationships.MeshUserRelationshipGraph;
import com.jivesoftware.community.user.relationships.UserRelationship;
import com.jivesoftware.community.user.relationships.UserRelationshipList;
import com.jivesoftware.community.user.relationships.UserRelationshipManager;
import com.jivesoftware.community.user.rest.HistoryService;
import com.jivesoftware.community.user.rest.OrgChartItemsViewBean;
import com.jivesoftware.community.user.rest.UserItemBean;
import com.jivesoftware.community.user.rest.UserList;
import com.jivesoftware.community.user.rest.UserPickerResult;
import com.jivesoftware.community.user.rest.UserService;
import com.jivesoftware.community.webservices.rest.ErrorBuilder;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

public class UserServiceImpl extends RemoteSupport implements UserService {

    private static Logger log = LogManager.getLogger(UserServiceImpl.class);

    public static final HashSet<String> DEFAULT_ORG_CHART_USER_PROPS = Sets.newHashSet(
            UserItemBeanDirectReportCountProviderImpl.PROPERTY_NAME,
            UserItemBeanDefaultProfileProviderImpl.PROPERTY_NAME, UserItemBeanAvatarIDProviderImpl.PROPERTY_NAME,
            UserItemBeanProfileTitleProviderImpl.PROPERTY_NAME, UserItemBeanProfileImageProviderImpl.PROPERTY_NAME,
            UserItemBeanOrgchartModificationLinkProviderImpl.PROPERTY_NAME);

    public static final HashSet<String> DEFAULT_ORG_CHART_NODE_PROPS = Sets.newHashSet(
            UserItemBeanAvatarIDProviderImpl.PROPERTY_NAME, UserItemBeanProfileTitleProviderImpl.PROPERTY_NAME,
            UserItemBeanDirectReportCountProviderImpl.PROPERTY_NAME);

    public static final Collection<String> DEFAULT_USER_BROWSE_VIEW_PROPERTIES = UserItemBean.DEFAULT_USER_BROWSE_VIEW_PROPERTIES;

    private ProfileSearchQueryManager profileSearchQueryManager;
    private ProfileSearchQuerySettingsManager profileSearchQuerySettingsManager;
    private ProfileSearchSettingsManager profileSearchSettingsManager;
    private UserRelationshipManager userRelationshipManager;
    private EntitlementTypeProvider entitlementTypeProvider;
    private BrowseManager browseManager;
    private BrowseFilterManager browseFilterManager;
    private BrowseTokenUtil browseTokenUtil;
    private HistoryService historyService;
    private EmailValidationHelper emailValidationHelper;
    private InvitedUserProvider invitedUserProvider;
    private RegistrationManager registrationManager;
    private JiveObjectIsVisibleToPartnerProviderImpl jiveObjectIsVisibleToPartnerProvider;
    private GroupManager groupManager;
    private InvitationConfiguration invitationConfiguration;

    private Collection<String> userItemBeanProps;
    private Collection<String> orgChartUserProps;
    private Collection<String> orgChartNodeProps;

    private Map<String, PostProcessingFilter<User>> searchUserFilters;

    public void setJiveObjectIsVisibleToPartnerProvider(
            JiveObjectIsVisibleToPartnerProviderImpl jiveObjectIsVisibleToPartnerProvider)
    {
        this.jiveObjectIsVisibleToPartnerProvider = jiveObjectIsVisibleToPartnerProvider;
    }

    public void setUserItemBeanProps(Collection<String> userItemBeanProps) {
        this.userItemBeanProps = userItemBeanProps;
    }

    public void setOrgChartUserProps(Collection<String> orgChartUserProps) {
        this.orgChartUserProps = orgChartUserProps;
    }

    public void setOrgChartNodeProps(Collection<String> orgChartNodeProps) {
        this.orgChartNodeProps = orgChartNodeProps;
    }

    public void setBrowseManager(BrowseManager browseManager) {
        this.browseManager = browseManager;
    }

    public void setBrowseTokenUtil(BrowseTokenUtil browseTokenUtil) {
        this.browseTokenUtil = browseTokenUtil;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public void setInvitedUserProvider(InvitedUserProvider invitedUserProvider) {
        this.invitedUserProvider = invitedUserProvider;
    }

    public void setRegistrationManager(RegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public void setSearchUserFilters(Collection<PostProcessingFilter<User>> userpickerFilters) {
        if (userpickerFilters != null) {
            this.searchUserFilters = Maps.newHashMap();
            for (PostProcessingFilter<User> userpickerFilter : userpickerFilters) {
                this.searchUserFilters.put(userpickerFilter.getId(), userpickerFilter);
            }
        }
    }

    private static final BrowseSort SORT_DEFAULT = new RecentActivitySort();

    public void init() {
        orgChartUserProps = DEFAULT_ORG_CHART_USER_PROPS;
        orgChartNodeProps = DEFAULT_ORG_CHART_NODE_PROPS;
        userItemBeanProps = DEFAULT_USER_BROWSE_VIEW_PROPERTIES;
    }

    @Override
    public UserItemBean getUser(long userID, long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement) {
        try {
            User user = userManager.getUser(userID);
            UserItemBean retval = userItemBeanBuilder.build(user, null, userItemBeanProps, Maps.<String, Object>newHashMap());
            retval.setEntitled(isEntitled(user, objectID, objectType, entitlement));
            return retval;
        }
        catch (UserNotFoundException unfe) {
            return null;
        }
    }

    @Override
    public UserPickerResult searchUsers(String query, long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement, boolean emailAllowed, boolean userAllowed,
            boolean listAllowed, boolean canInvitePartners, boolean canInviteJustPartners,
            boolean canInvitePreprovisioned, boolean invitePreprovisionedDomainRestricted,
            Collection<String> filterIDs, Collection<String> propNames, int numResults, int srole) {

        //if no prop names specified, add default props
        if (propNames.isEmpty()) {
            propNames.addAll(userItemBeanProps);
        }

        Map<String, Object> userItemBeanContext = Maps.newHashMap();
        userItemBeanContext.put(UserItemBeanDirectMessagePermsProviderImpl.ALLOW_DISABLED, true);
        userItemBeanContext.put(UserItemBeanDirectMessagePermsProviderImpl.ALLOW_SELF, true);

        //search for users
        List<UserItemBean> lou = new LinkedList<>();
        List<UserList> lolu = new LinkedList<>();
        List<TokenSearchResults> tokenSearchResults = new LinkedList<>();
        if (StringUtils.isNotBlank(query)) {
            List<String> tokens = StringUtils.removeDupStringsInListIgnoreCase(Arrays.asList(query.split("(\\s+)?[;,|\\t]+(\\s+)?")));  //split on semi colon, comma, pipe, and tab for all tokens
            tokenSearchResults.addAll(tokens.stream().map(token -> tokenSearchResults(objectID, objectType, entitlement, emailAllowed, userAllowed, listAllowed, canInvitePartners, canInviteJustPartners, canInvitePreprovisioned, invitePreprovisionedDomainRestricted, filterIDs, propNames, numResults, userItemBeanContext, lou, lolu, false, token, srole)).collect(Collectors.toList()));
        }
        Collections.sort(tokenSearchResults, new UserPickerTokenResultComparator());

        Map<String, Set<UserItemBean>> matchedUserTokens = new LinkedHashMap<>();
        for (TokenSearchResults tokenSearchResult : tokenSearchResults) {
            matchedUserTokens.put(tokenSearchResult.getTokenName(), tokenSearchResult.getTokenResultSet());
        }
        // deduplicate Set
        Set<UserItemBean> dedupedUserList = new LinkedHashSet<>();
        for (Map.Entry<String, Set<UserItemBean>> matchedUserToken : matchedUserTokens.entrySet()) {
            Set<UserItemBean> userBeans = matchedUserToken.getValue();
            for (UserItemBean userBean : userBeans) {
                dedupedUserList.add(userBean);
            }
        }
        lou.addAll(dedupedUserList);

        if (emailAllowed || canInvitePreprovisioned) {
            Collections.sort(lou, new UserPickerResultComparator());      //always show actual users over anonymous users
        }

        return new UserPickerResult(lou, lolu, false);
    }

    private TokenSearchResults tokenSearchResults(long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement, boolean emailAllowed, boolean userAllowed,
            boolean listAllowed, boolean canInvitePartners, boolean canInviteJustPartners,
            boolean canInvitePreprovisioned, boolean invitePreprovisionedDomainRestricted, Collection<String> filterIDs,
            Collection<String> propNames, int numResults, Map<String, Object> userItemBeanContext,
            List<UserItemBean> lou, List<UserList> lolu, boolean limitExceeded, String token, int srole)
    {
        Map<String, Set<UserItemBean>> tokenMapResults = new LinkedHashMap<>();
        Integer resultCount = 0;
        if (StringUtils.isNotBlank(token)) {
            Set<Long> userIDsFromEmail = Sets.newLinkedHashSet();
            String[] emailTokens = token.split("[\\s]");    //split on whitespace for email addresses
            if (emailAllowed || canInvitePreprovisioned) {
                for (String emailToken : emailTokens) {
                    if (StringUtils.isValidEmailAddress(emailToken)) {
                        if (!searchForEmailAddress(emailToken, objectID, objectType, entitlement, userAllowed, canInvitePartners, propNames, lou, userItemBeanContext, userIDsFromEmail, filterIDs, numResults)) {
                            limitExceeded = true;
                        }
                    }
                    if (StringUtils.isValidEmailAddressFragment(emailToken)) {
                        if (!suggestDomainRestrictedEmailAddresses(emailToken, objectID, objectType, canInvitePartners, canInviteJustPartners, canInvitePreprovisioned, invitePreprovisionedDomainRestricted, entitlement, lou, numResults)) {
                            limitExceeded = true;
                        }
                    }
                }
            }
            //search for users by username
            if (userAllowed) {
                Set<UserItemBean> results = new LinkedHashSet<>();
                if (!searchForUser(token, objectID, objectType, entitlement, propNames, canInvitePartners, results, userItemBeanContext, userIDsFromEmail, filterIDs, numResults, srole)) {
                    limitExceeded = true;
                }
                tokenMapResults.put(token, results);
                resultCount = results.size();
            }
            //search for lists
            if (listAllowed) {
                searchForUserList(token, objectID, objectType, entitlement, propNames, lolu, userItemBeanContext);
            }
        }

        return new TokenSearchResults(resultCount, limitExceeded, tokenMapResults, token);
    }

    public static class TokenSearchResults {
        int resultCount;
        boolean limitExceeded;
        String token;
        Map<String, Set<UserItemBean>> tokenMapResults;

        public TokenSearchResults(int resultCount, boolean limitExceeded, Map<String, Set<UserItemBean>> tokenMapResults, String token) {
            this.resultCount = resultCount;
            this.limitExceeded = limitExceeded;
            this.tokenMapResults = tokenMapResults;
            this.token = token;
        }

        public int getResultCount() {
            return resultCount;
        }

        public String getTokenName() {
            return token;
        }

        public Set<UserItemBean> getTokenResultSet() {
            if (tokenMapResults.size() >= 1) {
                return tokenMapResults.get(token);
            } else {
                return new LinkedHashSet<>();
            }
        }

    }

    private boolean searchForEmailAddress(String query, long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement, boolean userAllowed, boolean canInvitePartners,
            Collection<String> propNames,
            Collection<UserItemBean> results, Map<String, Object> userItemBeanContext, Set<Long> userIdsFromEmail,
            Collection<String> filterIDs, int numResults)
    {
        Iterator<User> emailIter = null;
        if (userAllowed) {
            ProfileSearchCriteria criteria = new ProfileSearchCriteriaBuilder(getUserID(), query).setRange(1)
                    .setSearchEmail(true).setReturnPartnerUsers(canInvitePartners).setSearchProfile(false)
                    .setReturnDisabledUsers(false).setReturnExternalUsers(false).setUserLocale(Locale.getDefault())
                    .build();
            ProfileSearchResult profileSearchResult = profileSearchQueryManager.executeSearch(criteria);
            emailIter = profileSearchResult.results().iterator();
            emailIter = applyUserSearchFilters(filterIDs, emailIter);
        }
        if (emailIter != null && emailIter.hasNext()) {
            while (emailIter.hasNext()) {
                if (numResults == -1 || results.size() < numResults) {
                    User u = emailIter.next();
                    UserItemBean ub = userItemBeanBuilder.build(u, null, propNames, userItemBeanContext);

                    userIdsFromEmail.add(u.getID());
                    ub.setEntitled(isEntitled(u, objectID, objectType, entitlement));
                    ub.setDisplayExternallyVisibleVisualCue(jiveObjectIsVisibleToPartnerProvider.provideProperty(u,getUser(),null));
                    ub.getProp().put("matchType", "email");
                    ub.getProp().put("matchToken", query);
                    ub.getProp().put("isPartner", Boolean.FALSE);
                    if (u.isEmailVisible()) {
                        //NOTE: we store the user ID for every match, but for cases were the email is hidden,
                        //we actually add the user from addDomainRestrictedEmail.
                        results.add(ub);
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private String calculateDomainBasedOnSuffix(String domain, String suffix) {
        String tempDomain = "";
        if (!domain.startsWith("*")) {
            tempDomain = domain;
        }
        else {
            if (suffix != null) {
                if (!suffix.contains(".")) {
                    tempDomain = suffix+domain.substring(1);
                }
                else {
                    String[] suffixSplit = suffix.split("\\.");
                    if (suffixSplit.length == 1) {
                        tempDomain = suffix+domain.substring(2);
                    }
                    else {
                        if (domain.substring(2).toLowerCase().trim().startsWith(suffix.substring(suffix.indexOf(".")+1).toLowerCase().trim())) {
                            tempDomain = suffix.substring(0,suffix.indexOf(".")+1)+domain.substring(2);
                        }
                    }
                }
            } else {
                tempDomain = domain.substring(2);
            }
        }
        return tempDomain;
    }

    private boolean domainInDomainList(String domainToCheck, Set<String> domains) {
       for (String domain : domains) {
           String tempDomain = calculateDomainBasedOnSuffix(domain, domainToCheck);
           if (tempDomain.trim().equalsIgnoreCase((domainToCheck != null ? domainToCheck.trim() : ""))) {
               return true;
           }
       }
       return false;
    }

    private boolean addDomainRestrictedEmailAddressesSuggestion(String query, long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement, Collection<UserItemBean> results, int numResults,
            boolean canInvitePartners, boolean canInviteJustPartners, boolean canInvitePreprovisioned,
            String prefix, String suffix, Set<String> emailDomains, Set<String> excludedEmailDomains, boolean isPartner) {

        for (String domain : emailDomains) {
            if (numResults == -1 || results.size() < numResults) {
                String tempDomain = calculateDomainBasedOnSuffix(domain, suffix);
                boolean domainStartsWithSuffix = tempDomain.toLowerCase().trim().startsWith((suffix != null ? suffix.toLowerCase().trim() : ""));
                boolean domainInExcludedDomain = domainInDomainList(suffix,excludedEmailDomains);
                if ((suffix == null || domainStartsWithSuffix)) {
                    String address = prefix + "@" + tempDomain;
                    if (StringUtils.isValidEmailAddress(address)) {
                        UserItemBean ub = new UserItemBean(String.valueOf(AnonymousUser.ANONYMOUS_ID), address, address, true,
                                true, address, defaultEntitled(objectID, objectType), true);
                        ub.setEntitled(defaultEntitled(objectID, objectType));
                        ub.getProp().put("matchType", "email");
                        ub.getProp().put("matchToken", query);
                        if (!domainInExcludedDomain) {
                            ub.getProp().put("isPartner", ((canInvitePreprovisioned || (!canInvitePartners && !canInviteJustPartners && !registrationManager.isDomainRestrictionEnabled()))? Boolean.FALSE : Boolean.valueOf(isPartner)));
                        } else {
                            ub.getProp().put("isPartner", Boolean.FALSE);
                        }
                        boolean addEmail = true;
                        for (UserItemBean uib : results) {
                            if (uib.getEmail()!=null && uib.getEmail().equals(address)) {
                                addEmail = false;
                                break;
                            }
                        }
                        if (addEmail) {
                            results.add(ub);
                        }
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean suggestDomainRestrictedEmailAddresses(String query, long objectID, int objectType,
            boolean canInvitePartners, boolean canInviteJustPartners, boolean canInvitePreprovisioned,
            boolean invitePreprovisionedDomainRestricted,
            EntitlementTypeProvider.EntitlementType entitlement, Collection<UserItemBean> results, int numResults) {

        if (query != null) {
            String[] splitQuery = query.split("@");
            String prefix = splitQuery[0];
            String suffix = splitQuery.length > 1 ? splitQuery[1] : null;
            Set<String> approvedEmailDomains = new LinkedHashSet<>();
            if (!canInviteJustPartners && invitationConfiguration.isSystemInvitationsEnabled()) {
                if (emailValidationHelper.isDomainRestrictionEnabled()) {
                    approvedEmailDomains = emailValidationHelper.getExpandedApprovedEmailDomains();
                }
                if (suffix != null && !emailValidationHelper.isDomainRestrictionEnabled()) {
                    approvedEmailDomains.add(suffix);
                }
            }
            else if (canInvitePreprovisioned) {
                if (invitePreprovisionedDomainRestricted) {
                    approvedEmailDomains = emailValidationHelper.getExpandedApprovedEmailDomains();
                } else {
                    if (suffix != null && !registrationManager.isNewAccountCreationEnabled() && canInvitePartners) {
                        approvedEmailDomains.add(suffix);
                    }
                }

            }

            if (addDomainRestrictedEmailAddressesSuggestion(query, objectID, objectType, entitlement, results, numResults, canInvitePartners, canInviteJustPartners, canInvitePreprovisioned, prefix, suffix, approvedEmailDomains , new LinkedHashSet<>(), false)) {
                if (canInvitePartners || (!canInviteJustPartners && canInvitePreprovisioned)) {
                    Set<String> approvedPartnerEmailDomains = new LinkedHashSet<>();
                    if ((!canInvitePreprovisioned && !invitePreprovisionedDomainRestricted) || (canInvitePreprovisioned && invitePreprovisionedDomainRestricted)) {
                        approvedPartnerEmailDomains = emailValidationHelper.getApprovedPartnerEmailDomains();
                    }
                    if (suffix != null && approvedPartnerEmailDomains.size() == 0 && !invitePreprovisionedDomainRestricted) {
                        approvedPartnerEmailDomains.add(suffix);
                    }
                    addDomainRestrictedEmailAddressesSuggestion(query, objectID, objectType, entitlement, results, numResults, canInvitePartners, canInviteJustPartners, canInvitePreprovisioned, prefix, suffix, approvedPartnerEmailDomains, emailValidationHelper.getExpandedApprovedEmailDomains(), true);
                }
            }
            else {
                return false;
            }
        }
        return true;
    }

    private boolean searchForUser(String query, long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement, Collection<String> propNames, boolean canInvitePartners,
            Collection<UserItemBean> results, Map<String, Object> userItemBeanContext, Set<Long> userIdsFromEmail,
            Collection<String> filterIDs, int numResults, int srole)
    {
        
	   	 Map<String, String> properties = new HashMap<String, String>();
		 
		  /**
	     * Fetch User Role
	     */
	    if(srole > 0) {
	   	 if(srole==SynchroConstants.SYNCHRO_PROJECT_OWNER_FIELDID)
	   	 {
	   		 properties.put("role", SynchroConstants.SYNCHRO_PROJECT_OWNER_FIELDNAME);
	   	 }
	   	 else if(srole==SynchroConstants.SYNCHRO_AGENCY_BAT_USER_FIELDID)
	   	 {
	   		 properties.put("role", SynchroConstants.SYNCHRO_AGENCY_BAT_USER_FIELDNAME);
	   	 }
	   	 else if(UserRole.getById(srole) != null)
	        {
	        	properties.put("role", UserRole.getById(srole).name());
	        }
	       
	    }

    /*	ProfileSearchCriteria criteria = new ProfileSearchCriteriaBuilder(getUserID(), query).setRange(10)
                .setSearchNamePhonetically(false).setSearchProfile(false).setReturnDisabledUsers(false)
                .setReturnExternalUsers(false).setReturnPartnerUsers(canInvitePartners).setUserLocale(Locale.getDefault())
                .setSearchUsername(profileSearchQuerySettingsManager.isUserSearchQueryUsername()).build();*/
	    
	    ProfileSearchCriteriaBuilder criteriaBuilder = new ProfileSearchCriteriaBuilder(getUserID(), query).setRange(10)
                .setSearchNamePhonetically(false).setSearchProfile(false).setReturnDisabledUsers(false)
                .setReturnExternalUsers(false).setReturnPartnerUsers(canInvitePartners).setUserLocale(Locale.getDefault())
                .setSearchUsername(profileSearchQuerySettingsManager.isUserSearchQueryUsername());
        
        
       if(properties != null && properties.size() > 0) {
           criteriaBuilder.setProperties(properties);
       }
       
       ProfileSearchCriteria criteria = criteriaBuilder.build();
       
        ProfileSearchResult profileSearchResult = profileSearchQueryManager.executeSearch(criteria);
        Iterator<User> userIter = profileSearchResult.results().iterator();
        userIter = applyUserSearchFilters(filterIDs, userIter);
        while (userIter.hasNext()) {
            if (numResults == -1 || results.size() < numResults) {
                User user = userIter.next();
                if (!userIdsFromEmail.contains(user.getID())) {
                    UserItemBean ub = userItemBeanBuilder.build(user, null, propNames, userItemBeanContext);
                    ub.setEntitled(isEntitled(user, objectID, objectType, entitlement));
                    ub.setDisplayExternallyVisibleVisualCue(jiveObjectIsVisibleToPartnerProvider.provideProperty(user,getUser(),null));
                    ub.getProp().put("matchType", "nameOrUsername");
                    ub.getProp().put("matchToken", query);
                    results.add(ub);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void searchForUserList(String query, long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement, Collection<String> propNames,
            Collection<UserList> results, Map<String, Object> userItemBeanContext)
    {
        LinkedList<UserItemBean> lou;
        Collection<UserRelationshipList> lists = userRelationshipManager.getRelationshipListsByOwner(getUser());

        for (UserRelationshipList list : lists) {
            if (list.getName().toLowerCase().contains(query.toLowerCase())) {
                // the list matches
                lou = new LinkedList<>();
                boolean listToDisplayExternallyVisibleVisualCue = false;
                for (UserRelationship r : list.getApprovedRelationships()) {
                    try {
                        User user = userManager.getUser(r.getRelatedUserID());
                        if (user.isEnabled()) {
                            UserItemBean ub = userItemBeanBuilder.build(user, null, propNames, userItemBeanContext);
                            ub.setEntitled(isEntitled(user, objectID, objectType, entitlement));
                            boolean userToDisplayExternallyVisibleVisualCue = jiveObjectIsVisibleToPartnerProvider.provideProperty(user,getUser(),null);
                            ub.setDisplayExternallyVisibleVisualCue(userToDisplayExternallyVisibleVisualCue);
                            if (!listToDisplayExternallyVisibleVisualCue && userToDisplayExternallyVisibleVisualCue) {
                                listToDisplayExternallyVisibleVisualCue = true;
                            }
                            ub.getProp().put("isPartner",userToDisplayExternallyVisibleVisualCue);
                            lou.add(ub);
                        }
                    }
                    catch (UserNotFoundException nfe) {
                        // noop
                    }
                }
                results.add(new UserList(list.getID(), list.getObjectType(), list.getName(), list.getLabelStyle(), lou, listToDisplayExternallyVisibleVisualCue));
            }
        }
    }

    private Iterator<User> applyUserSearchFilters(Collection<String> filterIDs, Iterator<User> users) {
        if (filterIDs != null && !filterIDs.isEmpty() && searchUserFilters != null) {
            for (String filterID : filterIDs) {
                PostProcessingFilter<User> filter = searchUserFilters.get(filterID);
                if (filter != null) {
                    users = Iterators.filter(users, filter.getPredicate());
                }
            }
        }
        return users;
    }

    private static class UserPickerTokenResultComparator implements Comparator<TokenSearchResults> {
        @Override
        public int compare(TokenSearchResults o1, TokenSearchResults o2) {
            if (o1.getResultCount() >= o2.getResultCount()) {
                return 1;
            }
            return -1;
        }
    }

    private static class UserPickerResultComparator implements Comparator<UserItemBean> {
        @Override
        public int compare(UserItemBean o1, UserItemBean o2) {
            return Boolean.valueOf(o1.isAnonymous()).compareTo(o2.isAnonymous());
        }
    }

    private boolean defaultEntitled(long objectID, int objectType) {
        boolean entitled = true;
        if (objectType != -1 && objectID != -1) {
            entitled = false;
        }
        return entitled;
    }

    private boolean isEntitled(User user, long objectID, int objectType,
            EntitlementTypeProvider.EntitlementType entitlement) {
        JiveObject jo = null;
        boolean entitled = true;
        if (objectType != -1 && objectID != -1) {
            try {
                jo = jiveObjectLoader.getJiveObject(objectType, objectID);
            }
            catch (NotFoundException nfe) {
                entitled = false;
            }
            if (jo != null) {
                //Fix for JIVE-40055: Draft or Under approval documents check if the user should be entitled to view the container not the document.
                if (jo.getObjectType() == JiveConstants.DOCUMENT) {
                    Document document = (Document) jo;
                    if  (document.getDocumentState() == DocumentState.INCOMPLETE || document.isInApproval()) {
                        entitled = entitlementTypeProvider.isUserEntitled(user, document.getJiveContainer(),entitlement);
                    } else {
                        entitled = entitlementTypeProvider.isUserEntitled(user, jo, entitlement);
                    }
                } else {
                    entitled = entitlementTypeProvider.isUserEntitled(user, jo, entitlement);
                }
            }
        }
        return entitled;
    }

    public ItemsViewBean<UserItemBean> getUsers(String userIDStr, int containerType, long containerID,
            String filterGroupID, Collection<String> filterIDs, String sortKey, int sortOrder, int start,
            int numResults, String query, long activityTime, String token, Collection<String> propertyNames) {

        long targetUserID = browseFilterManager.getUserIDFromString(userIDStr);
        User targetUser = getEffectiveUser(targetUserID);

        BrowseFilterContext.Builder context = new BrowseFilterContext.Builder();
        context.setUserID(targetUserID);
        context.setContainerType(containerType);
        context.setContainerID(containerID);
        context.setFilterIDs(filterIDs);
        context.setSortKey(sortKey);
        context.setSortOrder(sortOrder);
        context.setQuery(query);
        BrowseFilterGroup filterGroup = browseFilterManager.getFilterGroup(filterGroupID, context.build());
        Set<BrowseFilter> filters = filterGroup.getAppliedFilters();

        BrowseSort browseSort;
        Iterator<User> results = null;
        boolean maxPageReached = false;

        Set<String> userProps = new HashSet<>();
        if (propertyNames != null && !propertyNames.isEmpty()) {
            userProps.addAll(propertyNames);
        }
        else {
            userProps.addAll(userItemBeanProps);
        }

        PerspectiveFilter allFilter = null;
        OrgChartFilter orgChartFilter = null;
        ArchetypeFilter historyFilter = null;
        for (BrowseFilter filter : filters) {
            if (filter instanceof PerspectiveFilter
                    && ((PerspectiveFilter) filter).getPerspective().equals(BrowsePerspective.ALL)) {
                allFilter = (PerspectiveFilter) filter;
                break;
            }
            else if (filter instanceof OrgChartFilter) {
                orgChartFilter = (OrgChartFilter) filter;
                break;
            } if (filter instanceof ArchetypeFilter) {
                historyFilter = (ArchetypeFilter) filter;
                break;
            }
        }

        Map<String, Object> headerColumns = Maps.newHashMap();
        Map<String, Object> additionalProps = Maps.<String, Object> newHashMap();
        if (orgChartFilter != null) {

            String orgUser = null;
            if (propertyNames != null) {
                for (String prop : propertyNames) {
                    if (prop.contains("orgUser")) {
                        orgUser = prop.split("-")[1];
                    }
                }
            }

            if (orgUser != null) {
                userIDStr = orgUser;
            }
            // org chart has its own view bean
            return getOrgChart(userIDStr);
            //delegate to the history service for history
        } else if (historyFilter != null) {
            return historyService.getRecentHistory(targetUserID, filterGroupID, filterIDs, query, start, numResults, activityTime);
        }
        else {
            if (StringUtils.isNotEmpty(query)) {
                filters.add(new SearchBrowseFilter(query, getLocale(), getTimeZone()));
            }
            browseSort = filterGroup.getAppliedSort();

            // only apply status level logic if we sort by status level
            if (browseSort != null && browseSort instanceof UserStatusLevelSort) {
                userProps.add(UserItemBeanStatusLevelProviderImpl.PROPERTY_NAME);
                headerColumns.put(UserItemBeanStatusLevelProviderImpl.PROPERTY_NAME, true);
            }
            else if (browseSort != null && ((browseSort instanceof DateJoinedSort || browseSort instanceof CreationDateSort))) {
                headerColumns.put(browseSort.getRootKey(), true);
            }
            else if (browseSort != null && browseSort instanceof PlaceDateJoinedSort) {
                userProps.add(UserItemBeanPlaceMembershipProviderImpl.PROPERTY_NAME);
                headerColumns.put(browseSort.getRootKey(), true);
                additionalProps.put(UserItemBeanPlaceMembershipProviderImpl.CONTAINER_TYPE, containerType);
                additionalProps.put(UserItemBeanPlaceMembershipProviderImpl.CONTAINER_ID, containerID);
            }
            else {
                userProps.add(UserItemBeanLatestActivityEntryProviderImpl.PROPERTY_NAME);
                headerColumns.put(UserItemBeanLatestActivityEntryProviderImpl.PROPERTY_NAME, true);
            }

            MeshUserRelationshipGraph friendsGraph = userRelationshipManager.getDefaultMeshRelationshipGraph();
            headerColumns.put("friends", friendsGraph != null && friendsGraph.isReflexive());

            //guard against DOS attacks...
            numResults = browseTokenUtil.boundNumberOfResults(numResults);
            if (browseTokenUtil.isTokenValid(token, start + numResults, browseTokenUtil.getMaxPeopleStartIndex())) {
                if (allFilter != null) {
                    results = getAllFilterResults(start, numResults, query, filters, filterGroup, browseSort);
                }
                else {
                    results = browseManager.getUsers(filters, browseSort, start, numResults + 1);
                }
            }
            else {
                maxPageReached = true;
            }
        }

        Set<UserItemBean> items = Sets.newLinkedHashSet();
        boolean moreResultsPossiblyAvailable = false;
        if (results != null) {
            while (results.hasNext() && items.size() < numResults) {
                User result = results.next();
                items.add(userItemBeanBuilder.build(result, targetUser, userProps, additionalProps));
                // need to guard against the situation where the current result set's fence post user is filtered
                if (items.size() == numResults && !results.hasNext()) {
                    moreResultsPossiblyAvailable = true;
                }
            }
        }

        FilterUpdatingItemsViewBean<UserItemBean> itemsViewBean = new FilterUpdatingItemsViewBean<>(items);
        itemsViewBean.setMaxPageReached(maxPageReached);

        // if necessary get all filters for a given view
        if (allFilter != null) {
            final Set<FilterBean> filterBeans = Sets.newLinkedHashSet();
            BrowseFilterGroup allFilterGroup = browseFilterManager.getFilterGroup(filterGroupID, context.build());
            if (allFilterGroup == null) {
                throw ErrorBuilder.gone();
            }
            Set<BrowseFilter> baseFilters = new LinkedHashSet<>(allFilterGroup.getFilters());
            for (BrowseFilter baseFilter : baseFilters) {
                if (baseFilter instanceof PerspectiveFilter
                        && ((PerspectiveFilter) baseFilter).getPerspective().equals(BrowsePerspective.ALL)) {
                    filterBeans.add(allFilter.toBean());
                }
                else {
                    filterBeans.add(baseFilter.toBean());
                }
            }
            itemsViewBean.setFilters(filterBeans);
        }

        itemsViewBean.setItemGridDetailsHeaderTemplate("jive.browse.user.detailUserHeader");
        itemsViewBean.setItemGridDetailsColumns(headerColumns);
        itemsViewBean.setPageNumber(numResults > 0 ? (start / numResults) + 1 : 1);
        itemsViewBean.setPageSize(numResults);
        if (!maxPageReached) {
            itemsViewBean.setToken(browseTokenUtil.getToken(start + numResults));
        }
        if (results != null) {
            if (results instanceof IteratorWithResultCount) {
                int count = ((IteratorWithResultCount) results).getCount();
                itemsViewBean.setHasNext((count > (start + numResults)) || count == -1);
            } else {
                itemsViewBean.setHasNext(results.hasNext() || moreResultsPossiblyAvailable);
            }
            if (StringUtils.isNotEmpty(query) && results instanceof BrowseIterator && ((BrowseIterator)results).isMoreSearchResultsAvailable() && !itemsViewBean.getHasNext()) {
                itemsViewBean.setSearchResultsInfo(new SearchResultsInfo("people", query));
            }
        }
        else {
            itemsViewBean.setHasNext(false);
        }
        itemsViewBean.setSort(browseSort);

        if (filterGroupID != null && filterGroupID.equals("people") && allFilter != null && !itemsViewBean.getHasNext() && !getUser().isAnonymous()) {
            items.addAll(invitedUserProvider.buildInvitedUserItemBeans(getUserID(), targetUser));
        }

        itemsViewBean.setCurrentUserPartner(targetUser.isPartner());
        return itemsViewBean;
    }

    private Iterator<User> getAllFilterResults(int start, int numResults, String query, Set<BrowseFilter> filters,
            BrowseFilterGroup group, BrowseSort browseSort) {
        Iterator<User> results;
        /* following checked in at commit 355fbf85 (2010-12-13) Seems bug.
         * If a query string is specified, then the passed browseSort is ignored */
        // default to relevance if we have a query, else specified sort
        ProfileSearchCriteria.Sort sort = ((ProfileFieldSort) SORT_DEFAULT).getProfileSearchCriteriaSort();
        //if (StringUtils.isNotBlank(query)) {
        //    sort = ProfileSearchCriteria.DefaultSort.RELEVANCE;
        //}
        //else
        if (browseSort instanceof ProfileFieldSort) {
            sort = ((ProfileFieldSort) browseSort).getProfileSearchCriteriaSort();
        }

        // match term or begins with
        ProfileSearchCriteriaBuilder profileSearchCriteriaBuilder = new ProfileSearchCriteriaBuilder(getUserID(),
                query).setStart(start).setRange(numResults + 1).setSearchEmail(true).setCalculateFacets(false)
                .setReturnExternalUsers(false).setReturnDisabledUsers(true).setSearchNamePhonetically(false)
                .setSearchUsername(profileSearchQuerySettingsManager.isUserSearchQueryUsername()).setSort(sort)
                .setUserLocale(Locale.getDefault());

        // apply filters
        List<ProfileSearchFilter> profileSearchFilters = Lists.newArrayList();
        for (BrowseFilter filter : filters) {
            if (filter instanceof ExcludePartnerFilter || filter instanceof ExcludeDisabledFilter) {
                filter.getSearchFilterDef().apply(profileSearchCriteriaBuilder);
            }
            else if (filter instanceof ShowOnlineUserFilter && ((ShowOnlineUserFilter) filter).isShowDisabled()) {
                profileSearchCriteriaBuilder.setReturnOnlineUsers(true);
            }
            else if (filter instanceof ProfileFacetFilter) {
                ProfileSearchFilter profileSearchFilter = ((ProfileFacetFilter) filter).getProfileSearchFilter();
                // join date filter needs specific logic as it's not truly a profile field
                if (profileSearchFilter.getFieldID() == JoinDateFilter.FIELD_ID) {
                    String minValue = profileSearchFilter.getMinValue();
                    if (StringUtils.isNotBlank(minValue) && StringUtils.isNumeric(minValue)) {
                        profileSearchCriteriaBuilder.setMinCreationDate(new Date(Long.valueOf(minValue)));
                    }
                    String maxValue = profileSearchFilter.getMaxValue();
                    if (StringUtils.isNotBlank(maxValue) && StringUtils.isNumeric(maxValue)) {
                        profileSearchCriteriaBuilder.setMaxCreationDate(new Date(Long.valueOf(maxValue)));
                    }
                }
                else {
                    profileSearchFilters.add(profileSearchFilter);
                }
            }
            else if (filter instanceof UserTypeFacetFilter) {
                UserTypeFacetFilter userTypeFacetFilter = (UserTypeFacetFilter)filter;
                User.Type userType = userTypeFacetFilter.getUserType();
                if (userType != null) {
                    if (userType.equals(User.Type.REGULAR)) {
                        profileSearchCriteriaBuilder.setReturnRegularUsers(true);
                        profileSearchCriteriaBuilder.setReturnPartnerUsers(false);
                    }
                    else if (userType.equals(User.Type.PARTNER)) {
                        profileSearchCriteriaBuilder.setReturnRegularUsers(false);
                        profileSearchCriteriaBuilder.setReturnPartnerUsers(true);
                    }
                }
            }
            else if (filter instanceof TagFilter) {
                profileSearchCriteriaBuilder.setTags(((TagFilter) filter).getValues());
            }
        }

        profileSearchCriteriaBuilder.setFilters(profileSearchFilters);

        ProfileSearchResult profileSearchResult = profileSearchQueryManager.executeSearch(profileSearchCriteriaBuilder
                .build());

        results = new BrowseResultPostProcessingIterator<>(profileSearchResult.results().iterator(), filters);

        // Only use result count if we're no longer compensating for user visibility
        if (start + numResults > profileSearchSettingsManager.getMaxUserVisibilityRedundancy()) {
            results = new IteratorWithResultCount(results,profileSearchResult.getComponent(ResultCountComponent.class));
        }

        // second call to calculate facets
        profileSearchCriteriaBuilder.setCalculateFacets(true);
        profileSearchCriteriaBuilder.setIncludeAppliedFacets(true);
        ProfileSearchResult facetResult = profileSearchQueryManager.executeSearch(profileSearchCriteriaBuilder.build());
        ResultProfileFieldFacetComponent facetComponent = facetResult
                .getComponent(ResultProfileFieldFacetComponent.class);
        if (facetComponent != null && group instanceof ProfileFacetBrowseFilterGroup) {
            ((ProfileFacetBrowseFilterGroup) group).buildFacetFilters(facetComponent.getFacets());
        }

        return results;
    }

    private static class IteratorWithResultCount implements Iterator<User> {

        private final Iterator<User> iterator;
        private final ResultCountComponent count;

        private IteratorWithResultCount(Iterator<User> iterator, ResultCountComponent component) {
            this.iterator = iterator;
            this.count = component;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public User next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

        public int getCount() {
            return count != null ? count.getTotalCount() : -1;
        }
    }

    @Override
    public OrgChartItemsViewBean getOrgChart(String userIDStr) {
        HierarchicalUserRelationshipGraph reportingGraph = userRelationshipManager
                .getDefaultHierarchicalRelationshipGraph();

        if (reportingGraph == null || !reportingGraph.isEnabled() || getUser().isPartner()) {
            throw ErrorBuilder.gone();
        }
        else {
            UserItemBean manager = null;
            List<UserItemBean> peers = Collections.emptyList();
            long userID = browseFilterManager.getUserIDFromString(userIDStr);
            User targetUser = getEffectiveUser(userID);

            List<UserItemBean> escalationChain = wrapUserCollection(
                    userRelationshipManager.getEscalationChain(targetUser, reportingGraph), orgChartNodeProps);
            // last person in esc chain is user's manager
            if (!escalationChain.isEmpty()) {
                manager = escalationChain.get(escalationChain.size() - 1);
                if (manager != null) {
                    peers = wrapUserCollection(userRelationshipManager.getColleagues(targetUser, reportingGraph),
                            orgChartNodeProps);
                }
            }

            List<UserItemBean> directReports = wrapUserCollection(
                    userRelationshipManager.getDirectReports(targetUser, reportingGraph), orgChartNodeProps);

            Map<String, Object> context = Maps.<String, Object> newHashMap();
            context.put(UserItemBeanProfileImageProviderImpl.PARAM_IMAGE_SIZE, 350);
            return new OrgChartItemsViewBean(userItemBeanBuilder.build(targetUser, targetUser, orgChartUserProps,
                    context), escalationChain, manager, peers, directReports);
        }
    }

    public ItemsViewBean<UserItemBean> getDirectReports(long userID) {
        if (getUser().isPartner()) {
            throw ErrorBuilder.gone();
        }
        HierarchicalUserRelationshipGraph graph = userRelationshipManager.getDefaultHierarchicalRelationshipGraph();
        Set<UserItemBean> reports = Sets.newLinkedHashSet();
        if (graph != null && graph.isEnabled()) {
            try {
                User user = userManager.getUser(userID);
                List<User> users = userRelationshipManager.getDirectReports(user, graph);
                reports.addAll(wrapUserCollection(users, orgChartNodeProps));
            }
            catch (Exception e) {
                log.warn("Couldn't retrieve reports for user " + userID, e);
            }
        }
        return new ItemsViewBean<>(reports);
    }

    public int getDirectReportCount(long userID) {
        if (getUser().isPartner()) {
            throw ErrorBuilder.gone();
        }
        HierarchicalUserRelationshipGraph graph = userRelationshipManager.getDefaultHierarchicalRelationshipGraph();
        if (graph != null && graph.isEnabled()) {
            try {
                User user = userManager.getUser(userID);
                return userRelationshipManager.getDirectReportCount(user, graph);
            }
            catch (Exception e) {
                log.warn("Couldn't retrieve direct report count for user " + userID, e);
            }
        }
        return 0;
    }

    @Override
    public ItemsViewBean<UserItemBean> getCommonRelatedUsers(long userID) {
        MeshUserRelationshipGraph graph = userRelationshipManager.getDefaultMeshRelationshipGraph();
        Set<UserItemBean> commonRelatedBeans = Sets.newLinkedHashSet();
        User viewingUser = getUser();
        if (graph != null && graph.isEnabled() && viewingUser != null && !viewingUser.isAnonymous()) {
            try {
                User user = userManager.getUser(userID);
                List<User> users = userRelationshipManager.getCommonBidirectionalRelatedUsers(user, viewingUser, graph);
                commonRelatedBeans.addAll(wrapUserCollection(users, userItemBeanProps));
            }
            catch (Exception e) {
                log.warn("Couldn't retrieve common related users for user " + userID, e);
            }
        }
        return new ItemsViewBean<>(commonRelatedBeans);
    }

    @Override
    public int getCommonRelatedUsersCount(long userID) {
        MeshUserRelationshipGraph graph = userRelationshipManager.getDefaultMeshRelationshipGraph();
        User viewingUser = getUser();
        if (graph != null && graph.isEnabled() && viewingUser != null && !viewingUser.isAnonymous()) {
            try {
                User user = userManager.getUser(userID);
                return userRelationshipManager.getCommonBidirectionalRelatedUsers(user, viewingUser, graph).size();
            }
            catch (Exception e) {
                log.warn("Couldn't retrieve common related users count for user " + userID, e);
            }
        }
        return 0;
    }

    public String getUserProperty(String userIDStr, String propKey) {
        try {
            long userID = browseFilterManager.getUserIDFromString(userIDStr);
            User user = userManager.getUser(userID);
            return user.getProperties().get(propKey);
        }
        catch (UserNotFoundException e) {
            throw ErrorBuilder.gone();
        }
    }

    public void setUserProperty(String userIDStr, String propKey, String propValue) {
        try {
            long userID = browseFilterManager.getUserIDFromString(userIDStr);
            User user = userManager.getUser(userID);
            if (propKey != null && propValue != null && user.isPropertyEditSupported()) {
                user.getProperties().put(propKey, propValue);
                userManager.updateUser(user);
            }
        }
        catch (UserNotFoundException e) {
            throw ErrorBuilder.gone();
        }
        catch (UserAlreadyExistsException e) {
            throw ErrorBuilder.conflict(ErrorBuilder.ERROR_CODE_ALREADY_EXISTS, e.getMessage());
        }
        catch (UnsupportedOperationException | UnauthorizedException e) {
            throw ErrorBuilder.unauthorized();
        }
    }

    public void removeUserProperty(String userIDStr, String propKey) {
        try {
            long userID = browseFilterManager.getUserIDFromString(userIDStr);
            User user = userManager.getUser(userID);
            if (propKey != null && user.isPropertyEditSupported()) {
                user.getProperties().remove(propKey);
                userManager.updateUser(user);
            }
        }
        catch (UserNotFoundException e) {
            throw ErrorBuilder.gone();
        }
        catch (UserAlreadyExistsException e) {
            throw ErrorBuilder.conflict(ErrorBuilder.ERROR_CODE_ALREADY_EXISTS, e.getMessage());
        }
        catch (UnsupportedOperationException | UnauthorizedException e) {
            throw ErrorBuilder.unauthorized();
        }
    }

    @Override
    public boolean isLoginProcessing(long userID) {
        return groupManager instanceof LongRunningLoginProcess && ((LongRunningLoginProcess) groupManager).isProcessing(userID);
    }

    private List<UserItemBean> wrapUserCollection(Collection<User> coll, Collection<String> propertyNames) {
        return new ArrayList<>(userItemBeanBuilder.build(coll, authenticationProvider.getJiveUser(),
                propertyNames, Maps.<String, Object>newHashMap()));
    }

    public void setProfileSearchQueryManager(ProfileSearchQueryManager profileSearchQueryManager) {
        this.profileSearchQueryManager = profileSearchQueryManager;
    }

    public void setProfileSearchQuerySettingsManager(ProfileSearchQuerySettingsManager profileSearchQuerySettingsManager) {
        this.profileSearchQuerySettingsManager = profileSearchQuerySettingsManager;
    }

    public void setProfileSearchSettingsManager(ProfileSearchSettingsManager profileSearchSettingsManager) {
        this.profileSearchSettingsManager = profileSearchSettingsManager;
    }

    public void setUserRelationshipManager(UserRelationshipManager userRelationshipManager) {
        this.userRelationshipManager = userRelationshipManager;
    }

    public void setEntitlementTypeProvider(EntitlementTypeProvider entitlementTypeProvider) {
        this.entitlementTypeProvider = entitlementTypeProvider;
    }

    public void setBrowseFilterManager(BrowseFilterManager browseFilterManager) {
        this.browseFilterManager = browseFilterManager;
    }

    public void setEmailValidationHelper(EmailValidationHelper emailValidationHelper) {
        this.emailValidationHelper = emailValidationHelper;
    }

    @InjectConfiguration
    public void setInvitationConfiguration(InvitationConfiguration invitationConfiguration) {
        this.invitationConfiguration = invitationConfiguration;
    }
}
