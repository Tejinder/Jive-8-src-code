package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface ProjectEvaluationManagerNew {

	List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID, final Long endMarketId);

	ProjectEvaluationInitiation saveProjectEvaluationDetails(final ProjectEvaluationInitiation projectEvaluationInitiation);
	ProjectEvaluationInitiation updateProjectEvaluationDetails(final ProjectEvaluationInitiation projectEvaluationInitiation);
	
	ProjectEvaluationInitiation updateProjectMigration(final ProjectEvaluationInitiation projectEvaluationInitiation);
	
	List<ProjectEvaluationInitiation> getProjectEvaluationInitiation(final Long projectID);
	void deleteProjectEvaluationInitiation(final Long projectID);
	List<ProjectEvaluationInitiation> getProjectEvaluationInitiationAgency(final Long projectID, final Long agencyId);
}
