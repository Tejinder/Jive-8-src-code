/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.welcome;

import com.jivesoftware.api.core.v3.converters.tiles.PageEntityConverter;
import com.jivesoftware.api.core.v3.entities.tiles.PageEntity;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.ImageContainerResource;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.aaa.authz.SystemExecutor;
import com.jivesoftware.community.action.HomeNavHelper;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.announcements.Announcement;
import com.jivesoftware.community.announcements.AnnouncementManager;
import com.jivesoftware.community.announcements.AnnouncementViewBean;
import com.jivesoftware.community.announcements.authz.AnnouncementPermHelper;
import com.jivesoftware.community.content.announcement.AnnouncementActionHelper;
import com.jivesoftware.community.integration.tilepage.DefaultMobileHomePageHelper;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.places.rest.Place;
import com.jivesoftware.community.places.rest.impl.PlaceViewBean;
import com.jivesoftware.community.solution.annotations.InjectConfiguration;
import com.jivesoftware.community.spaces.action.util.CreateCommunityActionHelper;
import com.jivesoftware.community.util.JiveContainerPermHelper;
import com.jivesoftware.community.web.soy.SoyModelDriven;
import com.jivesoftware.community.web.soy.i18n.bundles.I18nResourceBundler;
import com.jivesoftware.community.widget.HomepageWidgetContext;
import com.jivesoftware.community.widget.WidgetContext;
import com.jivesoftware.community.widget.WidgetFrame;
import com.jivesoftware.community.widget.WidgetLayout;
import com.jivesoftware.community.widget.WidgetManager;
import com.jivesoftware.community.widget.presentation.WidgetPresentationConfigurator;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Sets up a view to show the user's home page in SBS.
 * 
 * @since Jive SBS 5.0
 */
public class WelcomeAction extends JiveActionSupport implements SoyModelDriven {

    static Logger log = LogManager.getLogger(WelcomeAction.class);

    public static String CUSTOMIZE_MSG_SEEN = "jive.customizeWidgetMsg.closed";
    public static String LAYOUT_UPGRADE_PROPERTY = "jive.upgrade.widgetLayoutInvalid";

    protected String path = "welcome";
    
    protected Iterable<Announcement> announcements;
    boolean pageCustomized;
    boolean customizeMessageSeen;
    protected Object model;

    protected WidgetManager widgetManager;
    protected AnnouncementManager announcementManager;
    protected CreateCommunityActionHelper createCommunityActionHelper;
    protected HomeNavHelper homeNavHelper;
    private WelcomeConfiguration welcomeConfiguration;
    private WidgetPresentationConfigurator widgetPresentationConfigurator;
    private AnnouncementActionHelper announcementActionHelper;
    private DefaultMobileHomePageHelper defaultMobileHomePageHelper;
    private PageEntityConverter pageEntityConverter;
    private I18nResourceBundler i18nResourceBundler;

    private boolean customizeNow;
    private boolean dynamic;

    public void setCustomizeNow(boolean customizeNow) {
        this.customizeNow = customizeNow;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        if (dynamic) {
            this.path = "welcome-dynamic";
        }
    }

    public boolean isVisible() {
        return welcomeConfiguration.isVisible();
    }

    public String execute() {
        if (!isVisible()) {
            return NOTFOUND;
        }

        Community container = communityManager.getRootCommunity();
        setContainer(container);
        String widgetMarkup = "";

        pageCustomized = isHasCustomized();
        String prop = getUser().getProperties().get(CUSTOMIZE_MSG_SEEN);
        customizeMessageSeen = prop != null && prop.equals("true");

        WidgetLayout widgetLayout = getWidgetLayout();
        WidgetContext widgetContext = getWidgetContext();
        if (widgetLayout != null) {
            widgetMarkup = widgetLayout.applyFreemarkerTemplate(widgetContext, getWidgetFrames(), dynamic);
        }

        String imageURL = null;
        if (getContainer() instanceof ImageContainerResource) {
            imageURL = ((ImageContainerResource)getContainer()).getContainerImageURL(2);
        }

        PageEntity defaultMobileHomePage =
                pageEntityConverter.convertForUI(defaultMobileHomePageHelper.getDefaultTilePage(),
                                                 !JiveContainerPermHelper.getCanManageContainer(container),
                                                 "-template,-tiles.config,-extstreams.config");

        model = new PlaceViewBean.Builder().place(getPlace()).widgetMarkup(widgetMarkup)
                .widgetTypeKey(widgetContext.getWidgetType().getKey())
                .announcements(getAnnouncements())
                .canManageContainer(JiveContainerPermHelper.getCanManageContainer(getContainer()))
                .canManageAnnouncements(AnnouncementPermHelper.getCanCreateAnnounce(getContainer()))
                .pageCustomized(pageCustomized)
                .customizeMessageSeen(customizeMessageSeen)
                .showLayoutUpgradeMessage(Boolean.valueOf(getContainer().getProperties().get(LAYOUT_UPGRADE_PROPERTY)))
                .customizeNow(customizeNow)
                .homeNavHelper(homeNavHelper.getProperties())
                .imageURL(imageURL)
                .communityMetaDescription(modifyMetaDescription(getPlace().getDescription()))
                .setWelcomeMessage(JiveGlobals.getJiveProperty("jive.mobile.welcome"))
                .setDefaultMobileHomePage(defaultMobileHomePage)
                .build();
        return SUCCESS;
    }

    @Override
    public Object getModel() {
        return model;
    }

    public WidgetContext getWidgetContext() {
        return new HomepageWidgetContext(getUser(), getAuthToken(), getRequest(), getResponse());
    }

