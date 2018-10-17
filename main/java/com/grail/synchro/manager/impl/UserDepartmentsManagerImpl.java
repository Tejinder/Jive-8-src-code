package com.grail.synchro.manager.impl;

import com.grail.synchro.beans.UserDepartment;
import com.grail.synchro.dao.UserDepartmentsDAO;
import com.grail.synchro.manager.UserDepartmentsManager;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 10:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserDepartmentsManagerImpl implements UserDepartmentsManager {

    private UserDepartmentsDAO userDepartmentsDAO;

    @Override
    public UserDepartment get(final Long id) {
        return userDepartmentsDAO.get(id);
    }

    @Override
    public void save(final UserDepartment userDepartment) {
        if(userDepartment.getId() != null && userDepartment.getId() > 0) {
            userDepartmentsDAO.update(userDepartment);
        } else {
            userDepartmentsDAO.save(userDepartment);
        }
    }

    @Override
    public void save(final Long id, final String name, final String desc) {
        UserDepartment userDepartment = new UserDepartment();
        userDepartment.setId(id);
        userDepartment.setName(name);
        userDepartment.setDescription(desc);
        save(userDepartment);
    }

    @Override
    public void delete(final Long id) {
        userDepartmentsDAO.delete(id);
    }

    @Override
    public List<UserDepartment> getAll() {
        return userDepartmentsDAO.getAll();
    }

    @Override
    public UserDepartment getByName(final String name) {
        return userDepartmentsDAO.getByName(name);
    }

    public UserDepartmentsDAO getUserDepartmentsDAO() {
        return userDepartmentsDAO;
    }

    public void setUserDepartmentsDAO(UserDepartmentsDAO userDepartmentsDAO) {
        this.userDepartmentsDAO = userDepartmentsDAO;
    }
}
