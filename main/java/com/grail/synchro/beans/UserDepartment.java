package com.grail.synchro.beans;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 10:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserDepartment {
    private Long id;
    private String name;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
