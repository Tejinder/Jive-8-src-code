package com.grail.osp.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.grail.osp.beans.OSPFile;
import com.grail.osp.beans.OSPFolder;
import com.grail.osp.beans.OSPTile;
import com.grail.osp.dao.OSPDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.util.ByteFormat;

/**
 * @author Tejinder
 * @version 1.0
 */

public class OSPDAOImpl extends JiveJdbcDaoSupport implements OSPDAO {
	
	private static final String LOAD_ORACLE_TILES = " SELECT tileid, tilename, tileimageurl, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailoracletile where isactive = 1 order by tileid";
	
	private static final String LOAD_SHARE_TILES = " SELECT tileid, tilename, tileimageurl, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailsharetile where isactive = 1 order by tileid";
	
	private static final String LOAD_ORACLE_TILE_TILE_ID = " SELECT tileid, tilename, tileimageurl, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailoracletile where tileid = ? and isactive = 1";
	
	private static final String LOAD_SHARE_TILE_TILE_ID = " SELECT tileid, tilename, tileimageurl, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailsharetile where tileid = ? and isactive = 1";
	
	private static final String LOAD_ORACLE_FOLDERS = " SELECT folderid, tileid, foldername, foldersize, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailoraclefolder where tileid = ? and isactive = 1 order by modificationdate desc";
	
	private static final String LOAD_SHARE_FOLDERS = " SELECT folderid, tileid, foldername, foldersize, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailsharefolder where tileid = ? and isactive = 1 order by modificationdate desc";
	
	private static final String LOAD_ORACLE_FOLDER_FOLDER_ID = " SELECT folderid, tileid, foldername, foldersize, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailoraclefolder where tileid = ? and folderid = ? and isactive = 1 order by modificationdate desc";
	
	private static final String LOAD_SHARE_FOLDER_FOLDER_ID = " SELECT folderid, tileid, foldername, foldersize, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailsharefolder where tileid = ? and folderid = ? and isactive = 1 order by modificationdate desc";
	
	private static final String LOAD_ORACLE_FILES = " SELECT attachmentid, folderid, tileid, filename, filesize, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailoraclefile where tileid = ? and folderid=? and isactive = 1 order by modificationdate desc";
	
	private static final String LOAD_SHARE_FILES = " SELECT attachmentid, folderid, tileid, filename, filesize, " +
            "       isactive, creationby, modificationby, creationdate, modificationdate " +
            "  FROM grailsharefile where tileid = ? and folderid=? and isactive = 1 order by modificationdate desc";
	
	private static final String INSERT_ORACLE_FOLDER = " INSERT INTO grailoraclefolder (folderid, tileid, foldername, foldersize,isactive, creationby, modificationby, creationdate, modificationdate) " +
			" VALUES (?,?,?,?,?,?,?,?,?)";
	
	
	private static final String INSERT_SHARE_FOLDER = " INSERT INTO grailsharefolder (folderid, tileid, foldername, foldersize,isactive, creationby, modificationby, creationdate, modificationdate) " +
			" VALUES (?,?,?,?,?,?,?,?,?)";
	
	
	private static final String UPDATE_ORACLE_FOLDERNAME = " update grailoraclefolder set foldername=?, modificationby=?, modificationdate=? where folderid=? ";
	
	private static final String UPDATE_SHARE_FOLDERNAME = " update grailsharefolder set foldername=?, modificationby=?, modificationdate=? where folderid=? ";
	
	private static final String UPDATE_ORACLE_FOLDERSIZE = " update grailoraclefolder set foldersize=?, modificationby=?, modificationdate=? where folderid=? ";
	
	private static final String UPDATE_SHARE_FOLDERSIZE = " update grailsharefolder set foldersize=?, modificationby=?, modificationdate=? where folderid=? ";
	
	private static final String INSERT_ORACLE_FILE = " INSERT INTO grailoraclefile (attachmentid, folderid, tileid, filename, filesize, isactive, creationby, modificationby, creationdate, modificationdate) " +
			" VALUES (?,?,?,?,?,?,?,?,?,?)";
	
