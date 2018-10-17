package com.grail.synchro.beans;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 8/5/14
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpendReportExtractBean {

    private Long userId;
    private Long userName;
    private String reportLabel;
    private BigDecimal quarter1 = new BigDecimal(0);
    private BigDecimal quarter2 = new BigDecimal(0);
    private BigDecimal quarter3 = new BigDecimal(0);
    private BigDecimal quarter4 = new BigDecimal(0);
    private Integer order = 1;
    private String currency;
    private Long projectId;
    private Long endMarketId;
    private Integer investmentType;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserName() {
        return userName;
    }

    public void setUserName(Long userName) {
        this.userName = userName;
    }

    public String getReportLabel() {
        return reportLabel;
    }

    public void setReportLabel(String reportLabel) {
        this.reportLabel = reportLabel;
    }

    public BigDecimal getQuarter1() {
        return quarter1;
    }

    public void setQuarter1(BigDecimal quarter1) {
        this.quarter1 = quarter1;
    }

    public BigDecimal getQuarter2() {
        return quarter2;
    }

    public void setQuarter2(BigDecimal quarter2) {
        this.quarter2 = quarter2;
    }

    public BigDecimal getQuarter3() {
        return quarter3;
    }

    public void setQuarter3(BigDecimal quarter3) {
        this.quarter3 = quarter3;
    }

    public BigDecimal getQuarter4() {
        return quarter4;
    }

    public void setQuarter4(BigDecimal quarter4) {
        this.quarter4 = quarter4;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
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

    public Integer getInvestmentType() {
        return investmentType;
    }

    public void setInvestmentType(Integer investmentType) {
        this.investmentType = investmentType;
    }
}
