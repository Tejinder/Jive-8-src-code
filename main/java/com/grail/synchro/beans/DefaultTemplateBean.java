package com.grail.synchro.beans;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 5:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplateBean {
    private Long id;
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

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileDownloadLink() {
        return fileDownloadLink;
    }

    public void setFileDownloadLink(String fileDownloadLink) {
        this.fileDownloadLink = fileDownloadLink;
    }
}
