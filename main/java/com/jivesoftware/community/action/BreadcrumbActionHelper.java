/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;

import com.jivesoftware.community.Blog;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.content.relationships.places.ContentPlaceRelationshipManager;
import com.jivesoftware.community.content.relationships.places.dao.ContentPlaceRelationshipResultFilter;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.places.rest.ContainerService;
import com.jivesoftware.community.places.rest.Place;
import com.jivesoftware.community.solution.annotations.InjectConfiguration;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.util.LocaleUtils;
import com.jivesoftware.util.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class BreadcrumbActionHelper {

    private ContainerService containerServiceImpl;
    private BreadcrumbConfiguration breadcrumbConfiguration;
    private ContentPlaceRelationshipManager contentPlaceRelationshipManager;

    @Required
    public void setContainerServiceImpl(ContainerService containerServiceImpl) {
        this.containerServiceImpl = containerServiceImpl;
    }

    @Required
    public void setContentPlaceRelationshipManager(ContentPlaceRelationshipManager contentPlaceRelationshipManager) {
        this.contentPlaceRelationshipManager = contentPlaceRelationshipManager;
    }

    @InjectConfiguration
    public void setBreadcrumbConfiguration(BreadcrumbConfiguration breadcrumbConfiguration) {
        this.breadcrumbConfiguration = breadcrumbConfiguration;
    }

    public BreadcrumbBean getBreadcrumbBean(JiveContainer container, EntityDescriptor content, Locale locale) {
        BreadcrumbBean bean = getBreadcrumbBean(container, locale);
        populatePlaceLinkInfo(content, bean);

        return bean;
    }

    public BreadcrumbBean getBreadcrumbBean(JiveContainer container, Locale locale) {
        BreadcrumbBean bean = new BreadcrumbBean();

        if (showContainerInBreadcrumbs(container)) {
            Place place = new Place(container,  false);
            place.setName(SkinUtils.getDisplayName(container));
            bean.setPlace(place);

            if (container.getObjectType() == JiveConstants.BLOG) {

                Blog blog = (Blog) container;
                // user blog should have the user container breadcrumbs (starting with people)
                if (blog.isUserBlog()) {
                    bean.setUserContainer(true);
                }
                // if the blog name is the same as the parent container name, simply call it Blog in the breadcrumbs: See CS-19298.
                JiveContainer blogContainer = ((Blog) container).getJiveContainer();
                if (blogContainer != null && blogContainer.getName().equals(container.getName())) {
                    bean.getPlace().setName(LocaleUtils.getLocalizedString("global.blog", locale));
                }
                // load the parent containers for the blog breadcrumbs
                bean.setParents(containerServiceImpl.getParentContainers(container.getObjectType(), container.getID()));

            } else if (container.getObjectType() == JiveConstants.USER_CONTAINER) {

                bean.setUserContainer(true);
                bean.setParents(Collections.<Place>emptyList());

            } else {

                bean.setUserContainer(false);
                bean.setParents(containerServiceImpl.getParentContainers(container.getObjectType(), container.getID()));

            }

            if (breadcrumbConfiguration.isUseLegacyStyle()) {
                String homeURL = JiveGlobals.getJiveProperty("skin.default.homeURL");
                if (StringUtils.isEmpty(homeURL)) {
                   // bean.setShowHomeURL(false);
                	setDefaultHome(bean);
                }
                else {
                    bean.setShowHomeURL(true);
                    bean.setFullHomeURL(homeURL.startsWith("http://") || homeURL.startsWith("https://"));
                    bean.setHomeURL(homeURL);
                }
                bean.setRootContainerName(JiveApplication.getEffectiveContext().getCommunityManager().getRootCommunity().getName());
            }
            bean.setSeparator(JiveGlobals.getJiveProperty("skin.template.defaultSeparator","&gt;"));
        }

        return bean;
    }
    private void setDefaultHome(BreadcrumbBean bean) {
        bean.setShowHomeURL(true);
        bean.setFullHomeURL(false);
        bean.setHomeURL("");
    }


    private void populatePlaceLinkInfo(EntityDescriptor content, BreadcrumbBean bean) {
        if (content != null && content.getID() != -1 && content.getObjectType() != -1L &&
                contentPlaceRelationshipManager.isContentPlaceRelationshipsEnabled()) {
            ContentPlaceRelationshipResultFilter filter = new ContentPlaceRelationshipResultFilter(content);
            int count = contentPlaceRelationshipManager.getRelationshipCount(filter);

            if (count > 0) {
                bean.setPlaceLinkCount(count);
            }
            bean.setLinkedContentID(content.getID());
            bean.setLinkedContentType(content.getObjectType());
        }
    }

    private boolean showContainerInBreadcrumbs(JiveContainer container) {
        return container != null && container.getObjectType() != JiveConstants.SYSTEM_CONTAINER && !(
                container.getObjectType() == JiveConstants.COMMUNITY && container.getID() == JiveApplication
                        .getEffectiveContext().getCommunityManager().getRootCommunity().getID());
    }

    public static class BreadcrumbBean {

        private boolean showHomeURL;
        private boolean fullHomeURL;
        private String homeURL;
        private Place place;
        private List<Place> parents;
        private boolean userContainer;
        private String separator;
        private String link;
        private Integer placeLinkCount;
        private Long linkedContentID;
        private Integer linkedContentType;
        private String rootContainerName;

        public boolean isShowHomeURL() {
            return showHomeURL;
        }

        public void setShowHomeURL(boolean showHomeURL) {
            this.showHomeURL = showHomeURL;
        }

        public boolean isFullHomeURL() {
            return fullHomeURL;
        }

        public void setFullHomeURL(boolean fullHomeURL) {
            this.fullHomeURL = fullHomeURL;
        }

        public String getHomeURL() {
            return homeURL;
        }

        public void setHomeURL(String homeURL) {
            this.homeURL = homeURL;
        }

        public Place getPlace() {
            return place;
        }

        public void setPlace(Place place) {
            this.place = place;
        }

        public List<Place> getParents() {
            return parents;
        }

        public void setParents(List<Place> parents) {
            this.parents = parents;
        }

        public boolean isUserContainer() {
            return userContainer;
        }

        public void setUserContainer(boolean userContainer) {
            this.userContainer = userContainer;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public String getSeparator() {
            return separator;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public Integer getPlaceLinkCount() {
            return placeLinkCount;
        }

        public void setPlaceLinkCount(Integer placeLinkCount) {
            this.placeLinkCount = placeLinkCount;
        }

        public Long getLinkedContentID() {
            return linkedContentID;
        }

        public void setLinkedContentID(Long linkedContentID) {
            this.linkedContentID = linkedContentID;
        }

        public Integer getLinkedContentType() {
            return linkedContentType;
        }

        public void setLinkedContentType(Integer linkedContentType) {
            this.linkedContentType = linkedContentType;
        }
        
        public String getRootContainerName() {
            return rootContainerName;
        }

        public void setRootContainerName(String rootContainerName) {
            this.rootContainerName = rootContainerName;
        }
    }
}
