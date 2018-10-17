package com.grail.synchro.object;

import com.grail.synchro.manager.DefaultTemplateManager;
import com.grail.synchro.proxy.DefaultTemplateProxy;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.proxy.ProxyUtils;
import com.jivesoftware.community.NotFoundException;
//import com.jivesoftware.community.impl.ObjectFactory;
import com.jivesoftware.community.objecttype.JiveObjectFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplateObjectFactory {/*implements ObjectFactory<DefaultTemplate>, JiveObjectFactory<DefaultTemplate> {

    private DefaultTemplateManager defaultTemplateManager;

    @Override
    public DefaultTemplate loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
        return defaultTemplateManager.get(id);
    }

    @Override
    public DefaultTemplate loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
        return defaultTemplateManager.get(Long.parseLong(id));
    }

    @Override
    public DefaultTemplate createProxy(DefaultTemplate obj, AuthToken auth) {
        return ProxyUtils.proxyObject(DefaultTemplateProxy.class, obj, auth);
    }

    @Override
    public DefaultTemplate loadObject(long id) throws NotFoundException {
        return defaultTemplateManager.get(id);
    }

    @Override
    public DefaultTemplate loadObject(String id) throws NotFoundException {
        try {
            long longId = Long.parseLong(id);
            return loadObject(longId);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }*/

}
