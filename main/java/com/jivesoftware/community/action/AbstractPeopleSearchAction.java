/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.UserRole;
import com.grail.util.BATConstants;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jivesoftware.base.User;
import com.jivesoftware.base.event.SearchEvent;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.ContentTag;
import com.jivesoftware.community.ContentTagCloudBean;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.PresenceManager;
import com.jivesoftware.community.TagResultFilter;
import com.jivesoftware.community.ThreadResultFilter;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.action.util.Pageable;
import com.jivesoftware.community.action.util.Paginator;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.filter.MemberFilter;
import com.jivesoftware.community.browse.filter.group.PeopleBrowseFilterGroupProvider;
import com.jivesoftware.community.browse.sort.ModificationDateSort;
import com.jivesoftware.community.browse.util.CastingIterator;
import com.jivesoftware.community.impl.search.user.ProfileSearchIndexManager;
import com.jivesoftware.community.search.ResultCountComponent;
import com.jivesoftware.community.search.user.ProfileSearchCriteria;
import com.jivesoftware.community.search.user.ProfileSearchCriteriaBuilder;
import com.jivesoftware.community.search.user.ProfileSearchCriteriaJSONSerializer;
import com.jivesoftware.community.search.user.ProfileSearchQueryManager;
import com.jivesoftware.community.search.user.ProfileSearchResult;
import com.jivesoftware.community.search.user.ProfileSearchResultViewHelper;
import com.jivesoftware.community.socialgroup.SocialGroup;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.community.user.profile.ProfileFieldManager;
import com.jivesoftware.community.user.profile.ProfileFieldValueCount;
import com.jivesoftware.community.user.profile.ProfileImageManager;
import com.jivesoftware.community.user.profile.ProfileManager;
import com.jivesoftware.community.user.profile.ProfileSearchFilter;
import com.jivesoftware.community.user.relationships.MeshUserRelationshipGraph;
import com.jivesoftware.community.user.relationships.UserRelationshipList;
import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to display logged on users, member list, member search, etc.
 */
public abstract class AbstractPeopleSearchAction extends CommunityActionSupport implements Pageable, Preparable {

    protected static final Logger log = LogManager.getLogger(AbstractPeopleSearchAction.class);

    private int maxActivityCount = JiveGlobals.getJiveIntProperty("people.activity.result.threshold", 25);

    public static final int RANGE = 12;

    public boolean lastNameSupported = false;

    public static final String VIEW_ALPHA = "alphabetical";
    public static final String VIEW_NEWEST = "newest";
    public static final String VIEW_STATUS = "status";
    public static final String VIEW_ONLINE = "online";
    public static final String VIEW_SEARCH = "search";

    public static final Map<String, ProfileSearchCriteria.Sort> sortMapping =
            ImmutableMap.<String, ProfileSearchCriteria.Sort>of(
                    PeopleBrowseFilterGroupProvider.SORT_DATE, ProfileSearchCriteria.DefaultSort.CREATION_DATE,
                    PeopleBrowseFilterGroupProvider.SORT_USERNAME, ProfileSearchCriteria.DefaultSort.USERNAME,
                    PeopleBrowseFilterGroupProvider.SORT_NAME, ProfileSearchCriteria.DefaultSort.NAME,
                    PeopleBrowseFilterGroupProvider.SORT_LAST_NAME, ProfileSearchCriteria.DefaultSort.LAST_NAME,
                    PeopleBrowseFilterGroupProvider.SORT_STATUS_LEVEL, ProfileSearchCriteria.DefaultSort.STATUS_LEVEL
            );

    private String query = "";
    private String prefix = "";
    private String sort = "";
    protected String view = "";
    private List<ProfileSearchFilter> searchFilters;
    private Map<ProfileField, Collection<ProfileFieldValueCount>> filterableFields;

    private int totalUserCount = 0;
    private int totalOnlineCount = 0;
    private int newlyAddedCount = 0;
    private int newestMemberListSize = JiveGlobals.getJiveIntProperty("people.newest.members.size", 2); //2 by default
    private Collection<User> newestMembers;
    private int newestDayThreshold = JiveGlobals.getJiveIntProperty("people.newest.members.threshold.days", 7); //default to one week approx
    private int topMemberListSize = JiveGlobals.getJiveIntProperty("people.top.members.size", 2); //2 by default
    private Collection<User> topMembers;
    private Date newestMinDate;

