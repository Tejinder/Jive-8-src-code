/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.document.rest.beans;

import com.jivesoftware.community.content.rest.beans.BaseContentServiceBean;
import java.util.List;
import java.util.Collection;

public class CreateDocumentServiceBean extends BaseContentServiceBean {

    private boolean isBinary;
    private boolean isDraftDisabled;
    

    private List<String> country;
    private List<String> brand;
    private List<String> methodology;
    private String periodMonth;
    private String periodYear;
    
    
    
    
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

	

	public List<String> getCountry() {
		return country;
	}

	public void setCountry(List<String> country) {
		this.country = country;
	}

	public boolean isBinary() {
        return isBinary;
    }

    public void setBinary(boolean binary) {
        isBinary = binary;
    }

    public boolean isDraftDisabled() {
        return isDraftDisabled;
    }

    public void setDraftDisabled(boolean draftDisabled) {
        isDraftDisabled = draftDisabled;
    }

	
}
