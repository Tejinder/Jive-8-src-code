package com.grail.synchro.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;

/**
 * @author Kanwar Grewal
 * @version 1.0, Date: 6/5/13
 */
public class ProjectStatusAction extends JiveActionSupport implements Preparable {

    private static final Logger LOGGER = Logger.getLogger(ProjectStatusAction.class);
    
    private Long projectID;
    private ProjectManager synchroProjectManager;
    private ProposalManager proposalManager;
    private Integer projectOverallStatus;
    private final static String GlOBAL_SEPARATOR = "_";
    private Project project;
    private EmailManager emailManager;
    private StageManager stageManager;
    private Integer projectActivateStatus;
    private Integer projectInitialStatus = -1;
    private Boolean hasCompleted = false;
    private Boolean multimarket = false; 
    private List<Long> endMarketIDs = Collections.emptyList();
	private SynchroUtils synchroUtils;
	private Map<String, String> statusMap = new HashMap<String, String>();
	private Map<String, String> awardedMap = new HashMap<String, String>();
	private Map<String, String> fieldWorkMap = new HashMap<String, String>();
	private ProjectStatus projectStatus;
	private EmailNotificationManager emailNotificationManager;
	
	public void prepare() throws Exception
	{
		 final String id = getRequest().getParameter("projectID");
		  if(id != null ) {
              try{
                  projectID = Long.parseLong(id);
	                  if(projectID!=null && projectID>0)
		          		{
	                	  //Binding Endmarket Project Status
	                	  if(getRequest().getMethod() == "POST") {
	                		  this.projectStatus = new ProjectStatus();
	                		  ServletRequestDataBinder binder = new ServletRequestDataBinder(this.projectStatus);
		                      binder.bind(getRequest());
		                      if(binder.getBindingResult().hasErrors()){
		                    	  LOGGER.error("Binding error for Project Status form for End Markets");
		                      }
	                	  }
	                      	                      
		          			project = getSynchroProjectManager().get(projectID);
		          			multimarket = project.getMultiMarket();
		          			
		          			/** Fetch endmarket status **/
		          			endMarketIDs = getSynchroProjectManager().getEndMarketIDs(projectID);
		          			for(Long ID : endMarketIDs)
		          			{
		          				Integer status = getSynchroProjectManager().getEndMarketStatus(projectID, ID);
		          				if(status!=null && status>-1)
		          				{
		          					statusMap.put(ID.toString(), status.toString());
		          				}
		          			}
		          			
		          			
		          			if(multimarket)
		          			{
		          				/** Fetches if endmarket wise Proposal to an Agency are awarded or NOT **/
		          				List<ProposalInitiation> proposalList = proposalManager.getProposalDetails(projectID);
			          			for(ProposalInitiation proposal : proposalList)
			          			{
			          				awardedMap.put(proposal.getEndMarketID()+"", (proposal.getIsAwarded()!=null && proposal.getIsAwarded())?"true":"false");
			          			}
			          			
			          			for(Long ID : endMarketIDs)
			          			{
			          				fieldWorkMap.put(ID+"", getSynchroUtils().isFieldWorkCompleted(projectID, ID).toString());
			          			}
		          			}
		          		}
	                  
              } catch (NumberFormatException nfEx) {
            	  LOGGER.error("Invalid ProjectID ");
                  throw nfEx;
              }
		  }
	}
	
