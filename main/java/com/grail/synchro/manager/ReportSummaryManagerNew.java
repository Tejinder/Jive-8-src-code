package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.beans.TPDSKUDetails;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ReportSummaryManagerNew {

	List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID, final Long endMarketId);
	List<ReportSummaryInitiation> getReportSummaryInitiation(final Long projectID);

	ReportSummaryInitiation saveReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation);
	ReportSummaryInitiation updateReportSummaryDetails(final ReportSummaryInitiation reportSummaryInitiation);
    long addAttachment(File attachment,String fileName, final String contentType, Long projectId, Long endMarketId, Long fieldCategoryId, Long userId) throws IOException, AttachmentException;
    long addAttachment(InputStream attachment,String fileName, final String contentType, Long projectId, Long endMarketId, Long fieldCategoryId, Long userId); 
    
    long addTPDSKUAttachment(File attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldCategoryId, Long userId, Integer rowId) throws IOException, AttachmentException; 
    		
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
    void saveReportSummaryDetailsNew(List<ReportSummaryDetails> reportSummaryDetails);
    List<ReportSummaryDetails> getReportSummaryDetails(final Long projectID);
    List<ReportSummaryDetails> getReportSummaryDetails(final Long projectID, final int reportType, final int reportOrderId);
    void deleteReportSummaryDetailsNew(final Long projectID);
    
    void deleteAllReportSummaryDetails(final Long projectID);
    Map<Integer, Map<Integer, List<Long>>> getReportSummaryAttachmentDetails(final Long projectID);
    
    Map<Integer, Map<Integer, List<Long>>> getReportSummaryAttachmentDetails(final Long projectID, int reportType);
    
    void saveReportSummaryAttachment(final List<ReportSummaryDetails> reportSummaryDetailsList);
    void deleteReportSummaryRow(final Long projectID, final Integer reportType, final Integer reportOrderId);
    void deleteReportSummaryAttachmentRow(final Long projectID, final Integer reportType, final Integer reportOrderId);
    void deleteReportSummaryAttachment(final Long attachmentId);
    Integer getMaxReportOrderId(final Long projectID, final Integer reportType);
    Integer getMinReportOrderId(final Long projectID, final Integer reportType);
    
    Integer getMaxReportOrderId(final Long projectID);
    void updateReportOrderId(final Long projectID, final Integer reportOrderId);
    void saveReportSummaryDetailsOnly(final List<ReportSummaryDetails> reportSummaryDetailsList);
    
    Map<Integer, List<AttachmentBean>> getTPDSKUDocumentAttachment(final Long projectId, final Long endMakerketId, List<TPDSKUDetails> tpdSKUDetails);
}
