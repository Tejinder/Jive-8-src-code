package com.grail.synchro.action;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.ProjectWaiverManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.URLUtils;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.ActionUtils;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.ByteFormat;
import com.opensymphony.xwork2.Preparable;


public class ProjectWaiverAction extends JiveActionSupport implements Preparable {
    private static Logger LOG = Logger.getLogger(ProjectWaiverAction.class);
    
    private SynchroUtils synchroUtils;
	private ProjectWaiverManager projectWaiverManager;
    private EmailManager emailManager;
    private UserManager userManager;
    private PermissionManager permissionManager;
    private ProjectWaiver projectWaiver;
    
    private String approveWaiver;
    private Boolean isApproved = false;
    private Boolean isApprover = false;
    private Map<Long,Long> attachmentUser;
    private Long projectWaiverID = -1L;
    
    private Long attachmentId;
    List<AttachmentBean> attachments;
    private boolean jiveUploadSizeLimitExceeded = false;
    private boolean canAttach = true;
    private long[] removeAttachID;
    private File[] attachFile;
    private String[] attachFileContentType;
    private String[] attachFileFileName;
    private int attachmentCount;
    
    private String waiverApproved ="";
    private StageManager stageManager;
    private EmailNotificationManager emailNotificationManager;
    

    @Override
	public void prepare() throws Exception {
		
		
	}
    
    @Override
    public String input() {
    	/** Authentication layer **/
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	
        if(!getSynchroUtils().canAccessProjectWaiver(getUser()))
    	{
    		return UNAUTHORIZED;    		
    	}
        
        if(getUser() != null) {
            if(!getPermissionManager().isSynchroUser(getUser()))
            {
                return UNAUTHORIZED;
            }
        }        
       
        
        if(projectWaiverID == null || projectWaiverID < 0)
        {
            this.projectWaiver = new ProjectWaiver();
        }
        else
        {
			this.projectWaiver = projectWaiverManager.get(projectWaiverID);
			if(getUser().getID() == projectWaiver.getApproverID())
			{
				isApprover = true;
				if(!(SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value()==projectWaiver.getStatus()))
					isApproved = true;
			}
        	
        loadAttachments(projectWaiverID);
        
        }
        
        return INPUT;
        
    }

    @Override
    public String execute() {
    	
    	String result = SUCCESS;
    	
    	/** Authentication layer **/
    	if(!SynchroPermHelper.canInitiateWaiver(getUser()))
    	{
    		return UNAUTHORIZED;
    	}
    	
    	/** Binding Project Waiver Bean on Create Waiver**/
    	bindProjectWaiver();
    	
    	//Set Initial Waiver Status from DRAFT to Pending Approval
    	this.projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value());
    	
