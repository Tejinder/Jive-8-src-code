/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.welcome;

import com.jivesoftware.community.solution.annotations.InjectConfiguration;

/**
 * Presents a view to the users which includes only the widgetized home page without any of the stream controls
 * or related links.
 *
 * @since Jive SBS 6.0
 */
public class SimplifiedWelcomeAction extends WelcomeAction {

    private WelcomeConfiguration welcomeConfiguration;

    protected String path = "simplified-welcome";

    public boolean isVisible() {
        return welcomeConfiguration.isSimplifiedVisible() || welcomeConfiguration.isVisible();
    }

    @InjectConfiguration
    public void setWelcomeConfiguration(WelcomeConfiguration welcomeConfiguration) {
        this.welcomeConfiguration = welcomeConfiguration;
    }
}
