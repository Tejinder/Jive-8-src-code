/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.navbar.rest;

import com.jivesoftware.base.User;
import com.jivesoftware.community.navbar.GlobalNavBar;
import com.jivesoftware.community.web.view.ViewBean;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A way to provide the nav bar to the template view layer.
 *
 * @since Jive SBS 5.0
 */
public class NavBarViewProvider implements ViewBean {

    static Logger log = LogManager.getLogger(NavBarViewProvider.class);

    private NavBarFactory navBarFactory;

    public void setNavBarFactory(NavBarFactory navBarFactory) {
        this.navBarFactory = navBarFactory;
    }

    public NavBarView getNavBarInstance(User user) {
        GlobalNavBar globalNavBar = navBarFactory.getNavBarInstance();
        return new NavBarView(user, globalNavBar);
    }
    
    public NavBarView getNavBarInstance(User user, HttpSession session) {
    	GlobalNavBar globalNavBar = navBarFactory.getNavBarInstance();
      
      
        return new NavBarView(user, globalNavBar, session);
    }

    public NavBarView getNavBarInstance(User user, HttpServletRequest request, HttpSession session) {
    	GlobalNavBar globalNavBar = navBarFactory.getNavBarInstance();
        return new NavBarView(user, globalNavBar, request, session);
    }
}
