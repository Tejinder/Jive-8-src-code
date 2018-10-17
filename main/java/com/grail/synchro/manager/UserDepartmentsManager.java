package com.grail.synchro.manager;

import com.grail.synchro.beans.UserDepartment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 10:32 AM
 * To change this template use File | Settings | File Templates.
 */
public interface UserDepartmentsManager {
    UserDepartment get(final Long id);
    void save(final UserDepartment userDepartment);
    void save(final Long id, final String name, final String desc);
    void delete(final Long id);
    List<UserDepartment> getAll();
    UserDepartment getByName(final String name);
}
