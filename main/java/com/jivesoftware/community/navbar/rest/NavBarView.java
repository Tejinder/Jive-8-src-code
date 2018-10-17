/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.navbar.rest;

import com.grail.cart.util.CartUtil;
import com.grail.util.BATConstants;
import com.grail.util.BATGlobal;

import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.navbar.GlobalNavBar;
import com.jivesoftware.community.web.component.ActionLink;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * View bean used for rendering the application's main nav bar.
 */
@XmlRootElement(name = "navbar")
public class NavBarView {

    private boolean showSatelliteMenu = true;
    private boolean hideSatelliteDisplayName = false;
    private boolean collapseCreateMenu = false;
    private boolean showSearch = true;
    private boolean spotlightSearchEnabled = JiveGlobals.getJiveBooleanProperty("jive.search.spotlight.enabled", true);
    private boolean spotlightV2Enabled =
            JiveGlobals.getJiveBooleanProperty("jive.search.spotlight.v2.enabled", true);
    private boolean createMenuShown;
    private boolean appsMenuShown;
    private String customNavLogoType;
    private String customNavLogoImageAltText;
    private String customNavLogoImageUrl;
    private String simpleNavLogoType;
    private String simpleNavLogoImageAltText;
    private String simpleNavLogoImageUrl;

    private List<ActionLink> links;
    private ActionLink registrationLink;
    private ActionLink historyLink;
    private ActionLink bookmarksLink;
    private ActionLink logoutLink;
    private ActionLink homeLink;
    private ActionLink newsLink;
    private ActionLink contentLink;
    private ActionLink peopleLink;
    private ActionLink placesLink;
    private ActionLink appsLink;
    private ActionLink createLink;
    
    private String portalType = BATGlobal.PortalType.RKP.toString();
    private boolean rkpPortal = true;
    private boolean synchroPortal = false;
    private boolean grailPortal = false;
    private boolean kantarPortal = false;
    private boolean kantarReportPortal = false;
    private boolean oracleDocumentsPortal = false;
    private String requestURI = "";
    private String cartItemCount = "0";
    
    // For CXF marshalling
    public NavBarView() {
    }

