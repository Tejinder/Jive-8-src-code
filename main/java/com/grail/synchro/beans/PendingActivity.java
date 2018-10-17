package com.grail.synchro.beans;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class PendingActivity {
    private Long id;
    private Long projectId;
    private Long endMarketId;
    private Boolean isMultiMarket = false;
    private Integer stage;
    private String responsibleBy;
    private Integer activity;
    private String activityLink;
    private Boolean isActive = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEndMarketId() {
        return endMarketId;
    }

    public void setEndMarketId(Long endMarketId) {
        this.endMarketId = endMarketId;
    }

    public Boolean getMultiMarket() {
        return isMultiMarket;
    }

    public void setMultiMarket(Boolean multiMarket) {
        isMultiMarket = multiMarket;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public String getResponsibleBy() {
        return responsibleBy;
    }

    public void setResponsibleBy(String responsibleBy) {
        this.responsibleBy = responsibleBy;
    }

    public Integer getActivity() {
        return activity;
    }

    public void setActivity(Integer activity) {
        this.activity = activity;
    }

    public String getActivityLink() {
        return activityLink;
    }

    public void setActivityLink(String activityLink) {
        this.activityLink = activityLink;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
