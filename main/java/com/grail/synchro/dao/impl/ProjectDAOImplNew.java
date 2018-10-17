package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.InvitedUser;
import com.grail.synchro.beans.InvitedUserResultFilter;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectPendingActivityViewBean;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.beans.ProjectStatus;
import com.grail.synchro.beans.ProjectTemplate;
import com.grail.synchro.beans.QPRProjectSnapshot;
import com.grail.synchro.beans.SpendReportExtractFilter;
import com.grail.synchro.dao.ProjectDAONew;
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
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.lifecycle.JiveApplication;
import java.math.BigDecimal;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class ProjectDAOImplNew extends SynchroAbstractDAO implements ProjectDAONew {
    private static final Logger LOG = Logger.getLogger(ProjectDAOImplNew.class);
    private CommunityManager communityManager;

    private static final String PROJECT_FIELDS =  "projectID, name, description, descriptiontext, categoryType, brand, methodologyType, " +
            " methodologyGroup, proposedMethodology, startDate, endDate, projectOwner, briefCreator, multiMarket, totalCost, totalCostCurrency, " +
            " creationby, modificationby, creationdate, modificationdate, status, caprating, isconfidential, region, area, issave, budgetyear,agencyDept, projectsavedate, projectstartdate, " +
            " methodologyDetails, projecttype, processtype, fieldworkstudy, methwaiverreq, brandspecificstudy, studytype, budgetlocation, projectmanager, newsynchro, refsynchrocode, iscancel, endmarketfunding, globalprojectoutcome, globaloutcomeeushare, multibrandstudytext, onlyglobaltype, ismigrated, hasnewsynchrosaved";

    private static final String PROJECT_FIELDS_NEW =  "projectID, name, description, descriptiontext, categoryType, brand, methodologyType, " +
            " methodologyGroup, methodologyDetails, startDate, endDate, projectOwner, briefCreator, totalCost, totalCostCurrency, " +
            " creationby, modificationby, creationdate, modificationdate, projecttype, processtype, fieldworkstudy, methwaiverreq, brandspecificstudy, studytype, budgetlocation, budgetyear, status";

    
    private static final String INSERT_PROJECT = "INSERT INTO grailproject( " +PROJECT_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?, ?, ?, ?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_PROJECT_COST = "INSERT INTO grailprojectcostdetails(projectid, agencyid, costcomponent, costcurrency, estimatedcost )" +
            " VALUES (?, ?, ?, ?, ?);";
    
    private static final String GET_PROJECT_COST = "SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE projectid = ?;";
    
    private static final String GET_PROJECT_COST_AGENCY = "SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE projectid = ? and agencyid=?;";
    
    private static final String DELETE_PROJECT_COST = "DELETE FROM grailprojectcostdetails WHERE projectid = ?";
    
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
    
    private static final String UPDATE_PROJECT_NEW = "UPDATE grailproject SET name=?,description=?, descriptiontext=?,categoryType=?,brand=?, " +
    		"methodologyType=?, methodologyDetails=?, startDate=?, endDate=?, modificationby=?, modificationdate=?, fieldworkstudy=?," +
    		" methwaiverreq=?, brandspecificstudy=?, studytype=?, processtype=?, budgetlocation=?, budgetyear=?, status=?, projectmanager=?, refsynchrocode=?, " +
    		"endmarketfunding=?, globalprojectoutcome=?, globaloutcomeeushare=?, multibrandstudytext=?  WHERE projectid = ?";
    
    
    private static final String UPDATE_PROJECT_NEW_MIGRATE = "UPDATE grailproject SET name=?,description=?, descriptiontext=?,categoryType=?,brand=?, " +
    		"methodologyType=?, methodologyDetails=?, methodologygroup=?, startDate=?, endDate=?, modificationby=?, modificationdate=?, fieldworkstudy=?," +
    		" methwaiverreq=?, brandspecificstudy=?, studytype=?, multibrandstudytext=?, processtype=?, projecttype=?, budgetlocation=?, budgetyear=?, status=?, projectmanager=?, refsynchrocode=?, " +
    		"endmarketfunding=?, globalprojectoutcome=?, totalCost=?, onlyglobaltype=?, ismigrated=?  WHERE projectid = ?";

    private static final String UPDATE_PROJECT_NEW_FROM_STAGES = "UPDATE grailproject SET startDate=?, endDate=?, modificationby=?, modificationdate=?, " +
    		" methwaiverreq=?, refsynchrocode=?, endmarketfunding=?, hasnewsynchrosaved=?  WHERE projectid = ?";
    
    private static final String UPDATE_PROJECT_TOTALCOST = "UPDATE grailproject" +
            " SET totalCost=?, totalCostCurrency=? " +
            " WHERE projectid = ?";

    private static final String UPDATE_PROJECT_OWNER = "UPDATE grailproject SET projectOwner=? WHERE projectID = ?";


    private static final String LOAD_PROJECT = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ? order by creationdate DESC";
    
    private static final String LOAD_PROJECT_NAME_BUDGLOCATION = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ? and p.name = ? and p.budgetlocation = ? and p.iscancel=0 order by creationdate DESC";

    private static final String ALL_PROJECTS = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + ",(CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact FROM grailproject p order by p.creationdate DESC";
    
    private static final String ALL_PROJECTS_NEW = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p order by creationdate DESC";
    
    private static final String ALL_PROJECTS_QPR = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") +" FROM grailproject p where p.iscancel=0 and p.status >0 order by p.creationdate DESC";
    
    private static final String BUDGET_YEAR_PROJECTS_QPR = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") +" FROM grailproject p where p.iscancel=0 and p.status >0 and p.budgetyear=? and p.newsynchro = 1 order by p.creationdate DESC";

    private static final String PROJECTS_BY_USER = "SELECT "+ prependAlias(PROJECT_FIELDS, "p") + ",(CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact FROM grailproject p where p.ownerid = ? order by p.creationdate DESC";

    private static final String DELETE_PROJECT = "DELETE FROM grailproject WHERE projectid = ?";

    private static final String GET_PROJECT_CREATOR_ID = "SELECT creationby FROM grailproject where projectid = ? ";




    private static final String UPDATE_PROJECT_STATUS = "UPDATE grailProjectStatus SET status=?, modificationby=?, modificationdate=? WHERE projectid = ? AND endMarketID = ?";
    private static final String UPDATE_PROJECT_STATUS_PROJECT_ID = "UPDATE grailproject SET status=? WHERE projectid = ?";
    
    private static final String UPDATE_PROJECT_NEW_SYNCHRO_PROJECT_ID = "UPDATE grailproject SET newsynchro=? WHERE projectid = ?";
    
    private static final String UPDATE_PROJECT_CATEGORY = "UPDATE grailproject SET categorytype=? WHERE projectid = ?";
    private static final String UPDATE_PROJECT_PIT = "UPDATE grailproject SET categorytype=?, proposedMethodology=?, name=?, isconfidential=?, budgetyear=? WHERE projectid = ?";
    private static final String UPDATE_PROJECT_PIT_NEW = "UPDATE grailproject SET name=?,description=?, descriptiontext=?,categoryType=?,brand=?, methodologyType=?, methodologyDetails=?, startDate=?, endDate=?, modificationby=?, modificationdate=?, fieldworkstudy=?, methwaiverreq=?, brandspecificstudy=?, studytype=?, budgetlocation=?, budgetyear=?, projectmanager=?, refsynchrocode=?, endmarketfunding=?, multibrandstudytext=?, hasnewsynchrosaved=?  WHERE projectid = ?";
    private static final String UPDATE_PROJECT_MW_NEW = "UPDATE grailproject SET  methwaiverreq=?  WHERE projectid = ?";
    
    private static final String CANCEL_PROJECT = "UPDATE grailproject SET iscancel=? WHERE projectid = ?";
   
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
    
    private static final String GET_ALL_BUDGET_LOCATIONS = "SELECT distinct(budgetlocation) FROM grailproject  where iscancel=0 and status >0 and budgetyear=? and budgetlocation is not null ";
    private static final String GET_ALL_METHODOLOGIES = "SELECT distinct(methodologydetails) FROM grailproject  where iscancel=0 and status >0 and budgetyear=? ";
    
    private static final String GET_ALL_CATEGORY_TYPES = "SELECT distinct(categorytype) FROM grailproject  where iscancel=0 and status >0 and budgetyear=? ";
    
    private static final String GET_ALL_BRANDS = "SELECT distinct(brand) FROM grailproject where iscancel=0 and status >0 and budgetyear=? and brandspecificstudy = 1 and brand is not null";
    private static final String GET_ALL_NON_BRANDS = "SELECT distinct(studytype) FROM grailproject where iscancel=0 and status >0 and budgetyear=? and brandspecificstudy = 2 and studytype is not null";
    
    private static final String GET_SPEND_BY_BUDGET_LOCATION_LATEST_COST = "select budgetlocation, sum(totalcost) as totalcost from grailproject where budgetyear=? and budgetlocation=? and iscancel=0 and status >0  group by budgetlocation ";
    
    private static final String GET_SPEND_BY_METHODOLOGY_LATEST_COST = "select methodologydetails, sum(totalcost) as totalcost from grailproject where budgetyear=? and methodologydetails=? and iscancel=0 and status >0  group by methodologydetails ";
    private static final String GET_SPEND_BY_BRAND_LATEST_COST = "select brand, sum(totalcost) as totalcost from grailproject where budgetyear=? and brand=? and iscancel=0 and status >0 and brandspecificstudy = 1 group by brand ";
    
    private static final String GET_SPEND_BY_NON_BRAND_LATEST_COST = "select studytype, sum(totalcost) as totalcost from grailproject where budgetyear=? and studytype=? and iscancel=0 and status >0 and brandspecificstudy = 2 group by studytype ";
    
    private static final String GET_ALL_AGENCIES = "SELECT distinct(agencyid) FROM grailprojectcostdetails where projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear=? ";
    
    private static final String GET_SPEND_BY_AGENCY_LATEST_COST = "SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = ? and projectid in (select p.projectid from grailproject p where p.iscancel=0 and p.status >0 and p.budgetyear=? );";
    
    private static final String GET_SPEND_BY_AGENCY_TYPE_LATEST_COST = "SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE projectid in (select p.projectid from grailproject p where p.iscancel=0 and p.status >0 and p.budgetyear=? ) and agencyid in (select agency.id from grailresearchagency agency, grailresearchagencymapping mapping where agency.id = mapping.researchagencyid and mapping.researchagencygroupid = ? );";

    private SynchroDAOUtil synchroDAOUtil;

    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public Project create(final Project project) {

        try {
            // update audit fields
            updateAuditFields(project, false);
            //Long id = syisnchroDAOUtil.generateProjectID();
            Long id = synchroDAOUtil.nextSequenceID("projectid", "grailproject");
            LOG.info("Project next sequence - " + id);
            project.setProjectID(id);
          
            getSimpleJdbcTemplate().update(INSERT_PROJECT,
                    id,
                    project.getName(),
                    project.getDescription(),
                    project.getDescriptionText(),
                    project.getCategoryType()!=null?Joiner.on(",").join(project.getCategoryType()):"",
                    project.getBrand()!=null?project.getBrand():-1,
                    project.getMethodologyType(),
                    project.getMethodologyGroup()!=null?project.getMethodologyGroup():-1,
                    project.getProposedMethodology()!=null?Joiner.on(",").join(project.getProposedMethodology()):null,
                    project.getStartDate()!=null?project.getStartDate().getTime():-1,
                    project.getEndDate()!=null?project.getEndDate().getTime():-1,
                    project.getProjectOwner()!=null?project.getProjectOwner():-1,
                    project.getBriefCreator(),
                    (project.getMultiMarket() != null && project.getMultiMarket())?1:0,
                    project.getTotalCost()!=null?project.getTotalCost():0,
                    project.getTotalCostCurrency()!=null?project.getTotalCostCurrency():-1,
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
                    (project.getStatus().intValue()==SynchroGlobal.Status.DRAFT.ordinal())?null:project.getModifiedDate(),
                    // New Synchro Fields Start
                    (project.getMethodologyDetails()!=null&&project.getMethodologyDetails().size()>0 && project.getMethodologyDetails().get(0)!=null)?Joiner.on(",").join(project.getMethodologyDetails()):null,
                    project.getProjectType(),
                    project.getProcessType(),
                    project.getFieldWorkStudy(),
                    project.getMethWaiverReq(),
                    project.getBrandSpecificStudy(),
                    project.getBrandSpecificStudyType(),
                    project.getBudgetLocation(),
                    project.getProjectManagerName(),1, 
                    (project.getRefSynchroCode() != null && project.getRefSynchroCode()>0)?project.getRefSynchroCode():null, 0,
                    project.getEndMarketFunding(), project.getEuMarketConfirmation(), project.getGlobalOutcomeEUShare(), project.getMultiBrandStudyText(),
                    project.getOnlyGlobalType(),
                    //project.getIsMigrated(),
                    (project.getIsMigrated() != null && project.getIsMigrated())?1:null,
                    (project.getHasNewSynchroSaved() != null && project.getHasNewSynchroSaved())?1:0
  
            );
           /* getSimpleJdbcTemplate().update(INSERT_PROJECT,
                    id,
                    project.getName(),
                    project.getDescription(),
                    project.getDescriptionText(),
                    project.getCategoryType()!=null?Joiner.on(",").join(project.getCategoryType()):"",
                    project.getBrand(),
                    project.getMethodologyType()!=null?project.getMethodologyType():1,
               		project.getMethodologyGroup()!=null?project.getMethodologyGroup():1,
                    project.getMethodologyDetails()!=null?Joiner.on(",").join(project.getMethodologyDetails()):null,
                    project.getStartDate()!=null?project.getStartDate().getTime():-1,
                    project.getEndDate()!=null?project.getEndDate().getTime():-1,
                    project.getProjectOwner()!=null?project.getProjectOwner():-1,
                    project.getBriefCreator(),
                    project.getTotalCost(),
                    project.getTotalCostCurrency(),
                    project.getCreationBy(),
                    project.getModifiedBy(),
                    project.getCreationDate(),
                    project.getModifiedDate(),
                    project.getProjectType(),
                    project.getProcessType(),
                    project.getFieldWorkStudy(),
                    project.getMethWaiverReq(),
                    project.getBrandSpecificStudy(),
                    project.getBrandSpecificStudyType(),
                    project.getBudgetLocation(),
                    project.getBudgetYear(),
                    project.getStatus()
                    		
                    		

            );*/
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

           /* getSimpleJdbcTemplate().update(UPDATE_PROJECT,
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

                    */
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_NEW,
                    project.getName(),
                    project.getDescription(),
                    project.getDescriptionText(),
                    project.getCategoryType()!=null?Joiner.on(",").join(project.getCategoryType()):"",
                    project.getBrand()!=null?project.getBrand():-1,
                    project.getMethodologyType(),
                   // project.getMethodologyDetails()!=null?Joiner.on(",").join(project.getMethodologyDetails()):"",
                    (project.getMethodologyDetails()!=null&&project.getMethodologyDetails().size()>0 && project.getMethodologyDetails().get(0)!=null)?Joiner.on(",").join(project.getMethodologyDetails()):"",
                    project.getStartDate()!=null?project.getStartDate().getTime():-1,
                    project.getEndDate()!=null?project.getEndDate().getTime():-1,
                  //  project.getProjectOwner(),
                    project.getModifiedBy(),
                    project.getModifiedDate(),
                    project.getFieldWorkStudy(),
                    project.getMethWaiverReq(),
                    project.getBrandSpecificStudy(),
                    project.getBrandSpecificStudyType(),
                    project.getProcessType(),
                    project.getBudgetLocation(),
                    project.getBudgetYear(),
                    project.getStatus(),
                    project.getProjectManagerName(),
                    project.getRefSynchroCode(),
                    project.getEndMarketFunding(),
                   // project.getFundingMarkets(),
                    project.getEuMarketConfirmation(),
                    project.getGlobalOutcomeEUShare(),
                    project.getMultiBrandStudyText(),
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
    public Project updateProjectMigrate(final Project project) {
        try {
            // update audit fields
            updateAuditFields(project, true);

           
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_NEW_MIGRATE,
                    project.getName(),
                    project.getDescription(),
                    project.getDescriptionText(),
                    project.getCategoryType()!=null?Joiner.on(",").join(project.getCategoryType()):"",
                    project.getBrand()!=null?project.getBrand():-1,
                    project.getMethodologyType(),
                   // project.getMethodologyDetails()!=null?Joiner.on(",").join(project.getMethodologyDetails()):"",
                    (project.getMethodologyDetails()!=null&&project.getMethodologyDetails().size()>0 && project.getMethodologyDetails().get(0)!=null)?Joiner.on(",").join(project.getMethodologyDetails()):"",
                    project.getMethodologyGroup(),
                    project.getStartDate()!=null?project.getStartDate().getTime():-1,
                    project.getEndDate()!=null?project.getEndDate().getTime():-1,
                  //  project.getProjectOwner(),
                    project.getModifiedBy(),
                    project.getModifiedDate(),
                    project.getFieldWorkStudy(),
                    project.getMethWaiverReq(),
                    project.getBrandSpecificStudy(),
                    project.getBrandSpecificStudyType(),
                    project.getMultiBrandStudyText(),
                    project.getProcessType(),
                    project.getProjectType(),
                    project.getBudgetLocation(),
                    project.getBudgetYear(),
                    project.getStatus(),
                    project.getProjectManagerName(),
                    project.getRefSynchroCode(),
                    project.getEndMarketFunding(),
                   // project.getFundingMarkets(),
                    project.getEuMarketConfirmation(),
                    project.getTotalCost(),
                    project.getOnlyGlobalType(),
                    1,
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
    public void updateProjectNew(final Project project){
    	try {
           
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_NEW_FROM_STAGES,
                
                    project.getStartDate()!=null?project.getStartDate().getTime():null,
                    project.getEndDate()!=null?project.getEndDate().getTime():null,
                    project.getModifiedBy(),
                    project.getModifiedDate(),
                    project.getMethWaiverReq(),
                    project.getRefSynchroCode(),
                    project.getEndMarketFunding(),
                    (project.getHasNewSynchroSaved() != null && project.getHasNewSynchroSaved())?1:0,
                     // project.getFundingMarkets(),
                    project.getProjectID()
          
                    
            );
        
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the project details.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    	
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void saveProjectCostDetails(final ProjectCostDetailsBean projectCostBean) {

        try {
           
           
            getSimpleJdbcTemplate().update(INSERT_PROJECT_COST,
            		projectCostBean.getProjectId(),
            		projectCostBean.getAgencyId(),
            		projectCostBean.getCostComponent(),
            		projectCostBean.getCostCurrency(),
            		projectCostBean.getEstimatedCost()
       
            );
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to create save Project Cost Details" + projectCostBean.getProjectId();
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    @Override
    public List<ProjectCostDetailsBean> getProjectCostDetails(final Long projectId) {

        try {
           
           
        	 return getSimpleJdbcTemplate().query(GET_PROJECT_COST, projectCostDetailRowMapper, projectId);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to get Project Cost Details" + projectId;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    @Override
    public List<ProjectCostDetailsBean> getProjectCostDetails(final Long projectId, final Long agencyId) {

        try {
           
           
        	 return getSimpleJdbcTemplate().query(GET_PROJECT_COST_AGENCY, projectCostDetailRowMapper, projectId, agencyId);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to get Project Cost Details" + projectId;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void deleteProjectCostDetails(final Long projectID) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_PROJECT_COST, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to delete the project Cost Details for Project --  " + projectID;
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
            final String message = "Failed to update ownerID for project " + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    @Override
    @Transactional(readOnly = false, propagation = Propagation.MANDATORY)
    public void updateProjectTotalCost(final Long projectID, final BigDecimal totalCost, final int totalCostCurrency) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_TOTALCOST, totalCost, totalCostCurrency, projectID);
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Total Cost for project " + projectID;
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
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    }
    
    public Project get(final Long projectID, final String projectName, final Integer budgetLocation){
    	Project project=null;
    	try {
    		// Fetch the cost for only active projects
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(LOAD_PROJECT_NAME_BUDGLOCATION, projectRowMapper, projectID, projectName, budgetLocation);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    public ProjectCostDetailsBean getProjectsBrandAgency(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer brand, String brandType, String categoryType, Long agency, String agencyType, Integer budgetYear, SpendReportExtractFilter spendReportFilter)
    {
    	List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }
    	
    	
    	
    	ProjectCostDetailsBean projects = null;
    	
    	
    	
    	StringBuilder whereClauseSql = new StringBuilder();
    	
    	whereClauseSql.append(" WHERE agencyid = "+agency.toString());
    	
    	//Adding the Agency Type Mapping
    	whereClauseSql.append(" AND agencyid IN (select agency.id from grailresearchagency agency, grailresearchagencygroup agencygroup, grailresearchagencymapping map where agency.id= "+agency.toString()+" and agencygroup.name = '"+agencyType+"' and map.researchagencyid = agency.id and map.researchagencygroupid = agencygroup.id) ");
    	
    	//sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());

    	//sql.append(applySpendReportFilter(spendReportFilter));
    	//sql.append(" )");
    	
    	  whereClauseSql.append(" and projectid IN (select projectid from grailproject ");
	    	
	    	boolean flag= false;
	    	if(projectID!=null && projectID.intValue() > 0)
	    	{
	    		whereClauseSql.append(" where projectid ="+projectID.toString());
	    		flag= true;
	    	}
	    	if(projectName!=null)
	    	{
		    	if(flag)
		    	{
		    		whereClauseSql.append(" and name ='");
		    		//whereClauseSql.append(projectName+"'");
		    		whereClauseSql.append(SynchroUtils.removeQuotes(projectName)+"'");
		    	}
		    	else
		    	{
		    		whereClauseSql.append(" where name ='");
		    		//whereClauseSql.append(projectName+"'");
		    		whereClauseSql.append(SynchroUtils.removeQuotes(projectName)+"'");
			    	flag= true;
		    	}
		    	
	    	}
	    	
	    	//Adding Budget Location Fields
	    	if(budgetLocation!=null)
	    	{
		    	
		    	if(flag)
		    	{
		    		whereClauseSql.append(" and budgetlocation = ");
		    		whereClauseSql.append(budgetLocation.toString());
		    	}
		    	else
		    	{
		    		whereClauseSql.append(" where budgetlocation = ");
		    		whereClauseSql.append(budgetLocation.toString());
			    	flag= true;
		    	}
	    	}
	    	
	    	if(region!=null)
	    	{
		    	if(StringUtils.isNotBlank(region))
		    	{
		    		whereClauseSql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
		    	}
	    	}
		    
	    	if(area!=null)
	    	{
		    	if(StringUtils.isNotBlank(area))
		    	{
		    		whereClauseSql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
		    	}
	    	}
	    	
	    	if(t20_t40!=null)
	    	{
		    	if(StringUtils.isNotBlank(t20_t40))
		    	{
		    		int t20_40 = -1;
		    		if(t20_t40.equalsIgnoreCase("T20"))
		    		{
		    			t20_40 = 1;
		    		}
		    		if(t20_t40.equalsIgnoreCase("T40"))
		    		{
		    			t20_40 = 2;
		    		}
		    		if(t20_t40.equalsIgnoreCase("Non-T40"))
		    		{
		    			t20_40 = 3;
		    		}
		    		whereClauseSql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
		    	}
	    	}
		    
	    	if(methodology!=null)
	    	{
		    	// Adding Methodology Fields
		    	
		     	
		     	if(flag)
		    	{
		     		whereClauseSql.append(" and methodologydetails='");
		     		whereClauseSql.append(methodology.toString()+"'");
		    	}
		    	else
		    	{
		    		whereClauseSql.append(" where methodologydetails='");
		    		whereClauseSql.append(methodology.toString()+"'");
			    	flag= true;
		    	}
		     	
		     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
		     	
		     	if(methIds!=null && methIds.size()>0)
		 	    {
		     		whereClauseSql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
		            if(methIds.size()>0)
		            {
		            	whereClauseSql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
		            	whereClauseSql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
		         	    whereClauseSql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
		                	for(int i=1;i<methIds.size();i++)
		                	{
		                		whereClauseSql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
		                		whereClauseSql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
		                		whereClauseSql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
		                		whereClauseSql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
		                	}
		            }
		            whereClauseSql.append(")");
		 	    }
	    	}
		     
	    	if(brand!=null)
	    	{
		     	int bType = -1;
		    	if(brandType!=null && brandType.equals("GDB"))
		    	{
		    		bType = 1;
		    	}
		    	else
		    	{
		    		bType = 2;
		    	}
		     	
		    	if(flag)
		    	{
		    		whereClauseSql.append(" and brand=");
		    		whereClauseSql.append(brand.toString());
		    	}
		    	else
		    	{
		    		whereClauseSql.append(" where brand=");
		    		whereClauseSql.append(brand.toString());
			    	flag= true;
		    	}
		    	
		     	
		    	whereClauseSql.append(" and brandspecificstudy = 1 ");
		    	
		    	whereClauseSql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
	    	}
	    	
	    	if(categoryType!=null)
	    	{
		    	// Adding Category Type Field
		    	
		    	
		    	if(flag)
		    	{
		    		whereClauseSql.append(" and categoryType='");
		    		whereClauseSql.append(categoryType.toString()+"'");
		    	}
		    	else
		    	{
		    		whereClauseSql.append(" where categoryType='");
		    		whereClauseSql.append(categoryType.toString()+"'");
			    	flag= true;
		    	}
	    	}
	    	
	    	if(flag)
	    	{
	    		whereClauseSql.append(" and iscancel=0 and newsynchro = 1 and status >0 and budgetYear="+budgetYear.toString());
	    	}
	    	else
	    	{
	    		whereClauseSql.append(" where iscancel=0 and newsynchro = 1 and status >0 and budgetYear="+budgetYear.toString());
	    	}
	    	
	    	// This is done for applying the filter for Cross tab as it was giving issue in case there is No SpendBy Project and Spend By Budget location is selected for Crosstab
	    	whereClauseSql.append(applySpendReportFilter(spendReportFilter));

	    	
	    	whereClauseSql.append(")");
    		
	    	
	    	
	    	 Long count = 0L;
	         StringBuilder fetchCountSql = new StringBuilder();
	         fetchCountSql.append("SELECT count(*) from grailprojectcostdetails ");
	         fetchCountSql.append(whereClauseSql);
	        
	         try {
	             count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(fetchCountSql.toString());
	         }
	         catch (DataAccessException e) {
	             final String message = "Failed to fetch count for CrossTab with Agency";
	             LOG.error(message, e);
	             
	         }
	         
	         
	         StringBuilder sql = new StringBuilder();
		     sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails ");
		     sql.append(whereClauseSql);
		    	
	         // If there are more rows for each unique key then we need to fetch the cost differently
	         if(count.intValue() > 1)
	         {
	        	List<ProjectCostDetailsBean> projectCostBeanList = Collections.emptyList();
	          	try {
	          			projectCostBeanList = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
	          			if(projectCostBeanList!=null && projectCostBeanList.size()>1)
	          			{
	          				
	          				BigDecimal totalCostGBP = new BigDecimal("0");
	          				for(ProjectCostDetailsBean pcb:projectCostBeanList )
	          				{
	          					if(pcb.getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1))
	        		    		{
	          						totalCostGBP = totalCostGBP.add(pcb.getEstimatedCost());
	        		    		}
	        		    		else
	        		    		{
	        		    			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(pcb.getCostCurrency())) * (pcb.getEstimatedCost().doubleValue()));
	        		    			totalCostGBP =  totalCostGBP.add(gbpEstimatedCost);
	        		    		}
	          				}
		          			projects = new ProjectCostDetailsBean();	
	          				projects.setEstimatedCost(totalCostGBP);
	          				projects.setCostCurrency(JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1));
	          			}
	          			
	          			
	              }
	              catch (DataAccessException e) {
	                  final String message = "Failed to MethId for Group";
	                  LOG.error(message, e);
	                 // throw new DAOException(message, e);
	              }
	         }
	         else
	         {
    	    	
		   
		        try {
		           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
		        	 projects = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectCostDetailRowMapper);
		        }
		        catch (DataAccessException e) {
		            final String message = "Failed to Spend By Latest Cost for Agency ==>"+ agency + "and Project id==>"+ projectID;
		            LOG.error(message, e);
		           // throw new DAOException(message, e);
		        }
	         }
        return projects;
    	
    	
    }

    public ProjectCostDetailsBean getProjectsNonBrandAgency(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType, Long agency, String agencyType, Integer budgetYear, SpendReportExtractFilter spendReportFilter)
    {
    	List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }
    	
    	
 		ProjectCostDetailsBean projects = null;
 	/*	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	*/
 		
    	StringBuilder whereClauseSql = new StringBuilder();
    	whereClauseSql.append(" WHERE agencyid = "+agency.toString());
    	
    	//Adding the Agency Type Mapping
    	whereClauseSql.append(" AND agencyid IN (select agency.id from grailresearchagency agency, grailresearchagencygroup agencygroup, grailresearchagencymapping map where agency.id= "+agency.toString()+" and agencygroup.name = '"+agencyType+"' and map.researchagencyid = agency.id and map.researchagencygroupid = agencygroup.id) ");
    	
    	
    	whereClauseSql.append(" and projectid IN (select projectid from grailproject ");
    	
    	boolean flag= false;
    	if(projectID!=null && projectID.intValue() > 0)
    	{
    		whereClauseSql.append(" where projectid ="+projectID.toString());
    		flag= true;
    	}
    	if(projectName!=null)
    	{
	    	if(flag)
	    	{
	    		whereClauseSql.append(" and name ='");
	    		//whereClauseSql.append(projectName+"'");
	    		whereClauseSql.append(SynchroUtils.removeQuotes(projectName)+"'");
	    	}
	    	else
	    	{
	    		whereClauseSql.append(" where name ='");
	    		//whereClauseSql.append(projectName+"'");
	    		whereClauseSql.append(SynchroUtils.removeQuotes(projectName)+"'");
		    	flag= true;
	    	}
	    	
    	}
    	
    	//Adding Budget Location Fields
    	if(budgetLocation!=null)
    	{
	    	
	    	if(flag)
	    	{
	    		whereClauseSql.append(" and budgetlocation = ");
	    		whereClauseSql.append(budgetLocation.toString());
	    	}
	    	else
	    	{
	    		whereClauseSql.append(" where budgetlocation = ");
	    		whereClauseSql.append(budgetLocation.toString());
		    	flag= true;
	    	}
    	}
    	
    	if(region!=null)
    	{
	    	if(StringUtils.isNotBlank(region))
	    	{
	    		whereClauseSql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
	    	}
    	}
	    
    	if(area!=null)
    	{
	    	if(StringUtils.isNotBlank(area))
	    	{
	    		whereClauseSql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
	    	}
    	}
    	
    	if(t20_t40!=null)
    	{
	    	if(StringUtils.isNotBlank(t20_t40))
	    	{
	    		int t20_40 = -1;
	    		if(t20_t40.equalsIgnoreCase("T20"))
	    		{
	    			t20_40 = 1;
	    		}
	    		if(t20_t40.equalsIgnoreCase("T40"))
	    		{
	    			t20_40 = 2;
	    		}
	    		if(t20_t40.equalsIgnoreCase("Non-T40"))
	    		{
	    			t20_40 = 3;
	    		}
	    		whereClauseSql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
	    	}
    	}
	    
    	if(methodology!=null)
    	{
	    	// Adding Methodology Fields
	    	
	     	
	     	if(flag)
	    	{
	     		whereClauseSql.append(" and methodologydetails='");
	     		whereClauseSql.append(methodology.toString()+"'");
	    	}
	    	else
	    	{
	    		whereClauseSql.append(" where methodologydetails='");
	    		whereClauseSql.append(methodology.toString()+"'");
		    	flag= true;
	    	}
	     	
	     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
	     	
	     	if(methIds!=null && methIds.size()>0)
	 	    {
	     		whereClauseSql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
	            if(methIds.size()>0)
	            {
	            	whereClauseSql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
	            	whereClauseSql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
	            	whereClauseSql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
	                	for(int i=1;i<methIds.size();i++)
	                	{
	                		whereClauseSql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
	                		whereClauseSql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
	                		whereClauseSql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
	                		whereClauseSql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
	                	}
	            }
	            whereClauseSql.append(")");
	 	    }
    	}
	     
    	if(nonBrand!=null)
    	{
	     	int bType = -1;
	    	if(brandType!=null && brandType.equals("GDB"))
	    	{
	    		bType = 1;
	    	}
	    	else
	    	{
	    		bType = 2;
	    	}
	     	
	    	if(flag)
	    	{
	    		whereClauseSql.append(" and studytype=");
	    		whereClauseSql.append(nonBrand.toString());
	    	}
	    	else
	    	{
	    		whereClauseSql.append(" where studytype=");
	    		whereClauseSql.append(nonBrand.toString());
		    	flag= true;
	    	}
	    	
	     	
	    	whereClauseSql.append(" and brandspecificstudy = 2 ");
	    	
	    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	}
    	
    	if(categoryType!=null)
    	{
	    	// Adding Category Type Field
	    	
	    	
	    	if(flag)
	    	{
	    		whereClauseSql.append(" and categoryType='");
	    		whereClauseSql.append(categoryType.toString()+"'");
	    	}
	    	else
	    	{
	    		whereClauseSql.append(" where categoryType='");
	    		whereClauseSql.append(categoryType.toString()+"'");
		    	flag= true;
	    	}
    	}
    	
    	whereClauseSql.append(" and iscancel=0 and newsynchro = 1 and status >0 and budgetYear="+budgetYear.toString());
    	
    	// This is done for applying the filter for Cross tab as it was giving issue in case there is No SpendBy Project and Spend By Budget location is selected for Crosstab
    	whereClauseSql.append(applySpendReportFilter(spendReportFilter));
    	
    	whereClauseSql.append(")");
    	
    	
		     
    	 Long count = 0L;
         StringBuilder fetchCountSql = new StringBuilder();
         fetchCountSql.append("SELECT count(*) from grailprojectcostdetails ");
         fetchCountSql.append(whereClauseSql);
        
         try {
             count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(fetchCountSql.toString());
         }
         catch (DataAccessException e) {
             final String message = "Failed to fetch count for CrossTab with Agency";
             LOG.error(message, e);
             
         }
         
    	 StringBuilder sql = new StringBuilder();
	     sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails ");
	     sql.append(whereClauseSql);
    	
	     // If there are more rows for each unique key then we need to fetch the cost differently
         if(count.intValue() > 1)
         {
        	List<ProjectCostDetailsBean> projectCostBeanList = Collections.emptyList();
          	try {
          			projectCostBeanList = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
          			if(projectCostBeanList!=null && projectCostBeanList.size()>1)
          			{
          				
          				BigDecimal totalCostGBP = new BigDecimal("0");
          				for(ProjectCostDetailsBean pcb:projectCostBeanList )
          				{
          					if(pcb.getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1))
        		    		{
          						totalCostGBP = totalCostGBP.add(pcb.getEstimatedCost());
        		    		}
        		    		else
        		    		{
        		    			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(pcb.getCostCurrency())) * (pcb.getEstimatedCost().doubleValue()));
        		    			totalCostGBP =  totalCostGBP.add(gbpEstimatedCost);
        		    		}
          				}
	          			projects = new ProjectCostDetailsBean();	
          				projects.setEstimatedCost(totalCostGBP);
          				projects.setCostCurrency(JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1));
          			}
          			
          			
              }
              catch (DataAccessException e) {
                  final String message = "Failed to MethId for Group";
                  LOG.error(message, e);
                 // throw new DAOException(message, e);
              }
         }
         else
         {
	    	
	   
	        try {
	           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
	        	 projects = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectCostDetailRowMapper);
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to Spend By Latest Cost for Agency ==>"+ agency + "and Project id==>"+ projectID;
	            LOG.error(message, e);
	           // throw new DAOException(message, e);
	        }
         }
        return projects;
    	
    }
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer brand, String brandType, String categoryType, Integer budgetYear, SpendReportExtractFilter spendReportFilter)
    {
    	
    	Project project=null;
    	
    	List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	StringBuilder selectFields = new StringBuilder();
 		StringBuilder groupByFields = new StringBuilder();
 		boolean flag = false;
 		
     	if(projectID!=null)
     	{
     		selectFields.append(" projectID ");
     		groupByFields.append(" projectID");
     		flag = true;
     	}
     	if(projectName!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",name ");
	     		groupByFields.append(",name");
     		}
     		else
     		{
     			selectFields.append(" name ");
	     		groupByFields.append(" name");
     		}
     		flag = true;
     	}
     	if(budgetLocation!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",budgetlocation ");
	     		groupByFields.append(",budgetlocation ");
     		}
     		else
     		{
     			selectFields.append(" budgetlocation ");
	     		groupByFields.append(" budgetlocation ");
     		}
     		flag = true;
     	}
     	if(methodology!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",methodologyDetails ");
	     		groupByFields.append(",methodologyDetails ");
     		}
     		else
     		{
     			selectFields.append(" methodologyDetails ");
	     		groupByFields.append(" methodologyDetails ");
     		}
     		flag = true;
     	}
     	
     	if(brand!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",brand ");
	     		groupByFields.append(",brand ");
     		}
     		else
     		{
     			selectFields.append(" brand ");
	     		groupByFields.append(" brand ");
     		}
     		flag = true;
     	}
     	
     	if(categoryType!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",categoryType ");
	     		groupByFields.append(",categoryType ");
     		}
     		else
     		{
     			selectFields.append(" categoryType ");
	     		groupByFields.append(" categoryType ");
     		}
     		flag = true;
     	}
      
     	selectFields.append(", sum(totalcost) as totalcost ");
    	StringBuilder sql = new StringBuilder();
    	
    	//sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p ");
    	
    	sql.append("SELECT "+ selectFields.toString() + " FROM grailproject ");
    	
    	boolean whereClauseAdded = false;
    	if(projectID != null && projectID.intValue() > 0)
    	{
    		sql.append(" WHERE projectID = ");
	    	sql.append(projectID.toString());
	    	whereClauseAdded = true;
    	}
    	
    	if(projectName!=null)
    	{
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and name ='");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE name ='");
	    	}
	    	sql.append(SynchroUtils.removeQuotes(projectName)+"'");
	    	whereClauseAdded = true;
    	}
    	
    	//Adding Budget Location Fields
    	if(budgetLocation!=null)
    	{
    		if(whereClauseAdded)
	    	{
	    		sql.append(" and budgetlocation =");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE budgetlocation=");
	    	}
    		sql.append(budgetLocation.toString());
    		whereClauseAdded = true;
    	}
    	
    	if(region!=null)
    	{
	    	if(StringUtils.isNotBlank(region))
	    	{
	    		if(whereClauseAdded)
		    	{
	    			sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
		    	}
		    	else
		    	{
		    		sql.append(" WHERE budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
		    	}
	    		whereClauseAdded = true;
	    	}
	    	
    	}
    	
    	if(area!=null)
    	{
	    	if(StringUtils.isNotBlank(area))
	    	{
	    		if(whereClauseAdded)
		    	{
	    			sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
		    	}
		    	else
		    	{
		    		sql.append(" WHERE budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
		    	}
	    		whereClauseAdded = true;
	    	}
	    	
    	}
    	
    	if(t20_t40!=null)
    	{
	    	if(StringUtils.isNotBlank(t20_t40))
	    	{
	    		int t20_40 = -1;
	    		if(t20_t40.equalsIgnoreCase("T20"))
	    		{
	    			t20_40 = 1;
	    		}
	    		if(t20_t40.equalsIgnoreCase("T40"))
	    		{
	    			t20_40 = 2;
	    		}
	    		if(t20_t40.equalsIgnoreCase("Non-T40"))
	    		{
	    			t20_40 = 3;
	    		}
	    		
	    		if(whereClauseAdded)
		    	{
	    			sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
		    	}
		    	else
		    	{
		    		sql.append(" WHERE budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
		    	}
	    		whereClauseAdded = true;
	    	}
    	}
    	
    	// Adding Methodology Fields
    	if(methodology!=null)
    	{
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and methodologydetails ='");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE methodologydetails ='");
	    	}
	     	sql.append(methodology.toString()+"'");
	     	whereClauseAdded = true;
    	
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
	     	if(methIds!=null && methIds.size()>0)
	 	   {
	     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
	            if(methIds.size()>0)
	            {
	         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
	         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
	         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
	                	for(int i=1;i<methIds.size();i++)
	                	{
	                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
	                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
	                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
	                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
	                	}
	            }
	            sql.append(")");
	 	   }
    	}
    	
    	if(brand!=null)
    	{
	     	int bType = -1;
	    	if(brandType!=null && brandType.equals("GDB"))
	    	{
	    		bType = 1;
	    	}
	    	else
	    	{
	    		bType = 2;
	    	}
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and brand =");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE brand =");
	    	}
	    	whereClauseAdded = true;
	     	sql.append(brand.toString());
	    	sql.append(" and brandspecificstudy = 1 ");
	    	
	    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	}
    	
    	// Adding Category Type Field
    	if(categoryType!=null)
    	{
	    	
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and categoryType ='");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE categoryType ='");
	    	}
	    	whereClauseAdded = true;
	    	sql.append(categoryType.toString()+"'");
    	}
    	sql.append(" and iscancel=0 and newsynchro = 1 and status >0 and budgetYear = " + budgetYear.toString());
    	
    	// This is done for applying the filter for Cross tab as it was giving issue in case there is No SpendBy Project and Spend By Budget location is selected for Crosstab
    	sql.append(applySpendReportFilter(spendReportFilter));
    	
    	sql.append(" group by "+ groupByFields.toString());
    	
    	try {
            //project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
    		
    		LOG.debug("CROSSTAB BRAND SQL ==> "+ sql.toString());
    		project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), crossTabLatestCostRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer nonBrand, String brandType, String categoryType, Integer budgetYear, SpendReportExtractFilter spendReportFilter)
    {
    	
    	Project project=null;
    	
    	List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	
    	//sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p ");
    	
    	StringBuilder selectFields = new StringBuilder();
 		StringBuilder groupByFields = new StringBuilder();
 		boolean flag = false;
 		
     	if(projectID!=null)
     	{
     		selectFields.append(" projectID ");
     		groupByFields.append(" projectID");
     		flag = true;
     	}
     	if(projectName!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",name ");
	     		groupByFields.append(",name");
     		}
     		else
     		{
     			selectFields.append(" name ");
	     		groupByFields.append(" name");
     		}
     		flag = true;
     	}
     	if(budgetLocation!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",budgetlocation ");
	     		groupByFields.append(",budgetlocation ");
     		}
     		else
     		{
     			selectFields.append(" budgetlocation ");
	     		groupByFields.append(" budgetlocation ");
     		}
     		flag = true;
     	}
     	if(methodology!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",methodologyDetails ");
	     		groupByFields.append(",methodologyDetails ");
     		}
     		else
     		{
     			selectFields.append(" methodologyDetails ");
	     		groupByFields.append(" methodologyDetails ");
     		}
     		flag = true;
     	}
     	
     	if(nonBrand!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",studytype ");
	     		groupByFields.append(",studytype ");
     		}
     		else
     		{
     			selectFields.append(" studytype ");
	     		groupByFields.append(" studytype ");
     		}
     		flag = true;
     	}
     	
     	if(categoryType!=null)
     	{
     		if(flag)
     		{
	     		selectFields.append(",categoryType ");
	     		groupByFields.append(",categoryType ");
     		}
     		else
     		{
     			selectFields.append(" categoryType ");
	     		groupByFields.append(" categoryType ");
     		}
     		flag = true;
     	}
      
     	selectFields.append(", sum(totalcost) as totalcost ");
