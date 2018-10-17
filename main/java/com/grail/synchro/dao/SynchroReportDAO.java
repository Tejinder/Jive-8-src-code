package com.grail.synchro.dao;

import java.math.BigDecimal;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.jivesoftware.base.User;


/**
 * @author Kanwar Grewal
 * @version 1.0, Date 5/30/13
 */

public interface SynchroReportDAO {
	
	public List<ResearchCycleReport> getResearchCycleReport(final ResearchCycleReportFilters researchCycleReportFilters);
	
	public List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFilters evaluationAgencyReportFilters);
	
	public List<ExchangeRateReport> getExchangeRateReport(final Integer year);
	
	List<DataExtractReport> getProjectDataExtractReport(final DataExtractReportFilters projectDataExtractFilters);

    List<RawExtractReportBean> getRawExtractReport(final StandardReportFilter filter);

    Long getRawExtractReportTotalCount(final StandardReportFilter filter);

    List<SpendReportExtractBean> getSpendReport(final SpendReportExtractFilter filter, final User user);

    BigDecimal getMethodologyTotalSpend(final SpendReportExtractFilter filter, final User user, final Quarter quarter);

}
