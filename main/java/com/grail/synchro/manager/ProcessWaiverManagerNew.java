package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverEndMarket;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author Tejinder
 * @version 1.0
 */
public interface ProcessWaiverManagerNew {

	ProjectWaiver create(ProjectWaiver projectWaiver);
    ProjectWaiver update(ProjectWaiver projectWaiver);
    List<ProjectWaiverEndMarket> getWaiverEndMarkets(final Long waiverID);
    void saveWaiverEndMarkets(final Long waiverID, final List<Long> endMarkets);
    boolean deleteWaiverEndMarkets(final ProjectWaiver projectWaiver);
    boolean deleteWaiverEndMarkets(final Long waiverID);
    void saveWaiverEndMarkets(final ProjectWaiver projectWaiver);
    Long getWaiverApproverID(final Long waiverID);
    Long getWaiverApprover(final ProjectWaiver projectWaiver);
    void saveWaiverApprover(final Long waiverID, final Long approverID);
    void updateWaiverApprover(final ProjectWaiver projectWaiver);
    void saveWaiverApprover(final ProjectWaiver projectWaiver);
    boolean deleteWaiverApprover(final ProjectWaiver projectWaiver);
    boolean deleteWaiverApprover(final Long waiverID);
    boolean approve(final Long waiverID, final Long approverID);
    boolean approve(final ProjectWaiver projectWaiver);
    boolean reject(final Long waiverID, final Long approverID);
    boolean reject(final ProjectWaiver projectWaiver);
    ProjectWaiver get(final Long id);
    ProjectWaiver get(final Long id, final Long userID);
    ProjectWaiver get(final String name);
    List<ProjectWaiver> getAll();
    
    List<ProjectWaiver> getAllByResultFilter(final ProjectResultFilter projectResultFilter);
    
    List<ProjectWaiver> getAllByResultFilter(final User user, final ProjectResultFilter projectResultFilter);
    List<ProjectWaiver> getAll(final Integer start, final Integer limit);
    List<ProjectWaiver> getAll(final Long userID);

    List<ProjectWaiver> getAll(final Long userID, final Integer start, final Integer limit);
    Long getTotalCount();

    Long getTotalCount(final Long userID);
    Long getPendingActivityTotalCount(final User user, final ProjectResultFilter filter);
    List<ProjectWaiver> getPendingApprovalWaivers(final User user, final ProjectResultFilter projectResultFilter);
    
    Boolean doesWaiverExists(final Long waiverID);
    
    Long generateWaiverID();
 
    Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long waiverID);
    List<AttachmentBean> getFieldAttachments(final Long waiverID);
    boolean addAttachment(File attachment,String fileName, final String contentType, Long waiverID, Long userId) throws IOException, AttachmentException;
    void updateDocumentAttachment(Long attachmentId, Long waiverID);
    boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;
    Map<Long,Long> getAttachmentUser(List<AttachmentBean> attachmentBean);
    
}
