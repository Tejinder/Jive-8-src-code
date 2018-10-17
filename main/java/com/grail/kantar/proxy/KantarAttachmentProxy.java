package com.grail.kantar.proxy;

import com.grail.kantar.object.KantarAttachment;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.proxy.JiveProxy;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 11/6/14
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarAttachmentProxy  extends KantarAttachment implements JiveProxy<KantarAttachment> {

    protected KantarAttachment kantarAttachment;
    protected AuthToken authToken;

    @Override
    public void init(KantarAttachment target, AuthToken authToken) {
        this.kantarAttachment = target;
        this.authToken = authToken;
    }

    @Override
    public KantarAttachment getUnproxiedObject() {
        return kantarAttachment;
    }

    @Override
    public AuthToken getProxyAuthToken() {
        return authToken;
    }
}
