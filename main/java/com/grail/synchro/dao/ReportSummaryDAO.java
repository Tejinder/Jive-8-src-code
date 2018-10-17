package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.object.SynchroAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ReportSummaryDAO {

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
}
