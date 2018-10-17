/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.browse.filter.group;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jivesoftware.base.util.PartnerUserHelper;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.browse.BrowsableType;
import com.jivesoftware.community.browse.BrowseFilterContext;
import com.jivesoftware.community.browse.BrowseFilterProvider;
import com.jivesoftware.community.browse.BrowsePerspective;
import com.jivesoftware.community.browse.filter.ArchetypeFilter;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.filter.CompositeBrowseFilter;
import com.jivesoftware.community.browse.filter.DraftFilter;
import com.jivesoftware.community.browse.filter.ExcludeObjectTypeFilter;
import com.jivesoftware.community.browse.filter.FollowingFilter;
import com.jivesoftware.community.browse.filter.ObjectTypeFilter;
import com.jivesoftware.community.browse.filter.OutcomeFilter;
import com.jivesoftware.community.browse.filter.ParticipatedFilter;
import com.jivesoftware.community.browse.filter.PerspectiveFilter;
import com.jivesoftware.community.browse.filter.PrivateContentFilter;
import com.jivesoftware.community.browse.filter.SimpleBrowseFilter;
import com.jivesoftware.community.browse.filter.TagFilter;
import com.jivesoftware.community.eae.tile.TileStreamEntry;
import com.jivesoftware.community.favorites.FavoriteManager;
import com.jivesoftware.community.favorites.type.ExternalUrlObjectType;
import com.jivesoftware.community.history.RecentHistoryManager;
import com.jivesoftware.community.microblogging.type.WallEntryObjectType;
import com.jivesoftware.community.objecttype.ContainerType;
import com.jivesoftware.community.objecttype.ContentObjectType;
import com.jivesoftware.community.objecttype.DraftableType;
import com.jivesoftware.community.objecttype.JiveObjectType;
import com.jivesoftware.community.objecttype.ObjectTypeManager;
import com.jivesoftware.community.outcome.OutcomeManager;
import com.jivesoftware.community.outcome.OutcomeSupportedType;
import com.jivesoftware.community.outcome.OutcomeType;
import com.jivesoftware.community.work.ContentStatusFilter;
import com.jivesoftware.eae.constants.ActivityConstants;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.jivesoftware.community.browse.sort.BrowseSort;
import com.jivesoftware.community.browse.sort.MostRatedDocumentsSort;
import com.jivesoftware.community.impl.DocumentContentType;
import com.jivesoftware.community.browse.filter.MostRatedFilter;
import com.jivesoftware.community.objecttype.RatingEnabledType;

/**
 * A filter group bean for content types driven from the object type framework.
 *
 * @since Jive SBS 5.0
 */
public class ContentObjectBrowseFilterGroupProvider extends AbstractBrowseFilterGroupProvider {

    protected volatile Map<Integer, BrowseFilterProvider> draftProviders;
    protected volatile Map<Integer, BrowseFilterProvider> publishedProviders;
    protected volatile Map<Integer, BrowseFilterProvider> historyProviders;
    protected volatile Map<Integer, BrowseFilterProvider> mostRatedProviders;

    private FavoriteManager favoriteManager;
    private ObjectTypeManager objectTypeManager;
    private Comparator<OutcomeType> outcomeTypeComparator;
    private OutcomeManager outcomeManager;

    @Required
    public void setFavoriteManager(FavoriteManager favoriteManager) {
        this.favoriteManager = favoriteManager;
    }

    @Required
    public void setObjectTypeManager(ObjectTypeManager objectTypeManager) {
        this.objectTypeManager = objectTypeManager;
    }

    @Required
    public void setOutcomeTypeComparator(Comparator<OutcomeType> outcomeTypeComparator) {
        this.outcomeTypeComparator = outcomeTypeComparator;
    }

    @Required
    public void setOutcomeManager(OutcomeManager outcomeManager) {
        this.outcomeManager = outcomeManager;
    }

    public ContentObjectBrowseFilterGroupProvider(String name) {
        super(name);
    }

