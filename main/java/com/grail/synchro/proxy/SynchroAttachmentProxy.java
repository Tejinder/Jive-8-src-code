package com.grail.synchro.proxy;

import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.proxy.JiveProxy;

/**
 *
 */
public class SynchroAttachmentProxy extends SynchroAttachment implements JiveProxy<SynchroAttachment> {
    protected SynchroAttachment synchroAttachment;
    protected AuthToken authToken;
    

    @Override
    public void init(SynchroAttachment target, AuthToken authToken) {
        this.synchroAttachment = target;
        this.authToken = authToken;
    }

    @Override
    public SynchroAttachment getUnproxiedObject() {
        return synchroAttachment;
    }

    @Override
    public AuthToken getProxyAuthToken() {
        return authToken;
    }
}
