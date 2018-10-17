package com.grail.synchro.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.springframework.dao.DataAccessException;

import com.grail.synchro.beans.FieldAttachmentBean;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: vivek
 * @since: 1.0
 */
public interface PIBDAONew {

    ProjectInitiation save(final ProjectInitiation projectInitiation);

    ProjectInitiation update(final ProjectInitiation projectInitiation);
    ProjectInitiation updateNew(final ProjectInitiation projectInitiation);
    ProjectInitiation updateMultiMarket(final ProjectInitiation projectInitiation); 

    List<ProjectInitiation> getProjectInitiation(final Long projectID);
    List<ProjectInitiation> getProjectInitiationNew(final Long projectID);
   List<ProjectInitiation> getProjectInitiation(final Long projectID, final Long endMarketId);
   

    ProjectInitiation savePIBReporting(final ProjectInitiation projectInitiation);
    
    ProjectInitiation savePIBStakeholderList(final ProjectInitiation projectInitiation);

    ProjectInitiation updatePIBReporting(final ProjectInitiation projectInitiation);
    void updatePIBMultiMarketReporting(final ProjectInitiation projectInitiation);
    ProjectInitiation updatePIBStakeholderList(final ProjectInitiation projectInitiation);
    ProjectInitiation updatePIBMultiMarketStakeholderList(final ProjectInitiation projectInitiation);

