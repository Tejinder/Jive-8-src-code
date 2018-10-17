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

import com.esotericsoftware.minlog.Log;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PSMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProposalEndMarketDetails;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ProposalReporting;
import com.grail.synchro.dao.PIBDAO;
import com.grail.synchro.dao.ProjectSpecsDAO;
import com.grail.synchro.dao.ProposalDAO;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.object.SynchroAttachment;
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
public class ProposalManagerImpl implements ProposalManager {

    private ProposalDAO proposalDAO;
    private ProjectSpecsDAO projectSpecsDAO;
    private ProjectSpecsManager projectSpecsManager;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;
    private PIBDAO pibDAO;
    private ProjectManager synchroProjectManager;

    @Override
    public List<ProposalInitiation> getProposalDetails(final Long projectID, final Long endMarketId, final Long agencyId) {
        return this.proposalDAO.getProposalInitiation(projectID,endMarketId,agencyId);
    }
    @Override
    public List<ProposalInitiation> getProposalDetails(final Long projectID) {
        return this.proposalDAO.getProposalInitiation(projectID);
    }
    @Override
    public List<ProposalInitiation> getProposalDetails(final Long projectID,final Long agencyId) {
        return this.proposalDAO.getProposalInitiation(projectID,agencyId);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProposalInitiation saveProposalDetails(final ProposalInitiation proposalInitiation){
        this.proposalDAO.save(proposalInitiation);
        this.proposalDAO.saveProposalReporting(proposalInitiation);

        // updatePIBDetails(projectInitiation);
        return proposalInitiation;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails){
        this.proposalDAO.saveProposalEMDetails(proposalEMDetails);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProposalInitiation updateProposalDetails(final ProposalInitiation proposalInitiation) {
        this.proposalDAO.update(proposalInitiation);
        updateProposalReporting(proposalInitiation);
        return  proposalInitiation;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails) {
        this.proposalDAO.updateProposalEMDetails(proposalEMDetails);
    }

    @Override
    public ProposalReporting getProposalReporting(final Long projectID, final Long endMarketId, final Long agencyId) {
        List<ProposalReporting> reportingList = this.proposalDAO.getProposalReporting(projectID,endMarketId,agencyId);
        if( reportingList != null && reportingList.size() > 0){
            return reportingList.get(0);
        }
        return null;
    }
    @Override
    public ProposalEndMarketDetails getProposalEMDetails(final Long projectID, final Long endMarketId, final Long agencyId)
    {
        List<ProposalEndMarketDetails> emDetailsList = this.proposalDAO.getProposalEMDetails(projectID,endMarketId,agencyId);
        if( emDetailsList != null && emDetailsList.size() > 0){
            return emDetailsList.get(0);
        }
        return null;
    }

    @Override
    public Map<Long, ProposalEndMarketDetails> getProposalEMDetails(final Long projectID, final Long agencyId)
    {
        List<ProposalEndMarketDetails> emDetailsList = this.proposalDAO.getProposalEMDetails(projectID, agencyId);
        Map<Long, ProposalEndMarketDetails> proposalEMDetailsMap = new HashMap<Long, ProposalEndMarketDetails>();
        if( emDetailsList != null && emDetailsList.size() > 0){
            for(ProposalEndMarketDetails proposalEmList: emDetailsList)
            {
                proposalEMDetailsMap.put(proposalEmList.getEndMarketID(),proposalEmList );
            }
            return proposalEMDetailsMap;
        }
        return null;
    }

    @Override
    public ProposalInitiation updateProposalReporting(final ProposalInitiation proposalInitiation) {

        ProposalReporting reporting = getProposalReporting(proposalInitiation.getProjectID(), proposalInitiation.getEndMarketID(), proposalInitiation.getAgencyID());
        if( reporting == null){
            this.proposalDAO.saveProposalReporting(proposalInitiation);
        }else {
            this.proposalDAO.updateProposalReporting(proposalInitiation);
        }
        return proposalInitiation;
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void submitProposal(final Long projectID, final Long agencyId)
    {
        proposalDAO.submitProposal(projectID, agencyId);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void awardAgency(final Project project, final Long agencyId,final Long endMarketId, Map<Integer, List<AttachmentBean>> attachmentMap)
    {
        proposalDAO.awardAgency(project.getProjectID(), agencyId);
        // Copy the Agency details to Project Specs once the Agency is Awarded
        ProposalInitiation proposalInitiation = getProposalDetails(project.getProjectID(), endMarketId, agencyId).get(0);

        ProposalEndMarketDetails proposalEMDetails = getProposalEMDetails(project.getProjectID(), endMarketId, agencyId);
        ProposalReporting propoalReporting = getProposalReporting(project.getProjectID(), endMarketId, agencyId);

        ProjectInitiation projectInitiation = pibDAO.getProjectInitiation(project.getProjectID()).get(0);

        ProjectSpecsInitiation projectSpecsInitiation = new ProjectSpecsInitiation();
        projectSpecsInitiation.setProjectID(project.getProjectID());
        projectSpecsInitiation.setActionStandard(proposalInitiation.getActionStandard());
        projectSpecsInitiation.setBizQuestion(proposalInitiation.getBizQuestion());
        projectSpecsInitiation.setCreationBy(proposalInitiation.getCreationBy());
        projectSpecsInitiation.setCreationDate(proposalInitiation.getCreationDate());
        projectSpecsInitiation.setEndMarketID(endMarketId);
        projectSpecsInitiation.setModifiedBy(proposalInitiation.getModifiedBy());
        projectSpecsInitiation.setModifiedDate(proposalInitiation.getModifiedDate());
        projectSpecsInitiation.setNpiReferenceNo(proposalInitiation.getNpiReferenceNo());
        projectSpecsInitiation.setResearchDesign(proposalInitiation.getResearchDesign());
        projectSpecsInitiation.setResearchObjective(proposalInitiation.getResearchObjective());
        projectSpecsInitiation.setSampleProfile(proposalInitiation.getSampleProfile());
        projectSpecsInitiation.setStimuliDate(proposalInitiation.getStimuliDate());
        projectSpecsInitiation.setStimulusMaterial(proposalInitiation.getStimulusMaterial());
        projectSpecsInitiation.setStimulusMaterialShipped(proposalInitiation.getStimulusMaterialShipped());
        projectSpecsInitiation.setOthers(proposalInitiation.getOthers());
        projectSpecsInitiation.setDescription(project.getDescription());
        projectSpecsInitiation.setDeviationFromSM(projectInitiation.getDeviationFromSM());

        projectSpecsInitiation.setBrand(proposalInitiation.getBrand());
        projectSpecsInitiation.setProjectOwner(proposalInitiation.getProjectOwner());
        projectSpecsInitiation.setSpiContact(proposalInitiation.getSpiContact());
        projectSpecsInitiation.setProposedMethodology(proposalInitiation.getProposedMethodology());
        projectSpecsInitiation.setMethodologyGroup(proposalInitiation.getMethodologyGroup());
        projectSpecsInitiation.setMethodologyType(proposalInitiation.getMethodologyType());
        projectSpecsInitiation.setStartDate(proposalInitiation.getStartDate());
        projectSpecsInitiation.setEndDate(proposalInitiation.getEndDate());
        //projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());

        // https://www.svn.sourcen.com/issues/18798
        projectSpecsInitiation.setScreener("Enter text and/or attach documents");
        projectSpecsInitiation.setConsumerCCAgreement("Enter text and/or attach documents");
        projectSpecsInitiation.setQuestionnaire("Enter text and/or attach documents");
        projectSpecsInitiation.setDiscussionguide("Enter text and/or attach documents");
        projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());

        projectSpecsInitiation.setOtherReportingRequirements(propoalReporting.getOtherReportingRequirements());
        projectSpecsInitiation.setPresentation(propoalReporting.getPresentation());
        projectSpecsInitiation.setTopLinePresentation(propoalReporting.getTopLinePresentation());
        projectSpecsInitiation.setFullreport(propoalReporting.getFullreport());

        // We are keeping the Category Type at Project Specs level as well.
        projectSpecsInitiation.setCategoryType(project.getCategoryType());

        projectSpecsDAO.save(projectSpecsInitiation);
        projectSpecsDAO.saveProjectSpecsReporting(projectSpecsInitiation);

        ProjectSpecsEndMarketDetails projectSpecsEMDetails = new ProjectSpecsEndMarketDetails();

        if(proposalEMDetails!=null)
        {
	        projectSpecsEMDetails.setProjectID(proposalEMDetails.getProjectID());
	        projectSpecsEMDetails.setEndMarketID(proposalEMDetails.getEndMarketID());
	        projectSpecsEMDetails.setTotalCost(proposalEMDetails.getTotalCost());
	        projectSpecsEMDetails.setTotalCostType(proposalEMDetails.getTotalCostType());
	
	        // Final cost field on the Project Specs section will be captured from the Total cost section
	        // from Proposal section, initially
	        projectSpecsEMDetails.setFinalCost(proposalEMDetails.getTotalCost());
	        projectSpecsEMDetails.setFinalCostType(proposalEMDetails.getTotalCostType());
	
	        projectSpecsEMDetails.setOriginalFinalCost(proposalEMDetails.getTotalCost());
	        projectSpecsEMDetails.setOriginalFinalCostType(proposalEMDetails.getTotalCostType());
	
	        //https://www.svn.sourcen.com/issues/18310
	        projectSpecsEMDetails.setProjectEndDate(projectSpecsInitiation.getEndDate());
	        projectSpecsEMDetails.setProjectEndDateLatest(projectSpecsInitiation.getEndDate());
	
	        if(proposalInitiation.getMethodologyType().intValue()!=4 )
	        {
	            projectSpecsEMDetails.setFieldworkCost(proposalEMDetails.getFieldworkCost());
	            projectSpecsEMDetails.setFieldworkCostType(proposalEMDetails.getFieldworkCostType());
	
	            projectSpecsEMDetails.setOperationalHubCost(proposalEMDetails.getOperationalHubCost());
	            projectSpecsEMDetails.setOperationalHubCostType(proposalEMDetails.getOperationalHubCostType());
	
	            projectSpecsEMDetails.setOtherCost(proposalEMDetails.getOtherCost());
	            projectSpecsEMDetails.setOtherCostType(proposalEMDetails.getOtherCostType());
	
	            projectSpecsEMDetails.setFwEndDate(proposalEMDetails.getFwEndDate());
	            projectSpecsEMDetails.setFwStartDate(proposalEMDetails.getFwStartDate());
	
	            projectSpecsEMDetails.setIntMgmtCost(proposalEMDetails.getIntMgmtCost());
	            projectSpecsEMDetails.setIntMgmtCostType(proposalEMDetails.getIntMgmtCostType());
	            projectSpecsEMDetails.setLocalMgmtCost(proposalEMDetails.getLocalMgmtCost());
	            projectSpecsEMDetails.setLocalMgmtCostType(proposalEMDetails.getLocalMgmtCostType());
	
	
	            projectSpecsEMDetails.setProposedFWAgencyNames(proposalEMDetails.getProposedFWAgencyNames());
	            projectSpecsEMDetails.setDataCollectionMethod(proposalEMDetails.getDataCollectionMethod());
	            // https://www.svn.sourcen.com/issues/17728
	            projectSpecsEMDetails.setFwEndDateLatest(proposalEMDetails.getFwEndDate());
	            projectSpecsEMDetails.setFwStartDateLatest(proposalEMDetails.getFwStartDate());
	
	            projectSpecsEMDetails.setCities(proposalEMDetails.getCities());
	            projectSpecsEMDetails.setGeoSpreadNational(proposalEMDetails.getGeoSpreadNational());
	            projectSpecsEMDetails.setGeoSpreadUrban(proposalEMDetails.getGeoSpreadUrban());
	        }
	
	
	        if(proposalInitiation.getMethodologyType().intValue()==1 || proposalInitiation.getMethodologyType().intValue()==3)
	        {
	            projectSpecsEMDetails.setTotalNoInterviews(proposalEMDetails.getTotalNoInterviews());
	            projectSpecsEMDetails.setAvIntDuration(proposalEMDetails.getAvIntDuration());
	            projectSpecsEMDetails.setTotalNoOfVisits(proposalEMDetails.getTotalNoOfVisits());
	        }
	        if(proposalInitiation.getMethodologyType().intValue()==2 || proposalInitiation.getMethodologyType().intValue()==3)
	        {
	            projectSpecsEMDetails.setTotalNoOfGroups(proposalEMDetails.getTotalNoOfGroups());
	            projectSpecsEMDetails.setInterviewDuration(proposalEMDetails.getInterviewDuration());
	            projectSpecsEMDetails.setNoOfRespPerGroup(proposalEMDetails.getNoOfRespPerGroup());
	            //projectSpecsEMDetails.setCities(proposalEMDetails.getCities());
	            //projectSpecsEMDetails.setGeoSpreadNational(proposalEMDetails.getGeoSpreadNational());
	            //projectSpecsEMDetails.setGeoSpreadUrban(proposalEMDetails.getGeoSpreadUrban());
	        }
	
	        projectSpecsDAO.saveProjectSpecsEMDetails(projectSpecsEMDetails);
        }
        copyAttachments(attachmentMap,project.getProjectID(), endMarketId, proposalInitiation.getCreationBy() );
        // Copy PIB Methodology Waiver to PS Methodology Waiver. https://www.svn.sourcen.com//issues/18431
        // Now as we maintain waiver only at PIB level so no need to copy the waiver from PIB to PS
        //copyPIBMethodologyWaiver(project.getProjectID(), endMarketId);


    }

    /**
     * This method will copy the PIB Methodology Waiver details to PS Methodology Waiver
     * @param projectId
     * @param endMarketId
     */
    private void copyPIBMethodologyWaiver(Long projectId, Long endMarketId)
    {
        List<PIBMethodologyWaiver> pibWaiverList = pibDAO.getPIBMethodologyWaiver(projectId, endMarketId);
        if(pibWaiverList!=null && pibWaiverList.size()>0)
        {
            PSMethodologyWaiver psWaiver = new PSMethodologyWaiver();
            psWaiver.setProjectID(pibWaiverList.get(0).getProjectID());
            psWaiver.setEndMarketID(endMarketId);
            psWaiver.setMethodologyDeviationRationale(pibWaiverList.get(0).getMethodologyDeviationRationale());
            psWaiver.setMethodologyApprover(pibWaiverList.get(0).getMethodologyApprover());
            psWaiver.setMethodologyApproverComment(pibWaiverList.get(0).getMethodologyApproverComment());
            psWaiver.setIsApproved(pibWaiverList.get(0).getIsApproved());
            psWaiver.setCreationBy(pibWaiverList.get(0).getCreationBy());
            psWaiver.setModifiedBy(pibWaiverList.get(0).getModifiedBy());
            psWaiver.setCreationDate(pibWaiverList.get(0).getCreationDate());
            psWaiver.setModifiedDate(pibWaiverList.get(0).getModifiedDate());
            psWaiver.setModifiedDate(pibWaiverList.get(0).getModifiedDate());
            psWaiver.setStatus(pibWaiverList.get(0).getStatus());

            projectSpecsDAO.savePS_PIBMethodologyWaiver(psWaiver);

        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void awardMultiMarketAgency(final Project project, final Long agencyId,final Long endMarketId, Map<Integer, List<AttachmentBean>> attachmentMap)
    {
        proposalDAO.awardAgency(project.getProjectID(), agencyId);
        // Copy the Agency details to Project Specs once the Agency is Awarded

        ProposalInitiation proposalInitiation = getProposalDetails(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, agencyId).get(0);
        ProposalReporting propoalReporting = getProposalReporting(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, agencyId);
        ProjectInitiation projectInitiation = pibDAO.getProjectInitiation(project.getProjectID()).get(0);
        ProjectInitiation projectInitiationAboveMarket = pibDAO.getProjectInitiation(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID).get(0);
        ProjectSpecsInitiation projectSpecsInitiation = new ProjectSpecsInitiation();
        projectSpecsInitiation.setProjectID(project.getProjectID());
        projectSpecsInitiation.setActionStandard(proposalInitiation.getActionStandard());
        projectSpecsInitiation.setBizQuestion(proposalInitiation.getBizQuestion());
        projectSpecsInitiation.setCreationBy(proposalInitiation.getCreationBy());
        projectSpecsInitiation.setCreationDate(proposalInitiation.getCreationDate());
        projectSpecsInitiation.setEndMarketID(SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        projectSpecsInitiation.setModifiedBy(proposalInitiation.getModifiedBy());
        projectSpecsInitiation.setModifiedDate(proposalInitiation.getModifiedDate());
        projectSpecsInitiation.setNpiReferenceNo(proposalInitiation.getNpiReferenceNo());
        projectSpecsInitiation.setResearchDesign(proposalInitiation.getResearchDesign());
        projectSpecsInitiation.setResearchObjective(proposalInitiation.getResearchObjective());
        projectSpecsInitiation.setSampleProfile(proposalInitiation.getSampleProfile());
        projectSpecsInitiation.setStimuliDate(proposalInitiation.getStimuliDate());
        projectSpecsInitiation.setStimulusMaterial(proposalInitiation.getStimulusMaterial());
        projectSpecsInitiation.setStimulusMaterialShipped(proposalInitiation.getStimulusMaterialShipped());
        projectSpecsInitiation.setOthers(proposalInitiation.getOthers());
        projectSpecsInitiation.setDescription(project.getDescription());
        projectSpecsInitiation.setDeviationFromSM(projectInitiationAboveMarket.getDeviationFromSM());

        projectSpecsInitiation.setBrand(proposalInitiation.getBrand());
        projectSpecsInitiation.setProjectOwner(proposalInitiation.getProjectOwner());
        projectSpecsInitiation.setSpiContact(proposalInitiation.getSpiContact());
        projectSpecsInitiation.setProposedMethodology(proposalInitiation.getProposedMethodology());
        projectSpecsInitiation.setMethodologyGroup(proposalInitiation.getMethodologyGroup());
        projectSpecsInitiation.setMethodologyType(proposalInitiation.getMethodologyType());
        projectSpecsInitiation.setStartDate(proposalInitiation.getStartDate());
        projectSpecsInitiation.setEndDate(proposalInitiation.getEndDate());
        //projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal());

        // https://www.svn.sourcen.com/issues/18798
        projectSpecsInitiation.setScreener("Enter text and/or attach documents");
        projectSpecsInitiation.setConsumerCCAgreement("Enter text and/or attach documents");
        projectSpecsInitiation.setQuestionnaire("Enter text and/or attach documents");
        projectSpecsInitiation.setDiscussionguide("Enter text and/or attach documents");
        projectSpecsInitiation.setStatus(SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal());

        projectSpecsInitiation.setOtherReportingRequirements(propoalReporting.getOtherReportingRequirements());
        projectSpecsInitiation.setPresentation(propoalReporting.getPresentation());
        projectSpecsInitiation.setTopLinePresentation(propoalReporting.getTopLinePresentation());
        projectSpecsInitiation.setFullreport(propoalReporting.getFullreport());
        projectSpecsInitiation.setGlobalSummary(propoalReporting.getGlobalSummary());

        //https://www.svn.sourcen.com/issues/18825
        projectSpecsInitiation.setAboveMarketFinalCost(projectInitiationAboveMarket.getLatestEstimate());
        projectSpecsInitiation.setAboveMarketFinalCostType(projectInitiationAboveMarket.getLatestEstimateType());

        // We are keeping the Category Type at Project Specs level as well.
        projectSpecsInitiation.setCategoryType(project.getCategoryType());

        projectSpecsDAO.save(projectSpecsInitiation);
        projectSpecsDAO.saveProjectSpecsReporting(projectSpecsInitiation);

        Map<Long, ProposalEndMarketDetails> proposalEMDetailsMap = getProposalEMDetails(project.getProjectID(), agencyId);
        if(proposalEMDetailsMap != null && proposalEMDetailsMap.size() > 0) {
            for(Long proposalEMId:proposalEMDetailsMap.keySet())
            {
                Integer endMarketStatus =  synchroProjectManager.getEndMarketStatus(project.getProjectID(), proposalEMId);

                //https://www.svn.sourcen.com/issues/19135.
                // Cancelled End Markets should not be carry forward to Project Specs and Report Stages.
                if(endMarketStatus==null || endMarketStatus!=SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal())
                {
                    //TODO: For now we are adding the same Project Specs details as of Above Market for Other end markets. Need to change this logic later
                    projectSpecsInitiation.setEndMarketID(proposalEMId);

                    //Code to fix SVN Bug #19103
                    try{
                        ProjectInitiation projectInitiationEM = pibDAO.getProjectInitiation(project.getProjectID(), proposalEMId).get(0);
                        projectSpecsInitiation.setDeviationFromSM(projectInitiationEM.getDeviationFromSM());
                    }catch(Exception e){Log.error("Error while updating DeviationFromSM for Project ID " + project.getProjectID() + " and endmarket ID "+ proposalEMId);}

                    projectSpecsDAO.save(projectSpecsInitiation);
                    projectSpecsDAO.saveProjectSpecsReporting(projectSpecsInitiation);

                    ProposalEndMarketDetails proposalEMDetails = proposalEMDetailsMap.get(proposalEMId);
                    ProjectSpecsEndMarketDetails projectSpecsEMDetails = new ProjectSpecsEndMarketDetails();

                    projectSpecsEMDetails.setProjectID(proposalEMDetails.getProjectID());
                    projectSpecsEMDetails.setEndMarketID(proposalEMDetails.getEndMarketID());
                    projectSpecsEMDetails.setTotalCost(proposalEMDetails.getTotalCost());
                    projectSpecsEMDetails.setTotalCostType(proposalEMDetails.getTotalCostType());

                    // Final cost field on the Project Specs section will be captured from the Total cost section
                    // from Proposal section, initially
                    projectSpecsEMDetails.setFinalCost(proposalEMDetails.getTotalCost());
                    projectSpecsEMDetails.setFinalCostType(proposalEMDetails.getTotalCostType());

                    projectSpecsEMDetails.setOriginalFinalCost(proposalEMDetails.getTotalCost());
                    projectSpecsEMDetails.setOriginalFinalCostType(proposalEMDetails.getTotalCostType());

                    //https://www.svn.sourcen.com/issues/18747

                    //	projectSpecsEMDetails.setFinalCost(projectInitiationAboveMarket.getLatestEstimate());
                    //	projectSpecsEMDetails.setFinalCostType(projectInitiationAboveMarket.getLatestEstimateType());

                    //	projectSpecsEMDetails.setOriginalFinalCost(projectInitiationAboveMarket.getLatestEstimate());
                    //	projectSpecsEMDetails.setOriginalFinalCostType(projectInitiationAboveMarket.getLatestEstimateType());

                    //https://www.svn.sourcen.com/issues/19625
                    /*List<FundingInvestment> fundingInv = synchroProjectManager.getProjectInvestments(proposalEMDetails.getProjectID(), proposalEMId);
                     if(fundingInv!=null && fundingInv.size()>0)
                     {
                         projectSpecsEMDetails.setOriginalFinalCost(fundingInv.get(0).getEstimatedCost());
                         projectSpecsEMDetails.setOriginalFinalCostType(fundingInv.get(0).getEstimatedCostCurrency().intValue());

                         projectSpecsEMDetails.setFinalCost(fundingInv.get(0).getEstimatedCost());
                         projectSpecsEMDetails.setFinalCostType(fundingInv.get(0).getEstimatedCostCurrency().intValue());
                     }*/

                    //https://www.svn.sourcen.com/issues/18310
                    projectSpecsEMDetails.setProjectEndDate(projectSpecsInitiation.getEndDate());
                    projectSpecsEMDetails.setProjectEndDateLatest(projectSpecsInitiation.getEndDate());

                    if(proposalInitiation.getMethodologyType().intValue()!=4 )
                    {
                        projectSpecsEMDetails.setFieldworkCost(proposalEMDetails.getFieldworkCost());
                        projectSpecsEMDetails.setFieldworkCostType(proposalEMDetails.getFieldworkCostType());

                        projectSpecsEMDetails.setOperationalHubCost(proposalEMDetails.getOperationalHubCost());
                        projectSpecsEMDetails.setOperationalHubCostType(proposalEMDetails.getOperationalHubCostType());

                        projectSpecsEMDetails.setOtherCost(proposalEMDetails.getOtherCost());
                        projectSpecsEMDetails.setOtherCostType(proposalEMDetails.getOtherCostType());

                        projectSpecsEMDetails.setFwEndDate(proposalEMDetails.getFwEndDate());
                        projectSpecsEMDetails.setFwStartDate(proposalEMDetails.getFwStartDate());

                        projectSpecsEMDetails.setIntMgmtCost(proposalEMDetails.getIntMgmtCost());
                        projectSpecsEMDetails.setIntMgmtCostType(proposalEMDetails.getIntMgmtCostType());
                        projectSpecsEMDetails.setLocalMgmtCost(proposalEMDetails.getLocalMgmtCost());
                        projectSpecsEMDetails.setLocalMgmtCostType(proposalEMDetails.getLocalMgmtCostType());


                        projectSpecsEMDetails.setProposedFWAgencyNames(proposalEMDetails.getProposedFWAgencyNames());
                        projectSpecsEMDetails.setDataCollectionMethod(proposalEMDetails.getDataCollectionMethod());
                        // https://www.svn.sourcen.com/issues/17728
                        projectSpecsEMDetails.setFwEndDateLatest(proposalEMDetails.getFwEndDate());
                        projectSpecsEMDetails.setFwStartDateLatest(proposalEMDetails.getFwStartDate());

                        projectSpecsEMDetails.setCities(proposalEMDetails.getCities());
                        projectSpecsEMDetails.setGeoSpreadNational(proposalEMDetails.getGeoSpreadNational());
                        projectSpecsEMDetails.setGeoSpreadUrban(proposalEMDetails.getGeoSpreadUrban());
                    }


                    if(proposalInitiation.getMethodologyType().intValue()==1 || proposalInitiation.getMethodologyType().intValue()==3)
                    {
                        projectSpecsEMDetails.setTotalNoInterviews(proposalEMDetails.getTotalNoInterviews());
                        projectSpecsEMDetails.setAvIntDuration(proposalEMDetails.getAvIntDuration());
                        projectSpecsEMDetails.setTotalNoOfVisits(proposalEMDetails.getTotalNoOfVisits());
                    }
                    if(proposalInitiation.getMethodologyType().intValue()==2 || proposalInitiation.getMethodologyType().intValue()==3)
                    {
                        projectSpecsEMDetails.setTotalNoOfGroups(proposalEMDetails.getTotalNoOfGroups());
                        projectSpecsEMDetails.setInterviewDuration(proposalEMDetails.getInterviewDuration());
                        projectSpecsEMDetails.setNoOfRespPerGroup(proposalEMDetails.getNoOfRespPerGroup());
                        //projectSpecsEMDetails.setCities(proposalEMDetails.getCities());
                        //projectSpecsEMDetails.setGeoSpreadNational(proposalEMDetails.getGeoSpreadNational());
                        //projectSpecsEMDetails.setGeoSpreadUrban(proposalEMDetails.getGeoSpreadUrban());
                    }
                    projectSpecsDAO.saveProjectSpecsEMDetails(projectSpecsEMDetails);
                }



            }
        }
        // Copy PIB Methodology Waiver to PS Methodology Waiver. https://www.svn.sourcen.com//issues/18431
        // Now as we maintain waiver only at PIB level so no need to copy the waiver from PIB to PS
        //copyPIBMethodologyWaiver(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
        //copyAttachments(attachmentMap,project.getProjectID(), endMarketId, proposalInitiation.getCreationBy() );

    }
    /**
     * This method will copy the attachments from Proposal stage to Project Specs stage
     * @param attachmentMap
     */
    private void copyAttachments(Map<Integer, List<AttachmentBean>> attachmentMap, Long projectId, Long endMarketId,
                                 Long userId)
    {
        if(attachmentMap!=null && attachmentMap.size()>0)
        {
            for(Integer key: attachmentMap.keySet())
            {
                // Because Proposal Cost Template attachments should not be copied to project Specs page
                if(key!=SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())
                {
                    List<AttachmentBean> attchBeanList = attachmentMap.get(key);
                    for(AttachmentBean ab:attchBeanList)
                    {
                        try
                        {
                            projectSpecsManager.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                                    ab.getContentType(), projectId, endMarketId, key.longValue(), userId);
                        }
                        catch(Exception e)
                        {
                            Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
                        }

                    }
                }
            }
        }

    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void rejectAgency(final Project project, final Long agencyId,final Long endMarketId, final Integer status)
    {
        proposalDAO.rejectAgency(project.getProjectID(), agencyId, status);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBReportingSingleEndMarketId(final Long projectID,	final Long endMarketID)
    {
        proposalDAO.updatePIBReportingSingleEndMarketId(projectID, endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBAttachmentSingleEndMarketId(final Long projectID,	final Long endMarketID)
    {
        proposalDAO.updatePIBAttachmentSingleEndMarketId(projectID, endMarketID);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProposalEndMarketId(final Long projectId, final Long agencyId, final Long endMarketId)
    {
        proposalDAO.updateProposalEndMarketId(projectId,agencyId,endMarketId);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateSendToProjectOwner(final Long projectID, final Long agencyId, final Integer sendToProjectOwner)
    {
        proposalDAO.updateSendToProjectOwner(projectID, agencyId, sendToProjectOwner);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateRequestClarificationModification(final Long projectID, final Long agencyId, final Integer reqClarification)
    {
        proposalDAO.updateRequestClarificationModification(projectID, agencyId,reqClarification);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateAgency(final Long projectId,  final Long endMarketId, final Long updatedAgencyId, final Long sourceAgencyId)
    {
        proposalDAO.updateAgency(projectId,  endMarketId, updatedAgencyId, sourceAgencyId);
    }
    @Override
    public HSSFWorkbook getPIBExcel(Project project, Document document)
    {
        return null;
    }
    @Override
    public boolean addAttachment(File attachment,String fileName, final String contentType,
                                 Long projectId, Long endMarketId, Long fieldType, Long userId, Long agencyId) throws IOException, AttachmentException
    {


        boolean success = false;
        try
        {
            Attachment att = attachmentHelper.createAttachment(
                    getSynchroAttachment(projectId, endMarketId,agencyId, fieldType), fileName , contentType, attachment);
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
                                 Long projectId, Long endMarketId, Long fieldCategoryId, Long userId, Long agencyId) throws  AttachmentException
    {
        boolean success = false;
/*        try
        {
            Attachment att = attachmentHelper.createAttachment(
                    getSynchroAttachment(projectId, endMarketId,agencyId, fieldCategoryId), fileName , contentType, attachment);
            pibDAO.saveAttachmentUser(att.getID(), userId);
            success = true;
        }

        catch (AttachmentException e)
        {
            throw new AttachmentException(e.getMessage(), e);
        }
  */      return success;
    }
    private SynchroAttachment getSynchroAttachment(final Long projectId, final Long endMarketId,Long agencyId, final Long fieldType) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((projectId + "-" + endMarketId + "-" + agencyId).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.PROPOSAL.toString()
                , SynchroGlobal.SynchroAttachmentObject.getById(fieldType.intValue()));
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    @Override
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception {
        /* boolean success = false;
       try
       {
           Attachment attachment = attachmentManager.getAttachment(attachmentId);
           attachmentManager.deleteAttachment(attachment);
           proposalDAO.removeDocumentAttachment(attachmentId);
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
       return success;*/
        boolean success = false;
        try
        {
            Attachment attachment = attachmentManager.getAttachment(attachmentId);
            attachmentManager.deleteAttachment(attachment);
            pibDAO.deleteAttachmentUser(attachmentId);
            // pibDAO.removeDocumentAttachment(attachmentId);
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
    public Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long projectId, final Long endMakerketId, Long agencyId)
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
            List<AttachmentBean> attachments = proposalDAO.getFieldAttachments(getSynchroAttachment(projectId, endMakerketId,agencyId, synchroAttObj.getId().longValue()));
            if(attachments != null && attachments.size() > 0) {
                attachmentMap.put(synchroAttObj.getId(), attachments);
            }
        }
        return attachmentMap;
        /*List<FieldAttachmentBean> fieldAttachment = proposalDAO.getDocumentAttachment(projectId, endMakerketId);
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

    @Override
    public void updateDocumentAttachment(Long attachmentId, Long projectId, Long agencyId, Long updatedEndMarketId)
    {
        long objectId = (projectId + "-" + updatedEndMarketId + "-" + agencyId).hashCode();
        pibDAO.updateDocumentAttachment(attachmentId, objectId);
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void removeAgency(final Long projectId, final Long endMarketId,final Long agencyId)
    {
        this.proposalDAO.removeAgency(projectId,endMarketId,agencyId);
        Map<Integer, List<AttachmentBean>> attMap = getDocumentAttachment(projectId, endMarketId, agencyId);
        if(attMap!=null && attMap.size()>0)
        {
            for(Integer attIds:attMap.keySet())
            {
                List<AttachmentBean> attBean = attMap.get(attIds);
                if(attBean!=null && attBean.size()>0)
                {
                    for(AttachmentBean attachment:attBean)
                    {
                        try
                        {
                            removeAttachment(attachment.getID());
                        }
                        catch(AttachmentException ae)
                        {
                            Log.error("Exception while removing attachment for Agency ---"+attachment.getID());
                        }
                        catch(Exception e)
                        {
                            Log.error("Exception while removing attachment for Agency ---"+attachment.getID());
                        }
                    }
                }
            }
        }

    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public Map<Integer, List<AttachmentBean>> removeAgencyOnly(final Long projectId, final Long endMarketId,final Long agencyId)
    {
        this.proposalDAO.removeAgency(projectId,endMarketId,agencyId);
        Map<Integer, List<AttachmentBean>> attMap = getDocumentAttachment(projectId, endMarketId, agencyId);
        /*if(attMap!=null && attMap.size()>0)
        {
            for(Integer attIds:attMap.keySet())
            {
                List<AttachmentBean> attBean = attMap.get(attIds);
                if(attBean!=null && attBean.size()>0)
                {
                    for(AttachmentBean attachment:attBean)
                    {
                        try
                        {
                            removeAttachment(attachment.getID());
                        }
                        catch(AttachmentException ae)
                        {
                            Log.error("Exception while removing attachment for Agency ---"+attachment.getID());
                        }
                        catch(Exception e)
                        {
                            Log.error("Exception while removing attachment for Agency ---"+attachment.getID());
                        }
                    }
                }
            }
        }*/
        return attMap;

    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void removeAttachment(Map<Integer, List<AttachmentBean>> attMap)
    {
    	 if(attMap!=null && attMap.size()>0)
        {
            for(Integer attIds:attMap.keySet())
            {
                List<AttachmentBean> attBean = attMap.get(attIds);
                if(attBean!=null && attBean.size()>0)
                {
                    for(AttachmentBean attachment:attBean)
                    {
                        try
                        {
                            removeAttachment(attachment.getID());
                        }
                        catch(AttachmentException ae)
                        {
                            Log.error("Exception while removing attachment for Agency ---"+attachment.getID());
                        }
                        catch(Exception e)
                        {
                            Log.error("Exception while removing attachment for Agency ---"+attachment.getID());
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProposalActionStandard(final List<ProposalInitiation> proposalInitiationList)
    {
        this.proposalDAO.updateProposalActionStandard(proposalInitiationList);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProposalResearchDesign(final List<ProposalInitiation> proposalInitiationList)
    {
        this.proposalDAO.updateProposalResearchDesign(proposalInitiationList);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProposalSampleProfile(final List<ProposalInitiation> proposalInitiationList)
    {
        this.proposalDAO.updateProposalSampleProfile(proposalInitiationList);
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProposalStimulusMaterial(final List<ProposalInitiation> proposalInitiationList)
    {
        this.proposalDAO.updateProposalStimulusMaterial(proposalInitiationList);
    }

    /**
     * This method will check whether a Agency can be reomoved or saved. Used when the Agencies are changed after PIB complete
     * @param projectID
     * @param endMarketId
     * @param agencyId
     * @param agencyType
     * @param pibStakeholderList
     * @return
     */
    @Override
    public Boolean checkAgencyRemoveOrSave(final Long agencyId, List<Long> sourceAgencyUsers) {

        if(sourceAgencyUsers!=null )
        {
            if(sourceAgencyUsers.contains(agencyId))
            {
                return false;
            }

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

    public ProposalDAO getProposalDAO() {
        return proposalDAO;
    }

    public void setProposalDAO(ProposalDAO proposalDAO) {
        this.proposalDAO = proposalDAO;
    }

    public ProjectSpecsDAO getProjectSpecsDAO() {
        return projectSpecsDAO;
    }

    public void setProjectSpecsDAO(ProjectSpecsDAO projectSpecsDAO) {
        this.projectSpecsDAO = projectSpecsDAO;
    }
    public ProjectSpecsManager getProjectSpecsManager() {
        return projectSpecsManager;
    }
    public void setProjectSpecsManager(ProjectSpecsManager projectSpecsManager) {
        this.projectSpecsManager = projectSpecsManager;
    }
    public PIBDAO getPibDAO() {
        return pibDAO;
    }
    public void setPibDAO(PIBDAO pibDAO) {
        this.pibDAO = pibDAO;
    }
    public ProjectManager getSynchroProjectManager() {
        return synchroProjectManager;
    }
    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

}
