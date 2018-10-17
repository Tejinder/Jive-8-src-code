package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverApprover;
import com.grail.synchro.beans.ProjectWaiverEndMarket;
import com.grail.synchro.dao.ProcessWaiverDAONew;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.StringUtils;

/**
 * @author Tejinder
 * @version 1.0
 */
public class ProcessWaiverDAOImplNew extends JiveJdbcDaoSupport implements ProcessWaiverDAONew {

    private static Logger LOG = Logger.getLogger(ProcessWaiverDAOImplNew.class);

    private SynchroDAOUtil synchroDAOUtil;

    /** PROJECT WAIVER SCRIPTS **/
    private static final String PROJECT_WAIVER_FIELDS =  "waiverID, name, summary, brand, methodology, preApprovals, preApprovalComment, nexus, status," +
            " creationby, modificationby, creationdate, modificationdate, iskantar ";

    private static final String GET_ALL_PROJECT_WAIVERS = "SELECT " + getSelectColumnsWithAlias(PROJECT_WAIVER_FIELDS, "gw") +
            " FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid) " +
            "where (gw.creationby=? OR ga.approverid=?) order by gw.creationdate DESC";

    private static final String GET_PROJECT_WAIVER_BY_ID = "SELECT " + PROJECT_WAIVER_FIELDS + " FROM grailwaiver where waiverID=? order by creationdate DESC";

    private static final String GET_PROJECT_WAIVER_BY_NAME = "SELECT " + PROJECT_WAIVER_FIELDS + " FROM grailwaiver where name=? order by creationdate DESC";

    private static final String INSERT_PROJECT_WAIVER = "INSERT INTO grailwaiver( " + PROJECT_WAIVER_FIELDS + ")" +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

    private static final String UPDATE_PROJECT_WAIVER =  "UPDATE grailwaiver" +
            " SET name=?, summary=?, brand=?, methodology=?, preApprovals=?, preApprovalComment=?, nexus=?, status=?," +
            " creationby=?, modificationby=?, creationdate=?, modificationdate=?" +
            " WHERE waiverID=?";

    private static final String GET_TOTAL_COUNT = "SELECT count(*) " +
            " FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid) " +
            "where (gw.creationby=? OR ga.approverid=?)";

    private static final String GET_PENDING_ACTIVITY_TOTAL_COUNT = "SELECT count(*) " +
            " FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid) " +
            "where (ga.approverid=? AND gw.status="+ SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value()+")";

    private static final String GET_PROJECT_WAIVERS_PENDING_APPROVAL_LIST = "SELECT " + getSelectColumnsWithAlias(PROJECT_WAIVER_FIELDS, "gw") +
            " FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid) " +
            "where (ga.approverid=? AND gw.status="+ SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value()+") order by gw.waiverid";


    /** PROJECT WAIVER END MARKETS SCRIPTS **/
    private static final String PROJECT_WAIVER_END_MARKET_FIELDS =  "waiverID, endmarketID," +
            " creationby, modificationby, creationdate, modificationdate ";

    private static final String GET_PROJECT_WAIVER_END_MARKETS = "SELECT " + PROJECT_WAIVER_END_MARKET_FIELDS +
            " FROM grailwaiverendmarket WHERE waiverid=?";

    private static final String GET_PROJECT_WAIVER_END_MARKETS_IDS = "SELECT endmarketID" +
            " FROM grailwaiverendmarket WHERE waiverid=?";

