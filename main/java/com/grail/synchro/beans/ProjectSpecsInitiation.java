package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectSpecsInitiation extends BeanObject {

    private long projectID;
    private long endMarketID;
  
    //Generic Proposal Stage fields
 
    private String bizQuestion;
   
    private String researchObjective;
    private String actionStandard;
    private String researchDesign;
    private String sampleProfile;
    private String stimulusMaterial;
    private String stimulusMaterialShipped;
    private String npiReferenceNo;
    private Date stimuliDate;
    
    private String screener;
    private String consumerCCAgreement;
    private String questionnaire;
    private String discussionguide;
    private String others;
    
    //Reporting Requirement fields
    private Boolean topLinePresentation;
    private Boolean presentation;
    private Boolean fullreport;
    private Boolean globalSummary;
    
    private String otherReportingRequirements;
    
    private Long brand;
    private Long methodologyType;
    private Long methodologyGroup;
    private List<Long> proposedMethodology;
    private Date startDate;
    private Date endDate;
    private Long projectOwner;
    private Long spiContact;
    private Integer status;
    private String description;
    private String poNumber;
    private String poNumber1;
    
    private Integer isScreenerCCApproved;
    private Integer isQDGApproved;
    private Integer isApproved;
    private Integer isSendForApproval;
    
    private Date screenerCCApprovedDate;
    private Date qdgApprovedDate;
    private Date approvedDate;
    
    private long screenerCCApprover;
    private long approver;
    private long qdgApprover;
    private Integer deviationFromSM;
    
    private Boolean legalApprovalStimulus;
    private String legalApproverStimulus;
    private Boolean legalApprovalScreener;
    private String legalApproverScreener;
    private Boolean legalApprovalQuestionnaire;
    private String legalApproverQuestionnaire;
    private Boolean legalApprovalDG;
    private String legalApproverDG;
    private Boolean isReqClariModification;
    
    private Boolean legalApprovalCCCA;
    private String legalApproverCCCA;
    
    private BigDecimal aboveMarketFinalCost;
    private Integer aboveMarketFinalCostType;
    
    private BigDecimal latestEstimate;
    private Integer latestEstimateType;
    
    private BigDecimal estimatedCost;
    private Integer estimatedCostType;
    
    private List<Long> categoryType;
    
    private Date projectSpecsSaveDate;
    private Date psLegalApprovalDate;
    private Date reqClarificationModDate;
    
    private String documentation;
    private String documentationText;
    
  
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

    public String getScreener() {
		return screener;
	}

	public void setScreener(String screener) {
		this.screener = screener;
	}

	public String getConsumerCCAgreement() {
		return consumerCCAgreement;
	}

	public void setConsumerCCAgreement(String consumerCCAgreement) {
		this.consumerCCAgreement = consumerCCAgreement;
	}

	public String getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(String questionnaire) {
		this.questionnaire = questionnaire;
	}

	public String getDiscussionguide() {
		return discussionguide;
	}

	public void setDiscussionguide(String discussionguide) {
		this.discussionguide = discussionguide;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

    public String getPoNumber1() {
        return poNumber1;
    }

    public void setPoNumber1(String poNumber1) {
        this.poNumber1 = poNumber1;
    }

    public Date getScreenerCCApprovedDate() {
		return screenerCCApprovedDate;
	}

	public void setScreenerCCApprovedDate(Date screenerCCApprovedDate) {
		this.screenerCCApprovedDate = screenerCCApprovedDate;
	}

	public Date getQdgApprovedDate() {
		return qdgApprovedDate;
	}

	public void setQdgApprovedDate(Date qdgApprovedDate) {
		this.qdgApprovedDate = qdgApprovedDate;
	}

	public Date getApprovedDate() {
		return approvedDate;
	}

	public void setApprovedDate(Date approvedDate) {
		this.approvedDate = approvedDate;
	}

	public long getScreenerCCApprover() {
		return screenerCCApprover;
	}

	public void setScreenerCCApprover(long screenerCCApprover) {
		this.screenerCCApprover = screenerCCApprover;
	}

	public long getApprover() {
		return approver;
	}

	public void setApprover(long approver) {
		this.approver = approver;
	}

	public long getQdgApprover() {
		return qdgApprover;
	}

	public void setQdgApprover(long qdgApprover) {
		this.qdgApprover = qdgApprover;
	}

	public Integer getIsScreenerCCApproved() {
		return isScreenerCCApproved;
	}

	public void setIsScreenerCCApproved(Integer isScreenerCCApproved) {
		this.isScreenerCCApproved = isScreenerCCApproved;
	}

	public Integer getIsQDGApproved() {
		return isQDGApproved;
	}

	public void setIsQDGApproved(Integer isQDGApproved) {
		this.isQDGApproved = isQDGApproved;
	}

	public Integer getIsApproved() {
		return isApproved;
	}

	public void setIsApproved(Integer isApproved) {
		this.isApproved = isApproved;
	}

	public Integer getDeviationFromSM() {
		return deviationFromSM;
	}

	public void setDeviationFromSM(Integer deviationFromSM) {
		this.deviationFromSM = deviationFromSM;
	}

	public String getStimulusMaterialShipped() {
		return stimulusMaterialShipped;
	}

	public void setStimulusMaterialShipped(String stimulusMaterialShipped) {
		this.stimulusMaterialShipped = stimulusMaterialShipped;
	}

	public Integer getIsSendForApproval() {
		return isSendForApproval;
	}

	public void setIsSendForApproval(Integer isSendForApproval) {
		this.isSendForApproval = isSendForApproval;
	}

	public Boolean getLegalApprovalStimulus() {
		return legalApprovalStimulus;
	}

	public void setLegalApprovalStimulus(Boolean legalApprovalStimulus) {
		this.legalApprovalStimulus = legalApprovalStimulus;
	}

	public String getLegalApproverStimulus() {
		return legalApproverStimulus;
	}

	public void setLegalApproverStimulus(String legalApproverStimulus) {
		this.legalApproverStimulus = legalApproverStimulus;
	}

	public Boolean getLegalApprovalScreener() {
		return legalApprovalScreener;
	}

	public void setLegalApprovalScreener(Boolean legalApprovalScreener) {
		this.legalApprovalScreener = legalApprovalScreener;
	}

	public String getLegalApproverScreener() {
		return legalApproverScreener;
	}

	public void setLegalApproverScreener(String legalApproverScreener) {
		this.legalApproverScreener = legalApproverScreener;
	}

	public Boolean getLegalApprovalQuestionnaire() {
		return legalApprovalQuestionnaire;
	}

	public void setLegalApprovalQuestionnaire(Boolean legalApprovalQuestionnaire) {
		this.legalApprovalQuestionnaire = legalApprovalQuestionnaire;
	}

	public String getLegalApproverQuestionnaire() {
		return legalApproverQuestionnaire;
	}

	public void setLegalApproverQuestionnaire(String legalApproverQuestionnaire) {
		this.legalApproverQuestionnaire = legalApproverQuestionnaire;
	}

	public Boolean getLegalApprovalDG() {
		return legalApprovalDG;
	}

	public void setLegalApprovalDG(Boolean legalApprovalDG) {
		this.legalApprovalDG = legalApprovalDG;
	}

	public String getLegalApproverDG() {
		return legalApproverDG;
	}

	public void setLegalApproverDG(String legalApproverDG) {
		this.legalApproverDG = legalApproverDG;
	}

	public Boolean getIsReqClariModification() {
		return isReqClariModification;
	}

	public void setIsReqClariModification(Boolean isReqClariModification) {
		this.isReqClariModification = isReqClariModification;
	}

	public Boolean getLegalApprovalCCCA() {
		return legalApprovalCCCA;
	}

	public void setLegalApprovalCCCA(Boolean legalApprovalCCCA) {
		this.legalApprovalCCCA = legalApprovalCCCA;
	}

	public String getLegalApproverCCCA() {
		return legalApproverCCCA;
	}

	public void setLegalApproverCCCA(String legalApproverCCCA) {
		this.legalApproverCCCA = legalApproverCCCA;
	}

	public Boolean getGlobalSummary() {
		return globalSummary;
	}

	public void setGlobalSummary(Boolean globalSummary) {
		this.globalSummary = globalSummary;
	}

	public BigDecimal getAboveMarketFinalCost() {
		return aboveMarketFinalCost;
	}

	public void setAboveMarketFinalCost(BigDecimal aboveMarketFinalCost) {
		this.aboveMarketFinalCost = aboveMarketFinalCost;
	}

	public Integer getAboveMarketFinalCostType() {
		return aboveMarketFinalCostType;
	}

	public void setAboveMarketFinalCostType(Integer aboveMarketFinalCostType) {
		this.aboveMarketFinalCostType = aboveMarketFinalCostType;
	}

	public BigDecimal getLatestEstimate() {
		return latestEstimate;
	}

	public void setLatestEstimate(BigDecimal latestEstimate) {
		this.latestEstimate = latestEstimate;
	}

	public Integer getLatestEstimateType() {
		return latestEstimateType;
	}

	public void setLatestEstimateType(Integer latestEstimateType) {
		this.latestEstimateType = latestEstimateType;
	}

	public BigDecimal getEstimatedCost() {
		return estimatedCost;
	}

	public void setEstimatedCost(BigDecimal estimatedCost) {
		this.estimatedCost = estimatedCost;
	}

	public Integer getEstimatedCostType() {
		return estimatedCostType;
	}

	public void setEstimatedCostType(Integer estimatedCostType) {
		this.estimatedCostType = estimatedCostType;
	}

	public List<Long> getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(List<Long> categoryType) {
		this.categoryType = categoryType;
	}

	public Date getProjectSpecsSaveDate() {
		return projectSpecsSaveDate;
	}

	public void setProjectSpecsSaveDate(Date projectSpecsSaveDate) {
		this.projectSpecsSaveDate = projectSpecsSaveDate;
	}

	public Date getPsLegalApprovalDate() {
		return psLegalApprovalDate;
	}

	public void setPsLegalApprovalDate(Date psLegalApprovalDate) {
		this.psLegalApprovalDate = psLegalApprovalDate;
	}

	public Date getReqClarificationModDate() {
		return reqClarificationModDate;
	}

	public void setReqClarificationModDate(Date reqClarificationModDate) {
		this.reqClarificationModDate = reqClarificationModDate;
	}

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public String getDocumentationText() {
		return documentationText;
	}

	public void setDocumentationText(String documentationText) {
		this.documentationText = documentationText;
	}

	
	
	
}
