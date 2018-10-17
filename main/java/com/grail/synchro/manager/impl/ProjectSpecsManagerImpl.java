package com.grail.synchro.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;
import com.grail.synchro.beans.PSMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectSpecsReporting;
import com.grail.synchro.dao.PIBDAO;
import com.grail.synchro.dao.ProjectSpecsDAO;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectSpecsManagerImpl implements ProjectSpecsManager {

    private ProjectSpecsDAO projectSpecsDAO;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;
    private PIBDAO pibDAO;

    @Override
    public List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID, final Long endMarketId)
    {
    	return projectSpecsDAO.getProjectSpecsInitiation(projectID, endMarketId);
    }
    @Override
    public List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID)
    {
    	return projectSpecsDAO.getProjectSpecsInitiation(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectSpecsInitiation saveProjectSpecsDetails(final ProjectSpecsInitiation projectSpecsInitiation){
        this.projectSpecsDAO.save(projectSpecsInitiation);
        this.projectSpecsDAO.saveProjectSpecsReporting(projectSpecsInitiation);
        return projectSpecsInitiation;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails){
       this.projectSpecsDAO.saveProjectSpecsEMDetails(projectSpecsEMDetails); 
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectSpecsInitiation updateProjectSpecsDetails(final ProjectSpecsInitiation projectSpecsInitiation) {
        this.projectSpecsDAO.update(projectSpecsInitiation);
        updateProjectSpecsReporting(projectSpecsInitiation);
        return  projectSpecsInitiation;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails) {
        this.projectSpecsDAO.updateProjectSpecsEMDetails(projectSpecsEMDetails);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProjectSpecsFieldWorkDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails) {
        this.projectSpecsDAO.updateProjectSpecsFieldWorkDetails(projectSpecsEMDetails);
        this.projectSpecsDAO.updateProjectSpecsEndDate(projectSpecsEMDetails.getProjectID(),projectSpecsEMDetails.getEndMarketID(),projectSpecsEMDetails.getProjectEndDateLatest());
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProjectSpecsFieldWorkDetailsMM(final ProjectSpecsEndMarketDetails projectSpecsEMDetails) {
        //this.projectSpecsDAO.updateProjectSpecsFieldWorkDetails(projectSpecsEMDetails);
    	this.projectSpecsDAO.updateProjectSpecsFieldWorkDetailsMM(projectSpecsEMDetails);
        //this.projectSpecsDAO.updateProjectSpecsEndDate(projectSpecsEMDetails.getProjectID(),projectSpecsEMDetails.getEndMarketID(),projectSpecsEMDetails.getProjectEndDateLatest());
        this.projectSpecsDAO.updateProjectSpecsEndDate(projectSpecsEMDetails.getProjectID(),projectSpecsEMDetails.getProjectEndDateLatest());
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProjectSpecsAMFinalCost(final Long projectId, final Long endMarketId, final BigDecimal aboveMarketFinalCost, final Integer aboveMarketFinalCostType)
    {
    	 this.projectSpecsDAO.updateProjectSpecsAMFinalCost(projectId, endMarketId, aboveMarketFinalCost,aboveMarketFinalCostType);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProjectSpecsEndMarketId(final Long projectId, final Long endMarketId)
    {
    	this.projectSpecsDAO.updateProjectSpecsEndMarketId(projectId,endMarketId);
    }
    
   @Override
    public ProjectSpecsReporting getProjectSpecsReporting(final Long projectID, final Long endMarketId) {
        List<ProjectSpecsReporting> reportingList = this.projectSpecsDAO.getProjectSpecsReporting(projectID,endMarketId);
        if( reportingList != null && reportingList.size() > 0){
            return reportingList.get(0);
        }
        return null;
    }
   @Override
   public ProjectSpecsEndMarketDetails getProjectSpecsEMDetails(final Long projectID, final Long endMarketId)
   {
	   List<ProjectSpecsEndMarketDetails> emDetailsList = this.projectSpecsDAO.getProjectSpecsEMDetails(projectID,endMarketId);
       if( emDetailsList != null && emDetailsList.size() > 0){
           return emDetailsList.get(0);
       }
       return null;
   }
   @Override
   public List<ProjectSpecsEndMarketDetails> getProjectSpecsEMDetails(final Long projectID)
   {
	   List<ProjectSpecsEndMarketDetails> emDetailsList = this.projectSpecsDAO.getProjectSpecsEMDetails(projectID);
       return emDetailsList;
   }

    @Override
    public ProjectSpecsInitiation updateProjectSpecsReporting(final ProjectSpecsInitiation projectSpecsInitiation) {
     
    	ProjectSpecsReporting reporting = getProjectSpecsReporting(projectSpecsInitiation.getProjectID(), projectSpecsInitiation.getEndMarketID());
        if( reporting == null){
            this.projectSpecsDAO.saveProjectSpecsReporting(projectSpecsInitiation);
        }else {
            this.projectSpecsDAO.updateProjectSpecsReporting(projectSpecsInitiation);
        }
        return projectSpecsInitiation;
    }
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBReportingSingleEndMarketId(final Long projectID,	final Long endMarketID) 
    {
    	projectSpecsDAO.updatePIBReportingSingleEndMarketId(projectID, endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBAttachmentSingleEndMarketId(final Long projectID,	final Long endMarketID) 
    {
    	projectSpecsDAO.updatePIBAttachmentSingleEndMarketId(projectID, endMarketID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteProjectSpecsDetails(final Long projectID) {
        this.projectSpecsDAO.deleteProjectSpecsDetails(projectID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteProjectSpecsDetails(final Long projectID, final Long endMarketID) {
        this.projectSpecsDAO.deleteProjectSpecsDetails(projectID, endMarketID);
    }
    
    @Override
    public HSSFWorkbook getPIBExcel(Project project, Document document)
    {
    	return null;
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
    public boolean addAttachment(InputStream attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldCategoryId, Long userId) throws  AttachmentException 
    {
    	boolean success = false;
       /* try
        {
        	Attachment att = attachmentHelper.createAttachment(
                    getSynchroAttachment(projectId, endMarketId, fieldCategoryId), fileName , contentType, attachment);
        	pibDAO.saveAttachmentUser(att.getID(), userId);
            success = true;
        }
       
        catch (AttachmentException e) 
        {
            throw new AttachmentException(e.getMessage(), e);
        }*/
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
    public void updateDocumentAttachment(Long attachmentId, Long projectId, Long updatedEndMarketId)
    {
    	long objectId = (projectId + "-" + updatedEndMarketId).hashCode();
    	pibDAO.updateDocumentAttachment(attachmentId, objectId);
    }
    
    @Override
    public Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long projectId, final Long endMakerketId)
    {
    	Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();

        // Get Business Question attachments
       /* List<AttachmentBean> bqAttachments = pibDAO.getFieldAttachments(getSynchroAttachment(projectId, endMakerketId, 0L));
        if(bqAttachments != null && bqAttachments.size() > 0) {
            attachmentMap.put(0, bqAttachments);
        }
        */
        for(SynchroAttachmentObject synchroAttObj : SynchroAttachmentObject.values())
        {
        	 List<AttachmentBean> attachments = projectSpecsDAO.getFieldAttachments(getSynchroAttachment(projectId, endMakerketId, synchroAttObj.getId().longValue()));
        	 if(attachments != null && attachments.size() > 0) {
                 attachmentMap.put(synchroAttObj.getId(), attachments);
             }
        }
        return attachmentMap;
    	/*List<FieldAttachmentBean> fieldAttachment = projectSpecsDAO.getDocumentAttachment(projectId, endMakerketId);
    	Map<Integer, List<Attachment>> attachmentMap = new HashMap<Integer, List<Attachment>>();
    	if(fieldAttachment!=null && fieldAttachment.size()>0)
    	{
    		for(FieldAttachmentBean fab:fieldAttachment)
    		{
    			if(attachmentMap.containsKey(fab.getFieldCategoryId().intValue()))
    			{
    				List<Attachment> attList = attachmentMap.get(fab.getFieldCategoryId().intValue());
    				try
    				{
    					attList.add(attachmentManager.getAttachment(fab.getAttachmentId()));
    					attachmentMap.put(fab.getFieldCategoryId().intValue(), attList);
    				}
    				catch (AttachmentNotFoundException e) 
    			    {
    			     	Log.info("Attachment Not Found -- "+ fab.getAttachmentId());
    			    }
    			}
    			else
    			{
    				List<Attachment> attList = new ArrayList<Attachment>();
    				try
    				{
    					attList.add(attachmentManager.getAttachment(fab.getAttachmentId()));
    					attachmentMap.put(fab.getFieldCategoryId().intValue(), attList);
    				}
    				catch (AttachmentNotFoundException e) 
    			    {
    			     	Log.info("Attachment Not Found -- "+ fab.getAttachmentId());
    			    }
    			}
    		}
    	}
    	return attachmentMap;*/
    }
    private SynchroAttachment getSynchroAttachment(final Long projectId, final Long endMarketId, final Long fieldType) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.PROJECT_SPECS.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    @Override
    public void approveScreener(final User user, final Long projectId, final  Long endMarketId)
    {
    	projectSpecsDAO.approveScreener(user, projectId, endMarketId);
    }
    
    @Override
    public void rejectScreener(final Long projectId, final  Long endMarketId)
    {
    	projectSpecsDAO.rejectScreener(projectId, endMarketId);
    }
    
    @Override
    public void approveQDG(final User user, final Long projectId, final  Long endMarketId)
    {
    	projectSpecsDAO.approveQDG(user, projectId, endMarketId);
    }
    @Override
    public void rejectQDG(final Long projectId, final  Long endMarketId)
    {
    	projectSpecsDAO.rejectQDG(projectId, endMarketId);
    }
    @Override
    public void approve(final User user, final Long projectId, final  Long endMarketId)
    {
    	projectSpecsDAO.approve(user, projectId, endMarketId);
    }
    @Override
    public void updateProjectSpecsStatus(final Long projectID,final  Long endMarketId,final Integer status) 
    {
    	projectSpecsDAO.updateProjectSpecsStatus(projectID, endMarketId, status);
    }
    @Override
    public void updateProjectSpecsSendForApproval(final Long projectID,final  Long endMarketId,final Integer sendForApproval)
    {
    	projectSpecsDAO.updateProjectSpecsSendForApproval(projectID, endMarketId, sendForApproval);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void savePSMethodologyWaiver(final PSMethodologyWaiver psWaiver)
    {
    	projectSpecsDAO.savePSMethodologyWaiver(psWaiver);
    	//pibDAO.updatePIBStatus(pibWaiver.getProjectID(), SynchroGlobal.StageStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
    }
    @Override
    public PSMethodologyWaiver getPSMethodologyWaiver(final Long projectID,final Long endMakerketId) {
        List<PSMethodologyWaiver> methodologyWaiverList = this.projectSpecsDAO.getPSMethodologyWaiver(projectID,endMakerketId);
        if( methodologyWaiverList != null && methodologyWaiverList.size() > 0){
            return methodologyWaiverList.get(0);
        }
        return null;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePSMethWaiverSingleEndMarketId(final Long projectID,	final Long endMarketID) 
    {
    	projectSpecsDAO.updatePSMethWaiverSingleEndMarketId(projectID, endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void approvePSMethodologyWaiver(final PSMethodologyWaiver psWaiver)
    {
    	projectSpecsDAO.approvePSMethodologyWaiver(psWaiver);
    	// Change the status of PIB to SAVED once the Methodology Waiver is approved so that the
    	// Notify Agency button should be enable after that.
    	//projectSpecsDAO.updatePIBStatus(psWaiver.getProjectID(), SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
    	
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void rejectPSMethodologyWaiver(final PSMethodologyWaiver psWaiver)
    {
    	projectSpecsDAO.rejectPSMethodologyWaiver(psWaiver);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void reqForInfoPSMethodologyWaiver(final PSMethodologyWaiver psWaiver)
    {
    	projectSpecsDAO.rejectPSMethodologyWaiver(psWaiver);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePSDeviation(final ProjectSpecsInitiation updatePSDeviation)
    {
    	projectSpecsDAO.updatePSDeviation(updatePSDeviation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateRequestClarificationModification(final Long projectID, final  Long endMarketId, final Integer reqClarification)
    {
    	projectSpecsDAO.updateRequestClarificationModification(projectID, endMarketId, reqClarification);
    }
    
    @Override
    public Boolean allPSMarketSaved(final long projectID, final int endMarketSize) {
    	List<ProjectSpecsInitiation> psList = getProjectSpecsInitiation(projectID);
    	if(psList!=null && psList.size()== endMarketSize)
    	{
    		for(ProjectSpecsInitiation ps : psList)
    		{
    			if(ps.getStatus()==SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal())
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

	public ProjectSpecsDAO getProjectSpecsDAO() {
		return projectSpecsDAO;
	}

	public void setProjectSpecsDAO(ProjectSpecsDAO projectSpecsDAO) {
		this.projectSpecsDAO = projectSpecsDAO;
	}
	public PIBDAO getPibDAO() {
		return pibDAO;
	}
	public void setPibDAO(PIBDAO pibDAO) {
		this.pibDAO = pibDAO;
	}

	
}
