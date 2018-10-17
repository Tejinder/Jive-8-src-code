package com.grail.synchro.beans;

/**
 * @author: vivek
 * @since: 1.0
 */
public class PIBTargetSegment {

    private Long projectID;
    private Long docID;
    private Long endmarketID;
    private String name;
    private String segementDetail;

    public PIBTargetSegment() {
    }

    public PIBTargetSegment(final Long projectID, final Long endmarketID) {
        this.projectID = projectID;
        this.endmarketID = endmarketID;
    }

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

    public Long getDocID() {
        return docID;
    }

    public void setDocID(final Long docID) {
        this.docID = docID;
    }

    public Long getEndmarketID() {
        return endmarketID;
    }

    public void setEndmarketID(final Long endmarketID) {
        this.endmarketID = endmarketID;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSegementDetail() {
        return segementDetail;
    }

    public void setSegementDetail(final String segementDetail) {
        this.segementDetail = segementDetail;
    }
}