    private static final String INSERT_PROJECT_WAIVER_END_MARKETS = "INSERT INTO " +
            "grailwaiverendmarket( " + PROJECT_WAIVER_END_MARKET_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?);";

    private static final String UPDATE_PROJECT_WAIVER_END_MARKETS = "UPDATE grailwaiverendmarket" +
            " SET waiverid=?, endmarketID=?," +
            " creationby=?, modificationby=?, creationdate=?, modificationdate=?" +
            " WHERE waiverid=?";

    private static final String DELETE_PROJECT_WAIVER_END_MARKETS = "DELETE FROM grailwaiverendmarket WHERE waiverid=?";

    /** PROJECT WAIVER APPROVERS SCRIPTS **/
    private static final String PROJECT_WAIVER_APPROVERS_FIELDS =  "waiverid, approverid, approverComments, isapproved," +
            " creationby, modificationby, creationdate, modificationdate";

    private static final String GET_PROJECT_WAIVER_APPROVER_ID = "SELECT approverid"
            +" FROM grailwaiverapprovers WHERE waiverid=?";

    private static final String GET_PROJECT_WAIVER_APPROVER = "SELECT " + PROJECT_WAIVER_APPROVERS_FIELDS
            +" FROM grailwaiverapprovers WHERE waiverid=?";

    private static final String INSERT_PROJECT_WAIVER_APPROVERS = "INSERT INTO " +
            "grailwaiverapprovers( " + PROJECT_WAIVER_APPROVERS_FIELDS + ")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String DELETE_PROJECT_WAIVER_APPROVERS = "DELETE FROM grailwaiverapprovers WHERE waiverid=?;";

    private static final String GET_ATTACHMENT_BY_OBJECT = "SELECT * from jiveattachment where objecttype=? AND objectid=?";

    private static final String INSERT_ATTACHMENT_USER = "INSERT INTO grailattachmentuser(attachmentid, userid) VALUES (?, ?)";

    private static final String UPDATE_ATTACHMENT_OBJECT_ID = "UPDATE jiveattachment " +
            "   SET objectid=? WHERE attachmentid = ? ";

    private static final String DELETE_ATTACHMENT_USER = "DELETE from grailattachmentuser where attachmentid=? ";

    private static final String GET_ATTACHMENT_USER = "Select userid from grailattachmentuser where attachmentid=? ";

    private static final String COUNT_WAIVER_ENDMARKETS = "SELECT COUNT(*) FROM grailWaiverEndMarket where waiverID=?;";

    private static final String COUNT_WAIVER_APPROVERS = "SELECT COUNT(*) FROM grailWaiverApprovers where waiverID=?;";


    @Override
    @Transactional
    public ProjectWaiver create(final ProjectWaiver projectWaiver) {
        try {
            Long id = -1L;
            if(projectWaiver.getWaiverID()!=null && projectWaiver.getWaiverID()>0)
                id = projectWaiver.getWaiverID();
            else
                id = synchroDAOUtil.generateWaiverID();

            projectWaiver.setWaiverID(id);

            getSimpleJdbcTemplate().getJdbcOperations().update(INSERT_PROJECT_WAIVER,
                    id,
                    projectWaiver.getName(),
                    projectWaiver.getSummary(),
                    projectWaiver.getBrand(),
                    projectWaiver.getMethodology(),
                    projectWaiver.getPreApprovals()!=null?Joiner.on(",").join(projectWaiver.getPreApprovals()):null,
                    projectWaiver.getPreApprovalComment(),
                    projectWaiver.getNexus(),
                    projectWaiver.getStatus(),
                    projectWaiver.getCreationBy(),
                    projectWaiver.getModifiedBy(),
                    projectWaiver.getCreationDate(),
                    projectWaiver.getModifiedDate(),
                    (projectWaiver.getIsKantar() != null && projectWaiver.getIsKantar())?1:0
            );
            projectWaiver.setWaiverID(id);
            return projectWaiver;
        } catch (DataAccessException e) {
            final String message = "Failed to create new waiver - "+projectWaiver.getName();
            LOG.error(message, e);
        }
        return null;
    }

    @Override
    @Transactional
    public ProjectWaiver update(final ProjectWaiver projectWaiver) {
        try {
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_PROJECT_WAIVER,
                    projectWaiver.getName(),
                    projectWaiver.getSummary(),
                    projectWaiver.getBrand(),
                    projectWaiver.getMethodology(),
                    projectWaiver.getPreApprovals()!=null?Joiner.on(",").join(projectWaiver.getPreApprovals()):null,
                    projectWaiver.getPreApprovalComment(),
                    projectWaiver.getNexus(),
                    projectWaiver.getStatus(),
                    projectWaiver.getCreationBy(),
                    projectWaiver.getModifiedBy(),
                    projectWaiver.getCreationDate(),
                    projectWaiver.getModifiedDate(),
                    projectWaiver.getWaiverID()
            );

            return projectWaiver;
        } catch (DataAccessException e) {
            final String message = "Failed to update Waiver Details for Waiver ID - " + projectWaiver.getWaiverID();
            LOG.error(message, e);
        }
        return null;
    }

    @Override
    @Transactional
    public List<ProjectWaiverEndMarket> getWaiverEndMarkets(final Long waiverID) {
        List<ProjectWaiverEndMarket> projectWaiverEndMarket = null;
        try {
            projectWaiverEndMarket = getSimpleJdbcTemplate().getJdbcOperations().query(GET_PROJECT_WAIVER_END_MARKETS, projectWaiverEndMarketRowMapper, waiverID);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaiverEndMarket;
    }

    @Override
    @Transactional
    public List<Long> getWaiverEndMarketsIDs(Long waiverID) {
        List<Long> endMarkets = null;
        try {
            Long count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(COUNT_WAIVER_ENDMARKETS, waiverID);
            if(count>0)
                endMarkets = getSimpleJdbcTemplate().getJdbcOperations().queryForList(GET_PROJECT_WAIVER_END_MARKETS_IDS,
                        Long.class, waiverID);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return endMarkets;
    }

    @Override
    public List<ProjectWaiverEndMarket> getWaiverEndMarkets(final ProjectWaiver projectWaiver) {
        return getWaiverEndMarkets(projectWaiver.getWaiverID());
    }

    @Override
    @Transactional
    public void saveWaiverEndMarkets(final Long waiverID, final List<Long> endMarkets) {
        ProjectWaiver projectWaiver = get(waiverID);
        projectWaiver.setEndMarkets(endMarkets);
        projectWaiver.setModifiedBy(getUser().getID());
        projectWaiver.setModifiedDate(new Date().getTime());
        saveWaiverEndMarkets(projectWaiver);
    }

    @Override
    @Transactional
    public void saveWaiverEndMarkets(final ProjectWaiver projectWaiver) {
        try {
            // Delete all pre-existing end markets associated to current project waiver
            boolean deleteSuccess = deleteWaiverEndMarkets(projectWaiver.getWaiverID());
            if(deleteSuccess) {
                if(projectWaiver.getEndMarkets() != null && !projectWaiver.getEndMarkets().isEmpty()) {
                    for(Long endMarket: projectWaiver.getEndMarkets()) {
                        getSimpleJdbcTemplate().getJdbcOperations().update(
                                INSERT_PROJECT_WAIVER_END_MARKETS,
                                projectWaiver.getWaiverID(),
                                endMarket,
                                projectWaiver.getCreationBy(),
                                projectWaiver.getModifiedBy(),
                                projectWaiver.getCreationDate(),
                                projectWaiver.getModifiedDate()
                        );

                    }
                }
            }
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteWaiverEndMarkets(final ProjectWaiver projectWaiver) {
        return deleteWaiverEndMarkets(projectWaiver.getWaiverID());
    }


    @Override
    @Transactional
    public Long getWaiverApproverID(final Long waiverID) {
        Long approverID = 0L;
        try {
            approverID = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_PROJECT_WAIVER_APPROVER_ID,
                    waiverID);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return approverID;
    }

    @Override
    public Long getWaiverApprover(final ProjectWaiver projectWaiver) {
        return getWaiverApproverID(projectWaiver.getWaiverID());
    }

    @Override
    @Transactional
    public void saveWaiverApprover(final Long waiverID, final Long approverID) {
        ProjectWaiver projectWaiver = get(waiverID);
        projectWaiver.setApproverID(approverID);
        projectWaiver.setModifiedBy(approverID);
        projectWaiver.setModifiedDate(new Date().getTime());
        saveWaiverApprover(projectWaiver);
    }

    @Override
    @Transactional
    public void saveWaiverApprover(final ProjectWaiver projectWaiver) {
        try {
            // Delete all pre-existing end markets associated to current project waiver
            boolean deleteSuccess = deleteWaiverApprover(projectWaiver.getWaiverID());
            if(deleteSuccess) {
                getSimpleJdbcTemplate().getJdbcOperations().update(
                        INSERT_PROJECT_WAIVER_APPROVERS,
                        projectWaiver.getWaiverID(),
                        projectWaiver.getApproverID(),
                        projectWaiver.getApproverComments()==null?"":projectWaiver.getApproverComments(),
                        0,
                        projectWaiver.getCreationBy(),
                        projectWaiver.getModifiedBy(),
                        projectWaiver.getCreationDate(),
                        projectWaiver.getModifiedDate()
                );
            }
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateWaiverApprover(final ProjectWaiver projectWaiver) {
        try {
            // Delete all pre-existing end markets associated to current project waiver
            boolean deleteSuccess = deleteWaiverApprover(projectWaiver.getWaiverID());
            if(deleteSuccess) {
                getSimpleJdbcTemplate().getJdbcOperations().update(
                        INSERT_PROJECT_WAIVER_APPROVERS,
                        projectWaiver.getWaiverID(),
                        projectWaiver.getApproverID(),
                        projectWaiver.getApproverComments()==null?"":projectWaiver.getApproverComments(),
                        1,
                        projectWaiver.getCreationBy(),
                        projectWaiver.getModifiedBy(),
                        projectWaiver.getCreationDate(),
                        projectWaiver.getModifiedDate()

                );
            }
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteWaiverApprover(final ProjectWaiver projectWaiver) {
        return deleteWaiverApprover(projectWaiver.getWaiverID());
    }


    public boolean deleteWaiverEndMarkets(final Long waiverID) {
        boolean deleteSuccess = false;
        try {
            Long count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(COUNT_WAIVER_ENDMARKETS, waiverID);
            if(count>0)
                getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_PROJECT_WAIVER_END_MARKETS, waiverID);
            deleteSuccess = true;
        } catch (DataAccessException e) {
            deleteSuccess = false;
            LOG.info(e.getMessage(), e);
        }
        return deleteSuccess;
    }

    @Override
    public boolean deleteWaiverApprover(final Long waiverID) {
        boolean deleteSuccess = false;
        try {
            Long count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(COUNT_WAIVER_APPROVERS, waiverID);
            if(count>0)
                getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_PROJECT_WAIVER_APPROVERS, waiverID);
            deleteSuccess = true;
        } catch (DataAccessException e) {
            deleteSuccess = false;
            LOG.info(e.getMessage(), e);
        }
        return deleteSuccess;
    }
    @Override
    @Transactional
    public boolean approve(final Long waiverID, final Long approverID) {
        return false;
    }

    @Override
    @Transactional
    public boolean approve(final ProjectWaiver projectWaiver) {
        return false;
    }

    @Override
    @Transactional
    public boolean reject(final Long waiverID, final Long approverID) {
        return false;
    }

    @Override
    @Transactional
    public boolean reject(final ProjectWaiver projectWaiver) {
        return false;
    }

    @Override
    @Transactional
    public ProjectWaiver get(final Long id) {
        return get(id, getUser().getID());
    }

    @Override
    @Transactional
    public ProjectWaiver get(final Long id, final Long userID) {
        ProjectWaiver projectWaiver = null;
        try {
            projectWaiver = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_PROJECT_WAIVER_BY_ID,
                    projectWaiverRowMapper, id);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaiver;
    }

    @Override
    @Transactional
    public ProjectWaiver get(final String name) {
        ProjectWaiver projectWaiver = null;
        try {
            projectWaiver = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_PROJECT_WAIVER_BY_NAME,
                    projectWaiverRowMapper, name);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaiver;
    }

    @Override
    public List<ProjectWaiver> getAll() {
        return getAll(getUser().getID());
    }

    @Override
    public List<ProjectWaiver> getAll(final Integer start, final Integer limit) {
        return getAll(getUser().getID(), start, limit);
    }

    @Override
    public List<ProjectWaiver> getAll(final Long userID) {
        return getAll(userID, null, null);
    }

    @Override
    @Transactional
    public List<ProjectWaiver> getAll(final Long userID, final Integer start, final Integer limit) {
        List<ProjectWaiver> projectWaivers = Collections.emptyList();
        StringBuilder sql = new StringBuilder(GET_ALL_PROJECT_WAIVERS);
        try {
            if(start != null) {
                sql.append(" OFFSET ").append(start);
            }
            if(limit != null && limit > 0) {
                sql.append(" LIMIT ").append(limit);
            }
            projectWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(),
                    projectWaiverRowMapper, userID, userID);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaivers;
    }


    @Override
    @Transactional
    public List<ProjectWaiver> getAllByResultFilter(final ProjectResultFilter projectResultFilter) {
        boolean flag = true;
        List<ProjectWaiver> projectWaivers = Collections.emptyList();

        String GET_ALL_PROJECT_WAIVERS_FILTER = "SELECT " + getSelectColumnsWithAlias(PROJECT_WAIVER_FIELDS, "gw") +
                " FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid and gw.iskantar=0)";

        //waiver name keyword
        String name = projectResultFilter.getName();
        if(!StringUtils.isNullOrEmpty(name))
        {
            GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where LOWER(name) like '%" + name.toLowerCase() +"%'";
            flag = false;
        }

      //Waiver Initiator
        String waiverInitiator =  projectResultFilter.getWaiverInitiator();
        if(!StringUtils.isNullOrEmpty(waiverInitiator))
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.creationby in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+waiverInitiator+"%')";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.creationby in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+waiverInitiator+"%')";
            }
        }

 // Start Date Filter
        
        if(projectResultFilter.getStartDateBegin() != null && projectResultFilter.getStartDateComplete()!=null) {
            StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
            
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
            }
            
            
            
        }
        else if(projectResultFilter.getStartDateBegin() != null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
           
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'";
            }
        }
        else if(projectResultFilter.getStartDateComplete()!=null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
           
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
            }
        }
        
     // End Date (Date of Approval) Filter 
        if(projectResultFilter.getEndDateBegin() != null && projectResultFilter.getEndDateComplete()!=null) {
            StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
            }
            
        }
        else if (projectResultFilter.getEndDateBegin() != null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
           
            
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'";
            }
        }
        else if (projectResultFilter.getEndDateComplete()!=null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
                       
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
            }
        }
        
        // Waiver Status
        List<Long> waiverStatus= projectResultFilter.getWaiverStatus();
        if(waiverStatus!=null && waiverStatus.size()>0)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.status in ("+StringUtils.join(waiverStatus, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.status in ("+StringUtils.join(waiverStatus, ',')+")";
            }
        }
        
        //Project Owner
       /* List<Long> owner= projectResultFilter.getOwnerfield();
        if(owner!=null && owner.size()>0)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.creationby in ("+StringUtils.join(owner, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.creationby in ("+StringUtils.join(owner, ',')+")";
            }
        }
*/
        //Waiver Brand Fields
        List<Long> brandFields = projectResultFilter.getBrands();
        if(brandFields!=null && brandFields.size()>0)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.brand in ("+StringUtils.join(brandFields, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.brand in ("+StringUtils.join(brandFields, ',')+")";
            }
        }
      //Research End Market Filter
        if(projectResultFilter.getResearchEndMarkets() != null && projectResultFilter.getResearchEndMarkets().size()>0 && !isListNull(projectResultFilter.getResearchEndMarkets())) {
            StringBuilder researchEndMarketsCondition = new StringBuilder();
            
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.waiverid in ( select waiverid from grailwaiverendmarket where endmarketid in ("+StringUtils.join(projectResultFilter.getResearchEndMarkets(), ',')+"))";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.waiverid in ( select waiverid from grailwaiverendmarket where endmarketid in ("+StringUtils.join(projectResultFilter.getResearchEndMarkets(), ',')+"))";
            }
            
        }
        


        // Fetch Region specific filters
        /*List<Long> regions = projectResultFilter.getRegionFields();
        List<Long> endMarkets = projectResultFilter.getEndMarkets();

        List<Long> filterEndMarketIDs = new ArrayList<Long>();
        //List<Long> regions = researchCycleReportFilters.getRegionFields();
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
        
        List<Long> e_waiverIDs = Collections.emptyList();
        boolean includeMarketClause = false;
        if(filterEndMarketIDs != null && filterEndMarketIDs.size() > 0)
        {
            //Filter out the unique values
            Set<Long> set  = new HashSet<Long>(filterEndMarketIDs);
            ArrayList<Long> filterEndMarketIDsUnique = new ArrayList<Long>();
            filterEndMarketIDsUnique.addAll(set);

            String ENDMARKET_DETAILS_SQL = "SELECT waiverid FROM grailwaiverendmarket where marketid in ("+StringUtils.join(filterEndMarketIDsUnique, ',')+")";

            try {
                e_waiverIDs = getSimpleJdbcTemplate().getJdbcOperations().queryForList(ENDMARKET_DETAILS_SQL,
                        Long.class);
                includeMarketClause = true;
            }
            catch (DataAccessException e) {
                final String message = "Failed to load end markets and regions mentioned in Advannced Filter ";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }

        ArrayList<Long> e_waiverIDsUnique = new ArrayList<Long>();

        if(e_waiverIDs!=null && e_waiverIDs.size() > 0 )
        {
            //Filter out the unique values
            Set<Long> set  = new HashSet<Long>(e_waiverIDs);
            e_waiverIDsUnique = new ArrayList<Long>();
            e_waiverIDsUnique.addAll(set);
        }

        //Fetch projects based on end markets/Region selected in filter
        if(includeMarketClause)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.waiverid in ("+StringUtils.join(e_waiverIDsUnique, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.waiverid in ("+StringUtils.join(e_waiverIDsUnique, ',')+")";
            }

        }
*/
        //Sorting
       // GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " order by gw.creationdate DESC";
        if(projectResultFilter != null) {
            String orderBy = getOrderByClause(projectResultFilter.getSortField(), projectResultFilter.getAscendingOrder());
            //sqlBuilder.append(orderBy);
            GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + orderBy;
            if(projectResultFilter.getStart() != null) {
                //sqlBuilder.append(" OFFSET ").append(projectResultFilter.getStart());
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " OFFSET " +  projectResultFilter.getStart();
            }
            if(projectResultFilter.getLimit() != null) {
               // sqlBuilder.append(" LIMIT ").append(projectResultFilter.getLimit());
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " LIMIT " +  projectResultFilter.getLimit();
            }
        } else {
        	GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " order by gw.creationdate DESC";
        }

        try {
            projectWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL_PROJECT_WAIVERS_FILTER, projectWaiverRowMapper);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaivers;
    }


    @Override
    @Transactional
    public List<ProjectWaiver> getAllByResultFilter(final User user, final ProjectResultFilter projectResultFilter) {
        boolean flag = true;
        List<ProjectWaiver> projectWaivers = Collections.emptyList();

        String GET_ALL_PROJECT_WAIVERS_FILTER = "SELECT " + getSelectColumnsWithAlias(PROJECT_WAIVER_FIELDS, "gw") +
                " FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid and gw.iskantar=0)";

        //http://redmine.nvish.com/redmine/issues/466
        if(SynchroPermHelper.isGlobalUserType())
        {
        	
        }
        else if(SynchroPermHelper.isRegionalUserType())
        {
        	List<Long> userRegionEndMarkets = SynchroUtils.getBudgetLocationRegionsEndMarkets(user);
        	List<Long> userEndMarkets = SynchroUtils.getBudgetLocationEndMarkets(user);
        	List<Long> allEndMarkets = new ArrayList<Long>();
        	allEndMarkets.addAll(userRegionEndMarkets);
        	allEndMarkets.addAll(userEndMarkets);
        	
	        if(allEndMarkets!=null)
	        {
	        	GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.waiverid in ( select waiverid from grailwaiverendmarket where endmarketid in ("+StringUtils.join(allEndMarkets, ',')+"))";
	        	flag=false;
	        }
        }
        else 
        {
	        List<Long> userEndMarkets = SynchroUtils.getBudgetLocationEndMarkets(user);
	        if(userEndMarkets!=null)
	        {
	        	GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.waiverid in ( select waiverid from grailwaiverendmarket where endmarketid in ("+StringUtils.join(userEndMarkets, ',')+"))";
	        	flag=false;
	        }
        }
        
        
        //waiver name keyword
        String name = projectResultFilter.getName();
        if(!StringUtils.isNullOrEmpty(name))
        {
            GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where LOWER(name) like '%" + name.toLowerCase() +"%'";
            flag = false;
        }

        
        
        
        
      //Waiver Initiator
        String waiverInitiator =  projectResultFilter.getWaiverInitiator();
        if(!StringUtils.isNullOrEmpty(waiverInitiator))
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.creationby in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+waiverInitiator+"%')";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.creationby in (select u.userid from jiveuser u where (u.firstname || ' ' || u.lastname) like '%"+waiverInitiator+"%')";
            }
        }
        
        // Start Date Filter
        
        if(projectResultFilter.getStartDateBegin() != null && projectResultFilter.getStartDateComplete()!=null) {
            StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
            
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
            }
            
            
            
        }
        else if(projectResultFilter.getStartDateBegin() != null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
           
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'";
            }
        }
        else if(projectResultFilter.getStartDateComplete()!=null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
           
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'";
            }
        }
        
     // End Date (Date of Approval) Filter 
        if(projectResultFilter.getEndDateBegin() != null && projectResultFilter.getEndDateComplete()!=null) {
            StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
            }
            
        }
        else if (projectResultFilter.getEndDateBegin() != null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
           
            
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'";
            }
        }
        else if (projectResultFilter.getEndDateComplete()!=null)
        {
     	   StringBuilder endDateCondition = new StringBuilder();
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
                       
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " WHERE to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'";
            }
        }
        
        // Waiver Status
        List<Long> waiverStatus= projectResultFilter.getWaiverStatus();
        if(waiverStatus!=null && waiverStatus.size()>0)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.status in ("+StringUtils.join(waiverStatus, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.status in ("+StringUtils.join(waiverStatus, ',')+")";
            }
        }
        
        //Project Owner
       /* List<Long> owner= projectResultFilter.getOwnerfield();
        if(owner!=null && owner.size()>0)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.creationby in ("+StringUtils.join(owner, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.creationby in ("+StringUtils.join(owner, ',')+")";
            }
        }
*/
        //Waiver Brand Fields
        List<Long> brandFields = projectResultFilter.getBrands();
        if(brandFields!=null && brandFields.size()>0)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.brand in ("+StringUtils.join(brandFields, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.brand in ("+StringUtils.join(brandFields, ',')+")";
            }
        }
      //Research End Market Filter
        if(projectResultFilter.getResearchEndMarkets() != null && projectResultFilter.getResearchEndMarkets().size()>0 && !isListNull(projectResultFilter.getResearchEndMarkets())) {
            StringBuilder researchEndMarketsCondition = new StringBuilder();
            
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.waiverid in ( select waiverid from grailwaiverendmarket where endmarketid in ("+StringUtils.join(projectResultFilter.getResearchEndMarkets(), ',')+"))";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.waiverid in ( select waiverid from grailwaiverendmarket where endmarketid in ("+StringUtils.join(projectResultFilter.getResearchEndMarkets(), ',')+"))";
            }
            
        }
        
        


        // Fetch Region specific filters
        /*List<Long> regions = projectResultFilter.getRegionFields();
        List<Long> endMarkets = projectResultFilter.getEndMarkets();

        List<Long> filterEndMarketIDs = new ArrayList<Long>();
        //List<Long> regions = researchCycleReportFilters.getRegionFields();
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
        
        List<Long> e_waiverIDs = Collections.emptyList();
        boolean includeMarketClause = false;
        if(filterEndMarketIDs != null && filterEndMarketIDs.size() > 0)
        {
            //Filter out the unique values
            Set<Long> set  = new HashSet<Long>(filterEndMarketIDs);
            ArrayList<Long> filterEndMarketIDsUnique = new ArrayList<Long>();
            filterEndMarketIDsUnique.addAll(set);

            String ENDMARKET_DETAILS_SQL = "SELECT waiverid FROM grailwaiverendmarket where marketid in ("+StringUtils.join(filterEndMarketIDsUnique, ',')+")";

            try {
                e_waiverIDs = getSimpleJdbcTemplate().getJdbcOperations().queryForList(ENDMARKET_DETAILS_SQL,
                        Long.class);
                includeMarketClause = true;
            }
            catch (DataAccessException e) {
                final String message = "Failed to load end markets and regions mentioned in Advannced Filter ";
                LOG.error(message, e);
                throw new DAOException(message, e);
            }
        }

        ArrayList<Long> e_waiverIDsUnique = new ArrayList<Long>();

        if(e_waiverIDs!=null && e_waiverIDs.size() > 0 )
        {
            //Filter out the unique values
            Set<Long> set  = new HashSet<Long>(e_waiverIDs);
            e_waiverIDsUnique = new ArrayList<Long>();
            e_waiverIDsUnique.addAll(set);
        }

        //Fetch projects based on end markets/Region selected in filter
        if(includeMarketClause)
        {
            if(flag)
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where gw.waiverid in ("+StringUtils.join(e_waiverIDsUnique, ',')+")";
                flag = false;
            }
            else
            {
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and gw.waiverid in ("+StringUtils.join(e_waiverIDsUnique, ',')+")";
            }
        }

        //Fetch projects based on end markets/Region selected in filter
        if(flag)
        {
            GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " where (gw.creationby=? OR ga.approverid=?)";
            flag = false;
        }
        else
        {
            GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " and (gw.creationby=? OR ga.approverid=?)";
        }
*/
        //Sorting
        
        //GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " order by gw.creationdate DESC";
        
        if(projectResultFilter != null) {
            String orderBy = getOrderByClause(projectResultFilter.getSortField(), projectResultFilter.getAscendingOrder());
            //sqlBuilder.append(orderBy);
            GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + orderBy;
            if(projectResultFilter.getStart() != null) {
               // sqlBuilder.append(" OFFSET ").append(projectResultFilter.getStart());
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " OFFSET " +  projectResultFilter.getStart();
            }
            if(projectResultFilter.getLimit() != null) {
                //sqlBuilder.append(" LIMIT ").append(projectResultFilter.getLimit());
                GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " LIMIT " +  projectResultFilter.getLimit();
            }
        } else {
        	GET_ALL_PROJECT_WAIVERS_FILTER = GET_ALL_PROJECT_WAIVERS_FILTER + " order by gw.creationdate DESC";
        }

        try {
            //projectWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL_PROJECT_WAIVERS_FILTER, projectWaiverRowMapper, user.getID(), user.getID());
        	projectWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(GET_ALL_PROJECT_WAIVERS_FILTER, projectWaiverRowMapper);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaivers;
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

    @Override
    public Long getTotalCount() {
        return getTotalCount(getUser().getID());
    }

    @Override
    @Transactional
    public Long getTotalCount(final Long userID) {
        Long totalCount = 0L;
        try {
            totalCount = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_TOTAL_COUNT, userID, userID);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return totalCount;
    }

    @Override
    public Long getPendingActivityTotalCount(final User user, final ProjectResultFilter filter) {
        Long totalCount = 0L;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT count(*) FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid)");
        sqlBuilder.append( " where (gw.iskantar = 0 and gw.status="+ SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value()+")");
       /* if(filter != null && filter.getKeyword() != null && !filter.getKeyword().equals("")) {
            sqlBuilder.append(" AND ")
                    .append("(")
                    .append("('' || gw.waiverID || '' ) like '%"+filter.getKeyword()+"%'")
                    .append(" OR ")
                    .append("lower(gw.name) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("to_char(to_timestamp(gw.creationdate/1000),'YYYY') like '%"+filter.getKeyword()+"%'")
                    .append(" OR ")
                    .append("(select lower(b.name) from grailbrandfields b where b.id = gw.brand) like '%"+filter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("(select (lower(u.firstname) || ' ' || lower(u.lastname)) FROM jiveUser u where u.userid = gw.creationby) like '%"+filter.getKeyword().toLowerCase()+"%'")

                    .append(")");
        }
*/
        sqlBuilder.append(applyPendingWaiverFilter(filter));
        try {
            //totalCount = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sqlBuilder.toString(), user.getID());
        	totalCount = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(sqlBuilder.toString());
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return totalCount;
    }

    @Override
    public List<ProjectWaiver> getPendingApprovalWaivers(final User user, final ProjectResultFilter filter) {
        List<ProjectWaiver> projectWaivers = Collections.emptyList();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT " + getSelectColumnsWithAlias(PROJECT_WAIVER_FIELDS, "gw") +" FROM grailwaiver gw INNER JOIN grailwaiverapprovers ga ON (gw.waiverid = ga.waiverid)");
        sqlBuilder.append(" where (gw.iskantar=0 AND gw.status="+ SynchroGlobal.ProjectWaiverStatus.PENDING_APPROVAL.value()+")");
        /*if(filter != null && filter.getKeyword() != null && !filter.getKeyword().equals("")) {
                sqlBuilder.append(" AND ")
                        .append("(")
                        .append("('' || gw.waiverID || '' ) like '%" + filter.getKeyword() + "%'")
                        .append(" OR ")
                        .append("lower(gw.name) like '%"+filter.getKeyword().toLowerCase()+"%'")
                        .append(" OR ")
                        .append("to_char(to_timestamp(gw.creationdate/1000),'YYYY') like '%"+filter.getKeyword()+"%'")
                        .append(" OR ")
                        .append("(select lower(b.name) from grailbrandfields b where b.id = gw.brand) like '%"+filter.getKeyword().toLowerCase()+"%'")
                        .append(" OR ")
                        .append("(select (lower(u.firstname) || ' ' || lower(u.lastname)) FROM jiveUser u where u.userid = gw.creationby) like '%" + filter.getKeyword().toLowerCase() + "%'")
                        .append(" OR ")
                        .append(" gw.waiverid in (select gwe.waiverid from grailwaiverendmarket gwe, grailendmarketfields ef where lower(ef.name) like '%").append(filter.getKeyword().toLowerCase()).append("%' and gwe.endmarketid = ef.id )")
                        .append(" OR ")
                        .append("(select (lower(u.firstname) || ' ' || lower(u.lastname)) FROM jiveUser u where u.userid = ga.approverid) like '%" + filter.getKeyword().toLowerCase() + "%'")
                        .append(")");
        }*/
        sqlBuilder.append(applyPendingWaiverFilter(filter));

        if(filter != null) {
            String orderBy = getOrderByClause(filter.getSortField(), filter.getAscendingOrder());
            sqlBuilder.append(orderBy);
            if(filter.getStart() != null) {
                sqlBuilder.append(" OFFSET ").append(filter.getStart());
            }
            if(filter.getLimit() != null) {
                sqlBuilder.append(" LIMIT ").append(filter.getLimit());
            }
        } else {
            sqlBuilder.append(" order by gw.waiverid");
        }
        try {
            projectWaivers = getSimpleJdbcTemplate().getJdbcOperations().query(sqlBuilder.toString(), projectWaiverRowMapper);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaivers;
    }

    public String applyPendingWaiverFilter(ProjectResultFilter projectResultFilter)
    {
    	StringBuilder sql = new StringBuilder();
    	if(projectResultFilter.getKeyword() != null && !projectResultFilter.getKeyword().equals("")) {
    		sql.append(" AND ")
                    .append("(")
                    .append("('' || gw.waiverID || '' ) like '%" + projectResultFilter.getKeyword() + "%'")
                    .append(" OR ")
                    .append("lower(gw.name) like '%"+projectResultFilter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("to_char(to_timestamp(gw.creationdate/1000),'YYYY') like '%"+projectResultFilter.getKeyword()+"%'")
                    .append(" OR ")
                    .append("(select lower(b.name) from grailbrandfields b where b.id = gw.brand) like '%"+projectResultFilter.getKeyword().toLowerCase()+"%'")
                    .append(" OR ")
                    .append("(select (lower(u.firstname) || ' ' || lower(u.lastname)) FROM jiveUser u where u.userid = gw.creationby) like '%" + projectResultFilter.getKeyword().toLowerCase() + "%'")
                    .append(" OR ")
                    .append(" gw.waiverid in (select gwe.waiverid from grailwaiverendmarket gwe, grailendmarketfields ef where lower(ef.name) like '%").append(projectResultFilter.getKeyword().toLowerCase()).append("%' and gwe.endmarketid = ef.id )")
                    .append(" OR ")
                    .append("(select (lower(u.firstname) || ' ' || lower(u.lastname)) FROM jiveUser u where u.userid = ga.approverid) like '%" + projectResultFilter.getKeyword().toLowerCase() + "%'")
                    .append(")");
    	}
    	 //Waiver Initiator
        String waiverInitiator =  projectResultFilter.getWaiverInitiator();
        if(!StringUtils.isNullOrEmpty(waiverInitiator))
        {
           
            sql.append(" and gw.creationby in (select u.userid from jiveuser u where (lower(u.firstname) || ' ' || lower(u.lastname)) like '%"+waiverInitiator.toLowerCase()+"%')");
            
        }
        
        
 // Start Date Filter
        
        if(projectResultFilter.getStartDateBegin() != null && projectResultFilter.getStartDateComplete()!=null) {
            StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
            
           sql.append(" AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'  AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
          
            
            
            
        }
        else if(projectResultFilter.getStartDateBegin() != null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateBegin = SynchroUtils.getDateString(projectResultFilter.getStartDateBegin().getTime());
           
            sql.append( " AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') >= '"+startDateBegin+"'");
          
        }
        else if(projectResultFilter.getStartDateComplete()!=null)
        {
     	   StringBuilder startDateCondition = new StringBuilder();
            String startDateComplete = SynchroUtils.getDateString(projectResultFilter.getStartDateComplete().getTime());
           
            sql.append(" AND to_char(to_timestamp(gw.creationdate/1000),'YYYY-MM-DD') <= '"+startDateComplete+"'");
            
        }
        
     // End Date (Date of Approval) Filter 
        if(projectResultFilter.getEndDateBegin() != null && projectResultFilter.getEndDateComplete()!=null) {
            StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
            sql.append(" AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'  AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
            
        }
        else if (projectResultFilter.getEndDateBegin() != null)
        {
     	    StringBuilder endDateCondition = new StringBuilder();
            String endDateBegin = SynchroUtils.getDateString(projectResultFilter.getEndDateBegin().getTime());
           
            
            sql.append( " AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') >= '"+endDateBegin+"'");
            
        }
        else if (projectResultFilter.getEndDateComplete()!=null)
        {
     	    StringBuilder endDateCondition = new StringBuilder();
            String endDateComplete = SynchroUtils.getDateString(projectResultFilter.getEndDateComplete().getTime());
                       
            sql.append(" AND to_char(to_timestamp(gw.modificationdate/1000),'YYYY-MM-DD') <= '"+endDateComplete+"'");
            
        }
        
        //Waiver Brand Fields
        List<Long> brandFields = projectResultFilter.getBrands();
        if(brandFields!=null && brandFields.size()>0)
        {
            sql.append(" and gw.brand in ("+StringUtils.join(brandFields, ',')+")");
            
        }
      //Research End Market Filter
        if(projectResultFilter.getResearchEndMarkets() != null && projectResultFilter.getResearchEndMarkets().size()>0 && !isListNull(projectResultFilter.getResearchEndMarkets())) {
           
        	sql.append(" and gw.waiverid in ( select waiverid from grailwaiverendmarket where endmarketid in ("+StringUtils.join(projectResultFilter.getResearchEndMarkets(), ',')+"))");
        }
        
    	return sql.toString();
    }
    private String getOrderByClause(final String sortField, final Integer order) {
        StringBuilder orderBy = new StringBuilder();

        if(org.apache.commons.lang.StringUtils.isNotBlank(sortField)) {
            String field = null;
            if(sortField.equals("id")) {
                field = "gw.waiverID";
            } else if(sortField.equals("name")) {
                field = "gw.name";
            } else if(sortField.equals("owner")) {
                field = "(select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = gw.creationby)";
            } else if(sortField.equals("brand")) {
                field = "(select lower(b.name) from grailbrandfields b where b.id = gw.brand)";
            } else if(sortField.equals("year")) {
                field = "to_char(to_timestamp(gw.creationdate/1000),'YYYY')::int";
            }  else if(sortField.equals("approver")) {
                field = "(select (u.firstname || ' ' || u.lastname) FROM jiveUser u where u.userid = ga.approverid)";
            }
            else {
                field = sortField;
            }
            if(org.apache.commons.lang.StringUtils.isNotBlank(field)) {
                orderBy.append(" order by ");
                orderBy.append(field).append(" ").append(SynchroDAOUtil.getSortType(order));
            }
        } else {
            orderBy.append(" order by ");
            orderBy.append("gw.waiverid ").append(SynchroDAOUtil.getSortType(1));
        }
        return orderBy.toString();
    }

    @Override
    @Transactional
    public ProjectWaiverApprover getWaiverApprover(final Long waiverID) {
        ProjectWaiverApprover projectWaiverApprover = new ProjectWaiverApprover();
        try {
            Long count = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(COUNT_WAIVER_APPROVERS, waiverID);
            if(count>0)
                projectWaiverApprover = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_PROJECT_WAIVER_APPROVER, projectWaiverApproverRowMapper, waiverID);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        return projectWaiverApprover;
    }


    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(final SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }

    private final ParameterizedRowMapper<ProjectWaiver> projectWaiverRowMapper = new ParameterizedRowMapper<ProjectWaiver>() {

        public ProjectWaiver mapRow(ResultSet rs, int row) throws SQLException {
            ProjectWaiver projectWaiver = new ProjectWaiver();
            projectWaiver.setWaiverID(rs.getLong("waiverid"));
            projectWaiver.setName(rs.getString("name"));
            projectWaiver.setSummary(rs.getString("summary"));
            projectWaiver.setBrand(rs.getLong("brand"));
            projectWaiver.setMethodology(rs.getLong("methodology"));
            projectWaiver.setPreApprovals(synchroDAOUtil.getIDs(rs.getString("preApprovals")));
            projectWaiver.setPreApprovalComment(rs.getString("preApprovalComment"));
            projectWaiver.setNexus(rs.getLong("nexus"));
            projectWaiver.setStatus(rs.getLong("status"));
            projectWaiver.setCreationBy(rs.getLong("creationby"));
            projectWaiver.setModifiedBy(rs.getLong("modificationby"));
            projectWaiver.setCreationDate(rs.getLong("creationdate"));
            projectWaiver.setModifiedDate(rs.getLong("modificationdate"));
            projectWaiver.setIsKantar(rs.getBoolean("iskantar"));
            return projectWaiver;
        }
    };

    private final ParameterizedRowMapper<ProjectWaiverApprover> projectWaiverApproverRowMapper = new ParameterizedRowMapper<ProjectWaiverApprover>() {
        public ProjectWaiverApprover mapRow(ResultSet rs, int row) throws SQLException {
            ProjectWaiverApprover projectWaiverApprover = new ProjectWaiverApprover();
            projectWaiverApprover.setWaiverID(rs.getLong("waiverid"));
            projectWaiverApprover.setApproverID(rs.getLong("approverid"));
            projectWaiverApprover.setComments(rs.getString("approvercomments"));
            projectWaiverApprover.setIsApproved(rs.getInt("isapproved"));

            projectWaiverApprover.setCreationBy(rs.getLong("creationby"));
            projectWaiverApprover.setModifiedBy(rs.getLong("modificationby"));
            projectWaiverApprover.setCreationDate(rs.getLong("creationdate"));
            projectWaiverApprover.setModifiedDate(rs.getLong("modificationdate"));
            return projectWaiverApprover;
        }
    };

    private final ParameterizedRowMapper<ProjectWaiverEndMarket> projectWaiverEndMarketRowMapper = new ParameterizedRowMapper<ProjectWaiverEndMarket>() {

        public ProjectWaiverEndMarket mapRow(ResultSet rs, int row) throws SQLException {
            ProjectWaiverEndMarket projectWaiverEndMarket = new ProjectWaiverEndMarket();
            projectWaiverEndMarket.setWaiverid(rs.getLong("waiverid"));
            projectWaiverEndMarket.setMarketid(rs.getLong("endmarketID"));
            projectWaiverEndMarket.setCreationBy(rs.getLong("creationby"));
            projectWaiverEndMarket.setModifiedBy(rs.getLong("modificationby"));
            projectWaiverEndMarket.setCreationDate(rs.getLong("creationdate"));
            projectWaiverEndMarket.setModifiedDate(rs.getLong("modificationdate"));
            return projectWaiverEndMarket;
        }
    };


    private static User getUser() {
        return JiveApplication.getContext().getAuthenticationProvider().getJiveUser();
    }

    private static String getSelectColumnsWithAlias(final String fields, final String alias) {
        StringBuilder columns = new StringBuilder();
        String[] colArr = fields.split(",");
        int index = 0;
        for(String col: colArr) {
            columns.append(alias).append(".").append(col.trim()).append(" as ").append(col.trim());
            if(index < colArr.length-1) {
                columns.append(",");
            }
            index++;
        }
        return columns.toString();
    }

    @Override
    public Boolean doesWaiverExists(Long waiverID) {
        Long totalCount = 0L;
        String WAIVER_COUNT = "Select count(*) from grailwaiver where waiverID = ?";
        try {
            totalCount = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(WAIVER_COUNT, waiverID);
        } catch (DataAccessException e) {
            LOG.info(e.getMessage(), e);
        }
        if(totalCount > 0)
            return true;
        else
            return false;

    }

    @Override
    public Long generateWaiverID() {
        return synchroDAOUtil.generateWaiverID();
    }

    /**
     * Attachments
     */
    @Override
    public List<AttachmentBean> getFieldAttachments(SynchroAttachment attachment) {
        List<AttachmentBean> bean = new ArrayList<AttachmentBean>();
        try{
            bean = getSimpleJdbcTemplate().query(GET_ATTACHMENT_BY_OBJECT, attachmentRowMapper, attachment.getObjectType(), attachment.getID());
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return bean;
    }

    @Override
    public void saveAttachmentUser(final Long attachmentId, Long userId) {
        try {
            getSimpleJdbcTemplate().update(INSERT_ATTACHMENT_USER,  attachmentId,userId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to SAVE Attachment User for Attachment Id - " + attachmentId;
            daEx.printStackTrace();
            throw new DAOException(message, daEx);
        }

    }

    @Override
    public void updateDocumentAttachment(Long attachmentId, long objectId)
    {
        try{
            getSimpleJdbcTemplate().update( UPDATE_ATTACHMENT_OBJECT_ID, objectId,attachmentId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to UPDATE Object Id for Attachment Id --  " + attachmentId ;
            daEx.printStackTrace();
            throw new DAOException(message, daEx);
        }
    }

    @Override
    public void deleteAttachmentUser(final Long attachmentId) {
        try {
            getSimpleJdbcTemplate().update(DELETE_ATTACHMENT_USER,  attachmentId);

        } catch (DataAccessException daEx) {
            final String message = "Failed to Delete Attachment User for Attachment Id - " + attachmentId;
            daEx.printStackTrace();
            throw new DAOException(message, daEx);
        }

    }

    @Override
    public Long getAttachmentUser(final Long attachmentId) {
        Long userId=null;
        try {
            userId = getSimpleJdbcTemplate().getJdbcOperations().queryForLong(GET_ATTACHMENT_USER, attachmentId);


        } catch (DataAccessException daEx) {
            // final String message = "Failed to Get Attachment User for Attachment Id - " + attachmentId;
            // LOG.log(Level.SEVERE, message, daEx);
            // throw new DAOException(message, daEx);
        }
        return userId;
    }

    private final RowMapper<AttachmentBean> attachmentRowMapper =  new RowMapper<AttachmentBean>() {
        public AttachmentBean mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            AttachmentBean bean = new AttachmentBean();
            bean.setID(rs.getLong("attachmentid"));
            bean.setObjectID(rs.getLong("objectid"));
            bean.setObjectType(rs.getInt("objecttype"));
            bean.setName(rs.getString("filename"));
            bean.setSize(rs.getInt("filesize"));
            bean.setContentType(rs.getString("contenttype"));
            bean.setCreationDate(rs.getLong("creationdate"));
            bean.setModificationDate(rs.getLong("modificationdate"));
            return bean;
        }
    };

}

