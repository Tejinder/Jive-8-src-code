package com.grail.osp.objecttype;

import com.grail.osp.object.OSPAttachment;
import com.grail.osp.proxy.OSPAttachmentProxy;
import com.grail.synchro.object.MyLibraryDocument;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.proxy.ProxyUtils;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.objecttype.EntitlementCheckableType;
import com.jivesoftware.community.objecttype.JiveObjectFactory;
import com.jivesoftware.community.objecttype.JiveObjectType;

/**
 *
 */
public class OSPAttachmentObjectType implements JiveObjectType, EntitlementCheckableType<OSPAttachment> {

    private static final String OSP_DOCUMENT_OBJECT_CODE = "ospObjectType";
    public static final int OSP_DOCUMENT_OBJECT_TYPE_ID = OSP_DOCUMENT_OBJECT_CODE.hashCode();

  
    private OSPAttachmentEntitlementCheckProvider documentEntitlementCheckProvider;


    @Override
    public int getID() {
        return OSP_DOCUMENT_OBJECT_TYPE_ID;
    }

    @Override
    public String getCode() {
        return OSP_DOCUMENT_OBJECT_CODE;
    }

    @Override
    public JiveObjectFactory getObjectFactory() {
        return new JiveObjectFactory<OSPAttachment>() {
            @Override
            public OSPAttachment loadObject(long id) throws NotFoundException {
                return new OSPAttachment();
            }

            @Override
            public OSPAttachment loadObject(String id) throws NotFoundException {
                return loadObject(Long.parseLong(id));
            }

            @Override
            public OSPAttachment loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
                return loadObject(id);
            }

            @Override
            public OSPAttachment loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
                return loadProxyObject(Long.parseLong(id));
            }

            @Override
            public OSPAttachment createProxy(OSPAttachment obj, AuthToken auth) {
                return ProxyUtils.proxyObject(OSPAttachmentProxy.class, obj, auth);
            }

        };
    }


    @Override
    public boolean isEnabled() {
        return true;
    }

   
    @Override
    public OSPAttachmentEntitlementCheckProvider getEntitlementCheckProvider() {
        return documentEntitlementCheckProvider;
    }

    public void setDocumentEntitlementCheckProvider(final OSPAttachmentEntitlementCheckProvider documentEntitlementCheckProvider) {
        this.documentEntitlementCheckProvider = documentEntitlementCheckProvider;
    }
}
