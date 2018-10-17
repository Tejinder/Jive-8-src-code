package com.grail.synchro.beans;

public class MetaFieldMapping {
	
	public MetaFieldMapping()
	{
	
	}
	public MetaFieldMapping(Long eid, Long id)
	{
		this.eid = eid;
		this.id = id;
	}
	
	private Long eid;
	private Long id;
	public Long getEid() {
		return eid;
	}
	public void setEid(Long eid) {
		this.eid = eid;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	
}
