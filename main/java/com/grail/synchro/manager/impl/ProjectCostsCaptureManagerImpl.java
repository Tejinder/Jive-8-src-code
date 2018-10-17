package com.grail.synchro.manager.impl;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostsBean;
import com.grail.synchro.beans.Quarter;
import com.grail.synchro.dao.ProjectCostsCaptureDAO;
import com.grail.synchro.manager.ProjectCostsCaptureManager;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCostsCaptureManagerImpl implements ProjectCostsCaptureManager {

    private static Logger LOG = Logger.getLogger(ProjectCostsCaptureManagerImpl.class);

    private ProjectCostsCaptureDAO projectCostsCaptureDAO;

    @Override
    @Transactional
    public List<Project> getProjects(final Quarter quarter) {
        return projectCostsCaptureDAO.getProjects(quarter);
    }

    @Override
    @Transactional
    public void save(final ProjectCostsBean bean, final Quarter quarter) {
        List<ProjectCostsBean> existProjects = get(bean.getProjectId(),bean.getEndmarketId(),bean.getInvestmentType().intValue(),  quarter);
        bean.setUpdatedDate(new Date());
        if(existProjects != null && existProjects.size() > 0) {
            ProjectCostsBean existProject = existProjects.get(0);
            bean.setId(existProject.getId());
            bean.setCapturedDate(existProject.getCapturedDate());
            LOG.trace("================== Updating project costs for projectId :" + bean.getProjectId() + "===============================");
            projectCostsCaptureDAO.updateProjectCosts(bean);
        } else {
            LOG.trace("================== Saving project costs for projectId :" + bean.getProjectId() + "===============================");
            bean.setCapturedDate(new Date());
            projectCostsCaptureDAO.saveProjectCosts(bean);
        }
    }



    @Override
    @Transactional
    public List<ProjectCostsBean> get(final Long projectId, final Quarter quarter) {
        return projectCostsCaptureDAO.get(projectId, quarter);
    }

    @Override
    @Transactional
    public List<ProjectCostsBean> get(final Long projectId, final Long endmarketId, final Integer investmentType, final Quarter quarter) {
        return projectCostsCaptureDAO.get(projectId, endmarketId,investmentType, quarter);
    }

    @Override
    @Transactional
    public void delete(final Long projectId, final Long endmarketId, final Integer investmentType, final Quarter quarter) {
        projectCostsCaptureDAO.delete(projectId, endmarketId,investmentType, quarter);
    }

    @Override
    @Transactional
    public void delete(final Long projectId, final Long endmarketId) {
        projectCostsCaptureDAO.delete(projectId, endmarketId);
    }

    public ProjectCostsCaptureDAO getProjectCostsCaptureDAO() {
        return projectCostsCaptureDAO;
    }

    public void setProjectCostsCaptureDAO(ProjectCostsCaptureDAO projectCostsCaptureDAO) {
        this.projectCostsCaptureDAO = projectCostsCaptureDAO;
    }
}
