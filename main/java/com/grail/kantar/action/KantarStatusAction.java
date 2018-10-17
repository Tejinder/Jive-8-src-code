package com.grail.kantar.action;

import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.manager.KantarBriefTemplateManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 11/17/14
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarStatusAction extends JiveActionSupport {
    private Long id;
    private KantarBriefTemplate kantarBriefTemplate;
    private KantarBriefTemplateManager kantarBriefTemplateManager;
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
            kantarBriefTemplate = kantarBriefTemplateManager.get(id);
            if(!SynchroPermHelper.canAccessKantarProject(kantarBriefTemplate, getUser())) {
                return UNAUTHORIZED;
            }
            if(kantarBriefTemplate != null && (SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin() ||
                    (kantarBriefTemplate.getBatContact() != null && kantarBriefTemplate.getBatContact().equals(getUser().getID()))
                            || (kantarBriefTemplate.getCreatedBy() != null && kantarBriefTemplate.getCreatedBy().equals(getUser().getID())))) {
                canEditProject = true;
                if(kantarBriefTemplate.getFinalCost() != null && kantarBriefTemplate.getFinalCostCurrency() != null
                        && kantarBriefTemplate.getFinalCostCurrency().longValue() > 0) {
                    canCompleteProject = true;
                }
            }
            if(kantarBriefTemplate.getStatus() != null) {
                status = kantarBriefTemplate.getStatus();
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

                kantarBriefTemplate = kantarBriefTemplateManager.get(id);
                if(status == 1) {
                    if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin())){
                        return SUCCESS;
                    }
                }
                kantarBriefTemplate.setStatus(status);
                kantarBriefTemplate.setModificationDate(new Date());
                kantarBriefTemplate.setModifiedBy(getUser().getID());
                kantarBriefTemplateManager.save(kantarBriefTemplate);
            }

        }

        return SUCCESS;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
