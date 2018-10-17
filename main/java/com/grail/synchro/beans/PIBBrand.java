package com.grail.synchro.beans;

import java.util.List;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class PIBBrand {

    private Long projectID;
    private Long endmarketID;
    private List<Long> brandId;
    private Integer brandMappingType;
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public Long getEndmarketID() {
		return endmarketID;
	}
	public void setEndmarketID(Long endmarketID) {
		this.endmarketID = endmarketID;
	}
	public List<Long> getBrandId() {
		return brandId;
	}
	public void setBrandId(List<Long> brandId) {
		this.brandId = brandId;
	}
	public Integer getBrandMappingType() {
		return brandMappingType;
	}
	public void setBrandMappingType(Integer brandMappingType) {
		this.brandMappingType = brandMappingType;
	}

   
}
