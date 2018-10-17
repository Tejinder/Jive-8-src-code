package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;


/**
 * @author: vivek
 * @since: 1.0
 */
public class ProjectInitiation extends BeanObject {

    private long projectID;
    private long endMarketID;
  
    //Generic PIB fields
 
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
    private BigDecimal latestEstimate;
    private Integer latestEstimateType;
    private String npiReferenceNo;
    private Date stimuliDate;
    private String others;
    private String othersText;

    //Reporting Requirement fields
    private Boolean topLinePresentation;
    private Boolean presentation;
    private Boolean fullreport;
    private Boolean globalSummary;
    
    private String otherReportingRequirements;
    private String otherReportingRequirementsText;
    private Integer status;
    
    //Detailed Stakeholder List Fields
    private Long agencyContact1;
    private Long agencyContact2;
    private Long agencyContact3;
    private Long globalLegalContact;
    private Long globalProcurementContact;
    private Long globalCommunicationAgency;
    
    // SPI Contact
    private Long spiContact;
    private Integer deviationFromSM;
    private Boolean legalApprovalRcvd;
    private Boolean legalApprovalNotReq;
    private String legalApprover;
    private Boolean isEndMarketChanged;
    private Boolean notifyAboveMarketContacts;
    private Boolean approveChanges;

    private Boolean notifySPI;
    private Boolean notifyPO;
    
    private Long agencyContact1Optional;
    private Long agencyContact2Optional;
    private Long agencyContact3Optional;
    private Long productContact;
	private BigDecimal fieldworkCost;
    private Long fieldworkCostCurrency;
    private Long hasTenderingProcess;
    
    private Integer nonKantar;
    
    private Date pibSaveDate;
    private Date pibLegalApprovalDate;
    private Date pibCompletionDate;
    private Date pibNotifyAMContactsDate;
	
    private String brief;
    private String briefText;
    
    private Long briefLegalApprover;
    private Integer sendForApproval;
    private Integer needsDiscussion;
    private Date legalApprovalDate;
    private Integer legalApprovalStatus;
    
    private Integer legalSignOffRequired;
    
    private Date sendReminderDate;
    
    private Date sendForApprovalDate;
    
    private String briefLegalApproverOffline;
    
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

    

    public Boolean getFullreport() {
        return fullreport;
    }

    public void setFullreport(final Boolean fullreport) {
        this.fullreport = fullreport;
    }

  

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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

	public String getOtherReportingRequirements() {
		return otherReportingRequirements;
	}

	public void setOtherReportingRequirements(String otherReportingRequirements) {
		this.otherReportingRequirements = otherReportingRequirements;
	}

	public Long getAgencyContact1() {
		return agencyContact1;
	}

	public void setAgencyContact1(Long agencyContact1) {
		this.agencyContact1 = agencyContact1;
	}

	public Long getAgencyContact2() {
		return agencyContact2;
	}

	public void setAgencyContact2(Long agencyContact2) {
		this.agencyContact2 = agencyContact2;
	}

	public Long getAgencyContact3() {
		return agencyContact3;
	}

	public void setAgencyContact3(Long agencyContact3) {
		this.agencyContact3 = agencyContact3;
	}

	public Long getGlobalLegalContact() {
		return globalLegalContact;
	}

	public void setGlobalLegalContact(Long globalLegalContact) {
		this.globalLegalContact = globalLegalContact;
	}

	public Long getGlobalProcurementContact() {
		return globalProcurementContact;
	}

	public void setGlobalProcurementContact(Long globalProcurementContact) {
		this.globalProcurementContact = globalProcurementContact;
	}

	public Long getGlobalCommunicationAgency() {
		return globalCommunicationAgency;
	}

