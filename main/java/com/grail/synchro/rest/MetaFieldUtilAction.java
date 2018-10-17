package com.grail.synchro.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.grail.kantar.util.KantarUtils;
import com.grail.util.GrailUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.grail.synchro.beans.Currency;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.manager.SynchroMetaFieldManager;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * RESTful Action related to Synchro Meta fields updation
 *
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class MetaFieldUtilAction extends JiveActionSupport {

    private static final Logger LOG = Logger.getLogger(MetaFieldUtilAction.class);

    private static SynchroMetaFieldManager synchroMetaFieldManager;

	public static SynchroMetaFieldManager getSynchroMetaFieldManager() {
		if(synchroMetaFieldManager==null)
		{
			return JiveApplication.getContext().getSpringBean("synchroMetaFieldManager");
		}
		return synchroMetaFieldManager;
	}

	/**
	 * End Markets	
	 * @param name
	 * @param region
	 */
	public Boolean addEndMarket(final String name, final Integer region, final Integer approval, final Integer markettype, final Integer t20_t40_type )
	{
		Long eid = getSynchroMetaFieldManager().saveEndMarketField(name, Long.parseLong(region.toString()));
		

		
		/** Synchro Phase  5
		 * @author kanwardeep.grewal
		 */
		getSynchroMetaFieldManager().saveEndMarketFieldOther(eid,Long.parseLong(approval.toString()), Long.parseLong(markettype.toString()), Long.parseLong(t20_t40_type.toString()));
		
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean updateEndMarket(final Integer id, final String name, final Integer region, final Integer approval, final Integer marketType, final Integer t20_t40_type)
	{
		MetaField endMarketField = new MetaField();
		endMarketField.setId(Long.parseLong(id.toString()));
		endMarketField.setName(name);
		getSynchroMetaFieldManager().updateEndMarketField(endMarketField, Long.parseLong(region.toString()));
		
		/**
		 * Synchro Phase 5
		 * @author kanwardeep.grewal
		 */
		getSynchroMetaFieldManager().updateEndMarketFieldOther(endMarketField, Long.parseLong(approval.toString()), Long.parseLong(marketType.toString()), Long.parseLong(t20_t40_type.toString()));
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean deleteEndMarket(final Integer id)
	{
		getSynchroMetaFieldManager().deleteEndMarketField(id);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean sortEndMarketField(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortEndMarketField(orderMap);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	
	/**
	 * Proposed methodology
	 * @param name
	 * @param ids
	 */
	public Boolean addMethodology(final String name, final String ids, final String isLessFrequent, final String briefException, 
			final String proposalException, final String agencyWaiverException, final String repSummaryException, final String brandSpecific)
	{
		List<Long> groupIds = new ArrayList<Long>();
		List<String> groupIdsString = new ArrayList<String>();
		if(ids != null)
		{
			groupIdsString = Arrays.asList(ids.split("\\s*,\\s*"));
		}
		for(String pid : groupIdsString)
		{
			groupIds.add(Long.parseLong(pid));
		}
		
		MetaField metaField = new MetaField();		
		metaField.setName(name);
		metaField.setLessFrequent((isLessFrequent.equals("1")?true:false));
		metaField.setBriefException((briefException.equals("1")?true:false));
		metaField.setProposalException((proposalException.equals("1")?true:false));
		metaField.setAgencyWaiverException((agencyWaiverException.equals("1")?true:false));
		metaField.setRepSummaryException((repSummaryException.equals("1")?true:false));
		metaField.setBrandSpecific((brandSpecific.equals("1")?1:2));
		getSynchroMetaFieldManager().saveMethodologyField(metaField, groupIds);
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	public Boolean updateMethodology(final Integer id, final String name, final String ids, final String isLessFrequent, final String briefException, 
			final String proposalException, final String agencyWaiverException, final String repSummaryException, final String brandSpecific)
	{
		
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		metaField.setLessFrequent(((isLessFrequent.equals("1")?true:false)));
		metaField.setBriefException((briefException.equals("1")?true:false));
		metaField.setProposalException((proposalException.equals("1")?true:false));
		metaField.setAgencyWaiverException((agencyWaiverException.equals("1")?true:false));
		metaField.setRepSummaryException((repSummaryException.equals("1")?true:false));
		metaField.setBrandSpecific(new Integer(brandSpecific));
		getSynchroMetaFieldManager().updateMethodologyField(metaField, toArray(ids));
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	public Boolean deleteMethodology(final Integer id)
	{
		getSynchroMetaFieldManager().deleteMethodology(id);
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	public Boolean sortMethodology(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortMethodologyField(orderMap);
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	/**
	 * Methodology Group
	 * @param name
	 * @param ids
	 */
	public Boolean addMethodologyGroup(final String name, final String ids)
	{
		List<Long> methdologyIds = new ArrayList<Long>();
		List<String> methdologyIdsString = new ArrayList<String>();
		if(ids != null)
		{
			methdologyIdsString = Arrays.asList(ids.split("\\s*,\\s*"));
		}
		for(String pid : methdologyIdsString)
		{
			methdologyIds.add(Long.parseLong(pid));
		}
		getSynchroMetaFieldManager().saveMethodologyGroupField(name, methdologyIds);
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	public Boolean updateMethodologyGroup(final Integer id, final String name, final String ids)
	{
		//TODO
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateMethodologyGroupField(metaField, toArray(ids));
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	public Boolean deleteMethodologyGroup(final Integer id)
	{
		getSynchroMetaFieldManager().deleteMethodologyGroup(id);
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	public Boolean sortMethodologyGroup(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortMethodologyGroupField(orderMap);
		SynchroUtils.triggerMethodology();
		return true;
	}
	
	/**
	 * Category/Product
	 * @param name
	 * @param ids
	 */
	public Boolean addProduct(final String name, final String ids)
	{
		List<Long> brandIds = new ArrayList<Long>();
		List<String> brandIdString = new ArrayList<String>();
		if(ids != null)
		{
			brandIdString = Arrays.asList(ids.split("\\s*,\\s*"));
		}
		for(String pid : brandIdString)
		{
			brandIds.add(Long.parseLong(pid));
		}
		getSynchroMetaFieldManager().saveProductField(name, brandIds);
		SynchroUtils.triggerProduct();
		return true;
	}
	
	public Boolean updateProduct(final Integer id, final String name, final String ids)
	{
		//TODO
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateProductField(metaField, toArray(ids));
		SynchroUtils.triggerProduct();
		return true;
	}
	
	public Boolean deleteProduct(final Integer id)
	{
		getSynchroMetaFieldManager().deleteProduct(id);
		SynchroUtils.triggerProduct();
		return true;
	}
	
	public Boolean sortProduct(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortProductField(orderMap);
		SynchroUtils.triggerProduct();
		return true;
	}
	
	/**
	 * Brand
	 * @param name
	 * @param ids
	 */
	public Boolean addBrand(final String name, final String ids)
		{
		List<Long> productIds = new ArrayList<Long>();
		List<String> productIdString = new ArrayList<String>();
		if(ids != null)
		{
			productIdString = Arrays.asList(ids.split("\\s*,\\s*"));
		}
		for(String pid : productIdString)
		{
			productIds.add(Long.parseLong(pid));
		}
			getSynchroMetaFieldManager().saveBrandField(name, productIds);
			SynchroUtils.triggerBrand();
			return true;
		}
		
	public Boolean updateBrand(final Integer id, final String name, final String ids)
		{
		//TODO
			MetaField metaField = new MetaField();
			metaField.setId(Long.parseLong(id.toString()));
			metaField.setName(name);
			getSynchroMetaFieldManager().updateBrandField(metaField, toArray(ids));
			SynchroUtils.triggerBrand();
			return true;
		}
	
	public Boolean deleteBrand(final Integer id)
	{
		getSynchroMetaFieldManager().deleteBrand(id);
		SynchroUtils.triggerBrand();
		return true;
	}
	
	public Boolean sortBrand(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortBrandField(orderMap);
		SynchroUtils.triggerBrand();
		return true;
	}
	
	/**
	 * Supplier Group
	 * @param name
	 * @param suppliers
	 */
	public Boolean addSupplierGroup(final String name, final String suppliers)
	{
		List<Long> supplierIds = new ArrayList<Long>();
		List<String> supplierIdsString = new ArrayList<String>();
		if(suppliers != null)
		{
			supplierIdsString = Arrays.asList(suppliers.split("\\s*,\\s*"));
		}
		for(String sid : supplierIdsString)
		{
			supplierIds.add(Long.parseLong(sid));
		}
		getSynchroMetaFieldManager().saveSupplierGroupField(name, supplierIds);
		SynchroUtils.triggerSuppliers();
		return true;
	}
	
	public Boolean updateSupplierGroup(final Integer id, final String name)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateSupplierGroupField(metaField);
		SynchroUtils.triggerSuppliers();
		return true;
	}
	
	/**
	 * Supplier
	 * @param name
	 * @param groups
	 */
	public Boolean addSupplier(final String name, final String groups)
	{
		List<Long> groupIds = new ArrayList<Long>();
		List<String> groupIdsString = new ArrayList<String>();
		if(groups != null)
		{
			groupIdsString = Arrays.asList(groups.split("\\s*,\\s*"));
		}
		for(String gid : groupIdsString)
		{
			groupIds.add(Long.parseLong(gid));
		}
		getSynchroMetaFieldManager().saveSupplierField(name, groupIds);
		SynchroUtils.triggerSuppliers();
		return true;
	}
	
	public Boolean updateSupplier(final Integer id, final String name)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateSupplierField(metaField);
		SynchroUtils.triggerSuppliers();
		return true;
	}
	
	/**
	 * Fieldwork Supplier Group
	 * @param name
	 * @param suppliers
	 */
	public Boolean addFwSupplierGroup(final String name, final String suppliers)
	{
		List<Long> supplierIds = new ArrayList<Long>();
		List<String> supplierIdsString = new ArrayList<String>();
		if(suppliers != null)
		{
			supplierIdsString = Arrays.asList(suppliers.split("\\s*,\\s*"));
		}
		for(String sid : supplierIdsString)
		{
			supplierIds.add(Long.parseLong(sid));
		}
		getSynchroMetaFieldManager().saveFwSupplierGroupField(name, supplierIds);
		SynchroUtils.triggerFwSuppliers();
		return true;
	}
	
	public Boolean updateFwSupplierGroup(final Integer id, final String name)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateFwSupplierGroupField(metaField);
		SynchroUtils.triggerFwSuppliers();
		return true;
	}
	
	/**
	 * Fieldworkd supplier
	 * @param name
	 * @param groups
	 */
	public Boolean addFwSupplier(final String name, final String groups)
	{
		List<Long> groupIds = new ArrayList<Long>();
		List<String> groupIdsString = new ArrayList<String>();
		if(groups != null)
		{
			groupIdsString = Arrays.asList(groups.split("\\s*,\\s*"));
		}
		for(String gid : groupIdsString)
		{
			groupIds.add(Long.parseLong(gid));
		}
		getSynchroMetaFieldManager().saveFwSupplierField(name, groupIds);
		SynchroUtils.triggerFwSuppliers();
		return true;
	}
	
	public Boolean updateFwSupplier(final Integer id, final String name)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateFwSupplierField(metaField);
		SynchroUtils.triggerFwSuppliers();
		return true;
	}
	
	/**
	 * Currency
	 * @param name
	 * @param description
	 */
	public Boolean addCurrency(final String name, final String description, final String globalCurrency)
	{
		getSynchroMetaFieldManager().saveCurrencyField(name, description, globalCurrency);
		SynchroUtils.triggerCurrency();
		return true;
	}
	
	public Boolean updateCurrency(final Integer id, final String name, final String description, final String globalCurrency)
	{
		Currency currency = new Currency();
		currency.setId(Long.parseLong(id.toString()));
		currency.setName(name);
		currency.setDescription(description);
		if(globalCurrency!=null)
		{
			if(globalCurrency.equals("1"))
			{
				currency.setGlobal(true);	
			}
			else
			{
				currency.setGlobal(false);
			}
		}
		getSynchroMetaFieldManager().updateCurrencyField(currency);
		SynchroUtils.triggerCurrency();
		return true;
	}
	
	public Boolean deleteCurrency(final Integer id)
	{
		getSynchroMetaFieldManager().deleteCurrencyField(id);
		SynchroUtils.triggerCurrency();
		return true;
	}
	
	public Boolean sortCurrency(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortCurrencyField(orderMap);
		SynchroUtils.triggerCurrency();
		return true;
	}
	
	/**
	 * Tendering Agency
	 * @param name
	 */
	public Boolean addTAgency(final String name)
	{
		getSynchroMetaFieldManager().saveTAgencyField(name);
		SynchroUtils.triggerTAgency();
		return true;
	}
	
	public Boolean updateTAgency(final Integer id, final String name)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateTAgencyField(metaField);
		SynchroUtils.triggerTAgency();
		return true;
	}
	

	/**
	 * Data Collections
	 */
	public Boolean addDataCollection(final String name, final String ids)
	{
		getSynchroMetaFieldManager().saveDataCollectionField(name, toArray(ids));
		SynchroUtils.triggerDataCollection();
		return true;
	}
	
	public Boolean updateDataCollection(final Integer id, final String name, final String ids)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateDataCollectionField(metaField, toArray(ids));
		SynchroUtils.triggerDataCollection();
		return true;
	}
	
	public Boolean deleteDataCollection(final Integer id)
	{
		getSynchroMetaFieldManager().deleteDataCollection(id);
		SynchroUtils.triggerDataCollection();
		return true;
	}
	
	public Boolean sortDataCollection(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortDataCollectionField(orderMap);
		SynchroUtils.triggerDataCollection();
		return true;
	}
	
	
	/**
	 *  Methodology Type
	 */
//	public Boolean addMethodologyType(final String name, final String ids)
//	{
//		getSynchroMetaFieldManager().saveMethodologyTypeField(name, toArray(ids));
//		SynchroUtils.triggerMethodologyType();
//		return true;
//	}

   /*
    *  Methodology Type
    */
    public Boolean addMethodologyType(final String name, final List<Long> collections, final List<Long> methodologies)
    {
        getSynchroMetaFieldManager().saveMethodologyTypeField(name, collections, methodologies);
        SynchroUtils.triggerMethodologyType();
        return true;
    }

    /*
    *  Methodology Type
    */
//    public Boolean addMethodologyType(final String name, final String collections, final String methodologies)
//    {
//        getSynchroMetaFieldManager().saveMethodologyTypeField(name, toArray(collections), toArray(methodologies));
//        SynchroUtils.triggerMethodologyType();
//        return true;
//    }

//	public Boolean updateMethodologyType(final Integer id, final String name, final String ids)
//	{
//		MetaField metaField = new MetaField();
//		metaField.setId(Long.parseLong(id.toString()));
//		metaField.setName(name);
//		getSynchroMetaFieldManager().updateMethodologyTypeField(metaField, toArray(ids));
//		SynchroUtils.triggerMethodologyType();
//		return true;
//	}

    public Boolean updateMethodologyType(final Integer id, final String name, final List<Long> collections, final List<Long> methodologies)
    {
        MetaField metaField = new MetaField();
        metaField.setId(Long.parseLong(id.toString()));
        metaField.setName(name);
        getSynchroMetaFieldManager().updateMethodologyTypeField(metaField, collections, methodologies);
        SynchroUtils.triggerMethodologyType();
        return true;
    }

//    public Boolean updateMethodologyType(final Integer id, final String name, final String collections, final String methodologies)
//    {
//        MetaField metaField = new MetaField();
//        metaField.setId(Long.parseLong(id.toString()));
//        metaField.setName(name);
//        getSynchroMetaFieldManager().updateMethodologyTypeField(metaField, toArray(collections), toArray(methodologies));
//        SynchroUtils.triggerMethodologyType();
//        return true;
//    }
	
	public Boolean deleteMethodologyType(final Integer id)
	{
		getSynchroMetaFieldManager().deleteMethodologyType(id);
		SynchroUtils.triggerMethodologyType();
		return true;
	}
	
	public Boolean sortMethodologyType(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortMethodologyTypeField(orderMap);
		SynchroUtils.triggerMethodologyType();
		return true;
	}	
	
	private List<Long> toArray(String str)
	{
		List<Long> list = new ArrayList<Long>();
		if(StringUtils.isNotBlank(str))
		{
			String[] array = str.split(",");
			for(int i=0; i < array.length; i++)
			{
				try{
				list.add(Long.parseLong(array[i]));
				}catch(Exception e){e.printStackTrace();}
			}
		}
		return list;
	}


	public Boolean updateDefaultValue(String prop, String value)
	{
		if(prop!=null && value!=null)
		{
			JiveGlobals.setJiveProperty(prop, value);		
		}	
		return true;
	}
	
	/**
	 * Region
	 * 
	 */
	public Boolean addRegion(final String name, final String ids)
	{
		List<Long> endmarketIDs  = new ArrayList<Long>();
		List<String> endmarketIDString = new ArrayList<String>();
		if(ids != null)
		{
			endmarketIDString = Arrays.asList(ids.split("\\s*,\\s*"));
		}
		for(String pid : endmarketIDString)
		{
			endmarketIDs.add(Long.parseLong(pid));
		}
		getSynchroMetaFieldManager().saveRegionField(name, endmarketIDs);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean updateRegion(final Integer id, final String name, final String ids)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateRegionField(metaField, toArray(ids));
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean deleteRegion(final Integer id)
	{
		getSynchroMetaFieldManager().deleteRegion(id);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean sortRegion(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortRegionField(orderMap);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	/**
	 * Area
	 * 
	 */
	public Boolean addArea(final String name, final String ids)
	{
		List<Long> endmarketIDs = new ArrayList<Long>();
		List<String> endmarketIDString = new ArrayList<String>();
		if(ids != null)
		{
			endmarketIDString = Arrays.asList(ids.split("\\s*,\\s*"));
		}
		for(String pid : endmarketIDString)
		{
			endmarketIDs.add(Long.parseLong(pid));
		}
		getSynchroMetaFieldManager().saveAreaField(name, endmarketIDs);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean updateArea(final Integer id, final String name, final String ids)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateAreaField(metaField, toArray(ids));
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean deleteArea(final Integer id)
	{
		getSynchroMetaFieldManager().deleteArea(id);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	public Boolean sortArea(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortAreaField(orderMap);
		SynchroUtils.triggerEndMarkets();
		return true;
	}
	
	/**
	 * Job Title
	 * 
	 */
	public Boolean addJobTitle(final String name)
	{
		getSynchroMetaFieldManager().saveJobTitle(name);
		SynchroUtils.triggerJobTitles();
		return true;
	}
	
	public Boolean updateJobTitle(final Integer id, final String name)
	{
		MetaField metaField = new MetaField();
		metaField.setId(Long.parseLong(id.toString()));
		metaField.setName(name);
		getSynchroMetaFieldManager().updateJobTitle(metaField);
		SynchroUtils.triggerJobTitles();
		return true;
	}
	
	public Boolean deleteJobTitle(final Integer id)
	{
		getSynchroMetaFieldManager().deleteJobTitles(id);
		SynchroUtils.triggerJobTitles();
		return true;
	}
	
	public Boolean sortJobTitle(final Map<Integer, Integer> orderMap)
	{
		getSynchroMetaFieldManager().sortJobTitles(orderMap);
		SynchroUtils.triggerJobTitles();
		return true;
	}
	
	public Boolean setCountryCurrencyMapping(final Integer countryid, final Integer currencyid)
	{
		if(countryid>0 && currencyid>0)
		{
			getSynchroMetaFieldManager().setCountryCurrencyMapping(countryid, currencyid);
			SynchroUtils.triggerCurrency();
		}
		
		return true;
	}

    public Boolean addKantarButtonMethodologyType(final String name) {
        getSynchroMetaFieldManager().setKantarButtonMethodologyType(name);
        KantarUtils.triggerKantarButtonMethodologyTypes();
        return true;
    }

    public Boolean deleteKantarButtonMethodologyType(final Long id) {
        getSynchroMetaFieldManager().deleteKantarButtonMethodologyType(id);
        KantarUtils.triggerKantarButtonMethodologyTypes();
        return true;
    }

    public Boolean updateKantarButtonMethodologyType(final Long id, final String name) {
        MetaField metaField = new MetaField();
        metaField.setId(id);
        metaField.setName(name);
        getSynchroMetaFieldManager().updateKantarButtonMethodologyType(metaField);
        KantarUtils.triggerKantarButtonMethodologyTypes();
        return true;
    }

    public Boolean sortKantarButtonMethodologyTypes(final Map<Long, Integer> orderMap) {
        getSynchroMetaFieldManager().sortKantarButtonMethodologyTypes(orderMap);
        KantarUtils.triggerKantarButtonMethodologyTypes();
        return true;
    }

    public Boolean addGrailButtonMethodologyType(final String name) {
        getSynchroMetaFieldManager().setGrailButtonMethodologyType(name);
        GrailUtils.triggerGrailButtonMethodologyTypes();
        return true;
    }

    public Boolean deleteGrailButtonMethodologyType(final Long id) {
        getSynchroMetaFieldManager().deleteGrailButtonMethodologyType(id);
        GrailUtils.triggerGrailButtonMethodologyTypes();
        return true;
    }

    public Boolean updateGrailButtonMethodologyType(final Long id, final String name) {
        MetaField metaField = new MetaField();
        metaField.setId(id);
        metaField.setName(name);
        getSynchroMetaFieldManager().updateGrailButtonMethodologyType(metaField);
        GrailUtils.triggerGrailButtonMethodologyTypes();
        return true;
    }

    public Boolean sortGrailButtonMethodologyTypes(final Map<Long, Integer> orderMap) {
        getSynchroMetaFieldManager().sortGrailButtonMethodologyTypes(orderMap);
        GrailUtils.triggerGrailButtonMethodologyTypes();
        return true;
    }

    public Boolean addDocumentRepositoryReportType(final String name) {
        getSynchroMetaFieldManager().setKantarReportType(name, false);
        KantarUtils.triggerKantarReportTypes();
        return true;
    }

    public Boolean getDocumentRepositoryReportType(final String name) {
        MetaField metaField = getSynchroMetaFieldManager().getKantarReportType(name);
        return metaField != null?true:false;
    }

    public Boolean deleteDocumentRepositoryReportType(final Long id) {
        getSynchroMetaFieldManager().deleteKantarReportType(id);
        KantarUtils.triggerKantarReportTypes();
        return true;
    }

    public Boolean updateDocumentRepositoryReportType(final Long id, final String name) {
        getSynchroMetaFieldManager().updateKantarReportType(id, name);
        KantarUtils.triggerKantarReportTypes();
        return true;
    }

    public Boolean sortDocumentRepositoryReportTypes(final Map<Long, Integer> orderMap) {
        getSynchroMetaFieldManager().sortKantarReportTypes(orderMap);
        KantarUtils.triggerKantarReportTypes();
        return true;
    }

    
    /**
     * Grail Phase 5
     * @author kanwardeep.grewal
     */
    
    /**
	 * T20/T40
	 * 
	 */
		public Boolean addT20T40(final String name, final String ids)
		{
			List<Long> endmarketIDs = new ArrayList<Long>();
			List<String> endmarketIDString = new ArrayList<String>();
			if(ids != null)
			{
				endmarketIDString = Arrays.asList(ids.split("\\s*,\\s*"));
			}
			for(String pid : endmarketIDString)
			{
				endmarketIDs.add(Long.parseLong(pid));
			}
			getSynchroMetaFieldManager().saveT20T40Field(name, endmarketIDs);
			SynchroUtils.triggerEndMarkets();
			return true;
		}
		
		public Boolean updateT20T40(final Integer id, final String name, final String ids)
		{
			MetaField metaField = new MetaField();
			metaField.setId(Long.parseLong(id.toString()));
			metaField.setName(name);
			getSynchroMetaFieldManager().updateT20T40Field(metaField, toArray(ids));
			SynchroUtils.triggerEndMarkets();
			return true;
		}
		
		public Boolean deleteT20T40(final Integer id)
		{
			getSynchroMetaFieldManager().deleteT20T40(id);
			SynchroUtils.triggerEndMarkets();
			return true;
		}
		
		public Boolean sortT20T40(final Map<Integer, Integer> orderMap)
		{
			getSynchroMetaFieldManager().sortT20T40Field(orderMap);
			SynchroUtils.triggerEndMarkets();
			return true;
		}

		
		/** Synchro Phase 5
		 * grailResearchAgency Group
		 * @param name
		 * @param suppliers
		 */
		public Boolean addGrailResearchAgencyGroup(final String name, final String agencies)
		{
			List<Long> agencyIds = new ArrayList<Long>();
			List<String> agencyIdsString = new ArrayList<String>();
			if(agencies != null)
			{
				agencyIdsString = Arrays.asList(agencies.split("\\s*,\\s*"));
			}
			for(String sid : agencyIdsString)
			{
				agencyIds.add(Long.parseLong(sid));
			}
			getSynchroMetaFieldManager().saveGrailResearchAgencyGroupField(name, agencyIds);
			SynchroUtils.triggerGrailResearchAgency();
			return true;
		}
		
		
		public Boolean updateGrailResearchAgencyGroup(final Integer id, final String name, final String agencies)
		{
			List<Long> agencyIds = new ArrayList<Long>();
			List<String> agencyIdsString = new ArrayList<String>();
			if(agencies != null)
			{
				agencyIdsString = Arrays.asList(agencies.split("\\s*,\\s*"));
			}
			for(String sid : agencyIdsString)
			{
				agencyIds.add(Long.parseLong(sid));
			}
			MetaField metaField = new MetaField();
			metaField.setId(Long.parseLong(id.toString()));
			metaField.setName(name);
			getSynchroMetaFieldManager().updateGrailResearchAgencyGroupField(metaField, agencyIds);
			SynchroUtils.triggerGrailResearchAgency();
			return true;
		}
		
		/**
		 * grailResearchAgency
		 * @param name
		 * @param groups
		 */
		public Boolean addGrailResearchAgency(final String name, final String groups)
		{
			List<Long> groupIds = new ArrayList<Long>();
			List<String> groupIdsString = new ArrayList<String>();
			if(groups != null)
			{
				groupIdsString = Arrays.asList(groups.split("\\s*,\\s*"));
			}
			for(String gid : groupIdsString)
			{
				groupIds.add(Long.parseLong(gid));
			}
			getSynchroMetaFieldManager().saveGrailResearchAgencyField(name, groupIds);
			SynchroUtils.triggerGrailResearchAgency();
			return true;
		}
		
		public Boolean updateGrailResearchAgency(final Integer id, final String name, final String groups)
		{
			List<Long> groupIds = new ArrayList<Long>();
			List<String> groupIdsString = new ArrayList<String>();
			if(groups != null)
			{
				groupIdsString = Arrays.asList(groups.split("\\s*,\\s*"));
			}
			for(String gid : groupIdsString)
			{
				groupIds.add(Long.parseLong(gid));
			}
			MetaField metaField = new MetaField();
			metaField.setId(Long.parseLong(id.toString()));
			metaField.setName(name);
			getSynchroMetaFieldManager().updateGrailResearchAgencyField(metaField, groupIds);
			SynchroUtils.triggerGrailResearchAgency();
			return true;
		}
		
		public Boolean sortGrailResearchAgencyField(final Map<Integer, Integer> orderMap)
		{
			getSynchroMetaFieldManager().sortGrailResearchAgencyField(orderMap);
			SynchroUtils.triggerGrailResearchAgency();
			return true;
		}
		
		public Boolean deleteResearchAgency(final Integer id)
		{
			getSynchroMetaFieldManager().deleteResearchAgency(id);
			SynchroUtils.triggerGrailResearchAgency();
			return true;
		}
		
		public Boolean deleteResearchAgencyGroup(final Integer id)
		{
			getSynchroMetaFieldManager().deleteResearchAgencyGroup(id);
			SynchroUtils.triggerGrailResearchAgency();
			return true;
		}
}
