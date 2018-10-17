package com.grail.synchro.dao;

import com.grail.synchro.beans.UserDepartment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 10:35 AM
 * To change this template use File | Settings | File Templates.
 */
public interface UserDepartmentsDAO {
    UserDepartment get(final Long id);
    void save(final UserDepartment userDepartment);
    void update(final UserDepartment userDepartment);
    void delete(final Long id);
    List<UserDepartment> getAll();
    UserDepartment getByName(final String name);
}
