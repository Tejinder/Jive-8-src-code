/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.work.rest.impl;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObjectLoader;
import com.jivesoftware.community.browse.BrowseFilterContext;
import com.jivesoftware.community.browse.BrowseFilterManager;
import com.jivesoftware.community.browse.BrowseIterator;
import com.jivesoftware.community.browse.BrowseManager;
import com.jivesoftware.community.browse.filter.ArchetypeFilter;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.filter.CompositeBrowseFilter;
import com.jivesoftware.community.browse.filter.ContainerFilter;
import com.jivesoftware.community.browse.filter.DraftFilter;
import com.jivesoftware.community.browse.filter.SearchBrowseFilter;
import com.jivesoftware.community.browse.filter.UserFilter;
import com.jivesoftware.community.browse.filter.group.BrowseFilterGroup;
import com.jivesoftware.community.browse.impl.BrowseTokenUtil;
import com.jivesoftware.community.browse.rest.ItemBean;
import com.jivesoftware.community.browse.rest.ItemBeanBuilder;
import com.jivesoftware.community.browse.rest.ItemBeanPropertyProvider;
import com.jivesoftware.community.browse.rest.impl.ItemsViewBean;
import com.jivesoftware.community.browse.rest.impl.JiveContentObjectItemBean;
import com.jivesoftware.community.browse.rest.impl.JiveObjectAuthorProviderImpl;
import com.jivesoftware.community.browse.rest.impl.SearchResultsInfo;
import com.jivesoftware.community.browse.sort.BrowseSort;
import com.jivesoftware.community.browse.sort.MostRatedDocumentsSort;
import com.jivesoftware.community.content.move.Capabilities;
import com.jivesoftware.community.content.move.MoveContentResponse;
import com.jivesoftware.community.content.move.MoveContentSupport;
import com.jivesoftware.community.content.relationships.places.ContentPlaceRelationship;
import com.jivesoftware.community.content.relationships.places.browse.ContentPlaceRelationshipInfoProviderImpl;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.places.rest.PlaceItemBeanParentInfoProviderImpl;
import com.jivesoftware.community.user.rest.HistoryService;
import com.jivesoftware.community.webservices.rest.ErrorBuilder;
import com.jivesoftware.community.work.ContentStatusFilter;
import com.jivesoftware.community.work.rest.ContentService;
import com.jivesoftware.community.work.rest.EntityDescriptorBean;
import com.jivesoftware.community.work.rest.UpdateEntityDescriptorBean;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.*;
import com.jivesoftware.community.browse.filter.MostRatedFilter;
import  com.jivesoftware.community.browse.rest.impl.ItemRatingBean;

/**
 * Implementation of work service.
 *
 * @since Jive SBS 5.0
 */
public class ContentServiceImpl extends RemoteSupport implements ContentService {

    static Logger log = LogManager.getLogger(ContentServiceImpl.class);

    private BrowseManager browseManager;
    private BrowseFilterManager browseFilterManager;
    private MoveContentSupport moveContentSupport;
    private HistoryService historyService;
    private BrowseTokenUtil browseTokenUtil;

    private ItemBeanBuilder<JiveContentObject, JiveContentObjectItemBean> contentItemBeanBuilder;

    private Collection<String> contentObjectProperties = JiveContentObjectItemBean.DEFAULT_CONTENT_OBJECT_BROWSE_VIEW_PROPERTIES;

    @Required
    public void setBrowseManager(BrowseManager browseManager) {
        this.browseManager = browseManager;
    }

    @Required
    public void setBrowseFilterManager(BrowseFilterManager browseFilterManager) {
        this.browseFilterManager = browseFilterManager;
    }

    @Required
    public void setHistoryService(HistoryService recentHistoryService) {
        this.historyService = recentHistoryService;
    }

    @Required
    public void setMoveContentSupport(MoveContentSupport moveContentSupport) {
        this.moveContentSupport = moveContentSupport;
    }

