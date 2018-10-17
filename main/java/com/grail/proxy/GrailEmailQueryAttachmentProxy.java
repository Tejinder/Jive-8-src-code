package com.grail.proxy;

import com.grail.object.GrailEmailQueryAttachment;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.proxy.JiveProxy;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/2/15
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailEmailQueryAttachmentProxy  extends GrailEmailQueryAttachment implements JiveProxy<GrailEmailQueryAttachment> {
    protected GrailEmailQueryAttachment grailEmailQueryAttachment;
    protected AuthToken authToken;

    @Override
    public void init(GrailEmailQueryAttachment target, AuthToken authToken) {
        this.grailEmailQueryAttachment = target;
        this.authToken = authToken;
    }

    @Override
    public GrailEmailQueryAttachment getUnproxiedObject() {
        return grailEmailQueryAttachment;
    }

    @Override
    public AuthToken getProxyAuthToken() {
        return authToken;
    }
}