    @Override
    protected void buildProviders() {
        draftProviders = draftProviders();
        publishedProviders = publishedProviders();
        historyProviders = historyProviders();
        mostRatedProviders = mostRatedProviders();
    }

    @Override
    public BrowseFilterGroup getFilterGroupInstance(BrowseFilterContext context) {
        BrowseFilterGroup group = createBrowseFilterGroup(name, context);

        group.addGlobalFilter(new ExcludeObjectTypeFilter(
                JiveConstants.TASK, JiveConstants.FAVORITE, JiveConstants.WALL_ENTRY, TileStreamEntry.OBJECTID, ActivityConstants.DIRECT_MESSAGE,
                new Integer(9876), JiveConstants.CONTENT_PLACE_RELATIONSHIP, new Integer(TileStreamEntry.OBJECTID)));

        if (!authenticationProvider.getJiveUser().isAnonymous()) {

            addAuthoredFilter(addRootFiltersFromProvider(group, publishedProviders, context), group, context);

            addDraftFilter(addRootFiltersFromProvider(group, draftProviders, context), group, context);

            addParticipatedFilter(addRootFiltersFromProvider(group, publishedProviders, context), group, context);

            addFollowingFilter(addRootFiltersFromProvider(group, publishedProviders, context), group, context);

            addHistoryFilter(addRootFiltersFromProvider(group, historyProviders, context), group);
            
            addMostRatedDocumentsFilter(addRootFiltersFromProvider(group, mostRatedProviders, context), group , context);

            if (eaeConfigManager.isRecommenderEnabled()) {
                addRecommendedFilter(group);
            }
            BrowseFilter allFilter = addAllFilter(addRootFiltersFromProvider(group, publishedProviders, context), group, context);

            group.setDefaultFilter(allFilter, false);

        } else {

            addRecommendedFilter(group);

            BrowseFilter allFilter = addAllFilter(addRootFiltersFromProvider(group, publishedProviders, context), group, context);

            group.setDefaultFilter(allFilter, false);
        }

        return group;
    }
    
    protected CompositeBrowseFilter addMostRatedDocumentsFilter(Set<BrowseFilter> children, BrowseFilterGroup group,
            BrowseFilterContext context) 
    {
        if (!authenticationProvider.getJiveUser().isAnonymous() || context.getUserID() > 0) {
        	CompositeBrowseFilter ratedFilter = new MostRatedFilter("browse.filter.mostrated");
            group.addFilter(ratedFilter);
            ratedFilter.addChild(new SimpleBrowseFilter("choose", "browse.filter.choose"));
            CompositeBrowseFilter contentParentFilter = new CompositeBrowseFilter("objecttype", "browse.filter.contenttype");
            contentParentFilter.setExclusive(false);
            ratedFilter.addChild(contentParentFilter);
            CompositeBrowseFilter showAllContent = new CompositeBrowseFilter("showall","browse.filter.content.showall");
            contentParentFilter.addChild(showAllContent);
            contentParentFilter.addChildren(children);
            addSubFiltersToObjectTypeFilterChildren(showAllContent, children, context);
            ratedFilter.addChild(new TagFilter("browse.filter.tag"));
            //draftFilter.addChild(new PrivateContentFilter("browse.filter.private"));
            ratedFilter.setSupportedObjectTypeIDs(Sets.<Integer>newHashSet(JiveConstants.DOCUMENT));
          //addDefaultSorts(draftFilter);
            ratedFilter.addSort(new MostRatedDocumentsSort("browse.sorts.by_rating.desc", BrowseSort.SortOrder.DESCENDING), true);
            return ratedFilter;
        } else {
            return null;
        }
    }

