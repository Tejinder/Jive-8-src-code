package com.grail.proxy;

import com.grail.object.GrailAttachment;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.proxy.JiveProxy;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/6/15
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailAttachmentProxy extends GrailAttachment implements JiveProxy<GrailAttachment> {
    protected GrailAttachment grailEmailQueryAttachment;
    protected AuthToken authToken;

    @Override
    public void init(GrailAttachment target, AuthToken authToken) {
        this.grailEmailQueryAttachment = target;
        this.authToken = authToken;
    }

    @Override
    public GrailAttachment getUnproxiedObject() {
        return grailEmailQueryAttachment;
    }

    @Override
    public AuthToken getProxyAuthToken() {
        return authToken;
    }
}
