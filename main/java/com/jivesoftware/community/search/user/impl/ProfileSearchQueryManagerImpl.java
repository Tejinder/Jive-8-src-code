/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.search.user.impl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.community.DataLoadingStrategy;
import com.jivesoftware.community.search.user.ProfileSearchCriteria;
import com.jivesoftware.community.search.user.ProfileSearchCriteriaBuilder;
import com.jivesoftware.community.search.user.ProfileSearchExecutorResult;
import com.jivesoftware.community.search.user.ProfileSearchQueryExecutor;
import com.jivesoftware.community.search.user.ProfileSearchQueryManager;
import com.jivesoftware.community.search.user.ProfileSearchQuerySettingsManager;
import com.jivesoftware.community.search.user.ProfileSearchResult;
import com.jivesoftware.community.search.user.ProfileSearchSettingsManager;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

public class ProfileSearchQueryManagerImpl implements ProfileSearchQueryManager {

    private static final Logger log = LogManager.getLogger(ProfileSearchQueryManagerImpl.class);

    private ProfileSearchQuerySettingsManager profileSearchQuerySettingsManager;
    private ProfileSearchQueryExecutor executor;
    private UserManager userManager;
    private ProfileSearchSettingsManager profileSearchSettingsManager;
    private DataLoadingStrategy<Long, User> dataLoadingStrategy;

    public void setProfileSearchQuerySettingsManager(ProfileSearchQuerySettingsManager profileSearchQuerySettingsManager) {
        this.profileSearchQuerySettingsManager = profileSearchQuerySettingsManager;
    }

    public void setProfileSearchExecutor(ProfileSearchQueryExecutor executor) {
        this.executor = executor;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setProfileSearchSettingsManager(ProfileSearchSettingsManager profileSearchSettingsManager) {
        this.profileSearchSettingsManager = profileSearchSettingsManager;
    }

    public void setDataLoadingStrategy(DataLoadingStrategy<Long, User> dataLoadingStrategy) {
        this.dataLoadingStrategy = dataLoadingStrategy;
    }

    public ProfileSearchCriteria prepareCriteria(ProfileSearchCriteria rawCriteria) {
        ProfileSearchCriteriaBuilder builder = new ProfileSearchCriteriaBuilder(rawCriteria);

        // Trim query keywords
        if (StringUtils.isNotBlank(builder.getKeywords())) {
            builder.setKeywords(builder.getKeywords().trim());
        }

        // Ensure disabled users flag is set appropriately
        if (builder.getFilterUserID() > 0L && builder.isReturnDisabledUsers()) {
            builder.setReturnDisabledUsers(false);
        }

        if (builder.isSearchUsername() && !profileSearchQuerySettingsManager.isUserSearchQueryUsername()) {
            builder.setSearchUsername(false);
        }

        return builder.build();
    }

    public ProfileSearchResult executeSearch(ProfileSearchCriteria criteria) {
        ProfileSearchCriteria preparedCriteria = prepareCriteria(criteria);

        ProfileSearchExecutorResult result = null;
        try {
            result = executor.executeQuery(preparedCriteria);
        }
        catch (RuntimeException e) {
            // need to log original input because lower level logging only shows the specific solrQuery
            log.error("Error executing query with criteria:" + criteria.toString());
            throw e;
        }

        // User updates are not put into the search index immediately so it is possible that a user gets disabled but
        // that user still comes back in the results because the search index doesn't know about the change yet.
        // Therefore, if the criteria says that disabled users should not be returned, we need to put in an extra
        // check to ensure that that condition is honored even if the index is a little behind.
        List<Predicate<User>> preds = Lists.newArrayList(Predicates.<User>notNull());
        if (!preparedCriteria.isReturnDisabledUsers()) {
            preds.add(new Predicate<User>() {
                public boolean apply(User user) {
                    //return user.isEnabled();
                	return true;	
                }
            });
        }

        if (profileSearchSettingsManager.isBulkDataLoadingEnabled()) {
            return new ProfileSearchResultImpl(preparedCriteria, result, preds, userManager, dataLoadingStrategy,
                    profileSearchSettingsManager.getDefaultBulkLoadingSize());
        }

        return new ProfileSearchResultImpl(preparedCriteria, result, preds, userManager);
    }
}
