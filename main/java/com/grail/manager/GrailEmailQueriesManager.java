package com.grail.manager;

import com.grail.beans.GrailEmailQuery;
import com.jivesoftware.base.User;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/4/14
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GrailEmailQueriesManager {
    public boolean processQuery(final GrailEmailQuery emailQuery);
    public boolean processQuery(final String recipients, final String subject, final String body, final Integer type, final User sender,
                                final File attachment, final String fileName, final String contentType, final String portalType);
}
