package com.grail.synchro.manager;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostsBean;
import com.grail.synchro.beans.Quarter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProjectCostsCaptureManager {
    List<Project> getProjects(final Quarter quarter);
    void save(final ProjectCostsBean bean, final Quarter quarter);
    List<ProjectCostsBean> get(final Long projectId, final Quarter quarter);
    List<ProjectCostsBean> get(final Long projectId,final Long endmarketId, final Integer investmentType , final Quarter quarter);
    void delete(final Long projectId,final Long endmarketId, final Integer investmentType, final Quarter quarter);
    void delete(final Long projectId,final Long endmarketId);
}
