package com.grail.synchro.action;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.MyLibraryManager;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MyLibraryAction extends JiveActionSupport {

    private final static Logger LOG = Logger.getLogger(MyLibraryAction.class);

    private PermissionManager permissionManager;
    private MyLibraryManager myLibraryManager;
    private boolean jiveUploadSizeLimitExceeded = false;
    private Long id;
    private String title;
    private String description;
    private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private InputStream addDocStatus;
    private InputStream removeDocStatus;
    private static final String ADD_DOC_RESPONSE = "addDocResponse";
    private static final String REMOVE_DOC_RESPONSE = "removeDocResponse";

    private String fileName;
    private SynchroUtils synchroUtils;


    public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    @Override
    public String execute() {
        if(getUser() != null) {
            if(!getPermissionManager().isSynchroUser(getUser()))
            {
                return UNAUTHORIZED;
            }
        }
        
        /*//Audit Logs
        String i18Key = SynchroGlobal.ProjectActivationStatus.getName(projectActivateStatus);
		String i18Text = getText("logger.project.status."+ i18Key.toLowerCase());
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
								SynchroGlobal.LogProjectStage.PROJECT_STATUS.getId(), i18Text, project.getName(), 
										project.getProjectID(), getUser().getID());*/
        return SUCCESS;
    }

    public String addDocument() throws UnsupportedEncodingException {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
        if(isFormValid(result)) {
            try {
                myLibraryManager.addDocument(title, description, attachFile, attachFileFileName, attachFileContentType);
                
                //Audit Logs
                String i18CustText = getText("logger.project.document.upload.text")+"- " + attachFileFileName;
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.MYLIBRARY.getId(), SynchroGlobal.Activity.UPLOAD.getId(), 
                										SynchroGlobal.LogProjectStage.DOCUMENT.getId(), i18CustText, "", -1L, getUser().getID());
                
                result.put("success", true);
                result.put("message", "Successfully added document");
            } catch (AttachmentException e) {
                result.put("success", false);
                result.put("message", "Unable to upload file.");
            } catch (UnauthorizedException e) {
                result.put("success", false);
                result.put("message", "Unauthorized.");
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", e.getMessage());
            }
        }
        try {
            out.put("data", result);
            addDocStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
            throw new UnsupportedEncodingException(e.getMessage());
        }
        return ADD_DOC_RESPONSE;
    }

    public String removeDocument() throws UnsupportedEncodingException {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
        try {
            myLibraryManager.removeDocument(id);
            result.put("success", true);
            result.put("message", "Successfully removed document");
        } catch (AttachmentException e) {
            result.put("success", false);
            result.put("message", "Attachment not found.");
        } catch (UnauthorizedException e) {
            result.put("success", false);
            result.put("message", "Unauthorized to remove document.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        try {
            out.put("data", result);
            removeDocStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
            
          //Audit Logs
            String i18CustText = fileName +" deleted";
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.MYLIBRARY.getId(), SynchroGlobal.Activity.DELETE.getId(), 
            										SynchroGlobal.LogProjectStage.DOCUMENT.getId(), i18CustText, "", -1L, getUser().getID());
            
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
            throw new UnsupportedEncodingException(e.getMessage());
        }
        return REMOVE_DOC_RESPONSE;
    }

    public boolean isFormValid(Map<String, Object> result) {
        boolean isValid = true;
        if(title == null || title.equals(""))  {
            result.put("title", "Please enter document title.");
            isValid = false;
        }
        if(attachFile == null) {
            result.put("attachFile", "Please attach file to the document.");
            isValid = false;
        } else if (isJiveUploadSizeLimitExceeded()) {
            result.put("attachFile", getText("attach.err.upload_errors.text"));
            isValid = false;
        }

        if(!isValid) {
            result.put("success", false);
            result.put("formInvalid", true);
        }
        return isValid;
    }


    public boolean isJiveUploadSizeLimitExceeded() {
        return jiveUploadSizeLimitExceeded;
    }

    public void setJiveUploadSizeLimitExceeded(boolean jiveUploadSizeLimitExceeded) {
        this.jiveUploadSizeLimitExceeded = jiveUploadSizeLimitExceeded;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(final PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public MyLibraryManager getMyLibraryManager() {
        return myLibraryManager;
    }

    public void setMyLibraryManager(MyLibraryManager myLibraryManager) {
        this.myLibraryManager = myLibraryManager;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getAttachFile() {
        return attachFile;
    }

    public void setAttachFile(File attachFile) {
        this.attachFile = attachFile;
    }

    public String getAttachFileContentType() {
        return attachFileContentType;
    }

    public void setAttachFileContentType(String attachFileContentType) {
        this.attachFileContentType = attachFileContentType;
    }

    public String getAttachFileFileName() {
        return attachFileFileName;
    }

    public void setAttachFileFileName(String attachFileFileName) {
        this.attachFileFileName = attachFileFileName;
    }

    public InputStream getAddDocStatus() {
        return addDocStatus;
    }

    public void setAddDocStatus(InputStream addDocStatus) {
        this.addDocStatus = addDocStatus;
    }

    public InputStream getRemoveDocStatus() {
        return removeDocStatus;
    }

    public void setRemoveDocStatus(InputStream removeDocStatus) {
        this.removeDocStatus = removeDocStatus;
    }
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
