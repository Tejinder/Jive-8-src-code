package com.grail.synchro.action;

import com.grail.synchro.beans.DefaultTemplateBean;
import com.grail.synchro.manager.DefaultTemplateManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;
import com.opensymphony.xwork2.Preparable;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 5:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTemplatesAction extends JiveActionSupport implements Preparable {

    private Map<Integer, List<DefaultTemplateBean>> defaultTemplatesMap = new HashMap<Integer, List<DefaultTemplateBean>>();
    private DefaultTemplateManager defaultTemplateManager;

    private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long templateType;
    private Long attachmentId;


    @Override
    public void prepare() throws Exception {
        defaultTemplatesMap = defaultTemplateManager.getAll();
    }

    @Override
    public String input() {
        if(!SynchroPermHelper.isSynchroUser(getUser())) {
            return UNAUTHORIZED;
        }
        return INPUT;
    }

    @Override
    public String execute() {
        DefaultTemplateBean bean = new DefaultTemplateBean();
        bean.setId(templateType);
        try {
            defaultTemplateManager.save(bean, attachFileFileName, attachFileContentType, attachFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public String delete() {
        try {
            defaultTemplateManager.deleteByAttachmentId(attachmentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public Map<Integer, List<DefaultTemplateBean>> getDefaultTemplatesMap() {
        return defaultTemplatesMap;
    }

    public void setDefaultTemplatesMap(Map<Integer, List<DefaultTemplateBean>> defaultTemplatesMap) {
        this.defaultTemplatesMap = defaultTemplatesMap;
    }

    public DefaultTemplateManager getDefaultTemplateManager() {
        return defaultTemplateManager;
    }

    public void setDefaultTemplateManager(DefaultTemplateManager defaultTemplateManager) {
        this.defaultTemplateManager = defaultTemplateManager;
    }

    public File getAttachFile() {
        return attachFile;
    }

    public void setAttachFile(File attachFile) {
        this.attachFile = attachFile;
    }

    public String getAttachFileContentType() {
        return attachFileContentType;
    }

    public void setAttachFileContentType(String attachFileContentType) {
        this.attachFileContentType = attachFileContentType;
    }

    public String getAttachFileFileName() {
        return attachFileFileName;
    }

    public void setAttachFileFileName(String attachFileFileName) {
        this.attachFileFileName = attachFileFileName;
    }

    public Long getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Long templateType) {
        this.templateType = templateType;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }
}
