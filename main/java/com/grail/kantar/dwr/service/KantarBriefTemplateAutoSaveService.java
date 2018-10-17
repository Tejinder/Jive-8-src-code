package com.grail.kantar.dwr.service;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.manager.KantarBriefTemplateManager;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/16/14
 * Time: 1:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarBriefTemplateAutoSaveService extends RemoteSupport {

    private static KantarBriefTemplateManager kantarBriefTemplateManager;

    public KantarBriefTemplate getDraftTemplate(final Long userId) {
        KantarBriefTemplate template = getKantarBriefTemplateManager().getDraftTemplate(userId);
        return template;
    }

    public void saveDraftTemplate(final KantarBriefTemplate template) {
        getKantarBriefTemplateManager().save(template);
    }

    public void deleteDraftTemplate(final Long userId) {
        KantarBriefTemplate template = getKantarBriefTemplateManager().getDraftTemplate(userId);
        if(template != null) {
            getKantarBriefTemplateManager().deleteDraftTemplate(userId);
        }
    }

    public static KantarBriefTemplateManager getKantarBriefTemplateManager() {
        if(kantarBriefTemplateManager == null) {
            kantarBriefTemplateManager = JiveApplication.getContext().getSpringBean("kantarBriefTemplateManager");
        }
        return kantarBriefTemplateManager;
    }

}