    	//Save Waiver details to database
    	if(projectWaiverID!=null && projectWaiverID > 0)
    	{
    		this.projectWaiver.setWaiverID(projectWaiverID);
    		projectWaiverManager.update(this.projectWaiver);
    	}
    	else
    	{
    		ProjectWaiver waiver = projectWaiverManager.create(this.projectWaiver);
    		projectWaiverID = waiver.getWaiverID();
    		
    		// Email Notifications when Process Waiver is initiated
    		try
    		{
	    		String approverEmail = getUserManager().getUser(waiver.getApproverID()).getEmail();
	            EmailMessage email = stageManager.populateNotificationEmail(approverEmail, null, null,"send.for.approval.process.waiver.htmlBody","send.for.approval.process.waiver.subject");
	            String baseUrl = URLUtils.getBaseURL(request);
	            //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/proposal-details!input.jspa?projectID=" + projectID;
	            String waiverUrl = baseUrl+"/synchro/project-waiver!input.jspa?projectWaiverID=" + projectWaiverID;
	           
	            email.getContext().put("waiverId", SynchroUtils.generateProjectCode(projectWaiverID));
	            email.getContext().put("waiverName",waiver.getName());
	            email.getContext().put("waiverUrl",waiverUrl);
	            email.getContext().put("userName", getUser().getName());
	
	
	            stageManager.sendNotification(getUser(),email);
	
	            //Email Notification TimeStamp Storage
	            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
	            emailNotBean.setProjectID(projectWaiverID);
	            
	            emailNotBean.setStageID(SynchroConstants.PROCESS_WAIVER_STAGE);
	            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_ACTION);
	            emailNotBean.setEmailDesc("Action Required | Process Waiver has been initiated");
		    	emailNotBean.setEmailSubject("Action Required | Process Waiver has been initiated");
	            emailNotBean.setEmailSender(getUser().getEmail());
	            emailNotBean.setEmailRecipients(approverEmail);
	
	            emailNotificationManager.saveDetails(emailNotBean);
    		}
    		 catch(UserNotFoundException ue)
             {
                 LOG.error("User not found while Process Waiver send for approval notification" + waiver.getApproverID());
             }
    		
    	}
    	
    	if(projectWaiverID > 0)
    	{
    		handleAttachments();
    	}
    	
    	//Audit Logs: Initiate a Waiver
        String i18Text = getText("waiver.initiate.heading");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.MYDASHBOARD.getId(), SynchroGlobal.Activity.ADD.getId(), 
								SynchroGlobal.LogProjectStage.DASHBOARDWAIVER.getId(), i18Text, "", new Long(-1L), getUser().getID());
        
        //Audit Logs: Notification: Submit Waiver for Approval
        String i18SubmitText = getText("logger.project.waiver.submit.waiver");
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.MYDASHBOARD.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
				SynchroGlobal.LogProjectStage.DASHBOARDWAIVER.getId(), i18Text + "- " + i18SubmitText, "", -1L, getUser().getID());
        
    	return result;
    }

    public String submit()
    {
    	bindProjectWaiver();
    	
    	if(approveWaiver!=null)
    	{
    		if(StringUtils.equalsIgnoreCase(approveWaiver, "true"))
        	{
        		projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.APPROVED.value());
        		waiverApproved = "true";
        		
        		// Email Notifications when Process Waiver is Approved
        		try
        		{
    	    		String initiatorEmail = getUserManager().getUser(projectWaiver.getCreationBy()).getEmail();
    	            EmailMessage email = stageManager.populateNotificationEmail(initiatorEmail, null, null,"approve.process.waiver.htmlBody","approve.process.waiver.subject");
    	            String baseUrl = URLUtils.getBaseURL(request);
    	            //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/proposal-details!input.jspa?projectID=" + projectID;
    	            String waiverUrl = baseUrl+"/synchro/project-waiver!input.jspa?projectWaiverID=" + projectWaiverID;
    	           
    	            email.getContext().put("waiverId", SynchroUtils.generateProjectCode(projectWaiverID));
    	            email.getContext().put("waiverName",projectWaiver.getName());
    	            email.getContext().put("waiverUrl",waiverUrl);
    	               	
    	            stageManager.sendNotification(getUser(),email);
    	
    	            //Email Notification TimeStamp Storage
    	            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
    	            emailNotBean.setProjectID(projectWaiverID);
    	            
    	            emailNotBean.setStageID(SynchroConstants.PROCESS_WAIVER_STAGE);
    	            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
    	            emailNotBean.setEmailDesc("Notification | Process Waiver Approved");
    		    	emailNotBean.setEmailSubject("Notification | Process Waiver Approved");
    	            emailNotBean.setEmailSender(getUser().getEmail());
    	            emailNotBean.setEmailRecipients(initiatorEmail);
    	
    	            emailNotificationManager.saveDetails(emailNotBean);
        		}
        		catch(UserNotFoundException ue)
                {
                    LOG.error("User not found while Process Waiver is approved" + projectWaiver.getCreationBy());
                }
        	}
        	else if(StringUtils.equalsIgnoreCase(approveWaiver, "false"))
        	{
        		projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.REJECTED.value());
        		waiverApproved = "false";
        		
        		// Email Notifications when Process Waiver is Rejected
        		try
        		{
    	    		String initiatorEmail = getUserManager().getUser(projectWaiver.getCreationBy()).getEmail();
    	            EmailMessage email = stageManager.populateNotificationEmail(initiatorEmail, null, null,"reject.process.waiver.htmlBody","reject.process.waiver.subject");
    	            String baseUrl = URLUtils.getBaseURL(request);
    	            //String stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/proposal-details!input.jspa?projectID=" + projectID;
    	            String waiverUrl = baseUrl+"/synchro/project-waiver!input.jspa?projectWaiverID=" + projectWaiverID;
    	           
    	            email.getContext().put("waiverId", SynchroUtils.generateProjectCode(projectWaiverID));
    	            email.getContext().put("waiverName",projectWaiver.getName());
    	            email.getContext().put("waiverUrl",waiverUrl);
    	               	
    	            stageManager.sendNotification(getUser(),email);
    	
    	            //Email Notification TimeStamp Storage
    	            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
    	            emailNotBean.setProjectID(projectWaiverID);
    	            
    	            emailNotBean.setStageID(SynchroConstants.PROCESS_WAIVER_STAGE);
    	            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
    	            emailNotBean.setEmailDesc("Notification | Process Waiver Rejected");
    		    	emailNotBean.setEmailSubject("Notification | Process Waiver Rejected");
    	            emailNotBean.setEmailSender(getUser().getEmail());
    	            emailNotBean.setEmailRecipients(initiatorEmail);
    	
    	            emailNotificationManager.saveDetails(emailNotBean);
        		}
        		catch(UserNotFoundException ue)
                {
                    LOG.error("User not found while Process Waiver is rejected" + projectWaiver.getCreationBy());
                }
        	}
    		
    		this.projectWaiver = projectWaiverManager.update(projectWaiver);
    	}
    	
    	return SUCCESS;
    }
    
