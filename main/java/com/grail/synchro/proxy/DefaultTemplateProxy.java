package com.grail.synchro.proxy;

import com.grail.synchro.object.DefaultTemplate;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.proxy.JiveProxy;
import com.jivesoftware.community.license.JiveLicenseManager;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 6:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplateProxy extends DefaultTemplate implements JiveProxy<DefaultTemplate> {

    protected DefaultTemplate defaultTemplate;
    protected AuthToken authToken;
    private JiveLicenseManager licenseManager;

    @Override
    public void init(DefaultTemplate target, AuthToken authToken) {
         this.defaultTemplate = target;
        this.authToken = authToken;
    }

    @Override
    public DefaultTemplate getUnproxiedObject() {
        return defaultTemplate;
    }

    @Override
    public AuthToken getProxyAuthToken() {
        return authToken;
    }
}
