package com.grail.synchro.dao.impl;

import com.grail.synchro.beans.AutoSaveDetailsBean;
import com.grail.synchro.dao.AutoSaveDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.exceptions.AutoSaveDetailsNotFoundException;
import com.grail.synchro.exceptions.AutoSavePersistException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class AutoSaveDAOImpl extends JiveJdbcDaoSupport implements AutoSaveDAO {
    private static final Logger LOG = Logger.getLogger(AutoSaveDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static final String FIELDS = "id, objectType, objectID, details, userID, isDraft";
    private static final String INSERT_AUTO_SAVE_DETAILS = "INSERT INTO grailAutoSaveFormDetails (" + FIELDS + ") " +
            "VALUES (?,?,?,?,?,?)";

    private static final String UPDATE_AUTO_SAVE_DETAILS = "UPDATE grailAutoSaveFormDetails SET " +
            "objectType=?, objectID=?, details=?, userID=?, isDraft=? WHERE id=?";

    private static final String GET_AUTO_SAVE_DETAILS = "SELECT " + FIELDS + " FROM grailAutoSaveFormDetails " +
            "WHERE objectType=? AND objectID=? AND userID=?";
    private static final String GET_DRAFT_DETAILS =  "SELECT " + FIELDS + " FROM grailAutoSaveFormDetails WHERE userID=? and isDraft=?";

    private static final String DELETE_DETAILS = "DELETE FROM grailAutoSaveFormDetails WHERE objectType=? AND objectID=? AND userID=?";

    @Override
    public void saveDetails(final AutoSaveDetailsBean bean) throws AutoSavePersistException {
        try{
            Long id = synchroDAOUtil.nextSequenceID("id", "grailAutoSaveFormDetails");
            bean.setId(id);
            getSimpleJdbcTemplate().getJdbcOperations().update(INSERT_AUTO_SAVE_DETAILS,
                    bean.getId(),
                    bean.getObjectType(),
                    bean.getObjectID(),
                    bean.getDetails(),
                    bean.getUserID(),
                    (bean.isDraft() ? 1:0)
            );
        } catch (DataAccessException e) {
            LOG.error(e.getCause());
            throw new AutoSavePersistException("Unable to save details.", e.getCause());
        }
    }

    @Override
    public void updateDetails(AutoSaveDetailsBean bean) throws AutoSavePersistException {
        try{
            getSimpleJdbcTemplate().getJdbcOperations().update(UPDATE_AUTO_SAVE_DETAILS,
                    bean.getObjectType(),
                    bean.getObjectID(),
                    bean.getDetails(),
                    bean.getUserID(),
                    (bean.isDraft()?1:0),
                    bean.getId()
            );
        } catch (DataAccessException e) {
            LOG.error(e.getCause());
            throw new AutoSavePersistException("Unable to save details.", e.getCause());
        }
    }

    @Override
    public AutoSaveDetailsBean getDetails(final Long objectType, final Long objectID,
                                          final Long userID) throws AutoSaveDetailsNotFoundException {
        AutoSaveDetailsBean bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_AUTO_SAVE_DETAILS,
                    autoSaveDetailsBeanRowMapper, objectType, objectID, userID);
        } catch (DataAccessException e) {
            throw new AutoSaveDetailsNotFoundException("Auto save details not found.", e.getCause());
        }
        return bean;
    }

    @Override
    public AutoSaveDetailsBean getDraftDetails(final Long userID, final boolean isDraft) throws AutoSaveDetailsNotFoundException {
        AutoSaveDetailsBean bean = null;
        try{
            bean = getSimpleJdbcTemplate().getJdbcOperations().queryForObject(GET_DRAFT_DETAILS,
                    autoSaveDetailsBeanRowMapper, userID, isDraft);
        } catch (DataAccessException e) {
            throw new AutoSaveDetailsNotFoundException("Auto save details not found.", e.getCause());
        }
        return bean;
    }

    @Override
    public boolean deleteDetails(final Long objectType, final Long objectID, final Long userID) {
        boolean success = false;
        try{
            int rows  = getSimpleJdbcTemplate().getJdbcOperations().update(DELETE_DETAILS, objectType, objectID, userID);
            success = rows > 0 ? true:false;
        } catch (DataAccessException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    private final ParameterizedRowMapper<AutoSaveDetailsBean> autoSaveDetailsBeanRowMapper
            = new ParameterizedRowMapper<AutoSaveDetailsBean>() {
        public AutoSaveDetailsBean mapRow(final ResultSet rs, final int row) throws SQLException {
            AutoSaveDetailsBean autoSaveDetails = new AutoSaveDetailsBean();
            autoSaveDetails.setId(rs.getLong("id"));
            autoSaveDetails.setObjectID(rs.getLong("objectID"));
            autoSaveDetails.setObjectType(rs.getLong("objectType"));
            autoSaveDetails.setDetails(rs.getString("details"));
            autoSaveDetails.setUserID(rs.getLong("userID"));
            autoSaveDetails.setDraft(rs.getBoolean("isDraft"));
            return autoSaveDetails;
        }
    };

    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
