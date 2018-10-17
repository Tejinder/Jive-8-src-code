package com.grail.manager;

import com.grail.beans.GrailBriefTemplate;
import com.grail.beans.GrailBriefTemplateFilter;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/1/14
 * Time: 11:49 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GrailBriefTemplateManager {
    Long save(final GrailBriefTemplate briefTemplate);
    GrailBriefTemplate get(final Long id);
    GrailBriefTemplate get(final Long id, final Long userId);
    List<GrailBriefTemplate> getAll(final Long userId);
    List<GrailBriefTemplate> getAll();
    List<GrailBriefTemplate> getAll(final GrailBriefTemplateFilter grailBriefTemplateFilter);
    Integer getTotalCount(final GrailBriefTemplateFilter grailBriefTemplateFilter);
    GrailBriefTemplate getDraftTemplate(final Long userId);
    void deleteDraftTemplate(final Long userId);
    List<AttachmentBean> getGrailAttachments(final Long id);
    public boolean addGrailAttachments(final File attachment, final String fileName, final String contentType,
                                        final Long id, final Long userId) throws IOException, AttachmentException;

    public void saveAttachmentUser(final Long attachmentId, final Long userId);
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId);
    public void deleteAttachmentUser(final Long attachmentId);
    public void deleteAttachmentUser(final List<Long> attachmentIds);
    public Long getAttachmentUser(final Long attachmentId);
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;
    public List<GrailBriefTemplate> getPendingActivities(final Long userId);
}
