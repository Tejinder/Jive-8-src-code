package com.grail.synchro.proxy;

import com.grail.synchro.object.MyLibraryDocument;
import com.jivesoftware.base.AuthToken;
import com.jivesoftware.base.proxy.JiveProxy;
import com.jivesoftware.community.license.JiveLicenseManager;

/**
 *
 */
public class MyLibraryDocumentProxy extends MyLibraryDocument implements JiveProxy<MyLibraryDocument> {
    protected MyLibraryDocument myLibraryDocument;
    protected AuthToken authToken;
    private JiveLicenseManager licenseManager;

    @Override
    public void init(MyLibraryDocument target, AuthToken authToken) {
        this.myLibraryDocument = target;
        this.authToken = authToken;
    }

    @Override
    public MyLibraryDocument getUnproxiedObject() {
        return myLibraryDocument;
    }

    @Override
    public AuthToken getProxyAuthToken() {
        return authToken;
    }
}
