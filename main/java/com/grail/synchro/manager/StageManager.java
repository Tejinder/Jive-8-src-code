package com.grail.synchro.manager;

import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.jivesoftware.base.User;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.mail.EmailMessage;

/**
 * This will contain all the operations performed on a particular stage for a project
 *
 * @author: Tejinder
 */
public interface StageManager {

    Map<String, String> getStageApprovers(Long stageId, Project project);
    EmailMessage populateNotificationEmail(String recipients, String subject, String messageBody, String htmlProp, String subProp);
    List<SynchroStageToDoListBean> getDisabledToDoListTabs(long stageId);
    List<SynchroStageToDoListBean> getDisabledToDoListTabs(User currentUser, long projectId, long stageId, String projectName, long endMarketId, String baseUrl);
    void updateStageStatus(long projectId,long endMarketId, int stageId, int statu, User user, Map<Integer, List<AttachmentBean>> attachmentMap);
    void updateMultiMarketStageStatus(long projectId, long endMarketId, int stageId, int status, User user, Map<Integer, List<AttachmentBean>> attachmentMap);
    Map<String, String> getPendingActivity(Project project);
    void sendNotification(User user,EmailMessage message);
    List<SynchroStageToDoListBean> getToDoListTabs(User currentUser, long projectId, long stageId, String projectName, long endMarketId, String baseUrl);
    void saveAgency(Long projectId, Long endMarketId, Long agencyId, int status, User user, Map<Integer, List<AttachmentBean>> attachmentMap);
    String getNotificationRecipients(String notificationRoles,long projectId,long endMarketId);
    
    void updateActivatedProposalEM(long projectId, long endMarketId, int stageId, int status, User user, Map<Integer, List<AttachmentBean>> attachmentMap);
    void copyAttachments(Map<Integer, List<AttachmentBean>> attachmentMap, Long projectId, Long endMarketId,
            Long userId, Long agencyId);
    
    void sendNotificationNew(String senderName, String senderEmail, EmailMessage message);

}