    protected CompositeBrowseFilter addDraftFilter(Set<BrowseFilter> children, BrowseFilterGroup group,
            BrowseFilterContext context) {
        if (!authenticationProvider.getJiveUser().isAnonymous() || context.getUserID() > 0) {
            CompositeBrowseFilter draftFilter = new DraftFilter("browse.filter.drafts");
            group.addFilter(draftFilter);
            draftFilter.addChild(new SimpleBrowseFilter("choose", "browse.filter.choose"));
            CompositeBrowseFilter contentParentFilter = new CompositeBrowseFilter("objecttype", "browse.filter.contenttype");
            contentParentFilter.setExclusive(false);
            draftFilter.addChild(contentParentFilter);
            CompositeBrowseFilter showAllContent = new CompositeBrowseFilter("showall","browse.filter.content.showall");
            contentParentFilter.addChild(showAllContent);
            contentParentFilter.addChildren(children);
            addSubFiltersToObjectTypeFilterChildren(showAllContent, children, context);
            draftFilter.addChild(new TagFilter("browse.filter.tag"));
            //draftFilter.addChild(new PrivateContentFilter("browse.filter.private"));
            draftFilter.setSupportedObjectTypeIDs(Sets.<Integer>newHashSet(draftProviders.keySet()));
            addDefaultSorts(draftFilter);
            return draftFilter;
        } else {
            return null;
        }
    }

    protected CompositeBrowseFilter addFollowingFilter(Set<BrowseFilter> children, BrowseFilterGroup group,
            BrowseFilterContext context) {
        if (!authenticationProvider.getJiveUser().isAnonymous() || context.getUserID() > 0) {
            CompositeBrowseFilter followingFilter = new FollowingFilter();
            group.addFilter(followingFilter);
            followingFilter.addChild(new SimpleBrowseFilter("choose", "browse.filter.choose"));
            CompositeBrowseFilter contentParentFilter = new CompositeBrowseFilter("objecttype", "browse.filter.contenttype");
            contentParentFilter.setExclusive(false);
            followingFilter.addChild(contentParentFilter);
            CompositeBrowseFilter showAllContent = new CompositeBrowseFilter("showall","browse.filter.content.showall");
            contentParentFilter.addChild(showAllContent);
            contentParentFilter.addChildren(children);
            addSubFiltersToObjectTypeFilterChildren(showAllContent, children, context);
            followingFilter.addChild(new TagFilter("browse.filter.tag"));
            //followingFilter.addChild(new PrivateContentFilter("browse.filter.private"));
            followingFilter.setSupportedObjectTypeIDs(Sets.<Integer>newHashSet(publishedProviders.keySet()));
            addDefaultSorts(followingFilter);
            return followingFilter;
        } else {
            return null;
        }
    }

    protected CompositeBrowseFilter addParticipatedFilter(Set<BrowseFilter> children, BrowseFilterGroup group,
            BrowseFilterContext context)
    {
        if (!authenticationProvider.getJiveUser().isAnonymous() || context.getUserID() > 0) {
            CompositeBrowseFilter participatedFilter = new ParticipatedFilter();
            group.addFilter(participatedFilter);
            participatedFilter.addChild(new SimpleBrowseFilter("choose", "browse.filter.choose"));
            CompositeBrowseFilter contentParentFilter = new CompositeBrowseFilter("objecttype",
                    "browse.filter.contenttype");
            contentParentFilter.setExclusive(false);
            participatedFilter.addChild(contentParentFilter);
            CompositeBrowseFilter showAllContent = new CompositeBrowseFilter("showall","browse.filter.content.showall");
            contentParentFilter.addChild(showAllContent);
            contentParentFilter.addChildren(children);
            addSubFiltersToObjectTypeFilterChildren(showAllContent, children, context);
            participatedFilter.addChild(new TagFilter("browse.filter.tag"));
            //participatedFilter.addChild(new PrivateContentFilter("browse.filter.private"));
            participatedFilter.setSupportedObjectTypeIDs(Sets.<Integer>newHashSet(publishedProviders.keySet()));
            addDefaultSorts(participatedFilter);
            return participatedFilter;
        } else {
            return null;
        }

    }

