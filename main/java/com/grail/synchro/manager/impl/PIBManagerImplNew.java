package com.grail.synchro.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.dao.PIBDAONew;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: vivek
 * @since: 1.0
 */
public class PIBManagerImplNew implements PIBManagerNew {

    private PIBDAONew pibDAONew;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;
    private ProjectManagerNew synchroProjectManagerNew;
  

    @Override
    public List<ProjectInitiation> getPIBDetails(final long projectID) {
       return this.pibDAONew.getProjectInitiation(projectID);
    }
    
    @Override
    public List<ProjectInitiation> getPIBDetailsNew(final Long projectID) {
       return this.pibDAONew.getProjectInitiationNew(projectID);
    }
  
    @Override
    public List<ProjectInitiation> getProjectInitiationPendingActivities(final ProjectResultFilter projectFilter) {
       return this.pibDAONew.getProjectInitiationPendingActivities(projectFilter);
    }
    @Override
    public List<ProjectInitiation> getPIBDetails(final long projectID, final Long endMarketID) {
       return this.pibDAONew.getProjectInitiation(projectID,endMarketID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectInitiation savePIBDetails(final ProjectInitiation projectInitiation) {
        this.pibDAONew.save(projectInitiation);
      //  this.pibDAO.savePIBReporting(projectInitiation);
      //  this.pibDAO.savePIBStakeholderList(projectInitiation);
       // updatePIBDetails(projectInitiation);
        return projectInitiation;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectInitiation saveMultiPIBDetails(final ProjectInitiation projectInitiation, List<EndMarketInvestmentDetail> endMarketDetails, boolean isAboveMarket, boolean canSaveOtherEndMarkets) {
        this.pibDAONew.save(projectInitiation);
        // This is done for saving the Business Question and Research Objective for other End Markets
       if(isAboveMarket && canSaveOtherEndMarkets)
       {
	        for(EndMarketInvestmentDetail emd:endMarketDetails)
	        {
	        	ProjectInitiation pi = new ProjectInitiation();
	        	pi.setCreationBy(projectInitiation.getCreationBy());
	        	pi.setCreationDate(projectInitiation.getCreationDate());
	           	pi.setModifiedBy(projectInitiation.getModifiedBy());
	        	pi.setModifiedDate(projectInitiation.getModifiedDate());
	        	pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
	        	pi.setDeviationFromSM(new Integer("0"));
	        	pi.setEndMarketID(emd.getEndMarketID());
	        	pi.setResearchObjective(projectInitiation.getResearchObjective());
	        	pi.setBizQuestion(projectInitiation.getBizQuestion());
	        	pi.setProjectID(projectInitiation.getProjectID());
	        	
	        	pi.setActionStandard(projectInitiation.getActionStandard());
	        	pi.setResearchDesign(projectInitiation.getResearchDesign());
	        	pi.setSampleProfile(projectInitiation.getSampleProfile());
	        	pi.setStimulusMaterial(projectInitiation.getStimulusMaterial());        	
	        	pi.setNpiReferenceNo(projectInitiation.getNpiReferenceNo());
	        	pi.setStimuliDate(projectInitiation.getStimuliDate());
	        	
	        	pi.setTopLinePresentation(projectInitiation.getTopLinePresentation());
	        	pi.setPresentation(projectInitiation.getPresentation());
	        	pi.setFullreport(projectInitiation.getFullreport());
	        	pi.setGlobalSummary(projectInitiation.getGlobalSummary());
	        	pi.setOtherReportingRequirements(projectInitiation.getOtherReportingRequirements());
	        	pi.setOthers(projectInitiation.getOthers());
	        	
	        //	pi.setGlobalLegalContact(projectInitiation.getGlobalLegalContact());
	      
	       /* 	if(projectInitiation.getBizQuestion()!=null && !projectInitiation.getBizQuestion().equals("")
                        && projectInitiation.getResearchObjective()!=null && !projectInitiation.getResearchObjective().equals("")
                        && projectInitiation.getActionStandard()!=null && !projectInitiation.getActionStandard().equals("")
                        && projectInitiation.getResearchDesign()!=null && !projectInitiation.getResearchDesign().equals("")
                        && projectInitiation.getSampleProfile()!=null && !projectInitiation.getSampleProfile().equals("")
                        && projectInitiation.getStimulusMaterial()!=null && !projectInitiation.getStimulusMaterial().equals("")
                        && (projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation()|| projectInitiation.getPresentation()!=null && projectInitiation.getPresentation()
                        || projectInitiation.getFullreport()!=null && projectInitiation.getFullreport() || projectInitiation.getGlobalSummary()!=null && projectInitiation.getGlobalSummary())
                
                        && projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)
	        	{
	        		pi.setStatus(SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
	        	}
	        	else
	        	{
	        		pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
	        	}
	        	*/
	        	
	        /*	this.pibDAO.updatePIBBusinessQuestion(pi);
	        	this.pibDAO.updatePIBResearchObjective(pi);
	        	this.pibDAO.updatePIBActionStandard(pi);
	        	this.pibDAO.updatePIBResearchDesign(pi);
	        	this.pibDAO.updatePIBSampleProfile(pi);
	        	this.pibDAO.updatePIBStimulusMaterial(pi);*/
	        	
	        	this.pibDAONew.updateMultiMarketOtherEM(pi);
	        	updatePIBMultiMarketReporting(pi);
	        	//this.pibDAO.updatePIBMultiMarketLegalContact(pi);
	        	
	        }
       }
        this.pibDAONew.savePIBReporting(projectInitiation);
        this.pibDAONew.savePIBStakeholderList(projectInitiation);
       // updatePIBDetails(projectInitiation);
        return projectInitiation;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectInitiation updatePIBDetails(final ProjectInitiation projectInitiation, Boolean isProposalAwarded,Boolean isAdminUser) {
        this.pibDAONew.update(projectInitiation);        
        updatePIBReporting(projectInitiation);
      // If Proposal is awarded then don't update the External Agencies
       //https://www.svn.sourcen.com/issues/19286
        if(isAdminUser)
        {
        	updatePIBStakeholderList(projectInitiation);
        }
        else if(isProposalAwarded != null && isProposalAwarded)
        {
        	updatePIBNonAgencyStakeholderList(projectInitiation);
        }
        else
        {
        	updatePIBStakeholderList(projectInitiation);
        }
        
       // updatePIBStimulus(projectInitiation);
        return  projectInitiation;
    }
  
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectInitiation updatePIBDetailsNew(final ProjectInitiation projectInitiation) {
        this.pibDAONew.updateNew(projectInitiation);        
      
        return  projectInitiation;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectInitiation updateMultiMarketPIBDetails(final ProjectInitiation projectInitiation,List<EndMarketInvestmentDetail> endMarketDetails, boolean isAboveMarket,  boolean canSaveOtherEndMarkets) {
        this.pibDAONew.updateMultiMarket(projectInitiation);
        
        // This is done for saving the Business Question and Research Objective for other End Markets
        if(isAboveMarket && canSaveOtherEndMarkets)
        {
	        for(EndMarketInvestmentDetail emd:endMarketDetails)
	        {
	        	ProjectInitiation pi = new ProjectInitiation();
	        	pi.setCreationBy(projectInitiation.getCreationBy());
	        	pi.setCreationDate(projectInitiation.getCreationDate());
	           	pi.setModifiedBy(projectInitiation.getModifiedBy());
	        	pi.setModifiedDate(projectInitiation.getModifiedDate());
	        	pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
	        	pi.setDeviationFromSM(new Integer("0"));
	        	pi.setEndMarketID(emd.getEndMarketID());
	        	pi.setProjectID(projectInitiation.getProjectID());
	        	pi.setResearchObjective(projectInitiation.getResearchObjective());
	        	pi.setBizQuestion(projectInitiation.getBizQuestion());
	        	
	        	pi.setActionStandard(projectInitiation.getActionStandard());
	        	pi.setResearchDesign(projectInitiation.getResearchDesign());
	        	pi.setSampleProfile(projectInitiation.getSampleProfile());
	        	pi.setStimulusMaterial(projectInitiation.getStimulusMaterial());        	
	        	
	        	pi.setNpiReferenceNo(projectInitiation.getNpiReferenceNo());
	        	pi.setStimuliDate(projectInitiation.getStimuliDate());
	        	
	        	pi.setTopLinePresentation(projectInitiation.getTopLinePresentation());
	        	pi.setPresentation(projectInitiation.getPresentation());
	        	pi.setFullreport(projectInitiation.getFullreport());
	        	pi.setGlobalSummary(projectInitiation.getGlobalSummary());
	        	pi.setOtherReportingRequirements(projectInitiation.getOtherReportingRequirements());
	        	pi.setOthers(projectInitiation.getOthers());
	        //	pi.setGlobalLegalContact(projectInitiation.getGlobalLegalContact());
	        	
	        	   //Text Fields Copy
	        	pi.setBizQuestionText(projectInitiation.getBizQuestionText());
	        	pi.setResearchObjectiveText(projectInitiation.getResearchObjectiveText());
	        	pi.setActionStandardText(projectInitiation.getActionStandardText());
	        	pi.setResearchDesignText(projectInitiation.getResearchDesignText());
	        	pi.setSampleProfileText(projectInitiation.getSampleProfileText());
	        	pi.setStimulusMaterialText(projectInitiation.getStimulusMaterialText());
	        	pi.setOthersText(projectInitiation.getOthersText());
	        	pi.setOtherReportingRequirementsText(projectInitiation.getOtherReportingRequirementsText());
                		
	        	
	        /*	if(projectInitiation.getBizQuestion()!=null && !projectInitiation.getBizQuestion().equals("")
                        && projectInitiation.getResearchObjective()!=null && !projectInitiation.getResearchObjective().equals("")
                        && projectInitiation.getActionStandard()!=null && !projectInitiation.getActionStandard().equals("")
                        && projectInitiation.getResearchDesign()!=null && !projectInitiation.getResearchDesign().equals("")
                        && projectInitiation.getSampleProfile()!=null && !projectInitiation.getSampleProfile().equals("")
                        && projectInitiation.getStimulusMaterial()!=null && !projectInitiation.getStimulusMaterial().equals("")
                        && (projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation()|| projectInitiation.getPresentation()!=null && projectInitiation.getPresentation()
                        || projectInitiation.getFullreport()!=null && projectInitiation.getFullreport() || projectInitiation.getGlobalSummary()!=null && projectInitiation.getGlobalSummary())
                
                        && projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)
	        	{
	        		pi.setStatus(SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
	        	}
	        	else
	        	{
	        		pi.setStatus(SynchroGlobal.StageStatus.PIB_STARTED.ordinal());
	        	}
	        	*/
	        	
	        	/*this.pibDAO.updatePIBBusinessQuestion(pi);
	        	this.pibDAO.updatePIBResearchObjective(pi);
	        	this.pibDAO.updatePIBActionStandard(pi);
	        	this.pibDAO.updatePIBResearchDesign(pi);
	        	this.pibDAO.updatePIBSampleProfile(pi);
	        	this.pibDAO.updatePIBStimulusMaterial(pi);*/  
	        	this.pibDAONew.updateMultiMarketOtherEM(pi);
	        	updatePIBMultiMarketReporting(pi);
	        //	this.pibDAO.updatePIBMultiMarketLegalContact(pi);
	        	
	        }
        }
        updatePIBMultiMarketReporting(projectInitiation);
       // updatePIBStakeholderList(projectInitiation);
        updatePIBMultiMarketStakeholderList(projectInitiation);
        return  projectInitiation;
    }

   @Override
    public PIBReporting getPIBReporting(final Long projectID) {
        List<PIBReporting> reportingList = this.pibDAONew.getPIBReporting(projectID);
        if( reportingList != null && reportingList.size() > 0){
            return reportingList.get(0);
        }
        return null;
    }
   
   @Override
   public PIBReporting getPIBReporting(final Long projectID, final Long endMarketId) {
       List<PIBReporting> reportingList = this.pibDAONew.getPIBReporting(projectID,endMarketId);
       if( reportingList != null && reportingList.size() > 0){
           return reportingList.get(0);
       }
       return null;
   }
    @Override
    public ProjectInitiation updatePIBReporting(final ProjectInitiation projectInitiation) {
        PIBReporting reporting = getPIBReporting(projectInitiation.getProjectID());
        if( reporting == null){
            this.pibDAONew.savePIBReporting(projectInitiation);
        }else {
            this.pibDAONew.updatePIBReporting(projectInitiation);
        }
        return projectInitiation;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    /**
     * This method is used for updating the End Markets as per https://svn.sourcen.com/issues/19505
     */
    public void updateMultiMarketOtherEM(final ProjectInitiation projectInitiation)
    {
    	this.pibDAONew.updateMultiMarketOtherEM(projectInitiation);
    	updatePIBMultiMarketReporting(projectInitiation);
    }
    
    @Override
    public void updatePIBMultiMarketReporting(final ProjectInitiation projectInitiation) {
        PIBReporting reporting = getPIBReporting(projectInitiation.getProjectID(), projectInitiation.getEndMarketID());
        if( reporting == null){
            this.pibDAONew.savePIBReporting(projectInitiation);
        }else {
            this.pibDAONew.updatePIBMultiMarketReporting(projectInitiation);
        }
       // return projectInitiation;
    }
    @Override
    public ProjectInitiation updatePIBStakeholderList(final ProjectInitiation projectInitiation) {
        PIBStakeholderList stakeholderList = getPIBStakeholderList(projectInitiation.getProjectID(),projectInitiation.getEndMarketID());
        if( stakeholderList == null){
            this.pibDAONew.savePIBStakeholderList(projectInitiation);
        }else {
            this.pibDAONew.updatePIBStakeholderList(projectInitiation);
        }
        return projectInitiation;
    }
    
    private void updatePIBNonAgencyStakeholderList(final ProjectInitiation projectInitiation) 
    {
    	this.pibDAONew.updatePIBNonAgencyStakeholderList(projectInitiation);
    }
    
    @Override
    public void updatePIBMultiMarketStakeholderList(final ProjectInitiation projectInitiation) {
        PIBStakeholderList stakeholderList = getPIBStakeholderList(projectInitiation.getProjectID(),projectInitiation.getEndMarketID());
        if( stakeholderList == null){
            this.pibDAONew.savePIBStakeholderList(projectInitiation);
        }else {
            this.pibDAONew.updatePIBMultiMarketStakeholderList(projectInitiation);
        }
        //return projectInitiation;
    }
    
    @Override
    public PIBStakeholderList getPIBStakeholderList(final Long projectID,final Long endMakerketId) {
        List<PIBStakeholderList> stakeholderList = this.pibDAONew.getPIBStakeholderList(projectID,endMakerketId);
        if( stakeholderList != null && stakeholderList.size() > 0){
            return stakeholderList.get(0);
        }
        return null;
    }
    @Override
    public PIBStakeholderList getPIBStakeholderList(final Long projectID) {
        List<PIBStakeholderList> stakeholderList = this.pibDAONew.getPIBStakeholderList(projectID);
        if( stakeholderList != null && stakeholderList.size() > 0){
            return stakeholderList.get(0);
        }
        return null;
    }
    @Override
    public  List<PIBStakeholderList> getPIBStakeholderListMultiMarket(final Long projectID) {
        List<PIBStakeholderList> stakeholderList = this.pibDAONew.getPIBStakeholderList(projectID);
        return stakeholderList;
    }
    
    @Override
    public PIBMethodologyWaiver getPIBMethodologyWaiver(final Long projectID,final Long endMakerketId) {
        List<PIBMethodologyWaiver> methodologyWaiverList = this.pibDAONew.getPIBMethodologyWaiver(projectID,endMakerketId);
        if( methodologyWaiverList != null && methodologyWaiverList.size() > 0){
            return methodologyWaiverList.get(0);
        }
        return null;
    }
   
    @Override
    public PIBMethodologyWaiver getPIBKantarMethodologyWaiver(final Long projectID,final Long endMakerketId) {
        List<PIBMethodologyWaiver> methodologyWaiverList = this.pibDAONew.getPIBKantarMethodologyWaiver(projectID,endMakerketId);
        if( methodologyWaiverList != null && methodologyWaiverList.size() > 0){
            return methodologyWaiverList.get(0);
        }
        return null;
    }
    @Override
    public Long getProjectId(final Long waiverId)
    {
    	return this.pibDAONew.getProjectId(waiverId);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBMethWaiverSingleEndMarketId(final Long projectID,	final Long endMarketID) 
    {
    	pibDAONew.updatePIBMethWaiverSingleEndMarketId(projectID, endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void approvePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.approvePIBMethodologyWaiver(pibWaiver);
    	// Change the status of PIB to SAVED once the Methodology Waiver is approved so that the
    	// Notify Agency button should be enable after that.
    	//pibDAO.updatePIBStatus(pibWaiver.getProjectID(), SynchroGlobal.StageStatus.PIB_SAVED.ordinal());
    	
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void approvePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.approvePIBKantarMethodologyWaiver(pibWaiver);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void rejectPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.rejectPIBMethodologyWaiver(pibWaiver);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void rejectPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.rejectPIBKantarMethodologyWaiver(pibWaiver);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void reqForInfoPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.reqForInfoPIBMethodologyWaiver(pibWaiver);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void reqForInfoPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.reqForInfoPIBKantarMethodologyWaiver(pibWaiver);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBReportingSingleEndMarketId(final Long projectID,	final Long endMarketID) 
    {
    	pibDAONew.updatePIBReportingSingleEndMarketId(projectID, endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBStakeholderListSingleEndMarketId(final Long projectID,	final Long endMarketID) 
    {
    	pibDAONew.updatePIBStakeholderListSingleEndMarketId(projectID, endMarketID);
    }
    
    @Override
    public HSSFWorkbook getPIBExcel(Project project, Document document)
    {
    	List<ProjectInitiation> projectInitiationList = getPIBDetails(project.getProjectID());
    	ProjectInitiation projectInitiation = new ProjectInitiation();
    	if(projectInitiationList!=null && projectInitiationList.size()>0)
    	{
    		projectInitiation = projectInitiationList.get(0);
    	}
    	//List<PIBTargetSegment> targetSegmentList = getPIBTargetSegments(project.getProjectID());
    	//List<PIBBrand> pibBrandList = getPIBReferenceBrands(project.getProjectID(),project.getEndMarkets());
    	//List<PIBBrand> pibSmokerList = getPIBSmokerGroup(project.getProjectID(),project.getEndMarkets());
    	//PIBStimulus pibStimulus = getPIBStimulus(project.getProjectID());
    	PIBReporting pibReporting = getPIBReporting(project.getProjectID());
    	//return SynchroReportUtil.getPIBExcel(project,projectInitiation,targetSegmentList,pibBrandList,pibSmokerList,pibStimulus, document,pibReporting);
    	return null;
    }

    @Override
    public List<AttachmentBean> getFieldAttachments(final Long projectId, final Long endmarketId, final Long fieldType) {
        return pibDAONew.getFieldAttachments(getSynchroAttachment(projectId, endmarketId,fieldType));  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean addAttachment(File attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldType, Long userId) throws IOException, AttachmentException
    {
        boolean success = false;
        try
        {
        	Attachment att = attachmentHelper.createAttachment(
                    getSynchroAttachment(projectId, endMarketId, fieldType), fileName , contentType, attachment);
        	pibDAONew.saveAttachmentUser(att.getID(), userId);
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
    		Long projectId, Long endMarketId, Long fieldType, Long userId) 
    {
        boolean success = false;
        try
        {
        	Attachment att = attachmentHelper.createAttachment(
                    getSynchroAttachment(projectId, endMarketId, fieldType), fileName , contentType, attachment, null);
        	pibDAONew.saveAttachmentUser(att.getID(), userId);
            success = true;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return success;
    }
    
    private SynchroAttachment getSynchroAttachment(final Long projectId, final Long endMarketId, final Long fieldType) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.PIB.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    @Override
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception {
        boolean success = false;
        try
        {
            Attachment attachment = attachmentManager.getAttachment(attachmentId);
            attachmentManager.deleteAttachment(attachment);
            pibDAONew.deleteAttachmentUser(attachmentId);
            //pibDAO.removeDocumentAttachment(attachmentId);
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
//    	List<FieldAttachmentBean> fieldAttachment = pibDAO.getDocumentAttachment(projectId, endMakerketId);
    	Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();

        // Get Business Question attachments
       /* List<AttachmentBean> bqAttachments = pibDAO.getFieldAttachments(getSynchroAttachment(projectId, endMakerketId, 0L));
        if(bqAttachments != null && bqAttachments.size() > 0) {
            attachmentMap.put(0, bqAttachments);
        }
        */
        for(SynchroAttachmentObject synchroAttObj : SynchroAttachmentObject.values())
        {
        	 List<AttachmentBean> attachments = pibDAONew.getFieldAttachments(getSynchroAttachment(projectId, endMakerketId, synchroAttObj.getId().longValue()));
        	 if(attachments != null && attachments.size() > 0) {
                 attachmentMap.put(synchroAttObj.getId(), attachments);
             }
        }
      
    	return attachmentMap;
    }
    @Override
    public Map<Long,Long> getAttachmentUser(List<AttachmentBean> attachmentBean)
    {
    	Map<Long,Long> attachmentUser = new HashMap<Long,Long>();
    	for(AttachmentBean ab:attachmentBean)
    	{
    		attachmentUser.put(ab.getID(), pibDAONew.getAttachmentUser(ab.getID()));
    	}
    	return attachmentUser;
    }
    @Override
    public void updateDocumentAttachment(Long attachmentId, Long projectId, Long updatedEndMarketId)
    {
    	long objectId = (projectId + "-" + updatedEndMarketId).hashCode();
    	pibDAONew.updateDocumentAttachment(attachmentId, objectId);
        
        
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void savePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.savePIBMethodologyWaiver(pibWaiver);
    	//pibDAO.updatePIBStatus(pibWaiver.getProjectID(), SynchroGlobal.StageStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void savePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver)
    {
    	pibDAONew.savePIBKantarMethodologyWaiver(pibWaiver);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBStatus(final Long projectID,final Integer status)
    {
    	pibDAONew.updatePIBStatus(projectID, status);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBDeviation(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updatePIBDeviation(projectInitiation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBActionStandard(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updatePIBActionStandard(projectInitiation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBResearchDesign(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updatePIBResearchDesign(projectInitiation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBSampleProfile(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updatePIBSampleProfile(projectInitiation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBStimulusMaterial(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updatePIBStimulusMaterial(projectInitiation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBEndMarket(final Long projectID) {
    	pibDAONew.deletePIBEndMarket(projectID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBEndMarket(final Long projectID,final Long endMarketID) {
    	pibDAONew.deletePIBEndMarket(projectID,endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBMWEndMarket(final Long projectID) {
    	pibDAONew.deletePIBMWEndMarket(projectID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBMWEndMarket(final Long projectID,final Long endMarketID) {
    	pibDAONew.deletePIBMWEndMarket(projectID,endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBReportingEndMarket(final Long projectID) {
    	pibDAONew.deletePIBReportingEndMarket(projectID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBReportingEndMarket(final Long projectID,final Long endMarketID) {
    	pibDAONew.deletePIBReportingEndMarket(projectID,endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBStakeholderEndMarket(final Long projectID) {
    	pibDAONew.deletePIBStakeholderEndMarket(projectID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deletePIBStakeholderEndMarket(final Long projectID,final Long endMarketID) {
    	pibDAONew.deletePIBStakeholderEndMarket(projectID, endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBBusinessQuestion(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updatePIBBusinessQuestion(projectInitiation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBResearchObjective(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updatePIBResearchObjective(projectInitiation);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBNotifyAboveMarketContact(final Long projectId, final Long endMarketId, final Integer notifyAboveMarketContact)
    {
    	pibDAONew.updatePIBNotifyAboveMarketContact(projectId,endMarketId,notifyAboveMarketContact);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBApproveChanges(final Long projectId, final Long endMarketId, final Integer approveChanges)
    {
    	pibDAONew.updatePIBApproveChanges(projectId,endMarketId,approveChanges);
    }
    @Override
    public Boolean allPIBMarketSaved(final long projectID, final int endMarketSize) {
    	List<ProjectInitiation> piList = getPIBDetails(projectID);
    	if(piList!=null && piList.size()== endMarketSize)
    	{
    		for(ProjectInitiation pi : piList)
    		{
    			Integer status = synchroProjectManagerNew.getEndMarketStatus(projectID, pi.getEndMarketID());
    			boolean canEditEM = true;
            	if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID && (status == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || status == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal()))
            	{
            		canEditEM = false;
            	}
    			if(canEditEM && pi.getStatus()==SynchroGlobal.StageStatus.PIB_STARTED.ordinal())
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
    public void updateNotifySPI(final Long projectId, final Integer notifySPI)
    {
    	pibDAONew.updateNotifySPI(projectId,notifySPI);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateNotifyPO(final Long projectId, final Integer notifyPO)
    {
    	pibDAONew.updateNotifyPO(projectId,notifyPO);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateLatestEstimate(final ProjectInitiation projectInitiation)
    {
    	pibDAONew.updateLatestEstimate(projectInitiation);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateOtherSPIContact(final String otherSPIContact, final Long projectID, final Long endMarketID){
        this.pibDAONew.updateOtherSPIContact(otherSPIContact, projectID, endMarketID);        
    }
    @Override
    public String getOtherSPIContact(final Long projectID, final Long endMarketId)
    {
    	return this.pibDAONew.getOtherSPIContact(projectID, endMarketId);
    }
    @Override
    public String getOtherLegalContact(final Long projectID, final Long endMarketId)
    {
    	return this.pibDAONew.getOtherLegalContact(projectID, endMarketId);
    }
    @Override
    public String getOtherProductContact(final Long projectID, final Long endMarketId)
    {
    	return this.pibDAONew.getOtherProductContact(projectID, endMarketId);
    }
    @Override
    public String getOtherAgencyContact(final Long projectID, final Long endMarketId)
    {
    	return this.pibDAONew.getOtherAgencyContact(projectID, endMarketId);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateOtherLegalContact(final String otherLegalContact, final Long projectID, final Long endMarketID){
        this.pibDAONew.updateOtherLegalContact(otherLegalContact, projectID, endMarketID);        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateOtherProductContact(final String otherProductContact, final Long projectID, final Long endMarketID){
        this.pibDAONew.updateOtherProductContact(otherProductContact, projectID, endMarketID);        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateOtherAgencyContact(final String otherAgencyContact, final Long projectID, final Long endMarketID){
        this.pibDAONew.updateOtherAgencyContact(otherAgencyContact, projectID, endMarketID);        
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateAgencyContact(final Long projectID, final Long endMarketId, final Long updatedAgencyContact)
    {
    	this.pibDAONew.updateAgencyContact(projectID, endMarketId,updatedAgencyContact);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBSendForApproval(final ProjectInitiation projectInitiation)
    {
    	this.pibDAONew.updatePIBSendForApproval(projectInitiation);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void resetPIB(final ProjectInitiation projectInitiation)
    {
    	this.pibDAONew.resetPIB(projectInitiation);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBSendReminder(final ProjectInitiation projectInitiation)
    {
    	this.pibDAONew.updatePIBSendReminder(projectInitiation);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBNeedsDiscussion(final ProjectInitiation projectInitiation)
    {
    	this.pibDAONew.updatePIBNeedsDiscussion(projectInitiation);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void confirmLegalApprovalSubmission(final ProjectInitiation projectInitiation)
    {
    	this.pibDAONew.confirmLegalApprovalSubmission(projectInitiation);
    }
    
    @Override
    public List<PIBMethodologyWaiver> getPIBMethodologyWaivers(final ProjectResultFilter projectResultFilter){
       return this.pibDAONew.getPIBMethodologyWaivers(projectResultFilter);
    }
    
    @Override
    public Long getPIBMethodologyWaiversTotalCount(final ProjectResultFilter filter)
    {
    	return this.pibDAONew.getPIBMethodologyWaiversTotalCount(filter);
    }
    
    
    @Override
    public List<PIBMethodologyWaiver> getPIBAgencyWaivers(final ProjectResultFilter projectResultFilter)
    {
    	return this.pibDAONew.getPIBAgencyWaivers(projectResultFilter);
    }
    
    @Override
    public Long getPIBAgencyWaiversTotalCount(final ProjectResultFilter filter)
    {
    	return this.pibDAONew.getPIBAgencyWaiversTotalCount(filter);
    }
    
    
    @Override
    public List<PIBMethodologyWaiver> getPIBPendingMethodologyWaivers(final ProjectResultFilter projectResultFilter){
       return this.pibDAONew.getPIBPendingMethodologyWaivers(projectResultFilter);
    }
    
    @Override
    public Long getPIBPendingMethodologyWaiversTotalCount(final ProjectResultFilter filter)
    {
    	return this.pibDAONew.getPIBPendingMethodologyWaiversTotalCount(filter);
    }
    
    
    @Override
    public List<PIBMethodologyWaiver> getPIBPendingAgencyWaivers(final ProjectResultFilter projectResultFilter)
    {
    	return this.pibDAONew.getPIBPendingAgencyWaivers(projectResultFilter);
    }
    
    @Override
    public Long getPIBPendingAgencyWaiversTotalCount(final ProjectResultFilter filter)
    {
    	return this.pibDAONew.getPIBPendingAgencyWaiversTotalCount(filter);
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

	public PIBDAONew getPibDAONew() {
		return pibDAONew;
	}

	public void setPibDAONew(PIBDAONew pibDAONew) {
		this.pibDAONew = pibDAONew;
	}

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public void setSynchroProjectManagerNew(
			ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}
	
}
