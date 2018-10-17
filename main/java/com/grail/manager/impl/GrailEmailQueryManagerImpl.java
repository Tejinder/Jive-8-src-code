package com.grail.manager.impl;

import com.grail.GrailGlobals;
import com.grail.beans.GrailEmailQuery;
import com.grail.dao.GrailEmailQueriesDAO;
import com.grail.manager.GrailEmailQueriesManager;
import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.util.KantarGlobals;
import com.grail.object.GrailAttachment;
import com.grail.objecttype.GrailEmailQueryAttachmentObjectType;
import com.grail.util.BATGlobal;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.InputStreamDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/4/14
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailEmailQueryManagerImpl implements GrailEmailQueriesManager {

    private GrailEmailQueriesDAO grailEmailQueriesDAO;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;

    private EmailManager emailManager;

    @Override
    public boolean processQuery(final GrailEmailQuery emailQuery) {
        grailEmailQueriesDAO.saveQuery(emailQuery);
        return false;
    }

    @Override
    public boolean processQuery(final String recipients, final String subject, final String body, final Integer type,
                                final User sender, final File attachment, final String fileName, final String contentType, final String portalType) {
        boolean success = false;
        try {
            EmailMessage message = new EmailMessage();

            Map<String, Object> messageContext = message.getContext();
            messageContext.put("portalType", portalType);

            EmailMessage.EmailAddress emailAddress = new EmailMessage.EmailAddress(sender.getUsername(), sender.getEmail());
            message.setSender(emailAddress);
            message.setSubject(subject);
            message.setTextBody(body);
            message.setHtmlBody(body);
            String receiptPrefix = "";
            if(portalType.equals(BATGlobal.PortalType.GRAIL.toString())) {
                receiptPrefix = "Grail";
            } else if(portalType.equals(BATGlobal.PortalType.KANTAR.toString())) {
                receiptPrefix = "Kantar";
            } else if(portalType.equals(BATGlobal.PortalType.KANTAR_REPORT.toString())) {
                receiptPrefix = "Research Repository";
            }
            message.addRecipient(receiptPrefix, recipients);
            if(attachment != null) {
                message.addAttachment(new InputStreamDataSource(fileName, contentType, new FileInputStream(attachment)));
            }
            boolean emailSent = false;
            try {
                emailManager.send(message);
                emailSent = true;
            } catch (Exception e) {
                emailSent = false;
            }

            if(emailSent) {
                GrailEmailQuery query = new GrailEmailQuery();
                query.setRecipients(recipients);
                query.setSubject(subject);
                query.setBody(body);
                query.setSender(sender.getID());
                query.setCreationDate(new Date());
                query.setAttachmentId(-1L);
                query.setEmailSent(emailSent);
                query.setType(type);
                Long id = grailEmailQueriesDAO.saveQuery(query);
                if(id != null && attachment != null) {
                    Attachment att = attachmentHelper.createAttachment(
                            getGrailEmailAttachmentObject(id, type), fileName , contentType, attachment);
                    grailEmailQueriesDAO.updateAttachment(id, att.getID());
                }
            }
            success = true;
        } catch (IOException e) {
            success = false;
        } catch (AttachmentException e) {
            success = false;
        }

        return success;
    }

    private GrailAttachment getGrailEmailAttachmentObject(final Long id, final Integer type) {

        GrailAttachment grailAttachment = new GrailAttachment();
        grailAttachment.getBean().setObjectId((id).hashCode());
        Integer objectType = KantarGlobals.buildKantarAttachmentObjectID((GrailGlobals.GrailEmailQueriesType.getNameById(type).replaceAll("\\s","") + "EmailQueriesDocuments"), GrailEmailQueryAttachmentObjectType.GRAIL_EMAIL_QUERY_ATTACHMENT_OBJECT_CODE.toString());
        grailAttachment.getBean().setObjectType(objectType);
        return grailAttachment;
    }

    public GrailEmailQueriesDAO getGrailEmailQueriesDAO() {
        return grailEmailQueriesDAO;
    }

    public void setGrailEmailQueriesDAO(GrailEmailQueriesDAO grailEmailQueriesDAO) {
        this.grailEmailQueriesDAO = grailEmailQueriesDAO;
    }

    public AttachmentHelper getAttachmentHelper() {
        return attachmentHelper;
    }

    public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
        this.attachmentHelper = attachmentHelper;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public EmailManager getEmailManager() {
        return emailManager;
    }

    public void setEmailManager(EmailManager emailManager) {
        this.emailManager = emailManager;
    }
}