    protected CompositeBrowseFilter addAuthoredFilter(Set<BrowseFilter> children, BrowseFilterGroup group,
            BrowseFilterContext context) {
        CompositeBrowseFilter authoredFilter = new ContentStatusFilter("browse.filter.authored",
                JiveContentObject.Status.PUBLISHED, JiveContentObject.Status.ABUSE_VISIBLE);
        // The below is needed for backwards compatibility
        authoredFilter.setId("contentstatus[published]");
        group.addFilter(authoredFilter);
        SimpleBrowseFilter chooseFilter = new SimpleBrowseFilter("choose", "browse.filter.choose");
        authoredFilter.addChild(chooseFilter);
        CompositeBrowseFilter contentParentFilter = new CompositeBrowseFilter("objecttype", "browse.filter.contenttype");
        contentParentFilter.setExclusive(false);
        authoredFilter.addChild(contentParentFilter);
        CompositeBrowseFilter showAllContent = new CompositeBrowseFilter("showall","browse.filter.content.showall");
        contentParentFilter.addChild(showAllContent);
        contentParentFilter.addChildren(children);
        addSubFiltersToObjectTypeFilterChildren(showAllContent, children, context);
        authoredFilter.addChild(new TagFilter("browse.filter.tag"));
        // authoredFilter.addChild(new PrivateContentFilter("browse.filter.private"));
        authoredFilter.setSupportedObjectTypeIDs(Sets.<Integer> newHashSet(publishedProviders.keySet()));
        addDefaultSorts(authoredFilter);
        return authoredFilter;
    }

    protected PerspectiveFilter addAllFilter(Set<BrowseFilter> children, BrowseFilterGroup group, BrowseFilterContext context) {
        PerspectiveFilter allFilter = new PerspectiveFilter("browse.filter.all", BrowsePerspective.ALL);
        ContentStatusFilter publishedFilter = new ContentStatusFilter(JiveContentObject.Status.PUBLISHED, JiveContentObject.Status.ABUSE_VISIBLE);
        // The below is needed for backwards compatibility
        publishedFilter.setId("contentstatus[published]");
        publishedFilter.setBindUser(false);
        allFilter.addBoundFilter(publishedFilter);
        group.addFilter(allFilter);
        allFilter.addChild(new SimpleBrowseFilter("choose", "browse.filter.choose"));
        group.setAllViewFilter(allFilter);
        CompositeBrowseFilter contentParentFilter = new CompositeBrowseFilter("objecttype", "browse.filter.contenttype");
        contentParentFilter.setExclusive(false);
        allFilter.addChild(contentParentFilter);
        CompositeBrowseFilter showAllContent = new CompositeBrowseFilter("showall","browse.filter.content.showall");
        contentParentFilter.addChild(showAllContent);
        contentParentFilter.addChildren(children);
        addSubFiltersToObjectTypeFilterChildren(showAllContent, children, context);
        allFilter.addChild(new TagFilter("browse.filter.tag"));
        //allFilter.addChild(new PrivateContentFilter("browse.filter.private"));
        allFilter.setSupportedObjectTypeIDs(Sets.<Integer>newHashSet(publishedProviders.keySet()));
        addDefaultSorts(allFilter);
        return allFilter;
    }

    protected ArchetypeFilter addHistoryFilter(Set<BrowseFilter> children, BrowseFilterGroup group) {
        ArchetypeFilter historyFilter = ArchetypeFilter.build(RecentHistoryManager.HistoryArchetype.Content);
        historyFilter.setDescription("browse.filter.history");
        group.addFilter(historyFilter);
        historyFilter.addChild(new SimpleBrowseFilter("choose", "browse.filter.choose"));
        group.setAllViewFilter(historyFilter);
        CompositeBrowseFilter contentParentFilter = new CompositeBrowseFilter("objecttype", "browse.filter.contenttype");
        historyFilter.addChild(contentParentFilter);
        contentParentFilter.addChild(new SimpleBrowseFilter("showall","browse.filter.content.showall"));
        //remove children of object type filters for history
        for (BrowseFilter child : children) {
            if (child instanceof CompositeBrowseFilter) {
                ((CompositeBrowseFilter)child).removeChildren();
            }
        }
        contentParentFilter.addChildren(children);
        historyFilter.setSupportedObjectTypeIDs(Sets.<Integer>newHashSet(historyProviders.keySet()));
        return historyFilter;
    }

