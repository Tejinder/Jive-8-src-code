/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.welcome;

import com.jivesoftware.community.solution.Configuration;
import com.jivesoftware.community.solution.annotations.DefaultValue;
import com.jivesoftware.community.solution.annotations.ExternalSolutionValue;
import com.jivesoftware.community.solution.annotations.PropertyName;

/**
 * Welcome page configuration.
 */
public interface WelcomeConfiguration extends Configuration {

    public static final String WELCOME_PAGE_PROPERTY = "welcome.page";
    public static final String WELCOME_PAGE_SIMPLIFIED_PROPERTY = "welcome.page.simplified";

    /**
     * Determines whether the overview/welcome page should be accessible in it's full form within the community.
     *
     * @return {@code true} if it should be presented, {@code false} otherwise
     */
    @PropertyName(WELCOME_PAGE_PROPERTY)
    @ExternalSolutionValue("true")
    @DefaultValue("false")
    Boolean isVisible();

    @PropertyName(WELCOME_PAGE_PROPERTY)
    void setVisible(boolean enabled);

    /**
     * Determines whether or not the welcome page is visible in a simplified, widgets only form.
     *
     * @return {@code true} to use the simplified page, {@code false} to use the comprehensive page
     */
    @ExternalSolutionValue("true")
    @DefaultValue("false")
    @PropertyName(WELCOME_PAGE_SIMPLIFIED_PROPERTY)
    Boolean isSimplifiedVisible();

    @PropertyName(WELCOME_PAGE_SIMPLIFIED_PROPERTY)
    void setSimplifiedVisible(boolean enabled);

}
