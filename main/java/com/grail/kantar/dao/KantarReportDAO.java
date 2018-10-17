package com.grail.kantar.dao;

import com.grail.kantar.beans.KantarReportBean;
import com.grail.kantar.beans.KantarReportResultFilter;
import com.grail.kantar.object.KantarAttachment;
import com.jivesoftware.base.User;
import com.jivesoftware.community.impl.dao.AttachmentBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public interface KantarReportDAO {

    Long save(final KantarReportBean kantarReportBean);
    Long update(final KantarReportBean kantarReportBean);
    KantarReportBean get(final Long id);
    KantarReportBean get(final Long id, final Long userId);
    List<KantarReportBean> getAll(final Long userId);
    List<KantarReportBean> getAll();
    List<KantarReportBean> getAll(final KantarReportResultFilter kantarReportResultFilter);
    List<KantarReportBean> getAll(final KantarReportResultFilter kantarReportResultFilter, final User owner);
    Integer getTotalCount(final KantarReportResultFilter kantarReportResultFilter);
    Integer getTotalCount(final KantarReportResultFilter kantarReportResultFilter, final User owner);
    List<AttachmentBean> getKantarReportAttachments(final long objectId, final int objectType);
    List<AttachmentBean> getKantarReportAttachments(final KantarAttachment kantarAttachment);
    public void saveAttachmentUser(final Long attachmentId, final Long userId);
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId);
    public void deleteAttachmentUser(final Long attachmentId);
    public void deleteAttachmentUser(final List<Long> attachmentIds);
    public Long getAttachmentUser(final Long attachmentId);
    public List<Long> getAuthors();
}