    public NavBarView(User user, GlobalNavBar navigationBar) {
        this.links = navigationBar.getLinks();

        this.registrationLink = navigationBar.getRegistrationLink();
        this.bookmarksLink = navigationBar.getBookmarksLink();
        this.historyLink = navigationBar.getHistoryLink();
        this.logoutLink = navigationBar.getLogoutLink();
        this.homeLink = navigationBar.getHomeLink();
        this.newsLink = navigationBar.getNewsLink();
        this.contentLink = navigationBar.getContentLink();
        this.peopleLink = navigationBar.getPeopleLink();
        this.placesLink = navigationBar.getPlacesLink();
        this.appsLink = navigationBar.getAppsLink();
        this.createLink = navigationBar.getCreateLink();
        this.appsMenuShown = navigationBar.isAppsMenuShown();
        this.createMenuShown = navigationBar.isCreateMenuShown();

        this.showSatelliteMenu = !user.isAnonymous();
        if(user!=null)
        {
        	this.cartItemCount = CartUtil.getCartItemsCount(user) + "";
        }
    }
    public NavBarView(User user, GlobalNavBar navigationBar, HttpSession session) {

        if(session != null && session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE) != null) {
            this.portalType = session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE).toString();

        } else {
            this.rkpPortal = true;
            this.synchroPortal = false;
            this.grailPortal = false;
            this.kantarPortal = false;
            this.kantarReportPortal = false;
        }
        
        if(user!=null)
        {
        	this.cartItemCount = CartUtil.getCartItemsCount(user) + "";
        }
     /*   if(this.portalType != null && this.portalType.equals(BATGlobal.PortalType.SYNCHRO.toString())) {
            for(SatelliteMenuSection section : navigationBar.getSatelliteMenuView().getSections()) {
                Collection<SatelliteMenuSection> sections = new LinkedList<SatelliteMenuSection>();
                if(section.getNameKey().equals("nav.bar.manage.section")) {
                    List<ActionLink> actionLinks = new LinkedList<ActionLink>();
                    for(ActionLink link : section.getItems()) {
                        if(!(link.getNameKey().equals("nav.bar.homepage.link") ||
                                link.getNameKey().equals("announcement.manage.system.title") ||
                                link.getNameKey().equals("userbar.admin.skin.basic"))) {
                            actionLinks.add(link);
                        }
                    }
                    section.setItems(actionLinks);
                }
                if(section.getItems() == null && section.getItems().size() <= 0) {
                    sections.remove(section);
                }

            }
        }*/

        this.links = navigationBar.getLinks();

        this.registrationLink = navigationBar.getRegistrationLink();
        this.bookmarksLink = navigationBar.getBookmarksLink();
        this.historyLink = navigationBar.getHistoryLink();
        this.logoutLink = navigationBar.getLogoutLink();
        this.homeLink = navigationBar.getHomeLink();
        this.newsLink = navigationBar.getNewsLink();
        this.contentLink = navigationBar.getContentLink();
        this.peopleLink = navigationBar.getPeopleLink();
        this.placesLink = navigationBar.getPlacesLink();
        this.appsLink = navigationBar.getAppsLink();
        this.createLink = navigationBar.getCreateLink();
        this.appsMenuShown = navigationBar.isAppsMenuShown();
        this.createMenuShown = navigationBar.isCreateMenuShown();

        this.showSatelliteMenu = !user.isAnonymous();

        if(null != this.portalType) {
            this.rkpPortal = false;
            this.synchroPortal = false;
            this.grailPortal = false;
            this.kantarPortal = false;
            this.kantarReportPortal = false;
            this.oracleDocumentsPortal = false;
            if(this.portalType.equals(BATGlobal.PortalType.RKP.toString())) {
                this.rkpPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.SYNCHRO.toString())) {
                this.synchroPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.GRAIL.toString())) {
                this.grailPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.KANTAR.toString())) {
                this.kantarPortal = true;
            }  else if(this.portalType.equals(BATGlobal.PortalType.KANTAR_REPORT.toString())) {
                this.kantarReportPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.ORACLE_DOCUMENTS.toString())) {
                this.oracleDocumentsPortal = true;
            }
        } else {
            this.rkpPortal = false;
            this.synchroPortal = false;
            this.grailPortal = false;
            this.kantarPortal = false;
            this.kantarReportPortal = false;
            this.oracleDocumentsPortal = false;
        }

    }

    public NavBarView(User user, GlobalNavBar navigationBar,HttpServletRequest request, HttpSession session) {

        if(session != null && session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE) != null) {
            this.portalType = session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE).toString();

        } else {
            this.rkpPortal = true;
            this.synchroPortal = false;
            this.grailPortal = false;
            this.kantarPortal = false;
            this.kantarReportPortal = false;
            this.oracleDocumentsPortal = false;
        }
        
        if(user!=null)
        {
        	this.cartItemCount = CartUtil.getCartItemsCount(user) + "";
        }
     /*   if(this.portalType != null && this.portalType.equals(BATGlobal.PortalType.SYNCHRO.toString())) {
            for(SatelliteMenuSection section : navigationBar.getSatelliteMenuView().getSections()) {
                Collection<SatelliteMenuSection> sections = new LinkedList<SatelliteMenuSection>();
                if(section.getNameKey().equals("nav.bar.manage.section")) {
                    List<ActionLink> actionLinks = new LinkedList<ActionLink>();
                    for(ActionLink link : section.getItems()) {
                        if(!(link.getNameKey().equals("nav.bar.homepage.link") ||
                                link.getNameKey().equals("announcement.manage.system.title") ||
                                link.getNameKey().equals("userbar.admin.skin.basic"))) {
                            actionLinks.add(link);
                        }
                    }
                    section.setItems(actionLinks);
                }
                if(section.getItems() == null && section.getItems().size() <= 0) {
                    sections.remove(section);
                }

            }
        }
*/
        this.links = navigationBar.getLinks();

        this.registrationLink = navigationBar.getRegistrationLink();
        this.bookmarksLink = navigationBar.getBookmarksLink();
        this.historyLink = navigationBar.getHistoryLink();
        this.logoutLink = navigationBar.getLogoutLink();
        this.homeLink = navigationBar.getHomeLink();
        this.newsLink = navigationBar.getNewsLink();
        this.contentLink = navigationBar.getContentLink();
        this.peopleLink = navigationBar.getPeopleLink();
        this.placesLink = navigationBar.getPlacesLink();
        this.appsLink = navigationBar.getAppsLink();
        this.createLink = navigationBar.getCreateLink();
        this.appsMenuShown = navigationBar.isAppsMenuShown();
        this.createMenuShown = navigationBar.isCreateMenuShown();

        this.showSatelliteMenu = !user.isAnonymous();

        if(null != this.portalType) {
            this.rkpPortal = false;
            this.synchroPortal = false;
            this.grailPortal = false;
            this.kantarPortal = false;
            this.kantarReportPortal = false;
            this.oracleDocumentsPortal = false;
            if(this.portalType.equals(BATGlobal.PortalType.RKP.toString()) && !request.getRequestURI().contains("/synchro/")) {
                this.rkpPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.SYNCHRO.toString()) || request.getRequestURI().contains("/synchro/")) {
                this.synchroPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.GRAIL.toString())) {
                this.grailPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.KANTAR.toString())) {
                this.kantarPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.KANTAR_REPORT.toString())) {
                this.kantarReportPortal = true;
            } else if(this.portalType.equals(BATGlobal.PortalType.ORACLE_DOCUMENTS.toString())) {
                this.kantarReportPortal = true;
            }
        } else {
            this.rkpPortal = false;
            this.synchroPortal = false;
            this.grailPortal = false;
            this.kantarPortal = false;
            this.kantarReportPortal = false;
            this.oracleDocumentsPortal = false;
        }

    }
    public boolean isCollapseCreateMenu() {
        return collapseCreateMenu;
    }

    public void setCollapseCreateMenu(boolean collapseCreateMenu) {
        this.collapseCreateMenu = collapseCreateMenu;
    }

    public boolean isHideSatelliteDisplayName() {
        return hideSatelliteDisplayName;
    }

    public void setHideSatelliteDisplayName(boolean hideSatelliteDisplayName) {
        this.hideSatelliteDisplayName = hideSatelliteDisplayName;
    }

    public boolean isShowSatelliteMenu() {
        return showSatelliteMenu;
    }

    public boolean isShowSearch() {
        return showSearch;
    }

    public void setShowSearch(boolean showSearch) {
        this.showSearch = showSearch;
    }

    @XmlElement(name = "links")
    public List<ActionLink> getLinks() {
        return links;
    }

    public void setLinks(List<ActionLink> links) {
        this.links = links;
    }

    public ActionLink getRegistrationLink() {
        return registrationLink;
    }

    public boolean isSpotlightSearchEnabled() {
        return spotlightSearchEnabled;
    }

    public boolean isSpotlightV2Enabled() {
        return spotlightV2Enabled;
    }

    public ActionLink getHistoryLink() {
        return historyLink;
    }

    public void setHistoryLink(ActionLink historyLink) {
        this.historyLink = historyLink;
    }

    public ActionLink getBookmarksLink() {
        return bookmarksLink;
    }

    public void setBookmarksLink(ActionLink bookmarksLink) {
        this.bookmarksLink = bookmarksLink;
    }

    public ActionLink getLogoutLink() {
        return logoutLink;
    }

    public void setLogoutLink(ActionLink logoutLink) {
        this.logoutLink = logoutLink;
    }

    public ActionLink getHomeLink() {
        return homeLink;
    }

    public void setHomeLink(ActionLink homeLink) {
        this.homeLink = homeLink;
    }

    public ActionLink getNewsLink() {
        return newsLink;
    }

    public void setNewsLink(ActionLink newsLink) {
        this.newsLink = newsLink;
    }

    public ActionLink getContentLink() {
        return contentLink;
    }

    public void setContentLink(ActionLink contentLink) {
        this.contentLink = contentLink;
    }

    public ActionLink getPeopleLink() {
        return peopleLink;
    }

    public void setPeopleLink(ActionLink peopleLink) {
        this.peopleLink = peopleLink;
    }

    public ActionLink getPlacesLink() {
        return placesLink;
    }

    public void setPlacesLink(ActionLink placesLink) {
        this.placesLink = placesLink;
    }

    public ActionLink getAppsLink() {
        return appsLink;
    }

    public void setAppsLink(ActionLink appsLink) {
        this.appsLink = appsLink;
    }

    public ActionLink getCreateLink() {
        return createLink;
    }

    public void setCreateLink(ActionLink createLink) {
        this.createLink = createLink;
    }

    public boolean isCreateMenuShown() {
        return createMenuShown;
    }

    public boolean isAppsMenuShown() {
        return appsMenuShown;
    }

    public String getCustomNavLogoType() {
        return customNavLogoType;
    }

    public void setCustomNavLogoType(String customNavLogoType) {
        this.customNavLogoType = customNavLogoType;
    }

    public String getCustomNavLogoImageAltText() {
        return customNavLogoImageAltText;
    }

    public void setCustomNavLogoImageAltText(String customNavLogoImageAltText) {
        this.customNavLogoImageAltText = customNavLogoImageAltText;
    }

    public String getCustomNavLogoImageUrl() {
        return customNavLogoImageUrl;
    }

    public void setCustomNavLogoImageUrl(String customNavLogoImageUrl) {
        this.customNavLogoImageUrl = customNavLogoImageUrl;
    }

    public String getSimpleNavLogoType() {
        return simpleNavLogoType;
    }

    public void setSimpleNavLogoType(String simpleNavLogoType) {
        this.simpleNavLogoType = simpleNavLogoType;
    }

    public String getSimpleNavLogoImageAltText() {
        return simpleNavLogoImageAltText;
    }

    public void setSimpleNavLogoImageAltText(String simpleNavLogoImageAltText) {
        this.simpleNavLogoImageAltText = simpleNavLogoImageAltText;
    }

    public String getSimpleNavLogoImageUrl() {
        return simpleNavLogoImageUrl;
    }

    public void setSimpleNavLogoImageUrl(String simpleNavLogoImageUrl) {
        this.simpleNavLogoImageUrl = simpleNavLogoImageUrl;
    }
    public String getPortalType() {
        return portalType;
    }

    public boolean getRkpPortal() {
        return rkpPortal;
    }

    public boolean getSynchroPortal() {
        return synchroPortal;
    }

    public boolean isGrailPortal() {
        return grailPortal;
    }

    public boolean isKantarPortal() {
        return kantarPortal;
    }

    public boolean isKantarReportPortal() {
        return kantarReportPortal;
    }

    public boolean isOracleDocumentsPortal() {
        return oracleDocumentsPortal;
    }

    public void setOracleDocumentsPortal(boolean oracleDocumentsPortal) {
        this.oracleDocumentsPortal = oracleDocumentsPortal;
    }

	public String getCartItemCount() {
		return cartItemCount;
	}

	public void setCartItemCount(String cartItemCount) {
		this.cartItemCount = cartItemCount;
	}

    
}
