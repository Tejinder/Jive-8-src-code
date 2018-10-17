package com.grail.synchro.objecttype;

import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.proxy.SynchroAttachmentProxy;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.proxy.ProxyUtils;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.objecttype.EntitlementCheckableType;
import com.jivesoftware.community.objecttype.JiveObjectFactory;
import com.jivesoftware.community.objecttype.JiveObjectType;
import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class SynchroAttachmentObjectType implements JiveObjectType, EntitlementCheckableType<SynchroAttachment> {

    private SynchroAttachmentEntitlementCheckProvider entitlementCheckProvider;
    private static String OBJECT_ID = "";
    private static String OBJECT_TYPE = "synchroAttachmentObjectType";

    @Override
    public int getID() {
        return buildCode(OBJECT_TYPE).hashCode();
    }

    public static String buildCode(final String objectType) {
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(objectType);
        return idBuilder.toString();
    }

//    public static String buildCode(final String prefix, final String objectType , final String suffix) {
//        StringBuilder idBuilder = new StringBuilder();
//        if(StringUtils.isNotBlank(prefix)) {
//            idBuilder.append(prefix).append("-");
//        }
//        idBuilder.append(objectType);
//        if(StringUtils.isNotBlank(suffix)) {
//            idBuilder.append("-").append(suffix);
//        }
//        return idBuilder.toString();
//    }

    @Override
    public String getCode() {
        return buildCode(OBJECT_TYPE);
    }

    @Override
    public JiveObjectFactory getObjectFactory() {
        return new JiveObjectFactory<SynchroAttachment>() {
            @Override
            public SynchroAttachment loadObject(long id) throws NotFoundException {
                return new SynchroAttachment();
            }

            @Override
            public SynchroAttachment loadObject(String id) throws NotFoundException {
                return loadObject(Long.parseLong(id));
            }

            @Override
            public SynchroAttachment loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
                return loadObject(id);
            }

            @Override
            public SynchroAttachment loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
                return loadProxyObject(Long.parseLong(id));
            }

            @Override
            public SynchroAttachment createProxy(SynchroAttachment obj, AuthToken auth) {
                return ProxyUtils.proxyObject(SynchroAttachmentProxy.class, obj, auth);
            }

        };
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @Override
    public SynchroAttachmentEntitlementCheckProvider getEntitlementCheckProvider() {
        return entitlementCheckProvider;
    }

    public void setEntitlementCheckProvider(final SynchroAttachmentEntitlementCheckProvider synchroAttachmentEntitlementCheckProvider) {
        this.entitlementCheckProvider = synchroAttachmentEntitlementCheckProvider;
    }

}
