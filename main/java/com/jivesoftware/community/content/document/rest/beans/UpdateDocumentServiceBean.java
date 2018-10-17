/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.document.rest.beans;

import java.util.List;

public class UpdateDocumentServiceBean extends CreateDocumentServiceBean {

    private long documentID;
    private int editedVersion;
    
    private List<String> country;
    private List<String> brand;
    private List<String> methodology;
    private String periodMonth;
    private String periodYear;
    
    public List<String> getBrand() {
		return brand;
	}

	public void setBrand(List<String> brand) {
		this.brand = brand;
	}

	public List<String> getMethodology() {
		return methodology;
	}

	public void setMethodology(List<String> methodology) {
		this.methodology = methodology;
	}

	public String getPeriodMonth() {
		return periodMonth;
	}

	public void setPeriodMonth(String periodMonth) {
		this.periodMonth = periodMonth;
	}

	public String getPeriodYear() {
		return periodYear;
	}

	public void setPeriodYear(String periodYear) {
		this.periodYear = periodYear;
	}

	


    public List<String> getCountry() {
		return country;
	}

	public void setCountry(List<String> country) {
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
}
