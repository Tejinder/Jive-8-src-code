package com.grail.dao;

import com.grail.beans.GrailBriefTemplate;
import com.grail.beans.GrailBriefTemplateFilter;
import com.grail.object.GrailAttachment;
import com.jivesoftware.community.impl.dao.AttachmentBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/1/14
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GrailBriefTemplateDAO {
    Long save(final GrailBriefTemplate briefTemplate);
    Long update(final GrailBriefTemplate briefTemplate);
    GrailBriefTemplate get(final Long id);
    GrailBriefTemplate get(final Long id, final Long userId);
    List<GrailBriefTemplate> getAll(final Long userId);
    List<GrailBriefTemplate> getAll();
    List<GrailBriefTemplate> getAll(final GrailBriefTemplateFilter grailBriefTemplateFilter);
    Integer getTotalCount(final GrailBriefTemplateFilter grailBriefTemplateFilter);
    GrailBriefTemplate getDraftTemplate(final Long userId);
    void deleteDraftTemplate(final Long userId);
    List<AttachmentBean> getGrailAttachments(final long objectId, final int objectType);
    List<AttachmentBean> getGrailAttachments(final GrailAttachment grailAttachment);
    public void saveAttachmentUser(final Long attachmentId, final Long userId);
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId);
    public void deleteAttachmentUser(final Long attachmentId);
    public void deleteAttachmentUser(final List<Long> attachmentIds);
    public Long getAttachmentUser(final Long attachmentId);
    public List<GrailBriefTemplate> getPendingActivities(final Long userId);
}
