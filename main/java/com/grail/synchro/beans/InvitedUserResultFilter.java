package com.grail.synchro.beans;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/9/15
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class InvitedUserResultFilter {

    private String keyword;
    private String sortField;
    private Integer ascendingOrder;
    private Long invitedBy;
    private Integer start;
    private Integer limit;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public Long getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
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
}
