package com.grail.synchro.search.filter;


import com.jivesoftware.base.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ProjectResultFilter {

	private String name;
	private List<Long> ownerfield;
	private Integer startMonth;
    private Integer startYear;
    private Integer endMonth;
    private Integer endYear;
    private List<Long> methodologyFields;
    private List<Long> brandFields;
    private List<Long> regionFields;
    private List<Long> endMarkets;
    private List<Long> projectSupplierFields;
    private String keyword;
    private Integer start;
    private Integer limit;
    private String sortField;
    private Integer ascendingOrder;
    private boolean fetchOnlyUserSpecificProjects = false;
    private List<Long> projectStatusFields;
    private List<String> projectActivityFields;
    private List<Long> spiContacts;
    private List<Long> agencies;
    private List<Long> agencyNames; // Departments
    private User user;
    private Integer draftProjectRemindOffset;
    private boolean fetchOnlyDraftProjects = false;
    private boolean ignoreSuperUserAccess = false;

    private boolean fetchProductContacts = true;
    private boolean fetchDraftProjects = false;
    private boolean fetchEndMarketProjects = false;
    private boolean fetchGlobalProjects = false;
    private boolean fetchRegionalProjects = false;
    
    private boolean fetchCancelProjects = false;
    
    // Synchro New Requirement Filters
    private List<Long> projectTypes;
    private List<Long> projectStatus;
    private List<Long> projectStages;
    private Date startDateBegin;
    private Date startDateComplete;
    private Date endDateBegin;
    private Date endDateComplete;
    private String projManager;
    private String projectInitiator;
    private List<Long> categoryTypes;
    private List<Long> researchEndMarkets;
    private List<Long> researchAgencies;
    private List<Long> methDetails;
    private List<Long> methodologyTypes;
    private List<Long> brands;
    private List<Long> budgetLocations;
    private List<Long> budgetYears;
    private BigDecimal totalCostStart;
    private BigDecimal totalCostEnd;
    
    private String waiverInitiator;
    private List<Long> waiverStatus;
    
    private Date creationDateBegin;
    private Date creationDateComplete;
    private List<Long> actionPendings;
    private List<Long> costComponents;
    
    private List<Long> tpdStatus;
    private Date tpdSubmitDateBegin;
    private Date tpdSubmitDateComplete;
    
    private Long budgetYearSelected;
    
    public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}
	public Integer getAscendingOrder() {
		return ascendingOrder;
	}
	public void setAscendingOrder(Integer ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}
	public List<Long> getProjectSupplierFields() {
		return projectSupplierFields;
	}
	public void setProjectSupplierFields(List<Long> projectSupplierFields) {
		this.projectSupplierFields = projectSupplierFields;
	}
	public List<Long> getEndMarkets() {
		return endMarkets;
	}
	public void setEndMarkets(List<Long> endMarkets) {
		this.endMarkets = endMarkets;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Long> getOwnerfield() {
		return ownerfield;
	}
	public void setOwnerfield(List<Long> ownerfield) {
		this.ownerfield = ownerfield;
	}
	public Integer getStartMonth() {
		return startMonth;
	}
	public void setStartMonth(Integer startMonth) {
		this.startMonth = startMonth;
	}
	public Integer getStartYear() {
		return startYear;
	}
	public void setStartYear(Integer startYear) {
		this.startYear = startYear;
	}
	public Integer getEndMonth() {
		return endMonth;
	}
	public void setEndMonth(Integer endMonth) {
		this.endMonth = endMonth;
	}
	public Integer getEndYear() {
		return endYear;
	}
	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}
	public List<Long> getMethodologyFields() {
		return methodologyFields;
	}
	public void setMethodologyFields(List<Long> methodologyFields) {
		this.methodologyFields = methodologyFields;
	}
	public List<Long> getBrandFields() {
		return brandFields;
	}
	public void setBrandFields(List<Long> brandFields) {
		this.brandFields = brandFields;
	}
	public List<Long> getRegionFields() {
		return regionFields;
	}
	public void setRegionFields(List<Long> regionFields) {
		this.regionFields = regionFields;
	}

	public List<Long> getProjectStatusFields() {
		return projectStatusFields;
	}
	public void setProjectStatusFields(List<Long> projectStatusFields) {
		this.projectStatusFields = projectStatusFields;
	}
	public List<String> getProjectActivityFields() {
		return projectActivityFields;
	}
	public void setProjectActivityFields(List<String> projectActivityFields) {
		this.projectActivityFields = projectActivityFields;
	}

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public boolean isFetchOnlyUserSpecificProjects() {
        return fetchOnlyUserSpecificProjects;
    }

    public void setFetchOnlyUserSpecificProjects(boolean fetchOnlyUserSpecificProjects) {
        this.fetchOnlyUserSpecificProjects = fetchOnlyUserSpecificProjects;
    }

    public List<Long> getSpiContacts() {
        return spiContacts;
    }

    public void setSpiContacts(List<Long> spiContacts) {
        this.spiContacts = spiContacts;
    }

    public List<Long> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<Long> agencies) {
        this.agencies = agencies;
    }

    public List<Long> getAgencyNames() {
        return agencyNames;
    }

    public void setAgencyNames(List<Long> agencyNames) {
        this.agencyNames = agencyNames;
    }
	public boolean isFetchProductContacts() {
		return fetchProductContacts;
	}
	public void setFetchProductContacts(boolean fetchProductContacts) {
		this.fetchProductContacts = fetchProductContacts;
	}
	public boolean isFetchDraftProjects() {
		return fetchDraftProjects;
	}
	public void setFetchDraftProjects(boolean fetchDraftProjects) {
		this.fetchDraftProjects = fetchDraftProjects;
	}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getDraftProjectRemindOffset() {
        return draftProjectRemindOffset;
    }

    public void setDraftProjectRemindOffset(Integer draftProjectRemindOffset) {
        this.draftProjectRemindOffset = draftProjectRemindOffset;
    }

    public boolean isFetchOnlyDraftProjects() {
        return fetchOnlyDraftProjects;
    }

    public void setFetchOnlyDraftProjects(boolean fetchOnlyDraftProjects) {
        this.fetchOnlyDraftProjects = fetchOnlyDraftProjects;
    }

    public boolean isIgnoreSuperUserAccess() {
        return ignoreSuperUserAccess;
    }

    public void setIgnoreSuperUserAccess(boolean ignoreSuperUserAccess) {
        this.ignoreSuperUserAccess = ignoreSuperUserAccess;
    }
	public boolean isFetchEndMarketProjects() {
		return fetchEndMarketProjects;
	}
	public void setFetchEndMarketProjects(boolean fetchEndMarketProjects) {
		this.fetchEndMarketProjects = fetchEndMarketProjects;
	}
	public boolean isFetchCancelProjects() {
		return fetchCancelProjects;
	}
	public void setFetchCancelProjects(boolean fetchCancelProjects) {
		this.fetchCancelProjects = fetchCancelProjects;
	}
	public boolean isFetchGlobalProjects() {
		return fetchGlobalProjects;
	}
	public void setFetchGlobalProjects(boolean fetchGlobalProjects) {
		this.fetchGlobalProjects = fetchGlobalProjects;
	}
	public boolean isFetchRegionalProjects() {
		return fetchRegionalProjects;
	}
	public void setFetchRegionalProjects(boolean fetchRegionalProjects) {
		this.fetchRegionalProjects = fetchRegionalProjects;
	}
	public List<Long> getProjectTypes() {
		return projectTypes;
	}
	public void setProjectTypes(List<Long> projectTypes) {
		this.projectTypes = projectTypes;
	}
	public List<Long> getProjectStatus() {
		return projectStatus;
	}
	public void setProjectStatus(List<Long> projectStatus) {
		this.projectStatus = projectStatus;
	}
	public List<Long> getProjectStages() {
		return projectStages;
	}
	public void setProjectStages(List<Long> projectStages) {
		this.projectStages = projectStages;
	}
	
	
	public String getProjectInitiator() {
		return projectInitiator;
	}
	public void setProjectInitiator(String projectInitiator) {
		this.projectInitiator = projectInitiator;
	}
	public List<Long> getCategoryTypes() {
		return categoryTypes;
	}
	public void setCategoryTypes(List<Long> categoryTypes) {
		this.categoryTypes = categoryTypes;
	}
	public List<Long> getResearchEndMarkets() {
		return researchEndMarkets;
	}
	public void setResearchEndMarkets(List<Long> researchEndMarkets) {
		this.researchEndMarkets = researchEndMarkets;
	}
	public List<Long> getResearchAgencies() {
		return researchAgencies;
	}
	public void setResearchAgencies(List<Long> researchAgencies) {
		this.researchAgencies = researchAgencies;
	}
	
	public List<Long> getMethodologyTypes() {
		return methodologyTypes;
	}
	public void setMethodologyTypes(List<Long> methodologyTypes) {
		this.methodologyTypes = methodologyTypes;
	}
	public List<Long> getBrands() {
		return brands;
	}
	public void setBrands(List<Long> brands) {
		this.brands = brands;
	}
	public List<Long> getBudgetLocations() {
		return budgetLocations;
	}
	public void setBudgetLocations(List<Long> budgetLocations) {
		this.budgetLocations = budgetLocations;
	}
	public List<Long> getBudgetYears() {
		return budgetYears;
	}
	public void setBudgetYears(List<Long> budgetYears) {
		this.budgetYears = budgetYears;
	}
	public BigDecimal getTotalCostStart() {
		return totalCostStart;
	}
	public void setTotalCostStart(BigDecimal totalCostStart) {
		this.totalCostStart = totalCostStart;
	}
	public BigDecimal getTotalCostEnd() {
		return totalCostEnd;
	}
	public void setTotalCostEnd(BigDecimal totalCostEnd) {
		this.totalCostEnd = totalCostEnd;
	}
	public Date getStartDateBegin() {
		return startDateBegin;
	}
	public void setStartDateBegin(Date startDateBegin) {
		this.startDateBegin = startDateBegin;
	}
	public Date getStartDateComplete() {
		return startDateComplete;
	}
	public void setStartDateComplete(Date startDateComplete) {
		this.startDateComplete = startDateComplete;
	}
	public Date getEndDateBegin() {
		return endDateBegin;
	}
	public void setEndDateBegin(Date endDateBegin) {
		this.endDateBegin = endDateBegin;
	}
	public Date getEndDateComplete() {
		return endDateComplete;
	}
	public void setEndDateComplete(Date endDateComplete) {
		this.endDateComplete = endDateComplete;
	}
	public String getProjManager() {
		return projManager;
	}
	public void setProjManager(String projManager) {
		this.projManager = projManager;
	}
	public List<Long> getMethDetails() {
		return methDetails;
	}
	public void setMethDetails(List<Long> methDetails) {
		this.methDetails = methDetails;
	}
	public String getWaiverInitiator() {
		return waiverInitiator;
	}
	public void setWaiverInitiator(String waiverInitiator) {
		this.waiverInitiator = waiverInitiator;
	}
	public List<Long> getWaiverStatus() {
		return waiverStatus;
	}
	public void setWaiverStatus(List<Long> waiverStatus) {
		this.waiverStatus = waiverStatus;
	}
	public Date getCreationDateBegin() {
		return creationDateBegin;
	}
	public void setCreationDateBegin(Date creationDateBegin) {
		this.creationDateBegin = creationDateBegin;
	}
	public Date getCreationDateComplete() {
		return creationDateComplete;
	}
	public void setCreationDateComplete(Date creationDateComplete) {
		this.creationDateComplete = creationDateComplete;
	}
	public List<Long> getActionPendings() {
		return actionPendings;
	}
	public void setActionPendings(List<Long> actionPendings) {
		this.actionPendings = actionPendings;
	}
	public List<Long> getCostComponents() {
		return costComponents;
	}
	public void setCostComponents(List<Long> costComponents) {
		this.costComponents = costComponents;
	}
	public List<Long> getTpdStatus() {
		return tpdStatus;
	}
	public void setTpdStatus(List<Long> tpdStatus) {
		this.tpdStatus = tpdStatus;
	}
	public Date getTpdSubmitDateBegin() {
		return tpdSubmitDateBegin;
	}
	public void setTpdSubmitDateBegin(Date tpdSubmitDateBegin) {
		this.tpdSubmitDateBegin = tpdSubmitDateBegin;
	}
	public Date getTpdSubmitDateComplete() {
		return tpdSubmitDateComplete;
	}
	public void setTpdSubmitDateComplete(Date tpdSubmitDateComplete) {
		this.tpdSubmitDateComplete = tpdSubmitDateComplete;
	}
	public Long getBudgetYearSelected() {
		return budgetYearSelected;
	}
	public void setBudgetYearSelected(Long budgetYearSelected) {
		this.budgetYearSelected = budgetYearSelected;
	}
}
