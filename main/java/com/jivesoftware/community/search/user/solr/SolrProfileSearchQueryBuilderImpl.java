/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.search.user.solr;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.util.UserPermHelper;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.PresenceManager;
import com.jivesoftware.community.externalcollaboration.SharedGroupManager;
import com.jivesoftware.community.impl.search.user.solr.SolrDocumentBuilder;
import com.jivesoftware.community.search.user.ProfileSearchCriteria;
import com.jivesoftware.community.search.user.ProfileSearchQuerySettingsManager;
import com.jivesoftware.community.search.user.ProfileSecurityConstrainedQuerier;
import com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField;
import com.jivesoftware.community.socialgroup.SocialGroup;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.community.user.profile.ProfileFieldManager;
import com.jivesoftware.community.user.profile.ProfileSearchFilter;
import com.jivesoftware.community.user.profile.security.ProfileSecurityLevel;
import com.jivesoftware.community.user.profile.security.ProfileSecurityManager;
import com.jivesoftware.util.DateUtils;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.MoreLikeThisParams;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SolrProfileSearchQueryBuilderImpl extends ProfileSecurityConstrainedQuerier
        implements SolrProfileSearchQueryBuilder {

    private static final Logger log = LogManager.getLogger(SolrProfileSearchQueryBuilderImpl.class);

    private static final String LIMIT_FIELD_PARAM = "limit.field";
    private static final String LIMIT_VALUES_PARAM = "limit.value";
    private static final int ENTITY_LIMIT = 5000;
    private static final String LIMIT_PRESERVE_ORDER = "limit.order";

    private static final String NAME_SECURITY_FILTER_TAG = "n";

    // junk characters that may NOT overlap with solr or lucene reserved characters, such as .
    private static final Pattern BAD_CHARS_PATTERN = Pattern.compile("[,$%;]");

    private final ProfileFieldManager profileFieldManager;
    private final SolrDocumentBuilder solrDocumentBuilder;
    private final ProfileSearchQuerySettingsManager profileSearchQuerySettingsManager;
    private final PresenceManager presenceManager;
    private final SharedGroupManager sharedGroupManager;
    private final SolrProfileSearchSettingsManager solrProfileSearchSettingsManager;
    private final SolrUtils solrUtils;

    public SolrProfileSearchQueryBuilderImpl(ProfileSecurityManager profileSecurityManager, UserManager userManager,
            ProfileFieldManager profileFieldManager, SolrDocumentBuilder solrDocumentBuilder,
            ProfileSearchQuerySettingsManager profileSearchQuerySettingsManager, PresenceManager presenceManager,
            SharedGroupManager sharedGroupManager,
            SolrProfileSearchSettingsManager solrProfileSearchSettingsManager, SolrUtils solrUtils) {
        super(profileSecurityManager, userManager);
        this.profileFieldManager = profileFieldManager;
        this.solrDocumentBuilder = solrDocumentBuilder;
        this.profileSearchQuerySettingsManager = profileSearchQuerySettingsManager;
        this.presenceManager = presenceManager;
        this.sharedGroupManager = sharedGroupManager;
        this.solrProfileSearchSettingsManager = solrProfileSearchSettingsManager;
        this.solrUtils = solrUtils;
    }

    private Set<ProfileField> getFacetFields(ProfileSearchCriteria criteria) {
        Set<ProfileField> fieldsToFacet = new HashSet<ProfileField>();
        //subtract any fields we're already filtering
        for (ProfileField field : profileFieldManager.getFilterableFields()) {
            boolean alreadyFiltered = false;
            if (criteria.getFilters() != null) {
                for (ProfileSearchFilter f : criteria.getFilters()) {
                    if (f.getFieldID() == field.getID() && !f.isEmpty()) {
                        alreadyFiltered = true;
                    }
                }
            }
            if (!alreadyFiltered || criteria.isIncludeAppliedFacets()) {
                fieldsToFacet.add(field);
            }
        }

        return fieldsToFacet;
    }

    private String buildFacetFieldString(String facetFieldName, Set<Long> facetedFieldIDs, long excludeFieldID,
            boolean excludeNameFilterTag) {
        StringBuilder filterTagExclusions = new StringBuilder();
        for (Long fieldID : facetedFieldIDs) {
            if (excludeFieldID != -1L && fieldID == excludeFieldID) {
                continue;
            }

            if (filterTagExclusions.length() > 0) {
                filterTagExclusions.append(",");
            }
            filterTagExclusions.append(fieldID);
        }

        // Add an exclusion for the name security filter
        if (excludeNameFilterTag) {
            if (filterTagExclusions.length() > 0) {
                filterTagExclusions.append(",");
            }
            filterTagExclusions.append(NAME_SECURITY_FILTER_TAG);
        }

        String facetFieldString;
        if (filterTagExclusions.length() > 0) {
            facetFieldString = new StringBuilder("{!ex=").append(filterTagExclusions.toString()).append("}")
                    .append(facetFieldName).toString();
        }
        else {
            facetFieldString = facetFieldName;
        }

        return facetFieldString;
    }

    public SolrQuery applyFacetConditionsToQuery(SolrQuery query, ProfileSearchCriteria criteria) {
        Map<Long, String> securityIdentifierMap = getSecurityRoleMap(criteria.getQuerierID());
        if (securityIdentifierMap.size() > 0) {
            Set<ProfileField> facetFields = getFacetFields(criteria);

            query.setFacet(true);
            query.setFacetLimit(profileSearchQuerySettingsManager.getFacetsFilterTermThreshhold());
            query.setFacetSort("count");
            query.setFacetMinCount(1);

            Set<Long> facetFieldIDs = Sets.newHashSet(Iterables.transform(facetFields, new Function<ProfileField, Long>() {
                public Long apply(ProfileField profileField) {
                    return profileField.getID();
                }
            }));

            for (ProfileField field : facetFields) {
                // For this field, add a security filter to the query and tag it with the field ID
                String fieldName = solrDocumentBuilder.getProfileFieldIndexFieldName(field.getType(), field.getID());
                String fieldAclName = new StringBuilder(fieldName).append(SolrDocumentBuilder.ACL_POSTFIX).toString();

                String fieldAclFilter = new StringBuilder("{!tag=").append(field.getID()).append("}")
                    .append(aclConditions(fieldAclName, securityIdentifierMap)).toString();

                query.addFilterQuery(fieldAclFilter);
                query.addFacetField(buildFacetFieldString(fieldName, facetFieldIDs, field.getID(), true));
            }

            // Add the username facet field
            query.addFacetField(buildFacetFieldString(SolrDocumentBuilder.USERNAME_PREFIX_FIELD, facetFieldIDs, -1L, true));

            // Add the name security condition for prefix faceting
            String nameAclFilter = new StringBuilder("{!tag=").append(NAME_SECURITY_FILTER_TAG).append("}")
                    .append(aclConditions(SolrDocumentBuilder.NAME_ACL_FIELD, securityIdentifierMap)).toString();
            query.addFilterQuery(nameAclFilter);
            query.addFacetField(buildFacetFieldString(SolrDocumentBuilder.NAME_PREFIX_FIELD, facetFieldIDs, -1L, false));
            if (profileSearchQuerySettingsManager.isLastNameSearchEnabled()) {
                query.addFacetField(buildFacetFieldString(SolrDocumentBuilder.LASTNAME_PREFIX_FIELD, facetFieldIDs, -1, false));
            }
        }

        return query;
    }

    public SolrQuery buildSimilarQuery(ProfileSearchCriteria criteria) {
        String sourceQuery = new StringBuilder(DefaultProfileSearchIndexField.USER_ID.getFieldName()).append(":")
                .append(criteria.getFilterUserID()).toString();

        //use "everyone" keyword bucket and tags by default
        long everyoneID = ProfileSecurityLevel.DefaultProfileSecurityLevel.ALL_USERS.getID();
        StringBuilder fields = new StringBuilder()
            .append(SolrDocumentBuilder.KEYWORDS_PREFIX).append(everyoneID).append(",")
            .append(DefaultProfileSearchIndexField.TAGS.getFieldName());

        //if a registered user is searching, use "registered user" keyword bucket
        if (criteria.getQuerierID() > -1L) {
            long regUserID = ProfileSecurityLevel.DefaultProfileSecurityLevel.REGISTERED_USERS.getID();
            fields.append(",").append(SolrDocumentBuilder.KEYWORDS_PREFIX).append(regUserID);
        }

        SolrQuery query = new SolrQuery();
        query.setQuery(sourceQuery);
        query.setQueryType("/" + MoreLikeThisParams.MLT);
        query.set(MoreLikeThisParams.MATCH_INCLUDE, false);
        query.set(MoreLikeThisParams.MIN_DOC_FREQ, solrProfileSearchSettingsManager.getMLTMinDocFreq());
        query.set(MoreLikeThisParams.MIN_WORD_LEN, solrProfileSearchSettingsManager.getMLTMinWordLength());
        query.set(MoreLikeThisParams.MAX_WORD_LEN, solrProfileSearchSettingsManager.getMLTMaxWordLength());
        query.set(MoreLikeThisParams.MIN_TERM_FREQ, solrProfileSearchSettingsManager.getMLTMinTermFreq());
        query.set(MoreLikeThisParams.MAX_QUERY_TERMS, solrProfileSearchSettingsManager.getMLTMaxQueryTerms());
        query.set(MoreLikeThisParams.BOOST, true);
        query.set(MoreLikeThisParams.SIMILARITY_FIELDS, fields.toString());

        Map<Long, String> securityIdentifierMap = getSecurityRoleMap(criteria.getQuerierID());

        applyFilters(query, criteria, securityIdentifierMap);
        applySort(query, criteria);

        return query;
    }

    public SolrQuery buildQuery(ProfileSearchCriteria criteria) {
        String keywords = solrUtils.removeHyphenPrefixes(criteria.getKeywords());

        //CS-6132 - yank wildcard if it's the first letter of the query as Lucene can't do that
        //see http://lucene.apache.org/java/docs/queryparsersyntax.html#Wildcard%20Searches
        keywords = solrUtils.sanitizeUserSuppliedQuery(keywords);
        keywords = solrUtils.removeWildcardPrefixes(keywords);
        if (criteria.getUserLocale() != null){
            keywords = solrUtils.removeStopWords(keywords, criteria.getUserLocale().getLanguage());
        }


        /* Remove additional characters that the more general (shared among content and people search) SolrUtils solrUtils
         * does not. We could replace them with ' ', i.e. treat them as delimiters, but it will make the query more
         * expensive and the results not more satisfying to the user.
         * In particular we wanted to address queries such as "John, , Smith" and "Smith, John" both returning terms
         * "John", "Smith" instead of "John," or "Smith," or ","
         */
        if (StringUtils.isNotEmpty(keywords)) {
            keywords = BAD_CHARS_PATTERN.matcher(keywords).replaceAll("");
        }

        // Make sure any space character is set as just a normal English space so that Lucene will break up the query
        // string terms correctly.
        if (StringUtils.isNotEmpty(keywords)) {
            keywords = solrUtils.normalizeSpace(keywords);
        }

        Map<Long, String> securityIdentifierMap = getSecurityRoleMap(criteria.getQuerierID());

        String finalQueryString = null;
        if (keywords == null || StringUtils.isBlank(keywords)) {
            finalQueryString = "*:*";
        }
        else {
            keywords = keywords.replace(":", "\\:");
            // Everything is indexed in all lowercase, except for namePhonetic that is indexed in all uppercase.
            // It turns out that slor matches the namePhonetic regardless of its case. So lowercase here everything once.

            keywords = solrUtils.toLowerIgnoreOperators(keywords);
            keywords = keywords.trim();

            if (StringUtils.isNotBlank(keywords)) {
                StringBuilder solrQuery = new StringBuilder();

                String queryString = solrUtils.appendWildcardToKeywordTerm(keywords);
                String usernameClause = null;
                StringBuilder usernameClauseBuilder = new StringBuilder();
                if (criteria.isSearchUsername()) {
                    String usernameTerm = queryString.replaceAll("\\*", "");
                    // CS-13920 -- usernames are indexed in lowercase so make sure we're querying similarly
                    // search ProfileSearchManagerImpl.buildDocument(user) for proof
                    // usernameTerm = usernameTerm.toLowerCase();    lower-cased everything above
                    //attempt to match username on each term
                    String terms[] = usernameTerm.split("\\s");
                    StringBuilder conditions = new StringBuilder();
                    for (String term : terms) {
                        String sanitizedTerm = solrUtils.sanitizeUserSuppliedQuery(term);
                        if (solrUtils.isQuotedTerm(term)) {
                            //do exact match on username if surrounded by quotes
                            conditions.append("+").append(sanitizedTerm).append(" ");
                        } else {
                            //otherwise to "begins with" match
                            if (conditions.length() > 0) {
                                conditions.append(" ");
                            }
                            // if the word isn't an operator like OR / AND then append a wildcard token
                            if (!solrUtils.isTermOperator(term)) {
                                String wildTerm = solrUtils.appendWildcardToKeywordTerm(sanitizedTerm);
                                conditions.append(wildTerm);
                            }
                        }
                    }

                    if (conditions.toString().trim().length() > 0) {
                        usernameClauseBuilder.append(DefaultProfileSearchIndexField.USERNAME.getFieldName()).append(":")
                                .append("(").append(conditions.toString().trim()).append(")");
                    }

                    if (JiveGlobals.getJiveBooleanProperty("username.allowWhiteSpace", false)) {
                        appendClause(usernameClauseBuilder, "username:(\"" + solrUtils.sanitizeUserSuppliedQuery(usernameTerm) + "\")");
                    }

                    usernameClause = usernameClauseBuilder.toString();
                }

                if (StringUtils.isNotBlank(usernameClause)) {
                    appendClause(solrQuery, usernameClause);
                }

                String nameClause = buildNameClause(criteria,securityIdentifierMap,keywords);
                if (StringUtils.isNotBlank(nameClause)) {
                    appendClause(solrQuery, nameClause);
                }

                String emailClause = null;
                // TODO revise email clause construction. Currently a two term search appears as (term1 term2*)
                if (criteria.isSearchEmail() && securityIdentifierMap.values().size() > 0) {
                    String acl = aclConditions(SolrDocumentBuilder.EMAIL_ACL_FIELD, securityIdentifierMap);

                    String emailFieldMatch = new StringBuilder(DefaultProfileSearchIndexField.EMAIL.getFieldName())
                            .append(":(").append(solrUtils.escapeAllParentheses(queryString)).append(")").toString();

                    emailClause = new StringBuilder("(+").append(acl).append(" +").append(emailFieldMatch).append(")").toString();
                }

                if (StringUtils.isNotBlank(emailClause)) {
                    appendClause(solrQuery, emailClause);
                }

                String profileClause = null;
                if (criteria.isSearchProfile()) {
                    String keywordsClause = null;
                    StringBuilder keywordsClauseBuilder = new StringBuilder();
                    for (Long levelID : securityIdentifierMap.keySet()) {
                        String acl = new StringBuilder(SolrDocumentBuilder.KEYWORDS_ACL_PREFIX).append(levelID)
                                .append(SolrDocumentBuilder.ACL_POSTFIX).append(":(").append(securityIdentifierMap.get(levelID))
                                .append(")").toString();

                        String keywordsFieldMatch = new StringBuilder(SolrDocumentBuilder.KEYWORDS_PREFIX).append(levelID)
                                .append(":(").append(keywords).append(")").toString();

                        if (keywordsClauseBuilder.length() > 0) {
                            keywordsClauseBuilder.append(" OR ");
                        }
                        keywordsClauseBuilder.append("(+").append(acl).append(" +").append(keywordsFieldMatch).append(")");
                    }
                    if (keywordsClauseBuilder.toString().trim().length() > 0) {
                        keywordsClause = keywordsClauseBuilder.toString().trim();
                    }

                    StringBuilder profileClauseBuilder = new StringBuilder();

                    if (keywordsClause != null) {
                        profileClauseBuilder.append(keywordsClause).append(" OR ");
                    }

                    //Additional keywords clause for name and custom field joint search JIVE-51801
                    keywordsClause = null;
                    keywordsClauseBuilder.setLength(0);
                    String keywordsOR = keywords.replace(" ", " OR ");
                    for (Long levelID : securityIdentifierMap.keySet()) {
                        String acl = new StringBuilder(SolrDocumentBuilder.KEYWORDS_ACL_PREFIX).append(levelID)
                                .append(SolrDocumentBuilder.ACL_POSTFIX).append(":(").append(securityIdentifierMap.get(levelID))
                                .append(")").toString();

                        String keywordsFieldMatch = new StringBuilder(SolrDocumentBuilder.KEYWORDS_PREFIX).append(levelID)
                                .append(":(").append(keywordsOR).append(")").append(" +name:(").append(keywordsOR).append(")").toString();

                        if (keywordsClauseBuilder.length() > 0) {
                            keywordsClauseBuilder.append(" OR ");
                        }
                        keywordsClauseBuilder.append("(+").append(acl).append(" +").append(keywordsFieldMatch).append(")");
                    }
                    if (keywordsClauseBuilder.toString().trim().length() > 0) {
                        keywordsClause = keywordsClauseBuilder.toString().trim();
                    }

                    if (keywordsClause != null) {
                        profileClauseBuilder.append(keywordsClause).append(" OR ");
                    }

                    // if we want to boost importance of tags for expertise tag feature, do it here
                    StringBuilder tagClauseBuilder = new StringBuilder(DefaultProfileSearchIndexField.TAGS.getFieldName())
                            .append(":(").append(keywords).append(")");
                    // improvement for spotlight search
                    if (keywords.endsWith("*") && keywords.length() > 2) {
                        tagClauseBuilder.append(" OR ")
                                .append(DefaultProfileSearchIndexField.TAGS.getFieldName())
                                .append(":(").append(keywords.substring(0, keywords.length() - 1)).append(")^2");
                    }

                    profileClauseBuilder.append(tagClauseBuilder.toString());
                    profileClause = profileClauseBuilder.toString();
                }

                if (StringUtils.isNotBlank(profileClause)) {
                    appendClause(solrQuery, profileClause);
                }

                setQueryDefaultOperator(solrQuery);
                finalQueryString = solrQuery.toString().trim();
            }
        }

        if (StringUtils.isBlank(finalQueryString)) {
            return null;
        }

        SolrQuery query = new SolrQuery();
        query.setQuery(finalQueryString);

        applyFilters(query, criteria, securityIdentifierMap);
        applySort(query, criteria);

        return query;
    }


    private String getUserPropertyClause(final String fieldName, final String fieldValue) {
        StringBuilder propertyClause = new StringBuilder();
        propertyClause.append(fieldName);
        propertyClause.append(":(").append(fieldValue).append(")");
        return propertyClause.toString();
    }
    
    private String buildNameClause(ProfileSearchCriteria criteria, Map<Long, String> securityIdentifierMap,
            String keywords)
    {
        keywords = solrUtils.removeWildcardsFromTerms(keywords);
        String nameClause = null;

        if (criteria.isSearchName() && securityIdentifierMap.values().size() > 0) {
            // TODO FIXME returns (reg OR 1 OR 1 OR 1 OR 1) why so many identical clauses?
            String acl = aclConditions(SolrDocumentBuilder.NAME_ACL_FIELD, securityIdentifierMap);

            StringBuilder nameClauseBuilder = new StringBuilder("(+").append(acl).append(" +(");

            String nameFieldMatch = new StringBuilder(DefaultProfileSearchIndexField.NAME.getFieldName())
                    .append(":(").append(keywords).append(")").toString();

            //boost name fields
            nameClauseBuilder.append("(").append(nameFieldMatch).append(")^3");

            // always add synonym clause
            String nameSynFieldMatch = new StringBuilder(SolrDocumentBuilder.SYNONYM_NAME).append(":(")
                    .append(keywords).append(")").toString();
            nameClauseBuilder.append(" OR ").append(nameSynFieldMatch);

            // add phonetic clause if enabled
            if (criteria.isSearchNamePhonetically() && profileSearchQuerySettingsManager.isPhoneticSearchEnabled()) {
                String phoneticFieldMatch = new StringBuilder(SolrDocumentBuilder.PHONETIC_NAME).append(":(")
                        .append(keywords).append(")").toString();
                nameClauseBuilder.append(" OR ").append(phoneticFieldMatch);
            }

            // add ngram clause if enabled, boosting here has the greatest effect on matching user names in brewspace
            if (criteria.isSearchNameUsingNGrams()) {
                String nGramMatch = SolrDocumentBuilder.NGRAM_NAME + ":(" + keywords + ")^8";
                nameClauseBuilder.append(" OR ").append(nGramMatch);
            }

            nameClauseBuilder.append("))");
            nameClause = nameClauseBuilder.toString().trim();
        }
        return nameClause;
    }

    /**
     * Applies the default operator to the query String given
     *
     * @param solrQuery query String to apply default operator condition to
     */
    private void setQueryDefaultOperator(StringBuilder solrQuery) {
        StringBuilder operator = new StringBuilder("{!q.op=");
        if (profileSearchQuerySettingsManager.getDefaultOperator().equalsIgnoreCase("OR")) {
            operator.append("OR");
        }
        else {
            operator.append("AND");
        }
        operator.append("}");

        solrQuery.insert(0, operator.toString());
    }

    /**
     * Appends a clause to the given query string with an "OR" condition
     *
     * @param queryString query string to append clause to
     * @param clause the clause to append to the query string
     */
    private void appendClause(StringBuilder queryString, String clause) {
        if (queryString.length() > 0) {
            queryString.append(" OR ");
        }

        queryString.append(clause);
    }

    /**
     * Applies the appropriate settings to the given {@link org.apache.solr.client.solrj.SolrQuery} to fulfill the sort
     * requirements of the given {@link com.jivesoftware.community.search.user.ProfileSearchCriteria}
     *
     * @param query the query object to apply sorting to
     * @param criteria the criteria for the search that contains the sort conditions
     */
    private void applySort(SolrQuery query, ProfileSearchCriteria criteria) {
        /* Removed from index status fields with names that include container object types and ids
         * Following in fact applied a sort on a field that was not indexed as it omitted the id
        /*
        // if the user is sorting by status level, sort by the community status level,
        // not the global status level: ie: if user bill has points = 59 globally
        // but only 1 points in this specific community, he should come after after user
        // matt who has 39 points globally but 20 points in this community
        if (criteria.getCommunityID() != -1L && criteria.getSort().equals(ProfileSearchCriteria.DefaultSort.STATUS_LEVEL)) {
            query.addSortField(new StringBuilder(DefaultProfileSearchIndexField.STATUS_LEVEL.getFieldName()).append("-")
                    .append(criteria.getCommunityID()).toString(), SolrQuery.ORDER.desc);
            return;
        }
        */
        if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.CREATION_DATE) {
            query.addSortField(DefaultProfileSearchIndexField.CREATION_DATE.getFieldName(), SolrQuery.ORDER.desc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.CREATION_DATE_ASC) {
            query.addSortField(DefaultProfileSearchIndexField.CREATION_DATE.getFieldName(), SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.MODIFICATION_DATE) {
            query.addSortField(DefaultProfileSearchIndexField.MODIFICATION_DATE.getFieldName(), SolrQuery.ORDER.desc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.MODIFICATION_DATE_ASC) {
            query.addSortField(DefaultProfileSearchIndexField.MODIFICATION_DATE.getFieldName(), SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.INITIAL_LOGIN_DATE) {
            query.addSortField(DefaultProfileSearchIndexField.INITIAL_LOGIN_DATE.getFieldName(), SolrQuery.ORDER.desc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.INITIAL_LOGIN_DATE_ASC) {
            query.addSortField(DefaultProfileSearchIndexField.INITIAL_LOGIN_DATE.getFieldName(), SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.LAST_PROFILE_UPDATE) {
            query.addSortField(DefaultProfileSearchIndexField.LAST_PROFILE_UPDATE.getFieldName(), SolrQuery.ORDER.desc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.LAST_PROFILE_UPDATE_ASC) {
            query.addSortField(DefaultProfileSearchIndexField.LAST_PROFILE_UPDATE.getFieldName(), SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.STATUS_LEVEL) {
            query.addSortField(DefaultProfileSearchIndexField.STATUS_LEVEL.getFieldName(), SolrQuery.ORDER.desc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.USERNAME) {
            query.addSortField(DefaultProfileSearchIndexField.USERNAME.getFieldName(), SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.USER_ID) {
            query.addSortField(DefaultProfileSearchIndexField.USER_ID.getFieldName(), SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.NAME) {
            query.addSortField(SolrDocumentBuilder.SORTABLE_NAME, SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.LAST_NAME) {
            query.addSortField(SolrDocumentBuilder.SORTABLE_LAST_NAME, SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.EMAIL) {
            query.addSortField(DefaultProfileSearchIndexField.EMAIL.getFieldName(), SolrQuery.ORDER.asc);
        }
        else if (criteria.getSort() == ProfileSearchCriteria.DefaultSort.LAST_LOGGED_IN) {
            query.addSortField(DefaultProfileSearchIndexField.LAST_LOGGED_IN.getFieldName(), SolrQuery.ORDER.desc);
        }
        else {
            query.addSortField("score", SolrQuery.ORDER.desc);
        }
    }

    /**
     * Applies any necessary filter queries to the given query object based on the conditions on the given {@link com.jivesoftware.community.search.user.ProfileSearchCriteria}
     * object and profile security information
     *
     * @param query the query to apply filtering to
     * @param criteria the criteria for the search
     * @param securityIdentifierMap map of profile security level ID to security role for the user the query is being
     * executed as
     */
    private void applyFilters(SolrQuery query, ProfileSearchCriteria criteria, Map<Long, String> securityIdentifierMap) {
        if (criteria.getFilters() != null && criteria.getFilters().size() > 0) {
            applyProfileFieldFilters(query, criteria.getFilters(), securityIdentifierMap, criteria.getUserLocale());
        }

        if (criteria.getMaxCreationDate() != null || criteria.getMinCreationDate() != null) {
            applyCreationDateFilter(query, criteria.getMinCreationDate(), criteria.getMaxCreationDate(), securityIdentifierMap);
        }

        if (criteria.getMaxLastProfileUpdate() != null || criteria.getMinLastProfileUpdate() != null) {
            applyLastProfileUpdateFilter(query, criteria.getMinLastProfileUpdate(), criteria.getMaxLastProfileUpdate(), securityIdentifierMap);
        }

        if (criteria.getMaxModificationDate() != null || criteria.getMinModificationDate() != null) {
            applyModificationDateFilter(query, criteria.getMinModificationDate(), criteria.getMaxModificationDate(), securityIdentifierMap);
        }

        if (StringUtils.isNotBlank(criteria.getPrefix())) {
            applyPrefixFilter(query, criteria, securityIdentifierMap);
        }

        // see details at removed method definition
        //if (criteria.getCommunityID() > -1L) {
        //    applyCommunityFilter(query, criteria.getCommunityID());
        //}

        if (criteria.areTagsSpecified()) {
            StringBuilder builder = new StringBuilder(DefaultProfileSearchIndexField.TAGS.getFieldName()).append(":(");
            for (String tag : criteria.getTags()) {
                builder.append(tag).append(" ");
            }
            builder.append(")");
            query.addFilterQuery(builder.toString());
        }
       
        Map<String, String> properties = criteria.getProperties();
        
        boolean getSynchroUsers = false;
        
     // This is for Synchro Phase 5 New Requirements to get All the Synchro Users both Enable and Disabled
       /* if(properties != null && properties.size() > 0)
        {
        	if(properties.containsKey("role"))
        	{
        		if(properties.get("role").equals(SynchroConstants.SYNCHRO_USER_FIELDNAME)){
        			getSynchroUsers= true;
        			query.addFilterQuery(new StringBuilder("-").append(DefaultProfileSearchIndexField.ENABLED.getFieldName()).append(":1")
    	                    .toString());
                }
        	}
        }
        if(!getSynchroUsers)
        {
	        if (!criteria.isReturnDisabledUsers()) {
	            query.addFilterQuery(new StringBuilder("-").append(DefaultProfileSearchIndexField.ENABLED.getFieldName()).append(":0")
	                    .toString());
	        }
        }
*/
        applyUserTypeFilter(query,criteria);
        
        /**
         * Customization for implementing user select filters in Synchro
         */
        List<String> propertiesCondition = new ArrayList<String>();
        String propertiesNotCondition = "";
       
        String fromSynchro = criteria.getFromSynchro();
        if(properties != null && properties.size() > 0) {
            if(properties.containsKey("brand")) {
                query.addFilterQuery(getUserPropertyClause(SolrDocumentBuilder.BRAND_PFB_FIELD, properties.get("brand")));
            }

            if(properties.containsKey("region")) {
                query.addFilterQuery(getUserPropertyClause(SolrDocumentBuilder.REGION_PFB_FIELD, properties.get("region")));
            }

            if(properties.containsKey("country")) {
                query.addFilterQuery(getUserPropertyClause(SolrDocumentBuilder.COUNTRY_PFB_FIELD, properties.get("country")));
            }

            if(properties.containsKey("role")) {
                
            	// This is for Synchro Phase 5 New Requirements to get All the Synchro Users
            	if(properties.get("role").equals(SynchroConstants.SYNCHRO_USER_FIELDNAME)){
            		applySynchroUserFilter(query, SolrDocumentBuilder.ROLE_PFB_FIELD);
                }
            	else if(properties.get("role").equals(SynchroConstants.SYNCHRO_PROJECT_OWNER_FIELDNAME)){
                    applyOwnerPropertyFilter(query, SolrDocumentBuilder.ROLE_PFB_FIELD);
                }
                // Fetch the Agency and BAT users
                else if(properties.get("role").equals(SynchroConstants.SYNCHRO_AGENCY_BAT_USER_FIELDNAME)){
                	applyBatAgencyPropertyFilter(query, SolrDocumentBuilder.ROLE_PFB_FIELD);
                }
                else {
                    query.addFilterQuery(getUserPropertyClause(SolrDocumentBuilder.ROLE_PFB_FIELD, properties.get("role")));
                }
            }

            if(properties.containsKey("jobTitle")) {
                query.addFilterQuery(getUserPropertyClause(SolrDocumentBuilder.JOB_TITLE_PFB_FIELD, properties.get("jobTitle")));
            }

        }

        if(StringUtils.isNotBlank(fromSynchro) && StringUtils.equalsIgnoreCase(fromSynchro, "true")) {
            query.addFilterQuery(getUserPropertyClause(SolrDocumentBuilder.FROM_SYNCHRO_PFB_FIELD, fromSynchro));
        }


//        if (!criteria.isReturnPartnerUsers()) {
//            query.addFilterQuery(new StringBuilder("-").append(DefaultProfileSearchIndexField.PARTNER.getFieldName()).append(":1")
//                    .toString());
//        }

        // Online user filtering
        if (criteria.isReturnOnlineUsers()) {
             applyEntitySetFilter(criteria, query, presenceManager.getOnlineUserIDs());
        }

        //filter an existing set of users
        if (criteria.getEntities() != null && !criteria.getEntities().isEmpty()) {
            Set<Long> userIDs = Sets.newLinkedHashSet();
            for (EntityDescriptor entityDescriptor : criteria.getEntities()) {
                userIDs.add(entityDescriptor.getID());
            }
            applyEntitySetFilter(criteria, query, userIDs);
        }
    }
    private void applyOwnerPropertyFilter(final SolrQuery query, final String fieldName) {

        //AND NOT (pfb-role:(BAT) AND pfb-role:(LEGAL) AND pfb-role:(SPI)) AND pfb-fromsynchro:(true)

        // Not Legal Role
        query.addFilterQuery(new StringBuilder().append("-").append(fieldName)
                .append(":(").append(SynchroGlobal.UserRole.LEGAL.name()).append(")").toString());

        // Not Procurement Role
        query.addFilterQuery(new StringBuilder().append("-").append(fieldName)
                .append(":(").append(SynchroGlobal.UserRole.PROCUREMENT.name()).append(")").toString());

        // Not External Agency Role
        query.addFilterQuery(new StringBuilder().append("-").append(fieldName)
                .append(":(").append(SynchroGlobal.UserRole.EXTERNALAGENCY.name()).append(")").toString());

//        // Not Communication Agency Role
        query.addFilterQuery(new StringBuilder().append("-").append(fieldName)
                .append(":(").append(SynchroGlobal.UserRole.COMMUNICATIONAGENCY.name()).append(")").toString());
    }
    
    /**
     * This method will fetch all the Agency Users and the BAT users
     * @param query
     * @param fieldName
     */
    private void applyBatAgencyPropertyFilter(final SolrQuery query, final String fieldName) {

        //AND NOT (pfb-role:(BAT) AND pfb-role:(LEGAL) AND pfb-role:(SPI)) AND pfb-fromsynchro:(true)

        // Not Procurement Role
        query.addFilterQuery(new StringBuilder().append("-").append(fieldName)
                .append(":(").append(SynchroGlobal.UserRole.PROCUREMENT.name()).append(")").toString());

      //        // Not Communication Agency Role
        query.addFilterQuery(new StringBuilder().append("-").append(fieldName)
                .append(":(").append(SynchroGlobal.UserRole.COMMUNICATIONAGENCY.name()).append(")").toString());
    }

    private void applySynchroUserFilter(final SolrQuery query, final String fieldName) {

        //AND NOT (pfb-role:(BAT) AND pfb-role:(LEGAL) AND pfb-role:(SPI)) AND pfb-fromsynchro:(true)

        // Not Legal Role
        query.addFilterQuery(new StringBuilder().append("-").append(fieldName)
                .append(":(").append(SynchroGlobal.UserRole.SYNCHRO.name()).append(")").toString());

    }
    
    /**
     * Filter on user type.  Admins can see all partner users.  Non-admins can only see partner users for shared groups
     * in which both the user and partner are members.
     */
    private void applyUserTypeFilter(SolrQuery query, ProfileSearchCriteria criteria) {
        List<String> clauses = new ArrayList<>();
        if (criteria.isReturnRegularUsers()) {
            clauses.add("(+userType:" + String.valueOf(User.Type.REGULAR.getId()) + ")");
        }
        if (criteria.isReturnExternalUsers()) {
            clauses.add("(+userType:" + String.valueOf(User.Type.EXTERNAL.getId()) + ")");
        }
        if (criteria.isReturnPartnerUsers()) {
            User user = getUser(criteria.getQuerierID());
            Iterator<SocialGroup> allSharedGroupsForUser = sharedGroupManager.getAllSharedGroupsForUser(user);
            boolean isAdmin = UserPermHelper.isGlobalUserAdmin(user) || UserPermHelper.isSystemAdmin(user);
            if (isAdmin || allSharedGroupsForUser.hasNext()) {
                StringBuilder partnerClause = new StringBuilder();
                partnerClause.append("(+userType:").append(String.valueOf(User.Type.PARTNER.getId()));
                if (!isAdmin) {
                    Iterable<SocialGroup> groups = () -> allSharedGroupsForUser;
                    Set<Long> groupIDs = StreamSupport.stream(groups.spliterator(), false).map(JiveObject::getID).collect(Collectors.<Long>toSet());
                    partnerClause.append(" +").append(SolrDocumentBuilder.SHARED_GROUPS);
                    partnerClause.append(":(").append(Joiner.on(" OR ").join(groupIDs)).append(")");
                }
                clauses.add(partnerClause.append(")").toString());
                if (log.isDebugEnabled()) {
                    log.debug("Partner query clause: " + partnerClause);
                }
            }
        }
        if (!clauses.isEmpty()) {
            query.addFilterQuery(Joiner.on(" OR ").join(clauses));
        }
    }

    /**
     * Applies filtering to the given query to limit results to users with usernames/names that are prefixed by the
     * prefix specified by a call to {@link com.jivesoftware.community.search.user.ProfileSearchCriteria#getPrefix()} on
     * the given criteria object and applies any appropriate security filtering according to the security information
     * given
     *
     * @param query the query to apply filtering to
     * @param criteria the criteria object for the query
     * @param securityIdentifierMap Map of security level ID to security role for the user the query is being executed
     * as
     */
    private void applyPrefixFilter(SolrQuery query, ProfileSearchCriteria criteria, Map<Long, String> securityIdentifierMap) {
        boolean useLastName = profileSearchQuerySettingsManager.isLastNameSearchEnabled();
        String prefix = criteria.getPrefix().toLowerCase();
        String usernameFilter = null;
        if (criteria.isSearchUsername()) {
            usernameFilter = new StringBuilder(SolrDocumentBuilder.USERNAME_PREFIX_FIELD).append(":")
                    .append(prefix).toString();
        }

        String nameFilter = null;
        if (criteria.isSearchName() && securityIdentifierMap.values().size() > 0) {
            String nameAcl = aclConditions(SolrDocumentBuilder.NAME_ACL_FIELD, securityIdentifierMap);
            String namePrefix = new StringBuilder(SolrDocumentBuilder.NAME_PREFIX_FIELD).append(":").append(prefix).toString();

            StringBuilder namePrefixFields = new StringBuilder("+").append(nameAcl).append(" +");
            if (useLastName) {
                String lastNamePrefix = new StringBuilder(SolrDocumentBuilder.LASTNAME_PREFIX_FIELD).append(":")
                        .append(prefix).toString();
                namePrefixFields.append("(").append(namePrefix).append(" OR ").append(lastNamePrefix).append(")");
            }
            else {
                namePrefixFields.append(namePrefix);
            }

            nameFilter = namePrefixFields.toString();
        }

        StringBuilder filterQuery = new StringBuilder();
        if (usernameFilter != null) {
            filterQuery.append(usernameFilter);
        }
        if (nameFilter != null) {
            if (filterQuery.length() > 0) {
                filterQuery.append(" OR (").append(nameFilter).append(")");
            }
            else {
                filterQuery.append(nameFilter);
            }
        }

        query.addFilterQuery(filterQuery.toString());
    }

    /**
     * Applies filtering to the given query to limit results to users with a creation date between the min and max dates
     * provided.
     *
     * @param query the query to apply filtering to
     * @param minCreationDate the minimum creation date value or NULL if no minimum
     * @param maxCreationDate the maximum creation date value or NULL if no minimum
     * @param securityIdentifierMap Map of security level ID to assigned role for the user the query is to be executed as
     */
    private void applyCreationDateFilter(SolrQuery query, Date minCreationDate, Date maxCreationDate,
            Map<Long, String> securityIdentifierMap) {
        String acl = aclConditions(SolrDocumentBuilder.CREATION_DATE_ACL_FIELD, securityIdentifierMap);

        StringBuilder fieldCondition = new StringBuilder(DefaultProfileSearchIndexField.CREATION_DATE.getFieldName()).append(":");
        Long min = minCreationDate != null ? minCreationDate.getTime() : null;
        Long max = maxCreationDate != null ? maxCreationDate.getTime() : null;
        fieldCondition.append(rangeCondition(min, max));

        query.addFilterQuery("+"+acl);
        query.addFilterQuery("+"+fieldCondition);
    }

    /**
     * Applies filtering to the given query to limit results to users with a last profile update between the min and max dates
     * provided.
     *
     * @param query the query to apply filtering to
     * @param minLastProfileUpdate the minimum last profile update value or NULL if no minimum
     * @param maxLastProfileUpdate the maximum last profile update value or NULL if no minimum
     * @param securityIdentifierMap Map of security level ID to assigned role for the user the query is to be executed as
     */
    private void applyLastProfileUpdateFilter(SolrQuery query, Date minLastProfileUpdate, Date maxLastProfileUpdate,
            Map<Long, String> securityIdentifierMap) {
        String acl = aclConditions(SolrDocumentBuilder.LAST_PROFILE_UPDATE_ACL_FIELD, securityIdentifierMap);

        StringBuilder fieldCondition = new StringBuilder(DefaultProfileSearchIndexField.LAST_PROFILE_UPDATE.getFieldName()).append(":");
        Long min = minLastProfileUpdate != null ? minLastProfileUpdate.getTime() : null;
        Long max = maxLastProfileUpdate != null ? maxLastProfileUpdate.getTime() : null;
        fieldCondition.append(rangeCondition(min, max));

        String filter = new StringBuilder("+").append(acl).append(" +").append(fieldCondition).toString();
        query.addFilterQuery(filter);
    }

    /**
     * Applies filtering to the given query to limit results to users with a modification date between the min and max dates
     * provided.
     *
     * @param query the query to apply filtering to
     * @param minModificationDate the minimum modification date value or NULL if no minimum
     * @param maxModificationDate the maximum modification date value or NULL if no minimum
     * @param securityIdentifierMap Map of security level ID to assigned role for the user the query is to be executed as
     */
    private void applyModificationDateFilter(SolrQuery query, Date minModificationDate, Date maxModificationDate,
            Map<Long, String> securityIdentifierMap) {
        String acl = aclConditions(SolrDocumentBuilder.MODIFICATION_DATE_ACL_FIELD, securityIdentifierMap);

        StringBuilder fieldCondition = new StringBuilder(DefaultProfileSearchIndexField.MODIFICATION_DATE.getFieldName()).append(":");
        Long min = minModificationDate != null ? minModificationDate.getTime() : null;
        Long max = maxModificationDate != null ? maxModificationDate.getTime() : null;
        fieldCondition.append(rangeCondition(min, max));

        String filter = new StringBuilder("+").append(acl).append(" +").append(fieldCondition).toString();
        query.addFilterQuery(filter);
    }

    /**
     * Applies filtering to the given query according to a given list of filters
     *
     * @param query the query to apply filtering to
     * @param filters the filters specifying the profile field conditions to add to the query
     * @param securityIdentifierMap Map of security level ID to assigned role for the user the query is to be executed as
     * @param userLocale locale of the user the query is to be executed as
     */
    private void applyProfileFieldFilters(SolrQuery query, List<ProfileSearchFilter> filters,
            Map<Long, String> securityIdentifierMap, Locale userLocale) {

        if (securityIdentifierMap.size() == 0) {
            return;
        }

        for (ProfileSearchFilter profileFilter : filters) {
            if (!profileFilter.isEmpty()) {
                ProfileField field = profileFieldManager.getProfileField(profileFilter.getFieldID());
                if (profileFilter.isValid(field.getType(), userLocale)) {
                    String condition = constructProfileFieldFilterCondition(profileFilter, field, userLocale);
                    if (condition == null) {
                        continue;
                    }

                    String fieldName = solrDocumentBuilder.getProfileFieldIndexFieldName(field.getType(), field.getID());
                    String aclFieldName = new StringBuilder(fieldName).append(SolrDocumentBuilder.ACL_POSTFIX).toString();

                    String fieldAcl = aclConditions(aclFieldName, securityIdentifierMap);
                    query.addFilterQuery("+" + fieldAcl);

                    String fieldQuery = new StringBuilder(fieldName).append(":").append(condition).toString();
                    query.addFilterQuery("+" + fieldQuery);
                }
            }
        }
    }

    /**
     * Constructs a query string that fulfills the given filter conditions specified by the given {@link com.jivesoftware.community.user.profile.ProfileSearchFilter}
     *
     * @param profileFilter the filter to construct a query for
     * @param field the profile field the condition is to be applied for
     * @param userLocale the locale of the user the query is to be executed as
     * @return
     */
    private String constructProfileFieldFilterCondition(ProfileSearchFilter profileFilter, ProfileField field,
            Locale userLocale) {

        String condition;
        ProfileField.Type type = field.getType();
        //deal with raw dates
        if (type.equals(ProfileField.Type.DATE) && profileFilter.isUnformatted()) {
            type = ProfileField.Type.NUMBER;
        }
        switch (type) {
            case DATE:
                Long minTime = null;
                Long maxTime = null;
                try {
                    if (StringUtils.isNotBlank(profileFilter.getMinValue())) {
                        Date minDate = DateUtils.parseFromLocalDate(profileFilter.getMinValue(), userLocale);
                        minTime = minDate.getTime();
                    }

                    if (StringUtils.isNotBlank(profileFilter.getMaxValue())) {
                        Date maxDate = DateUtils.parseFromLocalDate(profileFilter.getMaxValue(), userLocale);
                        maxTime = maxDate.getTime();
                    }

                    if (minTime == null && maxTime == null && StringUtils.isNotBlank(profileFilter.getValue())){
                        Date date = DateUtils.parseFromLocalDate(profileFilter.getValue(), userLocale);
                        minTime = maxTime = date.getTime();
                    }
                }
                catch (ParseException e) {
                    // Couldn't parse dates, no filter possible
                    return null;
                }

                condition = rangeCondition(minTime, maxTime);
                break;
            case DECIMAL:
                if (StringUtils.isEmpty(profileFilter.getValue())) {
                    Double minD = profileFilter.getMinValue() != null ? Double.parseDouble(profileFilter.getMinValue()) : null;
                    Double maxD = profileFilter.getMaxValue() != null ? Double.parseDouble(profileFilter.getMaxValue()) : null;
                    condition = rangeCondition(minD, maxD);
                } else {
                    condition = profileFilter.getValue();
                }
                break;
            case NUMBER:
                if (StringUtils.isEmpty(profileFilter.getValue())) {
                    Long minL = profileFilter.getMinValue() != null ? Long.parseLong(profileFilter.getMinValue()) : null;
                    Long maxL = profileFilter.getMaxValue() != null ? Long.parseLong(profileFilter.getMaxValue()) : null;
                    condition = rangeCondition(minL, maxL);
                } else {
                    condition = profileFilter.getValue();
                }
                break;
            case BOOLEAN:
                condition = profileFilter.getValue();
                break;
            default:
                StringBuilder sb = new StringBuilder();
                boolean quotesSurroundingValue = profileFilter.getValue().startsWith("\"") && profileFilter.getValue().endsWith("\"");
                String valueWithNoSurroundingQuotes = profileFilter.getValue();
                if (quotesSurroundingValue) {
                    valueWithNoSurroundingQuotes = profileFilter.getValue().substring(1,profileFilter.getValue().length()-1);
                }
                if (quotesSurroundingValue) {
                    sb.append("\\\"");
                } else {
                    sb.append("\"");
                }
                sb.append(valueWithNoSurroundingQuotes);
                if (quotesSurroundingValue) {
                    sb.append("\\\"");
                } else {
                    sb.append("\"");
                }
                condition = sb.toString();
        }

        return condition;
    }

    /**
     * Builds a range condition String that Solr will recognize built from the given minimum and maximum Numbers
     *
     * @param min minium range condition or NULL if no minimum
     * @param max maximum range condition or NULL if no maximum
     * @param <T> the type of Number that min and max are
     * @return Solr range query
     */
    private <T extends Number> String rangeCondition(T min, T max) {
        StringBuilder condition = new StringBuilder("[");
        if (min != null && max != null) {
            condition.append(min).append(" TO ").append(max);
        }
        else if (min != null) {
            condition.append(min).append(" TO *");
        }
        else if (max != null) {
            condition.append("* TO ").append(max);
        }
        condition.append("]");

        return condition.toString();
    }

    private String aclConditions(String aclFieldName, Map<Long, String> securityIdentifierMap) {
        StringBuilder aclConditions = new StringBuilder();
        for (String identifier : securityIdentifierMap.values()) {
            if (aclConditions.length() > 0) {
                aclConditions.append(" OR ");
            }
            aclConditions.append(identifier);
        }

        StringBuilder aclBuilder = new StringBuilder(aclFieldName).append(":");
        if (securityIdentifierMap.values().size() > 1) {
            aclBuilder.append("(").append(aclConditions).append(")");
        }
        else {
            aclBuilder.append(aclConditions);
        }

        return aclBuilder.toString();
    }

    private void applyEntitySetFilter(ProfileSearchCriteria criteria, SolrQuery query, Collection<Long> userIDs) {
        query.set(LIMIT_FIELD_PARAM, DefaultProfileSearchIndexField.USER_ID.getFieldName());
        int c = 0;
        for (Long userID : userIDs) {
            if (c >= ENTITY_LIMIT) {
                break;
            }
            query.add(LIMIT_VALUES_PARAM, String.valueOf(userID));
            c++;
        }
        if (criteria.isPreserveEntityOrder()) {
            query.add(LIMIT_PRESERVE_ORDER, "true");
        }
    }

}
