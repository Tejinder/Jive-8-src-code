package com.grail.kantar.manager;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplateFilter;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/16/14
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */
public interface KantarBriefTemplateManager {
    Long save(final KantarBriefTemplate briefTemplate);
    KantarBriefTemplate get(final Long id);
    KantarBriefTemplate get(final Long id, final Long userId);
    List<KantarBriefTemplate> getAll(final Long userId);
    List<KantarBriefTemplate> getAll();
    List<KantarBriefTemplate> getAll(final KantarBriefTemplateFilter kantarBriefTemplateFilter);
    Integer getTotalCount(final KantarBriefTemplateFilter kantarBriefTemplateFilter);
    KantarBriefTemplate getDraftTemplate(final Long userId);
    void deleteDraftTemplate(final Long userId);

    List<AttachmentBean> getKantarAttachments(final Long projectId);
    public boolean addKantarAttachments(final File attachment, final String fileName, final String contentType,
                                         final Long projectId, final Long userId) throws IOException, AttachmentException;

    public void saveAttachmentUser(final Long attachmentId, final Long userId);
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId);
    public void deleteAttachmentUser(final Long attachmentId);
    public void deleteAttachmentUser(final List<Long> attachmentIds);
    public Long getAttachmentUser(final Long attachmentId);
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;

    public List<KantarBriefTemplate> getPendingActivities(final Long userId);
}
