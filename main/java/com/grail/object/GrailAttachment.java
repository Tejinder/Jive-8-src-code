package com.grail.object;

import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.JiveObject;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/6/15
 * Time: 5:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailAttachment implements JiveObject, AttachmentContentResource {
    private GrailAttachmentBean bean;

    public GrailAttachment() {
        if(bean == null) {
            bean = new GrailAttachmentBean();
        }
    }

    public GrailAttachment(GrailAttachmentBean bean) {
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

    public GrailAttachmentBean getBean() {
        return bean;
    }

    public void setBean(GrailAttachmentBean bean) {
        this.bean = bean;
    }
}
