package com.grail.synchro.dao.impl;

import com.grail.synchro.beans.PendingActivity;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.dao.PendingActivityDAO;
import com.grail.synchro.dao.ProjectEvaluationDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PendingActivityDAOImpl extends JiveJdbcDaoSupport implements PendingActivityDAO {

    private static Logger LOG = Logger.getLogger(PendingActivityDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static String FIELDS = "projectid, endmarketid, ismultimarket,stage,responsibleBy,activity, activityLink, active";
    private static String INSERT_PENDING_ACTIVITY = "INSERT INTO grailpendingactivities (id, "+FIELDS+") VALUES (?,?,?,?,?,?,?,?)";
    private static String UPDATE_PENDING_ACTIVITY = "UPDATE grailpendingactivities SET "+FIELDS.replace(",","=?,")+"=? WHERE id = ?";
    private static String GET_BY_PROJECT_ENDMARKET = "SELECT id,"+FIELDS+" FROM grailpendingactivities WHERE projectid = ? AND endmarketid = ?";
    private static String GET_BY_PROJECT = "SELECT id,"+FIELDS+" FROM grailpendingactivities WHERE projectid = ?";

    @Override
    public void save(final PendingActivity activity) {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailpendingactivities");
        try {
            activity.setId(id);
            getJdbcTemplate().update(INSERT_PENDING_ACTIVITY,
                    activity.getId(),
                    activity.getProjectId(),
                    activity.getEndMarketId(),
                    activity.getMultiMarket()?1:0,
                    activity.getStage(),
                    activity.getResponsibleBy(),
                    activity.getActivity(),
                    activity.getActivityLink(),
                    activity.getActive()?1:0
            );
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
    }

    @Override
    public void update(final PendingActivity activity) {
        try {
            getJdbcTemplate().update(UPDATE_PENDING_ACTIVITY,
                    activity.getProjectId(),
                    activity.getEndMarketId(),
                    activity.getMultiMarket()?1:0,
                    activity.getStage(),
                    activity.getResponsibleBy(),
                    activity.getActivity(),
                    activity.getActivityLink(),
                    activity.getActive()?1:0,
                    activity.getId()
            );
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
    }

    @Override
    public List<PendingActivity> get(Long projectId, Long endmarketId) {
        List<PendingActivity> pendingActivities = Collections.emptyList();
        try {
            pendingActivities = getJdbcTemplate().query(GET_BY_PROJECT_ENDMARKET, pendingActivityParameterizedRowMapper,
                    projectId,
                    endmarketId
            );
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return pendingActivities;
    }

    @Override
    public List<PendingActivity> get(final Long projectId) {
        List<PendingActivity> pendingActivities = Collections.emptyList();
        try {
            pendingActivities = getJdbcTemplate().query(GET_BY_PROJECT, pendingActivityParameterizedRowMapper, projectId);
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return pendingActivities;
    }


    private final ParameterizedRowMapper<PendingActivity> pendingActivityParameterizedRowMapper = new ParameterizedRowMapper<PendingActivity>() {

        public PendingActivity mapRow(ResultSet rs, int row) throws SQLException {
            PendingActivity pendingActivity = new PendingActivity();
            pendingActivity.setId(rs.getLong("id"));
            pendingActivity.setProjectId(rs.getLong("projectid"));
            pendingActivity.setEndMarketId(rs.getLong("endmarketid"));
            pendingActivity.setMultiMarket(rs.getBoolean("ismultimarket"));
            pendingActivity.setStage(rs.getInt("stage"));
            pendingActivity.setResponsibleBy(rs.getString("responsibleBy"));
            pendingActivity.setActivity(rs.getInt("activity"));
            pendingActivity.setActivityLink(rs.getString("activityLink"));
            pendingActivity.setActive(rs.getBoolean("active"));
            return pendingActivity;
        }
    };


    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
