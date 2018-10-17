package com.grail.dwr.service;

import com.grail.beans.GrailBriefTemplate;
import com.grail.manager.GrailBriefTemplateManager;
import com.jivesoftware.base.User;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/6/14
 * Time: 3:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailBriefTemplateAutoSaveService extends RemoteSupport {

    private static GrailBriefTemplateManager grailBriefTemplateManager;

    public GrailBriefTemplate getDraftTemplate(final Long userId) {
        GrailBriefTemplate template = getGrailBriefTemplateManager().getDraftTemplate(userId);
        return template;
    }

    public void saveDraftTemplate(final GrailBriefTemplate template) {
        getGrailBriefTemplateManager().save(template);
    }

    public void deleteDraftTemplate(final Long userId) {
        GrailBriefTemplate template = getGrailBriefTemplateManager().getDraftTemplate(userId);
        if(template != null) {
            getGrailBriefTemplateManager().deleteDraftTemplate(userId);
        }
    }


    public static GrailBriefTemplateManager getGrailBriefTemplateManager() {
        if(grailBriefTemplateManager == null) {
            grailBriefTemplateManager = JiveApplication.getContext().getSpringBean("grailBriefTemplateManager");
        }
        return grailBriefTemplateManager;
    }




}
