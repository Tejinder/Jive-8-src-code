package com.grail.kantar.beans;

import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class KantarReportResultFilter {

    private String keyword;
    private String reportName;
    private List<Long> authors;
    private List<Long> endMarkets;
    private List<Integer> reportTypes;
    private Integer start;
    private Integer limit;
    private String sortField;
    private Integer ascendingOrder;
    private User owner;
    private boolean otherType;


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public List<Long> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Long> authors) {
        this.authors = authors;
    }

    public List<Long> getEndMarkets() {
        return endMarkets;
    }

    public void setEndMarkets(List<Long> endMarkets) {
        this.endMarkets = endMarkets;
    }

    public List<Integer> getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(List<Integer> reportTypes) {
        this.reportTypes = reportTypes;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

    public void setAscendingOrder(Integer ascendingOrder) {
        this.ascendingOrder = ascendingOrder;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isOtherType() {
        return otherType;
    }

    public void setOtherType(boolean otherType) {
        this.otherType = otherType;
    }
}
