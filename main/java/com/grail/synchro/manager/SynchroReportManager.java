package com.grail.synchro.manager;

import java.math.BigDecimal;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.jivesoftware.base.User;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */

public interface SynchroReportManager {

    List<ResearchCycleReport> getResearchCycleReport(final ResearchCycleReportFilters researchCycleReportFilters);

    List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFilters evaluationAgencyReportFilters);

    List<ExchangeRateReport> getExchangeRateReport(final Integer year);

    // To fetch the project summary report.
    HSSFWorkbook getProjectSummaryReport(Long projectId);
    HSSFWorkbook getProjectFinancialReport(Long projectId);
    List<DataExtractReport> getProjectDataExtractReport(final DataExtractReportFilters projectDataExtractFilters);

    List<RawExtractReportBean> getRawExtractReport(final StandardReportFilter filter);

    Long getRawExtractReportTotalCount(final StandardReportFilter filter);


    List<SpendReportExtractBean> getSpendReport(final SpendReportExtractFilter filter, final User user);

    BigDecimal getMethodologyTotalSpend(final SpendReportExtractFilter filter, final User user, final Quarter quarter);


}
