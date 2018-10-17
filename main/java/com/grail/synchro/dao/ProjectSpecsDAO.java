package com.grail.synchro.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.grail.synchro.beans.PSMethodologyWaiver;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectSpecsReporting;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ProjectSpecsDAO {

	ProjectSpecsInitiation save(final ProjectSpecsInitiation projectSpecsInitiation);

	ProjectSpecsInitiation update(final ProjectSpecsInitiation projectSpecsInitiation);

	List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID, final Long endMarketId);
	List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID);

	ProjectSpecsInitiation saveProjectSpecsReporting(final ProjectSpecsInitiation projectSpecsInitiation);
	void saveProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);
   
    ProjectSpecsInitiation updateProjectSpecsReporting(final ProjectSpecsInitiation projectSpecsInitiation);
    List<ProjectSpecsReporting> getProjectSpecsReporting(final Long projectID, final Long endMarketId);
    List<ProjectSpecsEndMarketDetails> getProjectSpecsEMDetails(final Long projectID, final Long endMarketId);
    List<ProjectSpecsEndMarketDetails> getProjectSpecsEMDetails(final Long projectID);
    void updateProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);
    void updateProjectSpecsFieldWorkDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);
    
    void updateProjectSpecsFieldWorkDetailsMM(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);
    
    int addDocumentAttachment(final Long projectId, final Long endMarketId, final Long fieldCategoryId, 
    		final Long attachmentId, final Long userId);
    boolean removeDocumentAttachment(final Long attachmentId);
    List<AttachmentBean> getFieldAttachments(final SynchroAttachment attachment);
    
    void updatePIBReportingSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBStakeholderListSingleEndMarketId(final Long projectID,final Long endMarketID);
    void updatePIBAttachmentSingleEndMarketId(final Long projectID,final Long endMarketID);
    void approveScreener(final User user, final Long projectId,final  Long endMarketId);
    void rejectScreener(final Long projectId,final Long endMarketId);
    void approveQDG(final User user, final Long projectId,final  Long endMarketId);
    void rejectQDG(final Long projectId,final  Long endMarketId);
    void approve(final User user, final Long projectId,final  Long endMarketId);
    void updateProjectSpecsStatus(final Long projectID,final  Long endMarketId,final Integer status);
    void updateProjectSpecsSendForApproval(final Long projectID,final  Long endMarketId,final Integer sendForApproval);
    
    void savePSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    void updatePSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    List<PSMethodologyWaiver> getPSMethodologyWaiver(final Long projectID,final Long endMakerketId);
    void approvePSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    void rejectPSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    void reqForInfoPSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    
    void updatePSMethWaiverSingleEndMarketId(final Long projectID,	final Long endMarketID);
    void updateProjectSpecsEndMarketId(final Long projectId, final Long endMarketId);
    void updatePSDeviation(final ProjectSpecsInitiation projectSpecsInitiation);
    void updateRequestClarificationModification(final Long projectID, final  Long endMarketId, final Integer reqClarification);
    
    void savePS_PIBMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    void updateProjectSpecsEndDate(final Long projectId, final Long endMarketId, final Date projectEndDate);
    void updateProjectSpecsEndDate(final Long projectId, final Date projectEndDate);
    void updateProjectSpecsAMFinalCost(final Long projectId, final Long endMarketId, final BigDecimal aboveMarketFinalCost, final Integer aboveMarketFinalCostType);
    void deleteProjectSpecsDetails(final Long projectID);
    void deleteProjectSpecsDetails(final Long projectID, final Long endMarketID);
}
