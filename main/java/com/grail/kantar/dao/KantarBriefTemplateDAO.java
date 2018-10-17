package com.grail.kantar.dao;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplateFilter;
import com.grail.kantar.object.KantarAttachment;
import com.jivesoftware.community.impl.dao.AttachmentBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/16/14
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface KantarBriefTemplateDAO {
    Long save(final KantarBriefTemplate briefTemplate);
    Long update(final KantarBriefTemplate briefTemplate);
    KantarBriefTemplate get(final Long id);
    KantarBriefTemplate get(final Long id, final Long userId);
    List<KantarBriefTemplate> getAll(final Long userId);
    List<KantarBriefTemplate> getAll();
    List<KantarBriefTemplate> getAll(final KantarBriefTemplateFilter kantarBriefTemplateFilter);
    Integer getTotalCount(final KantarBriefTemplateFilter kantarBriefTemplateFilter);
    KantarBriefTemplate getDraftTemplate(final Long userId);
    void deleteDraftTemplate(final Long userId);
    List<AttachmentBean> getKantarAttachments(final long objectId, final int objectType);
    List<AttachmentBean> getKantarAttachments(final KantarAttachment kantarAttachment);
    public void saveAttachmentUser(final Long attachmentId, final Long userId);
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId);
    public void deleteAttachmentUser(final Long attachmentId);
    public void deleteAttachmentUser(final List<Long> attachmentIds);
    public Long getAttachmentUser(final Long attachmentId);
    public List<KantarBriefTemplate> getPendingActivities(final Long userId);
}