    private String tag;

    //friending action
    private MeshUserRelationshipGraph friendsGraph;
    private Collection<UserRelationshipList> friendLists;

    //social group invitation action
    private Iterator<SocialGroup> socialGroups;

    // for tag cloud
    private int tagSort = ContentTag.SORT_TAGNAME;
    private int numOfBuckets = JiveGlobals.getJiveIntProperty("people.tagcloug.buckets.size", 10); //10 by default
    private int numResults = JiveGlobals.getJiveIntProperty("people.tagcloug.results.size", 50); //50 by default
    private List<ContentTagCloudBean> tagCloud;

    private int start = 0;
    private int range = JiveGlobals.getJiveIntProperty("people.search.pagesize", RANGE);
    private boolean fuzzyNameEnabled = JiveGlobals.getJiveBooleanProperty("people.search.fuzzy.enabled", false);
    private boolean showDisabledUsers = false;
    private boolean showExternalUsers = false;
    private boolean onlineOnly = false;
    private boolean recentlyAddedOnly = false;

    protected ProfileSearchIndexManager profileSearchIndexManager;
    protected PresenceManager presenceManager;
    protected ProfileFieldManager profileFieldManager;
    protected ProfileManager profileManager;
    protected ProfileImageManager profileImageManager;
    protected ProfileSearchQueryManager profileSearchQueryManager;
    protected ProfileSearchResultViewHelper viewHelper;
    protected ProfileSearchCriteriaJSONSerializer serializer;

    protected EventDispatcher eventDispatcher;

    protected boolean socGrpSupported = true;
    protected boolean tagCloudSupported = true;
    protected boolean peopleStatsSupported = true;
    protected boolean canInvitePartners = false;

    private String queryEncoded;
    private int facetTimeout = JiveGlobals.getJiveIntProperty("people.facet.timeout", 30000);

    protected int startLimit;
    protected int rangeLimit;
    protected int pageLimit;
    protected boolean showBrowse;
    
    private Integer brand = -1;
    private Integer region = -1;
    private Integer country = -1;
    private Integer role = -1;
    private Integer jobTitle = -1;
    private String fromSynchro;


    public boolean isCanInvitePartners() {
        return canInvitePartners;
    }

    public void setCanInvitePartners(boolean canInvitePartners) {
        this.canInvitePartners = canInvitePartners;
    }

    public void setProfileSearchIndexManager(ProfileSearchIndexManager profileSearchIndexManager) {
        this.profileSearchIndexManager = profileSearchIndexManager;
    }

    public void setPresenceManager(PresenceManager presenceManager) {
        this.presenceManager = presenceManager;
    }

