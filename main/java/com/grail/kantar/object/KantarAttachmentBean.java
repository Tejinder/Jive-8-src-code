package com.grail.kantar.object;

import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 11/6/14
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarAttachmentBean {

    private long objectId;
    private int objectType;

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(Integer objectType) {
        this.objectType = objectType;
    }
}
