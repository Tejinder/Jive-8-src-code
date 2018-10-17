package com.grail.synchro.beans;

/**
 * @author: vivek
 * @since: 1.0
 */
public class PIBReporting {

    private Long projectID;
    private Long endMarketID;

    private Boolean topLinePresentation;
    private Boolean presentation;
    private Boolean fullreport;
    private Boolean globalSummary;
    
    private String otherReportingRequirements;
    private String otherReportingRequirementsText;

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

	public Long getEndMarketID() {
		return endMarketID;
	}

	public void setEndMarketID(Long endMarketID) {
		this.endMarketID = endMarketID;
	}

	public Boolean getTopLinePresentation() {
		return topLinePresentation;
	}

	public void setTopLinePresentation(Boolean topLinePresentation) {
		this.topLinePresentation = topLinePresentation;
	}

	public Boolean getPresentation() {
		return presentation;
	}

	public void setPresentation(Boolean presentation) {
		this.presentation = presentation;
	}

	public Boolean getFullreport() {
		return fullreport;
	}

	public void setFullreport(Boolean fullreport) {
		this.fullreport = fullreport;
	}

	public String getOtherReportingRequirements() {
		return otherReportingRequirements;
	}

	public void setOtherReportingRequirements(String otherReportingRequirements) {
		this.otherReportingRequirements = otherReportingRequirements;
	}

	public Boolean getGlobalSummary() {
		return globalSummary;
	}

	public void setGlobalSummary(Boolean globalSummary) {
		this.globalSummary = globalSummary;
	}

	public String getOtherReportingRequirementsText() {
		return otherReportingRequirementsText;
	}

	public void setOtherReportingRequirementsText(
			String otherReportingRequirementsText) {
		this.otherReportingRequirementsText = otherReportingRequirementsText;
	}

  
}
