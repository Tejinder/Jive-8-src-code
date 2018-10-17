package com.grail.synchro.dao.impl;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostsBean;
import com.grail.synchro.beans.Quarter;
import com.grail.synchro.dao.ProjectCostsCaptureDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCostsCaptureDAOImpl extends JiveJdbcDaoSupport implements ProjectCostsCaptureDAO {

    private static Logger LOG = Logger.getLogger(ProjectCostsCaptureDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static final String PROJECT_FIELDS =  "projectID, name, description, categoryType, brand, methodologyType, " +
            " methodologyGroup, proposedMethodology, startDate, endDate, projectOwner, briefCreator, multiMarket, totalCost, totalCostCurrency, " +
            " creationby, modificationby, creationdate, modificationdate, status, caprating ";

    private static String GET_PROJECTS = "SELECT "+PROJECT_FIELDS+" FROM grailproject";
//            "WHERE projectid not in " +
//            "(SELECT pcf.projectid FROM grailprojectcostfields pcf" +
////            "AND pcf.projectStatus in " +
////            "("+ SynchroGlobal.Status.COMPLETED.ordinal()+"" +
////            ","+ SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal() +
////            ","+ SynchroGlobal.Status.DELETED.ordinal() +
////            ")
              //")";


    private static String INSERT_FIELDS = "projectId, endmarketId, ismultimarket, isAboveMarket, investmentType, spiContact, projectOwner, stage, projectStartDate, projectEndDate, " +
            "estimatedCost, estimatedCostCurrency, latestEstimatedCost, latestEstimatedCostCurrency, " +
            "agency1, agency1optional, agency2, agency2optional, agency3, agency3optional, agency1dept, agency2dept, agency3dept, awardedagency, "+
            "propTotalCost, propTotalCostCurrency, propInlMgmtCost, propInlMgmtCostCurrency, " +
            "propLocalMgmtCost, propLocalMgmtCostCurrency, propFieldworkCost, propFieldworkCostCurrency, " +
            "propOperHubCost, propOperHubCostCurrency, propOtherCost, propOtherCostCurrency, " +
            "psOriginalCost, psOriginalCostCurrency, psFinalCost, psFinalCostCurrency, totalProjectCost, totalProjectCostCurrency, " +
            "latestProjectCost, latestProjectCostCurrency,tenderingCost,tenderingCostCurrency, datacollection, capturedDateTime,updateddatetime, projectStatus";

    private static String INSERT_PROJECT_COST = "INSERT INTO grailprojectcostfields (id,"+INSERT_FIELDS+") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static String UPDATE_PROJECT_COST = "UPDATE grailprojectcostfields SET "+INSERT_FIELDS.replaceAll(",","=?,")+"=? WHERE id = ?";

    private static String GET_PROJECT_COSTS = "SELECT id,"+INSERT_FIELDS+" from grailprojectcostfields where projectid = ? " +
            "AND (to_char(to_timestamp(capturedDateTime/1000),'mm')::int BETWEEN ? AND ?) " +
            "AND (to_char(to_timestamp(capturedDateTime/1000),'YYYY')::int BETWEEN ? AND ?)";

    private static String GET_PROJECT_COSTS_BY_PROJECT_ENDMARKET = "SELECT id,"+INSERT_FIELDS+" from grailprojectcostfields where projectid = ? AND endmarketid = ? AND investmenttype = ?" +
            "AND (to_char(to_timestamp(capturedDateTime/1000),'mm')::int BETWEEN ? AND ?) " +
            "AND (to_char(to_timestamp(capturedDateTime/1000),'YYYY')::int BETWEEN ? AND ?)";

    private static String DELETE_PROJECT_COSTS_BY_PROJECT_ENDMARKET_PER_QUARTER = "DELETE FROM grailprojectcostfields where projectid = ? AND endmarketid = ? AND investmenttype =? "+
            "AND (to_char(to_timestamp(capturedDateTime/1000),'mm')::int BETWEEN ? AND ?) " +
            "AND (to_char(to_timestamp(capturedDateTime/1000),'YYYY')::int BETWEEN ? AND ?)";

    private static String DELETE_PROJECT_COSTS_BY_PROJECT_ENDMARKET = "DELETE FROM grailprojectcostfields where projectid = ? AND endmarketid = ?";


    @Override
    public List<Project> getProjects(final Quarter quarter) {
        List<Project> projects = Collections.emptyList();
        try {
            projects = getJdbcTemplate().query(GET_PROJECTS, projectRowMapper);
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return projects;
    }

    private final ParameterizedRowMapper<Project> projectRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
            Project project = new Project();
            project.setProjectID(rs.getLong("projectID"));
            project.setName(rs.getString("name"));
            project.setDescription(rs.getString("description"));
            project.setCategoryType(synchroDAOUtil.getIDs(rs.getString("categoryType")));
            project.setBrand(rs.getLong("brand"));
            project.setMethodologyType(rs.getLong("methodologyType"));
            project.setMethodologyGroup(rs.getLong("methodologyGroup"));
            project.setProposedMethodology(synchroDAOUtil.getIDs(rs.getString("proposedMethodology")));
            project.setStartDate(new Date(rs.getLong("startDate")));
            project.setEndDate(new Date(rs.getLong("endDate")));
            project.setProjectOwner(rs.getLong("projectOwner"));
            project.setBriefCreator(rs.getLong("briefCreator"));
            project.setMultiMarket(rs.getBoolean("multiMarket"));
            project.setTotalCost(rs.getBigDecimal("totalCost"));
            project.setTotalCostCurrency(rs.getLong("totalCostCurrency"));
            project.setCreationBy(rs.getLong("creationBy"));
            project.setCreationDate(rs.getLong("creationDate"));
            project.setModifiedBy(rs.getLong("modificationBy"));
            project.setModifiedDate(rs.getLong("modificationDate"));
            project.setStatus(rs.getLong("status"));
            project.setCapRating(rs.getLong("caprating"));
            return project;
        }
    };

    @Override
    public void saveProjectCosts(final ProjectCostsBean bean) {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailprojectcostfields");
        try {
            bean.setId(id);
            getJdbcTemplate().update(INSERT_PROJECT_COST,
                    bean.getId(),
                    bean.getProjectId(),
                    bean.getEndmarketId(),
                    bean.getMultiMarket()?1:0,
                    bean.getAboveMarket()?1:0,
                    bean.getInvestmentType(),
                    bean.getSpiContact(),
                    bean.getProjectOwner(),
                    bean.getStage(),
                    bean.getProjectStartDate() != null?bean.getProjectStartDate().getTime():null,
                    bean.getProjectEndDate() != null?bean.getProjectEndDate().getTime():null,
                    bean.getEstimatedCost() == null?0:bean.getEstimatedCost(),
                    bean.getEstimatedCostCurrency() == null?-1:bean.getEstimatedCostCurrency(),
                    bean.getLatestEstimatedCost() == null?0:bean.getLatestEstimatedCost(),
                    bean.getLatestEstimatedCostCurrency() == null?-1:bean.getLatestEstimatedCostCurrency(),
                    bean.getAgency1(),
                    bean.getAgency1optional(),
                    bean.getAgency2(),
                    bean.getAgency2optional(),
                    bean.getAgency3(),
                    bean.getAgency3optional(),
                    bean.getAgency1Department(),
                    bean.getAgency2Department(),
                    bean.getAgency3Department(),
                    bean.getAwardedAgency(),
                    bean.getProposalTotalCost() == null?0:bean.getProposalTotalCost(),
                    bean.getProposalTotalCostCurrency() == null?-1:bean.getProposalTotalCostCurrency(),
                    bean.getProposalInitialMgmtCost() == null?0:bean.getProposalInitialMgmtCost(),
                    bean.getProposalInitialMgmtCostCurrency() == null?-1:bean.getProposalInitialMgmtCostCurrency(),
                    bean.getProposalLocalMgmtCost() == null?0:bean.getProposalLocalMgmtCost(),
                    bean.getProposalLocalMgmtCostCurrency() == null?-1:bean.getProposalLocalMgmtCostCurrency(),
                    bean.getProposalFieldworkCost() == null?0:bean.getProposalFieldworkCost(),
                    bean.getProposalFieldworkCostCurrency() == null?-1:bean.getProposalFieldworkCostCurrency(),
                    bean.getProposalOperationHubCost() == null?0:bean.getProposalOperationHubCost(),
                    bean.getProposalOperationHubCostCurrency() == null?-1:bean.getProposalOperationHubCostCurrency(),
                    bean.getProposalOtherCost() == null?0:bean.getProposalOtherCost(),
                    bean.getProposalOtherCostCurrency() == null?-1:bean.getProposalOtherCostCurrency(),
                    bean.getPsOriginalCost() == null?0:bean.getPsOriginalCost(),
                    bean.getPsOriginalCostCurrency() == null?-1:bean.getPsOriginalCostCurrency(),
                    bean.getPsFinalCost() == null?0:bean.getPsFinalCost(),
                    bean.getPsFinalCostCurrency() == null?-1:bean.getPsFinalCostCurrency(),
                    bean.getTotalProjectCost() == null?0:bean.getTotalProjectCost(),
                    bean.getTotalProjectCostCurrency() == null?-1:bean.getTotalProjectCostCurrency(),
                    bean.getLatestProjectCost() == null?0:bean.getLatestProjectCost(),
                    bean.getLatestProjectCostCurrency() == null?-1:bean.getLatestProjectCostCurrency(),
                    bean.getTenderingCost() == null?0:bean.getTenderingCost(),
                    bean.getTenderingCostCurrency() == null?-1:bean.getTenderingCostCurrency(),
                    bean.getDatacollection(),
                    bean.getCapturedDate().getTime(),
                    bean.getUpdatedDate().getTime(),
                    bean.getProjectStatus()
            );

        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
    }

    @Override
    public void updateProjectCosts(final ProjectCostsBean bean) {
        try {
            getJdbcTemplate().update(UPDATE_PROJECT_COST,
                    bean.getProjectId(),
                    bean.getEndmarketId(),
                    bean.getMultiMarket()?1:0,
                    bean.getAboveMarket()?1:0,
                    bean.getInvestmentType(),
                    bean.getSpiContact(),
                    bean.getProjectOwner(),
                    bean.getStage(),
                    bean.getProjectStartDate() != null?bean.getProjectStartDate().getTime():null,
                    bean.getProjectEndDate() != null?bean.getProjectEndDate().getTime():null,
                    bean.getEstimatedCost() == null?0:bean.getEstimatedCost(),
                    bean.getEstimatedCostCurrency() == null?-1:bean.getEstimatedCostCurrency(),
                    bean.getLatestEstimatedCost() == null?0:bean.getLatestEstimatedCost(),
                    bean.getLatestEstimatedCostCurrency() == null?-1:bean.getLatestEstimatedCostCurrency(),
                    bean.getAgency1(),
                    bean.getAgency1optional(),
                    bean.getAgency2(),
                    bean.getAgency2optional(),
                    bean.getAgency3(),
                    bean.getAgency3optional(),
                    bean.getAgency1Department(),
                    bean.getAgency2Department(),
                    bean.getAgency3Department(),
                    bean.getAwardedAgency(),
                    bean.getProposalTotalCost() == null?0:bean.getProposalTotalCost(),
                    bean.getProposalTotalCostCurrency() == null?-1:bean.getProposalTotalCostCurrency(),
                    bean.getProposalInitialMgmtCost() == null?0:bean.getProposalInitialMgmtCost(),
                    bean.getProposalInitialMgmtCostCurrency() == null?-1:bean.getProposalInitialMgmtCostCurrency(),
                    bean.getProposalLocalMgmtCost() == null?0:bean.getProposalLocalMgmtCost(),
                    bean.getProposalLocalMgmtCostCurrency() == null?-1:bean.getProposalLocalMgmtCostCurrency(),
                    bean.getProposalFieldworkCost() == null?0:bean.getProposalFieldworkCost(),
                    bean.getProposalFieldworkCostCurrency() == null?-1:bean.getProposalFieldworkCostCurrency(),
                    bean.getProposalOperationHubCost() == null?0:bean.getProposalOperationHubCost(),
                    bean.getProposalOperationHubCostCurrency() == null?-1:bean.getProposalOperationHubCostCurrency(),
                    bean.getProposalOtherCost() == null?0:bean.getProposalOtherCost(),
                    bean.getProposalOtherCostCurrency() == null?-1:bean.getProposalOtherCostCurrency(),
                    bean.getPsOriginalCost() == null?0:bean.getPsOriginalCost(),
                    bean.getPsOriginalCostCurrency() == null?-1:bean.getPsOriginalCostCurrency(),
                    bean.getPsFinalCost() == null?0:bean.getPsFinalCost(),
                    bean.getPsFinalCostCurrency() == null?-1:bean.getPsFinalCostCurrency(),
                    bean.getTotalProjectCost() == null?0:bean.getTotalProjectCost(),
                    bean.getTotalProjectCostCurrency() == null?-1:bean.getTotalProjectCostCurrency(),
                    bean.getLatestProjectCost() == null?0:bean.getLatestProjectCost(),
                    bean.getLatestProjectCostCurrency() == null?-1:bean.getLatestProjectCostCurrency(),
                    bean.getTenderingCost() == null?0:bean.getTenderingCost(),
                    bean.getTenderingCostCurrency() == null?-1:bean.getTenderingCostCurrency(),
                    bean.getDatacollection(),
                    bean.getCapturedDate().getTime(),
                    bean.getUpdatedDate().getTime(),
                    bean.getProjectStatus(),
                    bean.getId()
            );

        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<ProjectCostsBean> get(final Long projectId, Quarter quarter) {
        List<ProjectCostsBean> beans =  null;
        try {
            beans = getJdbcTemplate().query(GET_PROJECT_COSTS, projectCostsRowMapper, projectId,
                    quarter.getStartMonth(), quarter.getEndMonth(), quarter.getStartYear(), quarter.getEndYear());
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return beans;
    }

    @Override
    public List<ProjectCostsBean> get(final Long projectId, final Long endmarketId, final Integer investmentType, final Quarter quarter) {
        List<ProjectCostsBean> beans =  null;
        try {
            beans = getJdbcTemplate().query(GET_PROJECT_COSTS_BY_PROJECT_ENDMARKET, projectCostsRowMapper,
                    projectId, endmarketId, investmentType,
                    quarter.getStartMonth(), quarter.getEndMonth(), quarter.getStartYear(), quarter.getEndYear());
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return beans;
    }

    @Override
    public void delete(final Long projectId, final Long endmarketId, final Integer investmentType, final Quarter quarter) {
        try {
            getJdbcTemplate().update(DELETE_PROJECT_COSTS_BY_PROJECT_ENDMARKET_PER_QUARTER, projectId,endmarketId,investmentType, quarter.getStartMonth(), quarter.getEndMonth(), quarter.getStartYear(), quarter.getEndYear());
        } catch (DAOException e) {

        }
    }

    @Override
    public void delete(final Long projectId, final Long endmarketId) {
        try {
            getJdbcTemplate().update(DELETE_PROJECT_COSTS_BY_PROJECT_ENDMARKET, projectId,endmarketId);
        } catch (DAOException e) {

        }
    }

    private final ParameterizedRowMapper<ProjectCostsBean> projectCostsRowMapper = new ParameterizedRowMapper<ProjectCostsBean>() {
        public ProjectCostsBean mapRow(ResultSet rs, int row) throws SQLException {

            ProjectCostsBean project = new ProjectCostsBean();
            project.setId(rs.getLong("id"));
            project.setProjectId(rs.getLong("projectId"));
            project.setEndmarketId(rs.getLong("endmarketId"));
            project.setMultiMarket(rs.getBoolean("ismultimarket"));
            project.setAboveMarket(rs.getBoolean("isAboveMarket"));
            project.setInvestmentType(rs.getLong("investmentType"));
            project.setSpiContact(rs.getLong("spiContact"));
            project.setProjectOwner(rs.getLong("projectOwner"));



            project.setStage(rs.getInt("stage"));
            project.setProjectStartDate(new Date(rs.getLong("projectStartDate")));
            project.setProjectEndDate(new Date(rs.getLong("projectEndDate")));

            project.setEstimatedCost(rs.getBigDecimal("estimatedCost"));
            project.setEstimatedCostCurrency(rs.getLong("estimatedCostCurrency"));
            project.setLatestEstimatedCost(rs.getBigDecimal("latestEstimatedCost"));
            project.setLatestEstimatedCostCurrency(rs.getLong("latestEstimatedCostCurrency"));

            project.setAgency1(rs.getLong("agency1"));
            project.setAgency1optional(rs.getLong("agency1optional"));
            project.setAgency2(rs.getLong("agency2"));
            project.setAgency2optional(rs.getLong("agency2optional"));
            project.setAgency3(rs.getLong("agency3"));
            project.setAgency3optional(rs.getLong("agency3optional"));

            project.setAgency1Department(rs.getString("agency1dept"));
            project.setAgency2Department(rs.getString("agency2dept"));
            project.setAgency3Department(rs.getString("agency3dept"));

            project.setAwardedAgency(rs.getLong("awardedagency"));

            project.setProposalTotalCost(rs.getBigDecimal("propTotalCost"));
            project.setProposalTotalCostCurrency(rs.getLong("propTotalCostCurrency"));
            project.setProposalInitialMgmtCost(rs.getBigDecimal("propInlMgmtCost"));
            project.setProposalInitialMgmtCostCurrency(rs.getLong("propInlMgmtCostCurrency"));
            project.setProposalLocalMgmtCost(rs.getBigDecimal("propLocalMgmtCost"));
            project.setProposalLocalMgmtCostCurrency(rs.getLong("propLocalMgmtCostCurrency"));
            project.setProposalFieldworkCost(rs.getBigDecimal("propFieldworkCost"));
            project.setProposalFieldworkCostCurrency(rs.getLong("propFieldworkCostCurrency"));
            project.setProposalOperationHubCost(rs.getBigDecimal("propOperHubCost"));
            project.setProposalOperationHubCostCurrency(rs.getLong("propOperHubCostCurrency"));
            project.setProposalOtherCost(rs.getBigDecimal("propOtherCost"));
            project.setProposalOtherCostCurrency(rs.getLong("propOtherCostCurrency"));

            project.setPsOriginalCost(rs.getBigDecimal("psOriginalCost"));
            project.setPsOriginalCostCurrency(rs.getLong("psOriginalCostCurrency"));
            project.setPsFinalCost(rs.getBigDecimal("psFinalCost"));
            project.setPsFinalCostCurrency(rs.getLong("psFinalCostCurrency"));
            project.setTotalProjectCost(rs.getBigDecimal("totalProjectCost"));
            project.setTotalProjectCostCurrency(rs.getLong("totalProjectCostCurrency"));
            project.setLatestProjectCost(rs.getBigDecimal("latestProjectCost"));
            project.setLatestProjectCostCurrency(rs.getLong("latestProjectCostCurrency"));

            project.setTenderingCost(rs.getBigDecimal("tenderingCost"));
            project.setTenderingCostCurrency(rs.getLong("tenderingCostCurrency"));
            project.setDatacollection(rs.getString("datacollection"));
            project.setCapturedDate(new Date(rs.getLong("capturedDateTime")));
            project.setUpdatedDate(new Date(rs.getLong("updateddatetime")));
            project.setProjectStatus(rs.getLong("projectStatus"));


            return project;
        }
    };


    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
