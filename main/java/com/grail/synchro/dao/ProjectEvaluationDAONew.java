package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.ProjectEvaluationInitiation;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ProjectEvaluationDAONew {

	ProjectEvaluationInitiation save(final ProjectEvaluationInitiation projectEvaluationInitiation);

	ProjectEvaluationInitiation update(final ProjectEvaluationInitiation projectEvaluationInitiation);
	
	ProjectEvaluationInitiation updateProjectMigration(final ProjectEvaluationInitiation projectEvaluationInitiation);

	List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID, final Long endMarketId);
	List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID); 
	void deleteProjectEvaluationInitiation(final Long projectID);
	List<ProjectEvaluationInitiation> getProjectEvaluationInitiationAgency(final Long projectID, final Long agencyId);
    
}
