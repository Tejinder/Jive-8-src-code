package com.grail.synchro.beans;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Kanwar Grewal
 * @version 4.0, Date: 11/27/13
 */
public class Project extends BeanObject {
    private Long projectID;
    private String name;
    private String description;
    private String descriptionText;
    private List<Long> categoryType;
    private Long brand;
    private Long methodologyType;
    private Long methodologyGroup;
    private List<Long> proposedMethodology;
    private Date startDate;
    private Date endDate;
    private Long projectOwner;
    private Long briefCreator;
    private Boolean multiMarket;
    private BigDecimal totalCost;
    private Long totalCostCurrency;
    private List<Long> endMarkets;
    private List<String> initialCost;
    private List<Long> initialCostCurrency;
    private List<Long> spiContact;
    private Long status;
    private Long capRating;
    private Boolean isConfidential;
    private List<Long> regions;
    private List<Long> areas;
    private Integer budgetYear;
    private Long agencyDept;
    
    private Date projectSaveDate;
    private Date projectStartDate;
    
    private Boolean isSave;
    
    private Integer fieldWorkStudy;
    private Integer methWaiverReq;
    private Integer brandSpecificStudy;
    private Integer brandSpecificStudyType;
    private Integer budgetLocation;
    private List<Long> methodologyDetails;
    
    private Integer projectType;
    private Integer processType;
    private String projectManagerName;
    private Boolean newSynchro;
    private String endMarketName;
    
    private Long refSynchroCode;
    
    private Boolean isCancel;
    
    private Integer endMarketFunding;
    
    private List<Long> fundingMarkets;
    
    private Integer euMarketConfirmation;
    
    private String projectTrackStatus;
    
    private Integer globalOutcomeEUShare;
    
    private String multiBrandStudyText;
    private Integer onlyGlobalType;
    
    private String region;
    private String area;
    private String t20_40;
    
    private String methGroup;
    private String brandType;
    private String methodologies;
    private String categories;
    
    private Boolean isMigrated;
    
    private Boolean hasNewSynchroSaved;
    
	public Long getProjectID() {
		return projectID;
	}
	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<Long> getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(List<Long> categoryType) {
		this.categoryType = categoryType;
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
	public Long getBriefCreator() {
		return briefCreator;
	}
	public void setBriefCreator(Long briefCreator) {
		this.briefCreator = briefCreator;
	}
	public Boolean getMultiMarket() {
		return multiMarket;
	}
	public void setMultiMarket(Boolean multiMarket) {
		this.multiMarket = multiMarket;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}
	public Long getTotalCostCurrency() {
		return totalCostCurrency;
	}
	public void setTotalCostCurrency(Long totalCostCurrency) {
		this.totalCostCurrency = totalCostCurrency;
	}
	public List<Long> getEndMarkets() {
		return endMarkets;
	}
	public void setEndMarkets(List<Long> endMarkets) {
		this.endMarkets = endMarkets;
	}

	public List<String> getInitialCost() {
		return initialCost;
	}
	public void setInitialCost(List<String> initialCost) {
		this.initialCost = initialCost;
	}
	public List<Long> getInitialCostCurrency() {
		return initialCostCurrency;
	}
	public void setInitialCostCurrency(List<Long> initialCostCurrency) {
		this.initialCostCurrency = initialCostCurrency;
	}

    public List<Long> getSpiContact() {
		return spiContact;
	}
	public void setSpiContact(List<Long> spiContact) {
		this.spiContact = spiContact;
	}
	public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }
	public Long getCapRating() {
		return capRating;
	}
	public void setCapRating(Long capRating) {
		this.capRating = capRating;
	}

    public Boolean getConfidential() {
        return isConfidential;
    }

    public void setConfidential(Boolean confidential) {
        isConfidential = confidential;
    }
	public String getDescriptionText() {
		return descriptionText;
	}
	public void setDescriptionText(String descriptionText) {
		this.descriptionText = descriptionText;
	}
	public List<Long> getRegions() {
		return regions;
	}
	public void setRegions(List<Long> regions) {
		this.regions = regions;
	}
	public List<Long> getAreas() {
		return areas;
	}
	public void setAreas(List<Long> areas) {
		this.areas = areas;
	}
	public Boolean getIsSave() {
		return isSave;
	}
	public void setIsSave(Boolean isSave) {
		this.isSave = isSave;
	}

    public Integer getBudgetYear() {
        return budgetYear;
    }

    public void setBudgetYear(Integer budgetYear) {
        this.budgetYear = budgetYear;
    }

    public Long getAgencyDept() {
        return agencyDept;
    }

