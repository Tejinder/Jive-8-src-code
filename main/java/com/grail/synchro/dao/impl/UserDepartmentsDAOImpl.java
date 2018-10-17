package com.grail.synchro.dao.impl;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.UserDepartment;
import com.grail.synchro.dao.UserDepartmentsDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 10:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserDepartmentsDAOImpl extends JiveJdbcDaoSupport implements UserDepartmentsDAO {
    private static Logger LOG = Logger.getLogger(UserDepartmentsDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static String FIELDS = "name, description";
    private static String GET_USER_DEPARTMENT = "SELECT id,"+FIELDS+" FROM grailuserdepartments where id = ? order by name";
    private static String INSERT_USER_DEPARTMENT = "INSERT INTO grailuserdepartments (id,"+FIELDS+") VALUES (?,?,?)";
    private static String UPDATE_USER_DEPARTMENT = "UPDATE grailuserdepartments SET "+FIELDS.replaceAll(",", "=?,")+"=? WHERE id=?";
    private static String GET_USER_DEPARTMENTS =  "SELECT id,"+FIELDS+" FROM grailuserdepartments";
    private static String DELETE_USER_DEPARTMENT =  "DELETE FROM grailuserdepartments WHERE id = ?";


    @Override
    public UserDepartment get(final Long id) {
        UserDepartment userDepartment = null;
        try {
            userDepartment = getJdbcTemplate().queryForObject(GET_USER_DEPARTMENT, userDepartmentParameterizedRowMapper, id);
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return userDepartment;
    }

    @Override
    public void save(final UserDepartment userDepartment) {
        Long id = synchroDAOUtil.nextSequenceID("id", "grailuserdepartments");
        try {
            userDepartment.setId(id);
            getJdbcTemplate().update(INSERT_USER_DEPARTMENT,
                    userDepartment.getId(),
                    userDepartment.getName(),
                    userDepartment.getDescription()
            );
            SynchroGlobal.updateUserDepartmentMap(userDepartment, "update");
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
    }

    @Override
    public void update(final UserDepartment userDepartment) {
        try {
            getJdbcTemplate().update(UPDATE_USER_DEPARTMENT,
                    userDepartment.getName(),
                    userDepartment.getDescription(),
                    userDepartment.getId()
            );
            SynchroGlobal.updateUserDepartmentMap(userDepartment, "update");
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
    }

    @Override
    public void delete(final Long id) {
        try {
            getJdbcTemplate().update(DELETE_USER_DEPARTMENT,id);
            SynchroGlobal.updateUserDepartmentMap(id, "remove");
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
    }

    @Override
    public List<UserDepartment> getAll() {
        List<UserDepartment> userDepartments = new ArrayList<UserDepartment>();
        try {
            userDepartments = getJdbcTemplate().query(GET_USER_DEPARTMENTS, userDepartmentParameterizedRowMapper);
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return userDepartments;
    }

    @Override
    public UserDepartment getByName(final String name) {

        String sql = "SELECT id,"+FIELDS+" FROM grailuserdepartments where lower(name) ='"+name.toLowerCase()+"' order by name";
        List<UserDepartment> userDepartments = null;
        try {
            userDepartments = getJdbcTemplate().query(sql, userDepartmentParameterizedRowMapper);

            if(userDepartments != null && userDepartments.size() > 0) {
                 return userDepartments.get(0);
            }
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return null;
    }

    private final ParameterizedRowMapper<UserDepartment> userDepartmentParameterizedRowMapper = new ParameterizedRowMapper<UserDepartment>() {

        public UserDepartment mapRow(ResultSet rs, int row) throws SQLException {
            UserDepartment userDepartment = new UserDepartment();
            userDepartment.setId(rs.getLong("id"));
            userDepartment.setName(rs.getString("name"));
            userDepartment.setDescription(rs.getString("description"));
            return userDepartment;
        }
    };


    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
