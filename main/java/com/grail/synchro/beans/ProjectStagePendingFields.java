package com.grail.synchro.beans;



/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectStagePendingFields extends BeanObject {

	  
	  private String fieldName;
	  private Boolean informationProvided;
	  private String displayInformation;
	  private String attachmentDone;
	  private Long endMarketId;
	  
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Boolean getInformationProvided() {
		return informationProvided;
	}
	public void setInformationProvided(Boolean informationProvided) {
		this.informationProvided = informationProvided;
	}
	public String getAttachmentDone() {
		return attachmentDone;
	}
	public void setAttachmentDone(String attachmentDone) {
		this.attachmentDone = attachmentDone;
	}
	public Long getEndMarketId() {
		return endMarketId;
	}
	public void setEndMarketId(Long endMarketId) {
		this.endMarketId = endMarketId;
	}
	public String getDisplayInformation() {
		return displayInformation;
	}
	public void setDisplayInformation(String displayInformation) {
		this.displayInformation = displayInformation;
	}
	
	
	  	
}
