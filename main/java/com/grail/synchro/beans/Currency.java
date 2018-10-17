package com.grail.synchro.beans;

public class Currency {

	private Long id;
	private String name;
	private String description;
	private boolean isGlobal;
	private boolean isDefault;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isGlobal() {
		return isGlobal;
	}
	public void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}
	public boolean isDefault() {
		return isDefault;
	}
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	
}
