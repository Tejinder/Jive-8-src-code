package com.grail.synchro.beans;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.community.lifecycle.JiveApplication;

import java.util.Date;

/**
 * @author Bhaskar
 * @version 1.0
 */
public class EndMarketDashboardViewBean {
    private Long id;
    private String name;
    private String region;
    private Date startDate;
    private Integer startYear = -1;
    private Integer startMonth = -1;
    private Date endDate;
    private Integer endYear = -1;
    private Integer endMonth = -1;
    private Long projectId;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public Integer getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(Integer endMonth) {
        this.endMonth = endMonth;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public static EndMarketDashboardViewBean toEndMarketDashboardViewBean(final EndMarketInvestmentDetail detail) {
        EndMarketDashboardViewBean bean = new EndMarketDashboardViewBean();
        bean.setId(detail.getEndMarketID());
        bean.setName(SynchroGlobal.getEndMarkets().get(detail.getEndMarketID().intValue()));
        bean.setProjectId(detail.getProjectID());
        //TODO: Following bean properties will change once multi-market project has been integrated
        bean.setStartYear(-1);
        bean.setStartMonth(-1);
        bean.setEndYear(-1);
        bean.setEndMonth(-1);
        return bean;
    }


}
