package com.grail.synchro.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.beans.TPDSKUDetails;
import com.grail.synchro.beans.TPDSummary;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.impl.dao.AttachmentBean;

/**
 * @author: tejinder
 * @since: 1.0
 */
public interface TPDSummaryManager {

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