    @Required
    public void setContentItemBeanBuilder(
            ItemBeanBuilder<JiveContentObject, JiveContentObjectItemBean> contentItemBeanBuilder)
    {
        this.contentItemBeanBuilder = contentItemBeanBuilder;
    }

    @Required
    public void setBrowseTokenUtil(BrowseTokenUtil browseTokenUtil) {
        this.browseTokenUtil = browseTokenUtil;
    }

    public void setContentObjectItemBeanProps(Collection<String> contentObjectProperties) {
        this.contentObjectProperties = contentObjectProperties;
    }

    public ItemsViewBean<JiveContentObjectItemBean> getContent(String userIDStr, int containerType, long containerID,
            String query, String filterGroupID, Collection<String> filterIDs, String sortKey, int sortOrder, int start,
            int numResults, long activityTime, String token) {

        long userID = browseFilterManager.getUserIDFromString(userIDStr);

        if (isWildCardEnabled() && StringUtils.isNotEmpty(query) && !query.endsWith("*")) {
            query = query + "*";
        }

        Map<String, Object> headerColumns = com.google.common.collect.Maps.newHashMap();

        BrowseFilterContext.Builder context = new BrowseFilterContext.Builder();
        context.setUserID(userID);
        if (containerType > ContainerFilter.UNSET_CONTAINER_VALUE && containerID > ContainerFilter.UNSET_CONTAINER_VALUE) {
            context.setContainerType(containerType);
            context.setContainerID(containerID);
            headerColumns.put(PlaceItemBeanParentInfoProviderImpl.PROPERTY_NAME, false);
        } else {
            headerColumns.put(PlaceItemBeanParentInfoProviderImpl.PROPERTY_NAME, true);
        }
        context.setFilterIDs(filterIDs);
        context.setSortKey(sortKey);
        context.setSortOrder(sortOrder);
        context.setQuery(query);

        BrowseFilterGroup filterGroup = browseFilterManager.getFilterGroup(filterGroupID, context.build());

        Set<BrowseFilter> filters = filterGroup.getAppliedFilters();
        if (StringUtils.isNotEmpty(query)) {
               filters.add(new SearchBrowseFilter(query, getLocale(), getTimeZone()));
        }
        
        boolean containsMostRatedFilter = containsMostRatingFilter(filters);

        BrowseSort sort = filterGroup.getAppliedSort();

        for (BrowseFilter filter : filters) {
            if (filter instanceof ArchetypeFilter) {
                return historyService.getRecentHistory(userID, filterGroupID, filterIDs, query, start, numResults, activityTime);
            }
        }

        // NOTE(darr): this is really a horrible hack, but the concept of version isn't something which is present
        // in core content API.  So instead, if somebody has asked for drafts, we give them drafts.
        Function<JiveContentObject, JiveContentObject> postProcessor = Functions.identity();
        if (!Iterables.isEmpty(Iterables.filter(filters, DraftFilter.class))) {
            postProcessor = new LoadDraftDocument();
        }

        Iterator<JiveContentObject> iterator = null;
        ItemsViewBean<JiveContentObjectItemBean> itemsViewBean;
        //guard browse service against DOS attacks...
        numResults = browseTokenUtil.boundNumberOfResults(numResults);
        if (browseTokenUtil.isTokenValid(token, start + numResults, browseTokenUtil.getMaxContentStartIndex())) {
            iterator = browseManager.getContent(filters, sort, start, numResults + 1);
            Set<JiveContentObjectItemBean> contentItems = Sets.newLinkedHashSet();
            Map<String, Object> additionalProps = Maps.<String, Object>newHashMap();
            additionalProps.put(ItemBeanPropertyProvider.ADDITIONAL_CONTEXT_APPLIED_FILTERS, filters);

            if (containsAuthoredFilter(filters)) {
                // tells author provider to use the item's creator as its author
                additionalProps.put(JiveObjectAuthorProviderImpl.ADDITIONAL_CONTEXT_SHOW_CREATOR, true);
                // tells the template to display "created by" message
                headerColumns.put(JiveObjectAuthorProviderImpl.ADDITIONAL_CONTEXT_SHOW_CREATOR, true);
            }
        
            if (iterator != null) {
                iterator = Iterators.transform(iterator, postProcessor);
                Collection<JiveContentObject> contentObjects = Lists.newLinkedList();
                while (iterator.hasNext() && contentObjects.size() < numResults) {
                   // contentObjects.add(iterator.next());
                	JiveContentObject jiveContentObject = iterator.next();
                    contentObjects.add(jiveContentObject);
                }
                if (postProcessor instanceof LoadContentPlaceRelContentObject) {
                    additionalProps.put(ContentPlaceRelationshipInfoProviderImpl.PROPERTY_NAME,
                            ((LoadContentPlaceRelContentObject) postProcessor).getContentEntityToPlaceRelationshipMap());
                }
                contentItems.addAll(contentItemBeanBuilder.build(contentObjects, authenticationProvider.getJiveUser(),
                        contentObjectProperties, additionalProps));
            }
            if(containsMostRatedFilter) {
                List items = new ArrayList();
                items.addAll(Arrays.asList(contentItems.toArray()));
                Collections.sort(items, new MostRatedRateSortComparator());
                itemsViewBean = new ItemsViewBean<JiveContentObjectItemBean>(new LinkedHashSet<JiveContentObjectItemBean>(items));
            } else {
            	itemsViewBean = new ItemsViewBean<JiveContentObjectItemBean>(contentItems);
            }
        }
        else {
            itemsViewBean = new ItemsViewBean<JiveContentObjectItemBean>(Sets.<JiveContentObjectItemBean>newHashSet());
            itemsViewBean.setMaxPageReached(true);
        }

        itemsViewBean.setItemGridDetailsHeaderTemplate("jive.browse.content.detailContentHeader");
        itemsViewBean.setPageNumber(numResults > 0 ? (start / numResults) + 1 : 1);
        itemsViewBean.setPageSize(numResults);
        boolean hasNext = iterator != null && iterator.hasNext();
        itemsViewBean.setHasNext(hasNext);
        itemsViewBean.setSort(sort);
        itemsViewBean.setToken(browseTokenUtil.getToken(start + numResults));

        Map<String, Object> resultMap = Maps.newHashMap();
        itemsViewBean.getProp().put("resultCounts", resultMap);
        applyResultsCountToProperties(resultMap, filterGroup.getFilters());

        if (sort != null && sort.getRootKey() != null) {
            headerColumns.put(sort.getRootKey(), true);
        }
        itemsViewBean.setItemGridDetailsColumns(headerColumns);
        if (StringUtils.isNotEmpty(query) && iterator != null && iterator instanceof BrowseIterator
                && ((BrowseIterator)iterator).isMoreSearchResultsAvailable() && !hasNext) {
            itemsViewBean.setSearchResultsInfo(new SearchResultsInfo("content", query));
        }
        return itemsViewBean;
    }