    protected void addSubFiltersToObjectTypeFilterChildren(CompositeBrowseFilter allFilter,
            Collection<BrowseFilter> filters, BrowseFilterContext context) {
        if (!authenticationProvider.getJiveUser().isAnonymous()) {
            Set<JiveObjectType> allJots = Sets.newHashSet();
            for (BrowseFilter filter : filters) {
                //document, polls, discussions
                if ((filter instanceof ObjectTypeFilter)) {
                    Set<Integer> typeIDs = ((ObjectTypeFilter) filter).getObjectTypeIDs();
                    boolean addPrivateContentFilter = (
                        typeIDs.contains(JiveConstants.DOCUMENT) ||
                        typeIDs.contains(JiveConstants.POLL) ||
                        typeIDs.contains(JiveConstants.THREAD)
                    );
                    long userID = context.getUserID();
                    if (typeIDs.contains(JiveConstants.POLL) &&
                            !(objectTypeManager.getObjectType(JiveConstants.POLL) instanceof OutcomeSupportedType)) {
                        // Can safely remove this when polls become an "OutcomeSupportedType"
                        for (BrowseFilter browseFilter : ((ObjectTypeFilter) filter).getChildren()) {
                            if (browseFilter instanceof CompositeBrowseFilter) {
                                ((CompositeBrowseFilter)browseFilter).addChild(
                                        new PrivateContentFilter("browse.filter.private", userID));
                            }
                        }
                    }
                    for (Integer typeID : typeIDs) {
                        JiveObjectType jot = objectTypeManager.getObjectType(typeID);
                        if (jot instanceof OutcomeSupportedType && outcomeManager.getConfiguration().isOutcomesEnabled()) {
                            Set<JiveObjectType> jots = Sets.newHashSet();
                            jots.add(jot);
                            allJots.add(jot);
                            //iterate children and add as a child to each child option
                            if (((ObjectTypeFilter) filter).hasChildren()) {
                                for (BrowseFilter browseFilter : ((ObjectTypeFilter) filter).getChildren()) {
                                    if (browseFilter instanceof CompositeBrowseFilter) {
                                        ((CompositeBrowseFilter)browseFilter).addChild(new OutcomeFilter(
                                            "browse.filter.outcome",
                                            jots,
                                            false,
                                            addPrivateContentFilter,
                                            false,
                                            userID,
                                            outcomeTypeComparator,
                                            objectTypeManager
                                        ));
                                    }
                                }
                            }
                            else {
                                ((ObjectTypeFilter) filter).addChild(new OutcomeFilter(
                                    "browse.filter.outcome",
                                    jots,
                                    true,
                                    addPrivateContentFilter,
                                    false,
                                    userID,
                                    outcomeTypeComparator,
                                    objectTypeManager
                                ));
                            }
                            break;
                        }
                    }
                }
            }
            if (allJots.size() > 0 && outcomeManager.getConfiguration().isOutcomesEnabled()) {
                allFilter.addChild(new OutcomeFilter(
                    "browse.filter.outcome",
                    allJots,
                    true,
                    false,
                    false,
                    context.getUserID(),
                    outcomeTypeComparator,
                    objectTypeManager
                ));
            }
        }
    }

    /**
     * Returns a list of filter providers for filtering by content types that support draft.
     */
    private Map<Integer, BrowseFilterProvider> draftProviders() {
        Map<Integer, BrowseFilterProvider> providers = Maps.newLinkedHashMap();
        Set<ContentObjectType> types = objectTypeManager.getObjectTypesByType(ContentObjectType.class);
        for (ContentObjectType type : types) {
            if (type instanceof BrowsableType && type instanceof DraftableType && !(type instanceof ContainerType)
                    && type.isEnabled())
            {
                BrowseFilterProvider filterProvider = ((BrowsableType) type).getBrowseFilterProvider();
                if (filterProvider != null) {
                    providers.put(type.getID(), filterProvider);
                }
            }
        }
        return providers;
    }