    public void setAgencyDept(Long agencyDept) {
        this.agencyDept = agencyDept;
    }
	public Date getProjectSaveDate() {
		return projectSaveDate;
	}
	public void setProjectSaveDate(Date projectSaveDate) {
		this.projectSaveDate = projectSaveDate;
	}
	public Date getProjectStartDate() {
		return projectStartDate;
	}
	public void setProjectStartDate(Date projectStartDate) {
		this.projectStartDate = projectStartDate;
	}
	public Integer getFieldWorkStudy() {
		return fieldWorkStudy;
	}
	public void setFieldWorkStudy(Integer fieldWorkStudy) {
		this.fieldWorkStudy = fieldWorkStudy;
	}
	public Integer getMethWaiverReq() {
		return methWaiverReq;
	}
	public void setMethWaiverReq(Integer methWaiverReq) {
		this.methWaiverReq = methWaiverReq;
	}
	public Integer getBrandSpecificStudy() {
		return brandSpecificStudy;
	}
	public void setBrandSpecificStudy(Integer brandSpecificStudy) {
		this.brandSpecificStudy = brandSpecificStudy;
	}
	public Integer getBrandSpecificStudyType() {
		return brandSpecificStudyType;
	}
	public void setBrandSpecificStudyType(Integer brandSpecificStudyType) {
		this.brandSpecificStudyType = brandSpecificStudyType;
	}
	public Integer getBudgetLocation() {
		return budgetLocation;
	}
	public void setBudgetLocation(Integer budgetLocation) {
		this.budgetLocation = budgetLocation;
	}
	public List<Long> getMethodologyDetails() {
		return methodologyDetails;
	}
	public void setMethodologyDetails(List<Long> methodologyDetails) {
		this.methodologyDetails = methodologyDetails;
	}
	public Integer getProjectType() {
		return projectType;
	}
	public void setProjectType(Integer projectType) {
		this.projectType = projectType;
	}
	public Integer getProcessType() {
		return processType;
	}
	public void setProcessType(Integer processType) {
		this.processType = processType;
	}
	public String getProjectManagerName() {
		return projectManagerName;
	}
	public void setProjectManagerName(String projectManagerName) {
		this.projectManagerName = projectManagerName;
	}
	public Boolean getNewSynchro() {
		return newSynchro;
	}
	public void setNewSynchro(Boolean newSynchro) {
		this.newSynchro = newSynchro;
	}
	public String getEndMarketName() {
		return endMarketName;
	}
	public void setEndMarketName(String endMarketName) {
		this.endMarketName = endMarketName;
	}
	public Long getRefSynchroCode() {
		return refSynchroCode;
	}
	public void setRefSynchroCode(Long refSynchroCode) {
		this.refSynchroCode = refSynchroCode;
	}
	public Boolean getIsCancel() {
		return isCancel;
	}
	public void setIsCancel(Boolean isCancel) {
		this.isCancel = isCancel;
	}
	public Integer getEndMarketFunding() {
		return endMarketFunding;
	}
	public void setEndMarketFunding(Integer endMarketFunding) {
		this.endMarketFunding = endMarketFunding;
	}
	
	public Integer getEuMarketConfirmation() {
		return euMarketConfirmation;
	}
	public void setEuMarketConfirmation(Integer euMarketConfirmation) {
		this.euMarketConfirmation = euMarketConfirmation;
	}
	public List<Long> getFundingMarkets() {
		return fundingMarkets;
	}
	public void setFundingMarkets(List<Long> fundingMarkets) {
		this.fundingMarkets = fundingMarkets;
	}
	public String getProjectTrackStatus() {
		return projectTrackStatus;
	}
	public void setProjectTrackStatus(String projectTrackStatus) {
		this.projectTrackStatus = projectTrackStatus;
	}
	public Integer getGlobalOutcomeEUShare() {
		return globalOutcomeEUShare;
	}
	public void setGlobalOutcomeEUShare(Integer globalOutcomeEUShare) {
		this.globalOutcomeEUShare = globalOutcomeEUShare;
	}
	public String getMultiBrandStudyText() {
		return multiBrandStudyText;
	}
	public void setMultiBrandStudyText(String multiBrandStudyText) {
		this.multiBrandStudyText = multiBrandStudyText;
	}
	public Integer getOnlyGlobalType() {
		return onlyGlobalType;
	}
	public void setOnlyGlobalType(Integer onlyGlobalType) {
		this.onlyGlobalType = onlyGlobalType;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getT20_40() {
		return t20_40;
	}
	public void setT20_40(String t20_40) {
		this.t20_40 = t20_40;
	}
	public String getMethGroup() {
		return methGroup;
	}
	public void setMethGroup(String methGroup) {
		this.methGroup = methGroup;
	}
	public String getBrandType() {
		return brandType;
	}
	public void setBrandType(String brandType) {
		this.brandType = brandType;
	}
	public String getMethodologies() {
		return methodologies;
	}
	public void setMethodologies(String methodologies) {
		this.methodologies = methodologies;
	}
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public Boolean getIsMigrated() {
		return isMigrated;
	}
	public void setIsMigrated(Boolean isMigrated) {
		this.isMigrated = isMigrated;
	}
	public Boolean getHasNewSynchroSaved() {
		return hasNewSynchroSaved;
	}
	public void setHasNewSynchroSaved(Boolean hasNewSynchroSaved) {
		this.hasNewSynchroSaved = hasNewSynchroSaved;
	}
}
