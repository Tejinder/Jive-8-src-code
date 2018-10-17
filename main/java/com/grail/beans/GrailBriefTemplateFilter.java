package com.grail.beans;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/6/15
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailBriefTemplateFilter {
    private String keyword;
    private List<Long> initiators;
    private List<Long> endMarkets;
    private List<Integer> methodologies;
    private Date deliveryDateFrom;
    private Date deliveryDateTo;
    private List<Long> batContacts;
    private Integer start;
    private Integer limit;
    private String sortField;
    private Integer ascendingOrder;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<Long> getInitiators() {
        return initiators;
    }

    public void setInitiators(List<Long> initiators) {
        this.initiators = initiators;
    }

    public List<Long> getEndMarkets() {
        return endMarkets;
    }

    public void setEndMarkets(List<Long> endMarkets) {
        this.endMarkets = endMarkets;
    }

    public List<Integer> getMethodologies() {
        return methodologies;
    }

    public void setMethodologies(List<Integer> methodologies) {
        this.methodologies = methodologies;
    }

    public Date getDeliveryDateFrom() {
        return deliveryDateFrom;
    }

    public void setDeliveryDateFrom(Date deliveryDateFrom) {
        this.deliveryDateFrom = deliveryDateFrom;
    }

    public Date getDeliveryDateTo() {
        return deliveryDateTo;
    }

    public void setDeliveryDateTo(Date deliveryDateTo) {
        this.deliveryDateTo = deliveryDateTo;
    }

    public List<Long> getBatContacts() {
        return batContacts;
    }

    public void setBatContacts(List<Long> batContacts) {
        this.batContacts = batContacts;
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
}
