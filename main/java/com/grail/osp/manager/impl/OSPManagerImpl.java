package com.grail.osp.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.osp.beans.OSPFile;
import com.grail.osp.beans.OSPFolder;
import com.grail.osp.beans.OSPTile;
import com.grail.osp.dao.OSPDAO;
import com.grail.osp.manager.OSPManager;
import com.grail.osp.object.OSPAttachment;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.attachments.AttachmentHelper;



/**
 * @author: Tejinder
 * @since: 1.0
 */
public class OSPManagerImpl implements OSPManager
{
	   private OSPDAO ospDAO;
	   private AttachmentHelper attachmentHelper;
	   private AttachmentManager attachmentManager;
	   
	   @Override
	   public List<OSPTile> getOracleTiles()
	   {
	       return this.ospDAO.getOracleTiles();
	   }
	   
	   @Override
	   public List<OSPTile> getShareTiles()
	   {
		   return this.ospDAO.getShareTiles();
	   }
		
	   @Override
	   public OSPTile getOracleTile(final Long tileId)
	   {
		   return this.ospDAO.getOracleTile(tileId);
	   }
		
	   @Override
	   public List<OSPFolder> getOracleFolders(Long tileId)
	   {
	       return this.ospDAO.getOracleFolders(tileId);
	   }
	   @Override
	   public List<OSPFolder> getOracleFolders(final Long tileId, final ProjectResultFilter projectResultFilter)
	   {
		   return this.ospDAO.getOracleFolders(tileId,projectResultFilter);
	   }
	   
	   @Override
	   public Long getOracleFoldersTotalCount(final Long tileId)
	   {
		   return this.ospDAO.getOracleFoldersTotalCount(tileId);
	   }
	   
	   @Override
	   public OSPFolder getOracleFolder(final Long tileId, final Long folderId)
	   {
		   return this.ospDAO.getOracleFolder(tileId,folderId);
	   }
	   
		@Override
	   public List<OSPFile> getOracleFiles(Long tileId, final Long folderId)
	   {
	       return this.ospDAO.getOracleFiles(tileId, folderId);
	   }
		
		@Override
		public List<OSPFile> getOracleFiles(final Long tileId, final Long folderId,final ProjectResultFilter projectResultFilter)
		{
			return this.ospDAO.getOracleFiles(tileId, folderId, projectResultFilter);
		}
		
		@Override
		public Long getOracleFilesTotalCount(final Long tileId , final Long folderId)
		{
			return this.ospDAO.getOracleFilesTotalCount(tileId, folderId);
		}
		
		@Override
		public void saveOracleFolder(final OSPFolder ospFolder)
		{
			this.ospDAO.saveOracleFolder(ospFolder);
		}
		
		@Override
		public void saveOracleFile(final OSPFile ospFile)
		{
			this.ospDAO.saveOracleFile(ospFile);
			
			
		}
		
		@Override
		public void deleteOracleFolder(final Long folderId)
		{
			this.ospDAO.deleteOracleFolder(folderId);
		}
		