/*
    public String saveWaiverDetails() throws UnsupportedEncodingException
    {
    	bindProjectWaiver();
        
    	JSONObject out = new JSONObject();
    	if(projectWaiverID != null && projectWaiverID > 0)
    	{
    		this.projectWaiver.setWaiverID(projectWaiverID);
    		projectWaiverManager.update(this.projectWaiver);
    	}
    	else
    	{
    		this.projectWaiver.setStatus(SynchroGlobal.ProjectWaiverStatus.DRAFT.value());
    		ProjectWaiver pw =projectWaiverManager.create(this.projectWaiver);
    		projectWaiverID = pw.getWaiverID();
    		this.projectWaiver.setWaiverID(projectWaiverID);
    	}
    	
    	try {
            out.put("data", getProjectWaiverID());
            saveWaiverStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
            throw new UnsupportedEncodingException(e.getMessage());
        }
    	
        return SAVE_WAIVER_RESPONSE;
    }
  */
  /*
    public String addAttachment() throws UnsupportedEncodingException {

        Map<String, Object> result = new HashMap<String, Object>();
        try
        {
        	projectWaiverManager.addAttachment(attachFile, attachFileFileName, attachFileContentType, projectWaiverID, getUser().getID());
            attachmentMap.put(fieldCategoryId.intValue(), projectWaiverManager.getFieldAttachments(projectWaiverID));
        }
        catch (AttachmentException ae) {
            result.put("success", false);
            result.put("message", "Unable to upload file.");
        } catch (UnauthorizedException ue) {
            result.put("success", false);
            result.put("message", "Unauthorized.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        if(projectWaiverID == null || projectWaiverID < 0)
        {
            this.projectWaiver = new ProjectWaiver();
        }
        else
        {
			this.projectWaiver = projectWaiverManager.get(projectWaiverID);
			getAttachments(projectWaiverID);
        }
        
        
        return INPUT;
    }
    */
		
	public String removeAttachment() throws UnsupportedEncodingException {
	    try
	    {
	    	projectWaiverManager.removeAttachment(attachmentId);
	    }
	    catch (Exception e) {
	        LOG.error("Exception while removing attachment Id --"+ attachmentId);
	    }
	    
	    if(projectWaiverID == null || projectWaiverID < 0)
        {
            this.projectWaiver = new ProjectWaiver();
        }
        else
        {
			this.projectWaiver = projectWaiverManager.get(projectWaiverID);
			loadAttachments(projectWaiverID);
        }
	    return INPUT;
	}
	