    private boolean isWildCardEnabled() {
        return JiveGlobals.getJiveBooleanProperty("search.wildcard.info.enabled", true);
    }

    private void applyResultsCountToProperties(Map<String, Object> resultMap, Set<BrowseFilter> filters) {
        for (BrowseFilter filter : filters) {
            String effectiveId = filter.getEffectiveId();
            resultMap.put(effectiveId, filter.getResultCount());
            if (filter instanceof CompositeBrowseFilter) {
                CompositeBrowseFilter composite = (CompositeBrowseFilter) filter;
                applyResultsCountToProperties(resultMap, composite.getChildren());
            }
        }
    }

    @Override
    public Capabilities getCapabilities(int objectType, long objectID, int containerType, long containerID) {
        return moveContentSupport.getCapabilities(objectType, objectID, containerType, containerID, getUser());
    }

    @Override
    public ItemBean update(int objectType, long objectID, UpdateEntityDescriptorBean updateBean) {
        EntityDescriptorBean container = new EntityDescriptorBean(updateBean.getObjectType(), updateBean.getObjectID());
        MoveContentResponse response = moveContentSupport.move(objectType, objectID, container, updateBean.isNotifyStreams());
        if (response.isMoveSuccess()) {
            return response.getItemBean();
        }
        else {
            String key = "content.mv.err.no_move_auth.text";
            if (response.getFailureKey() != null) {
                key = response.getFailureKey();
            }
            throw ErrorBuilder.forbidden(ErrorBuilder.ERROR_CODE_UNKOWN, getText(key));
        }
    }

