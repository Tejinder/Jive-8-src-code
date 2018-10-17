package com.grail.synchro.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.manager.PermissionManager;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * PIB Document Import Utility
 */
public class EmailAttachmentAction extends JiveActionSupport {

    private final static Logger LOG = Logger.getLogger(EmailAttachmentAction.class);

	private InputStream emailAttachmentStatus;
    private static final String ATTACHMENT_RESPONSE = "attachmentResponse";
    private PermissionManager permissionManager;
	private File mailAttachment;
	private String mailAttachmentFileName;
	private String mailAttachmentContentType;
	private final int MAX_SIZE_MB = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_ATTACHMENT_SIZE_MB, SynchroConstants.MAX_ATTACHMENT_SIZE_MB);
	
    @Override
    public String execute() {
        return UNAUTHORIZED;
    }

    public String validateEmailAttachment() throws UnsupportedEncodingException {
    	//Authentication layer
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            // This will check whether the user has accepted the Disclaimer or not.            

            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
                return UNAUTHORIZED;
            }
        }
        
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
            try {
            	
            	if(mailAttachment!=null)
            	{
            		InputStream fileInputStream = new FileInputStream(mailAttachment);
            		double sizeBytes = mailAttachment.length();
            		double sizeKilobytes = (sizeBytes / 1024);
        			double sizeMegabytes = (sizeKilobytes / 1024);
        			if(sizeMegabytes>MAX_SIZE_MB)
        			{
        				 result.put("success", false);
        				 result.put("message", "File size is larger. Please attach file lesser than " + MAX_SIZE_MB + " MB");
        			}
        			else
        			{
        				result.put("success", true);
                        result.put("message", "success");		
        			}
            	}                
                //result.put("fieldMap", pibFieldMap);
            }  catch (UnauthorizedException e) {
                result.put("success", false);
                result.put("message", "Unauthorized");
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "Unknown error while validating email attachment");
            }
        try {
            out.put("data", result);
            emailAttachmentStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
            throw new UnsupportedEncodingException(e.getMessage());
        }
        return ATTACHMENT_RESPONSE;

    }


public PermissionManager getPermissionManager() {
    if(permissionManager == null){
        permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
    }
    return permissionManager;
}

public InputStream getEmailAttachmentStatus() {
	return emailAttachmentStatus;
}

public void setEmailAttachmentStatus(InputStream emailAttachmentStatus) {
	this.emailAttachmentStatus = emailAttachmentStatus;
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

public void setPermissionManager(PermissionManager permissionManager) {
	this.permissionManager = permissionManager;
}


}
