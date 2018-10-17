package com.grail.osp.beans;

import com.grail.synchro.beans.BeanObject;

public class OSPFolder extends BeanObject {

	private Long folderId;
	private Long tileId;
	private String folderName;
	private Long folderSize;
	private String modifiedDateString;
	
	private String folderSizeString;
	
	public Long getTileId() {
		return tileId;
	}
	public void setTileId(Long tileId) {
		this.tileId = tileId;
	}
	public Long getFolderId() {
		return folderId;
	}
	public void setFolderId(Long folderId) {
		this.folderId = folderId;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public Long getFolderSize() {
		return folderSize;
	}
	public void setFolderSize(Long folderSize) {
		this.folderSize = folderSize;
	}
	public String getModifiedDateString() {
		return modifiedDateString;
	}
	public void setModifiedDateString(String modifiedDateString) {
		this.modifiedDateString = modifiedDateString;
	}
	public String getFolderSizeString() {
		return folderSizeString;
	}
	public void setFolderSizeString(String folderSizeString) {
		this.folderSizeString = folderSizeString;
	}

		
	
}
