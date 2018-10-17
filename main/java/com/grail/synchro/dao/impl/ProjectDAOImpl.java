package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.InvitedUser;
import com.grail.synchro.beans.InvitedUserResultFilter;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectPendingActivityViewBean;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.beans.ProjectStatus;
import com.grail.synchro.beans.ProjectTemplate;
import com.grail.synchro.dao.ProjectDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class ProjectDAOImpl extends SynchroAbstractDAO implements ProjectDAO {
    private static final Logger LOG = Logger.getLogger(ProjectDAOImpl.class);
    private CommunityManager communityManager;

    private static final String PROJECT_FIELDS =  "projectID, name, description, descriptiontext, categoryType, brand, methodologyType, " +
            " methodologyGroup, proposedMethodology, startDate, endDate, projectOwner, briefCreator, multiMarket, totalCost, totalCostCurrency, " +
            " creationby, modificationby, creationdate, modificationdate, status, caprating, isconfidential, region, area, issave, budgetyear,agencyDept, projectsavedate, projectstartdate ";


    private static final String INSERT_PROJECT = "INSERT INTO grailproject( " +PROJECT_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?, ?, ?, ?,?,?, ?, ?);";

    private static final String TEMPLATE_FIELDS =  "templateID, templateName, name, description, categorytype, brand, methodologytype, " +
            " methodologygroup, proposedmethodology, endMarkets, startdate, enddate, ownerid, spiid," +
            " creationby, modificationby, creationdate, modificationdate ";

    private static final String INSERT_PROJECT_TEMPLATE = "INSERT INTO grailProjectTemplate( " +TEMPLATE_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";


    private static final String UPDATE_PROJECT_TEMPLATE = "UPDATE grailProjectTemplate" +
            " SET templateName=?, name=?, description=?, categorytype=?, brand=?, methodologytype=?,"+
            " methodologygroup=?, proposedmethodology=?, endMarkets=?, startdate=?, enddate=?, ownerid=?, spiid=?,"+
            " modificationby=?, modificationdate=?"+
            " WHERE templateID = ?";

    private static final String LOAD_PROJECT_TEMPLATE = "SELECT "+ TEMPLATE_FIELDS + " FROM grailProjectTemplate WHERE templateID = ?";

    private static final String LOAD_ALL_PROJECT_TEMPLATES = "SELECT "+ TEMPLATE_FIELDS + " FROM grailProjectTemplate WHERE order by creationdate DESC";

    private static final String INSERT_EXCHANGE_RATE = "INSERT INTO grailCurrencyExchangeRate(currencyID, exchangeRate, modificationBy, modificationDate)" +
            " VALUES (?, ?, ?, ?);";

    private static final String UPDATE_EXCHANGE_RATE = "UPDATE grailCurrencyExchangeRate set exchangeRate=? WHERE currencyID = ? AND modificationDate = (SELECT max(er.modificationDate) from grailCurrencyExchangeRate er where er.currencyID = ?)";

    private static final String UPDATE_PROJECT = "UPDATE grailproject" +
            " SET name=?, description=?, descriptiontext=?, status=?, categoryType=?, brand=?, methodologyType=?, methodologyGroup=?, proposedMethodology=?," +
            "   startDate=?, endDate=?, projectOwner=?, totalCost=?, totalCostCurrency=?," +
            "   modificationby=?, modificationdate=?, caprating=?, isconfidential = ?, region=?, area=?, budgetYear=?,agencyDept=?, projectstartdate =? " +
            " WHERE projectid = ?";

    private static final String UPDATE_PROJECT_OWNER = "UPDATE grailproject SET projectOwner=? WHERE projectID = ?";


    private static final String LOAD_PROJECT = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + ",(CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact FROM grailproject p WHERE p.projectID = ? order by creationdate DESC";

    private static final String ALL_PROJECTS = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + ",(CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact FROM grailproject p order by p.creationdate DESC";

    private static final String PROJECTS_BY_USER = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + ",(CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact FROM grailproject p where p.ownerid = ? order by p.creationdate DESC";

    private static final String DELETE_PROJECT = "DELETE FROM grailproject WHERE projectid = ?";

    private static final String GET_PROJECT_CREATOR_ID = "SELECT creationby FROM grailproject where projectid = ? ";




    private static final String UPDATE_PROJECT_STATUS = "UPDATE grailProjectStatus SET status=?, modificationby=?, modificationdate=? WHERE projectid = ? AND endMarketID = ?";
    private static final String UPDATE_PROJECT_STATUS_PROJECT_ID = "UPDATE grailproject SET status=? WHERE projectid = ?";
    private static final String UPDATE_PROJECT_CATEGORY = "UPDATE grailproject SET categorytype=? WHERE projectid = ?";
    private static final String UPDATE_PROJECT_PIT = "UPDATE grailproject SET categorytype=?, proposedMethodology=?, name=?, isconfidential=?, budgetyear=? WHERE projectid = ?";