	private static final String INSERT_SHARE_FILE = " INSERT INTO grailsharefile (attachmentid, folderid, tileid, filename, filesize, isactive, creationby, modificationby, creationdate, modificationdate) " +
			" VALUES (?,?,?,?,?,?,?,?,?,?)";
	
	private static final String DELETE_ORACLE_FOLDER = " DELETE FROM grailoraclefolder where folderid = ? ";
	
	private static final String DELETE_ORACLE_FOLDER_FILE = " DELETE FROM grailoraclefile where folderid = ?  ";
	
	private static final String DELETE_ORACLE_FILE = " DELETE FROM grailoraclefile where attachmentid = ?  ";
	
	private static final String DELETE_SHARE_FOLDER = " DELETE FROM grailsharefolder where folderid = ? ";
	
	private static final String DELETE_SHARE_FOLDER_FILE = " DELETE FROM grailsharefile where folderid = ?  ";
	
	private static final String DELETE_SHARE_FILE = " DELETE FROM grailsharefile where attachmentid = ?  ";
	
	private SynchroDAOUtil synchroDAOUtil;
	
	private static final Logger LOG = Logger.getLogger(OSPDAOImpl.class.getName());
	 
	 
	@Override
	public List<OSPTile> getOracleTiles()
	{
		return getSimpleJdbcTemplate().query(LOAD_ORACLE_TILES, ospTileRowMapper);
        
	}
	
	@Override
	public List<OSPTile> getShareTiles()
	{
		return getSimpleJdbcTemplate().query(LOAD_SHARE_TILES, ospTileRowMapper);
        
	}
	
