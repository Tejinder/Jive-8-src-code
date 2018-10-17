package com.grail.synchro.manager.impl;

import java.util.List;

import com.grail.synchro.beans.EvaluationAgencyReportFiltersNew;
import com.grail.synchro.dao.SynchroReportDAONew;
import com.grail.synchro.manager.SynchroReportManagerNew;

/**
 * @author: Tejinder
 * @since: 1.0
 */
public class SynchroReportManagerImplNew implements SynchroReportManagerNew {

	private SynchroReportDAONew synchroReportDAONew;



	@Override
	public List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFiltersNew evaluationAgencyReportFilters)
	{
		return synchroReportDAONew.getAgencyEvaluationReport(evaluationAgencyReportFilters);
		
	}



	public SynchroReportDAONew getSynchroReportDAONew() {
		return synchroReportDAONew;
	}



	public void setSynchroReportDAONew(SynchroReportDAONew synchroReportDAONew) {
		this.synchroReportDAONew = synchroReportDAONew;
	}
	
	
}
