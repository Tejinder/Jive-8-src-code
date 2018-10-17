package com.grail.kantar.action;

import com.grail.kantar.beans.KantarReportBean;
import com.grail.kantar.manager.KantarReportManager;
import com.grail.kantar.util.KantarUtils;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
//import com.grail.synchro.action.HomeAction;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.ActionUtils;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.ByteFormat;
import com.opensymphony.xwork2.Preparable;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/2/15
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class KantarReportUploadAction extends JiveActionSupport implements Preparable {

    public static final String UPLOAD_REPORT_RESPONSE = "uploadReportResponse";
    private static final Logger logger = LogManager.getLogger(KantarReportUploadAction.class);

    private Long attachmentId;
    List<AttachmentBean> attachments;
    private boolean jiveUploadSizeLimitExceeded = false;
    private boolean canAttach = true;
    private long[] removeAttachID;
    private File[] attachFile;
    private String[] attachFileContentType;
    private String[] attachFileFileName;
    private int attachmentCount = 0;

    private final int MAX_SIZE_MB = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_ATTACHMENT_SIZE_MB, SynchroConstants.MAX_ATTACHMENT_SIZE_MB);
    private InputStream uploadReportStatus;

    private Long id;

    private KantarReportManager kantarReportManager;


    private KantarReportBean kantarReportBean;
    private KantarReportBean originalKantarReportBean;


    private Long authorId;
    private String authorName;

    private boolean canEditReport;
    private boolean newReport;


    @Override
    public void prepare() throws Exception {
        initializeDefaults();

        if(id != null && id > 0) {
            originalKantarReportBean = new KantarReportBean();
            originalKantarReportBean.setId(kantarReportBean.getId());
            originalKantarReportBean.setReportName(kantarReportBean.getReportName());
            originalKantarReportBean.setCountry(kantarReportBean.getCountry());
            originalKantarReportBean.setReportType(kantarReportBean.getReportType());
            originalKantarReportBean.setComments(kantarReportBean.getComments());
            originalKantarReportBean.setAttachments(kantarReportBean.getAttachments());
            originalKantarReportBean.setCreatedBy(kantarReportBean.getCreatedBy());
            originalKantarReportBean.setCreationDate(kantarReportBean.getCreationDate());
            originalKantarReportBean.setModifiedBy(kantarReportBean.getModifiedBy());
            originalKantarReportBean.setModificationDate(kantarReportBean.getModificationDate());
            originalKantarReportBean.setOtherReportType(kantarReportBean.getOtherReportType());
        }

        ServletRequestDataBinder binder = new ServletRequestDataBinder(this.kantarReportBean);
        binder.bind(getRequest());
    }

    @Override
    public String input() {
        if(!SynchroPermHelper.canAccessDocumentRepositoryPortal(getUser())) {
            return UNAUTHORIZED;
        }

        initializeDefaults();
        if(kantarReportBean != null && kantarReportBean.getId() != null && kantarReportBean.getId() > 0) {
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.DOCUMENT.getDescription(), SynchroGlobal.PageType.REPORTS.getId(),
                    SynchroGlobal.Activity.VIEW.getId(),
                    0,
                    "View Report",
                    kantarReportBean.getReportName(), -1L, getUser().getID());
        }
        return INPUT;
    }



    private void initializeDefaults() {
        canEditReport = false;
        newReport = false;
        if(request.getParameter("id") != null && !request.getParameter("id").equals("")) {
            id = Long.parseLong(request.getParameter("id"));
        }
        if(id != null && id > 0) {
            kantarReportBean = kantarReportManager.get(id);
            attachments = kantarReportManager.getKantarReportAttachments(id);
            kantarReportBean.setAttachments(attachments);

            newReport = false;
            if(kantarReportBean != null) {
                if(kantarReportBean.getCreatedBy() != null
                        && kantarReportBean.getCreatedBy() > 0) {
                    try {
                        User user = userManager.getUser(kantarReportBean.getCreatedBy());
                        setAuthorDetails(user);
                    } catch (UserNotFoundException e) {
                    	logger.error("User not found.");
                    }
                }
            }
            newReport = false;

//            if(kantarReportBean != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
//                    (kantarReportBean.getCreatedBy() != null && kantarReportBean.getCreatedBy().equals(getUser().getID()))
//                    || (kantarReportBean.getModifiedBy() != null && kantarReportBean.getModifiedBy().equals(getUser().getID())))) {
            if(kantarReportBean != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
                    SynchroPermHelper.isDocumentRepositoryAgencyUser(getUser()) || SynchroPermHelper.isDocumentRepositoryBATUser(getUser()))) {
                canEditReport = true;
                canAttach = true;
            } else {
                canEditReport = false;
                canAttach = false;
            }

        } else {
            kantarReportBean = new KantarReportBean();
            attachments = new ArrayList<AttachmentBean>();
            setAuthorDetails(getUser());
            newReport = true;
            canEditReport = true;
            canAttach = true;
        }

        if(attachments != null) {
            attachmentCount = attachments.size();
        }

    }

    private void setAuthorDetails(final User user) {
        if(user != null) {
            authorId = user.getID();
            authorName = user.getFirstName() + " " + user.getLastName();
        }
    }

    @Override
    public String execute() {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
        MetaField newReportType = null;
        boolean isEdit = (kantarReportBean.getId() != null && kantarReportBean.getId() > 0);
        if(isFormValid(result)) {

            try {
                if(kantarReportBean.getReportType() != null && kantarReportBean.getReportType().intValue() == -100 &&
                        kantarReportBean.getOtherReportType() != null && !kantarReportBean.getOtherReportType().equals("")) {
                    newReportType = KantarUtils.setKantarReportType(kantarReportBean.getOtherReportType().trim(), true);
                    kantarReportBean.setReportType(newReportType.getId().intValue());

                }

                Calendar cal = Calendar.getInstance();
                if(kantarReportBean.getId() == null) {
                    kantarReportBean.setCreatedBy(getUser().getID());
                    kantarReportBean.setCreationDate(cal.getTime());
                }
                kantarReportBean.setModifiedBy(getUser().getID());
                kantarReportBean.setModificationDate(cal.getTime());

                Long successId = kantarReportManager.save(kantarReportBean);

                if(successId != null && successId > 0) {
                    id = successId;
                    handleAttachments();
                    kantarReportBean.setAttachments(kantarReportManager.getKantarReportAttachments(id));
                    result.put("success", true);
                    result.put("id", successId);
                } else {
                    result.put("success", false);
                }

                if(newReportType != null) {
                    result.put("newReportType", newReportType);
                }
            } catch (Exception e) {
                result.put("success", false);
                logger.error(e.getMessage());
            }
        }
        out.put("data", result);
        try {
            uploadReportStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
        	logger.error(e.getMessage());
        }

        Set<String> changedFieldNames = new HashSet<String>();
        if(isEdit && originalKantarReportBean != null) {
            if(!originalKantarReportBean.getReportName().trim().equals(kantarReportBean.getReportName().trim())) {
                changedFieldNames.add("Document Name");
            }

            if(originalKantarReportBean.getCountry() != kantarReportBean.getCountry()) {
                changedFieldNames.add("Country");
            }

            if(originalKantarReportBean.getReportType() != kantarReportBean.getReportType()) {
                changedFieldNames.add("Document Type");
            }

            if((originalKantarReportBean.getComments() == null &&  kantarReportBean.getComments() != null) ||
                    (originalKantarReportBean.getComments() != null &&  kantarReportBean.getComments() == null) ||
                    (originalKantarReportBean.getComments() != null && kantarReportBean.getComments() != null
                            && !originalKantarReportBean.getComments().trim().equals(kantarReportBean.getComments().trim()))) {
                changedFieldNames.add("Comments");
            }

            if(originalKantarReportBean.getAttachments().size() != kantarReportBean.getAttachments().size()) {
                changedFieldNames.add("Attachments");
            } else {
                for(AttachmentBean outerBean: originalKantarReportBean.getAttachments()) {
                    boolean isContains = false;
                    for(AttachmentBean innerBean: kantarReportBean.getAttachments()) {
                        if(outerBean.getID() == innerBean.getID()) {
                            isContains = true;
                            break;
                        }
                    }
                    if(!isContains) {
                        changedFieldNames.add("Attachments");
                        break;
                    }
                }

            }
        }


        //Audit Logs
        if(!isEdit || (isEdit && changedFieldNames.size() > 0)) {
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.DOCUMENT.getDescription(), SynchroGlobal.PageType.REPORTS.getId(),
                    isEdit?SynchroGlobal.Activity.EDIT.getId():SynchroGlobal.Activity.UPLOAD.getId(),
                    0,
                    isEdit?"Edit Reports"+(changedFieldNames.size() > 0?(" - Fields Changed: "+StringUtils.join(changedFieldNames, ", ")):""):"Upload Report",
                    kantarReportBean.getReportName(),
                    -1L, getUser().getID());
        }

        return UPLOAD_REPORT_RESPONSE;
    }

    public boolean isFormValid(Map<String, Object> result) {
        boolean isValid = true;
        result.clear();
        if(this.kantarReportBean != null) {
            if(this.kantarReportBean.getReportType() != null
                    && this.kantarReportBean.getReportType().intValue() == -100
                    && this.kantarReportBean.getOtherReportType() != null
                    && !this.kantarReportBean.getOtherReportType().equals("")) {
                MetaField reportType = KantarUtils.getKantarReportType(this.kantarReportBean.getOtherReportType());
                if(reportType != null && reportType.getId() > 0) {
                    result.put("otherReportTypeError", "Entered document type already exists. Please select from the list");
                    result.put("success", false);
                    result.put("formInvalid", true);
                    return false;
                }
            }
            if(this.kantarReportBean.getReportName() == null
                    || this.kantarReportBean.getReportName().equals(""))  {
                result.put("reportNameError", "Please enter document name.");
                isValid = false;
            }

            if(this.kantarReportBean.getCountry() == null
                    || (this.kantarReportBean.getCountry() != -100 && this.kantarReportBean.getCountry() <= 0)) {
                result.put("countryError", "Please select country.");
                isValid = false;
            }

            if(this.kantarReportBean.getReportType() == null ||
                    (this.kantarReportBean.getReportType() <= 0 && this.kantarReportBean.getReportType().intValue() != -100)) {
                result.put("reportTypeError", "Please select document type.");
                isValid = false;
            }

            if(this.kantarReportBean.getReportType() != null
                    && this.kantarReportBean.getReportType().intValue() == -100) {
                if(this.kantarReportBean.getOtherReportType() == null || this.kantarReportBean.getOtherReportType().equals("")) {
                    result.put("otherReportTypeError", "Please enter the document type.");
                    isValid = false;
                }
            }

            if((attachFile == null || attachFile.length <= 0) && (attachments == null || attachments.size() <= 0)) {
                result.put("attachFileError", "Please attach at least one file to upload document.");
                isValid = false;
            }
        } else {
            isValid = false;
        }

        if(!isValid) {
            result.put("success", false);
            result.put("formInvalid", true);
        }
        return isValid;
    }



    private void handleAttachments() {
        // add any attachments that we've been told to add
        if (attachFile != null && attachFile.length != 0) {
            // Check to see that the incoming request is a multipart request.
            if (ActionUtils.isMultiPart(ServletActionContext.getRequest())) {
                addAttachments();
            }
            else {
                addFieldError("attachFile", getText("attach.err.upload_errors.text"));
            }
        }

        if(removeAttachID != null && removeAttachID.length > 0) {
            for(long id : removeAttachID) {
                try {
                    kantarReportManager.removeAttachment(id);
                } catch (AttachmentNotFoundException a) {
                    a.printStackTrace();
                } catch (AttachmentException b) {
                    b.printStackTrace();
                } catch (Exception c) {
                    c.printStackTrace();
                }
            }
        }
    }

    protected void addAttachments() throws UnauthorizedException {
        if (attachFile == null) {
            return;
        }
        log.debug("attachFile size was " + attachFile.length);

        for (int i = 0; i < attachFile.length; i++) {
            File file = attachFile[i];
            if (file == null) {
                log.debug("File was null, skipping");
                continue;
            }

            log.debug("File size was " + file.length());
            String fileName = attachFileFileName[i];
            String contentType = attachFileContentType[i];
            try
            {
                kantarReportManager.addKantarReportAttachments(attachFile[i], attachFileFileName[i], attachFileContentType[i], id, getUser().getID());
                attachments = kantarReportManager.getKantarReportAttachments(id);
            }
            catch (AttachmentException ae) {
            	logger.error("AttachmentException - " + ae.getMessage());
                handleAttachmentException(file, fileName, contentType, ae);
            } catch (UnauthorizedException ue) {
            	logger.error("UnauthorizedException - " + ue.getMessage());
            } catch (Exception e) {
            	logger.error("Exception - " + e.getMessage());
            }
        }

        if(attachments!=null)
            attachmentCount = attachments.size();
    }

    protected void handleAttachmentException(File file, String fileName, String contentType, AttachmentException e) {
        final AttachmentManager attachmentManager = JiveApplication.getContext().getAttachmentManager();
        if (e.getErrorType() == AttachmentException.TOO_LARGE) {
            List<Serializable> args = new ArrayList<Serializable>();
            args.add(fileName);
            String error = getText("attach.err.file_too_large.text", args);
            args.add(file.length());
            args.add(contentType);
            args.add(error);
            addFieldError("attachFile", error);
        }
        else if (e.getErrorType() == AttachmentException.BAD_CONTENT_TYPE) {
            List<Serializable> args = new ArrayList<Serializable>();
            args.add(fileName);
            String error = getText("attach.err.badContentType.text", args);
            args.add(file.length());
            args.add(contentType);
            args.add(error);

            addFieldError("attachFile", error);
        }
        else if (e.getErrorType() == AttachmentException.TOO_MANY_ATTACHMENTS) {
            addFieldError("attachFile", getText("attach.err.tooManyAttchmts.text"));
        }
        else {
            List<Serializable> args = new ArrayList<Serializable>();
            int maxAttachSize = attachmentManager.getMaxAttachmentSize();
            args.add((new ByteFormat()).formatKB(maxAttachSize));
            String error = getText("attach.err.no_read_perm.text", args);
            args.clear();
            args.add(fileName);
            args.add(file.length());
            args.add(contentType);
            args.add(error);

            addFieldError("attachFile", error);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InputStream getUploadReportStatus() {
        return uploadReportStatus;
    }

    public void setUploadReportStatus(InputStream uploadReportStatus) {
        this.uploadReportStatus = uploadReportStatus;
    }

    public KantarReportBean getKantarReportBean() {
        return kantarReportBean;
    }

    public void setKantarReportBean(KantarReportBean kantarReportBean) {
        this.kantarReportBean = kantarReportBean;
    }

    public KantarReportManager getKantarReportManager() {
        return kantarReportManager;
    }

    public void setKantarReportManager(KantarReportManager kantarReportManager) {
        this.kantarReportManager = kantarReportManager;
    }


    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public List<AttachmentBean> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentBean> attachments) {
        this.attachments = attachments;
    }

    public boolean isJiveUploadSizeLimitExceeded() {
        return jiveUploadSizeLimitExceeded;
    }

    public void setJiveUploadSizeLimitExceeded(boolean jiveUploadSizeLimitExceeded) {
        this.jiveUploadSizeLimitExceeded = jiveUploadSizeLimitExceeded;
    }

    public boolean isCanAttach() {
        return canAttach;
    }

    public void setCanAttach(boolean canAttach) {
        this.canAttach = canAttach;
    }

    public long[] getRemoveAttachID() {
        return removeAttachID;
    }

    public void setRemoveAttachID(long[] removeAttachID) {
        this.removeAttachID = removeAttachID;
    }

    public File[] getAttachFile() {
        return attachFile;
    }

    public void setAttachFile(File[] attachFile) {
        this.attachFile = attachFile;
    }

    public String[] getAttachFileContentType() {
        return attachFileContentType;
    }

    public void setAttachFileContentType(String[] attachFileContentType) {
        this.attachFileContentType = attachFileContentType;
    }

    public String[] getAttachFileFileName() {
        return attachFileFileName;
    }

    public void setAttachFileFileName(String[] attachFileFileName) {
        this.attachFileFileName = attachFileFileName;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(int attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public boolean isCanEditReport() {
        return canEditReport;
    }

    public void setCanEditReport(boolean canEditReport) {
        this.canEditReport = canEditReport;
    }

    public boolean isNewReport() {
        return newReport;
    }

    public void setNewReport(boolean newReport) {
        this.newReport = newReport;
    }
}
