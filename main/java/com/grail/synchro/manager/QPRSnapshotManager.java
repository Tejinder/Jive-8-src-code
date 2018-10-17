package com.grail.synchro.manager;

import java.util.List;
import java.util.Map;

import com.grail.synchro.beans.QPRProjectCostSnapshot;
import com.grail.synchro.beans.QPRProjectSnapshot;
import com.grail.synchro.beans.QPRSnapshot;
import com.grail.synchro.beans.SpendByReportBean;
import com.grail.synchro.beans.SpendReportExtractFilter;


/**
 * @author: Tejinder
 * @since: 1.0
 */
public interface QPRSnapshotManager {

	void saveSnapshot(final QPRSnapshot qprSnapshot);
	List<QPRSnapshot> getAllSnapshots();
	void updateProjectSnapshotFreeze(final QPRProjectSnapshot qprProjectSnapshot);
	void deleteSnapshot(final Long snapshotId);
	Long getSnapShotId(final Integer freezeSpendFor, final Integer budgetYear);
	
	QPRSnapshot getSnapshot(final Long snapShotId);
	List<QPRProjectSnapshot> getProjectSnapshot(final Long snapShotId);
	QPRProjectSnapshot getProjectSnapshot(final Long snapShotId, final Long projectId);
	
	List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId);
	List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId, final Long projectId);
	
	void updateProjectSnapshot(final QPRProjectSnapshot qprProjectSnapshot);
	List<SpendByReportBean> getSpendByProjects(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter,boolean fetchLatestCostProjects, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	Map<Integer, List<SpendByReportBean>> getSpendByBudgetLocationMap(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	
	List<SpendByReportBean> getSpendByBudgetLocation(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	
	List<SpendByReportBean> getSpendByMethodology(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	Map<String, List<SpendByReportBean>> getSpendByMethodologyMap(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
	   		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	List<SpendByReportBean> getSpendByBrand(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	
	Map<String, List<SpendByReportBean>> getSpendByBrandMap(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
    		
	List<SpendByReportBean> getSpendByAgency(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	Map<Integer, List<SpendByReportBean>> getSpendByAgencyMap(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	
	List<SpendByReportBean> getSpendByAgencyType(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	Map<String, List<SpendByReportBean>> getSpendByAgencyTypeMap(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean  fetchLatestCostProjects,
    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
    		
	List<SpendByReportBean> getSpendByCrossTab(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects, boolean isSpendByBudgetLocation, boolean isSpendByProject, boolean isSpendByBrand, boolean isSpendByMethodology, boolean isSpendByAgency, boolean isSpendByKantarNonKantar, boolean isSpendByCategoryType, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	
	List<SpendByReportBean> getSpendByCategoryType(Integer budgetYear, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	
	Map<String, List<SpendByReportBean>> getSpendByCategoryTypeMap(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	
	List<Long> getAllOpenProjectIds(final Long snapShotId, final Long budgetLocation);
	void updateOpenBudgetLocation(final Long snapShotId, final Long budgetLocation);
	void updateFreezeBudgetLocation(final Long snapShotId);
	void updateFreezeBudgetLocation(final Long snapShotId, final String budgetLocation);
	
	Long getBudgetYear(final Long snapShotId);
	 void saveProjectSnapshot(final QPRProjectSnapshot qprProjectSnapshot);
	 Map<String,List<SpendByReportBean>> getSpendByProjects(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
	    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	 
	 Map<String,List<SpendByReportBean>>  getSpendByBudgetLocation(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
	    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	 
	 Map<String, List<SpendByReportBean>> getSpendByMethodology(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
		   		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	 
	 Map<String, List<SpendByReportBean>> getSpendByBrand(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
	    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	 
	 
	 Map<String, List<SpendByReportBean>> getSpendByAgency(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
	    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	 
	 Map<String, List<SpendByReportBean>> getSpendByCategoryType(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter, boolean fetchLatestCostProjects,
	    		boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
	 
	 Map<String,List<SpendByReportBean>> getSpendByCrossTab(String[] budgetYearsArr, Integer currencyId, SpendReportExtractFilter spendReportFilter,
	    		boolean fetchLatestCostProjects, boolean isSpendByProject, boolean isSpendByBudgetLocation, boolean isSpendByBrand, 
	    		boolean isSpendByMethodology, boolean isSpendByAgency, boolean isSpendByKantarNonKantar, boolean isSpendByCategoryType, boolean isCOPLASpendFor, boolean isQPR1SpendFor, boolean isQPR2SpendFor, boolean isQPR3SpendFor, boolean isFullYearSpendFor);
}
