package com.grail.synchro.beans;

import java.util.Date;
import java.util.List;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProposalInitiation extends BeanObject {

    private long projectID;
    private long endMarketID;
    private long agencyID;
  
    //Generic Proposal Stage fields
 
    private String bizQuestion;
    private String bizQuestionText;
    
    private String researchObjective;
    private String researchObjectiveText;
    
    private String actionStandard;
    private String actionStandardText;
    
    private String researchDesign;
    private String researchDesignText;
    
    private String sampleProfile;
    private String sampleProfileText;
    
    private String stimulusMaterial;
    private String stimulusMaterialText;
    
    private String stimulusMaterialShipped;
    private String npiReferenceNo;
    private Date stimuliDate;

    //Reporting Requirement fields
    private Boolean topLinePresentation;
    private Boolean presentation;
    private Boolean fullreport;
    private Boolean globalSummary;
    
    private String otherReportingRequirements;
    private String otherReportingRequirementsText;
    
    private Boolean isPropSubmitted;
    private Boolean isAwarded;
    private Integer status;
    private String others;
    private String othersText;
    
    private String proposalCostTemplate;
    
    private Long brand;
    private Long methodologyType;
    private Long methodologyGroup;
    private List<Long> proposedMethodology;
    private Date startDate;
    private Date endDate;
    private Long projectOwner;
    private Long spiContact;
    private Boolean isSendToProjectOwner;
    private Boolean isReqClariModification;
    private Long capRating;
    private Boolean reqClarificationReqClicked;
    
    private Date proposalSaveDate;
    private Date proposalSubmitDate;
    private Date reqClarificationReqDate;
    private Date propSendToOwnerDate;
    private Date proposalAwardDate;
    
    
    private String proposal;
    private String proposalText;
    
    private Long proposalLegalApprover;
    private Integer sendForApproval;
    private Integer needsDiscussion;
    private Date legalApprovalDate;
    private Integer legalApprovalStatus;
    
    private Integer legalSignOffRequired;
    
    private Date sendReminderDate;
    private Date sendForApprovalDate;
    
    private String proposalLegalApproverOffline;
  
    public long getProjectID() {
        return projectID;
    }

    public void setProjectID(final long projectID) {
        this.projectID = projectID;
    }

   

    public String getBizQuestion() {
        return bizQuestion;
    }

    public void setBizQuestion(final String bizQuestion) {
        this.bizQuestion = bizQuestion;
    }
    public String getResearchObjective() {
        return researchObjective;
    }

    public void setResearchObjective(final String researchObjective) {
        this.researchObjective = researchObjective;
    }

    public String getActionStandard() {
        return actionStandard;
    }

    public void setActionStandard(final String actionStandard) {
        this.actionStandard = actionStandard;
    }

    public String getResearchDesign() {
        return researchDesign;
    }

    public void setResearchDesign(final String researchDesign) {
        this.researchDesign = researchDesign;
    }
  

	public long getEndMarketID() {
		return endMarketID;
	}

	public void setEndMarketID(long endMarketID) {
		this.endMarketID = endMarketID;
	}

	public String getSampleProfile() {
		return sampleProfile;
	}

	public void setSampleProfile(String sampleProfile) {
		this.sampleProfile = sampleProfile;
	}

	public String getStimulusMaterial() {
		return stimulusMaterial;
	}

	public void setStimulusMaterial(String stimulusMaterial) {
		this.stimulusMaterial = stimulusMaterial;
	}

	

	public String getOtherReportingRequirements() {
		return otherReportingRequirements;
	}

	public void setOtherReportingRequirements(String otherReportingRequirements) {
		this.otherReportingRequirements = otherReportingRequirements;
	}

	public String getNpiReferenceNo() {
		return npiReferenceNo;
	}

	public void setNpiReferenceNo(String npiReferenceNo) {
		this.npiReferenceNo = npiReferenceNo;
	}

    public Date getStimuliDate() {
        return stimuliDate;
    }

    public void setStimuliDate(Date stimuliDate) {
        this.stimuliDate = stimuliDate;
    }

    public long getAgencyID() {
		return agencyID;
	}

	public void setAgencyID(long agencyID) {
		this.agencyID = agencyID;
	}



	public Boolean getIsPropSubmitted() {
		return isPropSubmitted;
	}

	public void setIsPropSubmitted(Boolean isPropSubmitted) {
		this.isPropSubmitted = isPropSubmitted;
	}

	public Boolean getIsAwarded() {
		return isAwarded;
	}

	public void setIsAwarded(Boolean isAwarded) {
		this.isAwarded = isAwarded;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getProposalCostTemplate() {
		return proposalCostTemplate;
	}

	public void setProposalCostTemplate(String proposalCostTemplate) {
		this.proposalCostTemplate = proposalCostTemplate;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	public Long getBrand() {
		return brand;
	}

	public void setBrand(Long brand) {
		this.brand = brand;
	}

	public Long getMethodologyType() {
		return methodologyType;
	}

	public void setMethodologyType(Long methodologyType) {
		this.methodologyType = methodologyType;
	}

	public Long getMethodologyGroup() {
		return methodologyGroup;
	}

	public void setMethodologyGroup(Long methodologyGroup) {
		this.methodologyGroup = methodologyGroup;
	}

	public List<Long> getProposedMethodology() {
		return proposedMethodology;
	}

	public void setProposedMethodology(List<Long> proposedMethodology) {
		this.proposedMethodology = proposedMethodology;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getProjectOwner() {
		return projectOwner;
	}

	public void setProjectOwner(Long projectOwner) {
		this.projectOwner = projectOwner;
	}

	public Long getSpiContact() {
		return spiContact;
	}

	public void setSpiContact(Long spiContact) {
		this.spiContact = spiContact;
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

	public String getStimulusMaterialShipped() {
		return stimulusMaterialShipped;
	}

	public void setStimulusMaterialShipped(String stimulusMaterialShipped) {
		this.stimulusMaterialShipped = stimulusMaterialShipped;
	}

	public Boolean getIsSendToProjectOwner() {
		return isSendToProjectOwner;
	}

	public void setIsSendToProjectOwner(Boolean isSendToProjectOwner) {
		this.isSendToProjectOwner = isSendToProjectOwner;
	}

	public Boolean getIsReqClariModification() {
		return isReqClariModification;
	}

	public void setIsReqClariModification(Boolean isReqClariModification) {
		this.isReqClariModification = isReqClariModification;
	}

	public Boolean getGlobalSummary() {
		return globalSummary;
	}

	public void setGlobalSummary(Boolean globalSummary) {
		this.globalSummary = globalSummary;
	}

	public Long getCapRating() {
		return capRating;
	}

	public void setCapRating(Long capRating) {
		this.capRating = capRating;
	}

	public String getBizQuestionText() {
		return bizQuestionText;
	}

	public void setBizQuestionText(String bizQuestionText) {
		this.bizQuestionText = bizQuestionText;
	}

	public String getResearchObjectiveText() {
		return researchObjectiveText;
	}

	public void setResearchObjectiveText(String researchObjectiveText) {
		this.researchObjectiveText = researchObjectiveText;
	}

	public String getActionStandardText() {
		return actionStandardText;
	}

	public void setActionStandardText(String actionStandardText) {
		this.actionStandardText = actionStandardText;
	}

	public String getResearchDesignText() {
		return researchDesignText;
	}

	public void setResearchDesignText(String researchDesignText) {
		this.researchDesignText = researchDesignText;
	}

	public String getSampleProfileText() {
		return sampleProfileText;
	}

	public void setSampleProfileText(String sampleProfileText) {
		this.sampleProfileText = sampleProfileText;
	}

	public String getStimulusMaterialText() {
		return stimulusMaterialText;
	}

	public void setStimulusMaterialText(String stimulusMaterialText) {
		this.stimulusMaterialText = stimulusMaterialText;
	}

	public String getOtherReportingRequirementsText() {
		return otherReportingRequirementsText;
	}

	public void setOtherReportingRequirementsText(
			String otherReportingRequirementsText) {
		this.otherReportingRequirementsText = otherReportingRequirementsText;
	}

	public String getOthersText() {
		return othersText;
	}

	public void setOthersText(String othersText) {
		this.othersText = othersText;
	}

	public Boolean getReqClarificationReqClicked() {
		return reqClarificationReqClicked;
	}

	public void setReqClarificationReqClicked(Boolean reqClarificationReqClicked) {
		this.reqClarificationReqClicked = reqClarificationReqClicked;
	}

	public Date getProposalSaveDate() {
		return proposalSaveDate;
	}

	public void setProposalSaveDate(Date proposalSaveDate) {
		this.proposalSaveDate = proposalSaveDate;
	}

	public Date getProposalSubmitDate() {
		return proposalSubmitDate;
	}

	public void setProposalSubmitDate(Date proposalSubmitDate) {
		this.proposalSubmitDate = proposalSubmitDate;
	}

	public Date getReqClarificationReqDate() {
		return reqClarificationReqDate;
	}

	public void setReqClarificationReqDate(Date reqClarificationReqDate) {
		this.reqClarificationReqDate = reqClarificationReqDate;
	}

	public Date getPropSendToOwnerDate() {
		return propSendToOwnerDate;
	}

	public void setPropSendToOwnerDate(Date propSendToOwnerDate) {
		this.propSendToOwnerDate = propSendToOwnerDate;
	}

	public Date getProposalAwardDate() {
		return proposalAwardDate;
	}

	public void setProposalAwardDate(Date proposalAwardDate) {
		this.proposalAwardDate = proposalAwardDate;
	}

	public String getProposal() {
		return proposal;
	}

	public void setProposal(String proposal) {
		this.proposal = proposal;
	}

	public String getProposalText() {
		return proposalText;
	}

	public void setProposalText(String proposalText) {
		this.proposalText = proposalText;
	}

	public Long getProposalLegalApprover() {
		return proposalLegalApprover;
	}

	public void setProposalLegalApprover(Long proposalLegalApprover) {
		this.proposalLegalApprover = proposalLegalApprover;
	}

	public Integer getSendForApproval() {
		return sendForApproval;
	}

	public void setSendForApproval(Integer sendForApproval) {
		this.sendForApproval = sendForApproval;
	}

	public Integer getNeedsDiscussion() {
		return needsDiscussion;
	}

	public void setNeedsDiscussion(Integer needsDiscussion) {
		this.needsDiscussion = needsDiscussion;
	}

	public Date getLegalApprovalDate() {
		return legalApprovalDate;
	}

	public void setLegalApprovalDate(Date legalApprovalDate) {
		this.legalApprovalDate = legalApprovalDate;
	}

	public Integer getLegalApprovalStatus() {
		return legalApprovalStatus;
	}

	public void setLegalApprovalStatus(Integer legalApprovalStatus) {
		this.legalApprovalStatus = legalApprovalStatus;
	}

	public Integer getLegalSignOffRequired() {
		return legalSignOffRequired;
	}

	public void setLegalSignOffRequired(Integer legalSignOffRequired) {
		this.legalSignOffRequired = legalSignOffRequired;
	}

	public Date getSendReminderDate() {
		return sendReminderDate;
	}

	public void setSendReminderDate(Date sendReminderDate) {
		this.sendReminderDate = sendReminderDate;
	}

	public Date getSendForApprovalDate() {
		return sendForApprovalDate;
	}

	public void setSendForApprovalDate(Date sendForApprovalDate) {
		this.sendForApprovalDate = sendForApprovalDate;
	}

	public String getProposalLegalApproverOffline() {
		return proposalLegalApproverOffline;
	}

	public void setProposalLegalApproverOffline(String proposalLegalApproverOffline) {
		this.proposalLegalApproverOffline = proposalLegalApproverOffline;
	}
	
	
}
