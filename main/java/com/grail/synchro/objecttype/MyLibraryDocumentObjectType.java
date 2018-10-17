package com.grail.synchro.objecttype;

import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.manager.MyLibraryManager;
import com.grail.synchro.proxy.MyLibraryDocumentProxy;
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
public class MyLibraryDocumentObjectType implements JiveObjectType, EntitlementCheckableType<MyLibraryDocument> {

    private static final String MY_LIBRARY_DOCUMENT_OBJECT_CODE = "myLibraryObjectType";
    public static final int MY_LIBRARY_DOCUMENT_OBJECT_TYPE_ID = MY_LIBRARY_DOCUMENT_OBJECT_CODE.hashCode();

    private MyLibraryManager myLibraryManager;
    private MyLibraryDocumentEntitlementCheckProvider documentEntitlementCheckProvider;


    @Override
    public int getID() {
        return MY_LIBRARY_DOCUMENT_OBJECT_TYPE_ID;
    }

    @Override
    public String getCode() {
        return MY_LIBRARY_DOCUMENT_OBJECT_CODE;
    }

    @Override
    public JiveObjectFactory getObjectFactory() {
        return new JiveObjectFactory<MyLibraryDocument>() {
            @Override
            public MyLibraryDocument loadObject(long id) throws NotFoundException {
                return myLibraryManager.get(id);
            }

            @Override
            public MyLibraryDocument loadObject(String id) throws NotFoundException {
                return loadObject(Long.parseLong(id));
            }

            @Override
            public MyLibraryDocument loadProxyObject(long id) throws NotFoundException, UnauthorizedException {
                return myLibraryManager.get(id);
            }

            @Override
            public MyLibraryDocument loadProxyObject(String id) throws NotFoundException, UnauthorizedException {
                return loadProxyObject(Long.parseLong(id));
            }

            @Override
            public MyLibraryDocument createProxy(MyLibraryDocument obj, AuthToken auth) {
                return ProxyUtils.proxyObject(MyLibraryDocumentProxy.class, obj, auth);
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public MyLibraryManager getMyLibraryManager() {
        return myLibraryManager;
    }

    public void setMyLibraryManager(final MyLibraryManager myLibraryManager) {
        this.myLibraryManager = myLibraryManager;
    }

    @Override
    public MyLibraryDocumentEntitlementCheckProvider getEntitlementCheckProvider() {
        return documentEntitlementCheckProvider;
    }

    public void setDocumentEntitlementCheckProvider(final MyLibraryDocumentEntitlementCheckProvider myLibraryDocumentEntitlementCheckProvider) {
        this.documentEntitlementCheckProvider = myLibraryDocumentEntitlementCheckProvider;
    }
}
