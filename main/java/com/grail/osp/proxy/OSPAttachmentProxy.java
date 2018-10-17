package com.grail.osp.proxy;

import com.grail.osp.object.OSPAttachment;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.proxy.JiveProxy;

/**
 *
 */
public class OSPAttachmentProxy extends OSPAttachment implements JiveProxy<OSPAttachment> {
    protected OSPAttachment ospAttachment;
    protected AuthToken authToken;
    

    @Override
    public void init(OSPAttachment target, AuthToken authToken) {
        this.ospAttachment = target;
        this.authToken = authToken;
    }

    @Override
    public OSPAttachment getUnproxiedObject() {
        return ospAttachment;
    }

    @Override
    public AuthToken getProxyAuthToken() {
        return authToken;
    }
}
