/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.document.action.beans;

import com.jivesoftware.community.content.action.beans.BaseContentActionBean;

public class EditDocumentActionBean extends BaseContentActionBean {

    private long documentID;
    private int editedVersion;
    private CommentBean commentBean;
    private boolean draftDisabled = false;
    private boolean extendedAuthors;
    private String country;
    private String brand;
    private String methodology;
    private String month;
    private String year;
    
    
    public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getMethodology() {
		return methodology;
	}

	public void setMethodology(String methodology) {
		this.methodology = methodology;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	

    public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public long getDocumentID() {
        return documentID;
    }

    public void setDocumentID(long documentID) {
        this.documentID = documentID;
    }

    public int getEditedVersion() {
        return editedVersion;
    }

    public void setEditedVersion(int editedVersion) {
        this.editedVersion = editedVersion;
    }

    public CommentBean getCommentBean() {
        return commentBean;
    }

    public void setCommentBean(CommentBean commentBean) {
        this.commentBean = commentBean;
    }

    public boolean isDraftDisabled() {
        return draftDisabled;
    }

    public void setDraftDisabled(boolean draftDisabled) {
        this.draftDisabled = draftDisabled;
    }

    public boolean isExtendedAuthors() {
        return extendedAuthors;
    }

    public void setExtendedAuthors(boolean extendedAuthors) {
        this.extendedAuthors = extendedAuthors;
    }
}
