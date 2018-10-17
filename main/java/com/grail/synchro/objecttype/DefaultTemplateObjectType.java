package com.grail.synchro.objecttype;

import com.grail.synchro.manager.DefaultTemplateManager;
import com.grail.synchro.object.DefaultTemplate;
import com.grail.synchro.proxy.DefaultTemplateProxy;
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
 * Date: 6/30/14
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplateObjectType  implements JiveObjectType, EntitlementCheckableType<DefaultTemplate> {

    private static final String DEFAULT_TEMPLATE_OBJECT_CODE = "defaultTemplateObjectType";
    public static final int DEFAULT_TEMPLATE_OBJECT_TYPE_ID = DEFAULT_TEMPLATE_OBJECT_CODE.hashCode(); // -1952273580

    private DefaultTemplateManager defaultTemplateManager;
    private DefaultTemplateEntitlementCheckProvider defaultTemplateEntitlementCheckProvider;


    @Override
     public DefaultTemplateEntitlementCheckProvider getEntitlementCheckProvider() {
        return defaultTemplateEntitlementCheckProvider;
    }

    public DefaultTemplateEntitlementCheckProvider getDefaultTemplateEntitlementCheckProvider() {
        return defaultTemplateEntitlementCheckProvider;
    }

    public void setDefaultTemplateEntitlementCheckProvider(DefaultTemplateEntitlementCheckProvider defaultTemplateEntitlementCheckProvider) {
        this.defaultTemplateEntitlementCheckProvider = defaultTemplateEntitlementCheckProvider;
    }

    @Override
    public int getID() {
        return DEFAULT_TEMPLATE_OBJECT_TYPE_ID;
    }

    @Override
    public String getCode() {
        return DEFAULT_TEMPLATE_OBJECT_CODE;
    }

    @Override
    public JiveObjectFactory getObjectFactory() {
        return new JiveObjectFactory<DefaultTemplate>() {
            @Override
            public DefaultTemplate loadObject(long id) throws NotFoundException {
                return defaultTemplateManager.get(id);
            }

            @Override
            public DefaultTemplate loadObject(String id) throws NotFoundException {
                return loadObject(Long.parseLong(id));
            }

            @Override
            public DefaultTemplate loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
                return defaultTemplateManager.get(id);
            }

            @Override
            public DefaultTemplate loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
                return loadProxyObject(Long.parseLong(id));
            }

            @Override
            public DefaultTemplate createProxy(DefaultTemplate obj, AuthToken auth) {
                return ProxyUtils.proxyObject(DefaultTemplateProxy.class, obj, auth);
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public DefaultTemplateManager getDefaultTemplateManager() {
        return defaultTemplateManager;
    }

    public void setDefaultTemplateManager(DefaultTemplateManager defaultTemplateManager) {
        this.defaultTemplateManager = defaultTemplateManager;
    }


}