//    private static final String LOAD_PROJECT_STATUS = "SELECT projectID, endMarketID, status, creationby, modificationby, creationdate, modificationdate FROM grailProjectStatus where projectID = ? AND endMarketID = ? ";


    private static final String GET_PROJECTS = "SELECT " + prependAlias(PROJECT_FIELDS, "p") +
            ", (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = p.projectOwner) as ownerName" +
            ", (select b.name from grailBrandFields b where b.id=p.brand) as brandName" +
            ", (CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact"+
            " FROM grailproject p";
    private static final String GET_TOTAL_COUNT = "SELECT count(*) FROM grailproject p";

    // Investment & Funding SQL Queries
    private static String INVESTMENT_FIELDS = "investmentid, projectid, isabovemarket, investmenttype, investmenttypeid, fieldworkmarketid, fundingmarketid, projectcontact, estimatedcost," +
            " estimatedcostcurrency, isapproved, spicontact, approvalstatus, approvaldate ";

    private static String INSERT_INVESTMENT = "INSERT into grailfundinginvestment(" + INVESTMENT_FIELDS + ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String LOAD_INVESTMENT = "SELECT "+ INVESTMENT_FIELDS + " FROM grailfundinginvestment WHERE investmentid=?";

    private static final String UPDATE_INVESTMENT = "UPDATE grailfundinginvestment" +
            " SET isabovemarket=?, investmenttype=?, investmenttypeid=?, fieldworkmarketid=?, fundingmarketid=?, projectcontact=?, estimatedcost=?," +
            " estimatedcostcurrency=?, isapproved=?, spicontact=?, approvalstatus=?, approvaldate=?  " +
            " WHERE investmentid = ?";

    private static final String UPDATE_PROJECT_CONTACT = "UPDATE grailfundinginvestment" +
            " SET projectcontact=? WHERE projectid = ? and fieldworkmarketid=? ";

    private static final String UPDATE_SPI_CONTACT = "UPDATE grailfundinginvestment" +
            " SET spicontact=? WHERE projectid = ? and fieldworkmarketid=? ";

    private static String COUNT_INVESTMENT = "SELECT count(*) from grailfundinginvestment where investmentid=?";

    private static final String REMOVE_INVESTMENT = "DELETE FROM grailfundinginvestment WHERE investmentid=?";

    private static final String LOAD_INVESTMENTS_BY_PROJECT = "SELECT "+ INVESTMENT_FIELDS + " FROM grailfundinginvestment WHERE projectid=?";

    private static final String LOAD_INVESTMENTS_BY_PROJECT_END_MARKET_ID = "SELECT "+ INVESTMENT_FIELDS + " FROM grailfundinginvestment WHERE projectid=? and fieldworkmarketid=?";

    private static String COUNT_INVESTMENTS_BY_PROJECT = "SELECT count(*) from grailfundinginvestment where projectid=?";

    private static final String REMOVE_INVESTMENTS_BY_PROJECT = "DELETE FROM grailfundinginvestment WHERE projectid=?";

    private static final String DELETE_INVESTMENTS_BY_PROJECT_ID_END_MARKET_ID = "DELETE FROM grailfundinginvestment WHERE projectid=? and fieldworkmarketid=?";

    private SynchroDAOUtil synchroDAOUtil;

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Project create(final Project project) {

        try {
            // update audit fields
            updateAuditFields(project, false);
            //Long id = synchroDAOUtil.generateProjectID();
            Long id = synchroDAOUtil.nextSequenceID("projectid", "grailproject");
            LOG.info("Project next sequence - " + id);
            project.setProjectID(id);

            getSimpleJdbcTemplate().update(INSERT_PROJECT,
                    id,
                    project.getName(),
                    project.getDescription()!=null?project.getDescription():"",
                    project.getDescriptionText()!=null?project.getDescriptionText():"",
                    Joiner.on(",").join(project.getCategoryType()),
                    project.getBrand(),
                    project.getMethodologyType(),
                    project.getMethodologyGroup(),
                    project.getProposedMethodology()!=null?Joiner.on(",").join(project.getProposedMethodology()):null,
                    project.getStartDate().getTime(),
                    project.getEndDate().getTime(),
                    project.getProjectOwner(),
                    project.getBriefCreator(),
                    (project.getMultiMarket() != null && project.getMultiMarket())?1:0,
                    project.getTotalCost(),
                    project.getTotalCostCurrency(),
                    project.getCreationBy(),
                    project.getModifiedBy(),
                    project.getCreationDate(),
                    project.getModifiedDate(),
                    project.getStatus(),
                    project.getCapRating(),
                    (project.getConfidential() != null && project.getConfidential())?1:0,
                    project.getRegions()!=null?Joiner.on(",").join(project.getRegions()):null,
                    project.getAreas()!=null?Joiner.on(",").join(project.getAreas()):null,
                    (project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())?1:0,
                    project.getBudgetYear(),
                    project.getAgencyDept(),
                    (project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())?project.getModifiedDate():null,
                    (project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())?null:project.getModifiedDate()		
                    		

            );
            return project;
        }
        catch (DataAccessException e) {
            final String message = "Failed to create new synchro project";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Project update(final Project project) {
        try {
            // update audit fields
            updateAuditFields(project, true);

            getSimpleJdbcTemplate().update(UPDATE_PROJECT,
                    project.getName(),
                    project.getDescription(),
                    project.getDescriptionText(),
                    project.getStatus(),
                    Joiner.on(",").join(project.getCategoryType()),
                    project.getBrand(),
                    project.getMethodologyType(),
                    project.getMethodologyGroup(),
                    project.getProposedMethodology()!=null?Joiner.on(",").join(project.getProposedMethodology()):null,
                    project.getStartDate().getTime(),
                    project.getEndDate().getTime(),
                    project.getProjectOwner(),
                    project.getTotalCost(),
                    project.getTotalCostCurrency(),
                    project.getModifiedBy(),
                    project.getModifiedDate(),
                    project.getCapRating(),
                    (project.getConfidential() != null && project.getConfidential())?1:0,
                    project.getRegions()!=null?Joiner.on(",").join(project.getRegions()):null,
                    project.getAreas()!=null?Joiner.on(",").join(project.getAreas()):null,
                    project.getBudgetYear(),
                    project.getAgencyDept(),
                   // (project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())?project.getModifiedDate():null,
                    (project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())?null:project.getModifiedDate(),
                    project.getProjectID()

                    
                    
            );
            return project;
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the project details.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectTemplate createTemplate(final ProjectTemplate template) {

        try {
            // update audit fields
            updateAuditFields(template, false);
            Long id = synchroDAOUtil.nextSequenceID("templateID", "grailProjectTemplate");
            LOG.info("Project Template next sequence - " + id);
            template.setTemplateID(id);

            getSimpleJdbcTemplate().update(INSERT_PROJECT_TEMPLATE,
                    id,
                    template.getTemplateName(),
                    template.getName(),
                    template.getDescription(),
                    Joiner.on(",").join(template.getCategoryType()),
                    template.getBrand(),
                    template.getMethodology(),
                    template.getMethodologyGroup(),
                    template.getProposedMethodology(),
                    Joiner.on(",").join(template.getEndMarkets()),
                    synchroDAOUtil.getDateTimeStamp(template.getStartDate()),
                    synchroDAOUtil.getDateTimeStamp(template.getEndDate()),
                    template.getOwnerID(),
                    template.getSpi(),
                    template.getCreationBy(),
                    template.getModifiedBy(),
                    template.getCreationDate(),
                    template.getModifiedDate()
            );
            return template;
        }
        catch (DataAccessException e) {
            final String message = "Failed to create new synchro project template";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public ProjectTemplate update(final ProjectTemplate template) {
        try {
            // update audit fields
            updateAuditFields(template, true);

            getSimpleJdbcTemplate().update(UPDATE_PROJECT_TEMPLATE,
                    template.getTemplateName(),
                    template.getName(),
                    template.getDescription(),
                    Joiner.on(",").join(template.getCategoryType()),
                    template.getBrand(),
                    template.getMethodology(),
                    template.getMethodologyGroup(),
                    template.getProposedMethodology(),
                    Joiner.on(",").join(template.getEndMarkets()),
                    synchroDAOUtil.getDateTimeStamp(template.getStartDate()),
                    synchroDAOUtil.getDateTimeStamp(template.getEndDate()),
                    template.getOwnerID(),
                    template.getSpi(),
                    template.getModifiedBy(),
                    template.getModifiedDate(),
                    template.getTemplateID()
            );
            return template;
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the project template for template ID "+template.getTemplateID();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void delete(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_PROJECT, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the project " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateOwner(final Long projectID, final Long ownerID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_OWNER, ownerID, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to ownerID for project " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    public Project get(final Long projectID){
        try {
            Project project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_PROJECT, projectRowMapper, projectID);
            return project;
        }
        catch (DataAccessException e) {
            final String message = "Failed to create new project.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    public ProjectTemplate getTemplate(final Long templateID){
        try {
            ProjectTemplate template = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_PROJECT_TEMPLATE, projectTemplateRowMapper, templateID);
            return template;
        }
        catch (DataAccessException e) {
            final String message = "Failed to get project template with ID "+templateID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    public Long getProjectCreator(final Long projectID){
        try {
            Long userID = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_PROJECT_CREATOR_ID, projectID);
            return userID;
        }
        catch (DataAccessException e) {
            final String message = "Failed to get Project Creator for the project "+projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    public List<Project> getAll(){
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Project> projects = Collections.emptyList();
        try {
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(ALL_PROJECTS, projectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }

    public List<ProjectTemplate> getAllTemplates(){
        List<ProjectTemplate> templates = Collections.emptyList();
        try {
            templates = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_ALL_PROJECT_TEMPLATES, projectTemplateRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all project templates from database";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return templates;
    }

    @Override
    public List<Project> getProjects(final ProjectResultFilter projectResultFilter){
        List<Project> projects = Collections.emptyList();
        String projectFields =  "p.projectID, p.name, p.description, p.descriptiontext, p.categoryType, p.brand, p.methodologyType, " +
                " p.methodologyGroup, p.proposedMethodology, p.startDate, p.endDate, " +
                " (CASE WHEN (p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment fi1 where p.projectid = fi1.projectid AND fi1.isabovemarket = 1) > 0) THEN (SELECT fi.projectContact FROM grailfundinginvestment fi where p.projectid = fi.projectid order by fi.investmenttype offset 0 limit 1) ELSE p.projectOwner END) as projectOwner, " +
                " p.briefCreator, p.multiMarket, p.totalCost, p.totalCostCurrency, " +
                " p.creationby, p.modificationby, p.creationdate, p.modificationdate, p.status, p.caprating,p.isconfidential,p.area,p.region , p.issave, p.budgetyear,p.agencyDept, p.projectsavedate, p.projectstartdate";

        StringBuilder sql = new StringBuilder("SELECT " + projectFields +
                ", (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = (CASE WHEN (p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment fi1 where p.projectid = fi1.projectid AND fi1.isabovemarket = 1) > 0) THEN (SELECT fi.projectContact FROM grailfundinginvestment fi where p.projectid = fi.projectid order by fi.investmenttype offset 0 limit 1) ELSE p.projectOwner END)) as ownerName" +
                ", (select b.name from grailBrandFields b where b.id=p.brand) as brandName" +
                ", (CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact" +
                ", (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = (CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END)) as spiContactName" +
                " FROM grailproject p");
        sql.append(applyProjectFilter(projectResultFilter));

        //sql.append(getOrderByClause(projectResultFilter.getSortField(), projectResultFilter.getAscendingOrder()));
        if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
            sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        } else {
            sql.append(" order by ").append(getOrderByField("status")).append(" ").append(SynchroDAOUtil.getSortType(0)).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
        }
        try {
            if(projectResultFilter.getStart() != null) {
                sql.append(" OFFSET ").append(projectResultFilter.getStart());
            }
            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
            }
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load projects by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }


    private String applyProjectFilter(final ProjectResultFilter filter) {
        User user = filter.getUser() != null?filter.getUser():SynchroPermHelper.getEffectiveUser();

        // Handle conditions
        List<String> conditions = new ArrayList<String>();
        StringBuilder filterStringBuilder = new StringBuilder();

        StringBuilder endMargetRegionFilter = new StringBuilder();
        if((filter.getEndMarkets() != null && filter.getEndMarkets().size() > 0)
                || (filter.getRegionFields() != null && filter.getRegionFields().size() > 0)) {
            filterStringBuilder.append(" INNER JOIN grailEndMarketInvestment ei");
            filterStringBuilder.append(" ON ");
            filterStringBuilder.append(" (");
            filterStringBuilder.append("p.projectid = ei.projectid");

            List<String> endMarketRegionConditions = new ArrayList<String>();
            if(filter.getEndMarkets() != null && filter.getEndMarkets().size() > 0) {
                StringBuilder endMarketCondition = new StringBuilder();
                endMarketCondition.append(" ei.endMarketID in (")
                        .append(StringUtils.join(filter.getEndMarkets(), ",")).append(")");
                endMarketRegionConditions.add(endMarketCondition.toString());
            }

            if(filter.getRegionFields() != null && filter.getRegionFields().size() > 0) {
                StringBuilder regionCondition = new StringBuilder();
                regionCondition.append(" ei.endMarketID in (");
                regionCondition.append("select grfm.endmarketid from grailregionfieldmappingfields grfm where grfm.regionid in (")
                        .append(StringUtils.join(filter.getRegionFields(), ",")).append("))");
                endMarketRegionConditions.add(regionCondition.toString());
            }

            if(endMarketRegionConditions.size() > 0) {
                filterStringBuilder.append(" AND (");
                filterStringBuilder.append(StringUtils.join(endMarketRegionConditions," AND "));
                filterStringBuilder.append(")");
            }
            filterStringBuilder.append(")");
        }

        // Status filter
        StringBuilder statusCondition = new StringBuilder();

        if(filter.getProjectStatusFields() != null && filter.getProjectStatusFields().size() > 0) {
            statusCondition.append(" p.status in (").append(StringUtils.join(filter.getProjectStatusFields(),",")).append(")");
            conditions.add(statusCondition.toString());
        } else if(filter.isFetchOnlyUserSpecificProjects() && !(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroCommunicationAgencyAdmin())) {
            statusCondition.append(" p.status not in (").append(SynchroGlobal.Status.DELETED.ordinal()).append(")");
            conditions.add(statusCondition.toString());
        }


        // Confidential projects will not be visible for Communication Agency Admin role.
        if(filter.isFetchOnlyUserSpecificProjects() && SynchroPermHelper.isSynchroCommunicationAgencyAdmin()) {
            conditions.add(" p.isconfidential = 0 ");
        }

        // Keyword filter
        if(filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append("(lower(p.name) like '%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| p.projectID ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(filter.getKeyword())).append("%'")
//                    .append(" OR ").append("(SELECT count(*) FROM jiveUser u1 where u1.userid = (CASE WHEN (p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment fi1 where p.projectid = fi1.projectid AND fi1.isabovemarket = 1) > 0) THEN (SELECT fi.projectContact FROM grailfundinginvestment fi where p.projectid = fi.projectid order by fi.investmenttype offset 0 limit 1) ELSE p.projectOwner END) AND lower(u1.firstname || ' ' ||u1.lastname) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%') > 0")
                    .append(" OR ").append("(SELECT count(*) FROM jiveUser u1 where u1.userid = (CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) AND lower(u1.firstname || ' ' ||u1.lastname) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%') > 0")
                    .append(")");
            conditions.add(keywordCondition.toString());
        }

        // Name filter
        if(filter.getName() != null && !filter.getName().equals("")) {
            StringBuilder nameCondition = new StringBuilder();
            nameCondition.append("name like '%").append(filter.getName()).append("%'");
            conditions.add(nameCondition.toString());
        }


        // Owner filter
        if(filter.getOwnerfield() != null && filter.getOwnerfield().size() > 0) {
            StringBuilder ownerCondition = new StringBuilder();
            ownerCondition.append("(CASE WHEN (p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment fi1 where p.projectid = fi1.projectid) > 0) THEN (SELECT fi.projectContact FROM grailfundinginvestment fi where p.projectid = fi.projectid order by fi.investmenttype offset 0 limit 1) ELSE p.projectOwner END) in (").append(StringUtils.join(filter.getOwnerfield(), ",")).append(")");
            conditions.add(ownerCondition.toString());
        }

        // SPI Contacts filter
        if(filter.getSpiContacts() != null && filter.getSpiContacts().size() > 0) {
            StringBuilder spiContactsCondition = new StringBuilder();
            spiContactsCondition.append("(CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) in (").append(StringUtils.join(filter.getSpiContacts(), ",")).append(")");
            conditions.add(spiContactsCondition.toString());
        }

        // Agencies filter
        if(filter.getAgencies() != null && filter.getAgencies().size() > 0) {
            StringBuilder agenciesCondition = new StringBuilder();
            String agencyFilter = StringUtils.join(filter.getAgencies(), ",");
            agenciesCondition.append("(p.projectid = (SELECT sh.projectid FROM grailpibstakeholderlist sh where p.projectId = sh.projectId AND (sh.agencycontact1 in ("+agencyFilter+") OR sh.agencycontact2 in ("+agencyFilter+") OR sh.agencycontact3 in ("+agencyFilter+") OR sh.agencycontact1optional in ("+agencyFilter+") OR sh.agencycontact2optional in ("+agencyFilter+") OR sh.agencycontact3optional in ("+agencyFilter+")) group by sh.projectId)").append(")");
            conditions.add(agenciesCondition.toString());
        }

        // Agency names filter
        if(filter.getAgencyNames() != null && filter.getAgencyNames().size() > 0) {
            StringBuilder agenciesNamesCondition = new StringBuilder();
            String agencyNamesFilter = "'"+StringUtils.join(filter.getAgencyNames(), ",").replaceAll(",","', '")+"'";
            agenciesNamesCondition.append("(p.projectid = (SELECT sh.projectid FROM grailpibstakeholderlist sh, jiveuserprofile jup where p.projectId = sh.projectId  AND jup.fieldid = 2 AND jup.value in ("+agencyNamesFilter+") AND (sh.agencycontact1 = jup.userid OR sh.agencycontact2 = jup.userid OR sh.agencycontact3 = jup.userid OR sh.agencycontact1optional = jup.userid OR sh.agencycontact2optional = jup.userid OR sh.agencycontact3optional = jup.userid) group by sh.projectId)").append(")");
            conditions.add(agenciesNamesCondition.toString());
        }

        // Methodology filter
        /*   if(filter.getMethodologyFields() != null && filter.getMethodologyFields().size() > 0) {
               StringBuilder methodologyCondition = new StringBuilder();
               methodologyCondition.append("methodologytype in (")
                       .append(StringUtils.join(filter.getMethodologyFields(),",")).append(")");
               conditions.add(methodologyCondition.toString());
           }
   */
         //Proposed Methodology filter
           //This is issue raised by client - The reason for this is that on Advance Filter screen we display the Proposed Methodologies and we filter it by Methdology Type (Quantitative, Qualitative etc) 
             /* if(filter.getMethodologyFields() != null && filter.getMethodologyFields().size() > 0) {
                  StringBuilder methodologyCondition = new StringBuilder();
                  methodologyCondition.append("proposedmethodology in ('")
                          .append(StringUtils.join(filter.getMethodologyFields(),",")).append("')");
                  conditions.add(methodologyCondition.toString());
              }*/
           if(filter.getMethodologyFields() != null && filter.getMethodologyFields().size() > 0) {
               StringBuilder methodologyCondition = new StringBuilder();
               methodologyCondition.append("(proposedmethodology in ('")
                       .append(filter.getMethodologyFields().get(0)+"").append("')");
               if(filter.getMethodologyFields().size()>0)
               {
               	methodologyCondition.append(" or proposedmethodology like ('")
                    .append(filter.getMethodologyFields().get(0)+"").append(",%')");
               	methodologyCondition.append(" or proposedmethodology like ('%,")
                   .append(filter.getMethodologyFields().get(0)+"").append("')");
               	methodologyCondition.append(" or proposedmethodology like ('%,")
                .append(filter.getMethodologyFields().get(0)+"").append(",%')");
               	for(int i=1;i<filter.getMethodologyFields().size();i++)
               	{
               		methodologyCondition.append(" or proposedmethodology in ('")
                       .append(filter.getMethodologyFields().get(i)+"").append("')");
               		methodologyCondition.append(" or proposedmethodology like ('")
                       .append(filter.getMethodologyFields().get(i)+"").append(",%')");
                  	methodologyCondition.append(" or proposedmethodology like ('%,")
                      .append(filter.getMethodologyFields().get(i)+"").append("')");
                  	methodologyCondition.append(" or proposedmethodology like ('%,")
                    .append(filter.getMethodologyFields().get(i)+"").append(",%')");
               	}
               }
               methodologyCondition.append(")");
               LOG.info("Proposed Methodology Filter Query - " + methodologyCondition.toString());
               conditions.add(methodologyCondition.toString());
           }

        // Brand filter
        if(filter.getBrandFields() != null && filter.getBrandFields().size() > 0) {
            StringBuilder brandCondition = new StringBuilder();
            brandCondition.append("brand in (")
                    .append(StringUtils.join(filter.getBrandFields(),",")).append(")");
            conditions.add(brandCondition.toString());
        }


        // Start Year filter
        if(filter.getStartYear() != null && filter.getStartYear() > 0) {
            StringBuilder startYearCondition = new StringBuilder();
            startYearCondition.append("to_char(to_timestamp(startDate/1000),'YYYY')::int >= ")
                    .append(filter.getStartYear());
            conditions.add(startYearCondition.toString());
        }

        // Start Month filter
        if(filter.getStartMonth() != null && filter.getStartMonth() > -1) {
            StringBuilder startMonthCondition = new StringBuilder();
            startMonthCondition.append("to_char(to_timestamp(startDate/1000),'mm')::int >= ")
                    .append(filter.getStartMonth()+1);
            conditions.add(startMonthCondition.toString());
        }

        // End Year filter
        if(filter.getEndYear() != null && filter.getEndYear() > 0) {
            StringBuilder endYearCondition = new StringBuilder();
            endYearCondition.append("to_char(to_timestamp(endDate/1000),'YYYY')::int <= ")
                    .append(filter.getEndYear());
            conditions.add(endYearCondition.toString());
        }

        // End Month filter
        if(filter.getEndMonth() != null && filter.getEndMonth() > -1) {
            StringBuilder endMonthCondition = new StringBuilder();
            endMonthCondition.append("to_char(to_timestamp(endDate/1000),'mm')::int <= ")
                    .append(filter.getEndMonth()+1);
            conditions.add(endMonthCondition.toString());
        }

        // Fetch only user specific projects
        if(filter.isFetchOnlyUserSpecificProjects()) {

            // if user is belongs to admin group or methodology approver's group then do not apply filter
            // If user belongs to Communication Agency Admin user group then do not apply filter

            StringBuilder userResponsibleFilter = new StringBuilder();

            if(!(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin(user) || SynchroPermHelper.isSynchroCommunicationAgencyAdmin()) || filter.isIgnoreSuperUserAccess()) {

                /** Start of user responsible filter **/
                // Project Owner and Project Brief Creator condition
                StringBuilder projectOwnerCondition = new StringBuilder();
                projectOwnerCondition.append( "(p.projectOwner=").append(user.getID()).append(" OR ")
                        .append("briefCreator=").append(user.getID()).append(")");

                // Stake holders filter
                StringBuilder stakeHoldersFilter = new StringBuilder();

                //https://svn.sourcen.com/issues/19720
                if(filter.isFetchProductContacts())
                {
                    stakeHoldersFilter.append("(p.projectID = (SELECT sh.projectid FROM grailpibstakeholderlist sh WHERE ")
                            .append("(")
                            // Removing the status filter for QUICK FIX functionality
                            //.append("(").append("sh.agencycontact1=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                            .append("sh.agencycontact1=").append(user.getID()).append(" OR ")
                            .append("(").append("sh.agencycontact2=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                            .append("(").append("sh.agencycontact3=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                           // .append("(").append("sh.agencycontact1optional=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                             .append("sh.agencycontact1optional=").append(user.getID()).append(" OR ")
                            .append("(").append("sh.agencycontact2optional=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                            .append("(").append("sh.agencycontact3optional=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                            .append("sh.globallegalcontact=").append(user.getID()).append(" OR ")
                            .append("sh.globalprocurementcontact=").append(user.getID()).append(" OR ")
                            .append("sh.productcontact=").append(user.getID()).append(" OR ")
                            .append("sh.globalcommunicationagency=").append(user.getID()).append(" OR ")
                            .append("sh.otherspicontact like '%" + user.getID() +"%'").append(" OR ")
                            .append("sh.otherlegalcontact like '%" + user.getID() +"%'").append(" OR ")
                            .append("sh.otherproductcontact like '%" + user.getID() +"%'").append(" OR ")
                            .append("sh.otheragencycontact like '%" + user.getID() +"%'")
                            .append(")")
                            .append(" AND p.projectID = sh.projectid group by sh.projectid)").append(")");
                }
                else
                {
                    stakeHoldersFilter.append("(p.projectID = (SELECT sh.projectid FROM grailpibstakeholderlist sh WHERE ")
                            .append("(")
                          // Removing the status filter for QUICK FIX functionality
                            //  .append("(").append("sh.agencycontact1=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                             .append("sh.agencycontact1=").append(user.getID()).append(" OR ")
                            .append("(").append("sh.agencycontact2=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                            .append("(").append("sh.agencycontact3=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                         //   .append("(").append("sh.agencycontact1optional=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                             .append("sh.agencycontact1optional=").append(user.getID()).append(" OR ")
                            .append("(").append("sh.agencycontact2optional=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                            .append("(").append("sh.agencycontact3optional=").append(user.getID()).append(" AND p.status >= "+ SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+")").append(" OR ")
                            .append("sh.globallegalcontact=").append(user.getID()).append(" OR ")
                            .append("sh.globalprocurementcontact=").append(user.getID()).append(" OR ")
                            // .append("sh.productcontact=").append(user.getID()).append(" OR ")
                            .append("sh.globalcommunicationagency=").append(user.getID()).append(" OR ")
                            .append("sh.otherspicontact like '%" + user.getID() +"%'").append(" OR ")
                            .append("sh.otherlegalcontact like '%" + user.getID() +"%'").append(" OR ")
                            //  .append("sh.otherproductcontact like '%" + user.getID() +"%'").append(" OR ")
                            .append("sh.otheragencycontact like '%" + user.getID() +"%'")
                            .append(")")
                            .append(" AND p.projectID = sh.projectid group by sh.projectid)").append(")");

                }

                // End Market Filter
                StringBuilder endmarketSPIFilter = new StringBuilder();
                endmarketSPIFilter.append("(p.projectID = (SELECT emi.projectid FROM grailEndMarketInvestment emi WHERE ")
                        .append("emi.spiContact=").append(user.getID()).append(" AND p.projectID = emi.projectid  group by emi.projectid)").append(")");


                StringBuilder projectContactFilter = new StringBuilder();
                projectContactFilter.append("(p.projectID = (SELECT fi.projectid FROM grailfundinginvestment fi WHERE ")
                        .append("(").append("fi.projectcontact=").append(user.getID()).append(" OR ").append("fi.spicontact=").append(user.getID()).append(")").append(" AND p.projectID = fi.projectid  group by fi.projectid)").append(")");

                userResponsibleFilter.append("(");
                userResponsibleFilter.append(projectOwnerCondition.toString()).append(" OR ")
                        .append(stakeHoldersFilter.toString()).append(" OR ").append(endmarketSPIFilter.toString())
                        .append(" OR ").append(projectContactFilter.toString());

                if(SynchroPermHelper.isMethodologyApproverUser()) {

                    // PIB Methodology Waiver
                    StringBuilder pibMethWaiver = new StringBuilder();
                    pibMethWaiver.append("(p.projectID = (SELECT mw.projectid FROM grailpibmethodologywaiver mw WHERE ")
                            .append(" mw.methodologyapprover=").append(user.getID())
                            .append(" and (mw.isapproved IS NULL OR mw.isapproved !=1 )")
                                    //  .append(")")
                            .append(" AND p.projectID = mw.projectid group by mw.projectid)").append(")");

                    // PS Methodology Waiver
                    StringBuilder psMethWaiver = new StringBuilder();
                    psMethWaiver.append("(p.projectID = (SELECT mwps.projectid FROM grailpsmethodologywaiver mwps WHERE ")
                            .append(" mwps.methodologyapprover=").append(user.getID())
                            .append(" and (mwps.isapproved IS NULL OR mwps.isapproved !=1 )")
                                    //  .append(")")
                            .append(" AND p.projectID = mwps.projectid group by mwps.projectid)").append(")");
                    userResponsibleFilter.append(" OR ").append(pibMethWaiver.toString()).append(" OR ").append(psMethWaiver.toString());
                }

                //https://svn.sourcen.com/issues/19629 - The Kantar Methodology Waiver Approver is not able to see the projects in their dashboard
                if(SynchroPermHelper.isKantarMethodologyApproverUser()) {

                    // PIB Methodology Waiver
                    StringBuilder kantarMethWaiver = new StringBuilder();
                    kantarMethWaiver.append("(p.projectID = (SELECT kmw.projectid FROM grailpibkantarmw kmw WHERE ")
                            .append(" kmw.methodologyapprover=").append(user.getID())
                            .append(" and (kmw.isapproved IS NULL OR kmw.isapproved !=1 )")
                                    //  .append(")")
                            .append(" AND p.projectID = kmw.projectid group by kmw.projectid)").append(")");


                    userResponsibleFilter.append(" OR ").append(kantarMethWaiver.toString());
                }

                userResponsibleFilter.append(")");
            }
            /** End of user responsible filter **/



            if(!filter.isIgnoreSuperUserAccess()) {
                /** Start of user access filter **/
                // If current user is super user
                StringBuilder userAccessFilter = new StringBuilder();
                List<String> userAccessConditions = new ArrayList<String>();
                Map<String, String> userProperties = user.getProperties();

                // Has brand access
                if(userProperties != null && !userProperties.isEmpty()) {

                    boolean isGlobalSuperUser = SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin(user)
                            || SynchroPermHelper.isSynchroCommunicationAgencyAdmin() || SynchroPermHelper.isSynchroGlobalSuperUser(user);

                    if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS)) {
                        String brandsString = userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS);
                        if(brandsString != null && !brandsString.equals("")) {
                            StringBuilder brandsFilter = new StringBuilder();
                            brandsFilter.append("(");
                            brandsFilter.append("p.brand in ("+brandsString+")");
                            brandsFilter.append(")");
                            userAccessConditions.add(brandsFilter.toString());
                        }
                    }

                    // Has departments access
                    if(SynchroPermHelper.isExternalAgencyUser(user)) {
                        if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS)
                                && userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS) != null
                                && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS).equals("")) {
                            String departments = "'" + userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS) + "'";
                            departments = departments.replaceAll(",","','");
                            StringBuilder departmentFilter = new StringBuilder();
                            departmentFilter.append("(");
                            departmentFilter.append("(SELECT count(*) FROM grailpibstakeholderlist sh1 INNER JOIN jiveuserprofile jup ON")
                                    .append("(jup.fieldid = 2 AND jup.value in ("+departments+") AND (")
                                    .append("sh1.agencycontact1=jup.userid").append(" OR ")
                                    .append("sh1.agencycontact2=jup.userid").append(" OR ")
                                    .append("sh1.agencycontact3=jup.userid").append(" OR ")
                                    .append("sh1.agencycontact1optional=jup.userid").append(" OR ")
                                    .append("sh1.agencycontact2optional=jup.userid").append(" OR ")
                                    .append("sh1.agencycontact3optional=jup.userid").append("))")
                                    .append(" AND sh1.projectid = p.projectid AND p.status >= "+SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+") > 0");
                            departmentFilter.append(")");
                            userAccessConditions.add(departmentFilter.toString());
                        }
                    }


                    // Has regional/area/country super user access
                    List<Long> endmarketAccessList = new LinkedList<Long>();
                    if(!isGlobalSuperUser && userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                            && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                            && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                        String endmarketListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                        if(endmarketListStr != null && !endmarketListStr.equals("")) {
                            StringBuilder accessTypeFiler = new StringBuilder();
                            accessTypeFiler.append("(");
                            accessTypeFiler.append("(SELECT count(*) FROM grailendmarketinvestment emi where emi.projectid = p.projectid AND emi.endmarketid in ("+endmarketListStr+")) > 0");
                            accessTypeFiler.append(")");
                            userAccessConditions.add(accessTypeFiler.toString());
                        } else {
                            userAccessConditions.add("(1 = 0)");
                        }
                    } else if(!isGlobalSuperUser) {
                        userAccessConditions.add("(1 = 0)");
                    } else {
                        userAccessConditions.add("(1 = 1)");
                    }

                }
                if(userAccessConditions.size() > 0) {
                    userAccessFilter.append("(");
                    userAccessFilter.append("(").append(org.apache.commons.lang.StringUtils.join(userAccessConditions," AND ")).append(")");
                    if(!(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroGlobalSuperUser(user))) {
                        if(!filter.isFetchDraftProjects()) {
                            userAccessFilter.append(" OR ");
                            userAccessFilter.append("(CASE WHEN (p.status = "+SynchroGlobal.Status.DRAFT.ordinal()+") THEN (p.briefCreator = "+user.getID()+")  ELSE (1=0) END)");
                        }
                    }
                    userAccessFilter.append(")");

                }  else {
                    if(!(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isSynchroGlobalSuperUser(user))) {
                        if(!filter.isFetchDraftProjects()) {
                            userAccessFilter.append("(CASE WHEN (p.status = "+SynchroGlobal.Status.DRAFT.ordinal()+") THEN (p.briefCreator = "+user.getID()+")  ELSE (1=0) END)");
                        }
                    }
                }

                /** End of user access filter **/

                if(userResponsibleFilter.length() > 0 && userAccessFilter.length() > 0) {
                    conditions.add("(" + userResponsibleFilter.toString() + " OR " + userAccessFilter.toString() + ")");
                } else if(userResponsibleFilter.length() > 0) {
                    conditions.add(userResponsibleFilter.toString());
                } else if(userAccessFilter.length() > 0) {
                    conditions.add(userAccessFilter.toString());
                }
            } else {
                if(!filter.isFetchDraftProjects()) {
                    conditions.add("(CASE WHEN (p.status = "+SynchroGlobal.Status.DRAFT.ordinal()+") THEN (p.briefCreator = "+user.getID()+")  ELSE (1=0) END)");
                }
            }
        }
        if(filter.isFetchOnlyDraftProjects()) {
            if(filter.getDraftProjectRemindOffset() != null && filter.getDraftProjectRemindOffset().intValue() > 0) {
                Calendar calendar = Calendar.getInstance();
                String todayTimeStamp = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" +calendar.get(Calendar.DATE);
                String startDateTimeStamp = "(to_char(to_timestamp(startDate/1000),'YYYY')::int||'-'||to_char(to_timestamp(startDate/1000),'mm')::int||'-'||to_char(to_timestamp(startDate/1000),'dd')::int)";
                conditions.add("(DATE "+startDateTimeStamp+" - DATE '"+todayTimeStamp+"') = "+filter.getDraftProjectRemindOffset());
            }
        }


        if(conditions.size() > 0) {
            filterStringBuilder.append(" WHERE ").append(org.apache.commons.lang.StringUtils.join(conditions, " AND "));
        }


        return filterStringBuilder.toString();
    }


    /**
     *
     * @param sortField
     * @param order
     * @return
     */
    private String getOrderByClause(final String sortField, final Integer order) {
        StringBuilder orderBy = new StringBuilder();

        if(StringUtils.isNotBlank(sortField)) {
            String field = null;
            if(sortField.equals("id")) {
                field = "p.projectID";
            } else if(sortField.equals("name")) {
                field = "p.name";
            } else if(sortField.equals("owner")) {
                field = "ownerName";
            } else if(sortField.equals("brand")) {
                field = "brandName";
            } else if(sortField.equals("year")) {
                field = "to_char(to_timestamp(startDate/1000),'YYYY')::int";
            } else if(sortField.equals("status")) {
                field = "p.status";
            } else {
                field = sortField;
            }
            if(StringUtils.isNotBlank(field)) {
                orderBy.append(" order by ");
                orderBy.append(field).append(" ").append(SynchroDAOUtil.getSortType(order));
            }
        } else {
            orderBy.append(" order by ");
            orderBy.append("creationdate ").append(SynchroDAOUtil.getSortType(1));
        }
        return orderBy.toString();
    }

    private String getOrderByField(final String sortField) {
        if(StringUtils.isNotBlank(sortField)) {
            String field = null;
            if(sortField.equals("id")) {
                field = "p.projectID";
            } else if(sortField.equals("name")) {
                field = "p.name";
            } else if(sortField.equals("owner")) {
                field = "ownerName";
            } else if(sortField.equals("brand")) {
                field = "brandName";
            } else if(sortField.equals("year")) {
                field = "to_char(to_timestamp(startDate/1000),'YYYY')::int";
            } else if(sortField.equals("status")) {
                field = "p.status";
            } else if(sortField.equals("spiContact")) {
                field = "spiContactName";
            } else {
                field = sortField;
            }
            return field;
        }
        return null;
    }


    /**
     * This method will fetch the projects related to Pending Activities Section
     */
    @Override
    public List<Project> getReportProjects()
    {
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Project> projects = Collections.emptyList();
        try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(REPORTS_PROJECTS, projectRowMapper, SynchroGlobal.Status.DRAFT.ordinal(),SynchroGlobal.Status.DELETED.ordinal(),SynchroGlobal.Status.CONCEPT_CANCEL.ordinal(),SynchroGlobal.Status.PLANNED_CANCEL.ordinal(),SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal());
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }


    @Override
    public List<Project> getReportProjects(final ProjectResultFilter projectResultFilter)
    {
        boolean flag = true;
        String ALL_PROJECTS_FILTER = "SELECT "+ PROJECT_FIELDS + " FROM grailproject";

        //project name keyword
        String name = projectResultFilter.getName();
        if(StringUtils.isNotBlank(name))
        {
            ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where LOWER(gp.name) like '%" + name.toLowerCase() +"%'";
            flag = false;
        }

        //Project Owner
        List<Long> owner= projectResultFilter.getOwnerfield();
        if(owner!=null && owner.size()>0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where ownerid in ("+StringUtils.join(owner, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and ownerid in ("+StringUtils.join(owner, ',')+")";
            }
        }

        //Project Brand Fields
        List<Long> brandFields = projectResultFilter.getBrandFields();
        if(brandFields!=null && brandFields.size()>0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where brand in ("+StringUtils.join(brandFields, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and brand in ("+StringUtils.join(brandFields, ',')+")";
            }
        }


        //Fetch Region specific filters
        List<Long> regions = projectResultFilter.getRegionFields();
        List<Long> endMarkets = projectResultFilter.getEndMarkets();

        List<Long> filterEndMarketIDs = new ArrayList<Long>();
        //List<Long> regions = researchCycleReportFilters.getRegionFields();
        /* List<Long> regionEndMarketIDs = new ArrayList<Long>();
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
        List<Long> e_projectIDs = Collections.emptyList();
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
            }
            catch (DataAccessException e) {
                final String message = "Failed to load end markets and regions mentioned in Advannced Filter ";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }

        //Fetch projects based on end markets/Region selected in filter
        if(e_projectIDs != null && e_projectIDs.size() > 0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where projectid in ("+StringUtils.join(e_projectIDs, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and projectid in ("+StringUtils.join(e_projectIDs, ',')+")";
            }
        }



        //Project Status Filter Fields
        List<Long> statusFields = projectResultFilter.getProjectStatusFields();
        if(statusFields!=null && statusFields.size()>0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where status in ("+StringUtils.join(statusFields, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and status in ("+StringUtils.join(statusFields, ',')+")";
            }
        }



        /**By Default Reports shouldn't fetch DRAFT, DELETED and CANCELLED PROEJCTS **/
        if(flag)
        {
            //ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " WHERE status not in ("+SynchroGlobal.Status.DRAFT.ordinal()+", "+SynchroGlobal.Status.DELETED.ordinal()+", "+SynchroGlobal.Status.CONCEPT_CANCEL.ordinal()+", "+SynchroGlobal.Status.PLANNED_CANCEL.ordinal()+", "+SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()+")";
            flag = false;
        }
        else
        {
            //ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and status not in ("+SynchroGlobal.Status.DRAFT.ordinal()+", "+SynchroGlobal.Status.DELETED.ordinal()+", "+SynchroGlobal.Status.CONCEPT_CANCEL.ordinal()+", "+SynchroGlobal.Status.PLANNED_CANCEL.ordinal()+", "+SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()+")";
        }


        ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " order by creationdate DESC";
        List<Project> projects = Collections.emptyList();
        try {
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(ALL_PROJECTS_FILTER, projectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load projects for reports by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }

    public List<Project> getProjectsByUser(final Long userID){
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Project> projects = Collections.emptyList();
        try {
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(PROJECTS_BY_USER, projectRowMapper, userID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load projects for user "+userID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }



    public List<Project> getProjectsByUserAndResultFilter(final Long userID, final ProjectResultFilter projectResultFilter){
        boolean flag = true;
        String ALL_PROJECTS_FILTER = "SELECT "+ PROJECT_FIELDS + " FROM grailproject";

        //project name keyword
        String name = projectResultFilter.getName();
        if(StringUtils.isNotBlank(name))
        {
            ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where LOWER(name) like '%" + name.toLowerCase() +"%'";
            flag = false;
        }

        //Project Methodology Fields
        List<Long> methodologyFields = projectResultFilter.getMethodologyFields();
        if(methodologyFields!=null && methodologyFields.size()>0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where methodology in ("+StringUtils.join(methodologyFields, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and methodology in ("+StringUtils.join(methodologyFields, ',')+")";
            }

        }

        //Project Brand Fields
        List<Long> brandFields = projectResultFilter.getBrandFields();
        if(brandFields!=null && brandFields.size()>0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where brand in ("+StringUtils.join(brandFields, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and brand in ("+StringUtils.join(brandFields, ',')+")";
            }
        }

//    	Fetch Region specific filters
        List<Long> regions = projectResultFilter.getRegionFields();
        List<Long> endMarkets = projectResultFilter.getEndMarkets();

        List<Long> filterEndMarketIDs = new ArrayList<Long>();
        //List<Long> regions = researchCycleReportFilters.getRegionFields();
        /*
        List<Long> regionEndMarketIDs = new ArrayList<Long>();
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
        List<Long> e_projectIDs = Collections.emptyList();
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
                /*if(e_projectIDs == null || e_projectIDs.size() < 1)
                        {
                            //fetchProjects = false;
                            return null;
                        }*/
            }
            catch (DataAccessException e) {
                final String message = "Failed to load end markets and regions mentioned in Advannced Filter ";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }

        //Fetch projects based on end markets/Region selected in filter
        if(e_projectIDs != null && e_projectIDs.size() > 0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where projectid in ("+StringUtils.join(e_projectIDs, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and projectid in ("+StringUtils.join(e_projectIDs, ',')+")";
            }
        }

        //Fetch projects based on Project Status selected in filter
        // List<Long> status = synchroDAOUtil.getProjectStatus(projectResultFilter.getProjectStatusFields());
        List<Long> status = projectResultFilter.getProjectStatusFields();

        if(status != null && status.size() > 0)
        {
            if(flag)
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where status in ("+StringUtils.join(status, ',')+")";
                flag = false;
            }
            else
            {
                ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and status in ("+StringUtils.join(status, ',')+")";
            }
        }

        if(flag)
        {
            ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " where ownerid = " + userID;
            flag = false;
        }
        else
        {
            ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " and ownerid = " + userID;
        }

        ALL_PROJECTS_FILTER = ALL_PROJECTS_FILTER + " order by creationdate DESC";
        List<Project> projects = Collections.emptyList();
        try {
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(ALL_PROJECTS_FILTER, projectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load projects by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveExchangeRate(final Integer currencyID) {
        try {
            final Long currentTime = System.currentTimeMillis();
            final Long userID = JiveApplication.getContext().getAuthenticationProvider().getJiveUser().getID();
            getSimpleJdbcTemplate().update(INSERT_EXCHANGE_RATE,
                    currencyID,
                    SynchroUtils.getCurrencyExchangeRate(currencyID),
                    userID,
                    currentTime
            );

        }
        catch (DataAccessException e) {
            final String message = "Failed to update exchange rate information for currency ID " + currencyID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateExchangeRate(final Integer currencyID) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_EXCHANGE_RATE,
                    SynchroUtils.getCurrencyExchangeRate(currencyID),
                    currencyID,
                    currencyID
            );

        }
        catch (DataAccessException e) {
            final String message = "Failed to update exchange rate information for currency ID " + currencyID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }



    private final ParameterizedRowMapper<Project> projectRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
            Project project = new Project();
            project.setProjectID(rs.getLong("projectID"));
            project.setName(rs.getString("name"));
            project.setDescription(rs.getString("description"));
            project.setDescriptionText(rs.getString("descriptiontext"));
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
            project.setConfidential(rs.getBoolean("isconfidential"));
            List<Long> spiContact = new ArrayList<Long>();
            if(rs.getLong("spiContact") > 0) {
                spiContact.add(rs.getLong("spiContact"));
            }
            project.setSpiContact(spiContact);
            project.setRegions(synchroDAOUtil.getIDs(rs.getString("region")));
            project.setAreas(synchroDAOUtil.getIDs(rs.getString("area")));
            project.setIsSave(rs.getBoolean("issave"));
            project.setBudgetYear(rs.getInt("budgetyear"));
            project.setAgencyDept(rs.getLong("agencyDept"));
            
          
            if(rs.getLong("projectsavedate") > 0) {
            	project.setProjectSaveDate(new Date(rs.getLong("projectsavedate")));
            }
            if(rs.getLong("projectstartdate") > 0) {
            	project.setProjectStartDate(new Date(rs.getLong("projectstartdate")));
            }
            return project;
        }
    };

    private final ParameterizedRowMapper<ProjectTemplate> projectTemplateRowMapper = new ParameterizedRowMapper<ProjectTemplate>() {

        public ProjectTemplate mapRow(ResultSet rs, int row) throws SQLException {
            ProjectTemplate template = new ProjectTemplate();
            template.setTemplateID(rs.getLong("templateID"));
            template.setTemplateName(rs.getString("templateName"));
            template.setName(rs.getString("name"));
            template.setDescription(rs.getString("description"));

            String categories = rs.getString("categoryType");
            List<Long> categoryList = new ArrayList<Long>();
            for (String id : categories.split(","))
                categoryList.add(new Long(id));
            template.setCategoryType(categoryList);

            template.setBrand(rs.getLong("brand"));
            template.setMethodology(rs.getLong("methodologyType"));
            template.setMethodologyGroup(rs.getLong("methodologyGroup"));
            template.setProposedMethodology(rs.getLong("proposedMethodology"));

            String endMarkets = rs.getString("endMarkets");
            List<Long> endMarketsList = new ArrayList<Long>();
            for (String eid : endMarkets.split(","))
                endMarketsList.add(new Long(eid));
            template.setEndMarkets(endMarketsList);

            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("dd/MM/yyyy");

            Date startDate = new Date(rs.getLong("startDate"));
            template.setStartDate(sdf.format(startDate));

            Date endDate = new Date(rs.getLong("endDate"));
            template.setEndDate(sdf.format(endDate));

            template.setOwnerID(rs.getLong("ownerID"));
            template.setSpi(rs.getLong("spiID"));
            template.setCreationBy(rs.getLong("creationBy"));
            template.setCreationDate(rs.getLong("creationDate"));
            template.setModifiedBy(rs.getLong("modificationBy"));
            template.setModifiedDate(rs.getLong("modificationDate"));
            return template;
        }
    };

    private final ParameterizedRowMapper<ProjectStatus> projectStatusRowMapper = new ParameterizedRowMapper<ProjectStatus>() {
        public ProjectStatus mapRow(ResultSet rs, int row) throws SQLException {
            ProjectStatus projectStatus = new ProjectStatus();
            projectStatus.setProjectID(rs.getLong("projectID"));
            projectStatus.setEndMarketID(rs.getLong("endMarketID"));
            projectStatus.setStatus(rs.getLong("status"));
            projectStatus.setCreationBy(rs.getLong("creationBy"));
            projectStatus.setCreationDate(rs.getLong("creationDate"));
            projectStatus.setModifiedBy(rs.getLong("modificationBy"));
            projectStatus.setModifiedDate(rs.getLong("modificationDate"));
            return projectStatus;
        }
    };

    public void setSynchroDAOUtil(final SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }

    public void setCommunityManager(final CommunityManager communityManager) {
        this.communityManager = communityManager;
    }



    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectStatus(final ProjectStatus projectStatus) {
        try {
            // update audit fields
            updateAuditFields(projectStatus, true);
            getSimpleJdbcTemplate().update(UPDATE_PROJECT_STATUS,
                    projectStatus.getStatus(),
                    projectStatus.getModifiedBy(),
                    projectStatus.getModifiedDate(),
                    projectStatus.getProjectID(),
                    projectStatus.getEndMarketID()
            );
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the project status for new project ID  "+projectStatus.getProjectID() +" And end market ID " + projectStatus.getEndMarketID();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }
    @Override
    public void updateProjectStatus(final Long projectId, final Integer status) {
        try {

            getSimpleJdbcTemplate().update(UPDATE_PROJECT_STATUS_PROJECT_ID,
                    status,	projectId );
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the project status for new project ID  "+projectId ;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }

//    @Override
//    public ProjectStatus getProjectStatus(Long projectID, Long endMarketID) {
//        try {
//            ProjectStatus projectStatus = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_PROJECT_STATUS, projectStatusRowMapper, projectID, endMarketID);
//            return projectStatus;
//        }
//        catch (DataAccessException e) {
//            final String message = "Failed to load Project status for new project ID  "+projectID +" And end market ID " + endMarketID;
//            LOG.error(message, e);
//            throw new DAOException(message, e);
//        }
//    }

    @Override
    public Long getTotalCount(final ProjectResultFilter filter) {
        Long count = 0L;
        StringBuilder sql = new StringBuilder(GET_TOTAL_COUNT);
        sql.append(applyProjectFilter(filter));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        }
        catch (DataAccessException e) {
            final String message = "Failed to load projects by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return count;
    }


    private static String prependAlias(final String fields, final String alias) {
        StringBuilder result = new StringBuilder();
        String [] fieldsList =  fields.split(",");
        int i = 0;
        for(String field: fieldsList) {
            result.append(alias).append(".").append(field.trim());
            if(i < (fieldsList.length-1)) {
                result.append(", ");
            }
            i++;
        }
        return result.toString();
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateCategory(final Project project) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_CATEGORY,  Joiner.on(",").join(project.getCategoryType()), project.getProjectID());
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Category Type for project " + project.getProjectID();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updatePIT(final Project project) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_PIT,  Joiner.on(",").join(project.getCategoryType()), Joiner.on(",").join(project.getProposedMethodology()),
                    project.getName(),
                    (project.getConfidential() != null && project.getConfidential())?1:0,
                    project.getBudgetYear(),
                    project.getProjectID()
            );
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Category Type for project " + project.getProjectID();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long addInvite(final String email) {

        Long id = synchroDAOUtil.generateInviteUserID("id", "grailInviteUserTrackTable");
        String INSERT_INVITE = "INSERT INTO grailInviteUserTrackTable(id, email)" +
                " VALUES (?, ?) ";
        try {
            getSimpleJdbcTemplate().update(INSERT_INVITE, id, email);
        } catch (DataAccessException e) {
            final String message = "Failed to insert User invite in Track Table -  "+email;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        return id;
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long addInvite(final String email, final Long invitedBy, final Date invitedDate) {

        Long id = synchroDAOUtil.generateInviteUserID("id", "grailInviteUserTrackTable");
        String INSERT_INVITE = "INSERT INTO grailInviteUserTrackTable(id, email, invitedBy, invitedDate)" +
                " VALUES (?, ?, ?, ?) ";
        try {
            getSimpleJdbcTemplate().update(INSERT_INVITE,
                    id,
                    email,
                    invitedBy,
                    (invitedDate != null ? invitedDate.getTime() : (new Date()).getTime()));
        } catch (DataAccessException e) {
            final String message = "Failed to insert User invite in Track Table -  "+email;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        return id;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long getInvitedUser(final String email) {
        List<InvitedUser> invitedUsers = Collections.emptyList();
        String sql = "SELECT id, email, invitedBy, invitedDate FROM grailInviteUserTrackTable where email=?";
        try {
            invitedUsers = getSimpleJdbcTemplate().getJdbcOperations().query(sql,invitedUserRowMapper, email);
            if(invitedUsers != null && invitedUsers.size() > 0) {
                return invitedUsers.get(0).getId();
            }
        } catch (DataAccessException e) {
            final String message = "Failed to get invited users in Track Table -  ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return null;
    }

    @Override
    public Long getInvitedUser(final String email, final User user) {
        List<InvitedUser> invitedUsers = Collections.emptyList();
        String sql = "SELECT id, email, invitedBy, invitedDate FROM grailInviteUserTrackTable where email=? AND invitedBy = ?";
        try {
            invitedUsers = getSimpleJdbcTemplate().getJdbcOperations().query(sql,invitedUserRowMapper, email, user.getID());
            if(invitedUsers != null && invitedUsers.size() > 0) {
                return invitedUsers.get(0).getId();
            }
        } catch (DataAccessException e) {
            final String message = "Failed to get invited users in Track Table -  ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<InvitedUser> getInvitedUsers(final User user) {

        List<InvitedUser> invitedUsers = Collections.emptyList();
        String sql = "SELECT id, email, invitedBy, invitedDate FROM grailInviteUserTrackTable where invitedBy=?";
        try {
            invitedUsers = getSimpleJdbcTemplate().getJdbcOperations().query(sql,invitedUserRowMapper, user.getID());
        }
        catch (DataAccessException e) {
            final String message = "Failed to get invited users in Track Table -  ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return invitedUsers;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<InvitedUser> getInvitedUsers() {
        List<InvitedUser> invitedUsers = Collections.emptyList();
        String sql = "SELECT id, email, invitedBy, invitedDate FROM grailInviteUserTrackTable";
        try {
            invitedUsers = getSimpleJdbcTemplate().getJdbcOperations().query(sql, invitedUserRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to get invited users in Track Table -  ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return invitedUsers;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public List<InvitedUser> getInvitedUsers(final InvitedUserResultFilter filter) {
        List<InvitedUser> invitedUsers = Collections.emptyList();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT iu.id, iu.email, iu.invitedBy, iu.invitedDate FROM grailInviteUserTrackTable iu");
        sqlBuilder.append(applyInvitedUsersFilter(filter));
        if(filter.getStart() != null) {
            sqlBuilder.append(" OFFSET ").append(filter.getStart());
        }
        if(filter.getLimit() != null && filter.getLimit() > 0) {
            sqlBuilder.append(" LIMIT ").append(filter.getLimit());
        }
        try {
            invitedUsers = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(), invitedUserRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to get invited users in Track Table -  ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return invitedUsers;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long getInvitedUsersTotalCount(InvitedUserResultFilter filter) {
        Long count = 0L;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM grailInviteUserTrackTable iu");
        sqlBuilder.append(applyInvitedUsersFilter(filter));
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sqlBuilder.toString());
        }
        catch (DataAccessException e) {
            final String message = "Failed to get invited users in Track Table -  ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return count;
    }

    private String applyInvitedUsersFilter(final InvitedUserResultFilter filter) {
        StringBuilder where = new StringBuilder();
        List<String> conditions = new ArrayList<String>();
        if(filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            String keyword = filter.getKeyword().toLowerCase();
            StringBuilder keyWordBuilder = new StringBuilder();
            keyWordBuilder.append("(");
            keyWordBuilder.append("(lower(iu.email) like '%"+keyword+"%')");
            keyWordBuilder.append(" OR ");
            keyWordBuilder.append("((SELECT count(*) FROM jiveuser ju WHERE ju.userid = iu.invitedBy AND ((lower(ju.username) like '%"+keyword+"%') OR (lower(ju.name) like '%"+keyword+"%') OR (lower(ju.firstname) like '%"+keyword+"%') OR (lower(ju.lastname) like '%"+keyword+"%') OR (lower(ju.email) like '%"+keyword+"%')))> 0)");
            keyWordBuilder.append(")");
            conditions.add(keyWordBuilder.toString());
        }

        if(filter.getInvitedBy() != null && filter.getInvitedBy() > 0) {
            conditions.add("(iu.invitedBy = "+filter.getInvitedBy()+" OR iu.invitedBy is null)");
        }

        if(conditions.size() > 0) {
            where.append(" WHERE ").append(StringUtils.join(conditions, " AND "));
        }

        return where.toString();
    }

    private final ParameterizedRowMapper<InvitedUser> invitedUserRowMapper = new ParameterizedRowMapper<InvitedUser>() {

        public InvitedUser mapRow(ResultSet rs, int row) throws SQLException {
            InvitedUser invitedUser = new InvitedUser();
            invitedUser.setId(rs.getLong("id"));
            invitedUser.setEmail(rs.getString("email"));
            invitedUser.setInvitedBy(rs.getLong("invitedBy"));
            if(rs.getLong("invitedDate") > 0) {
                Date invitedDate = new Date(rs.getLong("invitedDate"));
                invitedUser.setInvitedDate(invitedDate);
            }

            return invitedUser;
        }
    };

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void removeInvite(final String email) {
        String DELETE_INVITE = "DELETE from grailInviteUserTrackTable" +
                " WHERE email = '" + email + "'";
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_PROJECT, email);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete User invite in Track Table -  "+email;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Long getInviteIdByEmail(final String email) {

        Long id = 0L;
        String INSERT_INVITE = "SELECT id from grailInviteUserTrackTable" +
                " where email = ?";

        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailInviteUserTrackTable where email = ?", email);
            if(count>0)
            {
                id = getSimpleJdbcTemplate().queryForLong(INSERT_INVITE, email);
            }
            else
            {
                id = 0L;
            }
        } catch (DataAccessException e) {
            final String message = "Failed to get User invite from Track Table -  "+email;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        return id;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void replaceInviteReferences(final Long referenceID, final Long id) {

        /* Project Owner */
        String UPDATE_PROJECTOWNER_INVITE = "UPDATE grailproject SET projectowner=? WHERE projectowner = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailproject where projectowner = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_PROJECTOWNER_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for Project Owner Contact";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        /* SPI */
        String UPDATE_SPI_INVITE = "UPDATE grailendmarketinvestment SET spicontact=? WHERE spicontact = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailendmarketinvestment where spicontact = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_SPI_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for SPI Contact";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        /* Agency 1 */
        String UPDATE_AGENCY1_INVITE = "UPDATE grailpibstakeholderlist SET agencycontact1=? WHERE agencycontact1 = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where agencycontact1 = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_AGENCY1_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for AGENCY Contact 1";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        /* Agency 2 */
        String UPDATE_AGENCY2_INVITE = "UPDATE grailpibstakeholderlist SET agencycontact2=? WHERE agencycontact2 = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where agencycontact2 = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_AGENCY2_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for AGENCY Contact 2";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        /* Agency 3 */
        String UPDATE_AGENCY3_INVITE = "UPDATE grailpibstakeholderlist SET agencycontact3=? WHERE agencycontact3 = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where agencycontact3 = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_AGENCY3_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for AGENCY Contact 3";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        /*Legal*/
        String UPDATE_LEGAL_INVITE = "UPDATE grailpibstakeholderlist SET globallegalcontact=? WHERE globallegalcontact = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where globallegalcontact = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_LEGAL_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for globallegalcontact";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        /*Procurement*/
        String UPDATE_PROC_INVITE = "UPDATE grailpibstakeholderlist SET globalprocurementcontact=? WHERE globalprocurementcontact = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where globalprocurementcontact = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_PROC_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for globalprocurementcontact";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        /*Communication Agency*/
        String UPDATE_COMM_INVITE = "UPDATE grailpibstakeholderlist SET globalcommunicationagency=? WHERE globalcommunicationagency = ?";
        try {

            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailpibstakeholderlist where globalcommunicationagency = ?", referenceID);

            if(count>0)
            {
                getSimpleJdbcTemplate().update(UPDATE_COMM_INVITE, id, referenceID);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to update User invite reference for globalcommunicationagency";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Integer setStatusOnDelete(final Long projectID, final Integer status) {
        String INSERT_STATUS_ONDELETE = "INSERT into grailProjectDeleteStatus(projectID, status) VALUES(?, ?)";
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectDeleteStatus where projectID = ?", projectID);
            if(count==0)
            {
                getSimpleJdbcTemplate().update(INSERT_STATUS_ONDELETE, projectID, status);
            }
            else
            {
                update(projectID, status);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to insert project status while deleting into grailProjectDeleteStatus";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return status;
    }

    public void update(final Long projectID, final Integer status) {
        String UPDATE_STATUS_ONDELETE = "UPDATE grailProjectDeleteStatus SET status=? WHERE projectID = ?";
        try {
            getSimpleJdbcTemplate().update(UPDATE_STATUS_ONDELETE, status, projectID);
        } catch (DataAccessException e) {
            final String message = "Failed to update project status while deleting into grailProjectDeleteStatus";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }

    @Override
    public Integer getStatusOnDelete(final Long projectID) {

        String SELECT_STATUS_ONDELETE = "SELECT status from grailProjectDeleteStatus" +
                " where projectID = ?";
        Integer status = -1;
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectDeleteStatus where projectID = ?", projectID);
            if(count>0)
            {
                status = getSimpleJdbcTemplate().queryForInt(SELECT_STATUS_ONDELETE, projectID);
            }
            else
            {
                status = -1;
            }
        } catch (DataAccessException e) {
            final String message = "Failed to get project status while deleting from grailProjectDeleteStatus for projectID - " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        return status;

    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Integer setStatusTrack(final Long projectID, final Integer status) {
        String INSERT_STATUS = "INSERT into grailProjectStatusTrack(projectID, statusOnAction) VALUES(?, ?)";
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectStatusTrack where projectID = ? ", projectID);
            if(count==0)
            {
                getSimpleJdbcTemplate().update(INSERT_STATUS, projectID, status);
            }
            else
            {
                updateStatusTrack(projectID, status);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to insert project status track in grailProjectStatusTrack";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return status;
    }

    public void updateStatusTrack(final Long projectID, final Integer status) {
        String UPDATE_STATUS = "UPDATE grailProjectStatusTrack SET statusOnAction = ? WHERE projectID = ? ";
        try {
            getSimpleJdbcTemplate().update(UPDATE_STATUS, status, projectID);
        } catch (DataAccessException e) {
            final String message = "Failed to update project status track in grailProjectStatusTrack";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }

    @Override
    public Integer getStatusTrack(final Long projectID) {

        String GET_STATUS_TRACK = "SELECT statusOnAction from grailProjectStatusTrack" +
                " where projectID = ?";
        Integer status = -1;
        try {
            int count = getSimpleJdbcTemplate().queryForInt("SELECT count(*) from grailProjectStatusTrack where projectID = ? ", projectID);
            if(count>0)
            {
                status = getSimpleJdbcTemplate().queryForInt(GET_STATUS_TRACK, projectID);
            }
            else
            {
                status = -1;
            }
        } catch (DataAccessException e) {
            final String message = "Failed to get project status track in grailProjectStatusTrack with " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        return status;

    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteStatusTrack(final Long projectID) {
        String DELETE_STATUS_TRACK = "DELETE FROM grailProjectStatusTrack WHERE projectID = ?  ";
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_STATUS_TRACK, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the project status track for projectid - " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }


    /**
     * Investment and Funding
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public FundingInvestment setInvestment(final FundingInvestment investment) {

        try {

            int count = 0;
            if(investment.getInvestmentID()!=null && investment.getInvestmentID()>0)
            {
                count = getSimpleJdbcTemplate().queryForInt(COUNT_INVESTMENT, investment.getInvestmentID());
            }

            if(count==0)
            {
                Long id = 0L;
                if(investment.getInvestmentID() != null && investment.getInvestmentID() > 0) {
                    id = investment.getInvestmentID();
                } else {
                    id = synchroDAOUtil.nextSequenceID("investmentid", "grailfundinginvestment");
                }
                getSimpleJdbcTemplate().update(INSERT_INVESTMENT,
                        id,
                        investment.getProjectID(),
                        (investment.getAboveMarket() != null && investment.getAboveMarket())?1:0,
                        investment.getInvestmentType(),
                        investment.getInvestmentTypeID(),
                        investment.getFieldworkMarketID(),
                        investment.getFundingMarketID(),
                        investment.getProjectContact(),
                        investment.getEstimatedCost(),
                        investment.getEstimatedCostCurrency(),
                        (investment.getApproved() != null && investment.getApproved())?1:0,
                        investment.getSpiContact(),
                        investment.getApprovalStatus()==null?null:(investment.getApprovalStatus())?1:0,
                        investment.getApprovalStatus()==null?0:(investment.getApprovalStatus())?System.currentTimeMillis():0
                );

                investment.setInvestmentID(id);
            }
            else
            {
                updateInvestment(investment);
            }

        } catch (DataAccessException e) {
            final String message = "Failed to insert Investment Info in grailfundinginvestment Table";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return investment;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public FundingInvestment updateInvestment(final FundingInvestment investment) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_INVESTMENT,
                    (investment.getAboveMarket() != null && investment.getAboveMarket())?1:0,
                    investment.getInvestmentType(),
                    investment.getInvestmentTypeID(),
                    investment.getFieldworkMarketID(),
                    investment.getFundingMarketID(),
                    investment.getProjectContact(),
                    investment.getEstimatedCost(),
                    investment.getEstimatedCostCurrency(),
                    (investment.getApproved() != null && investment.getApproved())?1:0,
                    investment.getSpiContact(),
                    investment.getApprovalStatus()==null?null:(investment.getApprovalStatus())?1:0,
                    investment.getApprovalStatus()==null?0:(investment.getApprovalStatus())?System.currentTimeMillis():0,
                    investment.getInvestmentID()

            );
            return investment;
        } catch (DataAccessException e) {
            final String message = "Failed to update Investment Info in grailfundinginvestment Table";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectContact(final Long projectContact, final Long projectId, final Long endMarketId) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_PROJECT_CONTACT,
                    projectContact, projectId, endMarketId
            );

        } catch (DataAccessException e) {
            final String message = "Failed to update Project Contact for Project--"+ projectId;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateFundingInvSPIContact(final Long spiContact, final Long projectId, final Long endMarketId) {
        try {
            getSimpleJdbcTemplate().update(UPDATE_SPI_CONTACT,
                    spiContact, projectId, endMarketId
            );

        } catch (DataAccessException e) {
            final String message = "Failed to update SPI Contact for Project--"+ projectId;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public Long getPendingActivityViewCount(final ProjectResultFilter filter, final Long userId) {
        Long count = 0L;
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(generatePendingActivitiesSQL(filter, true, userId, true, false));
        }
        catch (DataAccessException e) {
           // throw new DAOException(e.getMessage(), e);
        	final String message = "Failed to fetch Pending Activity View Count for user --"+ userId;
            LOG.error(message, e);
            count = 0L;
        }
        return count;
    }

    @Override
    public void updatePendingActivityViews(final ProjectResultFilter filter, final Long userId) {
        List<Map<Long, Long>> paIds = null;
        try {
            paIds = getSimpleJdbcTemplate().getJdbcOperations().query(generatePendingActivitiesSQL(filter, false, userId, true, true), pendingActivityViewsRowmapper);
            if(paIds != null && paIds.size() > 0) {
                String updateQuery = "INSERT INTO grailPendingActivityViews (projectid, userId,activitytypeid, viewed) VALUES ";
                int i = 0;
                for(Map<Long, Long> pMap : paIds) {
                   if(i > 0) {
                       updateQuery += ",";
                   }
                    Iterator<Long> its = pMap.keySet().iterator();
                    while (its.hasNext()) {
                        Long key = its.next();
                        updateQuery += "("+key+", "+userId+","+pMap.get(key)+", 1)";
                    }

                   i++;
                }
                getSimpleJdbcTemplate().update(updateQuery);

            }
        } catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    private final ParameterizedRowMapper<Map<Long, Long>> pendingActivityViewsRowmapper = new ParameterizedRowMapper<Map<Long, Long>>() {

        public Map<Long, Long> mapRow(ResultSet rs, int row) throws SQLException {
            Map<Long, Long> pa = new HashMap<Long, Long>();
            pa.put(rs.getLong("projectId"), rs.getLong("activityTypeId"));
            return pa;
        }
    };

    @Override
    public FundingInvestment getInvestment(final Long investmentID){

        try {

            int count = getSimpleJdbcTemplate().queryForInt(COUNT_INVESTMENT, investmentID);

            if(count>0)
            {
                FundingInvestment investment = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_INVESTMENT, investmentRowMapper, investmentID);
                return investment;
            }
            else
            {
                return new FundingInvestment();
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Investment Info from grailfundinginvestment Table for investment ID " + investmentID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteInvestment(final Long investmentID){

        try {

            int count = getSimpleJdbcTemplate().queryForInt(COUNT_INVESTMENT, investmentID);

            if(count>0)
            {
                getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_INVESTMENT, investmentID);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete Investment Info from grailfundinginvestment Table for investment ID " + investmentID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteInvestment(final Long projectID,final Long endMarketId){

        try {

            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_INVESTMENTS_BY_PROJECT_ID_END_MARKET_ID, projectID,endMarketId);

        }
        catch (DataAccessException e) {
            final String message = "Failed to delete Investment Info from grailfundinginvestment Table for Project ID " + projectID +" and End Market Id --"+ endMarketId;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<FundingInvestment> getProjectInvestments(final Long projectID){

        List<FundingInvestment> investments = Collections.emptyList();
        try {
            investments = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_INVESTMENTS_BY_PROJECT, investmentRowMapper, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load  all the Investments Info for Project ID " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        return investments;
    }

    @Override
    public List<FundingInvestment> getProjectInvestments(final Long projectID, final Long endMarketId){

        List<FundingInvestment> investments = Collections.emptyList();
        try {
            investments = getSimpleJdbcTemplate().getJdbcOperations().query(LOAD_INVESTMENTS_BY_PROJECT_END_MARKET_ID, investmentRowMapper, projectID, endMarketId);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load  all the Investments Info for Project ID " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

        return investments;
    }


    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteProjectInvestments(final Long projectID){

        try {
            int count = getSimpleJdbcTemplate().queryForInt(COUNT_INVESTMENTS_BY_PROJECT, projectID);

            if(count>0)
            {
                getSimpleJdbcTemplate().getJdbcOperations().update(REMOVE_INVESTMENTS_BY_PROJECT, projectID);
            }
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete all the Investment Info from grailfundinginvestment Table for Project ID " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }

    @Override
    public List<ProjectPendingActivityViewBean> getPendingActivities(final ProjectResultFilter filter, final Long userID) {
        List<ProjectPendingActivityViewBean> pendingActivities = null;
        try {
            pendingActivities = getSimpleJdbcTemplate().getJdbcOperations().query(generatePendingActivitiesSQL(filter, false, userID, false, false)
                    , pendingActivityViewBeanParameterizedRowMapper);
        }
        catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return pendingActivities;
    }

    @Override
    public Long getPendingActivitiesTotalCount(final ProjectResultFilter filter, final Long userID) {
        Long count = 0L;
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(generatePendingActivitiesSQL(filter, true, userID, false, false));
        }
        catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return count;
    }

    /**
     *
     * @param filter
     * @param fetchTotalCount
     * @param userID
     * @return
     */
    private String generatePendingActivitiesSQL(final ProjectResultFilter filter, final boolean fetchTotalCount,
                                                final Long userID, final boolean fetchUnViewed, final boolean fetchOnlyIds) {
        StringBuilder sql = new StringBuilder();

        // Select fields
        StringBuilder fields = new StringBuilder();
        fields.append("distinct p.projectid as projectId,p.name as projectName,")
                .append("(select (ju1.firstname || ' ' || ju1.lastname) from jiveuser ju1 where ju1.userid = p.projectowner) as projectOwner,")
                .append("p.projectowner as ownerId,")
                .append("(to_char(to_timestamp(p.startdate/1000),'YYYY')::int) as startYear,")
                .append("(select b.name from grailbrandfields b where b.id = p.brand) as brandName,")
                .append("p.brand as brand,p.multimarket as multimarket,p.status");

        // Fetch PIT Details SQL
        StringBuilder pitDetailsSQL = new StringBuilder();
        pitDetailsSQL.append("SELECT ").append(fields.toString());
        pitDetailsSQL.append(", '"+ SynchroGlobal.PendingActivityStatus.PIT_APPROVAL_ON_COST_PENDING.getId()+"' as activityTypeId");
        pitDetailsSQL.append(" from grailproject p,grailfundinginvestment gfi")
                .append(" WHERE p.status = "+ SynchroGlobal.Status.PIT_OPEN.ordinal()+" AND p.multimarket = 1 AND p.projectid = gfi.projectid")
                .append(" AND gfi.estimatedcost > 0 AND (gfi.projectcontact = " + userID + " OR gfi.spicontact = " + userID + ") AND isapproved = 0");

        // Fetch PIB Details SQL
        StringBuilder pibDetailsSQL = new StringBuilder();
        pibDetailsSQL.append("select ")
                .append(fields.toString());
        pibDetailsSQL.append(", (CASE WHEN ")
                .append("(mtap.status = "+ SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal()+" AND mtap.methodologyapprover="+userID+" AND (mtap.isapproved is null or mtap.isapproved = 0))")
                .append(" THEN ").append("'"+ SynchroGlobal.PendingActivityStatus.METHODOLOGY_WAIVER_APPROVAL_PENDING.getId()+"'")
                .append(" WHEN ")
                .append("(mtap.status = "+ SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal()+" AND mtap.modificationby="+userID+" AND (mtap.isapproved is null or mtap.isapproved = 0) )")
                .append(" THEN ").append("'"+ SynchroGlobal.PendingActivityStatus.METHODOLOGY_WAIVER_MORE_INFORMATION_REQUESTED.getId()+"'")
                .append(" WHEN ")
                .append("(pib.status = "+SynchroGlobal.StageStatus.PIB_SAVED.ordinal()+")")
                .append(" THEN ")
                .append("(CASE ")
                .append(" WHEN (p.multimarket = 0 AND (select distinct emi.spicontact from grailendmarketinvestment emi where emi.projectid = p.projectid)="+userID+")")
                .append(" THEN ")
                .append("(CASE WHEN (pib.legalapprovalnotreq is null OR pib.legalapprovalnotreq = 0) AND ((pib.legalapprovalrcvd is null OR pib.legalapprovalrcvd = 0) OR (pib.legalapprover is null AND pib.legalapprover = ''))")
                .append(" THEN ").append("'"+SynchroGlobal.PendingActivityStatus.PIB_LEGAL_APPROVAL_PENDING.getId()+"'")
                .append(" ELSE ").append("'"+SynchroGlobal.PendingActivityStatus.PIB_COMPLETE_NOTIFY_AGENCY_PENDING.getId()+"'").append(" END)")
                .append(" WHEN (p.multimarket = 1)")
                .append(" THEN ")
                .append("(CASE WHEN ((pib.legalapprovalnotreq is null OR pib.legalapprovalnotreq = 0) AND ((pib.legalapprovalrcvd is null OR pib.legalapprovalrcvd = 0) OR (pib.legalapprover is null AND pib.legalapprover = '')) AND ((pib.endmarketid = -100 AND p.projectowner="+userID+") OR (pib.endmarketid > 0 AND pib.projectid = (select distinct gfi.projectid from grailfundinginvestment gfi where gfi.projectid = pib.projectid and gfi.projectcontact="+userID+" AND pib.endmarketid = gfi.fieldworkmarketid))))")
                .append(" THEN ").append("'" + SynchroGlobal.PendingActivityStatus.PIB_LEGAL_APPROVAL_PENDING.getId() + "'")
                .append(" WHEN ((pib.legalapprovalnotreq = 1 OR (pib.legalapprovalrcvd = 1 AND (pib.legalapprover is not null AND pib.legalapprover != ''))) AND p.projectowner="+userID+" AND (SELECT count(*) FROM grailpib pib1 WHERE p.projectid = pib1.projectid AND (pib1.legalapprovalnotreq is null OR pib1.legalapprovalnotreq = 0) AND ((pib1.legalapprovalrcvd is null OR pib1.legalapprovalrcvd = 0) OR (pib1.legalapprover is null AND pib1.legalapprover = '')) AND ((pib1.endmarketid = -100 AND p.projectowner="+userID+") OR (pib1.endmarketid > 0 AND pib1.projectid = (select distinct gfi.projectid from grailfundinginvestment gfi where gfi.projectid = pib1.projectid AND pib1.endmarketid = gfi.fieldworkmarketid and gfi.projectcontact="+userID+")))) <= 0)")
                .append(" THEN ").append("'" + SynchroGlobal.PendingActivityStatus.PIB_COMPLETE_NOTIFY_AGENCY_PENDING.getId() + "'")
                .append(" WHEN ").append("((select count(*) from grailfundinginvestment gfi2 where pib.projectid = gfi2.projectid and (gfi2.projectcontact = " + userID + " OR gfi2.spicontact = " + userID + ")) > 0 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.fieldworkmarketid = pib.endmarketid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = " + userID + " OR gfi1.spicontact = " + userID + ") AND gfi1.isapproved = 0 AND gfi1.projectid = (SELECT pib2.projectid FROM grailpib pib2 WHERE pib2.projectid = gfi1.projectid AND pib2.endmarketid = gfi1.fieldworkmarketid AND (pib2.legalapprovalnotreq = 1 OR (pib2.legalapprovalrcvd = 1 AND (pib2.legalapprover is not null AND pib2.legalapprover != ''))))) = (select count(*) from grailfundinginvestment gfi2 where pib.projectid = gfi2.projectid and (gfi2.projectcontact = " + userID + " OR gfi2.spicontact = " + userID + ")))")
                .append(" THEN ").append("'"+SynchroGlobal.PendingActivityStatus.PIT_APPROVAL_ON_COST_PENDING.getId()+"'")
                .append(" END)")
                .append(" END)")
                .append(" WHEN ").append("(p.multimarket = 1 AND pib.status = "+SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal()+" AND (select count(*) from grailproposal prop where prop.projectid = pib.projectid) <= 0 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.fieldworkmarketid = pib.endmarketid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = "+userID+" OR gfi1.spicontact = "+userID+") AND gfi1.isapproved = 0) > 0)")
                .append(" THEN ").append("'" + SynchroGlobal.PendingActivityStatus.PIT_APPROVAL_ON_COST_PENDING.getId() + "'")
                .append(" END) as activityTypeId");
        pibDetailsSQL.append(" from ")
                .append("grailproject p,grailpib pib left join grailpibmethodologywaiver mtap on (mtap.projectid = pib.projectid) ")
                .append("where p.projectid = pib.projectid AND ")
                .append("(CASE WHEN  ")
                .append("(mtap.status = "+ SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal()+" AND mtap.methodologyapprover="+userID+" AND (mtap.isapproved is null or mtap.isapproved = 0))")
                .append(" THEN ").append(1)
                .append(" WHEN (mtap.status = "+ SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal()+" AND mtap.modificationby="+userID+" AND (mtap.isapproved is null or mtap.isapproved = 0) )")
                .append(" THEN ").append(1)
                .append(" WHEN (p.status = "+ SynchroGlobal.Status.PIB_OPEN.ordinal()).append(")")
                .append(" THEN ")
                .append("(CASE")
                .append(" WHEN ").append("(mtap.methodologyapprover=" + userID + " AND mtap.status = " + SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal() + " AND (mtap.isapproved is null or mtap.isapproved = 0))")
                .append(" THEN ").append(1)
                .append(" WHEN ").append("(mtap.modificationby=" + userID + " AND mtap.status = " + SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal() + " AND (mtap.isapproved is null or mtap.isapproved = 0))")
                .append(" THEN ").append(1)
                .append(" WHEN (pib.status = " + SynchroGlobal.StageStatus.PIB_SAVED.ordinal() + ")")
                .append(" THEN ")
                .append("(CASE ")
                .append(" WHEN (p.multimarket = 0 AND (select distinct emi.spicontact from grailendmarketinvestment emi where emi.projectid = p.projectid)="+userID+")")
                .append(" THEN ")
                .append("(CASE WHEN (pib.legalapprovalnotreq is null OR pib.legalapprovalnotreq = 0) AND ((pib.legalapprovalrcvd is null OR pib.legalapprovalrcvd = 0) OR (pib.legalapprover is null AND pib.legalapprover = ''))")
                .append(" THEN ").append(1).append(" ELSE ").append(1).append(" END)")
                .append(" WHEN (p.multimarket = 1)")
                .append(" THEN ")
                .append("(CASE WHEN ((pib.legalapprovalnotreq is null OR pib.legalapprovalnotreq = 0) AND ((pib.legalapprovalrcvd is null OR pib.legalapprovalrcvd = 0) OR (pib.legalapprover is null AND pib.legalapprover = '')) AND ((pib.endmarketid = -100 AND p.projectowner=" + userID + ") OR (pib.endmarketid > 0 AND pib.projectid = (select distinct gfi.projectid from grailfundinginvestment gfi where gfi.projectid = pib.projectid AND pib.endmarketid = gfi.fieldworkmarketid and gfi.projectcontact=" + userID + "))))")
                .append(" THEN ").append(1)
                .append(" WHEN ((pib.legalapprovalnotreq = 1 OR (pib.legalapprovalrcvd = 1 AND (pib.legalapprover is not null AND pib.legalapprover != ''))) AND p.projectowner="+userID+" AND (SELECT count(*) FROM grailpib pib1 WHERE p.projectid = pib1.projectid AND (pib1.legalapprovalnotreq is null OR pib1.legalapprovalnotreq = 0) AND ((pib1.legalapprovalrcvd is null OR pib1.legalapprovalrcvd = 0) OR (pib1.legalapprover is null AND pib1.legalapprover = '')) AND ((pib1.endmarketid = -100 AND p.projectowner="+userID+") OR (pib1.endmarketid > 0 AND pib1.projectid = (select distinct gfi.projectid from grailfundinginvestment gfi where gfi.projectid = pib1.projectid AND pib1.endmarketid = gfi.fieldworkmarketid and gfi.projectcontact="+userID+")))) <= 0)")
                .append(" THEN ").append(1)
                .append(" WHEN ").append("((select count(*) from grailfundinginvestment gfi2 where pib.projectid = gfi2.projectid and (gfi2.projectcontact = "+userID+" OR gfi2.spicontact = "+userID+")) > 0 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.fieldworkmarketid = pib.endmarketid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = "+userID+" OR gfi1.spicontact = "+userID+") AND gfi1.isapproved = 0 AND gfi1.projectid = (SELECT pib2.projectid FROM grailpib pib2 WHERE pib2.projectid = gfi1.projectid AND pib2.endmarketid = gfi1.fieldworkmarketid AND (pib2.legalapprovalnotreq = 1 OR (pib2.legalapprovalrcvd = 1 AND (pib2.legalapprover is not null AND pib2.legalapprover != ''))))) = (select count(*) from grailfundinginvestment gfi2 where pib.projectid = gfi2.projectid and (gfi2.projectcontact = "+userID+" OR gfi2.spicontact = "+userID+")))")
                .append(" THEN ").append(1)
                .append(" ELSE 0")
                .append(" END)")
                .append(" ELSE 0")
                .append(" END)")
                .append(" ELSE 0")
                .append(" END)")
                .append(" WHEN ").append("(p.multimarket = 1 AND pib.status = " + SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal() + " AND (select count(*) from grailproposal prop where prop.projectid = pib.projectid) <= 0 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.fieldworkmarketid = pib.endmarketid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = " + userID + " OR gfi1.spicontact = " + userID + ") AND gfi1.isapproved = 0) > 0)")
                .append(" THEN ").append(1)
                .append(" ELSE 0")
                .append(" END) = 1");

        // Fetch Proposal Details SQL
        StringBuilder proposalDetailsSQL = new StringBuilder();
        proposalDetailsSQL.append("select ")
                .append(fields.toString());
        proposalDetailsSQL.append(", (CASE ")
                .append(" WHEN ").append("((prsl.issubmitted is null OR prsl.issubmitted = 0) AND prsl.isreqclarimodification = 1 AND prsl.agencyid = "+userID+")")
                .append(" THEN ").append("'"+ SynchroGlobal.PendingActivityStatus.PROPOSAL_CLARIFICATION_REQUESTED.getId()+"'")
                .append(" WHEN ").append("(prsl.issubmitted = 1)")
                .append(" THEN ").append("(CASE")
                .append(" WHEN ").append("(p.multimarket = 0 AND prsl.isreqclarimodification = 1 AND prsl.issendtoprojectowner = 1 AND prsl.spicontact = "+userID+")").append(" THEN '" + SynchroGlobal.PendingActivityStatus.PROPOSAL_CLARIFICATION_REQUESTED.getId() + "' ")
                .append(" WHEN ").append("(p.multimarket = 0 AND p.projectowner = " + userID + ")").append(" THEN '"+SynchroGlobal.PendingActivityStatus.PROPOSAL_AWARD_AGENCY_PENDING.getId()+"' ")
                .append(" WHEN ").append("(p.multimarket = 1 AND prsl.isreqclarimodification = 1 AND prsl.issendtoprojectowner = 1 AND  prsl.projectowner = "+userID+")").append(" THEN '" + SynchroGlobal.PendingActivityStatus.PROPOSAL_CLARIFICATION_REQUESTED.getId() + "' ")
                .append(" WHEN ").append("(p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = "+userID+" OR gfi1.spicontact = "+userID+") AND gfi1.isapproved = 0) > 0)")
                .append(" THEN ").append("'"+SynchroGlobal.PendingActivityStatus.PIT_APPROVAL_ON_COST_PENDING.getId()+"'")
                .append(" WHEN ").append("(p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = " + userID + " OR gfi1.spicontact = " + userID + ") AND gfi1.isapproved = 0) <= 0 AND (select gfi.projectcontact from grailfundinginvestment gfi where gfi.projectid = p.projectid order by gfi.isabovemarket desc, gfi.investmenttype, gfi.investmentid OFFSET 0 LIMIT 1) = " + userID + ")").append(" THEN '"+SynchroGlobal.PendingActivityStatus.PROPOSAL_AWARD_AGENCY_PENDING.getId()+"' ")
                .append(" END)")
                .append(" WHEN ").append("(p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = "+userID+" OR gfi1.spicontact = "+userID+") AND gfi1.isapproved = 0) > 0)")
                .append(" THEN ").append("'" + SynchroGlobal.PendingActivityStatus.PIT_APPROVAL_ON_COST_PENDING.getId() + "'")
                .append(" END) as activityTypeId");
        proposalDetailsSQL.append(" from ")
                .append("grailproject p,grailproposal prsl ")
                .append("where prsl.projectid = p.projectid and (select count(*) from grailproposal prsl1 where prsl1.projectid=prsl.projectid and prsl1.isawarded = 1) <= 0")
                .append(" AND (")// Main Braces open
                .append("(CASE ")
                .append(" WHEN ").append("((prsl.issubmitted is null OR prsl.issubmitted = 0) AND prsl.isreqclarimodification = 1 AND prsl.agencyid = " + userID + ")")
                .append(" THEN ").append(1)
                .append(" WHEN ").append("(prsl.issubmitted = 1)")
                .append(" THEN ").append("(CASE")
                .append(" WHEN ").append("(p.multimarket = 0 AND prsl.isreqclarimodification = 1 AND prsl.issendtoprojectowner = 1 AND prsl.spicontact = "+userID+")").append(" THEN 1 ")
                .append(" WHEN ").append("(p.multimarket = 0 AND p.projectowner = " + userID + ")").append(" THEN 1")
                .append(" WHEN ").append("(p.multimarket = 1 AND prsl.isreqclarimodification = 1 AND prsl.issendtoprojectowner = 1 AND prsl.projectowner = "+userID+")").append(" THEN 1 ")
                .append(" WHEN ").append("(p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = "+userID+" OR gfi1.spicontact = "+userID+") AND gfi1.isapproved = 0) > 0)")
                .append(" THEN ").append(1)
                .append(" WHEN ").append("(p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = " + userID + " OR gfi1.spicontact = " + userID + ") AND gfi1.isapproved = 0) <= 0 AND (select gfi.projectcontact from grailfundinginvestment gfi where gfi.projectid = p.projectid order by gfi.isabovemarket desc, gfi.investmenttype, gfi.investmentid OFFSET 0 LIMIT 1) = " + userID + ")").append(" THEN 1 ")
                .append(" ELSE 0 END)")
                .append(" WHEN ").append("(p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment gfi1 WHERE p.projectid = gfi1.projectid AND gfi1.estimatedcost > 0 AND (gfi1.projectcontact = "+userID+" OR gfi1.spicontact = "+userID+") AND gfi1.isapproved = 0) > 0)")
                .append(" THEN ").append(1)
                .append(" ELSE 0 END) = 1")
                .append(")"); // Main Braces close


        // Fetch Project Specs Details SQL
        StringBuilder projSpecsDetailsSQL = new StringBuilder();
        projSpecsDetailsSQL.append("select ")
                .append(fields.toString());
        projSpecsDetailsSQL.append(", (CASE WHEN ")
                .append(" (psps.issendforapproval = 1)")
                .append(" THEN ")
                .append("(CASE WHEN ")
                .append("((SELECT count(*) FROM grailprojectspecs psps1 where psps.projectid = psps1.projectid AND (psps1.legalApprovalStimulus is null OR psps1.legalApprovalStimulus = 0 OR psps1.legalApproverStimulus is null OR psps1.legalApproverStimulus = '') AND (psps1.legalApprovalScreener is null OR psps1.legalApprovalScreener = 0 OR psps1.legalApproverScreener is null OR psps1.legalApproverScreener = '') AND (psps1.legalApprovalQuestionnaire is null OR psps1.legalApprovalQuestionnaire = 0 OR psps1.legalApproverQuestionnaire is null OR psps1.legalApproverQuestionnaire = '') AND (psps1.legalApprovalDG is null OR psps1.legalApprovalDG = 0 OR psps1.legalApproverDG is null OR psps1.legalApproverDG = '') AND (psps1.legalApprovalCCCA is null OR psps1.legalApprovalCCCA = 0 OR psps1.legalApproverCCCA is null OR psps1.legalApproverCCCA = '')) > 0)")
                .append(" THEN ").append("'"+ SynchroGlobal.PendingActivityStatus.PROJECT_SPECS_LEGAL_APPROVAL_PENDING.getId()+"'")
                .append(" ELSE ").append("'" + SynchroGlobal.PendingActivityStatus.PROJECT_SPECS_APPROVAL_PENDING.getId() + "'")
                .append(" END)")
                .append(" WHEN ")
                .append("(psps.isreqclarimodification = 1 AND (psps.issendforapproval is null OR psps.issendforapproval = 0))")
                .append(" THEN ").append("'"+SynchroGlobal.PendingActivityStatus.PROJECT_SPECS_CLARIFICATION_REQUESTED.getId()+"'")
                .append(" END) as activityTypeId");
        projSpecsDetailsSQL.append(" from ")
                .append("grailproject p,grailprojectspecs psps ")
                .append("where psps.projectid=p.projectid AND (psps.isapproved is null OR psps.isapproved = 0) AND psps.status = "+SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal()+"")
                .append(" AND (")
                .append("(psps.issendforapproval = 1 AND ((p.multimarket = 0 AND psps.spicontact="+userID+") OR (p.multimarket = 1 AND psps.projectowner="+userID+")))")
                .append(" OR ")
                .append("(psps.isreqclarimodification = 1 AND (psps.issendforapproval is null OR psps.issendforapproval = 0) AND psps.projectid=(select shs.projectid from grailpibstakeholderlist shs where shs.projectid = psps.projectid AND (shs.agencycontact1="+userID+" OR shs.agencycontact2="+userID+" OR shs.agencycontact3="+userID+")))")
                .append(")");
        User user = null;
        try {
            user = getUserManager().getUser(userID);
        } catch (UserNotFoundException e) {

        }
        // Fetch Report Summary Details SQL
        StringBuilder repSummDetailsSQL = new StringBuilder();
        repSummDetailsSQL.append("select ")
                .append(fields.toString());
        repSummDetailsSQL.append(", (CASE WHEN ")
                .append(" (reps.needrevision = 1 AND reps.projectid=(select shs.projectid from grailpibstakeholderlist shs where shs.projectid = reps.projectid AND (shs.agencycontact1="+userID+" OR shs.agencycontact2="+userID+" OR shs.agencycontact3="+userID+")))")
                .append(" THEN ").append("'"+ SynchroGlobal.PendingActivityStatus.REPORT_SUMMARY_CLARIFICATION_REQUESTED.getId()+"'");
        if(user != null && SynchroPermHelper.isSynchroAdmin(user)) {
            repSummDetailsSQL.append(" WHEN ")
                    .append("(reps.isspiapproved =1 AND '"+SynchroPermHelper.isSynchroAdmin(user)+"'='true')")
                    .append(" THEN ").append("'"+SynchroGlobal.PendingActivityStatus.REPORT_SUMMARY_UPLOADED_TO_IRIS_PENDING.getId()+"'");
        }
        repSummDetailsSQL.append(" ELSE ").append("(CASE WHEN ((SELECT count(*) FROM grailprojectrepsummary reps1 WHERE reps.projectid = reps1.projectid AND (reps1.legalapproval is null OR reps1.legalapproval = 0)) > 0) THEN '"+SynchroGlobal.PendingActivityStatus.REPORT_SUMMARY_LEGAL_APPROVAL_PENDING.getId()+"' ELSE '"+SynchroGlobal.PendingActivityStatus.REPORT_SUMMARY_APPROVAL_PENDING.getId()+"' END)")
                .append(" END) as activityTypeId");
        repSummDetailsSQL.append(" from ")
                .append("grailproject p,grailprojectrepsummary reps ")
                .append("where reps.projectid = p.projectid AND reps.sendforapproval = 1 AND reps.status = "+SynchroGlobal.StageStatus.REPORT_SUMMARY_SAVED.ordinal())
                .append(" AND (")// Main Braces open
                .append("(reps.needrevision = 1 AND reps.projectid=(select shs.projectid from grailpibstakeholderlist shs where shs.projectid = reps.projectid AND (shs.agencycontact1="+userID+" OR shs.agencycontact2="+userID+" OR shs.agencycontact3="+userID+")))")
                .append(" OR ")
                        //.append("((reps.isspiapproved is null OR reps.isspiapproved =0) AND (reps.needrevision is null OR reps.needrevision=0) AND ((p.multimarket = 0 AND (select emi.spicontact from grailendmarketinvestment emi where emi.projectid = p.projectid)= "+userID+") OR (p.multimarket = 1 AND (select gfi.projectcontact from grailfundinginvestment gfi where gfi.projectid = p.projectid order by gfi.isabovemarket desc, gfi.investmenttype, gfi.investmentid OFFSET 0 LIMIT 1)="+userID+")))");
                .append("((reps.isspiapproved is null OR reps.isspiapproved =0) AND (reps.needrevision is null OR reps.needrevision=0) AND ((p.multimarket = 0 AND (select emi.spicontact from grailendmarketinvestment emi where emi.projectid = p.projectid)= "+userID+") OR (p.multimarket = 1 AND p.projectowner="+userID+")))");
        if(user != null && SynchroPermHelper.isSynchroAdmin(user)) {
            repSummDetailsSQL.append(" OR ")
                    .append("(reps.isspiapproved =1 AND '"+SynchroPermHelper.isSynchroAdmin(user)+"'='true')");
        }
        repSummDetailsSQL.append(")");// Main Braces close

        // Fetch Report Summary Details SQL
        StringBuilder projEvalDetailsSQL = new StringBuilder();
        projEvalDetailsSQL.append("select ")
                .append(fields.toString());
        projEvalDetailsSQL.append(", '"+ SynchroGlobal.PendingActivityStatus.PROJECT_EVALUATION_PENDING.getId()+"' as activityTypeId");
        projEvalDetailsSQL.append(" from ")
                .append("grailproject p ")
                .append(" where p.status=").append(SynchroGlobal.Status.COMPLETED.ordinal())
                .append(" AND (")
                .append(" p.projectowner = "+userID)
                .append(" OR ")
                .append("((select count(*) from grailpibstakeholderlist stl where stl.projectid = p.projectid and (stl.agencycontact1 = "+userID+" OR stl.agencycontact2 = "+userID+" OR stl.agencycontact3 = "+userID+" OR stl.agencycontact1optional = "+userID+" OR stl.agencycontact2optional = "+userID+" OR stl.agencycontact3optional = "+userID+" OR stl.globallegalcontact = "+userID+" OR stl.globalprocurementcontact = "+userID+" OR stl.globalcommunicationagency = "+userID+" OR stl.productcontact = "+userID+")) > 0)")
                .append(" OR ")
                .append(" (p.multimarket = 0 AND (select count(*) from grailendmarketinvestment emi where emi.projectid = p.projectid AND emi.spicontact = "+userID+") > 0 )")
                .append(" OR ")
                .append(" (p.multimarket = 1 AND (select count(*) from grailfundinginvestment gfi where gfi.projectid = p.projectid AND (gfi.projectcontact = "+userID+" OR gfi.spicontact = "+userID+")) > 0 )")
                .append(")");

        // Generate final SQL
        if(fetchTotalCount) {
            sql.append("select count(*) as count from ");
        } else if(!fetchTotalCount && fetchOnlyIds) {
            sql.append("select project.projectId as projectId,project.activityTypeId as activityTypeId from ");
        } else {
//            sql.append("SELECT project.projectId, project.projectName,project.projectOwner,project.startYear,project.brandName,project.multimarket as multimarket,project.status FROM ");
            sql.append("SELECT project.projectId, project.projectName,project.projectOwner,project.startYear,project.brandName,project.multimarket as multimarket,project.activityTypeId,project.status FROM ");
        }
        sql.append("(");
        sql.append("(").append(pitDetailsSQL.toString()).append(")");
        sql.append(" UNION ");
        sql.append("(").append(pibDetailsSQL.toString()).append(")");
        sql.append(" UNION ");
        sql.append("(").append(proposalDetailsSQL.toString()).append(")");
        sql.append(" UNION ");
        sql.append("(").append(projSpecsDetailsSQL.toString()).append(")");
        sql.append(" UNION ");
        sql.append("(").append(repSummDetailsSQL.toString()).append(")");
        sql.append(" UNION ");
        sql.append("(").append(projEvalDetailsSQL.toString()).append(")");
        sql.append(") as project");


        sql.append(applyPendingActivitiesFilter(filter, userID, fetchUnViewed));

        if(!fetchTotalCount) {
            sql.append(applyPendingActivitiesOrderByClause(filter));
            sql.append(" OFFSET ").append(filter.getStart()).append(" LIMIT ").append(filter.getLimit());
        }

        return sql.toString();
    }

    /**
     * Apply Pending Activity filters that are selected from Advanced Search
     * @param filter
     * @return
     */
    private String applyPendingActivitiesFilter(final ProjectResultFilter filter,final Long userId, final boolean fetchUnViewed) {
        // Handle conditions
        List<String> conditions = new ArrayList<String>();
        StringBuilder filterStringBuilder = new StringBuilder();

        // Keyword filter
        if(filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append("(lower(project.projectName) like '%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(filter.getKeyword())).append("%'")
                    .append(" OR ").append("lower(project.projectOwner) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("lower(project.brandName) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(")");
            conditions.add(keywordCondition.toString());
        }

        // Name filter
        if(filter.getName() != null && !filter.getName().equals("")) {
            StringBuilder nameCondition = new StringBuilder();
            nameCondition.append("lower(project.projectName) like '%").append(filter.getName().toLowerCase()).append("%'");
            conditions.add(nameCondition.toString());
        }

        // Owner filter
        if(filter.getOwnerfield() != null && filter.getOwnerfield().size() > 0) {
            StringBuilder ownerCondition = new StringBuilder();
            ownerCondition.append("project.ownerId in (").append(StringUtils.join(filter.getOwnerfield(), ",")).append(")");
            conditions.add(ownerCondition.toString());
        }


        // Brand filter
        if(filter.getBrandFields() != null && filter.getBrandFields().size() > 0) {
            StringBuilder brandCondition = new StringBuilder();
            brandCondition.append("project.brand in (")
                    .append(StringUtils.join(filter.getBrandFields(),",")).append(")");
            conditions.add(brandCondition.toString());
        }

        // Start Year filter
        if(filter.getStartYear() != null && filter.getStartYear() > 0) {
            StringBuilder startYearCondition = new StringBuilder();
            startYearCondition.append("project.startYear = ")
                    .append(filter.getStartYear());
            conditions.add(startYearCondition.toString());
        }

        // Pending Activity Status Filter
        if(filter.getProjectActivityFields() != null && filter.getProjectActivityFields().size() > 0) {
            StringBuilder pendingActivityStatusCondition = new StringBuilder();
            pendingActivityStatusCondition.append("(");
            Iterator<String> it = filter.getProjectActivityFields().iterator();
            while(it.hasNext()) {
                pendingActivityStatusCondition.append("project.activityTypeId = '"+it.next().toString()+"'");
                if(it.hasNext()) {
                    pendingActivityStatusCondition.append(" OR ");
                }
            }
            pendingActivityStatusCondition.append(")");
            conditions.add(pendingActivityStatusCondition.toString());
        }
        // Status Filter

        StringBuilder statusCondition = new StringBuilder();
        if(filter.getProjectStatusFields() != null && filter.getProjectStatusFields().size() > 0) {
            statusCondition.append(" project.status in (").append(StringUtils.join(filter.getProjectStatusFields(),",")).append(")");
            conditions.add(statusCondition.toString());
        }

        if(fetchUnViewed) {
            conditions.add("((SELECT count(*) from grailPendingActivityViews views WHERE project.projectId = views.projectId AND project.activityTypeId::int = views.activityTypeId AND views.userid = "+userId+") <= 0)");
        }



        if(conditions.size() > 0) {
            filterStringBuilder.append(" WHERE ").append(org.apache.commons.lang.StringUtils.join(conditions, " AND "));
        }

        return filterStringBuilder.toString();
    }

    /**
     * Apply order by clause for pending activities results
     * @param filter
     * @return
     */
    private String applyPendingActivitiesOrderByClause(final ProjectResultFilter filter) {
        String sortField = null;
        if(filter.getSortField() != null && !filter.getSortField().equals("")) {
            if(filter.getSortField().equals("name")) {
                sortField = "projectName";
            } else if(filter.getSortField().equals("owner")) {
                sortField = "projectOwner";
            } else if(filter.getSortField().equals("year")) {
                sortField = "startYear";
            } else if(filter.getSortField().equals("brand")) {
                sortField = "brandName";
            } else {
                sortField = "projectId";
            }
        } else {
            sortField = "projectId";
            filter.setAscendingOrder(1);
        }
        return getOrderByClause(sortField, filter.getAscendingOrder());
    }


    private final ParameterizedRowMapper<ProjectPendingActivityViewBean>  pendingActivityViewBeanParameterizedRowMapper = new ParameterizedRowMapper<ProjectPendingActivityViewBean>() {
        public ProjectPendingActivityViewBean mapRow(ResultSet rs, int row) throws SQLException {
            ProjectPendingActivityViewBean bean = new ProjectPendingActivityViewBean();
            bean.setProjectID(rs.getLong("projectId"));
            bean.setProjectName(rs.getString("projectName"));
            bean.setOwner(rs.getString("projectOwner"));
            bean.setStartYear(rs.getString("startYear"));
            bean.setBrand(rs.getString("brandName"));
            Integer activityTypeId = rs.getInt("activityTypeId");
            bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.getById(activityTypeId));
            Boolean isMultimarket = rs.getBoolean("multimarket");
            String activityLink = ProjectStage.generateURL(bean.getProjectID(), ProjectStage.getCurrentStageNumber(bean.getProjectID()));
            bean.setActivityLink(activityLink);
            return bean;
        }
    };

    private final ParameterizedRowMapper<FundingInvestment> investmentRowMapper = new ParameterizedRowMapper<FundingInvestment>() {

        public FundingInvestment mapRow(ResultSet rs, int row) throws SQLException {
            FundingInvestment investment = new FundingInvestment();
            investment.setInvestmentID(rs.getLong("investmentid"));
            investment.setProjectID(rs.getLong("projectid"));
            investment.setAboveMarket(rs.getBoolean("isabovemarket"));
            investment.setInvestmentType(rs.getLong("investmenttype"));
            investment.setInvestmentTypeID(rs.getLong("investmenttypeid"));
            investment.setFieldworkMarketID(rs.getLong("fieldworkmarketid"));
            investment.setFundingMarketID(rs.getLong("fundingmarketid"));
            investment.setProjectContact(rs.getLong("projectcontact"));
            investment.setEstimatedCost(rs.getBigDecimal("estimatedcost"));
            investment.setEstimatedCostCurrency(rs.getLong("estimatedcostcurrency"));
            investment.setApproved(rs.getBoolean("isapproved"));
            investment.setSpiContact(rs.getLong("spicontact"));
            if(rs.getObject("approvalStatus")!=null)
            {
                investment.setApprovalStatus(rs.getBoolean("approvalStatus"));
            }
            else
            {
                investment.setApprovalStatus(null);
            }
            if(rs.getLong("approvaldate") > 0) {
            	investment.setApprovalDate(new Date(rs.getLong("approvaldate")));
            }
            

            return investment;
        }
    };

    private static UserManager userManager;
    public static UserManager getUserManager() {
        if(userManager == null) {
            return JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;

    }
}
