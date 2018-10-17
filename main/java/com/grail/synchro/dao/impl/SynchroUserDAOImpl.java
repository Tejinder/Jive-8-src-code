package com.grail.synchro.dao.impl;

import com.grail.synchro.dao.SynchroUserDAO;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.base.database.dao.DAOException;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/28/14
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroUserDAOImpl extends JiveJdbcDaoSupport implements SynchroUserDAO {
    private static Logger LOG = Logger.getLogger(SynchroUserDAOImpl.class);

    private SynchroDAOUtil synchroDAOUtil;

    private static final String GET_BY_EMAIL = "select * from jiveuser1 where email =?";
    private static final String GET_USER_PROP = "select * from jiveuserprop1 where userid =? and name=?";

    @Override
    public User getUserByEmail(final String email) {

        User user = null;
        try {
            List<User> users = getJdbcTemplate().query(GET_BY_EMAIL, userParameterizedRowMapper, email);
            if(users != null && users.size() > 0) {
                user = users.get(0);
            }
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return user;
    }

    private final ParameterizedRowMapper<User> userParameterizedRowMapper = new ParameterizedRowMapper<User>() {

        public User mapRow(ResultSet rs, int row) throws SQLException {
            UserTemplate user = new UserTemplate();
            user.setID(rs.getLong("userid"));
            user.setUsername(rs.getString("username"));
            user.setName(rs.getString("name"));
            user.setFirstName(rs.getString("firstname"));
            user.setLastName(rs.getString("lastname"));
            user.setEmail(rs.getString("email"));
            user.setEnabled(rs.getBoolean("userenabled"));
            user.setVisible(rs.getBoolean("visible"));
            return user;
        }
    };

    @Override
    public boolean isUserPropExists(final Long userId, final String userProp) {
        boolean userPropExits = false;
        try {
            List<User> users = getJdbcTemplate().query(GET_USER_PROP, userPropParameterizedRowMapper, userId, userProp);
            if(users != null && users.size() > 0) {
                userPropExits = true;
            } else {
                userPropExits = false;
            }
        } catch (DAOException e) {
            LOG.trace(e.getMessage());
        }
        return userPropExits;
    }

    private final ParameterizedRowMapper<User> userPropParameterizedRowMapper = new ParameterizedRowMapper<User>() {

        public User mapRow(ResultSet rs, int row) throws SQLException {
            UserTemplate user = new UserTemplate();
            user.setID(rs.getLong("userid"));
            return user;
        }
    };

    public SynchroDAOUtil getSynchroDAOUtil() {
        return synchroDAOUtil;
    }

    public void setSynchroDAOUtil(SynchroDAOUtil synchroDAOUtil) {
        this.synchroDAOUtil = synchroDAOUtil;
    }
}
