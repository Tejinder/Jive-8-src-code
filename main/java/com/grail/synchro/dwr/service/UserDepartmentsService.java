package com.grail.synchro.dwr.service;

import com.grail.synchro.beans.UserDepartment;
import com.grail.synchro.manager.UserDepartmentsManager;
import com.jivesoftware.community.dwr.RemoteSupport;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 10:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserDepartmentsService extends RemoteSupport {
    private UserDepartmentsManager userDepartmentsManager;

    public void save(final Long id,final String name, final String description) {
        userDepartmentsManager.save(id, name, description);
    }

    public UserDepartment get(final Long id) {
       return userDepartmentsManager.get(id);
    }

    public void deleteDepartment(final Long id) {
        userDepartmentsManager.delete(id);
    }

    public List<UserDepartment> getAll() {
        return userDepartmentsManager.getAll();
    }

    public UserDepartment getByName(final String name) {
        return userDepartmentsManager.getByName(name);
    }

    public UserDepartmentsManager getUserDepartmentsManager() {
        return userDepartmentsManager;
    }

    public void setUserDepartmentsManager(UserDepartmentsManager userDepartmentsManager) {
        this.userDepartmentsManager = userDepartmentsManager;
    }


}
