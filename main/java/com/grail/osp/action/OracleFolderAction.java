package com.grail.osp.action;


import java.util.List;

import com.grail.osp.beans.OSPFolder;
import com.grail.osp.beans.OSPTile;
import com.grail.osp.manager.OSPManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * User: Tejinder
 * Date: 11/20/17
 */
public class OracleFolderAction extends JiveActionSupport {

	private List<OSPFolder> oracleFolderList;
	private OSPManager ospManager;
	private Long tileID;
	private Long renameFolderID;
	private String deleteFolderIDs;
	private String folderName;
	private String renameFolderName;
	private OSPTile ospTile;
	
    
	public String input()
	{
       
		//oracleTileList = ospManager.getOracleTiles();
		oracleFolderList = ospManager.getOracleFolders(tileID);
		ospTile = ospManager.getOracleTile(tileID);
		
		return INPUT;
    }
	
	@Override
    public String execute() {
        if(!SynchroPermHelper.canEditOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
       // oracleTileList = ospManager.getOracleTiles();
        
        OSPFolder ospFolder = new OSPFolder();
        ospFolder.setTileId(tileID);
        ospFolder.setFolderName(folderName);
        //ospFolder.setFolderSize(new Long("123"));
        
        ospFolder.setCreationBy(getUser().getID());
        ospFolder.setCreationDate(System.currentTimeMillis());

        ospFolder.setModifiedBy(getUser().getID());
        ospFolder.setModifiedDate(System.currentTimeMillis());
        ospManager.saveOracleFolder(ospFolder);
        
        return SUCCESS;
    }
	

    public String renameFolder() {
        if(!SynchroPermHelper.canEditOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
       // oracleTileList = ospManager.getOracleTiles();
        
        OSPFolder ospFolder = new OSPFolder();
        ospFolder.setTileId(tileID);
        ospFolder.setFolderId(renameFolderID);
        ospFolder.setFolderName(renameFolderName);
        //ospFolder.setFolderSize(new Long("123"));
      
        ospFolder.setModifiedBy(getUser().getID());
        ospFolder.setModifiedDate(System.currentTimeMillis());
        ospManager.updateOracleFolderName(ospFolder);
        
        return SUCCESS;
    }
	
    public String delete() {
    
    	if(deleteFolderIDs.contains(","))
    	{
	        String[] delFolderId = deleteFolderIDs.split(",");
	    	for(String dFolderId:delFolderId)
	    	{	
	    		ospManager.deleteOracleFolder(new Long(dFolderId));
	    	}
    	}
    	else
    	{
    		ospManager.deleteOracleFolder(new Long(deleteFolderIDs));
    	}
        
        return SUCCESS;
    }
	public OSPManager getOspManager() {
		return ospManager;
	}
	public void setOspManager(OSPManager ospManager) {
		this.ospManager = ospManager;
	}
	public Long getTileID() {
		return tileID;
	}
	public void setTileID(Long tileID) {
		this.tileID = tileID;
	}

	public List<OSPFolder> getOracleFolderList() {
		return oracleFolderList;
	}

	public void setOracleFolderList(List<OSPFolder> oracleFolderList) {
		this.oracleFolderList = oracleFolderList;
	}

	public OSPTile getOspTile() {
		return ospTile;
	}

	public void setOspTile(OSPTile ospTile) {
		this.ospTile = ospTile;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getDeleteFolderIDs() {
		return deleteFolderIDs;
	}

	public void setDeleteFolderIDs(String deleteFolderIDs) {
		this.deleteFolderIDs = deleteFolderIDs;
	}

	public String getRenameFolderName() {
		return renameFolderName;
	}

	public void setRenameFolderName(String renameFolderName) {
		this.renameFolderName = renameFolderName;
	}

	public Long getRenameFolderID() {
		return renameFolderID;
	}

	public void setRenameFolderID(Long renameFolderID) {
		this.renameFolderID = renameFolderID;
	}

	

	

	

}
