package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProposalEndMarketDetails;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ProposalReporting;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ProposalManager {

    List<ProposalInitiation> getProposalDetails(final Long projectID, final Long endMarketId, final Long agencyId);
    List<ProposalInitiation> getProposalDetails(final Long projectID);
    List<ProposalInitiation> getProposalDetails(final Long projectID, final Long agencyId);
    
    ProposalInitiation saveProposalDetails(final ProposalInitiation proposalInitiation);
    void saveProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails);

    ProposalInitiation updateProposalDetails(final ProposalInitiation projectInitiation);

    ProposalReporting getProposalReporting(final Long projectID, final Long endMarketId, final Long agencyId);
    ProposalEndMarketDetails getProposalEMDetails(final Long projectID, final Long endMarketId, final Long agencyId);
    Map<Long, ProposalEndMarketDetails> getProposalEMDetails(final Long projectID, final Long agencyId);

    ProposalInitiation updateProposalReporting(final ProposalInitiation proposalInitiation);
    void updateProposalEMDetails(final ProposalEndMarketDetails proposalEMDetails);
    void submitProposal(final Long projectID, final Long agencyId);
    void awardAgency(final Project project, final Long agencyId, final Long endMarketId, Map<Integer, List<AttachmentBean>> attachmentMap);
    void awardMultiMarketAgency(final Project project, final Long agencyId,final Long endMarketId, Map<Integer, List<AttachmentBean>> attachmentMap);
    
    void rejectAgency(final Project project, final Long agencyId, final Long endMarketId, final Integer status);
    
    
    HSSFWorkbook getPIBExcel(Project project, Document document);
    boolean addAttachment(File attachment,String fileName, final String contentType, Long projectId, Long endMarketId, Long fieldCategoryId, Long userId, Long agencyId) throws IOException, AttachmentException;
    boolean addAttachment(InputStream attachment,String fileName, final String contentType, Long projectId, Long endMarketId, Long fieldCategoryId, Long userId, Long agencyId) throws  AttachmentException;
    
    boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;

    Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long projectId, final Long endMakerketId, final Long agencyId);
    
    void updatePIBReportingSingleEndMarketId(final Long projectID,final Long endMarketID);
    
    void updatePIBAttachmentSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updateProposalEndMarketId(final Long projectId, final Long agencyId, final Long endMarketId);
    void updateSendToProjectOwner(final Long projectID, final Long agencyId, final Integer sendToProjectOwner);
    void updateRequestClarificationModification(final Long projectID, final Long agencyId, final Integer reqClarification);
    void updateDocumentAttachment(Long attachmentId, Long projectId, Long agencyId, Long updatedEndMarketId);
    void removeAgency(final Long projectId, final Long endMarketId,final Long agencyId);
    
    Map<Integer, List<AttachmentBean>>  removeAgencyOnly(final Long projectId, final Long endMarketId,final Long agencyId);
    
    void updateProposalActionStandard(final List<ProposalInitiation> proposalInitiationList);
    void updateProposalResearchDesign(final List<ProposalInitiation> proposalInitiationList);
    void updateProposalSampleProfile(final List<ProposalInitiation> proposalInitiationList);
    void updateProposalStimulusMaterial(final List<ProposalInitiation> proposalInitiationList);
    Boolean  checkAgencyRemoveOrSave(final Long agencyId, List<Long> sourceAgencyUsers);
    void updateAgency(final Long projectId,  final Long endMarketId, final Long updatedAgencyId, final Long sourceAgencyId);
    void removeAttachment(Map<Integer, List<AttachmentBean>> attMap);
}
