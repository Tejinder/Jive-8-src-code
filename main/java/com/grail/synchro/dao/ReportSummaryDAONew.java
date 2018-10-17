package com.grail.synchro.dao;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ReportSummaryDAONew {

	ReportSummaryInitiation save(final ReportSummaryInitiation reportSummaryInitiation);
	

	ReportSummaryInitiation update(final ReportSummaryInitiation reportSummaryInitiation);

	List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID, final Long endMarketId);
	List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID);
   
    List<AttachmentBean> getFieldAttachments(final SynchroAttachment attachment);
    void approveSPI(final User user, final Long projectId,final  Long endMarketId);
    void approveLegal(final User user, final Long projectId,final  Long endMarketId);
    void updateReportSummaryStatus(final Long projectID,final  Long endMarketId,final Integer status); 
    void updateSendForApproval(final long projectId, final long endMarketId, final Integer sendForApproval);
    void updateNeedRevision(final long projectId, final long endMarketId);
    void updateSendForApproval(final long projectId, final Integer sendForApproval);
    void approveSPI(final User user, final Long projectId);
    
   void updateFullReport(final long projectId, final long endMarketId, final Integer fullReport);
   void updateFullReport(final long projectId, final Integer fullReport);
   void updateSummaryForIris(final long projectId, final long endMarketId, final Integer summaryForIris);
   void updateSummaryForIris(final long projectId, final Integer summaryForIris);
   void deleteReportSummaryDetails(final Long projectID, final Long endMarketID);
   void updateUploadToIRIS(final long projectId, final long endMarketId, final Integer uploadToIRIS);
   void updateUploadToCPSIDatabase(final long projectId, final long endMarketId, final Integer uploadToCPSIDatabase);
   List<SynchroToIRIS> getSynchroToIRIS(final Long projectID, final Long endMarketId); 
   void updateSynchroToIRIS(final SynchroToIRIS synchroToIRIS);
   ReportSummaryDetails saveReportSummaryDetails(final ReportSummaryDetails reportSummaryDetails);
   void saveReportSummaryAttachmentDetails(final ReportSummaryDetails reportSummaryDetails);
   List<ReportSummaryDetails> getReportSummaryDetails(final Long projectID);
   List<ReportSummaryDetails> getReportSummaryDetails(final Long projectID, final int reportType, final int reportOrderId);
   
   List<ReportSummaryDetails> getReportSummaryAttachmentDetails(final Long projectID);
   
   List<ReportSummaryDetails> getReportSummaryAttachmentDetails(final Long projectID, final int reportType);
   
   void deleteReportSummaryDetailsNew(final Long projectID);
   void deleteAllReportSummaryDetails(final Long projectID);
   void deleteReportSummaryAttachDetails(final Long projectID);
   List<Long> getReportSummaryAttachmentIds(final Long projectID, final int reportType, final int reportOrderId);
   void deleteReportSummaryRow(final Long projectID, final Integer reportType, final Integer reportOrderId);
   void deleteReportSummaryAttachmentRow(final Long projectID, final Integer reportType, final Integer reportOrderId);
   void deleteReportSummaryAttachment(final Long attachmentId);
   Integer getMaxReportOrderId(final Long projectID, final Integer reportType);
   
   Integer getMinReportOrderId(final Long projectID, final Integer reportType);
   
   Integer getMaxReportOrderId(final Long projectID);
   void updateReportOrderId(final Long projectID, final Integer reportOrderId);
}
