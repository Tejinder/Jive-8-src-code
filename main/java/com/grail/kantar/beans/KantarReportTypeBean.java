package com.grail.kantar.beans;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/7/15
 * Time: 11:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class KantarReportTypeBean {
    private Long id;
    private String name;
    private boolean isActive;
    private Integer sortOrder;
    private boolean otherType;

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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isOtherType() {
        return otherType;
    }

    public void setOtherType(boolean otherType) {
        this.otherType = otherType;
    }
}
