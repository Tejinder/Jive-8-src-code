package com.grail.action;

import com.grail.beans.GrailBriefTemplate;
import com.grail.manager.GrailBriefTemplateManager;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/7/15
 * Time: 2:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailTemplateCreationSuccessAction extends JiveActionSupport {
    private Long id;


    private GrailBriefTemplate grailBriefTemplate;
    private GrailBriefTemplateManager grailBriefTemplateManager;

    @Override
    public String input() {
        if(id != null && id > 0) {
            grailBriefTemplate = grailBriefTemplateManager.get(id);
        }
        return INPUT;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GrailBriefTemplate getGrailBriefTemplate() {
        return grailBriefTemplate;
    }

    public void setGrailBriefTemplate(GrailBriefTemplate grailBriefTemplate) {
        this.grailBriefTemplate = grailBriefTemplate;
    }

    public GrailBriefTemplateManager getGrailBriefTemplateManager() {
        return grailBriefTemplateManager;
    }

    public void setGrailBriefTemplateManager(GrailBriefTemplateManager grailBriefTemplateManager) {
        this.grailBriefTemplateManager = grailBriefTemplateManager;
    }
}
