package com.grail.synchro.dao;

import java.math.BigDecimal;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.jivesoftware.base.User;


/**
 * @author Tejinder
 * @version 1.0, Date 5/30/13
 */

public interface SynchroReportDAONew {
	
	public List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFiltersNew evaluationAgencyReportFilters);
	
	
}
