package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.ProjectEvaluationInitiation;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ProjectEvaluationDAO {

	ProjectEvaluationInitiation save(final ProjectEvaluationInitiation projectEvaluationInitiation);

	ProjectEvaluationInitiation update(final ProjectEvaluationInitiation projectEvaluationInitiation);

	List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID, final Long endMarketId);
	List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID); 
    
}
