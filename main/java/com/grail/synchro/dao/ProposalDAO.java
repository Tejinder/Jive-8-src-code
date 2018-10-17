package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.FieldAttachmentBean;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalEndMarketDetails;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ProposalReporting;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ProposalDAO {

	public ProposalInitiation save(final ProposalInitiation proposalInitiation);

	ProposalInitiation update(final ProposalInitiation proposalInitiation);

    List<ProposalInitiation> getProposalInitiation(final Long projectID, final Long endMarketId, final Long agencyId);
    List<ProposalInitiation> getProposalInitiation(final Long projectID);
    List<ProposalInitiation> getProposalInitiation(final Long projectID, final Long agencyId);

    ProposalInitiation saveProposalReporting(final ProposalInitiation proposalInitiation);
    void saveProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails);
   
    ProposalInitiation updateProposalReporting(final ProposalInitiation proposalInitiation);
    

    List<ProposalReporting> getProposalReporting(final Long projectID, final Long endMarketId, final Long agencyId);
    List<ProposalEndMarketDetails> getProposalEMDetails(final Long projectID, final Long endMarketId, final Long agencyId);
    List<ProposalEndMarketDetails> getProposalEMDetails(final Long projectID, final Long agencyId);
    
    void updateProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails);
    void submitProposal(final Long projectID, final Long agencyId);
    void awardAgency(final Long projectID, final Long agencyId);
    void rejectAgency(final Long projectID, final Long agencyId, final Integer status);
    
    int addDocumentAttachment(final Long projectId, final Long endMarketId, final Long fieldCategoryId, 
    		final Long attachmentId, final Long userId);
    boolean removeDocumentAttachment(final Long attachmentId);
    List<AttachmentBean> getFieldAttachments(final SynchroAttachment attachment);
    
    void updatePIBReportingSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBStakeholderListSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBAttachmentSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updateProposalStatus(final Long projectID,final List<ProposalInitiation> proposalInitiationList,final Integer status);
    void updateProposalEndMarketId(final Long projectId, final Long agencyId, final Long endMarketId);
    void updateSendToProjectOwner(final Long projectID, final Long agencyId, final Integer sendToProjectOwner);
    void updateRequestClarificationModification(final Long projectID, final Long agencyId, final Integer reqClarification);
    void removeAgency(final Long projectId, final Long endMarketId,final Long agencyId);
    void saveAgency(final Long projectID,final List<ProposalInitiation> proposalInitiationList);
    
    void updateProposalActionStandard(final List<ProposalInitiation> proposalInitiationList);
    void updateProposalResearchDesign(final List<ProposalInitiation> proposalInitiationList);
    void updateProposalSampleProfile(final List<ProposalInitiation> proposalInitiationList);
    void updateProposalStimulusMaterial(final List<ProposalInitiation> proposalInitiationList);
    void updateProposalSubimtted(final Long projectID, final Integer isSubmitted);
    void updateAgency(final Long projectId,  final Long endMarketId, final Long updatedAgencyId, final Long sourceAgencyId);
    
}
