package com.grail.synchro.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.dao.PIBDAO;
import com.grail.synchro.dao.ReportSummaryDAO;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ReportSummaryManagerImpl implements ReportSummaryManager {

    private ReportSummaryDAO reportSummaryDAO;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;
    private PIBDAO pibDAO;
    private SynchroUtils synchroUtils;
    private ProfileManager profileManager;
    private UserManager userManager;


    @Override
    public List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID, final Long endMarketId)
    {
    	return reportSummaryDAO.getReportSummaryInitiation(projectID, endMarketId);
    }
    @Override
    public List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID)
    {
    	return reportSummaryDAO.getReportSummaryInitiation(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ReportSummaryInitiation saveReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation){
        this.reportSummaryDAO.save(reportSummaryInitiation);
        return reportSummaryInitiation;
    }
   

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ReportSummaryInitiation updateReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation) {
        this.reportSummaryDAO.update(reportSummaryInitiation);
        return  reportSummaryInitiation;
    }
    
    @Override
    public boolean addAttachment(File attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldCategoryId, Long userId) throws IOException, AttachmentException 
    {
    	boolean success = false;
        try
        {
        	Attachment att = attachmentHelper.createAttachment(
                    getSynchroAttachment(projectId, endMarketId, fieldCategoryId), fileName , contentType, attachment);
        	pibDAO.saveAttachmentUser(att.getID(), userId);
            success = true;
        }
        catch (IOException e) 
        {
            throw new IOException(e.getMessage(), e);
        }
        catch (AttachmentException e) 
        {
            throw new AttachmentException(e.getMessage(), e);
        }
        return success;
    }
    @Override
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception {
    	boolean success = false;
        try
        {
            Attachment attachment = attachmentManager.getAttachment(attachmentId);
            attachmentManager.deleteAttachment(attachment);
            pibDAO.deleteAttachmentUser(attachmentId);
            success=true;
        } 
        catch (AttachmentNotFoundException e) 
        {
            throw new AttachmentNotFoundException(e.getMessage());
        }
        catch (AttachmentException e) 
        {
        	throw new AttachmentException(e.getMessage());
        }
        catch (Exception e) 
        {
            throw new Exception(e.getMessage(), e);
        }
        return success;
    }
    @Override
    public Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long projectId, final Long endMakerketId)
    {
    	Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();
        for(SynchroAttachmentObject synchroAttObj : SynchroAttachmentObject.values())
        {
        	 List<AttachmentBean> attachments = reportSummaryDAO.getFieldAttachments(getSynchroAttachment(projectId, endMakerketId, synchroAttObj.getId().longValue()));
        	 if(attachments != null && attachments.size() > 0) {
                 attachmentMap.put(synchroAttObj.getId(), attachments);
             }
        }
        return attachmentMap;
    	
    }
    private SynchroAttachment getSynchroAttachment(final Long projectId, final Long endMarketId, final Long fieldType) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.REPORT_SUMMARY.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    @Override
    public void approveSPI(final User user, final Long projectId,final  Long endMarketId)
    {
    	reportSummaryDAO.approveSPI(user,projectId,endMarketId);
    }
    @Override
    public void approveLegal(final User user, final Long projectId,final  Long endMarketId)
    {
    	reportSummaryDAO.approveLegal(user,projectId,endMarketId);
    }
    @Override
    public void updateReportSummaryStatus(final Long projectID,final  Long endMarketId,final Integer status)
    {
    	reportSummaryDAO.updateReportSummaryStatus(projectID,endMarketId, status);
    }
    @Override
    public void updateSendForApproval(final long projectId, final long endMarketId, final Integer sendForApproval) {
    	reportSummaryDAO.updateSendForApproval(projectId, endMarketId,sendForApproval);
    }
    @Override
    public void updateNeedRevision(final long projectId, final long endMarketId) {
    	reportSummaryDAO.updateNeedRevision(projectId, endMarketId);
    }
    @Override
    public void updateSendForApproval(final long projectId, final Integer sendForApproval)
    {
    	reportSummaryDAO.updateSendForApproval(projectId, sendForApproval);
    }
    @Override
    public void approveSPI(final User user, final Long projectId)
    {
    	reportSummaryDAO.approveSPI(user, projectId);
    }
    @Override
    public void updateFullReport(final long projectId, final long endMarketId, final Integer fullReport)
    {
    	reportSummaryDAO.updateFullReport(projectId, endMarketId, fullReport);
    }
    @Override
    public void updateFullReport(final long projectId, final Integer fullReport)
    {
    	reportSummaryDAO.updateFullReport(projectId, fullReport);
    }
    @Override
    public void updateSummaryForIris(final long projectId, final long endMarketId, final Integer summaryForIris)
    {
    	reportSummaryDAO.updateSummaryForIris(projectId, endMarketId, summaryForIris);
    }
    @Override
    public void updateSummaryForIris(final long projectId, final Integer summaryForIris)
    {
    	reportSummaryDAO.updateSummaryForIris(projectId, summaryForIris);
    }
    @Override
    public Boolean allRepSummaryMarketSaved(final long projectID, final int endMarketSize) {
    	List<ReportSummaryInitiation> rsList = getReportSummaryInitiation(projectID);
    	if(rsList!=null && rsList.size()== endMarketSize)
    	{
    		for(ReportSummaryInitiation rs : rsList)
    		{
    			if(rs.getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal())
    			{
    				return false;
    			}
    		}
    	}
    	else
    	{
    		return false;
    	}
    	return true;

    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteReportSummaryDetails(final Long projectID, final Long endMarketID) {
        this.reportSummaryDAO.deleteReportSummaryDetails(projectID,endMarketID);
        
    }
    
    @Override
    public void updateUploadToIRIS(final long projectId, final long endMarketId, final Integer uploadToIRIS) {
    	reportSummaryDAO.updateUploadToIRIS(projectId, endMarketId,uploadToIRIS);
    }
 
    @Override
    public void updateUploadToCPSIDatabase(final long projectId, final long endMarketId, final Integer uploadToCPSIDatabase) {
    	reportSummaryDAO.updateUploadToCPSIDatabase(projectId, endMarketId,uploadToCPSIDatabase);
    }
    
    @Override
    public SynchroToIRIS getSynchroToIRIS(final Long projectID, final Long endMarketId, Project project)
    {
    	List<SynchroToIRIS> syncroToIRISList =  reportSummaryDAO.getSynchroToIRIS(projectID, endMarketId);
    	if(syncroToIRISList!=null && syncroToIRISList.size()>0)
    	{
    		return syncroToIRISList.get(0);
    	}
    	else
    	{
    		ProjectInitiation projectInitiation = pibDAO.getProjectInitiation(projectID).get(0);
    		SynchroToIRIS synchroToIRIS = new SynchroToIRIS();
    		synchroToIRIS.setProjectID(projectInitiation.getProjectID());
    		synchroToIRIS.setEndMarketId(projectInitiation.getEndMarketID());
    		synchroToIRIS.setBrand(project.getBrand());
    		synchroToIRIS.setProjectDesc(project.getDescription());
    		synchroToIRIS.setResearchObjective(projectInitiation.getResearchObjective());
    		synchroToIRIS.setBizQuestion(projectInitiation.getBizQuestion());
    		synchroToIRIS.setActionStandard(projectInitiation.getActionStandard());
    		synchroToIRIS.setResearchDesign(projectInitiation.getResearchDesign());
    		synchroToIRIS.setMethodologyType(project.getMethodologyType());
    		synchroToIRIS.setMethodologyGroup(project.getMethodologyGroup());
    		synchroToIRIS.setProjectName(project.getName());
    		synchroToIRIS.setBatPrimaryContact(project.getProjectOwner());
    		Long awardedExternalAgency = synchroUtils.getAwardedExternalAgencyUserID(projectID, endMarketId);
    		if(awardedExternalAgency!=null)
    		{
    			//synchroToIRIS.setResearchAgency(awardedExternalAgency);
    			synchroToIRIS.setSummaryWrittenBy(awardedExternalAgency);
    			
    			if(project.getAgencyDept() != null && project.getAgencyDept().intValue() > 0) {
    				synchroToIRIS.setResearchAgency(SynchroGlobal.getDepartmentNameById(project.getAgencyDept().toString()));
                    
                } else {
                    try
                    {
	                	Map<Long, ProfileFieldValue> profileFieldMap = profileManager.getProfile(userManager.getUser(awardedExternalAgency));
	                    if(profileFieldMap!=null && profileFieldMap.get(2L)!=null) {
	                        String deptVal = profileFieldMap.get(2L).getValue();
	                        synchroToIRIS.setResearchAgency(SynchroGlobal.getDepartmentNameById(deptVal));
	                    } else {
	                    	synchroToIRIS.setResearchAgency("Not Defined");
	                    }
                    }
                    catch(UserNotFoundException ue)
                    {
                    	Log.error("Agency User Not Found --"+ awardedExternalAgency);
                    	synchroToIRIS.setResearchAgency("");
                    }
                }
    		}
    		
    		return synchroToIRIS;
    	}
    		
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateSynchroToIRIS(final SynchroToIRIS synchroToIRIS){
        this.reportSummaryDAO.updateSynchroToIRIS(synchroToIRIS);
      
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


	public ReportSummaryDAO getReportSummaryDAO() {
		return reportSummaryDAO;
	}

	public void setReportSummaryDAO(ReportSummaryDAO reportSummaryDAO) {
		this.reportSummaryDAO = reportSummaryDAO;
	}
	public PIBDAO getPibDAO() {
		return pibDAO;
	}
	public void setPibDAO(PIBDAO pibDAO) {
		this.pibDAO = pibDAO;
	}
	public SynchroUtils getSynchroUtils() {
		return synchroUtils;
	}
	public void setSynchroUtils(SynchroUtils synchroUtils) {
		this.synchroUtils = synchroUtils;
	}
	public ProfileManager getProfileManager() {
		return profileManager;
	}
	public void setProfileManager(ProfileManager profileManager) {
		this.profileManager = profileManager;
	}
	public UserManager getUserManager() {
		return userManager;
	}
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	
}
