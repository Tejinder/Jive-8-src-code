package com.grail.synchro.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.grail.kantar.beans.KantarReportTypeBean;
import com.grail.synchro.beans.Currency;
import com.grail.synchro.beans.CurrencyExchangeRate;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.beans.MetaFieldMapping;



/**
 * @author Kanwar Grewal
 * @version 1.0, Date 5/30/13
 */

public interface SynchroMetaFieldDAO {
	
	/**
	 * Endmarkets
	 * @return
	 */
	public List<MetaField> getEndMarketFields();
	
	public List<MetaField> getAllEndMarketFields();
	
	public List<MetaField> getEndMarketFieldsByRegion(Long id);
	
	public List<MetaField> getEndMarketFieldsByArea(Long id);
	
	public Long saveEndMarketField(String name, Long region);
	
	public void updateEndMarketField(MetaField endMarketField, Long region);
	
	/**
	 * Synchro Phase 5
	 * @param metaField
	 * @param approval
	 * @param marketType
	 * @author kanwardeep.grewal
	 */
	public void updateEndMarketFieldOther(MetaField metaField, Long approval, Long marketType, Long t20_t40_Type);
	
	public void saveEndMarketFieldOther(Long eid, Long approval, Long markettype, Long t20_t40_Type);
	
	
	public void deleteEndMarketField(Integer id);
	
	public void sortEndMarketField(Map<Integer, Integer> orderMap);
	
	/**
	 * Methodology
	 * @return
	 */
	public List<MetaField> getMethodologyFields();
    public List<MetaField> getAllMethodologyFields();
    public boolean isMethodologyTypeActive(final Long id);
    public List<MetaField> getSelectedInactiveMethodologyFields(final List<Long> ids);
	
	public void saveMethodologyField(MetaField metafield, List<Long> groups);
	
	public void updateMethodologyField(MetaField metaField, List<Long> ids);
	
	public void deleteMethodology(Integer id);
	
	public void sortMethodologyField(Map<Integer, Integer> orderMap);
	
	/**
	 * Methodology Group
	 * @return
	 */
	 
	public List<MetaField> getMethodologyGroupFields();
	
	public List<MetaField> getMethodologiesByGroup(Long id);
	public List<MetaField> getAllMethodologiesByGroup(Long id);
    public List<MetaField> getMethodologiesByType(Long id);
    public List<MetaField> getMethodologyGrpsByType(Long id);
    List<MetaField> getUnselectedMethodologiesForMethType();
    List<MetaField> getUnselectedMethodologyGroupsForType();

    Long getMethodologyTypeByProposedMethodology(final Long id);
    Set<Long> getMethodologyTypesByProposedMethodologies(final List<Long> ids);
    Long getMethodologyTypeByGroup(final Long id);

	
	public void saveMethodologyGroupField(String name, List<Long> methodologies);
	
	public void updateMethodologyGroupField(MetaField metaField, List<Long> ids);
	
	public void deleteMethodologyGroup(Integer id);
	
	public void sortMethodologyGroupField(Map<Integer, Integer> orderMap);
	
	/**
	 * 
	 * Product Fields
	 */
	public List<MetaField> getProductFields();
	
	public void saveProductField(String name, List<Long> brands);
	
	public void updateProductField(MetaField metaField, List<Long> ids);
	
	public void deleteProduct(Integer id);
	
	public void sortProductField(Map<Integer, Integer> orderMap);
	
	/**
	 * 
	 * Brand Fields
	 */
	
	public List<MetaField> getBrandFields();
	
	public List<MetaField> getAllBrandFields();
	
	public List<MetaField> getBrandFields(Long id);

    public Integer getBrandId(final String name);
	
	public void saveBrandField(String name, List<Long> ids);
	
	public void updateBrandField(MetaField metaField, List<Long> ids);
	
	public List<MetaField> getProductByBrandInclusion(Long id);
	
	public void deleteBrand(Integer id);
	
	public void sortBrandField(Map<Integer, Integer> orderMap);
	
	/**
	 * Suppliers
	 */
	
	public List<MetaField> getSupplierGroupFields();
	
