package com.grail.synchro.object;

import com.grail.synchro.beans.DefaultTemplateBean;
import com.grail.synchro.beans.MyLibraryDocumentBean;
import com.grail.synchro.objecttype.DefaultTemplateObjectType;
import com.grail.synchro.objecttype.MyLibraryDocumentObjectType;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.JiveObject;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplate implements JiveObject, AttachmentContentResource {

    private DefaultTemplateBean bean;
    private boolean versionableFieldChanged;

    public DefaultTemplate() {
        if(bean == null) {
            bean = new DefaultTemplateBean();
        }
    }

    public DefaultTemplate(final DefaultTemplateBean bean) {
        this.bean = bean;
    }

    public DefaultTemplate(final DefaultTemplateBean bean, final boolean versionableFieldChanged) {
        this.bean = bean;
        this.versionableFieldChanged = versionableFieldChanged;
    }


    @Override
    public long getID() {
        return this.getBean().getId();
    }

    @Override
    public int getObjectType() {
        return DefaultTemplateObjectType.DEFAULT_TEMPLATE_OBJECT_TYPE_ID;
    }


    public boolean isVersionableFieldChanged() {
        return versionableFieldChanged;
    }

    public void setVersionableFieldChanged(boolean versionableFieldChanged) {
        this.versionableFieldChanged = versionableFieldChanged;
    }

    public DefaultTemplateBean getBean() {
        return bean;
    }

    public void setBean(DefaultTemplateBean bean) {
        this.bean = bean;
    }

}
