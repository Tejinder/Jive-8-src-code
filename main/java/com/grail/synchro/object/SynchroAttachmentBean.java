package com.grail.synchro.object;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 12/11/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroAttachmentBean {

    // Object Id is the Stage to which the attachment belongs like PIB, Proposal
	private long objectId;
	// Object Type is the Field to which the attachment belongs like Business Question, Action Standard etc
    private Integer objectType;

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public Integer getObjectType() {
        return objectType;
    }

    public void setObjectType(Integer objectType) {
        this.objectType = objectType;
    }
}
