package com.grail.object;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/6/15
 * Time: 5:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailAttachmentBean {
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
