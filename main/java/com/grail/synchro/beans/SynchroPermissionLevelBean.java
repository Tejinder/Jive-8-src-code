package com.grail.synchro.beans;

/**
 * Representation of a GrailProjectPermission in the DAO.
 *
 * @author: Vivek Kondur
 */
public class SynchroPermissionLevelBean extends BeanObject {

    private long projectId;
    private long userId;
    private long jiveGroupId;
    private int stage;

    //Default
    public SynchroPermissionLevelBean() {
    }

    //Overloaded
    public SynchroPermissionLevelBean(final long projectId, final long userId, final long jiveGroupId, final int stage) {
        this.projectId = projectId;
        this.userId = userId;
        this.jiveGroupId = jiveGroupId;
        this.stage = stage;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    public long getJiveGroupId() {
        return jiveGroupId;
    }

    public void setJiveGroupId(final long jiveGroupId) {
        this.jiveGroupId = jiveGroupId;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(final int stage) {
        this.stage = stage;
    }
}
