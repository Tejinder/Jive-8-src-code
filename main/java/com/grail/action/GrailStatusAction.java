package com.grail.action;

import com.grail.beans.GrailBriefTemplate;
import com.grail.manager.GrailBriefTemplateManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/7/15
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailStatusAction extends JiveActionSupport {
    private Long id;
    private GrailBriefTemplate grailBriefTemplate;
    private GrailBriefTemplateManager grailBriefTemplateManager;
    private Integer status = 0;

    private boolean canEditProject = false;
    private boolean canCompleteProject = false;

    @Override
    public String input() {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }
        canEditProject = false;
        canCompleteProject = false;
        if(id != null && id > 0) {
            grailBriefTemplate = grailBriefTemplateManager.get(id);
            if(!SynchroPermHelper.canAccessGrailProject(grailBriefTemplate, getUser())) {
                return UNAUTHORIZED;
            }
            if(grailBriefTemplate != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
                    (grailBriefTemplate.getBatContact() != null && grailBriefTemplate.getBatContact().equals(getUser().getID()))
                            || (grailBriefTemplate.getCreatedBy() != null && grailBriefTemplate.getCreatedBy().equals(getUser().getID())))) {
                canEditProject = true;
                if(grailBriefTemplate.getFinalCost() != null && grailBriefTemplate.getFinalCostCurrency() != null
                        && grailBriefTemplate.getFinalCostCurrency().longValue() > 0) {
                    canCompleteProject = true;
                }
            }
            if(grailBriefTemplate.getStatus() != null) {
                status = grailBriefTemplate.getStatus();
            } else {
                status = 0;
            }

        } else {
            return ERROR;
        }
        return INPUT;
    }

    public String update() {
        if(request.getParameter("status") != null) {
            status = Integer.parseInt(request.getParameter("status"));
            if(status > 0) {

                grailBriefTemplate = grailBriefTemplateManager.get(id);
                if(status == 1) {
                    if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin())){
                        return SUCCESS;
                    }
                }
                grailBriefTemplate.setStatus(status);
                grailBriefTemplate.setModificationDate(new Date());
                grailBriefTemplate.setModifiedBy(getUser().getID());
                grailBriefTemplateManager.save(grailBriefTemplate);
            }

        }

        return SUCCESS;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean isCanEditProject() {
        return canEditProject;
    }

    public void setCanEditProject(boolean canEditProject) {
        this.canEditProject = canEditProject;
    }

    public boolean isCanCompleteProject() {
        return canCompleteProject;
    }

    public void setCanCompleteProject(boolean canCompleteProject) {
        this.canCompleteProject = canCompleteProject;
    }
}
