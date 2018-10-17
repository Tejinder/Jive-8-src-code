package com.grail.kantar.manager.impl;

import com.grail.kantar.beans.KantarReportBean;
import com.grail.kantar.beans.KantarReportResultFilter;
import com.grail.kantar.dao.KantarReportDAO;
import com.grail.kantar.manager.KantarReportManager;
import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.objecttype.KantarAttachmentObjectType;
import com.grail.kantar.util.KantarGlobals;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class KantarReportManagerImpl implements KantarReportManager {

    private KantarReportDAO kantarReportDAO;

    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;

    @Override
    public Long save(final KantarReportBean kantarReportBean) {
        if(kantarReportBean != null) {
            if(kantarReportBean.getId() != null && kantarReportBean.getId() > 0) {
                return kantarReportDAO.update(kantarReportBean);
            } else {
                return kantarReportDAO.save(kantarReportBean);
            }
        }
        return null;
    }

    @Override
    public KantarReportBean get(final Long id) {
        return kantarReportDAO.get(id);
    }

    @Override
    public KantarReportBean get(final Long id, final Long userId) {
        return kantarReportDAO.get(id, userId);
    }

    @Override
    public List<KantarReportBean> getAll(final Long userId) {
        return kantarReportDAO.getAll(userId);
    }

    @Override
    public List<KantarReportBean> getAll() {
        return kantarReportDAO.getAll();
    }

    @Override
    public List<KantarReportBean> getAll(final KantarReportResultFilter kantarReportResultFilter) {
        return kantarReportDAO.getAll(kantarReportResultFilter);
    }

    @Override
    public List<KantarReportBean> getAll(KantarReportResultFilter kantarReportResultFilter, User owner) {
        return kantarReportDAO.getAll(kantarReportResultFilter, owner);
    }

    @Override
    public Integer getTotalCount(final KantarReportResultFilter kantarReportResultFilter) {
        return kantarReportDAO.getTotalCount(kantarReportResultFilter);
    }

    @Override
    public Integer getTotalCount(KantarReportResultFilter kantarReportResultFilter, User owner) {
        return kantarReportDAO.getTotalCount(kantarReportResultFilter, owner);
    }

    @Override
    public List<AttachmentBean> getKantarReportAttachments(final Long id) {
        return kantarReportDAO.getKantarReportAttachments(getKantarReportAttachmentObject(id));
    }

    @Override
    public boolean addKantarReportAttachments(final File attachment, final String fileName, final String contentType,
                                              final Long id, final Long userId) throws IOException, AttachmentException {
        boolean success = false;
        try {
            Attachment att = attachmentHelper.createAttachment(
                    getKantarReportAttachmentObject(id), fileName , contentType, attachment);
            saveAttachmentUser(att.getID(), userId);
            success = true;
        } catch (IOException e) {
            throw new IOException(e.getMessage(), e);
        } catch (AttachmentException e) {
            throw new AttachmentException(e.getMessage(), e);
        }
        return success;
    }

    @Override
    public void saveAttachmentUser(final Long attachmentId, final Long userId) {
        kantarReportDAO.saveAttachmentUser(attachmentId, userId);
    }

    @Override
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId) {
        kantarReportDAO.saveAttachmentUser(attachmentIds, userId);
    }

    @Override
    public void deleteAttachmentUser(final Long attachmentId) {
        kantarReportDAO.deleteAttachmentUser(attachmentId);
    }

    @Override
    public void deleteAttachmentUser(final List<Long> attachmentIds) {
        kantarReportDAO.deleteAttachmentUser(attachmentIds);
    }

    @Override
    public Long getAttachmentUser(final Long attachmentId) {
        return kantarReportDAO.getAttachmentUser(attachmentId);
    }

    private KantarAttachment getKantarReportAttachmentObject(final Long projectId) {
        KantarAttachment kantarAttachment = new KantarAttachment();
        kantarAttachment.getBean().setObjectId((projectId).hashCode());
        Integer objectType = KantarGlobals.buildKantarAttachmentObjectID("KantarReportDocuments", KantarAttachmentObjectType.KANTAR_ATTACHMENT_OBJECT_CODE.toString());
        kantarAttachment.getBean().setObjectType(objectType);
        return kantarAttachment;
    }

    @Override
    public boolean removeAttachment(final Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception {
        boolean success = false;
        try {
            Attachment attachment = attachmentManager.getAttachment(attachmentId);
            attachmentManager.deleteAttachment(attachment);
            deleteAttachmentUser(attachmentId);
            success=true;
        } catch (AttachmentNotFoundException e) {
            throw new AttachmentNotFoundException(e.getMessage());
        } catch (AttachmentException e) {
            throw new AttachmentException(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        return success;
    }

    @Override
    @Transactional
    public List<Long> getAuthors() {
        return kantarReportDAO.getAuthors();
    }

    private User getUser() {
        return JiveApplication.getContext().getAuthenticationProvider().getJiveUser();
    }

    public KantarReportDAO getKantarReportDAO() {
        return kantarReportDAO;
    }

    public void setKantarReportDAO(KantarReportDAO kantarReportDAO) {
        this.kantarReportDAO = kantarReportDAO;
    }

    public AttachmentHelper getAttachmentHelper() {
        return attachmentHelper;
    }

    public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
        this.attachmentHelper = attachmentHelper;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }
}
