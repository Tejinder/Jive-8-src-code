package com.grail.synchro.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.EvaluationAgencyReportFiltersNew;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.dao.SynchroReportDAONew;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.community.user.profile.ProfileManager;

/**
 * @author: Tejinder
 * @since: 1.0
 */

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class SynchroReportDAOImplNew extends SynchroAbstractDAO implements SynchroReportDAONew {

    private static final Logger LOG = Logger.getLogger(SynchroReportDAOImpl.class);
    private SynchroDAOUtil synchroDAOUtil;

    private static UserManager userManager;
    private static ProfileManager profileManager;

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }


   
    @Override
    public List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFiltersNew evaluationAgencyReportFilters)
    {
        
        List<Long> projectIds = Collections.emptyList();
        StringBuilder sql = new StringBuilder();
        
        
        // http://redmine.nvish.com/redmine/issues/530
       // sql.append("select p.projectid from grailproject p where p.status in (1,2,3,4,5,6) ");
       
        //sql.append("select p.projectid from grailproject p where p.status in (6) ");
        
        // This is done for migrated projects. The migrated projects for which agency rating has been given, they should come as part of Agency Evaluation Report
        sql.append("select p.projectid from grailproject p where (case when p.ismigrated = 1 THEN p.projectid in (select eval.projectid from grailprojecteval eval where eval.projectid = p.projectid and eval.agencyid > 0 and eval.agencyrating > 0 ) ELSE p.projectid in (select projectid from grailproject where ismigrated isNull and newsynchro = 1 and p.status in (6)) END )   ");
        
        
        
        if(SynchroPermHelper.isSystemAdmin())
        {
        	
        }
        else if(SynchroPermHelper.isGlobalUserType())
    	{
    		
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		List<Long> emIds = new ArrayList<Long>();
    		for(Long region: regionBudgetLocations)
    		{
    			List<MetaField> endMarketFields = SynchroUtils.getEndMarketsByRegion(region);
    			for(MetaField mf: endMarketFields)
    			{
    				emIds.add(mf.getId());
    			}
    		}
    		if(emIds.size()>0)
    		{
    			sql.append("  AND p.projecttype IN (2,3) AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
        		sql.append(StringUtils.join(emIds, ","));
        		sql.append("))");
    		}
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		
    		sql.append(" AND p.projecttype IN (3) AND p.budgetlocation in (");
    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    		sql.append(StringUtils.join(emBudgetLocations, ","));
    		sql.append(")");
    	}
        
        // Only Admin or System Admin should fetch the Cancel projects
        if(SynchroPermHelper.isSystemAdmin() || SynchroPermHelper.isSynchroSystemOwner())
        {
        	sql.append(" and p.iscancel IN (0,1) ");
        }
        else
        {
        	sql.append(" and p.iscancel IN (0) ");
        }
         
        sql.append(applyAgencyEvaluationFilter(evaluationAgencyReportFilters));
        
        sql.append(" order by  p.projectid asc");
            try {

                projectIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql.toString(),
                        Long.class);
            }
            catch (DataAccessException e) {
                final String message = "Failed to load Project Ids in Agency Evaluation Report New";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
       
        return projectIds;

    }
    private String applyAgencyEvaluationFilter(final EvaluationAgencyReportFiltersNew evaluationAgencyReportFilters) 
    {
    	
    	 StringBuilder sql = new StringBuilder();
    	// Start Date Filter
        
        if(evaluationAgencyReportFilters.getAeStartDateBegin() != null && evaluationAgencyReportFilters.getAeStartDateComplete()!=null) {
            StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeStartDateBegin().getTime());
            String startDateComplete = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeStartDateComplete().getTime());
            startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
            
            sql.append(startDateCondition.toString());
            
        }
        else if(evaluationAgencyReportFilters.getAeStartDateBegin() != null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeStartDateBegin().getTime());
            startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'");
            
            sql.append(startDateCondition.toString());
        }
        else if(evaluationAgencyReportFilters.getAeStartDateComplete()!=null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateComplete = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeStartDateComplete().getTime());
            startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
            
            sql.append(startDateCondition.toString());
        }
       
        // End Date Filter
        if(evaluationAgencyReportFilters.getAeEndDateBegin() != null && evaluationAgencyReportFilters.getAeEndDateComplete()!=null) {
            StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeEndDateBegin().getTime());
            String endDateComplete = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeEndDateComplete().getTime());
            endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
            sql.append(endDateCondition.toString());
            
        }
        else if (evaluationAgencyReportFilters.getAeEndDateBegin() != null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeEndDateBegin().getTime());
            endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'");
            sql.append(endDateCondition.toString());
        }
        else if (evaluationAgencyReportFilters.getAeEndDateComplete()!=null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateComplete = SynchroUtils.getDateString(evaluationAgencyReportFilters.getAeEndDateComplete().getTime());
            endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
            sql.append(endDateCondition.toString());
        }
        
        // Evaluation Done By Filter
        
        if(evaluationAgencyReportFilters.getEvaluationDoneBy() != null && !evaluationAgencyReportFilters.getEvaluationDoneBy().equals("")) 
        {
        	StringBuilder evalDoneByCondition = new StringBuilder();
        	evalDoneByCondition.append(" AND p.projectid in (select eval.projectid from grailprojecteval eval where eval.modificationby in (select u.userid from jiveuser u where (lower(u.firstname) || ' ' || lower(u.lastname)) like '%"+evaluationAgencyReportFilters.getEvaluationDoneBy().toLowerCase()+"%' ");
        	evalDoneByCondition.append("))");
        	sql.append(evalDoneByCondition.toString());
        }
         
        //Research Agency Filter
        if(evaluationAgencyReportFilters.getAeResearchAgencies() != null && evaluationAgencyReportFilters.getAeResearchAgencies().size()>0 && !isListNull(evaluationAgencyReportFilters.getAeResearchAgencies())) {
            StringBuilder researchAgenciesCondition = new StringBuilder();
            researchAgenciesCondition.append(" AND p.projectid in (select pcd.projectid from grailprojectcostdetails pcd where pcd.agencyid in (");
            researchAgenciesCondition.append(StringUtils.join(evaluationAgencyReportFilters.getAeResearchAgencies(),","));
            researchAgenciesCondition.append("))");
            sql.append(researchAgenciesCondition.toString());
            
        }
        
      //Cost Components Filter
        if(evaluationAgencyReportFilters.getAeCostComponents() != null && evaluationAgencyReportFilters.getAeCostComponents().size()>0 && !isListNull(evaluationAgencyReportFilters.getAeCostComponents())) {
            StringBuilder costComponentsCondition = new StringBuilder();
            costComponentsCondition.append(" AND p.projectid in (select pcd.projectid from grailprojectcostdetails pcd where pcd.costcomponent in (");
            costComponentsCondition.append(StringUtils.join(evaluationAgencyReportFilters.getAeCostComponents(),","));
            costComponentsCondition.append("))");
            sql.append(costComponentsCondition.toString());
            
        }
        
        //Research End Market Filter
        if(evaluationAgencyReportFilters.getAeResearchEndMarkets() != null && evaluationAgencyReportFilters.getAeResearchEndMarkets().size()>0 && !isListNull(evaluationAgencyReportFilters.getAeResearchEndMarkets())) {
            StringBuilder researchEndMarketsCondition = new StringBuilder();
            researchEndMarketsCondition.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
            researchEndMarketsCondition.append(StringUtils.join(evaluationAgencyReportFilters.getAeResearchEndMarkets(),","));
            researchEndMarketsCondition.append("))");
            sql.append(researchEndMarketsCondition.toString());
            
        }
        
        
      //Methodology Details Filter
        if(evaluationAgencyReportFilters.getAeMethDetails() != null && evaluationAgencyReportFilters.getAeMethDetails().size()>0 && !isListNull(evaluationAgencyReportFilters.getAeMethDetails())) {
            StringBuilder methodologyDetailsCondition = new StringBuilder();
           
            
            methodologyDetailsCondition.append(" AND (p.methodologydetails in ('")
                    .append(evaluationAgencyReportFilters.getAeMethDetails().get(0)+"").append("')");
            if(evaluationAgencyReportFilters.getAeMethDetails().size()>0)
            {
         	   methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(evaluationAgencyReportFilters.getAeMethDetails().get(0)+"").append(",%')");
         	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(evaluationAgencyReportFilters.getAeMethDetails().get(0)+"").append("')");
         	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,") .append(evaluationAgencyReportFilters.getAeMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<evaluationAgencyReportFilters.getAeMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or p.methodologydetails in ('").append(evaluationAgencyReportFilters.getAeMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(evaluationAgencyReportFilters.getAeMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(evaluationAgencyReportFilters.getAeMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(evaluationAgencyReportFilters.getAeMethDetails().get(i)+"").append(",%')");
	               	}
            }
            methodologyDetailsCondition.append(")");
            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
            sql.append(methodologyDetailsCondition.toString());
            
        }
        
      //Methodology Type Filter
        if(evaluationAgencyReportFilters.getAeMethodologyTypes() != null && evaluationAgencyReportFilters.getAeMethodologyTypes().size()>0 && !isListNull(evaluationAgencyReportFilters.getAeMethodologyTypes())) {
            StringBuilder methodologyTypesCondition = new StringBuilder();
            methodologyTypesCondition.append(" AND p.methodologytype in (");
            methodologyTypesCondition.append(StringUtils.join(evaluationAgencyReportFilters.getAeMethodologyTypes(),","));
            methodologyTypesCondition.append(")");
            sql.append(methodologyTypesCondition.toString());
            
        }
        
        //Budget Location Filter
        if(evaluationAgencyReportFilters.getAeBudgetLocations() != null && evaluationAgencyReportFilters.getAeBudgetLocations().size()>0 && !isListNull(evaluationAgencyReportFilters.getAeBudgetLocations())) {
            StringBuilder budgetLocationsCondition = new StringBuilder();
            budgetLocationsCondition.append(" AND p.budgetlocation in (");
            budgetLocationsCondition.append(StringUtils.join(evaluationAgencyReportFilters.getAeBudgetLocations(),","));
            budgetLocationsCondition.append(")");
            sql.append(budgetLocationsCondition.toString());
            
        }
        
        //BudgetYear Filter
        if(evaluationAgencyReportFilters.getAeBudgetYears() != null && evaluationAgencyReportFilters.getAeBudgetYears().size()>0 && !isListNull(evaluationAgencyReportFilters.getAeBudgetYears())) {
            StringBuilder budgetYearsCondition = new StringBuilder();
            budgetYearsCondition.append(" AND p.budgetyear in (");
            budgetYearsCondition.append(StringUtils.join(evaluationAgencyReportFilters.getAeBudgetYears(),","));
            budgetYearsCondition.append(")");
            sql.append(budgetYearsCondition.toString());
            
        }
        
        return sql.toString();
    }
    private boolean isListNull(List<Long> projectFilterList)
    {
    	if(projectFilterList!=null && projectFilterList.size()==1)
    	{
    		if(projectFilterList.get(0)==null)
    		{
    			return true;
    		}
    	}
    	return false;
    }
}
