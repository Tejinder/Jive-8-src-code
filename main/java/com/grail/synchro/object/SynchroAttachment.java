package com.grail.synchro.object;

import com.grail.synchro.objecttype.SynchroAttachmentObjectType;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.JiveObject;

/**
 *
 */
public class SynchroAttachment implements JiveObject, AttachmentContentResource {

    private SynchroAttachmentBean bean;

    public SynchroAttachment() {
        if(bean == null) {
            bean = new SynchroAttachmentBean();
        }
    }

    public SynchroAttachment(final SynchroAttachmentBean bean) {
        this.bean = bean;
    }

    @Override
    public long getID() {
        return bean.getObjectId();
    }

    @Override
    public int getObjectType() {
        return bean.getObjectType();
    }

    public SynchroAttachmentBean getBean() {
        return bean;
    }

    public void setBean(SynchroAttachmentBean bean) {
        this.bean = bean;
    }


}