    /**
     * Returns a list of filter providers for filtering by content types that support draft.
     */
    private Map<Integer, BrowseFilterProvider> mostRatedProviders() {
        Map<Integer, BrowseFilterProvider> providers = Maps.newLinkedHashMap();
        Set<ContentObjectType> types = objectTypeManager.getObjectTypesByType(ContentObjectType.class);
        for (ContentObjectType type : types) {
            if (type instanceof DocumentContentType && type instanceof BrowsableType && type instanceof RatingEnabledType && !(type instanceof ContainerType)
                    && type.isEnabled())
            {
                BrowseFilterProvider filterProvider = ((BrowsableType) type).getBrowseFilterProvider();
                if (filterProvider != null) {
                    providers.put(type.getID(), filterProvider);
                }
            }
        }
        return providers;
    }
    /**
     * Returns a list of filter providers for filtering by content types that support published status.
     */
    private Map<Integer, BrowseFilterProvider> publishedProviders() {
        Map<Integer, BrowseFilterProvider> providers = Maps.newLinkedHashMap();
        Set<ContentObjectType> types = objectTypeManager.getObjectTypesByType(ContentObjectType.class);
        for (ContentObjectType type : types) {
            if (type instanceof BrowsableType && !(type instanceof ContainerType) && type.isEnabled()) {
                BrowseFilterProvider filterProvider = ((BrowsableType) type).getBrowseFilterProvider();
                if (filterProvider != null) {
                    providers.put(type.getID(), filterProvider);
                }
            }
        }
        return providers;
    }

    /**
     * Returns a list of filters for filtering by content type.  Returns new filters with new ids on each invocation.
     */
    private Map<Integer, BrowseFilterProvider> historyProviders() {

        Map<Integer, BrowseFilterProvider> providers = new LinkedHashMap<Integer, BrowseFilterProvider>();
        Set<JiveObjectType> types = Sets.newLinkedHashSet();
        types.addAll(objectTypeManager.getObjectTypesByType(ContentObjectType.class));
        for (JiveObjectType type : types) {
            if (type instanceof BrowsableType && !(type instanceof ContainerType) && type.isEnabled()) {
                BrowseFilterProvider filterProvider = ((BrowsableType) type).getBrowseFilterProvider();
                if (filterProvider != null) {
                    providers.put(type.getID(), filterProvider);
                }
            }
        }
        //add in wall entries ad-hoc since they're not officially browsable
        final int wallEntryID = JiveConstants.WALL_ENTRY;
        WallEntryObjectType wallEntryObjectType = (WallEntryObjectType) objectTypeManager.getObjectType(wallEntryID);
        if (wallEntryObjectType != null && wallEntryObjectType.isEnabled()) {
            providers.put(wallEntryID, new BrowseFilterProvider() {
                @Override
                public BrowseFilter getBrowseFilter(BrowseFilterGroup group, BrowseFilterContext context) {
                    return new ObjectTypeFilter("browse.filter.wallentry.type", ImmutableSet.of(wallEntryID));
                }
            });
        }

        //add in external URLs as they're not "content objects" strangely
        if (favoriteManager.isExternalFavoritingEnabled() && !PartnerUserHelper.isAuthenticatedUserPartner(authenticationProvider)) {
            final int externalUrlID = JiveConstants.EXTERNAL_URL;
            ExternalUrlObjectType externalUrlObjectType = (ExternalUrlObjectType) objectTypeManager.getObjectType(externalUrlID);
            if (externalUrlObjectType != null && externalUrlObjectType.isEnabled()) {
                providers.put(externalUrlID, new BrowseFilterProvider() {
                    @Override
                    public BrowseFilter getBrowseFilter(BrowseFilterGroup group, BrowseFilterContext context) {
                        return new ObjectTypeFilter("browse.filter.externalurl.type", ImmutableSet.of(externalUrlID));
                    }
                });
            }
        }

        return providers;
    }

}

