package com.grail.osp.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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

public class OracleFileDashboardAction extends JiveActionSupport {

	
	private OSPManager ospManager;
	private Long tileID;
	private Long folderID;
	private OSPFolder ospFolder;
	private OSPTile ospTile;
	
	private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long attachmentFieldID;
    private String attachmentName;
    
	private AttachmentManager attachmentManager;
	private String downloadAttachmentIds;
	private String deleteFileIDs;
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadFile";
    private String downloadFilename;
    private String downloadContentType;
    
    private Long hiddenTileID;    
    private Long hiddenFolderID;
    
    private String redirectURL;
    
    private Integer currPage;

    private String sortBy;
    
    private Integer sortOrder;
    
    private Integer hiddenCurrPage;    
    private String hiddenSortBy;
    
    @Override
    public String execute() {
    	if(!SynchroPermHelper.canAccessOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
       // System.out.println("TILE ID ==>"+ tileID);
       	
		ospTile = ospManager.getOracleTile(tileID);
		ospFolder = ospManager.getOracleFolder(tileID, folderID);
    	return SUCCESS;
    }
    
    public String addAttachment() throws UnsupportedEncodingException {

        LOG.info("Checking File Name"+attachFileFileName);
        LOG.info("Checking File Content Type"+attachFileContentType);
        Map<String, Object> result = new HashMap<String, Object>();
        try
        {        	
            
        	OSPFile ospFile = new OSPFile();
            
            
            ospFile.setTileId(tileID);
            ospFile.setFolderId(folderID);
            ospFile.setFileName(attachFileFileName);
           
            
            ospFile.setCreationBy(getUser().getID());
            ospFile.setCreationDate(System.currentTimeMillis());

            ospFile.setModifiedBy(getUser().getID());
            ospFile.setModifiedDate(System.currentTimeMillis()); 
            
            
        	
            if(ospManager.isValidOracleFileName(ospFile))
            {
            	if(attachFile!=null)
            	{
	            	Attachment attachment =  ospManager.addAttachment(attachFile, attachFileFileName, attachFileContentType, tileID, folderID);
		        	 
		        	 ospFile.setAttachmentId(attachment.getID());
		        	 ospFile.setFileSize(new Long(attachment.getSize()));
		        	 
		        	 if(attachment.getSize()>0)
		        	 {
		        	 
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
			 		     redirectURL="oracle-file-dashboard.jspa?tileID="+tileID+"&folderID="+folderID;
		        	 }
		        	 else
		        	 {
		        		 //redirectURL="oracle-file-dashboard.jspa?tileID="+tileID+"&folderID="+folderID+"&validFileSize=false&fileName="+attachFileFileName;
		        		 redirectURL="oracle-file-dashboard.jspa?tileID="+tileID+"&folderID="+folderID+"&validFileSize=false";
		        	 }
            	}
            	else
            	{
            		redirectURL="oracle-file-dashboard.jspa?tileID="+tileID+"&folderID="+folderID+"&validFileSize=false";
            	}
            }
            else
            {
            	//redirectURL="oracle-file-dashboard.jspa?tileID="+tileID+"&folderID="+folderID+"&validFile=false&fileName="+attachFileFileName;
            	redirectURL="oracle-file-dashboard.jspa?tileID="+tileID+"&folderID="+folderID+"&validFile=false";
            }
            
            // This is to preserve the Pagination and sort order in case new file is uploaded and page is refreshed
            if(StringUtils.isNotBlank(currPage.toString()))
            {
            	redirectURL=redirectURL+"&currPage="+currPage.toString();
            }
            if(StringUtils.isNotBlank(sortBy))
            {
            	redirectURL=redirectURL+"&sortBy="+sortBy;
            }
            
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
        return "fileAdded";
    }
    
    public String download()
	{
        if(!SynchroPermHelper.canAccessOSPOraclePortal(getUser())) 
        {
            return UNAUTHORIZED;
        }
        try
        {
        	tileID = hiddenTileID;
        	folderID = hiddenFolderID;
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	
        	ospTile = ospManager.getOracleTile(tileID);
        	
        	downloadFilename = "OSP-"+ospTile.getTileName()+"-Files.zip";
       	 
        	
             ZipOutputStream zos = new ZipOutputStream(baos);
             
           
            
             if(StringUtils.isNotBlank(downloadAttachmentIds))
             {
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
	    	    	 zos.close();
	                 downloadStream = new ByteArrayInputStream(baos.toByteArray());
	        	}
	        	else
	        	{
	        		 Attachment att1 = ospManager.getAttachments(new Long(downloadAttachmentIds));
	        		 downloadFilename = att1.getName();
	        		 
	        		 baos.write(IOUtils.toByteArray(att1.getData()));
	        		 downloadStream = new ByteArrayInputStream(baos.toByteArray());
	        	     
	        		 /*
	        		 ZipEntry zipEntry = new ZipEntry(att1.getName());
	                 zos.putNextEntry(zipEntry);
	                 zos.write(IOUtils.toByteArray(att1.getData()));
	                 zos.closeEntry();*/
	        		 
	        		 
	                
	        	}
             }
        	
        
            
             

            
             
             return DOWNLOAD_REPORT;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        return SUCCESS;
	}
	
	
	public String delete() {
	    
		tileID = hiddenTileID;
    	folderID = hiddenFolderID;
    	
    	// This is to preserve the Pagination and sort order in case new file is uploaded and page is refreshed
        if(hiddenCurrPage!=null)
        {
        	currPage = hiddenCurrPage;
        }
        if(StringUtils.isNotBlank(hiddenSortBy))
        {
        	sortBy=hiddenSortBy;
        }
		
    	if(StringUtils.isNotBlank(deleteFileIDs))
    	{
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
	    	
	    	
	    	List<OSPFile> ospFileList = ospManager.getOracleFiles(tileID, folderID);
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
    	
    	
        
        return "fileDeleted";
    }
   
	public Long getTileID() {
		return tileID;
	}
	public void setTileID(Long tileID) {
		this.tileID = tileID;
	}
	
	public OSPManager getOspManager() {
		return ospManager;
	}
	public void setOspManager(OSPManager ospManager) {
		this.ospManager = ospManager;
	}






	public Long getFolderID() {
		return folderID;
	}




	public void setFolderID(Long folderID) {
		this.folderID = folderID;
	}




	public OSPFolder getOspFolder() {
		return ospFolder;
	}




	public void setOspFolder(OSPFolder ospFolder) {
		this.ospFolder = ospFolder;
	}




	public OSPTile getOspTile() {
		return ospTile;
	}




	public void setOspTile(OSPTile ospTile) {
		this.ospTile = ospTile;
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


	public String getDeleteFileIDs() {
		return deleteFileIDs;
	}


	public void setDeleteFileIDs(String deleteFileIDs) {
		this.deleteFileIDs = deleteFileIDs;
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


	public String getDownloadContentType() {
		return downloadContentType;
	}


	public void setDownloadContentType(String downloadContentType) {
		this.downloadContentType = downloadContentType;
	}


	public Long getHiddenTileID() {
		return hiddenTileID;
	}


	public void setHiddenTileID(Long hiddenTileID) {
		this.hiddenTileID = hiddenTileID;
	}


	public Long getHiddenFolderID() {
		return hiddenFolderID;
	}


	public void setHiddenFolderID(Long hiddenFolderID) {
		this.hiddenFolderID = hiddenFolderID;
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

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public Integer getCurrPage() {
		return currPage;
	}

	public void setCurrPage(Integer currPage) {
		this.currPage = currPage;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public Integer getHiddenCurrPage() {
		return hiddenCurrPage;
	}

	public void setHiddenCurrPage(Integer hiddenCurrPage) {
		this.hiddenCurrPage = hiddenCurrPage;
	}

	public String getHiddenSortBy() {
		return hiddenSortBy;
	}

	public void setHiddenSortBy(String hiddenSortBy) {
		this.hiddenSortBy = hiddenSortBy;
	}



	

   
}
