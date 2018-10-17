/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.search.action;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jivesoftware.base.event.SearchEvent;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.BinaryBody;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.ContainerAwareEntityDescriptor;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.eae.impl.FilterBean;
import com.jivesoftware.community.integration.tile.Tile;
import com.jivesoftware.community.integration.tile.TileResultFilter;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.objecttype.CommentableType;
import com.jivesoftware.community.objecttype.JiveObjectType;
import com.jivesoftware.community.objecttype.ReplyableType;
import com.jivesoftware.community.outcome.OutcomeFeatureConfiguration;
import com.jivesoftware.community.outcome.OutcomeSupportedType;
import com.jivesoftware.community.outcome.OutcomeType;
import com.jivesoftware.community.search.opensearch.OpenSearchEngineImpl;
import com.jivesoftware.community.search.opensearch.SearchEngine;
import com.jivesoftware.community.search.user.ProfileSearchResult;
import com.jivesoftware.community.solution.annotations.InjectConfiguration;
import com.jivesoftware.community.web.soy.SoyModelDriven;
import com.jivesoftware.service.client.JiveSearchTextExtractionService;
import com.jivesoftware.util.LocaleUtils;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jivesoftware.community.impl.DocumentContentType;
import com.jivesoftware.community.impl.search.provider.DocumentIndexInfoProvider;
import com.jivesoftware.community.impl.search.provider.util.IndexInfoProviderHelper;
import com.jivesoftware.community.search.IndexField;
import com.jivesoftware.community.search.IndexInfo;
import com.jivesoftware.community.*;
import java.util.Date;
import java.util.Calendar;

public class SoySearchAction extends SearchAction implements SoyModelDriven {

    protected static final Logger log = LogManager.getLogger(SoySearchAction.class.getName());

    private List<String> placeFacets = null;
    private List<String> contentFacets = null;
    private List<String> timeRanges = null;
    private List<String> sortOptions = null;
    private List<String> outcomeTypes = null;
    private List<String> depthFacets = null;
    List<Document> documentsList = new ArrayList<Document>();

    private Comparator<OutcomeType> outcomeSearchFilterComparator;

	
    private OutcomeFeatureConfiguration outcomeFeatureConfiguration;
    private JiveSearchTextExtractionService jiveSearchTextExtractionService;
    private IndexInfoProviderHelper helper;
    
    private DocumentIndexInfoProvider documentIndexInfoProvider;
    private AttachmentManager attachmentManager;
    
    @Required
    public void setOutcomeSearchFilterComparator(Comparator<OutcomeType> outcomeSearchFilterComparator) {
        this.outcomeSearchFilterComparator = outcomeSearchFilterComparator;
    }

    @InjectConfiguration
    public void setOutcomeFeatureConfiguration(OutcomeFeatureConfiguration outcomeFeatureConfiguration) {
        this.outcomeFeatureConfiguration = outcomeFeatureConfiguration;
    }
    @Required
    public void setIndexInfoProviderHelper(IndexInfoProviderHelper helper) {
        this.helper = helper;
    }
    public String execute() {
        if (!JiveGlobals.getJiveBooleanProperty("search.enabled", true)) {
            addActionMessage(getText("search.err.func_disabled.text"));
            return ERROR;
        }
        if (!loadJiveObjects()) {
            return ERROR;
        }
        else if(advanceSearch!=null && !advanceSearch.equalsIgnoreCase(""))
        {
        	if(all_fields!=null && !all_fields.equalsIgnoreCase(""))
        	{
        		Date maxDate = new Date();
				Calendar cal = Calendar.getInstance();
        		cal.set(Calendar.YEAR, 2009);
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                long startTime = cal.getTimeInMillis();
                Date minDate = new Date(startTime);
                
                long minDocId = documentIndexInfoProvider.getMinID(minDate);
                long maxDocId = documentIndexInfoProvider.getMaxID(maxDate);
                
                List<IndexInfo> indexInfoList = documentIndexInfoProvider.getContent(minDocId, maxDocId, minDate, maxDate);
                List<Long> docIdIndexList = Lists.newLinkedList();
                List<Long> docIdList = Lists.newLinkedList();
                String[] allFieldsSplit = all_fields.split(" ");
                if(indexInfoList!=null && indexInfoList.size()>0)
                {
                	for(IndexInfo indexInfo: indexInfoList)
                	{
                		String containAllFields = "";
                		for(int i=0;i<allFieldsSplit.length;i++)
                		{
                			if(indexInfo.getIndexContent(true).get(IndexField.body).toString().toLowerCase().contains(allFieldsSplit[i].toLowerCase()))
                    		{
                    			//Long docId = new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"");
                    			//docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                    			
                    		}
                			else
                			{
                				containAllFields = "no";
                			}
                		}
                		if(!containAllFields.contains("no"))
                		{
                			docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                		}
                		containAllFields = "";
                		for(int i=0;i<allFieldsSplit.length;i++)
                		{
                			if(indexInfo.getIndexContent(true).get(IndexField.subject).toString().toLowerCase().contains(allFieldsSplit[i].toLowerCase()))
                    		{
                    			//Long docId = new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"");
                    			//docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                    			
                    		}
                			else
                			{
                				containAllFields = "no";
                			}
                		}
                		if(!containAllFields.contains("no"))
                		{
                			docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                		}
                		
                	}
                	docIdList.clear();
                	if(docIdIndexList!=null && docIdIndexList.size()>0)
                	{
                		//docIdList.clear();
                		docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdIndexList)));
                	}
                }
        		
