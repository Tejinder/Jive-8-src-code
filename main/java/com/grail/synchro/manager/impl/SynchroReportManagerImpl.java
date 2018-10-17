package com.grail.synchro.manager.impl;

import java.math.BigDecimal;
import java.util.List;

import com.grail.synchro.beans.*;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.jivesoftware.base.User;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.grail.synchro.dao.SynchroReportDAO;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.SynchroReportUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class SynchroReportManagerImpl implements SynchroReportManager {

	private SynchroReportDAO synchroReportDAO;

	@Override
	public List<ResearchCycleReport> getResearchCycleReport(final ResearchCycleReportFilters researchCycleReportFilters)
	{
		return synchroReportDAO.getResearchCycleReport(researchCycleReportFilters);
		
	}
	
	@Override
	public List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFilters evaluationAgencyReportFilters)
	{
		return synchroReportDAO.getAgencyEvaluationReport(evaluationAgencyReportFilters);
		
	}
	
	@Override
	public List<ExchangeRateReport> getExchangeRateReport(final Integer year)
	{
		return synchroReportDAO.getExchangeRateReport(year);
		
	}
	
	/**
	 * This method will fetch the Project Summary Report
	 */
	@Override
	public HSSFWorkbook getProjectSummaryReport(Long projectId)
	{
		return SynchroReportUtil.getProjectSummaryReport(projectId);
	}
	/**
	 * This method will fetch the Project Financial Report
	 */
	@Override
	public HSSFWorkbook getProjectFinancialReport(Long projectId)
	{
		return SynchroReportUtil.getProjectFinancialReport(projectId);
	}
	@Override
	public List<DataExtractReport> getProjectDataExtractReport(DataExtractReportFilters projectDataExtractFilters)
	{
		return synchroReportDAO.getProjectDataExtractReport(projectDataExtractFilters);
	}

    @Override
    @Transactional
    public List<RawExtractReportBean> getRawExtractReport(final StandardReportFilter filter) {
        return synchroReportDAO.getRawExtractReport(filter);
    }

    @Override
    @Transactional
    public Long getRawExtractReportTotalCount(final StandardReportFilter filter) {
        return synchroReportDAO.getRawExtractReportTotalCount(filter);
    }

    @Override
    @Transactional
    public List<SpendReportExtractBean> getSpendReport(final SpendReportExtractFilter filter, final User user) {
        return synchroReportDAO.getSpendReport(filter, user);
    }

    @Override
    @Transactional
    public BigDecimal getMethodologyTotalSpend(final SpendReportExtractFilter filter, final User user, final Quarter quarter) {
       return synchroReportDAO.getMethodologyTotalSpend(filter,user,quarter);
    }

    public SynchroReportDAO getSynchroReportDAO() {
		return synchroReportDAO;
	}
	public void setSynchroReportDAO(SynchroReportDAO synchroReportDAO) {
		this.synchroReportDAO = synchroReportDAO;
	}

}
