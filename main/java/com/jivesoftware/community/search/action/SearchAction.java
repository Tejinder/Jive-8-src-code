/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.search.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.event.SearchEvent;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.community.Blog;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.TagObjectNotFoundException;
import com.jivesoftware.community.ThreadResultFilter;
import com.jivesoftware.community.TypeUIProvider;
import com.jivesoftware.community.action.HasMetaDescription;
import com.jivesoftware.community.action.HistoryActionSupport;
import com.jivesoftware.community.action.util.Pageable;
import com.jivesoftware.community.action.util.Paginator;
import com.jivesoftware.community.action.util.RenderUtils;
import com.jivesoftware.community.action.util.SimplePaginator;
import com.jivesoftware.community.eae.tile.type.TileStreamEntryObjectType;
import com.jivesoftware.community.entitlements.util.MaskProvider;
import com.jivesoftware.community.impl.EntitlementCheckHelper;
import com.jivesoftware.community.integration.tile.Tile;
import com.jivesoftware.community.integration.tile.TileResultFilter;
import com.jivesoftware.community.objecttype.ContentObjectType;
import com.jivesoftware.community.objecttype.FilteredIndexableType;
import com.jivesoftware.community.objecttype.IndexableType;
import com.jivesoftware.community.objecttype.JiveObjectType;
import com.jivesoftware.community.objecttype.VisibleType;
import com.jivesoftware.community.objecttype.impl.CommunityObjectType;
import com.jivesoftware.community.objecttype.impl.SocialGroupObjectType;
import com.jivesoftware.community.search.IndexInfoProvider;
import com.jivesoftware.community.search.ResultCountComponent;
import com.jivesoftware.community.search.SearchQueryCriteria;
import com.jivesoftware.community.search.SearchQueryCriteriaBuilder;
import com.jivesoftware.community.search.SearchQueryManager;
import com.jivesoftware.community.search.SearchQueryResult;
import com.jivesoftware.community.search.SearchQuerySettingsManager;
import com.jivesoftware.community.search.SearchResult;
import com.jivesoftware.community.search.SearchResultViewHelper;
import com.jivesoftware.community.search.SearchSettingsManager;
import com.jivesoftware.community.search.user.ProfileSearchCriteria;
import com.jivesoftware.community.search.user.ProfileSearchCriteriaBuilder;
import com.jivesoftware.community.search.user.ProfileSearchQueryManager;
import com.jivesoftware.community.search.user.ProfileSearchResult;
import com.jivesoftware.community.search.user.ProfileSearchResultViewHelper;
import com.jivesoftware.community.search.user.ProfileSearchSettingsManager;
import com.jivesoftware.community.search.view.admin.SearchLanguageBean;
import com.jivesoftware.community.tagset.TagSet;
import com.jivesoftware.community.tagset.TagSetManager;
import com.jivesoftware.community.user.rest.UserItemBean;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.community.web.struts.mapping.CommunityURLMapping;
import com.jivesoftware.community.web.struts.mapping.ProjectURLMapping;
import com.jivesoftware.community.web.struts.mapping.SocialGroupURLMapping;
import com.jivesoftware.eae.constants.ActivityConstants;
import com.jivesoftware.util.RelativeDateRange;
import com.jivesoftware.util.StringUtils;
import com.jivesoftware.util.spell.SpellChecker;
import com.jivesoftware.util.spell.SpellSession;
import com.opensymphony.xwork2.ActionContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.jivesoftware.community.impl.DocumentContentType;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.*;

/**
 * An Action to encapsulate all the logic of searching message content.
 */
public class SearchAction extends HistoryActionSupport implements Pageable, HasMetaDescription {

    protected static final Logger log = LogManager.getLogger(SearchAction.class.getName());

    public static final int DEFAULT_NUM_RESULTS = 15;
    public static final int[] RESULT_OPTIONS = {10, 15, 30};
    public static final RelativeDateRange DEFAULT_DATE_RANGE = RelativeDateRange.ALL;
    public static final RelativeDateRange[] DATE_RANGES = {RelativeDateRange.YESTERDAY, RelativeDateRange.LAST_7_DAYS,
            RelativeDateRange.LAST_30_DAYS, RelativeDateRange.LAST_YEAR};

    public static final String VIEW_CONTENT = "content";
    public static final String VIEW_PEOPLE = "people";
    public static final String VIEW_PLACES = "places";
    public static final String VIEW_STATUS_UPDATES = "updates";
    public static final String VIEW_DIRECT_MESSAGES = "messages";

    protected final int MAX_RESULTS = JiveGlobals.getJiveIntProperty("search.maxViewableResults", 500);

    // Parameters
    protected long containerID = -1;
    protected int containerType = -1;
    protected String userID;
    protected String containerName;
    protected boolean advanced = false;
    protected boolean spotlight = false;
    protected boolean spellSuggestOn = JiveGlobals.getJiveBooleanProperty("search.spellcheck.enabled", true);
    protected boolean displayPerThread = JiveGlobals.getJiveBooleanProperty("search.results.groupByThread", true);
    protected boolean showInfoDefault = JiveGlobals.getJiveBooleanProperty("search.wildcard.info.enabled", true);
    protected int numResults = DEFAULT_NUM_RESULTS;
    protected int peopleResultLimit = JiveGlobals.getJiveIntProperty("search.peopleResults.limit", 5);
    protected String dateRange;
    protected String q;
    protected String qenc;
    protected String correctedQ;
    protected int start = 0;
    protected List<JiveObjectType> resultObjectTypes;
    protected List<Long> tagSetIDs;
    protected String view = VIEW_CONTENT;
    protected boolean peopleNameOnly = false;
    protected boolean peopleFuzzy = JiveGlobals.getJiveBooleanProperty("people.search.fuzzy.enabled", false);

    // Other protected objects for searching
    protected JiveContainer container;
    protected UserItemBean searchedUser;
    protected SearchResult searchQueryResult;
    protected ProfileSearchResult profileSearchResult;
    protected List<SearchQueryResult> results;
    protected LinkedList<User> peopleResults;
    protected ProfileSearchResult peopleHits;
    protected Map<String, String> placeNameToUrls;
    protected boolean moreResultsAvailable = false;
    protected String partialQueryString;
    protected String correctedURLQ;
    protected String partialCorrectedQueryString;
    protected SearchQueryCriteria.Sort rankBy = SearchQueryCriteria.DefaultSort.RELEVANCE;
    protected SearchQueryCriteria.SortOrder orderBy = SearchQueryCriteria.SortOrder.DESCENDING;
    protected ProfileSearchQueryManager profileSearchQueryManager;
    protected ProfileSearchSettingsManager profileSearchSettingsManager;
    protected SearchQueryManager searchQueryManager;
    protected TagSetManager tagSetManager;
    protected SearchSettingsManager searchSettingsManager;
    protected SearchQuerySettingsManager searchQuerySettingsManager;
    protected SearchActionHelper searchActionHelper;
    protected SearchResultViewHelper searchResultViewHelper;
    protected ProfileSearchResultViewHelper profileSearchResultViewHelper;
    protected EntitlementCheckHelper entitlementCheckHelper;
    protected MaskProvider maskProvider;
    protected EventDispatcher eventDispatcher;
    protected String language;
    