                if(docIdList!=null && docIdList.size()>0)
            	{
            		documentsList = documentManager.getDocuments(docIdList);
            	}
         
            	return "quick-search";
        	
    			
        	
        	}
        	else if(any_fields!=null && !any_fields.equalsIgnoreCase(""))
        	{

        		Date maxDate = new Date();
				Calendar cal = Calendar.getInstance();
        		cal.set(Calendar.YEAR, 2009);
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                long startTime = cal.getTimeInMillis();
                Date minDate = new Date(startTime);
                
                long minDocId = documentIndexInfoProvider.getMinID(minDate);
                long maxDocId = documentIndexInfoProvider.getMaxID(maxDate);
                
                List<IndexInfo> indexInfoList = documentIndexInfoProvider.getContent(minDocId, maxDocId, minDate, maxDate);
                List<Long> docIdIndexList = Lists.newLinkedList();
                List<Long> docIdList = Lists.newLinkedList();
                String[] anyFieldsSplit = any_fields.split(" ");
                if(indexInfoList!=null && indexInfoList.size()>0)
                {
                	for(IndexInfo indexInfo: indexInfoList)
                	{
                		for(int i=0;i<anyFieldsSplit.length;i++)
                		{
                			if(indexInfo.getIndexContent(true).get(IndexField.body).toString().toLowerCase().contains(anyFieldsSplit[i].toLowerCase()))
                    		{
                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                    			
                    		}
                			
                		}
                		for(int i=0;i<anyFieldsSplit.length;i++)
                		{
                			if(indexInfo.getIndexContent(true).get(IndexField.subject).toString().toLowerCase().contains(anyFieldsSplit[i].toLowerCase()))
                    		{
                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                    			
                    		}
                			
                		}
                		
                	}
                	docIdList.clear();
                	if(docIdIndexList!=null && docIdIndexList.size()>0)
                	{
                		//docIdList.clear();
                		docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdIndexList)));
                	}
                }
        		
                if(docIdList!=null && docIdList.size()>0)
            	{
            		documentsList = documentManager.getDocuments(docIdList);
            	}
         
            	return "quick-search";
        	}
        	else if(exact_fields!=null && !exact_fields.equalsIgnoreCase(""))
        	{


        		Date maxDate = new Date();
				Calendar cal = Calendar.getInstance();
        		cal.set(Calendar.YEAR, 2009);
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                long startTime = cal.getTimeInMillis();
                Date minDate = new Date(startTime);
                
                long minDocId = documentIndexInfoProvider.getMinID(minDate);
                long maxDocId = documentIndexInfoProvider.getMaxID(maxDate);
                
                List<IndexInfo> indexInfoList = documentIndexInfoProvider.getContent(minDocId, maxDocId, minDate, maxDate);
                List<Long> docIdIndexList = Lists.newLinkedList();
                List<Long> docIdList = Lists.newLinkedList();
               
                if(indexInfoList!=null && indexInfoList.size()>0)
                {
                	for(IndexInfo indexInfo: indexInfoList)
                	{
            			if(indexInfo.getIndexContent(true).get(IndexField.body).toString().toLowerCase().contains(exact_fields.toLowerCase()))
                		{
            				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                			
                		}
            			
           
            			if(indexInfo.getIndexContent(true).get(IndexField.subject).toString().toLowerCase().contains(exact_fields.toLowerCase()))
                		{
            				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                			
                		}
                                		
                	}
                	docIdList.clear();
                	if(docIdIndexList!=null && docIdIndexList.size()>0)
                	{
                		//docIdList.clear();
                		docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdIndexList)));
                	}
                }
        		
                if(docIdList!=null && docIdList.size()>0)
            	{
            		documentsList = documentManager.getDocuments(docIdList);
            	}
         
            	return "quick-search";
        	
        	}
        	else if(none_fields!=null && !none_fields.equalsIgnoreCase(""))
        	{

        		Date maxDate = new Date();
				Calendar cal = Calendar.getInstance();
        		cal.set(Calendar.YEAR, 2009);
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                long startTime = cal.getTimeInMillis();
                Date minDate = new Date(startTime);
                
                long minDocId = documentIndexInfoProvider.getMinID(minDate);
                long maxDocId = documentIndexInfoProvider.getMaxID(maxDate);
                
                List<IndexInfo> indexInfoList = documentIndexInfoProvider.getContent(minDocId, maxDocId, minDate, maxDate);
                List<Long> docIdIndexList = Lists.newLinkedList();
                List<Long> docIdList = Lists.newLinkedList();
                String[] none_fieldSplit = none_fields.split(" ");
                if(indexInfoList!=null && indexInfoList.size()>0)
                {
                	for(IndexInfo indexInfo: indexInfoList)
                	{
                		String containNoneFields = "";
                		for(int i=0;i<none_fieldSplit.length;i++)
                		{
                			if(indexInfo.getIndexContent(true).get(IndexField.body).toString().toLowerCase().contains(none_fieldSplit[i].toLowerCase()))
                    		{
                    			//Long docId = new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"");
                    			//docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                				containNoneFields="yes";
                    		}
                			
                		}
                		if(!containNoneFields.contains("yes"))
                		{
                			docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                		}
                		containNoneFields = "";
                		for(int i=0;i<none_fieldSplit.length;i++)
                		{
                			if(indexInfo.getIndexContent(true).get(IndexField.subject).toString().toLowerCase().contains(none_fieldSplit[i].toLowerCase()))
                    		{
                    			//Long docId = new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"");
                    			//docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                				containNoneFields="yes";
                    		}
                			
                		}
                		if(!containNoneFields.contains("yes"))
                		{
                			docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                		}
                		
                	}
                	docIdList.clear();
                	if(docIdIndexList!=null && docIdIndexList.size()>0)
                	{
                		//docIdList.clear();
                		docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdIndexList)));
                	}
                }
        		
                if(docIdList!=null && docIdList.size()>0)
            	{
            		documentsList = documentManager.getDocuments(docIdList);
            	}
         
            	return "quick-search";
        	
    			
        	
        	
        	}
        	else if(tag_fields!=null && !tag_fields.equalsIgnoreCase(""))
        	{

        		Date maxDate = new Date();
				Calendar cal = Calendar.getInstance();
        		cal.set(Calendar.YEAR, 2009);
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                long startTime = cal.getTimeInMillis();
                Date minDate = new Date(startTime);
                
                long minDocId = documentIndexInfoProvider.getMinID(minDate);
                long maxDocId = documentIndexInfoProvider.getMaxID(maxDate);
                
                List<IndexInfo> indexInfoList = documentIndexInfoProvider.getContent(minDocId, maxDocId, minDate, maxDate);
                List<Long> docIdIndexList = Lists.newLinkedList();
                List<Long> docIdList = Lists.newLinkedList();
                String[] tag_FieldSplit = tag_fields.split(" ");
                if(indexInfoList!=null && indexInfoList.size()>0)
                {
                	for(IndexInfo indexInfo: indexInfoList)
                	{
                		for(int i=0;i<tag_FieldSplit.length;i++)
                		{
                			if(indexInfo.getIndexContent(true).get(IndexField.tags).toString().toLowerCase().contains(tag_FieldSplit[i].toLowerCase()))
                    		{
                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                    			
                    		}
                			
                		}
                		
                		
                	}
                	docIdList.clear();
                	if(docIdIndexList!=null && docIdIndexList.size()>0)
                	{
                		//docIdList.clear();
                		docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdIndexList)));
                	}
                }
        		
                if(docIdList!=null && docIdList.size()>0)
            	{
            		documentsList = documentManager.getDocuments(docIdList);
            	}
         
            	return "quick-search";
        	}
        	else if(mrts!=null && !mrts.equalsIgnoreCase(""))
        	{

        		Date maxDate = new Date();
				Calendar cal = Calendar.getInstance();
        		cal.set(Calendar.YEAR, 2009);
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                long startTime = cal.getTimeInMillis();
                Date minDate = new Date(startTime);
                
                long minDocId = documentIndexInfoProvider.getMinID(minDate);
                long maxDocId = documentIndexInfoProvider.getMaxID(maxDate);
                
                List<IndexInfo> indexInfoList = documentIndexInfoProvider.getContent(minDocId, maxDocId, minDate, maxDate);
                List<Long> docIdIndexList = Lists.newLinkedList();
                List<Long> docIdList = Lists.newLinkedList();
               
                if(indexInfoList!=null && indexInfoList.size()>0)
                {
                	for(IndexInfo indexInfo: indexInfoList)
                	{
            			if(indexInfo.getIndexContent(true).get(IndexField.body).toString().toLowerCase().contains("mrts") && indexInfo.getIndexContent(true).get(IndexField.body).toString().toLowerCase().contains(mrts.toLowerCase()))
                		{
            				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                			
                		}
                                		
                	}
                	docIdList.clear();
                	if(docIdIndexList!=null && docIdIndexList.size()>0)
                	{
                		//docIdList.clear();
                		docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdIndexList)));
                	}
                }
        		
                if(docIdList!=null && docIdList.size()>0)
            	{
            		documentsList = documentManager.getDocuments(docIdList);
            	}
         
            	return "quick-search";
        	
        	
        	}
        	
        }
        	
        else if(applyExtendedPropertyFilters) { // Error check the query string
        	DocumentResultFilter documentFilter = new DocumentResultFilter();
        	
        	
        	List<DocumentResultFilter> docFilterList = new ArrayList<DocumentResultFilter>();
        	List<Long> docIdList = Lists.newLinkedList();
        	if(container == null) {
                container = communityManager.getRootCommunity();
            }
        	boolean isPropSelected = false;
        	if( docPeriod != null && docPeriod.size() == 4 && !docPeriod.get(0).toString().equalsIgnoreCase("NA")
                    && !docPeriod.get(1).toString().equalsIgnoreCase("NA") && !docPeriod.get(2).toString().equalsIgnoreCase("NA") && !docPeriod.get(3).toString().equalsIgnoreCase("NA"))
        	{
        		//documentFilter.addProperty(GRAIL_PERIOD_PROP, docPeriod.get(0).toString()+"~"+docPeriod.get(1).toString()+"~"+docPeriod.get(2).toString()+"~"+docPeriod.get(3).toString());
        		isPropSelected = true;
        		int startMonth = Integer.parseInt(docPeriod.get(0).toString());
        		int startYear = Integer.parseInt(docPeriod.get(1).toString());
        		int endMonth = Integer.parseInt(docPeriod.get(2).toString());
        		int endYear = Integer.parseInt(docPeriod.get(3).toString());
        		
        		
        		
        		if(startYear==endYear)
        		{
        			if(startMonth==endMonth)
        			{
        				documentFilter = new DocumentResultFilter();
        				documentFilter.addProperty(GRAIL_MONTH, docPeriod.get(0).toString(),ResultFilter.CONTAINS_MATCH);
                		documentFilter.addProperty(GRAIL_YEAR, docPeriod.get(1).toString(),ResultFilter.CONTAINS_MATCH);
                		if( docBrand != null && docBrand.size() > 0 && !docBrand.get(0).equalsIgnoreCase("NA"))
        	        	{
        	        		documentFilter.addProperty(GRAIL_BRAND_PROP, docBrand.get(0).toString(),ResultFilter.CONTAINS_MATCH);
        	        	}
        	        	if( docRegion != null && docRegion.size() > 0 && !docRegion.get(0).equalsIgnoreCase("NA"))
        	        	{
        	        		documentFilter.addProperty(GRAIL_COUNTRY_PROP, docRegion.get(0).toString(),ResultFilter.CONTAINS_MATCH);
        	        	}
        	        	if( docMethodology != null && docMethodology.size() > 0 && !docMethodology.get(0).equalsIgnoreCase("NA"))
        	        	{
        	        		documentFilter.addProperty(GRAIL_METHODOLOGY_PROP, docMethodology.get(0).toString(),ResultFilter.CONTAINS_MATCH);
        	        	}
                		documentFilter.setFromQuickSearchWidget(true);
                    	documentFilter.setRestrictToLatestVersion(true);
                    	documentFilter.addDocumentState(DocumentState.PUBLISHED);
                		docFilterList.add(documentFilter);
        			}
        			for(int i=startMonth;i<=endMonth;i++)
        			{
        				documentFilter = new DocumentResultFilter();
        				documentFilter.addProperty(GRAIL_MONTH, i+"",ResultFilter.CONTAINS_MATCH);
                		documentFilter.addProperty(GRAIL_YEAR, docPeriod.get(1).toString(),ResultFilter.CONTAINS_MATCH);
                		if( docBrand != null && docBrand.size() > 0 && !docBrand.get(0).equalsIgnoreCase("NA"))
        	        	{
        	        		documentFilter.addProperty(GRAIL_BRAND_PROP, docBrand.get(0).toString(),ResultFilter.CONTAINS_MATCH);
        	        	}
        	        	if( docRegion != null && docRegion.size() > 0 && !docRegion.get(0).equalsIgnoreCase("NA"))
        	        	{
        	        		documentFilter.addProperty(GRAIL_COUNTRY_PROP, docRegion.get(0).toString(),ResultFilter.CONTAINS_MATCH);
        	        	}
        	        	if( docMethodology != null && docMethodology.size() > 0 && !docMethodology.get(0).equalsIgnoreCase("NA"))
        	        	{
        	        		documentFilter.addProperty(GRAIL_METHODOLOGY_PROP, docMethodology.get(0).toString(),ResultFilter.CONTAINS_MATCH);
        	        	}
                		documentFilter.setFromQuickSearchWidget(true);
                    	documentFilter.setRestrictToLatestVersion(true);
                    	documentFilter.addDocumentState(DocumentState.PUBLISHED);
                		docFilterList.add(documentFilter);
        			}
        		}
        		else
        		{
        			int yearDiff = endYear - startYear;
                    int currentYear = startYear;
                   // conditions = Lists.newLinkedList();
                    for(int yInc = 0; yInc <= yearDiff; yInc++) {
                        int yr = currentYear + yInc;
                        for(int mInc = 0; mInc < 12; mInc++ ) {
                            if((yr == startYear && mInc < startMonth)
                                    || (yr == endYear && mInc > endMonth)) {
                                continue;
                            } else {
                            	documentFilter = new DocumentResultFilter();
                				documentFilter.addProperty(GRAIL_MONTH, mInc+"",ResultFilter.CONTAINS_MATCH);
                        		documentFilter.addProperty(GRAIL_YEAR, yr+"",ResultFilter.CONTAINS_MATCH);
                        		if( docBrand != null && docBrand.size() > 0 && !docBrand.get(0).equalsIgnoreCase("NA"))
                	        	{
                	        		documentFilter.addProperty(GRAIL_BRAND_PROP, docBrand.get(0).toString(),ResultFilter.CONTAINS_MATCH);
                	        	}
                	        	if( docRegion != null && docRegion.size() > 0 && !docRegion.get(0).equalsIgnoreCase("NA"))
                	        	{
                	        		documentFilter.addProperty(GRAIL_COUNTRY_PROP, docRegion.get(0).toString(),ResultFilter.CONTAINS_MATCH);
                	        	}
                	        	if( docMethodology != null && docMethodology.size() > 0 && !docMethodology.get(0).equalsIgnoreCase("NA"))
                	        	{
                	        		documentFilter.addProperty(GRAIL_METHODOLOGY_PROP, docMethodology.get(0).toString(),ResultFilter.CONTAINS_MATCH);
                	        	}
                        		documentFilter.setFromQuickSearchWidget(true);
                            	documentFilter.setRestrictToLatestVersion(true);
                            	documentFilter.addDocumentState(DocumentState.PUBLISHED);
                        		docFilterList.add(documentFilter);
                        		
                            	
                            }
                        }
                    }
        			
        		}
        				
        		
        		/*documentFilter.addProperty(GRAIL_YEAR, docPeriod.get(1).toString(),ResultFilter.CONTAINS_MATCH);
        		documentFilter.addProperty(GRAIL_YEAR, docPeriod.get(3).toString(),ResultFilter.CONTAINS_MATCH);
        		
        		
        		documentFilter.addProperty(GRAIL_YEAR, docPeriod.get(1).toString(),ResultFilter.CONTAINS_MATCH);
        		documentFilter.addProperty(GRAIL_YEAR, docPeriod.get(3).toString(),ResultFilter.CONTAINS_MATCH);
        		*/
        		
        		//documentFilter.setPropertyMode(ResultFilter.OR_MODE);
        		//documentFilter.setPropertyMode(ResultFilter.ASCENDING);
        		if(docFilterList!=null && docFilterList.size()>0)
            	{
            		List<Long> docIdListDulplicate = Lists.newLinkedList();
            		for(DocumentResultFilter docFltr : docFilterList)
            		{
            			Iterable<ContainerAwareEntityDescriptor> iterator = documentManager.getDocumentsAsContainerAwareEntityDescriptors(container, docFltr);
            			for (ContainerAwareEntityDescriptor doc : iterator) 
            			{
            				docIdListDulplicate.add(doc.getID());
       	             	}
            		}
            		docIdList = Lists.newLinkedList(Sets.newLinkedHashSet(docIdListDulplicate));
            	}
        	}
        	else
        	{
	        	
        		if( docBrand != null && docBrand.size() > 0 && !docBrand.get(0).equalsIgnoreCase("NA"))
	        	{
	        		documentFilter.addProperty(GRAIL_BRAND_PROP, docBrand.get(0).toString(),ResultFilter.CONTAINS_MATCH);
	        		isPropSelected = true;
	        	}
	        	if( docRegion != null && docRegion.size() > 0 && !docRegion.get(0).equalsIgnoreCase("NA"))
	        	{
	        		documentFilter.addProperty(GRAIL_COUNTRY_PROP, docRegion.get(0).toString(),ResultFilter.CONTAINS_MATCH);
	        		isPropSelected = true;
	        	}
	        	if( docMethodology != null && docMethodology.size() > 0 && !docMethodology.get(0).equalsIgnoreCase("NA"))
	        	{
	        		documentFilter.addProperty(GRAIL_METHODOLOGY_PROP, docMethodology.get(0).toString(),ResultFilter.CONTAINS_MATCH);
	        		isPropSelected = true;
	        	}
	        	if(isPropSelected)
	        	{
	        		documentFilter.setFromQuickSearchWidget(true);
	            	documentFilter.setRestrictToLatestVersion(true);
	            	documentFilter.addDocumentState(DocumentState.PUBLISHED);
	            	Iterable<ContainerAwareEntityDescriptor> iterator = documentManager.getDocumentsAsContainerAwareEntityDescriptors(container, documentFilter);
		        	
		        	 
		             for (ContainerAwareEntityDescriptor doc : iterator) {
		          	   docIdList.add(doc.getID());
		             }
	        	}
	        	
        	}
        	
        	//documentFilter.addProperty(GRAIL_COUNTRY_PROP, "Poland");
        	
        	
        	
        	
        	
        	/*
        	
        	if(docFilterList!=null && docFilterList.size()>0)
        	{
        		List<Long> docIdListDulplicate = Lists.newLinkedList();
        		for(DocumentResultFilter docFltr : docFilterList)
        		{
        			Iterable<ContainerAwareEntityDescriptor> iterator = documentManager.getDocumentsAsContainerAwareEntityDescriptors(container, docFltr);
        			for (ContainerAwareEntityDescriptor doc : iterator) 
        			{
        				docIdListDulplicate.add(doc.getID());
   	             	}
        		}
        		docIdList = Lists.newLinkedList(Sets.newLinkedHashSet(docIdListDulplicate));
        	}
        	else
        	{
        	
	        	Iterable<ContainerAwareEntityDescriptor> iterator = documentManager.getDocumentsAsContainerAwareEntityDescriptors(container, documentFilter);
	        	
	        	 
	             for (ContainerAwareEntityDescriptor doc : iterator) {
	          	   docIdList.add(doc.getID());
	             }
        	}
            */
        	/**
        	 * Search in Body Text
        	 */
        	if( searchParam != null && !searchParam.trim().equalsIgnoreCase(""))
        	{
        		
        		Date maxDate = new Date();
				Calendar cal = Calendar.getInstance();
        		cal.set(Calendar.YEAR, 2009);
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                long startTime = cal.getTimeInMillis();
                Date minDate = new Date(startTime);
                
                long minDocId = documentIndexInfoProvider.getMinID(minDate);
                long maxDocId = documentIndexInfoProvider.getMaxID(maxDate);
                
                List<IndexInfo> indexInfoList = documentIndexInfoProvider.getContent(minDocId, maxDocId, minDate, maxDate);
                List<Long> docIdIndexList = Lists.newLinkedList();
                
                if(indexInfoList!=null && indexInfoList.size()>0)
                {
                	for(IndexInfo indexInfo: indexInfoList)
                	{
                		if(indexInfo.getIndexContent(true).get(IndexField.body).toString().toLowerCase().contains(searchParam.toString().toLowerCase().replaceAll("\"", "")))
                		{
                			Long docId = new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"");
                			System.out.println("Doc --"+ docId);
                			System.out.println("Doc Index --"+ indexInfo.getIndexContent(true).get(IndexField.objectID)+"");
                			
                			if(isPropSelected)
                			{
	                			if(docIdList.contains(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"")))
	                			{
	                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
	                			}
                			}
                			else
                			{
                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                			}
                		}
                	
                		if(indexInfo.getIndexContent(true).get(IndexField.subject).toString().toLowerCase().contains(searchParam.toString().toLowerCase().replaceAll("\"", "")))
                		{
                			if(isPropSelected)
                			{
	                			if(docIdList.contains(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"")))
	                			{
	                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
	                			}
                			}
                			else
                			{
                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                			}
                		}
                		if(indexInfo.getIndexContent(true).get(IndexField.tags).toString().contains(searchParam.toString().replaceAll("\"", "")))
                		{
                			if(isPropSelected)
                			{
	                			if(docIdList.contains(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+"")))
	                			{
	                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
	                			}
                			}
                			else
                			{
                				docIdIndexList.add(new Long(indexInfo.getIndexContent(true).get(IndexField.objectID)+""));
                			}
                		}
                	/*	if(indexInfo.getIndexContent(false).get(IndexField.body).toString().contains(searchParam.get(0).toString()))
                		{
                			docIdIndexList.add(new Long(indexInfo.getIndexContent(false).get(IndexField.objectID)+""));
                		}
                	
                		if(indexInfo.getIndexContent(false).get(IndexField.subject).toString().contains(searchParam.get(0).toString()))
                		{
                			docIdIndexList.add(new Long(indexInfo.getIndexContent(false).get(IndexField.objectID)+""));
                		}*/
                	}
                	docIdList.clear();
                	if(docIdIndexList!=null && docIdIndexList.size()>0)
                	{
                		//docIdList.clear();
                		docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdIndexList)));
                	}
                }
        		
        		
        	/*	documentFilter = new DocumentResultFilter();
            	documentFilter.setRestrictToLatestVersion(true);
            	documentFilter.setSearchBodyParam(searchParam.get(0).toString());
            	documentFilter.setSearchBody(true);
        		List<Long> docIdListDulplicate = Lists.newLinkedList();
            	Iterable<ContainerAwareEntityDescriptor> iterator = documentManager.getDocumentsAsContainerAwareEntityDescriptors(container, documentFilter);
    			for (ContainerAwareEntityDescriptor doc : iterator) 
    			{
    				docIdListDulplicate.add(doc.getID());
    				 //loadBinaryBody(doc.getID());
    				
    				
    				IndexInfo indexInfo = documentIndexInfoProvider.getContent(doc.getID());
    				Map<IndexField, String> m1 = indexInfo.getIndexContent(true);
    				Map<IndexField, String> m2 = indexInfo.getIndexContent(false);
    				try
    				{
	    				Iterable<Attachment> attachments = attachmentManager.getAttachments(documentManager.getDocument(doc.getID()));
	    				for(Attachment att : attachments)
	    				{
	    					String name = att.getName();
	    					System.out.print("Name --"+ name);
	    				}
    				}
    				catch(Exception e)
    				{
    					e.printStackTrace();
    				}
                }
    			docIdList.addAll(Lists.newLinkedList(Sets.newLinkedHashSet(docIdListDulplicate)));
    			*/
    			
        	}
        	if(docIdList!=null && docIdList.size()>0)
        	{
        		documentsList = documentManager.getDocuments(docIdList);
        	}
     
        	return "quick-search";
        } 
        // Error check the query string
        else if (StringUtils.isBlank(q)) {

            addActionError(getText("search.noCriteria.text"));
            return INPUT;
        }

        return SUCCESS;
    }
    private String loadBinaryBody(long internalDocID) {
        BinaryBody binaryBody = null;
        Document document = null;
        try {
            document = documentManager.getDocument(internalDocID);
            binaryBody = document.getBinaryBody();
        }
        catch (DocumentObjectNotFoundException e) {
            log.debug("Couldn't find binary body for document " + internalDocID);
        }

        if (binaryBody == null) {
            return "";
        }

       

        EntityDescriptor entity = new EntityDescriptor(binaryBody);
        try {
            InputStream extractedBodyText = jiveSearchTextExtractionService.getEntityExtractedText(entity,
            		document.getModificationDate().getTime());

            return helper.readExtractedTextStream(entity, extractedBodyText);
        }
        catch (Throwable e) {
            log.warn("Error extracting text from binary body for entity " + entity, e);
            return "";
        }
    }
    public Object getModel() {
        SearchBean bean = new SearchBean();
        bean.setContentFacets(getContentFacets());
        bean.setCorrectedQuery(getCorrectedQ());
        bean.setPlaceFacets(getPlaceFacets());
        bean.setPlaceTypes(getPlaceTypes());
        bean.setDepthFacets(getDepthFacets());
        bean.setExternalActivityFacets(getExternalActivityFacets());
        bean.setQuery(getQ());
        bean.setSelectedContentFacet("all");
        bean.setSelectedPlaceFacet("all");
        bean.setSelectedTimeRange("all");
        bean.setSelectedOutcomeType(SearchBean.ALL_OUTCOMES_FILTER);
        bean.setSortOptions(getSortOptions());
        bean.setMultipleLanguageSearchEnabled(searchSettingsManager.isMultipleLanguageSearchEnabled());
        bean.setAllowedLanguages(getAllowedSearchLanguageBeans());
        bean.setDefaultLanguage(getDefaultLanguageBean());
        bean.setTimeRanges(getTimeRanges());
        bean.setView(getView());
        bean.setSearchEngines(prepareSearchEngineBeans(JiveApplication.getContext().getSearchEngineManager().getSearchEngines()));
        bean.setStats(JiveApplication.getContext().getSearchEngineManager().getStats());
        bean.setRootCommunityName(JiveApplication.getContext().getCommunityManager().getRootCommunity().getName());
        bean.setAllowAnonResultView(JiveGlobals.getJiveBooleanProperty("opensearch.allowAnonResultView", false));
        bean.setShowOpenSearch((!getUser().isAnonymous() ||
                JiveGlobals.getJiveBooleanProperty("opensearch.allowAnonResultView", false)));

        if (outcomeFeatureConfiguration.isOutcomesEnabled()) {
            bean.setOutcomeTypes(getOutcomeTypes());
        }

        bean.setSocialSearch(searchSettingsManager.isSocialSearchAvailable());

        bean.setOrgChartingEnabled(userRelationshipManager.isOrgChartingEnabled());

        return bean;
    }

    private Collection<SearchEngine> prepareSearchEngineBeans(Collection<SearchEngine> engines) {
        List<SearchEngine> preparedSearchEngines = new ArrayList<>();
        for (SearchEngine searchEngine : engines) {
            OpenSearchEngineImpl updatedEngine = (OpenSearchEngineImpl) searchEngine;
            updatedEngine.setExternalSearchURL(
                    StringUtils.replace(searchEngine.getExternalSearchURL(),
                    "javascript:",
                    ""
                    ));
            preparedSearchEngines.add(updatedEngine);
        }
        return preparedSearchEngines;
    }

    private List<FilterBean> getExternalActivityFacets() {
        TileResultFilter trf = new TileResultFilter();
        trf.setActivityTiles(true);
        Iterable<Tile> tiles = tileManager.get(trf);

        List<FilterBean> externalActivityFacets = new ArrayList<FilterBean>();
        for (final Tile tile : tiles) {
            externalActivityFacets.add(new FilterBean(tile.getDisplayName(), "/extstreams/" + tile.getID(), false) {
                @Override
                public String getDisplayName() {
                    String displayName = tile.getDisplayName();
                    if (LocaleUtils.isI18nKeyMatch(displayName)) {
                        displayName = StringUtils.defaultString(tileManager.getI18nResources(tile, getLocale()).get(displayName), displayName);
                    }
                    return displayName;
                }
            });
        }
        return externalActivityFacets;
    }

    private List<String> getPlaceFacets() {
        if (placeFacets == null) {
            placeFacets = new ArrayList<>();
            placeFacets.add("all");
            placeFacets.add("space");
            placeFacets.add("project");
            placeFacets.add("group");
        }
        return placeFacets;
    }

    private List<String> getContentFacets() {
        if (contentFacets == null) {
            contentFacets = new ArrayList<>();
            contentFacets.add("all");
            for (String s : searchActionHelper.getContentTypes()) {
                int index = s.indexOf(':');
                String type = index > 0 ? s.substring(0, index) : s;
                String name = index > 0 ? s.substring(index + 1) : s;
                // the string used for checking if a content type is enabled differs from its use in the UI / URL
                JiveObjectType jot = objectTypeManager.getObjectType(type);
                if (jot != null && jot.isEnabled()) {
                    contentFacets.add(name);
                }
            }
        }
        return contentFacets;
    }

    private List<String> getTimeRanges() {
        if (timeRanges == null) {
            timeRanges = new ArrayList<>();
            timeRanges.add("all");
            timeRanges.add("day");
            timeRanges.add("week");
            timeRanges.add("month");
            timeRanges.add("quarter");
            timeRanges.add("year");
        }
        return timeRanges;
    }

    private List<String> getSortOptions() {
        if (sortOptions == null) {
            sortOptions = new ArrayList<>();
            sortOptions.add("relevanceDesc");
            sortOptions.add("updatedDesc");
        }
        return sortOptions;
    }

    private List<String> getOutcomeTypes() {
        if (outcomeTypes == null && outcomeFeatureConfiguration.isOutcomesEnabled()) {
            List<Integer> objectTypes = Lists.newArrayList(
                    JiveConstants.THREAD,
                    JiveConstants.BLOGPOST,
                    JiveConstants.DOCUMENT);
            Set<OutcomeType> outcomeTypesSet = Sets.newTreeSet(outcomeSearchFilterComparator);
            for (Integer objectType : objectTypes) {
                JiveObjectType jot = objectTypeManager.getObjectType(objectType);
                if (jot instanceof OutcomeSupportedType) {
                    for (OutcomeType outcomeType : ((OutcomeSupportedType) jot).getOutcomeTypeProvider().getOutcomeTypes()) {
                        outcomeTypesSet.add(outcomeType);
                    }
                    if (jot instanceof CommentableType) {
                        for (OutcomeType outcomeType :
                                ((OutcomeSupportedType) objectTypeManager.getObjectType(JiveConstants.COMMENT)).getOutcomeTypeProvider().getOutcomeTypes())
                        {
                            outcomeTypesSet.add(outcomeType);
                        }
                    }
                    else if (jot instanceof ReplyableType) {
                        for (OutcomeType outcomeType :
                                ((OutcomeSupportedType) objectTypeManager.getObjectType(JiveConstants.MESSAGE)).getOutcomeTypeProvider().getOutcomeTypes())
                        {
                            outcomeTypesSet.add(outcomeType);
                        }
                    }
                }
            }
            outcomeTypes = Lists.newArrayList(Collections2.transform(outcomeTypesSet, new Function<OutcomeType, String>() {
                @Nullable
                @Override
                public String apply(@Nullable OutcomeType input) {
                    if (input == null) {
                        return null;
                    }
                    else {
                        return input.getName().toLowerCase();
                    }
                }
            }));
        }
        return outcomeTypes;
    }

    private List<String> getDepthFacets() {
        if (depthFacets == null) {
            depthFacets = new ArrayList<>();
            depthFacets.add("none");
            depthFacets.add("children");
            depthFacets.add("all");
        }
        return depthFacets;
    }

	public List<Document> getDocumentsList() {
		return documentsList;
	}

	public void setDocumentsList(List<Document> documentsList) {
		this.documentsList = documentsList;
	}

	public JiveSearchTextExtractionService getJiveSearchTextExtractionService() {
		return jiveSearchTextExtractionService;
	}

	public void setJiveSearchTextExtractionService(
			JiveSearchTextExtractionService jiveSearchTextExtractionService) {
		this.jiveSearchTextExtractionService = jiveSearchTextExtractionService;
	}

	public DocumentIndexInfoProvider getDocumentIndexInfoProvider() {
		return documentIndexInfoProvider;
	}

	public void setDocumentIndexInfoProvider(
			DocumentIndexInfoProvider documentIndexInfoProvider) {
		this.documentIndexInfoProvider = documentIndexInfoProvider;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
}
