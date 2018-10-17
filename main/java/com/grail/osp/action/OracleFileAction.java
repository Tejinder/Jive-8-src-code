package com.grail.osp.action;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.grail.osp.beans.OSPFile;
import com.grail.osp.beans.OSPFolder;
import com.grail.osp.beans.OSPTile;
import com.grail.osp.manager.OSPManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.action.JiveActionSupport;
import com.opensymphony.xwork2.Preparable;

/**
 * User: Tejinder
 * Date: 11/20/17
 */
public class OracleFileAction extends JiveActionSupport implements Preparable {

	private List<OSPFile> oracleFileList;
	private OSPManager ospManager;
	private Long tileID;
	private Long folderID;
	
	private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long attachmentFieldID;
    private String attachmentName;
	
    private OSPTile ospTile;
    private OSPFolder ospFolder;
    
    private AttachmentManager attachmentManager;
    private String downloadAttachmentIds;
    
    private String deleteFileIDs;
    
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadFile";
    private String downloadFilename;
    private String downloadContentType;
    
    public void prepare() throws Exception
    {
    	getRequest().getMethod();
    	getRequest().getContentType();
    }
    
	public String input()
	{
       
		//oracleTileList = ospManager.getOracleTiles();
		oracleFileList = ospManager.getOracleFiles(tileID, folderID);	
		ospTile = ospManager.getOracleTile(tileID);
		ospFolder = ospManager.getOracleFolder(tileID, folderID);
		return INPUT;
    }
	
