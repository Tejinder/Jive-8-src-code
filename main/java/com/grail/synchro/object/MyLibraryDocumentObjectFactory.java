package com.grail.synchro.object;

import com.grail.synchro.manager.MyLibraryManager;
import com.grail.synchro.proxy.MyLibraryDocumentProxy;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.proxy.ProxyUtils;
import com.jivesoftware.community.NotFoundException;
//import com.jivesoftware.community.impl.BlockObjectFactory;
//import com.jivesoftware.community.impl.ObjectFactory;
import com.jivesoftware.community.objecttype.JiveObjectFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 11/26/13
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyLibraryDocumentObjectFactory {/*implements ObjectFactory<MyLibraryDocument>, JiveObjectFactory<MyLibraryDocument> {
    private MyLibraryManager myLibraryManager;


    @Override
    public MyLibraryDocument loadObject(long id) throws NotFoundException {
        return myLibraryManager.get(id);
    }

    @Override
    public MyLibraryDocument loadObject(String id) throws NotFoundException {
        return myLibraryManager.get(Long.parseLong(id));
    }

    @Override
    public MyLibraryDocument loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
        return myLibraryManager.get(id);
    }

    @Override
    public MyLibraryDocument loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
        try {
            long longId = Long.parseLong(id);
            return loadObject(longId);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public MyLibraryDocument createProxy(MyLibraryDocument obj, AuthToken auth) {
        return ProxyUtils.proxyObject(MyLibraryDocumentProxy.class, obj, auth);
    }*/
}
