package com.grail.osp.beans;

import com.grail.synchro.beans.BeanObject;

public class OSPTile extends BeanObject {

	private Long tileId;
	private String tileName;
	private String tileImageURL;
	public Long getTileId() {
		return tileId;
	}
	public void setTileId(Long tileId) {
		this.tileId = tileId;
	}
	public String getTileName() {
		return tileName;
	}
	public void setTileName(String tileName) {
		this.tileName = tileName;
	}
	public String getTileImageURL() {
		return tileImageURL;
	}
	public void setTileImageURL(String tileImageURL) {
		this.tileImageURL = tileImageURL;
	}
		
	
}