		@Override
		public void deleteOracleFile(final Long fileId)
		{
			this.ospDAO.deleteOracleFile(fileId);
			try
			{
				Attachment attachment = attachmentManager.getAttachment(fileId);
		        attachmentManager.deleteAttachment(attachment);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		
		 @Override
		 @Transactional(propagation = Propagation.REQUIRED)
		 public Attachment addAttachment(final File attachment, final String fileName, final String contentType, final Long tileID, final Long folderID) throws IOException, AttachmentException 
		 {
	        OSPAttachment ospAttachment = new OSPAttachment();
	        Attachment att = null;
	        ospAttachment.getOspFile().setAttachmentId(new Long((tileID + "-" + folderID).hashCode()));
	       
	        try 
	        {
	          	att =  attachmentHelper.createAttachment(ospAttachment, fileName , contentType, attachment);
            } 
	        catch (IOException e) 
	        {
	           throw new IOException(e.getMessage(), e);
	        }
	        catch (AttachmentException e) 
	        {
	           throw new AttachmentException(e.getMessage(), e);
	        }
	        return att;
	    }
		 
		 @Override
		 public  void updateOracleFolderName(final OSPFolder ospFolder)
		 {
			 this.ospDAO.updateOracleFolderName(ospFolder);
		 }
		 
		 @Override
		 public void updateOracleFolderSize(final OSPFolder ospFolder)
		 {
			 this.ospDAO.updateOracleFolderSize(ospFolder);
		 }
		 
		 @Override
		 public Attachment getAttachments(final Long attachmentId)
		 {
			 Attachment att = null;
			 try
			 {
				 att = attachmentManager.getAttachment(attachmentId);
			 }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
			 return att;
			 
		 }
		 
		 @Override
		 public void saveIRISDoc(final long docId, final String studyOverview, final String relatedStudies, final long synchroCode)
		 {
			 ospDAO.saveIRISDoc(docId, studyOverview, relatedStudies, synchroCode);
		 }
		 
		 @Override
		 public boolean isValidFolderName(final OSPFolder ospFolder)
		 {
			 return ospDAO.isValidFolderName(ospFolder);
		 }
		 
		 @Override
		 public boolean isValidOracleFileName(final OSPFile ospFile)
		 {
			 return ospDAO.isValidOracleFileName(ospFile);
		 }
		 
		 
		@Override
		public	OSPTile getShareTile(final Long tileId)
		{
			return ospDAO.getShareTile(tileId);
		}
			
		@Override
		public List<OSPFolder> getShareFolders(final Long tileId)
		{
			return ospDAO.getShareFolders(tileId);
		}
		@Override
		public List<OSPFolder> getShareFolders(final Long tileId, final ProjectResultFilter projectResultFilter)
		{
			return ospDAO.getShareFolders(tileId,projectResultFilter);
		}
		@Override
		public Long getShareFoldersTotalCount(final Long tileId)
		{
			return ospDAO.getShareFoldersTotalCount(tileId);
		}
		@Override
		public OSPFolder getShareFolder(final Long tileId, final Long folderId)
		{
			return ospDAO.getShareFolder(tileId, folderId);
		}
		@Override
		public void saveShareFolder(final OSPFolder ospFolder)
		{
			ospDAO.saveShareFolder(ospFolder);
		}
		@Override
		public boolean isValidShareFolderName(final OSPFolder ospFolder)
		{
			return ospDAO.isValidShareFolderName(ospFolder);
		}
			
		@Override
		public void updateShareFolderName(final OSPFolder ospFolder)
		{
			ospDAO.updateShareFolderName(ospFolder);
		}
		@Override
		public void updateShareFolderSize(final OSPFolder ospFolder)
		{
			ospDAO.updateShareFolderSize(ospFolder);
		}
			
		@Override
		public void deleteShareFolder(final Long folderId)
		{
			ospDAO.deleteShareFolder(folderId);
		}
		@Override
		public void deleteShareFile(final Long fileId)
		{
			ospDAO.deleteShareFile(fileId);
		}
		@Override
		public void saveShareFile(final OSPFile ospFile)
		{
			ospDAO.saveShareFile(ospFile);
		}
			
		@Override
		public boolean isValidShareFileName(final OSPFile ospFile)
		{
			return ospDAO.isValidShareFileName(ospFile);
		}
			
		@Override
		public List<OSPFile> getShareFiles(final Long tileId, final Long folderId)
		{
			return ospDAO.getShareFiles(tileId,folderId);
		}
		    
		@Override
		public List<OSPFile> getShareFiles(final Long tileId, final Long folderId,final ProjectResultFilter projectResultFilter)
		{
			return ospDAO.getShareFiles(tileId,folderId, projectResultFilter);
		}
		    
		@Override
		public Long getShareFilesTotalCount(final Long tileId , final Long folderId)
		{
			return ospDAO.getShareFilesTotalCount(tileId,folderId);
		}
		    
		public OSPDAO getOspDAO() {
			return ospDAO;
		}
		public void setOspDAO(OSPDAO ospDAO) {
			this.ospDAO = ospDAO;
		}

		public AttachmentHelper getAttachmentHelper() {
			return attachmentHelper;
		}

		public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
			this.attachmentHelper = attachmentHelper;
		}

		public AttachmentManager getAttachmentManager() {
			return attachmentManager;
		}

		public void setAttachmentManager(AttachmentManager attachmentManager) {
			this.attachmentManager = attachmentManager;
		}
}