package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.grail.synchro.beans.PSMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectSpecsReporting;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ProjectSpecsManager {

	List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID, final Long endMarketId);
	List<ProjectSpecsInitiation> getProjectSpecsInitiation(final Long projectID);

    ProjectSpecsInitiation saveProjectSpecsDetails(final ProjectSpecsInitiation projectSpecsInitiation);
    void saveProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);

    ProjectSpecsInitiation updateProjectSpecsDetails(final ProjectSpecsInitiation projectSpecsInitiation);

    ProjectSpecsReporting getProjectSpecsReporting(final Long projectID, final Long endMarketId);
    ProjectSpecsEndMarketDetails getProjectSpecsEMDetails(final Long projectID, final Long endMarketId);
    List<ProjectSpecsEndMarketDetails> getProjectSpecsEMDetails(final Long projectID);

    ProjectSpecsInitiation updateProjectSpecsReporting(final ProjectSpecsInitiation projectSpecsInitiation);
    void updateProjectSpecsEMDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);
    void updateProjectSpecsFieldWorkDetails(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);
    
    HSSFWorkbook getPIBExcel(Project project, Document document);
    boolean addAttachment(File attachment,String fileName, final String contentType, Long projectId, Long endMarketId, Long fieldCategoryId, Long userId) throws IOException, AttachmentException;
    boolean addAttachment(InputStream attachment,String fileName, final String contentType, 
    		Long projectId, Long endMarketId, Long fieldCategoryId, Long userId) throws  AttachmentException ;
    boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;

    Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long projectId, final Long endMakerketId);
    void updatePIBReportingSingleEndMarketId(final Long projectID,final Long endMarketID);
    
    void updatePIBAttachmentSingleEndMarketId(final Long projectID,final Long endMarketID);
    void approveScreener(final User user, final Long projectId,final  Long endMarketId);
    void rejectScreener(final Long projectId,final  Long endMarketId);
    
    void approveQDG(final User user, final Long projectId,final  Long endMarketId);
    void rejectQDG(final Long projectId,final  Long endMarketId);
    void approve(final User user, final Long projectId,final  Long endMarketId);
    void updateProjectSpecsStatus(final Long projectID,final  Long endMarketId,final Integer status);
    void updateProjectSpecsSendForApproval(final Long projectID,final  Long endMarketId,final Integer sendForApproval);
    
    void savePSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    PSMethodologyWaiver getPSMethodologyWaiver(final Long projectID,final Long endMakerketId);
    void approvePSMethodologyWaiver(final PSMethodologyWaiver psWaiver); 
    void rejectPSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    void reqForInfoPSMethodologyWaiver(final PSMethodologyWaiver psWaiver);
    void updatePSMethWaiverSingleEndMarketId(final Long projectID,	final Long endMarketID);
    void updateProjectSpecsEndMarketId(final Long projectId, final Long endMarketId);
    void updatePSDeviation(final ProjectSpecsInitiation projectSpecsInitiation);
    void updateRequestClarificationModification(final Long projectID, final  Long endMarketId, final Integer reqClarification);
    void updateDocumentAttachment(Long attachmentId, Long projectId, Long updatedEndMarketId);
    Boolean allPSMarketSaved(final long projectID, final int endMarketSize);
    
    void updateProjectSpecsFieldWorkDetailsMM(final ProjectSpecsEndMarketDetails projectSpecsEMDetails);
    void updateProjectSpecsAMFinalCost(final Long projectId, final Long endMarketId, final BigDecimal aboveMarketFinalCost, final Integer aboveMarketFinalCostType);
    
    void deleteProjectSpecsDetails(final Long projectID);
    void deleteProjectSpecsDetails(final Long projectID, final Long endMarketID);
}
