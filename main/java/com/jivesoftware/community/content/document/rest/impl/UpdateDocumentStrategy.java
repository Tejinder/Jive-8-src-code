/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.content.document.rest.impl;

import com.jivesoftware.base.User;
import com.jivesoftware.base.aaa.AuthenticationProvider;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.BinaryBodyException;
import com.jivesoftware.community.BinaryBodyManager;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentAlreadyExistsException;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.action.DocCreationVersionErrorAction;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.content.BodyCacheManager;
import com.jivesoftware.community.content.action.IngressFilter;
import com.jivesoftware.community.content.document.impl.TempDocumentStore;
import com.jivesoftware.community.content.document.rest.beans.UpdateDocumentServiceBean;
import com.jivesoftware.community.content.publish.PublishResponse;
import com.jivesoftware.community.content.publish.PublishStrategy;
import com.jivesoftware.community.content.publish.collaboration.CollaborationStrategy;
import com.jivesoftware.community.content.publish.context.PublishContext;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.impl.DbInterceptorManager;
import com.jivesoftware.community.mail.incoming.impl.AttachmentData;
import com.jivesoftware.community.renderer.impl.v2.JAXPUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class UpdateDocumentStrategy implements PublishStrategy<Document> {

    private static final Logger log = Logger.getLogger(UpdateDocumentStrategy.class);

    @Autowired
    private AttachmentHelper attachmentHelper;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private RemoteSupport remoteSupport;

    @Autowired
    private AttachmentManager attachmentManager;

    @Autowired
    private BinaryBodyManager binaryBodyManger;

    @Autowired
    private DocumentStrategyHelper documentStrategyHelper;

    @Autowired
    private IngressFilter ingressFilter;

    @Autowired
    private DocumentCollaborationStrategy documentCollaborationStrategy;

    @Autowired
    private TempDocumentStore tempDocumentStore;

    @Autowired
    private BodyCacheManager bodyCacheManager;

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


        UpdateDocumentServiceBean bean = context.getBean();

        List<AttachmentData> attachments = context.getAttachments();

        User user = authenticationProvider.getJiveUser();

        JiveContainer container = context.getJiveContainer();

        Document document = null;

        try {

            document = documentManager.getDocument(bean.getDocumentID()).getVersionManager().getNewestDocumentVersion()
                    .getDocument();

            document.startEdit(user);

            documentStrategyHelper.applyDraftState(document,
                    context.getApprovers() !=null,
                    bean.isDraft());

            if (bean.isBinary()) {

                document.setSummary(JAXPUtils.toXmlString(ingressFilter.filter(bean.getBody(), document, bean.isMobileEditor(), !bean.isMobileEditor(), false)));
                document.setSubject(bean.getSubject());

                // see if the binary body has been changed
                if (!attachments.isEmpty()) {
                    AttachmentData info = attachments.get(0);

                    try {

                        attachmentHelper
                                .setBinaryBody(document, info.getFilename(), info.getContentType(),
                                        info.getInputStream());

                        // avoid the common attachment code processing attachments again
                        context.getAttachments().clear();
                    }
                    finally {
                        IOUtils.closeQuietly(info.getInputStream());
                    }
                }
            }
            else {
                document.setSubject(bean.getSubject());
                // Lets store old body in the cache so that listeners can use it. The cache will be
                // cleaned up after 1 minute
                bodyCacheManager.storeCachedBody(document, document.getBodyXML());
                // Set the new body now (we may or may not increase the version number later)
                document.setBody(ingressFilter.filter(bean.getBody(), document, bean.isMobileEditor(),
                        !bean.isMobileEditor(), false));
            }

            // move id needed
            if (document.getContainerID() != container.getID() &&
                    document.getContainerType() != container.getObjectType())
            {
                documentManager.moveDocument(document, container);
            }

            /*
                This might appear to be the same thing the content publisher is doing, but documents are versioned
                differently based on attachments
             */
            for (long attachmentID : bean.getRemoveAttachmentIDs()) {

                Attachment attachment = attachmentManager.getAttachment(attachmentID);

                document.deleteAttachment(attachment);

            }

            document.setModificationDate(new Date());
            document.setMinorEdit(bean.isMinorEdit());

            
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
            
            DbInterceptorManager interceptorManager = documentManager.initDbInterceptorManager(document.getJiveContainer());
            documentManager.invokePreInterceptors(interceptorManager, document);
        }
        catch (DocumentAlreadyExistsException dae) {
            responseBuilder.addError("doc.create.error_title_unique");
        }
        catch (AttachmentException e) {
            String key = attachmentHelper.getAttachmentExceptionKey(e);
            responseBuilder.addError(remoteSupport.getText(key, new String[]{e.getAttachmentName()}));
        }
        catch (BinaryBodyException e) {
            String key = documentStrategyHelper.getBinaryBodyExceptionKey(e);
            responseBuilder.addError(key);
        }
        catch (IllegalArgumentException iae) {
            log.error(String.format("Unexpected error saving a document %s", bean.getSubject()), iae);
            responseBuilder.addError("err.could_not_save_doc.text");
        }
        catch (IOException e) {
            log.error(String.format("Unexpected error saving a document %s", bean.getSubject()), e);
            responseBuilder.addError("err.could_not_save_doc.text");
        }
        catch (DocumentObjectNotFoundException e) {
            log.error(String.format("Unexpected error saving a document %s", bean.getSubject()), e);
            responseBuilder.addError("err.could_not_save_doc.text");
        }
        catch (AttachmentNotFoundException e) {
            log.error(String.format("Unexpected error saving a document %s", bean.getSubject()), e);
            responseBuilder.addError("err.could_not_save_doc.text");
        }

        return document;
    }

    @Override
    public void after(Document document, PublishContext context, PublishResponse.Builder responseBuilder) {
        // check if overriding the version that another user managed to save (and if the document is not a binary document) and forward to the version diff .
        if (document.getVersionManager().getNewestDocumentVersion().getVersionNumber() >
                context.<UpdateDocumentServiceBean>getBean().getEditedVersion() && document.isTextBody()) {
            tempDocumentStore.put(document.getID(), authenticationProvider.getJiveUser(), document.getBody());
            responseBuilder.setRedirectURL(DocCreationVersionErrorAction.getActionURL(document));
            return;
        }

        if (document.getStatus() != JiveContentObject.Status.AWAITING_MODERATION)  {
            document.save();
        }
        else {
            document.save(false,true);
        }

        stopEdit(document);
    }

    @Override
    public CollaborationStrategy lookupCollaborationStrategy(PublishContext context) {
        return documentCollaborationStrategy;
    }

    private void stopEdit(Document document) {
        User user = authenticationProvider.getJiveUser();
        document.stopEdit(user);
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
    
}