/*
	public void sendEmailNotifications(Long status, Long approverID,  Long creationBy) 
		{
		try{
			Boolean sendEmail = true;
			EmailMessage message = new EmailMessage();
			User sender = null;
			User reciever = null;
			
			String waiverURL =  SynchroUtils.getJiveURL()+"/synchro/project-waiver!input.jspa?waiverID="+projectWaiver.getWaiverID();
			switch(status.intValue())
			{
			case 1: sender = getUserManager().getUser(creationBy);
					reciever = getUserManager().getUser(approverID);
					message.setSubject(String.format(SynchroGlobal.EmailNotification.NEW_WAIVER_ADDED.getSubject(), projectWaiver.getID(), projectWaiver.getWaiverName()));
					message.setHtmlBody(String.format(SynchroGlobal.EmailNotification.NEW_WAIVER_ADDED.getMessageBody(), projectWaiver.getWaiverName(), sender.getName(), waiverURL));
					
					break;
			
			case 2: sender = getUserManager().getUser(approverID);
					reciever = getUserManager().getUser(creationBy);
					message.setSubject(String.format(SynchroGlobal.EmailNotification.WAIVER_APPROVE.getSubject(), projectWaiver.getID(), projectWaiver.getWaiverName(), SynchroGlobal.ProjectWaiverStatus.APPROVED.name()));
					message.setHtmlBody(String.format(SynchroGlobal.EmailNotification.WAIVER_APPROVE.getMessageBody(), projectWaiver.getWaiverName(), SynchroGlobal.ProjectWaiverStatus.APPROVED.name(), waiverURL));
					break;

			case 3: sender = getUserManager().getUser(approverID);
					reciever = getUserManager().getUser(creationBy);
					message.setSubject(String.format(SynchroGlobal.EmailNotification.WAIVER_APPROVE.getSubject(), projectWaiver.getID(), projectWaiver.getWaiverName(), SynchroGlobal.ProjectWaiverStatus.REJECTED.name()));
					message.setSubject(String.format(SynchroGlobal.EmailNotification.WAIVER_APPROVE.getSubject(), projectWaiver.getID(), projectWaiver.getWaiverName(), SynchroGlobal.ProjectWaiverStatus.REJECTED.name()));
					message.setHtmlBody(String.format(SynchroGlobal.EmailNotification.WAIVER_APPROVE.getMessageBody(), projectWaiver.getWaiverName(), SynchroGlobal.ProjectWaiverStatus.REJECTED.name(), waiverURL));
					break;
			
			default: sendEmail=false;
			}
			
			if(StringUtils.isValidEmailAddress(reciever.getEmail()))
			{
				message.addRecipient(reciever.getName(), reciever.getEmail());
			}
			if(StringUtils.isValidEmailAddress(sender.getEmail()))
			{
				message.setSender(sender.getName(), sender.getEmail());
			}
			if(sendEmail)
				getEmailManager().send(message);
			
		}catch(UserNotFoundException e){LOG.error("User not found " + e.getMessage());}
		}
	*/


	private void bindProjectWaiver()
	{
		if(projectWaiverID == null || projectWaiverID < 0)
		{			
			this.projectWaiver = new ProjectWaiver();
        }
		else
		{
			this.projectWaiver = projectWaiverManager.get(projectWaiverID);
        }
		
		// Apply request binding ONLY if the request is of type POST
        if(getRequest().getMethod() == "POST"){
            ServletRequestDataBinder binder = new ServletRequestDataBinder(this.projectWaiver);
            binder.bind(getRequest());
            if(binder.getBindingResult().hasErrors()){
                LOG.debug("Error occurred while binding the request object with the project waiver bean.");
                input();
            }
        }
	}
			
	private void loadAttachments(final Long projectWaiverID)
	{
		Map<Integer,List<AttachmentBean>>  attachmentMap = this.projectWaiverManager.getDocumentAttachment(projectWaiverID);
		if(attachmentMap!=null && attachmentMap.containsKey(SynchroGlobal.SynchroAttachmentObject.WAIVER_ATTACHMENT.getId().intValue()))
		{
			attachments = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.WAIVER_ATTACHMENT.getId().intValue());
			
		}
      
		attachmentCount = attachments!=null?attachments.size():0;
		
		List<AttachmentBean> abList = new ArrayList<AttachmentBean>();
        for(Integer i : attachmentMap.keySet())
        {
            abList.addAll(attachmentMap.get(i));
        }
        
        attachmentUser = this.projectWaiverManager.getAttachmentUser(abList);
	}
	
	public void handleAttachments() {
		
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
	            	projectWaiverManager.addAttachment(attachFile[i], attachFileFileName[i], attachFileContentType[i], projectWaiverID, getUser().getID());
	            	attachments = projectWaiverManager.getFieldAttachments(projectWaiverID);	            	
	            }
	            catch (AttachmentException ae) {
	               log.error("AttachmentException - " + ae.getMessage());
	               handleAttachmentException(file, fileName, contentType, ae);
	            } catch (UnauthorizedException ue) {
	            	log.error("UnauthorizedException - " + ue.getMessage());
	            } catch (Exception e) {
	            	log.error("Exception - " + e.getMessage());
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
	
	 public ProjectWaiver getProjectWaiver() {
	        return projectWaiver;
	    }

	    public void setProjectWaiver(ProjectWaiver projectWaiver) {
	        this.projectWaiver = projectWaiver;
	    }

	    public Long getProjectWaiverID() {
	        return projectWaiverID;
	    }

	    public void setProjectWaiverID(Long projectWaiverID) {
	        this.projectWaiverID = projectWaiverID;
	    }

	    public ProjectWaiverManager getProjectWaiverManager() {
	        return projectWaiverManager;
	    }

	    public void setProjectWaiverManager(final ProjectWaiverManager projectWaiverManager) {
	        this.projectWaiverManager = projectWaiverManager;
	    }


	    public SynchroUtils getSynchroUtils() {
	        if(synchroUtils == null){
	            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
	        }
	        return synchroUtils;
	    }
	    
	    public UserManager getUserManager() {
	        if(userManager == null){
	        	userManager = JiveApplication.getContext().getSpringBean("userManager");
	        }
	        return userManager;
	    }
	    
	    public EmailManager getEmailManager() {
	        if(emailManager == null){
	        	emailManager = JiveApplication.getContext().getSpringBean("emailManager");
	        }
	        return emailManager;
	    }
	    
		public PermissionManager getPermissionManager() {
	        if(permissionManager == null){
	            permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
	        }
	        return permissionManager;
	    }

		public Boolean getIsApproved() {
			return isApproved;
		}

		public void setIsApproved(Boolean isApproved) {
			this.isApproved = isApproved;
		}

		public Boolean getIsApprover() {
			return isApprover;
		}

		public void setIsApprover(Boolean isApprover) {
			this.isApprover = isApprover;
		}

		
		public String getApproveWaiver() {
			return approveWaiver;
		}

		public void setApproveWaiver(String approveWaiver) {
			this.approveWaiver = approveWaiver;
		}

		public Map<Long, Long> getAttachmentUser() {
			return attachmentUser;
		}

		public void setAttachmentUser(Map<Long, Long> attachmentUser) {
			this.attachmentUser = attachmentUser;
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

		public Long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(Long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public List<AttachmentBean> getAttachments() {
			if(attachments!=null)
				return attachments;
			else
				return Collections.EMPTY_LIST;
		}

		public void setAttachments(List<AttachmentBean> attachments) {
			this.attachments = attachments;
		}

		public String getWaiverApproved() {
			return waiverApproved;
		}

		public void setWaiverApproved(String waiverApproved) {
			this.waiverApproved = waiverApproved;
		}

		public StageManager getStageManager() {
			return stageManager;
		}

		public void setStageManager(StageManager stageManager) {
			this.stageManager = stageManager;
		}

		public EmailNotificationManager getEmailNotificationManager() {
			return emailNotificationManager;
		}

		public void setEmailNotificationManager(
				EmailNotificationManager emailNotificationManager) {
			this.emailNotificationManager = emailNotificationManager;
		}

}
