package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.jivesoftware.community.impl.dao.AttachmentBean;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.Document;

/**
 * @author: vivek
 * @since: 1.0
 */
public interface PIBManager {

    List<ProjectInitiation> getPIBDetails(final long projectID);
    List<ProjectInitiation> getPIBDetails(final long projectID, final Long endMarketID);

    ProjectInitiation savePIBDetails(final ProjectInitiation projectInitiation);
    ProjectInitiation saveMultiPIBDetails(final ProjectInitiation projectInitiation,List<EndMarketInvestmentDetail> endMarketDetails, boolean isAboveMarket, boolean canSaveOtherEndMarkets);

    ProjectInitiation updatePIBDetails(final ProjectInitiation projectInitiation, Boolean isProposalAwarded, Boolean isAdminUser);
    ProjectInitiation updateMultiMarketPIBDetails(final ProjectInitiation projectInitiation,List<EndMarketInvestmentDetail> endMarketDetails, boolean isAboveMarket,  boolean canSaveOtherEndMarkets);

    PIBReporting getPIBReporting(final Long projectID);
    PIBReporting getPIBReporting(final Long projectID, final Long endMarketId);

    ProjectInitiation updatePIBReporting(final ProjectInitiation projectInitiation);
    void updatePIBMultiMarketReporting(final ProjectInitiation projectInitiation);
    
    ProjectInitiation updatePIBStakeholderList(final ProjectInitiation projectInitiation);
    void updatePIBMultiMarketStakeholderList(final ProjectInitiation projectInitiation);
    PIBStakeholderList getPIBStakeholderList(final Long projectID,final Long endMakerketId);
    PIBStakeholderList getPIBStakeholderList(final Long projectID);
    List<PIBStakeholderList> getPIBStakeholderListMultiMarket(final Long projectID);

    HSSFWorkbook getPIBExcel(Project project, Document document);
    List<AttachmentBean> getFieldAttachments(final Long projectId, final Long endmarketId, final Long fieldType);
    boolean addAttachment(File attachment,String fileName, final String contentType, Long projectId, Long endMarketId, Long fieldType, Long userId) throws IOException, AttachmentException;
    boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;

    Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long projectId, final Long endMakerketId);
    void savePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    PIBMethodologyWaiver getPIBMethodologyWaiver(final Long projectID,final Long endMakerketId);
    void approvePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver); 
    void rejectPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    void reqForInfoPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    void updatePIBMethWaiverSingleEndMarketId(final Long projectID,	final Long endMarketID);
    void updatePIBReportingSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBStakeholderListSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBStatus(final Long projectID,final Integer status);
    Map<Long,Long> getAttachmentUser(List<AttachmentBean> attachmentBean);
    void updatePIBDeviation(final ProjectInitiation projectInitiation);
    void updateDocumentAttachment(Long attachmentId, Long projectId, Long updatedEndMarketId);
    void updatePIBActionStandard(final ProjectInitiation projectInitiation);
    
    void updatePIBResearchDesign(final ProjectInitiation projectInitiation);
    void updatePIBSampleProfile(final ProjectInitiation projectInitiation);
    void updatePIBStimulusMaterial(final ProjectInitiation projectInitiation);
    Boolean allPIBMarketSaved(final long projectID, final int endMarketSize);
    
    void deletePIBEndMarket(final Long projectID);
    void deletePIBMWEndMarket(final Long projectID);
    void deletePIBReportingEndMarket(final Long projectID);
    void deletePIBStakeholderEndMarket(final Long projectID);
    
    void deletePIBEndMarket(final Long projectID,final Long endMarketID);
    void deletePIBMWEndMarket(final Long projectID, final Long endMarketID);
    void deletePIBReportingEndMarket(final Long projectID, final Long endMarketID);
    void deletePIBStakeholderEndMarket(final Long projectID, final Long endMarketID);
    
    void updatePIBBusinessQuestion(final ProjectInitiation projectInitiation);
    void updatePIBResearchObjective(final ProjectInitiation projectInitiation);
    void updatePIBNotifyAboveMarketContact(final Long projectId, final Long endMarketId, final Integer notifyAboveMarketContact);
    void updatePIBApproveChanges(final Long projectId, final Long endMarketId, final Integer approveChanges);
    void updateNotifySPI(final Long projectId, final Integer notifySPI);
    void updateNotifyPO(final Long projectId, final Integer notifyPO);
    void updateLatestEstimate(final ProjectInitiation projectInitiation);
    void updateMultiMarketOtherEM(final ProjectInitiation projectInitiation);
    
    PIBMethodologyWaiver getPIBKantarMethodologyWaiver(final Long projectID,final Long endMakerketId);
    void approvePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver); 
    void rejectPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    void savePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    void reqForInfoPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    Long getProjectId(final Long waiverId);
    void updateOtherSPIContact(final String otherSPIContact, final Long projectID, final Long endMarketID);
    
    void updateOtherLegalContact(final String otherLegalContact, final Long projectID, final Long endMarketID);
    
    void updateOtherProductContact(final String otherProductContact, final Long projectID, final Long endMarketID);
    String getOtherSPIContact(final Long projectID, final Long endMarketId);
    
    String getOtherLegalContact(final Long projectID, final Long endMarketId);
    String getOtherProductContact(final Long projectID, final Long endMarketId);
    void updateAgencyContact(final Long projectID, final Long endMarketId, final Long updatedAgencyContact);
    
    void updateOtherAgencyContact(final String otherAgencyContact, final Long projectID, final Long endMarketID);
    String getOtherAgencyContact(final Long projectID, final Long endMarketId);
    
}
