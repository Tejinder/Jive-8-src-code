package com.grail.synchro.beans;


/**
 *
 */
public class FieldAttachmentBean extends BeanObject {

    private Long projectId;
    private Long endMarketId;
    private Long fieldCategoryId;
    private Long attachmentId;
    private Long userId;
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public Long getEndMarketId() {
		return endMarketId;
	}
	public void setEndMarketId(Long endMarketId) {
		this.endMarketId = endMarketId;
	}
	public Long getFieldCategoryId() {
		return fieldCategoryId;
	}
	public void setFieldCategoryId(Long fieldCategoryId) {
		this.fieldCategoryId = fieldCategoryId;
	}
	public Long getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
    
}
