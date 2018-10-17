package com.grail.synchro.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.beans.TPDSKUDetails;
import com.grail.synchro.dao.PIBDAONew;
import com.grail.synchro.dao.ReportSummaryDAONew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
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
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ReportSummaryManagerImplNew implements ReportSummaryManagerNew {

    private ReportSummaryDAONew reportSummaryDAONew;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;
    private PIBDAONew pibDAONew;
    private SynchroUtils synchroUtils;
    private ProfileManager profileManager;
    private UserManager userManager;


    @Override
    public List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID, final Long endMarketId)
    {
    	return reportSummaryDAONew.getReportSummaryInitiation(projectID, endMarketId);
    }
    @Override
    public List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID)
    {
    	return reportSummaryDAONew.getReportSummaryInitiation(projectID);
    }

    @Override
    public List<ReportSummaryDetails> getReportSummaryDetails(final Long projectID)
    {
    	return reportSummaryDAONew.getReportSummaryDetails(projectID);
    }
    
    @Override
    public List<ReportSummaryDetails> getReportSummaryDetails(final Long projectID, final int reportType, final int reportOrderId) 
    {
    	return reportSummaryDAONew.getReportSummaryDetails(projectID,reportType,reportOrderId);
    }
    @Override
    public Map<Integer, Map<Integer, List<Long>>> getReportSummaryAttachmentDetails(final Long projectID)
    {
    	Map<Integer, Map<Integer, List<Long>>> reportTypeReportOrder = new HashMap<Integer, Map<Integer, List<Long>>>();
    	List<ReportSummaryDetails> reportSummaryDetails = reportSummaryDAONew.getReportSummaryAttachmentDetails(projectID);
    	if(reportSummaryDetails!=null && reportSummaryDetails.size()>0)
    	{
    		
    		for(ReportSummaryDetails rsd: reportSummaryDetails) 
    		{
    			Map<Integer, List<Long>> reportOrder = new HashMap<Integer, List<Long>>();
    			if(reportTypeReportOrder.get(Integer.valueOf(rsd.getReportType()))!=null)
    			{
    				reportOrder= reportTypeReportOrder.get(Integer.valueOf(rsd.getReportType()));
    				List<Long> attachmentId = reportSummaryDAONew.getReportSummaryAttachmentIds(projectID, rsd.getReportType(), rsd.getReportOrderId());
    				reportOrder.put(Integer.valueOf(rsd.getReportOrderId()), attachmentId);
    				
    				
    				reportTypeReportOrder.put(Integer.valueOf(rsd.getReportType()), reportOrder);
    				
    				System.out.println("Checking Report Type ==>"+rsd.getReportType() + "Report Order ==> "+ reportOrder + "attachmentId ==>"+ attachmentId.get(0));
    			}
    			else
    			{
    				if(reportOrder.get(Integer.valueOf(rsd.getReportOrderId()))!=null)
    				{
    					
    				}
    				else
    				{
    					List<Long> attachmentId = reportSummaryDAONew.getReportSummaryAttachmentIds(projectID, rsd.getReportType(), rsd.getReportOrderId());
    					reportOrder.put(Integer.valueOf(rsd.getReportOrderId()), attachmentId);
    					reportTypeReportOrder.put(Integer.valueOf(rsd.getReportType()), reportOrder);
    					System.out.println("Checking Report Type ==>"+rsd.getReportType() + "Report Order ==> "+ reportOrder + "attachmentId ==>"+ attachmentId.get(0));
    				}
    			}
    			
    		}
    	}
    	
    	
    	
    	return reportTypeReportOrder;
    }
    
    @Override
    public Map<Integer, Map<Integer, List<Long>>> getReportSummaryAttachmentDetails(final Long projectID, int reportType)
    {
    	Map<Integer, Map<Integer, List<Long>>> reportTypeReportOrder = new HashMap<Integer, Map<Integer, List<Long>>>();
    	List<ReportSummaryDetails> reportSummaryDetails = reportSummaryDAONew.getReportSummaryAttachmentDetails(projectID, reportType);
    	if(reportSummaryDetails!=null && reportSummaryDetails.size()>0)
    	{
    		
    		for(ReportSummaryDetails rsd: reportSummaryDetails) 
    		{
    			Map<Integer, List<Long>> reportOrder = new HashMap<Integer, List<Long>>();
    			if(reportTypeReportOrder.get(Integer.valueOf(rsd.getReportType()))!=null)
    			{
    				reportOrder= reportTypeReportOrder.get(Integer.valueOf(rsd.getReportType()));
    				List<Long> attachmentId = reportSummaryDAONew.getReportSummaryAttachmentIds(projectID, rsd.getReportType(), rsd.getReportOrderId());
    				reportOrder.put(Integer.valueOf(rsd.getReportOrderId()), attachmentId);
    				
    				
    				reportTypeReportOrder.put(Integer.valueOf(rsd.getReportType()), reportOrder);
    				
    				System.out.println("Checking Report Type ==>"+rsd.getReportType() + "Report Order ==> "+ reportOrder + "attachmentId ==>"+ attachmentId.get(0));
    			}
    			else
    			{
    				if(reportOrder.get(Integer.valueOf(rsd.getReportOrderId()))!=null)
    				{
    					
    				}
    				else
    				{
    					List<Long> attachmentId = reportSummaryDAONew.getReportSummaryAttachmentIds(projectID, rsd.getReportType(), rsd.getReportOrderId());
    					reportOrder.put(Integer.valueOf(rsd.getReportOrderId()), attachmentId);
    					reportTypeReportOrder.put(Integer.valueOf(rsd.getReportType()), reportOrder);
    					System.out.println("Checking Report Type ==>"+rsd.getReportType() + "Report Order ==> "+ reportOrder + "attachmentId ==>"+ attachmentId.get(0));
    				}
    			}
    			
    		}
    	}
    	
    	
    	
    	return reportTypeReportOrder;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ReportSummaryInitiation saveReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation){
        this.reportSummaryDAONew.save(reportSummaryInitiation);
        return reportSummaryInitiation;
    }
   
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveReportSummaryDetailsNew(final List<ReportSummaryDetails> reportSummaryDetailsList){
        if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size()>0)
        {
        	for(ReportSummaryDetails reportSummaryDetails:reportSummaryDetailsList )
        	{
        		this.reportSummaryDAONew.saveReportSummaryDetails(reportSummaryDetails);
        		if(reportSummaryDetails.getAttachmentId()!=null && reportSummaryDetails.getAttachmentId().size()>0)
        		{
        			this.reportSummaryDAONew.saveReportSummaryAttachmentDetails(reportSummaryDetails);
        		}
        	}
        }
       
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveReportSummaryDetailsOnly(final List<ReportSummaryDetails> reportSummaryDetailsList){
        if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size()>0)
        {
        	for(ReportSummaryDetails reportSummaryDetails:reportSummaryDetailsList )
        	{
        		this.reportSummaryDAONew.saveReportSummaryDetails(reportSummaryDetails);
        		
        	}
        }
       
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveReportSummaryAttachment(final List<ReportSummaryDetails> reportSummaryDetailsList){
        if(reportSummaryDetailsList!=null && reportSummaryDetailsList.size()>0)
        {
        	for(ReportSummaryDetails reportSummaryDetails:reportSummaryDetailsList )
        	{
        		if(reportSummaryDetails.getAttachmentId()!=null && reportSummaryDetails.getAttachmentId().size()>0)
        		{
        			this.reportSummaryDAONew.saveReportSummaryAttachmentDetails(reportSummaryDetails);
        		}
        	}
        }
       
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ReportSummaryInitiation updateReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation) {
        this.reportSummaryDAONew.update(reportSummaryInitiation);
        return  reportSummaryInitiation;
    }
    
    @Override
    public long addAttachment(File attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldCategoryId, Long userId) throws IOException, AttachmentException 
    {
    	long attachmentId = 0;
        try
        {
        	Attachment att = attachmentHelper.createAttachment(
                    getRSSynchroAttachment(projectId, endMarketId, fieldCategoryId), fileName , contentType, attachment);
        	pibDAONew.saveAttachmentUser(att.getID(), userId);
        	attachmentId = att.getID();
        }
        catch (IOException e) 
        {
            throw new IOException(e.getMessage(), e);
        }
        catch (AttachmentException e) 
        {
            throw new AttachmentException(e.getMessage(), e);
        }
        return attachmentId;
    }
    @Override
    public long addAttachment(InputStream attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldCategoryId, Long userId)  
    {
    	long attachmentId = 0;
        try
        {
        	
        	Attachment att = attachmentHelper.createAttachment(
                    getRSSynchroAttachment(projectId, endMarketId, fieldCategoryId), fileName , contentType, attachment, null);
        	pibDAONew.saveAttachmentUser(att.getID(), userId);
        	attachmentId = att.getID();
        }

        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return attachmentId;
    }
    
    @Override
    public long addTPDSKUAttachment(File attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldCategoryId, Long userId, Integer rowId) throws IOException, AttachmentException 
    {
    	long attachmentId = 0;
        try
        {
        	Attachment att = attachmentHelper.createAttachment(
        			getTPDSKUSynchroAttachment(projectId, endMarketId, fieldCategoryId, rowId), fileName , contentType, attachment);
        	pibDAONew.saveAttachmentUser(att.getID(), userId);
        	attachmentId = att.getID();
        }
        catch (IOException e) 
        {
            throw new IOException(e.getMessage(), e);
        }
        catch (AttachmentException e) 
        {
            throw new AttachmentException(e.getMessage(), e);
        }
        return attachmentId;
    }
    
    @Override
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception {
    	boolean success = false;
        try
        {
            Attachment attachment = attachmentManager.getAttachment(attachmentId);
            attachmentManager.deleteAttachment(attachment);
            pibDAONew.deleteAttachmentUser(attachmentId);
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
	       	 List<AttachmentBean> psAttachments = reportSummaryDAONew.getFieldAttachments(getPSSynchroAttachment(projectId, endMakerketId, synchroAttObj.getId().longValue()));
	       	 List<AttachmentBean> pibAttachments = reportSummaryDAONew.getFieldAttachments(getPIBSynchroAttachment(projectId, endMakerketId, synchroAttObj.getId().longValue()));
	       	 List<AttachmentBean> proposalAttachments = reportSummaryDAONew.getFieldAttachments(getProposalSynchroAttachment(projectId, endMakerketId, new Long("-1"),synchroAttObj.getId().longValue()));
	       	 
	       	List<AttachmentBean> rsAttachments = reportSummaryDAONew.getFieldAttachments(getRSSynchroAttachment(projectId, endMakerketId, synchroAttObj.getId().longValue()));
	       	 
	       	 List<AttachmentBean> finalAttachments = new ArrayList<AttachmentBean>();
	       	 
	       	 if(psAttachments != null && psAttachments.size() > 0) {
	       		 finalAttachments.addAll(psAttachments);
	            }
	       	 
	       	 if(pibAttachments != null && pibAttachments.size() > 0) {
	       		 finalAttachments.addAll(pibAttachments);
	            }
	       	 
	       	 if(proposalAttachments != null && proposalAttachments.size() > 0) {
	       		 finalAttachments.addAll(proposalAttachments);
	            }
	       	 

	       	 if(rsAttachments != null && rsAttachments.size() > 0) {
	       		 finalAttachments.addAll(rsAttachments);
	            }
	       	 
	       	 if(finalAttachments != null && finalAttachments.size() > 0) {
	                attachmentMap.put(synchroAttObj.getId(), finalAttachments);
	            }
       }
        return attachmentMap;
    	
    }
    
    @Override
    public Map<Integer, List<AttachmentBean>> getTPDSKUDocumentAttachment(final Long projectId, final Long endMakerketId, List<TPDSKUDetails> tpdSKUDetails)
    {
    	Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();
        for(SynchroAttachmentObject synchroAttObj : SynchroAttachmentObject.values())
        {
        	
        	for(TPDSKUDetails tpdSKUDetail: tpdSKUDetails)
        	{
        		List<AttachmentBean> tpdSKUAttachments = reportSummaryDAONew.getFieldAttachments(getTPDSKUSynchroAttachment(projectId, endMakerketId, synchroAttObj.getId().longValue(), tpdSKUDetail.getRowId()));
        		
        		if(tpdSKUAttachments != null && tpdSKUAttachments.size() > 0)
        		{
        			List<AttachmentBean> finalAttachments = new ArrayList<AttachmentBean>();
        			finalAttachments.addAll(tpdSKUAttachments);
   	       		 	
	   	       		if(finalAttachments != null && finalAttachments.size() > 0) {
		                attachmentMap.put(tpdSKUDetail.getRowId(), finalAttachments);
		            }
   	            }
        		
        		
        	}
        	
	       	 /*
	       	 if(finalAttachments != null && finalAttachments.size() > 0) {
	                attachmentMap.put(synchroAttObj.getId(), finalAttachments);
	            }*/
       }
        return attachmentMap;
    	
    }
    
    
    private SynchroAttachment getRSSynchroAttachment(final Long projectId, final Long endMarketId, final Long fieldType) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.REPORT_SUMMARY.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    
    private SynchroAttachment getTPDSKUSynchroAttachment(final Long projectId, final Long endMarketId, final Long fieldType, Integer rowId) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId + "-" + rowId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.REPORT_SUMMARY.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    
    private SynchroAttachment getPSSynchroAttachment(final Long projectId, final Long endMarketId, final Long fieldType) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.PROJECT_SPECS.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    private SynchroAttachment getPIBSynchroAttachment(final Long projectId, final Long endMarketId, final Long fieldType) {
    	 SynchroAttachment synchroAttachment = new SynchroAttachment();
         synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId).hashCode());
         Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.PIB.toString()
                 , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
         synchroAttachment.getBean().setObjectType(objectType);
         return synchroAttachment;
    }
    private SynchroAttachment getProposalSynchroAttachment(final Long projectId, final Long endMarketId,Long agencyId, final Long fieldType) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId + "-" + agencyId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.PROPOSAL.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    
    @Override
    public void approveSPI(final User user, final Long projectId,final  Long endMarketId)
    {
    	reportSummaryDAONew.approveSPI(user,projectId,endMarketId);
    }
    @Override
    public void approveLegal(final User user, final Long projectId,final  Long endMarketId)
    {
    	reportSummaryDAONew.approveLegal(user,projectId,endMarketId);
    }
    @Override
    public void updateReportSummaryStatus(final Long projectID,final  Long endMarketId,final Integer status)
    {
    	reportSummaryDAONew.updateReportSummaryStatus(projectID,endMarketId, status);
    }
    @Override
    public void updateSendForApproval(final long projectId, final long endMarketId, final Integer sendForApproval) {
    	reportSummaryDAONew.updateSendForApproval(projectId, endMarketId,sendForApproval);
    }
    @Override
    public void updateNeedRevision(final long projectId, final long endMarketId) {
    	reportSummaryDAONew.updateNeedRevision(projectId, endMarketId);
    }
    @Override
    public void updateSendForApproval(final long projectId, final Integer sendForApproval)
    {
    	reportSummaryDAONew.updateSendForApproval(projectId, sendForApproval);
    }
    @Override
    public void approveSPI(final User user, final Long projectId)
    {
    	reportSummaryDAONew.approveSPI(user, projectId);
    }
    @Override
    public void updateFullReport(final long projectId, final long endMarketId, final Integer fullReport)
    {
    	reportSummaryDAONew.updateFullReport(projectId, endMarketId, fullReport);
    }
    @Override
    public void updateFullReport(final long projectId, final Integer fullReport)
    {
    	reportSummaryDAONew.updateFullReport(projectId, fullReport);
    }
    @Override
    public void updateSummaryForIris(final long projectId, final long endMarketId, final Integer summaryForIris)
    {
    	reportSummaryDAONew.updateSummaryForIris(projectId, endMarketId, summaryForIris);
    }
    @Override
    public void updateSummaryForIris(final long projectId, final Integer summaryForIris)
    {
    	reportSummaryDAONew.updateSummaryForIris(projectId, summaryForIris);
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
        this.reportSummaryDAONew.deleteReportSummaryDetails(projectID,endMarketID);
        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteAllReportSummaryDetails(final Long projectID) {
        this.reportSummaryDAONew.deleteAllReportSummaryDetails(projectID);
        
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteReportSummaryDetailsNew(final Long projectID) {
        this.reportSummaryDAONew.deleteReportSummaryDetailsNew(projectID);
        this.reportSummaryDAONew.deleteReportSummaryAttachDetails(projectID);        
    }
    @Override
    public void updateUploadToIRIS(final long projectId, final long endMarketId, final Integer uploadToIRIS) {
    	reportSummaryDAONew.updateUploadToIRIS(projectId, endMarketId,uploadToIRIS);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteReportSummaryRow(final Long projectID, final Integer reportType, final Integer reportOrderId){
        this.reportSummaryDAONew.deleteReportSummaryRow(projectID, reportType,reportOrderId );
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteReportSummaryAttachmentRow(final Long projectID, final Integer reportType, final Integer reportOrderId){
        this.reportSummaryDAONew.deleteReportSummaryAttachmentRow(projectID, reportType,reportOrderId );
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteReportSummaryAttachment(final Long attachmentId){
        this.reportSummaryDAONew.deleteReportSummaryAttachment(attachmentId);
    }
    
    @Override
    public void updateUploadToCPSIDatabase(final long projectId, final long endMarketId, final Integer uploadToCPSIDatabase) {
    	reportSummaryDAONew.updateUploadToCPSIDatabase(projectId, endMarketId,uploadToCPSIDatabase);
    }
    
    @Override
    public Integer getMaxReportOrderId(final Long projectID, final Integer reportType)
    {
    	return reportSummaryDAONew.getMaxReportOrderId(projectID, reportType);
    }
    
    @Override
    public Integer getMinReportOrderId(final Long projectID, final Integer reportType)
    {
    	return reportSummaryDAONew.getMinReportOrderId(projectID, reportType);
    }
    
    @Override
    public Integer getMaxReportOrderId(final Long projectID)
    {
    	return reportSummaryDAONew.getMaxReportOrderId(projectID);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateReportOrderId(final Long projectID, final Integer reportOrderId){
        this.reportSummaryDAONew.updateReportOrderId(projectID,reportOrderId);
    }
    
    
    @Override
    public SynchroToIRIS getSynchroToIRIS(final Long projectID, final Long endMarketId, Project project)
    {
    	List<SynchroToIRIS> syncroToIRISList =  reportSummaryDAONew.getSynchroToIRIS(projectID, endMarketId);
    	if(syncroToIRISList!=null && syncroToIRISList.size()>0)
    	{
    		return syncroToIRISList.get(0);
    	}
    	else
    	{
    		ProjectInitiation projectInitiation = pibDAONew.getProjectInitiation(projectID).get(0);
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
        this.reportSummaryDAONew.updateSynchroToIRIS(synchroToIRIS);
      
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
	public ReportSummaryDAONew getReportSummaryDAONew() {
		return reportSummaryDAONew;
	}
	public void setReportSummaryDAONew(ReportSummaryDAONew reportSummaryDAONew) {
		this.reportSummaryDAONew = reportSummaryDAONew;
	}
	public PIBDAONew getPibDAONew() {
		return pibDAONew;
	}
	public void setPibDAONew(PIBDAONew pibDAONew) {
		this.pibDAONew = pibDAONew;
	}

	
}
