package com.grail.synchro.beans;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/9/15
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class InvitedUser {

    private Long id;
    private String email;
    private Long invitedBy;
    private Date invitedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
    }

    public Date getInvitedDate() {
        return invitedDate;
    }

    public void setInvitedDate(Date invitedDate) {
        this.invitedDate = invitedDate;
    }
}