    // Customizations start
    protected DocumentManager documentManager;
    protected List<String> docBrand;
    protected List<String> docRegion;
    protected List<String> docPeriod;
    protected List<String> docMethodology;
    protected List<Document> docResults;
    public static String GRAIL_BRAND_PROP = "grail.brand";
    public static String GRAIL_COUNTRY_PROP = "grail.country";
    public static String GRAIL_METHODOLOGY_PROP = "grail.methodology";
    public static String GRAIL_PERIOD_PROP = "grail.period.long";
    public static final String GRAIL_MONTH = "grail.month";
    public static final String GRAIL_YEAR = "grail.year";
    private String newSearch;
    private int count = 0;
    protected boolean searchForDocumentsOnly = false;
    protected boolean applyExtendedPropertyFilters = false;
    private JiveContainer rootContainer;
    protected String searchParam;
    
    protected String advanceSearch;
    protected String all_fields;
    protected String any_fields;
    protected String exact_fields;
    protected String none_fields;
    protected String tag_fields;
    protected String mrts;
    

    public void setMaskProvider(MaskProvider maskProvider) {
        this.maskProvider = maskProvider;
    }

    public void setEntitlementCheckHelper(EntitlementCheckHelper entitlementCheckHelper) {
        this.entitlementCheckHelper = entitlementCheckHelper;
    }

    public void setProfileSearchQueryManager(ProfileSearchQueryManager profileSearchQueryManager) {
        this.profileSearchQueryManager = profileSearchQueryManager;
    }

    public void setProfileSearchSettingsManager(ProfileSearchSettingsManager profileSearchSettingsManager) {
        this.profileSearchSettingsManager = profileSearchSettingsManager;
    }

    public void setSearchQueryManager(SearchQueryManager searchQueryManager) {
        this.searchQueryManager = searchQueryManager;
    }

    public void setSearchResultViewHelper(SearchResultViewHelper searchResultViewHelper) {
        this.searchResultViewHelper = searchResultViewHelper;
    }

    public void setProfileSearchResultViewHelper(ProfileSearchResultViewHelper profileSearchResultViewHelper) {
        this.profileSearchResultViewHelper = profileSearchResultViewHelper;
    }

    public void setTagSetManager(TagSetManager tagSetManager) {
        this.tagSetManager = tagSetManager;
    }

    public void setSearchSettingsManager(SearchSettingsManager searchSettingsManager) {
        this.searchSettingsManager = searchSettingsManager;
    }

    public void setSearchQuerySettingsManager(SearchQuerySettingsManager searchQuerySettingsManager) {
        this.searchQuerySettingsManager = searchQuerySettingsManager;
    }

    public void setJiveEventDispatcher(EventDispatcher jiveEventDispatcher) {
        this.eventDispatcher = jiveEventDispatcher;
    }

    public void setSearchActionHelper(SearchActionHelper searchActionHelper) {
        this.searchActionHelper = searchActionHelper;
    }

    public String getObjectTypeCode(int objectType) {
        JiveObjectType type = objectTypeManager.getObjectType(objectType);
        if (type != null) {
            return type.getCode();
        }
        return "all";
    }

    /**
     * Returns the container ID for the current search
     *
     * @return the container ID for the current search
     */
    public long getContainerID() {
        return containerID;
    }

    /**
     * Sets the container ID for the current search
     *
     * @param containerID containerID to set
     */
    public void setContainerID(long containerID) {
        this.containerID = containerID;
    }

    /**
     * Returns the container type for the current search
     *
     * @return the container ID for the current search
     */
    public int getContainerType() {
        return containerType;
    }

    /**
     * sets the container type for the current search
     *
     * @param containerType type of container to set
     */
    public void setContainerType(int containerType) {
        this.containerType = containerType;
    }

    public String getUserIDs() {
        return userID;
    }

    public void setUserIDs(String userID) {
        if (userID != null && !"".equals(userID.trim())) {
            this.userID = userID.trim();
        }
    }

    public boolean isSpellSuggestOn() {
        return spellSuggestOn;
    }

    public void setSpellSuggestOn(boolean spellSuggestOn) {
        this.spellSuggestOn = spellSuggestOn;
    }

    public boolean isDisplayPerThread() {
        return displayPerThread;
    }

    public void setDisplayPerThread(boolean displayPerThread) {
        this.displayPerThread = displayPerThread;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public boolean isSpotlight() {
        return spotlight;
    }

    public void setSpotlight(boolean spotlight) {
        this.spotlight = spotlight;
    }

    public boolean isShowInfoDefault() {
        return showInfoDefault;
    }

    public boolean isWildcardIgnored() {
        return searchQuerySettingsManager.isWildcardIgnored();
    }

    public boolean isSearchForDocumentsOnly() {
        return searchForDocumentsOnly;
    }

    public void setSearchForDocumentsOnly(boolean searchForDocumentsOnly) {
        this.searchForDocumentsOnly = searchForDocumentsOnly;
    }

    public boolean getApplyExtendedPropertyFilters() {
        return applyExtendedPropertyFilters;
    }

    public void setApplyExtendedPropertyFilters(boolean applyExtendedPropertyFilters) {
        this.applyExtendedPropertyFilters = applyExtendedPropertyFilters;
    }
    public boolean isUserShowInfo() {
        User user = getUser();
        if (!user.isAnonymous()) {
            String hideWildcard = user.getProperties().get("hide.search.wildcard.dialog");
            if (hideWildcard != null && hideWildcard.equals("true")) {
                return false;
            }
        }
        return true;
    }

    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public int getPeopleResultLimit() {
        return peopleResultLimit;
    }

    public void setPeopleResultLimit(int peopleResultLimit) {
        this.peopleResultLimit = peopleResultLimit;
    }

    /**
     * Returns the current method of ranking results.
     *
     * @return the current method of ranking results.
     */
    public String getRankBy() {
        if (rankBy != null) {
            return rankBy.getKey();
        }
        else {
            return SearchQueryCriteria.DefaultSort.RELEVANCE.getKey();
        }
    }

    public void setRankBy(String rankBy) {
        if (rankBy != null) {
            this.rankBy = SearchQueryCriteria.DefaultSort.getSort(rankBy);
        }
    }

    public int getOrderBy() {
        if (orderBy != null) {
            return orderBy.getKey();
        }
        else {
            return SearchQueryCriteria.SortOrder.DESCENDING.getKey();
        }
    }

    public void setOrderBy(int orderBy) {
        if (orderBy >= 0 && orderBy <= 1) {
            this.orderBy = SearchQueryCriteria.SortOrder.getSortOrder(orderBy);
        }
    }

    public String getDateRange() {
        if (dateRange == null) {
            return JiveGlobals.getJiveProperty("search.defaultdaterange", DEFAULT_DATE_RANGE.getID());
        }
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        if (!StringUtils.isBlank(q)) {
            if ("*".equals(q)) {
                this.q = null;
            }
            else {
                this.q = q;
            }
            // Don't lowercase the query - let the search engine deal with lowercasing as it sees fit.
            // Otherwise, issues such as CS-4397 can't be fixed.
//            else {
//                this.q = q.toLowerCase();
//            }
        }
        else {
            this.q = null;
        }
    }

    public String getQenc() {
        return qenc;
    }

    public void setQenc(String qenc) {
        if (qenc != null && !"".equals(qenc.trim())) {
            this.qenc = qenc;
        }
    }
    
    public List<String> getDocBrand() {
        return docBrand;
    }

    public void setDocBrand(List<String> docBrand) {
        this.docBrand = docBrand;
    }

    public DocumentManager getDocumentManager() {
        return documentManager;
    }

    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public List<String> getDocRegion() {
        return docRegion;
    }

    public void setDocRegion(List<String> docRegion) {
        this.docRegion = docRegion;
    }

    public List<String> getDocPeriod() {
        return docPeriod;
    }

    public void setDocPeriod(List<String> docPeriod) {
        this.docPeriod = docPeriod;
    }

    public List<String> getDocMethodology() {
        return docMethodology;
    }

    public void setDocMethodology(List<String> docMethodology) {
        this.docMethodology = docMethodology;
    }

    public List<Document> getDocResults() {
        return docResults;
    }

    public void setDocResults(List<Document> docResults) {
        this.docResults = docResults;
    }

    public String getNewSearch() {
        return newSearch;
    }

    public void setNewSearch(String newSearch) {
        this.newSearch = newSearch;
    }
    
    public String getCorrectedQ() {
        if (correctedQ == null) {
            getSuggestedQuery();
        }
        if (correctedQ != null) {
            //Fix for FORUMS-2934.
            return correctedQ.equals("") ? null : StringUtils.escapeHTMLTags(correctedQ);
        }
        else {
            return null;
        }
    }

    protected void getSuggestedQuery() {
        if (!spellSuggestOn || StringUtils.isBlank(q)) {
            return;
        }

        try {
            SpellSession spellSession = SpellChecker.createSession(q, this.getLocale().toString());
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
                            spellSession.ignore();
                        }
                    }
                    else if (result == SpellSession.DOUBLED_WORD) {
                        spellSession.delete();
                    }
                    else {
                        log.error("Unexpected spell checking result code:" + result);
                        spellSession.ignore();
                    }
                }
                correctedQ = foundAlternate ? spellSession.getText() : "";

