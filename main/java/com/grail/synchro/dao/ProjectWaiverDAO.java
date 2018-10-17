package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverApprover;
import com.grail.synchro.beans.ProjectWaiverEndMarket;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public interface ProjectWaiverDAO {
    /**
     *
     * @param projectWaiver
     * @return
     */
    ProjectWaiver create(final ProjectWaiver projectWaiver);

    /**
     *
     * @param projectWaiver
     * @return
     */
    ProjectWaiver update(final ProjectWaiver projectWaiver);

    /**
    *
    * @param waiverID
    * @return
    */
    List<ProjectWaiverEndMarket> getWaiverEndMarkets(final Long waiverID);


    /**
     *
     * @param waiverID
     * @return
     */
    List<Long> getWaiverEndMarketsIDs(final Long waiverID);

    /**
     *
     * @param projectWaiver
     * @return
     */
    List<ProjectWaiverEndMarket> getWaiverEndMarkets(final ProjectWaiver projectWaiver);

    /**
     *
     * @param waiverID
     * @param endMarkets
     */
    void saveWaiverEndMarkets(final Long waiverID, final List<Long> endMarkets);

    /**
     *
     * @param projectWaiver
     * @return
     */
    boolean deleteWaiverEndMarkets(final ProjectWaiver projectWaiver);

    /**
     *
     * @param waiverID
     * @return
     */
    boolean deleteWaiverEndMarkets(final Long waiverID);

    /**
     *
     * @param projectWaiver
     */
    void saveWaiverEndMarkets(final ProjectWaiver projectWaiver);

    /**
     *
     * @param waiverID
     * @return
     */
    Long getWaiverApproverID(final Long waiverID);

    /**
     *
     * @param projectWaiver
     * @return
     */
    Long getWaiverApprover(final ProjectWaiver projectWaiver);
    
    ProjectWaiverApprover getWaiverApprover(final Long waiverID);

    /**
     *
     * @param waiverID
     * @param approverID
     */
    void saveWaiverApprover(final Long waiverID, final Long approverID);

    /**
    *
    * @param projectWaiver
    */
    void updateWaiverApprover(final ProjectWaiver projectWaiver);
    
    /**
     *
     * @param projectWaiver
     */
    void saveWaiverApprover(final ProjectWaiver projectWaiver);

    /**
     *
     * @param projectWaiver
     * @return
     */
    boolean deleteWaiverApprover(final ProjectWaiver projectWaiver);

    /**
     *
     * @param waiverID
     * @return
     */
    boolean deleteWaiverApprover(final Long waiverID);

    /**
     *
     * @param waiverID
     * @param approverID
     * @return
     */
    boolean approve(final Long waiverID, final Long approverID);

    /**
     *
     * @param projectWaiver
     * @return
     */
    boolean approve(final ProjectWaiver projectWaiver);

    /**
     *
     * @param waiverID
     * @param approverID
     * @return
     */
    boolean reject(final Long waiverID, final Long approverID);

    /**
     *
     * @param projectWaiver
     * @return
     */
    boolean reject(final ProjectWaiver projectWaiver);

    /**
     *
     * @param id
     * @return
     */
    ProjectWaiver get(final Long id);

    /**
     *
     * @param id
     * @param userID
     * @return
     */
    ProjectWaiver get(final Long id, final Long userID);

    /**
     *
     * @param name
     * @return
     */
    ProjectWaiver get(final String name);

    /**
     *
     * @return
     */
    List<ProjectWaiver> getAll();

    /**
     *
     * @param start
     * @param limit
     * @return
     */
    List<ProjectWaiver> getAll(final Integer start, final Integer limit);

    /**
     *
     * @param userID
     * @return
     */
    List<ProjectWaiver> getAll(final Long userID);
    
    List<ProjectWaiver> getAllByResultFilter(final ProjectResultFilter projectResultFilter);
    
    List<ProjectWaiver> getAllByResultFilter(final User user, final ProjectResultFilter projectResultFilter);
    
    /**
     *
     * @param userID
     * @param start
     * @param limit
     * @return
     */
    List<ProjectWaiver> getAll(final Long userID, final Integer start, final Integer limit);

    /**
     *
     * @return
     */
    Long getTotalCount();

    /**
     *
     * @return
     */
    Long getTotalCount(final Long userID);
    Long getPendingActivityTotalCount(final User user, final ProjectResultFilter filter);
    List<ProjectWaiver> getPendingApprovalWaivers(final User user, final ProjectResultFilter projectResultFilter);
    
    Boolean doesWaiverExists(final Long waiverID);
    
    Long generateWaiverID();
    
    List<AttachmentBean> getFieldAttachments(final SynchroAttachment attachment);
    
    void saveAttachmentUser(final Long attachmentId, Long userId);
    
    void updateDocumentAttachment(Long attachmentId, long objectId);
    
    void deleteAttachmentUser(final Long attachmentId);
    
    Long getAttachmentUser(final Long attachmentId); 
    
}