	public String input(){
		
//		if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
		
		if(projectID != null)
			{
				project = getSynchroProjectManager().get(projectID);
				if(project.getMultiMarket())
				{
					if(!(SynchroPermHelper.canEditMultimarketProject(getUser(), projectID)))
						return UNAUTHORIZED;
				}
				else
				{
					if(!(SynchroPermHelper.canEditproject(getUser(), projectID)))
						return UNAUTHORIZED;
				}
				
				if(!SynchroPermHelper.hasValidProjectStatus(projectID))
		    		return UNAUTHORIZED;
				
				
				
				// Update the project Status
				getSynchroUtils().updateProjectStatus(project);
				
				project = getSynchroProjectManager().get(projectID);
				
				String projectStatus = SynchroGlobal.Status.getById(project.getStatus().intValue()).name();
				
				//TODO check what following code do ?
				if(StringUtils.contains(projectStatus, GlOBAL_SEPARATOR))
				{
					String pStatusName =  StringUtils.substringAfterLast(projectStatus, GlOBAL_SEPARATOR);
					if(pStatusName.equals(SynchroGlobal.ProjectActivationStatus.OPEN.toString()) 
					|| pStatusName.equals(SynchroGlobal.ProjectActivationStatus.ONHOLD.toString())
					|| pStatusName.equals(SynchroGlobal.ProjectActivationStatus.CANCEL.toString()))
					{
						projectInitialStatus = SynchroGlobal.ProjectActivationStatus.valueOf(pStatusName).ordinal();
					}
					
				}
				
				/*Checks for Project IN-Progress drilled level status */
				if(projectStatus.equals(SynchroGlobal.ProjectStatus.INPROGRESS_PLANNING.name()))
				{
					projectOverallStatus = SynchroGlobal.ProjectStatus.INPROGRESS_PLANNING.ordinal();
					projectActivateStatus = SynchroGlobal.ProjectActivationStatus.OPEN.ordinal();
				}
				else if(projectStatus.equals(SynchroGlobal.ProjectStatus.INPROGRESS_FIELDWORK.name()))
				{
					projectOverallStatus = SynchroGlobal.ProjectStatus.INPROGRESS_FIELDWORK.ordinal();
					projectActivateStatus = SynchroGlobal.ProjectActivationStatus.OPEN.ordinal();
				} 
				else if(projectStatus.equals(SynchroGlobal.ProjectStatus.INPROGRESS_ANALYSIS.name()))
				{
					projectOverallStatus = SynchroGlobal.ProjectStatus.INPROGRESS_ANALYSIS.ordinal();
					projectActivateStatus = SynchroGlobal.ProjectActivationStatus.OPEN.ordinal();
				}
				else if(projectStatus.equals(SynchroGlobal.ProjectStatus.INPROGRESS_IRIS.name()))
				{
					projectOverallStatus = SynchroGlobal.ProjectStatus.INPROGRESS_IRIS.ordinal();
					projectActivateStatus = SynchroGlobal.ProjectActivationStatus.OPEN.ordinal();
				}
				
				//Checks if Project is completed
				else if(StringUtils.equals(projectStatus, SynchroGlobal.Status.COMPLETED.name()))
				{
					projectOverallStatus = SynchroGlobal.ProjectStatus.COMPLETED.ordinal();
					hasCompleted = true;
				}
				else if(StringUtils.equals(projectStatus, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.name()))
				{
					projectOverallStatus = SynchroGlobal.ProjectStatus.COMPLETED.ordinal();
					hasCompleted = true;
				}
				
				//Checks if Project is deleted
				else if(StringUtils.equals(projectStatus, SynchroGlobal.Status.DELETED.name()))
				{					
					Integer trackStatus = synchroProjectManager.getStatusTrack(projectID);
					if(trackStatus > 0 && StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.COMPLETED.name()))
					{
						hasCompleted = true;
					}
					projectActivateStatus = SynchroGlobal.ProjectActivationStatus.valueOf(projectStatus).ordinal();					
				}
				//Checks for all other cases
				else if(StringUtils.contains(projectStatus, GlOBAL_SEPARATOR))
				{
					String PREFIX_STATUS = StringUtils.substringBefore(projectStatus, GlOBAL_SEPARATOR);
					if(projectStatus.equals(SynchroGlobal.Status.INPROGRESS_ONHOLD.name())
							|| projectStatus.equals(SynchroGlobal.Status.INPROGRESS_CANCEL.name()))
					{
						Integer trackStatus = synchroProjectManager.getStatusTrack(projectID);
						if(trackStatus > 0)
						{
							projectOverallStatus = trackStatus;
						}
					}
					else
					{
						projectOverallStatus = SynchroGlobal.ProjectStatus.valueOf(PREFIX_STATUS).ordinal();
					}
					
					String POSTFIX_STATUS = StringUtils.substringAfterLast(projectStatus, GlOBAL_SEPARATOR);
					projectActivateStatus = SynchroGlobal.ProjectActivationStatus.valueOf(POSTFIX_STATUS).ordinal();
				}			
			}
			else
			{
				return UNAUTHORIZED;
			}
		

