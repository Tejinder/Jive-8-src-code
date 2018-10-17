package com.grail.synchro.manager.impl;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.dao.ProjectEvaluationDAO;
import com.grail.synchro.manager.ProjectEvaluationManager;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectEvaluationManagerImpl implements ProjectEvaluationManager {

    private ProjectEvaluationDAO projectEvaluationDAO;
  
    @Override
    public List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID, final Long endMarketId)
    {
    	return projectEvaluationDAO.getProjectEvaluationInitiation(projectID, endMarketId);
    }
    
    @Override
    public List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID)
    {
    	return projectEvaluationDAO.getProjectEvaluationInitiation(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectEvaluationInitiation saveProjectEvaluationDetails(final ProjectEvaluationInitiation projectEvaluationInitiation){
        this.projectEvaluationDAO.save(projectEvaluationInitiation);
        return projectEvaluationInitiation;
    }
   

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectEvaluationInitiation updateProjectEvaluationDetails(final ProjectEvaluationInitiation projectEvaluationInitiation) {
        this.projectEvaluationDAO.update(projectEvaluationInitiation);
        return  projectEvaluationInitiation;
    }
   
	public ProjectEvaluationDAO getProjectEvaluationDAO() {
		return projectEvaluationDAO;
	}

	public void setProjectEvaluationDAO(ProjectEvaluationDAO projectEvaluationDAO) {
		this.projectEvaluationDAO = projectEvaluationDAO;
	}




	
}
