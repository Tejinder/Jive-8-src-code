package com.grail.osp.action;

import org.apache.commons.lang3.StringUtils;

import com.grail.osp.beans.OSPFolder;
import com.grail.osp.beans.OSPTile;
import com.grail.osp.manager.OSPManager;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class OracleFolderDashboardAction extends JiveActionSupport {

	private Long tileID;
	
	private OSPTile ospTile;
	private OSPManager ospManager;
	
	private String folderName;
	private Long hiddenTileID;
	private String renameFolderName;
	private Long renameFolderID;
	private String deleteFolderIDs;
	private String redirectURL;
	
    @Override
    public String execute() {
    	if(!SynchroPermHelper.canAccessOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
       // System.out.println("TILE ID ==>"+ tileID);
        ospTile = ospManager.getOracleTile(tileID);
    	return SUCCESS;
    }
    
    
    
    public String createNewFolder() {
        if(!SynchroPermHelper.canEditOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
       // oracleTileList = ospManager.getOracleTiles();
        
        tileID = hiddenTileID;
        OSPFolder ospFolder = new OSPFolder();
        ospFolder.setTileId(hiddenTileID);
        if(StringUtils.isNotEmpty(folderName))
        {
        	ospFolder.setFolderName(folderName.replaceAll("^\\s+", "").replaceAll("\\s+$",""));
        }
        else
        {
        	ospFolder.setFolderName(folderName);
        }
        ospFolder.setFolderSize(new Long("0"));
        
        ospFolder.setCreationBy(getUser().getID());
        ospFolder.setCreationDate(System.currentTimeMillis());

        ospFolder.setModifiedBy(getUser().getID());
        ospFolder.setModifiedDate(System.currentTimeMillis());
        
        if(ospManager.isValidFolderName(ospFolder))
        {
        	ospManager.saveOracleFolder(ospFolder);
        	redirectURL="oracle-folder-dashboard.jspa?tileID="+tileID;
        }
        else
        {
        	redirectURL="oracle-folder-dashboard.jspa?tileID="+tileID+"&validFolder=false&folderName="+folderName;
        }
       // ospManager.saveOracleFolder(ospFolder);
        
        return "folderCreated";
    }
    
    public String renameFolder() {
        if(!SynchroPermHelper.canEditOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
       // oracleTileList = ospManager.getOracleTiles();
        tileID = hiddenTileID;
        OSPFolder ospFolder = new OSPFolder();
        ospFolder.setTileId(hiddenTileID);
        ospFolder.setFolderId(renameFolderID);
        
       // s.replaceAll("\\s+$","");
        
        
        if(StringUtils.isNotEmpty(renameFolderName))
        {
        	ospFolder.setFolderName(renameFolderName.replaceAll("^\\s+", "").replaceAll("\\s+$",""));
        }
        else
        {
        	ospFolder.setFolderName(renameFolderName);
        }
        
        //ospFolder.setFolderSize(new Long("123"));
      
        ospFolder.setModifiedBy(getUser().getID());
        ospFolder.setModifiedDate(System.currentTimeMillis());
        
        if(ospManager.isValidFolderName(ospFolder))
        {
        	ospManager.updateOracleFolderName(ospFolder);
        	redirectURL="oracle-folder-dashboard.jspa?tileID="+tileID;
        }
        else
        {
        	redirectURL="oracle-folder-dashboard.jspa?tileID="+tileID+"&validFolder=false&folderName="+renameFolderName;
        }
        
        
        return "folderRenamed";
    }
    
    public String delete() {
        
    	tileID = hiddenTileID;
    	
    	if(StringUtils.isNotBlank(deleteFolderIDs))
    	{
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
    	}
        
    	  return "folderDeleted";
    }
	public Long getTileID() {
		return tileID;
	}
	public void setTileID(Long tileID) {
		this.tileID = tileID;
	}
	public OSPTile getOspTile() {
		return ospTile;
	}
	public void setOspTile(OSPTile ospTile) {
		this.ospTile = ospTile;
	}
	public OSPManager getOspManager() {
		return ospManager;
	}
	public void setOspManager(OSPManager ospManager) {
		this.ospManager = ospManager;
	}



	public String getFolderName() {
		return folderName;
	}



	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}



	public Long getHiddenTileID() {
		return hiddenTileID;
	}



	public void setHiddenTileID(Long hiddenTileID) {
		this.hiddenTileID = hiddenTileID;
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



	public String getDeleteFolderIDs() {
		return deleteFolderIDs;
	}



	public void setDeleteFolderIDs(String deleteFolderIDs) {
		this.deleteFolderIDs = deleteFolderIDs;
	}



	public String getRedirectURL() {
		return redirectURL;
	}



	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

   
}