        return INPUT;
    }

	public String execute(){
		
		// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
		
		
    	if(!SynchroPermHelper.hasValidProjectStatus(projectID))
    		return UNAUTHORIZED;
    	
		if(projectID != null)
		{
			if(project==null)
			{
				project = getSynchroProjectManager().get(projectID);
			}
			
			if(project.getMultiMarket())
			{
				if(!(SynchroPermHelper.canEditMultimarketProject(getUser(), projectID)))
					return UNAUTHORIZED;
			}
			else
			{
				if(!(SynchroPermHelper.canEditproject(getUser(), projectID)))
					return UNAUTHORIZED;
			}
			
			Long initialStatus = project.getStatus();
			String initialStatusName = SynchroGlobal.Status.getById(initialStatus.intValue()).name();
			
			projectOverallStatus = getOverallProjectStatus(initialStatusName);
			String drilledStatus = SynchroGlobal.ProjectActivationStatus.getName(projectActivateStatus);
			
			//DELETE Operation
			if(drilledStatus.equals(SynchroGlobal.ProjectActivationStatus.DELETED.name()))
			{
				if(StringUtils.equals(initialStatusName, SynchroGlobal.Status.COMPLETED.name())
						|| StringUtils.equals(initialStatusName, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.name()))
				{
				
				}
				
				int projectStatus = SynchroGlobal.Status.valueOf(drilledStatus).ordinal();
				project.setStatus(new Long(projectStatus));
				synchroProjectManager.save(project);
				//Stores the current overall status of the project while deleting 
				synchroProjectManager.setStatusTrack(projectID, getProjectStatusForTrack(initialStatusName));
				sendEmailNotifications();
			}
			
			//OPEN operation
			else if(drilledStatus.equals(SynchroGlobal.ProjectActivationStatus.OPEN.name()))
			{
				String overallStatus = SynchroGlobal.ProjectStatus.getName(projectOverallStatus);
				//In case it was deleted previously
				if(StringUtils.equals(initialStatusName, SynchroGlobal.Status.DELETED.name()))
				{
					Integer trackStatus = synchroProjectManager.getStatusTrack(projectID);
					overallStatus = SynchroGlobal.ProjectStatus.getName(trackStatus);
				}
				
				String completeStatus = "";
				
				if(StringUtils.startsWith(initialStatusName, SynchroGlobal.ProjectStatus.INPROGRESS.name()))
				{
					Integer trackStatus = synchroProjectManager.getStatusTrack(projectID);
					completeStatus = SynchroGlobal.ProjectStatus.getName(trackStatus);
					if(StringUtils.equals(completeStatus, SynchroGlobal.ProjectStatus.INPROGRESS.name()))
					{
						completeStatus =  SynchroGlobal.Status.INPROGRESS_OPEN.name();
					}
				}
				//In case it was deleted previously
				else if(StringUtils.equals(initialStatusName, SynchroGlobal.Status.DELETED.name()))
				{
					Integer trackStatus = synchroProjectManager.getStatusTrack(projectID);
					if(StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_ANALYSIS.name())
							|| StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_FIELDWORK.name())
							|| StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_PLANNING.name())
							|| StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_IRIS.name()))
					{
						completeStatus = SynchroGlobal.ProjectStatus.getName(trackStatus);
					}
					else if(StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.Status.COMPLETED.name()))
					{
						completeStatus = SynchroGlobal.ProjectStatus.getName(trackStatus);
					}
					else
					{
						completeStatus = SynchroGlobal.ProjectStatus.getName(trackStatus) + GlOBAL_SEPARATOR + drilledStatus;	
					}
				}
				else
				{
					completeStatus = overallStatus + GlOBAL_SEPARATOR + drilledStatus;
				}
				
				int projectStatus = SynchroGlobal.Status.valueOf(completeStatus).ordinal();
				project.setStatus(new Long(projectStatus));
				synchroProjectManager.save(project);
				sendEmailNotifications();
				
				//Remove Status Track entry
				Integer projectTStatus= synchroProjectManager.getStatusTrack(projectID);
				if(projectTStatus>0 && !StringUtils.equals(SynchroGlobal.ProjectStatus.getName(projectTStatus), SynchroGlobal.ProjectStatus.COMPLETED.name()))
				{
					synchroProjectManager.deleteStatusTrack(projectID);	
				}
			}
			
			//For Cancel and On-Hold Operations
			else
			{
				String overallStatus = SynchroGlobal.ProjectStatus.getName(projectOverallStatus);
				//In case it was deleted previously
				if(StringUtils.equals(initialStatusName, SynchroGlobal.Status.DELETED.name()))
				{
					Integer trackStatus = synchroProjectManager.getStatusTrack(projectID);
					overallStatus = SynchroGlobal.ProjectStatus.getName(trackStatus);
				}
				
				String completeStatus = "";
				if(StringUtils.equals(initialStatusName, SynchroGlobal.Status.INPROGRESS_PLANNING.name())
						||StringUtils.equals(initialStatusName, SynchroGlobal.Status.INPROGRESS_FIELDWORK.name())
							||StringUtils.equals(initialStatusName, SynchroGlobal.Status.INPROGRESS_ANALYSIS.name())
								||StringUtils.equals(initialStatusName, SynchroGlobal.Status.INPROGRESS_IRIS.name())
									||StringUtils.equals(initialStatusName, SynchroGlobal.Status.INPROGRESS_OPEN.name()))
				{
					synchroProjectManager.setStatusTrack(projectID, projectOverallStatus);
					completeStatus = SynchroGlobal.ProjectStatus.INPROGRESS + GlOBAL_SEPARATOR + drilledStatus;
				}
				else
				{
					Integer trackStatus = synchroProjectManager.getStatusTrack(projectID);
					if(trackStatus>=0 && (StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_ANALYSIS.name())
							|| StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_FIELDWORK.name())
							|| StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_PLANNING.name())
							|| StringUtils.equals(SynchroGlobal.ProjectStatus.getName(trackStatus), SynchroGlobal.ProjectStatus.INPROGRESS_IRIS.name())))
					{
						completeStatus = SynchroGlobal.ProjectStatus.INPROGRESS + GlOBAL_SEPARATOR + drilledStatus;
					}
					else
					{
						completeStatus = overallStatus + GlOBAL_SEPARATOR + drilledStatus;	
					}
					
				}
				
				int projectStatus = SynchroGlobal.Status.valueOf(completeStatus).ordinal();
				project.setStatus(new Long(projectStatus));
				synchroProjectManager.save(project);
				sendEmailNotifications();
			}
		}
		else
		{
			return UNAUTHORIZED;
		}
	
		//Audit Logs
		if(projectActivateStatus!=null)
		{	         
			String i18Key = SynchroGlobal.ProjectActivationStatus.getName(projectActivateStatus);
			String i18Text = getText("logger.project.status."+ i18Key.toLowerCase());
	        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.EDIT.getId(), 
									SynchroGlobal.LogProjectStage.PROJECT_STATUS.getId(), i18Text, project.getName(), 
											project.getProjectID(), getUser().getID());
		}
		return SUCCESS;
    }
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ProjectManager getSynchroProjectManager() {
		return synchroProjectManager;
	}
	
	public void setProposalManager(ProposalManager proposalManager) {
		this.proposalManager = proposalManager;
	}

	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
		this.synchroProjectManager = synchroProjectManager;
	}
	
	public SynchroUtils getSynchroUtils() {
	        if(synchroUtils == null){
	            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
	        }
	        return synchroUtils;
	    }
	 
    public Integer getProjectOverallStatus() {
		return projectOverallStatus;
	}

	public void setProjectOverallStatus(Integer projectOverallStatus) {
		this.projectOverallStatus = projectOverallStatus;
	}

	public Integer getProjectActivateStatus() {
		return projectActivateStatus;
	}

	public void setProjectActivateStatus(Integer projectActivateStatus) {
		this.projectActivateStatus = projectActivateStatus;
	}

	public Boolean getMultimarket() {
		return multimarket;
	}
	
	public Map<String, String> getStatusMap() {
		return statusMap;
	}

	public Map<String, String> getAwardedMap() {
		return awardedMap;
	}

	public Map<String, String> getFieldWorkMap() {
		return fieldWorkMap;
	}

	public Long getProjectID() {
		return projectID;
	}

	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}
	
	public void sendEmailNotifications() 
	{
		List<User> stakeholders = getSynchroUtils().getProjectStakeHolders(project.getProjectID());
		EmailMessage message = new EmailMessage();;
		//message.setIncludeHeaderAndFooter(true);
		//TODO DON'T SEND EMAIL to REFERENCED USERS
		
		//EmailNotification#12 recipients for project change notification email
		StringBuilder userEmailList = new StringBuilder();
		Boolean isFirst = true;
		for(User user : stakeholders)
		{
			if(StringUtils.isValidEmailAddress(user.getEmail()))
			{
				if(!isFirst)
					userEmailList.append(",");
				userEmailList.append(user.getEmail());
				isFirst = false;
			}
		}
		
		if(StringUtils.isValidEmailAddress(getUser().getEmail()))
		{
			message.setSender(getUser().getName(), getUser().getEmail());
		}
		
		String projectURL =  SynchroUtils.getJiveURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID();
		
		if(projectActivateStatus!=null && SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal()==projectActivateStatus && projectInitialStatus.intValue()!=projectActivateStatus.intValue())
		{
			String subject = String.format(SynchroGlobal.EmailNotification.PROJECT_ONHOLD.getSubject(),project.getProjectID(), project.getName());
			String body = String.format(SynchroGlobal.EmailNotification.PROJECT_ONHOLD.getMessageBody(), projectURL, project.getName());
			EmailMessage email = stageManager.populateNotificationEmail(userEmailList.toString(), subject, body,"project.status.onhold.htmlBody","project.status.onhold.subject");
//			email.getContext().put("projectId", project.getProjectID());
			email.getContext().put("projectId", SynchroUtils.generateProjectCode(project.getProjectID()));
			email.getContext().put("projectName",project.getName());
			try{
				//emailManager.send(email);
				stageManager.sendNotification(getUser(),email);
				
				//Email Notification TimeStamp Storage
		    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
		    	emailNotBean.setProjectID(project.getProjectID());
		    	//emailNotBean.setEndmarketID(endMarketId);
		    //	emailNotBean.setAgencyID(agencyID);
		    	emailNotBean.setStageID(SynchroConstants.CHANGE_STATUS_STAGE);
		    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
		    	emailNotBean.setEmailDesc("Notification | Project is put On-Hold");
		    	
		    	emailNotBean.setEmailSubject("Notification | Project is put On-Hold");
		    	emailNotBean.setEmailSender(getUser().getEmail());
		    	emailNotBean.setEmailRecipients(userEmailList.toString());
		    	emailNotificationManager.saveDetails(emailNotBean);
				
			}catch(Exception e){LOGGER.error("Error sending email while changing the project status ");}
		}
		else if(projectActivateStatus!=null && SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal()==projectActivateStatus && projectInitialStatus.intValue()!=projectActivateStatus.intValue())
		{
			String subject = String.format(SynchroGlobal.EmailNotification.PROJECT_CANCELLED.getSubject(),project.getProjectID(), project.getName());
			String body = String.format(SynchroGlobal.EmailNotification.PROJECT_CANCELLED.getMessageBody(), projectURL, project.getName());
			EmailMessage email = stageManager.populateNotificationEmail(userEmailList.toString(), subject, body,"project.status.cancelled.htmlBody","project.status.cancelled.subject");
			//email.getContext().put("projectId", project.getProjectID());
			email.getContext().put("projectId", SynchroUtils.generateProjectCode(project.getProjectID()));
			email.getContext().put("projectName",project.getName());
			try{
				//emailManager.send(email);
				stageManager.sendNotification(getUser(),email);
				
				//Email Notification TimeStamp Storage
		    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
		    	emailNotBean.setProjectID(project.getProjectID());
		    	//emailNotBean.setEndmarketID(endMarketId);
		    //	emailNotBean.setAgencyID(agencyID);
		    	emailNotBean.setStageID(SynchroConstants.CHANGE_STATUS_STAGE);
		    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
		    	emailNotBean.setEmailDesc("Notification | Project is Cancelled");
		    	
		    	emailNotBean.setEmailSubject("Notification | Project is Cancelled");
		    	emailNotBean.setEmailSender(getUser().getEmail());
		    	emailNotBean.setEmailRecipients(userEmailList.toString());
		    	emailNotificationManager.saveDetails(emailNotBean);
		    	
			}catch(Exception e){LOGGER.error("Error sending email while changing the project status ");}
		}
		else if(projectActivateStatus!=null && SynchroGlobal.ProjectActivationStatus.DELETED.ordinal()==projectActivateStatus.intValue())
		{
			String subject = String.format(SynchroGlobal.EmailNotification.PROJECT_DELETED.getSubject(),project.getProjectID(), project.getName());
			String body = String.format(SynchroGlobal.EmailNotification.PROJECT_DELETED.getMessageBody(), projectURL, project.getName());
			EmailMessage email = stageManager.populateNotificationEmail(userEmailList.toString(), subject, body,"project.status.deleted.htmlBody","project.status.deleted.subject");
			//email.getContext().put("projectId", project.getProjectID());
			email.getContext().put("projectId", SynchroUtils.generateProjectCode(project.getProjectID()));
			email.getContext().put("projectName",project.getName());
			try{
				//emailManager.send(email);
				stageManager.sendNotification(getUser(),email);
				
				//Email Notification TimeStamp Storage
		    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
		    	emailNotBean.setProjectID(project.getProjectID());
		    	//emailNotBean.setEndmarketID(endMarketId);
		    //	emailNotBean.setAgencyID(agencyID);
		    	emailNotBean.setStageID(SynchroConstants.CHANGE_STATUS_STAGE);
		    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
		    	emailNotBean.setEmailDesc("Notification | Project is Deleted");
		    	
		    	emailNotBean.setEmailSubject("Notification | Project is Deleted");
		    	emailNotBean.setEmailSender(getUser().getEmail());
		    	emailNotBean.setEmailRecipients(userEmailList.toString());
		    	emailNotificationManager.saveDetails(emailNotBean);
		    	
			}catch(Exception e){LOGGER.error("Error sending email while changing the project status ");}
		}
		//else if(projectActivateStatus!=null && SynchroGlobal.ProjectActivationStatus.OPEN.ordinal()==projectActivateStatus && projectInitialStatus==SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal())
		else if(projectActivateStatus!=null && SynchroGlobal.ProjectActivationStatus.OPEN.ordinal()==projectActivateStatus && projectInitialStatus.intValue()!=projectActivateStatus.intValue())

		{
			String subject = String.format(SynchroGlobal.EmailNotification.PROJECT_REOPENED.getSubject(),project.getProjectID(), project.getName());
			String body = String.format(SynchroGlobal.EmailNotification.PROJECT_REOPENED.getMessageBody(), projectURL, project.getName());
			EmailMessage email = stageManager.populateNotificationEmail(userEmailList.toString(), subject, body,"project.status.reactivated.htmlBody","project.status.reactivated.subject");
			//email.getContext().put("projectId", project.getProjectID());
			email.getContext().put("projectId", SynchroUtils.generateProjectCode(project.getProjectID()));
			email.getContext().put("projectName",project.getName());
			try{
				//emailManager.send(email);
				stageManager.sendNotification(getUser(),email);
				
				//Email Notification TimeStamp Storage
		    	EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
		    	emailNotBean.setProjectID(project.getProjectID());
		    	//emailNotBean.setEndmarketID(endMarketId);
		    //	emailNotBean.setAgencyID(agencyID);
		    	emailNotBean.setStageID(SynchroConstants.CHANGE_STATUS_STAGE);
		    	emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
		    	emailNotBean.setEmailDesc("Notification | Project is Re-Opened");
		    	
		    	emailNotBean.setEmailSubject("Notification | Project is Re-Opened");
		    	emailNotBean.setEmailSender(getUser().getEmail());
		    	emailNotBean.setEmailRecipients(userEmailList.toString());
		    	emailNotificationManager.saveDetails(emailNotBean);
		    	
		    	
			}catch(Exception e){LOGGER.error("Error sending email while changing the project status ");}
		}
		
	}
	
	public StageManager getStageManager() {
		return stageManager;
	}
	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}
	public List<Long> getEndMarketIDs() {
		return endMarketIDs;
	}
	public Boolean getHasCompleted() {
		return hasCompleted;
	}
	public EmailManager getEmailManager() {
        if(emailManager == null){
        	emailManager = JiveApplication.getContext().getSpringBean("emailManager");
        }
        return emailManager;
    }
	private Integer getOverallProjectStatus(String projectStatus)
	{
		Integer overallStatus = 1;
		
		if(StringUtils.equals(projectStatus, SynchroGlobal.Status.COMPLETED.name()))
		{
			overallStatus = SynchroGlobal.ProjectStatus.COMPLETED.ordinal();
		}
		else if(StringUtils.equals(projectStatus, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.name()))
		{
			overallStatus = SynchroGlobal.ProjectStatus.COMPLETED.ordinal();
		}
		else if(StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_PLANNING.name())
				||StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_FIELDWORK.name())
						||StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_ANALYSIS.name())
								||StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_IRIS.name()))
								{
									overallStatus = SynchroGlobal.ProjectStatus.valueOf(projectStatus).ordinal();
								}
		
		else if(StringUtils.contains(projectStatus, GlOBAL_SEPARATOR))
		{
			String status = StringUtils.substringBefore(projectStatus, GlOBAL_SEPARATOR);
			overallStatus = SynchroGlobal.ProjectStatus.valueOf(status).ordinal();			
		}	

		return overallStatus;
		
	}
	
	private Integer getProjectStatusForTrack(String projectStatus)
	{
		Integer overallStatus = 1;
		
		if(StringUtils.equals(projectStatus, SynchroGlobal.Status.COMPLETED.name()))
		{
			overallStatus = SynchroGlobal.ProjectStatus.COMPLETED.ordinal();
		}
		else if(StringUtils.equals(projectStatus, SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.name()))
		{
			overallStatus = SynchroGlobal.ProjectStatus.COMPLETED.ordinal();
		}
		else if(StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_PLANNING.name())
				||StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_FIELDWORK.name())
						||StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_ANALYSIS.name())
								||StringUtils.equals(projectStatus, SynchroGlobal.Status.INPROGRESS_IRIS.name()))
								{
									overallStatus = SynchroGlobal.ProjectStatus.valueOf(projectStatus).ordinal();
								}
		
		else if(StringUtils.contains(projectStatus, GlOBAL_SEPARATOR))
		{
			
			if(StringUtils.startsWith(projectStatus, SynchroGlobal.ProjectStatus.INPROGRESS.name()))
			{
				Integer statusTrack = synchroProjectManager.getStatusTrack(projectID);
				if(statusTrack>0)
				{
					overallStatus = statusTrack;
				}
				else
				{
					String status = StringUtils.substringBefore(projectStatus, GlOBAL_SEPARATOR);
					overallStatus = SynchroGlobal.ProjectStatus.valueOf(status).ordinal();
				}
			}
			else
			{
				String status = StringUtils.substringBefore(projectStatus, GlOBAL_SEPARATOR);
				overallStatus = SynchroGlobal.ProjectStatus.valueOf(status).ordinal();
			}
		}	

		return overallStatus;
		
	}
	
	public String update()
	{
		// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
		
		
    	if(!SynchroPermHelper.hasValidProjectStatus(projectID))
    		return UNAUTHORIZED;
    	
		if(projectID != null)
		{
			project = getSynchroProjectManager().get(projectID);
			if(project.getMultiMarket())
			{
				if(!(SynchroPermHelper.canEditMultimarketProject(getUser(), projectID)))
					return UNAUTHORIZED;
			}
			else
			{
				if(!(SynchroPermHelper.canEditproject(getUser(), projectID)))
					return UNAUTHORIZED;
			}
			endMarketIDs = getSynchroProjectManager().getEndMarketIDs(projectID);
  			for(Long eID : endMarketIDs)
  			{
  				Integer index  = this.projectStatus.endMarketID.indexOf(eID);
  				if(index>-1)
  				{
  					getSynchroProjectManager().setEndMarketStatus(projectID, eID, this.projectStatus.getStatus().get(index));
  				}
  			}
		}
		else
		{
			return UNAUTHORIZED;
		}
	
		return SUCCESS;
	}
	
	
	public class ProjectStatus{
		
		List<Long> endMarketID;
		List<Integer> status;
		
		public List<Long> getEndMarketID() {
			return endMarketID;
		}
		public void setEndMarketID(List<Long> endMarketID) {
			this.endMarketID = endMarketID;
		}
		public List<Integer> getStatus() {
			return status;
		}
		public void setStatus(List<Integer> status) {
			this.status = status;
		}
	}


	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}
	
	
	
}

	