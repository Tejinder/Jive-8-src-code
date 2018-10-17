package com.grail.synchro.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.dao.StageDAO;
import com.jivesoftware.base.User;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.community.impl.dao.UserDAO;

/**
 * Operations related to stage for each Project
 * @author: Tejinder
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class StageDAOImpl extends SynchroAbstractDAO implements StageDAO {

    private static final Logger LOG = Logger.getLogger(StageDAOImpl.class.getName());

    private static final String LOAD_TODOLIST_BY_STAGE = "SELECT id, stageid, todoaction, role, notificationrole FROM grailstagetodolist WHERE stageid = ? ORDER BY id ";
	private static final String LOAD_TODOLIST_BY_ID = "SELECT id, stageid, todoaction, role, notificationrole FROM grailstagetodolist WHERE id = ?";
	private static final String LOAD_STAGE_APPROVERS = "SELECT userid from grailprojectpermission WHERE projectid=? and stage=? ";
	private static final String INSERT_STAGE_APPROVERS = "INSERT INTO grailstageapprovers( " +
            " projectid, stageid, approverid,approvaldate) " +
            " VALUES (?, ?, ?, ?)";
    private static final String GET_APPROVAL_DATE = "SELECT approvaldate FROM grailstageapprovers where projectid = ? and stageid = ? and approverid=? ";

	private UserDAO userDAO;
    /**
	 * Reusable row mapper for mapping a result set to SynchroStageToDoListBean
	 * .
	 */
	private final RowMapper<SynchroStageToDoListBean> stageToDoListMapper = new RowMapper<SynchroStageToDoListBean>() {
		public SynchroStageToDoListBean mapRow(ResultSet rs, int row)
				throws SQLException {
			SynchroStageToDoListBean toDoListBean = new SynchroStageToDoListBean();
			toDoListBean.setId(rs.getLong("id"));
			toDoListBean.setStageId(rs.getLong("stageid"));
			toDoListBean.setToDoAction(rs.getString("todoaction"));
			toDoListBean.setRole(rs.getString("role"));
			toDoListBean.setNotificationRecipients(rs.getString("notificationrole"));
			return toDoListBean;
		}
	};
	@Override
	/**
	 * This method will get the SynchroStageToDoListBean for a given stage Id
	 */
	public List<SynchroStageToDoListBean> getToDoListSequence(Long stageId) {

		final List<SynchroStageToDoListBean> toDoListBeans = getSimpleJdbcTemplate()
				.query(LOAD_TODOLIST_BY_STAGE, stageToDoListMapper, stageId);
		return toDoListBeans;

	}
	@Override
	public SynchroStageToDoListBean getToDoList(long notificationTabId) {

		List<SynchroStageToDoListBean> toDoListBeans = getSimpleJdbcTemplate()
				.query(LOAD_TODOLIST_BY_ID, stageToDoListMapper,
						notificationTabId);
		return toDoListBeans.get(0);
	}
	
	@Override
	public List<? extends User> getStageApprovers(long projectId, long stageId, String groupId) {

		 List<Long> approverIds = Collections.emptyList();
		 List<? extends User> stageApprovers = Collections.emptyList();
		 String sql = LOAD_STAGE_APPROVERS + " and jiveugid in ("+groupId +")";
		 try {
			 approverIds = getSimpleJdbcTemplate().getJdbcOperations().queryForList(sql,
	                                                                        Long.class, projectId,stageId);
			 if(approverIds.size()>0)
			 {
				 stageApprovers = userDAO.getUsersByIDs(new HashSet<Long>(approverIds));
			 }
	        }
	        catch (DataAccessException e) {
	            String message = "Failed to load stage approvers for project " + projectId + " and stage "+ stageId;
	            LOG.info("Error "+ message);
	            throw new DAOException(message, e);
	        }
		 return stageApprovers;
	}
	
	@Override
	public void updateStageApprovers(final Long projectId, final Long stageId,
			final Long approverId) {
		try {
			getSimpleJdbcTemplate().update(INSERT_STAGE_APPROVERS, projectId,
					stageId, approverId, (new Date()).getTime());

		} catch (DataAccessException daEx) {
			final String message = "Failed to UPDATE PIB Stage Approvers for Project - "
					+ projectId;
			LOG.log(Level.SEVERE, message, daEx);
			throw new DAOException(message, daEx);
		}
	}
	 
	@Override
	public Long getApprovalDate(final Long projectID, final Long stageId,
			final Long approverId) {
		try {
			Long approvalDate = getSimpleJdbcTemplate().getJdbcOperations()
					.queryForLong(GET_APPROVAL_DATE, projectID, stageId,
							approverId);
			return approvalDate;

		} catch (DataAccessException e) {
			final String message = "No Approval Date exist for project "
					+ projectID + " and stageId " + stageId;
			LOG.info("Error " + message);
			//throw new DAOException(message, e);
			return null;
		}
	}
	public UserDAO getUserDAO() {
		return userDAO;
	}
	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
}
