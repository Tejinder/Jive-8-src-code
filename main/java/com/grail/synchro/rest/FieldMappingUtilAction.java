package com.grail.synchro.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.manager.SynchroMetaFieldManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * RESTful Action for fetching product field mapping
 *
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class FieldMappingUtilAction extends RemoteSupport  {

    private static final Logger LOG = Logger.getLogger(FieldMappingUtilAction.class);
    private static SynchroMetaFieldManager synchroMetaFieldManager;
    
    public static SynchroMetaFieldManager getSynchroMetaFieldManager() {
        if(synchroMetaFieldManager==null)
        {
            return JiveApplication.getContext().getSpringBean("synchroMetaFieldManager");
        }
        return synchroMetaFieldManager;

    }
	public List<Integer> getCategoryBrandMapping(final List<Integer> list)
	{
		
		List<Integer> brands = new ArrayList<Integer>();
		List<Integer> mapping = new ArrayList<Integer>();
		 Map<Integer, Object[]> categoryBrandMapping = SynchroGlobal.getProductBrandMapping();
		 for(Integer categoryID : list)
		 {
			 Object[] objList = categoryBrandMapping.get(categoryID);
			 for(Object obj : objList)
			 {
				 if(!mapping.contains(obj))
				 {
					 mapping.add((Integer)obj);
				 }
				 
			 }
		 }
		 
		 Map<Integer, String> allBrands = SynchroGlobal.getBrands();
		 for(Integer key : allBrands.keySet())
		 {
			 if(mapping.contains(key))
			 {
				 brands.add(key);
			 }
		 }
		 
		return brands;
	}
	
	public List<Integer> getMethodlogyMapping(final Integer id, final Boolean fetchAll)
	{
		List<Integer> methodologies = new ArrayList<Integer>();
		if(fetchAll)
		{
			Map<Integer, String> allMethodologies = SynchroGlobal.getMethodologies();
			for(Integer mid: allMethodologies.keySet())
			{
				methodologies.add(mid);
			}
			return methodologies;
		}
		
		if(SynchroGlobal.getMethodologyMapping().containsKey(id))
		{
			Map<Integer, String> selectedMethodologies = SynchroGlobal.getMethodologyMapping().get(id); 
			for(Integer sid : selectedMethodologies.keySet())
			{
				methodologies.add(sid);
			}
			return methodologies;		
		}
		
		return methodologies;
	}
	
	public Map<Integer, String> getMethodlogyNames(final Boolean fetchAll)
	{		
		return SynchroGlobal.getMethodologies();
	}
	
	
	public Map<Long, String> getAreaByEndMarkets(final List<Long> eids)
	{
		Map<Long, String> areaMap = new LinkedHashMap<Long, String>();
		List<MetaField> areas = getSynchroMetaFieldManager().getAreaByEndMarkets(eids);
		for(MetaField field : areas)
		{
			areaMap.put(field.getId(), field.getName());
		}
		return areaMap;
	}
	
	public Map<Long, String> getRegionByEndMarkets(final List<Long> eids)
	{
		Map<Long, String> regionMap = new LinkedHashMap<Long, String>();
		List<MetaField> regions = getSynchroMetaFieldManager().getRegionsByEndMarkets(eids);
		for(MetaField field : regions)
		{
			regionMap.put(field.getId(), field.getName());
		}
		return regionMap;
	}
	
	public List<Integer> getDataCollections(final Boolean fetchAll, final Long id)
	{
		List<Integer> collections = new ArrayList<Integer>();
		if(fetchAll)
		{
			Map<Integer, String> allCollections = SynchroGlobal.getDataCollections();
			for(Integer mid: allCollections.keySet())
			{
				collections.add(mid);
			}
			return collections;
		}
		
		if(SynchroGlobal.getCollectionMapping().containsKey(id.intValue()))
		{
			Map<Integer, String> selectedCollections = SynchroGlobal.getCollectionMapping().get(id.intValue()); 
			for(Integer sid : selectedCollections.keySet())
			{
				collections.add(sid);
			}
			return collections;		
		}
		
		return collections;
	}
	
	public List<Integer> getProposedMethodology(final Boolean fetchAll, final Long id)
	{
		List<Integer> collections = new ArrayList<Integer>();
		if(fetchAll)
		{
			Map<Integer, String> allCollections = SynchroGlobal.getMethodologies();
			for(Integer mid: allCollections.keySet())
			{
				collections.add(mid);
			}
			return collections;
		}
		
		if(SynchroGlobal.getMethodologyMapping().containsKey(id.intValue()))
		{
			Map<Integer, String> selectedMethodologies = SynchroGlobal.getMethodologyMapping().get(id.intValue()); 
			for(Integer mid : selectedMethodologies.keySet())
			{
				collections.add(mid);
			}
			return collections;		
		}
		
		return collections;
	}
	
	public Map<Integer, String> getCollectionNames(final Boolean fetchAll)
	{		
		return SynchroGlobal.getDataCollections();
	}
	
	public Integer getDefaultBrandID(final Boolean fetchAll)
	{		
		return JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_BRAND_ID, 1);
	}
	
	public Integer getDefaultProposedMethodologyID(final Boolean fetchAll)
	{		
		return JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_PROPOSEDMETHOGOLOGY_ID, -1);
	}

	public Integer getRegionByEndMarket(final Long eID)
	{
		Map<Integer, Map<Integer, String>> mapping = SynchroGlobal.getRegionEndMarketsMapping();
		Integer endmarketRegionID = -1;
		for(Integer regionID : mapping.keySet())
		{
			Map<Integer, String> endmarkets = mapping.get(regionID);
			if(endmarkets.containsKey(eID))
			{
				endmarketRegionID =  regionID;
				break;
			}
		}
		return endmarketRegionID;
	}
	
	public Integer getAreaByEndMarket(final Long eID)
	{
		Map<Integer, Map<Integer, String>> mapping = SynchroGlobal.getAreaEndMarketsMapping();
		Integer endmarketAreaID = -1;
		for(Integer areaID : mapping.keySet())
		{
			Map<Integer, String> endmarkets = mapping.get(areaID);
			if(endmarkets.containsKey(eID))
			{
				endmarketAreaID =  areaID;
				break;
			}
		}
		return endmarketAreaID;
	}
	
	public Long getMethodologyTypeByMethodology(final String methodology)
	{		
		try
		{
			if(methodology!=null && !methodology.equals(""))
			{
				return SynchroGlobal.getMethodologyTypeByProposedMethodology(new Long(methodology));
			}
		}
		catch(Exception e)
		{
		//	e.printStackTrace();
		}
		return new Long("-1");
	}
	

}
