package com.grail.kantar.manager.impl;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplateFilter;
import com.grail.kantar.dao.KantarBriefTemplateDAO;
import com.grail.kantar.manager.KantarBriefTemplateManager;
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
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/16/14
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarBriefTemplateManagerImpl implements KantarBriefTemplateManager {

    private KantarBriefTemplateDAO kantarBriefTemplateDAO;

    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;


    @Override
    @Transactional
    public Long save(final KantarBriefTemplate briefTemplate) {
        if(briefTemplate != null) {
            if(briefTemplate.getId() != null && briefTemplate.getId() > 0) {
                return kantarBriefTemplateDAO.update(briefTemplate);
            } else {
                return kantarBriefTemplateDAO.save(briefTemplate);
            }
        }
        return null;
    }


    @Override
    @Transactional
    public KantarBriefTemplate get(final Long id) {
        return kantarBriefTemplateDAO.get(id);
    }

    @Override
    @Transactional
    public KantarBriefTemplate get(final Long id, final Long userId) {
        return kantarBriefTemplateDAO.get(id, userId);
    }

    @Override
    @Transactional
    public List<KantarBriefTemplate> getAll(final Long userId) {
        return kantarBriefTemplateDAO.getAll(userId);
    }

    @Override
    @Transactional
    public List<KantarBriefTemplate> getAll() {
        return kantarBriefTemplateDAO.getAll();
    }

    @Override
    public List<KantarBriefTemplate> getAll(KantarBriefTemplateFilter kantarBriefTemplateFilter) {
        return kantarBriefTemplateDAO.getAll(kantarBriefTemplateFilter);
    }

    @Override
    public Integer getTotalCount(KantarBriefTemplateFilter kantarBriefTemplateFilter) {
        return kantarBriefTemplateDAO.getTotalCount(kantarBriefTemplateFilter);
    }

    @Override
    @Transactional
    public KantarBriefTemplate getDraftTemplate(final Long userId) {
        return kantarBriefTemplateDAO.getDraftTemplate(userId);
    }

    @Override
    @Transactional
    public void deleteDraftTemplate(final Long userId) {
    }


    @Override
    @Transactional
    public List<AttachmentBean> getKantarAttachments(final Long projectId) {
        return kantarBriefTemplateDAO.getKantarAttachments(getKantarAttachmentObject(projectId));
    }

    @Override
    @Transactional
    public boolean addKantarAttachments(final File attachment, final String fileName, final String contentType, final Long projectId, final Long userId) throws IOException, AttachmentException {
        boolean success = false;
        try {
            Attachment att = attachmentHelper.createAttachment(
                    getKantarAttachmentObject(projectId), fileName , contentType, attachment);
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
    @Transactional
    public void saveAttachmentUser(final Long attachmentId, final Long userId) {
        kantarBriefTemplateDAO.saveAttachmentUser(attachmentId, userId);
    }

    @Override
    @Transactional
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId) {
        kantarBriefTemplateDAO.saveAttachmentUser(attachmentIds, userId);
    }

    @Override
    @Transactional
    public void deleteAttachmentUser(final Long attachmentId) {
        kantarBriefTemplateDAO.deleteAttachmentUser(attachmentId);
    }

    @Override
    public void deleteAttachmentUser(List<Long> attachmentIds) {
        kantarBriefTemplateDAO.deleteAttachmentUser(attachmentIds);
    }

    @Override
    @Transactional
    public Long getAttachmentUser(final Long attachmentId) {
        return kantarBriefTemplateDAO.getAttachmentUser(attachmentId);
    }

    @Override
    @Transactional
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
    public List<KantarBriefTemplate> getPendingActivities(final Long userId) {
        return kantarBriefTemplateDAO.getPendingActivities(userId);
    }

    private User getUser() {
        return JiveApplication.getContext().getAuthenticationProvider().getJiveUser();
    }


    private KantarAttachment getKantarAttachmentObject(final Long projectId) {
        KantarAttachment kantarAttachment = new KantarAttachment();
        kantarAttachment.getBean().setObjectId((projectId).hashCode());
        Integer objectType = KantarGlobals.buildKantarAttachmentObjectID("KantarDocuments", KantarAttachmentObjectType.KANTAR_ATTACHMENT_OBJECT_CODE.toString());
        kantarAttachment.getBean().setObjectType(objectType);
        return kantarAttachment;
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

    public KantarBriefTemplateDAO getKantarBriefTemplateDAO() {
        return kantarBriefTemplateDAO;
    }

    public void setKantarBriefTemplateDAO(KantarBriefTemplateDAO kantarBriefTemplateDAO) {
        this.kantarBriefTemplateDAO = kantarBriefTemplateDAO;
    }
}