	@Override
	public OSPTile getOracleTile(final Long tileId)
	{
		OSPTile ospTile = new OSPTile(); 
		try 
		{
			ospTile = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_ORACLE_TILE_TILE_ID, ospTileRowMapper, tileId);
	    }
	    catch (DataAccessException e) 
	    {
	        final String message = "Failed to Fetch  Oracle Tile -" + tileId;
	        LOG.log(Level.SEVERE, message, e);
        }
 	    return ospTile;
	}
	
	@Override
	public OSPTile getShareTile(final Long tileId)
	{
		OSPTile ospTile = new OSPTile(); 
		try 
		{
			ospTile = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_SHARE_TILE_TILE_ID, ospTileRowMapper, tileId);
	    }
	    catch (DataAccessException e) 
	    {
	        final String message = "Failed to Fetch  Share Tile -" + tileId;
	        LOG.log(Level.SEVERE, message, e);
        }
 	    return ospTile;
	}
	
	@Override
	public List<OSPFolder> getOracleFolders(final Long tileId)
	{
		return getSimpleJdbcTemplate().query(LOAD_ORACLE_FOLDERS, ospFolderRowMapper, tileId);
        
	}
	
	@Override
	public List<OSPFolder> getShareFolders(final Long tileId)
	{
		return getSimpleJdbcTemplate().query(LOAD_SHARE_FOLDERS, ospFolderRowMapper, tileId);
        
	}
	
	@Override
	public List<OSPFolder> getOracleFolders(final Long tileId, final ProjectResultFilter projectResultFilter)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT folderid, tileid, foldername, foldersize,  isactive, creationby, modificationby, creationdate, modificationdate  FROM grailoraclefolder where tileid = "+tileId+" and isactive = 1");
		
		List<OSPFolder> ospFolders = Collections.emptyList();
		
		 
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder()));
        } 
        else 
        {
        	sql.append(" order by modificationdate desc");
        }
		try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            ospFolders = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), ospFolderRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Oracle Folders";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
		
		return ospFolders; 
		//getSimpleJdbcTemplate().query(LOAD_ORACLE_FOLDERS, ospFolderRowMapper, tileId);
        
	}
	
	@Override
	public List<OSPFolder> getShareFolders(final Long tileId, final ProjectResultFilter projectResultFilter)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT folderid, tileid, foldername, foldersize,  isactive, creationby, modificationby, creationdate, modificationdate  FROM grailsharefolder where tileid = "+tileId+" and isactive = 1");
		
		List<OSPFolder> ospFolders = Collections.emptyList();
		
		 
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder()));
        } 
        else 
        {
        	sql.append(" order by modificationdate desc");
        }
		try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            ospFolders = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), ospFolderRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Share Folders";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
		
		return ospFolders; 
		//getSimpleJdbcTemplate().query(LOAD_ORACLE_FOLDERS, ospFolderRowMapper, tileId);
        
	}
	
	 @Override
	    public Long getOracleFoldersTotalCount(final Long tileId) {
	        Long count = 0L;
	       
	        StringBuilder sql = new StringBuilder("SELECT count(*)  FROM grailoraclefolder where tileid = "+tileId+" and isactive = 1");
	    	
	    	/*String waiverFields = "select count(*) from grailpibmethodologywaiver waiver, grailproject project where waiver.projectid = project.projectid ";
	        sql = new StringBuilder(waiverFields);
	        */
	        
	       
	        try {
	            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	        	//String sql  = "select count(*) from grailproject p";
	        	//count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
	        }
	        catch (DataAccessException e) {
	            throw new DAOException(e.getMessage(), e);
	        }
	        return count;
	    }
	
	 @Override
	    public Long getShareFoldersTotalCount(final Long tileId) {
	        Long count = 0L;
	       
	        StringBuilder sql = new StringBuilder("SELECT count(*)  FROM grailsharefolder where tileid = "+tileId+" and isactive = 1");
	    	
	    
	        
	       
	        try {
	            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	        	//String sql  = "select count(*) from grailproject p";
	        	//count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
	        }
	        catch (DataAccessException e) {
	            throw new DAOException(e.getMessage(), e);
	        }
	        return count;
	    }
	 
	@Override
	public OSPFolder getOracleFolder(final Long tileId, final Long folderId)
	{
		return getSimpleJdbcTemplate().queryForObject(LOAD_ORACLE_FOLDER_FOLDER_ID, ospFolderRowMapper, tileId, folderId);
        
	}
	
	 
		@Override
		public OSPFolder getShareFolder(final Long tileId, final Long folderId)
		{
			return getSimpleJdbcTemplate().queryForObject(LOAD_SHARE_FOLDER_FOLDER_ID, ospFolderRowMapper, tileId, folderId);
	        
		}
	 @Override
	 public void saveOracleFolder(final OSPFolder ospFolder)
	 {
		 Long id = synchroDAOUtil.nextSequenceID("folderid", "grailoraclefolder");
	        try {
	        	ospFolder.setFolderId(id);
	            getJdbcTemplate().update(INSERT_ORACLE_FOLDER,
	            		ospFolder.getFolderId(),
	            		ospFolder.getTileId(),
	            		ospFolder.getFolderName(),
	            		ospFolder.getFolderSize(),
	                    1,
	                    ospFolder.getCreationBy(),
	                    ospFolder.getModifiedBy(),
	                    ospFolder.getCreationDate(),
	                    ospFolder.getModifiedDate()
	                   
	            );

	        } catch (DAOException e) {
	        	 final String message = "Failed to Create Folder for Oracle Tile Id  -- " + ospFolder.getTileId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	
	 @Override
	 public void saveShareFolder(final OSPFolder ospFolder)
	 {
		 Long id = synchroDAOUtil.nextSequenceID("folderid", "grailsharefolder");
	        try {
	        	ospFolder.setFolderId(id);
	            getJdbcTemplate().update(INSERT_SHARE_FOLDER,
	            		ospFolder.getFolderId(),
	            		ospFolder.getTileId(),
	            		ospFolder.getFolderName(),
	            		ospFolder.getFolderSize(),
	                    1,
	                    ospFolder.getCreationBy(),
	                    ospFolder.getModifiedBy(),
	                    ospFolder.getCreationDate(),
	                    ospFolder.getModifiedDate()
	                   
	            );

	        } catch (DAOException e) {
	        	 final String message = "Failed to Create Folder for Share Tile Id  -- " + ospFolder.getTileId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	 
	 @Override
	 public boolean isValidFolderName(final OSPFolder ospFolder)
	 {
		 
		 Long count = 0L;
		 StringBuilder sql = new StringBuilder("SELECT count(*)  FROM grailoraclefolder where tileid = "+ospFolder.getTileId()+" and lower(foldername)='"+ospFolder.getFolderName().toLowerCase()+"' and isactive = 1");
	     try
	     {
	         count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	     }
	     catch (DataAccessException e) 
	     {
	          throw new DAOException(e.getMessage(), e);
	     }
	     if(count>0)
	     {
	       return false;
	     }
	     else
	     {
	       return true;
	     }
		 
		
	 }
	 
	 @Override
	 public boolean isValidShareFolderName(final OSPFolder ospFolder)
	 {
		 
		 Long count = 0L;
		 StringBuilder sql = new StringBuilder("SELECT count(*)  FROM grailsharefolder where tileid = "+ospFolder.getTileId()+" and lower(foldername)='"+ospFolder.getFolderName().toLowerCase()+"' and isactive = 1");
	     try
	     {
	         count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	     }
	     catch (DataAccessException e) 
	     {
	          throw new DAOException(e.getMessage(), e);
	     }
	     if(count>0)
	     {
	       return false;
	     }
	     else
	     {
	       return true;
	     }
		 
		
	 }
	 
	 @Override
	 public void updateOracleFolderName(final OSPFolder ospFolder)
	 {
		 
	        try {
	        	 getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_ORACLE_FOLDERNAME,	                     
	        			 ospFolder.getFolderName(), ospFolder.getModifiedBy(), ospFolder.getModifiedDate(), ospFolder.getFolderId());
	                     

	        } catch (DAOException e) {
	        	 final String message = "Failed to Rename Folder for Oracle Folder Id  -- " + ospFolder.getFolderId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	 
	 @Override
	 public void updateShareFolderName(final OSPFolder ospFolder)
	 {
		 
	        try {
	        	 getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_SHARE_FOLDERNAME,	                     
	        			 ospFolder.getFolderName(), ospFolder.getModifiedBy(), ospFolder.getModifiedDate(), ospFolder.getFolderId());
	                     

	        } catch (DAOException e) {
	        	 final String message = "Failed to Rename Folder for Share Folder Id  -- " + ospFolder.getFolderId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	 
	 @Override
	 public void updateOracleFolderSize(final OSPFolder ospFolder)
	 {
		 
	        try {
	        	 getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_ORACLE_FOLDERSIZE,	                     
	        			 ospFolder.getFolderSize(), ospFolder.getModifiedBy(), ospFolder.getModifiedDate(), ospFolder.getFolderId());
	                     

	        } catch (DAOException e) {
	        	 final String message = "Failed to Rename Size for Oracle Folder Id  -- " + ospFolder.getFolderId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	 
	 @Override
	 public void updateShareFolderSize(final OSPFolder ospFolder)
	 {
		 
	        try {
	        	 getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_SHARE_FOLDERSIZE,	                     
	        			 ospFolder.getFolderSize(), ospFolder.getModifiedBy(), ospFolder.getModifiedDate(), ospFolder.getFolderId());
	                     

	        } catch (DAOException e) {
	        	 final String message = "Failed to Rename Size for Share Folder Id  -- " + ospFolder.getFolderId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	 
	 @Override
	 public void deleteOracleFolder(final Long folderId)
	 {
        try 
        {
        	getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ORACLE_FOLDER, folderId);
        	getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ORACLE_FOLDER_FILE, folderId);
        } 
        catch (DAOException e) 
        {
        	 final String message = "Failed to Delete Folder for Folder Id  -- " + folderId;
             LOG.log(Level.SEVERE, message, e);
        }
	 }
	 
	 @Override
	 public void deleteShareFolder(final Long folderId)
	 {
        try 
        {
        	getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_SHARE_FOLDER, folderId);
        	getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_SHARE_FOLDER_FILE, folderId);
        } 
        catch (DAOException e) 
        {
        	 final String message = "Failed to Delete Folder for Folder Id  -- " + folderId;
             LOG.log(Level.SEVERE, message, e);
        }
	 }
	 
	 @Override
	 public void deleteOracleFile(final Long fileId)
	 {
        try 
        {
        	getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_ORACLE_FILE, fileId);
        } 
        catch (DAOException e) 
        {
        	 final String message = "Failed to Delete File for Attachment Id  -- " + fileId;
             LOG.log(Level.SEVERE, message, e);
        }
	 }
	 
	 @Override
	 public void deleteShareFile(final Long fileId)
	 {
        try 
        {
        	getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_SHARE_FILE, fileId);
        } 
        catch (DAOException e) 
        {
        	 final String message = "Failed to Delete File for Attachment Id  -- " + fileId;
             LOG.log(Level.SEVERE, message, e);
        }
	 }
	 
	 @Override
	 public void saveOracleFile(final OSPFile ospFile)
	 {
		 
	        try {
	        	  getJdbcTemplate().update(INSERT_ORACLE_FILE,
	        			ospFile.getAttachmentId(),
	        			ospFile.getFolderId(),
	        			ospFile.getTileId(),
	        			ospFile.getFileName(),
	        			ospFile.getFileSize(),
	                    1,
	                    ospFile.getCreationBy(),
	                    ospFile.getModifiedBy(),
	                    ospFile.getCreationDate(),
	                    ospFile.getModifiedDate()
	                   
	            );

	        } catch (DAOException e) {
	        	 final String message = "Failed to Create File for Oracle Tile Id  -- " + ospFile.getTileId() +" and Folder Id --"+ ospFile.getFolderId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	 
	 
	 @Override
	 public void saveShareFile(final OSPFile ospFile)
	 {
		 
	        try {
	        	  getJdbcTemplate().update(INSERT_SHARE_FILE,
	        			ospFile.getAttachmentId(),
	        			ospFile.getFolderId(),
	        			ospFile.getTileId(),
	        			ospFile.getFileName(),
	        			ospFile.getFileSize(),
	                    1,
	                    ospFile.getCreationBy(),
	                    ospFile.getModifiedBy(),
	                    ospFile.getCreationDate(),
	                    ospFile.getModifiedDate()
	                   
	            );

	        } catch (DAOException e) {
	        	 final String message = "Failed to Create File for Share Tile Id  -- " + ospFile.getTileId() +" and Folder Id --"+ ospFile.getFolderId();
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
	 
	 @Override
	 public boolean isValidOracleFileName(final OSPFile ospFile)
	 {
		 
		 Long count = 0L;
		 StringBuilder sql = new StringBuilder();
		 if(ospFile!=null && ospFile.getFileName()!=null)
		 {
			 sql = new StringBuilder("SELECT count(*)  FROM grailoraclefile where tileid = "+ospFile.getTileId()+" and folderid = "+ospFile.getFolderId()+" and lower(filename)='"+ospFile.getFileName().toLowerCase()+"' and isactive = 1");
		 }
		 else
		 {
			 sql = new StringBuilder("SELECT count(*)  FROM grailoraclefile where tileid = "+ospFile.getTileId()+" and folderid = "+ospFile.getFolderId()+" and filename='"+ospFile.getFileName()+"' and isactive = 1");
		 }
	     try
	     {
	         count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	     }
	     catch (DataAccessException e) 
	     {
	          throw new DAOException(e.getMessage(), e);
	     }
	     if(count>0)
	     {
	       return false;
	     }
	     else
	     {
	       return true;
	     }
		 
		
	 }
	 
	 
	 @Override
	 public boolean isValidShareFileName(final OSPFile ospFile)
	 {
		 
		 Long count = 0L;
		 StringBuilder sql = new StringBuilder();
		 if(ospFile!=null && ospFile.getFileName()!=null)
		 {
			 sql = new StringBuilder("SELECT count(*)  FROM grailsharefile where tileid = "+ospFile.getTileId()+" and folderid = "+ospFile.getFolderId()+" and lower(filename)='"+ospFile.getFileName().toLowerCase()+"' and isactive = 1");
		 }
		 else
		 {
			 sql = new StringBuilder("SELECT count(*)  FROM grailsharefile where tileid = "+ospFile.getTileId()+" and folderid = "+ospFile.getFolderId()+" and filename='"+ospFile.getFileName()+"' and isactive = 1");
		 }
	     try
	     {
	         count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	     }
	     catch (DataAccessException e) 
	     {
	          throw new DAOException(e.getMessage(), e);
	     }
	     if(count>0)
	     {
	       return false;
	     }
	     else
	     {
	       return true;
	     }
		 
		
	 } 
	@Override
	public List<OSPFile> getOracleFiles(final Long tileId, final Long folderId)
	{
		return getSimpleJdbcTemplate().query(LOAD_ORACLE_FILES, ospFileRowMapper, tileId, folderId);
        
	}
	
	@Override
	public List<OSPFile> getShareFiles(final Long tileId, final Long folderId)
	{
		return getSimpleJdbcTemplate().query(LOAD_SHARE_FILES, ospFileRowMapper, tileId, folderId);
        
	}
	
	@Override
	public List<OSPFile> getOracleFiles(final Long tileId, final Long folderId,final ProjectResultFilter projectResultFilter)
	{
		//return getSimpleJdbcTemplate().query(LOAD_ORACLE_FILES, ospFileRowMapper, tileId, folderId);
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT attachmentid, folderid, tileid, filename, filesize, isactive, creationby, modificationby, creationdate, modificationdate FROM grailoraclefile where tileid = "+tileId+" and folderid="+folderId+" and isactive = 1");
		
		List<OSPFile> ospFile = Collections.emptyList();
		
		 
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder()));
        } 
        else 
        {
        	sql.append(" order by modificationdate desc");
        }
        
		try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            ospFile = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), ospFileRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Oracle Files";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
		
		return ospFile; 
		//getSimpleJdbcTemplate().query(LOAD_ORACLE_FOLDERS, ospFolderRowMapper, tileId);
        
	}
	
	
	@Override
	public List<OSPFile> getShareFiles(final Long tileId, final Long folderId,final ProjectResultFilter projectResultFilter)
	{
		//return getSimpleJdbcTemplate().query(LOAD_ORACLE_FILES, ospFileRowMapper, tileId, folderId);
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT attachmentid, folderid, tileid, filename, filesize, isactive, creationby, modificationby, creationdate, modificationdate FROM grailsharefile where tileid = "+tileId+" and folderid="+folderId+" and isactive = 1");
		
		List<OSPFile> ospFile = Collections.emptyList();
		
		 
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder()));
        } 
        else 
        {
        	sql.append(" order by modificationdate desc");
        }
        
		try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            ospFile = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), ospFileRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Share Files";
        //    LOG.error(message, e);
            throw new DAOException(message, e);
        }
		
		return ospFile; 
		//getSimpleJdbcTemplate().query(LOAD_ORACLE_FOLDERS, ospFolderRowMapper, tileId);
        
	}
	
	
	 @Override
	    public Long getOracleFilesTotalCount(final Long tileId , final Long folderId) {
	        Long count = 0L;
	       
	        StringBuilder sql = new StringBuilder("SELECT count(*)  FROM grailoraclefile where tileid = "+tileId+" and folderid="+folderId+" and isactive = 1");
	    	
	    
	       
	        try {
	            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	        }
	        catch (DataAccessException e) {
	            throw new DAOException(e.getMessage(), e);
	        }
	        return count;
	    }
	 
	 @Override
	    public Long getShareFilesTotalCount(final Long tileId , final Long folderId) {
	        Long count = 0L;
	       
	        StringBuilder sql = new StringBuilder("SELECT count(*)  FROM grailsharefile where tileid = "+tileId+" and folderid="+folderId+" and isactive = 1");
	    	
	    
	       
	        try {
	            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
	        }
	        catch (DataAccessException e) {
	            throw new DAOException(e.getMessage(), e);
	        }
	        return count;
	    }
	 /**
     * Reusable row mapper for mapping a result set to OSPTile
     */
    private final RowMapper<OSPTile> ospTileRowMapper = new RowMapper<OSPTile>() {
        public OSPTile mapRow(ResultSet rs, int row) throws SQLException {
        	OSPTile initiationBean = new OSPTile();
            initiationBean.setTileId(rs.getLong("tileid"));
            initiationBean.setTileName(rs.getString("tilename"));
            initiationBean.setTileImageURL(rs.getString("tileimageurl"));           
           
            initiationBean.setCreationBy(rs.getLong("creationby"));
            initiationBean.setModifiedBy(rs.getLong("modificationby"));
            initiationBean.setCreationDate(rs.getLong("creationdate"));
            initiationBean.setModifiedDate(rs.getLong("modificationdate"));
            
            return initiationBean;
        }
    };
    
    /**
     * Reusable row mapper for mapping a result set to OSPFolder
     */
    private final RowMapper<OSPFolder> ospFolderRowMapper = new RowMapper<OSPFolder>() {
        public OSPFolder mapRow(ResultSet rs, int row) throws SQLException {
        	OSPFolder initiationBean = new OSPFolder();
            initiationBean.setTileId(rs.getLong("tileid"));
            initiationBean.setFolderId(rs.getLong("folderid"));
            initiationBean.setFolderName(rs.getString("foldername"));
            initiationBean.setFolderSize(rs.getLong("foldersize"));     
            
            initiationBean.setFolderSizeString(ByteFormat.getInstance().format(rs.getLong("foldersize")));     
           
            initiationBean.setCreationBy(rs.getLong("creationby"));
            initiationBean.setModifiedBy(rs.getLong("modificationby"));
            initiationBean.setCreationDate(rs.getLong("creationdate"));
            initiationBean.setModifiedDate(rs.getLong("modificationdate"));
            initiationBean.setModifiedDateString(SynchroUtils.getModifiedDate(new Date(rs.getLong("modificationdate"))));
            
            return initiationBean;
        }
    };
    
    
    /**
     * Reusable row mapper for mapping a result set to OSPFile
     */
    private final RowMapper<OSPFile> ospFileRowMapper = new RowMapper<OSPFile>() {
        public OSPFile mapRow(ResultSet rs, int row) throws SQLException {
        	OSPFile initiationBean = new OSPFile();
            initiationBean.setAttachmentId(rs.getLong("attachmentid"));
            initiationBean.setTileId(rs.getLong("tileid"));
            initiationBean.setFolderId(rs.getLong("folderid"));
            initiationBean.setFileName(rs.getString("filename"));
            initiationBean.setFileSize(rs.getLong("filesize"));
            
            initiationBean.setFileSizeString(ByteFormat.getInstance().format(rs.getLong("filesize"))); 
                      
            initiationBean.setCreationBy(rs.getLong("creationby"));
            initiationBean.setModifiedBy(rs.getLong("modificationby"));
            initiationBean.setCreationDate(rs.getLong("creationdate"));
            initiationBean.setModifiedDate(rs.getLong("modificationdate"));
            initiationBean.setObjectId(new Long((initiationBean.getTileId() + "-" + initiationBean.getFolderId()).hashCode()));
            
            initiationBean.setModifiedDateString(SynchroUtils.getModifiedDate(new Date(rs.getLong("modificationdate"))));
            
            
            return initiationBean;
        }
    };

    private String getOrderByField(final String sortField) {
        if(StringUtils.isNotBlank(sortField)) {
            String field = null;
            if(sortField.equals("folderid")) {
                field = "folderid";
            } else if(sortField.equals("modificationdate")) {
                field = "modificationdate";
            } else if(sortField.equals("foldername")) {
                field = "foldername";
            } 
            else if(sortField.equals("foldersize")) {
                field = "foldersize";
            } else if(sortField.equals("filename")) {
                field = "filename";
            } else if(sortField.equals("filesize")) {
                field = "filesize";
            } 
            else {
                field = sortField;
            }
            return field;
        }
        return null;
    }
	public SynchroDAOUtil getSynchroDAOUtil() {
		return synchroDAOUtil;
	}

	public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
		this.synchroDAOUtil = synchroDAOUtil;
	}
	
	
	 @Override
	 public void saveIRISDoc(final long docId, final String studyOverview, final String relatedStudies, final long synchroCode)
	 {
		 
		 String INSERT_IRIS = " INSERT INTO irisMetaData (documentId, studyoverview, relatedstudies, synchrocode) " +
					" VALUES (?,?,?,?)";
	        try {
	        	  getJdbcTemplate().update(INSERT_IRIS,
	        			  docId,
	        			  studyOverview,
	        			  relatedStudies,
	        			  synchroCode
	            );

	        } catch (DAOException e) {
	        	 final String message = "Failed to Create File for Oracle Tile I";
	             LOG.log(Level.SEVERE, message, e);
	        }
	 }
}
