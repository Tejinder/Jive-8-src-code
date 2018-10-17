/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.document.action;


import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.content.action.ContentActionHelper;
import com.jivesoftware.community.content.action.beans.BaseContentActionBean;
import com.jivesoftware.community.content.action.beans.JiveActionSupportAdapterBean;
import com.jivesoftware.community.web.soy.action.AbstractSoyModelDrivenAction;
import com.jivesoftware.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.jivesoftware.community.JiveGlobals;

public class CreateDocumentAction extends AbstractSoyModelDrivenAction {

    @Autowired
    private ContentActionHelper contentActionHelper;

    @Autowired
    private DocumentActionHelper documentActionHelper;

    private String fromQuest;
    private int questStep;
    private String defaultVisibility;
    private String tags;
    private String subject;
    
    

    // optional query param
    public void setFromQ(String fromQuest) {
        this.fromQuest = fromQuest;
    }

    // optional query param
    public void setQstep(int questStep) {
        this.questStep = questStep;
    }

    public void setQVis(String defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    @Override
    public String input() {
        JiveActionSupportAdapterBean actionAdapterBean = JiveActionSupportAdapterBean.createForJiveAction(this);

        if (contentActionHelper.isNeedsContainer(actionAdapterBean)) {
            return UNAUTHENTICATED;
        }

        BaseContentActionBean bean = new BaseContentActionBean();
        bean.setSubject(subject);
        bean.setFromQuest(fromQuest);
        bean.setQstep(questStep);
        bean.setDefaultVisibility(defaultVisibility);

        contentActionHelper.populateForJiveAction(bean, actionAdapterBean, JiveConstants.DOCUMENT, true);
        contentActionHelper.populateTokenInfo(bean, "document.create");
        contentActionHelper.populateForContentType(bean, JiveConstants.DOCUMENT, StringUtils.replace(tags, " ", ", "));
        contentActionHelper.populateAppContext(bean, getCoreAPIType(), null);
        documentActionHelper.prepareContentResource(bean, null);
       
        /* GRAIL CHANGES START*/
        List<String> countries = null;
        if(JiveGlobals.getJiveProperty("grail.countryList")!=null)
        {
	       countries = convertCSVToList(JiveGlobals.getJiveProperty("grail.countryList"));
	    }
        else
        {
        	countries = new ArrayList<String>();
        	 countries.add("NA");
        }
        bean.setCountryList(countries);
        List<String> brands = null;
        if(JiveGlobals.getJiveProperty("grail.brandList")!=null)
        {
           brands = convertCSVToList(JiveGlobals.getJiveProperty("grail.brandList"));
	       
        }
        else
        {
        	brands = new ArrayList<String>();
        	brands.add("NA");
        }
        bean.setBrandList(brands);
        
        List<String> methodologies = null;
        if(JiveGlobals.getJiveProperty("grail.methodologyList")!=null)
        {
        	methodologies = convertCSVToList(JiveGlobals.getJiveProperty("grail.methodologyList"));
	       
        }
        else
        {
        	methodologies = new ArrayList<String>();
        	methodologies.add("NA");
        }
        bean.setMethodologyList(methodologies);
        
        List<String> periodMonth = null;
        if(JiveGlobals.getJiveProperty("grail.periodMonth")!=null)
        {
        	periodMonth = convertCSVToList(JiveGlobals.getJiveProperty("grail.periodMonth"));
	       
        }
        else
        {
        	periodMonth = new ArrayList<String>();
        	periodMonth.add("NA");
        }
        bean.setPeriodMonth(periodMonth);
        
        List<String> periodYear = null;
        if(JiveGlobals.getJiveProperty("grail.periodMonth")!=null)
        {
        	periodYear = convertCSVToList(JiveGlobals.getJiveProperty("grail.periodYear"));
	       
        }
        else
        {
        	periodYear = new ArrayList<String>();
        	periodYear.add("NA");
        }
        bean.setPeriodYear(periodYear);
        
        Map<String, Object> model = new HashMap<>();
        model.put("bean", bean);
        this.model = model;

        return INPUT;
    }

    public String getCoreAPIType() {
        return "osapi.jive.core.Document";
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    /**
     * Convert a CSV string values to a List<String>
     *
     * @param csvString
     * @return List<String>
     */
    private List<String> convertCSVToList(String csvString) {
        String[] stripComma = StringUtils.split(csvString, ',');
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < stripComma.length; i++) {
            result.add(stripComma[i]);
        }
        return result;
    }
}
