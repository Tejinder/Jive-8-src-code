package com.grail.objecttype;

import com.grail.object.GrailEmailQueryAttachment;
import com.grail.proxy.GrailEmailQueryAttachmentProxy;
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
 * Date: 1/2/15
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailEmailQueryAttachmentObjectType  implements JiveObjectType, EntitlementCheckableType<GrailEmailQueryAttachment> {
    public static final String GRAIL_EMAIL_QUERY_ATTACHMENT_OBJECT_CODE = "grailEmailQueryAttachmentObjectType";
    public static final int GRAIL_EMAIL_QUERY_ATTACHMENT__OBJECT_TYPE_ID = GRAIL_EMAIL_QUERY_ATTACHMENT_OBJECT_CODE.hashCode();

    private GrailEmailQueryAttachmentEntitlementCheckProvider grailEmailQueryAttachmentEntitlementCheckProvider;


    @Override
    public int getID() {
        return GRAIL_EMAIL_QUERY_ATTACHMENT__OBJECT_TYPE_ID;
    }

    @Override
    public String getCode() {
        return GRAIL_EMAIL_QUERY_ATTACHMENT_OBJECT_CODE;
    }

    @Override
    public JiveObjectFactory getObjectFactory() {
        return new JiveObjectFactory<GrailEmailQueryAttachment>() {
            @Override
            public GrailEmailQueryAttachment loadObject(long id) throws NotFoundException {
                return new GrailEmailQueryAttachment();
            }

            @Override
            public GrailEmailQueryAttachment loadObject(String id) throws NotFoundException {
                return loadObject(Long.parseLong(id));
            }

            @Override
            public GrailEmailQueryAttachment loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
                return loadObject(id);
            }

            @Override
            public GrailEmailQueryAttachment loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
                return loadProxyObject(Long.parseLong(id));
            }

            @Override
            public GrailEmailQueryAttachment createProxy(GrailEmailQueryAttachment obj, AuthToken auth) {
                return ProxyUtils.proxyObject(GrailEmailQueryAttachmentProxy.class, obj, auth);
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public GrailEmailQueryAttachmentEntitlementCheckProvider getEntitlementCheckProvider() {
        return grailEmailQueryAttachmentEntitlementCheckProvider;
    }

    public void setGrailEmailQueryAttachmentEntitlementCheckProvider(GrailEmailQueryAttachmentEntitlementCheckProvider grailEmailQueryAttachmentEntitlementCheckProvider) {
        this.grailEmailQueryAttachmentEntitlementCheckProvider = grailEmailQueryAttachmentEntitlementCheckProvider;
    }
}
