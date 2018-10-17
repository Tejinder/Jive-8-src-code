package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ReportSummaryManager {

	List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID, final Long endMarketId);
	List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID);

	ReportSummaryInitiation saveReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation);
	ReportSummaryInitiation updateReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation);
    boolean addAttachment(File attachment,String fileName, final String contentType, Long projectId, Long endMarketId, Long fieldCategoryId, Long userId) throws IOException, AttachmentException;
    boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;
    Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long projectId, final Long endMakerketId);
    void approveSPI(final User user, final Long projectId,final  Long endMarketId);
    void approveLegal(final User user, final Long projectId,final  Long endMarketId);
    void updateReportSummaryStatus(final Long projectID,final  Long endMarketId,final Integer status);
    void updateSendForApproval(final long projectId, final long endMarketId, final Integer sendForApproval);
    void updateNeedRevision(final long projectId, final long endMarketId);
    Boolean allRepSummaryMarketSaved(final long projectID, final int endMarketSize);
    void updateSendForApproval(final long projectId, final Integer sendForApproval);
    void approveSPI(final User user, final Long projectId);
    
    void updateFullReport(final long projectId, final long endMarketId, final Integer fullReport);
    void updateFullReport(final long projectId, final Integer fullReport);
    void updateSummaryForIris(final long projectId, final long endMarketId, final Integer summaryForIris);
    void updateSummaryForIris(final long projectId, final Integer summaryForIris);
    void deleteReportSummaryDetails(final Long projectID, final Long endMarketID);
    
    void updateUploadToIRIS(final long projectId, final long endMarketId, final Integer uploadToIRIS);
    void updateUploadToCPSIDatabase(final long projectId, final long endMarketId, final Integer uploadToCPSIDatabase);
    SynchroToIRIS getSynchroToIRIS(final Long projectID, final Long endMarketId, Project project);
    void updateSynchroToIRIS(final SynchroToIRIS synchroToIRIS);
}
