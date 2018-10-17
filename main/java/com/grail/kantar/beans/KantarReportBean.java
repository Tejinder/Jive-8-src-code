package com.grail.kantar.beans;

import com.jivesoftware.community.impl.dao.AttachmentBean;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class KantarReportBean {
    private Long id;
    private String reportName;
    private Long country;
    private Integer reportType;
    private String otherReportType;
    private String comments;
    List<AttachmentBean> attachments;
    private Long createdBy;
    private Date creationDate;
    private Long modifiedBy;
    private Date modificationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Long getCountry() {
        return country;
    }

    public void setCountry(Long country) {
        this.country = country;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public String getOtherReportType() {
        return otherReportType;
    }

    public void setOtherReportType(String otherReportType) {
        this.otherReportType = otherReportType;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<AttachmentBean> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentBean> attachments) {
        this.attachments = attachments;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }
}