    public WidgetLayout getWidgetLayout() {
        try {
            if (!isHasCustomized()) {
                widgetPresentationConfigurator.setup(getContainer());
            } else {
                // if layout isn't valid in this context, we need to change it and publish it
                WidgetLayout widgetLayout = widgetManager.getPublishedWidgetLayout(getContainer());
                if (widgetManager.isWidgetTypeSupportedByLayout(widgetLayout, getWidgetContext().getWidgetType())) {
                    return widgetLayout;
                }

                // if we got here, the widget layout isn't valid for this context and needs to be reset to the default
                widgetPresentationConfigurator.reset(getContainer());

                // drop a note for the admin(s) letting them know it needed to be changed
                final JiveContainer container = getContainer();

                try {
                    new SystemExecutor<Void>(getAuthenticationProvider()).executeCallable(new Callable<Void>() {

                        public Void call() throws Exception {
                            container.getProperties().put(LAYOUT_UPGRADE_PROPERTY, Boolean.TRUE.toString());
                            JiveApplication.getEffectiveContext().getJiveContainerManager().update(container);
                            return null;
                        }
                    });
                }
                catch (Exception e) {
                    log.error("Problem setting widget layout upgrade property.", e);
                }
            }
            
            return widgetManager.getPublishedWidgetLayout(getContainer());
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    public Map<Integer, List<WidgetFrame>> getWidgetFrames() {
        if (!isHasCustomized()) {
            widgetPresentationConfigurator.setup(getContainer());
            widgetManager.publishWidgetFrames(getContainer());
        }

        Map<Integer, List<WidgetFrame>> frames = widgetManager.getPublishedWidgetFrames(getContainer());
        removedDeprecatedWidgets(frames);
        return frames;
    }

    protected void removedDeprecatedWidgets(Map<Integer, List<WidgetFrame>> widgetFrameMap) {
        Iterator<Integer> it = widgetFrameMap.keySet().iterator();
        while (it.hasNext()) {
            List<WidgetFrame> frameList = widgetFrameMap.get(it.next());
            List<WidgetFrame> framesToRemove = new ArrayList<WidgetFrame>();
            for (WidgetFrame f : frameList) {
                if (f.getWidget().getClass().isAnnotationPresent(Deprecated.class)) {
                    framesToRemove.add(f);
                }
            }
            for (WidgetFrame f : framesToRemove) {
                frameList.remove(f);
            }
        }
    }

    protected boolean isAuthorized(JiveContainer container) {
        return JiveContainerPermHelper.getCanManageContainer(container);
    }

    protected boolean isHasCustomized() {
        return widgetManager.getPublishedWidgetFrames(getContainer()).size() > 0;
    }

    protected Collection<AnnouncementViewBean> getAnnouncements() {
        if (JiveGlobals.getJiveBooleanProperty("announcements.enabled", true)) {
            if (announcements == null) {
                announcements = announcementManager.getAnnouncements(getContainer());
            }
            List<AnnouncementViewBean> announcementViewBeans = new ArrayList<AnnouncementViewBean>();
            for (Announcement ann : announcements) {
                // filter out expired announcements
                if (ann.getEndDate() != null && ann.getEndDate().getTime() >= System.currentTimeMillis()) {
                    announcementViewBeans.add(announcementActionHelper.buildAnnouncementViewBean(ann, getUser()));
                }
            }
            return announcementViewBeans;
        }
        return Collections.EMPTY_LIST;
    }

    protected Place getPlace() {
        Place place = new Place(getContainer(), false);
        Community root = communityManager.getRootCommunity();
        place.setBrowseID(browseManager.getBrowseID(root.getObjectType(), root.getID()));
        return place;
    }

    @Required
    public void setWidgetManager(WidgetManager widgetManager) {
        this.widgetManager = widgetManager;
    }

    @Required
    public void setAnnouncementManager(AnnouncementManager announcementManager) {
        this.announcementManager = announcementManager;
    }

    @Required
    public void setCreateCommunityActionHelper(CreateCommunityActionHelper createCommunityActionHelper) {
        this.createCommunityActionHelper = createCommunityActionHelper;
    }

    @Required
    public void setHomeNavHelper(HomeNavHelper homeNavHelper) {
        this.homeNavHelper = homeNavHelper;
    }

    @Required
    public void setWidgetPresentationConfigurator(WidgetPresentationConfigurator widgetPresentationConfigurator) {
        this.widgetPresentationConfigurator = widgetPresentationConfigurator;
    }

    @Required
    public void setAnnouncementActionHelper(AnnouncementActionHelper announcementActionHelper) {
        this.announcementActionHelper = announcementActionHelper;
    }

    @Required
    public void setDefaultMobileHomePageHelper(DefaultMobileHomePageHelper defaultMobileHomePageHelper) {
        this.defaultMobileHomePageHelper = defaultMobileHomePageHelper;
    }

    @Required
    public void setPageEntityConverter(PageEntityConverter pageEntityConverter) {
        this.pageEntityConverter = pageEntityConverter;
    }

    @Required
    public void setI18nResourceBundler(I18nResourceBundler i18nResourceBundler) {
        this.i18nResourceBundler = i18nResourceBundler;
    }

    @InjectConfiguration
    public void setWelcomeConfiguration(WelcomeConfiguration welcomeConfiguration) {
        this.welcomeConfiguration = welcomeConfiguration;
    }

    private String modifyMetaDescription(String meta) {
        String body = (meta == null) ? "" : meta;

        final int MAX_DESCRIPTON_LENGTH = JiveGlobals.getJiveIntProperty("meta.description.max-length", 155);

        if (body.length() > MAX_DESCRIPTON_LENGTH) {
            body = StringUtils.chopAtWord(body, MAX_DESCRIPTON_LENGTH);
        }

        return body;
    }
}
