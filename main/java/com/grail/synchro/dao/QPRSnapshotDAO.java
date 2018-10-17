package com.grail.synchro.dao;

import java.util.List;

import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.QPRProjectCostSnapshot;
import com.grail.synchro.beans.QPRProjectSnapshot;
import com.grail.synchro.beans.QPRSnapshot;
import com.grail.synchro.beans.SpendByReportBean;
import com.grail.synchro.beans.SpendReportExtractFilter;
import java.util.Set;


/**
 * @author Tejinder
 * 
 */
public interface QPRSnapshotDAO {

	void saveSnapshot(final QPRSnapshot qprSnapshot);
	List<QPRSnapshot> getAllSnapshots();
	
	void saveProjectSnapshot(final QPRProjectSnapshot qprProjectSnapshot);
	void saveProjectCostSnapshot(final QPRProjectCostSnapshot qprProjectCostSnapshot);
	
	void updateProjectSnapshotFreeze(final QPRProjectSnapshot qprProjectSnapshot);
	List<Long> getAllOpenProjectIds(final Long snapShotId);
	
	List<Long> getAllOpenProjectIds(final Long snapShotId, final Long budgetLocation);
	
	void deleteSnapshot(final Long snapshotId);
	void deleteSnapshotProject(final Long snapshotId);
	void deleteSnapshotProjectCost(final Long snapshotId);
	Long getSnapShotId(final Integer freezeSpendFor, final Integer budgetYear);
	QPRSnapshot getSnapshot(final Long snapShotId);
	List<QPRProjectSnapshot> getProjectSnapshot(final Long snapShotId);
	List<QPRProjectSnapshot> getProjectSnapshot(final Long snapShotId, final Long projectId);
	List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId);
	List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId, final Long projectId);
	
	void updateProjectSnapshot(final QPRProjectSnapshot qprProjectSnapshot);
	void deleteProjectCostDetailsSnapshot(final Long snapshotId, final Long projectID);

	List<QPRProjectSnapshot> getSpendByBudgetLocationSnapshot(final Long snapShotId);
	List<QPRProjectSnapshot> getSpendByBudgetLocationSnapshot(final Long snapShotId, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter);
	
	List<QPRProjectSnapshot> getSpendByMethodolgies(final Long snapShotId,final String methodology, SpendReportExtractFilter spendReportFilter);
	
	List<QPRProjectSnapshot> getSpendByCategoryTypes(final Long snapShotId,final String categoryType, SpendReportExtractFilter spendReportFilter);
	
	List<QPRProjectSnapshot> getSpendByBrandSnapshot(final Long snapShotId,final Integer brand, SpendReportExtractFilter spendReportFilter);
	List<QPRProjectSnapshot> getSpendByNonBrandSnapshot(final Long snapShotId,final Integer nonBrand, SpendReportExtractFilter spendReportFilter);
	
	List<QPRProjectCostSnapshot> getSpendByAgencySnapshot(final Long snapShotId,final Long agencyId, SpendReportExtractFilter spendReportFilter);
	List<QPRProjectCostSnapshot> getSpendByAgencyTypeSnapshot(final Long snapShotId,final Integer agencyType, SpendReportExtractFilter spendReportFilter);
	
	List<Long> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter);
	List<Long> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter, List<Long> selectedQPRs);
	List<Integer> getBudgetLocationsForQPRSnapshot(final Integer budgetYear);
	List<String> getMethodologiesForQPRSnapshot(final Integer budgetYear);
	
	List<String> getCategoryTypesForQPRSnapshot(final Integer budgetYear);
	
	List<Integer> getBrandsForQPRSnapshot(final Integer budgetYear);
	List<Integer> getNonBrandsForQPRSnapshot(final Integer budgetYear);
	List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear);
	List<QPRProjectSnapshot> getSpendByCrossTabBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, SpendReportExtractFilter spendReportFilter);
	List<QPRProjectSnapshot> getSpendByCrossTabNonBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonbrand, SpendReportExtractFilter spendReportFilter);
	
	List<QPRProjectSnapshot> getSpendByCrossTabBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, String categoryType, SpendReportExtractFilter spendReportFilter);
	List<QPRProjectSnapshot> getSpendByCrossTabNonBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonbrand, String categoryType, SpendReportExtractFilter spendReportFilter);
	
	
	List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, final Long agencyId, SpendReportExtractFilter spendReportFilter);
	List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyNonBrand(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonBrand, final Long agencyId, SpendReportExtractFilter spendReportFilter);
	
	List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyBrandCT(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer brand, final String categoryType, final Long agencyId, SpendReportExtractFilter spendReportFilter);
	List<QPRProjectCostSnapshot> getSpendByCrossTabAgencyNonBrandCT(final Long snapShotId,final Integer budgetLocation, final String methodology, final Integer nonBrand, final String categoryType, final Long agencyId, SpendReportExtractFilter spendReportFilter);
	
	 List<QPRProjectSnapshot> getSpendByCrossTabBLMeth(final Long snapShotId,final Integer budgetLocation, final String methodology, SpendReportExtractFilter spendReportFilter);
	 
	 List<QPRProjectSnapshot> getSpendByCrossTabBLCT(final Long snapShotId,final Integer budgetLocation, final String categoryType, SpendReportExtractFilter spendReportFilter);
	 
	 List<QPRProjectSnapshot> getSpendByCrossTabBLMethCT(final Long snapShotId,final Integer budgetLocation, final String methodology, String categoryType, SpendReportExtractFilter spendReportFilter);
	 
	 List<QPRProjectSnapshot> getSpendByCrossTabMethCT(final Long snapShotId, final String categoryType, final String methodology,  SpendReportExtractFilter spendReportFilter);
	 
	 List<QPRProjectSnapshot> getSpendByCrossTabMethBrand(final Long snapShotId, final String methodology, final Integer brand, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByCrossTabMethNonBrand(final Long snapShotId, final String methodology, final Integer nonBrand, SpendReportExtractFilter spendReportFilter);
	 
	 List<QPRProjectSnapshot> getSpendByCrossTabMethBrandCT(final Long snapShotId, final String methodology, final Integer brand,  String categoryType, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByCrossTabMethNonBrandCT(final Long snapShotId, final String methodology, final Integer nonBrand,  String categoryType, SpendReportExtractFilter spendReportFilter);
	
	 List<QPRProjectCostSnapshot> getProjectCostSnapshot(final Long snapShotId, final Long projectId, final Long agencyId);
	 void updateOpenBudgetLocation(final Long snapShotId, final Long budgetLocation);
	 void updateFreezeBudgetLocation(final Long snapShotId);
	 void updateFreezeBudgetLocation(final Long snapShotId, final String budgetLocation);
	 
	 List<QPRProjectSnapshot> getSpendByCrossTabBrandCT(final Long snapShotId, final Integer brand, String categoryType, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByCrossTabNonBrandCT(final Long snapShotId, final Integer nonBrand,String categoryType, SpendReportExtractFilter spendReportFilter);
	 
	 List<Long> getBudgetYear(final Long snapShotId);
	 List<Project> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, List<Long> projectIds, List<Long> selectedQPRs);
	 List<QPRProjectSnapshot> getProjectSnapshot(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation);
	 
	 List<Integer> getBudgetLocationsForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter);
	 List<SpendByReportBean> getBudgetLocationsForQPRSnapshot(final Integer budgetYear, Set<Integer> budgetLocations, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByBudgetLocationSnapshot(final Long snapShotId,final Integer budgetLocation, String region, String area, String t20_40, SpendReportExtractFilter spendReportFilter);
	 
	 List<String> getMethodologiesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter);
	 List<SpendByReportBean> getMethodologiesForQPRSnapshot(final Integer budgetYear, List<String> methodologies, List<Long> selectedQPRs);
	 List<QPRProjectSnapshot> getSpendByMethodolgies(final Long snapShotId,final String methodology, String methodologyGroup, SpendReportExtractFilter spendReportFilter);
	 
	 List<Integer> getBrandsForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter);
	 List<SpendByReportBean> getBrandsForQPRSnapshot(final Integer budgetYear, Set<Integer> brands, List<Long> selectedQPRs);
	 List<QPRProjectSnapshot> getSpendByBrandSnapshot(final Long snapShotId,final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter);
	 
	 List<Integer> getNonBrandsForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter);
	 List<SpendByReportBean> getNonBrandsForQPRSnapshot(final Integer budgetYear, Set<Integer> nonBrands, List<Long> selectedQPRs);
	 List<QPRProjectSnapshot> getSpendByNonBrandSnapshot(final Long snapShotId,final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter);
	 
	 List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs);
	 List<String> getCategoryTypesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter);
	 
	 List<QPRProjectSnapshot> getSpendByCrossTabBLMeth(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByCrossTabBLMethBrand(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByCrossTabBLMethNonBrand(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByCrossTabBLMethBrandCT(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer brand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectSnapshot> getSpendByCrossTabBLMethNonBrandCT(final Long snapShotId,final Integer budgetLocation,  String region, String area, String t20_40, final String methodology, final Integer nonBrand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer brand, String brandType, String categoryType);
	 List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer brand, String brandType);
	 List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer nonBrand, String brandType);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, String categoryType);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, Integer brand, String brandType, String categoryType);
	 List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, Integer nonBrand, String brandType, String categoryType);
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40,  Integer brand, String brandType);
	 List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40,Integer nonBrand, String brandType);
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String categoryType);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String methodology, String methGroup, Integer brand, String brandType);
	 List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String methodology, String methGroup, Integer nonBrand, String brandType);
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String methodology, String methGroup, String categoryType);
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName,  Integer brand, String brandType, String categoryType);
	 List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer nonBrand, String brandType, String categoryType);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40);
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer brand, String brandType);
	 List<QPRProjectSnapshot> getProjectNonBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, Integer nonBrand, String brandType);
	 
	 List<QPRProjectSnapshot> getProjectBrandSnapshotCrossTab(final Long snapShotId, final Long projectId, String projectName, String categoryType);
	 
	 List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, Long projectId);
	 List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectCostSnapshot> getProjectBrandAgencySnapshotCrossTab(final Long snapShotId,final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer brand, String brandType, String categoryType,final Long agencyId, String agencyType);
	 List<QPRProjectCostSnapshot> getProjectNonBrandAgencySnapshotCrossTab(final Long snapShotId,final Long projectId, String projectName, Integer budgetLocation, String region, String area, String t20_40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType,final Long agencyId, String agencyType);
	 
	 List<QPRProjectCostSnapshot> getAgenciesAgencyTypeForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs, Long projectId);
	 
	 List<QPRProjectCostSnapshot> getProjectAgencySnapshot(final Long snapShotId, final Long projectId, String projectName, Integer budgetLocation);
	 List<QPRProjectCostSnapshot> getBudgetLocationAgencySnapshot(final Long snapShotId,final Integer budgetLocation, String region, String area, String t20_40, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectCostSnapshot> getMethodologyAgencySnapshot(final Long snapShotId,final String methodology, String methodologyGroup, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectCostSnapshot> getBrandAgencySnapshot(final Long snapShotId,final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectCostSnapshot> getNonBrandAgencySnapshot(final Long snapShotId,final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter);
	 List<QPRProjectCostSnapshot> getCategoryTypeAgencySnapshot(final Long snapShotId,final String categoryType, SpendReportExtractFilter spendReportFilter);
}
