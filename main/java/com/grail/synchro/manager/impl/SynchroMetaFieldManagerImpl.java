package com.grail.synchro.manager.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.grail.kantar.beans.KantarReportTypeBean;
import com.grail.synchro.beans.CurrencyExchangeRate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.Currency;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.beans.MetaFieldMapping;
import com.grail.synchro.dao.SynchroMetaFieldDAO;
import com.grail.synchro.manager.SynchroMetaFieldManager;

/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class SynchroMetaFieldManagerImpl implements SynchroMetaFieldManager {
	
	private SynchroMetaFieldDAO synchroMetaFieldDAO;

	public void setSynchroMetaFieldDAO(SynchroMetaFieldDAO synchroMetaFieldDAO) {
		this.synchroMetaFieldDAO = synchroMetaFieldDAO;
	}
	
	/**
	 * End Markets
	 */
	@Override
	public List<MetaField> getEndMarketFields()
	{
		return synchroMetaFieldDAO.getEndMarketFields();
	}
	
	@Override
	public List<MetaField> getAllEndMarketFields()
	{
		return synchroMetaFieldDAO.getAllEndMarketFields();
	}
	
	@Override
	public List<MetaField> getAllRegionFields()
	{
		return synchroMetaFieldDAO.getAllRegionFields();
	}
	
	@Override
	public List<MetaField> getEndMarketFieldsByRegion(Long id)
	{
		return synchroMetaFieldDAO.getEndMarketFieldsByRegion(id);
	}
	
	@Override
	public List<MetaField> getEndMarketFieldsByArea(Long id)
	{
		return synchroMetaFieldDAO.getEndMarketFieldsByArea(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public Long saveEndMarketField(String name, Long region)
	{
		return synchroMetaFieldDAO.saveEndMarketField(name, region);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateEndMarketField(MetaField endMarketField, Long region)
	{
		synchroMetaFieldDAO.updateEndMarketField(endMarketField, region);
	}
	
	
	/**
	 * Synchro Phase 5
	 * @author kanwardeep.grewal
	 */
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveEndMarketFieldOther(Long eid, Long approval, Long markettype,  Long t20_t40_Type)
	{
		synchroMetaFieldDAO.saveEndMarketFieldOther(eid, approval, markettype, t20_t40_Type);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateEndMarketFieldOther(MetaField endMarketField, Long approval, Long marketType, Long t20_t40_Type)
	{
		synchroMetaFieldDAO.updateEndMarketFieldOther(endMarketField, approval, marketType, t20_t40_Type );
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteEndMarketField(Integer id)
	{
		synchroMetaFieldDAO.deleteEndMarketField(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortEndMarketField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortEndMarketField(orderMap);
	}
	
	/**
	 * Methodology Fields
	 */
	@Override
	public List<MetaField> getMethodologyFields()
	{
		return synchroMetaFieldDAO.getMethodologyFields();
	}

    @Override
    public List<MetaField> getAllMethodologyFields()
    {
        return synchroMetaFieldDAO.getAllMethodologyFields();
    }

    @Override
    public List<MetaField> getSelectedInactiveMethodologyFields(List<Long> ids) {
        return synchroMetaFieldDAO.getSelectedInactiveMethodologyFields(ids);
    }

    @Override
    public boolean isMethodologyTypeActive(final Long id) {
        return synchroMetaFieldDAO.isMethodologyTypeActive(id);
    }

    @Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveMethodologyField(MetaField metafield,  List<Long> groups)
	{
		synchroMetaFieldDAO.saveMethodologyField(metafield, groups);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateMethodologyField(MetaField metaField, List<Long> ids)
	{
		synchroMetaFieldDAO.updateMethodologyField(metaField, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteMethodology(Integer id)
	{
		synchroMetaFieldDAO.deleteMethodology(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortMethodologyField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortMethodologyField(orderMap);
	}
	
	/**
	 * Methodology Groups
	 */
	@Override
	public List<MetaField> getMethodologyGroupFields()
	{
		return synchroMetaFieldDAO.getMethodologyGroupFields();
	}
	
	@Override
	public List<MetaField> getMethodologiesByGroup(Long id)
	{
		return synchroMetaFieldDAO.getMethodologiesByGroup(id);
	}
	
	@Override
	public List<MetaField> getAllMethodologiesByGroup(Long id)
	{
		return synchroMetaFieldDAO.getAllMethodologiesByGroup(id);
	}

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<MetaField> getMethodologiesByType(Long id) {
        return synchroMetaFieldDAO.getMethodologiesByType(id);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<MetaField> getMethodologyGrpsByType(Long id) {
        return synchroMetaFieldDAO.getMethodologyGrpsByType(id);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<MetaField> getUnselectedMethodologiesForMethType() {
        return synchroMetaFieldDAO.getUnselectedMethodologiesForMethType();
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<MetaField> getUnselectedMethodologyGroupsForType() {
        return synchroMetaFieldDAO.getUnselectedMethodologyGroupsForType();
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getMethodologyTypeByProposedMethodology(final Long id) {
        return synchroMetaFieldDAO.getMethodologyTypeByProposedMethodology(id);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Set<Long> getMethodologyTypesByProposedMethodologies(List<Long> ids) {
        return synchroMetaFieldDAO.getMethodologyTypesByProposedMethodologies(ids);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public Long getMethodologyTypeByGroup(final Long id) {
        return synchroMetaFieldDAO.getMethodologyTypeByGroup(id);
    }

    @Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveMethodologyGroupField(String name, List<Long> methodologies)
	{
		synchroMetaFieldDAO.saveMethodologyGroupField(name, methodologies);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateMethodologyGroupField(MetaField metaField, List<Long> ids)
	{
		synchroMetaFieldDAO.updateMethodologyGroupField(metaField, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteMethodologyGroup(Integer id)
	{
		synchroMetaFieldDAO.deleteMethodologyGroup(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortMethodologyGroupField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortMethodologyGroupField(orderMap);
	}
	
	/**
	 * Product Fields
	 */
	@Override
	public List<MetaField> getProductFields()
	{
		return synchroMetaFieldDAO.getProductFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveProductField(String name, List<Long> ids)
	{
		synchroMetaFieldDAO.saveProductField(name, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateProductField(MetaField metaField, List<Long> ids)
	{
		synchroMetaFieldDAO.updateProductField(metaField, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteProduct(Integer id)
	{
		synchroMetaFieldDAO.deleteProduct(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortProductField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortProductField(orderMap);
	}
	
	/**
	 * Brand Fields
	 */
	@Override
	public List<MetaField> getBrandFields()
	{
		return synchroMetaFieldDAO.getBrandFields();
	}
	
	@Override
	public List<MetaField> getAllBrandFields()
	{
		return synchroMetaFieldDAO.getAllBrandFields();
	}

    @Override
    public Integer getBrandId(final String name) {
        return synchroMetaFieldDAO.getBrandId(name);
    }

    @Override
	public List<MetaField> getBrandFields(Long id)
	{
		return synchroMetaFieldDAO.getBrandFields(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveBrandField(String name, List<Long> ids)
	{
		synchroMetaFieldDAO.saveBrandField(name,ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateBrandField(MetaField metaField, List<Long> ids)
	{
		synchroMetaFieldDAO.updateBrandField(metaField, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteBrand(Integer id)
	{
		synchroMetaFieldDAO.deleteBrand(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortBrandField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortBrandField(orderMap);
	}
	
	@Override
	public List<MetaField> getProductByBrandInclusion(Long id)
	{
		return synchroMetaFieldDAO.getProductByBrandInclusion(id);
	}
	
	
	@Override
	public List<MetaField> getSupplierGroupFields()
	{
		return synchroMetaFieldDAO.getSupplierGroupFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveSupplierGroupField(String name,  List<Long> suppliers)
	{
		synchroMetaFieldDAO.saveSupplierGroupField(name, suppliers);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateSupplierGroupField(MetaField metaField)
	{
		synchroMetaFieldDAO.updateSupplierGroupField(metaField);
	}
	
	@Override
	public List<MetaField> getSupplierFields()
	{
		return synchroMetaFieldDAO.getSupplierFields();
	}
	
	@Override
	public List<MetaField> getSupplierFields(Long id)
	{
		return synchroMetaFieldDAO.getSupplierFields(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveSupplierField(String name, List<Long> groups)
	{
		synchroMetaFieldDAO.saveSupplierField(name, groups);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateSupplierField(MetaField metaField)
	{
		synchroMetaFieldDAO.updateSupplierField(metaField);
	}
	
	

	@Override
	public List<MetaField> getFwSupplierGroupFields()
	{
		return synchroMetaFieldDAO.getFwSupplierGroupFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveFwSupplierGroupField(String name,  List<Long> fWsuppliers)
	{
		synchroMetaFieldDAO.saveFwSupplierGroupField(name, fWsuppliers);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateFwSupplierGroupField(MetaField metaField)
	{
		synchroMetaFieldDAO.updateFwSupplierGroupField(metaField);
	}
	
	@Override
	public List<MetaField> getFwSupplierFields()
	{
		return synchroMetaFieldDAO.getFwSupplierFields();
	}
	
	@Override
	public List<MetaField> getFwSupplierFields(Long id)
	{
		return synchroMetaFieldDAO.getFwSupplierFields(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveFwSupplierField(String name, List<Long> fWgroups)
	{
		synchroMetaFieldDAO.saveFwSupplierField(name, fWgroups);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateFwSupplierField(MetaField metaField)
	{
		synchroMetaFieldDAO.updateFwSupplierField(metaField);
	}
	
	
	@Override
	public List<Currency> getCurrencyFields()
	{
		return synchroMetaFieldDAO.getCurrencyFields();
		
	}
	@Override
	public List<Currency> getGlobalCurrencyFields()
	{
		return synchroMetaFieldDAO.getGlobalCurrencyFields();
	}
	@Override
	public List<Currency> getNonGlobalCurrencyFields()
	{
		return synchroMetaFieldDAO.getNonGlobalCurrencyFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveCurrencyField(String name, String description, final String globalCurrency)
	{
		synchroMetaFieldDAO.saveCurrencyField(name, description, globalCurrency);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateCurrencyField(Currency currency)
	{
		synchroMetaFieldDAO.updateCurrencyField(currency);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteCurrencyField(Integer id)
	{
		synchroMetaFieldDAO.deleteCurrencyField(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortCurrencyField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortCurrencyField(orderMap);
	}

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public List<CurrencyExchangeRate> getCurrencyExchangeRates() {
        return synchroMetaFieldDAO.getCurrencyExchangeRates();
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public CurrencyExchangeRate getCurrencyExchangeRate(final Long id) {
        return synchroMetaFieldDAO.getCurrencyExchangeRate(id);
    }

    @Override
	public List<MetaField> getTAgencyFields()
	{
		return synchroMetaFieldDAO.getTAgencyFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveTAgencyField(String name)
	{
		synchroMetaFieldDAO.saveTAgencyField(name);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateTAgencyField(MetaField metaField)
	{
		synchroMetaFieldDAO.updateTAgencyField(metaField);
	}
	
	/**
	 * Data Collection Methods
	 */
	@Override
	public List<MetaField> getDataCollectionFields()
	{
		return synchroMetaFieldDAO.getDataCollectionFields();
	}
	
	@Override
	public List<MetaField> getDataCollectionFields(Long type)
	{
		return synchroMetaFieldDAO.getDataCollectionFields(type);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveDataCollectionField(String name,  List<Long> types)
	{
		synchroMetaFieldDAO.saveDataCollectionField(name, types);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateDataCollectionField(MetaField metaField,  List<Long> types)
	{
		synchroMetaFieldDAO.updateDataCollectionField(metaField, types);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteDataCollection(Integer id)
	{
		synchroMetaFieldDAO.deleteDataCollection(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortDataCollectionField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortDataCollectionField(orderMap);
	}
	
	/**
	 * Methodology Type 
	 */
	@Override
	public List<MetaField> getMethodologyTypeFields()
	{
		return synchroMetaFieldDAO.getMethodologyTypeFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveMethodologyTypeField(String name, List<Long> collections)
	{
		synchroMetaFieldDAO.saveMethodologyTypeField(name, collections);
	}

    @Override
    public void saveMethodologyTypeField(String name, List<Long> collections, List<Long> methodologies) {
        synchroMetaFieldDAO.saveMethodologyTypeField(name, collections, methodologies);
    }

    @Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateMethodologyTypeField(MetaField metaField, List<Long> collections)
	{
		synchroMetaFieldDAO.updateMethodologyTypeField(metaField, collections);
	}

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void updateMethodologyTypeField(MetaField metaField, List<Long> collections, List<Long> methodologies)
    {
        synchroMetaFieldDAO.updateMethodologyTypeField(metaField, collections, methodologies);
    }
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteMethodologyType(Integer id)
	{
		synchroMetaFieldDAO.deleteMethodologyType(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortMethodologyTypeField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortMethodologyTypeField(orderMap);
	}
	
	/**
	 * Region
	 */
	@Override
	public List<MetaField> getRegionFields()
	{
		return synchroMetaFieldDAO.getRegionFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveRegionField(String name, List<Long> ids)
	{
		synchroMetaFieldDAO.saveRegionField(name, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateRegionField(MetaField metaField, List<Long> ids)
	{
		synchroMetaFieldDAO.updateRegionField(metaField, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteRegion(Integer id)
	{
		synchroMetaFieldDAO.deleteRegion(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortRegionField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortRegionField(orderMap);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public List<MetaField> getRegionsByEndMarkets(List<Long> ids)
	{
		return synchroMetaFieldDAO.getRegionsByEndMarkets(ids);
	}
	
	
	/**
	 * Areas
	 */
	@Override
	public List<MetaField> getAreaFields()
	{
		return synchroMetaFieldDAO.getAreaFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveAreaField(String name, List<Long> ids)
	{
		synchroMetaFieldDAO.saveAreaField(name, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateAreaField(MetaField metaField, List<Long> ids)
	{
		synchroMetaFieldDAO.updateAreaField(metaField, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteArea(Integer id)
	{
		synchroMetaFieldDAO.deleteArea(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortAreaField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortAreaField(orderMap);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public List<MetaField> getAreaByEndMarkets(List<Long> ids)
	{
		return synchroMetaFieldDAO.getAreaByEndMarkets(ids);
	}

	/**
	 * Job Titles
	 */
	/**
	 * Areas
	 */
	@Override
	public List<MetaField> getJobTitles()
	{
		return synchroMetaFieldDAO.getJobTitles();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveJobTitle(String name)
	{
		synchroMetaFieldDAO.saveJobTitle(name);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateJobTitle(MetaField metaField)
	{
		synchroMetaFieldDAO.updateJobTitle(metaField);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteJobTitles(Integer id)
	{
		synchroMetaFieldDAO.deleteJobTitles(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortJobTitles(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortJobTitles(orderMap);
	}
	
	 @Override
	 public List<MetaFieldMapping> getEndMarketRegionMapping()
	 {
		 return synchroMetaFieldDAO.getEndMarketRegionMapping();
	 }
	 
	 @Override
	 public List<MetaFieldMapping> getEndMarketAreaMapping()
	 {
		 return synchroMetaFieldDAO.getEndMarketAreaMapping();
	 }
	 
	 @Override
	 @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	 public void setCountryCurrencyMapping(Integer countryid, Integer currencyid)
	 {
		 synchroMetaFieldDAO.setCountryCurrencyMapping(countryid, currencyid);
	 }
	 
	 @Override
	 public List<MetaFieldMapping> getCountryCurrencyMapping()
	 {
		 return synchroMetaFieldDAO.getCountryCurrencyMapping();
	 }

    @Override
    public Long setKantarReportType(final String name) {
        return synchroMetaFieldDAO.setKantarReportType(name);
    }

    @Override
    public Long setKantarReportType(final String name, final boolean otherType) {
        return synchroMetaFieldDAO.setKantarReportType(name, otherType);
    }

    @Override
    public MetaField getKantarReportType(final String name) {
        return synchroMetaFieldDAO.getKantarReportType(name);
    }

    @Override
    public MetaField getKantarReportType(final Long id) {
        return synchroMetaFieldDAO.getKantarReportType(id);
    }

    @Override
    public List<MetaField> getKantarReportTypes() {
        return synchroMetaFieldDAO.getKantarReportTypes();
    }

    @Override
    public List<KantarReportTypeBean> getKantarReportTypeBeans() {
        return synchroMetaFieldDAO.getKantarReportTypeBeans();
    }

    @Override
    public Long updateKantarReportType(final Long id, final String name) {
        return synchroMetaFieldDAO.updateKantarReportType(id, name);
    }

    @Override
    public void deleteKantarReportType(Long id) {
        synchroMetaFieldDAO.deleteKantarReportType(id);
    }

    @Override
    public void sortKantarReportTypes(Map<Long, Integer> orderMap) {
        synchroMetaFieldDAO.sortKantarReportTypes(orderMap);
    }

    @Override
    public Long setKantarButtonMethodologyType(String name) {
        return synchroMetaFieldDAO.setKantarButtonMethodologyType(name);
    }

    @Override
    public MetaField getKantarButtonMethodologyType(String name) {
        return synchroMetaFieldDAO.getKantarButtonMethodologyType(name);
    }

    @Override
    public MetaField getKantarButtonMethodologyType(Long id) {
        return synchroMetaFieldDAO.getKantarButtonMethodologyType(id);
    }

    @Override
    public List<MetaField> getKantarButtonMethodologyTypes() {
        return synchroMetaFieldDAO.getKantarButtonMethodologyTypes();
    }

    @Override
    public List<MetaField> getAllKantarButtonMethodologyTypes() {
        return synchroMetaFieldDAO.getAllKantarButtonMethodologyTypes();
    }

    @Override
    public void updateKantarButtonMethodologyType(MetaField metaField) {
        synchroMetaFieldDAO.updateKantarButtonMethodologyType(metaField);
    }

    @Override
    public void deleteKantarButtonMethodologyType(Long id) {
       synchroMetaFieldDAO.deleteKantarButtonMethodologyType(id);
    }

    @Override
    public void sortKantarButtonMethodologyTypes(Map<Long, Integer> orderMap) {
        synchroMetaFieldDAO.sortKantarButtonMethodologyTypes(orderMap);
    }

    @Override
    public Long setGrailButtonMethodologyType(String name) {
        return synchroMetaFieldDAO.setGrailButtonMethodologyType(name);
    }

    @Override
    public MetaField getGrailButtonMethodologyType(String name) {
        return synchroMetaFieldDAO.getGrailButtonMethodologyType(name);
    }

    @Override
    public MetaField getGrailButtonMethodologyType(Long id) {
        return synchroMetaFieldDAO.getGrailButtonMethodologyType(id);
    }

    @Override
    public List<MetaField> getGrailButtonMethodologyTypes() {
        return synchroMetaFieldDAO.getGrailButtonMethodologyTypes();
    }

    @Override
    public List<MetaField> getAllGrailButtonMethodologyTypes() {
        return synchroMetaFieldDAO.getAllGrailButtonMethodologyTypes();
    }

    @Override
    public void updateGrailButtonMethodologyType(MetaField metaField) {
        synchroMetaFieldDAO.updateGrailButtonMethodologyType(metaField);
    }

    @Override
    public void deleteGrailButtonMethodologyType(Long id) {
        synchroMetaFieldDAO.deleteGrailButtonMethodologyType(id);
    }

    @Override
    public void sortGrailButtonMethodologyTypes(Map<Long, Integer> orderMap) {
        synchroMetaFieldDAO.sortGrailButtonMethodologyTypes(orderMap);
    }
    
    @Override
    public List<MetaField> getResearchAgencyFields()
    {
    	return synchroMetaFieldDAO.getResearchAgencyFields();
    }
    
    @Override
    public List<MetaField> getAllResearchAgencyFields()
    {
    	return synchroMetaFieldDAO.getAllResearchAgencyFields();
    }
    @Override
    public List<MetaField> getResearchAgencyGroupFields()
    {
    	return synchroMetaFieldDAO.getResearchAgencyGroupFields();
    }
    @Override
    public List<MetaField> getResearchAgencyByGroup(Long id)
    {
    	return synchroMetaFieldDAO.getResearchAgencyByGroup(id);
    }
    
    @Override
    public List<MetaField> getAllResearchAgencyByGroup(Long id)
    {
    	return synchroMetaFieldDAO.getAllResearchAgencyByGroup(id);
    }
    
    /**
     * Synchro Phase 5
     * @author kanwardeep.grewal
     */
    // T20/T40 APIs
	@Override
	public List<MetaField> getT20T40Fields()
	{
		return synchroMetaFieldDAO.getT20T40Fields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveT20T40Field(String name, List<Long> ids)
	{
		synchroMetaFieldDAO.saveT20T40Field(name, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateT20T40Field(MetaField metaField, List<Long> ids)
	{
		synchroMetaFieldDAO.updateT20T40Field(metaField, ids);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteT20T40(Integer id)
	{
		synchroMetaFieldDAO.deleteT20T40(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortT20T40Field(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortT20T40Field(orderMap);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public List<MetaField> getT20T40ByEndMarkets(List<Long> ids)
	{
		return synchroMetaFieldDAO.getT20T40ByEndMarkets(ids);
	}
	
	@Override
	public List<MetaField> getEndMarketFieldsByT20T40(Long id)
	{
		return synchroMetaFieldDAO.getEndMarketFieldsByT20T40(id);
	}
	
	@Override
	public Integer getApprovalByEndmarket(Long id)
	{
		return synchroMetaFieldDAO.getApprovalByEndmarket(id);
	}
	
	@Override
	public Integer getMarketByEndmarket(Long id)
	{
		return synchroMetaFieldDAO.getMarketByEndmarket(id);
	}
	
	@Override
	public Integer getT20_T40_ByEndmarket(Long id)
	{
		return synchroMetaFieldDAO.getT20_T40_ByEndmarket(id);
	}
	
	/***
	 * Grail Agency Research & Group Tables
	 * Phase 5
	 * 	
	 */
	
	@Override
	public List<MetaField> getGrailResearchAgencyGroupFields()
	{
		return synchroMetaFieldDAO.getGrailResearchAgencyGroupFields();
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveGrailResearchAgencyGroupField(String name,  List<Long> agencies)
	{
		synchroMetaFieldDAO.saveGrailResearchAgencyGroupField(name, agencies);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateGrailResearchAgencyGroupField(MetaField metaField, List<Long> agencies)
	{
		synchroMetaFieldDAO.updateGrailResearchAgencyGroupField(metaField, agencies);
	}
	
	@Override
	public List<MetaField> getGrailResearchAgencyFields()
	{
		return synchroMetaFieldDAO.getGrailResearchAgencyFields();
	}
	
	@Override
	public List<MetaField> getGrailResearchAgencyFields(Long id)
	{
		return synchroMetaFieldDAO.getGrailResearchAgencyFields(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void saveGrailResearchAgencyField(String name, List<Long> groups)
	{
		synchroMetaFieldDAO.saveGrailResearchAgencyField(name, groups);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void updateGrailResearchAgencyField(MetaField metaField, List<Long> groups)
	{
		synchroMetaFieldDAO.updateGrailResearchAgencyField(metaField, groups);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void sortGrailResearchAgencyField(Map<Integer, Integer> orderMap)
	{
		synchroMetaFieldDAO.sortGrailResearchAgencyField(orderMap);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteResearchAgency(Integer id)
	{
		synchroMetaFieldDAO.deleteResearchAgency(id);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public void deleteResearchAgencyGroup(Integer id)
	{
		synchroMetaFieldDAO.deleteResearchAgencyGroup(id);
	}
}