                if (!StringUtils.isBlank(correctedQ)) {
                    // test the query to see if there would be any results for the suggestion
                    SearchResult result = getSearchQueryResult(correctedQ, false); // don't request highlighting
                    if (result != null) {
                        Iterator<JiveObject> iter = result.results(0, 2).iterator();
                        if (!iter.hasNext()) {
                            correctedQ = "";
                        }
                    }
                }
            }
            else {
                correctedQ = "";
            }
            correctedURLQ = correctedQ;
        }
        catch (Exception e) {
            log.warn("Got error searching for term '" + q + "' and trying to run spell check", e);
        }
    }

    public List<String> getResultTypes() {
        if (resultObjectTypes != null) {
            List<String> typeCodes = Lists.newArrayListWithExpectedSize(resultObjectTypes.size());
            for (JiveObjectType type : resultObjectTypes) {
                typeCodes.add(type.getCode());
            }
            return typeCodes;
        }

        return Collections.emptyList();
    }

    public void setResultTypes(List<String> resultTypes) {
        final List<JiveObjectType> objectTypeList = getIndexableTypes();
        this.resultObjectTypes = new ArrayList<JiveObjectType>();
        Map<String, String> objectTypesAdditionalMapping = new HashMap<>();
        for (String s : searchActionHelper.getContentTypes()) {
            int index = s.indexOf(':');
            if (index > 0) {
                objectTypesAdditionalMapping.put(s.substring(index + 1).trim().toLowerCase(),s.substring(0, index));
            }
        }
        for (JiveObjectType objectType : objectTypeList) {
            for (String typeCode : resultTypes) {

                //hack to work around mismatch between old param strings like "BLOG_POST" and current type codes like "blogpost"
                typeCode = typeCode.replace("_", "");
                if (objectType.getCode().equalsIgnoreCase(typeCode) ||
                        (objectTypesAdditionalMapping.containsKey(typeCode) &&
                         objectType.getCode().equalsIgnoreCase(objectTypesAdditionalMapping.get(typeCode)))) {
                    this.resultObjectTypes.add(objectType);
                }
            }
        }
    }

    public JiveContainer getContainer() {
        return container;
    }

    public void setContainer(JiveContainer container) {
        this.container = container;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    
    public String getContainerName() {
        return containerName;
    }
    
    public String getView() {
        return view;
    }

    public void setView(String view) {
        if (getUser().isAnonymous() && view.equals(VIEW_DIRECT_MESSAGES)) {
            this.view = VIEW_CONTENT;
        }
        else {
            this.view = view;
        }
    }

    public boolean isPeopleNameOnly() {
        return peopleNameOnly;
    }

    public void setPeopleNameOnly(boolean peopleNameOnly) {
        this.peopleNameOnly = peopleNameOnly;
    }

    public boolean isPeopleFuzzy() {
        return peopleFuzzy;
    }

    public String getContainerChooserLabel() {
        List<String> containers = new ArrayList<>();
        containers.add(getText("global.sgroup"));
        containers.add(getText("global.community"));
        if (projectManager.isFeatureEnabled()) {
            containers.add(getText("global.project"));
        }

        String delim = getText("global.comma") + " ";
        String penultimate = " " + getText("global.or") + " ";
        StringBuilder result = new StringBuilder(64);
        result.append(getText("search.form.location.partial.label")).append(" ");
        result.append(StringUtils.joinWithPenultimate(containers, delim, penultimate));
        return result.toString();
    }

    public void setPeopleFuzzy(boolean peopleFuzzy) {
        this.peopleFuzzy = peopleFuzzy;
    }

    public boolean isUserSearchEnabled() {
        return profileSearchSettingsManager.isSearchEnabled();
    }

    public List<Long> getTagSetIDs() {
        return tagSetIDs;
    }

    public void setTagSetIDs(List<Long> tagSetIDs) {
        this.tagSetIDs = tagSetIDs;
    }

    // From the Pageable interface
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    // From the Pageable interface
    public int getTotalItemCount() {
        return getResultCount();
    }

    // From the Pageable interface
    public ThreadResultFilter getResultFilter() {
        ThreadResultFilter filter = new ThreadResultFilter();
        filter.setStartIndex(getStart());
        filter.setNumResults(getNumResults());
        return filter;
    }

    // Other methods for parameters //

    /**
     * Returns the number of the first result on a result page. For instance, this will return 1 for the first page and
     * 16 on the second (if there are 15 results per page).
     *
     * @return the number of the first result on a result page.
     */
    public int getResultStart() {
        return getStart() + 1;
    }

    public RelativeDateRange getDefaultDateRange() {
        return DEFAULT_DATE_RANGE;
    }

    /**
     * Returns a list of the possible date ranges allowed in a search.
     *
     * @return a list of the possible date ranges allowed in a search.
     */
    public RelativeDateRange[] getDateRanges() {
        return DATE_RANGES;
    }

    /**
     * Returns the user used to filter searches or null if none was specified.
     *
     * @return the user used to filter searches or null if none was specified.
     */
    public UserItemBean getSearchedUser() {
        return searchedUser;
    }

    /**
     * Returns a list of the result count options.
     *
     * @return a list of the result count options.
     */
    public int[] getNumResultOptions() {
        return RESULT_OPTIONS;
    }

    public Paginator getNewPaginator() {
        boolean moreResultsAvailable = isMoreResultsAvailable();
        int numResults1 = getNumResults();
        return new SimplePaginator(this, numResults1, moreResultsAvailable);
    }

    public boolean isMoreResultsAvailable() {
        // see if we can load n+1 objects
        return moreResultsAvailable;
    }

    /**
     * Creates a partial query string which can be used to reconstruct the parameter list that executed a search. The
     * query string will not start with a ? or &amp;. <p>
     * <p/>
     * Example: q=foo+bar&amp;objID=3&amp;userID=joe <p>
     * <p/>
     * Only parameters actually used or different from default values will be returned.
     *
     * @return a partial query string containing parameters of this search.
     */
    public String getSearchParams() {
        if (partialQueryString == null) {
            StringBuffer buf = new StringBuffer();
            // query string
            if (qenc != null) {
                try {
                    buf.append("qenc=").append(URLEncoder.encode(qenc, JiveGlobals.getCharacterEncoding()))
                            .append("&");
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            else if (q != null) {
                try {
                    buf.append("q=");
                    buf.append(URLEncoder.encode(q, JiveGlobals.getCharacterEncoding()));
                    buf.append("&");
                }
                catch (UnsupportedEncodingException e) { /* should never happen - need assert :( */ }
            }
            if (view != null && !view.equals(VIEW_CONTENT)) {
                buf.append("view=").append(view).append("&");
                if (view.equals(VIEW_PEOPLE)) {
                    buf.append("peopleNameOnly=").append(peopleNameOnly).append("&");
                    buf.append("peopleFuzzy=").append(peopleFuzzy).append("&");
                }
            }
            // add result types
            if (resultObjectTypes != null && resultObjectTypes.size() > 0) {
                for (JiveObjectType type : resultObjectTypes) {
                    buf.append("resultTypes=").append(type.getCode()).append("&");
                }
            }
            if (tagSetIDs != null && tagSetIDs.size() > 0) {
                for (long tagSetID : tagSetIDs) {
                    buf.append("tagSetIDs=").append(tagSetID).append("&");
                }
            }
            // container
            if (container != null) {
                buf.append("containerType=").append(container.getObjectType()).append("&container=")
                        .append(container.getID()).append("&");
            }
            // date range
            if (getDateRange() != null) {
                try {
                    buf.append("dateRange=")
                            .append(URLEncoder.encode(getDateRange(), JiveGlobals.getCharacterEncoding()))
                            .append("&");
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // user id
            if (searchedUser != null) {
                buf.append("userID=").append(searchedUser.getID()).append("&");
            }
            // num results
            if (numResults != DEFAULT_NUM_RESULTS) {
                buf.append("numResults=").append(numResults).append("&");
            }
            // advanced:
            if (advanced) {
                buf.append("advanced=").append(advanced).append("&");
            }
            
            if( (docBrand != null && docBrand.size() > 0 && !docBrand.get(0).equalsIgnoreCase("NA")) ||
                    (docRegion != null && docRegion.size() > 0 && !docRegion.get(0).equalsIgnoreCase("NA")) ||
                    (docMethodology != null && docMethodology.size() > 0 && !docMethodology.get(0).equalsIgnoreCase("NA")) ||
                    (docPeriod != null && docPeriod.size() == 4 && !docPeriod.get(0).equalsIgnoreCase("NA")
                            && !docPeriod.get(1).equalsIgnoreCase("NA") && !docPeriod.get(2).equalsIgnoreCase("NA") && !docPeriod.get(3).equalsIgnoreCase("NA"))
                    ) {
                // rankby
                buf.append("newSearch=true").append("&");
                buf.append("searchForDocumentsOnly=true").append("&");
                buf.append("applyExtendedPropertyFilters=true").append("&");
                buf.append("resultTypes=document").append("&");
            } else {
                buf.append("searchForDocumentsOnly=false").append("&");
                buf.append("applyExtendedPropertyFilters=false").append("&");
                buf.append("resultTypes=all").append("&");
            }

            if(docBrand != null && docBrand.size() > 0) {
                buf.append("docBrand=").append(docBrand.get(0)).append("&");
            } else {
                buf.append("docBrand=NA").append("&");
            }

            if(docRegion != null && docRegion.size() > 0) {
                buf.append("docRegion=").append(docRegion.get(0)).append("&");
            } else {
                buf.append("docRegion=NA").append("&");
            }

            if(docMethodology != null && docMethodology.size() > 0) {
                buf.append("docMethodology=").append(docMethodology.get(0)).append("&");
            } else {
                buf.append("docMethodology=NA").append("&");
            }

            if(docPeriod != null && docPeriod.size() == 4) {
                buf.append("docPeriod=").append(docPeriod.get(0)).append("&");
                buf.append("docPeriod=").append(docPeriod.get(1)).append("&");
                buf.append("docPeriod=").append(docPeriod.get(2)).append("&");
                buf.append("docPeriod=").append(docPeriod.get(3)).append("&");
            } else {
                buf.append("docPeriod=NA").append("&");
                buf.append("docPeriod=NA").append("&");
                buf.append("docPeriod=NA").append("&");
                buf.append("docPeriod=NA").append("&");
            }


            // rankby
            buf.append("rankBy=").append(getRankBy());

            partialQueryString = buf.toString();
            if (partialQueryString != null && partialQueryString.length() > 0) {
                if (partialQueryString.charAt(partialQueryString.length() - 1) == '&') {
                    partialQueryString = partialQueryString.substring(0, partialQueryString.length() - 1);
                }
            }
        }
        return partialQueryString;
    }

    /**
     * Creates a partial query string which can be used to reconstruct the parameter list that executed a search. The
     * query string will not start with a ? or &amp;. <p>
     * <p/>
     * Example: q=foo+bar&amp;objID=3&amp;userID=joe <p>
     * <p/>
     * Only parameters actually used or different from default values will be returned.
     *
     * @return a partial query string containing parameters of this search.
     */
    public String getCorrectedSearchParams() {
        if (partialCorrectedQueryString == null) {
            StringBuffer buf = new StringBuffer();
            // query string
            if (qenc != null) {
                buf.append("qenc=").append(qenc).append("&");
            }
            else if (q != null) {
                buf.append("q=");
                buf.append(StringUtils.URLDecode(correctedURLQ));
                buf.append("&");
            }
            if (view != null && !view.equals(VIEW_CONTENT)) {
                buf.append("view=").append(view).append("&");
                if (view.equals(VIEW_PEOPLE)) {
                    buf.append("peopleNameOnly=").append(peopleNameOnly).append("&");
                    buf.append("peopleFuzzy=").append(peopleFuzzy).append("&");
                }
            }
            if (resultObjectTypes != null && resultObjectTypes.size() > 0) {
                for (JiveObjectType type : resultObjectTypes) {
                    buf.append("resultTypes=").append(type.getCode()).append("&");
                }
            }
            if (tagSetIDs != null && tagSetIDs.size() > 0) {
                for (long tagSetID : tagSetIDs) {
                    buf.append("tagSetIDs=").append(tagSetID).append("&");
                }
            }
            // container
            if (container != null) {
                buf.append("containerType=").append(container.getObjectType()).append("&container=")
                        .append(container.getID()).append("&");
            }
            // date range
            if (getDateRange() != null && !"all".equals(getDateRange())) {
                buf.append("dateRange=").append(getDateRange()).append("&");
            }
            // user id
            if (searchedUser != null) {
                buf.append("userID=").append(searchedUser.getID()).append("&");
            }
            // num results
            if (numResults != DEFAULT_NUM_RESULTS) {
                buf.append("numResults=").append(getNumResults()).append("&");
            }
            
            if( (docBrand != null && docBrand.size() > 0 && !docBrand.get(0).equalsIgnoreCase("NA")) ||
                    (docRegion != null && docRegion.size() > 0 && !docRegion.get(0).equalsIgnoreCase("NA")) ||
                    (docMethodology != null && docMethodology.size() > 0 && !docMethodology.get(0).equalsIgnoreCase("NA")) ||
                    (docPeriod != null && docPeriod.size() == 4 && !docPeriod.get(0).equalsIgnoreCase("NA")
                            && !docPeriod.get(1).equalsIgnoreCase("NA") && !docPeriod.get(2).equalsIgnoreCase("NA") && !docPeriod.get(3).equalsIgnoreCase("NA"))
                    ) {
                // rankby
                buf.append("newSearch=true").append("&");
                buf.append("searchForDocumentsOnly=true").append("&");
                buf.append("applyExtendedPropertyFilters=true").append("&");
                buf.append("resultTypes=document").append("&");
            } else {
                buf.append("searchForDocumentsOnly=false").append("&");
                buf.append("applyExtendedPropertyFilters=false").append("&");
                buf.append("resultTypes=all").append("&");
            }

            if(docBrand != null && docBrand.size() > 0) {
                buf.append("docBrand=").append(docBrand.get(0)).append("&");
            } else {
                buf.append("docBrand=NA").append("&");
            }

            if(docRegion != null && docRegion.size() > 0) {
                buf.append("docRegion=").append(docRegion.get(0)).append("&");
            } else {
                buf.append("docRegion=NA").append("&");
            }

            if(docMethodology != null && docMethodology.size() > 0) {
                buf.append("docMethodology=").append(docMethodology.get(0)).append("&");
            } else {
                buf.append("docMethodology=NA").append("&");
            }

            if(docPeriod != null && docPeriod.size() == 4) {
                buf.append("docPeriod=").append(docPeriod.get(0)).append("&");
                buf.append("docPeriod=").append(docPeriod.get(1)).append("&");
                buf.append("docPeriod=").append(docPeriod.get(2)).append("&");
                buf.append("docPeriod=").append(docPeriod.get(3)).append("&");
            } else {
                buf.append("docPeriod=NA").append("&");
                buf.append("docPeriod=NA").append("&");
                buf.append("docPeriod=NA").append("&");
                buf.append("docPeriod=NA").append("&");
            }

            
            partialCorrectedQueryString = buf.toString();
            if (partialCorrectedQueryString != null && partialCorrectedQueryString.length() > 0) {
                if (partialCorrectedQueryString.charAt(partialCorrectedQueryString.length() - 1) == '&') {
                    partialCorrectedQueryString = partialCorrectedQueryString
                            .substring(0, partialCorrectedQueryString.length() - 1);
                }
            }
        }
        return partialCorrectedQueryString;
    }

    // Search methods //

    /**
     * Returns an Iterator of QueryResult objects which represent the results of a search, or an empty iterator if there
     * were no results.
     *
     * @return an Iterator of search results (QueryResult objects) or an empty iterator if no results were found.
     */
    public Iterator getResults() {
        if (ActionContext.getContext().getActionInvocation().getProxy().getConfig().getMethodName() != null) {
            return Collections.emptyList().iterator();
        }
        if (q == null && searchedUser == null && !searchForDocumentsOnly) {
            return Collections.EMPTY_LIST.iterator();
        }

        return (results != null) ? results.iterator() : Collections.emptyList().iterator();
    }

    /**
     * Returns an Iterator of QueryResult objects which represent the results of a search, or an empty iterator if there
     * were no results.
     *
     * @return an Iterator of search results (QueryResult objects) or an empty iterator if no results were found.
     */
    public Iterator getPeopleResults() {
        if (ActionContext.getContext().getActionInvocation().getProxy().getConfig().getMethodName() != null) {
            return Collections.emptyList().iterator();
        }
        if (q == null) {
            return Collections.EMPTY_LIST.iterator();
        }

        return (peopleResults != null) ? peopleResults.iterator() : Collections.emptyList().iterator();
    }

    public List<Integer> getPlaceTypes() {
        List<Integer> typeIDs = new ArrayList<Integer>();
        for (JiveObjectType type : getIndexableTypes(true)) {
            typeIDs.add(type.getID());
        }
        return typeIDs;
    }

    /**
     * Returns the total number of results for this search.
     *
     * @return the total number of results for this search.
     */
    public int getResultCount() {
        if (results == null) {
            return 0;
        }

        return results.size();
    }

    List<Integer> getResultTypeList() {
        return getResultTypeList(false);
    }

    List<Integer> getResultTypeList(boolean places) {
        List<Integer> resultTypeIDs = new ArrayList<Integer>();
        List<JiveObjectType> actualTypes = (resultObjectTypes == null || resultObjectTypes.isEmpty())
                                           ? getIndexableTypes(places) : resultObjectTypes;

        for (JiveObjectType type : actualTypes) {
            if (type instanceof IndexableType) {
                IndexInfoProvider iip = ((IndexableType) type).getIndexInfoProvider();
                if (iip.getCanViewType()) {
                    resultTypeIDs.add(type.getID());
                }
            }
        }

        return resultTypeIDs;
    }

    private List<JiveObjectType> getIndexableTypes(boolean places) {
        List<JiveObjectType> types = new ArrayList<JiveObjectType>();
        for (JiveObjectType type : getIndexableTypes()) {
            if (places && searchActionHelper.isContainerType(type)) {
                types.add(type);

            }
            else if (!places && !searchActionHelper.isNotSearchableContentType(type)) {
                types.add(type);
            }
        }

        return types;
    }

    public String getRenderedHighlightedText(EntityDescriptor entity, String highlightedString) {
        if (StringUtils.isBlank(highlightedString)) {
            return "";
        }

        // Load the actual object because there are some casting assumptions made in the rendering code which a simple
        // EntityDescriptor will not work with
        JiveObject jiveObject = null;
        try {
            jiveObject = jiveObjectLoader.getJiveObject(entity);
        }
        catch (NotFoundException e) {
            log.debug("Could not load object for entity " + entity, e);
            return "";
        }

        try {
            return RenderUtils.renderToHtml(jiveObject, getGlobalRenderManager(), highlightedString);
        }
        catch (Exception e) {
            log.debug("Error highlighting text", e);
        }

        return "";
    }

//    /**
//     * Returns a subject and summary with search words highlighted appropriate to the search query string. Terms are
//     * highlighted with &lt;span class="jive-hilite">term&lt;/span>
//     *
//     * @param result the QueryResult to highlight
//     * @return an array of highlighted text with the subject being the first element and a summary of the main text as
//     *         the second element.
//     */
//    public Map<IndexField, String> getHighlightedText(SearchQueryResult result) {
//        Map<IndexField, String> highlights = new HashMap<IndexField, String>();
//        if (result == null) {
//            highlights.put(IndexField.subject, "");
//            highlights.put(IndexField.body, "");
//            return highlights;
//        }
//
//        if (getSearchQueryResult() != null) {
//            highlights = result.getHighlights();
//            for (Map.Entry<IndexField, String> entry : highlights.entrySet()) {
//                try {
//                    if (StringUtils.isBlank(entry.getValue())) {
//                        continue;
//                    }
//                    String filteredValue = RenderUtils.renderToHtml(result.getEntityDescriptor(),
//                            getGlobalRenderManager(), entry.getValue());
//                    entry.setValue(filteredValue);
//                }
//                catch (Exception e) {
//                    log.debug("Error highlighting text", e);
//                }
//            }
//        }
//        else {
//            JiveObject jiveObject = result.getEntityDescriptor();
//            if (jiveObject != null && jiveObject instanceof JiveContentObject) {
//                highlights.put(IndexField.subject, renderSubjectToText((JiveContentObject) jiveObject));
//            }
//        }
//
//        return highlights;
//    }

    public boolean isSearchEnabled() {
        return searchSettingsManager.isSearchEnabled();
    }

    public String getIconCss(int objectType) {
        JiveObjectType type = objectTypeManager.getObjectType(objectType);
        if (type instanceof ContentObjectType) {
            TypeUIProvider provider = ((ContentObjectType) type).getTypeUIProvider();
            if (provider != null) {
                return provider.getIconGenerator().getIcon(true, 0);
            }
        }

        return "";
    }

    // Webwork methods //

    public String input() {
        return doDefault();
    }

    public String doDefault() {
        if (!isSearchEnabled()) {
            addActionMessage(getText("search.err.func_disabled.text"));
            return ERROR;
        }
        if (!loadJiveObjects()) {
            return ERROR;
        }

        return INPUT;
    }

    public void executeQueries() {
        results = new ArrayList<SearchQueryResult>();

        // Need to protect ourselves from an attempt to go too far into the search results because the further
        // we move into the results, the more performance penalty we will pay
        boolean maxResultsHit = false;
        int resultsSize = getNumResults();
        if (getStart() < MAX_RESULTS && getStart() + resultsSize >= MAX_RESULTS) {
            resultsSize = MAX_RESULTS - getStart();
            maxResultsHit = true;
        }
        else if (getStart() >= MAX_RESULTS) {
            int totalPages = (MAX_RESULTS / resultsSize);
            if (MAX_RESULTS % resultsSize > 0) {
                totalPages++;
            }

            setStart((totalPages - 1) * resultsSize);
            resultsSize = MAX_RESULTS - ((totalPages - 1) * resultsSize);
            maxResultsHit = true;
        }

        int c = 0;
        SearchResult sr = getSearchQueryResult();
        if (sr != null) {
            Iterator<SearchQueryResult> resultsIter = searchResultViewHelper
                    .prepareViewBeans(sr, getStart(), resultsSize + 1).iterator();
            while (resultsIter.hasNext()) {
                results.add(resultsIter.next());
                c++;
                if (c >= resultsSize) {
                    break;
                }
            }
            moreResultsAvailable = !maxResultsHit && resultsIter.hasNext();
        }
    }

    public String execute() {
        if (!JiveGlobals.getJiveBooleanProperty("search.enabled", true)) {
            addActionMessage(getText("search.err.func_disabled.text"));
            return ERROR;
        }
        if (!loadJiveObjects()) {
            return ERROR;
        } else if(view != null && view.equals(VIEW_CONTENT) && applyExtendedPropertyFilters) { // Error check the query string
            // Check if at least one of document extended property filter selected ot not, other wise throw exception
            if (StringUtils.isBlank(q) && resultObjectTypes != null && resultObjectTypes.size() == 1) {
                for (JiveObjectType objType : resultObjectTypes) {
                    if (objType instanceof DocumentContentType) {
                        LOG.info("Query string is empty. If search results is only Documents the continue searching...");
                        if ( docBrand != null && docBrand.contains("NA")
                                && (docRegion != null && docRegion.contains("NA"))
                                && (docPeriod != null && docPeriod.contains("NA"))
                                && (docMethodology != null && docMethodology.contains("NA"))){
                            docResults = new ArrayList<Document>();
                            results = null;
                            addActionError("Please enter at least one search term or select from Brand, Country, Methodology, Period");
                            return INPUT;
                        } else {
                            int j = 0;
                            for(int i = 0 ; i< docPeriod.size() ; i++) {
                                if ( docPeriod.get(i).equalsIgnoreCase("NA")){
                                    ++j;
                                }
                            }

                            if( j > 0 && j < 4) {
                                docResults = new ArrayList<Document>();
                                results = null;
                                addActionError("Please choose valid  Period");
                                return INPUT;
                            }
                        }

                    }
                }
            }

            if(docPeriod != null && docPeriod.contains("NA")) {
                docPeriod = null;
            }
        }
        // Error check the query string
        else if (StringUtils.isBlank(q)) {
            return INPUT;
        }

        Community rootCommunity = communityManager.getRootCommunity();
        if (containerType == rootCommunity.getObjectType() && containerID == rootCommunity.getID()) {
            containerID = -1;
            containerType = -1;
        }

        if (view.equals(VIEW_PEOPLE)) {
            //todo: potentially need to set numResults and start???
            ProfileSearchResult sr = getProfileSearchResult();
            if(sr != null) {
		        peopleResults = Lists.newLinkedList(profileSearchResultViewHelper.toViewBeans(sr.results()));
		        eventDispatcher.fire(new SearchEvent(SearchEvent.Type.SEARCH_USER, sr.getCriteria()));
            }
        }
        else {
            handleContainerFilter();
            executeQueries();
            eventDispatcher
                    .fire(new SearchEvent(SearchEvent.Type.SEARCH_CONTENT, getSearchQueryResult().getCriteria()));
        }

        return SUCCESS;
    }

    //Handles the case where the user submitted the search form without choosing a selection from container autocomplete

    protected void handleContainerFilter() {
        if (container == null && containerName != null && containerName.length() > 0) {
            SearchQueryCriteria criteria = new SearchQueryCriteriaBuilder(containerName)
                    .setObjectTypes(Sets.newHashSet(getPlaceTypes())).build();
            Iterator<JiveObject> containerResult = searchQueryManager.executeQuery(criteria).results(0, 1).iterator();
            if (containerResult.hasNext()) {
                container = (JiveContainer) containerResult.next();
            }
        }
    }

    protected ProfileSearchResult getProfileSearchResult() {
        if (profileSearchResult != null) {
            return profileSearchResult;
        }
        else if (q == null) {
            return null;
        }

        if (qenc != null) {
            q = new String(StringUtils.decodeHex(qenc));
        }

        profileSearchResult = getProfileSearchResult(q);
        return profileSearchResult;
    }

    protected ProfileSearchResult getProfileSearchResult(String q) {
        if (q == null) {
            return null;
        }

        boolean useUsername = !peopleNameOnly && JiveGlobals
                .getJiveBooleanProperty("jive.peopleAction.search.username.enabled", true);
        boolean useName = true;
        boolean useFuzzyName = isPeopleFuzzy();
        boolean useEmail = !peopleNameOnly;
        boolean useProfile = !peopleNameOnly;

        ProfileSearchCriteriaBuilder criteriaBuilder = new ProfileSearchCriteriaBuilder(getUser().getID(), q)
                .setSearchUsername(useUsername).setSearchName(useName).setSearchNamePhonetically(useFuzzyName)
                .setSearchEmail(useEmail).setSearchProfile(useProfile).setReturnDisabledUsers(false)
                .setReturnExternalUsers(false).setReturnOnlineUsers(false).setUserLocale(getLocale());

        criteriaBuilder.setStart(start);
        criteriaBuilder.setRange(getNumResults());

        ProfileSearchCriteria criteria = criteriaBuilder.build();
        ProfileSearchResult sr = profileSearchQueryManager.executeSearch(criteria);
        if (sr != null) {
            ResultCountComponent count = sr.getComponent(ResultCountComponent.class);
            if (count != null) {
                moreResultsAvailable = count.getTotalCount() > start + getNumResults();
            }
        }

        return sr;
    }

    // Utility methods //

    public String getRSSSearchQuery() {
        return JiveGlobals.getJiveProperty("jiveURL") + "/community/feeds/search?" + getSearchParams();
    }

    public boolean isTaskSearchEnabled() {
        return JiveGlobals.getJiveBooleanProperty("task.search.enabled", false);
    }

    public boolean isDocumentCommentSearchEnabled() {
        return JiveGlobals.getJiveBooleanProperty("document.searchComments.enabled", true);
    }

    public boolean isBlogPostCommentSearchEnabled() {
        return JiveGlobals.getJiveBooleanProperty("blog.searchComments.enabled", true);
    }

    protected SearchResult getSearchQueryResult() {
        if (searchQueryResult != null) {
            return searchQueryResult;
        }
        else if (!applyExtendedPropertyFilters && q == null && searchedUser == null) {
            return null;
        }

        if (qenc != null) {
            q = new String(StringUtils.decodeHex(qenc));
        }

        searchQueryResult = getSearchQueryResult(q);
        return searchQueryResult;
    }

    protected SearchResult getSearchQueryResult(String query) {
        // have to leave the default as true for backwards compatibility
        return getSearchQueryResult(query, true);
    }

    protected SearchResult getSearchQueryResult(String query, boolean doHighlighting) {
        if (!applyExtendedPropertyFilters && query == null && searchedUser == null) {
            return null;
        }

        SearchQueryCriteriaBuilder criteria = new SearchQueryCriteriaBuilder(query)
                .setLanguage(getLanguage())
                .setPerformHighlighting(doHighlighting)
                .setTimeZone(getTimeZone())
                .setLocale(getLocale());

        if(applyExtendedPropertyFilters) {
            criteria.setDocumentsOnly(searchForDocumentsOnly);
            criteria.setApplyExtendedPropertyFilters(applyExtendedPropertyFilters);
            List<String> props = new ArrayList<String>();
            if(docBrand != null && !docBrand.isEmpty()
                    && !docBrand.get(0).equalsIgnoreCase("NA")) {
                props.add(docBrand.get(0).toString());
            }

            if(docRegion != null && !docRegion.isEmpty()
                    && !docRegion.get(0).equalsIgnoreCase("NA")) {
                props.add(docRegion.get(0).toString());
            }

            if(docMethodology != null && !docMethodology.isEmpty()
                    && !docMethodology.get(0).equalsIgnoreCase("NA")) {
                props.add(docMethodology.get(0).toString());
            }
            criteria.setExtendedProperties(props);

            if(docPeriod != null && !docPeriod.isEmpty()) {
                criteria.setDateRange(docPeriod);
            }

        }

        // Filter on a user
        if (searchedUser != null) {
            criteria.setUserID(Long.parseLong(searchedUser.getID()));
        }
        // Set the dates:
        if (getDateRange() != null && !"all".equals(getDateRange())) {
            RelativeDateRange range = null;
            for (RelativeDateRange dateRange : DATE_RANGES) {
                if (dateRange.getID().equals(getDateRange())) {
                    range = dateRange;
                    break;
                }
            }
            if (range != null) {
                criteria.setAfterDate(range.getStartDate(new Date(), getTimeZone(), getLocale()));
            }
        }

        // set the sort type
        criteria.setSort(rankBy);

        if (view.equals(VIEW_STATUS_UPDATES)) {
            Set<Integer> objtypes = new HashSet<Integer>(1);
            objtypes.add(JiveConstants.WALL_ENTRY);
            criteria.setObjectTypes(objtypes);
        }
        else if (view.equals(VIEW_DIRECT_MESSAGES)) {
            Set<Integer> objtypes = new HashSet<Integer>(1);
            objtypes.add(ActivityConstants.DIRECT_MESSAGE);
            criteria.setObjectTypes(objtypes);
        }
        else if (view.equals(VIEW_PLACES)) {
            criteria.setObjectTypes(new HashSet<Integer>(getResultTypeList(true)));
        }
        else {
            criteria.setObjectTypes(new HashSet<Integer>(getResultTypeList(false)));

            // add tag set filters
            for (TagSet tagSet : loadTagSets()) {
                criteria.addTagSetID(tagSet.getID());
            }

            if (displayPerThread) {
                criteria.collapseParents();
            }
            if (rankBy == SearchQueryCriteria.DefaultSort.SUBJECT) {
                criteria.setSortOrder(SearchQueryCriteria.SortOrder.ASCENDING);
            }
            if (rankBy == SearchQueryCriteria.DefaultSort.MODIFICATION_DATE) {
                criteria.setSortOrder(orderBy);
            }

            // Initialize the query object.
            if (container != null && !container.equals(communityManager.getRootCommunity())) {
                EntityDescriptor containerDescriptor = new EntityDescriptor(container);
                if (container.getContentTypes().contains(objectTypeManager.getObjectType(JiveConstants.BLOG))) {
                    Blog blog;
                    try {
                        blog = blogManager.getBlog(container);
                    }
                    catch (UnauthorizedException ue) {
                        blog = null;
                    }
                    if (blog != null) {
                        EntityDescriptor containerBlogDescriptor = new EntityDescriptor(blog);
                        criteria.setContainers(Sets.newHashSet(containerDescriptor, containerBlogDescriptor));
                    }
                    else {
                        criteria.setContainers(Sets.newHashSet(containerDescriptor));
                    }
                }
            }

            if (container == null && containerID != -1 && containerType != -1) {
                try {
                    container = jiveObjectLoader.getJiveContainer(containerType, containerID);
                }
                catch (NotFoundException e) {
                    log.debug("Could not load container with type: " + containerType + " and ID: " + containerID);
                }
                EntityDescriptor containerDescriptor = new EntityDescriptor(containerType, containerID);
                Blog blog;
                try {
                    blog = blogManager.getBlog(container);
                }
                catch (UnauthorizedException ue) {
                    blog = null;
                }
                if (container != null
                        && container.getContentTypes().contains(objectTypeManager.getObjectType(JiveConstants.BLOG))
                        && blog != null)
                {
                    EntityDescriptor containerBlogDescriptor = new EntityDescriptor(blog);
                    criteria.setContainers(Sets.newHashSet(containerDescriptor, containerBlogDescriptor));
                }
                else {
                    criteria.setContainers(Sets.newHashSet(containerDescriptor));
                }
            }
        }

        return searchQueryManager.executeQuery(criteria.build());
    }

    protected boolean loadJiveObjects() {
        boolean success = true;
        // Load the user
        if (userID != null) {
            User su = null;
            try {
                su = userManager.getUser(Long.parseLong(userID));
            }
            catch (Exception ignored) {
                try {
                    su = userManager.getUser(userID);
                }
                catch (Exception ignored2) {
                    addFieldError("userID", getText("search.err.failedToFindUsr.text"));
                }
            }
            if (su != null) {
                searchedUser = toUserBean(su);
            }
        }
        return success;
    }

    protected List<TagSet> loadTagSets() {
        if (tagSetIDs != null && tagSetIDs.size() > 0) {
            List<TagSet> tagSets = new ArrayList<TagSet>(tagSetIDs.size());
            for (long tagSetID : tagSetIDs) {
                TagSet tagSet;
                try {
                    tagSet = tagSetManager.getTagSet(tagSetID);
                    tagSets.add(tagSet);
                }
                catch (TagObjectNotFoundException e) {
                    log.debug("Could not load tag set with ID: " + tagSetID);
                }
            }
            return tagSets;
        }

        return Collections.emptyList();
    }

    public String getSubject(SearchQueryResult result) {
        JiveObject jiveObject = result.getJiveObject();
        if (jiveObject instanceof JiveContentObject) {
            return ((JiveContentObject) jiveObject).getSubject();
        }
        else if (jiveObject instanceof JiveContainer) {
            return ((JiveContainer) jiveObject).getName();
        }
        else if (jiveObject instanceof User) {
            return SkinUtils.getUserDisplayName(jiveObject.getID());
        }
        return "";
    }

    public List<JiveObjectType> getIndexableTypes() {
        final Set<IndexableType> objectTypesByType = objectTypeManager.getObjectTypesByType(IndexableType.class);
        List<JiveObjectType> types = new ArrayList<JiveObjectType>();
        for (JiveObjectType type : objectTypesByType) {
            if (type.isEnabled()) {
                types.add(type);
            }
        }
        return types;
    }

    public boolean isTypeEnabled(String typeCode) {
        JiveObjectType type = objectTypeManager.getObjectType(typeCode);
        if (type == null) {
            return false;
        }
        else if (type instanceof CommunityObjectType) {
            return type.isEnabled() && communityManager.isCommunityFeatureVisible();
        }
        else if (type instanceof SocialGroupObjectType) {
            return type.isEnabled()
                    && (this.getAuthenticationProvider().getJiveUser().isPartner() || entitlementCheckHelper.isEntitled(
                    this.getAuthenticationProvider().getJiveUser(),
                    entitlementCheckHelper.getEntitledContainer(null, JiveConstants.SOCIAL_GROUP),
                    JiveConstants.SOCIAL_GROUP,
                    maskProvider.getViewMask())
            );
        }
        else if (type instanceof TileStreamEntryObjectType) {
            TileResultFilter trf = new TileResultFilter();
            trf.setActivityTiles(true);
            Iterable<Tile> tiles = tileManager.get(trf);
            for (Tile tile : tiles) { return true; }
            return false;
        }
        else {
            return type.isEnabled();
        }
    }

    public boolean isResultTypeThresholdExceeded() {
        return objectTypeManager.getObjectTypesByType(FilteredIndexableType.class).size() > JiveGlobals
                .getJiveIntProperty("search.resultFilter.ui.threshold", 8);
    }

    public Map<String, String> getPlaceTypeUrls() {
        if (placeNameToUrls == null) {
            placeNameToUrls = new LinkedHashMap<String, String>();
            String spaceKey = ((VisibleType) objectTypeManager.getObjectType(JiveConstants.COMMUNITY))
                    .getTypeUIProvider().getContentTypeFeatureName(getLocale());
            String projectKey = ((VisibleType) objectTypeManager.getObjectType(JiveConstants.PROJECT))
                    .getTypeUIProvider().getContentTypeFeatureName(getLocale());
            String groupKey = ((VisibleType) objectTypeManager.getObjectType(JiveConstants.SOCIAL_GROUP))
                    .getTypeUIProvider().getContentTypeFeatureName(getLocale());
            // place names should not be lower cased: apparently nouns are always uppercased in German, CS-20685
            placeNameToUrls.put(spaceKey, CommunityURLMapping.COMMUNITY_URL_PREFIX);
            placeNameToUrls.put(projectKey, ProjectURLMapping.PROJECT_URL_PREFIX);
            placeNameToUrls.put(groupKey, SocialGroupURLMapping.SOCIAL_GROUP_URL_PREFIX);
        }
        return placeNameToUrls;
    }

    public List<JiveObjectType> getNonDefaultTypes() {
        Set<FilteredIndexableType> objectTypesByType = objectTypeManager
                .getObjectTypesByType(FilteredIndexableType.class);
        List<JiveObjectType> nonDefaults = new ArrayList<JiveObjectType>();

        for (FilteredIndexableType filteredType : objectTypesByType) {
            if (filteredType.isDefaultType() || !filteredType.includeInDefaultContentSearch()
                    // todo once video plugin is upgraded to 5.0.x remove the explicit id check for video
                    || filteredType.getID() == com.jivesoftware.community.JiveObjectType.Video.getKey())
            {
                continue;
            }

            nonDefaults.add(filteredType);
        }

        return nonDefaults;
    }

    public String getLanguage() {
        if (searchSettingsManager.isMultipleLanguageSearchEnabled() && language != null && language.length() > 0) {
            return language;
        }

        return searchActionHelper.getDefaultSearchLanguage(getUser());
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<SearchLanguageBean> getAllowedSearchLanguageBeans() {
        String defaultLanguage = searchActionHelper.getDefaultSearchLanguage(getUser());
        List<SearchLanguageBean> beans = Lists.newLinkedList();
        for (String lang : searchSettingsManager.getAllowedSearchLanguages()) {
            Locale langLocale = new Locale(lang);
            beans.add(new SearchLanguageBean(lang, langLocale.getDisplayName(getLocale()), lang.equals(defaultLanguage)));
        }
        return beans;
    }

    public SearchLanguageBean getDefaultLanguageBean() {
        String lang = searchActionHelper.getDefaultSearchLanguage(getUser());
        Locale langLocale = new Locale(lang);
        return new SearchLanguageBean(lang, langLocale.getDisplayName(getLocale()), true);
    }

    @Override
    public String buildMetaDescription() {
        return getText("search.meta.description", "", new String[]{getCommunityName()});
    }
    public JiveContainer getRootContainer() {
        return JiveApplication.getEffectiveContext().getCommunityManager().getRootCommunity();
    }

	public String getSearchParam() {
		return searchParam;
	}

	public void setSearchParam(String searchParam) {
		this.searchParam = searchParam;
	}

	public String getAll_fields() {
		return all_fields;
	}

	public void setAll_fields(String all_fields) {
		this.all_fields = all_fields;
	}

	public String getAdvanceSearch() {
		return advanceSearch;
	}

	public void setAdvanceSearch(String advanceSearch) {
		this.advanceSearch = advanceSearch;
	}

	public String getAny_fields() {
		return any_fields;
	}

	public void setAny_fields(String any_fields) {
		this.any_fields = any_fields;
	}

	public String getExact_fields() {
		return exact_fields;
	}

	public void setExact_fields(String exact_fields) {
		this.exact_fields = exact_fields;
	}

	public String getNone_fields() {
		return none_fields;
	}

	public void setNone_fields(String none_fields) {
		this.none_fields = none_fields;
	}

	public String getTag_fields() {
		return tag_fields;
	}

	public void setTag_fields(String tag_fields) {
		this.tag_fields = tag_fields;
	}

	public String getMrts() {
		return mrts;
	}

	public void setMrts(String mrts) {
		this.mrts = mrts;
	}
}
