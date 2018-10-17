package com.grail.synchro.dao.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.grail.synchro.beans.*;
import com.grail.synchro.dwr.service.UserDepartmentsService;
import com.grail.synchro.search.filter.StandardReportFilter;
import com.grail.synchro.util.QuarterRangeUtil;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.dao.SynchroReportDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.jivesoftware.base.database.dao.DAOException;

/**
 * @author: Kanwar Grewal
 * @since: 1.0
 */

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class SynchroReportDAOImpl extends SynchroAbstractDAO implements SynchroReportDAO {

    private static final Logger LOG = Logger.getLogger(SynchroReportDAOImpl.class);
    private SynchroDAOUtil synchroDAOUtil;

    private static UserManager userManager;
    private static ProfileManager profileManager;

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }


    @Override
    public List<ResearchCycleReport> getResearchCycleReport(final ResearchCycleReportFilters researchCycleReportFilters)
    {
        List<Long> projectIDs = Collections.emptyList();
        Boolean flag = true;
        Boolean fetchRows = false;
        Boolean fetchProjects1 = true;
        if(researchCycleReportFilters!= null)
        {

            projectIDs= getProjectIds(researchCycleReportFilters.getSupplier(), researchCycleReportFilters.getSupplierGroup(), researchCycleReportFilters.getEndMarkets(), researchCycleReportFilters.getRegionFields());
        }


        String LOAD_PROJECTS = getProjectQuery(projectIDs,researchCycleReportFilters.getWorkflowType(),researchCycleReportFilters.getMethodologyFields(),researchCycleReportFilters.getBrandFields(),researchCycleReportFilters.getStartMonth(),researchCycleReportFilters.getStartYear(),researchCycleReportFilters.getEndMonth(),researchCycleReportFilters.getEndYear(),null);


        List<ResearchCycleReport> projects = Collections.emptyList();
        try{
            if(projectIDs!=null)
            {
                /*projects = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_PROJECTS,
                            projectRowMapper,SynchroGlobal.Status.DRAFT.ordinal(), SynchroGlobal.Status.DELETED.ordinal(),SynchroGlobal.Status.CONCEPT_CANCEL.ordinal(),SynchroGlobal.Status.PLANNED_CANCEL.ordinal(),SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal());
                    */}
        }
        catch (DataAccessException e) {
            final String message = "Failed to get Research Cycle Report for projects ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }

    private final ParameterizedRowMapper<ResearchCycleReport> projectRowMapper = new ParameterizedRowMapper<ResearchCycleReport>() {
        public ResearchCycleReport mapRow(ResultSet rs, int row) throws SQLException {
            final ResearchCycleReport researchCycleReport = new ResearchCycleReport();
            researchCycleReport.setName(rs.getString("name"));
            researchCycleReport.setProjectID(rs.getLong("projectID"));
            researchCycleReport.setOwnerID(rs.getLong("ownerID"));
            researchCycleReport.setStartMonth(rs.getInt("startMonth"));
            researchCycleReport.setStartYear(rs.getInt("startYear"));
            researchCycleReport.setEndMonth(rs.getInt("endMonth"));
            researchCycleReport.setEndYear(rs.getInt("endYear"));
            researchCycleReport.setStatus(rs.getInt("status"));
            return researchCycleReport;
        }
    };
    private final ParameterizedRowMapper<DataExtractReport> dataExtractReportMapper = new ParameterizedRowMapper<DataExtractReport>() {
        public DataExtractReport mapRow(ResultSet rs, int row) throws SQLException {
            final DataExtractReport dataExtractReport = new DataExtractReport();
            dataExtractReport.setName(rs.getString("name"));
            dataExtractReport.setProjectID(rs.getLong("projectID"));
            dataExtractReport.setOwnerID(rs.getLong("ownerID"));
            dataExtractReport.setStartMonth(rs.getInt("startMonth"));
            dataExtractReport.setStartYear(rs.getInt("startYear"));
            dataExtractReport.setEndMonth(rs.getInt("endMonth"));
            dataExtractReport.setEndYear(rs.getInt("endYear"));
            dataExtractReport.setStatus(rs.getInt("status"));

            dataExtractReport.setProjectType(rs.getLong("projectType"));
            dataExtractReport.setBrand(rs.getLong("brand"));
            dataExtractReport.setMethodology(rs.getLong("methodology"));
            dataExtractReport.setMethodologyGroup(rs.getLong("methodologyGroup"));
            dataExtractReport.setResearchType(rs.getLong("researchType"));
            dataExtractReport.setInsights(rs.getBoolean("insights"));
            dataExtractReport.setFwEnabled(rs.getBoolean("isFWEnabled"));
            dataExtractReport.setNpi(rs.getLong("npi"));
            dataExtractReport.setPartialMethodologyWaiverRequired(rs.getBoolean("partialMethodologyWaiver"));

            return dataExtractReport;
        }
    };
    @Override
    public List<Long> getAgencyEvaluationReport(final EvaluationAgencyReportFilters evaluationAgencyReportFilters)
    {
        Long supplierGroup = evaluationAgencyReportFilters.getSupplierGroup();
        Long supplier = evaluationAgencyReportFilters.getSupplier();
        Boolean  flag = true;
        List<Long> projectIds = Collections.emptyList();

        String COORDINATION_LIST_SQL = "SELECT projectid FROM grailcoordinationdetails";
        if(supplier != null || supplierGroup != null)
        {
            if(supplier!=null && supplier > 0)
            {
                if(flag)
                {
                    COORDINATION_LIST_SQL = COORDINATION_LIST_SQL + " where supplier = " + supplier;
                    flag = false;
                }
                else
                {
                    COORDINATION_LIST_SQL = COORDINATION_LIST_SQL + " and supplier = " + supplier;
                }
            }
            if(supplierGroup!=null && supplierGroup > 0)
            {
                if(flag)
                {
                    COORDINATION_LIST_SQL = COORDINATION_LIST_SQL + " where suppliergroup = " + supplierGroup;
                    flag = false;
                }
                else
                {
                    COORDINATION_LIST_SQL = COORDINATION_LIST_SQL + " and suppliergroup = " + supplierGroup;
                }
            }


            try {

                projectIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(COORDINATION_LIST_SQL,
                        Long.class);
            }
            catch (DataAccessException e) {
                final String message = "Failed to load Coordination Agnecy in Agency Evaluation Report";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }
        return projectIds;

    }

    @Override
    public List<ExchangeRateReport> getExchangeRateReport(final Integer year)
    {
        List<ExchangeRateReport> exchangeRateReportList = new ArrayList<ExchangeRateReport>();
        if(year>0)
        {
            Long startDate = synchroDAOUtil.getStartDayOfYear(year);
            Long endDate = synchroDAOUtil.getEndDayOfYear(year);

            final String EXCHANGE_RATE = "SELECT g.currencyid, grailcurrencyexchangerate.exchangerate "+
                    "FROM (SELECT currencyid, MAX(modificationdate) AS val "+
                    "FROM grailcurrencyexchangerate where modificationdate>="+startDate+" AND modificationdate<="+endDate+
                    " GROUP BY currencyid) AS g "+
                    "JOIN grailcurrencyexchangerate ON g.currencyid = grailcurrencyexchangerate.currencyid AND g.val = grailcurrencyexchangerate.modificationdate;";

            try {
                exchangeRateReportList = getSimpleJdbcTemplate().getJdbcOperations().query(EXCHANGE_RATE, exchangeRateReportRowMapper);
            }catch (DataAccessException e) {
                final String message = "Failed to load exchange rate report for Year  "+year;
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }
        return exchangeRateReportList;
    }

    private final ParameterizedRowMapper<ExchangeRateReport> exchangeRateReportRowMapper = new ParameterizedRowMapper<ExchangeRateReport>() {

        public ExchangeRateReport mapRow(ResultSet rs, int row) throws SQLException {
            ExchangeRateReport exchangeRateReport = new ExchangeRateReport();
            exchangeRateReport.setCurrencyCode(rs.getInt("currencyid"));
            exchangeRateReport.setExchangeRate(rs.getDouble("exchangerate"));
            return exchangeRateReport;
        }
    };

    @Override
    public List<DataExtractReport> getProjectDataExtractReport(final DataExtractReportFilters projectDataExtractFilters)
    {
        List<Long> projectIDs = Collections.emptyList();
        if(projectDataExtractFilters!= null)
        {
            projectIDs= getProjectIds(projectDataExtractFilters.getSupplier(), projectDataExtractFilters.getSupplierGroup(), projectDataExtractFilters.getEndMarkets(),projectDataExtractFilters.getRegionFields());
        }
        String LOAD_PROJECTS = getProjectQuery(projectIDs,projectDataExtractFilters.getWorkflowType(),projectDataExtractFilters.getMethodologyFields(),projectDataExtractFilters.getBrandFields(),projectDataExtractFilters.getStartMonth(),projectDataExtractFilters.getStartYear(),projectDataExtractFilters.getEndMonth(),projectDataExtractFilters.getEndYear(),projectDataExtractFilters.getProductTypeFields());

        List<DataExtractReport> projects = Collections.emptyList();
        try{
            /*projects = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_PROJECTS,
                           dataExtractReportMapper,SynchroGlobal.Status.DRAFT.ordinal(),SynchroGlobal.Status.DELETED.ordinal(),SynchroGlobal.Status.CONCEPT_CANCEL.ordinal(),SynchroGlobal.Status.PLANNED_CANCEL.ordinal(),SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal());*/

        }
        catch (DataAccessException e) {
            final String message = "Failed to get Research Cycle Report for projects ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;

    }

    @Override
    public List<RawExtractReportBean> getRawExtractReport(final StandardReportFilter filter) {
        Quarter quarter = QuarterRangeUtil.getCurrentQuarter();
        Integer countryInvestmentType = SynchroGlobal.InvestmentType.COUNTRY.getId();
        List<RawExtractReportBean> rawExtractReportBeans = null;
        StringBuilder sql = new StringBuilder();
//        sql.append("SELECT pcf.projectid as projectid, pcf.endmarketid as endmarketid, pcf.awardedagency as supplier");
        sql.append("SELECT pcf.projectid as projectid, pcf.endmarketid as endmarketid, p.agencyDept as supplier");
        sql.append(", pcf.agency1 as agency1, pcf.agency1optional as agency1optional, pcf.agency2 as agency2, pcf.agency2optional as agency2optional, pcf.agency3 as agency3, pcf.agency3optional as agency3optional");
        sql.append(", pcf.ismultimarket as ismultimarket, pcf.projectStartDate as projectStartDate, pcf.projectEndDate as projectEndDate,pcf.isabovemarket as isabovemarket,pcf.investmentType as investmentType");
        sql.append(", pcf.spiContact as spiContact, pcf.projectOwner as projectOwner, pcf.projectStatus as projectStatus, pcf.investmentType as investmentType");
        sql.append(", (CASE WHEN (pcf.ismultimarket = 0 OR pcf.investmenttype != "+countryInvestmentType+") THEN -1 ELSE (select ems.status from grailprojectstatus ems where ems.projectid = pcf.projectid and ems.endmarketid = pcf.endmarketid and pcf.investmenttype = "+ countryInvestmentType +") END) as endmarketStatus");
        sql.append(", (CASE WHEN (pcf.spiContact is not null) THEN  (SELECT (ju.firstname || ' ' || ju.lastname) FROM jiveuser ju where ju.userid = pcf.spiContact) ELSE '' END)as spiContactName");
        sql.append(", (SELECT (ju.firstname || ' ' || ju.lastname) FROM jiveuser ju where ju.userid = pcf.projectOwner) as projectOwnerName");
        sql.append(", pcf.projectStartDate as projectStartDate, pcf.projectEndDate as projectEndDate");
        sql.append(", pcf.propTotalCost as propTotalCost,pcf.propTotalCostCurrency as propTotalCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.propTotalCostCurrency) as propTotalCostCurrencyName");
        sql.append(", pcf.propInlMgmtCost as propInlMgmtCost, pcf.propInlMgmtCostCurrency as propInlMgmtCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.propInlMgmtCostCurrency) as propInlMgmtCostCurrencyName");
        sql.append(", pcf.propLocalMgmtCost as propLocalMgmtCost,pcf.propLocalMgmtCostCurrency as propLocalMgmtCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.propLocalMgmtCostCurrency) as propLocalMgmtCostCurrencyName");
        sql.append(", pcf.propFieldworkCost as propFieldworkCost, pcf.propFieldworkCostCurrency as propFieldworkCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.propFieldworkCostCurrency) as propFieldworkCostCurrencyName");
        sql.append(", pcf.propOperHubCost as propOperHubCost,pcf.propOperHubCostCurrency as propOperHubCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.propOperHubCostCurrency) as propOperHubCostCurrencyName");
        sql.append(", pcf.propOtherCost as propOtherCost, pcf.propOtherCostCurrency as propOtherCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.propOtherCostCurrency) as propOtherCostCurrencyName");
        sql.append(", pcf.tenderingCost as tenderingCost, pcf.tenderingCostCurrency as tenderingCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.tenderingCostCurrency) as tenderingCostCurrencyName");
        sql.append(", p.name as projectName, p.budgetyear as budgetYear");
        sql.append(", (CASE" +
                " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.GlOBAL.getId() +
                " THEN 'Global'" +
                " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.REGION.getId() +
                " THEN (SELECT rf.name FROM grailregionfields rf WHERE rf.id = pcf.endmarketid)" +
                " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.AREA.getId() +
                " THEN (SELECT af.name FROM grailareafields af WHERE af.id = pcf.endmarketid)" +
                " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.COUNTRY.getId() +
                " THEN (SELECT emf.name FROM grailEndMarketFields emf WHERE emf.id = pcf.endmarketid)" +
                " END) as endmarketName");
        sql.append(",pcf.totalprojectcost as totalprojectcost,pcf.totalprojectcostcurrency as totalprojectcostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.totalprojectcostcurrency) as totalprojectcostcurrencyName");

        sql.append(", pcf.latestprojectcost as latestProjectCost");
        sql.append(", pcf.latestprojectcostcurrency as latestProjectCostCurrency");
        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.latestprojectcostcurrency) as latestprojectcostcurrencyName");

//        sql.append(", (CASE WHEN (pcf.ismultimarket = 1 AND pcf.investmentType = "+SynchroGlobal.InvestmentType.COUNTRY.getId()+" AND pcf.latestprojectcost <= 0 AND (SELECT count(*) FROM grailprojectcostfields pcf11 WHERE pcf11.projectid = pcf.projectid AND pcf11.investmentType = "+SynchroGlobal.InvestmentType.GlOBAL.getId()+" AND pcf11.latestprojectcost > 0 AND (SELECT MAX(pcfx.updateddatetime) FROM grailprojectcostfields pcfx WHERE pcfx.projectid = pcf11.projectid AND pcfx.endmarketid = pcf11.endmarketid AND pcfx.investmenttype = pcf11.investmenttype) = pcf11.updateddatetime) > 0) THEN (SELECT pcf12.latestprojectcost FROM grailprojectcostfields pcf12 WHERE pcf12.projectid = pcf.projectid AND pcf12.investmentType = "+SynchroGlobal.InvestmentType.GlOBAL.getId()+" AND (SELECT MAX(pcfx.updateddatetime) FROM grailprojectcostfields pcfx WHERE pcfx.projectid = pcf12.projectid AND pcfx.endmarketid = pcf12.endmarketid AND pcfx.investmenttype = pcf12.investmenttype) = pcf12.updateddatetime) ELSE pcf.latestprojectcost END) as latestProjectCost");
//        sql.append(", (CASE WHEN (pcf.ismultimarket = 1 AND pcf.investmentType = "+SynchroGlobal.InvestmentType.COUNTRY.getId()+" AND pcf.latestprojectcost <= 0 AND (SELECT count(*) FROM grailprojectcostfields pcf11 WHERE pcf11.projectid = pcf.projectid AND pcf11.investmentType = "+SynchroGlobal.InvestmentType.GlOBAL.getId()+" AND pcf11.latestprojectcost > 0 AND (SELECT MAX(pcfx.updateddatetime) FROM grailprojectcostfields pcfx WHERE pcfx.projectid = pcf11.projectid AND pcfx.endmarketid = pcf11.endmarketid AND pcfx.investmenttype = pcf11.investmenttype) = pcf11.updateddatetime) > 0) THEN (SELECT pcf12.latestprojectcostcurrency FROM grailprojectcostfields pcf12 WHERE pcf12.projectid = pcf.projectid AND pcf12.investmentType = "+SynchroGlobal.InvestmentType.GlOBAL.getId()+" AND (SELECT MAX(pcfx.updateddatetime) FROM grailprojectcostfields pcfx WHERE pcfx.projectid = pcf12.projectid AND pcfx.endmarketid = pcf12.endmarketid AND pcfx.investmenttype = pcf12.investmenttype) = pcf12.updateddatetime) ELSE pcf.latestprojectcostcurrency END) as latestProjectCostCurrency");
//        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = (CASE WHEN (pcf.ismultimarket = 1 AND pcf.investmentType = "+SynchroGlobal.InvestmentType.COUNTRY.getId()+" AND pcf.latestprojectcost <= 0 AND (SELECT count(*) FROM grailprojectcostfields pcf11 WHERE pcf11.projectid = pcf.projectid AND pcf11.investmentType = "+SynchroGlobal.InvestmentType.GlOBAL.getId()+" AND pcf11.latestprojectcost > 0 AND (SELECT MAX(pcfx.updateddatetime) FROM grailprojectcostfields pcfx WHERE pcfx.projectid = pcf11.projectid AND pcfx.endmarketid = pcf11.endmarketid AND pcfx.investmenttype = pcf11.investmenttype) = pcf11.updateddatetime) > 0) THEN (SELECT pcf12.latestprojectcostcurrency FROM grailprojectcostfields pcf12 WHERE pcf12.projectid = pcf.projectid AND pcf12.investmentType = "+SynchroGlobal.InvestmentType.GlOBAL.getId()+" AND (SELECT MAX(pcfx.updateddatetime) FROM grailprojectcostfields pcfx WHERE pcfx.projectid = pcf12.projectid AND pcfx.endmarketid = pcf12.endmarketid AND pcfx.investmenttype = pcf12.investmenttype) = pcf12.updateddatetime) ELSE pcf.latestprojectcostcurrency END)) as latestprojectcostcurrencyName");

        sql.append(", (SELECT bf.name from grailbrandfields bf WHERE bf.id = p.brand) as brandName");

        sql.append(", (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.latestprojectcostcurrency) as latestprojectcostcurrencyName");
        sql.append(", (SELECT bf.name from grailbrandfields bf WHERE bf.id = p.brand) as brandName");
        sql.append(",  (select mtf.name from grailmethodologytypefields mtf WHERE mtf.id = p.methodologytype) as methodologytypename");
        sql.append(",  p.proposedmethodology as proposedmethodology");
        sql.append(",  p.caprating as caprating, pcf.datacollection as datacollection");
        sql.append(" FROM grailprojectcostfields pcf INNER JOIN grailproject p ON (p.projectid = pcf.projectid) ");
        sql.append(" WHERE ");
        sql.append("(SELECT MAX(pcf1.updateddatetime) FROM grailprojectcostfields pcf1 WHERE pcf1.projectid = pcf.projectid AND pcf1.endmarketid = pcf.endmarketid AND pcf.investmenttype = pcf1.investmenttype group by pcf1.projectid,pcf1.investmenttype, pcf1.endmarketid) = pcf.updateddatetime");
        sql.append(" AND (to_char(to_timestamp(pcf.updateddatetime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth()+")");
        sql.append(" AND (to_char(to_timestamp(pcf.updateddatetime/1000),'yyyy')::int = "+quarter.getStartYear()+")");
        sql.append(" AND ((pcf.investmentType = " + SynchroGlobal.InvestmentType.GlOBAL.getId()+")" +
                " OR (pcf.investmentType = " + SynchroGlobal.InvestmentType.COUNTRY.getId() +" AND pcf.endmarketid = (SELECT em.id FROM grailendmarketfields em WHERE em.id = pcf.endmarketid AND em.isactive = 1 group by em.id))" +
                " OR (pcf.investmentType = " + SynchroGlobal.InvestmentType.AREA.getId() +" AND pcf.endmarketid = (SELECT af.id FROM grailareafields af WHERE af.id = pcf.endmarketid AND af.isactive = 1 group by af.id))" +
                " OR (pcf.investmentType = " + SynchroGlobal.InvestmentType.REGION.getId() +" AND pcf.endmarketid = (SELECT rf.id FROM grailregionfields rf WHERE rf.id = pcf.endmarketid AND rf.isactive = 1 group by rf.id)))");
        if(filter != null && filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            sql.append(" AND ").append(" (");
            sql.append("('' || p.projectid ||'') like '%" + filter.getKeyword() + "%' OR lower(p.name) like '%" + filter.getKeyword().toLowerCase() + "%'")
                    .append(" OR ")
                    .append("lower(CASE" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.GlOBAL.getId() +
                            " THEN 'Global'" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.REGION.getId() +
                            " THEN (SELECT rf.name FROM grailregionfields rf WHERE rf.id = pcf.endmarketid)" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.AREA.getId() +
                            " THEN (SELECT af.name FROM grailareafields af WHERE af.id = pcf.endmarketid)" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.COUNTRY.getId() +
                            " THEN (SELECT emf.name FROM grailEndMarketFields emf WHERE emf.id = pcf.endmarketid)" +
                            " END) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("(SELECT lower(bf.name) from grailbrandfields bf WHERE bf.id = p.brand) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("lower(CASE WHEN (p.caprating is null OR p.caprating <= 0) THEN 'NONE' WHEN p.caprating = 1 THEN 'CAP1' WHEN p.caprating = 2 THEN 'CAP2' WHEN p.caprating = 3 THEN 'CAP3' WHEN p.caprating = 4 THEN 'Not known' END) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("lower(CASE " +
                            " WHEN (pcf.awardedagency is null OR pcf.awardedagency <= 0)" +
                            " THEN 'Not Defined'" +
                            " WHEN ((select count(*) from jiveuserprofile jup, grailuserdepartments gud where gud.id::text = jup.value and jup.fieldid = 2 and jup.userid = pcf.awardedagency) < 0)" +
                            " THEN 'Not Defined'" +
                            " ELSE" +
                            " (select gud.name from jiveuserprofile jup, grailuserdepartments gud where gud.id::text = jup.value and jup.fieldid = 2 and jup.userid = pcf.awardedagency)" +
                            " END) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("(select count(*) from grailmethodologyfields m where m.id = ANY(('{' || p.proposedmethodology || '}')::int[]) and lower(m.name) like '%"+filter.getKeyword().toLowerCase()+"%') > 0")
                    .append(" OR ")
                    .append("lower(CASE WHEN (pcf.latestprojectcostcurrency is null OR pcf.latestprojectcostcurrency <= 0) THEN 'GBP' ELSE (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.latestprojectcostcurrency and cf.isactive = 1) END) like '%"+filter.getKeyword().toLowerCase()+"%'");


            sql.append(")");
        }
        sql.append(" ORDER BY pcf.projectid, pcf.investmenttype, pcf.endmarketid, endmarketName");
        if(filter != null) {
            if(filter.getStart() != null) {
                sql.append(" OFFSET ").append(filter.getStart());
            }
            if(filter.getLimit() != null) {
                sql.append(" LIMIT ").append(filter.getLimit());
            }
        }
        try {
            rawExtractReportBeans = getJdbcTemplate().query(sql.toString(), standardReportParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return rawExtractReportBeans;
    }


    @Override
    public Long getRawExtractReportTotalCount(final StandardReportFilter filter) {
        Long totalCount = 0L;
        StringBuilder sql = new StringBuilder();
        Quarter quarter = QuarterRangeUtil.getCurrentQuarter();
        sql.append("SELECT count(*)");
        sql.append(" FROM grailprojectcostfields pcf INNER JOIN grailproject p ON (p.projectid = pcf.projectid) ");
        sql.append(" WHERE ");
        sql.append("(SELECT MAX(pcf1.updateddatetime) FROM grailprojectcostfields pcf1 WHERE pcf1.projectid = pcf.projectid AND pcf1.endmarketid = pcf.endmarketid group by pcf1.projectid, pcf1.endmarketid) = pcf.updateddatetime");
        sql.append(" AND (to_char(to_timestamp(pcf.updateddatetime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth()+")");
        if(filter != null && filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            sql.append(" AND ").append(" (");
            sql.append("('' || p.projectid ||'') like '%" + filter.getKeyword() + "%' OR lower(p.name) like '%" + filter.getKeyword().toLowerCase() + "%'")
                    .append(" OR ")
                    .append("lower(CASE" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.GlOBAL.getId() +
                            " THEN 'Global'" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.REGION.getId() +
                            " THEN (SELECT rf.name FROM grailregionfields rf WHERE rf.id = pcf.endmarketid)" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.AREA.getId() +
                            " THEN (SELECT af.name FROM grailareafields af WHERE af.id = pcf.endmarketid)" +
                            " WHEN pcf.investmentType = " + SynchroGlobal.InvestmentType.COUNTRY.getId() +
                            " THEN (SELECT emf.name FROM grailEndMarketFields emf WHERE emf.id = pcf.endmarketid)" +
                            " END) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("(SELECT lower(bf.name) from grailbrandfields bf WHERE bf.id = p.brand) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("lower(CASE WHEN (p.caprating is null OR p.caprating <= 0) THEN 'NONE' WHEN p.caprating = 1 THEN 'CAP1' WHEN p.caprating = 2 THEN 'CAP2' WHEN p.caprating = 3 THEN 'CAP3' WHEN p.caprating = 4 THEN 'Not known' END) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("lower(CASE " +
                            " WHEN (pcf.awardedagency is null OR pcf.awardedagency <= 0)" +
                            " THEN 'Not Defined'" +
                            " WHEN ((select count(*) from jiveuserprofile jup, grailuserdepartments gud where gud.id::text = jup.value and jup.fieldid = 2 and jup.userid = pcf.awardedagency) < 0)" +
                            " THEN 'Not Defined'" +
                            " ELSE" +
                            " (select gud.name from jiveuserprofile jup, grailuserdepartments gud where gud.id::text = jup.value and jup.fieldid = 2 and jup.userid = pcf.awardedagency)" +
                            " END) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("(select count(*) from grailmethodologyfields m where m.id = ANY(('{' || p.proposedmethodology || '}')::int[]) and lower(m.name) like '%"+filter.getKeyword().toLowerCase()+"%') > 0")
                    .append(" OR ")
                    .append("lower(CASE WHEN (pcf.latestprojectcostcurrency is null OR pcf.latestprojectcostcurrency <= 0) THEN 'GBP' ELSE (SELECT cf.name FROM grailcurrencyfields cf WHERE cf.id = pcf.latestprojectcostcurrency and cf.isactive = 1) END) like '%"+filter.getKeyword().toLowerCase()+"%'");
            sql.append(")");
        }
        try {
            totalCount = getJdbcTemplate().queryForLong(sql.toString());
        } catch (DAOException e) {
            LOG.error(e.getMessage());
        }
        return totalCount;
    }

    private final ParameterizedRowMapper<RawExtractReportBean> standardReportParameterizedRowMapper = new ParameterizedRowMapper<RawExtractReportBean>() {
        public RawExtractReportBean mapRow(ResultSet rs, int row) throws SQLException {

            RawExtractReportBean rawExtractReportBean = new RawExtractReportBean();
            rawExtractReportBean.setProjectId(rs.getLong("projectid"));
            rawExtractReportBean.setProjectName(rs.getString("projectName"));
            boolean isMultiMarket = rs.getBoolean("ismultimarket" );
            rawExtractReportBean.setMultiMarket(isMultiMarket);
            rawExtractReportBean.setMarketId(rs.getLong("endmarketid"));
            rawExtractReportBean.setAboveMarket(rs.getBoolean("isabovemarket"));
            rawExtractReportBean.setSpiContact(rs.getString("spiContactName"));
            rawExtractReportBean.setProjectOwner(rs.getString("projectOwnerName"));

            Integer projectStatusId = rs.getInt("projectStatus");
            Integer endmarketStatusId = rs.getInt("endmarketStatus");
            Integer investmentType = rs.getInt("investmentType");
            if(isMultiMarket && investmentType.equals(SynchroGlobal.InvestmentType.COUNTRY.getId())
                    && endmarketStatusId != null && endmarketStatusId > -1) {
                if(endmarketStatusId.equals(SynchroGlobal.ProjectActivationStatus.OPEN.ordinal())) {
                    rawExtractReportBean.setProjectStatus("Endmarket Open");
                } else if(endmarketStatusId.equals(SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal())) {
                    rawExtractReportBean.setProjectStatus("Endmarket Cancelled");
                } else if(endmarketStatusId.equals(SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal())) {
                    rawExtractReportBean.setProjectStatus("Endmarket On-Hold");
                } else if(endmarketStatusId.equals(SynchroGlobal.ProjectActivationStatus.DELETED.ordinal())) {
                    rawExtractReportBean.setProjectStatus("Endmarket Deleted");
                }
            } else {
                rawExtractReportBean.setProjectStatus(SynchroGlobal.Status.getName(projectStatusId));
            }


            rawExtractReportBean.setProjectStartDate(new Date(rs.getLong("projectStartDate")));
            rawExtractReportBean.setProjectEndDate(new Date(rs.getLong("projectEndDate")));

            rawExtractReportBean.setMarket(rs.getString("endmarketName"));

            rawExtractReportBean.setBrand(rs.getString("brandName"));

            rawExtractReportBean.setBudgetYear(rs.getInt("budgetYear"));


            StringBuilder supplierBuilder = new StringBuilder();
//            Long agency1 = rs.getLong("agency1");
//            if(agency1 != null && agency1 > 0) {
//                try {
//                    Map<Long, ProfileFieldValue> profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(agency1));
//                    if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
//                        supplierBuilder.append(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
//                    }
//                } catch (UserNotFoundException e) {
//                    LOG.error(e.getMessage());
//                }
//            }
//
//            Long agency2 = rs.getLong("agency2");
//            if(agency2 != null && agency2 > 0) {
//                try {
//                    Map<Long, ProfileFieldValue> profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(agency2));
//                    if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
//                        if(supplierBuilder.length() > 0) {
//                            supplierBuilder.append(", ");
//                        }
//                        supplierBuilder.append(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
//                    }
//                } catch (UserNotFoundException e) {
//                    LOG.error(e.getMessage());
//                }
//            }
//
//            Long agency3 = rs.getLong("agency3");
//            if(agency3 != null && agency3 > 0) {
//                try {
//                    Map<Long, ProfileFieldValue> profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(agency3));
//                    if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
//                        if(supplierBuilder.length() > 0) {
//                            supplierBuilder.append(", ");
//                        }
//                        supplierBuilder.append(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
//                    }
//                } catch (UserNotFoundException e) {
//                    LOG.error(e.getMessage());
//                }
//            }

            Long supplierId = rs.getLong("supplier");
            if(supplierId != null && supplierId > 0) {
                supplierBuilder.append(SynchroGlobal.getDepartmentNameById(supplierId.toString()));
//                try {
//                    Map<Long, ProfileFieldValue> profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(supplierId));
//                    if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
//                        String value = profileFieldMap.get(2L).getValue();
//                        supplierBuilder.append(SynchroGlobal.getDepartmentNameById(profileFieldMap.get(2L).getValue()));
//                    }
//                } catch (UserNotFoundException e) {
//                    LOG.error(e.getMessage());
//                }
            }

            if(supplierBuilder.length() > 0) {
                rawExtractReportBean.setSupplier(supplierBuilder.toString());
            } else {
                rawExtractReportBean.setSupplier("NOT DEFINED");
            }

            //rawExtractReportBean.setCapRating(rs.getString("caprating"));
            Long capRatingID  = rs.getLong("caprating");
            if(capRatingID != null && capRatingID.intValue() > 0){
                if(SynchroGlobal.getProjectTypes().containsKey(capRatingID.intValue())) {
                    rawExtractReportBean.setCapRating(SynchroGlobal.getProjectTypes().get(capRatingID.intValue()));
                } else {
                    rawExtractReportBean.setCapRating("NONE");
                }
            } else {
                rawExtractReportBean.setCapRating("NONE");
            }

            rawExtractReportBean.setMethodologyType(rs.getString("methodologytypename"));

            String methodology = rs.getString("proposedmethodology");
            Map<Integer, String> propmethodologies = SynchroGlobal.getAllMethodologies();
            if(methodology != null && !methodology.equals("")) {
                String[] mids = methodology.split(",");
                List<String> methodologies = new ArrayList<String>();
                for(int i = 0; i < mids.length; i++) {
                    String meth = propmethodologies.get(Integer.parseInt(mids[i]));
                    if(meth != null && !meth.equals("")) {
                        methodologies.add(meth);
                    }
                }
                if(methodologies.size() > 0) {
                    rawExtractReportBean.setMethodology(StringUtils.join(methodologies, ", "));
                } else {
                    rawExtractReportBean.setMethodology("");
                }
            }

            BigDecimal latestProjectCost = rs.getBigDecimal("latestProjectCost");
            if(latestProjectCost != null) {
                rawExtractReportBean.setLatestProjectCost(latestProjectCost);
                rawExtractReportBean.setLatestProjectCostCurrency(rs.getString("latestprojectcostcurrencyName"));
                Long latestProjectCostCurrency  = rs.getLong("latestProjectCostCurrency");
                if(latestProjectCostCurrency != null && latestProjectCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(latestProjectCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setLatestProjectCostDBPRate(BigDecimal.valueOf(rate * latestProjectCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setLatestProjectCostDBPRate(latestProjectCost);
                    }
                } else {
                    rawExtractReportBean.setLatestProjectCostDBPRate(latestProjectCost);
                    rawExtractReportBean.setLatestProjectCostCurrency("GBP");
                }
            }

            BigDecimal totalProjectCost = rs.getBigDecimal("totalProjectCost");
            if(totalProjectCost != null) {
                rawExtractReportBean.setTotalProjectCost(totalProjectCost);
                rawExtractReportBean.setTotalProjectCostCurrency(rs.getString("totalprojectcostcurrencyName"));
                Long totalProjectCostCurrency  = rs.getLong("totalProjectCostCurrency");
                if(totalProjectCostCurrency != null && totalProjectCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(totalProjectCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setTotalProjectCostDBPRate(BigDecimal.valueOf(rate * totalProjectCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setTotalProjectCostDBPRate(totalProjectCost);
                    }
                } else {
                    rawExtractReportBean.setTotalProjectCostDBPRate(totalProjectCost);
                    rawExtractReportBean.setTotalProjectCostCurrency("GBP");
                }
            }


            BigDecimal tenderingCost = rs.getBigDecimal("tenderingCost");
            if(tenderingCost != null) {
                rawExtractReportBean.setTenderingCost(tenderingCost);
                rawExtractReportBean.setTenderingCostCurrency(rs.getString("tenderingcostcurrencyName"));
                Long tenderingCostCurrency  = rs.getLong("tenderingCostCurrency");
                if(tenderingCostCurrency != null && tenderingCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(tenderingCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setTenderingCostDBPRate(BigDecimal.valueOf(rate * tenderingCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setTenderingCostDBPRate(tenderingCost);
                    }
                } else {
                    rawExtractReportBean.setTenderingCostDBPRate(tenderingCost);
                    rawExtractReportBean.setTenderingCostCurrency("GBP");
                }
            }

            BigDecimal propTotalCost = rs.getBigDecimal("propTotalCost");
            if(propTotalCost != null) {
                rawExtractReportBean.setProposalTotalCost(propTotalCost);
                rawExtractReportBean.setProposalTotalCostCurrency(rs.getString("propTotalCostCurrencyName"));
                Long propTotalCostCurrency  = rs.getLong("propTotalCostCurrency");
                if(propTotalCostCurrency != null && propTotalCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(propTotalCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setProposalTotalCostDBPRate(BigDecimal.valueOf(rate * propTotalCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setProposalTotalCostDBPRate(propTotalCost);
                    }
                } else {
                    rawExtractReportBean.setProposalTotalCostDBPRate(propTotalCost);
                    rawExtractReportBean.setProposalTotalCostCurrency("GBP");
                }
            }

            BigDecimal propInlMgmtCost = rs.getBigDecimal("propInlMgmtCost");
            if(propInlMgmtCost != null) {
                rawExtractReportBean.setProposalInternationalMgmtCost(propInlMgmtCost);
                rawExtractReportBean.setProposalInternationalMgmtCostCurrency(rs.getString("propInlMgmtCostCurrencyName"));
                Long propInlMgmtCostCurrency  = rs.getLong("propInlMgmtCostCurrency");
                if(propInlMgmtCostCurrency != null && propInlMgmtCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(propInlMgmtCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setProposalInternationalMgmtCostDBPRate(BigDecimal.valueOf(rate * propInlMgmtCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setProposalInternationalMgmtCostDBPRate(propInlMgmtCost);
                    }
                } else {
                    rawExtractReportBean.setProposalInternationalMgmtCostDBPRate(propInlMgmtCost);
                    rawExtractReportBean.setProposalInternationalMgmtCostCurrency("GBP");
                }
            }

            BigDecimal propLocalMgmtCost = rs.getBigDecimal("propLocalMgmtCost");
            if(propLocalMgmtCost != null) {
                rawExtractReportBean.setProposalLocalMgmtCost(propLocalMgmtCost);
                rawExtractReportBean.setProposalLocalMgmtCostCurrency(rs.getString("propLocalMgmtCostCurrencyName"));
                Long propLocalMgmtCostCurrency  = rs.getLong("propLocalMgmtCostCurrency");
                if(propLocalMgmtCostCurrency != null && propLocalMgmtCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(propLocalMgmtCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setProposalLocalMgmtCostDBPRate(BigDecimal.valueOf(rate * propLocalMgmtCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setProposalLocalMgmtCostDBPRate(propLocalMgmtCost);
                    }
                } else {
                    rawExtractReportBean.setProposalLocalMgmtCostDBPRate(propLocalMgmtCost);
                    rawExtractReportBean.setProposalLocalMgmtCostCurrency("GBP");
                }
            }

            BigDecimal propFieldworkCost = rs.getBigDecimal("propFieldworkCost");
            if(propFieldworkCost != null) {
                rawExtractReportBean.setProposalFieldworkCost(propFieldworkCost);
                rawExtractReportBean.setProposalFieldworkCostCurrency(rs.getString("propFieldworkCostCurrencyName"));
                Long propFieldworkCostCurrency  = rs.getLong("propFieldworkCostCurrency");
                if(propFieldworkCostCurrency != null && propFieldworkCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(propFieldworkCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setProposalFieldworkCostDBPRate(BigDecimal.valueOf(rate * propFieldworkCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setProposalFieldworkCostDBPRate(propFieldworkCost);
                    }
                } else {
                    rawExtractReportBean.setProposalFieldworkCostDBPRate(propFieldworkCost);
                    rawExtractReportBean.setProposalFieldworkCostCurrency("GBP");
                }

            }

            BigDecimal propOperHubCost = rs.getBigDecimal("propOperHubCost");
            if(propOperHubCost != null) {
                rawExtractReportBean.setProposalOperationHubCost(propOperHubCost);
                rawExtractReportBean.setProposalOperationHubCostCurrency(rs.getString("propOperHubCostCurrencyName"));
                Long propOperHubCostCurrency  = rs.getLong("propOperHubCostCurrency");
                if(propOperHubCostCurrency != null && propOperHubCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(propOperHubCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setProposalOperationHubCostDBPRate(BigDecimal.valueOf(rate * propOperHubCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setProposalOperationHubCostDBPRate(propOperHubCost);
                    }
                } else {
                    rawExtractReportBean.setProposalOperationHubCostDBPRate(propOperHubCost);
                    rawExtractReportBean.setProposalOperationHubCostCurrency("GBP");
                }
            }

            BigDecimal propOtherCost = rs.getBigDecimal("propOtherCost");
            if(propOtherCost != null) {
                rawExtractReportBean.setProposalOtherCost(propOperHubCost);
                rawExtractReportBean.setProposalOtherCostCurrency(rs.getString("propOtherCostCurrencyName"));
                Long propOtherCostCurrency  = rs.getLong("propOtherCostCurrency");
                if(propOtherCostCurrency != null && propOtherCostCurrency > 0) {
                    Double rate = SynchroUtils.getCurrencyExchangeRate(propOtherCostCurrency);
                    if(rate != null && rate > 0) {
                        rawExtractReportBean.setProposalOtherCostDBPRate(BigDecimal.valueOf(rate * propOtherCost.doubleValue()));
                    } else {
                        rawExtractReportBean.setProposalOtherCostDBPRate(propOtherCost);
                    }
                } else {
                    rawExtractReportBean.setProposalOtherCostDBPRate(propOtherCost);
                    rawExtractReportBean.setProposalOtherCostCurrency("GBP");
                }
            }


            String dc = rs.getString("datacollection");
            if(dc != null && !dc.equals("")) {
                Map<Integer, String> dataCollections = SynchroGlobal.getDataCollections();
                List<String> dcList = new ArrayList<String>();
                String[] dcStrings = dc.split(",");
                for(int i = 0; i < dcStrings.length; i++) {
                    if(dcStrings[i] != null && !dcStrings[i].equals("")) {
                        dcList.add(dataCollections.get(Integer.parseInt(dcStrings[i])));
                    }
                }
                if(dcList.size() > 0) {
                    rawExtractReportBean.setDataCollectionMethod(StringUtils.join(dcList,", "));
                }
            }

            return rawExtractReportBean;
        }
    };


    private List<Long> getProjectIds(Long supplierID,Long supplierGroupID,List<Long> endMarkets,List<Long> regions ) {
        Boolean flag = true;
        List<Long> projectIDs = Collections.emptyList();
        List<Long> c_projectIDs = Collections.emptyList();
        List<Long> e_projectIDs = Collections.emptyList();
        String COORDINATION_DETAILS_SQL = "SELECT projectid FROM grailcoordinationdetails";
        //Long supplierID = researchCycleReportFilters.getSupplier();
        //Long supplierGroupID = researchCycleReportFilters.getSupplierGroup();
        Boolean fetchRows = false;
        Boolean fetchProjects = true;
        //Coordination details
        if(supplierID != null || supplierGroupID != null)
        {
            if(supplierID!=null && supplierID > 0)
            {
                if(flag)
                {
                    COORDINATION_DETAILS_SQL = COORDINATION_DETAILS_SQL + " where supplier = " + supplierID;
                    flag = false;
                }
                else
                {
                    COORDINATION_DETAILS_SQL = COORDINATION_DETAILS_SQL + " and supplier = " + supplierID;
                }
                fetchRows = true;
            }
            if(supplierGroupID!=null && supplierGroupID > 0)
            {
                if(flag)
                {
                    COORDINATION_DETAILS_SQL = COORDINATION_DETAILS_SQL + " where suppliergroup = " + supplierGroupID;
                    flag = false;
                }
                else
                {
                    COORDINATION_DETAILS_SQL = COORDINATION_DETAILS_SQL + " and suppliergroup = " + supplierGroupID;
                }
                fetchRows = true;
            }


            try {
                if(fetchRows)
                {
                    c_projectIDs = getSimpleJdbcTemplate().getJdbcOperations().queryForList(COORDINATION_DETAILS_SQL,
                            Long.class);
                }
            }
            catch (DataAccessException e) {
                final String message = "Failed to load research cycle report ";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }

        flag = true;
        //Fetch Region specific filters
        List<Long> filterEndMarketIDs = new ArrayList<Long>();
        //List<Long> regions = researchCycleReportFilters.getRegionFields();
        /*List<Long> regionEndMarketIDs = new ArrayList<Long>();
                       if(regions != null && regions.size() > 0)
                       {
                           Map<String, Integer> regionMapping = SynchroGlobal.getRegionEndMarketEnum();
                           for(String emID : regionMapping.keySet())
                           {
                               Integer regionID = regionMapping.get(emID)+1;
                               if((regions.contains(Long.parseLong(regionID.toString())) || regions.contains(Long.parseLong((SynchroGlobal.Region.GLOBAL.ordinal()+1)+""))) && !regionEndMarketIDs.contains(Long.parseLong(regionID.toString())))
                               {
                                   regionEndMarketIDs.add(Long.parseLong(emID+""));
                               }
                           }
                       }
                       //End Market details
                       //List<Long> endMarkets = researchCycleReportFilters.getEndMarkets();
                       if(endMarkets!=null && endMarkets.size() > 0)
                       {
                           filterEndMarketIDs.addAll(endMarkets);
                       }
                       if(regionEndMarketIDs!=null && regionEndMarketIDs.size() > 0)
                       {
                           filterEndMarketIDs.addAll(regionEndMarketIDs);
                       }
                       */
        if(filterEndMarketIDs != null && filterEndMarketIDs.size() > 0)
        {
            //Filter out the unique values
            Set<Long> set  = new HashSet<Long>(filterEndMarketIDs);
            ArrayList<Long> filterEndMarketIDsUnique = new ArrayList<Long>();
            filterEndMarketIDsUnique.addAll(set);

            String ENDMARKET_DETAILS_SQL = "SELECT projectid FROM grailendmarketdetails where endmarketid in ("+StringUtils.join(filterEndMarketIDsUnique, ',')+")";

            try {
                e_projectIDs = getSimpleJdbcTemplate().getJdbcOperations().queryForList(ENDMARKET_DETAILS_SQL,
                        Long.class);
                if(e_projectIDs == null || e_projectIDs.size() < 1)
                {
                    //fetchProjects = false;
                    return null;
                }
            }
            catch (DataAccessException e) {
                final String message = "Failed to load research cycle report ";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }

        if(c_projectIDs.size()>0 && e_projectIDs.size()>0)
        {
            c_projectIDs.retainAll(e_projectIDs);
            projectIDs = c_projectIDs;
        }
        else if(c_projectIDs.size()>0)
        {
            projectIDs = c_projectIDs;
        }
        else if(e_projectIDs.size()>0)
        {
            projectIDs = e_projectIDs;
        }

        return projectIDs;
    }
    private String getProjectQuery(List<Long> projectIDs,String workflowType,List<Long> methodologyFields,List<Long> brandFields,Integer startMonth,Integer startYear,Integer endMonth,Integer endYear,List<Long> productTypeFields)
    {

        String LOAD_PROJECTS = "Select projectID, name, ownerID, status, startMonth, startYear, endMonth, endYear, projecttype, brand, methodologygroup, methodology, researchtype, insights, npi, isfwenabled, partialmethodologywaiver FROM grailproject WHERE status not in (?,?,?,?,?)";
        if(projectIDs.size()>0)
        {
            LOAD_PROJECTS = LOAD_PROJECTS + " and projectID in ("+StringUtils.join(projectIDs, ',')+")";

        }
        if(workflowType!=null && !workflowType.equals("-1"))
        {
            LOAD_PROJECTS = LOAD_PROJECTS + " and workflowtype = '"+workflowType+"'";
        }

        if(methodologyFields!=null)
        {
            LOAD_PROJECTS = LOAD_PROJECTS + " and methodology in ("+StringUtils.join(methodologyFields, ',')+")";
        }

        if(brandFields!=null)
        {
            LOAD_PROJECTS = LOAD_PROJECTS + " and brand in ("+StringUtils.join(brandFields, ',')+")";
        }

        if(productTypeFields!=null)
        {
            LOAD_PROJECTS = LOAD_PROJECTS + " and producttype in ("+StringUtils.join(productTypeFields, ',')+")";
        }

        //Integer startMonth = researchCycleReportFilters.getStartMonth();
        //Integer startYear = researchCycleReportFilters.getStartYear();
        //Integer endMonth = researchCycleReportFilters.getEndMonth();
        //Integer endYear = researchCycleReportFilters.getEndYear();
        /*	if(startMonth != null && startMonth >= 0)
                   {
                       if(flag)
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " where startMonth >="+startMonth;
                           flag = false;
                       }
                       else
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " and startMonth >="+startMonth;
                       }
                   }
                   if(startYear != null && startYear >= 0)
                   {
                       if(flag)
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " where startYear >="+startYear;
                           flag = false;
                       }
                       else
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " and startYear >="+startYear;
                       }
                   }
                   if(endMonth != null && endMonth >= 0)
                   {
                       if(flag)
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " where endMonth <="+endMonth;
                           flag = false;
                       }
                       else
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " and endMonth <="+endMonth;
                       }
                   }
                   if(endYear != null && endYear >= 0)
                   {
                       if(flag)
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " where endYear <="+endYear;
                           flag = false;
                       }
                       else
                       {
                           LOAD_PROJECTS = LOAD_PROJECTS + " and endYear <="+endYear;
                       }
                   }



                      flag = false;*/
        return LOAD_PROJECTS;
    }

    @Override
    public List<SpendReportExtractBean> getSpendReport(final SpendReportExtractFilter filter, final User user) {
        List<SpendReportExtractBean> spendReportExtractBeans = Collections.emptyList();
        String sql = null;
        Integer reportType = filter.getReportType();
        Quarter quarter1 = QuarterRangeUtil.getQuarter(1);
        Quarter quarter2 = QuarterRangeUtil.getQuarter(2);
        Quarter quarter3 = QuarterRangeUtil.getQuarter(3);
        Quarter quarter4 = QuarterRangeUtil.getQuarter(4);
        if(reportType.equals(SynchroGlobal.SpendReportType.TOTAL_SPEND.getId())) {
            sql = generateTotalSpendReportSQL(filter, user, quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_METHODOLOGY.getId())) {
            sql = generateSpendByMethodologySQL(filter, user, quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_BRANDED_NON_BRANDED.getId())) {
            sql = generateSpendByBrandedNonBrandedSQL(filter, user, quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_SUPPLIER_GROUP.getId())) {
            sql = generateSpendBySupplierGroupSQL(filter, user, quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_PROJECTS.getId())) {
            sql = generateSpendByProjectsSQL(filter, user, quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_NON_BRANDED_PROJECTS.getId())) {
            sql = generateSpendByNonBrandedProjectsSQL(filter, user, quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB1_PROJECTS.getId())) {
            sql = generateSpendByGDB1ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB2_PROJECTS.getId())) {
            sql = generateSpendByGDB2ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB3_PROJECTS.getId())) {
            sql = generateSpendByGDB3ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB4_PROJECTS.getId())) {
            sql = generateSpendByGDB4ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB5_PROJECTS.getId())) {
            sql = generateSpendByGDB5ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB6_PROJECTS.getId())) {
            sql = generateSpendByGDB6ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_GDB7_PROJECTS.getId())) {
            sql = generateSpendByGDB7ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_UPT_PROJECTS.getId())) {
            sql = generateSpendByUPTProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_BPT_PROJECTS.getId())) {
            sql = generateSpendByBPTProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_CAP1_PROJECTS.getId())) {
            sql = generateSpendByCAP1ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_CAP2_PROJECTS.getId())) {
            sql = generateSpendByCAP2ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        } else if(reportType.equals(SynchroGlobal.SpendReportType.SPEND_BY_CAP3_PROJECTS.getId())) {
            sql = generateSpendByCAP3ProjectsSQL(filter, user,  quarter1, quarter2, quarter3, quarter4);
        }

        if(sql != null) {
            try {
                spendReportExtractBeans = getJdbcTemplate().query(sql, spendReportReportParameterizedRowMapper);
            } catch (DataAccessException e) {
                e.printStackTrace();
            }
        }
        return spendReportExtractBeans;
    }


    private final ParameterizedRowMapper<SpendReportExtractBean> spendReportReportParameterizedRowMapper = new ParameterizedRowMapper<SpendReportExtractBean>() {
        public SpendReportExtractBean mapRow(ResultSet rs, int row) throws SQLException {

            SpendReportExtractBean spendReportExtractBean = new SpendReportExtractBean();
            spendReportExtractBean.setReportLabel(rs.getString("reportLabel"));
            spendReportExtractBean.setQuarter1(rs.getBigDecimal("quarter1"));
            spendReportExtractBean.setQuarter2(rs.getBigDecimal("quarter2"));
            spendReportExtractBean.setQuarter3(rs.getBigDecimal("quarter3"));
            spendReportExtractBean.setQuarter4(rs.getBigDecimal("quarter4"));
            spendReportExtractBean.setOrder(rs.getInt("order"));
            spendReportExtractBean.setProjectId(rs.getLong("projectId"));
            return spendReportExtractBean;
        }
    };

    @Override
    public BigDecimal getMethodologyTotalSpend(final SpendReportExtractFilter filter, final User user, final Quarter quarter) {
        BigDecimal total = new BigDecimal(0);
        String filterString  = getSpendReportFilter(filter, user);

        String sql = "select sum((pcf.latestprojectcost * (CASE WHEN (pcf.latestprojectcostcurrency > 0) THEN (SELECT ger.exchangerate FROM grailcurrencyexchangerate ger WHERE " +
                "ger.currencyid=pcf.latestprojectcostcurrency AND ger.modificationdate = (SELECT MAX(ger1.modificationdate) FROM grailcurrencyexchangerate ger1 WHERE ger1.currencyid = ger.currencyid)) ELSE (1.0) END))" +
                " + " +
                "(pcf.tenderingcost * (CASE WHEN (pcf.tenderingcostcurrency > 0)  THEN " +
                "(SELECT ger2.exchangerate FROM grailcurrencyexchangerate ger2 WHERE ger2.currencyid=pcf.tenderingcostcurrency AND ger2.modificationdate = (SELECT MAX(ger3.modificationdate) FROM grailcurrencyexchangerate ger3 WHERE ger3.currencyid = ger2.currencyid)) ELSE (1.0) END))) " +
                "from grailprojectcostfields pcf where pcf.id in " +
                "(SELECT pcf1.id FROM grailprojectcostfields pcf1, grailproject p,grailmethodologyfields m WHERE p.projectid = pcf1.projectid AND m.id = ANY(('{' || p.proposedmethodology || '}')::int[]) " +
                " group by pcf1.projectid,pcf1.endmarketid,pcf1.investmenttype,pcf1.id) AND "+filterString+ " AND (to_char(to_timestamp(pcf.updateddatetime/1000),'mm')::int BETWEEN ? AND ?) ";
        try {
            total = getJdbcTemplate().queryForObject(sql, BigDecimal.class, quarter.getStartMonth(), quarter.getEndMonth());
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        if(total == null) {
            return new BigDecimal(0);
        } else {
            return total;
        }
    }

    private String generateTotalSpendReportSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();

        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);

        Map<String, String> userProperties = user.getProperties();


        // Global Costs SQL Builder
        StringBuilder globalSpendSQLBuilder = new StringBuilder();
        if(isGlobalSuperUser(user)) {
            globalSpendSQLBuilder.append("(SELECT 'Global' as reportLabel,1 as order,")
                    .append(quarter1SQL).append(" as quarter1").append(",")
                    .append(quarter2SQL).append(" as quarter2").append(",")
                    .append(quarter3SQL).append(" as quarter3").append(",")
                    .append(quarter4SQL).append(" as quarter4")
                    .append(" FROM grailprojectcostfields pcf");

            String filterString  = getSpendReportFilter(filter, user);
            if(filterString.length() > 0) {
                globalSpendSQLBuilder.append(" WHERE ").append(filterString);
            }
            globalSpendSQLBuilder.append(")");
        }

        // Regional Costs SQL Builder

        String yearFilter = getYearFilter(filter);

        String yearFilterStr = (yearFilter != null && !yearFilter.equals(""))?(" AND "+yearFilter):"";

        String statusFilter = getProjectStatusFilter(filter);


        StringBuilder regionalSpendSQLBuilder = new StringBuilder();
        List<Long> regionAccessList = new LinkedList<Long>();
        List<Long> selectedRegions = new LinkedList<Long>();

        if(filter.getRegions() != null && filter.getRegions().size() > 0) {
            selectedRegions = filter.getRegions();
        }

        if(!isGlobalSuperUser(user)) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER)
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER).equals("")) {
                String regionalAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST);
                if(regionalAccessListStr != null && !regionalAccessListStr.equals("")) {
                    String [] regionalAccessListArr = regionalAccessListStr.split(",");
                    for(String regionIdStr : regionalAccessListArr) {
                        Long regionId = Long.parseLong(regionIdStr);
                        if(selectedRegions.size() > 0) {
                            if(selectedRegions.contains(regionId)) {
                                regionAccessList.add(regionId);
                            }
                        } else {
                            regionAccessList.add(regionId);
                        }
                    }

                } else {
                    regionAccessList = selectedRegions;
                }
            } else {
                regionAccessList = selectedRegions;
            }
        } else {
            regionAccessList = selectedRegions;
        }

        regionalSpendSQLBuilder.append("(SELECT rf.name as reportLabel,2 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")

                .append(" FROM grailprojectcostfields pcf,grailregionfields rf")
                .append(" WHERE rf.isactive = 1 AND (" +
                        " (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.REGION.getId()+" AND pcf.endmarketid = rf.id)" +
                        " OR (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.AREA.getId()+" AND pcf.endmarketid = (select garm.areaid from grailareafieldmappingfields garm where garm.areaid = pcf.endmarketid AND garm.endmarketid in (SELECT grm.endmarketid from grailregionfieldmappingfields grm WHERE grm.regionid = rf.id AND (select count(*) from grailendmarketfields em where em.isactive = 1 AND em.id = grm.endmarketid) > 0 group by grm.endmarketid) group by garm.areaid))" +
                        " OR (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.COUNTRY.getId()+" AND pcf.endmarketid in (SELECT grm.endmarketid from grailregionfieldmappingfields grm WHERE grm.regionid = rf.id AND (select count(*) from grailendmarketfields em where em.isactive = 1 AND em.id = grm.endmarketid) > 0 group by grm.endmarketid))" +
                        ")")
                .append(" AND ").append(statusFilter).append(yearFilterStr);
//                .append(" FROM grailregionfields rf ")
//                .append(" LEFT JOIN grailregionfieldmappingfields rem ON (rf.isactive = 1 AND rem.regionid = rf.id)")
//                .append(" LEFT JOIN grailprojectcostfields pcf ON (" + statusFilter + yearFilterStr +" )")
//                .append(" WHERE rf.isactive = 1 AND ((pcf.investmenttype = "+ SynchroGlobal.InvestmentType.REGION.getId()+" AND pcf.endmarketid = rem.regionid) " +
//                        "OR (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.AREA.getId()+" AND pcf.endmarketid in (select garm.areaid from grailareafieldmappingfields garm where garm.endmarketid = rem.endmarketid AND (SELECT count(*) FROM grailareafields af WHERE af.id = pcf.endmarketid AND af.isactive = 1) > 0))"+
//                        "OR (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.COUNTRY.getId()+" AND pcf.endmarketid = rem.endmarketid AND (select count(*) from grailendmarketfields em where em.isactive = 1 AND em.id = pcf.endmarketid) > 0))");
        if(regionAccessList.size() > 0) {
            regionalSpendSQLBuilder.append(" AND rf.id in ("+StringUtils.join(regionAccessList, ",")+")");
        } else if(!isGlobalSuperUser(user)) {
            regionalSpendSQLBuilder.append(" AND 1 = 0");
        }

        if(filter != null && filter.getMarketTypes() != null && filter.getMarketTypes().size() > 0) {
            regionalSpendSQLBuilder.append(" AND pcf.ismultimarket in (").append(StringUtils.join(filter.getMarketTypes(), ",")).append(")");
        }

        regionalSpendSQLBuilder.append(" group by rf.id, rf.name)");




        // Endmarket Costs SQL Builder
        StringBuilder endmarketSpendSQLBuilder = new StringBuilder();
        List<Long> endmarketAccessList = new LinkedList<Long>();
        List<Long> selectedEndmarkets = new LinkedList<Long>();

        if(filter.getCountries() != null && filter.getCountries().size() > 0) {
            selectedEndmarkets = filter.getCountries();
        }

        if(!isGlobalSuperUser(user)) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                if(endmarketAccessListStr != null && !endmarketAccessList.equals("")) {
                    String [] endmarketAccessListArr = endmarketAccessListStr.split(",");
                    for(String endmarketIdStr : endmarketAccessListArr) {
                        Long endmarketId = Long.parseLong(endmarketIdStr);
                        if(selectedEndmarkets.size() > 0) {
                            if(selectedEndmarkets.contains(endmarketId)) {
                                endmarketAccessList.add(endmarketId);
                            }
                        }  else {
                            endmarketAccessList.add(endmarketId);
                        }
                    }

                } else {
                    endmarketAccessList = selectedEndmarkets;
                }
            } else {
                endmarketAccessList = selectedEndmarkets;
            }
        } else {
            endmarketAccessList = selectedEndmarkets;

        }

        endmarketSpendSQLBuilder.append("(SELECT emf.name as reportLabel,3 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailendmarketfields emf ")
                .append(" LEFT JOIN grailprojectcostfields pcf ON (" + statusFilter + yearFilterStr+")")
                .append(" WHERE emf.isactive = 1 AND (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.COUNTRY.getId()+" AND emf.id = pcf.endmarketid)");

        if(endmarketAccessList.size() > 0) {
            endmarketSpendSQLBuilder.append(" AND emf.id in ("+StringUtils.join(endmarketAccessList, ",")+")");
        } else if(!isGlobalSuperUser(user)) {
            endmarketSpendSQLBuilder.append(" AND 1 = 0");
        }

        if(filter != null && filter.getMarketTypes() != null && filter.getMarketTypes().size() > 0) {
            endmarketSpendSQLBuilder.append(" AND pcf.ismultimarket in (").append(StringUtils.join(filter.getMarketTypes(), ",")).append(")");
        }

        endmarketSpendSQLBuilder.append(" group by emf.id,emf.name)");


        sqlBuilder.append("SELECT result.reportLabel as reportLabel, -1 as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
        sqlBuilder.append(" FROM (");

        // Global Result
        sqlBuilder.append(globalSpendSQLBuilder.toString());

        // Regional Result
        if(regionalSpendSQLBuilder.length() > 0) {
            if(globalSpendSQLBuilder.length() > 0) {
                sqlBuilder.append(" UNION ");
            }
            sqlBuilder.append(regionalSpendSQLBuilder.toString());
        }
        // Endmarket result
        if(endmarketSpendSQLBuilder.length() > 0) {
            if(globalSpendSQLBuilder.length() > 0 || regionalSpendSQLBuilder.length() > 0) {
                sqlBuilder.append(" UNION ");
            }
            sqlBuilder.append(endmarketSpendSQLBuilder.toString());
        }

        sqlBuilder.append(") as result order by result.order,result.reportLabel");

        return sqlBuilder.toString();
    }

    private boolean isGlobalSuperUser(final User user) {
        boolean superUser = false;
        if(SynchroPermHelper.isSynchroAdmin(user)
                || SynchroPermHelper.isSynchroMiniAdmin(user)
                || SynchroPermHelper.isSynchroGlobalSuperUser(user)) {
            superUser = true;
        }
        return superUser;
    }

    private List<Long> getEndmarketIdsByFilter(final SpendReportExtractFilter filter) {
        List<Long> endmarketIds = new ArrayList<Long>();
//
        if(filter.getCountries() != null && filter.getCountries().size() > 0) {
            endmarketIds.addAll(filter.getCountries());
        }

        if(filter.getRegions() != null && filter.getRegions().size() > 0) {
            for(Long regionId : filter.getRegions()) {
                List<Long> eMids = getEndmarketIdsByRegion(regionId);
                if(eMids != null && eMids.size() > 0) {
                    endmarketIds.addAll(eMids);
                }
            }
        }
        return endmarketIds;
    }

    private String generateSpendByMethodologySQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();

        StringBuilder methodologySpendSQLQueries = new StringBuilder();

        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);
        String filterString = getSpendReportFilter(filter, user);
        methodologySpendSQLQueries.append("(SELECT m.name as reportLabel,1 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailmethodologyfields m,grailproject p")
                .append(" LEFT JOIN grailprojectcostfields AS pcf ON (p.projectid = pcf.projectid) WHERE (m.id = ANY(('{' || p.proposedmethodology || '}')::int[])) AND "+filterString);

        methodologySpendSQLQueries.append(" group by m.id,m.name)");

        if(methodologySpendSQLQueries.length() > 0) {
            sqlBuilder.append("SELECT result.reportLabel as reportLabel, -1 as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
            sqlBuilder.append(" FROM (").append(methodologySpendSQLQueries).append(") as result order by result.reportLabel");
        }
        return sqlBuilder.toString();
    }

    private String generateSpendByBrandedNonBrandedSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();
        Map<Integer, String> brandFields = SynchroUtils.getBrandFields();
        Integer nonBrandedId = getNonBrandedId();
        List<Integer> brandedIds = getBrandedIds();
        StringBuilder nonBrandedSQLBuilder = new StringBuilder();
        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);

        String filterString = getSpendReportFilter(filter, user);


        //String nonBrandedWhere = "(pcf.projectid = (SELECT p.projectid FROM grailproject p where p.projectid = pcf.projectid AND p.brand = "+nonBrandedId+"))";
        nonBrandedSQLBuilder.append("(SELECT 'Non-Branded' as reportLabel,1 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailbrandfields br")
                .append(" LEFT JOIN grailproject p ON (br.id = p.brand)")
                .append(" LEFT JOIN grailprojectcostfields pcf ON (p.projectid = pcf.projectid) WHERE br.id = "+nonBrandedId+" AND "+filterString);

        nonBrandedSQLBuilder.append(")");

        StringBuilder brandedSQLBuilder = new StringBuilder();
        //String brandedWhere = "(pcf.projectid = (SELECT p.projectid FROM grailproject p where p.projectid = pcf.projectid AND p.brand in ("+StringUtils.join(brandedIds,",")+")))";
        brandedSQLBuilder.append("(SELECT 'Branded' as reportLabel,2 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailbrandfields br")
                .append(" LEFT JOIN grailproject p ON (br.id = p.brand)")
                .append(" LEFT JOIN grailprojectcostfields pcf ON (p.projectid = pcf.projectid) WHERE br.id in ("+StringUtils.join(brandedIds,",")+") AND "+filterString);

        brandedSQLBuilder.append(")");

        //List<String> brandedQueries = new ArrayList<String>();
        StringBuilder brandedTypeSQLBuilder = new StringBuilder();
        if(brandedIds.size() > 0) {
            brandedTypeSQLBuilder.append("(SELECT br.name as reportLabel,3 as order,")
                    .append(quarter1SQL).append(" as quarter1").append(",")
                    .append(quarter2SQL).append(" as quarter2").append(",")
                    .append(quarter3SQL).append(" as quarter3").append(",")
                    .append(quarter4SQL).append(" as quarter4")
                    .append(" FROM grailbrandfields br, grailproject p")
                    .append(" LEFT JOIN grailprojectcostfields pcf ON (p.projectid = pcf.projectid) WHERE br.id = p.brand AND br.id in ("+StringUtils.join(brandedIds,",")+") AND "+filterString);

            brandedTypeSQLBuilder.append(" group by br.id,br.name)");
        }

        sqlBuilder.append("SELECT result.reportLabel as reportLabel, -1 as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
        sqlBuilder.append(" FROM (");

        // Non-Branded Result
        sqlBuilder.append(nonBrandedSQLBuilder.toString());

        // Branded Result
        if(brandedSQLBuilder.length() > 0) {
            sqlBuilder.append(" UNION ");
            sqlBuilder.append(brandedSQLBuilder.toString());
        }

        if(brandedTypeSQLBuilder.length() > 0) {
            sqlBuilder.append(" UNION ");
            sqlBuilder.append(brandedTypeSQLBuilder.toString());
        }

        sqlBuilder.append(") as result order by result.order,result.reportLabel");

        return sqlBuilder.toString();
    }


    private String generateSpendBySupplierGroupSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();

        StringBuilder supplierGroupSQLBuilder = new StringBuilder();

        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);

        String filterString = getSpendReportFilter(filter, user);

        supplierGroupSQLBuilder
                .append("(SELECT gud.name as reportLabel,1 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailuserdepartments gud")
                .append(" LEFT JOIN grailprojectcostfields pcf ON (gud.id = (SELECT p.agencyDept FROM grailproject p WHERE p.projectid = pcf.projectid)) WHERE gud.id > 0 AND ( "+filterString+" )");
                //.append(" LEFT JOIN jiveuserprofile jup ON (gud.id::text = jup.value AND jup.fieldid = 2)")
                //.append(" LEFT JOIN grailprojectcostfields pcf ON (pcf.awardedagency > 0 AND jup.userid = pcf.awardedagency) WHERE gud.id > 0 AND ( "+filterString+" )");

        supplierGroupSQLBuilder.append(" group by gud.id,gud.name)");

        if(supplierGroupSQLBuilder.length() > 0) {
            sqlBuilder.append("SELECT result.reportLabel as reportLabel, -1 as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
            sqlBuilder.append(" FROM (");

            sqlBuilder.append(supplierGroupSQLBuilder.toString());

            sqlBuilder.append(") as result order by result.reportLabel");
        }


        return sqlBuilder.toString();
    }

    private String generateSpendByProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder projectsSQLBuilder = new StringBuilder();

        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);

        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        if(filter.getYears() != null && filter.getYears().size() > 0) {
            year = filter.getYears().get(0);
        }

        projectsSQLBuilder
                .append("(SELECT  p.name as name, "+getProjectNameSelectField()+" as reportLabel,p.projectid as projectid,pcf.investmenttype as investmentType,1 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailprojectcostfields pcf, grailproject p where p.projectid = pcf.projectid")
                .append(" AND (to_char(to_timestamp(pcf.updateddatetime/1000),'yyyy')::int = "+year+")");
        String filterString = getSpendReportFilter(filter, user);
        if(filterString.length() > 0) {
            projectsSQLBuilder.append(" AND ").append(filterString);
        }
        projectsSQLBuilder.append(" group by p.projectid, pcf.endmarketid,pcf.investmenttype, p.name)");

        sqlBuilder.append("SELECT result.reportLabel as reportLabel,result.projectid as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
        sqlBuilder.append(" FROM (");

        sqlBuilder.append(projectsSQLBuilder.toString());

        sqlBuilder.append(") as result order by result.projectid, result.name, result.investmentType, result.reportLabel");
        return sqlBuilder.toString();
    }


    private String generateSpendByNonBrandedProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.NON_BRANDED.getName(), quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByGDB1ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.DUNHILL.getName(), quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByGDB2ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.KENT.getName(), quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByGDB3ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.LUCKY_STRIKE.getName(), quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByGDB4ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.PALL_MALL.getName(), quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByGDB5ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.ROTHMANS.getName(), quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByGDB6ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.VICEROY.getName(), quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByGDB7ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByBrand(filter, user, SynchroGlobal.SpendReportBrandType.VOGUE.getName(), quarter1, quarter2, quarter3, quarter4);
    }


    private String generateProjectsSQLByBrand(final SpendReportExtractFilter filter, final User user, final String brandName, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();
        Integer brandId = SynchroUtils.getBrandId(brandName);

        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);

        StringBuilder brandedSQLBuilder = new StringBuilder();
        String where = "(p.brand = "+brandId+")";
        brandedSQLBuilder
                .append("(SELECT  p.name as name, "+getProjectNameSelectField()+" as reportLabel,p.projectid as projectid,pcf.investmenttype as investmentType,1 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailprojectcostfields pcf, grailproject p where p.projectid = pcf.projectid and p.brand = "+brandId);

        String filterString = getSpendReportFilter(filter, user);
        if(filterString.length() > 0) {
            brandedSQLBuilder.append(" AND ").append(filterString);
        }
        brandedSQLBuilder.append(" group by p.projectid, pcf.endmarketid,pcf.investmenttype, p.name)");

        sqlBuilder.append("SELECT result.reportLabel as reportLabel,result.projectid as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
        sqlBuilder.append(" FROM (");

        sqlBuilder.append(brandedSQLBuilder.toString());

        sqlBuilder.append(") as result order by result.projectid, result.name, result.investmentType, result.reportLabel");

        return sqlBuilder.toString();
    }

    private String generateSpendByUPTProjectsSQL(final SpendReportExtractFilter filter, final User user,  final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateSpendByBPTProjectsSQLByProductTest(filter, user, "Unbranded", quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByBPTProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateSpendByBPTProjectsSQLByProductTest(filter, user, "Branded", quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByBPTProjectsSQLByProductTest(final SpendReportExtractFilter filter, final User user, final String type, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder capSQLBuilder = new StringBuilder();
        String where = "";

        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);

        if(type.equals("Branded")) {
            where = "(((SELECT mf.id FROM grailMethodologyFields mf where mf.name = '"+SynchroGlobal.SpendReportMethodologyType.BPT.getName()+"') = ANY(('{' || p.proposedmethodology || '}')::int[])) OR ((SELECT mf.id FROM grailMethodologyFields mf where mf.name = '"+SynchroGlobal.SpendReportMethodologyType.BG4S.getName()+"') = ANY(('{' || p.proposedmethodology || '}')::int[])))";
        } else if(type.equals("Unbranded")) {
            where = "(((SELECT mf.id FROM grailMethodologyFields mf where mf.name = '"+SynchroGlobal.SpendReportMethodologyType.UPT.getName()+"') = ANY(('{' || p.proposedmethodology || '}')::int[])) OR ((SELECT mf.id FROM grailMethodologyFields mf where mf.name = '"+SynchroGlobal.SpendReportMethodologyType.UG4S.getName()+"') = ANY(('{' || p.proposedmethodology || '}')::int[])))";
        }

        capSQLBuilder.append("(SELECT  p.name as name, "+getProjectNameSelectField()+" as reportLabel,p.projectid as projectid,pcf.investmenttype as investmentType,1 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailprojectcostfields pcf, grailproject p where p.projectid = pcf.projectid AND "+where);

        String filterString = getSpendReportFilter(filter, user);
        if(filterString.length() > 0) {
            capSQLBuilder.append(" AND ").append(filterString);
        }
        capSQLBuilder.append(" group by p.projectid, pcf.endmarketid,pcf.investmenttype, p.name)");

        sqlBuilder.append("SELECT result.reportLabel as reportLabel,result.projectid as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
        sqlBuilder.append(" FROM (");

        sqlBuilder.append(capSQLBuilder.toString());

        sqlBuilder.append(") as result order by result.projectid, result.name, result.investmentType, result.reportLabel");

        return sqlBuilder.toString();
    }

    private String generateSpendByCAP1ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByCAPRating(filter, user, 1, quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByCAP2ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByCAPRating(filter, user,  2, quarter1, quarter2, quarter3, quarter4);
    }

    private String generateSpendByCAP3ProjectsSQL(final SpendReportExtractFilter filter, final User user, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        return generateProjectsSQLByCAPRating(filter, user, 3, quarter1, quarter2, quarter3, quarter4);
    }

    private String generateProjectsSQLByCAPRating(final SpendReportExtractFilter filter, final User user, final Integer capRating, final Quarter quarter1, final Quarter quarter2, final Quarter quarter3, final Quarter quarter4) {
        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder capSQLBuilder = new StringBuilder();
        String quarter1SQL = getProjectCostSQL(quarter1, filter);
        String quarter2SQL = getProjectCostSQL(quarter2, filter);
        String quarter3SQL = getProjectCostSQL(quarter3, filter);
        String quarter4SQL = getProjectCostSQL(quarter4, filter);

        String where = "(p.caprating = "+capRating+")";
        capSQLBuilder
                .append("(SELECT  p.name as name, "+getProjectNameSelectField()+" as reportLabel,p.projectid as projectid,pcf.investmenttype as investmentType,1 as order,")
                .append(quarter1SQL).append(" as quarter1").append(",")
                .append(quarter2SQL).append(" as quarter2").append(",")
                .append(quarter3SQL).append(" as quarter3").append(",")
                .append(quarter4SQL).append(" as quarter4")
                .append(" FROM grailprojectcostfields pcf, grailproject p where p.projectid = pcf.projectid AND p.caprating = "+capRating);

        String filterString = getSpendReportFilter(filter, user);
        if(filterString.length() > 0) {
            capSQLBuilder.append(" AND ").append(filterString);
        }
        capSQLBuilder.append(" group by p.projectid, pcf.endmarketid,pcf.investmenttype, p.name)");

        sqlBuilder.append("SELECT result.reportLabel as reportLabel,result.projectid as projectId, result.quarter1 as quarter1, result.quarter2 as quarter2, result.quarter3 as quarter3, result.quarter4 as quarter4,result.order as order");
        sqlBuilder.append(" FROM (");

        sqlBuilder.append(capSQLBuilder.toString());

        sqlBuilder.append(") as result order by result.projectid, result.name, result.investmentType, result.reportLabel");

        return sqlBuilder.toString();
    }

    private String getProjectCostSQL(final Quarter quarter, final SpendReportExtractFilter filter) {
        StringBuilder projectCostSQLBuilder = new StringBuilder();
//        projectCostSQLBuilder.append("SUM").append("(CASE WHEN ((to_char(to_timestamp(pcf.capturedDateTime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth() + ") AND pcf.capturedDateTime = (SELECT MAX(pcf3.capturedDateTime) FROM grailprojectcostfields pcf3 WHERE pcf3.projectid = pcf.projectid AND pcf3.endmarketid = pcf.endmarketid AND pcf3.investmenttype = pcf.investmenttype AND (to_char(to_timestamp(pcf3.capturedDateTime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth() + ")))")
//                .append(" THEN ")
//                .append("(SELECT (" +
//                        "(pcf1.latestprojectcost * (CASE WHEN (pcf1.latestprojectcostcurrency > 0) THEN (SELECT ger.exchangerate FROM grailcurrencyexchangerate ger WHERE ger.currencyid=pcf1.latestprojectcostcurrency AND ger.modificationdate = (SELECT MAX(ger1.modificationdate) FROM grailcurrencyexchangerate ger1 WHERE ger1.currencyid = ger.currencyid)) ELSE (1.0) END))" +
//                        " + " +
//                        "(pcf1.tenderingcost * (CASE WHEN (pcf1.tenderingcostcurrency > 0) THEN (SELECT ger2.exchangerate FROM grailcurrencyexchangerate ger2 WHERE ger2.currencyid=pcf1.tenderingcostcurrency AND ger2.modificationdate = (SELECT MAX(ger3.modificationdate) FROM grailcurrencyexchangerate ger3 WHERE ger3.currencyid = ger2.currencyid)) ELSE (1.0) END))" +
//                        ")" +
//                        " FROM grailprojectcostfields pcf1 WHERE pcf.projectid = pcf1.projectid AND pcf.endmarketid = pcf1.endmarketid AND pcf.investmenttype = pcf1.investmenttype AND (to_char(to_timestamp(pcf1.capturedDateTime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth() + ") AND pcf1.capturedDateTime = (SELECT MAX(pcf2.capturedDateTime) FROM grailprojectcostfields pcf2 WHERE pcf2.projectid = pcf1.projectid AND pcf2.endmarketid = pcf1.endmarketid AND pcf2.investmenttype = pcf1.investmenttype AND (to_char(to_timestamp(pcf2.capturedDateTime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth() + ")) ORDER BY pcf1.capturedDateTime DESC LIMIT 1)")
//                .append(" ELSE (0.0) ")
//                .append("END)");
        Calendar calendar = Calendar.getInstance();

        Integer year = calendar.get(Calendar.YEAR);
        if(filter.getYears() != null && filter.getYears().size() > 0) {
            year = filter.getYears().get(0);
        }
//        filterBuilder.append(" AND ((to_char(to_timestamp(pcf.captureddatetime/1000),'yyyy')::int) = "+year+")");


        projectCostSQLBuilder.append("SUM((CASE " +
                "WHEN ((to_char(to_timestamp(pcf.updateddatetime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth() + ") AND (to_char(to_timestamp(pcf.updateddatetime/1000),'yyyy')::int) = "+year+" AND (pcf.id = (SELECT pcf3.id FROM grailprojectcostfields pcf3 WHERE pcf3.projectid = pcf.projectid AND pcf3.endmarketid = pcf.endmarketid AND pcf3.investmenttype = pcf.investmenttype AND (to_char(to_timestamp(pcf3.updateddatetime/1000),'mm')::int BETWEEN "+quarter.getStartMonth()+" AND "+quarter.getEndMonth() + ") AND (to_char(to_timestamp(pcf.updateddatetime/1000),'yyyy')::int) = "+year+" order by pcf3.id desc OFFSET 0 LIMIT 1)))" +
                "THEN (" +
                "(pcf.latestprojectcost * (CASE WHEN (pcf.latestprojectcostcurrency > 0) " +
                "THEN (SELECT ger.exchangerate FROM grailcurrencyexchangerate ger WHERE ger.currencyid=pcf.latestprojectcostcurrency AND ger.modificationdate = (SELECT MAX(ger1.modificationdate) FROM grailcurrencyexchangerate ger1 WHERE ger1.currencyid = ger.currencyid)) " +
                "ELSE (1.0) END))" +
                " + " +
                "(pcf.tenderingcost * (CASE WHEN (pcf.tenderingcostcurrency > 0) " +
                "THEN (SELECT ger2.exchangerate FROM grailcurrencyexchangerate ger2 WHERE ger2.currencyid=pcf.tenderingcostcurrency AND ger2.modificationdate = (SELECT MAX(ger3.modificationdate) FROM grailcurrencyexchangerate ger3 WHERE ger3.currencyid = ger2.currencyid)) " +
                "ELSE (1.0) END))" +
                ")" +
                "ELSE (0)" +
                " END))");
        return projectCostSQLBuilder.toString();
    }

    private String getProjectNameSelectField() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        stringBuilder.append("p.name");
        //stringBuilder.append(" || ' (' || ").append("p.projectid").append(" || ')'");
        stringBuilder.append(" || ' - ' || ").append("(CASE ")
                .append(" WHEN (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.GlOBAL.getId()+")").append(" THEN ").append("'Global'")
                .append(" WHEN (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.REGION.getId()+")").append(" THEN ").append("(SELECT rg.name from grailregionfields rg where rg.id = pcf.endmarketid)")
                .append(" WHEN (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.AREA.getId()+")").append(" THEN ").append("(SELECT ar.name from grailareafields ar where ar.id = pcf.endmarketid)")
                .append(" WHEN (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.COUNTRY.getId()+")").append(" THEN ").append("(SELECT em.name from grailendmarketfields em where em.id = pcf.endmarketid)")
                .append(" ELSE 'NONE' END)");
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private String getSpendReportFilter(final SpendReportExtractFilter filter, final User user) {
        StringBuilder filterBuilder = new StringBuilder();

//        filterBuilder.append("(pcf.capturedDateTime = (SELECT MAX(pcf1.capturedDateTime) FROM grailprojectcostfields pcf1 WHERE pcf1.projectid = pcf.projectid " +
//                "AND pcf1.endmarketid = pcf.endmarketid " +
//                "AND pcf1.investmenttype = pcf.investmenttype "+
//                " group by pcf1.projectid,pcf1.investmenttype, pcf1.endmarketid))");

        //filterBuilder.append(" AND ").append(getProjectStatusFilter(filter));

        filterBuilder.append(getProjectStatusFilter(filter));

//        Calendar calendar = Calendar.getInstance();
//        Integer year = calendar.get(Calendar.YEAR);
//        filterBuilder.append(" AND ((to_char(to_timestamp(pcf.captureddatetime/1000),'yyyy')::int) = "+year+")");

        filterBuilder.append(" AND ((pcf.investmentType = " + SynchroGlobal.InvestmentType.GlOBAL.getId()+")" +
                " OR (pcf.investmentType = " + SynchroGlobal.InvestmentType.COUNTRY.getId() +" AND pcf.endmarketid = (SELECT em.id FROM grailendmarketfields em WHERE em.id = pcf.endmarketid AND em.isactive = 1 group by em.id))" +
                " OR (pcf.investmentType = " + SynchroGlobal.InvestmentType.AREA.getId() +" AND pcf.endmarketid = (SELECT af.id FROM grailareafields af WHERE af.id = pcf.endmarketid AND af.isactive = 1 group by af.id))" +
                " OR (pcf.investmentType = " + SynchroGlobal.InvestmentType.REGION.getId() +" AND pcf.endmarketid = (SELECT rf.id FROM grailregionfields rf WHERE rf.id = pcf.endmarketid AND rf.isactive = 1 group by rf.id)))");

        if(filter != null && filter.getMarketTypes() != null && filter.getMarketTypes().size() > 0) {
            filterBuilder.append(" AND pcf.ismultimarket in (").append(StringUtils.join(filter.getMarketTypes(), ",")).append(")");
        }

        String yearFilter = getYearFilter(filter);
        if(yearFilter != null && !yearFilter.equals("")) {
            if(filterBuilder.length() > 0) {
                filterBuilder.append(" AND ");
            }
            filterBuilder.append(yearFilter);
        }
//        if(!isGlobalSuperUser(user)) {
        List<String> regionEndmarketFilters = new ArrayList<String>();

        String regionFilter = getRegionFilter(filter, user, new LinkedList<Long>());
        if(regionFilter != null && !regionFilter.equals("")) {
            regionEndmarketFilters.add(regionFilter);
        }

        String  endmarketFilter = getEndmarketFilter(filter, user, new LinkedList<Long>());
        if(endmarketFilter != null && !endmarketFilter.equals("")) {
            regionEndmarketFilters.add(endmarketFilter);
        }

        if(regionEndmarketFilters.size() > 0) {
            if(filterBuilder.length() > 0) {
                filterBuilder.append(" AND ");
            }
            filterBuilder.append("(").append(StringUtils.join(regionEndmarketFilters," OR ")).append(")");
        }
//        }

        return filterBuilder.toString();
    }


    private String getProjectStatusFilter(final SpendReportExtractFilter filter) {
        StringBuilder projectSatusFilterBuilder = new StringBuilder();
        projectSatusFilterBuilder.append("(CASE")
                .append(" WHEN (")
                        // Overall(global level) statues
                .append("((SELECT count(*) FROM grailprojectcostfields pcf1 WHERE pcf1.projectid = pcf.projectid AND pcf1.projectstatus in (" + SynchroGlobal.Status.DELETED.ordinal()
                        + ", " + SynchroGlobal.Status.PIT_CANCEL.ordinal()
                        + ", " + SynchroGlobal.Status.PIT_ONHOLD.ordinal()
                        + ", " + SynchroGlobal.Status.PIB_CANCEL.ordinal()
                        + ", " + SynchroGlobal.Status.PIB_ONHOLD.ordinal()
                        + ", " + SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()
                        + ", " + SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal() + ")"
                        + " GROUP BY pcf1.updateddatetime ORDER BY pcf1.updateddatetime desc LIMIT 1) > 0)")
//                .append("(pcf.projectstatus in (" + SynchroGlobal.Status.DELETED.ordinal()
//                        + ", " + SynchroGlobal.Status.PIT_CANCEL.ordinal()
//                        + ", " + SynchroGlobal.Status.PIT_ONHOLD.ordinal()
//                        + ", " + SynchroGlobal.Status.PIB_CANCEL.ordinal()
//                        + ", " + SynchroGlobal.Status.PIB_ONHOLD.ordinal()
//                        + ", " + SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()
//                        + ", " + SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal() + ")"
//                        + ")")
                .append(" OR ")
                        // Endmarket level statues
                .append("((SELECT count(*) from grailprojectstatus ps where ps.projectid = pcf.projectid" +
                        " AND (pcf.investmenttype = "+ SynchroGlobal.InvestmentType.COUNTRY.getId()+" AND ps.endmarketid = pcf.endmarketid)" +
                        " AND ps.status in ("+ SynchroGlobal.ProjectActivationStatus.DELETED.ordinal() + ", "
                        + SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() + ", "
                        + SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal()+")" +
                        ") > 0)")
                .append(")")
                .append(" THEN ").append("(1 = 0)")
                .append(" ELSE ").append("(1 = 1)")
                .append("END)");
        return projectSatusFilterBuilder.toString();
    }

    private String getYearFilter(final SpendReportExtractFilter filter) {
        if(filter.getYears() != null && filter.getYears().size() > 0) {
            //return "(to_char(to_timestamp(pcf.captureddatetime/1000),'yyyy')::int in ("+StringUtils.join(filter.getYears(), ",")+"))";
            return "((SELECT (CASE WHEN (pr.budgetyear is not null AND pr.budgetyear > 0) THEN pr.budgetyear ELSE (to_char(to_timestamp(pr.enddate/1000), 'YYYY')::int) END) FROM grailproject pr WHERE pr.projectid = pcf.projectid) in ("+StringUtils.join(filter.getYears(), ",")+"))";
        }
        return null;
    }

    private String getRegionFilter(final SpendReportExtractFilter filter, final User user, List<Long> regionList) {
        List<Long> regionAccessList = new LinkedList<Long>();
        List<Long> selectedRegions = new LinkedList<Long>();

        if(filter.getRegions() != null && filter.getRegions().size() > 0) {
            selectedRegions = filter.getRegions();
        }

        if(!isGlobalSuperUser(user)) {
            Map<String, String> userProperties = user.getProperties();
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER)
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER).equals("")) {
                String regionalAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST);
                if(regionalAccessListStr != null && !regionalAccessListStr.equals("")) {
                    String [] regionalAccessListArr = regionalAccessListStr.split(",");
                    for(String regionIdStr : regionalAccessListArr) {
                        Long regionId = Long.parseLong(regionIdStr);
                        if(selectedRegions.size() > 0) {
                            if(selectedRegions.contains(regionId)) {
                                regionAccessList.add(regionId);
                            }
                        } else {
                            regionAccessList.add(regionId);
                        }

                    }
                } else {
                    regionAccessList = selectedRegions;
                }
            } else {
                regionAccessList = selectedRegions;
            }
        } else {
            regionAccessList = selectedRegions;
        }

        if(regionAccessList != null && regionAccessList.size() > 0) {
            return "((select count(*) from grailregionfieldmappingfields grfm where grfm.regionid = pcf.endmarketid and pcf.investmenttype = "+SynchroGlobal.InvestmentType.REGION.getId()+" and grfm.regionid in ("+StringUtils.join(regionAccessList,",")+")) > 0)";
        } else if(!isGlobalSuperUser(user)) {
            return "1 = 0";
        }
        return null;
    }

    private String getEndmarketFilter(final SpendReportExtractFilter filter, final User user, final List<Long> endmarketList) {
        List<Long> endmarketAccessList = new LinkedList<Long>();
        List<Long> selectedEndmarkets = new LinkedList<Long>();

        if(filter.getCountries() != null && filter.getCountries().size() > 0) {
            selectedEndmarkets = filter.getCountries();
        }

        if(!isGlobalSuperUser(user)) {
            Map<String, String> userProperties = user.getProperties();
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                if(endmarketAccessListStr != null && !endmarketAccessList.equals("")) {
                    String [] endmarketAccessListArr = endmarketAccessListStr.split(",");
                    for(String endmarketIdStr : endmarketAccessListArr) {
                        Long endmarketId = Long.parseLong(endmarketIdStr);
                        if(selectedEndmarkets.size() > 0) {
                            if(selectedEndmarkets.contains(endmarketId)) {
                                endmarketAccessList.add(endmarketId);
                            }
                        }  else {
                            endmarketAccessList.add(endmarketId);
                        }
                    }
                } else {
                    endmarketAccessList = selectedEndmarkets;
                }
            } else {
                endmarketAccessList = selectedEndmarkets;
            }
        } else {
            endmarketAccessList = selectedEndmarkets;

        }

        if(endmarketAccessList != null && endmarketAccessList.size() > 0) {
            return "(pcf.investmenttype = "+SynchroGlobal.InvestmentType.COUNTRY.getId()+" and pcf.endmarketid in ("+StringUtils.join(endmarketAccessList,",")+"))";
        } else if(!isGlobalSuperUser(user)) {
            return "1 = 0";
        }

        return null;
    }


    private Integer getNonBrandedId() {
        return SynchroUtils.getBrandId(SynchroGlobal.SpendReportBrandType.NON_BRANDED.getName());
    }

    private List<Integer> getBrandedIds() {
        List<Integer> brandedIds = new ArrayList<Integer>();
        Map<Integer, String> brandFields = SynchroUtils.getBrandFields();
        Integer id = SynchroUtils.getBrandId(SynchroGlobal.SpendReportBrandType.NON_BRANDED.getName());
        for(Integer key : brandFields.keySet()) {
            if(id == null || !id.equals(key)) {
                brandedIds.add(key);
            }
        }
        return brandedIds;
    }

    private List<Long> getEndmarketIdsByRegion(Long regionId) {
        List<MetaField> endmarkets = SynchroUtils.getEndMarketsByRegion(regionId.intValue());
        List<Long> endmarketIds = new ArrayList<Long>();
        if(endmarkets != null && endmarkets.size() > 0) {
            for(MetaField endmarket: endmarkets) {
                endmarketIds.add(endmarket.getId());
            }
        }
        return endmarketIds;
    }




    public static UserManager getUserManager() {
        if(userManager == null) {
            return JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;

    }

    public static ProfileManager getProfileManager() {
        if(profileManager == null) {
            return JiveApplication.getContext().getSpringBean("profileManager");
        }
        return profileManager;

    }
}
