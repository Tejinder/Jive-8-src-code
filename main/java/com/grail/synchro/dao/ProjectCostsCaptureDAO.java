package com.grail.synchro.dao;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostsBean;
import com.grail.synchro.beans.Quarter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProjectCostsCaptureDAO {

    List<Project> getProjects(final Quarter quarter);
    void saveProjectCosts(final ProjectCostsBean bean);
    void updateProjectCosts(final ProjectCostsBean bean);
    List<ProjectCostsBean> get(final Long projectId, final Quarter quarter);
    List<ProjectCostsBean> get(final Long projectId,final Long endmarketId, final Integer investmentType, final Quarter quarter);
    void delete(final Long projectId,final Long endmarketId, final Integer investmentType, final Quarter quarter);
    void delete(final Long projectId,final Long endmarketId);
}
