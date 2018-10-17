package com.grail.synchro.manager.impl;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.TPDSKUDetails;
import com.grail.synchro.beans.TPDSummary;
import com.grail.synchro.dao.ProjectEvaluationDAONew;
import com.grail.synchro.dao.TPDSummaryDAO;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;
import com.grail.synchro.manager.TPDSummaryManager;
import com.grail.synchro.search.filter.ProjectResultFilter;

/**
 * @author: tejinder
 * @since: 1.0
 */
public class TPDSummaryManagerImpl implements TPDSummaryManager {

    private TPDSummaryDAO tpdSummaryDAO;
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public TPDSummary save(final TPDSummary tpdSummary)
    {
    	return tpdSummaryDAO.save(tpdSummary);
    }
    
    @Override
    public List<TPDSummary> getTPDSummaryDetails(final Long projectID)
    {
    	return tpdSummaryDAO.getTPDSummaryDetails(projectID);
    }
	
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public TPDSummary update(final TPDSummary tpdSummary) 	
    {
    	return tpdSummaryDAO.update(tpdSummary);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public TPDSummary updateLegalApprovalDetails(final TPDSummary tpdSummary)
    {
    	return tpdSummaryDAO.updateLegalApprovalDetails(tpdSummary);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateProposalLegalApprovalDetails(final ProposalInitiation proposalInitiation)
    {
    	tpdSummaryDAO.updateProposalLegalApprovalDetails(proposalInitiation);
    }
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updatePIBLegalApprovalDetails(final ProjectInitiation projectInitiation)
    {
    	 tpdSummaryDAO.updatePIBLegalApprovalDetails(projectInitiation);
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public TPDSKUDetails saveSKU(final TPDSKUDetails tpdSKUDetails)
    {
    	return tpdSummaryDAO.saveSKU(tpdSKUDetails);
    }
	
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteTPDSKUDetails(final Long projectID)
    {
    	tpdSummaryDAO.deleteTPDSKUDetails(projectID);
    }

    @Override
    public List<TPDSKUDetails> getTPDSKUDetails(final Long projectID)
    {
    	return tpdSummaryDAO.getTPDSKUDetails(projectID);
    }
    
    @Override
    public List<TPDSKUDetails> getTPDSKUDetailsRowId(final Long projectID, final Integer rowId)
    {
    	return tpdSummaryDAO.getTPDSKUDetailsRowId(projectID, rowId);
    }
    
    @Override
    public Long getTPDDashboardCount(final ProjectResultFilter filter)
    {
    	return tpdSummaryDAO.getTPDDashboardCount(filter);
    }
    
    @Override
    public List<Project> getTPDProjects(final ProjectResultFilter projectResultFilter)
    {
    	return tpdSummaryDAO.getTPDProjects(projectResultFilter);
    }
	
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public TPDSummary updateTPDPrevSubmission(final TPDSummary tpdSummary) 	
    {
    	return tpdSummaryDAO.updateTPDPrevSubmission(tpdSummary);
    }
    
	
	public TPDSummaryDAO getTpdSummaryDAO() {
		return tpdSummaryDAO;
	}

	public void setTpdSummaryDAO(TPDSummaryDAO tpdSummaryDAO) {
		this.tpdSummaryDAO = tpdSummaryDAO;
	}
  
   



	
}
