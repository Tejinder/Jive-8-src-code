package com.grail.synchro.manager.impl;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.DefaultTemplateBean;
import com.grail.synchro.dao.DefaultTemplateDAO;
import com.grail.synchro.manager.DefaultTemplateManager;
import com.grail.synchro.object.DefaultTemplate;
import com.grail.synchro.object.MyLibraryDocument;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.attachments.AttachmentHelper;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 6:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplateManagerImpl implements DefaultTemplateManager {
    private static Logger LOG = Logger.getLogger(DefaultTemplateManagerImpl.class);

    private DefaultTemplateDAO defaultTemplateDAO;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DefaultTemplate get(final Long id) {
        return defaultTemplateDAO.get(id);
    }

    @Override
    public DefaultTemplate getByAttachmentId(final Long attachmentId) {
        return defaultTemplateDAO.getByAttachmentId(attachmentId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<Integer, List<DefaultTemplateBean>> getAll() throws UnsupportedEncodingException {
        Map<Integer, List<DefaultTemplateBean>> templatesMap = new HashMap<Integer, List<DefaultTemplateBean>>();
        for(SynchroGlobal.DefaultTemplateType type : SynchroGlobal.DefaultTemplateType.values()) {
            List<DefaultTemplate> defaultTemplates = defaultTemplateDAO.getAllById(type.getId().longValue());
            if(defaultTemplates != null && defaultTemplates.size() > 0) {
                List<DefaultTemplateBean> templateBeans = new ArrayList<DefaultTemplateBean>();
                for(DefaultTemplate template : defaultTemplates) {
                    StringBuilder link = new StringBuilder();
                    link.append("/servlet/JiveServlet/download/");
                    link.append(template.getBean().getId()).append("-").append(template.getBean().getAttachmentId()).append("/").append(URLEncoder.encode(template.getBean().getFileName(), "UTF-8"));
                    template.getBean().setFileDownloadLink(link.toString());
                    templateBeans.add(template.getBean());
                }
                templatesMap.put(type.getId(), templateBeans);
            }

        }
//        List<DefaultTemplate> defaultTemplates = defaultTemplateDAO.getAll();
//        if(defaultTemplates != null && defaultTemplates.size() > 0) {
//            for(DefaultTemplate template:defaultTemplates) {
//
//                StringBuilder link = new StringBuilder();
//                link.append("/servlet/JiveServlet/download/");
//                link.append(template.getBean().getId()).append("-").append(template.getBean().getAttachmentId()).append("/").append(template.getBean().getFileName());;
//                template.getBean().setFileDownloadLink(link.toString());
//
//            }
//            templatesMap.put(template.getBean().getId().intValue(), template.getBean());
//        }
        return templatesMap;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean save(final DefaultTemplateBean defaultTemplateBean, final String fileName,
                        final String contentType, final File attachment) throws IOException, AttachmentException {

        DefaultTemplate defaultTemplate = new DefaultTemplate();
        defaultTemplate.setBean(defaultTemplateBean);
        try {
            attachmentHelper.createAttachment(defaultTemplate, fileName , contentType, attachment);
        } catch (IOException e) {
            throw new IOException(e.getMessage(), e);
        } catch (AttachmentException e) {
            throw new AttachmentException(e.getMessage(), e);
        }

        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean deleteByAttachmentId(final Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception {
        try {
            Attachment attachment = attachmentManager.getAttachment(attachmentId);
            attachmentManager.deleteAttachment(attachment);
        } catch (AttachmentNotFoundException e) {
            throw new AttachmentNotFoundException(e.getMessage());
        } catch (AttachmentException e) {
            throw new AttachmentException(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean delete(final Long id) throws AttachmentNotFoundException, AttachmentException, Exception {
        DefaultTemplate defaultTemplate = defaultTemplateDAO.get(id);
        try {
            Attachment attachment = attachmentManager.getAttachment(defaultTemplate.getID());
            attachmentManager.deleteAttachment(attachment);
        } catch (AttachmentNotFoundException e) {
            throw new AttachmentNotFoundException(e.getMessage());
        } catch (AttachmentException e) {
            throw new AttachmentException(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        return true;
    }

    public DefaultTemplateDAO getDefaultTemplateDAO() {
        return defaultTemplateDAO;
    }

    public void setDefaultTemplateDAO(DefaultTemplateDAO defaultTemplateDAO) {
        this.defaultTemplateDAO = defaultTemplateDAO;
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