    private boolean containsAuthoredFilter(Set<BrowseFilter> filters) {
        if (filters != null) {
            for (BrowseFilter filter : filters) {
                if (filter instanceof ContentStatusFilter &&
                        ((ContentStatusFilter)filter).getUserID() != UserFilter.UNSET_USER_VALUE) {
                    return true;
                }
            }
        }

        return false;
    }

    private static class LoadDraftDocument implements Function<JiveContentObject,JiveContentObject>{
        @Override
        public JiveContentObject apply(@Nullable JiveContentObject input) {
            if (input instanceof Document) {
                Document document = (Document) input;
                return document.getVersionManager().getNewestDocumentVersion().getDocument();
            } else {
                return input;
            }
        }
    }

    private static class LoadContentPlaceRelContentObject implements Function<JiveContentObject,JiveContentObject> {
        private JiveObjectLoader jiveObjectLoader;
        Map<EntityDescriptor, ContentPlaceRelationship> contentEntityToPlaceRelMap = Maps.newHashMap();

        private LoadContentPlaceRelContentObject(JiveObjectLoader jiveObjectLoader) {
            this.jiveObjectLoader = jiveObjectLoader;

        }

        public Map<EntityDescriptor, ContentPlaceRelationship> getContentEntityToPlaceRelationshipMap() {
            return contentEntityToPlaceRelMap;
        }

        @Override
        public JiveContentObject apply(@Nullable JiveContentObject input) {
            if (input instanceof ContentPlaceRelationship) {
                ContentPlaceRelationship relationship = (ContentPlaceRelationship) input;
                EntityDescriptor contentEntity = relationship.getContentObject();
                contentEntityToPlaceRelMap.put(contentEntity, relationship);
                try {
                    return jiveObjectLoader.getJiveContentObject(contentEntity.getObjectType(), contentEntity.getID());
                } catch (Exception ignore) {
                }
            }
            return input;
        }
    }
    
    private boolean containsMostRatingFilter(Set<BrowseFilter> filters) {
        if (filters != null) {
            for (BrowseFilter filter : filters) {
                if (filter instanceof MostRatedFilter) {
                    return true;
                }
            }
        }
        return false;
    }

    public class MostRatedRateSortComparator implements Comparator<JiveContentObjectItemBean> {
        @Override
        public int compare(JiveContentObjectItemBean o1, JiveContentObjectItemBean o2) {
            int retVal = 0;
            Map prop1 = o1.getProp();
            Map prop2 = o2.getProp();

            ItemRatingBean itemRatingBean1 = (ItemRatingBean) prop1.get("ratingInfo");
            ItemRatingBean itemRatingBean2 = (ItemRatingBean) prop2.get("ratingInfo");

            double meanRating1 = itemRatingBean1.getMeanRating();
            double meanRating2 = itemRatingBean2.getMeanRating();

            int viewCount1 = Integer.parseInt(prop1.get("viewCount").toString());
            int viewCount2 = Integer.parseInt(prop2.get("viewCount").toString());

            long lastActivityDate1 = Long.parseLong(prop1.get("lastActivityDate").toString());
            long lastActivityDate2 = Long.parseLong(prop2.get("lastActivityDate").toString());

            if(meanRating2 > meanRating1) {
                retVal = 1;
            } else if(meanRating1 == meanRating2 && viewCount2 > viewCount1) {
                retVal = 1;
            } else if(meanRating1 == meanRating2 && viewCount1 == viewCount2
                    && lastActivityDate2 > lastActivityDate1) {
                retVal = 1;
            }  else {
                retVal = -1;
            }
            return retVal;
        }
    }
}
