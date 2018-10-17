package com.grail.kantar.manager;

import com.grail.kantar.beans.KantarReportBean;
import com.grail.kantar.beans.KantarReportResultFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */
public interface KantarReportManager {
    Long save(final KantarReportBean kantarReportBean);
    KantarReportBean get(final Long id);
    KantarReportBean get(final Long id, final Long userId);
    List<KantarReportBean> getAll(final Long userId);
    List<KantarReportBean> getAll();
    List<KantarReportBean> getAll(final KantarReportResultFilter kantarReportResultFilter);
    List<KantarReportBean> getAll(final KantarReportResultFilter kantarReportResultFilter, final User owner);
    Integer getTotalCount(final KantarReportResultFilter kantarReportResultFilter);
    Integer getTotalCount(final KantarReportResultFilter kantarReportResultFilter, final User owner);
    List<AttachmentBean> getKantarReportAttachments(final Long id);
    public boolean addKantarReportAttachments(final File attachment, final String fileName, final String contentType,
                                        final Long id, final Long userId) throws IOException, AttachmentException;

    public void saveAttachmentUser(final Long attachmentId, final Long userId);
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId);
    public void deleteAttachmentUser(final Long attachmentId);
    public void deleteAttachmentUser(final List<Long> attachmentIds);
    public Long getAttachmentUser(final Long attachmentId);
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;
    public List<Long> getAuthors();
}
