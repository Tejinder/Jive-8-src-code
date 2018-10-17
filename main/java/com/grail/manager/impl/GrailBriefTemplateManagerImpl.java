package com.grail.manager.impl;

import com.grail.GrailGlobals;
import com.grail.beans.GrailBriefTemplate;
import com.grail.beans.GrailBriefTemplateFilter;
import com.grail.dao.GrailBriefTemplateDAO;
import com.grail.manager.GrailBriefTemplateManager;
import com.grail.object.GrailAttachment;
import com.grail.objecttype.GrailAttachmentObjectType;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/1/14
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class GrailBriefTemplateManagerImpl implements GrailBriefTemplateManager {

    private GrailBriefTemplateDAO grailBriefTemplateDAO;

    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;

    @Override
    @Transactional
    public Long save(final GrailBriefTemplate briefTemplate) {
         if(briefTemplate != null) {
             if(briefTemplate.getId() != null) {
                 return grailBriefTemplateDAO.update(briefTemplate);
             } else {
                 return grailBriefTemplateDAO.save(briefTemplate);
             }
         }
        return null;
    }

    @Override
    @Transactional
    public GrailBriefTemplate get(final Long id) {
        return grailBriefTemplateDAO.get(id);
    }

    @Override
    @Transactional
    public GrailBriefTemplate get(Long id, Long userId) {
        return grailBriefTemplateDAO.get(id, userId);
    }

    @Override
    @Transactional
    public List<GrailBriefTemplate> getAll(final Long userId) {
        return grailBriefTemplateDAO.getAll(userId);
    }

    @Override
    @Transactional
    public List<GrailBriefTemplate> getAll() {
        return grailBriefTemplateDAO.getAll();
    }

    @Override
    @Transactional
    public List<GrailBriefTemplate> getAll(final GrailBriefTemplateFilter grailBriefTemplateFilter) {
          return grailBriefTemplateDAO.getAll(grailBriefTemplateFilter);
    }

    @Override
    @Transactional
    public Integer getTotalCount(final GrailBriefTemplateFilter grailBriefTemplateFilter) {
        return grailBriefTemplateDAO.getTotalCount(grailBriefTemplateFilter);
    }

    @Override
    @Transactional
    public GrailBriefTemplate getDraftTemplate(final Long userId) {
        return grailBriefTemplateDAO.getDraftTemplate(userId);
    }

    @Override
    @Transactional
    public void deleteDraftTemplate(final Long userId) {
        grailBriefTemplateDAO.deleteDraftTemplate(userId);
    }



    @Override
    @Transactional
    public List<AttachmentBean> getGrailAttachments(final Long id) {
        return grailBriefTemplateDAO.getGrailAttachments(getGrailAttachmentObject(id));
    }

    @Override
    @Transactional
    public boolean addGrailAttachments(final File attachment,
                                       final String fileName,
                                       final String contentType, final Long id, final Long userId)
            throws IOException, AttachmentException {
        boolean success = false;
        try {
            Attachment att = attachmentHelper.createAttachment(
                    getGrailAttachmentObject(id), fileName , contentType, attachment);
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
        grailBriefTemplateDAO.saveAttachmentUser(attachmentId, userId);
    }

    @Override
    @Transactional
    public void saveAttachmentUser(final List<Long> attachmentIds, final Long userId) {
        grailBriefTemplateDAO.saveAttachmentUser(attachmentIds, userId);
    }

    @Override
    @Transactional
    public void deleteAttachmentUser(final Long attachmentId) {
        grailBriefTemplateDAO.deleteAttachmentUser(attachmentId);
    }

    @Override
    @Transactional
    public void deleteAttachmentUser(final List<Long> attachmentIds) {
        grailBriefTemplateDAO.deleteAttachmentUser(attachmentIds);
    }

    @Override
    @Transactional
    public Long getAttachmentUser(final Long attachmentId) {
        return grailBriefTemplateDAO.getAttachmentUser(attachmentId);
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
    public List<GrailBriefTemplate> getPendingActivities(final Long userId) {
        return grailBriefTemplateDAO.getPendingActivities(userId);
    }

    private GrailAttachment getGrailAttachmentObject(final Long projectId) {
        GrailAttachment grailAttachment = new GrailAttachment();
        grailAttachment.getBean().setObjectId((projectId).hashCode());
        Integer objectType = GrailGlobals.buildGrailAttachmentObjectID("GrailButtonDocuments", GrailAttachmentObjectType.GRAIL_ATTACHMENT_OBJECT_CODE.toString());
        grailAttachment.getBean().setObjectType(objectType);
        return grailAttachment;
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

    public GrailBriefTemplateDAO getGrailBriefTemplateDAO() {
        return grailBriefTemplateDAO;
    }

    public void setGrailBriefTemplateDAO(GrailBriefTemplateDAO grailBriefTemplateDAO) {
        this.grailBriefTemplateDAO = grailBriefTemplateDAO;
    }
}
