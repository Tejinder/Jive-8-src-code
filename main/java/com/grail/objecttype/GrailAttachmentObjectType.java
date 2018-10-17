package com.grail.objecttype;

import com.grail.object.GrailAttachment;
import com.grail.proxy.GrailAttachmentProxy;
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
 * Date: 1/6/15
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailAttachmentObjectType implements JiveObjectType, EntitlementCheckableType<GrailAttachment> {
    public static final String GRAIL_ATTACHMENT_OBJECT_CODE = "grailAttachmentObjectType";
    public static final int GRAIL_ATTACHMENT_OBJECT_TYPE_ID = GRAIL_ATTACHMENT_OBJECT_CODE.hashCode();

    private GrailAttachmentEntitlementCheckProvider grailAttachmentEntitlementCheckProvider;


    @Override
    public int getID() {
        return GRAIL_ATTACHMENT_OBJECT_TYPE_ID;
    }

    @Override
    public String getCode() {
        return GRAIL_ATTACHMENT_OBJECT_CODE;
    }

    @Override
    public JiveObjectFactory getObjectFactory() {
        return new JiveObjectFactory<GrailAttachment>() {
            @Override
            public GrailAttachment loadObject(long id) throws NotFoundException {
                return new GrailAttachment();
            }

            @Override
            public GrailAttachment loadObject(String id) throws NotFoundException {
                return loadObject(Long.parseLong(id));
            }

            @Override
            public GrailAttachment loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
                return loadObject(id);
            }

            @Override
            public GrailAttachment loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
                return loadProxyObject(Long.parseLong(id));
            }

            @Override
            public GrailAttachment createProxy(GrailAttachment obj, AuthToken auth) {
                return ProxyUtils.proxyObject(GrailAttachmentProxy.class, obj, auth);
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public GrailAttachmentEntitlementCheckProvider getEntitlementCheckProvider() {
        return grailAttachmentEntitlementCheckProvider;
    }

    public void setGrailAttachmentEntitlementCheckProvider(GrailAttachmentEntitlementCheckProvider grailAttachmentEntitlementCheckProvider) {
        this.grailAttachmentEntitlementCheckProvider = grailAttachmentEntitlementCheckProvider;
    }
}
