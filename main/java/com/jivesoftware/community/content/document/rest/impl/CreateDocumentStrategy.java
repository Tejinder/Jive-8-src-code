/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.document.rest.impl;

import com.google.common.collect.ImmutableMap;
import com.grail.osp.manager.OSPManager;
import com.jivesoftware.base.User;
import com.jivesoftware.base.aaa.AuthenticationProvider;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.BinaryBodyManager;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.DocumentType;
import com.jivesoftware.community.DocumentTypeManager;
import com.jivesoftware.community.DuplicateIDException;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.content.action.IngressFilter;
import com.jivesoftware.community.content.document.rest.beans.CreateDocumentServiceBean;
import com.jivesoftware.community.content.publish.PublishResponse;
import com.jivesoftware.community.content.publish.PublishStrategy;
import com.jivesoftware.community.content.publish.collaboration.CollaborationStrategy;
import com.jivesoftware.community.content.publish.context.PublishContext;
import com.jivesoftware.community.document.BinaryDocumentHelper;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.incoming.impl.AttachmentData;
import com.jivesoftware.community.quest.QuestManager;
import com.jivesoftware.community.renderer.impl.v2.JAXPUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

@Component
public class CreateDocumentStrategy implements PublishStrategy<Document> {

    private static final Logger log = Logger.getLogger(CreateDocumentStrategy.class);

    @Autowired
    private AttachmentHelper attachmentHelper;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private DocumentTypeManager documentTypeManager;

    @Autowired
    private RemoteSupport remoteSupport;

    @Autowired
    private BinaryBodyManager binaryBodyManager;

    @Autowired
    private DocumentStrategyHelper documentStrategyHelper;

    @Autowired
    private DocumentCollaborationStrategy documentCollaborationStrategy;

    @Autowired
    private IngressFilter ingressFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private BinaryDocumentHelper binaryDocumentHelper;
    
    private OSPManager ospManager;
    
    // constants for Grail
    public static final String GRAIL_COUNTRY = "grail.country";
    public static final String GRAIL_BRAND = "grail.brand";
    public static final String GRAIL_METHODOLOGY = "grail.methodology";
    public static final String GRAIL_MONTH = "grail.month";
    public static final String GRAIL_YEAR = "grail.year";
    public static final String GRAIL_PERIOD_LONG = "grail.period.long";


    @Override
    public Document create(PublishContext context, PublishResponse.Builder responseBuilder) {
        documentStrategyHelper.validateContext(context, responseBuilder);
        if (!responseBuilder.isValid()) {
            return null;
        }

        CreateDocumentServiceBean bean = context.getBean();

        JiveContainer container = context.getJiveContainer();

        if (bean.isDraft() && bean.isBinary()) {
            if (binaryDocumentHelper.isBinaryDocumentDraftDisabled(container)) {
                log.warn(MessageFormat.format("create called for binary-document and with Draft state but drafts aren''t allowed for give container ({0}/{1}). rejecting the request.", container.getObjectType(), container.getID()));
                responseBuilder.addError("doc.create.binary_draft_disabled");
                return null;
            }
        }

        Document document = null;
        try {
            // check if title already exist
            try {
                if (documentManager.getDocument(container, bean.getSubject()) != null) {
                    throw new DocumentAlreadyExistsException();
                }
            }
            catch (DocumentObjectNotFoundException e) {
                // if throws not-found - it is ok
            }

            User user = authenticationProvider.getJiveUser();
            DocumentType documentType = documentTypeManager.getDocumentType(1);
            document = documentManager.createDocument(user, documentType, null, bean.getSubject(), bean.getBody(), container);

            if (bean.isBinary()) {
                document.setSummary(JAXPUtils.toXmlString(ingressFilter.filter(bean.getBody(), document,
                        bean.isMobileEditor(), !bean.isMobileEditor(), false)));
                document.setSubject(bean.getSubject());

                applyBinaryBody(context, responseBuilder, bean, document);
                if (!responseBuilder.isValid()) {
                    return null;
                }
            }
            else {
                document.setSubject(bean.getSubject());
                document.setBody(ingressFilter.filter(bean.getBody(), document, bean.isMobileEditor(), !bean.isMobileEditor(), false));
            }

            // if this is from a quest we need to set the property
            if (bean.getFromQuest() != null && !bean.getFromQuest().isEmpty()) {
                document.getProperties().put(QuestManager.FROM_QUEST_KEY, bean.getFromQuest());
            }
            
         // GRAIL CHANGES START
            if (bean.getCountry() != null && !bean.getCountry().isEmpty()) {
                document.getProperties().put(GRAIL_COUNTRY, convertListToCSV(bean.getCountry()).trim());
            }
            if (bean.getBrand() != null && !bean.getBrand().isEmpty()) {
                document.getProperties().put(GRAIL_BRAND, convertListToCSV(bean.getBrand()).trim());
            }
            if (bean.getMethodology() != null && !bean.getMethodology().isEmpty()) {
                document.getProperties().put(GRAIL_METHODOLOGY, convertListToCSV(bean.getMethodology()).trim());
            }
            if (bean.getPeriodMonth() != null && !bean.getPeriodMonth().isEmpty()) {
                document.getProperties().put(GRAIL_MONTH, getMonth(bean.getPeriodMonth()));
            }
            if (bean.getPeriodYear() != null && !bean.getPeriodYear().isEmpty()) {
                document.getProperties().put(GRAIL_YEAR, bean.getPeriodYear());
            }
            System.out.println("Checking Country ---"+ bean.getCountry());
            
           
            
            documentStrategyHelper.applyDraftState(document, context.getApprovers() !=null, bean.isDraft());

            documentManager.addDocument(document, ImmutableMap.of());
            
            if(ospManager==null)
            {
            	ospManager = JiveApplication.getContext().getSpringBean("ospManager");
            }
            
           // ospManager.saveIRISDoc(document.getID(), bean.getStudyOverview(), bean.getRelatedStudies(), bean.getSynchroCode());
        }
        catch (DocumentAlreadyExistsException dae) {
            responseBuilder.addError("doc.create.error_title_unique");
        }
        catch (DuplicateIDException e) {
            responseBuilder.addError("doc.create.cldNotCreateDoc.text");
        }

        return document;
    }

