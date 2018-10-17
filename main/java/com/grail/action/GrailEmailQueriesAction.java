package com.grail.action;

import com.grail.manager.GrailEmailQueriesManager;
import com.grail.synchro.SynchroConstants;
import com.grail.util.BATConstants;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/4/14
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class GrailEmailQueriesAction extends JiveActionSupport {
    private static Logger LOG = Logger.getLogger(GrailEmailQueriesAction.class);

    public static final String EMAIL_QUERY_NOTIFICATION_RESPONSE = "emailQueryNotificationResponse";

    private File mailAttachment;
    private String mailAttachmentFileName;
    private String mailAttachmentContentType;

    private String recipients;
    private String subject;
    private String messageBody;
    private Integer type;

    private final int MAX_SIZE_MB = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_ATTACHMENT_SIZE_MB, SynchroConstants.MAX_ATTACHMENT_SIZE_MB);
    private InputStream emailQueryNotificationStatus;

    private GrailEmailQueriesManager grailEmailQueriesManager;
    private AttachmentManager attachmentManager;

    /**
     * used to send email query notification that was requested from  Kantar portal
     * @return jsonResponse
     */
    public String sendNotification() {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
        try {
            HttpSession session = getRequest().getSession();
            String portalType = session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE).toString();
            boolean success = grailEmailQueriesManager.processQuery(recipients, subject, messageBody, type,
                    getUser(), mailAttachment, mailAttachmentFileName, mailAttachmentContentType, portalType);
            if(success) {
                result.put("success", true);
                result.put("message", "Your message has been successfully sent.");
            } else {
                result.put("success", false);
                result.put("message", "Unable to send notification.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Unable to send notification.");
            LOG.error(e.getMessage());
        }
        out.put("data", result);
        try {
            emailQueryNotificationStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
        }
        return EMAIL_QUERY_NOTIFICATION_RESPONSE;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        GrailEmailQueriesAction.LOG = LOG;
    }

    public File getMailAttachment() {
        return mailAttachment;
    }

    public void setMailAttachment(File mailAttachment) {
        this.mailAttachment = mailAttachment;
    }

    public String getMailAttachmentFileName() {
        return mailAttachmentFileName;
    }

    public void setMailAttachmentFileName(String mailAttachmentFileName) {
        this.mailAttachmentFileName = mailAttachmentFileName;
    }

    public String getMailAttachmentContentType() {
        return mailAttachmentContentType;
    }

    public void setMailAttachmentContentType(String mailAttachmentContentType) {
        this.mailAttachmentContentType = mailAttachmentContentType;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public InputStream getEmailQueryNotificationStatus() {
        return emailQueryNotificationStatus;
    }

    public void setEmailQueryNotificationStatus(InputStream emailQueryNotificationStatus) {
        this.emailQueryNotificationStatus = emailQueryNotificationStatus;
    }

    public GrailEmailQueriesManager getGrailEmailQueriesManager() {
        return grailEmailQueriesManager;
    }

    public void setGrailEmailQueriesManager(GrailEmailQueriesManager grailEmailQueriesManager) {
        this.grailEmailQueriesManager = grailEmailQueriesManager;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
}
