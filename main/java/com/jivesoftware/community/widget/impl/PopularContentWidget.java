/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.widget.impl;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.activity.popularity.PopularityManager;
import com.jivesoftware.community.annotations.PropertyNames;
import com.jivesoftware.community.eae.RecommendationManager;
import com.jivesoftware.community.eae.recommendation.RecommendationQueryHelper;
import com.jivesoftware.community.eae.rest.impl.RecommendationServiceImpl;
import com.jivesoftware.community.objecttype.ContainableType;
import com.jivesoftware.community.objecttype.JiveObjectType;
import com.jivesoftware.community.objecttype.ObjectTypeManager;
import com.jivesoftware.community.util.JiveObjectFunctions;
import com.jivesoftware.community.widget.BaseLocationFilterableWidget;
import com.jivesoftware.community.widget.WidgetCategory;
import com.jivesoftware.community.widget.WidgetCategoryMarker;
import com.jivesoftware.community.widget.WidgetContext;
import com.jivesoftware.community.widget.WidgetType;
import com.jivesoftware.community.widget.WidgetTypeMarker;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WidgetTypeMarker({WidgetType.HOMEPAGE, WidgetType.COMMUNITY, WidgetType.PROJECT, WidgetType.SOCIALGROUP,  WidgetType.CONTAINER})
@WidgetCategoryMarker({WidgetCategory.CONTENT})
@PropertyNames({"numResults", "containerType", "containerID", "visibilityMap", "converted"})
public class PopularContentWidget extends BaseLocationFilterableWidget {

    private static final String FREEMARKER_FILE = "/template/widget/popular-content.ftl";
    private static final String CONVERT_MSG_SEEN = "convert.popularcontent.closed";

    private Map<Integer, Boolean> visibilityMap = new HashMap<Integer, Boolean>();
    private String converted;

    private ObjectTypeManager objectTypeManager;
    private RecommendationServiceImpl recommendationServiceImpl;
    private RecommendationManager recommendationManager;
    private PopularityManager popularityManager;
    private RecommendationQueryHelper recommendationQueryHelper;

    public void setObjectTypeManager(ObjectTypeManager objectTypeManager) {
        this.objectTypeManager = objectTypeManager;
    }

    public void setRecommendationServiceImpl(RecommendationServiceImpl recommendationServiceImpl) {
        this.recommendationServiceImpl = recommendationServiceImpl;
    }

    public void setRecommendationManager(RecommendationManager recommendationManager) {
        this.recommendationManager = recommendationManager;
    }

    @Required
    public final void setPopularityManager(PopularityManager popularityManager) {
        this.popularityManager = popularityManager;
    }

    @Required
    public void setRecommendationQueryHelper(RecommendationQueryHelper recommendationQueryHelper) {
        this.recommendationQueryHelper = recommendationQueryHelper;
    }

    public String getTitle(WidgetContext widgetContext) {
        return getLocalizedString("widget.popularcontent.title", widgetContext);
    }

    public String getDescription(WidgetContext widgetContext) {
        return getLocalizedString("widget.popularcontent.desc", widgetContext);
    }

    public String render(WidgetContext widgetContext, ContainerSize size) {
        return applyFreemarkerTemplate(widgetContext, size, FREEMARKER_FILE);
    }

    protected Map<String, Object> loadProperties(WidgetContext widgetContext, ContainerSize size) {
        Map<String, Object> properties = super.loadProperties(widgetContext, size);

        JiveContainer container = getContainer(widgetContext);

        List recommendations = Collections.emptyList();
        if (recommendationManager.isEnabled()) {
            recommendations = loadRecommendations(properties, container);
        }

        if (recommendations.isEmpty()) {
            properties.put("popularContent", loadPopularContent(widgetContext, container));
        } else {
            properties.put("recommendations", recommendations);

        }
        properties.put("container", container);
        properties.put("widgetID", getID());
        properties.put("widgetType", widgetContext.getWidgetType().getKey());
        properties.put("widgetFrameID", getWidgetFrameID());
        properties.put("converted", getConverted());
        properties.put("visibilityMap", getVisibilityMap());
        properties.put("visibleTypes", getVisibleTypes());


        String prop = widgetContext.getUser().getProperties().get(CONVERT_MSG_SEEN);
        boolean convertMessageSeen = prop != null && prop.equals("true");
        properties.put("convertMessageClosed", convertMessageSeen);

        return properties;
    }

    private List loadRecommendations(Map<String, Object> properties, JiveContainer container) {
        Set<Integer> contentTypes = getVisibleTypes();
        Map<String, Object> recommendationRequest = recommendationServiceImpl
                .getTrendingContent(getNumResults(), container.getObjectType(), container.getID(), contentTypes, true);
        final Object recommendations = recommendationRequest.get("recommendations");
        if (recommendations instanceof List) {
            return (List) recommendations;
        } else {
            return Collections.emptyList();
        }
    }

    private Iterator<JiveContentObject> loadPopularContent(WidgetContext widgetContext, JiveContainer container) {
        Iterator<JiveContentObject> iterator;
        if (isRootCommunity(container)) {
            iterator = popularityManager.getGlobalPopularContent();
        }
        else {
            iterator = popularityManager.getPopularContent(container);
        }
        Function<JiveContentObject, Integer> typeFunction = Functions
                .compose(JiveObjectFunctions.types(), Functions.<JiveContentObject>identity());
        Predicate<JiveContentObject> filter = Predicates.compose(Predicates.in(getVisibleTypes()), typeFunction);

        return Iterators.limit(Iterators.filter(iterator, filter), getNumResults());
    }

    public Set<Integer> getRecoRequestTypes() {
        Set<ContainableType> objectTypeList = objectTypeManager.getObjectTypesByType(ContainableType.class);

        Set<Integer> listO = new HashSet<Integer>();
        for (JiveObjectType jiveObjectType : objectTypeList) {
            if (!recommendationQueryHelper.isExcludedContentType(jiveObjectType.getID())) {
                listO.add(jiveObjectType.getID());
            }
        }

        if (objectTypeManager.getObjectType(JiveConstants.BLOG) != null) {
         //   listO.add(JiveConstants.BLOGPOST);
        }
        return listO;
    }

    public Map<Integer, Boolean> getVisibilityMap() {
        Set<Integer> listO = getRecoRequestTypes();

        if (visibilityMap.size() != listO.size()) {
            visibilityMap.clear();

            for (Integer objectType : listO) {
                visibilityMap.put(objectType, !isConverted());
            }
        }
        return visibilityMap;
    }

    public Set<Integer> getVisibleTypes() {
        Map<Integer, Boolean> visibilityMap = getVisibilityMap();
        return Maps.filterValues(visibilityMap, Predicates.equalTo(Boolean.TRUE)).keySet();
    }

    public void setVisibilityMap(Map<Integer, Boolean> visibilityMap) {
        this.visibilityMap = visibilityMap;
    }

    public String getConverted() {
        return converted;
    }

    public void setConverted(String converted) {
        this.converted = converted;
    }

    public boolean isPropertyEnabled(WidgetContext widgetContext, String propName) {
        if ("converted".equals(propName)) {
            return false;
        }
        else {
            return super.isPropertyEnabled(widgetContext, propName);
        }
    }

    protected boolean isConverted() {
        return (converted != null) && (!converted.isEmpty());
    }

}