	public void saveSupplierGroupField(String name, List<Long> suppliers);
	
	public void updateSupplierGroupField(MetaField metaField);
	
	public List<MetaField> getSupplierFields();
	
	public List<MetaField> getSupplierFields(Long id);
	
	public void saveSupplierField(String name, List<Long> groups);
	
	public void updateSupplierField(MetaField metaField);
	
	
	/**
	 * Field Work Suppliers
	 */
	
	public List<MetaField> getFwSupplierGroupFields();
	
	public void saveFwSupplierGroupField(String name, List<Long> fWsuppliers);
	
	public void updateFwSupplierGroupField(MetaField metaField);
	
	public List<MetaField> getFwSupplierFields();
	
	public List<MetaField> getFwSupplierFields(Long id);
	
	public void saveFwSupplierField(String name, List<Long> fWgroups);
	
	public void updateFwSupplierField(MetaField metaField);
	
	/**
	 * 
	 * Currency Fields
	 */
	public List<Currency> getCurrencyFields();
	public void saveCurrencyField(String name, String description, final String globalCurrency);
	
	public void updateCurrencyField(Currency currency);
	
	public void deleteCurrencyField(Integer id);
	
	public void sortCurrencyField(Map<Integer, Integer> orderMap);

    public List<CurrencyExchangeRate> getCurrencyExchangeRates();
    public CurrencyExchangeRate getCurrencyExchangeRate(final Long id);


    /**
	 * 
	 * Tendering Agency Fields
	 */
	public List<MetaField> getTAgencyFields();
	
	public void saveTAgencyField(String name);
	
	public void updateTAgencyField(MetaField metaField);
	
	
	/**
	 * Data Collection Fields
	 */
	public List<MetaField> getDataCollectionFields();
	
	public List<MetaField> getDataCollectionFields(Long type);
	
	public void saveDataCollectionField(String name, List<Long> types);
	
	public void updateDataCollectionField(MetaField metaField, List<Long> types);
	
	public void deleteDataCollection(Integer id);
	
	public void sortDataCollectionField(Map<Integer, Integer> orderMap);
	
	/**
	 * Methodology Type Fields
	 */
	public List<MetaField> getMethodologyTypeFields();
	
	public void saveMethodologyTypeField(String name, List<Long> collections);
    public void saveMethodologyTypeField(String name, List<Long> collections, List<Long> methodologies);
	
	public void updateMethodologyTypeField(MetaField metaField, List<Long> collections);
    public void updateMethodologyTypeField(MetaField metaField, List<Long> collections, List<Long> methodologies);
	
	public void deleteMethodologyType(Integer id);
	
	public void sortMethodologyTypeField(Map<Integer, Integer> orderMap);
	
	/**
	 * Regions
	 */
	public List<MetaField> getRegionFields();
	public List<MetaField> getAllRegionFields();
	
	public void saveRegionField(String name, List<Long> ids);
	
	public void updateRegionField(MetaField metaField, List<Long> ids);
	
	public void deleteRegion(Integer id);
	
	public void sortRegionField(Map<Integer, Integer> orderMap);
	
	public List<MetaField> getRegionsByEndMarkets(List<Long> ids);
	
	/**
	 * Areas
	 */
	public List<MetaField> getAreaFields();
	
	public void saveAreaField(String name, List<Long> ids);
	
	public void updateAreaField(MetaField metaField, List<Long> ids);
	
	public void deleteArea(Integer id);
	
	public void sortAreaField(Map<Integer, Integer> orderMap);
	
	public List<MetaField> getAreaByEndMarkets(List<Long> ids);
	
	/**
	 * Job Titles
	 */
	public List<MetaField> getJobTitles();
	
	public void saveJobTitle(String name);
	
	public void updateJobTitle(MetaField metaField);
	
	public void deleteJobTitles(Integer id);
	
	public void sortJobTitles(Map<Integer, Integer> orderMap);
	
	/**
	 * Endmarket-Region mapping
	 */
	
	public List<MetaFieldMapping> getEndMarketRegionMapping();
	/**
	 * Endmarket-Area mapping
	 */
	public List<MetaFieldMapping> getEndMarketAreaMapping();
	
