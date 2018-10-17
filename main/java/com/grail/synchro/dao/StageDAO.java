package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.jivesoftware.base.User;

/**
 * Operations related to stage for each Project
 * 
 * @author: Tejinder
 * @see SynchroStageToDoListBean
 */
public interface StageDAO {
    
    List<SynchroStageToDoListBean> getToDoListSequence(Long stageId);
    SynchroStageToDoListBean getToDoList(long notificationTabId);
    List<? extends User> getStageApprovers(long projectId, long stageId, String groupId);
    void updateStageApprovers(Long projectId,Long stageId,Long approverId); 
    Long getApprovalDate(Long projectID, Long stageId, Long approverId);
    

}
