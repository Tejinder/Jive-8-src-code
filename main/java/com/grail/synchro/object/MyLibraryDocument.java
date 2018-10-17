package com.grail.synchro.object;

import com.grail.synchro.beans.MyLibraryDocumentBean;
import com.grail.synchro.objecttype.MyLibraryDocumentObjectType;
import com.jivesoftware.base.User;
import com.jivesoftware.community.*;
import com.jivesoftware.community.impl.DatabaseObjectIterator;
import com.jivesoftware.util.LongList;

/**
 *
 */
public class MyLibraryDocument implements JiveObject, AttachmentContentResource {

    private MyLibraryDocumentBean bean;
    private boolean versionableFieldChanged;

    public MyLibraryDocument() {
        if(bean == null) {
            bean = new MyLibraryDocumentBean();
        }
    }

    public MyLibraryDocument(final MyLibraryDocumentBean bean) {
        this.bean = bean;
    }

    public MyLibraryDocument(final MyLibraryDocumentBean bean, final boolean versionableFieldChanged) {
        this.bean = bean;
        this.versionableFieldChanged = versionableFieldChanged;
    }


    @Override
    public long getID() {
        return this.getBean().getId();
    }

    @Override
    public int getObjectType() {
        return MyLibraryDocumentObjectType.MY_LIBRARY_DOCUMENT_OBJECT_TYPE_ID;
    }


    public boolean isVersionableFieldChanged() {
        return versionableFieldChanged;
    }

    public void setVersionableFieldChanged(boolean versionableFieldChanged) {
        this.versionableFieldChanged = versionableFieldChanged;
    }

    public MyLibraryDocumentBean getBean() {
        return bean;
    }

    public void setBean(MyLibraryDocumentBean bean) {
        this.bean = bean;
    }
}