	public void setGlobalCommunicationAgency(Long globalCommunicationAgency) {
		this.globalCommunicationAgency = globalCommunicationAgency;
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

    public Integer getDeviationFromSM() {
		return deviationFromSM;
	}

	public void setDeviationFromSM(Integer deviationFromSM) {
		this.deviationFromSM = deviationFromSM;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	public Long getSpiContact() {
		return spiContact;
	}

	public void setSpiContact(Long spiContact) {
		this.spiContact = spiContact;
	}


	public Boolean getLegalApprovalRcvd() {
		return legalApprovalRcvd;
	}

	public void setLegalApprovalRcvd(Boolean legalApprovalRcvd) {
		this.legalApprovalRcvd = legalApprovalRcvd;
	}

	public Boolean getLegalApprovalNotReq() {
		return legalApprovalNotReq;
	}

	public void setLegalApprovalNotReq(Boolean legalApprovalNotReq) {
		this.legalApprovalNotReq = legalApprovalNotReq;
	}

	public String getLegalApprover() {
		return legalApprover;
	}

	public void setLegalApprover(String legalApprover) {
		this.legalApprover = legalApprover;
	}

	public Boolean getIsEndMarketChanged() {
		return isEndMarketChanged;
	}

	public void setIsEndMarketChanged(Boolean isEndMarketChanged) {
		this.isEndMarketChanged = isEndMarketChanged;
	}

	public Boolean getNotifyAboveMarketContacts() {
		return notifyAboveMarketContacts;
	}

	public void setNotifyAboveMarketContacts(Boolean notifyAboveMarketContacts) {
		this.notifyAboveMarketContacts = notifyAboveMarketContacts;
	}

	public Boolean getApproveChanges() {
		return approveChanges;
	}

	public void setApproveChanges(Boolean approveChanges) {
		this.approveChanges = approveChanges;
	}

	public Boolean getNotifySPI() {
		return notifySPI;
	}

	public void setNotifySPI(Boolean notifySPI) {
		this.notifySPI = notifySPI;
	}

	public Boolean getNotifyPO() {
		return notifyPO;
	}

	public void setNotifyPO(Boolean notifyPO) {
		this.notifyPO = notifyPO;
	}

	public Boolean getGlobalSummary() {
		return globalSummary;
	}

	public void setGlobalSummary(Boolean globalSummary) {
		this.globalSummary = globalSummary;
	}

	public Long getAgencyContact1Optional() {
		return agencyContact1Optional;
	}

	public void setAgencyContact1Optional(Long agencyContact1Optional) {
		this.agencyContact1Optional = agencyContact1Optional;
	}

	public Long getAgencyContact2Optional() {
		return agencyContact2Optional;
	}

	public void setAgencyContact2Optional(Long agencyContact2Optional) {
		this.agencyContact2Optional = agencyContact2Optional;
	}

	public Long getAgencyContact3Optional() {
		return agencyContact3Optional;
	}

	public void setAgencyContact3Optional(Long agencyContact3Optional) {
		this.agencyContact3Optional = agencyContact3Optional;
	}

	public BigDecimal getFieldworkCost() {
		return fieldworkCost;
	}

	public void setFieldworkCost(BigDecimal fieldworkCost) {
		this.fieldworkCost = fieldworkCost;
	}

	public Long getFieldworkCostCurrency() {
		return fieldworkCostCurrency;
	}

	public void setFieldworkCostCurrency(Long fieldworkCostCurrency) {
		this.fieldworkCostCurrency = fieldworkCostCurrency;
	}

	public Long getHasTenderingProcess() {
		return hasTenderingProcess;
	}

	public void setHasTenderingProcess(Long hasTenderingProcess) {
		this.hasTenderingProcess = hasTenderingProcess;
	}

	public Long getProductContact() {
		return productContact;
	}

	public void setProductContact(Long productContact) {
		this.productContact = productContact;
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

	public String getOthersText() {
		return othersText;
	}

	public void setOthersText(String othersText) {
		this.othersText = othersText;
	}

	public String getOtherReportingRequirementsText() {
		return otherReportingRequirementsText;
	}

	public void setOtherReportingRequirementsText(
			String otherReportingRequirementsText) {
		this.otherReportingRequirementsText = otherReportingRequirementsText;
	}

	public Integer getNonKantar() {
		return nonKantar;
	}

	public void setNonKantar(Integer nonKantar) {
		this.nonKantar = nonKantar;
	}

	public Date getPibSaveDate() {
		return pibSaveDate;
	}

	public void setPibSaveDate(Date pibSaveDate) {
		this.pibSaveDate = pibSaveDate;
	}

	public Date getPibLegalApprovalDate() {
		return pibLegalApprovalDate;
	}

	public void setPibLegalApprovalDate(Date pibLegalApprovalDate) {
		this.pibLegalApprovalDate = pibLegalApprovalDate;
	}

	public Date getPibCompletionDate() {
		return pibCompletionDate;
	}

	public void setPibCompletionDate(Date pibCompletionDate) {
		this.pibCompletionDate = pibCompletionDate;
	}

	public Date getPibNotifyAMContactsDate() {
		return pibNotifyAMContactsDate;
	}

	public void setPibNotifyAMContactsDate(Date pibNotifyAMContactsDate) {
		this.pibNotifyAMContactsDate = pibNotifyAMContactsDate;
	}

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public String getBriefText() {
		return briefText;
	}

	public void setBriefText(String briefText) {
		this.briefText = briefText;
	}

	public Long getBriefLegalApprover() {
		return briefLegalApprover;
	}

	public void setBriefLegalApprover(Long briefLegalApprover) {
		this.briefLegalApprover = briefLegalApprover;
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

	public String getBriefLegalApproverOffline() {
		return briefLegalApproverOffline;
	}

	public void setBriefLegalApproverOffline(String briefLegalApproverOffline) {
		this.briefLegalApproverOffline = briefLegalApproverOffline;
	}
	
	
}
