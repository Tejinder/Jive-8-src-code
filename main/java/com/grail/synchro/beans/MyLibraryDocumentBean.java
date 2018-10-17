package com.grail.synchro.beans;

import com.jivesoftware.community.Attachment;
import com.jivesoftware.util.LongList;

import java.util.Date;

/**
 *
 */
public class MyLibraryDocumentBean extends BeanObject {

    private Long id;
    private String title;
    private String description;
    private Long userId;
    private Date addedDate;
    private Long attachmentId;
    private String fileName;
    private String fileSize; // in KB
    private String contentType;
    private String fileDownloadLink;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileDownloadLink() {
        return fileDownloadLink;
    }

    public void setFileDownloadLink(String fileDownloadLink) {
        this.fileDownloadLink = fileDownloadLink;
    }
}