    @Override
    public void after(Document document, PublishContext context, PublishResponse.Builder responseBuilder) {

        document.save(false, true);
    }

    @Override
    public CollaborationStrategy lookupCollaborationStrategy(PublishContext context) {
        return documentCollaborationStrategy;
    }



    private void applyBinaryBody(
            PublishContext context, PublishResponse.Builder responseBuilder,
            CreateDocumentServiceBean bean, Document document) {
        List<AttachmentData> attachments = context.getAttachments();
        Assert.isTrue(attachments.size() == 1,
                "Wrong number of binary attachments found for the multipart for document " + bean.getSubject());
        AttachmentData info = attachments.get(0);

        try {
            attachmentHelper
                    .setBinaryBody(document, info.getFilename(), info.getContentType(),
                            info.getInputStream());

            context.getAttachments().clear();
        }
        catch (AttachmentException e) {
            String key = attachmentHelper.getAttachmentExceptionKey(e);
            responseBuilder.addError(remoteSupport.getText(key, new String[]{e.getAttachmentName()}));
        }
        catch (BinaryBodyException e) {
            String key = documentStrategyHelper.getBinaryBodyExceptionKey(e);
            responseBuilder.addError(key);
        }
        catch (IOException e) {
            log.error(String.format("Unexpected error saving a document %s", bean.getSubject()), e);
            responseBuilder.addError("err.could_not_save_doc.text");
        }
        finally {
            IOUtils.closeQuietly(info.getInputStream());
        }
    }
    
    /**
     * Convert a List to a Comma Seperated Value string
     *
     * @param stringList
     * @return String
     */
    private String convertListToCSV(List<String> stringList) {
        StringBuilder sb = new StringBuilder();
        int listSize = stringList.size();
        for (int i = 0; i < listSize; i++) {
            if (i < (listSize - 1)) {
                sb.append(stringList.get(i)).append(",");
            } else {
                sb.append(stringList.get(i));
            }
        }
        return sb.toString().trim();
    }
    
    /**
     * Returns the month corresponding to the given month value in int
     * @param month
     * @return
     */
    private String getMonth(String month) {
        
        String monthValue = "";
    	if(month.equals("January")) {
    		monthValue="0";
        }else if(month.equals("February")){
        	monthValue="1";
        }else if(month.equals("March")){
        	monthValue="2";
        }else if(month.equals("April")){
        	monthValue="3";
        }else if(month.equals("May")){
        	monthValue="4";
        }else if(month.equals("June")){
        	monthValue="5";
        }else if(month.equals("July")){
        	monthValue="6";
        }else if(month.equals("August")){
        	monthValue="7";
        }else if(month.equals("September")){
        	monthValue="8";
        }else if(month.equals("October")){
        	monthValue="9";
        }else if(month.equals("November")){
        	monthValue="10";
        }else if(month.equals("December")){
        	monthValue="11";
        }else {
        	monthValue="NA";
        }
        return monthValue;
    }

	public OSPManager getOspManager() {
		return ospManager;
	}

	public void setOspManager(OSPManager ospManager) {
		this.ospManager = ospManager;
	}
 }
