/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.document.action;

import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.action.util.SoyActionUtil;
import com.jivesoftware.community.content.action.ContentActionHelper;
import com.jivesoftware.community.content.action.beans.JiveActionSupportAdapterBean;
import com.jivesoftware.community.content.document.action.beans.EditDocumentActionBean;
import com.jivesoftware.community.web.JiveResourceResolver;
import com.jivesoftware.community.web.soy.action.AbstractSoyModelDrivenAction;
import com.jivesoftware.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditDocumentAction extends AbstractSoyModelDrivenAction {

    private long documentID;

    @Autowired
    private ContentActionHelper contentActionHelper;

    @Autowired
    private DocumentActionHelper documentActionHelper;

    private Document document;

    private String fromQuest;
    private int questStep;
    
    // constants for Grail
    public static final String GRAIL_COUNTRY = "grail.country";
    public static final String GRAIL_BRAND = "grail.brand";
    public static final String GRAIL_METHODOLOGY = "grail.methodology";
    public static final String GRAIL_MONTH = "grail.month";
    public static final String GRAIL_YEAR = "grail.year";
    public static final String GRAIL_PERIOD_LONG = "grail.period.long";

    // optional query param
    public void setFromQ(String fromQuest) {
        this.fromQuest = fromQuest;
    }

    // optional query param
    public void setQstep(int questStep) {
        this.questStep = questStep;
    }
    
    
    @Override
    public String input() {
        try {
            document = documentActionHelper.loadDocument(documentID);
        }
        catch (DocumentObjectNotFoundException dnfe) {
            return NOTFOUND;
        } catch (UnauthorizedException unauth) {
            return UNAUTHORIZED;
        }

        User user = contentActionHelper.getUser();

        // don't let multiple users to edit the document
        if (document.isDocumentBeingEdited() && !document.isDocumentBeingEditedByUser(user)) {
            return "editing";
        }


        if (documentActionHelper.isAddEditWarningMessage(document, user)) {
            addActionMessage(getText("doc.create.edit_draft_warn.info",
                    new String[]{document.getUser().getUsername()}));
        }

        JiveActionSupportAdapterBean actionAdapterBean =
                JiveActionSupportAdapterBean.createForJiveActionAndContainerAware(this, document);

        EditDocumentActionBean bean = new EditDocumentActionBean();
        bean.setDocumentID(documentID);
        contentActionHelper.populateForJiveAction(bean, actionAdapterBean, JiveConstants.DOCUMENT, false);
        contentActionHelper.populateTokenInfo(bean, "document.edit");
        contentActionHelper.populateAttachments(bean, document);
        contentActionHelper.fixupUserContainerDisplayName(
                bean, "ctr.choose.myctr.document.header", "doc.create.users_document.title");
        contentActionHelper.populateForJiveObject(bean, document);
        contentActionHelper.populateAppContext(bean, getCoreAPIType(), document);
        try {
            documentActionHelper.startEdit(document);
        }
        catch (IllegalStateException iae) {
            return DocumentActionHelper.EDITING;
        }
        documentActionHelper.prepareContentResource(bean, document);
        documentActionHelper.populateCommentBean(bean,document);
        bean.setBody(documentActionHelper.getBody(document));
        bean.setSubject(document.getPlainSubject());
        bean.setDocumentID(documentID);
        bean.setDraftDisabled(document.isDraftDisabled());
        bean.setFromQuest(fromQuest);
        bean.setQstep(questStep);
        bean.setEditedVersion(document.getVersionID());
        bean.setExtendedAuthors(hasExtendedAuthors(document));
        
        /* GRAIL CHANGES START*/
        
        if (document.getProperties().get(GRAIL_COUNTRY) != null)
        {
        	bean.setCountry(document.getProperties().get(GRAIL_COUNTRY));
        }
        else
        {
        	bean.setCountry("");
        }
        if (document.getProperties().get(GRAIL_BRAND) != null)
        {
        	bean.setBrand(document.getProperties().get(GRAIL_BRAND));
        }
        else
        {
        	bean.setBrand("");
        }
        if (document.getProperties().get(GRAIL_METHODOLOGY) != null)
        {
        	bean.setMethodology(document.getProperties().get(GRAIL_METHODOLOGY));
        }
        else
        {
        	bean.setMethodology("");
        }
        if (document.getProperties().get(GRAIL_MONTH) != null)
        {
        	bean.setMonth(document.getProperties().get(GRAIL_MONTH));
        }
        else
        {
        	bean.setMonth("");
        }
        if (document.getProperties().get(GRAIL_YEAR) != null)
        {
        	bean.setYear(document.getProperties().get(GRAIL_YEAR));
        }
        else
        {
        	bean.setYear("");
        }
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

        SoyActionUtil.mapActionErrors(this, model);

        return INPUT;
    }

    private boolean hasExtendedAuthors(Document document) {
        return contentActionHelper.hasExtendedAuthors(document);
    }

    public void setID(long documentID) {
        this.documentID = documentID;
    }

    public String getDocumentURL() {
        return JiveResourceResolver.getJiveObjectURL(document, true);
    }

    public String getCoreAPIType() {
        return "osapi.jive.core.Document";
    }

    public JiveContainer getContainer() {
        Document document;
        try {
            document = documentActionHelper.loadDocument(documentID);
            return document.getJiveContainer();
        } catch (NotFoundException nfe) {
            log.warn(nfe.getMessage(), nfe);
        }
        return super.getContainer();
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