    List<PIBReporting> getPIBReporting(final Long projectID);
    List<PIBReporting> getPIBReporting(final Long projectID, final Long endMarketId);
    List<PIBStakeholderList> getPIBStakeholderList(final Long projectID, final Long endMarketId);
    List<PIBStakeholderList> getPIBStakeholderList(final Long projectID);
    int addDocumentAttachment(final Long projectId, final Long endMarketId, final Long fieldCategoryId, 
    		final Long attachmentId, final Long userId);
    boolean removeDocumentAttachment(final Long attachmentId);
    List<FieldAttachmentBean> getDocumentAttachment(final Long projectId, final Long endMakerketId);
    AttachmentBean getDocumentAttachment(final SynchroAttachment attachment);
    List<AttachmentBean> getFieldAttachments(final SynchroAttachment attachment);
    void savePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver); 
    void updatePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    List<PIBMethodologyWaiver> getPIBMethodologyWaiver(final Long projectID,final Long endMakerketId);
    void approvePIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver); 
    void rejectPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    void reqForInfoPIBMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    
    void updatePIBMethWaiverSingleEndMarketId(final Long projectID,	final Long endMarketID); 
    void updatePIBReportingSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBStakeholderListSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBStatus(final Long projectID,final Integer status);
    void saveAttachmentUser(final Long attachmentId, Long userId);
    void deleteAttachmentUser(final Long attachmentId); 
    Long getAttachmentUser(final Long attachmentId); 
    void updatePIBDeviation(final ProjectInitiation projectInitiation);
    void updateDocumentAttachment(Long attachmentId, long objectId);
    void updatePIBNonAgencyStakeholderList(final ProjectInitiation projectInitiation);
    
    void updatePIBActionStandard(final ProjectInitiation projectInitiation);
    void updatePIBResearchDesign(final ProjectInitiation projectInitiation);
    void updatePIBSampleProfile(final ProjectInitiation projectInitiation);
    void updatePIBStimulusMaterial(final ProjectInitiation projectInitiation);
    
    void updatePIBBusinessQuestion(final ProjectInitiation projectInitiation);
    void updatePIBResearchObjective(final ProjectInitiation projectInitiation);
    
    void deletePIBEndMarket(final Long projectID);
    void deletePIBMWEndMarket(final Long projectID);
    void deletePIBReportingEndMarket(final Long projectID);
    void deletePIBStakeholderEndMarket(final Long projectID);
    
    void deletePIBEndMarket(final Long projectID,final Long endMarketID);
    void deletePIBMWEndMarket(final Long projectID, final Long endMarketID);
    void deletePIBReportingEndMarket(final Long projectID, final Long endMarketID);
    void deletePIBStakeholderEndMarket(final Long projectID, final Long endMarketID);
    
    void updatePIBNotifyAboveMarketContact(final Long projectId, final Long endMarketId, final Integer notifyAboveMarketContact);
    void updatePIBApproveChanges(final Long projectId, final Long endMarketId, final Integer approveChanges);
    void updateNotifySPI(final Long projectId, final Integer notifySPI);
    void updateNotifyPO(final Long projectId, final Integer notifyPO);
    
    ProjectInitiation saveOtherEM(final ProjectInitiation projectInitiation);
    void updateMultiMarketOtherEM(final ProjectInitiation projectInitiation);
    
    ProjectInitiation savePIBLegalContact(final ProjectInitiation projectInitiation);
    void updatePIBMultiMarketLegalContact(final ProjectInitiation projectInitiation);
    void updateLatestEstimate(final ProjectInitiation projectInitiation);
    
    List<PIBMethodologyWaiver> getPIBKantarMethodologyWaiver(final Long projectID,final Long endMakerketId);
    void approvePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver); 
    void rejectPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    void reqForInfoPIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    void savePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver); 
    void updatePIBKantarMethodologyWaiver(final PIBMethodologyWaiver pibWaiver);
    Long getProjectId(final Long waiverId);
    void saveOtherSPIContact(final String otherSPIContact, final Long projectID, final Long endMarketID);
    void updateOtherSPIContact(final String otherSPIContact, final Long projectID, final Long endMarketID);
    
    void saveOtherLegalContact(final String otherLegalContact, final Long projectID, final Long endMarketID);
    void updateOtherLegalContact(final String otherLegalContact, final Long projectID, final Long endMarketID);
    
    void saveOtherProductContact(final String otherProductContact, final Long projectID, final Long endMarketID);
    void updateOtherProductContact(final String otherProductContact, final Long projectID, final Long endMarketID);
    String getOtherSPIContact(final Long projectID, final Long endMarketId);
    String getOtherLegalContact(final Long projectID, final Long endMarketId);
    String getOtherProductContact(final Long projectID, final Long endMarketId);
    void updateAgencyContact(final Long projectID, final Long endMarketId, final Long updatedAgencyContact);
    void updateOtherAgencyContact(final String otherAgencyContact, final Long projectID, final Long endMarketID);
    void saveOtherAgencyContact(final String otherAgencyContact, final Long projectID, final Long endMarketID);
    String getOtherAgencyContact(final Long projectID, final Long endMarketId);
    void updatePIBCompletionDate(final Long projectID,final Date pibCompletionDate) ;
    void updatePIBSendForApproval(final ProjectInitiation projectInitiation);
    void updatePIBNeedsDiscussion(final ProjectInitiation projectInitiation);
    void confirmLegalApprovalSubmission(final ProjectInitiation projectInitiation);
    List<ProjectInitiation> getProjectInitiationPendingActivities(ProjectResultFilter projectFilter);
   
    List<PIBMethodologyWaiver> getPIBMethodologyWaivers(final ProjectResultFilter projectResultFilter); 
    Long getPIBMethodologyWaiversTotalCount(final ProjectResultFilter filter);
    
    List<PIBMethodologyWaiver> getPIBAgencyWaivers(final ProjectResultFilter projectResultFilter); 
    Long getPIBAgencyWaiversTotalCount(final ProjectResultFilter filter);
    
    List<PIBMethodologyWaiver> getPIBPendingMethodologyWaivers(final ProjectResultFilter projectResultFilter);
    Long getPIBPendingMethodologyWaiversTotalCount(final ProjectResultFilter filter);
    
    List<PIBMethodologyWaiver> getPIBPendingAgencyWaivers(final ProjectResultFilter projectResultFilter);
    Long getPIBPendingAgencyWaiversTotalCount(final ProjectResultFilter filter);
    
    void updatePIBSendReminder(final ProjectInitiation projectInitiation);
    
    void resetPIB(final ProjectInitiation projectInitiation);
}
