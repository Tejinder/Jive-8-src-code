package com.grail.osp.object;

import com.grail.osp.beans.OSPFile;
import com.grail.osp.objecttype.OSPAttachmentObjectType;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.JiveObject;

/**
 *
 */
public class OSPAttachment implements JiveObject, AttachmentContentResource {

    private OSPFile ospFile;
    private boolean versionableFieldChanged;

    public OSPAttachment() {
        if(ospFile == null) {
        	ospFile = new OSPFile();
        }
    }

    public OSPAttachment(OSPFile ospFile) {
        this.ospFile = ospFile;
    }

    public OSPAttachment(final OSPFile ospFile, final boolean versionableFieldChanged) {
        this.ospFile = ospFile;
        this.versionableFieldChanged = versionableFieldChanged;
    }


    @Override
    public long getID() {
        return this.getOspFile().getAttachmentId();
    }

    @Override
    public int getObjectType() {
        return OSPAttachmentObjectType.OSP_DOCUMENT_OBJECT_TYPE_ID;
    }


    public boolean isVersionableFieldChanged() {
        return versionableFieldChanged;
    }

    public void setVersionableFieldChanged(boolean versionableFieldChanged) {
        this.versionableFieldChanged = versionableFieldChanged;
    }

	public OSPFile getOspFile() {
		return ospFile;
	}

	public void setOspFile(OSPFile ospFile) {
		this.ospFile = ospFile;
	}

   
}
