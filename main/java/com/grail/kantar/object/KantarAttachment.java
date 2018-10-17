package com.grail.kantar.object;

import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.JiveObject;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 11/6/14
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarAttachment  implements JiveObject, AttachmentContentResource {

    private KantarAttachmentBean bean;

    public KantarAttachment() {
        if(bean == null) {
            bean = new KantarAttachmentBean();
        }
    }

    public KantarAttachment(KantarAttachmentBean bean) {
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

    public KantarAttachmentBean getBean() {
        return bean;
    }

    public void setBean(KantarAttachmentBean bean) {
        this.bean = bean;
    }
}