	@Override
    public String execute() {
        if(!SynchroPermHelper.canAccessOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
      /*  OSPFile ospFile = new OSPFile();
        
        ospFile.setAttachmentId(new Long("4"));
        ospFile.setTileId(new Long("1"));
        ospFile.setFolderId(new Long("1"));
        ospFile.setFileName("File 1");
        ospFile.setFileSize(new Long("123"));
        
        ospFile.setCreationBy(getUser().getID());
        ospFile.setCreationDate(System.currentTimeMillis());

        ospFile.setModifiedBy(getUser().getID());
        ospFile.setModifiedDate(System.currentTimeMillis());
        
        ospManager.saveOracleFile(ospFile);*/
        return SUCCESS;
    }

	 public String addAttachment() throws UnsupportedEncodingException {

	        LOG.info("Checking File Name"+attachFileFileName);
	        LOG.info("Checking File Content Type"+attachFileContentType);
	        Map<String, Object> result = new HashMap<String, Object>();
	        try
	        {        	
	            /*pibManagerNew.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectID,endMarketId,fieldCategoryId,getUser().getID());
	            attachmentMap.put(fieldCategoryId.intValue(), pibManagerNew.getFieldAttachments(projectID, endMarketId, fieldCategoryId));*/
	        	
	        	// This has been done so that in case the end Market is changed for a project, then the attachments should remain intact.
	        	Attachment attachment =  ospManager.addAttachment(attachFile, attachFileFileName, attachFileContentType, tileID, folderID);
	        	 OSPFile ospFile = new OSPFile();
	             
	             ospFile.setAttachmentId(attachment.getID());
	             ospFile.setTileId(tileID);
	             ospFile.setFolderId(folderID);
	             ospFile.setFileName(attachment.getName());
	             ospFile.setFileSize(new Long(attachment.getSize()));
	             
	             ospFile.setCreationBy(getUser().getID());
	             ospFile.setCreationDate(System.currentTimeMillis());

	             ospFile.setModifiedBy(getUser().getID());
	             ospFile.setModifiedDate(System.currentTimeMillis());
	             
	             ospManager.saveOracleFile(ospFile);
	             
	             List<OSPFile> ospFileList = ospManager.getOracleFiles(ospFile.getTileId(), ospFile.getFolderId());
	 			 Long folderFileSize = new Long("0");
	 			 for(OSPFile oFile:ospFileList )
	 			 {
	 				folderFileSize = folderFileSize + oFile.getFileSize();
	 			 } 
	 			
	 			 // This code will update the folder size by adding the individual file sizes of a folder.
	 			 OSPFolder ospFolder = new OSPFolder();
	 		     ospFolder.setTileId(tileID);
	 		     ospFolder.setFolderId(folderID);
	 		     ospFolder.setFolderSize(folderFileSize);
	 		     ospFolder.setModifiedBy(getUser().getID());
	 		     ospFolder.setModifiedDate(System.currentTimeMillis());
	            
	 		     ospManager.updateOracleFolderSize(ospFolder);
	            
	        }
	        catch (AttachmentException ae) {
	            result.put("success", false);
	            result.put("message", "Unable to upload file.");
	        } catch (UnauthorizedException ue) {
	            result.put("success", false);
	            result.put("message", "Unauthorized.");
	        } catch (Exception e) {
	            result.put("success", false);
	            result.put("message", e.getMessage());
	        }
	        return SUCCESS;
	    }
	 
	public String download()
	{
        if(!SynchroPermHelper.canAccessOSPOraclePortal(getUser())) 
        {
            return UNAUTHORIZED;
        }
        try
        {
        	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	 downloadFilename = "OSP-Files.zip";
             ZipOutputStream zos = new ZipOutputStream(baos);
        	
        	if(downloadAttachmentIds.contains(","))
        	{
    	        String[] downloadFileId = downloadAttachmentIds.split(",");
    	    	for(String dFileId:downloadFileId)
    	    	{	
    	    		Attachment att1 = ospManager.getAttachments(new Long(dFileId));
           		 	ZipEntry zipEntry = new ZipEntry(att1.getName());
                    zos.putNextEntry(zipEntry);
                    zos.write(IOUtils.toByteArray(att1.getData()));
                    zos.closeEntry();
    	    	}
        	}
        	else
        	{
        		Attachment att1 = ospManager.getAttachments(new Long(downloadAttachmentIds));
        		 ZipEntry zipEntry = new ZipEntry(att1.getName());
                 zos.putNextEntry(zipEntry);
                 zos.write(IOUtils.toByteArray(att1.getData()));
                 zos.closeEntry();
                
        	}
        	
        /*	Attachment att1 = ospManager.getAttachments(new Long("10124"));
        	Attachment att2 = ospManager.getAttachments(new Long("10127"));
        	
             
             ZipEntry zipEntry = new ZipEntry(att1.getName());
             zos.putNextEntry(zipEntry);
             zos.write(IOUtils.toByteArray(att1.getData()));
             zos.closeEntry();
            
             zipEntry = new ZipEntry(att2.getName());
             zos.putNextEntry(zipEntry);
             zos.write(IOUtils.toByteArray(att2.getData()));
            
            
             zos.closeEntry();
          */   
             zos.close();
             downloadStream = new ByteArrayInputStream(baos.toByteArray());
             

            
             
             return DOWNLOAD_REPORT;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        return SUCCESS;
	}
	
	
	public String delete() {
	    
    	if(deleteFileIDs.contains(","))
    	{
	        String[] delFileId = deleteFileIDs.split(",");
	    	for(String dFileId:delFileId)
	    	{	
	    		ospManager.deleteOracleFile(new Long(dFileId));
	    	}
    	}
    	else
    	{
    		ospManager.deleteOracleFile(new Long(deleteFileIDs));
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

	public List<OSPFile> getOracleFileList() {
		return oracleFileList;
	}

	public void setOracleFileList(List<OSPFile> oracleFileList) {
		this.oracleFileList = oracleFileList;
	}

	public Long getFolderID() {
		return folderID;
	}

	public void setFolderID(Long folderID) {
		this.folderID = folderID;
	}

	public File getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(File attachFile) {
		this.attachFile = attachFile;
	}

	public String getAttachFileContentType() {
		return attachFileContentType;
	}

	public void setAttachFileContentType(String attachFileContentType) {
		this.attachFileContentType = attachFileContentType;
	}

	public String getAttachFileFileName() {
		return attachFileFileName;
	}

	public void setAttachFileFileName(String attachFileFileName) {
		this.attachFileFileName = attachFileFileName;
	}

	public Long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public Long getAttachmentFieldID() {
		return attachmentFieldID;
	}

	public void setAttachmentFieldID(Long attachmentFieldID) {
		this.attachmentFieldID = attachmentFieldID;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public OSPTile getOspTile() {
		return ospTile;
	}

	public void setOspTile(OSPTile ospTile) {
		this.ospTile = ospTile;
	}

	public OSPFolder getOspFolder() {
		return ospFolder;
	}

	public void setOspFolder(OSPFolder ospFolder) {
		this.ospFolder = ospFolder;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public String getDownloadAttachmentIds() {
		return downloadAttachmentIds;
	}

	public void setDownloadAttachmentIds(String downloadAttachmentIds) {
		this.downloadAttachmentIds = downloadAttachmentIds;
	}

	public InputStream getDownloadStream() {
		return downloadStream;
	}

	public void setDownloadStream(InputStream downloadStream) {
		this.downloadStream = downloadStream;
	}

	public String getDownloadFilename() {
		return downloadFilename;
	}

	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

	public String getDOWNLOAD_REPORT() {
		return DOWNLOAD_REPORT;
	}

	public String getDownloadContentType() {
		return downloadContentType;
	}

	public void setDownloadContentType(String downloadContentType) {
		this.downloadContentType = downloadContentType;
	}

	public String getDeleteFileIDs() {
		return deleteFileIDs;
	}

	public void setDeleteFileIDs(String deleteFileIDs) {
		this.deleteFileIDs = deleteFileIDs;
	}

	
}