    public void setProfileFieldManager(ProfileFieldManager profileFieldManager) {
        this.profileFieldManager = profileFieldManager;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public void setProfileImageManager(ProfileImageManager profileImageManager) {
        this.profileImageManager = profileImageManager;
    }

    public void setProfileSearchQueryManager(ProfileSearchQueryManager profileSearchQueryManager) {
        this.profileSearchQueryManager = profileSearchQueryManager;
    }

    public void setProfileSearchResultViewHelper(ProfileSearchResultViewHelper viewHelper) {
        this.viewHelper = viewHelper;
    }

    public void setProfileSearchCriteriaJSONSerializer(ProfileSearchCriteriaJSONSerializer serializer) {
        this.serializer = serializer;
    }

    public void setJiveEventDispatcher(EventDispatcher jiveEventDispatcher) {
        this.eventDispatcher = jiveEventDispatcher;
    }

    private ProfileSearchResult result;
    private Collection<User> userResults;
    private Paginator paginator;

    public boolean getUsernameEnabled() {
        return ServletRequestUtils.getBooleanParameter(request, "usernameEnabled", getDefaultUsernameEnabled());
    }

    protected boolean getDefaultUsernameEnabled() {
        return JiveGlobals.getJiveBooleanProperty("jive.peopleAction.search.username.enabled", true);
    }

    public boolean getNameEnabled() {
        return ServletRequestUtils.getBooleanParameter(request, "nameEnabled", true);
    }

    public boolean getEmailEnabled() {
        return ServletRequestUtils.getBooleanParameter(request, "emailEnabled", true);
    }

    public boolean getProfileEnabled() {
        return ServletRequestUtils.getBooleanParameter(request, "profileEnabled", true);
    }

    public boolean isPresenceEnabled() {
        return presenceManager.isPresencesEnabled();
    }

    public boolean isFullNameDisplayed() {
        return JiveGlobals.getJiveBooleanProperty("skin.default.displayFullNames");
    }

    public boolean isLastNameSupported() {
        return lastNameSupported;
    }

    public boolean isFuzzyNameEnabled() {
        return fuzzyNameEnabled;
    }

    public void setFuzzyNameEnabled(boolean fuzzyNameEnabled) {
        this.fuzzyNameEnabled = fuzzyNameEnabled;
    }

    public boolean isFacetsSupported() {
        return false;
    }

    public boolean isSocGrpSupported() {
        return socGrpSupported;
    }

    protected void setSocGrpSupported(boolean socGrpSupported) {
        this.socGrpSupported = socGrpSupported;
    }

    public boolean isTagCloudSupported() {
        return tagCloudSupported;
    }

    protected void setTagCloudSupported(boolean tagCloudSupported) {
        this.tagCloudSupported = tagCloudSupported;
    }

    public boolean isPeopleStatsSupported() {
        return peopleStatsSupported;
    }

    protected void setPeopleStatsSupported(boolean peopleStatsSupported) {
        this.peopleStatsSupported = peopleStatsSupported;
    }

    public boolean isShowDisabledUsers() {
        return showDisabledUsers;
    }

    public void setShowDisabledUsers(boolean showDisabledUsers) {
        this.showDisabledUsers = showDisabledUsers;
    }

    public boolean isShowExternalUsers() {
        return showExternalUsers;
    }

    public void setShowExternalUsers(boolean showExternalUsers) {
        this.showExternalUsers = showExternalUsers;
    }

    public boolean isOnlineOnly() {
        return onlineOnly;
    }

    public void setOnlineOnly(boolean onlineOnly) {
        this.onlineOnly = onlineOnly;
    }

    public boolean isRecentlyAddedOnly() {
        return recentlyAddedOnly;
    }

    public void setRecentlyAddedOnly(boolean recentlyAddedOnly) {
        this.recentlyAddedOnly = recentlyAddedOnly;
    }

    public boolean isShowBrowse() {
        return showBrowse;
    }

    public int getStart() {
        return start;
    }

    public ThreadResultFilter getResultFilter() {
        ThreadResultFilter filter = new ThreadResultFilter();
        filter.setStartIndex(start);
        filter.setNumResults(range);
        return filter;
    }

    public int getNumResults() {
        return range;
    }

    public void setStart(int start) {
        this.start = Math.min(start, startLimit);
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = Math.min(range, rangeLimit);
    }

    public int getTotalUserCount() {
        return totalUserCount;
    }

    public int getTotalOnlineCount() {
        return totalOnlineCount;
    }

    public int getNewlyAddedCount() {
        return newlyAddedCount;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // Methods used for filter options

    /**
     * This is for legacy "cid" parameters.
     *
     * @param community the community
     */
    public void setCid(Community community) {
        this.community = community;
    }

    public List<ProfileSearchFilter> getSearchFilters() {
        return searchFilters;
    }

    public void setSearchFilters(List<ProfileSearchFilter> searchFilters) {
        this.searchFilters = searchFilters;
    }

    public int getTagSort() {
        return tagSort;
    }

    public void setTagSort(int tagSort) {
        this.tagSort = tagSort;
    }

    public Collection<User> getNewestMembers() {
        return newestMembers;
    }

    public Collection<User> getTopMembers() {
        return topMembers;
    }

    public Map<ProfileField, ProfileSearchFilter> getAppliedFilterMap() {
        Map<ProfileField, ProfileSearchFilter> map = new LinkedHashMap<ProfileField, ProfileSearchFilter>();
        if (searchFilters != null) {
            for (ProfileSearchFilter searchFilter : searchFilters) {
                if (!searchFilter.isEmpty()) {
                    map.put(profileFieldManager.getProfileField(searchFilter.getFieldID()), searchFilter);
                }
            }
        }
        return map;
    }

    public Community getRootCommunity() {
        return communityManager.getRootCommunity();
    }

    public int getTotalItemCount() {
        if (result == null) {
            return 0;
        }

        ResultCountComponent count = result.getComponent(ResultCountComponent.class);
        if (count != null) {
            return count.getTotalCount();
        }

        return 0;
    }

    public List<ContentTagCloudBean> getTagCloud() {
        return tagCloud;
    }

    public MeshUserRelationshipGraph getFriendsGraph() {
        return friendsGraph;
    }

    public Collection<UserRelationshipList> getFriendLists() {
        return friendLists;
    }

    public Iterator<SocialGroup> getSocialGroups() {
        return socialGroups;
    }

    public Collection<User> getResults() {
        return userResults == null ? Collections.<User>emptyList() : userResults;
    }

    public Paginator getPaginator() {
        if (paginator == null) {
            paginator = new Paginator(this, pageLimit);
        }
        return paginator;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (prefix.length() > 0) {
            this.prefix = prefix.substring(0, 1);
        }
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
        if (view != null && view.equals(VIEW_NEWEST)) {
            recentlyAddedOnly = true;
        }
        else if (view != null && view.equals(VIEW_ONLINE)) {
            onlineOnly = true;
        }
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        if (sortMapping.containsKey(sort)) {
            this.sort = sort;
        }
    }

    public boolean acceptableParameterName(String parameterName) {
        return parameterName.startsWith("searchFilters[") || super.acceptableParameterName(parameterName);
    }

    public Map<ProfileField, Collection<ProfileFieldValueCount>> getFilterableFields() {
        return filterableFields;
    }

    public Map<ProfileFieldState, Collection<ProfileFieldValueCount>> getFilterableFieldMap() {
        Map appliedFilters = getAppliedFilterMap();
        Map<ProfileFieldState, Collection<ProfileFieldValueCount>> map
                = new LinkedHashMap<ProfileFieldState, Collection<ProfileFieldValueCount>>();
        boolean guestUser = authProvider.getJiveUser().isAnonymous();
        if (filterableFields != null) {
            for (ProfileField profileField : filterableFields.keySet()) {
                if ((guestUser && profileField.isVisibleToGuests()) ||
                        (!guestUser && profileField.isVisibleToUsers()) || authProvider.isSystemAdmin())
                {
                    map.put(new ProfileFieldState(profileField, appliedFilters.keySet().contains(profileField)),
                            filterableFields.get(profileField));
                }
            }
        }
        return map;
    }

    public static class ProfileFieldState {

        private ProfileField field;
        private boolean applied;

        public ProfileFieldState(ProfileField field, boolean applied) {
            this.field = field;
            this.applied = applied;
        }

        public ProfileField getField() {
            return field;
        }

        public boolean isApplied() {
            return applied;
        }
    }

    public ProfileSearchCriteria.Sort getSortOrder() {
        if (sortMapping.containsKey(sort)) {
            return sortMapping.get(sort);
        }
        else if (view.equalsIgnoreCase(VIEW_NEWEST)) {
            sort = PeopleBrowseFilterGroupProvider.SORT_DATE;
            return sortMapping.get(sort);
        }
        else if (view.equalsIgnoreCase(VIEW_STATUS)) {
            sort = PeopleBrowseFilterGroupProvider.SORT_STATUS_LEVEL;
            return sortMapping.get(sort);
        }
        else if (StringUtils.isBlank(query) && view.equalsIgnoreCase(VIEW_ALPHA)) {
            sort = (isFullNameDisplayed()) ? PeopleBrowseFilterGroupProvider.SORT_NAME : PeopleBrowseFilterGroupProvider.SORT_USERNAME;
            return sortMapping.get(sort);
        }
        else {
            return ProfileSearchCriteria.DefaultSort.RELEVANCE;
        }
    }

    private boolean isWildcardQuery() {
        return query != null && "*".equals(query.trim());
    }

    public void prepare() throws Exception {
        if (filterableFields == null) {
            filterableFields = new LinkedHashMap<ProfileField, Collection<ProfileFieldValueCount>>();
            Collection<ProfileField> fields = profileFieldManager.getFilterableFields();
            for (ProfileField field : fields) {
                filterableFields.put(field, new ArrayList<ProfileFieldValueCount>());
            }
        }
        this.lastNameSupported = JiveGlobals.getJiveBooleanProperty(JiveConstants.USER_LAST_NAME_FIRST_NAME_ENABLED);

        // Only display the browse tab if the number of application users is less than 1000
        // (by default), and limit the start, range, and number of pages parameters.
        showBrowse = userManager.getApplicationUserCount() <
                JiveGlobals.getJiveIntProperty("people.search.browse.limit", 1000);
        pageLimit = JiveGlobals.getJiveIntProperty("people.search.page.limit", 100);
        startLimit = JiveGlobals.getJiveIntProperty("people.search.start.limit", 1000);
        rangeLimit = JiveGlobals.getJiveIntProperty("people.search.range.limit", 100);
    }

    private boolean hasFilters() {
        if (searchFilters == null || searchFilters.isEmpty()) {
            return false;
        }
        for (ProfileSearchFilter searchFilter : searchFilters) {
            if (!searchFilter.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String execute() {
        if (AnonymousUser.isAnonymousUser(getUserID()) && !profileManager.isAnonymousBrowsingPeoplePermitted()) {
            return UNAUTHENTICATED;
        }

        boolean isPrefix = StringUtils.isNotBlank(prefix);
        boolean isQuery = StringUtils.isNotBlank(query);
        boolean isTag = StringUtils.isNotBlank(tag);

        boolean isNewestView = (view != null && VIEW_NEWEST.equals(view) && !isQuery) || recentlyAddedOnly;
        boolean isOnlineView = (view != null && VIEW_ONLINE.equals(view) && !isQuery) || onlineOnly;
        boolean isStatusView = (view != null && VIEW_STATUS.equals(view) && !isQuery);
        boolean isEveryoneView = (view != null && VIEW_ALPHA.equals(view) && !isQuery || isNewestView || isStatusView);

        //check default view if none specified
        if (!isQuery && !isPrefix && !isTag && StringUtils.isEmpty(view)) {
            isEveryoneView = getDefaultView();
        }

        boolean isPrefixOrWildcard = isPrefix || isWildcardQuery();
        boolean isSearch = (isQuery || isPrefixOrWildcard || hasFilters() || isEveryoneView || isTag || isOnlineView);

        if (isSearch) {
            if (view != null && VIEW_ALPHA.equals(view) && !showBrowse) {
                return SUCCESS;
            }

            if ((getSort().equals(PeopleBrowseFilterGroupProvider.SORT_USERNAME) ||
                    getSort().equals(PeopleBrowseFilterGroupProvider.SORT_NAME) ||
                    getSort().equals(PeopleBrowseFilterGroupProvider.SORT_LAST_NAME)) && !showBrowse) {
                sort = "";
            }

            boolean useUsername = (!isWildcardQuery() && getUsernameEnabled());
            boolean useName = (isPrefixOrWildcard || getNameEnabled());
            boolean useFuzzyName = (!isPrefixOrWildcard && isFuzzyNameEnabled());
            boolean useEmail = (!isPrefixOrWildcard && getEmailEnabled());
            boolean useProfile = (!isPrefixOrWildcard && getProfileEnabled());

            String q = (!isQuery || isWildcardQuery()) ? null : query;
            ProfileSearchCriteriaBuilder criteriaBuilder = new ProfileSearchCriteriaBuilder(getUserID(), q)
                    .setPrefix(prefix)
                    .setFilters(getSearchFilters())
                    .setReturnPartnerUsers(canInvitePartners)
                    .setTags(Sets.newHashSet(tag))
                    .setSort(getSortOrder())
                    .setSearchUsername(useUsername)
                    .setSearchName(useName)
                    .setSearchNamePhonetically(useFuzzyName)
                    .setSearchEmail(useEmail)
                    .setSearchProfile(useProfile)
                    .setReturnDisabledUsers(showDisabledUsers)
                    .setReturnExternalUsers(showExternalUsers)
                    .setReturnOnlineUsers(onlineOnly)
                    .setUserLocale(getLocale());

            if (community != null) {
                criteriaBuilder.setCommunityID(community.getID());
            }

            //if filter values don't validate, show error
            if (criteriaBuilder.getFilters() != null && !criteriaBuilder.getFilters().isEmpty()) {
                validateFilterValues(criteriaBuilder.getFilters());
            }

            if (recentlyAddedOnly) {
                criteriaBuilder.setMinCreationDate(getNewestMinDate());
            }

            criteriaBuilder.setStart(start);
            criteriaBuilder.setRange(range);

            Map<String, String> properties = new HashMap<String, String>();

            /**
             * Fetch Brand
             */
            if(getBrand() != null && getBrand() > 0) {
                int id = getBrand();
                Map<Integer, String> brands = SynchroGlobal.getBrands(true,  new Long(1));
                if(brands.containsKey(id)) {
                    properties.put("brand", getEncodedString(brands.get(id)));
                }
            }

            /**
             * Fetch Region
             */
            if(getRegion() != null && getRegion() > 0) {
                int id = getRegion();
                Map<Integer, String> regions = SynchroGlobal.getRegions();
                if(regions.containsKey(id)) {
                    properties.put("region", getEncodedString(regions.get(id)));
                }

            }

            /**
             * Fetch Country
             */
            if(getCountry() != null && getCountry() > 0) {
                int id = getCountry();
                Map<Integer, String> endMarkets = SynchroGlobal.getEndMarkets();
                if(endMarkets.containsKey(id)) {
                    properties.put("country", getEncodedString(endMarkets.get(id)));
                }
            }

            /**
             * Fetch User Role
             */
            if(getRole() != null && getRole() > 0) {
                int id = getRole();
                if(id==SynchroConstants.SYNCHRO_PROJECT_OWNER_FIELDID)
                {
                	properties.put("role", SynchroConstants.SYNCHRO_PROJECT_OWNER_FIELDNAME);
                }
                else if(id==SynchroConstants.SYNCHRO_AGENCY_BAT_USER_FIELDID)
                {
                	properties.put("role", SynchroConstants.SYNCHRO_AGENCY_BAT_USER_FIELDNAME);
                }
                else if(UserRole.getById(id) != null)
                {
                	properties.put("role", UserRole.getById(id).name());
                }
               
            }

            /**
             * Fetch Job Titles
             */
            if(getJobTitle() != null && getJobTitle() > 0) {
                int id = getJobTitle();
                Map<Integer, String> jobTitles = SynchroGlobal.getJobTitles();
                if(jobTitles.containsKey(id)) {
                    properties.put("jobTitle", getEncodedString(jobTitles.get(id)));
                }
            }

            if(properties != null && properties.size() > 0) {
                criteriaBuilder.setProperties(properties);
            }

            /**
             * Set From Synchro parameter
             */
            /*
            if(StringUtils.isNotBlank(getFromSynchro()) && StringUtils.equalsIgnoreCase(getFromSynchro(), "true"))
            {
            	criteriaBuilder.setFromSynchro(getFromSynchro());
            }
            */
            final String PORTAL_KEY = BATConstants.GRAIL_PORTAL_TYPE;
            if(getSession().get(PORTAL_KEY)!=null )
            {
                String portalType = (String)getSession().get(PORTAL_KEY);
                if(StringUtils.equalsIgnoreCase(portalType, BATConstants.JIVE_SYNCHRO_GROUP_NAME))
                {
                    criteriaBuilder.setFromSynchro("true");
                }
            }
            
            ProfileSearchCriteria criteria = criteriaBuilder.build();
            result = profileSearchQueryManager.executeSearch(criteria);
            userResults = Lists.newLinkedList(viewHelper.toViewBeans(result.results()));
            queryEncoded = serializer.encode(criteria);

            eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_USER, criteria));
        }

        // friending and social groups actions
        if (isSearch && getUser() != null) {
            friendsGraph = userRelationshipManager.getDefaultMeshRelationshipGraph();
            friendLists = userRelationshipManager.getRelationshipListsByOwner(getUser());

            if (socGrpSupported) {
                socialGroups = new CastingIterator<SocialGroup> (
                        browseManager.getContainers(
                                ImmutableSet.<BrowseFilter>of(new MemberFilter().getBoundInstance(getUserID())),
                                new ModificationDateSort(), 0, Integer.MAX_VALUE));
            }
        }

        //if main page, show stats
        if (!isSearch) {
            if (peopleStatsSupported) {
                totalUserCount = userManager.getApplicationUserCount();
                totalOnlineCount = presenceManager.getOnlineUserCount();

                ProfileSearchCriteria newestCriteria = ProfileSearchCriteriaBuilder.buildNewestMembersCriteria(
                        getUserID(), 0, newestMemberListSize);
                ProfileSearchResult newestResult = profileSearchQueryManager.executeSearch(newestCriteria);
                newestMembers = Lists.newLinkedList(newestResult.results());

                newlyAddedCount = userManager.getRecentUserCount(getNewestMinDate());

                ProfileSearchCriteria topCriteria = ProfileSearchCriteriaBuilder.buildTopMembersCriteria(
                        getUserID(), 0, topMemberListSize);
                ProfileSearchResult topResult = profileSearchQueryManager.executeSearch(topCriteria);
                topMembers = Lists.newLinkedList(topResult.results());
            }

            if (tagCloudSupported) {

                TagResultFilter tf = TagResultFilter.createDefaultFilter();
                tf.setNumResults(numResults);

                Map<String, Integer> tagMap = tagManager.getTagMap(communityManager.getRootCommunity(), tf,
                        Lists.newArrayList(tagManager.getTaggableType(JiveConstants.USER)));

                tagCloud = tagManager.getTagCloud(tagMap, numOfBuckets, tagSort);
            }
        }

        return SUCCESS;
    }

    private void validateFilterValues(Collection<ProfileSearchFilter> filters) {
        for (ProfileSearchFilter filter : filters) {
            if (!filter.isEmpty()) {
                ProfileField field = profileFieldManager.getProfileField(filter.getFieldID());
                if (!field.isFilterable()) {
                    addActionError("Field " + field.getName() + " is not filterable");
                }
                else {
                    if (!filter.isValid(field.getType(), getLocale())) {
                        addActionError(getText("people.filter.invalid"));
                    }
                }
            }
        }
    }

    private boolean getDefaultView() {
        boolean everyoneView = false;
        String defaultView = JiveGlobals.getJiveProperty(PeopleBrowseFilterGroupProvider.PEOPLE_SEARCH_VIEW_DEFAULT, VIEW_NEWEST);
        if (defaultView != null && !defaultView.equals(VIEW_SEARCH)) {
            everyoneView = true;
            if (defaultView.equals(VIEW_NEWEST) && StringUtils.isBlank(sort)) {
                setSort(PeopleBrowseFilterGroupProvider.SORT_DATE);
            }
            else if (defaultView.equals(VIEW_STATUS) && StringUtils.isBlank(sort)) {
                setSort(PeopleBrowseFilterGroupProvider.SORT_STATUS_LEVEL);
            }
            else if (defaultView.equals(VIEW_ALPHA) && StringUtils.isBlank(sort)) {
                setSort(((isFullNameDisplayed()) ? PeopleBrowseFilterGroupProvider.SORT_NAME : PeopleBrowseFilterGroupProvider.SORT_USERNAME));
            }
            else if (defaultView.equals(VIEW_ONLINE)) {
                onlineOnly = true;
            }
        }
        return everyoneView;
    }

    public Date getNewestMinDate() {
        if (newestMinDate == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DAY_OF_YEAR, -newestDayThreshold);
            newestMinDate = c.getTime();
        }
        return newestMinDate;
    }

    public String getQueryEncoded() {
        return queryEncoded;
    }

    public int getFacetTimeout() {
        return facetTimeout;
    }
    
    public Integer getBrand() {
        return brand;
    }

    public Integer getRegion() {
        return region;
    }

    public Integer getCountry() {
        return country;
    }

    public Integer getRole() {
        return role;
    }

    public Integer getJobTitle() {
        return jobTitle;
    }

    public String getFromSynchro() {
        return fromSynchro;
    }

    public void setFromSynchro(String fromSynchro) {
        this.fromSynchro = fromSynchro;
    }
	private String getEncodedString(String str)
	{
		return str.replaceAll(" ", "%20A");
	}
	
	private String getDecodedString(String str)
	{
		return str.replaceAll("%20A", " ");
	}
}
