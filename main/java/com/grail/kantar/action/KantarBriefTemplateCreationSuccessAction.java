package com.grail.kantar.action;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.manager.KantarBriefTemplateManager;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/29/14
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarBriefTemplateCreationSuccessAction extends JiveActionSupport {

    private Long id;


    private KantarBriefTemplate kantarBriefTemplate;
    private KantarBriefTemplateManager kantarBriefTemplateManager;

    @Override
    public String input() {
        if(id != null && id > 0) {
           kantarBriefTemplate = kantarBriefTemplateManager.get(id);
        }
        return INPUT;
    }

    public KantarBriefTemplate getKantarBriefTemplate() {
        return kantarBriefTemplate;
    }

    public void setKantarBriefTemplate(KantarBriefTemplate kantarBriefTemplate) {
        this.kantarBriefTemplate = kantarBriefTemplate;
    }

    public KantarBriefTemplateManager getKantarBriefTemplateManager() {
        return kantarBriefTemplateManager;
    }

    public void setKantarBriefTemplateManager(KantarBriefTemplateManager kantarBriefTemplateManager) {
        this.kantarBriefTemplateManager = kantarBriefTemplateManager;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
