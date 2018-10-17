package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.TPDSKUDetails;
import com.grail.synchro.beans.TPDSummary;
import com.grail.synchro.search.filter.ProjectResultFilter;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface TPDSummaryDAO {

	TPDSummary save(final TPDSummary tpdSummary);
	List<TPDSummary> getTPDSummaryDetails(final Long projectID);
	TPDSummary update(final TPDSummary tpdSummary);
	
	TPDSKUDetails saveSKU(final TPDSKUDetails tpdSKUDetails);
	void deleteTPDSKUDetails(final Long projectID);
	List<TPDSKUDetails> getTPDSKUDetails(final Long projectID);
	List<TPDSKUDetails> getTPDSKUDetailsRowId(final Long projectID, final Integer rowId);
	
	Long getTPDDashboardCount(final ProjectResultFilter filter);
	List<Project> getTPDProjects(final ProjectResultFilter projectResultFilter);
	TPDSummary updateTPDPrevSubmission(final TPDSummary tpdSummary);
	
	TPDSummary updateLegalApprovalDetails(final TPDSummary tpdSummary);
	void updateProposalLegalApprovalDetails(final ProposalInitiation proposalInitiation);
	void updatePIBLegalApprovalDetails(final ProjectInitiation projectInitiation);
 
}