//    	StringBuilder sql = new StringBuilder();
    	
    	//sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p ");
    	
    	sql.append("SELECT "+ selectFields.toString() + " FROM grailproject ");
    	
    	boolean whereClauseAdded = false;
    	if(projectID != null && projectID.intValue() > 0)
    	{
    		sql.append(" WHERE projectID = ");
	    	sql.append(projectID.toString());
	    	whereClauseAdded = true;
    	}
    	
    	if(projectName!=null)
    	{
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and name ='");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE name ='");
	    	}
	    	sql.append(SynchroUtils.removeQuotes(projectName)+"'");
	    	whereClauseAdded = true;
    	}
    	
    	//Adding Budget Location Fields
    	if(budgetLocation!=null)
    	{
    		if(whereClauseAdded)
	    	{
	    		sql.append(" and budgetlocation =");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE budgetlocation=");
	    	}
    		sql.append(budgetLocation.toString());
    		whereClauseAdded = true;
    	}
    	
    	if(region!=null)
    	{
	    	if(StringUtils.isNotBlank(region))
	    	{
	    		if(whereClauseAdded)
		    	{
	    			sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
		    	}
		    	else
		    	{
		    		sql.append(" WHERE budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
		    	}
	    		whereClauseAdded = true;
	    	}
	    	
    	}
    	
    	if(area!=null)
    	{
	    	if(StringUtils.isNotBlank(area))
	    	{
	    		if(whereClauseAdded)
		    	{
	    			sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
		    	}
		    	else
		    	{
		    		sql.append(" WHERE budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
		    	}
	    		whereClauseAdded = true;
	    	}
	    	
    	}
    	
    	if(t20_t40!=null)
    	{
	    	if(StringUtils.isNotBlank(t20_t40))
	    	{
	    		int t20_40 = -1;
	    		if(t20_t40.equalsIgnoreCase("T20"))
	    		{
	    			t20_40 = 1;
	    		}
	    		if(t20_t40.equalsIgnoreCase("T40"))
	    		{
	    			t20_40 = 2;
	    		}
	    		if(t20_t40.equalsIgnoreCase("Non-T40"))
	    		{
	    			t20_40 = 3;
	    		}
	    		
	    		if(whereClauseAdded)
		    	{
	    			sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
		    	}
		    	else
		    	{
		    		sql.append(" WHERE budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
		    	}
	    		whereClauseAdded = true;
	    	}
    	}
    	
    	// Adding Methodology Fields
    	if(methodology!=null)
    	{
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and methodologydetails ='");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE methodologydetails ='");
	    	}
	     	sql.append(methodology.toString()+"'");
	     	whereClauseAdded = true;
    	
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
	     	if(methIds!=null && methIds.size()>0)
	 	   {
	     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
	            if(methIds.size()>0)
	            {
	         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
	         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
	         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
	                	for(int i=1;i<methIds.size();i++)
	                	{
	                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
	                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
	                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
	                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
	                	}
	            }
	            sql.append(")");
	 	   }
    	}
    	
    	if(nonBrand!=null)
    	{
	     	int bType = -1;
	    	if(brandType!=null && brandType.equals("GDB"))
	    	{
	    		bType = 1;
	    	}
	    	else
	    	{
	    		bType = 2;
	    	}
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and studytype =");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE studytype =");
	    	}
	    	whereClauseAdded = true;
	     	sql.append(nonBrand.toString());
	    	sql.append(" and brandspecificstudy = 2 ");
	    	
	    	
    	}
    	
    	// Adding Category Type Field
    	if(categoryType!=null)
    	{
	    	
	    	if(whereClauseAdded)
	    	{
	    		sql.append(" and categoryType ='");
	    	}
	    	else
	    	{
	    		sql.append(" WHERE categoryType ='");
	    	}
	    	whereClauseAdded = true;
	    	sql.append(categoryType.toString()+"'");
    	}
    	
    	sql.append(" and iscancel=0 and newsynchro = 1 and status >0 and budgetYear = " + budgetYear.toString());
    	
    	// This is done for applying the filter for Cross tab as it was giving issue in case there is No SpendBy Project and Spend By Budget location is selected for Crosstab
    	sql.append(applySpendReportFilter(spendReportFilter));
    	
    	sql.append(" group by "+ groupByFields.toString());
    	
    	try {
    		LOG.debug("CROSSTAB NON BRAND SQL ==> "+ sql.toString());
    		project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), crossTabLatestCostRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer brand, String brandType){
    	Project project=null;
    	
    	
    	
    	 List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	// Adding Methodology Fields
    	sql.append(" and methodologydetails='");
     	sql.append(methodology.toString()+"'");
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
     	if(methIds!=null && methIds.size()>0)
 	   {
     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
            if(methIds.size()>0)
            {
         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
                	for(int i=1;i<methIds.size();i++)
                	{
                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
                	}
            }
            sql.append(")");
 	   }
     	
     	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
     	
     	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, Integer nonBrand, String brandType){
    	Project project=null;
    	
    	
    	
    	 List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	// Adding Methodology Fields
    	sql.append(" and methodologydetails='");
     	sql.append(methodology.toString()+"'");
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
     	if(methIds!=null && methIds.size()>0)
 	   {
     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
            if(methIds.size()>0)
            {
         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
                	for(int i=1;i<methIds.size();i++)
                	{
                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
                	}
            }
            sql.append(")");
 	   }
     	
     	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup){
    	Project project=null;
    	
    	
    	
    	 List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	// Adding Methodology Fields
    	sql.append(" and methodologydetails='");
     	sql.append(methodology.toString()+"'");
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
     	if(methIds!=null && methIds.size()>0)
 	   {
     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
            if(methIds.size()>0)
            {
         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
                	for(int i=1;i<methIds.size();i++)
                	{
                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
                	}
            }
            sql.append(")");
 	   }
     	
     	
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer brand, String brandType){
    
    	Project project=null;
     	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    
     	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
     	
     	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer nonBrand, String brandType){
    	Project project=null;
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	
     	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, String methodology, String methGroup, String categoryType){
    	Project project=null;
    	
    	
    	
    	 List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	// Adding Methodology Fields
    	sql.append(" and methodologydetails='");
     	sql.append(methodology.toString()+"'");
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
     	if(methIds!=null && methIds.size()>0)
 	   {
     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
            if(methIds.size()>0)
            {
         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
                	for(int i=1;i<methIds.size();i++)
                	{
                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
                	}
            }
            sql.append(")");
 	   }
     
    	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer brand, String brandType, String categoryType){
    	
    	Project project=null;
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	
     	
     	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
     	
     	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsNonBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40, Integer nonBrand, String brandType, String categoryType){
    	
    	Project project=null;
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	
     	
     	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40,  String categoryType){
    	
    	Project project=null;
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    
    	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, String methodology, String methGroup, Integer brand, String brandType){
    	Project project=null;
    	
    	
    	
    	 List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
    	
    	// Adding Methodology Fields
    	sql.append(" and methodologydetails='");
     	sql.append(methodology.toString()+"'");
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
     	if(methIds!=null && methIds.size()>0)
 	   {
     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
            if(methIds.size()>0)
            {
         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
                	for(int i=1;i<methIds.size();i++)
                	{
                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
                	}
            }
            sql.append(")");
 	   }
     	
     	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
     	
     	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsNonBrand(final Long projectID, final String projectName, String methodology, String methGroup, Integer nonBrand, String brandType){
    	Project project=null;
    	
    	
    	
    	 List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
    	// Adding Methodology Fields
    	sql.append(" and methodologydetails='");
     	sql.append(methodology.toString()+"'");
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
     	if(methIds!=null && methIds.size()>0)
 	   {
     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
            if(methIds.size()>0)
            {
         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
                	for(int i=1;i<methIds.size();i++)
                	{
                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
                	}
            }
            sql.append(")");
 	   }
     	
     	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, String methodology, String methGroup, String categoryType)
    {
    	Project project=null;
    	List<Integer> methIds = Collections.emptyList();
     	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methGroup+"' and mg.id = map.methodologygroupid";
     	
 		try {
            
 			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
         }
         catch (DataAccessException e) {
             final String message = "Failed to MethId for Group";
             LOG.error(message, e);
            // throw new DAOException(message, e);
         }

     	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
    	
    	// Adding Methodology Fields
    	sql.append(" and methodologydetails='");
     	sql.append(methodology.toString()+"'");
     	
     	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
     	
     	if(methIds!=null && methIds.size()>0)
 	   {
     		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
            if(methIds.size()>0)
            {
         	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
         	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
         	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
                	for(int i=1;i<methIds.size();i++)
                	{
                		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
                		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
                	}
            }
            sql.append(")");
 	   }
     	
     	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    public Project getProjectsBrand(final Long projectID, final String projectName, Integer brand, String brandType, String categoryType)
    {
    	
    	Project project=null;
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
     	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
     	
     	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsNonBrand(final Long projectID, final String projectName, Integer nonBrand, String brandType, String categoryType)
    {
    	Project project=null;
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
     	
     	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    public Project getProjectsBrand(final Long projectID, final String projectName, final Integer budgetLocation, String region, String area, String t20_t40)
    {
    	
    	Project project=null;
    	
    	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	//Adding Budget Location Fields
    	sql.append(" and budgetlocation = ");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
   
    	
    	
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    
    public Project getProjectsBrand(final Long projectID, final String projectName, Integer brand, String brandType)
    {
    	
    	Project project=null;
    	
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
     	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
     	
     	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }

    public Project getProjectsNonBrand(final Long projectID, final String projectName,  Integer nonBrand, String brandType)
    {
    	Project project=null;
    	
      
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
     	
     	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
    }
    
    public Project getProjectsBrand(final Long projectID, final String projectName, String categoryType)
    {
    	
    	Project project=null;
    	
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT "+ prependAlias(PROJECT_FIELDS, "p") + " FROM grailproject p WHERE p.projectID = ");
    	sql.append(projectID.toString());
    	sql.append(" and name ='");
    	sql.append(projectName+"'");
    	
    	// Adding Category Type Field
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	try {
            project = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(sql.toString(), projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to Fetch  project-" + projectID;
            LOG.error(message, e);
            //throw new DAOException(message, e);
        }
    	return project;
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
    
    public List<Project> getAllNew(){
    	List<Project> projects = Collections.emptyList();
    	try {
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(ALL_PROJECTS_NEW, projectRowMapper);
            
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
    	return projects;
    }
    
    public List<Project> getAllProjectsForQPRSnapshot(){
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Project> projects = Collections.emptyList();
        try {
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(ALL_PROJECTS_QPR, projectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects.";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getBudgetYearProjectsForQPRSnapshot(final Integer budgetYear){
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Project> projects = Collections.emptyList();
        try {
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(BUDGET_YEAR_PROJECTS_QPR, projectRowMapper, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByBudgetYearProjectsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter){
        // String ALL_PROJECTS = "SELECT "+ PROJECT_FIELDS + " FROM grailproject WHERE ";
        List<Project> projects = Collections.emptyList();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(prependAlias(PROJECT_FIELDS, "p")).append(" FROM grailproject p where p.iscancel=0 and p.newsynchro = 1 and p.status >0 and p.budgetyear=").append(budgetYear.toString());
        
        if(SynchroPermHelper.isGlobalUserType())
    	{
    		
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		sql.append(" AND p.budgetlocation in (");
    		
    		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
    		regionBudgetLocations.addAll(regionEndMarketLocations);
    		
    		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
    		if(SynchroPermHelper.isEndMarketUserType())
    		{
    			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    			regionBudgetLocations.addAll(emBudgetLocations);
    		}
    		
    		sql.append(StringUtils.join(regionBudgetLocations, ","));
    		sql.append(")");
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		sql.append(" AND p.budgetlocation in (");
    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    		sql.append(StringUtils.join(emBudgetLocations, ","));
    		sql.append(")");
    	} 
        
      //Methodology Details Filter
        if(spendReportFilter.getMethDetails() != null && spendReportFilter.getMethDetails().size()>0 && !isListNull(spendReportFilter.getMethDetails())) {
            StringBuilder methodologyDetailsCondition = new StringBuilder();
            methodologyDetailsCondition.append(" AND (p.methodologydetails in ('")
                    .append(spendReportFilter.getMethDetails().get(0)+"").append("')");
            if(spendReportFilter.getMethDetails().size()>0)
            {
         	   methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
         	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(0)+"").append("')");
         	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,") .append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<spendReportFilter.getMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or p.methodologydetails in ('").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
	               	}
            }
            methodologyDetailsCondition.append(")");
            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
            sql.append(methodologyDetailsCondition.toString());
            
        }
      
        
      //Brand Filter
        if(spendReportFilter.getBrands() != null && spendReportFilter.getBrands().size()>0 && !isListNull(spendReportFilter.getBrands())) {
            StringBuilder brandsCondition = new StringBuilder();
            brandsCondition.append(" AND (p.brand in (");
            brandsCondition.append(StringUtils.join(spendReportFilter.getBrands(),","));
            brandsCondition.append(")");
            brandsCondition.append(" OR p.studytype in (");
            brandsCondition.append(StringUtils.join(spendReportFilter.getBrands(),","));
            brandsCondition.append("))");
            sql.append(brandsCondition.toString());
            
        }
        
        //Budget Location Filter
        if(spendReportFilter.getBudgetLocations() != null && spendReportFilter.getBudgetLocations().size()>0 && !isListNull(spendReportFilter.getBudgetLocations())) {
            StringBuilder budgetLocationsCondition = new StringBuilder();
            budgetLocationsCondition.append(" AND p.budgetlocation in (");
            budgetLocationsCondition.append(StringUtils.join(spendReportFilter.getBudgetLocations(),","));
            budgetLocationsCondition.append(")");
            sql.append(budgetLocationsCondition.toString());
            
        }
        
          
        try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(BUDGET_YEAR_PROJECTS_QPR, projectRowMapper, budgetYear);
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByBudgetYearProjectsForQPRSnapshot(List<Long> projectIds)
    {
    	 List<Project> projects = Collections.emptyList();
         
         StringBuilder sql = new StringBuilder();
         sql.append("SELECT ").append(prependAlias(PROJECT_FIELDS, "p")).append(" FROM grailproject p where p.newsynchro = 1 and p.projectid in (").append(StringUtils.join(projectIds, ",")).append(")");
         
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(BUDGET_YEAR_PROJECTS_QPR, projectRowMapper, budgetYear);
            projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all projects ";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Integer> getBudgetLocationsForQPRSnapshot(final Integer budgetYear , SpendReportExtractFilter spendReportFilter){
        List<Integer> budgetLocations = Collections.emptyList();
       
        List<Long> budgetLocationsUser = getBudgetLocationsForProject(budgetYear);
        try {
        	//if(budgetLocations!=null && budgetLocations.size()>0)
        	if(SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isEndMarketUserType())
        	{
        		String sql = GET_ALL_BUDGET_LOCATIONS +  " AND budgetlocation in ("+ StringUtils.join(budgetLocationsUser, ",")+ ") ";
        		
        		if(budgetLocationsUser!=null && budgetLocationsUser.size()>0)
        		{
        			sql = sql + applySpendReportFilter(spendReportFilter);
        			budgetLocations = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        		}
        		else
        		{
        			sql = GET_ALL_BUDGET_LOCATIONS + applySpendReportFilter(spendReportFilter); 
        			budgetLocations = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        		}
        	}
        	else
        	{
        		String sql = GET_ALL_BUDGET_LOCATIONS + applySpendReportFilter(spendReportFilter); 
        		budgetLocations = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all budget Locations for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return budgetLocations;
    }
    
    public List<String> getMethodologiesForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter){
        List<String> methodologies = Collections.emptyList();
        List<Long> budgetLocations = getBudgetLocationsForProject(budgetYear);
        
        try {
        	//if(budgetLocations!=null && budgetLocations.size()>0)
        	if(SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isEndMarketUserType())
        	{
        		String sql = GET_ALL_METHODOLOGIES +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		
        		
        		if(budgetLocations!=null && budgetLocations.size()>0)
        		{
        			sql = sql + applySpendReportFilter(spendReportFilter);
        			methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        		}
        		else
        		{
        			sql = GET_ALL_METHODOLOGIES + applySpendReportFilter(spendReportFilter);
        			methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        		}
        	}
        	else
        	{
        		String sql = GET_ALL_METHODOLOGIES + applySpendReportFilter(spendReportFilter);
        		methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Methodologies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        
        return methodologies;
    }
    
    public List<String> getMethodologiesForQPRSnapshot(final Integer budgetYear, List<Long> selectedQPRs){
        List<String> methodologies = Collections.emptyList();
        try {
        	methodologies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_ALL_METHODOLOGIES, String.class, budgetYear);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Methodologies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return methodologies;
    }
    
    
    public List<String> getCategoryTypesForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter){
        
    	List<String> categoryTypes = Collections.emptyList();
      
    	 List<Long> budgetLocations = getBudgetLocationsForProject(budgetYear);
        try {
        	//if(budgetLocations!=null && budgetLocations.size()>0)
        	if(SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isEndMarketUserType())
        	{
        		String sql = GET_ALL_CATEGORY_TYPES +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ") ";
        		
        		
        		if(budgetLocations!=null && budgetLocations.size()>0)
        		{
        			sql = sql + applySpendReportFilter(spendReportFilter);
        			categoryTypes = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        		}
        		else
        		{
        			sql = GET_ALL_CATEGORY_TYPES + applySpendReportFilter(spendReportFilter);
        			categoryTypes = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        		}
        	}
        	else
        	{
        		String sql = GET_ALL_CATEGORY_TYPES + applySpendReportFilter(spendReportFilter);
        		categoryTypes = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, String.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Category Types for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        
        return categoryTypes;
    }
    
    public List<Integer> getBrandsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter){
        List<Integer> brands = Collections.emptyList();
        
        List<Long> budgetLocations = getBudgetLocationsForProject(budgetYear);
        
        
        try {
        	//if(budgetLocations!=null && budgetLocations.size()>0)
        	if(SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isEndMarketUserType())
        	{
        		String sql = GET_ALL_BRANDS +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ") ";
        		
        		
        		if(budgetLocations!=null && budgetLocations.size()>0)
        		{
        			
        			sql = sql + applySpendReportFilter(spendReportFilter);
        			brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        		}
        		else
        		{
        			sql = GET_ALL_BRANDS + applySpendReportFilter(spendReportFilter);
        			brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        		}
        	}
        	else
        	{
        		String sql = GET_ALL_BRANDS + applySpendReportFilter(spendReportFilter);
        		brands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return brands;
    }
    
    public List<Integer> getNonBrandsForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter){
        
    	List<Integer> nonBrands = Collections.emptyList();
        List<Long> budgetLocations = getBudgetLocationsForProject(budgetYear);
        
        try {
        	
        	//if(budgetLocations!=null && budgetLocations.size()>0)
        	if(SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isEndMarketUserType())
        	{
        		String sql = GET_ALL_NON_BRANDS +  " AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ") ";
        		
        		
        		if(budgetLocations!=null && budgetLocations.size()>0)
        		{
        			sql = sql + applySpendReportFilter(spendReportFilter);
        			nonBrands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        		}
        		else
        		{
        			sql = GET_ALL_NON_BRANDS + applySpendReportFilter(spendReportFilter);
        			nonBrands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        		}
        		
        	}
        	else
        	{
        		String sql = GET_ALL_NON_BRANDS + applySpendReportFilter(spendReportFilter);
        		nonBrands = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Integer.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Non brands for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return nonBrands;
    }
    
    public List<Long> getAgenciesForQPRSnapshot(final Integer budgetYear, SpendReportExtractFilter spendReportFilter){
        List<Long> agencies = Collections.emptyList();
        List<Long> budgetLocations = getBudgetLocationsForProject(budgetYear);
        
        try {
        	//if(budgetLocations!=null && budgetLocations.size()>0)
        	if(SynchroPermHelper.isGlobalUserType() || SynchroPermHelper.isRegionalUserType() || SynchroPermHelper.isEndMarketUserType())
        	{
        		String sql = "SELECT distinct(agencyid) FROM grailprojectcostdetails where projectid in (select p.projectid from grailproject p where p.iscancel=0 and p.status >0 and p.budgetyear=? AND budgetlocation in ("+ StringUtils.join(budgetLocations, ",")+ ")";
        		
        		sql = sql + applySpendReportFilter(spendReportFilter) + ")";
        		
        		if(budgetLocations!=null && budgetLocations.size()>0)
        		{
        			agencies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, budgetYear);
        		}
        		else
        		{
        			sql = GET_ALL_AGENCIES + applySpendReportFilter(spendReportFilter) + ")";
        			agencies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, budgetYear);
        		}
        	}
        	else
        	{
        		String sql = GET_ALL_AGENCIES + applySpendReportFilter(spendReportFilter) + ")";
        		agencies = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, budgetYear);
        	}
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Agencies for Budget Year ==> "+ budgetYear;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        
        return agencies;
    }
    
    public List<Long> getAllProjects(final Long budgetYear, Long budgetLocation){
        List<Long> projectIds = Collections.emptyList();
        try {
        	String sql = "select projectid from grailproject where budgetyear = ? and budgetlocation = ?";
        	projectIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql, Long.class, budgetYear, budgetLocation);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load all Projects for Budget Year ==> "+ budgetYear +" and Budget Location ==>"+budgetLocation;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projectIds;
    }
    
    public List<Project> getSpendByBudgetLocationLatestCost(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation ");
    	
    	try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BUDGET_LOCATION_LATEST_COST, budgetLocationLatestCostRowMapper, budgetYear,budgetLocation);
    		 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), budgetLocationLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Budget Location ==>"+ budgetLocation;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByBudgetLocationLatestCost(final Integer budgetYear, final Integer budgetLocation, String region, String area, String t20_t40, SpendReportExtractFilter spendReportFilter)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	//This else block will handle the case in which the Area is there in QPR but not there in Latest projects
    	/*else
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name isNull and area.id=map.areaid )");
    	}*/
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation ");
    	
    	try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BUDGET_LOCATION_LATEST_COST, budgetLocationLatestCostRowMapper, budgetYear,budgetLocation);
    		 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), budgetLocationLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Budget Location ==>"+ budgetLocation;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    
    public List<Project> getSpendByLatestCostBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer brand, final List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
   	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brandspecificstudy = 1 and brand=");
    	sql.append(brand.toString());
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetLocation, methodologydetails, brand ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByLatestCostNonBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer nonbrand, final List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
      	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brandspecificstudy = 1 and studytype=");
    	sql.append(nonbrand.toString());
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetLocation, methodologydetails, studytype ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByLatestCostBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer brand, String categoryType, final List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
   	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brandspecificstudy = 1 and brand=");
    	sql.append(brand.toString());
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetLocation, methodologydetails, brand, categorytype ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByLatestCostNonBrandAll(final Integer budgetYear, final Integer budgetLocation, SpendReportExtractFilter spendReportFilter, final String methodology, final Integer nonbrand, final String categoryType, final List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
      	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brandspecificstudy = 1 and studytype=");
    	sql.append(nonbrand.toString());
    	
    	sql.append(" and categorytype='");
    	sql.append(categoryType.toString()+"'");
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetLocation, methodologydetails, studytype, categorytype ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    public List<Project> getSpendByCrossTabBLMethodologyLatestCost(final Integer budgetYear ,final Integer budgetLocation, final String methodology, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation, methodologydetails ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByCrossTabBLMethodologyLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation, methodologydetails ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByCrossTabBLMethodologyBrandLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	
    	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, methodologydetails, brand, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation, methodologydetails, brand ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
        
        
        
   
    }
    
    
    
    public List<Project> getSpendByCrossTabBLMethodologyNonBrandLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer nonBrand, String brandType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	
    	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, methodologydetails, studytype, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation, methodologydetails, studytype ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
        
        
        
   
    }
    
    public List<Project> getSpendByCrossTabBLMethodologyBrandCTLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer brand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	
    	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, methodologydetails, brand, categoryType, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation, methodologydetails, brand, categoryType ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
   
    }
    
    public List<Project> getSpendByCrossTabBLMethodologyNonBrandCTLatestCost(final Integer budgetYear ,final Integer budgetLocation, String region, String area, String t20_t40, final String methodology, final Integer nonBrand, String brandType, String categoryType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	
    	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select budgetlocation, methodologydetails, studytype, categoryType, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and budgetlocation=");
    	sql.append(budgetLocation.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
    	//sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	if(StringUtils.isNotBlank(region))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailregionfieldmappingfields map, grailregionfields region where region.name='"+region+"' and region.id=map.regionid )");
    	}
    	
    	if(StringUtils.isNotBlank(area))
    	{
    		sql.append(" and budgetlocation in (select map.endmarketid from grailareafieldmappingfields map, grailareafields area where area.name='"+area+"' and area.id=map.areaid )");
    	}
    	
    	if(StringUtils.isNotBlank(t20_t40))
    	{
    		int t20_40 = -1;
    		if(t20_t40.equalsIgnoreCase("T20"))
    		{
    			t20_40 = 1;
    		}
    		if(t20_t40.equalsIgnoreCase("T40"))
    		{
    			t20_40 = 2;
    		}
    		if(t20_t40.equalsIgnoreCase("Non-T40"))
    		{
    			t20_40 = 3;
    		}
    		sql.append(" and budgetlocation in (select eid from grailemothersmapping where t20_t40 = "+t20_40+")");
    	}
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by budgetlocation, methodologydetails, studytype, categoryType ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
   
    }
    
    public List<Project> getSpendByCrossTabMethodologyBrandLatestCost(final Integer budgetYear , final String methodology, final Integer brand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brandspecificstudy = 1  and brand=");
    	sql.append(brand.toString());
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails, brand ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByCrossTabMethodologyNonBrandLatestCost(final Integer budgetYear , final String methodology, final Integer nonbrand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	sql.append(" and brandspecificstudy = 2 and studytype=");
    	sql.append(nonbrand.toString());
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails, studytype ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    private String applySpendReportFilter(SpendReportExtractFilter spendReportFilter)
    {
    	StringBuilder sql = new StringBuilder();
    	
    	// Access mechanism for Spend Reports Filter
    	if(SynchroPermHelper.isGlobalUserType())
    	{
    		
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		sql.append(" AND budgetlocation in (");
    		
    		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
    		regionBudgetLocations.addAll(regionEndMarketLocations);
    		
    		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
    		if(SynchroPermHelper.isEndMarketUserType())
    		{
    			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    			regionBudgetLocations.addAll(emBudgetLocations);
    		}
    		
    		sql.append(StringUtils.join(regionBudgetLocations, ","));
    		sql.append(")");
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{
    		sql.append(" AND budgetlocation in (");
    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    		sql.append(StringUtils.join(emBudgetLocations, ","));
    		sql.append(")");
    	} 
    	
    	//Methodology Details Filter
        if(spendReportFilter.getMethDetails() != null && spendReportFilter.getMethDetails().size()>0 && !isListNull(spendReportFilter.getMethDetails())) {
            StringBuilder methodologyDetailsCondition = new StringBuilder();
            methodologyDetailsCondition.append(" AND (methodologydetails in ('")
                    .append(spendReportFilter.getMethDetails().get(0)+"").append("')");
            if(spendReportFilter.getMethDetails().size()>0)
            {
         	   methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(0)+"").append("')");
         	   methodologyDetailsCondition.append(" or methodologydetails like ('%,") .append(spendReportFilter.getMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<spendReportFilter.getMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or methodologydetails in ('").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or methodologydetails like ('").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or methodologydetails like ('%,").append(spendReportFilter.getMethDetails().get(i)+"").append(",%')");
	               	}
            }
            methodologyDetailsCondition.append(")");
            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
            sql.append(methodologyDetailsCondition.toString());
            
        }
      
        
      //Brand Filter
        if(spendReportFilter.getBrands() != null && spendReportFilter.getBrands().size()>0 && !isListNull(spendReportFilter.getBrands())) {
            StringBuilder brandsCondition = new StringBuilder();
            
            Set<Integer> allBrands = new HashSet<Integer>();
        	Set<Integer> allNonBrands = new HashSet<Integer>();
            for(Long brand : spendReportFilter.getBrands())
        	{
        		/*allBrands.add(brand.intValue());
        		allNonBrands.add(brand.intValue());*/
        		if(brand.intValue()==SynchroConstants.MULTI_BRAND_UI_VALUE)
        		{
        			allNonBrands.add(SynchroConstants.MULTI_BRAND_DB_VALUE);
        		}
        		else if(brand.intValue()==SynchroConstants.NON_BRAND_UI_VALUE)
        		{
        			allNonBrands.add(SynchroConstants.NON_BRAND_DB_VALUE);
        		}
        		else
        		{
        			allBrands.add(brand.intValue());
        		}
        	}
            
            if(allBrands.size()>0 && allNonBrands.size()>0)
            {
	            brandsCondition.append(" AND (brand in (");
	            brandsCondition.append(StringUtils.join(allBrands,","));
	            brandsCondition.append(")");
	            brandsCondition.append(" OR studytype in (");
	            brandsCondition.append(StringUtils.join(allNonBrands,","));
	            brandsCondition.append("))");
            }
            else if(allBrands.size()>0)
            {
            	brandsCondition.append(" AND (brand in (");
	            brandsCondition.append(StringUtils.join(allBrands,","));
	            brandsCondition.append("))");
            }
            else if(allNonBrands.size()>0)
            {
            	brandsCondition.append(" AND (studytype in (");
	            brandsCondition.append(StringUtils.join(allNonBrands,","));
	            brandsCondition.append("))");
            }
            sql.append(brandsCondition.toString());
            
        }
        
        //Budget Location Filter
        if(spendReportFilter.getBudgetLocations() != null && spendReportFilter.getBudgetLocations().size()>0 && !isListNull(spendReportFilter.getBudgetLocations())) {
            StringBuilder budgetLocationsCondition = new StringBuilder();
            budgetLocationsCondition.append(" AND budgetlocation in (");
            budgetLocationsCondition.append(StringUtils.join(spendReportFilter.getBudgetLocations(),","));
            budgetLocationsCondition.append(")");
            sql.append(budgetLocationsCondition.toString());
            
        }
        return sql.toString();
    }
    
    public List<ProjectCostDetailsBean> getSpendByAgencyLatestCost(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }

    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBrand(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation, String methodology, Integer brand)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	sql.append(" and budgetlocation =").append(budgetLocation.toString());
    	sql.append(" and methodologydetails ='").append(methodology.toString()).append("'");
    	sql.append(" and brand =").append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }

    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostNonBrand(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation, String methodology, Integer nonbrand)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	sql.append(" and budgetlocation =").append(budgetLocation.toString());
    	sql.append(" and methodologydetails ='").append(methodology.toString()).append("'");
    	sql.append(" and studytype =").append(nonbrand.toString());
    	
    	sql.append(" and brandspecificstudy = 2 ");

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }

    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBLMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation, String methodology)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	if(budgetLocation!=null)
    	{
    		sql.append(" and budgetlocation =").append(budgetLocation.toString());
    	}
    	
    	if(methodology!=null)
    	{
    		sql.append(" and methodologydetails ='").append(methodology.toString()).append("' ");
    	}
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBrandMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, String methodology, Integer brand)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	if(methodology!=null)
    	{
    		sql.append(" and methodologydetails ='").append(methodology.toString()).append("'");
    	}
    	
    	if(brand!=null)
    	{
    		sql.append(" and brand =").append(brand.toString());
    	}
    	sql.append(" and brandspecificstudy = 1 ");

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostNonBrandMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, String methodology, Integer nonbrand)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	if(methodology!=null)
    	{
    		sql.append(" and methodologydetails ='").append(methodology.toString()).append("'");
    	}
    	
    	sql.append(" and studytype =").append(nonbrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBL(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer budgetLocation)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	sql.append(" and budgetlocation =").append(budgetLocation.toString());
    
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostMeth(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, String methodology)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	sql.append(" and methodologydetails ='").append(methodology.toString()).append("'");
    	

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostBrandOnly(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds, Integer brand)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	sql.append(" and brand =").append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<ProjectCostDetailsBean> getSpendByAgencyCrossTabLatestCostNonBrandOnly(final Integer budgetYear, final Long agency,SpendReportExtractFilter spendReportFilter, List<Long> projectIds,Integer nonbrand)
    {
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails " +
            " WHERE agencyid = "+agency.toString());
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear= "+budgetYear.toString());
    	
    	
    	sql.append(" and studytype =").append(nonbrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");

    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
    	
    	
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_LATEST_COST, projectCostDetailRowMapper, agency, budgetYear);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency ==>"+ agency;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    public List<ProjectCostDetailsBean> getSpendByAgencyTypeLatestCost(final Integer budgetYear, final Integer agencyType,SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	
    	List<ProjectCostDetailsBean> projects = Collections.emptyList();
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT projectid, agencyid, costcomponent, costcurrency, estimatedcost from grailprojectcostdetails WHERE ");
        sql.append(" agencyid in (select agency.id from grailresearchagency agency, grailresearchagencymapping mapping where agency.id = mapping.researchagencyid and mapping.researchagencygroupid = "+agencyType.toString()+" )");    
        
        if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
        
        sql.append(" and projectid in (select projectid from grailproject where iscancel=0 and status >0 and budgetyear="+budgetYear.toString());
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" )");
        try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_AGENCY_TYPE_LATEST_COST, projectCostDetailRowMapper, budgetYear, agencyType);
        	projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectCostDetailRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Agency Type==>"+ agencyType;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByMethodologyLatestCost(final Integer budgetYear, final String methodology, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByMethodologyLatestCost(final Integer budgetYear, final String methodology, final String methodologyGroup, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	
    	 List<Integer> methIds = Collections.emptyList();
    	String methMethGroupSql = "select map.methodologyid from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methodologyGroup+"' and mg.id = map.methodologygroupid";
    	
		try {
           
			methIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(methMethGroupSql.toString(), Integer.class);
        }
        catch (DataAccessException e) {
            final String message = "Failed to MethId for Group";
            LOG.error(message, e);
           // throw new DAOException(message, e);
        }

    	
         
    	StringBuilder sql = new StringBuilder();
    	sql.append("select methodologydetails, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and methodologydetails='");
    	sql.append(methodology.toString()+"'");
    	
    	//sql.append("and methodologydetails in ("+StringUtils.join(methIds, ",")+")")
    	
    	if(methIds!=null && methIds.size()>0)
	   {
    		sql.append(" and (methodologydetails in ('").append(methIds.get(0)+"").append("')");
           if(methIds.size()>0)
           {
        	   sql.append(" or methodologydetails like ('").append(methIds.get(0)+"").append(",%')");
        	   sql.append(" or methodologydetails like ('%,").append(methIds.get(0)+"").append("')");
        	   sql.append(" or methodologydetails like ('%,") .append(methIds.get(0)+"").append(",%')");
               	for(int i=1;i<methIds.size();i++)
               	{
               		sql.append(" or methodologydetails in ('").append(methIds.get(i)+"").append("')");
               		sql.append(" or methodologydetails like ('").append(methIds.get(i)+"").append(",%')");
               		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append("')");
               		sql.append(" or methodologydetails like ('%,").append(methIds.get(i)+"").append(",%')");
               	}
           }
           sql.append(")");
	   }
    	//sql.append(" and methodologydetails in (select to_char(map.methodologyid, '999') from grailmethfieldmappingfields map,  grailmethodologygroupfields mg where mg.name='"+methodologyGroup+"' and mg.id = map.methodologygroupid)");
    	//sql.append(methodology.toString()+"'");
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by methodologydetails ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and methodology ==>"+ methodology;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByCategoryTypeLatestCost(final Integer budgetYear, final String categoryType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	 
    	StringBuilder sql = new StringBuilder();
    	sql.append("select categoryType, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and categoryType='");
    	sql.append(categoryType.toString()+"'");
    	
		if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	  
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by categoryType ");
    	
    	try {
            //projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_METHODOLOGY_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,methodology);
    		projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and Category Type ==>"+ categoryType;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }

    public List<Project> getSpendByBrandLatestCost(final Integer budgetYear, final Integer brand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select brand, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by brand ");
    	
    	try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BRAND_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,brand);
    		 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and brand ==>"+ brand;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByBrandLatestCost(final Integer budgetYear, final Integer brand, String brandType, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    	
    	int bType = -1;
    	if(brandType!=null && brandType.equals("GDB"))
    	{
    		bType = 1;
    	}
    	else
    	{
    		bType = 2;
    	}
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("select brand, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and brand=");
    	sql.append(brand.toString());
    	sql.append(" and brandspecificstudy = 1 ");
    	
    	sql.append(" and brand in (select id from grailbrandfields where brandtype="+bType+") ");
    	
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by brand ");
    	
    	try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_BRAND_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,brand);
    		 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and brand ==>"+ brand;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByNonBrandLatestCost(final Integer budgetYear, final Integer nonBrand, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    			
    	StringBuilder sql = new StringBuilder();
    	sql.append("select studytype, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by studytype ");
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_NON_BRAND_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,nonBrand);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and nonBrand ==>"+ nonBrand;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return projects;
    }
    
    public List<Project> getSpendByNonBrandLatestCost(final Integer budgetYear, final Integer nonBrand, String brandtype, SpendReportExtractFilter spendReportFilter, List<Long> projectIds)
    {
    	List<Project> projects = Collections.emptyList();
    			
    	StringBuilder sql = new StringBuilder();
    	sql.append("select studytype, sum(totalcost) as totalcost from grailproject where iscancel=0 and status >0 and budgetyear = ");
    	sql.append(budgetYear.toString());
    	sql.append(" and studytype=");
    	sql.append(nonBrand.toString());
    	sql.append(" and brandspecificstudy = 2 ");
    	
    	if(projectIds!=null && projectIds.size()>0)
	    {
	     	sql.append(" AND projectId in (");
	      	sql.append(StringUtils.join(projectIds, ","));
	      	sql.append(")");
	    }
    	
    	
    	sql.append(applySpendReportFilter(spendReportFilter));
    	sql.append(" group by studytype ");
    	
        try {
           // projects = getSimpleJdbcTemplate().getJdbcOperations().query(GET_SPEND_BY_NON_BRAND_LATEST_COST, methodologyLatestCostRowMapper, budgetYear,nonBrand);
        	 projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), methodologyLatestCostRowMapper);
        }
        catch (DataAccessException e) {
            final String message = "Failed to Spend By Latest Cost for Budget Year ==> "+ budgetYear +" and nonBrand ==>"+ nonBrand;
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
        
      
        StringBuilder sql = new StringBuilder();
        if(projectResultFilter.isFetchEndMarketProjects() || projectResultFilter.isFetchGlobalProjects() || projectResultFilter.isFetchRegionalProjects() || projectResultFilter.isFetchOnlyDraftProjects())
        {
        	sql.append(getProjectDashboardQuery(projectResultFilter,false));
        	
        	LOG.info("PROJECT FILTER QUERY ==>"+ sql.toString());
        	LOG.debug("PROJECT FILTER QUERY ==>"+ sql.toString());
        	System.out.println("PROJECT FILTER QUERY ==>"+ sql.toString());
        	
        	
        	// The latest project Saved as Draft should come first : http://redmine.nvish.com/redmine/issues/453
        	if(projectResultFilter.isFetchOnlyDraftProjects())
	        {
        		// http://redmine.nvish.com/redmine/issues/494
        		if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals(""))
        		{
        			sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder()));
        		}
        		else
        		{
        			sql.append(" order by p.modificationdate ").append(SynchroDAOUtil.getSortType(1));
        		}
	        }
	        else if(projectResultFilter.getSortField() != null && !projectResultFilter.getSortField().equals("")) {
	           // This for handling the sorting for cancelled projects . //http://redmine.nvish.com/redmine/issues/459
	        	if(projectResultFilter.getSortField().equals("status"))
	            {
	            	sql.append(" order by p.iscancel ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(", ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
	            }
	            else
	            {
	            	sql.append(" order by ").append(getOrderByField(projectResultFilter.getSortField())).append(" ").append(SynchroDAOUtil.getSortType(projectResultFilter.getAscendingOrder())).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
	            }
	        } else {
	            //sql.append(" order by ").append(getOrderByField("status")).append(" ").append(SynchroDAOUtil.getSortType(0)).append(",").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
	        	// By default the projects should be sorted in order of ProjectId in Ascending order. 
	        	sql.append(" order by ").append(getOrderByField("id")).append(" ").append(SynchroDAOUtil.getSortType(0));
	        }
	        try {
	            if(projectResultFilter.getStart() != null) {
	                sql.append(" OFFSET ").append(projectResultFilter.getStart());
	            }
	            if(projectResultFilter.getLimit() != null && projectResultFilter.getLimit() > 0) {
	                sql.append(" LIMIT ").append(projectResultFilter.getLimit());
	            }
	            projects = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), projectDashboardRowMapper);
	        }
	        catch (DataAccessException e) {
	            final String message = "Failed to load projects by filter";
	            LOG.error(message, e);
	            throw new DAOException(message, e);
	        }
        
        }
        else
        {
        	String projectFields =  "p.projectID, p.name, p.description, p.descriptiontext, p.categoryType, p.brand, p.methodologyType, " +
	                " p.methodologyGroup, p.proposedMethodology, p.startDate, p.endDate, p.newsynchro, p.methodologydetails, p.methwaiverreq, p.brandspecificstudy," +
	                " p.projecttype, p.processtype, p.methwaiverreq, p.brandspecificstudy, p.studytype, p.budgetlocation, p.refsynchrocode, p.projectmanager, p.fieldworkstudy, p.iscancel, p.endmarketfunding,  p.globalprojectoutcome, " +
	                " (CASE WHEN (p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment fi1 where p.projectid = fi1.projectid AND fi1.isabovemarket = 1) > 0) THEN (SELECT fi.projectContact FROM grailfundinginvestment fi where p.projectid = fi.projectid order by fi.investmenttype offset 0 limit 1) ELSE p.projectOwner END) as projectOwner, " +
	                " p.briefCreator, p.multiMarket, p.totalCost, p.totalCostCurrency, " +
	                " p.creationby, p.modificationby, p.creationdate, p.modificationdate, p.status, p.caprating,p.isconfidential,p.area,p.region , p.issave, p.budgetyear,p.agencyDept, p.projectsavedate, p.projectstartdate";
	
	         sql = new StringBuilder("SELECT " + projectFields +
	                ", (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = (CASE WHEN (p.multimarket = 1 AND (SELECT count(*) FROM grailfundinginvestment fi1 where p.projectid = fi1.projectid AND fi1.isabovemarket = 1) > 0) THEN (SELECT fi.projectContact FROM grailfundinginvestment fi where p.projectid = fi.projectid order by fi.investmenttype offset 0 limit 1) ELSE p.projectOwner END)) as ownerName" +
	                ", (select b.name from grailBrandFields b where b.id=p.brand) as brandName" +
	                ", (CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END) as spiContact" +
	                ", (select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = (CASE WHEN (p.multimarket is null OR p.multimarket = 0) THEN (SELECT ei.spiContact FROM grailendmarketinvestment ei where p.projectid = ei.projectid) ELSE p.projectOwner END)) as spiContactName" +
	                " FROM grailproject p");
	        sql.append(applyProjectFilter(projectResultFilter));
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
	        
        }
	        //sql.append(getOrderByClause(projectResultFilter.getSortField(), projectResultFilter.getAscendingOrder()));
        
        return projects;
    }

    private String getProjectDashboardQuery(final ProjectResultFilter projectResultFilter, boolean fetchCount)
    {
    	StringBuilder sql = new StringBuilder();
        if(projectResultFilter.isFetchEndMarketProjects() || projectResultFilter.isFetchGlobalProjects() || projectResultFilter.isFetchRegionalProjects() || projectResultFilter.isFetchOnlyDraftProjects())
        {
        	String projectFields="";
        	
        	
	        if(projectResultFilter.isFetchEndMarketProjects())
	        {
	        	String todayDate = SynchroUtils.getDateString(new Date().getTime());
	        	if(fetchCount)
	        	{
	        		projectFields = "select count(*)  from grailproject p ";
	        	}
	        	else 
	        	{
	        		projectFields = "select p.projectid, p.name , p.budgetyear, p.projecttype, p.processtype, p.startdate, p.enddate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel, p.onlyglobaltype, p.ismigrated, p.hasnewsynchrosaved, (SELECT em.name FROM grailendmarketinvestment ei, grailendmarketfields em where p.projectid = ei.projectid and ei.endmarketid = em.id) as EndMarket, (CASE WHEN ((p.status IN (1,2) AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p.status IN (1,2,3,4,5) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')) THEN 'Not On Track' ELSE 'On Track' END) as projectTrackStatus  from grailproject p ";
	        	}
		        sql = new StringBuilder(projectFields);
		        sql.append(" where p.status IN ("+ StringUtils.join(projectResultFilter.getProjectStatusFields(), ",")+") ");
	        	sql.append(" and p.projecttype = 3");
	        	if(SynchroPermHelper.isGlobalUserType())
	        	{
	        		
	        	}
	        	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
	        	else if(SynchroPermHelper.isRegionalUserType())
	        	{
	        		/*List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
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
	        			sql.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
		        		sql.append(StringUtils.join(emIds, ","));
		        		sql.append("))");
	        		}*/
	        		
	        		sql.append(" AND p.budgetlocation in (");
	        		// This is done as per http://redmine.nvish.com/redmine/issues/343
	        		//List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
	        		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
	        		
	        		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
	        		if(SynchroPermHelper.isEndMarketUserType())
	        		{
	        			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	        			regionBudgetLocations.addAll(emBudgetLocations);
	        		}
	        		
	        		sql.append(StringUtils.join(regionBudgetLocations, ","));
	        		sql.append(")");
	        		
	        	}
	        	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
	        	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
	        	else if(SynchroPermHelper.isEndMarketUserType())
	        	{
	        		/*sql.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
	        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	        		sql.append(StringUtils.join(emBudgetLocations, ","));
	        		sql.append("))");*/
	        		sql.append(" AND p.budgetlocation in (");
	        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	        		sql.append(StringUtils.join(emBudgetLocations, ","));
	        		sql.append(")");
	        	}
	        		
	        }
	        else if (projectResultFilter.isFetchGlobalProjects())
	        {
	        	//projectFields = "select p.projectid, p.name , p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel,  'MutilMarket' as EndMarket from grailproject p ";
	        	String todayDate = SynchroUtils.getDateString(new Date().getTime());
	        	if(fetchCount)
	        	{
	        		projectFields = "select count(*)  from grailproject p ";
	        	}
	        	else
	        	{
	        		projectFields = "select p.projectid, p.name , p.projecttype, p.budgetyear, p.processtype, p.startdate,  p.enddate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel, p.onlyglobaltype, p.ismigrated, p.hasnewsynchrosaved, 'MultiMarket' as EndMarket , (CASE WHEN ((p.status = 1 AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p.status IN (1,2,3,4,5) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')) THEN 'Not On Track' ELSE 'On Track' END) as projectTrackStatus from grailproject p ";
	        	}
		        sql = new StringBuilder(projectFields);
		        sql.append(" where p.status IN ("+ StringUtils.join(projectResultFilter.getProjectStatusFields(), ",")+") ");
	        	sql.append(" and p.projecttype = 1");
	        	
	        	// http://redmine.nvish.com/redmine/issues/444
	        	if(SynchroPermHelper.isLegaUserType())
	        	{
	        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	        		if(emBudgetLocations!=null && emBudgetLocations.size()>0)
	        		{
	        			sql.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
		        		sql.append(StringUtils.join(emBudgetLocations, ","));
		        		sql.append("))");
	        		}
	        		
	        	}
	        	
	        }
	        else if (projectResultFilter.isFetchRegionalProjects())
	        {
	        	//projectFields = "select p.projectid, p.name , p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel,  'MutilMarket' as EndMarket from grailproject p ";
	        	String todayDate = SynchroUtils.getDateString(new Date().getTime());
	        	//projectFields = "select p.projectid, p.name , p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel,  'MultiMarket' as EndMarket , (CASE WHEN ((p.status = 1 AND p.startDate > "+todayTimeStamp+") OR (p.status IN (1,2,3) AND p.endDate > "+todayTimeStamp+")) THEN 'Not On Track' ELSE 'On Track' END) as projecttrackstatus from grailproject p";
	        	if(fetchCount)
	        	{
	        		projectFields = "select count(*)  from grailproject p ";
	        	}
	        	else
	        	{	
	        		projectFields = "select p.projectid, p.name , p.projecttype, p.budgetyear, p.processtype, p.startdate,  p.enddate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel, p.onlyglobaltype, p.ismigrated, p.hasnewsynchrosaved, 'MultiMarket' as EndMarket , (CASE WHEN ((p.status = 1 AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p.status IN (1,2,3,4,5) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')) THEN 'Not On Track' ELSE 'On Track' END) as projectTrackStatus from grailproject p ";
	        	}
		        sql = new StringBuilder(projectFields);
		        sql.append(" where p.status IN ("+ StringUtils.join(projectResultFilter.getProjectStatusFields(), ",")+") ");
	        	sql.append(" and p.projecttype = 2");
	        	
	        	if(SynchroPermHelper.isGlobalUserType())
	        	{
	        		
	        	}
	        	// This will fetch all the Regional projects for the particular end markets for the which the user the associated with
	        	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/343
	        	else if(SynchroPermHelper.isRegionalUserType())
	        	{
	        		/*List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
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
	        			sql.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
		        		sql.append(StringUtils.join(emIds, ","));
		        		sql.append("))");
	        		}*/
	        		
	        		sql.append(" AND p.budgetlocation in (");
	        		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
	        		sql.append(StringUtils.join(regionBudgetLocations, ","));
	        		sql.append(")");
	        		
	        	}
	        	
	        	// http://redmine.nvish.com/redmine/issues/444
	        	if(SynchroPermHelper.isLegaUserType())
	        	{
	        		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
	        		if(emBudgetLocations!=null && emBudgetLocations.size()>0)
	        		{
	        			sql.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
		        		sql.append(StringUtils.join(emBudgetLocations, ","));
		        		sql.append("))");
	        		}
	        		
	        	}
	        }
	        // Fetch Only Draft Projects correspond to each user
	        else if (projectResultFilter.isFetchOnlyDraftProjects())
	        {

	        	//projectFields = "select p.projectid, p.name , p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel,  'MutilMarket' as EndMarket from grailproject p ";
	        	String todayDate = SynchroUtils.getDateString(new Date().getTime());
	        	//projectFields = "select p.projectid, p.name , p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel,  'MultiMarket' as EndMarket , (CASE WHEN ((p.status = 1 AND p.startDate > "+todayTimeStamp+") OR (p.status IN (1,2,3) AND p.endDate > "+todayTimeStamp+")) THEN 'Not On Track' ELSE 'On Track' END) as projecttrackstatus from grailproject p";
	        	if(fetchCount)
	        	{
	        		projectFields = "select count(*)  from grailproject p ";
	        	}
	        	else
	        	{	
	        		projectFields = "select p.projectid, p.name , p.projecttype, p.budgetyear, p.processtype, p.startdate,  p.enddate, p.projectmanager, p.methodologydetails, p.totalcost, p.status, p.newsynchro, p.fieldworkstudy, p.iscancel, p.onlyglobaltype, p.ismigrated, p.hasnewsynchrosaved,  'MultiMarket' as EndMarket , (CASE WHEN ((p.status = 1 AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p.status IN (1,2,3,4,5) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')) THEN 'Not On Track' ELSE 'On Track' END) as projectTrackStatus from grailproject p ";
	        	}
		        sql = new StringBuilder(projectFields);
		        sql.append(" where p.status IN (0)");
	        	sql.append(" and p.projecttype in (1,2,3)");
	        	
	        	// Admin user will have access to all the Draft projects
	        	if(!SynchroPermHelper.isSystemAdmin())
	        	{
	        		sql.append(" and p.briefcreator ="+ SynchroPermHelper.getEffectiveUser().getID());
	        	}
	        	
	        	
	        
	        }
	        else
	        {
	        	
	        }
	        if(projectResultFilter.isFetchCancelProjects())
	        {
	        	sql.append(" and p.iscancel IN (0,1) ");
	        }
	        else
	        {
	        	sql.append(" and p.iscancel IN (0) ");
	        }
	        
	        // Check for Legal Users as they can access on EM/GLOBAL/REGIONAL projects with EU markets
	        // And Legal Users can access the Fieldwork projects also
	        if(SynchroPermHelper.isLegaUserType())
	        {
	        	//sql.append(" AND p.processtype in (1,5)");
	        	sql.append(" AND p.processtype in (1,5,4)");
	        }
	        sql.append(applyProjectFilterNew(projectResultFilter));
	        
	        // Code for adding filters on project codes for fetching data.
	        Long startProjectCode = JiveGlobals.getJiveLongProperty("start.project.code", new Long("-1"));
	        Long endProjectCode = JiveGlobals.getJiveLongProperty("end.project.code", new Long("-1"));
	        
	        if(startProjectCode > -1 && endProjectCode > -1)
	        {
	        	sql.append(" and p.projectID >="+ startProjectCode);
	        	sql.append(" and p.projectID <="+ endProjectCode);
	        }
	        
	        // This code is for fetching the report for a particular budget year
	        Long budgetYear = projectResultFilter.getBudgetYearSelected();
	        if(budgetYear!=null && budgetYear.intValue() > -1)
	        {
	        	sql.append(" and p.budgetyear ="+ budgetYear);
	        }
	        
        }
	        return sql.toString();
    }
    
    @Override
    public Long getProjectDashboardCount(final ProjectResultFilter filter) {
        Long count = 0L;
        StringBuilder sql = new StringBuilder();
        sql.append(getProjectDashboardQuery(filter,true));
       
        try {
            count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql.toString());
        }
        catch (DataAccessException e) {
            final String message = "Failed to load dashboard project count by filter";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return count;
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


    private String applyProjectFilterNew(final ProjectResultFilter projectResultFilter) {
    	
        StringBuilder sql = new StringBuilder();
        // Keyword filter
           if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
               StringBuilder keywordCondition = new StringBuilder();
               
               List<Integer> status = new ArrayList<Integer>();
               if(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(1);
               }
               if(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(2);
               }
               if(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(3);
               }
               if(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(4);
               }
               if(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(5);
               }
               if(SynchroGlobal.ProjectStatusNew.CLOSE.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   status.add(6);
               }
               
               
               keywordCondition.append(" AND (lower(p.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                       .append(" OR ").append("(''|| p.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(projectResultFilter.getKeyword())).append("%'")
                      // .append(" OR ").append("lower(methodologyapproverName) like ").append("'%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                     //  .append(" OR ").append("project.budgetyear = ").append(projectResultFilter.getKeyword().toLowerCase()).append(")");
                       .append(" OR to_char(to_timestamp(p.startdate/1000),'DD/MM/YYYY') like '%").append(projectResultFilter.getKeyword()).append("%'")
                       .append(" OR lower(p.projectmanager) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%'")
                       .append(" OR p.projectid in (select e.projectid from grailendmarketinvestment e, grailendmarketfields ef where lower(ef.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%' and e.endmarketid = ef.id )")
                       .append(" OR (''|| Round(p.totalcost, 2) ||'') like '%").append(projectResultFilter.getKeyword()).append("%'");
               
              // Status Keyword
               if(status.size()>0)
               {
            	   keywordCondition.append(" OR p.status in (").append(StringUtils.join(status,",")).append(")");
               }
               
               // Project Stage Keyword Track/Not On Track
               if(SynchroConstants.SYNCHRO_PROJECT_ON_TRACK.toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   String todayDate = SynchroUtils.getDateString(new Date().getTime());
            	   keywordCondition.append(" OR p.projectid not in (select projectid from grailproject p1 where ((p1.status IN (1,2) AND to_char(to_timestamp(p1.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p1.status IN (1,2,3,4,5) AND to_char(to_timestamp(p1.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')))");
               }
               if(SynchroConstants.SYNCHRO_PROJECT_NOT_ON_TRACK.toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	   String todayDate = SynchroUtils.getDateString(new Date().getTime());
            	   keywordCondition.append(" OR ((p.status IN (1,2) AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p.status IN (1,2,3,4,5) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"'))");
               }

               //Cancelled Project Stage Keyword
               if(SynchroGlobal.ProjectStatusNew.CANCEL.getValue().toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
               {
            	  keywordCondition.append(" OR p.iscancel = 1 ");
               }

               
               // Methodologies Keyword
               Map<Integer, String> allMethodologies = SynchroGlobal.getMethodologies();
               List<Integer> methodologies = new ArrayList<Integer>();
               if(allMethodologies!=null)
               {
            	   for(Integer methId : allMethodologies.keySet())
            	   {
            		   if(allMethodologies.get(methId).toLowerCase().contains(projectResultFilter.getKeyword().toLowerCase()))
            		   {
            			   methodologies.add(methId);
            		   }
            	   }
            	   if(methodologies!=null && methodologies.size()>0)
            	   {
            		   keywordCondition.append(" OR (p.methodologydetails in ('").append(methodologies.get(0)+"").append("')");
		               if(methodologies.size()>0)
		               {
		            	   keywordCondition.append(" or p.methodologydetails like ('").append(methodologies.get(0)+"").append(",%')");
		            	   keywordCondition.append(" or p.methodologydetails like ('%,").append(methodologies.get(0)+"").append("')");
		            	   keywordCondition.append(" or p.methodologydetails like ('%,") .append(methodologies.get(0)+"").append(",%')");
			               	for(int i=1;i<methodologies.size();i++)
			               	{
			               		keywordCondition.append(" or p.methodologydetails in ('").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or p.methodologydetails like ('").append(methodologies.get(i)+"").append(",%')");
			               		keywordCondition.append(" or p.methodologydetails like ('%,").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or p.methodologydetails like ('%,").append(methodologies.get(i)+"").append(",%')");
			               	}
		               }
		               keywordCondition.append(")");
            	   }
            	   
               }
               
               
               keywordCondition.append(")");
               
               
               sql.append(keywordCondition.toString());
           }
          
           //Project Type Filter
           if(projectResultFilter.getProjectTypes() != null && projectResultFilter.getProjectTypes().size()>0 && !isListNull(projectResultFilter.getProjectTypes())) {
               StringBuilder projectTypesCondition = new StringBuilder();
               
               List<Integer> projectStatus = getProjectStatusFromTypes(projectResultFilter.getProjectTypes());
               boolean selectCancel = false;
               
               // This check for filtering the Cancel projects as well
               for(Integer ps: projectStatus)
               {
            	   if(ps==SynchroGlobal.ProjectStatusNew.CANCEL.ordinal())
            	   {
            		   selectCancel = true;
            	   }
               }
               
               if(selectCancel)
               {
            	   projectTypesCondition.append(" AND (p.status in (");
	               
	               
	               projectTypesCondition.append(StringUtils.join(projectStatus,","));
	               projectTypesCondition.append(") OR p.iscancel IN (1))");
	               sql.append(projectTypesCondition.toString());
               }
               else
               {
	               projectTypesCondition.append(" AND p.status in (");
	               
	               
	               projectTypesCondition.append(StringUtils.join(projectStatus,","));
	               projectTypesCondition.append(")");
	               sql.append(projectTypesCondition.toString());
               }
               
           }
           
         //Project Status Filter
           if(projectResultFilter.getProjectStatus() != null && projectResultFilter.getProjectStatus().size()>0 && !isListNull(projectResultFilter.getProjectStatus())) {
               StringBuilder projectStatusCondition = new StringBuilder();
               
               String todayDate = SynchroUtils.getDateString(new Date().getTime());
               
               // If only one of the Project Statys On-Track/Not-On-Track is selected, then only filter the projects.
               if(projectResultFilter.getProjectStatus().size()==1)
               {
	               if(projectResultFilter.getProjectStatus().get(0)==1)
	               {
	            	   //On-Track
	            	   //projectStatusCondition.append(" AND ((p.status > 1 AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+todayDate+"' ) OR (p.status > 3 AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+todayDate+"'))");
	            	   projectStatusCondition.append(" AND p.projectid not in (select projectid from grailproject p1 where ((p1.status IN (1,2) AND to_char(to_timestamp(p1.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p1.status IN (1,2,3) AND to_char(to_timestamp(p1.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"')))");
	            	   
	               }
	               else
	               {
	            	 //Not On-Track
	            	   projectStatusCondition.append(" AND ((p.status IN (1,2) AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') < '"+todayDate+"' ) OR (p.status IN (1,2,3) AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') < '"+todayDate+"'))");
	               }
	               sql.append(projectStatusCondition.toString());
               }
               
           }
           
         //Project Stage Filter
           if(projectResultFilter.getProjectStages() != null && projectResultFilter.getProjectStages().size()>0 && !isListNull(projectResultFilter.getProjectStages())) {
               StringBuilder projectStagesCondition = new StringBuilder();
               
               List<Integer> projectStatus = getProjectStatusFromStages(projectResultFilter.getProjectStages());
               boolean selectCancel = false;
               
               // This check for filtering the Cancel projects as well
               for(Integer ps: projectStatus)
               {
            	   if(ps==SynchroGlobal.ProjectStatusNew.CANCEL.ordinal())
            	   {
            		   selectCancel = true;
            	   }
               }
               
               if(selectCancel)
               {
            	  
	               
	               projectStagesCondition.append(" AND (p.status in (");
	               projectStagesCondition.append(StringUtils.join(projectStatus,","));
	               projectStagesCondition.append(") OR p.iscancel IN (1))");
	               sql.append(projectStagesCondition.toString());
               }
               else
               {
            	   projectStagesCondition.append(" AND p.status in (");
                   projectStagesCondition.append(StringUtils.join(projectStatus,","));
                   projectStagesCondition.append(")");
                   sql.append(projectStagesCondition.toString());
               }
               
               
              
               
           }
           
           // Start Date Filter
           
           if(projectResultFilter.getStartDateBegin() != null && projectResultFilter.getStartDateComplete()!=null) {
               StringBuilder startDateCondition = new StringBuilder();
               String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
               String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
               
               sql.append(startDateCondition.toString());
               
           }
           else if(projectResultFilter.getStartDateBegin() != null)
           {
        	   StringBuilder startDateCondition = new StringBuilder();
               String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'");
               
               sql.append(startDateCondition.toString());
           }
           else if(projectResultFilter.getStartDateComplete()!=null)
           {
        	   StringBuilder startDateCondition = new StringBuilder();
               String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
               startDateCondition.append(" AND to_char(to_timestamp(p.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
               
               sql.append(startDateCondition.toString());
           }
          
           // End Date Filter
           if(projectResultFilter.getEndDateBegin() != null && projectResultFilter.getEndDateComplete()!=null) {
               StringBuilder endDateCondition = new StringBuilder();
               String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
               String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
               endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
               
               sql.append(endDateCondition.toString());
               
           }
           else if (projectResultFilter.getEndDateBegin() != null)
           {
        	   StringBuilder endDateCondition = new StringBuilder();
               String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
               endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'");
               sql.append(endDateCondition.toString());
           }
           else if (projectResultFilter.getEndDateComplete()!=null)
           {
        	   StringBuilder endDateCondition = new StringBuilder();
               String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
               endDateCondition.append(" AND to_char(to_timestamp(p.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
               
               sql.append(endDateCondition.toString());
           }
        
           // Project Manager filter
           if(projectResultFilter.getProjManager() != null && !projectResultFilter.getProjManager().equals("")) {
               StringBuilder projectManagerCondition = new StringBuilder();
               projectManagerCondition.append(" AND p.projectmanager like '%"+projectResultFilter.getProjManager()+"%'");
               
               sql.append(projectManagerCondition.toString());
           }
           
        // Project Initiator filter
           if(projectResultFilter.getProjectInitiator() != null && !projectResultFilter.getProjectInitiator().equals("")) {
               StringBuilder projectInitiatorCondition = new StringBuilder();
               projectInitiatorCondition.append(" AND p.briefcreator in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+projectResultFilter.getProjectInitiator()+"%'");
               projectInitiatorCondition.append(")");
               sql.append(projectInitiatorCondition.toString());
           }
           
         //Category Types Filter
           if(projectResultFilter.getCategoryTypes() != null && projectResultFilter.getCategoryTypes().size()>0 && !isListNull(projectResultFilter.getCategoryTypes())) {
               StringBuilder categoryTypesCondition = new StringBuilder();
               /*categoryTypesCondition.append(" AND p.categorytype in (");
               categoryTypesCondition.append(StringUtils.join(projectResultFilter.getCategoryTypes(),","));
               categoryTypesCondition.append(")");
               sql.append(categoryTypesCondition.toString());
               */
               
               

             
               categoryTypesCondition.append(" AND (p.categorytype in ('")
                       .append(projectResultFilter.getCategoryTypes().get(0)+"").append("')");
               if(projectResultFilter.getCategoryTypes().size()>0)
               {
	            	categoryTypesCondition.append(" or p.categorytype like ('").append(projectResultFilter.getCategoryTypes().get(0)+"").append(",%')");
	            	categoryTypesCondition.append(" or p.categorytype like ('%,").append(projectResultFilter.getCategoryTypes().get(0)+"").append("')");
	            	categoryTypesCondition.append(" or p.categorytype like ('%,") .append(projectResultFilter.getCategoryTypes().get(0)+"").append(",%')");
	               	for(int i=1;i<projectResultFilter.getCategoryTypes().size();i++)
	               	{
	               		categoryTypesCondition.append(" or p.categorytype in ('").append(projectResultFilter.getCategoryTypes().get(i)+"").append("')");
	               		categoryTypesCondition.append(" or p.categorytype like ('").append(projectResultFilter.getCategoryTypes().get(i)+"").append(",%')");
	               		categoryTypesCondition.append(" or p.categorytype like ('%,").append(projectResultFilter.getCategoryTypes().get(i)+"").append("')");
	               		categoryTypesCondition.append(" or p.categorytype like ('%,").append(projectResultFilter.getCategoryTypes().get(i)+"").append(",%')");
	               	}
               }
               categoryTypesCondition.append(")");
               LOG.info("Category Types Filter Query - " + categoryTypesCondition.toString());
               sql.append(categoryTypesCondition.toString());
           
               
               
           } 
           //Methodology Details Filter
           if(projectResultFilter.getMethDetails() != null && projectResultFilter.getMethDetails().size()>0 && !isListNull(projectResultFilter.getMethDetails())) {
               StringBuilder methodologyDetailsCondition = new StringBuilder();
              
               
               methodologyDetailsCondition.append(" AND (p.methodologydetails in ('")
                       .append(projectResultFilter.getMethDetails().get(0)+"").append("')");
               if(projectResultFilter.getMethDetails().size()>0)
               {
            	   methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(projectResultFilter.getMethDetails().get(0)+"").append(",%')");
            	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(0)+"").append("')");
            	   methodologyDetailsCondition.append(" or p.methodologydetails like ('%,") .append(projectResultFilter.getMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<projectResultFilter.getMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or p.methodologydetails in ('").append(projectResultFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('").append(projectResultFilter.getMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or p.methodologydetails like ('%,").append(projectResultFilter.getMethDetails().get(i)+"").append(",%')");
	               	}
               }
               methodologyDetailsCondition.append(")");
               LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
               sql.append(methodologyDetailsCondition.toString());
               
           }
         //Methodology Type Filter
           if(projectResultFilter.getMethodologyTypes() != null && projectResultFilter.getMethodologyTypes().size()>0 && !isListNull(projectResultFilter.getMethodologyTypes())) {
               StringBuilder methodologyTypesCondition = new StringBuilder();
               methodologyTypesCondition.append(" AND p.methodologytype in (");
               methodologyTypesCondition.append(StringUtils.join(projectResultFilter.getMethodologyTypes(),","));
               methodologyTypesCondition.append(")");
               sql.append(methodologyTypesCondition.toString());
               
           }
           
         //Brand Filter
           if(projectResultFilter.getBrands() != null && projectResultFilter.getBrands().size()>0 && !isListNull(projectResultFilter.getBrands())) {
               StringBuilder brandsCondition = new StringBuilder();
               brandsCondition.append(" AND (p.brand in (");
               brandsCondition.append(StringUtils.join(projectResultFilter.getBrands(),","));
               brandsCondition.append(")");
               brandsCondition.append(" OR p.studytype in (");
               brandsCondition.append(StringUtils.join(projectResultFilter.getBrands(),","));
               brandsCondition.append("))");
               sql.append(brandsCondition.toString());
               
           }
           
           //Budget Location Filter
           if(projectResultFilter.getBudgetLocations() != null && projectResultFilter.getBudgetLocations().size()>0 && !isListNull(projectResultFilter.getBudgetLocations())) {
               StringBuilder budgetLocationsCondition = new StringBuilder();
               budgetLocationsCondition.append(" AND p.budgetlocation in (");
               budgetLocationsCondition.append(StringUtils.join(projectResultFilter.getBudgetLocations(),","));
               budgetLocationsCondition.append(")");
               sql.append(budgetLocationsCondition.toString());
               
           }
           
           //BudgetYear Filter
           if(projectResultFilter.getBudgetYears() != null && projectResultFilter.getBudgetYears().size()>0 && !isListNull(projectResultFilter.getBudgetYears())) {
               StringBuilder budgetYearsCondition = new StringBuilder();
               budgetYearsCondition.append(" AND p.budgetyear in (");
               budgetYearsCondition.append(StringUtils.join(projectResultFilter.getBudgetYears(),","));
               budgetYearsCondition.append(")");
               sql.append(budgetYearsCondition.toString());
               
           }
           
         //Research End Market Filter
           if(projectResultFilter.getResearchEndMarkets() != null && projectResultFilter.getResearchEndMarkets().size()>0 && !isListNull(projectResultFilter.getResearchEndMarkets())) {
               StringBuilder researchEndMarketsCondition = new StringBuilder();
               researchEndMarketsCondition.append(" AND p.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
               researchEndMarketsCondition.append(StringUtils.join(projectResultFilter.getResearchEndMarkets(),","));
               researchEndMarketsCondition.append("))");
               sql.append(researchEndMarketsCondition.toString());
               
           }
           
         //Research Agency Filter
           if(projectResultFilter.getResearchAgencies() != null && projectResultFilter.getResearchAgencies().size()>0 && !isListNull(projectResultFilter.getResearchAgencies())) {
               StringBuilder researchAgenciesCondition = new StringBuilder();
               researchAgenciesCondition.append(" AND p.projectid in (select pcd.projectid from grailprojectcostdetails pcd where pcd.agencyid in (");
               researchAgenciesCondition.append(StringUtils.join(projectResultFilter.getResearchAgencies(),","));
               researchAgenciesCondition.append("))");
               sql.append(researchAgenciesCondition.toString());
               
           }
           
         //Total Cost Filter
           if(projectResultFilter.getTotalCostStart() != null && projectResultFilter.getTotalCostEnd() != null) {
               StringBuilder totalCostsCondition = new StringBuilder();
               totalCostsCondition.append(" AND p.totalcost >="+ projectResultFilter.getTotalCostStart() + " AND p.totalcost <="+ projectResultFilter.getTotalCostEnd());
                            
               sql.append(totalCostsCondition.toString());
               
           }
           else if(projectResultFilter.getTotalCostStart() != null)
           {
        	   StringBuilder totalCostsCondition = new StringBuilder();
               totalCostsCondition.append(" AND p.totalcost >="+ projectResultFilter.getTotalCostStart());
                            
               sql.append(totalCostsCondition.toString());
           }
           else if(projectResultFilter.getTotalCostEnd() != null)
           {
        	   StringBuilder totalCostsCondition = new StringBuilder();
               totalCostsCondition.append(" AND p.totalcost <="+ projectResultFilter.getTotalCostEnd());
                            
               sql.append(totalCostsCondition.toString());
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
    private List<Integer> getProjectStatusFromTypes(List<Long> projectTypes)
    {
    	List<Integer> projectStatus = new ArrayList<Integer>();
    	for(Long projectType:projectTypes)
    	{
    		if(projectType==1)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal());
    			
    		}
    		if(projectType==2)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    		}
    		if(projectType==3)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal());
    		}
    		if(projectType==4)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal());
    		}
    	}
    	return projectStatus;
    }
    
    private List<Integer> getProjectStatusFromStages(List<Long> projectStages)
    {
    	List<Integer> projectStatus = new ArrayList<Integer>();
    	for(Long projectStage:projectStages)
    	{
    		if(projectStage==1)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal());
    			
    			
    		}
    		if(projectStage==2)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal());
    		}
    		if(projectStage==3)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal());
       		}
    		if(projectStage==4)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CLOSE.ordinal());
       		}
    		if(projectStage==5)
    		{
    			projectStatus.add(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal());
    		}
    	}
    	return projectStatus;
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
            } else if(sortField.equals("projectStartDate")) {
                field = "p.startDate";
            } 
            else {
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
            } else if(sortField.equals("projectStartDate")) {
                field = "p.startDate";
            } else if(sortField.equals("projectManager")) {
                field = "p.projectmanager";
            }
            else {
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
           
            project.setMethodologyType(rs.getLong("methodologyType"));
            project.setMethodologyGroup(rs.getLong("methodologyGroup"));
           // project.setProposedMethodology(synchroDAOUtil.getIDs(rs.getString("proposedMethodology")));
            project.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
            
            if(rs.getLong("startDate")>0)
            {
            	project.setStartDate(new Date(rs.getLong("startDate")));
            }
            if(rs.getLong("endDate")>0)
            {
            	project.setEndDate(new Date(rs.getLong("endDate")));
            }
            
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
           // project.setCapRating(rs.getLong("caprating"));
           // project.setConfidential(rs.getBoolean("isconfidential"));
           /* List<Long> spiContact = new ArrayList<Long>();
            if(rs.getLong("spiContact") > 0) {
                spiContact.add(rs.getLong("spiContact"));
            }
            project.setSpiContact(spiContact);
            project.setRegions(synchroDAOUtil.getIDs(rs.getString("region")));
            project.setAreas(synchroDAOUtil.getIDs(rs.getString("area")));
            project.setIsSave(rs.getBoolean("issave"));*/
            project.setBudgetYear(rs.getInt("budgetyear"));
            project.setMethWaiverReq(rs.getInt("methwaiverreq"));
            project.setBrandSpecificStudy(rs.getInt("brandspecificstudy"));
           
            project.setBudgetLocation(rs.getInt("budgetlocation"));
            project.setProjectManagerName(rs.getString("projectmanager"));
            project.setProjectType(rs.getInt("projecttype"));
            project.setProcessType(rs.getInt("processtype"));
            
            project.setNewSynchro(rs.getBoolean("newsynchro"));
            
            project.setFieldWorkStudy(rs.getInt("fieldworkstudy"));
            project.setRefSynchroCode(rs.getLong("refsynchrocode"));
            
            project.setIsCancel(rs.getBoolean("iscancel"));
            
            project.setEndMarketFunding(rs.getInt("endmarketfunding"));
            //project.setFundingMarkets(rs.getLong("fundingmarket"));
            project.setEuMarketConfirmation(rs.getInt("globalprojectoutcome"));
            project.setGlobalOutcomeEUShare(rs.getInt("globaloutcomeeushare"));
            
            project.setMultiBrandStudyText(rs.getString("multibrandstudytext"));
            
            project.setOnlyGlobalType(rs.getInt("onlyglobaltype"));
            
            // These fields are set for Spend By Reports 
            project.setRegion(SynchroUtils.getRegionName(project.getBudgetLocation()));
            project.setArea(SynchroUtils.getAreaName(project.getBudgetLocation()));
            project.setT20_40(SynchroUtils.getT20_T40Name(project.getBudgetLocation()));
            project.setMethodologies(rs.getString("methodologyDetails"));
            
            String methGroupName = "";
	    	try
	    	{
	    		methGroupName = SynchroGlobal.getMethodologyGroupName(new Long(project.getMethodologies()));
	    	}
	    	catch(Exception e)
	    	{
	    		
	    	}
	    	project.setMethGroup(methGroupName);
           
	    	if(project.getBrandSpecificStudy().intValue()==1)
            {
            	 project.setBrand(rs.getLong("brand"));
            	 project.setBrandSpecificStudyType(-1);
            }
            if(project.getBrandSpecificStudy().intValue()==2)
            {
            	 project.setBrandSpecificStudyType(rs.getInt("studytype"));
            	 project.setBrand(new Long("-1"));
            }
            
            if(project.getBrandSpecificStudy().intValue()==1)
    		{
	    		String brandType = SynchroUtils.getBrandBrandTypeFields().get(project.getBrand().intValue());
	    		if(brandType!=null)
	    		{
	    			project.setBrandType(brandType);
	    		}
	    		else
	    		{
	    			project.setBrandType("Non-GDB");
	    		}
    		}
    		else
    		{
    			project.setBrandType("Non-GDB");
    		}
            
            
            project.setCategories(rs.getString("categorytype"));
            
            project.setIsMigrated(rs.getBoolean("ismigrated"));
            
            project.setHasNewSynchroSaved(rs.getBoolean("hasnewsynchrosaved"));

         //   project.setAgencyDept(rs.getLong("agencyDept"));
            
          /*
            if(rs.getLong("projectsavedate") > 0) {
            	project.setProjectSaveDate(new Date(rs.getLong("projectsavedate")));
            }
            if(rs.getLong("projectstartdate") > 0) {
            	project.setProjectStartDate(new Date(rs.getLong("projectstartdate")));
            }*/
            return project;
        }
    };
    
    private final ParameterizedRowMapper<Project> budgetLocationLatestCostRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
            Project project = new Project();
            project.setTotalCost(rs.getBigDecimal("totalCost"));
            project.setBudgetLocation(rs.getInt("budgetlocation"));
            return project;
        }
    };
    
    private final ParameterizedRowMapper<Project> crossTabLatestCostRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
            Project project = new Project();
            project.setTotalCost(rs.getBigDecimal("totalCost"));
           // project.setBudgetLocation(rs.getInt("budgetlocation"));
            return project;
        }
    };
    
    private final ParameterizedRowMapper<Project> methodologyLatestCostRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
            Project project = new Project();
            project.setTotalCost(rs.getBigDecimal("totalCost"));
           // project.setBudgetLocation(rs.getInt("budgetlocation"));
            return project;
        }
    };
    
    private final ParameterizedRowMapper<Project> projectDashboardRowMapper = new ParameterizedRowMapper<Project>() {

        public Project mapRow(ResultSet rs, int row) throws SQLException {
            Project project = new Project();
            project.setProjectID(rs.getLong("projectID"));
            project.setName(rs.getString("name"));
            project.setMethodologyDetails(synchroDAOUtil.getIDs(rs.getString("methodologyDetails")));
            
            
            if(rs.getLong("startDate")>0)
            {
            	project.setStartDate(new Date(rs.getLong("startDate")));
            }
            
            if(rs.getLong("endDate")>0)
            {
            	project.setEndDate(new Date(rs.getLong("endDate")));
            }
           
            project.setTotalCost(rs.getBigDecimal("totalCost"));
           
            project.setStatus(rs.getLong("status"));
           // project.setCapRating(rs.getLong("caprating"));
          
          
            project.setProjectManagerName(rs.getString("projectmanager"));
            project.setProjectType(rs.getInt("projecttype"));
            project.setProcessType(rs.getInt("processtype"));
            project.setEndMarketName(rs.getString("endmarket"));
            project.setNewSynchro(rs.getBoolean("newsynchro"));
            project.setFieldWorkStudy(rs.getInt("fieldworkstudy"));
            project.setProjectTrackStatus(rs.getString("projecttrackstatus"));
            project.setBudgetYear(rs.getInt("budgetyear"));
            
            project.setIsCancel(rs.getBoolean("iscancel"));
            
            project.setOnlyGlobalType(rs.getInt("onlyglobaltype"));
            
            project.setIsMigrated(rs.getBoolean("ismigrated"));
            
            project.setHasNewSynchroSaved(rs.getBoolean("hasnewsynchrosaved"));
            
            
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
    
    @Override
    public void updateProjectNewSynchroFlag(final Long projectId, final Integer newSynchro) {
        try {

            getSimpleJdbcTemplate().update(UPDATE_PROJECT_NEW_SYNCHRO_PROJECT_ID,
            		newSynchro,	projectId );
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the project New Synchro Flag for new project ID  "+projectId ;
            LOG.error(message, e);
            throw new DAOException(message, e);
        }

    }
    
    @Override
    public void updateCancelProject(final Long projectId, final Integer status) {
        try {

            getSimpleJdbcTemplate().update(CANCEL_PROJECT,
            		status,	projectId );
        }
        catch (DataAccessException e) {
            final String message = "Failed to update the cancel project project ID  "+projectId ;
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
        if(filter.isFetchEndMarketProjects())
        {
        	sql.append(" where p.status IN ("+ StringUtils.join(filter.getProjectStatusFields(), ",")+") ");
	        if(filter.isFetchEndMarketProjects())
	        {
	        	sql.append(" and p.projecttype = 3");
	        }
        }
        else
        {
        	sql.append(applyProjectFilter(filter));
        }
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
           /* getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_PIT_NEW,  Joiner.on(",").join(project.getCategoryType()), Joiner.on(",").join(project.getProposedMethodology()),
                    project.getName(),
                    (project.getConfidential() != null && project.getConfidential())?1:0,
                    project.getBudgetYear(),
                    project.getProjectID()*/
        	
        	 getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_PIT_NEW,
                     project.getName(),
                     project.getDescription(),
                     project.getDescriptionText(),
                     project.getCategoryType()!=null?Joiner.on(",").join(project.getCategoryType()):"",
                     project.getBrand(),
                     project.getMethodologyType(),
                     project.getMethodologyDetails()!=null?Joiner.on(",").join(project.getMethodologyDetails()):"",
                     project.getStartDate().getTime(),
                     project.getEndDate().getTime(),
                    // project.getProjectOwner(),
                     project.getModifiedBy(),
                     project.getModifiedDate(),
                     project.getFieldWorkStudy(),
                     project.getMethWaiverReq(),
                     project.getBrandSpecificStudy(),
                     project.getBrandSpecificStudyType(),
                     project.getBudgetLocation(),
                     project.getBudgetYear(),
                     project.getProjectManagerName(),
                     project.getRefSynchroCode(),
                     project.getEndMarketFunding(),
                     project.getMultiBrandStudyText(),
                     (project.getHasNewSynchroSaved() != null && project.getHasNewSynchroSaved())?1:0,
                    // project.getFundingMarkets(),
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
    public void updateProjectMW(final Project project) {
        try {
         
        	
        	 getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_MW_NEW,
                     project.getMethWaiverReq(),
                     project.getProjectID()
            );
        }
        catch (DataAccessException e) {
            final String message = "Failed to update Project MW Field for project " + project.getProjectID();
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
           // count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(generatePendingActivitiesSQL(filter, true, userId, true, false));
            
            // count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(generatePendingActivitiesSQL(filter, true, userID, false, false));
        	String sql = "select count(*) from grailproject p";
        	count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
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
        //String sql = " select p.projectid, p.name as projectName, p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, 'Brief' as activityType from grailproject p, grailpib pib where p.processtype = 1 and pib.briefsendforapproval = 1 and pib.brieflegalapprover = "+userID+" and pib.briefapprovaldate <=0 and p.projectid = pib.projectid";

        			
        /*			pendingActivities = getSimpleJdbcTemplate().getJdbcOperations().query(sql.append(applyPendingActivitiesFilter(filter, userID, false)
                    , pendingActivityViewBeanParameterizedRowMapper);*/
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
        	//String sql  = "select count(*) from grailproject p";
        	//count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sql);
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
        
        StringBuilder pibDetailsSQL = new StringBuilder();
        StringBuilder proposalDetailsSQL = new StringBuilder();
        
        if(SynchroPermHelper.isSystemAdmin())
        {
        	pibDetailsSQL.append(" select p.projectid, p.name as projectName, p.status, p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, 'Brief Approval' as activityType, p.briefcreator, p.enddate, p.categorytype, p.methodologytype, p.budgetlocation, p.creationdate from grailproject p, grailpib pib where  pib.briefsendforapproval = 1 and pib.briefapprovaldate <=0 and p.projectid = pib.projectid");
	        proposalDetailsSQL.append(" select p.projectid, p.name as projectName, p.status, p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, 'Proposal Approval' as activityType, p.briefcreator, p.enddate, p.categorytype, p.methodologytype, p.budgetlocation, p.creationdate from grailproject p, grailproposal proposal where  proposal.proposalsendforapproval = 1  and proposal.proposalapprovaldate <=0 and p.projectid = proposal.projectid ");
        }
        else
        {
        	// Legal Approval Don't have to show Cancel Projects in their Pending Dashboard . http://redmine.nvish.com/redmine/issues/340
        	pibDetailsSQL.append(" select p.projectid, p.name as projectName, p.status, p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, 'Brief Approval' as activityType, p.briefcreator, p.enddate, p.categorytype, p.methodologytype, p.budgetlocation, p.creationdate from grailproject p, grailpib pib where  pib.briefsendforapproval = 1 and pib.brieflegalapprover = "+userID+" and pib.briefapprovaldate <=0 and p.projectid = pib.projectid and p.iscancel IN (0) ");
	        proposalDetailsSQL.append(" select p.projectid, p.name as projectName, p.status, p.projecttype, p.startdate, p.projectmanager, p.methodologydetails, 'Proposal Approval' as activityType, p.briefcreator, p.enddate, p.categorytype, p.methodologytype, p.budgetlocation, p.creationdate from grailproject p, grailproposal proposal where proposal.proposalsendforapproval = 1 and proposal.proposallegalapprover = "+userID+" and proposal.proposalapprovaldate <=0 and p.projectid = proposal.projectid and p.iscancel IN (0) ");
        }
        
        

        
        User user = null;
        try {
            user = getUserManager().getUser(userID);
        } catch (UserNotFoundException e) {

        }
        
        // Generate final SQL
        if(fetchTotalCount) {
            sql.append("select count(*) as count from (");
        } else {
//            sql.append("SELECT project.projectId, project.projectName,project.projectOwner,project.startYear,project.brandName,project.multimarket as multimarket,project.status FROM ");
            //sql.append("SELECT project.projectId, project.projectName,project.projectOwner,project.startYear,project.brandName,project.multimarket as multimarket,project.activityTypeId,project.status FROM ");
        }
        sql.append("select * from ");
        sql.append("(").append(pibDetailsSQL.toString());
        sql.append(" UNION ");
        sql.append(proposalDetailsSQL.toString());
        sql.append(") as project");


        sql.append(applyPendingActivitiesFilterNew(filter, userID, fetchUnViewed));

        if(!fetchTotalCount) {
            sql.append(applyPendingActivitiesOrderByClause(filter));
            sql.append(" OFFSET ").append(filter.getStart()).append(" LIMIT ").append(filter.getLimit());
        }
        if(fetchTotalCount)
        {
        	 sql.append(") as countview");
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
            keywordCondition.append(" (lower(project.name) like '%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(filter.getKeyword())).append("%'")
                    .append(" OR ").append("lower(project.projectmanager) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("lower(project.activityType) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(")");
            
            
            conditions.add(keywordCondition.toString());
        }

        // Name filter
        if(filter.getName() != null && !filter.getName().equals("")) {
            StringBuilder nameCondition = new StringBuilder();
            nameCondition.append("lower(project.name) like '%").append(filter.getName().toLowerCase()).append("%'");
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
    private String applyPendingActivitiesFilterNew(final ProjectResultFilter filter,final Long userId, final boolean fetchUnViewed) {
        
        boolean flag=false;

        
        StringBuilder sql = new StringBuilder();
        
        
        // Keyword filter
        if(filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            StringBuilder keywordCondition = new StringBuilder();
            keywordCondition.append(" where (lower(project.projectName) like '%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("(''|| project.projectId ||'') like ").append("'%").append(synchroDAOUtil.getFormattedProjectCode(filter.getKeyword())).append("%'")
                    .append(" OR ").append("lower(project.projectmanager) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%'")
                    .append(" OR ").append("lower(project.activityType) like ").append("'%").append(filter.getKeyword().toLowerCase()).append("%'")
                     .append(" OR to_char(to_timestamp(project.startdate/1000),'DD/MM/YYYY') like '%").append(filter.getKeyword()).append("%'");
                  
            
            
            // Methodologies Keyword
            Map<Integer, String> allMethodologies = SynchroGlobal.getMethodologies();
            List<Integer> methodologies = new ArrayList<Integer>();
            if(allMethodologies!=null)
            {
         	   for(Integer methId : allMethodologies.keySet())
         	   {
         		   if(allMethodologies.get(methId).toLowerCase().contains(filter.getKeyword().toLowerCase()))
         		   {
         			   methodologies.add(methId);
         		   }
         	   }
         	   if(methodologies!=null && methodologies.size()>0)
         	   {
         		   keywordCondition.append(" OR (project.methodologydetails in ('").append(methodologies.get(0)+"").append("')");
		               if(methodologies.size()>0)
		               {
		            	   keywordCondition.append(" or project.methodologydetails like ('").append(methodologies.get(0)+"").append(",%')");
		            	   keywordCondition.append(" or project.methodologydetails like ('%,").append(methodologies.get(0)+"").append("')");
		            	   keywordCondition.append(" or project.methodologydetails like ('%,") .append(methodologies.get(0)+"").append(",%')");
			               	for(int i=1;i<methodologies.size();i++)
			               	{
			               		keywordCondition.append(" or project.methodologydetails in ('").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or project.methodologydetails like ('").append(methodologies.get(i)+"").append(",%')");
			               		keywordCondition.append(" or project.methodologydetails like ('%,").append(methodologies.get(i)+"").append("')");
			               		keywordCondition.append(" or project.methodologydetails like ('%,").append(methodologies.get(i)+"").append(",%')");
			               	}
		               }
		               keywordCondition.append(")");
         	   }
         	   
            }
            keywordCondition.append(")");
            sql.append(keywordCondition.toString());
            
           flag=true;
        }

        //Project Stage Filter
        if(filter.getProjectStages() != null && filter.getProjectStages().size()>0 && !isListNull(filter.getProjectStages())) {
            StringBuilder projectStagesCondition = new StringBuilder();
            if(flag)
            {
            	projectStagesCondition.append(" AND");
            }
            else
            {
            	projectStagesCondition.append(" WHERE");
            	flag=true;
            }
            projectStagesCondition.append(" project.status in (");
            projectStagesCondition.append(StringUtils.join(getProjectStatusFromStages(filter.getProjectStages()),","));
            projectStagesCondition.append(")");
            sql.append(projectStagesCondition.toString());
            
        }
        
        
        // Project Manager filter
        if(filter.getProjManager() != null && !filter.getProjManager().equals("")) {
            StringBuilder projectManagerCondition = new StringBuilder();
            if(flag)
            {
            	projectManagerCondition.append(" AND");
            }
            else
            {
            	projectManagerCondition.append(" WHERE");
            	flag=true;
            }
            projectManagerCondition.append(" project.projectmanager like '%"+filter.getProjManager()+"%'");
            
            sql.append(projectManagerCondition.toString());
        }
        
     // Project Initiator filter
        if(filter.getProjectInitiator() != null && !filter.getProjectInitiator().equals("")) {
            StringBuilder projectInitiatorCondition = new StringBuilder();
            if(flag)
            {
            	projectInitiatorCondition.append(" AND");
            }
            else
            {
            	projectInitiatorCondition.append(" WHERE");
            	flag=true;
            }
            projectInitiatorCondition.append(" project.briefcreator in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+filter.getProjectInitiator()+"%'");
            projectInitiatorCondition.append(")");
            sql.append(projectInitiatorCondition.toString());
        }
        
     // Start Date Filter
        
        if(filter.getStartDateBegin() != null && filter.getStartDateComplete()!=null) {
            StringBuilder startDateCondition = new StringBuilder();
            if(flag)
            {
            	startDateCondition.append(" AND");
            }
            else
            {
            	startDateCondition.append(" WHERE");
            	flag=true;
            }
            
            String startDateBegin = SynchroUtils.getDateString(filter.getStartDateBegin().getTime());
            String startDateComplete = SynchroUtils.getDateString(filter.getStartDateComplete().getTime());
            startDateCondition.append(" to_char(to_timestamp(project.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(project.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
            
            sql.append(startDateCondition.toString());
            
        }
        else if(filter.getStartDateBegin() != null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
     	  if(flag)
          {
          	startDateCondition.append(" AND");
          }
          else
          {
          	startDateCondition.append(" WHERE");
          	flag=true;
          } 
     	   String startDateBegin = SynchroUtils.getDateString(filter.getStartDateBegin().getTime());
            startDateCondition.append(" to_char(to_timestamp(project.startdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'");
            
            sql.append(startDateCondition.toString());
        }
        else if(filter.getStartDateComplete()!=null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
     	  if(flag)
          {
          	startDateCondition.append(" AND");
          }
          else
          {
          	startDateCondition.append(" WHERE");
          	flag=true;
          }  
     	   String startDateComplete = SynchroUtils.getDateString(filter.getStartDateComplete().getTime());
            startDateCondition.append(" to_char(to_timestamp(project.startdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
            
            sql.append(startDateCondition.toString());
        }
       
        // End Date Filter
        if(filter.getEndDateBegin() != null && filter.getEndDateComplete()!=null) {
            StringBuilder endDateCondition = new StringBuilder();
            if(flag)
            {
            	endDateCondition.append(" AND");
            }
            else
            {
            	endDateCondition.append(" WHERE");
            	flag=true;
            } 
            String endDateBegin = SynchroUtils.getDateString(filter.getEndDateBegin().getTime());
            String endDateComplete = SynchroUtils.getDateString(filter.getEndDateComplete().getTime());
            endDateCondition.append(" to_char(to_timestamp(project.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(project.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
            sql.append(endDateCondition.toString());
            
        }
        else if (filter.getEndDateBegin() != null)
        {
     	    StringBuilder endDateCondition = new StringBuilder();
     	   if(flag)
           {
           	endDateCondition.append(" AND");
           }
           else
           {
           	endDateCondition.append(" WHERE");
           	flag=true;
           } 
            String endDateBegin = SynchroUtils.getDateString(filter.getEndDateBegin().getTime());
            endDateCondition.append(" to_char(to_timestamp(project.enddate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'");
            sql.append(endDateCondition.toString());
        }
        else if (filter.getEndDateComplete()!=null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateComplete = SynchroUtils.getDateString(filter.getEndDateComplete().getTime());
            if(flag)
            {
            	endDateCondition.append(" AND");
            }
            else
            {
            	endDateCondition.append(" WHERE");
            	flag=true;
            } 
            endDateCondition.append(" to_char(to_timestamp(project.enddate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
            sql.append(endDateCondition.toString());
        }
        
       // Creation Date Filter
        
        if(filter.getCreationDateBegin() != null && filter.getCreationDateComplete()!=null) 
        {
            StringBuilder creationDateCondition = new StringBuilder();
            if(flag)
            {
            	creationDateCondition.append(" AND");
            }
            else
            {
            	creationDateCondition.append(" WHERE");
            	flag=true;
            }
            
            String creationDateBegin = SynchroUtils.getDateString(filter.getCreationDateBegin().getTime());
            String creationDateComplete = SynchroUtils.getDateString(filter.getCreationDateComplete().getTime());
            creationDateCondition.append(" to_char(to_timestamp(project.creationdate/1000),'YYYY-MM-DD') >= '"+creationDateBegin+"'  AND to_char(to_timestamp(project.creationdate/1000),'YYYY-MM-DD') <= '"+creationDateComplete+"'");
            
            sql.append(creationDateCondition.toString());
            
        }
        else if(filter.getCreationDateBegin() != null)
        {
     	   StringBuilder creationDateCondition = new StringBuilder();
     	  if(flag)
          {
     		 creationDateCondition.append(" AND");
          }
          else
          {
        	  creationDateCondition.append(" WHERE");
          	  flag=true;
          } 
     	   
     	  String creationDateBegin = SynchroUtils.getDateString(filter.getCreationDateBegin().getTime());
     	  creationDateCondition.append(" to_char(to_timestamp(project.creationdate/1000),'YYYY-MM-DD') >= '"+creationDateBegin+"'");
            
          sql.append(creationDateCondition.toString());
        }
        else if(filter.getCreationDateComplete()!=null)
        {
     	   StringBuilder creationDateCondition = new StringBuilder();
     	  if(flag)
          {
     		 creationDateCondition.append(" AND");
          }
          else
          {
        	  creationDateCondition.append(" WHERE");
          	  flag=true;
          }  
     	  
     	  String creationDateComplete = SynchroUtils.getDateString(filter.getCreationDateComplete().getTime());
     	  creationDateCondition.append(" to_char(to_timestamp(project.creationdate/1000),'YYYY-MM-DD') <= '"+creationDateComplete+"'");
            
          sql.append(creationDateCondition.toString());
        }
        
        //Category Types Filter
        if(filter.getCategoryTypes() != null && filter.getCategoryTypes().size()>0 && !isListNull(filter.getCategoryTypes())) {
            StringBuilder categoryTypesCondition = new StringBuilder();
            if(flag)
            {
            	categoryTypesCondition.append(" AND");
            }
            else
            {
            	categoryTypesCondition.append(" WHERE");
            	flag=true;
            } 
            
            

          
            categoryTypesCondition.append(" (project.categorytype in ('")
                    .append(filter.getCategoryTypes().get(0)+"").append("')");
            if(filter.getCategoryTypes().size()>0)
            {
            	categoryTypesCondition.append(" or project.categorytype like ('").append(filter.getCategoryTypes().get(0)+"").append(",%')");
            	categoryTypesCondition.append(" or project.categorytype like ('%,").append(filter.getCategoryTypes().get(0)+"").append("')");
            	categoryTypesCondition.append(" or project.categorytype like ('%,") .append(filter.getCategoryTypes().get(0)+"").append(",%')");
               	for(int i=1;i<filter.getCategoryTypes().size();i++)
               	{
               		categoryTypesCondition.append(" or project.categorytype in ('").append(filter.getCategoryTypes().get(i)+"").append("')");
               		categoryTypesCondition.append(" or project.categorytype like ('").append(filter.getCategoryTypes().get(i)+"").append(",%')");
               		categoryTypesCondition.append(" or project.categorytype like ('%,").append(filter.getCategoryTypes().get(i)+"").append("')");
               		categoryTypesCondition.append(" or project.categorytype like ('%,").append(filter.getCategoryTypes().get(i)+"").append(",%')");
               	}
            }
            categoryTypesCondition.append(")");
            LOG.info("Category Types Filter Query - " + categoryTypesCondition.toString());
            sql.append(categoryTypesCondition.toString());
        
            
            
        } 
        //Methodology Details Filter
        if(filter.getMethDetails() != null && filter.getMethDetails().size()>0 && !isListNull(filter.getMethDetails())) {
            StringBuilder methodologyDetailsCondition = new StringBuilder();
            if(flag)
            {
            	methodologyDetailsCondition.append(" AND");
            }
            else
            {
            	methodologyDetailsCondition.append(" WHERE");
            	flag=true;
            }
            
            methodologyDetailsCondition.append(" (project.methodologydetails in ('")
                    .append(filter.getMethDetails().get(0)+"").append("')");
            if(filter.getMethDetails().size()>0)
            {
         	   methodologyDetailsCondition.append(" or project.methodologydetails like ('").append(filter.getMethDetails().get(0)+"").append(",%')");
         	   methodologyDetailsCondition.append(" or project.methodologydetails like ('%,").append(filter.getMethDetails().get(0)+"").append("')");
         	   methodologyDetailsCondition.append(" or project.methodologydetails like ('%,") .append(filter.getMethDetails().get(0)+"").append(",%')");
	               	for(int i=1;i<filter.getMethDetails().size();i++)
	               	{
	               		methodologyDetailsCondition.append(" or project.methodologydetails in ('").append(filter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or project.methodologydetails like ('").append(filter.getMethDetails().get(i)+"").append(",%')");
	               		methodologyDetailsCondition.append(" or project.methodologydetails like ('%,").append(filter.getMethDetails().get(i)+"").append("')");
	               		methodologyDetailsCondition.append(" or project.methodologydetails like ('%,").append(filter.getMethDetails().get(i)+"").append(",%')");
	               	}
            }
            methodologyDetailsCondition.append(")");
            LOG.info("Methodology Details Filter Query - " + methodologyDetailsCondition.toString());
            sql.append(methodologyDetailsCondition.toString());
            
        }
      //Methodology Type Filter
        if(filter.getMethodologyTypes() != null && filter.getMethodologyTypes().size()>0 && !isListNull(filter.getMethodologyTypes())) {
            StringBuilder methodologyTypesCondition = new StringBuilder();
            if(flag)
            {
            	methodologyTypesCondition.append(" AND");
            }
            else
            {
            	methodologyTypesCondition.append(" WHERE");
            	flag=true;
            }
            methodologyTypesCondition.append(" project.methodologytype in (");
            methodologyTypesCondition.append(StringUtils.join(filter.getMethodologyTypes(),","));
            methodologyTypesCondition.append(")");
            sql.append(methodologyTypesCondition.toString());
            
        }
      
        //Budget Location Filter
        if(filter.getBudgetLocations() != null && filter.getBudgetLocations().size()>0 && !isListNull(filter.getBudgetLocations())) {
            StringBuilder budgetLocationsCondition = new StringBuilder();
            if(flag)
            {
            	budgetLocationsCondition.append(" AND");
            }
            else
            {
            	budgetLocationsCondition.append(" WHERE");
            	flag=true;
            }
            budgetLocationsCondition.append(" project.budgetlocation in (");
            budgetLocationsCondition.append(StringUtils.join(filter.getBudgetLocations(),","));
            budgetLocationsCondition.append(")");
            sql.append(budgetLocationsCondition.toString());
            
        }
      
        
      //Research End Market Filter
        if(filter.getResearchEndMarkets() != null && filter.getResearchEndMarkets().size()>0 && !isListNull(filter.getResearchEndMarkets()))
        {
            StringBuilder researchEndMarketsCondition = new StringBuilder();
            if(flag)
            {
            	researchEndMarketsCondition.append(" AND");
            }
            else
            {
            	researchEndMarketsCondition.append(" WHERE");
            	flag=true;
            }
            researchEndMarketsCondition.append(" project.projectid in ( select endmarket.projectid from grailendmarketinvestment endmarket where endmarket.endmarketid in (");
            researchEndMarketsCondition.append(StringUtils.join(filter.getResearchEndMarkets(),","));
            researchEndMarketsCondition.append("))");
            sql.append(researchEndMarketsCondition.toString());
            
        }
       
       // Action Pending Filter
        
        if(filter.getActionPendings() != null && filter.getActionPendings().size()>0 && !isListNull(filter.getActionPendings()))
        {
            StringBuilder actionPendingsCondition = new StringBuilder();
            if(flag)
            {
            	actionPendingsCondition.append(" AND");
            }
            else
            {
            	actionPendingsCondition.append(" WHERE");
            	flag=true;
            }
            if(filter.getActionPendings().size()==2)
            {
            	actionPendingsCondition.append(" (project.activityType like '%Brief Approval%' OR project.activityType like '%Proposal Approval%')");
            }
            if(filter.getActionPendings().size()==1)
            {
            	if(filter.getActionPendings().get(0)==1)
            	{
            		actionPendingsCondition.append(" (project.activityType like '%Brief Approval%')");
            	}
            	else
            	{
            		actionPendingsCondition.append(" (project.activityType like '%Proposal Approval%')");
            	}
            	
            }
        
            sql.append(actionPendingsCondition.toString());
            
        }
        
        return sql.toString();
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
            } else if(filter.getSortField().equals("projectType")) {
                sortField = "projectType";
            } else if(filter.getSortField().equals("startDate")) {
                sortField = "startDate";
            } else if(filter.getSortField().equals("projectManager")) {
                sortField = "projectManager";
            } 
            else if(filter.getSortField().equals("pendingAction")) {
                sortField = "activityType";
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
           // bean.setOwner(rs.getString("projectOwner"));
            bean.setStartYear("2006");
           // bean.setBrand(rs.getString("brandName"));
            
           // Integer activityTypeId = rs.getInt("activityTypeId");
            //bean.setPendingActivity(SynchroGlobal.PendingActivityStatus.getById(activityTypeId));
            bean.setPendingActivity(rs.getString("activityType"));
          //  Boolean isMultimarket = rs.getBoolean("multimarket");
           // String activityLink = ProjectStage.generateURL(bean.getProjectID(), ProjectStage.getCurrentStageNumber(bean.getProjectID()));
            if(rs.getString("activityType")!=null && rs.getString("activityType").equalsIgnoreCase("Brief Review"))
            {
            	bean.setActivityLink("/new-synchro/pib-details!input.jspa?projectID="+ bean.getProjectID());
            }
            else
            {
            	bean.setActivityLink("/new-synchro/proposal-details!input.jspa?projectID="+ bean.getProjectID());
            }
            bean.setMethodology(SynchroDAOUtil.getMethodologyNames(rs.getString("methodologydetails")));
            bean.setProjectManager(rs.getString("projectmanager"));
           // bean.setProjectType(rs.getString("projecttype"));
            if(rs.getInt("projecttype")==1)
            {
            	bean.setProjectType("Global");
            }
            else if(rs.getInt("projecttype")==2)
            {
            	bean.setProjectType("Regional");
            }
            else
            {
            	bean.setProjectType("EndMarket");
            }
            if(rs.getLong("startDate")>0)
            {
            	bean.setStartDate(new Date(rs.getLong("startDate")));
            }
           
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

    /**
     * Reusable row mapper for mapping a result set to ProjectCostDetailsBean
     */
    private final RowMapper<ProjectCostDetailsBean> projectCostDetailRowMapper = new RowMapper<ProjectCostDetailsBean>() {
        public ProjectCostDetailsBean mapRow(ResultSet rs, int row) throws SQLException {
        	ProjectCostDetailsBean initiationBean = new ProjectCostDetailsBean();
            initiationBean.setProjectId(rs.getLong("projectid"));
            
            initiationBean.setAgencyId(rs.getLong("agencyid"));
            initiationBean.setCostComponent(rs.getInt("costcomponent"));
            
            initiationBean.setCostCurrency(rs.getInt("costcurrency"));
            initiationBean.setEstimatedCost(rs.getBigDecimal("estimatedcost"));
            
            return initiationBean;
        }
    };
    private static UserManager userManager;
    public static UserManager getUserManager() {
        if(userManager == null) {
            return JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;

    }
    
    @Override
    public List<String> getSynchroUserNames() {
        
    	String sql = "select (u.firstname || ' ' || u.lastname) as name FROM jiveUser u";
    	List<String> synchroUsers = Collections.emptyList();
        try {
        	synchroUsers = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql,
                    String.class);
        }
        catch (DataAccessException e) {
            final String message = "Failed to load Synchro User Names";
            LOG.error(message, e);
            throw new DAOException(message, e);
        }
        return synchroUsers;
    }
    
    public List<Long> getBudgetLocationsForProject(final Integer budgetYear){
        List<Long> budgetLocations = new ArrayList<Long>();
        	
        	
    	// This is done as part of http://redmine.nvish.com/redmine/issues/515. 
    	//Only the budget location the user has access can have spend by result. Rest budget locations should not even appear in the spend by report.
    	// Access Mechanism for Spend Reports Filter
    	if(SynchroPermHelper.isGlobalUserType())
    	{
    		
    	}
    	// This will fetch all the End Market projects for the particular regions for the which the user the associated with
    	else if(SynchroPermHelper.isRegionalUserType())
    	{
    		
    		List<Long> regionBudgetLocations = SynchroUtils.getBudgetLocationRegions(SynchroPermHelper.getEffectiveUser());
    		List<Long> regionEndMarketLocations = SynchroUtils.getBudgetLocationRegionsEndMarkets(SynchroPermHelper.getEffectiveUser());
    		regionBudgetLocations.addAll(regionEndMarketLocations);
    		
    		// This is in case the Regional User is also and End Market user, then we need to fetch the projects correspond to that End Market As well.
    		if(SynchroPermHelper.isEndMarketUserType())
    		{
    			List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    			regionBudgetLocations.addAll(emBudgetLocations);
    		}
    		
    		budgetLocations.addAll(regionBudgetLocations);
    		
    		
    	}
    	// This will fetch all the End Market projects for the particular end markets for the which the user the associated with
    	// It should be fetched on the basis of Budget Location and not End Market. //http://redmine.nvish.com/redmine/issues/338
    	else if(SynchroPermHelper.isEndMarketUserType())
    	{

    		List<Long> emBudgetLocations = SynchroUtils.getBudgetLocationEndMarkets(SynchroPermHelper.getEffectiveUser());
    		budgetLocations.addAll(emBudgetLocations);
    		
    	} 
        	
        	
      
        
        return budgetLocations;
    }
    
}
