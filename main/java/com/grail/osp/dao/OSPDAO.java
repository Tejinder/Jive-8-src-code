package com.grail.osp.dao;

import java.util.List;

import com.grail.osp.beans.OSPFile;
import com.grail.osp.beans.OSPFolder;
import com.grail.osp.beans.OSPTile;
import com.grail.synchro.search.filter.ProjectResultFilter;

/**
 * @author Tejinder
 * @version 1.0
 */

public interface OSPDAO {
	List<OSPTile> getOracleTiles();
	OSPTile getOracleTile(final Long tileId);
	
	List<OSPFolder> getOracleFolders(final Long tileId, final ProjectResultFilter projectResultFilter);
	Long getOracleFoldersTotalCount(final Long tileId);
	
	List<OSPFolder> getOracleFolders(final Long tileId);
	OSPFolder getOracleFolder(final Long tileId, final Long folderId);
	List<OSPFile> getOracleFiles(final Long tileId, final Long folderId);
	
	List<OSPFile> getOracleFiles(final Long tileId, final Long folderId,final ProjectResultFilter projectResultFilter);
	Long getOracleFilesTotalCount(final Long tileId , final Long folderId);
	
	void saveOracleFolder(final OSPFolder ospFolder);
	void saveOracleFile(final OSPFile ospFile);
	void deleteOracleFolder(final Long folderId);
	void deleteOracleFile(final Long fileId);
	
	void updateOracleFolderName(final OSPFolder ospFolder);
	void updateOracleFolderSize(final OSPFolder ospFolder);
	boolean isValidFolderName(final OSPFolder ospFolder);
	boolean isValidOracleFileName(final OSPFile ospFile);
	void saveIRISDoc(final long docId, final String studyOverview, final String relatedStudies, final long synchroCode);
	
	List<OSPTile> getShareTiles();
	OSPTile getShareTile(final Long tileId);
	List<OSPFolder> getShareFolders(final Long tileId);
	List<OSPFolder> getShareFolders(final Long tileId, final ProjectResultFilter projectResultFilter);
	Long getShareFoldersTotalCount(final Long tileId);
	OSPFolder getShareFolder(final Long tileId, final Long folderId);
	void saveShareFolder(final OSPFolder ospFolder);
	boolean isValidShareFolderName(final OSPFolder ospFolder);
	
	void updateShareFolderName(final OSPFolder ospFolder);
	void updateShareFolderSize(final OSPFolder ospFolder);
	
	void deleteShareFolder(final Long folderId);
	void deleteShareFile(final Long fileId);
	void saveShareFile(final OSPFile ospFile);
	
	boolean isValidShareFileName(final OSPFile ospFile);
	
    List<OSPFile> getShareFiles(final Long tileId, final Long folderId);
    
    List<OSPFile> getShareFiles(final Long tileId, final Long folderId,final ProjectResultFilter projectResultFilter);
    
    Long getShareFilesTotalCount(final Long tileId , final Long folderId);
	 
	 
}
