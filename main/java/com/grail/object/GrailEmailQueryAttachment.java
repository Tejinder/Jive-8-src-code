package com.grail.object;

import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.JiveObject;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/2/15
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailEmailQueryAttachment implements JiveObject, AttachmentContentResource {

    private GrailEmailQueryAttachmentBean bean;

    public GrailEmailQueryAttachment() {
        if(bean == null) {
            bean = new GrailEmailQueryAttachmentBean();
        }
    }

    public GrailEmailQueryAttachment(GrailEmailQueryAttachmentBean bean) {
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

    public GrailEmailQueryAttachmentBean getBean() {
        return bean;
    }

    public void setBean(GrailEmailQueryAttachmentBean bean) {
        this.bean = bean;
    }
}
