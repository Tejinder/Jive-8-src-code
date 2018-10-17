package com.grail.kantar.objecttype;

import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.proxy.KantarAttachmentProxy;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.proxy.ProxyUtils;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.objecttype.EntitlementCheckableType;
import com.jivesoftware.community.objecttype.JiveObjectFactory;
import com.jivesoftware.community.objecttype.JiveObjectType;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 11/6/14
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarAttachmentObjectType implements JiveObjectType, EntitlementCheckableType<KantarAttachment> {

    public static final String KANTAR_ATTACHMENT_OBJECT_CODE = "kantarAttachmentObjectType";
    public static final int KANTAR_ATTACHMENT__OBJECT_TYPE_ID = KANTAR_ATTACHMENT_OBJECT_CODE.hashCode();

    private KantarAttachmentEntitlementCheckProvider kantarAttachmentEntitlementCheckProvider;


    @Override
    public int getID() {
        return KANTAR_ATTACHMENT__OBJECT_TYPE_ID;
    }

    @Override
    public String getCode() {
        return KANTAR_ATTACHMENT_OBJECT_CODE;
    }

    @Override
    public JiveObjectFactory getObjectFactory() {
        return new JiveObjectFactory<KantarAttachment>() {
            @Override
            public KantarAttachment loadObject(long id) throws NotFoundException {
                return new KantarAttachment();
            }

            @Override
            public KantarAttachment loadObject(String id) throws NotFoundException {
                return loadObject(Long.parseLong(id));
            }

            @Override
            public KantarAttachment loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
                return loadObject(id);
            }

            @Override
            public KantarAttachment loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
                return loadProxyObject(Long.parseLong(id));
            }

            @Override
            public KantarAttachment createProxy(KantarAttachment obj, AuthToken auth) {
                return ProxyUtils.proxyObject(KantarAttachmentProxy.class, obj, auth);
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public KantarAttachmentEntitlementCheckProvider getEntitlementCheckProvider() {
        return kantarAttachmentEntitlementCheckProvider;
    }

    public void setKantarAttachmentEntitlementCheckProvider(KantarAttachmentEntitlementCheckProvider kantarAttachmentEntitlementCheckProvider) {
        this.kantarAttachmentEntitlementCheckProvider = kantarAttachmentEntitlementCheckProvider;
    }
}
