package com.grail.osp.beans;

import com.grail.synchro.beans.BeanObject;

public class OSPFile extends BeanObject {

	private Long folderId;
	private Long tileId;
	private Long attachmentId;
	private String fileName;
	private Long fileSize;
	private String modifiedDateString;
	
	private String fileSizeString;
	
	private Long objectId;
	
	public Long getFolderId() {
		return folderId;
	}
	public void setFolderId(Long folderId) {
		this.folderId = folderId;
	}
	public Long getTileId() {
		return tileId;
	}
	public void setTileId(Long tileId) {
		this.tileId = tileId;
	}
	public Long getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public String getModifiedDateString() {
		return modifiedDateString;
	}
	public void setModifiedDateString(String modifiedDateString) {
		this.modifiedDateString = modifiedDateString;
	}
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	public String getFileSizeString() {
		return fileSizeString;
	}
	public void setFileSizeString(String fileSizeString) {
		this.fileSizeString = fileSizeString;
	}
	
	
		
	
}
