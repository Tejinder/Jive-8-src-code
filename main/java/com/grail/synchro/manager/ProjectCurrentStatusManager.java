package com.grail.synchro.manager;

import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.ProjectCurrentStatus;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectStagePendingFields;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;


/**
 * @author Tejinder
 * 
 */
public interface ProjectCurrentStatusManager {

	List<ProjectCurrentStatus> getPITStatusList(final long projectID, String stageUrl);
	List<ProjectCurrentStatus> getPIBStatusList(final long projectID, String stageUrl, List<Long> emIds, PIBMethodologyWaiver pibMethodologyWaiver, PIBMethodologyWaiver pibKantarMethodologyWaiver, List<ProjectInitiation> initiationList);
	List<ProjectStagePendingFields> getPIBPendingFields(List<ProjectInitiation> initiationList, Long projectID, List<Long> emIds);
	
	List<ProjectCurrentStatus> getProposalStatusList(List<ProposalInitiation> initiationList, final long projectID, String stageUrl);
	List<ProjectStagePendingFields> getProposalPendingFields(List<ProposalInitiation> initiationList, Long projectID, List<Long> emIds);
	
	List<ProjectCurrentStatus> getProjectSpecsStatusList(List<ProjectSpecsInitiation> initiationList, final long projectID, String stageUrl);
	List<ProjectStagePendingFields> getProjectSpecsPendingFields(List<ProjectSpecsInitiation> initiationList, Long projectID, List<Long> emIds);
	
	List<ProjectCurrentStatus> getReportSummaryStatusList(List<ReportSummaryInitiation> initiationList,final long projectID, String stageUrl);
	List<ProjectStagePendingFields> getReportSummaryPendingFields(List<ReportSummaryInitiation> initiationList, Long projectID, List<Long> emIds);
	List<ProjectCurrentStatus> getProjectEvaluationStatusList(final long projectID, String stageUrl);
	List<ProjectCurrentStatus> getPIBMultiStatusList(final long projectID, String stageUrl,List<Long> emIds, PIBMethodologyWaiver pibMW, PIBMethodologyWaiver pibKantarMW, List<FundingInvestment> fundingInvestmentList);
	Map<Long,List<ProjectStagePendingFields>> getPIBMultiPendingFields(Long projectID, List<Long> emIds);
	List<ProjectCurrentStatus> getProposalMultiStatusList(List<ProposalInitiation> initiationList, final long projectID, String stageUrl, List<FundingInvestment> fundingInvestmentList);
	List<ProjectStagePendingFields> getProposalMultiPendingFields(List<ProposalInitiation> initiationList, Long projectID, List<Long> emIds);
	
	List<ProjectCurrentStatus> getProjectSpecsMultiStatusList(final long projectID, String stageUrl, List<FundingInvestment> fundingInvestmentList);
	Map<Long,List<ProjectStagePendingFields>> getProjectSpecsMultiPendingFields( Long projectID, List<Long> emIds);
	
	List<ProjectCurrentStatus> getReportSummaryMultiStatusList(final long projectID, String stageUrl, List<FundingInvestment> fundingInvestmentList);
	Map<Long,List<ProjectStagePendingFields>> getReportSummaryMultiPendingFields( Long projectID, List<Long> emIds);
	
	List<ProjectCurrentStatus> getProjectEvaluationMultiStatusList(final long projectID, String stageUrl, List<Long> emIds, List<FundingInvestment> fundingInvestmentList);
}