	/**
	 * Country Currency ID
	 */
	public void setCountryCurrencyMapping(Integer countryid, Integer currencyid);
	
	public List<MetaFieldMapping> getCountryCurrencyMapping();

    /**
     * Kantar Report Types
     */

    public Long setKantarReportType(final String name);
    public Long setKantarReportType(final String name, final boolean otherType);
    public MetaField getKantarReportType(final String name);
    public MetaField getKantarReportType(final Long id);
    public List<MetaField> getKantarReportTypes();
    public List<KantarReportTypeBean> getKantarReportTypeBeans();
    public Long updateKantarReportType(final Long id, final String name);
    public void deleteKantarReportType(final Long id);
    public void sortKantarReportTypes(Map<Long, Integer> orderMap);


    /**
     * Kantar Button methodology types
     */
    public Long setKantarButtonMethodologyType(final String name);
    public MetaField getKantarButtonMethodologyType(final String name);
    public MetaField getKantarButtonMethodologyType(final Long id);
    public List<MetaField> getKantarButtonMethodologyTypes();
    public void updateKantarButtonMethodologyType(MetaField metaField);
    public void deleteKantarButtonMethodologyType(final Long id);
    public void sortKantarButtonMethodologyTypes(Map<Long, Integer> orderMap);
    public List<MetaField> getAllKantarButtonMethodologyTypes();

    /**
     * Grail Button methodology types
     */
    public Long setGrailButtonMethodologyType(final String name);
    public MetaField getGrailButtonMethodologyType(final String name);
    public MetaField getGrailButtonMethodologyType(final Long id);
    public List<MetaField> getGrailButtonMethodologyTypes();
    public void updateGrailButtonMethodologyType(MetaField metaField);
    public void deleteGrailButtonMethodologyType(final Long id);
    public void sortGrailButtonMethodologyTypes(Map<Long, Integer> orderMap);
    public List<MetaField> getAllGrailButtonMethodologyTypes();
    public List<MetaField> getResearchAgencyFields();
    public List<MetaField> getAllResearchAgencyFields();
    public List<MetaField> getResearchAgencyGroupFields();
    public List<MetaField> getResearchAgencyByGroup(final Long id);
    
    public List<MetaField> getAllResearchAgencyByGroup(final Long id);
    
    
    /**
     * Synchro Phase 4
     * @author kanwardeep.grewal 
     */
    
    /**
	 * T20/T40 APIs
	 */
	public List<MetaField> getT20T40Fields();
	
	public void saveT20T40Field(String name, List<Long> ids);
	
	public void updateT20T40Field(MetaField metaField, List<Long> ids);
	
	public void deleteT20T40(Integer id);
	
	public void sortT20T40Field(Map<Integer, Integer> orderMap);
	
	public List<MetaField> getT20T40ByEndMarkets(List<Long> ids);
	
	public List<MetaField> getEndMarketFieldsByT20T40(Long id);
	
	public Integer getApprovalByEndmarket(Long id);
	
	public Integer getMarketByEndmarket(Long id);
	
	public Integer getT20_T40_ByEndmarket(Long id);
	
	/**
	 * Grail Research Agency & Group Tables	
	 */
	
	public List<MetaField> getGrailResearchAgencyGroupFields();
	
	public void saveGrailResearchAgencyGroupField(String name, List<Long> agencies);
	
	public void updateGrailResearchAgencyGroupField(MetaField metaField, List<Long> agencies);
	
	public List<MetaField> getGrailResearchAgencyFields();
	
	public List<MetaField> getGrailResearchAgencyFields(Long id);
	
	public void saveGrailResearchAgencyField(String name, List<Long> groups);
	
	public void updateGrailResearchAgencyField(MetaField metaField, List<Long> groups);
	public List<Currency> getGlobalCurrencyFields();
	public List<Currency> getNonGlobalCurrencyFields();
	public void sortGrailResearchAgencyField(Map<Integer, Integer> orderMap);
	
	public void deleteResearchAgency(Integer id);
	public void deleteResearchAgencyGroup(Integer id);
}
