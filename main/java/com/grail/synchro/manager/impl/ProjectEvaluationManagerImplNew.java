package com.grail.synchro.manager.impl;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.dao.ProjectEvaluationDAONew;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class ProjectEvaluationManagerImplNew implements ProjectEvaluationManagerNew {

    private ProjectEvaluationDAONew projectEvaluationDAONew;
  
    @Override
    public List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID, final Long endMarketId)
    {
    	return projectEvaluationDAONew.getProjectEvaluationInitiation(projectID, endMarketId);
    }
    
    @Override
    public List<ProjectEvaluationInitiation> getProjectEvaluationInitiationAgency(final Long projectID, final Long agencyId)
    {
    	return projectEvaluationDAONew.getProjectEvaluationInitiationAgency(projectID, agencyId);
    }
    
    
    @Override
    public List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID)
    {
    	return projectEvaluationDAONew.getProjectEvaluationInitiation(projectID);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectEvaluationInitiation saveProjectEvaluationDetails(final ProjectEvaluationInitiation projectEvaluationInitiation){
        this.projectEvaluationDAONew.save(projectEvaluationInitiation);
        return projectEvaluationInitiation;
    }
   

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectEvaluationInitiation updateProjectEvaluationDetails(final ProjectEvaluationInitiation projectEvaluationInitiation) {
        this.projectEvaluationDAONew.update(projectEvaluationInitiation);
        return  projectEvaluationInitiation;
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ProjectEvaluationInitiation updateProjectMigration(final ProjectEvaluationInitiation projectEvaluationInitiation) {
        this.projectEvaluationDAONew.updateProjectMigration(projectEvaluationInitiation);
        return  projectEvaluationInitiation;
    }

    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteProjectEvaluationInitiation(final Long projectID)
    {
    	this.projectEvaluationDAONew.deleteProjectEvaluationInitiation(projectID);
    }
	public ProjectEvaluationDAONew getProjectEvaluationDAONew() {
		return projectEvaluationDAONew;
	}

	public void setProjectEvaluationDAONew(
			ProjectEvaluationDAONew projectEvaluationDAONew) {
		this.projectEvaluationDAONew = projectEvaluationDAONew;
	}
   



	
}
